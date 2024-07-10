package com.tsdm.agent;

import android.content.res.Configuration;
import android.os.Environment;
import android.os.StatFs;

import java.io.File;

import com.tsdm.adapt.tsLib;
import com.tsdm.net.netDefine;
import com.tsdm.tsService;

public class dmDevinfoAdapter implements dmDefineDevInfo
{
	static String DEVICEID = "";
	public static void devAdpSetDeviceId(String DeviceId)
	{
		DEVICEID = DeviceId;
	}
	public static String devAdpGetDeviceId()
	{
		return DEVICEID;
	}
	
	static String ManuFact = "";
	public static void devAdpSetManufacturer(String ManuFactStrc)
	{
		ManuFact = ManuFactStrc;
	}
	public static String devAdpGetManufacturer()
	{
		return ManuFact;
	}
	static String ModelName = "";
	public static void devAdpSetModelName(String ModelNameStr)
	{
		ModelName = ModelNameStr;
	}
	public static String devAdpGetModelName()
	{
		return ModelName;
	}
	static String SwVersion = "";
	public static void devAdpSetSoftwareVersion(String SwVerStr)
	{
		SwVersion = SwVerStr;
	}
	public static String devAdpGetSoftwareVersion()
	{
		return SwVersion;
	}


	public static String getLanguage()
	{
		final Configuration configuration = tsService.getContext().getResources().getConfiguration();
		final String language = configuration.locale.getLanguage();
		final String country = configuration.locale.getCountry();
		String loc = String.format("%s-%s", language, country);
		tsLib.debugPrint(DEBUG_DM, "language = " + loc);
		return loc;
	}
	public static String devAdpGetLanguageSetting()
	{
		String str = null;
		str = getLanguage();
		if (tsLib.isEmpty(str))
			str = "en-us";

		return str;
	}

	public static String devGetOEMName()
	{
		String str = "oemName";
		return str;
	}

	public static String devAdpGetFirmwareVersion()
	{
		String str = "firwareVer";
		return str;
	}
	public static String devAdpGetHardwareVersion()
	{
		String str = "hardwareVer";
		return str;
	}
	
	public static String devAdpGetHttpUserAgent()
	{
		String pUserAgent = null;
		String pManufacturer = null;
		String pModelName = null;

		pManufacturer = devAdpGetManufacturer();
		pModelName = devAdpGetModelName();

		if (tsLib.isEmpty(pManufacturer))
		{
			pManufacturer = "";
		}

		if (tsLib.isEmpty(pModelName))
		{
			pModelName = "";
		}

		pUserAgent = pManufacturer + " " + pModelName + " " + netDefine.HTTP_DM_USER_AGENT;
		return pUserAgent;
	}


	public static long getAvailableInternalMemorySize()
	{
		File path = Environment.getDataDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long availableBlocks = stat.getAvailableBlocks();
		return availableBlocks * blockSize;
	}

	public static long getTotalInternalMemorySize()
	{
		File path = Environment.getDataDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long totalBlocks = stat.getBlockCount();
		return totalBlocks * blockSize;
	}

	public static boolean checkExternalSdMemoryAvailable()
	{
		boolean bret = false;
		try
		{
			tsLib.debugPrint(DEBUG_DM, "checkExternalSdMemoryAvailable() = " + String.valueOf(Environment.getExternalStorageState()));
			bret = Environment. getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
		}
		catch (Exception e)
		{
			tsLib.debugPrint(DEBUG_EXCEPTION, "checkExternalSdMemoryAvailable() = " + e.toString());
		}
		return bret;
	}

	public static boolean checkExternalMemoryAvailable()
	{
		boolean bret = false;
		try
		{
			tsLib.debugPrint(DEBUG_DM, "checkExternalMemoryAvailable() = " + String.valueOf(Environment.getExternalStorageState()));
			bret = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
		}
		catch (Exception e)
		{
			tsLib.debugPrint(DEBUG_EXCEPTION, "checkExternalMemoryAvailable() = " + e.toString());
		}
		return bret;
	}

	public static long getAvailableExternalSdMemorySize()
	{
		String path = Environment.getExternalStorageDirectory().getAbsolutePath();
		StatFs stat = new StatFs(path);
		long blockSize = stat.getBlockSize();
		long availableBlocks = stat.getAvailableBlocks();
		return availableBlocks * blockSize;
	}

	public static long getTotalExternalSdMemorySize()
	{
		String path = Environment.getExternalStorageDirectory().getAbsolutePath();
		StatFs stat = new StatFs(path);
		long blockSize = stat.getBlockSize();
		long totalBlocks = stat.getBlockCount();
		return totalBlocks * blockSize;
	}

	public static long getAvailableExternalMemorySize()
	{
		File path = Environment.getExternalStorageDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long availableBlocks = stat.getAvailableBlocks();
		return availableBlocks * blockSize;
	}

	public static long getTotalExternalMemorySize()
	{
		File path = Environment.getExternalStorageDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long totalBlocks = stat.getBlockCount();
		return totalBlocks * blockSize;
	}


	public static boolean checkCacheMemoryAvailable(int nPkgSize)
	{
		StatFs stat = null;
		int blockSize = 0;
		int availableBlocks = 0;
		long freeSpace = 0;
		boolean bRtn = false;
		File downloadCache = Environment.getDownloadCacheDirectory(); // /cache

		stat = new StatFs(downloadCache.getPath());
		blockSize = stat.getBlockSize();
		availableBlocks = stat.getAvailableBlocks();
		freeSpace = blockSize * ((long) availableBlocks - 4);

		tsLib.debugPrint(DEBUG_DM, "freeSpace = " + freeSpace + ", nPkgSize = " + nPkgSize);

		if (freeSpace >= nPkgSize)
		{
			bRtn = true;
		}
		return bRtn;
	}
}
