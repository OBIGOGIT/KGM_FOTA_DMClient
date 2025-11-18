package com.tsdm.adapt;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.tsdm.core.data.constants.DmDevInfoConst;
import com.tsdm.db.tsDB;
import com.tsdm.db.tsdmDB;

public class tsOmVfs
{
	public tsDmVnode root;
	public byte[]				stdobj_space;

	private static int			index		= 0;
	private static final int	OM_MAX_LEN	= 512;

	public tsOmVfs()
	{
		root = null;
		stdobj_space = new byte[40960];
	}

	private void resetStdobj()
	{
		index = 0;
		stdobj_space = null;
		stdobj_space = new byte[40960];
	}

	public static int dmOmvfsInit(tsOmVfs pVfs)
	{
		int nRet = DmDevInfoConst.OMVFS_ERR_OK;
		if (pVfs.root == null)
		{
			pVfs.root = dmOmvfsCreateNewNode("/", true);
		}

		nRet = dmOmvfsLoadFs(pVfs);
		return nRet;
	}

	public static tsDmVnode dmOmvfsCreateNewNode(String name, boolean defaultacl)
	{
		tsOmAcl acl;
		tsOmList item;
		tsDmVnode ptNode;

		ptNode = new tsDmVnode();

		if (defaultacl)
		{
			acl = new tsOmAcl();
			acl.serverid = "*";
			acl.ac = DmDevInfoConst.OMACL_ADD | DmDevInfoConst.OMACL_DELETE | DmDevInfoConst.OMACL_GET | DmDevInfoConst.OMACL_REPLACE;

			item = new tsOmList();
			item.data = acl;
			item.next = null;

			ptNode.acl = item;
		}

		ptNode.name = name;
		ptNode.format = DmDevInfoConst.FORMAT_NODE;
		ptNode.verno = 0;
		ptNode.size = 0;
		ptNode.vaddr = -1;
		ptNode.scope = DmDevInfoConst.SCOPE_DYNAMIC;

		return ptNode;
	}

	public static int dmOmvfsLoadFs(tsOmVfs pVfs)
	{
		int nFileId = 0;
		DataInputStream Input = null;
		int pBuff = 0;
		int nSize = 0;
		String szfilename = null;

		nFileId = tsdmDB.dmdbGetFileIdObjectTreeInfo();
		szfilename = tsdmDB.fileGetNameFromCallerID(szfilename, nFileId);
		nSize = tsdmDB.dmdbGetFileSize(nFileId);
		if (nSize <= 0)
		{
			return DmDevInfoConst.OMVFS_ERR_OK;
		}

		byte[] tmp = new byte[nSize];
		tsdmDB.dmReadFile(nFileId, 0, nSize, tmp);
		try
		{
			Input = new DataInputStream(new FileInputStream(szfilename));
		}
		catch (FileNotFoundException e)
		{
			tsLib.debugPrintException(DmDevInfoConst.DEBUG_EXCEPTION, e.toString());
		}

		pBuff = tmp[index];
		while (pBuff == DmDevInfoConst.OMVFSPACK_STARTNODE)
		{
			try
			{
				pBuff = dmOmvfsUnpackFsNode(pVfs, Input, pBuff, pVfs.root, tmp, nSize);
			}
			catch (Exception e)
			{
				tsLib.debugPrintException(DmDevInfoConst.DEBUG_EXCEPTION, e.toString());

				pVfs.resetStdobj();
				dmOmvfsDeleteOmFile();
				return DmDevInfoConst.OMVFS_ERR_OK;
			}
			if (pBuff == 0)
			{
				try
				{
					if (Input != null)
						Input.close();
				}
				catch (IOException e)
				{
					tsLib.debugPrintException(DmDevInfoConst.DEBUG_EXCEPTION, e.toString());
				}
				tmp = null;
				index = 0;
				return DmDevInfoConst.OMVFS_ERR_FAILED;
			}
		}

		try
		{
			if (Input != null)
				Input.close();
		}
		catch (IOException e)
		{
			tsLib.debugPrintException(DmDevInfoConst.DEBUG_EXCEPTION, e.toString());
		}

		tmp = null;
		index = 0;
		nFileId = tsdmDB.dmdbGetFileIdObjectData();
		nSize = tsdmDB.dmdbGetFileSize(nFileId);
		tsdmDB.dmReadFile(nFileId, 0, nSize, pVfs.stdobj_space);

		return DmDevInfoConst.OMVFS_ERR_OK;
	}

