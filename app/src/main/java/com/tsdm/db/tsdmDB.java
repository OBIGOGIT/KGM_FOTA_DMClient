package com.tsdm.db;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;

import com.tsdm.adapt.tsMsgEvent;
import com.tsdm.agent.dmDefineDevInfo;
import com.tsdm.auth.Auth;
import com.tsdm.auth.base64;
import com.tsdm.agent.dmDevinfoAdapter;
import com.tsdm.agent.dmAgentInfo;
import com.tsdm.adapt.tsDefineIdle;
import com.tsdm.adapt.tsLib;
import com.tsdm.adapt.tsDefineUic;
import com.tsdm.agent.dmDefineUIEvent;
import com.tsdm.agent.dmProfileEntity;
import com.tsdm.agent.dmPreConfigEntity;

public class tsdmDB  extends tsDBFactoryBootstrap  implements Serializable, dmDefineDevInfo, tsDefineIdle, tsDefineUic, tsDefineDB, dmDefineUIEvent
{
	enum eSyncMLDMNVMParameter
	{
		NVM_DM_PROFILE(0),
		NVM_DM_INFO(1),
		NVM_FUMO_INFO(2),
		NVM_FUMO_POSTPONE(3),
		NVM_IMSI_INFO(4),
		NVM_DM_ACC_X_NODE(5),
		NVM_NOTI_RESYNC_MODE(6),
		NVM_SCOMO_INFO(7),
		NVM_LAWMO_INFO(8),
		NVM_MOBILE_TRACKING_INFO(9),
		NVM_DM_AGENT_INFO(10),
		NVM_SCOMO_MIDLET_INFO(11),
		NVM_MODIFY_INFO(12),
		NVM_NOTI_INFO(13),
		NVM_MAX(14);

		final private int	nIndex;

		eSyncMLDMNVMParameter(int nIndex)
		{
			this.nIndex = nIndex;
		}

		int Value()
		{
			return nIndex;
		}
	};

	enum eSyncMLDMFileParameter
	{
		FileObjectTreeInfo(0, 0x0100),
		FileObjectData(1, 0x0101),
		FileFirmwareData(2, 0x0102),
		FileLargeObjectData(3, 0x0103),
		FileBootstrapWbxml(4, 0x0104),
		FileTndsXmlData(5, 0x0105),
		FileScomoData(6, 0x0063),		// scomo data
		FileJADData(7, 0x0107),			// scomo download jad file
		FileScomoMidletInfo(8, 0x0108),	// Midlet Info store
		FileModifyInfo(9, 0x0109),
		FileMax(10, 0x0110);

		final private int	nIndex;
		final private int	nFileId;

		eSyncMLDMFileParameter(int nIndex, int nFileId)
		{
			this.nIndex = nIndex;
			this.nFileId = nFileId;
		}

		int Index()
		{
			return nIndex;
		}

		int FileId()
		{
			return nFileId;
		}
	};

	public static final int
			SyncDMNVMProfile			= 0x0051,
			SyncDMNVMInfo1				= 0x0052,
			SyncDMNVMInfo2				= 0x0053,
			SyncDMNVMInfo3				= 0x0054,
			SyncDMNVMInfo4				= 0x0055,
			SyncDMNVMInfo5				= 0x0056,
			SyncDMFUMOInfo1				= 0x0057,
			SyncMLPostPone				= 0x0058,
			SyncMLIMSIInfo				= 0x0059,
			SyncMLNVMResyncMode			= 0x0060,
			SyncMLNVMDmAgentInfo		= 0x0061,
			SyncMLNVMNotiInfo			= 0x0066,
			SyncMLlastAreaCode			= 0x0067,
			SyncMLNVMAccXNode			= 0x0083;
	

	public static final int					DMINFOMAGIC					= 2355;
	public static final int					DMPROFILEMAGIC				= 3783;

	public static final int 				DM_SETTING_PROFILE_NUM		 = 3;

	public static final String 				DM_FS_FFS_DIRECTORY 					= "data/data/com.tsdm";
	public static final String 				DM_FS_FFS_EXTERNEL_DIRECTORY_FOTA 		= "/sdcard";
	public static final String 				DM_FS_FFS_EXTERNEL_SD_DIRECTORY_FOTA 	= "/sdcard/external_sd";

	public static final String 				DM_FS_FFS_FILE_EXTENTION				 = ".cfg";
	public static final String 				DM_FS_FFFS_FOTA_SIZE_FILE 				= "data/data/com.tsdm/2355.cfg";

	public static tsdmInfo[]				ProfileInfoClass;
	public static tsdmNetworkProfileList NetProfileClass;
	public static tsdmInfoConRef ConRef;
	public static tsdmNvm DMNvmClass;
	public static tsdmDBadapter dbadapter					= null;

	private static final long				serialVersionUID			= 1L;
	private static String					REAL_DM_CONNECTION_NAME		= "DM Profile";

	private static final int 				DM_PROFILE_LIST = 0;
	private static final int 				DM_NET_PROFILE_LIST = 1;
	private static final int 				DM_PROFILE_LIST_VIEW = 2;
	private static final int 				DM_NET_PROFILE_LIST_VIEW = 3;
	private static final int 				DM_DB_READ_PROFILE_INFO = 4;
	private static final int 				TS_DMFOTA_MAX_PROFILE = 3;

	private static int						SyncMLDMNVMParamCount		= eSyncMLDMNVMParameter.NVM_MAX.Value();																// Nonce_resync_menu
	private final static int				FFS_OWNER_SYNCML			= 240;
	private static int						g_nFOTAFileId				= 0;

	private static tsdmFileParam SyncDMFileParam[];
	private static tsDBFileParam dmSyncMLFileParam[];

	private static int						nFumoStatus					= -1;
	private static int						nScomoStatus				= 0;

	private static tsDBFileParam[] dmSyncMLFileParmInit(tsDBFileParam[] dmSyncMLFileParam2, int MaxIndex)
	{
		int[] Areacodeindex = null;
		Areacodeindex = new int[MaxIndex];

		Areacodeindex[eSyncMLDMNVMParameter.NVM_DM_PROFILE.Value()] = SyncDMNVMProfile;
		Areacodeindex[eSyncMLDMNVMParameter.NVM_DM_INFO.Value()] = SyncDMNVMInfo1;
		Areacodeindex[eSyncMLDMNVMParameter.NVM_FUMO_INFO.Value()] = SyncDMFUMOInfo1;
		Areacodeindex[eSyncMLDMNVMParameter.NVM_FUMO_POSTPONE.Value()] = SyncMLPostPone;
		Areacodeindex[eSyncMLDMNVMParameter.NVM_IMSI_INFO.Value()] = SyncMLIMSIInfo;

		if (_SYNCML_TS_DM_VERSION_V12_)
		{
			Areacodeindex[eSyncMLDMNVMParameter.NVM_DM_ACC_X_NODE.Value()] = SyncMLNVMAccXNode;
			Areacodeindex[eSyncMLDMNVMParameter.NVM_NOTI_RESYNC_MODE.Value()] = SyncMLNVMResyncMode;
		}

		Areacodeindex[eSyncMLDMNVMParameter.NVM_DM_AGENT_INFO.Value()] = SyncMLNVMDmAgentInfo;

		Areacodeindex[eSyncMLDMNVMParameter.NVM_NOTI_INFO.Value()] = SyncMLNVMNotiInfo;

		for (int i = 0; i < MaxIndex; i++)
		{
			dmSyncMLFileParam2[i] = new tsDBFileParam();
			dmSyncMLFileParam2[i].AreaCode = Areacodeindex[i];
			dmSyncMLFileParam2[i].pExtFileID = 0;
			dmSyncMLFileParam2[i].pNVMUser = 0;
		}

		return dmSyncMLFileParam2;
	}

