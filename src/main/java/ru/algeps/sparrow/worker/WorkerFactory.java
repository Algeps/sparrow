package ru.algeps.sparrow.worker;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import ru.algeps.sparrow.config.Protocol;
import ru.algeps.sparrow.config.domain.WorkerConfig;
import ru.algeps.sparrow.config.domain.filter.BasicHttpRequestFilterConfig;
import ru.algeps.sparrow.config.domain.filter.HttpRequestFilterConfig;
import ru.algeps.sparrow.config.domain.filter.RequestFilterConfig;
import ru.algeps.sparrow.config.domain.filter.RequestFilterTypeConfig;
import ru.algeps.sparrow.config.domain.handler.HttpRequestHandlerConfig;
import ru.algeps.sparrow.config.domain.handler.RequestHandlerConfig;
import ru.algeps.sparrow.config.domain.handler.StaticContentHttpRequestHandlerConfig;
import ru.algeps.sparrow.config.domain.protocol.HttpsProtocolConfig;
import ru.algeps.sparrow.config.domain.protocol.ProtocolConfig;
import ru.algeps.sparrow.config.domain.provider.ProviderConfig;
import ru.algeps.sparrow.message.HttpVersion;
import ru.algeps.sparrow.util.FileUtil;
import ru.algeps.sparrow.util.hashfunction.HashFunction;
import ru.algeps.sparrow.util.security.SecurityContextLoad;
import ru.algeps.sparrow.util.security.config.KeyManagerConfig;
import ru.algeps.sparrow.util.security.config.KeyStoreConfig;
import ru.algeps.sparrow.util.security.config.TrustManagerConfig;
import ru.algeps.sparrow.worker.dispatcher.http.HttpDispatcher;
import ru.algeps.sparrow.worker.dispatcher.http.simple.SimpleHttpDispatcher;
import ru.algeps.sparrow.worker.handler.http.HttpRequestHandler;
import ru.algeps.sparrow.worker.handler.http.httpstaticcontent.SimpleStaticContentHttpRequestHandler;
import ru.algeps.sparrow.worker.processor.RequestProcessor;
import ru.algeps.sparrow.worker.processor.http.HttpRequestProcessor;
import ru.algeps.sparrow.worker.requestfilter.http.BasicAuthHttp1_1RequestFilter;
import ru.algeps.sparrow.worker.requestfilter.http.HttpRequestFilter;
import ru.algeps.sparrow.worker.requestrouter.RequestRouter;
import ru.algeps.sparrow.worker.server.Server;
import ru.algeps.sparrow.worker.server.ServerFactory;
import ru.algeps.sparrow.worker.server.config.ServerConfig;
import ru.algeps.sparrow.worker.server.stcp.STcpServerConfig;
import ru.algeps.sparrow.worker.server.tcp.TcpServerConfig;

public final class WorkerFactory {
  private WorkerFactory() {}

  public static Worker create(WorkerConfig workerConfig) throws WorkerCreateException {
    RequestProcessor requestProcessor = getRequestProcessor(workerConfig);
    Server server = getServer(workerConfig, requestProcessor);

    return new Worker(workerConfig.name(), server);
  }

  /**
   * В зависимости от конфигурации возвращает RequestProcessor.
   *
   * @see ru.algeps.sparrow.worker.processor.RequestProcessor
   */
  private static RequestProcessor getRequestProcessor(WorkerConfig workerConfig)
      throws WorkerCreateException {
    Protocol protocol = workerConfig.protocolConfig().getProtocol();
    return switch (protocol) {
      case HTTP_1_1, HTTPS_1_1 ->
          new HttpRequestProcessor(
              workerConfig.name(), protocolToHttp(protocol), getHttpDispatcher(workerConfig));
      case null -> throw new WorkerCreateException("Cannot find handler");
    };
  }

  private static HttpVersion protocolToHttp(Protocol protocol) {
    return switch (protocol) {
      case HTTP_1_1, HTTPS_1_1 -> HttpVersion.HTTP_1_1;
      case null -> throw new WorkerCreateException("Cannot find handler");
    };
  }

  // todo на данный момент возвращает только одного SimpleHttpDispatcher. Нужно добавить
  //    Dispatcher без фильтра, и отдельно с одним handler
  private static HttpDispatcher getHttpDispatcher(WorkerConfig workerConfig)
      throws WorkerCreateException {
    try {
      List<SimpleHttpDispatcher.HttpFilterConfig> httpFilterConfig =
          workerConfig.requestFilterConfig().stream()
              .map(requestFilterConfig -> getHttpFilterConfig(requestFilterConfig, workerConfig))
              .toList();

      List<SimpleHttpDispatcher.HttpRouteConfig> httpRouteConfigList =
          workerConfig.requestHandlerConfigList().stream()
              .map(
                  requestHandlerConfig ->
                      getHttpRouteConfig(
                          workerConfig,
                          requestHandlerConfig,
                          protocolToHttp(workerConfig.protocolConfig().getProtocol())))
              .toList();

      return new SimpleHttpDispatcher(httpFilterConfig, httpRouteConfigList);
    } catch (RequestRouter.InsertingInRequestRouterException e) {
      throw new WorkerCreateException(e);
    }
  }

