package ru.algeps.sparrow.request.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.HttpEntities;
import org.apache.hc.core5.http.message.BasicHeader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.algeps.sparrow.message.HttpVersion;
import ru.algeps.sparrow.message.request.domain.HttpRequest1_1;
import ru.algeps.sparrow.message.request.domain.mediatype.MediaType;
import ru.algeps.sparrow.message.request.parser.http.HttpRequestParser;
import ru.algeps.sparrow.message.response.domain.HttpResponse;
import ru.algeps.sparrow.message.response.domain.HttpResponse1_1;
import ru.algeps.sparrow.message.response.domain.HttpStatusCode;
import ru.algeps.sparrow.test_utils.TestUtil;
import ru.algeps.sparrow.worker.processor.RequestProcessor;
import ru.algeps.sparrow.worker.server.Server;
import ru.algeps.sparrow.worker.server.tcp.TcpServer;
import ru.algeps.sparrow.worker.server.tcp.TcpServerConfig;

/** Интеграционные тесты для проверки трейлеров (Http 1.1). */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TrailersIT {
  Logger logger = LoggerFactory.getLogger(TrailersIT.class);
  int port = 0;
  HttpVersion httpVersion = HttpVersion.HTTP_1_1;
  Map<String, String> serverTrailers =
      Map.of("Server-Timing", "custom-metric;dur=100.45;desc=\"Sparrow custom metrics\"");


  @Test
  void test_trailers_withBody() throws Exception {
    // конфигурация ответа
    HttpResponse1_1 httpResponse =
        HttpResponse.responseFor(httpVersion, HttpResponse1_1.class)
            .statusCode(HttpStatusCode.OK)
            .addHeader("Connection", "close")
            .addHeader("X-any-header", "null");
    httpResponse.addBody("{}".getBytes(StandardCharsets.UTF_8), MediaType.Application.JSON);
    for (Map.Entry<String, String> entry : serverTrailers.entrySet()) {
      httpResponse.addTrailer(entry.getKey(), entry.getValue());
    }

    // запуск сервера, в процессе парсинга запроса мы получим трейлеры от клиента
    Map<String, String> fromClientTrailers = new HashMap<>();
    RequestProcessor requestProcessor =
        createRequestProcessor(httpVersion, httpResponse, fromClientTrailers);
    TcpServerConfig tcpServerConfig = new TcpServerConfig("test-server", port, requestProcessor);
    Server server = new TcpServer(tcpServerConfig);
    server.start();
    TestUtil.validateStartServer(server);

    // выполняем запрос и получаем трейлеры
    int usePort = server.getServerState().getFirstUsePort();
    Map<String, String> clientTrailers = Map.of("X-TrailerHeader", "empty-value");
    Map<String, String> responseTrailersFromServer =
        sendClientRequest_andGetTrailersFromResponse(usePort, clientTrailers);

    // приводим в нижний регистр
    responseTrailersFromServer =
        responseTrailersFromServer.entrySet().stream()
            .collect(
                Collectors.toMap(
                    entry -> entry.getKey().toLowerCase(),
                    entry -> entry.getValue().toLowerCase()));
    fromClientTrailers =
        fromClientTrailers.entrySet().stream()
            .collect(
                Collectors.toMap(
                    entry -> entry.getKey().toLowerCase(),
                    entry -> entry.getValue().toLowerCase()));
    // валидация трейлеров, полученных от сервера
    for (Map.Entry<String, String> serverTrailer : serverTrailers.entrySet()) {
      assertEquals(
          serverTrailer.getValue().toLowerCase(),
          responseTrailersFromServer.get(serverTrailer.getKey().toLowerCase()));
    }
    // валидация трейлеров, полученных от клиента
    for (Map.Entry<String, String> clientTrailer : clientTrailers.entrySet()) {
      assertEquals(
          clientTrailer.getValue().toLowerCase(),
          fromClientTrailers.get(clientTrailer.getKey().toLowerCase()));
    }

    server.stop();
  }

  // todo обединить тест, добавив провайдер

  @Test
  void test_trailers_withoutBody() throws Exception {
    // конфигурация ответа
    HttpResponse1_1 httpResponse =
        HttpResponse.responseFor(httpVersion, HttpResponse1_1.class)
            .statusCode(HttpStatusCode.OK)
            .addHeader("Connection", "close")
            .addHeader("X-any-header", "null");
    for (Map.Entry<String, String> entry : serverTrailers.entrySet()) {
      httpResponse.addTrailer(entry.getKey(), entry.getValue());
    }

    // запуск сервера, в процессе парсинга запроса мы получим трейлеры от клиента
    Map<String, String> fromClientTrailers = new HashMap<>();
    RequestProcessor requestProcessor =
        createRequestProcessor(httpVersion, httpResponse, fromClientTrailers);
    TcpServerConfig tcpServerConfig = new TcpServerConfig("test-server", port, requestProcessor);
    Server server = new TcpServer(tcpServerConfig);
    server.start();
    TestUtil.validateStartServer(server);

    // выполняем запрос и получаем трейлеры
    int usePort = server.getServerState().getFirstUsePort();
    Map<String, String> clientTrailers = Map.of("X-TrailerHeader", "empty-value");
    Map<String, String> fromServerTrailers =
        sendClientRequest_andGetTrailersFromResponse(usePort, clientTrailers);

    // приводим в нижний регистр
    fromServerTrailers =
        fromServerTrailers.entrySet().stream()
            .collect(
                Collectors.toMap(
                    entry -> entry.getKey().toLowerCase(),
                    entry -> entry.getValue().toLowerCase()));
    fromClientTrailers =
        fromClientTrailers.entrySet().stream()
            .collect(
                Collectors.toMap(
                    entry -> entry.getKey().toLowerCase(),
                    entry -> entry.getValue().toLowerCase()));
    // валидация трейлеров, полученных от сервера
    for (Map.Entry<String, String> serverTrailer : serverTrailers.entrySet()) {
      assertEquals(
          serverTrailer.getValue().toLowerCase(),
          fromServerTrailers.get(serverTrailer.getKey().toLowerCase()));
    }
    // валидация трейлеров, полученных от клиента
    for (Map.Entry<String, String> clientTrailer : clientTrailers.entrySet()) {
      assertEquals(
          clientTrailer.getValue().toLowerCase(),
          fromClientTrailers.get(clientTrailer.getKey().toLowerCase()));
    }

    server.stop();
  }

  static Map<String, String> sendClientRequest_andGetTrailersFromResponse(
      int port, Map<String, String> requestTrailers) throws Exception {
    String uri = "http://localhost:" + port;

    HttpPost httpPost = new HttpPost(uri);
    Header[] headerTrailers =
        requestTrailers.entrySet().stream()
            .map(entry -> new BasicHeader(entry.getKey(), entry.getValue()))
            .toArray(Header[]::new);
    HttpEntity httpEntity =
        HttpEntities.create("some value", ContentType.TEXT_PLAIN, headerTrailers);
    httpPost.setEntity(httpEntity);

    HttpResponse1_1 httpResponse1_1 = TestUtil.sendClientRequestHttp1_1(httpPost);

    return httpResponse1_1.trailers().entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getKey, entry -> String.join("", entry.getValue())));
  }

  RequestProcessor createRequestProcessor(
      HttpVersion httpVersion, HttpResponse httpResponse, Map<String, String> trailers) {
    return byteChannel -> {
      HttpRequest1_1 httpRequest =
          (HttpRequest1_1) HttpRequestParser.connect(byteChannel, httpVersion).getHttpRequest();
      logger.info("[Server] Received request: {}", httpRequest);
      httpRequest
          .trailers()
          .forEach((key, value) -> value.forEach(trailerValue -> trailers.put(key, trailerValue)));

      ByteBuffer byteBufferResponse = httpResponse.toByteBuffer();
      logger.info("[Server] Sending response: {}", new String(byteBufferResponse.array()));
      byteChannel.write(byteBufferResponse);
    };
  }
}
