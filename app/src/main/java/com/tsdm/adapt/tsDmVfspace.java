package com.tsdm.adapt;

import com.tsdm.agent.dmDefineDevInfo;

public class tsDmVfspace implements dmDefineDevInfo
{
	int		i;
	int[]	start;
	int[]	end;

	public tsDmVfspace()
	{
		i = 0;
		start = new int[(int) MAX_NODE_NUM];
		end = new int[(int) MAX_NODE_NUM];
	}
}
