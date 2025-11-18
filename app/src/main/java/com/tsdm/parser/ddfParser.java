package com.tsdm.parser;

import java.io.FileInputStream;
import java.io.IOException;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.tsdm.agent.dmAgent;
import com.tsdm.core.data.constants.DmDevInfoConst;
import com.tsdm.db.tsdmDB;
import com.tsdm.adapt.tsLinkedList;
import com.tsdm.adapt.tsDmVnode;
import com.tsdm.adapt.tsLib;
import com.tsdm.adapt.tsOmList;
import com.tsdm.adapt.tsOmTree;
import com.tsdm.adapt.tsOmlib;

interface xmlDefine
{
	public final int	XML_TAG_AccessType					= 0x05;
	public final int	XML_TAG_ACL							= 0x06;
	public final int	XML_TAG_Add							= 0x07;
	public final int	XML_TAG_b64							= 0x08;
	public final int	XML_TAG_bin							= 0x09;
	public final int	XML_TAG_bool						= 0x0A;
	public final int	XML_TAG_chr							= 0x0B;
	public final int	XML_TAG_CaseSense					= 0x0C;
	public final int	XML_TAG_CIS							= 0x0D;
	public final int	XML_TAG_Copy						= 0x0E;
	public final int	XML_TAG_CS							= 0x0F;
	public final int	XML_TAG_date						= 0x10;
	public final int	XML_TAG_DDFName						= 0x11;
	public final int	XML_TAG_DefaultValue				= 0x12;
	public final int	XML_TAG_Delete						= 0x13;
	public final int	XML_TAG_Description					= 0x14;
	public final int	XML_TAG_DFFormat					= 0x15;
	public final int	XML_TAG_DFProperties				= 0x16;
	public final int	XML_TAG_DFTitle						= 0x17;
	public final int	XML_TAG_DFType						= 0x18;
	public final int	XML_TAG_Dynamic						= 0x19;
	public final int	XML_TAG_Exec						= 0x1A;
	public final int	XML_TAG_float						= 0x1B;
	public final int	XML_TAG_Format						= 0x1C;
	public final int	XML_TAG_Get							= 0x1D;
	public final int	XML_TAG_int							= 0x1E;
	public final int	XML_TAG_Man							= 0x1F;
	public final int	XML_TAG_MgmtTree					= 0x20;
	public final int	XML_TAG_MIME						= 0x21;
	public final int	XML_TAG_Mod							= 0x22;
	public final int	XML_TAG_Name						= 0x23;
	public final int	XML_TAG_Node						= 0x24;
	public final int	XML_TAG_node						= 0x25;
	public final int	XML_TAG_NodeName					= 0x26;
	public final int	XML_TAG_null						= 0x27;
	public final int	XML_TAG_Occurrence					= 0x28;
	public final int	XML_TAG_One							= 0x29;
	public final int	XML_TAG_OneOrMore					= 0x2A;
	public final int	XML_TAG_OneOrN						= 0x2B;
	public final int	XML_TAG_Path						= 0x2C;
	public final int	XML_TAG_Permanent					= 0x2D;
	public final int	XML_TAG_Replace						= 0x2E;
	public final int	XML_TAG_RTProperties				= 0x2F;
	public final int	XML_TAG_Scope						= 0x30;
	public final int	XML_TAG_Size						= 0x31;
	public final int	XML_TAG_time						= 0x32;
	public final int	XML_TAG_Title						= 0x33;
	public final int	XML_TAG_TStamp						= 0x34;
	public final int	XML_TAG_Type						= 0x35;
	public final int	XML_TAG_Value						= 0x36;
	public final int	XML_TAG_VerDTD						= 0x37;
	public final int	XML_TAG_VerNo						= 0x38;
	public final int	XML_TAG_xml							= 0x39;
	public final int	XML_TAG_ZeroOrMore					= 0x3A;
	public final int	XML_TAG_ZeroOrN						= 0x3B;
	public final int	XML_TAG_ZeroOrOne					= 0x3C;

	public final int	XML_TAG_MgmtTree_String_Start		= 0x00;
	public final int	XML_TAG_MgmtTree_String_End			= 0x01;

	public final int	XML_TAG_VerDTD_String_Start			= 0x02;
	public final int	XML_TAG_VerDTD_String_End			= 0x03;

	public final int	XML_TAG_Node_String_Start			= 0x04;
	public final int	XML_TAG_Node_String_End				= 0x05;

	public final int	XML_TAG_NodeName_String_Start		= 0x06;
	public final int	XML_TAG_NodeName_String_End			= 0x07;

	public final int	XML_TAG_Path_String_Start			= 0x08;
	public final int	XML_TAG_Path_String_End				= 0x09;

	public final int	XML_TAG_Value_String_Start			= 0x0A;
	public final int	XML_TAG_Value_String_End			= 0x0B;

	public final int	XML_TAG_RTProperties_String_Start	= 0x0C;
	public final int	XML_TAG_RTProperties_String_End		= 0x0D;

	public final int	XML_TAG_ACL_String_Start			= 0x0E;
	public final int	XML_TAG_ACL_String_End				= 0x0F;

	public final int	XML_TAG_Format_String_Start			= 0x10;
	public final int	XML_TAG_Format_String_End			= 0x11;

	public final int	XML_TAG_Type_String_Start			= 0x12;
	public final int	XML_TAG_Type_String_End				= 0x13;

	public final int	XML_TAG_Add_String_Start			= 0x14;
	public final int	XML_TAG_Add_String_End				= 0x15;

	public final int	XML_TAG_Get_String_Start			= 0x16;
	public final int	XML_TAG_Get_String_End				= 0x17;

	public final int	XML_TAG_Replace_String_Start		= 0x18;
	public final int	XML_TAG_Replace_String_End			= 0x19;

	public final int	XML_TAG_Delete_String_Start			= 0x1A;
	public final int	XML_TAG_Delete_String_End			= 0x1B;

	public final int	XML_TAG_Exec_String_Start			= 0x1C;
	public final int	XML_TAG_Exec_String_End				= 0x1D;

	public final int	XML_TAG_AccessType_String_Start		= 0x1E;
	public final int	XML_TAG_AccessType_String_End		= 0x1F;

	public final int	XML_TAG_CDATA_String_Start			= 0x20;
	public final int	XML_TAG_CDATA_String_End			= 0x21;

	public final int	XML_TAG_SyncML_String_Start			= 0x22;
	public final int	XML_TAG_SyncML_String_End			= 0x23;

	public final int	XML_TAG_ResultCode_String_Start		= 0x24;									
	public final int	XML_TAG_ResultCode_String_End		= 0x25;							

	public final int	XML_TAG_Identifier_String_Start		= 0x26;									
	public final int	XML_TAG_Identifier_String_End		= 0x27;							

	public final int 	DM_MO_ID_NONE 						= 0x00;
	public final int 	DM_MO_ID_W1 						= 0x01;
	public final int 	DM_MO_ID_W2 						= 0x02;
	public final int 	DM_MO_ID_W3 						= 0x03;
	public final int 	DM_MO_ID_W4 						= 0x04;
	public final int 	DM_MO_ID_W5 						= 0x05;
	public final int 	DM_MO_ID_W6							= 0x06;
	public final int 	DM_MO_ID_W7 						= 0x07;
	public final int 	DM_MO_ID_DEVINFO 					= 0x08;
	public final int 	DM_MO_ID_DEVDETAIL 					= 0x09;
	public final int 	DM_MO_ID_INBOX 						= 0x0A;
	public final int 	DM_MO_ID_FUMO						= 0x0B;
	public final int 	DM_MO_ID_END						= 0x0C;

	public final int	XML_ERR_OK							= 0;
	public final int	XML_ERROR							= 3000;
	public final int	XML_ERR_INVALID_PARAM				= XML_ERROR + 1;
	public final int	XML_ERR_PARSING_FAIL				= XML_ERROR + 2;
	public final int	DRM_ERR_XML_DUPLICATE_DATA			= XML_ERROR + 3;
	public final int	DRM_ERR_XML_NOT_FOUND_NAMESPACE		= XML_ERROR + 4;
	public final int	DRM_ERR_XML_UNKNOWN_NAMESPACE		= XML_ERROR + 5;
	public final int	DRM_ERR_XML_INTERNAL_ERROR			= XML_ERROR + 6;
	public final int	DRM_ERR_XML_NEEDLESS_NAMESPACE		= XML_ERROR + 7;
	public final int	DRM_ERR_XML_UNDEFINED				= XML_ERROR + 8;

	public final int	DDF_ID_SYNCML						= 0;
	public final int	DDF_ID_DEVINFO						= 1;
	public final int	DDF_ID_DEVDETAIL					= 2;
	public final int	DDF_ID_APP							= 3;
	public final int	DDF_ID_FUMO							= 4;
	public final int	DDF_ID_TNDS							= 5;

	public final int	DM_TNDS_ACCESSTYPE				= 0x05 + 0x40;
	public final int	DM_TNDS_ACL						= 0x06 + 0x40;
	public final int	DM_TNDS_ADD						= 0x07 + 0x40;
	public final int	DM_TNDS_B64_FORMAT				= 0x08 + 0x40;
	public final int	DM_TNDS_BIN_FORMAT				= 0x09 + 0x40;
	public final int	DM_TNDS_BOOL_FORMAT				= 0x0A + 0x40;
	public final int	DM_TNDS_CHR_FORMAT				= 0x0B + 0x40;
	public final int	DM_TNDS_CASESENSE				= 0x0C + 0x40;
	public final int	DM_TNDS_CIS						= 0x0D + 0x40;
	public final int	DM_TNDS_COPY					= 0x0E + 0x40;
	public final int	DM_TNDS_CS						= 0x0F + 0x40;
	public final int	DM_TNDS_DATE_FORMAT				= 0x10 + 0x40;									/* 0x10 + 0x40 */
	public final int	DM_TNDS_DDFNAME					= 0x11 + 0x40;
	public final int	DM_TNDS_DEFAULTVALUE			= 0x12 + 0x40;
	public final int	DM_TNDS_DELETE					= 0x13 + 0x40;
	public final int	DM_TNDS_DESCRIPTION				= 0x14 + 0x40;
	public final int	DM_TNDS_DFFORMAT				= 0x15 + 0x40;
	public final int	DM_TNDS_DFPROPERTIES			= 0x16 + 0x40;
	public final int	DM_TNDS_DFTITLE					= 0x17 + 0x40;
	public final int	DM_TNDS_DFTYPE					= 0x18 + 0x40;
	public final int	DM_TNDS_DYNAMIC					= 0x19 + 0x40;
	public final int	DM_TNDS_EXEC					= 0x1A + 0x40;
	public final int	DM_TNDS_FLOAT_FORMAT			= 0x1B + 0x40;
	public final int	DM_TNDS_FORMAT					= 0x1C + 0x40;
	public final int	DM_TNDS_GET						= 0x1D + 0x40;
	public final int	DM_TNDS_INT_FORAMT				= 0x1E + 0x40;
	public final int	DM_TNDS_MAN						= 0x1F + 0x40;
	public final int	DM_TNDS_MGMTTREE				= 0x20 + 0x40;
	public final int	DM_TNDS_MIME					= 0x21 + 0x40;
	public final int	DM_TNDS_MOD						= 0x22 + 0x40;
	public final int	DM_TNDS_NAME					= 0x23 + 0x40;
	public final int	DM_TNDS_NODE					= 0x24 + 0x40;
	public final int	DM_TNDS_NODE_FORAMT				= 0x25 + 0x40;
	public final int	DM_TNDS_NOADNAME				= 0x26 + 0x40;
	public final int	DM_TNDS_NULL_FORMAT				= 0x27 + 0x40;
	public final int	DM_TNDS_OCCURRENCE				= 0x28 + 0x40;
	public final int	DM_TNDS_ONE						= 0x29 + 0x40;
	public final int	DM_TNDS_ONEORMORE				= 0x2A + 0x40;
	public final int	DM_TNDS_ONEORN					= 0x2B + 0x40;
	public final int	DM_TNDS_PATH					= 0x2C + 0x40;
	public final int	DM_TNDS_PERMANENT				= 0x2D + 0x40;
	public final int	DM_TNDS_REPLACE					= 0x2E + 0x40;
	public final int	DM_TNDS_RTPROPERTIES			= 0x2F + 0x40;
	public final int	DM_TNDS_SCOPE					= 0x30 + 0x40;
	public final int	DM_TNDS_SIZE					= 0x31 + 0x40;
	public final int	DM_TNDS_TIME_FORMAT				= 0x32 + 0x40;
	public final int	DM_TNDS_TITLE					= 0x33 + 0x40;
	public final int	DM_TNDS_TSTAMP					= 0x34 + 0x40;
	public final int	DM_TNDS_TYPE					= 0x35 + 0x40;
	public final int	DM_TNDS_VALUE					= 0x36 + 0x40;
	public final int	DM_TNDS_VERDTD					= 0x37 + 0x40;
	public final int	DM_TNDS_VERNO					= 0x38 + 0x40;
	public final int	DM_TNDS_XML_FORMAT				= 0x39 + 0x40;
	public final int	DM_TNDS_ZEROORMORE				= 0x3A + 0x40;
	public final int	DM_TNDS_ZEROORN					= 0x3B + 0x40;
	public final int	DM_TNDS_ZEROORONE				= 0x3C + 0x40;
	public final int	DM_TNDS_MAX						= 0x3D + 0x40;
	public final int	DM_TNDS_SYNCML_SPECIAL_VALUE	= 0x88;

