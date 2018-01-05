package sk.henrichg.phoneprofiles;

import android.Manifest;
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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.RemoteViews;

import com.crashlytics.android.Crashlytics;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;


public class PhoneProfilesService extends Service {

    public static PhoneProfilesService instance = null;
    private static boolean serviceRunning = false;

    public static HandlerThread handlerThread = null;

    private static KeyguardManager keyguardManager = null;
    @SuppressWarnings("deprecation")
    private static KeyguardManager.KeyguardLock keyguardLock = null;

    private ScreenOnOffBroadcastReceiver screenOnOffReceiver = null;
    private InterruptionFilterChangedBroadcastReceiver interruptionFilterChangedReceiver = null;
    private PhoneCallBroadcastReceiver phoneCallBroadcastReceiver = null;
    private RingerModeChangeReceiver ringerModeChangeReceiver = null;
    private WifiStateChangedBroadcastReceiver wifiStateChangedBroadcastReceiver = null;

    private static SettingsContentObserver settingsContentObserver = null;

    public static String connectToSSID = Profile.CONNECTTOSSID_JUSTANY;

    private AudioManager audioManager = null;
    private MediaPlayer notificationMediaPlayer = null;
    private boolean notificationIsPlayed = false;
    private Timer notificationPlayTimer = null;

    static final String EXTRA_SET_SERVICE_FOREGROUND = "set_service_foreground";
    static final String EXTRA_CLEAR_SERVICE_FOREGROUND = "clear_service_foreground";
    static final String EXTRA_SWITCH_KEYGUARD = "switch_keyguard";

    @Override
    public void onCreate()
    {
        super.onCreate();

        PPApplication.logE("PhoneProfilesService.onCreate", "xxx");

        instance = this;
        final Context appContext = getApplicationContext();

        try {
            Crashlytics.setBool(ApplicationPreferences.PREF_NOTIFICATION_STATUS_BAR, ApplicationPreferences.notificationStatusBar(appContext));
            Crashlytics.setBool(ApplicationPreferences.PREF_NOTIFICATION_STATUS_BAR_PERMANENT, ApplicationPreferences.notificationStatusBarPermanent(appContext));
            Crashlytics.setBool(ApplicationPreferences.PREF_NOTIFICATION_SHOW_IN_STATUS_BAR, ApplicationPreferences.notificationShowInStatusBar(appContext));
        } catch (Exception ignored) {}

        // save version code (is used in PackageReplacedReceiver)
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            int actualVersionCode = pInfo.versionCode;
            PPApplication.setSavedVersionCode(appContext, actualVersionCode);
        } catch (Exception e) {
            //e.printStackTrace();
        }

        startHandlerThread();

        keyguardManager = (KeyguardManager)appContext.getSystemService(Activity.KEYGUARD_SERVICE);
        if (keyguardManager != null)
            //noinspection deprecation
            keyguardLock = keyguardManager.newKeyguardLock("phoneProfiles.keyguardLock");

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

        // start job for first start
        //FirstStartJob.start(appContext);
        PhoneProfilesService.startHandlerThread();
        final Handler handler = new Handler(PhoneProfilesService.handlerThread.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                if (powerManager != null) {
                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "PhoneProfilesService.doForFirstStart.2");
                    wakeLock.acquire(10 * 60 * 1000);
                }

                PPApplication.initRoot();
                // grant root
                //if (PPApplication.isRooted(false))
                //{
                if (PPApplication.isRootGranted())
                {
                    PPApplication.settingsBinaryExists();
                    PPApplication.serviceBinaryExists();
                    //PPApplication.getSUVersion();
                }
                //}
                PPApplication.getServicesList();

                Permissions.clearMergedPermissions(appContext);


                if (PPApplication.getApplicationStarted(appContext, false)) {
                    if ((wakeLock != null) && wakeLock.isHeld())
                        wakeLock.release();
                    return;
                }

                PPApplication.logE("PhoneProfilesService.onCreate", " application not started");

                //int startType = intent.getStringExtra(PPApplication.EXTRA_FIRST_START_TYPE);

