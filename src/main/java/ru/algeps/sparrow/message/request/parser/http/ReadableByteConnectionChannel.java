package ru.algeps.sparrow.message.request.parser.http;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

/** Обертка над ReadableByteChannel с новым функционалом чтения. */
public class ReadableByteConnectionChannel implements ReadableByteChannel {
  private final ReadableByteChannel readableByteChannel;
  private final ByteBuffer byteBuffer;
  private long readByteCount;
  private static final int BUFFER_SIZE = 1_000;
  //
  private static final String END_OF_STREAM_MESSAGE = "End-of-stream of readable channel";

  public ReadableByteConnectionChannel(ReadableByteChannel byteChannel) throws IOException {
    this.readableByteChannel = byteChannel;
    byteBuffer = ByteBuffer.allocate(BUFFER_SIZE);
    newRead();
  }

  /**
   * @return байт из потока
   * @throws ReadableByteConnectionChannelException если произошла ошибка чтения или достигнут конец
   *     потока
   */
  public byte readByte() throws ReadableByteConnectionChannelException {
    try {
      if (!byteBuffer.hasRemaining()) {
        if (newRead() == -1) {
          throw new ReadableByteConnectionChannelException(END_OF_STREAM_MESSAGE);
        }
      }
      readByteCount++;
      return byteBuffer.get();
    } catch (ReadableByteConnectionChannelException e) {
      throw e;
    } catch (BufferUnderflowException | IOException e) {
      throw new ReadableByteConnectionChannelException(e);
    }
  }

  public byte[] readBytes(int count) throws ReadableByteConnectionChannelException {
    try {
      byte[] bytes = new byte[count];
      int byteBufferRemaining = byteBuffer.remaining();

      if (byteBufferRemaining < count) {
        // записываем остаток байт
        byteBuffer.get(byteBuffer.position(), bytes, 0, byteBufferRemaining);
        if (newRead() == -1) {
          throw new ReadableByteConnectionChannelException(END_OF_STREAM_MESSAGE);
        }
        int forWritingRemaining = count - byteBufferRemaining;
        byteBuffer.get(byteBuffer.position(), bytes, byteBufferRemaining, forWritingRemaining);
        byteBuffer.position(forWritingRemaining);
      } else {
        byteBuffer.get(bytes);
      }
      readByteCount += count;
      return bytes;
    } catch (ReadableByteConnectionChannelException e) {
      throw e;
    } catch (BufferUnderflowException | IOException e) {
      throw new ReadableByteConnectionChannelException(e);
    }
  }

  public int readInt() throws ReadableByteConnectionChannelException {
    try {
      if (!byteBuffer.hasRemaining()) {
        readByteCount++;
        if (newRead() == -1) {
          throw new ReadableByteConnectionChannelException(END_OF_STREAM_MESSAGE);
        }
      }
      return byteBuffer.getInt();
    } catch (ReadableByteConnectionChannelException e) {
      throw e;
    } catch (BufferUnderflowException | IOException e) {
      throw new ReadableByteConnectionChannelException(e.getMessage());
    }
  }

  /** Чтение нового буфера из потока. */
  private int newRead() throws IOException {
    byteBuffer.clear();
    int readCount;
    while ((readCount = readableByteChannel.read(byteBuffer)) == 0) {
      Thread.onSpinWait();
    }
    if (readCount == -1) {
      throw new ReadableByteConnectionChannelException("Data is empty! (read return -1)");
    }

    byteBuffer.flip();
    return readCount;
  }

  @Override
  public int read(ByteBuffer dst) throws IOException {
    int dstRemaining = dst.remaining();
    int innerRemaining = byteBuffer.remaining();
    int readCount = 0;

    if (innerRemaining >= dstRemaining) {
      while (dst.hasRemaining()) {
        dst.put(dst.get());
        readCount++;

        readByteCount++;
      }
    } else {
      do {
        while (byteBuffer.hasRemaining()) {
          dst.put(dst.get());
          readCount++;

          readByteCount++;
        }
      } while (newRead() != -1 || dst.hasRemaining());
    }

    return readCount;
  }

  /** Возвращает true, если ещё можно читать из потока. */
  public boolean hasRemaining() {
    if (!byteBuffer.hasRemaining()) {
      try {
        return (newRead() != -1) && byteBuffer.hasRemaining();
      } catch (IOException ignored) {
        return false;
      }
    }
    return true;
  }

  public long getReadByteCount() {
    return readByteCount;
  }

  @Override
  public boolean isOpen() {
    return readableByteChannel.isOpen();
  }

  @Override
  public void close() throws IOException {
    readableByteChannel.close();
  }

  public static class ReadableByteConnectionChannelException extends IOException {
    public ReadableByteConnectionChannelException(String message) {
      super(message);
    }

    public ReadableByteConnectionChannelException(Throwable cause) {
      super(cause);
    }
  }
}