  /**
   * Первая строка настроек: <realm>(пробел)<Путь обработки>(пробел)<Список HTTP-методов для
   * фильтрации>
   */
  private static SimpleHttpDispatcher.HttpFilterConfig getHttpFilterConfig(
      RequestFilterConfig requestFilterConfig, WorkerConfig workerConfig) {
    if (!(requestFilterConfig instanceof HttpRequestFilterConfig httpRequestFilterConfig)) {
      throw new WorkerCreateException(
          "Incorrect configuration in worker: %s! Only http authentication is applied to the HTTP protocol!"
              .formatted(workerConfig.name()));
    }

    HttpRequestFilter httpRequestFilter =
        createHttpRequestFilter(requestFilterConfig, workerConfig.name());
    return new SimpleHttpDispatcher.HttpFilterConfig(
        httpRequestFilterConfig.getPath(), httpRequestFilter);
  }

  private static HttpRequestFilter createHttpRequestFilter(
      RequestFilterConfig requestFilterConfig, String workerName) {
    RequestFilterTypeConfig requestFilterTypeConfig = requestFilterConfig.getRequestFilterType();
    return switch (requestFilterTypeConfig) {
      case HTTP_BASIC -> {
        BasicHttpRequestFilterConfig basicHttpRequestFilterConfig =
            (BasicHttpRequestFilterConfig) requestFilterConfig;
        yield createBasicAuthHttp1_1RequestFilter(basicHttpRequestFilterConfig, workerName);
      }
    };
  }

  /**
   * В desctiption описание метода идёт следующим образом: <путь обработчика>(пробел)<Http-методы
   * через запятую><br>
   */
  private static SimpleHttpDispatcher.HttpRouteConfig getHttpRouteConfig(
      WorkerConfig workerConfig,
      RequestHandlerConfig requestHandlerConfig,
      HttpVersion httpVersion) {

    if (!(requestHandlerConfig instanceof HttpRequestHandlerConfig httpRequestHandlerConfig)) {
      throw new WorkerCreateException(
          "Incorrect configuration in worker: %s! Not HTTP handler is applied to the HTTP protocol!"
              .formatted(workerConfig.name()));
    }

    HttpRequestHandler httpRequestHandler =
        switch (httpVersion) {
          case HTTP_1_1 ->
              createHttpRequestHandler(workerConfig.name(), httpRequestHandlerConfig, httpVersion);
        };

    return new SimpleHttpDispatcher.HttpRouteConfig(
        httpRequestHandlerConfig.getPath(), httpRequestHandler);
  }

  private static HttpRequestHandler createHttpRequestHandler(
      String name, HttpRequestHandlerConfig httpRequestHandlerConfig, HttpVersion httpVersion) {
    return switch (httpRequestHandlerConfig.getHandlerType()) {
      case HTTP_STATIC_CONTENT -> {
        StaticContentHttpRequestHandlerConfig staticContentHttpRequestHandlerConfig =
            (StaticContentHttpRequestHandlerConfig) httpRequestHandlerConfig;
        yield createStaticContentHttpRequestHandler(
            name, httpVersion, staticContentHttpRequestHandlerConfig);
      }
      case null ->
          throw new IllegalArgumentException(
              "Not found handler=[null] for processor=[%s]".formatted(name));
    };
  }

  /** В первой строке указывается корневая директория */
  private static HttpRequestHandler createStaticContentHttpRequestHandler(
      String name,
      HttpVersion httpVersion,
      StaticContentHttpRequestHandlerConfig staticContentHttpRequestHandlerConfig) {
    List<String> dirs = staticContentHttpRequestHandlerConfig.getDirs();
    if (dirs == null) {
      throw new WorkerCreateException(
          "Incorrect config! Null dirs for static handler. Worker: %s".formatted(name));
    }

    if (dirs.size() == 1) {
      return new SimpleStaticContentHttpRequestHandler(name, dirs.getFirst(), httpVersion);
    }

    throw new WorkerCreateException(
        "List directories for static handler are not supported! Worker: %s".formatted(name));
  }

