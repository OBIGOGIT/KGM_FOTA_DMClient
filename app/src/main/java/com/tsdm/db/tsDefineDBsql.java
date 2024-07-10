package com.tsdm.db;

public interface tsDefineDBsql
{
	int		DB_SQL_OK = 0;
	String	DATABASE_NAME												= "dmdatabase.db";
	String	DATABASE_PROFILE_TABLE										= "profile";
	String	DATABASE_NETWORK_TABLE										= "network";
	String	DATABASE_PROFILELIST_TABLE									= "profilelist";
	String	DATABASE_FUMO_TABLE											= "fumo";
	String	DATABASE_POSTPONE_TABLE										= "postpone";
	String	DATABASE_SIMINFO_TABLE										= "siminfo";
	String	DATABASE_ACCXLISTNODE_TABLE									= "accxlistnode";
	String	DATABASE_RESYNCMODE_TABLE									= "resyncmode";

	long 	DM_SQL_DB_PROFILE1_ROWID 									= 1;
	long 	DM_SQL_DB_PROFILE2_ROWID									= 2;
	long 	DM_SQL_DB_PROFILE3_ROWID 									= 3;

	long 	DM_SQL_DB_NETWORK1_ROWID 									= 1;

	long 	DM_SQL_DB_PROFILELIST_ROWID 								= 1;
	long 	DM_SQL_DB_FUMO_ROWID 										= 1;
	long 	DM_SQL_DB_POSTPONE_ROWID 									= 1;
	long 	DM_SQL_DB_SIMINFO_ROWID 									= 1;
	long 	DM_SQL_DB_ACCXLISTNODE_ROWID 								= 1;
	long 	DM_SQL_DB_RESYNCMODE_ROWID 									= 1;

	String 	DM_SQL_DB_ROWID 											= "rowid";

	int 	dmSqlDbIdProfileList 		= 0x0051;
	int 	dmSqlDbIdProfileInfo1 		= 0x0052;
	int 	dmSqlDbIdProfileInfo2 		= 0x0053;
	int 	dmSqlDbIdProfileInfo3 		= 0x0054;
	int 	dmSqlDbIdProfileInfo4 		= 0x0055;
	int 	dmSqlDbIdProfileInfo5 		= 0x0056;
	int 	dmSqlDbIdFUMOInfo 			= 0x0057;
	int 	dmSqlDbIdAPostPone 			= 0x0058;
	int 	dmSqlDbIdIMSIInfo			= 0x0059;
	int 	dmSqlDbIdResyncMode 		= 0x0060;
	int 	dmSqlDbIdDmAgentInfo 		= 0x0061;
	int		dmSqlDbIdScomoInfo			= 0x0062;
	int 	dmSqlDbIdScomoPostpone 		= 0x0063;
	int 	dmSqlDbIdLawmoInfo 			= 0x0064;
	int		dmSqlDbIdNotiInfo 			= 0x0066;
	int 	dmSqlDbIdNetworkInfo 		= 0x0080;
	int 	dmSqlDbIdAccXNode			= 0x0083;


