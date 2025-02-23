package ru.algeps.sparrow.response.domain;

import static org.junit.jupiter.api.Assertions.*;
import static ru.algeps.sparrow.message.response.domain.HttpResponse.HTTP_DATE_TIME_FORMAT;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import ru.algeps.sparrow.context.Constants;
import ru.algeps.sparrow.message.request.domain.mediatype.MediaType;
import ru.algeps.sparrow.message.response.domain.HttpResponse;
import ru.algeps.sparrow.message.response.domain.HttpResponse1_1;
import ru.algeps.sparrow.message.response.domain.HttpStatusCode;

class HttpResponse1_1Test {
  /** Сравнивает текстовые тела, кроме поля Date. */
  @Test
  void toByteBuffer_NotContent_thenSuccess() {
    String expectedResponseString =
        """
                    HTTP/1.1 200 OK\r
                    Server:%s\r
                    Date:%s\r
                    \r
                    """
            .formatted(Constants.SERVER_NAME, LocalDateTime.now().format(HTTP_DATE_TIME_FORMAT));

    HttpResponse httpResponse = new HttpResponse1_1();
    httpResponse.statusCode(HttpStatusCode.OK);
    String actualResponseString = new String(httpResponse.toByteBuffer().array());

    assertEquals(expectedResponseString.substring(0, 60), actualResponseString.substring(0, 60));
    assertEquals(expectedResponseString.substring(63), actualResponseString.substring(63));
  }

  @Test
  void toByteBuffer_WithContent_thenSuccess() {
    String expectedResponseString =
        """
                        HTTP/1.1 200 OK\r
                        Server:%s\r
                        Date:%s\r
                        Content-type:text/plain; charset=utf-8\r
                        Content-length:13\r
                        \r
                        Hello, World!"""
            .formatted(Constants.SERVER_NAME, LocalDateTime.now().format(HTTP_DATE_TIME_FORMAT));

    HttpResponse httpResponse = new HttpResponse1_1();
    httpResponse
        .statusCode(HttpStatusCode.OK)
        .addBody("Hello, World!".getBytes(StandardCharsets.UTF_8), MediaType.Text.PLAIN);
    String actualResponseString = new String(httpResponse.toByteBuffer().array());

    assertEquals(expectedResponseString.substring(0, 60), actualResponseString.substring(0, 60));
    assertEquals(expectedResponseString.substring(63), actualResponseString.substring(63));
  }

  @Test
  void test_DefaultConstructor() {
    HttpResponse1_1 response = new HttpResponse1_1();
    String serverHeader = response.headers().getFirst("Server");
    assertTrue(
        serverHeader.contains("Sparrow"),
        () -> "Header no contain 'Server' value. Actual:" + serverHeader);
  }

  @Test
  void test_AddHeader() {
    HttpResponse1_1 response = new HttpResponse1_1();
    response.addHeader("Content-Type", "text/html");
    assertEquals("text/html", response.headers().getFirst("Content-Type"));
  }

  @Test
  void test_AddBody() {
    HttpResponse1_1 response = new HttpResponse1_1();
    byte[] body = "Hello, world!".getBytes(StandardCharsets.UTF_8);
    response.addBody(body, MediaType.Text.PLAIN);
    assertEquals("text/plain; charset=utf-8", response.headers().getFirst("Content-Type"));
    assertEquals("13", response.headers().getFirst("Content-Length"));
  }

  @Test
  void test_AddChunk() {
    HttpResponse1_1 response = new HttpResponse1_1();
    byte[] chunk = "Chunk data".getBytes(StandardCharsets.UTF_8);
    response.addChunk(chunk);
    assertTrue(response.headers().containsKey("Transfer-Encoding"));
    assertEquals("chunked", response.headers().getFirst("Transfer-Encoding"));
  }

  @Test
  void test_ReplaceChunk() {
    HttpResponse1_1 response = new HttpResponse1_1();
    byte[] chunk = "New chunk data".getBytes(StandardCharsets.UTF_8);
    response.replaceChunk(chunk);
    assertTrue(response.headers().containsKey("Transfer-Encoding"));
    assertEquals("chunked", response.headers().getFirst("Transfer-Encoding"));
  }

  @Test
  void test_AddTrailer() {
    HttpResponse1_1 response = new HttpResponse1_1();
    response.addTrailer("Trailer-Name", "Trailer-Value");
    assertTrue(response.headers().containsKey("Trailer"));
    assertEquals("Trailer-Name", response.headers().getFirst("Trailer"));
  }

  @Test
  void test_ToByteBuffer() {
    HttpResponse1_1 response = new HttpResponse1_1();
    response.addBody(
        "{\"key\":\"value\"}".getBytes(StandardCharsets.UTF_8), MediaType.Application.JSON);

    ByteBuffer buffer = response.toByteBuffer();
    String result = new String(buffer.array(), StandardCharsets.UTF_8);

    assertTrue(
        result.contains("Content-type"),
        () -> "ByteBuffer no contain header 'Content-Type':" + result);
    assertTrue(
        result.contains("application/json"),
        () -> "ByteBuffer no contain header value 'application/json' of 'Content-Type':" + result);
    assertTrue(
        result.contains("{\"key\":\"value\"}"),
        () -> "ByteBuffer no contain conent '{\"key\":\"value\"}':" + result);
  }

  @Test
  void test_CopyConstructor() {
    HttpResponse1_1 response = new HttpResponse1_1();
    response.addHeader("X-Test", "Value");
    HttpResponse1_1 copiedResponse = response.copy();

    assertNotSame(response, copiedResponse);
    assertEquals("Value", copiedResponse.headers().getFirst("X-Test"));
  }
}
