package com.tsdm.core.data.constants

object DmDevInfoConst {

    const val _SYNCML_TS_LOG_ON_: Boolean = true

    const val _SYNCML_TS_DM_VERSION_V11_: Boolean = false
    const val _SYNCML_TS_DM_VERSION_V12_: Boolean = true
    const val _SYNCML_TS_DM__VERSION_V12_NONCE_RESYNC: Boolean = false

    const val _SYNCML_TS_FOTA_: Boolean = true
    const val _SYNCML_TS_NONE_PROXY_: Boolean = false
    const val _SYNCML_TS_DM_REGISTRY_PROFILE_: Boolean = true
    const val _SYNCML_TS_DM_DELTA_EXTERNAL_STORAGE_: Boolean = false
    const val _SYNCML_TS_DM_DELTA_INTERIOR_MEMORY_STORAGE_: Boolean = true
    const val _SYNCML_TS_DM_DELTA_MULTI_MEMORY_STORAGE_: Boolean = false

    enum class SyncmlUICFlag {
        UIC_NONE,
        UIC_TRUE,
        UIC_FALSE,
        UIC_CANCELED,
        UIC_TIMEOUT
    };

    enum class SyncmlProcessingState {
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

    enum class SyncmlAtomicStep {
        ATOMIC_NONE,
        ATOMIC_STEP_ROLLBACK,
        ATOMIC_STEP_NOT_EXEC
    };

    enum class SyncmlState {
        DM_STATE_NONE,
        DM_STATE_INIT,
        DM_STATE_CLIENT_INIT_MGMT,
        DM_STATE_PROCESSING,
        DM_STATE_GENERIC_ALERT,
        DM_STATE_GENERIC_ALERT_REPORT,
        DM_STATE_ABORT_ALERT,
        DM_STATE_FINISH
    };

    const val PROCESS_STEP_NORMAL: Int = 0
    const val PROCESS_STEP_SEQUENCE: Int = 1
    const val PROCESS_STEP_ATOMIC: Int = 2
    const val PROCESS_STEP_FINISH: Int = 3

    const val SYNCMLAPPNONE: Int = -1
    const val SYNCMLDM: Int = 0
    const val SYNCMLDL: Int = 1
    const val SYNCMLMAXAPP: Int = 2

    const val NETWORK_SERVICE_NONE: Int = 0
    const val NETWORK_SERVICE_NOT_READY: Int = 1
    const val NETWORK_SERVICE_LIMITTED: Int = 2
    const val NETWORK_SERVICE_SEARCHING: Int = 3
    const val NETWORK_SERVICE_FULL_2G: Int = 4
    const val NETWORK_SERVICE_FULL_2_5G: Int = 5
    const val NETWORK_SERVICE_2_5G_EDGE: Int = 6
    const val NETWORK_SERVICE_FULL_3G: Int = 7
    const val NETWORK_SERVICE_HSDPA: Int = 8
    const val NETWORK_SERVICE_WLAN: Int = 9

    const val NETWORK_TYPE_UNKNOWN: Int = 0
    const val NETWORK_TYPE_GPRS: Int = 1
    const val NETWORK_TYPE_EDGE: Int = 2
    const val NETWORK_TYPE_UMTS: Int = 3

    const val NETWORK_STATE_NOT_USE: Int = 0
    const val NETWORK_STATE_NOT_READY: Int = 1
    const val NETWORK_STATE_SYNCML_USE: Int = 2
    const val NETWORK_STATE_APPLICATION_USE: Int = 3
    const val NETWORK_STATE_CALL_USE: Int = 4
    const val NETWORK_STATE_ALREADY_DOWNLOAD: Int = 5
    const val NETWORK_STATE_FDN_ENABLE: Int = 6

    const val SDMABORT_USER_REQUEST: Int = 0x01
    const val SDMABORT_TIMEOUT: Int = 0x02
    const val SDMABORT_AUTHENTICATION_FAILURE: Int = 0x03
    const val SDMABORT_AGENT_FAILURE: Int = 0x04
    const val SDMABORT_SEND_FAILURE: Int = 0x05
    const val SDMABORT_RECV_FAILURE: Int = 0x06
    const val SDMABORT_RECV_TIMEOUT: Int = 0x07
    const val SDMABORT_CONNECT_CLOSE: Int = 0x08
    const val SDMABORT_PARSING_FAILURE: Int = 0x09
    const val SDMABORT_ALERT_FROM_SERVER: Int = 0x10
    const val SMMABORT_OMINIT_FAILURE: Int = 0x11

