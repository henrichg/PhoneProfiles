package sk.henrichg.phoneprofiles;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.PowerManager;
import android.support.multidex.MultiDex;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.util.Pair;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.evernote.android.job.JobConfig;
import com.evernote.android.job.JobManager;
import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.look.Slook;
import com.stericson.RootShell.RootShell;
import com.stericson.RootShell.execution.Command;
import com.stericson.RootTools.RootTools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.fabric.sdk.android.Fabric;

//import com.google.firebase.analytics.FirebaseAnalytics;

//import com.github.anrwatchdog.ANRError;
//import com.github.anrwatchdog.ANRWatchDog;

public class PPApplication extends Application {

    //static final String romManufacturer = getROMManufacturer();
    static final boolean romIsMIUI = isMIUI();
    static final boolean romIsEMUI = isEMUI();
    static final boolean romIsSamsung = isSamsung();
    static String PACKAGE_NAME;

    //static final int VERSION_CODE_EXTENDER_1_0_4 = 60;
    //static final int VERSION_CODE_EXTENDER_2_0 = 100;
    static final int VERSION_CODE_EXTENDER_3_0 = 200;
    static final int VERSION_CODE_EXTENDER_4_0 = 400;
    static final int VERSION_CODE_EXTENDER_LATEST = VERSION_CODE_EXTENDER_4_0;

    public static final String EXPORT_PATH = "/PhoneProfiles";
    private static final String LOG_FILENAME = "log.txt";

    @SuppressWarnings("PointlessBooleanExpression")
    private static final boolean logIntoLogCat = true && BuildConfig.DEBUG;
    static final boolean logIntoFile = false;
    private static final boolean rootToolsDebug = false;
    private static final String logFilterTags = "##### PPApplication.onCreate"
                                            +"|PhoneProfilesService.onCreate"
                                            +"|PhoneProfilesService.onStartCommand"
                                            +"|PhoneProfilesService.doForFirstStart"
                                            +"|PhoneProfilesService.showProfileNotification"
                                            +"|PhoneProfilesService.onDestroy"
                                            +"|BootUpReceiver"
                                            +"|PackageReplacedReceiver"
                                            +"|ShutdownBroadcastReceiver"

                                            //+"|ProfileDurationAlarmBroadcastReceiver"

                                            //+"|ActivateProfilesHelper.executeForForceStopApplications"
                                            //+"|DataWrapper.setDynamicLauncherShortcuts"

                                            //+"|ActivateProfileHelper.execute"
                                            //+"|ActivateProfileHelper.setScreenTimeout"
                                            //+"|ActivateProfileHelper.screenTimeoutLock"
                                            //+"|@@@ ScreenOnOffBroadcastReceiver.onReceive"
            ;

    static final String EXTRA_PROFILE_ID = "profile_id";
    static final String EXTRA_STARTUP_SOURCE = "startup_source";

    static final int STARTUP_SOURCE_NOTIFICATION = 1;
    static final int STARTUP_SOURCE_WIDGET = 2;
    static final int STARTUP_SOURCE_SHORTCUT = 3;
    static final int STARTUP_SOURCE_BOOT = 4;
    static final int STARTUP_SOURCE_ACTIVATOR = 5;
    static final int STARTUP_SOURCE_SERVICE = 6;
    static final int STARTUP_SOURCE_EDITOR = 8;
    static final int STARTUP_SOURCE_ACTIVATOR_START = 9;
    static final int STARTUP_SOURCE_EXTERNAL_APP = 10;
    static final int STARTUP_SOURCE_SERVICE_MANUAL = 11;

    static final int PREFERENCES_STARTUP_SOURCE_ACTIVITY = 1;
    //static final int PREFERENCES_STARTUP_SOURCE_FRAGMENT = 2;
    static final int PREFERENCES_STARTUP_SOURCE_SHARED_PROFILE = 3;

    static final String PROFILE_NOTIFICATION_CHANNEL = "phoneProfiles_activated_profile";
    static final String INFORMATION_NOTIFICATION_CHANNEL = "phoneProfiles_information";
    static final String EXCLAMATION_NOTIFICATION_CHANNEL = "phoneProfiles_exclamation";
    static final String GRANT_PERMISSION_NOTIFICATION_CHANNEL = "phoneProfiles_grant_permission";

    static final int PROFILE_NOTIFICATION_ID = 700420;
    static final int IMPORTANT_INFO_NOTIFICATION_ID = 700422;
    static final int GRANT_PROFILE_PERMISSIONS_NOTIFICATION_ID = 700423;
    static final int GRANT_INSTALL_TONE_PERMISSIONS_NOTIFICATION_ID = 700424;
    static final int ACTION_FOR_EXTERNAL_APPLICATION_NOTIFICATION_ID = 700425;
    static final int PROFILE_ACTIVATION_MOBILE_DATA_PREFS_NOTIFICATION_ID = 700426;
    static final int PROFILE_ACTIVATION_LOCATION_PREFS_NOTIFICATION_ID = 700427;
    static final int PROFILE_ACTIVATION_WIFI_AP_PREFS_NOTIFICATION_ID = 700428;
    static final int ABOUT_APPLICATION_DONATE_NOTIFICATION_ID = 700429;
    static final int PROFILE_ACTIVATION_NETWORK_TYPE_PREFS_NOTIFICATION_ID = 700430;
    static final int GRANT_PLAY_RINGTONE_NOTIFICATION_PERMISSIONS_NOTIFICATION_ID = 700431;
    static final int GRANT_LOG_TO_FILE_PERMISSIONS_NOTIFICATION_ID = 700432;

    static final String APPLICATION_PREFS_NAME = "phone_profile_preferences";
    static final String SHARED_PROFILE_PREFS_NAME = "profile_preferences_default_profile";
    //static final String PERMISSIONS_PREFS_NAME = "permissions_list";
    static final String PERMISSIONS_STATUS_PREFS_NAME = "permissions_status";
    static final String WIFI_CONFIGURATION_LIST_PREFS_NAME = "wifi_configuration_list";

    private static final String PREF_APPLICATION_STARTED = "applicationStarted";
    private static final String PREF_SAVED_VERSION_CODE = "saved_version_code";
    private static final String PREF_DAYS_AFTER_FIRST_START = "days_after_first_start";
    private static final String PREF_DONATION_NOTIFICATION_COUNT = "donation_notification_count";
    private static final String PREF_DONATION_DONATED = "donation_donated";

    static final String EXTENDER_ACCESSIBILITY_SERVICE_ID = "sk.henrichg.phoneprofilesplusextender/.PPPEAccessibilityService";

