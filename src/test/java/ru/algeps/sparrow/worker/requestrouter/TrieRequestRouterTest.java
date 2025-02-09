package ru.algeps.sparrow.worker.requestrouter;

import static org.junit.jupiter.api.Assertions.*;
import static ru.algeps.sparrow.test_utils.TestUtil.createRandomLatinString;
import static ru.algeps.sparrow.test_utils.TestUtil.createRandomUrl;

import java.nio.channels.ByteChannel;
import org.junit.jupiter.api.Test;
import ru.algeps.sparrow.worker.handler.RequestHandler;
import ru.algeps.sparrow.worker.requestrouter.impl.TrieRequestRouter;

class TrieRequestRouterTest {
  ////////////////////////////////////////////////////////////
  //                  Проверки пути без фильтров
  ////////////////////////////////////////////////////////////
  //        Указание точного пути для обработчика //

  /**
   * Установка на конкретный путь обработчика, во всех остальных случаях должен вернуть null. Все
   * пути перебирать не нужны (чтобы проверить, что там отсутствует обработчик), нам лишь нужно
   * проверить, что именно на этот путь лежит данный обработчик.
   */
  @Test
  void testExactPath_thenSuccess() throws RequestRouter.InsertingInRequestRouterException {
    String path = "/data/index.html";
    RequestHandler requestHandler = getSimpleRequestHandler();
    TrieRequestRouter<RequestHandler> router = new TrieRequestRouter<>();
    router.insertHandler(path, requestHandler);

    RequestHandler actual = router.getHandlerByPath(path);
    assertEquals(requestHandler, actual);
  }

  /** Проверяет, что на путь, который не установлен обработчик возвращается null. */
  @Test
  void testExactPath_thenNull() throws RequestRouter.InsertingInRequestRouterException {
    String path = "/data/index.html";
    RequestHandler requestHandler = getSimpleRequestHandler();
    TrieRequestRouter<RequestHandler> router = new TrieRequestRouter<>();
    router.insertHandler(path, requestHandler);

    RequestHandler actualNull = router.getHandlerByPath("/123");
    assertNull(actualNull);
  }

  // Указание конкретного url для обработки (только внутри данного пути) //

  /** Проверка пути к папке (sub path url), проверяет что все папки, находящиеся в данной папки */
  @Test
  void testSpecificUrl_thenSuccess() throws RequestRouter.InsertingInRequestRouterException {
    String rootPath = "/%s/".formatted(createRandomLatinString(5));
    String pathForRequestHandler = rootPath + "*";
    RequestHandler requestHandler = getSimpleRequestHandler();
    TrieRequestRouter<RequestHandler> router = new TrieRequestRouter<>();
    router.insertHandler(pathForRequestHandler, requestHandler);

    int numberDifferentFile = 50;
    for (int i = 0; i < numberDifferentFile; i++) {
      RequestHandler actual = router.getHandlerByPath(rootPath + createRandomLatinString(10));
      assertEquals(requestHandler, actual);
    }
  }

  /** Проверяет, что все url, которые не покрывает обработчик, возвращает null. */
  @Test
  void testSpecificUrl_outsideFolder_thenNull()
      throws RequestRouter.InsertingInRequestRouterException {
    String rootPath = "/data/%s/".formatted(createRandomLatinString(5));
    String pathForRequestHandler = rootPath + "*";
    RequestHandler requestHandler = getSimpleRequestHandler();
    TrieRequestRouter<RequestHandler> router = new TrieRequestRouter<>();
    router.insertHandler(pathForRequestHandler, requestHandler);

    int numberDifferentUrl = 50;
    for (int i = 0; i < numberDifferentUrl; i++) {
      RequestHandler actualNull = router.getHandlerByPath("/" + createRandomLatinString(10));
      assertNull(actualNull);
    }

    for (int i = 0; i < numberDifferentUrl; i++) {
      RequestHandler actualNull =
          router.getHandlerByPath(
              "/" + createRandomLatinString(10) + "/" + createRandomLatinString(10));
      assertNull(actualNull);
    }
  }

  // Указание пути и все под пути для обработки (сколько угодно уровней вложенности) //

