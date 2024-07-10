package com.tsdm.adapt;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import com.tsdm.agent.dmDefineDevInfo;

public class tsDmParser extends tsDmWbxmldecoder implements dmDefineDevInfo
{
	public int					version;
	public int					puid;
	public int					charset;
	public String				stringtable;
	public int					stsize;
	public int					codePage;
	public Object				userdata;

	public tsDmParserItem _pItem			= null;
	public tsDmParserMapItem _pMapitem		= null;
	public String				_pTarget		= null;
	public String				_pParserElement	= null;
	public String				Source			= null;
	public tsDmParserMeta Meta			= null;
	public tsDmParserMeta Chal			= null;
	public tsDmParserCred Cred			= null;
	public ByteArrayInputStream	in;

	public tsDmParser(byte[] buf)
	{
		wbxbuff = null;
		wbxbuff = buf;
	}

	public tsDmParser()
	{
		// in = null;
		wbxbuff = null;
	}

	public void dmParseInit(tsDmParser p, Object userdata)
	{
		p.codePage = WBXML_CODEPAGE_SYNCML;
		p.userdata = userdata;
	}

	public int dmParse()
	{
		tsLib.debugPrint(DEBUG_DM, "");
		int result = DM_ERR_OK;

		if (wbxbuff == null)
			return 0;

		wbxindex = 0;
		dmWbxDecInit(wbxbuff, wbxindex);
		dmWbxDecParseStartdoc(this);

		int id = -1;
		try
		{
			id = dmParseCurrentElement();
		}
		catch (IOException e)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
		}

