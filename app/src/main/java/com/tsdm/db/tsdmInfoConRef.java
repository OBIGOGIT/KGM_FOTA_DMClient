package com.tsdm.db;

import java.io.Serializable;

public class tsdmInfoConRef implements Serializable
{
	private static final long			serialVersionUID	= 1L;

	public tsDBConRefNAP NAP;
	public tsDBConRefPX PX;
	public String						szHomeUrl;
	public int							nService;
	public boolean						Active;
	public boolean						bProxyUse;
	public tsDBConRefAdvanceSetting tAdvSetting;

	public tsdmInfoConRef()
	{
		szHomeUrl = "";
		NAP = new tsDBConRefNAP();
		PX = new tsDBConRefPX();
		tAdvSetting = new tsDBConRefAdvanceSetting();
	}
}
