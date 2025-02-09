package ru.algeps.sparrow.util;

import ru.algeps.sparrow.context.Constants;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

public final class ServerErrorUtil {
  private ServerErrorUtil() {}

  private static final String START_ERROR_PAGE =
      """
            <!DOCTYPE html>
            <html lang="en">
            <head>
            <title>500 Internal Server Error</title>
            </head>
            <body>
            <div align="left">
            <h1>500 Internal Server Error</h1>
            <h2>%s</h2>
            <pre>
            """
          .formatted(Constants.SERVER_NAME);
  private static final String END_ERROR_PAGE =
      """
          </pre>
          </div>
          </body>
          </html>
          """;

  public static byte[] getPageWithException(Exception e) {
    return (START_ERROR_PAGE + getFullStackTrace(e) + END_ERROR_PAGE)
        .getBytes(StandardCharsets.UTF_8);
  }

  public static String getFullStackTrace(Throwable throwable) {
    if (throwable == null) {
      return "";
    }

    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);

    pw.println(throwable);

    for (StackTraceElement stackTraceElement : throwable.getStackTrace()) {
      pw.println("\t" + stackTraceElement);
    }

    for (Throwable suppressed : throwable.getSuppressed()) {
      pw.print("Suppressed: ");
      getFullStackTrace(suppressed, pw);
    }

    Throwable cause = throwable.getCause();
    if (cause != null) {
      pw.print("Caused by: ");
      getFullStackTrace(cause, pw);
    }

    return sw.toString();
  }

  private static void getFullStackTrace(Throwable throwable, PrintWriter pw) {
    throwable.printStackTrace(pw);

    for (Throwable suppressed : throwable.getSuppressed()) {
      pw.print("Suppressed: ");
      getFullStackTrace(suppressed, pw);
    }
  }
}
