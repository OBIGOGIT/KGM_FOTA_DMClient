package com.tsdm.agent;

import java.io.ByteArrayOutputStream;
import java.net.SocketTimeoutException;

import com.tsdm.db.tsDefineDB;
import com.tsdm.db.tsdmDB;
import com.tsdm.db.tsdmDBadapter;
import com.tsdm.adapt.tsDefineIdle;
import com.tsdm.adapt.tsDmParamAbortmsg;
import com.tsdm.adapt.tsMsgEvent;
import com.tsdm.adapt.tsLib;
import com.tsdm.adapt.tsDmMsg;
import com.tsdm.net.netHttpAdapter;
import com.tsdm.net.netDefine;
import com.tsdm.net.netTimerConnect;
import com.tsdm.net.netTimerReceive;
import com.tsdm.net.netTimerSend;
import com.tsdm.tsService;

public class dlAgentHandler extends dlAgent implements dmDefineDevInfo, dmDefineMsg, dmDefineUIEvent, tsDefineIdle, tsDefineDB, netDefine
{
	public static void dlAgntHdlrCheckDD()
	{
	}

	public static int dlAgntHdlrCheckDdData(byte[] pReceiveData, int nAppID)
	{
		int eRet = SDL_RET_OK;

		return eRet;
	}

	int dlAgntHdlrCheckContentData(int nAppID)
	{
		int eRet = SDL_RET_OK;
		
		return eRet;
	}

	public static int dlAgntHdlrCheckDeltaPkgSize()
	{
		int nObjectSize = 0;
		int nResult = SDL_RET_OK;
		int nRet = TS_ERR_NO_MEM_READY;
		int nAgentType = SYNCML_DM_AGENT_DM;

		nObjectSize = tsdmDB.dmdbGetObjectSizeFUMO();
		tsLib.debugPrint(DEBUG_DL, "FirmwareObjectSize:" + nObjectSize);

		nRet = tsdmDBadapter.FileFreeSizeCheck(nObjectSize, nAgentType);
		if (nRet != TS_FS_OK)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, "FFS Free Space NOT  ENOUGH");
			tsdmDB.dmdbSetFUMOStatus(DM_FUMO_STATE_DOWNLOAD_DESCRIPTOR);

