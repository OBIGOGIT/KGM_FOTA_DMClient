package com.tsdm.core.data.constants

object DmTaskMsg {
    const val TASK_MSG_OS_INITIALIZED: Int = 0
    const val TASK_MSG_PHONEBOOK_INITIALIZED: Int = 1
    const val TASK_MSG_NETWORK_STATUS_UPDATED: Int = 2
    const val TASK_MSG_DM_SYNCML_CONNECT: Int = 11
    const val TASK_MSG_DM_SYNCML_INIT: Int = 12
    const val TASK_MSG_DM_SYNCML_IDLE_STATE: Int = 13

    // DM TASK //
    const val TASK_MSG_DM_SYNCML_CONNECTFAIL: Int = 21
    const val TASK_MSG_DM_SYNCML_CONNECTRETRY: Int = 22
    const val TASK_MSG_DM_SYNCML_START: Int = 23
    const val TASK_MSG_DM_SYNCML_CONTINUE: Int = 24
    const val TASK_DM_SYNCML_ABORT: Int = 25
    const val TASK_MSG_DM_SYNCML_FINISH: Int = 26
    const val TASK_MSG_DM_SYNCML_SENDFAIL: Int = 27
    const val TASK_MSG_DM_SYNCML_RECEIVEFAIL: Int = 28
    const val TASK_MSG_DM_OBEX_DEVICE_ACTIVATE: Int = 29
    const val TASK_MSG_DM_OBEX_DATA_RECEIVED: Int = 30
    const val TASK_MSG_DM_OBEX_DEVICE_DEACTIVATE: Int = 31

    const val TASK_MSG_DM_TCPIP_OPEN: Int = 32
    const val TASK_MSG_DM_TCPIP_SEND: Int = 33
    const val TASK_MSG_DM_TCPIP_CLOSE: Int = 34
    const val TASK_MSG_DM_SOCKET_CONNECTED: Int = 35
    const val TASK_MSG_DM_SOCKET_DISCONNECTED: Int = 36
    const val TASK_MSG_DM_SOCKET_DATA_RECEIVED: Int = 37
    const val TASK_MSG_DM_SOCKET_SSL_TUNNEL_CONNECT: Int = 38
    const val TASK_MSG_DM_TCPAPN_OPEN: Int = 39

    const val TASK_MSG_DM_DDF_PARSER_ACTIVE: Int = 61
    const val TASK_MSG_DM_DDF_PARSER_PROCESS: Int = 62

    const val TASK_MSG_DM_POLLING_UPDATE: Int = 70
    const val TASK_MSG_DM_AUTO_UPDATE_INITIATED: Int = 71

    const val TASK_MSG_DM_CLEAR_SESSION: Int = 99


    // DL TASK //
    const val TASK_MSG_DL_SYNCML_CONNECT: Int = 112
    const val TASK_MSG_DL_SYNCML_CONNECTFAIL: Int = 113
    const val TASK_MSG_DL_SYNCML_ABORT: Int = 114
    const val TASK_MSG_DL_SYNCML_CONNECTRETRY: Int = 115
    const val TASK_MSG_DL_SYNCML_START: Int = 116
    const val TASK_MSG_DL_TCPIP_OPEN: Int = 117
    const val TASK_MSG_DL_TCPIP_SEND: Int = 118
    const val TASK_MSG_DL_TCPIP_CLOSE: Int = 119
    const val TASK_MSG_DL_SYNCML_CONTINUE: Int = 120
    const val TASK_MSG_DL_SOCKET_DATA_RECEIVED: Int = 121
    const val TASK_MSG_DL_SOCKET_SSL_TUNNEL_CONNECT: Int = 122
    const val TASK_MSG_DL_SYNCML_FINISH: Int = 123
    const val TASK_MSG_DL_SYNCML_SENDFAIL: Int = 124
    const val TASK_MSG_DL_SYNCML_RECEIVEFAIL: Int = 125
    const val TASK_MSG_DL_TCPAPN_OPEN: Int = 126

    const val TASK_MSG_UIC_REQUEST: Int = 128
    const val TASK_MSG_UIC_RESPONSE: Int = 129

    // for Firmware Update.
    const val TASK_MSG_DL_FIRMWARE_UPDATE: Int = 272
    const val TASK_MSG_DL_LOW_BATTERY_BEFORE_DOWNLOAD: Int = 273
    const val TASK_MSG_DL_POSTPONE: Int = 274
    const val TASK_MSG_DL_USER_CANCEL_DOWNLOAD: Int = 275
    const val TASK_MSG_DL_DOWNLOAD_FILE_ERROR: Int = 276
    const val TASK_MSG_DL_USER_SUSPEND: Int = 277


    const val TASK_MSG_DL_FIRMWARE_UPDATE_STANDBY: Int = 279
    const val TASK_MSG_DL_FIRMWARE_UPDATE_START: Int = 280
    const val TASK_MSG_DL_FIRMWARE_UPDATE_SUCCESS: Int = 281
    const val TASK_MSG_DL_FIRMWARE_UPDATE_FAIL: Int = 282
    const val TASK_MSG_DL_FIRMWARE_UPDATE_USERCANCEL: Int = 283
    const val TASK_MSG_DL_FIRMWARE_UPDATE_PARTITION: Int = 284

    const val TASK_ABORT_PARSING_FAIL: Int = 0xFF
    const val TASK_ABORT_CONNECTION_FAIL: Int = 0xFE
    const val TASK_ABORT_SEND_FAIL: Int = 0xFD
    const val TASK_ABORT_RECEIVE_FAIL: Int = 0XFC
    const val TASK_ABORT_USER_REQ: Int = 0xFB
    const val TASK_ABORT_USB_DEACTIVATE: Int = 0xFA
    const val TASK_ABORT_HTTP_ERROR: Int = 0xF9
    const val TASK_ABORT_OBEX_ERROR: Int = 0xF8
    const val TASK_ABORT_AUTH_FAIL: Int = 0xF7
    const val TASK_ABORT_INTERNAL_SERVER_ERROR: Int = 0xF6
    const val TASK_ABORT_SYNCDM_ERROR: Int = 0xF5
    const val TASK_ABORT_SYNC_RETRY: Int = 0xF4
    const val TASK_ABORT_SYNC_AIRPLAIN_MODE: Int = 0xF3
    const val TASK_ABORT_ALERT_DISPLAY: Int = 0xF2
    const val TASK_ABORT_ERROR: Int = 0xC0
}