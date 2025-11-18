package com.tsdm.agent;

import java.net.SocketTimeoutException;

import com.tsdm.core.data.constants.DmDevInfoConst;
import com.tsdm.core.data.constants.DmTaskMsg;
import com.tsdm.core.data.constants.DmUiEvent;
import com.tsdm.core.data.constants.FumoConst;
import com.tsdm.core.data.constants.UserInteractionCommandConst;
import com.tsdm.db.tsDefineDB;
import com.tsdm.db.tsdmDB;
import com.tsdm.adapt.tsList;
import com.tsdm.adapt.tsDmWorkspace;
import com.tsdm.adapt.tsDmParamAbortmsg;
import com.tsdm.adapt.tsMsgEvent;
import com.tsdm.adapt.tsLib;
import com.tsdm.adapt.tsDmMsg;
import com.tsdm.adapt.tsDmParserItem;
import com.tsdm.adapt.tsDmParserPcdata;
import com.tsdm.adapt.tsDmUic;
import com.tsdm.adapt.tsDmUicOption;
import com.tsdm.adapt.tsDmUicResult;
import com.tsdm.net.NetConsts;
import com.tsdm.net.netHttpAdapter;
import com.tsdm.net.netTimerConnect;
import com.tsdm.net.netTimerReceive;

public class dmAgentHandler extends dmAgent implements tsDefineDB
{
	public void dmAgnetHandlerContinueSessionDmStart()
	{
		int nRet;

		tsLib.debugPrint(DmDevInfoConst.DEBUG_DM, "");

		// DM Session should be kept,  even if Change protocol case.
		// So, DM client should only reconnect to new server, 
		// and there's no need to create new package. 
		if (dmAgentGetWorkSpace() != null && tsdmDB.dmdbGetChangedProtocol())
		{
			tsLib.debugPrint(DmDevInfoConst.DEBUG_DM, "do not create new package");
		}
		else
		{
			nRet = dmAgentStartSession();
			if (nRet != DmDevInfoConst.SDM_RET_OK)
			{
				dmAgntHdlrAbortSession(DmDevInfoConst.SDMABORT_PARSING_FAILURE);
				return;
			}

			dmAgentMakeAppNode();

			if (dmAgentCreatePackage() != DmDevInfoConst.SDM_RET_OK)
			{
				dmAgntHdlrAbortSession(DmDevInfoConst.SDMABORT_PARSING_FAILURE);
				return;
			}
		}

		try
		{
			nRet = gHttpDMAdapter.tpOpen(DmDevInfoConst.SYNCMLDM);
		}
		catch (SocketTimeoutException e)
		{
			tsLib.debugPrintException(DmDevInfoConst.DEBUG_EXCEPTION, e.toString());
			netTimerConnect.endTimer();
			nRet = DmDevInfoConst.SDM_RET_CONNECT_FAIL;
		}

		if (nRet != DmDevInfoConst.SDM_RET_OK)
		{
			tsDmMsg.taskSendMessage(DmTaskMsg.TASK_MSG_DM_SYNCML_CONNECTFAIL, null, null);
			return;
		}

		nRet = dmAgentSendPackage();
		if (nRet == DmDevInfoConst.SDM_RET_OK)
			tsMsgEvent.SetMsgEvent(null, DmUiEvent.EVENT_UI_SERVER_CONNECT);

		if (nRet == DmDevInfoConst.SDM_RET_CHANGED_PROFILE)
		{
			tsdmDB.dmdbSetChangedProtocol(true);
			tsDmMsg.taskSendMessage(DmTaskMsg.TASK_MSG_DM_SYNCML_FINISH, null, null);
			tsDmMsg.taskSendMessage(DmTaskMsg.TASK_MSG_DM_SYNCML_CONNECT, null, null);
		}
	}

