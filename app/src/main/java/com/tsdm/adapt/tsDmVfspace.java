package com.tsdm.adapt;

import com.tsdm.core.data.constants.DmDevInfoConst;

public class tsDmVfspace
{
	int		i;
	int[]	start;
	int[]	end;

	public tsDmVfspace()
	{
		i = 0;
		start = new int[(int) DmDevInfoConst.MAX_NODE_NUM];
		end = new int[(int) DmDevInfoConst.MAX_NODE_NUM];
	}
}
