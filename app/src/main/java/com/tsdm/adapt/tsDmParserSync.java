package com.tsdm.adapt;

import java.io.IOException;

import com.tsdm.agent.dmDefineDevInfo;

public class tsDmParserSync implements tsDefineWbxml, dmDefineDevInfo
{
	public int				cmdid;
	public boolean			is_noresp;
	public boolean			is_noresults;
	public tsDmParserCred cred;
	public String			target;
	public String			source;
	public int				numofchanges;
	public tsDmParserMeta meta;

	public int dmParseSync(tsDmParser p)
	{
		int id = -1;
		int res = DM_ERR_OK;
		boolean call_start_sync = true;

		tsLib.debugPrint(DEBUG_DM, "");

		res = p.dmParseCheckElement(WBXML_TAG_Sync);
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

				case WBXML_TAG_NoResp:
					int noresp = p.dmParseBlankElement(id);
					if (noresp == 1)
						is_noresp = true;
					else
						is_noresp = false;
					break;

				case WBXML_TAG_NoResults:
					int noresults = p.dmParseBlankElement(id);
					if (noresults == 1)
						is_noresults = true;
					else
						is_noresults = false;
					break;

				case WBXML_TAG_Cred:
					cred = new tsDmParserCred();
					res = cred.dmParseCred(p);
					cred = p.Cred;
					break;

				case WBXML_TAG_Target:
					res = p.dmParseElement(id);
					target = p._pTarget;
					break;

				case WBXML_TAG_Source:
					res = p.dmParseElement(id);
					source = p.Source;
					break;

				case WBXML_TAG_Meta:
					meta = new tsDmParserMeta();
					res = meta.dmParseMeta(p);
					meta = p.Meta;
					break;

				case WBXML_TAG_NumberOfChanges:
					res = p.dmParseElement(id);
					numofchanges = Integer.parseInt(p._pParserElement);
					break;

				case WBXML_TAG_Copy:
					if (call_start_sync)
					{
						call_start_sync = false;
					}
					tsDmParserCopy copy = new tsDmParserCopy();
					res = copy.dmParseCopy(p);
					break;

				case WBXML_TAG_Sequence:
					if (call_start_sync)
					{
						call_start_sync = false;
					}
					tsDmParserSequence sequence = new tsDmParserSequence();
					res = sequence.dmParseSequence(p);
					break;

				case WBXML_TAG_Atomic:
					if (call_start_sync)
					{
						call_start_sync = false;
					}
					tsDmParserAtomic atomic = new tsDmParserAtomic();
					res = atomic.dmParseAtomic(p);
					break;
				// parse sync command and etc...
				case WBXML_TAG_Add:
					tsLib.debugPrint(DEBUG_DM, " : WBXML_TAG_Add");
					if (call_start_sync)
					{
						call_start_sync = false;
					}
					tsDmParserAdd add = new tsDmParserAdd();
					res = add.dmParseAdd(p);
					break;

				case WBXML_TAG_Replace:
					if (call_start_sync)
					{
						call_start_sync = false;
					}
					tsDmParserReplace replace = new tsDmParserReplace();
					res = replace.dmParseReplace(p);
					break;

				case WBXML_TAG_Delete:
					if (call_start_sync)
					{
						call_start_sync = false;
					}
					tsDmParserDelete delete = new tsDmParserDelete();
					res = delete.dmParseDelete(p);
					break;

				case WBXML_SWITCH_PAGE:
					id = p.dmParseReadElement();
					id = p.dmParseReadElement();

					p.codePage = id;
					break;

				default:
					res = DM_ERR_UNKNOWN_ELEMENT;
			}

			if (res != DM_ERR_OK)
			{
				return res;
			}
		}

		if (call_start_sync)
		{
			call_start_sync = false;
		}

		return DM_ERR_OK;

	}
}
