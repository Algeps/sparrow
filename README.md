# Sparrow

### HTTP-сервер с аутентификацией и поддержкой GOST-TLS (при указании пути к библиотекам Крипто ПРО)

#### Сервер написан на Java версии 21. Для обработки запросов используется паттерн [thread-per-request на виртуальных потоках](https://docs.oracle.com/en/java/javase/21/core/virtual-threads.html).

---

## Описание:

* [Архитектура](#архитектура-сервера)
* [Диаграмма классов http-парсера](#диаграмма-классов-синтаксического-анализатора-http-запросов-http-парсера)
* [Диаграмма классов компонентов и подкомпонентов "Worker"](#диаграмма-классов-компонентов-и-подкомпонентов-worker-для-обработки-статического-контента-с-диска-по-протоколу-http)
* [Диаграмма последовательности обработки запроса](#диаграмма-последовательности-для-запроса-статического-контента-по-протоколу-http)
* [Пример конфигурации сервера](#пример-конфигурации-сервера)
* [Скрин консольного запуска](#пример-консольного-запуска)

---

## Архитектура сервера

![architecture](docs/image/architecture_components_diagram.png)

#### Описание архитектуры:

* **ServerConfig** - компонент, отвечающий за загрузку и хранение конфигурации приложения.
* **WorkerFactory** - компонент, создающий экземпляры компонентов «Worker» на основе загруженной конфигурации.
* **ServerApplication** - компонент, управляющий жизненным циклом приложения, включая получение конфигурации от
  «ServerConfig», создание контекста приложения (содержащего компоненты «Worker» и другие настройки) с использованием
  «WorkerFactory», запуск компонентов «Worker» и завершение работы приложения с корректным освобождением ресурсов.
* **Worker** - компонент системы, обрабатывающий все входящие подключения на определенном сетевом порту. Таких
  компонентов
  может быть несколько, для обработки одним приложением нескольких протоколов.
* **Server** - компонент, который взаимодействует с операционной системой для приема и перенаправления входящих
  соединений
  на обработку. Он выполняет функцию точки входа для внешних запросов и направляет их на соответствующий обработчик
  запросов.
* **RequestProcessor** - компонент, выполняющий обработку входящих запросов в рамках установленного соединения
  по-определённому протокол.
* **Dispatcher** - компонент, осуществляющий маршрутизацию запроса к соответствующему обработчику. Перед маршрутизацией
  данный компонент проверяет наличие фильтра для данного маршрута и, при его наличии, передает запрос сначала фильтру, а
  затем конечному обработчику.
* **RequestFilter** - компонент, проверяющий запрос на соответствие определенным критериям, осуществляя «фильтрацию»
  запроса, поступившего от клиента.
* **RequestHandler** - компонент, выполняющий конечную обработку запроса, поступающих от клиента, реализуя основную
  логику приложения и являясь последним звеном в процессе обработки запросов

## Диаграмма классов синтаксического анализатора HTTP-запросов (HTTP-парсера)

![http parser](./docs/image/http_parser_class_diagram.png)

## Диаграмма классов компонентов и подкомпонентов "Worker" для обработки статического контента с диска по протоколу HTTP

![http parser](./docs/image/full_worker_class_diagram.png)

## Диаграмма последовательности для запроса статического контента по протоколу HTTP

![http parser](./docs/image/http_request_static_content_sequence_diagram.png)

## Пример конфигурации сервера:

Содержимое файла "config.json":

```json
{
  "workers": [
    {
      "name": "simple static file processor",
      "port": 8080,
      "protocolConfig": {
        "protocol": "HTTP_1_1"
      },
      "filtersConfig": [],
      "handlersConfig": [
        {
          "handlerType": "HTTP_STATIC_CONTENT",
          "path": "/**",
          "httpMethods": [
            "GET"
          ],
          "dirs": [
            "./"
          ]
        }
      ]
    }
  ]
}
```

Данная конфигурация запускает один обработчик порта с именем "simple static file processor" на порту по протоколу"
HTTP/1.1" с обработкой статического контента на запросы методом "GET".

#### Примеры конфигурации находятся в директории "example_configs"

## Пример консольного запуска:

![Запуск в windows](./docs/image/start_console_log.png)