	public static int dmOmvfsSaveFs(tsOmVfs pVfs) throws IOException
	{
		DataOutputStream Data = null;
		int nFileId = 0;

		String szfilename = null;
		nFileId = tsdmDB.dmdbGetFileIdObjectTreeInfo();
		szfilename = tsdmDB.fileGetNameFromCallerID(szfilename, nFileId);

		try
		{
			Data = new DataOutputStream(new FileOutputStream(szfilename));
			tsDmVnode ptNode = pVfs.root.childlist;

			while (ptNode != null)
			{
				Data = dmOmvfsPackFsNode(Data, ptNode);
				ptNode = ptNode.next;
			}
			Data.close();
		}
		catch (FileNotFoundException e)
		{
			tsLib.debugPrintException(DmDevInfoConst.DEBUG_EXCEPTION, e.toString());
		}
		catch (Exception e)
		{
			tsLib.debugPrintException(DmDevInfoConst.DEBUG_EXCEPTION, e.toString());
		}

		nFileId = tsdmDB.dmdbGetFileIdObjectData();
		tsdmDB.dmdbWriteFile(nFileId, (int) DmDevInfoConst.MAX_SPACE_SIZE, pVfs.stdobj_space);

		return DmDevInfoConst.OMVFS_ERR_OK;
	}

	public static DataOutputStream dmOmvfsPackFsNode(DataOutputStream pBuff, tsDmVnode ptNode) throws IOException
	{
		tsDmVnode cur = null;
		if (ptNode == null)
		{
			return pBuff;
		}

		cur = ptNode.childlist;

		pBuff = dmOmvfsPackStart(pBuff);
		pBuff = dmOmvfsPackNode(pBuff, ptNode);

		while (cur != null)
		{
			pBuff = dmOmvfsPackFsNode(pBuff, cur);
			cur = cur.next;
		}

		pBuff = dmOmvfsPackEnd(pBuff);
		return pBuff;
	}

	public static DataOutputStream dmOmvfsPackNode(DataOutputStream pBuff, tsDmVnode ptNode) throws IOException
	{
		int num = 0;
		tsOmAcl acl;
		tsOmList item;
		String str;

		item = ptNode.acl;
		while (item != null)
		{
			num++;
			item = item.next;
		}

		pBuff = dmOmvfsPackByte(pBuff, num);

		item = ptNode.acl;
		while (item != null)
		{
			acl = (tsOmAcl) item.data;
			pBuff = dmOmvfsPackStr(pBuff, acl.serverid);
			pBuff = dmOmvfsPackByte(pBuff, acl.ac);
			item = item.next;
		}

		pBuff = dmOmvfsPackInt32(pBuff, ptNode.format);
		pBuff = dmOmvfsPackStr(pBuff, ptNode.name);
		pBuff = dmOmvfsPackInt32(pBuff, (int) ptNode.size);
		pBuff = dmOmvfsPackStr(pBuff, ptNode.title);
		pBuff = dmOmvfsPackStr(pBuff, ptNode.tstamp);
		pBuff = dmOmvfsPackInt32(pBuff, ptNode.scope);

		num = 0;
		item = ptNode.type;
		while (item != null)
		{
			num++;
			item = item.next;
		}
		pBuff = dmOmvfsPackByte(pBuff, num);
		if (num > 0)
		{
			item = ptNode.type;
			while (item != null)
			{
				str = (String) item.data;
				pBuff = dmOmvfsPackStr(pBuff, str);
				item = item.next;
			}
		}

		pBuff = dmOmvfsPackInt16(pBuff, ptNode.verno);
		pBuff = dmOmvfsPackStr(pBuff, ptNode.ddfname);
		pBuff = dmOmvfsPackInt32(pBuff, ptNode.vaddr);

		return pBuff;
	}

