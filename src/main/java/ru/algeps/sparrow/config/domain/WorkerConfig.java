package ru.algeps.sparrow.config.domain;

import java.util.List;
import ru.algeps.sparrow.config.domain.filter.RequestFilterConfig;
import ru.algeps.sparrow.config.domain.handler.RequestHandlerConfig;
import ru.algeps.sparrow.config.domain.protocol.ProtocolConfig;

public record WorkerConfig(
    String name,
    ProtocolConfig protocolConfig,
    Integer port,
    List<RequestFilterConfig> requestFilterConfig,
    List<RequestHandlerConfig> requestHandlerConfigList) {}