  /** Третье и последующие: <имя>:<пароль> */
  private static BasicAuthHttp1_1RequestFilter createBasicAuthHttp1_1RequestFilter(
      BasicHttpRequestFilterConfig basicHttpRequestFilterConfig, String workerName) {

    List<BasicAuthHttp1_1RequestFilter.BasicAuthCredential> basicAuthCredentials =
        new ArrayList<>();

    List<String> credentialFiles = basicHttpRequestFilterConfig.getCredentialFiles();
    if (credentialFiles == null) {
      throw new WorkerCreateException(
          "Incorrect configuration! For http basic filter config contains null credential files! Worker: %s"
              .formatted(workerName));
    }

    for (String credentialFile : credentialFiles) {
      if (credentialFile == null || credentialFile.isEmpty()) {
        throw new WorkerCreateException(
            "Incorrect configuration! For http basic filter config contains null or empty credential file name in list! Worker: %s"
                .formatted(workerName));
      }

      Path pathCredentialFile = Path.of(credentialFile);
      Stream<String> lineStream = FileUtil.readAllStringFile(pathCredentialFile);
      List<BasicAuthHttp1_1RequestFilter.BasicAuthCredential> fromFileBasicAuthCredentials =
          getBasicAuthCredentials(lineStream, credentialFile);
      basicAuthCredentials.addAll(fromFileBasicAuthCredentials);
    }

    HashFunction hashFunction =
        HashFunction.getHashFunction(basicHttpRequestFilterConfig.getHashAlgorithm());
    if (hashFunction == null) {
      throw new WorkerCreateException(
          "Incorrect configuration! For http basic filter config contains unsupported hash algorithm or null! Worker: %s"
              .formatted(workerName));
    }

    return new BasicAuthHttp1_1RequestFilter(
        basicHttpRequestFilterConfig.getRealm(),
        basicHttpRequestFilterConfig.getHttpMethods(),
        hashFunction,
        basicAuthCredentials);
  }

  private static List<BasicAuthHttp1_1RequestFilter.BasicAuthCredential> getBasicAuthCredentials(
      Stream<String> credentialStream, String credentialFile) {
    if (credentialStream == null) {
      throw new WorkerCreateException("Not found file=[%s]".formatted(credentialFile));
    }

    return credentialStream
        .map(
            line -> {
              String[] credentialStrings = line.split(":");
              String username = credentialStrings[0];
              String password = credentialStrings[1];

              return new BasicAuthHttp1_1RequestFilter.BasicAuthCredential(username, password);
            })
        .toList();
  }

  /** Возвращает Сервер (транспорт для Worker) */
  private static Server getServer(WorkerConfig workerConfig, RequestProcessor requestProcessor)
      throws WorkerCreateException {

    ServerConfig serverConfig = mapToServerConfig(workerConfig, requestProcessor);
    return ServerFactory.create(workerConfig.protocolConfig().getProtocol(), serverConfig);
  }

  private static ServerConfig mapToServerConfig(
      WorkerConfig workerConfig, RequestProcessor requestProcessor) throws WorkerCreateException {
    ProtocolConfig protocolConfig = workerConfig.protocolConfig();

    return switch (protocolConfig.getProtocol()) {
      case HTTP_1_1 ->
          new TcpServerConfig(workerConfig.name(), workerConfig.port(), requestProcessor);
      case HTTPS_1_1 -> {
        HttpsProtocolConfig httpsProtocolConfig = (HttpsProtocolConfig) protocolConfig;

        String replacedName = workerConfig.name().replaceAll(" ", "_");
        String keyStorePassword = System.getProperty(replacedName + ".keystore.password");
        String keyManagerPassword =
            System.getProperty(replacedName + ".keystore.private_key.password");

        loadProviders(httpsProtocolConfig.getProviderConfig());

        KeyStoreConfig keyStoreConfig =
            new KeyStoreConfig(
                httpsProtocolConfig.getKeystorePath(),
                keyStorePassword,
                httpsProtocolConfig.getKeyStoreType());
        TrustManagerConfig trustManagerConfig =
            new TrustManagerConfig(keyStoreConfig, httpsProtocolConfig.getTrustManagerAlgorithm());
        KeyManagerConfig keyManagerConfig =
            new KeyManagerConfig(
                keyStoreConfig, keyManagerPassword, httpsProtocolConfig.getKeyManagerAlgorithm());

        yield new STcpServerConfig(
            workerConfig.name(),
            workerConfig.port(),
            requestProcessor,
            httpsProtocolConfig.getStrictHostname(),
            httpsProtocolConfig.getSslAlgorithm(),
            trustManagerConfig,
            keyManagerConfig,
            httpsProtocolConfig.getSecureRandomAlgorithm());
      }
      case null -> throw new WorkerCreateException("Unsupported protocol!");
    };
  }

  private static void loadProviders(ProviderConfig providerConfig) {
    for (String providerName : providerConfig.getProviderNameList()) {
      SecurityContextLoad.loadSecurityProvider(
          providerConfig.getProviderFilePathList(), providerName);
    }
  }

  public static class WorkerCreateException extends RuntimeException {
    public WorkerCreateException(Throwable cause) {
      super(cause);
    }

    public WorkerCreateException(String message) {
      super(message);
    }
  }
}
