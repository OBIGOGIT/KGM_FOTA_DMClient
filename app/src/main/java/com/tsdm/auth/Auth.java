package com.tsdm.auth;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.tsdm.agent.dmDefineDevInfo;
import com.tsdm.adapt.tsLib;

public class Auth implements dmDefineDevInfo
{
	public static com.tsdm.auth.md5 md5;

	private static final int SHA_KEY_PAD_LEN = 64;
	private static final int SHA_KEY_PAD_LEN_ = 64;

	public static String authCredType2String(int type)
	{
		switch (type)
		{
			case CRED_TYPE_BASIC:
				return CRED_TYPE_STRING_BASIC;
			case CRED_TYPE_MD5:
				return CRED_TYPE_STRING_MD5;
			case CRED_TYPE_HMAC:
				return CRED_TYPE_STRING_HMAC;
			case CRED_TYPE_X509:
				return CRED_TYPE_STRING_X509;
			case CRED_TYPE_SECUREID:
				return CRED_TYPE_STRING_SECUREID;
			case CRED_TYPE_SAFEWORD:
				return CRED_TYPE_STRING_SAFEWORD;
			case CRED_TYPE_DIGIPASS:
				return CRED_TYPE_STRING_DIGIPASS;
			default:
				tsLib.debugPrintException(DEBUG_EXCEPTION, "Not Support Auth Type");
				return null;
		}
	}

	public static int authCredString2Type(String type)
	{
		if (type.compareTo(CRED_TYPE_STRING_BASIC) == 0)
			return CRED_TYPE_BASIC;
		else if (type.compareTo(CRED_TYPE_STRING_MD5) == 0)
			return CRED_TYPE_MD5;
		else if (type.compareTo(CRED_TYPE_STRING_HMAC) == 0)
			return CRED_TYPE_HMAC;
		else if (type.compareTo(CRED_TYPE_STRING_X509) == 0)
			return CRED_TYPE_X509;
		else if (type.compareTo(CRED_TYPE_STRING_SECUREID) == 0)
			return CRED_TYPE_SECUREID;
		else if (type.compareTo(CRED_TYPE_STRING_SAFEWORD) == 0)
			return CRED_TYPE_SAFEWORD;
		else if (type.compareTo(CRED_TYPE_STRING_DIGIPASS) == 0)
			return CRED_TYPE_DIGIPASS;
		else
			return CRED_TYPE_NONE;
	}

	public static String authAAuthType2String(int type)
	{
		switch (type)
		{
			case CRED_TYPE_BASIC:
				return AUTH_TYPE_BASIC;
			case CRED_TYPE_MD5:
				return AUTH_TYPE_DIGEST;
			case CRED_TYPE_HMAC:
				return AUTH_TYPE_HMAC;
			case CRED_TYPE_X509:
				return AUTH_TYPE_X509;
			case CRED_TYPE_SECUREID:
				return AUTH_TYPE_SECUREID;
			case CRED_TYPE_SAFEWORD:
				return AUTH_TYPE_SAFEWORD;
			case CRED_TYPE_DIGIPASS:
				return AUTH_TYPE_DIGIPASS;
			default:
				return AUTH_TYPE_NONE;
		}
	}

	public static int authAAuthtring2Type(String type)
	{
		if (type.compareTo(AUTH_TYPE_BASIC) == 0)
			return CRED_TYPE_BASIC;
		else if (type.compareTo(AUTH_TYPE_DIGEST) == 0)
			return CRED_TYPE_MD5;
		else if (type.compareTo(AUTH_TYPE_HMAC) == 0)
			return CRED_TYPE_HMAC;
		else if (type.compareTo(AUTH_TYPE_X509) == 0)
			return CRED_TYPE_X509;
		else if (type.compareTo(AUTH_TYPE_SECUREID) == 0)
			return CRED_TYPE_SECUREID;
		else if (type.compareTo(AUTH_TYPE_DIGIPASS) == 0)
			return CRED_TYPE_DIGIPASS;
		else
			return CRED_TYPE_NONE;
	}

