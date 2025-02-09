package ru.algeps.sparrow.worker.server;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.ArrayList;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.algeps.sparrow.config.Protocol;
import ru.algeps.sparrow.message.HttpVersion;
import ru.algeps.sparrow.test_utils.TestSecurityLoad;
import ru.algeps.sparrow.test_utils.TestUtil;
import ru.algeps.sparrow.util.security.algorithms.*;
import ru.algeps.sparrow.util.security.config.KeyManagerConfig;
import ru.algeps.sparrow.util.security.config.KeyStoreConfig;
import ru.algeps.sparrow.util.security.config.TrustManagerConfig;
import ru.algeps.sparrow.worker.processor.RequestProcessor;
import ru.algeps.sparrow.worker.server.stcp.STcpServer;
import ru.algeps.sparrow.worker.server.stcp.STcpServerConfig;

class ServerFactoryTest extends TestSecurityLoad {
  final KeyStoreType keyStoreType = KeyStoreType.PKCS12;
  final KeyStoreConfig keyStoreConfig =
      new KeyStoreConfig("sparrow_keystore_with_rsa.p12", null, keyStoreType);
  final KeyManagerAlgorithm keyManagerAlgorithm = KeyManagerAlgorithm.PKIX;
  final TrustManagerAlgorithm trustManagerAlgorithm = TrustManagerAlgorithm.PKIX;
  //
  Protocol protocol = Protocol.HTTPS_1_1;
  String name = "test-stcp-server";
  Integer port = 0;
  String strictHost = "localhost";
  SslAlgorithm sslAlgorithm = SslAlgorithm.TLS_1_2;
  String keyManagerPassword = "password";

  @Test
  @DisplayName("Загружает keystore и создаёт защищённый TCP сервер открывая указанный порт")
  void test_create_securityTcp() {
    STcpServerConfig config = getSTcpServerConfig();

    Server server = assertDoesNotThrow(() -> ServerFactory.create(protocol, config));
    assertInstanceOf(STcpServer.class, server);
    assertDoesNotThrow(server::start);
    await("Ожидание запуска сервера")
        .atMost(Duration.ofSeconds(5))
        .pollInterval(Duration.ofSeconds(1))
        .pollDelay(Duration.ofSeconds(2))
        .untilAsserted(
            () ->
                assertTrue(
                    server.getServerState().isRunning(),
                    () ->
                        "Сервер не смог запуститься за указанное время. Описание:"
                            + server.getServerState()));
    assertDoesNotThrow(server::stop);
  }

  STcpServerConfig getSTcpServerConfig() {
    RequestProcessor requestProcessor =
        TestUtil.createRequestProcessor(name, HttpVersion.HTTP_1_1, new ArrayList<>());

    TrustManagerConfig trustManagerConfig =
        new TrustManagerConfig(keyStoreConfig, trustManagerAlgorithm);
    KeyManagerConfig keyManagerConfig =
        new KeyManagerConfig(keyStoreConfig, keyManagerPassword, keyManagerAlgorithm);
    SecureRandomAlgorithm secureRandomAlgorithm = SecureRandomAlgorithm.SHA1PRNG;
    return new STcpServerConfig(
        name,
        port,
        requestProcessor,
        strictHost,
        sslAlgorithm,
        trustManagerConfig,
        keyManagerConfig,
        secureRandomAlgorithm);
  }
}
