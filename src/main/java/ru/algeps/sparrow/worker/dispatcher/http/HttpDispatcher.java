package ru.algeps.sparrow.worker.dispatcher.http;

import ru.algeps.sparrow.message.request.Request;
import ru.algeps.sparrow.message.request.domain.HttpRequest;
import ru.algeps.sparrow.message.request.domain.exceptions.http.HttpRequestException;
import ru.algeps.sparrow.worker.dispatcher.Dispatcher;
import ru.algeps.sparrow.worker.handler.RequestHandler;
import ru.algeps.sparrow.worker.handler.http.HttpRequestHandler;

public interface HttpDispatcher extends Dispatcher {
  @Override
  default RequestHandler route(Request request) throws HttpRequestException{
    return this.httpRoute((HttpRequest) request);
  }

  HttpRequestHandler httpRoute(HttpRequest httpRequest) throws HttpRequestException;
}
