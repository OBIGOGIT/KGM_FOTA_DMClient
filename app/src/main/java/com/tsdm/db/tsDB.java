package com.tsdm.db;

import java.io.Serializable;
import java.util.Arrays;

import com.tsdm.adapt.tsLib;
import com.tsdm.core.data.constants.DmDevInfoConst;
import com.tsdm.net.NetConsts;
import com.tsdm.net.netHttpUtil;

public class tsDB extends tsdmDB implements Serializable
{
	private static final long	serialVersionUID	= 1L;

	public static int dbGetConnectType(int nAppId)
	{
		int type = NetConsts.TP_TYPE_NONE;

		switch (nAppId)
		{
			case DmDevInfoConst.SYNCMLDM:
			{
				String szProtocol = "";
				szProtocol = tsdmDB.dmdbGetProtocol();
				if (!tsLib.isEmpty(szProtocol))
				{
					type = netHttpUtil.exchangeProtocolType(szProtocol);
				}
				else
				{
					type = NetConsts.TP_TYPE_HTTP;
				}
				break;
			}
			case DmDevInfoConst.SYNCMLDL:
			{
				String szProtocol = "";
				int nAgentType = DmDevInfoConst.SYNCML_DM_AGENT_DM;

				szProtocol = tsdmDB.dmdbGetFUMOProtocol();
				if (!tsLib.isEmpty(szProtocol))
				{
					tsLib.debugPrint(DmDevInfoConst.DEBUG_DB, String.format("Protool [%s]", szProtocol)); // defect_110921
					type = netHttpUtil.exchangeProtocolType(szProtocol);
				}
				else
				{
					type = NetConsts.TP_TYPE_HTTP;
				}
				break;
			}
			default:
				type = NetConsts.TP_TYPE_HTTP;
				break;
		}
		return type;
	}

	public static String dbGetNotiSessionID(int nAppId)
	{
		String SessionId = null;

		switch (nAppId)
		{
			case DmDevInfoConst.SYNCMLDM:
				SessionId = tsdmDB.dmdbGetNotiSessionID(nAppId);
				break;

			default:
				break;
		}

		return SessionId;
	}

	public static int dbGetNotiEvent(int nAppId)
	{
		int nEvent = 0;

		switch (nAppId)
		{
			case DmDevInfoConst.SYNCMLDM:
			case DmDevInfoConst.SYNCMLDL:
				nEvent = dmdbGetNotiEvent();
				break;

			default:
				tsLib.debugPrintException(DmDevInfoConst.DEBUG_EXCEPTION, "Not Support Application: " + nAppId);
				break;
		}
		tsLib.debugPrint(DmDevInfoConst.DEBUG_DB, "nEvent :" + nEvent);
		return nEvent;
	}

	public static tsDBURLParser dbURLParser(String pURL)
	{
		String HTTP = "http://";
		String HTTPS = "https://";
		tsDBURLParser dbURLParser;
		String pCurrentPointer;
		String[] pNextPointer;
		String pTempAddress;
		int nProtocol = NetConsts.TP_TYPE_HTTP;
		int index = 0;

		String pAddress, pPath, pProtocol;
		int nPort;

		dbURLParser = new tsDBURLParser();

		if (pURL.startsWith(HTTPS))
		{
			pProtocol = DmDevInfoConst.NETWORK_TYPE_HTTPS;
			pCurrentPointer = pURL.substring(HTTPS.length(), pURL.length());
		}
		else if (pURL.startsWith(HTTP))
		{
			pProtocol = DmDevInfoConst.NETWORK_TYPE_HTTP;
			pCurrentPointer = pURL.substring(HTTP.length(), pURL.length());
		}
		else
		{
			pURL = HTTP;
			dbURLParser.pURL = pURL;
			return dbURLParser;
		}

		String path = pCurrentPointer;
		index = path.indexOf('/');

		if (index != -1)
		{
			pPath = path.substring(index, path.length());
		}
		else
		{
			pPath = "";
		}

		pNextPointer = pCurrentPointer.split("/");
		pTempAddress = pNextPointer[0];
		pCurrentPointer = pNextPointer[0];

		String[] pNextPointer2;
		pNextPointer2 = pCurrentPointer.split(":");

		if (pNextPointer2.length >= 2)
		{
			nPort = Integer.valueOf(pNextPointer2[1]);
			pAddress = pNextPointer2[0];
		}
		else
		{
			pAddress = pTempAddress;
			nProtocol = netHttpUtil.exchangeProtocolType(pProtocol);

			switch (nProtocol)
			{
				case NetConsts.TP_TYPE_HTTPS:
					nPort = 443; // https
					break;
				case NetConsts.TP_TYPE_HTTP:
					nPort = 80; // http
					break;
				case NetConsts.TP_TYPE_NONE:
				default:
					nPort = 80; // http
					break;
			}
		}

		dbURLParser.pURL = pURL;
		dbURLParser.pAddress = pAddress;
		dbURLParser.pPath = pPath;
		dbURLParser.pProtocol = pProtocol;
		dbURLParser.nPort = nPort;

		return dbURLParser;
	}

	public static String dbCheckOMADDURL(String pURL)
	{
		String retStr = "";

		int locAmp = pURL.indexOf('&');
		if (locAmp <= 0)
		{
			return pURL;
		}

		retStr = pURL.replaceAll("&amp;", "&");
		tsLib.debugPrint(DmDevInfoConst.DEBUG_DB, "URL = " + retStr);

		return retStr;
	}

	public static char[] dbDoDMBootStrapURI(char[] ResultURI, char[] BootURI, char[] BootPort)
	{
		int UriLen = 0;
		char[] temp = new char[DmDevInfoConst.DEFAULT_BUFFER_SIZE_2];
		int i = 0;
		int t = 0;
		int nCount = 0;
		int nPortCount = 0;

		if (BootURI == null || BootPort == null)
			return null;

		UriLen = (int) BootURI.length;

		for (i = 0; i < UriLen; i++)
		{
			if (BootURI[i] == '/')
			{
				nCount++;
			}
			else if (BootURI[i] == ':')
			{
				nPortCount++;
			}

			if (nPortCount == 2)
			{
/*				ResultURI = new char[BootURI.length];
				ResultURI = BootURI;*/
				return BootURI;
			}

			if (nCount == 3)
			{
				if (BootPort.length == 0 && BootPort[0] == '\0')
				{
					BootPort = "80".toCharArray();
					String tArg = tsLib.libString(temp);
					tArg = tArg.concat(":");
					tArg = tArg.concat(String.valueOf(BootPort));
					String Path = String.valueOf(BootURI);
					Path = Path.substring(i);
					tArg = tArg.concat(Path);
					/*ResultURI = tArg.toCharArray();*/
					tsLib.debugPrint(DmDevInfoConst.DEBUG_DB,  tArg); //ResultURI.toString());
					return tArg.toCharArray();
				}
				else
				{
					String tArg = tsLib.libString(temp);
					tArg = tArg.concat(":");
					tArg = tArg.concat(String.valueOf(BootPort));
					String Path = String.valueOf(BootURI);
					Path = Path.substring(i);
					tArg = tArg.concat(Path);
					/*ResultURI = tArg.toCharArray();*/
					tsLib.debugPrint(DmDevInfoConst.DEBUG_DB,  tArg);
					t = 0;
					return tArg.toCharArray();
				}

			}
			temp[t] = BootURI[i];
			t++;
		}
		return null;
	}
}
