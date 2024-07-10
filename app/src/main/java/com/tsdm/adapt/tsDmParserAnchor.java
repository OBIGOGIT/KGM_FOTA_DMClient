package com.tsdm.adapt;

import java.io.IOException;

import com.tsdm.agent.dmDefineDevInfo;

class tsDmParserAnchor implements tsDefineWbxml, dmDefineDevInfo
{
	public String	last;
	public String	next;

	public int dmParseAnchor(tsDmParser p)
	{
		int id = -1;
		int res = DM_ERR_OK;
		tsLib.debugPrint(DEBUG_DM, "");

		res = p.dmParseCheckElement(WBXML_METINF_Anchor);
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
			tsLib.debugPrintException(DEBUG_EXCEPTION, " not DM_ERR_OK");
			return res;
		}

		res = p.dmParseSkipLiteralElement();
		if (res != DM_ERR_OK)
		{
			return res;
		}

		while (true)
		{
			try
			{
				id = p.dmParseCurrentElement();
			}
			catch (IOException e)
			{
				tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
			}

			if (id == WBXML_END)
			{
				id = p.dmParseReadElement();
				break;
			}

			switch (id)
			{
				case WBXML_METINF_Last:
					res = p.dmParseElement(id);
					last = p._pParserElement;
					break;

				case WBXML_METINF_Next:
					res = p.dmParseElement(id);
					next = p._pParserElement;
					break;

				default:
					res = DM_ERR_UNKNOWN_ELEMENT;
			}

			if (res != DM_ERR_OK)
			{
				return res;
			}
		}
		return res;
	}
}
