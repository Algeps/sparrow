package ru.algeps.sparrow.worker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.algeps.sparrow.worker.exception.WorkerStartingException;
import ru.algeps.sparrow.worker.server.Server;

import java.io.Closeable;
import java.time.Duration;

public class Worker implements Closeable {
  /** Максимальное время за которое стартует Worker. */
  public final Duration MAX_STARTING_DURATION = Duration.ofSeconds(5);

  //
  protected final Logger log;
  protected final Server server;

  public Worker(String name, Server server) {
    this.log = LoggerFactory.getLogger(name);
    this.server = server;
  }

  /** Запускает обработчик на выполнение. */
  public void start() throws WorkerStartingException {
    server.start();
    long startTime = System.currentTimeMillis();
    long endTime = startTime + MAX_STARTING_DURATION.toMillis();

    while (!isRun()) {
      if (getError() != null) {
        throw new WorkerStartingException(
            "Worker '%s' start failed".formatted(log.getName()), getError());
      }

      if (System.currentTimeMillis() > endTime) {
        throw new WorkerStartingException(
            "Worker '%s' start timed out after".formatted(MAX_STARTING_DURATION));
      }

      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new WorkerStartingException("Server start interrupted", e);
      }
    }
  }

  public boolean isRun() {
    return server.getServerState().isRunning();
  }

  public Exception getError() {
    return server.getServerState().getException();
  }

  /** Останавливает обработчик. */
  public void stop() {
    close();
  }

  @Override
  public void close() {
    server.stop();
  }
}
