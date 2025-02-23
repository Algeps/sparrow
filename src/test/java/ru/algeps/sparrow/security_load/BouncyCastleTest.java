package ru.algeps.sparrow.security_load;

import org.bouncycastle.jcajce.spec.GOST3410ParameterSpec;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.algeps.sparrow.test_utils.CryptoTestUtil;
import ru.algeps.sparrow.util.security.SecurityContextLoad;
import ru.algeps.sparrow.util.security.algorithms.KeyStoreType;

import java.io.FileOutputStream;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BouncyCastleTest {
  static final String keyStorePathString = "temp_sparrow_keystore.p12";

  @BeforeAll
  static void setUp() {
    new SecurityContextLoad() {};
    System.setProperty("javax.net.debug", "all");
    Logger.getLogger("").setLevel(Level.ALL);
  }

  @Test
  void bouncyCastle_createGostCert_test() {
    try {
      KeyPair keyPair =
          CryptoTestUtil.generateKeyPairWithParams(
              "ECGOST3410-2012",
              "BC",
              new GOST3410ParameterSpec("Tc26-Gost-3410-12-512-paramSetC"));
      X509Certificate gostCertificate =
          CryptoTestUtil.generateSelfSignedCertificate(
              keyPair, "GOST3411-2012-512withGOST3410-2012-512", "BC");

      final KeyStore ks = KeyStore.getInstance(KeyStoreType.PKCS12.getType(), "BC");
      ks.load(null);
      ks.setKeyEntry("sparrow", keyPair.getPrivate(), null, new Certificate[] {gostCertificate});
      ks.store(new FileOutputStream(keyStorePathString), "".toCharArray());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
