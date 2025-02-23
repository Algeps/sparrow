package ru.algeps.sparrow.config.domain.file.handler;

import java.util.List;
import java.util.Objects;

public class StaticContentHttpHandlerFileConfig extends HttpHandlerFileConfig {
  private List<String> dirs;

  public StaticContentHttpHandlerFileConfig() {}

  public List<String> getDirs() {
    return dirs;
  }

  public void setDirs(List<String> dirs) {
    this.dirs = dirs;
  }

  @Override
  public boolean equals(Object object) {

    if (!(object instanceof StaticContentHttpHandlerFileConfig that)) return false;
    if (!super.equals(object)) return false;
    return Objects.equals(dirs, that.dirs);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), dirs);
  }

  @Override
  public String toString() {
    return "StaticContentHttpHandlerFileConfig{"
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