	public static DataOutputStream dmOmvfsPackStart(DataOutputStream pBuff) throws IOException
	{
		pBuff.writeByte(DmDevInfoConst.OMVFSPACK_STARTNODE);
		return pBuff;
	}

	public static DataOutputStream dmOmvfsPackEnd(DataOutputStream pBuff) throws IOException
	{
		pBuff.writeByte(DmDevInfoConst.OMVFSPACK_ENDNODE);
		return pBuff;
	}

	public static DataOutputStream dmOmvfsPackByte(DataOutputStream pBuff, int b) throws IOException
	{
		pBuff.writeInt(b);
		return pBuff;
	}

	public static DataOutputStream dmOmvfsPackStr(DataOutputStream pBuff, String str) throws IOException
	{
		int len = 0;

		if (str == null)
		{
			len = 0;
		}
		else
		{
			len = str.length();
		}
		pBuff.writeInt(len);

		if (str != null)
		{
			try
			{
				pBuff.write(str.getBytes());
			}
			catch (IOException e)
			{
				tsLib.debugPrintException(DmDevInfoConst.DEBUG_EXCEPTION, e.toString());
			}
		}

		return pBuff;
	}

	public static DataOutputStream dmOmvfsPackInt32(DataOutputStream pBuff, int n) throws IOException
	{
		pBuff.writeInt(n);
		return pBuff;
	}

	public static DataOutputStream dmOmvfsPackInt16(DataOutputStream pBuff, int n) throws IOException
	{
		pBuff.writeInt(n);
		return pBuff;
	}

	public static int dmOmvfsUnpackFsNode(tsOmVfs pVfs, DataInputStream in, int pBuff, tsDmVnode ptNode, byte[] buf, int nSize) throws Exception
	{
		tsDmVnode ptChild;
		int ptr = pBuff;

		if (in == null)
			return 0;

		while (ptr != DmDevInfoConst.OMVFSPACK_ENDNODE)
		{
			if (ptr == DmDevInfoConst.OMVFSPACK_STARTNODE)
			{
				ptr = in.readByte();
				index++;
				ptChild = new tsDmVnode();
				in = dmOmvfsUnpackNode(in, ptChild);
				dmOmvfsAppendNode(pVfs, ptNode, ptChild);

				ptr = buf[index];// in.read();
				while (ptr != DmDevInfoConst.OMVFSPACK_ENDNODE)
				{
					if (ptr == DmDevInfoConst.OMVFSPACK_STARTNODE)
					{
						ptr = dmOmvfsUnpackFsNode(pVfs, in, ptr, ptChild, buf, nSize);
					}
					else if (ptr != DmDevInfoConst.OMVFSPACK_ENDNODE)
					{
						return 0;
					}
				}
			}
			else if (ptr != DmDevInfoConst.OMVFSPACK_ENDNODE)
			{
				return 0;
			}
		}
		ptr = in.readByte();
		index++; // read(=skip) end
		if (index == nSize)
			return DmDevInfoConst.OMVFSPACK_ENDNODE;

		return buf[index];
	}

	public static DataInputStream dmOmvfsUnpackNode(DataInputStream in, tsDmVnode ptNode) throws Exception
	{
		int num = 0, ac;
		tsOmAcl acl;
		tsOmList item;
		String tmp = null;
		int i;
		String str = "";

		num = in.readInt();
		index += 4;
		for (i = 0; i < num; i++)
		{
			tmp = dmOmvfsUnpackStr(in);
			ac = in.readInt();
			index += 4;

			acl = new tsOmAcl();
			acl.serverid = tmp;
			acl.ac = ac;
			item = new tsOmList();
			item.data = acl;
			ptNode.acl = dmOmAppendList(ptNode.acl, item);
		}
		ptNode.format = in.readInt();
		index += 4;
		ptNode.name = dmOmvfsUnpackStrDup(in);
		ptNode.size = in.readInt();
		index += 4;
		ptNode.title = dmOmvfsUnpackStrDup(in);
		ptNode.tstamp = dmOmvfsUnpackStrDup(in);
		ptNode.scope = in.readInt();
		index += 4;
		num = in.readInt();
		index += 4;
		if (num > 0)
		{
			for (i = 0; i < num; i++)
			{
				tmp = dmOmvfsUnpackStr(in);
				str = tmp;
				item = new tsOmList();
				item.data = str;
				ptNode.type = dmOmAppendList(ptNode.type, item);
			}
		}
		ptNode.verno = in.readInt();
		index += 4;
		ptNode.ddfname = dmOmvfsUnpackStrDup(in);
		ptNode.vaddr = in.readInt();
		index += 4;

		return in;
	}

