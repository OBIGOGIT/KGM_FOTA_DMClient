package com.tsdm.adapt;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import com.tsdm.core.data.constants.DmDevInfoConst;
import com.tsdm.core.data.constants.WbxmlProtocolConst;

public class tsDmParserPcdata
{
	public int					type;
	public char[]				data;
	public int					size;
	public tsDmParserAnchor anchor;
	public boolean				skipstatus	= false;

	public int dmParsePcdata(tsDmParser p, int id)
	{
		int res = WbxmlProtocolConst.DM_ERR_OK;
		int n = 0;
		String tmp;

		tsLib.debugPrint(DmDevInfoConst.DEBUG_DM, "");

		res = p.dmParseCheckElement(id);
		if (res != WbxmlProtocolConst.DM_ERR_OK)
		{
			return res;
		}

		res = p.dmParseZeroBitTagCheck();
		if (res == WbxmlProtocolConst.DM_ERR_ZEROBIT_TAG)
		{
			return WbxmlProtocolConst.DM_ERR_OK;
		}
		else if (res != WbxmlProtocolConst.DM_ERR_OK)
		{
			tsLib.debugPrintException(DmDevInfoConst.DEBUG_EXCEPTION, "not DM_ERR_OK");
			return res;
		}

		try
		{
			n = p.dmWbxDecReadBufferByte();
			if (n == WbxmlProtocolConst.WBXML_STR_I)
			{
				tmp = p.dmWbxDecParseStr_i();
				dmParserString2pcdata(tmp);
			}
			else if (n == WbxmlProtocolConst.WBXML_STR_T)
			{
				tmp = p.dmWbxDecParseStr_t();
				dmParserString2pcdata(tmp);
			}
			else if (n == WbxmlProtocolConst.WBXML_OPAQUE)
			{
				tmp = p.dmWbxDecParseExtension(n);
				type = DmDevInfoConst.TYPE_OPAQUE;
				size = tmp.length();
				dmParserString2pcdata(tmp);
			}
			else if (n == WbxmlProtocolConst.WBXML_SWITCH_PAGE)
			{
				int tmpId = p.dmParseReadElement();
				if (res != WbxmlProtocolConst.DM_ERR_OK)
				{
					return res;
				}
				p.codePage = tmpId;
				tmpId = p.dmParseCurrentElement();
				do
				{
					if (p.codePage == WbxmlProtocolConst.WBXML_PAGE_METINF && tmpId == WbxmlProtocolConst.WBXML_METINF_Anchor)
					{
						anchor = new tsDmParserAnchor();
						res = anchor.dmParseAnchor(p);

						if (res != WbxmlProtocolConst.DM_ERR_OK)
						{
							return res;
						}
					}
					else if (p.codePage == WbxmlProtocolConst.WBXML_PAGE_METINF && tmpId == WbxmlProtocolConst.WBXML_METINF_Mem)
					{
						tsDmParserMem mem = new tsDmParserMem();
						res = mem.dmParseMem(p);

						if (res != WbxmlProtocolConst.DM_ERR_OK)
						{
							return res;
						}
					}
					else if (tmpId == WbxmlProtocolConst.WBXML_SWITCH_PAGE)
					{
						p.dmParseReadElement();
						p.dmParseReadElement();
					}

					tmpId = p.dmParseCurrentElement();
				} while (tmpId != WbxmlProtocolConst.WBXML_END);
			}
			else
			{
				p.wbxindex--;// backward buffer
				res = p.dmParseSkipElement(id);
				if (res != WbxmlProtocolConst.DM_ERR_OK)
				{
					return res;
				}

				type = DmDevInfoConst.TYPE_EXTENSION;
				size = 0;
				data = null;
			}

			res = p.dmParseCheckElement(WbxmlProtocolConst.WBXML_END);
			if (res != WbxmlProtocolConst.DM_ERR_OK)
			{
				return res;
			}

		}
		catch (IOException e)
		{
			tsLib.debugPrintException(DmDevInfoConst.DEBUG_EXCEPTION, e.toString());
		}

		return WbxmlProtocolConst.DM_ERR_OK;
	}

	public void dmParserString2pcdata(String str) throws UnsupportedEncodingException
	{
		char[] buff = null;
		type = DmDevInfoConst.TYPE_STRING;
		size = str.length();

		buff = str.toCharArray();
		data = new char[buff.length];

		for (int i = 0; i < buff.length; i++)
		{
			data[i] = buff[i];
		}
	}
}
