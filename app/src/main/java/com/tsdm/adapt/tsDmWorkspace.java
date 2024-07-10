package com.tsdm.adapt;

import java.io.ByteArrayOutputStream;

import com.tsdm.agent.dmDefineDevInfo;

public class tsDmWorkspace implements dmDefineDevInfo, tsDefineIdle
{
	public tsDmWorkspace ws;
	public int						appId;
	public int						msgID;
	public int						cmdID;
	public int						maxMsgSize;
	public int						maxObjSize;
	public int						bufsize;
	public int						authState;
	public int						serverAuthState;
	public int						numAction;
	public int						procStep;
	public int						sendPos;
	public int						authCount;
	public int						serverMaxObjSize;
	public int						serverMaxMsgSize;
	public int						prevBufPos;
	public int						dataTotalSize;
	public int						port;
	public int						credType;
	public int						serverCredType;
	public int						sessionAbort;
	public boolean					sendChal;
	public boolean					nextMsg;
	public boolean					endOfMsg;
	public boolean					isFinal;
	public boolean					atomicFlag;
	public boolean					dataBuffered;
	public boolean					sendRemain;

	public SyncmlUICFlag uicFlag;
	public tsList uicData;
	public SyncmlAtomicStep atomicStep;
	public SyncmlState state;
	public tsOmTree om;
	public tsDmEncoder e;
	public tsDmParser p;
	public SyncmlState dmState;
	public SyncmlProcessingState procState;

	public boolean					nTNDSFlag;
	public char						nUpdateMechanism;
	public String					aDownloadURI;

	public int						nScomoDownloadMechanism;
	public String					aScomoDownloadURI;

	public String					userName;
	public String					statusReturnCode;
	public String					serverID;
	public String					serverPW;
	public String					clientPW;
	public byte[]					nextNonce;
	public byte[]					serverNextNonce;
	public String					protocol;
	public String					hostname;
	public String					sourceURI;
	public String					targetURI;
	public String					sessionID;
	public String					msgRef;

	public tsLinkedList targetRefList;
	public tsLinkedList sourceRefList;
	public tsLinkedList list;
	public tsLinkedList statusList;
	public tsLinkedList resultsList;
	public tsLinkedList atomicList;
	public tsLinkedList sequenceList;
	public tsDmUicOption uicOption;
	public tsDmParserResults results;
	public tsDmParserSyncheader syncHeader;
	public tsDmParserAlert uicAlert;
	public tsDmHmacData recvHmacData;
	public tsDmParserResults tempResults;
	public tsDmParserItem tmpItem;
	public tsDmParserAtomic atomic;
	public tsDmParserSequence sequence;

	public ByteArrayOutputStream	buf;

	public boolean					inSequenceCmd;
	public boolean					IsSequenceProcessing;
	public boolean					inAtomicCmd;
	public Object					userData;

	public tsDmWorkspace()
	{
		aDownloadURI = "";
		userName = "";
		statusReturnCode = "";
		serverID = "";
		serverPW = "";
		clientPW = "";
		nextNonce = new byte[128];
		serverNextNonce = new byte[128];
		protocol = "";
		hostname = "";
		sourceURI = "";
		targetURI = "";
		sessionID = "";
		msgRef = "";
		uicData = null;

		authState = AUTH_STATE_NONE;
		serverAuthState = AUTH_STATE_NONE;
		credType = CRED_TYPE_NONE;
		serverCredType = CRED_TYPE_NONE;
		sendChal = false;
		inAtomicCmd = false;
		atomicList = tsLinkedList.listCreateLinkedList();
		inSequenceCmd = false;
		sequenceList = null;

		om = new tsOmTree();
		e = new tsDmEncoder();
		targetRefList = tsLinkedList.listCreateLinkedList();
		sourceRefList = tsLinkedList.listCreateLinkedList();
		list = tsLinkedList.listCreateLinkedList();
		statusList = tsLinkedList.listCreateLinkedList();
		resultsList = tsLinkedList.listCreateLinkedList();
		results = null;

		buf = new ByteArrayOutputStream();

		bufsize = WBXML_DM_ENCODING_BUF_SIZE;
		maxMsgSize = WBXML_DM_MAX_MESSAGE_SIZE;
		maxObjSize = WBXML_DM_MAX_OBJECT_SIZE;

		serverMaxMsgSize = WBXML_DM_MAX_MESSAGE_SIZE;
		serverMaxObjSize = WBXML_DM_MAX_OBJECT_SIZE;

		endOfMsg = false;
		syncHeader = null;
		sessionAbort = 0;
		dmState = SyncmlState.DM_STATE_INIT;
		cmdID = 1;
		appId = SYNCMLDM;
		msgID = 1;
		authCount = 0;

		dataBuffered = false;
		IsSequenceProcessing = false;
		nUpdateMechanism = 0;
		recvHmacData = new tsDmHmacData();

		uicFlag = SyncmlUICFlag.UIC_NONE;
		uicData = null;
	}

	public void wsDmFreeWorkSpace()
	{
		if (targetRefList != null)
		{
			targetRefList = null;
		}
		if (sourceRefList != null)
		{
			sourceRefList = null;
		}
		if (list != null)
		{
			list = null;
		}
		if (statusList != null)
		{
			statusList = null;
		}
		if (resultsList != null)
		{
			resultsList = null;
		}

		if (atomicList != null)
		{
			atomicList = null;
		}

	}

	public void wsDmFreeAgent(Object obj)
	{
	}
}
