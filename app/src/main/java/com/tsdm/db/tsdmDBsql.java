package com.tsdm.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.tsdm.agent.dmDefineDevInfo;
import com.tsdm.agent.dmAgentInfo;
import com.tsdm.adapt.tsLib;

public class tsdmDBsql implements tsDefineDBsql, dmDefineDevInfo
{
	private static SQLiteDatabase	db;

	public static void DBHelper(Context ctx)
	{
		// ctx.deleteDatabase(DATABASE_NAME);
		db = ctx.openOrCreateDatabase(DATABASE_NAME, Context.MODE_PRIVATE, null);
//		db = ctx.openOrCreateDatabase(DATABASE_NAME, DATABASE_VERSION, null);
		db.execSQL(DATABASE_PROFILE_CREATE);
		db.execSQL(DATABASE_NETWORK_CREATE);
		db.execSQL(DATABASE_PROFILELIST_CREATE);
		db.execSQL(DATABASE_FUMO_CREATE);
		db.execSQL(DATABASE_POSTPONE_CREATE);
		db.execSQL(DATABASE_SIMINFO_CREATE);
		db.execSQL(DATABASE_ACCXLISTNODE_CREATE);
		db.execSQL(DATABASE_RESYNCMODE_CREATE);
		db.execSQL(DATABASE_DM_AGENT_INFO_CREATE);

		DbSqlProfileUpdateColumn();
	}
	
	public static void DbSqlProfileUpdateColumn()
	{
		DbSqlAddColumn(DATABASE_PROFILELIST_TABLE, DM_SQL_DB_PROFILELIST_WIFIONLY, "boolean", "1");
		DbSqlAddColumn(DATABASE_PROFILELIST_TABLE, DM_SQL_DB_PROFILELIST_AUTOUPDATE, "boolean", "1");
		DbSqlAddColumn(DATABASE_PROFILELIST_TABLE, DM_SQL_DB_PROFILELIST_PUSHMESSAGE, "boolean", "1");
		DbSqlAddColumn(DATABASE_PROFILELIST_TABLE, DM_SQL_DB_PROFILELIST_AUTOUPDATETIME, "integer", "0");
		DbSqlAddColumn(DATABASE_PROFILELIST_TABLE, DM_SQL_DB_PROFILELIST_SAVE_DELTAFILE_INDEX, "integer", "0");
		DbSqlAddColumn(DATABASE_POSTPONE_TABLE, DM_SQL_DB_POSTPONE_POSTPONEDOWNLOAD, "boolean", "0");
	}
	
	public static void DbSqlAddColumn(String tabelName, String column, String type, String defaultValue)
	{
		try
		{
			String sql = "select "+ column + " from " + tabelName;
			Cursor c = db.rawQuery(sql, null);
		}
		catch(SQLiteException e)
		{
			String sql = "";
			if(tsLib.isEmpty(defaultValue))
			{
				sql = "ALTER TABLE " + tabelName + " ADD " + column + " " + type ;
			}
			else 
			{
				sql = "ALTER TABLE " + tabelName + " ADD " + column + " " + type + " default " + defaultValue;
			}
			tsLib.debugPrint(DEBUG_DM, "Database Add Column : " + tabelName + " / " + column + " / " + type);
			
			db.execSQL(sql);
		}
		catch(Exception e)
		{
			tsLib.debugPrint(DEBUG_DM, e.toString());
		}
	}


	public static void fullResetDB()
	{
		// drop table
		db.execSQL("DROP TABLE IF EXISTS profile");
		db.execSQL("DROP TABLE IF EXISTS network");
		db.execSQL("DROP TABLE IF EXISTS profilelist");
		db.execSQL("DROP TABLE IF EXISTS fumo");
		db.execSQL("DROP TABLE IF EXISTS postpone");
		db.execSQL("DROP TABLE IF EXISTS siminfo");
		db.execSQL("DROP TABLE IF EXISTS accxlistnode");
		db.execSQL("DROP TABLE IF EXISTS resyncmode");
		db.execSQL("DROP TABLE IF EXISTS DmAgnetInfo");

		// create table
		db.execSQL(DATABASE_PROFILE_CREATE);
		db.execSQL(DATABASE_NETWORK_CREATE);
		db.execSQL(DATABASE_PROFILELIST_CREATE);
		db.execSQL(DATABASE_FUMO_CREATE);
		db.execSQL(DATABASE_POSTPONE_CREATE);
		db.execSQL(DATABASE_SIMINFO_CREATE);
		db.execSQL(DATABASE_ACCXLISTNODE_CREATE);
		db.execSQL(DATABASE_RESYNCMODE_CREATE);
		db.execSQL(DATABASE_DM_AGENT_INFO_CREATE);
	}

	public static void insertProfileRow(tsdmInfo dminfo)
	{
		ContentValues initialValues = new ContentValues();

		initialValues.put(DM_SQL_DB_PROFILE_PROTOCOL, dminfo.Protocol);
		initialValues.put(DM_SQL_DB_PROFILE_SERVERPORT, dminfo.ServerPort);
		initialValues.put(DM_SQL_DB_PROFILE_SERVERURL, dminfo.ServerUrl);
		initialValues.put(DM_SQL_DB_PROFILE_SERVERIP, dminfo.ServerIP);
		initialValues.put(DM_SQL_DB_PROFILE_PATH, dminfo.Path);
		initialValues.put(DM_SQL_DB_PROFILE_PROTOCOL_ORG, dminfo.Protocol_Org);
		initialValues.put(DM_SQL_DB_PROFILE_SERVERPORT_ORG, dminfo.ServerPort_Org);
		initialValues.put(DM_SQL_DB_PROFILE_SERVERURL_ORG, dminfo.ServerUrl_Org);
		initialValues.put(DM_SQL_DB_PROFILE_SERVERIP_ORG, dminfo.ServerIP_Org);
		initialValues.put(DM_SQL_DB_PROFILE_PATH_ORG, dminfo.Path_Org);
		initialValues.put(DM_SQL_DB_PROFILE_CHANGEDPROTOCOL, dminfo.bChangedProtocol);
		initialValues.put(DM_SQL_DB_PROFILE_OBEXTYPE, dminfo.ObexType);
		initialValues.put(DM_SQL_DB_PROFILE_AUTHTYPE, dminfo.AuthType);
		initialValues.put(DM_SQL_DB_PROFILE_SERVERAUTHTYPE, dminfo.nServerAuthType);
		initialValues.put(DM_SQL_DB_PROFILE_APPID, dminfo.AppID);
		initialValues.put(DM_SQL_DB_PROFILE_AUTHLEVEL, dminfo.AuthLevel);
		initialValues.put(DM_SQL_DB_PROFILE_SERVERAUTHLEVEL, dminfo.ServerAuthLevel);
		initialValues.put(DM_SQL_DB_PROFILE_PREFCONREF, dminfo.PrefConRef);
		initialValues.put(DM_SQL_DB_PROFILE_USERNAME, dminfo.UserName);
		initialValues.put(DM_SQL_DB_PROFILE_PASSWORD, dminfo.Password);
		initialValues.put(DM_SQL_DB_PROFILE_SERVERID, dminfo.ServerID);
		initialValues.put(DM_SQL_DB_PROFILE_SERVERPWD, dminfo.ServerPwd);
		initialValues.put(DM_SQL_DB_PROFILE_CLIENTNONCE, dminfo.ClientNonce);
		initialValues.put(DM_SQL_DB_PROFILE_SERVERNONCE, dminfo.ServerNonce);
		initialValues.put(DM_SQL_DB_PROFILE_SERVERNONCEFORMAT, dminfo.ServerNonceFormat);
		initialValues.put(DM_SQL_DB_PROFILE_CLIENTNONCEFORMAT, dminfo.ClientNonceFormat);
		initialValues.put(DM_SQL_DB_PROFILE_PROFILENAME, dminfo.ProfileName);
		initialValues.put(DM_SQL_DB_PROFILE_NETWORKCONNNAME, dminfo.NetworkConnName);
		initialValues.put(DM_SQL_DB_PROFILE_NETWORKCONNINDEX, dminfo.nNetworkConnIndex);
		initialValues.put(DM_SQL_DB_PROFILE_MAGICNUMBER, dminfo.MagicNumber);

		db.insert(DATABASE_PROFILE_TABLE, null, initialValues);
	}

	public static void deleteProfileRow(long rowId)
	{
		db.delete(DATABASE_PROFILE_TABLE, "rowid=" + rowId, null);
	}

	public static void updateProfileRow(long rowId, tsdmInfo dminfo)
	{
		ContentValues args = new ContentValues();

		args.put(DM_SQL_DB_PROFILE_PROTOCOL, dminfo.Protocol);
		args.put(DM_SQL_DB_PROFILE_SERVERPORT, dminfo.ServerPort);
		args.put(DM_SQL_DB_PROFILE_SERVERURL, dminfo.ServerUrl);
		args.put(DM_SQL_DB_PROFILE_SERVERIP, dminfo.ServerIP);
		args.put(DM_SQL_DB_PROFILE_PATH, dminfo.Path);
		args.put(DM_SQL_DB_PROFILE_PROTOCOL_ORG, dminfo.Protocol_Org);
		args.put(DM_SQL_DB_PROFILE_SERVERPORT_ORG, dminfo.ServerPort_Org);
		args.put(DM_SQL_DB_PROFILE_SERVERURL_ORG, dminfo.ServerUrl_Org);
		args.put(DM_SQL_DB_PROFILE_SERVERIP_ORG, dminfo.ServerIP_Org);
		args.put(DM_SQL_DB_PROFILE_PATH_ORG, dminfo.Path_Org);
		args.put(DM_SQL_DB_PROFILE_CHANGEDPROTOCOL, dminfo.bChangedProtocol);
		args.put(DM_SQL_DB_PROFILE_OBEXTYPE, dminfo.ObexType);
		args.put(DM_SQL_DB_PROFILE_AUTHTYPE, dminfo.AuthType);
		args.put(DM_SQL_DB_PROFILE_SERVERAUTHTYPE, dminfo.nServerAuthType);
		args.put(DM_SQL_DB_PROFILE_APPID, dminfo.AppID);
		args.put(DM_SQL_DB_PROFILE_AUTHLEVEL, dminfo.AuthLevel);
		args.put(DM_SQL_DB_PROFILE_SERVERAUTHLEVEL, dminfo.ServerAuthLevel);
		args.put(DM_SQL_DB_PROFILE_PREFCONREF, dminfo.PrefConRef);
		args.put(DM_SQL_DB_PROFILE_USERNAME, dminfo.UserName);
		args.put(DM_SQL_DB_PROFILE_PASSWORD, dminfo.Password);
		args.put(DM_SQL_DB_PROFILE_SERVERID, dminfo.ServerID);
		args.put(DM_SQL_DB_PROFILE_SERVERPWD, dminfo.ServerPwd);
		args.put(DM_SQL_DB_PROFILE_CLIENTNONCE, dminfo.ClientNonce);
		args.put(DM_SQL_DB_PROFILE_SERVERNONCE, dminfo.ServerNonce);
		args.put(DM_SQL_DB_PROFILE_SERVERNONCEFORMAT, dminfo.ServerNonceFormat);
		args.put(DM_SQL_DB_PROFILE_CLIENTNONCEFORMAT, dminfo.ClientNonceFormat);
		args.put(DM_SQL_DB_PROFILE_PROFILENAME, dminfo.ProfileName);
		args.put(DM_SQL_DB_PROFILE_NETWORKCONNNAME, dminfo.NetworkConnName);
		args.put(DM_SQL_DB_PROFILE_NETWORKCONNINDEX, dminfo.nNetworkConnIndex);
		args.put(DM_SQL_DB_PROFILE_MAGICNUMBER, dminfo.MagicNumber);

		db.update(DATABASE_PROFILE_TABLE, args, "rowid=" + rowId, null);
	}

