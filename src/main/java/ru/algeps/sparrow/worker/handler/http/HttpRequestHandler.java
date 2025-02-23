package ru.algeps.sparrow.worker.handler.http;

import java.io.IOException;
import java.nio.channels.ByteChannel;
import ru.algeps.sparrow.message.request.domain.HttpMethod;
import ru.algeps.sparrow.message.request.domain.HttpRequest;
import ru.algeps.sparrow.message.request.domain.exceptions.http.HttpRequestException;
import ru.algeps.sparrow.message.request.parser.http.HttpRequestParser;
import ru.algeps.sparrow.worker.handler.RequestHandler;

public interface HttpRequestHandler extends RequestHandler {

  @Override
  default void handle(ByteChannel channel) throws IOException {
    HttpRequestParser httpRequestParser = getHttpRequestParser(channel);
    Thread currentThread = Thread.currentThread();
    boolean isCloseConnection = false;

    while (!isCloseConnection || !currentThread.isInterrupted()) {
      HttpRequest httpRequest = httpRequestParser.getHttpRequest();
      isCloseConnection = this.handle(httpRequest, channel);
    }
  }

  /**
   * Обрабатывает успешно один запрос за раз или выбрасывает исключение. Возвращает true если нужно
   * ещё продолжать обрабатывать соединение.
   */
  boolean handle(HttpRequest httpRequest, ByteChannel byteChannel) throws HttpRequestException;

  HttpRequestParser getHttpRequestParser(ByteChannel byteChannel) throws IOException;

  HttpMethod getHttpMethodHandle();
}
