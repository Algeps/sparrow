package ru.algeps.sparrow.message.request.domain.exceptions.http.clienterror.auth;

import ru.algeps.sparrow.message.request.domain.exceptions.http.clienterror.ClientErrorHttpRequestException;
import ru.algeps.sparrow.message.response.domain.HttpResponse;

public abstract class AuthClientErrorHttpRequestException extends ClientErrorHttpRequestException {
  protected AuthClientErrorHttpRequestException(HttpResponse httpResponse, String message) {
    super(httpResponse, message);
  }

  protected AuthClientErrorHttpRequestException(HttpResponse httpResponse, Throwable throwable) {
    super(httpResponse, throwable);
  }
}
