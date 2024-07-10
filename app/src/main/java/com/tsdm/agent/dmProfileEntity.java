package com.tsdm.agent;

import com.tsdm.db.tsDefineDB;
import com.tsdm.adapt.tsLib;

public class dmProfileEntity implements dmDefineDevInfo, tsDefineDB
{
	private static int			row;
	private static boolean		rowState							= false;
	
	public static boolean getRowState()
	{
		return rowState;
	}

	public static int getRow()
	{
		return row;
	}
	
	public static String dmDoServerURI(String ResultURI, char[] BootURI, char[] BootPort)
	{
		int UriLen = 0;
		char[] temp = new char[DEFAULT_BUFFER_SIZE_2];
		int i = 0;
		int t = 0;
		int nCount = 0;
		int nPortCount = 0;

		if (BootURI == null || BootPort == null)
			return null;

		UriLen = (int) BootURI.length;

		for (i = 0; i < UriLen; i++)
		{
			if (BootURI[i] == '/')
			{
				nCount++;
			}
			else if (BootURI[i] == ':')
			{
				nPortCount++;
			}

			if (nPortCount == 2)
			{
				ResultURI = new String(BootURI);
				return ResultURI;
			}

			if (nCount == 3)
			{
				if (BootPort.length == 0 && BootPort[0] == '\0')
				{
					BootPort = "80".toCharArray();
					String tArg = tsLib.libString(temp);
					tArg = tArg.concat(":");
					tArg = tArg.concat(String.valueOf(BootPort));
					String Path = String.valueOf(BootURI);
					Path = Path.substring(i);
					tArg = tArg.concat(Path);
					ResultURI = tArg;
					tsLib.debugPrint(DEBUG_DM, ResultURI);
					return ResultURI;
				}
				else
				{
					String tArg = tsLib.libString(temp);
					tArg = tArg.concat(":");
					tArg = tArg.concat(String.valueOf(BootPort));
					String Path = String.valueOf(BootURI);
					Path = Path.substring(i);
					tArg = tArg.concat(Path);
					ResultURI = tArg;
					tsLib.debugPrint(DEBUG_DM, ResultURI);
					t = 0;
					return ResultURI;
				}

			}
			temp[t] = BootURI[i];
			t++;
		}
		return null;
	}
}
