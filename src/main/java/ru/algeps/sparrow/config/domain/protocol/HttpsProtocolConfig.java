package ru.algeps.sparrow.config.domain.protocol;

import java.util.List;
import java.util.Objects;
import ru.algeps.sparrow.config.Protocol;
import ru.algeps.sparrow.config.domain.provider.ProviderConfig;
import ru.algeps.sparrow.util.security.algorithms.KeyManagerAlgorithm;
import ru.algeps.sparrow.util.security.algorithms.KeyStoreType;
import ru.algeps.sparrow.util.security.algorithms.SecureRandomAlgorithm;
import ru.algeps.sparrow.util.security.algorithms.SslAlgorithm;
import ru.algeps.sparrow.util.security.algorithms.TrustManagerAlgorithm;

public class HttpsProtocolConfig extends HttpProtocolConfig {
  private final String keystorePath;
  private final String strictHostname;
  private final ProviderConfig providerConfig;
  // keystore
  private final KeyStoreType keyStoreType;
  // ssl
  private final SslAlgorithm sslAlgorithm;
  // key manager
  private final KeyManagerAlgorithm keyManagerAlgorithm;
  // trust manager
  private final TrustManagerAlgorithm trustManagerAlgorithm;
  // secure random
  private final SecureRandomAlgorithm secureRandomAlgorithm;

  public HttpsProtocolConfig(
      Protocol protocol,
      String keystorePath,
      String strictHostname,
      KeyStoreType keyStoreType,
      SslAlgorithm sslAlgorithm,
      KeyManagerAlgorithm keyManagerAlgorithm,
      TrustManagerAlgorithm trustManagerAlgorithm,
      SecureRandomAlgorithm secureRandomAlgorithm) {
    super(protocol);
    this.keystorePath = keystorePath;
    this.strictHostname = strictHostname;
    this.providerConfig = new ProviderConfig();
    this.keyStoreType = keyStoreType;
    this.sslAlgorithm = sslAlgorithm;
    this.keyManagerAlgorithm = keyManagerAlgorithm;
    this.trustManagerAlgorithm = trustManagerAlgorithm;
    this.secureRandomAlgorithm = secureRandomAlgorithm;
  }

  public HttpsProtocolConfig(
      Protocol protocol,
      String keystorePath,
      String strictHostname,
      ProviderConfig providerConfig,
      KeyStoreType keyStoreType,
      SslAlgorithm sslAlgorithm,
      KeyManagerAlgorithm keyManagerAlgorithm,
      TrustManagerAlgorithm trustManagerAlgorithm,
      SecureRandomAlgorithm secureRandomAlgorithm) {
    super(protocol);
    this.keystorePath = keystorePath;
    this.strictHostname = strictHostname;
    this.providerConfig = providerConfig;
    this.keyStoreType = keyStoreType;
    this.sslAlgorithm = sslAlgorithm;
    this.keyManagerAlgorithm = keyManagerAlgorithm;
    this.trustManagerAlgorithm = trustManagerAlgorithm;
    this.secureRandomAlgorithm = secureRandomAlgorithm;
  }

  public String getKeystorePath() {
    return keystorePath;
  }

  public String getStrictHostname() {
    return strictHostname;
  }

  public ProviderConfig getProviderConfig() {
    return providerConfig;
  }

  public KeyStoreType getKeyStoreType() {
    return keyStoreType;
  }

  public SslAlgorithm getSslAlgorithm() {
    return sslAlgorithm;
  }

  public KeyManagerAlgorithm getKeyManagerAlgorithm() {
    return keyManagerAlgorithm;
  }

  public TrustManagerAlgorithm getTrustManagerAlgorithm() {
    return trustManagerAlgorithm;
  }

  public SecureRandomAlgorithm getSecureRandomAlgorithm() {
    return secureRandomAlgorithm;
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof HttpsProtocolConfig that)) return false;
    if (!super.equals(object)) return false;
    return Objects.equals(keystorePath, that.keystorePath)
        && Objects.equals(strictHostname, that.strictHostname)
        && Objects.equals(providerConfig, that.providerConfig)
        && keyStoreType == that.keyStoreType
        && sslAlgorithm == that.sslAlgorithm
        && keyManagerAlgorithm == that.keyManagerAlgorithm
        && trustManagerAlgorithm == that.trustManagerAlgorithm
        && secureRandomAlgorithm == that.secureRandomAlgorithm;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        super.hashCode(),
        keystorePath,
        strictHostname,
        providerConfig,
        keyStoreType,
        sslAlgorithm,
        keyManagerAlgorithm,
        trustManagerAlgorithm,
        secureRandomAlgorithm);
  }

  @Override
  public String toString() {
    return "HttpsProtocolConfig{"
        + "protocol="
        + protocol
        + ", secureRandomAlgorithm="
        + secureRandomAlgorithm
        + ", trustManagerAlgorithm="
        + trustManagerAlgorithm
        + ", keyManagerAlgorithm="
        + keyManagerAlgorithm
        + ", sslAlgorithm="
        + sslAlgorithm
        + ", keyStoreType="
        + keyStoreType
        + ", providerConfig="
        + providerConfig
        + ", strictHostname='"
        + strictHostname
        + '\''
        + ", keystorePath='"
        + keystorePath
        + '\''
        + '}';
  }
}
