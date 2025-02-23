package ru.algeps.sparrow.config.domain.filter;

import java.util.Objects;

public abstract class HttpRequestFilterConfig extends RequestFilterConfig {
  protected String path;

  public HttpRequestFilterConfig(RequestFilterTypeConfig filterType, String path) {
    super(filterType);
    this.path = path;
  }

  public String getPath() {
    return path;
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof HttpRequestFilterConfig that)) return false;
    if (!super.equals(object)) return false;
    return Objects.equals(path, that.path);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), path);
  }

  @Override
  public String toString() {
    return "HttpRequestFilterConfig{"
        + "path='"
        + path
        + '\''
        + ", requestFilterType="
        + requestFilterType
        + '}';
  }
}
