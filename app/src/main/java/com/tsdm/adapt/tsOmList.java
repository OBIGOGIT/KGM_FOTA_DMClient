package com.tsdm.adapt;

import com.tsdm.agent.dmDefineDevInfo;

public class tsOmList implements dmDefineDevInfo
{
	public tsOmList next;
	public Object		data;


	public static void dmOmDeleteAclList(tsOmList h)
	{
		tsOmList next;
		tsOmList cur = h;
		@SuppressWarnings("unused")
		tsOmAcl acl = null;

		while (cur != null)
		{
			next = cur.next;
			acl = (tsOmAcl) cur.data;

			acl = null;
			cur = null;
			cur = next;
		}
	}

	public void dmOmDeleteMimeList(tsOmList h)
	{
		tsOmList next;
		tsOmList cur;

		cur = h;

		while (cur != null)
		{
			next = cur.next;

			cur.data = null;
			cur = null;
			cur = next;
		}
	}
	public static String dmOmGetFormatString(int format)
	{
		String outbuf;

		switch (format)
		{
			case FORMAT_B64:
				outbuf = "b64";
				break;
			case FORMAT_BIN:
				outbuf = "bin";
				break;
			case FORMAT_BOOL:
				outbuf = "bool";
				break;
			case FORMAT_CHR:
				outbuf = "chr";
				break;
			case FORMAT_INT:
				outbuf = "int";
				break;
			case FORMAT_NODE:
				outbuf = "node";
				break;
			case FORMAT_NULL:
				outbuf = "null";
				break;
			case FORMAT_XML:
				outbuf = "xml";
				break;
			case FORMAT_FLOAT:
				outbuf = "float";
				break;
			case FORMAT_TIME:
				outbuf = "time";
				break;
			case FORMAT_DATE:
				outbuf = "date";
				break;

			default:
				outbuf = null;
				break;
		}

		return outbuf;
	}

	public static int dmOmGetFormatFromString(String str)
	{
		if (str.compareTo("b64") == 0)
			return FORMAT_B64;
		else if (str.compareTo("bin") == 0)
			return FORMAT_BIN;
		else if (str.compareTo("bool") == 0)
			return FORMAT_BOOL;
		else if (str.compareTo("chr") == 0)
			return FORMAT_CHR;
		else if (str.compareTo("int") == 0)
			return FORMAT_INT;
		else if (str.compareTo("node") == 0)
			return FORMAT_NODE;
		else if (str.compareTo("null") == 0)
			return FORMAT_NULL;
		else if (str.compareTo("xml") == 0)
			return FORMAT_XML;
		else if (str.compareTo("float") == 0)
			return FORMAT_FLOAT;
		else if (str.compareTo("time") == 0)
			return FORMAT_TIME;
		else if (str.compareTo("date") == 0)
			return FORMAT_DATE;

		else
			return FORMAT_NONE;
	}

}
