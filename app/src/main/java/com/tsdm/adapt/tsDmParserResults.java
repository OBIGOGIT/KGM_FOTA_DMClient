package com.tsdm.adapt;

import java.io.IOException;

import com.tsdm.agent.dmDefineDevInfo;

public class tsDmParserResults implements tsDefineWbxml, dmDefineDevInfo
{
	public int				cmdid;
	public String			msgref;
	public String			cmdref;
	public String			targetref;
	public String			sourceref;
	public tsList itemlist	= null;
	public tsDmParserMeta meta;

	public int dmParseResults(tsDmParser p)
	{
		int id = -1;
		int res = DM_ERR_OK;

		res = p.dmParseCheckElement(WBXML_TAG_Results);

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
				id = p.dmParseReadElement();
				break;
			}

			switch (id)
			{
				case WBXML_TAG_CmdID:
					res = p.dmParseElement(id);
					cmdid = Integer.parseInt(p._pParserElement);
					break;

				case WBXML_TAG_MsgRef:
					res = p.dmParseElement(id);
					msgref = p._pParserElement;
					break;

				case WBXML_TAG_CmdRef:
					res = p.dmParseElement(id);
					cmdref = p._pParserElement;
					break;

				case WBXML_TAG_Meta:
					meta = new tsDmParserMeta();
					res = meta.dmParseMeta(p);
					meta = p.Meta;
					break;

				case WBXML_TAG_TargetRef:
					res = p.dmParseElement(id);
					targetref = p._pParserElement;
					break;

				case WBXML_TAG_SourceRef:
					res = p.dmParseElement(id);
					sourceref = p._pParserElement;
					break;

				case WBXML_TAG_Item:
					itemlist = p.dmParseItemlist(itemlist);
					break;

				case WBXML_SWITCH_PAGE:
					id = p.dmParseReadElement();
					id = p.dmParseReadElement();

					p.codePage = id;
					break;

				default:
					res = DM_ERR_UNKNOWN_ELEMENT;
					break;
			}

			if (res != DM_ERR_OK)
			{
				return res;
			}
		}

		return DM_ERR_OK;
	}
}