	public final int	DM_WBXML_CODEPAGE				= 0x00;
	public final int	DM_WBXML_CLOSE_BRACKET			= 0x01;
	public final int	DM_WBXML_VERSION				= 0x02;

	public final int	DM_TNDS_TAG_NUM					= DM_TNDS_MAX - DM_TNDS_ACCESSTYPE;
	public final int	DM_TNDS_TAG_BRACKET_NUM			= 3;
	public final int	DM_TNDS_TAG_NAME_MAX_LEN		= 20;
	public final int	DM_TNDS_INCLUDE_TAG_MAX_NUM		= 30;
	public final int	DM_TNDS_TYPE_DATA_MAX_LEN		= 16;

	public final int	DM_WBXML_START_STRING_TAG		= 0x03;
	public final int	DM_WBXML_END_STRING_TAG			= 0x00;
	public final int	DM_WBXML_END_TAG				= 0x01;
	public final int	DM_XML_SYNCML_HEX_VALUE			= 0x6D;

	public final String	DM_XML_VERSION_STRING			= "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
	public final String	DM_XML_NAME_SPACE_STRING		= "<SyncML xmlns=\'syncml:dmddf1.2\'>";
	public final String	DM_XML_SYNCML_END_STRING		= "</SyncML>";
	public final String	DM_XML_SYNCML_STRING			= "SyncML";
	public final String	DM_XML_SYNCML_OPEN_TAG			= "<SyncML>";

	public final char	DM_XML_OPEN_TAG					= '<';
	public final char	DM_XML_CLOSE_TAG				= '>';
	public final char	DM_XML_SLASH					= '/';
	public final char	DM_XML_LINE_FEED				= '\n';

	public final int	DM_CONVERT_XML_TAG_MAX_LEN		= 32;

	public final int	DM_WBXML_VERSION_LEN			= 0x01;
	public final int	DM_WBXML_PUBLIC_ID_LEN			= 0x02;
	public final int	DM_WBXML_CHARACTER_SET_LEN		= 0x01;
	public final int	DM_WBXML_STRING_TABLE_LEN		= (0x1A + 0x01);
	public final int	DM_WBXML_TNDS_CODEPAGE_LEN		= 0x02;
	
	public final int DM_WBXML_HEADER_LEN 				= DM_WBXML_VERSION_LEN
															+ DM_WBXML_PUBLIC_ID_LEN
															+ DM_WBXML_CHARACTER_SET_LEN
															+ DM_WBXML_STRING_TABLE_LEN;
}

class DDFXmlElement
{
	int		acl;
	String	tag;
	String	name;
	String	type;
	String	DDFName;
	String	MIME;
	String	path;
	String	data;
	String	TStamp;
	String	Title;
	String	Size;
	String	VerNo;
	int		scope;
	int		format;
}

class XMLStream
{
	int		size;
	String	data;
}

class DM_Tree
{
	Object			object;
	DM_Tree parent;
	tsLinkedList childlist;
	tsLinkedList next;
}

class DMTndsData
{
	int		nWbxmlDataSize;
	String	pWbxmlData;
	String	pWbxmlDataStart;
	int		nXMLDataSize;
	String	pXMLData;
	String	pXMLDataStart;
}

class DMTndsTagManage implements xmlDefine
{
	int[]	eTagID;
	int		nTagSP;

	public DMTndsTagManage()
	{
		eTagID = new int[DM_TNDS_INCLUDE_TAG_MAX_NUM];
		nTagSP = 0;
	}
}

public class ddfParser extends DefaultHandler implements xmlDefine
{

	public static boolean				bNodeChangeMode;
	public static DM_Tree 				CurXmlTree;
	public static DM_Tree 				XmlTree;

	public int							gTagCode;
	public static tsOmTree g_om;

	public static DMTndsTagManage gstTagManage;
	public static DMTndsData gTndsData;

	public static String				gNewAccPath;

	public ddfParser()
	{
		bNodeChangeMode = false;
		CurXmlTree = new DM_Tree();
		gTagCode = 0;
		g_om = new tsOmTree();
		gNewAccPath = null;
	}

	public static String gSdmXmlOmaTags[] = {
			"",
			"",
			"",
			"",
			"",
			"AccessType",
			"ACL",
			"Add",
			"b64",
			"bin",
			"bool",
			"chr",
			"CaseSense",
			"CIS",
			"Copy",
			"CS",
			"data",
			"DDFName",
			"DefaultValue",
			"Delete",
			"Description",
			"DFFormat",
			"DFProperties",
			"DFTitle",
			"DFType",
			"Dynamic",
			"Exec",
			"float",
			"Format",
			"Get",
			"int",
			"Man",
			"MgmtTree",
			"MIME",
			"Mod",
			"Name",
			"Node",
			"node",
			"NodeName",
			"null",
			"Occurrence",
			"One",
			"OneOrMore",
			"OneOrN",
			"Path",
			"Permanent",
			"Replace",
			"RTProperties",
			"Scope",
			"Size",
			"time",
			"Title",
			"TStamp",
			"Type",
			"Value",
			"VerDTD",
			"VerNo",
			"xml",
			"ZeroOrMore",
			"ZeroOrN",
			"ZeroOrOne" };

	public static String gSdmXmlTagString[] = {
			"<MgmtTree>",		"</MgmtTree>\n",
			"<VerDTD>",			"</VerDTD>\n",
			"<Node>",			"</Node>\n",
			"<NodeName>",		"</NodeName>\n",
			"<Path>",			"</Path>\n",
			"<Value>",			"</Value>\n",
			"<RTProperties>",	"</RTProperties>\n",
			"<ACL>",			"</ACL>\n",
			"<Format>",			"</Format>\n",
			"<Type>",			"</Type>\n",
			"<Add>",			"</Add>",   
			"<Get>",			"</Get>",
			"<Replace>",		"</Replace>",
			"<Delete>",			"</Delete>",
			"<Exec>",			"</Exec>",
			"<AccessType>",		"</AccessType>\n", 
			"<![CDATA[",		"]]>",
			"<SyncML>",			"</SyncML>\n",
			"<ResultCode>",		"</ResultCode>",
			"<Identifier>",		"</Identifier>",
			"",					"" };

	public static String gSdmManagementObjectIdPath[] = {
			null,
			"./SyncML/DMAcc",
			"./SyncML/DMAcc",
			"./SyncML/DMAcc",
			"./SyncML/DMAcc",
			"./SyncML/DMAcc",
			"./SyncML/DMAcc",
			"./SyncML/DMAcc",
			"./DevInfo",
			"./DevDetail",
			"./Inbox",
			"./FUMO",
			null };

	public static String gSdmManagementObjectIdType[] = {
			null,
			"org.openmobilealliance/1.0/w1",
			"org.openmobilealliance/1.0/w2",
			"org.openmobilealliance/1.0/w3",
			"org.openmobilealliance/1.0/w4",
			"org.openmobilealliance/1.0/w5",
			"org.openmobilealliance/1.0/w6",
			"org.openmobilealliance/1.0/w7",
			"org.openmobilealliance.dm/1.0/DevInfo",
			"org.openmobilealliance.dm/1.0/DevDetail",
			"org.openmobilealliance.dm/1.0/Inbox",
			"org.openmobilealliance/1.0/FirmwareUpdateManagementObject",
			null };

	public static String gszTndsTokenStr[] = {
			"AccessType",
			"ACL",
			"Add",
			"b64",
			"bin",
			"bool",
			"chr",
			"CaseSense",
			"CIS",
			"Copy",
			"CS",
			"date",
			"DDFName",
			"DefaultValue",
			"Delete",
			"Description",
			"DFFormat",
			"DFProperties",
			"DFTitle",
			"DFType",
			"Dynamic",
			"Exec",
			"float",
			"Format",
			"Get",
			"int",
			"Man",
			"MgmtTree",
			"MIME",
			"Mod",
			"Name",
			"Node",
			"node",
			"NodeName",
			"null",
			"Occurrence",
			"One",
			"OneOrMore",
			"OneOrN",
			"Path",
			"Permanent",
			"Replace",
			"RTProperties",
			"Scope",
			"Size",
			"time",
			"Title",
			"TStamp",
			"Type",
			"Value",
			"VerDTD",
			"VerNo",
			"xml",
			"ZerOrMore",
			"ZeroOrN",
			"ZeroOrOne" };

	public static char[] gTndsWbxmlHeaderInfo = {
		   0x02,			/*wbxmlVer*/
		   0x00,0x00,		/*publicID*/
		   0x6A,			/*ChrSet*/
		   0x1A,			/*StringTableSize*/
		   0x2D,0x2F,0x2F,0x4F,0x4D,0x41,0x2F,0x2F,0x44,
		   0x54,0x44,0x2D,0x44,0x4D,0x2D,0x44, 0x44,0x46,
		   0x20,0x31,0x2E,0x32,0x2F,0x2F,0x45,0x4E			/*StringTable*/
	};

	private static void OMSETPATH(tsOmTree g_om, String path, int aclValue, int scope)
	{
		if (tsOmlib.dmOmLibGetNodeProp(g_om, path) == null)
		{
			tsOmlib.dmOmWrite(g_om, path, 0, 0, "", 0);
			dmAgent.dmAgentMakeDefaultAcl(g_om, path, aclValue, scope);
		}
	}

	@Override
	public void endDocument() throws SAXException
	{
		super.endDocument();
	}

	@Override
	public void startDocument() throws SAXException
	{
		super.startDocument();
	}

	public void startElement(String nameSpaceURI, String localName, String qName, Attributes atts) throws SAXException
	{
		DDFXmlElement XmlElement;
		DM_Tree childtree;
		int nTagCode;

		nTagCode = dmDDFXmlTagCode(localName);
		gTagCode = nTagCode;

		tsLib.debugPrint(DmDevInfoConst.DEBUG_PARSER, "start =          " + localName);

		switch (nTagCode)
		{
			case XML_TAG_Node:
				XmlElement = new DDFXmlElement();

				if (CurXmlTree.object == null)
				{
					CurXmlTree.object = (Object) XmlElement;

					XmlTree = CurXmlTree; // first link point save for java
				}
				else
				{
					if (CurXmlTree.childlist == null)
					{
						CurXmlTree.childlist = tsLinkedList.listCreateLinkedList();
					}

					childtree = new DM_Tree();
					childtree.parent = CurXmlTree;
					childtree.object = (Object) XmlElement;

					tsLinkedList.listAddObjAtLast(CurXmlTree.childlist, childtree);

					CurXmlTree = childtree;
				}
				XmlElement.tag = localName;
				break;

			case XML_TAG_DFProperties:
				break;

			default:
				break;

		}

	}

