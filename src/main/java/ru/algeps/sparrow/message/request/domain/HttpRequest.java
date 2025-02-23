package ru.algeps.sparrow.message.request.domain;

import com.sun.net.httpserver.Headers;
import java.net.URI;

import ru.algeps.sparrow.message.HttpVersion;
import ru.algeps.sparrow.message.request.Request;
import ru.algeps.sparrow.message.request.domain.exceptions.http.HttpRequestException;
import ru.algeps.sparrow.message.request.domain.exceptions.http.servererror.HttpVersionNotSupportedHttp1_1RequestException;

public interface HttpRequest extends Request {
  HttpMethod httpMethod();

  URI uri();

  HttpVersion version();

  Headers headers();

  byte[] body();

  String toString();

  static HttpRequest.Builder newBuilder(HttpVersion httpVersion)
      throws HttpVersionNotSupportedHttp1_1RequestException {
    return switch (httpVersion) {
      case HTTP_1_1 -> HttpRequest1_1.builder();
      case null -> throw new HttpVersionNotSupportedHttp1_1RequestException("");
    };
  }

  static <T extends HttpRequest.Builder> T newBuilder(HttpVersion httpVersion, Class<T> clazz)
      throws HttpVersionNotSupportedHttp1_1RequestException {
    if (!httpVersion.getRequestBuilderClass().equals(clazz)) {
      throw new IllegalArgumentException();
    }

    return switch (httpVersion) {
      case HTTP_1_1 -> clazz.cast(HttpRequest1_1.builder());
      case null ->
          throw new HttpVersionNotSupportedHttp1_1RequestException(
              "Not supported Http version: " + httpVersion);
    };
  }

  interface Builder {
    Builder httpMethod(HttpMethod httpMethod);

    Builder uri(URI uri);

    Builder version(HttpVersion version);

    Builder headers(Headers headers);

    Builder header(String name, String value);

    Builder body(byte[] body);

    HttpRequest build() throws HttpRequestException;

    Builder copy();

    Builder clear();
  }
}
