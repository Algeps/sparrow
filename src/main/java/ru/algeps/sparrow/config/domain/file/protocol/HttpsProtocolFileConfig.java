package ru.algeps.sparrow.config.domain.file.protocol;

import java.util.Objects;
import ru.algeps.sparrow.config.domain.file.provider.ProviderFileConfig;
import ru.algeps.sparrow.util.security.algorithms.KeyManagerAlgorithm;
import ru.algeps.sparrow.util.security.algorithms.KeyStoreType;
import ru.algeps.sparrow.util.security.algorithms.SecureRandomAlgorithm;
import ru.algeps.sparrow.util.security.algorithms.SslAlgorithm;
import ru.algeps.sparrow.util.security.algorithms.TrustManagerAlgorithm;

public class HttpsProtocolFileConfig extends HttpProtocolFileConfig {
  private String keystorePath;
  private String strictHostname;
  //
  private ProviderFileConfig providerConfig;
  // keystore
  private KeyStoreType keyStoreType;
  // ssl
  private SslAlgorithm sslAlgorithm;
  // key manager
  private KeyManagerAlgorithm keyManagerAlgorithm;
  // truster
  private TrustManagerAlgorithm trustManagerAlgorithm;
  // secureRandom
  private SecureRandomAlgorithm secureRandomAlgorithm;

  public String getKeystorePath() {
    return keystorePath;
  }

  public void setKeystorePath(String keystorePath) {
    this.keystorePath = keystorePath;
  }

  public String getStrictHostname() {
    return strictHostname;
  }

  public void setStrictHostname(String strictHostname) {
    this.strictHostname = strictHostname;
  }

  public ProviderFileConfig getProviderConfig() {
    return providerConfig;
  }

  public void setProviderConfig(ProviderFileConfig providerConfig) {
    this.providerConfig = providerConfig;
  }

  public KeyStoreType getKeyStoreType() {
    return keyStoreType;
  }

  public void setKeyStoreType(KeyStoreType keyStoreType) {
    this.keyStoreType = keyStoreType;
  }

  public SslAlgorithm getSslAlgorithm() {
    return sslAlgorithm;
  }

  public void setSslAlgorithm(SslAlgorithm sslAlgorithm) {
    this.sslAlgorithm = sslAlgorithm;
  }

  public KeyManagerAlgorithm getKeyManagerAlgorithm() {
    return keyManagerAlgorithm;
  }

  public void setKeyManagerAlgorithm(KeyManagerAlgorithm keyManagerAlgorithm) {
    this.keyManagerAlgorithm = keyManagerAlgorithm;
  }

  public TrustManagerAlgorithm getTrustManagerAlgorithm() {
    return trustManagerAlgorithm;
  }

  public void setTrustManagerAlgorithm(TrustManagerAlgorithm trustManagerAlgorithm) {
    this.trustManagerAlgorithm = trustManagerAlgorithm;
  }

  public SecureRandomAlgorithm getSecureRandomAlgorithm() {
    return secureRandomAlgorithm;
  }

  public void setSecureRandomAlgorithm(SecureRandomAlgorithm secureRandomAlgorithm) {
    this.secureRandomAlgorithm = secureRandomAlgorithm;
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof HttpsProtocolFileConfig that)) return false;
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
    return "HttpsProtocolFileConfig{"
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
