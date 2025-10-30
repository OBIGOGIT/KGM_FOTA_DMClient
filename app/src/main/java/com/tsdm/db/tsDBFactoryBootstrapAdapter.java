package com.tsdm.db;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import com.tsdm.agent.dmDefineDevInfo;
import com.tsdm.agent.dmDevInfoAdapter;
import com.tsdm.adapt.tsLib;

public class tsDBFactoryBootstrapAdapter implements dmDefineDevInfo
{
	private static int		default_passwordLen	= 16;

	private static byte[]	clientPasswordDict	= new byte[] { 0x0e, 0x06, 0x10, 0x0c, 0x0a,
															0x0e, 0x05, 0x0c, 0x12, 0x0a,
															0x0b, 0x06, 0x0d, 0x0e, 0x05 };

	private static byte[]	serverPasswordDict	= new byte[] { 0x0a, 0x06, 0x0e, 0x0e, 0x0a, 
															0x0b, 0x06, 0x0e, 0x0b, 0x04, 
															0x04, 0x07, 0x11, 0x0c, 0x0c };

	private static byte[][]	g_szPasswdDict		= new byte[][]
	{ 
		{0x0e, 0x06, 0x10, 0x0c, 0x0a, 0x0e, 0x05, 0x0c, 0x12, 0x0a, 0x0b, 0x06, 0x0d, 0x0e, 0x05},
		{0x0a, 0x06, 0x0e, 0x0e, 0x0a, 0x0b, 0x06, 0x0e, 0x0b, 0x04, 0x04, 0x07, 0x11, 0x0c, 0x0c},
		{0x0a, 0x06, 0x10, 0x0e, 0x0a, 0x0b, 0x05, 0x0e, 0x12, 0x04, 0x0b, 0x07, 0x0d, 0x0c, 0x05}
	};

	private static byte[]	szDict				= new byte[]
	{
		0x01, 0x0f, 0x05, 0x0b, 0x13, 0x1c, 0x17, 0x2f, 0x23, 0x2c, 
		0x02, 0x0e, 0x06, 0x0a, 0x12, 0x0d, 0x16, 0x1a, 0x20, 0x2f,
		0x03, 0x0d, 0x07, 0x09, 0x11, 0x1e, 0x15, 0x19, 0x21, 0x2d, 
		0x04, 0x0c, 0x08, 0x3f, 0x10, 0x1f, 0x14, 0x18, 0x22, 0x2e,
   };

	private static String generateClientPasswordKey(String deviceId)
	{
		return generateKeyFromDict(deviceId, clientPasswordDict);
	}

	private static String generateServerPasswordKey(String deviceId)
	{
		return generateKeyFromDict(deviceId, serverPasswordDict);
	}

	private static String generateKeyFromDict(String deviceId, byte[] dict)
	{
		int i = 0;
		int length = deviceId.length();
		long serial1 = 0, serial2 = 0;

		for (i = 0; i < length - 3; i++)
		{
			serial1 += (byte) deviceId.charAt(i + 3) * dict[i];
			serial2 += (byte) deviceId.charAt(i + 3) * (byte) deviceId.charAt(i + 2) * dict[i];
		}

		String temp = String.valueOf(serial1);
		temp = temp.concat("-");
		temp = temp.concat(String.valueOf(serial2));
		return temp;
	}

	public static String generateClientPassword(String deviceId, int passwordLen)
	{
		String imei = dmDevInfoAdapter.devAdpGetDeviceId();
		String key = generateClientPasswordKey(imei);
		return generatePassword(deviceId, key, passwordLen);
	}

	public static String generateServerPassword(String deviceId, int passwordLen)
	{
		String imei = dmDevInfoAdapter.devAdpGetDeviceId();
		String key = generateServerPasswordKey(imei);
		return generatePassword(deviceId, key, passwordLen);
	}

	public tsDBFactoryBootstrapAdapter()
	{
	}