	public void endElement(String namespaceURI, String localName, String qName) throws SAXException
	{
		DDFXmlElement XmlElement = new DDFXmlElement();

		tsLib.debugPrint(DmDevInfoConst.DEBUG_PARSER, "end =            " + localName);

		if (CurXmlTree == null)
		{
			return;
		}

		XmlElement = (DDFXmlElement) CurXmlTree.object;

		gTagCode = dmDDFXmlTagCode(localName);

		if (XmlElement == null)
		{
			return;
		}

		switch (gTagCode)
		{
			case XML_TAG_Node:
				CurXmlTree = CurXmlTree.parent;
				break;

			case XML_TAG_DFProperties:
			case XML_TAG_RTProperties:
				break;

			case XML_TAG_NodeName:
				if (XmlElement.name == null)
				{
					XmlElement.name = "Node";
				}
				break;

			case XML_TAG_Path:
				break;

			case XML_TAG_Value:
			case XML_TAG_DefaultValue:
				if (XmlElement.data == null)
				{
					XmlElement.data = "No Data";
				}
				break;

			case XML_TAG_Add:
				XmlElement.acl = XmlElement.acl | DmDevInfoConst.OMACL_ADD;
				break;

			case XML_TAG_Get:
				XmlElement.acl = XmlElement.acl | DmDevInfoConst.OMACL_GET;
				break;

			case XML_TAG_Replace:
				XmlElement.acl = XmlElement.acl | DmDevInfoConst.OMACL_REPLACE;
				break;

			case XML_TAG_Delete:
				XmlElement.acl = XmlElement.acl | DmDevInfoConst.OMACL_DELETE;
				break;

			case XML_TAG_Exec:
				XmlElement.acl = XmlElement.acl | DmDevInfoConst.OMACL_EXEC;
				break;

			case XML_TAG_Dynamic:
				XmlElement.scope = DmDevInfoConst.SCOPE_DYNAMIC;
				break;

			case XML_TAG_Permanent:
				XmlElement.scope = DmDevInfoConst.SCOPE_PERMANENT;
				break;

			case XML_TAG_node:
				XmlElement.format = DmDevInfoConst.FORMAT_NODE;
				break;

			case XML_TAG_chr:
				XmlElement.format = DmDevInfoConst.FORMAT_CHR;
				break;

			case XML_TAG_int:
				XmlElement.format = DmDevInfoConst.FORMAT_INT;
				break;

			case XML_TAG_xml:
				XmlElement.format = DmDevInfoConst.FORMAT_XML;
				break;

			case XML_TAG_null:
				XmlElement.format = DmDevInfoConst.FORMAT_NULL;
				break;

			case XML_TAG_bool:
				XmlElement.format = DmDevInfoConst.FORMAT_BOOL;
				break;

			case XML_TAG_bin:
				XmlElement.format = DmDevInfoConst.FORMAT_BIN;
				break;

			case XML_TAG_b64:
				XmlElement.format = DmDevInfoConst.FORMAT_B64;
				break;
			case XML_TAG_float:
				XmlElement.format = DmDevInfoConst.FORMAT_FLOAT;
				break;
			case XML_TAG_date:
				XmlElement.format = DmDevInfoConst.FORMAT_DATE;
				break;
			case XML_TAG_time:
				XmlElement.format = DmDevInfoConst.FORMAT_TIME;
				break;

			default:
				break;
		}
		gTagCode = 0;

	}

	public void characters(char ch[], int start, int length)
	{
		DDFXmlElement XmlElement = new DDFXmlElement();
		String pData;

		tsLib.debugPrint(DmDevInfoConst.DEBUG_PARSER, "characters =     " + new String(ch).substring(0, length));

		if (CurXmlTree == null)
		{
			return;
		}

		XmlElement = (DDFXmlElement) CurXmlTree.object;

		if (XmlElement == null)
		{
			return;
		}

		switch (gTagCode)
		{
			case XML_TAG_NodeName:
				pData = XmlElement.name;

				if (pData != null)
				{
					int old_len = pData.length();
					String temp = "";

					temp = pData.substring(0, old_len);
					temp = temp.concat(new String(ch).substring(old_len, length));

					pData = temp;
				}
				else
				{
					pData = "";
					pData = new String(ch).substring(0, length);
				}

				XmlElement.name = pData;
				break;

			case XML_TAG_Path:
				pData = XmlElement.path;

				if (pData != null)
				{
					int old_len = pData.length();
					String temp = "";

					temp = pData.substring(0, old_len);
					temp = temp.concat(new String(ch).substring(old_len, length));

					pData = temp;
				}
				else
				{
					pData = "";
					pData = new String(ch).substring(0, length);
				}

				XmlElement.path = pData;
				break;

			case XML_TAG_DDFName:
				pData = XmlElement.DDFName;

				if (pData != null)
				{
					int old_len = pData.length();
					String temp = "";

					temp = pData.substring(0, old_len);
					temp = temp.concat(new String(ch).substring(old_len, length));

					pData = temp;
				}
				else
				{
					pData = "";
					pData = new String(ch).substring(0, length);
				}

				XmlElement.DDFName = pData;
				break;

			case XML_TAG_MIME:
				pData = XmlElement.MIME;

				if (pData != null)
				{
					int old_len = pData.length();
					String temp = "";

					temp = pData.substring(0, old_len);
					temp = temp.concat(new String(ch).substring(old_len, length));

					pData = temp;
				}
				else
				{
					pData = "";
					pData = new String(ch).substring(0, length);
				}

				XmlElement.MIME = pData;
				break;

			case XML_TAG_Type:
				pData = XmlElement.type;

				if (pData != null)
				{
					int old_len = pData.length();
					String temp = "";

					temp = pData.substring(0, old_len);
					temp = temp.concat(new String(ch).substring(old_len, length));

					pData = temp;
				}
				else
				{
					pData = "";
					pData = new String(ch).substring(0, length);
				}

				XmlElement.type = pData;
				break;

			case XML_TAG_DefaultValue:
			case XML_TAG_Value:
				pData = XmlElement.data;

				if (pData != null)
				{
					int old_len = pData.length();
					String temp = "";

					temp = pData.substring(0, old_len);
					temp = temp.concat(new String(ch).substring(old_len, length));

					pData = temp;
				}
				else
				{
					pData = "";
					pData = new String(ch).substring(0, length);
				}

				XmlElement.data = pData;
				break;

			default:
				break;
		}
	}

	public static int dmDDFXmlTagCode(String name)
	{
		int i;

		for (i = 0; i < gSdmXmlOmaTags.length; i++)
		{
			if (gSdmXmlOmaTags[i].equals(name))
			{
				return i;
			}
		}

		return 0;
	}

	public static int dmDDFParsing(XMLStream stream, DM_Tree xmltree)
	{
		int wRC = XML_ERR_OK;

		if (stream == null || stream.data == null || xmltree == null)
		{
			wRC = XML_ERR_INVALID_PARAM;
			return wRC;
		}

		CurXmlTree = xmltree;

		ddfParser handler = new ddfParser();
		ddfXmlParser parser = new ddfXmlParser(handler, stream.data);

		return wRC;

	}

	public static boolean dmDDFCreateSyncMLNode()
	{
		boolean ret = false;

		XMLStream xmlstream = new XMLStream();
		DM_Tree xmltree = new DM_Tree();

		xmlstream.data = dmDDFGetFileId(DDF_ID_SYNCML);
		xmlstream.size = dmDDFGetFileSize(DDF_ID_SYNCML);

		if (dmDDFParsing(xmlstream, xmltree) != XML_ERR_OK)
		{
			tsLib.debugPrintException(DmDevInfoConst.DEBUG_EXCEPTION, "Parsing Fail");
			return ret;
		}

		ret = dmDDFXmlTagParsing(xmltree);

		if (!ret)
		{
			return ret;
		}

		ret = true;
		tsLib.debugPrint(DmDevInfoConst.DEBUG_PARSER, "Success.");

		return ret;
	}

	public static String dmDDFGetFileId(int Fileid)
	{
		String inputfilename;
		String ddfbuf = "";

		switch (Fileid)
		{
			case DDF_ID_SYNCML:
				inputfilename = "data/data/com.tsdm.ddf/SYNCML_DM_DDF.xml";

				FileInputStream fis = null;
				try
				{
					fis = new FileInputStream(inputfilename);
					byte[] buf = new byte[fis.available()];
					int ret = fis.read(buf);
					if(ret != -1)
						ddfbuf = new String(buf);
				}
				catch (Exception e)
				{
					tsLib.debugPrintException(DmDevInfoConst.DEBUG_EXCEPTION, e.toString());
				}
				finally
				{
					try
					{
						if (fis != null)
						{
							fis.close();
						}
					}
					catch (IOException e)
					{
						tsLib.debugPrintException(DmDevInfoConst.DEBUG_EXCEPTION, e.toString());
					}
				}

				break;

		}

		return ddfbuf;
	}

	public static int dmDDFGetFileSize(int Fileid)
	{
		String inputfilename;
		int nLen = 0;

		switch (Fileid)
		{
			case DDF_ID_SYNCML:
				inputfilename = "data/data/com.tsdm.ddf/SYNCML_DM_DDF.xml";
				FileInputStream fis = null;
				try
				{
					fis = new FileInputStream(inputfilename);
					nLen = fis.available();
				}
				catch (Exception e)
				{
					tsLib.debugPrintException(DmDevInfoConst.DEBUG_EXCEPTION, e.toString());
				}
				finally
				{
					try
					{
						if (fis != null)
						{
							fis.close();
						}
					}
					catch (IOException e)
					{
						tsLib.debugPrintException(DmDevInfoConst.DEBUG_EXCEPTION, e.toString());
					}
				}
				break;
		}

		return nLen;
	}