	String 	DM_SQL_DB_PROFILE_PROTOCOL = "protocol";
	String 	DM_SQL_DB_PROFILE_SERVERPORT = "serverport";
	String 	DM_SQL_DB_PROFILE_SERVERURL = "serverurl";
	String 	DM_SQL_DB_PROFILE_SERVERIP = "serverip";
	String 	DM_SQL_DB_PROFILE_PATH = "path";
	String 	DM_SQL_DB_PROFILE_PROTOCOL_ORG = "protocol_org";
	String 	DM_SQL_DB_PROFILE_SERVERPORT_ORG = "serverport_org";
	String 	DM_SQL_DB_PROFILE_SERVERURL_ORG = "serverurl_org";
	String 	DM_SQL_DB_PROFILE_SERVERIP_ORG = "serverip_org";
	String 	DM_SQL_DB_PROFILE_PATH_ORG = "path_org";
	String 	DM_SQL_DB_PROFILE_CHANGEDPROTOCOL = "changedprotocol";
	String 	DM_SQL_DB_PROFILE_OBEXTYPE = "obextype";
	String 	DM_SQL_DB_PROFILE_AUTHTYPE = "authtype";
	String 	DM_SQL_DB_PROFILE_SERVERAUTHTYPE = "serverauthtype";
	String 	DM_SQL_DB_PROFILE_APPID = "appid";
	String 	DM_SQL_DB_PROFILE_AUTHLEVEL = "authlevel";
	String 	DM_SQL_DB_PROFILE_SERVERAUTHLEVEL = "serverauthlevel";
	String 	DM_SQL_DB_PROFILE_PREFCONREF = "prefconref";
	String 	DM_SQL_DB_PROFILE_USERNAME = "username";
	String 	DM_SQL_DB_PROFILE_PASSWORD = "password";
	String 	DM_SQL_DB_PROFILE_SERVERID = "serverid";
	String 	DM_SQL_DB_PROFILE_SERVERPWD = "serverpwd";
	String 	DM_SQL_DB_PROFILE_CLIENTNONCE = "clientnonce";
	String 	DM_SQL_DB_PROFILE_SERVERNONCE = "servernonce";
	String 	DM_SQL_DB_PROFILE_SERVERNONCEFORMAT = "servernonceformat";
	String 	DM_SQL_DB_PROFILE_CLIENTNONCEFORMAT = "clientnonceformat";
	String 	DM_SQL_DB_PROFILE_PROFILENAME = "profilename";
	String 	DM_SQL_DB_PROFILE_NETWORKCONNNAME = "networkconnname";
	String 	DM_SQL_DB_PROFILE_NETWORKCONNINDEX = "networkconnindex";
	String 	DM_SQL_DB_PROFILE_MAGICNUMBER = "magicnumber";


	String 	DM_SQL_DB_NETWORK_HOMEURL = "homeurl";
	String 	DM_SQL_DB_NETWORK_SERVICE = "service";
	String 	DM_SQL_DB_NETWORK_ACTIVE = "active";
	String 	DM_SQL_DB_NETWORK_PROXYUSE = "proxyuse";
	String 	DM_SQL_DB_NETWORK_NAP_NETWORKPROFILENAME = "napnetworkprofilename";
	String 	DM_SQL_DB_NETWORK_NAP_BEARER = "napbearer";
	String 	DM_SQL_DB_NETWORK_NAP_ADDRTYPE = "napaddrtype";
	String 	DM_SQL_DB_NETWORK_NAP_ADDR = "napaddr";
	String 	DM_SQL_DB_NETWORK_NAP_AUTH_PAPID = "nappapid";
	String 	DM_SQL_DB_NETWORK_NAP_AUTH_PAPSECRET = "nappapsecret";
	String 	DM_SQL_DB_NETWORK_PX_PORTNBR = "pxportnbr";
	String 	DM_SQL_DB_NETWORK_PX_ADDRTYPE = "pxaddrtype";
	String 	DM_SQL_DB_NETWORK_PX_ADDR = "pxaddr";
	String 	DM_SQL_DB_NETWORK_PX_AUTH_PAPID = "pxpapid";
	String 	DM_SQL_DB_NETWORK_PX_AUTH_PAPSECRET = "pxpapsecret";
	String 	DM_SQL_DB_NETWORK_ADVSETTING_STATICIPUSE = "staticipuse";
	String 	DM_SQL_DB_NETWORK_ADVSETTING_STATICIP = "staticip";
	String 	DM_SQL_DB_NETWORK_ADVSETTING_STATICDNSUSE = "staticdnsuse";
	String 	DM_SQL_DB_NETWORK_ADVSETTING_STATICDNS1 = "staticdns1";
	String 	DM_SQL_DB_NETWORK_ADVSETTING_STATICDNS2 = "staticdns2";
	String 	DM_SQL_DB_NETWORK_ADVSETTING_TRAFFICCLASS = "trafficclass";


