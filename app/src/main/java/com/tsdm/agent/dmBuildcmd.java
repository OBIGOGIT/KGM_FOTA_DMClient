package com.tsdm.agent;

import com.tsdm.auth.Auth;
import com.tsdm.auth.base64;
import com.tsdm.core.data.constants.DmProtocol;
import com.tsdm.db.tsdmDB;
import com.tsdm.adapt.tsLinkedList;
import com.tsdm.adapt.tsList;
import com.tsdm.adapt.tsDmVnode;
import com.tsdm.adapt.tsDmWorkspace;
import com.tsdm.adapt.tsDmHandlecmd;
import com.tsdm.adapt.tsLib;
import com.tsdm.adapt.tsOmlib;
import com.tsdm.adapt.tsDmParserAlert;
import com.tsdm.adapt.tsDmParserCred;
import com.tsdm.adapt.tsDmParserItem;
import com.tsdm.adapt.tsDmParserMeta;
import com.tsdm.adapt.tsDmParserPcdata;
import com.tsdm.adapt.tsDmParserReplace;
import com.tsdm.adapt.tsDmParserResults;
import com.tsdm.adapt.tsdmParserStatus;
import com.tsdm.adapt.tsDmParserSyncheader;

public class dmBuildcmd extends tsDmHandlecmd implements dmDefineDevInfo
{
	public static int dmBuildCmdGetCmdID(tsDmWorkspace ws)
	{
		int cmdid = 0;
		cmdid = (int) ws.cmdID++;

		return cmdid;
	}

	public static tsDmWorkspace dmBuildCmdSyncHeader(tsDmWorkspace ws)
	{
		tsDmParserSyncheader sh;
		if (ws.syncHeader == null)
		{
			tsDmParserMeta meta;
			ws.syncHeader = new tsDmParserSyncheader();
			sh = ws.syncHeader;

			ws.targetURI = ws.hostname;

			if (_SYNCML_TS_DM_VERSION_V12_)
			{
				sh.verdtd = DM_VERDTD_1_2;
				sh.verproto = DM_VERPROTO_1_2;
			}
			else
			{
				sh.verdtd = DM_VERDTD_1_1;
				sh.verproto = DM_VERPROTO_1_1;
			}
			sh.sessionid = ws.sessionID;
			sh.msgid = (int) ws.msgID;

			sh.target = ws.targetURI;
			sh.source = ws.sourceURI;
			sh.locname = ws.userName;

			meta = new tsDmParserMeta();
			meta.maxmsgsize = (int) ws.maxMsgSize;
			meta.maxobjsize = ws.maxObjSize;

			sh.meta = meta;
		}
		else
		{
			sh = ws.syncHeader;
			sh.msgid = ws.msgID;
			if (ws.targetURI.compareTo(sh.target) != 0)
			{
				sh.target = ws.targetURI;
				dmBuildCmdParseTargetURI(ws);
			}

		}

		if (sh.cred != null)
		{
			sh.cred = null;
		}

		if ((ws.authState != AUTH_STATE_OK && ws.authState != AUTH_STATE_OK_PENDING) || ws.serverAuthState != AUTH_STATE_OK)
		{
			tsDmParserCred cred;
			tsDmParserMeta meta;
			String tmp = null;

			tmp = Auth.authCredType2String(ws.credType);
			if (tmp != null)
			{
				if ((ws.userName.length() == 0) && (ws.clientPW.length() == 0))
				{
					cred = null;
				}
				else if (ws.credType != CRED_TYPE_HMAC)
				{
					meta = new tsDmParserMeta();

					meta.type = Auth.authCredType2String(ws.credType);// String.valueOf(ws.credType);
					meta.format = "b64";
					cred = null;
					cred = new tsDmParserCred();
					cred.meta = meta;

					cred.data = Auth.authMakeDigest(ws.credType, ws.userName, ws.clientPW, ws.nextNonce, ws.nextNonce.length, null, 0, ws.serverID);
					if (cred.data != null)
					{
						tsLib.debugPrint(DEBUG_DM, "cred data = " + cred.data + "credType = " + ws.credType);
					}
				}
				else
				{
					cred = null;
				}
				sh.cred = cred;
			}
			else
			{
				sh.cred = null;
			}
		}

		return ws;
	}

