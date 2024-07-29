package com.tsdm.agent;

import com.tsdm.adapt.tsLib;
import com.tsdm.tsService;
import com.tsdm.db.tsDefineDB;
import com.tsdm.db.tsdmDB;
import com.tsdm.db.tsdmDBadapter;
import com.tsdm.adapt.tsDefineIdle;
import com.tsdm.adapt.tsMsgEvent;
import com.tsdm.adapt.tsDmMsg;

public class dmInitadapter implements dmDefineDevInfo, tsDefineIdle, dmDefineMsg, dmDefineUIEvent, tsDefineDB
{
	public static int dmInitAdpCheckNetworkReady(int nAppId)
	{
		int nNetworkStatus = NETWORK_SERVICE_NONE;
		boolean bResult = false;
		int nAgentStatus;

		nAgentStatus = tsdmDB.dmdbGetFUMOStatus();
		if (nAgentStatus == DM_FUMO_STATE_DOWNLOAD_COMPLETE || nAgentStatus == DM_FUMO_STATE_READY_TO_UPDATE || nAgentStatus == DM_FUMO_STATE_POSTPONE_TO_UPDATE || nAgentStatus == DM_FUMO_STATE_POSTPONE_TO_DOWNLOAD)
		{
			int nFileId = 0;
			nFileId = tsdmDB.dmdbGetFileIdFirmwareData();
			if (tsdmDB.dbAdpFileExists(null, nFileId) == SDM_RET_FAILED)
			{
				tsLib.debugPrintException(DEBUG_EXCEPTION, "File Not Exist");
				tsdmDB.dmdbSetFUMOStatus(DM_FUMO_STATE_NONE);
			}
			else
			{
				tsLib.debugPrintException(DEBUG_EXCEPTION, "Already Download");
				return NETWORK_STATE_ALREADY_DOWNLOAD;
			}
		}

		if (dmAgent.dmAgentGetSyncMode() != DM_SYNC_NONE)
		{
			tsLib.debugPrint(DEBUG_DM, "syncml user network");
			return NETWORK_STATE_SYNCML_USE;

		}

		if(!tsService.isNetworkConnect())
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, "Network service not ready");
			return NETWORK_STATE_NOT_READY;
		}
		else
		{
			tsLib.debugPrint(DEBUG_DM, "Network service ready");
		}

		switch (nAppId)
		{
			case SYNCMLDM:
			case SYNCMLDL:
				break;

			default:
				break;
		}

		if (bResult)
		{
			tsLib.debugPrint(DEBUG_DM, "OTHER APPLICATION USE NETWORK");
			return NETWORK_STATE_APPLICATION_USE;
		}
		else
		{
		return NETWORK_STATE_NOT_USE;
		}
	}

	public static void dmInitAdpCheckDownloadResume()
	{
		tsMsgEvent.SetMsgEvent(null, DL_EVNET_UI_RESUME_DOWNLOAD);
	}

	public static int dmGetSwUpdateState()
	{
		int nResult = tsdmDBadapter.BPE_READFAIL_ERROR_CODE;

		nResult = tsdmDBadapter.dmDBAdpgetFlag();
		tsLib.debugPrint(DEBUG_DM, " getFlag: " + nResult);

		nResult = tsdmDBadapter.BPE_READFAIL_ERROR_CODE;
		return nResult;
	}

	public static void dmInitAdpUpdateResultReport()
	{
		int nResult = 0;

		nResult = dmGetSwUpdateState();
		tsLib.debugPrint(DEBUG_DM, "nSuccess [" + nResult + "]");

		if (nResult == tsdmDBadapter.BPE_SUCCESS)
		{
			tsLib.debugPrint(DEBUG_DM, "DL_GENERIC_SUCCESSFUL_UPDATE");
			tsdmDB.dmdbSetFUMOResultCode(DL_GENERIC_SUCCESSFUL_UPDATE);
			tsdmDB.dmdbSetFUMOStatus(DM_FUMO_STATE_UPDATE_SUCCESSFUL_NODATA);

			tsMsgEvent.SetMsgEvent(null, DL_EVNET_UI_UPDATE_SUCCESS);
			
			Long curTime = System.currentTimeMillis();
			tsdmDB.dmdbSetCurrCheckTime(curTime);
			String sCurTime = new String (curTime.toString());
			byte[] cur = sCurTime.getBytes();
			dmCommonEntity.fileWrite(tsdmDB.DM_FS_FFS_DIRECTORY, "currentTime.dat", cur);
		}
		else
		{
			tsLib.debugPrint(DEBUG_DM, "DL_GENERIC_UPDATE_FAILED");
			tsdmDB.dmdbSetFUMOResultCode(DL_GENERIC_UPDATE_FAILED);
			tsdmDB.dmdbSetFUMOStatus(DM_FUMO_STATE_UPDATE_FAILED_NODATA);

			tsMsgEvent.SetMsgEvent(null, DL_EVENT_UI_UPDATE_FAIL);
		}
		tsdmDB.dmdbSetDmAgentType(SYNCML_DM_AGENT_FUMO);
	}

	public static boolean dmInitAdpEXTInit()
	{
		int nStatus = 0;
		int nFileId = 0;

		nStatus = tsdmDB.dmdbGetFUMOStatus();

		tsLib.debugPrint(DEBUG_DM, "nStatus [" + nStatus + "]");

		if (nStatus == DM_FUMO_STATE_UPDATE_IN_PROGRESS
			|| nStatus == DM_FUMO_STATE_UPDATE_SUCCESSFUL_HAVEDATA
			|| nStatus == DM_FUMO_STATE_UPDATE_SUCCESSFUL_NODATA
			|| nStatus == DM_FUMO_STATE_UPDATE_FAILED_NODATA
			|| nStatus == DM_FUMO_STATE_UPDATE_FAILED_HAVEDATA)
		{
			tsLib.debugPrint(DEBUG_DM, "dmdbGetFUMOResultCode" +tsdmDB.dmdbGetFUMOResultCode());
			tsdmDB.dmdbSetDmAgentType(SYNCML_DM_AGENT_FUMO);
			tsDmMsg.taskSendMessage(TASK_MSG_DM_SYNCML_START, null, null);
			//dmInitAdpUpdateResultReport();
		}
		else if (nStatus == DM_FUMO_STATE_DOWNLOAD_IN_PROGRESS || nStatus == DM_FUMO_STATE_DOWNLOAD_COMPLETE || nStatus == DM_FUMO_STATE_IDLE_START)
		{
			tsLib.debugPrint(DEBUG_DM, "FUMO_STATE_DOWNLOAD_IN_PROGRESS");
			dmInitAdpCheckDownloadResume();
		}
		else if (nStatus == DM_FUMO_STATE_DOWNLOAD_DESCRIPTOR || nStatus == DM_FUMO_STATE_SUSPEND)
		{
			tsLib.debugPrint(DEBUG_DM, "DM_FUMO_STATE_DOWNLOAD_DESCRIPTOR");
			//tsMsgEvent.SetMsgEvent(null, DL_EVENT_UI_DOWNLOAD_YES_NO);
			//tsdmDB.dmdbSetFUMOStatus(DM_FUMO_STATE_NONE);
		}
		else if (nStatus == DM_FUMO_STATE_DOWNLOAD_FAILED_REPORTING)
		{
			tsLib.debugPrint(DEBUG_DM, "DM_FUMO_STATE_DOWNLOAD_FAILED_REPORTING");
			//tsMsgEvent.SetMsgEvent(null, DL_EVENT_UI_DOWNLOAD_FAILED_REPORTING);
			//tsDmMsg.taskSendMessage(TASK_MSG_DM_SYNCML_CONNECT, null, null);
			//tsdmDB.dmdbSetFUMOStatus(DM_FUMO_STATE_NONE);
		}
		else if (nStatus == DM_FUMO_STATE_USER_CANCEL_REPORTING)
		{
			tsLib.debugPrint(DEBUG_DM, "DM_FUMO_STATE_USER_CANCEL_REPORTING");
			//tsMsgEvent.SetMsgEvent(null, DL_EVENT_UI_USER_CANCEL_REPORTING);
			//tsDmMsg.taskSendMessage(TASK_MSG_DM_SYNCML_CONNECT, null, null);
			//tsdmDB.dmdbSetFUMOStatus(DM_FUMO_STATE_NONE);
		}
		else if (nStatus == DM_FUMO_STATE_DOWNLOAD_FAILED)
		{
			tsLib.debugPrint(DEBUG_DM, "DM_FUMO_STATE_DOWNLOAD_FAILED");
			//tsDmMsg.taskSendMessage(TASK_MSG_DM_SYNCML_CONNECT, null, null);
			tsdmDB.dmdbSetFUMOStatus(DM_FUMO_STATE_NONE);
		}
		else if (nStatus == DM_FUMO_STATE_DOWNLOAD_IN_CANCEL)
		{
			tsLib.debugPrint(DEBUG_DM, "DM_FUMO_STATE_DOWNLOAD_IN_CANCEL");
			//tsMsgEvent.SetMsgEvent(null, DL_EVENT_UI_DOWNLOAD_IN_CANCEL);
			//tsDmMsg.taskSendMessage(TASK_MSG_DM_SYNCML_CONNECT, null, null);
			//tsdmDB.dmdbSetFUMOStatus(DM_FUMO_STATE_NONE);
		}
		else if (nStatus == DM_FUMO_STATE_READY_TO_UPDATE)
		{
			tsLib.debugPrint(DEBUG_DM, "DM_FUMO_STATE_READY_TO_UPDATE");
			tsService.tsDownloadComplete();
		}
		else
		{
			int nAgentType = SYNCML_DM_AGENT_DM;
			tsMsgEvent.SetMsgEvent(null, DM_EVENT_UI_IDLE_STATE);
		}
		return true;
	}

}
