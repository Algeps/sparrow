package ru.algeps.sparrow.worker.server.stcp;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static ru.algeps.sparrow.test_utils.TestUtil.validateStartServer;

import com.sun.net.httpserver.Headers;
import java.net.URI;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.HttpEntities;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.algeps.sparrow.message.HttpVersion;
import ru.algeps.sparrow.message.request.domain.HttpMethod;
import ru.algeps.sparrow.message.request.domain.HttpRequest;
import ru.algeps.sparrow.test_utils.SSLContextLoad;
import ru.algeps.sparrow.test_utils.TestUtil;
import ru.algeps.sparrow.worker.processor.RequestProcessor;
import ru.algeps.sparrow.worker.server.Server;
import ru.algeps.sparrow.worker.server.tcp.TcpServerConfig;

// todo добавить функционал Trailer и сжатия - полная поддержка HTTP (спросить у ИИ что ещё нужно)
// todo фаззинг-тестирование с помощью jazzer (после добавление всего необходимого в парсер)
// todo добавить функционал SHA-256 и стрибог2 512/256 хэш функции для заголовков (прямым текстом
//  написать, что Basic-auth не представляет из себя
//  безопасный протокол аутентификации без SSL)

class STcpServerTest extends SSLContextLoad {
  final String SERVER_NAME = "test-stcp-server";
  Integer port = 0;
  //
  Server server;
  List<HttpRequest> httpRequests = new ArrayList<>();

  /** Запуск сервера. */
  @BeforeAll
  public void beforeAll() throws Exception {
    super.beforeAll();
    RequestProcessor requestProcessor =
        TestUtil.createRequestProcessor(SERVER_NAME, HttpVersion.HTTP_1_1, httpRequests);
    TcpServerConfig tcpServerConfig = new TcpServerConfig(SERVER_NAME, port, requestProcessor);

    server =
        new STcpServer(
            tcpServerConfig, SSL_ALGORITHM, STRICT_HOST, trustManagers, keyManagers, secureRandom);
    assertDoesNotThrow(server::start);
  }

  @AfterAll
  void afterAll() {
    assertDoesNotThrow(server::stop);
  }

  @Test
  @DisplayName(
      "Создаёт защищённый TCP сервер. Клиент отправляет запрос. Проверяется правильная установка значений от парсера сервера.")
  void sTcpServer_integrationTest() throws Exception {
    validateStartServer(server);
    int firstUsePort = server.getServerState().getFirstUsePort();

    String uriPath = "/api/v1/static";
    String stringUri = "https://%s:%s%s".formatted(STRICT_HOST, firstUsePort, uriPath);
    String httpMethod = "POST";
    HttpUriRequestBase httpPost = new HttpUriRequestBase(httpMethod, URI.create(stringUri));
    String headerName = "Any-Header";
    String headerValue = "any value";
    httpPost.addHeader(headerName, headerValue);
    String stringJson = "{\"key\": \"value\"}";
    httpPost.setEntity(HttpEntities.create(stringJson, ContentType.APPLICATION_JSON));
    TestUtil.sendClientRequestHttps1_1(httpPost, sslContext);

    // проверка распарсеного запроса от клиента
    HttpRequest actualHttpRequest = httpRequests.getFirst();
    URI actualUri = actualHttpRequest.uri();
    HttpMethod actualHttpMethod = actualHttpRequest.httpMethod();
    Headers actualHeaders = actualHttpRequest.headers();
    byte[] actualBody = actualHttpRequest.body();
    String actualStringBody = new String(actualBody);
    assertEquals(uriPath, actualUri.getPath());
    assertEquals(httpMethod, actualHttpMethod.getName());
    assertEquals(headerValue, actualHeaders.getFirst(headerName));
    assertEquals(stringJson, actualStringBody);
  }
}
