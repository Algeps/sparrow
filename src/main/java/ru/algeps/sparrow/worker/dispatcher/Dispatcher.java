package ru.algeps.sparrow.worker.dispatcher;

import ru.algeps.sparrow.message.request.Request;
import ru.algeps.sparrow.message.request.domain.exceptions.RequestException;
import ru.algeps.sparrow.worker.handler.RequestHandler;

public interface Dispatcher {
  RequestHandler route(Request request) throws RequestException;
}