	public static tsDmParserAlert dmBuildCmdAlert(tsDmWorkspace ws, String data)
	{
		tsDmParserAlert alertCmd;
		alertCmd = new tsDmParserAlert();
		alertCmd.cmdid = dmBuildCmdGetCmdID(ws);
		alertCmd.data = data;

		return alertCmd;
	}
	public static tsDmParserAlert dmBuildCmdGenericAlert(tsDmWorkspace ws, String data)
	{
		tsDmParserAlert alertCmd;
		tsDmParserItem item = null;
		tsList head = null, tail = null;
		String pData;

		alertCmd = new tsDmParserAlert();
		alertCmd.cmdid = dmBuildCmdGetCmdID(ws);
		alertCmd.data = data;

		item = new tsDmParserItem();
		item.meta = new tsDmParserMeta();
		item.data = new tsDmParserPcdata();

		tsLib.debugPrint(DEBUG_DM, "Client init alert");
		item.source = tsdmDB.dmdbGetFUMOUpdateReportURI();

		if (dlAgent.dlAgentGetClientInitFlag() == DM_USER_INIT)
		{
			item.meta.format = "chr";
			item.meta.type = DM_USER_INIT_ALERT_TYPE;
		}
		else if(dlAgent.dlAgentGetClientInitFlag() == DM_DEVICE_INIT)
		{
			item.meta.format = "chr";
			item.meta.type = DM_DEV_INIT_ALERT_TYPE;
		}
		else
		{
			tsLib.debugPrint(DEBUG_DM, "Init no flag");
		}
		pData = "0";
		item.data.data = pData.toCharArray();
		item.data.type = TYPE_STRING;

		head = tsList.listAppend(head, tail, item);
		alertCmd.itemlist = head;

		return alertCmd;
	}

