package com.tsdm.agent;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.tsdm.adapt.tsLib;
import com.tsdm.core.data.constants.DmDevInfoConst;
import com.tsdm.core.data.constants.DmUiEvent;
import com.tsdm.core.data.constants.FumoConst;
import com.tsdm.db.tsdmDB;
import com.tsdm.tsService;
import com.tsdm.adapt.tsDmMsg.MsgItem;

public class dmUITask implements Runnable
{
	public static Handler	UI_TaskHandler;

	public dmUITask()
	{
		new Thread(this).start();
	}

	public void run()
	{
		Looper.prepare();

		UI_TaskHandler = new Handler()
		{
			public void handleMessage(Message msg)
			{
				dmUIEvent(msg);
			}
		};

		Looper.loop();
	}

	public boolean dmUIEvent(Message msg)
	{
		MsgItem msgItem = null;

		if ((msg.obj == null))
			return true;

		msgItem = (MsgItem) msg.obj;

		switch (msgItem.type)
		{
			case DmUiEvent.DM_EVENT_UI_NOT_INIT:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_UI, "DM_EVENT_UI_NOT_INIT");
				break;

			case DmUiEvent.DM_EVENT_UI_FINISH:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_UI, "DM_EVENT_UI_FINISH");
				break;

			case DmUiEvent.DM_EVENT_UI_ABORT_BYUSER:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_UI, "DM_EVENT_UI_ABORT_BYUSER");
				break;

			case DmUiEvent.DM_EVENT_UI_NETWORK_ERR:
			case DmUiEvent.DM_EVENT_UI_SYNC_ERROR:
			case DmUiEvent.DM_EVENT_UI_HTTP_INTERNAL_ERROR:
			case DmUiEvent.DM_EVENT_UI_SEND_FAIL:
			case DmUiEvent.DM_EVENT_UI_RECV_FAIL:
			case DmUiEvent.DM_EVENT_UI_SERVER_CONNECT_FAIL:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_UI, "network fail next power on retry");
				tsLib.debugPrint(DmDevInfoConst.DEBUG_UI, String.valueOf(msgItem.type));
				int nStatus = tsdmDB.dmdbGetFUMOStatus();
				if( nStatus != FumoConst.DM_FUMO_STATE_UPDATE_SUCCESSFUL_NODATA && nStatus != FumoConst.DM_FUMO_STATE_DOWNLOAD_FAILED_REPORTING) {
					if (nStatus == FumoConst.DM_FUMO_STATE_DOWNLOAD_IN_PROGRESS) {
						tsLib.debugPrintException(DmDevInfoConst.DEBUG_UI, "network fail occurred while progress download");
						tsService.downloadFileFailCause = "download network error";
						dmFotaEntity.downloadFileFail();
						tsService.tsDownloadFail(2);
					} else {
						dlAgent.dlAgentSetClientInitFlag(DmDevInfoConst.DM_NONE_INIT);
						dmAgent.dmAgentSetUserInitiatedStatus(false);
						dmAgent.dmAgentSetServerInitiatedStatus(false);
					}
				}
				break;

			case DmUiEvent.DL_EVENT_UI_DOWNLOAD_YES_NO:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_UI, "DL_EVENT_UI_DOWNLOAD_YES_NO");
				tsService.tsDownloadRequest();
				break;

			case DmUiEvent.DL_EVNET_UI_RESUME_DOWNLOAD:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_UI, "DL_EVNET_UI_RESUME_DOWNLOAD");
				dmCommonEntity.startSession();
				break;

			case DmUiEvent.DL_EVENT_UI_DOWNLOAD_IN_COMPLETE:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_UI, "DL_EVENT_UI_DOWNLOAD_IN_COMPLETE");
				tsService.tsDownloadComplete();
				break;

			case DmUiEvent.DL_EVENT_UI_DOWNLOAD_FAILED:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_UI, "DL_EVENT_UI_DOWNLOAD_FAILED");
				dlAgent.dlAgentSetClientInitFlag(DmDevInfoConst.DM_NONE_INIT);
				dmAgent.dmAgentSetUserInitiatedStatus(false);
				break;

			case DmUiEvent.DL_EVENT_UI_ALERT_GARAGE:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_DB, " Alert garage: ");
				tsService.tsUpdateExtra();
				break;

			case DmUiEvent.DM_EVENT_UI_DOWNLOAD_FAIL_RETRY_CONFIRM:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_UI, "DM_EVENT_UI_DOWNLOAD_FAIL_RETRY_CONFIRM");
				tsService.tsConnectionPool();
				break;
			case DmUiEvent.DM_EVENT_UI_DOWNLOAD_FAILED_NETWORK_DISCONNECTED:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_UI, "DM_EVENT_UI_DOWNLOAD_FAILED_NETWORK_DISCONNECTED");
				break;
			case DmUiEvent.EVENT_UI_SYNC_START:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_UI, "EVENT_UI_SYNC_START");
				break;
			case DmUiEvent.EVENT_UI_SERVER_CONNECT:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_UI, "EVENT_UI_SERVER_CONNECT");
				break;
			case DmUiEvent.DM_EVENT_UI_UIC_REQUEST:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_UI, "DM_EVENT_UI_UIC_REQUEST");
				break;
			case DmUiEvent.DM_EVENT_UI_DOWNLOAD_START_CONFIRM:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_UI, "DM_EVENT_UI_DOWNLOAD_START_CONFIRM");
				break;
			case DmUiEvent.DM_EVENT_UI_NOTI_INFORM:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_UI, "DM_EVENT_UI_NOTI_INFORM");
				break;
			case DmUiEvent.DM_EVENT_UI_WIFI_DISCONNECTED:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_UI, "DM_EVENT_UI_WIFI_DISCONNECTED");
				break;
			case DmUiEvent.DL_EVENT_UI_DOWNLOAD_OPEN_COMMUNICATION:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_UI, "DL_EVENT_UI_DOWNLOAD_OPEN_COMMUNICATION");
				break;
			case DmUiEvent.DL_EVENT_UI_DM_START_CONFIRM:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_UI, "DL_EVENT_UI_DM_START_CONFIRM");
				break;
			case DmUiEvent.DL_EVENT_UI_DOWNLOAD_IN_PROGRESS:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_UI,"DL_EVENT_UI_DOWNLOAD_IN_PROGRESS");
				break;
			case DmUiEvent.DL_EVENT_UI_DRAW_DOWNLOAD_PERCENTAGE:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_UI,"DL_EVENT_UI_DRAW_DOWNLOAD_PERCENTAGE");
				break;
			case DmUiEvent.DL_EVENT_UI_DOWNLOAD_COMPLETE_SUSPEND:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_UI, "DL_EVENT_UI_DOWNLOAD_COMPLETE_SUSPEND");
				break;
			case DmUiEvent.DL_EVNET_UI_UPDATE_SUCCESS:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_UI, "DL_EVNET_UI_UPDATE_SUCCESS");
				break;
			case DmUiEvent.DL_EVENT_UI_UPDATE_FAIL:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_UI, "DL_EVENT_UI_UPDATE_FAIL");
				break;

			case DmUiEvent.DL_EVENT_UI_DOWNLOAD_FAILED_REPORTING:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_UI, "DL_EVENT_UI_DOWNLOAD_FAILED_REPORTING");
				break;
			case DmUiEvent.DL_EVENT_UI_USER_CANCEL_REPORTING:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_UI, "DL_EVENT_UI_USER_CANCEL_REPORTING");
				break;
			case DmUiEvent.DL_EVENT_UI_DOWNLOAD_IN_CANCEL:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_UI, "DL_EVENT_UI_DOWNLOAD_IN_CANCEL");
				break;
			case DmUiEvent.DL_EVENT_UI_UPDATE_PLEASE_WAIT:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_UI, "DL_EVENT_UI_UPDATE_PLEASE_WAIT");
				break;
			case DmUiEvent.DL_EVENT_UI_MEMORY_FULL:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_UI, "DL_EVENT_UI_MEMORY_FULL");
				break;
			case DmUiEvent.DL_EVENT_UI_UPDATE_CONFIRM:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_UI, "DL_EVENT_UI_UPDATE_CONFIRM");
				break;
			case DmUiEvent.DL_EVENT_UI_POSTPONE:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_UI, "DL_EVENT_UI_POSTPONE");
				break;
			case DmUiEvent.DL_EVENT_UI_UPDATE_START:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_UI, "DL_EVENT_UI_UPDATE_START");
				break;
			case DmUiEvent.DL_EVENT_UI_UPDATE_PRE_START:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_UI, "DL_EVENT_UI_UPDATE_PRE_START");
				break;
			case DmUiEvent.DL_EVENT_UI_DOWNLOAD_COMPLETE_LOW_BATTARY:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_UI, "DL_EVENT_UI_DOWNLOAD_COMPLETE_LOW_BATTARY");
				break;
			case DmUiEvent.DL_EVENT_UI_PHONE_REBOOT:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_UI, "DL_EVENT_UI_PHONE_REBOOT");
				break;
			case DmUiEvent.DM_UI_BOOTSTRAP_INSTALL_RETRY:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_UI, "DM_UI_BOOTSTRAP_INSTALL_RETRY");
				break;
			case DmUiEvent.DM_UI_BOOTSTRAP_INSTALL_SUCCESS:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_UI, "DM_UI_BOOTSTRAP_INSTALL_SUCCESS");
				break;
			case DmUiEvent.DM_UI_BOOTSTRAP_INSTALL:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_UI, "DM_UI_BOOTSTRAP_INSTALL");
				break;
			case DmUiEvent.DM_UI_BOOTSTRAP_INSTALL_FAIL:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_UI, "DM_UI_BOOTSTRAP_INSTALL_FAIL");
				break;
			case DmUiEvent.DM_EVENT_UI_NOTI_NOT_SPECIFIED:
			case DmUiEvent.DM_EVENT_UI_NOTI_BACKGROUND:
			case DmUiEvent.DM_EVENT_UI_NOTI_INFORMATIVE:
			case DmUiEvent.DM_EVENT_UI_NOTI_INTERACTIVE:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_UI, String.valueOf(msgItem.type));
				break;
			case DmUiEvent.DM_EVENT_UI_IDLE_STATE:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_UI, "DM_EVENT_UI_IDLE_STATE");
				break;
			default:
				break;
		}

		msg = null;
		return false;
	}
}
