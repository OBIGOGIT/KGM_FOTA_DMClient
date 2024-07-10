package com.tsdm.agent;

import java.io.Serializable;

public class dmAgentInfo implements Serializable, dmDefineDevInfo
{
	private static final long	serialVersionUID	= 1L;
	public int					nAgentType;

	public dmAgentInfo()
	{
		nAgentType = SYNCML_DM_AGENT_DM;
	}
}
