package com.tsdm.adapt;

import com.tsdm.core.data.constants.DmDevInfoConst;
import com.tsdm.core.data.constants.WbxmlProtocolConst;

import java.io.IOException;

public class tsDmParserSyncheader extends tsDmHandlecmd
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
		int res = WbxmlProtocolConst.DM_ERR_OK;

		res = parser.dmParseCheckElement(WbxmlProtocolConst.WBXML_TAG_SyncHdr);
		if (res != WbxmlProtocolConst.DM_ERR_OK)
		{
			tsLib.debugPrintException(DmDevInfoConst.DEBUG_EXCEPTION, " not DM_ERR_OK");
			return res;
		}

		res = parser.dmParseZeroBitTagCheck();
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
				id = parser.dmParseCurrentElement();
			}
			catch (IOException e)
			{
				tsLib.debugPrintException(DmDevInfoConst.DEBUG_EXCEPTION, " error = " + e.toString());
			}

			if (id == WbxmlProtocolConst.WBXML_END)
			{
				id = parser.dmParseReadElement();
				break;
			}

			switch (id)
			{
				case WbxmlProtocolConst.WBXML_TAG_VerDTD:
					res = parser.dmParseElement(id);
					verdtd = parser._pParserElement;
					break;

				case WbxmlProtocolConst.WBXML_TAG_VerProto:
					res = parser.dmParseElement(id);
					verproto = parser._pParserElement;
					break;

				case WbxmlProtocolConst.WBXML_TAG_SessionID:
					res = parser.dmParseElement(id);
					sessionid = parser._pParserElement;
					break;

				case WbxmlProtocolConst.WBXML_TAG_MsgID:
					res = parser.dmParseElement(id);
					msgid = Integer.parseInt(parser._pParserElement);
					break;

				case WbxmlProtocolConst.WBXML_TAG_RespURI:
					res = parser.dmParseElement(id);
					respuri = parser._pParserElement;
					break;

				case WbxmlProtocolConst.WBXML_TAG_Source:
					res = parser.dmParseSource();
					source = parser._pTarget;
					break;

				case WbxmlProtocolConst.WBXML_TAG_Target:
					res = parser.dmParseTarget();
					target = parser.Source;
					break;

				case WbxmlProtocolConst.WBXML_TAG_NoResp:
					is_noresp = parser.dmParseBlankElement(id);
					break;

				case WbxmlProtocolConst.WBXML_TAG_Meta:
					meta = new tsDmParserMeta();
					res = meta.dmParseMeta(parser);
					meta = parser.Meta;
					break;

				case WbxmlProtocolConst.WBXML_TAG_Cred:
					cred = new tsDmParserCred();
					res = cred.dmParseCred(parser);
					cred = parser.Cred;
					break;

				case WbxmlProtocolConst.WBXML_SWITCH_PAGE:
					id = parser.dmParseReadElement();
					id = parser.dmParseReadElement();

					parser.codePage = id;
					break;

				default:
					res = WbxmlProtocolConst.DM_ERR_UNKNOWN_ELEMENT;
			}

			if (res != WbxmlProtocolConst.DM_ERR_OK)
			{
				tsLib.debugPrintException(DmDevInfoConst.DEBUG_EXCEPTION, " res not  DM_ERR_OK");
				return res;
			}
		}

		tsLib.debugPrint(DmDevInfoConst.DEBUG_DM, " verproto : " + verproto);
		tsLib.debugPrint(DmDevInfoConst.DEBUG_DM, " sessionid : " + sessionid);
		tsLib.debugPrint(DmDevInfoConst.DEBUG_DM, " target : " + target);
		tsLib.debugPrint(DmDevInfoConst.DEBUG_DM, " msgid : " + msgid);
		tsLib.debugPrint(DmDevInfoConst.DEBUG_DM, " source : " + source);
		tsLib.debugPrint(DmDevInfoConst.DEBUG_DM, " locname : " + locname);
		tsLib.debugPrint(DmDevInfoConst.DEBUG_DM, " respuri : " + respuri);
		tsLib.debugPrint(DmDevInfoConst.DEBUG_DM, " is_noresp : " + is_noresp);

		dmHdlCmdSyncHdr(parser.userdata, this);

		return WbxmlProtocolConst.DM_ERR_OK;
	}
}
