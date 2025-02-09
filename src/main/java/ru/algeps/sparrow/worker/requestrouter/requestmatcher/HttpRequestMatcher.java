package ru.algeps.sparrow.worker.requestrouter.requestmatcher;

import ru.algeps.sparrow.message.request.Request;
import ru.algeps.sparrow.message.request.domain.HttpRequest;
import ru.algeps.sparrow.worker.handler.RequestHandler;
import ru.algeps.sparrow.worker.handler.http.HttpRequestHandler;

public interface HttpRequestMatcher extends RequestMatcher {
  @Override
  default void insertRequestHandler(RequestHandler requestHandler) {
    this.insertHttpRequestHandler((HttpRequestHandler) requestHandler);
  }

  void insertHttpRequestHandler(HttpRequestHandler httpRequestHandler);

  @Override
  default RequestHandler match(Request request) {
    return this.httpMatch((HttpRequest) request);
  }

  HttpRequestHandler httpMatch(HttpRequest httpRequest);
}
