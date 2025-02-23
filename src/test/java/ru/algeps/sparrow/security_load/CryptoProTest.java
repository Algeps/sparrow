/*
package ru.algeps.sparrow.security_load;

import java.io.ByteArrayInputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import ru.CryptoPro.Crypto.CryptoProvider;
import ru.CryptoPro.JCP.ASN.PKIX1Explicit88.Extension;
import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.Util.JCPInit;
import ru.CryptoPro.JCPRequest.GostCertificateRequest;
import ru.algeps.sparrow.test_utils.CryptoTestUtil;
import ru.algeps.sparrow.util.security.SecurityContextLoad;

public class CryptoProTest {

  @BeforeAll
  static void setUp() {
    new SecurityContextLoad() {};
    JCPInit.initProviders(true);
  }

  @Test
  @Disabled
  void createAndStoreGostCertIn_HDImageStore() throws Exception {
    String keyAlg = JCP.GOST_DH_2012_512_NAME;
    KeyPairGenerator keyGen = KeyPairGenerator.getInstance(keyAlg, CryptoProvider.PROVIDER_NAME);
    KeyPair keyPair = keyGen.generateKeyPair();
    PrivateKey privKey = keyPair.getPrivate();
    PublicKey pubKey = keyPair.getPublic();

    String subjectName = "CN=Sparrow,OU=Algeps,O=AlgepsConcord,L=Omsk,ST=Omsk,C=RU";
    Extension extension =
        new Extension(
            CryptoTestUtil.getSubjectAltNameOID(),
            CryptoTestUtil.getDnsNamesObjectBytes("localhost"));
    GostCertificateRequest request = new GostCertificateRequest();
    request.setKeyUsage(GostCertificateRequest.CRYPT_DEFAULT);
    request.addExtKeyUsage(GostCertificateRequest.INTS_PKIX_SERVER_AUTH);
    request.setPublicKeyInfo(pubKey);
    request.setSubjectInfo(subjectName);
    request.addExtension(extension);
    request.encodeAndSign(privKey, JCP.GOST_SIGN_DH_2012_512_NAME);

    byte[] encoded =
        request.getEncodedSelfCert(keyPair, subjectName, JCP.GOST_SIGN_DH_2012_512_NAME);
    CertificateFactory cf = CertificateFactory.getInstance("X509");
    java.security.cert.Certificate gostCertificate =
        cf.generateCertificate(new ByteArrayInputStream(encoded));

    String password = "password";
    String alias = "sparrow";

    java.security.cert.Certificate[] certs = new java.security.cert.Certificate[] {gostCertificate};

    KeyStore hdImageStore = KeyStore.getInstance(JCP.HD_STORE_NAME);
    hdImageStore.load(null, null);
    hdImageStore.setKeyEntry(alias, privKey, password.toCharArray(), certs);
    hdImageStore.store(null, null);
  }
}
*/
