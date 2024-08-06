package com.tsdm.db;

import java.io.Serializable;
import java.security.SecureRandom;
import java.util.Date;
import java.util.Random;

import com.tsdm.auth.base64;
import com.tsdm.agent.dmDefineDevInfo;
import com.tsdm.agent.dmDevinfoAdapter;
import com.tsdm.adapt.tsLib;
import com.tsdm.agent.dmProfileEntity;
import com.tsdm.agent.dmPreConfigEntity;

public class tsDBFactoryBootstrap implements Serializable, dmDefineDevInfo, tsDefineDBsql
{
	public static final int 	DM_FACTORYBOOTSTRAP_SERVERPWD = 0;
	public static final int 	DM_FACTORYBOOTSTRAP_CLIENTID = 1;
	public static final int 	DM_FACTORYBOOTSTRAP_CLIENTPWD = 2;
	private static final long	serialVersionUID				= 1L;

/*	public static String  ServerID[]	= {"", "", ""};
	public static String  ProfileName[] = {"", "", ""};
	public static String  ServerUrl[]	= {"", "", ""};
	public static String ServerPort[]	= {"", "", ""};*/

	public static Object getFactoryBootstrapData(Object pNVMSyncMLDMInfo, int nIdex)
	{
		tsLib.debugPrint("", "nIndex = " + nIdex);
		tsdmInfo pProfileInfo = null;
		String pNonce = null;
		tsDBURLParser dbURLParser;
		tsDBURLParser dbURLParser2;
		dbURLParser = new tsDBURLParser();
		dbURLParser2 = new tsDBURLParser();
		String pIMEI = dmDevinfoAdapter.devAdpGetDeviceId();
		String szURL = "";
		
		pProfileInfo = (tsdmInfo) pNVMSyncMLDMInfo;
		
		if(_SYNCML_TS_DM_REGISTRY_PROFILE_)
		{
			if(dmPreConfigEntity.getDmProfileInfoFromConfigFile(pProfileInfo, nIdex) == false)
			{
				tsLib.debugPrintException(DEBUG_EXCEPTION, "Get Profile Info Failed");
				return null;
			}
		}
		else
		{
/*			pProfileInfo.ProfileName = ProfileName[nIdex];
			pProfileInfo.ServerID = ServerID[nIdex];
			pProfileInfo.ServerUrl = ServerUrl[nIdex];
			pProfileInfo.ServerPort = Integer.parseInt(ServerPort[nIdex]);
			pProfileInfo.AuthType = CRED_TYPE_BASIC;

			if (_SYNCML_TS_DM_VERSION_V12_)
			{
				pProfileInfo.AppID = "w7";
				pProfileInfo.AuthLevel = "CLCRED";
				pProfileInfo.ServerAuthLevel = "SRVCRED";
			}*/
		}

		String port = new String(Integer.toString(pProfileInfo.ServerPort));
		pProfileInfo.ServerUrl = dmProfileEntity.dmDoServerURI(szURL,
				pProfileInfo.ServerUrl.toCharArray(), port.toCharArray());
		tsLib.debugPrint(DEBUG_DB, "Tab : " + pProfileInfo.ServerUrl);
		
		dbURLParser = tsDB.dbURLParser(pProfileInfo.ServerUrl);
		pProfileInfo.ServerUrl = dbURLParser.pURL;
		pProfileInfo.ServerIP = dbURLParser.pAddress;
		pProfileInfo.Path = dbURLParser.pPath;
		pProfileInfo.ServerPort = dbURLParser.nPort;
		pProfileInfo.Protocol = dbURLParser.pProtocol;
		pProfileInfo.ServerUrl_Org = pProfileInfo.ServerUrl;

		dbURLParser2 = tsDB.dbURLParser(pProfileInfo.ServerUrl_Org);
		pProfileInfo.ServerUrl_Org = dbURLParser2.pURL;
		pProfileInfo.ServerIP_Org = dbURLParser2.pAddress;
		pProfileInfo.Path_Org = dbURLParser2.pPath;
		pProfileInfo.ServerPort_Org = dbURLParser2.nPort;
		pProfileInfo.Protocol_Org = dbURLParser2.pProtocol;
		pProfileInfo.bChangedProtocol = false;

		if (nIdex < 3)
		{
			if(!_SYNCML_TS_DM_REGISTRY_PROFILE_)
			{
				pProfileInfo.AuthType = CRED_TYPE_BASIC;
				pProfileInfo.nServerAuthType = CRED_TYPE_BASIC;
			}
			pProfileInfo.UserName = dmDevinfoAdapter.devAdpGetDeviceId();

			pNonce = fBGenerateFactoryNonce();

			if (_SYNCML_TS_DM_VERSION_V12_)
			{
				tsDBAccXNode dm_AccXNodeInfo = new tsDBAccXNode();
				
				if(_SYNCML_TS_DM_REGISTRY_PROFILE_)
				{
					if(dmPreConfigEntity.getAccXnodeInfoFromConfigFile(dm_AccXNodeInfo, nIdex) == false)
					{
						tsLib.debugPrintException(DEBUG_EXCEPTION, "Get Acc Xnode Info Failed");
					}
				}
				else
				{
					dm_AccXNodeInfo.Account = "./DMAcc/SampleAcc";
					dm_AccXNodeInfo.AppAddr = "./DMAcc/SampleAcc/AppAddr/AppAddrX";
					dm_AccXNodeInfo.AppAddrPort = "./DMAcc/SampleAcc/AppAddr/AppAddrX/Port/PortX";
					dm_AccXNodeInfo.ClientAppAuth = "./DMAcc/SampleAcc/AppAuth/ClientSide";
					dm_AccXNodeInfo.ServerAppAuth = "./DMAcc/SampleAcc/AppAuth/ServerSide";
					dm_AccXNodeInfo.ToConRef = "./DMAcc/SampleAcc/ToConRef/Connectivity Reference Name";
				}
				
				tsdmDBsql.dmsqlCreate(dmSqlDbIdAccXNode, (Object) dm_AccXNodeInfo);
			}

			pProfileInfo.ClientNonce = pNonce;
			pProfileInfo.ServerNonce = pNonce;
			pProfileInfo.ServerNonceFormat = 1; // 0 ==> FORMAT_B64;
			pProfileInfo.ClientNonceFormat = 1; // 0 ==> FORMAT_B64;
		}
		else
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, "Wrong Index : " + nIdex);
		}
		return pProfileInfo;
	}

/*	public static String getFactoryBootstrapServerID(int nIdex)
	{
		return ServerID[nIdex];
	}
	
	public static String getFactoryBootstrapServerPort(int nIdex)
	{
		return ServerPort[nIdex];
	}*/

	public static String fBGenerateFactoryNonce()
	{
		byte[] buf;
		String ptNonce;

		Date date = new Date();
		long seed = date.getTime();
		//Random rnd = new Random(seed);
		SecureRandom rnd = new SecureRandom(); // sonacube security
		String temp = String.valueOf(rnd.nextInt()) + "SSNextNonce";
		buf = temp.getBytes();

		byte[] encoder = base64.encode(buf);
		ptNonce = new String(encoder);

		return ptNonce;
	}
}
