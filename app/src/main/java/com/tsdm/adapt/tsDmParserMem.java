package com.tsdm.adapt;

import java.io.IOException;

import com.tsdm.core.data.constants.DmDevInfoConst;
import com.tsdm.core.data.constants.WbxmlProtocolConst;

public class tsDmParserMem
{
	public String	shared;
	public int		free;
	public int		freeid;

	public int dmParseMem(tsDmParser p)
	{
		int id = -1;
		int res = WbxmlProtocolConst.DM_ERR_OK;

		res = p.dmParseCheckElement(WbxmlProtocolConst.WBXML_METINF_Mem);
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
			return res;
		}

		res = p.dmParseSkipLiteralElement();
		if (res != WbxmlProtocolConst.DM_ERR_OK)
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
				tsLib.debugPrintException(DmDevInfoConst.DEBUG_EXCEPTION, e.toString());
			}

			if (id == WbxmlProtocolConst.WBXML_END)
			{
				id = p.dmParseReadElement();
				break;
			}

			switch (id)
			{
				case WbxmlProtocolConst.WBXML_METINF_SharedMem:
					res = p.dmParseElement(id);
					shared = p._pParserElement;
					break;

				case WbxmlProtocolConst.WBXML_METINF_FreeMem:
					res = p.dmParseElement(id);
					free = Integer.parseInt(p._pParserElement);
					break;

				case WbxmlProtocolConst.WBXML_METINF_FreeID:
					res = p.dmParseElement(id);
					freeid = Integer.parseInt(p._pParserElement);
					break;

				default:
					res = WbxmlProtocolConst.DM_ERR_UNKNOWN_ELEMENT;
			}

			if (res != WbxmlProtocolConst.DM_ERR_OK)
			{
				return res;
			}
		}

		return WbxmlProtocolConst.DM_ERR_OK;
	}
}
