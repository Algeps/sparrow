package ru.algeps.sparrow.message.response.domain;

/**
 * <a href="https://www.iana.org/assignments/http-status-codes/http-status-codes.xhtml">Hypertext
 * Transfer Protocol (HTTP) Status Code Registry</a>
 */
public enum HttpStatusCode {
  /** Continue <a href="[RFC9110, Section 15.2.1]">DOC</a> */
  CONTINUE(100, "Continue"),

  /** Switching Protocols <a href="[RFC9110, Section 15.2.2]">DOC</a> */
  SWITCHING_PROTOCOLS(101, "Switching Protocols"),

  /** Processing <a href="[RFC2518]">DOC</a> */
  PROCESSING(102, "Processing"),

  /** Early Hints <a href="[RFC8297]">DOC</a> */
  EARLY_HINTS(103, "Early Hints"),

  /** OK <a href="[RFC9110, Section 15.3.1]">DOC</a> */
  OK(200, "OK"),

  /** Created <a href="[RFC9110, Section 15.3.2]">DOC</a> */
  CREATED(201, "Created"),

  /** Accepted <a href="[RFC9110, Section 15.3.3]">DOC</a> */
  ACCEPTED(202, "Accepted"),

  /** Non-Authoritative Information <a href="[RFC9110, Section 15.3.4]">DOC</a> */
  NON_AUTHORITATIVE_INFORMATION(203, "Non-Authoritative Information"),

  /** No Content <a href="[RFC9110, Section 15.3.5]">DOC</a> */
  NO_CONTENT(204, "No Content"),

  /** Reset Content <a href="[RFC9110, Section 15.3.6]">DOC</a> */
  RESET_CONTENT(205, "Reset Content"),

  /** Partial Content <a href="[RFC9110, Section 15.3.7]">DOC</a> */
  PARTIAL_CONTENT(206, "Partial Content"),

  /** Multi-Status <a href="[RFC4918]">DOC</a> */
  MULTI_STATUS(207, "Multi-Status"),

  /** Already Reported <a href="[RFC5842]">DOC</a> */
  ALREADY_REPORTED(208, "Already Reported"),

  /** IM Used <a href="[RFC3229]">DOC</a> */
  IM_USED(226, "IM Used"),

  /** Multiple Choices <a href="[RFC9110, Section 15.4.1]">DOC</a> */
  MULTIPLE_CHOICES(300, "Multiple Choices"),

  /** Moved Permanently <a href="[RFC9110, Section 15.4.2]">DOC</a> */
  MOVED_PERMANENTLY(301, "Moved Permanently"),

  /** Found <a href="[RFC9110, Section 15.4.3]">DOC</a> */
  FOUND(302, "Found"),

  /** See Other <a href="[RFC9110, Section 15.4.4]">DOC</a> */
  SEE_OTHER(303, "See Other"),

  /** Not Modified <a href="[RFC9110, Section 15.4.5]">DOC</a> */
  NOT_MODIFIED(304, "Not Modified"),

  /** Use Proxy <a href="[RFC9110, Section 15.4.6]">DOC</a> */
  USE_PROXY(305, "Use Proxy"),

  /** Temporary Redirect <a href="[RFC9110, Section 15.4.8]">DOC</a> */
  TEMPORARY_REDIRECT(307, "Temporary Redirect"),

  /** Permanent Redirect <a href="[RFC9110, Section 15.4.9]">DOC</a> */
  PERMANENT_REDIRECT(308, "Permanent Redirect"),

  /** Bad Request <a href="[RFC9110, Section 15.5.1]">DOC</a> */
  BAD_REQUEST(400, "Bad Request"),

  /** Unauthorized <a href="[RFC9110, Section 15.5.2]">DOC</a> */
  UNAUTHORIZED(401, "Unauthorized"),

  /** Payment Required <a href="[RFC9110, Section 15.5.3]">DOC</a> */
  PAYMENT_REQUIRED(402, "Payment Required"),

  /** Forbidden <a href="[RFC9110, Section 15.5.4]">DOC</a> */
  FORBIDDEN(403, "Forbidden"),

  /** Not Found <a href="[RFC9110, Section 15.5.5]">DOC</a> */
  NOT_FOUND(404, "Not Found"),

  /** Method Not Allowed <a href="[RFC9110, Section 15.5.6]">DOC</a> */
  METHOD_NOT_ALLOWED(405, "Method Not Allowed"),

  /** Not Acceptable <a href="[RFC9110, Section 15.5.7]">DOC</a> */
  NOT_ACCEPTABLE(406, "Not Acceptable"),

