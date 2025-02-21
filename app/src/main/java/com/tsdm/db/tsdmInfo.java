package com.tsdm.db;

import java.io.Serializable;

public class tsdmInfo implements Serializable
{
	private static final long		serialVersionUID			= 1L;

	public String					Protocol;
	public int						ServerPort;
	public String					ServerUrl;
	public String					ServerIP;
	public String					Path;
	public String					Protocol_Org;
	public int						ServerPort_Org;
	public String					ServerUrl_Org;
	public String					ServerIP_Org;
	public String					Path_Org;
	public boolean					bChangedProtocol;

	public int						ObexType;
	public int						AuthType;
	public int						nServerAuthType;

	public String					AppID;
	public String					AuthLevel;
	public String					ServerAuthLevel;
	public String					PrefConRef;

	public String					UserName;
	public String					Password;
	public String					ServerID;
	public String					ServerPwd;

	public String					ClientNonce;
	public String					ServerNonce;
	public int						ServerNonceFormat;
	public int						ClientNonceFormat;

	public String					ProfileName;
	public String					NetworkConnName;
	public int						nNetworkConnIndex;
	public int						MagicNumber;
	public tsdmInfoConRef ConRef;
	public tsDBNetConProfileBackup ConBackup;

	public tsdmInfo()
	{
		Protocol = "";
		ServerPort = 80;
		ServerUrl = "";
		ServerIP = "";
		Path = "";
		Protocol_Org = "";
		ServerUrl_Org = "";
		ServerIP_Org = "";
		Path_Org = "";

		ObexType = 2;
		AuthType = 0;
		nServerAuthType = 0;

		AppID = "";
		AuthLevel = "";
		ServerAuthLevel = "";
		PrefConRef = "";
		UserName = "";
		Password = "";
		ServerID = "";
		ServerPwd = "";

		ClientNonce = "";
		ServerNonce = "";
		ServerNonceFormat = 0;
		ClientNonceFormat = 0;

		ProfileName = "";
		NetworkConnName = "";
		nNetworkConnIndex = 1;

		ConRef = new tsdmInfoConRef();
		ConBackup = new tsDBNetConProfileBackup();
	}
}
