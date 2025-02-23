package ru.algeps.sparrow.util;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

public final class TimeWaitUtil {
  private TimeWaitUtil() {}

  public static void runUntil(
      SupplierWithIoException<Boolean> supplier, Duration duration, Duration checkInterval)
      throws IOException {
    // todo всякие проверки на null

    try {
      Instant start = Instant.now();
      Instant needEnd = start.plus(duration);
      while (!supplier.get()) {
        if (Instant.now().isAfter(needEnd)) {
          break;
        }
        Thread.sleep(checkInterval.toMillis());
      }
    } catch (InterruptedException e) {
      Thread.interrupted();
    }
  }
}
