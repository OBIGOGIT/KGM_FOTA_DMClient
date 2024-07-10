package com.tsdm.parser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.parsers.*;

import org.xml.sax.helpers.DefaultHandler;

import com.tsdm.agent.dmDefineDevInfo;
import com.tsdm.adapt.tsLib;

public class ddfXmlParser implements dmDefineDevInfo
{
	public ddfXmlParser(DefaultHandler handler, String s)
	{
		InputStream inputSt = null;
		try
		{
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
			factory.setValidating(false); // DTD Validation check

			inputSt = new ByteArrayInputStream(s.toString().getBytes());

			parser.parse(inputSt, handler);
			// parser.parse(new File(s), handler);
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, "error : " + e.toString());
		}
		finally
		{
			// Defects : Close open streams in finally() blocks
			if (inputSt != null)
				try
				{
					inputSt.close();
				}
				catch (IOException e)
				{
					tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
				}
		}
	}
}
