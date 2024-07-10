package com.tsdm.adapt;

import java.io.IOException;

public class tsDmParserSequence extends tsDmHandlecmd implements tsDefineWbxml
{
	public int					cmdid;
	public int					is_noresp;
	public tsDmParserMeta meta;
	public tsDmParserAlert alert;
	public tsDmParserAdd add;
	public tsDmParserReplace replace;
	public tsDmParserDelete delete;
	public tsDmParserCopy copy;
	public tsDmParserMap map;
	public tsDmParserAtomic atomic;
	public tsDmParserGet get;
	public tsDmParserSync sync;
	public tsDmParserExec exec;
	public tsLinkedList itemlist;

	public int dmParseSequence(tsDmParser p)
	{
		int id = -1;
		int res = DM_ERR_OK;
		boolean call_start_seq = true;

		res = p.dmParseCheckElement(WBXML_TAG_Sequence);
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
					is_noresp = p.dmParseBlankElement(id);
					break;

				case WBXML_TAG_Meta:
					meta = new tsDmParserMeta();
					res = meta.dmParseMeta(p);
					meta = p.Meta;
					break;

				case WBXML_TAG_Alert:
					if (call_start_seq)
					{
						dmHdlCmdSequenceStart(p.userdata, this);
						call_start_seq = false;
					}
					alert = new tsDmParserAlert();
					alert.smParseAlert(p);
					break;
				case WBXML_TAG_Add:
					if (call_start_seq)
					{
						dmHdlCmdSequenceStart(p.userdata, this);
						call_start_seq = false;
					}
					add = new tsDmParserAdd();
					add.dmParseAdd(p);
					break;
				case WBXML_TAG_Replace:
					if (call_start_seq)
					{
						dmHdlCmdSequenceStart(p.userdata, this);
						call_start_seq = false;
					}
					replace = new tsDmParserReplace();
					replace.dmParseReplace(p);
					break;
				case WBXML_TAG_Delete:
					if (call_start_seq)
					{
						dmHdlCmdSequenceStart(p.userdata, this);
						call_start_seq = false;
					}
					delete = new tsDmParserDelete();
					delete.dmParseDelete(p);
					break;
				case WBXML_TAG_Copy:
					if (call_start_seq)
					{
						dmHdlCmdSequenceStart(p.userdata, this);
						call_start_seq = false;
					}
					copy = new tsDmParserCopy();
					copy.dmParseCopy(p);
					break;
				case WBXML_TAG_Atomic:
					if (call_start_seq)
					{
						dmHdlCmdSequenceStart(p.userdata, this);
						call_start_seq = false;
					}
					atomic = new tsDmParserAtomic();
					atomic.dmParseAtomic(p);
					break;
				case WBXML_TAG_Map:
					if (call_start_seq)
					{
						dmHdlCmdSequenceStart(p.userdata, this);
						call_start_seq = false;
					}
					map = new tsDmParserMap();
					map.dmParseMap(p);
					break;

				case WBXML_TAG_Get:
					if (call_start_seq)
					{
						dmHdlCmdSequenceStart(p.userdata, this);
						call_start_seq = false;
					}
					get = new tsDmParserGet();
					get.dmParseGet(p);
					break;

				case WBXML_TAG_Sync:
					if (call_start_seq)
					{
						dmHdlCmdSequenceStart(p.userdata, this);
						call_start_seq = false;
					}
					sync = new tsDmParserSync();
					sync.dmParseSync(p);
					break;
				case WBXML_TAG_Exec:
					if (call_start_seq)
					{
						dmHdlCmdSequenceStart(p.userdata, this);
						call_start_seq = false;
					}
					exec = new tsDmParserExec();
					exec.dmParseExec(p);
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

		if (call_start_seq)
		{
			dmHdlCmdSequenceStart(p.userdata, this);
			call_start_seq = false;
		}

		dmHdlCmdSequenceEnd(p.userdata);

		return DM_ERR_OK;
	}
}
