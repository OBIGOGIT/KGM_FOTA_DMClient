package com.tsdm.db;

public interface tsDefineDB
{
	int TS_FS_CREATE 					= 1;
	int TS_FS_READ 						= 2;
	int TS_FS_WRITE 					= 3;
	int TS_FS_TRUNCATE 					= 4;
	int TS_FS_APPEND 					= 5;
	int TS_FS_RDWR 						= 6;

	int DM_FILE_CREATE 					= 0x01;
	int DM_FILE_READ 					= 0x02;
	int DM_FILE_WRITE 					= 0x04;
	int DM_FILE_TRUNCATE 				= 0x08;
	int DM_FILE_APPEND 					= 0x10;


	int TS_FS_OK 						= 0;
	int TS_FS_ERR_BAD_PARAM 			= 2;
	int TS_FS_ERR_FILE_NOT_FOUND 		= 3;
	int TS_ERR_NO_MEM_READY 			= 4;
	int TS_FS_FAIL 						= 5;

	int	E2P_SYNCML_DM_PROFILE_IDX				= 0;
	int	E2P_SYNCML_DM_INFO_IDX					= 50;
	int	E2P_SYNCML_DM_ACCNODE_IDX				= 100;
	int	E2P_SYNCML_DM_AGENT_IDX					= 110;
	int	E2P_SYNCML_DM_SIM_IDX					= 120;
	int	E2P_SYNCML_DM_RESYNC_IDX				= 130;
	int	E2P_SYNCML_FUMO_IDX						= 200;
	int	E2P_SYNCML_POSTPONE_IDX					= 250;

	int	E2P_SYNCML_DM_PROFILE_MAGIC				= E2P_SYNCML_DM_PROFILE_IDX;
	int	E2P_SYNCML_DM_PROXY_PROFILE_INDEX		= 1 + E2P_SYNCML_DM_PROFILE_IDX;
	int	E2P_SYNCML_DM_PROFILE_INDEX				= 2 + E2P_SYNCML_DM_PROFILE_IDX;
	// added.. profile index
	int	E2P_SYNCML_DM_PROFILENAME1				= 3 + E2P_SYNCML_DM_PROFILE_IDX;
	int	E2P_SYNCML_DM_PROFILENAME2				= 4 + E2P_SYNCML_DM_PROFILE_IDX;
	int	E2P_SYNCML_DM_PROFILENAME3				= 5 + E2P_SYNCML_DM_PROFILE_IDX;
	int	E2P_SYNCML_DM_PROFILENAME4				= 6 + E2P_SYNCML_DM_PROFILE_IDX;
	int	E2P_SYNCML_DM_PROFILENAME5				= 7 + E2P_SYNCML_DM_PROFILE_IDX;

	int	E2P_SYNCML_DM_PROFILE					= 8 + E2P_SYNCML_DM_PROFILE_IDX;
	int	E2P_SYNCML_DM_NETWORKCONNNAME			= 9 + E2P_SYNCML_DM_PROFILE_IDX;
	int	E2P_SYNCML_DM_SESSIONID					= 10 + E2P_SYNCML_DM_PROFILE_IDX;
	int	E2P_SYNCML_DM_DESTORY_NOTIFICATION		= 11 + E2P_SYNCML_DM_PROFILE_IDX;
	int	E2P_SYNCML_DM_PROFILE_INDEX_NOTI_EVT	= 12 + E2P_SYNCML_DM_PROFILE_IDX;

