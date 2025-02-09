package ru.algeps.sparrow.util;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.stream.Stream;

public final class FileUtil {
  private FileUtil() {}

  public static String getFileExtension(String filePath) {
    Objects.requireNonNull(filePath);

    if (filePath.isEmpty()) {
      return "";
    }

    int indexDot = -1;
    int i = filePath.length() - 1;
    char c = filePath.charAt(i);

    while (i > 0 || c == '\\' || c == '/') {
      if (filePath.charAt(i) == '.') {
        indexDot = i;
        break;
      }

      i--;
    }

    return indexDot == -1 ? "" : filePath.substring(++indexDot);
  }

  public static String getFileExtension(String filePath, int maxLengthExtension) {
    Objects.requireNonNull(filePath);
    int indexDot = -1;

    for (int i = filePath.length() - 1;
        i > 0 || maxLengthExtension > 0;
        i--, maxLengthExtension--) {
      char c = filePath.charAt(i);

      if (c == '\\' || c == '/') {
        break;
      }

      if (filePath.charAt(i) == '.') {
        indexDot = i;
        break;
      }
    }
    return indexDot == -1 ? "" : filePath.substring(++indexDot);
  }

  /**
   * @return null если файл не найден.
   */
  public static Stream<String> readAllStringFile(Path path) {
    try {
      return Files.lines(path);
    } catch (IOException e) {
      return null;
    }
  }

  /**
   * @return null если файл не найден.
   */
  public static byte[] readFile(Path path) {
    try {
      return Files.readAllBytes(path);
    } catch (IOException e) {
      return null;
    }
  }

  /** */
  public static void readBigFile(
      Path path, int partLength, ConsumerWithIoException<ByteBuffer> readConsumer)
      throws IOException {
    try (RandomAccessFile raf = new RandomAccessFile(path.toFile(), "r");
        FileChannel fileChannel = raf.getChannel()) {
      ByteBuffer byteBuffer = ByteBuffer.allocate(partLength);

      while (fileChannel.read(byteBuffer) != -1) {
        byteBuffer.flip();
        readConsumer.accept(byteBuffer);
        byteBuffer.rewind();
      }
    }
  }

  /** Возвращает путь classpath ресурсов */
  public static Path getClasspathResourceDirPath() {
    try {
      return Path.of(ClassLoader.getSystemResource("").toURI());
    } catch (URISyntaxException e) {
      throw new IllegalStateException("Невозможно получить URI SystemClassLoader", e);
    }
  }
}
