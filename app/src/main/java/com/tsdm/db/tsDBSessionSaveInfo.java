package com.tsdm.db;

import java.io.Serializable;

public class tsDBSessionSaveInfo implements Serializable
{
	private static final long	serialVersionUID	= 1L;
	public int					nSessionSaveState;
	public int					nNotiUiEvent;
	public int					nNotiRetryCount;

	public tsDBSessionSaveInfo()
	{
		nSessionSaveState = 0;
		nNotiUiEvent = 0;
		nNotiRetryCount = 0;
	}
}
