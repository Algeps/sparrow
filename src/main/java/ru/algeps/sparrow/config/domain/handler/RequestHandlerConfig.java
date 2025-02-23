package ru.algeps.sparrow.config.domain.handler;

import ru.algeps.sparrow.config.domain.RequestHandlerTypeConfig;

import java.util.Objects;

public abstract class RequestHandlerConfig {
  protected RequestHandlerTypeConfig handlerType;

  public RequestHandlerConfig(RequestHandlerTypeConfig handlerType) {
    this.handlerType = handlerType;
  }

  public RequestHandlerTypeConfig getHandlerType() {
    return handlerType;
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof RequestHandlerConfig that)) return false;
    return handlerType == that.handlerType;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(handlerType);
  }

  @Override
  public String toString() {
    return "RequestHandlerConfig{" + "handlerType=" + handlerType + '}';
  }
}
