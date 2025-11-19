package com.tsdm.db;

import com.tsdm.adapt.tsLib;
import com.tsdm.core.data.constants.DmDevInfoConst;

public class tsDBCrypt
{
	private static byte[] IP = 
	{
		58,50,42,34,26,18,10, 2,
		60,52,44,36,28,20,12, 4,
		62,54,46,38,30,22,14, 6,
		64,56,48,40,32,24,16, 8,
		57,49,41,33,25,17, 9, 1,
		59,51,43,35,27,19,11, 3,
		61,53,45,37,29,21,13, 5,
		63,55,47,39,31,23,15, 7
	};

	private static byte[] FP = 
	{
		40, 8,48,16,56,24,64,32,
		39, 7,47,15,55,23,63,31,
		38, 6,46,14,54,22,62,30,
		37, 5,45,13,53,21,61,29,
		36, 4,44,12,52,20,60,28,
		35, 3,43,11,51,19,59,27,
		34, 2,42,10,50,18,58,26,
		33, 1,41, 9,49,17,57,25
	};

	private static byte[] PC1_C =
	{
		57,49,41,33,25,17, 9,
		 1,58,50,42,34,26,18,
		10, 2,59,51,43,35,27,
		19,11, 3,60,52,44,36,
	};

	private static byte[] PC1_D =
	{
		63,55,47,39,31,23,15,
		 7,62,54,46,38,30,22,
		14, 6,61,53,45,37,29,
		21,13, 5,28,20,12, 4,
	};

	private static byte[] shifts = { 1,1,2,2,2,2,2,2,1,2,2,2,2,2,2,1 };

	private static byte[] PC2_C =
	{
	   14, 17, 11, 24,  1,  5,
	    3, 28, 15,  6, 21, 10,
	   23, 19, 12,  4, 26,  8,
	   16,  7, 27, 20, 13,  2
	};

	private static byte[] PC2_D =
	{
	   41, 52, 31, 37, 47, 55,
	   30, 40, 51, 45, 33, 48,
	   44, 49, 39, 56, 34, 53,
	   46, 42, 50, 36, 29, 32
	};

	private static byte[] e2 =
	{
		32, 1, 2, 3, 4, 5,
		 4, 5, 6, 7, 8, 9,
		 8, 9,10,11,12,13,
		12,13,14,15,16,17,
		16,17,18,19,20,21,
		20,21,22,23,24,25,
		24,25,26,27,28,29,
		28,29,30,31,32, 1
	};

	private static byte[][] S =
	{
		{
		14, 4,13, 1, 2,15,11, 8, 3,10, 6,12, 5, 9, 0, 7,
		 0,15, 7, 4,14, 2,13, 1,10, 6,12,11, 9, 5, 3, 8,
		 4, 1,14, 8,13, 6, 2,11,15,12, 9, 7, 3,10, 5, 0,
		15,12, 8, 2, 4, 9, 1, 7, 5,11, 3,14,10, 0, 6,13,
		},
		{
		15, 1, 8,14, 6,11, 3, 4, 9, 7, 2,13,12, 0, 5,10,
		 3,13, 4, 7,15, 2, 8,14,12, 0, 1,10, 6, 9,11, 5,
		 0,14, 7,11,10, 4,13, 1, 5, 8,12, 6, 9, 3, 2,15,
		13, 8,10, 1, 3,15, 4, 2,11, 6, 7,12, 0, 5,14, 9,
		},
		{
		10, 0, 9,14, 6, 3,15, 5, 1,13,12, 7,11, 4, 2, 8,
		13, 7, 0, 9, 3, 4, 6,10, 2, 8, 5,14,12,11,15, 1,
		13, 6, 4, 9, 8,15, 3, 0,11, 1, 2,12, 5,10,14, 7,
		 1,10,13, 0, 6, 9, 8, 7, 4,15,14, 3,11, 5, 2,12,
		},
		{
		 7,13,14, 3, 0, 6, 9,10, 1, 2, 8, 5,11,12, 4,15,
		13, 8,11, 5, 6,15, 0, 3, 4, 7, 2,12, 1,10,14, 9,
		10, 6, 9, 0,12,11, 7,13,15, 1, 3,14, 5, 2, 8, 4,
		 3,15, 0, 6,10, 1,13, 8, 9, 4, 5,11,12, 7, 2,14,
		},
		{
		 2,12, 4, 1, 7,10,11, 6, 8, 5, 3,15,13, 0,14, 9,
		14,11, 2,12, 4, 7,13, 1, 5, 0,15,10, 3, 9, 8, 6,
		 4, 2, 1,11,10,13, 7, 8,15, 9,12, 5, 6, 3, 0,14,
		11, 8,12, 7, 1,14, 2,13, 6,15, 0, 9,10, 4, 5, 3,
		},
		{
		12, 1,10,15, 9, 2, 6, 8, 0,13, 3, 4,14, 7, 5,11,
		10,15, 4, 2, 7,12, 9, 5, 6, 1,13,14, 0,11, 3, 8,
		 9,14,15, 5, 2, 8,12, 3, 7, 0, 4,10, 1,13,11, 6,
		 4, 3, 2,12, 9, 5,15,10,11,14, 1, 7, 6, 0, 8,13,
		},
		{
		 4,11, 2,14,15, 0, 8,13, 3,12, 9, 7, 5,10, 6, 1,
		13, 0,11, 7, 4, 9, 1,10,14, 3, 5,12, 2,15, 8, 6,
		 1, 4,11,13,12, 3, 7,14,10,15, 6, 8, 0, 5, 9, 2,
		 6,11,13, 8, 1, 4,10, 7, 9, 5, 0,15,14, 2, 3,12,
		},
		{
		13, 2, 8, 4, 6,15,11, 1,10, 9, 3,14, 5, 0,12, 7,
		 1,15,13, 8,10, 3, 7, 4,12, 5, 6,11, 0,14, 9, 2,
		 7,11, 4, 1, 9,12,14, 2, 0, 6,10,13,15, 3, 5, 8,
		 2, 1,14, 7, 4,10, 8,13,15,12, 9, 0, 3, 5, 6,11
		}
	};

