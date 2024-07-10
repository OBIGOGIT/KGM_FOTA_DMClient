package com.tsdm.adapt;

import java.io.IOException;

import com.tsdm.agent.dmDefineDevInfo;

public class tsDmParserCred implements tsDefineWbxml
{
	public tsDmParserMeta meta;
	public String			data;

	public int dmParseCred(tsDmParser p)
	{
		int id = -1;
		int res = DM_ERR_OK;

		res = p.dmParseCheckElement(WBXML_TAG_Cred);
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
				case WBXML_TAG_Meta:
					meta = new tsDmParserMeta();
					res = meta.dmParseMeta(p);
					meta = p.Meta;
					break;

				case WBXML_TAG_Data:
					res = p.dmParseElement(id);
					data = p._pParserElement;
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

		p.Cred = this;
		return res;
	}

}
