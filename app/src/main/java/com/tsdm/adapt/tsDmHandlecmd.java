package com.tsdm.adapt;

import com.tsdm.agent.dmAgent;
import com.tsdm.agent.dmDefineDevInfo;

public class tsDmHandlecmd implements dmDefineDevInfo
{

	public void dmHdlCmdSyncHdr(Object userdata, tsDmParserSyncheader header)
	{
		tsDmWorkspace ws = (tsDmWorkspace) userdata;
		dmAgent agent;

		agent = new dmAgent();
		agent.header = new tsDmParserSyncheader();
		dmDataStDuplSyncHeader(agent.header, header);

		agent.cmd = "SyncHdr";
		if (ws.inAtomicCmd || ws.inSequenceCmd)
		{
			dmHdlCmdAddSelectedAgent(agent, ws.list);
		}
		else
		{
			ListAddObjAtLast(ws.list, agent);
		}
	}

	public void dmHdlCmdStatus(Object userdata, tsdmParserStatus status)
	{
		tsDmWorkspace ws = (tsDmWorkspace) userdata;
		dmAgent agent;

		agent = new dmAgent();
		agent.status = new tsdmParserStatus();
		dmDataStDuplStatus(agent.status, status);

		agent.cmd = "Status";

		if (ws.inAtomicCmd || ws.inSequenceCmd)
		{
			dmHdlCmdAddSelectedAgent(agent, ws.list);
		}
		else
		{
			ListAddObjAtLast(ws.list, agent);
		}

	}

	public void dmHdlCmdGet(Object userdata, tsDmParserGet get)
	{
		tsDmWorkspace ws = (tsDmWorkspace) userdata;
		dmAgent agent;

		agent = new dmAgent();
		agent.get = new tsDmParserGet();
		dmDataStDuplGet(agent.get, get);

		agent.cmd = "Get";
		if (ws.inAtomicCmd || ws.inSequenceCmd)
		{
			dmHdlCmdAddSelectedAgent(agent, ws.list);
		}
		else
		{
			ListAddObjAtLast(ws.list, agent);
		}
	}

	public void dmHdlCmdExec(Object userdata, tsDmParserExec exec)
	{
		tsDmWorkspace ws = (tsDmWorkspace) userdata;
		dmAgent agent;

		agent = new dmAgent();
		agent.exec = new tsDmParserExec();

		dmDataStDuplExec(agent.exec, exec);

		agent.cmd = "Exec";
		if (ws.inAtomicCmd || ws.inSequenceCmd)
		{
			dmHdlCmdAddSelectedAgent(agent, ws.list);
		}
		else
		{
			ListAddObjAtLast(ws.list, agent);
		}
	}

	public void dmHdlCmdAlert(Object userdata, tsDmParserAlert alert)
	{
		tsDmWorkspace ws = (tsDmWorkspace) userdata;
		dmAgent agent;

		agent = new dmAgent();
		agent.alert = new tsDmParserAlert();
		DMdataStDuplAlert(agent.alert, alert);

		agent.cmd = "Alert";
		if (ws.inAtomicCmd || ws.inSequenceCmd)
		{
			dmHdlCmdAddSelectedAgent(agent, ws.list);
		}
		else
		{
			ListAddObjAtLast(ws.list, agent);
		}
	}

	public void dmHdlCmdAdd(Object userdata, tsDmParserAdd addCmd)
	{
		tsDmWorkspace ws = (tsDmWorkspace) userdata;
		dmAgent agent;

		agent = new dmAgent();
		agent.addCmd = new tsDmParserAdd();
		dmDataStDuplAdd(agent.addCmd, addCmd);

		agent.cmd = "Add";
		if (ws.inAtomicCmd || ws.inSequenceCmd)
		{
			dmHdlCmdAddSelectedAgent(agent, ws.list);
		}
		else
		{
			ListAddObjAtLast(ws.list, agent);
		}
	}

