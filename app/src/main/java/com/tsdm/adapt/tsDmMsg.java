package com.tsdm.adapt;

import android.os.Message;

import com.tsdm.agent.dmTask;
import com.tsdm.agent.dmUITask;
import com.tsdm.agent.dmDefineDevInfo;

public class tsDmMsg implements dmDefineDevInfo
{
	public MsgItem msgItem;

	private static tsLinkedList SyncMLMsgQueue		= null;
	private static tsLinkedList UIMsgQueue			= null;

	private final static Object		syncMsgQueueObj		= new Object();
	private final static Object		syncUIMsgQueueObj	= new Object();

	public tsDmMsg()
	{
		msgItem = new MsgItem();
	}

	public static class MsgItem
	{
		public int			type;
		public MsgParam param;
	};

	public static class MsgParam
	{
		public Object	param;
		public Object	paramFree;
	};

	public static int status(int type, Object obj)
	{
		return 1;
	}

	public static tsDmMsg getSyncMLMessage()
	{
		tsDmMsg obj = null;

		if (null != SyncMLMsgQueue)
		{
			synchronized (syncMsgQueueObj)
			{
				tsLinkedList.listSetCurrentObj(SyncMLMsgQueue, 0);
				obj = (tsDmMsg) tsLinkedList.listGetNextObj(SyncMLMsgQueue);
				while (obj != null)
				{
					if (obj.msgItem.type >= 0)// == MsgType)
					{
						tsLinkedList.listRemovePreviousObj(SyncMLMsgQueue);
						break;
					}
					obj = (tsDmMsg) tsLinkedList.listGetNextObj(SyncMLMsgQueue);
				}

				if (0 == SyncMLMsgQueue.count)
				{
					tsLinkedList.listFreeLinkedList(SyncMLMsgQueue);
					SyncMLMsgQueue = null;
				}
			}
		}

		if (obj == null)
			return null;

		return obj;
	}

	public static void taskSendMessage(int type, Object param, Object paramFree)
	{
		MsgParam msgParam = null;
		MsgItem msgItem = null;

		if (null != param)
		{
			msgParam = new MsgParam();
			msgParam.param = param;

			msgParam.paramFree = null;
			if (paramFree != null)
			{
				msgParam.paramFree = paramFree;
			}
		}

		synchronized (syncMsgQueueObj)
		{
			msgItem = new MsgItem();
			
			if(dmTask.DM_TaskHandler == null)
			{
				// waiting for DM_TaskHandler create
				for(int i=0;i<5;i++)
				{
					try
					{
						tsLib.debugPrint(DEBUG_DM, "waiting for DM_TaskHandler create");
						//Thread.sleep(500);
						syncMsgQueueObj.wait(500); // Compliant, the current monitor is released.
					}
					catch (InterruptedException e)
					{
						tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
						Thread.currentThread().interrupt();
					}
					
					if(dmTask.DM_TaskHandler != null)
					{
						break;
					}
				}
			}
			
			try
			{
				if (null != msgItem)
				{
					msgItem.type = type;
					msgItem.param = msgParam;

					Message msg = dmTask.DM_TaskHandler.obtainMessage();
					msg.obj = msgItem;
					dmTask.DM_TaskHandler.sendMessage(msg);
				}
				else
				{
					tsLib.debugPrintException(DEBUG_EXCEPTION, "Can't send message");
				}
			}
			catch (Exception e) 
			{
				tsLib.debugPrintException(DEBUG_EXCEPTION, "Can't send message");
			}
		}
	}

	public static void uiSendMessage(int type, Object param, Object paramFree)
	{
		MsgParam msgParam = null;
		MsgItem msgItem = null;

		if (null != param)
		{
			msgParam = new MsgParam();
			msgParam.param = param;

			msgParam.paramFree = null;
			if (paramFree != null)
			{
				msgParam.paramFree = paramFree;
			}
		}

		msgItem = new MsgItem();
		if (null != msgItem)
		{
			msgItem.type = type;
			msgItem.param = msgParam;

			Message msg = dmUITask.UI_TaskHandler.obtainMessage();
			msg.obj = msgItem;
			dmUITask.UI_TaskHandler.sendMessage(msg);
		}
		else
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, "Can't send message");
		}
	}

	public static tsDmParamAbortmsg createAbortMessage(int abortCode, boolean userReq)
	{
		tsDmParamAbortmsg pAbortParam = null;

		pAbortParam = new tsDmParamAbortmsg();
		pAbortParam.abortCode = abortCode;
		pAbortParam.userReq = userReq;
		return pAbortParam;
	}


	public static tsDmParamConnectfailmsg createConnectFailMessage(int id, int FailCode)
	{
		tsDmParamConnectfailmsg pParam = null;

		pParam = new tsDmParamConnectfailmsg();
		pParam.nConnectFailCode = FailCode;
		pParam.appId = id;
		return pParam;
	}
}
