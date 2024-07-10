package com.tsdm.agent;

import java.net.SocketTimeoutException;

import com.tsdm.db.tsDB;
import com.tsdm.db.tsDBFumoInfo;
import com.tsdm.db.tsDBURLParser;
import com.tsdm.db.tsdmDB;
import com.tsdm.adapt.tsDefIne;
import com.tsdm.adapt.tsDefineIdle;
import com.tsdm.adapt.tsLib;
import com.tsdm.adapt.tsDmMsg;
import com.tsdm.parser.ddXMLDataSet;
import com.tsdm.parser.ddXMLParser;
import com.tsdm.net.netHttpAdapter;
import com.tsdm.net.netDefine;
import com.tsdm.net.netTimerConnect;
import com.tsdm.net.netTimerReceive;
import com.tsdm.net.netTimerSend;
import com.tsdm.tsService;

public class dlAgent implements dmDefineDevInfo, dmDefineMsg, tsDefineIdle, tsDefIne, netDefine
{
	public static final int			HTTP_HEADER_MAX_SIZE		= 768;
	public static final int 		DM_DL_MAX_DOWNLOAD_SIZE = HTTP_HEADER_MAX_SIZE + WBXML_DM_MAX_MESSAGE_SIZE;

	public static String[]			pReportStatus;
	public static boolean			nFFSWriteStatus;
	public static int				nUserInitStatus;
	public static byte[]			gReceiveBuffer;

	public dlAgentHandler dlagenthandler				= null;
	public static netHttpAdapter gHttpDLAdapter;

	private static int				DLConnectRetryCount			= 0;
	
	private static int              DLConnectRetryFailCount    = 0;

	public dlAgent()
	{
		nFFSWriteStatus = false;
		nUserInitStatus = DM_NONE_INIT;
		pReportStatus = new String[11];

		if (gHttpDLAdapter == null)
		{
			gHttpDLAdapter = new netHttpAdapter();
		}

		pReportStatus[OMA_DL_STAUS_SUCCESS] = "900 Success";
		pReportStatus[OMA_DL_STATUS_MEMORY_ERROR] = "901 Insufficient memory";
		pReportStatus[OMA_DL_STATUS_USER_CANCEL] = "902 User Cancelled";
		pReportStatus[OMA_DL_STATUS_LOSS_SERVICE] = "903 Loss of Service";
		pReportStatus[OMA_DL_STATUS_ATTRIBUTE_MISMATCH] = "905 Attribute mismatch";
		pReportStatus[OMA_DL_STATUS_INVALID_DESCRIPTOR] = "906 Invalid descriptor";
		pReportStatus[OMA_DL_STATUS_INVALID_DDVERSIONV] = "951 Invalid DDVersion";
		pReportStatus[OMA_DL_STATUS_DEVICE_ABORTED] = "952 Device Aborted";
		pReportStatus[OMA_DL_STATUS_NON_ACCEPTABLE_CONTENT] = "953 Non-Acceptable Content";
		pReportStatus[OMA_DL_STATUS_LOADER_ERROR] = "954 Loader Error";
		pReportStatus[OMA_DL_STATUS_NONE] = "";
	}

	public static boolean dlAgentGetWriteStatus()
	{
		return nFFSWriteStatus;
	}

	public static boolean dlAgentSetWriteStatus(boolean nStatus)
	{
		nFFSWriteStatus = nStatus;
		return true;
	}

	public static void dlAgentInitBuffer()
	{
		gReceiveBuffer = new byte[5 * 1024 * 1024 +1];
	}

	public static byte[] dlAgentGetBuffer()
	{
		return gReceiveBuffer;
	}

	public static void dlAgentSetClientInitFlag(int SetValue)
	{
		tsLib.debugPrint(DEBUG_DL, "Client Init Set:" + SetValue);
		nUserInitStatus = SetValue;

		if(SetValue == DM_NONE_INIT && tsdmDB.dmdbGetFUMOStatus()==DM_FUMO_STATE_NONE)
			tsService.logUpload();
	}

	public static int dlAgentGetClientInitFlag()
	{
		return nUserInitStatus;
	}
	
