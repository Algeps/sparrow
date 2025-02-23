package ru.algeps.sparrow.worker.server.tcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardProtocolFamily;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.algeps.sparrow.worker.processor.RequestProcessor;
import ru.algeps.sparrow.worker.server.Server;
import ru.algeps.sparrow.worker.server.state.DescriptionServerState;
import ru.algeps.sparrow.worker.server.tcp.exception.TcpServerException;

public class TcpServer implements Server {
  private final Logger log;
  private final int port;
  private final int backlog;
  private Thread threadServer;
  private final RequestProcessor requestProcessor;
  private ExecutorService executorService;
  /////////////// server state ///////////////
  private final DescriptionServerState descriptionServerState;

  // todo возможно необходимо очередь чтобы ложить туда запросы (оптимизация)

  public TcpServer(TcpServerConfig config) {
    log = LoggerFactory.getLogger(config.getName());

    this.backlog = config.getBacklog();
    this.port = config.getPort();
    this.requestProcessor = config.getRequestProcessor();
    this.threadServer = Thread.ofPlatform().name(log.getName()).unstarted(this::run);
    this.descriptionServerState = new DescriptionServerState();
  }

  @Override
  public void start() {
    threadServer.start();
  }

  private void run() {
    descriptionServerState.setStartingState();
    try {
      try (ServerSocketChannel serverSocketChannel =
          ServerSocketChannel.open(StandardProtocolFamily.INET)) {
        executorService = Executors.newVirtualThreadPerTaskExecutor();

        configAndBindServerSocketChannel(serverSocketChannel);

        runSelector(serverSocketChannel, executorService);
      }
    } catch (IOException e) {
      log.error("Exception in TCPServer.", e);
      descriptionServerState.setErrorState(e);
    }
  }

  /**
   * Установка параметров сокета.
   *
   * <pre>
   *  Устанавливаются следующие настройки:
   *  - Установка сокета в неблокирующий режим.
   *  - SO_KEEPALIVE в true (говорить ОС поддерживать соединение).
   *  - SO_REUSEADDR в false (запрещает использовать несколько прослушивающих
   *  сокетов к одному адресу и к одному порту).
   *  - SO_LINGER в -1 (задержка закрытия в секундах ,если в буфере присутствуют данные,
   *  так как включен неблокирующий режим настройка должна быть отключена)
   *  - TCP_NODELAY в false (выключение алгоритма Нейгла)
   * </pre>
   */
  private void configAndBindServerSocketChannel(ServerSocketChannel serverSocketChannel)
      throws IOException {
    InetSocketAddress inetSocketAddress = new InetSocketAddress(port);
    serverSocketChannel.bind(inetSocketAddress, backlog);

    serverSocketChannel.configureBlocking(false);
    // serverSocketChannel.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
    // serverSocketChannel.setOption(StandardSocketOptions.SO_REUSEADDR, false);
    // serverSocketChannel.setOption(StandardSocketOptions.SO_LINGER, -1);
    // serverSocketChannel.setOption(StandardSocketOptions.TCP_NODELAY, false);

    log.info("Server starting in:{}", serverSocketChannel.getLocalAddress());
    String stringUsePort = serverSocketChannel.getLocalAddress().toString().split(":")[1];
    int usePort = Integer.parseInt(stringUsePort);
    descriptionServerState.addUsePort(usePort);
  }

  /** Выполняет обработку запросов, пока не прервётся поток. */
  private void runSelector(ServerSocketChannel serverSocketChannel, ExecutorService executorService)
      throws IOException {
    Selector selector = SelectorProvider.provider().openSelector();
    SelectionKey listenedKey = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    descriptionServerState.setRunningState();

    while (!threadServer.isInterrupted()) {
      selector.select();
      Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();

      while (keyIterator.hasNext()) {
        SelectionKey key = keyIterator.next();
        doHandleSelectorKey(key, listenedKey, selector, executorService);
        keyIterator.remove();
      }
    }
  }

  private void doHandleSelectorKey(
      SelectionKey key,
      SelectionKey listenedKey,
      Selector selector,
      ExecutorService executorService) {
    try {
      if (key.equals(listenedKey)) {
        ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
        SocketChannel socketChannel = ssc.accept();
        if (socketChannel != null) {
          socketChannel.configureBlocking(false);
          socketChannel.register(selector, SelectionKey.OP_READ);
        }
      } else if (key.isReadable()) {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        key.cancel();
        socketChannel.configureBlocking(true);

        executorService.execute(() -> handleVirtual(socketChannel));
      }
    } catch (IOException e) {
      throw new TcpServerException(e);
    }
  }

  private void handleVirtual(SocketChannel socketChannel) {
    try (socketChannel) {
      long threadId = Thread.currentThread().threadId();
      Thread.currentThread().setName("%s-%s".formatted(threadServer.getName(), threadId));
      if (log.isDebugEnabled()) {
        log.debug(
            "Connection open. Thread id:[{}], remote address:[{}] ",
            threadId,
            socketChannel.getRemoteAddress());
      }

      requestProcessor.handle(socketChannel);
    } catch (IOException e) {
      log.error("An exception occurred during communication.", e);
    } finally {
      if (log.isDebugEnabled()) {
        log.debug("Connection closed: {}", Thread.currentThread().getName());
      }
    }
  }

  @Override
  public DescriptionServerState getServerState() {
    return descriptionServerState;
  }

  @Override
  public void stop() {
    if (threadServer != null) {
      threadServer.interrupt();
    }
    if (executorService != null) {
      executorService.close();
    }
    descriptionServerState.setStoppedState();
  }
}
