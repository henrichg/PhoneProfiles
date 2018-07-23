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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.widget.RemoteViews;

import com.crashlytics.android.Crashlytics;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;


public class PhoneProfilesService extends Service {

    private static volatile PhoneProfilesService instance = null;
    private boolean serviceHasFirstStart = false;
    private boolean serviceRunning = false;
    private boolean runningInForeground = false;

    private KeyguardManager keyguardManager = null;
    @SuppressWarnings("deprecation")
    private KeyguardManager.KeyguardLock keyguardLock = null;

    BrightnessView brightnessView = null;
    BrightnessView keepScreenOnView = null;

    LockDeviceActivity lockDeviceActivity = null;
    int screenTimeoutBeforeDeviceLock = 0;

    private ShutdownBroadcastReceiver shutdownBroadcastReceiver = null;
    private ScreenOnOffBroadcastReceiver screenOnOffReceiver = null;
    private InterruptionFilterChangedBroadcastReceiver interruptionFilterChangedReceiver = null;
    private PhoneCallBroadcastReceiver phoneCallBroadcastReceiver = null;
    private RingerModeChangeReceiver ringerModeChangeReceiver = null;
    private WifiStateChangedBroadcastReceiver wifiStateChangedBroadcastReceiver = null;
    private AccessibilityServiceBroadcastReceiver accessibilityServiceBroadcastReceiver = null;

    private SettingsContentObserver settingsContentObserver = null;

    String connectToSSID = Profile.CONNECTTOSSID_JUSTANY;

    private AudioManager audioManager = null;
    private MediaPlayer notificationMediaPlayer = null;
    private boolean notificationIsPlayed = false;
    private Timer notificationPlayTimer = null;

    static final String EXTRA_SHOW_PROFILE_NOTIFICATION = "show_profile_notification";
    static final String EXTRA_START_ON_PACKAGE_REPLACE = "start_on_package_replace";
    static final String EXTRA_ONLY_START = "only_start";
    static final String EXTRA_SET_SERVICE_FOREGROUND = "set_service_foreground";
    static final String EXTRA_CLEAR_SERVICE_FOREGROUND = "clear_service_foreground";
    static final String EXTRA_SWITCH_KEYGUARD = "switch_keyguard";

    @Override
    public void onCreate()
    {
        super.onCreate();

        PPApplication.logE("PhoneProfilesService.onCreate", "xxx");

        synchronized (PhoneProfilesService.class) {
            instance = this;
        }

        serviceHasFirstStart = false;
        serviceRunning = false;
        runningInForeground = false;

        if (Build.VERSION.SDK_INT >= 26)
            // show empty notification to avoid ANR
            showProfileNotification();

        final Context appContext = getApplicationContext();

        try {
            if ((Build.VERSION.SDK_INT < 26)) {
                Crashlytics.setBool(ApplicationPreferences.PREF_NOTIFICATION_STATUS_BAR, ApplicationPreferences.notificationStatusBar(this));
                Crashlytics.setBool(ApplicationPreferences.PREF_NOTIFICATION_STATUS_BAR_PERMANENT, ApplicationPreferences.notificationStatusBarPermanent(this));
                Crashlytics.setBool(ApplicationPreferences.PREF_NOTIFICATION_SHOW_IN_STATUS_BAR, ApplicationPreferences.notificationShowInStatusBar(this));
            }
        } catch (Exception ignored) {}

        /*
        ApplicationPreferences.getSharedPreferences(appContext);
        SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
        editor.putBoolean(ActivateProfileActivity.PREF_START_TARGET_HELPS, true);
        editor.putBoolean(ActivateProfileListFragment.PREF_START_TARGET_HELPS, true);
        editor.putBoolean(ActivateProfileListAdapter.PREF_START_TARGET_HELPS, true);
        editor.putBoolean(EditorProfilesActivity.PREF_START_TARGET_HELPS, true);
        editor.putBoolean(EditorProfileListFragment.PREF_START_TARGET_HELPS, true);
        editor.putBoolean(EditorProfileListAdapter.PREF_START_TARGET_HELPS, true);
        editor.putBoolean(ProfilePreferencesActivity.PREF_START_TARGET_HELPS, true);
        editor.putBoolean(ProfilePreferencesActivity.PREF_START_TARGET_HELPS_SAVE, true);
        editor.apply();
        */

        keyguardManager = (KeyguardManager)appContext.getSystemService(Activity.KEYGUARD_SERVICE);
        if (keyguardManager != null)
            //noinspection deprecation
            keyguardLock = keyguardManager.newKeyguardLock("phoneProfiles.keyguardLock");

        if (PPApplication.getApplicationStarted(getApplicationContext(), false))
            doForFirstStart(null);
        else
            stopSelf();
    }

