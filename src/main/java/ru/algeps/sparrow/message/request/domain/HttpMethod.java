package ru.algeps.sparrow.message.request.domain;

public enum HttpMethod {
  GET("GET"),
  HEAD("HEAD"),
  POST("POST"),
  PUT("PUT"),
  DELETE("DELETE"),
  CONNECT("CONNECT"),
  OPTIONS("OPTIONS"),
  TRACE("TRACE");

  final String name;

  HttpMethod(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
