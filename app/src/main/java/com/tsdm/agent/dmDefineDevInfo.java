package com.tsdm.agent;

public interface dmDefineDevInfo
{
	boolean _SYNCML_TS_LOG_ON_								 = true;

	boolean _SYNCML_TS_DM_VERSION_V11_ 						= false;
	boolean _SYNCML_TS_DM_VERSION_V12_						 = true;
	boolean _SYNCML_TS_DM__VERSION_V12_NONCE_RESYNC 		= false;
	boolean _SYNCML_TS_FOTA_								= true;
	boolean _SYNCML_TS_NONE_PROXY_ 							= false;
	boolean _SYNCML_TS_DM_REGISTRY_PROFILE_ 				= true;
	boolean _SYNCML_TS_DM_DELTA_EXTERNAL_STORAGE_ 			= false;
	boolean _SYNCML_TS_DM_DELTA_INTERIOR_MEMORY_STORAGE_ 	= true;
	boolean _SYNCML_TS_DM_DELTA_MULTI_MEMORY_STORAGE_ 		= false;
	
	enum SyncmlUICFlag
	{
		UIC_NONE,
		UIC_TRUE,
		UIC_FALSE,
		UIC_CANCELED,
		UIC_TIMEOUT
	};

	enum SyncmlProcessingState
	{
		PROC_NONE,
		PROC_SYNCHDR,
		PROC_ALERT,
		PROC_STATUS,
		PROC_RESULTS,
		PROC_GET,
		PROC_EXEC,
		PROC_ADD,
		PROC_REPLACE,
		PROC_DELETE,
		PROC_COPY
	};

	enum SyncmlAtomicStep
	{
		ATOMIC_NONE,
		ATOMIC_STEP_ROLLBACK,
		ATOMIC_STEP_NOT_EXEC
	};

	enum SyncmlState
	{
		DM_STATE_NONE,
		DM_STATE_INIT,
		DM_STATE_CLIENT_INIT_MGMT,
		DM_STATE_PROCESSING,
		DM_STATE_GENERIC_ALERT,
		DM_STATE_GENERIC_ALERT_REPORT,
		DM_STATE_ABORT_ALERT,
		DM_STATE_FINISH
	};

	int		PROCESS_STEP_NORMAL									= 0;
	int		PROCESS_STEP_SEQUENCE								= 1;
	int		PROCESS_STEP_ATOMIC									= 2;
	int		PROCESS_STEP_FINISH									= 3;

	int		SYNCMLAPPNONE										= -1;
	int		SYNCMLDM											= 0;
	int		SYNCMLDL											= 1;
	int		SYNCMLMAXAPP										= 2;

	int		NETWORK_SERVICE_NONE								= 0;
	int		NETWORK_SERVICE_NOT_READY							= 1;
	int		NETWORK_SERVICE_LIMITTED							= 2;
	int		NETWORK_SERVICE_SEARCHING							= 3;
	int		NETWORK_SERVICE_FULL_2G								= 4;
	int		NETWORK_SERVICE_FULL_2_5G							= 5;
	int		NETWORK_SERVICE_2_5G_EDGE							= 6;
	int		NETWORK_SERVICE_FULL_3G								= 7;
	int		NETWORK_SERVICE_HSDPA								= 8;
	int		NETWORK_SERVICE_WLAN								= 9;

	int		NETWORK_TYPE_UNKNOWN								= 0;
	int		NETWORK_TYPE_GPRS									= 1;
	int		NETWORK_TYPE_EDGE									= 2;
	int		NETWORK_TYPE_UMTS									= 3;

	int		NETWORK_STATE_NOT_USE								= 0;
	int		NETWORK_STATE_NOT_READY								= 1;
	int		NETWORK_STATE_SYNCML_USE							= 2;
	int		NETWORK_STATE_APPLICATION_USE						= 3;
	int		NETWORK_STATE_CALL_USE								= 4;
	int		NETWORK_STATE_ALREADY_DOWNLOAD						= 5;
	int		NETWORK_STATE_FDN_ENABLE							= 6;

