package com.tsdm.db;

import java.io.Serializable;

public class tsDBResyncMode implements Serializable
{
	private static final long	serialVersionUID	= 1L;
	public boolean				nNoceResyncMode;

	tsDBResyncMode()
	{
		nNoceResyncMode = false;
	}
}