	public static String dmOmvfsUnpackStr(DataInputStream in) throws Exception
	{
		int len = 0;
		byte[] tmp = null;
		len = in.readInt();
		index += 4;

		if (len == 0)
		{
			return null;
		}
		else if (len > OM_MAX_LEN)
		{
			throw new Exception("OM_MAX_LEN over");
		}

		tmp = new byte[len];
		in.read(tmp, 0, len);

		index += len;

		String str = new String(tmp);
		return str;
	}

	public static String dmOmvfsUnpackStrDup(DataInputStream in) throws Exception
	{
		int len;
		byte[] tmp = null;
		len = in.readInt();
		index += 4;

		if (len == 0)
		{
			return null;
		}
		else if (len > OM_MAX_LEN)
		{
			throw new Exception("OM_MAX_LEN over");
		}

		tmp = new byte[len];
		in.read(tmp, 0, len);

		index += len;

		String str = new String(tmp);

		return str;
	}

	public static tsOmList dmOmAppendList(tsOmList h, tsOmList node)
	{
		tsOmList tmp;

		if (h == null)
		{
			h = node;
			h.next = null;
			return h;
		}

		tmp = h;
		while (tmp.next != null)
		{
			tmp = tmp.next;
		}

		node.next = null;
		tmp.next = node;

		return h;
	}

	public static int dmOmvfsAppendNode(tsOmVfs pVfs, tsDmVnode ptParent, tsDmVnode ptChild)
	{
		tsDmVnode last;

		if (dmOmvfsHaveThisChild(pVfs, ptParent, ptChild))
		{
			return DmDevInfoConst.OMVFS_ERR_NOEFFECT;
		}

		if (ptParent.childlist == null)
		{
			ptParent.childlist = ptChild;
			/* MOD : Improve ACL Check(Merge From KDS) */
			ptChild.ptParentNode = ptParent;
			return DmDevInfoConst.OMVFS_ERR_OK;
		}

		last = ptParent.childlist;
		while (last.next != null)
		{
			last = last.next;
		}
		last.next = ptChild;

		/* MOD : Improve ACL Check(Merge From KDS) */
		ptChild.ptParentNode = ptParent;
		return DmDevInfoConst.OMVFS_ERR_OK;
	}

	public static boolean dmOmvfsHaveThisChild(tsOmVfs pVfs, tsDmVnode ptParent, tsDmVnode ptChild)
	{
		tsDmVnode cur = null;

		if (ptParent != null)
		{
			cur = ptParent.childlist;
		}

		while (cur != null)
		{
			if (cur.name.compareTo(ptChild.name) == 0)
			{
				return true;
			}
			cur = cur.next;
		}

		return false;
	}

	public static tsDmVnode dmOmvfsPath2Node(tsOmVfs pVfs, String pPath)
	{
		String[] nodenamesplit;
		// char[] nodename = new char[256];
		String nodename = "";
		String ptr = pPath;
		tsDmVnode ptNode = null;

		tsDmVnode ptBaseNode = pVfs.root;

		if (pPath == null)
			return null;

		if (pPath.compareTo(".") == 0 || pPath.compareTo("./") == 0)
		{
			return pVfs.root;
		}

		nodenamesplit = ptr.split("/");
		int l = nodenamesplit.length;
		if(nodenamesplit[0].compareTo(".") != 0)
		{
			return null;
		}
		
		for (int i = 1; i < l; i++)
		{
			nodename = nodenamesplit[i];

			ptNode = dmOmvfsGetNode(pVfs, nodename, ptBaseNode);
			if (ptNode == null)
			{
				return null;
			}

			ptBaseNode = ptNode;
		}

		return ptNode;
	}

