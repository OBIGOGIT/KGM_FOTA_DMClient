package com.tsdm.net;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.PowerManager;

import com.tsdm.tsService;
import com.tsdm.agent.dmDefineDevInfo;
import com.tsdm.agent.dmDevinfoAdapter;
import com.tsdm.db.tsDB;
import com.tsdm.db.tsDefineDB;
import com.tsdm.db.tsDBURLParser;
import com.tsdm.db.tsdmInfoConRef;
import com.tsdm.db.tsdmDB;
import com.tsdm.adapt.tsDefineIdle;
import com.tsdm.adapt.tsDmHmacData;
import com.tsdm.adapt.tsDmParamConnectfailmsg;
import com.tsdm.adapt.tsLib;
import com.tsdm.adapt.tsDmMsg;
import com.tsdm.agent.dmDefineMsg;
import com.tsdm.agent.dlAgentHandler;
import com.tsdm.agent.dmDefineUIEvent;

public class netHttpAdapter implements netDefine, dmDefineMsg, tsDefineIdle, dmDefineDevInfo, dmDefineUIEvent, tsDefineDB
{
	public int									RECEIVE_BUFFER_SIZE		= 5 * 1024 * 1024;
	public static boolean						isConnected				= false;
	public static boolean						isProxyServer			= true;

	public static netHttpObj[]					pHttpObj				= null;
	public static HttpURLConnection				conn					= null;
	public static HttpsURLConnection			sconn					= null;
	private static String						cookie					= null;
	private Proxy								conProxy				= null;
	private dlAgentHandler dlhandler									= null;

	private static TrustManager[]				trustManagers;
	private tsDmHmacData HMacData;

	static boolean								gUserCancel				= false;
	static boolean								nNetworkReuse			= false;
	private int									nHttpBodyLength;

	public static int							recvRetCount;

	private static Socket						mSocket					= null;
	private static SSLSocket					mSSLSocket				= null;
	private SSLContext							mSSLContext				= null;
	private SSLSocketFactory					mSSLFactory				= null;
	private static InputStream					mInput					= null;
	private static OutputStream					mOutput					= null;
	private String								pHttpHeaderData			= null;

	public static ConnectivityManager			mConnMgr				= null;
	public static ConnectivityManager			mConnectivityListener	= null;
	public static int							Tpappid					= 0;

	private ByteArrayOutputStream				m_bufDebug;
	public static boolean 						_DM_TP_LOG_ON_ = false;
	private static int							nHttpDebugCount			= 0;
	private static PowerManager.WakeLock		wakeLock				= null;
	private WifiManager.WifiLock 				wifiLock 				= null;

	public netHttpAdapter()
	{
		super();

		if (pHttpObj == null)
			pHttpObj = new netHttpObj[2];
	}

	public void tpInit(int appId)
	{
		tsLib.debugPrint(DEBUG_NET, "appId = "+appId);

		if (pHttpObj[appId] == null)
			pHttpObj[appId] = new netHttpObj();

		GetProxyData();
		httpInit(appId);

		if (_DM_TP_LOG_ON_)
		{
			if (m_bufDebug == null)
				m_bufDebug = new ByteArrayOutputStream();

			m_bufDebug.reset();
			nHttpDebugCount = 0;
		}
	}

	private void GetProxyData()
	{
	}

	public void tpApnEnable(int appid)
	{
		Tpappid = appid;
	}

	public static void tpApnDisable()
	{
		tsLib.debugPrint(DEBUG_NET, "");

		if (mConnectivityListener != null)
		{
			mConnectivityListener = null;
		}
	}

	public int tpEnsurerouteTohost(String URL)
	{
		int nRet = TP_RET_OK;
		int inetAddr;

		tsLib.debugPrint(DEBUG_NET, "URL= " + URL);

		inetAddr = lookupHost(URL);
		if (inetAddr == -1)
		{
			tsLib.debugPrint(DEBUG_NET, "Cannot establish route for " + URL + ": Unknown host");
			return TP_RET_INIT_FAIL;
		}
		else
		{
			tsLib.debugPrint(DEBUG_NET, "Cannot establish route to proxy " + inetAddr);
		}
		return nRet;
	}

	public int tpbeginConnectivity()
	{
		tsLib.debugPrint(DEBUG_NET, "");

		int result = 0;

		tsLib.debugPrint(DEBUG_NET, "result= " + result);
		return result;
	}

	public static void tpendConnectivity()
	{
		tsLib.debugPrint(DEBUG_NET, "");
	}

	public int tpApnOpen(int appId)
	{
		int nRet = TP_RET_OK;
		String Url = null;
		int result = 0;

		tsLib.debugPrint(DEBUG_NET, "");

		tpApnEnable(appId);

		result = tpbeginConnectivity();
		//-1 means errors  
		// 0 means already enabled  
		// 1 means enabled
		// other values can be returned, because this method is vendor specific

		try
		{
			if (result == 1)
			{
				if (getIsProxy())
				{
					Url = pHttpObj[appId].pProxyAddr;
					tpEnsurerouteTohost(Url);
				}
				else
				{
					Url = pHttpObj[appId].pServerAddr;
					tsLib.debugPrint(DEBUG_NET, "Url is " + Url);
					InetAddress address = null;
					try
					{
						address = InetAddress.getByName(Url);
						if (address != null)
							tpEnsurerouteTohost(address.getHostAddress());
					}
					catch (UnknownHostException e)
					{
						tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
					}
				}

				if (appId == SYNCMLDM)
					tsDmMsg.taskSendMessage(TASK_MSG_DM_TCPIP_OPEN, null, null);
				else
					tsDmMsg.taskSendMessage(TASK_MSG_DL_TCPIP_OPEN, null, null);
			}
			else if (result == 0)
			{
				tsLib.debugPrint(DEBUG_NET, "Extending DM connectivity returned " + result + " APN_REQUEST_STARTED");
			}
			else
			{
				tsLib.debugPrint(DEBUG_NET, "Extending DM connectivity returned " + result + "APN Error");
				tpApnClose();
				nRet = TP_RET_CONNECTION_FAIL;
			}
		}
		catch (Exception ex)
		{
			tsLib.debugPrintException(DEBUG_NET, ex.toString());
		}
		return nRet;
	}

	public int tpApnActive()
	{
		tsLib.debugPrint(DEBUG_NET, "");

		int result = tpbeginConnectivity();
		if (result == 1)
		{
			String Url = null;
			if (getIsProxy())
			{
				Url = pHttpObj[Tpappid].pProxyAddr;
				tpEnsurerouteTohost(Url);
			}
			else
			{
				Url = pHttpObj[Tpappid].pServerAddr;
				InetAddress address = null;
				try
				{
					address = InetAddress.getByName(Url);
					if (address != null)
						tpEnsurerouteTohost(address.getHostAddress());
				}
				catch (UnknownHostException e)
				{
					tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
				}
			}

			if (Tpappid == SYNCMLDM)
				tsDmMsg.taskSendMessage(TASK_MSG_DM_TCPIP_OPEN, null, null);
			else
				tsDmMsg.taskSendMessage(TASK_MSG_DL_TCPIP_OPEN, null, null);

			return TP_RET_OK;
		}
		else if (result == 0)
		{
			tsLib.debugPrint(DEBUG_NET, "Extending DM connectivity returned " + result + " iAPN_REQUEST_STARTED");

			return TP_RET_OK;
		}
		else
		{
			tsLib.debugPrint(DEBUG_NET, "Extending DM connectivity returned " + result + "APN Error");
			tpApnClose();
			return TP_RET_CONNECTION_FAIL;
		}
	}