		if (id != WBXML_TAG_SyncML)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, "not WBXML_TAG_SyncML");
			return DM_ERR_UNKNOWN_ELEMENT;
		}

		result = dmparParseSyncml();

		return result;
	}

	public int dmparParseSyncml()
	{
		int id = -1;
		int res;

		res = dmParseCheckElement(WBXML_TAG_SyncML);
		if (res != DM_ERR_OK)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, " not DM_ERR_OK");
			return res;
		}

		res = dmParseZeroBitTagCheck();
		if (res == DM_ERR_ZEROBIT_TAG)
		{
			return DM_ERR_OK;
		}
		else if (res != DM_ERR_OK)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, "not DM_ERR_OK");
			return res;
		}

		while (true)
		{
			try
			{
				id = dmParseCurrentElement();
			}
			catch (IOException e)
			{
				tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
			}

			if (id == WBXML_END)
			{
				break;
			}

			switch (id)
			{
				case WBXML_TAG_SyncHdr:
					tsDmParserSyncheader header = new tsDmParserSyncheader();
					res = header.dmParseSyncheader(this);
					break;

				case WBXML_TAG_SyncBody:
					res = dmParseSyncbody();
					break;

				case WBXML_SWITCH_PAGE:
					id = dmParseReadElement();
					id = dmParseReadElement();
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

		return DM_ERR_OK;
	}

	public int dmParseReadElement()
	{
		int tmp;
		int id = -1;

		try
		{
			tmp = dmWbxDecReadBufferByte();
			id = (tmp & WBXML_TOKEN_MASK) & 0x7f;
		}
		catch (IOException e)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
		}

		return id;
	}

	public int dmParseSyncbody()
	{
		int id = -1;
		int res;
		int tmp = 0;

		res = dmParseCheckElement(WBXML_TAG_SyncBody);
		if (res != DM_ERR_OK)
		{
			return res;
		}

		res = dmParseZeroBitTagCheck();
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
				id = dmParseCurrentElement();
			}
			catch (IOException e)
			{
				tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
			}

			if (id == WBXML_END)
			{
				id = dmParseReadElement();
				break;
			}

			switch (id)
			{
				case WBXML_TAG_Alert:
					tsDmParserAlert alert = new tsDmParserAlert();
					res = alert.smParseAlert(this);
					break;

				case WBXML_TAG_Add:
					tsDmParserAdd add = new tsDmParserAdd();
					res = add.dmParseAdd(this);
					break;

				case WBXML_TAG_Replace:
					tsDmParserReplace replace = new tsDmParserReplace();
					res = replace.dmParseReplace(this);
					break;

				case WBXML_TAG_Get:
					tsDmParserGet get = new tsDmParserGet();
					res = get.dmParseGet(this);
					break;

				case WBXML_TAG_Map:
					tsDmParserMap map = new tsDmParserMap();
					res = map.dmParseMap(this);
					break;

				case WBXML_TAG_Put:
					tsDmParserPut put = new tsDmParserPut();
					res = put.dmParsePut(this);
					break;

				case WBXML_TAG_Results:
					tsDmParserResults results = new tsDmParserResults();
					res = results.dmParseResults(this);
					break;

				case WBXML_TAG_Status:
					tsdmParserStatus status = new tsdmParserStatus();
					res = status.dmParseStatus(this);
					break;

				case WBXML_TAG_Atomic:
					tsDmParserAtomic atomic = new tsDmParserAtomic();
					res = atomic.dmParseAtomic(this);
					break;
				case WBXML_TAG_Sequence:
					tsDmParserSequence sequence = new tsDmParserSequence();
					res = sequence.dmParseSequence(this);
					break;
				case WBXML_TAG_Sync:
					tsDmParserSync sync = new tsDmParserSync();
					res = sync.dmParseSync(this);
					break;

				case WBXML_TAG_Delete:
					tsDmParserDelete delete = new tsDmParserDelete();
					res = delete.dmParseDelete(this);
					break;

				case WBXML_TAG_Copy:
					tsDmParserCopy copy = new tsDmParserCopy();
					res = copy.dmParseCopy(this);
					break;

				case WBXML_TAG_Exec:
					tsDmParserExec exec = new tsDmParserExec();
					res = exec.dmParseExec(this);
					break;

				case WBXML_TAG_Final:
					tmp = dmParseBlankElement(id);
					break;

				case WBXML_SWITCH_PAGE:
					id = dmParseReadElement();
					id = dmParseReadElement();

					codePage = id;
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
		tsLib.debugPrint(DEBUG_DM, " end tmp = " + tmp);
		dmHdlCmdSyncEnd(userdata, tmp);

		return DM_ERR_OK;
	}

	public void dmHdlCmdSyncEnd(Object userdata, int isfinal)
	{
		tsDmWorkspace ws = (tsDmWorkspace) userdata;
		if (isfinal > 0)
		{
			ws.isFinal = true;
		}
		else
		{
			tsLib.debugPrint(DEBUG_DM, "didn't catch FINAL");
			ws.isFinal = false;
		}
	}

	public tsList dmParseItemlist(tsList itemlist)
	{
		int res = DM_ERR_OK;
		tsList h = itemlist, t = null;
		int id = 0;

		while (true)
		{
			try
			{
				id = dmParseCurrentElement();
			}
			catch (IOException e)
			{
				tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
			}

			if (id != WBXML_TAG_Item)
			{
				break;
			}
			_pItem = new tsDmParserItem();
			res = _pItem.dmParseItem(this, _pItem);

			if (res != DM_ERR_OK)
			{
				return null;
			}

			h = tsList.listAppend(h, t, _pItem);
		}

		return h;
	}

	public tsList dmParseMapitemlist(tsList itemlist)
	{
		tsList h = itemlist, t = null;
		int id = -1;
		int res = DM_ERR_OK;

		while (true)
		{
			try
			{
				id = dmParseCurrentElement();
			}
			catch (IOException e)
			{
				tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
			}

			if (id != WBXML_TAG_MapItem)
			{
				break;
			}

			_pMapitem = new tsDmParserMapItem();
			res = _pMapitem.dmParseMapitem(this, _pMapitem);
			if (res != DM_ERR_OK)
			{
				return null;
			}

			// connect item to itemlist
			h = tsList.listAppend(h, t, _pMapitem);
		}

		return h;
	}

	public int dmParseTarget()
	{
		int res = DM_ERR_OK;
		String target = null;
		char[] targetname = null;
		int id = -1;

		res = dmParseCheckElement(WBXML_TAG_Target);

		if (res != DM_ERR_OK)
		{
			return res;
		}

		res = dmParseZeroBitTagCheck();
		if (res == DM_ERR_ZEROBIT_TAG)
		{
			return DM_ERR_OK;
		}
		else if (res != DM_ERR_OK)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, " not DM_ERR_OK");
			return res;
		}

		res = dmParseElement(WBXML_TAG_LocURI);
		if (res != DM_ERR_OK)
		{
			return res;
		}
		target = _pParserElement;

		try
		{
			id = dmParseCurrentElement();

			if (id == WBXML_TAG_LocName)
			{
				dmParseSkipElement(id);
			}
		}
		catch (IOException e)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
		}

		// skip WBXML END - Target
		res = dmParseCheckElement(WBXML_END);
		if (res != DM_ERR_OK)
		{
			return res;
		}

		if (target != null)
		{
			targetname = target.toCharArray();
			_pTarget = String.valueOf(targetname);
		}
		else
			_pTarget = null;

		return res;
	}

	public int dmParseSource()
	{
		String source = null;
		char[] sourcename = null;
		int id = -1;
		int res;

		res = dmParseCheckElement(WBXML_TAG_Source);
		if (res != DM_ERR_OK)
		{
			return res;
		}

		res = dmParseZeroBitTagCheck();
		if (res == DM_ERR_ZEROBIT_TAG)
		{
			return DM_ERR_OK;
		}
		else if (res != DM_ERR_OK)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, " not DM_ERR_OK");
			return res;
		}

		res = dmParseElement(WBXML_TAG_LocURI);
		if (res != DM_ERR_OK)
		{
			return res;
		}

		source = _pParserElement;
		try
		{
			id = dmParseCurrentElement();

			if (id == WBXML_TAG_LocName)
			{
				dmParseSkipElement(id);
			}
		}
		catch (IOException e)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
		}

		res = dmParseCheckElement(WBXML_END);
		if (res != DM_ERR_OK)
		{
			return res;
		}

		if (source != null)
		{
			sourcename = source.toCharArray();
			Source = String.valueOf(sourcename);
		}
		else
		{
			sourcename = null;
			Source = null;
		}

		return res;

	}

	public String dmParseContent()
	{
		String content = null;
		int id;
		int res;

		try
		{
			id = dmWbxDecReadBufferByte();
			if (id == WBXML_STR_I)
			{
				content = dmWbxDecParseStr_i();
			}
			else if (id == WBXML_STR_T)
			{
				content = dmWbxDecParseStr_t();
			}
			else if (id == WBXML_OPAQUE)
			{
				content = dmWbxDecParseExtension(id);
			}
			else
			{
				wbxindex--; // backward buffer
				res = dmParseSkipElement(id);
				// in.reset();
				if (res != DM_ERR_OK)
				{
					return null;
				}
			}
		}
		catch (IOException e)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
		}

		return content;
	}

	public tsList dmParseElelist(int eleid, tsList data)
	{
		int id = -1;
		String item;
		tsList h = null;
		tsList t = null;
		int res;
		while (true)
		{
			try
			{
				id = dmParseCurrentElement();
			}
			catch (IOException e)
			{
				tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
			}

			if (id != eleid)
			{
				break;
			}

			res = dmParseElement(eleid);
			if (res != DM_ERR_OK)
			{
				return null;
			}

			item = _pParserElement;
			data = tsList.listAppend(h, t, item);
		}
		return data;
	}

	public int dmParseChal()
	{
		tsDmParserMeta meta = new tsDmParserMeta();
		int res = DM_ERR_OK;

		res = dmParseCheckElement(WBXML_TAG_Chal);
		if (res != DM_ERR_OK)
		{
			return res;
		}

		res = dmParseZeroBitTagCheck();
		if (res == DM_ERR_ZEROBIT_TAG)
		{
			return DM_ERR_OK;
		}
		else if (res != DM_ERR_OK)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, "not DM_ERR_OK");
			return res;
		}

		res = meta.dmParseMeta(this);
		if (res != DM_ERR_OK)
		{
			return res;
		}
		meta = this.Meta;
		Chal = meta;

		res = dmParseCheckElement(WBXML_END);
		if (res != DM_ERR_OK)
		{
			return res;
		}

		return res;
	}

	public int dmParseElement(int id)
	{
		String data = null, result = new String("");
		int res = DM_ERR_OK;
		boolean do_content = true;
		String content = null;

		_pParserElement = "";
		// read element ID
		res = dmParseCheckElement(id);
		if (res != DM_ERR_OK)
		{
			return res;
		}

		res = dmParseZeroBitTagCheck();
		if (res == DM_ERR_ZEROBIT_TAG)
		{
			return DM_ERR_OK;
		}
		else if (res != DM_ERR_OK)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, "not DM_ERR_OK");
			return res;
		}

		res = dmParseSkipLiteralElement();
		if (res != DM_ERR_OK)
		{
			return res;
		}

		// read content
		while (do_content)
		{
			data = dmParseContent();
			_pParserElement = result.concat(data);
			try
			{
				id = dmWbxDecReadBufferByte();
				if (id == WBXML_STR_T)
				{
					content = dmWbxDecParseStr_t();
					_pParserElement = result.concat(content);
					id = dmWbxDecReadBufferByte();
					if (id == WBXML_END)
					{
						wbxindex--;
						break;
					}
					else
						wbxindex--; // back
				}
				else
				{
					wbxindex--; // back
					break;
				}
			}
			catch (IOException e)
			{
				tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
			}
		}
		// read WBXML END
		res = dmParseCheckElement(WBXML_END);
		if (res != DM_ERR_OK)
		{
			return res;
		}

		return res;
	}

	public int dmParseBlankElement(int id)
	{
		int tmp;
		int res;
		boolean haveend = false;

		// check element have contents
		try
		{
			tmp = dmParseCurrentElement();
			if ((tmp & WBXML_TOKEN_WITH_CONTENT) != 0)
			{
				haveend = true;
			}

			res = dmParseCheckElement(id);
			if (res != DM_ERR_OK)
			{
				return res;
			}

			if (haveend)
			{
				res = dmParseCheckElement(WBXML_END);
				if (res != DM_ERR_OK)
				{
					return res;
				}
			}
		}
		catch (IOException e)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
		}

		return 1;
	}

	public int dmParseCurrentElement() throws IOException
	{
		int cur = 0;
		int tmp;

		tmp = (wbxbuff[wbxindex] & 0xff);
		if (tmp == -1)
			throw new IOException("Unexpected EOF");

		cur = ((tmp & WBXML_TOKEN_MASK) & 0x7f);
		return cur;
	}

	public int dmParseCheckElement(int id)
	{
		int e;
		e = dmParseReadElement();

		if (id != e)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, " is DM_UNKNOWN_ELEMENT");
			return DM_ERR_UNKNOWN_ELEMENT;
		}

		return DM_ERR_OK;
	}

	public int dmParseZeroBitTagCheck()
	{
		int ret = DM_ERR_OK;
		int id = 0;
		int data = (wbxbuff[--wbxindex] & 0xff);
		int zerobit = 0;
		if (data == -1)
			return DM_ERR_FAIL;

		id = (data & WBXML_TOKEN_MASK) & 0x7f;

		if (id >= WBXML_TAG_Add && id <= WBXML_TAG_Correlator)
		{
			zerobit = (data & 0x40);
			if (zerobit == 0)
			{
				ret = DM_ERR_ZEROBIT_TAG;
			}
		}
		wbxindex++;
		return ret;
	}

	public int dmParseSkipElement(int id)
	{
		int tmp;
		int level = 0;

		try
		{
			while (true)
			{
				tmp = dmParseCurrentElement();
				if (tmp == WBXML_SWITCH_PAGE)
				{
					// skip namespace
					dmWbxDecReadBufferByte();
					dmWbxDecReadBufferByte();
				}
				else if (tmp == WBXML_END)
				{
					dmWbxDecReadBufferByte();
					level--;
					if (level == 0)
					{
						break;
					}
				}
				else if ((tmp == WBXML_STR_I) || (tmp == WBXML_STR_T) || (tmp == WBXML_OPAQUE))
				{
					dmParseContent();
				}
				else
				{
					dmWbxDecReadBufferByte();
					level++;
				}
			}

			while (true)
			{
				tmp = dmParseCurrentElement();
				if (tmp != WBXML_SWITCH_PAGE)
				{
					break;
				}

				dmWbxDecReadBufferByte();
				dmWbxDecReadBufferByte();
			}

		}
		catch (IOException e)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
		}
		return DM_ERR_OK;
	}

	public int dmParseSkipLiteralElement()
	{
		int id = -1; // for check LITERAL tags

		// check global LITERAL tags.. and ignore the tag
		try
		{
			id = dmParseCurrentElement();
			if (id == WBXML_LITERAL)
			{
				do
				{
					id = dmWbxDecReadBufferByte();
				} while (id != WBXML_END);
			}
		}
		catch (IOException e)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
		}

		return DM_ERR_OK;
	}
}
