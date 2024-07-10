package com.tsdm.net;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import com.tsdm.tsService;
import com.tsdm.agent.dmDefineDevInfo;
import com.tsdm.adapt.tsDefineIdle;
import com.tsdm.adapt.tsLib;
import com.tsdm.agent.dmDefineMsg;

public class netTimerSend implements netDefine, dmDefineMsg, tsDefineIdle, dmDefineDevInfo
{
	private static Timer				sendTimer		= null;
	private static httpSendTimerTask tpsendtimer		= null;
	private static int					sendcount		= 0;
	private static int					AppId			= 0;
	private final int					SendingTimer	= 60;

	public netTimerSend(int appId)
	{
		tpsendtimer = new httpSendTimerTask();
		sendTimer = new Timer();
		startTimer();

		AppId = appId;
	}

	public static void startTimer()
	{
		sendTimer.scheduleAtFixedRate(tpsendtimer, new Date(), NET_TIMER_INTERVAL);
	}

	public static void endTimer()
	{
		try
		{
			sendcount = 0;
			AppId = 0;

			if (sendTimer == null || tpsendtimer == null)
				return;

			//tsLib.debugPrint(DEBUG_NET, "=====================>> endTimer(send)");
			sendTimer.cancel();
			tpsendtimer.cancel();
			sendTimer = null;
			tpsendtimer = null;

			netTimerConnect.endTimer();
			netTimerReceive.endTimer();
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_NET, e.toString());
		}
	}

	public class httpSendTimerTask extends TimerTask implements dmDefineDevInfo
	{
		private boolean	isCloseTimer	= false;
		private int		timerAppId		= SYNCMLAPPNONE;

		public void run()
		{
			if (sendcount >= SendingTimer)
			{
				sendcount = 0;
				endTimer();
				tsLib.debugPrint(DEBUG_NET, "===Send Fail===");
				if (AppId == SYNCMLDM)
					tsService.Task.dmTaskdmXXXFail();
				else
					tsService.Task.dmTaskdlXXXFail();

				return;
			}
			if(sendcount>0)
			tsLib.debugPrint(DEBUG_NET, "== send Timer[" + sendcount + "]");
			sendcount++;
		}

		public int getAppId()
		{
			return timerAppId;
		}

		public void setAppId(int appId)
		{
			timerAppId = appId;
		}
	}
}
