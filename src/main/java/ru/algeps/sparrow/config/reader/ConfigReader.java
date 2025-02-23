package ru.algeps.sparrow.config.reader;

import ru.algeps.sparrow.config.domain.Config;

import java.io.IOException;

public interface ConfigReader {
  Config load(String... params) throws ConfigReaderException;

  static ConfigReader create(ConfigReaderType configReaderType) throws ConfigReaderException {
    return switch (configReaderType) {
      case JSON_FILE -> new JsonFileConfigReader();
      case null -> throw new ConfigReaderException("Not found config");
    };
  }

  enum ConfigReaderType {
    JSON_FILE;
  }

  class ConfigReaderException extends Exception {
    public ConfigReaderException() {}

    public ConfigReaderException(String message) {
      super(message);
    }

    public ConfigReaderException(String message, Throwable cause) {
      super(message, cause);
    }

    public ConfigReaderException(Throwable cause) {
      super(cause);
    }
  }
}
