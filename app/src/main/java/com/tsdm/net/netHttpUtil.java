package com.tsdm.net;

import com.tsdm.db.tsDB;
import com.tsdm.db.tsDBURLParser;

public class netHttpUtil
{
	public static String tpParsePath(String request)
	{
		int firsturl = request.indexOf("://");
		String SubStr = request.substring(firsturl + 3);
		int firstSlash = SubStr.indexOf('/');

		String retStr = request.substring(firsturl + firstSlash + 3);
		return retStr;
	}

	public static int httpGetConnectType(String pURL)
	{
		String prtString = pURL.substring(0, 5);
		int type = NetConsts.TP_TYPE_NONE;

		if (prtString.equals("http:"))
			type = NetConsts.TP_TYPE_HTTP;
		else if (prtString.equals("https"))
			type = NetConsts.TP_TYPE_HTTPS;
		else if (prtString.equals("obex:"))
			type = NetConsts.TP_TYPE_OBEX;

		return type;
	}

	public static int exchangeProtocolType(String protocol)
	{
		if (protocol.equals("https"))
		{
			return NetConsts.TP_TYPE_HTTPS;
		}
		else if (protocol.equals("http"))
		{
			return NetConsts.TP_TYPE_HTTP;
		}
		else if (protocol.equals("obex"))
		{
			return NetConsts.TP_TYPE_OBEX;
		}
		else
		{
			return NetConsts.TP_TYPE_NONE;
		}
	}

	public static String tpParserServerAddrWithPort(String requestUri)
	{
		tsDBURLParser parser = new tsDBURLParser();
		parser = tsDB.dbURLParser(requestUri);

		String retAddr = parser.pAddress + ":" + String.valueOf(parser.nPort);
		return retAddr;
	}
}
