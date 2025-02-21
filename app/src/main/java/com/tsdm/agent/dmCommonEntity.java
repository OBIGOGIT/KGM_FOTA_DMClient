package com.tsdm.agent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;

import android.content.Context;
import android.content.res.Resources;

import com.tsdm.tsService;
import com.tsdm.R;
import com.tsdm.db.tsdmDB;
import com.tsdm.adapt.tsDefineIdle;
import com.tsdm.adapt.tsMsgEvent;
import com.tsdm.adapt.tsLib;
import com.tsdm.adapt.tsDmMsg;
import com.tsdm.net.netHttpAdapter;

public class dmCommonEntity implements dmDefineUIEvent, dmDefineDevInfo, tsDefineIdle, dmDefineMsg
{
	
	public static boolean checkEngineInitialized()
	{
		boolean nStatus = true;

		if (!dmTask.g_IsDMInitialized)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, "Engine Not Initialized");
			nStatus = false;
		}
		
		return nStatus;
	}
	
	public static boolean startSession()
	{
		int nStatus;

		nStatus = dmInitadapter.dmInitAdpCheckNetworkReady(SYNCMLDM);
		if (nStatus != NETWORK_STATE_NOT_USE)
		{
			switch (nStatus)
			{
				case NETWORK_STATE_SYNCML_USE:
					tsMsgEvent.SetMsgEvent(null, DM_EVENT_UI_IN_SYNC);
					break;

				case NETWORK_STATE_ALREADY_DOWNLOAD:
					tsMsgEvent.SetMsgEvent(null, DL_EVENT_UI_UPDATE_START);
					break;

				case NETWORK_STATE_FDN_ENABLE:
					tsMsgEvent.SetMsgEvent(null, DM_EVENT_UI_FDN_ENABLE);
					break;

				default:
					break;
			}
			tsLib.debugPrint(DEBUG_DM, "return false");
			return false;
		}

		tsDmMsg.taskSendMessage(TASK_MSG_DM_SYNCML_CONNECT, null, null);
		return true;
	}

	public static Boolean fileExists(String filePath)
	{
		File file = new File(filePath);
		
		if (file.exists() == false)
		{
			return false;
		}
		
		return true;
	}
	
	public static byte[] fileRead(String filePath) {
		FileInputStream fis = null;
		File file = new File(filePath);

		if (file.exists() == false)
		{
			return null;
		}

		try {
			fis = new FileInputStream(file);
			byte[] bytedata = new byte[fis.available()];

			while(fis.read(bytedata) != -1)
			{
				;
			}
			fis.close();

			return bytedata;
		} catch (IOException e) {
			tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
		}finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					tsLib.debugPrintException(DEBUG_DM, e.toString());
				}
			}
		}

		return null;
	}
	
	public static void fileWrite(String dirPath, String fileName, byte[] byteData)
	{
		File cacheDirectory = new File(dirPath);
		if (cacheDirectory.exists() == false)
		{
			cacheDirectory.mkdirs();
		}
		
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(dirPath + "/" + fileName);
		} catch (FileNotFoundException e) {
			tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
		}
		
		if (fos != null)
		{
			try {
				fos.write(byteData);
			} catch (IOException e) {
				tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
			}
		}
		
		if (fos != null)
		{
			try {
				fos.close();
			} catch (IOException e) {
				tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
			}
		}
	}

	public static String getFfsRootPath()
	{
		String path = "";
		
		path = tsdmDB.DM_FS_FFS_DIRECTORY;
		return path;
	}
	public static void fileMove(File file, File dir){
		File newFile = new File(dir, file.getName());
		FileChannel outputChannel = null;
		FileChannel inputChannel = null;
		try {
			try {
				outputChannel = new FileOutputStream(newFile).getChannel();
			} catch (FileNotFoundException e) {
				tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
			}finally {
				;
			}
			try {
				inputChannel = new FileInputStream(file).getChannel();
			} catch (FileNotFoundException e) {
				tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
			}finally {
				;
			}

			inputChannel.transferTo(0, inputChannel.size(), outputChannel);
			inputChannel.close();

			if (!file.delete()) {
				tsLib.debugPrintException(DEBUG_EXCEPTION, "file delete fail");
			}
			inputChannel.close();
			outputChannel.close();
		} catch (IOException e) {
      		tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
        } finally {
			if (inputChannel != null) {
				try {
					inputChannel.close();
				} catch (IOException e) {
					tsLib.debugPrintException(DEBUG_DM, e.toString());
				}
			}
			if (outputChannel != null) {
				try {
					outputChannel.close();
				} catch (IOException e) {
					tsLib.debugPrintException(DEBUG_DM, e.toString());
				}
			}
		}

	}
	public static File rawToFileOnFfs(InputStream rawData, String fileName)
	{
		FileOutputStream fout = null;
		File rootPath = null;
		byte[] buf = null;
		int cntRead = 0;
		
		rootPath = new File(dmCommonEntity.getFfsRootPath());
		if (!rootPath.exists())
			rootPath.mkdir();
		
		buf = new byte[1024 + 1];
		try {
			fout = new FileOutputStream(new File(rootPath + "/" + fileName));
			while (0 < (cntRead = rawData.read(buf)))
				fout.write(buf, 0, cntRead);
		} catch (FileNotFoundException e1) {
			tsLib.debugPrintException(DEBUG_EXCEPTION, "FileNotFoundException "+e1.toString());
			return null;
		} catch (IOException e) {
			tsLib.debugPrintException(DEBUG_EXCEPTION, "IOException"+ e.toString());
			return null;
		} finally {
			try {
				if (fout != null)
					fout.close();
			} catch (Exception e) {
				fout = null;
			}
			try {
				if (rawData != null)
					rawData.close();
			} catch (Exception e) {
				rawData = null;
			}
		}
		
		return new File(rootPath + "/" + fileName);
	}

	public static void createConfigXmlFromResource(Context cnt)
	{
		String rootPath = dmCommonEntity.getFfsRootPath();
		Resources resource = cnt.getResources();
		InputStream finp = null;

		if(dmCommonEntity.fileExists(rootPath + "/" + "tsDmConfig.xml") == false)
		{
			tsLib.debugPrint(DEBUG_UM,"");
			finp = resource.openRawResource(R.raw.dm_config);
			if(dmCommonEntity.rawToFileOnFfs(finp, "tsDmConfig.xml") == null) {
				tsLib.debugPrintException(DEBUG_EXCEPTION, "createConfigXmlFromResource Fail");
			}
		}
	}

	public static void logFileWrite(String dirPath, String fileName, byte[] byteData)
	{
		File cacheDirectory = new File(dirPath);
		if (cacheDirectory.exists() == false)
		{
			cacheDirectory.mkdirs();
		}

		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(dirPath + "/" + fileName, true);
		} catch (FileNotFoundException e) {
			tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
		}

		if (fos != null)
		{
			try {
				fos.write(byteData);
			} catch (IOException e) {
				tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
			}
		}

		if (fos != null)
		{
			try {
				fos.close();
			} catch (IOException e) {
				tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
			}
		}
	}
}
