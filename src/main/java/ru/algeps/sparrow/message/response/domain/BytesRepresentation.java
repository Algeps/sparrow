package ru.algeps.sparrow.message.response.domain;

/** Сериализация в поток байтов. */
public interface BytesRepresentation {
  /** Возвращает байтовое представление */
  byte[] toBytes();
}
