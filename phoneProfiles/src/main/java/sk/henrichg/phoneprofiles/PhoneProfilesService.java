package sk.henrichg.phoneprofiles;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.TelephonyManager;

import com.commonsware.cwac.wakeful.WakefulIntentService;
import com.crashlytics.android.Crashlytics;


public class PhoneProfilesService extends Service {

    public static PhoneProfilesService instance = null;

    private ScreenOnOffBroadcastReceiver screenOnOffReceiver = null;
    private InterruptionFilterChangedBroadcastReceiver interruptionFilterChangedReceiver = null;
    private PhoneCallBroadcastReceiver phoneCallBroadcastReceiver = null;
    private RingerModeChangeReceiver ringerModeChangeReceiver = null;
    private WifiStateChangedBroadcastReceiver wifiStateChangedBroadcastReceiver = null;

    private static SettingsContentObserver settingsContentObserver = null;

    public static String connectToSSID = Profile.CONNECTTOSSID_JUSTANY;

    @Override
    public void onCreate()
    {
        super.onCreate();

        PPApplication.logE("PhoneProfilesService.onCreate", "xxx");

        instance = this;
        Context appContext = getApplicationContext();

        Crashlytics.setBool(ApplicationPreferences.PREF_NOTIFICATION_STATUS_BAR, ApplicationPreferences.notificationStatusBar(appContext));
        Crashlytics.setBool(ApplicationPreferences.PREF_NOTIFICATION_STATUS_BAR_PERMANENT, ApplicationPreferences.notificationStatusBarPermanent(appContext));
        Crashlytics.setBool(ApplicationPreferences.PREF_NOTIFICATION_SHOW_IN_STATUS_BAR, ApplicationPreferences.notificationShowInStatusBar(appContext));

        // save version code (is used in PackageReplacedReceiver)
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            int actualVersionCode = pInfo.versionCode;
            PPApplication.setSavedVersionCode(appContext, actualVersionCode);
        } catch (PackageManager.NameNotFoundException e) {
            //e.printStackTrace();
        }

        if (screenOnOffReceiver != null)
            appContext.unregisterReceiver(screenOnOffReceiver);
        screenOnOffReceiver = new ScreenOnOffBroadcastReceiver();
        IntentFilter intentFilter5 = new IntentFilter();
        intentFilter5.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter5.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter5.addAction(Intent.ACTION_USER_PRESENT);
        appContext.registerReceiver(screenOnOffReceiver, intentFilter5);

        if (android.os.Build.VERSION.SDK_INT >= 23) {
            boolean no60 = !Build.VERSION.RELEASE.equals("6.0");
            if (no60 && GlobalGUIRoutines.activityActionExists(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS, appContext)) {
                interruptionFilterChangedReceiver = new InterruptionFilterChangedBroadcastReceiver();
                IntentFilter intentFilter11 = new IntentFilter();
                intentFilter11.addAction(NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED);
                appContext.registerReceiver(interruptionFilterChangedReceiver, intentFilter11);
            }
        }

        if (phoneCallBroadcastReceiver != null)
            appContext.unregisterReceiver(phoneCallBroadcastReceiver);
        phoneCallBroadcastReceiver = new PhoneCallBroadcastReceiver();
        IntentFilter intentFilter6 = new IntentFilter();
        intentFilter6.addAction(Intent.ACTION_NEW_OUTGOING_CALL);
        intentFilter6.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        appContext.registerReceiver(phoneCallBroadcastReceiver, intentFilter6);

        if (ringerModeChangeReceiver != null)
            appContext.unregisterReceiver(ringerModeChangeReceiver);
        ringerModeChangeReceiver = new RingerModeChangeReceiver();
        IntentFilter intentFilter7 = new IntentFilter();
        intentFilter7.addAction(AudioManager.RINGER_MODE_CHANGED_ACTION);
        appContext.registerReceiver(ringerModeChangeReceiver, intentFilter7);

        if (wifiStateChangedBroadcastReceiver != null)
            appContext.unregisterReceiver(wifiStateChangedBroadcastReceiver);
        wifiStateChangedBroadcastReceiver = new WifiStateChangedBroadcastReceiver();
        IntentFilter intentFilter8 = new IntentFilter();
        intentFilter8.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        appContext.registerReceiver(wifiStateChangedBroadcastReceiver, intentFilter8);

        if (settingsContentObserver != null)
            appContext.getContentResolver().unregisterContentObserver(settingsContentObserver);
        //settingsContentObserver = new SettingsContentObserver(this, new Handler(getMainLooper()));
        settingsContentObserver = new SettingsContentObserver(appContext, new Handler());
        appContext.getContentResolver().registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, settingsContentObserver);

        // start service for first start
        Intent firstStartServiceIntent = new Intent(appContext, FirstStartService.class);
        WakefulIntentService.sendWakefulWork(appContext, firstStartServiceIntent);
    }

    @Override
    public void onDestroy()
    {
        Context appContext = getApplicationContext();

        if (screenOnOffReceiver != null)
            appContext.unregisterReceiver(screenOnOffReceiver);
        if (android.os.Build.VERSION.SDK_INT >= 23)
            if (interruptionFilterChangedReceiver != null)
                appContext.unregisterReceiver(interruptionFilterChangedReceiver);
        if (phoneCallBroadcastReceiver != null)
            appContext.unregisterReceiver(phoneCallBroadcastReceiver);
        if (ringerModeChangeReceiver != null)
            appContext.unregisterReceiver(ringerModeChangeReceiver);
        if (wifiStateChangedBroadcastReceiver != null)
            appContext.unregisterReceiver(wifiStateChangedBroadcastReceiver);

        if (settingsContentObserver != null)
            appContext.getContentResolver().unregisterContentObserver(settingsContentObserver);

        instance = null;

        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        super.onStartCommand(intent, flags, startId);

        PPApplication.logE("PhoneProfilesService.onStartCommand", "xxx");

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
