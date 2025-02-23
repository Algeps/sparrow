package ru.algeps.sparrow.worker.server.stcp.exception;

import ru.algeps.sparrow.worker.server.exception.ServerException;

public class STcpServerException extends ServerException {
  public STcpServerException(String message) {
    super(message);
  }

  public STcpServerException(Throwable cause) {
    super(cause);
  }

  public STcpServerException(String message, Throwable cause) {
    super(message, cause);
  }
}
