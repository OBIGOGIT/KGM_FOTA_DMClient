package com.tsdm.parser;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.tsdm.agent.dmDefineDevInfo;
import com.tsdm.adapt.tsLib;
import com.tsdm.tsService;

public class ddXMLParser implements dmDefineDevInfo
{
	private ByteArrayInputStream	pXMLStream;
	private InputSource				pInputXMLSource;

	public ddXMLDataSet dlParserDownloadDescriptor(byte[] pDump) throws ParserConfigurationException, SAXException, IOException
	{
		tsLib.debugPrint(DEBUG_PARSER, "");
		SAXParserFactory spf = SAXParserFactory.newInstance();
		spf.setFeature("http://xml.org/sax/features/external-general-entities", false);
		SAXParser sp = spf.newSAXParser();
		XMLReader xr = sp.getXMLReader();

		ddXMLHandler mDDXMLHandler = new ddXMLHandler();
		xr.setContentHandler(mDDXMLHandler);

		if (pDump == null || pDump.length == 0)
			return null;

		pXMLStream = new ByteArrayInputStream(pDump);
		pInputXMLSource = new InputSource(pXMLStream);
		xr.parse(pInputXMLSource);

		ddXMLDataSet parsedDataSet = mDDXMLHandler.getParsedData();
		try {
		int target_num = parsedDataSet.objectURI.indexOf("dd?id=")+6;
		String workId = parsedDataSet.objectURI.substring(target_num,(parsedDataSet.objectURI.substring(target_num).indexOf("&pkgid")+target_num));
		tsLib.debugPrint(DEBUG_PARSER, "workId "+workId);
		tsLib.debugPrint(DEBUG_PARSER, "objectURI "+ parsedDataSet.objectURI);
		tsLib.debugPrint(DEBUG_PARSER, "name "+parsedDataSet.name);
		tsLib.debugPrint(DEBUG_PARSER, "crc "+parsedDataSet.crc);
		tsLib.debugPrint(DEBUG_PARSER, "logUploadURI  "+parsedDataSet.logUploadURI);
		tsLib.debugPrint(DEBUG_PARSER, "size "+parsedDataSet.size);
		tsLib.debugPrint(DEBUG_PARSER, "installNotifyURI "+parsedDataSet.installNotifyURI);
		tsLib.debugPrint(DEBUG_PARSER, "rVersion  "+parsedDataSet.rVersion);
		//tsLib.debugPrint(DEBUG_PARSER, "description "+parsedDataSet.description);

		tsService.setDownFileInfo(workId, parsedDataSet.logUploadURI, parsedDataSet.description,
				                 parsedDataSet.name,parsedDataSet.crc,parsedDataSet.size,parsedDataSet.rVersion);

		//tsLib.debugPrint(DEBUG_PARSER, "total " + parsedDataSet.toString());
		}catch(NullPointerException ex) {
				tsLib.debugPrintException(DEBUG_EXCEPTION, ex.toString());
		}
		tsLib.debugPrint(DEBUG_PARSER, "Parsing DownloadDescriptor is Complete");
		return parsedDataSet;
	}
}