	int		SDMABORT_USER_REQUEST								= 0x01;
	int		SDMABORT_TIMEOUT									= 0x02;
	int		SDMABORT_AUTHENTICATION_FAILURE						= 0x03;
	int		SDMABORT_AGENT_FAILURE								= 0x04;
	int		SDMABORT_SEND_FAILURE								= 0x05;
	int		SDMABORT_RECV_FAILURE								= 0x06;
	int		SDMABORT_RECV_TIMEOUT								= 0x07;
	int		SDMABORT_CONNECT_CLOSE								= 0x08;
	int		SDMABORT_PARSING_FAILURE							= 0x09;
	int		SDMABORT_ALERT_FROM_SERVER							= 0x10;
	int		SMMABORT_OMINIT_FAILURE								= 0x11;

	int		DM_SYNC_NONE										= 0x00;
	int		DM_SYNC_RUN											= 0x01;
	int		DM_SYNC_COMPLETE									= 0x02;
	int 	DM_SYNC_BOOTSTRAP 									= 0x03;

	int		SDM_MAX_AUTH_COUNT									= 3;
	int		SDM_HTTP_RETRY_COUNT								= 3;

	int		TYPE_STRING											= 0;
	int		TYPE_OPAQUE											= 1;
	int		TYPE_EXTENSION										= 2;

	int		DEFAULT_BUFFER_SIZE_HALF							= 16;
	int		DEFAULT_BUFFER_SIZE									= 32;
	int		DEFAULT_BUFFER_SIZE_2								= 64;
	int		DEFAULT_BUFFER_SIZE_3								= 128;
	int		DEFAULT_BUFFER_SIZE_4								= 256;
	long	DEFAULT_BUFFER_SIZE_5								= 512;
	long	DEFAULT_BIG_BUFFER_SIZE								= 1024;
	long	DEFAULT_BIG_BUFFER_SIZE_2							= 2048;
	long	DEFAULT_BUFFER_SIZE_PATH							= 300;

	int		AUTH_STATE_OK										= 1;
	int		AUTH_STATE_OK_PENDING								= 0;
	int		AUTH_STATE_FAIL										= -1;
	int		AUTH_STATE_INVALID_CRED								= -2;
	int		AUTH_STATE_MISSING_CRED								= -3;
	int		AUTH_STATE_PAYMENT_REQ								= -4;
	int		AUTH_STATE_TRY_LATER								= -5;
	int		AUTH_STATE_BUSY										= -6;
	int		AUTH_STATE_RETRY									= -7;
	int		AUTH_STATE_NONE										= -8;
	int		AUTH_STATE_REQUIRED									= -9;
	int		AUTH_STATE_NOTFOUND									= -10;
	int		AUTH_STATE_NO_CRED									= -11;
	int		AUTH_STATE_FORBIDDEN								= -12;
	int		AUTH_STATE_SERVERFAILURE							= -13;
	int		AUTH_STATE_DATA_STORE_FAILURE						= -14;
	int		AUTH_STATE_PROCESSING_FAILURE						= -15;
	int		AUTH_STATE_COMMAND_FAILURE							= -16;
	int		AUTH_STATE_PROTOCOL_VERSION_ERROR					= -17;
	int		AUTH_STATE_OPTIONAL_NOT_SUPPORTED					= -18;

	//
	int 	CRED_TYPE_NONE 										= -1;
	int 	CRED_TYPE_BASIC 									= 0;
	int		CRED_TYPE_MD5 										= 1;
	int 	CRED_TYPE_HMAC 										= 2;
	int 	CRED_TYPE_MD5_NOT_BASE64 							= 3;
	int 	CRED_TYPE_X509 										= 4;
	int 	CRED_TYPE_SECUREID 									= 5;
	int 	CRED_TYPE_SAFEWORD 									= 6;
	int 	CRED_TYPE_DIGIPASS 									= 7;
	int 	CRED_TYPE_SHA1 										= 8;

	String	CRED_TYPE_NONE_STR									= "NONE";
	String 	CRED_TYPE_BASIC_STR 								= "BASIC";
	String 	CRED_TYPE_MD5_STR 									= "MD5";
	String 	CRED_TYPE_HMAC_STR 									= "HMAC";
	String 	CRED_TYPE_DIGEST_STR 								= "DIGEST";
	String 	CRED_TYPE_STRING_BASIC 								= "syncml:auth-basic";
	String  CRED_TYPE_STRING_MD5 								= "syncml:auth-md5";
	String 	CRED_TYPE_STRING_HMAC 								= "syncml:auth-MAC";
	String 	CRED_TYPE_STRING_X509 								= "syncml:auth-X509";
	String 	CRED_TYPE_STRING_SECUREID 							= "syncml:auth-securid";
	String	CRED_TYPE_STRING_SAFEWORD 							= "syncml:auth-safeword";
	String 	CRED_TYPE_STRING_DIGIPASS 							= "syncml:auth-digipass";

