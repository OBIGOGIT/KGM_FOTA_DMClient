package com.tsdm.agent;

import com.tsdm.core.data.constants.DmDevInfoConst;

import java.io.Serializable;

public class dmAgentInfo implements Serializable
{
	private static final long	serialVersionUID	= 1L;
	public int					nAgentType;

	public dmAgentInfo()
	{
		nAgentType = DmDevInfoConst.SYNCML_DM_AGENT_DM;
	}
}
