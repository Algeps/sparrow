package ru.algeps.sparrow.worker.server.exception;

public class ServerException extends RuntimeException {
  public ServerException(String message) {
    super(message);
  }

  public ServerException(String message, Throwable cause) {
    super(message, cause);
  }

  public ServerException(Throwable cause) {
    super(cause);
  }

  public ServerException(
      String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

  public ServerException() {}
}