    const val DM_SYNC_NONE: Int = 0x00
    const val DM_SYNC_RUN: Int = 0x01
    const val DM_SYNC_COMPLETE: Int = 0x02
    const val DM_SYNC_BOOTSTRAP: Int = 0x03

    const val SDM_MAX_AUTH_COUNT: Int = 3
    const val SDM_HTTP_RETRY_COUNT: Int = 3

    const val TYPE_STRING: Int = 0
    const val TYPE_OPAQUE: Int = 1
    const val TYPE_EXTENSION: Int = 2

    const val DEFAULT_BUFFER_SIZE_HALF: Int = 16
    const val DEFAULT_BUFFER_SIZE: Int = 32
    const val DEFAULT_BUFFER_SIZE_2: Int = 64
    const val DEFAULT_BUFFER_SIZE_3: Int = 128
    const val DEFAULT_BUFFER_SIZE_4: Int = 256
    const val DEFAULT_BUFFER_SIZE_5: Long = 512
    const val DEFAULT_BIG_BUFFER_SIZE: Long = 1024
    const val DEFAULT_BIG_BUFFER_SIZE_2: Long = 2048
    const val DEFAULT_BUFFER_SIZE_PATH: Long = 300

    const val AUTH_STATE_OK: Int = 1
    const val AUTH_STATE_OK_PENDING: Int = 0
    const val AUTH_STATE_FAIL: Int = -1
    const val AUTH_STATE_INVALID_CRED: Int = -2
    const val AUTH_STATE_MISSING_CRED: Int = -3
    const val AUTH_STATE_PAYMENT_REQ: Int = -4
    const val AUTH_STATE_TRY_LATER: Int = -5
    const val AUTH_STATE_BUSY: Int = -6
    const val AUTH_STATE_RETRY: Int = -7
    const val AUTH_STATE_NONE: Int = -8
    const val AUTH_STATE_REQUIRED: Int = -9
    const val AUTH_STATE_NOTFOUND: Int = -10
    const val AUTH_STATE_NO_CRED: Int = -11
    const val AUTH_STATE_FORBIDDEN: Int = -12
    const val AUTH_STATE_SERVERFAILURE: Int = -13
    const val AUTH_STATE_DATA_STORE_FAILURE: Int = -14
    const val AUTH_STATE_PROCESSING_FAILURE: Int = -15
    const val AUTH_STATE_COMMAND_FAILURE: Int = -16
    const val AUTH_STATE_PROTOCOL_VERSION_ERROR: Int = -17
    const val AUTH_STATE_OPTIONAL_NOT_SUPPORTED: Int = -18

    //
    const val CRED_TYPE_NONE: Int = -1
    const val CRED_TYPE_BASIC: Int = 0
    const val CRED_TYPE_MD5: Int = 1
    const val CRED_TYPE_HMAC: Int = 2
    const val CRED_TYPE_MD5_NOT_BASE64: Int = 3
    const val CRED_TYPE_X509: Int = 4
    const val CRED_TYPE_SECUREID: Int = 5
    const val CRED_TYPE_SAFEWORD: Int = 6
    const val CRED_TYPE_DIGIPASS: Int = 7
    const val CRED_TYPE_SHA1: Int = 8

    const val CRED_TYPE_NONE_STR: String = "NONE"
    const val CRED_TYPE_BASIC_STR: String = "BASIC"
    const val CRED_TYPE_MD5_STR: String = "MD5"
    const val CRED_TYPE_HMAC_STR: String = "HMAC"
    const val CRED_TYPE_DIGEST_STR: String = "DIGEST"
    const val CRED_TYPE_STRING_BASIC: String = "syncml:auth-basic"
    const val CRED_TYPE_STRING_MD5: String = "syncml:auth-md5"
    const val CRED_TYPE_STRING_HMAC: String = "syncml:auth-MAC"
    const val CRED_TYPE_STRING_X509: String = "syncml:auth-X509"
    const val CRED_TYPE_STRING_SECUREID: String = "syncml:auth-securid"
    const val CRED_TYPE_STRING_SAFEWORD: String = "syncml:auth-safeword"
    const val CRED_TYPE_STRING_DIGIPASS: String = "syncml:auth-digipass"

