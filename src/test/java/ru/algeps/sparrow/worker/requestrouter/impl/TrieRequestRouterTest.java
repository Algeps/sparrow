package ru.algeps.sparrow.worker.requestrouter.impl;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import ru.algeps.sparrow.message.request.Request;
import ru.algeps.sparrow.worker.handler.RequestHandler;
import ru.algeps.sparrow.worker.requestrouter.RequestRouter;
import ru.algeps.sparrow.worker.requestrouter.requestmatcher.RequestMatcher;

class TrieRequestRouterTest {

  /** Проверка на то, что он возвращает точное сопоставление находящийся внутри дерева строки. */
  @Test
  void findByPathWithoutRoute() throws RequestRouter.InsertingInRequestRouterException {
    String path = "/data/**";
    RequestMatcher requestMatcher = new SimpleRequestMatcher();
    TrieRequestRouter<RequestMatcher> requestRouter = new TrieRequestRouter<>();
    requestRouter.insertHandler(path, requestMatcher);

    RequestMatcher actual = requestRouter.findByPathWithoutRoute(path);
    assertEquals(requestMatcher, actual);
  }

  static class SimpleRequestMatcher implements RequestMatcher {
    @Override
    public void insertRequestHandler(RequestHandler requestHandler) {

    }

    @Override
    public RequestHandler match(Request request) {
      return null;
    }
  }
}
