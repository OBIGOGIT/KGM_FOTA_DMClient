package com.tsdm.adapt;

import com.tsdm.agent.dmDefineDevInfo;
import com.tsdm.core.data.constants.DmProtocol;
import com.tsdm.db.tsDefineDB;
import com.tsdm.db.tsDBUICResultKeep;
import com.tsdm.db.tsdmDB;

public class tsDmUic implements dmDefineDevInfo, tsDefineUic, tsDefineDB
{
	public static tsDmUicOption dmUicCreateUicOption()
	{
		tsDmUicOption opt;

		opt = new tsDmUicOption();
		opt.text = new tsDmText();
		opt.defaultResponse = new tsDmText();
		opt.text = tsList.listCreateText(UIC_MAX_TITLE_SIZE, null);
		opt.defaultResponse = tsList.listCreateText(UIC_MAX_USERINPUT_SIZE, 0);

		return opt;
	}

	public static tsDmUicOption dmUicFreeUicOption(tsDmUicOption uicOption)
	{
		tsDmUicOption obj = (tsDmUicOption) uicOption;
		int i = 0;

		if (obj != null)
		{
			obj.text = null;
			obj.defaultResponse = null;

			for (i = 0; i < obj.uicMenuNumbers; i++)
			{
				obj.uicMenuList[i] = null;
			}
			obj.uicMenuTitle = null;
			obj = null;
		}

		return uicOption;
	}

	public static int dmUicGetUicType(String pType)
	{
		tsLib.debugPrint(DEBUG_DM, "pType " + pType);
		int type = UIC_TYPE_NONE;
		if (pType.compareTo(DmProtocol.ALERT_DISPLAY) == 0)
		{
			type = UIC_TYPE_DISP;
		}
		else if (pType.compareTo(DmProtocol.ALERT_CONTINUE_OR_ABORT) == 0)
		{
			type = UIC_TYPE_CONFIRM;
		}
		else if (pType.compareTo(DmProtocol.ALERT_TEXT_INPUT) == 0)
		{
			type = UIC_TYPE_INPUT;
		}
		else if (pType.compareTo(DmProtocol.ALERT_SINGLE_CHOICE) == 0)
		{
			type = UIC_TYPE_SINGLE_CHOICE;
		}
		else if (pType.compareTo(DmProtocol.ALERT_MULTIPLE_CHOICE) == 0)
		{
			type = UIC_TYPE_MULTI_CHOICE;
		}

		return type;
	}

	public static tsDmUicOption dmUicCopyUicOption(Object target, Object source)
	{
		tsDmUicOption pTarget = (tsDmUicOption) target;
		tsDmUicOption pSource = (tsDmUicOption) source;
		int i = 0;
		tsLib.debugPrint(DEBUG_DM, "");
		pTarget.appId = pSource.appId;
		pTarget.inputType = pSource.inputType;
		pTarget.echoType = pSource.echoType;
		pTarget.maxDT = pSource.maxDT;
		pTarget.maxLen = pSource.maxLen;
		pTarget.minDT = pSource.minDT;
		pTarget.progrCurSize = pSource.progrCurSize;
		pTarget.progrMaxSize = pSource.progrMaxSize;
		pTarget.progrType = pSource.progrType;
		pTarget.UICType = pSource.UICType;
		pTarget.text = new tsDmText();
		if (pSource.text != null)
		{
			pTarget.text.len = pSource.text.len;
			pTarget.text.size = pSource.text.size;
			if (pSource.text.text != null)
			{
				pTarget.text.text = pSource.text.text;
			}
		}
		else
		{
			pTarget.text.len = 0;
			pTarget.text.size = 0;

		}
		pTarget.defaultResponse = new tsDmText();
		if (pSource.defaultResponse != null)
		{
			pTarget.defaultResponse.len = pSource.defaultResponse.len;
			pTarget.defaultResponse.size = pSource.defaultResponse.size;
			if (pSource.defaultResponse.text != null)
			{
				pTarget.defaultResponse.text = pSource.defaultResponse.text;
			}
		}
		else
		{
			pTarget.defaultResponse.len = 0;
			pTarget.defaultResponse.size = 0;
		}

		if (pSource.UICType == UIC_TYPE_SINGLE_CHOICE || pSource.UICType == UIC_TYPE_MULTI_CHOICE)
		{
			if (pSource.uicMenuNumbers == 0)
			{
				tsLib.debugPrint(DEBUG_DM, " uicMenuNumbers = 0 !!!");
			}
		}
		pTarget.uicMenuNumbers = pSource.uicMenuNumbers;
		for (i = 0; i < pSource.uicMenuNumbers; i++)
		{
			if (pSource.uicMenuList[i] != null)
			{
				if (i < UIC_MAX_CHOICE_MENU && pSource.uicMenuList[i].length() > 0)
				{
					pTarget.uicMenuList[i] = pSource.uicMenuList[i];
				}
			}
		}

		if (pSource.uicMenuTitle != null)
		{
			pTarget.uicMenuTitle = pSource.uicMenuTitle;
		}
		return (tsDmUicOption) target;
	}

