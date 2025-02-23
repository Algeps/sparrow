package ru.algeps.sparrow.worker.server;

import java.security.*;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import ru.algeps.sparrow.config.Protocol;
import ru.algeps.sparrow.util.security.KeyStoreUtil;
import ru.algeps.sparrow.util.security.config.KeyManagerConfig;
import ru.algeps.sparrow.util.security.config.TrustManagerConfig;
import ru.algeps.sparrow.util.security.exception.KeyStoreUtilException;
import ru.algeps.sparrow.worker.server.config.ServerConfig;
import ru.algeps.sparrow.worker.server.exception.ServerFactoryException;
import ru.algeps.sparrow.worker.server.stcp.*;
import ru.algeps.sparrow.worker.server.tcp.TcpServer;
import ru.algeps.sparrow.worker.server.tcp.TcpServerConfig;

public final class ServerFactory {
  private ServerFactory() {}

  public static Server create(Protocol protocol, ServerConfig serverConfig) {
    return switch (protocol) {
      case HTTP_1_1 -> {
        if (!(serverConfig instanceof TcpServerConfig)) {
          throw new ServerFactoryException("The configuration does not belong to the TCP server!");
        }
        yield create((TcpServerConfig) serverConfig);
      }
      case HTTPS_1_1 -> {
        if (!(serverConfig instanceof STcpServerConfig)) {
          throw new ServerFactoryException(
              "The configuration does not belong to the Security TCP server!");
        }
        yield create((STcpServerConfig) serverConfig);
      }
      case null -> throw new ServerFactoryException("Cannot find server and protocol");
    };
  }

  public static Server create(TcpServerConfig config) {
    return new TcpServer(config);
  }

  public static Server create(STcpServerConfig sTcpServerConfig) {
    TcpServerConfig config = mapToTcpServerConfig(sTcpServerConfig);

    if (sTcpServerConfig.getTrustManagerConfig() == null
        && sTcpServerConfig.getKeyManagerConfig() == null
        && sTcpServerConfig.getSecureRandomAlgorithm() == null) {
      return new STcpServer(config, sTcpServerConfig.getSslAlgorithm());
    }

    try {
      TrustManagerConfig trustManagerConfig = sTcpServerConfig.getTrustManagerConfig();
      TrustManagerFactory trustManagerFactory =
          KeyStoreUtil.getTrustManagerFactory(trustManagerConfig);

      KeyManagerConfig keyManagerConfig = sTcpServerConfig.getKeyManagerConfig();
      KeyManagerFactory keyManagerFactory = KeyStoreUtil.getKeyManagerFactory(keyManagerConfig);

      SecureRandom secureRandom =
          KeyStoreUtil.getSecureRandom(sTcpServerConfig.getSecureRandomAlgorithm());

      return new STcpServer(
          config,
          sTcpServerConfig.getSslAlgorithm(),
          sTcpServerConfig.getStrictHost(),
          trustManagerFactory.getTrustManagers(),
          keyManagerFactory.getKeyManagers(),
          secureRandom);
    } catch (KeyStoreUtilException e) {
      throw new ServerFactoryException(e);
    }
  }

  private static TcpServerConfig mapToTcpServerConfig(STcpServerConfig config) {
    return new TcpServerConfig(config.getName(), config.getPort(), config.getRequestProcessor());
  }
}
