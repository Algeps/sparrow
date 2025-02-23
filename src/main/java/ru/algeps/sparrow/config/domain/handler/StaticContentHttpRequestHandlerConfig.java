package ru.algeps.sparrow.config.domain.handler;

import ru.algeps.sparrow.config.domain.RequestHandlerTypeConfig;
import ru.algeps.sparrow.message.request.domain.HttpMethod;

import java.util.List;
import java.util.Objects;

public class StaticContentHttpRequestHandlerConfig extends HttpRequestHandlerConfig {
  private final List<String> dirs;

  public StaticContentHttpRequestHandlerConfig(
      RequestHandlerTypeConfig handlerType,
      String path,
      List<HttpMethod> httpMethods,
      List<String> dirs) {
    super(handlerType, path, httpMethods);
    this.dirs = List.copyOf(dirs);
  }

  public List<String> getDirs() {
    return dirs;
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof StaticContentHttpRequestHandlerConfig that)) return false;
    if (!super.equals(object)) return false;
    return Objects.equals(dirs, that.dirs);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), dirs);
  }

  @Override
  public String toString() {
    return "StaticContentHttpRequestHandlerConfig{"
        + "dirs="
        + dirs
        + ", path='"
        + path
        + '\''
        + ", httpMethods="
        + httpMethods
        + ", handlerType="
        + handlerType
        + '}';
  }
}
