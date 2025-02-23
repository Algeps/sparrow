package ru.algeps.sparrow.message.request.domain.exceptions.http.clienterror;

import ru.algeps.sparrow.message.FieldName;
import ru.algeps.sparrow.message.HttpVersion;
import ru.algeps.sparrow.message.response.domain.HttpResponse;
import ru.algeps.sparrow.message.response.domain.HttpStatusCode;

public class RequestHeaderTooLargeHttp1_1RequestException extends ClientErrorHttpRequestException {
  private static final HttpResponse REQUEST_HEADER_FIELDS_TOO_LARGE_RESPONSE =
      HttpResponse.responseFor(HttpVersion.HTTP_1_1)
          .statusCode(HttpStatusCode.REQUEST_HEADER_FIELDS_TOO_LARGE)
          .addHeader(FieldName.CONNECTION.getName(), "close");

  public RequestHeaderTooLargeHttp1_1RequestException(String message) {
    super(REQUEST_HEADER_FIELDS_TOO_LARGE_RESPONSE, message);
  }

  public RequestHeaderTooLargeHttp1_1RequestException(Throwable throwable) {
    super(REQUEST_HEADER_FIELDS_TOO_LARGE_RESPONSE, throwable);
  }

  @Override
  public HttpResponse getHttpResponse() {
    return REQUEST_HEADER_FIELDS_TOO_LARGE_RESPONSE;
  }
}