	public static boolean dmdbInit()
	{
		tsLib.debugPrint(DEBUG_DB, "");
		try
		{
			eSyncMLDMFileParameter Files[] = eSyncMLDMFileParameter.values();

			SyncDMFileParam = new tsdmFileParam[eSyncMLDMFileParameter.FileMax.Index()];

			for (eSyncMLDMFileParameter File : Files)
			{
				if (File.Index() == eSyncMLDMFileParameter.FileMax.Index())
					break;

				SyncDMFileParam[File.Index()] = new tsdmFileParam();
				SyncDMFileParam[File.Index()].FileID = File.FileId();
				SyncDMFileParam[File.Index()].hFile = 0;
				SyncDMFileParam[File.Index()].nSize = 0;
			}

			if (dmSyncMLFileParam == null)
			{
				dmSyncMLFileParam = new tsDBFileParam[SyncMLDMNVMParamCount];
				dmSyncMLFileParam = dmSyncMLFileParmInit(dmSyncMLFileParam, SyncMLDMNVMParamCount);
			}

			ProfileInfoClass = new tsdmInfo[TS_DMFOTA_MAX_PROFILE];
			NetProfileClass = new tsdmNetworkProfileList();
			DMNvmClass = new tsdmNvm();
			ConRef = new tsdmInfoConRef();
			dbadapter = new tsdmDBadapter();
		}
		catch (Exception ex)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, "Init Exception");
			return false;
		}

		return true;
	}

	public static int E2P_SYNCML_DM_PROFILENAME(int i)
	{
		int ret = 0;

		switch (i)
		{
			case 0:
				return E2P_SYNCML_DM_PROFILENAME1;
			case 1:
				return E2P_SYNCML_DM_PROFILENAME2;
			case 2:
				return E2P_SYNCML_DM_PROFILENAME3;
		}
		return ret;
	}

	public static int E2P_SYNCML_DM_ACCXNODE_INFO(int i)
	{
		int ret = 0;
		switch (i)
		{
			case 0:
				return E2P_SYNCML_DM_ACCXNODE_INFO1;
			case 1:
				return E2P_SYNCML_DM_ACCXNODE_INFO2;
			case 2:
				return E2P_SYNCML_DM_ACCXNODE_INFO3;
		}
		return ret;
	}

	public static Object dmdbRead(int nType)
	{
		Object outPut = null;

		if (dbadapter == null)
		{
			dbadapter = new tsdmDBadapter();
			dbadapter.dmdb = null;
		}

		switch (nType)
		{
			case DM_PROFILE_LIST:
				DMNvmClass.tProfileList = (tsdmProflieList) tsdmDBsql.dmsqlRead(dmSqlDbIdProfileList);
				return DMNvmClass.tProfileList;

			case DM_NET_PROFILE_LIST:
				dbadapter.dmdb = new tsDBfile(DM_NET_PROFILE_LIST);

				dmdbReadFile(DM_NET_PROFILE_LIST);

				return NetProfileClass;

			case DM_PROFILE_LIST_VIEW:
				for (int i = 0; i < TS_DMFOTA_MAX_PROFILE; i++)
				{
					ProfileInfoClass[i] = (tsdmInfo) tsdmDBsql.dmsqlRead(dmSqlDbIdProfileInfo1 + i);
				}

				return ProfileInfoClass;

			case DM_NET_PROFILE_LIST_VIEW:
				break;

			case DM_DB_READ_PROFILE_INFO:
				return DMNvmClass.NVMSyncMLDMInfo;
		}
		return outPut;
	}

	public static Object dmAgentInfoDbRead(int nType, Object Input)
	{
		switch (nType)
		{
			case E2P_SYNCML_DM_AGENT_INFO:
				return DMNvmClass.NVMSyncMLDmAgentInfo;

			case E2P_SYNCML_DM_AGENT_INFO_AGENT_TYPE:
				return DMNvmClass.NVMSyncMLDmAgentInfo.nAgentType;

			default:
				tsLib.debugPrintException(DEBUG_EXCEPTION, "Wrong Type");
				break;
		}
		return null;
	}

	public static void dmAgentInfoDbWrite(int nType, Object Input)
	{
		switch (nType)
		{
			case E2P_SYNCML_DM_AGENT_INFO:
				DMNvmClass.NVMSyncMLDmAgentInfo = (dmAgentInfo) Input;
				break;

			case E2P_SYNCML_DM_AGENT_INFO_AGENT_TYPE:
				DMNvmClass.NVMSyncMLDmAgentInfo.nAgentType = (Integer) (Input);
				break;

			default:
				tsLib.debugPrintException(DEBUG_EXCEPTION, "Wrong Type");
				break;
		}
	}

	public static Object dmdbRead(int nType, Object Input)
	{
		if (DMNvmClass == null)
			DMNvmClass = new tsdmNvm();

		if (nType >= E2P_SYNCML_DM_PROFILE_IDX && nType < E2P_SYNCML_DM_PROFILE_MAX)
		{
			DMNvmClass.tProfileList = (tsdmProflieList) tsdmDBsql.dmsqlRead(dmSqlDbIdProfileList);
			if (DMNvmClass.tProfileList == null)
				return null;

		}
		else if (nType == E2P_SYNCML_DM_INFO_CONREF)
		{
			DMNvmClass.NVMSyncMLDMInfo.ConRef = (tsdmInfoConRef) tsdmDBsql.dmsqlRead(dmSqlDbIdNetworkInfo);
			if (DMNvmClass.NVMSyncMLDMInfo.ConRef == null)
				return null;
		}
		else if (nType >= E2P_SYNCML_DM_INFO_IDX && nType < E2P_SYNCML_DM_INFO_MAX)
		{
			DMNvmClass.tProfileList = (tsdmProflieList) tsdmDBsql.dmsqlRead(dmSqlDbIdProfileList);
			if (DMNvmClass.tProfileList == null) // Defects
				return null;
			int rowid = DMNvmClass.tProfileList.Profileindex;
			int FileID = 0;
			switch (rowid)
			{
				case 0:
					FileID = dmSqlDbIdProfileInfo1;
					break;
				case 1:
					FileID = dmSqlDbIdProfileInfo2;
					break;
				case 2:
					FileID = dmSqlDbIdProfileInfo3;
					break;
			}
			DMNvmClass.NVMSyncMLDMInfo = (tsdmInfo) tsdmDBsql.dmsqlRead(FileID);

			if (DMNvmClass.NVMSyncMLDMInfo == null)
				return null;

		}
		else if (nType >= E2P_SYNCML_DM_ACCNODE_IDX && nType < E2P_SYNCML_DM_ACCNODE_MAX)
		{
			DMNvmClass.NVMSyncMLAccXNode = (tsDBAccXListNode) tsdmDBsql.dmsqlRead(dmSqlDbIdAccXNode);
			if (DMNvmClass.NVMSyncMLAccXNode == null)
				return null;

		}
		else if (nType >= E2P_SYNCML_DM_RESYNC_IDX && nType < E2P_SYNCML_DM_RESYNC_MAX)
		{
			DMNvmClass.NVMSyncMLResyncMode = (tsDBResyncMode) tsdmDBsql.dmsqlRead(SyncMLNVMResyncMode);
			if (DMNvmClass.NVMSyncMLResyncMode == null)
				return null;

		}
		else if (nType >= E2P_SYNCML_FUMO_IDX && nType < E2P_SYNCML_FUMO_MAX)
		{
			DMNvmClass.NVMSyncMLDMFUMOInfo = (tsDBFumoInfo) tsdmDBsql.dmsqlRead(dmSqlDbIdFUMOInfo);
			if (DMNvmClass.NVMSyncMLDMFUMOInfo == null)
				return null;
		}
		else if (nType >= E2P_SYNCML_POSTPONE_IDX && nType < E2P_SYNCML_POSTPONE_MAX)
		{
			DMNvmClass.NVMSyncMLPostPone = (tsDBPostPone) tsdmDBsql.dmsqlRead(dmSqlDbIdAPostPone);
			if (DMNvmClass.NVMSyncMLPostPone == null)
				return null;
		}
		else if (nType >= E2P_SYNCML_DM_SIM_IDX && nType < E2P_SYNCML_DM_SIM_MAX)
		{
			DMNvmClass.NVMSYNCMLSimInfo = (tsDBSimInfo) tsdmDBsql.dmsqlRead(dmSqlDbIdIMSIInfo);
			if (DMNvmClass.NVMSYNCMLSimInfo == null)
				return null;
		}
		else if (nType >= E2P_SYNCML_DM_AGENT_IDX && nType < E2P_SYNCML_DM_AGENT_MAX)
		{
			DMNvmClass.NVMSyncMLDmAgentInfo = (dmAgentInfo) tsdmDBsql.dmsqlRead(dmSqlDbIdDmAgentInfo);
			if (DMNvmClass.NVMSyncMLDmAgentInfo == null)
				return null;

			return dmAgentInfoDbRead(nType, Input);

		}
		else
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, "----wrong file id----");
			return null;
		}

		switch (nType)
		{
			case E2P_SYNCML_DM_PROFILE_MAGIC:
				return DMNvmClass.tProfileList.MagicNumber;
			case E2P_SYNCML_DM_PROXY_PROFILE_INDEX:
				return DMNvmClass.tProfileList.nProxyIndex;
			case E2P_SYNCML_DM_PROFILE_INDEX:
				return DMNvmClass.tProfileList.Profileindex;
			case E2P_SYNCML_DM_PROFILENAME1:
				return DMNvmClass.tProfileList.ProfileName[0];
			case E2P_SYNCML_DM_PROFILENAME2:
				return DMNvmClass.tProfileList.ProfileName[1];
			case E2P_SYNCML_DM_PROFILENAME3:
				return DMNvmClass.tProfileList.ProfileName[2];
			case E2P_SYNCML_DM_PROFILENAME4:
			case E2P_SYNCML_DM_PROFILENAME5:
				// Not Using Profile
				break;
			case E2P_SYNCML_DM_PROFILE:
				return DMNvmClass.tProfileList;
			case E2P_SYNCML_DM_NETWORKCONNNAME:
				return DMNvmClass.tProfileList.NetworkConnName;
			case E2P_SYNCML_DM_SESSIONID:
				return DMNvmClass.tProfileList.nSessionID;
			case E2P_SYNCML_DM_DESTORY_NOTIFICATION:
				return DMNvmClass.tProfileList.nDestoryNotiTime;
			case E2P_SYNCML_DM_PROFILE_INDEX_NOTI_EVT:
				return DMNvmClass.tProfileList.nNotiEvent;
			case E2P_SYNCML_DM_NOTI_SAVED_INFO:
				return DMNvmClass.tProfileList.NotiResumeState;
			case E2P_SYNCML_DM_NOTI_NOTI_RESYNC_MODE:
				if (_SYNCML_TS_DM_VERSION_V12_)
					return DMNvmClass.tProfileList.nNotiReSyncMode;
			case E2P_SYNCML_DM_UIC_RESULT_KEEP:
				return DMNvmClass.tProfileList.tUicResultKeep;
			case E2P_SYNCML_DM_UIC_RESULT_KEEP_FLAG:
				return DMNvmClass.tProfileList.tUicResultKeep.eStatus;
			case E2P_SYNCML_DM_SKIP_DEV_DISCOVERY:
				return DMNvmClass.tProfileList.bSkipDevDiscovery;
			case E2P_SYNCML_DM_IMEI:
				return DMNvmClass.tProfileList.szImei;
			case E2P_SYNCML_DM_WIFIONLY:
				return DMNvmClass.tProfileList.bWifiOnly;
			case E2P_SYNCML_DM_AUTOUPDATE:
				return DMNvmClass.tProfileList.bAutoUpdate;
			case E2P_SYNCML_DM_PUSH_MESSAGE:
				return DMNvmClass.tProfileList.bPushMessage;
			case E2P_SYNCML_DM_AUTOUPDATETIME:
				return DMNvmClass.tProfileList.nAutoUpdateTime;
			case E2P_SYNCML_DM_DDFPARSER_NODE_INDEX:
				return DMNvmClass.tProfileList.nDDFParserNodeIndex;
			case E2P_SYNCML_DM_DELTAFILE_SAVE_INDEX:
				return DMNvmClass.tProfileList.nSaveDeltaFileIndex;
			case E2P_SYNCML_DM_INFO:
				return DMNvmClass.NVMSyncMLDMInfo;
			case E2P_SYNCML_DM_MAGIC:
				return (DMNvmClass.NVMSyncMLDMInfo.MagicNumber);
			case E2P_SYNCML_DM_PROTOCOL:
				return DMNvmClass.NVMSyncMLDMInfo.Protocol;
			case E2P_SYNCML_DM_OBEX:
				return (DMNvmClass.NVMSyncMLDMInfo.ObexType);
			case E2P_SYNCML_DM_USERNAME:
				return DMNvmClass.NVMSyncMLDMInfo.UserName;
			case E2P_SYNCML_DM_PASSWORD:
				return DMNvmClass.NVMSyncMLDMInfo.Password;
			case E2P_SYNCML_DM_SERVERPORT:
				return DMNvmClass.NVMSyncMLDMInfo.ServerPort;
			case E2P_SYNCML_DM_SERVERIP:
				return DMNvmClass.NVMSyncMLDMInfo.ServerIP;
			case E2P_SYNCML_DM_PATH:
				return DMNvmClass.NVMSyncMLDMInfo.Path;
			case E2P_SYNCML_DM_APPID:
				if (_SYNCML_TS_DM_VERSION_V12_)
					return DMNvmClass.NVMSyncMLDMInfo.AppID;
			case E2P_SYNCML_DM_AUTHLEVEL:
				if (_SYNCML_TS_DM_VERSION_V12_)
					return DMNvmClass.NVMSyncMLDMInfo.AuthLevel;
			case E2P_SYNCML_DM_SERVERAUTHLEVEL:
				if (_SYNCML_TS_DM_VERSION_V12_)
					return DMNvmClass.NVMSyncMLDMInfo.ServerAuthLevel;
			case E2P_SYNCML_DM_PREFCONREF:
				if (_SYNCML_TS_DM_VERSION_V12_)
					return DMNvmClass.NVMSyncMLDMInfo.PrefConRef;
			case E2P_SYNCML_DM_SERVERID:
				return DMNvmClass.NVMSyncMLDMInfo.ServerID;
			case E2P_SYNCML_DM_SERVERPASSWORD:
				return DMNvmClass.NVMSyncMLDMInfo.ServerPwd;
			case E2P_SYNCML_DM_CLIENT_NONCE:
				return DMNvmClass.NVMSyncMLDMInfo.ClientNonce;
			case E2P_SYNCML_DM_SERVER_NONCE:
				return DMNvmClass.NVMSyncMLDMInfo.ServerNonce;
			case E2P_SYNCML_DM_SERVERURL:
				return DMNvmClass.NVMSyncMLDMInfo.ServerUrl;
			case E2P_SYNCML_DM_AUTHTYPE:
				return (DMNvmClass.NVMSyncMLDMInfo.AuthType);
			case E2P_SYNCML_DM_SERVER_AUTHTYPE:
				return (DMNvmClass.NVMSyncMLDMInfo.nServerAuthType);
			case E2P_SYNCML_DM_NETWORKCONNINDEX:
				return (DMNvmClass.NVMSyncMLDMInfo.nNetworkConnIndex);
			case E2P_SYNCML_DM_INFO_PROFILENAME:
				return DMNvmClass.NVMSyncMLDMInfo.ProfileName;
			case E2P_SYNCML_DM_INFO_CONREF:
				return DMNvmClass.NVMSyncMLDMInfo.ConRef;
			case E2P_SYNCML_DM_INFO_CON_BACKUP:
				return DMNvmClass.NVMSyncMLDMInfo.ConBackup;
			case E2P_SYNCML_DM_CHANGED_PROTOCOL:
				return (DMNvmClass.NVMSyncMLDMInfo.bChangedProtocol);
			case E2P_SYNCML_DM_ACCXNODE_INFO1:
				if (_SYNCML_TS_DM_VERSION_V12_)
					return DMNvmClass.NVMSyncMLAccXNode.stAccXNodeList[0];
			case E2P_SYNCML_DM_ACCXNODE_INFO2:
				if (_SYNCML_TS_DM_VERSION_V12_)
					return DMNvmClass.NVMSyncMLAccXNode.stAccXNodeList[1];
			case E2P_SYNCML_DM_ACCXNODE_INFO3:
				if (_SYNCML_TS_DM_VERSION_V12_)
					return DMNvmClass.NVMSyncMLAccXNode.stAccXNodeList[2];
			case E2P_SYNCML_DM_ACCXNODE_INFO4:
			case E2P_SYNCML_DM_ACCXNODE_INFO5:
				break;
			case E2P_SYNCML_DM_RESYNC_MODE:
				if (_SYNCML_TS_DM_VERSION_V12_)
					return DMNvmClass.NVMSyncMLResyncMode.nNoceResyncMode;
			case E2P_SYNCML_FUMO_INFO:
				return DMNvmClass.NVMSyncMLDMFUMOInfo;
			case E2P_SYNCML_FUMO_PROTOCOL:
				return DMNvmClass.NVMSyncMLDMFUMOInfo.Protocol;
			case E2P_SYNCML_FUMO_OBEX:
				return (DMNvmClass.NVMSyncMLDMFUMOInfo.ObexType);
			case E2P_SYNCML_FUMO_SERVERURL:
				return DMNvmClass.NVMSyncMLDMFUMOInfo.ServerUrl;
			case E2P_SYNCML_FUMO_SERVERIP:
				return DMNvmClass.NVMSyncMLDMFUMOInfo.ServerIP;
			case E2P_SYNCML_FUMO_SERVERPORT:
				return (DMNvmClass.NVMSyncMLDMFUMOInfo.ServerPort);
			case E2P_SYNCML_FUMO_OBJECTDOWNLOADPROTOCOL:
				return DMNvmClass.NVMSyncMLDMFUMOInfo.ObjectDownloadProtocol;
			case E2P_SYNCML_FUMO_OBJECTDOWNLOADURL:
				return DMNvmClass.NVMSyncMLDMFUMOInfo.ObjectDownloadUrl;
			case E2P_SYNCML_FUMO_OBJECTDOWNLOADIP:
				return DMNvmClass.NVMSyncMLDMFUMOInfo.ObjectDownloadIP;
			case E2P_SYNCML_FUMO_OBJECTDOWNLOADPORT:
				return (DMNvmClass.NVMSyncMLDMFUMOInfo.nObjectDownloadPort);
			case E2P_SYNCML_FUMO_STATUSNOTIFYPROTOCOL:
				return DMNvmClass.NVMSyncMLDMFUMOInfo.StatusNotifyProtocol;
			case E2P_SYNCML_FUMO_STATUSNOTIFYURL:
				return DMNvmClass.NVMSyncMLDMFUMOInfo.StatusNotifyUrl;
			case E2P_SYNCML_FUMO_STATUSNOTIFYIP:
				return DMNvmClass.NVMSyncMLDMFUMOInfo.StatusNotifyIP;
			case E2P_SYNCML_FUMO_STATUSNOTIFYPORT:
				return (DMNvmClass.NVMSyncMLDMFUMOInfo.nStatusNotifyPort);
			case E2P_SYNCML_FUMO_OBJECTSIZE:
				return DMNvmClass.NVMSyncMLDMFUMOInfo.nObjectSize;
			case E2P_SYNCML_FUMO_WRITE_OBJECTSIZE:
				return DMNvmClass.NVMSyncMLDMFUMOInfo.nFFSWriteSize;
			case E2P_SYNCML_FUMO_RESULT_CODE:
				return DMNvmClass.NVMSyncMLDMFUMOInfo.ResultCode;
			case E2P_SYNCML_FUMO_CORRELATOR:
				return DMNvmClass.NVMSyncMLDMFUMOInfo.Correlator;
			case E2P_SYNCML_FUMO_STATUS_NODE:
				return DMNvmClass.NVMSyncMLDMFUMOInfo.StatusNodeName;
			case E2P_SYNCML_FUMO_STATUS:
				return (DMNvmClass.NVMSyncMLDMFUMOInfo.nStatus);
			case E2P_SYNCML_FUMO_UPDATE_MECHANISM:
				return DMNvmClass.NVMSyncMLDMFUMOInfo.nUpdateMechanism;
			case E2P_SYNCML_FUMO_DOWNLOAD_MODE:
				return (DMNvmClass.NVMSyncMLDMFUMOInfo.nDownloadMode);
			case E2P_SYNCML_FUMO_UPDATE_WAIT:
				return (DMNvmClass.NVMSyncMLDMFUMOInfo.bUpdateWait);
			case E2P_SYNCML_FUMO_REPORT_URI:
				return DMNvmClass.NVMSyncMLDMFUMOInfo.ReportURI;
			case E2P_SYNCML_FUMO_DD_CONTENTTYPE:
				return DMNvmClass.NVMSyncMLDMFUMOInfo.szContentType;
			case E2P_SYNCML_FUMO_DD_ACCEPTTYPE:
				return DMNvmClass.NVMSyncMLDMFUMOInfo.szAcceptType;
			case E2P_SYNCML_FOTA_POSTPONE_COUNT:
				return (DMNvmClass.NVMSyncMLPostPone.nPostPoneCount);
			case E2P_SYNCML_FOTA_POSTPONE_DOWNLOAD:
				return (DMNvmClass.NVMSyncMLPostPone.bPostPoneDownload);
			case E2P_SYNCML_FOTA_POSTPONE_CURRENT_TIME:
				return (DMNvmClass.NVMSyncMLPostPone.tCurrentTime);
			case E2P_SYNCML_FOTA_POSTPONE_TIMER_END_TIME:
				return (DMNvmClass.NVMSyncMLPostPone.tEndTime);
			case E2P_SYNCML_FOTA_POSTPONE_DOWNLOADSTATE:
				return (DMNvmClass.NVMSyncMLPostPone.nAfterDownLoadBatteryStatus);
			case E2P_SYNCML_FOTA_POSTPONE_POSTTIME:
				return (DMNvmClass.NVMSyncMLPostPone.nPostPoneTime);
			case E2P_SYNCML_FOTA_POSTPONE:
				return (DMNvmClass.NVMSyncMLPostPone);
			case E2P_SYNCML_SIM_IMSI:
				return DMNvmClass.NVMSYNCMLSimInfo.pIMSI;
			case E2P_SYNCML_DM_NEXTUPDATETIME:
				return DMNvmClass.tProfileList.lNextUpdateTime;
			case E2P_SYNCML_DM_AUTOUPDATECHECK:
				return DMNvmClass.tProfileList.bAutoCheck;
			case E2P_SYNCML_DM_AGENT_MODE:
				return DMNvmClass.tProfileList.bAgentMode;
			case E2P_SYNCML_DM_CURR_CHECK_TIME:
				return DMNvmClass.tProfileList.lcurrCheckTime;
		}
		return null;
	}

	public static boolean dmdbWrite(int nType, Object oInput)
	{
		boolean bret = true;

		if (oInput == null)
		{
			tsLib.debugPrint(DEBUG_DB, "oInput is null");
			return false;
		}

		switch (nType)
		{
			case E2P_SYNCML_DM_PROFILE_MAGIC:
				DMNvmClass.tProfileList.MagicNumber = Integer.valueOf(oInput.toString());
				break;
			case E2P_SYNCML_DM_PROXY_PROFILE_INDEX:
				DMNvmClass.tProfileList.nProxyIndex = Integer.valueOf(oInput.toString());
				break;
			case E2P_SYNCML_DM_PROFILE_INDEX:
				DMNvmClass.tProfileList.Profileindex = Integer.valueOf(oInput.toString());
				break;
			case E2P_SYNCML_DM_PROFILENAME1:
				DMNvmClass.tProfileList.ProfileName[0] = oInput.toString();
				break;
			case E2P_SYNCML_DM_PROFILENAME2:
				DMNvmClass.tProfileList.ProfileName[1] = oInput.toString();
				break;
			case E2P_SYNCML_DM_PROFILENAME3:
				DMNvmClass.tProfileList.ProfileName[2] = oInput.toString();
				break;
			case E2P_SYNCML_DM_PROFILENAME4:
			case E2P_SYNCML_DM_PROFILENAME5:
				// Not Using Profile
				break;
			case E2P_SYNCML_DM_PROFILE:
				DMNvmClass.tProfileList = (tsdmProflieList) oInput;
				break;
			case E2P_SYNCML_DM_NETWORKCONNNAME:
				DMNvmClass.tProfileList.NetworkConnName = oInput.toString();
				break;
			case E2P_SYNCML_DM_SESSIONID:
				DMNvmClass.tProfileList.nSessionID = oInput.toString();
				break;
			case E2P_SYNCML_DM_DESTORY_NOTIFICATION:
				DMNvmClass.tProfileList.nDestoryNotiTime = Long.valueOf(oInput.toString());
				break;
			case E2P_SYNCML_DM_PROFILE_INDEX_NOTI_EVT:
				DMNvmClass.tProfileList.nNotiEvent = Integer.valueOf(oInput.toString());
				break;
			case E2P_SYNCML_DM_NOTI_SAVED_INFO:
				DMNvmClass.tProfileList.NotiResumeState = (tsDBSessionSaveInfo) oInput;
				break;
			// 1.2
			case E2P_SYNCML_DM_NOTI_NOTI_RESYNC_MODE:
				DMNvmClass.tProfileList.nNotiReSyncMode = Integer.valueOf(oInput.toString());
				break;
			case E2P_SYNCML_DM_UIC_RESULT_KEEP:
				DMNvmClass.tProfileList.tUicResultKeep = (tsDBUICResultKeep) oInput;
				break;
			case E2P_SYNCML_DM_UIC_RESULT_KEEP_FLAG:
				DMNvmClass.tProfileList.tUicResultKeep.eStatus = Integer.valueOf(oInput.toString());
				break;
			case E2P_SYNCML_DM_SKIP_DEV_DISCOVERY:
				DMNvmClass.tProfileList.bSkipDevDiscovery = Boolean.valueOf(oInput.toString());
				break;				
			case E2P_SYNCML_DM_IMEI:
				DMNvmClass.tProfileList.szImei = oInput.toString();
				break;
			case E2P_SYNCML_DM_WIFIONLY:
				DMNvmClass.tProfileList.bWifiOnly = Boolean.valueOf(oInput.toString());
				break;
			case E2P_SYNCML_DM_AUTOUPDATE:
				DMNvmClass.tProfileList.bAutoUpdate = Boolean.valueOf(oInput.toString());
				break;
			case E2P_SYNCML_DM_PUSH_MESSAGE:
				DMNvmClass.tProfileList.bPushMessage = Boolean.valueOf(oInput.toString());
				break;
			case E2P_SYNCML_DM_AUTOUPDATETIME:
				DMNvmClass.tProfileList.nAutoUpdateTime = Long.valueOf(oInput.toString());
				break;
			case E2P_SYNCML_DM_DDFPARSER_NODE_INDEX:
				DMNvmClass.tProfileList.nDDFParserNodeIndex = Integer.valueOf(oInput.toString());
				break;
			case E2P_SYNCML_DM_DELTAFILE_SAVE_INDEX:
				DMNvmClass.tProfileList.nSaveDeltaFileIndex = Integer.valueOf(oInput.toString());
				break;
			case E2P_SYNCML_DM_INFO:
				DMNvmClass.NVMSyncMLDMInfo = (tsdmInfo) oInput;
				break;
			case E2P_SYNCML_DM_MAGIC:
				DMNvmClass.NVMSyncMLDMInfo.MagicNumber = Integer.valueOf(oInput.toString());
				break;
			case E2P_SYNCML_DM_PROTOCOL:
				DMNvmClass.NVMSyncMLDMInfo.Protocol = oInput.toString();
				break;
			case E2P_SYNCML_DM_OBEX:
				DMNvmClass.NVMSyncMLDMInfo.ObexType = Integer.valueOf(oInput.toString());
				break;
			case E2P_SYNCML_DM_USERNAME:
				DMNvmClass.NVMSyncMLDMInfo.UserName = oInput.toString();
				break;
			case E2P_SYNCML_DM_PASSWORD:
				DMNvmClass.NVMSyncMLDMInfo.Password = oInput.toString();
				break;
			case E2P_SYNCML_DM_SERVERPORT:
				DMNvmClass.NVMSyncMLDMInfo.ServerPort = Integer.valueOf(oInput.toString());
				break;
			case E2P_SYNCML_DM_SERVERIP:
				DMNvmClass.NVMSyncMLDMInfo.ServerIP = oInput.toString();
				break;
			case E2P_SYNCML_DM_PATH:
				DMNvmClass.NVMSyncMLDMInfo.Path = oInput.toString();
				break;
			// 1.2
			case E2P_SYNCML_DM_APPID:
				DMNvmClass.NVMSyncMLDMInfo.AppID = oInput.toString();
				break;
			case E2P_SYNCML_DM_AUTHLEVEL:
				DMNvmClass.NVMSyncMLDMInfo.AuthLevel = oInput.toString();
				break;
			case E2P_SYNCML_DM_SERVERAUTHLEVEL:
				DMNvmClass.NVMSyncMLDMInfo.ServerAuthLevel = oInput.toString();
				break;
			case E2P_SYNCML_DM_PREFCONREF:
				DMNvmClass.NVMSyncMLDMInfo.PrefConRef = oInput.toString();
				break;
			case E2P_SYNCML_DM_SERVERID:
				DMNvmClass.NVMSyncMLDMInfo.ServerID = oInput.toString();
				break;
			case E2P_SYNCML_DM_SERVERPASSWORD:
				DMNvmClass.NVMSyncMLDMInfo.ServerPwd = oInput.toString();
				break;
			case E2P_SYNCML_DM_CLIENT_NONCE:
				DMNvmClass.NVMSyncMLDMInfo.ClientNonce = oInput.toString();
				break;
			case E2P_SYNCML_DM_SERVER_NONCE:
				DMNvmClass.NVMSyncMLDMInfo.ServerNonce = oInput.toString();
				break;
			case E2P_SYNCML_DM_SERVERURL:
				DMNvmClass.NVMSyncMLDMInfo.ServerUrl = oInput.toString();
				break;
			case E2P_SYNCML_DM_AUTHTYPE:
				DMNvmClass.NVMSyncMLDMInfo.AuthType = Integer.valueOf(oInput.toString());
				break;
			case E2P_SYNCML_DM_SERVER_AUTHTYPE:
				DMNvmClass.NVMSyncMLDMInfo.nServerAuthType = Integer.valueOf(oInput.toString());
				break;
			case E2P_SYNCML_DM_NETWORKCONNINDEX:
				DMNvmClass.NVMSyncMLDMInfo.nNetworkConnIndex = Integer.valueOf(oInput.toString());
				break;
			case E2P_SYNCML_DM_INFO_PROFILENAME:
				DMNvmClass.NVMSyncMLDMInfo.ProfileName = oInput.toString();
				break;
			case E2P_SYNCML_DM_INFO_CONREF:
				DMNvmClass.NVMSyncMLDMInfo.ConRef = (tsdmInfoConRef) oInput;
				break;
			case E2P_SYNCML_DM_INFO_CON_BACKUP:
				DMNvmClass.NVMSyncMLDMInfo.ConBackup = (tsDBNetConProfileBackup) oInput;
				break;
			case E2P_SYNCML_DM_CHANGED_PROTOCOL:
				DMNvmClass.NVMSyncMLDMInfo.bChangedProtocol = Boolean.valueOf(oInput.toString());
				break;
			// 1.2
			case E2P_SYNCML_DM_ACCXNODE_INFO1:
				if (_SYNCML_TS_DM_VERSION_V12_)
					DMNvmClass.NVMSyncMLAccXNode.stAccXNodeList[0] = (tsDBAccXNode) oInput;
				break;
			case E2P_SYNCML_DM_ACCXNODE_INFO2:
				if (_SYNCML_TS_DM_VERSION_V12_)
					DMNvmClass.NVMSyncMLAccXNode.stAccXNodeList[1] = (tsDBAccXNode) oInput;
				break;
			case E2P_SYNCML_DM_ACCXNODE_INFO3:
				if (_SYNCML_TS_DM_VERSION_V12_)
					DMNvmClass.NVMSyncMLAccXNode.stAccXNodeList[2] = (tsDBAccXNode) oInput;
				break;
			case E2P_SYNCML_DM_ACCXNODE_INFO4:
			case E2P_SYNCML_DM_ACCXNODE_INFO5:
				break;
			case E2P_SYNCML_DM_RESYNC_MODE:
				if (_SYNCML_TS_DM_VERSION_V12_)
					DMNvmClass.NVMSyncMLResyncMode.nNoceResyncMode = Boolean.valueOf(oInput.toString());
				break;

			case E2P_SYNCML_FUMO_INFO:
				DMNvmClass.NVMSyncMLDMFUMOInfo = (tsDBFumoInfo) oInput;
				break;
			case E2P_SYNCML_FUMO_PROTOCOL:
				DMNvmClass.NVMSyncMLDMFUMOInfo.Protocol = oInput.toString();
				break;
			case E2P_SYNCML_FUMO_OBEX:
				DMNvmClass.NVMSyncMLDMFUMOInfo.ObexType = Integer.valueOf(oInput.toString());
				break;
			case E2P_SYNCML_FUMO_SERVERURL:
				DMNvmClass.NVMSyncMLDMFUMOInfo.ServerUrl = oInput.toString();
				break;
			case E2P_SYNCML_FUMO_SERVERIP:
				DMNvmClass.NVMSyncMLDMFUMOInfo.ServerIP = oInput.toString();
				break;
			case E2P_SYNCML_FUMO_SERVERPORT:
				DMNvmClass.NVMSyncMLDMFUMOInfo.ServerPort = Integer.valueOf(oInput.toString());
				break;
			case E2P_SYNCML_FUMO_OBJECTDOWNLOADPROTOCOL:
				DMNvmClass.NVMSyncMLDMFUMOInfo.ObjectDownloadProtocol = oInput.toString();
				break;
			case E2P_SYNCML_FUMO_OBJECTDOWNLOADURL:
				DMNvmClass.NVMSyncMLDMFUMOInfo.ObjectDownloadUrl = oInput.toString();
				break;
			case E2P_SYNCML_FUMO_OBJECTDOWNLOADIP:
				DMNvmClass.NVMSyncMLDMFUMOInfo.ObjectDownloadIP = oInput.toString();
				break;
			case E2P_SYNCML_FUMO_OBJECTDOWNLOADPORT:
				DMNvmClass.NVMSyncMLDMFUMOInfo.nObjectDownloadPort = Integer.valueOf(oInput.toString());
				break;
			case E2P_SYNCML_FUMO_STATUSNOTIFYPROTOCOL:
				DMNvmClass.NVMSyncMLDMFUMOInfo.StatusNotifyProtocol = oInput.toString();
				break;
			case E2P_SYNCML_FUMO_STATUSNOTIFYURL:
				DMNvmClass.NVMSyncMLDMFUMOInfo.StatusNotifyUrl = oInput.toString();
				break;
			case E2P_SYNCML_FUMO_STATUSNOTIFYIP:
				DMNvmClass.NVMSyncMLDMFUMOInfo.StatusNotifyIP = oInput.toString();
				break;
			case E2P_SYNCML_FUMO_STATUSNOTIFYPORT:
				DMNvmClass.NVMSyncMLDMFUMOInfo.nStatusNotifyPort = Integer.valueOf(oInput.toString());
				break;
			case E2P_SYNCML_FUMO_OBJECTSIZE:
				DMNvmClass.NVMSyncMLDMFUMOInfo.nObjectSize = Integer.valueOf(oInput.toString());
				break;
			case E2P_SYNCML_FUMO_WRITE_OBJECTSIZE:
				DMNvmClass.NVMSyncMLDMFUMOInfo.nFFSWriteSize = Integer.valueOf(oInput.toString());
				break;
			case E2P_SYNCML_FUMO_RESULT_CODE:
				DMNvmClass.NVMSyncMLDMFUMOInfo.ResultCode = oInput.toString();
				break;
			case E2P_SYNCML_FUMO_CORRELATOR:
				DMNvmClass.NVMSyncMLDMFUMOInfo.Correlator = oInput.toString();
				break;
			case E2P_SYNCML_FUMO_STATUS_NODE:
				DMNvmClass.NVMSyncMLDMFUMOInfo.StatusNodeName = oInput.toString();
				break;
			case E2P_SYNCML_FUMO_STATUS:
				DMNvmClass.NVMSyncMLDMFUMOInfo.nStatus = Integer.valueOf(oInput.toString());
				break;
			case E2P_SYNCML_FUMO_UPDATE_MECHANISM:
				DMNvmClass.NVMSyncMLDMFUMOInfo.nUpdateMechanism = Integer.valueOf(oInput.toString());
				break;
			case E2P_SYNCML_FUMO_DOWNLOAD_MODE:
				DMNvmClass.NVMSyncMLDMFUMOInfo.nDownloadMode = Boolean.valueOf(oInput.toString());
				break;
			case E2P_SYNCML_FUMO_UPDATE_WAIT:
				DMNvmClass.NVMSyncMLDMFUMOInfo.bUpdateWait = Boolean.valueOf(oInput.toString());
				break;
			case E2P_SYNCML_FUMO_REPORT_URI:
				DMNvmClass.NVMSyncMLDMFUMOInfo.ReportURI = oInput.toString();
				break;
			case E2P_SYNCML_FUMO_DD_CONTENTTYPE:
				DMNvmClass.NVMSyncMLDMFUMOInfo.szContentType = oInput.toString();
				break;
			case E2P_SYNCML_FUMO_DD_ACCEPTTYPE:
				DMNvmClass.NVMSyncMLDMFUMOInfo.szAcceptType = oInput.toString();
				break;
			case E2P_SYNCML_FOTA_POSTPONE_COUNT:
				DMNvmClass.NVMSyncMLPostPone.nPostPoneCount = Integer.valueOf(oInput.toString());
				break;
			case E2P_SYNCML_FOTA_POSTPONE_DOWNLOAD:
				DMNvmClass.NVMSyncMLPostPone.bPostPoneDownload = Boolean.valueOf(oInput.toString());
				break;
			case E2P_SYNCML_FOTA_POSTPONE_CURRENT_TIME:
				DMNvmClass.NVMSyncMLPostPone.tCurrentTime = Long.valueOf(oInput.toString());
				break;
			case E2P_SYNCML_FOTA_POSTPONE_TIMER_END_TIME:
				DMNvmClass.NVMSyncMLPostPone.tEndTime = Long.valueOf(oInput.toString());
				break;
			case E2P_SYNCML_FOTA_POSTPONE_DOWNLOADSTATE:
				DMNvmClass.NVMSyncMLPostPone.nAfterDownLoadBatteryStatus = Boolean.valueOf(oInput.toString());
				break;
			case E2P_SYNCML_FOTA_POSTPONE_POSTTIME:
				DMNvmClass.NVMSyncMLPostPone.nPostPoneTime = Long.valueOf(oInput.toString());
				break;
			case E2P_SYNCML_FOTA_POSTPONE:
				DMNvmClass.NVMSyncMLPostPone = (tsDBPostPone) oInput;
				break;
			case E2P_SYNCML_SIM_IMSI:
				DMNvmClass.NVMSYNCMLSimInfo.pIMSI = oInput.toString();
				break;
			case E2P_SYNCML_DM_NEXTUPDATETIME:
				DMNvmClass.tProfileList.lNextUpdateTime = Long.valueOf(oInput.toString());
				break;
			case E2P_SYNCML_DM_AUTOUPDATECHECK:
				DMNvmClass.tProfileList.bAutoCheck = Boolean.valueOf(oInput.toString());
				break;
			case E2P_SYNCML_DM_AGENT_MODE:
				DMNvmClass.tProfileList.bAgentMode = Boolean.valueOf(oInput.toString());
				break;
			case E2P_SYNCML_DM_CURR_CHECK_TIME:
				DMNvmClass.tProfileList.lcurrCheckTime = Long.valueOf(oInput.toString());
				break;
		}

		tsDBFileParam pfileparam = new tsDBFileParam();
		String pszFileName = "";

		if (dbadapter == null)
			dbadapter = new tsdmDBadapter();

		if (nType >= E2P_SYNCML_DM_PROFILE_IDX && nType < E2P_SYNCML_DM_PROFILE_MAX)
		{
			bret = tsdmDBsql.dmsqlUpdate(dmSqlDbIdProfileList, DMNvmClass.tProfileList);
		}
		else if (nType == E2P_SYNCML_DM_INFO_CONREF)
		{
			if (tsdmDBsql.existsNetworkRow(1))
			{
				bret = tsdmDBsql.dmsqlUpdate(dmSqlDbIdNetworkInfo, DMNvmClass.NVMSyncMLDMInfo.ConRef);
			}
			else
				tsdmDBsql.insertNetworkRow(DMNvmClass.NVMSyncMLDMInfo.ConRef);

		}
		else if (nType >= E2P_SYNCML_DM_INFO_IDX && nType < E2P_SYNCML_DM_INFO_MAX)
		{
			int SqlID = 0;
			int row = 0;

			if (dmProfileEntity.getRowState())
			{
				row = dmProfileEntity.getRow();
			}
			else
			{
				DMNvmClass.tProfileList = (tsdmProflieList) tsdmDBsql.dmsqlRead(dmSqlDbIdProfileList);
				if (DMNvmClass.tProfileList != null)
				{
					row = DMNvmClass.tProfileList.Profileindex;
				}
				else
				{
					tsLib.debugPrint(DEBUG_DB, "DMNvmClass.tProfileList is null");
				}
			}
			switch (row)
			{
				case 0:
					SqlID = dmSqlDbIdProfileInfo1;
					break;
				case 1:
					SqlID = dmSqlDbIdProfileInfo2;
					break;
				case 2:
					SqlID = dmSqlDbIdProfileInfo3;
					break;
			}
			bret = tsdmDBsql.dmsqlUpdate(SqlID, DMNvmClass.NVMSyncMLDMInfo);
		}
		else if (nType >= E2P_SYNCML_DM_ACCNODE_IDX && nType < E2P_SYNCML_DM_ACCNODE_MAX)
		{
			pfileparam.AreaCode = SyncMLNVMAccXNode;
			pszFileName = fileGetNameFromCallerID(pszFileName, pfileparam.AreaCode);
			bret = dbadapter.FileWrite(pszFileName, DMNvmClass.NVMSyncMLAccXNode);
		}
		else if (nType >= E2P_SYNCML_DM_RESYNC_IDX && nType < E2P_SYNCML_DM_RESYNC_MAX)
		{
			bret = tsdmDBsql.dmsqlUpdate(SyncMLNVMResyncMode, DMNvmClass.NVMSyncMLResyncMode);
		}
		else if (nType >= E2P_SYNCML_FUMO_IDX && nType < E2P_SYNCML_FUMO_MAX)
		{
			bret = tsdmDBsql.dmsqlUpdate(SyncDMFUMOInfo1, DMNvmClass.NVMSyncMLDMFUMOInfo);
		}
		else if (nType >= E2P_SYNCML_POSTPONE_IDX && nType < E2P_SYNCML_POSTPONE_MAX)
		{
			bret = tsdmDBsql.dmsqlUpdate(SyncMLPostPone, DMNvmClass.NVMSyncMLPostPone);
		}
		else if (nType >= E2P_SYNCML_DM_SIM_IDX && nType < E2P_SYNCML_DM_SIM_MAX)
		{
			bret = tsdmDBsql.dmsqlUpdate(SyncMLIMSIInfo, DMNvmClass.NVMSYNCMLSimInfo);
		}
		else if (nType >= E2P_SYNCML_DM_AGENT_IDX && nType < E2P_SYNCML_DM_AGENT_MAX)
		{
			dmAgentInfoDbWrite(nType, oInput);
			bret = tsdmDBsql.dmsqlUpdate(SyncMLNVMDmAgentInfo, DMNvmClass.NVMSyncMLDmAgentInfo);
		}
		return bret;
	}

	public static void dmdbReadFile(int nType)
	{
		try
		{
			switch (nType)
			{
				case DM_PROFILE_LIST:
					break;

				case DM_NET_PROFILE_LIST:
					dmdbsettinguinetinfolist();
					break;

				case DM_PROFILE_LIST_VIEW:
					break;

				case DM_DB_READ_PROFILE_INFO:
					break;
			}
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_DB, e.toString());
		}

	}

	public static tsDBFileParam dmDbSetSyncMLNVMUser(tsDBFileParam pfileparam)
	{
		if (pfileparam.AreaCode == SyncDMNVMProfile)
		{
			pfileparam.pNVMUser = (Object) DMNvmClass.tProfileList;
		}
		else if (pfileparam.AreaCode >= SyncDMNVMInfo1 && pfileparam.AreaCode <= SyncDMNVMInfo5)
		{
			pfileparam.pNVMUser = (Object) DMNvmClass.NVMSyncMLDMInfo;
		}
		else if (pfileparam.AreaCode == SyncDMFUMOInfo1)
		{
			pfileparam.pNVMUser = (Object) DMNvmClass.NVMSyncMLDMFUMOInfo;
		}
		else if (pfileparam.AreaCode == SyncMLPostPone)
		{
			pfileparam.pNVMUser = (Object) DMNvmClass.NVMSyncMLPostPone;
		}
		else if (pfileparam.AreaCode == SyncMLIMSIInfo)
		{
			pfileparam.pNVMUser = (Object) DMNvmClass.NVMSYNCMLSimInfo;
		}
		else if (pfileparam.AreaCode == SyncMLNVMAccXNode)
		{
			if (_SYNCML_TS_DM_VERSION_V12_)
				pfileparam.pNVMUser = (Object) DMNvmClass.NVMSyncMLAccXNode;
		}
		else if (pfileparam.AreaCode == SyncMLNVMResyncMode)
		{
			if (_SYNCML_TS_DM_VERSION_V12_)
				pfileparam.pNVMUser = (Object) DMNvmClass.NVMSyncMLResyncMode;
		}
		else if (pfileparam.AreaCode == SyncMLNVMDmAgentInfo)
		{
			pfileparam.pNVMUser = (Object) DMNvmClass.NVMSyncMLDmAgentInfo;
		}
		else if (pfileparam.AreaCode == SyncMLNVMNotiInfo)
		{
		}
		else
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, "Not Support Area Code: " + String.valueOf(pfileparam.AreaCode));
		}

		return pfileparam;
	}

	static Object dmDbInitNVMSyncDMProfile(Object pProfileList, int nMagicNumber)
	{
		tsdmProflieList ptProflieList = (tsdmProflieList) pProfileList;
		int nCount = 0;
		String strTemp = "";
		String pImei = "";
		
		if(DMNvmClass == null || DMNvmClass.tProfileList == null) //defect_110921
		{
			tsLib.debugPrintException(DEBUG_DM, "DMNvmClass or DMNvmClass.tProfileList is null. return");
			return null;
		}
		
		for (nCount = 0; nCount < DM_SETTING_PROFILE_NUM; nCount++)
		{
			DMNvmClass.tProfileList.Profileindex = nCount;
			strTemp = (String) dmdbRead(E2P_SYNCML_DM_INFO_PROFILENAME, strTemp);

			if (strTemp != null && strTemp.hashCode() == 0)
			{
				strTemp = (String) dmdbRead(E2P_SYNCML_DM_SERVERID, strTemp);
				if (strTemp == null)
					strTemp = "";
			}
			ptProflieList.ProfileName[nCount] = strTemp;
		}
		DMNvmClass.tProfileList.Profileindex = 0;

		ptProflieList.MagicNumber = nMagicNumber;
		pImei = dmDevinfoAdapter.devAdpGetDeviceId();
		if (pImei != null)
		{
			ptProflieList.szImei = pImei;
		}

		ptProflieList.NetworkConnName = REAL_DM_CONNECTION_NAME;
		ptProflieList.bWifiOnly = true;
		ptProflieList.bAutoUpdate = true;
		ptProflieList.bPushMessage = true;

		return pProfileList;
	}

	private static Object dmDbInitNVMSyncDMInfo(Object SyncdmInfo, int nMagicNumber)
	{
		tsdmInfo NVMSyncMLDMInfo = (tsdmInfo) SyncdmInfo;
		int nProfileIndex = 0;

		nProfileIndex = (int) (nMagicNumber - DMINFOMAGIC);
		tsLib.debugPrint("", "nProfileIndex = " + nProfileIndex);
		NVMSyncMLDMInfo = (tsdmInfo) getFactoryBootstrapData(NVMSyncMLDMInfo, nProfileIndex);
		NVMSyncMLDMInfo.MagicNumber = nMagicNumber;
		return NVMSyncMLDMInfo;
	}

	private static Object dmDbInitNVMSyncDMFUMOInfo(Object SyncDMFUMOInfo, int nMagicNumber)
	{
		tsDBFumoInfo pFUMOInfo = (tsDBFumoInfo) SyncDMFUMOInfo;

		if (pFUMOInfo == null)
		{
			pFUMOInfo = new tsDBFumoInfo();
			tsLib.debugPrintException(DEBUG_DB, "pFUMOInfo = null");

		}

		pFUMOInfo.ServerUrl = "http://";
		pFUMOInfo.ServerIP = "0.0.0.0";
		pFUMOInfo.ResultCode = "";
		pFUMOInfo.ServerPort = 80;
		pFUMOInfo.nDownloadMode = true;
		pFUMOInfo.Protocol = NETWORK_TYPE_HTTP;

		return pFUMOInfo;
	}

	private static Object dmDbInitNVMSyncDMPostPone(Object SyncDMPostPone, int magicNumber)
	{
		tsDBPostPone stSyncDMPostPone = (tsDBPostPone) SyncDMPostPone;
		return stSyncDMPostPone;
	}

	private static Object dmDbInitNVMSyncDMSimIMSI(Object SyncDMSimInfo, int magicNumber)
	{
		tsDBSimInfo pSimInfo = (tsDBSimInfo) SyncDMSimInfo;
		return pSimInfo;
	}

	private static Object dmdbNVMSyncDMAccXNodeInit(Object SyncDMAccXNode, int magicNumber)
	{
		tsDBAccXListNode stSyncDMAccXNode = (tsDBAccXListNode) SyncDMAccXNode;
		return stSyncDMAccXNode;
	}

	private static Object dmdbNVMSyncDMResyncModeInit(Object SyncDMResyncMode, int magicNumber)
	{
		tsDBResyncMode bResyncMode = (tsDBResyncMode) SyncDMResyncMode;

		// Default_ResyncMode_ON
		if (_SYNCML_TS_DM__VERSION_V12_NONCE_RESYNC)
			bResyncMode.nNoceResyncMode = true;
		else
			bResyncMode.nNoceResyncMode = false;
		return bResyncMode;
	}

	private static Object dmdbNVMSyncDmAgentInfoInit(Object NVMDmAgentInfo, int nMagicNumber)
	{
		dmAgentInfo DmAgentInfo = (dmAgentInfo) NVMDmAgentInfo;

		if (DmAgentInfo == null)
		{
			DmAgentInfo = new dmAgentInfo();
		}
		else
		{
			DmAgentInfo.nAgentType = SYNCML_DM_AGENT_DM;
		}

		return DmAgentInfo;
	}

	private static Object dmDbLoadCallback(int areaCode, Object pNVMUser, int magicNumber)
	{
		tsLib.debugPrint("", "areaCode = " + areaCode);
		tsLib.debugPrint("", "magicNumber = " + magicNumber);
		if (areaCode == SyncDMNVMProfile)
		{
			pNVMUser = dmDbInitNVMSyncDMProfile(pNVMUser, magicNumber);
		}
		else if (areaCode >= SyncDMNVMInfo1 && areaCode <= SyncDMNVMInfo5)
		{
			pNVMUser = dmDbInitNVMSyncDMInfo(pNVMUser, magicNumber);
		}
		else if (areaCode == SyncDMFUMOInfo1)
		{
			pNVMUser = dmDbInitNVMSyncDMFUMOInfo(pNVMUser, magicNumber);
		}
		else if (areaCode == SyncMLPostPone)
		{
			pNVMUser = dmDbInitNVMSyncDMPostPone(pNVMUser, magicNumber);
		}
		else if (areaCode == SyncMLIMSIInfo)
		{
			pNVMUser = dmDbInitNVMSyncDMSimIMSI(pNVMUser, magicNumber);
		}
		else if (areaCode == SyncMLNVMAccXNode)
		{
			if (_SYNCML_TS_DM_VERSION_V12_)
				pNVMUser = dmdbNVMSyncDMAccXNodeInit(pNVMUser, magicNumber);
		}
		else if (areaCode == SyncMLNVMResyncMode)
		{
			if (_SYNCML_TS_DM_VERSION_V12_)
				pNVMUser = dmdbNVMSyncDMResyncModeInit(pNVMUser, magicNumber);
		}
		else if (areaCode == SyncMLNVMDmAgentInfo)
		{
			pNVMUser = dmdbNVMSyncDmAgentInfoInit(pNVMUser, magicNumber);
		}
		else if (areaCode == SyncMLNVMNotiInfo)
		{
		}

		return pNVMUser;
	}

	public static boolean dmdbInitFfsFile(int FileID, int nMagicNumber)
	{
		tsLib.debugPrint("", "nMagicNumber = " + nMagicNumber);
		int wRC = 0;
		tsDBFileParam pfileparam = null;

		pfileparam = (tsDBFileParam) dmDbGetSyncMLFileParamAreaCode(FileID);

		if (pfileparam != null)
		{
			pfileparam = dmDbSetSyncMLNVMUser(pfileparam);
			pfileparam.pNVMUser = dmDbLoadCallback(pfileparam.AreaCode, pfileparam.pNVMUser, nMagicNumber);
		}
		else
		{
			return false;
		}

		if ((FileID >= SyncDMNVMProfile) && (FileID < SyncMLlastAreaCode))
		{
			wRC = tsdmDBsql.dmsqlCreate(FileID, pfileparam.pNVMUser); // /// -- dm sql test -------------
		}
		else
		{
			return false;
		}

		if (wRC == 0)
		{
			return true;
		}
		else
		{
			return false;
		}

	}

	private static Object dmDbGetSyncMLFileParamAreaCode(int areacode)
	{
		tsDBFileParam SyncMLFileParamtemp;

		if (areacode == SyncDMNVMProfile)
		{
			SyncMLFileParamtemp = dmSyncMLFileParam[eSyncMLDMNVMParameter.NVM_DM_PROFILE.Value()];
		}
		else if ((areacode >= SyncDMNVMInfo1) && (areacode <= SyncDMNVMInfo5))
		{
			SyncMLFileParamtemp = dmSyncMLFileParam[eSyncMLDMNVMParameter.NVM_DM_INFO.Value()];
		}
		else if (areacode == SyncDMFUMOInfo1)
		{
			SyncMLFileParamtemp = dmSyncMLFileParam[eSyncMLDMNVMParameter.NVM_FUMO_INFO.Value()];
		}
		else if (areacode == SyncMLPostPone)
		{
			SyncMLFileParamtemp = dmSyncMLFileParam[eSyncMLDMNVMParameter.NVM_FUMO_POSTPONE.Value()];
		}
		else if (areacode == SyncMLIMSIInfo)
		{
			SyncMLFileParamtemp = dmSyncMLFileParam[eSyncMLDMNVMParameter.NVM_IMSI_INFO.Value()];
		}
		else if ((areacode == SyncMLNVMAccXNode) && (_SYNCML_TS_DM_VERSION_V12_))
		{
			SyncMLFileParamtemp = dmSyncMLFileParam[eSyncMLDMNVMParameter.NVM_DM_ACC_X_NODE.Value()];
		}
		else if ((areacode == SyncMLNVMResyncMode) && (_SYNCML_TS_DM_VERSION_V12_))
		{
			SyncMLFileParamtemp = dmSyncMLFileParam[eSyncMLDMNVMParameter.NVM_NOTI_RESYNC_MODE.Value()];
		}
		else if (areacode == SyncMLNVMDmAgentInfo)
		{
			SyncMLFileParamtemp = dmSyncMLFileParam[eSyncMLDMNVMParameter.NVM_DM_AGENT_INFO.Value()];
		}
		else if (areacode == SyncMLNVMNotiInfo)
		{
			SyncMLFileParamtemp = dmSyncMLFileParam[eSyncMLDMNVMParameter.NVM_NOTI_INFO.Value()];
		}
		else
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, "FFS not find area code by num :" + areacode);
			return null;
		}

		return SyncMLFileParamtemp;
	}

	public static int dmAppendFile(int FileID, int nSize, byte[] pBuffer)
	{
		int eRet = TS_FS_OK;
		int handle = 0;

		try
		{
			eRet = dbAdpFileOpen(null, TS_FS_RDWR, FileID, handle);
			if (eRet != TS_FS_OK)
			{
				eRet = dbAdpAppFileCreate(null, FileID, nSize, pBuffer); // think as file not exist -> create new File
				//if (eRet != TS_FS_OK)
				//{
				//	tsLib.debugPrintException(DEBUG_EXCEPTION, "Create FAILED");
				//}
			}
			else
			{
				int len = dmdbGetFileSize(FileID);
				eRet = dbAdpAppendFileWrite(FileID, len, pBuffer, nSize);
				if (eRet != TS_FS_OK)
				{
					tsLib.debugPrintException(DEBUG_EXCEPTION, "Append FAILED");
				}
			}
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_DB, e.toString());
		}
		return eRet;
	}

	public static int dbAdpFileOpen(Object pszFileName, int mode, int FileID, int pHandle)
	{
		int eRet = TS_FS_OK;
		int nMode = 0;

		switch (mode)
		{
			case TS_FS_CREATE:
				nMode = DM_FILE_CREATE;
				break;
			case TS_FS_READ:
				nMode = DM_FILE_READ;
				break;
			case TS_FS_WRITE:
				nMode = DM_FILE_WRITE;
				break;
			case TS_FS_TRUNCATE:
				nMode = DM_FILE_TRUNCATE;
				break;
			case TS_FS_APPEND:
				nMode = DM_FILE_CREATE | DM_FILE_READ | DM_FILE_APPEND;
				break;
			case TS_FS_RDWR:
				nMode = DM_FILE_READ | DM_FILE_WRITE;
				break;
			default:
				nMode = mode;
				break;
		}

		eRet = FileOpen(pszFileName, nMode, FileID, pHandle);

		return eRet;
	}

	private static int FileOpen(Object fileName, int mode, int FileID, int pHandle)
	{
		String szfilename = "";
		DataInputStream Input = null;

		if (FileID > 0)
		{
			szfilename = tsdmDB.fileGetNameFromCallerID(szfilename, FileID);
			try
			{
				Input = new DataInputStream(new FileInputStream(szfilename));
			}
			catch (FileNotFoundException e)
			{
				tsLib.debugPrint(DEBUG_DB, e.toString());
				return TS_FS_ERR_FILE_NOT_FOUND;
			}
			finally
			{
				try
				{
					if (Input != null)
					{
						Input.close();
					}
				}
				catch (IOException e)
				{
					tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
				}
			}
		}
		else
		{
			return TS_FS_FAIL;
		}

		return TS_FS_OK;
	}

	public static int dbAdpAppendFileWrite(int FileID, int offset, byte[] buffer, int size)
	{
		String szfilename = "";
		FileOutputStream stream = null;
		DataOutputStream Data = null;
		int nRtnStatus = TS_FS_OK;

		if (FileID > 0)
			szfilename = fileGetNameFromCallerID(szfilename, FileID);
		else
			return TS_FS_ERR_BAD_PARAM;

		try
		{
			//Data = new DataOutputStream(new FileOutputStream(szfilename, true));
			stream = new FileOutputStream(szfilename, true);
			Data = new DataOutputStream(stream);

			synchronized (Data)
			{
				Data.write(buffer);
				stream.getFD().sync();
			}
		}
		catch (FileNotFoundException e)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
			nRtnStatus = TS_FS_ERR_FILE_NOT_FOUND;
		}
		catch (IOException e)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
			nRtnStatus = TS_ERR_NO_MEM_READY;
		}
		finally
		{
			try
			{
				if (Data != null)
				{
					Data.close();
				}
			}
			catch (IOException e)
			{
				tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
				nRtnStatus = TS_FS_FAIL;
			}
		}

		return nRtnStatus;
	}

	public static int dmdbGetFileSize(int FileID)
	{
		String szfilename = null;
		int eRet = 0;

		if (FileID > 0)
		{
			szfilename = fileGetNameFromCallerID(szfilename, FileID);
		}
		else
			return -1;

		try
		{
			eRet = (int) dbadapter.FileGetSize(szfilename);
		}
		catch (NullPointerException ex)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, ex.toString());
		}
		return eRet;
	}

	public static int dmdbDeleteFile(int FileID)
	{
		int rc = 0;
		rc = dbAdpFileDelete((String) null, (int) FileID);
		return rc;
	}

	public static Object dmdbFullResetFFS()
	{
		int i = 0;
		tsdmFileParam pFileParam;

		int IndexLen = eSyncMLDMFileParameter.FileMax.Index();// Defects
		for (i = 0; i < IndexLen; i++)
		{ // Defects
			pFileParam = SyncDMFileParam[i];

			if (dbAdpFileExists(null, (int) pFileParam.FileID) == 0)
			{
				if (dbAdpFileDelete(null, (int) pFileParam.FileID) == 0)
				{
					pFileParam.nSize = 0;
				}
			}
		}

		return true;
	}

	public static void dbDMffs_Init()
	{
		int AreaCodeTemp;
		int nCount = 0;

		tsLib.debugPrint(DEBUG_DB, "");

		if(_SYNCML_TS_DM_REGISTRY_PROFILE_)
		{
			dmPreConfigEntity.openConfigFile();
		}
		
		/* Must Make before Profile Info. */
		if (_SYNCML_TS_DM_VERSION_V12_)
		{
			AreaCodeTemp = SyncMLNVMAccXNode;
			if (!tsdmDBsql.existsAccXListNodeRow(3))
				dmdbInitFfsFile((int) AreaCodeTemp, 0);
		}

		for (nCount = 0; nCount < DM_SETTING_PROFILE_NUM; nCount++)
		{
			AreaCodeTemp = SyncDMNVMInfo1 + nCount;
			if (!tsdmDBsql.existsProfileRow((long)nCount + 1))
				dmdbInitFfsFile((int) AreaCodeTemp, DMINFOMAGIC + nCount);
		}

		AreaCodeTemp = SyncDMNVMProfile;
		if (!tsdmDBsql.existsProfileListRow(1))
			dmdbInitFfsFile((int) AreaCodeTemp, DMPROFILEMAGIC);
		else
			dmdbRead(DM_PROFILE_LIST);

		AreaCodeTemp = SyncDMFUMOInfo1;
		if (!tsdmDBsql.existsFUMORow(1))
			dmdbInitFfsFile((int) AreaCodeTemp, 0);

		AreaCodeTemp = SyncMLPostPone; // PostPoneFFS
		if (!tsdmDBsql.existsPostPoneRow(1))
			dmdbInitFfsFile((int) AreaCodeTemp, 0);

		AreaCodeTemp = SyncMLIMSIInfo; // SIM IMSI
		if (!tsdmDBsql.existsSimInfoRow(1))
			dmdbInitFfsFile((int) AreaCodeTemp, 0);

		if (_SYNCML_TS_DM_VERSION_V12_)
		{
			AreaCodeTemp = SyncMLNVMResyncMode; // Nonce_resync_menu
			if (!tsdmDBsql.existsResyncModeRow(1))
				dmdbInitFfsFile((int) AreaCodeTemp, 0);
		}

		AreaCodeTemp = SyncMLNVMDmAgentInfo;
		if (!tsdmDBsql.dmDbSqlAgentInfoExistsRow(1))
			dmdbInitFfsFile((int) AreaCodeTemp, 0);

	}

	public static String fileGetNameFromCallerID(String szFileNameTemp, int FileID)
	{
		long handle = 0;
		String szFileName;
		if (FileID == eSyncMLDMFileParameter.FileFirmwareData.FileId())
		{
			handle = (long)(FFS_OWNER_SYNCML * 10000) + FileID;
			
			if(_SYNCML_TS_DM_DELTA_INTERIOR_MEMORY_STORAGE_)
			{
				int nDeltaFileIndex = dmdbGetDeltaFileSaveIndex();
				if(nDeltaFileIndex == DELTA_EXTERNAL_MEMORY)
				{
					szFileName = String.format("%s/%d%s", DM_FS_FFS_EXTERNEL_DIRECTORY_FOTA, handle, DM_FS_FFS_FILE_EXTENTION);
				}
				else
				{
					szFileName = String.format("%s/%d%s", DM_FS_FFS_DIRECTORY, handle, DM_FS_FFS_FILE_EXTENTION);
				}
			}
			else if(_SYNCML_TS_DM_DELTA_MULTI_MEMORY_STORAGE_)
			{
				int nDeltaFileIndex = dmdbGetDeltaFileSaveIndex();
				if(nDeltaFileIndex == DELTA_EXTERNAL_MEMORY)
				{
					szFileName = String.format("%s/%d%s", DM_FS_FFS_EXTERNEL_DIRECTORY_FOTA, handle, DM_FS_FFS_FILE_EXTENTION);
				}
				else if(nDeltaFileIndex == DELTA_EXTERNAL_SD_MEMORY)
				{
					szFileName = String.format("%s/%d%s", DM_FS_FFS_EXTERNEL_SD_DIRECTORY_FOTA, handle, DM_FS_FFS_FILE_EXTENTION);
				}
				else
				{
					szFileName = String.format("%s/%d%s", DM_FS_FFS_DIRECTORY, handle, DM_FS_FFS_FILE_EXTENTION);
				}
			}
			else if(_SYNCML_TS_DM_DELTA_EXTERNAL_STORAGE_)
			{
				szFileName = String.format("%s/%d%s", DM_FS_FFS_EXTERNEL_DIRECTORY_FOTA, handle, DM_FS_FFS_FILE_EXTENTION);
			}
			else
			{
				szFileName = String.format("%s/%d%s", DM_FS_FFS_DIRECTORY, handle, DM_FS_FFS_FILE_EXTENTION);
			}
		}
		else
		{
			handle = (long)(FFS_OWNER_SYNCML * 10000) + FileID;
			szFileName = String.format("%s/%d%s", DM_FS_FFS_DIRECTORY, handle, DM_FS_FFS_FILE_EXTENTION);
		}

		return szFileName;
	}

	public static int getFotaFileId()
	{
		return (int) g_nFOTAFileId;
	}

	public static int dbAdpAppFileCreate(String pFileName, int FileID, long l, byte[] pBuffer)
	{

		String szFileName = null;
		szFileName = fileGetNameFromCallerID(szFileName, FileID);

		dbadapter.FileCreateWrite(szFileName, pBuffer);
		return 0;
	}

	public static int dbAdpFileExists(String pszFileName, int FileID)
	{
		String szfilename = null;
		int eRet = 0;

		if (FileID > 0)
		{
			szfilename = fileGetNameFromCallerID(szfilename, FileID);

		}
		else
		{
			if (pszFileName == null)
			{
				return TS_FS_ERR_BAD_PARAM;
			}
			else
			{
				szfilename = pszFileName;
			}
		}

		if (dbadapter == null)
			dbadapter = new tsdmDBadapter();

		eRet = dbadapter.FileExists(szfilename, FileID);
		return eRet;
	}

	public static int dbAdpFileDelete(String pszFileName, int FileID)
	{
		int eRet = 0;
		String szfilename = null;

		if (FileID > 0)
		{
			szfilename = fileGetNameFromCallerID(szfilename, FileID);
		}
		else
		{
			if (pszFileName == null)
			{
				tsLib.debugPrintException(DEBUG_EXCEPTION, "pszFileName is NULL");
				return TS_FS_ERR_BAD_PARAM;
			}
			else
			{
				szfilename = pszFileName;
			}
		}
		try
		{
			eRet = dbadapter.FileRemove(szfilename, FileID);
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_DB, e.toString());
		}
		return eRet;
	}

	public static boolean clearCache(File targetFolder)
	{
		File[] childFile = targetFolder.listFiles();
		boolean confirm = false;
		int size = 0;

		tsLib.debugPrint(DEBUG_DM, "");
		if (childFile == null)
		{
			tsLib.debugPrint(DEBUG_DM, "can not get child list of " + targetFolder.getPath());
			return false;
		}

		size = childFile.length;
		tsLib.debugPrint(DEBUG_DM, String.format("nfilenum of directory [%s] : %d ", targetFolder.getPath(), size));
		try
		{
			if (size > 0)
			{
				for (int i = 0; i < size; i++)
				{
					String szName = childFile[i].getName();
					if (szName.contains("lost+found") || szName.contains("recovery"))
					{
						tsLib.debugPrintException(DEBUG_DM, "cannot delete specific file in cache directory " + szName);
					}
					else
					{
						if (childFile[i].isFile())
						{
							confirm = childFile[i].delete();
							if (!confirm)
							{
								tsLib.debugPrintException(DEBUG_DM, "fail to delete " + childFile[i].getPath());
							}
							else
								tsLib.debugPrintException(DEBUG_DM, "deleted file name is " + szName);
						}
						else
						{
							clearCache(childFile[i]);
						}
					}
				}
			}
		}
		catch (Exception ex)
		{
			tsLib.debugPrintException(DEBUG_DM, "fail to delete");
			return false;
		}
		return true;
	}
	
	public static boolean dmReadFile(int FileID, int nOffset, int nSize, byte[] pData)
	{
		String szfilename = null;
		boolean bRet = false;

		try
		{
			szfilename = fileGetNameFromCallerID(szfilename, FileID);
			bRet = dbadapter.FileRead(szfilename, pData, (int) nOffset, (int) nSize);
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_DB, e.toString());
		}
		return bRet;
	}

	public static Object dmReadFile(int FileID, int nOffset, int nSize)
	{
		String szfilename = null;
		byte[] Input = new byte[nSize];
		try
		{
			szfilename = fileGetNameFromCallerID(szfilename, FileID);
			dbadapter.FileRead(szfilename, Input, (int) nOffset, (int) nSize);
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_DB, e.toString());
		}
		return Input;
	}

	public static boolean dmdbWriteFile(int FileID, int nSize, Object pData)
	{
		String szfilename = null;
		boolean bRet = true;

		szfilename = fileGetNameFromCallerID(szfilename, FileID);

		bRet = dbadapter.FileWrite(szfilename, nSize, pData);
		return bRet;
	}

	private static void dmdbsettinguinetinfolist()
	{

		dbadapter.dmdb.DMNetProfile.ConRefName = "NetWork Info";// +(i+1) +"-1";

		dbadapter.dmdb.DMNetProfile.ConRef = (tsdmInfoConRef) tsdmDBsql.dmsqlRead(dmSqlDbIdNetworkInfo);

		NetProfileClass = dbadapter.dmdb.DMNetProfile;
	}

	// ======================================================================================================================//

	public static int dmdbGetFileIdObjectData()
	{
		return eSyncMLDMFileParameter.FileObjectData.FileId();
	}

	public static int dmdbGetFileIdObjectTreeInfo()
	{
		return eSyncMLDMFileParameter.FileObjectTreeInfo.FileId();
	}

	public static int dmdbGetFileIdFirmwareData()
	{
		return eSyncMLDMFileParameter.FileFirmwareData.FileId();
	}

	public static int dmdbGetFileIdLargeObjectData()
	{
		return eSyncMLDMFileParameter.FileLargeObjectData.FileId();
	}
	public static int dmdbGetFileIdTNDS()
	{
		return eSyncMLDMFileParameter.FileTndsXmlData.FileId();
	}

	public static int dmdbGetProfileIndex()
	{
		int nIdx = 0;
		try
		{
			nIdx = Integer.valueOf(String.valueOf(dmdbRead(E2P_SYNCML_DM_PROFILE_INDEX, nIdx)));
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_DB, e.toString());
		}
		return nIdx;
	}

	public static void dmdbSetProfileIndex(int idx)
	{
		try
		{
			dmdbWrite(E2P_SYNCML_DM_PROFILE_INDEX, String.valueOf(idx));
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_DB, String.format("unable to write index\n%s", e.toString()));
		}
	}

	public static int dmdbGetAuthType()
	{
		int authType = 0;
		try
		{
			authType = Integer.valueOf(String.valueOf(dmdbRead(E2P_SYNCML_DM_AUTHTYPE, authType)));
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_DB, e.toString());
		}
		return authType;
	}

	public static int dmdbGetServerAuthType()
	{
		int authType = 0;
		try
		{
			authType = Integer.valueOf(String.valueOf(dmdbRead(E2P_SYNCML_DM_SERVER_AUTHTYPE, authType)));
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_DB, e.toString());
		}
		return authType;
	}

	public static void dmdbSetAuthType(int authType)
	{
		try
		{
			dmdbWrite(E2P_SYNCML_DM_AUTHTYPE, String.valueOf(authType));
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_DB, e.toString());
		}
	}

	public static void dmdbSetServerAuthType(int authType)
	{
		try
		{
			dmdbWrite(E2P_SYNCML_DM_SERVER_AUTHTYPE, String.valueOf(authType));
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_DB, e.toString());
		}
	}

	public static void dmdbSetChangedProtocol(boolean bChanged)
	{
		tsLib.debugPrint(DEBUG_DB, " : " + bChanged);
		
		try
		{
			dmdbWrite(E2P_SYNCML_DM_CHANGED_PROTOCOL, String.valueOf(bChanged));
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_DB, e.toString());
		}
	}

	public static boolean dmdbGetChangedProtocol()
	{
		boolean bChanged = false;
		try
		{
			bChanged = Boolean.valueOf(String.valueOf(dmdbRead(E2P_SYNCML_DM_CHANGED_PROTOCOL, bChanged)));
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
		}
		return bChanged;
	}

	public static Object dmdbGetProfileInfo(Object ptDMProfileInfo)
	{
		tsdmInfo ptProfileInfo = null;

		ptProfileInfo = (tsdmInfo) ptDMProfileInfo;
		try
		{
			ptProfileInfo = (tsdmInfo) dmdbRead(E2P_SYNCML_DM_INFO, ptProfileInfo);
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_DB, e.toString());
		}
		return ptProfileInfo;
	}

	public static boolean dmdbSetProfileInfo(tsdmInfo ptProfileInfo)
	{
		boolean nRet = false;
		
		try
		{
			dmdbWrite(E2P_SYNCML_DM_INFO, ptProfileInfo);
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_DB, e.toString());
		}
		return nRet;
	}

	public static String dmdbGetProfileName()
	{
		String pProfileName = null;
		pProfileName = "server1";
		try
		{
			pProfileName = (String) dmdbRead(E2P_SYNCML_DM_INFO_PROFILENAME, pProfileName);
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_DB, e.toString());
		}
		return pProfileName;
	}

	public static void dmdbSetProfileName(int nIdx, String pProflieName)
	{
		try
		{
			dmdbWrite(E2P_SYNCML_DM_PROFILENAME(nIdx), pProflieName);
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_DB, e.toString());
		}
	}

	public static void dmdbSetDMXNodeInfo(int nIndex, Object ptAccXNodeInfo)
	{
		try
		{
			dmdbWrite(E2P_SYNCML_DM_ACCXNODE_INFO(nIndex), ptAccXNodeInfo);
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_DB, e.toString());
		}
	}
	public static void dmdbSetServerUrl(String pServerUrl)
	{
		try
		{
			dmdbWrite(E2P_SYNCML_DM_SERVERURL, pServerUrl);
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_DB, e.toString());
		}
	}

	public static void dmdbSetServerAddress(String pAddress)
	{
		try
		{
			dmdbWrite(E2P_SYNCML_DM_SERVERIP, pAddress);
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_DB, e.toString());
		}
	}

	public static void dmdbSetServerPort(int nPort)
	{
		try
		{
			dmdbWrite(E2P_SYNCML_DM_SERVERPORT, String.valueOf(nPort));
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_DB, e.toString());
		}
	}

	public static void dmdbSetServerProtocol(String pProtocol)
	{
		try
		{
			dmdbWrite(E2P_SYNCML_DM_PROTOCOL, pProtocol);
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_DB, e.toString());
		}
	}

	public static String dmdbGetProtocol()
	{
		String pProtocol = "";
		try
		{
			pProtocol = (String) dmdbRead(E2P_SYNCML_DM_PROTOCOL, pProtocol);
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_DB, e.toString());
		}
		return pProtocol;
	}

	public static String dmdbGetFUMOProtocol()
	{
		int nFUMOStatus = DM_FUMO_STATE_NONE;
		String pProtocol = "";

		nFUMOStatus = dmdbGetFUMOStatus();

		try
		{
			if (nFUMOStatus == DM_FUMO_STATE_DOWNLOAD_COMPLETE || nFUMOStatus == DM_FUMO_STATE_DOWNLOAD_FAILED || nFUMOStatus == DM_FUMO_STATE_DOWNLOAD_IN_CANCEL)
			{
				pProtocol = (String) dmdbRead(E2P_SYNCML_FUMO_STATUSNOTIFYPROTOCOL, pProtocol);
			}
			else if (nFUMOStatus == DM_FUMO_STATE_IDLE_START)
			{
				pProtocol = (String) dmdbRead(E2P_SYNCML_FUMO_PROTOCOL, pProtocol);
			}
			else
			{
				pProtocol = (String) dmdbRead(E2P_SYNCML_FUMO_OBJECTDOWNLOADPROTOCOL, pProtocol);
			}
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_DB, e.toString());
		}

		return pProtocol;
	}

	public static void dmdbSetNotiSessionID(int appId, String nSessionID)
	{
		switch (appId)
		{
			case SYNCMLDM:
				try
				{
					dmdbWrite(E2P_SYNCML_DM_SESSIONID, nSessionID);
				}
				catch (Exception e)
				{
					tsLib.debugPrintException(DEBUG_DB, e.toString());
				}
				break;

			case SYNCMLDL:
				tsLib.debugPrintException(DEBUG_EXCEPTION, "Not Support Application");
				break;

			default:
				tsLib.debugPrintException(DEBUG_EXCEPTION, "Not Support Application");
				break;
		}
	}

	public static String dmdbGetNotiSessionID(int appId)
	{
		String SessionId = null;

		try
		{
			switch (appId)
			{
				case SYNCMLDM:
					SessionId = (String) dmdbRead(E2P_SYNCML_DM_SESSIONID, SessionId);
					break;

				case SYNCMLDL:
					tsLib.debugPrintException(DEBUG_EXCEPTION, "Not Support Application");
					break;

				default:
					tsLib.debugPrintException(DEBUG_EXCEPTION, "Not Support Application");
					SessionId = (String) dmdbRead(E2P_SYNCML_DM_SESSIONID, SessionId);
					break;
			}
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_DB, e.toString());
		}

		return SessionId;

	}

	public static void dmdbSetServerNonce(String pNonce)
	{
		try
		{
			dmdbWrite(E2P_SYNCML_DM_SERVER_NONCE, pNonce);
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_DB, e.toString());
		}
	}

	public static void dmdbSetClientNonce(String pNonce)
	{
		if (pNonce == null)
		{
			return;
		}	

		try
		{
			dmdbWrite(E2P_SYNCML_DM_CLIENT_NONCE, pNonce);
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_DB, e.toString());
		}
	}

	public static String dmdbGetServerUrl(int appId)
	{
		String ServerUrl = null;
		int nStatus = DM_FUMO_STATE_NONE;

		try
		{
			switch (appId)
			{
				case SYNCMLDM:
					ServerUrl = (String) dmdbRead(E2P_SYNCML_DM_SERVERURL, ServerUrl);
					break;

				case SYNCMLDL:
					int nAgentType = SYNCML_DM_AGENT_DM;

					nStatus = dmdbGetFUMOStatus();
					if (nStatus == DM_FUMO_STATE_DOWNLOAD_COMPLETE || nStatus == DM_FUMO_STATE_DOWNLOAD_FAILED || nStatus == DM_FUMO_STATE_DOWNLOAD_IN_CANCEL)
					{
						ServerUrl = (String) dmdbRead(E2P_SYNCML_FUMO_STATUSNOTIFYURL, ServerUrl);
					}
					else if (nStatus == DM_FUMO_STATE_IDLE_START || nStatus == DM_STATE_GET_SESSION_ID_START)
					{
						ServerUrl = (String) dmdbRead(E2P_SYNCML_FUMO_SERVERURL, ServerUrl);
					}
					else
					{
						ServerUrl = (String) dmdbRead(E2P_SYNCML_FUMO_OBJECTDOWNLOADURL, ServerUrl);
					}
					break;

				default:
					ServerUrl = (String) dmdbRead(E2P_SYNCML_DM_SERVERURL, ServerUrl);
					break;
			}
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_DB, e.toString());
		}
		return ServerUrl;
	}

	public static String getServerAddress(int appId)
	{
		int nStatus = DM_FUMO_STATE_NONE;
		String ServerIP = null;
		try
		{
			switch (appId)
			{
				case SYNCMLDM:
					ServerIP = (String) dmdbRead(E2P_SYNCML_DM_SERVERIP, ServerIP);
					break;

				case SYNCMLDL:
					int nAgentType = SYNCML_DM_AGENT_DM;

					nStatus = dmdbGetFUMOStatus();
					if (nStatus == DM_FUMO_STATE_DOWNLOAD_COMPLETE || nStatus == DM_FUMO_STATE_DOWNLOAD_FAILED || nStatus == DM_FUMO_STATE_DOWNLOAD_IN_CANCEL)
					{
						ServerIP = (String) dmdbRead(E2P_SYNCML_FUMO_STATUSNOTIFYIP, ServerIP);
					}
					else if (nStatus == DM_FUMO_STATE_IDLE_START)
					{
						ServerIP = (String) dmdbRead(E2P_SYNCML_FUMO_SERVERIP, ServerIP);
					}
					else
					{
						ServerIP = (String) dmdbRead(E2P_SYNCML_FUMO_OBJECTDOWNLOADIP, ServerIP);
					}
					break;

				default:
					ServerIP = (String) dmdbRead(E2P_SYNCML_DM_SERVERIP, ServerIP);
					break;
			}
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_DB, e.toString());
		}
		return ServerIP;
	}

	public static String dmdbGetAuthLevel()
	{
		String pszAuthLevel = null;
		try
		{
			pszAuthLevel = (String) dmdbRead(E2P_SYNCML_DM_AUTHLEVEL, pszAuthLevel);
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_DB, e.toString());
		}
		return pszAuthLevel;
	}

	public static String dmdbGetServerAuthLevel()
	{
		String pszAuthLevel = null;
		try
		{
			pszAuthLevel = (String) dmdbRead(E2P_SYNCML_DM_SERVERAUTHLEVEL, pszAuthLevel);
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_DB, e.toString());
		}
		return pszAuthLevel;
	}

	public static String dmdbGetPrefConRef()
	{
		String pszPrefConRef = null;
		try
		{
			pszPrefConRef = (String) dmdbRead(E2P_SYNCML_DM_PREFCONREF, pszPrefConRef);
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_DB, e.toString());
		}
		return pszPrefConRef;
	}

	public static String dmdbGetServerID()
	{
		String severid = null;
		try
		{
			severid = (String) dmdbRead(E2P_SYNCML_DM_SERVERID, severid);
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_DB, e.toString());
		}
		return severid;
	}

	public static String dmdbGetUsername()
	{
		String Username = null;
		Username = (String) dmdbRead(E2P_SYNCML_DM_USERNAME, Username);
		return Username;
	}

	public static String dmdbGetClientPassword()
	{
		String ClientPassword = null;
		try
		{
			ClientPassword = (String) dmdbRead(E2P_SYNCML_DM_PASSWORD, ClientPassword);
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_DB, e.toString());
		}
		return ClientPassword;
	}

	public static String dmdbGetServerPassword()
	{
		String ServerPassword = null;
		try
		{
			ServerPassword = (String) dmdbRead(E2P_SYNCML_DM_SERVERPASSWORD, ServerPassword);
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_DB, e.toString());
		}
		return ServerPassword;
	}

	public static String dmdbGetServerNonce()
	{
		String pNonce = "";
		try
		{
			pNonce = (String) dmdbRead(E2P_SYNCML_DM_SERVER_NONCE, pNonce);
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_DB, e.toString());
		}
		return pNonce;
	}

	public static String dmdbGetClientNonce()
	{
		String pNonce = null;
		try
		{
			pNonce = (String) dmdbRead(E2P_SYNCML_DM_CLIENT_NONCE, pNonce);
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_DB, e.toString());
		}
		return pNonce;
	}

	public static int dmdbGetServerPort(int appId)
	{
		int nStatus = DM_FUMO_STATE_NONE;
		int port = 0;
		try
		{
			switch (appId)
			{
				case SYNCMLDM:
					port = Integer.valueOf(String.valueOf(dmdbRead(E2P_SYNCML_DM_SERVERPORT, port)));
					break;

				case SYNCMLDL:
					int nAgentType = SYNCML_DM_AGENT_DM;

					nStatus = dmdbGetFUMOStatus();
					if (nStatus == DM_FUMO_STATE_DOWNLOAD_COMPLETE || nStatus == DM_FUMO_STATE_DOWNLOAD_FAILED || nStatus == DM_FUMO_STATE_DOWNLOAD_IN_CANCEL)
					{
						port = Integer.valueOf(String.valueOf(dmdbRead(E2P_SYNCML_FUMO_STATUSNOTIFYPORT, port)));
					}
					else if (nStatus == DM_FUMO_STATE_IDLE_START)
					{
						port = Integer.valueOf(String.valueOf(dmdbRead(E2P_SYNCML_FUMO_SERVERPORT, port)));
					}
					else
					{
						port = Integer.valueOf(String.valueOf(dmdbRead(E2P_SYNCML_FUMO_OBJECTDOWNLOADPORT, port)));
					}
					break;

				default:
					break;
			}
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_DB, e.toString());
		}

		return port;
	}

	public static int dmdbGetNotiEvent()
	{
		int nEvent = 0;
		try
		{
			// defect_110921
			Object oStatus = dmdbRead(E2P_SYNCML_DM_PROFILE_INDEX_NOTI_EVT, nEvent);
			if (oStatus != null)
				nEvent = ((Integer) oStatus).intValue();			
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_DB, e.toString());
		}
		return nEvent;
	}

	public static void dmdbSetNotiEvent(int nEvent)
	{
		try
		{
			dmdbWrite(E2P_SYNCML_DM_PROFILE_INDEX_NOTI_EVT, String.valueOf(nEvent));
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_DB, e.toString());
		}
		return;
	}

	public static String dmdbGetNetworkConnName(int appId)
	{
		String pNetConnName = null;
		try
		{
/*			switch (appId)
			{
				case SYNCMLDM:
					pNetConnName = (String) dmdbRead(E2P_SYNCML_DM_NETWORKCONNNAME, pNetConnName);
					break;
				case SYNCMLDL:
					pNetConnName = (String) dmdbRead(E2P_SYNCML_DM_NETWORKCONNNAME, pNetConnName);
					break;
				default:
					pNetConnName = (String) dmdbRead(E2P_SYNCML_DM_NETWORKCONNNAME, pNetConnName);
					break;
			}*/
			pNetConnName = (String) dmdbRead(E2P_SYNCML_DM_NETWORKCONNNAME, pNetConnName);
			tsLib.debugPrint(DEBUG_DB, "AppID[" + appId + "], NetConnName is " + pNetConnName);

		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_DB, e.toString());
		}

		return pNetConnName;
	}

	public static void dmdbSetNetworkConnName(int appId, String pNetConnName)
	{
		switch (appId)
		{
			case SYNCMLDM:
				try
				{
					dmdbWrite(E2P_SYNCML_DM_NETWORKCONNNAME, pNetConnName);
				}
				catch (Exception e)
				{
					tsLib.debugPrintException(DEBUG_DB, e.toString());
				}
				break;
			case SYNCMLDL:
				break;
			default:
				try
				{
					dmdbWrite(E2P_SYNCML_DM_NETWORKCONNNAME, pNetConnName);
				}
				catch (Exception e)
				{
					tsLib.debugPrintException(DEBUG_DB, e.toString());
				}
				break;
		}

	}

	public static tsdmInfoConRef dmdbGetConRef(tsdmInfoConRef ptConRef)
	{
		try
		{
			ptConRef = (tsdmInfoConRef) dmdbRead(E2P_SYNCML_DM_INFO_CONREF, ptConRef);
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_DB, e.toString());
		}
		return ptConRef;
	}

	public static void dmdbSetConRef(tsdmInfoConRef ptConRef)
	{
		try
		{
			dmdbWrite(E2P_SYNCML_DM_INFO_CONREF, ptConRef);
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_DB, e.toString());
		}
	}

	public static tsDBNetConProfileBackup dmdbGetConBack(tsDBNetConProfileBackup ptConBack)
	{
		try
		{
			ptConBack = (tsDBNetConProfileBackup) dmdbRead(E2P_SYNCML_DM_INFO_CON_BACKUP, ptConBack);
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_DB, e.toString());
		}
		return ptConBack;
	}

	public static boolean dmdbCheckProfileListExist()
	{
		tsdmProflieList pDMProfile = null;
		boolean bReturn = false;
		int i = 0;

		pDMProfile = new tsdmProflieList();
		try
		{
			pDMProfile = (tsdmProflieList) dmdbRead(E2P_SYNCML_DM_PROFILE, pDMProfile);
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_DB, e.toString());
		}

		if (pDMProfile != null)
		{
			for (i = 0; i < DM_SETTING_PROFILE_NUM; i++)
			{
				if (!tsLib.isEmpty(pDMProfile.ProfileName[i]))
				{
					bReturn = true;
					break;
				}
			}
		}
		return bReturn;
	}

	public static int dmdbGetFUMOStatus()
	{
		int nStatus = 0;
		// Slow db Write speed 
		if (nFumoStatus != -1)
		{
			nStatus = nFumoStatus;
		}
		else 
		{
			Object oStatus = null;
			try
			{
				oStatus = dmdbRead(E2P_SYNCML_FUMO_STATUS, nStatus);
			}
			catch (Exception e)
			{
				tsLib.debugPrintException(DEBUG_DB, e.toString());
			}
			if (oStatus != null && oStatus.hashCode() != 0)
			{
				nStatus = ((Integer) oStatus).intValue();
			}
			else
			{
				nStatus = 0;
				// if status is 0, no report
			}
		}
		tsLib.debugPrint(DEBUG_DB, "= [" + nStatus+"]");
		return nStatus;
	}

	public static void dmdbSetFUMOStatus(int nstatus)
	{
		boolean bret = false;
		
		// Slow db Write speed 
		int nBuckupStatus = nFumoStatus;
		nFumoStatus = nstatus;
		
		tsLib.debugPrint(DEBUG_DB, "[" + nstatus +"]");
		try
		{
			bret = dmdbWrite(E2P_SYNCML_FUMO_STATUS, String.valueOf(nstatus));
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
		}

		if (!bret)
		{
			nFumoStatus = nBuckupStatus;
			tsLib.debugPrintException(DEBUG_EXCEPTION, "db write was failed");
		}
	}

	public static void dmdbSetFUMOStatusNode(String pStatus, int len)
	{
		try
		{
			dmdbWrite(E2P_SYNCML_FUMO_STATUS_NODE, pStatus);
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_DB, e.toString());
		}
	}

	public static String dmdbGetFUMOStatusNode()
	{
		String pNodeName = null;
		try
		{
			pNodeName = (String) dmdbRead(E2P_SYNCML_FUMO_STATUS_NODE, pNodeName);
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_DB, e.toString());
		}

		tsLib.debugPrint(DEBUG_DB, "pNodeName = " + pNodeName);
		return pNodeName;
	}

	public static void dmdbSetFUMOCorrelator(String pCorrelator)
	{
		tsLib.debugPrint(DEBUG_DB, "pCorrelator = " + pCorrelator);
		try
		{
			dmdbWrite(E2P_SYNCML_FUMO_CORRELATOR, pCorrelator);
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_DB, e.toString());
		}
	}

	public static String dmdbGetFUMOCorrelator()
	{
		String pCorrelator = null;
		try
		{
			pCorrelator = (String) dmdbRead(E2P_SYNCML_FUMO_CORRELATOR, pCorrelator);
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_DB, e.toString());
		}
		tsLib.debugPrint(DEBUG_DB, "pCorrelator = " + pCorrelator);

		return pCorrelator;
	}

	public static String dmdbGetMimeType()
	{
		int nFUMOStatus = DM_FUMO_STATE_NONE;

		nFUMOStatus = dmdbGetFUMOStatus();
		tsLib.debugPrint(DEBUG_DB, "nFUMOStatus :" + nFUMOStatus);

		if (nFUMOStatus == DM_FUMO_STATE_DOWNLOAD_DESCRIPTOR || nFUMOStatus == DM_FUMO_STATE_DOWNLOAD_IN_PROGRESS)
		{
			return dmdbGetFUMODDContentType();
		}
		return null;
	}

	public static String dmdbGetAcceptType()
	{
		int nFUMOStatus = DM_FUMO_STATE_NONE;

		nFUMOStatus = dmdbGetFUMOStatus();

		if (nFUMOStatus == DM_FUMO_STATE_DOWNLOAD_DESCRIPTOR || nFUMOStatus == DM_FUMO_STATE_DOWNLOAD_IN_PROGRESS)
		{
			return dmdbGetFUMODDAcceptType();
		}

		return null;
	}

	public static boolean dmdbGetWifiOnlyFlag()
	{
		boolean check = false;
		tsLib.debugPrint(DEBUG_DB, " is" + Boolean.toString(check));
		return check;
	}
	
	public static boolean dmdbSetWifiOnlyFlag(boolean check)
	{		
		boolean nRet = false;
		try
		{
			nRet = dmdbWrite(E2P_SYNCML_DM_WIFIONLY,check);
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_DB, e.toString());
		}
		return nRet;
	}
	
	public static int dmdbGetDeltaFileSaveIndex()
	{
		int nIndex = 0;
		try
		{
			// defect_110921
			Object oStatus = dmdbRead(E2P_SYNCML_DM_DELTAFILE_SAVE_INDEX, nIndex);
			if (oStatus != null)
				nIndex = ((Integer) oStatus).intValue();
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_DB, e.toString());
		}
		
		return nIndex;
	}

	public static void dmdbSetDeltaFileSaveIndex(int nIndex)
	{
		tsLib.debugPrint(DEBUG_DB, " : " + nIndex);
		try
		{
			dmdbWrite(E2P_SYNCML_DM_DELTAFILE_SAVE_INDEX, nIndex);
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
		}
	}

	public static String dmdbGetFUMODDContentType()
	{
		String pContent = null;
		try
		{
			pContent = (String) dmdbRead(E2P_SYNCML_FUMO_DD_CONTENTTYPE, pContent);
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_DB, e.toString());
		}

		return pContent;
	}

	public static String dmdbGetFUMODDAcceptType()
	{
		String pAccept = null;
		try
		{
			pAccept = (String) dmdbRead(E2P_SYNCML_FUMO_DD_ACCEPTTYPE, pAccept);
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_DB, e.toString());
		}

		return pAccept;
	}

	// ////////////////////////////////////////////////////////////////////////////////////////////////////////
	public static int dmdbGetFUMOUpdateMechanism()
	{
		int nMechanism = 0;
		try
		{
			Object temp = dmdbRead(E2P_SYNCML_FUMO_UPDATE_MECHANISM, nMechanism);
			if (temp != null)
			{
				nMechanism = (Integer) temp;
			}
			else
			{
				nMechanism = 0;
			}

			if ((nMechanism == DM_FUMO_MECHANISM_NONE) || (nMechanism >= DM_FUMO_MECHANISM_END))
			{
				return DM_FUMO_MECHANISM_NONE;
			}
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_DB, e.toString());
		}

		return nMechanism;
	}

	public static void dmdbSetFUMOUpdateMechanism(int status)
	{
		try
		{
			dmdbWrite(E2P_SYNCML_FUMO_UPDATE_MECHANISM, String.valueOf(status));
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
		}
	}

	public static void dmdbSetFUMODownloadMode(Boolean nDownloadMode)
	{
		try
		{
			dmdbWrite(E2P_SYNCML_FUMO_DOWNLOAD_MODE, String.valueOf(nDownloadMode));
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
		}
	}

	public static Boolean dmdbGetUpdateWait()
	{
		Boolean bUpdateWait = false;
		try
		{
			// defect_110923
			Object oStatus = dmdbRead(E2P_SYNCML_FUMO_UPDATE_WAIT, bUpdateWait);
			if (oStatus != null)
				bUpdateWait = (Boolean) oStatus;
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_DB, e.toString());
		}
		tsLib.debugPrint(DEBUG_DB, "bUpdateWait " + String.valueOf(bUpdateWait));
		return bUpdateWait;
	}

	public static void dmdbSetUpdateWait(Boolean bUpdateWait)
	{
		tsLib.debugPrint(DEBUG_DB, "bUpdateWait " + String.valueOf(bUpdateWait));

		try
		{
			dmdbWrite(E2P_SYNCML_FUMO_UPDATE_WAIT, String.valueOf(bUpdateWait));
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
		}
	}

	public static Boolean dmdbGetFUMODownloadMode()
	{		
		Boolean nDownloadMode = false;		
		try
		{
			// defect_110921
			Object oStatus = dmdbRead(E2P_SYNCML_FUMO_DOWNLOAD_MODE, nDownloadMode);
			if (oStatus != null)
				nDownloadMode = (Boolean)oStatus;
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_DB, e.toString());
		}

		return nDownloadMode;
	}

	public static void dmdbSetFUMOResultCode(String pResultCode)
	{
		try
		{
			dmdbWrite(E2P_SYNCML_FUMO_RESULT_CODE, pResultCode);
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
		}
	}

	public static String dmdbGetFUMOResultCode()
	{
		String pResultCode = "";
		try
		{
			pResultCode = (String) dmdbRead(E2P_SYNCML_FUMO_RESULT_CODE, pResultCode);
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_DB, e.toString());
		}
		return pResultCode;
	}

	public static boolean dmdbSetFUMOServerUrl(int appId, String pURL)
	{
		char[] aServerAddr = new char[MAX_URL_LENGTH];
		// ADD : Improve URL Port Adding
		char[] aTempServerAddr = null;
		char[] aProtocol = new char[MAX_PROTOCOL_LENGTH];
		char[] aTempPort = new char[MAX_PORT_LENGTH];

		int nPort = 0;
		boolean ret = false;
		tsDBURLParser getParser;

		tsLib.debugPrint(DEBUG_DB, " pURL: " + pURL);

		if(pURL.equals("Alert garage")){
			tsLib.debugPrint(DEBUG_DB, " Alert garage: ");
			tsMsgEvent.SetMsgEvent(null, DL_EVENT_UI_ALERT_GARAGE);
			return false;
		}
		else {
			try {

				getParser = tsDB.dbURLParser(pURL);
				if (getParser == null) {
					// wrong URL.
					return ret;
				}

				nPort = getParser.nPort;
				pURL = getParser.pURL;
				aServerAddr = getParser.pAddress.toCharArray();
				aProtocol = getParser.pProtocol.toCharArray();
				tsLib.debugPrint(DEBUG_DB, "nPort = [" + nPort + "] aServerAddr = [" + String.valueOf(aServerAddr) + "] aProtocol = [" + String.valueOf(aProtocol) + "]");

				tsLib.debugPrint(DEBUG_DB, "aProtocol = [" + getParser.pProtocol + "] " + "pURL = [" + pURL + "] aServerAddr = [" + getParser.pAddress + "] nPort = [" + getParser.nPort + "]");

				// ADD : Improve URL Port Adding
				aTempPort = String.valueOf(nPort).toCharArray();

				char[] szurl = new char[pURL.length()];
				szurl = pURL.toCharArray();
				tsLib.debugPrint(DEBUG_DB, String.valueOf(szurl));

				aTempServerAddr = tsDB.dbDoDMBootStrapURI(aTempServerAddr, szurl, aTempPort);

				if (aTempServerAddr == null)
					return ret;

				getParser = tsDB.dbURLParser(String.valueOf(aTempServerAddr));

				if (getParser.pAddress.hashCode() == 0) {
					// wrong URL.
					tsLib.debugPrintException(DEBUG_EXCEPTION, "Parsing Error");
					return ret;
				}

				nPort = getParser.nPort;
				pURL = getParser.pURL;
				aTempServerAddr = getParser.pAddress.toCharArray();
				aProtocol = getParser.pProtocol.toCharArray();

				tsLib.debugPrint(DEBUG_DB, "aProtocol = [" + getParser.pProtocol + "] " + "pURL = [" + pURL + "] aServerAddr = [" + getParser.pAddress + "] nPort = [" + getParser.nPort + "]");

				try {
					dmdbWrite(E2P_SYNCML_FUMO_PROTOCOL, String.valueOf(aProtocol));
					dmdbWrite(E2P_SYNCML_FUMO_SERVERURL, pURL);
					dmdbWrite(E2P_SYNCML_FUMO_SERVERIP, String.valueOf(aServerAddr));
					dmdbWrite(E2P_SYNCML_FUMO_SERVERPORT, String.valueOf(nPort));
				} catch (Exception e) {
					tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
				}

				return true;
			} catch (Exception ex) {
				tsLib.debugPrintException(DEBUG_DB, ex.toString());
				return false;
			}
		}
	}

	public static void dmdbSetFUMOUpdateReportURI(String pReportURI)
	{
		try
		{
			dmdbWrite(E2P_SYNCML_FUMO_REPORT_URI, pReportURI);
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
		}
	}

	public static String dmdbGetFUMOUpdateReportURI()
	{
		String pReportURI = null;
		try
		{
			pReportURI = (String) dmdbRead(E2P_SYNCML_FUMO_REPORT_URI, pReportURI);
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_DB, e.toString());
		}
		return pReportURI;
	}

	public static String dmdbGetStatusAddrFUMO(String pURL)
	{
		String pReportURI = null;
		try
		{
			pReportURI = (String) dmdbRead(E2P_SYNCML_FUMO_STATUSNOTIFYURL, pURL);
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_DB, e.toString());
		}
		return pReportURI;
	}

	public static String dmdbGetDownloadAddrFUMO(String pURL)
	{
		String pReportURI = null;
		try
		{
			pReportURI = (String) dmdbRead(E2P_SYNCML_FUMO_OBJECTDOWNLOADURL, pURL);
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_DB, e.toString());
		}
		return pReportURI;
	}

	public static int dmdbGetObjectSizeFUMO()
	{
		int nSize = 0;
		try
		{
			// defect_110921
			Object oStatus = dmdbRead(E2P_SYNCML_FUMO_OBJECTSIZE, nSize);
			if (oStatus != null)
				nSize = ((Integer) oStatus).intValue();			
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_DB, e.toString());
		}
		return nSize;
	}

	public static void dmdbSetWriteObjectSizeFUMO(int nSize)
	{
		RandomAccessFile f = null;
		File file = new File(DM_FS_FFFS_FOTA_SIZE_FILE);
		try
		{
			f = new RandomAccessFile(file, "rw");
			f.writeInt(nSize);
		}
		catch (FileNotFoundException e)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
		}
		catch (IOException e)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
		}
		finally
		{
			try
			{
				if (f != null)
				{
					f.close();
				}
			}
			catch (IOException e)
			{
				tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
			}
		}
	}

	public static tsDBFumoInfo dmdbGetObjectFUMO()
	{
		tsDBFumoInfo ptObjFUMO = new tsDBFumoInfo();

		try
		{
			ptObjFUMO = (tsDBFumoInfo) dmdbRead(E2P_SYNCML_FUMO_INFO, ptObjFUMO);
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_DB, e.toString());
		}
		return ptObjFUMO;
	}

	public static void dmdbSetObjectFUMO(Object ptObj)
	{
		tsDBFumoInfo ptObjFUMO = (tsDBFumoInfo) ptObj;

		try
		{
			dmdbWrite(E2P_SYNCML_FUMO_INFO, ptObjFUMO);
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_DB, e.toString());
		}
	}

	public static void dmdbSetPostponeCount(int nCount)
	{
		try
		{
			dmdbWrite(E2P_SYNCML_FOTA_POSTPONE_COUNT, nCount);
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
		}
	}

	public static int dmdbGetPostponeCount()
	{
		int nCount = 0;
		try
		{
			// defect_110921
			Object oStatus = dmdbRead(E2P_SYNCML_FOTA_POSTPONE_COUNT, nCount);
			if (oStatus != null)			
				nCount = ((Integer) oStatus).intValue();
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_DB, e.toString());
		}
		return nCount;
	}
	
	public static void dmdbSetDownloadPostponeFlag(boolean nFlag)
	{
		try
		{
			dmdbWrite(E2P_SYNCML_FOTA_POSTPONE_DOWNLOAD, nFlag);
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
		}
	}

	public static boolean dmdbGetDownloadPostponeFlag()
	{
		boolean bFlag = false;
		try
		{
			// defect_110921
			Object oStatus = dmdbRead(E2P_SYNCML_FOTA_POSTPONE_DOWNLOAD, bFlag);
			if (oStatus != null)
				bFlag = ((Boolean) oStatus).booleanValue();
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_DB, e.toString());
		}
		tsLib.debugPrint(DEBUG_DM, "Download Postpone Flag : " + bFlag);
		return bFlag;
	}

	public static int dmdbSetActiveProfileIndexByServerID(String inputServerId)
	{
		String serverId = null;
		int nProfileIdx = 0;

		tsLib.debugPrint(DEBUG_DB, "serverId = " + inputServerId);
		try
		{
			// defect_110921
			Object oStatus = dmdbRead(E2P_SYNCML_DM_PROFILE_INDEX, nProfileIdx);
			if (oStatus != null)			
				nProfileIdx = ((Integer) oStatus).intValue();
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
		}
		
		if (tsLib.isEmpty(inputServerId))
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, "ServerID is NULL");
			return nProfileIdx;
		}

		serverId = (String) dmdbRead(E2P_SYNCML_DM_SERVERID, serverId);
		if (inputServerId.equals(serverId)) // defect_110923
		{
			return nProfileIdx;
		}

		for (int i = 0; i < DM_SETTING_PROFILE_NUM; i++)
		{
			tsdmInfo dmInfo = (tsdmInfo) tsdmDBsql.dmsqlRead(dmSqlDbIdProfileInfo1 + i);

			if (dmInfo != null)
			{
				if (dmInfo.ServerID.equals(inputServerId))
				{
					dmdbSetProfileIndex(i);
					nProfileIdx = i;
					break;
				}
			}
		}

		return nProfileIdx;
	}
	
	public static Boolean dmdbSetNotiReSyncMode(int nMode)
	{
		try
		{
			dmdbWrite(E2P_SYNCML_DM_NOTI_NOTI_RESYNC_MODE, String.valueOf(nMode));
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
		}
		return true;
	}

	public static int dbGetNotiReSyncMode()
	{
		int nMode = 0;
		try
		{
			// defect_110921
			Object oStatus = dmdbRead(E2P_SYNCML_DM_NOTI_NOTI_RESYNC_MODE, nMode);
			if (oStatus != null)			
				nMode = ((Integer) oStatus).intValue();
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_DB, e.toString());
		}
		return nMode;
	}

	public static void dmdbBackUpServerUrl()
	{
		tsdmInfo pNvInfo = null;

		tsLib.debugPrint(DEBUG_DB, "");

		pNvInfo = (tsdmInfo) dmdbRead(E2P_SYNCML_DM_INFO, pNvInfo);
		if (pNvInfo == null)
		{
			tsLib.debugPrint(DEBUG_DB, "pNvInfo is NULL");
			return;
		}
		dmdbSetServerUrl(pNvInfo.ServerUrl_Org);
		dmdbSetServerAddress(pNvInfo.ServerIP_Org);
		dmdbSetServerPort(pNvInfo.ServerPort_Org);
		dmdbSetServerProtocol(pNvInfo.Protocol_Org);
		dmdbSetChangedProtocol(false);
	}

	public static String dmdbGetIMSIFromFFS()
	{
		String pIMSI = null;
		try
		{
			pIMSI = (String) dmdbRead(E2P_SYNCML_SIM_IMSI, pIMSI);
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_DB, e.toString());
		}
		return pIMSI;
	}

	public static boolean dmdbSetIMSIToFFS(String pIMSI)
	{
		if (pIMSI == null)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, "pIMSI is NULL");
			return false;
		}
		else
		{
			try
			{
				dmdbWrite(E2P_SYNCML_SIM_IMSI, pIMSI);
			}
			catch (Exception e)
			{
				tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
			}
		}

		return true;
	}

	public static String dmdbGetNotiDigest(int nAppId, String pServerID, int nAuthType, byte[] pPacketBody, int nBodyLen)
	{
		/* For Notification Digest */
		String szServerNonce = "";
		String szServerId = "";
		String szServerPwd = "";
		String pDigest = null;

		int nNonceLen = 0;

		int nActive = dmdbSetActiveProfileIndexByServerID(pServerID);
		try
		{
			szServerId = dmdbGetServerID();
			szServerPwd = dmdbGetServerPassword();
			szServerNonce = dmdbGetServerNonce();
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
		}

		if (tsLib.isEmpty(szServerNonce))
		{
			return null;
		}
		
		tsLib.debugPrint(DEBUG_DB, "nActive = " + nActive);
		tsLib.debugPrint(DEBUG_DB, "szServerId = " + szServerId);
		tsLib.debugPrint(DEBUG_DB, "szServerPwd = " + szServerPwd);
		tsLib.debugPrint(DEBUG_DB, "szServerNonce = " + szServerNonce);

		byte[] pNonce = new byte[szServerNonce.length()];
		pNonce = base64.decode(szServerNonce.getBytes());

		// defect_110921
		if (pNonce == null)
		{
			return null;
		}

		nNonceLen = pNonce.length;
		pDigest = Auth.authMakeDigest(nAuthType, szServerId, szServerPwd, pNonce, nNonceLen, pPacketBody, nBodyLen, szServerId);
		return pDigest;		
	}

	public static void dmdbClearUicResultKeepFlag()
	{
		int eUIcKeepFlag = UIC_SAVE_NONE;

		try
		{
			tsdmDB.dmdbWrite(E2P_SYNCML_DM_UIC_RESULT_KEEP_FLAG, String.valueOf(eUIcKeepFlag));
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
		}
		return;

	}

	public static void dmdbSetDmAgentType(int nAgentType)
	{
		tsLib.debugPrint(DEBUG_DB, "AgentType=" + nAgentType);
		try
		{
			dmdbWrite(E2P_SYNCML_DM_AGENT_INFO_AGENT_TYPE, (Integer) nAgentType);
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_DB, e.toString());
		}
	}

	public static int dmdbGetDmAgentType()
	{
		int nRet = SYNCML_DM_AGENT_DM; // Default Agent Type
		try
		{
			Object oStatus = dmdbRead(E2P_SYNCML_DM_AGENT_INFO_AGENT_TYPE, null);
			if (oStatus != null)			
				nRet = ((Integer) oStatus).intValue();
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_DB, e.toString());
		}
		return nRet;
	}

	public static boolean dmdbSetCurrCheckTime(long lcurrTime)
	{
		boolean nRet = false;
		try
		{
			tsLib.debugPrint(DEBUG_DB, "lcurrTime is " + lcurrTime);
			nRet = dmdbWrite(E2P_SYNCML_DM_CURR_CHECK_TIME, lcurrTime);
		}
		catch (Exception e) 
		{
			tsLib.debugPrintException(DEBUG_DB, e.toString());
		}
		return nRet;
	}

}
