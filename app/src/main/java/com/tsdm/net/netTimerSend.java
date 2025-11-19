package com.tsdm.net;

import android.annotation.SuppressLint;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import com.tsdm.core.data.constants.DmDevInfoConst;
import com.tsdm.tsService;
import com.tsdm.adapt.tsLib;

public class netTimerSend
{
	private static Timer				sendTimer		= null;
	private static httpSendTimerTask tpsendtimer		= null;
	private static int					sendcount		= 0;
	private static int					AppId			= 0;
	private final int					SendingTimer	= 180;

	public netTimerSend(int appId)
	{
		tpsendtimer = new httpSendTimerTask();
		sendTimer = new Timer();
		startTimer();

		AppId = appId;
	}

	public static void startTimer()
	{
		sendTimer.scheduleAtFixedRate(tpsendtimer, new Date(), NetConsts.NET_TIMER_INTERVAL);
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
			tsLib.debugPrintException(DmDevInfoConst.DEBUG_NET, e.toString());
		}
	}

	public class httpSendTimerTask extends TimerTask
	{
		private boolean	isCloseTimer	= false;
		private int		timerAppId		= DmDevInfoConst.SYNCMLAPPNONE;

		@SuppressLint("SuspiciousIndentation")
		public void run()
		{
			if (sendcount >= SendingTimer)
			{
				sendcount = 0;
				endTimer();
				tsLib.debugPrint(DmDevInfoConst.DEBUG_NET, "===Send Fail===");
				if (AppId == DmDevInfoConst.SYNCMLDM)
					tsService.Task.dmTaskdmXXXFail();
				else
					tsService.Task.dmTaskdlXXXFail();

				return;
			}
			if(sendcount>0)
			tsLib.debugPrint(DmDevInfoConst.DEBUG_NET, "== send Timer[" + sendcount + "]");
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
