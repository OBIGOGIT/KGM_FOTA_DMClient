package com.tsdm.core.data.constants

object DmUiEvent {

    const val EVENT_UI_SYNC_START: Int = 101
    const val EVENT_UI_SERVER_CONNECT: Int = 102
    const val DM_EVENT_UI_UIC_REQUEST: Int = 103
    const val DM_EVENT_UI_IN_SYNC: Int = 104
    const val DM_EVENT_UI_NOT_INIT: Int = 105
    const val DM_EVENT_UI_SERVER_CONNECT_FAIL: Int = 106
    const val DM_EVENT_UI_NETWORK_ERR: Int = 107
    const val DM_EVENT_UI_ABORT_BYUSER: Int = 108
    const val DM_EVENT_UI_RECV_FAIL: Int = 109
    const val DM_EVENT_UI_SEND_FAIL: Int = 110
    const val DM_EVENT_UI_HTTP_INTERNAL_ERROR: Int = 111
    const val DM_EVENT_UI_SYNC_ERROR: Int = 112
    const val DM_EVENT_UI_DOWNLOAD_FAIL_RETRY_CONFIRM: Int = 113
    const val DM_EVENT_UI_DOWNLOAD_FAILED_NETWORK_DISCONNECTED: Int = 114

    const val DM_EVENT_UI_NOTI_NOT_SPECIFIED: Int = 121
    const val DM_EVENT_UI_NOTI_BACKGROUND: Int = 122
    const val DM_EVENT_UI_NOTI_INFORMATIVE: Int = 123
    const val DM_EVENT_UI_NOTI_INTERACTIVE: Int = 124
    const val DM_EVENT_UI_IDLE_STATE: Int = 125
    const val DM_EVENT_UI_NOTI_INFORM: Int = 126
    const val DM_EVENT_UI_DOWNLOAD_START_CONFIRM: Int = 127
    const val DM_EVENT_UI_WIFI_DISCONNECTED: Int = 128
    const val DM_EVENT_UI_FDN_ENABLE: Int = 129
    const val DM_EVENT_UI_REMOTE_CLOSED: Int = 130
    const val DM_EVENT_UI_FINISH: Int = 131

    const val DM_UI_BOOTSTRAP_INSTALL: Int = 141
    const val DM_UI_BOOTSTRAP_INSTALL_RETRY: Int = 142
    const val DM_UI_BOOTSTRAP_INSTALL_SUCCESS: Int = 143
    const val DM_UI_BOOTSTRAP_INSTALL_FAIL: Int = 144

    const val DL_EVENT_UI_DOWNLOAD_OPEN_COMMUNICATION: Int = 151
    const val DL_EVENT_UI_DOWNLOAD_YES_NO: Int = 152
    const val DL_EVENT_UI_DOWNLOAD_IN_PROGRESS: Int = 153
    const val DL_EVENT_UI_DOWNLOAD_IN_COMPLETE: Int = 154
    const val DL_EVENT_UI_DM_START_CONFIRM: Int = 155

    const val DL_EVENT_UI_UPDATE_CONFIRM: Int = 160
    const val DL_EVENT_UI_UPDATE_START: Int = 161
    const val DL_EVENT_UI_UPDATE_PRE_START: Int = 162
    const val DL_EVENT_UI_PHONE_REBOOT: Int = 163
    const val DL_EVENT_UI_DOWNLOAD_COMPLETE_SUSPEND: Int = 164
    const val DL_EVENT_UI_DOWNLOAD_COMPLETE_LOW_BATTARY: Int = 165
    const val DL_EVENT_UI_DRAW_DOWNLOAD_PERCENTAGE: Int = 166
    const val DL_EVENT_UI_UPDATE_PLEASE_WAIT: Int = 167

    const val DL_EVNET_UI_UPDATE_SUCCESS: Int = 171
    const val DL_EVENT_UI_UPDATE_FAIL: Int = 172
    const val DL_EVNET_UI_RESUME_DOWNLOAD: Int = 173
    const val DL_EVENT_UI_POSTPONE: Int = 174
    const val DL_EVENT_UI_DOWNLOAD_FAILED_REPORTING: Int = 175
    const val DL_EVENT_UI_USER_CANCEL_REPORTING: Int = 176
    const val DL_EVENT_UI_DOWNLOAD_FAILED: Int = 177
    const val DL_EVENT_UI_DOWNLOAD_IN_CANCEL: Int = 178
    const val DL_EVENT_UI_MEMORY_FULL: Int = 181
    const val DL_EVENT_UI_DELTA_OVER_SIZE: Int = 182

    const val DL_EVENT_UI_ALERT_GARAGE: Int = 183
}