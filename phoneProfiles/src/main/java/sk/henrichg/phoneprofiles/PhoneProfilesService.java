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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.telephony.TelephonyManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.widget.RemoteViews;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class PhoneProfilesService extends Service {

    private static volatile PhoneProfilesService instance = null;
    private boolean serviceHasFirstStart = false;
    private boolean serviceRunning = false;
    //private boolean runningInForeground = false;

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
    private PPPExtenderBroadcastReceiver pppExtenderBroadcastReceiver = null;
    private DonationBroadcastReceiver donationBroadcastReceiver = null;

    private ExportPPDataBroadcastReceiver exportPPDataBroadcastReceiver = null;
    private SettingsContentObserver settingsContentObserver = null;
    private PPWifiNetworkCallback wifiConnectionCallback = null;

    String connectToSSID = Profile.CONNECTTOSSID_JUSTANY;

    private AudioManager audioManager = null;
    private MediaPlayer notificationMediaPlayer = null;
    private boolean notificationIsPlayed = false;
    private Timer notificationPlayTimer = null;

    //static final String EXTRA_SHOW_PROFILE_NOTIFICATION = "show_profile_notification";
    static final String EXTRA_START_ON_BOOT = "start_on_boot";
    static final String EXTRA_START_ON_PACKAGE_REPLACE = "start_on_package_replace";
    //static final String EXTRA_ONLY_START = "only_start";
    static final String EXTRA_INITIALIZE_START = "initialize_start";
    static final String EXTRA_ACTIVATE_PROFILES = "activate_profiles";
    //static final String EXTRA_SET_SERVICE_FOREGROUND = "set_service_foreground";
    //static final String EXTRA_CLEAR_SERVICE_FOREGROUND = "clear_service_foreground";
    static final String EXTRA_SWITCH_KEYGUARD = "switch_keyguard";

    //--------------------------

    static final String ACTION_COMMAND = PPApplication.PACKAGE_NAME + ".PhoneProfilesService.ACTION_COMMAND";
    private static final String ACTION_STOP = PPApplication.PACKAGE_NAME + ".PhoneProfilesService.ACTION_STOP_SERVICE";

    private final BroadcastReceiver commandReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            doCommand(intent);
        }
    };

    private final BroadcastReceiver stopReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                //noinspection deprecation
                context.removeStickyBroadcast(intent);
            } catch (Exception ignored) {}
            stopForeground(true);
            stopSelf();
        }
    };

    //--------------------------

    @Override
    public void onCreate()
    {
        super.onCreate();

        PPApplication.logE("PhoneProfilesService.onCreate", "xxx");

        synchronized (PPApplication.phoneProfilesServiceMutex) {
            instance = this;
        }

        serviceHasFirstStart = false;
        serviceRunning = false;
        //runningInForeground = false;

        showProfileNotification(true);

        registerReceiver(stopReceiver, new IntentFilter(ACTION_STOP));
        LocalBroadcastManager.getInstance(this).registerReceiver(commandReceiver, new IntentFilter(ACTION_COMMAND));

        final Context appContext = getApplicationContext();

        try {
            if ((Build.VERSION.SDK_INT < 26)) {
                PPApplication.setCustomKey(ApplicationPreferences.PREF_NOTIFICATION_SHOW_IN_STATUS_BAR, ApplicationPreferences.notificationStatusBar(this));
                PPApplication.setCustomKey(ApplicationPreferences.PREF_NOTIFICATION_STATUS_BAR_PERMANENT, ApplicationPreferences.notificationStatusBarPermanent(this));
                PPApplication.setCustomKey(ApplicationPreferences.PREF_NOTIFICATION_SHOW_IN_STATUS_BAR, ApplicationPreferences.notificationShowInStatusBar(this));
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
        editor.putBoolean(ProfilesPrefsActivity.PREF_START_TARGET_HELPS, true);
        editor.putBoolean(ProfilesPrefsActivity.PREF_START_TARGET_HELPS_SAVE, true);
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
        //if (!PPApplication.getApplicationStarted(getApplicationContext(), false)) {
        //    showProfileNotification();
        //    stopSelf();
        //}
    }

    @Override
    public void onDestroy()
    {
        PPApplication.logE("PhoneProfilesService.onDestroy", "xxx");

        try {
            unregisterReceiver(commandReceiver);
        } catch (Exception ignored) {}
        try {
            unregisterReceiver(stopReceiver);
        } catch (Exception ignored) {}
        unregisterReceivers();

        reenableKeyguard();

        if ((Build.VERSION.SDK_INT >= 26) || ApplicationPreferences.notificationStatusBarPermanent(getApplicationContext()))
            stopForeground(true);
        else {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null)
                notificationManager.cancel(PPApplication.PROFILE_NOTIFICATION_ID);
        }

        synchronized (PPApplication.phoneProfilesServiceMutex) {
            instance = null;
        }

        serviceHasFirstStart = false;
        serviceRunning = false;
        //runningInForeground = false;

        super.onDestroy();
    }

    static PhoneProfilesService getInstance() {
        //synchronized (PPApplication.phoneProfilesServiceMutex) {
            return instance;
        //}
    }

    public static void stop(Context context) {
        if (instance != null) {
            try {
                //noinspection deprecation
                context.sendStickyBroadcast(new Intent(ACTION_STOP));
            } catch (Exception ignored) {
            }
        }
    }

    boolean getServiceHasFirstStart() {
        return serviceHasFirstStart;
    }

    // start service for first start
    private void /*boolean*/ doForFirstStart(Intent intent/*, int flags, int startId*/) {
        PPApplication.logE("PhoneProfilesService.doForFirstStart", "PhoneProfilesService.doForFirstStart START");

        //boolean onlyStart = true;
        boolean initializeStart = false;
        boolean activateProfiles = false;
        boolean startOnBoot = false;
        boolean startOnPackageReplace = false;

        if (intent != null) {
            //onlyStart = intent.getBooleanExtra(EXTRA_ONLY_START, true);
            initializeStart = intent.getBooleanExtra(EXTRA_INITIALIZE_START, false);
            activateProfiles = intent.getBooleanExtra(EXTRA_ACTIVATE_PROFILES, false);
            startOnBoot = intent.getBooleanExtra(EXTRA_START_ON_BOOT, false);
            startOnPackageReplace = intent.getBooleanExtra(EXTRA_START_ON_PACKAGE_REPLACE, false);
        }

        //if (onlyStart)
        //    PPApplication.logE("PhoneProfilesService.doForFirstStart", "EXTRA_ONLY_START");
        if (initializeStart)
            PPApplication.logE("PhoneProfilesService.doForFirstStart", "EXTRA_INITIALIZE_START");
        if (activateProfiles)
            PPApplication.logE("PhoneProfilesService.doForFirstStart", "EXTRA_ACTIVATE_PROFILES");
        if (startOnBoot)
            PPApplication.logE("PhoneProfilesService.doForFirstStart", "EXTRA_START_ON_BOOT");
        if (startOnPackageReplace)
            PPApplication.logE("PhoneProfilesService.doForFirstStart", "EXTRA_START_ON_PACKAGE_REPLACE");

        PPApplication.logE("PhoneProfilesService.doForFirstStart", "serviceRunning="+serviceRunning);

        if (serviceRunning && /*onlyStart &&*/ !startOnBoot && !startOnPackageReplace && !initializeStart) {
            PPApplication.logE("PhoneProfilesService.doForFirstStart", "service already running");
            PPApplication.logE("PhoneProfilesService.doForFirstStart", "PhoneProfilesService.doForFirstStart END");
            return;// true;
        }

        /*if ((!startOnPackageReplace) && PPApplication.isNewVersion(getApplicationContext())) {
            PPApplication.logE("PhoneProfilesService.doForFirstStart", "is new version but not EXTRA_START_ON_PACKAGE_REPLACE");
            PPApplication.logE("PhoneProfilesService.doForFirstStart", "PhoneProfilesService.doForFirstStart END");
            return true;
        }*/

        serviceRunning = true;

        final Context appContext = getApplicationContext();

        //if (onlyStart) {
            //if (startOnPackageReplace) {
            //  moved to PackageReplacedReceiver
            //}

            final boolean _startOnBoot = startOnBoot;
            final boolean _startOnPackageReplace = startOnPackageReplace;
            final boolean _initializeStart = initializeStart;
            final boolean _activateProfiles = activateProfiles;
            PPApplication.startHandlerThread();
            final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    PPApplication.logE("PhoneProfilesService.doForFirstStart - handler", "PhoneProfilesService.doForFirstStart.2 START");

                    PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":PhoneProfilesService.doForFirstStart.2");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        File sd = Environment.getExternalStorageDirectory();
                        File exportDir = new File(sd, PPApplication.EXPORT_PATH);
                        if (!(exportDir.exists() && exportDir.isDirectory())) {
                            //noinspection ResultOfMethodCallIgnored
                            exportDir.mkdirs();
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

                        if (PPApplication.logEnabled()) {
                            // get list of TRANSACTIONS for "phone"
                            Object serviceManager = PPApplication.getServiceManager("phone");
                            if (serviceManager != null) {
                                PPApplication.getTransactionCode(String.valueOf(serviceManager), "");
                            }
                        }

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
                                } catch (Exception ignored) {
                                }
                            }
                            PPApplication.logE("PhoneProfilesService.doForFirstStart - handler", "PhoneProfilesService.doForFirstStart.2 END");
                            return;
                        }

                        serviceHasFirstStart = true;

                        DataWrapper dataWrapper = new DataWrapper(appContext, false, 0, false);

                        PPApplication.createNotificationChannels(appContext);
                        dataWrapper.setDynamicLauncherShortcuts();

                        if (_startOnBoot || _startOnPackageReplace || _initializeStart) {
                            PPApplication.logE("PhoneProfilesService.doForFirstStart - handler", " application not started, start it");

                            //Permissions.clearMergedPermissions(appContext);

                            //if (!TonesHandler.isToneInstalled(/*TonesHandler.TONE_ID,*/ appContext))
                            //    TonesHandler.installTone(TonesHandler.TONE_ID, TonesHandler.TONE_NAME, appContext);
                            DatabaseHandler.getInstance(appContext).fixPhoneProfilesSilentInProfiles();

                            //int startType = intent.getStringExtra(PPApplication.EXTRA_FIRST_START_TYPE);

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
                            NotMoreMaintainedNotification.showNotification(appContext);

                            ProfileDurationAlarmBroadcastReceiver.removeAlarm(appContext);
                            Profile.setActivatedProfileForDuration(appContext, 0);

                            LockDeviceActivityFinishBroadcastReceiver.removeAlarm(appContext);
                        }

                        if (PhoneProfilesService.getInstance() != null)
                            PhoneProfilesService.getInstance().registerReceivers();
                        DonationBroadcastReceiver.setAlarm(appContext);

                        if (_startOnBoot || _startOnPackageReplace || _initializeStart) {
                            PPApplication.logE("$$$ PhoneProfilesService.doForFirstStart - handler", "application started");

                            if (_activateProfiles) {
                                dataWrapper.activateProfileOnBoot();
                            }
                        }

                        if (!_startOnBoot && !_startOnPackageReplace && !_initializeStart) {
                            PPApplication.logE("$$$ PhoneProfilesService.doForFirstStart - handler", "###### not initialize start ######");
                                dataWrapper.activateProfileOnBoot();
                        }


                        dataWrapper.invalidateDataWrapper();

                        PPApplication.logE("PhoneProfilesService.doForFirstStart - handler", "PhoneProfilesService.doForFirstStart.2 END");
                    } finally {
                        if ((wakeLock != null) && wakeLock.isHeld()) {
                            try {
                                wakeLock.release();
                            } catch (Exception ignored) {}
                        }
                    }
                }
            });
        //}

        PPApplication.logE("PhoneProfilesService.doForFirstStart", "PhoneProfilesService.doForFirstStart END");

        //return onlyStart;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "intent="+intent);

        //if ((intent == null) || (!intent.getBooleanExtra(EXTRA_CLEAR_SERVICE_FOREGROUND, false))) {
            // do not call this from handlerThread. In Android 8 handlerThread is not called
            // when for service is not displayed foreground notification
            showProfileNotification(true);
        //}

        /*if (!PPApplication.getApplicationStarted(getApplicationContext(), false)) {
            stopSelf();
            return START_NOT_STICKY;
        }*/

        /*if (intent != null) {
            if (intent.getBooleanExtra(EXTRA_START_ON_PACKAGE_REPLACE, false)) {
                unregisterReceivers();

                reenableKeyguard();

                serviceHasFirstStart = false;
                serviceRunning = false;
                runningInForeground = false;
            }
        }*/

        /*if (!doForFirstStart(intent)) {
        }*/
        doForFirstStart(intent);

        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    private void doCommand(Intent intent) {
        if (intent != null) {
            /*if (intent.getBooleanExtra(EXTRA_SHOW_PROFILE_NOTIFICATION, false)) {
                PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "EXTRA_SHOW_PROFILE_NOTIFICATION");
                // not needed, is already called in start of onStartCommand
                //showProfileNotification();
            }
            else
            if (intent.getBooleanExtra(EXTRA_CLEAR_SERVICE_FOREGROUND, false)) {
                PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "EXTRA_CLEAR_SERVICE_FOREGROUND");
                clearProfileNotification();
            }
            else
            if (intent.getBooleanExtra(EXTRA_SET_SERVICE_FOREGROUND, false)) {
                PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "EXTRA_SET_SERVICE_FOREGROUND");
                // not needed, is already called in start of onStartCommand
                //showProfileNotification();
            }
            else*/
            if (intent.getBooleanExtra(EXTRA_SWITCH_KEYGUARD, false)) {
                PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "EXTRA_SWITCH_KEYGUARD");

                Context appContext = getApplicationContext();

                //boolean isScreenOn;
                //PowerManager pm = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                //isScreenOn = ((pm != null) && PPApplication.isScreenOn(pm));

                boolean secureKeyguard;
                if (keyguardManager == null)
                    keyguardManager = (KeyguardManager) appContext.getSystemService(Activity.KEYGUARD_SERVICE);
                if (keyguardManager != null) {
                    secureKeyguard = keyguardManager.isKeyguardSecure();
                    PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "secureKeyguard=" + secureKeyguard);
                    if (!secureKeyguard) {
                        //PPApplication.logE("$$$ PhoneProfilesService.onStartCommand xxx", "getLockScreenDisabled=" + ActivateProfileHelper.getLockScreenDisabled(appContext));

                        if (PPApplication.isScreenOn) {
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

    /*
    DO NOT CALL THIS !!! THIS IS CALLED ALSO WHEN, FOR EXAMPLE, ACTIVATOR GETS DISPLAYED !!!
    @Override
    public void onTaskRemoved(Intent rootIntent)
    {
        //PPApplication.logE("$$$ PhoneProfilesService.onTaskRemoved", "xxx");

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
    */

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        showProfileNotification(false);
        ActivateProfileHelper.updateGUI(getApplicationContext(), true);
    }

    private void registerReceivers() {
        //Context appContext = getApplicationContext();

        if (shutdownBroadcastReceiver != null) {
            try {
                unregisterReceiver(shutdownBroadcastReceiver);
                shutdownBroadcastReceiver = null;
            } catch (Exception e) {
                shutdownBroadcastReceiver = null;
            }
        }
        shutdownBroadcastReceiver = new ShutdownBroadcastReceiver();
        IntentFilter intentFilter50 = new IntentFilter();
        intentFilter50.addAction(Intent.ACTION_SHUTDOWN);
        registerReceiver(shutdownBroadcastReceiver, intentFilter50);

        if (screenOnOffReceiver != null) {
            try {
                unregisterReceiver(screenOnOffReceiver);
                screenOnOffReceiver = null;
            } catch (Exception e) {
                screenOnOffReceiver = null;
            }
        }
        screenOnOffReceiver = new ScreenOnOffBroadcastReceiver();
        IntentFilter intentFilter5 = new IntentFilter();
        intentFilter5.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter5.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter5.addAction(Intent.ACTION_USER_PRESENT);
        registerReceiver(screenOnOffReceiver, intentFilter5);

        if (android.os.Build.VERSION.SDK_INT >= 23) {
            if (interruptionFilterChangedReceiver != null) {
                try {
                    unregisterReceiver(interruptionFilterChangedReceiver);
                    interruptionFilterChangedReceiver = null;
                } catch (Exception e) {
                    interruptionFilterChangedReceiver = null;
                }
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
            try {
                unregisterReceiver(phoneCallBroadcastReceiver);
                phoneCallBroadcastReceiver = null;
            } catch (Exception e) {
                phoneCallBroadcastReceiver = null;
            }
        }
        phoneCallBroadcastReceiver = new PhoneCallBroadcastReceiver();
        IntentFilter intentFilter6 = new IntentFilter();
        // removed, not needed for unlink volumes
        //intentFilter6.addAction(Intent.ACTION_NEW_OUTGOING_CALL);
        intentFilter6.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        registerReceiver(phoneCallBroadcastReceiver, intentFilter6);

        if (ringerModeChangeReceiver != null) {
            try {
                unregisterReceiver(ringerModeChangeReceiver);
                ringerModeChangeReceiver = null;
            } catch (Exception e) {
                ringerModeChangeReceiver = null;
            }
        }
        ringerModeChangeReceiver = new RingerModeChangeReceiver();
        IntentFilter intentFilter7 = new IntentFilter();
        intentFilter7.addAction(AudioManager.RINGER_MODE_CHANGED_ACTION);
        registerReceiver(ringerModeChangeReceiver, intentFilter7);

        if (wifiStateChangedBroadcastReceiver != null) {
            try {
                unregisterReceiver(wifiStateChangedBroadcastReceiver);
                wifiStateChangedBroadcastReceiver = null;
            } catch (Exception e) {
                wifiStateChangedBroadcastReceiver = null;
            }
        }
        wifiStateChangedBroadcastReceiver = new WifiStateChangedBroadcastReceiver();
        IntentFilter intentFilter8 = new IntentFilter();
        intentFilter8.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        registerReceiver(wifiStateChangedBroadcastReceiver, intentFilter8);

        if (pppExtenderBroadcastReceiver != null) {
            try {
                unregisterReceiver(pppExtenderBroadcastReceiver);
                pppExtenderBroadcastReceiver = null;
            } catch (Exception e) {
                pppExtenderBroadcastReceiver = null;
            }
        }
        pppExtenderBroadcastReceiver = new PPPExtenderBroadcastReceiver();
        IntentFilter intentFilter23 = new IntentFilter();
        intentFilter23.addAction(PPApplication.ACTION_ACCESSIBILITY_SERVICE_CONNECTED);
        registerReceiver(pppExtenderBroadcastReceiver, intentFilter23,
                PPApplication.ACCESSIBILITY_SERVICE_PERMISSION, null);

        if (donationBroadcastReceiver != null) {
            try {
                unregisterReceiver(donationBroadcastReceiver);
                donationBroadcastReceiver = null;
            } catch (Exception e) {
                donationBroadcastReceiver = null;
            }
        }
        donationBroadcastReceiver = new DonationBroadcastReceiver();
        IntentFilter intentFilter30 = new IntentFilter();
        intentFilter30.addAction(PPApplication.ACTION_DONATION);
        registerReceiver(donationBroadcastReceiver, intentFilter30);

        if (pppExtenderForceStopApplicationBroadcastReceiver != null) {
            try {
                unregisterReceiver(pppExtenderForceStopApplicationBroadcastReceiver);
                pppExtenderForceStopApplicationBroadcastReceiver = null;
            } catch (Exception e) {
                pppExtenderForceStopApplicationBroadcastReceiver = null;
            }
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
        IntentFilter intentFilter24 = new IntentFilter();
        intentFilter24.addAction(PPApplication.ACTION_ACCESSIBILITY_SERVICE_UNBIND);
        intentFilter24.addAction(PPApplication.ACTION_FORCE_STOP_APPLICATIONS_END);
        registerReceiver(pppExtenderForceStopApplicationBroadcastReceiver, intentFilter24,
                PPApplication.ACCESSIBILITY_SERVICE_PERMISSION, null);

        intent = new Intent(PPApplication.ACTION_REGISTER_PPPE_FUNCTION);
        intent.putExtra(PPApplication.EXTRA_REGISTRATION_APP, "PhoneProfiles");
        intent.putExtra(PPApplication.EXTRA_REGISTRATION_TYPE, PPApplication.REGISTRATION_TYPE_LOCK_DEVICE_UNREGISTER);
        sendBroadcast(intent, PPApplication.ACCESSIBILITY_SERVICE_PERMISSION);
        intent = new Intent(PPApplication.ACTION_REGISTER_PPPE_FUNCTION);
        intent.putExtra(PPApplication.EXTRA_REGISTRATION_APP, "PhoneProfiles");
        intent.putExtra(PPApplication.EXTRA_REGISTRATION_TYPE, PPApplication.REGISTRATION_TYPE_LOCK_DEVICE_REGISTER);
        sendBroadcast(intent, PPApplication.ACCESSIBILITY_SERVICE_PERMISSION);

        if (exportPPDataBroadcastReceiver != null) {
            try {
                unregisterReceiver(exportPPDataBroadcastReceiver);
                exportPPDataBroadcastReceiver = null;
            } catch (Exception e) {
                exportPPDataBroadcastReceiver = null;
            }
        }
        exportPPDataBroadcastReceiver = new ExportPPDataBroadcastReceiver();
        intentFilter23 = new IntentFilter();
        intentFilter23.addAction(PPApplication.ACTION_EXPORT_PP_DATA_START);
        registerReceiver(exportPPDataBroadcastReceiver, intentFilter23,
                PPApplication.EXPORT_PP_DATA_PERMISSION, null);


        if (settingsContentObserver != null) {
            try {
                getContentResolver().unregisterContentObserver(settingsContentObserver);
                settingsContentObserver = null;
            } catch (Exception e) {
                settingsContentObserver = null;
            }
        }
        try {
            //settingsContentObserver = new SettingsContentObserver(this, new Handler(getMainLooper()));
            settingsContentObserver = new SettingsContentObserver(this, new Handler());
            getContentResolver().registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, settingsContentObserver);
        } catch (Exception ignored) {}

        if (wifiConnectionCallback != null) {
            try {
                ConnectivityManager connectivityManager =
                        (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                if (connectivityManager != null) {
                    connectivityManager.unregisterNetworkCallback(wifiConnectionCallback);
                }
                wifiConnectionCallback = null;
            } catch (Exception e) {
                wifiConnectionCallback = null;
            }
        }
        else {
            try {
                ConnectivityManager connectivityManager =
                        (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                if (connectivityManager != null) {
                    NetworkRequest networkRequest = new NetworkRequest.Builder()
                            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                            .build();

                    wifiConnectionCallback = new PPWifiNetworkCallback();
                    connectivityManager.registerNetworkCallback(networkRequest, wifiConnectionCallback);
                }
            } catch (Exception e) {
                wifiConnectionCallback = null;
                PPApplication.recordException(e);
            }
        }
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
            Intent intent = new Intent(PPApplication.ACTION_REGISTER_PPPE_FUNCTION);
            intent.putExtra(PPApplication.EXTRA_REGISTRATION_APP, "PhoneProfiles");
            intent.putExtra(PPApplication.EXTRA_REGISTRATION_TYPE, PPApplication.REGISTRATION_TYPE_FORCE_STOP_APPLICATIONS_UNREGISTER);
            sendBroadcast(intent, PPApplication.ACCESSIBILITY_SERVICE_PERMISSION);
        }
        if (pppExtenderBroadcastReceiver != null) {
            unregisterReceiver(pppExtenderBroadcastReceiver);
            pppExtenderBroadcastReceiver = null;
        }
        if (donationBroadcastReceiver != null) {
            unregisterReceiver(donationBroadcastReceiver);
            donationBroadcastReceiver = null;
        }
        if (exportPPDataBroadcastReceiver != null) {
            unregisterReceiver(exportPPDataBroadcastReceiver);
            exportPPDataBroadcastReceiver = null;
        }

        if (settingsContentObserver != null) {
            getContentResolver().unregisterContentObserver(settingsContentObserver);
            settingsContentObserver = null;
        }

        if (wifiConnectionCallback != null) {
            ConnectivityManager connectivityManager =
                    (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager != null) {
                connectivityManager.unregisterNetworkCallback(wifiConnectionCallback);
            }
            wifiConnectionCallback = null;
        }
    }

    // profile notification -------------------------------------------

    @SuppressLint("NewApi")
    private void _showProfileNotification(Profile profile, boolean inHandlerThread)
    {
        //PPApplication.logE("PhoneProfilesService.showProfileNotification", "xxx");

        /*
        if (ActivateProfileHelper.lockRefresh)
            // no refresh notification
            return;
        */

        final Context appContext = getApplicationContext();

        if ((Build.VERSION.SDK_INT >= 26) || ApplicationPreferences.notificationStatusBar(appContext))
        {
            //PPApplication.logE("PhoneProfilesService.showProfileNotification", "show");

            boolean notificationShowInStatusBar = ApplicationPreferences.notificationShowInStatusBar(appContext);
            boolean notificationStatusBarPermanent = ApplicationPreferences.notificationStatusBarPermanent(appContext);
            //boolean notificationDarkBackground = ApplicationPreferences.notificationDarkBackground(appContext);
            boolean notificationUseDecoration = ApplicationPreferences.notificationUseDecoration(appContext);
            boolean notificationPrefIndicator = ApplicationPreferences.notificationPrefIndicator(appContext);
            boolean notificationHideInLockScreen = ApplicationPreferences.notificationHideInLockScreen(appContext);
            String notificationStatusBarStyle = ApplicationPreferences.notificationStatusBarStyle(appContext);
            String notificationTextColor = ApplicationPreferences.notificationTextColor(appContext);
            String notificationBackgroundColor = ApplicationPreferences.notificationBackgroundColor(appContext);

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

            /*UiModeManager uiModeManager = (UiModeManager) appContext.getSystemService(Context.UI_MODE_SERVICE);
            if (uiModeManager != null) {
                uiModeManager.setNightMode(UiModeManager.MODE_NIGHT_NO);
            }*/

            boolean notificationDarkBackground = false;
            if (notificationBackgroundColor.equals("1")) {
                notificationDarkBackground = true;
            }
            else
            if (notificationBackgroundColor.equals("2")) {
                int nightModeFlags =
                        appContext.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
                switch (nightModeFlags) {
                    case Configuration.UI_MODE_NIGHT_YES:
                        notificationDarkBackground = true;
                        notificationTextColor = "2";
                        break;
                    case Configuration.UI_MODE_NIGHT_NO:
                        notificationTextColor = "1";
                        break;
                    case Configuration.UI_MODE_NIGHT_UNDEFINED:
                        break;
                }
            }

            boolean useDecorator = (!(PPApplication.deviceIsXiaomi && PPApplication.romIsMIUI)) || (Build.VERSION.SDK_INT >= 26);
            useDecorator = useDecorator && notificationUseDecoration;
            useDecorator = useDecorator && (!notificationDarkBackground) && (!notificationBackgroundColor.equals("2"));

            boolean profileIconExists = true;
            if (PPApplication.deviceIsXiaomi && PPApplication.romIsMIUI) {
                if (android.os.Build.VERSION.SDK_INT >= 24) {
                    if (!useDecorator)
                        contentViewLarge = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer_miui_no_decorator);
                    else
                        contentViewLarge = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer_miui);
                    if (!useDecorator)
                        contentView = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer_compact_miui_no_decorator);
                    else {
                        contentView = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer_compact);
                        profileIconExists = false;
                    }
                }
                else
                    contentViewLarge = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer_miui);
                //PPApplication.logE("PhoneProfilesService.showProfileNotification", "miui");
            }
            else
            if (PPApplication.deviceIsHuawei && PPApplication.romIsEMUI) {
                if (android.os.Build.VERSION.SDK_INT >= 24) {
                    if (!useDecorator)
                        contentViewLarge = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer_emui_no_decorator);
                    else
                        contentViewLarge = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer_emui);
                    if (!useDecorator)
                        contentView = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer_compact_emui_no_decorator);
                    else {
                        contentView = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer_compact);
                        profileIconExists = false;
                    }
                }
                else
                    contentViewLarge = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer_emui);
            }
            else
            if (PPApplication.deviceIsSamsung) {
                if (android.os.Build.VERSION.SDK_INT >= 24) {
                    if (!useDecorator)
                        contentViewLarge = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer_samsung_no_decorator);
                    else
                        contentViewLarge = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer);
                    if (!useDecorator)
                        contentView = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer_compact_samsung_no_decorator);
                    else {
                        contentView = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer_compact);
                        profileIconExists = false;
                    }
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
                    else {
                        contentView = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer_compact);
                        profileIconExists = false;
                    }
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
                    if (notificationPrefIndicator)
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

            if (Build.VERSION.SDK_INT >= 26) {
                PPApplication.createProfileNotificationChannel(appContext);
                notificationBuilder = new Notification.Builder(appContext, PPApplication.PROFILE_NOTIFICATION_CHANNEL);
                //notificationBuilder.setSettingsText("Test");
            }
            else {
                notificationBuilder = new Notification.Builder(appContext);
                if (notificationShowInStatusBar) {
                    KeyguardManager myKM = (KeyguardManager) appContext.getSystemService(Context.KEYGUARD_SERVICE);
                    if (myKM != null) {
                        //boolean screenUnlocked = !myKM.inKeyguardRestrictedInputMode();
                        boolean screenUnlocked = !myKM.isKeyguardLocked();
                        //boolean screenUnlocked = getScreenUnlocked(context);
                        if ((notificationHideInLockScreen && (!screenUnlocked)) ||
                                ((profile != null) && profile._hideStatusBarIcon))
                            notificationBuilder.setPriority(Notification.PRIORITY_MIN);
                        else
                            notificationBuilder.setPriority(Notification.PRIORITY_DEFAULT);
                    } else
                        notificationBuilder.setPriority(Notification.PRIORITY_DEFAULT);
                } else
                    notificationBuilder.setPriority(Notification.PRIORITY_MIN);
            }

            notificationBuilder.setContentIntent(pIntent);
            notificationBuilder.setColor(ContextCompat.getColor(appContext, R.color.notificationDecorationColor));
            notificationBuilder.setCategory(Notification.CATEGORY_STATUS);
            notificationBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);

            //notificationBuilder.setTicker(profileName);

            if (inHandlerThread) {
                if (isIconResourceID)
                {
                    int iconSmallResource;
                    if (iconBitmap != null) {
                        if (notificationStatusBarStyle.equals("0")) {
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
                                    //noinspection ConstantConditions
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
                                //noinspection ConstantConditions
                                iconSmallResource = Profile.profileIconNotifyId.get(iconIdentifier);
                            } catch (Exception ignored) {}
                            notificationBuilder.setSmallIcon(iconSmallResource);
                        }

                        contentViewLarge.setImageViewBitmap(R.id.notification_activated_profile_icon, iconBitmap);
                        if (profileIconExists) {
                            if (contentView != null)
                                contentView.setImageViewBitmap(R.id.notification_activated_profile_icon, iconBitmap);
                        }
                    }
                    else {
                        if (notificationStatusBarStyle.equals("0")) {
                            // colorful icon
                            //iconSmallResource = dataWrapper.context.getResources().getIdentifier(iconIdentifier + "_notify_color", "drawable", dataWrapper.context.getPackageName());
                            //if (iconSmallResource == 0)
                            //    iconSmallResource = R.drawable.ic_profile_default;
                            iconSmallResource = R.drawable.ic_profile_default_notify_color;
                            try {
                                //noinspection ConstantConditions
                                iconSmallResource = Profile.profileIconNotifyColorId.get(iconIdentifier);
                            } catch (Exception ignored) {}
                            notificationBuilder.setSmallIcon(iconSmallResource);

                            //int iconLargeResource = dataWrapper.context.getResources().getIdentifier(iconIdentifier, "drawable", dataWrapper.context.getPackageName());
                            //if (iconLargeResource == 0)
                            //    iconLargeResource = R.drawable.ic_profile_default;
                            int iconLargeResource = Profile.getIconResource(iconIdentifier);
                            Bitmap largeIcon = BitmapFactory.decodeResource(appContext.getResources(), iconLargeResource);
                            contentViewLarge.setImageViewBitmap(R.id.notification_activated_profile_icon, largeIcon);
                            if (profileIconExists) {
                                if (contentView != null)
                                    contentView.setImageViewBitmap(R.id.notification_activated_profile_icon, largeIcon);
                            }
                        } else {
                            // native icon
                            //iconSmallResource = dataWrapper.context.getResources().getIdentifier(iconIdentifier + "_notify", "drawable", dataWrapper.context.getPackageName());
                            //if (iconSmallResource == 0)
                            //    iconSmallResource = R.drawable.ic_profile_default_notify;
                            iconSmallResource = R.drawable.ic_profile_default_notify;
                            try {
                                //noinspection ConstantConditions
                                iconSmallResource = Profile.profileIconNotifyId.get(iconIdentifier);
                            } catch (Exception ignored) {}
                            notificationBuilder.setSmallIcon(iconSmallResource);

                            //int iconLargeResource = dataWrapper.context.getResources().getIdentifier(iconIdentifier, "drawable", dataWrapper.context.getPackageName());
                            //if (iconLargeResource == 0)
                            //    iconLargeResource = R.drawable.ic_profile_default;
                            int iconLargeResource = Profile.getIconResource(iconIdentifier);
                            Bitmap largeIcon = BitmapFactory.decodeResource(appContext.getResources(), iconLargeResource);
                            contentViewLarge.setImageViewBitmap(R.id.notification_activated_profile_icon, largeIcon);
                            if (profileIconExists) {
                                if (contentView != null)
                                    contentView.setImageViewBitmap(R.id.notification_activated_profile_icon, largeIcon);
                            }
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
                        if (notificationStatusBarStyle.equals("0"))
                            iconSmallResource = R.drawable.ic_profile_default;
                        else
                            iconSmallResource = R.drawable.ic_profile_default_notify;
                        notificationBuilder.setSmallIcon(iconSmallResource);
                    }

                    if (iconBitmap != null)
                        contentViewLarge.setImageViewBitmap(R.id.notification_activated_profile_icon, iconBitmap);
                    else
                        contentViewLarge.setImageViewResource(R.id.notification_activated_profile_icon, R.drawable.ic_profile_default);
                    if (profileIconExists) {
                        if (contentView != null) {
                            if (iconBitmap != null)
                                contentView.setImageViewBitmap(R.id.notification_activated_profile_icon, iconBitmap);
                            else
                                contentView.setImageViewResource(R.id.notification_activated_profile_icon, R.drawable.ic_profile_default);
                        }
                    }
                }
            }
            else {
                notificationBuilder.setSmallIcon(R.drawable.ic_empty);
                contentViewLarge.setImageViewResource(R.id.notification_activated_profile_icon, R.drawable.ic_empty);
                if (profileIconExists) {
                    if (contentView != null)
                        contentView.setImageViewResource(R.id.notification_activated_profile_icon, R.drawable.ic_empty);
                }
            }

            if (notificationDarkBackground) {
                int color = ContextCompat.getColor(appContext, R.color.notificationBackground_dark);
                contentViewLarge.setInt(R.id.notification_activated_profile_root, "setBackgroundColor", color);
                if (contentView != null)
                    contentView.setInt(R.id.notification_activated_profile_root, "setBackgroundColor", color);
            }
            else {
                contentViewLarge.setInt(R.id.notification_activated_profile_root, "setBackgroundColor", Color.TRANSPARENT);
                if (contentView != null)
                    contentView.setInt(R.id.notification_activated_profile_root, "setBackgroundColor", Color.TRANSPARENT);
            }

            if (notificationTextColor.equals("1") && (!notificationDarkBackground)) {
                contentViewLarge.setTextColor(R.id.notification_activated_profile_name, Color.BLACK);
                if (contentView != null)
                    contentView.setTextColor(R.id.notification_activated_profile_name, Color.BLACK);
            }
            else
            if (notificationTextColor.equals("2") || notificationDarkBackground) {
                contentViewLarge.setTextColor(R.id.notification_activated_profile_name, Color.WHITE);
                if (contentView != null)
                    contentView.setTextColor(R.id.notification_activated_profile_name, Color.WHITE);
            }

            contentViewLarge.setTextViewText(R.id.notification_activated_profile_name, profileName);
            if (contentView != null)
                contentView.setTextViewText(R.id.notification_activated_profile_name, profileName);
            notificationBuilder.setContentTitle(profileName);
            notificationBuilder.setContentText(profileName);

            if ((preferencesIndicator != null) && (notificationPrefIndicator))
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

                Notification.Action.Builder actionBuilder;
                //if (Build.VERSION.SDK_INT >= 23)
                    actionBuilder = new Notification.Action.Builder(
                            Icon.createWithResource(appContext, R.drawable.ic_action_exit_app_white),
                            appContext.getString(R.string.menu_exit),
                            pExitAppIntent);
                /*else
                    actionBuilder = new Notification.Action.Builder(
                            R.drawable.ic_action_exit_app_white,
                            appContext.getString(R.string.menu_exit),
                            pExitAppIntent);*/
                /*Notification.Action.Builder actionBuilder = new Notification.Action.Builder(
                        R.drawable.ic_action_exit_app_white,
                        appContext.getString(R.string.menu_exit),
                        pExitAppIntent);*/
                notificationBuilder.addAction(actionBuilder.build());
            }

            notificationBuilder.setOnlyAlertOnce(true);

            try {
                Notification notification = notificationBuilder.build();

                if (Build.VERSION.SDK_INT < 26) {
                    notification.flags &= ~Notification.FLAG_SHOW_LIGHTS;
                    notification.ledOnMS = 0;
                    notification.ledOffMS = 0;
                }

                if ((Build.VERSION.SDK_INT >= 26) || notificationStatusBarPermanent) {
                    //notification.flags |= Notification.FLAG_NO_CLEAR;
                    notification.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
                } else {
                    setAlarmForNotificationCancel(appContext);
                }

                if ((Build.VERSION.SDK_INT >= 26) || notificationStatusBarPermanent) {
                    startForeground(PPApplication.PROFILE_NOTIFICATION_ID, notification);
                    //runningInForeground = true;
                }
                else {
                    NotificationManager notificationManager = (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);
                    if (notificationManager != null)
                        notificationManager.notify(PPApplication.PROFILE_NOTIFICATION_ID, notification);
                }

            } catch (Exception ignored) {}
        }
        /*else
        {
            if ((Build.VERSION.SDK_INT >= 26) || ApplicationPreferences.notificationStatusBarPermanent(appContext))
                stopForeground(true);
            else {
                NotificationManager notificationManager = (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);
                if (notificationManager != null)
                    notificationManager.cancel(PPApplication.PROFILE_NOTIFICATION_ID);
            }
        }*/
    }

    void showProfileNotification(boolean forServiceStart) {
        final Context appContext = getApplicationContext();
        final DataWrapper dataWrapper = new DataWrapper(appContext, false, 0, false);
        final Profile profile = dataWrapper.getActivatedProfileFromDB(false, false);
        dataWrapper.invalidateDataWrapper();

        synchronized (PPApplication.phoneProfilesServiceMutex) {
            //if (!runningInForeground || (instance == null)) {
            if (forServiceStart) {
                _showProfileNotification(profile, false);
            }
        }

        PPApplication.startHandlerThreadProfileNotification();
        final Handler handler = new Handler(PPApplication.handlerThreadProfileNotification.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                synchronized (PPApplication.phoneProfilesServiceMutex) {
                    if (instance != null) {
                        DataWrapper dataWrapper = new DataWrapper(instance.getApplicationContext(), false, 0, false);
                        Profile profile = dataWrapper.getActivatedProfileFromDB(false, false);
                        instance._showProfileNotification(profile, true);
                        dataWrapper.invalidateDataWrapper();
                    }
                }
            }
        });
    }

    void clearProfileNotification(/*Context context, boolean onlyEmpty*/)
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
            //runningInForeground = false;
        }
    }

    private void setAlarmForNotificationCancel(Context context)
    {
        if (Build.VERSION.SDK_INT >= 26)
            return;

        String notificationStatusBarCancel = ApplicationPreferences.notificationStatusBarCancel(context);

        if (notificationStatusBarCancel.isEmpty() || notificationStatusBarCancel.equals("0"))
            return;

        Intent intent = new Intent(context, NotificationCancelAlarmBroadcastReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            long time = SystemClock.elapsedRealtime() + Integer.parseInt(notificationStatusBarCancel) * 1000;

            alarmManager.set(AlarmManager.ELAPSED_REALTIME, time, pendingIntent);
        }
    }

    //----------------------------------------

    // switch keyguard ------------------------------------

    private void disableKeyguard()
    {
        //PPApplication.logE("$$$ Keyguard.disable","keyguardLock="+keyguardLock);
        if ((keyguardLock != null) && Permissions.hasPermission(getApplicationContext(), Manifest.permission.DISABLE_KEYGUARD))
            keyguardLock.disableKeyguard();
    }

    private void reenableKeyguard()
    {
        //PPApplication.logE("$$$ Keyguard.reenable","keyguardLock="+keyguardLock);
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
                //PPApplication.logE("PhoneProfilesService.playNotificationSound", "vibration");
                try {
                    if (Build.VERSION.SDK_INT >= 26) {
                        vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                    } else {
                        vibrator.vibrate(500);
                    }
                } catch (Exception ignored) {}
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
                            //PPApplication.logE("PhoneProfilesService.playNotificationSound", "notification stopped");
                        }

                        notificationIsPlayed = false;
                        notificationMediaPlayer = null;

                        PPApplication.startHandlerThreadInternalChangeToFalse();
                        final Handler handler = new Handler(PPApplication.handlerThreadInternalChangeToFalse.getLooper());
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
                //PPApplication.logE("PhoneProfilesService.playNotificationSound", "security exception");
                stopPlayNotificationSound();
                PPApplication.startHandlerThreadInternalChangeToFalse();
                final Handler handler = new Handler(PPApplication.handlerThreadInternalChangeToFalse.getLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        RingerModeChangeReceiver.internalChange = false;
                    }
                }, 3000);
                Permissions.grantPlayRingtoneNotificationPermissions(this);
            } catch (Exception e) {
                //PPApplication.logE("PhoneProfilesService.playNotificationSound", "exception");
                stopPlayNotificationSound();
                PPApplication.startHandlerThreadInternalChangeToFalse();
                final Handler handler = new Handler(PPApplication.handlerThreadInternalChangeToFalse.getLooper());
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
