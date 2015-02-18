package sk.henrichg.phoneprofiles;

import android.app.KeyguardManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;


public class KeyguardService extends Service {

	@Override
    public void onCreate()
	{
		Keyguard.initialize(getApplicationContext());
	}
	 
	@Override
    public void onDestroy()
	{
    }
	 
	@Override
    public int onStartCommand(Intent intent, int flags, int startId)
	{
		GlobalData.logE("KeyguardService.onStartCommand","xxx");
		
		Context context = getApplicationContext();
		
		KeyguardManager kgMgr = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
		if (!kgMgr.inKeyguardRestrictedInputMode())
        //if (!kgMgr.isKeyguardSecure())
		{
			GlobalData.logE("KeyguardService.onStartCommand","not keyguard restructed");
			
			DataWrapper dataWrapper = new DataWrapper(context, false, false, 0);
			Profile profile = dataWrapper.getActivatedProfile();
			profile = GlobalData.getMappedProfile(profile, context);

			if (profile != null)
			{
				// zapnutie/vypnutie lockscreenu
				//getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
				switch (profile._deviceKeyguard) {
					case 1:
						GlobalData.logE("KeyguardService.onStartCommand","profile keyguard = 1");
						Keyguard.reenable();
						stopSelf();
						return START_NOT_STICKY;
					case 2:
						GlobalData.logE("KeyguardService.onStartCommand","profile keyguard = 2");
						Keyguard.reenable();
						Keyguard.disable();
				        return START_STICKY;
				}
			}
		}

		stopSelf();
		return START_NOT_STICKY;
    }
	
	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

}