                GlobalGUIRoutines.setLanguage(appContext);

                // remove phoneprofiles_silent.mp3
                //removeTone("phoneprofiles_silent.mp3", context);
                // install phoneprofiles_silent.ogg
                TonesHandler.installTone(TonesHandler.TONE_ID, TonesHandler.TONE_NAME, appContext, false);

                ActivateProfileHelper.setLockScreenDisabled(appContext, false);

                AudioManager audioManager = (AudioManager)appContext.getSystemService(Context.AUDIO_SERVICE);
                if (audioManager != null) {
                    ActivateProfileHelper.setRingerVolume(appContext, audioManager.getStreamVolume(AudioManager.STREAM_RING));
                    ActivateProfileHelper.setNotificationVolume(appContext, audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION));
                    RingerModeChangeReceiver.setRingerMode(appContext, audioManager);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
                        PPNotificationListenerService.setZenMode(appContext, audioManager);
                    InterruptionFilterChangedBroadcastReceiver.setZenMode(appContext, audioManager);
                }

                // show info notification
                ImportantInfoNotification.showInfoNotification(appContext);

                ProfileDurationAlarmBroadcastReceiver.removeAlarm(appContext);
                Profile.setActivatedProfileForDuration(appContext, 0);

                LockDeviceActivityFinishBroadcastReceiver.removeAlarm(appContext);

                DataWrapper dataWrapper = new DataWrapper(appContext, true, false, 0);
                dataWrapper.getActivateProfileHelper().initialize(appContext);

                PPApplication.setApplicationStarted(appContext, true);

                dataWrapper._activateProfile(null, PPApplication.STARTUP_SOURCE_BOOT, /*boolean _interactive,*/ null);
                //dataWrapper.invalidateDataWrapper();

                if ((wakeLock != null) && wakeLock.isHeld())
                    wakeLock.release();
            }
        });

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

        reenableKeyguard();

        removeProfileNotification(this);

        if (handlerThread != null) {
            if (Build.VERSION.SDK_INT >= 18)
                handlerThread.quitSafely();
            else
                handlerThread.quit();
            handlerThread = null;
        }

        instance = null;
        serviceRunning = false;

        super.onDestroy();
    }

    static void startHandlerThread() {
        if (handlerThread == null) {
            handlerThread = new HandlerThread("PPHandlerThread");
            handlerThread.start();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        PPApplication.logE("PhoneProfilesService.onStartCommand", "xxx");

        serviceRunning = true;

        ActivateProfileHelper.setMergedRingNotificationVolumes(getApplicationContext(), false);

        if ((intent == null) || (!intent.getBooleanExtra(EXTRA_CLEAR_SERVICE_FOREGROUND, false))) {
            final Context _this = this;
            PhoneProfilesService.startHandlerThread();
            final Handler handler = new Handler(PhoneProfilesService.handlerThread.getLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    // set service foreground
                    final DataWrapper dataWrapper =  new DataWrapper(_this, true, false, 0);
                    dataWrapper.getActivateProfileHelper().initialize(getApplicationContext());
                    Profile activatedProfile = dataWrapper.getActivatedProfile();
                    showProfileNotification(activatedProfile, dataWrapper);
                }
            });
        }

        if (intent != null) {
            /*if (intent.getBooleanExtra(EXTRA_SET_SERVICE_FOREGROUND, false)) {
                PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "EXTRA_SET_SERVICE_FOREGROUND");
                dataWrapper.getActivateProfileHelper().initialize(dataWrapper, getApplicationContext());
                showProfileNotification(activatedProfile, dataWrapper);
            }*/

            if (intent.getBooleanExtra(EXTRA_CLEAR_SERVICE_FOREGROUND, false)) {
                PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "EXTRA_CLEAR_SERVICE_FOREGROUND");
                removeProfileNotification(this);
            }

            if (intent.getBooleanExtra(EXTRA_SWITCH_KEYGUARD, false)) {
                PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "EXTRA_SWITCH_KEYGUARD");

                Context appContext = getApplicationContext();

                boolean isScreenOn;
                //if (android.os.Build.VERSION.SDK_INT >= 20)
                //{
                //    Display display = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
                //    isScreenOn = display.getState() == Display.STATE_ON;
                //}
                //else
                //{
                PowerManager pm = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                isScreenOn = ((pm != null) && pm.isScreenOn());
                //}

                boolean secureKeyguard;
                if (keyguardManager == null)
                    keyguardManager = (KeyguardManager)appContext.getSystemService(Activity.KEYGUARD_SERVICE);
                if (keyguardManager != null) {
                    secureKeyguard = keyguardManager.isKeyguardSecure();
                    PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "secureKeyguard=" + secureKeyguard);
                    if (!secureKeyguard) {
                        PPApplication.logE("$$$ PhoneProfilesService.onStartCommand xxx", "getLockScreenDisabled=" + ActivateProfileHelper.getLockScreenDisabled(appContext));

                        if (isScreenOn) {
                            PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "screen on");

                            if (ActivateProfileHelper.getLockScreenDisabled(appContext)) {
                                PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "Keyguard.disable(), START_STICKY");
                                reenableKeyguard();
                                disableKeyguard();
                            } else {
                                PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "Keyguard.reenable(), stopSelf(), START_NOT_STICKY");
                                reenableKeyguard();
                            }
                        }
                    }
                }
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
            Intent intent = new Intent(dataWrapper.context, ActivateProfileActivity.class);
            // clear all opened activities
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_NOTIFICATION);
            PendingIntent pIntent = PendingIntent.getActivity(dataWrapper.context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

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
                if (myKM != null) {
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

            if (android.os.Build.VERSION.SDK_INT >= 24) {
                //notificationBuilder.setStyle(new Notification.DecoratedCustomViewStyle());
                notificationBuilder.setCustomContentView(contentView);
            }
            else
                notificationBuilder.setContent(contentView);

            try {
                Notification notification = notificationBuilder.build();

                //TODO Android O
                //if (Build.VERSION.SDK_INT < 26) {
                if (ApplicationPreferences.notificationStatusBarPermanent(dataWrapper.context)) {
                    //notification.flags |= Notification.FLAG_NO_CLEAR;
                    notification.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
                } else {
                    setAlarmForNotificationCancel(dataWrapper.context);
                }
                //}

                if (ApplicationPreferences.notificationStatusBarPermanent(dataWrapper.context))
                    startForeground(PPApplication.PROFILE_NOTIFICATION_ID, notification);
                else {
                    NotificationManager notificationManager = (NotificationManager) dataWrapper.context.getSystemService(Context.NOTIFICATION_SERVICE);
                    if (notificationManager != null)
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
                if (notificationManager != null)
                    notificationManager.cancel(PPApplication.PROFILE_NOTIFICATION_ID);
            }
        }
    }

    private void removeProfileNotification(Context context)
    {
        if (ApplicationPreferences.notificationStatusBarPermanent(context))
            stopForeground(true);
        else {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null)
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
        if (alarmManager != null) {
            Calendar now = Calendar.getInstance();
            long time = now.getTimeInMillis() + Integer.valueOf(ApplicationPreferences.notificationStatusBarCancel(context)) * 1000;

            alarmManager.set(AlarmManager.RTC, time, pendingIntent);
        }
    }

    //----------------------------------------

    // switch keyguard ------------------------------------

    private void disableKeyguard()
    {
        PPApplication.logE("$$$ Keyguard.disable","keyguardLock="+keyguardLock);
        if ((keyguardLock != null) && Permissions.hasPermission(getApplicationContext(), Manifest.permission.DISABLE_KEYGUARD))
            keyguardLock.disableKeyguard();
    }

    private void reenableKeyguard()
    {
        PPApplication.logE("$$$ Keyguard.reenable","keyguardLock="+keyguardLock);
        if ((keyguardLock != null) && Permissions.hasPermission(getApplicationContext(), Manifest.permission.DISABLE_KEYGUARD))
            keyguardLock.reenableKeyguard();
    }

    //--------------------------------------

    //others -------------------------------

    private void stopPlayNotificationSound() {
        if (notificationPlayTimer != null) {
            notificationPlayTimer.cancel();
            notificationPlayTimer = null;
        }
        if ((notificationMediaPlayer != null) && notificationIsPlayed) {
            try {
                if (notificationMediaPlayer.isPlaying())
                    notificationMediaPlayer.stop();
            } catch (Exception ignored) {}
            notificationMediaPlayer.release();
            notificationIsPlayed = false;
            notificationMediaPlayer = null;
        }
    }

    public void playNotificationSound (final String notificationSound, final boolean notificationVibrate) {
        if (notificationVibrate) {
            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            if ((vibrator != null) && vibrator.hasVibrator()) {
                PPApplication.logE("PhoneProfilesService.playNotificationSound", "vibration");
                //TODO Android O
                //if (Build.VERSION.SDK_INT >= 26) {
                //    vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                //} else {
                vibrator.vibrate(500);
                //}
            }
        }

        if (audioManager == null )
            audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);

        stopPlayNotificationSound();

        if (!notificationSound.isEmpty())
        {
            Uri notificationUri = Uri.parse(notificationSound);

            try {
                RingerModeChangeReceiver.internalChange = true;

                notificationMediaPlayer = new MediaPlayer();
                notificationMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                notificationMediaPlayer.setDataSource(this, notificationUri);
                notificationMediaPlayer.prepare();
                notificationMediaPlayer.setLooping(false);

                /*
                oldMediaVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

                int notificationVolume = ActivateProfileHelper.getNotificationVolume(this);

                PPApplication.logE("PhoneProfilesService.playNotificationSound", "notificationVolume=" + notificationVolume);

                int maximumNotificationValue = audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);
                int maximumMediaValue = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

                float percentage = (float) notificationVolume / maximumNotificationValue * 100.0f;
                int mediaNotificationVolume = Math.round(maximumMediaValue / 100.0f * percentage);

                PPApplication.logE("PhoneProfilesService.playNotificationSound", "mediaNotificationVolume=" + mediaNotificationVolume);

                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mediaNotificationVolume, 0);
                */

                notificationMediaPlayer.start();

                notificationIsPlayed = true;

                //final Context context = this;
                notificationPlayTimer = new Timer();
                notificationPlayTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {

                        if (notificationMediaPlayer != null) {
                            try {
                                if (notificationMediaPlayer.isPlaying())
                                    notificationMediaPlayer.stop();
                            } catch (Exception ignored) {}
                            notificationMediaPlayer.release();

                            //audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, oldMediaVolume, 0);
                            PPApplication.logE("PhoneProfilesService.playNotificationSound", "notification stopped");
                        }

                        notificationIsPlayed = false;
                        notificationMediaPlayer = null;

                        PhoneProfilesService.startHandlerThread();
                        final Handler handler = new Handler(PhoneProfilesService.handlerThread.getLooper());
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                RingerModeChangeReceiver.internalChange = false;
                            }
                        }, 3000);

                        notificationPlayTimer = null;
                    }
                }, notificationMediaPlayer.getDuration());

            } catch (SecurityException e) {
                PPApplication.logE("PhoneProfilesService.playNotificationSound", "security exception");
                stopPlayNotificationSound();
                PhoneProfilesService.startHandlerThread();
                final Handler handler = new Handler(PhoneProfilesService.handlerThread.getLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        RingerModeChangeReceiver.internalChange = false;
                    }
                }, 3000);
                Permissions.grantPlayRingtoneNotificationPermissions(this);
            } catch (Exception e) {
                PPApplication.logE("PhoneProfilesService.playNotificationSound", "exception");
                stopPlayNotificationSound();
                PhoneProfilesService.startHandlerThread();
                final Handler handler = new Handler(PhoneProfilesService.handlerThread.getLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        RingerModeChangeReceiver.internalChange = false;
                    }
                }, 3000);
            }

        }
    }

    //----------------------------------------

}
