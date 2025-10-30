package com.tsdm.adapt;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.tsdm.agent.dmDefineDevInfo;
import com.tsdm.agent.dmDevInfoAdapter;

public class tsDmEncoder extends tsDmWbxmlencoder implements dmDefineDevInfo
{
	ByteArrayOutputStream	out;

	public tsDmEncoder()
	{
	}

	private int _START_E(int a)
	{
		if (!dmWbxEncStartElement(a, true))
			return DM_ERR_BUFFER_TOO_SMALL;
		return DM_ERR_OK;
	}

	private int _ADD_C(String a)
	{
		if (!dmWbxEncAddContent(a))
			return DM_ERR_BUFFER_TOO_SMALL;
		return DM_ERR_OK;
	}

	private int _END_E()
	{
		if (!dmWbxEncEndElement())
			return DM_ERR_BUFFER_TOO_SMALL;

		return DM_ERR_OK;
	}

	private void _ADD_E(int a, String b)
	{
		_START_E((a));
		_ADD_C((b));
		_END_E();
	}

	public int _ADD_BE(int a)
	{
		if (!dmWbxEncStartElement(a, false))
			return DM_ERR_BUFFER_TOO_SMALL;
		return DM_ERR_OK;
	}

	public void dmEncInit(ByteArrayOutputStream out)
	{
		dmWbxEncInit(out);
	}

	public int dmEncStartSyncml(int pid, int charset, String stringtable, int stsize)
	{
		// wbxenc_init(out);
		if (!dmWbxEncStartDocument(pid, charset, stringtable, stsize))
		{
			return DM_ERR_BUFFER_TOO_SMALL;
		}
		_START_E(WBXML_TAG_SyncML);

		return DM_ERR_OK;
	}

	public int dmEncEndSyncml()
	{
		if (!dmWbxEncEndElement())
			return DM_ERR_BUFFER_TOO_SMALL;

		if (!dmWbxEncEndDocument())
		{
			return DM_ERR_BUFFER_TOO_SMALL;
		}

		return DM_ERR_OK;
	}

	public int dmEncAddMeta(tsDmParserMeta meta)
	{
		int res;

		if (meta == null)
		{
			return DM_ERR_INVALID_PARAMETER;
		}

		_START_E(WBXML_TAG_Meta);
		if (!dmWbxEncAddSwitchpage(WBXML_PAGE_METINF))
		{
			return DM_ERR_BUFFER_TOO_SMALL;
		}

		if (meta.format != null)
		{
			_ADD_E(WBXML_METINF_Format, meta.format);
		}

		if (meta.type != null)
		{
			_ADD_E(WBXML_METINF_Type, meta.type);
		}

		if (meta.mark != null)
		{
			_ADD_E(WBXML_METINF_Mark, meta.mark);
		}

		if (meta.size > 0)
		{
			_ADD_E(WBXML_METINF_Size, String.valueOf(meta.size));
		}

		if (meta.version != null)
		{
			_ADD_E(WBXML_METINF_Version, meta.version);
		}

		if (meta.nextnonce != null)
		{
			_ADD_E(WBXML_METINF_NextNonce, new String(meta.nextnonce));
		}

		if (meta.maxmsgsize > 0)
		{
			_ADD_E(WBXML_METINF_MaxMsgSize, String.valueOf(meta.maxmsgsize));
		}

		if (meta.maxobjsize > 0)
		{
			_ADD_E(WBXML_METINF_MaxObjSize, String.valueOf(meta.maxobjsize));
		}

		if (meta.anchor != null)
		{
			if (meta.anchor.last != null && meta.anchor.next != null)
			{
				if ((res = dmEncAddMetinfAnchor(meta.anchor)) != DM_ERR_OK)
				{
					return res;
				}
			}
		}

		if (meta.emi != null)
		{
			if ((res = dmEncAddMetinfEmi(meta.emi)) != DM_ERR_OK)
			{
				return res;
			}
		}

		if (meta.mem != null)
		{
			if ((res = dmEncAddMetinfMem(meta.mem)) != DM_ERR_OK)
			{
				return res;
			}
		}

		if (!dmWbxEncAddSwitchpage(WBXML_PAGE_SYNCML))
		{
			return DM_ERR_BUFFER_TOO_SMALL;
		}
		_END_E();

		return DM_ERR_OK;
	}