	public static boolean dmDDFXmlTagParsing(DM_Tree pTree)
	{
		if (pTree == null)
		{
			return false;
		}
		DDFXmlElement XmlElement = null;
		tsLinkedList childlist = pTree.childlist;
		int nTagCode;
		boolean ret = true;
		String pPath = "";
		String pPathTemp = "";

		while (pTree != null)
		{
			XmlElement = (DDFXmlElement) pTree.object;

			if (XmlElement.tag != null)
			{
				tsLib.debugPrint(DmDevInfoConst.DEBUG_PARSER, "Tag:");
				tsLib.debugPrint(DmDevInfoConst.DEBUG_PARSER, XmlElement.tag);
			}

			if (XmlElement.name != null)
			{
				tsLib.debugPrint(DmDevInfoConst.DEBUG_PARSER, "Name:");
				tsLib.debugPrint(DmDevInfoConst.DEBUG_PARSER, XmlElement.name);
			}

			if (XmlElement.path != null)
			{
				tsLib.debugPrint(DmDevInfoConst.DEBUG_PARSER, "Path:");
				tsLib.debugPrint(DmDevInfoConst.DEBUG_PARSER, XmlElement.path);
			}

			if (XmlElement.data != null)
			{
				tsLib.debugPrint(DmDevInfoConst.DEBUG_PARSER, "Data:");
				tsLib.debugPrint(DmDevInfoConst.DEBUG_PARSER, XmlElement.data);
			}

			if (XmlElement.Size != null)
			{
				tsLib.debugPrint(DmDevInfoConst.DEBUG_PARSER, "Size:");
				tsLib.debugPrint(DmDevInfoConst.DEBUG_PARSER, XmlElement.Size);
			}

			nTagCode = dmDDFXmlTagCode(XmlElement.tag);
			// root node.
			if (pTree.parent == null)
			{
				pPath = null;

				if (XmlElement.path != null)
				{
					pPath = XmlElement.path;
				}
				else
				{
					tsLib.debugPrint(DmDevInfoConst.DEBUG_PARSER, "Root Path is NULL.");
				}

				if (XmlElement.type != null)
				{
					pPath = dmDDFCheckInbox(XmlElement.type);
					if (pPath != null)
					{
						XmlElement.path = null;
						XmlElement.path = pPath;
					}
				}
			}

			dmAgentVerifyNewAccount(g_om, XmlElement.path, XmlElement.name);

			switch (nTagCode)
			{
				case XML_TAG_Node:
					dmDDFPrintNodePropert(XmlElement);
					ret = dmDDFCreateNodeToOM(XmlElement);

					if (!ret)
					{
						pPath = null;
						return ret;
					}

					// Defects
					if (XmlElement.path != null && !XmlElement.path.equals("/") && !XmlElement.path.equals("./"))
					{
						pPath = "/";
					}

					if (pPath != null)
					{
						if(XmlElement.name != null)
						{
							pPath = pPath.concat(XmlElement.name);
						}
						dmDDFCreateNode(pTree, pPath);

						pPathTemp = tsLib.libStrrchr(pPath, '/');

						if (pPathTemp != null)
						{
							pPath = pPathTemp;
						}
					}
					break;

				case XML_TAG_MgmtTree:
				case XML_TAG_VerDTD:
				case XML_TAG_Man:
				case XML_TAG_Mod:
					break;

				default:
					tsLib.debugPrintException(DmDevInfoConst.DEBUG_EXCEPTION, String.valueOf(nTagCode));
					tsLib.debugPrintException(DmDevInfoConst.DEBUG_EXCEPTION, gSdmXmlOmaTags[nTagCode]);
					break;
			}
			pTree = (DM_Tree) tsLinkedList.listGetNextObj(childlist);

		}

		return ret;
	}

	public static String dmDDFCheckInbox(String pType)
	{
		String pId = null;
		int i = 0;
		String pPath = null;
		String out = null;

		if (pType == null)
		{
			return null;
		}

		for (i = 1; i < DM_MO_ID_END; i++)
		{
			pId = dmDDFGetMOType(i);
			if (pId == null)
			{
				continue;
			}

			if (pType.equals(pId))
			{
				pPath = dmDDFGetMOPath(i);
				if (pPath != null)
				{
					out = null;
					out = pPath;
					break;
				}
				else
				{
					out = null;
					break;
				}
			}
		}

		return out;
	}

	public static String dmDDFGetMOPath(int nId)
	{

		if (nId <= DM_MO_ID_NONE || nId >= DM_MO_ID_END)
		{
			tsLib.debugPrintException(DmDevInfoConst.DEBUG_EXCEPTION, "wrong nId. [" + nId + "]");
			return null;
		}
		return gSdmManagementObjectIdPath[nId];
	}

	public static String dmDDFGetMOType(int nId)
	{

		if (nId <= DM_MO_ID_NONE || nId >= DM_MO_ID_END)
		{
			tsLib.debugPrintException(DmDevInfoConst.DEBUG_EXCEPTION, "wrong nId. [" + nId + "]");
			return null;
		}

		return gSdmManagementObjectIdType[nId];
	}

	public static boolean dmAgentVerifyNewAccount(tsOmTree omt, String pPath, String pAccName)
	{
		String pTmpAccPath = "";
		tsDmVnode node = null;
		boolean bRet = false;

		if (pPath != null && pPath.equals(DmDevInfoConst.BASE_ACCOUNT_PATH))
		{
			pTmpAccPath = pPath;
			pTmpAccPath = pTmpAccPath.concat("/");
			if (pAccName != null)
				pTmpAccPath = pTmpAccPath.concat(pAccName);

			node = tsOmlib.dmOmLibGetNodeProp(omt, pTmpAccPath);
			if (node == null)
			{
				gNewAccPath = pTmpAccPath;
				bRet = true;
			}
			else
			{
				gNewAccPath = null;
				pTmpAccPath = null;
				bRet = false;
			}
		}

		return bRet;
	}

	public static void dmDDFPrintNodePropert(DDFXmlElement element)
	{
		String printData = "";

		if (element.path != null)
		{
			printData = printData.concat(element.path);
		}

		if (element.name != null)
		{
			if (DmDevInfoConst.DEFAULT_BIG_BUFFER_SIZE <= printData.length() + element.name.length())
			{
				tsLib.debugPrint(DmDevInfoConst.DEBUG_PARSER, "Buffer Overflow. Increase the space. for element->name.");
				printData = null;
				return;
			}
			printData = printData.concat("/");
			printData = printData.concat(element.name);
		}

		if (element.data != null)
		{
			if (DmDevInfoConst.DEFAULT_BIG_BUFFER_SIZE <= printData.length() + element.data.length())
			{
				tsLib.debugPrint(DmDevInfoConst.DEBUG_PARSER, "Buffer Overflow. Increase the space. for element->data.");
				printData = null;
				return;
			}
			printData = printData.concat(" [");
			printData = printData.concat(element.data);
		}

		if (element.type != null)
		{
			if (DmDevInfoConst.DEFAULT_BIG_BUFFER_SIZE <= printData.length() + element.type.length())
			{
				tsLib.debugPrint(DmDevInfoConst.DEBUG_PARSER, "Buffer Overflow. Increase the space. element->type.");
				printData = null;
				return;
			}
			printData = printData.concat("] [");
			printData = printData.concat(element.type);
		}
		if (printData != null)
			tsLib.debugPrint(DmDevInfoConst.DEBUG_PARSER, "[" + printData + "] [" + element.acl + "][" + element.format + "][" + element.scope + "].");

		printData = null;
	}

	public static boolean dmDDFCreateNodeToOM(DDFXmlElement element)
	{
		int scope = element.scope;
		int format = element.format;
		int aclValue = element.acl;
		String pData = element.data;
		int nLen = 0;
		String nodename = "";
		boolean bRet = false;
		String pTmpBuf = "";

		if (element.name == null)
		{
			tsLib.debugPrintException(DmDevInfoConst.DEBUG_EXCEPTION, "Not exist nodename.");
			return false;
		}

		if (DmDevInfoConst._SYNCML_TS_DM_VERSION_V12_)
		{
			if (tsLib.libStrstr(element.path, ".") == null)
			{
				bRet = false;
			}
			else
			{
				bRet = true;
			}

			if (!bRet)
			{
				if (element.path != null)
				{
					pTmpBuf = pTmpBuf.concat(".");
					pTmpBuf = pTmpBuf.concat(element.path);
					element.path = null;

					element.path = pTmpBuf;
				}
				else
				{
					element.path = ".";
				}
			}

			dmAgent.dmAgentSetXNodePath(element.path, element.name, true);

			if (element.name.equals("AAuthData"))
			{
				format = DmDevInfoConst.FORMAT_B64;
			}
		}

		nodename = element.path;
		nodename = nodename.concat("/");
		nodename = nodename.concat(element.name);

		if (aclValue == 0x00)
		{
			aclValue = DmDevInfoConst.OMACL_ADD | DmDevInfoConst.OMACL_DELETE | DmDevInfoConst.OMACL_GET | DmDevInfoConst.OMACL_REPLACE;
		}

		if (scope == DmDevInfoConst.SCOPE_NONE)
		{
			scope = DmDevInfoConst.SCOPE_DYNAMIC;
		}

		if (format == DmDevInfoConst.FORMAT_NONE)
		{
			format = DmDevInfoConst.FORMAT_NODE;
		}

		if (format == DmDevInfoConst.FORMAT_NODE || format == DmDevInfoConst.FORMAT_NULL || format == DmDevInfoConst.FORMAT_NONE)
		{
			dmDDFSetOMTree(g_om, nodename, pData, nLen, element.type, aclValue, scope, format);
			OMSETPATH(g_om, nodename, aclValue, scope);
		}
		else
		{
			if (format == DmDevInfoConst.FORMAT_BIN)
			{
				nLen = 0;
			}
			else
			{
				if (pData == null)
				{
					pData = "null";
					element.data = pData;
				}
				nLen = pData.length();
			}
			if (nodename.length() < 0 || nodename.length() >= DmDevInfoConst.MAX_NODENAME_SIZE)
			{
				tsLib.debugPrintException(DmDevInfoConst.DEBUG_EXCEPTION, "nodename Size[" + nodename.length() + "]. Fatal ERROR.");
				return false;
			}
			dmDDFSetOMTree(g_om, nodename, pData, nLen, element.type, aclValue, scope, format);
		}

		nodename = null;

		return true;
	}

	public static void dmDDFSetOMTree(tsOmTree omt, String path, String pData, int nLen, String pMime, int aclValue, int scope, int format)
	{
		tsDmVnode node = null;

		node = tsOmlib.dmOmLibGetNodeProp(omt, path);

		dmAgent.dmAgentSetSyncMode(DmDevInfoConst.DM_SYNC_BOOTSTRAP); // test code

		if (node == null || dmAgent.dmAgentGetSyncMode() == DmDevInfoConst.DM_SYNC_BOOTSTRAP)
		{
			dmDDFSetOMTreeProperty(omt, path, pData, nLen, pMime, format);
			dmAgent.dmAgentMakeDefaultAcl(omt, path, aclValue, scope);
		}

		if (bNodeChangeMode)
		{
			tsLib.debugPrint(DmDevInfoConst.DEBUG_PARSER, "bNodeChangeMode Change Node.");
			dmDDFSetOMTreeProperty(omt, path, pData, nLen, pMime, format);
			dmAgent.dmAgentMakeDefaultAcl(omt, path, aclValue, scope);
		}
	}

	public static void dmDDFSetOMTreeProperty(tsOmTree omt, String path, String pData, int nLen, String pMime, int format)
	{
		tsDmVnode node;
		tsOmList list;
		long nSize;
		char[] tmpbuf = new char[(int) DmDevInfoConst.MAX_NODENAME_SIZE];
		int index = 0;

		tsOmlib.dmOmMakeParentPath(path, tmpbuf);

		String tmp = "";

		while (tmpbuf[index] != '\0')
		{
			tmp = tmp.concat(String.valueOf(tmpbuf[index]));
			index++;
		}

		node = tsOmlib.dmOmLibGetNodeProp(omt, tmp);

		if (node == null)
		{
			tsOmlib.dmOmProcessCmdImplicitAdd(omt, tmp, DmDevInfoConst.OMACL_ADD | DmDevInfoConst.OMACL_DELETE | DmDevInfoConst.OMACL_GET | DmDevInfoConst.OMACL_REPLACE, 1);
		}

		if (format == DmDevInfoConst.FORMAT_NODE || format == DmDevInfoConst.FORMAT_NULL || format == DmDevInfoConst.FORMAT_NONE)
		{
			tsOmlib.dmOmWrite(omt, path, 0, 0, "", 0);
		}
		else
		{
			nSize = tsOmlib.dmOmWrite(omt, path, nLen, 0, pData, nLen);
			if (nSize <= 0)
			{
				tsLib.debugPrint(DmDevInfoConst.DEBUG_PARSER, "Size[" + nSize + "]");
			}
		}

		node = tsOmlib.dmOmLibGetNodeProp(omt, path);
		if (node != null)
		{
			if (node.type != null)
			{
				tsOmlib.dmOmDeleteMimeList(node.type);
			}

			list = new tsOmList();

			if (pMime != null)
			{
				list.data = pMime;
			}
			else
			{
				list.data = "text/plain";
			}
			list.data = null;
			node.type = list;
			node.format = format;
		}
	}

