package ru.algeps.sparrow.worker.processor.http;

import java.io.IOException;
import java.nio.channels.ByteChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.algeps.sparrow.message.HttpVersion;
import ru.algeps.sparrow.message.request.domain.HttpRequest;
import ru.algeps.sparrow.message.request.domain.exceptions.http.HttpRequestException;
import ru.algeps.sparrow.message.request.domain.mediatype.MediaType;
import ru.algeps.sparrow.message.request.parser.http.HttpRequestParser;
import ru.algeps.sparrow.message.response.domain.HttpResponse;
import ru.algeps.sparrow.message.response.domain.HttpStatusCode;
import ru.algeps.sparrow.util.ServerErrorUtil;
import ru.algeps.sparrow.worker.dispatcher.http.HttpDispatcher;
import ru.algeps.sparrow.worker.handler.http.HttpRequestHandler;
import ru.algeps.sparrow.worker.processor.RequestProcessor;

/** Выполняет обработку запросов в рамках одной Http-сессии. */
public class HttpRequestProcessor implements RequestProcessor {
  private final Logger log;
  private final HttpDispatcher httpDispatcher;
  private final HttpVersion httpVersion;

  public HttpRequestProcessor(String name, HttpVersion httpVersion, HttpDispatcher httpDispatcher) {
    this.log = LoggerFactory.getLogger(name);
    this.httpDispatcher = httpDispatcher;
    this.httpVersion = httpVersion;
  }

  public void handle(ByteChannel byteChannel) {
    try {
      boolean isNeedOpening = true;
      Thread currentThread = Thread.currentThread();
      HttpRequestParser<?, ?> parser = HttpRequestParser.connect(byteChannel, httpVersion);
      while (isNeedOpening && !currentThread.isInterrupted()) {
        isNeedOpening = doHandle(byteChannel, parser);
      }
    } catch (IOException e) {
      log.error("Exception in 'HttpRequestProcessor': '%s'".formatted(log.getName()), e);
    }
  }

  /** Выбирает у httpDispatcher обработчик сообщений. */
  private boolean doHandle(ByteChannel channel, HttpRequestParser<?, ?> parser) throws IOException {
    HttpRequest httpRequest = parser.getHttpRequest();
    try {
      HttpRequestHandler httpRequestHandler = httpDispatcher.httpRoute(httpRequest);
      return httpRequestHandler.handle(httpRequest, channel);
    } catch (HttpRequestException httpRequestException) {
      channel.write(httpRequestException.getHttpResponse().toByteBuffer());
      return false;
    } catch (Exception e) {
      HttpResponse httpResponse =
          HttpResponse.responseFor(httpVersion)
              .statusCode(HttpStatusCode.INTERNAL_SERVER_ERROR)
              .addBody(ServerErrorUtil.getPageWithException(e), MediaType.Text.HTML);
      channel.write(httpResponse.toByteBuffer());
      return false;
    }
  }
}
