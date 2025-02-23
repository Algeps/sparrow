package ru.algeps.sparrow.message.response.domain;

import static ru.algeps.sparrow.message.request.parser.http.AsciiAlphabet.*;
import static ru.algeps.sparrow.message.util.HttpMessageUtil.NEW_LINE;

import com.sun.net.httpserver.Headers;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import ru.algeps.sparrow.context.Constants;
import ru.algeps.sparrow.message.HttpVersion;
import ru.algeps.sparrow.message.request.domain.mediatype.MediaType;
import ru.algeps.sparrow.message.util.HttpMessageUtil;

public class HttpResponse1_1 implements HttpResponse {
  private static final HttpVersion HTTP_VERSION = HttpVersion.HTTP_1_1;
  private static final byte[] HTTP_VERSION_BYTES_REPRESENTATION =
      HTTP_VERSION.getVersion().getBytes();

  //
  /** Transfer-Encoding, передаваемый "как есть" - значение по-умолчанию. */
  private static final Set<TransferEncoding> DEFAULT_TRANSFER_ENCODING =
      EnumSet.of(TransferEncoding.IDENTITY);

  private static final Set<TransferEncoding> CHUNKED_TRANSFER_ENCODING =
      EnumSet.of(TransferEncoding.CHUNKED);
  private static final byte[] EMPTY_CHUNK = HttpMessageUtil.EMPTY_CHUNK;
  //
  private HttpStatusCode httpStatusCode;
  private final Headers headers;
  private byte[] bodyBytes;
  private Headers trailers;
  //
  private Set<TransferEncoding> transferEncoding = DEFAULT_TRANSFER_ENCODING;

  public HttpResponse1_1() {
    this.headers = new Headers();
    this.headers.add("Server", Constants.SERVER_NAME);
    this.httpStatusCode = HttpStatusCode.OK;
  }

  private HttpResponse1_1(HttpResponse1_1 httpResponse) {
    if (httpResponse == null) {
      throw new IllegalArgumentException("Argument in copy constructor must not be null!");
    }

    this.httpStatusCode = httpResponse.httpStatusCode;

    this.headers = new Headers();
    httpResponse
        .headers()
        .forEach(
            (nameHeader, valueHeaderList) ->
                valueHeaderList.forEach(valueHeader -> this.headers.add(nameHeader, valueHeader)));

    if (httpResponse.trailers != null) {
      this.trailers = new Headers();
      httpResponse.trailers.forEach(
          (nameHeader, valueHeaderList) ->
              valueHeaderList.forEach(valueHeader -> this.trailers.add(nameHeader, valueHeader)));
    }

    this.transferEncoding = new HashSet<>(httpResponse.transferEncoding);

    if (httpResponse.bodyBytes != null) {
      this.bodyBytes = Arrays.copyOf(httpResponse.bodyBytes, httpResponse.bodyBytes.length);
    }
  }

  @Override
  public HttpResponse1_1 statusCode(HttpStatusCode httpStatusCode) {
    this.httpStatusCode = httpStatusCode;
    return this;
  }

  @Override
  public HttpResponse1_1 addHeader(String name, String value) {
    this.headers.add(name, value);
    return this;
  }

  @Override
  public HttpResponse1_1 addHeaderForBigBody(long lengthBody, MediaType mediaType) {
    this.headers.put("Content-Length", List.of(String.valueOf(lengthBody)));
    this.headers.put("Content-Type", List.of(mediaType.getContentType()));
    return this;
  }

  @Override
  public HttpResponse1_1 addBody(byte[] bodyBytes, MediaType mediaType) {
    this.addBody(bodyBytes, mediaType, StandardCharsets.UTF_8);
    return this;
  }

  @Override
  public HttpResponse1_1 addBody(byte[] bodyBytes, MediaType mediaType, Charset charset) {
    if (this.bodyBytes == null) {
      this.bodyBytes = bodyBytes;
      this.headers.add("Content-Length", String.valueOf(bodyBytes.length));

      if (mediaType == null) {
        mediaType = MediaType.Application.OCTET_STREAM;
      }
      String contentType = mediaType.getContentType();
      if (charset != null) {
        contentType += "; charset=" + charset.name().toLowerCase();
      }
      this.headers.add("Content-Type", contentType);
    } else if (transferEncoding == CHUNKED_TRANSFER_ENCODING
        || transferEncoding.contains(TransferEncoding.CHUNKED)) {
      addChunk(bodyBytes);
    }

    // todo если тело не пустое, то преобразовать в чанки

    return this;
  }

  /** Добавить чанк определённого размера */
  public HttpResponse1_1 addChunk(byte[] chunkData) {
    if (this.bodyBytes == null) {
      this.bodyBytes = HttpMessageUtil.packChunkToBytesArray(chunkData);
      addChunkedTransferEncoding();
    } else if (transferEncoding == CHUNKED_TRANSFER_ENCODING
        || transferEncoding.contains(TransferEncoding.CHUNKED)) {
      this.bodyBytes = HttpMessageUtil.concatWithNewChunk(this.bodyBytes, chunkData);
    } else {
      this.bodyBytes = HttpMessageUtil.packChunkToBytesArray(this.bodyBytes);
      this.bodyBytes = HttpMessageUtil.concatWithNewChunk(this.bodyBytes, chunkData);
    }

    // todo нужно учитывать, если имеется сжатие
    return this;
  }

