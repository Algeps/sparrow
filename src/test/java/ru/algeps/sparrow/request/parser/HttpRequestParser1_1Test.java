package ru.algeps.sparrow.request.parser;

import static org.junit.jupiter.api.Assertions.*;
import static ru.algeps.sparrow.test_utils.TestUtil.*;

import com.sun.net.httpserver.Headers;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.algeps.sparrow.message.HttpVersion;
import ru.algeps.sparrow.message.request.domain.HttpMethod;
import ru.algeps.sparrow.message.request.domain.HttpRequest;
import ru.algeps.sparrow.message.request.domain.HttpRequest1_1;
import ru.algeps.sparrow.message.request.domain.exceptions.http.HttpRequestException;
import ru.algeps.sparrow.message.request.domain.exceptions.http.clienterror.BadRequestHttp1_1RequestException;
import ru.algeps.sparrow.message.request.domain.exceptions.http.clienterror.MethodNotAllowedHttp1_1RequestException;
import ru.algeps.sparrow.message.request.domain.exceptions.http.clienterror.UriTooLongHttp1_1RequestException;
import ru.algeps.sparrow.message.request.domain.exceptions.http.servererror.HttpVersionNotSupportedHttp1_1RequestException;
import ru.algeps.sparrow.message.request.parser.http.HttpRequestParser;
import ru.algeps.sparrow.message.request.parser.http.exceptions.HttpRequestParserException;
import ru.algeps.sparrow.message.request.parser.http.exceptions.HttpRequestParserInvalidMethodException;

class HttpRequestParser1_1Test {
  protected final Logger log = LoggerFactory.getLogger(this.getClass());
  static Headers mockHeaders =
      Headers.of(
          Map.of(
              "Host",
              List.of("localhost"),
              "Accept-Encoding",
              List.of("gzip", "deflate"),
              "User-Agent",
              List.of(
                  "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36")));
  static String mockHeadersString =
      """
            Host: localhost\r
            Accept-Encoding: gzip, deflate\r
            User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36\r""";
  /////////////////////////////////////////////////////////////////////////////////////////////////
  //                                Проверка парсинга Метода и короткого url
  /////////////////////////////////////////////////////////////////////////////////////////////////
  private static final List<String> incorrectMethodValueStrings =
      Arrays.stream(HttpMethod.values())
          .map(HttpMethod::getName)
          .map(method -> createIncorrectList(method, 1))
          .flatMap(Collection::stream)
          .toList();

  @ParameterizedTest
  @EnumSource(HttpMethod.class)
  void parseControlData_smallUri_withDifferentMethods_thenSuccess(HttpMethod httpMethod)
      throws IOException {
    String message =
        """
            %s /hello HTTP/1.1\r
            %s
            \r
            """
            .formatted(httpMethod.getName(), mockHeadersString);
    HttpRequest httpRequestExpected =
        HttpRequest.newBuilder(HttpVersion.HTTP_1_1)
            .httpMethod(httpMethod)
            .uri(URI.create("/hello"))
            .version(HttpVersion.HTTP_1_1)
            .headers(mockHeaders)
            .build();

    ByteArrayInputStream byteArrayInputStream =
        new ByteArrayInputStream(message.getBytes(StandardCharsets.UTF_8));
    ReadableByteChannel readableByteChannel = Channels.newChannel(byteArrayInputStream);

    HttpRequestParser httpRequestParser =
        HttpRequestParser.connect(readableByteChannel, HttpVersion.HTTP_1_1);
    HttpRequest httpRequestActual = httpRequestParser.getHttpRequest();
    assertEquals(httpRequestExpected.httpMethod(), httpRequestActual.httpMethod());
    assertEquals(httpRequestExpected.uri(), httpRequestActual.uri());
  }

