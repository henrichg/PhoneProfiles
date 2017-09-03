package sk.henrichg.phoneprofiles;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.RemoteViews;

import com.commonsware.cwac.wakeful.WakefulIntentService;
import com.crashlytics.android.Crashlytics;

import java.util.Calendar;


public class PhoneProfilesService extends Service {

    public static PhoneProfilesService instance = null;
    private static boolean serviceRunning = false;

    private ScreenOnOffBroadcastReceiver screenOnOffReceiver = null;
    private InterruptionFilterChangedBroadcastReceiver interruptionFilterChangedReceiver = null;
    private PhoneCallBroadcastReceiver phoneCallBroadcastReceiver = null;
    private RingerModeChangeReceiver ringerModeChangeReceiver = null;
    private WifiStateChangedBroadcastReceiver wifiStateChangedBroadcastReceiver = null;

    private static SettingsContentObserver settingsContentObserver = null;

    public static String connectToSSID = Profile.CONNECTTOSSID_JUSTANY;

    static final String EXTRA_SET_SERVICE_FOREGROUND = "set_service_foreground";
    static final String EXTRA_CLEAR_SERVICE_FOREGROUND = "clear_service_foreground";

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
        } catch (Exception e) {
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

        removeProfileNotification(this);

        instance = null;
        serviceRunning = false;

        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        PPApplication.logE("PhoneProfilesService.onStartCommand", "xxx");

        serviceRunning = true;

        ActivateProfileHelper.setMergedRingNotificationVolumes(getApplicationContext(), false);

        final DataWrapper dataWrapper =  new DataWrapper(this, true, false, 0);
        dataWrapper.getActivateProfileHelper().initialize(dataWrapper, getApplicationContext());
        Profile activatedProfile = dataWrapper.getActivatedProfile();
        showProfileNotification(activatedProfile, dataWrapper);

        if (intent != null) {
            if (intent.getBooleanExtra(EXTRA_SET_SERVICE_FOREGROUND, false)) {
                PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "EXTRA_SET_SERVICE_FOREGROUND");
                dataWrapper.getActivateProfileHelper().initialize(dataWrapper, getApplicationContext());
                showProfileNotification(activatedProfile, dataWrapper);
            }

            if (intent.getBooleanExtra(EXTRA_CLEAR_SERVICE_FOREGROUND, false)) {
                PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "EXTRA_CLEAR_SERVICE_FOREGROUND");
                removeProfileNotification(this);
            }
        }

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

    // profile notification -------------------------------------------

    @SuppressLint("NewApi")
    public void showProfileNotification(Profile profile, DataWrapper dataWrapper)
    {
        if (ActivateProfileHelper.lockRefresh)
            // no refresh notification
            return;

        if (serviceRunning && ApplicationPreferences.notificationStatusBar(dataWrapper.context))
        {
            // close showed notification
            //notificationManager.cancel(PPApplication.NOTIFICATION_ID);
            // vytvorenie intentu na aktivitu, ktora sa otvori na kliknutie na notifikaciu
            Intent intent = new Intent(dataWrapper.context, ActivateProfileActivity.class);
            // clear all opened activities
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            // nastavime, ze aktivita sa spusti z notifikacnej listy
            intent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_NOTIFICATION);
            PendingIntent pIntent = PendingIntent.getActivity(dataWrapper.context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

            // vytvorenie samotnej notifikacie
            Notification.Builder notificationBuilder;
            RemoteViews contentView;
            if (ApplicationPreferences.notificationTheme(dataWrapper.context).equals("1"))
                contentView = new RemoteViews(dataWrapper.context.getPackageName(), R.layout.notification_drawer_dark);
            else
            if (ApplicationPreferences.notificationTheme(dataWrapper.context).equals("2"))
                contentView = new RemoteViews(dataWrapper.context.getPackageName(), R.layout.notification_drawer_light);
            else
                contentView = new RemoteViews(dataWrapper.context.getPackageName(), R.layout.notification_drawer);

            boolean isIconResourceID;
            String iconIdentifier;
            String profileName;
            Bitmap iconBitmap;
            Bitmap preferencesIndicator;

            if (profile != null)
            {
                isIconResourceID = profile.getIsIconResourceID();
                iconIdentifier = profile.getIconIdentifier();
                profileName = profile.getProfileNameWithDuration(false, dataWrapper.context);
                iconBitmap = profile._iconBitmap;
                preferencesIndicator = profile._preferencesIndicator;
            }
            else
            {
                isIconResourceID = true;
                iconIdentifier = Profile.PROFILE_ICON_DEFAULT;
                profileName = dataWrapper.context.getResources().getString(R.string.profiles_header_profile_name_no_activated);
                iconBitmap = null;
                preferencesIndicator = null;
            }

            notificationBuilder = new Notification.Builder(dataWrapper.context)
                    .setContentIntent(pIntent);

            if (ApplicationPreferences.notificationShowInStatusBar(dataWrapper.context)) {
                KeyguardManager myKM = (KeyguardManager) dataWrapper.context.getSystemService(Context.KEYGUARD_SERVICE);
                //boolean screenUnlocked = !myKM.inKeyguardRestrictedInputMode();
                boolean screenUnlocked = !myKM.isKeyguardLocked();
                //boolean screenUnlocked = getScreenUnlocked(context);
                if ((ApplicationPreferences.notificationHideInLockScreen(dataWrapper.context) && (!screenUnlocked)) ||
                        ((profile != null) && profile._hideStatusBarIcon))
                    notificationBuilder.setPriority(Notification.PRIORITY_MIN);
                else
                    notificationBuilder.setPriority(Notification.PRIORITY_DEFAULT);
            }
            else
                notificationBuilder.setPriority(Notification.PRIORITY_MIN);
            //notificationBuilder.setPriority(Notification.PRIORITY_HIGH); // for heads-up in Android 5.0
            if (android.os.Build.VERSION.SDK_INT >= 21)
            {
                notificationBuilder.setCategory(Notification.CATEGORY_STATUS);
                notificationBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
            }

            notificationBuilder.setTicker(profileName);

            if (isIconResourceID)
            {
                int iconSmallResource;
                if (iconBitmap != null) {
                    if (ApplicationPreferences.notificationStatusBarStyle(dataWrapper.context).equals("0")) {
                        // colorful icon

                        // FC in Note 4, 6.0.1 :-/
                        String manufacturer = PPApplication.getROMManufacturer();
                        boolean isNote4 = (manufacturer != null) && (manufacturer.compareTo("samsung") == 0) &&
                                /*(Build.MODEL.startsWith("SM-N910") ||  // Samsung Note 4
                                 Build.MODEL.startsWith("SM-G900")     // Samsung Galaxy S5
                                ) &&*/
                                (android.os.Build.VERSION.SDK_INT == 23);
                        //Log.d("ActivateProfileHelper.showNotification","isNote4="+isNote4);
                        if ((android.os.Build.VERSION.SDK_INT >= 23) && (!isNote4)) {
                            notificationBuilder.setSmallIcon(ColorNotificationIcon.getFromBitmap(iconBitmap));
                        }
                        else {
                            iconSmallResource = dataWrapper.context.getResources().getIdentifier(iconIdentifier + "_notify_color", "drawable", dataWrapper.context.getPackageName());
                            if (iconSmallResource == 0)
                                iconSmallResource = R.drawable.ic_profile_default;
                            notificationBuilder.setSmallIcon(iconSmallResource);
                        }
                    }
                    else {
                        // native icon
                        iconSmallResource = dataWrapper.context.getResources().getIdentifier(iconIdentifier + "_notify", "drawable", dataWrapper.context.getPackageName());
                        if (iconSmallResource == 0)
                            iconSmallResource = R.drawable.ic_profile_default_notify;
                        notificationBuilder.setSmallIcon(iconSmallResource);
                    }

                    contentView.setImageViewBitmap(R.id.notification_activated_profile_icon, iconBitmap);
                }
                else {
                    if (ApplicationPreferences.notificationStatusBarStyle(dataWrapper.context).equals("0")) {
                        // colorful icon
                        iconSmallResource = dataWrapper.context.getResources().getIdentifier(iconIdentifier + "_notify_color", "drawable", dataWrapper.context.getPackageName());
                        if (iconSmallResource == 0)
                            iconSmallResource = R.drawable.ic_profile_default;
                        notificationBuilder.setSmallIcon(iconSmallResource);

                        int iconLargeResource = dataWrapper.context.getResources().getIdentifier(iconIdentifier, "drawable", dataWrapper.context.getPackageName());
                        if (iconLargeResource == 0)
                            iconLargeResource = R.drawable.ic_profile_default;
                        Bitmap largeIcon = BitmapFactory.decodeResource(dataWrapper.context.getResources(), iconLargeResource);
                        contentView.setImageViewBitmap(R.id.notification_activated_profile_icon, largeIcon);
                    } else {
                        // native icon
                        iconSmallResource = dataWrapper.context.getResources().getIdentifier(iconIdentifier + "_notify", "drawable", dataWrapper.context.getPackageName());
                        if (iconSmallResource == 0)
                            iconSmallResource = R.drawable.ic_profile_default_notify;
                        notificationBuilder.setSmallIcon(iconSmallResource);

                        int iconLargeResource = dataWrapper.context.getResources().getIdentifier(iconIdentifier, "drawable", dataWrapper.context.getPackageName());
                        if (iconLargeResource == 0)
                            iconLargeResource = R.drawable.ic_profile_default;
                        Bitmap largeIcon = BitmapFactory.decodeResource(dataWrapper.context.getResources(), iconLargeResource);
                        contentView.setImageViewBitmap(R.id.notification_activated_profile_icon, largeIcon);
                    }
                }
            }
            else
            {
                // FC in Note 4, 6.0.1 :-/
                String manufacturer = PPApplication.getROMManufacturer();
                boolean isNote4 = (manufacturer != null) && (manufacturer.compareTo("samsung") == 0) &&
                        /*(Build.MODEL.startsWith("SM-N910") ||  // Samsung Note 4
                         Build.MODEL.startsWith("SM-G900")     // Samsung Galaxy S5
                        ) &&*/
                        (android.os.Build.VERSION.SDK_INT == 23);
                //Log.d("ActivateProfileHelper.showNotification","isNote4="+isNote4);
                if ((Build.VERSION.SDK_INT >= 23) && (!isNote4) && (iconBitmap != null)) {
                    notificationBuilder.setSmallIcon(ColorNotificationIcon.getFromBitmap(iconBitmap));
                }
                else {
                    int iconSmallResource;
                    if (ApplicationPreferences.notificationStatusBarStyle(dataWrapper.context).equals("0"))
                        iconSmallResource = R.drawable.ic_profile_default;
                    else
                        iconSmallResource = R.drawable.ic_profile_default_notify;
                    notificationBuilder.setSmallIcon(iconSmallResource);
                }

                if (iconBitmap != null)
                    contentView.setImageViewBitmap(R.id.notification_activated_profile_icon, iconBitmap);
                else
                    contentView.setImageViewResource(R.id.notification_activated_profile_icon, R.drawable.ic_profile_default);
            }

            // workaround for LG G4, Android 6.0
            if (android.os.Build.VERSION.SDK_INT < 24)
                contentView.setInt(R.id.notification_activated_app_root, "setVisibility", View.GONE);

            if (ApplicationPreferences.notificationTextColor(dataWrapper.context).equals("1")) {
                contentView.setTextColor(R.id.notification_activated_profile_name, Color.BLACK);
                if (android.os.Build.VERSION.SDK_INT >= 24)
                    contentView.setTextColor(R.id.notification_activated_app_name, Color.BLACK);
            }
            else
            if (ApplicationPreferences.notificationTextColor(dataWrapper.context).equals("2")) {
                contentView.setTextColor(R.id.notification_activated_profile_name, Color.WHITE);
                if (android.os.Build.VERSION.SDK_INT >= 24)
                    contentView.setTextColor(R.id.notification_activated_app_name, Color.WHITE);
            }
            contentView.setTextViewText(R.id.notification_activated_profile_name, profileName);

            //contentView.setImageViewBitmap(R.id.notification_activated_profile_pref_indicator,
            //		ProfilePreferencesIndicator.paint(profile, context));
            if ((preferencesIndicator != null) && (ApplicationPreferences.notificationPrefIndicator(dataWrapper.context)))
                contentView.setImageViewBitmap(R.id.notification_activated_profile_pref_indicator, preferencesIndicator);
            else
                contentView.setImageViewResource(R.id.notification_activated_profile_pref_indicator, R.drawable.ic_empty);

            //noinspection deprecation
            notificationBuilder.setContent(contentView);

            try {
                Notification notification = notificationBuilder.build();

                //TODO Android O
                //if (Build.VERSION.SDK_INT < 26) {
                if (ApplicationPreferences.notificationStatusBarPermanent(dataWrapper.context)) {
                    //notification.flags |= Notification.FLAG_NO_CLEAR;
                    notification.flags |= Notification.FLAG_ONGOING_EVENT;
                } else {
                    setAlarmForNotificationCancel(dataWrapper.context);
                }
                //}

                if (ApplicationPreferences.notificationStatusBarPermanent(dataWrapper.context))
                    startForeground(PPApplication.PROFILE_NOTIFICATION_ID, notification);
                else {
                    NotificationManager notificationManager = (NotificationManager) dataWrapper.context.getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.notify(PPApplication.PROFILE_NOTIFICATION_ID, notification);
                }

            } catch (Exception ignored) {}
        }
        else
        {
            if (ApplicationPreferences.notificationStatusBarPermanent(dataWrapper.context))
                stopForeground(true);
            else {
                NotificationManager notificationManager = (NotificationManager) dataWrapper.context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(PPApplication.PROFILE_NOTIFICATION_ID);
            }
        }
    }

    void removeProfileNotification(Context context)
    {
        if (ApplicationPreferences.notificationStatusBarPermanent(context))
            stopForeground(true);
        else {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(PPApplication.PROFILE_NOTIFICATION_ID);
        }
    }

    private void setAlarmForNotificationCancel(Context context)
    {
        if (ApplicationPreferences.notificationStatusBarCancel(context).isEmpty() || ApplicationPreferences.notificationStatusBarCancel(context).equals("0"))
            return;

        Intent intent = new Intent(context, NotificationCancelAlarmBroadcastReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);

        Calendar now = Calendar.getInstance();
        long time = now.getTimeInMillis() + Integer.valueOf(ApplicationPreferences.notificationStatusBarCancel(context)) * 1000;

        alarmManager.set(AlarmManager.RTC, time, pendingIntent);
    }

    //----------------------------------------
}
