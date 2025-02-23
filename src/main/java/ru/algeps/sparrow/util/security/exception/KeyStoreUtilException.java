package ru.algeps.sparrow.util.security.exception;

import java.io.IOException;

public class KeyStoreUtilException extends IOException {
  public KeyStoreUtilException(String message) {
    super(message);
  }

  public KeyStoreUtilException(Throwable cause) {
    super(cause);
  }
}