	int	E2P_SYNCML_DM_NOTI_SAVED_INFO			= 13 + E2P_SYNCML_DM_PROFILE_IDX;
	// 1.2
	int	E2P_SYNCML_DM_NOTI_NOTI_RESYNC_MODE		= 14 + E2P_SYNCML_DM_PROFILE_IDX;
	int	E2P_SYNCML_DM_UIC_RESULT_KEEP			= 15 + E2P_SYNCML_DM_PROFILE_IDX;
	int	E2P_SYNCML_DM_UIC_RESULT_KEEP_FLAG		= 16 + E2P_SYNCML_DM_PROFILE_IDX;
	int	E2P_SYNCML_DM_SKIP_DEV_DISCOVERY		= 17 + E2P_SYNCML_DM_PROFILE_IDX;
	int	E2P_SYNCML_DM_IMEI						= 18 + E2P_SYNCML_DM_PROFILE_IDX;
	int E2P_SYNCML_DM_WIFIONLY					= 19 + E2P_SYNCML_DM_PROFILE_IDX;
	int E2P_SYNCML_DM_AUTOUPDATE				= 20 + E2P_SYNCML_DM_PROFILE_IDX;
	int E2P_SYNCML_DM_PUSH_MESSAGE				= 21 + E2P_SYNCML_DM_PROFILE_IDX;
	int E2P_SYNCML_DM_AUTOUPDATETIME			= 22 + E2P_SYNCML_DM_PROFILE_IDX;
	int	E2P_SYNCML_DM_DDFPARSER_NODE_INDEX		= 23 + E2P_SYNCML_DM_PROFILE_IDX;
	int E2P_SYNCML_DM_DELTAFILE_SAVE_INDEX		= 24 + E2P_SYNCML_DM_PROFILE_IDX;
	int E2P_SYNCML_DM_NEXTUPDATETIME			= 25 + E2P_SYNCML_DM_PROFILE_IDX;
	int E2P_SYNCML_DM_AUTOUPDATECHECK			= 26 + E2P_SYNCML_DM_PROFILE_IDX;
	int E2P_SYNCML_DM_AGENT_MODE				= 27 + E2P_SYNCML_DM_PROFILE_IDX;
	int E2P_SYNCML_DM_CURR_CHECK_TIME			= 28 + E2P_SYNCML_DM_PROFILE_IDX;
	int	E2P_SYNCML_DM_PROFILE_MAX				= 29 + E2P_SYNCML_DM_PROFILE_IDX;
	

	int	E2P_SYNCML_DM_INFO						= E2P_SYNCML_DM_INFO_IDX;
	int	E2P_SYNCML_DM_MAGIC						= 1 + E2P_SYNCML_DM_INFO_IDX;
	int	E2P_SYNCML_DM_PROTOCOL					= 2 + E2P_SYNCML_DM_INFO_IDX;
	int	E2P_SYNCML_DM_OBEX						= 3 + E2P_SYNCML_DM_INFO_IDX;
	int	E2P_SYNCML_DM_USERNAME					= 4 + E2P_SYNCML_DM_INFO_IDX;
	int	E2P_SYNCML_DM_PASSWORD					= 5 + E2P_SYNCML_DM_INFO_IDX;
	int	E2P_SYNCML_DM_SERVERPORT				= 6 + E2P_SYNCML_DM_INFO_IDX;
	int	E2P_SYNCML_DM_SERVERIP					= 7 + E2P_SYNCML_DM_INFO_IDX;
	int	E2P_SYNCML_DM_PATH						= 8 + E2P_SYNCML_DM_INFO_IDX;
	int	E2P_SYNCML_DM_APPID						= 9 + E2P_SYNCML_DM_INFO_IDX;
	int	E2P_SYNCML_DM_AUTHLEVEL					= 10 + E2P_SYNCML_DM_INFO_IDX;
	int	E2P_SYNCML_DM_SERVERAUTHLEVEL			= 11 + E2P_SYNCML_DM_INFO_IDX;
	int	E2P_SYNCML_DM_PREFCONREF				= 12 + E2P_SYNCML_DM_INFO_IDX;
	int	E2P_SYNCML_DM_SERVERID					= 13 + E2P_SYNCML_DM_INFO_IDX;
	int	E2P_SYNCML_DM_SERVERPASSWORD			= 14 + E2P_SYNCML_DM_INFO_IDX;
	int	E2P_SYNCML_DM_CLIENT_NONCE				= 15 + E2P_SYNCML_DM_INFO_IDX;
	int	E2P_SYNCML_DM_SERVER_NONCE				= 16 + E2P_SYNCML_DM_INFO_IDX;
	int	E2P_SYNCML_DM_SERVERURL					= 17 + E2P_SYNCML_DM_INFO_IDX;
	int	E2P_SYNCML_DM_AUTHTYPE					= 18 + E2P_SYNCML_DM_INFO_IDX;
	int	E2P_SYNCML_DM_SERVER_AUTHTYPE			= 19 + E2P_SYNCML_DM_INFO_IDX;
	int	E2P_SYNCML_DM_NETWORKCONNINDEX			= 20 + E2P_SYNCML_DM_INFO_IDX;
	int	E2P_SYNCML_DM_INFO_PROFILENAME			= 21 + E2P_SYNCML_DM_INFO_IDX;
	int	E2P_SYNCML_DM_INFO_CONREF				= 22 + E2P_SYNCML_DM_INFO_IDX;
	int	E2P_SYNCML_DM_INFO_CON_BACKUP			= 23 + E2P_SYNCML_DM_INFO_IDX;
	int	E2P_SYNCML_DM_CHANGED_PROTOCOL			= 24 + E2P_SYNCML_DM_INFO_IDX;
	int	E2P_SYNCML_DM_INFO_MAX					= 25 + E2P_SYNCML_DM_INFO_IDX;