	String 	AUTH_TYPE_NONE 										= "NONE";
	String 	AUTH_TYPE_BASIC 									= "BASIC";
	String 	AUTH_TYPE_DIGEST 									= "DIGEST";
	String 	AUTH_TYPE_HMAC										= "HMAC";
	String 	AUTH_TYPE_X509									 	= "X509";
	String	AUTH_TYPE_SECUREID 									= "SECUREID";
	String	AUTH_TYPE_SAFEWORD									= "SAFEWORD";
	String 	AUTH_TYPE_DIGIPASS 									= "DIGIPASS";

	int		WBXML_MAX_MESSAGE_SIZE								= 1024 * 5;
	int		WBXML_DM_MAX_MESSAGE_SIZE							= WBXML_MAX_MESSAGE_SIZE;
	int		WBXML_DM_ENCODING_BUF_SIZE							= 1024 * 7;
	int		WBXML_DM_MAX_OBJECT_SIZE							= 1024 * 1024;

	int		SDM_RET_ABORT										= 9;
	int		SDM_RET_FINISH										= 8;
	int		SDM_RET_EXEC_DOWNLOAD_COMPLETE						= 7;
	int		SDM_RET_EXEC_ALTERNATIVE_UPDATE						= 6;
	int		SDM_RET_EXEC_ALTERNATIVE_DOWNLOAD					= 5;
	int		SDM_RET_CHANGED_PROFILE								= 4;
	int		SDM_ALERT_SESSION_ABORT								= 3;
	int		SDM_RET_EXEC_ALTERNATIVE							= 2;
	int		SDM_RET_EXEC_REPLACE								= 1;
	int		SDM_RET_OK											= 0;
	int		SDM_RET_FAILED										= -1;
	int		SDM_RET_PARSE_ERROR									= -2;
	int		SDM_RECV_TIMEOUT									= -3;
	int		SDM_BUFFER_SIZE_EXCEEDED							= -4;
	int		SDM_PAUSED_BECAUSE_UIC_COMMAND						= -5;
	int		SDM_RET_AUTH_MAX_ERROR								= -6;
	int		SDM_RET_CONNECT_FAIL								= -7;
	int		SDM_UNKNOWN_ERROR									= -9;

	int		FORMAT_B64											= 1;
	int		FORMAT_BIN											= 2;
	int		FORMAT_BOOL											= 3;
	int		FORMAT_CHR											= 4;
	int		FORMAT_INT											= 5;
	int		FORMAT_NODE											= 6;
	int		FORMAT_NULL											= 7;
	int		FORMAT_XML											= 8;
	int		FORMAT_FLOAT										= 9;
	int		FORMAT_DATE											= 10;
	int		FORMAT_TIME											= 11;
	int		FORMAT_NONE											= 12;

	int		OM_NODENAME_MAXLENGTH							= 256;
	int		OM_MAX_ID_LENGTH								= 40;
	int		OM_BUFFER_SIZE									= 64;
	int		OM_MAX_ACL_NUM									= 16;
	int		OM_MAX_MIME_NUM									= 16;
	int 	OM_MAX_CHILD_NUM 								= 100;

	int 	OMACL_NONE 										= 0x00;
	int 	OMACL_ADD 										= 0x01;
	int 	OMACL_DELETE									= 0x02;
	int 	OMACL_EXEC 										= 0x04;
	int 	OMACL_GET 										= 0x08;
	int 	OMACL_REPLACE 									= 0x10;

	int		SCOPE_NONE											= 0;
	int		SCOPE_PERMANENT										= 1;
	int		SCOPE_DYNAMIC										= 2;

	int 	OMVFS_ERR_OK 										= 0;
	int 	OMVFS_ERR_INVALIDPARAMETER 							= -1;
	int 	OMVFS_ERR_NOEFFECT 									= -2;
	int 	OMVFS_ERR_BUFFER_NOT_ENOUGH 						= -3;
	int 	OMVFS_ERR_FAILED 									= -4;
	int 	OMVFS_ERR_NOSPACE 									= -5;

