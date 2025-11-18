package com.tsdm.agent;

import com.tsdm.adapt.tsLib;
import com.tsdm.core.data.constants.DmDevInfoConst;
import com.tsdm.core.data.constants.DmTaskMsg;
import com.tsdm.core.data.constants.DmUiEvent;
import com.tsdm.core.data.constants.FumoConst;
import com.tsdm.tsService;
import com.tsdm.db.tsDefineDB;
import com.tsdm.db.tsdmDB;
import com.tsdm.db.tsdmDBadapter;
import com.tsdm.adapt.tsMsgEvent;
import com.tsdm.adapt.tsDmMsg;

public class dmInitadapter implements tsDefineDB
{
	public static int dmInitAdpCheckNetworkReady(int nAppId)
	{
		int nNetworkStatus = DmDevInfoConst.NETWORK_SERVICE_NONE;
		//boolean bResult = false;
		int nAgentStatus;

		nAgentStatus = tsdmDB.dmdbGetFUMOStatus();
		if (nAgentStatus == FumoConst.DM_FUMO_STATE_DOWNLOAD_COMPLETE || nAgentStatus == FumoConst.DM_FUMO_STATE_READY_TO_UPDATE || nAgentStatus == FumoConst.DM_FUMO_STATE_POSTPONE_TO_UPDATE || nAgentStatus == FumoConst.DM_FUMO_STATE_POSTPONE_TO_DOWNLOAD)
		{
			int nFileId = 0;
			nFileId = tsdmDB.dmdbGetFileIdFirmwareData();
			if (tsdmDB.dbAdpFileExists(null, nFileId) == DmDevInfoConst.SDM_RET_FAILED)
			{
				tsLib.debugPrintException(DmDevInfoConst.DEBUG_EXCEPTION, "File Not Exist");
				tsdmDB.dmdbSetFUMOStatus(FumoConst.DM_FUMO_STATE_NONE);
			}
			else
			{
				tsLib.debugPrintException(DmDevInfoConst.DEBUG_EXCEPTION, "Already Download");
				return DmDevInfoConst.NETWORK_STATE_ALREADY_DOWNLOAD;
			}
		}

		if (dmAgent.dmAgentGetSyncMode() != DmDevInfoConst.DM_SYNC_NONE)
		{
			tsLib.debugPrint(DmDevInfoConst.DEBUG_DM, "syncml user network");
			return DmDevInfoConst.NETWORK_STATE_SYNCML_USE;

		}

		if(!tsService.isNetworkConnect())
		{
			tsLib.debugPrintException(DmDevInfoConst.DEBUG_EXCEPTION, "Network service not ready");
			return DmDevInfoConst.NETWORK_STATE_NOT_READY;
		}
		else
		{
			tsLib.debugPrint(DmDevInfoConst.DEBUG_DM, "Network service ready");
		}

		switch (nAppId)
		{
			case DmDevInfoConst.SYNCMLDM:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_DM, "nAppId SYNCMLDM");
				break;
			case DmDevInfoConst.SYNCMLDL:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_DM, "nAppId  SYNCMLDL");
				break;
			default:
				break;
		}

