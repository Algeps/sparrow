package ru.algeps.sparrow.util.security;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Provider;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public abstract class SecurityContextLoad {

  static {
    Security.addProvider(new BouncyCastleProvider());
    Security.setProperty("crypto.policy", "unlimited");
  }

  public static void loadSecurityProvider(List<String> stringFilePaths, String providerName)
      throws SecurityContextLoadProviderException {
    try {

      List<URL> urlList = new ArrayList<>();
      for (String stringFilePath : stringFilePaths) {
        if (stringFilePath.endsWith("*")) {
          stringFilePath = stringFilePath.substring(0, stringFilePath.length() - 2);
        }

        Path absoluteFilePath = Paths.get(stringFilePath).toAbsolutePath();
        File absoluteFile = absoluteFilePath.toFile();

        if (absoluteFile.isDirectory()) {
          File[] jarFiles = absoluteFile.listFiles(file -> file.getName().endsWith(".jar"));
          if (jarFiles == null) {
            throw new SecurityContextLoadProviderException(stringFilePath);
          }
          // поиск всех файлов в директории

          for (File jarFile : jarFiles) {
            URL url = jarFile.toPath().toUri().toURL();
            urlList.add(url);
          }
        } else if (absoluteFile.isFile()) {
          URL url = absoluteFilePath.toUri().toURL();
          urlList.add(url);
        } else {
          throw new SecurityContextLoadProviderException(stringFilePath);
        }
      }

      URL[] urls = urlList.toArray(URL[]::new);
      if (!loadProviders(urls, providerName)) {
        throw new SecurityContextLoadProviderException(stringFilePaths.toString(), providerName);
      }
    } catch (Exception e) {
      if (!(e instanceof SecurityContextLoadProviderException)) {
        throw new SecurityContextLoadProviderException(stringFilePaths.toString(), providerName, e);
      }
      throw (SecurityContextLoadProviderException) e;
    }
  }

  private static boolean loadProviders(URL[] urls, String providerName) {
    ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
    URLClassLoader urlClassLoader = URLClassLoader.newInstance(urls, contextClassLoader);
    Thread.currentThread().setContextClassLoader(urlClassLoader);

    ServiceLoader<Provider> serviceLoader = ServiceLoader.load(Provider.class, urlClassLoader);
    for (Provider provider : serviceLoader) {
      if (provider.getName().equals(providerName)) {
        Security.insertProviderAt(provider, 0);
        return true;
      }
    }

    return false;
  }

  public static class SecurityContextLoadProviderException extends RuntimeException {
    public SecurityContextLoadProviderException(
        String stringFilePath, String providerName, Throwable throwable) {
      super(
          "Load error security provider name: '%s', path: '%s'"
              .formatted(providerName, stringFilePath),
          throwable);
    }

    public SecurityContextLoadProviderException(String stringFilePath) {
      super("Not found file for provider path: '%s'".formatted(stringFilePath));
    }

    public SecurityContextLoadProviderException(String stringFilePath, String providerName) {
      super(
          "Not found security provider name: '%s', path: '%s'"
              .formatted(providerName, stringFilePath));
    }
  }
}