	private static byte[] P = 
	{
		16, 7,20,21,
		29,12,28,17,
		 1,15,23,26,
		 5,18,31,10,
		 2, 8,24,14,
		32,27, 3, 9,
		19,13,30, 6,
		22,11, 4,25,
	};

	private static int		MAX_CRYPT_BITS_SIZE	= 64;
	private static int		MAX_ENCRYPT_SIZE	= 16;

	private static String cryptCryptResult;
	private static byte[] cryptCryptByte;

	private static byte[]	C;
	private static byte[]	D;
	private static byte[][]	KS;
	private static byte[]	E;
	private static byte[]	preS;

	public tsDBCrypt()
	{
		C = new byte[28];
		D = new byte[28];
		KS = new byte[16][48];
		E = new byte[48];
		preS = new byte[48];

		cryptCryptByte = new byte[MAX_ENCRYPT_SIZE];

	}

	private byte[] cryptInitPassword(byte[] key, byte[] password)
	{
		int i = 0;
		int j = 0;

		if ((key == null) || (password == null))
			return null;

		int k = 0;

		while (key[k] != '\0' && (i < MAX_CRYPT_BITS_SIZE))
		{
			for (j = 6; j >= 0; j--)
			{
				password[i] = (byte) ((key[k] >> j) & 0x01);
				i++;
			}
			k++;
			password[i++] = 0;
		}

		while (i < MAX_CRYPT_BITS_SIZE + 2)
		{
			password[i++] = 0;
		}

		return password;
	}

	private byte[] crypttZeroPassword(byte[] password)
	{
		int i;

		for (i = 0; i < MAX_CRYPT_BITS_SIZE + 2; i++)
		{
			password[i] = 0;
		}
		return password;
	}

	private void cryptSetKey(byte[] key)
	{
		int i, j, k;
		byte temp;

		for (i = 0; i < 28; i++)
		{
			C[i] = key[PC1_C[i] - 1];
			D[i] = key[PC1_D[i] - 1];
		}

		for (i = 0; i < 16; i++)
		{
			for (k = 0; k < shifts[i]; k++)
			{
				temp = C[0];
				for (j = 0; j < 28 - 1; j++)
				{
					C[j] = C[j + 1];
				}
				C[27] = temp;

				temp = D[0];
				for (j = 0; j < 28 - 1; j++)
				{
					D[j] = D[j + 1];
				}
				D[27] = temp;
			}

			for (j = 0; j < 24; j++)
			{
				KS[i][j] = C[PC2_C[j] - 1];
				// KS[i][j+24] = D[PC2_D[j]-1];
				KS[i][j + 24] = D[PC2_D[j] - 28 - 1];
			}
		}

		for (i = 0; i < 48; i++)
		{
			E[i] = e2[i];
		}
	}

