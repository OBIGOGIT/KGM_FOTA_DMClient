package com.tsdm.adapt;

import java.io.IOException;

public class tsOmlib extends tsOmVfs
{
	public static int dmOmInit(tsOmTree ptOmt)
	{
		int ret = 0;

		ret = (int) dmOmvfsInit(ptOmt.vfs);
		if (ret == 0)
			return 0;

		return -3;
	}

	public static int dmOmEnd(tsOmTree ptOmt)
	{
		if (ptOmt == null)
		{
			return -3;
		}

		try
		{
			dmOmvfsSaveFs(ptOmt.vfs);
		}
		catch (IOException e)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, e.toString());
		}
		return 0;
	}

	public static int dmOmSetServerId(tsOmTree ptOmt, String sid)
	{
		if (sid == null || sid.charAt(0) == '\0' || sid.length() > 20 - 1)
		{
			return -3;
		}

		ptOmt.serverid = sid;
		return 0;
	}

	public static tsDmVnode dmOmLibGetNodeProp(tsOmTree ptOmt, String pPath)
	{
		return dmOmvfsPath2Node(ptOmt.vfs, pPath);
	}


	public static int dmOmWrite(tsOmTree ptOmt, String pPath, int nTotalSize, int nOffset, Object pData, int nSize)
	{
		int ret;
		ret = dmOmvfsCreatePath(ptOmt.vfs, pPath);
		if (pData != null && nSize > 0)
		{
			ret = dmOmvfsWriteObj(ptOmt.vfs, pPath, nTotalSize, nOffset, pData, nSize);
			if (ret < 0)
			{
				return -3;
			}
		}

		return nSize;
	}

	public static int dmOmRead(tsOmTree ptOmt, String pPath, int nOffset, char[] databuf, int bufsize)
	{
		int ret;
		if (!dmOmvfsCheckPath(ptOmt.vfs, pPath))
		{
			return -1;
		}
		ret = dmOmvfsReadObj(ptOmt.vfs, pPath, nOffset, databuf, bufsize);
		if (ret < 0)
		{
			return -3;
		}

		return ret;
	}

	public static boolean dmOmvfsCheckPath(tsOmVfs pVfs, String pPath)
	{
		tsDmVnode ptNode;

		ptNode = dmOmvfsPath2Node(pVfs, pPath);
		if (ptNode == null)
		{
			return false;
		}

		return true;
	}

	public static int dmOmvfsReadObj(tsOmVfs pVfs, String pPath, int nOffset, char[] pBuff, int nBuffSize)
	{
		tsDmVnode ptNode;
		int ret;
		int blocksize;

		ptNode = dmOmvfsPath2Node(pVfs, pPath);
		if (ptNode == null)
		{
			return OMVFS_ERR_FAILED;
		}
		if (ptNode.size > 0 && ptNode.vaddr >= 0)
		{
			blocksize = nBuffSize + nOffset;
			if (blocksize > ptNode.size)
			{
				nBuffSize -= (blocksize - ptNode.size);
			}
			ret = dmOmvfsLoadFsData(pVfs, ptNode, (int) (ptNode.vaddr + nOffset), pBuff, nBuffSize);
			if (ret != OMVFS_ERR_OK)
			{
				return OMVFS_ERR_FAILED;
			}
		}
		else
		{
			return OMVFS_ERR_FAILED;
		}

		return nBuffSize;
	}

	public static int dmOmlibDelete(tsOmTree ptOmt, String pPath, boolean deletechild)
	{
		tsDmVnode node;
		int ret;

		node = dmOmvfsPath2Node(ptOmt.vfs, pPath);
		if (node == null)
		{
			return -3;
		}

		if (!dmOmCheckAcl(ptOmt, node, OMACL_DELETE))
		{
			return -5;
		}

		ret = dmOmvfsRemoveNode(ptOmt.vfs, node, deletechild);
		if (ret != OMVFS_ERR_OK)
		{
			return -3;
		}

		return 0;
	}


	public static boolean dmOmCheckAcl(tsOmTree ptOmt, tsDmVnode ptNode, int nAcl)
	{
		String pszServerId = ptOmt.serverid;

		if ((ptOmt == null) || (ptNode == null))
			return false;

		if (!dmOmCheckNodeAcl(ptNode, nAcl, pszServerId))
		{
			return false;
		}

		return true;
	}


	public static boolean dmOmCheckNodeAcl(tsDmVnode ptNode, int nAcl, String pszServerId)
	{
		tsOmList ptList = null;
		tsOmAcl ptAcl = null;

		ptList = ptNode.acl;

		if (ptList == null)
		{
			return false;
		}

		while (ptList != null)
		{
			ptAcl = (tsOmAcl) (ptList.data);

			if ((ptAcl.serverid.compareTo(pszServerId) == 0 || ptAcl.serverid.compareTo("*") == 0))
			{
				if (ptAcl.ac == 0)
				{
					return dmOmCheckNodeAcl(ptNode.ptParentNode, nAcl, pszServerId);
				}
				else
				{
					if ((ptAcl.ac & nAcl) == nAcl)
					{
						return true;
					}
				}
			}

			ptList = ptList.next;
		}

		return false;
	}


	public static int dmOmGetChild(tsOmTree om, String pPath, String[] bufs, int maxnum)
	{
		tsDmVnode node;
		tsDmVnode cur;
		int i = 0;

		node = dmOmvfsPath2Node(om.vfs, pPath);
		if (node == null)
		{
			return -6;
		}

		cur = node.childlist;

		while (cur != null)
		{
			if (i >= maxnum)
			{
				return maxnum;
			}

			if (cur.name == null)
			{
				return -3;
			}
			bufs[i] = cur.name;

			cur = cur.next;
			i++;
		}

		return i;
	}

	public static boolean dmOmCheckAclCurrentNode(tsOmTree ptOmt, String pPath, int action)
	{
		String ptr = null;
		String nodename = null;
		tsDmVnode node;
		tsDmVnode basenode = ptOmt.vfs.root;
		String sid = ptOmt.serverid;
		boolean rootcheck = false;

		int searchSlash = 0;

		searchSlash = pPath.indexOf("/");
		if (searchSlash >= 0)
		{
			nodename = pPath.substring(0, searchSlash);
			ptr = pPath.substring(searchSlash + 1);
		}

		tsLib.debugPrint(DEBUG_DM, "strnodename :" + nodename + ", ptr :" + ptr);

		while (!tsLib.isEmpty(ptr))
		{
			if (tsLib.isEmpty(nodename))
			{
				if (!rootcheck && (ptr.charAt(0) == '/' || ptr.charAt(0) == '.'))
				{
					rootcheck = true;
					node = ptOmt.vfs.root;
				}
				else
				{
					searchSlash = ptr.indexOf("/");
					if (searchSlash >= 0)
					{
						nodename = ptr.substring(0, searchSlash);
						ptr = ptr.substring(searchSlash + 1);
						tsLib.debugPrint(DEBUG_DM, "strnodename :" + nodename + ", ptr :" + ptr);
					}
					continue;
				}
			}

			if (tsLib.isEmpty(ptr) && action == OMACL_ADD)
			{
				return true;
			}

			if (nodename.equals("."))
			{
				node = ptOmt.vfs.root;
			}
			else
			{
				node = tsOmVfs.dmOmvfsGetNode(ptOmt.vfs, nodename, basenode);
				if (node == null)
				{
					return false;
				}
			}

			if (tsLib.isEmpty(ptr))
			{
				if (!dmOmCheckNodeAcl(node, action, sid))
				{
					return false;
				}
			}

			basenode = node;
			searchSlash = ptr.indexOf("/");
			if (searchSlash >= 0)
			{
				nodename = ptr.substring(0, searchSlash);
				ptr = ptr.substring(searchSlash + 1);
				tsLib.debugPrint(DEBUG_DM, "strnodename :" + nodename + ", ptr :" + ptr);
			}
			else
			{
				return true;
			}
		}
		return true;
	}

	public static void dmOmMakeParentPath(String in, char[] out)
	{
		int i, len;
		int pos = -1;

		if (in == null)
			return;
		len = in.length();
		for (i = len - 1; i >= 0; i--)
		{
			if (in.charAt(i) == '/')
			{
				pos = i;
				break;
			}
		}

		if (pos < 0)
		{
			out[0] = '\0';
			return;
		}

		for (i = 0; i < pos; i++)
		{
			out[i] = in.charAt(i);
		}
		out[i] = '\0';
	}

	public static boolean dmOmProcessCmdImplicitAdd(Object pOM, String pNodeName, int aclValue, int bStart)
	{
		char[] parentNode = null;
		String parentnodename = null;
		tsDmVnode node = null;
		tsOmTree om = (tsOmTree) pOM;

		if (pNodeName.contains(".") == false)
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, "ROOT NODE not found");
			return false;
		}

		if (bStart > 0)
		{
			if (!dmOmCheckNodePathDepth(pNodeName))
			{
				return false;
			}
		}

		parentNode = new char[256];// DEFAULT_BUFFER_SIZE_4;

		dmOmMakeParentPath(pNodeName, parentNode);
		parentnodename = tsLib.libString(parentNode);
		node = dmOmLibGetNodeProp(om, parentnodename);

		if (node == null)
		{
			dmOmProcessCmdImplicitAdd(om, parentnodename, aclValue, 0);
		}

		dmOmWrite(om, pNodeName, 0, 0, "", 0);
		dmOmDefaultACL(om, pNodeName, aclValue, SCOPE_DYNAMIC);

		return true;
	}
	public static boolean dmOmCheckNodePathDepth(String pNodePath)
	{
		int nCount = 0;
		int index = 0;
		String string = pNodePath;

		for (index = 0; index < string.length(); index++)
		{
			if (string.charAt(index) == '/')
			{
				nCount++;
			}
			// string++;
		}

		if (nCount > 15)
		{
			return false;
		}

		return true;
	}

	public static void dmOmDefaultACL(Object pOM, String pPath, int aclValue, int scope)
	{
		tsOmTree om = (tsOmTree) pOM;
		tsOmAcl acl;
		tsOmList item;
		tsDmVnode node;

		node = dmOmLibGetNodeProp(om, pPath);
		if (node != null)
		{
			item = node.acl;
			acl = (tsOmAcl) item.data;
			acl.ac = aclValue;
			node.scope = scope;
		}
		else
		{
			tsLib.debugPrintException(DEBUG_EXCEPTION, "Not Exist");
		}
	}

	public static int dmOmlibDeleteImplicit(tsOmTree ptOmt, String pPath, boolean deletechild)
	{
		tsDmVnode node;
		int ret;

		node = dmOmvfsPath2Node(ptOmt.vfs, pPath);
		if (node == null)
		{
			return -3;
		}

		ret = dmOmvfsRemoveNode(ptOmt.vfs, node, deletechild);
		if (ret != OMVFS_ERR_OK)
		{
			return -3;
		}

		return 0;
	}

}