    const val AUTH_TYPE_NONE: String = "NONE"
    const val AUTH_TYPE_BASIC: String = "BASIC"
    const val AUTH_TYPE_DIGEST: String = "DIGEST"
    const val AUTH_TYPE_HMAC: String = "HMAC"
    const val AUTH_TYPE_X509: String = "X509"
    const val AUTH_TYPE_SECUREID: String = "SECUREID"
    const val AUTH_TYPE_SAFEWORD: String = "SAFEWORD"
    const val AUTH_TYPE_DIGIPASS: String = "DIGIPASS"

    const val WBXML_MAX_MESSAGE_SIZE: Int = 1024 * 5
    const val WBXML_DM_MAX_MESSAGE_SIZE: Int = WBXML_MAX_MESSAGE_SIZE
    const val WBXML_DM_ENCODING_BUF_SIZE: Int = 1024 * 7
    const val WBXML_DM_MAX_OBJECT_SIZE: Int = 1024 * 1024

    const val SDM_RET_ABORT: Int = 9
    const val SDM_RET_FINISH: Int = 8
    const val SDM_RET_EXEC_DOWNLOAD_COMPLETE: Int = 7
    const val SDM_RET_EXEC_ALTERNATIVE_UPDATE: Int = 6
    const val SDM_RET_EXEC_ALTERNATIVE_DOWNLOAD: Int = 5
    const val SDM_RET_CHANGED_PROFILE: Int = 4
    const val SDM_ALERT_SESSION_ABORT: Int = 3
    const val SDM_RET_EXEC_ALTERNATIVE: Int = 2
    const val SDM_RET_EXEC_REPLACE: Int = 1
    const val SDM_RET_OK: Int = 0
    const val SDM_RET_FAILED: Int = -1
    const val SDM_RET_PARSE_ERROR: Int = -2
    const val SDM_RECV_TIMEOUT: Int = -3
    const val SDM_BUFFER_SIZE_EXCEEDED: Int = -4
    const val SDM_PAUSED_BECAUSE_UIC_COMMAND: Int = -5
    const val SDM_RET_AUTH_MAX_ERROR: Int = -6
    const val SDM_RET_CONNECT_FAIL: Int = -7
    const val SDM_UNKNOWN_ERROR: Int = -9

    const val FORMAT_B64: Int = 1
    const val FORMAT_BIN: Int = 2
    const val FORMAT_BOOL: Int = 3
    const val FORMAT_CHR: Int = 4
    const val FORMAT_INT: Int = 5
    const val FORMAT_NODE: Int = 6
    const val FORMAT_NULL: Int = 7
    const val FORMAT_XML: Int = 8
    const val FORMAT_FLOAT: Int = 9
    const val FORMAT_DATE: Int = 10
    const val FORMAT_TIME: Int = 11
    const val FORMAT_NONE: Int = 12

    const val OM_NODENAME_MAXLENGTH: Int = 256
    const val OM_MAX_ID_LENGTH: Int = 40
    const val OM_BUFFER_SIZE: Int = 64
    const val OM_MAX_ACL_NUM: Int = 16
    const val OM_MAX_MIME_NUM: Int = 16
    const val OM_MAX_CHILD_NUM: Int = 100

    const val OMACL_NONE: Int = 0x00
    const val OMACL_ADD: Int = 0x01
    const val OMACL_DELETE: Int = 0x02
    const val OMACL_EXEC: Int = 0x04
    const val OMACL_GET: Int = 0x08
    const val OMACL_REPLACE: Int = 0x10

    const val SCOPE_NONE: Int = 0
    const val SCOPE_PERMANENT: Int = 1
    const val SCOPE_DYNAMIC: Int = 2

    const val OMVFS_ERR_OK: Int = 0
    const val OMVFS_ERR_INVALIDPARAMETER: Int = -1
    const val OMVFS_ERR_NOEFFECT: Int = -2
    const val OMVFS_ERR_BUFFER_NOT_ENOUGH: Int = -3
    const val OMVFS_ERR_FAILED: Int = -4
    const val OMVFS_ERR_NOSPACE: Int = -5