	public void dmAgnetHandlerContinueSessionFumoStart()
	{
		int nStatus;
		int nFileId = 0;
		int nUpdateMechanism;
		int nRet;

		tsLib.debugPrint(DmDevInfoConst.DEBUG_DM, "");

		nStatus = tsdmDB.dmdbGetFUMOStatus();
		tsLib.debugPrint(DmDevInfoConst.DEBUG_DM, "nStatus [" + nStatus + "]");

		if ((nStatus == FumoConst.DM_FUMO_STATE_UPDATE_SUCCESSFUL_NODATA) || (nStatus == FumoConst.DM_FUMO_STATE_UPDATE_SUCCESSFUL_HAVEDATA)
				|| (nStatus == FumoConst.DM_FUMO_STATE_UPDATE_FAILED_NODATA) || (nStatus == FumoConst.DM_FUMO_STATE_UPDATE_FAILED_HAVEDATA)
				|| (nStatus == FumoConst.DM_FUMO_STATE_UPDATE_IN_PROGRESS) || (nStatus == FumoConst.DM_FUMO_STATE_USER_CANCEL_REPORTING)
				|| (nStatus == FumoConst.DM_FUMO_STATE_DOWNLOAD_FAILED_REPORTING))
		{
			nFileId = tsdmDB.dmdbGetFileIdFirmwareData();
			tsdmDB.dmdbDeleteFile(nFileId);

			nUpdateMechanism = tsdmDB.dmdbGetFUMOUpdateMechanism();
			if (nUpdateMechanism == FumoConst.DM_FUMO_MECHANISM_ALTERNATIVE_DOWNLOAD)
			{
				nRet = dmAgentStartGeneralAlert(FumoConst.DM_FUMO_MECHANISM_UPDATE);
				if (nRet == DmDevInfoConst.SDM_RET_FAILED)
				{
					dmAgntHdlrAbortSession(DmDevInfoConst.SDMABORT_PARSING_FAILURE);
					return;
				}
				else if (nRet == DmDevInfoConst.SDM_RET_CONNECT_FAIL)
				{
					tsDmMsg.taskSendMessage(DmTaskMsg.TASK_MSG_DM_SYNCML_CONNECTFAIL, null, null);
					return;
				}
			}
			else
			{
				nRet = dmAgentStartGeneralAlert(FumoConst.DM_FUMO_MECHANISM_ALTERNATIVE);
				if (nRet == DmDevInfoConst.SDM_RET_FAILED)
				{
					dmAgntHdlrAbortSession(DmDevInfoConst.SDMABORT_PARSING_FAILURE);
					return;
				}
				else if (nRet == DmDevInfoConst.SDM_RET_CONNECT_FAIL)
				{
					tsDmMsg.taskSendMessage(DmTaskMsg.TASK_MSG_DM_SYNCML_CONNECTFAIL, null, null);
					return;
				}
			}
		}
		else
		{
			nUpdateMechanism = tsdmDB.dmdbGetFUMOUpdateMechanism();
			if (nUpdateMechanism == FumoConst.DM_FUMO_MECHANISM_ALTERNATIVE_DOWNLOAD)
			{
				nRet = dmAgentStartGeneralAlert(FumoConst.DM_FUMO_MECHANISM_ALTERNATIVE_DOWNLOAD);
				if (nRet == DmDevInfoConst.SDM_RET_FAILED)
				{
					dmAgntHdlrAbortSession(DmDevInfoConst.SDMABORT_PARSING_FAILURE);
					return;
				}
				else if (nRet == DmDevInfoConst.SDM_RET_CONNECT_FAIL)
				{
					tsDmMsg.taskSendMessage(DmTaskMsg.TASK_MSG_DM_SYNCML_CONNECTFAIL, null, null);
					return;
				}
			}
			else
			{
				nRet = dmAgentStartSession();
				if (nRet != DmDevInfoConst.SDM_RET_OK)
				{
					dmAgntHdlrAbortSession(DmDevInfoConst.SDMABORT_PARSING_FAILURE);
					return;
				}

				dmAgentMakeAppNode();

				if (dmAgentCreatePackage() != DmDevInfoConst.SDM_RET_OK)
				{
					dmAgntHdlrAbortSession(DmDevInfoConst.SDMABORT_PARSING_FAILURE);
					return;
				}

				try
				{
					nRet = gHttpDMAdapter.tpOpen(DmDevInfoConst.SYNCMLDM);
				}
				catch (SocketTimeoutException e)
				{
					tsLib.debugPrintException(DmDevInfoConst.DEBUG_EXCEPTION, e.toString());
					netTimerConnect.endTimer();
					nRet = DmDevInfoConst.SDM_RET_CONNECT_FAIL;
				}

				if (nRet != DmDevInfoConst.SDM_RET_OK)
				{
					tsDmMsg.taskSendMessage(DmTaskMsg.TASK_MSG_DM_SYNCML_CONNECTFAIL, null, null);
					return;
				}

				nRet = dmAgentSendPackage();
				if (nRet == DmDevInfoConst.SDM_RET_OK)
					tsMsgEvent.SetMsgEvent(null, DmUiEvent.EVENT_UI_SERVER_CONNECT);
			}
		}

		if (nRet == DmDevInfoConst.SDM_RET_CHANGED_PROFILE)
		{
			tsdmDB.dmdbSetChangedProtocol(true);
			tsDmMsg.taskSendMessage(DmTaskMsg.TASK_MSG_DM_SYNCML_FINISH, null, null);
			tsDmMsg.taskSendMessage(DmTaskMsg.TASK_MSG_DM_SYNCML_CONNECT, null, null);
		}
	}

