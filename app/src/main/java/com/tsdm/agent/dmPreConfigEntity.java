package com.tsdm.agent;

import java.io.*;
import java.util.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

import com.tsdm.db.tsDBAccXNode;
import com.tsdm.db.tsdmInfo;
import com.tsdm.adapt.tsLib;

import javax.xml.parsers.*;

public class dmPreConfigEntity extends DefaultHandler implements dmDefineDevInfo
{
	private static final String	DM_CONFIG_FILE_NAME					= "file:///data/data/com.tsdm/tsDmConfig.xml";
	private static final String	DM_CONFIG_BASE_ACCOUNT_PATH			= "Dm12Root";
	
	private static final String	DM_CONFIG_SECTION_ACCOUNT1			= "Account1";
	private static final String	DM_CONFIG_SECTION_ACCOUNT2			= "Account2";
	private static final String	DM_CONFIG_SECTION_ACCOUNT3			= "Account3";
	private static final String	DM_CONFIG_SECTION_DM_SETTING		= "Setting";
	
	private static final String	DM_CONFIG_PROFILENAME				= "AccountName";
	private static final String	DM_CONFIG_SERVERURL					= "ServerUrl";
	private static final String	DM_CONFIG_SERVERPORT				= "ServerPort";
	private static final String	DM_CONFIG_SERVERID					= "ServerId";
	private static final String	DM_CONFIG_SERVERPWD					= "ServerPwd";
	private static final String	DM_CONFIG_CLIENTID					= "ClientId";
	private static final String	DM_CONFIG_CLIENTPWD					= "ClientPwd";
	private static final String	DM_CONFIG_CLIENTAUTHTYPE			= "ClientAuthType";
	private static final String	DM_CONFIG_SERVERAUTHTYPE			= "ServerAuthType";
	private static final String	DM_CONFIG_SERVERNONCE				= "ServerNonce";
	private static final String	DM_CONFIG_CLIENTNONCE				= "ClientNonce";
	private static final String	DM_CONFIG_SERVERNONCEFORMAT			= "ServerNonceFormat";
	private static final String	DM_CONFIG_CLIENTNONCEFORMAT			= "ClientNonceFormat";
	private static final String	DM_CONFIG_NETCONNNAME				= "NetConnName";
	private static final String	DM_CONFIG_CONREFACTIVE				= "ConRefActive";
	private static final String	DM_CONFIG_NAPBEARER					= "NapBearer";
	private static final String	DM_CONFIG_NAPADDRTYPE				= "NapAddrType";
	private static final String	DM_CONFIG_NAPADDR					= "NapAddr";
	private static final String	DM_CONFIG_NAPLOGINID				= "NapLoginId";
	private static final String	DM_CONFIG_NAPLOGINPWD				= "NapLoginPwd";
	private static final String	DM_CONFIG_PROXYADDR					= "ProxyAddr";
	private static final String	DM_CONFIG_PROXYPORT					= "ProxyPort";
	private static final String	DM_CONFIG_PROXYADDRTYPE				= "ProxyAddrType";
	private static final String	DM_CONFIG_PROXYENABLE				= "ProxyEnable";
	private static final String	DM_CONFIG_STATICIPENABLE			= "StaticIpEnable";
	private static final String	DM_CONFIG_STATICIP					= "StaticIp";
	private static final String	DM_CONFIG_STATICDNSENABLE			= "StaticDnsEnable";
	private static final String	DM_CONFIG_DNSADDR1					= "DnsAddr1";
	private static final String	DM_CONFIG_DNSADDR2					= "DnsAddr2";
	private static final String	DM_CONFIG_APPID						= "AppId";
	private static final String	DM_CONFIG_CLIENT_AUTHLEVEL			= "ClientAuthLevel";
	private static final String	DM_CONFIG_SERVER_AUTHLEVEL			= "ServerAuthLevel";
	private static final String	DM_CONFIG_APPADDR_X_NAME			= "AppAddrX";
	private static final String	DM_CONFIG_APPADDR_PORT_X_NAME		= "PortX";
	private static final String	DM_CONFIG_APPAUTH_CLIENT_X_NAME		= "ClientAppAuthX";
	private static final String	DM_CONFIG_APPAUTH_SERVER_X_NAME		= "ServerAppAuthX";
	private static final String	DM_CONFIG_TOCONREF_X_NAME			= "ToConRefX";
	

	private String ConfigPath;

	private Map<String, String> ConfigMap;
	
	private String Cur_Attr_Name = "";

	private String ConfigGroup = "";
	