  /** Заменить тело на указанный чанк */
  public HttpResponse1_1 replaceChunk(byte[] chunkData) {
    this.bodyBytes = HttpMessageUtil.packChunkToBytesArray(chunkData);
    addChunkedTransferEncoding();
    return this;
  }

  @Override
  public Headers headers() {
    return new Headers(headers);
  }

  public HttpResponse1_1 addTrailer(String name, String value) {
    if (this.trailers == null) {
      this.trailers = new Headers();
    }
    addChunkedTransferEncoding();
    this.headers.add("Trailer", name);
    this.trailers.add(name, value);
    return this;
  }

  private void addChunkedTransferEncoding() {
    if (this.transferEncoding == DEFAULT_TRANSFER_ENCODING) {
      this.transferEncoding = CHUNKED_TRANSFER_ENCODING;
      this.headers.add("Transfer-Encoding", TransferEncoding.CHUNKED.getName());
      transformToChunk();
    } else if (this.transferEncoding != CHUNKED_TRANSFER_ENCODING
        || !this.transferEncoding.contains(TransferEncoding.CHUNKED)) {
      this.transferEncoding.add(TransferEncoding.CHUNKED);
      this.headers.add("Transfer-Encoding", TransferEncoding.CHUNKED.getName());
      transformToChunk();
    }
    this.headers.remove("Content-Length");
  }

  private void transformToChunk() {
    if (this.bodyBytes != null) {
      replaceChunk(this.bodyBytes);
    }
  }

  public Headers trailers() {
    if (this.trailers == null) {
      return HttpMessageUtil.EMPTY_HEADERS;
    }
    return this.trailers;
  }

  @Override
  public ByteBuffer toByteBuffer() {
    headers.add("Date", LocalDateTime.now().format(HTTP_DATE_TIME_FORMAT));

    byte[] httpVersionBytes = HTTP_VERSION_BYTES_REPRESENTATION;
    byte[] statusCodeBytes = httpStatusCode.asString().getBytes();
    byte[] headersBytes = headersToStringLines(this.headers).getBytes(StandardCharsets.US_ASCII);

    // todo реализовать отправку сжатием
    // todo добавить метод отправки без указания размера содержимого (unknown) - и отпралвять
    // чанками

    if (transferEncoding == CHUNKED_TRANSFER_ENCODING
        || transferEncoding.contains(TransferEncoding.CHUNKED)) {
      byte[] trailersBytes =
          headersToStringLines(this.trailers).getBytes(StandardCharsets.US_ASCII);

      boolean isEmptyChunksData =
          this.bodyBytes == null || this.bodyBytes.length == EMPTY_CHUNK.length;

      int sizeByteBuffer =
          httpVersionBytes.length
              + 1
              + statusCodeBytes.length
              + NEW_LINE.length
              + headersBytes.length
              + NEW_LINE.length
              + (isEmptyChunksData ? 0 : bodyBytes.length)
              + EMPTY_CHUNK.length
              + trailersBytes.length
              + (trailersBytes.length != 0 ? NEW_LINE.length : 0);

      ByteBuffer byteBuffer =
          ByteBuffer.allocate(sizeByteBuffer)
              .put(httpVersionBytes)
              .put(SPACE)
              .put(statusCodeBytes)
              .put(NEW_LINE)
              .put(headersBytes)
              .put(NEW_LINE);
      if (!isEmptyChunksData) {
        byteBuffer.put(bodyBytes);
      }
      // добавление завершающего чанка
      byteBuffer.put(EMPTY_CHUNK);
      if (trailersBytes.length != 0) {
        byteBuffer.put(trailersBytes);
        byteBuffer.put(NEW_LINE);
      }

      return byteBuffer.rewind();
    }

    int sizeByteBuffer =
        httpVersionBytes.length
            + 1
            + statusCodeBytes.length
            + NEW_LINE.length
            + headersBytes.length
            + NEW_LINE.length
            + (bodyBytes == null ? 0 : bodyBytes.length);
    ByteBuffer byteBuffer =
        ByteBuffer.allocate(sizeByteBuffer)
            .put(httpVersionBytes)
            .put(SPACE)
            .put(statusCodeBytes)
            .put(NEW_LINE)
            .put(headersBytes)
            .put(NEW_LINE);
    if (bodyBytes != null) {
      byteBuffer.put(bodyBytes);
    }
    return byteBuffer.rewind();
  }

  private static String headersToStringLines(Headers headers) {
    if (headers == null || headers.isEmpty()) {
      return "";
    }

    StringBuilder stringBuilder = new StringBuilder(100);

    headers.forEach(
        (k, v) -> {
          stringBuilder.append(k).append(":");

          stringBuilder.append(v.getFirst());
          for (int i = 1; i < v.size(); i++) {
            stringBuilder.append((char) COMMA);
            stringBuilder.append(v.get(i));
          }

          stringBuilder.append((char) CARRIAGE_RETURN).append((char) LINE_FEED);
        });

    return stringBuilder.toString();
  }

  @Override
  public HttpResponse1_1 copy() {
    return new HttpResponse1_1(this);
  }

  @Override
  public String toString() {
    return "HttpResponse1_1{"
        + "httpStatusCode="
        + httpStatusCode
        + ", headers="
        + headers
        + ", bodyBytes="
        + Arrays.toString(bodyBytes)
        + ", nameTrailers="
        + trailers
        + '}';
  }

  public static class HttpResponseException extends IOException {
    public HttpResponseException(String message) {
      super(message);
    }
  }
}