	// 1.2
	int	E2P_SYNCML_DM_ACCXNODE_INFO1			= E2P_SYNCML_DM_ACCNODE_IDX;
	int	E2P_SYNCML_DM_ACCXNODE_INFO2			= 1 + E2P_SYNCML_DM_ACCNODE_IDX;
	int	E2P_SYNCML_DM_ACCXNODE_INFO3			= 2 + E2P_SYNCML_DM_ACCNODE_IDX;
	int	E2P_SYNCML_DM_ACCXNODE_INFO4			= 3 + E2P_SYNCML_DM_ACCNODE_IDX;
	int	E2P_SYNCML_DM_ACCXNODE_INFO5			= 4 + E2P_SYNCML_DM_ACCNODE_IDX;
	int	E2P_SYNCML_DM_ACCNODE_MAX				= 5 + E2P_SYNCML_DM_ACCNODE_IDX;

	int	E2P_SYNCML_DM_RESYNC_MODE				= E2P_SYNCML_DM_RESYNC_IDX;
	int	E2P_SYNCML_DM_RESYNC_MAX				= 1 + E2P_SYNCML_DM_RESYNC_IDX;

	// for DmAgentInfo
	int	E2P_SYNCML_DM_AGENT_INFO				= E2P_SYNCML_DM_AGENT_IDX;
	int	E2P_SYNCML_DM_AGENT_INFO_AGENT_TYPE		= 1 + E2P_SYNCML_DM_AGENT_IDX;
	int	E2P_SYNCML_DM_AGENT_MAX					= 2 + E2P_SYNCML_DM_AGENT_IDX;

	// Sim Information
	int	E2P_SYNCML_SIM_IMSI						= E2P_SYNCML_DM_SIM_IDX;
	int	E2P_SYNCML_DM_SIM_MAX					= 1 + E2P_SYNCML_DM_SIM_IDX;