	private void cryptEExpandsion(byte[] salt)
	{
		int i, j;
		byte temp;

		if (salt == null)
			return;

		int k = 0;

		for (i = 0; i < 2; i++)
		{
			byte c;

			c = salt[k++];
			cryptCryptByte[i] = c;

			if (c > 'Z')
			{
				c -= 6 + 7 + '.';
			}
			else if (c > '9')
			{
				c -= 7 + '.';
			}
			else
			{
				c -= '.';
			}

			for (j = 0; j < 6; j++)
			{
				if ((byte) ((c >> j) & 0x01) != '\0')
				{
					temp = E[6 * i + j];
					E[6 * i + j] = E[6 * i + j + 24];
					E[6 * i + j + 24] = temp;
				}
			}
		}
	}

	private byte[] cryptDESEncrypt(byte[] block)
	{
		int i, ii, j, k;
		byte[] left = new byte[32];
		byte[] right = new byte[32];
		byte[] old = new byte[32];
		byte[] f = new byte[32];
		byte temp;

		for (j = 0; j < 32; j++)
		{
			left[j] = block[IP[j] - 1];
		}

		for (; j < 64; j++)
		{
			right[j - 32] = block[IP[j] - 1];
		}

		for (ii = 0; ii < 16; ii++)
		{
			i = ii;

			for (j = 0; j < 32; j++)
			{
				old[j] = right[j];
			}

			for (j = 0; j < 48; j++)
			{
				preS[j] = (byte) (right[E[j] - 1] ^ KS[i][j]);
			}

			for (j = 0; j < 8; j++)
			{
				temp = (byte) (6 * j);

				int s1 = preS[temp + 0] << 5;
				int s2 = preS[temp + 1] << 3;
				int s3 = preS[temp + 2] << 2;
				int s4 = preS[temp + 3] << 1;
				int s5 = preS[temp + 4] << 0;
				int s6 = preS[temp + 5] << 4;

				int sSum = s1 + s2 + s3 + s4 + s5 + s6;
				k = (int) S[j][sSum];

				temp = (byte) (4 * j);
				f[temp + 0] = (byte) ((k >> 3) & 0x01);
				f[temp + 1] = (byte) ((k >> 2) & 0x01);
				f[temp + 2] = (byte) ((k >> 1) & 0x01);
				f[temp + 3] = (byte) ((k >> 0) & 0x01);
			}

			for (j = 0; j < 32; j++)
			{
				right[j] = (byte) (left[j] ^ f[P[j] - 1]);
			}

			for (j = 0; j < 32; j++)
			{
				left[j] = old[j];
			}
		}

		for (j = 0; j < 32; j++)
		{
			temp = left[j];
			left[j] = right[j];
			right[j] = temp;
		}

		j = 0;
		for (j = 0; j < 64; j++)
		{
			i = FP[j];
			if (i < 33)
			{
				block[j] = left[FP[j] - 1];
			}
			else
			{
				block[j] = right[FP[j] - 33];
			}
		}
		return block;
	}

	private void cryptEncrypt(byte[] password)
	{
		int i, j;
		byte c;

		for (i = 0; i < 25; i++)
		{
			password = cryptDESEncrypt(password);
		}

		for (i = 0; i < 11; i++)
		{
			c = 0;
			for (j = 0; j < 6; j++)
			{
				c <<= 1;
				c |= password[6 * i + j];
			}
			c += '.';
			if (c > '9')
			{
				c += 7;
			}
			if (c > 'Z')
			{
				c += 6;
			}
			cryptCryptByte[i + 2] = c;
		}
		cryptCryptByte[i + 2] = '\0';
		if (cryptCryptByte[1] == '\0')
		{
			cryptCryptByte[1] = cryptCryptByte[0];
		}
	}
	public String cryptGenerate(String strKey, byte[] salt)
	{
		byte[] password = new byte[MAX_CRYPT_BITS_SIZE + 2];
		byte[] key = new byte[strKey.length()];
		try
		{
			key = strKey.getBytes();
			password = cryptInitPassword(key, password);

			if (password != null)
			{
				cryptSetKey(password);
				password = crypttZeroPassword(password);
				cryptEExpandsion(salt);
				cryptEncrypt(password);
			}

			cryptCryptResult = null;
			cryptCryptResult = new String(cryptCryptByte);
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DmDevInfoConst.DEBUG_DM, " Fail");
		}
		return cryptCryptResult;
	}
}
