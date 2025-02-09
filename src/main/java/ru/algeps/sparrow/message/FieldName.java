package ru.algeps.sparrow.message;

/**
 * <a href="https://www.iana.org/assignments/http-fields/http-fields.xhtml">Hypertext Transfer
 * Protocol (HTTP) Field Name Registry</a>
 */
public enum FieldName {
  A_IM("A-IM"),
  ACCEPT("Accept"),
  ACCEPT_ADDITIONS("Accept-Additions"),
  ACCEPT_CH("Accept-CH"),
  ACCEPT_CHARSET("Accept-Charset"),
  ACCEPT_DATETIME("Accept-Datetime"),
  ACCEPT_ENCODING("Accept-Encoding"),
  ACCEPT_FEATURES("Accept-Features"),
  ACCEPT_LANGUAGE("Accept-Language"),
  ACCEPT_PATCH("Accept-Patch"),
  ACCEPT_POST("Accept-Post"),
  ACCEPT_RANGES("Accept-Ranges"),
  ACCEPT_SIGNATURE("Accept-Signature"),
  ACCESS_CONTROL("Access-Control"),
  ACCESS_CONTROL_ALLOW_CREDENTIALS("Access-Control-Allow-Credentials"),
  ACCESS_CONTROL_ALLOW_HEADERS("Access-Control-Allow-Headers"),
  ACCESS_CONTROL_ALLOW_METHODS("Access-Control-Allow-Methods"),
  ACCESS_CONTROL_ALLOW_ORIGIN("Access-Control-Allow-Origin"),
  ACCESS_CONTROL_EXPOSE_HEADERS("Access-Control-Expose-Headers"),
  ACCESS_CONTROL_MAX_AGE("Access-Control-Max-Age"),
  ACCESS_CONTROL_REQUEST_HEADERS("Access-Control-Request-Headers"),
  ACCESS_CONTROL_REQUEST_METHOD("Access-Control-Request-Method"),
  AGE("Age"),
  ALLOW("Allow"),
  ALPN("ALPN"),
  ALT_SVC("Alt-Svc"),
  ALT_USED("Alt-Used"),
  ALTERNATES("Alternates"),
  AMP_CACHE_TRANSFORM("AMP-Cache-Transform"),
  APPLY_TO_REDIRECT_REF("Apply-To-Redirect-Ref"),
  AUTHENTICATION_CONTROL("Authentication-Control"),
  AUTHENTICATION_INFO("Authentication-Info"),
  AUTHORIZATION("Authorization"),
  AVAILABLE_DICTIONARY("Available-Dictionary"),
  C_EXT("C-Ext"),
  C_MAN("C-Man"),
  C_OPT("C-Opt"),
  C_PEP("C-PEP"),
  C_PEP_INFO("C-PEP-Info"),
  CACHE_CONTROL("Cache-Control"),
  CACHE_STATUS("Cache-Status"),
  CAL_MANAGED_ID("Cal-Managed-ID"),
  CALDAV_TIMEZONES("CalDAV-Timezones"),
  CAPSULE_PROTOCOL("Capsule-Protocol"),
  CDN_CACHE_CONTROL("CDN-Cache-Control"),
  CDN_LOOP("CDN-Loop"),
  CERT_NOT_AFTER("Cert-Not-After"),
  CERT_NOT_BEFORE("Cert-Not-Before"),
  CLEAR_SITE_DATA("Clear-Site-Data"),
  CLIENT_CERT("Client-Cert"),
  CLIENT_CERT_CHAIN("Client-Cert-Chain"),
  CLOSE("Close"),
  CMCD_OBJECT("CMCD-Object"),
  CMCD_REQUEST("CMCD-Request"),
  CMCD_SESSION("CMCD-Session"),
  CMCD_STATUS("CMCD-Status"),
  CMSD_DYNAMIC("CMSD-Dynamic"),
  CMSD_STATIC("CMSD-Static"),
  CONCEALED_AUTH_EXPORT("Concealed-Auth-Export"),
  CONFIGURATION_CONTEXT("Configuration-Context"),
  CONNECTION("Connection"),
  CONTENT_BASE("Content-Base"),
  CONTENT_DIGEST("Content-Digest"),
  CONTENT_DISPOSITION("Content-Disposition"),
  CONTENT_ENCODING("Content-Encoding"),
  CONTENT_ID("Content-ID"),
  CONTENT_LANGUAGE("Content-Language"),
  CONTENT_LENGTH("Content-Length"),
  CONTENT_LOCATION("Content-Location"),
  CONTENT_MD5("Content-MD5"),
  CONTENT_RANGE("Content-Range"),
  CONTENT_SCRIPT_TYPE("Content-Script-Type"),
  CONTENT_SECURITY_POLICY("Content-Security-Policy"),
  CONTENT_SECURITY_POLICY_REPORT_ONLY("Content-Security-Policy-Report-Only"),
  CONTENT_STYLE_TYPE("Content-Style-Type"),
  CONTENT_TYPE("Content-Type"),
  CONTENT_VERSION("Content-Version"),
  COOKIE("Cookie"),
  COOKIE2("Cookie2"),
  CROSS_ORIGIN_EMBEDDER_POLICY("Cross-Origin-Embedder-Policy"),
  CROSS_ORIGIN_EMBEDDER_POLICY_REPORT_ONLY("Cross-Origin-Embedder-Policy-Report-Only"),
  CROSS_ORIGIN_OPENER_POLICY("Cross-Origin-Opener-Policy"),
  CROSS_ORIGIN_OPENER_POLICY_REPORT_ONLY("Cross-Origin-Opener-Policy-Report-Only"),
  CROSS_ORIGIN_RESOURCE_POLICY("Cross-Origin-Resource-Policy"),
  CTA_COMMON_ACCESS_TOKEN("CTA-Common-Access-Token"),
  DASL("DASL"),
  DATE("Date"),
  DAV("DAV"),
  DEFAULT_STYLE("Default-Style"),
  DELTA_BASE("Delta-Base"),
  DEPRECATION("Deprecation"),
  DEPTH("Depth"),
  DERIVED_FROM("Derived-From"),
  DESTINATION("Destination"),
  DIFFERENTIAL_ID("Differential-ID"),
  DICTIONARY_ID("Dictionary-ID"),
  DIGEST("Digest"),
  DPoP("DPoP"),
  DPoP_NONCE("DPoP-Nonce"),
  EARLY_DATA("Early-Data"),
  EDIINT_FEATURES("EDIINT-Features"),
  ETAG("ETag"),
  EXPECT("Expect"),
  EXPECT_CT("Expect-CT"),
  EXPIRES("Expires"),
  EXT("Ext"),
  FORWARDED("Forwarded"),
  FROM("From"),
  GET_PROFILE("GetProfile"),
  HOBAREG("Hobareg"),
  HOST("Host"),
  HTTP2_SETTINGS("HTTP2-Settings"),
  IF("If"),
  IF_MATCH("If-Match"),
  IF_MODIFIED_SINCE("If-Modified-Since"),
  IF_NONE_MATCH("If-None-Match"),
  IF_RANGE("If-Range"),
  IF_SCHEDULE_TAG_MATCH("If-Schedule-Tag-Match"),
  IF_UNMODIFIED_SINCE("If-Unmodified-Since"),
  IM("IM"),
  INCLUDE_REFERRED_TOKEN_BINDING_ID("Include-Referred-Token-Binding-ID"),
  ISOLATION("Isolation"),
  KEEP_ALIVE("Keep-Alive"),
  LABEL("Label"),
  LAST_EVENT_ID("Last-Event-ID"),
  LAST_MODIFIED("Last-Modified"),
  LINK("Link"),
  LINK_TEMPLATE("Link-Template"),
  LOCATION("Location"),
  LOCK_TOKEN("Lock-Token"),
  MAN("Man"),
  MAX_FORWARDS("Max-Forwards"),
  MEMENTO_DATETIME("Memento-Datetime"),
  METER("Meter"),
  METHOD_CHECK("Method-Check"),
  METHOD_CHECK_EXPIRES("Method-Check-Expires"),
  MIME_VERSION("MIME-Version"),
  NEGOTIATE("Negotiate"),
  NEL("NEL"),
  ODATA_ENTITYID("OData-EntityId"),
  ODATA_ISOLATION("OData-Isolation"),
  ODATA_MAXVERSION("OData-MaxVersion"),
  ODATA_VERSION("OData-Version"),
  OPT("Opt"),
  OPTIONAL_WWW_AUTHENTICATE("Optional-WWW-Authenticate"),
  ORDERING_TYPE("Ordering-Type"),
  ORIGIN("Origin"),
  ORIGIN_AGENT_CLUSTER("Origin-Agent-Cluster"),
  OSCORE("OSCORE"),
  OSLC_CORE_VERSION("OSLC-Core-Version"),
  OVERWRITE("Overwrite"),
  P3P("P3P"),
  PEP("PEP"),
  PEP_INFO("PEP-Info"),
  PERMISSIONS_POLICY("Permissions-Policy"),
  PICS_LABEL("PICS-Label"),
  PING_FROM("Ping-From"),
  PING_TO("Ping-To"),
  POSITION("Position"),
  PRAGMA("Pragma"),
  PREFER("Prefer"),
  PREFERENCE_APPLIED("Preference-Applied"),
  PRIORITY("Priority"),
  PROFILE_OBJECT("ProfileObject"),
  PROTOCOL("Protocol"),
  PROTOCOL_INFO("Protocol-Info"),
  PROTOCOL_QUERY("Protocol-Query"),
  PROTOCOL_REQUEST("Protocol-Request"),
  PROXY_AUTHENTICATE("Proxy-Authenticate"),
  PROXY_AUTHENTICATION_INFO("Proxy-Authentication-Info"),
  PROXY_AUTHORIZATION("Proxy-Authorization"),
  PROXY_FEATURES("Proxy-Features"),
  PROXY_INSTRUCTION("Proxy-Instruction"),
  PROXY_STATUS("Proxy-Status"),
  PUBLIC("Public"),
  PUBLIC_KEY_PINS("Public-Key-Pins"),
  PUBLIC_KEY_PINS_REPORT_ONLY("Public-Key-Pins-Report-Only"),
  RANGE("Range"),
  REDIRECT_REF("Redirect-Ref"),
  REFERER("Referer"),
  REFERER_ROOT("Referer-Root"),
  REFERRER_POLICY("Referrer-Policy"),
  REFRESH("Refresh"),
  REPEATABILITY_CLIENT_ID("Repeatability-Client-ID"),
  REPEATABILITY_FIRST_SENT("Repeatability-First-Sent"),
  REPEATABILITY_REQUEST_ID("Repeatability-Request-ID"),
  REPEATABILITY_RESULT("Repeatability-Result"),
  REPLAY_NONCE("Replay-Nonce"),
  REPORTING_ENDPOINTS("Reporting-Endpoints"),
  REPR_DIGEST("Repr-Digest"),
  RETRY_AFTER("Retry-After"),
  SAFE("Safe"),
  SCHEDULE_REPLY("Schedule-Reply"),
  SCHEDULE_TAG("Schedule-Tag"),
  SEC_GPC("Sec-GPC"),
  SEC_PURPOSE("Sec-Purpose"),
  SEC_TOKEN_BINDING("Sec-Token-Binding"),
  SEC_WEBSOCKET_ACCEPT("Sec-WebSocket-Accept"),
  SEC_WEBSOCKET_EXTENSIONS("Sec-WebSocket-Extensions"),
  SEC_WEBSOCKET_KEY("Sec-WebSocket-Key"),
  SEC_WEBSOCKET_PROTOCOL("Sec-WebSocket-Protocol"),
  SEC_WEBSOCKET_VERSION("Sec-WebSocket-Version"),
  SECURITY_SCHEME("Security-Scheme"),
  SERVER("Server"),
  SERVER_TIMING("Server-Timing"),
  SET_COOKIE("Set-Cookie"),
  SET_COOKIE2("Set-Cookie2"),
  SET_PROFILE("SetProfile"),
  SIGNATURE("Signature"),
  SIGNATURE_INPUT("Signature-Input"),
  SLUG("SLUG"),
  SOAP_ACTION("SoapAction"),
  STATUS_URI("Status-URI"),
  STRICT_TRANSPORT_SECURITY("Strict-Transport-Security"),
  SUNSET("Sunset"),
  SURROGATE_CAPABILITY("Surrogate-Capability"),
  SURROGATE_CONTROL("Surrogate-Control"),
  TCN("TCN"),
  TE("TE"),
  TIMEOUT("Timeout"),
  TIMING_ALLOW_ORIGIN("Timing-Allow-Origin"),
  TOPIC("Topic"),
  TRACEPARENT("Traceparent"),
  TRACESTATE("Tracestate"),
  TRAILER("Trailer"),
  TRANSFER_ENCODING("Transfer-Encoding"),
  TTL("TTL"),
  UPGRADE("Upgrade"),
  URGENCY("Urgency"),
  URI("URI"),
  USE_AS_DICTIONARY("Use-As-Dictionary"),
  USER_AGENT("User-Agent"),
  VARIANT_VARY("Variant-Vary"),
  VARY("Vary"),
  VIA("Via"),
  WANT_CONTENT_DIGEST("Want-Content-Digest"),
  WANT_DIGEST("Want-Digest"),
  WANT_REPR_DIGEST("Want-Repr-Digest"),
  WARNING("Warning"),
  WWW_AUTHENTICATE("WWW-Authenticate"),
  X_CONTENT_TYPE_OPTIONS("X-Content-Type-Options"),
  X_FRAME_OPTIONS("X-Frame-Options");

  final String name;

  FieldName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
