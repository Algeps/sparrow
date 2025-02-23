package ru.algeps.sparrow.util.security;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.algeps.sparrow.test_utils.CryptoTestUtil;
import ru.algeps.sparrow.util.security.algorithms.KeyManagerAlgorithm;
import ru.algeps.sparrow.util.security.algorithms.KeyStoreType;
import ru.algeps.sparrow.util.security.algorithms.SecureRandomAlgorithm;
import ru.algeps.sparrow.util.security.algorithms.TrustManagerAlgorithm;
import ru.algeps.sparrow.util.security.config.KeyManagerConfig;
import ru.algeps.sparrow.util.security.config.KeyStoreConfig;
import ru.algeps.sparrow.util.security.config.TrustManagerConfig;
import ru.algeps.sparrow.util.security.exception.KeyStoreUtilException;

class KeyStoreUtilTest extends SecurityContextLoad {
  final String keyStorePassword = "password";
  String keyStorePath;

  @BeforeEach
  void initPath() {
    keyStorePath =
        "temp_keystore_for_test-%s.%s"
            .formatted(System.currentTimeMillis(), KeyStoreType.PKCS12.getType());
  }

  @Test
  void test_getKeyStoreFromFile() throws Exception {
    KeyStoreType keyStoreType = KeyStoreType.PKCS12;
    KeyStoreConfig keyStoreConfig =
        new KeyStoreConfig(keyStorePath, keyStorePassword, keyStoreType);
    String alias = "alias";

    Key secretKey =
        create256AesKeyInKeyStoreFile(keyStoreType, keyStorePassword, keyStorePath, alias);

    KeyStore loadedKeyStoreFromFile = KeyStoreUtil.getKeyStoreFromFile(keyStoreConfig);
    Key actualSecretKey = loadedKeyStoreFromFile.getKey(alias, keyStorePassword.toCharArray());

    assertEquals(secretKey, actualSecretKey);
    assertTrue(
        Files.deleteIfExists(Path.of(keyStorePath)), "Невозможно удалить файл:" + keyStorePath);
  }

  static Key create256AesKeyInKeyStoreFile(
      KeyStoreType keyStoreType, String password, String path, String alias) throws Exception {

    KeyStore newKeyStore = KeyStore.getInstance(keyStoreType.getType());
    newKeyStore.load(null, password.toCharArray());
    KeyPair keyPair = CryptoTestUtil.generateKeyPair("RSA", null, 1024);
    X509Certificate x509Certificate =
        CryptoTestUtil.generateSelfSignedCertificate(keyPair, "SHA256WithRSAEncryption", "BC");
    Certificate[] certificateChain = new Certificate[] {x509Certificate};
    newKeyStore.setKeyEntry(alias, keyPair.getPrivate(), password.toCharArray(), certificateChain);

    try (FileOutputStream fos = new FileOutputStream(path)) {
      newKeyStore.store(fos, password.toCharArray());
    }

    return keyPair.getPrivate();
  }

  @Test
  void test_getKeyManagerFactory() throws Exception {
    KeyStoreType keyStoreType = KeyStoreType.PKCS12;
    String keyManagerPassword = "keyManagerPassword";
    KeyManagerAlgorithm algorithm = KeyManagerAlgorithm.PKIX;
    KeyManagerConfig config =
        new KeyManagerConfig(
            keyStorePath, keyStorePassword, keyStoreType, keyManagerPassword, algorithm);

    String alias = "alias";
    create256AesKeyInKeyStoreFile(keyStoreType, keyStorePassword, keyStorePath, alias);

    KeyManagerFactory keyManagerFactory = KeyStoreUtil.getKeyManagerFactory(config);
    KeyManager[] keyManagers = keyManagerFactory.getKeyManagers();

    assertTrue(keyManagers.length > 0, "В KeyStore должен быть как минимум 1 KeyManager!");
    assertTrue(
        Files.deleteIfExists(Path.of(keyStorePath)), "Невозможно удалить файл:" + keyStorePath);
  }

  @Test
  void test_getTrustManagerFactory() throws Exception {
    KeyStoreType keyStoreType = KeyStoreType.PKCS12;
    TrustManagerAlgorithm trustManagerAlgorithm = TrustManagerAlgorithm.PKIX;
    TrustManagerConfig config =
        new TrustManagerConfig(keyStorePath, keyStorePassword, keyStoreType, trustManagerAlgorithm);

    String alias = "alias";
    create256AesKeyInKeyStoreFile(keyStoreType, keyStorePassword, keyStorePath, alias);

    TrustManagerFactory trustManagerFactory = KeyStoreUtil.getTrustManagerFactory(config);
    TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();

    assertTrue(trustManagers.length > 0, "В KeyStore должен быть как минимум 1 TrustManager!");
    assertTrue(
        Files.deleteIfExists(Path.of(keyStorePath)), "Невозможно удалить файл!" + keyStorePath);
  }

