package ru.algeps.sparrow.worker.requestfilter.http;

import ru.algeps.sparrow.message.request.Request;
import ru.algeps.sparrow.message.request.domain.HttpMethod;
import ru.algeps.sparrow.message.request.domain.HttpRequest;
import ru.algeps.sparrow.message.request.domain.exceptions.http.clienterror.auth.AuthClientErrorHttpRequestException;
import ru.algeps.sparrow.worker.requestfilter.RequestFilter;

public interface HttpRequestFilter extends RequestFilter {
  @Override
  default void filter(Request request) throws AuthClientErrorHttpRequestException {
    this.httpFilter((HttpRequest) request);
  }

  void httpFilter(HttpRequest httpRequest) throws AuthClientErrorHttpRequestException;

  HttpMethod[] getFilterHttpMethods();

  default boolean hasHandleFilter(HttpMethod httpMethod) {
    for (HttpMethod method : getFilterHttpMethods()) {
      if (method.equals(httpMethod)) {
        return true;
      }
    }
    return false;
  }
}
