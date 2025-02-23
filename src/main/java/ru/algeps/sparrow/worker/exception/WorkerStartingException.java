package ru.algeps.sparrow.worker.exception;

public class WorkerStartingException extends Exception {

  public WorkerStartingException(String message) {
    super(message);
  }

  public WorkerStartingException(String message, Throwable cause) {
    super(message, cause);
  }
}
