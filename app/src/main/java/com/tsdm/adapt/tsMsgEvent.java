package com.tsdm.adapt;

public class tsMsgEvent
{
	public static void SetMsgEvent(Object userdata, int nevt)
	{
		int UiEventMsg = 0;
		Object pParam1 = userdata;
		Object pParam2 = null;

		UiEventMsg = nevt;
		tsDmMsg.uiSendMessage(UiEventMsg, pParam1, pParam2);
	}
}
