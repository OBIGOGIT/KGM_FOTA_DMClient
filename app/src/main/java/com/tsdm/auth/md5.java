package com.tsdm.auth;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.tsdm.adapt.tsLib;
import com.tsdm.core.data.constants.DmDevInfoConst;

public class md5
{
	private Object MD5Sum;

	public final byte[] computeMD5Credentials(String username, String passwordTemp, byte[] nonce, String serverID)
	{
		String password = generatePassword.generatePassword(username, serverID, generatePassword.GENERATE_TYPE_DEVICE_PASSWORD);

		String digestStr;
		String userAndPassword = username;
		userAndPassword = userAndPassword.concat(":");
		userAndPassword = userAndPassword.concat(password);

		try
		{
			MD5Sum = MessageDigest.getInstance("MD5");
			((MessageDigest) MD5Sum).reset();
		}
		catch (NoSuchAlgorithmException e)
		{
			tsLib.debugPrintException(DmDevInfoConst.DEBUG_EXCEPTION, e.toString());
		}

		// Client의 Digest를 생성 B64(H(username:password))
		String userDigest = new String(base64.encode(((MessageDigest) MD5Sum).digest(userAndPassword.getBytes())));

		// Digest = H(B64(H(username:password)):nonce)
		byte[] userDigestBytes = userDigest.getBytes();
		byte[] buf = new byte[userDigestBytes.length + 1 + nonce.length];

		System.arraycopy(userDigestBytes, 0, buf, 0, userDigestBytes.length);
		buf[userDigestBytes.length] = (byte) ':';
		System.arraycopy(nonce, 0, buf, userDigestBytes.length + 1, nonce.length);

		byte[] digest = ((MessageDigest) MD5Sum).digest(buf);
		byte[] credentials = base64.encode(digest);

		return credentials;
	}

}