    static final String ACTION_ACCESSIBILITY_SERVICE_CONNECTED = "sk.henrichg.phoneprofilesplusextender.ACTION_ACCESSIBILITY_SERVICE_CONNECTED";
    static final String ACTION_ACCESSIBILITY_SERVICE_UNBIND = "sk.henrichg.phoneprofilesplusextender.ACTION_ACCESSIBILITY_SERVICE_UNBIND";
    static final String ACTION_REGISTER_PPPE_FUNCTION = "sk.henrichg.phoneprofilesplusextender.ACTION_REGISTER_PPPE_FUNCTION";
    static final String ACTION_FORCE_STOP_APPLICATIONS_START = "sk.henrichg.phoneprofilesplusextender.ACTION_FORCE_STOP_APPLICATIONS_START";
    static final String ACTION_FORCE_STOP_APPLICATIONS_END = "sk.henrichg.phoneprofilesplusextender.ACTION_FORCE_STOP_APPLICATIONS_END";
    static final String ACTION_LOCK_DEVICE = "sk.henrichg.phoneprofilesplusextender.ACTION_LOCK_DEVICE";
    static final String ACCESSIBILITY_SERVICE_PERMISSION = "sk.henrichg.phoneprofilesplusextender.ACCESSIBILITY_SERVICE_PERMISSION";

    static final String EXTRA_REGISTRATION_APP = "registration_app";
    static final String EXTRA_REGISTRATION_TYPE = "registration_type";
    static final int REGISTRATION_TYPE_FORCE_STOP_APPLICATIONS_REGISTER = 1;
    static final int REGISTRATION_TYPE_FORCE_STOP_APPLICATIONS_UNREGISTER = -1;
    static final int REGISTRATION_TYPE_LOCK_DEVICE_REGISTER = 5;
    static final int REGISTRATION_TYPE_LOCK_DEVICE_UNREGISTER = -5;

    static final String EXTRA_APPLICATIONS = "extra_applications";

    //@SuppressWarnings("SpellCheckingInspection")
    //static private FirebaseAnalytics mFirebaseAnalytics;

    public static HandlerThread handlerThread = null;
    public static HandlerThread handlerThreadInternalChangeToFalse = null;
    public static HandlerThread handlerThreadWidget = null;
    public static HandlerThread handlerThreadProfileNotification = null;
    public static HandlerThread handlerThreadPlayTone = null;

    public static HandlerThread handlerThreadVolumes = null;
    public static HandlerThread handlerThreadRadios = null;
    public static HandlerThread handlerThreadAdaptiveBrightness = null;
    public static HandlerThread handlerThreadWallpaper = null;
    public static HandlerThread handlerThreadPowerSaveMode = null;
    public static HandlerThread handlerThreadLockDevice = null;
    public static HandlerThread handlerThreadRunApplication = null;
    public static HandlerThread handlerThreadHeadsUpNotifications = null;
    public static HandlerThread handlerThreadNotificationLed = null;

    public static Handler brightnessHandler;
    public static Handler toastHandler;
    public static Handler screenTimeoutHandler;

    public static final PhoneProfilesServiceMutex phoneProfilesServiceMutex = new PhoneProfilesServiceMutex();
    public static final RootMutex rootMutex = new RootMutex();
    private static final ServiceListMutex serviceListMutex = new ServiceListMutex();
    public static final ScanResultsMutex scanResultsMutex = new ScanResultsMutex();

    public static boolean startedOnBoot = false;

    //public static final RootMutex rootMutex = new RootMutex();

    // Samsung Look instance
    public static Slook sLook = null;
    public static boolean sLookCocktailPanelEnabled = false;
    //public static boolean sLookCocktailBarEnabled = false;

    private static final RefreshActivitiesBroadcastReceiver refreshActivitiesBroadcastReceiver = new RefreshActivitiesBroadcastReceiver();
    private static final DashClockBroadcastReceiver dashClockBroadcastReceiver = new DashClockBroadcastReceiver();

