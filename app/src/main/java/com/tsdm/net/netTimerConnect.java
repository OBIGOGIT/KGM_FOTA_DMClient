package com.tsdm.net;

import android.annotation.SuppressLint;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import com.tsdm.core.data.constants.DmDevInfoConst;
import com.tsdm.tsService;
import com.tsdm.adapt.tsLib;

public class netTimerConnect
{
	private static Timer				connectTimer	= null;
	private static httpConnectTimerTask tprconnecttimer	= null;
	private static int					connectcount	= 0;
	private static int					AppId			= 0;
	private final int					AttatchTimer	= 180;

	public netTimerConnect(boolean status, int appId)
	{
		tprconnecttimer = new httpConnectTimerTask();
		connectTimer = new Timer();
		startTimer();

		AppId = appId;
	}

	public static void startTimer()
	{
		connectTimer.scheduleAtFixedRate(tprconnecttimer, new Date(), NetConsts.NET_TIMER_INTERVAL);
	}

	public static void endTimer()
	{
		try
		{
			connectcount = 0;
			AppId = 0;

			if (connectTimer == null || tprconnecttimer == null)
				return;

			//tsLib.debugPrint(DEBUG_NET, "=====================>> endTimer(connect)");
			connectTimer.cancel();
			tprconnecttimer.cancel();

			connectTimer = null;
			tprconnecttimer = null;

			netTimerReceive.endTimer();
			netTimerSend.endTimer();

		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DmDevInfoConst.DEBUG_NET, e.toString());
		}
	}

	public class httpConnectTimerTask extends TimerTask
	{
		private boolean	isCloseTimer	= false;
		private int		timerAppId		= DmDevInfoConst.SYNCMLAPPNONE;

		@SuppressLint("SuspiciousIndentation")
		public void run()
		{
			if (connectcount >= AttatchTimer)
			{
				connectcount = 0;
				endTimer();
				tsLib.debugPrint(DmDevInfoConst.DEBUG_NET, "===Connect Fail===");
				if (AppId == DmDevInfoConst.SYNCMLDM)
					tsService.Task.dmTaskdmXXXFail();
				else
					tsService.Task.dmTaskdlXXXFail();

				return;
			}
			if(connectcount>0)
			tsLib.debugPrint(DmDevInfoConst.DEBUG_NET, "== Connect Timer[" + connectcount + "]");
			connectcount++;
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