	public static tsDmParserAlert dmBuildCmdGenericAlertReport(tsDmWorkspace ws, String data)
	{
		tsDmParserAlert Alert;
		tsDmParserItem Item = null;
		tsList head = null, tail = null;
		String szResult = null;
		String szCorrelator = null;
		int nAgentType = SYNCML_DM_AGENT_DM;

		Alert = new tsDmParserAlert();
		Alert.cmdid = dmBuildCmdGetCmdID(ws);
		Alert.data = data;

		szCorrelator = tsdmDB.dmdbGetFUMOCorrelator();
		if (szCorrelator != null)
		{
			if (szCorrelator.length() != 0)
			{
				Alert.correlator = szCorrelator;
			}
		}

		Item = new tsDmParserItem();
		Item.data = new tsDmParserPcdata();
		Item.data.type = TYPE_STRING;

		nAgentType = tsdmDB.dmdbGetDmAgentType();

		if (nAgentType == SYNCML_DM_AGENT_FUMO)
		{
			Item.source = tsdmDB.dmdbGetFUMOUpdateReportURI();
			Item.meta = new tsDmParserMeta();

			// Defects
			if (Item.source == null)
			{
				tsLib.debugPrintException(DEBUG_EXCEPTION, "Item.source is null");
			}
			else if (Item.source.contains(DM_OMA_EXEC_REPLACE))
			{
				Item.meta.type = DM_UPDATE_REPORT_ALERT_TYPE_UPDATE;
			}
			else if (Item.source.contains(DM_OMA_EXEC_ALTERNATIVE))
			{
				Item.meta.type = DM_UPDATE_REPORT_ALERT_TYPE_DOWNLOAD_AND_UPDATE;
			}
			else if (Item.source.contains(DM_OMA_EXEC_ALTERNATIVE_2))
			{
				Item.meta.type = DM_UPDATE_REPORT_ALERT_TYPE_DOWNLOAD;
			}

			szResult = tsdmDB.dmdbGetFUMOResultCode();
			if (szResult == null)
			{
				szResult = "0";
				Item.data.data = szResult.toCharArray();
			}
			else
			{
				Item.data.data = szResult.toCharArray();
			}
		}
		else
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, "nAgentType : " + nAgentType);
		}

		head = tsList.listAppend(head, tail, Item);
		Alert.itemlist = head;

		return Alert;
	}

	public static tsDmParserReplace dmBuildCmdReplace(tsDmWorkspace ws, tsLinkedList list)
	{
		tsDmParserReplace replaceCmd = null;
		tsDmParserItem obj, obj2;
		tsList head = null, tail = null;

		replaceCmd = new tsDmParserReplace();
		replaceCmd.cmdid = dmBuildCmdGetCmdID(ws);

		tsLinkedList.listSetCurrentObj(list, 0);
		obj = (tsDmParserItem) tsLinkedList.listGetNextObj(list);
		while (obj != null)
		{
			obj2 = new tsDmParserItem();
			dmDataStDuplItem(obj2, obj);
			if (head == null)
				head = tsList.listAppend(head, tail, obj2);
			else
				tsList.listAppend(head, tail, obj2);
			obj = (tsDmParserItem) tsLinkedList.listGetNextObj(list);
		}
		replaceCmd.itemlist = head;

		return replaceCmd;
	}

	public static tsdmParserStatus dmBuildCmdStatus(tsDmWorkspace ws, int cmdRef, String cmd, String source, String target, String data)
	{
		tsdmParserStatus status = null;
		tsDmParserItem obj, obj2;
		tsList head, tail;
		String buf;
		tsDmParserMeta chal;
		String ret;
		status = new tsdmParserStatus();

		status.cmdid = dmBuildcmd.dmBuildCmdGetCmdID(ws);
		status.msgref = ws.msgRef;
		status.cmdref = String.valueOf(cmdRef);
		status.cmd = cmd;
		status.data = data;

		head = null;
		tail = null;

		if (target != null)
		{
			buf = target;
			head = tsList.listAppend(head, tail, buf);
			status.targetref = head;
		}
		else if (ws.targetRefList != null)
		{
			tsLinkedList.listSetCurrentObj(ws.targetRefList, 0);
			obj = (tsDmParserItem) tsLinkedList.listGetNextObj(ws.targetRefList);
			while (obj != null)
			{
				obj2 = new tsDmParserItem();
				dmDataStDuplItem(obj2, obj);
				if (head == null)
					head = tsList.listAppend(head, tail, obj2);
				else
					tsList.listAppend(head, tail, obj2);
				obj = (tsDmParserItem) tsLinkedList.listGetNextObj(ws.targetRefList);
			}
			status.targetref = head;
		}

		head = null;
		tail = null;
		if (source != null)
		{
			buf = source;
			head = tsList.listAppend(head, tail, buf);
			status.sourceref = head;
		}
		else if (ws.sourceRefList != null)
		{
			tsLinkedList.listSetCurrentObj(ws.sourceRefList, 0);
			obj = (tsDmParserItem) tsLinkedList.listGetNextObj(ws.sourceRefList);
			while (obj != null)
			{
				if (head == null)
					head = tsList.listAppend(head, tail, obj);
				else
					tsList.listAppend(head, tail, obj);
				obj = (tsDmParserItem) tsLinkedList.listGetNextObj(ws.sourceRefList);
			}
			status.sourceref = head;
		}
		if (((data.compareTo(DmProtocol.STATUS_AUTHENTICATION_REQUIRED) == 0) || (data.compareTo(DmProtocol.STATUS_UNAUTHORIZED) == 0)) && (cmdRef == 0))
		{
			chal = new tsDmParserMeta();
			chal.format = "b64";
			if (ws.serverCredType == CRED_TYPE_BASIC)
			{
				chal.type = CRED_TYPE_STRING_BASIC;
				tsLib.debugPrint(DEBUG_DM, "CRED_TYPE_BASIC");
			}
			else if (ws.serverCredType == CRED_TYPE_MD5)
			{
				chal.type = CRED_TYPE_STRING_MD5;
				byte[] encoder = base64.encode(ws.serverNextNonce);
				chal.nextnonce = null;
				chal.nextnonce = new String(encoder);
				ret = new String(encoder);
				tsLib.debugPrint(DEBUG_DM, "CRED_TYPE_STRING_MD5 " + "WS.serverNextNonce: " + new String(ws.serverNextNonce) + "Encoded server nonce " + chal.nextnonce);

			}
			else if (ws.serverCredType == CRED_TYPE_HMAC)
			{
				chal.type = CRED_TYPE_STRING_HMAC;
				byte[] encoder = base64.encode(ws.serverNextNonce);
				chal.nextnonce = null;
				chal.nextnonce = new String(encoder);
				ret = new String(encoder);
			}
			status.chal = chal;
		}

		if (!ws.sendChal)
		{
			if ((data.compareTo("212") == 0) && (cmdRef == 0))
			{
				if (ws.serverCredType == CRED_TYPE_MD5)
				{
					if (status.chal != null)
					{
						dmDataStDeleteMeta(status.chal);
						status.chal = null;
					}

					chal = new tsDmParserMeta();
					chal.format = "b64";
					chal.type = CRED_TYPE_STRING_MD5;
					byte[] encoder = base64.encode(ws.serverNextNonce);
					chal.nextnonce = null;
					chal.nextnonce = new String(encoder);
					ret = new String(encoder);

					tsdmDB.dmdbSetServerNonce(ret);
					tsLib.debugPrint(DEBUG_DM, "CRED_TYPE_STRING_MD5 " + "WS.serverNextNonce: " + new String(ws.serverNextNonce) + "Encoded server nonce " + chal.nextnonce);
					ws.sendChal = true;
					status.chal = chal;
				}
			}
		}

		if ((ws.serverCredType == CRED_TYPE_HMAC) && (data.compareTo("200") == 0) && (cmdRef == 0))
		{
			chal = new tsDmParserMeta();
			chal.format = "b64";
			chal.type = CRED_TYPE_STRING_HMAC;

			byte[] encoder = base64.encode(ws.serverNextNonce);
			chal.nextnonce = null;
			chal.nextnonce = new String(encoder);
			ret = new String(encoder);

			status.chal = chal;
		}
		return status;
	}

	public static tsDmParserResults dmBuildCmdDetailResults(tsDmWorkspace ws, int cmdRef, String source, String format, String type, int size, char[] data)
	{
		tsDmParserResults results = null;
		tsDmParserItem item = null;
		tsDmParserMeta meta = null;
		tsList h = null, t = null;
		tsDmVnode node = null;
		char[] nodename = new char[128];

		// Defects
		if (ws == null || source == null)
		{
			tsLib.debugPrint(DEBUG_DM, "ws or source is null");
			return null;
		}

		tsLib.libStrsplit(source.toCharArray(), '?', nodename);
		String nodename1 = tsLib.libString(nodename);
		node = tsOmlib.dmOmLibGetNodeProp(ws.om, nodename1);
		if (node == null)
		{
			tsLib.debugPrint(DEBUG_DM, "Result node is null");
			return null; // Defects
		}
		/* End 1.2 ISSUE */

		results = new tsDmParserResults();
		item = new tsDmParserItem();
		results.cmdid = dmBuildCmdGetCmdID(ws);
		results.msgref = ws.msgRef;
		results.cmdref = String.valueOf(cmdRef);

		// set item->source
		if (source != null)
		{
			item.source = source;
		}

		// set item->meta
		if (format != null || type != null || size > 0)
		{
			meta = new tsDmParserMeta();
			if (type != null)
				meta.type = type;
			if (format != null)
				meta.format = format;
			if (size > 0)
				meta.size = size;
			item.meta = meta;
		}

		// set item->data
		if (data != null)
		{
			if (node.format == FORMAT_BIN)
			{
				item.data = new tsDmParserPcdata();
				item.data.type = TYPE_OPAQUE;
				item.data.data = new char[size];
				for (int i = 0; i < size; i++)
					item.data.data[i] = data[i];

				item.data.size = size;
			}
			else
			{
				if (format != null && format.compareTo("bin") == 0) /* tnds converter */
				{
					item.data = new tsDmParserPcdata();
					item.data.data = new char[size];

					for (int i = 0; i < size; i++)
						item.data.data[i] = data[i];

					item.data.size = size;
				}
				else
				{
					item.data = dmDataStString2Pcdata(data);
				}
			}
		}
		else
		{
			item.data = null;
		}
		h = tsList.listAppend(h, t, item);

		results.itemlist = h;

		return results;
	}

	public static void dmBuildCmdParseTargetURI(tsDmWorkspace ws)
	{
		String target = ws.targetURI;

		if (target.substring(0, 5).compareTo("https") == 0)
		{
			String subStr = target.substring(8);
			tsLib.debugPrint(DEBUG_DM, "target.substring " + subStr);
			int firstComma = subStr.indexOf(':');
			int firstQuestion = subStr.indexOf('?');
			if (firstQuestion > 0)
				ws.hostname = "https" + "://" + subStr.substring(0, firstQuestion);
			else
				ws.hostname = "https" + "://" + subStr;
			tsLib.debugPrint(DEBUG_DM, "ws.hostname => " + ws.hostname);
			int firstSlash = subStr.indexOf('/');
			String portStr = subStr.substring(firstComma + 1, firstSlash);
			// MSC : port not exist , default port is 80
			try
			{
				ws.port = Integer.parseInt(portStr);
			}
			catch (NumberFormatException e)
			{
				ws.port = 80;
			}
			tsLib.debugPrint(DEBUG_DM, "ws.port =>" + ws.port);
		}
		else
		{
			String subStr = target.substring(7);
			tsLib.debugPrint(DEBUG_DM, "target.substring " + subStr);
			int firstComma = subStr.indexOf(':');
			int firstQuestion = subStr.indexOf('?');
			String hostname = subStr.substring(0, firstQuestion);
			ws.hostname = "http" + "://" + hostname;
			tsLib.debugPrint(DEBUG_DM, "ws.hostname => " + ws.hostname);
			int firstSlash = subStr.indexOf('/');
			String portStr = subStr.substring(firstComma + 1, firstSlash);

			// MSC : port not exist , default port is 80
			try
			{
				ws.port = Integer.parseInt(portStr);
			}
			catch (NumberFormatException e)
			{
				ws.port = 80;
			}

			tsLib.debugPrint(DEBUG_DM, "ws.port => " + ws.port);
		}
	}

}
