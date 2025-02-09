package ru.algeps.sparrow.config.domain;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/** Конфигурирует и создаёт Worker`s. */
public class Config {
  private final Map<String, WorkerConfig> requestHandlerMap;

  public Config() {
    this.requestHandlerMap = new LinkedHashMap<>();
  }

  public void addHandlerConfigs(String name, WorkerConfig requestHandler) {
    this.requestHandlerMap.put(name, requestHandler);
  }

  public Set<Map.Entry<String, WorkerConfig>> entries() {
    return requestHandlerMap.entrySet();
  }
}
