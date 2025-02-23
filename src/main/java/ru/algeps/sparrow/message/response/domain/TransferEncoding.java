package ru.algeps.sparrow.message.response.domain;

/**
 * <a
 * href="https://www.iana.org/assignments/http-parameters/http-parameters.xhtml#content-coding">Transfer-Encoding</a>
 */
public enum TransferEncoding {
  CHUNKED("chunked"),
  COMPRESS("compress"),
  DEFLATE("deflate"),
  GZIP("gzip"),
  IDENTITY("identity"),
  TRAILERS("nameTrailers"),
  X_COMPRESS("x-compress"),
  X_GZIP("x-gzip");

  final String name;

  TransferEncoding(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public static TransferEncoding parseOf(String rawTransferEncoding) {
    String lowerRawTransferEncoding = rawTransferEncoding.toLowerCase();
    for (TransferEncoding transferEncoding : TransferEncoding.values()) {
      if (transferEncoding.name.equalsIgnoreCase(lowerRawTransferEncoding)) {
        return transferEncoding;
      }
    }
    throw new IllegalArgumentException(rawTransferEncoding);
  }
}
