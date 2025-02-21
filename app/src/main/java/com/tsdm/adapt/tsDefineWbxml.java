package com.tsdm.adapt;

public interface tsDefineWbxml
{
	int DM_ERR_OK 									= 0;
	int DM_ERR_FAIL 								= 1;
	int DM_ERR_UNKNOWN_ELEMENT 						= 2;
	int DM_ERR_BUFFER_TOO_SMALL						= 3;
	int DM_ERR_INVALID_PARAMETER 					= 4;
	int DM_ERR_ZEROBIT_TAG = 5;
	
	
	int	WBXML_VERSION								= 0x02;
	int	WBXML_CHARSET_UTF8							= 0x6a;

	/* WBXML Global Tokens */
	int	WBXML_SWITCH_PAGE							= 0x00;
	int	WBXML_END									= 0x01;
	int	WBXML_ENTITY								= 0x02;
	int	WBXML_STR_I									= 0x03;
	int	WBXML_LITERAL								= 0x04;
	int	WBXML_EXT_I_0								= 0x40;
	int	WBXML_EXT_I_1								= 0x41;
	int	WBXML_EXT_I_2								= 0x42;
	int	WBXML_PI									= 0x43;
	int	WBXML_LITERAL_C								= 0x44;
	int	WBXML_EXT_T_0								= 0x80;
	int	WBXML_EXT_T_1								= 0x81;
	int	WBXML_EXT_T_2								= 0x82;
	int	WBXML_STR_T									= 0x83;
	int	WBXML_LITERAL_A								= 0x84;
	int	WBXML_EXT_0									= 0xC0;
	int	WBXML_EXT_1									= 0xC1;
	int	WBXML_EXT_2									= 0xC2;
	int	WBXML_OPAQUE								= 0xC3;
	int	WBXML_LITERAL_AC							= 0xC4;

	/* WBXML Tokens Masks */
	int	WBXML_TOKEN_MASK							= 0x3F;
	int	WBXML_TOKEN_WITH_ATTRS						= 0x80;
	int	WBXML_TOKEN_WITH_CONTENT					= 0x40;

	int	WBXML_PUBLICID_TOKEN_V10					= 0xFD1;
	int	WBXML_PUBLICID_TOKEN_V11					= 0xFD3;
	int	WBXML_PUBLICID_TOKEN_V12					= 0x1201;

	int WBXML_PAGE_SYNCML 							= 0x00;
	int WBXML_PAGE_METINF 							= 0x01;
	int	WBXML_PAGE_DEVINF							= 0x02;

	/* SyncML WBXML element id */
	int WBXML_TAG_Add 								= 0x05;
	int WBXML_TAG_Alert 							= 0x06;
	int WBXML_TAG_Archive 							= 0x07;
	int WBXML_TAG_Atomic 							= 0x08;
	int WBXML_TAG_Chal 								= 0x09;
	int WBXML_TAG_Cmd 								= 0x0a;
	int WBXML_TAG_CmdID 							= 0x0b;
	int WBXML_TAG_CmdRef 							= 0x0c;
	int WBXML_TAG_Copy 								= 0x0d;
	int WBXML_TAG_Cred 								= 0x0e;
	int WBXML_TAG_Data 								= 0x0f;
	int WBXML_TAG_Delete 							= 0x10;
	int WBXML_TAG_Exec 								= 0x11;
	int WBXML_TAG_Final 							= 0x12;
	int WBXML_TAG_Get 								= 0x13;
	int WBXML_TAG_Item 								= 0x14;
	int WBXML_TAG_Lang 								= 0x15;
	int WBXML_TAG_LocName 							= 0x16;
	int WBXML_TAG_LocURI 							= 0x17;
	int WBXML_TAG_Map 								= 0x18;
	int WBXML_TAG_MapItem 							= 0x19;
	int WBXML_TAG_Meta 								= 0x1a;
	int WBXML_TAG_MsgID 							= 0x1b;
	int WBXML_TAG_MsgRef 							= 0x1c;
	int WBXML_TAG_NoResp 							= 0x1d;
	int WBXML_TAG_NoResults 						= 0x1e;
	int WBXML_TAG_Put 								= 0x1f;
	int WBXML_TAG_Replace 							= 0x20;
	int WBXML_TAG_RespURI 							= 0x21;
	int WBXML_TAG_Results 							= 0x22;
	int	WBXML_TAG_Search							= 0x23;
	int WBXML_TAG_Sequence 							= 0x24;
	int WBXML_TAG_SessionID 						= 0x25;
	int WBXML_TAG_SftDel							= 0x26;
	int WBXML_TAG_Source 							= 0x27;
	int WBXML_TAG_SourceRef 						= 0x28;
	int WBXML_TAG_Status 							= 0x29;
	int WBXML_TAG_Sync 								= 0x2a;
	int WBXML_TAG_SyncBody 							= 0x2b;
	int WBXML_TAG_SyncHdr							 = 0x2c;
	int WBXML_TAG_SyncML							 = 0x2d;
	int WBXML_TAG_Target 							= 0x2e;
	int WBXML_TAG_TargetRef 						= 0x2f;
	int	WBXML_TAG_NULL								= 0x30;
	int WBXML_TAG_VerDTD 							= 0x31;
	int WBXML_TAG_VerProto 							= 0x32;
	int WBXML_TAG_NumberOfChanges 					= 0x33;
	int WBXML_TAG_MoreData 							= 0x34;
	int WBXML_TAG_Correlator 						= 0x3C;

