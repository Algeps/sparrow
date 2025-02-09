package ru.algeps.sparrow.message.request.domain.exceptions.http.clienterror;

import ru.algeps.sparrow.message.request.domain.exceptions.http.HttpRequestException;
import ru.algeps.sparrow.message.response.domain.HttpResponse;

public abstract class ClientErrorHttpRequestException extends HttpRequestException {
  protected ClientErrorHttpRequestException(HttpResponse httpResponse, String message) {
    super(httpResponse, message);
  }

  protected ClientErrorHttpRequestException(HttpResponse httpResponse, Throwable throwable) {
    super(httpResponse, throwable);
  }

  public ClientErrorHttpRequestException(
      HttpResponse httpResponse, String message, Throwable cause) {
    super(httpResponse, message, cause);
  }
}
