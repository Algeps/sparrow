package ru.algeps.sparrow.message.util;

import com.sun.net.httpserver.Headers;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import static ru.algeps.sparrow.message.request.parser.http.AsciiAlphabet.CARRIAGE_RETURN;
import static ru.algeps.sparrow.message.request.parser.http.AsciiAlphabet.LINE_FEED;

public final class HttpMessageUtil {
  private HttpMessageUtil() {}

  private static final byte[] HEX_ASCII = new byte[255];

  static {
    Arrays.fill(HEX_ASCII, (byte) -1);
    for (int i = 0; i <= 9; i++) {
      HEX_ASCII[i] = (byte) (i + '0');
    }
    for (int i = 10; i <= 15; i++) {
      HEX_ASCII[i] = (byte) (i - 10 + 'a');
    }
  }

  public static final Headers EMPTY_HEADERS = new EmptyHeaders();

  static class EmptyHeaders extends Headers {
    @Override
    public List<String> put(String key, List<String> value) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(Map<? extends String, ? extends List<String>> t) {
      throw new UnsupportedOperationException();
    }

    @Override
    public List<String> compute(
        String key,
        BiFunction<? super String, ? super List<String>, ? extends List<String>>
            remappingFunction) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void add(String key, String value) {
      throw new UnsupportedOperationException();
    }

    @Override
    public List<String> putIfAbsent(String key, List<String> value) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void set(String key, String value) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean replace(String key, List<String> oldValue, List<String> newValue) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object key, Object value) {
      throw new UnsupportedOperationException();
    }

    @Override
    public List<String> computeIfAbsent(
        String key, Function<? super String, ? extends List<String>> mappingFunction) {
      throw new UnsupportedOperationException();
    }

    @Override
    public List<String> computeIfPresent(
        String key,
        BiFunction<? super String, ? super List<String>, ? extends List<String>>
            remappingFunction) {
      throw new UnsupportedOperationException();
    }

    @Override
    public List<String> replace(String key, List<String> value) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void replaceAll(
        BiFunction<? super String, ? super List<String>, ? extends List<String>> function) {
      throw new UnsupportedOperationException();
    }

    @Override
    public List<String> getOrDefault(Object key, List<String> defaultValue) {
      throw new UnsupportedOperationException();
    }

