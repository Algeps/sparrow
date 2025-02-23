package ru.algeps.sparrow.util.security.algorithms;

/**
 * Поддерживаемые алгоритмы: <a
 * href="https://docs.oracle.com/en/java/javase/17/docs/specs/security/standard-names.html#sslcontext-algorithms">SSLContext
 * Algorithms</a>
 */
public enum SslAlgorithm {
  TLS_1_1("TLSv1.1"),
  TLS_1_2("TLSv1.2"),
  TLS_1_3("TLSv1.3"),
  // crypto_pro
  GOST_TLS_1_2("GostTLSv1.2");

  final String algorithmName;

  SslAlgorithm(String algorithmName) {
    this.algorithmName = algorithmName;
  }

  public String getAlgorithmName() {
    return algorithmName;
  }
}
