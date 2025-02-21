package com.tsdm.db;

import java.io.Serializable;

import com.tsdm.agent.dmDefineDevInfo;

public class tsDBfile implements Serializable, dmDefineDevInfo
{
	private static final long		serialVersionUID			= 1L;
	private static final int 		DM_PROFILE_LIST = 0;
	private static final int 		DM_NET_PROFILE_LIST = 1;
	private static final int 		DM_PROFILE_LIST_VIEW = 2;
	private static final int 		DM_NET_PROFILE_LIST_VIEW = 3;
	private static final int 		DM_DB_READ_PROFILE_INFO = 4;

	public tsdmNvm DMNvmClass;
	public tsdmProflieList DMprofile;
	public tsdmInfo DMprofileInfo;
	public tsdmNetworkProfileList DMNetProfile;

	tsDBfile()
	{
		DMprofile = new tsdmProflieList();
		DMprofileInfo = new tsdmInfo();
		DMNetProfile = new tsdmNetworkProfileList();
		DMNvmClass = new tsdmNvm();
	}

	tsDBfile(int nType)
	{
		switch (nType)
		{
			case DM_PROFILE_LIST:
				DMprofile = new tsdmProflieList();
				break;
			case DM_NET_PROFILE_LIST:
				DMNetProfile = new tsdmNetworkProfileList();
				break;
			case DM_PROFILE_LIST_VIEW:
				DMprofileInfo = new tsdmInfo();
				break;
			case DM_NET_PROFILE_LIST_VIEW:
				break;
			case DM_DB_READ_PROFILE_INFO:
				DMNvmClass = new tsdmNvm();
				break;
		}
	}
}
