package com.tsdm.adapt;

import java.io.IOException;

import com.tsdm.agent.dmDefineDevInfo;

public class tsDmParserItem implements tsDefineWbxml, dmDefineDevInfo
{
	public String				target;
	public String				source;
	public tsDmParserMeta meta;
	public tsDmParserPcdata data;
	public int					moredata;

	public int dmParseItem(tsDmParser p, tsDmParserItem item)
	{
		int id = -1;
		int res = DM_ERR_OK;

		res = p.dmParseCheckElement(WBXML_TAG_Item);
		if (res != DM_ERR_OK)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, " res is not ok!!");
			return res;
		}

		res = p.dmParseZeroBitTagCheck();
		if (res == DM_ERR_ZEROBIT_TAG)
		{
			return DM_ERR_OK;
		}
		else if (res != DM_ERR_OK)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, " not OK");
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
				case WBXML_TAG_Target:
					res = p.dmParseTarget();
					target = p._pTarget;
					break;

				case WBXML_TAG_Source:
					res = p.dmParseSource();
					source = p.Source;
					break;

				case WBXML_TAG_Meta:
					meta = new tsDmParserMeta();
					res = meta.dmParseMeta(p);
					meta = p.Meta;
					break;

				case WBXML_TAG_Data:
					data = new tsDmParserPcdata();
					res = data.dmParsePcdata(p, id);
					break;

				case WBXML_TAG_MoreData:
					moredata = p.dmParseBlankElement(id);
					break;

				case WBXML_SWITCH_PAGE:
					id = p.dmParseReadElement();
					id = p.dmParseReadElement();

					p.codePage = id;
					break;

				default:
					// unknown element
					res = DM_ERR_UNKNOWN_ELEMENT;
			}

			if (res != DM_ERR_OK)
			{
				return res;
			}
		}

		tsLib.debugPrint(DEBUG_DM, " target = " + target);
		tsLib.debugPrint(DEBUG_DM, " source = " + source);
		tsLib.debugPrint(DEBUG_DM, " res  = " + res);

		//item = this;
		return res;
	}
}