	public int dmEncAddItem(tsDmParserItem item)
	{
		int res;

		if (item == null)
		{
			return DM_ERR_INVALID_PARAMETER;
		}

		_START_E(WBXML_TAG_Item);

		if (item.target != null)
		{
			if ((res = dmEncAddTarget(item.target)) != DM_ERR_OK)
			{
				return res;
			}
		}

		if (item.source != null)
		{
			if ((res = dmEncAddSource(item.source)) != DM_ERR_OK)
			{
				return res;
			}
		}

		if (item.meta != null)
		{
			if ((res = dmEncAddMeta(item.meta)) != DM_ERR_OK)
			{
				return res;
			}
		}

		if (item.data != null)
		{
			if (item.data.type == TYPE_STRING)
			{
				_ADD_E(WBXML_TAG_Data, dmEncPcdataGetString(item.data));
			}
			else if (item.data.type == TYPE_OPAQUE)
			{
				_START_E(WBXML_TAG_Data);
				try
				{

					if (!dmWbxEncAddOpaque(item.data.data, item.data.size))
					{
						return DM_ERR_BUFFER_TOO_SMALL;
					}
				}
				catch (IOException e)
				{
					tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
				}
				_END_E();
			}
			else if (item.data.type == TYPE_EXTENSION)
			{
				_START_E(WBXML_TAG_Data);
				if (!dmWbxEncAddSwitchpage(WBXML_PAGE_METINF))
				{
					return DM_ERR_BUFFER_TOO_SMALL;
				}

				if ((res = dmEncAddMetinfAnchor(item.data.anchor)) != DM_ERR_OK)
				{
					return res;
				}
				if (!dmWbxEncAddSwitchpage(WBXML_PAGE_SYNCML))
				{
					return DM_ERR_BUFFER_TOO_SMALL;
				}
				_END_E();
			}
		}

		_END_E();

		return DM_ERR_OK;
	}

	public int dmEncAddMapItem(tsDmParserMapItem item)
	{
		int res;

		if (item == null)
		{
			return DM_ERR_INVALID_PARAMETER;
		}

		_START_E(WBXML_TAG_MapItem);

		if (item.target != null)
		{
			if ((res = dmEncAddTarget(item.target)) != DM_ERR_OK)
			{
				return res;
			}
		}

		if (item.source != null)
		{
			if ((res = dmEncAddSource(item.source)) != DM_ERR_OK)
			{
				return res;
			}
		}

		_END_E();

		return DM_ERR_OK;
	}

	public int dmEncAddCred(tsDmParserCred cred)
	{
		int res;

		_START_E(WBXML_TAG_Cred);

		if (cred.meta != null)
		{
			if ((res = dmEncAddMeta(cred.meta)) != DM_ERR_OK)
			{
				return res;
			}
		}

		if (cred.data != null)
		{
			_ADD_E(WBXML_TAG_Data, cred.data);
		}

		_END_E();

		return DM_ERR_OK;
	}

