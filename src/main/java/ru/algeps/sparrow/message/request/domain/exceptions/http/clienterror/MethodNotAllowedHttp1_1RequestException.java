package ru.algeps.sparrow.message.request.domain.exceptions.http.clienterror;

import ru.algeps.sparrow.message.FieldName;
import ru.algeps.sparrow.message.HttpVersion;
import ru.algeps.sparrow.message.response.domain.HttpResponse;
import ru.algeps.sparrow.message.response.domain.HttpStatusCode;

public class MethodNotAllowedHttp1_1RequestException extends ClientErrorHttpRequestException {

  private static final HttpResponse METHOD_NOT_ALLOWED_RESPONSE =
      HttpResponse.responseFor(HttpVersion.HTTP_1_1)
          .statusCode(HttpStatusCode.METHOD_NOT_ALLOWED)
          .addHeader(FieldName.CONNECTION.getName(), "close");

  public MethodNotAllowedHttp1_1RequestException(String message) {
    super(METHOD_NOT_ALLOWED_RESPONSE, message);
  }

  public MethodNotAllowedHttp1_1RequestException(Throwable throwable) {
    super(METHOD_NOT_ALLOWED_RESPONSE, throwable);
  }

  @Override
  public HttpResponse getHttpResponse() {
    return METHOD_NOT_ALLOWED_RESPONSE;
  }
}
