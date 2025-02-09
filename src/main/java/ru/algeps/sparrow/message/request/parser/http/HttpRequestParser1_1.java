package ru.algeps.sparrow.message.request.parser.http;

import static ru.algeps.sparrow.message.request.parser.http.AsciiAlphabet.*;
import static ru.algeps.sparrow.message.request.parser.http.AsciiAlphabet.LINE_FEED;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import ru.algeps.sparrow.message.FieldName;
import ru.algeps.sparrow.message.HttpVersion;
import ru.algeps.sparrow.message.request.domain.HttpMethod;
import ru.algeps.sparrow.message.request.domain.HttpRequest;
import ru.algeps.sparrow.message.request.domain.HttpRequest1_1;
import ru.algeps.sparrow.message.request.domain.exceptions.http.clienterror.BadRequestHttp1_1RequestException;
import ru.algeps.sparrow.message.request.domain.exceptions.http.clienterror.ContentTooLargeHttp1_1RequestException;
import ru.algeps.sparrow.message.request.domain.exceptions.http.clienterror.MethodNotAllowedHttp1_1RequestException;
import ru.algeps.sparrow.message.request.domain.exceptions.http.clienterror.RequestHeaderTooLargeHttp1_1RequestException;
import ru.algeps.sparrow.message.request.domain.exceptions.http.clienterror.UriTooLongHttp1_1RequestException;
import ru.algeps.sparrow.message.request.domain.exceptions.http.servererror.HttpVersionNotSupportedHttp1_1RequestException;
import ru.algeps.sparrow.message.request.parser.http.exceptions.HttpRequestParserException;
import ru.algeps.sparrow.message.request.parser.http.exceptions.HttpRequestParserInvalidHeadersException;
import ru.algeps.sparrow.message.request.parser.http.exceptions.HttpRequestParserInvalidMethodException;
import ru.algeps.sparrow.message.response.domain.TransferEncoding;
import ru.algeps.sparrow.message.util.HttpMessageUtil;