	public static Object fetchProfileRow(long rowId, tsdmInfo dminfo)
	{
		String[] FROM = {
				DM_SQL_DB_ROWID,
				DM_SQL_DB_PROFILE_PROTOCOL,
				DM_SQL_DB_PROFILE_SERVERPORT,
				DM_SQL_DB_PROFILE_SERVERURL,
				DM_SQL_DB_PROFILE_SERVERIP,
				DM_SQL_DB_PROFILE_PATH,
				DM_SQL_DB_PROFILE_PROTOCOL_ORG,
				DM_SQL_DB_PROFILE_SERVERPORT_ORG,
				DM_SQL_DB_PROFILE_SERVERURL_ORG,
				DM_SQL_DB_PROFILE_SERVERIP_ORG,
				DM_SQL_DB_PROFILE_PATH_ORG,
				DM_SQL_DB_PROFILE_CHANGEDPROTOCOL,
				DM_SQL_DB_PROFILE_OBEXTYPE,
				DM_SQL_DB_PROFILE_AUTHTYPE,
				DM_SQL_DB_PROFILE_SERVERAUTHTYPE,
				DM_SQL_DB_PROFILE_APPID,
				DM_SQL_DB_PROFILE_AUTHLEVEL,
				DM_SQL_DB_PROFILE_SERVERAUTHLEVEL,
				DM_SQL_DB_PROFILE_PREFCONREF,
				DM_SQL_DB_PROFILE_USERNAME,
				DM_SQL_DB_PROFILE_PASSWORD,
				DM_SQL_DB_PROFILE_SERVERID,
				DM_SQL_DB_PROFILE_SERVERPWD,
				DM_SQL_DB_PROFILE_CLIENTNONCE,
				DM_SQL_DB_PROFILE_SERVERNONCE,
				DM_SQL_DB_PROFILE_SERVERNONCEFORMAT,
				DM_SQL_DB_PROFILE_CLIENTNONCEFORMAT,
				DM_SQL_DB_PROFILE_PROFILENAME,
				DM_SQL_DB_PROFILE_NETWORKCONNNAME,
				DM_SQL_DB_PROFILE_NETWORKCONNINDEX,
				DM_SQL_DB_PROFILE_MAGICNUMBER};

		Cursor cursor = db.query(true, DATABASE_PROFILE_TABLE, FROM, "rowid=" + rowId, null, null, null, null, null);

		if (cursor.getCount() > 0)
		{
			while (cursor.moveToNext())
			{
				@SuppressWarnings("unused")
				long id = cursor.getLong(0);
				dminfo.Protocol = cursor.getString(1);
				dminfo.ServerPort = cursor.getInt(2);
				dminfo.ServerUrl = cursor.getString(3);
				dminfo.ServerIP = cursor.getString(4);
				dminfo.Path = cursor.getString(5);
				dminfo.Protocol_Org = cursor.getString(6);
				dminfo.ServerPort_Org = cursor.getInt(7);
				dminfo.ServerUrl_Org = cursor.getString(8);
				dminfo.ServerIP_Org = cursor.getString(9);
				dminfo.Path_Org = cursor.getString(10);
				if (cursor.getInt(11) != 0)
					dminfo.bChangedProtocol = true;
				else
					dminfo.bChangedProtocol = false;
				dminfo.ObexType = cursor.getInt(12);
				dminfo.AuthType = cursor.getInt(13);
				dminfo.nServerAuthType = cursor.getInt(14);
				dminfo.AppID = cursor.getString(15);
				dminfo.AuthLevel = cursor.getString(16);
				dminfo.ServerAuthLevel = cursor.getString(17);
				dminfo.PrefConRef = cursor.getString(18);
				dminfo.UserName = cursor.getString(19);
				dminfo.Password = cursor.getString(20);
				dminfo.ServerID = cursor.getString(21);
				dminfo.ServerPwd = cursor.getString(22);
				dminfo.ClientNonce = cursor.getString(23);
				dminfo.ServerNonce = cursor.getString(24);
				dminfo.ServerNonceFormat = cursor.getInt(25);
				dminfo.ClientNonceFormat = cursor.getInt(26);
				dminfo.ProfileName = cursor.getString(27);
				dminfo.NetworkConnName = cursor.getString(28);
				dminfo.nNetworkConnIndex = cursor.getInt(29);
				dminfo.MagicNumber = cursor.getInt(30);
			}
		}
		cursor.close();
		return dminfo;
	}

	public static boolean existsProfileRow(long rowId)
	{
		String[] FROM = {
				DM_SQL_DB_ROWID,
				DM_SQL_DB_PROFILE_PROTOCOL,
				DM_SQL_DB_PROFILE_SERVERPORT,
				DM_SQL_DB_PROFILE_SERVERURL,
				DM_SQL_DB_PROFILE_SERVERIP,
				DM_SQL_DB_PROFILE_PATH,
				DM_SQL_DB_PROFILE_PROTOCOL_ORG,
				DM_SQL_DB_PROFILE_SERVERPORT_ORG,
				DM_SQL_DB_PROFILE_SERVERURL_ORG,
				DM_SQL_DB_PROFILE_SERVERIP_ORG,
				DM_SQL_DB_PROFILE_PATH_ORG,
				DM_SQL_DB_PROFILE_CHANGEDPROTOCOL,
				DM_SQL_DB_PROFILE_OBEXTYPE,
				DM_SQL_DB_PROFILE_AUTHTYPE,
				DM_SQL_DB_PROFILE_SERVERAUTHTYPE,
				DM_SQL_DB_PROFILE_APPID,
				DM_SQL_DB_PROFILE_AUTHLEVEL,
				DM_SQL_DB_PROFILE_SERVERAUTHLEVEL,
				DM_SQL_DB_PROFILE_PREFCONREF,
				DM_SQL_DB_PROFILE_USERNAME,
				DM_SQL_DB_PROFILE_PASSWORD,
				DM_SQL_DB_PROFILE_SERVERID,
				DM_SQL_DB_PROFILE_SERVERPWD,
				DM_SQL_DB_PROFILE_CLIENTNONCE,
				DM_SQL_DB_PROFILE_SERVERNONCE,
				DM_SQL_DB_PROFILE_SERVERNONCEFORMAT,
				DM_SQL_DB_PROFILE_CLIENTNONCEFORMAT,
				DM_SQL_DB_PROFILE_PROFILENAME,
				DM_SQL_DB_PROFILE_NETWORKCONNNAME,
				DM_SQL_DB_PROFILE_NETWORKCONNINDEX,
				DM_SQL_DB_PROFILE_MAGICNUMBER};

		Cursor cursor = db.query(true, DATABASE_PROFILE_TABLE, FROM, "rowid=" + rowId, null, null, null, null, null);

		if (cursor.getCount() > 0)
		{
			cursor.close();
			return true;
		}
		else
		{
			cursor.close();
			return false;
		}
	}

	public static void insertNetworkRow(tsdmInfoConRef ConRef)
	{
		ContentValues initialValues = new ContentValues();

		initialValues.put(DM_SQL_DB_NETWORK_HOMEURL, ConRef.szHomeUrl);
		initialValues.put(DM_SQL_DB_NETWORK_SERVICE, ConRef.nService);
		initialValues.put(DM_SQL_DB_NETWORK_ACTIVE, ConRef.Active);
		initialValues.put(DM_SQL_DB_NETWORK_PROXYUSE, ConRef.bProxyUse);
		initialValues.put(DM_SQL_DB_NETWORK_NAP_NETWORKPROFILENAME, ConRef.NAP.NetworkProfileName);
		initialValues.put(DM_SQL_DB_NETWORK_NAP_BEARER, ConRef.NAP.nBearer);
		initialValues.put(DM_SQL_DB_NETWORK_NAP_ADDRTYPE, ConRef.NAP.nAddrType);
		initialValues.put(DM_SQL_DB_NETWORK_NAP_ADDR, ConRef.NAP.Addr);
		initialValues.put(DM_SQL_DB_NETWORK_NAP_AUTH_PAPID, ConRef.NAP.Auth.PAP_ID);
		initialValues.put(DM_SQL_DB_NETWORK_NAP_AUTH_PAPSECRET, ConRef.NAP.Auth.PAP_Secret);
		initialValues.put(DM_SQL_DB_NETWORK_PX_PORTNBR, ConRef.PX.nPortNbr);
		initialValues.put(DM_SQL_DB_NETWORK_PX_ADDRTYPE, ConRef.PX.nAddrType);
		initialValues.put(DM_SQL_DB_NETWORK_PX_ADDR, ConRef.PX.Addr);
		initialValues.put(DM_SQL_DB_NETWORK_PX_AUTH_PAPID, ConRef.PX.Auth.PAP_ID);
		initialValues.put(DM_SQL_DB_NETWORK_PX_AUTH_PAPSECRET, ConRef.PX.Auth.PAP_Secret);
		initialValues.put(DM_SQL_DB_NETWORK_ADVSETTING_STATICIPUSE, ConRef.tAdvSetting.bStaticIpUse);
		initialValues.put(DM_SQL_DB_NETWORK_ADVSETTING_STATICIP, ConRef.tAdvSetting.szStaticIp);
		initialValues.put(DM_SQL_DB_NETWORK_ADVSETTING_STATICDNSUSE, ConRef.tAdvSetting.bStaticDnsUse);
		initialValues.put(DM_SQL_DB_NETWORK_ADVSETTING_STATICDNS1, ConRef.tAdvSetting.szStaticDns1);
		initialValues.put(DM_SQL_DB_NETWORK_ADVSETTING_STATICDNS2, ConRef.tAdvSetting.szStaticDns2);
		initialValues.put(DM_SQL_DB_NETWORK_ADVSETTING_TRAFFICCLASS, ConRef.tAdvSetting.nTrafficClass);

		db.insert(DATABASE_NETWORK_TABLE, null, initialValues);
	}

	public static void deleteNetworkRow(long rowId)
	{
		db.delete(DATABASE_NETWORK_TABLE, "rowid=" + rowId, null);
	}

