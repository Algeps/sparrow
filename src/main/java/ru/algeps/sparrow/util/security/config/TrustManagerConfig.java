package ru.algeps.sparrow.util.security.config;

import ru.algeps.sparrow.util.security.algorithms.KeyStoreType;
import ru.algeps.sparrow.util.security.algorithms.TrustManagerAlgorithm;

public class TrustManagerConfig extends KeyStoreConfig {
  private final TrustManagerAlgorithm algorithm;
  private final String trustManagerProviderName;

  public TrustManagerConfig(KeyStoreConfig keyStoreConfig, TrustManagerAlgorithm algorithm) {
    this(
        keyStoreConfig.path,
        keyStoreConfig.password,
        keyStoreConfig.keyStoreType,
        null,
        algorithm,
        null);
  }

  public TrustManagerConfig(
      KeyStoreConfig keyStoreConfig,
      TrustManagerAlgorithm algorithm,
      String trustManagerProviderName) {
    this(
        keyStoreConfig.path,
        keyStoreConfig.password,
        keyStoreConfig.keyStoreType,
        null,
        algorithm,
        trustManagerProviderName);
  }

  public TrustManagerConfig(
      String keyStorePathString,
      String keyStorePassword,
      KeyStoreType keyStoreType,
      TrustManagerAlgorithm algorithm) {
    super(keyStorePathString, keyStorePassword, keyStoreType, null);
    this.algorithm = algorithm;
    this.trustManagerProviderName = null;
  }

  public TrustManagerConfig(
      String keyStorePathString,
      String keyStorePassword,
      KeyStoreType keyStoreType,
      String keyStoreProviderName,
      TrustManagerAlgorithm algorithm,
      String trustManagerProviderName) {
    super(keyStorePathString, keyStorePassword, keyStoreType, keyStoreProviderName);
    this.algorithm = algorithm;
    this.trustManagerProviderName = trustManagerProviderName;
  }

  public TrustManagerConfig(
      String keyStorePathString,
      String keyStorePassword,
      KeyStoreType keyStoreType,
      TrustManagerAlgorithm algorithm,
      String trustManagerProviderName) {
    super(keyStorePathString, keyStorePassword, keyStoreType);
    this.algorithm = algorithm;
    this.trustManagerProviderName = trustManagerProviderName;
  }

  public TrustManagerAlgorithm getAlgorithm() {
    return algorithm;
  }

  public String getTrustManagerProviderName() {
    return trustManagerProviderName;
  }
}
