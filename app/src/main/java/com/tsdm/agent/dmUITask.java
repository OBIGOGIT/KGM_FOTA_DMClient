package com.tsdm.agent;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.tsdm.adapt.tsLib;
import com.tsdm.tsService;
import com.tsdm.adapt.tsDefineIdle;
import com.tsdm.adapt.tsDmMsg.MsgItem;

public class dmUITask implements Runnable, dmDefineMsg, dmDefineUIEvent, dmDefineDevInfo, tsDefineIdle
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
			case DM_EVENT_UI_NOT_INIT:
				tsLib.debugPrint(DEBUG_UI, "DM_EVENT_UI_NOT_INIT");
				break;

			case DM_EVENT_UI_FINISH:
				tsLib.debugPrint(DEBUG_UI, "DM_EVENT_UI_FINISH");
				break;

			case DM_EVENT_UI_ABORT_BYUSER:
				tsLib.debugPrint(DEBUG_UI, "DM_EVENT_UI_ABORT_BYUSER");
				break;

			case DM_EVENT_UI_NETWORK_ERR:
			case DM_EVENT_UI_SYNC_ERROR:
			case DM_EVENT_UI_HTTP_INTERNAL_ERROR:
			case DM_EVENT_UI_SEND_FAIL:
			case DM_EVENT_UI_RECV_FAIL:
				tsLib.debugPrint(DEBUG_UI, String.valueOf(msgItem.type));
				tsService.tsDownloadFail(2);
				dlAgent.dlAgentSetClientInitFlag(DM_NONE_INIT);
				dmAgent.dmAgentSetUserInitiatedStatus(false);
				dmAgent.dmAgentSetServerInitiatedStatus(false);
				break;

			case DM_EVENT_UI_SERVER_CONNECT_FAIL:
				tsLib.debugPrint(DEBUG_UI, "DM_EVENT_UI_SERVER_CONNECT_FAIL");
				tsService.tsDownloadFail(1);
				dlAgent.dlAgentSetClientInitFlag(DM_NONE_INIT);
				dmAgent.dmAgentSetUserInitiatedStatus(false);
				dmAgent.dmAgentSetServerInitiatedStatus(false);
				break;

			case DL_EVENT_UI_DOWNLOAD_YES_NO:
				tsLib.debugPrint(DEBUG_UI, "DL_EVENT_UI_DOWNLOAD_YES_NO");
				tsService.tsDownloadRequest();
				break;

			case DL_EVNET_UI_RESUME_DOWNLOAD:
				tsLib.debugPrint(DEBUG_UI, "DL_EVNET_UI_RESUME_DOWNLOAD");
				dmCommonEntity.startSession();
				break;

			case DL_EVENT_UI_DOWNLOAD_IN_COMPLETE:
				tsLib.debugPrint(DEBUG_UI, "DL_EVENT_UI_DOWNLOAD_IN_COMPLETE");
				tsService.tsDownloadComplete();
				break;

			case DL_EVENT_UI_DOWNLOAD_FAILED:
				tsLib.debugPrint(DEBUG_UI, "DL_EVENT_UI_DOWNLOAD_FAILED");
				tsService.tsDownloadFail(1);
				dlAgent.dlAgentSetClientInitFlag(DM_NONE_INIT);
				dmAgent.dmAgentSetUserInitiatedStatus(false);
				break;

			case DL_EVENT_UI_ALERT_GARAGE:
				tsLib.debugPrint(DEBUG_DB, " Alert garage: ");
				tsService.tsUpdateExtra();
				break;

			case DM_EVENT_UI_DOWNLOAD_FAIL_RETRY_CONFIRM:
				tsLib.debugPrint(DEBUG_UI, "DM_EVENT_UI_DOWNLOAD_FAIL_RETRY_CONFIRM");
				break;
			case DM_EVENT_UI_DOWNLOAD_FAILED_WIFI_DISCONNECTED:
				tsLib.debugPrint(DEBUG_UI, "DM_EVENT_UI_DOWNLOAD_FAILED_WIFI_DISCONNECTED");
				break;
			case EVENT_UI_SYNC_START:
				tsLib.debugPrint(DEBUG_UI, "EVENT_UI_SYNC_START");
				break;
			case EVENT_UI_SERVER_CONNECT:
				tsLib.debugPrint(DEBUG_UI, "EVENT_UI_SERVER_CONNECT");
				break;
			case DM_EVENT_UI_UIC_REQUEST:
				tsLib.debugPrint(DEBUG_UI, "DM_EVENT_UI_UIC_REQUEST");
				break;
			case DM_EVENT_UI_DOWNLOAD_START_CONFIRM:
				tsLib.debugPrint(DEBUG_UI, "DM_EVENT_UI_DOWNLOAD_START_CONFIRM");
				break;
			case DM_EVENT_UI_NOTI_INFORM:
				tsLib.debugPrint(DEBUG_UI, "DM_EVENT_UI_NOTI_INFORM");
				break;
			case DM_EVENT_UI_WIFI_DISCONNECTED:
				tsLib.debugPrint(DEBUG_UI, "DM_EVENT_UI_WIFI_DISCONNECTED");
				break;
			case DL_EVENT_UI_DOWNLOAD_OPEN_COMMUNICATION:
				tsLib.debugPrint(DEBUG_UI, "DL_EVENT_UI_DOWNLOAD_OPEN_COMMUNICATION");
				break;
			case DL_EVENT_UI_DM_START_CONFIRM:
				tsLib.debugPrint(DEBUG_UI, "DL_EVENT_UI_DM_START_CONFIRM");
				break;
			case DL_EVENT_UI_DOWNLOAD_IN_PROGRESS:
				tsLib.debugPrint(DEBUG_UI,"DL_EVENT_UI_DOWNLOAD_IN_PROGRESS");
				break;
			case DL_EVENT_UI_DRAW_DOWNLOAD_PERCENTAGE:
				tsLib.debugPrint(DEBUG_UI,"DL_EVENT_UI_DRAW_DOWNLOAD_PERCENTAGE");
				break;
			case DL_EVENT_UI_DOWNLOAD_COMPLETE_SUSPEND:
				tsLib.debugPrint(DEBUG_UI, "DL_EVENT_UI_DOWNLOAD_COMPLETE_SUSPEND");
				break;
			case DL_EVNET_UI_UPDATE_SUCCESS:
				tsLib.debugPrint(DEBUG_UI, "DL_EVNET_UI_UPDATE_SUCCESS");
				break;
			case DL_EVENT_UI_UPDATE_FAIL:
				tsLib.debugPrint(DEBUG_UI, "DL_EVENT_UI_UPDATE_FAIL");
				break;

			case DL_EVENT_UI_DOWNLOAD_FAILED_REPORTING:
				tsLib.debugPrint(DEBUG_UI, "DL_EVENT_UI_DOWNLOAD_FAILED_REPORTING");
				break;
			case DL_EVENT_UI_USER_CANCEL_REPORTING:
				tsLib.debugPrint(DEBUG_UI, "DL_EVENT_UI_USER_CANCEL_REPORTING");
				break;
			case DL_EVENT_UI_DOWNLOAD_IN_CANCEL:
				tsLib.debugPrint(DEBUG_UI, "DL_EVENT_UI_DOWNLOAD_IN_CANCEL");
				break;
			case DL_EVENT_UI_UPDATE_PLEASE_WAIT:
				tsLib.debugPrint(DEBUG_UI, "DL_EVENT_UI_UPDATE_PLEASE_WAIT");
				break;
			case DL_EVENT_UI_MEMORY_FULL:
				tsLib.debugPrint(DEBUG_UI, "DL_EVENT_UI_MEMORY_FULL");
				break;
			case DL_EVENT_UI_UPDATE_CONFIRM:
				tsLib.debugPrint(DEBUG_UI, "DL_EVENT_UI_UPDATE_CONFIRM");
				break;
			case DL_EVENT_UI_POSTPONE:
				tsLib.debugPrint(DEBUG_UI, "DL_EVENT_UI_POSTPONE");
				break;
			case DL_EVENT_UI_UPDATE_START:
				tsLib.debugPrint(DEBUG_UI, "DL_EVENT_UI_UPDATE_START");
				break;
			case DL_EVENT_UI_UPDATE_PRE_START:
				tsLib.debugPrint(DEBUG_UI, "DL_EVENT_UI_UPDATE_PRE_START");
				break;
			case DL_EVENT_UI_DOWNLOAD_COMPLETE_LOW_BATTARY:
				tsLib.debugPrint(DEBUG_UI, "DL_EVENT_UI_DOWNLOAD_COMPLETE_LOW_BATTARY");
				break;
			case DL_EVENT_UI_PHONE_REBOOT:
				tsLib.debugPrint(DEBUG_UI, "DL_EVENT_UI_PHONE_REBOOT");
				break;
			case DM_UI_BOOTSTRAP_INSTALL_RETRY:
				tsLib.debugPrint(DEBUG_UI, "DM_UI_BOOTSTRAP_INSTALL_RETRY");
				break;
			case DM_UI_BOOTSTRAP_INSTALL_SUCCESS:
				tsLib.debugPrint(DEBUG_UI, "DM_UI_BOOTSTRAP_INSTALL_SUCCESS");
				break;
			case DM_UI_BOOTSTRAP_INSTALL:
				tsLib.debugPrint(DEBUG_UI, "DM_UI_BOOTSTRAP_INSTALL");
				break;
			case DM_UI_BOOTSTRAP_INSTALL_FAIL:
				tsLib.debugPrint(DEBUG_UI, "DM_UI_BOOTSTRAP_INSTALL_FAIL");
				break;
			case DM_EVENT_UI_NOTI_NOT_SPECIFIED:
			case DM_EVENT_UI_NOTI_BACKGROUND:
			case DM_EVENT_UI_NOTI_INFORMATIVE:
			case DM_EVENT_UI_NOTI_INTERACTIVE:
				tsLib.debugPrint(DEBUG_UI, String.valueOf(msgItem.type));
				break;
			case DM_EVENT_UI_IDLE_STATE:
				tsLib.debugPrint(DEBUG_UI, "DM_EVENT_UI_IDLE_STATE :");
				break;
			default:
				break;
		}

		msg = null;
		return false;
	}
}