	public static void updateNetworkRow(long rowId, tsdmInfoConRef conref)
	{
		ContentValues args = new ContentValues();

		args.put(DM_SQL_DB_NETWORK_HOMEURL, conref.szHomeUrl);
		args.put(DM_SQL_DB_NETWORK_SERVICE, conref.nService);
		args.put(DM_SQL_DB_NETWORK_ACTIVE, conref.Active);
		args.put(DM_SQL_DB_NETWORK_PROXYUSE, conref.bProxyUse);
		args.put(DM_SQL_DB_NETWORK_NAP_NETWORKPROFILENAME, conref.NAP.NetworkProfileName);
		args.put(DM_SQL_DB_NETWORK_NAP_BEARER, conref.NAP.nBearer);
		args.put(DM_SQL_DB_NETWORK_NAP_ADDRTYPE, conref.NAP.nAddrType);
		args.put(DM_SQL_DB_NETWORK_NAP_ADDR, conref.NAP.Addr);
		args.put(DM_SQL_DB_NETWORK_NAP_AUTH_PAPID, conref.NAP.Auth.PAP_ID);
		args.put(DM_SQL_DB_NETWORK_NAP_AUTH_PAPSECRET, conref.NAP.Auth.PAP_Secret);
		args.put(DM_SQL_DB_NETWORK_PX_PORTNBR, conref.PX.nPortNbr);
		args.put(DM_SQL_DB_NETWORK_PX_ADDRTYPE, conref.PX.nAddrType);
		args.put(DM_SQL_DB_NETWORK_PX_ADDR, conref.PX.Addr);
		args.put(DM_SQL_DB_NETWORK_PX_AUTH_PAPID, conref.PX.Auth.PAP_ID);
		args.put(DM_SQL_DB_NETWORK_PX_AUTH_PAPSECRET, conref.PX.Auth.PAP_Secret);
		args.put(DM_SQL_DB_NETWORK_ADVSETTING_STATICIPUSE, conref.tAdvSetting.bStaticIpUse);
		args.put(DM_SQL_DB_NETWORK_ADVSETTING_STATICIP, conref.tAdvSetting.szStaticIp);
		args.put(DM_SQL_DB_NETWORK_ADVSETTING_STATICDNSUSE, conref.tAdvSetting.bStaticDnsUse);
		args.put(DM_SQL_DB_NETWORK_ADVSETTING_STATICDNS1, conref.tAdvSetting.szStaticDns1);
		args.put(DM_SQL_DB_NETWORK_ADVSETTING_STATICDNS2, conref.tAdvSetting.szStaticDns2);
		args.put(DM_SQL_DB_NETWORK_ADVSETTING_TRAFFICCLASS, conref.tAdvSetting.nTrafficClass);

		db.update(DATABASE_NETWORK_TABLE, args, "rowid=" + rowId, null);
	}

	public static Object fetchNetworkRow(long rowId, tsdmInfoConRef conref)
	{
		String[] FROM = {
				DM_SQL_DB_ROWID,
				DM_SQL_DB_NETWORK_HOMEURL,
				DM_SQL_DB_NETWORK_SERVICE,
				DM_SQL_DB_NETWORK_ACTIVE,
				DM_SQL_DB_NETWORK_PROXYUSE,
				DM_SQL_DB_NETWORK_NAP_NETWORKPROFILENAME,
				DM_SQL_DB_NETWORK_NAP_BEARER,
				DM_SQL_DB_NETWORK_NAP_ADDRTYPE,
				DM_SQL_DB_NETWORK_NAP_ADDR,
				DM_SQL_DB_NETWORK_NAP_AUTH_PAPID,
				DM_SQL_DB_NETWORK_NAP_AUTH_PAPSECRET,
				DM_SQL_DB_NETWORK_PX_PORTNBR,
				DM_SQL_DB_NETWORK_PX_ADDRTYPE,
				DM_SQL_DB_NETWORK_PX_ADDR,
				DM_SQL_DB_NETWORK_PX_AUTH_PAPID,
				DM_SQL_DB_NETWORK_PX_AUTH_PAPSECRET,
				DM_SQL_DB_NETWORK_ADVSETTING_STATICIPUSE,
				DM_SQL_DB_NETWORK_ADVSETTING_STATICIP,
				DM_SQL_DB_NETWORK_ADVSETTING_STATICDNSUSE,
				DM_SQL_DB_NETWORK_ADVSETTING_STATICDNS1,
				DM_SQL_DB_NETWORK_ADVSETTING_STATICDNS2,
				DM_SQL_DB_NETWORK_ADVSETTING_TRAFFICCLASS};

		Cursor cursor = db.query(true, DATABASE_NETWORK_TABLE, FROM, "rowid=" + rowId, null, null, null, null, null);

		if (cursor.getCount() > 0)
		{
			while (cursor.moveToNext())
			{
				@SuppressWarnings("unused")
				long id = cursor.getLong(0);
				conref.szHomeUrl = cursor.getString(1);
				conref.nService = cursor.getInt(2);
				if (cursor.getInt(3) != 0)
					conref.Active = true;
				else
					conref.Active = false;
				if (cursor.getInt(4) != 0)
					conref.bProxyUse = true;
				else
					conref.bProxyUse = false;
				conref.NAP.NetworkProfileName = cursor.getString(5);
				conref.NAP.nBearer = cursor.getInt(6);
				conref.NAP.nAddrType = cursor.getInt(7);
				conref.NAP.Addr = cursor.getString(8);
				conref.NAP.Auth.PAP_ID = cursor.getString(9);
				conref.NAP.Auth.PAP_Secret = cursor.getString(10);
				conref.PX.nPortNbr = cursor.getInt(11);
				conref.PX.nAddrType = cursor.getInt(12);
				conref.PX.Addr = cursor.getString(13);
				conref.PX.Auth.PAP_ID = cursor.getString(14);
				conref.PX.Auth.PAP_Secret = cursor.getString(15);
				if (cursor.getInt(16) != 0)
					conref.tAdvSetting.bStaticIpUse = true;
				else
					conref.tAdvSetting.bStaticIpUse = false;
				conref.tAdvSetting.szStaticIp = cursor.getString(17);
				if (cursor.getInt(18) != 0)
					conref.tAdvSetting.bStaticDnsUse = true;
				else
					conref.tAdvSetting.bStaticDnsUse = false;
				conref.tAdvSetting.szStaticDns1 = cursor.getInt(19);
				conref.tAdvSetting.szStaticDns2 = cursor.getInt(20);
				conref.tAdvSetting.nTrafficClass = cursor.getInt(21);
			}
		}
		cursor.close();
		return conref;
	}

	public static boolean existsNetworkRow(long rowId)
	{
		String[] FROM = {
				DM_SQL_DB_ROWID,
				DM_SQL_DB_NETWORK_HOMEURL,
				DM_SQL_DB_NETWORK_SERVICE,
				DM_SQL_DB_NETWORK_ACTIVE,
				DM_SQL_DB_NETWORK_PROXYUSE,
				DM_SQL_DB_NETWORK_NAP_NETWORKPROFILENAME,
				DM_SQL_DB_NETWORK_NAP_BEARER,
				DM_SQL_DB_NETWORK_NAP_ADDRTYPE,
				DM_SQL_DB_NETWORK_NAP_ADDR,
				DM_SQL_DB_NETWORK_NAP_AUTH_PAPID,
				DM_SQL_DB_NETWORK_NAP_AUTH_PAPSECRET,
				DM_SQL_DB_NETWORK_PX_PORTNBR,
				DM_SQL_DB_NETWORK_PX_ADDRTYPE,
				DM_SQL_DB_NETWORK_PX_ADDR,
				DM_SQL_DB_NETWORK_PX_AUTH_PAPID,
				DM_SQL_DB_NETWORK_PX_AUTH_PAPSECRET,
				DM_SQL_DB_NETWORK_ADVSETTING_STATICIPUSE,
				DM_SQL_DB_NETWORK_ADVSETTING_STATICIP,
				DM_SQL_DB_NETWORK_ADVSETTING_STATICDNSUSE,
				DM_SQL_DB_NETWORK_ADVSETTING_STATICDNS1,
				DM_SQL_DB_NETWORK_ADVSETTING_STATICDNS2,
				DM_SQL_DB_NETWORK_ADVSETTING_TRAFFICCLASS};

		try
		{
			Cursor cursor = db.query(true, DATABASE_NETWORK_TABLE, FROM, "rowid=" + rowId, null, null, null, null, null);

			if (cursor.getCount() > 0)
			{
				cursor.close();
				return true;
			}
			else
			{
				cursor.close();
				return false;
			}
		}
		catch (NullPointerException ex)
		{
			tsLib.debugPrintException(DEBUG_DB, "db not initialized " + ex.toString());
			return false;
		}
	}

	public static void insertProfileListRow(tsdmProflieList profilelist)
	{
		ContentValues initialValues = new ContentValues();

		initialValues.put(DM_SQL_DB_PROFILELIST_NETWORKCONNNAME, profilelist.NetworkConnName);
		initialValues.put(DM_SQL_DB_PROFILELIST_PROXYINDEX, profilelist.nProxyIndex);
		initialValues.put(DM_SQL_DB_PROFILELIST_PROFILEINDEX, profilelist.Profileindex);
		initialValues.put(DM_SQL_DB_PROFILELIST_PROFILENAME1, profilelist.ProfileName[0]);
		initialValues.put(DM_SQL_DB_PROFILELIST_PROFILENAME2, profilelist.ProfileName[1]);
		initialValues.put(DM_SQL_DB_PROFILELIST_PROFILENAME3, profilelist.ProfileName[2]);
		initialValues.put(DM_SQL_DB_PROFILELIST_SESSIONID, profilelist.nSessionID);
		initialValues.put(DM_SQL_DB_PROFILELIST_NOTIEVENT, profilelist.nNotiEvent);
		initialValues.put(DM_SQL_DB_PROFILELIST_DESTORYNOTITIME, profilelist.nDestoryNotiTime);
		initialValues.put(DM_SQL_DB_PROFILELIST_NOTIRESYNCMODE, profilelist.nNotiReSyncMode);
		initialValues.put(DM_SQL_DB_PROFILELIST_DDFPARSERNODEINDEX, profilelist.nDDFParserNodeIndex);
		initialValues.put(DM_SQL_DB_PROFILELIST_SKIPDEVDISCOVERY, profilelist.bSkipDevDiscovery);
		initialValues.put(DM_SQL_DB_PROFILELIST_MAGICNUMBER, profilelist.MagicNumber);
		initialValues.put(DM_SQL_DB_PROFILELIST_NOTIRESUMESTATE_SESSIONSAVESTATE, profilelist.NotiResumeState.nSessionSaveState);
		initialValues.put(DM_SQL_DB_PROFILELIST_NOTIRESUMESTATE_NOTIUIEVENT, profilelist.NotiResumeState.nNotiUiEvent);
		initialValues.put(DM_SQL_DB_PROFILELIST_NOTIRESUMESTATE_NOTIRETRYCOUNT, profilelist.NotiResumeState.nNotiRetryCount);
		initialValues.put(DM_SQL_DB_PROFILELIST_UICRESULTKEEP_STATUS, profilelist.tUicResultKeep.eStatus);
		initialValues.put(DM_SQL_DB_PROFILELIST_UICRESULTKEEP_APPID, profilelist.tUicResultKeep.appId);
		initialValues.put(DM_SQL_DB_PROFILELIST_UICRESULTKEEP_UICTYPE, profilelist.tUicResultKeep.UICType);
		initialValues.put(DM_SQL_DB_PROFILELIST_UICRESULTKEEP_RESULT, profilelist.tUicResultKeep.result);
		initialValues.put(DM_SQL_DB_PROFILELIST_UICRESULTKEEP_NUMBER, profilelist.tUicResultKeep.number);
		initialValues.put(DM_SQL_DB_PROFILELIST_UICRESULTKEEP_TEXT, profilelist.tUicResultKeep.szText);
		initialValues.put(DM_SQL_DB_PROFILELIST_UICRESULTKEEP_LEN, profilelist.tUicResultKeep.nLen);
		initialValues.put(DM_SQL_DB_PROFILELIST_UICRESULTKEEP_SIZE, profilelist.tUicResultKeep.nSize);
		initialValues.put(DM_SQL_DB_PROFILELIST_IMEI, profilelist.szImei);
		initialValues.put(DM_SQL_DB_PROFILELIST_WIFIONLY,profilelist.bWifiOnly);
		initialValues.put(DM_SQL_DB_PROFILELIST_AUTOUPDATE,profilelist.bAutoUpdate);
		initialValues.put(DM_SQL_DB_PROFILELIST_PUSHMESSAGE,profilelist.bPushMessage);
		initialValues.put(DM_SQL_DB_PROFILELIST_AUTOUPDATETIME,profilelist.nAutoUpdateTime);
		initialValues.put(DM_SQL_DB_PROFILELIST_SAVE_DELTAFILE_INDEX,profilelist.nSaveDeltaFileIndex);
		initialValues.put(DM_SQL_DB_PROFILELIST_NEXTUPDATETIME,profilelist.lNextUpdateTime);
		initialValues.put(DM_SQL_DB_PROFILELIST_AUTOUPDATECHECK,profilelist.bAutoCheck);
		initialValues.put(DM_SQL_DB_PROFILELIST_AGENT_MODE,profilelist.bAgentMode);
		initialValues.put(DM_SQL_DB_PROFILELIST_CURR_CHECK_TIME,profilelist.lcurrCheckTime);
		db.insert(DATABASE_PROFILELIST_TABLE, null, initialValues);
	}

