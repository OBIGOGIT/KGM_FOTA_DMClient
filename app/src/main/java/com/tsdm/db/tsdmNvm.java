package com.tsdm.db;

import java.io.Serializable;

import com.tsdm.agent.dmAgentInfo;
import com.tsdm.core.data.constants.DmDevInfoConst;

public class tsdmNvm implements Serializable
{
	private static final long		serialVersionUID	= 1L;

	public tsdmProfileList tProfileList;
	public tsdmInfo NVMSyncMLDMInfo;
	public tsDBFumoInfo NVMSyncMLDMFUMOInfo;
	public tsDBPostPone NVMSyncMLPostPone;
	public tsDBSimInfo NVMSYNCMLSimInfo;

	public tsDBAccXListNode NVMSyncMLAccXNode;
	public tsDBResyncMode NVMSyncMLResyncMode;		// Nonce_resync_menu

	public dmAgentInfo NVMSyncMLDmAgentInfo;


	public tsdmNvm()
	{
		tProfileList = new tsdmProfileList();
		NVMSyncMLDMInfo = new tsdmInfo();
		NVMSyncMLDMFUMOInfo = new tsDBFumoInfo();
		NVMSyncMLPostPone = new tsDBPostPone();
		NVMSYNCMLSimInfo = new tsDBSimInfo();
		if (DmDevInfoConst._SYNCML_TS_DM_VERSION_V12_)
		{
			NVMSyncMLAccXNode = new tsDBAccXListNode();
			NVMSyncMLResyncMode = new tsDBResyncMode();
		}

		NVMSyncMLDmAgentInfo = new dmAgentInfo();
	}
}
