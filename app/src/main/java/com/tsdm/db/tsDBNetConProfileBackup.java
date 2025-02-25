package com.tsdm.db;

import java.io.Serializable;

public class tsDBNetConProfileBackup implements Serializable
{
	private static final long	serialVersionUID		= 1L;
	public boolean				Active;
	public int					protoAppType;
	public String				APN;
	public String				Id;
	public String				Password;
	public String				Address;						// xxx.xxx.xxx.xxx:12345

	tsDBNetConProfileBackup()
	{
		APN = "";
		Id = "";
		Password = "";
		Address = "";
	}
}
