package ru.algeps.sparrow.config.domain.filter;

import ru.algeps.sparrow.message.request.domain.HttpMethod;
import ru.algeps.sparrow.util.hashfunction.HashFunction;

import java.util.List;
import java.util.Objects;

public class BasicHttpRequestFilterConfig extends HttpRequestFilterConfig {
  private final String realm;
  private final List<HttpMethod> httpMethods;
  private final List<String> credentialFiles;
  private final HashFunction.HashAlgorithm hashAlgorithm;

  public BasicHttpRequestFilterConfig(
      RequestFilterTypeConfig filterType,
      String path,
      String realm,
      List<HttpMethod> httpMethods,
      List<String> credentialFiles,
      HashFunction.HashAlgorithm hashAlgorithm) {
    super(filterType, path);
    this.realm = realm;
    this.httpMethods = List.copyOf(httpMethods);
    this.credentialFiles = credentialFiles;
    this.hashAlgorithm = hashAlgorithm;
  }

  public String getRealm() {
    return realm;
  }

  public List<HttpMethod> getHttpMethods() {
    return httpMethods;
  }

  public List<String> getCredentialFiles() {
    return credentialFiles;
  }

  public HashFunction.HashAlgorithm getHashAlgorithm() {
    return hashAlgorithm;
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof BasicHttpRequestFilterConfig that)) return false;
    if (!super.equals(object)) return false;
    return Objects.equals(realm, that.realm)
        && Objects.equals(httpMethods, that.httpMethods)
        && Objects.equals(credentialFiles, that.credentialFiles)
        && hashAlgorithm == that.hashAlgorithm;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), realm, httpMethods, credentialFiles, hashAlgorithm);
  }

  @Override
  public String toString() {
    return "BasicHttpRequestFilterConfig{"
        + "hashAlgorithm="
        + hashAlgorithm
        + ", path='"
        + path
        + '\''
        + ", requestFilterType="
        + requestFilterType
        + ", realm='"
        + realm
        + '\''
        + ", httpMethods="
        + httpMethods
        + ", credentialFiles="
        + credentialFiles
        + '}';
  }
}
