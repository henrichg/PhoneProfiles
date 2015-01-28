package sk.henrichg.phoneprofiles;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PackageReplacedReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {

		int intentUid = intent.getExtras().getInt("android.intent.extra.UID");
		int myUid = android.os.Process.myUid();
		if (intentUid == myUid)
		{
			GlobalData.loadPreferences(context);
			
			if (GlobalData.getApplicationStarted(context))
			{
				// must by false for avoiding starts/pause events before restart events
				GlobalData.setApplicationStarted(context, false); 
				
				// start ReceiverService
				context.startService(new Intent(context.getApplicationContext(), ReceiversService.class));
				
				// start service for first start
				//Intent eventsServiceIntent = new Intent(context, FirstStartService.class);
				//context.startService(eventsServiceIntent);
				
				/*
				DataWrapper dataWrapper = new DataWrapper(context, true, false, 0);
				dataWrapper.getActivateProfileHelper().initialize(null, context);

				// zrusenie notifikacie
				dataWrapper.getActivateProfileHelper().removeNotification();

				// grant root
				Intent eventsServiceIntent = new Intent(context, GrantRootService.class);
				context.startService(eventsServiceIntent);
				
				// show notification about upgrade PPHelper
				//if (GlobalData.isRooted(false))
				//{
					if (!PhoneProfilesHelper.isPPHelperInstalled(context, PhoneProfilesHelper.PPHELPER_CURRENT_VERSION))
					{
						// proper PPHelper version is not installed
						if (PhoneProfilesHelper.PPHelperVersion != -1)
						{
							// PPHelper is installed, show notification 
							PhoneProfilesHelper.showPPHelperUpgradeNotification(context);							
						}
					}
				//}

				GlobalData.setApplicationStarted(context, true);
					
				dataWrapper.activateProfile(0, GlobalData.STARTUP_SOURCE_BOOT, null);
				dataWrapper.invalidateDataWrapper();

				// start PPHelper
				//PhoneProfilesHelper.startPPHelper(context);
				*/
				
			}

		}		
	}

}