	public int dmEncAddSyncHeader(tsDmParserSyncheader sh)
	{
		int res;

		_START_E(WBXML_TAG_SyncHdr);

		if (sh.verdtd != null)
		{
			_ADD_E(WBXML_TAG_VerDTD, sh.verdtd);
		}

		if (sh.verproto != null)
		{
			_ADD_E(WBXML_TAG_VerProto, sh.verproto);
		}

		if (sh.sessionid != null)
		{
			_ADD_E(WBXML_TAG_SessionID, sh.sessionid);
		}

		if (sh.msgid > 0)
		{
			_ADD_E(WBXML_TAG_MsgID, String.valueOf(sh.msgid));
		}

		if (sh.respuri != null)
		{
			_ADD_E(WBXML_TAG_RespURI, sh.respuri);
		}

		if (sh.is_noresp > 0)
		{
			_ADD_BE(WBXML_TAG_NoResp);

		}

		if (sh.target != null)
		{
			if ((res = dmEncAddTarget(sh.target)) != DM_ERR_OK)
			{
				return res;
			}
		}

		if (sh.source != null)
		{
			if (sh.locname == null)
			{
				if ((res = dmEncAddSource(sh.source)) != DM_ERR_OK)
				{
					return res;
				}
			}
			else
			{
				if ((res = dmEncAddSourceWithLocname(sh.source, sh.locname)) != DM_ERR_OK)
				{
					return res;
				}
			}
		}

		if (sh.cred != null)
		{
			if (sh.cred.meta != null || sh.cred.data != null)
			{
				if ((res = dmEncAddCred(sh.cred)) != DM_ERR_OK)
				{
					return res;
				}
			}
		}

		if (sh.meta != null)
		{
			if ((res = dmEncAddMeta(sh.meta)) != DM_ERR_OK)
			{
				return res;
			}
		}
		_END_E(); /* SyncHdr */

		return DM_ERR_OK;
	}

	public int dmEncAddTarget(String target)
	{
		if (target == null)
		{
			return DM_ERR_INVALID_PARAMETER;
		}

		_START_E(WBXML_TAG_Target);
		_ADD_E(WBXML_TAG_LocURI, target);
		_END_E();

		return DM_ERR_OK;
	}

	public int dmEncAddSource(String source)
	{
		if (source == null)
		{
			return DM_ERR_INVALID_PARAMETER;
		}
		_START_E(WBXML_TAG_Source);
		_ADD_E(WBXML_TAG_LocURI, source);
		_END_E();

		return DM_ERR_OK;
	}

	public int dmEncAddSourceWithLocname(String source, String locname)
	{
		if (source == null || locname == null)
		{
			return DM_ERR_INVALID_PARAMETER;
		}
		_START_E(WBXML_TAG_Source);
		_ADD_E(WBXML_TAG_LocURI, source);
		_ADD_E(WBXML_TAG_LocName, locname);
		_END_E();

		return DM_ERR_OK;
	}

	public int dmEncStartSyncbody()
	{
		_START_E(WBXML_TAG_SyncBody);

		return DM_ERR_OK;
	}

	public int dmEncEndSyncbody(boolean is_final)
	{
		if (is_final)
		{
			_ADD_BE(WBXML_TAG_Final);
		}

		_END_E();

		return DM_ERR_OK;
	}
	public int dmEncAddAlert(tsDmParserAlert cmd)
	{
		int res;
		if (cmd == null)
		{
			return DM_ERR_INVALID_PARAMETER;
		}

		if (cmd.cmdid < 0 || cmd.data == null)
		{
			return DM_ERR_INVALID_PARAMETER;
		}

		_START_E(WBXML_TAG_Alert);

		if (cmd.cmdid > 0)
		{
			_ADD_E(WBXML_TAG_CmdID, String.valueOf(cmd.cmdid));
		}

		if (cmd.correlator != null)
		{
			_ADD_E(WBXML_TAG_Correlator, cmd.correlator);
		}

		if (cmd.data != null)
		{
			_ADD_E(WBXML_TAG_Data, cmd.data);
		}

		if (cmd.cred != null)
		{
			if ((res = dmEncAddCred(cmd.cred)) != DM_ERR_OK)
			{
				return res;
			}
		}

		if (cmd.is_noresp > 0)
		{
			_ADD_BE(WBXML_TAG_NoResp);
		}

		if (cmd.itemlist != null)
		{
			if ((res = dmEncAddItemlist(cmd.itemlist)) != DM_ERR_OK)
			{
				return res;
			}
		}
		_END_E();

		return DM_ERR_OK;
	}

