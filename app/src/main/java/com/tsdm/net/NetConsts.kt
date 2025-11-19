package com.tsdm.net

object NetConsts {
    const val NET_TIMER_INTERVAL: Int = 1000
    const val DNSQUERY_TIME_OUT: Int = 60 * 1000
    const val CONNECT_TIME_OUT: Int = 60 * 1000
    const val RECEIVE_TIME_OUT: Int = 60 * 1000
    const val SEND_TIME_OUT: Int = 60 * 1000

    const val HTTP_METHOD_GET: String = "GET"
    const val HTTP_METHOD_OPTIONS: String = "OPTIONS"
    const val HTTP_METHOD_HEAD: String = "HEAD"
    const val HTTP_METHOD_DELETE: String = "DELETE"
    const val HTTP_METHOD_TRACE: String = "TRACE"
    const val HTTP_METHOD_POST: String = "POST"
    const val HTTP_METHOD_PUT: String = "PUT"
    const val HTTP_METHOD_CONNECT: String = "CONNECT"

    const val UNDEFINED_CONTENT_LENGTH: Long = 0x7FFFFFFFL
    const val HTTP_DM_USER_AGENT: String = "SyncML_DM Client"
    const val HTTP_CACHECONTROL: String = "no-store, private"
    const val HTTP_LANGUAGE: String = "en"
    const val HTTP_CONNECTION_TYPE_KEEPALIVE: String = "Keep-Alive"
    const val HTTP_CONNECTION_TYPE_CLOSE: String = "close"
    const val HTTP_MIME_DM_XML_TYPES: String = "application/vnd.syncml.dm+xml"
    const val HTTP_MIME_DM_WBXML_TYPES: String = "application/vnd.syncml.dm+wbxml"
    const val HTTP_MIME_DS_WBXML_TYPES: String = "application/vnd.syncml+wbxml"
    const val HTTP_HEADER_DS_ACCEPT: String = "application/vnd.syncml+wbxml"
    const val HTTP_HEADER_DM_ACCEPT: String = "application/vnd.syncml.dm+wbxml"
    const val HTTP_HEADER_DL_CONTENT_TYPE: String = "application/vnd.oma.dd+xml"
    const val HTTP_HEADER_DL_ACCEPT: String = "application/vnd.oma.dd+xml"
    const val HTTP_MIME_WAP_WML_TYPES: String = "text/vnd.wap.wml"

    const val TP_TYPE_NONE: Int = 0
    const val TP_TYPE_HTTPS: Int = 1
    const val TP_TYPE_HTTP: Int = 2
    const val TP_TYPE_WAP: Int = 3
    const val TP_TYPE_OBEX: Int = 4

    const val HTTP_NOT_CHUNKED: Int = 0
    const val HTTP_CHUNKED: Int = 1

    const val TP_RETRY_COUNT_NONE: Int = 0
    const val TP_RETRY_COUNT_MAX: Int = 20
    const val TP_DL_RETRY_COUNT_MAX: Int = 20
    const val TP_DL_RETRY_FAIL_COUNT_MAX: Int = 20

    const val TP_HTTP_CONNECTION_NONE: Int = 0
    const val TP_HTTP_CONNECTION_CLOSE: Int = 1
    const val TP_HTTP_CONNECTION_KEEP_ALIVE: Int = 2

    const val TP_RET_SSL_TUNNEL_MODE: Int = 0
    const val TP_RET_CHANGED_PROFILE: Int = 1

    const val TP_RET_OK: Int = 0
    const val TP_RET_INIT_FAIL: Int = -1
    const val TP_RET_CONNECTION_FAIL: Int = -2
    const val TP_RET_SEND_FAIL: Int = -3
    const val TP_RET_RECEIVE_FAIL: Int = -4
    const val TP_RET_INVALID_PARAM: Int = -5
    const val TP_RET_HTTP_RES_FAIL: Int = -6
    const val TP_RET_FILE_ERROR: Int = -7
    const val TP_RET_HTTP_CONNECTION_POOL: Int = -8


    const val TP_HTTP_STATUS_INITD: Int = 1
    const val TP_ECODE_PARAM_NULL: Int = 0
    const val TP_ECODE_PROTO_MAX_CONNENTIONS: Int = 1
    const val TP_ECODE_PROTO_FLIGHT_MODE: Int = 2
    const val TP_ECODE_NETWORK_ATTACH_FAIL: Int = 3
    const val TP_ECODE_NETWORK_ACCOUNT_FAIL: Int = 4
    const val TP_ECODE_NETWORK_CONNECT_RETRY: Int = 5
    const val TP_ECODE_NETWORK_STOPPED: Int = 6
    const val TP_ECODE_SOCKET_RECEIVE_TIME_OUT: Int = 7
    const val TP_ECODE_SOCKET_RECEIVE_FAILED: Int = 8
    const val TP_ECODE_SOCKET_SEND_TIME_OUT: Int = 9
    const val TP_ECODE_SOCKET_SEND_FAILED: Int = 10
    const val TP_ECODE_SOCKET_REMOTE_CLOSED: Int = 11
    const val TP_ECODE_HTTP_RETURN_STATUS_ERROR: Int = 12

    const val TP_MODE_SSL_TUNNEL_NONE: Int = 0
    const val TP_MODE_SSL_TUNNEL_DEACTIVE: Int = 1
    const val TP_MODE_SSL_TUNNEL_ACTIVE: Int = 2

    const val TP_SSL_TUNNEL_NONE: Int = 0
    const val TP_SSL_TUNNEL_CONNECTING: Int = 1

    const val HTTP_HOST_STRING: String = "host:"
    const val HTTP_FORM_STRING: String = "from:"
    const val HTTP_REFERER_STRING: String = "referer:"
    const val HTTP_CONTENT_LEN_STRING: String = "content-length:"
    const val HTTP_CONTENT_TYPE_STRING: String = "content-type:"
    const val HTTP_CONNECTION_STRING: String = "connection:"
    const val HTTP_TRANSFER_ENCODING_STRING: String = "transfer-encoding:"
    const val HTTP_X_SYNCML_HMAC_STRING: String = "x-syncml-hmac:"
    const val HTTP_CONTENT_RANGE_STRING: String = "content-range:"

    const val HTTP_CONTENT_DISPOSITION_STRING: String = "content-disposition:"
    const val HTTP_COOKIE_STRING: String = "set-cookie:"
    const val HTTP_COOKIE_JSESSIONID_STRING: String = "jsessionid"
    const val HTTP_COOKIE_AWSALB_STRING: String = "awsalb"

    const val HTTP_CRLF_STRING: String = "\r\n"
}