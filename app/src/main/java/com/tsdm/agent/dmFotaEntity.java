package com.tsdm.agent;

import java.math.BigDecimal;
import java.util.GregorianCalendar;

import android.text.format.Time;

import com.tsdm.tsService;
import com.tsdm.db.tsDBFumoInfo;
import com.tsdm.db.tsDefineDB;
import com.tsdm.db.tsdmDB;
import com.tsdm.adapt.tsDefineIdle;
import com.tsdm.adapt.tsLib;
import com.tsdm.adapt.tsDmMsg;
import com.tsdm.net.netHttpAdapter;

public class dmFotaEntity implements dmDefineDevInfo, tsDefineIdle, dmDefineUIEvent, dmDefineMsg, tsDefineDB {
	private static tsDBFumoInfo mResultData;
	private static long mDeltaPackageTotalSize = 0;
	private static long mDeltaDownloadSize = 0;
	private static long mDeltaDownloadCheckSize = 0;
	private static String szDownloadPercent = "0%";
	private static String szDownloadSize = "";
	private static String szDownloadTime = " ";
	private static String szInvalidatePercent = "0%";
	private static int nInvalidateSize = 0;
	private static int nDownloadCount = 0;
	private static long nBeforeDownSize = 0;
	private static long lBeforeDownTime = 0;
	private static long nOneTermDownSize = 0;
	private static long lOneTermDownTime = 0;
	private static final int MEGA_BYTE_SIZE = 1024 * 1024;
	//private static final int		KILO_BYTE_SIZE			= 1024;

	public static int startSession() {
		int nStatus = 0;

		if (!tsService.isNetworkConnect()) {
			tsLib.debugPrintException(DEBUG_EXCEPTION, "Network disable");
			return -1;
		}

		if ((dmAgent.dmAgentGetSyncMode() > DM_SYNC_NONE) || (netHttpAdapter.getIsConnected() == true)) {
			tsLib.debugPrintException(DEBUG_EXCEPTION, "Already Connecting");
			return -1;
		}
		if (dmCommonEntity.startSession()) {
			tsLib.debugPrint(DEBUG_DM, "");
			dlAgent.dlAgentSetClientInitFlag(DM_USER_INIT);
			dmAgent.dmAgentSetUserInitiatedStatus(true);
		}
		return nStatus;
	}

	public static void checkDownloadMemory() {
		tsLib.debugPrint(DEBUG_UM, "");
		dlFota.dlDownloadMemoryCheck();
	}

	public static void cancelDownload() {
		tsLib.debugPrint(DEBUG_UM, "");
		tsDmMsg.taskSendMessage(TASK_MSG_DL_USER_CANCEL_DOWNLOAD, null, null);
		dmAgent.dmAgentSetUserInitiatedStatus(false);
	}

	public static void downloadFileFail() {
		tsLib.debugPrint(DEBUG_UM, "");
		tsDmMsg.taskSendMessage(TASK_MSG_DL_DOWNLOAD_FILE_ERROR, null, null);
		dmAgent.dmAgentSetUserInitiatedStatus(false);
	}

	public static void updateStandby() {
		tsLib.debugPrint(DEBUG_UM, "");
		tsDmMsg.taskSendMessage(TASK_MSG_DL_FIRMWARE_UPDATE_STANDBY, null, null);
		dmAgent.dmAgentSetUserInitiatedStatus(false);
	}

	public static void updateStart() {
		tsLib.debugPrint(DEBUG_UM, "updateType= "+tsService.updateType);
		tsDmMsg.taskSendMessage(TASK_MSG_DL_FIRMWARE_UPDATE_START, null, null);
		dmAgent.dmAgentSetUserInitiatedStatus(false);
	}

	public static void updateSuccess() {
		tsLib.debugPrint(DEBUG_UM, "updateType= "+tsService.updateType);
		tsDmMsg.taskSendMessage(TASK_MSG_DL_FIRMWARE_UPDATE_SUCCESS, null, null);
		dmAgent.dmAgentSetUserInitiatedStatus(false);
	}

	public static void updateFail() {
		tsLib.debugPrint(DEBUG_UM, "updateType= "+tsService.updateType+" uploadFailCause= "+tsService.uploadFailCause);
		tsDmMsg.taskSendMessage(TASK_MSG_DL_FIRMWARE_UPDATE_FAIL, null, null);
		dmAgent.dmAgentSetUserInitiatedStatus(false);
	}

	public static void updateUserCancel() {
		tsLib.debugPrint(DEBUG_UM, "updateType= "+tsService.updateType);
		tsDmMsg.taskSendMessage(TASK_MSG_DL_FIRMWARE_UPDATE_USERCANCEL, null, null);
		dmAgent.dmAgentSetUserInitiatedStatus(false);
	}

