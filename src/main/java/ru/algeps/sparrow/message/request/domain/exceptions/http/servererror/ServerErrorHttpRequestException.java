package ru.algeps.sparrow.message.request.domain.exceptions.http.servererror;

import ru.algeps.sparrow.message.request.domain.exceptions.http.HttpRequestException;
import ru.algeps.sparrow.message.response.domain.HttpResponse;

public abstract class ServerErrorHttpRequestException extends HttpRequestException {
  protected ServerErrorHttpRequestException(HttpResponse httpResponse, String message) {
    super(httpResponse, message);
  }

  protected ServerErrorHttpRequestException(HttpResponse httpResponse, Throwable throwable) {
    super(httpResponse, throwable);
  }
}
