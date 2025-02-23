package ru.algeps.sparrow.message.request.domain.exceptions.http.clienterror;

import ru.algeps.sparrow.message.FieldName;
import ru.algeps.sparrow.message.HttpVersion;
import ru.algeps.sparrow.message.response.domain.HttpResponse;
import ru.algeps.sparrow.message.response.domain.HttpStatusCode;

public class UriTooLongHttp1_1RequestException extends ClientErrorHttpRequestException {
  private static final HttpResponse URI_TOO_LONG_RESPONSE =
      HttpResponse.responseFor(HttpVersion.HTTP_1_1)
          .statusCode(HttpStatusCode.URI_TOO_LONG)
          .addHeader(FieldName.CONNECTION.getName(), "close");

  public UriTooLongHttp1_1RequestException(String message) {
    super(URI_TOO_LONG_RESPONSE, message);
  }

  public UriTooLongHttp1_1RequestException(Throwable throwable) {
    super(URI_TOO_LONG_RESPONSE, throwable);
  }

  @Override
  public HttpResponse getHttpResponse() {
    return URI_TOO_LONG_RESPONSE;
  }
}
