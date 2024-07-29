package com.tsdm.parser;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class ddXMLHandler extends DefaultHandler implements ddInterface
{
	private boolean			in_ddversion				= false;
	private boolean			in_name						= false;
	private boolean			in_type						= false;
	private boolean			in_crc						= false;
	private boolean			in_logUploadURI 			= false;
	private boolean			in_rVersion     			= false;
	private boolean			in_description				= false;
	private boolean			in_objecturi				= false;
	private boolean			in_size						= false;
	private boolean			in_notifyuri				= false;
	private boolean			in_installparam				= false;
	private boolean			in_vendor					= false;
	private boolean			in_nextURL					= false;
	private boolean			in_infoURL					= false;
	private boolean			in_iconURI					= false;

	private ddXMLDataSet DownloadDescriptorDataSet	= new ddXMLDataSet();

	@Override
	public void startElement(String namespacesURI, String localName, String qName, Attributes atts) throws SAXException
	{
		if (szDDVersion.equals(localName))
		{
			this.in_ddversion = true;
		}
		else if (szName.equals(localName))
		{
			this.in_name = true;
		}
		else if (szType.equals(localName))
		{
			this.in_type = true;
		}
		else if (szCrc.equals(localName))
		{
			this.in_crc = true;
		}
		else if (szLogUploadURI.equals(localName))
		{
			this.in_logUploadURI = true;
		}
		else if (szRVersion.equals(localName))
		{
			this.in_rVersion = true;
		}
		else if (szDescription.equals(localName))
		{
			this.in_description = true;
		}
		else if (szObjectURI.equals(localName))
		{
			this.in_objecturi = true;
		}
		else if (szSize.equals(localName))
		{
			this.in_size = true;
		}
		else if (szNotifyURI.equals(localName))
		{
			this.in_notifyuri = true;
		}
		else if (szInstallParam.equals(localName))
		{
			this.in_installparam = true;
		}
		else if (szVendor.equals(localName))
		{
			this.in_vendor = true;
		}
		else if (szNextURL.equals(localName))
		{
			this.in_nextURL = true;
		}
		else if (szInforURL.equals(localName))
		{
			this.in_infoURL = true;
		}
		else if (sziconURI.equals(localName))
		{
			this.in_iconURI = true;
		}
	}

	@Override
	public void endElement(String namespaceURL, String localName, String qName) throws SAXException
	{
		if (szDDVersion.equals(localName))
		{
			this.in_ddversion = false;
		}
		else if (szName.equals(localName))
		{
			this.in_name = false;
		}
		else if (szType.equals(localName))
		{
			this.in_type = false;
		}
		else if (szCrc.equals(localName))
		{
			this.in_crc = false;
		}
		else if (szLogUploadURI.equals(localName))
		{
			this.in_logUploadURI = false;
		}
		else if (szRVersion.equals(localName))
		{
			this.in_rVersion = false;
		}
		else if (szDescription.equals(localName))
		{
			this.in_description = false;
		}
		else if (szObjectURI.equals(localName))
		{
			this.in_objecturi = false;
		}
		else if (szSize.equals(localName))
		{
			this.in_size = false;
		}
		else if (szNotifyURI.equals(localName))
		{
			this.in_notifyuri = false;
		}
		else if (szInstallParam.equals(localName))
		{
			this.in_installparam = false;
		}
		else if (szVendor.equals(localName))
		{
			this.in_vendor = false;
		}
		else if (szNextURL.equals(localName))
		{
			this.in_nextURL = false;
		}
		else if (szInforURL.equals(localName))
		{
			this.in_infoURL = false;
		}
		else if (sziconURI.equals(localName))
		{
			this.in_iconURI = false;
		}
	}

	@Override
	public void characters(char ch[], int start, int length)
	{
		if (this.in_ddversion)
		{
			DownloadDescriptorDataSet.DDVersion += new String(ch, start, length);
		}
		else if (this.in_name)
		{
			DownloadDescriptorDataSet.name += new String(ch, start, length);
		}
		else if (this.in_type)
		{
			DownloadDescriptorDataSet.type += new String(ch, start, length);
		}
		else if (this.in_crc)
		{
			DownloadDescriptorDataSet.crc += new String(ch, start, length);
		}
		else if (this.in_logUploadURI)
		{
			DownloadDescriptorDataSet.logUploadURI += new String(ch, start, length);
		}
		else if (this.in_rVersion)
		{
			DownloadDescriptorDataSet.rVersion += new String(ch, start, length);
		}
		else if (this.in_description)
		{
			DownloadDescriptorDataSet.description += new String(ch, start, length);
		}
		else if (this.in_objecturi)
		{
			DownloadDescriptorDataSet.objectURI += new String(ch, start, length);
		}
		else if (this.in_size)
		{
			DownloadDescriptorDataSet.size += new String(ch, start, length);
		}
		else if (this.in_notifyuri)
		{
			DownloadDescriptorDataSet.installNotifyURI += new String(ch, start, length);
		}
		else if (this.in_installparam)
		{
			DownloadDescriptorDataSet.installParam += new String(ch, start, length); // install parameter case
		}
		else if (this.in_vendor)
		{
			DownloadDescriptorDataSet.vendor += new String(ch, start, length);
		}
		else if (this.in_nextURL)
		{
			DownloadDescriptorDataSet.nextURL += new String(ch, start, length);
		}
		else if (this.in_infoURL)
		{
			DownloadDescriptorDataSet.infoURL += new String(ch, start, length);
		}
		else if (this.in_iconURI)
		{
			DownloadDescriptorDataSet.iconURI += new String(ch, start, length);
		}
	}

	@Override
	public void startDocument() throws SAXException
	{
		// Do some startup if needed
	}

	@Override
	public void endDocument() throws SAXException
	{
		// Do some finishing work if needed
	}

	public ddXMLDataSet getParsedData()
	{
		return this.DownloadDescriptorDataSet;
	}
}
