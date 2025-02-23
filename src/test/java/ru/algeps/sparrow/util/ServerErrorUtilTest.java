package ru.algeps.sparrow.util;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ServerErrorUtilTest {

  @Test
  void test_getFullStackTrace() {
    RuntimeException arithmeticException =
        assertThrows(
            RuntimeException.class,
            () -> {
              try {
                int val = 5 / 0;
              } catch (Exception e) {
                throw new RuntimeException(e);
              }
            });

    String fullStackTrace = ServerErrorUtil.getFullStackTrace(arithmeticException);
    System.out.println(fullStackTrace);
    assertNotNull(fullStackTrace);
  }

  @Test
  void test_getPageWithException() throws IOException {
    RuntimeException arithmeticException =
        assertThrows(
            RuntimeException.class,
            () -> {
              try {
                int val = 5 / 0;
              } catch (Exception e) {
                throw new RuntimeException("Error: \"in test\"", e);
              }
            });

    byte[] pageWithException = ServerErrorUtil.getPageWithException(arithmeticException);
    // Files.writeString(Path.of("error.html"), new String(pageWithException));
  }
}
