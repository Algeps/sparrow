package ru.algeps.sparrow.message.request.domain.exceptions.http.clienterror;

import ru.algeps.sparrow.message.FieldName;
import ru.algeps.sparrow.message.HttpVersion;
import ru.algeps.sparrow.message.response.domain.HttpResponse;
import ru.algeps.sparrow.message.response.domain.HttpStatusCode;

public class LengthRequiredHttp1_1RequestException extends ClientErrorHttpRequestException {
  private static final HttpResponse LENGTH_REQUIRED_RESPONSE =
      HttpResponse.responseFor(HttpVersion.HTTP_1_1)
          .statusCode(HttpStatusCode.LENGTH_REQUIRED)
          .addHeader(FieldName.CONNECTION.getName(), "close");

  public LengthRequiredHttp1_1RequestException(String message) {
    super(LENGTH_REQUIRED_RESPONSE, message);
  }

  @Override
  public HttpResponse getHttpResponse() {
    return LENGTH_REQUIRED_RESPONSE;
  }
}
