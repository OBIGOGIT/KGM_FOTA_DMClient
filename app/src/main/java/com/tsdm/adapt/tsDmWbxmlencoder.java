package com.tsdm.adapt;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.tsdm.core.data.constants.DmDevInfoConst;
import com.tsdm.core.data.constants.WbxmlProtocolConst;

public class tsDmWbxmlencoder
{
	private static ByteArrayOutputStream	out				= null;

	ByteArrayOutputStream					buf				= new ByteArrayOutputStream();
	ByteArrayOutputStream					stringTableBuf	= new ByteArrayOutputStream();

	/* Encoder Function Prototypes */
	public void dmWbxEncInit(ByteArrayOutputStream pout)
	{
		out = pout;
	}

	public boolean dmWbxEncStartDocument(int pid, int charset, String stringtable, int stsize)
	{
		if (!dmWbxEncAppendByte(WbxmlProtocolConst.WBXML_VERSION))
			return false;
		if (!dmWbxEncAppendMbUINT32(pid))
			return false;
		if (pid == 0)
		{
			if (!dmWbxEncAppendMbUINT32(0))
				return false;
		}
		if (!dmWbxEncAppendMbUINT32(charset))
			return false;
		if (!dmWbxEncAppendMbUINT32(stsize))
			return false;
		if (!dmWbxEncAppendToBuffer(stringtable))
			return false;

		return true;
	}

	public boolean dmWbxEncEndDocument()
	{
		return true;
	}

	public boolean dmWbxEncStartElement(int index, boolean content)
	{
		int token = index;

		if (content)
			token |= WbxmlProtocolConst.WBXML_TOKEN_WITH_CONTENT;

		return dmWbxEncAppendByte(token);
	}

	public boolean dmWbxEncEndElement()
	{
		return dmWbxEncAppendByte(WbxmlProtocolConst.WBXML_END);
	}

	public boolean dmWbxEncAddSwitchpage(int index)
	{
		if (!dmWbxEncAppendByte(WbxmlProtocolConst.WBXML_SWITCH_PAGE))
		{
			return false;
		}
		if (!dmWbxEncAppendByte(index))
		{
			return false;
		}

		return true;
	}

	public boolean dmWbxEncAddContent(String str)
	{
		if (!dmWbxEncAppendByte(WbxmlProtocolConst.WBXML_STR_I))
		{
			return false;
		}

		if (!dmWbxEncAppendToBuffer(str))
			return false;

		out.write(0);
		return true;
	}

	public boolean dmWbxEncAddOpaque(char[] buf, int size) throws IOException
	{
		if (!dmWbxEncAppendByte(WbxmlProtocolConst.WBXML_OPAQUE))
		{
			return false;
		}

		if (!dmWbxEncAppendMbUINT32(size))
		{
			return false;
		}

		for (int i = 0; i < size; i++)
		{
			out.write(buf[i]);
		}

		return true;
	}

	public static int dmWbxEncGetBufferSize()
	{
		int r = out.size();
		return r;
	}

	public boolean dmWbxEncAppendToBuffer(String s)
	{

		try
		{
			out.write(s.getBytes());
		}
		catch (IOException e)
		{
			tsLib.debugPrintException(DmDevInfoConst.DEBUG_EXCEPTION, e.toString());
		}
		return true;
	}

	public boolean dmWbxEncAppendByte(int value)
	{
		out.write(value);
		return true;
	}

	public boolean dmWbxEncAppendMbUINT32(int value)
	{
		byte[] buf = new byte[5];
		int idx = 0;

		do
		{
			buf[idx++] = (byte) (value & 0x7f);
			value = value >> 7;
		} while (value != 0);

		while (idx > 1)
		{
			out.write(buf[--idx] | 0x80);
		}

		out.write(buf[0]);
		return true;
	}
}
