package ru.algeps.sparrow.test_utils;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import ru.algeps.sparrow.util.security.algorithms.KeyManagerAlgorithm;
import ru.algeps.sparrow.util.security.algorithms.KeyStoreType;
import ru.algeps.sparrow.util.security.algorithms.SecureRandomAlgorithm;
import ru.algeps.sparrow.util.security.algorithms.SslAlgorithm;
import ru.algeps.sparrow.util.security.algorithms.TrustManagerAlgorithm;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class SSLContextLoad extends TestSecurityLoad {
  public SSLContext sslContext;
  public static final String STRICT_HOST = "localhost";
  public static final SslAlgorithm SSL_ALGORITHM = SslAlgorithm.TLS_1_3;
  // keystore
  static final String KEYSTORE_TYPE = KeyStoreType.PKCS12.getType();
  static final String KEY_STORE_PATH =
      "keystore_for_test_%s.p12".formatted(System.currentTimeMillis());
  static final char[] KEY_STORE_PASSWORD = null;
  // key manager
  static final KeyManagerAlgorithm KEY_MANAGER_ALGORITHM = KeyManagerAlgorithm.PKIX;
  static final String KEYSTORE_KEY_MANAGER_PASSWORD = "password";
  public static KeyManager[] keyManagers;
  // trust manager
  static final TrustManagerAlgorithm TRUST_MANAGER_ALGORITHM = TrustManagerAlgorithm.PKIX;
  public static TrustManager[] trustManagers;
  // secure random
  public static final SecureRandomAlgorithm SECURE_RANDOM_ALGORITHM =
      SecureRandomAlgorithm.SHA1PRNG;
  public static SecureRandom secureRandom;
  // certificate
  static final String CERTIFICATE_ALIAS = "1";

  /** Создание и сохранение сертификата в keystore. Создание защищённого контекста (SSLContext). */
  @BeforeAll
  public void beforeAll() throws Exception {
    createCertificate_and_storeInKeyStore();
    sslContextLoad();
  }

  void createCertificate_and_storeInKeyStore() throws Exception {
    KeyStore newKeyStore = KeyStore.getInstance(KEYSTORE_TYPE);
    newKeyStore.load(null, null);
    KeyPair keyPair = CryptoTestUtil.generateKeyPair("RSA", null, 1024);
    X509Certificate x509Certificate =
        CryptoTestUtil.generateSelfSignedCertificate(keyPair, "SHA256WithRSAEncryption", "BC");
    Certificate[] certificateChain = new Certificate[] {x509Certificate};
    newKeyStore.setKeyEntry(
        CERTIFICATE_ALIAS,
        keyPair.getPrivate(),
        KEYSTORE_KEY_MANAGER_PASSWORD.toCharArray(),
        certificateChain);

    try (FileOutputStream fos = new FileOutputStream(KEY_STORE_PATH)) {
      newKeyStore.store(fos, KEY_STORE_PASSWORD);
    }
    System.out.println("Создан сертификат:" + x509Certificate);
    System.out.println("Создан keystore:" + KEY_STORE_PATH);
  }

  void sslContextLoad() throws Exception {
    KeyStore keyStore = KeyStore.getInstance(KEYSTORE_TYPE);
    InputStream inputStream = new ByteArrayInputStream(Files.readAllBytes(Path.of(KEY_STORE_PATH)));
    keyStore.load(inputStream, KEY_STORE_PASSWORD);

    KeyManagerFactory keyManagerFactory =
        KeyManagerFactory.getInstance(KEY_MANAGER_ALGORITHM.getAlgorithmName());
    keyManagerFactory.init(keyStore, KEYSTORE_KEY_MANAGER_PASSWORD.toCharArray());
    keyManagers = keyManagerFactory.getKeyManagers();
    TrustManagerFactory trustManagerFactory =
        TrustManagerFactory.getInstance(TRUST_MANAGER_ALGORITHM.getAlgorithmName());
    trustManagerFactory.init(keyStore);
    trustManagers = trustManagerFactory.getTrustManagers();
    secureRandom = SecureRandom.getInstance(SECURE_RANDOM_ALGORITHM.getAlgorithmName());

    sslContext = SSLContext.getInstance(SslAlgorithm.TLS_1_3.getAlgorithmName());
    sslContext.init(keyManagers, trustManagers, secureRandom);
  }

  /** Удаление keystore. */
  @AfterAll
  public void AfterAll() throws IOException {
    Files.delete(Path.of(KEY_STORE_PATH));
    System.out.println("Удалён keystore:" + KEY_STORE_PATH);
  }
}
