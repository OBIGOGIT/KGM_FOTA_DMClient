package com.tsdm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class autoStartReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
				Intent intent1 = new Intent(context, tsService.class);
/*		   if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
					context.startForegroundService(intent1);
			}else {
			   context.startService(intent1);
		   }*/
				context.startForegroundService(intent1);

			}
		}
	}
