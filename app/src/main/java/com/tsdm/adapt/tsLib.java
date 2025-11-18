package com.tsdm.adapt;

import android.text.TextUtils;
import android.util.Log;

import com.tsdm.agent.dmCommonEntity;
import com.tsdm.core.data.constants.DmDevInfoConst;
import com.tsdm.db.tsdmDB;
import com.tsdm.tsService;

import java.text.SimpleDateFormat;
import java.util.Date;

public class tsLib
{
	private final static char[]	HEX_DIGITS	= {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

	public static String libStrsplit(char[] str, char delim, char[] out)
	{
		int i = 0;

		if (str == null)
		{
			return null;
		}
		if (str.length == 0)
		{
			return null;
		}

		while (i < str.length)
		{
			if (str[i] == delim)
			{
				out[i] = '\0';
				char[] t = new char[str.length - (i + 1)];
				for (int n = 0; n < str.length - (i + 1); n++)
					t[n] = str[n + i + 1];
				return String.valueOf(t);
			}
			out[i] = str[i];
			i++;
		}
		return null;
	}

	public static String libStrstr(String source, String token)
	{
		int index = 0;

		if (source == null)
			return null;

		index = source.indexOf(token);

		if (index == -1)
			return null;

		return source.substring(index);
	}

	public static String libStrchr(String source, char c)
	{
		int index = 0;

		if (source == null)
			return null;

		index = source.indexOf(c);

		if (index == -1)
			return null;

		return source.substring(index);
	}

	public static void libMemcpy(String dest, String src, int len)
	{
		char buf1[] = new String(dest).toCharArray();
		char buf2[] = new String(src).toCharArray();
		try
		{
			System.arraycopy(buf2, 0, buf1, 0, len);
		}
		catch (Exception e)
		{
			debugPrintException(DmDevInfoConst.DEBUG_EXCEPTION, e.getMessage());
		}
	}

	public static void libMemcpy(byte[] dest, byte[] src, int len)
	{
		char buf1[] = new String(dest).toCharArray();
		char buf2[] = new String(src).toCharArray();
		try
		{
			System.arraycopy(buf2, 0, buf1, 0, len);
		}
		catch (Exception e)
		{
			debugPrintException(DmDevInfoConst.DEBUG_EXCEPTION, e.getMessage());
		}
	}

	public static int libStrncmp(String str1, String str2, int size)
	{
		String pTmp;

		if (size < str1.length())
			pTmp = str1.substring(0, str2.length());
		else
			pTmp = str1;

		if (pTmp.compareTo(str2) == 0)
		{
			return 0;
		}

		return 1;
	}

	public static String libStrrchr(String source, char c)
	{
		int index = 0;

		index = source.lastIndexOf(c);

		return source.substring(0, index);
	}

	public static boolean libIsSpace(char ch)
	{
		// return Character.isSpace(ch);
		return ((ch == ' ') || (ch == '\f') || (ch == '\n') || (ch == '\r') || (ch == '\t') /* || (ch == '\v') */) ? true : false;
	}

	public static char libTolower(char ch)
	{
		return Character.toLowerCase(ch);
	}
	
	public static int libHexToChar(int nHex)
	{
		if (0 <= nHex && nHex <= 9)
			return '0' + nHex;
		else
			if (10 <= nHex && nHex <= 15)
				return 'A' + nHex - 10;

		return '?';
	}

	public static char libToupper(char ch)
	{
		return Character.toUpperCase(ch);
	}

	public static String libString(char[] str)
	{
		int i = 0;
		char[] buf = null;
		if (str.length <= 0)
			return null;
		while (str[i] != '\0' && str.length > i)
		{
			i++;
		}

		buf = new char[i];

		for (int n = 0; n < i; n++)
		{
			buf[n] = str[n];
		}

		return String.valueOf(buf);
	}

	public static boolean libisnum(char c)
	{
		return Character.isDigit(c);
	}

	public static boolean libisalpha(char c)
	{
		return Character.isLetter(c);
	}

	public static boolean libisalnum(char c)
	{
		return Character.isLetterOrDigit(c);
	}

	public static void debugPrint(String title, String content)
	{
		if (DmDevInfoConst._SYNCML_TS_LOG_ON_)
		{
			StackTraceElement[] trace = new Throwable().getStackTrace();
			String msg = "";
			StringBuffer strBuffer = new StringBuffer();

			if (trace.length >= 1)
			{
				StackTraceElement elt = trace[1];
				strBuffer.append("[");
				strBuffer.append(elt.getFileName());
				strBuffer.append(" Line:");
				strBuffer.append(elt.getLineNumber());
				strBuffer.append("] ");
				strBuffer.append(elt.getMethodName());
				strBuffer.append(" ");
				strBuffer.append(content);
				msg = strBuffer.toString();
				Log.i(title, msg);
			}
			else
			{
				Log.i(title, msg);
			}

			if(tsService.logOnOff){

				if (content.toLowerCase().contains("intent")){
					dmCommonEntity.logFileWrite(tsdmDB.DM_FS_FFS_DIRECTORY, tsService.DM_CLIENT_LOG_FILE, "\n".getBytes());
					dmCommonEntity.logFileWrite(tsdmDB.DM_FS_FFS_DIRECTORY, tsService.DM_CLIENT_LOG_FILE, "#############################################################################################################\n".getBytes());
				}

				Long curTime = System.currentTimeMillis();
				Date date = new Date(curTime);
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
				String logTime = format.format(date);
				String log= logTime+" "+title+" "+msg+"\n";
				dmCommonEntity.logFileWrite(tsdmDB.DM_FS_FFS_DIRECTORY, tsService.DM_CLIENT_LOG_FILE, log.getBytes());
			}
		}
	}

	public static void debugPrintException(String title, String content)
	{
		if (DmDevInfoConst._SYNCML_TS_LOG_ON_)
		{
			StringBuffer strBuffer = new StringBuffer();
			StackTraceElement[] trace = new Throwable().getStackTrace();
			String msg = "";

			if (trace.length >= 1)
			{
				StackTraceElement elt = trace[1];

				strBuffer.append("Warning!!! [");
				strBuffer.append(elt.getFileName());
				strBuffer.append(" Line:");
				strBuffer.append(elt.getLineNumber());
				strBuffer.append("] ");
				strBuffer.append(elt.getMethodName());
				strBuffer.append(" ");
				strBuffer.append(content);
				msg = strBuffer.toString();

				Log.e(title, msg);
			}
			else
			{
				Log.e(title, msg);
			}

			if(tsService.logOnOff){

				Long curTime = System.currentTimeMillis();
				Date date = new Date(curTime);
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
				String logTime = format.format(date);
				String log= logTime+" "+title+" "+msg+"\n";
				dmCommonEntity.logFileWrite(tsdmDB.DM_FS_FFS_DIRECTORY, tsService.DM_CLIENT_LOG_FILE, log.getBytes());
			}
		}
	}

	public static byte[] hexStringToBytes(String s)
	{
		byte[] ret;

		if (s == null)
			return null;

		int sz = s.length();

		ret = new byte[sz / 2];

		for (int i = 0; i < sz; i += 2)
		{
			ret[i / 2] = (byte) ((hexCharToInt(s.charAt(i)) << 4) | hexCharToInt(s.charAt(i + 1)));
		}

		return ret;
	}

	static int hexCharToInt(char c)
	{
		if (c >= '0' && c <= '9')
			return (c - '0');
		if (c >= 'A' && c <= 'F')
			return (c - 'A' + 10);
		if (c >= 'a' && c <= 'f')
			return (c - 'a' + 10);

		throw new RuntimeException("invalid hex char '" + c + "'");
	}

	public static String bytesToHexString(byte[] bytes)
	{
		if (bytes == null)
			return null;

		StringBuilder ret = new StringBuilder(2 * bytes.length);

		for (int i = 0; i < bytes.length; i++)
		{
			int b;

			b = 0x0f & (bytes[i] >> 4);

			ret.append("0123456789abcdef".charAt(b));

			b = 0x0f & bytes[i];

			ret.append("0123456789abcdef".charAt(b));
		}

		return ret.toString();
	}

	public static boolean isEmpty(String str)
	{
		if (TextUtils.isEmpty(str))
			return true;
		else
			return false;
	}

	public static String toHexString(byte buf[])
	{
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < buf.length; i++)
		{
			sb.append(Integer.toHexString(0x0100 + (buf[i] & 0x00FF)).substring(1));
		}
		return sb.toString();
	}

