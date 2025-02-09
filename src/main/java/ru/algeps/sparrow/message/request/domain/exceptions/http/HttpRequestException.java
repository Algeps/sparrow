package ru.algeps.sparrow.message.request.domain.exceptions.http;

import ru.algeps.sparrow.message.request.domain.exceptions.RequestException;
import ru.algeps.sparrow.message.response.domain.HttpResponse;

public abstract class HttpRequestException extends RequestException {
  protected final HttpResponse httpResponse;

  protected HttpRequestException(HttpResponse httpResponse, String message) {
    super(message);
    this.httpResponse = httpResponse;
  }

  public HttpRequestException(HttpResponse httpResponse, String message, Throwable cause) {
    super(message, cause);
    this.httpResponse = httpResponse;
  }

  protected HttpRequestException(HttpResponse httpResponse, Throwable throwable) {
    super(throwable);
    this.httpResponse = httpResponse;
  }

  public HttpResponse getHttpResponse() {
    return httpResponse;
  }
}
