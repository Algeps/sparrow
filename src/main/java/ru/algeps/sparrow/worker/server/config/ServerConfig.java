package ru.algeps.sparrow.worker.server.config;

import ru.algeps.sparrow.worker.processor.RequestProcessor;

public class ServerConfig {
  protected final String name;
  protected final Integer port;
  protected final RequestProcessor requestProcessor;

  public ServerConfig(String name, Integer port, RequestProcessor requestProcessor) {
    this.name = name;
    this.port = port;
    this.requestProcessor = requestProcessor;
  }

  public String getName() {
    return name;
  }

  public Integer getPort() {
    return port;
  }

  public RequestProcessor getRequestProcessor() {
    return requestProcessor;
  }
}
