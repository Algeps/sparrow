package ru.algeps.sparrow.worker.server;

import ru.algeps.sparrow.worker.server.state.DescriptionServerState;

public interface Server {
  /** Запускает обработчик порта. */
  void start();

  /** Возвращает состояние сервера с описанием. */
  DescriptionServerState getServerState();

  /** Завершает работу сервера. Метод может вызываться несколько раз. */
  void stop();
}
