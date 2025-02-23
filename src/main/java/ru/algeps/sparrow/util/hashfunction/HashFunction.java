package ru.algeps.sparrow.util.hashfunction;

public interface HashFunction {

  enum HashAlgorithm {
    NO,
    SHA_256,
    STRIBOG_512;
  }

  static HashFunction getHashFunction(HashAlgorithm hashAlgorithm) {
    return switch (hashAlgorithm) {
      case NO -> new NoHashFunction();
      case SHA_256, STRIBOG_512 ->
          throw new IllegalArgumentException("Not supported hash function!");
      case null -> null;
    };
  }

  /** Хэширование */
  byte[] hash(byte[] val);

  /**
   * Сопоставление
   *
   * @param raw сырой пароль
   * @param hashing хэшированный пароль
   */
  boolean match(byte[] raw, byte[] hashing);
}