	public int dmEncAddElelist(tsList list, int id)
	{
		tsList h = list;
		String item;
		if (list == null)
		{
			return DM_ERR_INVALID_PARAMETER;
		}

		item = (String) tsList.listGetItem(h);
		h = tsList.listGetItemPtr(h);
		while (item != null)
		{
			if (item != null)
			{
				_ADD_E(id, item);
			}

			item = (String) tsList.listGetItem(h);
			h = tsList.listGetItemPtr(h);
		}

		return DM_ERR_OK;
	}

	public int dmEncAddStatus(tsdmParserStatus cmd)
	{
		int res;

		if (cmd == null)
		{
			return DM_ERR_INVALID_PARAMETER;
		}

		tsLib.debugPrint(DEBUG_DM, " cmd.cmdid = " + cmd.cmdid);
		tsLib.debugPrint(DEBUG_DM, " cmd.msgref = " + cmd.msgref);
		tsLib.debugPrint(DEBUG_DM, " cmd.cmd = " + cmd.cmd);
		tsLib.debugPrint(DEBUG_DM, " cmd.data = " + cmd.data);

		if (cmd.cmdid < 0 || cmd.msgref == null || cmd.data == null)
		{
			return DM_ERR_INVALID_PARAMETER;
		}

		_START_E(WBXML_TAG_Status);

		if (cmd.cmdid > 0)
		{
			_ADD_E(WBXML_TAG_CmdID, String.valueOf(cmd.cmdid));
		}
		if (cmd.msgref != null)
		{
			_ADD_E(WBXML_TAG_MsgRef, cmd.msgref);
		}
		if (cmd.cmdref != null)
		{
			_ADD_E(WBXML_TAG_CmdRef, cmd.cmdref);
		}
		if (cmd.cmd != null)
		{
			_ADD_E(WBXML_TAG_Cmd, cmd.cmd);
		}
		if (cmd.targetref != null)
		{
			dmEncAddElelist(cmd.targetref, WBXML_TAG_TargetRef);
		}
		if (cmd.sourceref != null)
		{
			dmEncAddElelist(cmd.sourceref, WBXML_TAG_SourceRef);
		}
		if (cmd.cred != null)
		{
			if ((res = dmEncAddCred(cmd.cred)) != DM_ERR_OK)
			{
				return res;
			}
		}
		if (cmd.chal != null)
		{
			_START_E(WBXML_TAG_Chal);
			if ((res = dmEncAddMeta(cmd.chal)) != DM_ERR_OK)
			{
				return res;
			}
			_END_E();
		}
		if (cmd.data != null)
		{
			_ADD_E(WBXML_TAG_Data, cmd.data);
		}

		if (cmd.itemlist != null)
		{
			if ((res = dmEncAddItemlist(cmd.itemlist)) != DM_ERR_OK)
			{
				return res;
			}
		}
		_END_E();

		return DM_ERR_OK;
	}

	public int dmEncAddReplace(tsDmParserReplace cmd)
	{
		int res;

		if (cmd == null)
		{
			return DM_ERR_INVALID_PARAMETER;
		}
		_START_E(WBXML_TAG_Replace);

		if (cmd.cmdid > 0)
		{
			_ADD_E(WBXML_TAG_CmdID, String.valueOf(cmd.cmdid));
		}
		if (cmd.is_noresp > 0)
		{
			_ADD_BE(WBXML_TAG_NoResp);
		}
		if (cmd.cred != null)
		{
			if ((res = dmEncAddCred(cmd.cred)) != DM_ERR_OK)
			{
				return res;
			}
		}
		if (cmd.meta != null)
		{
			if ((res = dmEncAddMeta(cmd.meta)) != DM_ERR_OK)
			{
				return res;
			}
		}
		if (cmd.itemlist != null)
		{
			if ((res = dmEncAddItemlist(cmd.itemlist)) != DM_ERR_OK)
			{
				return res;
			}
		}
		_END_E();

		return DM_ERR_OK;
	}

