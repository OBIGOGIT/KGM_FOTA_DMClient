package com.tsdm;

import static com.tsdm.db.tsdmDB.DM_FS_FFS_DIRECTORY;
import static com.tsdm.db.tsdmDB.dbadapter;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import androidx.core.app.NotificationCompat;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Binder;
import android.os.IBinder;

import com.tsdm.agent.dmTask;
import com.tsdm.agent.dmUITask;
import com.tsdm.agent.dmDefineDevInfo;
import com.tsdm.agent.dmDevinfoAdapter;
import com.tsdm.db.tsDefineDB;
import com.tsdm.db.tsdmDB;
import com.tsdm.db.tsdmDBadapter;
import com.tsdm.db.tsdmDBsql;
import com.tsdm.adapt.tsDefineIdle;
import com.tsdm.adapt.tsLib;
import com.tsdm.adapt.tsDmMsg;
import com.tsdm.agent.dmDefineMsg;
import com.tsdm.agent.dmDefineUIEvent;
import com.tsdm.agent.dmCommonEntity;
import com.tsdm.agent.dmFotaEntity;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Objects;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.mengbo.service.avnlib.obigo.data.CcuInfo;
import com.ematsoft.common.def.APP_ID;
import com.ematsoft.emthirdpartylib.EMThirdPartyLib;
import com.ematsoft.emthirdpartylib.EM3LibNetwork;
import com.ematsoft.emthirdpartylib.EM3LibCommon;

