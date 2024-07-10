package com.tsdm.db;

import java.io.Serializable;

public class tsDBAccXNode implements Serializable
{
	private static final long	serialVersionUID	= 1L;
	public String				Account;
	public String				AppAddr;
	public String				AppAddrPort;
	public String				ClientAppAuth;
	public String				ServerAppAuth;
	public String				ToConRef;

	public tsDBAccXNode()
	{
		Account = "";
		AppAddr = "";
		AppAddrPort = "";
		ClientAppAuth = "";
		ServerAppAuth = "";
		ToConRef = "";
	}
}
