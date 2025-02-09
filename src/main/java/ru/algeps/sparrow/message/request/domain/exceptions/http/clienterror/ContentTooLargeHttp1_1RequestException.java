package ru.algeps.sparrow.message.request.domain.exceptions.http.clienterror;

import ru.algeps.sparrow.message.FieldName;
import ru.algeps.sparrow.message.HttpVersion;
import ru.algeps.sparrow.message.response.domain.HttpResponse;
import ru.algeps.sparrow.message.response.domain.HttpStatusCode;

public class ContentTooLargeHttp1_1RequestException extends ClientErrorHttpRequestException {
  private static final HttpResponse PAYLOAD_TOO_LARGE_RESPONSE =
      HttpResponse.responseFor(HttpVersion.HTTP_1_1)
          .statusCode(HttpStatusCode.CONTENT_TOO_LARGE)
          .addHeader(FieldName.CONNECTION.getName(), "close");

  public ContentTooLargeHttp1_1RequestException(String message) {
    super(PAYLOAD_TOO_LARGE_RESPONSE, message);
  }

  @Override
  public HttpResponse getHttpResponse() {
    return PAYLOAD_TOO_LARGE_RESPONSE;
  }
}
