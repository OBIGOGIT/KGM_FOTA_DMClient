package com.tsdm.adapt;

import java.io.IOException;

import com.tsdm.core.data.constants.DmDevInfoConst;
import com.tsdm.core.data.constants.WbxmlProtocolConst;

public class tsDmParserResults
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
		int res = WbxmlProtocolConst.DM_ERR_OK;

		res = p.dmParseCheckElement(WbxmlProtocolConst.WBXML_TAG_Results);

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
				id = p.dmParseReadElement();
				break;
			}

			switch (id)
			{
				case WbxmlProtocolConst.WBXML_TAG_CmdID:
					res = p.dmParseElement(id);
					cmdid = Integer.parseInt(p._pParserElement);
					break;

				case WbxmlProtocolConst.WBXML_TAG_MsgRef:
					res = p.dmParseElement(id);
					msgref = p._pParserElement;
					break;

				case WbxmlProtocolConst.WBXML_TAG_CmdRef:
					res = p.dmParseElement(id);
					cmdref = p._pParserElement;
					break;

				case WbxmlProtocolConst.WBXML_TAG_Meta:
					meta = new tsDmParserMeta();
					res = meta.dmParseMeta(p);
					meta = p.Meta;
					break;

				case WbxmlProtocolConst.WBXML_TAG_TargetRef:
					res = p.dmParseElement(id);
					targetref = p._pParserElement;
					break;

				case WbxmlProtocolConst.WBXML_TAG_SourceRef:
					res = p.dmParseElement(id);
					sourceref = p._pParserElement;
					break;

				case WbxmlProtocolConst.WBXML_TAG_Item:
					itemlist = p.dmParseItemlist(itemlist);
					break;

				case WbxmlProtocolConst.WBXML_SWITCH_PAGE:
					id = p.dmParseReadElement();
					id = p.dmParseReadElement();

					p.codePage = id;
					break;

				default:
					res = WbxmlProtocolConst.DM_ERR_UNKNOWN_ELEMENT;
					break;
			}

			if (res != WbxmlProtocolConst.DM_ERR_OK)
			{
				return res;
			}
		}

		return WbxmlProtocolConst.DM_ERR_OK;
	}
}
