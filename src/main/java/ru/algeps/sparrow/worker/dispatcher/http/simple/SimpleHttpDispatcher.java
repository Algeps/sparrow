package ru.algeps.sparrow.worker.dispatcher.http.simple;

import java.util.List;
import java.util.Objects;

import ru.algeps.sparrow.message.request.domain.HttpRequest;
import ru.algeps.sparrow.message.request.domain.exceptions.http.HttpRequestException;
import ru.algeps.sparrow.message.request.domain.exceptions.http.clienterror.NotFoundHttp1_1RequestException;
import ru.algeps.sparrow.worker.dispatcher.http.HttpDispatcher;
import ru.algeps.sparrow.worker.handler.http.HttpRequestHandler;
import ru.algeps.sparrow.worker.requestfilter.http.HttpRequestFilter;
import ru.algeps.sparrow.worker.requestrouter.RequestRouter;
import ru.algeps.sparrow.worker.requestrouter.impl.TrieRequestRouter;

public class SimpleHttpDispatcher implements HttpDispatcher {
  private final TrieRequestRouter<HttpRequestFilter> requestFilterRouterTrie;
  private final TrieRequestRouter<HttpRequestHandler> requestHandlerRouterTrie;

  public SimpleHttpDispatcher(
      List<HttpFilterConfig> httpFilterConfigs, List<HttpRouteConfig> httpRouteConfigs)
      throws RequestRouter.InsertingInRequestRouterException {
    Objects.requireNonNull(httpRouteConfigs, "List of HttpRouteConfig should not be null!");
    if (httpRouteConfigs.isEmpty()) {
      throw new IllegalArgumentException("There must be at least one request handler!");
    }

    this.requestHandlerRouterTrie = new TrieRequestRouter<>();
    for (HttpRouteConfig httpRouteConfig : httpRouteConfigs) {
      this.requestHandlerRouterTrie.insertHandler(
          httpRouteConfig.path(), httpRouteConfig.requestHandler());
    }

    this.requestFilterRouterTrie = new TrieRequestRouter<>();
    if (httpFilterConfigs != null) {
      for (HttpFilterConfig httpRequestFilter : httpFilterConfigs) {
        this.requestFilterRouterTrie.insertHandler(
            httpRequestFilter.path(), httpRequestFilter.requestFilter());
      }
    }
  }

  @Override
  public HttpRequestHandler httpRoute(HttpRequest httpRequest) throws HttpRequestException {
    String path = httpRequest.uri().getPath();

    HttpRequestFilter filterByPath = requestFilterRouterTrie.getHandlerByPath(path);
    if (filterByPath != null && filterByPath.hasHandleFilter(httpRequest.httpMethod())) {
      filterByPath.httpFilter(httpRequest);
    }

    HttpRequestHandler handlerByPath = requestHandlerRouterTrie.getHandlerByPath(path);
    if (handlerByPath == null) {
      throw new NotFoundHttp1_1RequestException("Not found for path=[%s]".formatted(path));
    }
    return handlerByPath;
  }

  public record HttpRouteConfig(String path, HttpRequestHandler requestHandler) {}

  public record HttpFilterConfig(String path, HttpRequestFilter requestFilter) {}
}