	String 	DM_SQL_DB_PROFILELIST_NETWORKCONNNAME = "networkconnname";
	String 	DM_SQL_DB_PROFILELIST_PROXYINDEX = "proxyindex";
	String 	DM_SQL_DB_PROFILELIST_PROFILEINDEX = "profileindex";
	String 	DM_SQL_DB_PROFILELIST_PROFILENAME1 = "profilename1";
	String 	DM_SQL_DB_PROFILELIST_PROFILENAME2 = "profilename2";
	String 	DM_SQL_DB_PROFILELIST_PROFILENAME3 = "profilename3";
	String 	DM_SQL_DB_PROFILELIST_SESSIONID = "sessionid";
	String 	DM_SQL_DB_PROFILELIST_NOTIEVENT = "notievent";
	String 	DM_SQL_DB_PROFILELIST_DESTORYNOTITIME = "destorynotitime";
	String 	DM_SQL_DB_PROFILELIST_NOTIRESYNCMODE = "notiresyncmode";
	String 	DM_SQL_DB_PROFILELIST_DDFPARSERNODEINDEX = "ddfparsernodeindex";
	String 	DM_SQL_DB_PROFILELIST_SKIPDEVDISCOVERY = "skipdevdiscovery";
	String 	DM_SQL_DB_PROFILELIST_MAGICNUMBER = "magicnumber";
	String 	DM_SQL_DB_PROFILELIST_NOTIRESUMESTATE_SESSIONSAVESTATE = "sessionsavestate";
	String 	DM_SQL_DB_PROFILELIST_NOTIRESUMESTATE_NOTIUIEVENT = "notiuievent";
	String 	DM_SQL_DB_PROFILELIST_NOTIRESUMESTATE_NOTIRETRYCOUNT = "notiretrycount";
	String 	DM_SQL_DB_PROFILELIST_UICRESULTKEEP_STATUS = "status";
	String 	DM_SQL_DB_PROFILELIST_UICRESULTKEEP_APPID = "appid";
	String 	DM_SQL_DB_PROFILELIST_UICRESULTKEEP_UICTYPE = "uictype";
	String 	DM_SQL_DB_PROFILELIST_UICRESULTKEEP_RESULT = "result";
	String 	DM_SQL_DB_PROFILELIST_UICRESULTKEEP_NUMBER = "number";
	String 	DM_SQL_DB_PROFILELIST_UICRESULTKEEP_TEXT = "text";
	String 	DM_SQL_DB_PROFILELIST_UICRESULTKEEP_LEN = "len";
	String 	DM_SQL_DB_PROFILELIST_UICRESULTKEEP_SIZE = "size";
	String 	DM_SQL_DB_PROFILELIST_IMEI = "imei";
	String 	DM_SQL_DB_PROFILELIST_WIFIONLY = "wifionly";
	String 	DM_SQL_DB_PROFILELIST_AUTOUPDATE = "autoupdate";
	String 	DM_SQL_DB_PROFILELIST_PUSHMESSAGE = "pushmessage";
	String 	DM_SQL_DB_PROFILELIST_AUTOUPDATETIME = "autoupdatetime";
	String 	DM_SQL_DB_PROFILELIST_SAVE_DELTAFILE_INDEX = "savedeltafileindex";
	String 	DM_SQL_DB_PROFILELIST_NEXTUPDATETIME = "nextupdatetime";
	String 	DM_SQL_DB_PROFILELIST_AUTOUPDATECHECK = "autoupdatecheck";
	String 	DM_SQL_DB_PROFILELIST_AGENT_MODE = "agentmode";
	String 	DM_SQL_DB_PROFILELIST_CURR_CHECK_TIME = "currentchecktime";
	