	public void dmHdlCmdReplace(Object userdata, tsDmParserReplace replaceCmd)
	{
		tsDmWorkspace ws = (tsDmWorkspace) userdata;
		dmAgent agent;

		agent = new dmAgent();
		agent.replaceCmd = new tsDmParserReplace();
		dmDataStDuplReplace(agent.replaceCmd, replaceCmd);

		agent.cmd = "Replace";
		if (ws.inAtomicCmd || ws.inSequenceCmd)
		{
			dmHdlCmdAddSelectedAgent(agent, ws.list);
		}
		else
		{
			ListAddObjAtLast(ws.list, agent);
		}
	}

	public void dmHdlCmdCopy(Object userdata, tsDmParserCopy copyCmd)
	{
		tsDmWorkspace ws = (tsDmWorkspace) userdata;
		dmAgent agent;

		agent = new dmAgent();
		agent.copyCmd = new tsDmParserCopy();

		dmDataStDuplCopy(agent.copyCmd, copyCmd);

		agent.cmd = "Copy";
		if (ws.inAtomicCmd || ws.inSequenceCmd)
		{
			dmHdlCmdAddSelectedAgent(agent, ws.list);
		}
		else
		{
			ListAddObjAtLast(ws.list, agent);
		}
	}

	public void dmHdlCmdDelete(Object userdata, tsDmParserDelete deleteCmd)
	{
		tsDmWorkspace ws = (tsDmWorkspace) userdata;
		dmAgent agent;

		agent = new dmAgent();
		agent.deleteCmd = new tsDmParserDelete();
		dmDataStDuplDelete(agent.deleteCmd, deleteCmd);

		agent.cmd = "Delete";
		if (ws.inAtomicCmd || ws.inSequenceCmd)
		{
			dmHdlCmdAddSelectedAgent(agent, ws.list);
		}
		else
		{
			ListAddObjAtLast(ws.list, agent);
		}
	}

	public void dmHdlCmdAtomicStart(Object userdata, tsDmParserAtomic atomic)
	{
		tsDmWorkspace ws = (tsDmWorkspace) userdata;
		dmAgent agent;

		agent = new dmAgent();
		agent.atomic = new tsDmParserAtomic();
		dmDataStDuplAtomic(agent.atomic, atomic);
		ws.atomic = new tsDmParserAtomic();
		dmDataStDuplAtomic(ws.atomic, atomic);

		agent.cmd = "Atomic_Start";
		if (ws.inAtomicCmd || ws.inSequenceCmd)
		{
			dmHdlCmdAddSelectedAgent(agent, ws.list);
		}
		else
		{
			ws.inAtomicCmd = true;
			ListAddObjAtLast(ws.list, agent);
		}
	}

	public void dmHdlCmdAtomicEnd(Object userdata)
	{
		tsDmWorkspace ws = (tsDmWorkspace) userdata;
		dmAgent locateagent = null;
		if (ws.inAtomicCmd || ws.inSequenceCmd)
		{
			dmHdlCmdLocateSelectedAgent(locateagent, ws.list);
			//if (locateagent != null)
			//{
				if (locateagent.inProgresscmd)
				{
					locateagent.inProgresscmd = false;
				}
			//}
			//else
			//{
			//	ws.inAtomicCmd = false;
			//}
		}
	}

	public void dmHdlCmdSequenceStart(Object userdata, tsDmParserSequence sequence)
	{
		tsDmWorkspace ws = (tsDmWorkspace) userdata;
		dmAgent agent;

		agent = new dmAgent();
		agent.sequence = new tsDmParserSequence();
		dmDataStDuplSequence(agent.sequence, sequence);
		if (ws.sequence != null)
		{
			dmDataStDeleteSequence(ws.sequence);
		}
		ws.sequence = new tsDmParserSequence();
		dmDataStDuplSequence(ws.sequence, sequence);

		agent.cmd = "Sequence_Start";

		if (ws.inAtomicCmd || ws.inSequenceCmd)
		{
			dmHdlCmdAddSelectedAgent(agent, ws.list);
		}
		else
		{
			ws.inSequenceCmd = false;
			ListAddObjAtLast(ws.list, agent);
		}
	}

