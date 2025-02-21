package com.tsdm.db;

import java.io.Serializable;

public class tsDBConRefNAP implements Serializable
{
	private static final long	serialVersionUID	= 1L;
	public String				NetworkProfileName;
	public int					nBearer;
	public int					nAddrType;
	public String				Addr;
	public tsDBConRefAuth Auth;

	tsDBConRefNAP()
	{
		NetworkProfileName = "";
		Addr = "";
		Auth = new tsDBConRefAuth();
	}
}
