package sk.henrichg.phoneprofiles;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;


public class PhoneProfilesService extends Service {

    public static PhoneProfilesService instance = null;

    private final ScreenOnOffBroadcastReceiver screenOnOffReceiver = new ScreenOnOffBroadcastReceiver();
    private InterruptionFilterChangedBroadcastReceiver interruptionFilterChangedReceiver = null;

    private static SettingsContentObserver settingsContentObserver = null;

    @Override
    public void onCreate()
    {
        PPApplication.logE("PhoneProfilesService.onCreate", "xxx");

        //Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        instance = this;

        // save version code (is used in PackageReplacedReceiver)
        try {
            PackageInfo pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            int actualVersionCode = pinfo.versionCode;
            PPApplication.setSavedVersionCode(getApplicationContext(), actualVersionCode);
        } catch (PackageManager.NameNotFoundException e) {
            //e.printStackTrace();
        }

        IntentFilter intentFilter5 = new IntentFilter();
        intentFilter5.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter5.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter5.addAction(Intent.ACTION_USER_PRESENT);
        getApplicationContext().registerReceiver(screenOnOffReceiver, intentFilter5);

        if (android.os.Build.VERSION.SDK_INT >= 23) {
            boolean no60 = !Build.VERSION.RELEASE.equals("6.0");
            if (no60) {
                interruptionFilterChangedReceiver = new InterruptionFilterChangedBroadcastReceiver();
                IntentFilter intentFilter11 = new IntentFilter();
                intentFilter11.addAction(NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED);
                getApplicationContext().registerReceiver(interruptionFilterChangedReceiver, intentFilter11);
            }
        }

        if (settingsContentObserver != null)
            getContentResolver().unregisterContentObserver(settingsContentObserver);
        //settingsContentObserver = new SettingsContentObserver(this, new Handler(getMainLooper()));
        settingsContentObserver = new SettingsContentObserver(this, new Handler());
        getContentResolver().registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, settingsContentObserver);

        // start service for first start
        Intent firstStartServiceIntent = new Intent(getApplicationContext(), FirstStartService.class);
        getApplicationContext().startService(firstStartServiceIntent);

    }

    @Override
    public void onDestroy()
    {
        getApplicationContext().unregisterReceiver(screenOnOffReceiver);
        if (android.os.Build.VERSION.SDK_INT >= 23)
            if (interruptionFilterChangedReceiver != null)
                getApplicationContext().unregisterReceiver(interruptionFilterChangedReceiver);

        if (settingsContentObserver != null)
            getContentResolver().unregisterContentObserver(settingsContentObserver);

        instance = null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        PPApplication.logE("PhoneProfilesService.onStartCommand", "xxx");

        //Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        PPApplication.setMergedRingNotificationVolumes(getApplicationContext(), false);

        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent)
    {
        PPApplication.logE("$$$ PhoneProfilesService.onTaskRemoved", "xxxxx");

        ActivateProfileHelper.screenTimeoutUnlock(getApplicationContext());
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

}