	long 	MAX_NODE_NUM 										= 1024;
	long 	MAX_NODENAME_SIZE 									= 256;
	long 	MAX_SPACE_SIZE 										= 40960;
	int		MAX_ACL_NUM											= 10;
	int		MAX_TYPE_NUM										= 10;

	int 	OMVFSPACK_STARTNODE 								= 0x42;
	int 	OMVFSPACK_ENDNODE 									= 0x44;
	int		OMVFSPACK_ACL										= 0x46;


	String	SYNCML_DEFAULT_CONREF								= "dataProxy";
	String	DEVINFO_DEFAULT_DMV1_1								= " 1.1";
	String	DEVINFO_DEFAULT_DMV1_2								= " 1.2";
	String	DEVINFO_DEFAULT_LANG								= "en-us";
	String	SYNCML_DEFAULT_TMONAME								= "TMOFOTA";

	String	DEVDETAIL_DEFAULT_URI_SUBNODE_VALUE					= "0";
	String	DEVDETAIL_DEFAULT_DEVTYPE							= "phone";
	String	DEVDETAIL_DEFAULT_LRGOBJ_SUPPORT					= "false";


	String  BASE_ACCOUNT_PATH_1_1 								= "./SyncML/DMAcc";
	String 	BASE_ACCOUNT_PATH 									= ".";
	String	 ATT_BASE_ACCOUNT_PATH 								= "./DMAcc";
	String	BASE_CON_PATH										= "./SyncML/Con";

	String	SYNCML_DMACC_PATH									= "./SyncML/DMAcc";
	String	SYNCML_DMACC_ADDR_PATH								= "/Addr";
	String	SYNCML_DMACC_ADDRTYPE_PATH							= "/AddrType";
	String	SYNCML_DMACC_PORTNBR_PATH							= "/PortNbr";
	String	SYNCML_DMACC_CONREF_PATH							= "/ConRef";
	String	SYNCML_DMACC_SERVERID_PATH_1_1						= "/ServerId";
	String	SYNCML_DMACC_SERVERPW_PATH							= "/ServerPW";
	String	SYNCML_DMACC_SERVERNONCE_PATH						= "/ServerNonce";
	String	SYNCML_DMACC_USERNAME_PATH							= "/UserName";
	String	SYNCML_DMACC_CLIENTPW_PATH							= "/ClientPW";
	String	SYNCML_DMACC_CLIENTNONCE_PATH						= "/ClientNonce";
	String	SYNCML_DMACC_AUTHPREF_PATH							= "/AuthPref";
	String	SYNCML_PATH											= "./SyncML";
	String	SYNCML_CON_PATH										= "./SyncML/Con";

	String	SYNCML_DMACC_APPID_PATH								= "/AppID";
	String	SYNCML_DMACC_SERVERID_PATH							= "/ServerID";
	String	SYNCML_DMACC_NAME_PATH								= "/Name";
	String	SYNCML_DMACC_PREFCONREF_PATH						= "/PrefConRef";
	String	SYNCML_DMACC_TOCONREF_PATH							= "/ToConRef";
	String	SYNCML_DMACC_APPADDR_PATH							= "/AppAddr";
	String	SYNCML_DMACC_AAUTHPREF_PATH							= "/AAuthPref";
	String	SYNCML_DMACC_APPAUTH_PATH							= "/AppAuth";
	String	SYNCML_DMACC_EXT_PATH								= "/Ext";

	String	SYNCML_TOCONREF_CONREF_PATH							= "/ConRef";

	String	SYNCML_APPADDR_ADDR_PATH							= "/Addr";
	String	SYNCML_APPADDR_ADDRTYPE_PATH						= "/AddrType";
	String	SYNCML_APPADDR_PORT_PATH							= "/Port";
	String	SYNCML_APPADDR_PORT_PORTNUMBER_PATH					= "/PortNbr";

	String	SYNCML_APPAUTH_AAUTHLEVEL_PATH						= "/AAuthLevel";
	String	SYNCML_APPAUTH_AAUTHTYPE_PATH						= "/AAuthType";
	String	SYNCML_APPAUTH_AAUTHNAME_PATH						= "/AAuthName";
	String	SYNCML_APPAUTH_AAUTHSECRET_PATH						= "/AAuthSecret";
	String	SYNCML_APPAUTH_AAUTHDATA_PATH						= "/AAuthData";

