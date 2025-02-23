package ru.algeps.sparrow.message.request.domain.exceptions.http.clienterror.auth;

import ru.algeps.sparrow.message.FieldName;
import ru.algeps.sparrow.message.HttpVersion;
import ru.algeps.sparrow.message.response.domain.HttpResponse;
import ru.algeps.sparrow.message.response.domain.HttpStatusCode;

public class ForbiddenHttp1_1RequestException extends AuthClientErrorHttpRequestException {
  private static final HttpResponse FORBIDDEN_RESPONSE =
      HttpResponse.responseFor(HttpVersion.HTTP_1_1)
          .statusCode(HttpStatusCode.FORBIDDEN)
          .addHeader(FieldName.CONNECTION.getName(), "close");

  public ForbiddenHttp1_1RequestException(String credential) {
    super(FORBIDDEN_RESPONSE, "Forbidden request for credential=[%s]".formatted(credential));
  }

  public ForbiddenHttp1_1RequestException(Throwable throwable) {
    super(FORBIDDEN_RESPONSE, throwable);
  }

  @Override
  public HttpResponse getHttpResponse() {
    return FORBIDDEN_RESPONSE;
  }
}