  @Test
  void test_getTrustManagerFactory_emptyKeyStoreProvider() throws Exception {
    KeyStoreType keyStoreType = KeyStoreType.PKCS12;
    TrustManagerAlgorithm trustManagerAlgorithm = TrustManagerAlgorithm.PKIX;
    TrustManagerConfig config =
        new TrustManagerConfig(
            keyStorePath,
            keyStorePassword,
            keyStoreType,
            "non_exist",
            trustManagerAlgorithm,
            "non_exist");

    String alias = "alias";
    create256AesKeyInKeyStoreFile(keyStoreType, keyStorePassword, keyStorePath, alias);

    assertThrows(KeyStoreUtilException.class, () -> KeyStoreUtil.getTrustManagerFactory(config));

    assertTrue(
        Files.deleteIfExists(Path.of(keyStorePath)), "Невозможно удалить файл!" + keyStorePath);
  }

  @Test
  void test_getTrustManagerFactory_emptyTrustManagerProvider() throws Exception {
    KeyStoreType keyStoreType = KeyStoreType.PKCS12;
    TrustManagerAlgorithm trustManagerAlgorithm = TrustManagerAlgorithm.PKIX;
    TrustManagerConfig config =
        new TrustManagerConfig(
            keyStorePath, keyStorePassword, keyStoreType, trustManagerAlgorithm, "non_exist");

    String alias = "alias";
    create256AesKeyInKeyStoreFile(keyStoreType, keyStorePassword, keyStorePath, alias);

    assertThrows(KeyStoreUtilException.class, () -> KeyStoreUtil.getTrustManagerFactory(config));

    assertTrue(
        Files.deleteIfExists(Path.of(keyStorePath)), "Невозможно удалить файл!" + keyStorePath);
  }

  ///
  @Test
  void test_getKeyStoreFromFile_nullConfig() {
    assertThrows(IllegalArgumentException.class, () -> KeyStoreUtil.getKeyStoreFromFile(null));
  }

  @Test
  void test_getKeyStoreFromFile_nullKeyStoreType() {
    KeyStoreConfig config = new KeyStoreConfig(keyStorePath, keyStorePassword, null);
    assertThrows(IllegalArgumentException.class, () -> KeyStoreUtil.getKeyStoreFromFile(config));
  }

  @Test
  void test_getKeyStoreFromFile_nullPath() {
    KeyStoreConfig config = new KeyStoreConfig(null, keyStorePassword, KeyStoreType.JKS);
    assertDoesNotThrow(() -> KeyStoreUtil.getKeyStoreFromFile(config));
  }

  @Test
  void test_getKeyStoreFromFile_validConfig_noPasswordNoPath() throws Exception {
    KeyStoreConfig config = new KeyStoreConfig(null, null, KeyStoreType.JKS);
    KeyStore keyStore = KeyStoreUtil.getKeyStoreFromFile(config);
    assertNotNull(keyStore);
  }

  @Test
  void test_getKeyStoreFromFile_validConfig_withPassword() throws Exception {
    KeyStoreType keyStoreType = KeyStoreType.PKCS12;
    KeyStoreConfig config = new KeyStoreConfig(keyStorePath, keyStorePassword, keyStoreType);
    String alias = "alias";

    create256AesKeyInKeyStoreFile(keyStoreType, keyStorePassword, keyStorePath, alias);

    KeyStore loadedKeyStoreFromFile = KeyStoreUtil.getKeyStoreFromFile(config);
    assertTrue(
        Files.deleteIfExists(Path.of(keyStorePath)), "Невозможно удалить файл:" + keyStorePath);
  }

  @Test
  void test_getKeyStoreFromFile_validConfig_withPath() throws Exception {
    KeyStoreType keyStoreType = KeyStoreType.PKCS12;
    KeyStoreConfig config = new KeyStoreConfig(keyStorePath, null, keyStoreType);
    String alias = "alias";

    create256AesKeyInKeyStoreFile(keyStoreType, keyStorePassword, keyStorePath, alias);

    KeyStore loadedKeyStoreFromFile = KeyStoreUtil.getKeyStoreFromFile(config);
    assertTrue(
        Files.deleteIfExists(Path.of(keyStorePath)), "Невозможно удалить файл:" + keyStorePath);
  }

  @Test
  void test_getKeyStoreFromFile_ioException() {
    KeyStoreType keyStoreType = KeyStoreType.PKCS12;
    String invalidPath = "invalid/path/keystore.p12";
    KeyStoreConfig config = new KeyStoreConfig(invalidPath, keyStorePassword, keyStoreType);

    assertThrows(KeyStoreUtilException.class, () -> KeyStoreUtil.getKeyStoreFromFile(config));
  }

  @Test
  void test_getKeyStoreFromFile_keyStoreException() {
    // Создаем конфиг с несуществующим типом хранилища
    KeyStoreConfig config = new KeyStoreConfig(keyStorePath, keyStorePassword, null);

    assertThrows(IllegalArgumentException.class, () -> KeyStoreUtil.getKeyStoreFromFile(config));
  }

