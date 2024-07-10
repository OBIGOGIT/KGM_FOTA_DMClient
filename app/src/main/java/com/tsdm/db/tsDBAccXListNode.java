package com.tsdm.db;

import java.io.Serializable;

public class tsDBAccXListNode implements Serializable
{
	private static final long	serialVersionUID			= 1L;
	int DM_SETTING_PROFILE_NUM = 3;

	public tsDBAccXNode[]		stAccXNodeList;

	tsDBAccXListNode()
	{
		stAccXNodeList = new tsDBAccXNode[DM_SETTING_PROFILE_NUM];
	}
}