    @Override
    public List<String> merge(
        String key,
        List<String> value,
        BiFunction<? super List<String>, ? super List<String>, ? extends List<String>>
            remappingFunction) {
      throw new UnsupportedOperationException();
    }
  }

  private static final byte[] EMPTY_BYTE_ARRAY = new byte[] {};

  //
  public static final int BYTE_MAX = 0xFF_FF_FF_00;
  public static final int TWO_BYTE_MAX = 0xFF_FF_00_00;
  public static final int THREE_BYTE_MAX = 0xFF_00_00_00;
  //
  public static final byte[] NEW_LINE = new byte[] {CARRIAGE_RETURN, LINE_FEED};
  public static final byte[] EMPTY_CHUNK = new byte[] {'0', CARRIAGE_RETURN, LINE_FEED};
  //
  public static final int HEX_CHARS_PER_INT = 8;
  public static final int BITS_PER_HEX_DIGIT = 4;

  //
  /** Максимальный размер данных для чанка: 2_147_483_635 */
  public static final int MAX_SIZE_DATA_FOR_CHUNK =
      Integer.MAX_VALUE - (8 + NEW_LINE.length + NEW_LINE.length);

  /////////////////////////////////////////////////
  /**
   * Создает новый байтовый массив заданного размера и копирует в него данные из исходного массива.
   */
  public static byte[] copyBytes(byte[] source, int targetSize) {
    if (source == null) {
      throw new IllegalArgumentException("Source bytes array is null!");
    }

    if (targetSize < 0) {
      throw new IllegalArgumentException("The size of the target array cannot be negative");
    }

    if (source.length > targetSize) {
      throw new IllegalArgumentException(
          "The size of the source array exceeds the size of the target array");
    }

    byte[] target = new byte[targetSize];
    System.arraycopy(source, 0, target, 0, source.length);
    return target;
  }

  /**
   * @param data данные для оборачивания в чанк. Размер должен быть меньше чем (Integer.MAX_VALUE -
   *     8 (размер чанка) - 4(NEW_LINE)), иначе чанк не сможет уложиться в размер int.
   */
  public static ByteBuffer packChunkToBytesBuffer(byte[] data) {
    return ByteBuffer.wrap(packChunkToBytesArray(data));
  }

  /** Возвращает собранный чанк. Если массив равен 0 или null, то будет возвращён пустой чанк. */
  public static byte[] packChunkToBytesArray(byte[] data) {
    if (data == null || data.length == 0) {
      return EMPTY_CHUNK;
    }

    if (data.length > MAX_SIZE_DATA_FOR_CHUNK) {
      throw new IllegalArgumentException(
          "The length of the data for the chunk is more than %s: %s"
              .formatted(MAX_SIZE_DATA_FOR_CHUNK, data.length));
    }

    byte[] sizeChunk = intToHexBytes(data.length);
    int totalSize = sizeChunk.length + NEW_LINE.length + data.length + NEW_LINE.length;

    byte[] result = new byte[totalSize];
    System.arraycopy(sizeChunk, 0, result, 0, sizeChunk.length);
    result[sizeChunk.length] = NEW_LINE[0];
    result[sizeChunk.length + 1] = NEW_LINE[1];
    System.arraycopy(data, 0, result, sizeChunk.length + 2, data.length);
    result[result.length - 2] = NEW_LINE[0];
    result[result.length - 1] = NEW_LINE[1];
    return result;
  }

  public static int hexBytesToInt(byte[] bytes) {
    if (bytes == null) {
      throw new IllegalArgumentException("Null value!");
    }

    return hexBytesToInt(bytes, bytes.length);
  }

  /**
   * Возвращает integer представление hex входного массива.
   *
   * @param length количество байт, которое необходимо прочитать из массива
   */
  public static int hexBytesToInt(byte[] bytes, int length) {
    if (bytes == null) {
      throw new IllegalArgumentException("Null value!");
    }

    if (bytes.length > HEX_CHARS_PER_INT) {
      throw new IllegalArgumentException(
          "For the size of the integer, more hex characters were received than the maximum allowed");
    }

    if (length <= 0) {
      throw new IllegalArgumentException("Argument <= 0!");
    }

    if (length > bytes.length) {
      throw new IllegalArgumentException("Length more than bytes array length");
    }

    return doHexBytesToInt(bytes, length);
  }

  private static int doHexBytesToInt(byte[] bytes, int length) {
    int result = 0;
    for (int i = 0; i < length; i++) {
      byte b = bytes[i];
      int digit;
      if (b >= '0' && b <= '9') {
        digit = b - '0';
      } else if (b >= 'a' && b <= 'f') {
        digit = b - 'a' + 10;
      } else if (b >= 'A' && b <= 'F') {
        digit = b - 'A' + 10;
      } else {
        throw new IllegalArgumentException(
            "Invalid hex character: %s, byte value: %s".formatted((char) b, b));
      }
      result = (result << BITS_PER_HEX_DIGIT) | digit;
    }
    return result;
  }

  /** Представляет каждый байт в HEX представлении. */
  public static byte[] intToHexBytes(int val) {
    if (val < 0) {
      throw new IllegalArgumentException("Argument < 0!");
    }

    if (val == 0) {
      return new byte[] {'0'};
    }

    // детерминированный алгоритм, вычисляющий количество шестнадцатеричных чисел
    int length = (Integer.SIZE - Integer.numberOfLeadingZeros(val) + 3) / 4;

    byte[] result = new byte[length];
    for (int i = length - 1; i >= 0; i--) {
      result[i] = HEX_ASCII[val & 0xF];
      val >>= 4;
    }
    return result;
  }

  /** Создаёт новый чанк и объединяет со старым чанком в новом массиве. */
  public static byte[] concatWithNewChunk(byte[] oldChunk, byte[] newChunkData) {
    byte[] newChunk = packChunkToBytesArray(newChunkData);
    int concatChunksSize = oldChunk.length + newChunk.length;

    // если размер меньше (переполнение суммой)
    if (concatChunksSize < oldChunk.length) {
      throw new IllegalArgumentException("Result chunk");
    }

    byte[] chunks = new byte[concatChunksSize];
    System.arraycopy(oldChunk, 0, chunks, 0, oldChunk.length);
    System.arraycopy(newChunk, 0, chunks, oldChunk.length, newChunk.length);
    return chunks;
  }

  public static byte[] separateBytes(int val) {
    if ((val & BYTE_MAX) == 0) {
      return new byte[] {(byte) val};
    } else if ((val & TWO_BYTE_MAX) == 0) {
      return new byte[] {(byte) (val >> 8), (byte) (val)};
    } else if ((val & THREE_BYTE_MAX) == 0) {
      return new byte[] {(byte) (val >> 16), (byte) (val >> 8), (byte) (val)};
    }
    return new byte[] {(byte) (val >> 24), (byte) (val >> 16), (byte) (val >> 8), (byte) (val)};
  }
}
