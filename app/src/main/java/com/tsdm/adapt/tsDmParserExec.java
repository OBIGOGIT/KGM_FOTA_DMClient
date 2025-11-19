package com.tsdm.adapt;

import com.tsdm.core.data.constants.DmDevInfoConst;
import com.tsdm.core.data.constants.WbxmlProtocolConst;

import java.io.IOException;

public class tsDmParserExec extends tsDmHandlecmd
{
	public int				cmdid;
	public int				is_noresp;
	public String			correlator;
	public tsDmParserMeta meta;
	public tsList itemlist	= null;

	public int dmParseExec(tsDmParser p)
	{
		int id = -1;
		int res = WbxmlProtocolConst.DM_ERR_OK;

		res = p.dmParseCheckElement(WbxmlProtocolConst.WBXML_TAG_Exec);
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
			tsLib.debugPrintException(DmDevInfoConst.DEBUG_EXCEPTION, " not DM_ERR_OK");
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
				p.dmParseReadElement();
				break;
			}

			switch (id)
			{
				case WbxmlProtocolConst.WBXML_TAG_CmdID:
					res = p.dmParseElement(id);
					cmdid = Integer.parseInt(p._pParserElement);
					break;

				case WbxmlProtocolConst.WBXML_TAG_NoResp:
					is_noresp = p.dmParseBlankElement(id);
					break;

				case WbxmlProtocolConst.WBXML_TAG_Meta:
					meta = new tsDmParserMeta();
					res = meta.dmParseMeta(p);
					meta = p.Meta;
					break;

				case WbxmlProtocolConst.WBXML_TAG_Item:
					itemlist = p.dmParseItemlist(itemlist);
					break;

				case WbxmlProtocolConst.WBXML_TAG_Correlator:
					res = p.dmParseElement(id);
					correlator = p._pParserElement;
					break;

				case WbxmlProtocolConst.WBXML_SWITCH_PAGE:
					id = p.dmParseReadElement();
					id = p.dmParseReadElement();

					p.codePage = id;
					break;

				default:
					res = WbxmlProtocolConst.DM_ERR_UNKNOWN_ELEMENT;
			}

			if (res != WbxmlProtocolConst.DM_ERR_OK)
			{
				return res;
			}
		}

		dmHdlCmdExec(p.userdata, this);
		return WbxmlProtocolConst.DM_ERR_OK;
	}
}