  /** Проверка пути к папке (sub path url), проверяет что все папки, находящиеся в данной папки */
  @Test
  void testSpecificUrlAndAllSubPath_thenSuccess()
      throws RequestRouter.InsertingInRequestRouterException {
    String rootPath = "/%s/".formatted(createRandomLatinString(5));
    String pathForRequestHandler = rootPath + "**";
    RequestHandler requestHandler = getSimpleRequestHandler();
    TrieRequestRouter<RequestHandler> router = new TrieRequestRouter<>();
    router.insertHandler(pathForRequestHandler, requestHandler);

    int numberDifferentUrl = 50;
    for (int i = 0; i < numberDifferentUrl; i++) {
      RequestHandler actual =
          router.getHandlerByPath(rootPath + createRandomUrl(2, 15).substring(1));
      assertEquals(requestHandler, actual);
    }
  }

  /**
   * Проверяет, что все url, которые не покрывает обработчик, возвращает null. (те, которые не
   * "достигают" указанного пути и те, которые имеют совершенно другое начало пути)
   */
  @Test
  void testSpecificUrlAndAllSubPath_outsideFolder_thenNull()
      throws RequestRouter.InsertingInRequestRouterException {
    String rootPath = "/%s/".formatted(createRandomLatinString(5));
    String pathForRequestHandler = rootPath + "*";
    RequestHandler requestHandler = getSimpleRequestHandler();
    TrieRequestRouter<RequestHandler> router = new TrieRequestRouter<>();
    router.insertHandler(pathForRequestHandler, requestHandler);

    int numberDifferentUrl = 10;
    for (int i = 0; i < numberDifferentUrl; i++) {
      RequestHandler actualNull = router.getHandlerByPath("/data" + createRandomUrl(2, 15));
      assertNull(actualNull);
    }
  }

  // Указание path с некоторым расширением файлов //

  /**
   * Проверяет, что обработчик правильно интерпретирует расширения файлов. Для каждого расширения -
   * один обработчик.
   */
  @Test
  void testUrlWithFileExtension_thenSuccess()
      throws RequestRouter.InsertingInRequestRouterException {
    String fileExtension = ".jpg";
    String rootPath = "/%s/".formatted(createRandomLatinString(5));
    String pathForRequestHandler = rootPath + "*" + fileExtension;
    RequestHandler requestHandler = getSimpleRequestHandler();
    TrieRequestRouter<RequestHandler> router = new TrieRequestRouter<>();
    router.insertHandler(pathForRequestHandler, requestHandler);

    int numberDifferentUrl = 10;
    for (int i = 0; i < numberDifferentUrl; i++) {
      RequestHandler actual =
          router.getHandlerByPath(rootPath + createRandomLatinString(10) + fileExtension);
      assertEquals(requestHandler, actual);
    }
  }

  @Test
  void testDepthUrlWithFileExtension_thenSuccess()
      throws RequestRouter.InsertingInRequestRouterException {
    String fileExtension = ".jpg";
    String rootPath = "/%s/".formatted(createRandomLatinString(5));
    String pathForRequestHandler = rootPath + "**" + fileExtension;
    RequestHandler requestHandler = getSimpleRequestHandler();
    TrieRequestRouter<RequestHandler> router = new TrieRequestRouter<>();
    router.insertHandler(pathForRequestHandler, requestHandler);

    int numberDifferentUrl = 10;
    int depth = 10;
    int supPathSize = 10;
    for (int i = 0; i < numberDifferentUrl; i++) {
      RequestHandler actual =
          router.getHandlerByPath(
              rootPath
                  + createRandomUrl(depth, supPathSize).substring(1)
                  + "/"
                  + createRandomLatinString(10)
                  + fileExtension);
      assertEquals(requestHandler, actual);
    }
  }

