package ru.algeps.sparrow.message.request.parser.http.exceptions;

public class HttpRequestParserInvalidVersionException extends HttpRequestParserException{
    public HttpRequestParserInvalidVersionException() {
        super("Cannot parseAndLoadConfig HTTP version");
    }
}
