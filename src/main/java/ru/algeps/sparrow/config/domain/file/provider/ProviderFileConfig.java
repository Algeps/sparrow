package ru.algeps.sparrow.config.domain.file.provider;

import java.util.List;
import java.util.Objects;

public class ProviderFileConfig {
  private List<String> providerNameList;
  private List<String> providerFilePathList;

  public List<String> getProviderNameList() {
    return providerNameList;
  }

  public void setProviderNameList(List<String> providerNameList) {
    this.providerNameList = providerNameList;
  }

  public List<String> getProviderFilePathList() {
    return providerFilePathList;
  }

  public void setProviderFilePathList(List<String> providerFilePathList) {
    this.providerFilePathList = providerFilePathList;
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof ProviderFileConfig that)) return false;
    return Objects.equals(providerNameList, that.providerNameList)
        && Objects.equals(providerFilePathList, that.providerFilePathList);
  }

  @Override
  public int hashCode() {
    return Objects.hash(providerNameList, providerFilePathList);
  }

  @Override
  public String toString() {
    return "ProviderFileConfig{"
        + "providerNameList="
        + providerNameList
        + ", providerFilePathList="
        + providerFilePathList
        + '}';
  }
}