	public static String generatePswdByID(int nTypePwd, String pId)
	{
		String retPasswd = "";
		int passwordLen = default_passwordLen;

		if (nTypePwd == tsDBFactoryBootstrap.DM_FACTORYBOOTSTRAP_CLIENTPWD)
		{
			retPasswd = generateClientPassword(pId, passwordLen);
		}
		else if (nTypePwd == tsDBFactoryBootstrap.DM_FACTORYBOOTSTRAP_SERVERPWD)
		{
			retPasswd = generateServerPassword(pId, passwordLen);
		}
		return retPasswd;
	}

	private static String generatePassword(String deviceId, String key, int passwordLen)
	{
		String imei = null;
		String t = null;
		if (25 == passwordLen)
		{
			return null;
		}
		// by default we generate 16 chars password.
		imei = dmDevInfoAdapter.devAdpGetDeviceId();

		String newimei = get36BasedIMEI(imei);
		StringBuffer SBuffer = null;
		String buffer = imei;
		buffer = buffer.concat(key);
		buffer = buffer.concat(deviceId);

		try
		{
			MessageDigest md5Digest = MessageDigest.getInstance("MD5");
			md5Digest.reset();
			byte[] digest = md5Digest.digest(buffer.getBytes());
			char encodedStr1[] = encodeHex(digest);

			char[] encodedStr2 = new char[6];

			encodedStr2[0] = encodedStr1[2];
			encodedStr2[1] = encodedStr1[7];
			encodedStr2[2] = encodedStr1[8];
			encodedStr2[3] = encodedStr1[12];
			encodedStr2[4] = encodedStr1[25];
			encodedStr2[5] = encodedStr1[30];

			String tmpPassword = String.valueOf(encodedStr2);
			tmpPassword = tmpPassword.concat(newimei);

			SBuffer = new StringBuffer().append(tmpPassword);
			SBuffer = shuffle(SBuffer);
			SBuffer = shuffle(SBuffer);
			SBuffer = shuffle(SBuffer);

			t = new String(SBuffer);
		}
		catch (NoSuchAlgorithmException e)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
		}
		return t;
	}

	private static String get36BasedIMEI(String pDeviceID)
	{
		String pEndPtr = "";
		long nIndex = 0;
		int i = 0;
		char[] tmpimei = null;
		int nLength = 0;
		int TEMP_IMEI_SIZE = 10;
		String tmpImei;

		char[] pBase36String = new char[TEMP_IMEI_SIZE];
		tmpimei = new char[TEMP_IMEI_SIZE];

		nIndex = convertStrToUint64(pDeviceID, pEndPtr, 10);
		tmpimei = convertUint64ToA(nIndex, tmpimei, 36);

		nLength = tmpimei.length;

		if (nLength < TEMP_IMEI_SIZE)
		{
			for (i = 0; i < TEMP_IMEI_SIZE - nLength; i++)
			{
				pBase36String[i] = '0';
			}
			tmpImei = new String(pBase36String) + new String(tmpimei);
			pBase36String = tmpImei.toCharArray();
		}
		else
		{
			pBase36String = tmpimei;
			tmpImei = new String(pBase36String);
		}

		return tmpImei;
	}

	private static char[] convertUint64ToA(long nVal, char[] pBuf, int nRadix)
	{
		char[] p = null; /* pointer to traverse string */
		char[] pFirstDig = null; /* pointer to first digit */
		int nDigVal = 0; /* value of digit */
		int pi = 0;

		String spBuf = new String(pBuf);
		p = spBuf.toCharArray();

		/* save pointer to first digit */
		do
		{
			nDigVal = (int) (nVal % nRadix);
			/* get next digit */
			nVal /= nRadix;

			/* convert to ascii and store */
			if (nDigVal > 9)
			{
				/* a letter */
				p[pi++] = (char) (nDigVal - 10 + 'a');
			}
			else
			{
				/* a digit */
				p[pi++] = (char) (nDigVal + '0');
			}
		} while (nVal > 0);

		pFirstDig = new char[p.length];
		int i = 0;
		int j = 0;
		for (i = p.length - 1, j = 0; i >= 0; i--, j++)
			pFirstDig[j] = p[i];

		return pFirstDig;
	}

	private static long convertStrToUint64(String pNptr, String pEndPtr, int nBase)
	{
		boolean negative = false;
		long i = 0;
		char[] s, save = null, end = null;
		char c = 0;
		int si = 0;
		int slength = pNptr.length();

		if (nBase < 0 || nBase == 1 || nBase > 36)
			return 0;

		s = new char[slength];
		pNptr.getChars(0, slength, s, 0);
		save = s;
		while (tsLib.libIsSpace(s[si]))
			++si;

		if (si >= slength)
		{
			if (pEndPtr != null)
			{
				if (save.length - pNptr.length() >= 2 && tsLib.libToupper(save[-1]) == 'X' && save[-2] == '0')
				{
					pEndPtr = Arrays.toString(save);   //save.toString().substring(-1); // save[-1];
				}
				else
				{
					pEndPtr = pNptr;
				}
			}
			return 0;
		}

		if (s[si] == '-')
		{
			negative = true;
			++si;
		}
		else if (s[si] == '+')
		{
			negative = false;
			++si;
		}
		else
		{
			negative = false;
		}

		if (s[si] == '0')
		{
			if ((nBase == 0 || nBase == 16) && tsLib.libToupper(s[1]) == 'X')
			{
				si += 2;
				nBase = 16;
			}
			else if (nBase == 0)
				nBase = 8;
		}
		else if (nBase == 0)
		{
			nBase = 10;
		}

		save = s;
		end = null;

		i = 0;
		while (si < s.length)
		{
			c = s[si++];
			if (s == end)
			{
				break;
			}

			if (c >= '0' && c <= '9')
			{
				c -= '0';
			}
			else if (tsLib.libisalpha(c))
			{
				c = (char) (tsLib.libToupper(c) - 'A' + 10);
			}
			else
			{
				break;
			}

			if ((int) c >= nBase)
				break;

			i *= (long) nBase;
			i += c;
		}

		if (si == 0)
		{
			if (pEndPtr != null)
			{
				if (save.length - pNptr.length() >= 2 && tsLib.libToupper(save[-1]) == 'X' && save[-2] == '0')
				{
					pEndPtr = Arrays.toString(save);   //save.toString().substring(-1); // save[-1];
				}
				else
				{
					pEndPtr = pNptr;
				}
			}

			return 0;
		}

		if (pEndPtr.length() > 0)
		{
			pEndPtr = Arrays.toString(s).substring(si, s.length);//s.toString().substring(si, s.length);

		}

		return negative ? -i : i;
	}

	public static StringBuffer shuffle(StringBuffer buffer)
	{
		int nLen = buffer.length();
		int mod = nLen % 2;
		int secondHalfPos = (mod == 0 ? nLen / 2 : nLen / 2 + 1);
		char ch;
		for (int i = secondHalfPos; i < nLen; i++)
		{
			ch = buffer.charAt(i);
			buffer.deleteCharAt(i);
			int nInsertPos = (mod == 0 ? nLen - i - 1 : nLen - i);
			buffer.insert(nInsertPos, ch);
		}

		// if (mod == 1)
		// {
		// ch = buffer.charAt(nLen - 1);
		// buffer.deleteCharAt(nLen - 1);
		// buffer.insert(0, ch);
		// }

		return buffer;
	}
	// end of PasswordUtils

	private static final char[]	_hexTable	= {
    	'0', '1', '2', '3', '4', '5', '6', '7',
    	'8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    };

	public static char[] encodeHex(byte[] data)
	{
		// Borrowed from Tomas, since this is probably more efficient.
		int len = data.length;
		char[] output = new char[len * 2];
		int j = 0;

		for (int i = 0; i < len; i++)
		{
			byte d = data[i];

			output[j++] = _hexTable[d & 0x0F]; // Get low 4 bits
			output[j++] = _hexTable[(d >>> 4) & 0x0F]; // Get high 4 bits
		}

		return output;
	}

}