	public static String authMakeDigest(int authType, String userName, String passWord, byte[] nonce, int nonceLength, byte[] packetBody, int bodyLength, String serverID)
	{
		String ret = null;
		String creddata = null;
		String databody;
		byte[] digest = new byte[16];

		switch (authType)
		{
			case CRED_TYPE_BASIC:
			{
				if ((userName == null) || (passWord == null))
				{
					tsLib.debugPrintException(DEBUG_EXCEPTION, "userName or passWord is NULL");
					return null;
				}
				break;
			}
			case CRED_TYPE_MD5:
			{
				if ((userName == null) || (passWord == null) || (nonce == null) || (nonceLength <= 0))
				{
					tsLib.debugPrintException(DEBUG_EXCEPTION, "userName or passWord or nonce or nonceLength is NULL");
					return null;
				}
				break;
			}
			case CRED_TYPE_HMAC:
			case CRED_TYPE_MD5_NOT_BASE64:
			{
				if ((userName == null) || (passWord == null) || (nonce == null) || (nonceLength <= 0) || (packetBody == null) || (bodyLength <= 0))
				{
					tsLib.debugPrintException(DEBUG_EXCEPTION, "userName or passWord or nonce or nonceLength or packetBody or bodyLength is NULL");
					return null;
				}
				break;
			}
			default:
			{
				tsLib.debugPrint(DEBUG_AUTH, "Not Support Auth Type");
				return null;
			}
		}

		switch (authType)
		{
			case CRED_TYPE_BASIC:
			{
				creddata = userName;
				creddata = creddata.concat(":");
				creddata = creddata.concat(passWord);
				digest = base64.encode(creddata.getBytes());
				ret = new String(digest);

				tsLib.debugPrint(DEBUG_AUTH, "CRED_TYPE_BASIC name:" + userName + " pwd:" + passWord + " cred:" + creddata + " ret:" + ret);
				creddata = null;
				break;
			}
			case CRED_TYPE_MD5:
			{

				com.tsdm.auth.md5 md5 = new md5();
				byte[] md5digest = md5.computeMD5Credentials(userName, passWord, nonce, serverID);
				ret = new String(md5digest);

				String non = new String(nonce);
				tsLib.debugPrint(DEBUG_AUTH, "CRED_TYPE_MD5 name= " + userName + " pwd= " + passWord + " nonce= " + non + " ret= " + ret);

				creddata = null;
				break;
			}
			case CRED_TYPE_HMAC:
			case CRED_TYPE_MD5_NOT_BASE64: // for Dm noti.
			{
				MessageDigest md5 = null;
				try
				{
					md5 = MessageDigest.getInstance("MD5");
				}
				catch (NoSuchAlgorithmException e)
				{
					tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
				}

				// Defects
				if (md5 == null)
					return null;

				creddata = userName;
				creddata = creddata.concat(":");
				creddata = creddata.concat(passWord); // username and password
				md5.reset();

				digest = md5.digest(creddata.getBytes());
				byte[] encoder = base64.encode(digest);

				creddata = new String(encoder);
				digest = md5.digest(packetBody);
				byte[] encoder2 = base64.encode(digest);
				databody = new String(encoder2);

				creddata = creddata.concat(":");
				creddata = creddata.concat(new String(nonce));
				creddata = creddata.concat(":");
				creddata = creddata.concat(databody);
				digest = md5.digest(creddata.getBytes());
				if (authType == CRED_TYPE_HMAC)
				{
					byte[] encoder3 = base64.encode(digest);
					ret = null;
					ret = new String(encoder3);
				}
				else
				// for DM noti. not base64encode
				{
					ret = new String(digest);
				}
				break;
			}
		}
		return ret;
	}