	public static tsDmVnode dmOmvfsGetNode(tsOmVfs pVfs, String pNodeName, tsDmVnode ptBaseNode)
	{
		tsDmVnode cur = null;
		if (ptBaseNode != null)
		{
			cur = ptBaseNode.childlist;
		}

		if ((pNodeName == null) || (pNodeName.length() == 0))
			return null;

		if (pNodeName.compareTo("/") == 0 || pNodeName.charAt(0) == '\0')
		{
			return pVfs.root;
		}

		while (cur != null)
		{
			if (cur.name.equals(pNodeName)) // (cur.name.compareTo(pNodeName) == 0)
			{
				return cur;
			}
			cur = cur.next;
		}
		return null;
	}

	public static int dmOmvfsCreatePath(tsOmVfs pVfs, String pPath)
	{
		String[] nodenamesplit;
		String nodename = "";
		String ptr = pPath;
		tsDmVnode ptNode;
		int index = 0;
		int i = 0;
		tsDmVnode ptBaseNode = pVfs.root;

		nodenamesplit = ptr.split("/");
		int l = nodenamesplit.length;

		if (nodenamesplit[0].compareTo(".") == 0)
			index = 1;
		else
			index = 0;

		for (i = index; i < l; i++)
		{
			nodename = nodenamesplit[i];
			if (i + 1 == l)
			{
				if (dmOmvfsGetNode(pVfs, nodename, ptBaseNode) != null)
				{
					return DmDevInfoConst.OMVFS_ERR_NOEFFECT;
				}

				ptNode = dmOmvfsCreateNewNode(nodename, true);
				dmOmvfsAppendNode(pVfs, ptBaseNode, ptNode);
				return DmDevInfoConst.OMVFS_ERR_OK;
			}
			ptNode = dmOmvfsGetNode(pVfs, nodename, ptBaseNode);
			if (ptNode == null)
			{
				return DmDevInfoConst.OMVFS_ERR_BUFFER_NOT_ENOUGH;
			}

			ptBaseNode = ptNode;
		}
		return DmDevInfoConst.OMVFS_ERR_OK;
	}

	public static int dmOmvfsWriteObj(tsOmVfs pVfs, String pPath, int nTotalSize, int nOffset, Object pBuff, int nBuffSize)
	{
		tsDmVnode ptNode;
		int addr = 0;
		int ret;
		int blocksize;

		ptNode = dmOmvfsPath2Node(pVfs, pPath);
		if (ptNode == null)
		{
			return DmDevInfoConst.OMVFS_ERR_INVALIDPARAMETER;
		}
		if (nOffset == 0)
		{
			// {
			addr = dmOmvfsGetFreeVaddr(pVfs, nTotalSize);
			if (addr < 0)
			{
				return addr;
			}
			ptNode.vaddr = addr;
			ptNode.size = nTotalSize;
		}
		else
		{
			addr = ptNode.vaddr;
		}

		blocksize = nOffset + nBuffSize;
		if (blocksize > nTotalSize)
		{
			nBuffSize -= (blocksize - nTotalSize);
		}

		ret = dmOmvfsSaveFsData(pVfs, ptNode, (int) (addr + nOffset), pBuff, nBuffSize);
		//if (ret != DmDevInfoConst.OMVFS_ERR_OK)
		//{
		//	return DmDevInfoConst.OMVFS_ERR_FAILED;
		//}
		return nBuffSize;

	}

