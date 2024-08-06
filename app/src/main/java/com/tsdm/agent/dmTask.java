package com.tsdm.agent;

import java.net.SocketTimeoutException;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.tsdm.adapt.tsLib;
import com.tsdm.tsService;
import com.tsdm.db.tsDefineDB;
import com.tsdm.db.tsdmDB;
import com.tsdm.db.tsdmInfo;
import com.tsdm.adapt.tsDefineIdle;
import com.tsdm.adapt.tsDmParamAbortmsg;
import com.tsdm.adapt.tsMsgEvent;
import com.tsdm.adapt.tsDmMsg;
import com.tsdm.adapt.tsDmUic;
import com.tsdm.adapt.tsDefineUic;
import com.tsdm.adapt.tsDmUicOption;
import com.tsdm.adapt.tsDmUicResult;
import com.tsdm.adapt.tsDmMsg.MsgItem;
import com.tsdm.net.netHttpAdapter;
import com.tsdm.net.netDefine;
import com.tsdm.net.netTimerConnect;

public class dmTask implements Runnable, dmDefineDevInfo, dmDefineMsg, dmDefineUIEvent, tsDefineUic, tsDefineIdle, tsDefineDB, netDefine
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
					tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
					Thread.currentThread().interrupt();
				}
			}
		};

		Looper.loop();
	}

	private boolean dmTaskDBInit()
	{
		boolean bRtn = false;
		tsLib.debugPrint(DEBUG_DM, "");
		try
		{
			tsdmDB.dbDMffs_Init();
			bRtn = true;
			mDBInit = true;
		}
		catch (NullPointerException e)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
		}

		return bRtn;
	}

	@SuppressWarnings("static-access")
	public void dmTaskInit()
	{
		if (!g_IsSyncTaskInit)
		{
			tsLib.debugPrint(DEBUG_DM, "");
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
			tsLib.debugPrint(DEBUG_DM, "already g_IsSyncTaskInit true");
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
			case TASK_MSG_OS_INITIALIZED:
				tsLib.debugPrint(DEBUG_TASK, "TASK_MSG_OS_INITIALIZED");
				netHttpAdapter.setIsConnected(false);
				dmAgent.tpSetRetryCount(TP_RETRY_COUNT_NONE);
				dmAgent.dmAgentSetSyncMode(DM_SYNC_NONE);
				dmTaskInit();
				break;

			case TASK_MSG_PHONEBOOK_INITIALIZED:
				tsLib.debugPrint(DEBUG_TASK, "TASK_MSG_PHONEBOOK_INITIALIZED");
				break;

			case TASK_MSG_NETWORK_STATUS_UPDATED:
				tsLib.debugPrint(DEBUG_TASK, "TASK_MSG_NETWORK_STATUS_UPDATED");
				break;

			case TASK_MSG_DM_SYNCML_IDLE_STATE:
				tsLib.debugPrint(DEBUG_TASK, "TASK_MSG_DM_SYNCML_IDLE_STATE");
				break;

			case TASK_MSG_DM_SYNCML_INIT:
				tsLib.debugPrint(DEBUG_TASK, "TASK_MSG_DM_SYNCML_INIT");
				if (!tsService.isNetworkConnect())
				{
					tsLib.debugPrint(DEBUG_TASK, " Network Status is not ready. DM Not Initialized");
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
						tsLib.debugPrint(DEBUG_TASK, "TASK_MSG_DM_SYNCML_INIT : Not Initialized");
					}
					else {
						tsLib.debugPrint(DEBUG_TASK, "TASK_MSG_DM_SYNCML_INIT : Initialized");
						g_IsDMInitialized = true;
					}

				}

				tsService.dmNetProfileChangeSet(); // profile change
				break;

			case TASK_MSG_DM_SYNCML_CONNECT:
			{
				tsLib.debugPrint(DEBUG_TASK, "TASK_MSG_DM_SYNCML_CONNECT");
				int nMechanism = 0;
				int nFumoStatus = DM_FUMO_STATE_NONE;
				int nAgentType = SYNCML_DM_AGENT_DM;

				if (g_IsDMInitialized)
				{
					boolean rc = true;

					rc = dlAgent.dlAgentIsStatus();
					tsLib.debugPrint(DEBUG_TASK, "dlAgentIsStatus =" + rc);

					if (rc)
					{
						if (tsdmDB.dmdbGetChangedProtocol())
						{
							tsLib.debugPrint(DEBUG_TASK, "TASK_MSG_DM_SYNCML_CONNECT : Changed Protocol");
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
						agent.tpInit(SYNCMLDM);
						tsDmMsg.taskSendMessage(TASK_MSG_DM_TCPIP_OPEN, null, null);
					}
					else
					{
						nMechanism = tsdmDB.dmdbGetFUMOUpdateMechanism();
						tsLib.debugPrint(DEBUG_TASK, "nMechanism : " + nMechanism);
						nFumoStatus = tsdmDB.dmdbGetFUMOStatus();
						tsLib.debugPrint(DEBUG_TASK, "nFumoStatus : " + nFumoStatus);

						if (nMechanism == DM_FUMO_MECHANISM_ALTERNATIVE)
						{
							if (nFumoStatus == DM_FUMO_STATE_READY_TO_UPDATE)
							{
								tsMsgEvent.SetMsgEvent(null, DL_EVENT_UI_UPDATE_START);
							}
							else
							{
								tsLib.debugPrint(DEBUG_TASK, "DM_FUMO_MECHANISM_ALTERNATIVE TASK_MSG_DM_SYNCML_CONNECT");
								tsDmMsg.taskSendMessage(TASK_MSG_DL_SYNCML_CONNECT, null, null); // dl connect
							}
						}
						else if (nMechanism == DM_FUMO_MECHANISM_ALTERNATIVE_DOWNLOAD)
						{
							if (nFumoStatus == DM_FUMO_STATE_READY_TO_UPDATE)
							{
								tsMsgEvent.SetMsgEvent(null, DL_EVENT_UI_UPDATE_START);
							}
							else if (nFumoStatus == DM_FUMO_STATE_DOWNLOAD_COMPLETE)
							{
								agent.tpInit(SYNCMLDM);
								tsDmMsg.taskSendMessage(TASK_MSG_DM_TCPIP_OPEN, null, null);
							}
							else
							{
								tsLib.debugPrint(DEBUG_TASK, "DM_FUMO_MECHANISM_ALTERNATIVE_DOWNLOAD TASK_MSG_DM_SYNCML_CONNECT");
								tsDmMsg.taskSendMessage(TASK_MSG_DL_SYNCML_CONNECT, null, null);
							}
						}
						else if (nMechanism == DM_FUMO_MECHANISM_REPLACE)
						{
							if (tsdmDB.dmdbGetChangedProtocol())
							{
								tsLib.debugPrint(DEBUG_TASK, "DM_FUMO_MECHANISM_REPLACE : Changed Protocol");
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
								tsLib.debugPrint(DEBUG_TASK, "ServerUrl_Org:" + pNvInfo.ServerUrl_Org);
								tsLib.debugPrint(DEBUG_TASK, "ServerIP_Org:" + pNvInfo.ServerIP_Org);
								tsLib.debugPrint(DEBUG_TASK, "ServerPort_Org:" + pNvInfo.ServerPort_Org);
								tsLib.debugPrint(DEBUG_TASK, "Protocol_Org:" + pNvInfo.Protocol_Org);
							}
							agent.tpInit(SYNCMLDM);
							tsDmMsg.taskSendMessage(TASK_MSG_DM_TCPIP_OPEN, null, null);
						}
						else if (nMechanism == DM_FUMO_MECHANISM_NONE)
						{
							if (tsdmDB.dmdbGetChangedProtocol())
							{
								tsLib.debugPrint(DEBUG_TASK, "DM_FUMO_MECHANISM_NONE : Changed Protocol");
							}
							else
							{
								tsLib.debugPrint(DEBUG_TASK, "DM_FUMO_MECHANISM_NONE");

								tsdmInfo pNvInfo = null;
								pNvInfo = (tsdmInfo) tsdmDB.dmdbGetProfileInfo(pNvInfo);
								if (pNvInfo == null)
									pNvInfo = new tsdmInfo();
								tsdmDB.dmdbSetServerUrl(pNvInfo.ServerUrl_Org);
								tsdmDB.dmdbSetServerAddress(pNvInfo.ServerIP_Org);
								tsdmDB.dmdbSetServerPort(pNvInfo.ServerPort_Org);
								tsdmDB.dmdbSetServerProtocol(pNvInfo.Protocol_Org);
							}
							agent.tpInit(SYNCMLDM);
							tsDmMsg.taskSendMessage(TASK_MSG_DM_TCPIP_OPEN, null, null);
						}
					}
				}
				else
				{
					tsLib.debugPrint(DEBUG_TASK, "TASK_MSG_DM_SYNCML_CONNECT g_IsDMInitialized "+g_IsDMInitialized);
				}
				break;
			}

			case TASK_MSG_DM_SYNCML_CONNECTRETRY:
				tsLib.debugPrint(DEBUG_TASK, "TASK_MSG_DM_SYNCML_CONNECTRETRY");
				break;

			case TASK_MSG_DM_SYNCML_CONNECTFAIL:
			{
				boolean bRc = false;
				tsLib.debugPrint(DEBUG_TASK, "TASK_MSG_DM_SYNCML_CONNECTFAIL");
				agent.tpClose(SYNCMLDM);
				agent.tpCloseNetWork(SYNCMLDM);

				bRc = agent.tpCheckRetry();
				if (bRc)
				{
					Thread.sleep(3000);
					tsDmMsg.taskSendMessage(TASK_MSG_DM_SYNCML_CONNECT, null, null);
					break;
				}
				if (tsdmDB.dmdbGetChangedProtocol())
				{
					tsdmDB.dmdbBackUpServerUrl();
				}
				dmAgent.dmAgentSetSyncMode(DM_SYNC_NONE);
				dmAgent.tpSetRetryCount(TP_RETRY_COUNT_NONE);
				tsMsgEvent.SetMsgEvent(null, DM_EVENT_UI_SERVER_CONNECT_FAIL);
				break;
			}

			case TASK_MSG_DM_SYNCML_SENDFAIL:
			{
				boolean bRc = false;
				tsLib.debugPrint(DEBUG_TASK, "TASK_MSG_DM_SYNCML_SENDFAIL");
				agent.tpClose(SYNCMLDM);
				agent.tpCloseNetWork(SYNCMLDM);
				bRc = agent.tpCheckRetry();
				if (bRc)
				{
					tsDmMsg.taskSendMessage(TASK_MSG_DM_SYNCML_CONNECT, null, null);
					break;
				}
				if (tsdmDB.dmdbGetChangedProtocol())
				{
					tsdmDB.dmdbBackUpServerUrl();
				}
				dmAgent.dmAgentSetSyncMode(DM_SYNC_NONE);
				dmAgent.tpSetRetryCount(TP_RETRY_COUNT_NONE);
				tsMsgEvent.SetMsgEvent(null, DM_EVENT_UI_SEND_FAIL);
				break;
			}

			case TASK_MSG_DM_SYNCML_RECEIVEFAIL:
			{
				boolean rc = false;
				tsLib.debugPrint(DEBUG_TASK, "TASK_MSG_DM_SYNCML_RECEIVEFAIL");
				agent.tpClose(SYNCMLDM);
				agent.tpCloseNetWork(SYNCMLDM);
				
				rc = agent.tpCheckRetry();
				if (rc)
				{
					tsDmMsg.taskSendMessage(TASK_MSG_DM_SYNCML_CONNECT, null, null);
					break;
				}
				if (tsdmDB.dmdbGetChangedProtocol())
				{
					tsdmDB.dmdbBackUpServerUrl();
				}
				dmAgent.dmAgentSetSyncMode(DM_SYNC_NONE);
				dmAgent.tpSetRetryCount(TP_RETRY_COUNT_NONE);
				tsMsgEvent.SetMsgEvent(null, DM_EVENT_UI_RECV_FAIL);
				break;
			}

			case TASK_MSG_DM_SYNCML_START:
				tsLib.debugPrint(DEBUG_TASK, "TASK_MSG_DM_SYNCML_START");
				if (!tsService.isNetworkConnect())
				{
					tsLib.debugPrint(DEBUG_TASK, " Network Status is not ready. DM Not Initialized");
					tsService.tsNetworkUnready();
					break;
				}
				dmAgent.dmAgentSetSyncMode(DM_SYNC_RUN); // DM Workspace Active
				agent.agenthandler.dmAgntHdlrContinueSession(TASK_MSG_DM_SYNCML_START, null);
				break;

			case TASK_MSG_DM_SYNCML_CONTINUE:
				tsLib.debugPrint(DEBUG_TASK, "TASK_MSG_DM_SYNCML_CONTINUE");
				agent.agenthandler.dmAgntHdlrContinueSession(TASK_MSG_DM_SYNCML_CONTINUE, null);
				break;

			case TASK_DM_SYNCML_ABORT:
				tsLib.debugPrint(DEBUG_TASK, "TASK_DM_SYNCML_ABORT");

				if (msgItem.param == null)
				{
					tsLib.debugPrintException(DEBUG_EXCEPTION, "param is null");
					return false;
				}

				if (tsdmDB.dmdbGetChangedProtocol())
				{
					tsdmDB.dmdbBackUpServerUrl();
				}

				tsDmParamAbortmsg pAbortParam = (tsDmParamAbortmsg) msgItem.param.param;
				int nDMStatus = DM_SYNC_NONE;
				int rc;

				netHttpAdapter.httpCookieClear();

				tsLib.debugPrint(DEBUG_TASK, " pAbortParam.abortCode:" + pAbortParam.abortCode);
				if (pAbortParam.abortCode == TASK_ABORT_USER_REQ)
				{
					tsdmDB.dmdbSetDmAgentType(SYNCML_DM_AGENT_DM);
					tsdmDB.dmdbSetFUMOStatus(DM_FUMO_STATE_NONE);
					tsdmDB.dmdbSetFUMOUpdateMechanism(DM_FUMO_MECHANISM_NONE);
				
					dmAgent.dmAgentSetUserInitiatedStatus(false);
					dmAgent.dmAgentSetServerInitiatedStatus(false);

					dmAgent.dmAgentClose();

					rc = agent.tpAbort(SYNCMLDM);
					if (rc >= TP_RET_OK)
					{
						agent.tpClose(SYNCMLDM);
						netHttpAdapter.netAdpSetReuse(false);
						agent.tpCloseNetWork(SYNCMLDM);
					}
					else
					{
						tsLib.debugPrint(DEBUG_TASK, "TASK_DM_SYNCML_ABORT, !rc >= TP_RET_OK");
					}
				}
				else if (pAbortParam.abortCode == TASK_ABORT_SYNCDM_ERROR)
				{
					dmAgentHandler.dmAgentClose();

					rc = agent.tpAbort(SYNCMLDM);
					if (rc >= TP_RET_OK)
					{
						agent.tpClose(SYNCMLDM);
						netHttpAdapter.netAdpSetReuse(false);
						agent.tpCloseNetWork(SYNCMLDM);
					}
				}
				else if (pAbortParam.abortCode == TP_ECODE_HTTP_RETURN_STATUS_ERROR)
				{
					int nAgentType = tsdmDB.dmdbGetDmAgentType();

					tsLib.debugPrint(DEBUG_TASK, "TASK_DM_SYNCML_ABORT, not implement...");
					dmAgentHandler.dmAgentClose();

					rc = agent.tpAbort(SYNCMLDM);
					if (rc >= TP_RET_OK)
					{
						agent.tpClose(SYNCMLDM);
						netHttpAdapter.netAdpSetReuse(false);
						agent.tpCloseNetWork(SYNCMLDM);
					}

					if (nAgentType == SYNCML_DM_AGENT_FUMO)
					{
						tsdmDB.dmdbSetDmAgentType(SYNCML_DM_AGENT_DM);
						tsdmDB.dmdbSetFUMOStatus(DM_FUMO_STATE_NONE);
						tsdmDB.dmdbSetFUMOUpdateMechanism(DM_FUMO_MECHANISM_NONE);
					}
				}
				else if (pAbortParam.abortCode == TP_ECODE_SOCKET_REMOTE_CLOSED)
				{
					nDMStatus = dmAgentHandler.dmAgentGetSyncMode();
					if (nDMStatus == DM_SYNC_COMPLETE)
					{
						break;
					}
					else
					{
						dmAgentHandler.dmAgentClose();

						rc = agent.tpAbort(SYNCMLDM);
						if (rc >= TP_RET_OK)
						{
							agent.tpClose(SYNCMLDM);
							netHttpAdapter.netAdpSetReuse(true);
							agent.tpCloseNetWork(SYNCMLDM);
						}

						/* for remote close(in uic screen) */
						tsMsgEvent.SetMsgEvent(null, DM_EVENT_UI_REMOTE_CLOSED);
					}
				}
				else
				{
					tsLib.debugPrint(DEBUG_TASK, " TASK_DM_SYNCML_ABORT : ELSE");
					dmAgentHandler.dmAgentClose();
					rc = agent.tpAbort(SYNCMLDM);
					if (rc >= TP_RET_OK)
					{
						agent.tpClose(SYNCMLDM);
						netHttpAdapter.netAdpSetReuse(true);
						agent.tpCloseNetWork(SYNCMLDM);
					}
					else
					{
						tsLib.debugPrint(DEBUG_TASK, "TASK_DM_SYNCML_ABORT : ELSE, !rc >= TP_RET_OK");
					}
				}
				netHttpAdapter.netAdpSetReuse(false);

				dmAgent.tpSetRetryCount(TP_RETRY_COUNT_NONE);
				switch (pAbortParam.abortCode)
				{
					case TP_ECODE_SOCKET_RECEIVE_TIME_OUT:
					case TP_ECODE_SOCKET_RECEIVE_FAILED:
						tsMsgEvent.SetMsgEvent(null, DM_EVENT_UI_RECV_FAIL);
						break;
					case TP_ECODE_SOCKET_SEND_FAILED:
					case TP_ECODE_SOCKET_SEND_TIME_OUT:
						tsMsgEvent.SetMsgEvent(null, DM_EVENT_UI_SEND_FAIL);
						break;
					case TP_ECODE_SOCKET_REMOTE_CLOSED:
						tsMsgEvent.SetMsgEvent(null, DM_EVENT_UI_NETWORK_ERR);
						break;
					case TASK_ABORT_USER_REQ:
						tsMsgEvent.SetMsgEvent(null, DM_EVENT_UI_ABORT_BYUSER);
						break;
					case TASK_ABORT_SYNC_RETRY:
					case TASK_ABORT_SYNCDM_ERROR:
						tsMsgEvent.SetMsgEvent(null, DM_EVENT_UI_SYNC_ERROR);
						break;
					case TP_ECODE_HTTP_RETURN_STATUS_ERROR:
						tsMsgEvent.SetMsgEvent(null, DM_EVENT_UI_HTTP_INTERNAL_ERROR);
						break;
					default:
						break;
				}
				break;

			case TASK_MSG_DM_SYNCML_FINISH:
				tsLib.debugPrint(DEBUG_TASK, "TASK_MSG_DM_SYNCML_FINISH");
				agent.tpClose(SYNCMLDM);
				agent.tpCloseNetWork(SYNCMLDM);
				dmAgent.tpSetRetryCount(TP_RETRY_COUNT_NONE);
				agent.agenthandler.dmAgntHdlrContinueSession(TASK_MSG_DM_SYNCML_FINISH, null);
				dlAgent.dlAgentSetClientInitFlag(DM_NONE_INIT);
				break;

			case TASK_MSG_DM_TCPAPN_OPEN:

               {
				int ret = 0;
				ret = agent.gHttpDMAdapter.tpApnOpen(SYNCMLDM);
				if (ret == TP_RET_CONNECTION_FAIL)
				{
					tsDmMsg.taskSendMessage(TASK_MSG_DM_SYNCML_CONNECTFAIL, null, null);
				}
				break;
                }

			case TASK_MSG_DM_TCPIP_OPEN:
				tsLib.debugPrint(DEBUG_TASK, "TASK_MSG_DM_TCPIP_OPEN");
				tsDmMsg.taskSendMessage(TASK_MSG_DM_SYNCML_START, null, null);
				break;

			case TASK_MSG_DM_TCPIP_SEND:
				tsLib.debugPrint(DEBUG_TASK, "TASK_MSG_DM_TCPIP_SEND");
				break;
			case TASK_MSG_DM_TCPIP_CLOSE:
				tsLib.debugPrint(DEBUG_TASK, "TASK_MSG_DM_TCPIP_CLOSE");
				break;
			case TASK_MSG_DM_SOCKET_CONNECTED:
				tsLib.debugPrint(DEBUG_TASK, "TASK_MSG_DM_SOCKET_CONNECTED");
				break;
			case TASK_MSG_DM_SOCKET_DISCONNECTED:
				tsLib.debugPrint(DEBUG_TASK, "TASK_MSG_DM_SOCKET_DISCONNECTED");
				break;
			case TASK_MSG_DM_SOCKET_DATA_RECEIVED:
				tsLib.debugPrint(DEBUG_TASK, "TASK_MSG_DM_SOCKET_DATA_RECEIVED");
				break;
			case TASK_MSG_DM_SOCKET_SSL_TUNNEL_CONNECT:
				tsLib.debugPrint(DEBUG_TASK, "TASK_MSG_DM_SOCKET_SSL_TUNNEL_CONNECT");
				break;
			case TASK_MSG_DM_DDF_PARSER_ACTIVE:
				tsLib.debugPrint(DEBUG_TASK, "TASK_MSG_DM_DDF_PARSER_ACTIVE");
				break;
			case TASK_MSG_DM_DDF_PARSER_PROCESS:
				tsLib.debugPrint(DEBUG_TASK, "TASK_MSG_DM_DDF_PARSER_PROCESS");
				break;
			case TASK_MSG_DM_CLEAR_SESSION:
				tsLib.debugPrint(DEBUG_TASK, "TASK_MSG_DM_CLEAR_SESSION");
				break;
			case TASK_MSG_DM_OBEX_DEVICE_ACTIVATE:
				tsLib.debugPrint(DEBUG_TASK, "TASK_MSG_DM_OBEX_DEVICE_ACTIVATE");
				break;
			case TASK_MSG_DM_OBEX_DATA_RECEIVED:
				tsLib.debugPrint(DEBUG_TASK, "TASK_MSG_DM_OBEX_DATA_RECEIVED");
				break;
			case TASK_MSG_DM_OBEX_DEVICE_DEACTIVATE:
				tsLib.debugPrint(DEBUG_TASK, "TASK_MSG_DM_OBEX_DEVICE_DEACTIVATE");
				break;
			case TASK_MSG_DM_POLLING_UPDATE:
				tsLib.debugPrint(DEBUG_TASK, "TASK_MSG_DM_POLLING_UPDATE");
				break;
			case TASK_MSG_DM_AUTO_UPDATE_INITIATED:
				tsLib.debugPrint(DEBUG_TASK, "TASK_MSG_DM_AUTO_UPDATE_INITIATED");
				break;

			case TASK_MSG_DL_SYNCML_CONNECT:
			{
				tsLib.debugPrint(DEBUG_TASK, "TASK_MSG_DL_SYNCML_CONNECT");
				dlagent.dltpInit(SYNCMLDL);
				dmAgent.dmAgentSetSyncMode(DM_SYNC_RUN);
				tsDmMsg.taskSendMessage(TASK_MSG_DL_TCPIP_OPEN, null, null);
				break;
			}

			case TASK_MSG_DL_SYNCML_CONNECTFAIL:
			{
				boolean bRc = false;
				tsLib.debugPrint(DEBUG_TASK, "TASK_MSG_DL_SYNCML_CONNECTFAIL");

				dlagent.dltpClose(SYNCMLDL);
				dlagent.dltpCloseNetWork(SYNCMLDL);

				bRc = dlagent.dltpCheckRetry();
				if (bRc)
				{
					int nStatus = tsdmDB.dmdbGetFUMOStatus();
					if (!tsService.isNetworkConnect())
					{
						dmAgent.dmAgentSetSyncMode(DM_SYNC_NONE);
						dlAgent.dltpSetRetryCount(TP_RETRY_COUNT_NONE);
						tsMsgEvent.SetMsgEvent(null, DM_EVENT_UI_DOWNLOAD_FAILED_WIFI_DISCONNECTED);
					}
					else if (dlAgent.dltpGetRetryCount() % 3 == 0 && nStatus == DM_FUMO_STATE_DOWNLOAD_IN_PROGRESS)
					{
						tsMsgEvent.SetMsgEvent(null, DM_EVENT_UI_DOWNLOAD_FAIL_RETRY_CONFIRM);
					}
					else
					{
						tsDmMsg.taskSendMessage(TASK_MSG_DL_SYNCML_CONNECT, null, null);
					}
					break;
				}

				dmAgent.dmAgentSetSyncMode(DM_SYNC_NONE);
				dlAgent.dltpSetRetryCount(TP_RETRY_COUNT_NONE);

				if(bRc == false)
				{
				}

				int nRetryFailCnt = dlAgent.dltpGetRetryFailCount();
				if (nRetryFailCnt < TP_DL_RETRY_FAIL_COUNT_MAX)
				{
					nRetryFailCnt++;
					tsLib.debugPrintException(DEBUG_EXCEPTION, "TASK_MSG_DL_SYNCML_CONNECTFAIL nRetryFailCnt=" + nRetryFailCnt);
					dlAgent.dltpSetRetryFailCount(nRetryFailCnt);
					Thread.sleep(3000);
					tsDmMsg.taskSendMessage(TASK_MSG_DL_SYNCML_CONNECT, null, null);
				}
				else
				{
					tsLib.debugPrintException(DEBUG_EXCEPTION, "TASK_MSG_DL_SYNCML_CONNECTFAIL nRetryFailCntMax OVER. Session reset");
					nRetryFailCnt = 0;
					dlAgent.dltpSetRetryFailCount(nRetryFailCnt);

					netHttpAdapter.netAdpSetReuse(false);
					tsdmDB.dmdbSetFUMOResultCode(DL_GENERIC_SERVER_ERROR);

					int nfumostatus = tsdmDB.dmdbGetFUMOStatus();
					tsLib.debugPrint(DEBUG_TASK, "Fumo Status = " + nfumostatus);
					if (nfumostatus != DM_FUMO_STATE_NONE)
					{
						tsLib.debugPrint(DEBUG_TASK, "send generic alert for fail to download package");
						// send generic alert for fail to download package
						// cause HTTP response error
						tsdmDB.dmdbSetFUMOStatus(DM_FUMO_STATE_DOWNLOAD_FAILED_REPORTING);
						tsDmMsg.taskSendMessage(TASK_MSG_DM_SYNCML_CONNECT, null, null);
						tsService.tsDownloadFail(2);
					}
				}

				break;
			}

			case TASK_MSG_DL_SYNCML_SENDFAIL:
			{
				boolean bRc = false;
				tsLib.debugPrint(DEBUG_TASK, "TASK_MSG_DL_SYNCML_SENDFAIL");

				dlagent.dltpClose(SYNCMLDL);
				dlagent.dltpCloseNetWork(SYNCMLDL);

				bRc = dlagent.dltpCheckRetry();
				if (bRc)
				{
					int nStatus = tsdmDB.dmdbGetFUMOStatus();
					if (!tsService.isNetworkConnect())
					{
						dmAgent.dmAgentSetSyncMode(DM_SYNC_NONE);
						dlAgent.dltpSetRetryCount(TP_RETRY_COUNT_NONE);
						tsMsgEvent.SetMsgEvent(null, DM_EVENT_UI_DOWNLOAD_FAILED_WIFI_DISCONNECTED);
					}
					else if (dlAgent.dltpGetRetryCount() % 3 == 0 && nStatus == DM_FUMO_STATE_DOWNLOAD_IN_PROGRESS)
					{
						tsMsgEvent.SetMsgEvent(null, DM_EVENT_UI_DOWNLOAD_FAIL_RETRY_CONFIRM);
					}
					else
					{
						tsDmMsg.taskSendMessage(TASK_MSG_DL_SYNCML_CONNECT, null, null);
					}
					break;
				}
				dmAgent.dmAgentSetSyncMode(DM_SYNC_NONE);
				dlAgent.dltpSetRetryCount(TP_RETRY_COUNT_NONE);
				tsMsgEvent.SetMsgEvent(null, DM_EVENT_UI_SEND_FAIL);
				break;
			}

			case TASK_MSG_DL_SYNCML_RECEIVEFAIL:
			{
				tsLib.debugPrint(DEBUG_TASK, "TASK_MSG_DL_SYNCML_RECEIVEFAIL");
				dlagent.dltpClose(SYNCMLDL);
				dlagent.dltpCloseNetWork(SYNCMLDL);
				boolean nrc = dlagent.dltpCheckRetry();
				if (nrc)
				{
					int nStatus = tsdmDB.dmdbGetFUMOStatus();
					if (!tsService.isNetworkConnect())
					{
						dmAgent.dmAgentSetSyncMode(DM_SYNC_NONE);
						dlAgent.dltpSetRetryCount(TP_RETRY_COUNT_NONE);
						tsMsgEvent.SetMsgEvent(null, DM_EVENT_UI_DOWNLOAD_FAILED_WIFI_DISCONNECTED);
					}
					else if (dlAgent.dltpGetRetryCount() % 3 == 0 && nStatus == DM_FUMO_STATE_DOWNLOAD_IN_PROGRESS)
					{
						tsMsgEvent.SetMsgEvent(null, DM_EVENT_UI_DOWNLOAD_FAIL_RETRY_CONFIRM);
					}
					else
					{
						tsDmMsg.taskSendMessage(TASK_MSG_DL_SYNCML_CONNECT, null, null);
					}
					break;
				}
				dmAgent.dmAgentSetSyncMode(DM_SYNC_NONE);
				dlAgent.dltpSetRetryCount(TP_RETRY_COUNT_NONE);
				tsMsgEvent.SetMsgEvent(null, DM_EVENT_UI_RECV_FAIL);
				break;
			}

			case TASK_MSG_DL_SYNCML_ABORT:
			{
				tsLib.debugPrint(DEBUG_TASK, "TASK_MSG_DL_SYNCML_ABORT");

				if (msgItem.param == null)
					return false;

				tsDmParamAbortmsg pDLAbortParam = (tsDmParamAbortmsg) msgItem.param.param;

				if (pDLAbortParam.abortCode == TASK_ABORT_USER_REQ)
				{
					if (!tsService.isNetworkConnect())
					{
						dmAgent.dmAgentSetSyncMode(DM_SYNC_NONE);
						dlAgent.dltpSetRetryCount(TP_RETRY_COUNT_NONE);
						tsMsgEvent.SetMsgEvent(null, DM_EVENT_UI_DOWNLOAD_FAILED_WIFI_DISCONNECTED);
						return false;
					}
					
					tsdmDB.dmdbSetDmAgentType(SYNCML_DM_AGENT_FUMO);
					tsdmDB.dmdbSetFUMOStatus(DM_FUMO_STATE_NONE);
					tsdmDB.dmdbSetFUMOUpdateMechanism(DM_FUMO_MECHANISM_NONE);

					dmAgent.dmAgentSetUserInitiatedStatus(false);
					dmAgent.dmAgentSetServerInitiatedStatus(false);
					dmAgent.dmAgentClose();
					
					rc = dlagent.dltpAbort(SYNCMLDL);
					if (rc >= TP_RET_OK)
					{
						dlagent.dltpClose(SYNCMLDL);
						netHttpAdapter.netAdpSetReuse(false);
						dlagent.dltpCloseNetWork(SYNCMLDL);
					}
					else
					{
						tsLib.debugPrint(DEBUG_TASK, "TASK_MSG_DL_SYNCML_ABORT, !rc >= TP_RET_OK");
					}
				}
				if (pDLAbortParam.abortCode == TP_ECODE_HTTP_RETURN_STATUS_ERROR)
				{
					tsLib.debugPrint(DEBUG_TASK, "TP_ECODE_HTTP_RETURN_STATUS_ERROR, not implement...");
					dmAgentHandler.dmAgentClose();
					rc = dlagent.dltpAbort(SYNCMLDL);
					if (rc >= 0)
					{
						dlagent.dltpClose(SYNCMLDL);
						netHttpAdapter.netAdpSetReuse(false);
						dlagent.dltpCloseNetWork(SYNCMLDL);

						dmAgent.dmAgentSetSyncMode(DM_SYNC_NONE);
						dlAgent.dltpSetRetryCount(TP_RETRY_COUNT_NONE);
					}

					int nfumostatus = tsdmDB.dmdbGetFUMOStatus();
					if (nfumostatus == DM_FUMO_STATE_DOWNLOAD_COMPLETE)
					{
						tsdmDB.dmdbSetFUMOStatus(DM_FUMO_STATE_READY_TO_UPDATE);
						tsMsgEvent.SetMsgEvent(null, DL_EVENT_UI_DOWNLOAD_IN_COMPLETE);
					}
					else
					{
						// send generic alert for fail to download package
						// cause HTTP response error
						tsLib.debugPrint(DEBUG_TASK, "send generic alert for fail to download package");
						tsdmDB.dmdbSetFUMOResultCode(DL_GENERIC_SERVER_ERROR);
						tsdmDB.dmdbSetFUMOStatus(DM_FUMO_STATE_DOWNLOAD_FAILED_REPORTING);
						tsDmMsg.taskSendMessage(TASK_MSG_DM_SYNCML_CONNECT, null, null);

						tsMsgEvent.SetMsgEvent(null, DM_EVENT_UI_SERVER_CONNECT_FAIL);
					}
				}
				break;
			}

			case TASK_MSG_DL_SYNCML_CONNECTRETRY:
				tsLib.debugPrint(DEBUG_TASK, "TASK_MSG_DL_SYNCML_CONNECTRETRY");
				break;

			case TASK_MSG_DL_TCPAPN_OPEN:
				tsLib.debugPrint(DEBUG_TASK, "TASK_MSG_DL_TCPAPN_OPEN");
				{
				int ret = 0;
				ret = dlagent.gHttpDLAdapter.tpApnOpen(SYNCMLDL);
				if (ret == TP_RET_CONNECTION_FAIL)
				{
					tsDmMsg.taskSendMessage(TASK_MSG_DL_SYNCML_CONNECTFAIL, null, null);
				}
				break;
				}

			case TASK_MSG_DL_TCPIP_OPEN:
				tsLib.debugPrint(DEBUG_TASK, "TASK_MSG_DL_TCPIP_OPEN");
				int ret = TP_RET_OK;
				try
				{
					ret = dlagent.gHttpDLAdapter.tpOpen(SYNCMLDL);
				}
				catch (SocketTimeoutException e)
				{
					tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
					netTimerConnect.endTimer();
					ret = TP_RET_CONNECTION_FAIL;
				}
				if (ret == TP_RET_OK)
					tsDmMsg.taskSendMessage(TASK_MSG_DL_SYNCML_START, null, null);
				else
					tsDmMsg.taskSendMessage(TASK_MSG_DL_SYNCML_CONNECTFAIL, null, null);

				break;
			case TASK_MSG_DL_TCPIP_SEND:
				tsLib.debugPrint(DEBUG_TASK, "TASK_MSG_DL_TCPIP_SEND");
				break;
			case TASK_MSG_DL_TCPIP_CLOSE:
				tsLib.debugPrint(DEBUG_TASK, "TASK_MSG_DL_TCPIP_CLOSE");
				dlagent.dltpClose(SYNCMLDL);
				dlagent.dltpCloseNetWork(SYNCMLDL);
				break;
			case TASK_MSG_DL_SYNCML_START:
				tsLib.debugPrint(DEBUG_TASK, "TASK_MSG_DL_SYNCML_START");
				dlagent.dlagenthandler.dlAgntHdlrStartOMADLAgent(TASK_MSG_DL_SYNCML_START);
				break;
			case TASK_MSG_DL_SYNCML_CONTINUE:
				tsLib.debugPrint(DEBUG_TASK, "TASK_MSG_DL_SYNCML_CONTINUE");
				dlagent.dlagenthandler.dlAgntHdlrStartOMADLAgent(TASK_MSG_DL_SYNCML_CONTINUE);
				break;

			case TASK_MSG_DL_SOCKET_SSL_TUNNEL_CONNECT:
				tsLib.debugPrint(DEBUG_TASK, "TASK_MSG_DL_SOCKET_SSL_TUNNEL_CONNECT");
				break;
			case TASK_MSG_DL_SYNCML_FINISH:
			{
				tsLib.debugPrint(DEBUG_TASK, "TASK_MSG_DL_SYNCML_FINISH");
				rc = dlagent.dltpAbort(SYNCMLDL);
				if (rc >= 0)
				{
					dlagent.dltpClose(SYNCMLDL);
					dlagent.dltpCloseNetWork(SYNCMLDL);
				}
				dlAgent.dltpSetRetryCount(TP_RETRY_COUNT_NONE);
				dmAgent.dmAgentSetSyncMode(DM_SYNC_NONE);

				int nStatus = DM_FUMO_STATE_NONE;
				nStatus = tsdmDB.dmdbGetFUMOStatus();

				if (nStatus == DM_FUMO_STATE_DOWNLOAD_DESCRIPTOR)
				{
					tsMsgEvent.SetMsgEvent(null, DL_EVENT_UI_DOWNLOAD_YES_NO);
				}

				/* IOT Issue */
				if (_SYNCML_TS_DM_VERSION_V12_)
				{
					tsdmDB.dmdbSetNotiReSyncMode(DM_NOTI_RESYNC_MODE_FALSE);
				}
				if (nStatus == DM_FUMO_STATE_READY_TO_UPDATE || nStatus == DM_FUMO_STATE_DOWNLOAD_COMPLETE)
				{
					tsMsgEvent.SetMsgEvent(null, DL_EVENT_UI_UPDATE_START);
				}
				break;
			}

			case TASK_MSG_DL_USER_CANCEL_DOWNLOAD:
				tsLib.debugPrint(DEBUG_TASK, "TASK_MSG_DL_USER_CANCEL_DOWNLOAD");
				tsdmDB.dmdbSetFUMOResultCode(DL_USER_CANCELED_DOWNLOAD);
				tsdmDB.dmdbSetFUMOStatus(DM_FUMO_STATE_DOWNLOAD_IN_CANCEL);
				tsDmMsg.taskSendMessage(TASK_MSG_DM_SYNCML_CONNECT, null, null);
				break;

			case TASK_MSG_DL_DOWNLOAD_FILE_ERROR:
				tsLib.debugPrint(DEBUG_TASK, "TASK_MSG_DL_DOWNLOAD_FILE_ERROR"+" "+ tsService.downloadFileFailCause);
				tsdmDB.dmdbSetFUMOResultCode(DL_GENERIC_DOWNLOAD_FILE_ERROR+" "+ tsService.downloadFileFailCause);
				//tsdmDB.dmdbSetFUMOResultCode(DL_GENERIC_DOWNLOAD_FILE_ERROR);
				tsdmDB.dmdbSetFUMOStatus(DM_FUMO_STATE_UPDATE_SUCCESSFUL_NODATA);
				tsdmDB.dmdbSetDmAgentType(SYNCML_DM_AGENT_FUMO);
				tsDmMsg.taskSendMessage(TASK_MSG_DM_SYNCML_START, null, null);
				break;

			case TASK_MSG_DL_FIRMWARE_UPDATE_STANDBY:
				tsLib.debugPrint(DEBUG_TASK, "TASK_MSG_DL_FIRMWARE_UPDATE_STANDBY");
				tsdmDB.dmdbSetFUMOResultCode(UPDATE_STANDBY);
				tsdmDB.dmdbSetFUMOStatus(DM_FUMO_STATE_UPDATE_SUCCESSFUL_NODATA);
				tsdmDB.dmdbSetDmAgentType(SYNCML_DM_AGENT_FUMO);
				tsDmMsg.taskSendMessage(TASK_MSG_DM_SYNCML_START, null, null);
				break;

			case TASK_MSG_DL_FIRMWARE_UPDATE_START:
				tsLib.debugPrint(DEBUG_TASK, "TASK_MSG_DL_FIRMWARE_UPDATE_START updateType= " +tsService.updateType);
				if(tsService.updateType == 1) //PAS
					tsdmDB.dmdbSetFUMOResultCode(UPDATE_START_REBOOT);
				else if(tsService.updateType == 2) //AVNT
					tsdmDB.dmdbSetFUMOResultCode(UPDATE_START);
				tsdmDB.dmdbSetFUMOStatus(DM_FUMO_STATE_UPDATE_SUCCESSFUL_NODATA);
				tsdmDB.dmdbSetDmAgentType(SYNCML_DM_AGENT_FUMO);
				tsDmMsg.taskSendMessage(TASK_MSG_DM_SYNCML_START, null, null);
				break;

			case TASK_MSG_DL_FIRMWARE_UPDATE_SUCCESS:
				tsLib.debugPrint(DEBUG_TASK, "TASK_MSG_DL_FIRMWARE_UPDATE_SUCCESS updateType= " +tsService.updateType);
				tsdmDB.dmdbSetFUMOResultCode(UPDATE_SUCCESS);
				tsdmDB.dmdbSetFUMOStatus(DM_FUMO_STATE_UPDATE_SUCCESSFUL_NODATA);
				tsdmDB.dmdbSetDmAgentType(SYNCML_DM_AGENT_FUMO);
				tsDmMsg.taskSendMessage(TASK_MSG_DM_SYNCML_START, null, null);
				break;

			case TASK_MSG_DL_FIRMWARE_UPDATE_FAIL:
				tsLib.debugPrint(DEBUG_TASK, "TASK_MSG_DL_FIRMWARE_UPDATE_FAIL updateType= " +tsService.updateType+" uploadFailCause="+ tsService.uploadFailCause);
				tsdmDB.dmdbSetFUMOResultCode(UPDATE_FAIL+" "+ tsService.uploadFailCause);
				//tsdmDB.dmdbSetFUMOResultCode(UPDATE_FAIL);
				tsdmDB.dmdbSetFUMOStatus(DM_FUMO_STATE_UPDATE_SUCCESSFUL_NODATA);
				tsdmDB.dmdbSetDmAgentType(SYNCML_DM_AGENT_FUMO);
				tsDmMsg.taskSendMessage(TASK_MSG_DM_SYNCML_START, null, null);
				break;

			case TASK_MSG_DL_FIRMWARE_UPDATE_USERCANCEL:
				tsLib.debugPrint(DEBUG_TASK, "TASK_MSG_DL_FIRMWARE_UPDATE_USERCANCEL updateType= " +tsService.updateType);
				tsdmDB.dmdbSetFUMOResultCode(UPDATE_USER_CANCELED);
				tsdmDB.dmdbSetFUMOStatus(DM_FUMO_STATE_UPDATE_SUCCESSFUL_NODATA);
				tsdmDB.dmdbSetDmAgentType(SYNCML_DM_AGENT_FUMO);
				tsDmMsg.taskSendMessage(TASK_MSG_DM_SYNCML_START, null, null);
				break;

			case TASK_MSG_DL_FIRMWARE_UPDATE:
				tsLib.debugPrint(DEBUG_TASK, "TASK_MSG_DL_FIRMWARE_UPDATE");
				break;
			case TASK_MSG_DL_LOW_BATTERY_BEFORE_DOWNLOAD:
				tsLib.debugPrint(DEBUG_TASK, "TASK_MSG_DL_LOW_BATTERY_BEFORE_DOWNLOAD");
				break;
			case TASK_MSG_DL_POSTPONE:
				tsLib.debugPrint(DEBUG_TASK, "TASK_MSG_DL_POSTPONE");
				break;
			case TASK_MSG_UIC_REQUEST:
			{
				tsLib.debugPrint(DEBUG_TASK, "TASK_MSG_UIC_REQUEST");
				tsDmUicOption pSrcUICOption = (tsDmUicOption) msgItem.param.param;

				tsDmUicOption pUicOption = null;
				tsDmUicResult pUicResultKeep = null;
				int eUicResultKeepFlag = UIC_SAVE_NONE;

				pUicResultKeep = (tsDmUicResult) tsDmUic.dmGetUicResultKeep(eUicResultKeepFlag);
				{
					tsLib.debugPrint(DEBUG_TASK, "TASK_MSG_UIC_REQUEST else");
					pUicOption = tsDmUic.dmUicCreateUicOption();
					pUicOption = tsDmUic.dmUicCopyUicOption(pUicOption, pSrcUICOption);
					tsDmMsg.uiSendMessage(DM_EVENT_UI_UIC_REQUEST, pUicOption, null);
				}
				tsDmUic.dmUicFreeUicResult(pUicResultKeep);
				break;
			}
			case TASK_MSG_UIC_RESPONSE:
				tsLib.debugPrint(DEBUG_TASK, "TASK_MSG_UIC_RESPONSE");

				tsDmUicResult pUicResult = (tsDmUicResult) msgItem.param.param;
				pUicResult = tsDmUic.dmSetUicResultKeep(pUicResult, UIC_SAVE_OK);
				dmAgentHandler dmAgtHandler = new dmAgentHandler();
				dmAgtHandler.dmAgntHdlrContinueSession(TASK_MSG_UIC_RESPONSE, pUicResult);
				break;

			default:
				break;
		}

		msg = null;
		return false;
	}
	
	public void dmUITaskUserCancelDownload(int appId)
	{
		tsLib.debugPrint(DEBUG_DM, "appId : " + appId);

		if(appId == SYNCMLDM)
		{
			agent.tpClose(SYNCMLDM);
			netHttpAdapter.netAdpSetReuse(false);
			agent.tpCloseNetWork(SYNCMLDM);

			dmAgent.dmAgentSetSyncMode(DM_SYNC_NONE);
			dmAgent.tpSetRetryCount(TP_RETRY_COUNT_NONE);
			return;
		}
		else // during SYNCMLDL session
		{
			dlagent.dltpClose(SYNCMLDL);
			netHttpAdapter.netAdpSetReuse(false);
			dlagent.dltpCloseNetWork(SYNCMLDL);

			dmAgent.dmAgentSetSyncMode(DM_SYNC_NONE);
			dlAgent.dltpSetRetryCount(TP_RETRY_COUNT_NONE);
		}

		// send generic alert for fail to download package
		// cause User cancel download		
		tsLib.debugPrint(DEBUG_TASK, "send generic alert for fail to download package");
		tsdmDB.dmdbSetFUMOResultCode(DL_USER_CANCELED_DOWNLOAD);
		tsdmDB.dmdbSetFUMOStatus(DM_FUMO_STATE_USER_CANCEL_REPORTING);
		tsDmMsg.taskSendMessage(TASK_MSG_DM_SYNCML_CONNECT, null, null);
	}

	public void dmTaskdmXXXFail()
	{
		tsLib.debugPrint(DEBUG_TASK,"dmTaskdmXXXFail");
		agent.tpClose(SYNCMLDM);
		agent.tpCloseNetWork(SYNCMLDM);
	}

	public void dmTaskdlXXXFail()
	{
		tsLib.debugPrint(DEBUG_TASK, "dmTaskdlXXXFail");
    	dlagent.dltpClose(SYNCMLDL);
    	dlagent.dltpCloseNetWork(SYNCMLDL);
	}
}