	public void dmHdlCmdAddSelectedAgent(dmAgent agent, tsLinkedList curlocate)
	{
		dmAgent cmdagent = null;

		cmdagent = (dmAgent) tsLinkedList.listGetObj(curlocate, (curlocate.count) - 1);
		if (cmdagent != null)
		{
			if (cmdagent.inProgresscmd)
			{

				if (cmdagent.atomic != null)
				{
					if (cmdagent.atomic.itemlist != null)
					{
						dmHdlCmdAddSelectedAgent(agent, cmdagent.atomic.itemlist);
					}
					else
					{
						cmdagent.atomic.itemlist = tsLinkedList.listCreateLinkedList();
						dmHdlCmdAddSelectedAgent(agent, cmdagent.atomic.itemlist);
					}
				}
				else if (cmdagent.sequence != null)
				{
					if (cmdagent.sequence.itemlist != null)
					{
						dmHdlCmdAddSelectedAgent(agent, cmdagent.sequence.itemlist);
					}
					else
					{
						cmdagent.sequence.itemlist = tsLinkedList.listCreateLinkedList();
						dmHdlCmdAddSelectedAgent(agent, cmdagent.sequence.itemlist);
					}
				}
				else
				{
					ListAddObjAtLast(curlocate, agent);
				}

			}
			else
			{
				if (agent.cmd.compareTo("Atomic_Start") == 0 || agent.cmd.compareTo("Sequence_Start") == 0)
				{
					cmdagent.inProgresscmd = true;
				}
				if (cmdagent.atomic != null)
				{
					curlocate = cmdagent.atomic.itemlist;
				}
				else if (cmdagent.sequence != null)
				{
					curlocate = cmdagent.sequence.itemlist;
				}

				if (curlocate != null)
				{
					ListAddObjAtLast(curlocate, agent);
				}
				else
				{
					curlocate = tsLinkedList.listCreateLinkedList();
					ListAddObjAtLast(curlocate, agent);
				}
			}
		}
		else
		{
			curlocate = tsLinkedList.listCreateLinkedList();
			ListAddObjAtLast(curlocate, agent);
		}
	}

	public void dmHdlCmdLocateSelectedAgent(dmAgent agent, tsLinkedList curlocate)
	{
		dmAgent cmdagent = null;

		cmdagent = (dmAgent) tsLinkedList.listGetObj(curlocate, (curlocate.count) - 1);
		if (cmdagent != null)
		{
			if (cmdagent.inProgresscmd)
			{
				agent = cmdagent;
				if (cmdagent.atomic != null)
				{
					dmHdlCmdLocateSelectedAgent(agent, cmdagent.atomic.itemlist);
				}
				else if (cmdagent.sequence != null)
				{
					dmHdlCmdLocateSelectedAgent(agent, cmdagent.sequence.itemlist);
				}
			}
			else
			{
				if (agent != null)
				{
					agent.inProgresscmd = false;
				}
			}
		}
	}

	public void ListAddObjAtLast(tsLinkedList list, Object obj)
	{
		tsDmNode top = list.top;
		tsDmNode node = new tsDmNode();

		tsLinkedList.listBindObjectToNode(node, obj);

		if (node != null)
		{
			node.next = top;
			node.previous = top.previous;
			top.previous.next = node;
			top.previous = node;

			list.count++;
		}
	}

	public void dmHdlCmdSequenceEnd(Object userdata)
	{
		tsDmWorkspace ws = (tsDmWorkspace) userdata;
		dmAgent locateagent = null;

		if (ws.inSequenceCmd || ws.inAtomicCmd)
		{
			dmHdlCmdLocateSelectedAgent(locateagent, ws.list);
			//if (locateagent != null)
			//{
				if (locateagent.inProgresscmd)
				{
					locateagent.inProgresscmd = false;
				}
			//}
			//else
			//{
			//	ws.inSequenceCmd = false;
			//}
		}
	}

