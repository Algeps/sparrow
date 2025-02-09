package ru.algeps.sparrow.message.request.parser.http.exceptions;

public class HttpRequestParserInvalidMethodException extends HttpRequestParserException {
  public HttpRequestParserInvalidMethodException() {
    super("Cannot parse http method");
  }
}