	public static void deleteProfileListRow(long rowId)
	{
		db.delete(DATABASE_PROFILELIST_TABLE, "rowid=" + rowId, null);
	}

	public static void updateProfileListRow(long rowId, tsdmProflieList profilelist)
	{
		ContentValues args = new ContentValues();

		args.put(DM_SQL_DB_PROFILELIST_NETWORKCONNNAME, profilelist.NetworkConnName);
		args.put(DM_SQL_DB_PROFILELIST_PROXYINDEX, profilelist.nProxyIndex);
		args.put(DM_SQL_DB_PROFILELIST_PROFILEINDEX, profilelist.Profileindex);
		args.put(DM_SQL_DB_PROFILELIST_PROFILENAME1, profilelist.ProfileName[0]);
		args.put(DM_SQL_DB_PROFILELIST_PROFILENAME2, profilelist.ProfileName[1]);
		args.put(DM_SQL_DB_PROFILELIST_PROFILENAME3, profilelist.ProfileName[2]);
		args.put(DM_SQL_DB_PROFILELIST_SESSIONID, profilelist.nSessionID);
		args.put(DM_SQL_DB_PROFILELIST_NOTIEVENT, profilelist.nNotiEvent);
		args.put(DM_SQL_DB_PROFILELIST_DESTORYNOTITIME, profilelist.nDestoryNotiTime);
		args.put(DM_SQL_DB_PROFILELIST_NOTIRESYNCMODE, profilelist.nNotiReSyncMode);
		args.put(DM_SQL_DB_PROFILELIST_DDFPARSERNODEINDEX, profilelist.nDDFParserNodeIndex);
		args.put(DM_SQL_DB_PROFILELIST_SKIPDEVDISCOVERY, profilelist.bSkipDevDiscovery);
		args.put(DM_SQL_DB_PROFILELIST_MAGICNUMBER, profilelist.MagicNumber);
		args.put(DM_SQL_DB_PROFILELIST_NOTIRESUMESTATE_SESSIONSAVESTATE, profilelist.NotiResumeState.nSessionSaveState);
		args.put(DM_SQL_DB_PROFILELIST_NOTIRESUMESTATE_NOTIUIEVENT, profilelist.NotiResumeState.nNotiUiEvent);
		args.put(DM_SQL_DB_PROFILELIST_NOTIRESUMESTATE_NOTIRETRYCOUNT, profilelist.NotiResumeState.nNotiRetryCount);
		args.put(DM_SQL_DB_PROFILELIST_UICRESULTKEEP_STATUS, profilelist.tUicResultKeep.eStatus);
		args.put(DM_SQL_DB_PROFILELIST_UICRESULTKEEP_APPID, profilelist.tUicResultKeep.appId);
		args.put(DM_SQL_DB_PROFILELIST_UICRESULTKEEP_UICTYPE, profilelist.tUicResultKeep.UICType);
		args.put(DM_SQL_DB_PROFILELIST_UICRESULTKEEP_RESULT, profilelist.tUicResultKeep.result);
		args.put(DM_SQL_DB_PROFILELIST_UICRESULTKEEP_NUMBER, profilelist.tUicResultKeep.number);
		args.put(DM_SQL_DB_PROFILELIST_UICRESULTKEEP_TEXT, profilelist.tUicResultKeep.szText);
		args.put(DM_SQL_DB_PROFILELIST_UICRESULTKEEP_LEN, profilelist.tUicResultKeep.nLen);
		args.put(DM_SQL_DB_PROFILELIST_UICRESULTKEEP_SIZE, profilelist.tUicResultKeep.nSize);
		args.put(DM_SQL_DB_PROFILELIST_IMEI, profilelist.szImei);
		args.put(DM_SQL_DB_PROFILELIST_WIFIONLY, profilelist.bWifiOnly);
		args.put(DM_SQL_DB_PROFILELIST_AUTOUPDATE, profilelist.bAutoUpdate);
		args.put(DM_SQL_DB_PROFILELIST_PUSHMESSAGE, profilelist.bPushMessage);
		args.put(DM_SQL_DB_PROFILELIST_AUTOUPDATETIME,profilelist.nAutoUpdateTime);
		args.put(DM_SQL_DB_PROFILELIST_SAVE_DELTAFILE_INDEX,profilelist.nSaveDeltaFileIndex);
		args.put(DM_SQL_DB_PROFILELIST_NEXTUPDATETIME,profilelist.lNextUpdateTime);
		args.put(DM_SQL_DB_PROFILELIST_AUTOUPDATECHECK,profilelist.bAutoCheck);
		args.put(DM_SQL_DB_PROFILELIST_AGENT_MODE,profilelist.bAgentMode);
		args.put(DM_SQL_DB_PROFILELIST_CURR_CHECK_TIME,profilelist.lcurrCheckTime);
		db.update(DATABASE_PROFILELIST_TABLE, args, "rowid=" + rowId, null);
	}

	public static Object fetchProfileListRow(long rowId, tsdmProflieList profilelist)
	{
		String[] FROM = {
				DM_SQL_DB_ROWID,
				DM_SQL_DB_PROFILELIST_NETWORKCONNNAME,
				DM_SQL_DB_PROFILELIST_PROXYINDEX,
				DM_SQL_DB_PROFILELIST_PROFILEINDEX,
				DM_SQL_DB_PROFILELIST_PROFILENAME1,
				DM_SQL_DB_PROFILELIST_PROFILENAME2,
				DM_SQL_DB_PROFILELIST_PROFILENAME3,
				DM_SQL_DB_PROFILELIST_SESSIONID,
				DM_SQL_DB_PROFILELIST_NOTIEVENT,
				DM_SQL_DB_PROFILELIST_DESTORYNOTITIME,
				DM_SQL_DB_PROFILELIST_NOTIRESYNCMODE,
				DM_SQL_DB_PROFILELIST_DDFPARSERNODEINDEX,
				DM_SQL_DB_PROFILELIST_SKIPDEVDISCOVERY,
				DM_SQL_DB_PROFILELIST_MAGICNUMBER,
				DM_SQL_DB_PROFILELIST_NOTIRESUMESTATE_SESSIONSAVESTATE,
				DM_SQL_DB_PROFILELIST_NOTIRESUMESTATE_NOTIUIEVENT,
				DM_SQL_DB_PROFILELIST_NOTIRESUMESTATE_NOTIRETRYCOUNT,
				DM_SQL_DB_PROFILELIST_UICRESULTKEEP_STATUS,
				DM_SQL_DB_PROFILELIST_UICRESULTKEEP_APPID,
				DM_SQL_DB_PROFILELIST_UICRESULTKEEP_UICTYPE,
				DM_SQL_DB_PROFILELIST_UICRESULTKEEP_RESULT,
				DM_SQL_DB_PROFILELIST_UICRESULTKEEP_NUMBER,
				DM_SQL_DB_PROFILELIST_UICRESULTKEEP_TEXT,
				DM_SQL_DB_PROFILELIST_UICRESULTKEEP_LEN,
				DM_SQL_DB_PROFILELIST_UICRESULTKEEP_SIZE,
				DM_SQL_DB_PROFILELIST_IMEI,
				DM_SQL_DB_PROFILELIST_WIFIONLY,
				DM_SQL_DB_PROFILELIST_AUTOUPDATE,
				DM_SQL_DB_PROFILELIST_PUSHMESSAGE,
				DM_SQL_DB_PROFILELIST_AUTOUPDATETIME,
				DM_SQL_DB_PROFILELIST_SAVE_DELTAFILE_INDEX,
				DM_SQL_DB_PROFILELIST_NEXTUPDATETIME,
				DM_SQL_DB_PROFILELIST_AUTOUPDATECHECK,
				DM_SQL_DB_PROFILELIST_AGENT_MODE,
				DM_SQL_DB_PROFILELIST_CURR_CHECK_TIME};

		Cursor cursor = db.query(true, DATABASE_PROFILELIST_TABLE, FROM, "rowid=" + rowId, null, null, null, null, null);

		if (cursor.getCount() > 0)
		{
			while (cursor.moveToNext())
			{
				@SuppressWarnings("unused")
				long id = cursor.getLong(0);
				profilelist.NetworkConnName = cursor.getString(1);
				profilelist.nProxyIndex = cursor.getInt(2);
				profilelist.Profileindex = cursor.getInt(3);
				profilelist.ProfileName[0] = cursor.getString(4);
				profilelist.ProfileName[1] = cursor.getString(5);
				profilelist.ProfileName[2] = cursor.getString(6);
				profilelist.nSessionID = cursor.getString(7);
				profilelist.nNotiEvent = cursor.getInt(8);
				profilelist.nDestoryNotiTime = cursor.getInt(9);
				profilelist.nNotiReSyncMode = cursor.getInt(10);
				profilelist.nDDFParserNodeIndex = cursor.getInt(11);
				if (cursor.getInt(12) != 0)
					profilelist.bSkipDevDiscovery = true;
				else
					profilelist.bSkipDevDiscovery = false;
				profilelist.MagicNumber = cursor.getInt(13);
				profilelist.NotiResumeState.nSessionSaveState = cursor.getInt(14);
				profilelist.NotiResumeState.nNotiUiEvent = cursor.getInt(15);
				profilelist.NotiResumeState.nNotiRetryCount = cursor.getInt(16);
				profilelist.tUicResultKeep.eStatus = cursor.getInt(17);
				profilelist.tUicResultKeep.appId = cursor.getInt(18);
				profilelist.tUicResultKeep.UICType = cursor.getInt(19);
				profilelist.tUicResultKeep.result = cursor.getInt(20);
				profilelist.tUicResultKeep.number = cursor.getInt(21);
				profilelist.tUicResultKeep.szText = cursor.getString(22);
				profilelist.tUicResultKeep.nLen = cursor.getInt(23);
				profilelist.tUicResultKeep.nSize = cursor.getInt(24);
				profilelist.szImei = cursor.getString(25);
				if (cursor.getInt(26) != 0)
					profilelist.bWifiOnly = true;
				else
					profilelist.bWifiOnly = false;
				if (cursor.getInt(27) != 0)
					profilelist.bAutoUpdate = true;
				else
					profilelist.bAutoUpdate = false;
				if (cursor.getInt(28) != 0)
					profilelist.bPushMessage = true;
				else
					profilelist.bPushMessage = false;
				profilelist.nAutoUpdateTime = cursor.getLong(29);
				profilelist.nSaveDeltaFileIndex = cursor.getInt(30);
				profilelist.lNextUpdateTime = cursor.getLong(31);
				if (cursor.getLong(32) != 0)
					profilelist.bAutoCheck = true;
				else
					profilelist.bAutoCheck = false;
				if (cursor.getLong(33) != 0)
					profilelist.bAgentMode = true;
				else
					profilelist.bAgentMode = false;
				profilelist.lcurrCheckTime = cursor.getLong(34);
			}
		}
		cursor.close();
		return profilelist;
	}

