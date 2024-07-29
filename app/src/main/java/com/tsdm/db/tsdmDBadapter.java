package com.tsdm.db;

import java.io.*;

import com.tsdm.auth.base64;
import com.tsdm.agent.dmDefineDevInfo;
import com.tsdm.agent.dmDevinfoAdapter;
import com.tsdm.adapt.tsLib;
import com.tsdm.net.netDefine;

public class tsdmDBadapter extends tsDBfile implements dmDefineDevInfo, netDefine, tsDefineDB
{
	private static final long	serialVersionUID				= 1L;

	private static final String DEFAULT_NULL_ADDRESS = "0.0.0.0";
	private static final String DEFAULT_PORT = "8080";

	private static final int 	DM_FUMO_X_NODE_COUNT_COMMON = 1;
	private static final String DM_FUMO_X_NODE_COMMON = "/FUMO-1";

	private static final String	REAL_DM_CONNECTION_NAME			= "DM Profile";

	public static int 			BPE_READFAIL_ERROR_CODE = -1;

	public final static int 	BPE_SUCCESS = 0;							// 1

	private Object				obj;

	public tsDBfile dmdb;
	public int					nFlag;
	public final static boolean DM_CON_REF_ACTIVED = true;
	public final static boolean	DM_CON_REF_NOT_ACTIVED = false;

	public tsdmDBadapter()
	{
		super();
		dmdb = new tsDBfile();
	}

	public static int dmDBAdpgetFlag()
	{
			return 1;

	}
	public static int dmDBAdpGetFUMOxNodenCount(String pszXnode)
	{
		return DM_FUMO_X_NODE_COUNT_COMMON;
	}

	public static String dmDBAdpGetFUMOxNodeName()
	{
		return DM_FUMO_X_NODE_COMMON;
	}

	public static int FileFreeSizeCheck(int nDataSize, int nAgentType)
	{
		int nret = TS_FS_OK;
		long RemainSize = 0;
		long TotalSize = 0;

		if(_SYNCML_TS_DM_DELTA_INTERIOR_MEMORY_STORAGE_)
		{
			int nDeltaFileIndex = tsdmDB.dmdbGetDeltaFileSaveIndex();
			if(nDeltaFileIndex == DELTA_EXTERNAL_MEMORY)
			{
				if(dmDevinfoAdapter.checkExternalMemoryAvailable())
				{
					RemainSize = dmDevinfoAdapter.getAvailableExternalMemorySize();
					TotalSize = dmDevinfoAdapter.getTotalExternalMemorySize();
				}
			}
			else
			{
				RemainSize = dmDevinfoAdapter.getAvailableInternalMemorySize();
				TotalSize = dmDevinfoAdapter.getTotalInternalMemorySize();
			}
		}
		else if(_SYNCML_TS_DM_DELTA_MULTI_MEMORY_STORAGE_)
		{
			int nDeltaFileIndex = tsdmDB.dmdbGetDeltaFileSaveIndex();
			if(nDeltaFileIndex == DELTA_EXTERNAL_MEMORY)
			{
				if(dmDevinfoAdapter.checkExternalMemoryAvailable())
				{
					RemainSize = dmDevinfoAdapter.getAvailableExternalMemorySize();
					TotalSize = dmDevinfoAdapter.getTotalExternalMemorySize();
				}
			}
			else if(nDeltaFileIndex == DELTA_EXTERNAL_SD_MEMORY)
			{
				if(dmDevinfoAdapter.checkExternalSdMemoryAvailable())
				{
					RemainSize = dmDevinfoAdapter.getAvailableExternalSdMemorySize();
					TotalSize = dmDevinfoAdapter.getTotalExternalSdMemorySize();
				}
			}
			else
			{
				RemainSize = dmDevinfoAdapter.getAvailableInternalMemorySize();
				TotalSize = dmDevinfoAdapter.getTotalInternalMemorySize();
			}
		}
		else if (_SYNCML_TS_DM_DELTA_EXTERNAL_STORAGE_)
		{
			if(dmDevinfoAdapter.checkExternalMemoryAvailable())
			{
				RemainSize = dmDevinfoAdapter.getAvailableExternalMemorySize();
				TotalSize = dmDevinfoAdapter.getTotalExternalMemorySize();
			}
		}
		else
		{
			RemainSize = dmDevinfoAdapter.getAvailableInternalMemorySize();
			TotalSize = dmDevinfoAdapter.getTotalInternalMemorySize();
		}
		tsLib.debugPrint(DEBUG_DM, String.format("External Memory Size is %d %d and Delta Size is %d bytes", RemainSize, TotalSize, nDataSize));
		
		if (RemainSize <= (long)nDataSize)
		{
			nret = TS_ERR_NO_MEM_READY;
		}
		
		return nret;
	}
	
