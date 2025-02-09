package ru.algeps.sparrow.worker.processor;

import java.io.IOException;
import java.nio.channels.ByteChannel;

/** Обрабатывает входящие запросы. Наследники могут реализовать различные протоколы. */
public interface RequestProcessor {
  /** Метод вызывается один раз и должен обрабатывать все сообщения, которые придут на обработку. */
  void handle(ByteChannel byteChannel) throws IOException;
}
