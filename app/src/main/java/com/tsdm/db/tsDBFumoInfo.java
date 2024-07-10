package com.tsdm.db;

import java.io.Serializable;

public class tsDBFumoInfo implements Serializable
{
	private static final long	serialVersionUID	= 1L;

	public String				Protocol;
	public int					ObexType;
	public int					AuthType;
	public int					ServerPort;
	public String				ServerUrl;
	public String				ServerIP;
	public String				ObjectDownloadProtocol;
	public String				ObjectDownloadUrl;
	public String				ObjectDownloadIP;
	public int					nObjectDownloadPort;

	public String				StatusNotifyProtocol;
	public String				StatusNotifyUrl;
	public String				StatusNotifyIP;
	public int					nStatusNotifyPort;

	public String				ReportURI;
	public int					nObjectSize;
	public int					nFFSWriteSize;
	public int					nStatus;
	public String				StatusNodeName;
	public String				ResultCode;
	public int					nUpdateMechanism;
	public boolean				nDownloadMode;
	public String				Correlator;
	public String				szContentType;
	public String				szAcceptType;
	public String				szDescription;
	public boolean				bUpdateWait;

	public tsDBFumoInfo()
	{
		Protocol = "";
		ServerUrl = "";
		ServerIP = "";
		ObjectDownloadProtocol = "";
		ObjectDownloadUrl = "";
		ObjectDownloadIP = "";

		StatusNotifyProtocol = "";
		StatusNotifyUrl = "";
		StatusNotifyIP = "";

		ReportURI = "";

		StatusNodeName = "";
		ResultCode = "";

		Correlator = "";
		szContentType = "";
		szAcceptType = "";

		nDownloadMode = false;
		bUpdateWait = false;
	}
}