	public void dmAgntHdlrContinueSession(int nEvent, Object pData)
	{
		int nRet = DmDevInfoConst.SDM_RET_OK;
		int nStatus;
		tsDmWorkspace ws = null;
		int nAgentType = DmDevInfoConst.SYNCML_DM_AGENT_DM;
		int nSyncMode = DmDevInfoConst.DM_RESULT_REPORT_GENRIC_COMPLETE;

		tsLib.debugPrint(DmDevInfoConst.DEBUG_DM, "");
		ws = (tsDmWorkspace) dmAgentGetWorkSpace();
		if (ws == null)
		{
			tsLib.debugPrint(DmDevInfoConst.DEBUG_DM, "!ws WARNING");
		}

		switch (nEvent)
		{
			case DmTaskMsg.TASK_MSG_DM_SYNCML_START:
			{
				nAgentType = tsdmDB.dmdbGetDmAgentType();
				tsLib.debugPrint(DmDevInfoConst.DEBUG_DM, "TASK_MSG_DM_SYNCML_START nAgentType : " + nAgentType);
				if (nAgentType == DmDevInfoConst.SYNCML_DM_AGENT_FUMO)
				{
					dmAgnetHandlerContinueSessionFumoStart();
				}
				else
				{
					dmAgnetHandlerContinueSessionDmStart();
				}
				break;
			}

			case DmTaskMsg.TASK_MSG_DM_SYNCML_CONTINUE:
			{
				tsLib.debugPrint(DmDevInfoConst.DEBUG_DM, "TASK_MSG_DM_SYNCML_CONTINUE");
				if (ws == null)
				{
					tsLib.debugPrint(DmDevInfoConst.DEBUG_DM, "ws TASK_MSG_DM_SYNCML_CONTINUE WARNING");
					return;
				}
				ws.procState = DmDevInfoConst.SyncmlProcessingState.PROC_NONE;
				ws.buf.reset();
				
				try
				{
					nRet = gHttpDMAdapter.tpReceiveData(ws.buf, DmDevInfoConst.SYNCMLDM);
				}
				catch (SocketTimeoutException e)
				{
					tsLib.debugPrintException(DmDevInfoConst.DEBUG_EXCEPTION, e.toString());
					netTimerReceive.endTimer();
					nRet = NetConsts.TP_RET_RECEIVE_FAIL;
				}
				catch (NullPointerException e)
				{
					tsLib.debugPrintException(DmDevInfoConst.DEBUG_EXCEPTION, e.toString());
					netTimerReceive.endTimer();
					nRet = NetConsts.TP_RET_RECEIVE_FAIL;
				}
				catch (Exception e)
				{
					tsLib.debugPrintException(DmDevInfoConst.DEBUG_EXCEPTION, e.toString());
					netTimerReceive.endTimer();
					nRet = NetConsts.TP_RET_RECEIVE_FAIL;
				}

				if (ws.buf == null)
				{
					dmAgntHdlrAbortSession(DmDevInfoConst.SDMABORT_PARSING_FAILURE);
					break;
				}
				ws.recvHmacData = gHttpDMAdapter.getCurHMACData();

				if (nRet == NetConsts.TP_RET_HTTP_RES_FAIL)
				{
					tsDmParamAbortmsg pAbortParam;
					pAbortParam = tsDmMsg.createAbortMessage(NetConsts.TP_ECODE_HTTP_RETURN_STATUS_ERROR, false);
					tsDmMsg.taskSendMessage(DmTaskMsg.TASK_DM_SYNCML_ABORT, pAbortParam, null);
					return;
				}
				else if (nRet != DmDevInfoConst.SDM_RET_OK)
				{
					tsDmMsg.taskSendMessage(DmTaskMsg.TASK_MSG_DM_SYNCML_RECEIVEFAIL, null, null);
					return;
				}

				nRet = dmAgentStartMgmtSession();
				if (nRet == DmDevInfoConst.SDM_RET_FAILED)
				{
					tsLib.debugPrint(DmDevInfoConst.DEBUG_DM, "DmDevInfoConst.SDM_RET_FAILED");
					dmAgntHdlrAbortSession(DmDevInfoConst.SDMABORT_PARSING_FAILURE);
					break;
				}
				else if (nRet == DmDevInfoConst.SDM_RET_AUTH_MAX_ERROR)
				{
					tsLib.debugPrint(DmDevInfoConst.DEBUG_DM, "DmDevInfoConst.SDM_RET_AUTH_MAX_ERROR");
					dmAgntHdlrAbortSession(DmDevInfoConst.SDMABORT_AUTHENTICATION_FAILURE);
					break;
				}
				else if (nRet == DmDevInfoConst.SDM_ALERT_SESSION_ABORT)
				{
					tsLib.debugPrint(DmDevInfoConst.DEBUG_DM, "DmDevInfoConst.SDM_ALERT_SESSION_ABORT");
					dmAgntHdlrDestroySession();
					break;
				}
				else if (nRet == DmDevInfoConst.SDM_RET_FINISH)
				{
					tsLib.debugPrint(DmDevInfoConst.DEBUG_DM, "DmDevInfoConst.SDM_RET_FINISH");
					tsLib.debugPrint(DmDevInfoConst.DEBUG_DM, "no action command finish session");

					nStatus = tsdmDB.dmdbGetFUMOStatus();
					if ((nStatus == FumoConst.DM_FUMO_STATE_UPDATE_SUCCESSFUL_NODATA)
						|| (nStatus == FumoConst.DM_FUMO_STATE_UPDATE_SUCCESSFUL_HAVEDATA)
						|| (nStatus == FumoConst.DM_FUMO_STATE_UPDATE_FAILED_NODATA)
						|| (nStatus == FumoConst.DM_FUMO_STATE_UPDATE_FAILED_HAVEDATA)
						|| (nStatus == FumoConst.DM_FUMO_STATE_UPDATE_IN_PROGRESS)
						|| (nStatus == FumoConst.DM_FUMO_STATE_USER_CANCEL_REPORTING)
						|| (nStatus == FumoConst.DM_FUMO_STATE_DOWNLOAD_FAILED_REPORTING))
					{
						tsLib.debugPrint(DmDevInfoConst.DEBUG_DL, "dmdbGetFUMOResultCode " + tsdmDB.dmdbGetFUMOResultCode());

						tsdmDB.dmdbSetDmAgentType(DmDevInfoConst.SYNCML_DM_AGENT_DM);
						tsdmDB.dmdbSetFUMOStatus(FumoConst.DM_FUMO_STATE_NONE);
						tsdmDB.dmdbSetFUMOUpdateMechanism(FumoConst.DM_FUMO_MECHANISM_NONE);
					}

					dmAgntHdlrDestroySession();
				}
				else if (nRet == DmDevInfoConst.SDM_RET_ABORT)
				{
					tsLib.debugPrint(DmDevInfoConst.DEBUG_DM, "SDM_RET_ABORT");
					tsDmParamAbortmsg pAbortParam;
					pAbortParam = tsDmMsg.createAbortMessage(DmTaskMsg.TASK_ABORT_SYNC_RETRY, false);
					tsDmMsg.taskSendMessage(DmTaskMsg.TASK_DM_SYNCML_ABORT, pAbortParam, null);
				}
				else if (nRet == DmDevInfoConst.SDM_PAUSED_BECAUSE_UIC_COMMAND)
				{
					tsLib.debugPrint(DmDevInfoConst.DEBUG_DM, "SDM_PAUSED_BECAUSE_UIC_COMMAND");
					dmAgntHdlrlUicSendEvent(null, ws.uicOption);
				}
				else if (nRet == DmDevInfoConst.SDM_RET_EXEC_ALTERNATIVE)
				{
					tsLib.debugPrint(DmDevInfoConst.DEBUG_DM, "SDM_RET_EXEC_ALTERNATIVE");
					tsLib.debugPrint(DmDevInfoConst.DEBUG_DM, "Connect to the Contents Server");

					tsDmMsg.taskSendMessage(DmTaskMsg.TASK_MSG_DM_SYNCML_FINISH, null, null);
					tsDmMsg.taskSendMessage(DmTaskMsg.TASK_MSG_DL_SYNCML_CONNECT, null, null);
				}
				else if (nRet == DmDevInfoConst.SDM_RET_EXEC_REPLACE)
				{

				}
				else if (nRet == DmDevInfoConst.SDM_RET_EXEC_DOWNLOAD_COMPLETE)
				{
					dmAgntHdlrDestroySession();
					tsMsgEvent.SetMsgEvent(null, DmUiEvent.DL_EVENT_UI_DOWNLOAD_IN_COMPLETE);
				}
				else if (nRet == DmDevInfoConst.SDM_RET_EXEC_ALTERNATIVE_DOWNLOAD)
				{
					tsLib.debugPrint(DmDevInfoConst.DEBUG_DM, "SDM_RET_EXEC_ALTERNATIVE_DOWNLOAD");
					tsDmMsg.taskSendMessage(DmTaskMsg.TASK_MSG_DM_SYNCML_FINISH, null, null);
					tsDmMsg.taskSendMessage(DmTaskMsg.TASK_MSG_DL_SYNCML_CONNECT, null, null);
				}
				else if (nRet == DmDevInfoConst.SDM_RET_EXEC_ALTERNATIVE_UPDATE)
				{
					tsLib.debugPrint(DmDevInfoConst.DEBUG_DM, "SDM_RET_EXEC_ALTERNATIVE_UPDATE");
					tsDmMsg.taskSendMessage(DmTaskMsg.TASK_MSG_DM_SYNCML_FINISH, null, null);
					tsdmDB.dmdbSetFUMOStatus(FumoConst.DM_FUMO_STATE_READY_TO_UPDATE);
					try
					{
						Thread.sleep(1000);
					}
					catch (InterruptedException e)
					{
						tsLib.debugPrintException(DmDevInfoConst.DEBUG_EXCEPTION, e.toString());
						Thread.currentThread().interrupt();
					}
					tsMsgEvent.SetMsgEvent(null, DmUiEvent.DL_EVENT_UI_DOWNLOAD_IN_COMPLETE);
				}
				else if (nRet == DmDevInfoConst.SDM_RET_CHANGED_PROFILE)
				{
					tsLib.debugPrint(DmDevInfoConst.DEBUG_DM, "DmDevInfoConst.SDM_RET_CHANGED_PROFILE");
					tsdmDB.dmdbSetChangedProtocol(true);
					tsDmMsg.taskSendMessage(DmTaskMsg.TASK_MSG_DM_SYNCML_CONNECT, null, null);
				}
				break;
			}
			case DmTaskMsg.TASK_MSG_UIC_RESPONSE:
			{
				tsDmUicResult pUicResult;

				tsLib.debugPrint(DmDevInfoConst.DEBUG_DM, "TASK_MSG_UIC_RESPONSE");
				if (ws == null || pData == null)
				{
					tsLib.debugPrint(DmDevInfoConst.DEBUG_DM, "TASK_MSG_UIC_RESPONSE sdmContinueSession:  WARNING!!!!!!!!!!!");
					break;
				}
				pUicResult = (tsDmUicResult) pData;

				if (pUicResult.UICType == UserInteractionCommandConst.UIC_TYPE_CONFIRM)
				{
					if (pUicResult.result == UserInteractionCommandConst.UIC_RESULT_YES)
					{
						ws.uicFlag = DmDevInfoConst.SyncmlUICFlag.UIC_TRUE;
					}
					else if (pUicResult.result == UserInteractionCommandConst.UIC_RESULT_TIMEOUT)
					{
						if (ws.uicOption.defaultResponse.len != 0) // DR Exist
						{
							if (ws.uicOption.defaultResponse.text.compareTo("1") == 0) // 200
							{
								ws.uicFlag = DmDevInfoConst.SyncmlUICFlag.UIC_TRUE;
							}
							else if (ws.uicOption.defaultResponse.text.compareTo("0") == 0) // 304 ?? 215 ??
							{
								ws.uicFlag = DmDevInfoConst.SyncmlUICFlag.UIC_TIMEOUT; // 215
							}
							else
							{
								tsLib.debugPrint(DmDevInfoConst.DEBUG_DM, "UIC_TYPE_CONFIRM__&&__UIC_RESULT_TIMEOUT\n");
								ws.uicFlag = DmDevInfoConst.SyncmlUICFlag.UIC_FALSE;
							}
						}
						else
							{
							ws.uicFlag = DmDevInfoConst.SyncmlUICFlag.UIC_CANCELED;
						}

					}
					else
					// UIC_RESULT_REJECT . 304
					{
						ws.uicFlag = DmDevInfoConst.SyncmlUICFlag.UIC_FALSE;
					}
				}
				else if (pUicResult.UICType == UserInteractionCommandConst.UIC_TYPE_INPUT)
				{
					tsDmParserItem item = null;
					tsList h = null, t = null;

					tsLib.debugPrint(DmDevInfoConst.DEBUG_DM, " UIC_TYPE_INPUT input text :" + pUicResult.text.text + " UIC Result :" + pUicResult.result);

					if (pUicResult.result == UserInteractionCommandConst.UIC_RESULT_OK)
					{
						item = new tsDmParserItem();

						item.data = dmAgentHandlerString2pcdata(pUicResult.text.text.toCharArray());
						h = tsList.listAppend(h, t, item);
						ws.uicData = h;
						ws.uicFlag = DmDevInfoConst.SyncmlUICFlag.UIC_TRUE;

					}
					else if (pUicResult.result == UserInteractionCommandConst.UIC_RESULT_REJECT)
					{
						ws.uicFlag = DmDevInfoConst.SyncmlUICFlag.UIC_CANCELED;
					}
					else if (pUicResult.result == UserInteractionCommandConst.UIC_RESULT_TIMEOUT)
					{
						if (ws.uicOption.defaultResponse.len != 0 && (ws.uicOption.defaultResponse.text.compareTo("0") != 0)) // Timeout with DR.
						{
							item = new tsDmParserItem();
							pUicResult.text.text = ws.uicOption.defaultResponse.text;
							item.data = dmAgentHandlerString2pcdata(pUicResult.text.text.toCharArray());
							h = tsList.listAppend(h, t, item);
							ws.uicData = h;
							ws.uicFlag = DmDevInfoConst.SyncmlUICFlag.UIC_TRUE;
						}
						else
						// TimeOut without DF (think Cancel)
						{
							ws.uicFlag = DmDevInfoConst.SyncmlUICFlag.UIC_TIMEOUT;
						}
					}
					else
					// Cancel..
					{
						ws.uicFlag = DmDevInfoConst.SyncmlUICFlag.UIC_FALSE;
					}
				}
				else if ((pUicResult.UICType == UserInteractionCommandConst.UIC_TYPE_SINGLE_CHOICE) || (pUicResult.UICType == UserInteractionCommandConst.UIC_TYPE_MULTI_CHOICE))
				{
					tsList h = null, t = null;

					if (pUicResult.result == UserInteractionCommandConst.UIC_RESULT_SINGLE_CHOICE)
					{
						tsDmParserItem item = new tsDmParserItem();
						String DataText = "";

						if (pUicResult.SingleSelected > 0)
							DataText = String.valueOf(pUicResult.SingleSelected);

						item.data = dmAgentHandlerString2pcdata(DataText.toCharArray());
						h = tsList.listAppend(h, t, item);
						ws.uicData = h;
						ws.uicFlag = DmDevInfoConst.SyncmlUICFlag.UIC_TRUE;
					}
					else if (pUicResult.result == UserInteractionCommandConst.UIC_RESULT_MULTI_CHOICE)
					{
						int nCount = 0;

						for (nCount = 0; nCount < pUicResult.MenuNumbers; nCount++)
						{
							tsLib.debugPrint(DmDevInfoConst.DEBUG_DM, String.valueOf(pUicResult.MultiSelected[nCount]));
							if (pUicResult.MultiSelected[nCount] == 1)
							{
								tsDmParserItem item = new tsDmParserItem();
								String DataText = "";

								DataText = String.valueOf(nCount + 1);
								item.data = dmAgentHandlerString2pcdata(DataText.toCharArray()); // pUicResult.number;
								h = tsList.listAppend(h, t, item);
							}
						}
						if (h == null) // not select
						{
							tsDmParserItem item = new tsDmParserItem();
							h = tsList.listAppend(h, t, item);
						}

						ws.uicData = h;
						ws.uicFlag = DmDevInfoConst.SyncmlUICFlag.UIC_TRUE;
					}
					else if (pUicResult.result == UserInteractionCommandConst.UIC_RESULT_REJECT)
					{
						ws.uicFlag = DmDevInfoConst.SyncmlUICFlag.UIC_CANCELED;
					}
					else if (pUicResult.result == UserInteractionCommandConst.UIC_RESULT_TIMEOUT)
					{
						if (ws.uicOption.defaultResponse.len != 0 && (ws.uicOption.defaultResponse.text.compareTo("0") != 0)) // TimeOut with DR
						{
							if (pUicResult.UICType == UserInteractionCommandConst.UIC_RESULT_SINGLE_CHOICE)
							{
								tsDmParserItem item = new tsDmParserItem();

								item.data = dmAgentHandlerString2pcdata(ws.uicOption.defaultResponse.text.toCharArray()); // single choice DR string
								h = tsList.listAppend(h, t, item);
								ws.uicData = h;
								ws.uicFlag = DmDevInfoConst.SyncmlUICFlag.UIC_TRUE;
							}
							else
							// MULTI CHOICE
							{
								int nCount = 0;

								dmGetUICOptionDRMultiChoice(pUicResult);

								for (nCount = 0; nCount < pUicResult.MenuNumbers; nCount++)
								{
									tsLib.debugPrint(DmDevInfoConst.DEBUG_DM, String.valueOf(pUicResult.MultiSelected[nCount]));

									if (pUicResult.MultiSelected[nCount] == 1)
									{
										tsDmParserItem item = new tsDmParserItem();
										String DataText = "";
										DataText = String.valueOf(nCount + 1);
										item.data = dmAgentHandlerString2pcdata(DataText.toCharArray());
										h = tsList.listAppend(h, t, item);
									}
								}
								ws.uicData = h;
								ws.uicFlag = DmDevInfoConst.SyncmlUICFlag.UIC_TRUE;
							}
						}
						else
						// TimeOut without DR (think Cancel)
						{
							ws.uicFlag = DmDevInfoConst.SyncmlUICFlag.UIC_TIMEOUT;
						}
					}
					else
					{
						ws.uicFlag = DmDevInfoConst.SyncmlUICFlag.UIC_FALSE;
					}
				}
				else
				{
					ws.uicFlag = DmDevInfoConst.SyncmlUICFlag.UIC_NONE;
				}

				if (ws.uicOption != null)
				{
					tsDmUic.dmUicFreeUicOption(ws.uicOption);
					ws.uicOption = null;
				}

				nRet = dmAgentStartMgmtSession();

				if (nRet == DmDevInfoConst.SDM_RET_FAILED)
				{
					dmAgntHdlrAbortSession(DmDevInfoConst.SDMABORT_PARSING_FAILURE);
					break;
				}
				else if (nRet == DmDevInfoConst.SDM_ALERT_SESSION_ABORT) /* 1223 (UIC => other command ?) */
				{
					dmAgntHdlrDestroySession();
					break;
				}
				else if (nRet == DmDevInfoConst.SDM_RET_ABORT) /* Ping ping Status.(UIC => other command ?) */
				{
					tsDmParamAbortmsg pAbortParam = null;

					pAbortParam = tsDmMsg.createAbortMessage(DmTaskMsg.TASK_ABORT_SYNC_RETRY, false);
					tsDmMsg.taskSendMessage(DmTaskMsg.TASK_DM_SYNCML_ABORT, pAbortParam, null);
				}
				// else if(nRet == DmDevInfoConst.SDM_PAUSED_BECAUSE_UIC_COMMAND)
				// {
				// __smdmAgntHdlrlUicSendEvent(null, ws.uicOption);
				// }
				else if (nRet == DmDevInfoConst.SDM_RET_FINISH) // Crash fot App setting
				{
					tsLib.debugPrint(DmDevInfoConst.DEBUG_DM, "no action command finish session");

					dmAgntHdlrDestroySession();
					break;
				}
				else if (nRet == DmDevInfoConst.SDM_RET_AUTH_MAX_ERROR)
				{
					break;
				}
				else if (nRet == DmDevInfoConst.SDM_RET_CHANGED_PROFILE)
				{
					tsdmDB.dmdbSetChangedProtocol(true);
					tsDmMsg.taskSendMessage(DmTaskMsg.TASK_MSG_DM_SYNCML_CONNECT, null, null);
				}
				break;
			}
			case DmTaskMsg.TASK_MSG_DM_SYNCML_FINISH:
			{
				nAgentType = tsdmDB.dmdbGetDmAgentType();
				if (nAgentType == DmDevInfoConst.SYNCML_DM_AGENT_FUMO)
				{
					nStatus = tsdmDB.dmdbGetFUMOStatus();
				}
				else
				{
					nStatus = FumoConst.DM_FUMO_STATE_NONE;
				}

				tsLib.debugPrint(DmDevInfoConst.DEBUG_DM, "nStatus [" + nStatus + "]");

				if (nStatus == FumoConst.DM_FUMO_STATE_NONE)
				{
					if (dmAgentGetPendingStatus())
					{
						tsLib.debugPrint(DmDevInfoConst.DEBUG_DM, "DM_TASK_RETRY");
						dmAgentClose();
						break;
					}

					if (tsdmDB.dmdbGetChangedProtocol())
					{
						tsdmDB.dmdbBackUpServerUrl();
					}
					else
					{
						netHttpAdapter.httpCookieClear();
					}

					netHttpAdapter.netAdpSetReuse(false);

				}
				else if (nStatus == FumoConst.DM_FUMO_STATE_IDLE_START)
				{
					dmAgent.dmAgentSetServerInitiatedStatus(false);
					tsLib.debugPrint(DmDevInfoConst.DEBUG_DM, "TASK_MSG_DM_SYNCML_FINISH DM_FUMO_STATE_IDLE_START");
				}
				else
				{
					dmAgent.dmAgentSetServerInitiatedStatus(false);
					tsLib.debugPrint(DmDevInfoConst.DEBUG_DM, "TASK_MSG_DM_SYNCML_FINISH BUT not FINISH STATUS");
				}

				dmAgentClose();
				break;
			}

			default:
				break;
		}
	}