	String	SYNCML_CON_EXT_PATH									= "/Ext";
	String	SYNCML_CON_EXT_SERVICE_PATH							= "/Ext/Service";
	String	SYNCML_CON_NAP_PATH									= "/NAP";
	String	SYNCML_CON_NAP_BEARER_PATH							= "/NAP/Bearer";
	String	SYNCML_CON_NAP_ADDRTYPE_PATH						= "/NAP/AddrType";
	String	SYNCML_CON_NAP_ADDR_PATH							= "/NAP/Addr";
	String	SYNCML_CON_NAP_AUTH_PATH							= "/NAP/Auth";
	String	SYNCML_CON_NAP_AUTH_PAP_PATH						= "/NAP/Auth/PAP";
	String	SYNCML_CON_NAP_AUTH_PAP_ID_PATH						= "/NAP/Auth/PAP/Id";
	String	SYNCML_CON_NAP_AUTH_PAP_SEC_PATH					= "/NAP/Auth/PAP/Secret";
	String	SYNCML_CON_NAP_AUTH_CHAP_PATH						= "/NAP/Auth/CHAP";
	String	SYNCML_CON_NAP_AUTH_CHAP_ID_PATH					= "/NAP/Auth/CHAP/Id";
	String	SYNCML_CON_NAP_AUTH_CHAP_SEC_PATH					= "/NAP/Auth/CHAP/Secret";

	String	SYNCML_CON_PX_PATH									= "/PX";
	String	SYNCML_CON_PX_PORTNBR_PATH							= "/PX/PortNbr";
	String	SYNCML_CON_PX_ADDRTYPE_PATH							= "/PX/AddrType";
	String	SYNCML_CON_PX_ADDR_PATH								= "/PX/Addr";
	String	SYNCML_CON_PX_AUTH_PATH								= "/PX/Auth";

	String	DEVINFO_PATH										= "./DevInfo";
	String	DEVINFO_EXT_PATH									= "./DevInfo/Ext";
	String	DEVINFO_BEARER_PATH									= "./DevInfo/Bearer";
	String	DEVINFO_DEVID_PATH									= "./DevInfo/DevId";
	String	DEVINFO_MAN_PATH									= "./DevInfo/Man";
	String	DEVINFO_MOD_PATH									= "./DevInfo/Mod";
	String	DEVINFO_DMV_PATH									= "./DevInfo/DmV";
	String	DEVINFO_LANG_PATH									= "./DevInfo/Lang";

	String	DEVDETAIL_PATH										= "./DevDetail";
	String	DEVDETAIL_EXT_PATH									= "./DevDetail/Ext";
	String	DEVDETAIL_BEARER_PATH								= "./DevDetail/Bearer";
	String	DEVDETAIL_URI_PATH									= "./DevDetail/URI";
	String	DEVDETAIL_URI_MAXDEPTH_PATH							= "./DevDetail/URI/MaxDepth";
	String	DEVDETAIL_URI_MAXTOLEN_PATH							= "./DevDetail/URI/MaxTotLen";
	String	DEVDETAIL_URI_MAXSEGLEN_PATH						= "./DevDetail/URI/MaxSegLen";
	String	DEVDETAIL_DEVTYPE_PATH								= "./DevDetail/DevTyp";
	String	DEVDETAIL_OEM_PATH									= "./DevDetail/OEM";
	String	DEVDETAIL_FWV_PATH									= "./DevDetail/FwV";
	String	DEVDETAIL_SWV_PATH									= "./DevDetail/SwV";
	String	DEVDETAIL_HWV_PATH									= "./DevDetail/HwV";
	String	DEVDETAIL_LRGOBJ_PATH								= "./DevDetail/LrgObj";

	String 	DM_OMA_REPLACE 										= "/Update/PkgData";
	String 	DM_OMA_ALTERNATIVE 									= "/DownloadAndUpdate/PkgURL";
	String 	DM_OMA_ALTERNATIVE_2 								= "/Download/PkgURL";

	String 	DM_OMA_EXEC_REPLACE 								= "/Update";
	String 	DM_OMA_EXEC_ALTERNATIVE								= "/DownloadAndUpdate";
	String 	DM_OMA_EXEC_ALTERNATIVE_2 							= "/Download";