    @Override
    public void onDestroy()
    {
        PPApplication.logE("PhoneProfilesService.onDestroy", "xxx");

        Context appContext = getApplicationContext();

        if (shutdownBroadcastReceiver != null) {
            appContext.unregisterReceiver(shutdownBroadcastReceiver);
            shutdownBroadcastReceiver = null;
        }
        if (screenOnOffReceiver != null) {
            appContext.unregisterReceiver(screenOnOffReceiver);
            screenOnOffReceiver = null;
        }
        if (android.os.Build.VERSION.SDK_INT >= 23)
            if (interruptionFilterChangedReceiver != null) {
                appContext.unregisterReceiver(interruptionFilterChangedReceiver);
                interruptionFilterChangedReceiver = null;
            }
        if (phoneCallBroadcastReceiver != null) {
            appContext.unregisterReceiver(phoneCallBroadcastReceiver);
            phoneCallBroadcastReceiver = null;
        }
        if (ringerModeChangeReceiver != null) {
            appContext.unregisterReceiver(ringerModeChangeReceiver);
            ringerModeChangeReceiver = null;
        }
        if (wifiStateChangedBroadcastReceiver != null) {
            appContext.unregisterReceiver(wifiStateChangedBroadcastReceiver);
            wifiStateChangedBroadcastReceiver = null;
        }
        if (accessibilityServiceBroadcastReceiver != null) {
            appContext.unregisterReceiver(accessibilityServiceBroadcastReceiver);
            accessibilityServiceBroadcastReceiver = null;
        }

        if (settingsContentObserver != null) {
            appContext.getContentResolver().unregisterContentObserver(settingsContentObserver);
            settingsContentObserver = null;
        }

        reenableKeyguard();

        removeProfileNotification(this);

        synchronized (PhoneProfilesService.class) {
            instance = null;
        }

        serviceHasFirstStart = false;
        serviceRunning = false;
        runningInForeground = false;

        super.onDestroy();
    }

    static PhoneProfilesService getInstance() {
        return instance;
    }

    boolean getServiceHasFirstStart() {
        return serviceHasFirstStart;
    }

    // start service for first start
    private boolean doForFirstStart(Intent intent/*, int flags, int startId*/) {
        boolean onlyStart = true;
        boolean startOnPackageReplace = false;

        if (intent != null) {
            onlyStart = intent.getBooleanExtra(EXTRA_ONLY_START, true);
            startOnPackageReplace = intent.getBooleanExtra(EXTRA_START_ON_PACKAGE_REPLACE, false);
        }

        if ((intent == null) || (!intent.getBooleanExtra(EXTRA_CLEAR_SERVICE_FOREGROUND, false))) {
            // do not call this from handlerThread. In Android 8 handlerThread is not called
            // when for service is not displayed foreground notification
            showProfileNotification();
        }

        if (serviceRunning && onlyStart && !startOnPackageReplace) {
            PPApplication.logE("PhoneProfilesService.doForFirstStart", "EXTRA_ONLY_START already running");
            return true;
        }

        if ((!startOnPackageReplace) && PPApplication.isNewVersion(getApplicationContext())) {
            PPApplication.logE("PhoneProfilesService.doForFirstStart", "is new version");
            return true;
        }

        serviceRunning = true;

        if (onlyStart)
            PPApplication.logE("PhoneProfilesService.doForFirstStart", "EXTRA_ONLY_START");
        if (startOnPackageReplace)
            PPApplication.logE("PhoneProfilesService.doForFirstStart", "EXTRA_START_ON_PACKAGE_REPLACE");

        final Context appContext = getApplicationContext();

        if (onlyStart) {

            //if (startOnPackageReplace) {
            //  moved to PackageReplacedReceiver
            //}

            PPApplication.startHandlerThread();
            final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "PhoneProfilesService.doForFirstStart.2");
                        wakeLock.acquire(10 * 60 * 1000);
                    }

