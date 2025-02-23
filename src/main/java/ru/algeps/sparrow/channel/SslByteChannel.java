package ru.algeps.sparrow.channel;

import ru.algeps.sparrow.util.TimeWaitUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSession;

// todo сначала сделать так, чтобы данные копировались из внутренних переменных класса, затем
// оптимизация

/**
 * <a
 * href="https://docs.oracle.com/en/java/javase/21/security/java-secure-socket-extension-jsse-reference-guide.html#GUID-8796681D-06C8-4884-ADE4-782394F6F6FB:~:text=must%20be%20created.-,SSLEngine%20Methods,-There%20are%20three">SSLEngine
 * Methods</a>
 */
public class SslByteChannel implements ByteChannel {
  // https://datatracker.ietf.org/doc/html/rfc8446#autoid-60
  // 2^14 + 256 = 16_640 todo +36 байт?
  private static final int MAX_FRAGMENT_LENGTH = (1 << 14) + 256;

  //
  private final ByteChannel byteChannel;
  private final SSLEngine sslEngine;
  private SSLEngineResult sslEngineResult;
  //
  private final ByteBuffer peerNetData;
  // расшифрованные данные от удалённое узла
  private ByteBuffer peerAppData;
  // сырые данные для отправки
  private ByteBuffer localNetData;
  // расшифрованные данные для отправки
  private final ByteBuffer localAppData;
  //
  private final ExecutorService executorService;
  // constants
  private static final String CHANNEL_CLOSED_UNEXPECTEDLY_MESSAGE =
      "The connection was unexpectedly terminated";

  // исключение нужно только при закрытии соединения

  public SslByteChannel(ByteChannel byteChannel, SSLEngine sslEngine) {
    Objects.requireNonNull(byteChannel, "ByteChannel should not be null!");
    this.byteChannel = byteChannel;
    Objects.requireNonNull(sslEngine, "SSLEngine should not be null!");
    this.sslEngine = sslEngine;

    this.peerNetData = ByteBuffer.allocate(MAX_FRAGMENT_LENGTH);
    this.peerAppData = ByteBuffer.allocate(MAX_FRAGMENT_LENGTH);
    this.localNetData = ByteBuffer.allocate(MAX_FRAGMENT_LENGTH);
    this.localAppData = ByteBuffer.allocate(MAX_FRAGMENT_LENGTH);

    this.executorService = Executors.newVirtualThreadPerTaskExecutor();
  }

  public void startHandshake() throws IOException {
    sslEngine.beginHandshake();
    doHandshake();
  }

  @Override
  public int read(ByteBuffer dst) throws IOException {
    if (peerAppData.position() == 0) {
      readPeerDataFormChannel();
      doHandshake();
    }

    peerAppData.flip();

    int bytesRead = 0;
    while (dst.hasRemaining() && peerAppData.hasRemaining()) {
      dst.put(peerAppData.get());
      bytesRead++;
    }

    peerAppData.compact();

    if (bytesRead == 0 && peerAppData.position() == 0 && peerAppData.limit() == 0) {
      return -1;
    }

    return bytesRead;
  }

  private void readPeerDataFormChannel() throws IOException {
    peerNetData.clear();

    if (byteChannel.read(peerNetData) < 0) {
      throw new SslByteChannelException(CHANNEL_CLOSED_UNEXPECTEDLY_MESSAGE);
    }

    peerNetData.flip();

    do {
      sslEngineResult = sslEngine.unwrap(peerNetData, peerAppData);
    } while (peerNetData.hasRemaining()
        && sslEngineResult.getStatus() != SSLEngineResult.Status.BUFFER_UNDERFLOW);
  }

  @Override
  public int write(ByteBuffer src) throws IOException {
    int totalBytesWritten = 0;
    try {
      Thread.sleep(1);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }

    while (src.hasRemaining()) {
      if (localAppData.remaining() < src.remaining()) {
        int bytesToWrite = localAppData.remaining();
        ByteBuffer slice = src.slice();
        slice.limit(bytesToWrite);
        localAppData.put(slice);
        src.position(src.position() + bytesToWrite);
      } else {
        localAppData.put(src);
      }

      localAppData.flip();
      writeLocalAppDataInChannel();
      totalBytesWritten += localAppData.position();
      localAppData.clear();
    }

    return totalBytesWritten;
  }