    @Override
    public void onCreate()
    {
        super.onCreate();

        PPApplication.logE("##### PPApplication.onCreate", "romManufacturer="+Build.MANUFACTURER);
        PPApplication.logE("##### PPApplication.onCreate", "romIsMIUI="+romIsMIUI);

        if (checkAppReplacingState())
            return;

        if (logIntoFile)
            Permissions.grantLogToFilePermissions(getApplicationContext());

        try {
            // Obtain the FirebaseAnalytics instance.
            //mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

            // Set up Crashlytics, disabled for debug builds
            Crashlytics crashlyticsKit = new Crashlytics.Builder()
                    .core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
                    .build();

            Fabric.with(this, crashlyticsKit);
            // Crashlytics.getInstance().core.logException(exception); -- this log will be associated with crash log.
        } catch (Exception e) {
            /*
            java.lang.IllegalStateException:
              at android.app.ContextImpl.getSharedPreferences (ContextImpl.java:447)
              at android.app.ContextImpl.getSharedPreferences (ContextImpl.java:432)
              at android.content.ContextWrapper.getSharedPreferences (ContextWrapper.java:174)
              at io.fabric.sdk.android.services.persistence.PreferenceStoreImpl.<init> (PreferenceStoreImpl.java:39)
              at io.fabric.sdk.android.services.common.AdvertisingInfoProvider.<init> (AdvertisingInfoProvider.java:37)
              at io.fabric.sdk.android.services.common.IdManager.<init> (IdManager.java:114)
              at io.fabric.sdk.android.Fabric$Builder.build (Fabric.java:289)
              at io.fabric.sdk.android.Fabric.with (Fabric.java:340)

              This exception occurs, when storage is protected and PPP is started via LOCKED_BOOT_COMPLETED

              Code from android.app.ContextImpl:
                if (getApplicationInfo().targetSdkVersion >= android.os.Build.VERSION_CODES.O) {
                    if (isCredentialProtectedStorage()
                            && !getSystemService(UserManager.class)
                                    .isUserUnlockingOrUnlocked(UserHandle.myUserId())) {
                        throw new IllegalStateException("SharedPreferences in credential encrypted "
                                + "storage are not available until after user is unlocked");
                    }
                }
            */
            Log.e("PPPEApplication.onCreate", Log.getStackTraceString(e));
        }

        /*
        // set up ANR-WatchDog
        ANRWatchDog anrWatchDog = new ANRWatchDog();
        //anrWatchDog.setReportMainThreadOnly();
        anrWatchDog.setANRListener(new ANRWatchDog.ANRListener() {
            @Override
            public void onAppNotResponding(ANRError error) {
                Crashlytics.getInstance().core.logException(error);
            }
        });
        anrWatchDog.start();
        */

        try {
            Crashlytics.setBool("DEBUG", BuildConfig.DEBUG);
        } catch (Exception ignored) {}

        //if (BuildConfig.DEBUG) {
        int actualVersionCode = 0;
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            actualVersionCode = PPApplication.getVersionCode(pInfo);
        } catch (Exception ignored) {
        }
        Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler(getApplicationContext(), actualVersionCode));
        //}

        //	Debug.startMethodTracing("phoneprofiles");

        //long nanoTimeStart = startMeasuringRunTime();

        PACKAGE_NAME = getPackageName();

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(PPApplication.refreshActivitiesBroadcastReceiver, new IntentFilter("RefreshActivitiesBroadcastReceiver"));
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(PPApplication.dashClockBroadcastReceiver, new IntentFilter("DashClockBroadcastReceiver"));

        startHandlerThread();
        startHandlerThreadInternalChangeToFalse();
        startHandlerThreadWidget();
        startHandlerThreadProfileNotification();
        startHandlerThreadPlayTone();
        startHandlerThreadVolumes();
        startHandlerThreadRadios();
        startHandlerThreadAdaptiveBrightness();
        startHandlerThreadWallpaper();
        startHandlerThreadPowerSaveMode();
        startHandlerThreadLockDevice();
        startHandlerThreadRunApplication();
        startHandlerThreadHeadsUpNotifications();
        startHandlerThreadNotificationLed();

        toastHandler = new Handler(getMainLooper());
        brightnessHandler = new Handler(getMainLooper());
        screenTimeoutHandler = new Handler(getMainLooper());

        // initialization
        //loadPreferences(this);

        PPApplication.initRoot();

        JobConfig.setForceAllowApi14(true); // https://github.com/evernote/android-job/issues/197
        JobManager.create(this).addJobCreator(new PPJobsCreator());

        //Log.d("PPApplication.onCreate", "memory usage (after create activateProfileHelper)=" + Debug.getNativeHeapAllocatedSize());

        //Log.d("PPApplication.onCreate","xxx");

        //getMeasuredRunTime(nanoTimeStart, "PPApplication.onCreate");

        // Samsung Look initialization
        sLook = new Slook();
        try {
            sLook.initialize(this);
            // true = The Device supports Edge Single Mode, Edge Single Plus Mode, and Edge Feeds Mode.
            sLookCocktailPanelEnabled = sLook.isFeatureEnabled(Slook.COCKTAIL_PANEL);
            // true = The Device supports Edge Immersive Mode feature.
            //sLookCocktailBarEnabled = sLook.isFeatureEnabled(Slook.COCKTAIL_BAR);
        } catch (SsdkUnsupportedException e) {
            sLook = null;
        }

        if (PPApplication.getApplicationStarted(getApplicationContext(), false)) {
            try {
                PPApplication.logE("##### PPApplication.onCreate", "start service");
                Intent serviceIntent = new Intent(getApplicationContext(), PhoneProfilesService.class);
                //serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, true);
                serviceIntent.putExtra(PhoneProfilesService.EXTRA_INITIALIZE_START, true);
                serviceIntent.putExtra(PhoneProfilesService.EXTRA_ACTIVATE_PROFILES, false);
                startPPService(getApplicationContext(), serviceIntent);
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    // workaround for: java.lang.NullPointerException: Attempt to invoke virtual method
    // 'android.content.res.AssetManager android.content.res.Resources.getAssets()' on a null object reference
    // https://issuetracker.google.com/issues/36972466
    private boolean checkAppReplacingState() {
        if (getResources() == null) {
            Log.w("PPApplication.onCreate", "app is replacing...kill");
            android.os.Process.killProcess(android.os.Process.myPid());
            return true;
        }
        return false;
    }

    /*
    static boolean isNewVersion(Context appContext) {
        int oldVersionCode = PPApplication.getSavedVersionCode(appContext);
        PPApplication.logE("PPApplication.isNewVersion", "oldVersionCode="+oldVersionCode);
        int actualVersionCode;
        try {
            if (oldVersionCode == 0) {
                // save version code
                try {
                    PackageInfo pInfo = appContext.getPackageManager().getPackageInfo(appContext.getPackageName(), 0);
                    actualVersionCode = PPApplication.getVersionCode(pInfo);
                    PPApplication.setSavedVersionCode(appContext, actualVersionCode);
                } catch (Exception ignored) {
                }
                return false;
            }

            PackageInfo pInfo = appContext.getPackageManager().getPackageInfo(appContext.getPackageName(), 0);
            actualVersionCode = PPApplication.getVersionCode(pInfo);
            PPApplication.logE("PPApplication.isNewVersion", "actualVersionCode=" + actualVersionCode);

            return (oldVersionCode < actualVersionCode);
        } catch (Exception e) {
            return false;
        }
    }
    */

    static int getVersionCode(PackageInfo pInfo) {
        //noinspection deprecation
        return pInfo.versionCode;
    }

    //--------------------------------------------------------------

    static void startPPService(Context context, Intent serviceIntent) {
        if (Build.VERSION.SDK_INT < 26)
            context.getApplicationContext().startService(serviceIntent);
        else
            context.getApplicationContext().startForegroundService(serviceIntent);
    }

    static void runCommand(Context context, Intent intent) {
        PPApplication.logE("PPApplication.runCommand", "xxx");
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    //--------------------------------------------------------------

    static public boolean getApplicationStarted(Context context, boolean testService)
    {
        ApplicationPreferences.getSharedPreferences(context);
        if (testService)
            return ApplicationPreferences.preferences.getBoolean(PREF_APPLICATION_STARTED, false)  &&
                (PhoneProfilesService.getInstance() != null) && PhoneProfilesService.getInstance().getServiceHasFirstStart();
        else
            return ApplicationPreferences.preferences.getBoolean(PREF_APPLICATION_STARTED, false);
    }

    static public void setApplicationStarted(Context context, boolean appStarted)
    {
        ApplicationPreferences.getSharedPreferences(context);
        Editor editor = ApplicationPreferences.preferences.edit();
        editor.putBoolean(PREF_APPLICATION_STARTED, appStarted);
        editor.apply();
    }

    static public int getSavedVersionCode(Context context) {
        ApplicationPreferences.getSharedPreferences(context);
        return ApplicationPreferences.preferences.getInt(PREF_SAVED_VERSION_CODE, 0);
    }

    static public void setSavedVersionCode(Context context, int version)
    {
        ApplicationPreferences.getSharedPreferences(context);
        Editor editor = ApplicationPreferences.preferences.edit();
        editor.putInt(PREF_SAVED_VERSION_CODE, version);
        editor.apply();
    }


    //-------------------------------------

    // log ----------------------------------------------------------

    static private void resetLog()
    {
        File sd = Environment.getExternalStorageDirectory();
        File exportDir = new File(sd, PPApplication.EXPORT_PATH);
        if (!(exportDir.exists() && exportDir.isDirectory()))
            //noinspection ResultOfMethodCallIgnored
            exportDir.mkdirs();

        File logFile = new File(sd, EXPORT_PATH + "/" + LOG_FILENAME);
        //noinspection ResultOfMethodCallIgnored
        logFile.delete();
    }

    @SuppressWarnings("UnusedAssignment")
    @SuppressLint("SimpleDateFormat")
    static private void logIntoFile(String type, String tag, String text)
    {
        if (!logIntoFile)
            return;

        try
        {
            // warnings when logIntoFile == false
            File sd = Environment.getExternalStorageDirectory();
            File exportDir = new File(sd, PPApplication.EXPORT_PATH);
            if (!(exportDir.exists() && exportDir.isDirectory()))
                //noinspection ResultOfMethodCallIgnored
                exportDir.mkdirs();

            File logFile = new File(sd, EXPORT_PATH + "/" + LOG_FILENAME);

            if (logFile.length() > 1024 * 10000)
                resetLog();

            if (!logFile.exists())
            {
                //noinspection ResultOfMethodCallIgnored
                logFile.createNewFile();
            }
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            String log = "";
            SimpleDateFormat sdf = new SimpleDateFormat("d.MM.yy HH:mm:ss:S");
            String time = sdf.format(Calendar.getInstance().getTimeInMillis());
            log = log + time + "--" + type + "-----" + tag + "------" + text;
            buf.append(log);
            buf.newLine();
            buf.flush();
            buf.close();
        }
        catch (IOException ignored) {
        }
    }

    private static boolean logContainsFilterTag(String tag)
    {
        boolean contains = false;
        String[] splits = logFilterTags.split("\\|");
        for (String split : splits) {
            if (tag.contains(split)) {
                contains = true;
                break;
            }
        }
        return contains;
    }

    @SuppressWarnings({"BooleanMethodIsAlwaysInverted", "WeakerAccess"})
    static public boolean logEnabled() {
        //noinspection ConstantConditions
        return (logIntoLogCat || logIntoFile);
    }

    @SuppressWarnings("unused")
    static public void logI(String tag, String text)
    {
        if (!logEnabled())
            return;

        if (logContainsFilterTag(tag))
        {
            if (logIntoLogCat) Log.i(tag, text);
            logIntoFile("I", tag, text);
        }
    }

    @SuppressWarnings("unused")
    static public void logW(String tag, String text)
    {
        if (!logEnabled())
            return;

        if (logContainsFilterTag(tag))
        {
            if (logIntoLogCat) Log.w(tag, text);
            logIntoFile("W", tag, text);
        }
    }

    @SuppressWarnings("unused")
    static public void logE(String tag, String text)
    {
        if (!logEnabled())
            return;

        if (logContainsFilterTag(tag))
        {
            if (logIntoLogCat) Log.e(tag, text);
            logIntoFile("E", tag, text);
        }
    }

    @SuppressWarnings("unused")
    static public void logD(String tag, String text)
    {
        if (!logEnabled())
            return;

        if (logContainsFilterTag(tag))
        {
            if (logIntoLogCat) Log.d(tag, text);
            logIntoFile("D", tag, text);
        }
    }

    static public int getDaysAfterFirstStart(Context context)
    {
        ApplicationPreferences.getSharedPreferences(context);
        return ApplicationPreferences.preferences.getInt(PREF_DAYS_AFTER_FIRST_START, 0);
    }

    static public void setDaysAfterFirstStart(Context context, int days)
    {
        ApplicationPreferences.getSharedPreferences(context);
        Editor editor = ApplicationPreferences.preferences.edit();
        editor.putInt(PREF_DAYS_AFTER_FIRST_START, days);
        editor.apply();
    }

    static public int getDonationNotificationCount(Context context)
    {
        ApplicationPreferences.getSharedPreferences(context);
        return ApplicationPreferences.preferences.getInt(PREF_DONATION_NOTIFICATION_COUNT, 0);
    }

    static public void setDonationNotificationCount(Context context, int days)
    {
        ApplicationPreferences.getSharedPreferences(context);
        Editor editor = ApplicationPreferences.preferences.edit();
        editor.putInt(PREF_DONATION_NOTIFICATION_COUNT, days);
        editor.apply();
    }

    static public boolean getDonationDonated(Context context)
    {
        ApplicationPreferences.getSharedPreferences(context);
        return ApplicationPreferences.preferences.getBoolean(PREF_DONATION_DONATED, false);
    }

    static public void setDonationDonated(Context context)
    {
        ApplicationPreferences.getSharedPreferences(context);
        Editor editor = ApplicationPreferences.preferences.edit();
        editor.putBoolean(PREF_DONATION_DONATED, true);
        editor.apply();
    }

    //--------------------------------------------------------------

    // notification channels -------------------------

    static void createProfileNotificationChannel(/*Profile profile, */Context context) {
        if (Build.VERSION.SDK_INT >= 26) {
            int importance;
            if (ApplicationPreferences.notificationShowInStatusBar(context)) {
                /*KeyguardManager myKM = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
                if (myKM != null) {
                    //boolean screenUnlocked = !myKM.inKeyguardRestrictedInputMode();
                    //boolean screenUnlocked = getScreenUnlocked(context);
                    boolean screenUnlocked = !myKM.isKeyguardLocked();
                    if ((ApplicationPreferences.notificationHideInLockScreen(context) && (!screenUnlocked)) ||
                            ((profile != null) && profile._hideStatusBarIcon))
                        importance = NotificationManager.IMPORTANCE_MIN;
                    else
                        importance = NotificationManager.IMPORTANCE_LOW;
                }
                else*/
                    importance = NotificationManager.IMPORTANCE_DEFAULT;
            } else
                importance = NotificationManager.IMPORTANCE_MIN;

            // The user-visible name of the channel.
            CharSequence name = context.getString(R.string.notification_channel_activated_profile);
            // The user-visible description of the channel.
            String description = context.getString(R.string.notification_channel_activated_profile_description_pp);

            NotificationChannel channel = new NotificationChannel(PROFILE_NOTIFICATION_CHANNEL, name, importance);

            // Configure the notification channel.
            //channel.setImportance(importance);
            channel.setDescription(description);
            channel.enableLights(false);
            // Sets the notification light color for notifications posted to this
            // channel, if the device supports this feature.
            //channel.setLightColor(Color.RED);
            channel.enableVibration(false);
            //channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            channel.setSound(null, null);
            channel.setShowBadge(false);

            NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null)
                notificationManager.createNotificationChannel(channel);
        }
    }

    static void createInformationNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= 26) {
            // The user-visible name of the channel.
            CharSequence name = context.getString(R.string.notification_channel_information);
            // The user-visible description of the channel.
            String description = context.getString(R.string.empty_string);

            NotificationChannel channel = new NotificationChannel(INFORMATION_NOTIFICATION_CHANNEL, name, NotificationManager.IMPORTANCE_LOW);

            // Configure the notification channel.
            //channel.setImportance(importance);
            channel.setDescription(description);
            channel.enableLights(false);
            // Sets the notification light color for notifications posted to this
            // channel, if the device supports this feature.
            //channel.setLightColor(Color.RED);
            channel.enableVibration(false);
            //channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});

            NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null)
                notificationManager.createNotificationChannel(channel);
        }
    }

    static void createExclamationNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= 26) {
            // The user-visible name of the channel.
            CharSequence name = context.getString(R.string.notification_channel_exclamation);
            // The user-visible description of the channel.
            String description = context.getString(R.string.empty_string);

            NotificationChannel channel = new NotificationChannel(EXCLAMATION_NOTIFICATION_CHANNEL, name, NotificationManager.IMPORTANCE_DEFAULT);

            // Configure the notification channel.
            //channel.setImportance(importance);
            channel.setDescription(description);
            channel.enableLights(false);
            // Sets the notification light color for notifications posted to this
            // channel, if the device supports this feature.
            //channel.setLightColor(Color.RED);
            channel.enableVibration(false);
            //channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});

            NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null)
                notificationManager.createNotificationChannel(channel);
        }
    }

    static void createGrantPermissionNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= 26) {
            // The user-visible name of the channel.
            CharSequence name = context.getString(R.string.notification_channel_grant_permission);
            // The user-visible description of the channel.
            String description = context.getString(R.string.notification_channel_grant_permission_description);

            NotificationChannel channel = new NotificationChannel(GRANT_PERMISSION_NOTIFICATION_CHANNEL, name, NotificationManager.IMPORTANCE_DEFAULT);

            // Configure the notification channel.
            //channel.setImportance(importance);
            channel.setDescription(description);
            channel.enableLights(false);
            // Sets the notification light color for notifications posted to this
            // channel, if the device supports this feature.
            //channel.setLightColor(Color.RED);
            channel.enableVibration(false);
            //channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});

            NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null)
                notificationManager.createNotificationChannel(channel);
        }
    }

    static void createNotificationChannels(Context appContext) {
        PPApplication.createProfileNotificationChannel(appContext);
        PPApplication.createInformationNotificationChannel(appContext);
        PPApplication.createExclamationNotificationChannel(appContext);
        PPApplication.createGrantPermissionNotificationChannel(appContext);
    }

    static void showProfileNotification(/*Context context*/) {
        try {
            PPApplication.logE("PPApplication.showProfileNotification", "xxx");
            /*Intent serviceIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_SHOW_PROFILE_NOTIFICATION, true);
            PPApplication.startPPService(context, serviceIntent);*/
            if (PhoneProfilesService.getInstance() != null)
                PhoneProfilesService.getInstance().showProfileNotification();
        } catch (Exception ignored) {}
    }

    // -----------------------------------------------

    // root -----------------------------------------------------

    private static synchronized void initRoot() {
        synchronized (PPApplication.rootMutex) {
            rootMutex.rootChecked = false;
            rootMutex.rooted = false;
            //rootMutex.rootGranted = false;
            rootMutex.settingsBinaryChecked = false;
            rootMutex.settingsBinaryExists = false;
            //rootMutex.isSELinuxEnforcingChecked = false;
            //rootMutex.isSELinuxEnforcing = false;
            //rootMutex.suVersion = null;
            //rootMutex.suVersionChecked = false;
            rootMutex.serviceBinaryChecked = false;
            rootMutex.serviceBinaryExists = false;
        }
    }

    private static boolean _isRooted()
    {
        RootShell.debugMode = rootToolsDebug;

        if (rootMutex.rootChecked)
            return rootMutex.rooted;

        try {
            PPApplication.logE("PPApplication._isRooted", "start isRootAvailable");
            //if (RootTools.isRootAvailable()) {
            if (RootToolsSmall.isRooted()) {
                // device is rooted
                PPApplication.logE("PPApplication._isRooted", "root available");
                rootMutex.rooted = true;
            } else {
                PPApplication.logE("PPApplication._isRooted", "root NOT available");
                rootMutex.rooted = false;
                //rootMutex.settingsBinaryExists = false;
                //rootMutex.settingsBinaryChecked = false;
                //rootMutex.isSELinuxEnforcingChecked = false;
                //rootMutex.isSELinuxEnforcing = false;
                //rootMutex.suVersionChecked = false;
                //rootMutex.suVersion = null;
                //rootMutex.serviceBinaryExists = false;
                //rootMutex.serviceBinaryChecked = false;
            }
            rootMutex.rootChecked = true;
        } catch (Exception e) {
            Log.e("PPApplication._isRooted", Log.getStackTraceString(e));
        }
        //if (rooted)
        //	getSUVersion();
        return rootMutex.rooted;
    }

    static boolean isRooted(boolean fromUIThread) {
        if (rootMutex.rootChecked)
            return rootMutex.rooted;

        if (fromUIThread)
            return false;

        synchronized (PPApplication.rootMutex) {
            return _isRooted();
        }
    }

    static void isRootGranted(/*boolean onlyCheck*/)
    {
        RootShell.debugMode = rootToolsDebug;

        //if (onlyCheck && rootMutex.grantRootChecked)
        //    return rootMutex.rootGranted;

        if (isRooted(false)) {
            synchronized (PPApplication.rootMutex) {
                try {
                    PPApplication.logE("PPApplication.isRootGranted", "start isAccessGiven");
                    if (RootTools.isAccessGiven()) {
                        // root is granted
                        PPApplication.logE("PPApplication.isRootGranted", "root granted");
                        //rootMutex.rootGranted = true;
                        //rootMutex.grantRootChecked = true;
                    } else {
                        // grant denied
                        PPApplication.logE("PPApplication.isRootGranted", "root NOT granted");
                        //rootMutex.rootGranted = false;
                        //rootMutex.grantRootChecked = true;
                    }
                } catch (Exception e) {
                    Log.e("PPApplication.isRootGranted", Log.getStackTraceString(e));
                    //rootMutex.rootGranted = false;
                }
                //return rootMutex.rootGranted;
            }
        } else {
            PPApplication.logE("PPApplication.isRootGranted", "not rooted");
        }
        //return false;
    }

    static boolean settingsBinaryExists(boolean fromUIThread)
    {
        RootShell.debugMode = rootToolsDebug;

        if (rootMutex.settingsBinaryChecked)
            return rootMutex.settingsBinaryExists;

        if (fromUIThread)
            return false;

        synchronized (PPApplication.rootMutex) {
            if (!rootMutex.settingsBinaryChecked) {
                PPApplication.logE("PPApplication.settingsBinaryExists", "start");
                rootMutex.settingsBinaryExists = RootToolsSmall.hasSettingBin();
                rootMutex.settingsBinaryChecked = true;
            }
            PPApplication.logE("PPApplication.settingsBinaryExists", "settingsBinaryExists=" + rootMutex.settingsBinaryExists);
            return rootMutex.settingsBinaryExists;
        }
    }

    static boolean serviceBinaryExists(boolean fromUIThread)
    {
        RootShell.debugMode = rootToolsDebug;

        if (rootMutex.serviceBinaryChecked)
            return rootMutex.serviceBinaryExists;

        if (fromUIThread)
            return false;

        synchronized (PPApplication.rootMutex) {
            if (!rootMutex.serviceBinaryChecked) {
                PPApplication.logE("PPApplication.serviceBinaryExists", "start");
                rootMutex.serviceBinaryExists = RootToolsSmall.hasServiceBin();
                rootMutex.serviceBinaryChecked = true;
            }
            PPApplication.logE("PPApplication.serviceBinaryExists", "serviceBinaryExists=" + rootMutex.serviceBinaryExists);
            return rootMutex.serviceBinaryExists;
        }
    }

    /**
     * Detect if SELinux is set to enforcing, caches result
     *
     * @return true if SELinux set to enforcing, or false in the case of
     *         permissive or not present
     */
    /*public static boolean isSELinuxEnforcing()
    {
        RootShell.debugMode = rootToolsDebug;

        synchronized (PPApplication.rootMutex) {
            if (!isSELinuxEnforcingChecked)
            {
                boolean enforcing = false;

                // First known firmware with SELinux built-in was a 4.2 (17)
                // leak
                //if (android.os.Build.VERSION.SDK_INT >= 17) {
                    // Detect enforcing through sysfs, not always present
                    File f = new File("/sys/fs/selinux/enforce");
                    if (f.exists()) {
                        try {
                            InputStream is = new FileInputStream("/sys/fs/selinux/enforce");
                            //noinspection TryFinallyCanBeTryWithResources
                            try {
                                enforcing = (is.read() == '1');
                            } finally {
                                is.close();
                            }
                        } catch (Exception ignored) {
                        }
                    }
                //}

                isSELinuxEnforcing = enforcing;
                isSELinuxEnforcingChecked = true;
            }

            PPApplication.logE("PPApplication.isSELinuxEnforcing", "isSELinuxEnforcing="+isSELinuxEnforcing);

            return isSELinuxEnforcing;
        }
    }*/

    /*
    public static String getSELinuxEnforceCommand(String command, Shell.ShellContext context)
    {
        if ((suVersion != null) && suVersion.contains("SUPERSU"))
            return "su --context " + context.getValue() + " -c \"" + command + "\"  < /dev/null";
        else
            return command;
    }

    public static String getSUVersion()
    {
        if (!suVersionChecked)
        {
            Command command = new Command(0, false, "su -v")
            {
                @Override
                public void commandOutput(int id, String line) {
                    suVersion = line;

                    super.commandOutput(id, line);
                }
            }
            ;
            try {
                RootTools.getShell(false).add(command);
                commandWait(command);
                suVersionChecked = true;
            } catch (RootDeniedException e) {
                PPApplication.rootMutex.rootGranted = false;
                Log.e("PPApplication.getSUVersion", Log.getStackTraceString(e));
            } catch (Exception e) {
                Log.e("PPApplication.getSUVersion", Log.getStackTraceString(e));
            }
        }
        return suVersion;
    }
    
    */

    public static String getJavaCommandFile(Class<?> mainClass, String name, Context context, Object cmdParam) {
        try {
            String cmd =
                    "#!/system/bin/sh\n" +
                            "base=/system\n" +
                            "export CLASSPATH=" + context.getPackageManager().getPackageInfo(context.getPackageName(), 0).applicationInfo.sourceDir + "\n" +
                            "exec app_process $base/bin " + mainClass.getName() + " " + cmdParam + " \"$@\"\n";

            FileOutputStream fos = context.openFileOutput(name, Context.MODE_PRIVATE);
            fos.write(cmd.getBytes());
            fos.close();

            File file = context.getFileStreamPath(name);
            if (!file.setExecutable(true))
                return null;

            /*
            File sd = Environment.getExternalStorageDirectory();
            File exportDir = new File(sd, PPApplication.EXPORT_PATH);
            if (!(exportDir.exists() && exportDir.isDirectory()))
                exportDir.mkdirs();

            File outFile = new File(sd, PPApplication.EXPORT_PATH + "/" + name);
            OutputStream out = new FileOutputStream(outFile);
            out.write(cmd.getBytes());
            out.close();

            outFile.setExecutable(true);
            */

            return file.getAbsolutePath();

        } catch (Exception e) {
            return null;
        }
    }

    static void getServicesList() {
        synchronized (PPApplication.serviceListMutex) {
            if (serviceListMutex.serviceList == null)
                serviceListMutex.serviceList = new ArrayList<>();
            else
                serviceListMutex.serviceList.clear();

            synchronized (PPApplication.rootMutex) {
                //noinspection RegExpRedundantEscape
                final Pattern compile = Pattern.compile("^[0-9]+\\s+([a-zA-Z0-9_\\-\\.]+): \\[(.*)\\]$");
                Command command = new Command(0, false, "service list") {
                    @Override
                    public void commandOutput(int id, String line) {
                        Matcher matcher = compile.matcher(line);
                        if (matcher.find()) {
                            //noinspection unchecked
                            serviceListMutex.serviceList.add(new Pair(matcher.group(1), matcher.group(2)));
                        }
                        super.commandOutput(id, line);
                    }
                };
                try {
                    RootTools.getShell(false).add(command);
                    commandWait(command);
                } catch (Exception e) {
                    Log.e("PPApplication.getServicesList", Log.getStackTraceString(e));
                }
            }
        }
    }

    static Object getServiceManager(String serviceType) {
        synchronized (PPApplication.serviceListMutex) {
            if (serviceListMutex.serviceList != null) {
                for (Pair pair : serviceListMutex.serviceList) {
                    if (serviceType.equals(pair.first)) {
                        return pair.second;
                    }
                }
            }
            return null;
        }
    }

    static int getTransactionCode(String serviceManager, String method) {
        int code = -1;
        try {
            for (Class declaredFields : Class.forName(serviceManager).getDeclaredClasses()) {
                Field[] declaredFields2 = declaredFields.getDeclaredFields();
                int length = declaredFields2.length;
                int iField = 0;
                while (iField < length) {
                    Field field = declaredFields2[iField];
                    String name = field.getName();
                    if (/*name == null ||*/ !name.equals("TRANSACTION_" + method)) {
                        iField++;
                    } else {
                        try {
                            field.setAccessible(true);
                            code = field.getInt(field);
                            break;
                        } catch (Exception e) {
                            Log.e("PPApplication.getTransactionCode", Log.getStackTraceString(e));
                        }
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            Log.e("PPApplication.getTransactionCode", Log.getStackTraceString(e));
        }
        return code;
    }

    static String getServiceCommand(String serviceType, int transactionCode, Object... params) {
        if (params.length > 0) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("service").append(" ").append("call").append(" ").append(serviceType).append(" ").append(transactionCode);
            for (Object param : params) {
                if (param != null) {
                    stringBuilder.append(" ");
                    if (param instanceof Integer) {
                        stringBuilder.append("i32").append(" ").append(param);
                    } else if (param instanceof String) {
                        stringBuilder.append("s16").append(" ").append("'").append(((String) param).replace("'", "'\\''")).append("'");
                    }
                }
            }
            return stringBuilder.toString();
        }
        else
            return null;
    }

    static void commandWait(Command cmd) {
        int waitTill = 50;
        int waitTillMultiplier = 2;
        int waitTillLimit = 3200; // 6350 milliseconds (3200 * 2 - 50)

        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (cmd) {
            while (!cmd.isFinished() && waitTill<=waitTillLimit) {
                try {
                    //if (!cmd.isFinished()) {
                        cmd.wait(waitTill);
                        waitTill *= waitTillMultiplier;
                    //}
                } catch (InterruptedException e) {
                    Log.e("PPApplication.commandWait", Log.getStackTraceString(e));
                }
            }
        }
        if (!cmd.isFinished()){
            Log.e("PPApplication.commandWait", "Could not finish root command in " + (waitTill/waitTillMultiplier));
        }
    }

    // Debug -----------------------------------------------------------------

    /*
    public static long startMeasuringRunTime()
    {
        return System.nanoTime();
    }

    public static void getMeasuredRunTime(long nanoTimeStart, String log)
    {
        long nanoTimeEnd = System.nanoTime();
        long measuredTime = (nanoTimeEnd - nanoTimeStart) / 1000000;

        Log.d(log, "MEASURED TIME=" + measuredTime);
    }
    */

    // others ------------------------------------------------------------------

    static boolean isScreenOn(PowerManager powerManager) {
        if (Build.VERSION.SDK_INT >= 20)
            return powerManager.isInteractive();
        else
            //noinspection deprecation
            return powerManager.isScreenOn();
    }

    public static void sleep(long ms) {
        /*long start = SystemClock.uptimeMillis();
        do {
            SystemClock.sleep(100);
        } while (SystemClock.uptimeMillis() - start < ms);*/
        //SystemClock.sleep(ms);
        try{ Thread.sleep(ms); }catch(InterruptedException ignored){ }
    }

    /*
    private static String getROMManufacturer() {
        String line;
        BufferedReader input = null;
        try {
            java.lang.Process p = Runtime.getRuntime().exec("getprop ro.product.brand");
            input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
            input.close();
        }
        catch (IOException ex) {
            Log.e("PPApplication.getROMManufacturer", "Unable to read sysprop ro.product.brand", ex);
            return null;
        }
        finally {
            if (input != null) {
                try {
                    input.close();
                }
                catch (IOException e) {
                    Log.e("PPApplication.getROMManufacturer", "Exception while closing InputStream", e);
                }
            }
        }
        return line;
    }
    */

    private static boolean isMIUI() {
        boolean miuiRom = false;
        try {
            Properties prop = new Properties();
            prop.load(new FileInputStream(new File(Environment.getRootDirectory(), "build.prop")));
            miuiRom = prop.getProperty("ro.miui.ui.version.code", null) != null
                    || prop.getProperty("ro.miui.ui.version.name", null) != null
                    || prop.getProperty("ro.miui.internal.storage", null) != null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return miuiRom ||
                Build.BRAND.equalsIgnoreCase("xiaomi") ||
                Build.MANUFACTURER.equalsIgnoreCase("xiaomi") ||
                Build.FINGERPRINT.toLowerCase().contains("xiaomi");
    }

    private static String getEmuiRomName() {
        try {
            String line;
            //BufferedReader input = null;
            try {
                /*java.lang.Process p = Runtime.getRuntime().exec("getprop " + propName);
                input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
                line = input.readLine();
                input.close();*/
                Properties prop = new Properties();
                prop.load(new FileInputStream(new File(Environment.getRootDirectory(), "build.prop")));
                line = prop.getProperty("ro.build.version.emui", null);
            } catch (IOException ex) {
                ex.printStackTrace();
                return null;
            }/* finally {
                if (input != null) {
                    try {
                        input.close();
                    } catch (IOException e) {
                        Log.e("PPApplication.getSystemProperty", "Exception while closing InputStream", e);
                    }
                }
            }*/
            return line;
        } catch (Exception e) {
            return "";
        }
    }

    private static boolean isEMUI() {
        String emuiRomName = getEmuiRomName();
        PPApplication.logE("PPApplication.isEMUI", "emuiRomName="+emuiRomName);
        String romName = "";
        if (emuiRomName != null)
            romName = emuiRomName.toLowerCase();

        return (romName.indexOf("emotionui_") == 0) ||
                Build.DISPLAY.toLowerCase().contains("emui2.3") || "EMUI 2.3".equalsIgnoreCase(emuiRomName) ||
                Build.BRAND.equalsIgnoreCase("huawei") ||
                Build.MANUFACTURER.equalsIgnoreCase("huawei") ||
                Build.FINGERPRINT.toLowerCase().contains("huawei");
    }

    private static boolean isSamsung() {
        return Build.BRAND.equalsIgnoreCase("samsung") ||
                Build.MANUFACTURER.equalsIgnoreCase("samsung") ||
                Build.FINGERPRINT.toLowerCase().contains("samsung");
    }

    static boolean hasSystemFeature(Context context, String feature) {
        try {
            PackageManager packageManager = context.getPackageManager();
            return packageManager.hasSystemFeature(feature);
        } catch (Exception e) {
            return false;
        }
    }

    public static void exitApp(final Context context, /*DataWrapper dataWrapper,*/ final Activity activity,
                               boolean shutdown/*, boolean killProcess*/) {
        try {
            // remove alarm for profile duration
            ProfileDurationAlarmBroadcastReceiver.removeAlarm(context);
            Profile.setActivatedProfileForDuration(context, 0);

            LockDeviceActivityFinishBroadcastReceiver.removeAlarm(context);

            ImportantInfoNotification.removeNotification(context);
            Permissions.removeNotifications(context);

            if (!shutdown) {
                if (PPApplication.brightnessHandler != null) {
                    PPApplication.brightnessHandler.post(new Runnable() {
                        public void run() {
                            ActivateProfileHelper.removeBrightnessView(context);

                        }
                    });
                }
                if (PPApplication.screenTimeoutHandler != null) {
                    PPApplication.screenTimeoutHandler.post(new Runnable() {
                        public void run() {
                            ActivateProfileHelper.removeScreenTimeoutAlwaysOnView(context);
                            ActivateProfileHelper.removeBrightnessView(context);

                        }
                    });
                }

                //PPApplication.initRoot();
            }

            Permissions.setAllShowRequestPermissions(context.getApplicationContext(), true);

            //ActivateProfileHelper.setScreenUnlocked(context.getApplicationContext(), true);

            PhoneProfilesService.stop(context.getApplicationContext());

            PPApplication.setApplicationStarted(context, false);

            ActivateProfileHelper.updateGUI(context, false);

            if (!shutdown) {
                Handler _handler = new Handler(context.getMainLooper());
                Runnable r = new Runnable() {
                    public void run() {
                        if (activity != null) {
                            try {
                                activity.finish();
                            } catch (Exception ignored) {}
                        }
                    }
                };
                _handler.post(r);
                /*if (killProcess) {
                    Handler _handler = new Handler(context.getMainLooper());
                    Runnable r = new Runnable() {
                        public void run() {
                            android.os.Process.killProcess(android.os.Process.myPid());
                        }
                    };
                    _handler.postDelayed(r, 1000);
                }*/
            }
        } catch (Exception e) {
            PPApplication.logE("PPApplication._exitApp", Log.getStackTraceString(e));
        }
    }

    static void startHandlerThread() {
        if (handlerThread == null) {
            handlerThread = new HandlerThread("PPHandlerThread");
            handlerThread.start();
        }
    }

    static void startHandlerThreadInternalChangeToFalse() {
        if (handlerThreadInternalChangeToFalse == null) {
            handlerThreadInternalChangeToFalse = new HandlerThread("PPHandlerThreadInternalChangeToFalse");
            handlerThreadInternalChangeToFalse.start();
        }
    }

    static void startHandlerThreadWidget() {
        if (handlerThreadWidget == null) {
            handlerThreadWidget = new HandlerThread("PPHandlerThreadWidget");
            handlerThreadWidget.start();
        }
    }

    static void startHandlerThreadProfileNotification() {
        if (handlerThreadProfileNotification == null) {
            handlerThreadProfileNotification = new HandlerThread("PPHandlerThreadProfileNotification");
            handlerThreadProfileNotification.start();
        }
    }

    static void startHandlerThreadPlayTone() {
        if (handlerThreadPlayTone == null) {
            handlerThreadPlayTone = new HandlerThread("PPHandlerThreadPlayTone");
            handlerThreadPlayTone.start();
        }
    }

    static void startHandlerThreadVolumes() {
        if (handlerThreadVolumes == null) {
            handlerThreadVolumes = new HandlerThread("handlerThreadVolumes");
            handlerThreadVolumes.start();
        }
    }

    static void startHandlerThreadRadios() {
        if (handlerThreadRadios == null) {
            handlerThreadRadios = new HandlerThread("handlerThreadRadios");
            handlerThreadRadios.start();
        }
    }

    static void startHandlerThreadAdaptiveBrightness() {
        if (handlerThreadAdaptiveBrightness == null) {
            handlerThreadAdaptiveBrightness = new HandlerThread("handlerThreadAdaptiveBrightness");
            handlerThreadAdaptiveBrightness.start();
        }
    }

    static void startHandlerThreadWallpaper() {
        if (handlerThreadWallpaper == null) {
            handlerThreadWallpaper = new HandlerThread("handlerThreadWallpaper");
            handlerThreadWallpaper.start();
        }
    }

    static void startHandlerThreadPowerSaveMode() {
        if (handlerThreadPowerSaveMode == null) {
            handlerThreadPowerSaveMode = new HandlerThread("handlerThreadPowerSaveMode");
            handlerThreadPowerSaveMode.start();
        }
    }

    static void startHandlerThreadLockDevice() {
        if (handlerThreadLockDevice == null) {
            handlerThreadLockDevice = new HandlerThread("handlerThreadLockDevice");
            handlerThreadLockDevice.start();
        }
    }

    static void startHandlerThreadRunApplication() {
        if (handlerThreadRunApplication == null) {
            handlerThreadRunApplication = new HandlerThread("handlerThreadRunApplication");
            handlerThreadRunApplication.start();
        }
    }

    static void startHandlerThreadHeadsUpNotifications() {
        if (handlerThreadHeadsUpNotifications == null) {
            handlerThreadHeadsUpNotifications = new HandlerThread("handlerThreadHeadsUpNotifications");
            handlerThreadHeadsUpNotifications.start();
        }
    }

    static void startHandlerThreadNotificationLed() {
        if (handlerThreadNotificationLed == null) {
            handlerThreadNotificationLed = new HandlerThread("handlerThreadNotificationLed");
            handlerThreadNotificationLed.start();
        }
    }

    // Google Analytics ----------------------------------------------------------------------------

    /*
    static void logAnalyticsEvent(String itemId, String itemName, String contentType) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, itemId);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, itemName);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, contentType);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }
    */

    //---------------------------------------------------------------------------------------------

}
