package com.tsdm.agent;

import com.tsdm.core.data.constants.DmDevInfoConst;
import com.tsdm.db.tsDefineDB;
import com.tsdm.adapt.tsLib;

public class dmProfileEntity implements tsDefineDB
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
		char[] temp = new char[DmDevInfoConst.DEFAULT_BUFFER_SIZE_2];
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
/*				ResultURI = new String(BootURI);*/
				return new String(BootURI);
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
					/*ResultURI = tArg;*/
					tsLib.debugPrint(DmDevInfoConst.DEBUG_DM, tArg);
					return tArg;
				}
				else
				{
					String tArg = tsLib.libString(temp);
					tArg = tArg.concat(":");
					tArg = tArg.concat(String.valueOf(BootPort));
					String Path = String.valueOf(BootURI);
					Path = Path.substring(i);
					tArg = tArg.concat(Path);
				    /*	ResultURI = tArg;*/
					tsLib.debugPrint(DmDevInfoConst.DEBUG_DM, tArg);
					t = 0;
					return tArg;
				}

			}
			temp[t] = BootURI[i];
			t++;
		}
		return null;
	}
}
