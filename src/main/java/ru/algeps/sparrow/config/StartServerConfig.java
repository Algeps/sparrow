package ru.algeps.sparrow.config;

import ru.algeps.sparrow.config.domain.Config;
import ru.algeps.sparrow.config.exception.StartServerConfigException;
import ru.algeps.sparrow.config.reader.ConfigReader;

/** Загружает и формирует данные необходимые для запуска сервера. */
public class StartServerConfig {
  private StartServerConfig() {}

  private static final String DEFAULT_CONFIG_FILE_PATH = "config.json";

  /**
   * Загружает настройки. Если не указаны значения, то устанавливает значения по умолчанию. Парсит
   * аргументы командой строки.
   */
  public static Config parseAndLoadConfig(String... args) throws StartServerConfigException {
    String configFileName = getConfigFileName(args);
    boolean inFile = true;

    if (inFile) {
      try {
        return ConfigReader.create(ConfigReader.ConfigReaderType.JSON_FILE).load(configFileName);
      } catch (Exception e) {
        throw new StartServerConfigException(e);
      }
    } else {
      throw new StartServerConfigException("Cannot load config");
    }
  }

  private static String getConfigFileName(String... args) {
    if (args.length > 1 && args[0] != null) {
      return args[0];
    }

    return DEFAULT_CONFIG_FILE_PATH;
  }
}