	public void dmAgntHdlrlUicSendEvent(Object pUserData, tsDmUicOption pUicOption)
	{
		tsDmUicOption pUicOptionDest = null;

		tsLib.debugPrint(DmDevInfoConst.DEBUG_DM, "");

		pUicOptionDest = tsDmUic.dmUicCreateUicOption();
		pUicOptionDest = tsDmUic.dmUicCopyUicOption(pUicOptionDest, pUicOption);

		tsDmMsg.taskSendMessage(DmTaskMsg.TASK_MSG_UIC_REQUEST, pUicOptionDest, null);
	}

	public void dmAgntHdlrDestroySession()
	{
		dmAgentSetSyncMode(DmDevInfoConst.DM_SYNC_COMPLETE);
		tsDmMsg.taskSendMessage(DmTaskMsg.TASK_MSG_DM_SYNCML_FINISH, null, null);
	}

	public void dmAgntHdlrAbortSession(int nReason)
	{
		tsDmParamAbortmsg pAbortParam = new tsDmParamAbortmsg();
		int nRet;
		tsLib.debugPrint(DmDevInfoConst.DEBUG_DM, " : AbortReason=[" + nReason + "]");
		nRet = tpGetHttpEcode();
		if (nRet != NetConsts.TP_ECODE_SOCKET_REMOTE_CLOSED)
		{
			// abort on generic alert
			pAbortParam = tsDmMsg.createAbortMessage(DmTaskMsg.TASK_ABORT_SYNCDM_ERROR, false);
			tsDmMsg.taskSendMessage(DmTaskMsg.TASK_DM_SYNCML_ABORT, pAbortParam, null);
		}
	}

