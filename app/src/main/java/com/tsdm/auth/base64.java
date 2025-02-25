/*
 * Copyright 2001-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tsdm.auth;

import android.util.Base64;

import com.tsdm.agent.dmDefineDevInfo;
import com.tsdm.adapt.tsLib;

public class base64 implements dmDefineDevInfo
{
	private static byte[]	base64Alphabet	= new byte[255];

	static
	{
		for (int i = 0; i < 255; i++)
		{
			base64Alphabet[i] = (byte) -1;
		}
		for (int i = 'Z'; i >= 'A'; i--)
		{
			base64Alphabet[i] = (byte) (i - 'A');
		}
		for (int i = 'z'; i >= 'a'; i--)
		{
			base64Alphabet[i] = (byte) (i - 'a' + 26);
		}
		for (int i = '9'; i >= '0'; i--)
		{
			base64Alphabet[i] = (byte) (i - '0' + 52);
		}

		base64Alphabet['+'] = 62;
		base64Alphabet['/'] = 63;
	}


	@SuppressWarnings("unchecked")
	public static byte[] encode(byte[] binaryData)
	{
		byte[] buf = null;

		try
		{
			buf = Base64.encode(binaryData, Base64.NO_WRAP);
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
		}

		return buf;
	}

	@SuppressWarnings("unchecked")
	public static byte[] decode(byte[] base64Data)
	{
		byte[] buf = null;
		byte[] myroomedData;
		int ncnt = 0;
		try
		{
			buf = Base64.decode(base64Data, Base64.NO_WRAP);
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
		}

		return buf;
	}
}