	public static String toHexString(byte[] array, int offset, int length)
	{
		char[] buf = new char[length * 2];

		int bufIndex = 0, i = 0;
		String bufStr = null;

		for (i = offset; i < offset + length; i++)
		{
			byte b = array[i];
			buf[bufIndex++] = HEX_DIGITS[(b >>> 4) & 0x0F];
			buf[bufIndex++] = HEX_DIGITS[b & 0x0F];
		}

		// remove '0' at front characters
		for (i = 0; i < buf.length; i++)
		{
			if (buf[i] > '0')
			{
				break;
			}
		}
		if (i == buf.length)
		{
			bufStr = new String("0");
		}
		else
		{
			bufStr = String.valueOf(buf, i, buf.length - i);
		}
		return bufStr;
	}
	
	public static String replaceString(String src, String before, String after)
	{
		if (src == null || before == null || after == null)
		{
			return src;
		}

		StringBuilder sb = new StringBuilder();

		int index = src.indexOf(before);
		int begine = 0;
		while (index != -1)
		{
			sb.append(src.substring(begine, index));
			sb.append(after);
			begine = index + before.length();
			index = src.indexOf(before, begine);
		}

		if (index == -1 && begine != src.length())
		{
			sb.append(src.substring(begine));
		}

		return sb.toString();
	}
}