	public void dmGetUICOptionDRMultiChoice(tsDmUicResult pData)
	{
		tsDmWorkspace ws = (tsDmWorkspace) dmAgentGetWorkSpace();
		if (ws == null)
		{ // Defects
			tsLib.debugPrintException(DmDevInfoConst.DEBUG_DM, "ws is null");
			return;
		}

		tsDmUicResult pUicResult = null;
		char[] pStartCharacter = new char[ws.uicOption.defaultResponse.text.length()];
		char[] pEndCharacter = new char[ws.uicOption.defaultResponse.text.length()];
		int nIndex;
		int ipStartCharacter = 0;
		int ipEndCharacter = 0;
		pUicResult = (tsDmUicResult) pData;

		pStartCharacter = ws.uicOption.defaultResponse.text.toCharArray();
		pEndCharacter = pStartCharacter;

		// while(*pStartCharacter != '\0')
		while (ipStartCharacter < pStartCharacter.length)
		{
			if (ipEndCharacter >= pEndCharacter.length)
				break;
			if ((pEndCharacter[ipEndCharacter] == '-') || (pEndCharacter[ipEndCharacter] == '\0'))
			{
				char[] tmpBuf = {0,};

				String.valueOf(pStartCharacter).getChars(0, ipEndCharacter - ipStartCharacter, tmpBuf, 0);
				nIndex = Integer.valueOf(String.valueOf(tmpBuf));

				pUicResult.MultiSelected[nIndex] = 1;

				pStartCharacter = pEndCharacter;

				if (pStartCharacter[ipStartCharacter] == '\0')
					break;

				++ipStartCharacter;
			}

			++ipEndCharacter;
		}
	}

	public static tsDmParserPcdata dmAgentHandlerString2pcdata(char[] str)
	{
		tsDmParserPcdata o;
		o = new tsDmParserPcdata();

		o.type = DmDevInfoConst.TYPE_STRING;
		o.size = str.length;
		o.data = new char[str.length];
		for (int i = 0; i < str.length; i++)
			o.data[i] = str[i];
		return o;
	}
}
