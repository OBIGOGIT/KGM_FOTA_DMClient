package com.tsdm.adapt;

import java.io.IOException;

import com.tsdm.core.data.constants.DmDevInfoConst;
import com.tsdm.core.data.constants.WbxmlProtocolConst;

public class tsDmParserItem
{
	public String				target;
	public String				source;
	public tsDmParserMeta meta;
	public tsDmParserPcdata data;
	public int					moredata;

	public int dmParseItem(tsDmParser p, tsDmParserItem item)
	{
		int id = -1;
		int res = WbxmlProtocolConst.DM_ERR_OK;

		res = p.dmParseCheckElement(WbxmlProtocolConst.WBXML_TAG_Item);
		if (res != WbxmlProtocolConst.DM_ERR_OK)
		{
			tsLib.debugPrintException(DmDevInfoConst.DEBUG_EXCEPTION, " res is not ok!!");
			return res;
		}

		res = p.dmParseZeroBitTagCheck();
		if (res == WbxmlProtocolConst.DM_ERR_ZEROBIT_TAG)
		{
			return WbxmlProtocolConst.DM_ERR_OK;
		}
		else if (res != WbxmlProtocolConst.DM_ERR_OK)
		{
			tsLib.debugPrintException(DmDevInfoConst.DEBUG_EXCEPTION, " not OK");
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
				case WbxmlProtocolConst.WBXML_TAG_Target:
					res = p.dmParseTarget();
					target = p._pTarget;
					break;

				case WbxmlProtocolConst.WBXML_TAG_Source:
					res = p.dmParseSource();
					source = p.Source;
					break;

				case WbxmlProtocolConst.WBXML_TAG_Meta:
					meta = new tsDmParserMeta();
					res = meta.dmParseMeta(p);
					meta = p.Meta;
					break;

				case WbxmlProtocolConst.WBXML_TAG_Data:
					data = new tsDmParserPcdata();
					res = data.dmParsePcdata(p, id);
					break;

				case WbxmlProtocolConst.WBXML_TAG_MoreData:
					moredata = p.dmParseBlankElement(id);
					break;

				case WbxmlProtocolConst.WBXML_SWITCH_PAGE:
					id = p.dmParseReadElement();
					id = p.dmParseReadElement();

					p.codePage = id;
					break;

				default:
					// unknown element
					res = WbxmlProtocolConst.DM_ERR_UNKNOWN_ELEMENT;
			}

			if (res != WbxmlProtocolConst.DM_ERR_OK)
			{
				return res;
			}
		}

		tsLib.debugPrint(DmDevInfoConst.DEBUG_DM, " target = " + target);
		tsLib.debugPrint(DmDevInfoConst.DEBUG_DM, " source = " + source);
		tsLib.debugPrint(DmDevInfoConst.DEBUG_DM, " res  = " + res);

		//item = this;
		return res;
	}
}
