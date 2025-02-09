package ru.algeps.sparrow.request.parser;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.code_intelligence.jazzer.junit.FuzzTest;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import ru.algeps.sparrow.message.HttpVersion;
import ru.algeps.sparrow.message.request.domain.HttpRequest;
import ru.algeps.sparrow.message.request.domain.exceptions.http.clienterror.BadRequestHttp1_1RequestException;
import ru.algeps.sparrow.message.request.domain.exceptions.http.clienterror.ContentTooLargeHttp1_1RequestException;
import ru.algeps.sparrow.message.request.domain.exceptions.http.clienterror.MethodNotAllowedHttp1_1RequestException;
import ru.algeps.sparrow.message.request.domain.exceptions.http.clienterror.RequestHeaderTooLargeHttp1_1RequestException;
import ru.algeps.sparrow.message.request.domain.exceptions.http.clienterror.UriTooLongHttp1_1RequestException;
import ru.algeps.sparrow.message.request.domain.exceptions.http.servererror.HttpVersionNotSupportedHttp1_1RequestException;
import ru.algeps.sparrow.message.request.parser.http.HttpRequestParser1_1;
import ru.algeps.sparrow.test_utils.TestUtil;

class HttpRequestParser1_1FuzzTest {

  @FuzzTest(maxDuration = "15m")
  void fuzzTest(byte[] fuzzedBytes) {
    try {
      ReadableByteChannel readableByteChannel = new ReadableByteChannelImpl(fuzzedBytes);
      HttpRequest httpRequest =
          HttpRequestParser1_1.connect(readableByteChannel, HttpVersion.HTTP_1_1).getHttpRequest();

      assertNotNull(httpRequest.httpMethod());
      assertNotNull(httpRequest.uri());
      assertNotNull(httpRequest.version());
      assertNotNull(httpRequest.headers());
    } catch (Exception e) {
      assertTrue(
          e instanceof BadRequestHttp1_1RequestException
              || e instanceof MethodNotAllowedHttp1_1RequestException
              || e instanceof UriTooLongHttp1_1RequestException
              || e instanceof HttpVersionNotSupportedHttp1_1RequestException
              || e instanceof RequestHeaderTooLargeHttp1_1RequestException
              || e instanceof ContentTooLargeHttp1_1RequestException
              || e.getMessage().startsWith("Data is empty!"),
          TestUtil.getFullMessage(e));
    }
  }

  static class ReadableByteChannelImpl implements ReadableByteChannel {
    private final ByteBuffer byteBuffer;
    private boolean open = true;

    public ReadableByteChannelImpl(byte[] bytes) {
      this.byteBuffer = ByteBuffer.wrap(bytes);
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
      if (!isOpen()) {
        throw new IOException("Channel is closed");
      }

      if (!byteBuffer.hasRemaining()) {
        return -1; // End of stream
      }

      int bytesToRead = Math.min(dst.remaining(), byteBuffer.remaining());
      byteBuffer.limit(byteBuffer.position() + bytesToRead);
      dst.put(byteBuffer);
      byteBuffer.limit(byteBuffer.capacity());

      return bytesToRead;
    }

    @Override
    public boolean isOpen() {
      return open;
    }

    @Override
    public void close() {
      open = false;
    }
  }
}
