package ru.algeps.sparrow.worker.server.tcp.exception;

import ru.algeps.sparrow.worker.server.exception.ServerException;

public class TcpServerException extends ServerException {
  public TcpServerException(Throwable cause) {
    super(cause);
  }

  public TcpServerException(String message, Throwable cause) {
    super(message, cause);
  }
}