	static dmPreConfigEntity xmlConfigFile = null;

	public dmPreConfigEntity(String path)
	{
		ConfigPath = path;
		loadConfig();
	}
	
	public static void openConfigFile()
	{
		xmlConfigFile = new dmPreConfigEntity(DM_CONFIG_FILE_NAME);
	}


	public String findConfig(String group, String field)
	{
		return ConfigMap.get(group+field);
	}
	
	public int getSize()
	{
		return ConfigMap.size();
	}

	public int loadConfig()
	{
		ConfigMap = new HashMap<String, String>();
		int nRet = -1;

		tsLib.debugPrint(DEBUG_DM, "Start Load_Config "+ConfigPath);

		try {
			tsLib.debugPrint(DEBUG_DM, "Config File Read Start");
			// Start parsing of the XML
			nRet = parseBySAX(ConfigPath);
		} catch(FileNotFoundException fnfe) {
			tsLib.debugPrint(DEBUG_EXCEPTION, "Open ["+ConfigPath+"] FileNotFoundException: "+fnfe.getMessage());
			return -1;
		} catch(IOException e) {
			tsLib.debugPrint(DEBUG_EXCEPTION, "Read ["+ConfigPath+"] IOException: "+e.getMessage());
			return -1;
		} catch(Exception e) {
			tsLib.debugPrint(DEBUG_EXCEPTION, "Read ["+ConfigPath+"] Exception: "+e.getMessage());
			return -1;
		}

		return nRet;
	}
	
	public int parseBySAX(String xml_file_path) throws Exception
	{
		try {
			// Create object
			SAXParserFactory factory = SAXParserFactory.newInstance();
			factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
			SAXParser parser = factory.newSAXParser();
			
			// Registration the event handler and parse the XML data
			ParserAdapter adapter = new ParserAdapter(parser.getParser());
			adapter.setContentHandler(this);
			adapter.parse(xml_file_path);
		} catch(FileNotFoundException fnfe) {
			tsLib.debugPrint(DEBUG_EXCEPTION, "Open ["+ConfigPath+"] FileNotFoundException: "+fnfe.getMessage());
			return -1;
		} catch (Exception e) {
			tsLib.debugPrint(DEBUG_EXCEPTION, "processWithSAX Exception : " + e.getMessage());
			return -1;
		}

		return 1;
	}

	public void startDocument()
	{
		tsLib.debugPrint(DEBUG_DM, "XML parsing started");
	}

	public void endDocument()
	{
		tsLib.debugPrint(DEBUG_DM, "XML parsing ended");
	}


	public void characters(char[] ch, int start, int length)
	{
		String str_value = new String(ch, start, length);
		str_value = str_value.trim();
		
		if(length > 0 && str_value.length() > 0)
		{
//			ConfigMap.put(ConfigGroup+Cur_Attr_Name, str_value);
//			wsdmImplDebugEntity.printDebug("characters Cur_Attr_Name:"+Cur_Attr_Name+" ["+str_value+"] length:"+length);
		}
	}

	public void startElement(String namespace, String localName, String qName, Attributes attrs)
	{
		String name = null;
		String value = null;
		
		//wsdmImplDebugEntity.printDebug("startElement qName:"+qName+" Length:"+attrs.getLength());
		Cur_Attr_Name = qName;
		
		if(attrs != null && attrs.getLength() > 0)
		{
            for(int i=0; i<attrs.getLength(); i++)
            {
                name = attrs.getLocalName(i);
                if("".equals(name)) name = attrs.getQName(i);
            	value = attrs.getValue(i);
            	
                if("name".equals(name))
                {
                    ConfigGroup = value;
					tsLib.debugPrint(DEBUG_DM, "GroupName:["+value+"]");
                }
                
                if("value".equals(name))
                {
        			ConfigMap.put(ConfigGroup+Cur_Attr_Name, value);
					tsLib.debugPrint(DEBUG_DM, "AttrName:"+Cur_Attr_Name+" ["+value+"]");
                }
            }
        }
	}

