package ru.algeps.sparrow.util.security;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.security.Provider;
import java.security.Security;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.algeps.sparrow.test_utils.SimpleSecurityProvider;
import ru.algeps.sparrow.test_utils.TestUtil;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class SecurityContextLoadTest {
  @Test
  void test_loadSecurityProvider_jarFile() throws URISyntaxException {
    final String jarPath =
        TestUtil.getAbsoluteStringFilePath("jar_samples/SimpleSecurityProvider.jar");
    final String providerName = SimpleSecurityProvider.PROVIDER_NAME;

    // загружаем и регистрируем первый раз
    Assertions.assertDoesNotThrow(
        () -> SecurityContextLoad.loadSecurityProvider(List.of(jarPath), providerName));

    Provider provider = Security.getProvider(providerName);
    assertNotNull(provider);
  }

  @Test
  void test_loadSecurityProvider_dir() throws URISyntaxException {
    final String jarPath =
        Path.of(TestUtil.getAbsoluteStringFilePath("jar_samples/SimpleSecurityProvider.jar"))
                .getParent()
                .toString()
            + "\\*";
    final String providerName = SimpleSecurityProvider.PROVIDER_NAME;

    // загружаем и регистрируем первый раз
    Assertions.assertDoesNotThrow(
        () -> SecurityContextLoad.loadSecurityProvider(List.of(jarPath), providerName));

    Provider provider = Security.getProvider(providerName);
    assertNotNull(provider);
  }
}