public class HttpRequestParser1_1
    extends HttpRequestParser<HttpRequest1_1.Builder1_1, HttpRequest1_1> {
  // constants
  private static final HttpVersion HTTP_VERSION = HttpVersion.HTTP_1_1;
  private static final byte[] HTTP_VERSION_BYTES_REPRESENTATION =
      HttpVersion.HTTP_1_1.getRepresentationInBytes();
  //
  protected static Set<TransferEncoding> transferEncodings = null;
  protected static List<String> nameTrailers = null;

  public HttpRequestParser1_1(ReadableByteConnectionChannel readableByteConnectionChannel)
      throws HttpVersionNotSupportedHttp1_1RequestException {
    super(
        readableByteConnectionChannel,
        HttpRequest.newBuilder(HttpVersion.HTTP_1_1, HttpRequest1_1.Builder1_1.class));
  }

  @Override
  public HttpRequest1_1 getHttpRequest() throws IOException {
    this.httpRequestBuilder.clear();

    parseMethod();
    parseUri();
    parseVersion();
    parseHeaders();
    parseBody();
    parseTrailers();
    clearAfterRead();

    return this.httpRequestBuilder.build();
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////
  //                                Парсинг стартовой строки
  /////////////////////////////////////////////////////////////////////////////////////////////////

  /** Читает заголовок из потока. */
  private void parseMethod() throws IOException {
    byte[] bytes = channel.readBytes(3);

    HttpMethod httpMethod =
        switch (bytes[0]) {
          case G -> {
            if (bytes[1] == E && bytes[2] == T) {
              yield HttpMethod.GET;
            }
            yield null;
          }
          case P -> {
            if (bytes[1] == O && bytes[2] == S && channel.readByte() == T) {
              yield HttpMethod.POST;
            }
            if (bytes[1] == U && bytes[2] == T) {
              yield HttpMethod.PUT;
            }
            yield null;
          }
          case D -> {
            byte[] remainingBytes = channel.readBytes(3);
            if (bytes[1] == E
                && bytes[2] == L
                && remainingBytes[0] == E
                && remainingBytes[1] == T
                && remainingBytes[2] == E) {
              yield HttpMethod.DELETE;
            }
            yield null;
          }
          case H -> {
            if (bytes[1] == E && bytes[2] == A && channel.readByte() == D) {
              yield HttpMethod.HEAD;
            }
            yield null;
          }
          case C -> {
            byte[] remainingBytes = channel.readBytes(4);
            if (bytes[1] == O
                && bytes[2] == N
                && remainingBytes[0] == N
                && remainingBytes[1] == E
                && remainingBytes[2] == C
                && remainingBytes[3] == T) {
              yield HttpMethod.CONNECT;
            }
            yield null;
          }
          case O -> {
            byte[] remainingBytes = channel.readBytes(4);
            if (bytes[1] == P
                && bytes[2] == T
                && remainingBytes[0] == I
                && remainingBytes[1] == O
                && remainingBytes[2] == N
                && remainingBytes[3] == S) {
              yield HttpMethod.OPTIONS;
            }
            yield null;
          }
          case T -> {
            byte[] remainingBytes = channel.readBytes(2);
            if (bytes[1] == R
                && bytes[2] == A
                && remainingBytes[0] == C
                && remainingBytes[1] == E) {
              yield HttpMethod.TRACE;
            }
            yield null;
          }
          default -> null;
        };

    if (httpMethod == null) {
      throw new MethodNotAllowedHttp1_1RequestException(
          new HttpRequestParserInvalidMethodException());
    }

    httpRequestBuilder.httpMethod(httpMethod);
  }

  /**
   * Читает версию uri из потока. Если путь относительный (начинается на ../), то выбрасывает
   * исключение.
   */
  private void parseUri() throws IOException {
    if (channel.readByte() == SPACE) {
      byte[] uriBytes = new byte[MAX_URI_STRING_SIZE];
      int uriLength = 0;
      byte b = channel.readByte();

      if (b == DOT) {
        throw new BadRequestHttp1_1RequestException(
            new HttpRequestParserException("Incorrect URI (is relative)"));
      }
      uriBytes[uriLength++] = b;

      while ((b = channel.readByte()) != SPACE) {
        if (uriLength >= MAX_URI_STRING_SIZE) {
          throw new UriTooLongHttp1_1RequestException(
              new HttpRequestParserException(
                  "Too long URI (max=%d)".formatted(MAX_URI_STRING_SIZE)));
        }
        uriBytes[uriLength++] = b;
      }

      String uriString = new String(uriBytes, 0, uriLength, StandardCharsets.US_ASCII);
      try {
        URI uri = URI.create(uriString);
        httpRequestBuilder.uri(uri);
      } catch (IllegalArgumentException e) {
        throw new BadRequestHttp1_1RequestException(
            new HttpRequestParserException("Invalid uri value:" + uriString));
      }
    } else {
      throw new BadRequestHttp1_1RequestException(
          new HttpRequestParserException("Cannot parse uri (there in no space)"));
    }
  }

  /**
   * Читает версию HTTP из канала. Сначала парсит стандартный набор ASCII-символов: "HTTP/", затем
   * парсит номер протокола. <br>
   * В конце метода выполняется условие ИЛИ на проверку правильного завершения строки управляющих
   * данных. С помощью условия ИЛИ выполняется чтение двух байт. Два следующих прочитанных байта
   * должны быть: \r и \n.
   */
  private void parseVersion() throws IOException {
    byte[] buffer = channel.readBytes(HTTP_VERSION_BYTES_REPRESENTATION.length);

    if (Arrays.equals(buffer, HTTP_VERSION_BYTES_REPRESENTATION)) {
      httpRequestBuilder.version(HttpVersion.HTTP_1_1);
    } else {
      throw new HttpVersionNotSupportedHttp1_1RequestException(
          "Request protocol=[%s]".formatted(new String(buffer)));
    }

    buffer = channel.readBytes(2);
    if (buffer[0] != CARRIAGE_RETURN || buffer[1] != LINE_FEED) {
      throw new BadRequestHttp1_1RequestException(
          new HttpRequestParserException(
              "Incorrect end control data! Expected \\r and \\n symbols!"));
    }
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////
  //                                Парсинг заголовков (ленивый тип вызова)
  /////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Читает все заголовки из потока. Парсит и проверяет синтаксис.
   *
   * <pre>
   *  Каждый проход - чтение одного заголовка.
   *  Проход состоит из следующих шагов:
   *  1) Читает первый байт из потока, если этот байт = {@value ru.algeps.sparrow.message.request.parser.http.AsciiAlphabet#CARRIAGE_RETURN}
   *  (возврат каретки), то читается следующий байт. Если следующий = {@value ru.algeps.sparrow.message.request.parser.http.AsciiAlphabet#LINE_FEED}
   *  (перевод на новую строку), то это говорит о завершении блока заголовков.
   *  2) Чтение заголовка.
   *  3) Удаление знаков = [{@value ru.algeps.sparrow.message.request.parser.http.AsciiAlphabet#SPACE} (пробел), {@value ru.algeps.sparrow.message.request.parser.http.AsciiAlphabet#TAB} (табуляция)]
   *  после названия заголовка (после {@value ru.algeps.sparrow.message.request.parser.http.AsciiAlphabet#COLON} (двоеточие)).
   *  4) Чтение значения заголовка.
   *  5) Проверка корректного завершения строки. (байт = {@value ru.algeps.sparrow.message.request.parser.http.AsciiAlphabet#LINE_FEED} (перевод на новую строку))
   * </pre>
   */
  protected void parseHeaders() throws IOException {
    byte[] nameBuffer = new byte[MAX_NAME_HEADER_SIZE];
    byte[] valueBuffer = new byte[MAX_VALUE_HEADER_SIZE];

    while (true) {
      byte current = channel.readByte();
      if (current == CARRIAGE_RETURN && (current = channel.readByte()) == LINE_FEED) {
        break;
      }
      // название
      int headerLength = readHeaderName(current, nameBuffer);
      String headerName = new String(nameBuffer, 0, headerLength, StandardCharsets.US_ASCII);
      current = getFirstCharacterExceptSpaceAndTabFromChannel();
      // чтение значений заголовка и сохранения в билдере
      readHeaderValue(current, valueBuffer, headerName);
      // ОСТАВИТЬ
      throwIfIncorrectEndHeaderLine(channel.readByte(), headerName);
    }
  }

  /**
   * Читает название заголовка из потока. Выкидывает исключение, если размер больше максимально *
   * допустимого.
   */
  private int readHeaderName(byte current, byte[] nameBuffer) throws IOException {
    int length = 0;
    throwIfUnsupportedByteInHeaderName(current);
    nameBuffer[length++] = current;

    while ((current = channel.readByte()) != COLON) {
      throwIfSizeOfHeaderNameBiggerMax(length);
      throwIfUnsupportedByteInHeaderName(current);
      nameBuffer[length++] = current;
    }

    return length;
  }

  private void readHeaderValue(byte current, byte[] valueBuffer, String headerName)
      throws IOException {
    int length = 0;
    int valueStart = 0;

    // если пустой заголовок
    if (current == CARRIAGE_RETURN) {
      return;
    }
    throwIfUnsupportedByteInHeaderValue(current);
    valueBuffer[length++] = current;
    FieldName serviceHeader = getServerHeaderOrNull(headerName);

    boolean isList = false;
    int emptyElementCount = 0;
    ValueParsingState state =
        headerName.equalsIgnoreCase(FieldName.USER_AGENT.getName())
            ? ValueParsingState.THREAT_AS_IT_IS
            : ValueParsingState.START_STRING;

    while ((current = channel.readByte()) != CARRIAGE_RETURN) {
      throwIfSizeOfHeaderValueBiggerMax(length);
      throwIfUnsupportedByteInHeaderValue(current);

      switch (state) {
        case START_STRING:
          switch (current) {
            case DOUBLE_QUOTE:
              state = ValueParsingState.IS_QUOTED_STRING;
              break;
            case LEFT_PARENTHESIS:
              state = ValueParsingState.COMMENT;
              break;
            case COMMA:
              emptyElementCount = 1;
              isList = true;
              valueStart = length;
              state = ValueParsingState.NONE;
              break;
            case SPACE:
            case TAB:
              break;
            default:
              state = ValueParsingState.NONE;
              valueBuffer[length++] = current;
              break;
          }
          break;
        case NONE:
          switch (current) {
            case DOUBLE_QUOTE:
              state = ValueParsingState.IS_QUOTED_STRING;
              break;
            case LEFT_PARENTHESIS:
              state = ValueParsingState.COMMENT;
              break;
            case COMMA:
              if (length - valueStart > 0) {
                for (int i = 0; i < emptyElementCount; i++) {
                  httpRequestBuilder.header(headerName, "");
                }
                String value =
                    new String(
                            valueBuffer, valueStart, length - valueStart, StandardCharsets.US_ASCII)
                        .trim();
                // todo trim заменить на индекс последнего элемента не равного TAB или SPACE
                httpRequestBuilder.header(headerName, value);
                emptyElementCount = 0;
                handleServiceHeader(serviceHeader, value);
              } else {
                emptyElementCount++;
              }
              valueStart = length;
              isList = true;
              break;
            case SPACE:
            case TAB:
              {
                if (isList) {
                  break;
                }
              }
            default:
              valueBuffer[length++] = current;
              break;
          }
          break;
        case IS_QUOTED_STRING:
          switch (current) {
            case DOUBLE_QUOTE:
              state = ValueParsingState.NONE;
              break;
            case SLASH:
              state = ValueParsingState.ESCAPING;
              break;
            default:
              valueBuffer[length++] = current;
              break;
          }
          break;
        case ESCAPING:
          valueBuffer[length++] = current;
          state = ValueParsingState.IS_QUOTED_STRING;
          break;
        case COMMENT:
          valueBuffer[length++] = current;
          if (current == RIGHT_PARENTHESIS) {
            state = ValueParsingState.NONE;
          }
          break;
        case THREAT_AS_IT_IS:
          valueBuffer[length++] = current;
          break;
      }
    }

    if (length - valueStart > 0) {
      String value =
          new String(valueBuffer, valueStart, length - valueStart, StandardCharsets.US_ASCII);
      handleServiceHeader(serviceHeader, value);
      httpRequestBuilder.header(headerName, value);
    }
  }

  private enum ValueParsingState {
    NONE,
    START_STRING,
    IS_QUOTED_STRING,
    ESCAPING,
    COMMENT,
    // обрабатывать как есть
    THREAT_AS_IT_IS
  }

  private FieldName getServerHeaderOrNull(String headerName) {
    if (headerName.equalsIgnoreCase(FieldName.CONTENT_LENGTH.getName())) {
      return FieldName.CONTENT_LENGTH;
    }
    if (headerName.equalsIgnoreCase(FieldName.TRANSFER_ENCODING.getName())) {
      return FieldName.TRANSFER_ENCODING;
    }
    if (headerName.equalsIgnoreCase(FieldName.TRAILER.getName())) {
      return FieldName.TRAILER;
    }
    return null;
  }

  /**
   * Пропускает [{@value ru.algeps.sparrow.message.request.parser.http.AsciiAlphabet#SPACE}
   * (пробел), {@value ru.algeps.sparrow.message.request.parser.http.AsciiAlphabet#TAB} (табуляция)]
   * из потока, пока не встреться любой другой символ.
   *
   * @return Возвращает первый символ, который != [{@value
   *     ru.algeps.sparrow.message.request.parser.http.AsciiAlphabet#SPACE} (пробел), {@value
   *     ru.algeps.sparrow.message.request.parser.http.AsciiAlphabet#TAB} (табуляция)].
   */
  private byte getFirstCharacterExceptSpaceAndTabFromChannel() throws IOException {
    byte b;
    do {
      b = channel.readByte();
    } while (b == SPACE || b == TAB);
    return b;
  }

  /**
   * Выполняет проверку: поддерживается ли символ в названии заголовка (поддерживаемые символы
   * определены в протоколе). <a href="https://httpwg.org/specs/rfc9110.html#fields.names">DOC
   * RFC</a>
   *
   * @throws
   *     ru.algeps.sparrow.message.request.domain.exceptions.http.clienterror.BadRequestHttp1_1RequestException
   *     если встретился неподдерживаемый символ.
   */
  private void throwIfUnsupportedByteInHeaderName(byte b) throws BadRequestHttp1_1RequestException {
    if (b < 0 || !SUPPORTED_ASCII_SYMBOLS_IN_NAME_HEADER_ARRAY[b]) {
      throw new BadRequestHttp1_1RequestException(
          new HttpRequestParserException(
              "Unsupported symbol in header=[%s]. Number of byte=[%s]"
                  .formatted(b, channel.getReadByteCount())));
    }
  }

  /**
   * Если значение length больше максимального значения для размера названия заголовка.
   *
   * @param length длина названия заголовка
   * @throws
   *     ru.algeps.sparrow.message.request.domain.exceptions.http.clienterror.RequestHeaderTooLargeHttp1_1RequestException
   *     в случаи превышении длины
   */
  private void throwIfSizeOfHeaderNameBiggerMax(int length)
      throws RequestHeaderTooLargeHttp1_1RequestException {
    if (length >= MAX_NAME_HEADER_SIZE) {
      throw new RequestHeaderTooLargeHttp1_1RequestException(
          new HttpRequestParserInvalidHeadersException(
              "Incorrect header name size (max=%s)!".formatted(MAX_NAME_HEADER_SIZE)));
    }
  }

  /**
   * Выполняет проверку: поддерживается ли символ в значении заголовка (поддерживаемые символы
   * определены в протоколе). <a href="https://httpwg.org/specs/rfc9110.html#fields.values">DOC
   * RFC</a>
   *
   * @throws
   *     ru.algeps.sparrow.message.request.domain.exceptions.http.clienterror.BadRequestHttp1_1RequestException
   *     если встретился неподдерживаемый символ.
   */
  private void throwIfUnsupportedByteInHeaderValue(byte b)
      throws BadRequestHttp1_1RequestException {
    if (b < 0 || !VISIBLE_CHARACTERS_WITH_SPACE_ARRAY[b]) {
      throw new BadRequestHttp1_1RequestException(
          new HttpRequestParserException(
              "Unsupported symbol in header=[%s]. Number of byte=[%s]"
                  .formatted(b, channel.getReadByteCount())));
    }
  }

  /**
   * Если величина length больше максимального значения.
   *
   * @param length длина значения заголовка
   * @throws
   *     ru.algeps.sparrow.message.request.domain.exceptions.http.clienterror.RequestHeaderTooLargeHttp1_1RequestException
   *     в случаи превышении длины
   */
  private void throwIfSizeOfHeaderValueBiggerMax(int length)
      throws RequestHeaderTooLargeHttp1_1RequestException {
    if (length >= MAX_VALUE_HEADER_SIZE) {
      throw new RequestHeaderTooLargeHttp1_1RequestException(
          new HttpRequestParserInvalidHeadersException(
              "Incorrect value header size (max=%s)!".formatted(MAX_VALUE_HEADER_SIZE)));
    }
  }

  /**
   * Выбрасывает исключение, если строка заголовка не заканчивается на {@value
   * ru.algeps.sparrow.message.request.parser.http.AsciiAlphabet#LINE_FEED} (перевод на новую
   * строку).
   *
   * @param headerName для указания, в какой строке (в каком заголовке) находится некорректный
   *     символ.
   */
  private void throwIfIncorrectEndHeaderLine(byte endByte, String headerName)
      throws BadRequestHttp1_1RequestException {
    if (endByte != LINE_FEED) {
      throw new BadRequestHttp1_1RequestException(
          new HttpRequestParserInvalidHeadersException(
              "Incorrect headers! An unexpected symbol=[%s] ASCII number char on end of line in header=[%s]. Number of byte=[%s]"
                  .formatted(endByte, headerName, channel.getReadByteCount())));
    }
  }

  private void contentLengthHeaderValue(String contentLengthString)
      throws BadRequestHttp1_1RequestException {
    if (contentLength != null) {
      throw new BadRequestHttp1_1RequestException("Double 'Content-Length' header");
    }
    try {
      this.contentLength = Long.parseLong(contentLengthString);
    } catch (NumberFormatException e) {
      throw new BadRequestHttp1_1RequestException(
          new HttpRequestParserException(
              "Cannot convert to long string=[%s] in header 'Content-Length'"
                  .formatted(contentLengthString)));
    }
  }

  private void handleServiceHeader(FieldName fieldName, String headerValue) throws IOException {
    switch (fieldName) {
      case CONTENT_LENGTH -> contentLengthHeaderValue(headerValue);
      case TRANSFER_ENCODING -> transferEncodingHeaderValue(headerValue);
      case TRAILER -> trailersHeaderValue(headerValue);
      case null, default -> {}
    }
  }

  private void transferEncodingHeaderValue(String transferEncoding)
      throws BadRequestHttp1_1RequestException {
    if (transferEncodings == null) {
      transferEncodings = new LinkedHashSet<>();
    }
    try {
      transferEncodings.add(TransferEncoding.parseOf(transferEncoding));
    } catch (IllegalArgumentException e) {
      throw new BadRequestHttp1_1RequestException(
          "Incorrect value in '%s'".formatted(FieldName.TRANSFER_ENCODING), e);
    }
  }

  private void trailersHeaderValue(String trailerName) {
    if (nameTrailers == null) {
      nameTrailers = new ArrayList<>(2);
    }
    nameTrailers.add(trailerName);
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////
  //                    Парсинг тела сообщения (ленивый тип вызова)
  /////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Чтение контента. Количество прочитанных байт - значение в заголовке Content-Length
   *
   * @throws java.io.IOException если невозможно прочитать байты из потока
   */
  private void parseBody() throws IOException {
    if (contentLength != null
        && transferEncodings != null
        && transferEncodings.contains(TransferEncoding.CHUNKED)) {
      throw new BadRequestHttp1_1RequestException(
          "Violation of the specification: '%s' and '%s'(with value %s) headers in a single request!"
              .formatted(
                  FieldName.CONTENT_LENGTH.getName(),
                  FieldName.TRANSFER_ENCODING.getName(),
                  TransferEncoding.CHUNKED.getName()));
    }

    // если установлен размер Content-Length
    if (contentLength != null) {
      readContent();
    } else if (transferEncodings != null && transferEncodings.contains(TransferEncoding.CHUNKED)) {
      readChunks();
    }
  }

  private void readContent()
      throws ReadableByteConnectionChannel.ReadableByteConnectionChannelException {
    byte[] bodyBytes = new byte[contentLength.intValue()];

    for (int i = 0; i < contentLength; i++) {
      bodyBytes[i] = channel.readByte();
    }

    httpRequestBuilder.body(bodyBytes);
  }

  private void readChunks() throws IOException {
    try {

      byte[] bodyBytes = new byte[] {};
      int contentSize = 0;
      byte current;
      int index = 0;

      while (true) {
        // чтение и валидация размера чанка
        int chunkSize = readChunkSize();
        if (contentSize < 0) {
          throw new ContentTooLargeHttp1_1RequestException("Content too large: " + contentSize);
        }
        if (chunkSize == 0) {
          break;
        }
        contentSize += chunkSize;

        // создание нового массива контента
        bodyBytes = HttpMessageUtil.copyBytes(bodyBytes, contentSize);
        for (; index < contentSize; index++) {
          bodyBytes[index] = channel.readByte();
        }

        validateBoundaryOfChunk();
      }

      httpRequestBuilder.body(bodyBytes);
    } catch (IllegalArgumentException e) {
      throw new BadRequestHttp1_1RequestException(e);
    }
  }

  private int readChunkSize() throws IOException {
    int chunkSize;
    byte current;
    byte[] bytesChunkSize = new byte[HEX_CHARS_PER_INT];
    int i = 0;

    while ((current = channel.readByte()) != CARRIAGE_RETURN) {
      if (i >= HEX_CHARS_PER_INT) {
        String exceptionMessage =
            "For the size of the chunk, more hex characters were received than the maximum allowed. Max hex count: %s, current size: %s"
                .formatted(MAX_CHUNK_SIZE, i);
        throw new ContentTooLargeHttp1_1RequestException(exceptionMessage);
      }

      bytesChunkSize[i++] = current;
    }
    chunkSize = HttpMessageUtil.hexBytesToInt(bytesChunkSize, i);

    if ((current = channel.readByte()) != LINE_FEED) {
      String exceptionMessage = createUnexpectedSymbolExceptionMessage(current);
      throw new BadRequestHttp1_1RequestException(new HttpRequestParserException(exceptionMessage));
    }
    return chunkSize;
  }

  private String createUnexpectedSymbolExceptionMessage(byte current) {
    return "Unexpected symbol (byte value): %s. Number of byte=[%s]"
        .formatted(current, channel.getReadByteCount());
  }

  private void validateBoundaryOfChunk() throws IOException {
    byte current;
    if ((current = channel.readByte()) != CARRIAGE_RETURN) {
      throw new BadRequestHttp1_1RequestException(
          new HttpRequestParserException(createUnexpectedSymbolExceptionMessage(current)));
    }
    if ((current = channel.readByte()) != LINE_FEED) {
      throw new BadRequestHttp1_1RequestException(
          new HttpRequestParserException(createUnexpectedSymbolExceptionMessage(current)));
    }
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////
  //                    Парсинг трейлеров
  /////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Парсинг трейлеров - заголовков после тела сообщения. Запускается, только если информация о них
   * была в заголовках.
   */
  private void parseTrailers() throws IOException {
    if (transferEncodings != null
        && transferEncodings.contains(TransferEncoding.CHUNKED)
        && nameTrailers != null
        && !nameTrailers.isEmpty()) {
      int trailerCount = nameTrailers.size();
      int trailerReadCount = 0;

      byte[] nameBuffer = new byte[MAX_NAME_HEADER_SIZE];
      byte[] valueBuffer = new byte[MAX_VALUE_HEADER_SIZE];
      byte current;

      do {
        current = channel.readByte();
        // название трейлеров
        int headerLength = readHeaderName(current, nameBuffer);
        String headerName = new String(nameBuffer, 0, headerLength, StandardCharsets.US_ASCII);
        // проверка присутствия трейлера в заголовке трейлеров
        validateContainTrailer(headerName);

        current = getFirstCharacterExceptSpaceAndTabFromChannel();
        // чтение значений трейлера и сохранения в билдере
        readTrailerValue(current, valueBuffer, headerName);
        throwIfIncorrectEndHeaderLine(channel.readByte(), headerName);
        trailerReadCount++;
      } while (trailerCount != trailerReadCount);

      validateEndTrailers();
    }
  }

  private void validateContainTrailer(String headerName) throws IOException {
    if (!nameTrailers.contains(headerName)) {
      throw new BadRequestHttp1_1RequestException(
          new BadRequestHttp1_1RequestException(
              "There was a trailer that was not defined in the header 'Trailer': %s"
                  .formatted(headerName)));
    }
  }

  private void readTrailerValue(byte current, byte[] valueBuffer, String headerName)
      throws IOException {
    int length = 0;
    int valueStart = 0;

    // если пустой трейлер
    if (current == CARRIAGE_RETURN) {
      httpRequestBuilder.trailer(headerName, "");
    }
    throwIfUnsupportedByteInHeaderValue(current);
    valueBuffer[length++] = current;

    boolean isList = false;
    int emptyElementCount = 0;
    TrailerValueParsingState state = TrailerValueParsingState.START_STRING;

    while ((current = channel.readByte()) != CARRIAGE_RETURN) {
      throwIfSizeOfHeaderValueBiggerMax(length);
      throwIfUnsupportedByteInHeaderValue(current);

      switch (state) {
        case START_STRING:
          switch (current) {
            case DOUBLE_QUOTE:
              state = TrailerValueParsingState.IS_QUOTED_STRING;
              break;
            case LEFT_PARENTHESIS:
              state = TrailerValueParsingState.COMMENT;
              break;
            case COMMA:
              emptyElementCount = 1;
              isList = true;
              valueStart = length;
              state = TrailerValueParsingState.NONE;
              break;
            case SPACE:
            case TAB:
              break;
            default:
              state = TrailerValueParsingState.NONE;
              valueBuffer[length++] = current;
              break;
          }
          break;
        case NONE:
          switch (current) {
            case DOUBLE_QUOTE:
              state = TrailerValueParsingState.IS_QUOTED_STRING;
              break;
            case LEFT_PARENTHESIS:
              state = TrailerValueParsingState.COMMENT;
              break;
            case COMMA:
              if (length - valueStart > 0) {
                for (int i = 0; i < emptyElementCount; i++) {
                  httpRequestBuilder.trailer(headerName, "");
                }
                String value =
                    new String(
                            valueBuffer, valueStart, length - valueStart, StandardCharsets.US_ASCII)
                        .trim();
                // todo trim заменить на индекс последнего элемента не равного TAB или SPACE
                httpRequestBuilder.trailer(headerName, value);
                emptyElementCount = 0;
              } else {
                emptyElementCount++;
              }
              valueStart = length;
              isList = true;
              break;
            case SPACE:
            case TAB:
              {
                if (isList) {
                  break;
                }
              }
            default:
              valueBuffer[length++] = current;
              break;
          }
          break;
        case IS_QUOTED_STRING:
          switch (current) {
            case DOUBLE_QUOTE:
              state = TrailerValueParsingState.NONE;
              break;
            case SLASH:
              state = TrailerValueParsingState.ESCAPING;
              break;
            default:
              valueBuffer[length++] = current;
              break;
          }
          break;
        case ESCAPING:
          valueBuffer[length++] = current;
          state = TrailerValueParsingState.IS_QUOTED_STRING;
          break;
        case COMMENT:
          valueBuffer[length++] = current;
          if (current == RIGHT_PARENTHESIS) {
            state = TrailerValueParsingState.NONE;
          }
          break;
      }
    }

    if (length - valueStart > 0) {
      String value =
          new String(valueBuffer, valueStart, length - valueStart, StandardCharsets.US_ASCII);
      // todo trim заменить на индекс последнего элемента не равного TAB или SPACE
      httpRequestBuilder.trailer(headerName, value.trim());
    }
  }

  private enum TrailerValueParsingState {
    NONE,
    START_STRING,
    IS_QUOTED_STRING,
    ESCAPING,
    COMMENT
  }

  private void validateEndTrailers() throws IOException {
    byte current;
    if ((current = channel.readByte()) != CARRIAGE_RETURN) {
      throw new BadRequestHttp1_1RequestException(
          new HttpRequestParserException(
              getExceptionMessageInTrailerField(current, CARRIAGE_RETURN)));
    }
    if ((current = channel.readByte()) != LINE_FEED) {
      throw new BadRequestHttp1_1RequestException(
          new HttpRequestParserException(getExceptionMessageInTrailerField(current, LINE_FEED)));
    }
  }

  private String getExceptionMessageInTrailerField(byte actualByte, byte expectedByte) {
    return "Incorrect completion of the trailer section. Expected byte value: %s, actual: %s"
        .formatted(expectedByte, actualByte);
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////
  //                   Очистка после чтения
  /////////////////////////////////////////////////////////////////////////////////////////////////

  private void clearAfterRead() {
    contentLength = null;
    nameTrailers = null;
    transferEncodings = null;
  }
}
