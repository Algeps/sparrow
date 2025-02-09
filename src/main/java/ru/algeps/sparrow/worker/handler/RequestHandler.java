package ru.algeps.sparrow.worker.handler;

import java.io.IOException;
import java.nio.channels.ByteChannel;

public interface RequestHandler {
  // todo доабвить ещё одни Handler, который может запрашивать с другого ресурса (REVERSE_PROXY)
  void handle(ByteChannel channel) throws IOException;
}
