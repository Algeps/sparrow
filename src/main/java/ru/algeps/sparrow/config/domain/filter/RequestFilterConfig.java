package ru.algeps.sparrow.config.domain.filter;

import java.util.Objects;

public abstract class RequestFilterConfig {
  protected RequestFilterTypeConfig requestFilterType;

  public RequestFilterConfig(RequestFilterTypeConfig requestFilterType) {
    this.requestFilterType = requestFilterType;
  }

  public RequestFilterTypeConfig getRequestFilterType() {
    return requestFilterType;
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof RequestFilterConfig that)) return false;
    return requestFilterType == that.requestFilterType;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(requestFilterType);
  }

  @Override
  public String toString() {
    return "RequestFilterConfig{" + "requestFilterType=" + requestFilterType + '}';
  }
}