  @Test
  void test_getKeyStoreFromFile_noSuchProviderException() {
    // Создаем конфиг с несуществующим провайдером
    KeyStoreConfig config =
        new KeyStoreConfig(
            keyStorePath, keyStorePassword, KeyStoreType.PKCS12, "NON_EXISTENT_PROVIDER");
    assertThrows(KeyStoreUtilException.class, () -> KeyStoreUtil.getKeyStoreFromFile(config));
  }

  // getKeyManagerFactory tests
  @Test
  void test_getKeyManagerFactory_nullConfig() {
    assertThrows(IllegalArgumentException.class, () -> KeyStoreUtil.getKeyManagerFactory(null));
  }

  @Test
  void test_getKeyManagerFactory_nullAlgorithm() {
    KeyManagerConfig config =
        new KeyManagerConfig(
            keyStorePath, keyStorePassword, KeyStoreType.PKCS12, keyStorePassword, null);
    assertThrows(IllegalArgumentException.class, () -> KeyStoreUtil.getKeyManagerFactory(config));
  }

  @Test
  void test_getKeyManagerFactory_nullKeyManagerPassword() {
    KeyManagerConfig config =
        new KeyManagerConfig(
            keyStorePath, keyStorePassword, KeyStoreType.PKCS12, null, KeyManagerAlgorithm.PKIX);
    assertThrows(IllegalArgumentException.class, () -> KeyStoreUtil.getKeyManagerFactory(config));
  }

  @Test
  void test_getKeyManagerFactory_validConfig() throws Exception {
    KeyStoreType keyStoreType = KeyStoreType.PKCS12;
    String keyManagerPassword = "keyManagerPassword";
    KeyManagerAlgorithm algorithm = KeyManagerAlgorithm.PKIX;
    KeyManagerConfig config =
        new KeyManagerConfig(
            keyStorePath, keyStorePassword, keyStoreType, keyManagerPassword, algorithm);

    String alias = "alias";
    create256AesKeyInKeyStoreFile(keyStoreType, keyStorePassword, keyStorePath, alias);

    KeyManagerFactory keyManagerFactory = KeyStoreUtil.getKeyManagerFactory(config);
    assertNotNull(keyManagerFactory);
    assertTrue(
        Files.deleteIfExists(Path.of(keyStorePath)), "Невозможно удалить файл:" + keyStorePath);
  }

  @Test
  void test_getKeyManagerFactory_invalidProvider() throws Exception {
    KeyStoreType keyStoreType = KeyStoreType.PKCS12;
    String keyManagerPassword = "keyManagerPassword";
    KeyManagerAlgorithm algorithm = KeyManagerAlgorithm.PKIX;
    KeyManagerConfig config =
        new KeyManagerConfig(
            keyStorePath,
            keyStorePassword,
            keyStoreType,
            keyManagerPassword,
            algorithm,
            "non_exist");

    String alias = "alias";
    create256AesKeyInKeyStoreFile(keyStoreType, keyStorePassword, keyStorePath, alias);

    assertThrows(KeyStoreUtilException.class, () -> KeyStoreUtil.getKeyManagerFactory(config));
    assertTrue(
        Files.deleteIfExists(Path.of(keyStorePath)), "Невозможно удалить файл:" + keyStorePath);
  }

  @Test
  void test_getKeyManagerFactory_noSuchAlgorithmException() {
    KeyManagerConfig config =
        new KeyManagerConfig(
            keyStorePath, keyStorePassword, KeyStoreType.PKCS12, keyStorePassword, null);
    assertThrows(IllegalArgumentException.class, () -> KeyStoreUtil.getKeyManagerFactory(config));
  }

  @Test
  void test_getKeyManagerFactory_noSuchProviderException() {
    KeyStoreConfig keyStoreConfig =
        new KeyStoreConfig(keyStorePath, keyStorePassword, KeyStoreType.PKCS12);
    KeyManagerConfig config =
        new KeyManagerConfig(keyStoreConfig, "", KeyManagerAlgorithm.PKIX, "NON_EXISTENT_PROVIDER");
    assertThrows(KeyStoreUtilException.class, () -> KeyStoreUtil.getKeyManagerFactory(config));
  }

  @Test
  void test_getSecureRandom_nullAlgorithm() throws KeyStoreUtilException {
    assertNull(KeyStoreUtil.getSecureRandom(null));
  }

  @Test
  void test_getSecureRandom_validAlgorithm() throws KeyStoreUtilException {
    SecureRandomAlgorithm algorithm = SecureRandomAlgorithm.SHA1PRNG;
    SecureRandom secureRandom = KeyStoreUtil.getSecureRandom(algorithm);
    assertNotNull(secureRandom);
    assertEquals("SHA1PRNG", secureRandom.getAlgorithm());
  }

  @Test
  void test_getSecureRandom_nonValidAlgorithm() {
    SecureRandomAlgorithm algorithm = SecureRandomAlgorithm.NATIVE_PRNG;
    assertThrows(KeyStoreUtilException.class, () -> KeyStoreUtil.getSecureRandom(algorithm));
  }
}