	public static boolean getDmProfileInfoFromConfigFile(tsdmInfo pProfileInfo, int nIndex)
	{
		tsLib.debugPrint("getDmProfileInfoFromConfigFile", "nIndex = " + nIndex);
		String pszSection = DM_CONFIG_SECTION_ACCOUNT1;

		switch(nIndex)
		{
			case 0:
				pszSection = DM_CONFIG_SECTION_ACCOUNT1;
				break;

			case 1:
				pszSection = DM_CONFIG_SECTION_ACCOUNT2;
				break;

			case 2:
				pszSection = DM_CONFIG_SECTION_ACCOUNT3;
				break;

			default:
				break;
		}
		
		/* DM Profile */
		/* Profile Name */
		pProfileInfo.ProfileName = xmlConfigFile.findConfig(pszSection, DM_CONFIG_PROFILENAME);
		/* Server URL */
		pProfileInfo.ServerUrl = xmlConfigFile.findConfig(pszSection, DM_CONFIG_SERVERURL);
		/* Server Port */
		pProfileInfo.ServerPort = Integer.valueOf(xmlConfigFile.findConfig(pszSection, DM_CONFIG_SERVERPORT)).intValue();
		/* Server ID */
		pProfileInfo.ServerID = xmlConfigFile.findConfig(pszSection, DM_CONFIG_SERVERID);
		/* Server Password */
		pProfileInfo.ServerPwd = xmlConfigFile.findConfig(pszSection, DM_CONFIG_SERVERPWD);
		/* Client ID */
		pProfileInfo.UserName = xmlConfigFile.findConfig(pszSection, DM_CONFIG_CLIENTID);
		/* Client Password */
		pProfileInfo.Password = xmlConfigFile.findConfig(pszSection, DM_CONFIG_CLIENTPWD);
		/* Client Authentication Type */
		pProfileInfo.AuthType = Integer.valueOf(xmlConfigFile.findConfig(pszSection, DM_CONFIG_CLIENTAUTHTYPE)).intValue();
		/* Server Authentication Type */
		pProfileInfo.nServerAuthType = Integer.valueOf(xmlConfigFile.findConfig(pszSection, DM_CONFIG_SERVERAUTHTYPE)).intValue();
		/* Server Nonce */
		pProfileInfo.ServerNonce = xmlConfigFile.findConfig(pszSection, DM_CONFIG_SERVERNONCE);
		/* Client Nonce */
		pProfileInfo.ClientNonce = xmlConfigFile.findConfig(pszSection, DM_CONFIG_CLIENTNONCE);
		/* Server Nonce Format */
		pProfileInfo.ServerNonceFormat = Integer.valueOf(xmlConfigFile.findConfig(pszSection, DM_CONFIG_SERVERNONCEFORMAT)).intValue();
		/* Client Nonce Format */
		pProfileInfo.ClientNonceFormat = Integer.valueOf(xmlConfigFile.findConfig(pszSection, DM_CONFIG_CLIENTNONCEFORMAT)).intValue();
		/* Network Connecting Profile Name */
		pProfileInfo.NetworkConnName = xmlConfigFile.findConfig(pszSection, DM_CONFIG_NETCONNNAME);
		
		/* DM Network Profile */
		/* Network Profile Exist or Not */
		pProfileInfo.ConRef.Active = Boolean.valueOf(xmlConfigFile.findConfig(pszSection, DM_CONFIG_CONREFACTIVE));
		/* APN Bearer */
		pProfileInfo.ConRef.NAP.nBearer = Integer.valueOf(xmlConfigFile.findConfig(pszSection, DM_CONFIG_NAPBEARER)).intValue();
		/* APN Address Type */
		pProfileInfo.ConRef.NAP.nAddrType = Integer.valueOf(xmlConfigFile.findConfig(pszSection, DM_CONFIG_NAPADDRTYPE)).intValue();
		/* APN Address */
		pProfileInfo.ConRef.NAP.Addr = xmlConfigFile.findConfig(pszSection, DM_CONFIG_NAPADDR);
		/* APN Login ID */
		pProfileInfo.ConRef.NAP.Auth.PAP_ID = xmlConfigFile.findConfig(pszSection, DM_CONFIG_NAPLOGINID);
		/* APN Login Password */
		pProfileInfo.ConRef.NAP.Auth.PAP_Secret = xmlConfigFile.findConfig(pszSection, DM_CONFIG_NAPLOGINPWD);
		/* Proxy Address */
		pProfileInfo.ConRef.PX.Addr = xmlConfigFile.findConfig(pszSection, DM_CONFIG_PROXYADDR);
		/* Proxy Port */
		pProfileInfo.ConRef.PX.nPortNbr = Integer.valueOf(xmlConfigFile.findConfig(pszSection, DM_CONFIG_PROXYPORT)).intValue();
		/* Proxy Address Type */
		pProfileInfo.ConRef.PX.nAddrType = Integer.valueOf(xmlConfigFile.findConfig(pszSection, DM_CONFIG_PROXYADDRTYPE)).intValue();
		/* Proxy Use */
		pProfileInfo.ConRef.bProxyUse = Boolean.valueOf(xmlConfigFile.findConfig(pszSection, DM_CONFIG_PROXYENABLE));
		/* Static IP Use */
		pProfileInfo.ConRef.tAdvSetting.bStaticIpUse = Boolean.valueOf(xmlConfigFile.findConfig(pszSection, DM_CONFIG_STATICIPENABLE));
		/* Static IP Address */
		pProfileInfo.ConRef.tAdvSetting.szStaticIp = xmlConfigFile.findConfig(pszSection, DM_CONFIG_STATICIP);
		/* Static DNS Use */
		pProfileInfo.ConRef.tAdvSetting.bStaticDnsUse = Boolean.valueOf(xmlConfigFile.findConfig(pszSection, DM_CONFIG_STATICDNSENABLE));
		/* Static DNS1 Address */
		pProfileInfo.ConRef.tAdvSetting.szStaticDns1 = Integer.valueOf(xmlConfigFile.findConfig(pszSection, DM_CONFIG_DNSADDR1)).intValue();
		/* Static DNS2 Address */
		pProfileInfo.ConRef.tAdvSetting.szStaticDns2 = Integer.valueOf(xmlConfigFile.findConfig(pszSection, DM_CONFIG_DNSADDR2)).intValue();
		/* Application ID */
		pProfileInfo.AppID = xmlConfigFile.findConfig(pszSection, DM_CONFIG_APPID);
		/* Client Authentication Level */
		pProfileInfo.AuthLevel = xmlConfigFile.findConfig(pszSection, DM_CONFIG_CLIENT_AUTHLEVEL);
		/* Server Authentication Level */
		pProfileInfo.ServerAuthLevel = xmlConfigFile.findConfig(pszSection, DM_CONFIG_SERVER_AUTHLEVEL);
		
		return true;
	}
	
