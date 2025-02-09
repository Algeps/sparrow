package ru.algeps.sparrow.message.request.domain.exceptions;

import java.io.IOException;

public abstract class RequestException extends IOException {
  protected RequestException() {}

  protected RequestException(String message) {
    super(message);
  }

  protected RequestException(String message, Throwable cause) {
    super(message, cause);
  }

  protected RequestException(Throwable cause) {
    super(cause);
  }
}