	public static boolean dmDDFCreateNode(DM_Tree pTree, String pPath)
	{
		if (pTree == null)
		{ // Defects
			return false;
		}
		DDFXmlElement XmlElement = null;
		tsLinkedList childlist = pTree.childlist;
		int nTagCode;
		String pPathTemp = null;
		boolean ret;

		if (childlist == null)
		{
			return true;
		}

		tsLinkedList.listSetCurrentObj(childlist, 0);

		while (pTree != null)
		{
			pTree = (DM_Tree) tsLinkedList.listGetNextObj(childlist);

			if (pTree == null)
			{
				return true;
			}

			XmlElement = (DDFXmlElement) pTree.object;

			nTagCode = dmDDFXmlTagCode(XmlElement.tag);
			switch (nTagCode)
			{
				case XML_TAG_Node:
					XmlElement.path = null;
					XmlElement.path = pPath;
					ret = dmDDFCreateNodeToOM(XmlElement);
					if (!ret)
					{
						return ret;
					}

					pPath = pPath.concat("/");
					pPath = pPath.concat(XmlElement.name);
					dmDDFCreateNode(pTree, pPath);
					// DM 1.2
					pPathTemp = tsLib.libStrrchr(pPath, '/');
					pPath = pPathTemp;
					break;

				default:
					tsLib.debugPrintException(DmDevInfoConst.DEBUG_EXCEPTION, String.valueOf(nTagCode));
					tsLib.debugPrintException(DmDevInfoConst.DEBUG_EXCEPTION, gSdmXmlOmaTags[nTagCode]);
					break;
			}
		}

		return true;
	}

	public static int dmDDFCreateTNDSNodeFromFile(int nFileID, tsOmTree omt)
	{
		int ret = 0;
		boolean r = false;
		int nLen = 0;
		byte[] pData = null;
		String Data = null;

		nLen = tsdmDB.dmdbGetFileSize(nFileID);
		if (nLen <= 0)
		{
			return 0;
		}
		pData = new byte[nLen];
		r = tsdmDB.dmReadFile(nFileID, 0, nLen, pData);

		if (!r)
		{
			return 0;
		}
		Data = new String(pData);
		ret = dmDDFCreateTNDSNode(Data, Data.length(), omt);

		return ret;

	}

	public static int dmDDFCreateTNDSNode(String pData, int nLen, tsOmTree omt)
	{
		boolean ret = false;
		int totalSize = 0;
		String data;
		XMLStream xmlstream = new XMLStream();
		DM_Tree xmltree = new DM_Tree();

		data = dmDDFParseCDATA(pData, nLen, totalSize);

		if (data != null)
		{
			xmlstream.data = dmDDFParseCDATA(pData, nLen, totalSize);
			xmlstream.size = totalSize;
		}
		else
		{
			xmlstream.data = pData;
			xmlstream.size = nLen;
		}

		if (dmDDFParsing(xmlstream, xmltree) != XML_ERR_OK)
		{
			tsLib.debugPrintException(DmDevInfoConst.DEBUG_EXCEPTION, "Parsing Fail.");
			g_om = null;
			return 0;
		}

		if (omt == null)
		{
			tsLib.debugPrintException(DmDevInfoConst.DEBUG_EXCEPTION, "OMT is NULL.");
			g_om = null;
			return 0;
		}

		g_om = omt;

		xmltree = XmlTree;

		ret = dmDDFXmlTagParsing(xmltree);

		if (!ret)
		{
			tsLib.debugPrintException(DmDevInfoConst.DEBUG_EXCEPTION, "Check the xml file.");
			g_om = null;
			return 0;
		}

		ret = true;
		tsLib.debugPrint(DmDevInfoConst.DEBUG_PARSER, "Success.");

		return 1;
	}

	public static String dmDDFParseCDATA(String pData, int nLen, int nSize)
	{
		String cData;
		String pStartStr;
		String pEndStr;
		int cDataLen = 0;
		String end_str = gSdmXmlTagString[XML_TAG_CDATA_String_End];
		String start_str = gSdmXmlTagString[XML_TAG_CDATA_String_Start];

		if (pData == null)
		{
			return null;
		}

		if (tsLib.libStrncmp(pData, start_str, start_str.length()) != 0)
		{
			return null;
		}

		pEndStr = tsLib.libStrstr(pData, end_str);

		if (pEndStr == null)
		{
			return null;
		}

		pStartStr = pData.substring(start_str.length());
		cDataLen = pStartStr.indexOf(end_str);

		if (cDataLen <= 0)
		{
			return null;
		}

		cData = pStartStr.substring(0, cDataLen);

		//nSize = cData.length();

		return cData;
	}

	public static String TndsWbxmlParse(String pInData, int nInSize)
	{
		int nSize;
		String pData = null;
		byte[] wbxmlData;
		boolean bSyncMLTag = false;
		String pOutData = null;

		dmDDFTNDSInitParse(pInData, nInSize);

		nSize = dmDDFTNDSGetWbxmlSize();
		pData = dmDDFTNDSGetWbxmlData();

		// Defects
		while (nSize != 0 && pData != null)
		{
			wbxmlData = pData.getBytes();

			switch (wbxmlData[0])
			{
				case DM_WBXML_CODEPAGE:
				{
					dmDDFTNDSParsingCodePage();
				}
					break;

				case DM_WBXML_VERSION:
				{
					dmDDFTNDSParsingWbxmlHeader();
				}
					break;

				case DM_XML_SYNCML_HEX_VALUE:
				{
					bSyncMLTag = true;
					dmDDFTNDSParsingSyncMLTag();
				}
					break;

				case DM_TNDS_MGMTTREE:
				{
					dmDDFTNDSParsingMgmtTreeTag();
					dmDDFTNDSUderMgmtTreeTagParse();
				}
					break;

				default:
				{
					tsLib.debugPrintException(DmDevInfoConst.DEBUG_EXCEPTION, String.valueOf(nSize));
					tsLib.debugPrintException(DmDevInfoConst.DEBUG_EXCEPTION, String.valueOf(wbxmlData));
					return null;
				}
					// break;

			}

			nSize = dmDDFTNDSGetWbxmlSize();
			pData = (nSize != 0) ? dmDDFTNDSGetWbxmlData() : null;
		}

		if (!bSyncMLTag)
		{
			dmDDFTNDSAppendSyncMLCloseTag();
		}

		pOutData = dmDDFTNDSGetXMLData();

		return pOutData;
	}

	public static void dmDDFTNDSInitParse(String pInData, int nInSize)
	{
		gTndsData = new DMTndsData();

		dmDDFTNDSSetWbxmlSize(nInSize);
		dmDDFTNDSSetWbxmlData(pInData);
		dmDDFTNDSAllocXMLData(nInSize);

		dmDDFTNDSAppendNameSpace();

		gstTagManage = new DMTndsTagManage();

	}

	public static void dmDDFTNDSSetWbxmlSize(int nSize)
	{
		gTndsData.nWbxmlDataSize = nSize;
	}

	public static void dmDDFTNDSSetWbxmlData(String pData)
	{
		gTndsData.pWbxmlData = pData;
	}

	public static void dmDDFTNDSSetXMLSize(int nSize)
	{
		gTndsData.nXMLDataSize = nSize;
	}

	public static void dmDDFTNDSSetXMLData(String pData)
	{
		gTndsData.pXMLData = pData;
	}

	public static void dmDDFTNDSSetXMLDataStart(String pData)
	{
		gTndsData.pXMLDataStart = pData;
	}

	public static void dmDDFTNDSSetWbxmlDataStart(String pData)
	{
		gTndsData.pWbxmlDataStart = pData;
	}

	public static int dmDDFTNDSGetWbxmlSize()
	{
		return gTndsData.nWbxmlDataSize;
	}

	public static String dmDDFTNDSGetWbxmlData()
	{
		return gTndsData.pWbxmlData;
	}

	public static int dmDDFTNDSGetXMLSize()
	{
		return gTndsData.nXMLDataSize;
	}

	public static String dmDDFTNDSGetXMLData()
	{
		return gTndsData.pXMLData;
	}

	public static String dmDDFTNDSGetXMLDataStart()
	{
		return gTndsData.pXMLDataStart;
	}

	public static String dmDDFTNDSGetWbxmlDataStart()
	{
		return gTndsData.pWbxmlDataStart;
	}

	public static boolean dmDDFTNDSAllocXMLData(int nSize)
	{
		String pXMLBuf = "";

		dmDDFTNDSCheckMem(pXMLBuf);
		//if (!dmDDFTNDSCheckMem(pXMLBuf))
		//{
		//	tsLib.debugPrintException(DmDevInfoConst.DEBUG_EXCEPTION, "Alloc Error !!! ###");
		//	return false;
		//}

		dmDDFTNDSSetXMLData(pXMLBuf);

		return true;
	}

	public static boolean dmDDFTNDSCheckMem(Object ptr)
	{
		return (ptr != null) ? true : false;
	}

	public static void dmDDFTNDSAppendNameSpace()
	{
		String pXmlData;
		int nXmlSize;
		int nTmpLen;

		pXmlData = dmDDFTNDSGetXMLData();
		nXmlSize = dmDDFTNDSGetXMLSize();

		pXmlData = pXmlData.concat(DM_XML_VERSION_STRING);
		pXmlData = pXmlData.concat(DM_XML_NAME_SPACE_STRING);

		nTmpLen = pXmlData.length();

		dmDDFTNDSSetXMLData(pXmlData);
		dmDDFTNDSSetXMLSize(nXmlSize + nTmpLen);
	}

	public static boolean dmDDFTNDSParsingCodePage()
	{
		int nWbxmlSize = 0;
		int index = 0;
		String pWbxmlData = dmDDFTNDSGetWbxmlData();

		// 1. wbxml TNDS CodePage : 00 02 - 2 byte
		pWbxmlData = pWbxmlData.substring(index + 1);
		++nWbxmlSize;

		if (pWbxmlData.charAt(index) == 0x02)
		{
			// tnds
		}
		else if (pWbxmlData.charAt(index) == 0x00)
		{
			// syncml
		}
		else
		{
			tsLib.debugPrintException(DmDevInfoConst.DEBUG_EXCEPTION, "TNDS Tag Right ###");
			return false;
		}

		pWbxmlData = pWbxmlData.substring(index + 1);
		++nWbxmlSize;

		dmDDFTNDSSetWbxmlData(pWbxmlData);
		dmDDFTNDSSetWbxmlSize(dmDDFTNDSGetWbxmlSize() - nWbxmlSize);
		return true;
	}

	public static void dmDDFTNDSParsingWbxmlHeader()
	{
		int nWbxmlHeaderSize = 0;
		int nStringTableSize = 0;
		String pWbxmlHeaderData = dmDDFTNDSGetWbxmlData();

		// 1. wbxml version - 1byte
		pWbxmlHeaderData = pWbxmlHeaderData.substring(1);
		++nWbxmlHeaderSize;

		// 2. public ID - 2 byte
		pWbxmlHeaderData = pWbxmlHeaderData.substring(2);
		nWbxmlHeaderSize += 2;

		// 3. char Set - 1 byte
		pWbxmlHeaderData = pWbxmlHeaderData.substring(1);
		++nWbxmlHeaderSize;

		// 4. String Table Size - 1byte
		nStringTableSize = (int) pWbxmlHeaderData.charAt(0);
		pWbxmlHeaderData = pWbxmlHeaderData.substring(1);
		++nWbxmlHeaderSize;

		// 5. String Tabel - nStringTableSize
		pWbxmlHeaderData = pWbxmlHeaderData.substring(nStringTableSize);
		nWbxmlHeaderSize += nStringTableSize;

		dmDDFTNDSSetWbxmlData(pWbxmlHeaderData);
		dmDDFTNDSSetWbxmlSize(dmDDFTNDSGetWbxmlSize() - nWbxmlHeaderSize);
	}