	int WBXML_METINF_Anchor 						= 0x05;
	int WBXML_METINF_EMI 							= 0x06;
	int WBXML_METINF_Format 						= 0x07;
	int WBXML_METINF_FreeID 						= 0x08;
	int WBXML_METINF_FreeMem 						= 0x09;
	int WBXML_METINF_Last 							= 0x0a;
	int WBXML_METINF_Mark 							= 0x0b;
	int WBXML_METINF_MaxMsgSize 					= 0x0c;
	int WBXML_METINF_Mem 							= 0x0d;
	int	WBXML_METINF_MetInf							= 0x0e;
	int WBXML_METINF_Next 							= 0x0f;
	int WBXML_METINF_NextNonce 						= 0x10;
	int WBXML_METINF_SharedMem 						= 0x11;
	int WBXML_METINF_Size 							= 0x12;
	int WBXML_METINF_Type 							= 0x13;
	int WBXML_METINF_Version 						= 0x14;
	int WBXML_METINF_MaxObjSize 					= 0x15;

	int	WBXML_DEVINF_CTCap							= 0x05;
	int	WBXML_DEVINF_CTType							= 0x06;
	int	WBXML_DEVINF_DataStore						= 0x07;
	int	WBXML_DEVINF_DataType						= 0x08;
	int	WBXML_DEVINF_DevID							= 0x09;
	int WBXML_DEVINF_DevInf 						= 0x0a;
	int	WBXML_DEVINF_DevTyp							= 0x0b;
	int	WBXML_DEVINF_DisplayName					= 0x0c;
	int	WBXML_DEVINF_DSMem							= 0x0d;
	int	WBXML_DEVINF_Ext							= 0x0e;
	int	WBXML_DEVINF_FwV							= 0x0f;
	int	WBXML_DEVINF_HwV							= 0x10;
	int	WBXML_DEVINF_Man							= 0x11;
	int	WBXML_DEVINF_MaxGUIDSize					= 0x12;
	int	WBXML_DEVINF_MaxID							= 0x13;
	int	WBXML_DEVINF_MaxMem							= 0x14;
	int	WBXML_DEVINF_Mod							= 0x15;
	int	WBXML_DEVINF_OEM							= 0x16;
	int	WBXML_DEVINF_ParamName						= 0x17;
	int	WBXML_DEVINF_PropName						= 0x18;
	int	WBXML_DEVINF_Rx								= 0x19;
	int	WBXML_DEVINF_Rx_Pref						= 0x1a;
	int	WBXML_DEVINF_SharedMem						= 0x1b;
	int	WBXML_DEVINF_Size							= 0x1c;
	int	WBXML_DEVINF_SourceRef						= 0x1d;
	int	WBXML_DEVINF_SwV							= 0x1e;
	int	WBXML_DEVINF_SyncCap						= 0x1f;
	int	WBXML_DEVINF_SyncType						= 0x20;
	int	WBXML_DEVINF_Tx								= 0x21;
	int	WBXML_DEVINF_Tx_Pref						= 0x22;
	int	WBXML_DEVINF_ValEnum						= 0x23;
	int	WBXML_DEVINF_VerCT							= 0x24;
	int	WBXML_DEVINF_VerDTD							= 0x25;
	int	WBXML_DEVINF_XNam							= 0x26;
	int	WBXML_DEVINF_XVal							= 0x27;
	int	WBXML_DEVINF_UTC							= 0x28;
	int	WBXML_DEVINF_SupportNumberOfChanges			= 0x29;
	int	WBXML_DEVINF_SupportLargeObjs				= 0x2a;
	int	WBXML_DEVINF_Property						= 0x2b;
	int	WBXML_DEVINF_PropParam						= 0x2c;
	int	WBXML_DEVINF_MaxOccur						= 0x2d;
	int	WBXML_DEVINF_NoTruncate						= 0x2e;
	int	WBXML_DEVINF_FilterRx						= 0x30;
	int	WBXML_DEVINF_FilterCap						= 0x31;
	int	WBXML_DEVINF_FilterKeyword					= 0x32;
	int	WBXML_DEVINF_FieldLevel						= 0x33;
	int	WBXML_DEVINF_SupportHierarchicalSync		= 0x34;

	int WBXML_CODEPAGE_SYNCML 						= 0;
	int WBXML_CODEPAGE_METINF 						= 1;
}
