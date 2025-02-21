package com.tsdm.net;

public interface netDefine
{
	int		NET_TIMER_INTERVAL						= 1000;
	int		DNSQUERY_TIME_OUT						= 60 * 1000;
	int		CONNECT_TIME_OUT						= 60 * 1000;
	int		RECEIVE_TIME_OUT						= 60 * 1000;
	int		SEND_TIME_OUT							= 60 * 1000;

	String	HTTP_METHOD_GET							= "GET";
	String	HTTP_METHOD_OPTIONS						= "OPTIONS";
	String	HTTP_METHOD_HEAD						= "HEAD";
	String	HTTP_METHOD_DELETE						= "DELETE";
	String	HTTP_METHOD_TRACE						= "TRACE";
	String	HTTP_METHOD_POST						= "POST";
	String	HTTP_METHOD_PUT							= "PUT";
	String	HTTP_METHOD_CONNECT						= "CONNECT";

	long    UNDEFINED_CONTENT_LENGTH = 0x7FFFFFFFL;
	String	HTTP_DM_USER_AGENT						= "SyncML_DM Client";
	String  HTTP_CACHECONTROL 						= "no-store, private";
	String  HTTP_LANGUAGE 							= "en";
	String	HTTP_CONNECTION_TYPE_KEEPALIVE			= "Keep-Alive";
	String	HTTP_CONNECTION_TYPE_CLOSE				= "close";
	String	HTTP_MIME_DM_XML_TYPES					= "application/vnd.syncml.dm+xml";
	String	HTTP_MIME_DM_WBXML_TYPES				= "application/vnd.syncml.dm+wbxml";
	String	HTTP_MIME_DS_WBXML_TYPES				= "application/vnd.syncml+wbxml";
	String	HTTP_HEADER_DS_ACCEPT					= "application/vnd.syncml+wbxml";
	String	HTTP_HEADER_DM_ACCEPT					= "application/vnd.syncml.dm+wbxml";
	String	HTTP_HEADER_DL_CONTENT_TYPE				= "application/vnd.oma.dd+xml";
	String	HTTP_HEADER_DL_ACCEPT					= "application/vnd.oma.dd+xml";
	String	HTTP_MIME_WAP_WML_TYPES					= "text/vnd.wap.wml";

	int 	TP_TYPE_NONE 							= 0;
	int 	TP_TYPE_HTTPS 							= 1;
	int 	TP_TYPE_HTTP 							= 2;
	int		TP_TYPE_WAP								= 3;
	int 	TP_TYPE_OBEX 							= 4;

	int 	HTTP_NOT_CHUNKED 						= 0;
	int 	HTTP_CHUNKED 							= 1;

	int 	TP_RETRY_COUNT_NONE 					= 0;
	int 	TP_RETRY_COUNT_MAX 						= 10;
	int 	TP_DL_RETRY_COUNT_MAX 					= 10;
	int 	TP_DL_RETRY_FAIL_COUNT_MAX 				= 10;

	int 	TP_HTTP_CONNECTION_NONE 				= 0;
	int     TP_HTTP_CONNECTION_CLOSE 				= 1;
	int     TP_HTTP_CONNECTION_KEEP_ALIVE 			= 2;

	int 	TP_RET_SSL_TUNNEL_MODE 					= 0;
	int 	TP_RET_CHANGED_PROFILE 					= 1;

	int 	TP_RET_OK 								= 0;
	int 	TP_RET_INIT_FAIL 						= -1;
	int		TP_RET_CONNECTION_FAIL 					= -2;
	int 	TP_RET_SEND_FAIL 						= -3;
	int 	TP_RET_RECEIVE_FAIL 					= -4;
	int 	TP_RET_INVALID_PARAM 					= -5;
	int 	TP_RET_HTTP_RES_FAIL 					= -6;
	int 	TP_RET_FILE_ERROR 						= -7;
	int 	TP_RET_HTTP_CONNECTION_POOL				= -8;


	int 	TP_HTTP_STATUS_INITD 					= 1;
	int 	TP_ECODE_PARAM_NULL 					= 0;
	int 	TP_ECODE_PROTO_MAX_CONNENTIONS 			= 1;
	int 	TP_ECODE_PROTO_FLIGHT_MODE 				= 2;
	int 	TP_ECODE_NETWORK_ATTACH_FAIL 			= 3;
	int 	TP_ECODE_NETWORK_ACCOUNT_FAIL 			= 4;
	int 	TP_ECODE_NETWORK_CONNECT_RETRY 			= 5;
	int 	TP_ECODE_NETWORK_STOPPED 				= 6;
	int 	TP_ECODE_SOCKET_RECEIVE_TIME_OUT 		= 7;
	int 	TP_ECODE_SOCKET_RECEIVE_FAILED 			= 8;
	int 	TP_ECODE_SOCKET_SEND_TIME_OUT 			= 9;
	int 	TP_ECODE_SOCKET_SEND_FAILED 			= 10;
	int 	TP_ECODE_SOCKET_REMOTE_CLOSED 			= 11;
	int 	TP_ECODE_HTTP_RETURN_STATUS_ERROR		 = 12;

	int 	TP_MODE_SSL_TUNNEL_NONE 				= 0;
	int 	TP_MODE_SSL_TUNNEL_DEACTIVE 			= 1;
	int 	TP_MODE_SSL_TUNNEL_ACTIVE 				= 2;

	int 	TP_SSL_TUNNEL_NONE 						= 0;
	int 	TP_SSL_TUNNEL_CONNECTING 				= 1;

	String  HTTP_HOST_STRING 						= "host:";
	String	HTTP_FORM_STRING						= "from:";
	String	HTTP_REFERER_STRING						= "referer:";
	String 	HTTP_CONTENT_LEN_STRING 				= "content-length:";
	String	HTTP_CONTENT_TYPE_STRING				= "content-type:";
	String 	HTTP_CONNECTION_STRING 					= "connection:";
	String 	HTTP_TRANSFER_ENCODING_STRING 			= "transfer-encoding:";
	String 	HTTP_X_SYNCML_HMAC_STRING 				= "x-syncml-hmac:";
	String 	HTTP_CONTENT_RANGE_STRING 				= "content-range:";

	String 	HTTP_CONTENT_DISPOSITION_STRING 		= "content-disposition:";
	String  HTTP_COOKIE_STRING 						= "set-cookie:";
	String  HTTP_COOKIE_JSESSIONID_STRING 			= "jsessionid";
	String  HTTP_COOKIE_AWSALB_STRING 				= "awsalb";

	String 	HTTP_CRLF_STRING 						= "\r\n";
}
