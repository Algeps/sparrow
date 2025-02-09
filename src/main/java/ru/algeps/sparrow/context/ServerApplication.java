package ru.algeps.sparrow.context;

import static ru.algeps.sparrow.context.Constants.LOGO;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.algeps.sparrow.config.domain.Config;
import ru.algeps.sparrow.config.StartServerConfig;
import ru.algeps.sparrow.config.exception.StartServerConfigException;
import ru.algeps.sparrow.worker.Worker;
import ru.algeps.sparrow.config.domain.WorkerConfig;
import ru.algeps.sparrow.worker.WorkerFactory;
import ru.algeps.sparrow.worker.exception.WorkerStartingException;

public final class ServerApplication {

  private static final Logger log = LoggerFactory.getLogger(ServerApplication.class);
  private static final Map<String, Worker> workerContext = new ConcurrentHashMap<>();
  private static Config config;

  private ServerApplication() {}

  /** Запускает сервер и загружает конфигурацию. */
  public static void run(String... args) {
    printLogo();

    try {
      loadConfig(args);
    } catch (Exception e) {
      log.error("Exception in load config", e);
      exitWithException();
    }

    try {
      createWorkers();
    } catch (Exception e) {
      log.error("Exception in create workers", e);
      exitWithException();
    }

    try {
      startingWorkers();
    } catch (Exception e) {
      log.error("Exception in create workers", e);
      exitWithException();
    }

    initShutdownCallback();
  }

  private static void printLogo() {
    System.out.println(LOGO);
  }

  /** Возвращает конфигурацию приложения. */
  private static void loadConfig(String... args) throws StartServerConfigException {
    log.info("Load configuration...");
    config = StartServerConfig.parseAndLoadConfig(args);
    log.info("Configuration load successful");
  }

  private static void createWorkers() throws WorkerFactory.WorkerCreateException {
    log.info("Creating workers...");
    for (Map.Entry<String, WorkerConfig> handlerConfig : config.entries()) {
      Worker worker = WorkerFactory.create(handlerConfig.getValue());
      workerContext.put(handlerConfig.getKey(), worker);
    }
    log.info("Creating workers complete");
  }

  private static void startingWorkers() throws WorkerStartingException {
    log.info("Starting workers...");
    for (Map.Entry<String, Worker> entryWorker : workerContext.entrySet()) {
      String workerName = entryWorker.getKey();
      Worker worker = entryWorker.getValue();

      log.info("  Starting worker '{}' ", workerName);
      worker.start();
      log.info("  Start worker '{}' successful", workerName);
    }
  }

  private static void stopWorkers() {
    log.info("Stopping workers...");
    for (Worker worker : workerContext.values()) {
      worker.stop();
    }
  }

  /**
   * Устанавливает ShutdownHook для корректного завершения приложения.
   *
   * <p><a
   * href="https://docs.oracle.com/javase/1.5.0/docs/api/java/lang/Runtime.html#addShutdownHook(java.lang.Thread)">ShutdownHook</a>
   */
  private static void initShutdownCallback() {
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  Thread.currentThread().setName("exit thread");
                  log.info("Close context and exit...");
                  stopWorkers();
                  log.info("Close context successful");
                  log.info("Exit");
                }));
  }

  private static void exitWithException() {
    System.exit(-1);
  }
}
