package ru.algeps.sparrow.message.request.domain.exceptions.http.clienterror.auth;

import ru.algeps.sparrow.message.HttpVersion;
import ru.algeps.sparrow.message.response.domain.HttpResponse;
import ru.algeps.sparrow.message.response.domain.HttpStatusCode;

public class UnauthorizedHttp1_1RequestException extends AuthClientErrorHttpRequestException {
  private static final String NAME_HEADER = "WWW-Authenticate";
  private static final HttpResponse UNAUTHORIZED_RESPONSE =
      HttpResponse.responseFor(HttpVersion.HTTP_1_1).statusCode(HttpStatusCode.UNAUTHORIZED);

  public UnauthorizedHttp1_1RequestException(String typeScheme, String realm) {
    super(
        UNAUTHORIZED_RESPONSE
            .copy()
            .addHeader(NAME_HEADER, "%s realm=\"%s\"".formatted(typeScheme, realm).intern()),
        "Unauthorized request");
  }

  public UnauthorizedHttp1_1RequestException(Throwable throwable) {
    super(UNAUTHORIZED_RESPONSE, throwable);
  }
}
