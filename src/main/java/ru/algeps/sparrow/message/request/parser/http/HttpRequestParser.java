package ru.algeps.sparrow.message.request.parser.http;

import static ru.algeps.sparrow.message.request.parser.http.AsciiAlphabet.*;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;

import ru.algeps.sparrow.message.request.domain.HttpRequest;
import ru.algeps.sparrow.message.HttpVersion;

/**
 * Класс для парсинга Http запросов. В документации внутри класса в методах парсинга строки
 * управляющих данных и заголовков понятие байт эквивалентно понятие символ, поскольку используется
 * US-ASCII кодировка.
 */
public abstract class HttpRequestParser<B extends HttpRequest.Builder, R extends HttpRequest> {
  //////////////// Массив допустимых символов для названия заголовка ////////////
  /**
   * Поддерживаемые: "!"(33) / "#"(35) / "$"(36) / "%"(37) / "&"(38) / "'"(39) / "*"(42) / "+"(43) /
   * * "-"(45) / "."(46) / "^"(94) / "_"(95) / "`"(96) / "|"(124) / "~"(126) / DIGIT(48-57) / *
   * ALPHA(65-90, 97-122) <br>
   * Информация взята из HTTP DOC: <a href="https://httpwg.org/specs/rfc9110.html#fields.names">HTTP
   * DOC</a><br>
   * <a href="https://www.rfc-editor.org/rfc/rfc5234.html#appendix-B.1">ABNF RFC DOC</a>
   */
  protected static final boolean[] SUPPORTED_ASCII_SYMBOLS_IN_NAME_HEADER_ARRAY =
      new boolean[Byte.MAX_VALUE + 1];

  static {
    SUPPORTED_ASCII_SYMBOLS_IN_NAME_HEADER_ARRAY[EXCLAMATION_MARK] = true;
    SUPPORTED_ASCII_SYMBOLS_IN_NAME_HEADER_ARRAY[NUMBER_SIGN] = true;
    SUPPORTED_ASCII_SYMBOLS_IN_NAME_HEADER_ARRAY[DOLLAR_SIGN] = true;
    SUPPORTED_ASCII_SYMBOLS_IN_NAME_HEADER_ARRAY[PERCENT_SIGN] = true;
    SUPPORTED_ASCII_SYMBOLS_IN_NAME_HEADER_ARRAY[AMPERSAND] = true;
    SUPPORTED_ASCII_SYMBOLS_IN_NAME_HEADER_ARRAY[SINGLE_QUOTE] = true;
    SUPPORTED_ASCII_SYMBOLS_IN_NAME_HEADER_ARRAY[ASTERISK] = true;
    SUPPORTED_ASCII_SYMBOLS_IN_NAME_HEADER_ARRAY[PLUS_SIGN] = true;
    SUPPORTED_ASCII_SYMBOLS_IN_NAME_HEADER_ARRAY[HYPHEN_MINUS] = true;
    SUPPORTED_ASCII_SYMBOLS_IN_NAME_HEADER_ARRAY[DOT] = true;
    SUPPORTED_ASCII_SYMBOLS_IN_NAME_HEADER_ARRAY[CARET] = true;
    SUPPORTED_ASCII_SYMBOLS_IN_NAME_HEADER_ARRAY[UNDERSCORE] = true;
    SUPPORTED_ASCII_SYMBOLS_IN_NAME_HEADER_ARRAY[GRAVE_ACCENT] = true;
    SUPPORTED_ASCII_SYMBOLS_IN_NAME_HEADER_ARRAY[VERTICAL_BAR] = true;
    SUPPORTED_ASCII_SYMBOLS_IN_NAME_HEADER_ARRAY[TILDE] = true;

    for (byte digit = ZERO; digit <= NINE; digit++) {
      SUPPORTED_ASCII_SYMBOLS_IN_NAME_HEADER_ARRAY[digit] = true;
    }

    for (byte alphaUp = A; alphaUp <= Z; alphaUp++) {
      SUPPORTED_ASCII_SYMBOLS_IN_NAME_HEADER_ARRAY[alphaUp] = true;
    }

    for (byte alphaDown = LOWERCASE_A; alphaDown <= LOWERCASE_Z; alphaDown++) {
      SUPPORTED_ASCII_SYMBOLS_IN_NAME_HEADER_ARRAY[alphaDown] = true;
    }
  }

  protected static final int DEFAULT_HEADER_NAME_SIZE = 15;

  //////////// Массив видимых символов (они же используются для значения заголовка) ////////////

  /**
   * Массив видимых символов: %x21-7E (33-126). С пробелом (32) <br>
   * <a href="https://www.rfc-editor.org/rfc/rfc5234.html#appendix-B.1">ABNF RFC DOC</a>
   */
  protected static final boolean[] VISIBLE_CHARACTERS_WITH_SPACE_ARRAY =
      new boolean[Byte.MAX_VALUE + 1];

  static {
    for (byte visibleByte = EXCLAMATION_MARK; visibleByte <= TILDE; visibleByte++) {
      VISIBLE_CHARACTERS_WITH_SPACE_ARRAY[visibleByte] = true;
    }
    VISIBLE_CHARACTERS_WITH_SPACE_ARRAY[SPACE] = true;
  }

  /////////////////////////////// Ограничения (constrains) //////////////////////////
  protected static final int MAX_URI_STRING_SIZE = 8000;
  protected static final int MAX_NAME_HEADER_SIZE = 100;
  protected static final int MAX_VALUE_HEADER_SIZE = 1_000;
  protected static final int DEFAULT_HEADER_VALUE_SIZE = 100;
  protected static final int MAX_CHUNK_SIZE = Integer.MAX_VALUE;
  protected static final int MAX_CONTENT_WITH_CHUNK_SIZE = Integer.MAX_VALUE;
  protected static final int MINIMUM_FOR_CHUNK_CONTENT_SIZE = 8192;
  protected static final int SIZE_MULTIPLIER_FOR_CHUNK_SIZE = 2;

  /** 8 байт в hex формате для int */
  protected static final int HEX_CHARS_PER_INT = 8;

  /////////////////////////////// Основные поля класса ///////////////////////////////
  protected final B httpRequestBuilder;
  protected final ReadableByteConnectionChannel channel;
  protected Long contentLength;

  protected HttpRequestParser(ReadableByteConnectionChannel channel, B httpRequestBuilder) {
    this.httpRequestBuilder = httpRequestBuilder;
    this.channel = channel;
    this.contentLength = null;
  }

  /**
   * Возвращает парсер. Начинает парсинг в зависимости от первоначального протокола.
   *
   * @param version ожидаемый протокол
   * @param readableByteChannel канал, по которому можно получить данные для парсинга
   * @return HttpRequestControlData с заполненными полями или исключение в случае некорректного
   *     синтаксиса.
   */
  public static HttpRequestParser<? extends HttpRequest.Builder, ? extends HttpRequest> connect(
      ReadableByteChannel readableByteChannel, HttpVersion version) throws IOException {
    ReadableByteConnectionChannel channel = new ReadableByteConnectionChannel(readableByteChannel);

    return switch (version) {
      case HTTP_1_1 -> new HttpRequestParser1_1(channel);
    };
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////
  //                                Возврат готового Http запроса
  /////////////////////////////////////////////////////////////////////////////////////////////////

  /** Возвращает Http Запрос. */
  public abstract R getHttpRequest() throws IOException;

  /////////////////////////////////////////////////////////////////////////////////////////////////
  //                        Настройка максимально допустимых значений для размеров
  /////////////////////////////////////////////////////////////////////////////////////////////////
  // todo написать методы настройки
}
