package ru.algeps.sparrow.worker.server.state;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class DescriptionServerState {
  private volatile ServerState serverState;
  private volatile Exception exception;
  private volatile int firstUsePort;
  private volatile Set<Integer> usesPort;

  public DescriptionServerState() {
    this.serverState = ServerState.CREATED;
  }

  public void setStartingState() {
    this.serverState = ServerState.STARTING;
  }

  public void setRunningState() {
    this.serverState = ServerState.RUNNING;
  }

  public void setStoppedState() {
    this.serverState = ServerState.STOPPED;
  }

  public void setErrorState(Exception exception) {
    this.serverState = ServerState.ERROR;
    this.exception = exception;
  }

  public Exception getException() {
    return exception;
  }

  public ServerState getServerState() {
    return serverState;
  }

  public boolean isRunning() {
    return this.serverState == ServerState.RUNNING;
  }

  public void addUsePort(int port) {
    if (usesPort == null) {
      this.firstUsePort = port;
      this.usesPort = new LinkedHashSet<>();
      this.usesPort.add(port);
    }
  }

  /** Возвращает -1, если порт не замаплен. */
  public int getFirstUsePort() {
    return firstUsePort;
  }

  /** Возвращает список используемых портов. */
  public List<Integer> getUsesPort() {
    return List.copyOf(usesPort);
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof DescriptionServerState that)) return false;
    return serverState == that.serverState && Objects.equals(exception, that.exception);
  }

  @Override
  public int hashCode() {
    return Objects.hash(serverState, exception);
  }

  @Override
  public String toString() {
    return "DescriptionServerState{"
        + "serverState="
        + serverState
        + ", exception="
        + exception
        + '}';
  }
}
