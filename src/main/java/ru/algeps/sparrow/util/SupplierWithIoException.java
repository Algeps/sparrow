package ru.algeps.sparrow.util;

import java.io.IOException;

@FunctionalInterface
public interface SupplierWithIoException<T> {

  T get() throws IOException;
}
