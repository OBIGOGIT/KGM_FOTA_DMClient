package com.tsdm.adapt;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import com.tsdm.agent.dmDefineDevInfo;

public class tsDmParserPcdata implements tsDefineWbxml, dmDefineDevInfo
{
	public int					type;
	public char[]				data;
	public int					size;
	public tsDmParserAnchor anchor;
	public boolean				skipstatus	= false;

	public int dmParsePcdata(tsDmParser p, int id)
	{
		int res = DM_ERR_OK;
		int n = 0;
		String tmp;

		tsLib.debugPrint(DEBUG_DM, "");

		res = p.dmParseCheckElement(id);
		if (res != DM_ERR_OK)
		{
			return res;
		}

		res = p.dmParseZeroBitTagCheck();
		if (res == DM_ERR_ZEROBIT_TAG)
		{
			return DM_ERR_OK;
		}
		else if (res != DM_ERR_OK)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, "not DM_ERR_OK");
			return res;
		}

		try
		{
			n = p.dmWbxDecReadBufferByte();
			if (n == WBXML_STR_I)
			{
				tmp = p.dmWbxDecParseStr_i();
				dmParserString2pcdata(tmp);
			}
			else if (n == WBXML_STR_T)
			{
				tmp = p.dmWbxDecParseStr_t();
				dmParserString2pcdata(tmp);
			}
			else if (n == WBXML_OPAQUE)
			{
				tmp = p.dmWbxDecParseExtension(n);
				type = TYPE_OPAQUE;
				size = tmp.length();
				dmParserString2pcdata(tmp);
			}
			else if (n == WBXML_SWITCH_PAGE)
			{
				int tmpId = p.dmParseReadElement();
				if (res != DM_ERR_OK)
				{
					return res;
				}
				p.codePage = tmpId;
				tmpId = p.dmParseCurrentElement();
				do
				{
					if (p.codePage == WBXML_PAGE_METINF && tmpId == WBXML_METINF_Anchor)
					{
						anchor = new tsDmParserAnchor();
						res = anchor.dmParseAnchor(p);

						if (res != DM_ERR_OK)
						{
							return res;
						}
					}
					else if (p.codePage == WBXML_PAGE_METINF && tmpId == WBXML_METINF_Mem)
					{
						tsDmParserMem mem = new tsDmParserMem();
						res = mem.dmParseMem(p);

						if (res != DM_ERR_OK)
						{
							return res;
						}
					}
					else if (tmpId == WBXML_SWITCH_PAGE)
					{
						p.dmParseReadElement();
						p.dmParseReadElement();
					}

					tmpId = p.dmParseCurrentElement();
				} while (tmpId != WBXML_END);
			}
			else
			{
				p.wbxindex--;// backward buffer
				res = p.dmParseSkipElement(id);
				if (res != DM_ERR_OK)
				{
					return res;
				}

				type = TYPE_EXTENSION;
				size = 0;
				data = null;
			}

			res = p.dmParseCheckElement(WBXML_END);
			if (res != DM_ERR_OK)
			{
				return res;
			}

		}
		catch (IOException e)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
		}

		return DM_ERR_OK;
	}

	public void dmParserString2pcdata(String str) throws UnsupportedEncodingException
	{
		char[] buff = null;
		type = TYPE_STRING;
		size = str.length();

		buff = str.toCharArray();
		data = new char[buff.length];

		for (int i = 0; i < buff.length; i++)
		{
			data[i] = buff[i];
		}
	}
}
