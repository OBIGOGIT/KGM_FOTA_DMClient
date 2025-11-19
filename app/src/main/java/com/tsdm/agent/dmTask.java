package com.tsdm.agent;

import java.net.SocketTimeoutException;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.tsdm.adapt.tsLib;
import com.tsdm.core.data.constants.DmDevInfoConst;
import com.tsdm.core.data.constants.DmTaskMsg;
import com.tsdm.core.data.constants.DmUiEvent;
import com.tsdm.core.data.constants.FumoConst;
import com.tsdm.core.data.constants.UserInteractionCommandConst;
import com.tsdm.net.NetConsts;
import com.tsdm.tsService;
import com.tsdm.db.tsDefineDB;
import com.tsdm.db.tsdmDB;
import com.tsdm.db.tsdmInfo;
import com.tsdm.adapt.tsDmParamAbortmsg;
import com.tsdm.adapt.tsMsgEvent;
import com.tsdm.adapt.tsDmMsg;
import com.tsdm.adapt.tsDmUic;
import com.tsdm.adapt.tsDmUicOption;
import com.tsdm.adapt.tsDmUicResult;
import com.tsdm.adapt.tsDmMsg.MsgItem;
import com.tsdm.net.netHttpAdapter;
import com.tsdm.net.netTimerConnect;

public class dmTask implements Runnable, tsDefineDB
{
	private boolean			mDBInit						= false;
	public static boolean	g_IsSyncTaskInit			= false;
	public static boolean	g_IsDMInitialized			= false;

	public dmAgent agent						= null;
	public dlAgent dlagent						= null;
	public static Handler	DM_TaskHandler;

	public dmTask()
	{
		if (!g_IsSyncTaskInit)
		{
			new Thread(this).start();
		}
	}

	public void run()
	{
		Looper.prepare();

		DM_TaskHandler = new Handler()
		{
			public void handleMessage(Message msg)
			{
				try
				{
					dmTaskHandler(msg);
				}
				catch (InterruptedException e)
				{
					tsLib.debugPrintException(DmDevInfoConst.DEBUG_EXCEPTION, e.toString());
					Thread.currentThread().interrupt();
				}
			}
		};

		Looper.loop();
	}

	private boolean dmTaskDBInit()
	{
		boolean bRtn = false;
		tsLib.debugPrint(DmDevInfoConst.DEBUG_DM, "");
		try
		{
			tsdmDB.dbDMffs_Init();
			bRtn = true;
			mDBInit = true;
		}
		catch (NullPointerException e)
		{
			tsLib.debugPrintException(DmDevInfoConst.DEBUG_EXCEPTION, e.toString());
		}

		return bRtn;
	}

	@SuppressWarnings("static-access")
	public void dmTaskInit()
	{
		if (!g_IsSyncTaskInit)
		{
			tsLib.debugPrint(DmDevInfoConst.DEBUG_DM, "");
			agent = new dmAgent();
			agent.agenthandler = new dmAgentHandler();
			dlagent = new dlAgent();
			dlagent.dlagenthandler = new dlAgentHandler();

			agent.agenthandler.gHttpDMAdapter = agent.gHttpDMAdapter;
			dlagent.dlagenthandler.gHttpDLAdapter = dlagent.gHttpDLAdapter;

			// DB Init
			tsdmDB.dmdbInit();
			//dmTaskDBInit();

			g_IsSyncTaskInit = true;
		}
		else
			tsLib.debugPrint(DmDevInfoConst.DEBUG_DM, "already g_IsSyncTaskInit true");
	}

	
	@SuppressWarnings("static-access")
	public boolean dmTaskHandler(Message msg) throws InterruptedException
	{
		MsgItem msgItem = null;

		if (msg.obj == null)
			return true;

		msgItem = (MsgItem) msg.obj;

		switch (msgItem.type)
		{
			case DmTaskMsg.TASK_MSG_OS_INITIALIZED:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, "TASK_MSG_OS_INITIALIZED");
				netHttpAdapter.setIsConnected(false);
				dmAgent.tpSetRetryCount(NetConsts.TP_RETRY_COUNT_NONE);
				dmAgent.dmAgentSetSyncMode(DmDevInfoConst.DM_SYNC_NONE);
				dmTaskInit();
				break;

			case DmTaskMsg.TASK_MSG_PHONEBOOK_INITIALIZED:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, "TASK_MSG_PHONEBOOK_INITIALIZED");
				break;

