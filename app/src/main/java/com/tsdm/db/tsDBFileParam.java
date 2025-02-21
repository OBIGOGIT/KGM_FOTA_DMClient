package com.tsdm.db;

import java.io.Serializable;

public class tsDBFileParam implements Serializable
{
	private static final long	serialVersionUID	= 1L;

	public int					AreaCode;
	public int					pExtFileID;
	public Object				pNVMUser;
	public long					offset;
	public long					size;
}
