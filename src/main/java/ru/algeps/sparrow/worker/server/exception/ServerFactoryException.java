package ru.algeps.sparrow.worker.server.exception;

public class ServerFactoryException extends RuntimeException {
  public ServerFactoryException(String message) {
    super(message);
  }

  public ServerFactoryException(Throwable cause) {
    super(cause);
  }
}
