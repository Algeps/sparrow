package ru.algeps.sparrow.util.security;

import ru.algeps.sparrow.util.security.algorithms.SecureRandomAlgorithm;
import ru.algeps.sparrow.util.security.config.KeyManagerConfig;
import ru.algeps.sparrow.util.security.config.KeyStoreConfig;
import ru.algeps.sparrow.util.security.config.TrustManagerConfig;
import ru.algeps.sparrow.util.security.exception.KeyStoreUtilException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;

public final class KeyStoreUtil extends SecurityContextLoad {
  private KeyStoreUtil() {}

  public static TrustManagerFactory getTrustManagerFactory(TrustManagerConfig trustManagerConfig)
      throws KeyStoreUtilException {
    try {
      KeyStore trustKeyStore = getKeyStoreFromFile(trustManagerConfig);

      TrustManagerFactory trustManagerFactory =
          trustManagerConfig.getTrustManagerProviderName() == null
              ? TrustManagerFactory.getInstance(
                  trustManagerConfig.getAlgorithm().getAlgorithmName())
              : TrustManagerFactory.getInstance(
                  trustManagerConfig.getAlgorithm().getAlgorithmName(),
                  trustManagerConfig.getTrustManagerProviderName());
      trustManagerFactory.init(trustKeyStore);

      return trustManagerFactory;
    } catch (NoSuchAlgorithmException
        | KeyStoreException
        | IOException
        | NoSuchProviderException e) {
      throw new KeyStoreUtilException(e);
    }
  }

  public static KeyManagerFactory getKeyManagerFactory(KeyManagerConfig keyManagerConfig)
      throws KeyStoreUtilException {
    if (keyManagerConfig == null) {
      throw new IllegalArgumentException("The 'KeyManagerConfig' parameter cannot be null!");
    }

    if (keyManagerConfig.getAlgorithm() == null) {
      throw new IllegalArgumentException(
          "The 'algorithm' parameter for 'KeyManagerConfig' cannot be null!");
    }

    if (keyManagerConfig.getKeyManagerPassword() == null) {
      throw new IllegalArgumentException(
          "The 'keyManagerPassword' parameter for 'KeyManagerConfig' cannot be null!");
    }

    try {
      KeyStore keyStore = getKeyStoreFromFile(keyManagerConfig);

      KeyManagerFactory keyManagerFactory =
          keyManagerConfig.getKeyManagerProviderName() == null
              ? KeyManagerFactory.getInstance(keyManagerConfig.getAlgorithm().getAlgorithmName())
              : KeyManagerFactory.getInstance(
                  keyManagerConfig.getAlgorithm().getAlgorithmName(),
                  keyManagerConfig.getKeyManagerProviderName());
      keyManagerFactory.init(keyStore, keyManagerConfig.getKeyManagerPassword().toCharArray());

      return keyManagerFactory;
    } catch (NoSuchAlgorithmException
        | KeyStoreException
        | IOException
        | UnrecoverableKeyException
        | NoSuchProviderException e) {
      throw new KeyStoreUtilException(e);
    }
  }

  public static SecureRandom getSecureRandom(SecureRandomAlgorithm secureRandomAlgorithm)
      throws KeyStoreUtilException {
    if (secureRandomAlgorithm == null) {
      return null;
    }

    try {
      return SecureRandom.getInstance(secureRandomAlgorithm.getAlgorithmName());
    } catch (NoSuchAlgorithmException e) {
      throw new KeyStoreUtilException(e);
    }
  }

  public static KeyStore getKeyStoreFromFile(KeyStoreConfig keyStoreConfig)
      throws KeyStoreUtilException {
    try {
      if (keyStoreConfig == null) {
        throw new IllegalArgumentException();
      }

      if (keyStoreConfig.getKeyStoreType() == null) {
        throw new IllegalArgumentException(
            "The 'keyStoreType' parameter for 'KeyStoreConfig' cannot be null!");
      }

      KeyStore keyStore =
          keyStoreConfig.getKeyStoreProviderName() == null
              ? KeyStore.getInstance(keyStoreConfig.getKeyStoreType().getType())
              : KeyStore.getInstance(
                  keyStoreConfig.getKeyStoreType().getType(),
                  keyStoreConfig.getKeyStoreProviderName());

      if (keyStoreConfig.getPassword() == null && keyStoreConfig.getPath() == null) {
        keyStore.load(null, null);
        return keyStore;
      }

      if (keyStoreConfig.getPath() == null) {
        keyStore.load(null, keyStoreConfig.getPassword().toCharArray());
        return keyStore;
      }

      if (keyStoreConfig.getPassword() == null) {
        try (FileInputStream trustStoreFileInputStream =
            new FileInputStream(keyStoreConfig.getPath())) {
          keyStore.load(trustStoreFileInputStream, null);
          return keyStore;
        }
      }

      try (FileInputStream trustStoreFileInputStream =
          new FileInputStream(keyStoreConfig.getPath())) {
        keyStore.load(trustStoreFileInputStream, keyStoreConfig.getPassword().toCharArray());
        return keyStore;
      }
    } catch (IOException
        | CertificateException
        | KeyStoreException
        | NoSuchProviderException
        | NoSuchAlgorithmException e) {
      throw new KeyStoreUtilException(e);
    }
  }
}
