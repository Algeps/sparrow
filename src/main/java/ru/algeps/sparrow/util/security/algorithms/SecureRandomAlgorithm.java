package ru.algeps.sparrow.util.security.algorithms;

/**
 * <a
 * href="https://docs.oracle.com/en/java/javase/17/docs/specs/security/standard-names.html#securerandom-number-generation-algorithms">SecureRandom
 * algorithm</a>
 */
public enum SecureRandomAlgorithm {
  NATIVE_PRNG("NativePRNG"),
  NATIVE_PRNG_BLOCKING("NativePRNGBlocking"),
  NATIVE_PRNG_NON_BLOCKING("NativePRNGNonBlocking"),
  PKCS11("PKCS11"),
  DRBG("DRBG"),
  SHA1PRNG("SHA1PRNG");

  final String algorithmName;

  SecureRandomAlgorithm(String algorithmName) {
    this.algorithmName = algorithmName;
  }

  public String getAlgorithmName() {
    return algorithmName;
  }
}
