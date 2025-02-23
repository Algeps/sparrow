package ru.algeps.sparrow.config.reader;

import java.nio.file.Path;
import java.util.List;
import ru.algeps.sparrow.config.Protocol;
import ru.algeps.sparrow.config.domain.Config;
import ru.algeps.sparrow.config.domain.WorkerConfig;
import ru.algeps.sparrow.config.domain.file.ConfigFile;
import ru.algeps.sparrow.config.domain.file.WorkerFileConfig;
import ru.algeps.sparrow.config.domain.file.filter.BasicHttpFilterFileConfig;
import ru.algeps.sparrow.config.domain.file.filter.FilterFileConfig;
import ru.algeps.sparrow.config.domain.file.handler.HandlerFileConfig;
import ru.algeps.sparrow.config.domain.file.handler.StaticContentHttpHandlerFileConfig;
import ru.algeps.sparrow.config.domain.file.protocol.HttpsProtocolFileConfig;
import ru.algeps.sparrow.config.domain.file.protocol.ProtocolFileConfig;
import ru.algeps.sparrow.config.domain.file.provider.ProviderFileConfig;
import ru.algeps.sparrow.config.domain.filter.BasicHttpRequestFilterConfig;
import ru.algeps.sparrow.config.domain.filter.RequestFilterConfig;
import ru.algeps.sparrow.config.domain.handler.RequestHandlerConfig;
import ru.algeps.sparrow.config.domain.handler.StaticContentHttpRequestHandlerConfig;
import ru.algeps.sparrow.config.domain.protocol.HttpProtocolConfig;
import ru.algeps.sparrow.config.domain.protocol.HttpsProtocolConfig;
import ru.algeps.sparrow.config.domain.protocol.ProtocolConfig;
import ru.algeps.sparrow.config.domain.provider.ProviderConfig;
import ru.algeps.sparrow.util.FileUtil;
import ru.algeps.sparrow.util.ObjectJsonMapper;

public class JsonFileConfigReader implements ConfigReader {

  @Override
  public Config load(String... params) throws ConfigReaderException {
    String pathFileString = params[0];
    Path path = Path.of(pathFileString);

    byte[] bytes = FileUtil.readFile(path);
    if (bytes == null) {
      throw new ConfigReaderException(
          "Cannot read file! File=[%s] not found!".formatted(pathFileString));
    }

    try {
      ConfigFile configFile = ObjectJsonMapper.toObject(bytes, ConfigFile.class);
      return configFileToConfig(configFile);
    } catch (ObjectJsonMapper.ObjectJsonMapperException e) {
      throw new JsonFileConfigReaderException(e);
    }
  }

  private Config configFileToConfig(ConfigFile configFile) {
    Config config = new Config();

    for (WorkerFileConfig workerFileConfig : configFile.getListWorkerFileConfig()) {
      List<RequestFilterConfig> requestFilterConfigList =
          workerFileConfig.getFiltersConfig().stream().map(this::mapToRequestFilterConfig).toList();

      List<RequestHandlerConfig> requestHandlerConfigList =
          workerFileConfig.getHandlersConfig().stream()
              .map(this::mapToRequestHandlerConfig)
              .toList();

      ProtocolConfig protocolConfig = mapToProtocolConfig(workerFileConfig.getProtocolConfig());

      WorkerConfig workerConfig =
          new WorkerConfig(
              workerFileConfig.getName(),
              protocolConfig,
              workerFileConfig.getPort(),
              requestFilterConfigList,
              requestHandlerConfigList);

      config.addHandlerConfigs(workerFileConfig.getName(), workerConfig);
    }

    return config;
  }

  private ProtocolConfig mapToProtocolConfig(ProtocolFileConfig protocolConfig) {
    Protocol protocol = protocolConfig.getProtocol();
    return switch (protocol) {
      case HTTP_1_1 -> new HttpProtocolConfig(protocol);
      case HTTPS_1_1 -> {
        HttpsProtocolFileConfig httpsProtocolFileConfig = (HttpsProtocolFileConfig) protocolConfig;
        yield new HttpsProtocolConfig(
            protocol,
            httpsProtocolFileConfig.getKeystorePath(),
            httpsProtocolFileConfig.getStrictHostname(),
            mapToProviderConfig(httpsProtocolFileConfig.getProviderConfig()),
            httpsProtocolFileConfig.getKeyStoreType(),
            httpsProtocolFileConfig.getSslAlgorithm(),
            httpsProtocolFileConfig.getKeyManagerAlgorithm(),
            httpsProtocolFileConfig.getTrustManagerAlgorithm(),
            httpsProtocolFileConfig.getSecureRandomAlgorithm());
      }
      case null -> null;
    };
  }

  private ProviderConfig mapToProviderConfig(ProviderFileConfig providerFileConfig) {
    return providerFileConfig == null
        ? new ProviderConfig()
        : new ProviderConfig(
            providerFileConfig.getProviderNameList(), providerFileConfig.getProviderFilePathList());
  }

  private RequestFilterConfig mapToRequestFilterConfig(FilterFileConfig filterFileConfig) {
    return switch (filterFileConfig.getFilterType()) {
      case HTTP_BASIC -> {
        BasicHttpFilterFileConfig fileConfig = (BasicHttpFilterFileConfig) filterFileConfig;
        yield new BasicHttpRequestFilterConfig(
            fileConfig.getFilterType(),
            fileConfig.getPath(),
            fileConfig.getRealm(),
            fileConfig.getHttpMethods(),
            fileConfig.getCredentialFiles(),
            fileConfig.getHashAlgorithm());
      }
      case null -> null;
    };
  }

  private RequestHandlerConfig mapToRequestHandlerConfig(HandlerFileConfig handlerFileConfig) {
    return switch (handlerFileConfig.getHandlerType()) {
      case HTTP_STATIC_CONTENT -> {
        StaticContentHttpHandlerFileConfig fileConfig =
            (StaticContentHttpHandlerFileConfig) handlerFileConfig;
        yield new StaticContentHttpRequestHandlerConfig(
            fileConfig.getHandlerType(),
            fileConfig.getPath(),
            fileConfig.getHttpMethods(),
            fileConfig.getDirs());
      }
      case null -> null;
    };
  }

  public static class JsonFileConfigReaderException extends ConfigReaderException {
    public JsonFileConfigReaderException(Throwable cause) {
      super(cause);
    }

    public JsonFileConfigReaderException(String message) {
      super(message);
    }
  }
}