	public void dmDataStDuplSyncHeader(tsDmParserSyncheader dest, tsDmParserSyncheader src)
	{
		if (src.verdtd != null)
		{
			dest.verdtd = src.verdtd;
		}
		if (src.verproto != null)
		{
			dest.verproto = src.verproto;
		}
		if (src.sessionid != null)
		{
			dest.sessionid = src.sessionid;
		}
		if (src.msgid > 0)
		{
			dest.msgid = src.msgid;
		}
		if (src.target != null)
		{
			dest.target = src.target;
		}
		if (src.source != null)
		{
			dest.source = src.source;
		}
		if (src.locname != null)
		{
			dest.locname = src.locname;
		}
		if (src.respuri != null)
		{
			dest.respuri = src.respuri;
		}
		if (src.cred != null)
		{
			dest.cred = new tsDmParserCred();
			dmDataStDuplCred(dest.cred, src.cred);
		}
		if (src.meta != null)
		{
			dest.meta = new tsDmParserMeta();
			dmDataStDuplMeta(dest.meta, src.meta);
		}
	}

	public void dmDataStDuplStatus(tsdmParserStatus dest, tsdmParserStatus src)
	{
		if (src == null)
		{
			return;
		}
		if (src.cmdid > 0)
		{
			dest.cmdid = src.cmdid;
		}
		if (src.msgref != null)
		{
			dest.msgref = src.msgref;
		}
		if (src.cmdref != null)
		{
			dest.cmdref = src.cmdref;
		}
		if (src.cmd != null)
		{
			dest.cmd = src.cmd;
		}
		if (src.targetref != null)
		{
			dest.targetref = src.targetref;
		}
		if (src.sourceref != null)
		{
			dest.sourceref = src.sourceref;
		}
		if (src.cred != null)
		{
			dest.cred = new tsDmParserCred();
			dmDataStDuplCred(dest.cred, src.cred);
		}
		if (src.chal != null)
		{
			dest.chal = new tsDmParserMeta();
			dmDataStDuplMeta(dest.chal, src.chal);
		}
		if (src.data != null)
		{
			dest.data = src.data;
		}
		if (src.itemlist != null)
		{
			dest.itemlist = dmDataStDuplItemlist(src.itemlist);
		}
	}

	public void dmDataStDuplGet(tsDmParserGet dest, tsDmParserGet src)
	{
		if (src == null)
		{
			return;
		}

		if (src.cmdid > 0)
		{
			dest.cmdid = src.cmdid;
		}
		if (src.lang > 0)
		{
			dest.lang = src.lang;
		}
		if (src.cred != null)
		{
			dest.cred = new tsDmParserCred();
			dmDataStDuplCred(dest.cred, src.cred);
		}
		if (src.meta != null)
		{
			dest.meta = new tsDmParserMeta();
			dmDataStDuplMeta(dest.meta, src.meta);
		}
		if (src.itemlist != null)
		{
			dest.itemlist = dmDataStDuplItemlist(src.itemlist);
		}
	}

	public void dmDataStDuplExec(tsDmParserExec dest, tsDmParserExec src)
	{
		if (src == null)
		{
			return;
		}
		if (src.cmdid > 0)
		{
			dest.cmdid = src.cmdid;
		}
		if (src.correlator != null)
		{
			dest.correlator = src.correlator;
		}
		if (src.meta != null)
		{
			dest.meta = new tsDmParserMeta();
			dmDataStDuplMeta(dest.meta, src.meta);
		}
		if (src.itemlist != null)
		{
			dest.itemlist = dmDataStDuplItemlist(src.itemlist);
		}
	}


	public static void DMdataStDuplAlert(tsDmParserAlert dest, tsDmParserAlert src)
	{
		if (src == null)
		{
			return;
		}

		if (src.cmdid > 0)
		{
			dest.cmdid = src.cmdid;
		}
		if (src.correlator != null)
		{
			dest.correlator = src.correlator;
		}
		if (src.cred != null)
		{
			dest.cred = new tsDmParserCred();
			dmDataStDuplCred(dest.cred, src.cred);
		}
		if (src.data != null)
		{
			dest.data = src.data;
		}
		if (src.itemlist != null)
		{
			dest.itemlist = dmDataStDuplItemlist(src.itemlist);
		}
	}

	public void dmDataStDuplAdd(tsDmParserAdd dest, tsDmParserAdd src)
	{
		if (src == null)
		{
			return;
		}

		if (src.cmdid > 0)
		{
			dest.cmdid = src.cmdid;
		}
		if (src.cred != null)
		{
			dest.cred = new tsDmParserCred();
			dmDataStDuplCred(dest.cred, src.cred);
		}
		if (src.meta != null)
		{
			dest.meta = new tsDmParserMeta();
			dmDataStDuplMeta(dest.meta, src.meta);
		}
		if (src.itemlist != null)
		{
			dest.itemlist = dmDataStDuplItemlist(src.itemlist);
		}
	}

