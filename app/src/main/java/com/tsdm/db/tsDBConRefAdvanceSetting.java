package com.tsdm.db;

import java.io.Serializable;

public class tsDBConRefAdvanceSetting implements Serializable
{
	private static final long	serialVersionUID	= 1L;
	public boolean				bStaticIpUse;
	public String				szStaticIp;
	public boolean				bStaticDnsUse;
	public int					szStaticDns1;
	public int					szStaticDns2;
	public int					nTrafficClass;
}
