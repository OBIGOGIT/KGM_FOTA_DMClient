package com.tsdm.agent;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.Calendar;

import com.tsdm.auth.Auth;
import com.tsdm.auth.base64;
import com.tsdm.db.tsDB;
import com.tsdm.db.tsDBURLParser;
import com.tsdm.db.tsdmDB;
import com.tsdm.db.tsdmDBadapter;
import com.tsdm.db.tsdmInfo;
import com.tsdm.parser.ddfParser;
import com.tsdm.adapt.tsDefIne;
import com.tsdm.adapt.tsDefineIdle;
import com.tsdm.adapt.tsDmAccXNode;
import com.tsdm.adapt.tsDmHmacData;
import com.tsdm.adapt.tsLinkedList;
import com.tsdm.adapt.tsList;
import com.tsdm.adapt.tsDmVnode;
import com.tsdm.adapt.tsDmWorkspace;
import com.tsdm.adapt.tsDmEncoder;
import com.tsdm.adapt.tsDmHandlecmd;
import com.tsdm.adapt.tsLib;
import com.tsdm.adapt.tsDmMsg;
import com.tsdm.adapt.tsOmAcl;
import com.tsdm.adapt.tsOmList;
import com.tsdm.adapt.tsOmTree;
import com.tsdm.adapt.tsOmVfs;
import com.tsdm.adapt.tsOmlib;
import com.tsdm.adapt.tsDmParser;
import com.tsdm.adapt.tsDmParserAdd;
import com.tsdm.adapt.tsDmParserAlert;
import com.tsdm.adapt.tsDmParserAtomic;
import com.tsdm.adapt.tsDmParserCopy;
import com.tsdm.adapt.tsDmParserCred;
import com.tsdm.adapt.tsDmParserDelete;
import com.tsdm.adapt.tsDmParserExec;
import com.tsdm.adapt.tsDmParserGet;
import com.tsdm.adapt.tsDmParserItem;
import com.tsdm.adapt.tsDmParserPcdata;
import com.tsdm.adapt.tsDmParserReplace;
import com.tsdm.adapt.tsDmParserResults;
import com.tsdm.adapt.tsDmParserSequence;
import com.tsdm.adapt.tsdmParserStatus;
import com.tsdm.adapt.tsDmParserSyncheader;
import com.tsdm.adapt.tsDmUic;
import com.tsdm.adapt.tsDmWbxmlencoder;
import com.tsdm.net.netHttpAdapter;
import com.tsdm.net.netDefine;
import com.tsdm.net.netTimerConnect;
import com.tsdm.net.netTimerReceive;
import com.tsdm.net.netTimerSend;

public class dmAgent implements dmDefineDevInfo, dmDefineMsg, tsDefineIdle, tsDefIne, netDefine {
	private final static String 	DM_DEFAULT_NONCE = "MTIzNA==";
	private final int				PACKAGE_SIZE_GAP		= 128;
	public String					cmd;
	public boolean					inProgresscmd;
	public tsDmParserSyncheader header;
	public tsdmParserStatus status;
	public tsDmParserGet get;
	public tsDmParserExec exec;
	public tsDmParserAlert alert;
	public tsDmParserAdd addCmd;
	public tsDmParserReplace replaceCmd;
	public tsDmParserDelete deleteCmd;
	public tsDmParserCopy copyCmd;
	public tsDmParserAtomic atomic;
	public tsDmParserSequence sequence;
	public static tsDmWorkspace dm_ws;
	public static tsDmAccXNode dm_AccXNodeInfo;
	public static tsDmAccXNode dm_AccXNodeTndsInfo		= null;

	public static String			g_AccName;
	public static String			pAccName;
	public static int				inDMSync				= DM_SYNC_NONE;

	public dmAgentHandler agenthandler			= null;
	public netHttpAdapter gHttpDMAdapter;
	private static boolean			nPendingStatus			= false;
	public static boolean			bUserInitiatedStatus	= false;
	public static boolean			bServerInitiatedStatus	= false;

	private static int				ConnectRetryCount		= 0;

	public dmAgent()
	{
		if (gHttpDMAdapter == null)
		{
			gHttpDMAdapter = new netHttpAdapter();
		}
	}

	public static int dmAgentInitParser(tsDmWorkspace ws, tsDmParser p)
	{
		p.dmParseInit(p, ws);
		return SDM_RET_OK;
	}

	public tsDmWorkspace dmAgentGetWorkSpace()
	{
		if (dm_ws == null)
		{
			tsLib.debugPrint(DEBUG_DM, "dm_ws is NULL");
			return null;
		}
		return dm_ws;
	}

	public static int dmAgentGetSyncMode()
	{
		int nSync = DM_SYNC_NONE;

		nSync = inDMSync;
		if (nSync != DM_SYNC_NONE)
			tsLib.debugPrint(DEBUG_DM, "nSync = " + nSync);

		return nSync;
	}

	public static boolean dmAgentSetSyncMode(int nSync)
	{
		tsLib.debugPrint(DEBUG_DM, "nSync = " + nSync);
		inDMSync = nSync;

		return true;
	}


	public static boolean dmAgentSetUserInitiatedStatus(boolean bState)
	{
		bUserInitiatedStatus = bState;
		tsLib.debugPrint(DEBUG_DM, "Set bUserInitiatedStatus = " + bUserInitiatedStatus);
		return true;
	}
	public static void dmAgentSetServerInitiatedStatus(boolean bState)
	{
		bServerInitiatedStatus = bState;
		tsLib.debugPrint(DEBUG_DM, "Set bServerInitiatedStatus = " + bServerInitiatedStatus);
	}

	public boolean dmAgentIsAccessibleNode(String path)
	{
		String pInbox = null;

		if (_SYNCML_TS_DM_VERSION_V11_)
		{
			if (tsLib.libStrstr(path, SYNCML_DMACC_CLIENTPW_PATH) != null)
			{
				return false;
			}

			if (tsLib.libStrstr(path, SYNCML_DMACC_SERVERPW_PATH) != null)
			{
				return false;
			}

			if (tsLib.libStrstr(path, SYNCML_DMACC_CLIENTNONCE_PATH) != null)
			{
				return false;
			}

			if (tsLib.libStrstr(path, SYNCML_DMACC_SERVERNONCE_PATH) != null)
			{
				return false;
			}
		}
		else if (_SYNCML_TS_DM_VERSION_V12_)
		{
			if (tsLib.libStrstr(path, SYNCML_APPAUTH_AAUTHSECRET_PATH) != null)
			{
				return false;
			}

			if (tsLib.libStrstr(path, SYNCML_APPAUTH_AAUTHDATA_PATH) != null)
			{
				return false;
			}

			pInbox = ddfParser.dmDDFGetMOPath(ddfParser.DM_MO_ID_INBOX);
			if (pInbox == null)
			{
				return true;
			}

			if (tsLib.libStrncmp(path, pInbox, pInbox.length()) == 0)
			{
				return false;
			}
		}
		return true;
	}

	public boolean dmAgentIsPermanentNode(tsOmTree om, String path)
	{
		tsDmVnode node;

		node = tsOmlib.dmOmLibGetNodeProp(om, path);

		if (node != null)
		{
			if (node.scope == SCOPE_PERMANENT)
			{
				return true;
			}
		}
		return false;
	}

	public static int dmAgentInit()
	{
		dm_ws = new tsDmWorkspace();
		//if (dm_ws == null)
		//	return SDM_RET_FAILED;

		g_AccName = BASE_ACCOUNT_PATH_1_1;

		if (_SYNCML_TS_DM_VERSION_V12_)
			dm_AccXNodeInfo = new tsDmAccXNode();

		return SDM_RET_OK;
	}

	public static int dmAgentClose()
	{
		tsDmWorkspace ws = dm_ws;

		tsLib.debugPrint(DEBUG_DM, "inDMSync = " + inDMSync);
		if (inDMSync > 0)
		{
			if (ws != null)
			{
				if (nPendingStatus)
				{
					tsLib.debugPrint(DEBUG_DM, "Pending Status don't save");
					tsOmlib.dmOmvfsEnd(ws.om.vfs);
				}
				else
				{
					tsLib.debugPrint(DEBUG_DM, "workspace save");
					tsOmlib.dmOmEnd(ws.om);
				}
				ws.wsDmFreeWorkSpace();
				dm_ws = null;

			}
			g_AccName = null;
			dmAgentSetSyncMode(DM_SYNC_NONE);
		}
		return SDM_RET_OK;
	}

	public static int dmAgentParsingWbxml(byte[] buf)
	{
		tsDmWorkspace ws = dm_ws;
		int res = 0;

		ws.nextMsg = false;
		ws.endOfMsg = false;
		tsDmParser p = new tsDmParser(buf);
		dmAgentInitParser(ws, p);
		res = p.dmParse();
		if (res != SDM_RET_OK)
		{
			return SDM_RET_PARSE_ERROR;
		}
		return SDM_RET_OK;
	}

