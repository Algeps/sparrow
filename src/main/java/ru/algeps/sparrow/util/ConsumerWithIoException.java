package ru.algeps.sparrow.util;

import java.io.IOException;

public interface ConsumerWithIoException<T> {
  void accept(T t) throws IOException;
}
