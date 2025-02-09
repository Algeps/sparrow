package ru.algeps.sparrow.message.request.domain.exceptions.http.clienterror;

import ru.algeps.sparrow.message.FieldName;
import ru.algeps.sparrow.message.HttpVersion;
import ru.algeps.sparrow.message.response.domain.HttpResponse;
import ru.algeps.sparrow.message.response.domain.HttpStatusCode;

public class BadRequestHttp1_1RequestException extends ClientErrorHttpRequestException {
  private static final HttpResponse BAD_REQUEST_RESPONSE =
      HttpResponse.responseFor(HttpVersion.HTTP_1_1)
          .statusCode(HttpStatusCode.BAD_REQUEST)
          .addHeader(FieldName.CONNECTION.getName(), "close");

  public BadRequestHttp1_1RequestException(String message) {
    super(BAD_REQUEST_RESPONSE, message);
  }

  public BadRequestHttp1_1RequestException(Throwable throwable) {
    super(BAD_REQUEST_RESPONSE, throwable);
  }

  public BadRequestHttp1_1RequestException(String message, Throwable cause) {
    super(BAD_REQUEST_RESPONSE, message, cause);
  }

  @Override
  public HttpResponse getHttpResponse() {
    return BAD_REQUEST_RESPONSE;
  }
}