	public int dmAgentVerifyServerAuth(tsDmParserSyncheader syncHeader)
	{
		tsDmWorkspace ws = dm_ws;
		String key = null;
		tsDmParserCred cred = syncHeader.cred;
		int ret = AUTH_STATE_OK;
		tsDmHmacData pHMAC = ws.recvHmacData;

		tsLib.debugPrint(DEBUG_DM, "");

		if (ws.serverID == null)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, "!ws.serverID");
			if (ws.serverAuthState == AUTH_STATE_RETRY || ws.serverAuthState == AUTH_STATE_REQUIRED)
			{
				ret = AUTH_STATE_FAIL;
			}
			else
			{
				ret = AUTH_STATE_REQUIRED;
			}
			return ret;
		}
		if (ws.serverCredType == CRED_TYPE_HMAC)
		{
			if (pHMAC == null)
			{
				tsLib.debugPrint(DEBUG_DM, "Check the HMAC DATA !pHMAC");
				tsLib.debugPrint(DEBUG_DM, "ServerAuth :" + ws.serverAuthState);
				ws.serverAuthState = AUTH_STATE_NO_CRED;
				if (ws.serverAuthState == AUTH_STATE_RETRY || ws.serverAuthState == AUTH_STATE_REQUIRED)
				{
					return AUTH_STATE_FAIL;
				}
				else
				{
					return AUTH_STATE_REQUIRED;
				}
			}

			tsLib.debugPrint(DEBUG_DM, "algorighm" + pHMAC.hmacAlgorithm);
			tsLib.debugPrint(DEBUG_DM, "name" + pHMAC.hmacUserName);
			tsLib.debugPrint(DEBUG_DM, "name" + pHMAC.hamcDigest);

			if ((pHMAC.hmacAlgorithm == null) || (pHMAC.hmacUserName == null) || (pHMAC.hamcDigest == null))
			{
				ws.serverAuthState = AUTH_STATE_NO_CRED;
				tsLib.debugPrint(DEBUG_DM, "Check the HMAC DATA");
				tsLib.debugPrint(DEBUG_DM, "ServerAuth :" + ws.serverAuthState);
				return AUTH_STATE_FAIL;
			}
			else if (pHMAC.hmacAlgorithm.compareTo("MD5") == 0)
			{
				ws.serverAuthState = AUTH_STATE_NO_CRED;
				tsLib.debugPrint(DEBUG_DM, "State No Credential");
				tsLib.debugPrint(DEBUG_DM, "ServerAuth :" + ws.serverAuthState);
				return AUTH_STATE_FAIL;
			}

			tsLib.debugPrint(DEBUG_DM, "ID:" + ws.serverID + ", PASS:" + ws.serverPW);
			tsLib.debugPrint(DEBUG_DM, "credtype:" + ws.serverCredType + ", nextNonce:" + ws.serverNextNonce);
			tsLib.debugPrint(DEBUG_DM, "httpContentLength:" + pHMAC.httpContentLength);

			key = Auth.authMakeDigest(ws.serverCredType, ws.serverID, ws.serverPW, ws.serverNextNonce, ws.serverNextNonce.length, ws.buf.toByteArray(), pHMAC.httpContentLength, ws.serverID);

			if (key == null)
			{
				ws.serverAuthState = AUTH_STATE_REQUIRED;
				tsLib.debugPrint(DEBUG_DM, "Check the HMAC key");
				tsLib.debugPrint(DEBUG_DM, "ServerAuth :" + ws.serverAuthState);
				return AUTH_STATE_FAIL;
			}

			if (key.compareTo(pHMAC.hamcDigest) != 0)
			{
				ws.serverAuthState = AUTH_STATE_REQUIRED;
				tsLib.debugPrint(DEBUG_DM, "key and pHMAC.hamcDigest not equal");
				tsLib.debugPrint(DEBUG_DM, "ServerAuth :" + ws.serverAuthState);
				return AUTH_STATE_FAIL;
			}
			ret = AUTH_STATE_OK;
		}
		else
		{
			if (cred.meta == null)
			{
				ws.serverAuthState = AUTH_STATE_NO_CRED;
				if (ws.serverAuthState == AUTH_STATE_RETRY || ws.serverAuthState == AUTH_STATE_REQUIRED)
				{
					ret = AUTH_STATE_FAIL;
				}
				else
				{
					ret = AUTH_STATE_REQUIRED;
				}
				return ret;
			}
			else if (cred.meta != null)
			{
				if (Auth.authCredString2Type(cred.meta.type) == ws.serverCredType)
				{
					if (cred.meta.type.compareTo(CRED_TYPE_STRING_MD5) == 0)
					{
						tsLib.debugPrint(DEBUG_DM, "CRED_TYPE_STRING_MD5 ws.serverCredType : " + ws.serverCredType);
						tsLib.debugPrint(DEBUG_DM, "CRED_TYPE_STRING_MD5 ws.serverID : " + ws.serverID);
						tsLib.debugPrint(DEBUG_DM, "CRED_TYPE_STRING_MD5 ws.serverPW : " + ws.serverPW);
						tsLib.debugPrint(DEBUG_DM, "CRED_TYPE_STRING_MD5 ws.serverCredType : " + ws.serverCredType);
						key = Auth.authMakeDigest(ws.serverCredType, ws.serverID, ws.serverPW, ws.serverNextNonce, ws.serverNextNonce.length, null, 0, ws.serverID);

						if (key == null)
							ret = AUTH_STATE_FAIL;
						else
						{
							if (key.compareTo(cred.data) != 0)
							{
								tsLib.debugPrint(DEBUG_DM, "key.compareTo(cred.data) != 0 key= " + key + " cred.data= " + cred.data);
								ws.serverAuthState = AUTH_STATE_REQUIRED;
								if (ws.serverAuthState == AUTH_STATE_RETRY || ws.serverAuthState == AUTH_STATE_REQUIRED)
								{
									ret = AUTH_STATE_FAIL;
								}
								else
								{
									ret = AUTH_STATE_REQUIRED;
								}
								return ret;
							}
							else
							{
								ret = AUTH_STATE_OK;
							}
						}
					}
					else if (cred.meta.type.compareTo(CRED_TYPE_STRING_BASIC) == 0)
					{
						key = Auth.authMakeDigest(ws.serverCredType, ws.serverID, ws.serverPW, "".getBytes(), 0, null, 0, ws.serverID);

						if (key == null)
							ret = AUTH_STATE_FAIL;
						else
						{
							if (key.compareTo(cred.data) != 0)
							{
								ws.serverAuthState = AUTH_STATE_REQUIRED;
								if (ws.serverAuthState == AUTH_STATE_RETRY || ws.serverAuthState == AUTH_STATE_REQUIRED)
								{
									ret = AUTH_STATE_FAIL;
								}
								else
								{
									ret = AUTH_STATE_REQUIRED;
								}
								return ret;
							}
							else
							{
								ret = AUTH_STATE_OK;
							}
						}
					}
				}
			}
		}

		return ret;
	}

	public int dmAgentSendPackage()
	{
		String mac;
		String pHmacData = null;
		tsDmWorkspace ws = dm_ws;
		int ret = SDM_RET_OK;

		if (ws.credType == CRED_TYPE_BASIC || ws.credType == CRED_TYPE_MD5)
		{
			try
			{
				ret = gHttpDMAdapter.tpSetHttpObj(ws.targetURI, null, null, HTTP_METHOD_POST, SYNCMLDM, false);
			}
			catch (NullPointerException e)
			{
				tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
				netTimerSend.endTimer();
				ret = TP_RET_SEND_FAIL;
				tsDmMsg.taskSendMessage(TASK_MSG_DM_SYNCML_SENDFAIL, null, null);
				return ret;
			}

			if (ret == TP_RET_CHANGED_PROFILE)
			{
				nPendingStatus = true;
				ret = SDM_RET_CHANGED_PROFILE;
			}
			else
			{
				if (nPendingStatus)
				{
					nPendingStatus = false;
				}

				try
				{
					ret = gHttpDMAdapter.tpSendData(ws.buf.toByteArray(), ws.buf.size(), SYNCMLDM);
				}
				catch (SocketTimeoutException e)
				{
					tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
					netTimerSend.endTimer();
					ret = TP_RET_SEND_FAIL;
				}
				if (ret == TP_RET_OK)
				{
					tsDmMsg.taskSendMessage(TASK_MSG_DM_SYNCML_CONTINUE, null, null);
				}
				else if (ret == TP_RET_CONNECTION_FAIL)
				{
					tsDmMsg.taskSendMessage(TASK_MSG_DM_SYNCML_CONNECTFAIL, null, null);
				}
				else
				// SEND_FAIL
				{
					tsDmMsg.taskSendMessage(TASK_MSG_DM_SYNCML_SENDFAIL, null, null);
				}
			}
		}
		else
		{
			int len = tsDmWbxmlencoder.dmWbxEncGetBufferSize();
			mac = Auth.authMakeDigest(ws.credType, ws.userName, ws.clientPW, ws.nextNonce, ws.nextNonce.length, ws.buf.toByteArray(), len, ws.serverID);

			pHmacData = "algorithm=MD5, username=" + "\"" + ws.userName + "\"" + ", mac=" + mac;

			try
			{
				ret = gHttpDMAdapter.tpSetHttpObj(ws.targetURI, pHmacData, null, HTTP_METHOD_POST, SYNCMLDM, false);
			}
			catch (NullPointerException e)
			{
				tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
				netTimerSend.endTimer();
				ret = TP_RET_SEND_FAIL;
				tsDmMsg.taskSendMessage(TASK_MSG_DM_SYNCML_SENDFAIL, null, null);
				return ret;
			}

			if (ret == TP_RET_CHANGED_PROFILE)
			{
				nPendingStatus = true;
				ret = SDM_RET_CHANGED_PROFILE;
			}
			else
			{
				if (nPendingStatus)
				{
					nPendingStatus = false;
				}

				try
				{
					ret = gHttpDMAdapter.tpSendData(ws.buf.toByteArray(), tsDmEncoder.dmEncGetBufferSize(ws.e), SYNCMLDM);
				}
				catch (SocketTimeoutException e)
				{
					tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
					netTimerSend.endTimer();
					ret = TP_RET_SEND_FAIL;
				}
				if (ret == TP_RET_OK)
				{
					tsDmMsg.taskSendMessage(TASK_MSG_DM_SYNCML_CONTINUE, null, null);
				}
				else if (ret == TP_RET_CONNECTION_FAIL)
				{
					tsDmMsg.taskSendMessage(TASK_MSG_DM_SYNCML_CONNECTFAIL, null, null);
				}
				else
				// SEND_FAIL
				{
					tsDmMsg.taskSendMessage(TASK_MSG_DM_SYNCML_SENDFAIL, null, null);
				}
			}
		}
		return ret;
	}

	public String dmMakeSessionID()
	{
		Calendar data = Calendar.getInstance();
		String sessionid;
		int second;

		String nowdata = String.valueOf(data.get(Calendar.MINUTE));
		second = data.get(Calendar.SECOND);

		sessionid = String.format("%x%x", Integer.valueOf(nowdata), Integer.valueOf(second));
		tsLib.debugPrint(DEBUG_DM, "sessionid =" + sessionid);

		return sessionid;
	}

	public int dmAgentStartSession()
	{
		int ret = SDM_RET_OK;
		int nNotiEvent = 0;
		tsDmWorkspace ws;

		tsLib.debugPrint(DEBUG_DM, "");
		dmAgentInit();
		//if (dmAgentInit() != SDM_RET_OK)
		//{
		//	return SDM_RET_FAILED;
		//}

		ws = dm_ws;
		nNotiEvent = tsDB.dbGetNotiEvent(ws.appId);
		if (nNotiEvent > 0 && !nPendingStatus)
		{
			ws.sessionID = tsDB.dbGetNotiSessionID(ws.appId);
		}
		else
		{
			ws.sessionID = dmMakeSessionID();
		}
		if (dmAgentMakeNode() != SDM_RET_OK)
		{
			ret = SDM_RET_FAILED;
		}

		return ret;
	}

	public int dmAgentMakeAppNode()
	{
		return SDM_RET_OK;
	}

	public int dmAgentMakeNode()
	{
		tsDmWorkspace ws = dm_ws;
		tsOmTree om = ws.om;

		if (tsOmlib.dmOmInit(om) != SDM_RET_OK)
		{
			return -1;
		}

		tsOmlib.dmOmSetServerId(om, "*");

		dmAgentMakeSyncMLNode();
		dmAgentMakeDevInfoNode();
		dmAgentMakeDevDetailNode();
		if (_SYNCML_TS_FOTA_)
			dmAgentMakeFwUpdateNode();

		return SDM_RET_OK;
	}

	public int dmAgentCreatePackage()
	{
		int WBXML_CHARSET_UTF8 = 0x6a;
		String szWbxmlStrTbl;
		tsDmWorkspace ws = dm_ws;
		int nNotiEvent;
		int res = SDM_RET_OK;
		tsDmEncoder e = ws.e;

		if (ws.dmState == SyncmlState.DM_STATE_INIT)
		{
			res = dmAgentLoadWorkSpace();
			if (dlAgent.dlAgentGetClientInitFlag() != DM_NONE_INIT)
				ws.dmState = SyncmlState.DM_STATE_GENERIC_ALERT;
			else
				ws.dmState = SyncmlState.DM_STATE_CLIENT_INIT_MGMT;
		}
		else if ((ws.dmState == SyncmlState.DM_STATE_GENERIC_ALERT_REPORT) || (ws.dmState == SyncmlState.DM_STATE_ABORT_ALERT))
		{
			res = dmAgentLoadWorkSpace();
		}
		if (res != SDM_RET_OK)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, " failed");
			return SDM_RET_FAILED;
		}

		ws.buf.reset();
		e.dmEncInit(ws.buf);
		if (_SYNCML_TS_DM_VERSION_V12_)
			szWbxmlStrTbl = DM_WBXML_STRING_TABLE_1_2;
		else
			szWbxmlStrTbl = DM_WBXML_STRING_TABLE_1_1;
		e.dmEncStartSyncml(0, WBXML_CHARSET_UTF8, szWbxmlStrTbl, szWbxmlStrTbl.length());
		ws = dmBuildcmd.dmBuildCmdSyncHeader(ws);
		e.dmEncAddSyncHeader(ws.syncHeader);
		e.dmEncStartSyncbody();

		switch (ws.dmState)
		{
			case DM_STATE_CLIENT_INIT_MGMT:
				tsLib.debugPrint(DEBUG_DM, "DM_STATE_CLIENT_INIT_MGMT");
				dmAgentClientInitPackage(e);
				break;
			case DM_STATE_PROCESSING:
				tsLib.debugPrint(DEBUG_DM, "DM_STATE_PROCESSING");
				dmAgentMgmtPackage(e);
				break;
			case DM_STATE_GENERIC_ALERT:
				tsLib.debugPrint(DEBUG_DM, "DM_STATE_GENERIC_ALERT");
				res = dmAgentClientInitPackage(e);

				if (res != SDM_RET_OK)
				{
					if (res == SDM_BUFFER_SIZE_EXCEEDED)
					{
						ws.endOfMsg = false;
					}
					else
					{
						tsLib.debugPrintException(DEBUG_EXCEPTION, "failed(%d)" + res);
					}
					return SDM_RET_FAILED;
				}

				res = dmAgentCreatePackageGenericAlert(e, ALERT_GENERIC);
				if (res != SDM_RET_OK)
				{
					if (res == SDM_BUFFER_SIZE_EXCEEDED)
					{
						ws.endOfMsg = false;
					}
					else
					{
						tsLib.debugPrintException(DEBUG_EXCEPTION, "failed(%d)" + res);
					}
					return SDM_RET_FAILED;
				}
				ws.endOfMsg = true;
				break;
			case DM_STATE_GENERIC_ALERT_REPORT:
				tsLib.debugPrint(DEBUG_DM, "DM_STATE_GENERIC_ALERT_REPORT");
				res = dmAgentClientInitPackage(e);

				if (res != SDM_RET_OK)
				{
					if (res == SDM_BUFFER_SIZE_EXCEEDED)
					{
						ws.endOfMsg = false;
					}
					else
					{
						tsLib.debugPrintException(DEBUG_EXCEPTION, "failed(%d)" + res);
					}
					return SDM_RET_FAILED;
				}

				res = dmAgentCreatePackageReportGenericAlert(e, ALERT_GENERIC);
				if (res != SDM_RET_OK)
				{
					if (res == SDM_BUFFER_SIZE_EXCEEDED)
					{
						ws.endOfMsg = false;
					}
					else
					{
						tsLib.debugPrintException(DEBUG_EXCEPTION, "failed(%d)" + res);
					}
					return SDM_RET_FAILED;
				}
				ws.endOfMsg = true;
				break;
			case DM_STATE_ABORT_ALERT:
				tsLib.debugPrint(DEBUG_DM, "DM_STATE_ABORT_ALERT");
				nNotiEvent = tsDB.dbGetNotiEvent(ws.appId);
				if (nNotiEvent > 0)
				{
					res = dmAgentCreatePackageAlert(e, ALERT_SERVER_INITIATED_MGMT);
				}
				else
				{
					res = dmAgentCreatePackageAlert(e, ALERT_CLIENT_INITIATED_MGMT);
				}
				if (res != SDM_RET_OK)
				{
					if (res == SDM_BUFFER_SIZE_EXCEEDED)
					{
						ws.endOfMsg = false;
					}
					else
					{
						tsLib.debugPrintException(DEBUG_EXCEPTION, "failed(%d)" + res);
					}
					return SDM_RET_FAILED;
				}
				res = dmAgentCreatePackageAlert(e, ALERT_SESSION_ABORT);

				if (res != SDM_RET_OK)
				{
					if (res == SDM_BUFFER_SIZE_EXCEEDED)
					{
						ws.endOfMsg = false;
					}
					else
					{
						tsLib.debugPrintException(DEBUG_EXCEPTION, "failed(%d)" + res);
					}
					return SDM_RET_FAILED;
				}

				ws.endOfMsg = true;
				break;
		}

		if (ws.dataBuffered || ws.sendRemain)
		{
			ws.endOfMsg = false;
		}

		e.dmEncEndSyncbody(ws.endOfMsg);
		e.dmEncEndSyncml();

		return res;
	}

	public int dmAgentLoadWorkSpace()
	{
		if (_SYNCML_TS_DM_VERSION_V12_)
		{
			// Defects
			tsDmWorkspace ws = dm_ws;
			if (ws == null)
			{
				return SDM_RET_FAILED;
			}
			tsOmTree om = ws.om;
			byte[] dValue = null;
			String szAccBuf = "";
			char[] buf;
			tsDmVnode node = null;
			int nReSyncMode = 0;

			szAccBuf = dm_AccXNodeInfo.ClientAppAuth;
			szAccBuf = szAccBuf.concat(SYNCML_APPAUTH_AAUTHNAME_PATH);
			node = tsOmlib.dmOmLibGetNodeProp(om, szAccBuf);
			if (node == null)
			{
				return SDM_RET_FAILED;
			}

			buf = new char[(int) node.size];
			tsOmlib.dmOmRead(om, szAccBuf, 0, buf, node.size);
			ws.userName = String.valueOf(buf);

			szAccBuf = dm_AccXNodeInfo.ClientAppAuth;
			szAccBuf = szAccBuf.concat(SYNCML_APPAUTH_AAUTHSECRET_PATH);
			node = tsOmlib.dmOmLibGetNodeProp(om, szAccBuf);
			if (node == null)
			{
				return SDM_RET_FAILED;
			}
			buf = new char[(int) node.size];
			tsOmlib.dmOmRead(om, szAccBuf, 0, buf, node.size);
			ws.clientPW = String.valueOf(buf);

			szAccBuf = dm_AccXNodeInfo.ClientAppAuth;
			szAccBuf = szAccBuf.concat(SYNCML_APPAUTH_AAUTHTYPE_PATH);
			node = tsOmlib.dmOmLibGetNodeProp(om, szAccBuf);
			if (node == null)
			{
				return SDM_RET_FAILED;
			}
			buf = new char[(int) node.size];
			tsOmlib.dmOmRead(om, szAccBuf, 0, buf, node.size);

			nReSyncMode = tsdmDB.dbGetNotiReSyncMode();

			if (nReSyncMode == DM_NOTI_RESYNC_MODE_TRUE)
			{
				/* ex) "BASIC" -> int */
				ws.credType = Auth.authAAuthtring2Type(AUTH_TYPE_DIGEST);
			}
			else
			{
				/* ex) "BASIC" -> int */
				String tmp;
				tmp = String.valueOf(buf);
				ws.credType = Auth.authAAuthtring2Type(tmp);
			}

			szAccBuf = dm_AccXNodeInfo.ServerAppAuth;
			szAccBuf = szAccBuf.concat(SYNCML_APPAUTH_AAUTHTYPE_PATH);
			node = tsOmlib.dmOmLibGetNodeProp(om, szAccBuf);
			if (node == null)
			{
				return SDM_RET_FAILED;
			}
			buf = new char[(int) node.size];
			tsOmlib.dmOmRead(om, szAccBuf, 0, buf, node.size);
			nReSyncMode = tsdmDB.dbGetNotiReSyncMode();

			if (nReSyncMode == DM_NOTI_RESYNC_MODE_TRUE)
			{
				/* ex) "BASIC" -> int */
				ws.serverCredType = Auth.authAAuthtring2Type(AUTH_TYPE_DIGEST);
			}
			else
			{
				/* ex) "BASIC" -> int */
				String tmp;
				tmp = String.valueOf(buf);
				ws.serverCredType = Auth.authAAuthtring2Type(tmp);
				if (ws.serverCredType == CRED_TYPE_NONE)
				{
					ws.serverCredType = ws.credType;
				}
			}
			szAccBuf = dm_AccXNodeInfo.Account;
			szAccBuf = szAccBuf.concat(SYNCML_DMACC_SERVERID_PATH);
			node = tsOmlib.dmOmLibGetNodeProp(om, szAccBuf);
			if (node == null)
			{
				return SDM_RET_FAILED;
			}
			buf = new char[(int) node.size];
			tsOmlib.dmOmRead(om, szAccBuf, 0, buf, node.size);
			ws.serverID = String.valueOf(buf);

			szAccBuf = dm_AccXNodeInfo.ServerAppAuth;
			szAccBuf = szAccBuf.concat(SYNCML_APPAUTH_AAUTHSECRET_PATH);
			node = tsOmlib.dmOmLibGetNodeProp(om, szAccBuf);
			if (node == null)
			{

				return SDM_RET_FAILED;
			}
			buf = new char[(int) node.size];
			tsOmlib.dmOmRead(om, szAccBuf, 0, buf, node.size);
			ws.serverPW = String.valueOf(buf);

			if (nReSyncMode == DM_NOTI_RESYNC_MODE_TRUE)
			{
				ws.nextNonce[0] = 0x00;
				ws.nextNonce[1] = 0x00;
				ws.nextNonce[2] = 0x00;
				ws.nextNonce[3] = 0x00;
			}
			else
			{
				szAccBuf = dm_AccXNodeInfo.ClientAppAuth;
				szAccBuf = szAccBuf.concat(SYNCML_APPAUTH_AAUTHDATA_PATH);
				node = tsOmlib.dmOmLibGetNodeProp(om, szAccBuf);
				if (node != null)
				{
					if (node.size > 0)
					{
						buf = new char[(int) node.size];
						tsOmlib.dmOmRead(om, szAccBuf, 0, buf, node.size);
					}
					else
					{
						buf = null;
					}
					if (buf != null)
					{
						if (node.format != FORMAT_B64)
						{
							for (int i = 0; i < node.size; i++)
								ws.nextNonce[i] = (byte) buf[i];

							tsLib.debugPrint(DEBUG_DM, "node->size = " + node.size);
						}
						else
						{
							dValue = base64.decode(new String(buf).getBytes());
							if (dValue != null)
							{
								ws.nextNonce = null;
								ws.nextNonce = new byte[dValue.length];
								for (int i = 0; i < dValue.length; i++)
									ws.nextNonce[i] = dValue[i];
							}
							else
							{
								tsLib.debugPrint(DEBUG_DM, "sdmPreparePackaging : dValue is NULL");
							}
						}
					}
				}
				else
				{
					return SDM_RET_FAILED;
				}
			}

			szAccBuf = dm_AccXNodeInfo.ServerAppAuth;
			szAccBuf = szAccBuf.concat(SYNCML_APPAUTH_AAUTHDATA_PATH);
			tsLib.debugPrint(DEBUG_DM, "Server szAccBuf) :" + szAccBuf);
			node = tsOmlib.dmOmLibGetNodeProp(om, szAccBuf);
			if (node != null)
			{
				if (node.size > 0)
				{
					buf = null;
					buf = new char[(int) node.size];
					tsOmlib.dmOmRead(om, szAccBuf, 0, buf, node.size);
				}
				else
				{
					buf = null;
				}
				if (buf != null)
				{
					if (node.format != FORMAT_B64)
					{
						for (int i = 0; i < node.size; i++)
							ws.serverNextNonce[i] = (byte) buf[i];
					}
					else
					{
						tsLib.debugPrint(DEBUG_DM, "Server Next Noncenew String(buf) :" + new String(buf));
						dValue = null;
						dValue = base64.decode(new String(buf).getBytes());
						if (dValue != null)
						{
							ws.serverNextNonce = null;
							ws.serverNextNonce = new byte[dValue.length];
							for (int i = 0; i < dValue.length; i++)
								ws.serverNextNonce[i] = dValue[i];
						}
						else
						{
							tsLib.debugPrint(DEBUG_DM, " dValue is NULL");
						}
					}
				}
			}
			else
			{
				return SDM_RET_FAILED;
			}
			szAccBuf = dm_AccXNodeInfo.AppAddr;
			szAccBuf = szAccBuf.concat(SYNCML_APPADDR_ADDR_PATH);
			node = tsOmlib.dmOmLibGetNodeProp(om, szAccBuf);
			if (node == null)
			{
				return SDM_RET_FAILED;
			}
			buf = new char[(int) node.size];
			tsOmlib.dmOmRead(om, szAccBuf, 0, buf, node.size);
			ws.hostname = String.valueOf(buf);

			szAccBuf = dm_AccXNodeInfo.AppAddrPort;
			szAccBuf = szAccBuf.concat(SYNCML_APPADDR_PORT_PORTNUMBER_PATH);
			node = tsOmlib.dmOmLibGetNodeProp(om, szAccBuf);
			if (node == null)
			{
				return SDM_RET_FAILED;
			}
			buf = new char[(int) node.size];
			tsOmlib.dmOmRead(om, szAccBuf, 0, buf, node.size);
			try
			{
				ws.port = (int) Integer.valueOf(String.valueOf(buf));
			}
			catch (NumberFormatException ne)
			{
				tsLib.debugPrintException(DEBUG_EXCEPTION, ne.toString());
			}
			// DEVINFO_DEVID_PATH
			node = tsOmlib.dmOmLibGetNodeProp(om, DEVINFO_DEVID_PATH);
			if (node == null)
			{
				return SDM_RET_FAILED;
			}
			buf = new char[(int) node.size];
			tsOmlib.dmOmRead(om, DEVINFO_DEVID_PATH, 0, buf, node.size);

			ws.sourceURI = String.valueOf(buf);

			buf = null;

			return SDM_RET_OK;
		}
		else
		{
			// Defects
			tsDmWorkspace ws = dm_ws;
			if (ws == null)
			{
				return SDM_RET_FAILED;
			}
			tsOmTree om = ws.om;
			byte[] dValue = null;
			String szAccBuf = "";
			char[] buf;
			tsDmVnode node = null;

			szAccBuf = g_AccName;
			szAccBuf = szAccBuf.concat(SYNCML_DMACC_USERNAME_PATH);
			node = tsOmlib.dmOmLibGetNodeProp(om, szAccBuf);

			if (node == null)
			{
				return SDM_RET_FAILED;
			}

			buf = new char[(int) node.size];
			tsOmlib.dmOmRead(om, szAccBuf, 0, buf, node.size);
			ws.userName = String.valueOf(buf);

			szAccBuf = g_AccName;
			szAccBuf = szAccBuf.concat(SYNCML_DMACC_CLIENTPW_PATH);

			node = tsOmlib.dmOmLibGetNodeProp(om, szAccBuf);
			if (node == null)
			{
				return SDM_RET_FAILED;
			}
			buf = new char[(int) node.size];
			tsOmlib.dmOmRead(om, szAccBuf, 0, buf, node.size);
			ws.clientPW = String.valueOf(buf);

			szAccBuf = g_AccName;
			szAccBuf = szAccBuf.concat(SYNCML_DMACC_AUTHPREF_PATH);
			node = tsOmlib.dmOmLibGetNodeProp(om, szAccBuf);
			if (node == null)
			{
				return SDM_RET_FAILED;
			}
			buf = new char[(int) node.size];
			tsOmlib.dmOmRead(om, szAccBuf, 0, buf, node.size);

			String tmp;
			tmp = String.valueOf(buf);
			ws.credType = Auth.authCredString2Type(tmp);

			if (ws.serverCredType == CRED_TYPE_NONE)
			{
				ws.serverCredType = ws.credType;
			}

			szAccBuf = g_AccName;
			szAccBuf = szAccBuf.concat(SYNCML_DMACC_SERVERID_PATH_1_1);

			node = tsOmlib.dmOmLibGetNodeProp(om, szAccBuf);
			if (node == null)
			{
				return SDM_RET_FAILED;
			}
			buf = new char[(int) node.size];
			tsOmlib.dmOmRead(om, szAccBuf, 0, buf, node.size);
			ws.serverID = String.valueOf(buf);

			szAccBuf = g_AccName;
			szAccBuf = szAccBuf.concat(SYNCML_DMACC_SERVERPW_PATH);
			node = tsOmlib.dmOmLibGetNodeProp(om, szAccBuf);
			if (node == null)
			{

				return SDM_RET_FAILED;
			}
			buf = new char[(int) node.size];
			tsOmlib.dmOmRead(om, szAccBuf, 0, buf, node.size);
			ws.serverPW = String.valueOf(buf);

			szAccBuf = g_AccName;
			szAccBuf = szAccBuf.concat(SYNCML_DMACC_CLIENTNONCE_PATH);
			node = tsOmlib.dmOmLibGetNodeProp(om, szAccBuf);
			if (node != null)
			{
				buf = new char[node.size];
				tsOmlib.dmOmRead(om, szAccBuf, 0, buf, node.size);
				if (buf[0] != 0)
				{
					if (node.format != FORMAT_B64)
					{
						for (int i = 0; i < node.size; i++)
							ws.nextNonce[i] = (byte) buf[i];

						tsLib.debugPrint(DEBUG_DM, "node->size = " + node.size);
					}
					else
					{
						dValue = base64.decode(new String(buf).getBytes());
						if (dValue != null)
						{
							ws.nextNonce = null;
							ws.nextNonce = new byte[dValue.length];
							for (int i = 0; i < dValue.length; i++)
								ws.nextNonce[i] = dValue[i];
						}
						else
						{
							tsLib.debugPrint(DEBUG_DM, "sdmPreparePackaging : dValue is NULL");
						}
					}
				}
			}
			else
			{
				return SDM_RET_FAILED;
			}

			szAccBuf = g_AccName;
			szAccBuf = szAccBuf.concat(SYNCML_DMACC_SERVERNONCE_PATH);
			node = tsOmlib.dmOmLibGetNodeProp(om, szAccBuf);
			if (node != null)
			{
				buf = new char[(int) node.size];
				tsOmlib.dmOmRead(om, szAccBuf, 0, buf, node.size);
				if (buf[0] != 0)
				{
					if (node.format != FORMAT_B64)
					{
						for (int i = 0; i < node.size; i++)
							ws.serverNextNonce[i] = (byte) buf[i];
					}
					else
					{
						tsLib.debugPrint(DEBUG_DM, "Server Next Noncenew String(buf) :" + new String(buf));
						dValue = null;
						dValue = base64.decode(new String(buf).getBytes());
						if (dValue != null)
						{
							ws.serverNextNonce = null;
							ws.serverNextNonce = new byte[dValue.length];
							for (int i = 0; i < dValue.length; i++)
								ws.serverNextNonce[i] = dValue[i];
						}
						else
						{
							tsLib.debugPrint(DEBUG_DM, " dValue is NULL");
						}
					}
				}
			}
			else
			{
				return SDM_RET_FAILED;
			}
			szAccBuf = g_AccName;
			szAccBuf = szAccBuf.concat(SYNCML_DMACC_ADDR_PATH);
			node = tsOmlib.dmOmLibGetNodeProp(om, szAccBuf);
			if (node == null)
			{
				return SDM_RET_FAILED;
			}
			buf = new char[(int) node.size];
			tsOmlib.dmOmRead(om, szAccBuf, 0, buf, node.size);
			ws.hostname = String.valueOf(buf);

			szAccBuf = g_AccName;
			szAccBuf = szAccBuf.concat(SYNCML_DMACC_PORTNBR_PATH);
			node = tsOmlib.dmOmLibGetNodeProp(om, szAccBuf);
			if (node == null)
			{
				return SDM_RET_FAILED;
			}
			buf = new char[(int) node.size];
			tsOmlib.dmOmRead(om, szAccBuf, 0, buf, node.size);
			try
			{
				ws.port = (int) Integer.valueOf(String.valueOf(buf));
			}
			catch (NumberFormatException ne)
			{
				tsLib.debugPrintException(DEBUG_EXCEPTION, ne.toString());
			}

			// DEVINFO_DEVID_PATH
			node = tsOmlib.dmOmLibGetNodeProp(om, DEVINFO_DEVID_PATH);
			if (node == null)
			{
				return SDM_RET_FAILED;
			}
			buf = new char[(int) node.size];
			tsOmlib.dmOmRead(om, DEVINFO_DEVID_PATH, 0, buf, node.size);

			ws.sourceURI = String.valueOf(buf);

			buf = null;

			return SDM_RET_OK;
		}
	}

	public int dmAgentMgmtPackage(tsDmEncoder e)
	{
		tsDmWorkspace ws = dm_ws;
		int res;
		if (ws.dataBuffered)
		{
			res = dmAgentCreatePackageAlert(e, ALERT_NEXT_MESSAGE);
			if (res != SDM_RET_OK)
			{
				if (res == SDM_BUFFER_SIZE_EXCEEDED)
				{
					ws.endOfMsg = false;
				}
				else
				{
					tsLib.debugPrintException(DEBUG_EXCEPTION, "failed = " + res);
				}
				return SDM_RET_FAILED;
			}
		}
		res = dmAgentCreatePackageStatus(e);
		if (res != SDM_RET_OK)
		{
			if (res == SDM_BUFFER_SIZE_EXCEEDED)
			{
				ws.endOfMsg = false;
			}
			else
			{
				tsLib.debugPrintException(DEBUG_EXCEPTION, "failed = " + res);
			}
			return SDM_RET_FAILED;
		}
		res = dmAgentCreatePackageResults(e);
		if (res != SDM_RET_OK)
		{
			if (res == SDM_BUFFER_SIZE_EXCEEDED)
			{
				ws.endOfMsg = false;
			}
			else
			{
				tsLib.debugPrintException(DEBUG_EXCEPTION, "failed = " + res);
			}
			return SDM_RET_FAILED;
		}

		ws.endOfMsg = true;

		return SDM_RET_OK;
	}

	public int dmAgentMakeSyncMLNode()
	{
		if (_SYNCML_TS_DM_VERSION_V12_)
		{
			tsDmWorkspace ws = dm_ws;
			tsOmTree om = ws.om;
			int aclValue;
			String szAccBuf = "";
			int authType = CRED_TYPE_BASIC;
			String TempBuf = "";
			String inbox;
			int nRet = 0;

			// ./
			aclValue = OMACL_GET | OMACL_ADD;
			dmAgentMakeDefaultAcl(om, ".", aclValue, SCOPE_PERMANENT);

			// ./SyncML
			aclValue = OMACL_ADD | OMACL_DELETE | OMACL_GET | OMACL_REPLACE;
			DM_SET_OM_PATH(om, SYNCML_PATH, aclValue, SCOPE_PERMANENT);

			// ./DMAcc
			aclValue = OMACL_ADD | OMACL_DELETE | OMACL_GET | OMACL_REPLACE;
			DM_SET_OM_PATH(om, BASE_ACCOUNT_PATH, aclValue, SCOPE_PERMANENT);

			aclValue = OMACL_ADD | OMACL_DELETE | OMACL_GET | OMACL_REPLACE;
			DM_SET_OM_PATH(om, ATT_BASE_ACCOUNT_PATH, aclValue, SCOPE_PERMANENT);

			// ./SyncML/Con
			aclValue = OMACL_ADD | OMACL_DELETE | OMACL_GET | OMACL_REPLACE;
			DM_SET_OM_PATH(om, SYNCML_CON_PATH, aclValue, SCOPE_PERMANENT);

			aclValue = OMACL_ADD | OMACL_DELETE | OMACL_GET | OMACL_REPLACE;
			DM_SET_OM_PATH(om, dm_AccXNodeInfo.Account, aclValue, SCOPE_DYNAMIC);

			TempBuf = "w7";
			szAccBuf = dm_AccXNodeInfo.Account;
			szAccBuf = szAccBuf.concat(SYNCML_DMACC_APPID_PATH);
			dmAgentSetOMAccStr(om, szAccBuf, TempBuf, aclValue, SCOPE_DYNAMIC);

			TempBuf = tsdmDB.dmdbGetServerID();
			szAccBuf = dm_AccXNodeInfo.Account;
			szAccBuf = szAccBuf.concat(SYNCML_DMACC_SERVERID_PATH);
			aclValue = OMACL_ADD | OMACL_DELETE | OMACL_GET | OMACL_REPLACE;
			dmAgentSetOMAccStr(om, szAccBuf, TempBuf, aclValue, SCOPE_DYNAMIC);

			TempBuf = tsdmDB.dmdbGetProfileName();
			aclValue = OMACL_ADD | OMACL_DELETE | OMACL_GET | OMACL_REPLACE;
			szAccBuf = dm_AccXNodeInfo.Account;
			szAccBuf = szAccBuf.concat(SYNCML_DMACC_NAME_PATH);
			dmAgentSetOMAccStr(om, szAccBuf, TempBuf, aclValue, SCOPE_DYNAMIC);

			TempBuf = tsdmDB.dmdbGetPrefConRef();
			aclValue = OMACL_ADD | OMACL_DELETE | OMACL_GET | OMACL_REPLACE;
			szAccBuf = dm_AccXNodeInfo.Account;
			szAccBuf = szAccBuf.concat(SYNCML_DMACC_PREFCONREF_PATH);
			dmAgentSetOMAccStr(om, szAccBuf, TempBuf, aclValue, SCOPE_DYNAMIC);

			aclValue = OMACL_ADD | OMACL_DELETE | OMACL_GET | OMACL_REPLACE;
			szAccBuf = dm_AccXNodeInfo.Account;
			szAccBuf = szAccBuf.concat(SYNCML_DMACC_TOCONREF_PATH);
			DM_SET_OM_PATH(om, szAccBuf, aclValue, SCOPE_DYNAMIC);

			aclValue = OMACL_ADD | OMACL_DELETE | OMACL_GET | OMACL_REPLACE;
			szAccBuf = dm_AccXNodeInfo.ToConRef;
			DM_SET_OM_PATH(om, szAccBuf, aclValue, SCOPE_DYNAMIC);

			aclValue = OMACL_ADD | OMACL_DELETE | OMACL_GET | OMACL_REPLACE;
			szAccBuf = dm_AccXNodeInfo.ToConRef;
			szAccBuf = szAccBuf.concat(SYNCML_TOCONREF_CONREF_PATH);
			dmAgentSetOMAccStr(om, szAccBuf, SYNCML_DEFAULT_CONREF, aclValue, SCOPE_DYNAMIC);

			aclValue = OMACL_ADD | OMACL_DELETE | OMACL_GET | OMACL_REPLACE;
			szAccBuf = dm_AccXNodeInfo.Account;
			szAccBuf = szAccBuf.concat(SYNCML_DMACC_APPADDR_PATH);
			DM_SET_OM_PATH(om, szAccBuf, aclValue, SCOPE_DYNAMIC);

			aclValue = OMACL_ADD | OMACL_DELETE | OMACL_GET | OMACL_REPLACE;
			szAccBuf = dm_AccXNodeInfo.AppAddr;
			DM_SET_OM_PATH(om, szAccBuf, aclValue, SCOPE_DYNAMIC);

			TempBuf = tsdmDB.dmdbGetServerUrl(SYNCMLDM);
			tsLib.debugPrint(DEBUG_DM, "ServerUrl = " + TempBuf);
			aclValue = OMACL_ADD | OMACL_DELETE | OMACL_GET | OMACL_REPLACE;
			szAccBuf = dm_AccXNodeInfo.AppAddr;
			szAccBuf = szAccBuf.concat(SYNCML_APPADDR_ADDR_PATH);
			dmAgentSetOMAccStr(om, szAccBuf, TempBuf, aclValue, SCOPE_DYNAMIC);

			aclValue = OMACL_ADD | OMACL_DELETE | OMACL_GET | OMACL_REPLACE;
			szAccBuf = dm_AccXNodeInfo.AppAddr;
			szAccBuf = szAccBuf.concat(SYNCML_APPADDR_ADDRTYPE_PATH);
			dmAgentSetOMAccStr(om, szAccBuf, "URI", aclValue, SCOPE_DYNAMIC);

			aclValue = OMACL_ADD | OMACL_DELETE | OMACL_GET | OMACL_REPLACE;
			szAccBuf = dm_AccXNodeInfo.AppAddr;
			szAccBuf = szAccBuf.concat(SYNCML_APPADDR_PORT_PATH);
			DM_SET_OM_PATH(om, szAccBuf, aclValue, SCOPE_DYNAMIC);

			aclValue = OMACL_ADD | OMACL_DELETE | OMACL_GET | OMACL_REPLACE;
			szAccBuf = dm_AccXNodeInfo.AppAddrPort;
			DM_SET_OM_PATH(om, szAccBuf, aclValue, SCOPE_DYNAMIC);

			TempBuf = String.valueOf(tsdmDB.dmdbGetServerPort(SYNCMLDM));
			tsLib.debugPrint(DEBUG_DM, "ServerPort = " + TempBuf);
			aclValue = OMACL_ADD | OMACL_DELETE | OMACL_GET | OMACL_REPLACE;
			szAccBuf = dm_AccXNodeInfo.AppAddrPort;
			szAccBuf = szAccBuf.concat(SYNCML_APPADDR_PORT_PORTNUMBER_PATH);
			dmAgentSetOMAccStr(om, szAccBuf, TempBuf, aclValue, SCOPE_DYNAMIC);

			authType = tsdmDB.dmdbGetAuthType();
			aclValue = OMACL_ADD | OMACL_DELETE | OMACL_GET | OMACL_REPLACE;
			szAccBuf = dm_AccXNodeInfo.Account;
			szAccBuf = szAccBuf.concat(SYNCML_DMACC_AAUTHPREF_PATH);
			dmAgentSetOMAccStr(om, szAccBuf, Auth.authCredType2String(authType), aclValue, SCOPE_DYNAMIC);

			aclValue = OMACL_ADD | OMACL_DELETE | OMACL_GET | OMACL_REPLACE;
			szAccBuf = dm_AccXNodeInfo.Account;
			szAccBuf = szAccBuf.concat(SYNCML_DMACC_APPAUTH_PATH);
			DM_SET_OM_PATH(om, szAccBuf, aclValue, SCOPE_DYNAMIC);

			aclValue = OMACL_ADD | OMACL_DELETE | OMACL_GET | OMACL_REPLACE;
			szAccBuf = dm_AccXNodeInfo.ClientAppAuth;
			DM_SET_OM_PATH(om, szAccBuf, aclValue, SCOPE_DYNAMIC);

			TempBuf = tsdmDB.dmdbGetAuthLevel();
			aclValue = OMACL_ADD | OMACL_DELETE | OMACL_GET | OMACL_REPLACE;
			szAccBuf = dm_AccXNodeInfo.ClientAppAuth;
			szAccBuf = szAccBuf.concat(SYNCML_APPAUTH_AAUTHLEVEL_PATH);
			dmAgentSetOMAccStr(om, szAccBuf, TempBuf, aclValue, SCOPE_DYNAMIC);

			authType = tsdmDB.dmdbGetAuthType();
			aclValue = OMACL_ADD | OMACL_DELETE | OMACL_GET | OMACL_REPLACE;
			szAccBuf = dm_AccXNodeInfo.ClientAppAuth;
			szAccBuf = szAccBuf.concat(SYNCML_APPAUTH_AAUTHTYPE_PATH);
			dmAgentSetOMAccStr(om, szAccBuf, Auth.authAAuthType2String(authType), aclValue, SCOPE_DYNAMIC);

			TempBuf = tsdmDB.dmdbGetUsername();
			aclValue = OMACL_ADD | OMACL_DELETE | OMACL_GET | OMACL_REPLACE;
			szAccBuf = dm_AccXNodeInfo.ClientAppAuth;
			szAccBuf = szAccBuf.concat(SYNCML_APPAUTH_AAUTHNAME_PATH);
			dmAgentSetOMAccStr(om, szAccBuf, TempBuf, aclValue, SCOPE_DYNAMIC);

			TempBuf = tsdmDB.dmdbGetClientPassword();
			aclValue = OMACL_ADD | OMACL_DELETE | OMACL_GET | OMACL_REPLACE;
			szAccBuf = dm_AccXNodeInfo.ClientAppAuth;
			szAccBuf = szAccBuf.concat(SYNCML_APPAUTH_AAUTHSECRET_PATH);
			dmAgentSetOMAccStr(om, szAccBuf, TempBuf, aclValue, SCOPE_DYNAMIC);

			TempBuf = tsdmDB.dmdbGetClientNonce();
			TempBuf = dmAgentCheckNonce(TempBuf); // Defects

			aclValue = OMACL_ADD | OMACL_DELETE | OMACL_GET | OMACL_REPLACE;
			szAccBuf = dm_AccXNodeInfo.ClientAppAuth;
			szAccBuf = szAccBuf.concat(SYNCML_APPAUTH_AAUTHDATA_PATH);
			dmAgentSetOMAccB64(om, szAccBuf, TempBuf, aclValue, SCOPE_DYNAMIC);

			aclValue = OMACL_ADD | OMACL_DELETE | OMACL_GET | OMACL_REPLACE;
			szAccBuf = dm_AccXNodeInfo.ServerAppAuth;
			DM_SET_OM_PATH(om, szAccBuf, aclValue, SCOPE_DYNAMIC);

			TempBuf = tsdmDB.dmdbGetServerAuthLevel();
			aclValue = OMACL_ADD | OMACL_DELETE | OMACL_GET | OMACL_REPLACE;
			szAccBuf = dm_AccXNodeInfo.ServerAppAuth;
			szAccBuf = szAccBuf.concat(SYNCML_APPAUTH_AAUTHLEVEL_PATH);
			dmAgentSetOMAccStr(om, szAccBuf, TempBuf, aclValue, SCOPE_DYNAMIC);

			authType = tsdmDB.dmdbGetServerAuthType();
			aclValue = OMACL_ADD | OMACL_DELETE | OMACL_GET | OMACL_REPLACE;
			szAccBuf = dm_AccXNodeInfo.ServerAppAuth;
			szAccBuf = szAccBuf.concat(SYNCML_APPAUTH_AAUTHTYPE_PATH);
			dmAgentSetOMAccStr(om, szAccBuf, Auth.authAAuthType2String(authType), aclValue, SCOPE_DYNAMIC);

			TempBuf = tsdmDB.dmdbGetServerID();
			aclValue = OMACL_ADD | OMACL_DELETE | OMACL_GET | OMACL_REPLACE;
			szAccBuf = dm_AccXNodeInfo.ServerAppAuth;
			szAccBuf = szAccBuf.concat(SYNCML_APPAUTH_AAUTHNAME_PATH);
			dmAgentSetOMAccStr(om, szAccBuf, TempBuf, aclValue, SCOPE_DYNAMIC);

			TempBuf = tsdmDB.dmdbGetServerPassword();
			aclValue = OMACL_ADD | OMACL_DELETE | OMACL_GET | OMACL_REPLACE;
			szAccBuf = dm_AccXNodeInfo.ServerAppAuth;
			szAccBuf = szAccBuf.concat(SYNCML_APPAUTH_AAUTHSECRET_PATH);
			dmAgentSetOMAccStr(om, szAccBuf, TempBuf, aclValue, SCOPE_DYNAMIC);

			TempBuf = tsdmDB.dmdbGetServerNonce();
			TempBuf = dmAgentCheckNonce(TempBuf); // Defects
			aclValue = OMACL_ADD | OMACL_DELETE | OMACL_GET | OMACL_REPLACE;
			szAccBuf = dm_AccXNodeInfo.ServerAppAuth;
			szAccBuf = szAccBuf.concat(SYNCML_APPAUTH_AAUTHDATA_PATH);
			dmAgentSetOMAccB64(om, szAccBuf, TempBuf, aclValue, SCOPE_DYNAMIC);

			aclValue = OMACL_ADD | OMACL_DELETE | OMACL_GET | OMACL_REPLACE;
			szAccBuf = dm_AccXNodeInfo.Account;
			szAccBuf = szAccBuf.concat(SYNCML_DMACC_EXT_PATH);
			dmAgentSetOMAccStr(om, szAccBuf, " ", aclValue, SCOPE_DYNAMIC);

			inbox = ddfParser.dmDDFGetMOPath(ddfParser.DM_MO_ID_INBOX);
			if (inbox != null)
			{
				aclValue = OMACL_ADD | OMACL_DELETE | OMACL_GET | OMACL_REPLACE;
				DM_SET_OM_PATH(om, inbox, aclValue, SCOPE_PERMANENT);
			}

			nRet = SDM_RET_OK;
			pAccName = null;
			TempBuf = null;

			return nRet;
		}
		else
		{
			tsDmWorkspace ws = dm_ws;
			tsOmTree om = ws.om;
			int aclValue;
			String szAccBuf = "";
			int authType = CRED_TYPE_BASIC;
			String TempBuf = "";
			String pAccName = null;
			int nRet = 0;

			aclValue = OMACL_ADD | OMACL_DELETE | OMACL_GET | OMACL_REPLACE;
			DM_SET_OM_PATH(om, SYNCML_PATH, aclValue, SCOPE_PERMANENT);

			aclValue = OMACL_ADD | OMACL_DELETE | OMACL_GET | OMACL_REPLACE;
			DM_SET_OM_PATH(om, SYNCML_DMACC_PATH, aclValue, SCOPE_PERMANENT);

			aclValue = OMACL_ADD | OMACL_DELETE | OMACL_GET | OMACL_REPLACE;
			DM_SET_OM_PATH(om, SYNCML_CON_PATH, aclValue, SCOPE_PERMANENT);

			pAccName = tsdmDB.dmdbGetProfileName();

			if (pAccName == null)
			{
				pAccName = "x";
			}

			if (g_AccName != null)
			{
				g_AccName = g_AccName.concat("/");
				g_AccName = g_AccName.concat(pAccName);
			}
			else
			{
				g_AccName = "./SyncML/DMAcc";
				g_AccName = g_AccName.concat("/");
				g_AccName = g_AccName.concat(pAccName);
			}

			aclValue = OMACL_ADD | OMACL_DELETE | OMACL_GET | OMACL_REPLACE;
			DM_SET_OM_PATH(om, g_AccName, aclValue, SCOPE_DYNAMIC);

			TempBuf = tsdmDB.dmdbGetServerID();
			aclValue = OMACL_ADD | OMACL_DELETE | OMACL_GET | OMACL_REPLACE;
			szAccBuf = g_AccName;
			szAccBuf += SYNCML_DMACC_SERVERID_PATH_1_1;
			dmAgentSetOMAccStr(om, szAccBuf, TempBuf, aclValue, SCOPE_DYNAMIC);

			TempBuf = tsdmDB.dmdbGetUsername();
			aclValue = OMACL_ADD | OMACL_DELETE | OMACL_GET | OMACL_REPLACE;
			szAccBuf = g_AccName;
			szAccBuf += SYNCML_DMACC_USERNAME_PATH;
			dmAgentSetOMAccStr(om, szAccBuf, TempBuf, aclValue, SCOPE_DYNAMIC);

			TempBuf = tsdmDB.dmdbGetClientPassword();
			aclValue = OMACL_ADD | OMACL_DELETE | OMACL_REPLACE;
			szAccBuf = g_AccName;
			szAccBuf += SYNCML_DMACC_CLIENTPW_PATH;
			dmAgentSetOMAccStr(om, szAccBuf, TempBuf, aclValue, SCOPE_DYNAMIC);

			TempBuf = tsdmDB.dmdbGetServerPassword();
			aclValue = OMACL_ADD | OMACL_DELETE | OMACL_REPLACE;
			szAccBuf = g_AccName;
			szAccBuf += SYNCML_DMACC_SERVERPW_PATH;
			dmAgentSetOMAccStr(om, szAccBuf, TempBuf, aclValue, SCOPE_DYNAMIC);

			TempBuf = tsdmDB.dmdbGetServerUrl(SYNCMLDM);
			aclValue = OMACL_ADD | OMACL_DELETE | OMACL_GET | OMACL_REPLACE;
			szAccBuf = g_AccName;
			szAccBuf += SYNCML_DMACC_ADDR_PATH;
			dmAgentSetOMAccStr(om, szAccBuf, TempBuf, aclValue, SCOPE_DYNAMIC);

			aclValue = OMACL_ADD | OMACL_DELETE | OMACL_GET | OMACL_REPLACE;
			szAccBuf = g_AccName;
			szAccBuf += SYNCML_DMACC_ADDRTYPE_PATH;
			dmAgentSetOMAccStr(om, szAccBuf, " ", aclValue, SCOPE_DYNAMIC);

			int Port = tsdmDB.dmdbGetServerPort(SYNCMLDM);
			TempBuf = String.valueOf(Port);
			aclValue = OMACL_ADD | OMACL_DELETE | OMACL_GET | OMACL_REPLACE;
			szAccBuf = g_AccName;
			szAccBuf += SYNCML_DMACC_PORTNBR_PATH;
			dmAgentSetOMAccStr(om, szAccBuf, TempBuf, aclValue, SCOPE_DYNAMIC);

			TempBuf = tsdmDB.dmdbGetClientNonce();
			TempBuf = dmAgentCheckNonce(TempBuf); // Defects
			aclValue = OMACL_ADD | OMACL_DELETE | OMACL_REPLACE;
			szAccBuf = g_AccName;
			szAccBuf += SYNCML_DMACC_CLIENTNONCE_PATH;
			dmAgentSetOMAccB64(om, szAccBuf, TempBuf, aclValue, SCOPE_DYNAMIC);

			TempBuf = tsdmDB.dmdbGetServerNonce();
			TempBuf = dmAgentCheckNonce(TempBuf); // Defects
			aclValue = OMACL_ADD | OMACL_DELETE | OMACL_REPLACE;
			szAccBuf = g_AccName;
			szAccBuf += SYNCML_DMACC_SERVERNONCE_PATH;
			dmAgentSetOMAccB64(om, szAccBuf, TempBuf, aclValue, SCOPE_DYNAMIC);

			authType = tsdmDB.dmdbGetAuthType();
			TempBuf = Auth.authCredType2String(authType);
			aclValue = OMACL_ADD | OMACL_DELETE | OMACL_REPLACE;
			szAccBuf = g_AccName;
			szAccBuf += SYNCML_DMACC_AUTHPREF_PATH;
			dmAgentSetOMAccStr(om, szAccBuf, TempBuf, aclValue, SCOPE_DYNAMIC);

			aclValue = OMACL_ADD | OMACL_DELETE | OMACL_GET | OMACL_REPLACE;
			szAccBuf = g_AccName;
			szAccBuf += SYNCML_DMACC_CONREF_PATH;
			DM_SET_OM_STR(om, szAccBuf, SYNCML_DEFAULT_CONREF, aclValue, SCOPE_DYNAMIC);

			nRet = SDM_RET_OK;
			pAccName = null;
			TempBuf = null;

			return nRet;
		}
	}

	private String dmAgentCheckNonce(String nonce)
	{
		// Defects
		if (!tsLib.isEmpty(nonce))
		{
			return nonce;
		}
		else
		{
			nonce = DM_DEFAULT_NONCE;
		}

		byte[] encoder1 = base64.encode(nonce.getBytes());
		nonce = new String(encoder1);

		return nonce;
	}

	public static int dmAgentReMakeFwUpdateNode(tsOmTree om, String FumoNodePath)
	{
		int aclValue;
		String pFUMONode = null;
		String pFUMOPackageNode = null;
		String tmpbuf = null;
		char[] tmpbuf_2 = null;
		String status = null;

		pFUMONode = "";
		pFUMOPackageNode = "";
		status = "";
		tmpbuf = "";
		tmpbuf_2 = new char[FumoNodePath.length()];

		String path = FumoNodePath;
		tmpbuf = path;

		for (;;)
		{
			tsOmlib.dmOmMakeParentPath(path, tmpbuf_2);
			tmpbuf = tsLib.libString(tmpbuf_2);
			tsLib.debugPrint(DEBUG_DM, tmpbuf); // Defects : Avoid String + operator in loops

			if ((!tmpbuf.contains(DM_OMA_EXEC_REPLACE)) && (!tmpbuf.contains(DM_OMA_EXEC_ALTERNATIVE)) && (!tmpbuf.contains(DM_OMA_EXEC_ALTERNATIVE_2)))
			{
				if (tsOmlib.dmOmLibGetNodeProp(om, tmpbuf) == null)
				{
					tsOmlib.dmOmProcessCmdImplicitAdd(om, tmpbuf, OMACL_GET | OMACL_REPLACE, 1);
				}
				break;
			}
			path = tmpbuf;
		}

		// may be modification required
		pFUMOPackageNode = tmpbuf;

		/* strcpy -> memcpy */
		pFUMONode = pFUMOPackageNode + FUMO_PKGNAME_PATH;
		aclValue = OMACL_ADD | OMACL_GET | OMACL_REPLACE;
		dmAgentSetOMAccStr(om, pFUMONode, " ", aclValue, SCOPE_DYNAMIC);

		pFUMONode = pFUMOPackageNode + FUMO_PKGVERSION_PATH;
		aclValue = OMACL_ADD | OMACL_GET | OMACL_REPLACE;
		dmAgentSetOMAccStr(om, pFUMONode, " ", aclValue, SCOPE_DYNAMIC);

		pFUMONode = pFUMOPackageNode + FUMO_DOWNLOAD_PATH;
		aclValue = OMACL_ADD | OMACL_GET | OMACL_EXEC;
		DM_SET_OM_PATH(om, pFUMONode, aclValue, SCOPE_DYNAMIC);

		pFUMONode = pFUMONode.concat(FUMO_PKGURL_PATH);
		aclValue = OMACL_ADD | OMACL_GET | OMACL_REPLACE;
		dmAgentSetOMAccStr(om, pFUMONode, " ", aclValue, SCOPE_DYNAMIC);

		pFUMONode = pFUMOPackageNode + FUMO_UPDATE_PATH;
		aclValue = OMACL_ADD | OMACL_GET | OMACL_REPLACE | OMACL_EXEC;
		DM_SET_OM_PATH(om, pFUMONode, aclValue, SCOPE_DYNAMIC);

		pFUMONode = pFUMONode.concat(FUMO_PKGDATA_PATH);
		aclValue = OMACL_REPLACE;
		dmAgentSetOMAccBin(om, pFUMONode, "", 0, aclValue, SCOPE_DYNAMIC);

		pFUMONode = pFUMOPackageNode + FUMO_DOWNLOADANDUPDATE_PATH;
		aclValue = OMACL_ADD | OMACL_GET | OMACL_EXEC | OMACL_REPLACE;
		DM_SET_OM_PATH(om, pFUMONode, aclValue, SCOPE_DYNAMIC);

		pFUMONode = pFUMONode.concat(FUMO_PKGURL_PATH);
		aclValue = OMACL_ADD | OMACL_GET | OMACL_REPLACE;
		dmAgentSetOMAccStr(om, pFUMONode, " ", aclValue, SCOPE_DYNAMIC);

		pFUMONode = pFUMOPackageNode + FUMO_STATE_PATH;
		aclValue = OMACL_GET;

		// MOD : for ./FUMO/x/Status.
		status = String.valueOf(tsdmDB.dmdbGetFUMOStatus());
		dmAgentSetOMAccStr(om, pFUMONode, status, aclValue, SCOPE_DYNAMIC);

		pFUMONode = pFUMOPackageNode + FUMO_EXT;
		aclValue = OMACL_GET;
		DM_SET_OM_PATH(om, pFUMONode, aclValue, SCOPE_DYNAMIC);

		tsLib.debugPrint(DEBUG_DM, "pFUMONode:" + pFUMONode);
		return SDM_RET_OK;
	}

	public int dmAgentMakeDevInfoNode()
	{
		tsDmWorkspace ws = dm_ws;
		tsOmTree om = ws.om;
		int aclValue;
		String pModelName = "";
		String pManuFact = "";
		String pDevID = "";
		String pLang = "";

		aclValue = OMACL_ADD | OMACL_DELETE | OMACL_GET;
		DM_SET_OM_PATH(om, DEVINFO_PATH, aclValue, SCOPE_PERMANENT);

		pDevID = dmDevinfoAdapter.devAdpGetDeviceId();
		aclValue = OMACL_GET;
		dmAgentSetOMAccStr(om, DEVINFO_DEVID_PATH, pDevID, aclValue, SCOPE_PERMANENT);


		pManuFact = dmDevinfoAdapter.devAdpGetManufacturer();
		aclValue = OMACL_ADD | OMACL_DELETE | OMACL_GET;
		dmAgentSetOMAccStr(om, DEVINFO_MAN_PATH, pManuFact, aclValue, SCOPE_PERMANENT);


		pModelName = dmDevinfoAdapter.devAdpGetModelName();
		aclValue = OMACL_ADD | OMACL_DELETE | OMACL_GET;
		dmAgentSetOMAccStr(om, DEVINFO_MOD_PATH, pModelName, aclValue, SCOPE_PERMANENT);

		aclValue = OMACL_ADD | OMACL_DELETE | OMACL_GET;

		if (_SYNCML_TS_DM_VERSION_V11_)
		{
			dmAgentSetOMAccStr(om, DEVINFO_DMV_PATH, DEVINFO_DEFAULT_DMV1_1, aclValue, SCOPE_PERMANENT);
		}
		else if (_SYNCML_TS_DM_VERSION_V12_)
		{
			dmAgentSetOMAccStr(om, DEVINFO_DMV_PATH, DEVINFO_DEFAULT_DMV1_2, aclValue, SCOPE_PERMANENT);
		}

		pLang = dmDevinfoAdapter.devAdpGetLanguageSetting();
		aclValue = OMACL_ADD | OMACL_DELETE | OMACL_GET;
		dmAgentSetOMAccStr(om, DEVINFO_LANG_PATH, pLang, aclValue, SCOPE_PERMANENT);

		aclValue = OMACL_DELETE | OMACL_GET;
		DM_SET_OM_PATH(om, DEVINFO_EXT_PATH, aclValue, SCOPE_PERMANENT);
		
		pModelName = null;
		pManuFact = null;
		pDevID = null;
		pLang = null;

		return SDM_RET_OK;
	}

	public int dmAgentMakeDevDetailNode()
	{
		tsDmWorkspace ws = dm_ws;
		tsOmTree om = ws.om;
		tsDmVnode node;
		int aclValue;
		String pHwVersion = "";
		String pSwVersion = "";
		String pFwVersion = "";
		String pOEMName = "";

		aclValue = OMACL_GET;

		DM_SET_OM_PATH(om, DEVDETAIL_PATH, aclValue, SCOPE_PERMANENT);

		aclValue = OMACL_GET;
		DM_SET_OM_PATH(om, DEVDETAIL_URI_PATH, aclValue, SCOPE_PERMANENT);

		aclValue = OMACL_GET;
		dmAgentSetOMAccStr(om, DEVDETAIL_URI_MAXDEPTH_PATH, DEVDETAIL_DEFAULT_URI_SUBNODE_VALUE, aclValue, SCOPE_PERMANENT);

		aclValue = OMACL_GET;
		dmAgentSetOMAccStr(om, DEVDETAIL_URI_MAXTOLEN_PATH, DEVDETAIL_DEFAULT_URI_SUBNODE_VALUE, aclValue, SCOPE_PERMANENT);

		aclValue = OMACL_GET;
		dmAgentSetOMAccStr(om, DEVDETAIL_URI_MAXSEGLEN_PATH, DEVDETAIL_DEFAULT_URI_SUBNODE_VALUE, aclValue, SCOPE_PERMANENT);

		aclValue = OMACL_GET;
		dmAgentSetOMAccStr(om, DEVDETAIL_DEVTYPE_PATH, DEVDETAIL_DEFAULT_DEVTYPE, aclValue, SCOPE_PERMANENT);

		pOEMName = dmDevinfoAdapter.devGetOEMName();
		aclValue = OMACL_GET;
		dmAgentSetOMAccStr(om, DEVDETAIL_OEM_PATH, pOEMName, aclValue, SCOPE_PERMANENT);

		pFwVersion = dmDevinfoAdapter.devAdpGetFirmwareVersion();
		aclValue = OMACL_GET;
		dmAgentSetOMAccStr(om, DEVDETAIL_FWV_PATH, pFwVersion, aclValue, SCOPE_PERMANENT);

		pSwVersion = dmDevinfoAdapter.devAdpGetSoftwareVersion();
		aclValue = OMACL_GET;
		dmAgentSetOMAccStr(om, DEVDETAIL_SWV_PATH, pSwVersion, aclValue, SCOPE_PERMANENT);

		pHwVersion = dmDevinfoAdapter.devAdpGetHardwareVersion();
		aclValue = OMACL_GET;
		dmAgentSetOMAccStr(om, DEVDETAIL_HWV_PATH, pHwVersion, aclValue, SCOPE_PERMANENT);

		aclValue = OMACL_GET;
		dmAgentSetOMAccStr(om, DEVDETAIL_LRGOBJ_PATH, DEVDETAIL_DEFAULT_LRGOBJ_SUPPORT, aclValue, SCOPE_PERMANENT);
		node = tsOmlib.dmOmLibGetNodeProp(om, DEVDETAIL_LRGOBJ_PATH);
		if (node != null)
		{
			node.format = FORMAT_BOOL;
			node.type = null;
		}
		
		aclValue = OMACL_GET | OMACL_DELETE;
		DM_SET_OM_PATH(om, DEVDETAIL_EXT_PATH, aclValue, SCOPE_PERMANENT);

		pHwVersion = null;
		pSwVersion = null;
		pFwVersion = null;
		pOEMName = null;

		return SDM_RET_OK;
	}

	public int dmAgentMakeFwUpdateNode()
	{
		tsDmWorkspace ws = dm_ws;
		tsOmTree om = ws.om;
		int aclValue;

		String pFUMONode;
		String pFUMOPackageNode = "";
		int count;
		int nStatus;
		String status = "";
		int nFumoXnodeCount;

		tsLib.debugPrint(DEBUG_DM, " Initialize");

		String pFUMORoot = FUMO_PATH;
		aclValue = OMACL_ADD | OMACL_GET | OMACL_REPLACE;
		DM_SET_OM_PATH(om, pFUMORoot, aclValue, SCOPE_PERMANENT); // juneyeob : permanent node for FUMO

		String szFumoXnode = tsdmDBadapter.dmDBAdpGetFUMOxNodeName();
		nFumoXnodeCount = tsdmDBadapter.dmDBAdpGetFUMOxNodenCount(szFumoXnode);

		for (count = 0; count < nFumoXnodeCount; count++)
		{
			if (nFumoXnodeCount > 1)
			{
				// Defects : Avoid String + operator in loops
				pFUMOPackageNode = pFUMORoot.concat(szFumoXnode).concat(String.valueOf(count + 1));
			}
			else
			{
				pFUMOPackageNode = pFUMORoot.concat(szFumoXnode);
			}

			// Defects : Avoid String + operator in loops
			String dbgStr = "pFUMOPackageNode :";
			dbgStr = dbgStr.concat(pFUMOPackageNode);
			tsLib.debugPrint(DEBUG_DM, dbgStr);
			aclValue = OMACL_ADD | OMACL_GET | OMACL_REPLACE;
			DM_SET_OM_PATH(om, pFUMOPackageNode, aclValue, SCOPE_PERMANENT); // juneyeob : permanent node for FUMO

			// Defects : Avoid String + operator in loops
			pFUMONode = pFUMOPackageNode.concat(FUMO_PKGNAME_PATH);
			aclValue = OMACL_ADD | OMACL_GET | OMACL_REPLACE;
			dmAgentSetOMAccStr(om, pFUMONode, FUMO_DEFAULT_PKGNAME, aclValue, SCOPE_DYNAMIC);

			pFUMONode = pFUMOPackageNode.concat(FUMO_PKGVERSION_PATH);
			aclValue = OMACL_ADD | OMACL_GET | OMACL_REPLACE;
			dmAgentSetOMAccStr(om, pFUMONode, FUMO_DEFAULT_PKGVERSION, aclValue, SCOPE_DYNAMIC);

			pFUMONode = pFUMOPackageNode.concat(FUMO_DOWNLOAD_PATH);
			aclValue = OMACL_ADD | OMACL_GET | OMACL_EXEC | OMACL_REPLACE;
			DM_SET_OM_PATH(om, pFUMONode, aclValue, SCOPE_DYNAMIC);

			pFUMONode = pFUMONode.concat(FUMO_PKGURL_PATH);
			aclValue = OMACL_ADD | OMACL_GET | OMACL_REPLACE;
			dmAgentSetOMAccStr(om, pFUMONode, " ", aclValue, SCOPE_DYNAMIC);

			pFUMONode = pFUMOPackageNode.concat(FUMO_UPDATE_PATH);
			aclValue = OMACL_ADD | OMACL_GET | OMACL_REPLACE | OMACL_EXEC;
			DM_SET_OM_PATH(om, pFUMONode, aclValue, SCOPE_DYNAMIC);

			pFUMONode = pFUMONode.concat(FUMO_PKGDATA_PATH);
			aclValue = OMACL_REPLACE;
			dmAgentSetOMAccBin(om, pFUMONode, "", 0, aclValue, SCOPE_DYNAMIC);

			pFUMONode = pFUMOPackageNode.concat(FUMO_DOWNLOADANDUPDATE_PATH);
			aclValue = OMACL_ADD | OMACL_GET | OMACL_EXEC | OMACL_REPLACE;
			DM_SET_OM_PATH(om, pFUMONode, aclValue, SCOPE_DYNAMIC);

			pFUMONode = pFUMONode.concat(FUMO_PKGURL_PATH);
			aclValue = OMACL_ADD | OMACL_GET | OMACL_REPLACE;
			dmAgentSetOMAccStr(om, pFUMONode, " ", aclValue, SCOPE_DYNAMIC);

			pFUMONode = pFUMOPackageNode.concat(FUMO_STATE_PATH);
			aclValue = OMACL_GET;

			// MOD : for ./FUMO/x/Status
			nStatus = tsdmDB.dmdbGetFUMOStatus();
			status = String.valueOf(nStatus);
			tsLib.debugPrint(DEBUG_DM, status);
			if(status.equals("0") == true)
			{
				tsLib.debugPrint(DEBUG_DM, "in");
				status = "10";
			}
			tsLib.debugPrint(DEBUG_DM, "pFUMONode " + pFUMONode + "/" + status);

			/* MOD : for ./FUMO/x/Status */
			dmAgentSetOMAccStr(om, pFUMONode, status, aclValue, SCOPE_DYNAMIC);

			pFUMONode = pFUMOPackageNode.concat(FUMO_EXT);
			aclValue = OMACL_GET;
			DM_SET_OM_PATH(om, pFUMONode, aclValue, SCOPE_DYNAMIC);
		}

		return SDM_RET_OK;
	}

	public static void DM_SET_OM_PATH(tsOmTree om, String path, int aclValue, int scope)
	{
		if (tsOmlib.dmOmLibGetNodeProp(om, path) == null)
		{
			tsOmlib.dmOmWrite(om, path, 0, 0, "", 0);
			dmAgentMakeDefaultAcl(om, path, aclValue, scope);
		}
	}

	public void DM_SET_OM_STR(tsOmTree om, String path, String str, int aclValue, int scope)
	{
		if (tsOmlib.dmOmLibGetNodeProp(om, path) == null)
		{
			dmAgentSetOM(path, str);
			dmAgentMakeDefaultAcl(om, path, aclValue, scope);
		}
	}

	public static void dmAgentMakeDefaultAcl(tsOmTree om, String path, int aclValue, int scope)
	{
		tsOmAcl acl = null;
		tsOmList item = null;
		tsDmVnode node = null;

		node = tsOmlib.dmOmLibGetNodeProp(om, path);
		if (node != null)
		{

			if (aclValue != OMACL_NONE)
			{
				item = node.acl;
				acl = (tsOmAcl) item.data;
				acl.ac = aclValue;
			}
			else
			{
				tsLib.debugPrint(DEBUG_DM, "ACL is OMACL_NONE");
			}
			node.scope = scope;
		}
		else
		{
			tsLib.debugPrint(DEBUG_DM, "Not Exist");
		}
	}

	public static void dmAgentSetOMAccStr(Object omt, String path, String str, int aclValue, int scope)
	{
		tsOmTree om = (tsOmTree) omt;
		tsDmVnode node;
		char[] temp;
		String tmp;
		node = tsOmlib.dmOmLibGetNodeProp(om, path);

		if (node == null)
		{
			dmAgentSetOM(path, str);
			dmAgentMakeDefaultAcl(om, path, aclValue, scope);
			return;
		}

		temp = new char[(int) node.size];
		tsOmlib.dmOmRead(om, path, 0, temp, node.size);

		tmp = String.valueOf(temp);
		if (node.size != str.length())
		{
			dmAgentSetOM(path, str);
		}
		else if (tmp.compareTo(str) != 0)
		{
			dmAgentSetOM(path, str);
		}

		temp = null;

	}

	public void dmAgentSetOMAccB64(Object omt, String path, String str, int aclValue, int scope)
	{
		tsOmTree om = (tsOmTree) omt;
		tsDmVnode node;
		char[] temp;
		String tmp;

		node = tsOmlib.dmOmLibGetNodeProp(om, path);
		if (node == null)
		{
			dmAgentSetOMB64(path, str);
			dmAgentMakeDefaultAcl(om, path, aclValue, scope);
			return;
		}

		temp = new char[node.size];
		tsOmlib.dmOmRead(om, path, 0, temp, node.size);

		tmp = String.valueOf(temp);
		if (str.length() != node.size)
		{
			dmAgentSetOMB64(path, str);
		}
		else if (tmp.compareTo(str) != 0)
		{
			dmAgentSetOMB64(path, str);
		}
		temp = null;
	}

	public static void dmAgentSetOMAccBin(Object omt, String path, String pData, int size, int aclValue, int scope)
	{
		tsOmTree om = (tsOmTree) omt;
		tsDmVnode node;
		char[] temp;

		node = tsOmlib.dmOmLibGetNodeProp(om, path);

		if (node == null)
		{
			dmAgentSetOMBin(om, path, pData, size);
			dmAgentMakeDefaultAcl(om, path, aclValue, scope);
			return;
		}

		temp = new char[node.size];
		tsOmlib.dmOmRead(om, path, 0, temp, node.size);

		String tmpStr =  Arrays.toString(temp); //temp.toString();

		if (size != node.size)
		{
			dmAgentSetOMBin(om, path, pData, size);
		}
		else if (tmpStr.compareTo(pData) != 0)
		{
			dmAgentSetOMBin(om, path, pData, size);
		}
	}

	public static void dmAgentSetOMBin(Object om, String path, String data, int datasize)
	{
		tsDmWorkspace ws = dm_ws;
		tsDmVnode node;

		tsOmlib.dmOmWrite(ws.om, path, datasize, 0, data, datasize);
		node = tsOmlib.dmOmLibGetNodeProp(ws.om, path);

		if (node != null)
		{
			if (node.type != null)
			{
				tsOmlib.dmOmDeleteMimeList(node.type);
			}
			node.type = null;
			node.format = FORMAT_BIN;
		}
	}

	public void dmAgentSetOMB64(String path, Object data)
	{
		tsDmWorkspace ws = dm_ws;
		tsDmVnode node;
		tsOmList list;
		int nLen = 0;

		if (data == null)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, "data is NULL");
			return;
		}

		nLen = String.valueOf(data).getBytes().length;

		if (nLen <= 0)
		{
			tsOmlib.dmOmlibDeleteImplicit(ws.om, path, true);
			tsLib.debugPrint(DEBUG_DM, "The [%s] node is 0 length" + path);
		}

		tsOmlib.dmOmWrite(ws.om, path, nLen, 0, data, nLen);
		node = tsOmlib.dmOmLibGetNodeProp(ws.om, path);
		if (node != null)
		{
			if (node.type != null)
				tsOmlib.dmOmDeleteMimeList(node.type);

			list = new tsOmList();
			list.data = "text/plain";
			list.next = null;
			node.type = list;
			node.format = FORMAT_B64;
		}
	}

	public static void dmAgentSetOM(String path, Object data)
	{
		tsDmWorkspace ws = dm_ws;
		tsDmVnode node;
		tsOmList list;
		int nLen = 0;

		if (data == null)
		{
			return;
		}

		nLen = String.valueOf(data).getBytes().length;
		if (nLen <= 0)
		{
			tsOmlib.dmOmlibDeleteImplicit(ws.om, path, true);
		}

		tsOmlib.dmOmWrite(ws.om, path, nLen, 0, data, nLen);

		node = tsOmlib.dmOmLibGetNodeProp(ws.om, path);
		if (node != null)
		{
			if (node.type != null)
				tsOmlib.dmOmDeleteMimeList(node.type);

			list = new tsOmList();
			list.data = "text/plain";
			list.next = null;
			node.type = list;
			node.format = FORMAT_CHR;
		}
	}

	public int dmAgentClientInitPackage(tsDmEncoder e)
	{
		tsDmWorkspace ws = dm_ws;

		int res;
		int nNotiEvent;

		tsLib.debugPrint(DEBUG_DM, "");
		res = dmAgentCreatePackageStatus(e);
		if (res != SDM_RET_OK)
		{
			if (res == SDM_BUFFER_SIZE_EXCEEDED)
			{
				ws.endOfMsg = false;
			}
			else
			{
				tsLib.debugPrintException(DEBUG_EXCEPTION, "failed(%d)" + res);
			}
			return SDM_RET_FAILED;
		}

		res = dmAgentCreatePackageResults(e);
		if (res != SDM_RET_OK)
		{
			if (res == SDM_BUFFER_SIZE_EXCEEDED)
			{
				ws.endOfMsg = false;
			}
			else
			{
				tsLib.debugPrintException(DEBUG_EXCEPTION, "failed(%d)" + res);
			}
			return SDM_RET_FAILED;
		}

		nNotiEvent = tsDB.dbGetNotiEvent(ws.appId); // 0512
		if (nNotiEvent > 0)
		{
			res = dmAgentCreatePackageAlert(e, ALERT_SERVER_INITIATED_MGMT);
		}
		else
		{
			res = dmAgentCreatePackageAlert(e, ALERT_CLIENT_INITIATED_MGMT);
		}

		if (res != SDM_RET_OK)
		{
			if (res == SDM_BUFFER_SIZE_EXCEEDED)
			{
				ws.endOfMsg = false;
			}
			else
			{
				tsLib.debugPrintException(DEBUG_EXCEPTION, "failed(%d)" + res);
			}
			return SDM_RET_FAILED;
		}

		res = dmAgentCreatePackageDevInfo(e);
		if (res != SDM_RET_OK)
		{
			if (res == SDM_BUFFER_SIZE_EXCEEDED)
			{
				ws.endOfMsg = false;
			}
			else
			{
				tsLib.debugPrintException(DEBUG_EXCEPTION, "failed(%d)" + res);
			}
			return SDM_RET_FAILED;
		}

		ws.endOfMsg = true;

		return SDM_RET_OK;
	}

	public int dmAgentCreatePackageStatus(tsDmEncoder e)
	{
		tsdmParserStatus status = null;
		tsdmParserStatus tmp;
		tsDmWorkspace ws = dm_ws;
		tsLinkedList.listSetCurrentObj(ws.statusList, 0);

		status = (tsdmParserStatus) tsLinkedList.listGetNextObj(ws.statusList);
		while (status != null)
		{
			e.dmEncAddStatus(status);
			tmp = status;
			status = (tsdmParserStatus) tsLinkedList.listGetNextObj(ws.statusList);
			tsLinkedList.listRemoveObjAtFirst(ws.statusList);
			tsDmHandlecmd.dmDataStDeleteStatus(tmp);
		}
		tsLinkedList.listClearLinkedList(ws.statusList);

		return SDM_RET_OK;
	}

	public int dmAgentCreatePackageResults(tsDmEncoder e)
	{
		tsDmWorkspace ws = dm_ws;
		tsOmTree om = dm_ws.om;
		tsDmParserResults results = null;
		tsDmParserResults tmp;
		tsDmVnode node;
		tsDmParserItem item;
		int datasize, usedsize;
		int cmdsize, remainsize;
		int partialsize = 0;
		int offset = 0;
		boolean partialsend = false;
		char[] buf = null;
		@SuppressWarnings("unused")
		int res = 0;

		tsLinkedList.listSetCurrentObj(ws.resultsList, 0);
		results = (tsDmParserResults) tsLinkedList.listGetNextObj(ws.resultsList);

		if (results == null && ws.dmState != SyncmlState.DM_STATE_CLIENT_INIT_MGMT && ws.nextMsg)
		{
			tsLinkedList.listClearLinkedList(ws.resultsList);
			return SDM_RET_OK;
		}
		while (results != null)
		{
			item = (tsDmParserItem) results.itemlist.item;
			if (item.meta != null)
			{
				if (item.meta.size > 0)
				{
					datasize = item.meta.size;
				}
				else
				{
					datasize = 0;
				}
			}
			else
			{
				datasize = 0;
			}
			node = tsOmlib.dmOmLibGetNodeProp(om, item.source);
			usedsize = tsDmEncoder.dmEncGetBufferSize(e);
			cmdsize = PACKAGE_SIZE_GAP + datasize;
			remainsize = ws.maxMsgSize - usedsize;

			if (remainsize < PACKAGE_SIZE_GAP)
			{
				return SDM_BUFFER_SIZE_EXCEEDED;
			}
			else if (datasize > 0 && remainsize < cmdsize)
			{
				partialsize = datasize;
			}
			else
			{
				partialsize = 0;
			}

			if (partialsize > 0)
			{
				buf = new char[partialsize];
				if (item.meta != null)
				{
					if (item.meta.type != null)
					{
						if (_SYNCML_TS_DM_VERSION_V12_)
						{
							if (item.meta.type.compareTo(SYNCML_MIME_TYPE_TNDS_XML) == 0)
							{
								int nFileId = 0;
								byte[] buftmp = new byte[partialsize];
								nFileId = tsdmDB.dmdbGetFileIdTNDS();
								tsdmDB.dmReadFile(nFileId, offset, partialsize, buftmp);
								String data1 = new String(buftmp);
								item.data = tsDmHandlecmd.dmDataStString2Pcdata(data1.toCharArray());
								if (partialsend)
								{
									item.moredata = 1;
									ws.sendPos += partialsize;
								}
								else
								{
									ws.sendPos = 0;
								}

								e.dmEncAddResults(results);
								tmp = results;
								results = (tsDmParserResults) tsLinkedList.listGetNextObj(ws.resultsList);
								tsLinkedList.listRemoveObjAtFirst(ws.resultsList);
								tsDmHandlecmd.dmDataStDeleteResults(tmp);

								buf = null;
								continue;
							}
						}
					}
				}
				res = (int) tsOmlib.dmOmRead(om, item.source, (int) offset, buf, partialsize);

				if (node != null && node.format == FORMAT_BIN)
				{
					item.data = new tsDmParserPcdata();//
					item.data.type = TYPE_OPAQUE;
					item.data.data = new char[(int) partialsize];

					for (int i = 0; i < partialsize; i++)
						item.data.data[i] = buf[i];
					item.data.size = (int) partialsize;
				}
				else
				{
					item.data = tsDmHandlecmd.dmDataStString2Pcdata(buf);
				}
			}

			if (partialsend)
			{
				item.moredata = 1;
				ws.sendPos += partialsize;
			}
			else
			{
				ws.sendPos = 0;
			}

			e.dmEncAddResults(results);
			tmp = results;
			results = (tsDmParserResults) tsLinkedList.listGetNextObj(ws.resultsList);
			tsLinkedList.listRemoveObjAtFirst(ws.resultsList);
			tsDmHandlecmd.dmDataStDeleteResults(tmp);

			buf = null;
		}
		tsLinkedList.listClearLinkedList(ws.resultsList);

		return SDM_RET_OK;
	}

	public int dmAgentCreatePackageAlert(tsDmEncoder e, String data)
	{
		tsDmParserAlert alert = null;
		tsDmWorkspace ws = dm_ws;

		alert = dmBuildcmd.dmBuildCmdAlert(ws, data);
		e.dmEncAddAlert(alert);
		tsDmHandlecmd.dmDataStDeleteAlert(alert);

		return SDM_RET_OK;
	}

	int dmAgentCreatePackageDevInfo(tsDmEncoder e)
	{
		tsDmWorkspace ws = dm_ws;
		tsDmParserReplace rep = null;
		tsLinkedList list = null;
		tsOmTree om = ws.om;
		tsDmVnode node;

		list = tsLinkedList.listCreateLinkedList();
		node = tsOmlib.dmOmLibGetNodeProp(om, DEVINFO_LANG_PATH);
		if (node != null)
			DM_MAKE_REP_ITEM(om, list, DEVINFO_LANG_PATH, node.size);

		node = tsOmlib.dmOmLibGetNodeProp(om, DEVINFO_DMV_PATH);
		if (node != null)
			DM_MAKE_REP_ITEM(om, list, DEVINFO_DMV_PATH, node.size);

		node = tsOmlib.dmOmLibGetNodeProp(om, DEVINFO_MOD_PATH);
		if (node != null)
			DM_MAKE_REP_ITEM(om, list, DEVINFO_MOD_PATH, node.size);

		node = tsOmlib.dmOmLibGetNodeProp(om, DEVINFO_MAN_PATH);
		if (node != null)
			DM_MAKE_REP_ITEM(om, list, DEVINFO_MAN_PATH, node.size);

		node = tsOmlib.dmOmLibGetNodeProp(om, DEVINFO_DEVID_PATH);
		if (node != null)
			DM_MAKE_REP_ITEM(om, list, DEVINFO_DEVID_PATH, node.size);

		rep = dmBuildcmd.dmBuildCmdReplace(ws, list);

		tsLinkedList.listFreeLinkedList(list);
		e.dmEncAddReplace(rep);
		tsDmHandlecmd.dmDataStDeleteReplace(rep);

		return SDM_RET_OK;
	}

	public void DM_MAKE_REP_ITEM(tsOmTree om, tsLinkedList list, String path, int node_size)
	{
		char[] buf = new char[(int) node_size];
		tsDmParserItem item;
		if (tsOmlib.dmOmRead(om, path, 0, buf, node_size) < 0)
		{
			tsLib.debugPrint(DEBUG_DM, "dmOmRead failed");
		}
		item = new tsDmParserItem();
		item.source = path;
		item.data = tsDmHandlecmd.dmDataStString2Pcdata(buf);
		tsLinkedList.listAddObjAtLast(list, item);
	}

	public int dmAgentCreatePackageGenericAlert(tsDmEncoder e, String data)
	{
		tsDmParserAlert alert;
		tsDmWorkspace ws = dm_ws;

		tsLib.debugPrint(DEBUG_DM, "");
		alert = dmBuildcmd.dmBuildCmdGenericAlert(ws, data);
		e.dmEncAddAlert(alert);
		tsDmHandlecmd.dmDataStDeleteAlert(alert);

		return SDM_RET_OK;
	}

	public int dmAgentCreatePackageReportGenericAlert(tsDmEncoder e, String data)
	{
		tsDmParserAlert alert;
		tsDmWorkspace ws = dm_ws;

		alert = dmBuildcmd.dmBuildCmdGenericAlertReport(ws, data);
		e.dmEncAddAlert(alert);
		tsDmHandlecmd.dmDataStDeleteAlert(alert);

		return SDM_RET_OK;
	}

	public boolean dmAgentVefifyAtomicCmd(dmAgent cmd)
	{
		boolean res = true;

		if (cmd.cmd.compareTo("Atomic_Start") == 0)
		{
			return false;
		}
		else if (cmd.cmd.compareTo("GET") == 0)
		{
			return false;
		}
		else
		{
			tsLib.debugPrint(DEBUG_DM, "");
			return res;
		}
	}

	public int dmAgentCmdAtomicBlock(tsDmParserAtomic atomic, tsLinkedList list)
	{
		tsDmWorkspace ws = dm_ws;
		dmAgent cmd;
		tsDmParserItem item = null;
		tsdmParserStatus status = null;
		boolean isProcess = true;
		boolean res;
		int r = 0;
		int num = 1;

		ws.tmpItem = null;
		tsLinkedList.listSetCurrentObj(list, 0);
		cmd = (dmAgent) tsLinkedList.listGetNextObj(list);

		while (cmd != null)
		{
			res = dmAgentVefifyAtomicCmd(cmd);
			if (!res)
			{
				isProcess = false;
			}
			cmd = (dmAgent) tsLinkedList.listGetNextObj(list);
		}

		if (isProcess)
		{
			status = dmBuildcmd.dmBuildCmdStatus(ws, atomic.cmdid, "Atomic", null, null, STATUS_OK);
			ws.atomicStep = SyncmlAtomicStep.ATOMIC_NONE;
		}
		else
		{
			status = dmBuildcmd.dmBuildCmdStatus(ws, atomic.cmdid, "Atomic", null, null, STATUS_ATOMIC_FAILED);
			ws.atomicStep = SyncmlAtomicStep.ATOMIC_STEP_ROLLBACK;
		}

		tsLinkedList.listAddObjAtLast(ws.statusList, status);

		tsLinkedList.listSetCurrentObj(list, 0);
		cmd = (dmAgent) tsLinkedList.listGetNextObj(list);
		while (cmd != null)
		{
			if (ws.atomicFlag)
			{
				if (cmd.cmd.compareTo("Get") == 0)
				{
					item = (tsDmParserItem) cmd.get.itemlist.item;
					if (item.target != null)
					{
						status = dmBuildcmd.dmBuildCmdStatus(ws, cmd.get.cmdid, CMD_GET, null, item.target, STATUS_NOT_EXECUTED);
					}
					else
					{
						status = dmBuildcmd.dmBuildCmdStatus(ws, cmd.get.cmdid, CMD_GET, null, null, STATUS_NOT_FOUND);
					}

					tsLinkedList.listAddObjAtLast(ws.statusList, status);
				}
				else if (cmd.cmd.compareTo("Exec") == 0)
				{
					item = (tsDmParserItem) cmd.exec.itemlist.item;
					if (item.target != null)
					{
						status = dmBuildcmd.dmBuildCmdStatus(ws, (cmd.exec.cmdid), CMD_EXEC, null, item.target, STATUS_NOT_EXECUTED);
					}
					else
					{
						status = dmBuildcmd.dmBuildCmdStatus(ws, (cmd.exec.cmdid), CMD_GET, null, null, STATUS_NOT_FOUND);
					}

					tsLinkedList.listAddObjAtLast(ws.statusList, status);
				}
				else if (cmd.cmd.compareTo("Add") == 0)
				{
					item = (tsDmParserItem) cmd.addCmd.itemlist.item;
					if (item.target != null)
					{
						status = dmBuildcmd.dmBuildCmdStatus(ws, (cmd.addCmd.cmdid), CMD_ADD, null, item.target, STATUS_NOT_EXECUTED);
					}
					else
					{
						status = dmBuildcmd.dmBuildCmdStatus(ws, (cmd.addCmd.cmdid), CMD_GET, null, null, STATUS_NOT_FOUND);
					}

					tsLinkedList.listAddObjAtLast(ws.statusList, status);
				}
				else if (cmd.cmd.compareTo("Delete") == 0)
				{
					item = (tsDmParserItem) cmd.deleteCmd.itemlist.item;

					if (item.target != null)
					{
						status = dmBuildcmd.dmBuildCmdStatus(ws, (cmd.deleteCmd.cmdid), CMD_DELETE, null, item.target, STATUS_NOT_EXECUTED);
					}
					else
					{
						status = dmBuildcmd.dmBuildCmdStatus(ws, (cmd.deleteCmd.cmdid), CMD_GET, null, null, STATUS_NOT_FOUND);
					}
					tsLinkedList.listAddObjAtLast(ws.statusList, status);
				}
				else if (cmd.cmd.compareTo("Replace") == 0)
				{
					item = (tsDmParserItem) cmd.replaceCmd.itemlist.item;
					if (item.target != null)
					{
						status = dmBuildcmd.dmBuildCmdStatus(ws, (cmd.replaceCmd.cmdid), CMD_REPLACE, null, item.target, STATUS_NOT_EXECUTED);
					}
					else
					{
						status = dmBuildcmd.dmBuildCmdStatus(ws, (cmd.replaceCmd.cmdid), CMD_GET, null, null, STATUS_NOT_FOUND);
					}
					tsLinkedList.listAddObjAtLast(ws.statusList, status);
				}
				else if (cmd.cmd.compareTo("Copy") == 0)
				{
					item = (tsDmParserItem) cmd.copyCmd.itemlist.item;
					if (item.target != null)
					{
						status = dmBuildcmd.dmBuildCmdStatus(ws, (cmd.copyCmd.cmdid), CMD_COPY, null, item.target, STATUS_NOT_EXECUTED);
					}
					else
					{
						status = dmBuildcmd.dmBuildCmdStatus(ws, (cmd.copyCmd.cmdid), CMD_GET, null, null, STATUS_NOT_FOUND);
					}
					tsLinkedList.listAddObjAtLast(ws.statusList, status);
				}
				else
				{
					tsLib.debugPrint(DEBUG_DM, "unknown command");
				}
			}
			else
			{
				if (cmd.cmd.compareTo("Get") == 0)
				{
					r = dmAgentCmdGet(cmd.get, true);
					if (r != SDM_RET_OK)
					{
						tsLib.debugPrintException(DEBUG_EXCEPTION, "get failed");
						return SDM_RET_FAILED;
					}
					num++;
				}
				else if (cmd.cmd.compareTo("Exec") == 0)
				{
					r = dmAgentCmdExec(cmd.exec, true, status);

					if (status.data.compareTo(STATUS_ATOMIC_FAILED) == 0)
					{
						ws.atomicFlag = true; // command failed
					}

					if (r != SDM_RET_OK)
					{
						tsLib.debugPrintException(DEBUG_EXCEPTION, "exec failed");
						return SDM_RET_FAILED;
					}
					num++;
				}
				else if (cmd.cmd.compareTo("Add") == 0)
				{
					r = dmAgentCmdAdd(cmd.addCmd, true, status);

					if (status.data.compareTo(STATUS_ATOMIC_FAILED) == 0)
					{
						ws.atomicFlag = true;
					}

					if (r != SDM_RET_OK)
					{
						tsLib.debugPrintException(DEBUG_EXCEPTION, "Add failed");
						return SDM_RET_FAILED;
					}
					num++;
				}
				else if (cmd.cmd.compareTo("Delete") == 0)
				{
					r = dmAgentCmdDelete(cmd.deleteCmd, true, status);

					if (status.data.compareTo(STATUS_ATOMIC_FAILED) == 0)
					{
						ws.atomicFlag = true; // command failed
					}

					if (r != SDM_RET_OK)
					{
						tsLib.debugPrintException(DEBUG_EXCEPTION, "Delete failed");
						return SDM_RET_FAILED;
					}
					num++;
				}
				else if (cmd.cmd.compareTo("Replace") == 0)
				{
					r = dmAgentCmdReplace(cmd.replaceCmd, true, status);

					if (status.data.compareTo(STATUS_ATOMIC_FAILED) == 0)
					{
						ws.atomicFlag = true; // command failed
					}

					if (r != SDM_RET_OK)
					{
						tsLib.debugPrintException(DEBUG_EXCEPTION, " : Replace failed");
						return SDM_RET_FAILED;
					}
					num++;
				}
				else if (cmd.cmd.compareTo("Copy") == 0)
				{
					r = dmAgentCmdCopy(cmd.copyCmd, true, status);

					if (status.data.compareTo(STATUS_ATOMIC_FAILED) == 0)
					{
						ws.atomicFlag = true; // command failed
					}

					if (r != SDM_RET_OK)
					{
						tsLib.debugPrintException(DEBUG_EXCEPTION, "Copy failed");
						return SDM_RET_FAILED;
					}
					num++;
				}
				else if (cmd.cmd.compareTo("Atomic_Start") == 0)
				{
					ws.atomicFlag = true;
					dmAgentCmdAtomicBlock(cmd.atomic, cmd.atomic.itemlist);
				}
				else
				{
					tsLib.debugPrintException(DEBUG_EXCEPTION, "unknown command");
				}
			}
			cmd = (dmAgent) tsLinkedList.listGetNextObj(list);
		}
		tsLinkedList.listClearLinkedList(atomic.itemlist);

		return num;
	}

	public int dmAgentCmdGet(tsDmParserGet get, boolean isAtomic)
	{
		tsDmWorkspace ws = dm_ws;
		tsOmTree om = ws.om;
		tsDmParserItem item = null;
		tsdmParserStatus status = null;
		tsDmParserResults results = null;
		String type = null;
		String format = null;
		String Resultbuf = null;
		String[] chlist = new String[OM_MAX_CHILD_NUM];
		tsList cur = null;

		tsDmVnode node = null;
		boolean process = true;
		int bufsize = 0;
		int res, i;
		char[] pData = null;

		process = dmAgentCmdUicAlert();

		cur = get.itemlist;
		while (cur != null)
		{
			item = (tsDmParserItem) cur.item;

			if (ws.serverAuthState != AUTH_STATE_OK)
			{
				if (item.target != null)
				{
					status = dmBuildcmd.dmBuildCmdStatus(ws, (get.cmdid), CMD_GET, null, item.target, ws.statusReturnCode);
				}
				else
				{
					status = dmBuildcmd.dmBuildCmdStatus(ws, (get.cmdid), CMD_GET, null, null, ws.statusReturnCode);
				}
				tsLinkedList.listAddObjAtLast(ws.statusList, status);
				cur = cur.next;
				continue;
			}

			if (item.target == null)
			{

				status = dmBuildcmd.dmBuildCmdStatus(ws, (get.cmdid), CMD_GET, null, null, STATUS_NOT_FOUND);
				tsLinkedList.listAddObjAtLast(ws.statusList, status);
				cur = cur.next;
				continue;
			}

			if (!process)
			{
				if (item.target != null)
				{
					status = dmBuildcmd.dmBuildCmdStatus(ws, (get.cmdid), CMD_GET, null, item.target, STATUS_NOT_EXECUTED);
				}
				else
				{
					status = dmBuildcmd.dmBuildCmdStatus(ws, (get.cmdid), CMD_GET, null, null, STATUS_NOT_EXECUTED);
				}

				if (status != null)
				{
					tsLinkedList.listAddObjAtLast(ws.statusList, status);
				}
				cur = cur.next;
				continue;
			}

			if (isAtomic)
			{
				if (item.target != null)
				{
					status = dmBuildcmd.dmBuildCmdStatus(ws, (get.cmdid), CMD_GET, item.target, null, STATUS_NOT_EXECUTED);
				}
				else
				{
					status = dmBuildcmd.dmBuildCmdStatus(ws, (get.cmdid), CMD_GET, null, null, STATUS_NOT_EXECUTED);
				}

				if (status != null)
				{
					tsLinkedList.listAddObjAtLast(ws.statusList, status);
				}
				cur = cur.next;
				continue;
			}

			if (tsLib.libStrstr(item.target, "?") != null)
			{
				res = dmAgentCmdProp(CMD_GET, item, get);
				cur = cur.next;
				continue;
			}

			node = tsOmlib.dmOmLibGetNodeProp(om, item.target);
			if (node == null)
			{
				status = dmBuildcmd.dmBuildCmdStatus(ws, (get.cmdid), CMD_GET, null, item.target, STATUS_NOT_FOUND);
				tsLinkedList.listAddObjAtLast(ws.statusList, status);
				cur = cur.next;
				continue;
			}

			if (!dmAgentIsAccessibleNode(item.target))
			{
				status = dmBuildcmd.dmBuildCmdStatus(ws, (get.cmdid), CMD_GET, null, item.target, STATUS_COMMAND_NOT_ALLOWED);
				tsLinkedList.listAddObjAtLast(ws.statusList, status);
				cur = cur.next;
				continue;
			}
			if (!tsOmlib.dmOmCheckAcl(om, node, OMACL_GET))
			{
				status = dmBuildcmd.dmBuildCmdStatus(ws, (get.cmdid), CMD_GET, null, item.target, STATUS_PERMISSION_DENIED);
				tsLinkedList.listAddObjAtLast(ws.statusList, status);
				cur = cur.next;
				continue;
			}

			if (node.vaddr < 0 && node.size <= 0)
			{
				status = dmBuildcmd.dmBuildCmdStatus(ws, (get.cmdid), CMD_GET, null, item.target, STATUS_OK);
				tsLinkedList.listAddObjAtLast(ws.statusList, status);

				res = tsOmlib.dmOmGetChild(om, item.target, chlist, OM_MAX_CHILD_NUM);

				// MOD for Meta format null Node Get
				format = tsOmList.dmOmGetFormatString(node.format);
				if (node.type != null && node.type.data != null)
				{
					type = String.valueOf(node.type.data);
				}
				else
				{
					type = null;
				}

				if (res > 0)
				{
					Resultbuf = chlist[0];
					for (i = 1; i < res; i++)
					{
						Resultbuf = Resultbuf.concat("/");
						Resultbuf = Resultbuf.concat(chlist[i]);
					}
				}
				else
				{
					Resultbuf = "";
				}
				results = dmBuildcmd.dmBuildCmdDetailResults(ws, (get.cmdid), item.target, format, type, 0, Resultbuf.toCharArray());
				tsLinkedList.listAddObjAtLast(ws.resultsList, results);
			}
			else
			{
				if (node.size > ws.serverMaxObjSize)
				{
					status = dmBuildcmd.dmBuildCmdStatus(ws, (get.cmdid), CMD_GET, null, item.target, STATUS_REQUEST_ENTITY_TOO_LARGE);
					tsLinkedList.listAddObjAtLast(ws.statusList, status);
					cur = cur.next;
					continue;
				}

				bufsize = node.size;

				format = tsOmList.dmOmGetFormatString(node.format);

				pData = new char[node.size];
				if (pData == null)
				{
					status = dmBuildcmd.dmBuildCmdStatus(ws, (get.cmdid), CMD_GET, null, item.target, STATUS_NOT_EXECUTED);
					if (status != null)
					{
						tsLinkedList.listAddObjAtLast(ws.statusList, status);
					}
					cur = cur.next;
					continue;
				}

				status = dmBuildcmd.dmBuildCmdStatus(ws, (get.cmdid), CMD_GET, null, item.target, STATUS_OK);

				tsLinkedList.listAddObjAtLast(ws.statusList, status);

				if (node.type != null && node.type.data != null)
				{
					type = String.valueOf(node.type.data);
				}
				else
				{
					type = null;
				}

				res = tsOmlib.dmOmRead(om, item.target, 0, pData, node.size);
				results = dmBuildcmd.dmBuildCmdDetailResults(ws, (get.cmdid), item.target, format, type, bufsize, pData);

				tsLinkedList.listAddObjAtLast(ws.resultsList, results);
				
				// Add get data log(target & data)
				if(pData != null)
				{
					tsLib.debugPrint(DEBUG_DM, "item.target = " + item.target);
					tsLib.debugPrint(DEBUG_DM, "item.data = " + new String(pData));
				}

				format = null;
				pData = null;
			}
			cur = cur.next;
		}
		return SDM_RET_OK;
	}

	public boolean dmAgentCmdUicAlert()
	{
		tsDmWorkspace ws = dm_ws;
		tsdmParserStatus status = null;

		if (ws.uicAlert != null)
		{
			if (ws.uicFlag == SyncmlUICFlag.UIC_TRUE || ws.uicFlag == SyncmlUICFlag.UIC_NONE)
			{
				status = dmBuildcmd.dmBuildCmdStatus(ws, (ws.uicAlert.cmdid), CMD_ALERT, null, null, STATUS_OK);
				if (ws.uicData != null)
				{
					status.itemlist = ws.uicData;
				}
			}
			else if (ws.uicFlag == SyncmlUICFlag.UIC_FALSE)
			{
				status = dmBuildcmd.dmBuildCmdStatus(ws, (ws.uicAlert.cmdid), CMD_ALERT, null, null, STATUS_NOT_MODIFIED);
				if (ws.uicData != null)
				{
					status.itemlist = ws.uicData;
				}
			}
			else if (ws.uicFlag == SyncmlUICFlag.UIC_CANCELED)
			{
				status = dmBuildcmd.dmBuildCmdStatus(ws, (ws.uicAlert.cmdid), CMD_ALERT, null, null, STATUS_OPERATION_CANCELLED);
				if (ws.uicData != null)
				{
					status.itemlist = ws.uicData;
				}
			}
			else
			{
				status = dmBuildcmd.dmBuildCmdStatus(ws, (ws.uicAlert.cmdid), CMD_ALERT, null, null, STATUS_NOT_EXECUTED);
				if (ws.uicData != null)
				{
					status.itemlist = ws.uicData;
				}
			}
			tsLinkedList.listAddObjAtLast(ws.statusList, status);
			tsDmHandlecmd.dmDataStDeleteAlert(ws.uicAlert);
			ws.uicData = null;
			ws.uicAlert = null;
			status = null;

		}
		if (ws.uicFlag == SyncmlUICFlag.UIC_TRUE || ws.uicFlag == SyncmlUICFlag.UIC_NONE)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	public int dmAgentCmdProp(String cmd, tsDmParserItem item, Object p)
	{
		tsDmWorkspace ws = dm_ws;
		tsdmParserStatus status;
		char[] nodename = null;
		char[] prop = null;
		String ptr;
		int ret = SDM_RET_OK;

		ptr = item.target;

		if (cmd.compareTo("Get") == 0)
		{
			tsDmParserGet get = (tsDmParserGet) p;

			nodename = new char[ptr.length()];
			ptr = tsLib.libStrsplit(ptr.toCharArray(), '?', nodename); // node path
			tsLib.debugPrint(DEBUG_DM, "ptr = " + ptr);
			if (ptr == null)
			{
				status = dmBuildcmd.dmBuildCmdStatus(ws, (get.cmdid), cmd, null, item.target, STATUS_COMMAND_NOT_ALLOWED);
				tsLinkedList.listAddObjAtLast(ws.statusList, status);
				return SDM_RET_OK;
			}
			prop = new char[ptr.length()];
			ptr = tsLib.libStrsplit(ptr.toCharArray(), '=', prop); // prop = 'prop', ptr = options...
			tsLib.debugPrint(DEBUG_DM, "ptr = " + ptr);
			if (ptr == null)
			{
				status = dmBuildcmd.dmBuildCmdStatus(ws, (get.cmdid), cmd, null, item.target, STATUS_COMMAND_NOT_ALLOWED);
				tsLinkedList.listAddObjAtLast(ws.statusList, status);
				return SDM_RET_OK;
			}

			ret = dmAgentCmdPropGet(ws, item, ptr, prop, nodename, p);
		}
		else if (cmd.compareTo("Replace") == 0)
		{
			tsDmParserReplace replace = (tsDmParserReplace) p;
			nodename = new char[ptr.length()];
			ptr = tsLib.libStrsplit(ptr.toCharArray(), '?', nodename); // node path
			if (ptr.length() == 0)
			{
				status = dmBuildcmd.dmBuildCmdStatus(ws, (replace.cmdid), cmd, null, item.target, STATUS_COMMAND_NOT_ALLOWED);
				tsLinkedList.listAddObjAtLast(ws.statusList, status);
				return SDM_RET_OK;
			}
			tsLib.debugPrint(DEBUG_DM, String.valueOf(ptr));
			prop = new char[ptr.length()];
			ptr = tsLib.libStrsplit(ptr.toCharArray(), '=', prop); // prop = 'prop', ptr = options...
			tsLib.debugPrint(DEBUG_DM, String.valueOf(prop) + ":" + String.valueOf(ptr));

			if (tsLib.isEmpty(ptr))
			{
				status = dmBuildcmd.dmBuildCmdStatus(ws, (replace.cmdid), cmd, null, item.target, STATUS_COMMAND_NOT_ALLOWED);
				tsLinkedList.listAddObjAtLast(ws.statusList, status);
				return SDM_RET_OK;
			}
			ret = dmAgentCmdPropReplace(ws, item, ptr, nodename, p);
		}
		else
		{
			tsDmParserGet get = (tsDmParserGet) p;

			status = dmBuildcmd.dmBuildCmdStatus(ws, (get.cmdid), cmd, null, item.target, STATUS_COMMAND_NOT_ALLOWED);
			tsLinkedList.listAddObjAtLast(ws.statusList, status);
			return SDM_RET_OK;
		}
		return ret;
	}

	public int dmAgentCmdPropGet(tsDmWorkspace ws, tsDmParserItem item, String ptr, char[] prop, char[] pNodeName, Object pkg)
	{
		tsOmTree om = ws.om;
		tsDmParserGet get = (tsDmParserGet) pkg;
		tsdmParserStatus status;
		tsDmParserResults results;
		tsDmVnode node;
		String outbuf = null;
		char[] chreBuf = null;
		int nFileId = 0;
		boolean ret = false;

		if (_SYNCML_TS_DM_VERSION_V12_)
			nFileId = tsdmDB.dmdbGetFileIdTNDS();

		String nodename = tsLib.libString(pNodeName);
		node = tsOmlib.dmOmLibGetNodeProp(om, nodename);

		if (node == null)
		{
			status = dmBuildcmd.dmBuildCmdStatus(ws, (get.cmdid), CMD_GET, null, item.target, STATUS_NOT_FOUND);
			tsLinkedList.listAddObjAtLast(ws.statusList, status);
			return SDM_RET_OK;
		}
		String propname = tsLib.libString(prop);
		if (propname.compareTo("list") == 0)
		{
			/* ADD : Improve Checking inaccessible node(?list=StructData) */
			if (!dmAgentIsAccessibleNode(item.target))
			{
				status = dmBuildcmd.dmBuildCmdStatus(ws, (get.cmdid), CMD_GET, null, item.target, STATUS_COMMAND_NOT_ALLOWED);
				tsLinkedList.listAddObjAtLast(ws.statusList, status);
				return SDM_RET_OK;
			}
			// MOD : improved Get operating
			else if (!tsOmlib.dmOmCheckAcl(om, node, OMACL_GET))
			{
				status = dmBuildcmd.dmBuildCmdStatus(ws, (get.cmdid), CMD_GET, null, item.target, STATUS_PERMISSION_DENIED);
				tsLinkedList.listAddObjAtLast(ws.statusList, status);
				return SDM_RET_OK;
			}

			if (ptr.compareTo("Struct") == 0)
			{
				dmAgentCmdPropGetStruct(get, node, false);
			}
			else if (ptr.compareTo("StructData") == 0)
			{
				dmAgentCmdPropGetStruct(get, node, true);
			}
			else if (ptr.contains("TNDS"))
			{
				if (_SYNCML_TS_DM_VERSION_V12_)
				{
					ret = dmAgentCmdPropGetTnds(get, om, node, ptr);
					if (!ret)
					{
						tsdmDB.dmdbDeleteFile(nFileId);
						status = dmBuildcmd.dmBuildCmdStatus(dm_ws, (get.cmdid), CMD_GET, null, item.target, STATUS_NOT_FOUND);
						tsLinkedList.listAddObjAtLast(dm_ws.statusList, status);
						return SDM_RET_OK;
					}
				}
			}

			status = dmBuildcmd.dmBuildCmdStatus(ws, (get.cmdid), CMD_GET, null, item.target, STATUS_OK);
			tsLinkedList.listAddObjAtLast(ws.statusList, status);
			return SDM_RET_OK;
		}

		/* MOD : Improve ACL Check(Merge From KDS) */
		if (!tsOmlib.dmOmCheckAcl(om, node, OMACL_GET))
		{
			status = dmBuildcmd.dmBuildCmdStatus(ws, (get.cmdid), CMD_GET, null, item.target, STATUS_PERMISSION_DENIED);
			tsLinkedList.listAddObjAtLast(ws.statusList, status);
			return SDM_RET_OK;
		}

		if (ptr.compareTo("ACL") == 0)
		{
			status = dmBuildcmd.dmBuildCmdStatus(ws, (get.cmdid), CMD_GET, null, item.target, STATUS_OK);
			tsLinkedList.listAddObjAtLast(ws.statusList, status);

			outbuf = dmAgentGetAclStr(node.acl, item);
			if (outbuf != null)
				chreBuf = outbuf.toCharArray();

			/* IOT Issue */
			results = dmBuildcmd.dmBuildCmdDetailResults(ws, (get.cmdid), item.target, "chr", "text/plain", 0, chreBuf);
			tsLinkedList.listAddObjAtLast(ws.resultsList, results);

			outbuf = null;
			return SDM_RET_OK;
		}
		else if (ptr.compareTo("Format") == 0)
		{
			outbuf = tsOmList.dmOmGetFormatString(node.format);
			if (outbuf != null)
				chreBuf = outbuf.toCharArray();

			status = dmBuildcmd.dmBuildCmdStatus(ws, (get.cmdid), CMD_GET, null, item.target, STATUS_OK);
			tsLinkedList.listAddObjAtLast(ws.statusList, status);

			/* IOT Issue */
			results = dmBuildcmd.dmBuildCmdDetailResults(ws, (get.cmdid), item.target, "chr", "text/plain", 0, chreBuf);
			tsLinkedList.listAddObjAtLast(ws.resultsList, results);

			outbuf = null;
		}
		else if (ptr.compareTo("Type") == 0)
		{
			status = dmBuildcmd.dmBuildCmdStatus(ws, (get.cmdid), CMD_GET, null, item.target, STATUS_OK);
			tsLinkedList.listAddObjAtLast(ws.statusList, status);

			if (node.type != null && node.type.data != null)
			{
				outbuf = String.valueOf(node.type.data);
				results = dmBuildcmd.dmBuildCmdDetailResults(ws, (get.cmdid), item.target, "chr", outbuf, 0, outbuf.toCharArray()); // __FTA_DM_1.2_ISSUE__
				tsLinkedList.listAddObjAtLast(ws.resultsList, results);

				outbuf = null;
			}
			else
			{
				outbuf = "null";
				results = dmBuildcmd.dmBuildCmdDetailResults(ws, (get.cmdid), item.target, null, null, 0, outbuf.toCharArray()); // __FTA_DM_1.2_ISSUE__
				tsLinkedList.listAddObjAtLast(ws.resultsList, results);

				outbuf = null;
			}
			/* End of FTA Issue */
		}
		else if (ptr.compareTo("Size") == 0)
		{
			if (node.vaddr >= 0 && node.size > 0)
			{
				status = dmBuildcmd.dmBuildCmdStatus(ws, (get.cmdid), CMD_GET, null, item.target, STATUS_OK);
				tsLinkedList.listAddObjAtLast(ws.statusList, status);

				outbuf = String.valueOf(node.size - 1);
				/* IOT Issue */
				results = dmBuildcmd.dmBuildCmdDetailResults(ws, (get.cmdid), item.target, "chr", "text/plain", 0, outbuf.toCharArray());
				tsLinkedList.listAddObjAtLast(ws.resultsList, results);

				outbuf = null;
			}
			else
			{
				status = dmBuildcmd.dmBuildCmdStatus(ws, (get.cmdid), CMD_GET, null, item.target, STATUS_OPTIONAL_FEATURE_NOT_SUPPORTED);
				tsLinkedList.listAddObjAtLast(ws.statusList, status);
			}
		}
		else if (ptr.compareTo("Name") == 0)
		{
			status = dmBuildcmd.dmBuildCmdStatus(ws, (get.cmdid), CMD_GET, null, item.target, STATUS_OK);
			tsLinkedList.listAddObjAtLast(ws.statusList, status);

			outbuf = node.name;

			/* IOT Issue */
			results = dmBuildcmd.dmBuildCmdDetailResults(ws, (get.cmdid), item.target, "chr", "text/plain", 0, outbuf.toCharArray());
			tsLinkedList.listAddObjAtLast(ws.resultsList, results);

			outbuf = null;
		}
		else if (ptr.compareTo("Title") == 0)
		{
			status = dmBuildcmd.dmBuildCmdStatus(ws, (get.cmdid), CMD_GET, null, item.target, STATUS_OK);
			tsLinkedList.listAddObjAtLast(ws.statusList, status);

			if (node.title != null)
			{
				outbuf = node.title;
			}
			else
			{
				outbuf = "";
			}
			/* IOT Issue */
			results = dmBuildcmd.dmBuildCmdDetailResults(ws, (get.cmdid), item.target, "chr", "text/plain", 0, outbuf.toCharArray());
			tsLinkedList.listAddObjAtLast(ws.resultsList, results);

			outbuf = null;
		}
		else
		{
			status = dmBuildcmd.dmBuildCmdStatus(ws, (get.cmdid), CMD_GET, null, item.target, STATUS_COMMAND_NOT_ALLOWED);
			tsLinkedList.listAddObjAtLast(ws.statusList, status);
			return SDM_RET_OK;
		}

		return SDM_RET_OK;
	}

	public void dmAgentCmdPropGetStruct(tsDmParserGet get, tsDmVnode node, boolean makedata)
	{
		tsDmWorkspace ws = dm_ws;
		tsOmTree om = ws.om;
		tsDmParserResults results;
		String name = null, format = null;
		char[] data = null;
		tsDmVnode cur = null;
		@SuppressWarnings("unused")
		int bufsize, res;
		int datasize = 0;// [16];

		if (node == null)
		{
			return;
		}

		cur = node.childlist;

		name = dmAgentGetPathFromNode(ws.om, node);

		if (makedata)
		{
			if (node.vaddr >= 0 && node.size > 0)
			{
				if (node.format > 0)
				{
					format = tsOmList.dmOmGetFormatString(node.format);
				}
				else
				{
					format = null;
				}
				// get data...
				bufsize = node.size;
				data = new char[bufsize];

				res = (int) tsOmlib.dmOmRead(om, name, 0, data, bufsize);
				datasize = (int) node.size;
				// end.
			}
			else
			{
				data = null; // ??
				format = tsOmList.dmOmGetFormatString(node.format);
			}
		}
		else
		{
			data = null;
			format = tsOmList.dmOmGetFormatString(node.format);
		}

		if (dmAgentIsAccessibleNode(node.name))
		{
			results = dmBuildcmd.dmBuildCmdDetailResults(ws, (get.cmdid), name, format, null, datasize, data);
			tsLinkedList.listAddObjAtLast(ws.resultsList, results);
		}
		name = null;
		format = null;
		data = null;

		while (cur != null)
		{
			dmAgentCmdPropGetStruct(get, cur, makedata);
			cur = cur.next;
		}
	}

	public String dmAgentGetPathFromNode(tsOmTree om, tsDmVnode node)
	{
		String outbuf;
		String[] buf = new String[10];
		int level = 0, i;
		tsDmVnode cur = node;
		String pName = null;

		cur = tsOmlib.dmOmvfsGetParent(om.vfs, om.vfs.root, node);
		while (cur != null && cur != om.vfs.root)
		{
			buf[level] = cur.name;
			level++;
			cur = tsOmlib.dmOmvfsGetParent(om.vfs, om.vfs.root, cur);
		}

		if (cur == null)
		{
			outbuf = ".";
			pName = outbuf;
			return pName;
		}

		outbuf = "./";
		for (i = level - 1; i >= 0; i--)
		{
			outbuf = outbuf.concat(buf[i]);
			outbuf = outbuf.concat("/");
		}
		if ((outbuf.compareTo("/") != 0) || (outbuf.compareTo("./") != 0))
		{
			outbuf = outbuf.concat(node.name);
		}

		pName = outbuf;
		return pName;
	}

	public int dmAgentCmdPropReplace(tsDmWorkspace ws, tsDmParserItem item, String ptr, char[] pNodeName, Object pkg)
	{
		tsOmTree om = ws.om;
		tsDmParserReplace replace = (tsDmParserReplace) pkg;
		tsdmParserStatus status;
		tsDmVnode node;
		String outbuf = null;
		tsOmList acllist = null;

		String szNodeName = tsLib.libString(pNodeName);
		node = tsOmlib.dmOmLibGetNodeProp(om, szNodeName);
		if (node == null)
		{
			tsLib.debugPrint(DEBUG_DM, "!node");

			status = dmBuildcmd.dmBuildCmdStatus(ws, (replace.cmdid), CMD_REPLACE, null, item.target, STATUS_NOT_FOUND);
			tsLinkedList.listAddObjAtLast(ws.statusList, status);
			return SDM_RET_OK;
		}

		if (dmAgentIsPermanentNode(om, szNodeName))
		{
			status = dmBuildcmd.dmBuildCmdStatus(ws, (replace.cmdid), CMD_REPLACE, null, item.target, STATUS_COMMAND_NOT_ALLOWED);
			tsLinkedList.listAddObjAtLast(ws.statusList, status);
			return SDM_RET_OK;
		}

		if (!tsOmlib.dmOmCheckAcl(om, node, OMACL_REPLACE))
		{
			tsLib.debugPrint(DEBUG_DM, "OMACL_REPLACE");
			status = dmBuildcmd.dmBuildCmdStatus(ws, (replace.cmdid), CMD_REPLACE, null, item.target, STATUS_PERMISSION_DENIED);
			tsLinkedList.listAddObjAtLast(ws.statusList, status);
			return SDM_RET_OK;
		}

		if (ptr.compareTo("ACL") == 0)
		{
			tsLib.debugPrint(DEBUG_DM, "ACL");

			/* MOD : Improve ACL Check(Merge From KDS) */
			if (node.format != FORMAT_NODE)
			{
				if (!tsOmlib.dmOmCheckAcl(om, node.ptParentNode, OMACL_REPLACE))
				{
					status = dmBuildcmd.dmBuildCmdStatus(ws, (replace.cmdid), CMD_REPLACE, null, item.target, STATUS_PERMISSION_DENIED);
					tsLinkedList.listAddObjAtLast(ws.statusList, status);
					tsLib.debugPrint(DEBUG_DM, "STATUS_COMMAND_NOT_ALLOWED=" + String.valueOf(pNodeName));
					return SDM_RET_OK;
				}
			}
			outbuf = tsDmHandlecmd.dmDataStGetString(item.data);
			if (outbuf == null)
			{
				outbuf = String.valueOf(item.data.data);
			}
			acllist = dmAgentMakeAcl(acllist, outbuf);
			outbuf = null;
			tsOmList.dmOmDeleteAclList(node.acl);
			node.acl = acllist;

			status = dmBuildcmd.dmBuildCmdStatus(ws, (replace.cmdid), CMD_REPLACE, null, item.target, STATUS_OK);
			tsLinkedList.listAddObjAtLast(ws.statusList, status);
		}
		else if (ptr.compareTo("Format") == 0)
		{
			status = dmBuildcmd.dmBuildCmdStatus(ws, (replace.cmdid), CMD_REPLACE, null, item.target, STATUS_COMMAND_NOT_ALLOWED);
			tsLinkedList.listAddObjAtLast(ws.statusList, status);
		}
		else if (ptr.compareTo("Type") == 0)
		{
			status = dmBuildcmd.dmBuildCmdStatus(ws, (replace.cmdid), CMD_REPLACE, null, item.target, STATUS_COMMAND_NOT_ALLOWED);
			tsLinkedList.listAddObjAtLast(ws.statusList, status);
		}
		else if (ptr.compareTo("Size") == 0)
		{
			status = dmBuildcmd.dmBuildCmdStatus(ws, (replace.cmdid), CMD_REPLACE, null, item.target, STATUS_COMMAND_NOT_ALLOWED);
			tsLinkedList.listAddObjAtLast(ws.statusList, status);
		}
		else if (ptr.compareTo("Name") == 0)
		{
			status = dmBuildcmd.dmBuildCmdStatus(ws, (replace.cmdid), CMD_REPLACE, null, item.target, STATUS_COMMAND_NOT_ALLOWED);
			tsLinkedList.listAddObjAtLast(ws.statusList, status);
		}
		else if (ptr.compareTo("Title") == 0)
		{
			status = dmBuildcmd.dmBuildCmdStatus(ws, (replace.cmdid), CMD_REPLACE, null, item.target, STATUS_OK);

			node.title = null;
			node.title = tsDmHandlecmd.dmDataStGetString(item.data);
			if (node.title == null)
			{
				if (item.data != null && item.data.data != null)
				{
					node.title = String.valueOf(item.data.data);
				}
			}
		}
		else
		{
			status = dmBuildcmd.dmBuildCmdStatus(ws, (replace.cmdid), CMD_REPLACE, null, item.target, STATUS_COMMAND_NOT_ALLOWED);
			tsLinkedList.listAddObjAtLast(ws.statusList, status);
		}
		return SDM_RET_OK;
	}

	public int dmAgentMakeTndsSubTree(tsOmTree om, tsDmVnode node, int nFlag, String pPath)
	{
		int res = 0;
		int nLen = 0;
		int ac = 0;
		String pTag = null;
		String pData = null;
		String pFormat = null;
		String pNodeProperty = null;
		String pType = null;
		String pNodeUri = null;

		tsDmVnode cur = new tsDmVnode();
		tsOmAcl acl = new tsOmAcl();
		tsOmList list = new tsOmList();
		int nFileId = 0;

		if (node == null)
		{
			// exception.
			return SDM_RET_FAILED;
		}
		cur = node.childlist;
		nFileId = tsdmDB.dmdbGetFileIdTNDS();

		pTag = ddfParser.gSdmXmlTagString[ddfParser.XML_TAG_Node_String_Start];

		pNodeProperty = pTag;

		if (pPath != null)
		{

			pTag = ddfParser.gSdmXmlTagString[ddfParser.XML_TAG_Path_String_Start];
			tsLib.debugPrint(DEBUG_DM, " pTag : " + pTag);
			pNodeProperty = pNodeProperty.concat(pTag);
			tsLib.debugPrint(DEBUG_DM, " pPath : " + pPath);
			pNodeProperty = pNodeProperty.concat(pPath);
			pTag = ddfParser.gSdmXmlTagString[ddfParser.XML_TAG_Path_String_End];
			tsLib.debugPrint(DEBUG_DM, " pTag : " + pTag);
			pNodeProperty = pNodeProperty.concat(pTag);
		}
		if (node.name != null)
		{
			pTag = ddfParser.gSdmXmlTagString[ddfParser.XML_TAG_NodeName_String_Start];
			pNodeProperty = pNodeProperty.concat(pTag);
			pNodeProperty = pNodeProperty.concat(node.name);
			tsLib.debugPrint(DEBUG_DM, " node.name : " + node.name);
			pTag = ddfParser.gSdmXmlTagString[ddfParser.XML_TAG_NodeName_String_End];
			pNodeProperty = pNodeProperty.concat(pTag);
		}

		pFormat = tsOmList.dmOmGetFormatString(node.format);

		if ((pFormat != null) || (node.acl != null) || (node.type != null))
		{
			pTag = ddfParser.gSdmXmlTagString[ddfParser.XML_TAG_RTProperties_String_Start];
			pNodeProperty = pNodeProperty.concat(pTag);

			if ((nFlag & TNDS_PROPERTY_FORMAT) == TNDS_PROPERTY_FORMAT)
			{
				if (pFormat != null)
				{
					pTag = ddfParser.gSdmXmlTagString[ddfParser.XML_TAG_Format_String_Start];
					pNodeProperty = pNodeProperty.concat(pTag);
					pNodeProperty = pNodeProperty.concat("<");
					pNodeProperty = pNodeProperty.concat(pFormat);
					pNodeProperty = pNodeProperty.concat("/>");
					pTag = ddfParser.gSdmXmlTagString[ddfParser.XML_TAG_Format_String_End];
					pNodeProperty = pNodeProperty.concat(pTag);
				}
			}

			if ((nFlag & TNDS_PROPERTY_TYPE) == TNDS_PROPERTY_TYPE)
			{
				if (node.type != null)
				{
					list = node.type;
					pType = (String) list.data;

					if (pType != null)
					{
						pTag = ddfParser.gSdmXmlTagString[ddfParser.XML_TAG_Type_String_Start];
						pNodeProperty = pNodeProperty.concat(pTag);
						pNodeProperty = pNodeProperty.concat("<MIME>");
						pNodeProperty = pNodeProperty.concat(pType);
						pNodeProperty = pNodeProperty.concat("</MIME>");
						pTag = ddfParser.gSdmXmlTagString[ddfParser.XML_TAG_Type_String_End];
						pNodeProperty = pNodeProperty.concat(pTag);
					}
				}
			}
			list = null;

			if ((nFlag & TNDS_PROPERTY_ACL) == TNDS_PROPERTY_ACL)
			{
				if (node.acl != null)
				{
					list = node.acl;
					acl = (tsOmAcl) list.data;

					if (acl != null)
					{
						ac = acl.ac;
						if (ac != 0x00)
						{
							boolean IsOtherACL = false;
							pTag = ddfParser.gSdmXmlTagString[ddfParser.XML_TAG_ACL_String_Start];
							pNodeProperty = pNodeProperty.concat(pTag);
							if ((ac & OMACL_ADD) == OMACL_ADD)
							{
								pNodeProperty = pNodeProperty.concat("Add=*");
								IsOtherACL = true;
							}
							if ((ac & OMACL_DELETE) == OMACL_DELETE)
							{
								if (IsOtherACL)
								{
									pNodeProperty = pNodeProperty.concat("&amp;Delete=*");
								}
								else
								{
									pNodeProperty = pNodeProperty.concat("Delete=*");
									IsOtherACL = true;
								}
							}
							if ((ac & OMACL_EXEC) == OMACL_EXEC)
							{
								if (IsOtherACL)
								{
									pNodeProperty = pNodeProperty.concat("&amp;Exec=*");
								}
								else
								{
									pNodeProperty = pNodeProperty.concat("Exec=*");
									IsOtherACL = true;
								}
							}
							if ((ac & OMACL_GET) == OMACL_GET)
							{
								if (IsOtherACL)
								{
									pNodeProperty = pNodeProperty.concat("&amp;Get=*");
								}
								else
								{
									pNodeProperty = pNodeProperty.concat("Get=*");
									IsOtherACL = true;
								}
							}
							if ((ac & OMACL_REPLACE) == OMACL_REPLACE)
							{
								if (IsOtherACL)
								{
									pNodeProperty = pNodeProperty.concat("&amp;Replace=*");
								}
								else
								{
									pNodeProperty = pNodeProperty.concat("Replace=*");
									IsOtherACL = true;
								}
							}
							pTag = ddfParser.gSdmXmlTagString[ddfParser.XML_TAG_ACL_String_End];
							pNodeProperty = pNodeProperty.concat(pTag);
							IsOtherACL = false;
						}
					}
				}
			}
			pTag = ddfParser.gSdmXmlTagString[ddfParser.XML_TAG_RTProperties_String_End];
			pNodeProperty = pNodeProperty.concat(pTag);
		}

		if ((nFlag & TNDS_PROPERTY_VALUE) == TNDS_PROPERTY_VALUE)
		{
			if (node.size > 0)
			{
				pTag = ddfParser.gSdmXmlTagString[ddfParser.XML_TAG_Value_String_Start];
				pNodeProperty = pNodeProperty.concat(pTag);
				tsdmDB.dmAppendFile(nFileId, pNodeProperty.length(), pNodeProperty.getBytes());

				pData = "";
				pNodeUri = dmAgentGetPathFromNode(om, node);
				char[] cTemp = new char[node.size];
				nLen = tsOmlib.dmOmRead(om, pNodeUri, 0, cTemp, node.size);
				pData = String.valueOf(cTemp);
				if (nLen > 0)
				{
					tsdmDB.dmAppendFile(nFileId, pData.length(), pData.getBytes());
				}
				pTag = ddfParser.gSdmXmlTagString[ddfParser.XML_TAG_Value_String_End];
				tsdmDB.dmAppendFile(nFileId, pTag.length(), pTag.getBytes());
			}
			else
			{
				tsdmDB.dmAppendFile(nFileId, pNodeProperty.length(), pNodeProperty.getBytes());
			}
		}
		else
		{
			tsdmDB.dmAppendFile(nFileId, pNodeProperty.length(), pNodeProperty.getBytes());
		}

		while (cur != null)
		{
			res = dmAgentMakeTndsSubTree(om, cur, nFlag, null);
			if (res != 0)
			{

			}
			cur = cur.next;
		}

		pTag = ddfParser.gSdmXmlTagString[ddfParser.XML_TAG_Node_String_End];

		tsdmDB.dmAppendFile(nFileId, pTag.length(), pTag.getBytes());

		return SDM_RET_OK;
	}

	public boolean dmAgentCmdPropGetTnds(tsDmParserGet get, tsOmTree om, tsDmVnode node, String propertylist)
	{
		@SuppressWarnings("unused")
		int res = 0;
		String pTemp = "";
		String pTag = "";
		String name = "";
		String format = "";
		String data = "";
		int nFlag = 0;
		int nSize = 0;
		String token = "";
		String[] ptr = null;
		tsDmParserResults results = null;
		tsDmWorkspace ws = dm_ws;
		char[] tempPath = null;

		/* tnds converter */
		int nFileId = 0;

		tsLib.debugPrint(DEBUG_DM, "");
		if (node == null)
		{
			return false;
		}

		if (node.childlist == null)
		{
			return false;
		}

		nFileId = tsdmDB.dmdbGetFileIdTNDS();

		ptr = propertylist.split("\\+");
		// Defects
		if (ptr == null)
		{
			return false;
		}

		token = ptr[0];
		tsLib.debugPrint(DEBUG_DM, "token : " + token);
		if (ptr.length > 1)
		{
			int i = 1;

			while (i < ptr.length)
			{
				if (token.compareTo("ACL") == 0)
				{
					nFlag = nFlag | TNDS_PROPERTY_ACL;
				}
				else if (token.compareTo("Format") == 0)
				{
					nFlag = nFlag | TNDS_PROPERTY_FORMAT;
				}
				else if (token.compareTo("Type") == 0)
				{
					nFlag = nFlag | TNDS_PROPERTY_TYPE;
				}
				else if (token.compareTo("Value") == 0)
				{
					nFlag = nFlag | TNDS_PROPERTY_VALUE;
				}

				token = ptr[i++];
			}
		}
		else
		{
			nFlag = TNDS_PROPERTY_ACL | TNDS_PROPERTY_FORMAT | TNDS_PROPERTY_TYPE | TNDS_PROPERTY_VALUE;
			ptr = propertylist.split("-");
			if (ptr != null)
			{
				token = ptr[0];
				tsLib.debugPrint(DEBUG_DM, "token : " + token);

				if (token.compareTo("ACL") == 0)
				{
					nFlag = nFlag & 0xFE;
				}
				else if (token.compareTo("Format") == 0)
				{
					nFlag = nFlag & 0xFD;
				}
				else if (token.compareTo("Type") == 0)
				{
					nFlag = nFlag & 0xFB;
				}
				else if (token.compareTo("Value") == 0)
				{
					nFlag = nFlag & 0xF7;
				}
			}

		}

		tsdmDB.dmdbDeleteFile(nFileId);

		pTag = ddfParser.gSdmXmlTagString[ddfParser.XML_TAG_SyncML_String_Start];
		pTemp = pTemp.concat(pTag);

		pTag = ddfParser.gSdmXmlTagString[ddfParser.XML_TAG_MgmtTree_String_Start];
		pTemp = pTemp.concat(pTag);

		pTag = ddfParser.gSdmXmlTagString[ddfParser.XML_TAG_VerDTD_String_Start];
		pTemp = pTemp.concat(pTag);
		pTemp = pTemp.concat(DM_VERDTD_1_2); // version dtd 1.2

		pTag = ddfParser.gSdmXmlTagString[ddfParser.XML_TAG_VerDTD_String_End];
		pTemp = pTemp.concat(pTag);
		tsdmDB.dmAppendFile(nFileId, pTemp.length(), pTemp.getBytes());

		String sTempPath = dmAgentGetPathFromNode(om, node);
		tempPath = new char[sTempPath.length()];
		tsOmlib.dmOmMakeParentPath(sTempPath, tempPath);
		tsLib.debugPrint(DEBUG_DM, "tempPath : " + tsLib.libString(tempPath));
		res = dmAgentMakeTndsSubTree(om, node, nFlag, tsLib.libString(tempPath));

		pTag = ddfParser.gSdmXmlTagString[ddfParser.XML_TAG_MgmtTree_String_End];
		tsdmDB.dmAppendFile(nFileId, pTag.length(), pTag.getBytes());

		pTag = ddfParser.gSdmXmlTagString[ddfParser.XML_TAG_SyncML_String_End];
		tsdmDB.dmAppendFile(nFileId, pTag.length(), pTag.getBytes());

		nSize = tsdmDB.dmdbGetFileSize(nFileId);
		byte[] bTemp = (byte[]) tsdmDB.dmReadFile(nFileId, 0, nSize);
		data = new String(bTemp);
		name = dmAgentGetPathFromNode(om, node);
		format = tsOmList.dmOmGetFormatString(FORMAT_XML);

		tsLib.debugPrint(DEBUG_DM, "name : " + name);
		//if (data == null)
		//{
		//	tsLib.debugPrintException(DEBUG_EXCEPTION, "_____ TNDSResults File Read Error!");
		//	results = dmBuildcmd.dmBuildCmdDetailResults(ws, get.cmdid, name, format, SYNCML_MIME_TYPE_TNDS_XML, nSize, null);
		//}
		//else
		//{
			results = dmBuildcmd.dmBuildCmdDetailResults(ws, get.cmdid, name, format, SYNCML_MIME_TYPE_TNDS_XML, nSize, data.toCharArray());
		//}
		tsLinkedList.listAddObjAtLast(ws.resultsList, results);

		return true;
	}

	public String dmAgentGetAclStr(tsOmList acllist, tsDmParserItem item)
	{
		// String[] buf = new String[5];
		String[] buf = new String[] {"\0", "\0", "\0", "\0", "\0"};// Defects

		String outbuf, tmp;
		tsOmAcl acl;
		int i;
		tsOmList cur = acllist;

		if (acllist == null)
			return null;

		outbuf = "\0";
		// int size = buf.length;
		// for(i=0; i< size;i++)
		// {
		// buf[i]="\0";
		// }//Defects

		if (item.meta == null)
			item.meta = null;
		else if (item.meta.format == null)
			item.meta.format = null;
		else
			tsLib.debugPrint(DEBUG_DM, "item->meta !NULL");
		while (cur != null)
		{
			acl = (tsOmAcl) cur.data;
			if ((acl.ac & OMACL_ADD) > 0)
			{
				// if (buf[0].charAt(0) != '\0')
				// buf[0]="+";//Defects - buf array is alaways initialize "\0"
				if (buf[0].charAt(0) == '\0')
					buf[0] = acl.serverid;
				else
					buf[0] = buf[0].concat(acl.serverid);
			}
			if ((acl.ac & OMACL_DELETE) > 0)
			{
				// if (buf[1].charAt(0) != '\0') buf[1]= "+";//Defects - buf array is alaways initialize "\0"
				if (buf[1].charAt(0) == '\0')
					buf[1] = acl.serverid;
				else
					buf[1] = buf[1].concat(acl.serverid);
			}
			if ((acl.ac & OMACL_EXEC) > 0)
			{
				// if (buf[2].charAt(0) != '\0') buf[2] = "+";//Defects - buf array is alaways initialize "\0"
				if (buf[2].charAt(0) == '\0')
					buf[2] = acl.serverid;
				else
					buf[2] = buf[2].concat(acl.serverid);
			}
			if ((acl.ac & OMACL_GET) > 0)
			{
				// if (buf[3].charAt(0) != '\0') buf[3]= "+";//Defects - buf array is alaways initialize "\0"
				if (buf[3].charAt(0) == '\0')
					buf[3] = acl.serverid;
				else
					buf[3] = buf[3].concat(acl.serverid);
			}
			if ((acl.ac & OMACL_REPLACE) > 0)
			{
				// if (buf[4].charAt(0) != '\0') buf[4]="+";//Defects - buf array is alaways initialize "\0"
				if (buf[4].charAt(0) == '\0')
					buf[4] = acl.serverid;
				else
					buf[4] = buf[4].concat(acl.serverid);
			}
			cur = cur.next;
		}

		for (i = 0; i < 5; i++)
		{
			if (i == 0 && buf[i].charAt(0) != '\0')
			{
				if (outbuf.charAt(0) != '\0')
				{
					if (item.meta == null)
					{
						outbuf = outbuf.concat("&");
					}
					else if (item.meta.format == null)
					{
						outbuf = outbuf.concat("&");
					}
					else if (item.meta.format.compareTo("xml") == 0)
					{
						outbuf = outbuf.concat("&amp;");
					}
					else
					{
						outbuf = outbuf.concat("&");
					}
				}
				// kjk end
				if (outbuf.charAt(0) != '\0')
					outbuf = outbuf.concat("Add=");
				else
					outbuf = "Add=";
			}
			if (i == 1 && buf[i].charAt(0) != '\0')
			{
				if (outbuf.charAt(0) != '\0')
				{
					if (item.meta == null)
					{
						outbuf = outbuf.concat("&");
					}
					else if (item.meta.format == null)
					{
						outbuf = outbuf.concat("&");
					}
					else if (item.meta.format.compareTo("xml") == 0)
					{
						outbuf = outbuf.concat("&amp;");
					}
					else
					{
						outbuf = outbuf.concat("&");
					}
				}
				// kjk end
				if (outbuf.charAt(0) != '\0')
					outbuf = outbuf.concat("Delete=");
				else
					outbuf = "Delete=";
			}
			if (i == 2 && buf[i].charAt(0) != '\0')
			{
				if (outbuf.charAt(0) != '\0')
				{
					if (item.meta == null)
					{
						outbuf = outbuf.concat("&");
					}
					else if (item.meta.format == null)
					{
						outbuf = outbuf.concat("&");
					}
					else if (item.meta.format.compareTo("xml") == 0)
					{
						outbuf = outbuf.concat("&amp;");
					}
					else
					{
						outbuf = outbuf.concat("&");
					}
				}
				// kjk end
				if (outbuf.charAt(0) != '\0')
					outbuf = outbuf.concat("Exec=");
				else
					outbuf = "Exec=";
			}
			if (i == 3 && buf[i].charAt(0) != '\0')
			{
				if (outbuf.charAt(0) != '\0')
				{
					if (item.meta == null)
					{
						outbuf = outbuf.concat("&");
					}
					else if (item.meta.format == null)
					{
						outbuf = outbuf.concat("&");
					}
					else if (item.meta.format.compareTo("xml") == 0)
					{
						outbuf = outbuf.concat("&amp;");
					}
					else
					{
						outbuf = outbuf.concat("&");
					}
				}
				// kjk end
				if (outbuf.charAt(0) != '\0')
					outbuf = outbuf.concat("Get=");
				else
					outbuf = "Get=";
			}
			if (i == 4 && buf[i].charAt(0) != '\0')
			{
				if (outbuf.charAt(0) != '\0')
				{
					if (item.meta == null)
					{
						outbuf = outbuf.concat("&");
					}
					else if (item.meta.format == null)
					{
						outbuf = outbuf.concat("&");
					}
					else if (item.meta.format.compareTo("xml") == 0)
					{
						outbuf = outbuf.concat("&amp;");
					}
					else
					{
						outbuf = outbuf.concat("&");
					}
				}
				// kjk end
				if (outbuf.charAt(0) != '\0')
					outbuf = outbuf.concat("Replace=");
				else
					outbuf = "Replace=";
			}

			if (buf[i].charAt(0) != '\0')
			{
				outbuf = outbuf.concat(buf[i]);
			}
		}

		tmp = outbuf;

		return tmp;
	}

	public tsOmList dmAgentMakeAcl(tsOmList acllist, String aclstr)
	{
		char[] buf = null; // new char[80];
		char[] subbuf = null;// new char[80];
		String ptr = aclstr;
		String subptr;

		buf = new char[ptr.length()];
		ptr = tsLib.libStrsplit(ptr.toCharArray(), '&', buf);
		while (buf != null)// (ptr!=null)
		{
			if (ptr == null)
				buf[buf.length - 1] = '\0';
			subptr = tsLib.libString(buf);
			subbuf = new char[subptr.length()];
			subptr = tsLib.libStrsplit(subptr.toCharArray(), '=', subbuf);
			String cmdStr = tsLib.libString(subbuf);

			if (subptr != null)
			{
				if (cmdStr.compareTo("Add") == 0)
				{
					acllist = dmAgentAppendAclItem(acllist, subptr, OMACL_ADD);
				}
				else if (cmdStr.compareTo("Delete") == 0)
				{
					acllist = dmAgentAppendAclItem(acllist, subptr, OMACL_DELETE);
				}
				else if (cmdStr.compareTo("Replace") == 0)
				{
					acllist = dmAgentAppendAclItem(acllist, subptr, OMACL_REPLACE);
				}
				else if (cmdStr.compareTo("Get") == 0)
				{
					acllist = dmAgentAppendAclItem(acllist, subptr, OMACL_GET);
				}
				else if (cmdStr.compareTo("Exec") == 0)
				{
					acllist = dmAgentAppendAclItem(acllist, subptr, OMACL_EXEC);
				}
			}

			if (ptr == null)
				break;
			if (ptr.charAt(0) == 'a' && ptr.charAt(1) == 'm' && ptr.charAt(2) == 'p' && ptr.charAt(3) == ';') // ("amp;") == 0) // _support_xml_amp_
			{
				ptr = ptr.substring(4);
			}

			buf = null;
			buf = new char[ptr.length() + 1];
			ptr = tsLib.libStrsplit(ptr.toCharArray(), '&', buf);
		}
		return acllist;
	}

	public tsOmList dmAgentAppendAclItem(tsOmList acllist, String aclValue, int aclflag)
	{
		String ptr = aclValue;
		tsOmList cur, tmp;
		tsOmAcl acl;
		char[] buf = null;
		char[] tmp1;
		boolean found = false;

		buf = new char[ptr.length() + 1];

		if (!ptr.contains("*"))
			ptr = tsLib.libStrsplit(ptr.toCharArray(), '+', buf);
		else
		{
			buf[0] = '*';
			buf[1] = '\0';
		}
		while (ptr != null)
		{
			// find matching server-id
			cur = acllist;
			while (cur != null && cur.data != null)
			{
				acl = (tsOmAcl) cur.data;

				if (acl.serverid.compareTo(String.valueOf(buf)) == 0)
				{
					found = true;
					acl.ac = (acl.ac | aclflag);
				}

				cur = cur.next;
			}

			if (!found)
			{
				tmp1 = new char[40];
				acl = new tsOmAcl();
				if (!String.valueOf(buf).contains("*"))
					String.valueOf(buf).getChars(0, 40 - 1, tmp1, 0);
				else
				{
					tmp1[0] = '*';
					tmp1[1] = '\0';
				}
				acl.serverid = tsLib.libString(tmp1);

				acl.ac = (acl.ac | aclflag);
				tmp = new tsOmList();
				tmp.data = acl;
				tmp.next = null;
				acllist = tsOmlib.dmOmAppendList(acllist, tmp);
			}
			buf = null;
			buf = new char[ptr.length()];
			ptr = tsLib.libStrsplit(ptr.toCharArray(), '+', buf);
		}
		return acllist;
	}

	public int dmAgentStartMgmtSession()
	{
		tsDmWorkspace ws = dm_ws;
		int res = SDM_RET_OK;

		if (ws == null)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, "Parsing package failed Abort session " + res);
			return SDM_RET_FAILED;
		}
		if (ws.procState == SyncmlProcessingState.PROC_NONE)
		{
			ws.numAction = 0;
			res = dmAgentParsingWbxml(ws.buf.toByteArray());

			if (res != SDM_RET_OK)
			{
				tsLib.debugPrintException(DEBUG_EXCEPTION, "Parsing package failed Abort session" + res);
				return SDM_RET_FAILED;
			}
		}

		res = dmAgentHandleCmd();

		ws.uicFlag = SyncmlUICFlag.UIC_TRUE;

		switch (res)
		{
			case SDM_PAUSED_BECAUSE_UIC_COMMAND:
				tsLib.debugPrint(DEBUG_DM, "Handling Paused  Processing UIC Command");
				return res;
			case SDM_ALERT_SESSION_ABORT:
				tsLib.debugPrint(DEBUG_DM, "SDM_ALERT_SESSION_ABORT");
				tsdmDB.dmdbSetFUMOStatus(DM_FUMO_STATE_NONE);
				tsdmDB.dmdbSetFUMOUpdateMechanism(DM_FUMO_MECHANISM_NONE);
				return res;
			case SDM_RET_OK:
				break;
			default:
				tsLib.debugPrintException(DEBUG_EXCEPTION, "Handling Commands failed Abort session " + res);
				return SDM_RET_FAILED;
		}
		if (ws.dmState != SyncmlState.DM_STATE_FINISH)
		{
			ws.msgID++;
		}
		if (ws.authState != AUTH_STATE_OK || ws.serverAuthState != AUTH_STATE_OK)
		{
			ws.authCount++;
			if (ws.authCount >= SDM_MAX_AUTH_COUNT)
			{
				ws.authCount = 0;
				ws.serverAuthState = AUTH_STATE_NONE;
				tsLib.debugPrintException(DEBUG_EXCEPTION, "Authentification Failed Abort");
				dlAgent.dlAgentSetClientInitFlag(DM_NONE_INIT);
				dmAgent.dmAgentSetServerInitiatedStatus(false);
				return SDM_RET_AUTH_MAX_ERROR;
			}
			/* IOT Issue */
			else if (ws.authState == AUTH_STATE_OK_PENDING)
			{
				ws.dmState = SyncmlState.DM_STATE_PROCESSING;
			}
			else
			{
				// for: auth
				tsLib.debugPrint(DEBUG_DM, "Authentification Retry...ws->dmState = " + ws.dmState);
				if ((ws.dmState != SyncmlState.DM_STATE_CLIENT_INIT_MGMT) && (ws.dmState != SyncmlState.DM_STATE_GENERIC_ALERT) && (ws.dmState != SyncmlState.DM_STATE_GENERIC_ALERT_REPORT))
				{
					ws.dmState = SyncmlState.DM_STATE_PROCESSING;
				}
			}
			if (dmAgentCreatePackage() != SDM_RET_OK)
			{
				tsLib.debugPrintException(DEBUG_EXCEPTION, "failed");
				return SDM_RET_FAILED;
			}

			if (netHttpAdapter.pHttpObj[SYNCMLDM].nHttpConnection == TP_HTTP_CONNECTION_CLOSE)
			{
				try
				{
					res = gHttpDMAdapter.tpOpen(SYNCMLDM);
				}
				catch (SocketTimeoutException e)
				{
					tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
					netTimerConnect.endTimer();
					res = TP_RET_CONNECTION_FAIL;
				}
				if (res != TP_RET_OK)
				{
					return SDM_RET_CONNECT_FAIL;
				}
			}

			res = dmAgentSendPackage();
			return res;
		}
		else
		{
			ws.dmState = SyncmlState.DM_STATE_PROCESSING;
			ws.authCount = 0;
			tsLib.debugPrint(DEBUG_DM, "total action commands = " + ws.numAction);

			if (ws.numAction == 0 && ws.isFinal)
			{
				int nUpdateMechanism;
				int nAgentStatus;

				tsdmDB.dmdbClearUicResultKeepFlag();
				nUpdateMechanism = tsdmDB.dmdbGetFUMOUpdateMechanism();
				nAgentStatus = tsdmDB.dmdbGetFUMOStatus();
				tsLib.debugPrint(DEBUG_DM, "nStatus :" + nAgentStatus);

				if (nUpdateMechanism == DM_FUMO_MECHANISM_ALTERNATIVE && nAgentStatus == DM_FUMO_STATE_IDLE_START)
				{
					tsLib.debugPrint(DEBUG_DM, "Now Download Start");
					return SDM_RET_EXEC_ALTERNATIVE;
				}
				else if (nUpdateMechanism == DM_FUMO_MECHANISM_REPLACE && nAgentStatus == DM_FUMO_STATE_DOWNLOAD_COMPLETE)
				{
					tsLib.debugPrint(DEBUG_DM, "OMA-DM Download Completed");
					tsdmDB.dmdbSetFUMOStatus(DM_FUMO_STATE_READY_TO_UPDATE);
					return SDM_RET_EXEC_DOWNLOAD_COMPLETE;
				}

				// ADD : for gernic Alert Type for Update Report
				else if (nUpdateMechanism == DM_FUMO_MECHANISM_ALTERNATIVE_DOWNLOAD && nAgentStatus == DM_FUMO_STATE_IDLE_START)
				{
					tsLib.debugPrint(DEBUG_DM, "SDM_RET_EXEC_ALTERNATIVE_DOWNLOAD Start");
					return SDM_RET_EXEC_ALTERNATIVE_DOWNLOAD;
				}
				else if (nUpdateMechanism == DM_FUMO_MECHANISM_ALTERNATIVE_DOWNLOAD && nAgentStatus == DM_FUMO_STATE_READY_TO_UPDATE)
				{
					tsLib.debugPrint(DEBUG_DM, "Now Update Start");
					return SDM_RET_EXEC_ALTERNATIVE_UPDATE;
				}

				if (_SYNCML_TS_DM_VERSION_V12_)
				{
					tsdmDB.dmdbSetNotiReSyncMode(DM_NOTI_RESYNC_MODE_FALSE);
				}

				tsdmDB.dmdbClearUicResultKeepFlag();
				return SDM_RET_FINISH;
			}
			
			res = dmAgentCreatePackage();

			if (res < 0)
			{
				tsLib.debugPrintException(DEBUG_EXCEPTION, " failed " + res);
				return SDM_RET_FAILED;
			}

			if (netHttpAdapter.pHttpObj[SYNCMLDM].nHttpConnection == TP_HTTP_CONNECTION_CLOSE)
			{
				try
				{
					res = gHttpDMAdapter.tpOpen(SYNCMLDM);
				}
				catch (SocketTimeoutException e)
				{
					tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
					netTimerConnect.endTimer();
					res = TP_RET_CONNECTION_FAIL;
				}
				if (res != TP_RET_OK)
				{
					tsLib.debugPrintException(DEBUG_EXCEPTION, "SDM_RET_CONNECT_FAIL");
					return SDM_RET_CONNECT_FAIL;
				}
			}

			res = dmAgentSendPackage();
			return res;
		}
	}

	public int dmAgentStartGeneralAlert(int nUpdateMechanism)
	{
		int nNotiEvent;
		int ret = SDM_RET_OK;
		int nAgentType = SYNCML_DM_AGENT_DM;

		tsLib.debugPrint(DEBUG_DM, "");
		dmAgentInit();
		//if (dmAgentInit() != SDM_RET_OK)
		//{
		//	return SDM_RET_FAILED;
		//}

		nNotiEvent = tsDB.dbGetNotiEvent(dm_ws.appId);
		if (nNotiEvent > 0)
		{
			dm_ws.sessionID = tsDB.dbGetNotiSessionID(dm_ws.appId);
		}
		else
		{
			dm_ws.sessionID = dmMakeSessionID();
		}

		if (dmAgentMakeNode() != SDM_RET_OK)
		{
			return SDM_RET_FAILED;
		}

		nAgentType = tsdmDB.dmdbGetDmAgentType();
		tsLib.debugPrint(DEBUG_DM, "nAgentType : " + nAgentType);
		if (nAgentType == SYNCML_DM_AGENT_FUMO)
		{
			int nStatus = DM_FUMO_STATE_NONE;
			String status = null;
			String pStatusNode = null;
			String pStatusNodeName = null;
			tsOmTree om = dm_ws.om;
			int aclValue;

			pStatusNodeName = tsdmDB.dmdbGetFUMOStatusNode();
			if (!tsLib.isEmpty(pStatusNodeName))
			{
				pStatusNode = FUMO_PATH;
				pStatusNode = pStatusNode.concat("/");
				pStatusNode = pStatusNode.concat(pStatusNodeName);
				pStatusNode = pStatusNode.concat(FUMO_STATE_PATH);
				aclValue = OMACL_GET;

				nStatus = tsdmDB.dmdbGetFUMOStatus();
				status = String.valueOf(nStatus);
				tsLib.debugPrint(DEBUG_DM, "node[" + pStatusNode + "] value[" + status + "]");
				dmAgentSetOMAccStr(om, pStatusNode, status, aclValue, SCOPE_DYNAMIC);
			}
		}

		dm_ws.dmState = SyncmlState.DM_STATE_GENERIC_ALERT_REPORT;

		if (dmAgentCreatePackage() != SDM_RET_OK)
		{
			return SDM_RET_FAILED;
		}

		try
		{
			ret = gHttpDMAdapter.tpOpen(SYNCMLDM);
		}
		catch (SocketTimeoutException e)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
			netTimerConnect.endTimer();
			ret = SDM_RET_CONNECT_FAIL;
		}

		if (ret != SDM_RET_OK)
		{
			return SDM_RET_CONNECT_FAIL;
		}

		netHttpAdapter.setIsConnected(true);
		ret = dmAgentSendPackage();
		return ret;
	}


	public int dmAgentHandleCmd()
	{
		tsDmWorkspace ws = dm_ws;
		dmAgent cmditem, tmp;
		tsLinkedList list = null;
		tsdmParserStatus status = null;
		int res = 0;
		boolean isAtomic = false;

		if (ws.procState == SyncmlProcessingState.PROC_NONE)
		{
			ws.procStep = PROCESS_STEP_NORMAL;
			ws.cmdID = 1;
		}
		while (ws.procStep != PROCESS_STEP_FINISH)
		{
			// For Multi Sequence with UIC and DM cmd
			if (!ws.IsSequenceProcessing)
			{
				list = ws.list;
				tsLinkedList.listSetCurrentObj(list, 0);
				cmditem = (dmAgent) tsLinkedList.listGetNextObj(list);
			}
			else
			{
				list = ws.sequenceList;
				/* 2008.05.01 prevent reset(sequence command */
				cmditem = (dmAgent) tsLinkedList.listGetObj(list, 0);
				if (cmditem != null)
				{
					dmAgentCmdSequenceBlock(cmditem.sequence, ws.sequenceList);
				}
				else
				{
					ws.IsSequenceProcessing = false;
				}
				/* End of prevent reset(sequence command */

				if (!ws.IsSequenceProcessing)
				{
					list = ws.list;
					tsLinkedList.listSetCurrentObj(list, 0);
					cmditem = (dmAgent) tsLinkedList.listGetNextObj(list);

					/* MOD : For UIC */
					tmp = cmditem;
					cmditem = (dmAgent) tsLinkedList.listGetNextObj(list);
					tsLinkedList.listRemoveObjAtFirst(list);
					ws.wsDmFreeAgent(tmp);
				}
			}

			// ADD : for UIC
			// solution for no status problem at uic process .
			if (cmditem == null)
			{
				if (ws.uicAlert != null)
				{
					dmAgentCmdUicAlert();
				}
				else
				{
					break;
				}
			}

			// processing...
			while (cmditem != null)
			{
				if ((cmditem.cmd.compareTo("Get") == 0) 
						|| (cmditem.cmd.compareTo("Exec") == 0) 
						|| (cmditem.cmd.compareTo("Alert") == 0)
						|| (cmditem.cmd.compareTo("Add") == 0) 
						|| (cmditem.cmd.compareTo("Replace") == 0) 
						|| (cmditem.cmd.compareTo("Copy") == 0)
						|| (cmditem.cmd.compareTo("Delete") == 0) 
						|| (cmditem.cmd.compareTo("Atomic_Start") == 0)
						|| (cmditem.cmd.compareTo("Sequence_Start") == 0))
				{
					ws.numAction++;
				}
				tsLib.debugPrint(DEBUG_DM, cmditem.cmd);
				if (cmditem.atomic != null)
				{
					ws.inAtomicCmd = true;
					isAtomic = true;
				}
				else if (cmditem.sequence != null)
				{
					ws.inSequenceCmd = true;
				}

				res = dmAgentVerifyCmd(cmditem, isAtomic, status);

				// For Multi Sequence with UIC and DM cmd
				if (!ws.IsSequenceProcessing)
				{
					tmp = cmditem;
					cmditem = (dmAgent) tsLinkedList.listGetNextObj(list);
					tsLinkedList.listRemoveObjAtFirst(list);
					ws.wsDmFreeAgent(tmp);
				}

				// ADD : for UIC
				// solution for no status problem at uic process .
				if (res == SDM_PAUSED_BECAUSE_UIC_COMMAND) // && ws->inSequenceCmd)
				{
					tsLib.debugPrint(DEBUG_DM, "SDM_PAUSED_BECAUSE_UIC_COMMAND");
					return SDM_PAUSED_BECAUSE_UIC_COMMAND;
				}
				else
				{
					ws.atomicFlag = false;
					ws.inAtomicCmd = false;
					ws.inSequenceCmd = false;
				} // For scts UIC END

				if (res == SDM_RET_EXEC_ALTERNATIVE || res == SDM_RET_EXEC_REPLACE)
				{
					ws.procStep = PROCESS_STEP_FINISH;
					ws.numAction = 0;
					return res;
				}
				else if (res == SDM_ALERT_SESSION_ABORT)
				{
					return res;
				}
				else if (res != SDM_RET_OK)
				{
					tsLib.debugPrintException(DEBUG_EXCEPTION, "Processing failed");
					return SDM_RET_FAILED;
				}
			}
			tsLinkedList.listClearLinkedList(list);
		}
		tsLinkedList.listClearLinkedList(ws.list);
		return SDM_RET_OK;
	}

	public int dmAgentVerifyCmd(dmAgent cmd, boolean isAtomic, tsdmParserStatus atomic_status)
	{
		tsDmWorkspace ws = dm_ws;
		int res = SDM_RET_OK;

		if (cmd.cmd.compareTo("SyncHdr") == 0)
		{
			res = dmAgentCmdSyncHeader(cmd.header);
		}
		else if (cmd.cmd.compareTo("Status") == 0)
		{
			res = dmAgentCmdStatus(cmd.status);
		}
		else if (cmd.cmd.compareTo("Get") == 0)
		{
			res = dmAgentCmdGet(cmd.get, isAtomic);
		}
		else if (cmd.cmd.compareTo("Exec") == 0)
		{
			res = dmAgentCmdExec(cmd.exec, isAtomic, atomic_status);
		}
		else if (cmd.cmd.compareTo("Alert") == 0)
		{
			res = dmAgentCmdAlert(cmd.alert, isAtomic, atomic_status);
		}
		else if (cmd.cmd.compareTo("Add") == 0)
		{
			res = dmAgentCmdAdd(cmd.addCmd, isAtomic, atomic_status);
		}
		else if (cmd.cmd.compareTo("Replace") == 0)
		{
			res = dmAgentCmdReplace(cmd.replaceCmd, isAtomic, atomic_status);
		}
		else if (cmd.cmd.compareTo("Copy") == 0)
		{
			res = dmAgentCmdCopy(cmd.copyCmd, isAtomic, atomic_status);
		}
		else if (cmd.cmd.compareTo("Delete") == 0)
		{
			res = dmAgentCmdDelete(cmd.deleteCmd, isAtomic, atomic_status);
		}
		else if (cmd.cmd.compareTo("Atomic_Start") == 0)
		{
			ws.inAtomicCmd = true;
			ws.atomicFlag = false;

			try
			{
				tsOmlib.dmOmvfsSaveFs(ws.om.vfs);
			}
			catch (IOException e)
			{
				tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
			} // om data to file write
			res = dmAgentCmdAtomic(cmd.atomic);
			if (ws.atomicFlag) // command failed
			{
				ws.om = null;
				ws.om = new tsOmTree();
				tsOmlib.dmOmvfsInit(ws.om.vfs); // command fail-> atomicFlag set TRUE
			}

			ws.inAtomicCmd = false;
		}
		else if (cmd.cmd.compareTo("Sequence_Start") == 0)
		{
			ws.inSequenceCmd = true;
			res = dmAgentCmdSequence(cmd.sequence);

			if (res == SDM_PAUSED_BECAUSE_UIC_COMMAND)
			{
				return res;
			}
			else
			{
				ws.inSequenceCmd = false;
			}
		}
		else
		{
			// unknown command
			tsLib.debugPrintException(DEBUG_EXCEPTION, "Unknown Command" + cmd.cmd);
			return SDM_UNKNOWN_ERROR;
		}

		return res;
	}

	public int dmAgentCmdSequenceBlock(tsDmParserSequence sequence, tsLinkedList list)
	{
		tsDmWorkspace ws = dm_ws;
		dmAgent cmditem, tmp;
		tsdmParserStatus status = null;
		int res = SDM_RET_OK;
		boolean isAtomic = false;

		tsLinkedList.listSetCurrentObj(list, 0);
		cmditem = (dmAgent) tsLinkedList.listGetNextObj(list);

		if (cmditem != null)
			ws.IsSequenceProcessing = true;// Defects
		while (cmditem != null)
		{
			if (cmditem.cmd.compareTo("Get") == 0
					|| cmditem.cmd.compareTo("Exec") == 0 
					|| cmditem.cmd.compareTo("Alert") == 0
					|| cmditem.cmd.compareTo("Add") == 0 
					|| cmditem.cmd.compareTo("Replace") == 0 
					|| cmditem.cmd.compareTo("Copy") == 0
					|| cmditem.cmd.compareTo("Delete") == 0)
			{
				ws.numAction++;
			}

			if (cmditem.cmd.compareTo("Atomic_Start") == 0)
			{
				isAtomic = true;
				tsLib.debugPrint(DEBUG_DM, "Atomic_Start");
				// 2006.02.10 atomic process within sequence ??
				if (ws.inAtomicCmd)
				{
					res = dmAgentCmdAtomic(cmditem.atomic);
				}
				else
				{
					res = dmAgentVerifyCmd(cmditem, isAtomic, status);
				}
				isAtomic = false;
			}
			else if (cmditem.cmd.compareTo("Sequence_Start") == 0)
			{
				tsLib.debugPrint(DEBUG_DM, "Sequence_Start");
				if (ws.inSequenceCmd)
				{
					res = dmAgentCmdSequence(cmditem.sequence);
				}
				else
				{
					res = dmAgentVerifyCmd(cmditem, isAtomic, status);
				}
			}
			else
			{
				res = dmAgentVerifyCmd(cmditem, isAtomic, status);
			}

			tmp = cmditem;
			cmditem = (dmAgent) tsLinkedList.listGetNextObj(list);
			tsLinkedList.listRemoveObjAtFirst(list);
			ws.wsDmFreeAgent(tmp);

			if (res == SDM_PAUSED_BECAUSE_UIC_COMMAND)
			{
				// For Multi Sequence with UIC and DM cmd
				ws.sequenceList = list;
				return SDM_PAUSED_BECAUSE_UIC_COMMAND;
			}
			else if (res == SDM_ALERT_SESSION_ABORT)
			{
				return res;
			}
			else if (res != SDM_RET_OK)
			{
				tsLib.debugPrintException(DEBUG_EXCEPTION, "Processing failed");
				return SDM_RET_FAILED;
			}

		}

		tsLinkedList.listClearLinkedList(list);
		ws.IsSequenceProcessing = false;
		return res;
	}

	public int dmAgentCmdSyncHeader(tsDmParserSyncheader synchdr)
	{
		tsDmWorkspace ws = dm_ws;
		tsdmParserStatus status;

		ws.msgRef = String.valueOf(synchdr.msgid);

		if (synchdr.respuri != null)
		{
			ws.targetURI = synchdr.respuri;
			tsLib.debugPrint(DEBUG_DM, "synchdr->respuri:" + synchdr.respuri);
			tsLib.debugPrint(DEBUG_DM, "ws->targetURI:" + ws.targetURI);
		}

		if (synchdr.meta != null)
		{
			if (synchdr.meta.maxobjsize > 0)
			{
				ws.serverMaxObjSize = synchdr.meta.maxobjsize;
				if (ws.serverMaxObjSize <= 0)
				{
					ws.serverMaxObjSize = WBXML_DM_MAX_OBJECT_SIZE;
				}
				else if (ws.serverMaxObjSize > 5120000) // 5 Mega
				{
					ws.serverMaxObjSize = WBXML_DM_MAX_OBJECT_SIZE;
				}
			}
			else
			{
				ws.serverMaxObjSize = WBXML_DM_MAX_OBJECT_SIZE;
			}

			if (synchdr.meta.maxmsgsize > 0)
			{
				ws.serverMaxMsgSize = synchdr.meta.maxmsgsize;
			}
			else
			{
				ws.serverMaxMsgSize = WBXML_DM_MAX_MESSAGE_SIZE;
			}
		}
		else
		{
			ws.serverMaxObjSize = WBXML_DM_MAX_OBJECT_SIZE;
			ws.serverMaxMsgSize = WBXML_DM_MAX_MESSAGE_SIZE;
		}

		if (ws.serverAuthState != AUTH_STATE_OK)
		{
			if (ws.serverCredType != CRED_TYPE_HMAC)
			{
				if (synchdr.cred != null)
				{
					ws.serverAuthState = dmAgentVerifyServerAuth(synchdr);
				}
				else
				{
					tsLib.debugPrintException(DEBUG_EXCEPTION, "Not Used Server Authentication");
					if (ws.serverAuthState == AUTH_STATE_RETRY || ws.serverAuthState == AUTH_STATE_REQUIRED)
					{
						ws.serverAuthState = AUTH_STATE_FAIL;
					}
					/* 2008.05.26 MOD auth retry case */
					else
					{
						ws.serverAuthState = AUTH_STATE_REQUIRED;
					}
				}
			}
			else
			{
				ws.serverAuthState = dmAgentVerifyServerAuth(synchdr);
			}
		}

		if (ws.serverAuthState == AUTH_STATE_OK)
		{
			if (ws.serverCredType == CRED_TYPE_HMAC)
			{
				ws.statusReturnCode = STATUS_OK;
			}
			else
			{
				ws.statusReturnCode = STATUS_AUTHENTICATIONACCEPTED;
			}
			tsdmDB.dmdbSetServerAuthType(ws.serverCredType); // DB write

		}
		else if (ws.serverAuthState == AUTH_STATE_REQUIRED)
		{
			ws.statusReturnCode = STATUS_AUTHENTICATION_REQUIRED;
		}
		else
		{
			ws.statusReturnCode = STATUS_UNAUTHORIZED;
		}

		status = dmBuildcmd.dmBuildCmdStatus(ws, 0, CMD_SYNCHDR, ws.hostname, ws.sourceURI, ws.statusReturnCode);
		tsLinkedList.listAddObjAtLast(ws.statusList, status);

		return SDM_RET_OK;
	}

	public int dmAgentCmdStatus(tsdmParserStatus status)
	{
		tsDmWorkspace ws = dm_ws;
		tsDmParserResults tmp;
		String szAccBuf;
		byte[] dValue = null;

		if (status.data.compareTo(STATUS_UNAUTHORIZED) == 0)
		{
			ws.authState = AUTH_STATE_RETRY;
			tsLib.debugPrint(DEBUG_DM, "Client invalid credential(401)");
			if ((status.cmd.compareTo(CMD_SYNCHDR) == 0) && status.chal == null) // auth retry mod
			{
				tsLib.debugPrint(DEBUG_DM, "SyncHdr Status 401. and No Chal");
				ws.authCount = SDM_MAX_AUTH_COUNT;
			}
		}
		// auth accepted
		else if (status.data.compareTo(STATUS_AUTHENTICATIONACCEPTED) == 0)
		{
			ws.authState = AUTH_STATE_OK;
		}
		else if ((status.data.compareTo(STATUS_OK) == 0) && (status.cmd.compareTo(CMD_SYNCHDR) == 0))
		{
			if (ws.credType == CRED_TYPE_HMAC)
			{
				if (status.chal != null)
				{
					ws.authState = AUTH_STATE_OK;
				}
			}
			else
			{
				ws.authState = AUTH_STATE_OK;
			}
			tsLib.debugPrint(DEBUG_DM, "Client Authrization Accepted (Catch 200)");
		}
		else if (status.data.compareTo(STATUS_ACCEPTED_AND_BUFFERED) == 0)
		{
			tsLib.debugPrint(DEBUG_DM, "received Status 'buffered' cmd " + status.cmdref);

			if (ws.tempResults != null)
			{
				tmp = new tsDmParserResults();
				tsDmHandlecmd.dmDataStDuplResults(tmp, ws.tempResults);
				tsLinkedList.listAddObjAtFirst(ws.resultsList, tmp);
			}
			else
			{
				tsLib.debugPrintException(DEBUG_EXCEPTION, "can't find cached results can't send multi-messaged");
			}
		}

		if (status.chal != null)
		{
			if (status.chal.type.compareTo(CRED_TYPE_STRING_MD5) == 0)
			{
				ws.credType = CRED_TYPE_MD5;
				tsdmDB.dmdbSetAuthType(ws.credType); // DB write

				if (status.chal.format.compareTo("b64") == 0)
				{
					if (_SYNCML_TS_DM_VERSION_V11_)
					{
						szAccBuf = g_AccName;
						szAccBuf = szAccBuf.concat(SYNCML_DMACC_CLIENTNONCE_PATH);
					}
					else
					{
						szAccBuf = dm_AccXNodeInfo.ClientAppAuth;
						szAccBuf = szAccBuf.concat(SYNCML_APPAUTH_AAUTHDATA_PATH);
					}
					dmAgentSetOMB64(szAccBuf, status.chal.nextnonce);

					dValue = base64.decode(status.chal.nextnonce.getBytes());
					if (dValue != null)
					{
						ws.nextNonce = null;
						ws.nextNonce = new byte[dValue.length];
						for (int i = 0; i < dValue.length; i++)
							ws.nextNonce[i] = dValue[i];
					}
					else
					{
						tsLib.debugPrint(DEBUG_DM, ": dValue is NULL");
					}

					String temp = new String(ws.nextNonce);

					tsLib.debugPrint(DEBUG_DM, "receive nextNonce:" + status.chal.nextnonce + "B64 decode String(ws.nextNonce):" + temp);
					// +"Test1 buffer String(test):" + test1);
				}
				else
				{
					// if Not b64, process is abnormal.
					tsLib.debugPrint(DEBUG_DM, "!B64");
					if (_SYNCML_TS_DM_VERSION_V11_)
					{
						szAccBuf = g_AccName;
						szAccBuf = szAccBuf.concat(SYNCML_DMACC_CLIENTNONCE_PATH);
					}
					else
					{
						szAccBuf = dm_AccXNodeInfo.ClientAppAuth;
						szAccBuf = szAccBuf.concat(SYNCML_APPAUTH_AAUTHDATA_PATH);
					}
					dmAgentSetOM(szAccBuf, status.chal.nextnonce);
					ws.nextNonce = status.chal.nextnonce.getBytes();
				}
				tsdmDB.dmdbSetClientNonce(status.chal.nextnonce); // db write

				if (_SYNCML_TS_DM_VERSION_V11_)
				{
					szAccBuf = g_AccName;
					szAccBuf = szAccBuf.concat(SYNCML_DMACC_AUTHPREF_PATH);
				}
				else
				{
					szAccBuf = dm_AccXNodeInfo.Account;
					szAccBuf = szAccBuf.concat(SYNCML_DMACC_AAUTHPREF_PATH);
				}
				dmAgentSetOM(szAccBuf, status.chal.type);
			}
			else if (status.chal.type.compareTo(CRED_TYPE_STRING_HMAC) == 0)
			{
				ws.credType = CRED_TYPE_HMAC;
				tsdmDB.dmdbSetAuthType(ws.credType); // DB write

				if (status.chal.format.compareTo("b64") == 0)
				{
					if (_SYNCML_TS_DM_VERSION_V11_)
					{
						szAccBuf = g_AccName;
						szAccBuf = szAccBuf.concat(SYNCML_DMACC_CLIENTNONCE_PATH);
					}
					else
					{
						szAccBuf = dm_AccXNodeInfo.ClientAppAuth;
						szAccBuf = szAccBuf.concat(SYNCML_APPAUTH_AAUTHDATA_PATH);
					}
					dmAgentSetOMB64(szAccBuf, status.chal.nextnonce);
					dValue = base64.decode(status.chal.nextnonce.getBytes());
					if (dValue != null)
					{
						ws.nextNonce = null;
						ws.nextNonce = new byte[dValue.length];
						for (int i = 0; i < dValue.length; i++)
							ws.nextNonce[i] = dValue[i];
					}
					else
					{
						tsLib.debugPrint(DEBUG_DM, ": dValue is NULL");
					}
					String temp = new String(ws.nextNonce);
					tsLib.debugPrint(DEBUG_DM, "B64 decode nextNonce" + temp);
				}
				else
				{
					if (_SYNCML_TS_DM_VERSION_V11_)
					{
						szAccBuf = g_AccName;
						szAccBuf = szAccBuf.concat(SYNCML_DMACC_CLIENTNONCE_PATH);
					}
					else
					{
						szAccBuf = dm_AccXNodeInfo.ClientAppAuth;
						szAccBuf = szAccBuf.concat(SYNCML_APPAUTH_AAUTHDATA_PATH);
					}
					dmAgentSetOM(szAccBuf, status.chal.nextnonce);
					ws.nextNonce = status.chal.nextnonce.getBytes();
				}
				tsdmDB.dmdbSetClientNonce(status.chal.nextnonce);// db write

				if (_SYNCML_TS_DM_VERSION_V11_)
				{
					szAccBuf = g_AccName;
					szAccBuf = szAccBuf.concat(SYNCML_DMACC_AUTHPREF_PATH);
				}
				else
				{
					szAccBuf = dm_AccXNodeInfo.Account;
					szAccBuf = szAccBuf.concat(SYNCML_DMACC_AAUTHPREF_PATH);
				}
				dmAgentSetOM(szAccBuf, status.chal.type);
			}
			else if (status.chal.type.compareTo(CRED_TYPE_STRING_BASIC) == 0)
			{
				// @tmp
				ws.credType = CRED_TYPE_BASIC;
				tsdmDB.dmdbSetAuthType(ws.credType); // db save

				if (_SYNCML_TS_DM_VERSION_V11_)
				{
					szAccBuf = g_AccName;
					szAccBuf = szAccBuf.concat(SYNCML_DMACC_AUTHPREF_PATH);
				}
				else
				{
					szAccBuf = dm_AccXNodeInfo.Account;
					szAccBuf = szAccBuf.concat(SYNCML_DMACC_AAUTHPREF_PATH);
				}
				dmAgentSetOM(szAccBuf, status.chal.type);
			}
		}
		return SDM_RET_OK;
	}

	private void dmAgentCmdExecFumo(tsDmWorkspace DmWorkspace, tsDmParserExec exec, tsDmParserItem DmParserItem)
	{
		String szToken = null;
		String szLast = null;
		String szFumoProfile = null;
		int nLen = 0;

		switch (DmWorkspace.nUpdateMechanism)
		{
			case DM_FUMO_MECHANISM_REPLACE:
				tsdmDB.dmdbSetFUMOUpdateMechanism(DM_FUMO_MECHANISM_NONE);
				tsdmDB.dmdbSetFUMOStatus(DM_FUMO_STATE_NONE);
				status = dmBuildcmd.dmBuildCmdStatus(DmWorkspace, (exec.cmdid), CMD_EXEC, null, DmParserItem.target, STATUS_OPTIONAL_FEATURE_NOT_SUPPORTED);
				tsLinkedList.listAddObjAtLast(DmWorkspace.statusList, status);
				break;

			case DM_FUMO_MECHANISM_ALTERNATIVE:
			{
				tsdmDB.dmdbSetFUMOStatus(DM_FUMO_STATE_IDLE_START);
				tsdmDB.dmdbSetFUMOUpdateReportURI(DmParserItem.target);
				tsLib.debugPrint(DEBUG_DM, "Mechanism is DM_FUMO_MECHANISM_ALTERNATIVE");

				if (exec != null)
				{
					status = dmBuildcmd.dmBuildCmdStatus(DmWorkspace, (exec.cmdid), CMD_EXEC, null, DmParserItem.target, STATUS_ACCEPTED_FOR_PROCESSING);
					tsLinkedList.listAddObjAtLast(DmWorkspace.statusList, status);
				}

				// ADD : for ./FUMO/x/Status
				String fumopath = null;
				fumopath = FUMO_PATH;

				szToken = tsLib.libStrstr(DmParserItem.target, fumopath);
				szLast = tsLib.libStrstr(DmParserItem.target, DM_OMA_EXEC_ALTERNATIVE);
				if (szToken != null && szLast != null)
				{
					szToken = tsLib.libStrchr(DmParserItem.target, '.');
					if (!tsLib.isEmpty(szToken))
					{
						try
						{
							int nIndex = 0;

							szToken = szToken.substring(fumopath.length() + 1);
							nIndex = szToken.indexOf(DM_OMA_EXEC_ALTERNATIVE);
							szToken = szToken.substring(0, nIndex);

							szFumoProfile = szToken;
							tsdmDB.dmdbSetFUMOStatusNode(szFumoProfile, nLen);
						}
						catch (Exception e)
						{
							tsLib.debugPrintException(DEBUG_EXCEPTION, "Node Parsing Error");
						}
					}
					else
					{
						tsLib.debugPrintException(DEBUG_EXCEPTION, "Node Parsing Error");
						// do not stop, proceed.
					}
				}
				else
				{
					tsLib.debugPrintException(DEBUG_EXCEPTION, "Node Parsing Error 2");
					// do not stop, proceed.
				}
			}
				break;

			// MOD : for gernic Alert Type for Update Report
			case DM_FUMO_MECHANISM_ALTERNATIVE_DOWNLOAD:
			{
				int nStatus = 0;

				tsdmDB.dmdbSetFUMOUpdateReportURI(DmParserItem.target);
				nStatus = tsdmDB.dmdbGetFUMOStatus();
				tsLib.debugPrint(DEBUG_DM, "nStatus" + nStatus);

				if (nStatus == DM_FUMO_STATE_DOWNLOAD_COMPLETE)
				{
					tsdmDB.dmdbSetFUMOStatus(DM_FUMO_STATE_READY_TO_UPDATE);
				}
				else
				{
					tsdmDB.dmdbSetFUMOStatus(DM_FUMO_STATE_IDLE_START);
				}

				tsLib.debugPrint(DEBUG_DM, "Mechanism is DM_FUMO_MECHANISM_ALTERNATIVE_DOWNLOAD");
				status = dmBuildcmd.dmBuildCmdStatus(DmWorkspace, (exec.cmdid), CMD_EXEC, null, DmParserItem.target, STATUS_ACCEPTED_FOR_PROCESSING);
				tsLinkedList.listAddObjAtLast(DmWorkspace.statusList, status);
				szToken = tsLib.libStrstr(DmParserItem.target, FUMO_PATH);
				szLast = tsLib.libStrstr(DmParserItem.target, DM_OMA_EXEC_ALTERNATIVE_2);
			}
				break;

			case DM_FUMO_MECHANISM_NONE:
				tsdmDB.dmdbSetFUMOStatus(DM_FUMO_STATE_NONE);
				tsdmDB.dmdbSetFUMOUpdateMechanism(DM_FUMO_MECHANISM_NONE);
				status = dmBuildcmd.dmBuildCmdStatus(DmWorkspace, (exec.cmdid), CMD_EXEC, null, DmParserItem.target, STATUS_ACCEPTED_FOR_PROCESSING);
				tsLinkedList.listAddObjAtLast(DmWorkspace.statusList, status);
				tsLib.debugPrint(DEBUG_DM, "Mechanism is DM_FUMO_MECHANISM_NONE");
				break;

			default:
				tsdmDB.dmdbSetFUMOStatus(DM_FUMO_STATE_NONE);
				tsdmDB.dmdbSetFUMOUpdateMechanism(DM_FUMO_MECHANISM_NONE);
				status = dmBuildcmd.dmBuildCmdStatus(DmWorkspace, (exec.cmdid), CMD_EXEC, null, DmParserItem.target, STATUS_COMMAND_FAILED);
				tsLinkedList.listAddObjAtLast(DmWorkspace.statusList, status);
				tsLib.debugPrint(DEBUG_DM, "Mechanism is");
				break;
		}
	}

	public int dmAgentCmdExec(tsDmParserExec exec, boolean isAtomic, tsdmParserStatus atomic_status)
	{
		tsDmWorkspace ws = dm_ws;
		tsDmParserItem item = null;
		tsList cur = null;
		tsDmVnode node = null;
		tsdmParserStatus status = null;
		tsOmTree om = ws.om;

		cur = exec.itemlist;

		while (cur != null)
		{
			item = (tsDmParserItem) cur.item;

			if (ws.serverAuthState != AUTH_STATE_OK)
			{
				tsdmDB.dmdbSetFUMOStatus(DM_FUMO_STATE_NONE); // DB write
				tsdmDB.dmdbSetFUMOUpdateMechanism(DM_FUMO_MECHANISM_NONE); // DB write
				if (item.target != null)
				{
					status = dmBuildcmd.dmBuildCmdStatus(ws, (exec.cmdid), CMD_EXEC, null, item.target, ws.statusReturnCode);
				}
				else
				{
					status = dmBuildcmd.dmBuildCmdStatus(ws, (exec.cmdid), CMD_EXEC, null, null, ws.statusReturnCode);
				}

				tsLinkedList.listAddObjAtLast(ws.statusList, status);
				cur = cur.next;
				continue;
			}

			if (item.target == null)
			{
				tsdmDB.dmdbSetFUMOStatus(DM_FUMO_STATE_NONE); // DB write
				tsdmDB.dmdbSetFUMOUpdateMechanism(DM_FUMO_MECHANISM_NONE); // DB write
				status = dmBuildcmd.dmBuildCmdStatus(ws, (exec.cmdid), CMD_EXEC, null, null, STATUS_NOT_FOUND);
				tsLinkedList.listAddObjAtLast(ws.statusList, status);
				cur = cur.next;
				continue;
			}

			node = tsOmlib.dmOmLibGetNodeProp(om, item.target);
			if (node == null)
			{
				tsdmDB.dmdbSetFUMOStatus(DM_FUMO_STATE_NONE); // DB write
				tsdmDB.dmdbSetFUMOUpdateMechanism(DM_FUMO_MECHANISM_NONE); // DB write

				status = dmBuildcmd.dmBuildCmdStatus(ws, (exec.cmdid), CMD_EXEC, null, item.target, STATUS_NOT_FOUND);
				tsLinkedList.listAddObjAtLast(ws.statusList, status);
				cur = cur.next;
				continue;
			}

			if (!dmAgentIsAccessibleNode(item.target))
			{
				status = dmBuildcmd.dmBuildCmdStatus(ws, (exec.cmdid), CMD_EXEC, null, item.target, STATUS_COMMAND_NOT_ALLOWED);
				tsLinkedList.listAddObjAtLast(ws.statusList, status);
				cur = cur.next;
				continue;
			}
			/* FTA Server Issue */
			if (dmAgentIsPermanentNode(om, item.target)) // exec_permanentnode_check
			{
				status = dmBuildcmd.dmBuildCmdStatus(ws, (exec.cmdid), CMD_EXEC, null, item.target, STATUS_COMMAND_NOT_ALLOWED);
				tsLinkedList.listAddObjAtLast(ws.statusList, status);
				cur = cur.next;
				continue;
			}

			// check ACL
			if (!tsOmlib.dmOmCheckAcl(om, node, OMACL_EXEC))
			{
				status = dmBuildcmd.dmBuildCmdStatus(ws, (exec.cmdid), CMD_EXEC, null, item.target, STATUS_PERMISSION_DENIED);
				tsLinkedList.listAddObjAtLast(ws.statusList, status);
				cur = cur.next;
				continue;
			}

			if (item.target != null)
			{
				boolean bIsScomoExecNode = false;

				tsLib.debugPrint(DEBUG_DM, item.target);

				// MOD : For Dynamic Node
				if (!tsOmlib.dmOmCheckAclCurrentNode(om, item.target, OMACL_EXEC))
				{
					status = dmBuildcmd.dmBuildCmdStatus(ws, (exec.cmdid), CMD_EXEC, null, item.target, STATUS_PERMISSION_DENIED);
					tsLinkedList.listAddObjAtLast(ws.statusList, status);
					cur = cur.next;
					continue;
				}

				if (!item.target.contains(DM_OMA_EXEC_ALTERNATIVE) && !item.target.contains(DM_OMA_EXEC_ALTERNATIVE_2)
						&& !item.target.contains(DM_OMA_EXEC_REPLACE) && !bIsScomoExecNode )
				{
					tsLib.debugPrint(DEBUG_DM, "Node is not exsisted");

					tsdmDB.dmdbSetFUMOStatus(DM_FUMO_STATE_NONE); // DB write
					tsdmDB.dmdbSetFUMOUpdateMechanism(DM_FUMO_MECHANISM_NONE); // DB write
					status = dmBuildcmd.dmBuildCmdStatus(ws, (exec.cmdid), CMD_EXEC, null, item.target, STATUS_OPTIONAL_FEATURE_NOT_SUPPORTED);

					tsLinkedList.listAddObjAtLast(dm_ws.statusList, status);
					cur = cur.next;
					continue;
				}
				else
				{
					if (item.target.contains(DM_OMA_EXEC_REPLACE) || item.target.contains(DM_OMA_EXEC_ALTERNATIVE) || item.target.contains(DM_OMA_EXEC_ALTERNATIVE_2))
					{
						tsdmDB.dmdbSetDmAgentType(SYNCML_DM_AGENT_FUMO);
					}
					else
					{
						tsdmDB.dmdbSetDmAgentType(SYNCML_DM_AGENT_DM);
					}
				}

			}
			else
			{
				tsLib.debugPrint(DEBUG_DM, "Error item->target->pLocURI is NULL");
				tsdmDB.dmdbSetFUMOUpdateMechanism(DM_FUMO_MECHANISM_NONE); // DB write

				tsdmDB.dmdbSetFUMOStatus(DM_FUMO_STATE_NONE); // DB write
				status = dmBuildcmd.dmBuildCmdStatus(ws, (exec.cmdid), CMD_EXEC, null, null, STATUS_FORBIDDEN);
				tsLinkedList.listAddObjAtLast(dm_ws.statusList, status);
				cur = cur.next;
				continue;
			}

			if (!tsLib.isEmpty(exec.correlator))
			{
				tsdmDB.dmdbSetFUMOCorrelator(exec.correlator);
			}

			int nAgentType = tsdmDB.dmdbGetDmAgentType();

			if (nAgentType == SYNCML_DM_AGENT_FUMO)
			{
				dmAgentCmdExecFumo(ws, exec, item);
			}
			else
			{
				tsLib.debugPrint(DEBUG_DM, String.valueOf(nAgentType));
			}

			cur = cur.next;
		}
		return SDM_RET_OK;
	}

	public int dmAgentCmdAlert(tsDmParserAlert alert, boolean isAtomic, tsdmParserStatus atomic_status)
	{
		tsDmWorkspace ws = dm_ws;
		tsdmParserStatus status = null;
		tsDmParserItem item = null;
		tsList cur = null;
		String str = null;

		ws.procState = SyncmlProcessingState.PROC_ALERT;

		if (alert.data != null)
		{
			tsLib.debugPrint(DEBUG_DM, "Code " + alert.data);
		}
		else
		{
			tsLib.debugPrint(DEBUG_DM, "alert->data is NULL");
			return SDM_RET_FAILED; // Defects
		}

		if (ws.serverAuthState != AUTH_STATE_OK)
		{
			status = dmBuildcmd.dmBuildCmdStatus(ws, (alert.cmdid), CMD_ALERT, null, null, ws.statusReturnCode);
			tsLinkedList.listAddObjAtLast(ws.statusList, status);
		}
		// atomic is not supported...
		else if (isAtomic)
		{
			status = dmBuildcmd.dmBuildCmdStatus(ws, (alert.cmdid), CMD_ALERT, null, null, STATUS_NOT_EXECUTED);
			tsLinkedList.listAddObjAtLast(ws.statusList, status);
		}
		else if (alert.data.compareTo(ALERT_NEXT_MESSAGE) == 0)
		{
			ws.nextMsg = true;
			status = dmBuildcmd.dmBuildCmdStatus(ws, (alert.cmdid), CMD_ALERT, null, null, STATUS_OK);
			tsLinkedList.listAddObjAtLast(ws.statusList, status);
		}
		// Mod: change define name
		else if ((alert.data.compareTo(ALERT_DISPLAY) == 0) || (alert.data.compareTo(ALERT_CONTINUE_OR_ABORT) == 0)
				|| (alert.data.compareTo(ALERT_TEXT_INPUT) == 0) || (alert.data.compareTo(ALERT_SINGLE_CHOICE) == 0)
				|| (alert.data.compareTo(ALERT_MULTIPLE_CHOICE) == 0))
		{
			if (ws.uicOption != null)
			{
				tsDmUic.dmUicFreeUicOption(ws.uicOption);
				ws.uicOption = null;
			}

			if (ws.uicOption == null)
			{
				ws.uicOption = tsDmUic.dmUicCreateUicOption();
			}

			ws.uicOption.UICType = tsDmUic.dmUicGetUicType(alert.data);

			cur = alert.itemlist;
			item = (tsDmParserItem) cur.item;

			/* MOD : For UIC */
			if (item.data != null)
			{
				str = tsDmHandlecmd.dmDataStGetString(item.data);
				tsLib.debugPrint(DEBUG_DM, "str = " + str);
			}
			else
			{
				str = null;
				tsLib.debugPrint(DEBUG_DM, "str = NULL");
			}

			if (!tsLib.isEmpty(str))
			{
				if ((alert.data.compareTo(ALERT_DISPLAY) == 0) || (alert.data.compareTo(ALERT_CONTINUE_OR_ABORT) == 0)
						|| (alert.data.compareTo(ALERT_TEXT_INPUT) == 0) || (alert.data.compareTo(ALERT_SINGLE_CHOICE) == 0)
						|| (alert.data.compareTo(ALERT_MULTIPLE_CHOICE) == 0))
				{
					str = SDM_DEFAULT_DISPLAY_UIC_OPTION;
				}
				else
				{
					if (item.data != null && item.data.data != null)
					{
						/* MOD : For UIC */
						str = String.valueOf(item.data.data);
					}
				}
			}

			if (str != null)
			{
				str = tsDmUic.dmUicOptionProcess(str, ws.uicOption);
			}

			cur = cur.next;

			if (cur != null)
			{
				item = (tsDmParserItem) cur.item;
			}

			if (item.data != null)
			{
				str = tsDmHandlecmd.dmDataStGetString(item.data);
			}

			if (str == null)
			{
				if (item.data != null && item.data.data != null)
				{
					str = String.valueOf(item.data.data);
				}
			}

			if (str != null)
			{
				ws.uicOption.text = tsList.listAppendStrText(ws.uicOption.text, str); // need implementation
			}

			// Mod: change define name
			if (alert.data.compareTo(ALERT_SINGLE_CHOICE) == 0 || alert.data.compareTo(ALERT_MULTIPLE_CHOICE) == 0)
			{
				int iuicMenu = 0;
				if (cur != null)
				{
					cur = cur.next;
					while (cur != null)
					{
						item = (tsDmParserItem) cur.item;

						if (item.data != null)
						{
							str = tsDmHandlecmd.dmDataStGetString(item.data);
						}

						if (str == null)
						{
							if (item.data != null && item.data.data != null)
							{
								str = String.valueOf(item.data.data);
							}
						}

						if (str != null)
						{
							ws.uicOption.uicMenuList[iuicMenu++] = str;
						}

						cur = cur.next;
					}
				}
				ws.uicOption.uicMenuNumbers = iuicMenu;
			}

			ws.uicOption.appId = ws.appId;

			if (ws.uicAlert != null)
			{
				tsDmHandlecmd.dmDataStDeleteAlert(ws.uicAlert);
			}
			ws.uicAlert = new tsDmParserAlert();
			tsDmHandlecmd.DMdataStDuplAlert(ws.uicAlert, alert);

			return SDM_PAUSED_BECAUSE_UIC_COMMAND;
		}
		else if (alert.data.compareTo(ALERT_SESSION_ABORT) == 0)
		{
			ws.sessionAbort = 1;
			return SDM_ALERT_SESSION_ABORT;
		}
		else if (alert.data.compareTo(ALERT_GENERIC) == 0)
		{
			// what to do ?

		}
		ws.procState = SyncmlProcessingState.PROC_NONE;
		return SDM_RET_OK;
	}

	public int dmAgentCmdAdd(tsDmParserAdd add, boolean isAtomic, tsdmParserStatus atomic_status)
	{
		tsDmWorkspace ws = dm_ws;
		tsDmParserItem item = null;
		tsdmParserStatus status = null;
		tsOmTree om = ws.om;
		String buf = null;
		String type = null;
		int format = FORMAT_NONE;
		tsDmVnode node;
		tsList cur = null;
		char[] tmpbuf = new char[256];// [DEFAULT_BUFFER_SIZE_4];
		String nodename = null;
		boolean process;
		int bufsize = 0;
		int res = 0;
		int addr;
		String pInbox = null;
		int nFileId = 0;
		boolean bCmdFlag = false;
		// TNDS support wbxml
		String OutBuf = null;
		process = dmAgentCmdUicAlert();

		if (_SYNCML_TS_DM_VERSION_V12_)
			nFileId = tsdmDB.dmdbGetFileIdTNDS();

		cur = add.itemlist;
		while (cur != null)
		{
			type = null;
			format = FORMAT_NONE;
			item = (tsDmParserItem) cur.item;
			
			if(item.meta == null)                                       // modify_meta_110120
			{
				if(add.meta != null)
					item.meta = add.meta;
			}
			else
			{
				if(add.meta != null)
				{
					if(item.meta.type == null)
					{
						if(add.meta.type != null)
							item.meta.type = add.meta.type;
					}
					
					if(item.meta.format == null)
					{
						if(add.meta.format != null)
							item.meta.format = add.meta.format;
					}		
				}
			}
			
			if (ws.serverAuthState != AUTH_STATE_OK)
			{
				if (item.target != null)
				{
					status = dmBuildcmd.dmBuildCmdStatus(ws, (add.cmdid), CMD_ADD, null, item.target, ws.statusReturnCode);
				}
				else
				{
					status = dmBuildcmd.dmBuildCmdStatus(ws, (add.cmdid), CMD_ADD, null, null, ws.statusReturnCode);
				}
				tsLinkedList.listAddObjAtLast(ws.statusList, status);
				cur = cur.next;
				continue;
			}
			if (!process)
			{
				if (item.moredata > 0)
				{
					ws.dataBuffered = true;
				}

				if (item.target != null)
				{
					status = dmBuildcmd.dmBuildCmdStatus(ws, (add.cmdid), CMD_ADD, null, item.target, STATUS_NOT_EXECUTED);
				}
				else
				{
					status = dmBuildcmd.dmBuildCmdStatus(ws, (add.cmdid), CMD_ADD, null, null, STATUS_NOT_EXECUTED);
				}

				tsLinkedList.listAddObjAtLast(ws.statusList, status);
				cur = cur.next;
				continue;
			}

			// if failed atomic block...
			if (isAtomic && ws.atomicStep != SyncmlAtomicStep.ATOMIC_NONE)
			{
				if (ws.tmpItem != null)
				{
					// if failed item?
					if (ws.tmpItem.equals(item))
					{
						if (item.target != null)
						{
							status = dmBuildcmd.dmBuildCmdStatus(ws, (add.cmdid), CMD_ADD, null, item.target, STATUS_ALREADY_EXISTS);
						}
						else
						{
							status = dmBuildcmd.dmBuildCmdStatus(ws, (add.cmdid), CMD_ADD, null, null, STATUS_ALREADY_EXISTS);
						}

						ws.atomicStep = SyncmlAtomicStep.ATOMIC_STEP_NOT_EXEC;
						ws.tmpItem = null;
					}
					else
					{
						if (item.target != null)
						{
							status = dmBuildcmd.dmBuildCmdStatus(ws, (add.cmdid), CMD_ADD, null, item.target, STATUS_ATOMIC_ROLL_BACK_OK);
						}
						else
						{
							status = dmBuildcmd.dmBuildCmdStatus(ws, (add.cmdid), CMD_ADD, null, null, STATUS_ATOMIC_ROLL_BACK_OK);
						}
					}
				}
				else
				{
					if (ws.atomicStep == SyncmlAtomicStep.ATOMIC_STEP_NOT_EXEC)
					{
						if (item.target != null)
						{
							status = dmBuildcmd.dmBuildCmdStatus(ws, (add.cmdid), CMD_ADD, null, item.target, STATUS_NOT_EXECUTED);
						}
						else
						{
							status = dmBuildcmd.dmBuildCmdStatus(ws, (add.cmdid), CMD_ADD, null, null, STATUS_NOT_EXECUTED);
						}
					}
					else
					{
						if (item.target != null)
						{
							status = dmBuildcmd.dmBuildCmdStatus(ws, (add.cmdid), CMD_ADD, null, item.target, STATUS_ATOMIC_ROLL_BACK_OK);
						}
						else
						{
							status = dmBuildcmd.dmBuildCmdStatus(ws, (add.cmdid), CMD_ADD, null, null, STATUS_ATOMIC_ROLL_BACK_OK);
						}
					}
				}
				tsLinkedList.listAddObjAtLast(ws.statusList, status);
				cur = cur.next;
				continue;
			}

			if (item.target == null)
			{
				status = dmBuildcmd.dmBuildCmdStatus(ws, (add.cmdid), CMD_ADD, null, null, STATUS_FORBIDDEN);
				tsLinkedList.listAddObjAtLast(ws.statusList, status);
				cur = cur.next;

				if (isAtomic)
				{
					atomic_status.data = STATUS_ATOMIC_FAILED;
				}
				continue;
			}

			tsLib.debugPrint(DEBUG_DM, item.target);

			// MOD : for Bootstrap Con Node.
			// when Bootstrap case ADD for any condition
			if (dmAgentGetSyncMode() != DM_SYNC_BOOTSTARP)
			{
				node = tsOmlib.dmOmLibGetNodeProp(om, item.target);
				if (node != null && item.moredata == 0 && !ws.dataBuffered)
				{
					pInbox = ddfParser.dmDDFGetMOPath(ddfParser.DM_MO_ID_INBOX);
					if (pInbox == null)
					{
						status = dmBuildcmd.dmBuildCmdStatus(ws, (add.cmdid), CMD_ADD, null, item.target, STATUS_ALREADY_EXISTS);
						tsLinkedList.listAddObjAtLast(ws.statusList, status);
						cur = cur.next;

						// command in Atomic -> atomic status change to STATUS_ATOMIC_FAILED
						if (isAtomic)
						{
							atomic_status.data = STATUS_ATOMIC_FAILED;
						}
						continue;
					}

					String tmp = item.target.substring(0, item.target.length());
					if (tmp.compareTo(pInbox) != 0)
					{
						// already exists(418)
						status = dmBuildcmd.dmBuildCmdStatus(ws, (add.cmdid), CMD_ADD, null, item.target, STATUS_ALREADY_EXISTS);
						tsLinkedList.listAddObjAtLast(ws.statusList, status);
						cur = cur.next;

						// command in Atomic -> atomic status change to STATUS_ATOMIC_FAILED
						if (isAtomic)
						{
							atomic_status.data = STATUS_ATOMIC_FAILED;
						}
						continue;
					}

					// already exists(418)
					status = dmBuildcmd.dmBuildCmdStatus(ws, (add.cmdid), CMD_ADD, null, item.target, STATUS_ALREADY_EXISTS);
					tsLinkedList.listAddObjAtLast(ws.statusList, status);
					cur = cur.next;
					tsLib.debugPrint(DEBUG_DM, "node already Existed[418]");

					// command in Atomic -> atomic status change to STATUS_ATOMIC_FAILED
					if (isAtomic)
					{
						atomic_status.data = STATUS_ATOMIC_FAILED;
					}
					continue;
				}
			}
			else
			{

				tsOmlib.dmOmMakeParentPath(item.target, tmpbuf);
				if (_SYNCML_TS_DM_VERSION_V12_)
				{
					String name = tsLib.libString(tmpbuf);
					dmAgentSetXNodePath(name, item.target, false);
				}
				else
				{
					String name = tsLib.libString(tmpbuf);
					if (name.compareTo("./SyncML/DMAcc") == 0)
					{
						pAccName = item.target;
					}
				}
			}

			tsOmlib.dmOmMakeParentPath(item.target, tmpbuf);
			nodename = tsLib.libString(tmpbuf);
			// check parent path...
			node = tsOmlib.dmOmLibGetNodeProp(om, nodename);
			if (node == null)
			{
				// ADD : Improve Implicit ADD
				boolean bResultImplicitAdd = false;

				bResultImplicitAdd = tsOmlib.dmOmProcessCmdImplicitAdd(om, nodename, OMACL_ADD | OMACL_DELETE | OMACL_GET | OMACL_REPLACE, 1);

				if (!bResultImplicitAdd)
				{
					if (item.moredata > 0)
					{
						ws.dataBuffered = true;
					}

					status = dmBuildcmd.dmBuildCmdStatus(ws, (add.cmdid), CMD_ADD, null, item.target, STATUS_COMMAND_FAILED);
					tsLinkedList.listAddObjAtLast(ws.statusList, status);
					cur = cur.next;

					tsLib.debugPrintException(DEBUG_EXCEPTION, "Node depth is over 15  Command failed500");

					if (isAtomic)
					{
						atomic_status.data = STATUS_ATOMIC_FAILED;
					}
				}
				continue;
			}

			// check ACL
			if (!tsOmlib.dmOmCheckAcl(om, node, OMACL_ADD))
			{
				// @test
				if (item.moredata > 0)
					ws.dataBuffered = true;

				status = dmBuildcmd.dmBuildCmdStatus(ws, (add.cmdid), CMD_ADD, null, item.target, STATUS_PERMISSION_DENIED);
				tsLinkedList.listAddObjAtLast(ws.statusList, status);
				cur = cur.next;

				// command in Atomic -> atomic status change to STATUS_ATOMIC_FAILED
				if (isAtomic)
				{
					atomic_status.data = STATUS_ATOMIC_FAILED;
				}

				continue;
			}

			if (item.meta != null && item.meta.format != null && item.meta.format.compareTo("node") == 0)
			{
				buf = null;
				bufsize = 0;
				ws.dataTotalSize = 0;
				format = FORMAT_NODE;
				if (item.meta.type != null)
				{
					type = item.meta.type;
				}
			}
			else if (item.meta != null)
			{
				if (item.data != null && item.data.data != null)
				{
					buf = tsDmHandlecmd.dmDataStGetString(item.data);
					if (buf == null)
					{
						bufsize = item.data.size;
						buf = String.valueOf(item.data.data);
					}
					else
					{
						bufsize = buf.length();
					}
				}
				if (item.meta.size > 0)
				{
					ws.dataTotalSize = item.meta.size;
					ws.prevBufPos = 0;
				}
				else if (ws.prevBufPos == 0)
				{
					/* MOD : For abnormal case(in case of Format Field omit) */
					if (item.data != null)
					{
						if (item.data.size > 0)
						{
							ws.dataTotalSize = item.data.size;
						}
						else
						{
							ws.dataTotalSize = bufsize;
						}
					}
					else
					{
						ws.dataTotalSize = bufsize;
					}
				}
				if (item.meta.type != null)
				{
					type = item.meta.type;
				}
				if (item.meta.format != null)
				{
					format = tsOmList.dmOmGetFormatFromString(item.meta.format);
				}
			}
			else
			{
				// use no format and type...
				if (item.data != null && item.data.data != null)
				{
					buf = tsDmHandlecmd.dmDataStGetString(item.data);
					if (buf == null)
					{
						bufsize = item.data.size;
						if (item.data.data != null)
							buf = String.valueOf(item.data.data);
						else
							buf = null;
					}
					else
					{
						bufsize = buf.length();
					}

					if (!ws.dataBuffered)
					{
						ws.dataTotalSize = bufsize;
						type = null;
						format = FORMAT_NONE;
					}
				}
				else
				{
					buf = null;
					bufsize = 0;
					ws.dataTotalSize = 0;
					format = FORMAT_NONE;
					type = null;
				}
			}

			if (_SYNCML_TS_DM_VERSION_V12_)
			{
				if (ws.nTNDSFlag)
				{
					// Defects
					if (type != null && buf != null)
					{
						if (type.equals(SYNCML_MIME_TYPE_TNDS_XML))
						{
							tsdmDB.dmAppendFile(nFileId, bufsize, buf.getBytes());
							buf = null;
							type = null;
							if (item.moredata == 0)
							{
								ws.prevBufPos = 0;
								ws.dataBuffered = false;
								ws.dataTotalSize = 0;
								ws.nTNDSFlag = false;
								res = ddfParser.dmDDFCreateTNDSNodeFromFile(nFileId, om);

								if (res > 0)
								{
									status = dmBuildcmd.dmBuildCmdStatus(ws, (add.cmdid), CMD_ADD, null, item.target, STATUS_OK);
									tsLinkedList.listAddObjAtLast(ws.statusList, status);
									cur = cur.next;
									continue;
								}
								status = dmBuildcmd.dmBuildCmdStatus(ws, (add.cmdid), CMD_ADD, null, item.target, STATUS_COMMAND_FAILED);
								tsLinkedList.listAddObjAtLast(ws.statusList, status);
								cur = cur.next;
								continue;
							}

							status = dmBuildcmd.dmBuildCmdStatus(ws, (add.cmdid), CMD_ADD, null, item.target, STATUS_ACCEPTED_AND_BUFFERED);
							tsLinkedList.listAddObjAtLast(ws.statusList, status);
							cur = cur.next;
							continue;
						}
						else if (type.equals(SYNCML_MIME_TYPE_TNDS_WBXML))
						{
							tsLib.debugPrint(DEBUG_DM, "SYNCML_MIME_TYPE_TNDS_WBXML ###\n");

							tsdmDB.dmAppendFile(nFileId, bufsize, buf.getBytes());

							if (item.moredata == 0)
							{
								int nWbxmlDataLen = 0;
								byte[] pWbxmlData = null;
								String WbxmlStr = null;
								ws.prevBufPos = 0;
								ws.dataBuffered = false;
								ws.dataTotalSize = 0;
								ws.nTNDSFlag = false;

								// wbxml to xml convert !!!
								nWbxmlDataLen = tsdmDB.dmdbGetFileSize(nFileId);
								pWbxmlData = new byte[nWbxmlDataLen];
								tsdmDB.dmReadFile(nFileId, 0, nWbxmlDataLen, pWbxmlData);
								WbxmlStr = new String(pWbxmlData);
								OutBuf = ddfParser.TndsWbxmlParse(WbxmlStr, WbxmlStr.length());
								int outBufSize = 0; // Defects
								if (OutBuf != null)
								{
									outBufSize = OutBuf.length();
								}
								res = ddfParser.dmDDFCreateTNDSNode(OutBuf, outBufSize, om);
								ddfParser.TndsParseFinish();

								if (res > 0)
								{
									status = dmBuildcmd.dmBuildCmdStatus(ws, (add.cmdid), CMD_ADD, null, item.target, STATUS_OK);
									tsLinkedList.listAddObjAtLast(ws.statusList, status);
									cur = cur.next;
									continue;
								}
								status = dmBuildcmd.dmBuildCmdStatus(ws, (add.cmdid), CMD_ADD, null, item.target, STATUS_COMMAND_FAILED);
								tsLinkedList.listAddObjAtLast(ws.statusList, status);
								cur = cur.next;
								continue;
							}
							status = dmBuildcmd.dmBuildCmdStatus(ws, (add.cmdid), CMD_ADD, null, item.target, STATUS_ACCEPTED_AND_BUFFERED);
							tsLinkedList.listAddObjAtLast(ws.statusList, status);
							cur = cur.next;
							continue;
						}
					}
				}

				// Defects
				if (type != null && buf != null)// && format == FORMAT_XML)
				{
					if (type.compareTo(SYNCML_MIME_TYPE_TNDS_XML) == 0)
					{

						if (item.moredata > 0)
						{
							ws.prevBufPos += bufsize;
							ws.dataBuffered = true;

							if (!ws.nTNDSFlag)
							{
								ws.nTNDSFlag = true;
								tsdmDB.dmdbDeleteFile(nFileId);
							}

							tsdmDB.dmAppendFile(nFileId, bufsize, buf.getBytes());
							// create status for data buffered...
							status = dmBuildcmd.dmBuildCmdStatus(ws, (add.cmdid), CMD_ADD, null, item.target, STATUS_ACCEPTED_AND_BUFFERED);
							tsLinkedList.listAddObjAtLast(ws.statusList, status);
							cur = cur.next;
							continue;
						}
						res = ddfParser.dmDDFCreateTNDSNode(buf, bufsize, om);
						if (res > 0)
						{
							status = dmBuildcmd.dmBuildCmdStatus(ws, (add.cmdid), CMD_ADD, null, item.target, STATUS_OK);
							tsLinkedList.listAddObjAtLast(ws.statusList, status);
							cur = cur.next;

							// DM 1.2
							// 2006.06.13 kangdongsoo
							res = dmAgentGetAccountFromOM(om);

							continue;
						}

						buf = null;
						type = null;
						status = dmBuildcmd.dmBuildCmdStatus(ws, (add.cmdid), CMD_ADD, null, item.target, STATUS_COMMAND_FAILED);
						tsLinkedList.listAddObjAtLast(ws.statusList, status);
						cur = cur.next; // ADD. 2006.03.22
						tsLib.debugPrintException(DEBUG_EXCEPTION, " Fail.\n");
						return SDM_RET_FAILED;
					}
					else if (type.compareTo(SYNCML_MIME_TYPE_TNDS_WBXML) == 0)
					{
						tsLib.debugPrint(DEBUG_DM, "SYNCML_MIME_TYPE_TNDS_WBXML ###\n");
						if (item.moredata > 0)
						{
							ws.prevBufPos += bufsize;
							ws.dataBuffered = true;

							if (!ws.nTNDSFlag)
							{
								ws.nTNDSFlag = true;
								tsdmDB.dmdbDeleteFile(nFileId);
							}

							tsdmDB.dmAppendFile(nFileId, bufsize, buf.getBytes());
							status = dmBuildcmd.dmBuildCmdStatus(ws, (add.cmdid), CMD_ADD, null, item.target, STATUS_ACCEPTED_AND_BUFFERED);
							tsLinkedList.listAddObjAtLast(ws.statusList, status);
							cur = cur.next;
							continue;
						}

						OutBuf = ddfParser.TndsWbxmlParse(buf, bufsize);
						int outBufSize = 0; // Defects
						if (OutBuf != null)
						{
							outBufSize = OutBuf.length();
						}
						res = ddfParser.dmDDFCreateTNDSNode(OutBuf, outBufSize, om);
						ddfParser.TndsParseFinish();

						if (res > 0)
						{
							status = dmBuildcmd.dmBuildCmdStatus(ws, (add.cmdid), CMD_ADD, null, item.target, STATUS_OK);
							tsLinkedList.listAddObjAtLast(ws.statusList, status);
							cur = cur.next;

							if (dmAgentGetSyncMode() != DM_SYNC_BOOTSTARP) // tnds bootstrap 080314
							{
								res = dmAgentGetAccountFromOM(om);
							}

							continue;
						}

						buf = null;
						type = null;
						status = dmBuildcmd.dmBuildCmdStatus(ws, (add.cmdid), CMD_ADD, null, item.target, STATUS_COMMAND_FAILED);
						tsLinkedList.listAddObjAtLast(ws.statusList, status);
						cur = cur.next;
						tsLib.debugPrintException(DEBUG_EXCEPTION, "sdmProcessCmdAdd : Warning!!!. Fail.\n");
						return SDM_RET_FAILED;
					}
				}
			}

			// write data
			if (ws.dataTotalSize == 0)
			{
				res = (int) tsOmlib.dmOmWrite(om, item.target, ws.dataTotalSize, 0, buf, bufsize);

				if (_SYNCML_TS_DM_VERSION_V12_)
				{
					dmAgentSetAclDynamicFUMONode(om, item.target); // dynamic_node_patch
				}

				tsLib.debugPrint(DEBUG_DM, "ADD (NO DATA)");
				node = tsOmlib.dmOmLibGetNodeProp(om, item.target);
				if (format != FORMAT_NONE)
				{
					node.format = format;
				}
			}
			else
			{
				// check device storage
				if (ws.prevBufPos == 0)
				{
					// MOD : For Dynamic Node
					if (item.target.compareTo(DM_OMA_REPLACE) != 0)
					{
						addr = tsOmVfs.dmOmvfsGetFreeVaddr(ws.om.vfs, ws.dataTotalSize);
					}
					else
					{
						addr = 0;
					}

					if (addr < 0)
					{
						// device full
						status = dmBuildcmd.dmBuildCmdStatus(ws, (add.cmdid), CMD_ADD, null, item.target, STATUS_DEVICE_FULL);
						tsLinkedList.listAddObjAtLast(ws.statusList, status);
						tsLib.debugPrint(DEBUG_DM, "ADD STATUS_DEVICE_FULL");

						cur = cur.next;

						if (item.moredata > 0)
						{
							ws.dataBuffered = true;
						}

						// command in Atomic -> atomic status change to STATUS_ATOMIC_FAILED
						if (isAtomic)
						{
							atomic_status.data = STATUS_ATOMIC_FAILED;
						}
						continue;
					}
				}

				res = (int) tsOmlib.dmOmWrite(om, item.target, ws.dataTotalSize, ws.prevBufPos, buf, bufsize);
			}

			// check data writing status
			if (res < 0)
			{
				tsLib.debugPrintException(DEBUG_EXCEPTION, String.valueOf(res));
				// @test
				if (item.moredata > 0)
				{
					ws.dataBuffered = true;
				}

				// command failed(500)
				status = dmBuildcmd.dmBuildCmdStatus(ws, (add.cmdid), CMD_ADD, null, item.target, STATUS_COMMAND_FAILED);
				tsLinkedList.listAddObjAtLast(ws.statusList, status);

				buf = null;
				ws.dataBuffered = false;
				cur = cur.next;

				if (isAtomic)
				{
					atomic_status.data = STATUS_ATOMIC_FAILED;
				}
				continue;
			}

			node = tsOmlib.dmOmLibGetNodeProp(om, item.target);
			if (type != null)
			{
				if (node.type != null)
				{
					tsOmlib.dmOmDeleteMimeList(node.type);
				}
				node.type = new tsOmList();
				node.type.data = type;
				node.type.next = null;
			}
			if (format != FORMAT_NONE)
			{
				node.format = format;
			}
			// remove buffer

			buf = null;
			// checking chunked data (= need more data?)
			if (item.moredata == 0)
			{
				ws.prevBufPos = 0;
				ws.dataBuffered = false;
				ws.dataTotalSize = 0;

				// create status ok
				status = dmBuildcmd.dmBuildCmdStatus(ws, (add.cmdid), CMD_ADD, null, item.target, STATUS_OK);
			}
			else
			{
				ws.prevBufPos += bufsize;
				ws.dataBuffered = true;

				// create status for data buffered...
				status = dmBuildcmd.dmBuildCmdStatus(ws, (add.cmdid), CMD_ADD, null, item.target, STATUS_ACCEPTED_AND_BUFFERED);
			}
			tsLinkedList.listAddObjAtLast(ws.statusList, status);
			cur = cur.next;
		}
		return SDM_RET_OK;
	}

	public int dmAgentGetAccountFromOM(tsOmTree om)
	{
		tsdmInfo pProfileInfo = null;
		tsDmVnode node = null;
		int size; // Defects
		int format; // Defects
		char[] TmpBuf = null;
		String AddrURI = null;
		String PortNum = null;
		String pAccTmpBuf = null;
		char[] ServerUrl = new char[256];
		int aclValue;
		int authType = 0;
		tsDBURLParser getParser;

		pProfileInfo = new tsdmInfo();

		if (dm_AccXNodeTndsInfo.Account == null)
		{
			dmAgentClose();
			return SDM_RET_FAILED;
		}
		else if (dm_AccXNodeTndsInfo.AppAddr == null)
		{
			dmAgentClose();
			return SDM_RET_FAILED;
		}
		else if (dm_AccXNodeTndsInfo.AppAddrPort == null)
		{
			dmAgentClose();
			return SDM_RET_FAILED;
		}
		else if (dm_AccXNodeTndsInfo.ClientAppAuth == null)
		{
			dmAgentClose();
			return SDM_RET_FAILED;
		}
		else if (dm_AccXNodeTndsInfo.ServerAppAuth == null)
		{
			dmAgentClose();
			return SDM_RET_FAILED;
		}
		else if (dm_AccXNodeTndsInfo.ToConRef == null)
		{
			dmAgentClose();
			return SDM_RET_FAILED;
		}

		pAccTmpBuf = dm_AccXNodeTndsInfo.Account;
		pAccTmpBuf = pAccTmpBuf.concat(SYNCML_DMACC_APPID_PATH);
		node = tsOmlib.dmOmLibGetNodeProp(om, pAccTmpBuf);
		if (node != null)
		{ // Defects
			size = node.size;
		}
		else
		{
			size = 0;
		}
		TmpBuf = new char[size];
		tsOmlib.dmOmRead(om, pAccTmpBuf, 0, TmpBuf, size);
		pProfileInfo.AppID = String.valueOf(TmpBuf);

		pAccTmpBuf = dm_AccXNodeTndsInfo.Account;
		pAccTmpBuf = pAccTmpBuf.concat(SYNCML_DMACC_SERVERID_PATH);
		node = tsOmlib.dmOmLibGetNodeProp(om, pAccTmpBuf);
		if (node != null)
		{ // Defects
			size = node.size;
		}
		else
		{
			size = 0;
		}
		TmpBuf = new char[size];
		tsOmlib.dmOmRead(om, pAccTmpBuf, 0, TmpBuf, size);
		pProfileInfo.ServerID = String.valueOf(TmpBuf);
		tsLib.debugPrint(DEBUG_DM, "get DM informations from OM...ServerId  \r\n" + String.valueOf(TmpBuf));

		pAccTmpBuf = dm_AccXNodeTndsInfo.Account;
		pAccTmpBuf = pAccTmpBuf.concat(SYNCML_DMACC_NAME_PATH);
		node = tsOmlib.dmOmLibGetNodeProp(om, pAccTmpBuf);
		if (node != null)
		{ // Defects
			TmpBuf = new char[node.size];
			tsOmlib.dmOmRead(om, pAccTmpBuf, 0, TmpBuf, node.size);
			pProfileInfo.ProfileName = String.valueOf(TmpBuf);
		}
		else
		{
			pProfileInfo.ProfileName = pProfileInfo.ServerID;
			aclValue = OMACL_ADD | OMACL_DELETE | OMACL_GET | OMACL_REPLACE;
			dmAgentSetOMAccStr(om, pAccTmpBuf, pProfileInfo.ProfileName, aclValue, SCOPE_DYNAMIC);
		}

		pAccTmpBuf = dm_AccXNodeTndsInfo.Account;
		pAccTmpBuf = pAccTmpBuf.concat(SYNCML_DMACC_PREFCONREF_PATH);
		node = tsOmlib.dmOmLibGetNodeProp(om, pAccTmpBuf);
		if (node != null)
		{ // Defects
			size = node.size;
		}
		else
		{
			size = 0;
		}
		TmpBuf = new char[size];
		tsOmlib.dmOmRead(om, pAccTmpBuf, 0, TmpBuf, size);
		pProfileInfo.PrefConRef = String.valueOf(TmpBuf);

		pAccTmpBuf = dm_AccXNodeTndsInfo.AppAddr;
		pAccTmpBuf = pAccTmpBuf.concat(SYNCML_APPADDR_ADDR_PATH);
		node = tsOmlib.dmOmLibGetNodeProp(om, pAccTmpBuf);
		if (node != null)
		{ // Defects
			size = node.size;
		}
		else
		{
			size = 0;
		}
		TmpBuf = new char[size];
		tsOmlib.dmOmRead(om, pAccTmpBuf, 0, TmpBuf, size);
		AddrURI = String.valueOf(TmpBuf);
		tsLib.debugPrint(DEBUG_DM, "dmAgentGetDMInfoFromOmTree : get DM informations from OM...AddURI  \r\n" + AddrURI);

		pAccTmpBuf = dm_AccXNodeTndsInfo.AppAddrPort;
		pAccTmpBuf = pAccTmpBuf.concat(SYNCML_APPADDR_PORT_PORTNUMBER_PATH);
		node = tsOmlib.dmOmLibGetNodeProp(om, pAccTmpBuf);
		if (node != null)
		{ // Defects
			size = node.size;
		}
		else
		{
			size = 0;
		}
		TmpBuf = new char[size];
		tsOmlib.dmOmRead(om, pAccTmpBuf, 0, TmpBuf, size);
		PortNum = String.valueOf(TmpBuf);

		ServerUrl = tsDB.dbDoDMBootStrapURI(ServerUrl, AddrURI.toCharArray(), PortNum.toCharArray());
		if (ServerUrl == null)
			return SDM_RET_FAILED;
		pProfileInfo.ServerUrl = new String(ServerUrl);
		getParser = tsDB.dbURLParser(pProfileInfo.ServerUrl);
		pProfileInfo.ServerUrl = getParser.pURL;
		pProfileInfo.ServerIP = getParser.pAddress;
		pProfileInfo.Path = getParser.pPath;
		pProfileInfo.ServerPort = getParser.nPort;
		pProfileInfo.Protocol = getParser.pProtocol;

		pProfileInfo.ServerUrl_Org = pProfileInfo.ServerUrl;
		pProfileInfo.ServerIP_Org = pProfileInfo.ServerIP;
		pProfileInfo.Path_Org = pProfileInfo.Path;
		pProfileInfo.Protocol_Org = pProfileInfo.Protocol;
		pProfileInfo.ServerPort_Org = pProfileInfo.ServerPort;
		pProfileInfo.bChangedProtocol = false;

		pAccTmpBuf = dm_AccXNodeTndsInfo.ClientAppAuth;
		pAccTmpBuf = pAccTmpBuf.concat(SYNCML_APPAUTH_AAUTHLEVEL_PATH);
		node = tsOmlib.dmOmLibGetNodeProp(om, pAccTmpBuf);
		if (node != null)
		{ // Defects
			size = node.size;
		}
		else
		{
			size = 0;
		}
		TmpBuf = new char[size];
		tsOmlib.dmOmRead(om, pAccTmpBuf, 0, TmpBuf, size);
		pProfileInfo.AuthLevel = String.valueOf(TmpBuf);

		pAccTmpBuf = dm_AccXNodeTndsInfo.ClientAppAuth;
		pAccTmpBuf = pAccTmpBuf.concat(SYNCML_APPAUTH_AAUTHTYPE_PATH);
		node = tsOmlib.dmOmLibGetNodeProp(om, pAccTmpBuf);
		if (node != null)
		{ // Defects
			size = node.size;
		}
		else
		{
			size = 0;
		}
		TmpBuf = new char[size];
		tsOmlib.dmOmRead(om, pAccTmpBuf, 0, TmpBuf, size);

		if (String.valueOf(TmpBuf).compareTo(CRED_TYPE_MD5_STR) == 0)
		{
			authType = CRED_TYPE_MD5;
		}
		else if (String.valueOf(TmpBuf).compareTo(CRED_TYPE_BASIC_STR) == 0)
		{
			authType = CRED_TYPE_BASIC;
		}
		else if (String.valueOf(TmpBuf).compareTo(CRED_TYPE_HMAC_STR) == 0)
		{
			authType = CRED_TYPE_HMAC;
		}
		else if (String.valueOf(TmpBuf).compareTo(CRED_TYPE_DIGEST_STR) == 0)
		{
			authType = CRED_TYPE_MD5;
		}
		else
		{
			authType = CRED_TYPE_BASIC;
		}
		pProfileInfo.AuthType = authType;

		pAccTmpBuf = dm_AccXNodeTndsInfo.ClientAppAuth;
		pAccTmpBuf = pAccTmpBuf.concat(SYNCML_APPAUTH_AAUTHNAME_PATH);
		node = tsOmlib.dmOmLibGetNodeProp(om, pAccTmpBuf);
		if (node != null)
		{ // Defects
			size = node.size;
		}
		else
		{
			size = 0;
		}
		TmpBuf = new char[size];
		tsOmlib.dmOmRead(om, pAccTmpBuf, 0, TmpBuf, size);
		pProfileInfo.UserName = String.valueOf(TmpBuf);

		pAccTmpBuf = dm_AccXNodeTndsInfo.ClientAppAuth;
		pAccTmpBuf = pAccTmpBuf.concat(SYNCML_APPAUTH_AAUTHSECRET_PATH);
		node = tsOmlib.dmOmLibGetNodeProp(om, pAccTmpBuf);
		if (node != null)
		{ // Defects
			size = node.size;
		}
		else
		{
			size = 0;
		}
		TmpBuf = new char[size];
		tsOmlib.dmOmRead(om, pAccTmpBuf, 0, TmpBuf, size);
		pProfileInfo.Password = String.valueOf(TmpBuf);

		pAccTmpBuf = dm_AccXNodeTndsInfo.ClientAppAuth;
		pAccTmpBuf = pAccTmpBuf.concat(SYNCML_APPAUTH_AAUTHDATA_PATH);
		node = tsOmlib.dmOmLibGetNodeProp(om, pAccTmpBuf);
		if (node != null)
		{ // Defects
			size = node.size;
			format = node.format;
		}
		else
		{
			size = 0;
			format = FORMAT_NONE;
		}
		TmpBuf = new char[size];
		tsOmlib.dmOmRead(om, pAccTmpBuf, 0, TmpBuf, size);
		pProfileInfo.ClientNonce = String.valueOf(TmpBuf);
		pProfileInfo.ClientNonceFormat = format;

		pAccTmpBuf = dm_AccXNodeTndsInfo.ServerAppAuth;
		pAccTmpBuf = pAccTmpBuf.concat(SYNCML_APPAUTH_AAUTHLEVEL_PATH);
		node = tsOmlib.dmOmLibGetNodeProp(om, pAccTmpBuf);
		if (node != null)
		{ // Defects
			size = node.size;
		}
		else
		{
			size = 0;
		}
		TmpBuf = new char[size];
		tsOmlib.dmOmRead(om, pAccTmpBuf, 0, TmpBuf, size);
		pProfileInfo.ServerAuthLevel = String.valueOf(TmpBuf);

		pAccTmpBuf = dm_AccXNodeTndsInfo.ServerAppAuth;
		pAccTmpBuf = pAccTmpBuf.concat(SYNCML_APPAUTH_AAUTHTYPE_PATH);
		node = tsOmlib.dmOmLibGetNodeProp(om, pAccTmpBuf);
		if (node != null)
		{ // Defects
			size = node.size;
		}
		else
		{
			size = 0;
		}
		TmpBuf = new char[size];
		tsOmlib.dmOmRead(om, pAccTmpBuf, 0, TmpBuf, size);

		if (String.valueOf(TmpBuf).compareTo(CRED_TYPE_MD5_STR) == 0)
		{
			authType = CRED_TYPE_MD5;
		}
		else if (String.valueOf(TmpBuf).compareTo(CRED_TYPE_BASIC_STR) == 0)
		{
			authType = CRED_TYPE_BASIC;
		}
		else if (String.valueOf(TmpBuf).compareTo(CRED_TYPE_HMAC_STR) == 0)
		{
			authType = CRED_TYPE_HMAC;
		}
		else if (String.valueOf(TmpBuf).compareTo(CRED_TYPE_DIGEST_STR) == 0)
		{
			authType = CRED_TYPE_MD5;
		}
		else
		{
			authType = CRED_TYPE_BASIC;
		}
		pProfileInfo.nServerAuthType = authType;

		pAccTmpBuf = dm_AccXNodeTndsInfo.ServerAppAuth;
		pAccTmpBuf = pAccTmpBuf.concat(SYNCML_APPAUTH_AAUTHNAME_PATH);
		node = tsOmlib.dmOmLibGetNodeProp(om, pAccTmpBuf);
		if (node != null)
		{ // Defects
			size = node.size;
		}
		else
		{
			size = 0;
		}
		TmpBuf = new char[size];
		tsOmlib.dmOmRead(om, pAccTmpBuf, 0, TmpBuf, size);
		pProfileInfo.ServerID = String.valueOf(TmpBuf);

		pAccTmpBuf = dm_AccXNodeTndsInfo.ServerAppAuth;
		pAccTmpBuf = pAccTmpBuf.concat(SYNCML_APPAUTH_AAUTHSECRET_PATH);
		node = tsOmlib.dmOmLibGetNodeProp(om, pAccTmpBuf);
		if (node != null)
		{ // Defects
			size = node.size;
		}
		else
		{
			size = 0;
		}
		TmpBuf = new char[size];
		tsOmlib.dmOmRead(om, pAccTmpBuf, 0, TmpBuf, size);
		pProfileInfo.ServerPwd = String.valueOf(TmpBuf);

		pAccTmpBuf = dm_AccXNodeTndsInfo.ServerAppAuth;
		pAccTmpBuf = pAccTmpBuf.concat(SYNCML_APPAUTH_AAUTHDATA_PATH);
		node = tsOmlib.dmOmLibGetNodeProp(om, pAccTmpBuf);
		if (node != null)
		{ // Defects
			size = node.size;
			format = node.format;
		}
		else
		{
			size = 0;
			format = FORMAT_NONE;
		}
		TmpBuf = new char[size];
		tsOmlib.dmOmRead(om, pAccTmpBuf, 0, TmpBuf, size);
		pProfileInfo.ServerNonce = String.valueOf(TmpBuf);
		pProfileInfo.ServerNonceFormat = format;

		// Ext
		pAccTmpBuf = dm_AccXNodeTndsInfo.Account;
		pAccTmpBuf = pAccTmpBuf.concat(SYNCML_DMACC_EXT_PATH);
		node = tsOmlib.dmOmLibGetNodeProp(om, pAccTmpBuf);
/*		if (node != null)
		{
			// vendor specific information
		}
		else
		{
			// vendor specific information
		}*/
		pAccTmpBuf = null;

		{
			int Index = 0;
			Index = tsdmDB.dmdbSetActiveProfileIndexByServerID(pProfileInfo.ServerID);

			tsdmDB.dmdbSetProfileInfo(pProfileInfo);
			tsdmDB.dmdbSetProfileName(Index, pProfileInfo.ProfileName);
		}
		pProfileInfo = null;
		dm_AccXNodeTndsInfo = null;
		return SDM_RET_OK;
	}

	public int dmAgentCmdReplace(tsDmParserReplace replace, boolean isAtomic, tsdmParserStatus atomic_status)
	{
		tsDmWorkspace ws = dm_ws;
		tsOmTree om = ws.om;
		tsDmParserItem item;
		tsdmParserStatus status;
		String type = null;
		String buf = null;
		tsList cur;
		tsDmVnode node;
		int format = FORMAT_NONE;
		tsOmList list;
		boolean process;
		int bufsize = 0;
		int res = SDM_RET_OK;
		int nFileId = 0;
		boolean bCmdFlag = false;

		process = dmAgentCmdUicAlert();

		if (_SYNCML_TS_DM_VERSION_V12_)
			nFileId = tsdmDB.dmdbGetFileIdTNDS();

		cur = replace.itemlist;
		while (cur != null)
		{
			item = (tsDmParserItem) cur.item;
			
			if(item.meta == null)                                       // modify_meta_110120
			{
				if(replace.meta != null)
					item.meta = replace.meta;
			}
			else
			{
				if(replace.meta != null)
				{
					if(item.meta.type == null)
					{
						if(replace.meta.type != null)
							item.meta.type = replace.meta.type;
					}
					
					if(item.meta.format == null)
					{
						if(replace.meta.format != null)
							item.meta.format = replace.meta.format;
					}		
				}
			}
				
			status = null;

			if (ws.serverAuthState != AUTH_STATE_OK)
			{
				if (item.target != null)
				{
					status = dmBuildcmd.dmBuildCmdStatus(ws, (replace.cmdid), CMD_REPLACE, null, item.target, ws.statusReturnCode);
				}
				else
				{
					status = dmBuildcmd.dmBuildCmdStatus(ws, (replace.cmdid), CMD_REPLACE, null, null, ws.statusReturnCode);
				}

				tsLinkedList.listAddObjAtLast(ws.statusList, status);
				cur = cur.next;
				continue;
			}
			if (item.data != null)
			{
				tsLib.debugPrint(DEBUG_DM, String.valueOf(item.data.size));
			}

			// if failed atomic block...
			if (isAtomic && ws.atomicStep != SyncmlAtomicStep.ATOMIC_NONE)
			{
				status = null;
				if (ws.tmpItem != null)
				{
					// if failed item?
					if (ws.tmpItem.equals(item))
					{
						if (item.target != null)
						{
							status = dmBuildcmd.dmBuildCmdStatus(ws, (replace.cmdid), CMD_REPLACE, null, item.target, STATUS_NOT_FOUND);
						}
						else
						{
							status = dmBuildcmd.dmBuildCmdStatus(ws, (replace.cmdid), CMD_REPLACE, null, null, STATUS_NOT_FOUND);
						}
						ws.atomicStep = SyncmlAtomicStep.ATOMIC_STEP_NOT_EXEC;
						ws.tmpItem = null;
					}
					else
					{
						if (item.target != null)
						{
							status = dmBuildcmd.dmBuildCmdStatus(ws, (replace.cmdid), CMD_REPLACE, null, item.target, STATUS_ATOMIC_ROLL_BACK_OK);
						}
						else
						{
							status = dmBuildcmd.dmBuildCmdStatus(ws, (replace.cmdid), CMD_REPLACE, null, null, STATUS_ATOMIC_ROLL_BACK_OK);
						}
					}
				}
				else
				{
					if (ws.atomicStep == SyncmlAtomicStep.ATOMIC_STEP_NOT_EXEC)
					{
						if (item.target != null)
						{
							status = dmBuildcmd.dmBuildCmdStatus(ws, (replace.cmdid), CMD_REPLACE, null, item.target, STATUS_NOT_EXECUTED);
						}
						else
						{
							status = dmBuildcmd.dmBuildCmdStatus(ws, (replace.cmdid), CMD_REPLACE, null, null, STATUS_NOT_EXECUTED);
						}
					}
					else
					{
						if (item.target != null)
						{
							status = dmBuildcmd.dmBuildCmdStatus(ws, (replace.cmdid), CMD_REPLACE, null, item.target, STATUS_ATOMIC_ROLL_BACK_OK);
						}
						else
						{
							status = dmBuildcmd.dmBuildCmdStatus(ws, (replace.cmdid), CMD_REPLACE, null, null, STATUS_ATOMIC_ROLL_BACK_OK);
						}
					}
				}

				if (status != null)
				{
					tsLinkedList.listAddObjAtLast(ws.statusList, status);
				}
				cur = cur.next;
				continue;
			}

			if (item.target == null)
			{
				status = dmBuildcmd.dmBuildCmdStatus(ws, (replace.cmdid), CMD_REPLACE, null, null, STATUS_NOT_FOUND);
				cur = cur.next;
				continue;
			}

			// if request node property...
			if (item.target.contains("?"))
			{
				res = dmAgentCmdProp(CMD_REPLACE, item, replace);
				cur = cur.next;
				continue;
			}

			node = tsOmlib.dmOmLibGetNodeProp(om, item.target);
			if (!process)
			{
				status = dmBuildcmd.dmBuildCmdStatus(ws, (replace.cmdid), CMD_REPLACE, null, item.target, STATUS_NOT_EXECUTED);

				// command in Atomic -> atomic status change to STATUS_ATOMIC_FAILED
				if ((isAtomic) && (atomic_status != null))
				{
					atomic_status.data = STATUS_ATOMIC_FAILED;
				}
				// MOD : For improved Replace cmd
				if (status != null)
				{
					tsLinkedList.listAddObjAtLast(ws.statusList, status);
				}
				cur = cur.next;
				continue;
			}
			else if (node == null)
			{
				tsLib.debugPrint(DEBUG_DM, "node == null(not exist) : " + item.target);

				if (_SYNCML_TS_FOTA_ && (item.target.contains(DM_OMA_ALTERNATIVE) || item.target.contains(DM_OMA_ALTERNATIVE_2) || item.target.contains(DM_OMA_REPLACE)))
				{
					dmAgentReMakeFwUpdateNode(om, item.target);
				}
				else
				{
					status = dmBuildcmd.dmBuildCmdStatus(ws, (replace.cmdid), CMD_REPLACE, null, item.target, STATUS_NOT_FOUND);
					// command in Atomic -> atomic status change to STATUS_ATOMIC_FAILED
					if ((isAtomic) && (atomic_status != null))
						atomic_status.data = STATUS_ATOMIC_FAILED;

					// MOD : For improved Replace cmd
					if (status != null)
						tsLinkedList.listAddObjAtLast(ws.statusList, status);

					cur = cur.next;
					continue;
				}
			}
			else if (dmAgentIsPermanentNode(om, item.target))
			{
				tsLib.debugPrint(DEBUG_DM, " Fail");
				status = dmBuildcmd.dmBuildCmdStatus(ws, (replace.cmdid), CMD_REPLACE, null, item.target, STATUS_COMMAND_NOT_ALLOWED);

				// command in Atomic -> atomic status change to STATUS_ATOMIC_FAILED
				if ((isAtomic) && (atomic_status != null))
				{
					atomic_status.data = STATUS_ATOMIC_FAILED;
				}
				// MOD : For improved Replace cmd
				if (status != null)
				{
					tsLinkedList.listAddObjAtLast(ws.statusList, status);
				}
				cur = cur.next;
				continue;
			}
			else if (!tsOmlib.dmOmCheckAcl(om, node, OMACL_REPLACE))
			{
				tsLib.debugPrint(DEBUG_DM, " Fail");
				status = dmBuildcmd.dmBuildCmdStatus(ws, (replace.cmdid), CMD_REPLACE, null, item.target, STATUS_PERMISSION_DENIED);

				// command in Atomic -> atomic status change to STATUS_ATOMIC_FAILED
				if ((isAtomic) && (atomic_status != null))
				{
					atomic_status.data = STATUS_ATOMIC_FAILED;
				}
				// MOD : For improved Replace cmd
				if (status != null)
				{
					tsLinkedList.listAddObjAtLast(ws.statusList, status);
				}
				cur = cur.next;
				continue;
			}

			tsLib.debugPrint(DEBUG_DM, " else");
			tsLib.debugPrint(DEBUG_DM, item.target);

			if (item.meta != null)
			{
				if (item.meta.type != null)
				{
					type = item.meta.type;
				}
				if (item.meta.format != null)
				{
					format = tsOmList.dmOmGetFormatFromString(item.meta.format);
				}

				if (item.meta.size > 0)
				{
					ws.dataTotalSize = item.meta.size;
					ws.prevBufPos = 0;
				}
				else if (ws.prevBufPos == 0)
				{
					if (item.data != null)
					{
						ws.dataTotalSize = item.data.size;
					}
					else
					{
						ws.dataTotalSize = 0;
					}
				}

				if (item.data != null && item.data.data != null)
				{
					buf = tsDmHandlecmd.dmDataStGetString(item.data);
					if (buf == null)
					{
						bufsize = item.data.size;
						buf = String.valueOf(item.data.data);
					}
					else
					{
						bufsize = buf.length();
					}
				}
				else
				{
					bufsize = 0;
					buf = null;
					ws.dataTotalSize = 0;
					tsLib.debugPrint(DEBUG_DM, "REPLACE ( no item->data)");
				}
			}
			else if (item.data != null && item.data.data != null)
			{
				buf = tsDmHandlecmd.dmDataStGetString(item.data);
				if (buf == null)
				{
					bufsize = item.data.size;
					buf = String.valueOf(item.data.data);
				}
				else
				{
					bufsize = buf.length();
				}
				if (!ws.dataBuffered)
				{
					ws.dataTotalSize = bufsize;
				}
			}
			if (_SYNCML_TS_DM_VERSION_V12_)
			{
				if (ws.nTNDSFlag)
				{
					tsLib.debugPrint(DEBUG_DM, "REPLACE ws.nTNDSFlag = true");

					if (buf == null)
					{
						buf = "";
					}
					tsdmDB.dmAppendFile(nFileId, (int) bufsize, buf.getBytes());
					if (item.moredata == 0)
					{
						ws.prevBufPos = 0;
						ws.dataBuffered = false;
						ws.dataTotalSize = 0;
						ws.nTNDSFlag = false;
						res = tsOmlib.dmOmlibDeleteImplicit(om, item.target, true);
						if (res >= 0)
						{
							res = ddfParser.dmDDFCreateTNDSNodeFromFile(nFileId, om);
						}

						if (res > 0)
						{
							status = dmBuildcmd.dmBuildCmdStatus(ws, (replace.cmdid), CMD_REPLACE, null, item.target, STATUS_OK);
							tsLinkedList.listAddObjAtLast(ws.statusList, status);
							cur = cur.next;
							continue;
						}
						status = dmBuildcmd.dmBuildCmdStatus(ws, (replace.cmdid), CMD_REPLACE, null, item.target, STATUS_COMMAND_FAILED);
						tsLinkedList.listAddObjAtLast(ws.statusList, status);
						cur = cur.next;
						continue;
					}

					status = dmBuildcmd.dmBuildCmdStatus(ws, (replace.cmdid), CMD_REPLACE, null, item.target, STATUS_ACCEPTED_AND_BUFFERED);
					tsLinkedList.listAddObjAtLast(ws.statusList, status);
					cur = cur.next;
					continue;
				}

				if (type != null)// && format == FORMAT_XML)
				{
					if (type.compareTo(SYNCML_MIME_TYPE_TNDS_XML) == 0)
					{
						if (item.moredata > 0)
						{
							ws.prevBufPos += bufsize;
							ws.dataBuffered = true;

							if (!ws.nTNDSFlag)
							{
								ws.nTNDSFlag = true;
								tsdmDB.dmdbDeleteFile(nFileId);
							}

							if (buf == null)
							{
								buf = "";
							}
							tsdmDB.dmAppendFile(nFileId, (int) bufsize, buf.getBytes());
							// create status for data buffered...
							status = dmBuildcmd.dmBuildCmdStatus(ws, (replace.cmdid), CMD_REPLACE, null, item.target, STATUS_ACCEPTED_AND_BUFFERED);
							tsLinkedList.listAddObjAtLast(ws.statusList, status);
							cur = cur.next;
							continue;
						}
						res = tsOmlib.dmOmlibDeleteImplicit(om, item.target, true);
						if (res >= 0)
						{
							res = ddfParser.dmDDFCreateTNDSNode(buf, bufsize, om);

							if (res > 0)
							{
								status = dmBuildcmd.dmBuildCmdStatus(ws, (replace.cmdid), CMD_REPLACE, null, item.target, STATUS_OK);
								tsLinkedList.listAddObjAtLast(ws.statusList, status);
								cur = cur.next;
								continue;
							}
						}

						status = dmBuildcmd.dmBuildCmdStatus(ws, (replace.cmdid), CMD_REPLACE, null, item.target, STATUS_COMMAND_FAILED);
						tsLinkedList.listAddObjAtLast(ws.statusList, status);
						cur = cur.next; // ADD. 2006.03.22
						tsLib.debugPrintException(DEBUG_EXCEPTION, "Delete Fail.\n");
						return SDM_RET_FAILED;
					}
					else if (type.compareTo(SYNCML_MIME_TYPE_TNDS_WBXML) == 0)
					{
						status = dmBuildcmd.dmBuildCmdStatus(ws, (replace.cmdid), CMD_REPLACE, null, item.target, STATUS_OPTIONAL_FEATURE_NOT_SUPPORTED);
						tsLinkedList.listAddObjAtLast(ws.statusList, status);
						cur = cur.next; // ADD. 2006.03.22
						tsLib.debugPrintException(DEBUG_EXCEPTION, "Not Support TNDS with WBXML Type.\n");
						return SDM_RET_FAILED;
					}
				}
			}
			res = dmAgentVerifyUpdateMechanism(ws, item.target, buf);

			if (res > 0)
			{
				tsLib.debugPrint(DEBUG_DM, item.target);
				tsLib.debugPrint(DEBUG_DM, String.valueOf(bufsize));
				tsLib.debugPrint(DEBUG_DM, String.valueOf(ws.dataTotalSize));
				res = (int) tsOmlib.dmOmWrite(om, item.target, ws.dataTotalSize, ws.prevBufPos, buf, bufsize);
			}
			else
			{
				tsLib.debugPrintException(DEBUG_EXCEPTION, item.target);
				res = SDM_RET_FAILED;
			}

			// Defects
			if (buf != null)
			{
				buf = null;
			}
			else if (node != null)
			{
				node.vaddr = -1; // initialize...
				node.size = 0;
			}

			if (res < 0)
			{
				// writing failed - command failed(500)
				status = dmBuildcmd.dmBuildCmdStatus(ws, (replace.cmdid), CMD_REPLACE, null, item.target, STATUS_COMMAND_FAILED);

				// command in Atomic -> atomic status change to STATUS_ATOMIC_FAILED
				if ((isAtomic) && (atomic_status != null))
				{
					atomic_status.data = STATUS_ATOMIC_FAILED;
				}
			}
			else
			{
				node = tsOmlib.dmOmLibGetNodeProp(om, item.target);
				if (type != null)
				{
					if (node.type != null)
					{
						tsOmlib.dmOmDeleteMimeList(node.type);
					}
					list = new tsOmList();
					list.data = type;
					list.next = null;
					node.type = list;
				}

				if (format > 0)
				{
					node.format = format;
				}

				if (item.moredata == 0)
				{
					ws.prevBufPos = 0;
					ws.dataBuffered = false;
					ws.dataTotalSize = 0;
					int nAgentType = SYNCML_DM_AGENT_DM;

					if (ws.nUpdateMechanism == DM_FUMO_MECHANISM_REPLACE)
					{
						tsdmDB.dmdbSetFUMOStatus(DM_FUMO_STATE_DOWNLOAD_COMPLETE);
					}

					// writing success
					status = dmBuildcmd.dmBuildCmdStatus(ws, (replace.cmdid), CMD_REPLACE, null, item.target, STATUS_OK);
				}
				else
				{
					ws.prevBufPos += bufsize;
					ws.dataBuffered = true;
					int nAgentType = SYNCML_DM_AGENT_DM;

					if (ws.nUpdateMechanism == DM_FUMO_MECHANISM_REPLACE)
					{
						tsdmDB.dmdbSetFUMOStatus(DM_FUMO_STATE_DOWNLOAD_IN_PROGRESS);
					}

					// create status for data buffered...
					status = dmBuildcmd.dmBuildCmdStatus(ws, (replace.cmdid), CMD_REPLACE, null, item.target, STATUS_ACCEPTED_AND_BUFFERED);
				}
			}

			if (status != null)
			{
				tsLinkedList.listAddObjAtLast(ws.statusList, status);
			}
			cur = cur.next;
		}

		return SDM_RET_OK;
	}

	public int dmAgentVerifyUpdateMechanism(tsDmWorkspace ws, String pPath, String pPkgURL)
	{
		if (pPath.contains(DM_OMA_REPLACE))
		{
			ws.nUpdateMechanism = DM_FUMO_MECHANISM_REPLACE;
			tsdmDB.dmdbSetFUMOStatus(DM_FUMO_STATE_IDLE_START);
			tsdmDB.dmdbSetFUMOUpdateMechanism(ws.nUpdateMechanism);
		}

		// MOD : for gernic Alert Type for Update Report
		else if (pPath.contains(DM_OMA_ALTERNATIVE))
		{
			if (tsLib.isEmpty(pPkgURL) || (pPkgURL.length() > DEFAULT_BUFFER_SIZE_4))
			{
				tsLib.debugPrint(DEBUG_DM, "D/L Mechanism  Object URL MISMATCH");
				return 0;
			}

			boolean bret = tsdmDB.dmdbSetFUMOServerUrl(SYNCMLDL, pPkgURL);
			if (!bret)
			{
				tsLib.debugPrint(DEBUG_DM, "wrong URL");
				return 0;
			}

			ws.nUpdateMechanism = DM_FUMO_MECHANISM_ALTERNATIVE;
			ws.aDownloadURI = "";
			ws.aDownloadURI = pPkgURL;
			tsdmDB.dmdbSetFUMOUpdateMechanism(ws.nUpdateMechanism);
		}
		// MOD : for gernic Alert Type for Update Report
		else if (pPath.contains(DM_OMA_ALTERNATIVE_2))
		{
			if (tsLib.isEmpty(pPkgURL) || (pPkgURL.length() > DEFAULT_BUFFER_SIZE_4))
			{
				tsLib.debugPrint(DEBUG_DM, "D/L Mechanism  Object URL MISMATCH");
				return 0;
			}

			boolean bret = tsdmDB.dmdbSetFUMOServerUrl(SYNCMLDL, pPkgURL);
			if (!bret)
			{
				tsLib.debugPrint(DEBUG_DM, "wrong URL");
				return 0;
			}

			ws.nUpdateMechanism = DM_FUMO_MECHANISM_ALTERNATIVE_DOWNLOAD;
			ws.aDownloadURI = "";
			ws.aDownloadURI = pPkgURL;
			tsdmDB.dmdbSetFUMOUpdateMechanism(ws.nUpdateMechanism);
		}
		return 1;
	}

	public int dmAgentCmdCopy(tsDmParserCopy copy, boolean isAtomic, tsdmParserStatus atomic_status)
	{
		tsDmWorkspace ws = dm_ws;
		tsOmTree om = ws.om;
		tsdmParserStatus status = null;
		tsDmParserItem item = null;
		tsList cur = null;
		tsDmVnode node;
		char[] sourcedata = null;
		char[] targetdata = null;
		String type;
		int format;
		int sourceformat;
		boolean process;
		char[] tmpbuf = new char[80];
		int sourcesize = 0;
		int targetsize = 0;
		int bufsize = 0;
		int bufsize1 = 0;
		int res;

		process = dmAgentCmdUicAlert();
		cur = copy.itemlist;
		while (cur != null)
		{
			type = null;
			format = FORMAT_NONE;
			item = (tsDmParserItem) cur.item;

			if (ws.serverAuthState != AUTH_STATE_OK)
			{
				if (item.target != null)
				{
					status = dmBuildcmd.dmBuildCmdStatus(ws, (copy.cmdid), CMD_COPY, null, item.target, ws.statusReturnCode);
				}
				else
				{
					status = dmBuildcmd.dmBuildCmdStatus(ws, copy.cmdid, CMD_COPY, null, null, ws.statusReturnCode);
				}

				tsLinkedList.listAddObjAtLast(ws.statusList, status);
				cur = cur.next;
				continue;
			}
			if (!process)
			{
				// @test
				if (item.moredata > 0)
				{
					ws.dataBuffered = true;
				}

				if (item.target != null)
				{
					status = dmBuildcmd.dmBuildCmdStatus(ws, copy.cmdid, CMD_COPY, null, item.target, STATUS_NOT_EXECUTED);
				}
				else
				{
					status = dmBuildcmd.dmBuildCmdStatus(ws, copy.cmdid, CMD_COPY, null, null, STATUS_NOT_EXECUTED);
				}

				tsLinkedList.listAddObjAtLast(ws.statusList, status);
				cur = cur.next;
				continue;
			}

			// if failed atomic block...
			if (isAtomic && ws.atomicStep != SyncmlAtomicStep.ATOMIC_NONE)
			{
				if (ws.tmpItem != null)
				{
					// if failed item?
					if (ws.tmpItem.equals(item))
					{
						if (item.target != null)
						{
							status = dmBuildcmd.dmBuildCmdStatus(ws, copy.cmdid, CMD_COPY, null, item.target, STATUS_ALREADY_EXISTS);
						}
						else
						{
							status = dmBuildcmd.dmBuildCmdStatus(ws, copy.cmdid, CMD_COPY, null, null, STATUS_ALREADY_EXISTS);
						}

						ws.atomicStep = SyncmlAtomicStep.ATOMIC_STEP_NOT_EXEC;
						ws.tmpItem = null;
					}
					else
					{
						// roll back...
						if (item.target != null)
						{
							status = dmBuildcmd.dmBuildCmdStatus(ws, copy.cmdid, CMD_COPY, null, item.target, STATUS_ATOMIC_ROLL_BACK_OK);
						}
						else
						{
							status = dmBuildcmd.dmBuildCmdStatus(ws, copy.cmdid, CMD_COPY, null, null, STATUS_ATOMIC_ROLL_BACK_OK);
						}
					}
				}
				else
				{
					if (ws.atomicStep == SyncmlAtomicStep.ATOMIC_STEP_NOT_EXEC)
					{
						if (item.target != null)
						{
							status = dmBuildcmd.dmBuildCmdStatus(ws, copy.cmdid, CMD_COPY, null, item.target, STATUS_NOT_EXECUTED);
						}
						else
						{
							status = dmBuildcmd.dmBuildCmdStatus(ws, copy.cmdid, CMD_COPY, null, null, STATUS_NOT_EXECUTED);
						}
					}
					else
					{
						if (item.target != null)
						{
							status = dmBuildcmd.dmBuildCmdStatus(ws, copy.cmdid, CMD_COPY, null, item.target, STATUS_ATOMIC_ROLL_BACK_OK);
						}
						else
						{
							status = dmBuildcmd.dmBuildCmdStatus(ws, copy.cmdid, CMD_COPY, null, null, STATUS_ATOMIC_ROLL_BACK_OK);
						}
					}
				}
				tsLinkedList.listAddObjAtLast(ws.statusList, status);
				cur = cur.next;
				continue;
			}

			if (item.target == null)
			{
				status = dmBuildcmd.dmBuildCmdStatus(ws, copy.cmdid, CMD_COPY, null, null, STATUS_NOT_FOUND);
				tsLinkedList.listAddObjAtLast(ws.statusList, status);
				cur = cur.next;
				continue;
			}

			if (item.target == null)
			{
				status = dmBuildcmd.dmBuildCmdStatus(ws, copy.cmdid, CMD_COPY, null, null, STATUS_NOT_FOUND);
				tsLinkedList.listAddObjAtLast(ws.statusList, status);
				cur = cur.next;
				continue;
			}

			node = tsOmlib.dmOmLibGetNodeProp(om, item.source);

			if (node == null)
			{
				status = dmBuildcmd.dmBuildCmdStatus(ws, copy.cmdid, CMD_COPY, null, item.source, STATUS_NOT_EXECUTED);
				tsLinkedList.listAddObjAtLast(ws.statusList, status);
				cur = cur.next;

				// command in Atomic -> atomic status change to STATUS_ATOMIC_FAILED
				if ((isAtomic) && (atomic_status != null))
				{
					atomic_status.data = STATUS_ATOMIC_FAILED;
				}
				continue;
			}
			else
			{
				bufsize = (int) node.size;
				sourcesize = bufsize;
				sourcedata = new char[sourcesize];
				tsOmVfs.dmOmvfsGetData(om.vfs, node, sourcedata);

				format = node.format;

				if (node.type != null && node.type.data != null)
				{
					type = String.valueOf(node.type.data);
				}
				else
				{
					type = null;
				}

				// sourcetype = type;
				sourceformat = node.format;
			}

			node = tsOmlib.dmOmLibGetNodeProp(om, item.target);
			if (node != null && item.moredata == 0 && !ws.dataBuffered)
			{
				bufsize1 = (int) node.size;
				targetsize = bufsize1;

				if (targetsize < sourcesize)
				{
					targetdata = null;
					targetdata = new char[sourcesize];
					bufsize1 = bufsize;
					targetsize = sourcesize;
				}
				else
				{
					targetdata = null;
					targetdata = new char[targetsize];
				}

				if (node.format == FORMAT_NODE)
				{
					status = dmBuildcmd.dmBuildCmdStatus(ws, copy.cmdid, CMD_COPY, null, item.source, STATUS_NOT_EXECUTED);
					tsLinkedList.listAddObjAtLast(ws.statusList, status);
					cur = cur.next;

					if ((isAtomic) && (atomic_status != null))
					{
						atomic_status.data = STATUS_ATOMIC_FAILED;
					}
					continue;
				}
				else
				{
					for (int i = 0; i < sourcesize; i++)
						targetdata[i] = sourcedata[i];
					String data = new String(targetdata);
					tsOmVfs.dmOmvfsSetData(om.vfs, node, data, bufsize);
				}

				if (item.moredata == 0)
				{
					ws.prevBufPos = 0;
					ws.dataBuffered = false;
					ws.dataTotalSize = 0;

					// create status ok
					status = dmBuildcmd.dmBuildCmdStatus(ws, copy.cmdid, CMD_COPY, null, item.target, STATUS_OK);
				}
				else
				{
					ws.prevBufPos += bufsize1;
					ws.dataBuffered = true;

					// create status for data buffered...
					status = dmBuildcmd.dmBuildCmdStatus(ws, copy.cmdid, CMD_COPY, null, item.target, STATUS_ACCEPTED_AND_BUFFERED);
				}
			}
			else
			{
				tsOmlib.dmOmMakeParentPath(item.target, tmpbuf);
				String spathname = tsLib.libString(tmpbuf);
				// check parent path...
				node = tsOmlib.dmOmLibGetNodeProp(om, spathname);
				if (node == null)
				{
					// @test
					if (item.moredata > 0)
					{
						ws.dataBuffered = true;
					}
					// command failed(500)
					status = dmBuildcmd.dmBuildCmdStatus(ws, copy.cmdid, CMD_COPY, null, item.target, STATUS_COMMAND_FAILED);
					tsLinkedList.listAddObjAtLast(ws.statusList, status);
					cur = cur.next;

					// command in Atomic -> atomic status change to STATUS_ATOMIC_FAILED
					if ((isAtomic) && (atomic_status != null))
					{
						atomic_status.data = STATUS_ATOMIC_FAILED;
					}
					continue;
				}

				if (!tsOmlib.dmOmCheckAcl(om, node, OMACL_ADD))
				{
					// @test
					if (item.moredata > 0)
					{
						ws.dataBuffered = true;
					}
					status = dmBuildcmd.dmBuildCmdStatus(ws, copy.cmdid, CMD_COPY, null, item.target, STATUS_PERMISSION_DENIED);
					tsLinkedList.listAddObjAtLast(ws.statusList, status);
					cur = cur.next;
					continue;
				}

				ws.dataTotalSize = bufsize;
				format = sourceformat;
				type = null;
				res = (int) tsOmlib.dmOmWrite(om, item.target, ws.dataTotalSize, 0, String.valueOf(sourcedata), sourcesize);

				// check data writing status
				if (res < 0)
				{
					// @test
					if (item.moredata > 0)
					{
						ws.dataBuffered = true;
					}

					// command failed(500)
					status = dmBuildcmd.dmBuildCmdStatus(ws, copy.cmdid, CMD_COPY, null, item.target, STATUS_COMMAND_FAILED);
					tsLinkedList.listAddObjAtLast(ws.statusList, status);

					ws.dataBuffered = false;
					cur = cur.next;

					// command in Atomic -> atomic status change to STATUS_ATOMIC_FAILED
					if ((isAtomic) && (atomic_status != null))
					{
						atomic_status.data = STATUS_ATOMIC_FAILED;
					}
					continue;
				}

				node = tsOmlib.dmOmLibGetNodeProp(om, item.target);
				if (type != null) // modificaion
				{
					if (node.type != null)
					{
						tsOmlib.dmOmDeleteMimeList(node.type);
					}
					node.type = new tsOmList();
					node.type.data = type;
					node.type.next = null;
				}
				node.format = format;

				// checking chunked data (= need more data?)
				if (item.moredata == 0)
				{
					ws.prevBufPos = 0;
					ws.dataBuffered = false;
					ws.dataTotalSize = 0;

					// create status ok
					status = dmBuildcmd.dmBuildCmdStatus(ws, copy.cmdid, CMD_COPY, null, item.target, STATUS_OK);
				}
				else
				{
					ws.prevBufPos += bufsize;
					ws.dataBuffered = true;

					// create status for data buffered...
					status = dmBuildcmd.dmBuildCmdStatus(ws, copy.cmdid, CMD_COPY, null, item.target, STATUS_ACCEPTED_AND_BUFFERED);
				}
			}

			tsLinkedList.listAddObjAtLast(ws.statusList, status);
			cur = cur.next;
		}
		return SDM_RET_OK;
	}

	public int dmAgentCmdDelete(tsDmParserDelete delcmd, boolean isAtomic, tsdmParserStatus atomic_status)
	{
		tsDmWorkspace ws = dm_ws;
		tsOmTree om = ws.om;
		tsdmParserStatus status = null;
		tsDmParserItem item = null;
		tsList cur = null;
		tsDmVnode node = null;
		boolean process;
		int res;

		process = dmAgentCmdUicAlert();

		cur = delcmd.itemlist;
		while (cur != null)
		{
			item = (tsDmParserItem) cur.item;
			status = null;
			if (ws.serverAuthState != AUTH_STATE_OK)
			{
				if (item.target != null)
				{
					status = dmBuildcmd.dmBuildCmdStatus(ws, delcmd.cmdid, CMD_DELETE, null, item.target, ws.statusReturnCode);
				}
				else
				{
					status = dmBuildcmd.dmBuildCmdStatus(ws, delcmd.cmdid, CMD_DELETE, null, null, ws.statusReturnCode);
				}
				tsLinkedList.listAddObjAtLast(ws.statusList, status);
				cur = cur.next;
				continue;
			}

			if (item.target != null)
			{
				node = tsOmlib.dmOmLibGetNodeProp(om, item.target);
			}

			if (!process)
			{
				if (item.target != null)
				{
					status = dmBuildcmd.dmBuildCmdStatus(ws, delcmd.cmdid, CMD_DELETE, null, item.target, STATUS_NOT_EXECUTED);
				}
				else
				{
					status = dmBuildcmd.dmBuildCmdStatus(ws, delcmd.cmdid, CMD_DELETE, null, null, STATUS_NOT_EXECUTED);
				}

				if ((isAtomic) && (atomic_status != null))
				{
					atomic_status.data = STATUS_ATOMIC_FAILED;
				}
			}
			// if failed atomic block...
			else if (isAtomic && ws.atomicStep != SyncmlAtomicStep.ATOMIC_NONE)
			{
				if (ws.tmpItem != null)
				{
					// if failed item?
					if (ws.tmpItem.equals(item))
					{
						if (item.target != null)
						{
							status = dmBuildcmd.dmBuildCmdStatus(ws, delcmd.cmdid, CMD_DELETE, null, item.target, STATUS_NOT_FOUND);
						}
						else
						{
							status = dmBuildcmd.dmBuildCmdStatus(ws, delcmd.cmdid, CMD_DELETE, null, null, STATUS_NOT_FOUND);
						}
						ws.atomicStep = SyncmlAtomicStep.ATOMIC_STEP_NOT_EXEC;
						ws.tmpItem = null;
					}
					else
					{
						if (item.target != null)
						{
							status = dmBuildcmd.dmBuildCmdStatus(ws, delcmd.cmdid, CMD_DELETE, null, item.target, STATUS_ATOMIC_ROLL_BACK_OK);
						}
						else
						{
							status = dmBuildcmd.dmBuildCmdStatus(ws, delcmd.cmdid, CMD_DELETE, null, null, STATUS_ATOMIC_ROLL_BACK_OK);
						}
					}
				}
				else
				{
					if (ws.atomicStep == SyncmlAtomicStep.ATOMIC_STEP_NOT_EXEC)
					{
						if (item.target != null)
						{
							status = dmBuildcmd.dmBuildCmdStatus(ws, delcmd.cmdid, CMD_DELETE, null, item.target, STATUS_NOT_EXECUTED);
						}
						else
						{
							status = dmBuildcmd.dmBuildCmdStatus(ws, delcmd.cmdid, CMD_DELETE, null, null, STATUS_NOT_EXECUTED);
						}
					}
					else
					{
						if (item.target != null)
						{
							status = dmBuildcmd.dmBuildCmdStatus(ws, delcmd.cmdid, CMD_DELETE, null, item.target, STATUS_ATOMIC_ROLL_BACK_OK);
						}
						else
						{
							status = dmBuildcmd.dmBuildCmdStatus(ws, delcmd.cmdid, CMD_DELETE, null, null, STATUS_ATOMIC_ROLL_BACK_OK);
						}
					}
				}
			}
			else if (node == null)
			{
				if (item.target != null)
				{
					status = dmBuildcmd.dmBuildCmdStatus(ws, delcmd.cmdid, CMD_DELETE, null, item.target, STATUS_NOT_FOUND);
				}
				else
				{
					status = dmBuildcmd.dmBuildCmdStatus(ws, delcmd.cmdid, CMD_DELETE, null, null, STATUS_NOT_FOUND);
				}

				if ((isAtomic) && (atomic_status != null))
				{
					atomic_status.data = STATUS_ATOMIC_FAILED;
				}

			}
			else if (dmAgentIsPermanentNode(om, item.target))
			{
				status = dmBuildcmd.dmBuildCmdStatus(ws, delcmd.cmdid, CMD_DELETE, null, item.target, STATUS_COMMAND_NOT_ALLOWED);

				if ((isAtomic) && (atomic_status != null))
				{
					atomic_status.data = STATUS_ATOMIC_FAILED;
				}
			}
			else if (!tsOmlib.dmOmCheckAcl(om, node, OMACL_DELETE))
			{
				status = dmBuildcmd.dmBuildCmdStatus(ws, delcmd.cmdid, CMD_DELETE, null, item.target, STATUS_PERMISSION_DENIED);

				if ((isAtomic) && (atomic_status != null))
				{
					atomic_status.data = STATUS_ATOMIC_FAILED;
				}
			}
			else if (node == om.vfs.root) // need check
			{
				status = dmBuildcmd.dmBuildCmdStatus(ws, delcmd.cmdid, CMD_DELETE, null, item.target, STATUS_COMMAND_NOT_ALLOWED);

				if ((isAtomic) && (atomic_status != null))
				{
					atomic_status.data = STATUS_ATOMIC_FAILED;
				}
			}
			else
			{
				tsLib.debugPrint(DEBUG_DM, item.target);
				res = tsOmlib.dmOmlibDelete(om, item.target, true);
				if (res < 0)
				{
					status = dmBuildcmd.dmBuildCmdStatus(ws, delcmd.cmdid, CMD_DELETE, null, item.target, STATUS_COMMAND_NOT_ALLOWED);
					if ((isAtomic) && (atomic_status != null))
					{
						atomic_status.data = STATUS_ATOMIC_FAILED;
					}
				}
				else
				{
					status = dmBuildcmd.dmBuildCmdStatus(ws, delcmd.cmdid, CMD_DELETE, null, item.target, STATUS_OK);
				}
			}

			if (status != null)
			{
				tsLinkedList.listAddObjAtLast(ws.statusList, status);
			}

			cur = cur.next;
		}
		return SDM_RET_OK;
	}

	public int dmAgentCmdAtomic(tsDmParserAtomic atomic)
	{
		tsDmWorkspace ws = dm_ws;
		int res;

		if (atomic.itemlist != null)
		{
			res = dmAgentCmdAtomicBlock(atomic, atomic.itemlist);
		}
		else
			return SDM_RET_FAILED;
		if (res < 0)
		{
			return SDM_RET_FAILED;
		}
		ws.numAction += res;
		return SDM_RET_OK;
	}

	public int dmAgentCmdSequence(tsDmParserSequence sequence)
	{
		tsDmWorkspace ws = dm_ws;
		tsdmParserStatus status;
		int res = SDM_RET_OK;

		if (!ws.IsSequenceProcessing)
		{
			status = dmBuildcmd.dmBuildCmdStatus(ws, sequence.cmdid, "Sequence", null, null, STATUS_OK);
			tsLinkedList.listAddObjAtLast(ws.statusList, status);
		}
		if (sequence.itemlist != null)
		{
			res = dmAgentCmdSequenceBlock(sequence, sequence.itemlist);
		}
		else
		{
			return SDM_RET_FAILED;
		}

		if (res == SDM_PAUSED_BECAUSE_UIC_COMMAND)
		{
			return SDM_PAUSED_BECAUSE_UIC_COMMAND;
		}

		if (res < 0)
		{
			return SDM_RET_FAILED;
		}

		// ADD : For sequence lists
		return res;
	}

	public static void dmAgentSetXNodePath(String pPath, String target, boolean bTndsFlag)
	{
		String tmpBuf;
		tsLib.debugPrint(DEBUG_DM, "sdmProcessCmdAdd : target[" + target + "]" + "parent[" + pPath + "]\n");

		if (dmAgentGetSyncMode() != DM_SYNC_BOOTSTARP)
		{
			dm_AccXNodeTndsInfo = new tsDmAccXNode();

			if (pPath.compareTo(BASE_ACCOUNT_PATH) == 0 || pPath.compareTo(ATT_BASE_ACCOUNT_PATH) == 0)
			{
				dm_AccXNodeTndsInfo.Account = pPath;
				dm_AccXNodeTndsInfo.Account = dm_AccXNodeTndsInfo.Account.concat("/");
				dm_AccXNodeTndsInfo.Account = dm_AccXNodeTndsInfo.Account.concat(target);

			}

			tmpBuf = dm_AccXNodeTndsInfo.Account;
			tmpBuf = tmpBuf.concat(SYNCML_DMACC_TOCONREF_PATH);

			if (pPath.compareTo(tmpBuf) == 0)
			{
				dm_AccXNodeTndsInfo.ToConRef = pPath;
				dm_AccXNodeTndsInfo.ToConRef = dm_AccXNodeTndsInfo.ToConRef.concat("/");
				dm_AccXNodeTndsInfo.ToConRef = dm_AccXNodeTndsInfo.ToConRef.concat(target);
			}

			tmpBuf = dm_AccXNodeTndsInfo.Account;
			tmpBuf = tmpBuf.concat(SYNCML_DMACC_APPADDR_PATH);
			if (pPath.compareTo(tmpBuf) == 0)
			{
				dm_AccXNodeTndsInfo.AppAddr = pPath;
				dm_AccXNodeTndsInfo.AppAddr = dm_AccXNodeTndsInfo.AppAddr.concat("/");
				dm_AccXNodeTndsInfo.AppAddr = dm_AccXNodeTndsInfo.AppAddr.concat(target);
			}

			tmpBuf = dm_AccXNodeTndsInfo.AppAddr;
			tmpBuf = tmpBuf.concat(SYNCML_APPADDR_PORT_PATH);
			if (pPath.compareTo(tmpBuf) == 0)
			{
				dm_AccXNodeTndsInfo.AppAddrPort = pPath;
				dm_AccXNodeTndsInfo.AppAddrPort = dm_AccXNodeTndsInfo.AppAddrPort.concat("/");
				dm_AccXNodeTndsInfo.AppAddrPort = dm_AccXNodeTndsInfo.AppAddrPort.concat(target);
			}

			tmpBuf = dm_AccXNodeTndsInfo.Account;
			tmpBuf = tmpBuf.concat(SYNCML_DMACC_APPAUTH_PATH);

			if (pPath.compareTo(tmpBuf) == 0)
			{
				// if(dm_AccXNodeTndsInfo.ClientAppAuth.length() != 0)
				if (target.compareTo("ClientSide") == 0)
				{
					dm_AccXNodeTndsInfo.ClientAppAuth = pPath;
					dm_AccXNodeTndsInfo.ClientAppAuth = dm_AccXNodeTndsInfo.ClientAppAuth.concat("/");
					dm_AccXNodeTndsInfo.ClientAppAuth = dm_AccXNodeTndsInfo.ClientAppAuth.concat(target);
				}
				else
				{
					dm_AccXNodeTndsInfo.ServerAppAuth = pPath;
					dm_AccXNodeTndsInfo.ServerAppAuth = dm_AccXNodeTndsInfo.ServerAppAuth.concat("/");
					dm_AccXNodeTndsInfo.ServerAppAuth = dm_AccXNodeTndsInfo.ServerAppAuth.concat(target);
				}
			}
		}
		else
		{
			if (bTndsFlag)
			{
				if (pPath.compareTo(BASE_ACCOUNT_PATH) == 0 || pPath.compareTo(ATT_BASE_ACCOUNT_PATH) == 0)
				{
					dm_AccXNodeInfo.Account = pPath;
					dm_AccXNodeInfo.Account = dm_AccXNodeInfo.Account.concat("/");
					dm_AccXNodeInfo.Account = dm_AccXNodeInfo.Account.concat(target);
				}

				tmpBuf = dm_AccXNodeInfo.Account;
				tmpBuf = tmpBuf.concat(SYNCML_DMACC_TOCONREF_PATH);
				if (pPath.compareTo(tmpBuf) == 0)
				{
					dm_AccXNodeInfo.ToConRef = pPath;
					dm_AccXNodeInfo.ToConRef = dm_AccXNodeInfo.ToConRef.concat("/");
					dm_AccXNodeInfo.ToConRef = dm_AccXNodeInfo.ToConRef.concat(target);
				}

				tmpBuf = dm_AccXNodeInfo.Account;
				tmpBuf = tmpBuf.concat(SYNCML_DMACC_APPADDR_PATH);
				if (pPath.compareTo(tmpBuf) == 0)
				{
					dm_AccXNodeInfo.AppAddr = pPath;
					dm_AccXNodeInfo.AppAddr = dm_AccXNodeInfo.AppAddr.concat("/");
					dm_AccXNodeInfo.AppAddr = dm_AccXNodeInfo.AppAddr.concat(target);
				}

				tmpBuf = dm_AccXNodeInfo.AppAddr;
				tmpBuf = tmpBuf.concat(SYNCML_APPADDR_PORT_PATH);
				if (pPath.compareTo(tmpBuf) == 0)
				{
					dm_AccXNodeInfo.AppAddrPort = pPath;
					dm_AccXNodeInfo.AppAddrPort = dm_AccXNodeInfo.AppAddrPort.concat("/");
					dm_AccXNodeInfo.AppAddrPort = dm_AccXNodeInfo.AppAddrPort.concat(target);
				}

				tmpBuf = dm_AccXNodeInfo.Account;
				tmpBuf = tmpBuf.concat(SYNCML_DMACC_APPAUTH_PATH);
				if (pPath.compareTo(tmpBuf) == 0)
				{
					if (target.compareTo("ClientSide") == 0)
					{
						dm_AccXNodeInfo.ClientAppAuth = pPath;
						dm_AccXNodeInfo.ClientAppAuth = dm_AccXNodeInfo.ClientAppAuth.concat("/");
						dm_AccXNodeInfo.ClientAppAuth = dm_AccXNodeInfo.ClientAppAuth.concat(target);
					}
					else
					{
						dm_AccXNodeInfo.ServerAppAuth = pPath;
						dm_AccXNodeInfo.ServerAppAuth = dm_AccXNodeInfo.ServerAppAuth.concat("/");
						dm_AccXNodeInfo.ServerAppAuth = dm_AccXNodeInfo.ServerAppAuth.concat(target);
					}
				}
			}
			else
			{
				if (pPath.compareTo(BASE_ACCOUNT_PATH) == 0 || pPath.compareTo(ATT_BASE_ACCOUNT_PATH) == 0)
				{
					dm_AccXNodeInfo.Account = target;
				}

				tmpBuf = dm_AccXNodeInfo.Account;
				tmpBuf = tmpBuf.concat(SYNCML_DMACC_TOCONREF_PATH);
				if (pPath.compareTo(tmpBuf) == 0)
				{
					dm_AccXNodeInfo.ToConRef = target;
				}

				tmpBuf = dm_AccXNodeInfo.Account;
				tmpBuf = tmpBuf.concat(SYNCML_DMACC_APPADDR_PATH);
				if (pPath.compareTo(tmpBuf) == 0)
				{
					dm_AccXNodeInfo.AppAddr = target;
				}

				tmpBuf = dm_AccXNodeInfo.AppAddr;
				tmpBuf = tmpBuf.concat(SYNCML_APPADDR_PORT_PATH);
				if (pPath.compareTo(tmpBuf) == 0)
				{
					dm_AccXNodeInfo.AppAddrPort = target;
				}

				tmpBuf = dm_AccXNodeInfo.Account;
				tmpBuf = tmpBuf.concat(SYNCML_DMACC_APPAUTH_PATH);
				if (pPath.compareTo(tmpBuf) == 0)
				{
					// if(dm_AccXNodeInfo.ClientAppAuth.length() != 0)
					if (target.compareTo("ClientSide") == 0)
					{
						dm_AccXNodeInfo.ClientAppAuth = target;
					}
					else
					{
						dm_AccXNodeInfo.ServerAppAuth = target;
					}
				}
			}
		}
	}

	public int dmAgentSetAclDynamicFUMONode(tsOmTree ptOm, String FumoNodePath) // dynamic_node_patch
	{
		if (_SYNCML_TS_DM_VERSION_V12_)
		{
			int aclValue = 0;
			tsLib.debugPrint(DEBUG_DM, "target path[" + FumoNodePath + "]");

			if (FumoNodePath.contains(FUMO_PKGNAME_PATH))
			{
				aclValue = OMACL_GET | OMACL_REPLACE;
				dmAgentMakeDefaultAcl(ptOm, FumoNodePath, aclValue, SCOPE_DYNAMIC);
			}
			else if (FumoNodePath.contains(FUMO_PKGVERSION_PATH))
			{
				aclValue = OMACL_GET | OMACL_REPLACE;
				dmAgentMakeDefaultAcl(ptOm, FumoNodePath, aclValue, SCOPE_DYNAMIC);
			}
			else if (FumoNodePath.contains(FUMO_PKGURL_PATH))
			{
				aclValue = OMACL_GET | OMACL_REPLACE | OMACL_ADD;
				dmAgentMakeDefaultAcl(ptOm, FumoNodePath, aclValue, SCOPE_DYNAMIC);
			}
			else if (FumoNodePath.contains(FUMO_DOWNLOADANDUPDATE_PATH))
			{
				aclValue = OMACL_GET | OMACL_EXEC | OMACL_ADD;
				dmAgentMakeDefaultAcl(ptOm, FumoNodePath, aclValue, SCOPE_DYNAMIC);
			}
			else if (FumoNodePath.contains(FUMO_UPDATE_PATH))
			{
				aclValue = OMACL_GET | OMACL_EXEC;
				dmAgentMakeDefaultAcl(ptOm, FumoNodePath, aclValue, SCOPE_DYNAMIC);
			}
			else if (FumoNodePath.contains(FUMO_PKGDATA_PATH))
			{
				aclValue = OMACL_REPLACE;
				dmAgentMakeDefaultAcl(ptOm, FumoNodePath, aclValue, SCOPE_DYNAMIC);
			}
			else if (FumoNodePath.contains(FUMO_DOWNLOAD_PATH))
			{
				aclValue = OMACL_ADD | OMACL_GET | OMACL_EXEC;
				dmAgentMakeDefaultAcl(ptOm, FumoNodePath, aclValue, SCOPE_DYNAMIC);
			}
			else if (FumoNodePath.contains(FUMO_STATE_PATH))
			{
				aclValue = OMACL_GET;
				dmAgentMakeDefaultAcl(ptOm, FumoNodePath, aclValue, SCOPE_DYNAMIC);
			}
			else if (FumoNodePath.contains(FUMO_EXT))
			{
				aclValue = OMACL_GET;
				dmAgentMakeDefaultAcl(ptOm, FumoNodePath, aclValue, SCOPE_DYNAMIC);
			}
		}
		return 0;
	}

	public void tpClose(int appId)
	{
		gHttpDMAdapter.tpClose(appId);
	}

	public void tpInit(int appId)
	{
		gHttpDMAdapter.tpInit(appId);
	}

	public int tpAbort(int appId)
	{
		return gHttpDMAdapter.tpAbort(appId);
	}

	public void tpCloseNetWork(int appId)
	{
		gHttpDMAdapter.tpCloseNetWork(appId);
	}

	public int tpGetHttpEcode()
	{
		return gHttpDMAdapter.tpGetHttpEcode(SYNCMLDM);
	}

	public boolean tpCheckRetry()
	{
		tsLib.debugPrint(DEBUG_DM, "ConntectRetryCount " + ConnectRetryCount);
		if (ConnectRetryCount >= TP_RETRY_COUNT_MAX)
		{
			netTimerConnect.endTimer();
			netTimerReceive.endTimer();
			netTimerSend.endTimer();
			ConnectRetryCount = 0;
			return false;
		}
		ConnectRetryCount++;
		return true;
	}

	public static void tpSetRetryCount(int nCnt)
	{
		ConnectRetryCount = nCnt;
	}

	public static boolean dmAgentGetPendingStatus()
	{
		return nPendingStatus;
	}
}
