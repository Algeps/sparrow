package ru.algeps.sparrow.message.request.domain.exceptions.http.servererror;

import ru.algeps.sparrow.message.FieldName;
import ru.algeps.sparrow.message.HttpVersion;
import ru.algeps.sparrow.message.response.domain.HttpResponse;
import ru.algeps.sparrow.message.response.domain.HttpStatusCode;

public class HttpVersionNotSupportedHttp1_1RequestException
    extends ServerErrorHttpRequestException {
  private static final HttpResponse HTTP_VERSION_NOT_SUPPORTED_RESPONSE =
      HttpResponse.responseFor(HttpVersion.HTTP_1_1)
          .statusCode(HttpStatusCode.HTTP_VERSION_NOT_SUPPORTED)
          .addHeader(FieldName.CONNECTION.getName(), "close");

  public HttpVersionNotSupportedHttp1_1RequestException(String message) {
    super(HTTP_VERSION_NOT_SUPPORTED_RESPONSE, message);
  }

  @Override
  public HttpResponse getHttpResponse() {
    return HTTP_VERSION_NOT_SUPPORTED_RESPONSE;
  }
}