    const val MAX_NODE_NUM: Long = 1024
    const val MAX_NODENAME_SIZE: Long = 256
    const val MAX_SPACE_SIZE: Long = 40960
    const val MAX_ACL_NUM: Int = 10
    const val MAX_TYPE_NUM: Int = 10

    const val OMVFSPACK_STARTNODE: Int = 0x42
    const val OMVFSPACK_ENDNODE: Int = 0x44
    const val OMVFSPACK_ACL: Int = 0x46


    const val SYNCML_DEFAULT_CONREF: String = "dataProxy"
    const val DEVINFO_DEFAULT_DMV1_1: String = " 1.1"
    const val DEVINFO_DEFAULT_DMV1_2: String = " 1.2"
    const val DEVINFO_DEFAULT_LANG: String = "en-us"
    const val SYNCML_DEFAULT_TMONAME: String = "TMOFOTA"

    const val DEVDETAIL_DEFAULT_URI_SUBNODE_VALUE: String = "0"
    const val DEVDETAIL_DEFAULT_DEVTYPE: String = "phone"
    const val DEVDETAIL_DEFAULT_LRGOBJ_SUPPORT: String = "false"


    const val BASE_ACCOUNT_PATH_1_1: String = "./SyncML/DMAcc"
    const val BASE_ACCOUNT_PATH: String = "."
    const val ATT_BASE_ACCOUNT_PATH: String = "./DMAcc"
    const val BASE_CON_PATH: String = "./SyncML/Con"

    const val SYNCML_DMACC_PATH: String = "./SyncML/DMAcc"
    const val SYNCML_DMACC_ADDR_PATH: String = "/Addr"
    const val SYNCML_DMACC_ADDRTYPE_PATH: String = "/AddrType"
    const val SYNCML_DMACC_PORTNBR_PATH: String = "/PortNbr"
    const val SYNCML_DMACC_CONREF_PATH: String = "/ConRef"
    const val SYNCML_DMACC_SERVERID_PATH_1_1: String = "/ServerId"
    const val SYNCML_DMACC_SERVERPW_PATH: String = "/ServerPW"
    const val SYNCML_DMACC_SERVERNONCE_PATH: String = "/ServerNonce"
    const val SYNCML_DMACC_USERNAME_PATH: String = "/UserName"
    const val SYNCML_DMACC_CLIENTPW_PATH: String = "/ClientPW"
    const val SYNCML_DMACC_CLIENTNONCE_PATH: String = "/ClientNonce"
    const val SYNCML_DMACC_AUTHPREF_PATH: String = "/AuthPref"
    const val SYNCML_PATH: String = "./SyncML"
    const val SYNCML_CON_PATH: String = "./SyncML/Con"

    const val SYNCML_DMACC_APPID_PATH: String = "/AppID"
    const val SYNCML_DMACC_SERVERID_PATH: String = "/ServerID"
    const val SYNCML_DMACC_NAME_PATH: String = "/Name"
    const val SYNCML_DMACC_PREFCONREF_PATH: String = "/PrefConRef"
    const val SYNCML_DMACC_TOCONREF_PATH: String = "/ToConRef"
    const val SYNCML_DMACC_APPADDR_PATH: String = "/AppAddr"
    const val SYNCML_DMACC_AAUTHPREF_PATH: String = "/AAuthPref"
    const val SYNCML_DMACC_APPAUTH_PATH: String = "/AppAuth"
    const val SYNCML_DMACC_EXT_PATH: String = "/Ext"

    const val SYNCML_TOCONREF_CONREF_PATH: String = "/ConRef"

    const val SYNCML_APPADDR_ADDR_PATH: String = "/Addr"
    const val SYNCML_APPADDR_ADDRTYPE_PATH: String = "/AddrType"
    const val SYNCML_APPADDR_PORT_PATH: String = "/Port"
    const val SYNCML_APPADDR_PORT_PORTNUMBER_PATH: String = "/PortNbr"

    const val SYNCML_APPAUTH_AAUTHLEVEL_PATH: String = "/AAuthLevel"
    const val SYNCML_APPAUTH_AAUTHTYPE_PATH: String = "/AAuthType"
    const val SYNCML_APPAUTH_AAUTHNAME_PATH: String = "/AAuthName"
    const val SYNCML_APPAUTH_AAUTHSECRET_PATH: String = "/AAuthSecret"
    const val SYNCML_APPAUTH_AAUTHDATA_PATH: String = "/AAuthData"

