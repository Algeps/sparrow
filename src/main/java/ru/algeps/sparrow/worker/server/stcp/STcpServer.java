package ru.algeps.sparrow.worker.server.stcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardProtocolFamily;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.security.SecureRandom;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.net.ssl.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.algeps.sparrow.channel.*;
import ru.algeps.sparrow.util.security.algorithms.SslAlgorithm;
import ru.algeps.sparrow.worker.processor.RequestProcessor;
import ru.algeps.sparrow.worker.server.Server;
import ru.algeps.sparrow.worker.server.state.DescriptionServerState;
import ru.algeps.sparrow.worker.server.stcp.exception.STcpServerException;
import ru.algeps.sparrow.worker.server.tcp.TcpServerConfig;

public class STcpServer implements Server {
  private final Logger log;
  private final int port;
  private final int backlog;
  private final Thread threadServer;
  private final RequestProcessor requestProcessor;
  private ExecutorService executorService;
  /////////////// server state ///////////////
  private final DescriptionServerState descriptionServerState;
  /////////////// SSL ///////////////
  private final String strictHost;
  private final String protocol;
  private final TrustManager[] trustManagers;
  private final KeyManager[] keyManagers;
  private final SecureRandom secureRandom;

  public STcpServer(
      TcpServerConfig config,
      SslAlgorithm sslAlgorithm,
      String strictHost,
      TrustManager[] trustManagers,
      KeyManager[] keyManagers,
      SecureRandom secureRandom) {
    log = LoggerFactory.getLogger(config.getName());

    this.backlog = config.getBacklog();
    this.port = config.getPort();
    this.requestProcessor = config.getRequestProcessor();
    this.strictHost = strictHost;
    this.protocol = sslAlgorithm.getAlgorithmName();
    this.trustManagers = trustManagers;
    this.keyManagers = keyManagers;
    this.secureRandom = secureRandom;

    this.threadServer = Thread.ofPlatform().name(log.getName()).unstarted(this::run);

    this.descriptionServerState = new DescriptionServerState();
  }

  public STcpServer(TcpServerConfig config, SslAlgorithm sslAlgorithm) {
    this(config, sslAlgorithm, "localhost", null, null, null);
  }

  @Override
  public void start() throws STcpServerException {
    threadServer.start();
  }

  private void run() {
    try {
      descriptionServerState.setStartingState();

      SSLContext sslContext = getConfiguredSslContext();

      try (ServerSocketChannel serverSocketChannel =
          ServerSocketChannel.open(StandardProtocolFamily.INET)) {
        executorService = Executors.newVirtualThreadPerTaskExecutor();
        executorService = Executors.newFixedThreadPool(2);

        configAndBindServerSocketChannel(serverSocketChannel);
        runSelector(serverSocketChannel, executorService, sslContext);
      }
    } catch (Exception e) {
      log.error("Exception in Security TcpServer.", e);
      descriptionServerState.setErrorState(e);
    }
  }

  private SSLContext getConfiguredSslContext() throws Exception {
    SSLContext sslContext = SSLContext.getInstance(protocol);
    sslContext.init(keyManagers, trustManagers, secureRandom);
    return sslContext;
  }

  /** Конфигурация ServerSocketChannel. */
  private void configAndBindServerSocketChannel(ServerSocketChannel serverSocketChannel)
      throws IOException {
    InetSocketAddress inetSocketAddress = new InetSocketAddress(port);
    serverSocketChannel.bind(inetSocketAddress, backlog);

    serverSocketChannel.configureBlocking(false);

    log.info("Server starting in:{}", serverSocketChannel.getLocalAddress());
    String stringUsePort = serverSocketChannel.getLocalAddress().toString().split(":")[1];
    int usePort = Integer.parseInt(stringUsePort);
    descriptionServerState.addUsePort(usePort);
    if (log.isDebugEnabled()) {
      log.debug("Server stcp use: strict host:'{}', protocol: '{}'", strictHost, protocol);
    }
  }

  /** Выполняет обработку запросов, пока не прервётся поток. */
  private void runSelector(
      ServerSocketChannel serverSocketChannel,
      ExecutorService executorService,
      SSLContext sslContext)
      throws IOException {
    Selector selector = SelectorProvider.provider().openSelector();
    SelectionKey listenedKey = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    descriptionServerState.setRunningState();

    while (!threadServer.isInterrupted()) {
      selector.select();
      Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();

      while (keyIterator.hasNext()) {
        SelectionKey key = keyIterator.next();
        keyIterator.remove();
        doHandleSelectorKey(key, listenedKey, selector, executorService, sslContext);
      }
    }
  }

  private void doHandleSelectorKey(
      SelectionKey key,
      SelectionKey listenedKey,
      Selector selector,
      ExecutorService executorService,
      SSLContext sslContext)
      throws IOException {
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

      executorService.execute(() -> handleVirtual(socketChannel, sslContext));
    }
  }

  private void handleVirtual(SocketChannel socketChannel, SSLContext sslContext) {
    SSLEngine sslEngine = sslContext.createSSLEngine(strictHost, port);
    sslEngine.setUseClientMode(false);
    sslEngine.setNeedClientAuth(false);
    sslEngine.setWantClientAuth(false);

    try (SslByteChannel sslByteChannel = new SslByteChannel(socketChannel, sslEngine)) {
      socketChannel.configureBlocking(false);

      while (!socketChannel.finishConnect()) {
        // ожидаем пока будет полноценное подключение, чтобы можно было сразу взять не
        // MAX_FRAGMENT_LENGTH, а размер из сессии
      }

      long threadId = Thread.currentThread().threadId();
      Thread.currentThread().setName("%s-%s".formatted(threadServer.getName(), threadId));
      if (log.isDebugEnabled()) {
        log.debug(
            "Connection open. Thread id:[{}], remote address:[{}] ",
            threadId,
            socketChannel.getRemoteAddress());
      }

      sslByteChannel.startHandshake();
      requestProcessor.handle(sslByteChannel);
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
