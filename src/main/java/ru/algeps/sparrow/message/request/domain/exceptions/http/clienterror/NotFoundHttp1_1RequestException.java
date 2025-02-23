package ru.algeps.sparrow.message.request.domain.exceptions.http.clienterror;

import static ru.algeps.sparrow.context.Constants.NOT_FOUND_HTML_CONTENT;

import ru.algeps.sparrow.message.FieldName;
import ru.algeps.sparrow.message.HttpVersion;
import ru.algeps.sparrow.message.request.domain.mediatype.MediaType;
import ru.algeps.sparrow.message.response.domain.HttpResponse;
import ru.algeps.sparrow.message.response.domain.HttpStatusCode;

public class NotFoundHttp1_1RequestException extends ClientErrorHttpRequestException {
  private static final HttpResponse NOT_FOUND_RESPONSE =
      HttpResponse.responseFor(HttpVersion.HTTP_1_1)
          .statusCode(HttpStatusCode.NOT_FOUND)
          .addBody(NOT_FOUND_HTML_CONTENT, MediaType.Text.HTML);

  public NotFoundHttp1_1RequestException(String message) {
    super(NOT_FOUND_RESPONSE, message);
  }

  public NotFoundHttp1_1RequestException(Throwable throwable) {
    super(NOT_FOUND_RESPONSE, throwable);
  }

  @Override
  public HttpResponse getHttpResponse() {
    return NOT_FOUND_RESPONSE;
  }
}