			case DmTaskMsg.TASK_MSG_NETWORK_STATUS_UPDATED:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, "TASK_MSG_NETWORK_STATUS_UPDATED");
				break;

			case DmTaskMsg.TASK_MSG_DM_SYNCML_IDLE_STATE:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, "TASK_MSG_DM_SYNCML_IDLE_STATE");
				break;

			case DmTaskMsg.TASK_MSG_DM_SYNCML_INIT:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, "TASK_MSG_DM_SYNCML_INIT");
				if (!tsService.isNetworkConnect())
				{
					tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, " Network Status is not ready. DM Not Initialized");
					tsService.tsNetworkUnready();
					break;
				}

				if (!g_IsDMInitialized)
				{
					boolean bRet = true;

					if (!mDBInit)
						bRet = dmTaskDBInit();

					if (!bRet)
					{
						break;
					}
					
					if (!dmInitadapter.dmInitAdpEXTInit())
					{
						tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, "TASK_MSG_DM_SYNCML_INIT : Not Initialized");
					}
					else {
						tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, "TASK_MSG_DM_SYNCML_INIT : Initialized");
						g_IsDMInitialized = true;
					}

				}

				tsService.dmNetProfileChangeSet(); // profile change
				break;

			case DmTaskMsg.TASK_MSG_DM_SYNCML_CONNECT:
			{
				tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, "TASK_MSG_DM_SYNCML_CONNECT");
				int nMechanism = 0;
				int nFumoStatus = FumoConst.DM_FUMO_STATE_NONE;
				int nAgentType = DmDevInfoConst.SYNCML_DM_AGENT_DM;

				if (g_IsDMInitialized)
				{
					boolean rc = true;

					rc = dlAgent.dlAgentIsStatus();
					tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, "dlAgentIsStatus =" + rc);

					if (rc)
					{
						if (tsdmDB.dmdbGetChangedProtocol())
						{
							tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, "TASK_MSG_DM_SYNCML_CONNECT : Changed Protocol");
						}
						else
						{
							tsdmInfo pNvInfo = null;
							pNvInfo = (tsdmInfo) tsdmDB.dmdbGetProfileInfo(pNvInfo);
							if (pNvInfo == null)
								pNvInfo = new tsdmInfo();
							tsdmDB.dmdbSetServerUrl(pNvInfo.ServerUrl_Org);
							tsdmDB.dmdbSetServerAddress(pNvInfo.ServerIP_Org);
							tsdmDB.dmdbSetServerPort(pNvInfo.ServerPort_Org);
							tsdmDB.dmdbSetServerProtocol(pNvInfo.Protocol_Org);
						}
						agent.tpInit(DmDevInfoConst.SYNCMLDM);
						tsDmMsg.taskSendMessage(DmTaskMsg.TASK_MSG_DM_TCPIP_OPEN, null, null);
					}
					else
					{
						nMechanism = tsdmDB.dmdbGetFUMOUpdateMechanism();
						tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, "nMechanism : " + nMechanism);
						nFumoStatus = tsdmDB.dmdbGetFUMOStatus();
						tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, "nFumoStatus : " + nFumoStatus);

						if (nMechanism == FumoConst.DM_FUMO_MECHANISM_ALTERNATIVE)
						{
							if (nFumoStatus == FumoConst.DM_FUMO_STATE_READY_TO_UPDATE)
							{
								tsMsgEvent.SetMsgEvent(null, DmUiEvent.DL_EVENT_UI_UPDATE_START);
							}
							else
							{
								tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, "DM_FUMO_MECHANISM_ALTERNATIVE TASK_MSG_DM_SYNCML_CONNECT");
								tsDmMsg.taskSendMessage(DmTaskMsg.TASK_MSG_DL_SYNCML_CONNECT, null, null); // dl connect
							}
						}
						else if (nMechanism == FumoConst.DM_FUMO_MECHANISM_ALTERNATIVE_DOWNLOAD)
						{
							if (nFumoStatus == FumoConst.DM_FUMO_STATE_READY_TO_UPDATE)
							{
								tsMsgEvent.SetMsgEvent(null, DmUiEvent.DL_EVENT_UI_UPDATE_START);
							}
							else if (nFumoStatus == FumoConst.DM_FUMO_STATE_DOWNLOAD_COMPLETE)
							{
								agent.tpInit(DmDevInfoConst.SYNCMLDM);
								tsDmMsg.taskSendMessage(DmTaskMsg.TASK_MSG_DM_TCPIP_OPEN, null, null);
							}
							else
							{
								tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, "DM_FUMO_MECHANISM_ALTERNATIVE_DOWNLOAD TASK_MSG_DM_SYNCML_CONNECT");
								tsDmMsg.taskSendMessage(DmTaskMsg.TASK_MSG_DL_SYNCML_CONNECT, null, null);
							}
						}
						else if (nMechanism == FumoConst.DM_FUMO_MECHANISM_REPLACE)
						{
							if (tsdmDB.dmdbGetChangedProtocol())
							{
								tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, "DM_FUMO_MECHANISM_REPLACE : Changed Protocol");
							}
							else
							{
								tsdmInfo pNvInfo = null;
								pNvInfo = (tsdmInfo) tsdmDB.dmdbGetProfileInfo(pNvInfo);
								if (pNvInfo == null)
									pNvInfo = new tsdmInfo();
								tsdmDB.dmdbSetServerUrl(pNvInfo.ServerUrl_Org);
								tsdmDB.dmdbSetServerAddress(pNvInfo.ServerIP_Org);
								tsdmDB.dmdbSetServerPort(pNvInfo.ServerPort_Org);
								tsdmDB.dmdbSetServerProtocol(pNvInfo.Protocol_Org);
								tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, "ServerUrl_Org:" + pNvInfo.ServerUrl_Org);
								tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, "ServerIP_Org:" + pNvInfo.ServerIP_Org);
								tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, "ServerPort_Org:" + pNvInfo.ServerPort_Org);
								tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, "Protocol_Org:" + pNvInfo.Protocol_Org);
							}
							agent.tpInit(DmDevInfoConst.SYNCMLDM);
							tsDmMsg.taskSendMessage(DmTaskMsg.TASK_MSG_DM_TCPIP_OPEN, null, null);
						}
						else if (nMechanism == FumoConst.DM_FUMO_MECHANISM_NONE)
						{
							if (tsdmDB.dmdbGetChangedProtocol())
							{
								tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, "DM_FUMO_MECHANISM_NONE : Changed Protocol");
							}
							else
							{
								tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, "DM_FUMO_MECHANISM_NONE");

								tsdmInfo pNvInfo = null;
								pNvInfo = (tsdmInfo) tsdmDB.dmdbGetProfileInfo(pNvInfo);
								if (pNvInfo == null)
									pNvInfo = new tsdmInfo();
								tsdmDB.dmdbSetServerUrl(pNvInfo.ServerUrl_Org);
								tsdmDB.dmdbSetServerAddress(pNvInfo.ServerIP_Org);
								tsdmDB.dmdbSetServerPort(pNvInfo.ServerPort_Org);
								tsdmDB.dmdbSetServerProtocol(pNvInfo.Protocol_Org);
							}
							agent.tpInit(DmDevInfoConst.SYNCMLDM);
							tsDmMsg.taskSendMessage(DmTaskMsg.TASK_MSG_DM_TCPIP_OPEN, null, null);
						}
					}
				}
				else
				{
					tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, "TASK_MSG_DM_SYNCML_CONNECT g_IsDMInitialized "+g_IsDMInitialized);
				}
				break;
			}

			case DmTaskMsg.TASK_MSG_DM_SYNCML_CONNECTRETRY:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, "TASK_MSG_DM_SYNCML_CONNECTRETRY");
				break;

			case DmTaskMsg.TASK_MSG_DM_SYNCML_CONNECTFAIL:
			{
				boolean bRc = false;
				tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, "TASK_MSG_DM_SYNCML_CONNECTFAIL");
				agent.tpClose(DmDevInfoConst.SYNCMLDM);
				agent.tpCloseNetWork(DmDevInfoConst.SYNCMLDM);

				bRc = agent.tpCheckRetry();
				if (bRc)
				{
					Thread.sleep(3000);
					tsDmMsg.taskSendMessage(DmTaskMsg.TASK_MSG_DM_SYNCML_CONNECT, null, null);
					break;
				}
				if (tsdmDB.dmdbGetChangedProtocol())
				{
					tsdmDB.dmdbBackUpServerUrl();
				}
				dmAgent.dmAgentSetSyncMode(DmDevInfoConst.DM_SYNC_NONE);
				dmAgent.tpSetRetryCount(NetConsts.TP_RETRY_COUNT_NONE);
				tsMsgEvent.SetMsgEvent(null, DmUiEvent.DM_EVENT_UI_SERVER_CONNECT_FAIL);
				break;
			}

			case DmTaskMsg.TASK_MSG_DM_SYNCML_SENDFAIL:
			{
				boolean bRc = false;
				tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, "TASK_MSG_DM_SYNCML_SENDFAIL");
				agent.tpClose(DmDevInfoConst.SYNCMLDM);
				agent.tpCloseNetWork(DmDevInfoConst.SYNCMLDM);
				bRc = agent.tpCheckRetry();
				if (bRc)
				{
					tsDmMsg.taskSendMessage(DmTaskMsg.TASK_MSG_DM_SYNCML_CONNECT, null, null);
					break;
				}
				if (tsdmDB.dmdbGetChangedProtocol())
				{
					tsdmDB.dmdbBackUpServerUrl();
				}
				dmAgent.dmAgentSetSyncMode(DmDevInfoConst.DM_SYNC_NONE);
				dmAgent.tpSetRetryCount(NetConsts.TP_RETRY_COUNT_NONE);
				tsMsgEvent.SetMsgEvent(null, DmUiEvent.DM_EVENT_UI_SEND_FAIL);
				break;
			}

			case DmTaskMsg.TASK_MSG_DM_SYNCML_RECEIVEFAIL:
			{
				boolean rc = false;
				tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, "TASK_MSG_DM_SYNCML_RECEIVEFAIL");
				agent.tpClose(DmDevInfoConst.SYNCMLDM);
				agent.tpCloseNetWork(DmDevInfoConst.SYNCMLDM);
				
				rc = agent.tpCheckRetry();
				if (rc)
				{
					tsDmMsg.taskSendMessage(DmTaskMsg.TASK_MSG_DM_SYNCML_CONNECT, null, null);
					break;
				}
				if (tsdmDB.dmdbGetChangedProtocol())
				{
					tsdmDB.dmdbBackUpServerUrl();
				}
				dmAgent.dmAgentSetSyncMode(DmDevInfoConst.DM_SYNC_NONE);
				dmAgent.tpSetRetryCount(NetConsts.TP_RETRY_COUNT_NONE);
				tsMsgEvent.SetMsgEvent(null, DmUiEvent.DM_EVENT_UI_RECV_FAIL);
				break;
			}

			case DmTaskMsg.TASK_MSG_DM_SYNCML_START:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, "TASK_MSG_DM_SYNCML_START");
				if (!tsService.isNetworkConnect())
				{
					tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, " Network Status is not ready. DM Not Initialized");
					tsService.tsNetworkUnready();
					break;
				}
				dmAgent.dmAgentSetSyncMode(DmDevInfoConst.DM_SYNC_RUN); // DM Workspace Active
				agent.agenthandler.dmAgntHdlrContinueSession(DmTaskMsg.TASK_MSG_DM_SYNCML_START, null);
				break;

			case DmTaskMsg.TASK_MSG_DM_SYNCML_CONTINUE:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, "TASK_MSG_DM_SYNCML_CONTINUE");
				agent.agenthandler.dmAgntHdlrContinueSession(DmTaskMsg.TASK_MSG_DM_SYNCML_CONTINUE, null);
				break;

			case DmTaskMsg.TASK_DM_SYNCML_ABORT:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, "TASK_DM_SYNCML_ABORT");

				if (msgItem.param == null)
				{
					tsLib.debugPrintException(DmDevInfoConst.DEBUG_EXCEPTION, "param is null");
					return false;
				}

				if (tsdmDB.dmdbGetChangedProtocol())
				{
					tsdmDB.dmdbBackUpServerUrl();
				}

				tsDmParamAbortmsg pAbortParam = (tsDmParamAbortmsg) msgItem.param.param;
				int nDMStatus = DmDevInfoConst.DM_SYNC_NONE;
				int rc;

				netHttpAdapter.httpCookieClear();

				tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, " pAbortParam.abortCode:" + pAbortParam.abortCode);
				if (pAbortParam.abortCode == DmTaskMsg.TASK_ABORT_USER_REQ)
				{
					tsdmDB.dmdbSetDmAgentType(DmDevInfoConst.SYNCML_DM_AGENT_DM);
					tsdmDB.dmdbSetFUMOStatus(FumoConst.DM_FUMO_STATE_NONE);
					tsdmDB.dmdbSetFUMOUpdateMechanism(FumoConst.DM_FUMO_MECHANISM_NONE);
				
					dmAgent.dmAgentSetUserInitiatedStatus(false);
					dmAgent.dmAgentSetServerInitiatedStatus(false);

					dmAgent.dmAgentClose();

					rc = agent.tpAbort(DmDevInfoConst.SYNCMLDM);
					if (rc >= NetConsts.TP_RET_OK)
					{
						agent.tpClose(DmDevInfoConst.SYNCMLDM);
						netHttpAdapter.netAdpSetReuse(false);
						agent.tpCloseNetWork(DmDevInfoConst.SYNCMLDM);
					}
					else
					{
						tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, "TASK_DM_SYNCML_ABORT, !rc >= TP_RET_OK");
					}
				}
				else if (pAbortParam.abortCode == DmTaskMsg.TASK_ABORT_SYNCDM_ERROR)
				{
					dmAgentHandler.dmAgentClose();

					rc = agent.tpAbort(DmDevInfoConst.SYNCMLDM);
					if (rc >= NetConsts.TP_RET_OK)
					{
						agent.tpClose(DmDevInfoConst.SYNCMLDM);
						netHttpAdapter.netAdpSetReuse(false);
						agent.tpCloseNetWork(DmDevInfoConst.SYNCMLDM);
					}
				}
				else if (pAbortParam.abortCode == NetConsts.TP_ECODE_HTTP_RETURN_STATUS_ERROR)
				{
					int nAgentType = tsdmDB.dmdbGetDmAgentType();

					tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, "TASK_DM_SYNCML_ABORT, not implement...");
					dmAgentHandler.dmAgentClose();

					rc = agent.tpAbort(DmDevInfoConst.SYNCMLDM);
					if (rc >= NetConsts.TP_RET_OK)
					{
						agent.tpClose(DmDevInfoConst.SYNCMLDM);
						netHttpAdapter.netAdpSetReuse(false);
						agent.tpCloseNetWork(DmDevInfoConst.SYNCMLDM);
					}

					if (nAgentType == DmDevInfoConst.SYNCML_DM_AGENT_FUMO)
					{
						tsdmDB.dmdbSetDmAgentType(DmDevInfoConst.SYNCML_DM_AGENT_DM);
						tsdmDB.dmdbSetFUMOStatus(FumoConst.DM_FUMO_STATE_NONE);
						tsdmDB.dmdbSetFUMOUpdateMechanism(FumoConst.DM_FUMO_MECHANISM_NONE);
					}
				}
				else if (pAbortParam.abortCode == NetConsts.TP_ECODE_SOCKET_REMOTE_CLOSED)
				{
					nDMStatus = dmAgentHandler.dmAgentGetSyncMode();
					if (nDMStatus == DmDevInfoConst.DM_SYNC_COMPLETE)
					{
						break;
					}
					else
					{
						dmAgentHandler.dmAgentClose();

						rc = agent.tpAbort(DmDevInfoConst.SYNCMLDM);
						if (rc >= NetConsts.TP_RET_OK)
						{
							agent.tpClose(DmDevInfoConst.SYNCMLDM);
							netHttpAdapter.netAdpSetReuse(true);
							agent.tpCloseNetWork(DmDevInfoConst.SYNCMLDM);
						}

						/* for remote close(in uic screen) */
						tsMsgEvent.SetMsgEvent(null, DmUiEvent.DM_EVENT_UI_REMOTE_CLOSED);
					}
				}
				else
				{
					tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, " TASK_DM_SYNCML_ABORT : ELSE");
					dmAgentHandler.dmAgentClose();
					rc = agent.tpAbort(DmDevInfoConst.SYNCMLDM);
					if (rc >= NetConsts.TP_RET_OK)
					{
						agent.tpClose(DmDevInfoConst.SYNCMLDM);
						netHttpAdapter.netAdpSetReuse(true);
						agent.tpCloseNetWork(DmDevInfoConst.SYNCMLDM);
					}
					else
					{
						tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, "TASK_DM_SYNCML_ABORT : ELSE, !rc >= TP_RET_OK");
					}
				}
				netHttpAdapter.netAdpSetReuse(false);

				dmAgent.tpSetRetryCount(NetConsts.TP_RETRY_COUNT_NONE);
				switch (pAbortParam.abortCode)
				{
					case NetConsts.TP_ECODE_SOCKET_RECEIVE_TIME_OUT:
					case NetConsts.TP_ECODE_SOCKET_RECEIVE_FAILED:
						tsMsgEvent.SetMsgEvent(null, DmUiEvent.DM_EVENT_UI_RECV_FAIL);
						break;
					case NetConsts.TP_ECODE_SOCKET_SEND_FAILED:
					case NetConsts.TP_ECODE_SOCKET_SEND_TIME_OUT:
						tsMsgEvent.SetMsgEvent(null, DmUiEvent.DM_EVENT_UI_SEND_FAIL);
						break;
					case NetConsts.TP_ECODE_SOCKET_REMOTE_CLOSED:
						tsMsgEvent.SetMsgEvent(null, DmUiEvent.DM_EVENT_UI_NETWORK_ERR);
						break;
					case DmTaskMsg.TASK_ABORT_USER_REQ:
						tsMsgEvent.SetMsgEvent(null, DmUiEvent.DM_EVENT_UI_ABORT_BYUSER);
						break;
					case DmTaskMsg.TASK_ABORT_SYNC_RETRY:
					case DmTaskMsg.TASK_ABORT_SYNCDM_ERROR:
						tsMsgEvent.SetMsgEvent(null, DmUiEvent.DM_EVENT_UI_SYNC_ERROR);
						break;
					case NetConsts.TP_ECODE_HTTP_RETURN_STATUS_ERROR:
						tsMsgEvent.SetMsgEvent(null, DmUiEvent.DM_EVENT_UI_HTTP_INTERNAL_ERROR);
						break;
					default:
						break;
				}
				break;

			case DmTaskMsg.TASK_MSG_DM_SYNCML_FINISH:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, "TASK_MSG_DM_SYNCML_FINISH");
				tsService.setUpdateCheckStatus(false);
				agent.tpClose(DmDevInfoConst.SYNCMLDM);
				agent.tpCloseNetWork(DmDevInfoConst.SYNCMLDM);
				dmAgent.tpSetRetryCount(NetConsts.TP_RETRY_COUNT_NONE);
				agent.agenthandler.dmAgntHdlrContinueSession(DmTaskMsg.TASK_MSG_DM_SYNCML_FINISH, null);
				dlAgent.dlAgentSetClientInitFlag(DmDevInfoConst.DM_NONE_INIT);
				break;

			case DmTaskMsg.TASK_MSG_DM_TCPAPN_OPEN:

               {
				int ret = 0;
				ret = agent.gHttpDMAdapter.tpApnOpen(DmDevInfoConst.SYNCMLDM);
				if (ret == NetConsts.TP_RET_CONNECTION_FAIL)
				{
					tsDmMsg.taskSendMessage(DmTaskMsg.TASK_MSG_DM_SYNCML_CONNECTFAIL, null, null);
				}
				break;
                }

			case DmTaskMsg.TASK_MSG_DM_TCPIP_OPEN:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, "TASK_MSG_DM_TCPIP_OPEN");
				tsDmMsg.taskSendMessage(DmTaskMsg.TASK_MSG_DM_SYNCML_START, null, null);
				break;

			case DmTaskMsg.TASK_MSG_DM_TCPIP_SEND:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, "TASK_MSG_DM_TCPIP_SEND");
				break;
			case DmTaskMsg.TASK_MSG_DM_TCPIP_CLOSE:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, "TASK_MSG_DM_TCPIP_CLOSE");
				break;
			case DmTaskMsg.TASK_MSG_DM_SOCKET_CONNECTED:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, "TASK_MSG_DM_SOCKET_CONNECTED");
				break;
			case DmTaskMsg.TASK_MSG_DM_SOCKET_DISCONNECTED:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, "TASK_MSG_DM_SOCKET_DISCONNECTED");
				break;
			case DmTaskMsg.TASK_MSG_DM_SOCKET_DATA_RECEIVED:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, "TASK_MSG_DM_SOCKET_DATA_RECEIVED");
				break;
			case DmTaskMsg.TASK_MSG_DM_SOCKET_SSL_TUNNEL_CONNECT:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, "TASK_MSG_DM_SOCKET_SSL_TUNNEL_CONNECT");
				break;
			case DmTaskMsg.TASK_MSG_DM_DDF_PARSER_ACTIVE:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, "TASK_MSG_DM_DDF_PARSER_ACTIVE");
				break;
			case DmTaskMsg.TASK_MSG_DM_DDF_PARSER_PROCESS:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, "TASK_MSG_DM_DDF_PARSER_PROCESS");
				break;
			case DmTaskMsg.TASK_MSG_DM_CLEAR_SESSION:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, "TASK_MSG_DM_CLEAR_SESSION");
				break;
			case DmTaskMsg.TASK_MSG_DM_OBEX_DEVICE_ACTIVATE:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, "TASK_MSG_DM_OBEX_DEVICE_ACTIVATE");
				break;
			case DmTaskMsg.TASK_MSG_DM_OBEX_DATA_RECEIVED:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, "TASK_MSG_DM_OBEX_DATA_RECEIVED");
				break;
			case DmTaskMsg.TASK_MSG_DM_OBEX_DEVICE_DEACTIVATE:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, "TASK_MSG_DM_OBEX_DEVICE_DEACTIVATE");
				break;
			case DmTaskMsg.TASK_MSG_DM_POLLING_UPDATE:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, "TASK_MSG_DM_POLLING_UPDATE");
				break;
			case DmTaskMsg.TASK_MSG_DM_AUTO_UPDATE_INITIATED:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, "TASK_MSG_DM_AUTO_UPDATE_INITIATED");
				break;

			case DmTaskMsg.TASK_MSG_DL_SYNCML_CONNECT:
			{
				tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, "TASK_MSG_DL_SYNCML_CONNECT");
				dlagent.dltpInit(DmDevInfoConst.SYNCMLDL);
				dmAgent.dmAgentSetSyncMode(DmDevInfoConst.DM_SYNC_RUN);
				tsDmMsg.taskSendMessage(DmTaskMsg.TASK_MSG_DL_TCPIP_OPEN, null, null);
				break;
			}

			case DmTaskMsg.TASK_MSG_DL_SYNCML_CONNECTFAIL:
			{
				boolean bRc = false;
				tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, "TASK_MSG_DL_SYNCML_CONNECTFAIL");

				dlagent.dltpClose(DmDevInfoConst.SYNCMLDL);
				dlagent.dltpCloseNetWork(DmDevInfoConst.SYNCMLDL);

				bRc = dlagent.dltpCheckRetry();
				if (bRc)
				{
					int nStatus = tsdmDB.dmdbGetFUMOStatus();
					if (!tsService.isNetworkConnect())
					{
						dmAgent.dmAgentSetSyncMode(DmDevInfoConst.DM_SYNC_NONE);
						dlAgent.dltpSetRetryCount(NetConsts.TP_RETRY_COUNT_NONE);
						tsMsgEvent.SetMsgEvent(null, DmUiEvent.DM_EVENT_UI_DOWNLOAD_FAILED_NETWORK_DISCONNECTED);
					}
/*					else if (dlAgent.dltpGetRetryCount() % 3 == 0 && nStatus == DM_FUMO_STATE_DOWNLOAD_IN_PROGRESS)
					{
						tsMsgEvent.SetMsgEvent(null, DM_EVENT_UI_DOWNLOAD_FAIL_RETRY_CONFIRM);
					}*/
					else
					{
						tsDmMsg.taskSendMessage(DmTaskMsg.TASK_MSG_DL_SYNCML_CONNECT, null, null);
					}
					break;
				}

				dmAgent.dmAgentSetSyncMode(DmDevInfoConst.DM_SYNC_NONE);
				dlAgent.dltpSetRetryCount(NetConsts.TP_RETRY_COUNT_NONE);

				if(bRc == false)
				{
				}

				int nRetryFailCnt = dlAgent.dltpGetRetryFailCount();
				if (nRetryFailCnt < NetConsts.TP_DL_RETRY_FAIL_COUNT_MAX)
				{
					nRetryFailCnt++;
					tsLib.debugPrintException(DmDevInfoConst.DEBUG_EXCEPTION, "TASK_MSG_DL_SYNCML_CONNECTFAIL nRetryFailCnt=" + nRetryFailCnt);
					dlAgent.dltpSetRetryFailCount(nRetryFailCnt);
					Thread.sleep(3000);
					tsDmMsg.taskSendMessage(DmTaskMsg.TASK_MSG_DL_SYNCML_CONNECT, null, null);
				}
				else
				{
					tsLib.debugPrintException(DmDevInfoConst.DEBUG_EXCEPTION, "TASK_MSG_DL_SYNCML_CONNECTFAIL nRetryFailCntMax OVER. Session reset");
					nRetryFailCnt = 0;
					dlAgent.dltpSetRetryFailCount(nRetryFailCnt);

					netHttpAdapter.netAdpSetReuse(false);
					tsdmDB.dmdbSetFUMOResultCode(FumoConst.DL_GENERIC_SERVER_ERROR);

					int nfumostatus = tsdmDB.dmdbGetFUMOStatus();
					tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, "Fumo Status = " + nfumostatus);
					if (nfumostatus != FumoConst.DM_FUMO_STATE_NONE)
					{
						tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, "send generic alert for fail to download package");
						// send generic alert for fail to download package
						// cause HTTP response error