	public static void dmDDFTNDSParsingSyncMLTag()
	{
		String pWbxmlData = null;
		int nWbxmlSize;
		int nTag;

		// Get wbxml, xml buf info
		nWbxmlSize = dmDDFTNDSGetWbxmlSize();
		pWbxmlData = dmDDFTNDSGetWbxmlData();

		nTag = DM_TNDS_SYNCML_SPECIAL_VALUE;

		// Make open tag <abc>
		dmDDFTNDSManagePushTag(nTag);

		// Wbxml Buf Process (move to wbxml buf pointer)
		pWbxmlData = pWbxmlData.substring(1);
		--nWbxmlSize;
		dmDDFTNDSSetWbxmlSize(nWbxmlSize);
		dmDDFTNDSSetWbxmlData(pWbxmlData);
	}

	public static boolean dmDDFTNDSManagePushTag(int eTokenIndex)
	{
		if (gstTagManage.nTagSP == DM_TNDS_INCLUDE_TAG_MAX_NUM)
		{
			tsLib.debugPrintException(DmDevInfoConst.DEBUG_EXCEPTION, "TagSP FULL !!! ###");
			return false;
		}

		gstTagManage.eTagID[gstTagManage.nTagSP] = eTokenIndex;
		++gstTagManage.nTagSP;

		return true;
	}

	public static void dmDDFTNDSParsingMgmtTreeTag()
	{
		dmDDFTNDSParsingOpenTag();
	}

	public static void dmDDFTNDSParsingVerdtdTag()
	{
		dmDDFTNDSParsingOpenTag();
	}

	public static void dmDDFTNDSParsingNodeTag()
	{
		dmDDFTNDSParsingOpenTag();
	}

	public static void dmDDFTNDSParsingNodeNameTag()
	{
		dmDDFTNDSParsingOpenTag();
	}

	public static void dmDDFTNDSParsingRTPropertiesTag()
	{
		dmDDFTNDSParsingOpenTag();
	}

	public static void dmDDFTNDSParsingFormatTag()
	{
		dmDDFTNDSParsingOpenTag();
		dmDDFTNDSParsingFormatChildElement();
	}

	public static void dmDDFTNDSParsingTypeTag()
	{
		dmDDFTNDSParsingOpenTag();
	}

	public static void dmDDFTNDSParsingDDFNameTag()
	{
		dmDDFTNDSParsingOpenTag();
	}

	public static void dmDDFTNDSParsingValueTag()
	{
		dmDDFTNDSParsingOpenTag();
	}

	public static void dmDDFTNDSParsingMIMETag()
	{
		dmDDFTNDSParsingOpenTag();
	}

	public static void dmDDFTNDSParsingAccessTypeTag()
	{
		dmDDFTNDSParsingOpenTag();
		dmDDFTNDSParsingAccessTypeChildElement();
	}

	public static void dmDDFTNDSParsingPathTag()
	{
		dmDDFTNDSParsingOpenTag();
	}

	public static void dmDDFTNDSParsingACLTag()
	{
		dmDDFTNDSParsingOpenTag();
	}

	public static void dmDDFTNDSParsingOpenTag()
	{
		String pWbxmlData = null;
		int nWbxmlSize;
		String pXmlData = null;
		int nXmlSize;

		int nTagLen = 0;
		String szTagString = null;
		int nTag;

		// Get wbxml, xml buf info
		nWbxmlSize = dmDDFTNDSGetWbxmlSize();
		pWbxmlData = dmDDFTNDSGetWbxmlData();
		nTag = (int) pWbxmlData.charAt(0);
		pXmlData = dmDDFTNDSGetXMLData();
		nXmlSize = dmDDFTNDSGetXMLSize();

		// Make open tag <abc>
		dmDDFTNDSManagePushTag(nTag);
		szTagString = dmDDFTNDSMakeOpenTagString(nTag);
		if (szTagString != null)
		{ // Defects
			nTagLen = szTagString.length();
			// XML Buf Process (copy to xml buf)
			pXmlData = pXmlData.concat(szTagString);
		}
		dmDDFTNDSSetXMLSize(nXmlSize + nTagLen);
		dmDDFTNDSSetXMLData(pXmlData);

		// Wbxml Buf Process (move to wbxml buf pointer)
		pWbxmlData = pWbxmlData.substring(1); /* move 1 byte */
		--nWbxmlSize;
		dmDDFTNDSSetWbxmlSize(nWbxmlSize);
		dmDDFTNDSSetWbxmlData(pWbxmlData);

		szTagString = null;
	}

	public static String dmDDFTNDSMakeOpenTagString(int eTokenIndex)
	{
		String pOpenTagName = "";

		dmDDFTNDSCheckMem(pOpenTagName);
		//if (!dmDDFTNDSCheckMem(pOpenTagName))
		//{
		//	tsLib.debugPrintException(DmDevInfoConst.DEBUG_EXCEPTION, " Alloc Error !!! ###");
		//	return null;
		//}

		pOpenTagName = "<";
		pOpenTagName = pOpenTagName.concat(dmDDFTNDSGetTagString(eTokenIndex));
		pOpenTagName = pOpenTagName.concat(">");

		return pOpenTagName;
	}

	public static String dmDDFTNDSGetTagString(int eTokenIndex)
	{
		int nIndex = 0;

		if (eTokenIndex < DM_TNDS_MAX)
		{
			nIndex = eTokenIndex - DM_TNDS_ACCESSTYPE; /* Match wbxml Tag 'enum == string' */
			return gszTndsTokenStr[nIndex];
		}
		else if (eTokenIndex == DM_TNDS_SYNCML_SPECIAL_VALUE)
		{
			return DM_XML_SYNCML_STRING;
		}
		else
		{
			// error
			return "NULL";
		}
	}

	public static void dmDDFTNDSParsingFormatChildElement()
	{
		String pWbxmlData = null;
		int nWbxmlSize;
		String pXmlData = null;
		int nXmlSize;

		int nTag;
		int nTagLen;
		String szTypeBuf = ""; // Defects

		// Get wbxml, xml buf info
		nWbxmlSize = dmDDFTNDSGetWbxmlSize();
		pWbxmlData = dmDDFTNDSGetWbxmlData();
		nTag = (int) pWbxmlData.charAt(0);
		pXmlData = dmDDFTNDSGetXMLData();
		nXmlSize = dmDDFTNDSGetXMLSize();

		nTag = nTag + 0x40;

		switch (nTag)
		{
			/* <Format>,<DFFormat> child element : <node/>, <chr/>, <bool/> ... */
			case DM_TNDS_NODE_FORAMT:
			case DM_TNDS_CHR_FORMAT:
			case DM_TNDS_BOOL_FORMAT:
			case DM_TNDS_B64_FORMAT:
			case DM_TNDS_BIN_FORMAT:
			case DM_TNDS_DATE_FORMAT:
			case DM_TNDS_FLOAT_FORMAT:
			case DM_TNDS_INT_FORAMT:
			case DM_TNDS_NULL_FORMAT:
			case DM_TNDS_TIME_FORMAT:
			case DM_TNDS_XML_FORMAT:

				/* <Occurrence> child element 1 : <ZerOorMore>, <ZeroOrOne> ... */
			case DM_TNDS_ZEROORMORE:
			case DM_TNDS_ZEROORONE:
			case DM_TNDS_ONE:
			case DM_TNDS_ONEORMORE:
			{
				szTypeBuf = "<";
				szTypeBuf = szTypeBuf.concat(dmDDFTNDSGetTagString(nTag));
				szTypeBuf = szTypeBuf.concat("/>");
			}
				break;

			/* <Occurrence> child element 2 : <OneOrN>, <ZeroOrN> ... */
			case DM_TNDS_ONEORN:
			case DM_TNDS_ZEROORN:
			{
			}
				break;

			default:
			{
				tsLib.debugPrintException(DmDevInfoConst.DEBUG_EXCEPTION, " child tag vlaue is " + nTag + "  ###");
			}
				break;
		}

		// XML Buf Process (copy to xml buf)
		nTagLen = szTypeBuf.length();
		pXmlData = pXmlData.concat(szTypeBuf);
		dmDDFTNDSSetXMLSize(nXmlSize + nTagLen);
		dmDDFTNDSSetXMLData(pXmlData);

		// Wbxml Buf Process (move to wbxml buf pointer)
		pWbxmlData = pWbxmlData.substring(1); /* move 1 byte */
		--nWbxmlSize;
		dmDDFTNDSSetWbxmlSize(nWbxmlSize);
		dmDDFTNDSSetWbxmlData(pWbxmlData);
	}

	public static void dmDDFTNDSParsingAccessTypeChildElement()
	{
		String pWbxmlData = null;
		int nWbxmlSize;
		String pXmlData = null;
		int nXmlSize;

		int nTag;
		int nTagLen;
		String szTagString = null;
		String szTypeBuf = null;

		// Get wbxml, xml buf info
		nWbxmlSize = dmDDFTNDSGetWbxmlSize();
		pWbxmlData = dmDDFTNDSGetWbxmlData();
		nTag = (int) pWbxmlData.charAt(0);
		pXmlData = dmDDFTNDSGetXMLData();
		nXmlSize = dmDDFTNDSGetXMLSize();

		nTag = nTag - 0x40; /* tnds tag */

		switch (nTag)
		{
			case DM_TNDS_ADD:
			case DM_TNDS_COPY:
			case DM_TNDS_DELETE:
			case DM_TNDS_EXEC:
			case DM_TNDS_GET:
			case DM_TNDS_REPLACE:
			{
				szTypeBuf = "<";
				szTypeBuf = szTypeBuf.concat(dmDDFTNDSGetTagString(nTag));
				szTypeBuf = szTypeBuf.concat("/>");
			}
				break;

			default:
				return;
		}

		// XML Buf Process (copy to xml buf)
		nTagLen = szTypeBuf.length();
		//if (szTagString != null)
		//{
		//	pXmlData = pXmlData.concat(szTagString);
		//}
		dmDDFTNDSSetXMLSize(nXmlSize + nTagLen);
		dmDDFTNDSSetXMLData(pXmlData);

		// Wbxml Buf Process (move to wbxml buf pointer)
		pWbxmlData = pWbxmlData.substring(1); /* move 1 byte */
		--nWbxmlSize;
		dmDDFTNDSSetWbxmlSize(nWbxmlSize);
		dmDDFTNDSSetWbxmlData(pWbxmlData);
	}

	public static boolean dmDDFTNDSUderMgmtTreeTagParse()
	{
		int nSize;
		String pData = null;
		byte[] wbxmlData;

		nSize = dmDDFTNDSGetWbxmlSize();
		pData = dmDDFTNDSGetWbxmlData();

		// Defects
		while (nSize != 0 && pData != null)
		{
			wbxmlData = pData.getBytes();

			switch (wbxmlData[0])
			{
				case DM_WBXML_CLOSE_BRACKET:
				{
					dmDDFTNDSParsingCloseTag();
				}
					break;

				case DM_WBXML_START_STRING_TAG:
				{
					dmDDFTNDSProcessStringData();
				}
					break;

				case DM_TNDS_NODE:
				{
					dmDDFTNDSParsingNodeTag();
				}
					break;

				case DM_TNDS_NOADNAME:
				{
					dmDDFTNDSParsingNodeNameTag();
				}
					break;

				case DM_TNDS_RTPROPERTIES:
				{
					dmDDFTNDSParsingRTPropertiesTag();
				}
					break;

				case DM_TNDS_FORMAT:
				{
					dmDDFTNDSParsingFormatTag();
				}
					break;

				case DM_TNDS_TYPE:
				{
					dmDDFTNDSParsingTypeTag();
				}
					break;

				case DM_TNDS_DDFNAME:
				{
					dmDDFTNDSParsingDDFNameTag();
				}
					break;

				case DM_TNDS_VALUE:
				{
					dmDDFTNDSParsingValueTag();
				}
					break;

				case DM_TNDS_MIME:
				{
					dmDDFTNDSParsingMIMETag();
				}
					break;

				case DM_TNDS_ACCESSTYPE:
				{
					dmDDFTNDSParsingAccessTypeTag();
				}
					break;

				case DM_TNDS_PATH:
				{
					dmDDFTNDSParsingPathTag();
				}
					break;

				case DM_TNDS_ACL:
				{
					dmDDFTNDSParsingACLTag();
				}
					break;

				case DM_TNDS_VERDTD:
				{
					dmDDFTNDSParsingVerdtdTag();
				}
					break;

				case DM_WBXML_CODEPAGE:
				{
					dmDDFTNDSParsingCodePage();
				}
					break;

				default:
				{
					tsLib.debugPrintException(DmDevInfoConst.DEBUG_EXCEPTION, String.valueOf(nSize));
					tsLib.debugPrintException(DmDevInfoConst.DEBUG_EXCEPTION, pData);
					return false;
				}
					// break;
			}

			nSize = dmDDFTNDSGetWbxmlSize();
			pData = (nSize != 0) ? dmDDFTNDSGetWbxmlData() : null;
		}

		return true;
	}

