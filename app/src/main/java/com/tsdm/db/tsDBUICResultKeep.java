package com.tsdm.db;

import java.io.Serializable;

public class tsDBUICResultKeep implements Serializable
{
	private static final long	serialVersionUID	= 1L;

	public int					eStatus;
	public int					appId;
	public int					UICType;
	public int					result;
	public int					number;
	public String				szText;
	public int					nLen;
	public int					nSize;

	public tsDBUICResultKeep()
	{
		szText = "";
	}
}
