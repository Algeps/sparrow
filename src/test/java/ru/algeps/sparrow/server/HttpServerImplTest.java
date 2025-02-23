package ru.algeps.sparrow.server;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.net.Socket;
import java.nio.CharBuffer;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.IntStream;
import org.junit.jupiter.api.*;
import org.slf4j.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HttpServerImplTest {
  private final Logger log = LoggerFactory.getLogger(HttpServerImplTest.class);

  // HttpServer httpServer;
  int port = 9090;
  String host = "";
  String message =
      """
  GET / HTTP/1.1
  host: localhost
  id:\s""";

  /*final int maxBacklog = 100;

  @BeforeAll
  void init() {
    ServerConfig serverConfig = new ServerConfig("");
    InetSocketAddress inetSocketAddress = new InetSocketAddress(port);
    StartServerConfig startServerConfig =
        new StartServerConfig(false, inetSocketAddress, maxBacklog, serverConfig);
    httpServer = HttpServerFactory.createHttp(startServerConfig);
    httpServer.start();
  }

  @AfterAll
  void close() {
    httpServer.stop();
  }*/


  void start() {
    int numberOfClients = 200;
    boolean isTermination;
    List<Future<Long>> futureList;
    long maxTimeout = 5;

    try (ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor()) {
      futureList =
          IntStream.rangeClosed(1, numberOfClients)
              .mapToObj(i -> executorService.submit(() -> connectServerAndRequest(i)))
              .toList();
      executorService.shutdown();
      isTermination = executorService.awaitTermination(maxTimeout, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }

    assertTrue(isTermination);
    long averageResponseTimeInMilliseconds =
        futureList.stream().map(this::getResult).reduce(0L, Long::sum) / numberOfClients;
    double averageResponseTimeInSeconds = averageResponseTimeInMilliseconds / 1000.0;
    log.info(
        "Average running={} milliseconds or {} seconds",
        averageResponseTimeInMilliseconds,
        averageResponseTimeInSeconds);
  }

  /** Возвращает продолжительность обработки запроса. */
  private long connectServerAndRequest(int i) {
    String currentName = "request=" + i;
    Thread.currentThread().setName(currentName);
    long start = System.currentTimeMillis();
    CharBuffer charBuffer = CharBuffer.allocate(1500);

    try (Socket socket = new Socket(host, port);
        PrintWriter writer = new PrintWriter(socket.getOutputStream());
        BufferedReader reader =
            new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
      writer.print(message + currentName);
      writer.flush();

      if (reader.read(charBuffer) == -1) {
        throw new RuntimeException("not response");
      }
      String resposneString = charBuffer.rewind().toString().trim();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    long duration = System.currentTimeMillis() - start;
    log.info("execute in thread={}, duration={}", Thread.currentThread().getName(), duration);
    return duration;
  }

  private long getResult(Future<Long> future) {
    try {
      return future.get();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
  }
}
