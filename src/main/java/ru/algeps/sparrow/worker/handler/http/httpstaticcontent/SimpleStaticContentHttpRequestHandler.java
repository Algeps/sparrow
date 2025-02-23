package ru.algeps.sparrow.worker.handler.http.httpstaticcontent;

import static ru.algeps.sparrow.message.response.domain.HttpResponse.HTTP_DATE_TIME_FORMAT;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.ByteChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.algeps.sparrow.message.request.domain.HttpMethod;
import ru.algeps.sparrow.message.request.domain.HttpRequest;
import ru.algeps.sparrow.message.HttpVersion;
import ru.algeps.sparrow.message.request.domain.exceptions.http.HttpRequestException;
import ru.algeps.sparrow.message.request.domain.exceptions.http.clienterror.BadRequestHttp1_1RequestException;
import ru.algeps.sparrow.message.request.domain.exceptions.http.clienterror.MethodNotAllowedHttp1_1RequestException;
import ru.algeps.sparrow.message.request.domain.exceptions.http.clienterror.NotFoundHttp1_1RequestException;
import ru.algeps.sparrow.message.request.domain.mediatype.MediaType;
import ru.algeps.sparrow.message.request.parser.http.HttpRequestParser;
import ru.algeps.sparrow.message.response.domain.HttpResponse;
import ru.algeps.sparrow.message.response.domain.HttpStatusCode;
import ru.algeps.sparrow.util.FileUtil;
import ru.algeps.sparrow.worker.handler.http.HttpRequestHandler;

public class SimpleStaticContentHttpRequestHandler implements HttpRequestHandler {
  private final Logger log;
  private final String root;
  private final HttpVersion httpVersion;

  ////////////////////////////////////////////////////////////////////////
  //                    Ограничения
  ////////////////////////////////////////////////////////////////////////

  /** Максимальный размер файла, чтобы отправить одним запросом. */
  private final long maxReadAtTimeLength = 1300;

  /** Максимальный размер части файла, прочитываемый за раз для больших файлов. */
  private final int maxBufferSize = 10_000;

  public SimpleStaticContentHttpRequestHandler(String name, String root, HttpVersion httpVersion) {
    if (!Path.of(root).toFile().exists()) {
      throw new IllegalArgumentException(
          "root for=[%s] does not exist (root=%s)".formatted(name, root));
    }
    this.root = root;
    this.log = LoggerFactory.getLogger(name + " {static[%s]}".formatted(root));
    this.httpVersion = httpVersion;
  }

  @Override
  public boolean handle(HttpRequest httpRequest, ByteChannel channel) {
    try {
      if (httpRequest.httpMethod() != HttpMethod.GET) {
        throw new MethodNotAllowedHttp1_1RequestException(
            "Method not allowed for path=[%s]".formatted(httpRequest.uri().getPath()));
      }

      File file = getFile(httpRequest.uri().getPath());
      Path path = file.toPath();
      String fileExtension = FileUtil.getFileExtension(httpRequest.uri().getPath());
      LocalDateTime lastModifiedLocalDateTime =
          LocalDateTime.ofInstant(
              Instant.ofEpochMilli(file.lastModified()), ZoneId.systemDefault());
      String lastModified = HTTP_DATE_TIME_FORMAT.format(lastModifiedLocalDateTime);
      HttpResponse httpResponse =
          HttpResponse.responseFor(httpVersion)
              .statusCode(HttpStatusCode.OK)
              .addHeader("Last-Modified", lastModified);

      if (file.length() <= maxReadAtTimeLength) {
        doHandleLittleFile(path, fileExtension, httpResponse, channel);
      } else {
        doHandleBigFile(path, fileExtension, httpResponse, channel, file.length());
      }

      log.debug(
          "Successful handle file=[{}] size=[{}] for request=[{}]",
          file.getName(),
          file.length(),
          httpRequest);

    } catch (HttpRequestException e) {
      return handleHttpRequestException(e, httpRequest, channel);
    } catch (Exception e) {
      return handleException(e, httpRequest, channel);
    }

    return true;
  }

  private File getFile(String filePath) throws HttpRequestException {
    Path path = Paths.get(root, filePath);
    File file = path.toFile();

    if (file.isDirectory()) {
      throw new BadRequestHttp1_1RequestException(
          new IllegalArgumentException("Path is directory (path=%s)".formatted(path)));
    }

    long length = file.length();
    if (length == 0) {
      throw new NotFoundHttp1_1RequestException(
          new FileNotFoundException("File is not exist (path=%s)".formatted(path)));
    }
    return file;
  }

  private void doHandleBigFile(
      Path path,
      String fileExtension,
      HttpResponse httpResponse,
      ByteChannel channel,
      long fileLength)
      throws IOException {
    httpResponse.addHeaderForBigBody(
        fileLength, MediaType.getMediaTypeByFileExtension(fileExtension));
    channel.write(httpResponse.toByteBuffer());

    FileUtil.readBigFile(path, maxBufferSize, channel::write);
  }

  public void doHandleLittleFile(
      Path path, String fileExtension, HttpResponse httpResponse, ByteChannel channel)
      throws IOException {
    byte[] fileBytes = readFile(path);
    channel.write(
        httpResponse
            .addBody(fileBytes, MediaType.getMediaTypeByFileExtension(fileExtension))
            .toByteBuffer());
  }

  public byte[] readFile(Path path) throws FileNotFoundException {
    byte[] bytes = FileUtil.readFile(path);
    if (bytes == null) {
      throw new FileNotFoundException("Not found file in path=[%s]".formatted(path));
    }
    return bytes;
  }

  boolean handleHttpRequestException(
      HttpRequestException e, HttpRequest httpRequest, ByteChannel channel) {
    log.error("Exception=[{}] with request=[{}]", e, httpRequest);
    try {
      channel.write(e.getHttpResponse().toByteBuffer());
    } catch (IOException ex) {
      log.error("Cannot write in channel", e);
    }
    return false;
  }

  boolean handleException(Exception e, HttpRequest httpRequest, ByteChannel channel) {
    log.error("Exception=[{}] with request=[{}]", e, httpRequest);
    try {
      channel.write(
          new NotFoundHttp1_1RequestException("Not found file").getHttpResponse().toByteBuffer());
    } catch (IOException ex) {
      log.error("Cannot write in channel", e);
    }
    return false;
  }

  @Override
  public HttpRequestParser getHttpRequestParser(ByteChannel byteChannel) throws IOException {
    return HttpRequestParser.connect(byteChannel, httpVersion);
  }

  @Override
  public HttpMethod getHttpMethodHandle() {
    return HttpMethod.GET;
  }
}
