package ru.algeps.sparrow.worker.server.tcp;

import ru.algeps.sparrow.worker.processor.RequestProcessor;
import ru.algeps.sparrow.worker.server.config.ServerConfig;

public class TcpServerConfig extends ServerConfig {
  protected final int backlog;

  public TcpServerConfig(String name, Integer port, RequestProcessor requestProcessor) {
    this(name, port, 50, requestProcessor);
  }

  public TcpServerConfig(
      String name, Integer port, int backlog, RequestProcessor requestProcessor) {
    super(name, port, requestProcessor);
    this.backlog = backlog;
  }

  public int getBacklog() {
    return backlog;
  }
}
