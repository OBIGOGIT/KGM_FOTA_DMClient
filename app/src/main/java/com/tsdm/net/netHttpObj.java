package com.tsdm.net;

public class netHttpObj
{
	public int		appId;
	public int		protocol;
	public int		eCode;
	public int 		appSockfd;
	public int 		httpSocketStatus;
	public int 		networkstatus;
	public int		cServerType;
	public int		hProtocol;
	public boolean	bHttpBufferAlloc;
	public boolean	bHttpHeaderParser;

	public int		nContentLength;
	public int		nHeaderLength;

	public int		nHttpReturnStatusValue;
	public boolean	httpObjUseable;
	public int		dnsQueryRetryCount;
	public int		nTimerID;
	public int		nTimerWaitCounter;
	public boolean	nTimerUsed;
	public boolean	nComingRemoteClose;
	public boolean	nDownloadMode;
	public int		nTunnelMode;
	public int		nTunnelConnected;
	public String	pEventHandlerName;

	// Server Information
	public boolean	isServerInfo;
	public String	pServerURL;
	public String	pProxyAddr;
	public String	pServerAddr;
	public String	pServerIP;
	public String	pProxyIP;
	public int		nServerPort;
	public int		proxyPort;
	public int		nInetAddr;
	public int		nPort;

	public byte[]	pReceiveBuffer;
	public int		nRecvDataSize;
	public int		nRecvCurOffset;
	public int		nRecvReadLen;

	public byte[]	pChunkBuffer;
	public int		nChunkDataSize;
	public int		nChunkCurOffset;
	public int		nChunkReadLen;

	public byte[]	pWriteBuffer;
	public int		nWriteDataSize;
	public int		nWriteCurOffset;
	public int		nWriteLen;

	public byte[]	pReceiveHmacData;
	public byte[]	pHmacData;


	public String	nHttpOpenMode;
	public int		nTransferCoding;
	public int		nHttpConnection;

	public String	pHttpConnection;
	public String	pHttpMimeType;
	public String	pHttpVersion;
	public String	pHttpUserAgent;
	public String	pHttpHost;
	public String	pHttpAccept;
	public String	pRequestUri;
	public String	pContentRange;
	public String 	pHttpcookie;
	public boolean	nTransferCodingFlag;

	public String	pResponseContentType;

	int				event;
	String			pNetAccountName;
	int				pHProtoAccount;
	int				Networkstate;
	int				nRetryCount;

	public netHttpObj()
	{
		pEventHandlerName = "";
		pServerURL = "";
		pProxyAddr = "";
		pServerAddr = "";
		pServerIP = "";
		pProxyIP = "";

		nHttpOpenMode = "";
		nHttpConnection = NetConsts.TP_HTTP_CONNECTION_NONE;
		pHttpConnection = "";
		pHttpMimeType = "";
		pHttpVersion = "";
		pHttpUserAgent = "";
		pHttpHost = "";
		pHttpAccept = "";
		pRequestUri = "";
		pContentRange = "";
		pHttpcookie = "";

		pResponseContentType = "";
		pNetAccountName = "";
		nHttpReturnStatusValue = 0;
		nContentLength = 0;
		nTransferCoding = 0;
	}
}