	String 	DM_SQL_DB_FUMO_PROTOCOL = "protocol";
	String 	DM_SQL_DB_FUMO_OBEXTYPE = "obextype";
	String 	DM_SQL_DB_FUMO_AUTHTYPE = "authtype";
	String 	DM_SQL_DB_FUMO_SERVERPORT = "serverport";
	String 	DM_SQL_DB_FUMO_SERVERURL = "serverurl";
	String 	DM_SQL_DB_FUMO_SERVERIP = "serverip";
	String 	DM_SQL_DB_FUMO_OBJECTDOWNLOADPROTOCOL = "objectdownloadprotocol";
	String 	DM_SQL_DB_FUMO_OBJECTDOWNLOADURL = "objectdownloadurl";
	String 	DM_SQL_DB_FUMO_OBJECTDOWNLOADIP = "objectdownloadip";
	String 	DM_SQL_DB_FUMO_OBJECTDOWNLOADPORT = "objectdownloadport";
	String 	DM_SQL_DB_FUMO_STATUSNOTIFYPROTOCOL = "statusnotifyprotocol";
	String 	DM_SQL_DB_FUMO_STATUSNOTIFYURL = "statusnotifyurl";
	String 	DM_SQL_DB_FUMO_STATUSNOTIFYIP = "statusnotifyip";
	String 	DM_SQL_DB_FUMO_STATUSNOTIFYPORT = "statusnotifyport";
	String 	DM_SQL_DB_FUMO_REPORTURI = "reporturi";
	String 	DM_SQL_DB_FUMO_OBJECTSIZE = "objectsize";
	String 	DM_SQL_DB_FUMO_FFSWRITESIZE = "ffswritesize";
	String 	DM_SQL_DB_FUMO_STATUS = "status";
	String 	DM_SQL_DB_FUMO_STATUSNODENAME = "statusnodename";
	String 	DM_SQL_DB_FUMO_RESULTCODE = "resultcode";
	String 	DM_SQL_DB_FUMO_UPDATEMECHANISM = "updatemechanism";
	String 	DM_SQL_DB_FUMO_DOWNLOADMODE = "downloadmode";
	String 	DM_SQL_DB_FUMO_CORRELATOR = "correlator";
	String 	DM_SQL_DB_FUMO_CONTENTTYPE = "contenttype";
	String 	DM_SQL_DB_FUMO_ACCEPTTYPE = "accepttype";
	String 	DM_SQL_DB_FUMO_UPDATEWAIT = "updatewait";


	String 	DM_SQL_DB_POSTPONE_CURRENTTIME = "currenttime";
	String 	DM_SQL_DB_POSTPONE_ENDTIME = "endtime";
	String 	DM_SQL_DB_POSTPONE_AFTERDOWNLOADBATTERYSTATUS = "afterdownloadbatterystatus";
	String 	DM_SQL_DB_POSTPONE_POSTPONETIME = "postponetime";
	String 	DM_SQL_DB_POSTPONE_POSTPONECOUNT = "postponecount";
	String 	DM_SQL_DB_POSTPONE_POSTPONEDOWNLOAD = "postponedownload";

	String 	DM_SQL_DB_SIMINFO_IMSI = "imsi";


	String 	DM_SQL_DB_ACCXLISTNODE_ACCOUNT = "account";
	String 	DM_SQL_DB_ACCXLISTNODE_APPADDR = "appaddr";
	String 	DM_SQL_DB_ACCXLISTNODE_APPADDRPORT = "appaddrport";
	String 	DM_SQL_DB_ACCXLISTNODE_CLIENTAPPAUTH = "clientappauth";
	String 	DM_SQL_DB_ACCXLISTNODE_SERVERAPPAUTH = "serverappauth";
	String 	DM_SQL_DB_ACCXLISTNODE_TOCONREF = "toconref";


	String 	DM_SQL_DB_RESYNCMODE_NONCERESYNCMODE = "nonceresyncmode";

