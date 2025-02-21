package com.tsdm.adapt;

import java.io.IOException;

import com.tsdm.agent.dmDefineDevInfo;

public class tsDmParserMem implements tsDefineWbxml
{
	public String	shared;
	public int		free;
	public int		freeid;

	public int dmParseMem(tsDmParser p)
	{
		int id = -1;
		int res = DM_ERR_OK;

		res = p.dmParseCheckElement(WBXML_METINF_Mem);
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
				tsLib.debugPrintException(dmDefineDevInfo.DEBUG_EXCEPTION, e.toString());
			}

			if (id == WBXML_END)
			{
				id = p.dmParseReadElement();
				break;
			}

			switch (id)
			{
				case WBXML_METINF_SharedMem:
					res = p.dmParseElement(id);
					shared = p._pParserElement;
					break;

				case WBXML_METINF_FreeMem:
					res = p.dmParseElement(id);
					free = Integer.parseInt(p._pParserElement);
					break;

				case WBXML_METINF_FreeID:
					res = p.dmParseElement(id);
					freeid = Integer.parseInt(p._pParserElement);
					break;

				default:
					res = DM_ERR_UNKNOWN_ELEMENT;
			}

			if (res != DM_ERR_OK)
			{
				return res;
			}
		}

		return DM_ERR_OK;
	}
}
