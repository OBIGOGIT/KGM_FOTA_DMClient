package com.tsdm.adapt;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.tsdm.agent.dmDefineDevInfo;

public class tsDmWbxmlencoder implements tsDefineWbxml
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
		if (!dmWbxEncAppendByte(WBXML_VERSION))
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
			token |= WBXML_TOKEN_WITH_CONTENT;

		return dmWbxEncAppendByte(token);
	}

	public boolean dmWbxEncEndElement()
	{
		return dmWbxEncAppendByte(WBXML_END);
	}

	public boolean dmWbxEncAddSwitchpage(int index)
	{
		if (!dmWbxEncAppendByte(WBXML_SWITCH_PAGE))
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
		if (!dmWbxEncAppendByte(WBXML_STR_I))
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
		if (!dmWbxEncAppendByte(WBXML_OPAQUE))
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
			tsLib.debugPrintException(dmDefineDevInfo.DEBUG_EXCEPTION, e.toString());
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
