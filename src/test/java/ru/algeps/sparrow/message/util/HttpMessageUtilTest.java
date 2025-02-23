package ru.algeps.sparrow.message.util;

import static org.junit.jupiter.api.Assertions.*;
import static ru.algeps.sparrow.message.util.HttpMessageUtil.MAX_SIZE_DATA_FOR_CHUNK;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class HttpMessageUtilTest {

  @Test
  void test_separateBytes_10() {
    int val = 10;

    byte[] hexRepresentation = HttpMessageUtil.separateBytes(val);
    assertArrayEquals(new byte[] {0xA}, hexRepresentation);
  }

  @Test
  void test_separateBytes_1000() {
    int val = 1_000;

    byte[] hexRepresentation = HttpMessageUtil.separateBytes(val);
    assertArrayEquals(new byte[] {0x3, (byte) 0xE8}, hexRepresentation);
  }

  @Test
  void test_separateBytes_9_837_465() {
    int val = 9_837_465;

    byte[] hexRepresentation = HttpMessageUtil.separateBytes(val);
    assertArrayEquals(new byte[] {(byte) 0x96, 0x1B, (byte) 0x99}, hexRepresentation);
  }

  @Test
  void test_separateBytes_minus_1() {
    int val = -1;

    byte[] hexRepresentation = HttpMessageUtil.separateBytes(val);
    assertArrayEquals(
        new byte[] {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF}, hexRepresentation);
  }

  @Test
  void test_separateBytes_minus_1_073_941() {
    int val = -1_073_941;

    byte[] hexRepresentation = HttpMessageUtil.separateBytes(val);
    assertArrayEquals(
        new byte[] {(byte) 0xFF, (byte) 0xEF, (byte) 0x9C, (byte) 0xEB}, hexRepresentation);
  }

  @Test
  void test_intToHexBytes_0() {
    int val = 0x0;

    byte[] hexRepresentation = HttpMessageUtil.intToHexBytes(val);
    // должен вернуть 97
    assertArrayEquals(new byte[] {'0'}, hexRepresentation);
  }

  @Test
  void test_intToHexBytes_A() {
    int val = 0xA;

    byte[] hexRepresentation = HttpMessageUtil.intToHexBytes(val);
    // должен вернуть 97
    assertArrayEquals(new byte[] {'a'}, hexRepresentation);
  }

  // intToHexBytes //

  @Test
  void test_intToHexBytes_3D() {
    int val = 0x3D;

    byte[] hexRepresentation = HttpMessageUtil.intToHexBytes(val);
    assertArrayEquals(new byte[] {'3', 'd'}, hexRepresentation);
  }

  @Test
  void test_intToHexBytes_7_D0() {
    int val = 0x7_D0;

    byte[] hexRepresentation = HttpMessageUtil.intToHexBytes(val);
    assertArrayEquals(new byte[] {'7', 'd', '0'}, hexRepresentation);
  }

  @Test
  void test_intToHexBytes_E2_F5() {
    int val = 0xE2_F5;

    byte[] hexRepresentation = HttpMessageUtil.intToHexBytes(val);
    assertArrayEquals(new byte[] {'e', '2', 'f', '5'}, hexRepresentation);
  }

  @Test
  void test_intToHexBytes_A_E2_F5() {
    int val = 0xA_E2_F5;

    byte[] hexRepresentation = HttpMessageUtil.intToHexBytes(val);
    assertArrayEquals(new byte[] {'a', 'e', '2', 'f', '5'}, hexRepresentation);
  }

  @Test
  void test_intToHexBytes_A5_E2_F5() {
    int val = 0xA5_E2_F5;

    byte[] hexRepresentation = HttpMessageUtil.intToHexBytes(val);
    assertArrayEquals(new byte[] {'a', '5', 'e', '2', 'f', '5'}, hexRepresentation);
  }

  @Test
  void test_intToHexBytes_2_A5_E2_F5() {
    int val = 0x2_A5_E2_F5;

    byte[] hexRepresentation = HttpMessageUtil.intToHexBytes(val);
    assertArrayEquals(new byte[] {'2', 'a', '5', 'e', '2', 'f', '5'}, hexRepresentation);
  }

  @Test
  void test_intToHexBytes_24_A5_E2_F5() {
    int val = 0x24_A5_E2_F5;

    byte[] hexRepresentation = HttpMessageUtil.intToHexBytes(val);
    assertArrayEquals(new byte[] {'2', '4', 'a', '5', 'e', '2', 'f', '5'}, hexRepresentation);
  }

  @Test
  void test_intToHexBytes_minus_value() {
    int val = -1;

    assertThrows(IllegalArgumentException.class, () -> HttpMessageUtil.intToHexBytes(val));
  }

  /// packChunkToBytesArray ///
  @Test
  void test_packChunkToBytesBuffer_0_size() {
    byte[] data = new byte[0];
    byte[] chunk = HttpMessageUtil.packChunkToBytesBuffer(data).array();
    assertArrayEquals("0\r\n".getBytes(StandardCharsets.UTF_8), chunk);
  }

  @Test
  void test_packChunkToBytesBuffer_10_size() {
    byte[] data = new byte[10];
    byte[] chunk = HttpMessageUtil.packChunkToBytesBuffer(data).array();
    assertEquals('a', chunk[0]);
    assertEquals('\r', chunk[1]);
    assertEquals('\n', chunk[2]);
    assertEquals('\r', chunk[chunk.length - 2]);
    assertEquals('\n', chunk[chunk.length - 1]);
  }

  /// packChunkToBytesArray ///
  @Test
  void test_packChunkToBytesArray_0_size() {
    byte[] data = new byte[0];
    byte[] chunk = HttpMessageUtil.packChunkToBytesArray(data);
    assertArrayEquals("0\r\n".getBytes(StandardCharsets.UTF_8), chunk);
  }

  @Test
  void test_packChunkToBytesArray_null() {
    byte[] chunk = HttpMessageUtil.packChunkToBytesArray(null);
    assertArrayEquals("0\r\n".getBytes(StandardCharsets.UTF_8), chunk);
  }

  @Test
  void test_packChunkToBytesArray_1_size() {
    byte[] data = new byte[] {1};
    byte[] chunk = HttpMessageUtil.packChunkToBytesArray(data);
    assertArrayEquals("1\r\n\1\r\n".getBytes(StandardCharsets.UTF_8), chunk);
  }

  @Test
  void test_packChunkToBytesArray_10_size() {
    byte[] data = new byte[10];
    byte[] chunk = HttpMessageUtil.packChunkToBytesArray(data);
    assertEquals('a', chunk[0]);
    assertEquals('\r', chunk[1]);
    assertEquals('\n', chunk[2]);
    assertEquals('\r', chunk[chunk.length - 2]);
    assertEquals('\n', chunk[chunk.length - 1]);
  }

  @Test
  void test_packChunkToBytesArray_15_size() {
    byte[] data = new byte[15];
    byte[] chunk = HttpMessageUtil.packChunkToBytesArray(data);
    assertArrayEquals(
        "f\r\n\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\r\n".getBytes(StandardCharsets.UTF_8), chunk);
  }

  @Test
  void test_packChunkToBytesArray_16_size() {
    byte[] data = new byte[16];
    byte[] chunk = HttpMessageUtil.packChunkToBytesArray(data);
    assertArrayEquals(
        "10\r\n\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\r\n".getBytes(StandardCharsets.UTF_8), chunk);
  }

  @Test
  void test_packChunkToBytesArray_too_large() {
    byte[] data = new byte[MAX_SIZE_DATA_FOR_CHUNK + 1];
    assertThrows(IllegalArgumentException.class, () -> HttpMessageUtil.packChunkToBytesArray(data));
  }

  @Test
  void test_packChunkToBytesArray_with_data() {
    byte[] data = "Hello".getBytes(StandardCharsets.UTF_8);
    byte[] chunk = HttpMessageUtil.packChunkToBytesArray(data);
    assertArrayEquals("5\r\nHello\r\n".getBytes(StandardCharsets.UTF_8), chunk);
  }

  @Test
  void test_packChunkToBytesArray_with_special_chars() {
    byte[] data = "\0\1\t\n\r".getBytes(StandardCharsets.UTF_8);
    byte[] chunk = HttpMessageUtil.packChunkToBytesArray(data);
    assertArrayEquals("5\r\n\0\1\t\n\r\r\n".getBytes(StandardCharsets.UTF_8), chunk);
  }

  @Test
  void test_packChunkToBytesArray_with_unicode() {
    byte[] data = "Привет".getBytes(StandardCharsets.UTF_8);
    byte[] chunk = HttpMessageUtil.packChunkToBytesArray(data);
    assertArrayEquals("c\r\nПривет\r\n".getBytes(StandardCharsets.UTF_8), chunk);
  }

  /// packChunkToBytesArray ///

  @Test
  void test_concat_empty_chunks() {
    byte[] oldChunk = HttpMessageUtil.packChunkToBytesArray(new byte[0]);
    byte[] newChunkData = new byte[0];
    byte[] result = HttpMessageUtil.concatWithNewChunk(oldChunk, newChunkData);
    byte[] expected = "0\r\n0\r\n".getBytes(StandardCharsets.UTF_8);
    assertArrayEquals(expected, result);
  }

  @Test
  void test_concat_small_chunks() {
    byte[] oldChunk = HttpMessageUtil.packChunkToBytesArray("Hello".getBytes(StandardCharsets.UTF_8));
    byte[] newChunkData = "World".getBytes(StandardCharsets.UTF_8);
    byte[] result = HttpMessageUtil.concatWithNewChunk(oldChunk, newChunkData);
    byte[] expected = "5\r\nHello\r\n5\r\nWorld\r\n".getBytes(StandardCharsets.UTF_8);
    assertArrayEquals(expected, result);
  }

  @Test
  void test_concat_one_empty_one_not_empty() {
    byte[] oldChunk = HttpMessageUtil.packChunkToBytesArray(new byte[0]);
    byte[] newChunkData = "Data".getBytes(StandardCharsets.UTF_8);
    byte[] result = HttpMessageUtil.concatWithNewChunk(oldChunk, newChunkData);
    byte[] expected = "0\r\n4\r\nData\r\n".getBytes(StandardCharsets.UTF_8);
    assertArrayEquals(expected, result);

    oldChunk = HttpMessageUtil.packChunkToBytesArray("Data".getBytes(StandardCharsets.UTF_8));
    newChunkData = new byte[0];
    result = HttpMessageUtil.concatWithNewChunk(oldChunk, newChunkData);
    expected = "4\r\nData\r\n0\r\n".getBytes(StandardCharsets.UTF_8);
    assertArrayEquals(expected, result);
  }

  @Test
  void test_concat_large_chunks() {
    byte[] oldChunkData = new byte[50];
    Arrays.fill(oldChunkData, (byte) 'a');
    byte[] newChunkData = new byte[70];
    Arrays.fill(newChunkData, (byte) 'b');

    byte[] oldChunk = HttpMessageUtil.packChunkToBytesArray(oldChunkData);

    byte[] result = HttpMessageUtil.concatWithNewChunk(oldChunk, newChunkData);

    byte[] expected =
        ("32\r\n"
                + new String(oldChunkData, StandardCharsets.UTF_8)
                + "\r\n46\r\n"
                + new String(newChunkData, StandardCharsets.UTF_8)
                + "\r\n")
            .getBytes(StandardCharsets.UTF_8);
    assertArrayEquals(expected, result);
  }

  @Test
  void test_concat_overflow_throws_exception() {
    byte[] oldChunk = new byte[Integer.MAX_VALUE - 10]; // почти максимальный размер
    byte[] newChunk = new byte[20];

    assertThrows(
        IllegalArgumentException.class,
        () -> HttpMessageUtil.concatWithNewChunk(oldChunk, newChunk));
  }

  @Test
  void test_concat_chunks_with_special_characters() {
    byte[] oldChunkData = "\r\n\t".getBytes(StandardCharsets.UTF_8);
    byte[] newChunkData = "спец\0симв".getBytes(StandardCharsets.UTF_8);

    byte[] oldChunk = HttpMessageUtil.packChunkToBytesArray(oldChunkData);

    byte[] result = HttpMessageUtil.concatWithNewChunk(oldChunk, newChunkData);

    byte[] expected = ("3\r\n\r\n\t\r\n11\r\nспец\0симв\r\n").getBytes(StandardCharsets.UTF_8);
    assertArrayEquals(expected, result);
  }

  /// hexBytesToInt ///
  @Test
  void test_hexBytesToInt_withParams_nullInput() {
    assertThrows(
        IllegalArgumentException.class,
        () -> HttpMessageUtil.hexBytesToInt(null, 0),
        "Null value!");
  }

  @Test
  void test_hexBytesToInt_withParams_zeroLengthValue() {
    assertThrows(
        IllegalArgumentException.class, () -> HttpMessageUtil.hexBytesToInt(new byte[] {1, 2}, 0));
  }

  @Test
  void test_hexBytesToInt_withParams_lessThenZeroLengthValue() {
    assertThrows(
        IllegalArgumentException.class, () -> HttpMessageUtil.hexBytesToInt(new byte[] {1, 2}, -5));
  }

  @Test
  void test_hexBytesToInt_withParams_moreThenBytesArrayLengthLengthValue() {
    assertThrows(
        IllegalArgumentException.class, () -> HttpMessageUtil.hexBytesToInt(new byte[] {1, 2}, 10));
  }

  @Test
  void test_hexBytesToInt_nullInput() {
    assertThrows(
        IllegalArgumentException.class, () -> HttpMessageUtil.hexBytesToInt(null), "Null value!");
  }

  @Test
  void test_hexBytesToInt_tooLongInput() {
    byte[] tooLongHexBytes = "000000000".getBytes();
    assertThrows(
        IllegalArgumentException.class,
        () -> HttpMessageUtil.hexBytesToInt(tooLongHexBytes),
        "For the size of the integer, more hex characters were received than the maximum allowed");
  }

  @ParameterizedTest
  @MethodSource("validHexBytesProvider")
  void test_hexBytesToInt_validInput(byte[] hexBytes, int expected) {
    int actual = HttpMessageUtil.hexBytesToInt(hexBytes);
    assertEquals(expected, actual);
  }

  private static Stream<Arguments> validHexBytesProvider() {
    return Stream.of(
        Arguments.of("00000000".getBytes(), 0),
        Arguments.of("00000001".getBytes(), 1),
        Arguments.of("000000FF".getBytes(), 255),
        Arguments.of("7FFFFFFF".getBytes(), 2147483647),
        Arguments.of("FFFFFFFF".getBytes(), -1),
        Arguments.of("abcc4501".getBytes(), -1412676351),
        Arguments.of("ABCC4501".getBytes(), -1412676351),
        Arguments.of("12345678".getBytes(), 305419896),
        Arguments.of("0".getBytes(), 0),
        Arguments.of("F".getBytes(), 15),
        Arguments.of("10".getBytes(), 16));
  }

  @ParameterizedTest
  @MethodSource("invalidHexBytesProvider")
  void test_hexBytesToInt_invalidInput(byte[] hexBytes) {
    assertThrows(
        IllegalArgumentException.class,
        () -> HttpMessageUtil.hexBytesToInt(hexBytes),
        "Invalid hex character");
  }

  private static Stream<Arguments> invalidHexBytesProvider() {
    return Stream.of(
        Arguments.of("G".getBytes()),
        Arguments.of("z".getBytes()),
        Arguments.of("!".getBytes()),
        Arguments.of("0x10".getBytes()));
  }

  /// copyBytes ///

  @Test
  void test_copyBytes_nullSource() {
    assertThrows(
        IllegalArgumentException.class,
        () -> HttpMessageUtil.copyBytes(null, 5),
        "Source bytes array is null!");
  }

  @Test
  void test_copyBytes_negativeTargetSize() {
    byte[] source = {1, 2, 3};
    assertThrows(
        IllegalArgumentException.class,
        () -> HttpMessageUtil.copyBytes(source, -1),
        "The size of the target array cannot be negative");
  }

  @Test
  void test_copyBytes_sourceLargerThanTarget() {
    byte[] source = {1, 2, 3};
    assertThrows(
        IllegalArgumentException.class,
        () -> HttpMessageUtil.copyBytes(source, 2),
        "The size of the source array exceeds the size of the target array");
  }

  @ParameterizedTest
  @MethodSource("validByteArraysProvider")
  void test_copyBytes_validInput(byte[] source, int targetSize, byte[] expected) {
    byte[] target = HttpMessageUtil.copyBytes(source, targetSize);
    assertArrayEquals(expected, target);
  }

  private static Stream<Arguments> validByteArraysProvider() {
    return Stream.of(
        Arguments.of(new byte[] {1, 2, 3}, 5, new byte[] {1, 2, 3, 0, 0}),
        Arguments.of(new byte[] {}, 2, new byte[] {0, 0}),
        Arguments.of(new byte[] {10, 20}, 2, new byte[] {10, 20}),
        Arguments.of(
            new byte[] {Byte.MAX_VALUE, Byte.MIN_VALUE},
            4,
            new byte[] {Byte.MAX_VALUE, Byte.MIN_VALUE, 0, 0}));
  }
}
