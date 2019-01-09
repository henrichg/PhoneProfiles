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
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.widget.RemoteViews;

import com.crashlytics.android.Crashlytics;

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
    private PPPExtenderBroadcastReceiver pppExtenderForceStopApplicationBroadcastReceiver = null;

    private SettingsContentObserver settingsContentObserver = null;

    String connectToSSID = Profile.CONNECTTOSSID_JUSTANY;

    private AudioManager audioManager = null;
    private MediaPlayer notificationMediaPlayer = null;
    private boolean notificationIsPlayed = false;
    private Timer notificationPlayTimer = null;

    static final String EXTRA_SHOW_PROFILE_NOTIFICATION = "show_profile_notification";
    static final String EXTRA_START_ON_BOOT = "start_on_boot";
    static final String EXTRA_START_ON_PACKAGE_REPLACE = "start_on_package_replace";
    static final String EXTRA_ONLY_START = "only_start";
    static final String EXTRA_INITIALIZE_START = "initialize_start";
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

        /*if (PPApplication.getApplicationStarted(getApplicationContext(), false))
            doForFirstStart(null);
        else {
            showProfileNotification();
            stopSelf();
        }*/
        if (!PPApplication.getApplicationStarted(getApplicationContext(), false)) {
            showProfileNotification();
            stopSelf();
        }
    }

    @Override
    public void onDestroy()
    {
        PPApplication.logE("PhoneProfilesService.onDestroy", "xxx");

        unregisterReceivers();

        reenableKeyguard();

        if ((Build.VERSION.SDK_INT >= 26) || ApplicationPreferences.notificationStatusBarPermanent(getApplicationContext()))
            stopForeground(true);
        else {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null)
                notificationManager.cancel(PPApplication.PROFILE_NOTIFICATION_ID);
        }

        /*synchronized (PhoneProfilesService.class) {
            instance = null;
        }*/

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
        PPApplication.logE("PhoneProfilesService.doForFirstStart", "PhoneProfilesService.doForFirstStart START");

        boolean onlyStart = true;
        boolean initializeStart = false;
        boolean startOnBoot = false;
        boolean startOnPackageReplace = false;

        if (intent != null) {
            onlyStart = intent.getBooleanExtra(EXTRA_ONLY_START, true);
            initializeStart = intent.getBooleanExtra(EXTRA_INITIALIZE_START, false);
            startOnBoot = intent.getBooleanExtra(EXTRA_START_ON_BOOT, false);
            startOnPackageReplace = intent.getBooleanExtra(EXTRA_START_ON_PACKAGE_REPLACE, false);
        }

        if (onlyStart)
            PPApplication.logE("PhoneProfilesService.doForFirstStart", "EXTRA_ONLY_START");
        if (initializeStart)
            PPApplication.logE("PhoneProfilesService.doForFirstStart", "EXTRA_INITIALIZE_START");
        if (startOnBoot)
            PPApplication.logE("PhoneProfilesService.doForFirstStart", "EXTRA_START_ON_BOOT");
        if (startOnPackageReplace)
            PPApplication.logE("PhoneProfilesService.doForFirstStart", "EXTRA_START_ON_PACKAGE_REPLACE");

        PPApplication.logE("PhoneProfilesService.doForFirstStart", "serviceRunning="+serviceRunning);

        if (serviceRunning && onlyStart && !startOnBoot && !startOnPackageReplace && !initializeStart) {
            PPApplication.logE("PhoneProfilesService.doForFirstStart", "only EXTRA_ONLY_START, service already running");
            PPApplication.logE("PhoneProfilesService.doForFirstStart", "PhoneProfilesService.doForFirstStart END");
            return true;
        }

        if ((!startOnPackageReplace) && PPApplication.isNewVersion(getApplicationContext())) {
            PPApplication.logE("PhoneProfilesService.doForFirstStart", "is new version but not EXTRA_START_ON_PACKAGE_REPLACE");
            PPApplication.logE("PhoneProfilesService.doForFirstStart", "PhoneProfilesService.doForFirstStart END");
            return true;
        }

        serviceRunning = true;

        final Context appContext = getApplicationContext();

        if (onlyStart) {
            //if (startOnPackageReplace) {
            //  moved to PackageReplacedReceiver
            //}

            final boolean _startOnBoot = startOnBoot;
            final boolean _startOnPackageReplace = startOnPackageReplace;
            final boolean _initializeStart = initializeStart;
            PPApplication.startHandlerThread();
            final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    PPApplication.logE("PhoneProfilesService.doForFirstStart - handler", "PhoneProfilesService.doForFirstStart.2 START");

                    PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME+":PhoneProfilesService.doForFirstStart.2");
                        wakeLock.acquire(10 * 60 * 1000);
                    }

                    // is called from PPApplication
                    //PPApplication.initRoot();
                    if (!ApplicationPreferences.applicationNeverAskForGrantRoot(appContext)) {
                        // grant root
                        PPApplication.isRootGranted();
                    }
                    //PPApplication.getSUVersion();
                    PPApplication.settingsBinaryExists(false);
                    PPApplication.serviceBinaryExists(false);
                    PPApplication.getServicesList();

                    GlobalGUIRoutines.setLanguage(appContext);

                    if (_startOnBoot || _startOnPackageReplace || _initializeStart) {
                        // restart first start
                        serviceHasFirstStart = false;
                    }

                    //if (PPApplication.getApplicationStarted(appContext, false)) {
                    if (serviceHasFirstStart) {
                        PPApplication.logE("PhoneProfilesService.doForFirstStart - handler", " application already started");
                        if ((wakeLock != null) && wakeLock.isHeld()) {
                            try {
                                wakeLock.release();
                            } catch (Exception ignored) {}
                        }
                        PPApplication.logE("PhoneProfilesService.doForFirstStart - handler", "PhoneProfilesService.doForFirstStart.2 END");
                        return;
                    }

                    DataWrapper dataWrapper = new DataWrapper(appContext, false, 0, false);

                    PPApplication.createNotificationChannels(appContext);
                    dataWrapper.setDynamicLauncherShortcuts();

                    if (_startOnBoot || _startOnPackageReplace || _initializeStart) {
                        PPApplication.logE("PhoneProfilesService.doForFirstStart - handler", " application not started, start it");

                        //Permissions.clearMergedPermissions(appContext);

                        //int startType = intent.getStringExtra(PPApplication.EXTRA_FIRST_START_TYPE);

                        //TonesHandler.installTone(TonesHandler.TONE_ID, TonesHandler.TONE_NAME, appContext, false);

                        ActivateProfileHelper.setLockScreenDisabled(appContext, false);

                        ActivateProfileHelper.setMergedRingNotificationVolumes(appContext, true);

                        AudioManager audioManager = (AudioManager) appContext.getSystemService(Context.AUDIO_SERVICE);
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
                    }

                    if (PhoneProfilesService.getInstance() != null)
                        PhoneProfilesService.getInstance().registerReceivers();
                    AboutApplicationJob.scheduleJob(appContext, false);

                    if (_startOnBoot || _startOnPackageReplace || _initializeStart) {
                        PPApplication.logE("$$$ PhoneProfilesService.doForFirstStart - handler", "application started");

                        dataWrapper.activateProfile(0, PPApplication.STARTUP_SOURCE_BOOT, null);
                    }
                    if (!_startOnBoot && !_startOnPackageReplace && !_initializeStart) {
                        PPApplication.logE("$$$ PhoneProfilesService.doForFirstStart - handler", "###### not initialize start ######");
                        if (ApplicationPreferences.applicationActivate(appContext))
                        {
                            Profile profile = DatabaseHandler.getInstance(appContext).getActivatedProfile();
                            long profileId = 0;
                            if (profile != null)
                                profileId = profile._id;
                            dataWrapper.activateProfile(profileId, PPApplication.STARTUP_SOURCE_BOOT, null/*, ""*/);
                        }
                    }

                    dataWrapper.invalidateDataWrapper();

                    serviceHasFirstStart = true;

                    PPApplication.logE("PhoneProfilesService.doForFirstStart - handler", "PhoneProfilesService.doForFirstStart.2 END");

                    if ((wakeLock != null) && wakeLock.isHeld()) {
                        try {
                            wakeLock.release();
                        } catch (Exception ignored) {}
                    }
                }
            });
        }

        PPApplication.logE("PhoneProfilesService.doForFirstStart", "PhoneProfilesService.doForFirstStart END");

        return onlyStart;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "intent="+intent);

        //if ((intent == null) || (!intent.getBooleanExtra(EXTRA_CLEAR_SERVICE_FOREGROUND, false))) {
            // do not call this from handlerThread. In Android 8 handlerThread is not called
            // when for service is not displayed foreground notification
            showProfileNotification();
        //}

        if (!PPApplication.getApplicationStarted(getApplicationContext(), false)) {
            stopSelf();
            return START_NOT_STICKY;
        }

        if (intent != null) {
            if (intent.getBooleanExtra(EXTRA_START_ON_PACKAGE_REPLACE, false)) {
                unregisterReceivers();

                reenableKeyguard();

                serviceHasFirstStart = false;
                serviceRunning = false;
                runningInForeground = false;
            }
        }

        if (!doForFirstStart(intent/*, flags, startId*/)) {
            if (intent != null) {
                if (intent.getBooleanExtra(EXTRA_SHOW_PROFILE_NOTIFICATION, false)) {
                    PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "EXTRA_SHOW_PROFILE_NOTIFICATION");
                    // not needed, is already called in start of onStartCommand
                    //showProfileNotification();
                }
                else
                if (intent.getBooleanExtra(EXTRA_CLEAR_SERVICE_FOREGROUND, false)) {
                    PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "EXTRA_CLEAR_SERVICE_FOREGROUND");
                    clearProfileNotification(/*this*/);
                }
                else
                if (intent.getBooleanExtra(EXTRA_SET_SERVICE_FOREGROUND, false)) {
                    PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "EXTRA_SET_SERVICE_FOREGROUND");
                    // not needed, is already called in start of onStartCommand
                    //showProfileNotification();
                }
                else
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
        //Context appContext = getApplicationContext();

        if (shutdownBroadcastReceiver != null) {
            unregisterReceiver(shutdownBroadcastReceiver);
            shutdownBroadcastReceiver = null;
        }
        shutdownBroadcastReceiver = new ShutdownBroadcastReceiver();
        IntentFilter intentFilter50 = new IntentFilter();
        intentFilter50.addAction(Intent.ACTION_SHUTDOWN);
        registerReceiver(shutdownBroadcastReceiver, intentFilter50);

        if (screenOnOffReceiver != null) {
            unregisterReceiver(screenOnOffReceiver);
            screenOnOffReceiver = null;
        }
        screenOnOffReceiver = new ScreenOnOffBroadcastReceiver();
        IntentFilter intentFilter5 = new IntentFilter();
        intentFilter5.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter5.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter5.addAction(Intent.ACTION_USER_PRESENT);
        registerReceiver(screenOnOffReceiver, intentFilter5);

        if (android.os.Build.VERSION.SDK_INT >= 23) {
            if (interruptionFilterChangedReceiver != null) {
                unregisterReceiver(interruptionFilterChangedReceiver);
                interruptionFilterChangedReceiver = null;
            }
            boolean no60 = !Build.VERSION.RELEASE.equals("6.0");
            if (no60 && GlobalGUIRoutines.activityActionExists(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS, this)) {
                interruptionFilterChangedReceiver = new InterruptionFilterChangedBroadcastReceiver();
                IntentFilter intentFilter11 = new IntentFilter();
                intentFilter11.addAction(NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED);
                registerReceiver(interruptionFilterChangedReceiver, intentFilter11);
            }
        }

        if (phoneCallBroadcastReceiver != null) {
            unregisterReceiver(phoneCallBroadcastReceiver);
            phoneCallBroadcastReceiver = null;
        }
        phoneCallBroadcastReceiver = new PhoneCallBroadcastReceiver();
        IntentFilter intentFilter6 = new IntentFilter();
        // removed, not needed for unlink volumes
        //intentFilter6.addAction(Intent.ACTION_NEW_OUTGOING_CALL);
        intentFilter6.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        registerReceiver(phoneCallBroadcastReceiver, intentFilter6);

        if (ringerModeChangeReceiver != null) {
            unregisterReceiver(ringerModeChangeReceiver);
            ringerModeChangeReceiver = null;
        }
        ringerModeChangeReceiver = new RingerModeChangeReceiver();
        IntentFilter intentFilter7 = new IntentFilter();
        intentFilter7.addAction(AudioManager.RINGER_MODE_CHANGED_ACTION);
        registerReceiver(ringerModeChangeReceiver, intentFilter7);

        if (wifiStateChangedBroadcastReceiver != null) {
            unregisterReceiver(wifiStateChangedBroadcastReceiver);
            wifiStateChangedBroadcastReceiver = null;
        }
        wifiStateChangedBroadcastReceiver = new WifiStateChangedBroadcastReceiver();
        IntentFilter intentFilter8 = new IntentFilter();
        intentFilter8.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        registerReceiver(wifiStateChangedBroadcastReceiver, intentFilter8);

        if (pppExtenderForceStopApplicationBroadcastReceiver != null) {
            unregisterReceiver(pppExtenderForceStopApplicationBroadcastReceiver);
            pppExtenderForceStopApplicationBroadcastReceiver = null;
            Intent intent = new Intent(PPApplication.ACTION_REGISTER_PPPE_FUNCTION);
            intent.putExtra(PPApplication.EXTRA_REGISTRATION_APP, "PhoneProfiles");
            intent.putExtra(PPApplication.EXTRA_REGISTRATION_TYPE, PPApplication.REGISTRATION_TYPE_FORCE_STOP_APPLICATIONS_UNREGISTER);
            sendBroadcast(intent, PPApplication.ACCESSIBILITY_SERVICE_PERMISSION);
        }
        Intent intent = new Intent(PPApplication.ACTION_REGISTER_PPPE_FUNCTION);
        intent.putExtra(PPApplication.EXTRA_REGISTRATION_APP, "PhoneProfiles");
        intent.putExtra(PPApplication.EXTRA_REGISTRATION_TYPE, PPApplication.REGISTRATION_TYPE_FORCE_STOP_APPLICATIONS_REGISTER);
        sendBroadcast(intent, PPApplication.ACCESSIBILITY_SERVICE_PERMISSION);
        pppExtenderForceStopApplicationBroadcastReceiver = new PPPExtenderBroadcastReceiver();
        IntentFilter intentFilter23 = new IntentFilter();
        intentFilter23.addAction(PPApplication.ACTION_ACCESSIBILITY_SERVICE_UNBIND);
        intentFilter23.addAction(PPApplication.ACTION_FORCE_STOP_APPLICATIONS_END);
        registerReceiver(pppExtenderForceStopApplicationBroadcastReceiver, intentFilter23,
                PPApplication.ACCESSIBILITY_SERVICE_PERMISSION, null);

        if (settingsContentObserver != null) {
            getContentResolver().unregisterContentObserver(settingsContentObserver);
            settingsContentObserver = null;
        }
        try {
            //settingsContentObserver = new SettingsContentObserver(this, new Handler(getMainLooper()));
            settingsContentObserver = new SettingsContentObserver(this, new Handler());
            getContentResolver().registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, settingsContentObserver);
        } catch (Exception ignored) {}
    }

    private void unregisterReceivers() {
        if (shutdownBroadcastReceiver != null) {
            unregisterReceiver(shutdownBroadcastReceiver);
            shutdownBroadcastReceiver = null;
        }
        if (screenOnOffReceiver != null) {
            unregisterReceiver(screenOnOffReceiver);
            screenOnOffReceiver = null;
        }
        if (android.os.Build.VERSION.SDK_INT >= 23)
            if (interruptionFilterChangedReceiver != null) {
                unregisterReceiver(interruptionFilterChangedReceiver);
                interruptionFilterChangedReceiver = null;
            }
        if (phoneCallBroadcastReceiver != null) {
            unregisterReceiver(phoneCallBroadcastReceiver);
            phoneCallBroadcastReceiver = null;
        }
        if (ringerModeChangeReceiver != null) {
            unregisterReceiver(ringerModeChangeReceiver);
            ringerModeChangeReceiver = null;
        }
        if (wifiStateChangedBroadcastReceiver != null) {
            unregisterReceiver(wifiStateChangedBroadcastReceiver);
            wifiStateChangedBroadcastReceiver = null;
        }
        if (pppExtenderForceStopApplicationBroadcastReceiver != null) {
            unregisterReceiver(pppExtenderForceStopApplicationBroadcastReceiver);
            pppExtenderForceStopApplicationBroadcastReceiver = null;
        }

        if (settingsContentObserver != null) {
            getContentResolver().unregisterContentObserver(settingsContentObserver);
            settingsContentObserver = null;
        }
    }

    // profile notification -------------------------------------------

    @SuppressLint("NewApi")
    private void _showProfileNotification(Profile profile, boolean inHandlerThread)
    {
        PPApplication.logE("PhoneProfilesService.showProfileNotification", "xxx");

        /*
        if (ActivateProfileHelper.lockRefresh)
            // no refresh notification
            return;
        */

        final Context appContext = getApplicationContext();

        if ((PhoneProfilesService.getInstance() != null) && ((Build.VERSION.SDK_INT >= 26) || ApplicationPreferences.notificationStatusBar(appContext)))
        {
            PPApplication.logE("PhoneProfilesService.showProfileNotification", "show");

            // close showed notification
            //notificationManager.cancel(PPApplication.NOTIFICATION_ID);
            Intent intent = new Intent(appContext, ActivateProfileActivity.class);
            // clear all opened activities
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_NOTIFICATION);
            int requestCode = 0;
            if (inHandlerThread && (profile != null))
                requestCode = (int)profile._id;
            PendingIntent pIntent = PendingIntent.getActivity(appContext, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            Notification.Builder notificationBuilder;

            /*
            boolean miui = (PPApplication.romManufacturer != null) &&
                    (PPApplication.romManufacturer.compareToIgnoreCase("xiaomi") == 0)// &&
                    //(android.os.Build.VERSION.SDK_INT >= 24);
            */
            //boolean miui = PPApplication.romIsMIUI;

            RemoteViews contentView = null;
            RemoteViews contentViewLarge;

            boolean darkBackground = ApplicationPreferences.notificationDarkBackground(appContext);

            boolean useDecorator = (!PPApplication.romIsMIUI) || (Build.VERSION.SDK_INT >= 26);
            useDecorator = useDecorator && ApplicationPreferences.notificationUseDecoration(appContext);
            useDecorator = useDecorator && (!darkBackground);

            if (PPApplication.romIsMIUI) {
                if (android.os.Build.VERSION.SDK_INT >= 24) {
                    if (!useDecorator)
                        contentViewLarge = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer_miui_no_decorator);
                    else
                        contentViewLarge = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer_miui);
                    if (!useDecorator)
                        contentView = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer_compact_miui_no_decorator);
                    else
                        contentView = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer_compact);
                }
                else
                    contentViewLarge = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer_miui);
                PPApplication.logE("PhoneProfilesService.showProfileNotification", "miui");
            }
            else
            if (PPApplication.romIsEMUI) {
                if (android.os.Build.VERSION.SDK_INT >= 24) {
                    if (!useDecorator)
                        contentViewLarge = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer_emui_no_decorator);
                    else
                        contentViewLarge = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer_emui);
                    if (!useDecorator)
                        contentView = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer_compact_emui_no_decorator);
                    else
                        contentView = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer_compact);
                }
                else
                    contentViewLarge = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer_emui);
            }
            else
            if (PPApplication.romIsSamsung) {
                if (android.os.Build.VERSION.SDK_INT >= 24) {
                    if (!useDecorator)
                        contentViewLarge = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer_samsung_no_decorator);
                    else
                        contentViewLarge = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer);
                    if (!useDecorator)
                        contentView = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer_compact_samsung_no_decorator);
                    else
                        contentView = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer_compact);
                }
                else
                    contentViewLarge = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer);
            }
            else {
                if (android.os.Build.VERSION.SDK_INT >= 24) {
                    if (!useDecorator)
                        contentViewLarge = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer_no_decorator);
                    else
                        contentViewLarge = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer);
                    if (!useDecorator)
                        contentView = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer_compact_no_decorator);
                    else
                        contentView = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer_compact);
                }
                else
                    contentViewLarge = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer);
            }
            //}

            boolean isIconResourceID;
            String iconIdentifier;
            Spannable profileName;
            Bitmap iconBitmap;
            Bitmap preferencesIndicator;

            if (profile != null)
            {
                isIconResourceID = profile.getIsIconResourceID();
                iconIdentifier = profile.getIconIdentifier();
                profileName = profile.getProfileNameWithDuration("", false, appContext);

                if (inHandlerThread) {
                    profile.generateIconBitmap(appContext, false, 0, false);
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
                String pName;
                if (inHandlerThread)
                    pName = appContext.getResources().getString(R.string.profiles_header_profile_name_no_activated);
                else
                    pName = appContext.getResources().getString(R.string.empty_string);
                profileName = new SpannableString(pName);
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

            //notificationBuilder.setTicker(profileName);

            if (inHandlerThread) {
                if (isIconResourceID)
                {
                    int iconSmallResource;
                    if (iconBitmap != null) {
                        if (ApplicationPreferences.notificationStatusBarStyle(appContext).equals("0")) {
                            // colorful icon

                            // FC in Note 4, 6.0.1 :-/
                            boolean isNote4 = (Build.MANUFACTURER.compareToIgnoreCase("samsung") == 0) &&
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

                        contentViewLarge.setImageViewBitmap(R.id.notification_activated_profile_icon, iconBitmap);
                        if ((android.os.Build.VERSION.SDK_INT >= 24) && (!useDecorator)/* && (contentView != null)*/)
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
                            contentViewLarge.setImageViewBitmap(R.id.notification_activated_profile_icon, largeIcon);
                            if ((android.os.Build.VERSION.SDK_INT >= 24) && (!useDecorator)/* && (contentView != null)*/)
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
                            contentViewLarge.setImageViewBitmap(R.id.notification_activated_profile_icon, largeIcon);
                            if ((android.os.Build.VERSION.SDK_INT >= 24) && (!useDecorator)/* && (contentView != null)*/)
                                contentView.setImageViewBitmap(R.id.notification_activated_profile_icon, largeIcon);
                        }
                    }
                }
                else
                {
                    // FC in Note 4, 6.0.1 :-/
                    boolean isNote4 = (Build.MANUFACTURER.compareToIgnoreCase("samsung") == 0) &&
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
                        contentViewLarge.setImageViewBitmap(R.id.notification_activated_profile_icon, iconBitmap);
                    else
                        contentViewLarge.setImageViewResource(R.id.notification_activated_profile_icon, R.drawable.ic_profile_default);
                    if ((android.os.Build.VERSION.SDK_INT >= 24) && (!useDecorator)/* && (contentView != null)*/) {
                        if (iconBitmap != null)
                            contentView.setImageViewBitmap(R.id.notification_activated_profile_icon, iconBitmap);
                        else
                            contentView.setImageViewResource(R.id.notification_activated_profile_icon, R.drawable.ic_profile_default);
                    }
                }
            }
            else {
                notificationBuilder.setSmallIcon(R.drawable.ic_empty);
                contentViewLarge.setImageViewResource(R.id.notification_activated_profile_icon, R.drawable.ic_empty);
                if ((android.os.Build.VERSION.SDK_INT >= 24) && (!useDecorator)/* && (contentView != null)*/)
                    contentView.setImageViewResource(R.id.notification_activated_profile_icon, R.drawable.ic_empty);
            }

            if (darkBackground) {
                int color = getResources().getColor(R.color.notificationBackground_dark);
                contentViewLarge.setInt(R.id.notification_activated_profile_root, "setBackgroundColor", color);
                if ((Build.VERSION.SDK_INT >= 24)/* && (contentView != null)*/)
                    contentView.setInt(R.id.notification_activated_profile_root, "setBackgroundColor", color);
            }

            if (ApplicationPreferences.notificationTextColor(appContext).equals("1") && (!darkBackground)) {
                contentViewLarge.setTextColor(R.id.notification_activated_profile_name, Color.BLACK);
                if ((Build.VERSION.SDK_INT >= 24)/* && (contentView != null)*/)
                    contentView.setTextColor(R.id.notification_activated_profile_name, Color.BLACK);
            }
            else
            if (ApplicationPreferences.notificationTextColor(appContext).equals("2") || darkBackground) {
                contentViewLarge.setTextColor(R.id.notification_activated_profile_name, Color.WHITE);
                if ((Build.VERSION.SDK_INT >= 24)/* && (contentView != null)*/)
                    contentView.setTextColor(R.id.notification_activated_profile_name, Color.WHITE);
            }

            contentViewLarge.setTextViewText(R.id.notification_activated_profile_name, profileName);
            if ((Build.VERSION.SDK_INT >= 24)/* && (contentView != null)*/)
                contentView.setTextViewText(R.id.notification_activated_profile_name, profileName);

            if ((preferencesIndicator != null) && (ApplicationPreferences.notificationPrefIndicator(appContext)))
                contentViewLarge.setImageViewBitmap(R.id.notification_activated_profile_pref_indicator, preferencesIndicator);
            else
                contentViewLarge.setImageViewResource(R.id.notification_activated_profile_pref_indicator, R.drawable.ic_empty);

            if (android.os.Build.VERSION.SDK_INT >= 24) {
                if (useDecorator)
                    notificationBuilder.setStyle(new Notification.DecoratedCustomViewStyle());
                //if (contentView != null) {
                    String layoutType = ApplicationPreferences.notificationLayoutType(appContext);
                    switch (layoutType) {
                        case "1":
                            // only large layout
                            notificationBuilder.setCustomContentView(contentViewLarge);
                            break;
                        case "2":
                            // only small layout
                            notificationBuilder.setCustomContentView(contentView);
                            break;
                        default:
                            // expandable layout
                            notificationBuilder.setCustomContentView(contentView);
                            notificationBuilder.setCustomBigContentView(contentViewLarge);
                            break;
                    }
                //}
                //else
                //    notificationBuilder.setCustomContentView(contentViewLarge);
            }
            else
                notificationBuilder.setContent(contentViewLarge);

            if ((Build.VERSION.SDK_INT >= 24) &&
                    (ApplicationPreferences.notificationShowButtonExit(appContext)) &&
                    useDecorator) {
                // add action button to stop application

                // intent to LauncherActivity, for click on notification
                Intent exitAppIntent = new Intent(appContext, ExitApplicationActivity.class);
                // clear all opened activities
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                PendingIntent pExitAppIntent = PendingIntent.getActivity(appContext, 0, exitAppIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                Notification.Action.Builder actionBuilder = new Notification.Action.Builder(
                        Icon.createWithResource(appContext, R.drawable.ic_action_exit_app_white),
                        appContext.getString(R.string.menu_exit),
                        pExitAppIntent);
                notificationBuilder.addAction(actionBuilder.build());
            }

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
                    if (PhoneProfilesService.getInstance() != null)
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
        final DataWrapper dataWrapper = new DataWrapper(appContext, false, 0, false);
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
                if (PhoneProfilesService.getInstance() != null) {
                    DataWrapper dataWrapper = new DataWrapper(PhoneProfilesService.getInstance().getApplicationContext(), false, 0, false);
                    Profile profile = dataWrapper.getActivatedProfileFromDB(false, false);
                    if (PhoneProfilesService.getInstance() != null)
                        PhoneProfilesService.getInstance()._showProfileNotification(profile, true);
                    dataWrapper.invalidateDataWrapper();
                }
            }
        });
    }

    private void clearProfileNotification(/*Context context, boolean onlyEmpty*/)
    {
        /*if (onlyEmpty) {
            final Context appContext = getApplicationContext();
            final DataWrapper dataWrapper = new DataWrapper(appContext, false, 0, false);
            //final Profile profile = dataWrapper.getActivatedProfileFromDB(false, false);
            _showProfileNotification(null, false);
            dataWrapper.invalidateDataWrapper();
        }
        else*/ {
        final Context appContext = getApplicationContext();
            if ((Build.VERSION.SDK_INT >= 26) || ApplicationPreferences.notificationStatusBarPermanent(appContext))
                stopForeground(true);
            else {
                NotificationManager notificationManager = (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);
                if (notificationManager != null)
                    notificationManager.cancel(PPApplication.PROFILE_NOTIFICATION_ID);
            }
            runningInForeground = false;
        }
    }

    private void setAlarmForNotificationCancel(Context context)
    {
        if (Build.VERSION.SDK_INT >= 26)
            return;

        if (ApplicationPreferences.notificationStatusBarCancel(context).isEmpty() || ApplicationPreferences.notificationStatusBarCancel(context).equals("0"))
            return;

        Intent intent = new Intent(context, NotificationCancelAlarmBroadcastReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            long time = SystemClock.elapsedRealtime() + Integer.valueOf(ApplicationPreferences.notificationStatusBarCancel(context)) * 1000;

            alarmManager.set(AlarmManager.ELAPSED_REALTIME, time, pendingIntent);
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