	String	DATABASE_PROFILE_CREATE										=
		"create table if not exists " + DATABASE_PROFILE_TABLE + " (rowid integer primary key autoincrement, "
		+ DM_SQL_DB_PROFILE_PROTOCOL +" text, "
		+ DM_SQL_DB_PROFILE_SERVERPORT + " integer, "
		+ DM_SQL_DB_PROFILE_SERVERURL + " text, "
		+ DM_SQL_DB_PROFILE_SERVERIP + " text, "
		+ DM_SQL_DB_PROFILE_PATH + " text, "
		+ DM_SQL_DB_PROFILE_PROTOCOL_ORG + " text, "
		+ DM_SQL_DB_PROFILE_SERVERPORT_ORG + " integer, "
		+ DM_SQL_DB_PROFILE_SERVERURL_ORG + " text, "
		+ DM_SQL_DB_PROFILE_SERVERIP_ORG + " text, "
		+ DM_SQL_DB_PROFILE_PATH_ORG + " text, "
		+ DM_SQL_DB_PROFILE_CHANGEDPROTOCOL + " integer, "
		+ DM_SQL_DB_PROFILE_OBEXTYPE + " integer, "
		+ DM_SQL_DB_PROFILE_AUTHTYPE + " integer, "
		+ DM_SQL_DB_PROFILE_SERVERAUTHTYPE + " integer, "
		+ DM_SQL_DB_PROFILE_APPID + " text, "
		+ DM_SQL_DB_PROFILE_AUTHLEVEL + " text, "
		+ DM_SQL_DB_PROFILE_SERVERAUTHLEVEL + " text, "
		+ DM_SQL_DB_PROFILE_PREFCONREF + " text, "
		+ DM_SQL_DB_PROFILE_USERNAME + " text, "
		+ DM_SQL_DB_PROFILE_PASSWORD + " text, "
		+ DM_SQL_DB_PROFILE_SERVERID + " text, "
		+ DM_SQL_DB_PROFILE_SERVERPWD + " text, "
		+ DM_SQL_DB_PROFILE_CLIENTNONCE + " text, "
		+ DM_SQL_DB_PROFILE_SERVERNONCE + " text, "
		+ DM_SQL_DB_PROFILE_SERVERNONCEFORMAT + " integer, "
		+ DM_SQL_DB_PROFILE_CLIENTNONCEFORMAT + " integer, "
		+ DM_SQL_DB_PROFILE_PROFILENAME + " text, "
		+ DM_SQL_DB_PROFILE_NETWORKCONNNAME + " text, "
		+ DM_SQL_DB_PROFILE_NETWORKCONNINDEX + " integer, "
		+ DM_SQL_DB_PROFILE_MAGICNUMBER + " integer);";

	String	DATABASE_NETWORK_CREATE										=
		"create table if not exists " + DATABASE_NETWORK_TABLE + " (rowid integer primary key autoincrement, "
		+ DM_SQL_DB_NETWORK_HOMEURL + " text, "
		+ DM_SQL_DB_NETWORK_SERVICE + " integer, "
		+ DM_SQL_DB_NETWORK_ACTIVE + " integer, "
		+ DM_SQL_DB_NETWORK_PROXYUSE + " integer, "
		+ DM_SQL_DB_NETWORK_NAP_NETWORKPROFILENAME + " text, "
		+ DM_SQL_DB_NETWORK_NAP_BEARER + " integer, "
		+ DM_SQL_DB_NETWORK_NAP_ADDRTYPE + " integer, "
		+ DM_SQL_DB_NETWORK_NAP_ADDR + " text, "
		+ DM_SQL_DB_NETWORK_NAP_AUTH_PAPID + " text, "
		+ DM_SQL_DB_NETWORK_NAP_AUTH_PAPSECRET + " text, "
		+ DM_SQL_DB_NETWORK_PX_PORTNBR + " integer, "
		+ DM_SQL_DB_NETWORK_PX_ADDRTYPE + " integer, "
		+ DM_SQL_DB_NETWORK_PX_ADDR + " text, "
		+ DM_SQL_DB_NETWORK_PX_AUTH_PAPID + " text, "
		+ DM_SQL_DB_NETWORK_PX_AUTH_PAPSECRET + " text, "
		+ DM_SQL_DB_NETWORK_ADVSETTING_STATICIPUSE + " integer, "
		+ DM_SQL_DB_NETWORK_ADVSETTING_STATICIP + " text, "
		+ DM_SQL_DB_NETWORK_ADVSETTING_STATICDNSUSE + " integer, "
		+ DM_SQL_DB_NETWORK_ADVSETTING_STATICDNS1 + " integer, "
		+ DM_SQL_DB_NETWORK_ADVSETTING_STATICDNS2 + " integer, "
	    + DM_SQL_DB_NETWORK_ADVSETTING_TRAFFICCLASS + " integer);";
	