	public int dmEncStartReplace(tsDmParserReplace cmd)
	{
		int res;

		if (cmd == null)
		{
			return DM_ERR_INVALID_PARAMETER;
		}

		_START_E(WBXML_TAG_Replace);

		if (cmd.cmdid > 0)
		{
			_ADD_E(WBXML_TAG_CmdID, String.valueOf(cmd.cmdid));
		}

		if (cmd.is_noresp > 0)
		{
			_ADD_BE(WBXML_TAG_NoResp);
		}

		if (cmd.cred != null)
		{
			if ((res = dmEncAddCred(cmd.cred)) != DM_ERR_OK)
			{
				return res;
			}
		}

		if (cmd.meta != null)
		{
			if ((res = dmEncAddMeta(cmd.meta)) != DM_ERR_OK)
			{
				return res;
			}
		}

		return DM_ERR_OK;
	}

	public int dmEncEndReplace()
	{
		_END_E();

		return DM_ERR_OK;
	}
	public int dmEncAddDelete(tsDmParserDelete cmd)
	{
		int res;

		if (cmd == null)
		{
			return DM_ERR_INVALID_PARAMETER;
		}
		_START_E(WBXML_TAG_Delete);

		if (cmd.cmdid > 0)
		{
			_ADD_E(WBXML_TAG_CmdID, String.valueOf(cmd.cmdid));
		}

		if (cmd.is_noresp > 0)
		{
			_ADD_BE(WBXML_TAG_NoResp);
		}
		if (cmd.is_archive > 0)
		{
			_ADD_BE(WBXML_TAG_Archive);
		}
		if (cmd.is_sftdel > 0)
		{
			_ADD_BE(WBXML_TAG_SftDel);
		}
		if (cmd.cred != null)
		{
			if ((res = dmEncAddCred(cmd.cred)) != DM_ERR_OK)
			{
				return res;
			}
		}
		if (cmd.meta != null)
		{
			if ((res = dmEncAddMeta(cmd.meta)) != DM_ERR_OK)
			{
				return res;
			}
		}
		if (cmd.itemlist != null)
		{
			if ((res = dmEncAddItemlist(cmd.itemlist)) != DM_ERR_OK)
			{
				return res;
			}
		}
		_END_E();

		return DM_ERR_OK;
	}

	public int dmEncStartDelete(tsDmParserDelete cmd)
	{
		int res;

		if (cmd == null)
		{
			return DM_ERR_INVALID_PARAMETER;
		}
		_START_E(WBXML_TAG_Delete);

		if (cmd.cmdid > 0)
		{
			_ADD_E(WBXML_TAG_CmdID, String.valueOf(cmd.cmdid));
		}

		if (cmd.is_noresp > 0)
		{
			_ADD_BE(WBXML_TAG_NoResp);
		}
		if (cmd.is_archive > 0)
		{
			_ADD_BE(WBXML_TAG_Archive);
		}
		if (cmd.is_sftdel > 0)
		{
			_ADD_BE(WBXML_TAG_SftDel);
		}
		if (cmd.cred != null)
		{
			if ((res = dmEncAddCred(cmd.cred)) != DM_ERR_OK)
			{
				return res;
			}
		}

		if (cmd.meta != null)
		{
			if ((res = dmEncAddMeta(cmd.meta)) != DM_ERR_OK)
			{
				return res;
			}
		}

		return DM_ERR_OK;
	}

	public int dmEncEndDelete()
	{
		_END_E();
		return DM_ERR_OK;
	}