  /** Proxy Authentication Required <a href="[RFC9110, Section 15.5.8]">DOC</a> */
  PROXY_AUTHENTICATION_REQUIRED(407, "Proxy Authentication Required"),

  /** Request Timeout <a href="[RFC9110, Section 15.5.9]">DOC</a> */
  REQUEST_TIMEOUT(408, "Request Timeout"),

  /** Conflict <a href="[RFC9110, Section 15.5.10]">DOC</a> */
  CONFLICT(409, "Conflict"),

  /** Gone <a href="[RFC9110, Section 15.5.11]">DOC</a> */
  GONE(410, "Gone"),

  /** Length Required <a href="[RFC9110, Section 15.5.12]">DOC</a> */
  LENGTH_REQUIRED(411, "Length Required"),

  /** Precondition Failed <a href="[RFC9110, Section 15.5.13]">DOC</a> */
  PRECONDITION_FAILED(412, "Precondition Failed"),

  /** Content Too Large <a href="[RFC9110, Section 15.5.14]">DOC</a> */
  CONTENT_TOO_LARGE(413, "Content Too Large"),

  /** URI Too Long <a href="[RFC9110, Section 15.5.15]">DOC</a> */
  URI_TOO_LONG(414, "URI Too Long"),

  /** Unsupported Media Type <a href="[RFC9110, Section 15.5.16]">DOC</a> */
  UNSUPPORTED_MEDIA_TYPE(415, "Unsupported Media Type"),

  /** Range Not Satisfiable <a href="[RFC9110, Section 15.5.17]">DOC</a> */
  RANGE_NOT_SATISFIABLE(416, "Range Not Satisfiable"),

  /** Expectation Failed <a href="[RFC9110, Section 15.5.18]">DOC</a> */
  EXPECTATION_FAILED(417, "Expectation Failed"),

  /** Misdirected Request <a href="[RFC9110, Section 15.5.20]">DOC</a> */
  MISDIRECTED_REQUEST(421, "Misdirected Request"),

  /** Unprocessable Content <a href="[RFC9110, Section 15.5.21]">DOC</a> */
  UNPROCESSABLE_CONTENT(422, "Unprocessable Content"),

  /** Locked <a href="[RFC4918]">DOC</a> */
  LOCKED(423, "Locked"),

  /** Failed Dependency <a href="[RFC4918]">DOC</a> */
  FAILED_DEPENDENCY(424, "Failed Dependency"),

  /** Too Early <a href="[RFC8470]">DOC</a> */
  TOO_EARLY(425, "Too Early"),

  /** Upgrade Required <a href="[RFC9110, Section 15.5.22]">DOC</a> */
  UPGRADE_REQUIRED(426, "Upgrade Required"),

  // Code 427 is unassigned

  /** Precondition Required <a href="[RFC6585]">DOC</a> */
  PRECONDITION_REQUIRED(428, "Precondition Required"),

  /** Too Many Requests <a href="[RFC6585]">DOC</a> */
  TOO_MANY_REQUESTS(429, "Too Many Requests"),

  // Code 430 is unassigned

  /** Request Header Fields Too Large <a href="[RFC6585]">DOC</a> */
  REQUEST_HEADER_FIELDS_TOO_LARGE(431, "Request Header Fields Too Large"),

  // Codes 432-450 are unassigned

  /** Unavailable For Legal Reasons <a href="[RFC7725]">DOC</a> */
  UNAVAILABLE_FOR_LEGAL_REASONS(451, "Unavailable For Legal Reasons"),

  // Codes 452-499 are unassigned

  /** Internal Server Error <a href="[RFC9110, Section 15.6.1]">DOC</a> */
  INTERNAL_SERVER_ERROR(500, "Internal Server Error"),

  /** Not Implemented <a href="[RFC9110, Section 15.6.2]">DOC</a> */
  NOT_IMPLEMENTED(501, "Not Implemented"),

  /** Bad Gateway <a href="[RFC9110, Section 15.6.3]">DOC</a> */
  BAD_GATEWAY(502, "Bad Gateway"),

  /** Service Unavailable <a href="[RFC9110, Section 15.6.4]">DOC</a> */
  SERVICE_UNAVAILABLE(503, "Service Unavailable"),

  /** Gateway Timeout <a href="[RFC9110, Section 15.6.5]">DOC</a> */
  GATEWAY_TIMEOUT(504, "Gateway Timeout"),

  /** HTTP Version Not Supported <a href="[RFC9110, Section 15.6.6]">DOC</a> */
  HTTP_VERSION_NOT_SUPPORTED(505, "HTTP Version Not Supported"),

