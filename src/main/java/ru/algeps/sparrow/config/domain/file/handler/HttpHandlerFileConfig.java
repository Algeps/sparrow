package ru.algeps.sparrow.config.domain.file.handler;

import java.util.List;
import java.util.Objects;

import ru.algeps.sparrow.message.request.domain.HttpMethod;

public class HttpHandlerFileConfig extends HandlerFileConfig {
  protected String path;
  protected List<HttpMethod> httpMethods;

  public HttpHandlerFileConfig() {}

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public List<HttpMethod> getHttpMethods() {
    return httpMethods;
  }

  public void setHttpMethods(List<HttpMethod> httpMethods) {
    this.httpMethods = httpMethods;
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof HttpHandlerFileConfig that)) return false;
    if (!super.equals(object)) return false;
    return Objects.equals(path, that.path) && Objects.equals(httpMethods, that.httpMethods);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), path, httpMethods);
  }

  @Override
  public String toString() {
    return "HttpHandlerFileConfig{"
        + "path='"
        + path
        + '\''
        + ", httpMethods="
        + httpMethods
        + ", handlerType="
        + handlerType
        + '}';
  }
}
