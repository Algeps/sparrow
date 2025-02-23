package ru.algeps.sparrow.message.response.domain;

import java.nio.ByteBuffer;

/**
 * Сообщение, которое можно представить в виде байтового буфера (сериализация в байтовое
 * предоставление).
 */
public interface ByteBufferMessage {
    /** Возвращает готовый ля чтения буфер */
    ByteBuffer toByteBuffer();
}
