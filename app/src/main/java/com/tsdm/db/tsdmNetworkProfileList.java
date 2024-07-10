package com.tsdm.db;

import java.io.Serializable;

public class tsdmNetworkProfileList implements Serializable
{
	private static final long	serialVersionUID	= 1L;

	public int					ActivateID;
	public String				ConRefName;

	public tsdmInfoConRef ConRef;

	public tsdmNetworkProfileList()
	{
		ConRefName = "";
		ConRef = new tsdmInfoConRef();
	}
}
