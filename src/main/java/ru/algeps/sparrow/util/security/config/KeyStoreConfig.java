package ru.algeps.sparrow.util.security.config;

import ru.algeps.sparrow.util.security.algorithms.KeyStoreType;

public class KeyStoreConfig {
  protected final String path;
  protected final String password;
  protected final KeyStoreType keyStoreType;
  protected final String keyStoreProviderName;

  public KeyStoreConfig(String path, String password, KeyStoreType keyStoreType) {
    this(path, password, keyStoreType, null);
  }

  public KeyStoreConfig(String path, String password, KeyStoreType keyStoreType, String keyStoreProviderName) {
    this.path = path;
    this.password = password;
    this.keyStoreType = keyStoreType;
    this.keyStoreProviderName = keyStoreProviderName;
  }

  public String getPath() {
    return path;
  }

  public String getPassword() {
    return password;
  }

  public KeyStoreType getKeyStoreType() {
    return keyStoreType;
  }

  public String getKeyStoreProviderName() {
    return keyStoreProviderName;
  }
}
