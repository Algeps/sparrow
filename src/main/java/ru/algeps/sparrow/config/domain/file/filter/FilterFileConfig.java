package ru.algeps.sparrow.config.domain.file.filter;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.Objects;
import ru.algeps.sparrow.config.domain.filter.RequestFilterTypeConfig;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "filterType",
    visible = true)
@JsonSubTypes({@JsonSubTypes.Type(value = BasicHttpFilterFileConfig.class, name = "HTTP_BASIC")})
public class FilterFileConfig {
  protected RequestFilterTypeConfig filterType;

  public FilterFileConfig() {}

  public RequestFilterTypeConfig getFilterType() {
    return filterType;
  }

  public void setFilterType(RequestFilterTypeConfig filterType) {
    this.filterType = filterType;
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof FilterFileConfig that)) return false;
    return filterType == that.filterType;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(filterType);
  }

  @Override
  public String toString() {
    return "FilterFileConfig{" + "filterType=" + filterType + '}';
  }
}
