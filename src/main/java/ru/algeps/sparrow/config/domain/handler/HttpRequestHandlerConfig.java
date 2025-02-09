package ru.algeps.sparrow.config.domain.handler;

import ru.algeps.sparrow.config.domain.RequestHandlerTypeConfig;
import ru.algeps.sparrow.message.request.domain.HttpMethod;

import java.util.List;
import java.util.Objects;

public abstract class HttpRequestHandlerConfig extends RequestHandlerConfig {
  protected final String path;
  protected final List<HttpMethod> httpMethods;

  public HttpRequestHandlerConfig(
      RequestHandlerTypeConfig handlerType, String path, List<HttpMethod> httpMethods) {
    super(handlerType);
    this.path = path;
    this.httpMethods = httpMethods;
  }

  public String getPath() {
    return path;
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof HttpRequestHandlerConfig that)) return false;
    if (!super.equals(object)) return false;
    return Objects.equals(path, that.path);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), path);
  }

  @Override
  public String toString() {
    return "HttpRequestHandlerConfig{"
        + "path='"
        + path
        + '\''
        + ", handlerType="
        + handlerType
        + '}';
  }
}