	public void dmDataStDuplReplace(tsDmParserReplace dest, tsDmParserReplace src)
	{
		if (src == null)
		{
			return;
		}

		if (src.cmdid > 0)
		{
			dest.cmdid = src.cmdid;
		}
		if (src.cred != null)
		{
			dest.cred = new tsDmParserCred();
			dmDataStDuplCred(dest.cred, src.cred);
		}
		if (src.meta != null)
		{
			dest.meta = new tsDmParserMeta();
			dmDataStDuplMeta(dest.meta, src.meta);
		}
		if (src.itemlist != null)
		{
			dest.itemlist = dmDataStDuplItemlist(src.itemlist);
		}
	}

	public void dmDataStDuplCopy(tsDmParserCopy dest, tsDmParserCopy src)
	{
		if (src == null)
		{
			return;
		}

		if (src.cmdid > 0)
		{
			dest.cmdid = src.cmdid;
		}
		if (src.cred != null)
		{
			dest.cred = new tsDmParserCred();
			dmDataStDuplCred(dest.cred, src.cred);
		}
		if (src.meta != null)
		{
			dest.meta = new tsDmParserMeta();
			dmDataStDuplMeta(dest.meta, src.meta);
		}
		if (src.itemlist != null)
		{
			dest.itemlist = dmDataStDuplItemlist(src.itemlist);
		}
	}

	public void dmDataStDuplDelete(tsDmParserDelete dest, tsDmParserDelete src)
	{
		if (src == null)
		{
			return;
		}

		if (src.cmdid > 0)
		{
			dest.cmdid = src.cmdid;
		}
		if (src.cred != null)
		{
			dest.cred = new tsDmParserCred();
			dmDataStDuplCred(dest.cred, src.cred);
		}
		if (src.meta != null)
		{
			dest.meta = new tsDmParserMeta();
			dmDataStDuplMeta(dest.meta, src.meta);
		}
		if (src.itemlist != null)
		{
			dest.itemlist = dmDataStDuplItemlist(src.itemlist);
		}
	}

	public static void dmDataStDuplCred(tsDmParserCred dest, tsDmParserCred src)
	{
		if (src == null)
		{
			return;
		}
		if (src.data != null)
		{
			dest.data = src.data;
		}
		if (src.meta != null)
		{
			dest.meta = new tsDmParserMeta();
			dmDataStDuplMeta(dest.meta, src.meta);
		}
	}

	public static void dmDataStDuplMeta(tsDmParserMeta dest, tsDmParserMeta src)
	{
		if (src == null)
		{
			return;
		}

		if (src.type != null)
		{
			dest.type = src.type;
		}
		if (src.format != null)
		{
			dest.format = src.format;
		}
		if (src.mark != null)
		{
			dest.mark = src.mark;
		}
		if (src.size > 0)
		{
			dest.size = src.size;
		}
		if (src.nextnonce != null)
		{
			dest.nextnonce = src.nextnonce;
		}
		if (src.version != null)
		{
			dest.version = src.version;
		}
		if (src.maxmsgsize > 0)
		{
			dest.maxmsgsize = src.maxmsgsize;
		}
		if (src.maxobjsize > 0)
		{
			dest.maxobjsize = src.maxobjsize;
		}
		if (src.mem != null)
		{
			dest.mem = new tsDmParserMem();
			dmDataStDuplMetinfMem(dest.mem, src.mem);
		}
		if (src.emi != null)
		{
			dest.emi = src.emi;
		}
		if (src.anchor != null)
		{
			dest.anchor = new tsDmParserAnchor();
			dmDataStDuplMetinfAnchor(dest.anchor, src.anchor);
		}
	}