	public static String dlAgentGetReportStatus(int nStatus)
	{
		if (nStatus >= OMA_DL_STAUS_SUCCESS && nStatus < OMA_DL_STATUS_NONE)
		{
			tsLib.debugPrint(DEBUG_DL, "pReportStatusValue = [" + pReportStatus[nStatus] + "]");
		}
		return pReportStatus[nStatus];
	}

	public static boolean dlAgentIsStatus()
	{
		int nAgentStatus;
		nAgentStatus = tsdmDB.dmdbGetFUMOStatus();

		tsLib.debugPrint(DEBUG_DL, "nAgentStatus = [" + nAgentStatus + "]");

		switch (nAgentStatus)
		{
			case DM_FUMO_STATE_NONE:
			case DM_FUMO_STATE_UPDATE_SUCCESSFUL_NODATA:
			case DM_FUMO_STATE_UPDATE_SUCCESSFUL_HAVEDATA:
			case DM_FUMO_STATE_UPDATE_FAILED_NODATA:
			case DM_FUMO_STATE_UPDATE_FAILED_HAVEDATA:
			case DM_FUMO_STATE_UPDATE_IN_PROGRESS:
			case DM_FUMO_STATE_DOWNLOAD_FAILED_REPORTING:
			case DM_FUMO_STATE_USER_CANCEL_REPORTING:
				return true;

			case DM_FUMO_STATE_DOWNLOAD_IN_CANCEL:
			case DM_FUMO_STATE_DOWNLOAD_FAILED:
			case DM_FUMO_STATE_DOWNLOAD_DESCRIPTOR:
			case DM_FUMO_STATE_DOWNLOAD_IN_PROGRESS:
			case DM_FUMO_STATE_DOWNLOAD_COMPLETE:
			case DM_FUMO_STATE_IDLE_START:
				break;

			default:
				break;
		}
		return false;
	}

	public static int dlAgentGetHttpConStatus(boolean nDownloadMode) // byte[] pContentRange, boolean nDownloadMode)
	{
		int nOffset;
		int nTotalObjectSize;
		int nFileId = 0;
		int nAgentType = SYNCML_DM_AGENT_DM;

		tsLib.debugPrint(DEBUG_DL, "");

		nFileId = tsdmDB.dmdbGetFileIdFirmwareData();
		nOffset = tsdmDB.dmdbGetFileSize(nFileId);
		nTotalObjectSize = tsdmDB.dmdbGetObjectSizeFUMO();

		if (nOffset == nTotalObjectSize)
		{
			tsLib.debugPrint(DEBUG_DL, "offset = " + nOffset + "  TotalSize = " + nTotalObjectSize);
			return SDL_RET_OK;
		}
		else if (nOffset > nTotalObjectSize)
		{
			tsdmDB.dmdbDeleteFile(nFileId);

			tsLib.debugPrintException(DEBUG_EXCEPTION, "offset =" + nOffset + "  TotalSize = " + nTotalObjectSize);
			return SDL_RET_FAILED;
		}

		return SDL_RET_CONTINUE;
	}

	public static String dlAgentGetHttpContentRange(boolean nDownloadMode)
	{
		int nOffset = 0;
		int nDownloadSize = 0;
		int nTotalObjectSize = 0;
		int nFileId = 0;
		String strConLength = "";
		int nAgentType = SYNCML_DM_AGENT_DM;

		if(nFFSWriteStatus)
		{
			nFFSWriteStatus = false;
		}

		nFileId = tsdmDB.dmdbGetFileIdFirmwareData();
		nOffset = tsdmDB.dmdbGetFileSize(nFileId);
		nTotalObjectSize = tsdmDB.dmdbGetObjectSizeFUMO();

		tsLib.debugPrint(DEBUG_DL, "nOffset = " + nOffset + " nTotalObjectSize = " + nTotalObjectSize);

		if (!nDownloadMode)
		{
			if (DM_DL_MAX_DOWNLOAD_SIZE < nTotalObjectSize - nOffset)
				nDownloadSize = DM_DL_MAX_DOWNLOAD_SIZE + nOffset - 1;
			else
				nDownloadSize = nTotalObjectSize - 1;

			strConLength = String.valueOf(nOffset);
			tsLib.debugPrint(DEBUG_DL, "offset = " + nOffset + " , downloadsize = " + nDownloadSize + ", TotalSize = " + nTotalObjectSize);
		}
		else
		{
			strConLength = String.valueOf(nOffset);
			tsLib.debugPrint(DEBUG_DL, "offset = " + nOffset + ", TotalSize = " + nTotalObjectSize);
		}

		return strConLength;
	}

