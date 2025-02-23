package ru.algeps.sparrow.request.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static ru.algeps.sparrow.test_utils.TestUtil.durationFunction;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.algeps.sparrow.test_utils.TestUtil;
import ru.algeps.sparrow.message.request.parser.http.ReadableByteConnectionChannel;

class ReadableByteConnectionChannelTest {
  protected final Logger log = LoggerFactory.getLogger(ReadableByteConnectionChannelTest.class);
  final int messageLength = 1_000;
  String message;

  @BeforeEach
  void beforeEach() {
    message = TestUtil.createRandomLatinString(messageLength);
  }

  @Test
  void readTestChannel() throws IOException {
    ByteArrayInputStream byteArrayInputStream =
        new ByteArrayInputStream(message.getBytes(StandardCharsets.UTF_8));
    ReadableByteChannel readableByteChannel = Channels.newChannel(byteArrayInputStream);
    ReadableByteConnectionChannel channel = new ReadableByteConnectionChannel(readableByteChannel);

    TestUtil.DurationFunction<String> durationFunction =
        durationFunction(() -> getStringFromChannel(channel));

    assertEquals(message, durationFunction.getResult());
    log.info("Duration channel:{}", durationFunction);
  }

  private static String getStringFromChannel(ReadableByteConnectionChannel channel) {
    StringBuilder result = new StringBuilder();
    try {
      while (channel.hasRemaining()) {
        byte b = channel.readByte();
        result.append((char) b);
      }
    } catch (ReadableByteConnectionChannel.ReadableByteConnectionChannelException ignored) {
    }
    return result.toString();
  }

  @Test
  void testInputStream() {
    InputStream inputStream = new ByteArrayInputStream(message.getBytes(StandardCharsets.UTF_8));

    TestUtil.DurationFunction<String> durationFunction =
        durationFunction(() -> getStringFromInputStream(inputStream));

    assertEquals(message, durationFunction.getResult());
    log.info("Duration inputStream:{}", durationFunction);
  }

  private static String getStringFromInputStream(InputStream inputStream) {
    StringBuilder sb = new StringBuilder();
    try {
      int c;
      while ((c = inputStream.read()) != -1) {
        sb.appendCodePoint(c);
      }
    } catch (IOException ignored) {
    }
    return sb.toString();
  }
}
