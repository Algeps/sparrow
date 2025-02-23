package ru.algeps.sparrow.worker.server.stcp;

import ru.algeps.sparrow.util.security.config.KeyManagerConfig;
import ru.algeps.sparrow.util.security.algorithms.SecureRandomAlgorithm;
import ru.algeps.sparrow.util.security.algorithms.SslAlgorithm;
import ru.algeps.sparrow.util.security.config.TrustManagerConfig;
import ru.algeps.sparrow.worker.processor.RequestProcessor;
import ru.algeps.sparrow.worker.server.tcp.TcpServerConfig;

public class STcpServerConfig extends TcpServerConfig {
  protected final String strictHost;
  protected final SslAlgorithm sslAlgorithm;
  protected final TrustManagerConfig trustManagerConfig;
  protected final KeyManagerConfig keyManagerConfig;
  protected final SecureRandomAlgorithm secureRandomAlgorithm;

  public STcpServerConfig(
      String name,
      Integer port,
      RequestProcessor requestProcessor,
      String strictHost,
      SslAlgorithm sslAlgorithm,
      TrustManagerConfig trustManagerConfig,
      KeyManagerConfig keyManagerConfig,
      SecureRandomAlgorithm secureRandomAlgorithm) {
    super(name, port, requestProcessor);
    this.strictHost = strictHost;
    this.sslAlgorithm = sslAlgorithm;
    this.trustManagerConfig = trustManagerConfig;
    this.keyManagerConfig = keyManagerConfig;
    this.secureRandomAlgorithm = secureRandomAlgorithm;
  }

  public STcpServerConfig(
      String name,
      Integer port,
      int backlog,
      RequestProcessor requestProcessor,
      String strictHost,
      SslAlgorithm sslAlgorithm,
      TrustManagerConfig trustManagerConfig,
      KeyManagerConfig keyManagerConfig,
      SecureRandomAlgorithm secureRandomAlgorithm) {
    super(name, port, backlog, requestProcessor);
    this.strictHost = strictHost;
    this.sslAlgorithm = sslAlgorithm;
    this.trustManagerConfig = trustManagerConfig;
    this.keyManagerConfig = keyManagerConfig;
    this.secureRandomAlgorithm = secureRandomAlgorithm;
  }

  public String getStrictHost() {
    return strictHost;
  }

  public SslAlgorithm getSslAlgorithm() {
    return sslAlgorithm;
  }

  public TrustManagerConfig getTrustManagerConfig() {
    return trustManagerConfig;
  }

  public KeyManagerConfig getKeyManagerConfig() {
    return keyManagerConfig;
  }

  public SecureRandomAlgorithm getSecureRandomAlgorithm() {
    return secureRandomAlgorithm;
  }
}
