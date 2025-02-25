package com.tsdm.auth;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.digest.UnixCrypt;

public class generatePassword {

	public static final int	GENERATE_TYPE_DEVICE_ID			= 0;
	public static final int	GENERATE_TYPE_DEVICE_PASSWORD	= 1;
	public static final int	GENERATE_TYPE_SERVER_PASSWORD	= 2;

	private static byte[][]	keyDict	=
	{
		{0x02, 0x20, 0x05, 0x09, 0x30, 0x2f, 0x0a, 0x29, 0x22, 0x2a, 0x1f, 0x07, 0x08, 0x04, 0x0c, 0x3a, 0x35, 0x33, 0x4f, 0x41,
		 0x0b, 0x03, 0x25, 0x00, 0x31, 0x3e, 0x06, 0x33, 0x3c, 0x00, 0x3f, 0x2f, 0x05, 0x21, 0x01, 0x40, 0x45, 0x4c, 0x4e, 0x3b
		},
		{0x00, 0x31, 0x3e, 0x06, 0x33, 0x1f, 0x07, 0x08, 0x04, 0x0c, 0x09, 0x30, 0x2f, 0x0b, 0x03, 0x40, 0x45, 0x33, 0x4f, 0x41,
		 0x0a, 0x06, 0x10, 0x0e, 0x0a, 0x0b, 0x05, 0x0e, 0x12, 0x04, 0x0b, 0x07, 0x0d, 0x0c, 0x05, 0x3a, 0x35, 0x35, 0x33, 0x4f
		},
		{0x20, 0x09, 0x2f, 0x29, 0x1f, 0x04, 0x09, 0x01, 0x04, 0x3a, 0x3c, 0x0d, 0x29, 0x4f, 0x03, 0x3e, 0x35, 0x33, 0x41, 0x0b,
		 0x04, 0x3e, 0x10, 0x3c, 0x05, 0x40, 0x0d, 0x0e, 0x3b, 0x05, 0x0b, 0x3c, 0x04, 0x09, 0x0c, 0x07, 0x35, 0x29, 0x41, 0x4f
		}
	};

	public static String generateKey(String filterNum, int type){
		int serialNum1 = 0;
		int serialNum2 = 0;
		String key = "";

		for(int i=0; i< filterNum.length() - 1; i++){

		  serialNum1 +=Integer.valueOf(filterNum.charAt(i)) * keyDict[type][i];

		  serialNum2 +=Integer.valueOf(filterNum.charAt(i)) * Integer.valueOf(filterNum.charAt(filterNum.length() - i - 1)) * keyDict[type][i];
		}

		key = String.valueOf(serialNum1) + String.valueOf(serialNum2);

		return key;
	}

	public static String generatePassword(String deviceId, String serverId, int type)
	{
		byte[] szBuf = null;
		int cnt = 0;
		String szGenerateResult = "";

		String key = generateKey(deviceId, type);

		String digestKey = deviceId + key + serverId;


		try{
			szBuf = digestKey.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e){
			//
		}

		try{
			MessageDigest md5Digest = MessageDigest.getInstance("MD5");
			md5Digest.reset();

			byte[] digest = md5Digest.digest(szBuf);
			char[] digestHex = encodeHex(digest);

			String cryptResult = crypt(digestKey);

			char[] cBuf = new char[5];
			cBuf[0] = digestHex[0];
			cBuf[1] = digestHex[2];
			cBuf[2] = digestHex[5];
			cBuf[3] = digestHex[6];
			cBuf[4] = digestHex[8];

			String temppasswd = null;
			temppasswd = String.valueOf(cBuf);
			temppasswd = temppasswd.concat(cryptResult);

			StringBuffer sBuffer = null;
			sBuffer = new StringBuffer().append(temppasswd);


			if(type == GENERATE_TYPE_DEVICE_ID)	//DeviceId
			{
				for (cnt = 0; cnt < 4; cnt++){
					sBuffer = shuffle(sBuffer);
				}
			}
			else if(type == GENERATE_TYPE_DEVICE_PASSWORD)	//type == Device Password
			{
				for (cnt = 0; cnt < 5; cnt++){
					sBuffer = shuffle(sBuffer);
				}
			}
			else //type == Server Password
			{
				for (cnt = 0; cnt < 6; cnt++){
					sBuffer = shuffle(sBuffer);
				}
			}
			szGenerateResult = new String(sBuffer);

		} catch (NoSuchAlgorithmException e){

		} catch (ArrayIndexOutOfBoundsException e){

		}
		return szGenerateResult;
	}

	private static final char[] HEX_TABLE = { '0', '1', '2', '3', '4', '5',
			'6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

	public static char[] encodeHex(byte[] data){
		// Borrowed from Tomas, since this is probably more efficient.
		int len = data.length;
		char[] output = new char[len * 2];
		int j = 0;

		for (int i = 0; i < len; i++){
			byte d = data[i];

			output[j++] = HEX_TABLE[d & 0x0F]; // Get low 4 bits
			output[j++] = HEX_TABLE[(d >>> 4) & 0x0F]; // Get high 4 bits
		}

		return output;
	}

	public static String crypt(String cryptKey){

		String salt = String.valueOf(cryptKey.charAt(cryptKey.length() - 1)) + String.valueOf(cryptKey.charAt(cryptKey.length() - 2));
		return UnixCrypt.crypt(cryptKey, salt);
	}

	public static StringBuffer shuffle(StringBuffer buffer){
		int nLen = buffer.length();
		int mod = nLen % 2;
		int secondHalfPos = (mod == 0 ? nLen / 2 : nLen / 2 + 1);
		char ch;
		for (int i = secondHalfPos; i < nLen; i++){
			ch = buffer.charAt(i);
			buffer.deleteCharAt(i);
			int nInsertPos = (mod == 0 ? nLen - i - 1 : nLen - i);
			buffer.insert(nInsertPos, ch);
		}

		return buffer;
	}
}
