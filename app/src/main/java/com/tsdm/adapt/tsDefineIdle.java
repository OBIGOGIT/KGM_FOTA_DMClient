package com.tsdm.adapt;

public interface tsDefineIdle
{
	int 	DM_FUMO_MECHANISM_NONE 									= 0;
	int 	DM_FUMO_MECHANISM_REPLACE 								= 1;
	int 	DM_FUMO_MECHANISM_ALTERNATIVE 							= 2;
	int 	DM_FUMO_MECHANISM_ALTERNATIVE_DOWNLOAD 					= 3;
	int 	DM_FUMO_MECHANISM_UPDATE 								= 4;
	int 	DM_FUMO_MECHANISM_END 									= 5;

	int 	DM_FUMO_STATE_NONE 										= 0;
	int 	DM_FUMO_STATE_IDLE_START 								= 10;
	int 	DM_FUMO_STATE_DOWNLOAD_FAILED 							= 20;
	int 	DM_FUMO_STATE_DOWNLOAD_IN_PROGRESS 						= 30;
	int 	DM_FUMO_STATE_DOWNLOAD_COMPLETE 						= 40;
	int 	DM_FUMO_STATE_READY_TO_UPDATE 							= 50;
	int 	DM_FUMO_STATE_UPDATE_IN_PROGRESS 						= 60;
	int 	DM_FUMO_STATE_UPDATE_FAILED_HAVEDATA					= 70;
	int 	DM_FUMO_STATE_UPDATE_FAILED_NODATA 						= 80;
	int 	DM_FUMO_STATE_UPDATE_SUCCESSFUL_HAVEDATA 				= 90;
	int 	DM_FUMO_STATE_UPDATE_SUCCESSFUL_NODATA 					= 100;


	int 	DM_FUMO_STATE_DOWNLOAD_DESCRIPTOR 						= 200;					// not spec
	int 	DM_FUMO_STATE_START_TO_UPDATE 							= 210;					// not spec
	int 	DM_FUMO_STATE_POSTPONE_TO_UPDATE 						= 220;					// not spec
	int 	DM_FUMO_STATE_POSTPONE_TO_DOWNLOAD 						= 221;					// not spec
	int 	DM_FUMO_STATE_DOWNLOAD_IN_CANCEL 						= 230;					// not spec
	int 	DM_FUMO_STATE_USER_CANCEL_REPORTING 					= 240;					// not spec
	int 	DM_FUMO_STATE_DOWNLOAD_FAILED_REPORTING 				= 241;
	int 	DM_STATE_GET_SESSION_ID_START 							= 242;
	int 	DM_FUMO_STATE_SUSPEND 									= 250;					// not spec

	String	FUMO_DEFAULT_PKGNAME									= "fota_delta_dp";
	String	FUMO_DEFAULT_PKGVERSION									= "1.0";
	String	FUMO_PATH												= "./FUMO";
	String	FUMO_PKGNAME_PATH										= "/PkgName";
	String	FUMO_PKGVERSION_PATH									= "/PkgVersion";
	String	FUMO_DOWNLOAD_PATH										= "/Download";
	String	FUMO_PKGURL_PATH										= "/PkgURL";
	String	FUMO_UPDATE_PATH										= "/Update";
	String	FUMO_PKGDATA_PATH										= "/PkgData";
	String	FUMO_DOWNLOADANDUPDATE_PATH								= "/DownloadAndUpdate";
	String	FUMO_STATE_PATH											= "/State";
	String	FUMO_EXT												= "/Ext";


	String 	DL_GENERIC_SUCCESSFUL 									= "200";
	String 	DL_GENERIC_SUCCESSFUL_UPDATE 							= "200";
	String 	DL_GENERIC_SUCCESSFUL_DOWNLOAD 							= "200";
	String 	DL_GENERIC_SUCCESSFUL_VENDOR_SPECIFIED 					= "250";
	String 	DL_GENERIC_CLIENT_ERROR 								= "400";
	String 	DL_GENERIC_USER_CANCELED_DOWNLOAD						= "401";
	String 	DL_GENERIC_CORRUPTED_FW_UP 								= "402";
	String	DL_GENERIC_PACKAGE_MISMATCH								= "403";
	String 	DL_GENERIC_FAILED_FW_UP_VALIDATION 						= "404";
	String	DL_GENERIC_NOT_ACCEPTABLE 								= "405";
	String 	DL_GENERIC_AUTHENTICATION_FAILURE 						= "406";
	String 	DL_GENERIC_REQUEST_TIME_OUT								= "407";
	String	DL_GENERIC_NOT_IMPLEMENTED 								= "408";
	String 	DL_GENERIC_UNDEFINED_ERROR 								= "409";
	String	DL_GENERIC_UPDATE_FAILED 								= "410";
	String 	DL_GENERIC_BAD_URL 										= "411";
	String 	DL_GENERIC_SERVER_UNAVAILABLE 							= "412";
	String 	DL_GENERIC_SERVER_ERROR 								= "500";
	String	DL_GENERIC_DOWNLOAD_FAILED_OUT_MEMORY 					= "501";
	String 	DL_GENERIC_UPDATE_FAILED_OUT_MEMORY 					= "502";
	String 	DL_GENERIC_DOWNLOAD_FAILED_NETWORK 						= "503";
	String 	DL_GENERIC_DOWNLOAD_FILE_ERROR 							= "952";
	String 	DL_USER_CANCELED_DOWNLOAD 								= "902";



	String 	UPDATE_USER_CANCELED 							        = "401";
	String 	UPDATE_PARTITION 							        	= "190";
	String 	UPDATE_START				 							= "198";
	String 	UPDATE_START_REBOOT			 							= "199";
	String 	UPDATE_SUCCESS				 							= "200";
	String 	UPDATE_FAIL				 							    = "400";
	String 	UPDATE_STANDBY				 							= "197";




	int 	DL_MEMORY_INSUFFICIENT 									= 2;
	int 	DL_OVER_OBJECT_SIZE 									= 1;

	int 	OMA_DL_STAUS_SUCCESS 									= 0;
	int 	OMA_DL_STATUS_MEMORY_ERROR 								= 1;
	int 	OMA_DL_STATUS_USER_CANCEL 								= 2;
	int 	OMA_DL_STATUS_LOSS_SERVICE 								= 3;
	int 	OMA_DL_STATUS_ATTRIBUTE_MISMATCH 						= 4;
	int 	OMA_DL_STATUS_INVALID_DESCRIPTOR 						= 5;
	int 	OMA_DL_STATUS_INVALID_DDVERSIONV 						= 6;
	int 	OMA_DL_STATUS_DEVICE_ABORTED 							= 7;
	int 	OMA_DL_STATUS_NON_ACCEPTABLE_CONTENT 					= 8;
	int 	OMA_DL_STATUS_LOADER_ERROR 								= 9;
	int 	OMA_DL_STATUS_NONE 										= 10;

	int		SDL_RET_FAILED											= -2;
	int		SDL_RET_UNKNOWN_DD										= -1;
	int		SDL_RET_OK												= 0;
	int		SDL_RET_CONTINUE										= 1;
}