	@SuppressLint("InvalidWakeLockTag")
	public int tpOpen(int appId) throws SocketTimeoutException
	{
		int nRet = TP_RET_OK;

		tsLib.debugPrint(DEBUG_NET, "appId: "+appId);

		if (pHttpObj[appId] == null)
		{
			tpInit(appId);
		}

/*		if (wakeLock == null)
		{
			PowerManager pm = (PowerManager) tsService.getContext().getSystemService(Context.POWER_SERVICE);
			wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "wakeLock");
			wakeLock.setReferenceCounted(false);
			wakeLock.acquire();
		}
		
		if (wifiLock == null) 
		{
			WifiManager wifiManager = (WifiManager) tsService.getContext().getSystemService(Context.WIFI_SERVICE);
			wifiLock = wifiManager.createWifiLock("wifilock"); 
			wifiLock.setReferenceCounted(false); 
			wifiLock.acquire();
		}*/

		new netTimerConnect(true, appId);

		SocketAddress socketAddress = null;
		
		if (pHttpObj[appId].protocol == TP_TYPE_HTTP)
		{
			if (getIsProxy())
			{
				socketAddress = conProxy.address();
				tsLib.debugPrint(DEBUG_NET, "conProxy is " + conProxy.toString());
			}
			else
				socketAddress = new InetSocketAddress(pHttpObj[appId].pServerAddr, pHttpObj[appId].nServerPort);

			try
			{
				mSocket = new Socket();
				mSocket.connect(socketAddress, CONNECT_TIME_OUT);

				mInput = new BufferedInputStream(mSocket.getInputStream(), 4 * 1024);
				mOutput = new BufferedOutputStream(mSocket.getOutputStream(), 1024);
			}
			catch (IOException e)
			{
				tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
				netTimerConnect.endTimer();
				return TP_RET_CONNECTION_FAIL;
			}
			catch (Exception e)
			{
				tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
				netTimerConnect.endTimer();
				return TP_RET_CONNECTION_FAIL;
			}
		}
		else if (pHttpObj[appId].protocol == TP_TYPE_HTTPS)
		{
			String SSLHost = null, SSLPXHost = null;
			int SSLPort = 0, SSLPXPort = 0;
			try
			{
				mSSLContext = SSLContext.getInstance("TLS");
				if (mSSLContext != null)
				{
					// here, trust managers is a single trust-all manager
					TrustManager[] trustManagers = new TrustManager[] {new X509TrustManager()
					{
						@Override
						public X509Certificate[] getAcceptedIssuers()
						{
							return null;
						}
						@Override
						public void checkClientTrusted(X509Certificate[] certs, String authType) throws CertificateException // Noncompliant
						{
							try {
								certs[0].checkValidity();
							} catch (Exception e) {
								throw new CertificateException("Certificate not valid or trusted.");
							}
						}
						@Override
						public void checkServerTrusted(X509Certificate[] certs, String authType) throws CertificateException // Noncompliant
						{
							try {
								certs[0].checkValidity();
							} catch (Exception e) {
								throw new CertificateException("Certificate not valid or trusted.");
							}
						}
					}};

					mSSLContext.init(null, trustManagers, new SecureRandom());
					mSSLContext.getServerSessionContext().setSessionTimeout(CONNECT_TIME_OUT);
					mSSLFactory = mSSLContext.getSocketFactory();
				}
			}
			catch (Exception t)
			{
				tsLib.debugPrintException(DEBUG_NET, "HttpsConnection: failed to initialize the socket factory");
				netTimerConnect.endTimer();
				return TP_RET_CONNECTION_FAIL;
			}
			if (getIsProxy())
			{
				mSocket = new Socket();
				SSLPXHost = pHttpObj[appId].pProxyAddr;
				SSLPXPort = pHttpObj[appId].proxyPort;

				SSLHost = pHttpObj[appId].pServerAddr;
				SSLPort = pHttpObj[appId].nServerPort;

				InetSocketAddress tunnelAddr = new InetSocketAddress(SSLPXHost, SSLPXPort);
				try
				{
					mSocket.connect(tunnelAddr, CONNECT_TIME_OUT);

					nRet = tpTunnelHandshake(mSocket, SSLHost, SSLPort, appId);
					if (nRet != TP_RET_OK)
					{
						netTimerConnect.endTimer();
						return TP_RET_CONNECTION_FAIL;
					}

					// Overlay tunnel socket with SSL
					mSSLSocket = (SSLSocket) mSSLFactory.createSocket(mSocket, SSLHost, SSLPort, true);
					mSSLSocket.setSoTimeout(CONNECT_TIME_OUT);
					mSSLSocket.addHandshakeCompletedListener(new HandshakeCompletedListener()
					{
						public void handshakeCompleted(HandshakeCompletedEvent event)
						{
							tsLib.debugPrint(DEBUG_NET, ">>>>>>> Handshake finished! <<<<<<<<");
						}
					});
					mSSLSocket.startHandshake();
				}
				catch (IOException e)
				{
					tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
					netTimerConnect.endTimer();
					return TP_RET_CONNECTION_FAIL;
				}
				catch (Exception e)
				{
					tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
					netTimerConnect.endTimer();
					return TP_RET_CONNECTION_FAIL;
				}
			}
			else
			{
				SSLHost = pHttpObj[appId].pServerAddr;
				SSLPort = pHttpObj[appId].nServerPort;
				try
				{
					mSSLSocket = (SSLSocket) mSSLFactory.createSocket(SSLHost, SSLPort);
					mSSLSocket.setSoTimeout(CONNECT_TIME_OUT);
					// mSSLSocket.setNeedClientAuth(true);

					mSSLSocket.addHandshakeCompletedListener(new HandshakeCompletedListener()
					{
						public void handshakeCompleted(HandshakeCompletedEvent event)
						{
							tsLib.debugPrint(DEBUG_NET, "Handshake finished!");
						}
					});
					mSSLSocket.startHandshake();
				}
				catch (IOException e)
				{
					tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
					netTimerConnect.endTimer();
					return TP_RET_CONNECTION_FAIL;
				}
				catch (Exception e)
				{
					tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
					netTimerConnect.endTimer();
					return TP_RET_CONNECTION_FAIL;
				}
			}

			try
			{
				mInput = new BufferedInputStream(mSSLSocket.getInputStream(), 4 * 1024);
				mOutput = new BufferedOutputStream(mSSLSocket.getOutputStream(), 1024);
			}
			catch (UnknownHostException e)
			{
				tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
				netTimerConnect.endTimer();
				return TP_RET_CONNECTION_FAIL;
			}
			catch (IOException e)
			{
				tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
				netTimerConnect.endTimer();
				return TP_RET_CONNECTION_FAIL;
			}
			catch (Exception e)
			{
				tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
				netTimerConnect.endTimer();
				return TP_RET_CONNECTION_FAIL;
			}
		}
		else
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, "Other ProtocolType");
		}

		netTimerConnect.endTimer();
		setIsConnected(true);
		return TP_RET_OK;
	}
	
	private int tpTunnelHandshake(Socket tunnel, String host, int port, int appId) throws IOException
	{
		String msg = tpMakeSSLTunneling(appId);

		tsLib.debugPrint(DEBUG_NET, "");

		if (tsLib.isEmpty(msg))
			return SDM_RET_FAILED;

		mOutput = tunnel.getOutputStream();

		byte b[];
		try
		{
			b = msg.getBytes("UTF-8");
		}
		catch (UnsupportedEncodingException ignored)
		{
			b = msg.getBytes();
		}
		mOutput.write(b);
		mOutput.flush();

		byte reply[] = new byte[200];
		int replyLen = 0;
		int newlinesSeen = 0;
		boolean headerDone = false;

		mInput = tunnel.getInputStream();

		while (newlinesSeen < 2)
		{
			int i = mInput.read();
			if (i < 0)
			{
				tsLib.debugPrint(DEBUG_NET, "Unable to tunnel");
				try
				{
					mInput.close();
					mOutput.close();
				}
				catch (IOException e)
				{
					tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
				}
				return TP_RET_CONNECTION_FAIL;
			}
			if (i == '\n')
			{
				headerDone = true;
				++newlinesSeen;
			}
			else if (i != '\r')
			{
				newlinesSeen = 0;
				if (!headerDone && replyLen < reply.length)
				{
					reply[replyLen++] = (byte) i;
				}
			}
		}

		String replyStr;
		try
		{
			replyStr = new String(reply, 0, replyLen, "UTF-8");
		}
		catch (UnsupportedEncodingException ignored)
		{
			replyStr = new String(reply, 0, replyLen);
		}

		tsLib.debugPrint(DEBUG_NET, "Proxy returns \"" + replyStr + "\"");
		if (replyStr.startsWith("HTTP/1.1 200") || replyStr.startsWith("HTTP/1.0 200"))
		{
			return TP_RET_OK;
		}

		tsLib.debugPrintException(DEBUG_EXCEPTION, "Unable to tunnel through Proxy");
		return TP_RET_CONNECTION_FAIL;
	}
	
	private void HTTP_APPEND_HEADER(String x, String y)
	{
		if (x != null)
		{
			pHttpHeaderData = pHttpHeaderData.concat(y);
			pHttpHeaderData = pHttpHeaderData.concat(x);
			pHttpHeaderData = pHttpHeaderData.concat(HTTP_CRLF_STRING);

		}
	}

	private String httpPsrMakeHeader(int conLength, int appId)
	{
		tsLib.debugPrint(DEBUG_NET, "");
		String OpenMode = tpGetHttpOpenMode(appId);
		if (OpenMode == null)
			return null;

		pHttpHeaderData = OpenMode;

		if (!tsLib.isEmpty(pHttpObj[appId].pRequestUri))
		{
			pHttpHeaderData = pHttpHeaderData.concat(" ");
			pHttpHeaderData = pHttpHeaderData.concat(pHttpObj[appId].pRequestUri);
		}
		else
		{
			tsLib.debugPrint(DEBUG_NET, "PATH is NULL. Checking the pHttpObj->pRequestUri");
		}
		pHttpHeaderData = pHttpHeaderData.concat(" ");
		if (!tsLib.isEmpty(pHttpObj[appId].pHttpVersion))
		{
			pHttpHeaderData = pHttpHeaderData.concat(pHttpObj[appId].pHttpVersion);
		}
		pHttpHeaderData = pHttpHeaderData.concat("\r\n");

		HTTP_APPEND_HEADER(HTTP_CACHECONTROL, "Cache-Control: ");

		if (!tsLib.isEmpty(pHttpObj[appId].pHttpConnection))
		{
			HTTP_APPEND_HEADER(pHttpObj[appId].pHttpConnection, "Connection: ");
		}

		if (!tsLib.isEmpty(pHttpObj[appId].pHttpUserAgent))
		{
			HTTP_APPEND_HEADER(pHttpObj[appId].pHttpUserAgent, "User-Agent: ");
		}

		if (!tsLib.isEmpty(pHttpObj[appId].pHttpAccept))
		{
			HTTP_APPEND_HEADER(pHttpObj[appId].pHttpAccept, "Accept: ");
		}

		HTTP_APPEND_HEADER(HTTP_LANGUAGE, "Accept-Language: ");
		HTTP_APPEND_HEADER("utf-8", "Accept-Charset: ");

		if (!tsLib.isEmpty(pHttpObj[appId].pServerAddr))
		{
			String ServerHost = pHttpObj[appId].pServerAddr + ":" + String.valueOf(pHttpObj[appId].nServerPort);
			HTTP_APPEND_HEADER(ServerHost, "Host: ");
		}

		if (!tsLib.isEmpty(pHttpObj[appId].pHttpMimeType))
		{
			HTTP_APPEND_HEADER(pHttpObj[appId].pHttpMimeType, "Content-Type: ");
		}
		
		if(!tsLib.isEmpty(pHttpObj[appId].pHttpcookie))
		{
			HTTP_APPEND_HEADER(pHttpObj[appId].pHttpcookie, "Cookie: ");
		}

		if (!tsLib.isEmpty(pHttpObj[appId].pContentRange))
		{
			HTTP_APPEND_HEADER("bytes=" + pHttpObj[appId].pContentRange + "-", "Range: ");
		}

		pHttpHeaderData = pHttpHeaderData.concat("Content-Length: ");
		pHttpHeaderData = pHttpHeaderData.concat(String.valueOf(conLength));
		pHttpHeaderData = pHttpHeaderData.concat(HTTP_CRLF_STRING);

		if (pHttpObj[appId].pHmacData != null &&  Arrays.hashCode(pHttpObj[appId].pHmacData) !=0) //pHttpObj[appId].pHmacData.hashCode() != 0)
		{
			HTTP_APPEND_HEADER(new String(pHttpObj[appId].pHmacData), "x-syncml-hmac: ");
			pHttpObj[appId].pHmacData = null;
		}
		pHttpHeaderData = pHttpHeaderData.concat(HTTP_CRLF_STRING);

		tsLib.debugPrint(DEBUG_NET, "\r\n [_____Make Header_____]\r\n" + pHttpHeaderData);

		return pHttpHeaderData;
	}

	public int tpSendData(byte[] pData, int dataSize, int appId) throws SocketTimeoutException
	{
		int nRet = TP_RET_OK;
		String pHttpHeaderData = null;
		byte[] pSendBuffer = null;
		int SendDataLen = 0;

		tsLib.debugPrint(DEBUG_NET, "dataSize = " +dataSize);

		if (mOutput == null)
		{
			return TP_RET_CONNECTION_FAIL;
		}

		try
		{
			if (mInput != null)
			{
				mInput.mark(1);
				mInput.reset();
			}
		}
		catch (IOException e)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
			netTimerSend.endTimer();
			return TP_RET_SEND_FAIL;
		}

		if (pData != null)
		{
			pSendBuffer = pData;
			SendDataLen = pSendBuffer.length;
		}
		else
		{
			SendDataLen = 0;
		}

		if (pHttpObj[appId].protocol == TP_TYPE_HTTP)
		{
			try
			{
				mSocket.setSoTimeout(SEND_TIME_OUT);
				new netTimerSend(appId);
			}
			catch (SocketException e)
			{
				tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
				netTimerSend.endTimer();
				return TP_RET_SEND_FAIL;
			}
			pHttpHeaderData = httpPsrMakeHeader(dataSize, appId);
			if (pHttpHeaderData == null)
			{
				tsLib.debugPrint(DEBUG_NET, "pHttpHeaderData is null");
				netTimerSend.endTimer();
				return TP_RET_SEND_FAIL;
			}
		}
		else if (pHttpObj[appId].protocol == TP_TYPE_HTTPS)
		{
			try
			{
				mSSLSocket.setSoTimeout(SEND_TIME_OUT);
				new netTimerSend(appId);
			}
			catch (SocketException e)
			{
				tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
				netTimerSend.endTimer();
				return TP_RET_SEND_FAIL;
			}
			if (getIsProxy())
			{
				pHttpHeaderData = httpPsrMakeSslTunnelHeader(dataSize, appId);
				if (pHttpHeaderData == null)
				{
					netTimerSend.endTimer();
					return TP_RET_SEND_FAIL;
				}
			}
			else
			{
				pHttpHeaderData = httpPsrMakeHeader(dataSize, appId);
				if (pHttpHeaderData == null)
				{
					netTimerSend.endTimer();
					return TP_RET_SEND_FAIL;
				}
			}
		}
		else
		{
			tsLib.debugPrint(DEBUG_NET, "Other ProtocolType");
			netTimerSend.endTimer();
			return TP_RET_HTTP_RES_FAIL;
		}

		if (_DM_TP_LOG_ON_)
		{
			if (appId == SYNCMLDM)
			{
				if (pSendBuffer != null)
				{
					httpLibDump(pSendBuffer, 0, pSendBuffer.length);
					try
					{
						if (m_bufDebug != null)
						{
							m_bufDebug.reset();
							m_bufDebug.write(pSendBuffer, 0, pSendBuffer.length);
							httpWriteFile(String.format("data/data/com.tsdm/httpdata" + String.valueOf(nHttpDebugCount) + ".wbxml"), m_bufDebug.toByteArray());
						}
					}
					catch (Exception ex)
					{
						tsLib.debugPrintException(DEBUG_EXCEPTION, ex.toString());
					}
					nHttpDebugCount++;
				}
			}
		}

		byte[] headbyte = pHttpHeaderData.getBytes();
		byte[] SendBuf = new byte[headbyte.length + SendDataLen];

		System.arraycopy(headbyte, 0, SendBuf, 0, headbyte.length);
		if (SendDataLen != 0 && pSendBuffer != null)
			System.arraycopy(pSendBuffer, 0, SendBuf, pHttpHeaderData.getBytes().length, SendDataLen);

		OutputStream out = mOutput;
		int position = 0;
		int totalLength = SendBuf.length;
		try
		{
			while (position != totalLength)
			{
				int sendLength = totalLength - position > 256 ? 256 : totalLength - position;
				try
				{
					out.write(SendBuf, position, sendLength);
				}
				catch (IOException e)
				{
					tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
					netTimerSend.endTimer();
					return TP_RET_SEND_FAIL;
				}
				position += sendLength;
			}
			out.flush();
		}
		catch (IOException e)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
			netTimerSend.endTimer();
			return TP_RET_SEND_FAIL;
		}
		netTimerSend.endTimer();
		return nRet;

	}

	public String tpMakeSSLTunneling(int appId)
	{
		String Header = null;

		try
		{
			tpSetHttpObj(pHttpObj[appId].pServerURL, null, null, HTTP_METHOD_CONNECT, appId, false);
		}
		catch (NullPointerException e)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
			return null;
		}
		Header = httpMakeSSLTunnelHeader(appId);

		return Header;
	}

	public String httpMakeSSLTunnelHeader(int appId)
	{
		String Header = null;
		pHttpObj[appId].nTunnelConnected = TP_SSL_TUNNEL_CONNECTING;
		Header = httpPsrMakeSslTunnelHeader(0, appId);
		return Header;
	}

	public String httpPsrMakeSslTunnelHeader(int conLength, int appId)
	{
		tsLib.debugPrint(DEBUG_NET, "");
		String ServerURL = null;
		String OpenMode = tpGetHttpOpenMode(appId);
		if (OpenMode == null)
			return null;

		pHttpHeaderData = OpenMode;

		if (pHttpObj[appId].nHttpOpenMode.equals(HTTP_METHOD_CONNECT))
		{
			if (!tsLib.isEmpty(pHttpObj[appId].pRequestUri))
			{
				pHttpHeaderData = pHttpHeaderData.concat(" ");
				ServerURL = netHttpUtil.tpParserServerAddrWithPort(pHttpObj[appId].pRequestUri);
				pHttpHeaderData = pHttpHeaderData.concat(ServerURL);
			}
			else
			{
				tsLib.debugPrint(DEBUG_NET, "PATH is NULL. Checking the pHttpObj->pRequestUri");
			}

			pHttpHeaderData = pHttpHeaderData.concat(" ");
			if (!tsLib.isEmpty(pHttpObj[appId].pHttpVersion))
			{
				pHttpHeaderData = pHttpHeaderData.concat(pHttpObj[appId].pHttpVersion);
			}
			pHttpHeaderData = pHttpHeaderData.concat("\r\n");

			if (!tsLib.isEmpty(pHttpObj[appId].pServerAddr))
			{
				String ServerHost = pHttpObj[appId].pServerAddr + ":" + String.valueOf(pHttpObj[appId].nServerPort);
				HTTP_APPEND_HEADER(ServerHost, "Host: ");
			}
		}
		else
		{
			if (!tsLib.isEmpty(pHttpObj[appId].pRequestUri))
			{
				pHttpHeaderData = pHttpHeaderData.concat(" ");
				pHttpHeaderData = pHttpHeaderData.concat(pHttpObj[appId].pRequestUri);
			}
			else
			{
				tsLib.debugPrint(DEBUG_NET, "PATH is NULL. Checking the pHttpObj->pRequestUri");
			}

			pHttpHeaderData = pHttpHeaderData.concat(" ");
			if (!tsLib.isEmpty(pHttpObj[appId].pHttpVersion))
			{
				pHttpHeaderData = pHttpHeaderData.concat(pHttpObj[appId].pHttpVersion);
			}
			pHttpHeaderData = pHttpHeaderData.concat("\r\n");

			if (!tsLib.isEmpty(pHttpObj[appId].pServerAddr))
			{
				String ServerHost = pHttpObj[appId].pServerAddr + ":" + String.valueOf(pHttpObj[appId].nServerPort);
				HTTP_APPEND_HEADER(ServerHost, "Host: ");

			}
		}

		HTTP_APPEND_HEADER(HTTP_CACHECONTROL, "Cache-Control: ");

		if (!tsLib.isEmpty(pHttpObj[appId].pHttpConnection))
		{
			HTTP_APPEND_HEADER(pHttpObj[appId].pHttpConnection, "Connection: ");
		}

		if (!tsLib.isEmpty(pHttpObj[appId].pHttpUserAgent))
		{
			HTTP_APPEND_HEADER(pHttpObj[appId].pHttpUserAgent, "User-Agent: ");
		}

		if (!tsLib.isEmpty(pHttpObj[appId].pHttpAccept))
		{
			HTTP_APPEND_HEADER(pHttpObj[appId].pHttpAccept, "Accept: ");
		}

		HTTP_APPEND_HEADER(HTTP_LANGUAGE, "Accept-Language: ");
		HTTP_APPEND_HEADER("utf-8", "Accept-Charset: ");

		if (!tsLib.isEmpty(pHttpObj[appId].pHttpMimeType))
		{
			HTTP_APPEND_HEADER(pHttpObj[appId].pHttpMimeType, "Content-Type: ");
		}
		
		if(!tsLib.isEmpty(pHttpObj[appId].pHttpcookie))
		{
			HTTP_APPEND_HEADER(pHttpObj[appId].pHttpcookie, "Cookie: ");
		}

		if (!tsLib.isEmpty(pHttpObj[appId].pContentRange))
		{
			HTTP_APPEND_HEADER("bytes=" + pHttpObj[appId].pContentRange + "-", "Range: ");
		}

		pHttpHeaderData = pHttpHeaderData.concat("Content-Length: ");
		pHttpHeaderData = pHttpHeaderData.concat(String.valueOf(conLength));
		pHttpHeaderData = pHttpHeaderData.concat(HTTP_CRLF_STRING);

		if (pHttpObj[appId].pHmacData != null && Arrays.hashCode(pHttpObj[appId].pHmacData) !=0) //pHttpObj[appId].pHmacData.hashCode() != 0)
		{
			HTTP_APPEND_HEADER(new String(pHttpObj[appId].pHmacData), "x-syncml-hmac: ");
			pHttpObj[appId].pHmacData = null;
		}
		pHttpHeaderData = pHttpHeaderData.concat(HTTP_CRLF_STRING);

		tsLib.debugPrint(DEBUG_NET, "\r\n [_____SSL Proxy Make Header_____]\r\n" + pHttpHeaderData);

		return pHttpHeaderData;
	}

	public int tpReceiveData(ByteArrayOutputStream pData, int appId) throws SocketTimeoutException
	{
		int nRet = TP_RET_OK;
		int nFumoStatus = DM_FUMO_STATE_NONE;
		byte[] actualBuff = null;
		int chunkedlen = 0;
		ByteArrayInputStream aBuff = null;
		InputStream in = mInput;

		int bytesread = 0;
		int ContentBytesread = 0;
		int actual = 0;

		tsLib.debugPrint(DEBUG_NET, "");

		if (in == null)
		{
			return TP_RET_CONNECTION_FAIL;
		}

		if (pHttpObj[appId].protocol == TP_TYPE_HTTP)
		{
			try
			{
				mSocket.setSoTimeout(RECEIVE_TIME_OUT);
				new netTimerReceive(appId);
			}
			catch (SocketException e)
			{
				tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
				tsLib.debugPrint(DEBUG_NET, "Time out");
				netTimerReceive.endTimer();
				return TP_RET_RECEIVE_FAIL;
			}
		}
		else
		{
			try
			{
				mSSLSocket.setSoTimeout(RECEIVE_TIME_OUT);
				new netTimerReceive(appId);
			}
			catch (SocketException e)
			{
				tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
				tsLib.debugPrint(DEBUG_NET, "Time out");
				netTimerReceive.endTimer();
				return TP_RET_RECEIVE_FAIL;
			}
		}

		try
		{
			nRet = httpHeaderParser(appId, in);
		}
		catch (Exception e)
		{
			nRet = TP_RET_RECEIVE_FAIL;
			tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
		}

		if (nRet != TP_RET_OK)
		{
			netTimerReceive.endTimer();
			return TP_RET_RECEIVE_FAIL;
		}

		if (pHttpObj[appId].nHttpReturnStatusValue == 503)
		{
			netTimerReceive.endTimer();
			return TP_RET_HTTP_CONNECTION_POOL;
		}

		if (pHttpObj[appId].nHttpReturnStatusValue < 200 || pHttpObj[appId].nHttpReturnStatusValue > 300)
		{
			netTimerReceive.endTimer();
			return TP_RET_HTTP_RES_FAIL;
		}

		if (pHttpObj[appId].nContentLength == 0 && pHttpObj[appId].nTransferCoding != HTTP_CHUNKED)
		{
			if (!tsLib.isEmpty(pHttpObj[appId].pContentRange))
			{
				tsLib.debugPrint(DEBUG_EXCEPTION, "Content-length 0, Content-Range Use");
				pHttpObj[appId].nContentLength = httpPsrGetContentLengthByRange(pHttpObj[appId].pContentRange);
			}
		}
		else if (pHttpObj[appId].nContentLength == 0 || pHttpObj[appId].nContentLength == UNDEFINED_CONTENT_LENGTH)
		{
			pHttpObj[appId].nContentLength = 0;
		}

		if (pHttpObj[appId].nContentLength != 0 && pHttpObj[appId].nTransferCoding == HTTP_CHUNKED)
		{
			pHttpObj[appId].nContentLength = 0;
		}

		if (pHttpObj[appId].nContentLength > 0)
			nHttpBodyLength = pHttpObj[appId].nContentLength;
		else
			nHttpBodyLength = 0;

		nFumoStatus = tsdmDB.dmdbGetFUMOStatus();

		if (nFumoStatus == DM_FUMO_STATE_DOWNLOAD_IN_PROGRESS)
     	{
			if (dlhandler == null)
				dlhandler = new dlAgentHandler();
		}

		if (pHttpObj[appId].nTransferCoding == HTTP_CHUNKED)
		{
			tsLib.debugPrint(DEBUG_NET, "HTTP_CHUNKED");
			if (appId == SYNCMLDM)
			{
				pHttpObj[appId].pReceiveBuffer = new byte[RECEIVE_BUFFER_SIZE];
				try
				{
					while ((chunkedlen = httpPsrChunkSizeParsing(in)) > 0)
					{
						tsLib.debugPrint(DEBUG_NET, "chunkedlen:" + chunkedlen);
						pHttpObj[appId].pChunkBuffer = new byte[chunkedlen];
						while (chunkedlen != ContentBytesread)
						{
							if ((actual = in.read(pHttpObj[appId].pChunkBuffer, ContentBytesread, chunkedlen - ContentBytesread)) > 0)
							{
								tsLib.debugPrint(DEBUG_NET, "ContentBytesread:" + ContentBytesread + " , actual :" + actual);
								actualBuff = new byte[actual];
								aBuff = new ByteArrayInputStream(pHttpObj[appId].pChunkBuffer, ContentBytesread, actual);
								// Defects : Close open streams in finally() blocks
								try
								{
									int ret = aBuff.read(actualBuff);
									if(ret == -1)
									{
										tsLib.debugPrint(DEBUG_EXCEPTION, "Buff read fail");
									}
								}
								catch (IOException e)
								{
									tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
								}
								finally
								{
									try
									{
										if (aBuff != null)
											aBuff.close();
									}
									catch (IOException e)
									{
										tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
									}
								}
								System.arraycopy(actualBuff, 0, pHttpObj[appId].pReceiveBuffer, ContentBytesread, actualBuff.length);
								ContentBytesread += actual;

								actual = -1;
							}
						}
						chunkedHeadRead(in);
						chunkedlen = 0;
					}
				}
				catch (IOException e)
				{
					tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
					netTimerReceive.endTimer();
					return TP_RET_RECEIVE_FAIL;
				}

				nHttpBodyLength = ContentBytesread;
				ContentBytesread = 0;
				tsLib.debugPrint(DEBUG_NET, "CHUNKED pHttpObj[appId].pReceiveBuffer.length = " + pHttpObj[appId].pReceiveBuffer.length);
				tsLib.debugPrint(DEBUG_NET, "CHUNKED nHttpBodyLength = " + nHttpBodyLength);

				if (nFumoStatus != DM_FUMO_STATE_DOWNLOAD_IN_PROGRESS)
				// nFumoStatus != SCOMO_DOWNLOAD_STATUS_DOWNLOAD_PROGRESSING
				{
					pData.reset();
					if (nHttpBodyLength > 0)
						pData.write(pHttpObj[appId].pReceiveBuffer, 0, nHttpBodyLength);
					else
						pData = null;
				}
			}
			else
			{
				if (nFumoStatus != DM_FUMO_STATE_DOWNLOAD_IN_PROGRESS)
				{
					pHttpObj[appId].pReceiveBuffer = new byte[RECEIVE_BUFFER_SIZE];
					tsLib.debugPrint(DEBUG_EXCEPTION, "DL MODE BUT NOT DOWNLOAD_IN_PROGRESS");
				}

				try
				{
					while ((chunkedlen = httpPsrChunkSizeParsing(in)) > 0)
					{
						tsLib.debugPrint(DEBUG_NET, "chunkedlen:" + chunkedlen);
						pHttpObj[appId].pChunkBuffer = new byte[chunkedlen];
						while (chunkedlen != ContentBytesread)
						{
							if ((actual = in.read(pHttpObj[appId].pChunkBuffer, ContentBytesread, chunkedlen - ContentBytesread)) > 0)
							{
								tsLib.debugPrint(DEBUG_NET, "ContentBytesread:" + ContentBytesread + " , bytesread:" + bytesread + " , actual :" + actual);
								actualBuff = new byte[actual];
								aBuff = new ByteArrayInputStream(pHttpObj[appId].pChunkBuffer, ContentBytesread, actual);
								try
								{
									int ret = aBuff.read(actualBuff);
									if(ret == -1)
									{
										tsLib.debugPrint(DEBUG_EXCEPTION, "Buff read fail");
									}
								}
								catch (IOException e)
								{
									tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
								}
								finally
								{
									try
									{
										if (aBuff != null)
											aBuff.close();
									}
									catch (IOException e)
									{
										tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
									}
								}

								if (nFumoStatus == DM_FUMO_STATE_DOWNLOAD_IN_PROGRESS)
								{
									nRet = dlhandler.dlWriteFirmwareObject(actual, actualBuff);
									netTimerReceive.endTimer();
									new netTimerReceive(appId);

									if (nRet == TS_ERR_NO_MEM_READY)
										return TP_RET_FILE_ERROR;
									else if (nRet != SDL_RET_OK)
										return TP_RET_RECEIVE_FAIL;
								}
								else if (nFumoStatus == DM_FUMO_STATE_DOWNLOAD_IN_CANCEL)
								{
									break;
								}
								else if (nFumoStatus == DM_FUMO_STATE_DOWNLOAD_FAILED)
								{
									break;
								}
								else
								{
									System.arraycopy(actualBuff, 0, pHttpObj[appId].pReceiveBuffer, ContentBytesread, actualBuff.length);
								}

								ContentBytesread += actual;
								bytesread += actual;
								actual = -1;
							}
						}
						chunkedHeadRead(in);
						chunkedlen = 0;
						ContentBytesread = 0;
					}

				}
				catch (IOException e)
				{
					tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
					netTimerReceive.endTimer();
					tsdmDB.dmdbSetFUMOStatus(DM_FUMO_STATE_SUSPEND);
					return TP_RET_RECEIVE_FAIL;
				}

				tsLib.debugPrint(DEBUG_NET, "CHUNKED nHttpBodyLength = " + bytesread);
				if (nFumoStatus == DM_FUMO_STATE_DOWNLOAD_IN_PROGRESS)
				{
					tsdmDB.dmdbSetWriteObjectSizeFUMO(bytesread);
				}
				else
				{
					nHttpBodyLength = bytesread;
					pData.reset();
					if (nHttpBodyLength > 0)
						pData.write(pHttpObj[appId].pReceiveBuffer, 0, nHttpBodyLength);
					else
						pData = null;
				}
			}
		}
		else
		{
			if (nFumoStatus != DM_FUMO_STATE_DOWNLOAD_COMPLETE && nFumoStatus != DM_FUMO_STATE_DOWNLOAD_FAILED
					&& nFumoStatus != DM_FUMO_STATE_DOWNLOAD_IN_CANCEL)
			{
				if (nHttpBodyLength == 0)
				{
					netTimerReceive.endTimer();
					return TP_RET_RECEIVE_FAIL;
				}
			}

			try
			{
				int old_actual = 0;
				while ((ContentBytesread != nHttpBodyLength) && (actual != -1))
				{
					if(tsService.DownloadStop) {
						tsService.DownloadStop = false;
						break;
					}
					if (nFumoStatus == DM_FUMO_STATE_DOWNLOAD_IN_PROGRESS)
					{
						pHttpObj[appId].pReceiveBuffer = new byte[RECEIVE_BUFFER_SIZE];
						actual = in.read(pHttpObj[appId].pReceiveBuffer);
						if(old_actual != actual) { //debug reduce
							tsLib.debugPrint(DEBUG_NET, "ContentBytesread:" + ContentBytesread + " , nHttpBodyLength :" + nHttpBodyLength + " , actual :" + actual);
							old_actual=actual;
						}
						tsService.dowloadFileSize(ContentBytesread);
						actualBuff = new byte[actual];
						aBuff = new ByteArrayInputStream(pHttpObj[appId].pReceiveBuffer, 0, actual);
						try
						{
							int ret = aBuff.read(actualBuff);
							if(ret == -1)
							{
								tsLib.debugPrint(DEBUG_EXCEPTION, "Buff read fail");
							}
						}
						catch (IOException e)
						{
							tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
						}
						finally
						{
							try
							{
								if (aBuff != null)
									aBuff.close();
							}
							catch (IOException e)
							{
								tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
							}
						}
					}
					else
					{
						if (nHttpBodyLength > 0)
						{
							if (ContentBytesread == 0)
							{
								pHttpObj[appId].pReceiveBuffer = new byte[nHttpBodyLength];
							}
							actual = in.read(pHttpObj[appId].pReceiveBuffer, ContentBytesread, nHttpBodyLength - ContentBytesread);
						}
					}

					if (nFumoStatus == DM_FUMO_STATE_DOWNLOAD_IN_PROGRESS)
					{
						nRet = dlhandler.dlWriteFirmwareObject(actual, actualBuff);
						netTimerReceive.endTimer();
						new netTimerReceive(appId);

						if (nRet == TS_ERR_NO_MEM_READY)
							return TP_RET_FILE_ERROR;
						else if (nRet != SDL_RET_OK)
							return TP_RET_RECEIVE_FAIL;
					}
					else if (nFumoStatus == DM_FUMO_STATE_DOWNLOAD_IN_CANCEL)
					{
						break;
					}
					else if (nFumoStatus == DM_FUMO_STATE_DOWNLOAD_FAILED)
					{
						break;
					}

					if (actualBuff != null)
						actualBuff = null;

					bytesread += actual;
					ContentBytesread += actual;

				}
			}
			catch (IOException e)
			{
				tsLib.debugPrintException(DEBUG_NET, e.toString());
				netTimerReceive.endTimer();
				tsdmDB.dmdbSetFUMOStatus(DM_FUMO_STATE_SUSPEND);
				return TP_RET_RECEIVE_FAIL;
			}

			if (nFumoStatus == DM_FUMO_STATE_DOWNLOAD_IN_PROGRESS)
			{
				tsdmDB.dmdbSetWriteObjectSizeFUMO(bytesread);
			}
			else
			{
				pData.reset();
				if (nHttpBodyLength > 0)
					pData.write(pHttpObj[appId].pReceiveBuffer, 0, nHttpBodyLength);
				else
					pData = null;
			}
		}

		if (_DM_TP_LOG_ON_)
		{
			if (appId == SYNCMLDM)
			{
				httpLibDump(pHttpObj[appId].pReceiveBuffer, 0, pHttpObj[appId].pReceiveBuffer.length);
				m_bufDebug.reset();
				m_bufDebug.write(pHttpObj[appId].pReceiveBuffer, 0, pHttpObj[appId].pReceiveBuffer.length);
				httpWriteFile(String.format("data/data/com.tsdm/httpdata" + String.valueOf(nHttpDebugCount) + ".wbxml"), m_bufDebug.toByteArray());
				nHttpDebugCount++;
			}
		}

		netTimerReceive.endTimer();

		if (pHttpObj[appId].pHttpConnection != null && pHttpObj[appId].pHttpConnection.equals(HTTP_CONNECTION_TYPE_CLOSE))
		{
			tsLib.debugPrint(DEBUG_NET, "HTTP_CONNECTION_TYPE_CLOSE");
			pHttpObj[appId].nHttpConnection = TP_HTTP_CONNECTION_CLOSE;
			tpClose(appId);
		}
		else if (pHttpObj[appId].pHttpConnection != null && pHttpObj[appId].pHttpConnection.equals(HTTP_CONNECTION_TYPE_KEEPALIVE))
		{
			pHttpObj[appId].nHttpConnection = TP_HTTP_CONNECTION_KEEP_ALIVE;
		}
		else
		{
			tsLib.debugPrint(DEBUG_NET, "HTTP_CONNECTION_TYPE_NONE");
			pHttpObj[appId].nHttpConnection = TP_HTTP_CONNECTION_KEEP_ALIVE;
			pHttpObj[appId].pHttpConnection = HTTP_CONNECTION_TYPE_KEEPALIVE;
		}
		return TP_RET_OK;
	}

	public static void tpApnClose()
	{
		tsLib.debugPrint(DEBUG_NET, "");
		tpApnDisable();
		tpendConnectivity();
	}

	public void tpClose(int appId)
	{
		tsLib.debugPrint(DEBUG_NET, "appId "+appId);
/*
		if (wakeLock != null)
		{
			wakeLock.release();
			wakeLock = null;
		}
		if (wifiLock != null) 
		{ 
			wifiLock.release(); 
			wifiLock = null; 
		}
*/

		if (pHttpObj == null || pHttpObj[appId] == null)
			return;

		try
		{
			if (mInput != null)
				mInput.close();
			if (mOutput != null)
				mOutput.close();

			if (mSocket != null)
				mSocket.close();

			if (mSSLSocket != null)
				mSSLSocket.close();

			if (mSSLFactory != null)
				mSSLFactory = null;

			if (mSSLContext != null)
				mSSLContext = null;

		}
		catch (IOException e)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
		}
		catch (RuntimeException e)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
		}

		if (trustManagers != null)
			trustManagers = null;

		setIsConnected(false);
	}

	public static void shutdownInput()
	{
		tsLib.debugPrint(DEBUG_NET, "");
		try
		{
			mSocket.shutdownInput();
		}
		catch (IOException e)
		{
			tsLib.debugPrint(DEBUG_NET, e.toString());
		}
	}

	public static void shutdownOutput()
	{
		tsLib.debugPrint(DEBUG_NET, "");
		try
		{
			mSocket.shutdownOutput();
		}
		catch (IOException e)
		{
			tsLib.debugPrint(DEBUG_NET, e.toString());
		}
	}

	private int httpInit(int appId)
	{
		int ret = TP_RET_OK;

		pHttpObj[appId].appId = appId;
		pHttpObj[appId].protocol = TP_TYPE_NONE;
		pHttpObj[appId].appSockfd = -1;
		pHttpObj[appId].httpSocketStatus = TP_HTTP_STATUS_INITD;
		pHttpObj[appId].networkstatus = 1;
		pHttpObj[appId].bHttpBufferAlloc = false;
		pHttpObj[appId].bHttpHeaderParser = false;
		pHttpObj[appId].nContentLength = -1;
		pHttpObj[appId].nHeaderLength = -1;
		pHttpObj[appId].hProtocol = -1;
		pHttpObj[appId].httpObjUseable = true;
		pHttpObj[appId].nHttpReturnStatusValue = 0;
		pHttpObj[appId].dnsQueryRetryCount = 0;

		pHttpObj[appId].nTimerID = 0;
		pHttpObj[appId].nTimerWaitCounter = 0;
		pHttpObj[appId].nTimerUsed = false;
		pHttpObj[appId].pHmacData = null;
		pHttpObj[appId].cServerType = 2;
		pHttpObj[appId].nComingRemoteClose = false;

		pHttpObj[appId].nTunnelMode = TP_MODE_SSL_TUNNEL_NONE;
		pHttpObj[appId].nTunnelConnected = TP_SSL_TUNNEL_NONE;

		pHttpObj[appId].nHttpOpenMode = HTTP_METHOD_POST;

		pHttpObj[appId].pHttpVersion = "HTTP/1.1";
		pHttpObj[appId].pHttpHost = null;
		pHttpObj[appId].pRequestUri = null;
		pHttpObj[appId].pContentRange = null;
		pHttpObj[appId].nTransferCoding = -1;

		pHttpObj[appId].protocol = tsDB.dbGetConnectType(appId);

		if (appId == SYNCMLDM)
		{
			pHttpObj[appId].nHttpConnection = TP_HTTP_CONNECTION_KEEP_ALIVE;
			pHttpObj[appId].pHttpConnection = HTTP_CONNECTION_TYPE_KEEPALIVE;
			pHttpObj[appId].pHttpMimeType = HTTP_MIME_DM_WBXML_TYPES;
			pHttpObj[appId].pHttpAccept = HTTP_HEADER_DM_ACCEPT;
		}
		else if (appId == SYNCMLDL)
		{
			pHttpObj[appId].nHttpConnection = TP_HTTP_CONNECTION_KEEP_ALIVE;
			pHttpObj[appId].pHttpConnection = HTTP_CONNECTION_TYPE_KEEPALIVE;
			pHttpObj[appId].pHttpMimeType = HTTP_HEADER_DL_CONTENT_TYPE;
			pHttpObj[appId].pHttpAccept = HTTP_HEADER_DL_ACCEPT;
		}
		pHttpObj[appId].pHttpUserAgent = dmDevinfoAdapter.devAdpGetHttpUserAgent();

		ret = getHttpInfo(appId);
		if (pHttpObj[appId].protocol == TP_TYPE_HTTPS && getIsProxy())
		{
			tsLib.debugPrint(DEBUG_NET, "TP_MODE_SSL_TUNNEL_ACTIVE");
			pHttpObj[appId].nTunnelMode = TP_MODE_SSL_TUNNEL_ACTIVE;
		}
		else if (pHttpObj[appId].protocol == TP_TYPE_HTTPS && !getIsProxy())
		{
			tsLib.debugPrint(DEBUG_NET, "TP_MODE_SSL_TUNNEL_DEACTIVE");
			pHttpObj[appId].nTunnelMode = TP_MODE_SSL_TUNNEL_DEACTIVE;
		}

		return ret;
	}

	private int getHttpInfo(int appId)
	{
		int ret = TP_RET_OK;

		if (pHttpObj == null)
			return TP_RET_INIT_FAIL;

		pHttpObj[appId].appId = appId;

		String ServerURL = "";
		String ProxyAddress = "";

		ServerURL = tsdmDB.dmdbGetServerUrl(pHttpObj[appId].appId);
		tsLib.debugPrint(DEBUG_NET, "ServerURL =>" + ServerURL);
		if (tsLib.isEmpty(ServerURL))
		{
			return TP_RET_INIT_FAIL;
		}

		// get proxy from db
		tsdmInfoConRef Conref = new tsdmInfoConRef();
		Conref = tsdmDB.dmdbGetConRef(Conref);
		// defect_110921
		if (Conref == null)
		{
			tsLib.debugPrintException(DEBUG_NET, "Get Conref from DB is failed");
			return TP_RET_INIT_FAIL;
		}
		
		if (tsLib.isEmpty(Conref.PX.Addr) || Conref.PX.Addr.contains("0.0.0.0"))
		{
			Conref.bProxyUse = false;
			setIsProxy(false);
		}
		else
		{
			tsLib.debugPrint(DEBUG_NET, "Proxy Mode");
			Conref.bProxyUse = true;
			setIsProxy(true);

			ProxyAddress = "http://";
			ProxyAddress = ProxyAddress.concat(Conref.PX.Addr);
			ProxyAddress = ProxyAddress.concat(":");
			ProxyAddress = ProxyAddress.concat(String.valueOf(Conref.PX.nPortNbr));
			conProxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(Conref.PX.Addr, Conref.PX.nPortNbr));
			tsLib.debugPrint(DEBUG_NET, "PX addr :" + Conref.PX.Addr + ", and Port : " + Conref.PX.nPortNbr);
		}

		tsDBURLParser parser = tsDB.dbURLParser(ServerURL);
		pHttpObj[appId].pServerURL = parser.pURL;
		pHttpObj[appId].pServerAddr = parser.pAddress;
		pHttpObj[appId].nServerPort = parser.nPort;
		pHttpObj[appId].isServerInfo = true;
		pHttpObj[appId].protocol = netHttpUtil.httpGetConnectType(ServerURL);

		if (getIsProxy())
		{
			pHttpObj[appId].pProxyAddr = Conref.PX.Addr;
			pHttpObj[appId].pProxyIP = Conref.PX.Addr;
			pHttpObj[appId].proxyPort = Conref.PX.nPortNbr;
		}
		return ret;
	}
	
	public int tpSetHttpObj(String pRequest, String pHmacData, String pContentRange, String nHttpOpenMode, int appId, boolean nDownloadMode)
	{
		int ret = TP_RET_OK;
		tsDBURLParser parser = new tsDBURLParser();

		if (pHttpObj == null)
			return TP_RET_INIT_FAIL;

		if (pRequest != null)
		{
			if (appId == SYNCMLDM)
			{
				int nSrcType = 0, nDstType = 0;
				int nPortOrg = 0;
				String AddressOrg;

				tsLib.debugPrint(DEBUG_NET, "respUri = " + pRequest);
				parser = tsDB.dbURLParser(pRequest);
				nSrcType = tsDB.dbGetConnectType(appId);
				nDstType = netHttpUtil.exchangeProtocolType(parser.pProtocol);
				AddressOrg = tsdmDB.getServerAddress(appId);
				nPortOrg = tsdmDB.dmdbGetServerPort(appId);

				// Defects
				if (AddressOrg == null)
				{
					tsLib.debugPrint(DEBUG_NET, "AddressOrg is null");
					return TP_RET_INIT_FAIL;
				}

				if ((nSrcType != nDstType) || (AddressOrg.compareTo(parser.pAddress) != 0) || (nPortOrg != parser.nPort))
				{
					String strURL = "";
					int firstQuestion = pRequest.indexOf('?');
					if (firstQuestion > 0)
						strURL = pRequest.substring(0, firstQuestion);
					else
						strURL = pRequest;
					if (strURL.length() > 0)
					{
						tsdmDB.dmdbSetServerUrl(strURL);
					}

					tsdmDB.dmdbSetServerAddress(parser.pAddress);
					tsdmDB.dmdbSetServerPort(parser.nPort);
					tsdmDB.dmdbSetServerProtocol(parser.pProtocol);
					return TP_RET_CHANGED_PROFILE;
				}
				
				if(!tsLib.isEmpty(cookie))
				{
					pHttpObj[appId].pHttpcookie = cookie;
				}
			}

			if (getIsProxy())
				pHttpObj[appId].pRequestUri = pRequest;
			else
				pHttpObj[appId].pRequestUri = netHttpUtil.tpParsePath(pRequest);

			tsLib.debugPrint(DEBUG_NET, "requestURI = " + pHttpObj[appId].pRequestUri);
		}

		if (pHmacData != null)
		{
			if (pHttpObj[appId].pHmacData == null)
			{
				pHttpObj[appId].pHmacData = new byte[pHmacData.length()];
			}
			pHttpObj[appId].pHmacData = pHmacData.getBytes();
		}
		else
			pHttpObj[appId].pHmacData = null;

		if (nHttpOpenMode != null)
			pHttpObj[appId].nHttpOpenMode = nHttpOpenMode;
		else
			pHttpObj[appId].nHttpOpenMode = null;

		if (!tsLib.isEmpty(pContentRange))
			pHttpObj[appId].pContentRange = pContentRange;
		else
			pHttpObj[appId].pContentRange = null;

		if (nDownloadMode)
			pHttpObj[appId].nDownloadMode = nDownloadMode;
		else
			pHttpObj[appId].nDownloadMode = false;

		if (appId == SYNCMLDL)
		{
			pHttpObj[appId].pHttpMimeType = null;
			pHttpObj[appId].pHttpAccept = null;

			pHttpObj[appId].pHttpMimeType = tsdmDB.dmdbGetMimeType();
			pHttpObj[appId].pHttpAccept = tsdmDB.dmdbGetAcceptType();

			if (pHttpObj[appId].pHttpMimeType == null)
				pHttpObj[appId].pHttpMimeType = HTTP_HEADER_DL_CONTENT_TYPE;

			if (pHttpObj[appId].pHttpAccept == null)
				pHttpObj[appId].pHttpAccept = HTTP_HEADER_DL_ACCEPT;
		}
		return ret;
	}

	private String httpHeadRead(InputStream in)
	{
		StringBuffer data = new StringBuffer("");
		int c = 0;

		try
		{
			in.mark(1);
			if (in.read() == -1)
				return null;
			else
				in.reset();

			while ((c = in.read()) >= 0)
			{
				if (c == 0x00 || c == 0x0a || c == 0x0d)
					break;
				else
					data.append((char) c);
			}

			if (c == 0x0d)
			{
				in.mark(1);
				if (in.read() != 0x0a)
					in.reset();
			}
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
			return null;
		}

		return data.toString();
	}

	private int httpHeaderParser(int appId, InputStream in)
	{
		tsLib.debugPrint(DEBUG_NET, "");

		StringBuffer header = new StringBuffer("");
		String data = "";
		int pos = -1;
		String hmac = "";
		String encoding = "";
		String contentrange = "";
		int nContentLen = 0;
		String nHost = null, nConnection = null;

		data = httpHeadRead(in);
		if (tsLib.isEmpty(data))
		{
			// Temp: retry one more (Why!!!)
			data = httpHeadRead(in);
			if (tsLib.isEmpty(data))
			{
				tsLib.debugPrint(DEBUG_NET, "data is null ");
				return TP_RET_RECEIVE_FAIL;
			}
		}
		header.append(data + "\r\n");
		pos = data.indexOf(' ');
		if ((data.toLowerCase().startsWith("http")) && (pos >= 0) && (data.indexOf(" ", pos + 1) >= 0))
		{
			String rcString = data.substring(pos + 1, data.indexOf(" ", pos + 1));
			try
			{
				pHttpObj[appId].nHttpReturnStatusValue = Integer.parseInt(rcString);
			}
			catch (Exception e)
			{
				tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
				return TP_RET_RECEIVE_FAIL;
			}
		}

		tsLib.debugPrint(DEBUG_NET, "pHttpObj[appId].nHttpReturnStatusValue=" + String.valueOf(pHttpObj[appId].nHttpReturnStatusValue));

		// get the rest of the header info
		while ((data = httpHeadRead(in)) != null)
		{
			// the header ends at the first blank line
			if (data.length() == 0)
				break;

			// Defects : Avoid String + operator in loops
			data = data.concat("\r\n");
			header.append(data);
			// header.append(data + "\r\n");

			// check for the Host header
			pos = data.toLowerCase().indexOf(HTTP_HOST_STRING);
			if (pos >= 0)
				nHost = data.substring(pos + 5).trim();

			// check for the Content-Length header
			pos = data.toLowerCase().indexOf(HTTP_CONTENT_LEN_STRING);
			if (pos >= 0)
				nContentLen = Integer.parseInt(data.substring(pos + 15).trim());

			pos = data.toLowerCase().indexOf(HTTP_CONNECTION_STRING);
			if (pos >= 0)
				nConnection = data.substring(pos + 11).trim();

			pos = data.toLowerCase().indexOf(HTTP_X_SYNCML_HMAC_STRING);
			if (pos >= 0)
				hmac = data.substring(pos + 14).trim();

			pos = data.toLowerCase().indexOf(HTTP_TRANSFER_ENCODING_STRING);
			if (pos >= 0)
				encoding = data.substring(pos + 18).trim();

			pos = data.toLowerCase().indexOf(HTTP_CONTENT_RANGE_STRING);
			if (pos >= 0)
				contentrange = data.substring(pos + 14).trim();

			pos = data.toLowerCase().indexOf(HTTP_COOKIE_STRING);
			if (pos >= 0)
			{
				if((data.toLowerCase()).contains(HTTP_COOKIE_JSESSIONID_STRING) || (data.toLowerCase()).contains(HTTP_COOKIE_AWSALB_STRING))
				{
					if(!tsLib.isEmpty(cookie)) {
						cookie=cookie+";";
					    cookie=cookie+data.substring(pos + 11).trim();
					}
					else {
						cookie=data.substring(pos + 11).trim();
					}
				}
			}
		}

		pHttpObj[appId].nHeaderLength = header.length();
		if (header.length() < 1024)
			tsLib.debugPrint(DEBUG_NET, "\r\n [_____Receive Header_____]\r\n" + header.toString());

		pHttpObj[appId].pHttpHost = nHost;
		pHttpObj[appId].nContentLength = nContentLen;
		pHttpObj[appId].pHttpConnection = nConnection;

		tsLib.debugPrint(DEBUG_NET, "chunked = " + encoding);
		tsLib.debugPrint(DEBUG_NET, "pHttpObj[appId].nHeaderLength =" + String.valueOf(pHttpObj[appId].nHeaderLength));
		tsLib.debugPrint(DEBUG_NET, "pHttpObj[appId].nContentLength =" + String.valueOf(pHttpObj[appId].nContentLength));

		if (!tsLib.isEmpty(encoding))
		{
			if (encoding.equals("chunked"))
				pHttpObj[appId].nTransferCoding = HTTP_CHUNKED;
			else
				pHttpObj[appId].nTransferCoding = HTTP_NOT_CHUNKED;
		}
		else
			pHttpObj[appId].nTransferCoding = HTTP_NOT_CHUNKED;

		if (!tsLib.isEmpty(hmac))
		{
			HMacData = httpPsrParserHMAC(hmac, pHttpObj[appId].nContentLength);

		}
		else
		{
			HMacData = null;
			tsLib.debugPrint(DEBUG_NET, "szHMAC null");
		}

		if (!tsLib.isEmpty(contentrange))
		{
			pHttpObj[appId].pContentRange = contentrange;
			tsLib.debugPrint(DEBUG_NET, "pContentRange" + pHttpObj[appId].pContentRange);
		}
		else
		{
			pHttpObj[appId].pContentRange = null;
		}
		
		if(!tsLib.isEmpty(cookie))
		{
			pHttpObj[appId].pHttpcookie = cookie; 
		}
		
		return TP_RET_OK;

	}

	public void netAdpEventSend(int appId)
	{
		tsDmParamConnectfailmsg pFailParam = null;
		switch (pHttpObj[appId].eCode)
		{
			case TP_ECODE_NETWORK_ATTACH_FAIL:
				pFailParam = tsDmMsg.createConnectFailMessage(appId, TP_ECODE_NETWORK_ATTACH_FAIL);
				break;
			case TP_ECODE_NETWORK_ACCOUNT_FAIL:
				pFailParam = tsDmMsg.createConnectFailMessage(appId, TP_ECODE_NETWORK_ACCOUNT_FAIL);
				break;
			case TP_ECODE_PROTO_MAX_CONNENTIONS:
				pFailParam = tsDmMsg.createConnectFailMessage(appId, TP_ECODE_PROTO_MAX_CONNENTIONS);
				break;
			case TP_ECODE_PROTO_FLIGHT_MODE:
				pFailParam = tsDmMsg.createConnectFailMessage(appId, TP_ECODE_PROTO_FLIGHT_MODE);
				break;
			case TP_ECODE_PARAM_NULL:
			case TP_ECODE_NETWORK_CONNECT_RETRY:
			case TP_ECODE_NETWORK_STOPPED:
				pFailParam = null;
			default:
				break;
		}
		tsDmMsg.taskSendMessage(TASK_MSG_DM_SYNCML_CONNECTFAIL, pFailParam, null);
	}

	public boolean getIsProxy()
	{
		return isProxyServer;
	}

	public void setIsProxy(boolean isProxy)
	{
		isProxyServer = isProxy;
	}

	public static boolean getIsConnected()
	{
		tsLib.debugPrint(DEBUG_NET, "connect status is " + String.valueOf(isConnected));
		return isConnected;
	}

	public static void setIsConnected(boolean isCon)
	{
		isConnected = isCon;
	}

	public static void httpCookieClear()
	{
		cookie = "";
		tsLib.debugPrint(DEBUG_NET, "!!");
	}
	
	public static int tpStart(int appId)
	{
		int rc = TP_RET_OK;

		tsLib.debugPrint(DEBUG_NET, "nTunnelMode[" + pHttpObj[appId].nTunnelMode + "] HttpSocketStatus[" + pHttpObj[appId].httpSocketStatus
				+ "] nTunnelConnected[" + pHttpObj[appId].nTunnelConnected + "]");

		if (pHttpObj[appId].nTunnelMode == TP_MODE_SSL_TUNNEL_ACTIVE)
		{
			return TP_RET_SSL_TUNNEL_MODE;
		}

		return rc;
	}

	public int tpAbort(int appId)
	{
		int rc = TP_RET_OK;
		return rc;
	}

	public static void netAdpSetReuse(boolean nFlag)
	{
		nNetworkReuse = nFlag;
	}

	public void tpCloseNetWork(int appId)
	{
		if (_DM_TP_LOG_ON_)
		{
			if (appId == SYNCMLDM)
			{
				try
				{
					if (m_bufDebug != null)
					{
						m_bufDebug.close();
					}
					nHttpDebugCount = 0;
				}
				catch (IOException e)
				{
					tsLib.debugPrint(DEBUG_NET, " error");
				}
				catch (Exception e)
				{
					tsLib.debugPrint(DEBUG_NET, " error " + e.toString());
				}
			}
		}

		pHttpObj[appId] = null;
	}

	public int tpGetHttpEcode(int appId)
	{
		return pHttpObj[appId].eCode;
	}

	public String tpGetHttpOpenMode(int appId)
	{
		return pHttpObj[appId].nHttpOpenMode;
	}


	public tsDmHmacData httpPsrParserHMAC(String pszValue, int ConLen)
	{
		tsDmHmacData MacData = new tsDmHmacData();
		if (pszValue == null)
		{
			return null;
		}

		int nStartAlgorithm = pszValue.indexOf("algorithm=");
		String szData = pszValue.substring(nStartAlgorithm);
		int token = szData.indexOf(',');
		MacData.hmacAlgorithm = szData.substring("algorithm=".length(), token - 1);
		tsLib.debugPrint(DEBUG_NET, "algorithm:" + MacData.hmacAlgorithm);

		int nStartUserName = pszValue.indexOf("username=\"");
		szData = pszValue.substring(nStartUserName + "username=\"".length());
		token = szData.indexOf("\"");
		MacData.hmacUserName = szData.substring(0, token);
		tsLib.debugPrint(DEBUG_NET, "username:" + MacData.hmacUserName);

		int nStartmac = pszValue.indexOf("mac=");
		szData = pszValue.substring(nStartmac);
		MacData.hamcDigest = szData.substring("mac=".length());
		tsLib.debugPrint(DEBUG_NET, "nStartmac:" + MacData.hamcDigest);

		MacData.httpContentLength = ConLen;
		return MacData;
	}

	public tsDmHmacData getCurHMACData()
	{
		return HMacData;
	}

	public int setCurHMACData(tsDmHmacData MacData)
	{
		int nRet = TP_RET_OK;

		if (HMacData == null)
			HMacData = new tsDmHmacData();

		HMacData = MacData;

		return nRet;
	}

	private String chunkedHeadRead(InputStream in)
	{
		StringBuffer data = new StringBuffer("");
		int c = 0;

		try
		{
			in.mark(1);
			if (in.read() == -1)
				return null;
			else
				in.reset();

			while ((c = in.read()) >= 0)
			{
				if (c == 0x0d)// if (c == 0x00 || c == 0x0a || c == 0x0d)
				{
					in.mark(1);
					if ((c = in.read()) == 0x0a)
						break;
					else
						in.reset();
				}
				else
					data.append((char) c);
			}

		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
			return null;
		}

		return data.toString();
	}

	public int httpPsrChunkSizeParsing(InputStream in)
	{
		StringBuffer data = new StringBuffer("");
		int c = 0;
		int p_num = 1;
		int ChunkedLen = 0;
		char ch = 0;
		char small_A = 'a';
		char large_A = 'A';

		try
		{
			in.mark(1);
			if (in.read() == -1)
				return 0;
			else
				in.reset();

			while ((c = in.read()) >= 0)
			{
				if (c == 0x0d)// if (c == 0x00 || c == 0x0a || c == 0x0d)
				{
					in.mark(1);
					if ((c = in.read()) == 0x0a)
						break;
					else
						in.reset();
				}
				else
					data.append((char) c);
			}

		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
			return 0;
		}
		String nSize = data.toString();

		char[] size = nSize.toCharArray();
		for (int j = size.length - 1; j >= 0; j--)
		{
			if (!tsLib.libisalnum(size[j]))
			{
				return 0;
			}
			if (tsLib.libisnum(size[j]))
			{
				ChunkedLen += p_num * (size[j] - '0');
			}
			else
			{
				ch = (char) ((size[j] < small_A) ? small_A + size[j] - large_A : size[j]);
				ChunkedLen += p_num * (ch - small_A + 10);
			}

			p_num *= 16;
		}

		return ChunkedLen;
	}

	public int httpPsrGetContentLengthByRange(String pContentRange)
	{
		int contentlength = 0;
		String rangeStr = null, range = null;
		int pos = 0, len = 0, index = 0;

		pos = pContentRange.toLowerCase().indexOf("bytes ");
		if (pos < 0)
			return 0;

		rangeStr = pContentRange.substring(pos + 6).trim();
		if (rangeStr.hashCode() == 0)
			return 0;

		len = rangeStr.length();
		if (len == 0)
			return 0;
		else
		{
			for (int i = 0; i < len; i++)
			{
				if (rangeStr.charAt(i) == '/')
				{
					index = i;
					break;
				}
			}
			if (index == 0)
			{
				range = rangeStr;
			}
			else
			{
				range = rangeStr.substring(0, index);
			}
			String tmp[] = range.split("-");
			if (tmp[0] != null && tmp[1] != null)
			{
				contentlength = Integer.valueOf(tmp[1]) - Integer.valueOf(tmp[0]);
				if (contentlength <= 0)
				{
					return 0;
				}
				contentlength += 1;
			}
			else
				return 0;
		}
		return contentlength;
	}

	public static void httpLibDump(byte[] szBuf, int pos, int len)
	{
		int i = 0;
		StringBuilder szAsc = new StringBuilder();
		StringBuilder szDump = new StringBuilder();

		for (i = 0; i < len; i++)
		{
			szDump.append((char) tsLib.libHexToChar((szBuf[i + pos] >> 4) & 0xf));
			szDump.append((char) tsLib.libHexToChar(szBuf[i + pos] & 0xf));
			szDump.append(' ');

			if (szBuf[i + pos] >= ' ' && szBuf[i + pos] <= 126)
			{
				szAsc.append((char) szBuf[i + pos]);
			}
			else
			{
				szAsc.append('.');
			}

			if (i % 16 == 15 || i == len - 1)
			{
				if (szAsc.length() > 0)
				{
					szDump.append("   ");
					szDump.append(szAsc);
				}
				szDump.append("\r\n");
				tsLib.debugPrint(DEBUG_NET, szDump.toString());
				szDump.setLength(0);
				szAsc.setLength(0);
			}
		}
	}

	public static void httpWriteFile(String file, byte[] data)
	{
		DataOutputStream fw = null;
		try
		{
			fw = new DataOutputStream(new FileOutputStream(file));
			fw.write(data);
		}
		catch (FileNotFoundException e)
		{
			tsLib.debugPrint(DEBUG_NET, e.toString());
		}
		catch (IOException e)
		{
			tsLib.debugPrint(DEBUG_NET, e.toString());
		}
		finally
		{
			if (fw != null)
				try
				{
					fw.close();
				}
				catch (IOException e)
				{
					tsLib.debugPrint(DEBUG_NET, e.toString());
				}
		}
	}

	public static int tpCheckURL(String objectURL, String respURL)
	{
		int nRet = TP_RET_OK;
		if (objectURL == null || respURL == null)
		{
			tsLib.debugPrintException(DEBUG_NET, "Input Uri is NULL");
			return TP_RET_INVALID_PARAM;
		}

		tsLib.debugPrint(DEBUG_NET, "object URL [" + objectURL + "], response URL [" + respURL + "]");

		tsDBURLParser parser1 = new tsDBURLParser();
		tsDBURLParser parser2 = new tsDBURLParser();

		parser1 = tsDB.dbURLParser(objectURL);
		parser2 = tsDB.dbURLParser(respURL);

		if (!(parser1.pProtocol.equals(parser2.pProtocol)) 
			|| !(parser1.pAddress.equals(parser2.pAddress)) 
			|| (parser1.nPort != parser2.nPort))
		{
			tsLib.debugPrint(DEBUG_NET, "different response url!!");
			nRet = TP_RET_CHANGED_PROFILE;
		}
		else
		{
			nRet = TP_RET_OK;
		}

		return nRet;
	}

	public static int lookupHost(String hostname)
	{
		InetAddress inetAddress;
		try
		{
			inetAddress = InetAddress.getByName(hostname);
		}
		catch (UnknownHostException e)
		{
			return -1;
		}
		byte[] addrBytes;
		int addr;
		addrBytes = inetAddress.getAddress();
		addr = ((addrBytes[3] & 0xff) << 24) | ((addrBytes[2] & 0xff) << 16) | ((addrBytes[1] & 0xff) << 8) | (addrBytes[0] & 0xff);
		return addr;
	}
}