	public static boolean existsProfileListRow(long rowId)
	{
		String[] FROM = {
				DM_SQL_DB_ROWID,
				DM_SQL_DB_PROFILELIST_NETWORKCONNNAME,
				DM_SQL_DB_PROFILELIST_PROXYINDEX,
				DM_SQL_DB_PROFILELIST_PROFILEINDEX,
				DM_SQL_DB_PROFILELIST_PROFILENAME1,
				DM_SQL_DB_PROFILELIST_PROFILENAME2,
				DM_SQL_DB_PROFILELIST_PROFILENAME3,
				DM_SQL_DB_PROFILELIST_SESSIONID,
				DM_SQL_DB_PROFILELIST_NOTIEVENT,
				DM_SQL_DB_PROFILELIST_DESTORYNOTITIME,
				DM_SQL_DB_PROFILELIST_NOTIRESYNCMODE,
				DM_SQL_DB_PROFILELIST_DDFPARSERNODEINDEX,
				DM_SQL_DB_PROFILELIST_SKIPDEVDISCOVERY,
				DM_SQL_DB_PROFILELIST_MAGICNUMBER,
				DM_SQL_DB_PROFILELIST_NOTIRESUMESTATE_SESSIONSAVESTATE,
				DM_SQL_DB_PROFILELIST_NOTIRESUMESTATE_NOTIUIEVENT,
				DM_SQL_DB_PROFILELIST_NOTIRESUMESTATE_NOTIRETRYCOUNT,
				DM_SQL_DB_PROFILELIST_UICRESULTKEEP_STATUS,
				DM_SQL_DB_PROFILELIST_UICRESULTKEEP_APPID,
				DM_SQL_DB_PROFILELIST_UICRESULTKEEP_UICTYPE,
				DM_SQL_DB_PROFILELIST_UICRESULTKEEP_RESULT,
				DM_SQL_DB_PROFILELIST_UICRESULTKEEP_NUMBER,
				DM_SQL_DB_PROFILELIST_UICRESULTKEEP_TEXT,
				DM_SQL_DB_PROFILELIST_UICRESULTKEEP_LEN,
				DM_SQL_DB_PROFILELIST_UICRESULTKEEP_SIZE,
				DM_SQL_DB_PROFILELIST_IMEI,
				DM_SQL_DB_PROFILELIST_WIFIONLY,
				DM_SQL_DB_PROFILELIST_AUTOUPDATE,
				DM_SQL_DB_PROFILELIST_PUSHMESSAGE,
				DM_SQL_DB_PROFILELIST_AUTOUPDATETIME,
				DM_SQL_DB_PROFILELIST_SAVE_DELTAFILE_INDEX,
				DM_SQL_DB_PROFILELIST_NEXTUPDATETIME,
				DM_SQL_DB_PROFILELIST_AUTOUPDATECHECK,
				DM_SQL_DB_PROFILELIST_AGENT_MODE,
				DM_SQL_DB_PROFILELIST_CURR_CHECK_TIME};
				
		Cursor cursor = db.query(true, DATABASE_PROFILELIST_TABLE, FROM, "rowid=" + rowId, null, null, null, null, null);

		if (cursor.getCount() > 0)
		{
			cursor.close();
			return true;
		}
		else
		{
			cursor.close();
			return false;
		}
	}

	public static void insertFUMORow(tsDBFumoInfo fumoinfo)
	{
		ContentValues initialValues = new ContentValues();

		initialValues.put(DM_SQL_DB_FUMO_PROTOCOL, fumoinfo.Protocol);
		initialValues.put(DM_SQL_DB_FUMO_OBEXTYPE, fumoinfo.ObexType);
		initialValues.put(DM_SQL_DB_FUMO_AUTHTYPE, fumoinfo.AuthType);
		initialValues.put(DM_SQL_DB_FUMO_SERVERPORT, fumoinfo.ServerPort);
		initialValues.put(DM_SQL_DB_FUMO_SERVERURL, fumoinfo.ServerUrl);
		initialValues.put(DM_SQL_DB_FUMO_SERVERIP, fumoinfo.ServerIP);
		initialValues.put(DM_SQL_DB_FUMO_OBJECTDOWNLOADPROTOCOL, fumoinfo.ObjectDownloadProtocol);
		initialValues.put(DM_SQL_DB_FUMO_OBJECTDOWNLOADURL, fumoinfo.ObjectDownloadUrl);
		initialValues.put(DM_SQL_DB_FUMO_OBJECTDOWNLOADIP, fumoinfo.ObjectDownloadIP);
		initialValues.put(DM_SQL_DB_FUMO_OBJECTDOWNLOADPORT, fumoinfo.nObjectDownloadPort);
		initialValues.put(DM_SQL_DB_FUMO_STATUSNOTIFYPROTOCOL, fumoinfo.StatusNotifyProtocol);
		initialValues.put(DM_SQL_DB_FUMO_STATUSNOTIFYURL, fumoinfo.StatusNotifyUrl);
		initialValues.put(DM_SQL_DB_FUMO_STATUSNOTIFYIP, fumoinfo.StatusNotifyIP);
		initialValues.put(DM_SQL_DB_FUMO_STATUSNOTIFYPORT, fumoinfo.nStatusNotifyPort);
		initialValues.put(DM_SQL_DB_FUMO_REPORTURI, fumoinfo.ReportURI);
		initialValues.put(DM_SQL_DB_FUMO_OBJECTSIZE, fumoinfo.nObjectSize);
		initialValues.put(DM_SQL_DB_FUMO_FFSWRITESIZE, fumoinfo.nFFSWriteSize);
		initialValues.put(DM_SQL_DB_FUMO_STATUS, fumoinfo.nStatus);
		initialValues.put(DM_SQL_DB_FUMO_STATUSNODENAME, fumoinfo.StatusNodeName);
		initialValues.put(DM_SQL_DB_FUMO_RESULTCODE, fumoinfo.ResultCode);
		initialValues.put(DM_SQL_DB_FUMO_UPDATEMECHANISM, fumoinfo.nUpdateMechanism);
		initialValues.put(DM_SQL_DB_FUMO_DOWNLOADMODE, fumoinfo.nDownloadMode);
		initialValues.put(DM_SQL_DB_FUMO_CORRELATOR, fumoinfo.Correlator);
		initialValues.put(DM_SQL_DB_FUMO_CONTENTTYPE, fumoinfo.szContentType);
		initialValues.put(DM_SQL_DB_FUMO_ACCEPTTYPE, fumoinfo.szAcceptType);
		initialValues.put(DM_SQL_DB_FUMO_UPDATEWAIT, fumoinfo.bUpdateWait);

		db.insert(DATABASE_FUMO_TABLE, null, initialValues);
	}

	public static void deleteFUMORow(long rowId)
	{
		db.delete(DATABASE_FUMO_TABLE, "rowid=" + rowId, null);
	}

	public static Object fetchFUMORow(long rowId, tsDBFumoInfo fumoinfo)
	{
		String[] FROM = {
				DM_SQL_DB_ROWID,
				DM_SQL_DB_FUMO_PROTOCOL,
				DM_SQL_DB_FUMO_OBEXTYPE,
				DM_SQL_DB_FUMO_AUTHTYPE,
				DM_SQL_DB_FUMO_SERVERPORT,
				DM_SQL_DB_FUMO_SERVERURL,
				DM_SQL_DB_FUMO_SERVERIP,
				DM_SQL_DB_FUMO_OBJECTDOWNLOADPROTOCOL,
				DM_SQL_DB_FUMO_OBJECTDOWNLOADURL,
				DM_SQL_DB_FUMO_OBJECTDOWNLOADIP,
				DM_SQL_DB_FUMO_OBJECTDOWNLOADPORT,
				DM_SQL_DB_FUMO_STATUSNOTIFYPROTOCOL,
				DM_SQL_DB_FUMO_STATUSNOTIFYURL,
				DM_SQL_DB_FUMO_STATUSNOTIFYIP,
				DM_SQL_DB_FUMO_STATUSNOTIFYPORT,
				DM_SQL_DB_FUMO_REPORTURI,
				DM_SQL_DB_FUMO_OBJECTSIZE,
				DM_SQL_DB_FUMO_FFSWRITESIZE,
				DM_SQL_DB_FUMO_STATUS,
				DM_SQL_DB_FUMO_STATUSNODENAME,
				DM_SQL_DB_FUMO_RESULTCODE,
				DM_SQL_DB_FUMO_UPDATEMECHANISM,
				DM_SQL_DB_FUMO_DOWNLOADMODE,
				DM_SQL_DB_FUMO_CORRELATOR,
				DM_SQL_DB_FUMO_CONTENTTYPE,
				DM_SQL_DB_FUMO_ACCEPTTYPE,
				DM_SQL_DB_FUMO_UPDATEWAIT};

		Cursor cursor = db.query(true, DATABASE_FUMO_TABLE, FROM, "rowid=" + rowId, null, null, null, null, null);

		if (cursor.getCount() > 0)
		{
			while (cursor.moveToNext())
			{
				@SuppressWarnings("unused")
				long id = cursor.getLong(0);
				fumoinfo.Protocol = cursor.getString(1);
				fumoinfo.ObexType = cursor.getInt(2);
				fumoinfo.AuthType = cursor.getInt(3);
				fumoinfo.ServerPort = cursor.getInt(4);
				fumoinfo.ServerUrl = cursor.getString(5);
				fumoinfo.ServerIP = cursor.getString(6);
				fumoinfo.ObjectDownloadProtocol = cursor.getString(7);
				fumoinfo.ObjectDownloadUrl = cursor.getString(8);
				fumoinfo.ObjectDownloadIP = cursor.getString(9);
				fumoinfo.nObjectDownloadPort = cursor.getInt(10);
				fumoinfo.StatusNotifyProtocol = cursor.getString(11);
				fumoinfo.StatusNotifyUrl = cursor.getString(12);
				fumoinfo.StatusNotifyIP = cursor.getString(13);
				fumoinfo.nStatusNotifyPort = cursor.getInt(14);
				fumoinfo.ReportURI = cursor.getString(15);
				fumoinfo.nObjectSize = cursor.getInt(16);
				fumoinfo.nFFSWriteSize = cursor.getInt(17);
				fumoinfo.nStatus = cursor.getInt(18);
				fumoinfo.StatusNodeName = cursor.getString(19);
				fumoinfo.ResultCode = cursor.getString(20);
				fumoinfo.nUpdateMechanism = cursor.getInt(21);
				if (cursor.getInt(22) == 0)
					fumoinfo.nDownloadMode = false;
				else
					fumoinfo.nDownloadMode = true;
				fumoinfo.Correlator = cursor.getString(23);
				fumoinfo.szContentType = cursor.getString(24);
				fumoinfo.szAcceptType = cursor.getString(25);
				if (cursor.getInt(26) == 0)
					fumoinfo.bUpdateWait = false;
				else
					fumoinfo.bUpdateWait = true;
			}
		}
		cursor.close();
		return fumoinfo;
	}