    const val SYNCML_CON_EXT_PATH: String = "/Ext"
    const val SYNCML_CON_EXT_SERVICE_PATH: String = "/Ext/Service"
    const val SYNCML_CON_NAP_PATH: String = "/NAP"
    const val SYNCML_CON_NAP_BEARER_PATH: String = "/NAP/Bearer"
    const val SYNCML_CON_NAP_ADDRTYPE_PATH: String = "/NAP/AddrType"
    const val SYNCML_CON_NAP_ADDR_PATH: String = "/NAP/Addr"
    const val SYNCML_CON_NAP_AUTH_PATH: String = "/NAP/Auth"
    const val SYNCML_CON_NAP_AUTH_PAP_PATH: String = "/NAP/Auth/PAP"
    const val SYNCML_CON_NAP_AUTH_PAP_ID_PATH: String = "/NAP/Auth/PAP/Id"
    const val SYNCML_CON_NAP_AUTH_PAP_SEC_PATH: String = "/NAP/Auth/PAP/Secret"
    const val SYNCML_CON_NAP_AUTH_CHAP_PATH: String = "/NAP/Auth/CHAP"
    const val SYNCML_CON_NAP_AUTH_CHAP_ID_PATH: String = "/NAP/Auth/CHAP/Id"
    const val SYNCML_CON_NAP_AUTH_CHAP_SEC_PATH: String = "/NAP/Auth/CHAP/Secret"

    const val SYNCML_CON_PX_PATH: String = "/PX"
    const val SYNCML_CON_PX_PORTNBR_PATH: String = "/PX/PortNbr"
    const val SYNCML_CON_PX_ADDRTYPE_PATH: String = "/PX/AddrType"
    const val SYNCML_CON_PX_ADDR_PATH: String = "/PX/Addr"
    const val SYNCML_CON_PX_AUTH_PATH: String = "/PX/Auth"

    const val DEVINFO_PATH: String = "./DevInfo"
    const val DEVINFO_EXT_PATH: String = "./DevInfo/Ext"
    const val DEVINFO_BEARER_PATH: String = "./DevInfo/Bearer"
    const val DEVINFO_DEVID_PATH: String = "./DevInfo/DevId"
    const val DEVINFO_MAN_PATH: String = "./DevInfo/Man"
    const val DEVINFO_MOD_PATH: String = "./DevInfo/Mod"
    const val DEVINFO_DMV_PATH: String = "./DevInfo/DmV"
    const val DEVINFO_LANG_PATH: String = "./DevInfo/Lang"

    const val DEVDETAIL_PATH: String = "./DevDetail"
    const val DEVDETAIL_EXT_PATH: String = "./DevDetail/Ext"
    const val DEVDETAIL_BEARER_PATH: String = "./DevDetail/Bearer"
    const val DEVDETAIL_URI_PATH: String = "./DevDetail/URI"
    const val DEVDETAIL_URI_MAXDEPTH_PATH: String = "./DevDetail/URI/MaxDepth"
    const val DEVDETAIL_URI_MAXTOLEN_PATH: String = "./DevDetail/URI/MaxTotLen"
    const val DEVDETAIL_URI_MAXSEGLEN_PATH: String = "./DevDetail/URI/MaxSegLen"
    const val DEVDETAIL_DEVTYPE_PATH: String = "./DevDetail/DevTyp"
    const val DEVDETAIL_OEM_PATH: String = "./DevDetail/OEM"
    const val DEVDETAIL_FWV_PATH: String = "./DevDetail/FwV"
    const val DEVDETAIL_SWV_PATH: String = "./DevDetail/SwV"
    const val DEVDETAIL_HWV_PATH: String = "./DevDetail/HwV"
    const val DEVDETAIL_LRGOBJ_PATH: String = "./DevDetail/LrgObj"

    const val DM_OMA_REPLACE: String = "/Update/PkgData"
    const val DM_OMA_ALTERNATIVE: String = "/DownloadAndUpdate/PkgURL"
    const val DM_OMA_ALTERNATIVE_2: String = "/Download/PkgURL"

    const val DM_OMA_EXEC_REPLACE: String = "/Update"
    const val DM_OMA_EXEC_ALTERNATIVE: String = "/DownloadAndUpdate"
    const val DM_OMA_EXEC_ALTERNATIVE_2: String = "/Download"

