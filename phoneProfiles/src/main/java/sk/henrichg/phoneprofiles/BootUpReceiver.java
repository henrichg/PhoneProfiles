package sk.henrichg.phoneprofiles;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootUpReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		
		GlobalData.loadPreferences(context);
		
		GlobalData.setApplicationStarted(context, false);
		
		if (GlobalData.applicationStartOnBoot)
		{	
			// start ReceiverService
			context.startService(new Intent(context.getApplicationContext(), ReceiversService.class));
			
			// start service for first start
			//Intent eventsServiceIntent = new Intent(context, FirstStartService.class);
			//context.startService(eventsServiceIntent);
			
			/*
			GlobalData.setApplicationStarted(context, true);

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
			
			DataWrapper dataWrapper = new DataWrapper(context, true, false, 0);
			dataWrapper.getActivateProfileHelper().initialize(null, context);
			
			dataWrapper.activateProfile(0, GlobalData.STARTUP_SOURCE_BOOT, null);
			dataWrapper.invalidateDataWrapper();

			// start PPHelper
			//PhoneProfilesHelper.startPPHelper(context);
			*/
			
		}

	}

}