                    PPApplication.logE("PhoneProfilesService.doForFirstStart", "doForFirstStart.2");

                    PPApplication.initRoot();
                    // grant root
                    //noinspection StatementWithEmptyBody
                    if (PPApplication.isRootGranted())
                    {
                    }
                    //PPApplication.getSUVersion();
                    PPApplication.settingsBinaryExists();
                    PPApplication.serviceBinaryExists();
                    PPApplication.getServicesList();

                    GlobalGUIRoutines.setLanguage(appContext);

                    //if (PPApplication.getApplicationStarted(appContext, false)) {
                    if (serviceHasFirstStart) {
                        PPApplication.logE("PhoneProfilesService.doForFirstStart", " application already started");
                        if ((wakeLock != null) && wakeLock.isHeld()) {
                            try {
                                wakeLock.release();
                            } catch (Exception ignored) {}
                        }
                        return;
                    }

                    PPApplication.logE("PhoneProfilesService.doForFirstStart", " application not started");

                    Permissions.clearMergedPermissions(appContext);

                    PPApplication.createNotificationChannels(appContext);

                    //int startType = intent.getStringExtra(PPApplication.EXTRA_FIRST_START_TYPE);

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
                        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
                        PPNotificationListenerService.setZenMode(appContext, audioManager);
                        InterruptionFilterChangedBroadcastReceiver.setZenMode(appContext, audioManager);
                    }

                    // show info notification
                    ImportantInfoNotification.showInfoNotification(appContext);

                    ProfileDurationAlarmBroadcastReceiver.removeAlarm(appContext);
                    Profile.setActivatedProfileForDuration(appContext, 0);

                    LockDeviceActivityFinishBroadcastReceiver.removeAlarm(appContext);

                    DataWrapper dataWrapper = new DataWrapper(appContext, false, 0);
                    dataWrapper.setDynamicLauncherShortcuts();

                    if (instance != null)
                        PhoneProfilesService.getInstance().registerReceivers();
                    AboutApplicationJob.scheduleJob(getApplicationContext(), true);

                    serviceHasFirstStart = true;
                    //PPApplication.setApplicationStarted(appContext, true);

                    dataWrapper.activateProfile(0, PPApplication.STARTUP_SOURCE_BOOT, null);
                    //dataWrapper.invalidateDataWrapper();

                    if ((wakeLock != null) && wakeLock.isHeld()) {
                        try {
                            wakeLock.release();
                        } catch (Exception ignored) {}
                    }
                }
            });

            ActivateProfileHelper.setMergedRingNotificationVolumes(appContext, false);
        }

        return onlyStart;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "intent="+intent);

        if (!PPApplication.getApplicationStarted(getApplicationContext(), false)) {
            stopSelf();
            return START_NOT_STICKY;
        }

        if (!doForFirstStart(intent/*, flags, startId*/)) {
            if (intent != null) {
                if (intent.getBooleanExtra(EXTRA_SHOW_PROFILE_NOTIFICATION, false)) {
                    PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "EXTRA_SHOW_PROFILE_NOTIFICATION");
                    showProfileNotification();
                }

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
                        keyguardManager = (KeyguardManager) appContext.getSystemService(Activity.KEYGUARD_SERVICE);
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
        }

        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent)
    {
        PPApplication.logE("$$$ PhoneProfilesService.onTaskRemoved", "xxx");

        if (PPApplication.screenTimeoutHandler != null) {
            PPApplication.screenTimeoutHandler.post(new Runnable() {
                public void run() {
                    ActivateProfileHelper.removeScreenTimeoutAlwaysOnView(getApplicationContext());
                }
            });
        }// else
        //    ActivateProfileHelper.removeScreenTimeoutAlwaysOnView(getApplicationContext());

        super.onTaskRemoved(rootIntent);
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    private void registerReceivers() {
        Context appContext = getApplicationContext();

        if (shutdownBroadcastReceiver != null) {
            appContext.unregisterReceiver(shutdownBroadcastReceiver);
            shutdownBroadcastReceiver = null;
        }
        shutdownBroadcastReceiver = new ShutdownBroadcastReceiver();
        IntentFilter intentFilter50 = new IntentFilter();
        intentFilter50.addAction(Intent.ACTION_SHUTDOWN);
        appContext.registerReceiver(shutdownBroadcastReceiver, intentFilter50);

        if (screenOnOffReceiver != null) {
            appContext.unregisterReceiver(screenOnOffReceiver);
            screenOnOffReceiver = null;
        }
        screenOnOffReceiver = new ScreenOnOffBroadcastReceiver();
        IntentFilter intentFilter5 = new IntentFilter();
        intentFilter5.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter5.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter5.addAction(Intent.ACTION_USER_PRESENT);
        appContext.registerReceiver(screenOnOffReceiver, intentFilter5);

        if (android.os.Build.VERSION.SDK_INT >= 23) {
            if (interruptionFilterChangedReceiver != null) {
                appContext.unregisterReceiver(interruptionFilterChangedReceiver);
                interruptionFilterChangedReceiver = null;
            }
            boolean no60 = !Build.VERSION.RELEASE.equals("6.0");
            if (no60 && GlobalGUIRoutines.activityActionExists(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS, appContext)) {
                interruptionFilterChangedReceiver = new InterruptionFilterChangedBroadcastReceiver();
                IntentFilter intentFilter11 = new IntentFilter();
                intentFilter11.addAction(NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED);
                appContext.registerReceiver(interruptionFilterChangedReceiver, intentFilter11);
            }
        }

        if (phoneCallBroadcastReceiver != null) {
            appContext.unregisterReceiver(phoneCallBroadcastReceiver);
            phoneCallBroadcastReceiver = null;
        }
        phoneCallBroadcastReceiver = new PhoneCallBroadcastReceiver();
        IntentFilter intentFilter6 = new IntentFilter();
        intentFilter6.addAction(Intent.ACTION_NEW_OUTGOING_CALL);
        intentFilter6.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        appContext.registerReceiver(phoneCallBroadcastReceiver, intentFilter6);

        if (ringerModeChangeReceiver != null) {
            appContext.unregisterReceiver(ringerModeChangeReceiver);
            ringerModeChangeReceiver = null;
        }
        ringerModeChangeReceiver = new RingerModeChangeReceiver();
        IntentFilter intentFilter7 = new IntentFilter();
        intentFilter7.addAction(AudioManager.RINGER_MODE_CHANGED_ACTION);
        appContext.registerReceiver(ringerModeChangeReceiver, intentFilter7);

        if (wifiStateChangedBroadcastReceiver != null) {
            appContext.unregisterReceiver(wifiStateChangedBroadcastReceiver);
            wifiStateChangedBroadcastReceiver = null;
        }
        wifiStateChangedBroadcastReceiver = new WifiStateChangedBroadcastReceiver();
        IntentFilter intentFilter8 = new IntentFilter();
        intentFilter8.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        appContext.registerReceiver(wifiStateChangedBroadcastReceiver, intentFilter8);

        if (accessibilityServiceBroadcastReceiver != null) {
            appContext.unregisterReceiver(accessibilityServiceBroadcastReceiver);
            accessibilityServiceBroadcastReceiver = null;
        }
        accessibilityServiceBroadcastReceiver = new AccessibilityServiceBroadcastReceiver();
        IntentFilter intentFilter23 = new IntentFilter();
        intentFilter23.addAction(PPApplication.ACTION_ACCESSIBILITY_SERVICE_UNBIND);
        intentFilter23.addAction(PPApplication.ACTION_FORCE_STOP_APPLICATIONS_END);
        appContext.registerReceiver(accessibilityServiceBroadcastReceiver, intentFilter23,
                PPApplication.ACCESSIBILITY_SERVICE_PERMISSION, null);

        if (settingsContentObserver != null) {
            appContext.getContentResolver().unregisterContentObserver(settingsContentObserver);
            settingsContentObserver = null;
        }
        try {
            //settingsContentObserver = new SettingsContentObserver(this, new Handler(getMainLooper()));
            settingsContentObserver = new SettingsContentObserver(appContext, new Handler());
            appContext.getContentResolver().registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, settingsContentObserver);
        } catch (Exception ignored) {}
    }

    // profile notification -------------------------------------------

    @SuppressLint("NewApi")
    private void _showProfileNotification(Profile profile, boolean inHandlerThread)
    {
        PPApplication.logE("PhoneProfilesService.showProfileNotification", "xxx");

        if (ActivateProfileHelper.lockRefresh)
            // no refresh notification
            return;

        final Context appContext = getApplicationContext();

        if ((instance != null) && ((Build.VERSION.SDK_INT >= 26) || ApplicationPreferences.notificationStatusBar(appContext)))
        {
            PPApplication.logE("PhoneProfilesService.showProfileNotification", "show");

            // close showed notification
            //notificationManager.cancel(PPApplication.NOTIFICATION_ID);
            Intent intent = new Intent(appContext, ActivateProfileActivity.class);
            // clear all opened activities
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_NOTIFICATION);
            PendingIntent pIntent = PendingIntent.getActivity(appContext, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            Notification.Builder notificationBuilder;

            boolean miui = (PPApplication.romManufacturer != null) &&
                    (PPApplication.romManufacturer.compareToIgnoreCase("xiaomi") == 0)/* &&
                    (android.os.Build.VERSION.SDK_INT >= 24)*/;

            RemoteViews contentView;
            /*if (ApplicationPreferences.notificationTheme(dataWrapper.context).equals("1"))
                contentView = new RemoteViews(dataWrapper.context.getPackageName(), R.layout.notification_drawer_dark);
            else
            if (ApplicationPreferences.notificationTheme(dataWrapper.context).equals("2"))
                contentView = new RemoteViews(dataWrapper.context.getPackageName(), R.layout.notification_drawer_light);
            else {*/
            if (miui/* && (Build.VERSION.SDK_INT < 25)*/)
                contentView = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer_miui);
            else
                contentView = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer);
            //}

            boolean isIconResourceID;
            String iconIdentifier;
            String profileName;
            Bitmap iconBitmap;
            Bitmap preferencesIndicator;

            if (profile != null)
            {
                isIconResourceID = profile.getIsIconResourceID();
                iconIdentifier = profile.getIconIdentifier();
                profileName = profile.getProfileNameWithDuration(false, appContext);

                if (inHandlerThread) {
                    profile.generateIconBitmap(appContext, false, 0);
                    if (ApplicationPreferences.notificationPrefIndicator(appContext))
                        profile.generatePreferencesIndicator(appContext, false, 0);
                    iconBitmap = profile._iconBitmap;
                    preferencesIndicator = profile._preferencesIndicator;
                }
                else {
                    iconBitmap = null;
                    preferencesIndicator = null;
                }
            }
            else
            {
                isIconResourceID = true;
                iconIdentifier = Profile.PROFILE_ICON_DEFAULT;
                if (inHandlerThread)
                    profileName = appContext.getResources().getString(R.string.profiles_header_profile_name_no_activated);
                else
                    profileName = appContext.getResources().getString(R.string.empty_string);
                iconBitmap = null;
                preferencesIndicator = null;
            }

            notificationBuilder = new Notification.Builder(appContext);
            notificationBuilder.setContentIntent(pIntent);

            if (Build.VERSION.SDK_INT >= 21)
                notificationBuilder.setColor(ContextCompat.getColor(appContext, R.color.primary));

            if (Build.VERSION.SDK_INT >= 26) {
                PPApplication.createProfileNotificationChannel(/*profile, */appContext);
                notificationBuilder.setChannelId(PPApplication.PROFILE_NOTIFICATION_CHANNEL);
                //notificationBuilder.setSettingsText("Test");
            }
            else {
                if (ApplicationPreferences.notificationShowInStatusBar(appContext)) {
                    KeyguardManager myKM = (KeyguardManager) appContext.getSystemService(Context.KEYGUARD_SERVICE);
                    if (myKM != null) {
                        //boolean screenUnlocked = !myKM.inKeyguardRestrictedInputMode();
                        boolean screenUnlocked = !myKM.isKeyguardLocked();
                        //boolean screenUnlocked = getScreenUnlocked(context);
                        if ((ApplicationPreferences.notificationHideInLockScreen(appContext) && (!screenUnlocked)) ||
                                ((profile != null) && profile._hideStatusBarIcon))
                            notificationBuilder.setPriority(Notification.PRIORITY_MIN);
                        else
                            notificationBuilder.setPriority(Notification.PRIORITY_DEFAULT);
                    } else
                        notificationBuilder.setPriority(Notification.PRIORITY_DEFAULT);
                } else
                    notificationBuilder.setPriority(Notification.PRIORITY_MIN);
            }

            if (android.os.Build.VERSION.SDK_INT >= 21)
            {
                notificationBuilder.setCategory(Notification.CATEGORY_STATUS);
                notificationBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
            }

            notificationBuilder.setTicker(profileName);

            if (inHandlerThread) {
                if (isIconResourceID)
                {
                    int iconSmallResource;
                    if (iconBitmap != null) {
                        if (ApplicationPreferences.notificationStatusBarStyle(appContext).equals("0")) {
                            // colorful icon

                            // FC in Note 4, 6.0.1 :-/
                            boolean isNote4 = (PPApplication.romManufacturer != null) && (PPApplication.romManufacturer.compareToIgnoreCase("samsung") == 0) &&
                            /*(Build.MODEL.startsWith("SM-N910") ||  // Samsung Note 4
                             Build.MODEL.startsWith("SM-G900")     // Samsung Galaxy S5
                            ) &&*/
                                    (android.os.Build.VERSION.SDK_INT == 23);
                            //Log.d("ActivateProfileHelper.showNotification","isNote4="+isNote4);
                            if ((android.os.Build.VERSION.SDK_INT >= 23) && (!isNote4)) {
                                notificationBuilder.setSmallIcon(Icon.createWithBitmap(iconBitmap));
                            }
                            else {
                                iconSmallResource = R.drawable.ic_profile_default_notify_color;
                                try {
                                    iconSmallResource = Profile.profileIconNotifyColorId.get(iconIdentifier);
                                } catch (Exception ignored) {}
                                notificationBuilder.setSmallIcon(iconSmallResource);
                            }
                        }
                        else {
                            // native icon
                            //iconSmallResource = dataWrapper.context.getResources().getIdentifier(iconIdentifier + "_notify", "drawable", dataWrapper.context.getPackageName());
                            //if (iconSmallResource == 0)
                            //    iconSmallResource = R.drawable.ic_profile_default_notify;
                            iconSmallResource = R.drawable.ic_profile_default_notify;
                            try {
                                iconSmallResource = Profile.profileIconNotifyId.get(iconIdentifier);
                            } catch (Exception ignored) {}
                            notificationBuilder.setSmallIcon(iconSmallResource);
                        }

                        contentView.setImageViewBitmap(R.id.notification_activated_profile_icon, iconBitmap);
                    }
                    else {
                        if (ApplicationPreferences.notificationStatusBarStyle(appContext).equals("0")) {
                            // colorful icon
                            //iconSmallResource = dataWrapper.context.getResources().getIdentifier(iconIdentifier + "_notify_color", "drawable", dataWrapper.context.getPackageName());
                            //if (iconSmallResource == 0)
                            //    iconSmallResource = R.drawable.ic_profile_default;
                            iconSmallResource = R.drawable.ic_profile_default_notify_color;
                            try {
                                iconSmallResource = Profile.profileIconNotifyColorId.get(iconIdentifier);
                            } catch (Exception ignored) {}
                            notificationBuilder.setSmallIcon(iconSmallResource);

                            //int iconLargeResource = dataWrapper.context.getResources().getIdentifier(iconIdentifier, "drawable", dataWrapper.context.getPackageName());
                            //if (iconLargeResource == 0)
                            //    iconLargeResource = R.drawable.ic_profile_default;
                            int iconLargeResource = Profile.getIconResource(iconIdentifier);
                            Bitmap largeIcon = BitmapFactory.decodeResource(appContext.getResources(), iconLargeResource);
                            contentView.setImageViewBitmap(R.id.notification_activated_profile_icon, largeIcon);
                        } else {
                            // native icon
                            //iconSmallResource = dataWrapper.context.getResources().getIdentifier(iconIdentifier + "_notify", "drawable", dataWrapper.context.getPackageName());
                            //if (iconSmallResource == 0)
                            //    iconSmallResource = R.drawable.ic_profile_default_notify;
                            iconSmallResource = R.drawable.ic_profile_default_notify;
                            try {
                                iconSmallResource = Profile.profileIconNotifyId.get(iconIdentifier);
                            } catch (Exception ignored) {}
                            notificationBuilder.setSmallIcon(iconSmallResource);

                            //int iconLargeResource = dataWrapper.context.getResources().getIdentifier(iconIdentifier, "drawable", dataWrapper.context.getPackageName());
                            //if (iconLargeResource == 0)
                            //    iconLargeResource = R.drawable.ic_profile_default;
                            int iconLargeResource = Profile.getIconResource(iconIdentifier);
                            Bitmap largeIcon = BitmapFactory.decodeResource(appContext.getResources(), iconLargeResource);
                            contentView.setImageViewBitmap(R.id.notification_activated_profile_icon, largeIcon);
                        }
                    }
                }
                else
                {
                    // FC in Note 4, 6.0.1 :-/
                    boolean isNote4 = (PPApplication.romManufacturer != null) && (PPApplication.romManufacturer.compareToIgnoreCase("samsung") == 0) &&
                    /*(Build.MODEL.startsWith("SM-N910") ||  // Samsung Note 4
                     Build.MODEL.startsWith("SM-G900")     // Samsung Galaxy S5
                    ) &&*/
                            (android.os.Build.VERSION.SDK_INT == 23);
                    //Log.d("ActivateProfileHelper.showNotification","isNote4="+isNote4);
                    if ((Build.VERSION.SDK_INT >= 23) && (!isNote4) && (iconBitmap != null)) {
                        notificationBuilder.setSmallIcon(Icon.createWithBitmap(iconBitmap));
                    }
                    else {
                        int iconSmallResource;
                        if (ApplicationPreferences.notificationStatusBarStyle(appContext).equals("0"))
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
            }
            else {
                notificationBuilder.setSmallIcon(R.drawable.ic_empty);
                contentView.setImageViewResource(R.id.notification_activated_profile_icon, R.drawable.ic_empty);
            }

            if (ApplicationPreferences.notificationTextColor(appContext).equals("1")) {
                contentView.setTextColor(R.id.notification_activated_profile_name, Color.BLACK);
            }
            else
            if (ApplicationPreferences.notificationTextColor(appContext).equals("2")) {
                contentView.setTextColor(R.id.notification_activated_profile_name, Color.WHITE);
            }
            contentView.setTextViewText(R.id.notification_activated_profile_name, profileName);

            if ((preferencesIndicator != null) && (ApplicationPreferences.notificationPrefIndicator(appContext)))
                contentView.setImageViewBitmap(R.id.notification_activated_profile_pref_indicator, preferencesIndicator);
            else
                contentView.setImageViewResource(R.id.notification_activated_profile_pref_indicator, R.drawable.ic_empty);

            if (android.os.Build.VERSION.SDK_INT >= 24) {
                // workaround for MIUI :-(
                if ((!miui) || (Build.VERSION.SDK_INT >= 26))
                    notificationBuilder.setStyle(new Notification.DecoratedCustomViewStyle());
                notificationBuilder.setCustomContentView(contentView);
            }
            else
                notificationBuilder.setContent(contentView);

            try {
                Notification notification = notificationBuilder.build();

                if (Build.VERSION.SDK_INT < 26) {
                    notification.flags &= ~Notification.FLAG_SHOW_LIGHTS;
                    notification.ledOnMS = 0;
                    notification.ledOffMS = 0;
                }

                if ((Build.VERSION.SDK_INT >= 26) || ApplicationPreferences.notificationStatusBarPermanent(appContext)) {
                    //notification.flags |= Notification.FLAG_NO_CLEAR;
                    notification.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
                } else {
                    setAlarmForNotificationCancel(appContext);
                }

                if ((Build.VERSION.SDK_INT >= 26) || ApplicationPreferences.notificationStatusBarPermanent(appContext))
                    if (instance != null)
                        PhoneProfilesService.getInstance().startForeground(PPApplication.PROFILE_NOTIFICATION_ID, notification);
                else {
                    NotificationManager notificationManager = (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);
                    if (notificationManager != null)
                        notificationManager.notify(PPApplication.PROFILE_NOTIFICATION_ID, notification);
                }

            } catch (Exception ignored) {}
        }
        else
        {
            if ((Build.VERSION.SDK_INT >= 26) || ApplicationPreferences.notificationStatusBarPermanent(appContext))
                stopForeground(true);
            else {
                NotificationManager notificationManager = (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);
                if (notificationManager != null)
                    notificationManager.cancel(PPApplication.PROFILE_NOTIFICATION_ID);
            }
        }
    }

    private void showProfileNotification() {
        final Context appContext = getApplicationContext();
        final DataWrapper dataWrapper = new DataWrapper(appContext, false, 0);
        final Profile profile = dataWrapper.getActivatedProfileFromDB(false, false);
        dataWrapper.invalidateDataWrapper();

        if (!runningInForeground) {
            _showProfileNotification(profile, false);
            runningInForeground = true;
        }

        PPApplication.startHandlerThreadProfileNotification();
        final Handler handler = new Handler(PPApplication.handlerThreadProfileNotification.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (instance != null) {
                    DataWrapper dataWrapper = new DataWrapper(PhoneProfilesService.getInstance().getApplicationContext(), false, 0);
                    Profile profile = dataWrapper.getActivatedProfileFromDB(false, false);
                    if (instance != null)
                        PhoneProfilesService.getInstance()._showProfileNotification(profile, true);
                    dataWrapper.invalidateDataWrapper();
                }
            }
        });
    }

    private void removeProfileNotification(Context context)
    {
        if ((Build.VERSION.SDK_INT >= 26) || ApplicationPreferences.notificationStatusBarPermanent(context))
            stopForeground(true);
        else {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null)
                notificationManager.cancel(PPApplication.PROFILE_NOTIFICATION_ID);
        }
        runningInForeground = false;
    }

    private void setAlarmForNotificationCancel(Context context)
    {
        if (Build.VERSION.SDK_INT >= 26)
            return;

        if (ApplicationPreferences.notificationStatusBarCancel(context).isEmpty() || ApplicationPreferences.notificationStatusBarCancel(context).equals("0"))
            return;

        Intent intent = new Intent(context, NotificationCancelAlarmBroadcastReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

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
                notificationMediaPlayer.release();
            } catch (Exception ignored) {}
            notificationIsPlayed = false;
            notificationMediaPlayer = null;
        }
    }

    public void playNotificationSound (final String notificationSound, final boolean notificationVibrate) {
        if (notificationVibrate) {
            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            if ((vibrator != null) && vibrator.hasVibrator()) {
                PPApplication.logE("PhoneProfilesService.playNotificationSound", "vibration");
                if (Build.VERSION.SDK_INT >= 26) {
                    vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(500);
                }
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
                                notificationMediaPlayer.release();
                            } catch (Exception ignored) {}

                            //audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, oldMediaVolume, 0);
                            PPApplication.logE("PhoneProfilesService.playNotificationSound", "notification stopped");
                        }

                        notificationIsPlayed = false;
                        notificationMediaPlayer = null;

                        PPApplication.startHandlerThread();
                        final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
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
                PPApplication.startHandlerThread();
                final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
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
                PPApplication.startHandlerThread();
                final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
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