    const val DM_OMA_MECHANISM: String = "/Mechanism"

    const val DM_NOTI_RESYNC_MODE_FALSE: Int = 0
    const val DM_NOTI_RESYNC_MODE_TRUE: Int = 1


    const val DM_VERDTD_1_1: String = "1.1"
    const val DM_VERPROTO_1_1: String = "DM/1.1"
    const val DM_WBXML_STRING_TABLE_1_1: String = "-//SYNCML//DTD SyncML 1.1//EN"
    const val DM_VERDTD_1_2: String = "1.2"
    const val DM_VERPROTO_1_2: String = "DM/1.2"
    const val DM_WBXML_STRING_TABLE_1_2: String = "-//SYNCML//DTD SyncML 1.2//EN"

    const val DM_DEV_INIT_ALERT_TYPE: String = "org.openmobilealliance.dm.firmwareupdate.devicerequest"
    const val DM_USER_INIT_ALERT_TYPE: String = "org.openmobilealliance.dm.firmwareupdate.userrequest"
    const val DM_UPDATE_REPORT_ALERT_TYPE_DOWNLOAD: String = "org.openmobilealliance.dm.firmwareupdate.download"
    const val DM_UPDATE_REPORT_ALERT_TYPE_UPDATE: String = "org.openmobilealliance.dm.firmwareupdate.update"
    const val DM_UPDATE_REPORT_ALERT_TYPE_DOWNLOAD_AND_UPDATE: String = "org.openmobilealliance.dm.firmwareupdate.downloadandupdate"

    const val SDM_DEFAULT_DISPLAY_UIC_OPTION: String = "MINDT=30"

    const val SYNCML_MIME_TYPE_TNDS_XML: String = "application/vnd.syncml.dmtnds+xml"
    const val SYNCML_MIME_TYPE_TNDS_WBXML: String = "application/vnd.syncml.dmtnds+wbxml"

    const val NETWORK_TYPE_HTTPS: String = "https"
    const val NETWORK_TYPE_HTTP: String = "http"
    const val NETWORK_TYPE_WAP: String = "wap"
    const val NETWORK_TYPE_OBEX: String = "obex"

    const val MAX_PORT_LENGTH: Int = 6

    const val MAX_PROTOCOL_LENGTH: Int = DEFAULT_BUFFER_SIZE_HALF
    const val MAX_URL_LENGTH: Int = DEFAULT_BUFFER_SIZE_4
    const val MAX_APN_LENGTH: Int = DEFAULT_BUFFER_SIZE_3

    const val TNDS_PROPERTY_ACL: Int = 0x0001
    const val TNDS_PROPERTY_FORMAT: Int = 0x0002
    const val TNDS_PROPERTY_TYPE: Int = 0x0004
    const val TNDS_PROPERTY_VALUE: Int = 0x0008


    const val DEBUG_DM: String = "DEBUG_DM"
    const val DEBUG_DL: String = "DEBUG_DL"
    const val DEBUG_TASK: String = "DEBUG_TASK"
    const val DEBUG_DB: String = "DEBUG_DB"
    const val DEBUG_NET: String = "DEBUG_NET"
    const val DEBUG_AUTH: String = "DEBUG_AUTH"
    const val DEBUG_UM: String = "DEBUG_UM"
    const val DEBUG_PARSER: String = "DEBUG_PARSER"
    const val DEBUG_UI: String = "DEBUG_UI"

    const val DEBUG_EXCEPTION: String = "DEBUG_EXCEPTION"


    const val DM_RESULT_REPORT_SYNC: Int = 0
    const val DM_RESULT_REPORT_ASYNC: Int = 1
    const val DM_RESULT_REPORT_GENRIC_COMPLETE: Int = 2

    const val SYNCML_DM_AGENT_DM: Int = 0
    const val SYNCML_DM_AGENT_FUMO: Int = 1

    const val DELTA_INTERIOR_MEMORY: Int = 0
    const val DELTA_EXTERNAL_MEMORY: Int = 1
    const val DELTA_EXTERNAL_SD_MEMORY: Int = 2

    const val DM_NONE_INIT: Int = 0
    const val DM_USER_INIT: Int = 1
    const val DM_DEVICE_INIT: Int = 2
}