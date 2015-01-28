package sk.henrichg.phoneprofiles;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;


public class FirstStartService extends IntentService {

	public FirstStartService()
	{
		super("GrantRootService");
	}

	@Override
	protected void onHandleIntent(Intent intent)
	{
		Context context = getBaseContext();
		
		//Log.e("FirstStartService.onHandleIntent","xxx");

		// grant root
		//if (GlobalData.isRooted(false))
		//{
			if (GlobalData.grantRoot(true))
			{
				GlobalData.settingsBinaryExists();
				//GlobalData.getSUVersion();
			}
		//}
		
		if (GlobalData.getApplicationStarted(context))
			return;
		
		//int startType = intent.getStringExtra(GlobalData.EXTRA_FIRST_START_TYPE);
		
		GlobalData.loadPreferences(context);
		GUIData.setLanguage(context);

		
		// start ReceiverService
		context.startService(new Intent(context.getApplicationContext(), ReceiversService.class));
		
		DataWrapper dataWrapper = new DataWrapper(context, true, false, 0);
		dataWrapper.getActivateProfileHelper().initialize(null, context);

		// zrusenie notifikacie
		dataWrapper.getActivateProfileHelper().removeNotification();

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
		
	}

}
