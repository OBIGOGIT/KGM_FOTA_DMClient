package com.tsdm.adapt;

public interface tsDefineUic
{
	int	UIC_SAVE_NONE				= 0x00;
	int	UIC_SAVE_OK					= 0x01;

	int	UIC_MAX_TITLE_SIZE			= 128;
	int	UIC_MAX_USERINPUT_SIZE		= 128;
	int	UIC_MAX_CHOICE_MENU			= 32;

	int	UIC_TYPE_NONE				= 0x00;
	int	UIC_TYPE_DISP				= 0x01;
	int	UIC_TYPE_CONFIRM			= 0x02;
	int	UIC_TYPE_INPUT				= 0x03;
	int	UIC_TYPE_SINGLE_CHOICE		= 0x04;
	int	UIC_TYPE_MULTI_CHOICE		= 0x05;
	int	UIC_TYPE_PROGR				= 0x06;

	int	UIC_INPUTTYPE_ALPHANUMERIC	= 0x01;
	int	UIC_INPUTTYPE_NUMERIC		= 0x02;
	int	UIC_INPUTTYPE_DATE			= 0x03;
	int	UIC_INPUTTYPE_TIME			= 0x04;
	int	UIC_INPUTTYPE_PHONENUBMER	= 0x05;
	int	UIC_INPUTTYPE_IPADDRESS		= 0x06;
	int	UIC_ECHOTYPE_TEXT			= 0x01;
	int	UIC_ECHOTYPE_PASSWORD		= 0x02;

	int	UIC_RESULT_OK				= 0x00;
	// used confirm
	int	UIC_RESULT_YES				= 0x01;
	int	UIC_RESULT_REJECT			= 0x02;

	int	UIC_RESULT_SINGLE_CHOICE	= 0x04;
	int	UIC_RESULT_MULTI_CHOICE		= 0x05;

	int	UIC_RESULT_TIMEOUT			= 0x10;
	int	UIC_RESULT_NOT_EXCUTED		= 0x11;
}