	int	E2P_SYNCML_FUMO_INFO					= E2P_SYNCML_FUMO_IDX;
	int	E2P_SYNCML_FUMO_PROTOCOL				= 1 + E2P_SYNCML_FUMO_IDX;
	int	E2P_SYNCML_FUMO_OBEX					= 2 + E2P_SYNCML_FUMO_IDX;
	int	E2P_SYNCML_FUMO_SERVERURL				= 3 + E2P_SYNCML_FUMO_IDX;
	int	E2P_SYNCML_FUMO_SERVERIP				= 4 + E2P_SYNCML_FUMO_IDX;
	int	E2P_SYNCML_FUMO_SERVERPORT				= 5 + E2P_SYNCML_FUMO_IDX;
	int	E2P_SYNCML_FUMO_OBJECTDOWNLOADPROTOCOL	= 6 + E2P_SYNCML_FUMO_IDX;
	int	E2P_SYNCML_FUMO_OBJECTDOWNLOADURL		= 7 + E2P_SYNCML_FUMO_IDX;
	int	E2P_SYNCML_FUMO_OBJECTDOWNLOADIP		= 8 + E2P_SYNCML_FUMO_IDX;
	int	E2P_SYNCML_FUMO_OBJECTDOWNLOADPORT		= 9 + E2P_SYNCML_FUMO_IDX;
	int	E2P_SYNCML_FUMO_STATUSNOTIFYPROTOCOL	= 10 + E2P_SYNCML_FUMO_IDX;
	int	E2P_SYNCML_FUMO_STATUSNOTIFYURL			= 11 + E2P_SYNCML_FUMO_IDX;
	int	E2P_SYNCML_FUMO_STATUSNOTIFYIP			= 12 + E2P_SYNCML_FUMO_IDX;
	int	E2P_SYNCML_FUMO_STATUSNOTIFYPORT		= 13 + E2P_SYNCML_FUMO_IDX;
	int	E2P_SYNCML_FUMO_OBJECTSIZE				= 14 + E2P_SYNCML_FUMO_IDX;
	int	E2P_SYNCML_FUMO_WRITE_OBJECTSIZE		= 15 + E2P_SYNCML_FUMO_IDX;
	int	E2P_SYNCML_FUMO_RESULT_CODE				= 16 + E2P_SYNCML_FUMO_IDX;
	int	E2P_SYNCML_FUMO_CORRELATOR				= 17 + E2P_SYNCML_FUMO_IDX;
	int	E2P_SYNCML_FUMO_STATUS_NODE				= 18 + E2P_SYNCML_FUMO_IDX;
	int	E2P_SYNCML_FUMO_STATUS					= 19 + E2P_SYNCML_FUMO_IDX;
	int	E2P_SYNCML_FUMO_UPDATE_MECHANISM		= 20 + E2P_SYNCML_FUMO_IDX;
	int	E2P_SYNCML_FUMO_DOWNLOAD_MODE			= 21 + E2P_SYNCML_FUMO_IDX;
	int	E2P_SYNCML_FUMO_REPORT_URI				= 22 + E2P_SYNCML_FUMO_IDX;
	int	E2P_SYNCML_FUMO_DD_CONTENTTYPE			= 23 + E2P_SYNCML_FUMO_IDX;
	int	E2P_SYNCML_FUMO_UPDATE_WAIT				= 24 + E2P_SYNCML_FUMO_IDX;
	int	E2P_SYNCML_FUMO_DD_ACCEPTTYPE			= 25 + E2P_SYNCML_FUMO_IDX;
	int	E2P_SYNCML_FUMO_MAX						= 26 + E2P_SYNCML_FUMO_IDX;

	int	E2P_SYNCML_FOTA_POSTPONE_COUNT			= E2P_SYNCML_POSTPONE_IDX;
	int	E2P_SYNCML_FOTA_POSTPONE_CURRENT_TIME	= 1 + E2P_SYNCML_POSTPONE_IDX;
	int	E2P_SYNCML_FOTA_POSTPONE_TIMER_END_TIME	= 2 + E2P_SYNCML_POSTPONE_IDX;
	int	E2P_SYNCML_FOTA_POSTPONE_DOWNLOADSTATE	= 3 + E2P_SYNCML_POSTPONE_IDX;
	int	E2P_SYNCML_FOTA_POSTPONE_POSTTIME		= 4 + E2P_SYNCML_POSTPONE_IDX;
	int	E2P_SYNCML_FOTA_POSTPONE_DOWNLOAD		= 5 + E2P_SYNCML_POSTPONE_IDX;
	int	E2P_SYNCML_FOTA_POSTPONE				= 6 + E2P_SYNCML_POSTPONE_IDX;
	int	E2P_SYNCML_POSTPONE_MAX					= 7 + E2P_SYNCML_POSTPONE_IDX;
}
