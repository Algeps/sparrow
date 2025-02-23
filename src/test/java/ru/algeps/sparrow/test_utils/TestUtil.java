package ru.algeps.sparrow.test_utils;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.util.Timeout;
import org.junit.jupiter.api.Test;
import ru.algeps.sparrow.context.Constants;
import ru.algeps.sparrow.message.FieldName;
import ru.algeps.sparrow.message.HttpVersion;
import ru.algeps.sparrow.message.request.domain.HttpRequest;
import ru.algeps.sparrow.message.request.domain.mediatype.MediaType;
import ru.algeps.sparrow.message.request.parser.http.HttpRequestParser;
import ru.algeps.sparrow.message.response.domain.HttpResponse;
import ru.algeps.sparrow.message.response.domain.HttpResponse1_1;
import ru.algeps.sparrow.message.response.domain.HttpStatusCode;
import ru.algeps.sparrow.worker.processor.RequestProcessor;
import ru.algeps.sparrow.worker.server.Server;

import javax.net.ssl.SSLContext;

public final class TestUtil {
  private TestUtil() {}

  private static final char[] UP_AND_DOWN_LATIN_AND_NUMBER_CHARACTERS =
      "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
  private static final Random random = new Random();

  /**
   * Возвращает случайную строку указанного размера. Могут содержать указанные символы: [0-9],
   * [a-z], [A-Z]
   */
  public static String createRandomLatinString(int length) {
    if (length < 0) {
      throw new IllegalArgumentException("'length' should not be below 0");
    }

    StringBuilder sb = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      char c = getRandomLatinChar();
      sb.append(c);
    }
    return sb.toString();
  }

  /** Возвращает случайный символ из возможных: [0-9], * [a-z], [A-Z], исключая символ except */
  public static char getRandomLatinChar(char except) {
    char c;
    do {
      c = getRandomLatinChar();
    } while (c == except);

    return c;
  }

  /** Возвращает случайный символ из возможных: [0-9], * [a-z], [A-Z] */
  public static char getRandomLatinChar() {
    int index = random.nextInt(0, UP_AND_DOWN_LATIN_AND_NUMBER_CHARACTERS.length);
    return UP_AND_DOWN_LATIN_AND_NUMBER_CHARACTERS[index];
  }

  /**
   * Создаёт случайный url с указанными уровнями вложенности. НЕ завершает путь слэшем.
   *
   * <pre>
   *  Пример:
   *  - для 0 - создание только одной директории: '/path', '/9138t49t'
   *  - для 1: '/o837y3gn0/893c2498ty'
   *  и так далее
   *  </pre>
   *
   * @param depth уровней вложенности (не может быть отрицательным)
   * @param subPathSize количество символов для каждого уровня вложенности
   */
  public static String createRandomUrl(int depth, int subPathSize) {
    if (depth < 0) {
      throw new IllegalArgumentException("'depth' should not be below 0");
    }

    StringBuilder sb = new StringBuilder("/");
    for (int i = 0; i < depth; i++) {
      sb.append(createRandomLatinString(subPathSize));
    }
    return sb.toString();
  }

  /**
   * Заменяет в строке input на указанной позиции index на любой другой символ ASCII. Невозможна
   * ситуация, при которой символ на указанной позиции будет заменён на тот же самый.
   *
   * @return Возвращает новую строку, с изменённым на позиции index символом.
   */
  public static String replaceCharInString(String input, int index) {
    char[] temp = input.toCharArray();
    char except = temp[index];
    temp[index] = getRandomLatinChar(except);
    return new String(temp);
  }

  /** Возвращает продолжительность операции в миллисекундах. */
  public static <T> DurationFunction<T> durationFunction(Supplier<T> supplier) {
    Instant start = Instant.now();
    T result = supplier.get();
    Instant end = Instant.now();
    return new DurationFunction<>(start, end, result);
  }

  /**
   * Меняет с первого и по второй символы строки. Размер возвращаемого списка = input.length() -
   * index. Если с index больше чем input.length() - 1, то будет возвращена входная строка input
   * строка без изменений.
   *
   * @param index индекс char, с которого будут изменяться символы.
   */
  public static List<String> createIncorrectList(String input, int index) {
    if (index < 0) {
      throw new IllegalArgumentException("Index must be greater than zero!");
    }

    List<String> resultList = new ArrayList<>();

    if (input.length() < index) {
      resultList.add(input);
      return resultList;
    }

    for (int i = index; i < input.length(); i++) {
      resultList.add(replaceCharInString(input, i));
    }

    return resultList;
  }

  public static String getFullMessage(Throwable e) {
    if (e == null) {
      return null;
    }

    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    pw.println(e.getMessage());
    e.printStackTrace(pw);
    return sw.toString();
  }

  public static class DurationFunction<T> {
    private final Instant start;
    private final Instant end;
    private final T result;

    public DurationFunction(Instant start, Instant end, T result) {
      this.start = start;
      this.end = end;
      this.result = result;
    }

    public T getResult() {
      return result;
    }

    public long getMillisecondDuration() {
      return start.until(end, ChronoUnit.MILLIS);
    }

    public long getSecondDuration() {
      return start.until(end, ChronoUnit.SECONDS);
    }

    public long getNanoSecondDuration() {
      return start.until(end, ChronoUnit.NANOS);
    }

    public long getMicroSecondDuration() {
      return start.until(end, ChronoUnit.MICROS);
    }

    @Override
    public String toString() {
      return new StringJoiner(", ", DurationFunction.class.getSimpleName() + "[", "]")
          .add("nanos=" + getNanoSecondDuration())
          .add("micros=" + getMicroSecondDuration())
          .add("millis=" + getMillisecondDuration())
          .add("seconds=" + getSecondDuration())
          .toString();
    }

    public String toStringWithResult() {
      return new StringJoiner(", ", DurationFunction.class.getSimpleName() + "[", "]")
          .add("nanos=" + getNanoSecondDuration())
          .add("micros=" + getMicroSecondDuration())
          .add("millis=" + getMillisecondDuration())
          .add("seconds=" + getSecondDuration())
          .add("result=" + getResult())
          .toString();
    }
  }

  public static RequestProcessor createRequestProcessor(
      String name, HttpVersion httpVersion, List<HttpRequest> httpRequests) {
    HttpResponse httpResponse =
        HttpResponse.responseFor(httpVersion)
            .statusCode(HttpStatusCode.OK)
            .addHeader(FieldName.CONNECTION.getName(), "close")
            .addHeader("Custom-Header", "Hello world!")
            .addBody(Constants.NOT_FOUND_HTML_CONTENT, MediaType.Text.HTML);
    return createRequestProcessor(name, httpVersion, httpResponse, httpRequests);
  }

  public static RequestProcessor createRequestProcessor(
      String name,
      HttpVersion httpVersion,
      HttpResponse httpResponse,
      List<HttpRequest> httpRequests) {
    return (byteChannel) -> {
      var httpRequestParser = HttpRequestParser.connect(byteChannel, httpVersion);
      HttpRequest httpRequest = httpRequestParser.getHttpRequest();
      httpRequests.add(httpRequest);
      System.out.printf(
          "Handler name:[%s]. Receive message: \n\r%s\n\r %s \n\r%s\n\r",
          name, "-".repeat(30), httpRequest, "-".repeat(30));

      ByteBuffer responseByteBuffer = httpResponse.toByteBuffer();
      byteChannel.write(responseByteBuffer);
    };
  }

  /** Проверяет готовность сервера принимать ответы. Параметры ожидания захардкожены. */
  public static void validateStartServer(Server server) {
    await("Ожидание запуска сервера")
        // начиная со 2 секунды
        .pollDelay(Duration.ofSeconds(2))
        // каждую секунду
        .pollInterval(Duration.ofSeconds(1))
        // в течение 5 секунд
        .atMost(Duration.ofSeconds(5))
        .untilAsserted(
            () ->
                assertTrue(
                    server.getServerState().isRunning(),
                    () ->
                        "Сервер не смог запуститься за указанное время. Описание:"
                            + server.getServerState()));
  }

  //////////////////////////////////////////////////////////////////////
  // http components client
  //////////////////////////////////////////////////////////////////////

  public static HttpResponse1_1 sendClientRequestHttp1_1(HttpUriRequestBase httpUriRequestBase)
      throws Exception {
    RequestConfig requestConfig =
        RequestConfig.custom().setResponseTimeout(Timeout.of(1_000, TimeUnit.SECONDS)).build();

    try (CloseableHttpClient httpClient =
        HttpClients.custom().setDefaultRequestConfig(requestConfig).build()) {
      System.out.printf(
          "[Client] Send request: %s, uri: %s",
          httpUriRequestBase.getMethod(), httpUriRequestBase.getUri());

      return httpClient.execute(
          httpUriRequestBase,
          httpResponse -> {
            HttpResponse1_1 httpResponseBuilder =
                HttpResponse.responseFor(HttpVersion.HTTP_1_1, HttpResponse1_1.class)
                    .statusCode(HttpStatusCode.parseOfCode(httpResponse.getCode()));

            System.out.printf(
                "[Client]: Received response: %s %s \r\n",
                httpResponse.getCode(), httpResponse.getReasonPhrase());

            HttpEntity responseEntity = httpResponse.getEntity();
            if (responseEntity != null) {
              System.out.printf(
                  "[Client] Response content length: %s \r\n", responseEntity.getContentLength());

              MediaType mediaType = MediaType.parseOfMediaType(responseEntity.getContentType());
              byte[] contentByteArray = EntityUtils.toByteArray(responseEntity);
              String contentString = new String(contentByteArray);
              httpResponseBuilder.addBody(contentByteArray, mediaType);

              System.out.printf("[Client] Response body: %s \r\n", contentString);

              org.apache.hc.core5.function.Supplier<List<? extends Header>> supplierTrailers =
                  responseEntity.getTrailers();
              if (supplierTrailers != null) {
                List<? extends Header> responseTrailers = supplierTrailers.get();
                System.out.printf("[Client] Trailers in response: %s \r\n", responseTrailers);

                for (Header responseTrailer : responseTrailers) {
                  httpResponseBuilder.addTrailer(
                      responseTrailer.getName(), responseTrailer.getValue());
                }
              }
            }
            return httpResponseBuilder;
          });
    }
  }

  public static HttpResponse1_1 sendClientRequestHttps1_1(
      HttpUriRequestBase httpUriRequestBase, SSLContext sslContext) throws Exception {

    RequestConfig requestConfig =
        RequestConfig.custom().setResponseTimeout(Timeout.of(1_000, TimeUnit.SECONDS)).build();
    DefaultClientTlsStrategy defaultClientTlsStrategy = new DefaultClientTlsStrategy(sslContext);

    try (PoolingHttpClientConnectionManager connectionManager =
            PoolingHttpClientConnectionManagerBuilder.create()
                .setTlsSocketStrategy(defaultClientTlsStrategy)
                .build();
        CloseableHttpClient httpClient =
            HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .build()) {
      System.out.printf(
          "[Client] Send request: %s, uri: %s",
          httpUriRequestBase.getMethod(), httpUriRequestBase.getUri());

      return httpClient.execute(
          httpUriRequestBase,
          httpResponse -> {
            HttpResponse1_1 httpResponseBuilder =
                HttpResponse.responseFor(HttpVersion.HTTP_1_1, HttpResponse1_1.class)
                    .statusCode(HttpStatusCode.parseOfCode(httpResponse.getCode()));

            System.out.printf(
                "[Client]: Received response: %s %s \r\n",
                httpResponse.getCode(), httpResponse.getReasonPhrase());

            HttpEntity responseEntity = httpResponse.getEntity();
            if (responseEntity != null) {
              System.out.printf(
                  "[Client] Response content length: %s \r\n", responseEntity.getContentLength());

              MediaType mediaType = MediaType.parseOfMediaType(responseEntity.getContentType());
              byte[] contentByteArray = EntityUtils.toByteArray(responseEntity);
              String contentString = new String(contentByteArray);
              httpResponseBuilder.addBody(contentByteArray, mediaType);

              System.out.printf("[Client] Response body: %s \r\n", contentString);

              org.apache.hc.core5.function.Supplier<List<? extends Header>> supplierTrailers =
                  responseEntity.getTrailers();
              if (supplierTrailers != null) {
                List<? extends Header> responseTrailers = supplierTrailers.get();
                System.out.printf("[Client] Trailers in response: %s \r\n", responseTrailers);

                for (Header responseTrailer : responseTrailers) {
                  httpResponseBuilder.addTrailer(
                      responseTrailer.getName(), responseTrailer.getValue());
                }
              }
            }
            return httpResponseBuilder;
          });
    }
  }

  /** Возвращает абсолютный путь файла с директории ресурсов, если он существует. */
  public static String getAbsoluteStringFilePath(String fileName) throws URISyntaxException {
    return Path.of(
                    Objects.requireNonNull(TestUtil.class.getClassLoader().getResource(fileName)).toURI())
        .toAbsolutePath()
        .toString();
  }
}