	public static int FUMOMultiMemoryFreeSizeCheck(int nDataSize, int nDeltaFileIndex)
	{
		int nret = TS_FS_OK;
		long RemainSize = 0;
		long TotalSize = 0;
		
		if (_SYNCML_TS_DM_DELTA_INTERIOR_MEMORY_STORAGE_)
		{
			if (nDeltaFileIndex == DELTA_EXTERNAL_MEMORY)
			{
				if (dmDevinfoAdapter.checkExternalMemoryAvailable())
				{
					RemainSize = dmDevinfoAdapter.getAvailableExternalMemorySize();
					TotalSize = dmDevinfoAdapter.getTotalExternalMemorySize();
				}
			}
			else
			{
				RemainSize = dmDevinfoAdapter.getAvailableInternalMemorySize();
				TotalSize = dmDevinfoAdapter.getTotalInternalMemorySize();
			}
		}
		else if (_SYNCML_TS_DM_DELTA_MULTI_MEMORY_STORAGE_)
		{
			if (nDeltaFileIndex == DELTA_EXTERNAL_MEMORY)
			{
				if (dmDevinfoAdapter.checkExternalMemoryAvailable())
				{
					RemainSize = dmDevinfoAdapter.getAvailableExternalMemorySize();
					TotalSize = dmDevinfoAdapter.getTotalExternalMemorySize();
				}
			}
			else if (nDeltaFileIndex == DELTA_EXTERNAL_SD_MEMORY)
			{
				if (dmDevinfoAdapter.checkExternalSdMemoryAvailable())
				{
					RemainSize = dmDevinfoAdapter.getAvailableExternalSdMemorySize();
					TotalSize = dmDevinfoAdapter.getTotalExternalSdMemorySize();
				}
			}
			else
			{
				RemainSize = dmDevinfoAdapter.getAvailableInternalMemorySize();
				TotalSize = dmDevinfoAdapter.getTotalInternalMemorySize();
			}
		}
		tsLib.debugPrint(DEBUG_DB, " Index : " + nDeltaFileIndex);
		tsLib.debugPrint(DEBUG_DM, String.format("Free Memory Size is %d/%d and Delta Size is %d bytes", RemainSize, TotalSize, nDataSize));
		
		if (RemainSize <= (long)nDataSize)
		{
			nret = TS_ERR_NO_MEM_READY;
		}
		
		return nret;
	}

	public int FileExists(String pszFileName, int fileID)
	{
		File file = new File(pszFileName);

		if (file.exists())
		{

			if (file.canRead())
			{
				return SDM_RET_OK;
			}
			else
			{
				return SDM_RET_FAILED;
			}
		}
		else
		{
			return SDM_RET_FAILED;
		}
	}