	public static int dmOmvfsGetFreeVaddr(tsOmVfs pVfs, int nSize)
	{
		tsDmVfspace pSpace = null;
		int start, end, s;
		int i, k;
		int ret;

		pSpace = new tsDmVfspace();
		dmOmvfsFindVaddr(pVfs, pVfs.root, pSpace);

		if (pSpace.i == 0)
		{
			ret = 0;
			pSpace = null;
			return ret;
		}

		for (i = pSpace.i - 1; i >= 1; i--)
		{
			for (k = 0; k <= i - 1; k++)
			{
				if (pSpace.start[k + 1] < pSpace.start[k])
				{
					start = pSpace.start[k + 1];
					end = pSpace.end[k + 1];
					pSpace.start[k + 1] = pSpace.start[k];
					pSpace.end[k + 1] = pSpace.end[k];
					pSpace.start[k] = start;
					pSpace.end[k] = end;
				}
			}
		}

		if (pSpace.start[0] > 0 && pSpace.start[0] + 1 >= nSize)
		{
			ret = 0;
			pSpace = null;
			return ret;
		}

		for (i = 0; i < pSpace.i - 1; i++)
		{
			s = pSpace.start[i + 1] - pSpace.end[i] - 1;
			if (s >= nSize)
			{
				ret = pSpace.end[i];
				pSpace = null;
				return ret;
			}
		}

		if (DmDevInfoConst.MAX_SPACE_SIZE - pSpace.end[pSpace.i - 1] - 1 >= nSize)
		{
			ret = pSpace.end[pSpace.i - 1];
			pSpace = null;
			return ret;
		}

		ret = DmDevInfoConst.OMVFS_ERR_NOSPACE;

		pSpace = null;
		return ret;
	}

	public static void dmOmvfsFindVaddr(tsOmVfs pVfs, tsDmVnode ptNode, tsDmVfspace pSpace)
	{
		tsDmVnode cur = ptNode.childlist;

		while (cur != null)
		{
			dmOmvfsFindVaddr(pVfs, cur, pSpace);
			cur = cur.next;
		}

		if (ptNode.vaddr >= 0 && ptNode.size > 0)
		{
			pSpace.start[pSpace.i] = ptNode.vaddr;
			pSpace.end[pSpace.i] = (ptNode.vaddr + ptNode.size);
			pSpace.i++;
		}
	}

	public static int dmOmvfsSaveFsData(tsOmVfs pVfs, tsDmVnode ptNode, int l, Object pBuff, int nSize)
	{
		int i = 0;
		String tmp = new String(pBuff.toString());//
		byte[] data = tmp.getBytes();
		for (i = 0; i < nSize; i++)
		{
			if (data.length <= i)
				break;
			pVfs.stdobj_space[l + i] = data[i];
		}
		return DmDevInfoConst.OMVFS_ERR_OK;
	}

	public static int dmOmvfsLoadFsData(tsOmVfs pVfs, tsDmVnode ptNode, int addr, char[] pBuff, int nSize)
	{
		int i = 0;
		for (i = 0; i < nSize; i++)
		{
			pBuff[i] = (char) pVfs.stdobj_space[addr + i];
		}
		return DmDevInfoConst.OMVFS_ERR_OK;
	}

	public static int dmOmvfsRemoveNode(tsOmVfs pVfs, tsDmVnode ptNode, boolean deletechilds)
	{
		tsDmVnode cur = ptNode.childlist;
		tsDmVnode ptParent, tmp;
		int ret;

		if (cur != null)
		{
			if (!deletechilds)
			{
				return DmDevInfoConst.OMVFS_ERR_FAILED;
			}

			while (cur != null)
			{
				ret = dmOmvfsRemoveNode(pVfs, cur, true);
				if (ret != DmDevInfoConst.OMVFS_ERR_OK)
				{
					return ret;
				}

				if (ptNode.childlist == null)
				{
					cur = null;
				}
				else
				{
					cur = ptNode.childlist;
				}
			}
		}

		ptParent = dmOmvfsGetParent(pVfs, pVfs.root, ptNode);
		if (ptParent == null)
		{
			return DmDevInfoConst.OMVFS_ERR_FAILED;
		}
		if (ptParent.childlist == ptNode)
		{
			tmp = ptNode.next;
			ptParent.childlist = tmp;
		}
		else
		{
			cur = ptParent.childlist;
			while (cur.next != null)
			{
				if (cur.next == ptNode)
				{
					cur.next = ptNode.next;
					break;
				}
				cur = cur.next;
			}
		}

		if (ptNode.acl != null)
		{
			dmOmDeleteAclList(ptNode.acl);
		}
		if (ptNode.type != null)
		{
			dmOmDeleteMimeList(ptNode.type);
		}

		ptNode.name = null;
		ptNode.title = null;
		ptNode.tstamp = null;
		ptNode.ddfname = null;

		ptNode.next = null;
		/* MOD : Improve ACL Check(Merge From KDS) */
		ptNode.ptParentNode = null;
		ptNode = null;

		return DmDevInfoConst.OMVFS_ERR_OK;
	}

