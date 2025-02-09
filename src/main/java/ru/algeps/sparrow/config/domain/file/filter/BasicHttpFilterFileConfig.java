package ru.algeps.sparrow.config.domain.file.filter;

import java.util.List;
import java.util.Objects;
import ru.algeps.sparrow.message.request.domain.HttpMethod;
import ru.algeps.sparrow.util.hashfunction.HashFunction;

public class BasicHttpFilterFileConfig extends HttpFilterFileConfig {
  private String realm;
  private List<HttpMethod> httpMethods;
  private List<String> credentialFiles;
  private HashFunction.HashAlgorithm hashAlgorithm;

  public BasicHttpFilterFileConfig() {}

  public String getRealm() {
    return realm;
  }

  public void setRealm(String realm) {
    this.realm = realm;
  }

  public List<HttpMethod> getHttpMethods() {
    return httpMethods;
  }

  public void setHttpMethods(List<HttpMethod> httpMethods) {
    this.httpMethods = httpMethods;
  }

  public List<String> getCredentialFiles() {
    return credentialFiles;
  }

  public void setCredentialFiles(List<String> credentialFiles) {
    this.credentialFiles = credentialFiles;
  }

  public HashFunction.HashAlgorithm getHashAlgorithm() {
    return hashAlgorithm;
  }

  public void setHashAlgorithm(HashFunction.HashAlgorithm hashAlgorithm) {
    this.hashAlgorithm = hashAlgorithm;
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof BasicHttpFilterFileConfig that)) return false;
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
    return "BasicHttpFilterFileConfig{"
        + "filterType="
        + filterType
        + ", path='"
        + path
        + '\''
        + ", hashAlgorithm="
        + hashAlgorithm
        + ", credentialFiles="
        + credentialFiles
        + ", httpMethods="
        + httpMethods
        + ", realm='"
        + realm
        + '\''
        + '}';
  }
}
