package ru.algeps.sparrow.message.request.domain.mediatype;

/** <a href="https://www.iana.org/assignments/media-types/media-types.xhtml">Media Types</a> */
public interface MediaType {
  // todo включить поддержку ещё нескольких типов: фото, видео

  enum Application implements MediaType {
    OCTET_STREAM("application/octet-stream"),
    JSON("application/json"),
    PDF("application/pdf");

    final String contentType;

    Application(String contentType) {
      this.contentType = contentType;
    }

    public String getContentType() {
      return contentType;
    }
  }

  enum Image implements MediaType {
    /** Кастомный заголовок специально для FAVICON. */
    FAVICON("image/x-icon"),

    VND_MICROSOFT_ICON("image/vnd.microsoft.icon");

    final String contentType;

    Image(String contentType) {
      this.contentType = contentType;
    }

    public String getContentType() {
      return contentType;
    }
  }

  enum Text implements MediaType {
    CSS("text/css"),
    JAVASCRIPT("text/javascript"),
    HTML("text/html"),
    PLAIN("text/plain"),
    /** <a href="https://www.rfc-editor.org/rfc/rfc7763.html">DOC</a> */
    MARKDOWN("text/markdown");

    final String contentType;

    Text(String contentType) {
      this.contentType = contentType;
    }

    public String getContentType() {
      return contentType;
    }
  }

  /** Возвращает строковое представление MediaType. */
  String getContentType();

  static MediaType getMediaTypeByFileExtension(String fileExtension) {
    return switch (fileExtension) {
      case "js" -> Text.JAVASCRIPT;
      case "json" -> Application.JSON;
      case "html" -> Text.HTML;
      case "ico" -> Image.VND_MICROSOFT_ICON;
      case "css" -> Text.CSS;
      case "md" -> Text.MARKDOWN;
      case "txt" -> Text.PLAIN;
      case "pdf" -> Application.PDF;
      case null, default -> Application.OCTET_STREAM;
    };
  }

  static MediaType parseOfMediaType(String rawContentType) {
    if (rawContentType == null) {
      return null;
    }

    String rawMediaType = rawContentType.split(";")[0];

    if (rawMediaType == null) {
      throw new IllegalStateException("Unexpected value: null");
    }
    rawMediaType = rawMediaType.trim();

    for (MediaType mediaType : Application.values()) {
      if (mediaType.getContentType().equals(rawMediaType)) {
        return mediaType;
      }
    }

    for (MediaType mediaType : Image.values()) {
      if (mediaType.getContentType().equals(rawMediaType)) {
        return mediaType;
      }
    }

    for (MediaType mediaType : Text.values()) {
      if (mediaType.getContentType().equals(rawMediaType)) {
        return mediaType;
      }
    }

    throw new IllegalStateException("Unexpected value: " + rawMediaType);
  }
}
