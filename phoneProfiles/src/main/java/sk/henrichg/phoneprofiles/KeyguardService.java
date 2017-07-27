package sk.henrichg.phoneprofiles;

import android.Manifest;
import android.app.Activity;
import android.app.KeyguardManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;

//getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

public class KeyguardService extends Service {

    private static final String KEYGUARD_LOCK = "phoneProfiles.keyguardLock";

    private KeyguardManager keyguardManager;
    @SuppressWarnings("deprecation")
    private KeyguardManager.KeyguardLock keyguardLock;

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate()
    {
        PPApplication.logE("$$$ KeyguardService.onStartCommand","onCreate");
        keyguardManager = (KeyguardManager)getBaseContext().getSystemService(Activity.KEYGUARD_SERVICE);
        keyguardLock = keyguardManager.newKeyguardLock(KEYGUARD_LOCK);
    }

    @Override
    public void onDestroy()
    {
        PPApplication.logE("$$$ KeyguardService.onStartCommand", "onDestroy");
        reEnableKeyguard();
    }

    @SuppressWarnings("deprecation")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Context context = getBaseContext();

        if (!PPApplication.getApplicationStarted(context, true)) {
            reEnableKeyguard();
            stopSelf();
            return START_NOT_STICKY;
        }

        boolean isScreenOn;
        //if (android.os.Build.VERSION.SDK_INT >= 20)
        //{
        //    Display display = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        //    isScreenOn = display.getState() == Display.STATE_ON;
        //}
        //else
        //{
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        isScreenOn = pm.isScreenOn();
        //}

        boolean secureKeyguard = keyguardManager.isKeyguardSecure();
        PPApplication.logE("$$$ KeyguardService.onStartCommand","secureKeyguard="+secureKeyguard);
        if (!secureKeyguard)
        {
            PPApplication.logE("$$$ KeyguardService.onStartCommand xxx","getLockScreenDisabled="+ ActivateProfileHelper.getLockScreenDisabled(context));


            if (isScreenOn) {
                PPApplication.logE("$$$ KeyguardService.onStartCommand", "screen on");

                if (ActivateProfileHelper.getLockScreenDisabled(context)) {
                    PPApplication.logE("$$$ KeyguardService.onStartCommand", "Keyguard.disable(), START_STICKY");
                    reEnableKeyguard();
                    disableKeyguard();
                    return START_STICKY;
                } else {
                    PPApplication.logE("$$$ KeyguardService.onStartCommand", "Keyguard.reEnable(), stopSelf(), START_NOT_STICKY");
                    reEnableKeyguard();
                    stopSelf();
                    return START_NOT_STICKY;
                }
            }
            /*else {
                PPApplication.logE("$$$ KeyguardService.onStartCommand", "screen off");

                if (PPApplication.getLockScreenDisabled(context)) {
                    PPApplication.logE("$$$ KeyguardService.onStartCommand", "Keyguard.disable(), START_STICKY");

                    // re-enable with old keyguardLock
                    Keyguard.reEnable(keyguardLock);

                    // create new keyguardLock
                    //keyguardLock = keyguardManager.newKeyguardLock(KEYGUARD_LOCK);

                    stopSelf();
                    return START_NOT_STICKY;
                }
            }*/
        }

        PPApplication.logE("$$$ KeyguardService.onStartCommand"," secureKeyguard, stopSelf(), START_NOT_STICKY");
        stopSelf();
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    private void disableKeyguard()
    {
        PPApplication.logE("$$$ Keyguard.disable","keyguardLock="+keyguardLock);
        if ((keyguardLock != null) && Permissions.hasPermission(getBaseContext(), Manifest.permission.DISABLE_KEYGUARD))
            keyguardLock.disableKeyguard();
    }

    private void reEnableKeyguard()
    {
        PPApplication.logE("$$$ Keyguard.reEnable","keyguardLock="+keyguardLock);
        if ((keyguardLock != null) && Permissions.hasPermission(getBaseContext(), Manifest.permission.DISABLE_KEYGUARD))
            keyguardLock.reenableKeyguard();
    }

}
