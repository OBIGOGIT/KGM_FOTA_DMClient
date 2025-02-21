package com.tsdm.db;

import java.io.Serializable;

public class tsDBPostPone implements Serializable
{
	private static final long	serialVersionUID	= 1L;
	public long					tCurrentTime;
	public long					tEndTime;
	public boolean				nAfterDownLoadBatteryStatus;
	public long					nPostPoneTime;
	public int					nPostPoneCount;
	public boolean				bPostPoneDownload;
}
