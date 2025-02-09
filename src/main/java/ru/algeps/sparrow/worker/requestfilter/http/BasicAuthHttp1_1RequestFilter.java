package ru.algeps.sparrow.worker.requestfilter.http;

import java.nio.charset.StandardCharsets;
import java.util.*;
import ru.algeps.sparrow.message.request.domain.HttpMethod;
import ru.algeps.sparrow.message.request.domain.HttpRequest;
import ru.algeps.sparrow.message.request.domain.exceptions.http.clienterror.auth.AuthClientErrorHttpRequestException;
import ru.algeps.sparrow.message.request.domain.exceptions.http.clienterror.auth.ForbiddenHttp1_1RequestException;
import ru.algeps.sparrow.message.request.domain.exceptions.http.clienterror.auth.UnauthorizedHttp1_1RequestException;
import ru.algeps.sparrow.util.hashfunction.HashFunction;

/**
 * Реализует <a href="https://www.rfc-editor.org/info/rfc7617">"The 'Basic' HTTP Authentication
 * Scheme" </a>
 */
public class BasicAuthHttp1_1RequestFilter implements HttpRequestFilter {
  private static final String NAME_HEADER = "Authorization";
  private static final String TYPE_SCHEME = "Basic";

  /** Индекс начала учётных данных в Base64 */
  private static final int START_INDEX_CREDENTIALS = TYPE_SCHEME.length() + 1;

  private final String realm;
  private final HttpMethod[] filterHttpMethod;
  private final Map<String, String> basicAuthCredentialStorage;
  private final HashFunction hashForPasswordFunction;
  private static final Base64.Decoder base64Decoder = Base64.getDecoder();

  public BasicAuthHttp1_1RequestFilter(
      String realm,
      List<HttpMethod> httpMethod,
      HashFunction hashForPasswordFunction,
      List<BasicAuthCredential> basicAuthCredentials) {
    this.filterHttpMethod = httpMethod.toArray(HttpMethod[]::new);
    this.hashForPasswordFunction = hashForPasswordFunction;

    Map<String, String> tempStorage = new HashMap<>();
    for (BasicAuthCredential basicAuthCredential : basicAuthCredentials) {
      tempStorage.put(basicAuthCredential.username(), basicAuthCredential.password());
    }
    this.basicAuthCredentialStorage = Collections.unmodifiableMap(tempStorage);

    this.realm = realm;
  }

  @Override
  public void httpFilter(HttpRequest httpRequest) throws AuthClientErrorHttpRequestException {
    String valueHeader = httpRequest.headers().getFirst(NAME_HEADER);
    if (valueHeader == null || !valueHeader.startsWith(TYPE_SCHEME)) {
      throw new UnauthorizedHttp1_1RequestException(TYPE_SCHEME, realm);
    }

    String[] credential;
    try {
      credential = extractCredential(valueHeader);
    } catch (IllegalArgumentException e) {
      throw new UnauthorizedHttp1_1RequestException(TYPE_SCHEME, realm);
    }

    if (credential.length < 1) {
      throw new UnauthorizedHttp1_1RequestException(TYPE_SCHEME, realm);
    }

    String username = credential[0];
    String password = credential[1];
    String getPassword;
    if ((getPassword = basicAuthCredentialStorage.get(username)) == null) {
      throw new ForbiddenHttp1_1RequestException(credential[0] + ":" + credential[1]);
    }

    if (!hashForPasswordFunction.match(
        getPassword.getBytes(StandardCharsets.UTF_8), password.getBytes(StandardCharsets.UTF_8))) {
      throw new ForbiddenHttp1_1RequestException(credential[0] + ":" + credential[1]);
    }
  }

  private String[] extractCredential(String val) throws IllegalArgumentException {
    val = val.substring(START_INDEX_CREDENTIALS);
    byte[] decode = base64Decoder.decode(val.getBytes(StandardCharsets.UTF_8));
    String tempCredential = new String(decode).intern();
    return tempCredential.split(":");
  }

  @Override
  public HttpMethod[] getFilterHttpMethods() {
    return filterHttpMethod;
  }

  public record BasicAuthCredential(String username, String password) {
    /** Преобразует BasicAuthCredential в username:password. */
    public String toInline() {
      return this.username + ":" + this.password;
    }

    @Override
    public String toString() {
      return "BasicAuthCredential{"
          + "username='"
          + username
          + '\''
          + ", password='"
          + password
          + '\''
          + '}';
    }
  }
}
