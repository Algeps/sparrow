package ru.algeps.sparrow.util.security.algorithms;

/**
 * <a
 * href="https://docs.oracle.com/en/java/javase/17/docs/specs/security/standard-names.html#trustmanagerfactory-algorithms">TrustManagerAlgorithm</a>
 */
public enum TrustManagerAlgorithm {
  PKIX("PKIX"),
  // crypto pro
  GOST_X509("GostX509");

  final String algorithmName;

  TrustManagerAlgorithm(String algorithmName) {
    this.algorithmName = algorithmName;
  }

  public String getAlgorithmName() {
    return algorithmName;
  }
}
