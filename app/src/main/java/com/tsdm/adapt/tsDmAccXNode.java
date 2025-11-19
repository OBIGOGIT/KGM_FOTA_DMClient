package com.tsdm.adapt;

import com.tsdm.core.data.constants.DmDevInfoConst;
import com.tsdm.db.tsDBAccXNode;
import com.tsdm.db.tsdmDB;
import com.tsdm.agent.dmPreConfigEntity;

public class tsDmAccXNode
{
	public String	Account;
	public String	AppAddr;
	public String	AppAddrPort;
	public String	ClientAppAuth;
	public String	ServerAppAuth;
	public String	ToConRef;

	public tsDmAccXNode()
	{
		if(DmDevInfoConst._SYNCML_TS_DM_REGISTRY_PROFILE_)
		{
			tsDBAccXNode pAccXnodeInfo = new tsDBAccXNode();
			int nIdx = tsdmDB.dmdbGetProfileIndex();
			dmPreConfigEntity.getAccXnodeInfoFromConfigFile(pAccXnodeInfo, nIdx);

			Account = pAccXnodeInfo.Account;
			AppAddr = pAccXnodeInfo.AppAddr;
			AppAddrPort = pAccXnodeInfo.AppAddrPort;
			ClientAppAuth = pAccXnodeInfo.ClientAppAuth;
			ServerAppAuth = pAccXnodeInfo.ServerAppAuth;
			ToConRef = pAccXnodeInfo.ToConRef;
		}
		else
		{
			Account = new String("./DMAcc/SampleAcc");
			AppAddr = new String("./DMAcc/SampleAcc/AppAddr/AppAddrX");
			AppAddrPort = new String("./DMAcc/SampleAcc/AppAddr/AppAddrX/Port/PortX");
			ClientAppAuth = new String("./DMAcc/SampleAcc/AppAuth/ClientSide");
			ServerAppAuth = new String("./DMAcc/SampleAcc/AppAuth/ServerSide");
			ToConRef = new String("./DMAcc/SampleAcc/ToConRef/Connectivity Reference Name");
		}
	}
}