/*						tsdmDB.dmdbSetFUMOStatus(DM_FUMO_STATE_DOWNLOAD_FAILED_REPORTING);
						tsDmMsg.taskSendMessage(TASK_MSG_DM_SYNCML_CONNECT, null, null);*/
						tsService.downloadFileFailCause = "download network error";
						dmFotaEntity.downloadFileFail();
						tsService.tsDownloadFail(2);
					}
				}

				break;
			}

			case DmTaskMsg.TASK_MSG_DL_SYNCML_SENDFAIL:
			{
				boolean bRc = false;
				tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, "TASK_MSG_DL_SYNCML_SENDFAIL");

				dlagent.dltpClose(DmDevInfoConst.SYNCMLDL);
				dlagent.dltpCloseNetWork(DmDevInfoConst.SYNCMLDL);

				bRc = dlagent.dltpCheckRetry();
				if (bRc)
				{
					int nStatus = tsdmDB.dmdbGetFUMOStatus();
					if (!tsService.isNetworkConnect())
					{
						dmAgent.dmAgentSetSyncMode(DmDevInfoConst.DM_SYNC_NONE);
						dlAgent.dltpSetRetryCount(NetConsts.TP_RETRY_COUNT_NONE);
						tsMsgEvent.SetMsgEvent(null, DmUiEvent.DM_EVENT_UI_DOWNLOAD_FAILED_NETWORK_DISCONNECTED);
					}
/*					else if (dlAgent.dltpGetRetryCount() % 3 == 0 && nStatus == DM_FUMO_STATE_DOWNLOAD_IN_PROGRESS)
					{
						tsMsgEvent.SetMsgEvent(null, DM_EVENT_UI_DOWNLOAD_FAIL_RETRY_CONFIRM);
					}*/
					else
					{
						tsDmMsg.taskSendMessage(DmTaskMsg.TASK_MSG_DL_SYNCML_CONNECT, null, null);
					}
					break;
				}
				dmAgent.dmAgentSetSyncMode(DmDevInfoConst.DM_SYNC_NONE);
				dlAgent.dltpSetRetryCount(NetConsts.TP_RETRY_COUNT_NONE);
				tsMsgEvent.SetMsgEvent(null, DmUiEvent.DM_EVENT_UI_SEND_FAIL);
				break;
			}

			case DmTaskMsg.TASK_MSG_DL_SYNCML_RECEIVEFAIL:
			{
				tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, "TASK_MSG_DL_SYNCML_RECEIVEFAIL");
				dlagent.dltpClose(DmDevInfoConst.SYNCMLDL);
				dlagent.dltpCloseNetWork(DmDevInfoConst.SYNCMLDL);
				boolean nrc = dlagent.dltpCheckRetry();
				if (nrc)
				{
					int nStatus = tsdmDB.dmdbGetFUMOStatus();
					if (!tsService.isNetworkConnect())
					{
						dmAgent.dmAgentSetSyncMode(DmDevInfoConst.DM_SYNC_NONE);
						dlAgent.dltpSetRetryCount(NetConsts.TP_RETRY_COUNT_NONE);
						tsMsgEvent.SetMsgEvent(null, DmUiEvent.DM_EVENT_UI_DOWNLOAD_FAILED_NETWORK_DISCONNECTED);
					}
/*					else if (dlAgent.dltpGetRetryCount() % 3 == 0 && nStatus == DM_FUMO_STATE_DOWNLOAD_IN_PROGRESS)
					{
						tsMsgEvent.SetMsgEvent(null, DM_EVENT_UI_DOWNLOAD_FAIL_RETRY_CONFIRM);
					}*/
					else
					{
						tsDmMsg.taskSendMessage(DmTaskMsg.TASK_MSG_DL_SYNCML_CONNECT, null, null);
					}
					break;
				}
				dmAgent.dmAgentSetSyncMode(DmDevInfoConst.DM_SYNC_NONE);
				dlAgent.dltpSetRetryCount(NetConsts.TP_RETRY_COUNT_NONE);
				tsMsgEvent.SetMsgEvent(null, DmUiEvent.DM_EVENT_UI_RECV_FAIL);
				break;
			}

			case DmTaskMsg.TASK_MSG_DL_SYNCML_ABORT:
			{
				tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, "TASK_MSG_DL_SYNCML_ABORT");

				if (msgItem.param == null)
					return false;

				tsDmParamAbortmsg pDLAbortParam = (tsDmParamAbortmsg) msgItem.param.param;

				if (pDLAbortParam.abortCode == DmTaskMsg.TASK_ABORT_USER_REQ)
				{
					if (!tsService.isNetworkConnect())
					{
						dmAgent.dmAgentSetSyncMode(DmDevInfoConst.DM_SYNC_NONE);
						dlAgent.dltpSetRetryCount(NetConsts.TP_RETRY_COUNT_NONE);
						tsMsgEvent.SetMsgEvent(null, DmUiEvent.DM_EVENT_UI_DOWNLOAD_FAILED_NETWORK_DISCONNECTED);
						return false;
					}
					
					tsdmDB.dmdbSetDmAgentType(DmDevInfoConst.SYNCML_DM_AGENT_FUMO);
					tsdmDB.dmdbSetFUMOStatus(FumoConst.DM_FUMO_STATE_NONE);
					tsdmDB.dmdbSetFUMOUpdateMechanism(FumoConst.DM_FUMO_MECHANISM_NONE);

					dmAgent.dmAgentSetUserInitiatedStatus(false);
					dmAgent.dmAgentSetServerInitiatedStatus(false);
					dmAgent.dmAgentClose();
					
					rc = dlagent.dltpAbort(DmDevInfoConst.SYNCMLDL);
					if (rc >= NetConsts.TP_RET_OK)
					{
						dlagent.dltpClose(DmDevInfoConst.SYNCMLDL);
						netHttpAdapter.netAdpSetReuse(false);
						dlagent.dltpCloseNetWork(DmDevInfoConst.SYNCMLDL);
					}
					else
					{
						tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, "TASK_MSG_DL_SYNCML_ABORT, !rc >= TP_RET_OK");
					}
				}
				if (pDLAbortParam.abortCode == NetConsts.TP_ECODE_HTTP_RETURN_STATUS_ERROR)
				{
					tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, "TP_ECODE_HTTP_RETURN_STATUS_ERROR, not implement...");
					dmAgentHandler.dmAgentClose();
					rc = dlagent.dltpAbort(DmDevInfoConst.SYNCMLDL);
					if (rc >= 0)
					{
						dlagent.dltpClose(DmDevInfoConst.SYNCMLDL);
						netHttpAdapter.netAdpSetReuse(false);
						dlagent.dltpCloseNetWork(DmDevInfoConst.SYNCMLDL);

						dmAgent.dmAgentSetSyncMode(DmDevInfoConst.DM_SYNC_NONE);
						dlAgent.dltpSetRetryCount(NetConsts.TP_RETRY_COUNT_NONE);
					}

					int nfumostatus = tsdmDB.dmdbGetFUMOStatus();
					if (nfumostatus == FumoConst.DM_FUMO_STATE_DOWNLOAD_COMPLETE)
					{
						tsdmDB.dmdbSetFUMOStatus(FumoConst.DM_FUMO_STATE_READY_TO_UPDATE);
						tsMsgEvent.SetMsgEvent(null, DmUiEvent.DL_EVENT_UI_DOWNLOAD_IN_COMPLETE);
					}
					else
					{
						// send generic alert for fail to download package
						// cause HTTP response error
						tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, "send generic alert for fail to download package");
						tsdmDB.dmdbSetFUMOResultCode(FumoConst.DL_GENERIC_SERVER_ERROR);
						tsdmDB.dmdbSetFUMOStatus(FumoConst.DM_FUMO_STATE_DOWNLOAD_FAILED_REPORTING);
						tsDmMsg.taskSendMessage(DmTaskMsg.TASK_MSG_DM_SYNCML_CONNECT, null, null);

						tsMsgEvent.SetMsgEvent(null, DmUiEvent.DM_EVENT_UI_SERVER_CONNECT_FAIL);
					}
				}
				break;
			}

			case DmTaskMsg.TASK_MSG_DL_SYNCML_CONNECTRETRY:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, "TASK_MSG_DL_SYNCML_CONNECTRETRY");
				break;

			case DmTaskMsg.TASK_MSG_DL_TCPAPN_OPEN:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, "TASK_MSG_DL_TCPAPN_OPEN");
				{
				int ret = 0;
				ret = dlagent.gHttpDLAdapter.tpApnOpen(DmDevInfoConst.SYNCMLDL);
				if (ret == NetConsts.TP_RET_CONNECTION_FAIL)
				{
					tsDmMsg.taskSendMessage(DmTaskMsg.TASK_MSG_DL_SYNCML_CONNECTFAIL, null, null);
				}
				break;
				}

			case DmTaskMsg.TASK_MSG_DL_TCPIP_OPEN:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, "TASK_MSG_DL_TCPIP_OPEN");
				int ret = NetConsts.TP_RET_OK;
				try
				{
					ret = dlagent.gHttpDLAdapter.tpOpen(DmDevInfoConst.SYNCMLDL);
				}
				catch (SocketTimeoutException e)
				{
					tsLib.debugPrintException(DmDevInfoConst.DEBUG_EXCEPTION, e.toString());
					netTimerConnect.endTimer();
					ret = NetConsts.TP_RET_CONNECTION_FAIL;
				}
				if (ret == NetConsts.TP_RET_OK)
					tsDmMsg.taskSendMessage(DmTaskMsg.TASK_MSG_DL_SYNCML_START, null, null);
				else
					tsDmMsg.taskSendMessage(DmTaskMsg.TASK_MSG_DL_SYNCML_CONNECTFAIL, null, null);

				break;
			case DmTaskMsg.TASK_MSG_DL_TCPIP_SEND:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, "TASK_MSG_DL_TCPIP_SEND");
				break;
			case DmTaskMsg.TASK_MSG_DL_TCPIP_CLOSE:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, "TASK_MSG_DL_TCPIP_CLOSE");
				dlagent.dltpClose(DmDevInfoConst.SYNCMLDL);
				dlagent.dltpCloseNetWork(DmDevInfoConst.SYNCMLDL);
				break;
			case DmTaskMsg.TASK_MSG_DL_SYNCML_START:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, "TASK_MSG_DL_SYNCML_START");
				dlagent.dlagenthandler.dlAgntHdlrStartOMADLAgent(DmTaskMsg.TASK_MSG_DL_SYNCML_START);
				break;
			case DmTaskMsg.TASK_MSG_DL_SYNCML_CONTINUE:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, "TASK_MSG_DL_SYNCML_CONTINUE");
				dlagent.dlagenthandler.dlAgntHdlrStartOMADLAgent(DmTaskMsg.TASK_MSG_DL_SYNCML_CONTINUE);
				break;

			case DmTaskMsg.TASK_MSG_DL_SOCKET_SSL_TUNNEL_CONNECT:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, "TASK_MSG_DL_SOCKET_SSL_TUNNEL_CONNECT");
				break;
			case DmTaskMsg.TASK_MSG_DL_SYNCML_FINISH:
			{
				tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, "TASK_MSG_DL_SYNCML_FINISH");
				rc = dlagent.dltpAbort(DmDevInfoConst.SYNCMLDL);
				if (rc >= 0)
				{
					dlagent.dltpClose(DmDevInfoConst.SYNCMLDL);
					dlagent.dltpCloseNetWork(DmDevInfoConst.SYNCMLDL);
				}
				dlAgent.dltpSetRetryCount(NetConsts.TP_RETRY_COUNT_NONE);
				dmAgent.dmAgentSetSyncMode(DmDevInfoConst.DM_SYNC_NONE);

				int nStatus = FumoConst.DM_FUMO_STATE_NONE;
				nStatus = tsdmDB.dmdbGetFUMOStatus();

				if (nStatus == FumoConst.DM_FUMO_STATE_DOWNLOAD_DESCRIPTOR)
				{
					tsMsgEvent.SetMsgEvent(null, DmUiEvent.DL_EVENT_UI_DOWNLOAD_YES_NO);
				}

				/* IOT Issue */
				if (DmDevInfoConst._SYNCML_TS_DM_VERSION_V12_)
				{
					tsdmDB.dmdbSetNotiReSyncMode(DmDevInfoConst.DM_NOTI_RESYNC_MODE_FALSE);
				}
				if (nStatus == FumoConst.DM_FUMO_STATE_READY_TO_UPDATE || nStatus == FumoConst.DM_FUMO_STATE_DOWNLOAD_COMPLETE)
				{
					tsMsgEvent.SetMsgEvent(null, DmUiEvent.DL_EVENT_UI_UPDATE_START);
				}
				break;
			}

			case DmTaskMsg.TASK_MSG_DL_USER_CANCEL_DOWNLOAD:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, "TASK_MSG_DL_USER_CANCEL_DOWNLOAD");
				tsdmDB.dmdbSetFUMOResultCode(FumoConst.DL_USER_CANCELED_DOWNLOAD);
				tsdmDB.dmdbSetFUMOStatus(FumoConst.DM_FUMO_STATE_DOWNLOAD_IN_CANCEL);
				tsDmMsg.taskSendMessage(DmTaskMsg.TASK_MSG_DM_SYNCML_CONNECT, null, null);
				break;

			case DmTaskMsg.TASK_MSG_DL_DOWNLOAD_FILE_ERROR:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, "TASK_MSG_DL_DOWNLOAD_FILE_ERROR"+" "+ tsService.downloadFileFailCause);
				tsdmDB.dmdbSetFUMOResultCode(FumoConst.DL_GENERIC_DOWNLOAD_FILE_ERROR+" "+ tsService.downloadFileFailCause);
				//tsdmDB.dmdbSetFUMOResultCode(DL_GENERIC_DOWNLOAD_FILE_ERROR);
				tsdmDB.dmdbSetFUMOStatus(FumoConst.DM_FUMO_STATE_UPDATE_SUCCESSFUL_NODATA);
				tsdmDB.dmdbSetDmAgentType(DmDevInfoConst.SYNCML_DM_AGENT_FUMO);
				tsDmMsg.taskSendMessage(DmTaskMsg.TASK_MSG_DM_SYNCML_START, null, null);
				break;

			case DmTaskMsg.TASK_MSG_DL_FIRMWARE_UPDATE_STANDBY:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, "TASK_MSG_DL_FIRMWARE_UPDATE_STANDBY");
				tsdmDB.dmdbSetFUMOResultCode(FumoConst.UPDATE_STANDBY);
				tsdmDB.dmdbSetFUMOStatus(FumoConst.DM_FUMO_STATE_UPDATE_SUCCESSFUL_NODATA);
				tsdmDB.dmdbSetDmAgentType(DmDevInfoConst.SYNCML_DM_AGENT_FUMO);
				tsDmMsg.taskSendMessage(DmTaskMsg.TASK_MSG_DM_SYNCML_START, null, null);
				break;

			case DmTaskMsg.TASK_MSG_DL_FIRMWARE_UPDATE_START:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, "TASK_MSG_DL_FIRMWARE_UPDATE_START updateType= " +tsService.updateType);
				if(tsService.updateType == 1) //PAS
					tsdmDB.dmdbSetFUMOResultCode(FumoConst.UPDATE_START_REBOOT);
				else if(tsService.updateType == 2) //AVNT
					tsdmDB.dmdbSetFUMOResultCode(FumoConst.UPDATE_START);
				tsdmDB.dmdbSetFUMOStatus(FumoConst.DM_FUMO_STATE_UPDATE_SUCCESSFUL_NODATA);
				tsdmDB.dmdbSetDmAgentType(DmDevInfoConst.SYNCML_DM_AGENT_FUMO);
				tsDmMsg.taskSendMessage(DmTaskMsg.TASK_MSG_DM_SYNCML_START, null, null);
				break;

			case DmTaskMsg.TASK_MSG_DL_FIRMWARE_UPDATE_SUCCESS:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, "TASK_MSG_DL_FIRMWARE_UPDATE_SUCCESS updateType= " +tsService.updateType);
				tsdmDB.dmdbSetFUMOResultCode(FumoConst.UPDATE_SUCCESS);
				tsdmDB.dmdbSetFUMOStatus(FumoConst.DM_FUMO_STATE_UPDATE_SUCCESSFUL_NODATA);
				tsdmDB.dmdbSetDmAgentType(DmDevInfoConst.SYNCML_DM_AGENT_FUMO);
				tsDmMsg.taskSendMessage(DmTaskMsg.TASK_MSG_DM_SYNCML_START, null, null);
				break;

			case DmTaskMsg.TASK_MSG_DL_FIRMWARE_UPDATE_FAIL:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, "TASK_MSG_DL_FIRMWARE_UPDATE_FAIL updateType= " +tsService.updateType+" uploadFailCause="+ tsService.uploadFailCause);
				tsdmDB.dmdbSetFUMOResultCode(FumoConst.UPDATE_FAIL+" "+ tsService.uploadFailCause);
				//tsdmDB.dmdbSetFUMOResultCode(UPDATE_FAIL);
				tsdmDB.dmdbSetFUMOStatus(FumoConst.DM_FUMO_STATE_UPDATE_SUCCESSFUL_NODATA);
				tsdmDB.dmdbSetDmAgentType(DmDevInfoConst.SYNCML_DM_AGENT_FUMO);
				tsDmMsg.taskSendMessage(DmTaskMsg.TASK_MSG_DM_SYNCML_START, null, null);
				break;

			case DmTaskMsg.TASK_MSG_DL_FIRMWARE_UPDATE_USERCANCEL:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, "TASK_MSG_DL_FIRMWARE_UPDATE_USERCANCEL updateType= " +tsService.updateType);
				tsdmDB.dmdbSetFUMOResultCode(FumoConst.UPDATE_USER_CANCELED);
				tsdmDB.dmdbSetFUMOStatus(FumoConst.DM_FUMO_STATE_UPDATE_SUCCESSFUL_NODATA);
				tsdmDB.dmdbSetDmAgentType(DmDevInfoConst.SYNCML_DM_AGENT_FUMO);
				tsDmMsg.taskSendMessage(DmTaskMsg.TASK_MSG_DM_SYNCML_START, null, null);
				break;

			case DmTaskMsg.TASK_MSG_DL_FIRMWARE_UPDATE_PARTITION:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, "TASK_MSG_DL_FIRMWARE_UPDATE_PARTITION updateType= " +tsService.updateType);
				tsdmDB.dmdbSetFUMOResultCode(FumoConst.UPDATE_PARTITION);
				tsdmDB.dmdbSetFUMOStatus(FumoConst.DM_FUMO_STATE_UPDATE_SUCCESSFUL_NODATA);
				tsdmDB.dmdbSetDmAgentType(DmDevInfoConst.SYNCML_DM_AGENT_FUMO);
				tsDmMsg.taskSendMessage(DmTaskMsg.TASK_MSG_DM_SYNCML_START, null, null);
				break;

			case DmTaskMsg.TASK_MSG_DL_FIRMWARE_UPDATE:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, "TASK_MSG_DL_FIRMWARE_UPDATE");
				break;
			case DmTaskMsg.TASK_MSG_DL_LOW_BATTERY_BEFORE_DOWNLOAD:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, "TASK_MSG_DL_LOW_BATTERY_BEFORE_DOWNLOAD");
				break;
			case DmTaskMsg.TASK_MSG_DL_POSTPONE:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, "TASK_MSG_DL_POSTPONE");
				break;
			case DmTaskMsg.TASK_MSG_UIC_REQUEST:
			{
				tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, "TASK_MSG_UIC_REQUEST");
				tsDmUicOption pSrcUICOption = (tsDmUicOption) msgItem.param.param;

				tsDmUicOption pUicOption = null;
				tsDmUicResult pUicResultKeep = null;
				int eUicResultKeepFlag = UserInteractionCommandConst.UIC_SAVE_NONE;

				pUicResultKeep = (tsDmUicResult) tsDmUic.dmGetUicResultKeep(eUicResultKeepFlag);
				{
					tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, "TASK_MSG_UIC_REQUEST else");
					pUicOption = tsDmUic.dmUicCreateUicOption();
					pUicOption = tsDmUic.dmUicCopyUicOption(pUicOption, pSrcUICOption);
					tsDmMsg.uiSendMessage(DmUiEvent.DM_EVENT_UI_UIC_REQUEST, pUicOption, null);
				}
				tsDmUic.dmUicFreeUicResult(pUicResultKeep);
				break;
			}
			case DmTaskMsg.TASK_MSG_UIC_RESPONSE:
				tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, "TASK_MSG_UIC_RESPONSE");

				tsDmUicResult pUicResult = (tsDmUicResult) msgItem.param.param;
				pUicResult = tsDmUic.dmSetUicResultKeep(pUicResult, UserInteractionCommandConst.UIC_SAVE_OK);
				dmAgentHandler dmAgtHandler = new dmAgentHandler();
				dmAgtHandler.dmAgntHdlrContinueSession(DmTaskMsg.TASK_MSG_UIC_RESPONSE, pUicResult);
				break;

			default:
				break;
		}

		msg = null;
		return false;
	}
	
	public void dmUITaskUserCancelDownload(int appId)
	{
		tsLib.debugPrint(DmDevInfoConst.DEBUG_DM, "appId : " + appId);

		if(appId == DmDevInfoConst.SYNCMLDM)
		{
			agent.tpClose(DmDevInfoConst.SYNCMLDM);
			netHttpAdapter.netAdpSetReuse(false);
			agent.tpCloseNetWork(DmDevInfoConst.SYNCMLDM);

			dmAgent.dmAgentSetSyncMode(DmDevInfoConst.DM_SYNC_NONE);
			dmAgent.tpSetRetryCount(NetConsts.TP_RETRY_COUNT_NONE);
			return;
		}
		else // during DmDevInfoConst.SYNCMLDL session
		{
			dlagent.dltpClose(DmDevInfoConst.SYNCMLDL);
			netHttpAdapter.netAdpSetReuse(false);
			dlagent.dltpCloseNetWork(DmDevInfoConst.SYNCMLDL);

			dmAgent.dmAgentSetSyncMode(DmDevInfoConst.DM_SYNC_NONE);
			dlAgent.dltpSetRetryCount(NetConsts.TP_RETRY_COUNT_NONE);
		}

		// send generic alert for fail to download package
		// cause User cancel download		
		tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, "send generic alert for fail to download package");
		tsdmDB.dmdbSetFUMOResultCode(FumoConst.DL_USER_CANCELED_DOWNLOAD);
		tsdmDB.dmdbSetFUMOStatus(FumoConst.DM_FUMO_STATE_USER_CANCEL_REPORTING);
		tsDmMsg.taskSendMessage(DmTaskMsg.TASK_MSG_DM_SYNCML_CONNECT, null, null);
	}

	public void dmTaskdmXXXFail()
	{
		tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK,"dmTaskdmXXXFail");
		agent.tpClose(DmDevInfoConst.SYNCMLDM);
		agent.tpCloseNetWork(DmDevInfoConst.SYNCMLDM);
	}

	public void dmTaskdlXXXFail()
	{
		tsLib.debugPrint(DmDevInfoConst.DEBUG_TASK, "dmTaskdlXXXFail");
    	dlagent.dltpClose(DmDevInfoConst.SYNCMLDL);
    	dlagent.dltpCloseNetWork(DmDevInfoConst.SYNCMLDL);
	}
}
