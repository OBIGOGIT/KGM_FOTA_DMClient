package com.tsdm.adapt;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.tsdm.agent.dmDefineDevInfo;

public class tsDmWbxmldecoder implements tsDefineWbxml
{
	protected byte[]	wbxbuff		= null;
	public int			wbxindex	= 0;
	public String		stringT		= null;

	public void dmWbxDecInit(byte[] input, int index)
	{
		wbxbuff = input;
		wbxindex = index;
	}

	void dmWbxDecParseStartdoc(tsDmParser parser)
	{
		try
		{
			parser.version = dmWbxDecReadBufferByte();
			parser.puid = dmWbxDecReadBufferMbUINT32();

			if (parser.puid == 0x00)
			{
				dmWbxDecReadBufferMbUINT32();
			}

			parser.charset = dmWbxDecReadBufferMbUINT32();

			parser.stringtable = dmWbxDecParseStringtable();
			stringT = new String(parser.stringtable);

		}
		catch (IOException e)
		{
			tsLib.debugPrintException(dmDefineDevInfo.DEBUG_EXCEPTION, e.toString());
		}
	}

	public String dmWbxDecParseStr_t()
	{
		int len = 0;
		String str;
		int i = 0;
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		try
		{
			len = dmWbxDecReadBufferMbUINT32();
			i = len;
			while (stringT.charAt(i) != 0)
			{
				int j = stringT.charAt(i);
				buf.write(j);
				i++;
			}
		}
		catch (IOException e)
		{
			tsLib.debugPrintException(dmDefineDevInfo.DEBUG_EXCEPTION, e.toString());
		}

		str = new String(buf.toByteArray());
		return str;
	}

	public String dmWbxDecParseStr_i() throws IOException
	{
		ByteArrayOutputStream buf = new ByteArrayOutputStream();

		while (true)
		{
			int i = dmWbxDecReadBufferByte();

			if (i == 0)
			{
				break;
			}

			if (i == -1)
			{
				throw new IOException("Unexpected EOF wbxdec_parse_str_i");
			}

			buf.write(i);
		}

		String result = new String(buf.toByteArray());
		buf.close();
		return result;
	}

	public String dmWbxDecParseExtension(int type)
	{
		String obj = null;
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		int len;
		try
		{
			if (type == WBXML_OPAQUE)
			{
				len = dmWbxDecReadBufferMbUINT32();
				for (int i = 0; i < len; i++)
				{
					int j = dmWbxDecReadBufferByte();
					buf.write(j);
				}
				obj = new String(buf.toByteArray());
			}
		}
		catch (IOException e)
		{
			tsLib.debugPrintException(dmDefineDevInfo.DEBUG_EXCEPTION, e.toString());
		}

		return obj;
	}

	public String dmWbxDecParseStringtable() throws IOException
	{
		String obj = null;
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		int len;

		try
		{
			len = dmWbxDecReadBufferMbUINT32();

			for (int i = 0; i < len; i++)
			{
				int j = dmWbxDecReadBufferByte();
				buf.write(j);
			}

			obj = new String(buf.toByteArray());

		}
		catch (IOException e)
		{
			tsLib.debugPrintException(dmDefineDevInfo.DEBUG_EXCEPTION, e.toString());
		}

		return obj;
	}

	public int dmWbxDecReadBufferMbUINT32() throws IOException
	{
		int result = 0;
		int uint = 0, byte_pos;
		int cur_byte;

		for (byte_pos = 0; byte_pos < 5; byte_pos++)
		{
			if ((cur_byte = dmWbxDecReadBufferByte()) < 0)
			{
				return result;
			}

			uint = (uint << 7) | (cur_byte & 0x7F);

			if ((cur_byte & 0x80) == 0)
			{
				result = uint;
				return result;
			}
		}

		return result;
	}

	public int dmWbxDecReadBufferByte() throws IOException
	{
		int data = (wbxbuff[wbxindex++] & 0xff);
		if (data == -1)
			throw new IOException("Unexpected EOF wbxdec_buffer_read_byte");

		return data;
	}

}