	public static void updateFUMORow(long rowId, tsDBFumoInfo fumoinfo)
	{
		ContentValues args = new ContentValues();
		args.put(DM_SQL_DB_FUMO_PROTOCOL, fumoinfo.Protocol);
		args.put(DM_SQL_DB_FUMO_OBEXTYPE, fumoinfo.ObexType);
		args.put(DM_SQL_DB_FUMO_AUTHTYPE, fumoinfo.AuthType);
		args.put(DM_SQL_DB_FUMO_SERVERPORT, fumoinfo.ServerPort);
		args.put(DM_SQL_DB_FUMO_SERVERURL, fumoinfo.ServerUrl);
		args.put(DM_SQL_DB_FUMO_SERVERIP, fumoinfo.ServerIP);
		args.put(DM_SQL_DB_FUMO_OBJECTDOWNLOADPROTOCOL, fumoinfo.ObjectDownloadProtocol);
		args.put(DM_SQL_DB_FUMO_OBJECTDOWNLOADURL, fumoinfo.ObjectDownloadUrl);
		args.put(DM_SQL_DB_FUMO_OBJECTDOWNLOADIP, fumoinfo.ObjectDownloadIP);
		args.put(DM_SQL_DB_FUMO_OBJECTDOWNLOADPORT, fumoinfo.nObjectDownloadPort);
		args.put(DM_SQL_DB_FUMO_STATUSNOTIFYPROTOCOL, fumoinfo.StatusNotifyProtocol);
		args.put(DM_SQL_DB_FUMO_STATUSNOTIFYURL, fumoinfo.StatusNotifyUrl);
		args.put(DM_SQL_DB_FUMO_STATUSNOTIFYIP, fumoinfo.StatusNotifyIP);
		args.put(DM_SQL_DB_FUMO_STATUSNOTIFYPORT, fumoinfo.nStatusNotifyPort);
		args.put(DM_SQL_DB_FUMO_REPORTURI, fumoinfo.ReportURI);
		args.put(DM_SQL_DB_FUMO_OBJECTSIZE, fumoinfo.nObjectSize);
		args.put(DM_SQL_DB_FUMO_FFSWRITESIZE, fumoinfo.nFFSWriteSize);
		args.put(DM_SQL_DB_FUMO_STATUS, fumoinfo.nStatus);
		args.put(DM_SQL_DB_FUMO_STATUSNODENAME, fumoinfo.StatusNodeName);
		args.put(DM_SQL_DB_FUMO_RESULTCODE, fumoinfo.ResultCode);
		args.put(DM_SQL_DB_FUMO_UPDATEMECHANISM, fumoinfo.nUpdateMechanism);
		args.put(DM_SQL_DB_FUMO_DOWNLOADMODE, fumoinfo.nDownloadMode);
		args.put(DM_SQL_DB_FUMO_CORRELATOR, fumoinfo.Correlator);
		args.put(DM_SQL_DB_FUMO_CONTENTTYPE, fumoinfo.szContentType);
		args.put(DM_SQL_DB_FUMO_ACCEPTTYPE, fumoinfo.szAcceptType);
		args.put(DM_SQL_DB_FUMO_UPDATEWAIT, fumoinfo.bUpdateWait);
		db.update(DATABASE_FUMO_TABLE, args, "rowid=" + rowId, null);
	}

	public static boolean existsFUMORow(long rowId)
	{
		String[] FROM = {
				DM_SQL_DB_ROWID,
				DM_SQL_DB_FUMO_PROTOCOL,
				DM_SQL_DB_FUMO_OBEXTYPE,
				DM_SQL_DB_FUMO_AUTHTYPE,
				DM_SQL_DB_FUMO_SERVERPORT,
				DM_SQL_DB_FUMO_SERVERURL,
				DM_SQL_DB_FUMO_SERVERIP,
				DM_SQL_DB_FUMO_OBJECTDOWNLOADPROTOCOL,
				DM_SQL_DB_FUMO_OBJECTDOWNLOADURL,
				DM_SQL_DB_FUMO_OBJECTDOWNLOADIP,
				DM_SQL_DB_FUMO_OBJECTDOWNLOADPORT,
				DM_SQL_DB_FUMO_STATUSNOTIFYPROTOCOL,
				DM_SQL_DB_FUMO_STATUSNOTIFYURL,
				DM_SQL_DB_FUMO_STATUSNOTIFYIP,
				DM_SQL_DB_FUMO_STATUSNOTIFYPORT,
				DM_SQL_DB_FUMO_REPORTURI,
				DM_SQL_DB_FUMO_OBJECTSIZE,
				DM_SQL_DB_FUMO_FFSWRITESIZE,
				DM_SQL_DB_FUMO_STATUS,
				DM_SQL_DB_FUMO_STATUSNODENAME,
				DM_SQL_DB_FUMO_RESULTCODE,
				DM_SQL_DB_FUMO_UPDATEMECHANISM,
				DM_SQL_DB_FUMO_DOWNLOADMODE,
				DM_SQL_DB_FUMO_CORRELATOR,
				DM_SQL_DB_FUMO_CONTENTTYPE,
				DM_SQL_DB_FUMO_ACCEPTTYPE,
				DM_SQL_DB_FUMO_UPDATEWAIT};

		Cursor cursor = db.query(true, DATABASE_FUMO_TABLE, FROM, "rowid=" + rowId, null, null, null, null, null);

		if (cursor.getCount() > 0)
		{
			cursor.close();
			return true;
		}
		else
		{
			cursor.close();
			return false;
		}
	}

	public static void insertPostPoneRow(tsDBPostPone postpone)
	{
		ContentValues initialValues = new ContentValues();

		initialValues.put(DM_SQL_DB_POSTPONE_CURRENTTIME, postpone.tCurrentTime);
		initialValues.put(DM_SQL_DB_POSTPONE_ENDTIME, postpone.tEndTime);
		initialValues.put(DM_SQL_DB_POSTPONE_AFTERDOWNLOADBATTERYSTATUS, postpone.nAfterDownLoadBatteryStatus);
		initialValues.put(DM_SQL_DB_POSTPONE_POSTPONETIME, postpone.nPostPoneTime);
		initialValues.put(DM_SQL_DB_POSTPONE_POSTPONECOUNT, postpone.nPostPoneCount);
		initialValues.put(DM_SQL_DB_POSTPONE_POSTPONEDOWNLOAD, postpone.bPostPoneDownload);

		db.insert(DATABASE_POSTPONE_TABLE, null, initialValues);
	}

	public static void deletePostPoneRow(long rowId)
	{
		db.delete(DATABASE_POSTPONE_TABLE, "rowid=" + rowId, null);
	}

	public static Object fetchPostPoneRow(long rowId, tsDBPostPone postpone)
	{
		String[] FROM = {
				DM_SQL_DB_ROWID,
				DM_SQL_DB_POSTPONE_CURRENTTIME,
				DM_SQL_DB_POSTPONE_ENDTIME,
				DM_SQL_DB_POSTPONE_AFTERDOWNLOADBATTERYSTATUS,
				DM_SQL_DB_POSTPONE_POSTPONETIME,
				DM_SQL_DB_POSTPONE_POSTPONECOUNT,
				DM_SQL_DB_POSTPONE_POSTPONEDOWNLOAD};

		Cursor cursor = db.query(true, DATABASE_POSTPONE_TABLE, FROM, "rowid=" + rowId, null, null, null, null, null);

		if (cursor.getCount() > 0)
		{
			while (cursor.moveToNext())
			{
				@SuppressWarnings("unused")
				long id = cursor.getLong(0);
				postpone.tCurrentTime = cursor.getLong(1);
				postpone.tEndTime = cursor.getLong(2);
				if (cursor.getInt(3) == 0)
					postpone.nAfterDownLoadBatteryStatus = false;
				else
					postpone.nAfterDownLoadBatteryStatus = true;
				postpone.nPostPoneTime = cursor.getLong(4);
				postpone.nPostPoneCount = cursor.getInt(5);
				if (cursor.getInt(6) == 0)
					postpone.bPostPoneDownload = false;
				else
					postpone.bPostPoneDownload = true;
			}
		}
		cursor.close();
		return postpone;
	}

	public static void updatePostPoneRow(long rowID, tsDBPostPone postpone)
	{
		ContentValues args = new ContentValues();
		args.put(DM_SQL_DB_POSTPONE_CURRENTTIME, postpone.tCurrentTime);
		args.put(DM_SQL_DB_POSTPONE_ENDTIME, postpone.tEndTime);
		args.put(DM_SQL_DB_POSTPONE_AFTERDOWNLOADBATTERYSTATUS, postpone.nAfterDownLoadBatteryStatus);
		args.put(DM_SQL_DB_POSTPONE_POSTPONETIME, postpone.nPostPoneTime);
		args.put(DM_SQL_DB_POSTPONE_POSTPONECOUNT, postpone.nPostPoneCount);
		args.put(DM_SQL_DB_POSTPONE_POSTPONEDOWNLOAD, postpone.bPostPoneDownload);
		db.update(DATABASE_POSTPONE_TABLE, args, "rowid=" + rowID, null);
	}

	public static boolean existsPostPoneRow(long rowId)
	{
		String[] FROM = {
				DM_SQL_DB_ROWID,
				DM_SQL_DB_POSTPONE_CURRENTTIME,
				DM_SQL_DB_POSTPONE_ENDTIME,
				DM_SQL_DB_POSTPONE_AFTERDOWNLOADBATTERYSTATUS,
				DM_SQL_DB_POSTPONE_POSTPONETIME,
				DM_SQL_DB_POSTPONE_POSTPONECOUNT,
				DM_SQL_DB_POSTPONE_POSTPONEDOWNLOAD};

		Cursor cursor = db.query(true, DATABASE_POSTPONE_TABLE, FROM, "rowid=" + rowId, null, null, null, null, null);

		if (cursor.getCount() > 0)
		{
			cursor.close();
			return true;
		}
		else
		{
			cursor.close();
			return false;
		}
	}

	public static void insertSimInfoRow(tsDBSimInfo siminfo)
	{
		ContentValues initialValues = new ContentValues();

		initialValues.put(DM_SQL_DB_SIMINFO_IMSI, siminfo.pIMSI);

		db.insert(DATABASE_SIMINFO_TABLE, null, initialValues);
	}

	public static void deleteSimInfoRow(long rowId)
	{
		db.delete(DATABASE_SIMINFO_TABLE, "rowid=" + rowId, null);
	}