	String	DATABASE_PROFILELIST_CREATE									=
		"create table if not exists " + DATABASE_PROFILELIST_TABLE + " (rowid integer primary key autoincrement, "
		+ DM_SQL_DB_PROFILELIST_NETWORKCONNNAME + " text, "
		+ DM_SQL_DB_PROFILELIST_PROXYINDEX + " integer, "
		+ DM_SQL_DB_PROFILELIST_PROFILEINDEX + " integer, "
		+ DM_SQL_DB_PROFILELIST_PROFILENAME1 + " text, "
		+ DM_SQL_DB_PROFILELIST_PROFILENAME2 + " text, "
		+ DM_SQL_DB_PROFILELIST_PROFILENAME3 + " text, "
		+ DM_SQL_DB_PROFILELIST_SESSIONID + " text, "
		+ DM_SQL_DB_PROFILELIST_NOTIEVENT + " integer, "
		+ DM_SQL_DB_PROFILELIST_DESTORYNOTITIME + " integer, "
		+ DM_SQL_DB_PROFILELIST_NOTIRESYNCMODE + " integer, "
		+ DM_SQL_DB_PROFILELIST_DDFPARSERNODEINDEX + " integer, "
		+ DM_SQL_DB_PROFILELIST_SKIPDEVDISCOVERY + " integer, "
		+ DM_SQL_DB_PROFILELIST_MAGICNUMBER + " integer, "
		+ DM_SQL_DB_PROFILELIST_NOTIRESUMESTATE_SESSIONSAVESTATE + " integer, "
		+ DM_SQL_DB_PROFILELIST_NOTIRESUMESTATE_NOTIUIEVENT + " integer, "
		+ DM_SQL_DB_PROFILELIST_NOTIRESUMESTATE_NOTIRETRYCOUNT + " integer, "
		+ DM_SQL_DB_PROFILELIST_UICRESULTKEEP_STATUS + " integer, "
		+ DM_SQL_DB_PROFILELIST_UICRESULTKEEP_APPID + " integer, "
		+ DM_SQL_DB_PROFILELIST_UICRESULTKEEP_UICTYPE + " integer, "
		+ DM_SQL_DB_PROFILELIST_UICRESULTKEEP_RESULT + " integer, "
		+ DM_SQL_DB_PROFILELIST_UICRESULTKEEP_NUMBER + " integer, "
		+ DM_SQL_DB_PROFILELIST_UICRESULTKEEP_TEXT + " text, "
		+ DM_SQL_DB_PROFILELIST_UICRESULTKEEP_LEN + " integer, "
		+ DM_SQL_DB_PROFILELIST_UICRESULTKEEP_SIZE + " integer, "
	    + DM_SQL_DB_PROFILELIST_IMEI + " integer, "
		+ DM_SQL_DB_PROFILELIST_WIFIONLY + " boolean, "
		+ DM_SQL_DB_PROFILELIST_AUTOUPDATE + " boolean, "
		+ DM_SQL_DB_PROFILELIST_PUSHMESSAGE + " boolean, "
		+ DM_SQL_DB_PROFILELIST_AUTOUPDATETIME + " integer, "
		+ DM_SQL_DB_PROFILELIST_SAVE_DELTAFILE_INDEX + " integer, "
		+ DM_SQL_DB_PROFILELIST_NEXTUPDATETIME + " integer, "
		+ DM_SQL_DB_PROFILELIST_AUTOUPDATECHECK + " boolean, "
		+ DM_SQL_DB_PROFILELIST_AGENT_MODE + " boolean, "
		+ DM_SQL_DB_PROFILELIST_CURR_CHECK_TIME + " integer);";
	
