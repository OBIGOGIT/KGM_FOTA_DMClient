package com.tsdm.adapt;

public class tsList
{
	public Object		item;
	public tsList next;

	public static tsList listAppend(tsList header, tsList tail, Object item)
	{
		tsList tmp = null;

		tmp = new tsList();

		tmp.item = item;
		tmp.next = null;

		if (header == null)
		{
			header = tmp;
			tail = header;
		}
		else
		{
			if (tail == null)
			{
				tail = header;
				while (tail.next != null)
					tail = tail.next;
			}

			tail.next = tmp;
			tail = tmp;
		}

		return header;
	}

	public void listDelete(tsList header)
	{
		tsList curr = header;
		@SuppressWarnings("unused")
		tsList tmp = null;

		while (curr != null)
		{
			tmp = curr;
			curr = curr.next;

			tmp = null;
		}

		header = null;
	}

	public tsList listDeleteData(tsList list, Object data)
	{
		tsList entry, prev;

		prev = null;
		entry = list;

		while (entry != null)
		{
			if (entry.item == data)
			{
				if (prev != null)
					prev.next = entry.next;
				else
					list = entry.next;
				entry = null;

				break;
			}
			prev = entry;
			entry = entry.next;
		}
		return list;
	}

	public static Object listGetItem(tsList header)
	{
		Object item = null;

		if (header == null)
		{
			return null;
		}

		item = header.item;
		// header = header.next;

		return item;
	}

	public static tsList listGetItemPtr(tsList header)
	{
		if (header == null)
		{
			return null;
		}

		header = header.next;

		return header;

	}

	public Object listPopItem(tsList header)
	{
		Object item = null;
		@SuppressWarnings("unused")
		tsList tmp;

		if (header == null)
		{
			return null;
		}

		item = header.item;
		tmp = header;
		header = header.next;

		tmp = null;
		return item;
	}

	public static tsDmText listCreateText(int size, Object initText)
	{
		tsDmText text = new tsDmText();

		if (initText != null)
		{
			text.text = String.valueOf(initText);
			text.len = text.text.length();
			text.size = text.len;
		}
		else
		{
			text.text = "";
			text.size = size;
			text.len = 0;
		}
		return text;
	}

	public static tsDmText listAppendStrText(tsDmText target, String AppendText)
	{
		int len = 0;
		len = AppendText.length();

		target = listVerifyTextSize(target, target.len + len);
		target.text += AppendText;

		target.len += len;
		return target;
	}

	public static tsDmText listCopyStrText(tsDmText target, String CopyText)
	{
		int len = 0;
		len = CopyText.length();

		target = listVerifyTextSize(target, target.len + len);
		target.text = CopyText;

		target.len = len;
		return target;
	}

	public static tsDmText listVerifyTextSize(tsDmText text, int size)
	{
		if (text.size < size)
		{
			String old = text.text;
			text.text = "";
			text.size = size;
			text.text = old;

		}
		return text;
	}

	public static tsDmText listAppendText(tsDmText target, tsDmText tail)
	{

		target = listVerifyTextSize(target, target.len + tail.len);

		target.text += tail.text;
		target.len += tail.len;

		return target;
	}

}
