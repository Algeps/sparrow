package ru.algeps.sparrow.util.security.config;

import ru.algeps.sparrow.util.security.algorithms.KeyManagerAlgorithm;
import ru.algeps.sparrow.util.security.algorithms.KeyStoreType;

public class KeyManagerConfig extends KeyStoreConfig {
  private final String keyManagerPassword;
  private final KeyManagerAlgorithm algorithm;
  private final String keyManagerProviderName;

  public KeyManagerConfig(
      KeyStoreConfig keyStoreConfig, String keyManagerPassword, KeyManagerAlgorithm algorithm) {
    super(keyStoreConfig.path, keyStoreConfig.password, keyStoreConfig.keyStoreType);
    this.keyManagerPassword = keyManagerPassword;
    this.algorithm = algorithm;
    this.keyManagerProviderName = null;
  }

  public KeyManagerConfig(
      KeyStoreConfig keyStoreConfig,
      String keyManagerPassword,
      KeyManagerAlgorithm algorithm,
      String keyManagerProviderName) {
    super(keyStoreConfig.path, keyStoreConfig.password, keyStoreConfig.keyStoreType);
    this.keyManagerPassword = keyManagerPassword;
    this.algorithm = algorithm;
    this.keyManagerProviderName = keyManagerProviderName;
  }

  public KeyManagerConfig(
      String keyStorePathString,
      String keyStorePassword,
      KeyStoreType keyStoreType,
      String keyManagerPassword,
      KeyManagerAlgorithm algorithm) {
    this(
        keyStorePathString,
        keyStorePassword,
        keyStoreType,
        null,
        keyManagerPassword,
        algorithm,
        null);
  }

  public KeyManagerConfig(
      String path,
      String password,
      KeyStoreType keyStoreType,
      String keyManagerPassword,
      KeyManagerAlgorithm algorithm,
      String keyManagerProvider) {
    super(path, password, keyStoreType);
    this.keyManagerPassword = keyManagerPassword;
    this.algorithm = algorithm;
    this.keyManagerProviderName = keyManagerProvider;
  }

  public KeyManagerConfig(
      String path,
      String password,
      KeyStoreType keyStoreType,
      String keyStoreProvider,
      String keyManagerPassword,
      KeyManagerAlgorithm algorithm,
      String keyManagerProvider) {
    super(path, password, keyStoreType, keyStoreProvider);
    this.keyManagerPassword = keyManagerPassword;
    this.algorithm = algorithm;
    this.keyManagerProviderName = keyManagerProvider;
  }

  public KeyManagerAlgorithm getAlgorithm() {
    return algorithm;
  }

  public String getKeyManagerPassword() {
    return keyManagerPassword;
  }

  public String getKeyManagerProviderName() {
    return keyManagerProviderName;
  }
}
