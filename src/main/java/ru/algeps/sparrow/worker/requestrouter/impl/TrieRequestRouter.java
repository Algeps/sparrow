package ru.algeps.sparrow.worker.requestrouter.impl;

import static ru.algeps.sparrow.message.request.parser.http.AsciiAlphabet.SLASH;

import java.util.*;
import ru.algeps.sparrow.util.FileUtil;
import ru.algeps.sparrow.worker.requestrouter.RequestRouter;

/** Тип данных Бор (префиксное дерево). */
public class TrieRequestRouter<T> implements RequestRouter<T> {
  private static final char ROOT_CHAR = SLASH;
  private final Node<T> root;

  public TrieRequestRouter() {
    root = new Node<>(ROOT_CHAR, false, null, new HashMap<>());
  }

  /**
   * Добавляет путь для обработки. Например: '/api/v1' -> requestHandler1, '/api/v2' ->
   * requestHandler2.
   *
   * <pre>
   *  Можно применять следующий синтаксис:
   *  - Указание чёткого пути к файлу: '/api/v1/index.html'
   *  - Указание чёткого пути к папке: '/api/v1/' будет эквивалентно '/api/v1/*'
   *  Данный путь будет относится только к url, которые будут вложены в данную папку
   *  (url='/api/v1' - будет отождествлено к конкретным файлом, а не папкой)
   *  - Указание вложенного пути к папке: '/api/v1/**' все папки, файлы и все под папки
   *  - Указание обработки некоторых расширений файлов: '*.jpg' или '/data/*.jpg'
   * </pre>
   *
   * <pre>
   *  Например:
   *   *) '/api/*' и '/api/data'
   *   *) '/api/**' и '/api/www/data'
   * </pre>
   *
   * @param path путь, который будет обрабатывать обработчик
   * @param handler обработчик для указанного пути
   */
  @Override
  public void insertHandler(String path, T handler) throws InsertingInRequestRouterException {
    if (path == null || path.isEmpty()) {
      throw new IllegalArgumentException("Path should not be empty");
    }

    if (handler == null) {
      throw new IllegalArgumentException("RequestHandler should not be null");
    }

    if (path.charAt(0) != ROOT_CHAR) {
      throw new IllegalArgumentException("Path must be start with '/'");
    }

    if (path.equals("/*") || path.equals("/**")) {
      root.setRequestHandler(handler);
    }

    Node<T> currentNode = doInsertRequestHandler(path);
    currentNode.setEnd(true);
    currentNode.setRequestHandler(handler);
  }

  /**
   *
   *
   * <pre>
   *  currentNode - текущая нода
   *  childNode - потомок currentNode, который был выбран исходя из условий
   *  pathCurrentIndex - индекс текущего символа в path
   *  Шаги выполнения:
   *  *) если путь null или он пустой, то выбрасывается исключение
   *  *) если обработчик == null, то выбрасывается исключение
   *  *) если путь начинается не начинается на '/', то выбрасывается исключение
   *  *) в цикле:
   *    *)
   * </pre>
   *
   * @param path путь для вставки
   * @return последняя нода, которая была добавлена в Trie
   * @throws InsertingInRequestRouterException если имеется пересечение путей
   */
  private Node<T> doInsertRequestHandler(String path) throws InsertingInRequestRouterException {
    // todo дописать doc
    Node<T> currentNode = root;
    Node<T> childNode;
    boolean isUpdate = false;

    char[] pathCharArray = path.toCharArray();
    int index = 1;
    while (index < pathCharArray.length) {
      char c = pathCharArray[index];
      childNode = currentNode.getChild(c);
      index++;
      if (childNode != null) {
        currentNode = childNode;
        throwIfIntersectingPath(currentNode, path, pathCharArray, index);
      } else {
        Node<T> newNode = new Node<>(c, false, null);
        currentNode.addChild(newNode);
        currentNode = newNode;

        isUpdate = true;
      }
    }

    // todo проверить в тестах на дубликат
    if (!isUpdate) {
      throw new InsertingInRequestRouterException(
          "Duplicate path=[%s] (path already exist in RequestRouter)".formatted(path));
    }

    // todo добавлять одну звёздочку, если путь заканчивается на '/'

    return currentNode;
  }

  /** Выбрасывает исключение, если уже существует путь, который обрабатывает более общий путь. */
  private void throwIfIntersectingPath(
      Node<T> currentNode, String path, char[] pathCharArray, int index)
      throws InsertingInRequestRouterException {
    if (currentNode.getChild('*') != null) {
      if (currentNode.getChild('*').getChild('*') != null) {
        throw new InsertingInRequestRouterException(
            "Path [%s] intersects with [%s**]".formatted(path, path.substring(0, index)));
      }
      if (!checkingThatThereIsStillSlashNext(pathCharArray, index)) {
        throw new InsertingInRequestRouterException(
            "Path [%s] intersects with [%s*]".formatted(path, path.substring(0, index)));
      }
    }
  }

  /**
   * Проверяет, имеется ли от текущего индекса и дальше завершение директории (SLASH '/'). То или
   * иное, имеется ли дальше в пути SLASH.
   *
   * <pre>
   *  Пример:
   *  1) pathCharArray = /data/www
   *     index = 6
   *     Метод вернёт false
   *  2) /data/*
   *     index = 2
   *     Метод вернёт true
   * </pre>
   */
  private boolean checkingThatThereIsStillSlashNext(char[] pathCharArray, int index) {
    if (index == pathCharArray.length) {
      return false;
    }

    while (index < pathCharArray.length) {
      char c = pathCharArray[index];
      if (c == '/') {
        return true;
      }
      index++;
    }
    return false;
  }