	String	DM_OMA_MECHANISM									= "/Mechanism";

	int 	DM_NOTI_RESYNC_MODE_FALSE						 	= 0;
	int 	DM_NOTI_RESYNC_MODE_TRUE 							= 1;


	String 	DM_VERDTD_1_1 										= "1.1";
	String 	DM_VERPROTO_1_1 									= "DM/1.1";
	String 	DM_WBXML_STRING_TABLE_1_1 							= "-//SYNCML//DTD SyncML 1.1//EN";
	String 	DM_VERDTD_1_2 										= "1.2";
	String 	DM_VERPROTO_1_2 									= "DM/1.2";
	String 	DM_WBXML_STRING_TABLE_1_2 							= "-//SYNCML//DTD SyncML 1.2//EN";

	String 	DM_DEV_INIT_ALERT_TYPE 								= "org.openmobilealliance.dm.firmwareupdate.devicerequest";
	String 	DM_USER_INIT_ALERT_TYPE 							= "org.openmobilealliance.dm.firmwareupdate.userrequest";
	String 	DM_UPDATE_REPORT_ALERT_TYPE_DOWNLOAD 				= "org.openmobilealliance.dm.firmwareupdate.download";
	String 	DM_UPDATE_REPORT_ALERT_TYPE_UPDATE 					= "org.openmobilealliance.dm.firmwareupdate.update";
	String 	DM_UPDATE_REPORT_ALERT_TYPE_DOWNLOAD_AND_UPDATE 	= "org.openmobilealliance.dm.firmwareupdate.downloadandupdate";

	String	SDM_DEFAULT_DISPLAY_UIC_OPTION						= "MINDT=30";

	String	SYNCML_MIME_TYPE_TNDS_XML							= "application/vnd.syncml.dmtnds+xml";
	String	SYNCML_MIME_TYPE_TNDS_WBXML							= "application/vnd.syncml.dmtnds+wbxml";

	String	NETWORK_TYPE_HTTPS									= "https";
	String	NETWORK_TYPE_HTTP									= "http";
	String	NETWORK_TYPE_WAP									= "wap";
	String	NETWORK_TYPE_OBEX									= "obex";

	int 	MAX_PORT_LENGTH 									= 6;

	int 	MAX_PROTOCOL_LENGTH 								= DEFAULT_BUFFER_SIZE_HALF;
	int 	MAX_URL_LENGTH 										= DEFAULT_BUFFER_SIZE_4;
	int 	MAX_APN_LENGTH 										= DEFAULT_BUFFER_SIZE_3;

	int 	TNDS_PROPERTY_ACL 									= 0x0001;
	int 	TNDS_PROPERTY_FORMAT 								= 0x0002;
	int 	TNDS_PROPERTY_TYPE 									= 0x0004;
	int 	TNDS_PROPERTY_VALUE 								= 0x0008;





    String DEBUG_DM 											= "DEBUG_DM";
	String DEBUG_DL 											= "DEBUG_DL";
	String  DEBUG_TASK											= "DEBUG_TASK";
	String  DEBUG_DB 											= "DEBUG_DB";
	String  DEBUG_NET 											= "DEBUG_NET";
	String  DEBUG_AUTH 											= "DEBUG_AUTH";
	String  DEBUG_UM 											= "DEBUG_UM";
	String  DEBUG_PARSER 										= "DEBUG_PARSER";
	String  DEBUG_UI                                            = "DEBUG_UI";

	String  DEBUG_EXCEPTION 									= "DEBUG_EXCEPTION";


	int		DM_RESULT_REPORT_SYNC								= 0;
	int		DM_RESULT_REPORT_ASYNC								= 1;
	int 	DM_RESULT_REPORT_GENRIC_COMPLETE 					= 2;

	int		SYNCML_DM_AGENT_DM									= 0;
	int		SYNCML_DM_AGENT_FUMO								= 1;

	int		DELTA_INTERIOR_MEMORY								= 0;
	int		DELTA_EXTERNAL_MEMORY								= 1;
	int		DELTA_EXTERNAL_SD_MEMORY							= 2;

	int 	DM_NONE_INIT 										= 0;
	int 	DM_USER_INIT 										= 1;
	int 	DM_DEVICE_INIT 										= 2;
}
