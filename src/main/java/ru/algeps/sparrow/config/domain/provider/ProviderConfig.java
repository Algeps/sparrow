package ru.algeps.sparrow.config.domain.provider;

import java.util.List;
import java.util.Objects;

public class ProviderConfig {
  private final List<String> providerNameList;
  protected final List<String> providerFilePathList;

  public ProviderConfig() {
    this.providerNameList = List.of();
    this.providerFilePathList = List.of();
  }

  public ProviderConfig(List<String> providerNameList, List<String> providerFilePathList) {
    this.providerNameList = providerNameList;
    this.providerFilePathList = providerFilePathList == null ? List.of() : providerFilePathList;
  }

  public List<String> getProviderNameList() {
    return providerNameList;
  }

  public List<String> getProviderFilePathList() {
    return providerFilePathList;
  }

  public boolean isEmpty() {
    return providerNameList.isEmpty() && providerFilePathList.isEmpty();
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof ProviderConfig that)) return false;
    return Objects.equals(providerNameList, that.providerNameList)
        && Objects.equals(providerFilePathList, that.providerFilePathList);
  }

  @Override
  public int hashCode() {
    return Objects.hash(providerNameList, providerFilePathList);
  }

  @Override
  public String toString() {
    return "ProviderConfig{"
        + "providerNameList="
        + providerNameList
        + ", providerFilePathList="
        + providerFilePathList
        + '}';
  }
}