	public int dmEncAddMap(tsDmParserMap cmd)
	{
		int res;
		tsDmParserMapItem item;
		tsList h = null;

		if (cmd == null)
		{
			return DM_ERR_INVALID_PARAMETER;
		}
		h = cmd.itemlist;

		_START_E(WBXML_TAG_Map);

		if (cmd.cmdid > 0)
		{
			_ADD_E(WBXML_TAG_CmdID, String.valueOf(cmd.cmdid));
		}
		if (cmd.cred != null)
		{
			if ((res = dmEncAddCred(cmd.cred)) != DM_ERR_OK)
			{
				return res;
			}
		}
		if (cmd.meta != null)
		{
			if ((res = dmEncAddMeta(cmd.meta)) != DM_ERR_OK)
			{
				return res;
			}
		}

		if (cmd.target != null)
		{
			if ((res = dmEncAddTarget(cmd.target)) != DM_ERR_OK)
			{
				return res;
			}
		}
		if (cmd.source != null)
		{
			if ((res = dmEncAddSource(cmd.source)) != DM_ERR_OK)
			{
				return res;
			}
		}

		item = (tsDmParserMapItem) tsList.listGetItem(h);
		h = tsList.listGetItemPtr(h);
		while (item != null)
		{
			if (item != null)
			{
				if ((res = dmEncAddMapItem(item)) != DM_ERR_OK)
				{
					_END_E();
					return res;
				}
			}

			item = (tsDmParserMapItem) tsList.listGetItem(h);
			h = tsList.listGetItemPtr(h);
		}

		_END_E();

		return DM_ERR_OK;
	}

	public int dmEncStartMap(tsDmParserMap cmd)
	{
		int res;

		if (cmd == null)
		{
			return DM_ERR_INVALID_PARAMETER;
		}

		_START_E(WBXML_TAG_Map);

		if (cmd.cmdid >= 0)
		{
			_ADD_E(WBXML_TAG_CmdID, String.valueOf(cmd.cmdid));
		}
		if (cmd.cred != null)
		{
			if ((res = dmEncAddCred(cmd.cred)) != DM_ERR_OK)
			{
				return res;
			}
		}
		if (cmd.meta != null)
		{
			if ((res = dmEncAddMeta(cmd.meta)) != DM_ERR_OK)
			{
				return res;
			}
		}
		if (cmd.target != null)
		{
			if ((res = dmEncAddTarget(cmd.target)) != DM_ERR_OK)
			{
				return res;
			}
		}
		if (cmd.source != null)
		{
			if ((res = dmEncAddSource(cmd.source)) != DM_ERR_OK)
			{
				return res;
			}
		}

		return DM_ERR_OK;
	}

	public int dmEncEndMap()
	{
		_END_E();
		return DM_ERR_OK;
	}

	public int dmEncAddGet(tsDmParserGet get)
	{
		int res;
		tsDmParserItem item;
		tsList h = null;

		if (get == null)
		{
			return DM_ERR_INVALID_PARAMETER;
		}

		h = get.itemlist;

		_START_E(WBXML_TAG_Get);

		if (get.cmdid > 0)
		{
			_ADD_E(WBXML_TAG_CmdID, String.valueOf(get.cmdid));
		}

		if (get.cred != null)
		{
			if ((res = dmEncAddCred(get.cred)) != DM_ERR_OK)
			{
				return res;
			}
		}
		if (get.is_noresp > 0)
		{
			_ADD_BE(WBXML_TAG_NoResp);
		}
		if (get.lang > 0)
		{
			_ADD_E(WBXML_TAG_Lang, String.valueOf(get.lang));
		}
		if (get.meta != null)
		{
			if ((res = dmEncAddMeta(get.meta)) != DM_ERR_OK)
			{
				return res;
			}
		}

		item = (tsDmParserItem) tsList.listGetItem(h);
		h = tsList.listGetItemPtr(h);
		while (item != null)
		{
			if (item != null)
			{
				if ((res = dmEncAddItem(item)) != DM_ERR_OK)
				{
					_END_E();
					return res;
				}
			}
			item = (tsDmParserItem) tsList.listGetItem(h);
			h = tsList.listGetItemPtr(h);
		}

		_END_E();

		return DM_ERR_OK;
	}

