package ru.algeps.sparrow.util.security.algorithms;


/**
 * <a
 * href="https://docs.oracle.com/en/java/javase/17/docs/specs/security/standard-names.html#keystore-types">keystore-types</a>
 */
public enum KeyStoreType {
  JKS("jks"),
  PKCS11("pkcs11"),
  PKCS12("pkcs12"),
  // crypto pro
  HD_IMAGE("HDImageStore");

  final String type;

  KeyStoreType(String type) {
    this.type = type;
  }

  public String getType() {
    return type;
  }
}