	public static tsDmVnode dmOmvfsGetParent(tsOmVfs pVfs, tsDmVnode ptBaseNode, tsDmVnode ptNode)
	{
		tsDmVnode ptParent, ptChild;
		tsDmVnode tmp;

		ptParent = ptBaseNode;
		ptChild = ptParent.childlist;

		while (ptChild != null)
		{
			if (ptChild == ptNode)
				return ptParent;
			ptChild = ptChild.next;
		}

		ptChild = ptParent.childlist;
		while (ptChild != null)
		{
			tmp = dmOmvfsGetParent(pVfs, ptChild, ptNode);
			if (tmp != null)
			{
				return tmp;
			}
			ptChild = ptChild.next;
		}

		return null;
	}

	public static void dmOmDeleteAclList(tsOmList h)
	{
		tsOmList next;
		tsOmList cur = h;
		@SuppressWarnings("unused")
		tsOmAcl acl = null;

		while (cur != null)
		{
			next = cur.next;
			acl = (tsOmAcl) cur.data;

			acl = null;
			cur = null;

			cur = next;
		}
	}

	public static void dmOmDeleteMimeList(tsOmList h)
	{
		tsOmList next;
		tsOmList cur;

		cur = h;

		while (cur != null)
		{
			next = cur.next;

			cur.data = null;
			cur = null;

			cur = next;
		}
	}

	public static int dmOmvfsGetData(tsOmVfs pVfs, tsDmVnode ptNode, char[] pBuff)
	{
		int ret;

		if (ptNode.size > 0 && ptNode.vaddr >= 0)
		{
			ret = dmOmvfsLoadFsData(pVfs, ptNode, ptNode.vaddr, pBuff, ptNode.size);
			//if (ret != DmDevInfoConst.OMVFS_ERR_OK)
			//{
			//	return DmDevInfoConst.OMVFS_ERR_FAILED;
			//}
			return DmDevInfoConst.OMVFS_ERR_OK;
		}
		return DmDevInfoConst.OMVFS_ERR_FAILED;
	}

	public static int dmOmvfsSetData(tsOmVfs pVfs, tsDmVnode ptNode, Object pBuff, int nBuffSize)
	{
		int addr;
		int ret;

		addr = dmOmvfsGetFreeVaddr(pVfs, nBuffSize);
		if (addr < 0)
		{
			return addr;
		}

		ptNode.vaddr = addr;
		ptNode.size = nBuffSize;

		ret = dmOmvfsSaveFsData(pVfs, ptNode, (int) addr, pBuff, nBuffSize);
		//if (ret != DmDevInfoConst.OMVFS_ERR_OK)
		//{
		//	return DmDevInfoConst.OMVFS_ERR_FAILED;
		//}

		return DmDevInfoConst.OMVFS_ERR_OK;
	}

	public static void dmOmvfsEnd(tsOmVfs pVfs)
	{
		dmOmvfsDeleteVfs(pVfs.root);
	}

	public static void dmOmvfsDeleteVfs(tsDmVnode ptNode)
	{
		tsDmVnode cur = ptNode.childlist;
		tsDmVnode tmp;

		while (cur != null)
		{
			tmp = cur.next;
			dmOmvfsDeleteVfs(cur);
			cur = tmp;
		}

		if (ptNode.acl != null)
		{
			dmOmDeleteAclList(ptNode.acl);
		}
		if (ptNode.type != null)
		{
			dmOmDeleteMimeList(ptNode.type);
		}
		ptNode.name = null;
		ptNode.title = null;
		ptNode.tstamp = null;
		ptNode.ddfname = null;
		ptNode = null;
	}

	private static void dmOmvfsDeleteOmFile()
	{
		int nFileId;

		nFileId = tsdmDB.dmdbGetFileIdObjectTreeInfo();
		tsDB.dmdbDeleteFile(nFileId);

		nFileId = tsdmDB.dmdbGetFileIdObjectData();
		tsDB.dmdbDeleteFile(nFileId);
	}
}