	public static void dmDDFTNDSParsingCloseTag()
	{
		String pWbxmlData = null;
		int nWbxmlSize;
		String pXmlData = null;
		int nXmlSize;

		int nTagLen = 0;
		String szTagString = null;

		// Get wbxml, xml buf info
		nWbxmlSize = dmDDFTNDSGetWbxmlSize();
		pWbxmlData = dmDDFTNDSGetWbxmlData();
		pXmlData = dmDDFTNDSGetXMLData();
		nXmlSize = dmDDFTNDSGetXMLSize();

		// Make Close tag </abc>
		szTagString = dmDDFTNDSMakeCloseTagString();
		if (szTagString != null)
		{ // Defects
			nTagLen = szTagString.length();
			// XML Buf Process (copy to xml buf)
			pXmlData = pXmlData.concat(szTagString);
		}
		dmDDFTNDSSetXMLSize(nXmlSize + nTagLen);
		dmDDFTNDSSetXMLData(pXmlData);

		// Wbxml Buf Process (move to wbxml buf pointer)
		pWbxmlData = pWbxmlData.substring(1);/* move 1 byte */
		--nWbxmlSize;
		dmDDFTNDSSetWbxmlSize(nWbxmlSize);
		dmDDFTNDSSetWbxmlData(pWbxmlData);
	}

	public static String dmDDFTNDSMakeCloseTagString()
	{
		int eCloseTokenIndex = 0;
		String pCloseTagName = "";

		dmDDFTNDSCheckMem(pCloseTagName);
		//if (!dmDDFTNDSCheckMem(pCloseTagName))
		//{
		//	tsLib.debugPrintException(DmDevInfoConst.DEBUG_EXCEPTION, "Alloc Error !!! ###");
		//	return null;
		//}

		eCloseTokenIndex = dmDDFTNDSManagePopTag();

		pCloseTagName = "</";
		pCloseTagName = pCloseTagName.concat(dmDDFTNDSGetTagString(eCloseTokenIndex));
		pCloseTagName = pCloseTagName.concat(">");

		return pCloseTagName; /* Must Free */
	}

	public static int dmDDFTNDSManagePopTag()
	{
		int eCloseTagID = 0;

		if (gstTagManage.nTagSP == 0)
		{
			tsLib.debugPrintException(DmDevInfoConst.DEBUG_EXCEPTION, " TagSP EMPTY !!! ###");
			return 0;
		}

		--gstTagManage.nTagSP;
		eCloseTagID = gstTagManage.eTagID[gstTagManage.nTagSP];
		gstTagManage.eTagID[gstTagManage.nTagSP] = 0;

		return eCloseTagID;
	}

	public static void dmDDFTNDSProcessStringData()
	{
		String pWbxmlData = null;
		int nWbxmlSize;
		String pXmlData = null;
		StringBuffer pBufXmlData = new StringBuffer();
		int nXmlSize;
		int nTag;

		int nDataLen = 0;

		nWbxmlSize = dmDDFTNDSGetWbxmlSize();
		pWbxmlData = dmDDFTNDSGetWbxmlData();
		pXmlData = dmDDFTNDSGetXMLData();
		pBufXmlData.append(pXmlData);
		nXmlSize = dmDDFTNDSGetXMLSize();

		pWbxmlData = pWbxmlData.substring(1); /* skip 0x03 */
		++nDataLen;
		nTag = (int) pWbxmlData.charAt(0); /* first string data byte */

		while (nTag != DM_WBXML_END_STRING_TAG) /* contents copy to XML buf */
		{
			pBufXmlData.append((char) nTag);
			pWbxmlData = pWbxmlData.substring(1);
			++nDataLen;
			nTag = (int) pWbxmlData.charAt(0);
		}

		dmDDFTNDSSetXMLSize(nXmlSize + nDataLen - 1); /* only contents data */
		dmDDFTNDSSetXMLData(pBufXmlData.toString());

		pWbxmlData = pWbxmlData.substring(1); /* 0x00 */
		nWbxmlSize -= (nDataLen + 1); /* first + contents + last */
		dmDDFTNDSSetWbxmlSize(nWbxmlSize);
		dmDDFTNDSSetWbxmlData(pWbxmlData);
	}

	public static void dmDDFTNDSAppendSyncMLCloseTag()
	{
		String pXmlData = null;
		int nXmlSize;
		int nTmpLen;

		pXmlData = dmDDFTNDSGetXMLData();
		nXmlSize = dmDDFTNDSGetXMLSize();

		pXmlData = pXmlData.concat(DM_XML_SYNCML_END_STRING);

		nTmpLen = DM_XML_SYNCML_END_STRING.length();

		dmDDFTNDSSetXMLData(pXmlData);
		dmDDFTNDSSetXMLSize(nXmlSize + nTmpLen);
	}

	// Xml to Wbxml Convert
	public static String tndsXml2WbxmlConvert(String pInData, int nInSize)
	{
		int nXmlSize = 0;
		String pXmlData = null;
		char nChar = 0x00;
		String pOutData;

		dmDDFInitConvert(pInData, nInSize);

		nXmlSize = dmDDFTNDSGetXMLSize();
		pXmlData = dmDDFTNDSGetXMLData();
		// nChar = pXmlData.charAt(0);
		nChar = pXmlData.toCharArray()[0];
		// nChar = pXmlData.getBytes()[0];

		while (nXmlSize != 0)
		{
			switch (Integer.valueOf(nChar))
			{
				case (int) DM_XML_OPEN_TAG:
				{
					dmDDFConvertCheckTag();
				}
					break;

				case (int) DM_XML_LINE_FEED:
				case DM_WBXML_START_STRING_TAG:
				case DM_WBXML_END_STRING_TAG:
				{
					dmDDFConvertSkip1Byte();
				}
					break;
				case DM_WBXML_PUBLIC_ID_LEN:
					break;
				default:
				{
					tsLib.debugPrintException(DmDevInfoConst.DEBUG_EXCEPTION, String.valueOf(nChar));
					return null;
				}
					// break;
			}

			nXmlSize = dmDDFTNDSGetXMLSize();
			pXmlData = dmDDFTNDSGetXMLData();
			if (pXmlData.length() != 0)
				// nChar = pXmlData.getBytes()[0];
				nChar = pXmlData.toCharArray()[0];
		}

		/* Convert End : nXmlSize will be 0 */
		pOutData = dmDDFTNDSGetWbxmlData();
		return pOutData;
	}

	public static void dmDDFInitConvert(String pInData, int nInSize)
	{
		gTndsData = new DMTndsData();

		dmDDFTNDSSetXMLSize(nInSize);
		dmDDFTNDSSetXMLData(pInData);
		dmDDFAllocConvertWbxmlData(nInSize);
		dmDDFConvertAddWbxmlHeader();
	}

	public static boolean dmDDFAllocConvertWbxmlData(int nSize)
	{
		String pWbxmlBuf = null;

		pWbxmlBuf = "";
		dmDDFTNDSCheckMem(pWbxmlBuf);
		//if (!dmDDFTNDSCheckMem(pWbxmlBuf))
		//{
		//	tsLib.debugPrintException(DmDevInfoConst.DEBUG_EXCEPTION, "Alloc Error !!! ###");
		//	return false;
		//}

		dmDDFTNDSSetWbxmlData(pWbxmlBuf);
		dmDDFTNDSSetWbxmlDataStart(pWbxmlBuf);
		return true;
	}

	public static void dmDDFConvertAddWbxmlHeader()
	{
		String pWbxmlData = null;
		int nWbxmlSize;
		int nIndex = 0;
		char[] tmpWbxml = new char[DM_WBXML_HEADER_LEN];

		nWbxmlSize = dmDDFTNDSGetWbxmlSize();

		while (nIndex < DM_WBXML_HEADER_LEN)
		{
			tmpWbxml[nIndex] = gTndsWbxmlHeaderInfo[nIndex];
			++nIndex;
		}

		nWbxmlSize = nIndex;
		pWbxmlData = String.valueOf(tmpWbxml);

		// Wbxml Buf Process (Hex value input Wbxml buf)
		dmDDFTNDSSetWbxmlSize(nWbxmlSize);
		dmDDFTNDSSetWbxmlData(pWbxmlData);
	}

	public static void dmDDFConvertCheckTag()
	{
		String pXmlData = null;

		pXmlData = dmDDFTNDSGetXMLData();
		pXmlData = pXmlData.substring(1);

		if (pXmlData.charAt(0) == DM_XML_SLASH)
		{
			// </ABC> Close Tag
			dmDDFConvertXmlCloseTag();
		}
		else
		{
			// <ABC> Open Tag
			dmDDFConvertXml2Wbxml();
		}
	}

	public static void dmDDFConvertXmlCloseTag()
	{
		String pWbxmlData = null;
		int nWbxmlSize;
		String pXmlData = null;
		int nXmlSize;

		char nTag;
		// int nTagLen = 0;

		// Get wbxml, xml buf info
		nWbxmlSize = dmDDFTNDSGetWbxmlSize();
		pWbxmlData = dmDDFTNDSGetWbxmlData();
		pXmlData = dmDDFTNDSGetXMLData();
		nXmlSize = dmDDFTNDSGetXMLSize();

		pXmlData = pXmlData.substring(2); /* pass 2 byte = '</' */
		nXmlSize -= 2;

		nTag = pXmlData.charAt(0);

		while (nTag != DM_XML_CLOSE_TAG)
		{
			pXmlData = pXmlData.substring(1);
			--nXmlSize;
			nTag = pXmlData.charAt(0);
		}

		pXmlData = pXmlData.substring(1); /* pass 1 byte = '>' */
		--nXmlSize;

		// Wbxml Buf Process
		pWbxmlData = pWbxmlData.concat(String.valueOf((char) DM_WBXML_END_TAG));
		++nWbxmlSize;

		dmDDFTNDSSetWbxmlSize(nWbxmlSize);
		dmDDFTNDSSetWbxmlData(pWbxmlData);

		// XML Buf Process (move to XML buf pointer)
		dmDDFTNDSSetXMLSize(nXmlSize);
		dmDDFTNDSSetXMLData(pXmlData);
	}

