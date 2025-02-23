package ru.algeps.sparrow.message.request.parser.http.exceptions;

import java.io.IOException;

public class HttpRequestParserException extends IOException {
  public HttpRequestParserException(String message) {
    super(message);
  }
}