	public int dmEncAddPut(tsDmParserPut put)
	{
		int res = 0;
		tsDmParserItem item;
		tsList h;

		tsLib.debugPrint(DEBUG_DM, "");

		if (put == null)
		{
			return DM_ERR_INVALID_PARAMETER;
		}

		h = put.itemlist;

		_START_E(WBXML_TAG_Put);

		if (put.cmdid > 0)
		{
			_ADD_E(WBXML_TAG_CmdID, String.valueOf(put.cmdid));
		}

		if (put.cred != null)
		{
			if ((res = dmEncAddCred(put.cred)) != DM_ERR_OK)
			{
				return res;
			}
		}

		if (put.is_noresp > 0)
		{
			_ADD_BE(WBXML_TAG_NoResp);
		}

		if (put.lang > 0)
		{
			_ADD_E(WBXML_TAG_Lang, String.valueOf(put.lang));
		}

		if (put.meta != null)
		{
			if ((res = dmEncAddMeta(put.meta)) != DM_ERR_OK)
			{
				return res;
			}
		}

		item = (tsDmParserItem) tsList.listGetItem(h);
		h = tsList.listGetItemPtr(h);
		while (item != null)
		{
			if (item != null)
			{
				if ((res = dmEncAddItem(item)) != DM_ERR_OK)
				{
					_END_E();
					return res;
				}
			}
			item = (tsDmParserItem) tsList.listGetItem(h);
			h = tsList.listGetItemPtr(h);
		}

		_END_E();

		return DM_ERR_OK;
	}

	public int dmEncAddItemlist(tsList list)
	{
		int res;
		tsList h = list;
		tsDmParserItem item;

		if (list == null)
		{
			return DM_ERR_INVALID_PARAMETER;
		}

		item = (tsDmParserItem) tsList.listGetItem(h);
		h = tsList.listGetItemPtr(h);

		while (item != null)
		{
			if (item != null)
			{
				if ((res = dmEncAddItem(item)) != DM_ERR_OK)
				{
					return res;
				}
			}

			item = (tsDmParserItem) tsList.listGetItem(h);
			h = tsList.listGetItemPtr(h);

		}

		return DM_ERR_OK;
	}

	public int dmEncAddMetinfAnchor(tsDmParserAnchor cmd)
	{
		if (cmd == null)
		{
			return DM_ERR_INVALID_PARAMETER;
		}

		if (cmd.last == null && cmd.next == null)
			return DM_ERR_INVALID_PARAMETER;

		_START_E(WBXML_METINF_Anchor);

		if (cmd.last != null)
		{
			_ADD_E(WBXML_METINF_Last, cmd.last);
		}
		if (cmd.next != null)
		{
			_ADD_E(WBXML_METINF_Next, cmd.next);
		}
		_END_E();

		return DM_ERR_OK;
	}

	public int dmEncAddMetinfMem(tsDmParserMem mem)
	{
		if (mem == null)
		{
			return DM_ERR_INVALID_PARAMETER;
		}

		if (mem.free <= 0 && mem.freeid <= 0 && mem.shared == null)
		{
			return DM_ERR_INVALID_PARAMETER;
		}

		_START_E(WBXML_METINF_Mem);

		if (mem.free >= 0)
		{
			_ADD_E(WBXML_METINF_FreeMem, String.valueOf(mem.free));
		}
		if (mem.freeid >= 0)
		{
			_ADD_E(WBXML_METINF_FreeID, String.valueOf(mem.freeid));
		}
		if (mem.shared != null)
		{
			_ADD_E(WBXML_METINF_SharedMem, mem.shared);
		}

		_END_E();

		return DM_ERR_OK;
	}

	public int dmEncAddMetinfEmi(String emi)
	{
		if (emi == null)
		{
			return DM_ERR_INVALID_PARAMETER;
		}

		_ADD_E(WBXML_METINF_EMI, emi);

		return DM_ERR_OK;
	}

	public static int dmEncGetBufferSize(tsDmEncoder e)
	{
		return dmWbxEncGetBufferSize();
	}

