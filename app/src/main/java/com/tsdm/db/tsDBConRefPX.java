package com.tsdm.db;

import java.io.Serializable;

public class tsDBConRefPX implements Serializable
{
	private static final long	serialVersionUID	= 1L;
	public int					nPortNbr;
	public int					nAddrType;
	public String				Addr;
	public tsDBConRefAuth Auth;

	tsDBConRefPX()
	{
		nPortNbr = 0;
		nAddrType = 0;
		Addr = "";
		Auth = new tsDBConRefAuth();
	}
}
