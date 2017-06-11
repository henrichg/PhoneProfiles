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
import android.support.v4.content.LocalBroadcastManager;


public class PhoneProfilesService extends Service {

    public static PhoneProfilesService instance = null;

    private ScreenOnOffBroadcastReceiver screenOnOffReceiver = null;
    private InterruptionFilterChangedBroadcastReceiver interruptionFilterChangedReceiver = null;

    private static SettingsContentObserver settingsContentObserver = null;

    private RefreshGUIBroadcastReceiver refreshGUIBroadcastReceiver = null;
    private DashClockBroadcastReceiver dashClockBroadcastReceiver = null;

    public static String connectToSSID = Profile.CONNECTTOSSID_JUSTANY;

    @Override
    public void onCreate()
    {
        super.onCreate();

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

        if (screenOnOffReceiver != null)
            getApplicationContext().unregisterReceiver(screenOnOffReceiver);
        screenOnOffReceiver = new ScreenOnOffBroadcastReceiver();
        IntentFilter intentFilter5 = new IntentFilter();
        intentFilter5.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter5.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter5.addAction(Intent.ACTION_USER_PRESENT);
        getApplicationContext().registerReceiver(screenOnOffReceiver, intentFilter5);

        if (android.os.Build.VERSION.SDK_INT >= 23) {
            boolean no60 = !Build.VERSION.RELEASE.equals("6.0");
            if (no60 && GlobalGUIRoutines.activityActionExists(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS, getApplicationContext())) {
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

        if (refreshGUIBroadcastReceiver != null)
            LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(refreshGUIBroadcastReceiver);
        refreshGUIBroadcastReceiver = new RefreshGUIBroadcastReceiver();
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(refreshGUIBroadcastReceiver, new IntentFilter("RefreshGUIBroadcastReceiver"));

        if (dashClockBroadcastReceiver != null)
            LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(dashClockBroadcastReceiver);
        dashClockBroadcastReceiver = new DashClockBroadcastReceiver();
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(dashClockBroadcastReceiver, new IntentFilter("DashClockBroadcastReceiver"));

        // start service for first start
        Intent firstStartServiceIntent = new Intent(getApplicationContext(), FirstStartService.class);
        getApplicationContext().startService(firstStartServiceIntent);

    }

    @Override
    public void onDestroy()
    {
        if (screenOnOffReceiver != null)
            getApplicationContext().unregisterReceiver(screenOnOffReceiver);

        if (android.os.Build.VERSION.SDK_INT >= 23)
            if (interruptionFilterChangedReceiver != null)
                getApplicationContext().unregisterReceiver(interruptionFilterChangedReceiver);

        if (settingsContentObserver != null)
            getContentResolver().unregisterContentObserver(settingsContentObserver);

        if (refreshGUIBroadcastReceiver != null)
            LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(refreshGUIBroadcastReceiver);
        if (dashClockBroadcastReceiver != null)
            LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(dashClockBroadcastReceiver);

        instance = null;

        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        super.onStartCommand(intent, flags, startId);

        PPApplication.logE("PhoneProfilesService.onStartCommand", "xxx");

        //Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        ActivateProfileHelper.setMergedRingNotificationVolumes(getApplicationContext(), false);

        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent)
    {
        PPApplication.logE("$$$ PhoneProfilesService.onTaskRemoved", "xxxxx");

        if (PPApplication.screenTimeoutHandler != null) {
            PPApplication.screenTimeoutHandler.post(new Runnable() {
                public void run() {
                    ActivateProfileHelper.screenTimeoutUnlock(getApplicationContext());
                }
            });
        }// else
        //    ActivateProfileHelper.screenTimeoutUnlock(getApplicationContext());

        super.onTaskRemoved(rootIntent);
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

}
