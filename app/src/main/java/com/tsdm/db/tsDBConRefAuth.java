package com.tsdm.db;

import java.io.Serializable;

public class tsDBConRefAuth implements Serializable
{
	private static final long	serialVersionUID	= 1L;

	public String				PAP_ID;
	public String				PAP_Secret;

	tsDBConRefAuth()
	{
		PAP_ID = "";
		PAP_Secret = "";
	}
}
