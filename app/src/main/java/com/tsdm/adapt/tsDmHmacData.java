package com.tsdm.adapt;

public class tsDmHmacData
{
	public String	hmacAlgorithm;
	public String	hmacUserName;
	public String	hamcDigest;
	public int		httpHeaderLength;
	public int		httpContentLength;

	public tsDmHmacData()
	{
		hmacAlgorithm = "";
		hmacUserName = "";
		hamcDigest = "";
	}
}
