package com.tsdm.agent;

import com.tsdm.db.tsDefineDB;
import com.tsdm.db.tsdmDB;
import com.tsdm.db.tsdmDBadapter;
import com.tsdm.adapt.tsDefineIdle;
import com.tsdm.adapt.tsMsgEvent;
import com.tsdm.adapt.tsLib;
import com.tsdm.adapt.tsDmMsg;

public class dlFota implements dmDefineDevInfo, dmDefineMsg, dmDefineUIEvent, tsDefineIdle, tsDefineDB
{
	static boolean							bDownloadStates				= false;
	static boolean							gRetryPopup					= false;
	static boolean							bDrawingPercentageStates	= false;	

	public static boolean dlFotaDownUserInteractAction()
	{
		int nRet = 0;
		boolean bRes = true;

		nRet = dlAgentHandler.dlAgntHdlrCheckDeltaPkgSize();

		if (nRet == DL_OVER_OBJECT_SIZE) // 1 max FirmwareMaxObjectOver
		{
			tsLib.debugPrint(DEBUG_DM, "Firmware size BIG");
			tsMsgEvent.SetMsgEvent(null, DL_EVENT_UI_DELTA_OVER_SIZE);
			bRes = false;
		}
		else if (nRet == DL_MEMORY_INSUFFICIENT)// 2 ffs INSUFFICIENT
		{
			tsLib.debugPrint(DEBUG_DM, "FFS memory Insufficient");
			tsMsgEvent.SetMsgEvent(null, DL_EVENT_UI_MEMORY_FULL);
			bRes = false;
		}
		else
		{
;
		}

		return bRes;
	}
	
	public static int dlFotaSelectDeltaIndex()
	{
		int nRet = TS_FS_OK;
		int nSelectIndex = DELTA_INTERIOR_MEMORY;
		int nObjectSize = tsdmDB.dmdbGetObjectSizeFUMO();

		if(_SYNCML_TS_DM_DELTA_INTERIOR_MEMORY_STORAGE_)
		{
			nRet = tsdmDBadapter.FUMOMultiMemoryFreeSizeCheck(nObjectSize * 2, DELTA_INTERIOR_MEMORY);
			if(nRet == TS_FS_OK)
			{
				nSelectIndex =  DELTA_INTERIOR_MEMORY;
			}
			else
			{
				nRet = tsdmDBadapter.FUMOMultiMemoryFreeSizeCheck(nObjectSize * 2, DELTA_EXTERNAL_MEMORY);
				if (nRet == TS_FS_OK)
				{
					nSelectIndex = DELTA_EXTERNAL_MEMORY;
				}
				else
				{
					nSelectIndex = DELTA_INTERIOR_MEMORY;
				}
			}
		}
		else if(_SYNCML_TS_DM_DELTA_MULTI_MEMORY_STORAGE_)
		{
			nRet = tsdmDBadapter.FUMOMultiMemoryFreeSizeCheck(nObjectSize * 2, DELTA_INTERIOR_MEMORY);
			if(nRet == TS_FS_OK)
			{
				nSelectIndex =  DELTA_INTERIOR_MEMORY;
			}
			else
			{
				nRet = tsdmDBadapter.FUMOMultiMemoryFreeSizeCheck(nObjectSize * 2, DELTA_EXTERNAL_MEMORY);
				if (nRet == TS_FS_OK)
				{
					nSelectIndex = DELTA_EXTERNAL_MEMORY;
				}
				else
				{
					nSelectIndex =  DELTA_EXTERNAL_SD_MEMORY;
				}
			}
		}
		return nSelectIndex;
	}
	
	public static void dlDownloadMemoryCheck()
	{
		if (_SYNCML_TS_DM_DELTA_INTERIOR_MEMORY_STORAGE_)
		{
			int nDeltaFileIndex = dlFotaSelectDeltaIndex();
			tsdmDB.dmdbSetDeltaFileSaveIndex(nDeltaFileIndex);
			if (dlFota.dlFotaDownUserInteractAction())
			{
				tsDmMsg.taskSendMessage(TASK_MSG_DL_SYNCML_CONNECT, null, null);
			}
		}
		else if (_SYNCML_TS_DM_DELTA_MULTI_MEMORY_STORAGE_)
		{
			int nDeltaFileIndex = dlFotaSelectDeltaIndex();
			tsdmDB.dmdbSetDeltaFileSaveIndex(nDeltaFileIndex);
			if(nDeltaFileIndex == DELTA_EXTERNAL_SD_MEMORY)
			{
				if(!dmDevinfoAdapter.checkExternalSdMemoryAvailable())
				{
						tsDmMsg.taskSendMessage(TASK_MSG_DL_USER_CANCEL_DOWNLOAD, null, null);
						dmAgent.dmAgentSetUserInitiatedStatus(false);
				}
				else
				{
					if(dlFota.dlFotaDownUserInteractAction())
					{
						tsDmMsg.taskSendMessage(TASK_MSG_DL_SYNCML_CONNECT, null, null);
					}
				}
			}
			else
			{
				if (dlFota.dlFotaDownUserInteractAction())
				{
					tsDmMsg.taskSendMessage(TASK_MSG_DL_SYNCML_CONNECT, null, null);
				}
			}
		}
		else if(_SYNCML_TS_DM_DELTA_EXTERNAL_STORAGE_)
		{
			if(!dmDevinfoAdapter.checkExternalMemoryAvailable())
			{
					tsDmMsg.taskSendMessage(TASK_MSG_DL_USER_CANCEL_DOWNLOAD, null, null);
					dmAgent.dmAgentSetUserInitiatedStatus(false);
			}
			else
			{
				if(dlFota.dlFotaDownUserInteractAction())
				{
					tsDmMsg.taskSendMessage(TASK_MSG_DL_SYNCML_CONNECT, null, null);
				}
			}
		}
		else
		{
			if(dlFota.dlFotaDownUserInteractAction())
			{
				tsDmMsg.taskSendMessage(TASK_MSG_DL_SYNCML_CONNECT, null, null);
			}
		}
	}
	
}
