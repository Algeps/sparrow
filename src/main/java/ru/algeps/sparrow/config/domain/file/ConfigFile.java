package ru.algeps.sparrow.config.domain.file;

import java.util.ArrayList;
import java.util.List;

public class ConfigFile {
  private List<WorkerFileConfig> workers;

  public ConfigFile() {
    workers = new ArrayList<>();
  }

  public ConfigFile(List<WorkerFileConfig> workerFileConfig) {
    this.workers = workerFileConfig;
  }

  public List<WorkerFileConfig> getListWorkerFileConfig() {
    return workers;
  }

  public void setWorkers(List<WorkerFileConfig> workers) {
    this.workers = workers;
  }
}