	public static tsList dmDataStDuplItemlist(tsList src)
	{
		tsList curr = src;
		tsList tmp;
		Object item;
		tsList head = null, tail = null;

		while (curr != null)
		{
			tmp = curr;
			curr = curr.next;

			item = new tsDmParserItem();
			dmDataStDuplItem((tsDmParserItem) item, (tsDmParserItem) tmp.item);
			if (head == null)
				head = tsList.listAppend(head, tail, item);
			else
				tsList.listAppend(head, tail, item);
		}

		return head;
	}

	public static void dmDataStDuplItem(tsDmParserItem dest, tsDmParserItem src)
	{
		if (src == null)
		{
			return;
		}

		if (src.target != null)
		{
			dest.target = src.target;
		}
		if (src.source != null)
		{
			dest.source = src.source;
		}
		if (src.meta != null)
		{
			dest.meta = new tsDmParserMeta();
			dmDataStDuplMeta(dest.meta, src.meta);
		}
		if (src.data != null)
		{
			dest.data = new tsDmParserPcdata();
			dmDataStDuplPcdata(dest.data, src.data);
		}
		dest.moredata = src.moredata;

	}

	public static void dmDataStDuplPcdata(tsDmParserPcdata dest, tsDmParserPcdata src)
	{
		if (src == null)
		{
			return;
		}

		dest.type = src.type;
		if (dest.type == TYPE_STRING)
		{
			dest.data = src.data;
			dest.size = src.size;
		}
		else
		{
			if (src.data != null)
			{
				dest.data = new char[src.size];
				for (int i = 0; i < src.size; i++)
					dest.data[i] = src.data[i];
				dest.size = src.size;
			}
		}
		if (src.anchor != null)
		{
			dest.anchor = new tsDmParserAnchor();
			dmDataStDuplMetinfAnchor(dest.anchor, src.anchor);
		}
	}

	public static void dmDataStDuplMetinfMem(tsDmParserMem dest, tsDmParserMem src)
	{
		if (src == null)
		{
			return;
		}
		if (src.free > 0)
		{
			dest.free = src.free;
		}
		if (src.freeid > 0)
		{
			dest.freeid = src.freeid;
		}
		if (src.shared != null)
		{
			dest.shared = src.shared;
		}
	}

	public static void dmDataStDuplMetinfAnchor(tsDmParserAnchor dest, tsDmParserAnchor src)
	{
		if (src == null)
		{
			return;
		}

		if (src.last != null)
		{
			dest.last = src.last;
		}
		if (src.next != null)
		{
			dest.next = src.next;
		}
	}

	public void dmDataStDuplAtomic(tsDmParserAtomic dest, tsDmParserAtomic src)
	{
		dmAgent cmd = null;
		if (src == null)
		{
			return;
		}

		if (src.cmdid > 0)
		{
			dest.cmdid = src.cmdid;
		}
		if (src.meta != null)
		{
			dest.meta = new tsDmParserMeta();
			dmDataStDuplMeta(dest.meta, src.meta);
		}
		if (src.itemlist != null)
		{
			tsLinkedList.listSetCurrentObj(src.itemlist, 0);
			cmd = (dmAgent) tsLinkedList.listGetNextObj(src.itemlist);
			while (cmd != null)
			{
				ListAddObjAtLast(dest.itemlist, cmd);
				cmd = (dmAgent) tsLinkedList.listGetNextObj(src.itemlist);
			}
		}
		else
		{
			dest.itemlist = tsLinkedList.listCreateLinkedList();
		}
	}


	public void dmDataStDuplSequence(tsDmParserSequence dest, tsDmParserSequence src)
	{
		dmAgent cmd = null;

		if (src == null)
		{
			return;
		}

		if (src.cmdid > 0)
		{
			dest.cmdid = src.cmdid;
		}
		if (src.meta != null)
		{
			dest.meta = new tsDmParserMeta();
			dmDataStDuplMeta(dest.meta, src.meta);
		}
		if (src.itemlist != null)
		{
			tsLinkedList.listSetCurrentObj(src.itemlist, 0);
			cmd = (dmAgent) tsLinkedList.listGetNextObj(src.itemlist);
			while (cmd != null)
			{
				ListAddObjAtLast(dest.itemlist, cmd);
				cmd = (dmAgent) tsLinkedList.listGetNextObj(src.itemlist);
			}
		}
		else
		{
			dest.itemlist = tsLinkedList.listCreateLinkedList();
		}
	}