  private void writeLocalAppDataInChannel() throws IOException {
    localNetData.clear();
    sslEngineResult = sslEngine.wrap(localAppData, localNetData);
    if (sslEngineResult.getStatus() != SSLEngineResult.Status.OK
        && sslEngineResult.getHandshakeStatus()
            != SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING) {
      doHandshake();
    }
    localNetData.flip();
    if (byteChannel.write(localNetData) < 0) {
      throw new SslByteChannelException(CHANNEL_CLOSED_UNEXPECTEDLY_MESSAGE);
    }
  }

  /**
   * Выполняется для каждой операции (read или write).
   *
   * <pre>
   * Описание статусов рукопожатия {@link javax.net.ssl.SSLEngineResult.HandshakeStatus}:
   *   NOT_HANDSHAKING - на данный момент рукопожатия не происходит.
   *   FINISHED - завершения этапа рукопожатия
   *   NEED_TASK - {@link SSLEngine} необходимо выполнить одну или несколько задач (например,
   * {@link javax.net.ssl.TrustManager} может потребоваться подключиться к удаленной службе проверки сертификатов
   * или {@link javax.net.ssl.KeyManager} может потребоваться предложить пользователю определить, какой сертификат
   * использовать в качестве части аутентификации клиента). Требуемые задачи можно распараллеливать.
   *   NEED_UNWRAP - Данные от другой стороны необходимо распаковать с помощью метода {@link SSLEngine#unwrap(ByteBuffer, ByteBuffer)}.
   *   NEED_UNWRAP_AGAIN - Необходима повторная распаковка. Данный статус применим только к <b>DTLS</b>
   *   NEED_WRAP - Данные необходимо запаковать и отправить другой стороне с помощью метода {@link SSLEngine#wrap(ByteBuffer, ByteBuffer)}.
   * </pre>
   *
   * <pre>
   * Описание статусов результата операции {@link javax.net.ssl.SSLEngineResult.Status}:
   *   BUFFER_UNDERFLOW - При выполнении метода {@link SSLEngine#unwrap(ByteBuffer, ByteBuffer)},
   * когда приведших данных недостаточно для распаковки. Необходимо прочитать больше байт из канала.
   *   BUFFER_OVERFLOW - При выполнении метода {@link SSLEngine#wrap(ByteBuffer, ByteBuffer)},
   * когда размер буфера для записи (dst) недостаточно для записи в него данных. Необходимо увеличить размер буфера то размера {@link SSLSession#getPacketBufferSize()}.
   *   OK - Всё хорошо, можно продолжать выполнение.
   *   CLOSED - Закрыто со стороны {@link SSLEngine}.
   * </pre>
   */
  private void doHandshake() throws IOException {
    // todo данный метод выполняется, только когда статус не OK
    SSLEngineResult.HandshakeStatus handshakeStatus = sslEngine.getHandshakeStatus();

    while (handshakeStatus != SSLEngineResult.HandshakeStatus.FINISHED
        && handshakeStatus != SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING) {
      switch (handshakeStatus) {
        case NEED_TASK -> {
          // нужно выполнить одну или несколько задач
          //  (например, проверить сертификат у центра сертификации)
          handshakeStatus = runTask();
        }
        // операция записи
        case NEED_WRAP -> {
          localNetData.clear();
          sslEngineResult = sslEngine.wrap(localAppData, localNetData);
          handshakeStatus = sslEngineResult.getHandshakeStatus();

          outboundHandleStatus(sslEngineResult.getStatus());
        }
        // операция чтения
        case NEED_UNWRAP, NEED_UNWRAP_AGAIN -> {
          peerNetData.clear();
          if (byteChannel.read(peerNetData) < 0) {
            throw new SslByteChannelException(CHANNEL_CLOSED_UNEXPECTEDLY_MESSAGE);
          }
          peerNetData.flip();

          try {
            do {
              sslEngineResult = sslEngine.unwrap(peerNetData, peerAppData);
              if (peerNetData.limit() != peerNetData.position()
                  && sslEngineResult.getHandshakeStatus()
                      == SSLEngineResult.HandshakeStatus.NEED_TASK) {
                handshakeStatus = runTask();
              }
            } while (peerNetData.limit() != peerNetData.position());
            handshakeStatus = sslEngineResult.getHandshakeStatus();
            inboundHandleStatus(sslEngineResult.getStatus());
          } catch (SSLHandshakeException e) {
            if (e.getMessage().contains("certificate_unknown")) {
              throw new SslByteChannelException(
                  "The client has indicated that the server certificate is invalid. (Received fatal alert: certificate_unknown)");
            }
          }
        }
      }
    }
  }

