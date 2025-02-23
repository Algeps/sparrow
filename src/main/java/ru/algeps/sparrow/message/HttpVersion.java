package ru.algeps.sparrow.message;

import ru.algeps.sparrow.message.request.domain.HttpRequest;
import ru.algeps.sparrow.message.request.domain.HttpRequest1_1;
import ru.algeps.sparrow.message.response.domain.HttpResponse;
import ru.algeps.sparrow.message.response.domain.HttpResponse1_1;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public enum HttpVersion {
  HTTP_1_1(
      "HTTP/1.1", HttpRequest1_1.Builder1_1.class, HttpRequest1_1.class, HttpResponse1_1.class);

  private final String version;
  private final byte[] representationInBytes;
  private final Class<? extends HttpRequest.Builder> requestBuilderClass;
  private final Class<? extends HttpRequest> requestClass;
  private final Class<? extends HttpResponse> responseHandlerClass;

  HttpVersion(
      String version,
      Class<? extends HttpRequest.Builder> requestBuilderClass,
      Class<? extends HttpRequest> requestClass,
      Class<? extends HttpResponse> responseHandlerClass) {
    this.version = version;
    this.representationInBytes = version.getBytes(StandardCharsets.UTF_8);
    this.requestBuilderClass = requestBuilderClass;
    this.requestClass = requestClass;
    this.responseHandlerClass = responseHandlerClass;
  }

  public String getVersion() {
    return version;
  }

  public byte[] getRepresentationInBytes() {
    return representationInBytes;
  }

  public static String supportedVersionArrayString() {
    return Arrays.toString(HttpVersion.values());
  }

  public Class<? extends HttpResponse> getResponseHandlerClass() {
    return responseHandlerClass;
  }

  public Class<? extends HttpRequest.Builder> getRequestBuilderClass() {
    return requestBuilderClass;
  }

  public Class<? extends HttpRequest> getRequestClass() {
    return requestClass;
  }
}
