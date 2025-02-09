package ru.algeps.sparrow.config.domain.file.filter;

import java.util.Objects;

public class HttpFilterFileConfig extends FilterFileConfig {
  protected String path;

  public HttpFilterFileConfig() {}

  public void setPath(String path) {
    this.path = path;
  }

  public String getPath() {
    return path;
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof HttpFilterFileConfig that)) return false;
    if (!super.equals(object)) return false;
    return Objects.equals(path, that.path);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), path);
  }

  @Override
  public String toString() {
    return "HttpFilterFileConfig{" + "path='" + path + '\'' + ", filterType=" + filterType + "}";
  }
}
