package com.tsdm.adapt;

import java.io.IOException;

public class tsDmParserSyncheader extends tsDmHandlecmd implements tsDefineWbxml
{
	public String			verdtd;
	public String			verproto;
	public String			sessionid;
	public int				msgid;
	public String			target;
	public String			source;
	public String			locname;
	public String			respuri;
	public int				is_noresp;
	public tsDmParserCred cred;
	public tsDmParserMeta meta;

	public int dmParseSyncheader(tsDmParser parser)
	{
		int id = -1;
		int res = DM_ERR_OK;

		res = parser.dmParseCheckElement(WBXML_TAG_SyncHdr);
		if (res != DM_ERR_OK)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, " not DM_ERR_OK");
			return res;
		}

		res = parser.dmParseZeroBitTagCheck();
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
				id = parser.dmParseCurrentElement();
			}
			catch (IOException e)
			{
				tsLib.debugPrintException(DEBUG_EXCEPTION, " error = " + e.toString());
			}

			if (id == WBXML_END)
			{
				id = parser.dmParseReadElement();
				break;
			}

			switch (id)
			{
				case WBXML_TAG_VerDTD:
					res = parser.dmParseElement(id);
					verdtd = parser._pParserElement;
					break;

				case WBXML_TAG_VerProto:
					res = parser.dmParseElement(id);
					verproto = parser._pParserElement;
					break;

				case WBXML_TAG_SessionID:
					res = parser.dmParseElement(id);
					sessionid = parser._pParserElement;
					break;

				case WBXML_TAG_MsgID:
					res = parser.dmParseElement(id);
					msgid = Integer.parseInt(parser._pParserElement);
					break;

				case WBXML_TAG_RespURI:
					res = parser.dmParseElement(id);
					respuri = parser._pParserElement;
					break;

				case WBXML_TAG_Source:
					res = parser.dmParseSource();
					source = parser._pTarget;
					break;

				case WBXML_TAG_Target:
					res = parser.dmParseTarget();
					target = parser.Source;
					break;

				case WBXML_TAG_NoResp:
					is_noresp = parser.dmParseBlankElement(id);
					break;

				case WBXML_TAG_Meta:
					meta = new tsDmParserMeta();
					res = meta.dmParseMeta(parser);
					meta = parser.Meta;
					break;

				case WBXML_TAG_Cred:
					cred = new tsDmParserCred();
					res = cred.dmParseCred(parser);
					cred = parser.Cred;
					break;

				case WBXML_SWITCH_PAGE:
					id = parser.dmParseReadElement();
					id = parser.dmParseReadElement();

					parser.codePage = id;
					break;

				default:
					res = DM_ERR_UNKNOWN_ELEMENT;
			}

			if (res != DM_ERR_OK)
			{
				tsLib.debugPrintException(DEBUG_EXCEPTION, " res not  DM_ERR_OK");
				return res;
			}
		}

		tsLib.debugPrint(DEBUG_DM, " verproto : " + verproto);
		tsLib.debugPrint(DEBUG_DM, " sessionid : " + sessionid);
		tsLib.debugPrint(DEBUG_DM, " target : " + target);
		tsLib.debugPrint(DEBUG_DM, " msgid : " + msgid);
		tsLib.debugPrint(DEBUG_DM, " source : " + source);
		tsLib.debugPrint(DEBUG_DM, " locname : " + locname);
		tsLib.debugPrint(DEBUG_DM, " respuri : " + respuri);
		tsLib.debugPrint(DEBUG_DM, " is_noresp : " + is_noresp);

		dmHdlCmdSyncHdr(parser.userdata, this);

		return DM_ERR_OK;
	}
}