	public static int getDownloadPercent() {
	/*	int nFileId;
		long nDownloadSize = 0;
		int nPercentage = 0;
		int nAgentType = SYNCML_DM_AGENT_DM;

		nFileId = tsdmDB.dmdbGetFileIdFirmwareData();
		nDownloadSize = tsdmDB.dmdbGetFileSize(nFileId);

		if (mDeltaPackageTotalSize != 0 && nDownloadSize != 0) {
			nPercentage = (int) ((nDownloadSize * 100) / mDeltaPackageTotalSize);
		}
		tsLib.debugPrint(DEBUG_DM, "Percentate [" + nPercentage + "%]");

		//percent
		if (nPercentage > 0) {
			szDownloadPercent = String.valueOf(nPercentage);
			szDownloadPercent = szDownloadPercent.concat("%");
		}

		//size
		if (nDownloadSize > 0 && mDeltaPackageTotalSize > 0) {
			BigDecimal bd_recv = new BigDecimal(nDownloadSize / (double) MEGA_BYTE_SIZE);
			bd_recv = bd_recv.setScale(2, BigDecimal.ROUND_UP);
			String szDownloadRecvSize = String.format("%s", bd_recv.toString());
			tsLib.debugPrint(DEBUG_DM, "szDownloadRecvSize [" + szDownloadRecvSize + "]");

			BigDecimal bd_total = new BigDecimal(mDeltaPackageTotalSize / (double) MEGA_BYTE_SIZE);
			bd_total = bd_total.setScale(2, BigDecimal.ROUND_UP);
			String szDownloadTotalSize = String.format("%s", bd_total.toString());

			szDownloadSize = szDownloadRecvSize;
			szDownloadSize = szDownloadSize.concat("MB");
			szDownloadSize = szDownloadSize.concat("/");
			szDownloadSize = szDownloadSize.concat(szDownloadTotalSize);
			szDownloadSize = szDownloadSize.concat("MB");
		}
		setDownloadTime(nDownloadSize);
		mDeltaDownloadSize = nDownloadSize;
		return nPercentage;
		*/
		return 0;
	}

	private static void setDownloadTime(long nDownloadSize) {
		final int DOWNLOADTIME_CHECK_START_COUNT = 6;
		final int DOWNLOADTIME_CHECK_END_COUNT = 20;
		final int DOWNLOADTIME_CHECK_RESET_COUNT = 60;
		final int TIME_TENSEC = 10;
		final int TIME_ONEMIN = 60;

		long lDownTime = 0;
		long nDownRemainData = 0;
		int nDownTermCount = 0;
		int nDownTimeSec = 0;
		int nDownTimeMin = 0;

		GregorianCalendar calendar = new GregorianCalendar();//current time
		nDownloadCount++;

		if (nDownloadCount == DOWNLOADTIME_CHECK_START_COUNT) {
			lBeforeDownTime = calendar.getTimeInMillis();
			nBeforeDownSize = nDownloadSize;
		} else if (nDownloadCount == DOWNLOADTIME_CHECK_END_COUNT) {
			lOneTermDownTime = calendar.getTimeInMillis() - lBeforeDownTime;
			nOneTermDownSize = nDownloadSize - nBeforeDownSize;
		} else if (nDownloadCount > DOWNLOADTIME_CHECK_END_COUNT) {
			String szDownTimeMin;
			String szDownTimeSec;
			nDownRemainData = mDeltaPackageTotalSize - nDownloadSize;
			nDownTermCount = (int) (nDownRemainData / nOneTermDownSize);
			lDownTime = (nDownTermCount * lOneTermDownTime) / 1000;
			nDownTimeMin = (int) (lDownTime / TIME_ONEMIN);
			nDownTimeSec = (int) (lDownTime % TIME_ONEMIN);
			if (nDownTimeMin < TIME_TENSEC)
				szDownTimeMin = "0" + String.valueOf(nDownTimeMin);
			else
				szDownTimeMin = String.valueOf(nDownTimeMin);
			if (nDownTimeSec < TIME_TENSEC)
				szDownTimeSec = "0" + String.valueOf(nDownTimeSec);
			else
				szDownTimeSec = String.valueOf(nDownTimeSec);

			szDownloadTime = szDownTimeMin + ":" + szDownTimeSec;
			tsLib.debugPrint(DEBUG_DM, "nDownRemainData [" + nDownRemainData + "]");
		} else if (nDownloadCount > DOWNLOADTIME_CHECK_RESET_COUNT) {
			nDownloadCount = 0;
		}

		tsLib.debugPrint(DEBUG_DM, "DownloadTime [" + szDownloadTime + "]");
	}
}
