package com.tsdm.db;

import java.io.Serializable;

public class tsdmProflieList implements Serializable
{
	private static final long	serialVersionUID		= 1L;
	public int					SDM_SETTING_PROFILE_NUM	= 3;

	// Member value
	public String				NetworkConnName;
	public int					nProxyIndex;
	public int					Profileindex;
	public String[]				ProfileName;
	public String				nSessionID;
	public int					nNotiEvent;
	public long					nDestoryNotiTime;
	public int					nNotiReSyncMode;
	public int					nDDFParserNodeIndex;
	public boolean				bSkipDevDiscovery;
	public int					MagicNumber;
	public tsDBSessionSaveInfo NotiResumeState;
	public tsDBUICResultKeep tUicResultKeep;
	public String				szImei;
	public boolean				bWifiOnly;
	public boolean				bAutoUpdate;
	public boolean				bPushMessage;
	public long					nAutoUpdateTime;
	public int 					nSaveDeltaFileIndex;
	
	public long					lNextUpdateTime;
	public boolean				bAutoCheck;
	public boolean				bAgentMode;
	public long					lcurrCheckTime;
	
	public tsdmProflieList()
	{
		NetworkConnName = "";
		nProxyIndex = 0;
		Profileindex = 0;
		ProfileName = new String[SDM_SETTING_PROFILE_NUM];
		bSkipDevDiscovery = false;
		NotiResumeState = new tsDBSessionSaveInfo();
		tUicResultKeep = new tsDBUICResultKeep();
		nDDFParserNodeIndex = 0;
		szImei = "";
		bWifiOnly = false;
		bAutoUpdate = false;
		bPushMessage = false;
		nAutoUpdateTime = 0;
		nSaveDeltaFileIndex = 0;
		lNextUpdateTime = 0l;
		bAutoCheck = true;
		bAgentMode = false;
		lcurrCheckTime = 0l;
	}
}