  /**
   * Проходит от начала до конца.
   *
   * <pre>
   *  Алгоритм:
   *  1) Сначала проходимся по вершине, где указан путь '*.<любой формат файла>'. Если есть совпадение, то
   *  возвращаем обработчик.
   *  2) Если нет пути, начинающегося на *, то выполняется цикл хождения по бору с сопоставлением пути.
   *    *)
   * </pre>
   *
   * @return null, если обработчик отсутствует.
   */
  @Override
  public T getHandlerByPath(String path) {
    if (path == null) {
      throw new IllegalArgumentException("Path should not be empty");
    }

    if (path.charAt(0) != ROOT_CHAR) {
      throw new IllegalArgumentException("Path must be start with '/'");
    }

    if (path.length() == 1) {
      return root.getRequestHandler();
    }

    return doGetRequestHandlerByPath(path);
  }

  private T doGetRequestHandlerByPath(String path) {
    Node<T> currentNode = root;
    Node<T> child = null;
    int index = 0;

    char c;
    char[] pathCharArray = path.toCharArray();
    while (index < pathCharArray.length) {
      c = pathCharArray[index];
      index++;

      if (c != '/') {
        return null;
      }

      // сначала проверка на *
      if (currentNode.getChild('*') != null) {
        // проверка на **
        if (currentNode.getChild('*').getChild('*') != null) {
          if (currentNode.getChild('*').getChild('*').getChild('.') != null) {
            currentNode = currentNode.getChild('*').getChild('*').getChild('.');
            String fileExtension = FileUtil.getFileExtension(path);
            if (!fileExtension.isBlank()) {
              return checkFileExtension(fileExtension, currentNode);
            }
          }
          return currentNode.getChild('*').getChild('*').getRequestHandler();
        }
        // если дальше по пути слэша нет
        if (!checkingThatThereIsStillSlashNext(pathCharArray, index)) {
          if (currentNode.getChild('*').getChild('.') != null) {
            currentNode = currentNode.getChild('*').getChild('.');
            String fileExtension = FileUtil.getFileExtension(path);
            if (!fileExtension.isBlank()) {
              return checkFileExtension(fileExtension, currentNode);
            }
          }

          return currentNode.getChild('*').getRequestHandler();
        }
        break;
      }

      while (index < pathCharArray.length) {
        c = pathCharArray[index];
        index++;

        // проверяет, есть ли в пути сопоставление по дереву
        child = currentNode.getChild(c);
        if (child == null) {
          return null;
        }

        currentNode = child;

        if (c == '/') {
          index--;
          break;
        }
      }
    }
    return child == null ? null : child.getRequestHandler();
  }

  private T checkFileExtension(String fileExtension, Node<T> currentNode) {
    Node<T> child;
    for (char c : fileExtension.toCharArray()) {
      child = currentNode.getChild(c);
      if (child == null) {
        return null;
      }
      currentNode = child;
    }

    return currentNode.getRequestHandler();
  }

  /**
   * Ищет полное сопоставление по пути (1 в 1 как путь лежит в дереве). Не обрабатывает путь, а
   * только возвращает существующий обработчик для данного пути.
   *
   * <pre>
   *  Например:
   *  В дереве лежит обработчик, который обрабатывает указанный путь: "/data/**"
   *
   *  Вызов метода findByPathWithoutRoute("/data/**") вернёт true,
   *  а вызов метода findByPathWithoutRoute("/data/image.png") вернёт false.
   * </pre>
   */
  protected T findByPathWithoutRoute(String path) {
    if (path == null) {
      throw new IllegalArgumentException("Path should not be empty");
    }

    if (path.charAt(0) != ROOT_CHAR) {
      throw new IllegalArgumentException("Path must be start with '/'");
    }

    Node<T> currentNode = root;
    T handler = null;

    char[] pathCharArray = path.toCharArray();
    int index = 1;
    while (index < pathCharArray.length) {
      char c = pathCharArray[index];

      if (currentNode.getChild(c) != null) {
        handler = currentNode.getChild(c).getRequestHandler();
      } else {
        handler = null;
        break;
      }

      currentNode = currentNode.getChild(c);
      index++;
    }

    return handler;
  }

  private static class Node<T> {
    private final char aChar;
    private boolean isEnd;
    private T handler;
    private final Map<Character, Node<T>> childNodes;

    public Node(char aChar, boolean isEnd, T handler) {
      this(aChar, isEnd, handler, new HashMap<>());
    }

    public Node(char aChar, boolean isEnd, T handler, Map<Character, Node<T>> childNodes) {
      this.aChar = aChar;
      this.isEnd = isEnd;
      this.handler = handler;
      this.childNodes = childNodes == null ? new HashMap<>() : childNodes;
    }

    public void setEnd(boolean isEnd) {
      this.isEnd = isEnd;
    }

    public void setRequestHandler(T handler) {
      this.handler = handler;
    }

    public void addChild(Node<T> node) {
      childNodes.put(node.getChar(), node);
    }

    public Node<T> getChild(char aChar) {
      return childNodes.get(aChar);
    }

    public char getChar() {
      return aChar;
    }

    public boolean isEnd() {
      return isEnd;
    }

    public T getRequestHandler() {
      return handler;
    }

    @Override
    public String toString() {
      return new StringJoiner(", ", this.getClass().getSimpleName() + "[", "]")
          .add("aChar=" + aChar)
          .add("isEnd=" + isEnd)
          .add("requestHandler=" + handler)
          .add(childNodes.toString())
          .toString();
    }
  }
}
