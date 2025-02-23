package ru.algeps.sparrow.worker.requestfilter;

import ru.algeps.sparrow.message.request.Request;
import ru.algeps.sparrow.message.request.domain.exceptions.RequestException;

public interface RequestFilter {
  void filter(Request request) throws RequestException;
}