  @ParameterizedTest
  @NullAndEmptySource
  @MethodSource("providerFor_parseControlData_thenException")
  void parseControlData_thenException(String incorrectMethods) {
    String message =
        """
                    %s /hello HTTP/1.1\r
                    %s
                    \r
                    """
            .formatted(incorrectMethods, mockHeadersString);
    ByteArrayInputStream byteArrayInputStream =
        new ByteArrayInputStream(message.getBytes(StandardCharsets.UTF_8));
    ReadableByteChannel readableByteChannel = Channels.newChannel(byteArrayInputStream);

    MethodNotAllowedHttp1_1RequestException methodNotAllowedHttp11RequestException =
        assertThrows(
            MethodNotAllowedHttp1_1RequestException.class,
            () ->
                HttpRequestParser.connect(readableByteChannel, HttpVersion.HTTP_1_1)
                    .getHttpRequest());
    assertEquals(
        new HttpRequestParserInvalidMethodException().toString(),
        methodNotAllowedHttp11RequestException.getMessage());
  }

  static Stream<Arguments> providerFor_parseControlData_thenException() {
    return incorrectMethodValueStrings.stream().map(Arguments::of);
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////
  //                                Проверка парсинга url
  /////////////////////////////////////////////////////////////////////////////////////////////////
  @Test
  void parseControlData_bigUri_thenSuccess() throws IOException {
    String bigUriString = "/hello".repeat(200);
    String message =
        """
                GET %s HTTP/1.1\r
                %s
                \r
                """
            .formatted(bigUriString, mockHeadersString);
    ByteArrayInputStream byteArrayInputStream =
        new ByteArrayInputStream(message.getBytes(StandardCharsets.UTF_8));
    ReadableByteChannel readableByteChannel = Channels.newChannel(byteArrayInputStream);
    HttpRequest httpRequestExpected =
        HttpRequest.newBuilder(HttpVersion.HTTP_1_1)
            .httpMethod(HttpMethod.GET)
            .uri(URI.create(bigUriString))
            .version(HttpVersion.HTTP_1_1)
            .headers(mockHeaders)
            .build();

    HttpRequestParser httpRequestParser =
        HttpRequestParser.connect(readableByteChannel, HttpVersion.HTTP_1_1);
    HttpRequest httpRequestActual = httpRequestParser.getHttpRequest();
    assertEquals(httpRequestExpected.httpMethod(), httpRequestActual.httpMethod());
    assertEquals(httpRequestExpected.uri(), httpRequestActual.uri());
  }

  @Test
  void parseControlData_bigUri_thenException() {
    ByteArrayInputStream byteArrayInputStream =
        getByteArrayInputStreamFor_parseControlData_bigUri_thenException();
    ReadableByteChannel readableByteChannel = Channels.newChannel(byteArrayInputStream);

    UriTooLongHttp1_1RequestException uriTooLongHttp11RequestException =
        assertThrows(
            UriTooLongHttp1_1RequestException.class,
            () ->
                HttpRequestParser.connect(readableByteChannel, HttpVersion.HTTP_1_1)
                    .getHttpRequest());
    assertEquals(
        new HttpRequestParserException("Too long URI (max=8000)").toString(),
        uriTooLongHttp11RequestException.getMessage());
  }

  private static ByteArrayInputStream
      getByteArrayInputStreamFor_parseControlData_bigUri_thenException() {
    String bigUriString = "/hello".repeat(1500);
    String message =
        """
                    GET %s HTTP/1.1\r
                    %s
                    \r
                    """
            .formatted(bigUriString, mockHeadersString);
    return new ByteArrayInputStream(message.getBytes(StandardCharsets.UTF_8));
  }

  @Test
  void parseControlData_incorrectUriRelative_thenException() {
    ByteArrayInputStream byteArrayInputStream =
        getByteArrayInputStreamFor_parseControlData_incorrectUri_thenException();
    ReadableByteChannel readableByteChannel = Channels.newChannel(byteArrayInputStream);

    BadRequestHttp1_1RequestException badRequestHttp11RequestException =
        assertThrows(
            BadRequestHttp1_1RequestException.class,
            () ->
                HttpRequestParser.connect(readableByteChannel, HttpVersion.HTTP_1_1)
                    .getHttpRequest());
    assertEquals(
        new HttpRequestParserException("Incorrect URI (is relative)").toString(),
        badRequestHttp11RequestException.getMessage());
  }

  private static ByteArrayInputStream
      getByteArrayInputStreamFor_parseControlData_incorrectUri_thenException() {
    String bigUriString = "../hello";
    String message =
        """
                    GET %s HTTP/1.1\r
                    %s
                    \r
                    """
            .formatted(bigUriString, mockHeadersString);

    return new ByteArrayInputStream(message.getBytes(StandardCharsets.UTF_8));
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////
  //                                Проверка парсинга версии HTTP
  /////////////////////////////////////////////////////////////////////////////////////////////////

  @ParameterizedTest
  @EnumSource(HttpVersion.class)
  void parseControlData_Version_thenSuccess(HttpVersion httpVersion) throws IOException {
    ByteArrayInputStream byteArrayInputStream =
        getByteArrayInputStreamFor_parseControlData_Version_thenSuccess(httpVersion);
    ReadableByteChannel readableByteChannel = Channels.newChannel(byteArrayInputStream);
    HttpRequest httpRequestExpected =
        HttpRequest.newBuilder(HttpVersion.HTTP_1_1)
            .httpMethod(HttpMethod.GET)
            .uri(URI.create("/hello"))
            .version(httpVersion)
            .headers(mockHeaders)
            .build();

    HttpRequestParser httpRequestParser =
        HttpRequestParser.connect(readableByteChannel, HttpVersion.HTTP_1_1);
    HttpRequest httpRequestActual = httpRequestParser.getHttpRequest();
    assertEquals(httpRequestExpected.httpMethod(), httpRequestActual.httpMethod());
  }

  private static ByteArrayInputStream
      getByteArrayInputStreamFor_parseControlData_Version_thenSuccess(HttpVersion httpVersion) {
    String message =
        """
                GET /hello %s\r
                %s
                \r
                """
            .formatted(httpVersion.getVersion(), mockHeadersString);
    return new ByteArrayInputStream(message.getBytes(StandardCharsets.UTF_8));
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(
      strings = {
        "HTTG/1.0",
        "HTT/1.0",
        "HTGP/1.0",
        "HGTP/1.0",
        "GTTP/1.0",
        "HTTP/1.",
        "http/1.0",
        "HTTP\\1.0",
        "HTTP1.0",
        "\n"
      })
  void parseControlData_Version_thenException(String incorrectVersion) {
    ByteArrayInputStream byteArrayInputStream =
        getByteArrayInputStreamFor_parseControlData_Version_thenException(incorrectVersion);
    ReadableByteChannel readableByteChannel = Channels.newChannel(byteArrayInputStream);

    assertThrows(
        HttpVersionNotSupportedHttp1_1RequestException.class,
        () ->
            HttpRequestParser.connect(readableByteChannel, HttpVersion.HTTP_1_1).getHttpRequest());
  }

  private static ByteArrayInputStream
      getByteArrayInputStreamFor_parseControlData_Version_thenException(String incorrectVersion) {
    String message =
        """
                        GET /hello %s\r
                        %s
                        \r
                        """
            .formatted(incorrectVersion, mockHeadersString);
    return new ByteArrayInputStream(message.getBytes(StandardCharsets.UTF_8));
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////
  //                                Проверка парсинга заголовков
  /////////////////////////////////////////////////////////////////////////////////////////////////

  @ParameterizedTest
  @MethodSource("providerFor_parseHeaders_thenSuccess")
  void parseHeaders_thenSuccess(String headers, Map<String, List<String>> headersMap)
      throws IOException {
    String message =
        """
        GET /hello HTTP/1.1\r
        %s
        \r
        """
            .formatted(headers);
    ByteArrayInputStream byteArrayInputStream =
        new ByteArrayInputStream(message.getBytes(StandardCharsets.UTF_8));
    ReadableByteChannel readableByteChannel = Channels.newChannel(byteArrayInputStream);
    HttpRequest httpRequestExpected =
        HttpRequest.newBuilder(HttpVersion.HTTP_1_1)
            .httpMethod(HttpMethod.GET)
            .uri(URI.create("/hello"))
            .version(HttpVersion.HTTP_1_1)
            .headers(Headers.of(headersMap))
            .build();

    HttpRequestParser httpRequestParser =
        HttpRequestParser.connect(readableByteChannel, HttpVersion.HTTP_1_1);
    HttpRequest httpRequestActual = httpRequestParser.getHttpRequest();
    assertEquals(httpRequestExpected.headers(), httpRequestActual.headers(), httpRequestActual.toString());
  }

  static Stream<Arguments> providerFor_parseHeaders_thenSuccess() {
    return Stream.of(
        Arguments.of(
            """
                      Host: localhost\r
                      Accept-Encoding:gzip, deflate\r
                      User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36\r""",
            Map.of(
                "Host",
                List.of("localhost"),
                "Accept-Encoding",
                List.of("gzip", "deflate"),
                "User-Agent",
                List.of(
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36"))),
        Arguments.of(
            """
                          Host: localhost\r
                          AUTH: BEarer  123\r
                          Accept-Encoding:  gzip,     deflate            \r
                          User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36\r""",
            Map.of(
                "Host",
                List.of("localhost"),
                "AUTH",
                List.of("BEarer  123"),
                "Accept-Encoding",
                List.of("gzip", "deflate"),
                "User-Agent",
                List.of(
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36"))),
        Arguments.of(
            """
                    Host: localhost\r
                    Content-Type: application/json\r
                    Accept-Language: en-US,en;q=0.9\r
                    User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36\r""",
            Map.of(
                "Host",
                List.of("localhost"),
                "Content-Type",
                List.of("application/json"),
                "Accept-Language",
                List.of("en-US,en;q=0.9"),
                "User-Agent",
                List.of(
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36"))),
        Arguments.of(
            """
                    Host: localhost\r
                    Cache-Control: no-cache\r
                    Pragma: no-cache\r
                    User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36\r""",
            Map.of(
                "Host",
                List.of("localhost"),
                "Cache-Control",
                List.of("no-cache"),
                "Pragma",
                List.of("no-cache"),
                "User-Agent",
                List.of(
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36"))),
        Arguments.of(
            """
                    Host: localhost\r
                    Empty-Header: \r
                    User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36\r""",
            Map.of(
                "Host",
                List.of("localhost"),
                "User-Agent",
                List.of(
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36"))),
        Arguments.of(
            """
                    Host: localhost\r
                    Key: Value1\r
                    Key: Value2\r
                    User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36\r""",
            Map.of(
                "Host",
                List.of("localhost"),
                "Key",
                List.of("Value1", "Value2"),
                "User-Agent",
                List.of(
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36"))));
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////
  //                                Проверка парсинга всего http сообщения (вместе с телом)
  /////////////////////////////////////////////////////////////////////////////////////////////////
  static String mockMessage =
      """
             GET /data/index.html HTTP/1.1\r
             Host: localhost\r
             Accept: */*\r
             Accept-Encoding: gzip, deflate\r
             Cache-Control: no-cache\r
             Content-Length: 14\r
             Content-Type: text/plain\r
             User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36\r
             \r
             Hello, world!
             """;
  static HttpRequest mockHttpRequest;

  static {
    try {
      mockHttpRequest =
          HttpRequest.newBuilder(HttpVersion.HTTP_1_1)
              .httpMethod(HttpMethod.GET)
              .uri(URI.create("/data/index.html"))
              .version(HttpVersion.HTTP_1_1)
              .headers(
                  new Headers(
                      Map.of(
                          "Host",
                          List.of("localhost"),
                          "Content-Length",
                          List.of("14"),
                          "Content-Type",
                          List.of("text/plain"),
                          "Accept-Encoding",
                          List.of("gzip", "deflate"),
                          "User-Agent",
                          List.of(
                              "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36"),
                          "Accept",
                          List.of("*/*"),
                          "Cache-Control",
                          List.of("no-cache"))))
              .body("Hello, world!\n".getBytes(StandardCharsets.UTF_8))
              .build();
    } catch (HttpRequestException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  void parseAllHttpRequest() throws IOException {
    String message = mockMessage;
    HttpRequest httpRequestExpected = mockHttpRequest;

    ReadableByteChannel readableByteChannel =
        Channels.newChannel(new ByteArrayInputStream(message.getBytes(StandardCharsets.UTF_8)));

    HttpRequestParser httpRequestParser =
        HttpRequestParser.connect(readableByteChannel, HttpVersion.HTTP_1_1);
    HttpRequest httpRequestActual = httpRequestParser.getHttpRequest();
    assertEquals(httpRequestExpected, httpRequestActual);
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////
  //                                Проверка парсинга трейлеров
  /////////////////////////////////////////////////////////////////////////////////////////////////

  @Test
  void test_trailers() throws IOException {
    String messageWithTrailers =
        """
             GET /data/index.html HTTP/1.1\r
             Host: localhost\r
             Accept: */*\r
             Accept-Encoding: gzip, deflate\r
             Cache-Control: no-cache\r
             Trailer: AnyTrailer, AnyTrailer2\r
             Transfer-Encoding: chunked\r
             Content-Type: text/plain\r
             User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36\r
             \r
             E\r
             Hello, world!
             \r
             0\r
             AnyTrailer: anyValue1 , anyValue2\r
             AnyTrailer2: anyValue3  \r
             \r
             """;
    ReadableByteChannel readableByteChannel =
        Channels.newChannel(
            new ByteArrayInputStream(messageWithTrailers.getBytes(StandardCharsets.UTF_8)));

    var httpRequestParser = HttpRequestParser.connect(readableByteChannel, HttpVersion.HTTP_1_1);
    HttpRequest1_1 httpRequest = (HttpRequest1_1) httpRequestParser.getHttpRequest();
    // валидация контента
    String stringBody = new String(httpRequest.body());
    assertEquals("Hello, world!\n", stringBody);
    // валидация трейлеров
    Headers trailers = httpRequest.trailers();
    assertNotNull(trailers);
    assertEquals("anyValue1", trailers.get("AnyTrailer").get(0));
    assertEquals("anyValue2", trailers.get("AnyTrailer").get(1));
    assertEquals("anyValue3", trailers.get("AnyTrailer2").getFirst());
  }

  // todo написать тест со множеством чанков

  /////////////////////////////////////////////////////////////////////////////////////////////////
  //                                Проверка поточного парсинга
  /////////////////////////////////////////////////////////////////////////////////////////////////

  // todo возможно нужен метод clear(), который бы не создавал новый объект, а использовал уже
  //  созданный builder (с очисткой заголовков)
  //  создать свой Headers
  //  создать свой аналог StringBuilder (чтобы проверять при расширении лимит)
  //  создать префиксное дерево, чтобы проверять некоторые заголовки

  @Test
  void parseStreamHttpRequest() throws IOException {
    final int countMessageInStream = 2_000_00;
    String message = mockMessage.repeat(countMessageInStream);
    HttpRequest httpRequestExpected = mockHttpRequest;

    ReadableByteChannel readableByteChannel =
        Channels.newChannel(new ByteArrayInputStream(message.getBytes(StandardCharsets.UTF_8)));
    HttpRequestParser httpRequestParser =
        HttpRequestParser.connect(readableByteChannel, HttpVersion.HTTP_1_1);

    List<HttpRequest> expectedList =
        IntStream.range(0, countMessageInStream).mapToObj(i -> httpRequestExpected).toList();
    List<HttpRequest> actualList = new ArrayList<>((int) (countMessageInStream * 1.5));

    DurationFunction<List<HttpRequest>> durationFunction =
        durationFunction(() -> handle(countMessageInStream, actualList, httpRequestParser));

    assertEquals(expectedList, actualList);

    float rpc = countMessageInStream / (float) durationFunction.getMillisecondDuration() * 1_000;
    int messageBitesSize = mockMessage.getBytes(StandardCharsets.UTF_8).length;
    log.info("MessageBitesSize=[{}]", messageBitesSize);
    log.info("Count message=[{}]", countMessageInStream);
    log.info("Duration=[{}]", durationFunction);
    log.info("Parse message/seconds=[{}]", rpc);
  }

  List<HttpRequest> handle(
      int countMessageInStream,
      List<HttpRequest> httpRequestList,
      HttpRequestParser httpRequestParser) {
    int i = 0;
    try {
      for (; i < countMessageInStream; i++) {
        HttpRequest httpRequestActual = httpRequestParser.getHttpRequest();
        httpRequestList.add(httpRequestActual);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return httpRequestList;
  }
}
