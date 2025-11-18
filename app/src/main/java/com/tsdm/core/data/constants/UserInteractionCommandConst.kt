package com.tsdm.core.data.constants

object UserInteractionCommandConst {
    const val UIC_SAVE_NONE: Int = 0x00
    const val UIC_SAVE_OK: Int = 0x01

    const val UIC_MAX_TITLE_SIZE: Int = 128
    const val UIC_MAX_USERINPUT_SIZE: Int = 128
    const val UIC_MAX_CHOICE_MENU: Int = 32

    const val UIC_TYPE_NONE: Int = 0x00
    const val UIC_TYPE_DISP: Int = 0x01
    const val UIC_TYPE_CONFIRM: Int = 0x02
    const val UIC_TYPE_INPUT: Int = 0x03
    const val UIC_TYPE_SINGLE_CHOICE: Int = 0x04
    const val UIC_TYPE_MULTI_CHOICE: Int = 0x05
    const val UIC_TYPE_PROGR: Int = 0x06

    const val UIC_INPUTTYPE_ALPHANUMERIC: Int = 0x01
    const val UIC_INPUTTYPE_NUMERIC: Int = 0x02
    const val UIC_INPUTTYPE_DATE: Int = 0x03
    const val UIC_INPUTTYPE_TIME: Int = 0x04
    const val UIC_INPUTTYPE_PHONENUBMER: Int = 0x05
    const val UIC_INPUTTYPE_IPADDRESS: Int = 0x06
    const val UIC_ECHOTYPE_TEXT: Int = 0x01
    const val UIC_ECHOTYPE_PASSWORD: Int = 0x02

    const val UIC_RESULT_OK: Int = 0x00

    // used confirm
    const val UIC_RESULT_YES: Int = 0x01
    const val UIC_RESULT_REJECT: Int = 0x02

    const val UIC_RESULT_SINGLE_CHOICE: Int = 0x04
    const val UIC_RESULT_MULTI_CHOICE: Int = 0x05

    const val UIC_RESULT_TIMEOUT: Int = 0x10
    const val UIC_RESULT_NOT_EXCUTED: Int = 0x11

}