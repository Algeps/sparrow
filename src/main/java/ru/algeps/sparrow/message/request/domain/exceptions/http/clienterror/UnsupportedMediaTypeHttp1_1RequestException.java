package ru.algeps.sparrow.message.request.domain.exceptions.http.clienterror;

import ru.algeps.sparrow.message.FieldName;
import ru.algeps.sparrow.message.HttpVersion;
import ru.algeps.sparrow.message.response.domain.HttpResponse;
import ru.algeps.sparrow.message.response.domain.HttpStatusCode;

public class UnsupportedMediaTypeHttp1_1RequestException extends ClientErrorHttpRequestException {
  private static final HttpResponse UNSUPPORTED_MEDIA_TYPE_RESPONSE =
      HttpResponse.responseFor(HttpVersion.HTTP_1_1)
          .statusCode(HttpStatusCode.UNSUPPORTED_MEDIA_TYPE)
          .addHeader(FieldName.CONNECTION.getName(), "close");

  public UnsupportedMediaTypeHttp1_1RequestException(String message) {
    super(UNSUPPORTED_MEDIA_TYPE_RESPONSE, message);
  }

  @Override
  public HttpResponse getHttpResponse() {
    return UNSUPPORTED_MEDIA_TYPE_RESPONSE;
  }
}