	public void dmDataStDeleteSequence(Object obj)
	{
		tsDmParserSequence sequence = (tsDmParserSequence) obj;
		dmAgent cmd = null;

		if (sequence == null)
			return;

		sequence.cmdid = 0;
		if (sequence.meta != null)
		{
			sequence.meta.anchor = null;
			sequence.meta.emi = null;
			sequence.meta.format = null;
			sequence.meta.mark = null;
			sequence.meta.maxmsgsize = 0;
			sequence.meta.maxobjsize = 0;
			sequence.meta.mem = null;
			sequence.meta.nextnonce = null;
			sequence.meta.size = 0;
			sequence.meta.type = null;
			sequence.meta.version = null;
			sequence.meta = null;
		}

		if (sequence.itemlist != null)
		{
			tsLinkedList.listSetCurrentObj(sequence.itemlist, 0);
			cmd = (dmAgent) tsLinkedList.listGetNextObj(sequence.itemlist);

			while (cmd != null)
			{
				tsLinkedList.listRemoveObjAtFirst(sequence.itemlist);
				cmd = (dmAgent) tsLinkedList.listGetNextObj(sequence.itemlist);
			}
		}
		sequence = null;
	}

	public static void dmDataStDeleteStatus(Object obj)
	{
		tsdmParserStatus status = (tsdmParserStatus) obj;
		if (status == null)
		{
			return;
		}

		if (status.chal != null)
		{
			dmDataStDeleteMeta(status.chal);
		}
		if (status.itemlist != null)
		{
			dmDataStDeleteItemlist(status.itemlist);
		}
		if (status.cred != null)
		{
			dmDataStDeleteCred(status.cred);
		}
		status.cmd = null;
		status.cmdid = 0;
		status.cmdref = null;
		status.data = null;
		status.msgref = null;

		if (status.sourceref != null)
		{
			dmDataStDeleteElelist(status.sourceref);
		}
		if (status.targetref != null)
		{
			dmDataStDeleteElelist(status.targetref);
		}
		status = null;

	}

	public static void dmDataStDeleteMeta(Object obj)
	{
		tsDmParserMeta meta = (tsDmParserMeta) obj;
		if (meta == null)
		{
			return;
		}
		if (meta.anchor != null)
		{
			dmDataStDeleteMetinfAnchor(meta.anchor);
		}
		if (meta.mem != null)
		{
			dmDataStDeleteMetinfMem(meta.mem);
		}

		meta.emi = null;
		meta.format = null;
		meta.mark = null;
		meta.maxmsgsize = 0;
		meta.maxobjsize = 0;
		meta.nextnonce = null;
		meta.size = 0;
		meta.type = null;
		meta.version = null;
		meta = null;
	}

	public static void dmDataStDeleteMetinfAnchor(Object obj)
	{
		tsDmParserAnchor anchor = (tsDmParserAnchor) obj;
		if (anchor == null)
		{
			return;
		}

		anchor.last = null;
		anchor.next = null;
		anchor = null;
	}

	public static void dmDataStDeleteMetinfMem(Object obj)
	{
		tsDmParserMem mem = (tsDmParserMem) obj;
		if (mem == null)
		{
			return;
		}

		mem.free = 0;
		mem.freeid = 0;
		mem.shared = null;
		mem = null;
	}

	public static void dmDataStDeleteItemlist(Object obj)
	{
		tsList header = (tsList) obj;
		tsList curr = header;
		tsList tmp;

		while (curr != null)
		{
			tmp = curr;
			curr = curr.next;

			dmDataStDeleteItem(tmp.item);
			tmp = null;
		}

		header = null;
	}

	public static void dmDataStDeleteItem(Object obj)
	{
		tsDmParserItem item = (tsDmParserItem) obj;
		if (item == null)
		{
			return;
		}

		if (item.data != null)
		{
			dmDataStDeletePcdata(item.data);
		}

		item.source = null;
		item.target = null;

		if (item.meta != null)
		{
			dmDataStDeleteMeta(item.meta);
		}

		item = null;

	}

