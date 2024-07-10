package com.tsdm.db;

import java.io.Serializable;

public class tsDBURLParser implements Serializable
{
	public static final long	serialVersionUID	= 1L;
	public String				pURL;
	public String				pAddress;
	public String				pPath;
	public int					nPort;
	public String				pProtocol;

	public tsDBURLParser()
	{
		pURL = "";
		pAddress = "";
		pPath = "";
		nPort = 0;
		pProtocol = "";
	}
}