	public static void dmDDFConvertXml2Wbxml()
	{
		String pWbxmlData = null;
		int nWbxmlSize;
		String pXmlData = null;
		int nXmlSize;

		char nTag;
		int nTagLen = 0;
		String szTagString = "";

		char cHexValue = 0x00;

		// Get wbxml, xml buf info
		nWbxmlSize = dmDDFTNDSGetWbxmlSize();
		pWbxmlData = dmDDFTNDSGetWbxmlData();
		pXmlData = dmDDFTNDSGetXMLData();
		nTag = pXmlData.charAt(0);
		nXmlSize = dmDDFTNDSGetXMLSize();

		if (nTag == DM_XML_OPEN_TAG)
		{
			pXmlData = pXmlData.substring(1); /* pass '<' */
			nTag = pXmlData.charAt(0);

			while (nTag != DM_XML_CLOSE_TAG)
			{
				szTagString = szTagString.concat(String.valueOf(nTag));
				++nTagLen;
				pXmlData = pXmlData.substring(1);
				nTag = pXmlData.charAt(0);
			}

			++nTagLen; /* pass last char */
		}

		if (nTag == DM_XML_CLOSE_TAG)
		{
			++nTagLen;
			pXmlData = pXmlData.substring(1); /* pass '>' */

			cHexValue = dmDDFConvertString2WbxmlHex(szTagString);

			// Wbxml Buf Process (Hex value input Wbxml buf)
			pWbxmlData = pWbxmlData.concat(String.valueOf(cHexValue));
			++nWbxmlSize;

			dmDDFTNDSSetWbxmlSize(nWbxmlSize);
			dmDDFTNDSSetWbxmlData(pWbxmlData);

			// XML Buf Process (move to XML buf pointer)
			dmDDFTNDSSetXMLSize(nXmlSize - nTagLen);
			dmDDFTNDSSetXMLData(pXmlData);
		}

		szTagString = null;

		dmDDFConvertCheckElement(cHexValue);
	}

	public static char dmDDFConvertString2WbxmlHex(String szString)
	{
		int nIndex = 0;
		int nStringLen = 0;
		String XmlTag;

		nStringLen = szString.length();

		while (nIndex != DM_TNDS_TAG_NUM)
		{
			XmlTag = dmDDFConvertGetXMLTag(nIndex);

			if (szString.regionMatches(0, XmlTag, 0, nStringLen))
			{
				return (char) (nIndex + 0x05/* tnds spec */+ 0x40)/* wbxml spec */;
			}
			++nIndex;
		}

		if (szString.regionMatches(0, DM_XML_SYNCML_STRING, 0, DM_XML_SYNCML_STRING.length())) /* SyncML tag */
		{
			return DM_XML_SYNCML_HEX_VALUE;
		}

		tsLib.debugPrintException(DmDevInfoConst.DEBUG_EXCEPTION, "Not Found String !!! ###");
		return (char) -1;
	}

	public static String dmDDFConvertGetXMLTag(int nIndex)
	{
		return gszTndsTokenStr[nIndex];
	}

	public static void dmDDFConvertCheckElement(char cHex)
	{
		switch ((byte) cHex)
		{
			/*  */
			case DM_TNDS_PATH:
			case DM_TNDS_NOADNAME:
			case DM_TNDS_MIME:
			case DM_TNDS_ACL:
			case DM_TNDS_VALUE:
			case DM_TNDS_VERDTD:
			case DM_TNDS_MAN:
			case DM_TNDS_MOD:
			case DM_TNDS_DDFNAME:
			case DM_TNDS_VERNO:
			case DM_TNDS_DEFAULTVALUE:
			case DM_TNDS_DESCRIPTION:
			case DM_TNDS_DFTITLE:
			case DM_TNDS_NAME:
			case DM_TNDS_SIZE:
			case DM_TNDS_TITLE:
			case DM_TNDS_TSTAMP:
			{
				dmDDFProcessConvertStringData();
			}
				break;

			/*  */
			case DM_TNDS_FORMAT: /* <chr/> <chr />, <chr></chr> , chr */
			case DM_TNDS_ACCESSTYPE:
			case DM_TNDS_DFFORMAT:
			case DM_TNDS_OCCURRENCE: /* 2 case */
			case DM_TNDS_SCOPE:
			case DM_TNDS_CASESENSE:
			{
				dmDDFProcessConvertHexData(cHex);
			}
				break;

			case DM_TNDS_MGMTTREE:
			{
				dmDDFConvertAddTndsCodePage();
			}
				break;

			case DM_XML_SYNCML_HEX_VALUE:
			case DM_TNDS_NODE:
			case DM_TNDS_RTPROPERTIES:
			case DM_TNDS_TYPE:
			{
				// don't need
			}
				break;

			default:
			{
				tsLib.debugPrintException(DmDevInfoConst.DEBUG_EXCEPTION, "[value : " + cHex + "]!!! ###");
			}
				break;
		}
	}

	public static void dmDDFProcessConvertStringData()
	{
		String pWbxmlData = null;
		int nWbxmlSize;
		String pXmlData = null;
		int nXmlSize;

		char nChar;
		int nStringLen = 0;

		// Get wbxml, xml buf info
		nWbxmlSize = dmDDFTNDSGetWbxmlSize();
		pWbxmlData = dmDDFTNDSGetWbxmlData();
		pXmlData = dmDDFTNDSGetXMLData();
		nChar = pXmlData.charAt(0);
		nXmlSize = dmDDFTNDSGetXMLSize();

		// 0x03
		pWbxmlData = pWbxmlData.concat(String.valueOf((char) DM_WBXML_START_STRING_TAG));
		++nWbxmlSize;

		// string
		while (nChar != DM_XML_OPEN_TAG)
		{
			pWbxmlData = pWbxmlData.concat(String.valueOf(nChar));
			++nWbxmlSize;
			pXmlData = pXmlData.substring(1);
			++nStringLen;
			nChar = pXmlData.charAt(0);
		}

		// 0x00
		pWbxmlData = pWbxmlData.concat(String.valueOf((char) DM_WBXML_END_STRING_TAG));
		++nWbxmlSize;

		// Wbxml Buf Process (Hex value input Wbxml buf)
		dmDDFTNDSSetWbxmlSize(nWbxmlSize);
		dmDDFTNDSSetWbxmlData(pWbxmlData);

		// XML Buf Process (move to XML buf pointer)
		dmDDFTNDSSetXMLSize(nXmlSize - (nStringLen));
		dmDDFTNDSSetXMLData(pXmlData);
	}

	public static void dmDDFProcessConvertHexData(char cHex)
	{
		switch (cHex)
		{
			case DM_TNDS_FORMAT:
			case DM_TNDS_DFFORMAT:
			{
				dmDDFProcessConvertFormatElement();
			}
				break;

			case DM_TNDS_ACCESSTYPE:
			{
				dmDDFProcessConvertAccessTypeElement();
			}
				break;

			case DM_TNDS_OCCURRENCE: /* 2 case */
			case DM_TNDS_SCOPE:
			case DM_TNDS_CASESENSE:
			{

			}
				break;
		}
	}

	public static void dmDDFProcessConvertFormatElement()
	{
		String pWbxmlData = null;
		int nWbxmlSize;
		String pXmlData = null;
		int nXmlSize;

		char nChar;
		String szString = "";

		// Get wbxml, xml buf info
		nWbxmlSize = dmDDFTNDSGetWbxmlSize();
		pWbxmlData = dmDDFTNDSGetWbxmlData();
		pXmlData = dmDDFTNDSGetXMLData();
		nChar = pXmlData.charAt(0);
		nXmlSize = dmDDFTNDSGetXMLSize();

		if (nChar == DM_XML_OPEN_TAG)
		{ // <chr/>
			pXmlData = pXmlData.substring(1); // pass '<'
			--nXmlSize;
			nChar = pXmlData.charAt(0); // input first char
			while (nChar != DM_XML_SLASH)
			{
				szString = szString.concat(String.valueOf(nChar));
				pXmlData = pXmlData.substring(1);
				--nXmlSize;

				nChar = pXmlData.charAt(0);
				if (nChar == ' ')
				{
					pXmlData = pXmlData.substring(1);
					--nXmlSize;

					nChar = pXmlData.charAt(0);
				}
			}
			pXmlData = pXmlData.substring(2); // pass "/>"
			nXmlSize -= 2;
		}
		else //if (nChar != DM_XML_OPEN_TAG)
		{ // chr
			while (nChar != DM_XML_OPEN_TAG)
			{
				szString = szString.concat(String.valueOf(nChar));
				pXmlData = pXmlData.substring(1);
				--nXmlSize;

				nChar = pXmlData.charAt(0);
			}
		}
		//else
		// <chr></chr>
		//{
		//}

		nChar = 0x00;
		nChar = (char) dmDDFConvertString2WbxmlHex(szString);
		nChar = (char) ((int) nChar - 0x40)/* wbxml */;

		// Wbxml Buf Process (Hex value input Wbxml buf)
		pWbxmlData = pWbxmlData.concat(String.valueOf(nChar));
		++nWbxmlSize;
		dmDDFTNDSSetWbxmlSize(nWbxmlSize);
		dmDDFTNDSSetWbxmlData(pWbxmlData);

		// XML Buf Process (move to XML buf pointer)
		dmDDFTNDSSetXMLSize(nXmlSize);
		dmDDFTNDSSetXMLData(pXmlData);

		szString = null;
	}

	public static void dmDDFProcessConvertAccessTypeElement()
	{
		String pWbxmlData = null;
		int nWbxmlSize;
		String pXmlData = null;
		int nXmlSize;

		char nChar;
		String szString = "";

		// Get wbxml, xml buf info
		nWbxmlSize = dmDDFTNDSGetWbxmlSize();
		pWbxmlData = dmDDFTNDSGetWbxmlData();
		pXmlData = dmDDFTNDSGetXMLData();
		nChar = pXmlData.charAt(0);
		nXmlSize = dmDDFTNDSGetXMLSize();

		if (nChar == DM_XML_OPEN_TAG)
		{ // <chr/>
			pXmlData = pXmlData.substring(1); // pass '<'
			--nXmlSize;
			nChar = pXmlData.charAt(0); // input first char
			while (nChar != DM_XML_SLASH)
			{
				szString = szString.concat(String.valueOf(nChar));
				pXmlData = pXmlData.substring(1);
				--nXmlSize;

				nChar = pXmlData.charAt(0);
			}
			pXmlData = pXmlData.substring(2); // pass "/>"
			nXmlSize -= 2;
		}

		nChar = 0x00;
		nChar = (char) dmDDFConvertString2WbxmlHex(szString);
		nChar = (char) ((int) nChar - 0x40) /* wbxml */;

		// Wbxml Buf Process (Hex value input Wbxml buf)
		pWbxmlData = pWbxmlData.concat(String.valueOf(nChar));
		++nWbxmlSize;
		dmDDFTNDSSetWbxmlSize(nWbxmlSize);
		dmDDFTNDSSetWbxmlData(pWbxmlData);

		// XML Buf Process (move to XML buf pointer)
		dmDDFTNDSSetXMLSize(nXmlSize);
		dmDDFTNDSSetXMLData(pXmlData);

		szString = null;
	}

	public static void dmDDFConvertAddTndsCodePage()
	{
		String pWbxmlData = null;
		int nWbxmlSize;
		char nTmp = 0x00;
		byte[] tmpWbxml;

		nWbxmlSize = dmDDFTNDSGetWbxmlSize();
		pWbxmlData = dmDDFTNDSGetWbxmlData();

		tmpWbxml = pWbxmlData.getBytes();

		--nWbxmlSize;
		nTmp = (char) tmpWbxml[nWbxmlSize]; /* MgmtTree */

		pWbxmlData = pWbxmlData.substring(0, nWbxmlSize);

		pWbxmlData = pWbxmlData.concat(String.valueOf((char) 0x00));
		++nWbxmlSize;

		pWbxmlData = pWbxmlData.concat(String.valueOf((char) 0x02));
		++nWbxmlSize;

		pWbxmlData = pWbxmlData.concat(String.valueOf((char) nTmp));
		++nWbxmlSize;

		dmDDFTNDSSetWbxmlSize(nWbxmlSize);
		dmDDFTNDSSetWbxmlData(pWbxmlData);
	}

	public static void dmDDFConvertSkip1Byte()
	{
		String pXmlData = null;
		int nXmlSize;

		pXmlData = dmDDFTNDSGetXMLData();
		nXmlSize = dmDDFTNDSGetXMLSize();

		pXmlData = pXmlData.substring(1);
		--nXmlSize;

		dmDDFTNDSSetXMLSize(nXmlSize);
		dmDDFTNDSSetXMLData(pXmlData);
	}


	public static void TndsParseFinish()
	{
		gTndsData = null;
		gstTagManage = null;
	}
}