	public static int dlAgentParserDescriptor(byte[] pData, int size)
	{
		Object objectInfo;
		ddXMLDataSet objectDD = null;
		int nRet = SDL_RET_FAILED;

		// for FUMO
		objectInfo = new tsDBFumoInfo();
		objectInfo = tsdmDB.dmdbGetObjectFUMO();
		ddXMLParser DDParser = new ddXMLParser();
		try
		{
			objectDD = DDParser.dlParserDownloadDescriptor(pData);
		}
		catch (Exception ex)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, ex.toString());
		}

		if (objectDD != null)
		{
			objectInfo = dlAgentSetFUMOObjectFromDD(objectInfo, objectDD);
			if (objectInfo != null)
			{
				tsdmDB.dmdbSetObjectFUMO(objectInfo);
				nRet = SDL_RET_OK;
			}
		}

		return nRet;
	}

	public static Object dlAgentSetFUMOObjectFromDD(Object objectInfo, ddXMLDataSet objectDD)
	{
		tsDBFumoInfo fumoInfo = (tsDBFumoInfo) objectInfo;
		ddXMLDataSet ddInfo = (ddXMLDataSet) objectDD;
		tsDBURLParser parser = new tsDBURLParser();

		String aInstallSize;
		String aTempURL;
		String aType;
		String aTempServerAddr;
		String aTempPort;
		char[] sztemp = new char[256];
		int nLen = 0;
		int nPort = 80;

		nLen = ddInfo.type.length();
		aType = ddInfo.type;
		tsLib.debugPrint(DEBUG_DL, "Mime Media Type = [" + String.valueOf(nLen) + aType + "]");

		fumoInfo.szContentType = aType;
		fumoInfo.szAcceptType = aType;
		tsLib.debugPrint(DEBUG_DL, "szAcceptType = " + String.valueOf(fumoInfo.szAcceptType));

		nLen = ddInfo.objectURI.length();

		String szURL = ddInfo.objectURI;
		aTempURL = tsDB.dbCheckOMADDURL(szURL);
		tsLib.debugPrint(DEBUG_DL, "Install Len = [" + String.valueOf(nLen) + "] [" + aTempURL + "]");
		
		parser = tsDB.dbURLParser(aTempURL);

		aTempServerAddr = parser.pURL;
		nPort = parser.nPort;
		aTempPort = String.valueOf(nPort);

		sztemp = tsDB.dbDoDMBootStrapURI(aTempServerAddr.toCharArray(), aTempURL.toCharArray(), aTempPort.toCharArray());
		if (sztemp == null)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, "Install URI Parsing Error");
			return null;
		}

		parser = null;
		parser = tsDB.dbURLParser(String.valueOf(sztemp));
		if (parser == null)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, " Parsing Error");
			return fumoInfo;
		}

		fumoInfo.ObjectDownloadUrl = parser.pURL;
		fumoInfo.ObjectDownloadIP = parser.pAddress;

		tsLib.debugPrint(DEBUG_DL, "Install URI = " + fumoInfo.ObjectDownloadUrl);

		if (nPort == 0 || nPort > 65535)
		{
			nPort = fumoInfo.ServerPort;
		}

		fumoInfo.nObjectDownloadPort = nPort;

		aTempURL = null;
		nPort = 0;
		parser = null;

		nLen = ddInfo.installNotifyURI.length();
		szURL = ddInfo.installNotifyURI;
		aTempURL = tsDB.dbCheckOMADDURL(szURL);

		tsLib.debugPrint(DEBUG_DL, "Notify URI Len = [" + nLen + "][" + aTempURL + "]");
		parser = tsDB.dbURLParser(aTempURL);

		aTempServerAddr = parser.pURL;
		aTempPort = String.valueOf(parser.nPort);

		sztemp = tsDB.dbDoDMBootStrapURI(aTempServerAddr.toCharArray(), aTempURL.toCharArray(), aTempPort.toCharArray());
		if (sztemp == null)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, "Notify URI Parsing Error");
			return null;
		}
		
		parser = null;
		parser = tsDB.dbURLParser(aTempServerAddr);
		if (parser == null)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, " Parsing Error");
			return fumoInfo;
		}

		fumoInfo.StatusNotifyUrl = parser.pURL;
		fumoInfo.StatusNotifyIP = parser.pAddress;
		fumoInfo.StatusNotifyProtocol = parser.pProtocol;
		nPort = parser.nPort;

		tsLib.debugPrint(DEBUG_DL, "Notify URI = " + fumoInfo.StatusNotifyUrl);
		if (nPort == 0 || nPort > 65535)
		{
			nPort = fumoInfo.ServerPort;
		}
		fumoInfo.nStatusNotifyPort = nPort;
		aInstallSize = ddInfo.size;
		tsLib.debugPrint(DEBUG_DL, "Object Size Len = [" + aInstallSize + "]");
		fumoInfo.nObjectSize = Integer.parseInt(aInstallSize);
		fumoInfo.nFFSWriteSize = 0;
		
		if(!tsLib.isEmpty(ddInfo.description))
		{
			fumoInfo.szDescription = ddInfo.description;
		}

		return fumoInfo;
	}

	public static int dlAgentUserCancel()
	{
		int nRc = SDL_RET_OK;
		String szResURL = "";
		String pDownloadStatus = "";
		int nAgentType = SYNCML_DM_AGENT_DM;

		tsLib.debugPrint(DEBUG_DL, "");

		szResURL = tsdmDB.dmdbGetStatusAddrFUMO(szResURL);

		try
		{
			gHttpDLAdapter.tpSetHttpObj(szResURL, null, null, HTTP_METHOD_POST, SYNCMLDL, false);
		}
		catch (NullPointerException e)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
			netTimerSend.endTimer();
			nRc = TP_RET_SEND_FAIL;
			tsDmMsg.taskSendMessage(TASK_MSG_DL_SYNCML_SENDFAIL, null, null);
			return nRc;
		}
		pDownloadStatus = dlAgentGetReportStatus(OMA_DL_STATUS_USER_CANCEL);
		try
		{
			nRc = gHttpDLAdapter.tpSendData(pDownloadStatus.getBytes(), pDownloadStatus.length(), SYNCMLDL);
		}
		catch (SocketTimeoutException e)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
			netTimerSend.endTimer();
			nRc = TP_RET_SEND_FAIL;
		}

		if (nRc == TP_RET_OK)
		{
			tsDmMsg.taskSendMessage(TASK_MSG_DL_SYNCML_CONTINUE, null, null);
		}
		else if (nRc == TP_RET_CONNECTION_FAIL)
		{
			tsDmMsg.taskSendMessage(TASK_MSG_DL_SYNCML_CONNECTFAIL, null, null);
		}
		else
		{
			tsDmMsg.taskSendMessage(TASK_MSG_DL_SYNCML_SENDFAIL, null, null);
		}

		return nRc;
	}

	public static int dlAgentDownloadFailed()
	{
		int nRc = SDL_RET_OK;
		String szResURL = "";
		String pDownloadStatus = "";
		String pszResultCode = "";
		int nAgentType = SYNCML_DM_AGENT_DM;

		tsLib.debugPrint(DEBUG_DL, "");

		szResURL = tsdmDB.dmdbGetStatusAddrFUMO(szResURL);

		try
		{
			gHttpDLAdapter.tpSetHttpObj(szResURL, null, null, HTTP_METHOD_POST, SYNCMLDL, false);
		}
		catch (NullPointerException e)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
			netTimerSend.endTimer();
			nRc = TP_RET_SEND_FAIL;
			tsDmMsg.taskSendMessage(TASK_MSG_DL_SYNCML_SENDFAIL, null, null);
			return nRc;
		}

		pszResultCode = tsdmDB.dmdbGetFUMOResultCode();

		if (pszResultCode != null)
		{
			if (pszResultCode.compareTo(DL_GENERIC_DOWNLOAD_FAILED_OUT_MEMORY) == 0)
			{
				/* Out of Memory */
				pDownloadStatus = dlAgentGetReportStatus(OMA_DL_STATUS_MEMORY_ERROR);
			}
			else if (pszResultCode.compareTo(DL_GENERIC_SERVER_ERROR) == 0)
			{
				/* Http Status Error */
				pDownloadStatus = dlAgentGetReportStatus(OMA_DL_STATUS_LOSS_SERVICE);
			}
			else if (pszResultCode.compareTo(DL_GENERIC_SERVER_UNAVAILABLE) == 0)
			{
				/* Connect, Send, Receive Fail */
				pDownloadStatus = dlAgentGetReportStatus(OMA_DL_STATUS_LOSS_SERVICE);
			}
			else if (pszResultCode.compareTo(DL_GENERIC_BAD_URL) == 0)
			{
				/* Download Descriptor Parsing Error */
				pDownloadStatus = dlAgentGetReportStatus(OMA_DL_STATUS_INVALID_DESCRIPTOR);
			}
			else
			{
				/* Other Case */
				pDownloadStatus = dlAgentGetReportStatus(OMA_DL_STATUS_MEMORY_ERROR);
			}
		}
		else
		{
			pDownloadStatus = dlAgentGetReportStatus(OMA_DL_STATUS_MEMORY_ERROR);
		}
		/* End of For report status(in case of download fail) */

		try
		{
			nRc = gHttpDLAdapter.tpSendData(pDownloadStatus.getBytes(), pDownloadStatus.length(), SYNCMLDL);
		}
		catch (SocketTimeoutException e)
		{
			tsLib.debugPrint(DEBUG_DL, "tpSendData Time out");
			tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
			netTimerSend.endTimer();
			nRc = TP_RET_SEND_FAIL;
		}

		if (nRc == TP_RET_OK)
		{
			tsDmMsg.taskSendMessage(TASK_MSG_DL_SYNCML_CONTINUE, null, null);
		}
		else if (nRc == TP_RET_CONNECTION_FAIL)
		{
			tsDmMsg.taskSendMessage(TASK_MSG_DL_SYNCML_CONNECTFAIL, null, null);
		}
		else
		{
			tsDmMsg.taskSendMessage(TASK_MSG_DL_SYNCML_SENDFAIL, null, null);
		}
		return nRc;
	}

	public void dltpClose(int appId)
	{
		gHttpDLAdapter.tpClose(appId);
	}

	public void dltpInit(int appId)
	{
		gHttpDLAdapter.tpInit(appId);
	}

	public int dltpAbort(int appId)
	{
		return gHttpDLAdapter.tpAbort(appId);
	}

	public void dltpCloseNetWork(int appId)
	{
		gHttpDLAdapter.tpCloseNetWork(appId);
	}

	public boolean dltpCheckRetry()
	{
		tsLib.debugPrint(DEBUG_DM, "ConntectRetryCount " + DLConnectRetryCount);
		if (DLConnectRetryCount >= TP_DL_RETRY_COUNT_MAX)
		{
			netTimerConnect.endTimer();
			netTimerReceive.endTimer();
			netTimerSend.endTimer();
			DLConnectRetryCount = 0;
			return false;
		}
		DLConnectRetryCount++;
		return true;
	}

	public static void dltpSetRetryCount(int nCnt)
	{
		DLConnectRetryCount = nCnt;
	}
	
	public static int dltpGetRetryCount()
	{
		return DLConnectRetryCount;
	}
	
	public static int dltpGetRetryFailCount()
	{
		tsLib.debugPrint(DEBUG_DM, "DLConnectRetryFailCount " + DLConnectRetryFailCount);
		return DLConnectRetryFailCount;
	}
	
	public static void dltpSetRetryFailCount(int nCnt)
	{
		tsLib.debugPrint(DEBUG_DM, "nCnt " + nCnt);
		DLConnectRetryFailCount = nCnt;
	}
}
