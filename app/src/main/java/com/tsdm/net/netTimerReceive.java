package com.tsdm.net;

import android.annotation.SuppressLint;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import com.tsdm.core.data.constants.DmDevInfoConst;
import com.tsdm.tsService;
import com.tsdm.adapt.tsLib;

public class netTimerReceive
{
	private static Timer				recvTimer		= null;
	private static httpRecvTimerTask tprecvtimer		= null;
	private static int					recvcount		= 0;
	private static int					AppId			= 0;
	private final int					ReceiveTimer	= 180;

	public netTimerReceive(int appId)
	{
		tprecvtimer = new httpRecvTimerTask();
		recvTimer = new Timer();
		startTimer();

		AppId = appId;
	}

	public static void startTimer()
	{
		recvTimer.scheduleAtFixedRate(tprecvtimer, new Date(), NetConsts.NET_TIMER_INTERVAL);
	}

	public static void endTimer()
	{
		try
		{
			recvcount = 0;
			AppId = 0;

			if (recvTimer == null || tprecvtimer == null)
				return;


     		//tsLib.debugPrint(DEBUG_NET, "=====================>> endTimer(recv)");
			recvTimer.cancel();
			tprecvtimer.cancel();

			recvTimer = null;
			tprecvtimer = null;

			netTimerSend.endTimer();
			netTimerConnect.endTimer();
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DmDevInfoConst.DEBUG_NET, e.toString());
		}
	}

	public class httpRecvTimerTask extends TimerTask
	{
		private boolean	isCloseTimer	= false;
		private int		timerAppId		= DmDevInfoConst.SYNCMLAPPNONE;

		@SuppressLint("SuspiciousIndentation")
		public void run()
		{
			if (recvcount >= ReceiveTimer)
			{
				recvcount = 0;
				endTimer();
				tsLib.debugPrint(DmDevInfoConst.DEBUG_NET, "===Receive Fail===");
				if (AppId == DmDevInfoConst.SYNCMLDM)
					tsService.Task.dmTaskdmXXXFail();
				else
					tsService.Task.dmTaskdlXXXFail();

				return;
			}
			if(recvcount>0)
			tsLib.debugPrint(DmDevInfoConst.DEBUG_NET, "== recv Timer[" + recvcount + "]");
			recvcount++;
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
