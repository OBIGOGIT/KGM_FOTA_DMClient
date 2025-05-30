package com.tsdm.adapt;

import java.io.IOException;

public class tsDmParserExec extends tsDmHandlecmd implements tsDefineWbxml
{
	public int				cmdid;
	public int				is_noresp;
	public String			correlator;
	public tsDmParserMeta meta;
	public tsList itemlist	= null;

	public int dmParseExec(tsDmParser p)
	{
		int id = -1;
		int res = DM_ERR_OK;

		res = p.dmParseCheckElement(WBXML_TAG_Exec);
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
				p.dmParseReadElement();
				break;
			}

			switch (id)
			{
				case WBXML_TAG_CmdID:
					res = p.dmParseElement(id);
					cmdid = Integer.parseInt(p._pParserElement);
					break;

				case WBXML_TAG_NoResp:
					is_noresp = p.dmParseBlankElement(id);
					break;

				case WBXML_TAG_Meta:
					meta = new tsDmParserMeta();
					res = meta.dmParseMeta(p);
					meta = p.Meta;
					break;

				case WBXML_TAG_Item:
					itemlist = p.dmParseItemlist(itemlist);
					break;

				case WBXML_TAG_Correlator:
					res = p.dmParseElement(id);
					correlator = p._pParserElement;
					break;

				case WBXML_SWITCH_PAGE:
					id = p.dmParseReadElement();
					id = p.dmParseReadElement();

					p.codePage = id;
					break;

				default:
					res = DM_ERR_UNKNOWN_ELEMENT;
			}

			if (res != DM_ERR_OK)
			{
				return res;
			}
		}

		dmHdlCmdExec(p.userdata, this);
		return DM_ERR_OK;
	}
}
