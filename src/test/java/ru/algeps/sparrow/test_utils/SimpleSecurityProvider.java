package ru.algeps.sparrow.test_utils;

import java.security.Provider;

public class SimpleSecurityProvider extends Provider {
  public static final String PROVIDER_NAME = "Simple_Provider_Name";

  public SimpleSecurityProvider() {
    super(PROVIDER_NAME, "1.0.0", "This is a primitive security provider for tests.");
  }
}
