package com.tsdm.adapt;

import java.io.IOException;

import com.tsdm.core.data.constants.DmDevInfoConst;
import com.tsdm.core.data.constants.WbxmlProtocolConst;

public class tsDmParserMap
{
	public int				cmdid;
	public String			target;
	public String			source;
	public tsDmParserCred cred;
	public tsDmParserMeta meta;
	public tsList itemlist;

	public int dmParseMap(tsDmParser p)
	{
		int id = -1;
		int res = WbxmlProtocolConst.DM_ERR_OK;

		res = p.dmParseCheckElement(WbxmlProtocolConst.WBXML_TAG_Map);
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

				case WbxmlProtocolConst.WBXML_TAG_Target:
					res = p.dmParseTarget();
					target = p._pTarget;
					break;

				case WbxmlProtocolConst.WBXML_TAG_Source:
					res = p.dmParseTarget();
					source = p.Source;
					break;

				case WbxmlProtocolConst.WBXML_TAG_Cred:
					cred = new tsDmParserCred();
					res = cred.dmParseCred(p);
					cred = p.Cred;
					break;

				case WbxmlProtocolConst.WBXML_TAG_Meta:
					meta = new tsDmParserMeta();
					res = meta.dmParseMeta(p);
					meta = p.Meta;
					break;

				case WbxmlProtocolConst.WBXML_TAG_MapItem:
					itemlist = p.dmParseMapitemlist(itemlist);
					break;

				case WbxmlProtocolConst.WBXML_SWITCH_PAGE:
					id = p.dmParseReadElement();
					id = p.dmParseReadElement();

					if (res != WbxmlProtocolConst.DM_ERR_OK)
					{
						return res;
					}

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
		return WbxmlProtocolConst.DM_ERR_OK;
	}
}
