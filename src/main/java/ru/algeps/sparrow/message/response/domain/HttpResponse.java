package ru.algeps.sparrow.message.response.domain;

import com.sun.net.httpserver.Headers;
import java.nio.charset.Charset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import ru.algeps.sparrow.message.HttpVersion;
import ru.algeps.sparrow.message.request.domain.mediatype.MediaType;

public interface HttpResponse extends ByteBufferMessage {
  DateTimeFormatter HTTP_DATE_TIME_FORMAT =
      DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US);

  HttpResponse statusCode(HttpStatusCode httpStatusCode);

  HttpResponse addHeader(String name, String value);

  HttpResponse addHeaderForBigBody(long lengthBody, MediaType mediaType);

  Headers headers();

  HttpResponse addBody(byte[] bodyBytes, MediaType mediaType);

  HttpResponse addBody(byte[] bodyBytes, MediaType mediaType, Charset charset);

  static HttpResponse responseFor(HttpVersion httpVersion) {
    return switch (httpVersion) {
      case HTTP_1_1 -> new HttpResponse1_1();
    };
  }

  static <T extends HttpResponse> T responseFor(HttpVersion httpVersion, Class<T> clazz) {
    if (!httpVersion.getResponseHandlerClass().equals(clazz)) {
      throw new IllegalArgumentException();
    }
    return switch (httpVersion) {
      case HTTP_1_1 -> clazz.cast(new HttpResponse1_1());
    };
  }

  /** Копирует все данные данного запроса. */
  HttpResponse copy();

  String toString();
}