  /** Проверяет, что для расширения, для которого нет обработчика - вернёт null. */
  @Test
  void testUrlWithFileExtension_thenNull() throws RequestRouter.InsertingInRequestRouterException {
    String fileExtension = ".jpg";
    String rootPath = "/%s/".formatted(createRandomLatinString(5));
    String pathForRequestHandler = rootPath + "*." + fileExtension;
    RequestHandler requestHandler = getSimpleRequestHandler();
    TrieRequestRouter<RequestHandler> router = new TrieRequestRouter<>();
    router.insertHandler(pathForRequestHandler, requestHandler);

    int numberDifferentUrl = 10;
    String anotherFileExtension = ".png";
    for (int i = 0; i < numberDifferentUrl; i++) {
      RequestHandler actualNull =
          router.getHandlerByPath(rootPath + createRandomLatinString(10) + anotherFileExtension);
      assertNull(actualNull);
    }
  }

  // Негативные сценарии вставки //
  @Test
  void testInsert_withNullPath_thenException() {
    TrieRequestRouter<RequestHandler> router = new TrieRequestRouter<>();
    SimpleRequestHandler handler = getSimpleRequestHandler();

    assertThrows(IllegalArgumentException.class, () -> router.insertHandler(null, handler));
  }

  @Test
  void testInsert_withEmptyPath_thenException() {
    TrieRequestRouter<RequestHandler> router = new TrieRequestRouter<>();
    SimpleRequestHandler handler = getSimpleRequestHandler();

    assertThrows(IllegalArgumentException.class, () -> router.insertHandler("", handler));
  }

  @Test
  void testInsert_withNullRequestHandler_thenException() {
    TrieRequestRouter<RequestHandler> router = new TrieRequestRouter<>();

    assertThrows(IllegalArgumentException.class, () -> router.insertHandler("/data", null));
  }

  @Test
  void testInsert_withPathNotStartWithSlash_thenException() {
    TrieRequestRouter<RequestHandler> router = new TrieRequestRouter<>();
    SimpleRequestHandler handler = getSimpleRequestHandler();

    assertThrows(IllegalArgumentException.class, () -> router.insertHandler("data/www", handler));
  }

  /**
   * Проверяет пересечение. Имеется обработчик, который обрабатывает все URL в данном пути.
   *
   * <pre>
   *  Пример:
   *  /data/* - обработчик, который обрабатывает указанный путь (все файлы)
   *  /data/*.jpg - обработчик, который обрабатывает только указанный файл
   *
   *  Для указанного примера должно быть выброшено исключение.
   * </pre>
   */
  @Test
  void testInsert_withAnyRequestHandlerInPath_thenException()
      throws RequestRouter.InsertingInRequestRouterException {
    String rootPath = "/%s/".formatted(createRandomLatinString(5));
    String pathForRequestHandler = rootPath + "*";
    TrieRequestRouter<RequestHandler> router = new TrieRequestRouter<>();
    RequestHandler requestHandler = getSimpleRequestHandler();
    router.insertHandler(pathForRequestHandler, requestHandler);

    RequestHandler intersectingRequestHandler = getSimpleRequestHandler();

    assertThrows(
        RequestRouter.InsertingInRequestRouterException.class,
        () -> router.insertHandler(rootPath + "image.jpg", intersectingRequestHandler));
  }

  /**
   * Проверяет пересечение. Имеется обработчик, который обрабатывает все URL в данном пути.
   *
   * <pre>
   *  Пример:
   *  /data/** - обработчик, который обрабатывает указанный путь (все файлы)
   *  /data/www/files/*.jpg - обработчик, который обрабатывает только указанный файл
   *
   *  Для указанного примера должно быть выброшено исключение.
   * </pre>
   */
  @Test
  void testInsert_withAnyRequestHandlerInDepthPath_thenException()
      throws RequestRouter.InsertingInRequestRouterException {
    String rootPath = "/%s/".formatted(createRandomLatinString(5));
    String pathForRequestHandler = rootPath + "**";
    TrieRequestRouter<RequestHandler> router = new TrieRequestRouter<>();
    RequestHandler requestHandler = getSimpleRequestHandler();
    router.insertHandler(pathForRequestHandler, requestHandler);

    RequestHandler intersectingRequestHandler = getSimpleRequestHandler();

    assertThrows(
        RequestRouter.InsertingInRequestRouterException.class,
        () ->
            router.insertHandler(
                rootPath + createRandomUrl(2, 10) + "/*.jpg", intersectingRequestHandler));
  }