			return DL_MEMORY_INSUFFICIENT;
		}

		return nResult;
	}

	public static int dlAgntHdlrDownloadProgress(byte[] pRecv)
	{
		String pContentRange = null;
		int nRc = SDM_RET_OK;
		int nStatus = 0;
		int nAgentType = SYNCML_DM_AGENT_DM;
		boolean nDownloadMode;

		ByteArrayOutputStream pReceiveBuffer = new ByteArrayOutputStream();

		try
		{
			nRc = gHttpDLAdapter.tpReceiveData(pReceiveBuffer, SYNCMLDL);
		}
		catch (SocketTimeoutException e)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
			netTimerReceive.endTimer();
			nRc = TP_RET_RECEIVE_FAIL;
		}
		catch (NullPointerException e)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
			netTimerReceive.endTimer();
			nRc = TP_RET_RECEIVE_FAIL;
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
			netTimerReceive.endTimer();
			nRc = TP_RET_RECEIVE_FAIL;
		}

		if (nRc == TP_RET_HTTP_RES_FAIL)
		{
			tsDmParamAbortmsg pAbortParam;
			pAbortParam = tsDmMsg.createAbortMessage(TP_ECODE_HTTP_RETURN_STATUS_ERROR, false);
			tsDmMsg.taskSendMessage(TASK_MSG_DL_SYNCML_ABORT, pAbortParam, null);
			return nRc;
		}
		else if (nRc == TP_RET_HTTP_CONNECTION_POOL) {
			tsLib.debugPrint(DEBUG_DL, "TP_RET_HTTP_CONNECTION_POOL");
			tsMsgEvent.SetMsgEvent(null, DM_EVENT_UI_DOWNLOAD_FAIL_RETRY_CONFIRM);
			return nRc;
		}
		else if (nRc == TP_RET_FILE_ERROR)
		{
			// for Memory space
			int nFUMOFileId = tsdmDB.dmdbGetFileIdFirmwareData();
			tsdmDB.dmdbDeleteFile(nFUMOFileId);


			tsdmDB.dmdbSetFUMOResultCode(DL_GENERIC_DOWNLOAD_FAILED_OUT_MEMORY);
			tsdmDB.dmdbSetFUMOStatus(DM_FUMO_STATE_DOWNLOAD_FAILED);

			tsDmMsg.taskSendMessage(TASK_MSG_DL_SYNCML_FINISH, null, null);
			tsDmMsg.taskSendMessage(TASK_MSG_DL_SYNCML_CONNECT, null, null);
			tsMsgEvent.SetMsgEvent(null, DL_EVENT_UI_MEMORY_FULL);
			return nRc;
		}
		else if (nRc != SDM_RET_OK)
		{
			int nAgentStatus = 0;
			nAgentStatus = tsdmDB.dmdbGetFUMOStatus();
			if (nAgentStatus == DM_FUMO_STATE_DOWNLOAD_IN_CANCEL)
				return nRc;
			else if (nAgentStatus == DM_FUMO_STATE_DOWNLOAD_FAILED) // Delta file Download 100% Over Issue - Download Fail
				return nRc;
			else if (nAgentStatus == DM_FUMO_STATE_SUSPEND)
				return nRc;

			tsDmMsg.taskSendMessage(TASK_MSG_DL_SYNCML_RECEIVEFAIL, null, null);
			return nRc;
		}

		nDownloadMode = tsdmDB.dmdbGetFUMODownloadMode();
		if (nDownloadMode)
			tsLib.debugPrint(DEBUG_DL, "nDownloadMode is TRUE");
		else
			tsLib.debugPrint(DEBUG_DL, "nDownloadMode is FALSE");

		nStatus = dlAgentGetHttpConStatus(nDownloadMode);
		if (nStatus != SDL_RET_OK)
		{
			pContentRange = dlAgentGetHttpContentRange(nDownloadMode);
		}

		nRc = dlAgntHdlrDownloadProgressFumo(nStatus, nDownloadMode, pContentRange);

		return nRc;
	}

	public static int dlAgntHdlrDownloadProgressFumo(int nStatus, boolean nDownloadMode, String pContentRange)
	{
		String szResURL = "";
		String pDownloadStatus = null;
		int nMechanism = 0;
		int nRc = SDM_RET_OK;

		if (nStatus == SDL_RET_OK)
		{
			tsdmDB.dmdbSetFUMOStatus(DM_FUMO_STATE_DOWNLOAD_COMPLETE);

			szResURL = tsdmDB.dmdbGetStatusAddrFUMO(szResURL);
			if (tsLib.isEmpty(szResURL)) // defect_110921
			{
				tsDmMsg.taskSendMessage(TASK_MSG_DL_SYNCML_FINISH, null, null);

				nMechanism = tsdmDB.dmdbGetFUMOUpdateMechanism();
				if (nMechanism == DM_FUMO_MECHANISM_ALTERNATIVE)
				{
					tsdmDB.dmdbSetFUMOStatus(DM_FUMO_STATE_READY_TO_UPDATE);
					try
					{
						Thread.sleep(1000);
					}
					catch (InterruptedException e)
					{
						tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
						Thread.currentThread().interrupt();
					}
					tsMsgEvent.SetMsgEvent(null, DL_EVENT_UI_DOWNLOAD_IN_COMPLETE);
				}
				else if (nMechanism == DM_FUMO_MECHANISM_ALTERNATIVE_DOWNLOAD)
				{
					tsDmMsg.taskSendMessage(TASK_MSG_DM_SYNCML_CONNECT, null, null);
				}
				else
				{
					tsLib.debugPrintException(DEBUG_EXCEPTION, "ERROR");
				}
			}
			else
			{
				String objectURL = null;
				objectURL = tsdmDB.dmdbGetDownloadAddrFUMO(objectURL);
				String respURL = null;
				respURL = tsdmDB.dmdbGetStatusAddrFUMO(respURL);

				int nRet = netHttpAdapter.tpCheckURL(objectURL, respURL);
				if (nRet == TP_RET_CHANGED_PROFILE)
				{
					netHttpAdapter.netAdpSetReuse(true);
					tsDmMsg.taskSendMessage(TASK_MSG_DL_SYNCML_FINISH, null, null);
					tsDmMsg.taskSendMessage(TASK_MSG_DL_SYNCML_CONNECT, null, null);
					nRet = TP_RET_CONNECTION_FAIL;
				}
				else if (nRet == TP_RET_INVALID_PARAM)
				{
					nRet = TP_RET_CONNECTION_FAIL;
				}
				else
				{
					if (netHttpAdapter.pHttpObj[SYNCMLDL].nHttpConnection == TP_HTTP_CONNECTION_CLOSE)
					{
						try
						{
							nRc = gHttpDLAdapter.tpOpen(SYNCMLDL);
						}
						catch (SocketTimeoutException e)
						{
							tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
							netTimerConnect.endTimer();
							nRc = TP_RET_CONNECTION_FAIL;
						}
						if (nRc != TP_RET_OK)
						{
							tsDmMsg.taskSendMessage(TASK_MSG_DL_SYNCML_CONNECTFAIL, null, null);
							return nRc;
						}
					}

					try
					{
						if (nDownloadMode)
							gHttpDLAdapter.tpSetHttpObj(szResURL, null, null, HTTP_METHOD_POST, SYNCMLDL, true);
						else
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
					tsService.getDownoloadSpeed();
					pDownloadStatus = dlAgentGetReportStatus(OMA_DL_STAUS_SUCCESS);
					if (!tsLib.isEmpty(pDownloadStatus))
					{
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
						// SEND_FAIL
						{
							tsDmMsg.taskSendMessage(TASK_MSG_DL_SYNCML_SENDFAIL, null, null);
						}
					}
				}
			}
		}
		else if (nStatus == SDL_RET_CONTINUE)
		{
			szResURL = tsdmDB.dmdbGetDownloadAddrFUMO(szResURL);

			try
			{
				if (nDownloadMode)
					gHttpDLAdapter.tpSetHttpObj(szResURL, null, pContentRange, HTTP_METHOD_GET, SYNCMLDL, true);
				else
					gHttpDLAdapter.tpSetHttpObj(szResURL, null, pContentRange, HTTP_METHOD_GET, SYNCMLDL, false);
			}
			catch (NullPointerException e)
			{
				tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
				netTimerSend.endTimer();
				nRc = TP_RET_SEND_FAIL;
				tsDmMsg.taskSendMessage(TASK_MSG_DL_SYNCML_SENDFAIL, null, null);
				return nRc;
			}

			try
			{
				nRc = gHttpDLAdapter.tpSendData(null, 0, SYNCMLDL);
			}
			catch (SocketTimeoutException e)
			{
				tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
				netTimerSend.endTimer();
				nRc = TP_RET_SEND_FAIL;
			}
			if (nRc == TP_RET_OK)
			{
				tsLib.debugPrint(DEBUG_DL, "TASK_MSG_DL_SYNCML_CONTINUE");
				tsDmMsg.taskSendMessage(TASK_MSG_DL_SYNCML_CONTINUE, null, null);
			}
			else if (nRc == TP_RET_CONNECTION_FAIL)
			{
				tsLib.debugPrintException(DEBUG_EXCEPTION, "TP_RET_CONNECTION_FAIL");
				tsDmMsg.taskSendMessage(TASK_MSG_DL_SYNCML_CONNECTFAIL, null, null);
			}
			else
			// SEND_FAIL
			{
				tsDmMsg.taskSendMessage(TASK_MSG_DL_SYNCML_SENDFAIL, null, null);
			}
		}
		else
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, "What Problem");
			tsdmDB.dmdbSetFUMOStatus(DM_FUMO_STATE_DOWNLOAD_FAILED);
			tsdmDB.dmdbSetFUMOResultCode(DL_GENERIC_DOWNLOAD_FAILED_OUT_MEMORY);
			szResURL = tsdmDB.dmdbGetStatusAddrFUMO(szResURL);
			pDownloadStatus = dlAgentGetReportStatus(OMA_DL_STATUS_MEMORY_ERROR);

			String objectUrl = null;
			objectUrl = tsdmDB.dmdbGetDownloadAddrFUMO(szResURL);
			int nRet = netHttpAdapter.tpCheckURL(objectUrl, szResURL);
			if (nRet == TP_RET_CHANGED_PROFILE)
			{
				netHttpAdapter.netAdpSetReuse(true);
				tsDmMsg.taskSendMessage(TASK_MSG_DL_SYNCML_FINISH, null, null);
				tsDmMsg.taskSendMessage(TASK_MSG_DL_SYNCML_CONNECT, null, null);
				nRet = TP_RET_RECEIVE_FAIL;
			}
			else if (nRet == TP_RET_INVALID_PARAM)
			{
				nRet = TP_RET_RECEIVE_FAIL;
			}
			else
			{
				try
				{
					if (nDownloadMode)
						gHttpDLAdapter.tpSetHttpObj(szResURL, null, pContentRange, HTTP_METHOD_POST, SYNCMLDL, true);
					else
						gHttpDLAdapter.tpSetHttpObj(szResURL, null, pContentRange, HTTP_METHOD_POST, SYNCMLDL, false);
				}
				catch (NullPointerException e)
				{
					tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
					netTimerSend.endTimer();
					nRc = TP_RET_SEND_FAIL;
					tsDmMsg.taskSendMessage(TASK_MSG_DL_SYNCML_SENDFAIL, null, null);
					return nRc;
				}

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
				// SEND_FAIL
				{
					tsDmMsg.taskSendMessage(TASK_MSG_DL_SYNCML_SENDFAIL, null, null);
				}
			}
		}
		return nRc;
	}

	public int dlWriteFirmwareObject(int nReceiveDataSize, byte[] pRecv)
	{
		int nFileId = 0;
		int nRc = SDL_RET_OK;
		int nAgentType = SYNCML_DM_AGENT_DM;
		boolean nWriteStatus = false;

		nFileId = tsdmDB.dmdbGetFileIdFirmwareData();

		nRc = dlAgntHdlrCheckContentData(SYNCMLDL);
		if (nRc == SDL_RET_OK)
		{
			nWriteStatus = dlAgentGetWriteStatus();
			if (nWriteStatus)
			{
				tsLib.debugPrintException(DEBUG_EXCEPTION, "FFS WRITE FAILED CONNECT RETRY");
				return nRc;
			}

			if (nReceiveDataSize > 0)
			{
				int nRet = 0;
				nRet = tsdmDB.dmAppendFile(nFileId, nReceiveDataSize, pRecv);
				
				if(_SYNCML_TS_DM_DELTA_MULTI_MEMORY_STORAGE_)
				{
					if (tsdmDB.dmdbGetDeltaFileSaveIndex() == DELTA_EXTERNAL_SD_MEMORY)
					{
						if (!dmDevinfoAdapter.checkExternalSdMemoryAvailable())
							return TS_ERR_NO_MEM_READY;
					}
				}				
				else if(_SYNCML_TS_DM_DELTA_EXTERNAL_STORAGE_)
				{
					if(!dmDevinfoAdapter.checkExternalMemoryAvailable())
						return TS_ERR_NO_MEM_READY;
				}

				if (nRet != TS_FS_OK)
				{
					dlAgentSetWriteStatus(true);
					tsLib.debugPrintException(DEBUG_EXCEPTION, "FFS WRITE FAILED");
					return nRet;
				}
				//tsLib.debugPrint(DEBUG_DL, "FFS WRITE OK. dataSize = " + nReceiveDataSize);
			}
		}
		return SDL_RET_OK;
	}

	public static int dlAgntHdlrDownloadStart()
	{
		String pContentRange = "";
		int nRc = SDM_RET_OK;
		int nStatus;
		int nAgentType = SYNCML_DM_AGENT_DM;
		boolean nDownloadMode;

		tsLib.debugPrint(DEBUG_DL, "");

		nDownloadMode = tsdmDB.dmdbGetFUMODownloadMode();
		nStatus = dlAgentGetHttpConStatus(nDownloadMode);
		if (nStatus != SDL_RET_OK)
		{
			pContentRange = dlAgentGetHttpContentRange(nDownloadMode);
		}

		nRc = dlAgntHdlrDownloadStartFumo(nStatus, nDownloadMode, pContentRange);

		return nRc;
	}

	public static int dlAgntHdlrDownloadStartFumo(int nStatus, boolean nDownloadMode, String pContentRange)
	{
		String szResURL = null;
		String pDownloadStatus = null;
		int nRc = SDM_RET_OK;
		int nMechanism;

		tsLib.debugPrint(DEBUG_DL, "");

		if (nStatus == SDL_RET_OK)
		{
			// ADD : for without installnotify url
			tsdmDB.dmdbSetFUMOStatus(DM_FUMO_STATE_DOWNLOAD_COMPLETE);

			szResURL = tsdmDB.dmdbGetStatusAddrFUMO(szResURL);

			// ADD : for without installnotify url
			if (tsLib.isEmpty(szResURL))
			{
				/* ADD : For disconnect Network(without installnotify url) */
				tsDmMsg.taskSendMessage(TASK_MSG_DL_SYNCML_FINISH, null, null);

				// ADD : for gernic Alert Type for Update Report
				nMechanism = tsdmDB.dmdbGetFUMOUpdateMechanism();
				if (nMechanism == DM_FUMO_MECHANISM_ALTERNATIVE)
				{
					tsdmDB.dmdbSetFUMOStatus(DM_FUMO_STATE_READY_TO_UPDATE);
					try
					{
						Thread.sleep(1000);
					}
					catch (InterruptedException e)
					{
						tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
						Thread.currentThread().interrupt();
					}
					tsMsgEvent.SetMsgEvent(null, DL_EVENT_UI_DOWNLOAD_IN_COMPLETE);
				}
				else if (nMechanism == DM_FUMO_MECHANISM_ALTERNATIVE_DOWNLOAD)
				{
					tsDmMsg.taskSendMessage(TASK_MSG_DM_SYNCML_CONNECT, null, null);
				}
				else
				{
					tsLib.debugPrintException(DEBUG_EXCEPTION, "ERROR.");
				}
			}
			else
			{
				String objectURL = null;
				objectURL = tsdmDB.dmdbGetDownloadAddrFUMO(objectURL);
				String respURL = null;
				respURL = tsdmDB.dmdbGetStatusAddrFUMO(respURL);

				int nRet = netHttpAdapter.tpCheckURL(objectURL, respURL);
				if (nRet == TP_RET_CHANGED_PROFILE)
				{
					netHttpAdapter.netAdpSetReuse(true);
					tsDmMsg.taskSendMessage(TASK_MSG_DL_SYNCML_FINISH, null, null);
					tsDmMsg.taskSendMessage(TASK_MSG_DL_SYNCML_CONNECT, null, null);
					nRet = TP_RET_CONNECTION_FAIL;
				}
				else if (nRet == TP_RET_INVALID_PARAM)
				{
					nRet = TP_RET_CONNECTION_FAIL;
				}
				else
				{
					// normal case
					try
					{
						if (nDownloadMode)
							gHttpDLAdapter.tpSetHttpObj(szResURL, null, pContentRange, HTTP_METHOD_POST, SYNCMLDL, true);
						else
							gHttpDLAdapter.tpSetHttpObj(szResURL, null, pContentRange, HTTP_METHOD_POST, SYNCMLDL, false);
					}
					catch (NullPointerException e)
					{
						tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
						netTimerSend.endTimer();
						nRc = TP_RET_SEND_FAIL;
						tsDmMsg.taskSendMessage(TASK_MSG_DL_SYNCML_SENDFAIL, null, null);
						return nRc;
					}
					tsService.getDownoloadSpeed();
					pDownloadStatus = dlAgentGetReportStatus(OMA_DL_STAUS_SUCCESS);
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
					// SEND_FAIL
					{
						tsDmMsg.taskSendMessage(TASK_MSG_DL_SYNCML_SENDFAIL, null, null);
					}
				}
			}
		}
		else if (nStatus == SDL_RET_CONTINUE)
		{
			szResURL = tsdmDB.dmdbGetDownloadAddrFUMO(szResURL);

			try
			{
				if (nDownloadMode)
					gHttpDLAdapter.tpSetHttpObj(szResURL, null, pContentRange, HTTP_METHOD_GET, SYNCMLDL, true);
				else
					gHttpDLAdapter.tpSetHttpObj(szResURL, null, pContentRange, HTTP_METHOD_GET, SYNCMLDL, false);
			}
			catch (NullPointerException e)
			{
				tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
				netTimerSend.endTimer();
				nRc = TP_RET_SEND_FAIL;
				tsDmMsg.taskSendMessage(TASK_MSG_DL_SYNCML_SENDFAIL, null, null);
				return nRc;
			}

			try
			{
				nRc = gHttpDLAdapter.tpSendData(null, 0, SYNCMLDL);
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
			// SEND_FAIL
			{
				tsDmMsg.taskSendMessage(TASK_MSG_DL_SYNCML_SENDFAIL, null, null);
			}

			tsdmDB.dmdbSetFUMOStatus(DM_FUMO_STATE_DOWNLOAD_IN_PROGRESS);
		}
		else
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, "What Problem");

			tsdmDB.dmdbSetFUMOStatus(DM_FUMO_STATE_DOWNLOAD_FAILED);
			tsdmDB.dmdbSetFUMOResultCode(DL_GENERIC_DOWNLOAD_FAILED_OUT_MEMORY);
			szResURL = tsdmDB.dmdbGetStatusAddrFUMO(szResURL);

			String downloadURL = null;
			downloadURL = tsdmDB.dmdbGetDownloadAddrFUMO(downloadURL);

			int nRet = netHttpAdapter.tpCheckURL(downloadURL, szResURL);
			if (nRet == TP_RET_CHANGED_PROFILE)
			{
				netHttpAdapter.netAdpSetReuse(true);
				tsDmMsg.taskSendMessage(TASK_MSG_DL_SYNCML_FINISH, null, null);
				tsDmMsg.taskSendMessage(TASK_MSG_DL_SYNCML_CONNECT, null, null);
				nRet = TP_RET_CONNECTION_FAIL;
			}
			else if (nRet == TP_RET_INVALID_PARAM)
			{
				nRet = TP_RET_CONNECTION_FAIL;
			}
			else
			{
				pDownloadStatus = dlAgentGetReportStatus(OMA_DL_STATUS_ATTRIBUTE_MISMATCH);

				try
				{
					if (nDownloadMode)
						gHttpDLAdapter.tpSetHttpObj(szResURL, null, pContentRange, HTTP_METHOD_POST, SYNCMLDL, true);
					else
						gHttpDLAdapter.tpSetHttpObj(szResURL, null, pContentRange, HTTP_METHOD_POST, SYNCMLDL, false);
				}
				catch (NullPointerException e)
				{
					tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
					netTimerSend.endTimer();
					nRc = TP_RET_SEND_FAIL;
					tsDmMsg.taskSendMessage(TASK_MSG_DL_SYNCML_SENDFAIL, null, null);
					return nRc;
				}

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
				// SEND_FAIL
				{
					tsDmMsg.taskSendMessage(TASK_MSG_DL_SYNCML_SENDFAIL, null, null);
				}
			}
		}
		return nRc;
	}

	public static int dlAgntHdlrDD(byte[] pRecv)
	{
		int nRet = SDM_RET_OK;
		int nReceiveDataSize = 0;

		tsLib.debugPrint(DEBUG_DL, "");
		ByteArrayOutputStream pReceiveBuffer = new ByteArrayOutputStream();

		try
		{
			nRet = gHttpDLAdapter.tpReceiveData(pReceiveBuffer, SYNCMLDL);
		}
		catch (SocketTimeoutException e)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
			netTimerReceive.endTimer();
			nRet = TP_RET_RECEIVE_FAIL;
		}
		catch (NullPointerException e)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
			netTimerReceive.endTimer();
			nRet = TP_RET_RECEIVE_FAIL;
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
			netTimerReceive.endTimer();
			nRet = TP_RET_RECEIVE_FAIL;
		}

		if (nRet == TP_RET_HTTP_RES_FAIL)
		{
			tsDmParamAbortmsg pAbortParam;
			pAbortParam = tsDmMsg.createAbortMessage(TP_ECODE_HTTP_RETURN_STATUS_ERROR, false);
			tsDmMsg.taskSendMessage(TASK_MSG_DL_SYNCML_ABORT, pAbortParam, null);
			return nRet;
		}
		else if (nRet != SDL_RET_OK)
		{
			tsDmMsg.taskSendMessage(TASK_MSG_DL_SYNCML_RECEIVEFAIL, null, null);
			return nRet;
		}

		if (pRecv == null)
			pRecv = new byte[pReceiveBuffer.size()];

		pRecv = pReceiveBuffer.toByteArray();
		nReceiveDataSize = pReceiveBuffer.toByteArray().length;
		nRet = dlAgntHdlrCheckDdData(pRecv, SYNCMLDL);
		tsLib.debugPrint(DEBUG_DL, "DD check success. nRet = [" + nRet + "]");

		if (nRet == SDL_RET_OK)
		{
			int nAgentType = SYNCML_DM_AGENT_DM;

			nRet = dlAgentParserDescriptor(pRecv, nReceiveDataSize);
			if (nRet == SDL_RET_OK)
			{
				tsdmDB.dmdbSetFUMOStatus(DM_FUMO_STATE_DOWNLOAD_DESCRIPTOR);
				dlAgntHdlrCheckDD();
				tsDmMsg.taskSendMessage(TASK_MSG_DL_SYNCML_FINISH, null, null);
			}
			else
			/* DD Parsing Error Stop Session */
			{
				tsdmDB.dmdbSetFUMOResultCode(DL_GENERIC_BAD_URL);
				tsdmDB.dmdbSetFUMOStatus(DM_FUMO_STATE_DOWNLOAD_FAILED_REPORTING);
				tsDmMsg.taskSendMessage(TASK_MSG_DL_SYNCML_FINISH, null, null);

				if (netHttpAdapter.pHttpObj[SYNCMLDL].nHttpConnection == TP_HTTP_CONNECTION_CLOSE)
				{
					try
					{
						nRet = gHttpDLAdapter.tpOpen(SYNCMLDL);
					}
					catch (SocketTimeoutException e)
					{
						tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
						netTimerConnect.endTimer();
						nRet = TP_RET_CONNECTION_FAIL;
					}
					if (nRet != TP_RET_OK)
					{
						tsDmMsg.taskSendMessage(TASK_MSG_DM_SYNCML_CONNECTFAIL, null, null);
					}
					else
						tsDmMsg.taskSendMessage(TASK_MSG_DM_SYNCML_CONNECT, null, null);
				}
				else
					tsDmMsg.taskSendMessage(TASK_MSG_DM_SYNCML_CONNECT, null, null);
				/* End of For report status(in case of download fail) */
				tsMsgEvent.SetMsgEvent(null, DM_EVENT_UI_SYNC_ERROR);
			}
		}
		return nRet;
	}

	public static int dlAgntHdlrDownloadTakeOver()
	{
		String pContentRange = null;
		int nStatus = 0;
		int nRc = SDM_RET_OK;
		int nAgentType = SYNCML_DM_AGENT_DM;
		
		// defect_110921
		boolean bDownloadMode = tsdmDB.dmdbGetFUMODownloadMode();
		tsLib.debugPrint(DEBUG_DL, "nDownloadMode = " + String.valueOf(bDownloadMode));

		nStatus = dlAgentGetHttpConStatus(bDownloadMode);

		if (nStatus != SDL_RET_OK)
		{
			pContentRange = dlAgentGetHttpContentRange(bDownloadMode);
		}

		nRc = dlAgntHdlrDownloadTakeOverFumo(nStatus, bDownloadMode, pContentRange);

		return nRc;
	}

	public static int dlAgntHdlrDownloadTakeOverFumo(int nStatus, boolean nDownloadMode, String pContentRange)
	{
		String szResURL = "";
		String pSendData = null;
		int nMechanism = 0;
		int nRc = SDM_RET_OK;

		int nOrgStatus = tsdmDB.dmdbGetFUMOStatus();
		tsLib.debugPrint(DEBUG_DL, "fumo org status = " + nOrgStatus);

		if (nStatus == SDL_RET_OK)
		{
			tsdmDB.dmdbSetFUMOStatus(DM_FUMO_STATE_DOWNLOAD_COMPLETE);
			szResURL = tsdmDB.dmdbGetStatusAddrFUMO(szResURL);

			if (tsLib.isEmpty(szResURL)) // defect_110921
			{
				tsDmMsg.taskSendMessage(TASK_MSG_DL_SYNCML_FINISH, null, null);

				nMechanism = tsdmDB.dmdbGetFUMOUpdateMechanism();
				if (nMechanism == DM_FUMO_MECHANISM_ALTERNATIVE)
				{
					tsdmDB.dmdbSetFUMOStatus(DM_FUMO_STATE_READY_TO_UPDATE);
					try
					{
						Thread.sleep(1000);
					}
					catch (InterruptedException e)
					{
						tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
						Thread.currentThread().interrupt();
					}
					tsMsgEvent.SetMsgEvent(null, DL_EVENT_UI_DOWNLOAD_IN_COMPLETE);
				}
				else if (nMechanism == DM_FUMO_MECHANISM_ALTERNATIVE_DOWNLOAD)
				{
					tsDmMsg.taskSendMessage(TASK_MSG_DM_SYNCML_CONNECT, null, null);
				}
				else
				{
					tsLib.debugPrintException(DEBUG_EXCEPTION, "ERROR.");
				}
			}
			else
			{
				int nRet;
				if (nOrgStatus == DM_FUMO_STATE_DOWNLOAD_IN_PROGRESS)
				{
					String downloadUrl = null;
					downloadUrl = tsdmDB.dmdbGetDownloadAddrFUMO(downloadUrl);
					nRet = netHttpAdapter.tpCheckURL(downloadUrl, szResURL);
				}
				else
				{
					nRet = TP_RET_OK;
				}

				if (nRet == TP_RET_CHANGED_PROFILE)
				{
					netHttpAdapter.netAdpSetReuse(true);
					tsDmMsg.taskSendMessage(TASK_MSG_DL_SYNCML_FINISH, null, null);
					tsDmMsg.taskSendMessage(TASK_MSG_DL_SYNCML_CONNECT, null, null);
					nRet = TP_RET_CONNECTION_FAIL;
				}
				else if (nRet == TP_RET_INVALID_PARAM)
				{
					nRet = TP_RET_CONNECTION_FAIL;
				}
				else
				{
					tsService.getDownoloadSpeed();
					pSendData = dlAgentGetReportStatus(OMA_DL_STAUS_SUCCESS);

					try
					{
						if (nDownloadMode)
							gHttpDLAdapter.tpSetHttpObj(szResURL, null, pContentRange, HTTP_METHOD_POST, SYNCMLDL, true);
						else
							gHttpDLAdapter.tpSetHttpObj(szResURL, null, pContentRange, HTTP_METHOD_POST, SYNCMLDL, false);
					}
					catch (NullPointerException e)
					{
						tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
						netTimerSend.endTimer();
						nRc = TP_RET_SEND_FAIL;
						tsDmMsg.taskSendMessage(TASK_MSG_DL_SYNCML_SENDFAIL, null, null);
						return nRc;
					}

					try
					{
						nRc = gHttpDLAdapter.tpSendData(pSendData.getBytes(), pSendData.length(), SYNCMLDL);
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
					else if (nRc == TP_RET_HTTP_RES_FAIL)
					{
						tsDmParamAbortmsg pAbortParam;
						pAbortParam = tsDmMsg.createAbortMessage(TP_ECODE_HTTP_RETURN_STATUS_ERROR, false);
						tsDmMsg.taskSendMessage(TASK_MSG_DL_SYNCML_ABORT, pAbortParam, null);
					}
					else
					// SEND_FAIL
					{
						tsDmMsg.taskSendMessage(TASK_MSG_DL_SYNCML_SENDFAIL, null, null);
					}
				}
			}
		}
		else if (nStatus == SDL_RET_CONTINUE)
		{
			szResURL = tsdmDB.dmdbGetDownloadAddrFUMO(szResURL);
			try
			{
				if (nDownloadMode)
					gHttpDLAdapter.tpSetHttpObj(szResURL, null, pContentRange, HTTP_METHOD_GET, SYNCMLDL, true);
				else
					gHttpDLAdapter.tpSetHttpObj(szResURL, null, pContentRange, HTTP_METHOD_GET, SYNCMLDL, false);
			}
			catch (NullPointerException e)
			{
				tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
				netTimerSend.endTimer();
				nRc = TP_RET_SEND_FAIL;
				tsDmMsg.taskSendMessage(TASK_MSG_DL_SYNCML_SENDFAIL, null, null);
				return nRc;
			}

			try
			{
				nRc = gHttpDLAdapter.tpSendData(null, 0, SYNCMLDL);
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
			// SEND_FAIL
			{
				tsDmMsg.taskSendMessage(TASK_MSG_DL_SYNCML_SENDFAIL, null, null);
			}

			tsdmDB.dmdbSetFUMOStatus(DM_FUMO_STATE_DOWNLOAD_IN_PROGRESS);
		}
		else
		{
			tsLib.debugPrint(DEBUG_DL, "DM_FUMO_STATE_DOWNLOAD_FAILED");
			tsdmDB.dmdbSetFUMOStatus(DM_FUMO_STATE_DOWNLOAD_FAILED);
			tsdmDB.dmdbSetFUMOResultCode(DL_GENERIC_DOWNLOAD_FAILED_OUT_MEMORY);
			szResURL = tsdmDB.dmdbGetStatusAddrFUMO(szResURL);

			String objectURL = null;
			objectURL = tsdmDB.dmdbGetDownloadAddrFUMO(objectURL);
			String respURL = null;
			respURL = tsdmDB.dmdbGetStatusAddrFUMO(respURL);

			int nRet = netHttpAdapter.tpCheckURL(objectURL, respURL);
			if (nRet == TP_RET_CHANGED_PROFILE)
			{
				netHttpAdapter.netAdpSetReuse(true);
				tsDmMsg.taskSendMessage(TASK_MSG_DL_SYNCML_FINISH, null, null);
				tsDmMsg.taskSendMessage(TASK_MSG_DL_SYNCML_CONNECT, null, null);
				nRet = TP_RET_CONNECTION_FAIL;
			}
			else if (nRet == TP_RET_INVALID_PARAM)
			{
				nRet = TP_RET_CONNECTION_FAIL;
			}
			else
			{
				pSendData = dlAgentGetReportStatus(OMA_DL_STATUS_MEMORY_ERROR);

				try
				{
					if (nDownloadMode)
						gHttpDLAdapter.tpSetHttpObj(szResURL, null, pContentRange, HTTP_METHOD_POST, SYNCMLDL, true);
					else
						gHttpDLAdapter.tpSetHttpObj(szResURL, null, pContentRange, HTTP_METHOD_POST, SYNCMLDL, false);
				}
				catch (NullPointerException e)
				{
					tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
					netTimerSend.endTimer();
					nRc = TP_RET_SEND_FAIL;
					tsDmMsg.taskSendMessage(TASK_MSG_DL_SYNCML_SENDFAIL, null, null);
					return nRc;
				}

				try
				{
					nRc = gHttpDLAdapter.tpSendData(pSendData.getBytes(), pSendData.length(), SYNCMLDL);
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
				// SEND_FAIL
				{
					tsDmMsg.taskSendMessage(TASK_MSG_DL_SYNCML_SENDFAIL, null, null);
				}
			}
		}
		return nRc;
	}

	public static int dlAgntHdlrDownloadComplete(byte[] pRecv)
	{
		int nRc = SDM_RET_OK;
		int nAgentType = SYNCML_DM_AGENT_DM;

		tsLib.debugPrint(DEBUG_DL, "");

		ByteArrayOutputStream pReceiveBuffer = new ByteArrayOutputStream();

		try
		{
			nRc = gHttpDLAdapter.tpReceiveData(pReceiveBuffer, SYNCMLDL);
		}
		catch (SocketTimeoutException e)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
			netTimerReceive.endTimer();
			nRc = TP_RET_RECEIVE_FAIL;
		}
		catch (NullPointerException e)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
			netTimerReceive.endTimer();
			nRc = TP_RET_RECEIVE_FAIL;
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
			netTimerReceive.endTimer();
			nRc = TP_RET_RECEIVE_FAIL;
		}

		if (nRc == TP_RET_HTTP_RES_FAIL)
		{
			tsDmParamAbortmsg pAbortParam;
			pAbortParam = tsDmMsg.createAbortMessage(TP_ECODE_HTTP_RETURN_STATUS_ERROR, false);
			tsDmMsg.taskSendMessage(TASK_MSG_DL_SYNCML_ABORT, pAbortParam, null);
			return nRc;
		}
		else if (nRc != SDM_RET_OK)
		{
			tsDmMsg.taskSendMessage(TASK_MSG_DL_SYNCML_RECEIVEFAIL, null, null);
			return nRc;
		}

		if (pRecv == null && pReceiveBuffer != null && pReceiveBuffer.hashCode() != 0)
			pRecv = new byte[pReceiveBuffer.size()];

		if (pReceiveBuffer != null && pReceiveBuffer.hashCode() != 0)
			pRecv = pReceiveBuffer.toByteArray();

		nRc = dlAgntHdlrDownloadCompleteFumo();

		return nRc;
	}

	public static int dlAgntHdlrDownloadCompleteFumo()
	{
		int nRc = SDM_RET_OK;
		int nAgentStatus = 0;
		int nMechanism = 0;

		nAgentStatus = tsdmDB.dmdbGetFUMOStatus();

		if (nAgentStatus == DM_FUMO_STATE_DOWNLOAD_IN_CANCEL)
		{
			tsDmMsg.taskSendMessage(TASK_MSG_DL_SYNCML_FINISH, null, null);
			tsdmDB.dmdbSetFUMOStatus(DM_FUMO_STATE_USER_CANCEL_REPORTING);
			tsDmMsg.taskSendMessage(TASK_MSG_DM_SYNCML_CONNECT, null, null);
		}
		else if (nAgentStatus == DM_FUMO_STATE_DOWNLOAD_FAILED)
		{
			tsDmMsg.taskSendMessage(TASK_MSG_DL_SYNCML_FINISH, null, null);
			tsdmDB.dmdbSetFUMOStatus(DM_FUMO_STATE_DOWNLOAD_FAILED_REPORTING);
			if (netHttpAdapter.pHttpObj[SYNCMLDL].nHttpConnection == TP_HTTP_CONNECTION_CLOSE)
			{
				try
				{
					nRc = gHttpDLAdapter.tpOpen(SYNCMLDL);
				}
				catch (SocketTimeoutException e)
				{
					tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
					netTimerConnect.endTimer();
					nRc = TP_RET_CONNECTION_FAIL;
				}
				if (nRc != TP_RET_OK)
				{
					tsDmMsg.taskSendMessage(TASK_MSG_DM_SYNCML_CONNECTFAIL, null, null);
				}
				else
					tsDmMsg.taskSendMessage(TASK_MSG_DM_SYNCML_CONNECT, null, null);
			}
			else
				tsDmMsg.taskSendMessage(TASK_MSG_DM_SYNCML_CONNECT, null, null);
		}
		else
		{
			nMechanism = tsdmDB.dmdbGetFUMOUpdateMechanism();
			tsdmDB.dmdbSetFUMODownloadMode(true); // download mode -> true

			if (nMechanism == DM_FUMO_MECHANISM_ALTERNATIVE)
			{
				// ADD : for Not Network Deactivate.
				tsDmMsg.taskSendMessage(TASK_MSG_DL_SYNCML_FINISH, null, null);
				tsdmDB.dmdbSetFUMOStatus(DM_FUMO_STATE_READY_TO_UPDATE);
				try
				{
					Thread.sleep(1000);
				}
				catch (InterruptedException e)
				{
					tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
					Thread.currentThread().interrupt();
				}
				// Here ....
				tsMsgEvent.SetMsgEvent(null, DL_EVENT_UI_DOWNLOAD_IN_COMPLETE);
			}
			else if (nMechanism == DM_FUMO_MECHANISM_ALTERNATIVE_DOWNLOAD)
			{
				// ADD : for Not Network Deactivate.
				tsDmMsg.taskSendMessage(TASK_MSG_DL_SYNCML_FINISH, null, null);
				tsdmDB.dmdbSetFUMOStatus(DM_FUMO_STATE_DOWNLOAD_COMPLETE);
				tsDmMsg.taskSendMessage(TASK_MSG_DM_SYNCML_CONNECT, null, null);
			}
		}
		return nRc;
	}

	public static int dlAgntHdlrStartOMADLAgent(int nEvent)
	{
		int nDLStatus;
		int rc = SDM_RET_OK;
		int nAppID = SYNCMLDL;
		int nAgentType = SYNCML_DM_AGENT_DM;
		byte[] pBuffer = null;

		pBuffer = dlAgentGetBuffer();

		nDLStatus = tsdmDB.dmdbGetFUMOStatus();

		tsLib.debugPrint(DEBUG_DL, "nEvent [" + nEvent + "] nAgentStatus[" + nDLStatus + "]");

		switch (nEvent)
		{
			case TASK_MSG_DL_SYNCML_START:
			{
				switch (nDLStatus)
				{
					case DM_FUMO_STATE_IDLE_START:
					{
						int ret = 0;
						String pResponsURL = tsdmDB.dmdbGetServerUrl(SYNCMLDL);

						try
						{
							gHttpDLAdapter.tpSetHttpObj(pResponsURL, null, null, HTTP_METHOD_GET, SYNCMLDL, false);
						}
						catch (NullPointerException e)
						{
							tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
							netTimerSend.endTimer();
							ret = TP_RET_SEND_FAIL;
							tsDmMsg.taskSendMessage(TASK_MSG_DL_SYNCML_SENDFAIL, null, null);
							return ret;
						}

						try
						{
							ret = gHttpDLAdapter.tpSendData(null, 0, SYNCMLDL);
						}
						catch (SocketTimeoutException e)
						{
							tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
							netTimerSend.endTimer();
							ret = TP_RET_SEND_FAIL;
						}
						if (ret == TP_RET_OK)
						{
							tsDmMsg.taskSendMessage(TASK_MSG_DL_SYNCML_CONTINUE, null, null);
						}
						else if (ret == TP_RET_CONNECTION_FAIL)
						{
							tsDmMsg.taskSendMessage(TASK_MSG_DL_SYNCML_CONNECTFAIL, null, null);
						}
						else
						// SEND_FAIL
						{
							tsDmMsg.taskSendMessage(TASK_MSG_DL_SYNCML_SENDFAIL, null, null);
						}
						break;
					}
					case DM_FUMO_STATE_DOWNLOAD_DESCRIPTOR:
						dlAgntHdlrDownloadStart();
						break;

					case DM_FUMO_STATE_DOWNLOAD_IN_PROGRESS:
					case DM_FUMO_STATE_DOWNLOAD_COMPLETE:
					case TASK_MSG_DL_USER_SUSPEND:
						rc = dlAgntHdlrDownloadTakeOver();
						break;

					case DM_FUMO_STATE_DOWNLOAD_IN_CANCEL:
						dlAgentUserCancel();
						break;

					case DM_FUMO_STATE_DOWNLOAD_FAILED:
						dlAgentDownloadFailed();
						break;

					case DM_FUMO_STATE_READY_TO_UPDATE:
						rc = gHttpDLAdapter.tpAbort(nAppID);
						if (rc >= SDM_RET_OK)
						{
							gHttpDLAdapter.tpClose(nAppID);
							gHttpDLAdapter.tpCloseNetWork(nAppID);
						}
						tsMsgEvent.SetMsgEvent(null, DL_EVENT_UI_UPDATE_START);
						rc = TP_RET_OK;
						break;

					default:
						break;
				}
				break;
			}
			case TASK_MSG_DL_SYNCML_CONTINUE:
			{
				tsLib.debugPrint(DEBUG_DL, "MSG_DL_SYNCML_CONTINUE");
				switch (nDLStatus)
				{
					case DM_FUMO_STATE_IDLE_START:
						tsLib.debugPrint(DEBUG_DL, "TASK_MSG_DL_SYNCML_CONTINUE  & DM_FUMO_STATE_IDLE_START");
						dlAgntHdlrDD(pBuffer);
						// Temp Code
						break;

					case DM_FUMO_STATE_DOWNLOAD_IN_PROGRESS:
						tsLib.debugPrint(DEBUG_DL, "DM_FUMO_STATE_DOWNLOAD_IN_PROGRESS");
						dlAgntHdlrDownloadProgress(pBuffer);
						nDLStatus = tsdmDB.dmdbGetFUMOStatus();
						if(nDLStatus == DM_FUMO_STATE_SUSPEND)
						{
							tsLib.debugPrint(DEBUG_DL, "DM_FUMO_STATE_SUSPEND");
							tsService.tsDownloadFail(2);
							tsService.downloadFileFailCause = "download suspend error";
							dmFotaEntity.downloadFileFail();
  					    }
						break;

					case DM_FUMO_STATE_DOWNLOAD_COMPLETE:
						tsLib.debugPrint(DEBUG_DL, "DM_FUMO_STATE_DOWNLOAD_COMPLETE");
						dlAgntHdlrDownloadComplete(pBuffer);
						break;

					case DM_FUMO_STATE_DOWNLOAD_IN_CANCEL:
						tsLib.debugPrint(DEBUG_DL, "DM_FUMO_STATE_DOWNLOAD_IN_CANCEL");
						dlAgntHdlrDownloadComplete(pBuffer);
						break;

					case DM_FUMO_STATE_DOWNLOAD_FAILED:
						tsLib.debugPrint(DEBUG_DL, "DM_FUMO_STATE_DOWNLOAD_IN_FAIL");
						dlAgntHdlrDownloadComplete(pBuffer);
						break;

					default:
						break;
				}
				break;
			}

			case TASK_MSG_DL_USER_CANCEL_DOWNLOAD:
				tsLib.debugPrint(DEBUG_DL, "TASK_MSG_DL_USER_CANCEL_DOWNLOAD");
				tsdmDB.dmdbSetFUMOResultCode(DL_USER_CANCELED_DOWNLOAD);
				tsdmDB.dmdbSetFUMOStatus(DM_FUMO_STATE_DOWNLOAD_IN_CANCEL);

				rc = gHttpDLAdapter.tpAbort(nAppID);
				if (rc >= TP_RET_OK)
				{
					gHttpDLAdapter.tpClose(nAppID);
					gHttpDLAdapter.tpCloseNetWork(nAppID);
				}
				rc = TP_RET_OK;


				tsDmMsg.taskSendMessage(TASK_MSG_DL_SYNCML_CONNECT, null, null);
				break;
				
			case TASK_MSG_DL_DOWNLOAD_FILE_ERROR:
				tsLib.debugPrint(DEBUG_DL, "TASK_MSG_DL_DOWNLOAD_FILE_ERROR");
				tsdmDB.dmdbSetFUMOResultCode(DL_GENERIC_DOWNLOAD_FILE_ERROR);
				tsdmDB.dmdbSetFUMOStatus(DM_FUMO_STATE_DOWNLOAD_FAILED);
				
				rc = gHttpDLAdapter.tpAbort(nAppID);
				if (rc >= TP_RET_OK)
				{
					gHttpDLAdapter.tpClose(nAppID);
					gHttpDLAdapter.tpCloseNetWork(nAppID);
				}
				rc = TP_RET_OK;
				
				tsDmMsg.taskSendMessage(TASK_MSG_DL_SYNCML_CONNECT, null, null);
				break;
				
			case TASK_MSG_DL_USER_SUSPEND:
				tsLib.debugPrint(DEBUG_DL, "TASK_MSG_DL_USER_SUSPEND");

				rc = gHttpDLAdapter.tpAbort(nAppID);
				if (rc >= TP_RET_OK)
				{
					gHttpDLAdapter.tpClose(nAppID);
					gHttpDLAdapter.tpCloseNetWork(nAppID);
				}
				rc = TP_RET_OK;
				break;

			default:
				break;
		}

		dlAgentInitBuffer();
		return rc;
	}
}