  /** Variant Also Negotiates <a href="[RFC2295]">DOC</a> */
  VARIANT_ALSO_NEGOTIATES(506, "Variant Also Negotiates"),

  /** Insufficient Storage <a href="[RFC4918]">DOC</a> */
  INSUFFICIENT_STORAGE(507, "Insufficient Storage"),

  /** Loop Detected <a href="[RFC5842]">DOC</a> */
  LOOP_DETECTED(508, "Loop Detected"),

  // Code 509 is unassigned

  /**
   * Not Extended (OBSOLETED) <a
   * href="[RFC2774][status-change-http-experiments-to-historic]">DOC</a>
   */
  NOT_EXTENDED_OBSOLETED(510, "Not Extended (OBSOLETED)"),

  /** Network Authentication Required <a href="[RFC6585]">DOC</a> */
  NETWORK_AUTHENTICATION_REQUIRED(511, "Network Authentication Required");

  private final int code;
  private final String description;

  HttpStatusCode(int code, String description) {
    this.code = code;
    this.description = description;
  }

  public int getCode() {
    return code;
  }

  public String getDescription() {
    return description;
  }

  public String asString() {
    return code + " " + description;
  }

  public static HttpStatusCode parseOfCode(int code) {
    return switch (code) {
      case 100 -> CONTINUE;
      case 101 -> SWITCHING_PROTOCOLS;
      case 102 -> PROCESSING;
      case 103 -> EARLY_HINTS;
      case 200 -> OK;
      case 201 -> CREATED;
      case 202 -> ACCEPTED;
      case 203 -> NON_AUTHORITATIVE_INFORMATION;
      case 204 -> NO_CONTENT;
      case 205 -> RESET_CONTENT;
      case 206 -> PARTIAL_CONTENT;
      case 207 -> MULTI_STATUS;
      case 208 -> ALREADY_REPORTED;
      case 226 -> IM_USED;
      case 300 -> MULTIPLE_CHOICES;
      case 301 -> MOVED_PERMANENTLY;
      case 302 -> FOUND;
      case 303 -> SEE_OTHER;
      case 304 -> NOT_MODIFIED;
      case 305 -> USE_PROXY;
      case 307 -> TEMPORARY_REDIRECT;
      case 308 -> PERMANENT_REDIRECT;
      case 400 -> BAD_REQUEST;
      case 401 -> UNAUTHORIZED;
      case 402 -> PAYMENT_REQUIRED;
      case 403 -> FORBIDDEN;
      case 404 -> NOT_FOUND;
      case 405 -> METHOD_NOT_ALLOWED;
      case 406 -> NOT_ACCEPTABLE;
      case 407 -> PROXY_AUTHENTICATION_REQUIRED;
      case 408 -> REQUEST_TIMEOUT;
      case 409 -> CONFLICT;
      case 410 -> GONE;
      case 411 -> LENGTH_REQUIRED;
      case 412 -> PRECONDITION_FAILED;
      case 413 -> CONTENT_TOO_LARGE;
      case 414 -> URI_TOO_LONG;
      case 415 -> UNSUPPORTED_MEDIA_TYPE;
      case 416 -> RANGE_NOT_SATISFIABLE;
      case 417 -> EXPECTATION_FAILED;
      case 421 -> MISDIRECTED_REQUEST;
      case 422 -> UNPROCESSABLE_CONTENT;
      case 423 -> LOCKED;
      case 424 -> FAILED_DEPENDENCY;
      case 425 -> TOO_EARLY;
      case 426 -> UPGRADE_REQUIRED;
      case 428 -> PRECONDITION_REQUIRED;
      case 429 -> TOO_MANY_REQUESTS;
      case 431 -> REQUEST_HEADER_FIELDS_TOO_LARGE;
      case 451 -> UNAVAILABLE_FOR_LEGAL_REASONS;
      case 500 -> INTERNAL_SERVER_ERROR;
      case 501 -> NOT_IMPLEMENTED;
      case 502 -> BAD_GATEWAY;
      case 503 -> SERVICE_UNAVAILABLE;
      case 504 -> GATEWAY_TIMEOUT;
      case 505 -> HTTP_VERSION_NOT_SUPPORTED;
      case 506 -> VARIANT_ALSO_NEGOTIATES;
      case 507 -> INSUFFICIENT_STORAGE;
      case 508 -> LOOP_DETECTED;
      case 510 -> NOT_EXTENDED_OBSOLETED;
      case 511 -> NETWORK_AUTHENTICATION_REQUIRED;
      default -> throw new IllegalStateException("Unexpected value (http code): " + code);
    };
  }
}