  ////////////////////////////////////////////////////////////
  //           Проверки пути без фильтров
  ////////////////////////////////////////////////////////////

  static SimpleRequestHandler getSimpleRequestHandler() {
    return new SimpleRequestHandler();
  }

  static class SimpleRequestHandler implements RequestHandler {

    @Override
    public void handle(ByteChannel socketChannel) {}
  }

  ////////////////////////////////////////////////////////////
  //           Случайный набор тестов
  ////////////////////////////////////////////////////////////

  @Test
  void test_getHandlerByPath_nullPath() {
    TrieRequestRouter<String> router = new TrieRequestRouter<>();
    assertThrows(IllegalArgumentException.class, () -> router.getHandlerByPath(null));
  }

  @Test
  void test_getHandlerByPath_pathNotStartWithSlash() {
    TrieRequestRouter<String> router = new TrieRequestRouter<>();
    assertThrows(IllegalArgumentException.class, () -> router.getHandlerByPath("path"));
  }

  @Test
  void test_getHandlerByPath_emptyRouter() {
    TrieRequestRouter<String> router = new TrieRequestRouter<>();
    assertNull(router.getHandlerByPath("/"));
    assertNull(router.getHandlerByPath("/any/path"));
  }

  @Test
  void test_getHandlerByPath_exactMatch() throws RequestRouter.InsertingInRequestRouterException {
    TrieRequestRouter<String> router = new TrieRequestRouter<>();
    router.insertHandler("/api/v1", "handler1");
    assertEquals("handler1", router.getHandlerByPath("/api/v1"));
  }

  @Test
  void test_getHandlerByPath_noMatch() throws RequestRouter.InsertingInRequestRouterException {
    TrieRequestRouter<String> router = new TrieRequestRouter<>();
    router.insertHandler("/api/v1", "handler1");
    assertNull(router.getHandlerByPath("/api/v2"));
    assertNull(router.getHandlerByPath("/api"));
    assertNull(router.getHandlerByPath("/"));
  }

  @Test
  void test_getHandlerByPath_starMatch_singleLevel()
      throws RequestRouter.InsertingInRequestRouterException {
    TrieRequestRouter<String> router = new TrieRequestRouter<>();
    router.insertHandler("/api/*", "handlerStar");
    assertEquals("handlerStar", router.getHandlerByPath("/api/users"));
    assertEquals("handlerStar", router.getHandlerByPath("/api/products"));
    assertNull(router.getHandlerByPath("/api"));
    assertNull(router.getHandlerByPath("/api/users/details"));
  }

  @Test
  void test_getHandlerByPath_doubleStarMatch_multipleLevels()
      throws RequestRouter.InsertingInRequestRouterException {
    TrieRequestRouter<String> router = new TrieRequestRouter<>();
    router.insertHandler("/api/**", "handlerDoubleStar");
    assertEquals("handlerDoubleStar", router.getHandlerByPath("/api/users"));
    assertEquals("handlerDoubleStar", router.getHandlerByPath("/api/users/details"));
    assertEquals("handlerDoubleStar", router.getHandlerByPath("/api/products/123/info"));
    assertNull(router.getHandlerByPath("/"));
    assertNull(router.getHandlerByPath("/other/path"));
  }

  @Test
  void test_getHandlerByPath_rootStarMatch()
      throws RequestRouter.InsertingInRequestRouterException {
    TrieRequestRouter<String> router = new TrieRequestRouter<>();
    router.insertHandler("/*", "rootHandler");
    assertEquals("rootHandler", router.getHandlerByPath("/"));
    assertNull(router.getHandlerByPath("/some/path"));
    assertEquals("rootHandler", router.getHandlerByPath("/any"));
  }

  @Test
  void test_getHandlerByPath_rootDoubleStarMatch()
      throws RequestRouter.InsertingInRequestRouterException {
    TrieRequestRouter<String> router = new TrieRequestRouter<>();
    router.insertHandler("/**", "rootHandler");
    assertEquals("rootHandler", router.getHandlerByPath("/"));
    assertEquals("rootHandler", router.getHandlerByPath("/some/path"));
    assertEquals("rootHandler", router.getHandlerByPath("/any"));
  }
}