	public static Object fetchSimInfoRow(long rowId, tsDBSimInfo siminfo)
	{
		String[] FROM = {
				DM_SQL_DB_ROWID,
				DM_SQL_DB_SIMINFO_IMSI};

		Cursor cursor = db.query(true, DATABASE_SIMINFO_TABLE, FROM, "rowid=" + rowId, null, null, null, null, null);

		if (cursor.getCount() > 0)
		{
			while (cursor.moveToNext())
			{
				@SuppressWarnings("unused")
				long id = cursor.getLong(0);
				siminfo.pIMSI = cursor.getString(1);
			}
		}
		cursor.close();
		return siminfo;
	}

	public static void updateSimInfoRow(long rowId, tsDBSimInfo siminfo)
	{
		ContentValues args = new ContentValues();
		args.put(DM_SQL_DB_SIMINFO_IMSI, siminfo.pIMSI);
		db.update(DATABASE_SIMINFO_TABLE, args, "rowid=" + rowId, null);
	}

	public static boolean existsSimInfoRow(long rowId)
	{
		String[] FROM = {
				DM_SQL_DB_ROWID,
				DM_SQL_DB_SIMINFO_IMSI};

		Cursor cursor = db.query(true, DATABASE_SIMINFO_TABLE, FROM, "rowid=" + rowId, null, null, null, null, null);

		if (cursor.getCount() > 0)
		{
			cursor.close();
			return true;
		}
		else
		{
			cursor.close();
			return false;
		}
	}

	public static void insertAccXListNodeRow(tsDBAccXNode accxnode)
	{
		ContentValues initialValues = new ContentValues();

		initialValues.put(DM_SQL_DB_ACCXLISTNODE_ACCOUNT, accxnode.Account);
		initialValues.put(DM_SQL_DB_ACCXLISTNODE_APPADDR, accxnode.AppAddr);
		initialValues.put(DM_SQL_DB_ACCXLISTNODE_APPADDRPORT, accxnode.AppAddrPort);
		initialValues.put(DM_SQL_DB_ACCXLISTNODE_CLIENTAPPAUTH, accxnode.ClientAppAuth);
		initialValues.put(DM_SQL_DB_ACCXLISTNODE_SERVERAPPAUTH, accxnode.ServerAppAuth);
		initialValues.put(DM_SQL_DB_ACCXLISTNODE_TOCONREF, accxnode.ToConRef);

		db.insert(DATABASE_ACCXLISTNODE_TABLE, null, initialValues);
	}

	public static void deleteAccXListNodeRow(long rowId)
	{
		db.delete(DATABASE_ACCXLISTNODE_TABLE, "rowid=" + rowId, null);
	}

	public static Object fetchAccXListNodeRow(long rowId, tsDBAccXNode accxnode)
	{
		String[] FROM = {
				DM_SQL_DB_ROWID,
				DM_SQL_DB_ACCXLISTNODE_ACCOUNT,
				DM_SQL_DB_ACCXLISTNODE_APPADDR,
				DM_SQL_DB_ACCXLISTNODE_APPADDRPORT,
				DM_SQL_DB_ACCXLISTNODE_CLIENTAPPAUTH,
				DM_SQL_DB_ACCXLISTNODE_SERVERAPPAUTH,
				DM_SQL_DB_ACCXLISTNODE_TOCONREF};

		Cursor cursor = db.query(true, DATABASE_ACCXLISTNODE_TABLE, FROM, "rowid=" + rowId, null, null, null, null, null);

		if (cursor.getCount() > 0)
		{
			while (cursor.moveToNext())
			{
				@SuppressWarnings("unused")
				long id = cursor.getLong(0);
				accxnode.Account = cursor.getString(1);
				accxnode.AppAddr = cursor.getString(2);
				accxnode.AppAddrPort = cursor.getString(3);
				accxnode.ClientAppAuth = cursor.getString(4);
				accxnode.ServerAppAuth = cursor.getString(5);
				accxnode.ToConRef = cursor.getString(6);
			}
		}
		cursor.close();
		return accxnode;
	}

	public static void updateAccXListNodeRow(long rowId, tsDBAccXNode accxnode)
	{
		ContentValues args = new ContentValues();
		args.put(DM_SQL_DB_ACCXLISTNODE_ACCOUNT, accxnode.Account);
		args.put(DM_SQL_DB_ACCXLISTNODE_APPADDR, accxnode.AppAddr);
		args.put(DM_SQL_DB_ACCXLISTNODE_APPADDRPORT, accxnode.AppAddrPort);
		args.put(DM_SQL_DB_ACCXLISTNODE_CLIENTAPPAUTH, accxnode.ClientAppAuth);
		args.put(DM_SQL_DB_ACCXLISTNODE_SERVERAPPAUTH, accxnode.ServerAppAuth);
		args.put(DM_SQL_DB_ACCXLISTNODE_TOCONREF, accxnode.ToConRef);
		db.update(DATABASE_ACCXLISTNODE_TABLE, args, "rowid=" + rowId, null);
	}

	public static boolean existsAccXListNodeRow(long rowId)
	{
		String[] FROM = {
				DM_SQL_DB_ROWID,
				DM_SQL_DB_ACCXLISTNODE_ACCOUNT,
				DM_SQL_DB_ACCXLISTNODE_APPADDR,
				DM_SQL_DB_ACCXLISTNODE_APPADDRPORT,
				DM_SQL_DB_ACCXLISTNODE_CLIENTAPPAUTH,
				DM_SQL_DB_ACCXLISTNODE_SERVERAPPAUTH,
				DM_SQL_DB_ACCXLISTNODE_TOCONREF};

		Cursor cursor = db.query(true, DATABASE_ACCXLISTNODE_TABLE, FROM, "rowid=" + rowId, null, null, null, null, null);

		if (cursor.getCount() > 0)
		{
			cursor.close();
			return true;
		}
		else
		{
			cursor.close();
			return false;
		}
	}

	public static void insertResyncModeRow(tsDBResyncMode resyncmode)
	{
		ContentValues initialValues = new ContentValues();

		initialValues.put(DM_SQL_DB_RESYNCMODE_NONCERESYNCMODE, resyncmode.nNoceResyncMode);

		db.insert(DATABASE_RESYNCMODE_TABLE, null, initialValues);
	}

	public static void deleteResyncModeRow(long rowId)
	{
		db.delete(DATABASE_RESYNCMODE_TABLE, "rowid=" + rowId, null);
	}

	public static Object fetchResyncModeRow(long rowId, tsDBResyncMode resyncmode)
	{
		String[] FROM = {
				DM_SQL_DB_ROWID,
				DM_SQL_DB_RESYNCMODE_NONCERESYNCMODE};

		Cursor cursor = db.query(true, DATABASE_RESYNCMODE_TABLE, FROM, "rowid=" + rowId, null, null, null, null, null);

		if (cursor.getCount() > 0)
		{
			while (cursor.moveToNext())
			{
				@SuppressWarnings("unused")
				long id = cursor.getLong(0);
				if (cursor.getInt(1) == 0)
					resyncmode.nNoceResyncMode = false;
				else
					resyncmode.nNoceResyncMode = true;
			}
		}
		cursor.close();
		return resyncmode;
	}

	public static void updateResyncModeRow(long rowId, tsDBResyncMode resyncmode)
	{
		ContentValues args = new ContentValues();
		args.put(DM_SQL_DB_RESYNCMODE_NONCERESYNCMODE, resyncmode.nNoceResyncMode);
		db.update(DATABASE_RESYNCMODE_TABLE, args, "rowid=" + rowId, null);
	}

	public static boolean existsResyncModeRow(long rowId)
	{
		String[] FROM = {
				DM_SQL_DB_ROWID,
				DM_SQL_DB_RESYNCMODE_NONCERESYNCMODE};

		Cursor cursor = db.query(true, DATABASE_RESYNCMODE_TABLE, FROM, "rowid=" + rowId, null, null, null, null, null);

		if (cursor.getCount() > 0)
		{
			cursor.close();
			return true;
		}
		else
		{
			cursor.close();
			return false;
		}
	}

	public static void dmDbSqlAgentInfoInsertRow(dmAgentInfo DmAgentInfo)
	{
		ContentValues ContentValues = new ContentValues();

		ContentValues.put(DM_SQL_DB_AGENT_INFO_AGENT_TYPE, DmAgentInfo.nAgentType);

		db.insert(DATABASE_DM_AGENT_INFO_TABLE, null, ContentValues);
	}

	public static void dmDbSqlAgentInfoDeleteRow(long RowId)
	{
		db.delete(DATABASE_DM_AGENT_INFO_TABLE, "rowid=" + RowId, null);
	}

	public static Object dmDbSqlAgentInfoFetchRow(long RowId, dmAgentInfo DmAgentInfo)
	{
		String[] FROM = {
				DM_SQL_DB_ROWID,
				DM_SQL_DB_AGENT_INFO_AGENT_TYPE};

		Cursor Cursor = db.query(true, DATABASE_DM_AGENT_INFO_TABLE, FROM, "rowid=" + RowId, null, null, null, null, null);

		if (Cursor.getCount() > 0)
		{
			while (Cursor.moveToNext())
			{
				@SuppressWarnings("unused")
				long Id = Cursor.getLong(0);

				DmAgentInfo.nAgentType = Cursor.getInt(1);
			}
		}
		Cursor.close();
		return DmAgentInfo;
	}

	public static void dmDbSqlAgentInfoUpdateRow(long RowId, dmAgentInfo DmAgentInfo)
	{
		ContentValues ContentValues = new ContentValues();

		ContentValues.put(DM_SQL_DB_AGENT_INFO_AGENT_TYPE, DmAgentInfo.nAgentType);
		db.update(DATABASE_DM_AGENT_INFO_TABLE, ContentValues, "rowid=" + RowId, null);
	}

	public static boolean dmDbSqlAgentInfoExistsRow(long RowId)
	{
		String[] FROM = {
				DM_SQL_DB_ROWID,
				DM_SQL_DB_AGENT_INFO_AGENT_TYPE};

		Cursor Cursor = db.query(true, DATABASE_DM_AGENT_INFO_TABLE, FROM, "rowid=" + RowId, null, null, null, null, null);

		if (Cursor.getCount() > 0)
		{
			Cursor.close();
			return true;
		}
		else
		{
			Cursor.close();
			return false;
		}
	}

	public static boolean dmsqlExistsRow(int SqlID)
	{
		boolean bExists = false;

/*
		switch (SqlID)
		{
			case dmSqlDbIdLawmoInfo:
				break;

			case dmSqlDbIdNotiInfo:
				break;

			default:
				break;
		}
*/

		return bExists;
	}

	public static int dmsqlCreate(int SqlID, Object Input)
	{

		switch (SqlID)
		{
			case dmSqlDbIdProfileList:
				insertProfileListRow((tsdmProflieList) Input);
				break;

			case dmSqlDbIdProfileInfo1:
			case dmSqlDbIdProfileInfo2:
			case dmSqlDbIdProfileInfo3:
			case dmSqlDbIdProfileInfo4:
			case dmSqlDbIdProfileInfo5:
				insertProfileRow((tsdmInfo) Input);
				break;

			case dmSqlDbIdFUMOInfo:
				insertFUMORow((tsDBFumoInfo) Input);
				break;

			case dmSqlDbIdAPostPone:
				insertPostPoneRow((tsDBPostPone) Input);
				break;

			case dmSqlDbIdIMSIInfo:
				insertSimInfoRow((tsDBSimInfo) Input);
				break;

			case dmSqlDbIdAccXNode:
				insertAccXListNodeRow((tsDBAccXNode) Input);
				break;

			case dmSqlDbIdResyncMode:
				insertResyncModeRow((tsDBResyncMode) Input);
				break;

			case dmSqlDbIdDmAgentInfo:
				dmDbSqlAgentInfoInsertRow((dmAgentInfo) Input);
				break;

			case dmSqlDbIdNotiInfo:
				break;
		}

		return DB_SQL_OK;
	}

