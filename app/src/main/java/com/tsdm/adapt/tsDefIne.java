package com.tsdm.adapt;

public interface tsDefIne
{
	String	CMD_SYNCHDR											= "SyncHdr";
	String	CMD_STATUS											= "Status";
	String	CMD_ALERT											= "Alert";
	String	CMD_SYNC											= "Sync";
	String	CMD_ATOMIC											= "Atomic";
	String	CMD_SEQUENCE										= "Sequence";
	String	CMD_ADD												= "Add";
	String	CMD_REPLACE											= "Replace";
	String	CMD_DELETE											= "Delete";
	String	CMD_PUT												= "Put";
	String	CMD_GET												= "Get";
	String	CMD_RESULTS											= "Results";
	String	CMD_MAP												= "Map";
	String	CMD_EXEC											= "Exec";
	String	CMD_COPY											= "Copy";

	// ==================================================================//
	// For DM Status define
	String	STATUS_OK											= "200";
	String	STATUS_ACCEPTED_FOR_PROCESSING						= "202";
	String	STATUS_AUTHENTICATIONACCEPTED						= "212";
	String	STATUS_ACCEPTED_AND_BUFFERED						= "213";
	String	STATUS_OPERATION_CANCELLED							= "214";
	String	STATUS_NOT_EXECUTED									= "215";
	String	STATUS_ATOMIC_ROLL_BACK_OK							= "216";
	String	STATUS_NOT_MODIFIED									= "304";
	String	STATUS_UNAUTHORIZED									= "401";
	String	STATUS_FORBIDDEN									= "403";
	String	STATUS_NOT_FOUND									= "404";
	String	STATUS_COMMAND_NOT_ALLOWED							= "405";
	String	STATUS_OPTIONAL_FEATURE_NOT_SUPPORTED				= "406";
	String	STATUS_AUTHENTICATION_REQUIRED						= "407";
	String	STATUS_REQUEST_TIMEOUT								= "408";
	String	STATUS_INCOMPLETE_COMMAND							= "412";
	String	STATUS_REQUEST_ENTITY_TOO_LARGE						= "413";
	String	STATUS_URI_TOO_LONG									= "414";
	String	STATUS_UNSUPPORTED_MEDEA_TYPE_OR_FORMAT				= "415";
	String	STATUS_REQUESTED_RANGE_NOT_SATISFIABLE				= "416";
	String	STATUS_ALREADY_EXISTS								= "418";
	String	STATUS_DEVICE_FULL									= "420";
	String	STATUS_PERMISSION_DENIED							= "425";
	String	STATUS_COMMAND_FAILED								= "500";
	String	STATUS_ATOMIC_FAILED								= "507";
	String	STATUS_DATA_STORE_FAILURE							= "510";
	String	STATUS_ATOMIC_ROLL_BACK_FAILED						= "516";
	String	STATUS_ATOMIC_RESPONSE_TOO_LARGE_TO_FIT				= "517";

	String	ALERT_DISPLAY										= "1100";
	String	ALERT_CONTINUE_OR_ABORT								= "1101";
	String	ALERT_TEXT_INPUT									= "1102";
	String	ALERT_SINGLE_CHOICE									= "1103";
	// Mod: change define name
	String	ALERT_MULTIPLE_CHOICE								= "1104";
	String	ALERT_SERVER_INITIATED_MGMT							= "1200";
	String	ALERT_CLIENT_INITIATED_MGMT							= "1201";
	String	ALERT_NEXT_MESSAGE									= "1222";
	String	ALERT_SESSION_ABORT									= "1223";
	String	ALERT_CLIENT_EVENT									= "1224";
	String	ALERT_NO_END_OF_DATA								= "1225";
	String	ALERT_GENERIC										= "1226";
}