	public int dmEncAddResults(tsDmParserResults results)
	{
		int res;

		if (results == null)
		{
			return DM_ERR_INVALID_PARAMETER;
		}

		_START_E(WBXML_TAG_Results);

		if (results.cmdid > 0)
		{
			_ADD_E(WBXML_TAG_CmdID, String.valueOf(results.cmdid));
		}
		if (results.msgref != null)
		{
			_ADD_E(WBXML_TAG_MsgRef, results.msgref);
		}
		if (results.cmdref != null)
		{
			_ADD_E(WBXML_TAG_CmdRef, results.cmdref);
		}
		if (results.meta != null)
		{
			if ((res = dmEncAddMeta(results.meta)) != DM_ERR_OK)
			{
				_END_E();
				return res;
			}
		}
		if (results.targetref != null)
		{
			_ADD_E(WBXML_TAG_TargetRef, results.targetref);
		}
		if (results.sourceref != null)
		{
			_ADD_E(WBXML_TAG_SourceRef, results.sourceref);
		}
		if (results.itemlist != null)
		{
			if ((res = dmEncAddItemlist(results.itemlist)) != DM_ERR_OK)
			{
				return res;
			}
		}
		_END_E();

		return DM_ERR_OK;
	}

	public int dmEncAddCopy(tsDmParserCopy copy)
	{
		int res;

		if (copy == null)
		{
			return DM_ERR_INVALID_PARAMETER;
		}

		_START_E(WBXML_TAG_Copy);

		if (copy.cmdid > 0)
		{
			_ADD_E(WBXML_TAG_CmdID, String.valueOf(copy.cmdid));
		}

		if (copy.is_noresp > 0)
		{
			_ADD_BE(WBXML_TAG_NoResp);
		}

		if (copy.cred != null)
		{
			if ((res = dmEncAddCred(copy.cred)) != DM_ERR_OK)
			{
				return res;
			}
		}

		if (copy.meta != null)
		{
			if ((res = dmEncAddMeta(copy.meta)) != DM_ERR_OK)
			{
				return res;
			}
		}

		if (copy.itemlist != null)
		{
			if ((res = dmEncAddItemlist(copy.itemlist)) != DM_ERR_OK)
			{
				return res;
			}
		}

		_END_E();

		return DM_ERR_OK;
	}

	public int dmEncAddExec(tsDmParserExec exec)
	{
		int res;

		if (exec == null)
		{
			return DM_ERR_INVALID_PARAMETER;
		}

		_START_E(WBXML_TAG_Copy);

		if (exec.cmdid > 0)
		{
			_ADD_E(WBXML_TAG_CmdID, String.valueOf(exec.cmdid));
		}

		if (exec.correlator != null)
		{
			_ADD_E(WBXML_TAG_Correlator, exec.correlator);
		}

		if (exec.is_noresp > 0)
		{
			_ADD_BE(WBXML_TAG_NoResp);
		}

		if (exec.meta != null)
		{
			if ((res = dmEncAddMeta(exec.meta)) != DM_ERR_OK)
			{
				return res;
			}
		}

		if (exec.itemlist != null)
		{
			if ((res = dmEncAddItemlist(exec.itemlist)) != DM_ERR_OK)
			{
				return res;
			}
		}

		_END_E();

		return DM_ERR_OK;
	}

	public byte[] dmEncDevinf2Opaque(ByteArrayOutputStream out, dmDevInfoAdapter devinf, int[] size)
	{
		if (devinf == null)
		{
			return null;
		}
		dmWbxEncInit(out);

		if (!dmWbxEncStartElement(WBXML_DEVINF_DevInf, true))
		{
			return null;
		}

		byte[] buffer = new byte[out.size()];
		buffer = out.toByteArray();
		size[0] = buffer.length;

		return buffer;
	}

	public String dmEncPcdataGetString(tsDmParserPcdata pcdata)
	{
		String str;
		if (pcdata == null)
		{
			return null;
		}

		if (pcdata.type != TYPE_STRING)
		{
			return null;
		}

		str = String.valueOf(pcdata.data);
		return str;
	}

}
