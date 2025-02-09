package ru.algeps.sparrow.worker.requestrouter.requestmatcher.impl;

import java.util.EnumMap;
import ru.algeps.sparrow.message.request.domain.HttpMethod;
import ru.algeps.sparrow.message.request.domain.HttpRequest;
import ru.algeps.sparrow.worker.handler.http.HttpRequestHandler;
import ru.algeps.sparrow.worker.requestrouter.requestmatcher.HttpRequestMatcher;

public class HttpRequestMatcherImpl implements HttpRequestMatcher {
  private final EnumMap<HttpMethod, HttpRequestHandler> httpRequestHandlers;

  public HttpRequestMatcherImpl() {
    this.httpRequestHandlers = new EnumMap<>(HttpMethod.class);
  }

  @Override
  public void insertHttpRequestHandler(HttpRequestHandler httpRequestHandler) {
    httpRequestHandlers.put(httpRequestHandler.getHttpMethodHandle(), httpRequestHandler);
  }

  /** Если есть сопоставление, то ОК. */
  @Override
  public HttpRequestHandler httpMatch(HttpRequest httpRequest) {
    return httpRequestHandlers.get(httpRequest.httpMethod());
  }
}
