package sk.henrichg.phoneprofiles;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ScreenOnOffBroadcastReceiver extends BroadcastReceiver {

	public void onReceive(Context context, Intent intent) {
		
		GlobalData.logE("ScreenOnOffBroadcastReceiver.onReceive","xxx");
		
		if (!GlobalData.getApplicationStarted(context))
			// application is not started
			return;

		GlobalData.logE("ScreenOnOffBroadcastReceiver.onReceive","application started");
		
		GlobalData.loadPreferences(context);

		if (intent.getAction().equals(Intent.ACTION_SCREEN_ON) ||
			intent.getAction().equals(Intent.ACTION_USER_PRESENT))
		{
			GlobalData.logE("ScreenOnOffBroadcastReceiver.onReceive","screen on");
			
			// enable/disable keyguard
			if (Keyguard.keyguardService == null)
				Keyguard.keyguardService = new Intent(context.getApplicationContext(), KeyguardService.class); 
			context.startService(Keyguard.keyguardService);
		}

	}
	
}
