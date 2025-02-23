package ru.algeps.sparrow.config.domain.protocol;

import ru.algeps.sparrow.config.Protocol;

import java.util.Objects;

public class ProtocolConfig {
  protected Protocol protocol;

  public ProtocolConfig(Protocol protocol) {
    this.protocol = protocol;
  }

  public Protocol getProtocol() {
    return protocol;
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof ProtocolConfig that)) return false;
    return protocol == that.protocol;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(protocol);
  }

  @Override
  public String toString() {
    return "ProtocolFileConfig{" + "protocol=" + protocol + '}';
  }
}