	public static tsDmUicResult createUicResult()
	{
		tsDmUicResult res;
		tsLib.debugPrint(DEBUG_DM, "");
		res = new tsDmUicResult();
		res.text = new tsDmText();
		res.text = tsList.listCreateText(UIC_MAX_USERINPUT_SIZE, null);

		return res;
	}

	public static Object dmUicFreeUicResult(tsDmUicResult uicResult)
	{
		tsDmUicResult obj = (tsDmUicResult) uicResult;
		tsLib.debugPrint(DEBUG_DM, "");
		obj.text.text = null;
		obj.text = null;

		obj = null;
		return null;
	}

	public static tsDmUicResult dmSetUicResultKeep(tsDmUicResult pData, int pUicResultKeepFlag)
	{
		tsDmUicResult ptUicResult = (tsDmUicResult) pData;
		tsDBUICResultKeep ptUicResultKeep = null;

		tsLib.debugPrint(DEBUG_DM, " pUicResultKeepFlag [" + pUicResultKeepFlag + "]");
		if (ptUicResult == null)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, "ptUicResult Pointer is NULL!");
			return ptUicResult;
		}

		ptUicResultKeep = new tsDBUICResultKeep();

		ptUicResultKeep.eStatus = pUicResultKeepFlag;
		ptUicResultKeep.appId = ptUicResult.appId;
		ptUicResultKeep.UICType = ptUicResult.UICType;
		ptUicResultKeep.result = ptUicResult.result;
		ptUicResultKeep.number = ptUicResult.MenuNumbers;

		if (ptUicResult.text != null)
		{
			ptUicResultKeep.nLen = ptUicResult.text.len;
			ptUicResultKeep.nSize = ptUicResult.text.size;
			if (ptUicResult.text.text != null)
			{
				ptUicResultKeep.szText = ptUicResult.text.text;
			}
		}

		try
		{
			tsdmDB.dmdbWrite(E2P_SYNCML_DM_UIC_RESULT_KEEP, ptUicResultKeep);
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_DB, e.toString());
		}
		ptUicResultKeep = null;

		return ptUicResult;
	}

	public static String dmUicOptionProcess(String pUicOptions, tsDmUicOption uicOption)
	{
		int i = 0;
		char[] pOption = pUicOptions.toCharArray();
		int chTmp = 0;
		int ipOption = 0;

		tsLib.debugPrint(DEBUG_DM, "pUicOptions :" + pUicOptions);
		tsLib.debugPrint(DEBUG_DM, "uicOption :" + uicOption.toString());

		if (pOption[ipOption] == 0)
			return String.valueOf(pOption);

		while (pOption[ipOption] == ' ' || pOption[ipOption] == '\t')
			ipOption++;

		while ((ipOption + i < pOption.length) && (pOption[ipOption + i] != '='))
			i++;

		char[] sOption = new char[pOption.length - ipOption];
		String.valueOf(pOption).getChars(ipOption, pOption.length - ipOption, sOption, 0);

		if ((String.valueOf(sOption).contains("MINDT")) || (String.valueOf(sOption).contains("MDT")))
		{
			ipOption = ipOption + i + 1;
			i = 0;
			while (ipOption + i < pOption.length && pOption[ipOption + i] != '&' && pOption[ipOption + i] != 0)
				i++;

			if (ipOption + i < pOption.length)
			{
				chTmp = pOption[ipOption + i];
				pOption[ipOption + i] = 0;
			}
			else
				chTmp = 0;

			String temp = String.valueOf(pOption).substring(ipOption, ipOption + i);
			try
			{
				uicOption.minDT = Integer.valueOf(temp);
			}
			catch (NumberFormatException e)
			{
				tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
				uicOption.minDT = 0;
			}
			catch (Exception e)
			{
				tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
				uicOption.minDT = 0;
			}
		}
		else if (String.valueOf(sOption).contains("MAXDT"))
		{
			ipOption = ipOption + i + 1;
			i = 0;
			while (ipOption + i < pOption.length && pOption[ipOption + i] != '&' && pOption[ipOption + i] != 0)
				i++;

			if (ipOption + i < pOption.length)
			{
				chTmp = pOption[ipOption + i];
				pOption[ipOption + i] = 0;
			}
			else
				chTmp = 0;

			String temp = String.valueOf(pOption).substring(ipOption, ipOption + i);
			tsLib.debugPrint(DEBUG_DM, "temp :" + temp);
			try
			{
				uicOption.maxDT = Integer.valueOf(temp);
			}
			catch (NumberFormatException e)
			{
				tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
				uicOption.maxDT = 0;
			}
			catch (Exception e)
			{
				tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
				uicOption.maxDT = 0;
			}
		}
		else if (String.valueOf(sOption).contains("DR"))
		{
			ipOption = ipOption + i + 1;
			i = 0;
			while (ipOption + i < pOption.length && pOption[ipOption + i] != '&' && pOption[ipOption + i] != 0)
				i++;

			if (ipOption + i < pOption.length)
			{
				chTmp = pOption[ipOption + i];
				pOption[ipOption + i] = 0;
			}
			else
				chTmp = 0;

			String temp = String.valueOf(pOption).substring(ipOption, ipOption + i);
			uicOption.defaultResponse = tsList.listCopyStrText(uicOption.defaultResponse, temp);
		}
		else if (String.valueOf(sOption).contains("MAXLEN"))
		{
			ipOption = ipOption + i + 1;
			i = 0;
			while (ipOption + i < pOption.length && pOption[ipOption + i] != '&' && pOption[ipOption + i] != 0)
				i++;

			if (ipOption + i < pOption.length)
			{
				chTmp = pOption[ipOption + i];
				pOption[ipOption + i] = 0;
			}
			else
				chTmp = 0;

			String temp = String.valueOf(pOption).substring(ipOption, ipOption + i);
			try
			{
				uicOption.maxLen = Integer.valueOf(temp);
			}
			catch (NumberFormatException e)
			{
				tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
				uicOption.maxLen = 0;
			}
			catch (Exception e)
			{
				tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
				uicOption.maxLen = 0;
			}
		}
		else if (String.valueOf(sOption).contains("IT"))
		{
			ipOption = ipOption + i + 1;
			i = 0;
			while (ipOption + i < pOption.length && pOption[ipOption + i] != '&' && pOption[ipOption + i] != 0)
				i++;

			if (ipOption + i < pOption.length)
			{
				chTmp = pOption[ipOption + i];
				pOption[ipOption + i] = 0;
			}
			else
				chTmp = 0;

			sOption = null;
			sOption = new char[i];
			String.valueOf(pOption).getChars(ipOption, ipOption + i, sOption, 0);
			switch (pOption[ipOption])
			{
				case 'A':
					uicOption.inputType = UIC_INPUTTYPE_ALPHANUMERIC;
					break;
				case 'N':
					uicOption.inputType = UIC_INPUTTYPE_NUMERIC;
					break;
				case 'D':
					uicOption.inputType = UIC_INPUTTYPE_DATE;
					break;
				case 'T':
					uicOption.inputType = UIC_INPUTTYPE_TIME;
					break;
				case 'P':
					uicOption.inputType = UIC_INPUTTYPE_PHONENUBMER;
					break;
				case 'I':
					uicOption.inputType = UIC_INPUTTYPE_IPADDRESS;
					break;
			}
		}
		else if (String.valueOf(sOption).contains("ET"))
		{
			ipOption = ipOption + i + 1;
			i = 0;
			while (ipOption + i < pOption.length && pOption[ipOption + i] != '&' && pOption[ipOption + i] != 0)
				i++;

			if (ipOption + i < pOption.length)
			{
				chTmp = pOption[ipOption + i];
				pOption[ipOption + i] = 0;
			}
			else
				chTmp = 0;
			sOption = null;
			sOption = new char[i];
			String.valueOf(pOption).getChars(ipOption, ipOption + i, sOption, 0);
			switch (pOption[ipOption])
			{
				case 'T':
					uicOption.echoType = UIC_ECHOTYPE_TEXT;
					break;
				case 'P':
					uicOption.echoType = UIC_ECHOTYPE_PASSWORD;
					break;
			}
		}
		else
		{
			return String.valueOf(sOption);
		}

		if (chTmp == '&')
		{
			ipOption = ipOption + i + 1;

			sOption = null;
			sOption = new char[pOption.length - ipOption];
			String.valueOf(pOption).getChars(ipOption, pOption.length, sOption, 0);

			return tsDmUic.dmUicOptionProcess(new String(sOption), uicOption);
		}
		return String.valueOf(sOption);
	}

	public static tsDmUicResult dmGetUicResultKeep(int pUicResultKeepFlag)
	{
		tsDBUICResultKeep ptUicResultKeep = null;
		tsDmUicResult ptUicResult = null;

		ptUicResultKeep = new tsDBUICResultKeep();

		ptUicResult = tsDmUic.createUicResult();
		//if (ptUicResult == null)
		//	return null;

		ptUicResultKeep = (tsDBUICResultKeep) tsdmDB.dmdbRead(E2P_SYNCML_DM_UIC_RESULT_KEEP, ptUicResultKeep);

		//pUicResultKeepFlag = ptUicResultKeep.eStatus;

		ptUicResult.appId = ptUicResultKeep.appId;
		ptUicResult.UICType = ptUicResultKeep.UICType;
		ptUicResult.result = ptUicResultKeep.result;
		ptUicResult.MenuNumbers = ptUicResultKeep.number;

		if (ptUicResultKeep.szText.length() > 0)
		{
			ptUicResult.text.len = ptUicResultKeep.nLen;
			ptUicResult.text.size = ptUicResultKeep.nSize;
			ptUicResult.text.text = ptUicResultKeep.szText;
		}

		return ptUicResult;
	}
}
