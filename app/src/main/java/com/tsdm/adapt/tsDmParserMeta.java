package com.tsdm.adapt;

import java.io.IOException;

import com.tsdm.agent.dmDefineDevInfo;

public class tsDmParserMeta implements tsDefineWbxml
{
	public String				type;
	public String				format;
	public String				mark;
	public int					size;
	public String				nextnonce;
	public String				version;
	public int					maxmsgsize;
	public int					maxobjsize;
	public tsDmParserMem mem;
	public String				emi;
	public tsDmParserAnchor anchor;

	public int dmParseMeta(tsDmParser p)
	{
		int id = -1;
		int res = DM_ERR_OK;

		res = p.dmParseCheckElement(WBXML_TAG_Meta);
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
			return res;
		}

		// check namespace
		res = p.dmParseCheckElement(WBXML_SWITCH_PAGE);
		if (res != DM_ERR_OK)
		{
			return res;
		}

		res = p.dmParseCheckElement(WBXML_PAGE_METINF);
		if (res != DM_ERR_OK)
		{
			return res;
		}

		p.codePage = WBXML_CODEPAGE_METINF;

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

			// MetInf (Format?, Type?, Mark?, Size?, Anchor?, Version?, NextNonce?, MaxMsgSize?, MaxObjSize?, EMI*, Mem?)
			switch (id)
			{
				case WBXML_SWITCH_PAGE:
					id = p.dmParseReadElement();
					id = p.dmParseReadElement();

					p.codePage = id;
					break;

				case WBXML_METINF_Type:
					res = p.dmParseElement(id);
					type = p._pParserElement;
					break;
				case WBXML_METINF_Format:
					res = p.dmParseElement(id);
					format = p._pParserElement;
					break;
				case WBXML_METINF_Mark:
					res = p.dmParseElement(id);
					mark = p._pParserElement;
					break;
				case WBXML_METINF_Size:
					res = p.dmParseElement(id);
					size = Integer.valueOf(p._pParserElement);
					break;
				case WBXML_METINF_NextNonce:
					res = p.dmParseElement(id);
					nextnonce = p._pParserElement;
					break;
				case WBXML_METINF_Version:
					res = p.dmParseElement(id);
					version = p._pParserElement;
					break;
				case WBXML_METINF_MaxMsgSize:
					res = p.dmParseElement(id);
					maxmsgsize = Integer.parseInt(p._pParserElement);
					break;
				case WBXML_METINF_MaxObjSize:
					res = p.dmParseElement(id);
					maxobjsize = Integer.parseInt(p._pParserElement);
					break;
				case WBXML_METINF_EMI:
					res = p.dmParseElement(id);
					emi = p._pParserElement;
					break;
				case WBXML_METINF_Mem:
					mem = new tsDmParserMem();
					mem.dmParseMem(p);
					break;
				case WBXML_METINF_Anchor:
					anchor = new tsDmParserAnchor();
					anchor.dmParseAnchor(p);
					break;
				default:
					res = DM_ERR_UNKNOWN_ELEMENT;
			}

			if (res != DM_ERR_OK)
			{
				return res;
			}
		}

		p.Meta = this;
		return res;
	}

}
