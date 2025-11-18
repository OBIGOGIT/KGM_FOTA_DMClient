package com.tsdm.agent;

import com.tsdm.core.data.constants.DmDevInfoConst;
import com.tsdm.core.data.constants.DmTaskMsg;
import com.tsdm.core.data.constants.DmUiEvent;
import com.tsdm.core.data.constants.FumoConst;
import com.tsdm.db.tsDefineDB;
import com.tsdm.db.tsdmDB;
import com.tsdm.db.tsdmDBadapter;
import com.tsdm.adapt.tsMsgEvent;
import com.tsdm.adapt.tsLib;
import com.tsdm.adapt.tsDmMsg;

public class dlFota implements tsDefineDB
{
	static boolean							bDownloadStates				= false;
	static boolean							gRetryPopup					= false;
	static boolean							bDrawingPercentageStates	= false;	

	public static boolean dlFotaDownUserInteractAction()
	{
		int nRet = 0;
		boolean bRes = true;

		nRet = dlAgentHandler.dlAgntHdlrCheckDeltaPkgSize();

		if (nRet == FumoConst.DL_OVER_OBJECT_SIZE) // 1 max FirmwareMaxObjectOver
		{
			tsLib.debugPrint(DmDevInfoConst.DEBUG_DM, "Firmware size BIG");
			tsMsgEvent.SetMsgEvent(null, DmUiEvent.DL_EVENT_UI_DELTA_OVER_SIZE);
			bRes = false;
		}
		else if (nRet == FumoConst.DL_MEMORY_INSUFFICIENT)// 2 ffs INSUFFICIENT
		{
			tsLib.debugPrint(DmDevInfoConst.DEBUG_DM, "FFS memory Insufficient");
			tsMsgEvent.SetMsgEvent(null, DmUiEvent.DL_EVENT_UI_MEMORY_FULL);
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
		int nSelectIndex = DmDevInfoConst.DELTA_INTERIOR_MEMORY;
		int nObjectSize = tsdmDB.dmdbGetObjectSizeFUMO();

		if(DmDevInfoConst._SYNCML_TS_DM_DELTA_INTERIOR_MEMORY_STORAGE_)
		{
			nRet = tsdmDBadapter.FUMOMultiMemoryFreeSizeCheck(nObjectSize * 2, DmDevInfoConst.DELTA_INTERIOR_MEMORY);
			if(nRet == TS_FS_OK)
			{
				nSelectIndex =  DmDevInfoConst.DELTA_INTERIOR_MEMORY;
			}
			else
			{
				nRet = tsdmDBadapter.FUMOMultiMemoryFreeSizeCheck(nObjectSize * 2, DmDevInfoConst.DELTA_EXTERNAL_MEMORY);
				if (nRet == TS_FS_OK)
				{
					nSelectIndex = DmDevInfoConst.DELTA_EXTERNAL_MEMORY;
				}
				else
				{
					nSelectIndex = DmDevInfoConst.DELTA_INTERIOR_MEMORY;
				}
			}
		}
		else if(DmDevInfoConst._SYNCML_TS_DM_DELTA_MULTI_MEMORY_STORAGE_)
		{
			nRet = tsdmDBadapter.FUMOMultiMemoryFreeSizeCheck(nObjectSize * 2, DmDevInfoConst.DELTA_INTERIOR_MEMORY);
			if(nRet == TS_FS_OK)
			{
				nSelectIndex =  DmDevInfoConst.DELTA_INTERIOR_MEMORY;
			}
			else
			{
				nRet = tsdmDBadapter.FUMOMultiMemoryFreeSizeCheck(nObjectSize * 2, DmDevInfoConst.DELTA_EXTERNAL_MEMORY);
				if (nRet == TS_FS_OK)
				{
					nSelectIndex = DmDevInfoConst.DELTA_EXTERNAL_MEMORY;
				}
				else
				{
					nSelectIndex =  DmDevInfoConst.DELTA_EXTERNAL_SD_MEMORY;
				}
			}
		}
		return nSelectIndex;
	}
	
	public static void dlDownloadMemoryCheck()
	{
		if (DmDevInfoConst._SYNCML_TS_DM_DELTA_INTERIOR_MEMORY_STORAGE_)
		{
			int nDeltaFileIndex = dlFotaSelectDeltaIndex();
			tsdmDB.dmdbSetDeltaFileSaveIndex(nDeltaFileIndex);
			if (dlFota.dlFotaDownUserInteractAction())
			{
				tsDmMsg.taskSendMessage(DmTaskMsg.TASK_MSG_DL_SYNCML_CONNECT, null, null);
			}
		}
		else if (DmDevInfoConst._SYNCML_TS_DM_DELTA_MULTI_MEMORY_STORAGE_)
		{
			int nDeltaFileIndex = dlFotaSelectDeltaIndex();
			tsdmDB.dmdbSetDeltaFileSaveIndex(nDeltaFileIndex);
			if(nDeltaFileIndex == DmDevInfoConst.DELTA_EXTERNAL_SD_MEMORY)
			{
				if(!dmDevInfoAdapter.checkExternalSdMemoryAvailable())
				{
						tsDmMsg.taskSendMessage(DmTaskMsg.TASK_MSG_DL_USER_CANCEL_DOWNLOAD, null, null);
						dmAgent.dmAgentSetUserInitiatedStatus(false);
				}
				else
				{
					if(dlFota.dlFotaDownUserInteractAction())
					{
						tsDmMsg.taskSendMessage(DmTaskMsg.TASK_MSG_DL_SYNCML_CONNECT, null, null);
					}
				}
			}
			else
			{
				if (dlFota.dlFotaDownUserInteractAction())
				{
					tsDmMsg.taskSendMessage(DmTaskMsg.TASK_MSG_DL_SYNCML_CONNECT, null, null);
				}
			}
		}
		else if(DmDevInfoConst._SYNCML_TS_DM_DELTA_EXTERNAL_STORAGE_)
		{
			if(!dmDevInfoAdapter.checkExternalMemoryAvailable())
			{
					tsDmMsg.taskSendMessage(DmTaskMsg.TASK_MSG_DL_USER_CANCEL_DOWNLOAD, null, null);
					dmAgent.dmAgentSetUserInitiatedStatus(false);
			}
			else
			{
				if(dlFota.dlFotaDownUserInteractAction())
				{
					tsDmMsg.taskSendMessage(DmTaskMsg.TASK_MSG_DL_SYNCML_CONNECT, null, null);
				}
			}
		}
		else
		{
			if(dlFota.dlFotaDownUserInteractAction())
			{
				tsDmMsg.taskSendMessage(DmTaskMsg.TASK_MSG_DL_SYNCML_CONNECT, null, null);
			}
		}
	}
	
}
