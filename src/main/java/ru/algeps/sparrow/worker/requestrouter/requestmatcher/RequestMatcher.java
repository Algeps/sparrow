package ru.algeps.sparrow.worker.requestrouter.requestmatcher;

import ru.algeps.sparrow.message.request.Request;
import ru.algeps.sparrow.worker.handler.RequestHandler;

public interface RequestMatcher {
  void insertRequestHandler(RequestHandler requestHandler);

  RequestHandler match(Request request);
}
