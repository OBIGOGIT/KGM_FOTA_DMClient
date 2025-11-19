package com.tsdm.core.data.constants

object FumoConst {

    // FUMO Mechanism
    const val DM_FUMO_MECHANISM_NONE: Int = 0
    const val DM_FUMO_MECHANISM_REPLACE: Int = 1
    const val DM_FUMO_MECHANISM_ALTERNATIVE: Int = 2
    const val DM_FUMO_MECHANISM_ALTERNATIVE_DOWNLOAD: Int = 3
    const val DM_FUMO_MECHANISM_UPDATE: Int = 4
    const val DM_FUMO_MECHANISM_END: Int = 5

    // FUMO State - spec
    const val DM_FUMO_STATE_NONE: Int = 0
    const val DM_FUMO_STATE_IDLE_START: Int = 10
    const val DM_FUMO_STATE_DOWNLOAD_FAILED: Int = 20
    const val DM_FUMO_STATE_DOWNLOAD_IN_PROGRESS: Int = 30
    const val DM_FUMO_STATE_DOWNLOAD_COMPLETE: Int = 40
    const val DM_FUMO_STATE_READY_TO_UPDATE: Int = 50
    const val DM_FUMO_STATE_UPDATE_IN_PROGRESS: Int = 60
    const val DM_FUMO_STATE_UPDATE_FAILED_HAVEDATA: Int = 70
    const val DM_FUMO_STATE_UPDATE_FAILED_NODATA: Int = 80
    const val DM_FUMO_STATE_UPDATE_SUCCESSFUL_HAVEDATA: Int = 90
    const val DM_FUMO_STATE_UPDATE_SUCCESSFUL_NODATA: Int = 100

    // FUMO State - not spec
    const val DM_FUMO_STATE_DOWNLOAD_DESCRIPTOR: Int = 200 // not spec
    const val DM_FUMO_STATE_START_TO_UPDATE: Int = 210 // not spec
    const val DM_FUMO_STATE_POSTPONE_TO_UPDATE: Int = 220 // not spec
    const val DM_FUMO_STATE_POSTPONE_TO_DOWNLOAD: Int = 221 // not spec
    const val DM_FUMO_STATE_DOWNLOAD_IN_CANCEL: Int = 230 // not spec
    const val DM_FUMO_STATE_USER_CANCEL_REPORTING: Int = 240 // not spec
    const val DM_FUMO_STATE_DOWNLOAD_FAILED_REPORTING: Int = 241
    const val DM_STATE_GET_SESSION_ID_START: Int = 242
    const val DM_FUMO_STATE_SUSPEND: Int = 250 // not spec

    // FUMO Path
    const val FUMO_DEFAULT_PKGNAME: String = "fota_delta_dp"
    const val FUMO_DEFAULT_PKGVERSION: String = "1.0"
    const val FUMO_PATH: String = "./FUMO"
    const val FUMO_PKGNAME_PATH: String = "/PkgName"
    const val FUMO_PKGVERSION_PATH: String = "/PkgVersion"
    const val FUMO_DOWNLOAD_PATH: String = "/Download"
    const val FUMO_PKGURL_PATH: String = "/PkgURL"
    const val FUMO_UPDATE_PATH: String = "/Update"
    const val FUMO_PKGDATA_PATH: String = "/PkgData"
    const val FUMO_DOWNLOADANDUPDATE_PATH: String = "/DownloadAndUpdate"
    const val FUMO_STATE_PATH: String = "/State"
    const val FUMO_EXT: String = "/Ext"

    // FUMO Download Result Code
    const val DL_GENERIC_SUCCESSFUL: String = "200"
    const val DL_GENERIC_SUCCESSFUL_UPDATE: String = "200"
    const val DL_GENERIC_SUCCESSFUL_DOWNLOAD: String = "200"
    const val DL_GENERIC_SUCCESSFUL_VENDOR_SPECIFIED: String = "250"
    const val DL_GENERIC_CLIENT_ERROR: String = "400"
    const val DL_GENERIC_USER_CANCELED_DOWNLOAD: String = "401"
    const val DL_GENERIC_CORRUPTED_FW_UP: String = "402"
    const val DL_GENERIC_PACKAGE_MISMATCH: String = "403"
    const val DL_GENERIC_FAILED_FW_UP_VALIDATION: String = "404"
    const val DL_GENERIC_NOT_ACCEPTABLE: String = "405"
    const val DL_GENERIC_AUTHENTICATION_FAILURE: String = "406"
    const val DL_GENERIC_REQUEST_TIME_OUT: String = "407"
    const val DL_GENERIC_NOT_IMPLEMENTED: String = "408"
    const val DL_GENERIC_UNDEFINED_ERROR: String = "409"
    const val DL_GENERIC_UPDATE_FAILED: String = "410"
    const val DL_GENERIC_BAD_URL: String = "411"
    const val DL_GENERIC_SERVER_UNAVAILABLE: String = "412"
    const val DL_GENERIC_SERVER_ERROR: String = "500"
    const val DL_GENERIC_DOWNLOAD_FAILED_OUT_MEMORY: String = "501"
    const val DL_GENERIC_UPDATE_FAILED_OUT_MEMORY: String = "502"
    const val DL_GENERIC_DOWNLOAD_FAILED_NETWORK: String = "503"
    const val DL_GENERIC_DOWNLOAD_FILE_ERROR: String = "952"
    const val DL_USER_CANCELED_DOWNLOAD: String = "902"

    // FUMO Update Result Code
    const val UPDATE_USER_CANCELED: String = "401"
    const val UPDATE_PARTITION: String = "190"
    const val UPDATE_START: String = "198"
    const val UPDATE_START_REBOOT: String = "199"
    const val UPDATE_SUCCESS: String = "200"
    const val UPDATE_FAIL: String = "400"
    const val UPDATE_STANDBY: String = "197"

    // FUMO File check Fail Index
    const val DL_MEMORY_INSUFFICIENT: Int = 2
    const val DL_OVER_OBJECT_SIZE: Int = 1

    // OMA DL Status
    const val OMA_DL_STAUS_SUCCESS: Int = 0
    const val OMA_DL_STATUS_MEMORY_ERROR: Int = 1
    const val OMA_DL_STATUS_USER_CANCEL: Int = 2
    const val OMA_DL_STATUS_LOSS_SERVICE: Int = 3
    const val OMA_DL_STATUS_ATTRIBUTE_MISMATCH: Int = 4
    const val OMA_DL_STATUS_INVALID_DESCRIPTOR: Int = 5
    const val OMA_DL_STATUS_INVALID_DDVERSIONV: Int = 6
    const val OMA_DL_STATUS_DEVICE_ABORTED: Int = 7
    const val OMA_DL_STATUS_NON_ACCEPTABLE_CONTENT: Int = 8
    const val OMA_DL_STATUS_LOADER_ERROR: Int = 9
    const val OMA_DL_STATUS_NONE: Int = 10

    const val SDL_RET_FAILED: Int = -2
    const val SDL_RET_UNKNOWN_DD: Int = -1
    const val SDL_RET_OK: Int = 0
    const val SDL_RET_CONTINUE: Int = 1
}