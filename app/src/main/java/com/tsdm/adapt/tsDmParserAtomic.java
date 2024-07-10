package com.tsdm.adapt;

import java.io.IOException;

public class tsDmParserAtomic extends tsDmHandlecmd implements tsDefineWbxml
{
	public int				cmdid;
	public int				is_noresp;
	public tsDmParserMeta meta;
	public tsLinkedList itemlist;

	public int dmParseAtomic(tsDmParser p)
	{
		int id = -1;
		int res = DM_ERR_OK;
		boolean call_start_atomic = true;

		res = p.dmParseCheckElement(WBXML_TAG_Atomic);
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
				break;
			}

			switch (id)
			{
				// parse atomic element information
				case WBXML_TAG_CmdID:
					res = p.dmParseElement(id);
					cmdid = Integer.parseInt(p._pParserElement);
					break;

				case WBXML_TAG_NoResp:
					is_noresp = p.dmParseBlankElement(id);
					break;

				case WBXML_TAG_Meta:
					meta = new tsDmParserMeta();
					res = meta.dmParseMeta(p);
					meta = p.Meta;
					break;
				// parse atomic element commands(child elements)
				case WBXML_TAG_Add:
					if (call_start_atomic)
					{
						dmHdlCmdAtomicStart(p.userdata, this);
						call_start_atomic = false;
					}

					tsDmParserAdd add = new tsDmParserAdd();
					res = add.dmParseAdd(p);
					break;
				case WBXML_TAG_Delete:
					if (call_start_atomic)
					{
						dmHdlCmdAtomicStart(p.userdata, this);
						call_start_atomic = false;
					}

					tsDmParserDelete delete = new tsDmParserDelete();
					res = delete.dmParseDelete(p);
					break;
				case WBXML_TAG_Exec:
					if (call_start_atomic)
					{
						dmHdlCmdAtomicStart(p.userdata, this);
						call_start_atomic = false;
					}
					tsDmParserExec exec = new tsDmParserExec();
					res = exec.dmParseExec(p);
					break;
				case WBXML_TAG_Copy:
					if (call_start_atomic)
					{
						dmHdlCmdAtomicStart(p.userdata, this);
						call_start_atomic = false;
					}
					tsDmParserCopy copy = new tsDmParserCopy();
					res = copy.dmParseCopy(p);
					break;
				case WBXML_TAG_Atomic:
					if (call_start_atomic)
					{
						dmHdlCmdAtomicStart(p.userdata, this);
						call_start_atomic = false;
					}

					res = dmParseAtomic(p);
					break;
				case WBXML_TAG_Map:
					if (call_start_atomic)
					{
						dmHdlCmdAtomicStart(p.userdata, this);
						call_start_atomic = false;
					}

					tsDmParserMap map = new tsDmParserMap();
					res = map.dmParseMap(p);
					break;

				case WBXML_TAG_Replace:
					if (call_start_atomic)
					{
						dmHdlCmdAtomicStart(p.userdata, this);
						call_start_atomic = false;
					}

					tsDmParserReplace replace = new tsDmParserReplace();
					res = replace.dmParseReplace(p);
					break;

				case WBXML_TAG_Sequence:
					if (call_start_atomic)
					{
						dmHdlCmdAtomicStart(p.userdata, this);
						call_start_atomic = false;
					}
					tsDmParserSequence sequence = new tsDmParserSequence();
					res = sequence.dmParseSequence(p);
					break;

				case WBXML_TAG_Sync:
					if (call_start_atomic)
					{
						dmHdlCmdAtomicStart(p.userdata, this);
						call_start_atomic = false;
					}

					tsDmParserSync sync = new tsDmParserSync();
					res = sync.dmParseSync(p);
					break;

				case WBXML_SWITCH_PAGE:
					id = p.dmParseReadElement();
					id = p.dmParseReadElement();

					p.codePage = id;
					break;

				default:
					res = DM_ERR_UNKNOWN_ELEMENT;
					break;
			}

			if (res != DM_ERR_OK)
			{
				return res;
			}
		}

		if (call_start_atomic)
		{
			dmHdlCmdAtomicStart(p.userdata, this);
			call_start_atomic = false;
		}

		dmHdlCmdAtomicEnd(p.userdata);
		return DM_ERR_OK;
	}
}
