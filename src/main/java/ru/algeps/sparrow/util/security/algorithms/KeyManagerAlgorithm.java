package ru.algeps.sparrow.util.security.algorithms;

public enum KeyManagerAlgorithm {
  PKIX("PKIX"),
  // crypto pro
  GOST_X509("GostX509");

  final String algorithmName;

  KeyManagerAlgorithm(String algorithmName) {
    this.algorithmName = algorithmName;
  }

  public String getAlgorithmName() {
    return algorithmName;
  }
}
