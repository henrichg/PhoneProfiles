package sk.henrichg.phoneprofiles;

import android.app.KeyguardManager.KeyguardLock;

public class Keyguard {

	//public static Intent keyguardService = null;
	//private static KeyguardManager keyguardManager = null;
	//private static KeyguardLock keyguardLock = null;
    
	//static final String KEYGUARD_LOCK = "phoneProfiles.keyguardLock";

    /*
	public static void initialize(Context context)
	{
	    if (keyguardManager == null)
	    	keyguardManager = (KeyguardManager)context.getSystemService(Activity.KEYGUARD_SERVICE);
	    if (keyguardLock == null)
	    	keyguardLock = keyguardManager.newKeyguardLock(KEYGUARD_LOCK);
	}
	
	public static void destroy()
	{
		if (keyguardLock != null)
		{
			keyguardLock.reenableKeyguard();
			keyguardLock = null;
		}
		if (keyguardManager != null)
			keyguardManager = null;
	}
	*/

	public static void disable(KeyguardLock keyguardLock)
	{
		if (keyguardLock != null)
			keyguardLock.disableKeyguard();
	}
	
	public static void reenable(KeyguardLock keyguardLock)
	{
		if (keyguardLock != null)
			keyguardLock.reenableKeyguard();
	}
	
}