	public static String authMakeDigestSHA1(int nAuthType, byte[] pszSecretKey, int nSecertLen, byte[] pszPacketBody, int nBodyLen)
	{
		int nSecretLen = nSecertLen;
		byte[] szK_IPad = new byte[SHA_KEY_PAD_LEN_];
		byte[] szK_OPad = new byte[SHA_KEY_PAD_LEN_];
		byte[] szTemp = null;
		byte[] digest = null;
		String digestStr = "";

		switch (nAuthType)
		{
			case CRED_TYPE_SHA1:
				if ((pszSecretKey == null) || (pszPacketBody == null))
				{
					return null;
				}
				break;
			default:
				tsLib.debugPrintException(DEBUG_EXCEPTION, "Not Support Auth Type.");
				return null;
		}

		if (nSecretLen > SHA_KEY_PAD_LEN)
		{
			; // waht problem????
		}
		else
		{
			for (int i = 0; i < nSecretLen; i++)
			{
				szK_IPad[i] = pszSecretKey[i];
			}
		}

		for (int i = 0; i < SHA_KEY_PAD_LEN_; i++)
		{
			szK_OPad[i] = szK_IPad[i];
		}

		for (int nCount = 0; nCount < SHA_KEY_PAD_LEN; nCount++)
		{
			szK_IPad[nCount] ^= 0x36;
			szK_OPad[nCount] ^= 0x5c;
		}

		MessageDigest sha = null;
		try
		{
			sha = MessageDigest.getInstance("SHA-1");
		}
		catch (NoSuchAlgorithmException e)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
		}

		// Defects
		if (sha == null)
			return null;

		sha.update(szK_IPad);
		sha.update(pszPacketBody);
		szTemp = sha.digest();

		sha.update(szK_OPad);
		sha.update(szTemp);
		digest = sha.digest();

		digestStr = tsLib.bytesToHexString(digest);
		digestStr = digestStr.toUpperCase();

		return digestStr;
	}

	public static String authFactoryDigest(byte[] buf, byte[] digest)
	{
/*		MessageDigest md5 = null;
		try
		{
			md5 = MessageDigest.getInstance("MD5");
		}
		catch (NoSuchAlgorithmException e)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
		}

		// Defects
		String retStr = null;
		if (md5 != null)
		{
			digest = md5.digest(buf);
			retStr = new String(digest);
		}

		return retStr;*/
		return null;
	}

	public static String authMakeNonceDigest(int authType, String userName, String passWord, byte[] nonce, int nonceLength, byte[] packetBody, int bodyLength)
	{

		String ret = null;
		// String creddata=null;
		// String databody;
		byte[] digest = new byte[16];

		if ((userName == null) || (passWord == null) || (nonce == null) || (nonceLength <= 0) || (packetBody == null) || (bodyLength <= 0))
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, "userName or passWord or nonce or nonceLength or packetBody or bodyLength is NULL");
			return null;
		}

		MessageDigest md5 = null;

		try
		{
			md5 = MessageDigest.getInstance("MD5");
		}
		catch (NoSuchAlgorithmException e)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
		}

		if (md5 != null)
		{
			md5.reset();
			md5.update(userName.getBytes());
			md5.update(":".getBytes());
			md5.update(passWord.getBytes());
			digest = md5.digest();

			byte[] encoder = base64.encode(digest);

			// creddata = new String(encoder);
			md5.update(packetBody);
			digest = md5.digest();
			byte[] encoder2 = base64.encode(digest);

			int len = encoder.length + 1 + nonce.length + 1 + encoder2.length;
			int loc = 0;
			byte[] buff = new byte[len];

			int i = 0;

			for (i = 0; i < encoder.length; i++)
			{
				buff[i] = encoder[i];
			}

			buff[encoder.length] = (byte) 0x3A; // ":"
			loc = encoder.length + 1;

			for (i = 0; i < nonce.length; i++)
			{
				buff[i + loc] = (byte) nonce[i];
			}

			loc = loc + nonce.length;
			buff[loc] = (byte) 0x3A; // ":"
			loc = loc + 1;

			for (i = 0; i < encoder2.length; i++)
			{
				buff[i + loc] = encoder2[i];
			}

			md5.update(buff);
			digest = md5.digest();
		}

		if (authType == CRED_TYPE_HMAC)
		{
			byte[] encoder3 = base64.encode(digest);
			ret = null;
			ret = new String(encoder3);
		}
		else
		// for DM noti. not base64encode
		{
			ret = new String(digest);
		}

		return ret;
	}
}
