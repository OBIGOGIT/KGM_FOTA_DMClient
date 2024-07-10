package com.tsdm.adapt;

import java.io.IOException;

import com.tsdm.agent.dmDefineDevInfo;

public class tsdmParserStatus extends tsDmHandlecmd implements tsDefineWbxml, dmDefineDevInfo
{
	public int				cmdid;
	public String			msgref;
	public String			cmdref;
	public String			cmd;
	public tsList targetref;
	public tsList sourceref;
	public tsDmParserCred cred		= null;
	public tsDmParserMeta chal		= null;
	public String			data;
	public tsList itemlist	= null;

	public int dmParseStatus(tsDmParser p)
	{
		int id = -1;
		int res = DM_ERR_OK;

		res = p.dmParseCheckElement(WBXML_TAG_Status);
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
				tsLib.debugPrint(DEBUG_DM, " WBXML_END");
				break;
			}

			if (id == 41)
			{
				p.dmParseReadElement();
				continue;
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

				case WBXML_TAG_Cmd:
					res = p.dmParseElement(id);
					cmd = p._pParserElement;
					break;

				case WBXML_TAG_TargetRef:
					targetref = new tsList();
					targetref = p.dmParseElelist(id, targetref);
					break;

				case WBXML_TAG_SourceRef:
					sourceref = new tsList();
					sourceref = p.dmParseElelist(id, sourceref);
					break;

				case WBXML_TAG_Cred:
					cred = new tsDmParserCred();
					res = cred.dmParseCred(p);
					cred = p.Cred;
					break;

				case WBXML_TAG_Chal:
					chal = new tsDmParserMeta();
					res = p.dmParseChal();
					chal = p.Chal;
					break;

				case WBXML_TAG_Data:
					res = p.dmParseElement(id);
					data = p._pParserElement;
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
					tsLib.debugPrintException(DEBUG_EXCEPTION, "ERR_UNKNOWN_ELEMENT !!!!!!!");
					res = DM_ERR_UNKNOWN_ELEMENT;

			} // end switch

			if (res != DM_ERR_OK)
			{
				return res;
			}
		}

		tsLib.debugPrint(DEBUG_DM, "WBXML_TAG_CmdID cmdid =" + cmdid);
		tsLib.debugPrint(DEBUG_DM, "WBXML_TAG_MsgRef msgref =" + msgref);
		tsLib.debugPrint(DEBUG_DM, "WBXML_TAG_CmdRef cmdref =" + cmdref);
		tsLib.debugPrint(DEBUG_DM, "WBXML_TAG_Cmd cmd =" + cmd);
		tsLib.debugPrint(DEBUG_DM, "WBXML_TAG_Data data =" + data);

		dmHdlCmdStatus(p.userdata, this);
		return DM_ERR_OK;
	}
}
