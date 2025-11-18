package com.tsdm.adapt;

public class tsDmUicResult implements tsDefineUic
{
	int					UIC_MAX_CHOICE_MENU	= 32;

	public int			appId;
	public int			UICType;
	public int			result;
	// DM 1.2
	public int			SingleSelected;
	public int			MenuNumbers;
	public int[]		MultiSelected;
	public tsDmText text;						// used when userInput

	tsDmUicResult()
	{
		MultiSelected = new int[UIC_MAX_CHOICE_MENU];
	}
}
