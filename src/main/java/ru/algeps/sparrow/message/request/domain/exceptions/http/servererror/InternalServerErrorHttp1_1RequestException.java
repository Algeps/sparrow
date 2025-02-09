package ru.algeps.sparrow.message.request.domain.exceptions.http.servererror;

import ru.algeps.sparrow.message.FieldName;
import ru.algeps.sparrow.message.HttpVersion;
import ru.algeps.sparrow.message.response.domain.HttpResponse;
import ru.algeps.sparrow.message.response.domain.HttpStatusCode;

public class InternalServerErrorHttp1_1RequestException extends ServerErrorHttpRequestException {
  private static final HttpResponse INTERNAL_SERVER_ERROR_RESPONSE =
      HttpResponse.responseFor(HttpVersion.HTTP_1_1)
          .statusCode(HttpStatusCode.INTERNAL_SERVER_ERROR)
          .addHeader(FieldName.CONNECTION.getName(), "close");

  public InternalServerErrorHttp1_1RequestException(String message) {
    super(INTERNAL_SERVER_ERROR_RESPONSE, message);
  }

  @Override
  public HttpResponse getHttpResponse() {
    return INTERNAL_SERVER_ERROR_RESPONSE;
  }
}
