package ru.algeps.sparrow.message.request.domain;

import com.sun.net.httpserver.Headers;
import java.net.URI;
import java.util.*;

import ru.algeps.sparrow.message.HttpVersion;
import ru.algeps.sparrow.message.request.domain.exceptions.http.HttpRequestException;
import ru.algeps.sparrow.message.request.domain.exceptions.http.clienterror.BadRequestHttp1_1RequestException;

public record HttpRequest1_1(
    HttpMethod httpMethod,
    URI uri,
    HttpVersion version,
    Headers headers,
    byte[] body,
    Headers trailers)
    implements HttpRequest {

  public static Builder1_1 builder() {
    return new Builder1_1();
  }

  public static class Builder1_1 implements HttpRequest.Builder {
    private static final Set<String> INDIVISIBLE_HEADER =
        Set.of("User-Agent", "Content-Type", "Content-Length", "Authorization", "Accept-Language");

    private HttpMethod httpMethod;
    private URI uri;
    private HttpVersion version;
    private Headers headers;
    private byte[] body;
    private Headers trailers;

    public Builder1_1() {
      clear();
    }

    @Override
    public Builder1_1 httpMethod(HttpMethod httpMethod) {
      this.httpMethod = httpMethod;
      return this;
    }

    @Override
    public Builder1_1 uri(URI uri) {
      this.uri = uri;
      return this;
    }

    @Override
    public Builder1_1 version(HttpVersion version) {
      this.version = version;
      return this;
    }

    @Override
    public Builder1_1 headers(Headers headers) {
      this.headers.putAll(headers);
      return this;
    }

    /** Конкатенирует заголовки, которые не должны быть делимыми. */
    @Override
    public Builder1_1 header(String name, String value) {
      List<String> valueList = this.headers.get(name);
      if (valueList != null && INDIVISIBLE_HEADER.contains(name)) {
        valueList.set(0, valueList.getFirst() + "," + value);
      } else {
        this.headers.add(name, value);
      }

      return this;
    }

    @Override
    public Builder1_1 body(byte[] body) {
      this.body = body;
      return this;
    }

    public Builder1_1 trailers(Headers headers) {
      if (this.trailers == null) {
        this.trailers = new Headers(HashMap.newHashMap(1));
      }
      this.trailers.putAll(headers);
      return this;
    }

    public Builder1_1 trailer(String name, String value) {
      if (this.trailers == null) {
        this.trailers = new Headers(HashMap.newHashMap(1));
      }

      this.trailers.add(name, value);

      return this;
    }

    @Override
    public HttpRequest1_1 build() throws HttpRequestException {
      if (httpMethod == null) {
        throw new BadRequestHttp1_1RequestException("Not contains http method!");
      }

      if (this.headers.isEmpty()) {
        throw new BadRequestHttp1_1RequestException(
            "There are no headers! Request must contain at least the heading \"Host\"!");
      }

      if (!this.headers.containsKey("host") || !this.headers.containsKey("Host")) {
        throw new BadRequestHttp1_1RequestException("Unknown header 'host' in header");
      }

      // todo нужно сделать быструю верификацию полей!
      if (!headers.containsKey("User-Agent")) {
        throw new BadRequestHttp1_1RequestException("Unknown header 'User-Agent' in header");
      }

      return new HttpRequest1_1(httpMethod, uri, version, headers, body, trailers);
    }

    @Override
    public Builder1_1 copy() {
      // todo реализовать позднее
      return this;
    }

    @Override
    public Builder1_1 clear() {
      this.httpMethod = null;
      this.uri = null;
      this.version = null;
      this.headers = new Headers(HashMap.newHashMap(5));
      this.body = null;
      this.trailers = null;
      return this;
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof HttpRequest1_1 that)) return false;
    return httpMethod == that.httpMethod
        && Objects.equals(uri, that.uri)
        && version == that.version
        && Objects.equals(headers, that.headers)
        && Arrays.equals(body, that.body);
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(httpMethod, uri, version, headers);
    result = 31 * result + Arrays.hashCode(body);
    return result;
  }

  @Override
  public String toString() {
    return "HttpRequest1_1{"
        + "httpMethod="
        + httpMethod
        + ", uri="
        + uri
        + ", version="
        + version
        + ", headers="
        + headers
        + ", body="
        + Arrays.toString(body)
        + ", nameTrailers="
        + trailers
        + '}';
  }
}