	public static boolean dmsqlUpdate(int SqlID, Object Input)
	{
		switch (SqlID)
		{
			case dmSqlDbIdProfileList:
				tsdmDB.DMNvmClass.tProfileList = (tsdmProflieList) Input;
				updateProfileListRow(DM_SQL_DB_PROFILELIST_ROWID, tsdmDB.DMNvmClass.tProfileList);
				break;

			case dmSqlDbIdProfileInfo1:
				tsdmDB.DMNvmClass.NVMSyncMLDMInfo = (tsdmInfo) Input;
				updateProfileRow(DM_SQL_DB_PROFILE1_ROWID, tsdmDB.DMNvmClass.NVMSyncMLDMInfo);
				break;

			case dmSqlDbIdProfileInfo2:
				tsdmDB.DMNvmClass.NVMSyncMLDMInfo = (tsdmInfo) Input;
				updateProfileRow(DM_SQL_DB_PROFILE2_ROWID, tsdmDB.DMNvmClass.NVMSyncMLDMInfo);
				break;

			case dmSqlDbIdProfileInfo3:
				tsdmDB.DMNvmClass.NVMSyncMLDMInfo = (tsdmInfo) Input;
				updateProfileRow(DM_SQL_DB_PROFILE3_ROWID, tsdmDB.DMNvmClass.NVMSyncMLDMInfo);
				break;

			case dmSqlDbIdNetworkInfo:
				tsdmDB.DMNvmClass.NVMSyncMLDMInfo.ConRef = (tsdmInfoConRef) Input;
				updateNetworkRow(DM_SQL_DB_NETWORK1_ROWID, tsdmDB.DMNvmClass.NVMSyncMLDMInfo.ConRef);
				break;

			case dmSqlDbIdFUMOInfo:
				tsdmDB.DMNvmClass.NVMSyncMLDMFUMOInfo = (tsDBFumoInfo) Input;
				updateFUMORow(DM_SQL_DB_FUMO_ROWID, tsdmDB.DMNvmClass.NVMSyncMLDMFUMOInfo);
				break;

			case dmSqlDbIdAPostPone:
				tsdmDB.DMNvmClass.NVMSyncMLPostPone = (tsDBPostPone) Input;
				updatePostPoneRow(DM_SQL_DB_POSTPONE_ROWID, tsdmDB.DMNvmClass.NVMSyncMLPostPone);
				break;

			case dmSqlDbIdIMSIInfo:
				tsdmDB.DMNvmClass.NVMSYNCMLSimInfo = (tsDBSimInfo) Input;
				updateSimInfoRow(DM_SQL_DB_SIMINFO_ROWID, tsdmDB.DMNvmClass.NVMSYNCMLSimInfo);
				break;

			case dmSqlDbIdAccXNode:
				tsdmDB.DMNvmClass.NVMSyncMLAccXNode = (tsDBAccXListNode) Input;
				for (int i = 0; i < 3; i++)
					updateAccXListNodeRow(i, tsdmDB.DMNvmClass.NVMSyncMLAccXNode.stAccXNodeList[i]);
				break;

			case dmSqlDbIdResyncMode:
				tsdmDB.DMNvmClass.NVMSyncMLResyncMode = (tsDBResyncMode) Input;
				updateResyncModeRow(DM_SQL_DB_RESYNCMODE_ROWID, tsdmDB.DMNvmClass.NVMSyncMLResyncMode);
				break;

			case dmSqlDbIdDmAgentInfo:
				tsdmDB.DMNvmClass.NVMSyncMLDmAgentInfo = (dmAgentInfo) Input;
				dmDbSqlAgentInfoUpdateRow(1, (dmAgentInfo) Input);
				break;

			case dmSqlDbIdScomoPostpone:
				break;

			case dmSqlDbIdNotiInfo:
				break;
		}
		return true;
	}

	public static boolean dmsqlUpdate(int SqlID, int rowId, Object Input)
	{
		switch (SqlID)
		{
		}
		return true;
	}

	public static int dmsqlDelete(int SqlID)
	{

		switch (SqlID)
		{
			case dmSqlDbIdProfileList:
				deleteProfileListRow(DM_SQL_DB_PROFILELIST_ROWID);
				break;

			case dmSqlDbIdProfileInfo1:
				deleteProfileRow(DM_SQL_DB_PROFILE1_ROWID);
				deleteNetworkRow(DM_SQL_DB_NETWORK1_ROWID);
				break;

			case dmSqlDbIdProfileInfo2:
				deleteProfileRow(DM_SQL_DB_PROFILE2_ROWID);
				break;

			case dmSqlDbIdProfileInfo3:
				deleteProfileRow(DM_SQL_DB_PROFILE3_ROWID);
				break;

			case dmSqlDbIdProfileInfo4:
				break;

			case dmSqlDbIdProfileInfo5:
				break;

			case dmSqlDbIdFUMOInfo:
				deleteFUMORow(DM_SQL_DB_FUMO_ROWID);
				break;

			case dmSqlDbIdAPostPone:
				deletePostPoneRow(DM_SQL_DB_POSTPONE_ROWID);
				break;

			case dmSqlDbIdIMSIInfo:
				deleteSimInfoRow(DM_SQL_DB_SIMINFO_ROWID);
				break;

			case dmSqlDbIdAccXNode:
				deleteAccXListNodeRow(DM_SQL_DB_ACCXLISTNODE_ROWID);
				break;

			case dmSqlDbIdResyncMode:
				deleteResyncModeRow(DM_SQL_DB_RESYNCMODE_ROWID);
				break;

			case dmSqlDbIdDmAgentInfo:
				dmDbSqlAgentInfoDeleteRow(1);
				break;

			case dmSqlDbIdNotiInfo:
				break;
		}

		return DB_SQL_OK;
	}

	public static Object dmsqlRead(int FileID)
	{

		switch (FileID)
		{
			case dmSqlDbIdProfileList:
				tsdmDB.DMNvmClass.tProfileList = (tsdmProflieList) fetchProfileListRow(DM_SQL_DB_PROFILELIST_ROWID, tsdmDB.DMNvmClass.tProfileList);
				return tsdmDB.DMNvmClass.tProfileList;

			case dmSqlDbIdProfileInfo1:
				tsdmDB.ProfileInfoClass[(int) (DM_SQL_DB_PROFILE1_ROWID - 1)] = new tsdmInfo();
				tsdmDB.ProfileInfoClass[(int) (DM_SQL_DB_PROFILE1_ROWID - 1)] = (tsdmInfo) fetchProfileRow(DM_SQL_DB_PROFILE1_ROWID, tsdmDB.ProfileInfoClass[(int) (DM_SQL_DB_PROFILE1_ROWID - 1)]);
				return tsdmDB.ProfileInfoClass[(int) (DM_SQL_DB_PROFILE1_ROWID - 1)];

			case dmSqlDbIdProfileInfo2:
				tsdmDB.ProfileInfoClass[(int) (DM_SQL_DB_PROFILE2_ROWID - 1)] = new tsdmInfo();
				tsdmDB.ProfileInfoClass[(int) (DM_SQL_DB_PROFILE2_ROWID - 1)] = (tsdmInfo) fetchProfileRow(DM_SQL_DB_PROFILE2_ROWID, tsdmDB.ProfileInfoClass[(int) (DM_SQL_DB_PROFILE2_ROWID - 1)]);
				return tsdmDB.ProfileInfoClass[(int) (DM_SQL_DB_PROFILE2_ROWID - 1)];

			case dmSqlDbIdProfileInfo3:
				tsdmDB.ProfileInfoClass[(int) (DM_SQL_DB_PROFILE3_ROWID - 1)] = new tsdmInfo();
				tsdmDB.ProfileInfoClass[(int) (DM_SQL_DB_PROFILE3_ROWID - 1)] = (tsdmInfo) fetchProfileRow(DM_SQL_DB_PROFILE3_ROWID, tsdmDB.ProfileInfoClass[(int) (DM_SQL_DB_PROFILE3_ROWID - 1)]);
				return tsdmDB.ProfileInfoClass[(int) (DM_SQL_DB_PROFILE3_ROWID - 1)];

			case dmSqlDbIdNetworkInfo:
				tsdmDB.ConRef = new tsdmInfoConRef();
				tsdmDB.ConRef = (tsdmInfoConRef) fetchNetworkRow(DM_SQL_DB_NETWORK1_ROWID, tsdmDB.ConRef);
				return tsdmDB.ConRef;

			case dmSqlDbIdFUMOInfo:
				tsdmDB.DMNvmClass.NVMSyncMLDMFUMOInfo = (tsDBFumoInfo) fetchFUMORow(DM_SQL_DB_PROFILE1_ROWID, tsdmDB.DMNvmClass.NVMSyncMLDMFUMOInfo);
				return tsdmDB.DMNvmClass.NVMSyncMLDMFUMOInfo;

			case dmSqlDbIdAPostPone:
				tsdmDB.DMNvmClass.NVMSyncMLPostPone = (tsDBPostPone) fetchPostPoneRow(DM_SQL_DB_PROFILE1_ROWID, tsdmDB.DMNvmClass.NVMSyncMLPostPone);
				return tsdmDB.DMNvmClass.NVMSyncMLPostPone;

			case dmSqlDbIdIMSIInfo:
				tsdmDB.DMNvmClass.NVMSYNCMLSimInfo = (tsDBSimInfo) fetchSimInfoRow(DM_SQL_DB_PROFILE1_ROWID, tsdmDB.DMNvmClass.NVMSYNCMLSimInfo);
				return tsdmDB.DMNvmClass.NVMSYNCMLSimInfo;

			case dmSqlDbIdAccXNode:
				for (int i = 0; i < 3; i++)
				{
					tsdmDB.DMNvmClass.NVMSyncMLAccXNode.stAccXNodeList[i] = new tsDBAccXNode();
					tsdmDB.DMNvmClass.NVMSyncMLAccXNode.stAccXNodeList[i] = (tsDBAccXNode) fetchAccXListNodeRow(i, tsdmDB.DMNvmClass.NVMSyncMLAccXNode.stAccXNodeList[i]);
				}
				return tsdmDB.DMNvmClass.NVMSyncMLAccXNode;

			case dmSqlDbIdResyncMode:
				tsdmDB.DMNvmClass.NVMSyncMLResyncMode = (tsDBResyncMode) fetchResyncModeRow(DM_SQL_DB_PROFILE1_ROWID, tsdmDB.DMNvmClass.NVMSyncMLResyncMode);
				return tsdmDB.DMNvmClass.NVMSyncMLResyncMode;

			case dmSqlDbIdDmAgentInfo:
				tsdmDB.DMNvmClass.NVMSyncMLDmAgentInfo = (dmAgentInfo) dmDbSqlAgentInfoFetchRow(1, tsdmDB.DMNvmClass.NVMSyncMLDmAgentInfo);
				return tsdmDB.DMNvmClass.NVMSyncMLDmAgentInfo;

			case dmSqlDbIdNotiInfo:
		}
		return null;
	}
}
