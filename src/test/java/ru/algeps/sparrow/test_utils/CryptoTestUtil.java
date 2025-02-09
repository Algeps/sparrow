package ru.algeps.sparrow.test_utils;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

public final class CryptoTestUtil {
  private CryptoTestUtil() {}

  /**
   * Возвращает dnsName OID определённом в <a
   * href="https://www.rfc-editor.org/rfc/rfc2459#section-4.2.1.7">alt names</a>
   */
  public static int[] getSubjectAltNameOID() {
    return new int[] {2, 5, 29, 17};
  }

  public static byte[] getDnsNamesObjectBytes(String... dnsNames) {
    GeneralName[] generalNames =
        Arrays.stream(dnsNames)
            .map(dnsName -> new GeneralName(GeneralName.dNSName, dnsName))
            .toArray(GeneralName[]::new);
    GeneralNames names = new GeneralNames(generalNames);
    try {
      return new DEROctetString(names).getOctets();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static KeyPair generateKeyPair(String algorithmName) throws NoSuchAlgorithmException {
    KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(algorithmName);
    return keyPairGenerator.genKeyPair();
  }

  /***
   * Генерирует пару ключей ассиметричного алгоритма.
   *
   * @param provider провайдер, может быть null
   */
  public static KeyPair generateKeyPair(String algorithmName, String provider, int keySize)
      throws NoSuchAlgorithmException, NoSuchProviderException {
    KeyPairGenerator keyPairGenerator =
        provider == null
            ? KeyPairGenerator.getInstance(algorithmName)
            : KeyPairGenerator.getInstance(algorithmName, provider);
    keyPairGenerator.initialize(keySize);
    return keyPairGenerator.genKeyPair();
  }

  public static KeyPair generateKeyPairWithParams(
      String algorithmName, String provider, AlgorithmParameterSpec algorithmParameterSpec)
      throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
    KeyPairGenerator keyPairGenerator =
        provider == null
            ? KeyPairGenerator.getInstance(algorithmName)
            : KeyPairGenerator.getInstance(algorithmName, provider);
    keyPairGenerator.initialize(algorithmParameterSpec);
    return keyPairGenerator.genKeyPair();
  }

  /** Создаёт сертификат с "замоканными" данными. */
  public static X509Certificate generateSelfSignedCertificate(
      KeyPair keyPair, String signatureAlgorithm, String signatureAlgorithmProvider)
      throws CertificateException, OperatorCreationException, IOException {
    PublicKey publicKey = keyPair.getPublic();
    PrivateKey privateKey = keyPair.getPrivate();

    // Данные для сертификата
    String issueName = "CN=Sparrow,OU=Algeps,O=AlgepsConcord,L=Omsk,ST=Omsk,C=RU";

    // Дата начала и окончания действия сертификата
    Calendar calendar = Calendar.getInstance();
    Date startDate = calendar.getTime(); // Сейчас
    calendar.add(Calendar.YEAR, 1); // Один год вперед
    Date endDate = calendar.getTime();

    // Создание структуры сертификата
    X500Name issuer = new X500Name(issueName);
    BigInteger serial = BigInteger.valueOf(System.currentTimeMillis());
    X500Name subject = new X500Name(issueName);
    X509v3CertificateBuilder certificateBuilder =
        new JcaX509v3CertificateBuilder(issuer, serial, startDate, endDate, subject, publicKey);

    // добавление доменного имени
    GeneralName dnsName = new GeneralName(GeneralName.dNSName, "localhost");
    GeneralNames names = new GeneralNames(dnsName);
    DEROctetString derOctetStringGeneralNames = new DEROctetString(names);
    Extension extension =
        new Extension(Extension.subjectAlternativeName, true, derOctetStringGeneralNames);
    certificateBuilder.addExtension(extension);

    ContentSigner contentSigner =
        new JcaContentSignerBuilder(signatureAlgorithm)
            .setProvider(signatureAlgorithmProvider)
            .build(privateKey);

    return new JcaX509CertificateConverter()
        .setProvider(signatureAlgorithmProvider)
        .getCertificate(certificateBuilder.build(contentSigner));
  }
}