import org.json.JSONException;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class tsService extends Service implements dmDefineMsg, dmDefineUIEvent, dmDefineDevInfo, tsDefineIdle, tsDefineDB
{
	public static dmTask    Task							= null;
	public static dmUITask  UITask							= null;
	Binder					binder							= new Binder();
	public static Context	mContext;


	//DM-UM State Define
	static int 				DMState 						= 0;
	public static int		DM_IDLE		                    = 0; // IDLE 상태
	public static int 		DM_PROGRESS 					= 1; // FOTA DM PROGRESS중
	public static int 		DL_PROGRESS 					= 2; // FOTA DL PROGRESS중

	//DM Use APN
	public static boolean 		apn3Enable 					= false;
	public static boolean 		isApnChanged 				= false;
	public static String        APN3_NAME_STRING            = "m2m-sym-tele2.lguplus.co.kr";


	//DM Log Upload Define
	public static boolean 		logUploadSkip = false;
	public static String        DM_CLIENT_LOG_FILE          = "dmClient.log";



	//Intent Action
	public static String		DM2UM_INTENT_NAME	        			= "com.dmclient.intent.dm2um";
	public static String		UM2DM_INTENT_NAME	       				= "com.ematsoft.intent.um2dm";

	public static String		INTENT_MSGTYPE 							= "msgType";

	//UM-->DM
	public static String 		INTENT_MSGTYPE_UPDATE_CHECK 			= "updateCheck";
	public static String 		INTENT_MSGTYPE_DOWNLOAD_CONFIRM_RES 	= "downloadConfirmRes";
	public static String 		INTENT_MSGTYPE_UPDATE_STANDBY 			= "updateStandby";
	public static String 		INTENT_MSGTYPE_UPDATE_REPORT 			= "updateReport";
	public static String 		INTENT_MSGTYPE_USB_UPDATE_START 		= "usbUpdateStart";
	public static String 		INTENT_MSGTYPE_USB_UPDATE_REPORT 		= "usbUpdateReport";

	//DM-->UM
	public static String 		INTENT_MSGTYPE_DOWNLOAD_CONFIRM_REQ 	= "downloadConfirmReq";
	public static String 		INTENT_MSGTYPE_RESUME_DOWNLOAD 			= "downloadResume";
	public static String		INTENT_MSGTYPE_DOWNLOAD_FAIL			= "downloadFail";
	public static String 		INTENT_MSGTYPE_UPDATE_EXCLUSION 		= "updateExclusion";

	public static String 		INTENT_MSGTYPE_NETWORK_UNREADY 		    = "networkUnready";
	public static String 		INTENT_MSGTYPE_DOWNLOAD_COMPLEATE       = "downloadCompleate";

	//Intent data
	public static String 		INTENT_MODEL_NAME 			= "modelName";
	public static String		INTENT_VIN_ID			    = "vinID";
	public static String		INTENT_OPERATION_ID		    = "operationId";
	public static String		INTENT_LOG_ONOFF			= "logOnOff";
	public static String		INTENT_LOG_MAXSIZE		    = "logMaxSize";
	public static String		INTENT_AVNT_VER		        = "avntVer";
	public static String		INTENT_SOC_VER		        = "socVer";
	public static String		INTENT_CCU_VER		        = "ccuVer";
	public static String		INTENT_MCU_VER		        = "mcuVer";
	public static String		INTENT_PAS_VER		        = "pasVer";
	public static String		INTENT_SOC_FILEPATH	        = "socFilePath";
	public static String		INTENT_CCU_FILEPATH	        = "ccuFilePath";
	public static String		INTENT_MCU_FILEPATH	        = "mcuFilePath";
	public static String		INTENT_PAS_FILEPATH	        = "pasFilePath";
	public static String		INTENT_CONFIRM_RES	        = "confirmResult";
	public static String		INTENT_UPDATE_TYPE	        = "updateType";
	public static String		INTENT_UPDATE_STATE	        = "updateState";

	public static String		INTENT_UPDATE_FAILINDEX	    = "failIndex";
	public static String		INTENT_UPDATE_FAILCASUSE	= "failCause";
	public static String		INTENT_UPDATE_CONTENT		= "updateContent";


	
	public static final String	MANUFACTURE = "KGMOBILITY";
	public static String modelName;
	public static String vinId;
	public static int updateState; //0: 시작, 1: 성공, 2: 실패, 3:사용자 취소
	public static int updateType;  //1:PAS, 2:AVNT
	public static int failIndex;   //FAILINDEX
	public static int failCause;
	public static String downloadFileFailCause;
	public static String uploadFailCause;
	public static String operationId;
	public static Boolean logOnOff= false;
	public static int logMaxSize;
	public static String avntVer;
	public static String pasVer;
	public static String socFilePath;
	public static String ccuFilePath;
	public static String mcuFilePath;
	public static String pasFilePath;


	//다운로드 완료된 화일의 존재여부
	public static Boolean isSocFile;
	public static Boolean isCcuFile;
	public static Boolean isMcuFile;
	public static Boolean isPasFile;


    //속도 체크
	static int downloadDataSize =0;
	static Long receiveStartTime = 0L;
	static Long receiveEndTime = 0L;
	static String downloadSpeed;


	public enum FAILINDEX {
		SOC_Android(1,"SOC_Android"),
		SOC_QNX(2,"SOC_QNX"),
		SOC_xbl(3,"SOC_xbl"),
		CCU(4,"CCU"),
		MCU(5, "MCU"),
		PAS(6, "PAS"),
		;
		private final int indexInt;
		private final String indexString;

		FAILINDEX(int indexInt, String indexString) {
			this.indexInt = indexInt;
			this.indexString = indexString;
		}

		public int indexInt() {
			return indexInt;
		}
		public String indexString() {
			return indexString;
		}
	}

	public enum SOCFAILCAUSE {
		SOC_UA_RESULT_UPDATE_ERROR(-1,"SOC_UA_RESULT_UPDATE_ERROR"),
		SOC_UA_RESULT_ORG_CKSUM_ERROR(-2,"SOC_UA_RESULT_ORG_CKSUM_ERROR"),
		SOC_UA_RESULT_UPDATE_CKSUM_ERROR(-3,"SOC_UA_RESULT_UPDATE_CKSUM_ERROR"),
		SOC_UA_RESULT_SIGNATURE_ERROR(-4,"SOC_UA_RESULT_SIGNATURE_ERROR"),
		SOC_UA_RESULT_VERSION_ERROR(-5, "SOC_UA_RESULT_VERSION_ERROR"),
		SOC_UA_RESULT_INIT_ERROR(-6, "SOC_UA_RESULT_INIT_ERROR"),
		SOC_UA_RESULT_CONFIG_ERROR(-7, "SOC_UA_RESULT_CONFIG_ERROR"),
		SOC_UA_RESULT_MEMORY_ERROR(-8, "SOC_UA_RESULT_MEMORY_ERROR"),
		SOC_UA_RESULT_STORAGE_ERROR(-9, "SOC_UA_RESULT_STORAGE_ERROR"),
		SOC_UA_RESULT_PROGRESS_ERROR(-10, "SOC_UA_RESULT_PROGRESS_ERROR"),
		SOC_UA_RESULT_DELTA_CKSUM_ERROR(-11, "SOC_UA_RESULT_DELTA_CKSUM_ERROR"),
		SOC_UA_RESULT_DELTA_ERROR(-12, "SOC_UA_RESULT_DELTA_ERROR"),
		;
		private final int failInt;
		private final String failString;

		SOCFAILCAUSE(int failInt, String failString) {
			this.failInt = failInt;
			this.failString = failString;
		}

		public int failInt() {
			return failInt;
		}
		public String failString() {
			return failString;
		}
	}

	public enum MCUFAILCAUSE {
		MCU_RESULT_FILEREAD_ERROR(1,"MCU_RESULT_FILEREAD_ERROR"),
		MCU_RESULT_PROTOCOL_ERROR(2,"MCU_RESULT_PROTOCOL_ERROR"),
		MCU_RESULT_FILENOTFOUND_ERROR(3,"MCU_RESULT_FILENOTFOUND_ERROR"),
		MCU_RESULT_VERSION_ERROR(4,"MCU_RESULT_VERSION_ERROR");

		private final int failInt;
		private final String failString;

		MCUFAILCAUSE(int failInt, String failString) {
			this.failInt = failInt;
			this.failString = failString;
		}

		public int failInt() {
			return failInt;
		}
		public String failString() {
			return failString;
		}
	}

	public enum CCUFAILCAUSE {
		CCU_RESULT_MD5_ERROR(1,"CCU_RESULT_MD5_ERROR"),
		CCU_RESULT_VERSION_ERROR(2,"CCU_RESULT_VERSION_ERROR"),
		CCU_RESULT_FILENOFOUND_ERROR(3,"CCU_RESULT_FILENOFOUND_ERROR"),
		CCU_RESULT_PAKAGE_ERROR(4,"CCU_RESULT_PAKAGE_ERROR");

		private final int failInt;
		private final String failString;

		CCUFAILCAUSE(int failInt, String failString) {
			this.failInt = failInt;
			this.failString = failString;
		}

		public int failInt() {
			return failInt;
		}
		public String failString() {
			return failString;
		}
	}

	public enum PASFAILCAUSE {
		PAS_RESULT_NOTTESTED_ERROR(1,"PAS_RESULT_NOTTESTED_ERROR"),
		PAS_RESULT_LINFRAME_ERROR(2,"PAS_RESULT_LINFRAME_ERROR"),
		PAS_RESULT_TIMEOUT_ERROR(3,"PAS_RESULT_TIMEOUT_ERROR"),
		PAS_RESULT_LINCRC_ERROR(4,"PAS_RESULT_LINCRC_ERROR"),
		PAS_RESULT_DATACRC_ERROR(5,"PAS_RESULT_DATACRC_ERROR"),
		PAS_RESULT_NOTSUPPORTED_ERROR(12,"PAS_RESULT_NOTSUPPORTED_ERROR"),
		PAS_RESULT_LENGTH_ERROR(13,"PAS_RESULT_LENGTH_ERROR"),
		PAS_RESULT_CONDITION_ERROR(22,"PAS_RESULT_CONDITION_ERROR"),
		PAS_RESULT_SEQUENCE_ERROR(24,"PAS_RESULT_SEQUENCE_ERROR"),
		PAS_RESULT_OUTOFRANGE_ERROR(31,"PAS_RESULT_OUTOFRANGE_ERROR"),
		PAS_RESULT_ACCESS_ERROR(33,"PAS_RESULT_ACCESS_ERROR"),
		PAS_RESULT_PROGRAMMING_ERROR(72,"PAS_RESULT_PROGRAMMING_ERROR");
		private final int failInt;
		private final String failString;

		PASFAILCAUSE(int failInt, String failString) {
			this.failInt = failInt;
			this.failString = failString;
		}

		public int failInt() {
			return failInt;
		}
		public String failString() {
			return failString;
		}
	}

	public static Context getContext()
	{
		return mContext;
	}

	protected void attachBaseContext(Context base)
	{
		super.attachBaseContext(base);
		mContext = base;
	}

	public IBinder onBind(Intent intent)
	{
		tsLib.debugPrint(DEBUG_UM,"");
		return binder;
	}

	@Override
	public void onDestroy() {
		tsLib.debugPrint(DEBUG_UM,"");
		unregisterReceiver(brReciver);
		stopForeground(true);
		super.onDestroy();
	}

	@Override
	public void onCreate() {
		tsLib.debugPrint(DEBUG_UM,"");
		super.onCreate();
		createNotificationChannel();
	}


	public int onStartCommand(Intent intent, int flags, int startId)
	{
		tsLib.debugPrint(DEBUG_UM,"");

		/*PackageInfo pInfo = null;
		try {
			pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		String versionName = pInfo.versionName; //버전명*/

		apnLibInit();

		IntentFilter filter = new IntentFilter();
		filter.addAction(UM2DM_INTENT_NAME);
		registerReceiver(brReciver,filter);

		dmCommonEntity.createConfigXmlFromResource(this);

		Task = new dmTask();

		UITask = new dmUITask();

		tsDmMsg.taskSendMessage(TASK_MSG_OS_INITIALIZED, null, null);

		tsdmDBsql.DBHelper(this);

		return super.onStartCommand(intent, flags, startId);
	}

	public static void setDMState(int state)
	{
		tsLib.debugPrint(DEBUG_UM, "State = "+state);
		DMState = state;
	}

	public static int getDMState()
	{
		tsLib.debugPrint(DEBUG_UM, "State = "+DMState);
		return DMState;
	}

	public void apnLibInit()
	{
		tsLib.debugPrint("DEBUG_UM", "");
		EMThirdPartyLib.main().Init(this, APP_ID.APP.DM_SERVICE);

		EMThirdPartyLib.main().SetListener(new EMThirdPartyLib.EMThirdPartyLibListener() {
			@SuppressLint("SuspiciousIndentation")
			@Override
			public void onConnectService() {
				tsLib.debugPrint(DEBUG_UM, "onConnectService");

				if(apn3Enable &&  !isApnChanged) {
					CcuInfo[] ccuInfos = EMThirdPartyLib.network().getCcuInfo();
					if (ccuInfos != null) {
						tsLib.debugPrint(DEBUG_UM, "ccuInfos length "+ccuInfos.length);
						for (int i = 0; i < ccuInfos.length; i++) {

							CcuInfo info = ccuInfos[i];
							tsLib.debugPrint(DEBUG_UM, "ccuInfos getApn= " + info.getApn());
							tsLib.debugPrint(DEBUG_UM, "ccuInfos getUiccid= " + info.getUiccid());
							tsLib.debugPrint(DEBUG_UM, "ccuInfos getCtn= " + info.getCtn());

							if (info.getApn().equals(APN3_NAME_STRING)) {
								if(changeApn(info.getNetwork()))
									DMStart();
							}
						}
					}
				}
			}
		});

		EMThirdPartyLib.network().SetApnConnectionChangedListener(new EM3LibNetwork.EM3LibNetworkApnConnectionChangedListener() {
			@Override
			public void onApnConnectionChanged(boolean apn1, boolean apn2, boolean apn3) {
				tsLib.debugPrint(DEBUG_UM,  "onApnConnectionChanged: apn1= " + apn1 + ", apn2= " + apn2 + ", apn3= " + apn3);
				apn3Enable =apn3;

				if(apn3Enable)
				{
					if(apn3Enable &&  !isApnChanged) {
						CcuInfo[] ccuInfos = EMThirdPartyLib.network().getCcuInfo();
						if (ccuInfos != null) {
							tsLib.debugPrint(DEBUG_UM, "ccuInfos length "+ccuInfos.length);
							for (int i = 0; i < ccuInfos.length; i++) {

								CcuInfo info = ccuInfos[i];
								tsLib.debugPrint(DEBUG_UM, "ccuInfos getApn= " + info.getApn());
								tsLib.debugPrint(DEBUG_UM, "ccuInfos getUiccid= " + info.getUiccid());
								tsLib.debugPrint(DEBUG_UM, "ccuInfos getCtn= " + info.getCtn());

								if (info.getApn().equals(APN3_NAME_STRING)) {
									if(changeApn(info.getNetwork()))
										DMStart();
								}
							}
						}
					}
				}
			}
		});

/*		EMThirdPartyLib.common().SetListener(new EM3LibCommon.EM3LibCommonListener() {
			@Override
			public void onSplit(int split) {
				tsLib.debugPrint(DEBUG_UM, "onSplit: " + split);
			}
		});*/

		EMThirdPartyLib.common().SetFactoryListener(new EM3LibCommon.EM3LibCommonFactoryListener() {
			@Override
			public void onFactoryReset(int option) {
				tsLib.debugPrint(DEBUG_UM, "onFactoryReset: " + option);
			}
		});
	}

	@SuppressLint("ForegroundServiceType")
	private void createNotificationChannel() {

		tsLib.debugPrint(DEBUG_UM,"");

		String channel_id = "kgmOtaChannel";
		CharSequence name = "kgmOtaService";
		String description = "kgmOtaService";
		int importance = NotificationManager.IMPORTANCE_DEFAULT ;
		NotificationChannel mChannel = new NotificationChannel(channel_id, name, importance);
		mChannel.setDescription(description);
		NotificationManager notificationManager = getSystemService(NotificationManager.class);
		notificationManager.createNotificationChannel(mChannel);


		NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channel_id);
		Notification notification = notificationBuilder.setOngoing(true)
				.setSmallIcon(R.mipmap.ic_launcher_round)
				.setContentTitle("App is running in background")
				.setPriority(NotificationManager.IMPORTANCE_MIN)
				.setCategory(Notification.CATEGORY_SERVICE)
				.build();
		startForeground(2, notification);
	}

	public void DMStart(){

		tsLib.debugPrint(DEBUG_UM, "");

		setDMState(DM_IDLE);

		SharedPreferences spt = mContext.getSharedPreferences("dmClient", MODE_PRIVATE);
		if (dmDevinfoAdapter.devAdpGetModelName().isEmpty()) {
			dmDevinfoAdapter.devAdpSetModelName(spt.getString("modelName", ""));
		}
		if (dmDevinfoAdapter.devAdpGetDeviceId().isEmpty()) {
			dmDevinfoAdapter.devAdpSetDeviceId(spt.getString("vinId", ""));
		}

		int nStatus = tsdmDB.dmdbGetFUMOStatus();
		if (nStatus == DM_FUMO_STATE_UPDATE_IN_PROGRESS || nStatus == DM_FUMO_STATE_UPDATE_SUCCESSFUL_HAVEDATA
				|| nStatus == DM_FUMO_STATE_UPDATE_SUCCESSFUL_NODATA || nStatus == DM_FUMO_STATE_UPDATE_FAILED_NODATA
				|| nStatus == DM_FUMO_STATE_UPDATE_FAILED_HAVEDATA
				|| nStatus == DM_FUMO_STATE_DOWNLOAD_IN_PROGRESS || nStatus == DM_FUMO_STATE_DOWNLOAD_COMPLETE || nStatus == DM_FUMO_STATE_IDLE_START
				|| nStatus == DM_FUMO_STATE_DOWNLOAD_DESCRIPTOR || nStatus == DM_FUMO_STATE_SUSPEND
				|| nStatus == DM_FUMO_STATE_DOWNLOAD_FAILED_REPORTING || nStatus == DM_FUMO_STATE_DOWNLOAD_FAILED
				|| nStatus == DM_FUMO_STATE_DOWNLOAD_IN_CANCEL
				|| nStatus == DM_FUMO_STATE_READY_TO_UPDATE)
		{
			SharedPreferences sp = mContext.getSharedPreferences("dmClient", MODE_PRIVATE);
			SharedPreferences.Editor sprefEditor = sp.edit();
			sprefEditor.apply();
			logOnOff = sp.getBoolean("logOnOff",false);

			tsLib.debugPrint(DEBUG_UM, "FUMO Status is [" + nStatus + "]. Resume");

			if (nStatus == DM_FUMO_STATE_DOWNLOAD_IN_PROGRESS) {
				Intent i = new Intent();
				i.setAction(DM2UM_INTENT_NAME);
				i.putExtra(INTENT_MSGTYPE, INTENT_MSGTYPE_RESUME_DOWNLOAD);
				tsLib.debugPrint(DEBUG_UM, "intent msgType = " + i.getStringExtra(INTENT_MSGTYPE));
				mContext.sendBroadcast(i);
				setDMState(DL_PROGRESS);
				receiveStartTime = System.currentTimeMillis();
			}
			tsDmMsg.taskSendMessage(TASK_MSG_DM_SYNCML_INIT, null, null);
		} else {
			setDMState(DM_IDLE);
		}
	}


	BroadcastReceiver brReciver = new BroadcastReceiver(){
		@SuppressLint("SuspiciousIndentation")
		@Override
		public void onReceive(Context context, Intent intent)
		{

			if(Objects.equals(intent.getAction(), UM2DM_INTENT_NAME)) {

				SharedPreferences sptemp = mContext.getSharedPreferences("dmClient", MODE_PRIVATE);
				logOnOff = sptemp.getBoolean("logOnOff",false);

                if(Objects.equals(intent.getStringExtra(INTENT_MSGTYPE), INTENT_MSGTYPE_UPDATE_CHECK)){

					modelName=intent.getStringExtra(INTENT_MODEL_NAME);
					if(modelName==null)modelName="";
					vinId=intent.getStringExtra(INTENT_VIN_ID);
					if(vinId==null)vinId="";
					operationId=intent.getStringExtra(INTENT_OPERATION_ID);
					logOnOff=intent.getBooleanExtra(INTENT_LOG_ONOFF,false);
					logMaxSize=intent.getIntExtra(INTENT_LOG_MAXSIZE,0);
					avntVer=intent.getStringExtra(INTENT_AVNT_VER);
					pasVer=intent.getStringExtra(INTENT_PAS_VER);
					socFilePath=intent.getStringExtra(INTENT_SOC_FILEPATH);
					ccuFilePath=intent.getStringExtra(INTENT_CCU_FILEPATH);
					mcuFilePath=intent.getStringExtra(INTENT_MCU_FILEPATH);
					pasFilePath=intent.getStringExtra(INTENT_PAS_FILEPATH);

					SharedPreferences sp= getSharedPreferences("dmClient", MODE_PRIVATE);
					SharedPreferences.Editor spedit = sp.edit();
					spedit.putString("modelName",modelName);
					spedit.putString("vinId",vinId);
					spedit.putBoolean("logOnOff",logOnOff);
					spedit.putInt("logMaxSize",logMaxSize);
					spedit.putString("socFilePath",socFilePath);
					spedit.putString("ccuFilePath",ccuFilePath);
					spedit.putString("mcuFilePath",mcuFilePath);
					spedit.putString("pasFilePath",pasFilePath);
					spedit.putString("workId","");
					spedit.apply();

					if(!logOnOff) {
                        dmCommonEntity.fileWrite(tsdmDB.DM_FS_FFS_DIRECTORY, DM_CLIENT_LOG_FILE, "".getBytes());
                    }

					tsLib.debugPrint(DEBUG_UM, "intent update check");
					tsLib.debugPrint(DEBUG_UM, "intent modelName = " + modelName);
					tsLib.debugPrint(DEBUG_UM, "intent vinId = " + vinId);
					tsLib.debugPrint(DEBUG_UM, "intent operationId = " + operationId);
					tsLib.debugPrint(DEBUG_UM, "intent logOnOff = " + logOnOff);
					tsLib.debugPrint(DEBUG_UM, "intent logMaxSize = " + logMaxSize);
					tsLib.debugPrint(DEBUG_UM, "intent avntVer = " + avntVer);
					tsLib.debugPrint(DEBUG_UM, "intent pasVer = " + pasVer);
					tsLib.debugPrint(DEBUG_UM, "intent socFilePath = " + socFilePath);
					tsLib.debugPrint(DEBUG_UM, "intent ccuFilePath = " + ccuFilePath);
					tsLib.debugPrint(DEBUG_UM, "intent mcuFilePath = " + mcuFilePath);
					tsLib.debugPrint(DEBUG_UM, "intent pasFilePath = " + pasFilePath);

					if(modelName.isEmpty() || vinId.isEmpty() ) {
						tsLib.debugPrint(DEBUG_UM, "modelName vinId is Empty");
						return;
					}

					if(getDMState()!= DM_IDLE) {
						tsLib.debugPrint(DEBUG_UM, "DM state Not idle state= "+getDMState());
						return;
					}

					if(avntVer ==null) avntVer="";
					if(pasVer ==null) pasVer="";

					String checkVer= avntVer + ";" + pasVer;
					dmDevinfoAdapter.devAdpSetManufacturer(MANUFACTURE);
					dmDevinfoAdapter.devAdpSetModelName(modelName);
					dmDevinfoAdapter.devAdpSetDeviceId(vinId);
					dmDevinfoAdapter.devAdpSetSoftwareVersion(checkVer);

					downloadFileFailCause="";
					uploadFailCause="";

					setDMState(DM_PROGRESS);
					tsdmDB.dmdbSetFUMOStatus(DM_FUMO_STATE_NONE);
					tsDmMsg.taskSendMessage(TASK_MSG_DM_SYNCML_INIT, null, null);
					dmFotaEntity.startSession();
				}
				else if(Objects.equals(intent.getStringExtra(INTENT_MSGTYPE), INTENT_MSGTYPE_DOWNLOAD_CONFIRM_RES)){

					if(intent.getBooleanExtra(INTENT_CONFIRM_RES, false)) {
						tsLib.debugPrint(DEBUG_UM, "intent confirmResult true");

						if(getDMState()!= DM_IDLE) {
							tsLib.debugPrint(DEBUG_UM, "DM state Not idle state= "+getDMState());
							return;
						}

						if( tsdmDB.dmdbGetFUMOStatus() == DM_FUMO_STATE_DOWNLOAD_DESCRIPTOR)
						{
							setDMState(DL_PROGRESS);
							tsDmMsg.taskSendMessage(TASK_MSG_DM_SYNCML_INIT, null, null);
							dmFotaEntity.checkDownloadMemory();
							receiveStartTime = System.currentTimeMillis();
						}
					}
					else {
						tsLib.debugPrint(DEBUG_UM, "intent confirmResult false");

						if(getDMState()!= DM_IDLE) {
							tsLib.debugPrint(DEBUG_UM, "DM state Not idle state= "+getDMState());
							return;
						}

						if(tsdmDB.dmdbGetFUMOStatus() == DM_FUMO_STATE_DOWNLOAD_DESCRIPTOR)
						{
							setDMState(DM_IDLE);
							tsDmMsg.taskSendMessage(TASK_MSG_DM_SYNCML_INIT, null, null);
							dmFotaEntity.cancelDownload();
						}
					}
				}
				else if(Objects.equals(intent.getStringExtra(INTENT_MSGTYPE), INTENT_MSGTYPE_UPDATE_STANDBY)){
					tsLib.debugPrint(DEBUG_UM, "intent update standby");
					setDMState(DM_IDLE);
					tsDmMsg.taskSendMessage(TASK_MSG_DM_SYNCML_INIT, null, null);
					dmFotaEntity.updateStandby();
				}
				else if(Objects.equals(intent.getStringExtra(INTENT_MSGTYPE), INTENT_MSGTYPE_UPDATE_REPORT)){

					modelName=intent.getStringExtra(INTENT_MODEL_NAME);
					if(modelName==null)modelName="";
					vinId=intent.getStringExtra(INTENT_VIN_ID);
					if(vinId==null)vinId="";
					updateState=intent.getIntExtra(INTENT_UPDATE_STATE,0);
					updateType=intent.getIntExtra(INTENT_UPDATE_TYPE,0);

					failIndex=intent.getIntExtra(INTENT_UPDATE_FAILINDEX,0);
					failCause=intent.getIntExtra(INTENT_UPDATE_FAILCASUSE,0);

					tsLib.debugPrint(DEBUG_UM, "intent update report");
					tsLib.debugPrint(DEBUG_UM, "intent modelName = " + modelName);
					tsLib.debugPrint(DEBUG_UM, "intent vinId = " + vinId);
					tsLib.debugPrint(DEBUG_UM, "intent updateState = " + updateState);
					tsLib.debugPrint(DEBUG_UM, "intent updateType = " + updateType);
					tsLib.debugPrint(DEBUG_UM, "intent failIndex = " + failIndex);
					tsLib.debugPrint(DEBUG_UM, "intent failCause = " + failCause);

					setDMState(DM_PROGRESS);

					tsDmMsg.taskSendMessage(TASK_MSG_DM_SYNCML_INIT, null, null);

					if(updateState == 0){ //update start

						logUploadSkip=true;
						dmFotaEntity.updateStart();

				    }else if(updateState == 1){ //update success

						dmFotaEntity.updateSuccess();

					}else if(updateState == 2){ //update fail

						if(failIndex == FAILINDEX.SOC_Android.indexInt){
							uploadFailCause=FAILINDEX.SOC_Android.indexString;

							for(SOCFAILCAUSE F : SOCFAILCAUSE.values()){
								if(F.failInt == failCause)
									uploadFailCause = uploadFailCause + " " + F.failString;
							}
						}else if(failIndex == FAILINDEX.SOC_Android.indexInt){
							uploadFailCause=FAILINDEX.SOC_QNX.indexString;

							for(SOCFAILCAUSE F : SOCFAILCAUSE.values()){
								if(F.failInt == failCause)
									uploadFailCause = uploadFailCause + " " + F.failString;
							}
						}else if(failIndex == FAILINDEX.SOC_xbl.indexInt){
							uploadFailCause=FAILINDEX.SOC_xbl.indexString;

							for(SOCFAILCAUSE F : SOCFAILCAUSE.values()){
								if(F.failInt == failCause)
									uploadFailCause = uploadFailCause + " " + F.failString;
							}
						}else if(failIndex == FAILINDEX.CCU.indexInt){
							uploadFailCause=FAILINDEX.CCU.indexString;

							for(CCUFAILCAUSE F : CCUFAILCAUSE.values()){
								if(F.failInt == failCause)
									uploadFailCause = uploadFailCause + " " + F.failString;
							}

						}else if(failIndex == FAILINDEX.MCU.indexInt){
							uploadFailCause=FAILINDEX.MCU.indexString;

							for(MCUFAILCAUSE F : MCUFAILCAUSE.values()){
								if(F.failInt == failCause)
									uploadFailCause = uploadFailCause + " " + F.failString;
							}

						}else if(failIndex == FAILINDEX.PAS.indexInt){
							uploadFailCause=FAILINDEX.PAS.indexString;

							for(PASFAILCAUSE F : PASFAILCAUSE.values()){
								if(F.failInt == failCause)
									uploadFailCause = uploadFailCause + " " + F.failString;
							}
						}
						tsLib.debugPrint(DEBUG_UM, "uploadFailCause = "+ uploadFailCause);

						dmFotaEntity.updateFail();

					}else if(updateState == 3){ //update user cancel

						dmFotaEntity.updateUserCancel();

					}
				}
				else if(Objects.equals(intent.getStringExtra(INTENT_MSGTYPE), INTENT_MSGTYPE_USB_UPDATE_START)){
					modelName=intent.getStringExtra(INTENT_MODEL_NAME);
					if(modelName==null)modelName="";
					vinId=intent.getStringExtra(INTENT_VIN_ID);
					if(vinId==null)vinId="";

					tsLib.debugPrint(DEBUG_UM, "intent usb update start");
					tsLib.debugPrint(DEBUG_UM, "intent modelName = " + modelName);
					tsLib.debugPrint(DEBUG_UM, "intent vinId = " + vinId);
				}
				else if(Objects.equals(intent.getStringExtra(INTENT_MSGTYPE), INTENT_MSGTYPE_USB_UPDATE_REPORT)){

					modelName=intent.getStringExtra(INTENT_MODEL_NAME);
					if(modelName==null)modelName="";
					vinId=intent.getStringExtra(INTENT_VIN_ID);
					if(vinId==null)vinId="";
					avntVer=intent.getStringExtra(INTENT_AVNT_VER);
					pasVer=intent.getStringExtra(INTENT_PAS_VER);
					socFilePath=intent.getStringExtra(INTENT_SOC_FILEPATH);
					ccuFilePath=intent.getStringExtra(INTENT_CCU_FILEPATH);
					mcuFilePath=intent.getStringExtra(INTENT_MCU_FILEPATH);
					pasFilePath=intent.getStringExtra(INTENT_PAS_FILEPATH);

					SharedPreferences sp= getSharedPreferences("dmClient", MODE_PRIVATE);
					SharedPreferences.Editor spedit = sp.edit();
					spedit.putString("modelName",modelName);
					spedit.putString("vinId",vinId);
					spedit.putString("socFilePath",socFilePath);
					spedit.putString("ccuFilePath",ccuFilePath);
					spedit.putString("mcuFilePath",mcuFilePath);
					spedit.putString("pasFilePath",pasFilePath);
					spedit.apply();

					tsLib.debugPrint(DEBUG_UM, "intent usb update report");
					tsLib.debugPrint(DEBUG_UM, "intent modelName = " + modelName);
					tsLib.debugPrint(DEBUG_UM, "intent vinId = " + vinId);
					tsLib.debugPrint(DEBUG_UM, "intent avntVer = " + avntVer);
					tsLib.debugPrint(DEBUG_UM, "intent pasVer = " + pasVer);
					tsLib.debugPrint(DEBUG_UM, "intent socFilePath = " + socFilePath);
					tsLib.debugPrint(DEBUG_UM, "intent ccuFilePath = " + ccuFilePath);
					tsLib.debugPrint(DEBUG_UM, "intent mcuFilePath = " + mcuFilePath);
					tsLib.debugPrint(DEBUG_UM, "intent pasFilePath = " + pasFilePath);


					if(avntVer ==null) avntVer="";
					if(pasVer ==null) pasVer="";
					String checkVer= avntVer + ";" + pasVer;
					dmDevinfoAdapter.devAdpSetManufacturer(MANUFACTURE);
					dmDevinfoAdapter.devAdpSetModelName(modelName);
					dmDevinfoAdapter.devAdpSetDeviceId(vinId);
					dmDevinfoAdapter.devAdpSetSoftwareVersion(checkVer);

					setDMState(DM_PROGRESS);

					tsdmDB.dmdbSetFUMOStatus(DM_FUMO_STATE_NONE);
					tsDmMsg.taskSendMessage(TASK_MSG_DM_SYNCML_INIT, null, null);
					dmFotaEntity.startSession();
				}
			}
		}
	};


	public static void tsDownloadFail(int failType)   //(DM-->UM)
	{
		tsLib.debugPrint(DEBUG_UM, "type = " +failType);
		setDMState(DM_IDLE);

		Intent i = new Intent();
		i.setAction(DM2UM_INTENT_NAME);
		i.putExtra(INTENT_MSGTYPE, INTENT_MSGTYPE_DOWNLOAD_FAIL);
		i.putExtra(INTENT_UPDATE_FAILCASUSE, failType);
		tsLib.debugPrint(DEBUG_UM, "intent msgType = " + i.getStringExtra(INTENT_MSGTYPE));
		tsLib.debugPrint(DEBUG_UM, "intent failCause = " + i.getIntExtra(INTENT_UPDATE_FAILCASUSE,0));
		mContext.sendBroadcast(i);
	}

	public static void tsUpdateExtra()   //(DM-->UM)
	{
		tsLib.debugPrint(DEBUG_UM, "");
		setDMState(DM_IDLE);

		Intent i = new Intent();
		i.setAction(DM2UM_INTENT_NAME);
		i.putExtra(INTENT_MSGTYPE, INTENT_MSGTYPE_UPDATE_EXCLUSION);
		tsLib.debugPrint(DEBUG_UM, "intent msgType = " + i.getStringExtra(INTENT_MSGTYPE));
		mContext.sendBroadcast(i);
	}

	public static void tsNetworkUnready()   //(DM-->UM)
	{
		tsLib.debugPrint(DEBUG_UM, "");
		setDMState(DM_IDLE);

		Intent i = new Intent();
		i.setAction(DM2UM_INTENT_NAME);
		i.putExtra(INTENT_MSGTYPE, INTENT_MSGTYPE_NETWORK_UNREADY);
		tsLib.debugPrint(DEBUG_UM, "intent msgType = " + i.getStringExtra(INTENT_MSGTYPE));
		mContext.sendBroadcast(i);
	}

	public static void tsDownloadRequest()   //(DM-->UM)
	{
		tsLib.debugPrint(DEBUG_UM, "");
		setDMState(DM_IDLE);

		SharedPreferences sp= mContext.getSharedPreferences("dmClient", MODE_PRIVATE);
		String updateContent= sp.getString("downloadDescript","");

		Intent i = new Intent();
		i.setAction(DM2UM_INTENT_NAME);
		i.putExtra(INTENT_MSGTYPE, INTENT_MSGTYPE_DOWNLOAD_CONFIRM_REQ);
		i.putExtra(INTENT_UPDATE_CONTENT, updateContent);
		tsLib.debugPrint(DEBUG_UM, "intent msgType = " + i.getStringExtra(INTENT_MSGTYPE));
		tsLib.debugPrint(DEBUG_UM, "intent updateContent = " + i.getStringExtra(INTENT_UPDATE_CONTENT));
		mContext.sendBroadcast(i);
	}

	public static void tsDownloadComplete()   //(DM-->UM)
	{
		tsLib.debugPrint(DEBUG_UM, "");
		setDMState(DM_IDLE);

		SharedPreferences sp= mContext.getSharedPreferences("dmClient", MODE_PRIVATE);
		socFilePath = sp.getString("socFilePath","");
		ccuFilePath = sp.getString("ccuFilePath","");
		mcuFilePath = sp.getString("mcuFilePath","");
		pasFilePath = sp.getString("pasFilePath","");

		try {
		if(downloadFileProcess()) {

			tsdmDB.dmdbSetFUMOStatus(DM_FUMO_STATE_NONE);
			Intent i = new Intent();
			i.setAction(DM2UM_INTENT_NAME);
			i.putExtra(INTENT_MSGTYPE, INTENT_MSGTYPE_DOWNLOAD_COMPLEATE);
			if(isSocFile)
				i.putExtra(INTENT_SOC_FILEPATH, socFilePath);
			if(isCcuFile)
				i.putExtra(INTENT_CCU_FILEPATH, ccuFilePath);
			if(isMcuFile)
				i.putExtra(INTENT_MCU_FILEPATH, mcuFilePath);
			if(isPasFile)
				i.putExtra(INTENT_PAS_FILEPATH, pasFilePath);
			tsLib.debugPrint(DEBUG_UM, "intent msgType = " + i.getStringExtra(INTENT_MSGTYPE));
			tsLib.debugPrint(DEBUG_UM, "intent socFilePath = " + i.getStringExtra(INTENT_SOC_FILEPATH));
			tsLib.debugPrint(DEBUG_UM, "intent ccuFilePath = " + i.getStringExtra(INTENT_CCU_FILEPATH));
			tsLib.debugPrint(DEBUG_UM, "intent mcuFilePath = " + i.getStringExtra(INTENT_MCU_FILEPATH));
			tsLib.debugPrint(DEBUG_UM, "intent pasFilePath = " + i.getStringExtra(INTENT_PAS_FILEPATH));
			mContext.sendBroadcast(i);
		}
		else {
				tsLib.debugPrint(DEBUG_UM, "downloadFileProcess Fail");
				dmFotaEntity.downloadFileFail();
		}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void setDownFileInfo(String workId, String logUploadURI , String downloadDescript, String downloadFileName, String downloadFileCrc, String downloadFileSize)
	{
		tsLib.debugPrint(DEBUG_UM, downloadFileName);

		SharedPreferences sp = mContext.getSharedPreferences("dmClient", MODE_PRIVATE);
		SharedPreferences.Editor sprefEditor = sp.edit();
		sprefEditor.putString("workId",workId);
		sprefEditor.putString("logUploadURI",logUploadURI);
		sprefEditor.putString("downloadDescript",downloadDescript);
		sprefEditor.putString("downloadFileName",downloadFileName);
		sprefEditor.putString("downloadFileCrc",downloadFileCrc);
		sprefEditor.putString("downloadFileSize",downloadFileSize);
		sprefEditor.apply();
	}

	public static boolean downloadFileProcess() throws Exception {
		isSocFile = false;
		isCcuFile = false;
		isMcuFile = false;
		isPasFile = false;

		SharedPreferences sp= mContext.getSharedPreferences("dmClient", MODE_PRIVATE);
		String downloadFileName= DM_FS_FFS_DIRECTORY + File.separator+sp.getString("downloadFileName","");
		String downloadFileCrc= sp.getString("downloadFileCrc","");

		tsLib.debugPrint(DEBUG_UM, "downloadFileName = "+downloadFileName+" downloadFileCrc = "+ downloadFileCrc);

		if(tsdmDBadapter.FileRename(DM_FS_FFS_DIRECTORY + File.separator+"2400258.cfg",downloadFileName) !=SDM_RET_OK)
		{
			downloadFileFailCause="download file error";
			tsLib.debugPrintException(DEBUG_UM, "download file rename fail");
			return false;
		}
		if(downloadFileCrc != null){
			if(getCRC32Checksum(downloadFileName) != Long.parseLong(downloadFileCrc))
			{
				downloadFileFailCause="download file CRC error";
				tsLib.debugPrintException(DEBUG_UM, "download file CRC fail");
				dbadapter.FileDelete(downloadFileName);
				return false;
			}
		}

		if(downloadFileName.toLowerCase().contains("zip")) {
			FileInputStream fis;
			byte[] buffer = new byte[1024];
			try {
				fis = new FileInputStream(downloadFileName);
				ZipInputStream zis = new ZipInputStream(fis);
				ZipEntry ze = null;
				while ((ze = zis.getNextEntry()) != null) {
					String fileName = ze.getName();
					File newFile = new File(DM_FS_FFS_DIRECTORY + File.separator + fileName);
					FileOutputStream fos = new FileOutputStream(newFile);
					int len;
					while ((len = zis.read(buffer)) > 0) {
						fos.write(buffer, 0, len);
					}
					fos.close();
					zis.closeEntry();
					tsLib.debugPrint(DEBUG_UM, "downloadFile unzip FileName = "+newFile.getName());

					if (newFile.getName().toLowerCase().contains("aupdate")){
						avntFileProcess(newFile.getAbsolutePath());
					}

					if (newFile.getName().toLowerCase().contains("pas")) {
						dmCommonEntity.fileMove(newFile, new File(pasFilePath));
						isPasFile = true;
					}

				}
				zis.closeEntry();
				zis.close();
				fis.close();
			} catch (Exception e) {
				tsLib.debugPrintException(DEBUG_UM, e.toString());
				downloadFileFailCause="download file unzip error";
				return false;
			}

			if (!dbadapter.FileDelete(downloadFileName)) {
				tsLib.debugPrintException(DEBUG_UM, "download file delete fail");
				downloadFileFailCause="download file error";
				return false;
			}
		}
		else {
			tsLib.debugPrintException(DEBUG_UM, "download file not zip !!!!!");
			downloadFileFailCause="download file unzip error";
			return false;
		}

        return true;
    }

	public static boolean avntFileProcess(String avntFile) {

		if(avntFile.toLowerCase().contains("zip")) {
			FileInputStream fis;
			byte[] buffer = new byte[1024];
			try {
				fis = new FileInputStream(avntFile);
				ZipInputStream zis = new ZipInputStream(fis);
				ZipEntry ze = null;
				while ((ze = zis.getNextEntry()) != null) {
					String fileName = ze.getName();
					File newFile = new File(DM_FS_FFS_DIRECTORY + File.separator + fileName);
					FileOutputStream fos = new FileOutputStream(newFile);
					int len;
					while ((len = zis.read(buffer)) > 0) {
						fos.write(buffer, 0, len);
					}
					fos.close();
					zis.closeEntry();
					tsLib.debugPrint(DEBUG_UM, "avnt unzip FileName = "+newFile.getName());

					if (newFile.getName().toLowerCase().contains("soc")){
						socFileProcess(newFile.getAbsolutePath());
						isSocFile = true;
					}

					if (newFile.getName().toLowerCase().contains("ccu")){
						dmCommonEntity.fileMove(newFile, new File(ccuFilePath));
						isCcuFile = true;
					}

					if (newFile.getName().toLowerCase().contains("dgu")){
						dmCommonEntity.fileMove(newFile, new File(mcuFilePath));
						isMcuFile = true;
					}
				}
				zis.closeEntry();
				zis.close();
				fis.close();
			} catch (Exception e) {
				tsLib.debugPrintException(DEBUG_UM, e.toString());
				downloadFileFailCause="avnt file unzip error";
				return false;
			}

			if (!dbadapter.FileDelete(avntFile)) {
				tsLib.debugPrintException(DEBUG_UM, "avnt File zip file delete fail");
				downloadFileFailCause="avnt file delete file error";
				return false;
			}
		}
		else {
			tsLib.debugPrintException(DEBUG_UM, "avnt File file not zip !!!!!");
			downloadFileFailCause="avnt file unzip error";
			return false;
		}

		return true;
	}

	public static boolean socFileProcess(String socFile) {

		if(socFile.toLowerCase().contains("zip")) {
			FileInputStream fis;
			byte[] buffer = new byte[1024];
			try {
				fis = new FileInputStream(socFile);
				ZipInputStream zis = new ZipInputStream(fis);
				ZipEntry ze = null;
				while ((ze = zis.getNextEntry()) != null) {
					String fileName = ze.getName();
					File newFile = new File(DM_FS_FFS_DIRECTORY + File.separator + fileName);
					FileOutputStream fos = new FileOutputStream(newFile);
					int len;
					while ((len = zis.read(buffer)) > 0) {
						fos.write(buffer, 0, len);
					}
					fos.close();
				    zis.closeEntry();
					tsLib.debugPrint(DEBUG_UM, "soc unzip FileName = "+newFile.getName());
					dmCommonEntity.fileMove(newFile, new File(socFilePath));

				}
				zis.closeEntry();
				zis.close();
				fis.close();
			} catch (Exception e) {
				tsLib.debugPrintException(DEBUG_UM, e.toString());
				downloadFileFailCause="soc file unzip error";
				return false;
			}

			if (!dbadapter.FileDelete(socFile)) {
				tsLib.debugPrintException(DEBUG_UM, "soc File zip file delete fail");
				downloadFileFailCause="soc file delete file error";
				return false;
			}
		}
		else {
			tsLib.debugPrintException(DEBUG_UM, "soc File file not zip !!!!!");
			downloadFileFailCause="soc file unzip error";
			return false;
		}

		return true;
	}

	public static long getCRC32Checksum(String filePath) throws Exception {
		// CRC
		CRC32 checksum = new CRC32();
		try {
			BufferedInputStream is = new BufferedInputStream(
					new FileInputStream(filePath));
			byte[] bytes = new byte[1024];
			int len = 0;

			while ((len = is.read(bytes)) >= 0) {
				checksum.update(bytes, 0, len);
			}
			is.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return checksum.getValue();
	}

	public static void POSTFunction(String mUrl, String params) {

		try {
			URL url = new URL(mUrl);
			HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

			SSLContext							mSSLContext				= null;
			SSLSocketFactory					mSSLFactory				= null;
			mSSLContext = SSLContext.getInstance("TLS");
			if (mSSLContext != null)
			{
				// here, trust managers is a single trust-all manager
				TrustManager[] trustManagers = new TrustManager[] {new X509TrustManager()
				{
					public X509Certificate[] getAcceptedIssuers()
					{
						return null;
					}

					public void checkClientTrusted(X509Certificate[] certs, String authType) throws CertificateException
					{
					}

					public void checkServerTrusted(X509Certificate[] certs, String authType) throws CertificateException
					{
					}
				}};

				mSSLContext.init(null, trustManagers, new SecureRandom());
				mSSLContext.getServerSessionContext().setSessionTimeout(60000);
				mSSLFactory = mSSLContext.getSocketFactory();
			}
			conn.setSSLSocketFactory(mSSLFactory);
			conn.setReadTimeout(10000);
			conn.setConnectTimeout(15000);
			conn.setRequestMethod("POST");
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setRequestProperty("Accept-Charset", "UTF-8");
			conn.setRequestProperty("Content-Type", "application/json");

			byte[] outputInBytes = params.getBytes(StandardCharsets.UTF_8);
			OutputStream os = conn.getOutputStream();
			os.write(outputInBytes);
			os.close();

			//결과값을 받아온다.
			InputStream is = conn.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String line;
			StringBuffer response = new StringBuffer();
			while ((line = br.readLine()) != null) {
				response.append(line);
				response.append("\\r");
			}
			br.close();

			String res = response.toString();
			res = res.trim();
		} catch (Exception e) {
			tsLib.debugPrintException(DEBUG_UM, ""+e.toString());
		}
	}

	public static void logUpload()
	{
		setDMState(DM_IDLE);
		tsLib.debugPrint(DEBUG_UM, "logOnOff="+logOnOff+" logUploadSkip="+logUploadSkip);
		if(!logUploadSkip){
			if(logOnOff) {
				SharedPreferences sp= mContext.getSharedPreferences("dmClient", MODE_PRIVATE);
				String workId= sp.getString("workId","");
				String logUploadURI= sp.getString("logUploadURI","");
				//tsLib.debugPrint(DEBUG_UM, "workId = "+workId+" logUploadURI = "+ logUploadURI);
				byte[] logData = dmCommonEntity.fileRead(tsdmDB.DM_FS_FFS_DIRECTORY + "/" + DM_CLIENT_LOG_FILE);
				String logDataStr = new String(logData);
				if(!workId.isEmpty() && !logUploadURI.isEmpty())
				{
					JSONObject jsonObject = new JSONObject();
					try {
						jsonObject.put("vin",dmDevinfoAdapter.devAdpGetDeviceId());
						jsonObject.put("workId",workId);
						jsonObject.put("log",logDataStr);
					} catch (JSONException e) {
						tsLib.debugPrintException(DEBUG_UM, ""+e.toString());
					}
				    tsLib.debugPrint(DEBUG_UM, "log upload success data size = " + logData.length);
					//tsLib.debugPrint(DEBUG_UM, "log upload success data data = " + jsonObject.toString());
				    POSTFunction(logUploadURI,  jsonObject.toString());
					dmCommonEntity.fileWrite(tsdmDB.DM_FS_FFS_DIRECTORY, DM_CLIENT_LOG_FILE, "".getBytes()); //clear
				}
			}
		}
		logUploadSkip=false;
	}

	public static void dowloadFileSize(int ContentBytesread)
	{

		downloadDataSize=ContentBytesread;
	}

	public static String getDownoloadSpeed()
	{
		String ret="";
		receiveEndTime = System.currentTimeMillis();
		if(receiveEndTime !=0 && receiveStartTime!=0 && downloadDataSize !=0) {
			tsLib.debugPrint(DEBUG_UM, "speed dowloadData = " + downloadDataSize+"bytes");
			tsLib.debugPrint(DEBUG_UM, "speed downloadTime = " + (receiveEndTime - receiveStartTime)+"ms");

			if(downloadDataSize > 1024*1024) {
				float dowloadData = downloadDataSize / (1024 * 1024);
				float downloadTime = (receiveEndTime - receiveStartTime) / 1000;
				downloadSpeed = String.format("%.2f", dowloadData / downloadTime)+" MB/s";
				tsLib.debugPrint(DEBUG_UM, "speed downloadSpeed = " + downloadSpeed);
			}
			else{
				float dowloadData = downloadDataSize/1024;
				float downloadTime = (receiveEndTime - receiveStartTime) / 1000;
				downloadSpeed = String.format("%.2f", dowloadData / downloadTime)+" KB/s";
				tsLib.debugPrint(DEBUG_UM, "speed downloadSpeed = " + downloadSpeed);
			}
			ret=downloadSpeed;
		}
		return ret;
	}

	public boolean changeApn(Network net) {
		if (!isApnChanged) {
			ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			boolean result = connectivityManager.bindProcessToNetwork(net);
			tsLib.debugPrint(DEBUG_UM,  "changeApn!!!! " + result);
			isApnChanged = result;
			return result;
		}
		return false;
	}

	public static boolean isNetworkConnect()
	{
		boolean ret = false;
/*
		ConnectivityManager connMgr =(ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		boolean isWifiConn = false;
		boolean isMobileConn = false;
		for (Network network : connMgr.getAllNetworks()) {
			NetworkInfo networkInfo = connMgr.getNetworkInfo(network);
			if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
				isWifiConn |= networkInfo.isConnectedOrConnecting();
		}
			if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
				isMobileConn |= networkInfo.isConnectedOrConnecting();
		}
	}
		tsLib.debugPrint(DEBUG_UM, "WiFi Connected: " + isWifiConn);
		tsLib.debugPrint(DEBUG_UM, "Mobile Connected: " + isMobileConn);

		if((isWifiConn) || (isMobileConn))
		ret = true;
*/


		tsLib.debugPrint(DEBUG_UM, "apn3Enable : " + apn3Enable);
		tsLib.debugPrint(DEBUG_UM, "isApnChanged: " + isApnChanged);
		if(apn3Enable || isApnChanged)
			ret = true;

		return ret;
	}

}