	String	DATABASE_FUMO_CREATE										=
		"create table if not exists " + DATABASE_FUMO_TABLE + " (rowid integer primary key autoincrement, "
		+ DM_SQL_DB_FUMO_PROTOCOL + " text, "
		+ DM_SQL_DB_FUMO_OBEXTYPE + " integer, "
		+ DM_SQL_DB_FUMO_AUTHTYPE + " integer, "
		+ DM_SQL_DB_FUMO_SERVERPORT + " integer, "
		+ DM_SQL_DB_FUMO_SERVERURL + " text, "
		+ DM_SQL_DB_FUMO_SERVERIP + " text, "
		+ DM_SQL_DB_FUMO_OBJECTDOWNLOADPROTOCOL + " text, "
		+ DM_SQL_DB_FUMO_OBJECTDOWNLOADURL + " text, "
		+ DM_SQL_DB_FUMO_OBJECTDOWNLOADIP + " text, "
		+ DM_SQL_DB_FUMO_OBJECTDOWNLOADPORT + " integer, "
		+ DM_SQL_DB_FUMO_STATUSNOTIFYPROTOCOL + " text, "
		+ DM_SQL_DB_FUMO_STATUSNOTIFYURL + " text, "
		+ DM_SQL_DB_FUMO_STATUSNOTIFYIP + " text, "
		+ DM_SQL_DB_FUMO_STATUSNOTIFYPORT + " integer, "
		+ DM_SQL_DB_FUMO_REPORTURI + " text, "
		+ DM_SQL_DB_FUMO_OBJECTSIZE + " integer, "
		+ DM_SQL_DB_FUMO_FFSWRITESIZE + " integer, "
		+ DM_SQL_DB_FUMO_STATUS + " integer, "
		+ DM_SQL_DB_FUMO_STATUSNODENAME + " text, "
		+ DM_SQL_DB_FUMO_RESULTCODE + " text, "
		+ DM_SQL_DB_FUMO_UPDATEMECHANISM + " integer, "
		+ DM_SQL_DB_FUMO_DOWNLOADMODE + " integer, "
		+ DM_SQL_DB_FUMO_CORRELATOR + " text, "
		+ DM_SQL_DB_FUMO_CONTENTTYPE + " text, "
		+ DM_SQL_DB_FUMO_ACCEPTTYPE + " text, "
		+ DM_SQL_DB_FUMO_UPDATEWAIT + " integer);";
	
	String	DATABASE_POSTPONE_CREATE									=
		"create table if not exists " + DATABASE_POSTPONE_TABLE + " (rowid integer primary key autoincrement, "
		+ DM_SQL_DB_POSTPONE_CURRENTTIME + " integer, "
		+ DM_SQL_DB_POSTPONE_ENDTIME + " integer, "
		+ DM_SQL_DB_POSTPONE_AFTERDOWNLOADBATTERYSTATUS + " integer, "
		+ DM_SQL_DB_POSTPONE_POSTPONETIME + " integer, "
		+ DM_SQL_DB_POSTPONE_POSTPONECOUNT + " integer, "
		+ DM_SQL_DB_POSTPONE_POSTPONEDOWNLOAD + " boolean);";

	String	DATABASE_SIMINFO_CREATE										=
		"create table if not exists " + DATABASE_SIMINFO_TABLE + " (rowid integer primary key autoincrement, "
		+ DM_SQL_DB_SIMINFO_IMSI + " text);";
	
	String	DATABASE_ACCXLISTNODE_CREATE								=
		"create table if not exists " + DATABASE_ACCXLISTNODE_TABLE + " (rowid integer primary key autoincrement, "
		+ DM_SQL_DB_ACCXLISTNODE_ACCOUNT + " text, "
		+ DM_SQL_DB_ACCXLISTNODE_APPADDR + " text, "
		+ DM_SQL_DB_ACCXLISTNODE_APPADDRPORT + " text, "
		+ DM_SQL_DB_ACCXLISTNODE_CLIENTAPPAUTH + " text, "
		+ DM_SQL_DB_ACCXLISTNODE_SERVERAPPAUTH + " text, "
		+ DM_SQL_DB_ACCXLISTNODE_TOCONREF + " text);";
	
	String	DATABASE_RESYNCMODE_CREATE									=
		"create table if not exists " + DATABASE_RESYNCMODE_TABLE + " (rowid integer primary key autoincrement, "
		+ DM_SQL_DB_RESYNCMODE_NONCERESYNCMODE + " integer);";

	String	DATABASE_DM_AGENT_INFO_TABLE								= "DmAgnetInfo";
	String DM_SQL_DB_AGENT_INFO_AGENT_TYPE = "AgentType";

	String	DATABASE_DM_AGENT_INFO_CREATE								=
		"create table if not exists " + DATABASE_DM_AGENT_INFO_TABLE + " (rowid integer primary key autoincrement, "
		+ DM_SQL_DB_AGENT_INFO_AGENT_TYPE + " integer);";
}