/*		if (bResult)
		{
			tsLib.debugPrint(DmDevInfoConst.DEBUG_DM, "OTHER APPLICATION USE NETWORK");
			return NETWORK_STATE_APPLICATION_USE;
		}
		else
		{
		return NETWORK_STATE_NOT_USE;
		}*/
		return DmDevInfoConst.NETWORK_STATE_NOT_USE;
	}

	public static void dmInitAdpCheckDownloadResume()
	{
		tsMsgEvent.SetMsgEvent(null, DmUiEvent.DL_EVNET_UI_RESUME_DOWNLOAD);
	}

	public static int dmGetSwUpdateState()
	{
		int nResult = tsdmDBadapter.BPE_READFAIL_ERROR_CODE;

		nResult = tsdmDBadapter.dmDBAdpgetFlag();
		tsLib.debugPrint(DmDevInfoConst.DEBUG_DM, " getFlag: " + nResult);

		nResult = tsdmDBadapter.BPE_READFAIL_ERROR_CODE;
		return nResult;
	}

	public static void dmInitAdpUpdateResultReport()
	{
		int nResult = 0;

		nResult = dmGetSwUpdateState();
		tsLib.debugPrint(DmDevInfoConst.DEBUG_DM, "nSuccess [" + nResult + "]");

		if (nResult == tsdmDBadapter.BPE_SUCCESS)
		{
			tsLib.debugPrint(DmDevInfoConst.DEBUG_DM, "DL_GENERIC_SUCCESSFUL_UPDATE");
			tsdmDB.dmdbSetFUMOResultCode(FumoConst.DL_GENERIC_SUCCESSFUL_UPDATE);
			tsdmDB.dmdbSetFUMOStatus(FumoConst.DM_FUMO_STATE_UPDATE_SUCCESSFUL_NODATA);

			tsMsgEvent.SetMsgEvent(null, DmUiEvent.DL_EVNET_UI_UPDATE_SUCCESS);
			
			Long curTime = System.currentTimeMillis();
			tsdmDB.dmdbSetCurrCheckTime(curTime);
			String sCurTime = new String (curTime.toString());
			byte[] cur = sCurTime.getBytes();
			dmCommonEntity.fileWrite(tsdmDB.DM_FS_FFS_DIRECTORY, "currentTime.dat", cur);
		}
		else
		{
			tsLib.debugPrint(DmDevInfoConst.DEBUG_DM, "DL_GENERIC_UPDATE_FAILED");
			tsdmDB.dmdbSetFUMOResultCode(FumoConst.DL_GENERIC_UPDATE_FAILED);
			tsdmDB.dmdbSetFUMOStatus(FumoConst.DM_FUMO_STATE_UPDATE_FAILED_NODATA);

			tsMsgEvent.SetMsgEvent(null, DmUiEvent.DL_EVENT_UI_UPDATE_FAIL);
		}
		tsdmDB.dmdbSetDmAgentType(DmDevInfoConst.SYNCML_DM_AGENT_FUMO);
	}

	public static boolean dmInitAdpEXTInit()
	{
		int nStatus = 0;
		int nFileId = 0;

		nStatus = tsdmDB.dmdbGetFUMOStatus();

		tsLib.debugPrint(DmDevInfoConst.DEBUG_DM, "nStatus [" + nStatus + "]");

		if (nStatus == FumoConst.DM_FUMO_STATE_UPDATE_IN_PROGRESS
			|| nStatus == FumoConst.DM_FUMO_STATE_UPDATE_SUCCESSFUL_HAVEDATA
			|| nStatus == FumoConst.DM_FUMO_STATE_UPDATE_SUCCESSFUL_NODATA
			|| nStatus == FumoConst.DM_FUMO_STATE_UPDATE_FAILED_NODATA
			|| nStatus == FumoConst.DM_FUMO_STATE_UPDATE_FAILED_HAVEDATA)
		{
			tsLib.debugPrint(DmDevInfoConst.DEBUG_DM, "dmdbGetFUMOResultCode " +tsdmDB.dmdbGetFUMOResultCode());
			tsdmDB.dmdbSetDmAgentType(DmDevInfoConst.SYNCML_DM_AGENT_FUMO);
			tsDmMsg.taskSendMessage(DmTaskMsg.TASK_MSG_DM_SYNCML_START, null, null);
			//dmInitAdpUpdateResultReport();
		}
		else if (nStatus == FumoConst.DM_FUMO_STATE_DOWNLOAD_IN_PROGRESS || nStatus == FumoConst.DM_FUMO_STATE_DOWNLOAD_COMPLETE || nStatus == FumoConst.DM_FUMO_STATE_IDLE_START)
		{
			tsLib.debugPrint(DmDevInfoConst.DEBUG_DM, "FUMO_STATE_DOWNLOAD_IN_PROGRESS");
			dmInitAdpCheckDownloadResume();
		}
		else if ( nStatus == FumoConst.DM_FUMO_STATE_SUSPEND)
		{
			tsLib.debugPrint(DmDevInfoConst.DEBUG_DM, "DM_FUMO_STATE_SUSPEND");
			tsService.downloadFileFailCause = "download suspend error";
			dmFotaEntity.downloadFileFail();
		}
		else if (nStatus == FumoConst.DM_FUMO_STATE_DOWNLOAD_DESCRIPTOR )
		{
			tsLib.debugPrint(DmDevInfoConst.DEBUG_DM, "DM_FUMO_STATE_DOWNLOAD_DESCRIPTOR");
			int descriptResumeState=tsService.descriptResumeState();
			if(descriptResumeState == 1){
				tsDmMsg.taskSendMessage(DmTaskMsg.TASK_MSG_DM_SYNCML_INIT, null, null);
				dmFotaEntity.checkDownloadMemory();
			}else if(descriptResumeState == 2){
				dmFotaEntity.cancelDownload();
			}else{
				tsService.tsDownloadRequest();
			}
			//tsMsgEvent.SetMsgEvent(null, DL_EVENT_UI_DOWNLOAD_YES_NO);
		}
		else if (nStatus == FumoConst.DM_FUMO_STATE_DOWNLOAD_FAILED_REPORTING)
		{
			tsLib.debugPrint(DmDevInfoConst.DEBUG_DM, "DM_FUMO_STATE_DOWNLOAD_FAILED_REPORTING");
			tsService.downloadFileFailCause = "download network error";
			dmFotaEntity.downloadFileFail();
		}
		else if (nStatus == FumoConst.DM_FUMO_STATE_USER_CANCEL_REPORTING)
		{
			tsLib.debugPrint(DmDevInfoConst.DEBUG_DM, "DM_FUMO_STATE_USER_CANCEL_REPORTING");
			//tsMsgEvent.SetMsgEvent(null, DL_EVENT_UI_USER_CANCEL_REPORTING);
			//tsDmMsg.taskSendMessage(TASK_MSG_DM_SYNCML_CONNECT, null, null);
			tsdmDB.dmdbSetFUMOStatus(FumoConst.DM_FUMO_STATE_NONE);
		}
		else if (nStatus == FumoConst.DM_FUMO_STATE_DOWNLOAD_FAILED)
		{
			tsLib.debugPrint(DmDevInfoConst.DEBUG_DM, "DM_FUMO_STATE_DOWNLOAD_FAILED");
			//tsDmMsg.taskSendMessage(TASK_MSG_DM_SYNCML_CONNECT, null, null);
			tsdmDB.dmdbSetFUMOStatus(FumoConst.DM_FUMO_STATE_NONE);
		}
		else if (nStatus == FumoConst.DM_FUMO_STATE_DOWNLOAD_IN_CANCEL)
		{
			tsLib.debugPrint(DmDevInfoConst.DEBUG_DM, "DM_FUMO_STATE_DOWNLOAD_IN_CANCEL");
			//tsMsgEvent.SetMsgEvent(null, DL_EVENT_UI_DOWNLOAD_IN_CANCEL);
			//tsDmMsg.taskSendMessage(TASK_MSG_DM_SYNCML_CONNECT, null, null);
			tsdmDB.dmdbSetFUMOStatus(FumoConst.DM_FUMO_STATE_NONE);
		}
		else if (nStatus == FumoConst.DM_FUMO_STATE_READY_TO_UPDATE)
		{
			tsLib.debugPrint(DmDevInfoConst.DEBUG_DM, "DM_FUMO_STATE_READY_TO_UPDATE");
			tsService.tsDownloadComplete();
		}
		else
		{
			int nAgentType = DmDevInfoConst.SYNCML_DM_AGENT_DM;
			tsMsgEvent.SetMsgEvent(null, DmUiEvent.DM_EVENT_UI_IDLE_STATE);
		}
		return true;
	}

}
