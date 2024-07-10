package com.tsdm.adapt;

import com.tsdm.agent.dmDefineDevInfo;

public class tsLinkedList implements dmDefineDevInfo
{
	public tsDmNode top;
	public tsDmNode cur;
	public int			count;

	public static tsLinkedList listCreateLinkedList()
	{
		tsLinkedList list = null;
		tsDmNode node = null;

		list = new tsLinkedList();

		node = listCreateNodeFromMemory();
		if (node == null)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, "Create node memory alloc failed");
			list = null;
			return null;
		}

		node.next = node;
		node.previous = node;

		list.top = node;
		list.count = 0;

		return list;
	}

	public static void listFreeLinkedList(tsLinkedList list) // Don't need
	{
		tsDmNode top = list.top;
		tsDmNode cur = top.next;
		@SuppressWarnings("unused")
		Object pObj;

		while (cur != top)
		{
			cur = cur.next;
		}

		pObj = listFreeNodeFromMemory(top);
		pObj = null;
		list = null;
	}

	public static void listClearLinkedList(tsLinkedList list)
	{
		tsDmNode top = list.top;
		tsDmNode cur = top.next;
		@SuppressWarnings("unused")
		Object obj;

		while (cur != top)
		{
			cur = cur.next;
			obj = listFreeNodeFromMemory(cur.previous);
			obj = null;
		}

		top.next = top;
		top.previous = top;
		list.top = top;

		list.count = 0;
	}

	public static tsDmNode listCreateNodeFromMemory()
	{
		tsDmNode node = null;
		node = new tsDmNode();

		return node;
	}

	public static void listAddObjAtFirst(tsLinkedList list, Object obj)
	{
		tsDmNode top = list.top;
		tsDmNode node = new tsDmNode();

		listBindObjectToNode(node, obj);

		node.next = top.next;
		node.previous = top;
		top.next = node;
		node.next.previous = node;

		list.count++;
	}
	public static void listAddObjAtLast(tsLinkedList list, Object obj)
	{
		tsDmNode top = list.top;
		tsDmNode node = new tsDmNode();

		listBindObjectToNode(node, obj);

		if (node != null)
		{
			node.next = top;
			node.previous = top.previous;
			top.previous.next = node;
			top.previous = node;

			list.count++;
		}
	}

	public static Object listGetObj(tsLinkedList list, int idx)
	{
		tsDmNode top = list.top;
		tsDmNode cur = top;

		if (idx >= list.count || idx < 0)
		{
			return (tsDmNode) null;
		}

		while (idx-- >= 0)
		{
			cur = cur.next;
		}

		return cur.obj;
	}

	public Object listRemoveObj(tsLinkedList list, Object obj, int size)
	{
		tsDmNode top = list.top;
		tsDmNode cur = top.next;
		Object ret = null;

		while (cur != top)
		{
			if (cur.obj.equals(obj))
			{
				cur.previous.next = cur.next;
				cur.next.previous = cur.previous;
				ret = listFreeNodeFromMemory(cur);
				list.count--;
				break;
			}
			else
			{
				cur = cur.next;
			}
		}

		return ret;
	}

	public static Object listRemoveObjAt(tsLinkedList list, int idx)
	{
		tsDmNode top = list.top;
		tsDmNode cur = top;
		Object obj;

		if (idx >= list.count || idx < 0)
		{
			return null;
		}

		while (idx-- >= 0)
		{
			cur = cur.next;
		}

		cur.previous.next = cur.next;
		cur.next.previous = cur.previous;

		obj = listFreeNodeFromMemory(cur);
		list.count--;

		return obj;
	}

	public static Object listRemoveObjAtFirst(tsLinkedList list)
	{
		return listRemoveObjAt(list, 0);
	}

	public static Object listBindObjectToNode(tsDmNode node, Object obj)
	{
		Object ret = null;

		if (node != null && obj != null)
		{
			ret = node.obj;
			node.obj = obj;
		}

		return ret;
	}

	public static Object listFreeNodeFromMemory(tsDmNode node)
	{
		Object ret = null;
		if (node != null)
		{
			ret = node.obj;
			node = null;
		}
		return ret;
	}

	public static void listSetCurrentObj(tsLinkedList list, int idx)
	{
		tsDmNode cur = list.top;
		if (idx >= 0 && idx < list.count)
		{
			while (idx-- >= 0)
			{
				cur = cur.next;
			}
		}

		list.cur = cur;
	}

	public static Object listGetNextObj(tsLinkedList list)
	{
		tsDmNode cur = list.cur;

		if (cur == list.top)
		{
			return (tsDmNode) null;
		}
		else
		{
			cur = cur.next;
		}
		list.cur = cur;
		return cur.previous.obj;
	}

	public static Object listRemovePreviousObj(tsLinkedList list)
	{
		tsDmNode cur = list.cur;
		if (cur.previous != list.top)
		{
			cur = cur.previous;

			cur.previous.next = cur.next;
			cur.next.previous = cur.previous;

			list.count--;
			return listFreeNodeFromMemory(cur);
		}

		return null;
	}
}