	public long FileGetSize(String path)
	{
		DataInputStream ObjIn = null;
		long len = 0;
		try
		{
			obj = null;
			ObjIn = new DataInputStream(new FileInputStream(path));
			len = ObjIn.available();
		}
		catch (StreamCorruptedException e)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
		}
		catch (FileNotFoundException e)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
		}
		catch (IOException e)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
		}
		finally
		{
			try
			{
				if (ObjIn != null)
				{
					ObjIn.close();
				}
			}
			catch (IOException e)
			{
				tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
			}
		}
		return len;
	}

	public boolean FileRead(String path, byte[] buffer, int offset, int length)
	{
		int ret = 0;
		DataInputStream ObjIn = null;
		if (length <= 0)
			return false;
		try
		{
			obj = null;
			ObjIn = new DataInputStream(new FileInputStream(path));
			ret = ObjIn.read(buffer, offset, length);
		}
		catch (StreamCorruptedException e)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
		}
		catch (FileNotFoundException e)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
		}
		catch (IOException e)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
		}
		finally
		{
			try
			{
				if (ObjIn != null)
				{
					ObjIn.close();
				}
			}
			catch (IOException e)
			{
				tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
			}
		}

		if (ret != SDM_RET_FAILED)
			return true;
		else
			return false;
	}

	public boolean FileWrite(String path, int nSize, Object data)
	{
		byte[] tmp = (byte[]) data;
		FileOutputStream stream = null;
		DataOutputStream ObjOut = null;

		try
		{
			//ObjOut = new DataOutputStream(new FileOutputStream(path));
			stream = new FileOutputStream(path);
			ObjOut = new DataOutputStream(stream);
			ObjOut.write(tmp, 0, nSize);
			stream.getFD().sync();
		}
		catch (StreamCorruptedException e)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
			return false;
		}
		catch (FileNotFoundException e)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
			return false;
		}
		catch (IOException e)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
			return false;
		}
		finally
		{
			try
			{
				if (ObjOut != null)
				{
					ObjOut.close();
				}
			}
			catch (IOException e)
			{
				tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
			}
		}
		tmp = null;
		return true;
	}

	public boolean FileWrite(String path, Object data)
	{
		FileOutputStream stream = null;
		ObjectOutputStream ObjOut = null;
		try
		{
			//ObjOut = new ObjectOutputStream(new FileOutputStream(path));
			stream = new FileOutputStream(path);
			ObjOut = new ObjectOutputStream(stream);
			ObjOut.reset();
			ObjOut.writeObject(data);
			stream.getFD().sync();
		}
		catch (StreamCorruptedException e)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
			return false;
		}
		catch (FileNotFoundException e)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
			return false;
		}
		catch (IOException e)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
			return false;
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
			return false;
		}
		finally
		{
			try
			{
				if (ObjOut != null)
				{
					ObjOut.close();
				}
			}
			catch (IOException e)
			{
				tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
			}
		}

		return true;
	}

	public boolean FileCreateWrite(String path, byte[] data)
	{
		FileOutputStream stream = null;
		DataOutputStream ObjOut = null;
		try
		{
			//ObjOut = new DataOutputStream(new FileOutputStream(path));
			stream = new FileOutputStream(path);
			ObjOut = new DataOutputStream(stream);
			ObjOut.write(data);
			stream.getFD().sync();
		}
		catch (StreamCorruptedException e)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
			return false;
		}
		catch (FileNotFoundException e)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
			return false;
		}
		catch (IOException e)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
			return false;
		}
		finally
		{
			try
			{
				if (ObjOut != null)
				{
					ObjOut.close();
				}
			}
			catch (IOException e)
			{
				tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
			}
		}

		return true;
	}

	public boolean FileDelete(String path)
	{
		try
		{
			File file = new File(path);
			if (file.exists())
				file.delete();
		}
		catch (Exception ex)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, ex.toString());

			return false;
		}

		return true;

	}

	public int FileRemove(String path, int fileID)
	{
		try
		{
			File file = new File(path);
			file.delete();
		}
		catch (Exception ex)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, ex.toString());

			return SDM_RET_FAILED;
		}

		return SDM_RET_OK;
	}

	public static int FileRename(String srcPath, String destPath)
	{
		if (srcPath == null || destPath == null)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, "file path file");
			return SDM_RET_FAILED;
		}

		try
		{
			File srcFile = new File(srcPath);

			if (srcFile.exists())
			{
				if (srcFile.canRead())
				{
					try
					{
						File destFile = new File(destPath);
						srcFile.renameTo(destFile);
					}
					catch (Exception e)
					{
						tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
						return SDM_RET_FAILED;
					}
				}
			}
		}
		catch (Exception ex)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, ex.toString());

			return SDM_RET_FAILED;
		}

		return SDM_RET_OK;
	}
}
