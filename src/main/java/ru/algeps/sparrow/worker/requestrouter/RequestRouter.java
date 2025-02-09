package ru.algeps.sparrow.worker.requestrouter;

/** Маршрутизатор запросов. */
public interface RequestRouter<T> {

  /**
   * Добавление на указанный путь обработчик. Унаследованные обработчики могут обрабатывать как
   * указанный путь, так и все под пути, с указанием '*'
   *
   * @param path путь для обработчика запросов
   * @param handler обработчик запросов
   */
  void insertHandler(String path, T handler) throws InsertingInRequestRouterException;

  class InsertingInRequestRouterException extends Exception {
    public InsertingInRequestRouterException(String message) {
      super(message);
    }
  }

  /**
   * Возвращает обработчик исходя из переданного пути.
   *
   * @param path путь
   * @return обработчик, который обрабатывает указанный путь. Если на таком пути обработчика нет, то
   *     возвращается null
   */
  T getHandlerByPath(String path);
}
