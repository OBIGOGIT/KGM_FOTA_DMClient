package com.tsdm.adapt;

public class tsDmUicOption implements tsDefineUic
{
	public int			appId;
	public int			minDT;
	public int			maxDT;
	public int			maxLen;
	public long			progrMaxSize;
	public long			progrCurSize;
	public tsDmText text;
	public tsDmText defaultResponse;
	public int			UICType;
	public int			inputType;
	// DM 1.2
	public int			uicMenuNumbers;
	public String[]		uicMenuList;
	public String		uicMenuTitle;

	public int			progrType;
	public int			echoType;

	tsDmUicOption()
	{
		uicMenuList = new String[UIC_MAX_CHOICE_MENU];
		text = new tsDmText();
		defaultResponse = new tsDmText();
	}
}