	public static boolean getAccXnodeInfoFromConfigFile(tsDBAccXNode pAccXnodeInfo, int nIndex)
	{
		tsLib.debugPrint("getAccXnodeInfoFromConfigFile", "nIndex = " + nIndex);
		String pszSection = DM_CONFIG_SECTION_ACCOUNT1;

		switch(nIndex)
		{
			case 0:
				pszSection = DM_CONFIG_SECTION_ACCOUNT1;
				break;

			case 1:
				pszSection = DM_CONFIG_SECTION_ACCOUNT2;
				break;

			case 2:
				pszSection = DM_CONFIG_SECTION_ACCOUNT3;
				break;

			default:
				break;
		}
		
		/* Account x Information */
		/* ./x/Name(Server Id) */
		pAccXnodeInfo.Account = xmlConfigFile.findConfig(DM_CONFIG_SECTION_DM_SETTING, DM_CONFIG_BASE_ACCOUNT_PATH) + "/" + xmlConfigFile.findConfig(pszSection, DM_CONFIG_SERVERID);
		/* ./x/AppAddr/x */
		pAccXnodeInfo.AppAddr = pAccXnodeInfo.Account + SYNCML_DMACC_APPADDR_PATH + "/" + xmlConfigFile.findConfig(pszSection, DM_CONFIG_APPADDR_X_NAME);
		/* ./x/AppAddr/x/Port/x */
		pAccXnodeInfo.AppAddrPort = pAccXnodeInfo.AppAddr + SYNCML_APPADDR_PORT_PATH + "/" + xmlConfigFile.findConfig(pszSection, DM_CONFIG_APPADDR_PORT_X_NAME);
		/* ./x/AppAuth/x (Server) */
		pAccXnodeInfo.ServerAppAuth = pAccXnodeInfo.Account + SYNCML_DMACC_APPAUTH_PATH + "/" + xmlConfigFile.findConfig(pszSection, DM_CONFIG_APPAUTH_SERVER_X_NAME);
		/* ./x/AppAuth/x (Client) */
		pAccXnodeInfo.ClientAppAuth = pAccXnodeInfo.Account + SYNCML_DMACC_APPAUTH_PATH + "/" + xmlConfigFile.findConfig(pszSection, DM_CONFIG_APPAUTH_CLIENT_X_NAME);
		/* ./x/ToConRef/x */
		pAccXnodeInfo.ToConRef = pAccXnodeInfo.Account + SYNCML_DMACC_TOCONREF_PATH + "/" + xmlConfigFile.findConfig(pszSection, DM_CONFIG_TOCONREF_X_NAME);

		return true;
	}
}

