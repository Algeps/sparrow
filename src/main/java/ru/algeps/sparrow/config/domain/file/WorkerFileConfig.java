package ru.algeps.sparrow.config.domain.file;

import java.util.*;
import ru.algeps.sparrow.config.Protocol;
import ru.algeps.sparrow.config.domain.file.filter.FilterFileConfig;
import ru.algeps.sparrow.config.domain.file.handler.HandlerFileConfig;
import ru.algeps.sparrow.config.domain.file.protocol.ProtocolFileConfig;

public class WorkerFileConfig {
  protected String name;
  protected Integer port;
  protected ProtocolFileConfig protocolConfig;
  protected List<FilterFileConfig> filtersConfig;
  protected List<HandlerFileConfig> handlersConfig;

  public WorkerFileConfig() {
    this.protocolConfig = null;
    this.name = "";
    this.port = 0;
    this.filtersConfig = new ArrayList<>();
    this.handlersConfig = new ArrayList<>();
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Integer getPort() {
    return port;
  }

  public void setPort(Integer port) {
    this.port = port;
  }

  public ProtocolFileConfig getProtocolConfig() {
    return protocolConfig;
  }

  public void setProtocolConfig(ProtocolFileConfig protocolConfig) {
    this.protocolConfig = protocolConfig;
  }

  public List<FilterFileConfig> getFiltersConfig() {
    return filtersConfig;
  }

  public void setFiltersConfig(List<FilterFileConfig> filtersConfig) {
    this.filtersConfig = filtersConfig;
  }

  public List<HandlerFileConfig> getHandlersConfig() {
    return handlersConfig;
  }

  public void setHandlersConfig(List<HandlerFileConfig> handlersConfig) {
    this.handlersConfig = handlersConfig;
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof WorkerFileConfig that)) return false;
    return Objects.equals(name, that.name)
        && Objects.equals(port, that.port)
        && Objects.equals(protocolConfig, that.protocolConfig)
        && Objects.equals(filtersConfig, that.filtersConfig)
        && Objects.equals(handlersConfig, that.handlersConfig);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, port, protocolConfig, filtersConfig, handlersConfig);
  }

  @Override
  public String toString() {
    return "WorkerFileConfig{"
        + "name='"
        + name
        + '\''
        + ", port="
        + port
        + ", protocolConfig="
        + protocolConfig
        + ", filtersConfig="
        + filtersConfig
        + ", handlersConfig="
        + handlersConfig
        + '}';
  }
}
