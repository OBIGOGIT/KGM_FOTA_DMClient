package com.tsdm.core.data.constants

object WbxmlProtocolConst {

    const val DM_ERR_OK: Int = 0
    const val DM_ERR_FAIL: Int = 1
    const val DM_ERR_UNKNOWN_ELEMENT: Int = 2
    const val DM_ERR_BUFFER_TOO_SMALL: Int = 3
    const val DM_ERR_INVALID_PARAMETER: Int = 4
    const val DM_ERR_ZEROBIT_TAG: Int = 5


    const val WBXML_VERSION: Int = 0x02
    const val WBXML_CHARSET_UTF8: Int = 0x6a

    /* WBXML Global Tokens */
    const val WBXML_SWITCH_PAGE: Int = 0x00
    const val WBXML_END: Int = 0x01
    const val WBXML_ENTITY: Int = 0x02
    const val WBXML_STR_I: Int = 0x03
    const val WBXML_LITERAL: Int = 0x04
    const val WBXML_EXT_I_0: Int = 0x40
    const val WBXML_EXT_I_1: Int = 0x41
    const val WBXML_EXT_I_2: Int = 0x42
    const val WBXML_PI: Int = 0x43
    const val WBXML_LITERAL_C: Int = 0x44
    const val WBXML_EXT_T_0: Int = 0x80
    const val WBXML_EXT_T_1: Int = 0x81
    const val WBXML_EXT_T_2: Int = 0x82
    const val WBXML_STR_T: Int = 0x83
    const val WBXML_LITERAL_A: Int = 0x84
    const val WBXML_EXT_0: Int = 0xC0
    const val WBXML_EXT_1: Int = 0xC1
    const val WBXML_EXT_2: Int = 0xC2
    const val WBXML_OPAQUE: Int = 0xC3
    const val WBXML_LITERAL_AC: Int = 0xC4

    /* WBXML Tokens Masks */
    const val WBXML_TOKEN_MASK: Int = 0x3F
    const val WBXML_TOKEN_WITH_ATTRS: Int = 0x80
    const val WBXML_TOKEN_WITH_CONTENT: Int = 0x40

    const val WBXML_PUBLICID_TOKEN_V10: Int = 0xFD1
    const val WBXML_PUBLICID_TOKEN_V11: Int = 0xFD3
    const val WBXML_PUBLICID_TOKEN_V12: Int = 0x1201

    const val WBXML_PAGE_SYNCML: Int = 0x00
    const val WBXML_PAGE_METINF: Int = 0x01
    const val WBXML_PAGE_DEVINF: Int = 0x02

    /* SyncML WBXML element id */
    const val WBXML_TAG_Add: Int = 0x05
    const val WBXML_TAG_Alert: Int = 0x06
    const val WBXML_TAG_Archive: Int = 0x07
    const val WBXML_TAG_Atomic: Int = 0x08
    const val WBXML_TAG_Chal: Int = 0x09
    const val WBXML_TAG_Cmd: Int = 0x0a
    const val WBXML_TAG_CmdID: Int = 0x0b
    const val WBXML_TAG_CmdRef: Int = 0x0c
    const val WBXML_TAG_Copy: Int = 0x0d
    const val WBXML_TAG_Cred: Int = 0x0e
    const val WBXML_TAG_Data: Int = 0x0f
    const val WBXML_TAG_Delete: Int = 0x10
    const val WBXML_TAG_Exec: Int = 0x11
    const val WBXML_TAG_Final: Int = 0x12
    const val WBXML_TAG_Get: Int = 0x13
    const val WBXML_TAG_Item: Int = 0x14
    const val WBXML_TAG_Lang: Int = 0x15
    const val WBXML_TAG_LocName: Int = 0x16
    const val WBXML_TAG_LocURI: Int = 0x17
    const val WBXML_TAG_Map: Int = 0x18
    const val WBXML_TAG_MapItem: Int = 0x19
    const val WBXML_TAG_Meta: Int = 0x1a
    const val WBXML_TAG_MsgID: Int = 0x1b
    const val WBXML_TAG_MsgRef: Int = 0x1c
    const val WBXML_TAG_NoResp: Int = 0x1d
    const val WBXML_TAG_NoResults: Int = 0x1e
    const val WBXML_TAG_Put: Int = 0x1f
    const val WBXML_TAG_Replace: Int = 0x20
    const val WBXML_TAG_RespURI: Int = 0x21
    const val WBXML_TAG_Results: Int = 0x22
    const val WBXML_TAG_Search: Int = 0x23
    const val WBXML_TAG_Sequence: Int = 0x24
    const val WBXML_TAG_SessionID: Int = 0x25
    const val WBXML_TAG_SftDel: Int = 0x26
    const val WBXML_TAG_Source: Int = 0x27
    const val WBXML_TAG_SourceRef: Int = 0x28
    const val WBXML_TAG_Status: Int = 0x29
    const val WBXML_TAG_Sync: Int = 0x2a
    const val WBXML_TAG_SyncBody: Int = 0x2b
    const val WBXML_TAG_SyncHdr: Int = 0x2c
    const val WBXML_TAG_SyncML: Int = 0x2d
    const val WBXML_TAG_Target: Int = 0x2e
    const val WBXML_TAG_TargetRef: Int = 0x2f
    const val WBXML_TAG_NULL: Int = 0x30
    const val WBXML_TAG_VerDTD: Int = 0x31
    const val WBXML_TAG_VerProto: Int = 0x32
    const val WBXML_TAG_NumberOfChanges: Int = 0x33
    const val WBXML_TAG_MoreData: Int = 0x34
    const val WBXML_TAG_Correlator: Int = 0x3C

