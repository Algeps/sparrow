package ru.algeps.sparrow.config.domain.file.handler;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import ru.algeps.sparrow.config.domain.RequestHandlerTypeConfig;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "handlerType",
    visible = true)
@JsonSubTypes({
  @JsonSubTypes.Type(value = StaticContentHttpHandlerFileConfig.class, name = "HTTP_STATIC_CONTENT")
})
public class HandlerFileConfig {
  protected RequestHandlerTypeConfig handlerType;

  public HandlerFileConfig() {}

  public HandlerFileConfig(RequestHandlerTypeConfig handlerType) {
    this.handlerType = handlerType;
  }

  public RequestHandlerTypeConfig getHandlerType() {
    return handlerType;
  }

  public void setHandlerType(RequestHandlerTypeConfig handlerType) {
    this.handlerType = handlerType;
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof HandlerFileConfig that)) return false;
    return handlerType == that.handlerType;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(handlerType);
  }

  @Override
  public String toString() {
    return "HandlerFileConfig{" + "handlerType=" + handlerType + '}';
  }
}
