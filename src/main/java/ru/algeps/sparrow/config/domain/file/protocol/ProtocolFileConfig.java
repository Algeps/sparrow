package ru.algeps.sparrow.config.domain.file.protocol;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import ru.algeps.sparrow.config.Protocol;
import ru.algeps.sparrow.config.domain.file.handler.StaticContentHttpHandlerFileConfig;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "protocol",
    visible = true)
@JsonSubTypes({
  @JsonSubTypes.Type(value = HttpProtocolFileConfig.class, name = "HTTP_1_1"),
  @JsonSubTypes.Type(value = HttpsProtocolFileConfig.class, name = "HTTPS_1_1")
})
public class ProtocolFileConfig {
  protected Protocol protocol;

  public ProtocolFileConfig() {}

  public Protocol getProtocol() {
    return protocol;
  }

  public void setProtocol(Protocol protocol) {
    this.protocol = protocol;
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof ProtocolFileConfig that)) return false;
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