  private SSLEngineResult.HandshakeStatus runTask() {
    Runnable task;
    while ((task = sslEngine.getDelegatedTask()) != null) {
      executorService.execute(task);
    }

    return sslEngine.getHandshakeStatus();
  }

  private void outboundHandleStatus(SSLEngineResult.Status status) throws IOException {
    switch (status) {
      case BUFFER_UNDERFLOW -> {
        // todo что тут надо сделать
        /*peerNetData.clear();
        if (byteChannel.read(peerNetData) < 0) {
          throw new IOException("Channel closed unexpectedly");
        }
        peerNetData.flip();*/
      }
      case BUFFER_OVERFLOW -> {
        doHandleBufferOverflow();
      }
      case OK -> {
        localNetData.flip();
        while (localNetData.hasRemaining()) {
          if (byteChannel.write(localNetData) < 0) {
            throw new SslByteChannelException(CHANNEL_CLOSED_UNEXPECTEDLY_MESSAGE);
          }
        }
      }
      case CLOSED -> {
        this.close();
      }
    }
  }

  private void doHandleBufferOverflow() {
    int netSize = sslEngine.getSession().getPacketBufferSize();
    if (netSize > localNetData.capacity()) {
      ByteBuffer tempServerNetData = ByteBuffer.allocate(netSize + localNetData.position());
      localNetData.flip();
      tempServerNetData.put(localNetData);
      localNetData = tempServerNetData;
    } else {
      localNetData.clear();
    }
  }

  private void inboundHandleStatus(SSLEngineResult.Status status) throws IOException {
    switch (status) {
      case BUFFER_UNDERFLOW -> {
        /*peerNetData.compact();
        if (byteChannel.read(peerNetData) < 0) {
          throw new SslByteChannelException(CHANNEL_CLOSED_UNEXPECTEDLY_MESSAGE);
        }
        peerNetData.flip();*/
      }
      case BUFFER_OVERFLOW -> {
        int netSize = sslEngine.getSession().getApplicationBufferSize();
        if (netSize > peerAppData.capacity()) {
          ByteBuffer tempServerNetData = ByteBuffer.allocate(netSize + peerAppData.position());
          peerAppData.flip();
          tempServerNetData.put(peerAppData);
          peerAppData = tempServerNetData;
        }
      }
      case OK -> {}
      case CLOSED -> this.close();
    }
  }

  @Override
  public boolean isOpen() {
    return byteChannel.isOpen();
  }

  /** Мы сначала отправляем сигнал о завершении работы, затем принимает alert "close_notify" */
  @Override
  public void close() throws IOException {
    try {
      sslEngine.closeOutbound();

      while (!sslEngine.isOutboundDone()) {
        localNetData.clear();
        SSLEngineResult result = sslEngine.wrap(localAppData, localNetData);

        if (result.getStatus() == SSLEngineResult.Status.BUFFER_OVERFLOW) {
          doHandleBufferOverflow();
        }

        if (result.getStatus() == SSLEngineResult.Status.OK
            || result.getStatus() == SSLEngineResult.Status.CLOSED) {
          localNetData.flip();
          while (localNetData.hasRemaining()) {
            if (byteChannel.write(localNetData) < 0) {
              throw new SslByteChannelException(CHANNEL_CLOSED_UNEXPECTEDLY_MESSAGE);
            }
          }
        }
      }

      peerNetData.clear();
      peerAppData.clear();
      TimeWaitUtil.runUntil(
          () -> {
            int read;
            if ((read = byteChannel.read(peerNetData)) > 0) {
              peerNetData.flip();
              sslEngine.unwrap(peerNetData, peerAppData);
            }
            return read > 0;
          },
          Duration.ofSeconds(1),
          Duration.ofMillis(100));

      // мы должны были получить сигнал о завершении
      sslEngine.closeInbound();
    } finally {
      byteChannel.close();
      executorService.close();
      // принудительно указываем, что входящего трафика больше не будет
      sslEngine.closeInbound();
      executorService.shutdown();
    }
  }

  public static class SslByteChannelException extends IOException {
    public SslByteChannelException(String message) {
      super(message);
    }
  }
}