	public static void dmDataStDeletePcdata(Object obj)
	{
		tsDmParserPcdata pcdata = (tsDmParserPcdata) obj;
		if (pcdata == null)
		{
			return;
		}

		pcdata.data = null;
		if (pcdata.anchor != null)
		{
			dmDataStDeleteMetinfAnchor(pcdata.anchor);
		}

		pcdata = null;
	}

	public static void dmDataStDeleteCred(Object obj)
	{
		tsDmParserCred cred = (tsDmParserCred) obj;
		if (cred == null)
		{
			return;
		}
		cred.data = null;

		if (cred.meta != null)
		{
			dmDataStDeleteMeta(cred.meta);
		}
		cred = null;
	}

	public static void dmDataStDeleteElelist(Object obj)
	{
		tsList h = (tsList) obj;
		tsList curr, tmp;

		curr = h;
		while (curr != null)
		{
			tmp = curr;
			curr = curr.next;

			tmp.item = null;
			tmp = null;
		}
	}

	public static void dmDataStDeleteAlert(Object obj)
	{
		tsDmParserAlert alert = (tsDmParserAlert) obj;
		if (alert == null)
		{
			return;
		}
		alert.cmdid = 0;

		alert.correlator = null;

		if (alert.cred != null)
		{
			dmDataStDeleteCred(alert.cred);
		}
		alert.data = null;

		if (alert.itemlist != null)
		{
			dmDataStDeleteItemlist(alert.itemlist);
		}
		alert = null;
	}

	public static void dmDataStDeleteReplace(Object obj)
	{
		tsDmParserReplace rep = (tsDmParserReplace) obj;
		if (rep == null)
		{
			return;
		}

		if (rep.cred != null)
		{
			dmDataStDeleteCred(rep.cred);
		}
		if (rep.itemlist != null)
		{
			dmDataStDeleteItemlist(rep.itemlist);
		}
		if (rep.meta != null)
		{
			dmDataStDeleteMeta(rep.meta);
		}
		rep.cmdid = 0;
		rep = null;
	}

	public static tsDmParserPcdata dmDataStString2Pcdata(char[] str)
	{
		tsDmParserPcdata o;
		o = new tsDmParserPcdata();

		o.type = TYPE_STRING;
		o.size = str.length;
		o.data = new char[str.length];
		for (int i = 0; i < str.length; i++)
			o.data[i] = str[i];
		return o;
	}

	public static void dmDataStDeleteResults(Object obj)
	{
		tsDmParserResults results = (tsDmParserResults) obj;
		if (results == null)
		{
			return;
		}

		results.cmdid = 0;
		results.msgref = null;
		results.cmdref = null;

		if (results.meta != null)
		{
			dmDataStDeleteMeta(results.meta);
		}
		results.targetref = null;
		results.sourceref = null;

		if (results.itemlist != null)
		{
			dmDataStDeleteItemlist(results.itemlist);
		}
		results = null;
	}

	public static String dmDataStGetString(tsDmParserPcdata pcdata)
	{
		String string = null;

		if (pcdata == null)
		{
			return null;
		}
		if (pcdata.type != TYPE_STRING)
		{
			return null;
		}

		if (pcdata.data == null)
			return null;

		string = String.valueOf(pcdata.data);

		return string;
	}

	public static void dmDataStDuplResults(tsDmParserResults dest, tsDmParserResults src)
	{
		if (src == null)
		{
			return;
		}

		if (src.cmdid > 0)
		{
			dest.cmdid = src.cmdid;
		}
		if (src.msgref != null)
		{
			dest.msgref = src.msgref;
		}
		if (src.cmdref != null)
		{
			dest.cmdref = src.cmdref;
		}
		if (src.meta != null)
		{
			dest.meta = new tsDmParserMeta();
			dmDataStDuplMeta(dest.meta, src.meta);
		}
		if (src.targetref != null)
		{
			dest.targetref = src.targetref;
		}
		if (src.sourceref != null)
		{
			dest.sourceref = src.sourceref;
		}
		if (src.itemlist != null)
		{
			dest.itemlist = dmDataStDuplItemlist(src.itemlist);
		}
	}
}