    const val WBXML_METINF_Anchor: Int = 0x05
    const val WBXML_METINF_EMI: Int = 0x06
    const val WBXML_METINF_Format: Int = 0x07
    const val WBXML_METINF_FreeID: Int = 0x08
    const val WBXML_METINF_FreeMem: Int = 0x09
    const val WBXML_METINF_Last: Int = 0x0a
    const val WBXML_METINF_Mark: Int = 0x0b
    const val WBXML_METINF_MaxMsgSize: Int = 0x0c
    const val WBXML_METINF_Mem: Int = 0x0d
    const val WBXML_METINF_MetInf: Int = 0x0e
    const val WBXML_METINF_Next: Int = 0x0f
    const val WBXML_METINF_NextNonce: Int = 0x10
    const val WBXML_METINF_SharedMem: Int = 0x11
    const val WBXML_METINF_Size: Int = 0x12
    const val WBXML_METINF_Type: Int = 0x13
    const val WBXML_METINF_Version: Int = 0x14
    const val WBXML_METINF_MaxObjSize: Int = 0x15

    const val WBXML_DEVINF_CTCap: Int = 0x05
    const val WBXML_DEVINF_CTType: Int = 0x06
    const val WBXML_DEVINF_DataStore: Int = 0x07
    const val WBXML_DEVINF_DataType: Int = 0x08
    const val WBXML_DEVINF_DevID: Int = 0x09
    const val WBXML_DEVINF_DevInf: Int = 0x0a
    const val WBXML_DEVINF_DevTyp: Int = 0x0b
    const val WBXML_DEVINF_DisplayName: Int = 0x0c
    const val WBXML_DEVINF_DSMem: Int = 0x0d
    const val WBXML_DEVINF_Ext: Int = 0x0e
    const val WBXML_DEVINF_FwV: Int = 0x0f
    const val WBXML_DEVINF_HwV: Int = 0x10
    const val WBXML_DEVINF_Man: Int = 0x11
    const val WBXML_DEVINF_MaxGUIDSize: Int = 0x12
    const val WBXML_DEVINF_MaxID: Int = 0x13
    const val WBXML_DEVINF_MaxMem: Int = 0x14
    const val WBXML_DEVINF_Mod: Int = 0x15
    const val WBXML_DEVINF_OEM: Int = 0x16
    const val WBXML_DEVINF_ParamName: Int = 0x17
    const val WBXML_DEVINF_PropName: Int = 0x18
    const val WBXML_DEVINF_Rx: Int = 0x19
    const val WBXML_DEVINF_Rx_Pref: Int = 0x1a
    const val WBXML_DEVINF_SharedMem: Int = 0x1b
    const val WBXML_DEVINF_Size: Int = 0x1c
    const val WBXML_DEVINF_SourceRef: Int = 0x1d
    const val WBXML_DEVINF_SwV: Int = 0x1e
    const val WBXML_DEVINF_SyncCap: Int = 0x1f
    const val WBXML_DEVINF_SyncType: Int = 0x20
    const val WBXML_DEVINF_Tx: Int = 0x21
    const val WBXML_DEVINF_Tx_Pref: Int = 0x22
    const val WBXML_DEVINF_ValEnum: Int = 0x23
    const val WBXML_DEVINF_VerCT: Int = 0x24
    const val WBXML_DEVINF_VerDTD: Int = 0x25
    const val WBXML_DEVINF_XNam: Int = 0x26
    const val WBXML_DEVINF_XVal: Int = 0x27
    const val WBXML_DEVINF_UTC: Int = 0x28
    const val WBXML_DEVINF_SupportNumberOfChanges: Int = 0x29
    const val WBXML_DEVINF_SupportLargeObjs: Int = 0x2a
    const val WBXML_DEVINF_Property: Int = 0x2b
    const val WBXML_DEVINF_PropParam: Int = 0x2c
    const val WBXML_DEVINF_MaxOccur: Int = 0x2d
    const val WBXML_DEVINF_NoTruncate: Int = 0x2e
    const val WBXML_DEVINF_FilterRx: Int = 0x30
    const val WBXML_DEVINF_FilterCap: Int = 0x31
    const val WBXML_DEVINF_FilterKeyword: Int = 0x32
    const val WBXML_DEVINF_FieldLevel: Int = 0x33
    const val WBXML_DEVINF_SupportHierarchicalSync: Int = 0x34

    const val WBXML_CODEPAGE_SYNCML: Int = 0
    const val WBXML_CODEPAGE_METINF: Int = 1
}