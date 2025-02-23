package ru.algeps.sparrow.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

public final class ObjectJsonMapper {
  private static final ObjectMapper objectMapper = new ObjectMapper();

  private ObjectJsonMapper() {}

  public static String toJson(Object object) throws ObjectJsonMapperException {
    try {
      return objectMapper.writeValueAsString(object);
    } catch (IOException e) {
      throw new ObjectJsonMapperException(e);
    }
  }

  public static <T> T toObject(byte[] byteValue, Class<T> clazz) throws ObjectJsonMapperException {
    try {
      return objectMapper.readValue(byteValue, clazz);
    } catch (IOException e) {
      throw new ObjectJsonMapperException(e);
    }
  }

  public static <T> T toObject(String jsonValue, Class<T> clazz) throws ObjectJsonMapperException {
    try {
      return objectMapper.readValue(jsonValue, clazz);
    } catch (IOException e) {
      throw new ObjectJsonMapperException(e);
    }
  }

  public static class ObjectJsonMapperException extends IOException {
    public ObjectJsonMapperException(Throwable cause) {
      super(cause);
    }
  }
}
