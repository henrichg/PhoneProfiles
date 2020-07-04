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
import android.os.Handler;
import android.os.HandlerThread;
import android.os.PowerManager;
import android.util.Log;
import android.util.Pair;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.look.Slook;
import com.stericson.RootShell.RootShell;
import com.stericson.RootShell.execution.Command;
import com.stericson.RootTools.RootTools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.core.content.pm.PackageInfoCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.multidex.MultiDex;

//import com.google.firebase.analytics.FirebaseAnalytics;

//import com.github.anrwatchdog.ANRError;
//import com.github.anrwatchdog.ANRWatchDog;

@SuppressWarnings("WeakerAccess")
public class PPApplication extends Application {

    private static PPApplication instance;

    //static final String romManufacturer = getROMManufacturer();
    static final boolean deviceIsXiaomi = isXiaomi();
    static final boolean deviceIsHuawei = isHuawei();
    static final boolean deviceIsSamsung = isSamsung();
    static final boolean deviceIsLG = isLG();
    static final boolean deviceIsOnePlus = isOnePlus();
    static final boolean romIsMIUI = isMIUIROM();
    static final boolean romIsEMUI = isEMUIROM();

    static final String PACKAGE_NAME = "sk.henrichg.phoneprofiles";
    static final String PACKAGE_NAME_EXTENDER = "sk.henrichg.phoneprofilesplusextender";

    //static final int VERSION_CODE_EXTENDER_1_0_4 = 60;
    //static final int VERSION_CODE_EXTENDER_2_0 = 100;
    static final int VERSION_CODE_EXTENDER_3_0 = 200;
    static final int VERSION_CODE_EXTENDER_4_0 = 400;
    //static final int VERSION_CODE_EXTENDER_5_1_2 = 465;
    static final int VERSION_CODE_EXTENDER_5_1_3_1 = 540;
    static final int VERSION_CODE_EXTENDER_LATEST = VERSION_CODE_EXTENDER_5_1_3_1;

    public static final String EXPORT_PATH = "/PhoneProfiles";
    static final String LOG_FILENAME = "log.txt";

    @SuppressWarnings("PointlessBooleanExpression")
    private static final boolean logIntoLogCat = true && BuildConfig.DEBUG;
    private static final boolean logIntoFile = false;
    @SuppressWarnings("PointlessBooleanExpression")
    static final boolean crashIntoFile = true && BuildConfig.DEBUG;
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
                                            +"|DataWrapper.activateProfileOnBoot"

                                            //+"|[***] ActivateProfileHelper"
                                            //+"|PPPExtenderBroadcastReceiver"

                                            // for list of TRANSACTION_* for "phone" service
                                            //+"|[LIST] PPApplication.getTransactionCode"

                                            //+"|ProfileDurationAlarmBroadcastReceiver"

                                            //+"|ActivateProfilesHelper.executeForForceStopApplications"
                                            //+"|DataWrapper.setDynamicLauncherShortcuts"

                                            //+"|ActivateProfileHelper.execute"
                                            //+"|ActivateProfileHelper.setScreenTimeout"
                                            //+"|ActivateProfileHelper.screenTimeoutLock"
                                            //+"|@@@ ScreenOnOffBroadcastReceiver.onReceive"
                                            //+"|Profile.isProfilePreferenceAllowed"
                                            //+"|PPApplication.hasSystemFeature"

                                            //+"|InfoDialogPreferenceFragmentX"
                                            //+"|ImportantInfoActivity"

                                            //+"|[BRS] SettingsContentObserver.onChange"
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

    //static final int PREFERENCES_STARTUP_SOURCE_ACTIVITY = 1;
    //static final int PREFERENCES_STARTUP_SOURCE_FRAGMENT = 2;
    //static final int PREFERENCES_STARTUP_SOURCE_SHARED_PROFILE = 3;

    static final String PROFILE_NOTIFICATION_CHANNEL = "phoneProfiles_activated_profile";
    static final String INFORMATION_NOTIFICATION_CHANNEL = "phoneProfiles_information";
    static final String EXCLAMATION_NOTIFICATION_CHANNEL = "phoneProfiles_exclamation";
    static final String GRANT_PERMISSION_NOTIFICATION_CHANNEL = "phoneProfiles_grant_permission";
    static final String DONATION_CHANNEL = "phoneProfiles_donation";
    static final String EXPORT_PP_DATA_CHANNEL = "phoneProfiles_export_pp_data";

    static final int PROFILE_NOTIFICATION_ID = 700420;
    static final int IMPORTANT_INFO_NOTIFICATION_ID = 700422;
    static final int GRANT_PROFILE_PERMISSIONS_NOTIFICATION_ID = 700423;
    //static final int GRANT_INSTALL_TONE_PERMISSIONS_NOTIFICATION_ID = 700424;
    static final int ACTION_FOR_EXTERNAL_APPLICATION_NOTIFICATION_ID = 700425;
    static final int PROFILE_ACTIVATION_MOBILE_DATA_PREFS_NOTIFICATION_ID = 700426;
    static final int PROFILE_ACTIVATION_LOCATION_PREFS_NOTIFICATION_ID = 700427;
    static final int PROFILE_ACTIVATION_WIFI_AP_PREFS_NOTIFICATION_ID = 700428;
    static final int ABOUT_APPLICATION_DONATE_NOTIFICATION_ID = 700429;
    static final int PROFILE_ACTIVATION_NETWORK_TYPE_PREFS_NOTIFICATION_ID = 700430;
    static final int GRANT_PLAY_RINGTONE_NOTIFICATION_PERMISSIONS_NOTIFICATION_ID = 700431;
    static final int GRANT_LOG_TO_FILE_PERMISSIONS_NOTIFICATION_ID = 700432;
    static final int EXPORT_PP_DATA_NOTIFICATION_ID = 700433;

    static final String APPLICATION_PREFS_NAME = "phone_profile_preferences";
    //static final String SHARED_PROFILE_PREFS_NAME = "profile_preferences_default_profile";
    //static final String PERMISSIONS_PREFS_NAME = "permissions_list";
    static final String PERMISSIONS_STATUS_PREFS_NAME = "permissions_status";
    static final String WIFI_CONFIGURATION_LIST_PREFS_NAME = "wifi_configuration_list";

    private static final String PREF_APPLICATION_STARTED = "applicationStarted";
    private static final String PREF_SAVED_VERSION_CODE = "saved_version_code";
    private static final String PREF_DAYS_AFTER_FIRST_START = "days_after_first_start";
    private static final String PREF_DONATION_NOTIFICATION_COUNT = "donation_notification_count";
    private static final String PREF_DONATION_DONATED = "donation_donated";
    private static final String PREF_DAYS_FOR_NEXT_DONATION_NOTIFICATION = "days_for_next_donation_notification";

    static final String EXTENDER_ACCESSIBILITY_SERVICE_ID = "sk.henrichg.phoneprofilesplusextender/.PPPEAccessibilityService";

    static final String ACTION_ACCESSIBILITY_SERVICE_CONNECTED = PPApplication.PACKAGE_NAME_EXTENDER + ".ACTION_ACCESSIBILITY_SERVICE_CONNECTED";
    static final String ACTION_ACCESSIBILITY_SERVICE_UNBIND = PPApplication.PACKAGE_NAME_EXTENDER + ".ACTION_ACCESSIBILITY_SERVICE_UNBIND";
    static final String ACTION_REGISTER_PPPE_FUNCTION = PPApplication.PACKAGE_NAME_EXTENDER + ".ACTION_REGISTER_PPPE_FUNCTION";
    static final String ACTION_FORCE_STOP_APPLICATIONS_START = PPApplication.PACKAGE_NAME_EXTENDER + ".ACTION_FORCE_STOP_APPLICATIONS_START";
    static final String ACTION_FORCE_STOP_APPLICATIONS_END = PPApplication.PACKAGE_NAME_EXTENDER + ".ACTION_FORCE_STOP_APPLICATIONS_END";
    static final String ACTION_LOCK_DEVICE = PPApplication.PACKAGE_NAME_EXTENDER + ".ACTION_LOCK_DEVICE";
    static final String ACCESSIBILITY_SERVICE_PERMISSION = PPApplication.PACKAGE_NAME_EXTENDER + ".ACCESSIBILITY_SERVICE_PERMISSION";
    static final String ACTION_DONATION = PPApplication.PACKAGE_NAME_EXTENDER + ".ACTION_DONATION";

    static final String EXTRA_REGISTRATION_APP = "registration_app";
    static final String EXTRA_REGISTRATION_TYPE = "registration_type";
    static final int REGISTRATION_TYPE_FORCE_STOP_APPLICATIONS_REGISTER = 1;
    static final int REGISTRATION_TYPE_FORCE_STOP_APPLICATIONS_UNREGISTER = -1;
    static final int REGISTRATION_TYPE_LOCK_DEVICE_REGISTER = 5;
    static final int REGISTRATION_TYPE_LOCK_DEVICE_UNREGISTER = -5;

    static final String EXTRA_APPLICATIONS = "extra_applications";

    //@SuppressWarnings("SpellCheckingInspection")
    //static private FirebaseAnalytics mFirebaseAnalytics;

    public static boolean isScreenOn;

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

    //public static final RootMutex rootMutex = new RootMutex();

    // !! this must be here
    public static boolean blockProfileEventActions = false;

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

        instance = this;

        PPApplication.logE("##### PPApplication.onCreate", "romManufacturer="+Build.MANUFACTURER);

        PPApplication.logE("##### PPApplication.onCreate", "deviceIsXiaomi="+deviceIsXiaomi);
        PPApplication.logE("##### PPApplication.onCreate", "deviceIsHuawei="+deviceIsHuawei);
        PPApplication.logE("##### PPApplication.onCreate", "deviceIsSamsung="+deviceIsSamsung);
        PPApplication.logE("##### PPApplication.onCreate", "deviceIsLG="+deviceIsLG);
        PPApplication.logE("##### PPApplication.onCreate", "deviceIsOnePlus="+deviceIsOnePlus);

        PPApplication.logE("##### PPApplication.onCreate", "romIsMIUI="+romIsMIUI);
        PPApplication.logE("##### PPApplication.onCreate", "romIsEMUI="+romIsEMUI);

        PPApplication.logE("##### PPApplication.onCreate", "model="+Build.MODEL);

        if (checkAppReplacingState())
            return;

        ///////////////////////////////////////////
        // Bypass Android's hidden API restrictions
        // https://github.com/tiann/FreeReflection
        if (Build.VERSION.SDK_INT >= 28) {
            try {
                Method forName = Class.class.getDeclaredMethod("forName", String.class);
                Method getDeclaredMethod = Class.class.getDeclaredMethod("getDeclaredMethod", String.class, Class[].class);

                Class<?> vmRuntimeClass = (Class<?>) forName.invoke(null, "dalvik.system.VMRuntime");
                Method getRuntime = (Method) getDeclaredMethod.invoke(vmRuntimeClass, "getRuntime", null);
                Method setHiddenApiExemptions = (Method) getDeclaredMethod.invoke(vmRuntimeClass, "setHiddenApiExemptions", new Class[]{String[].class});

                if (getRuntime != null) {
                    Object vmRuntime = getRuntime.invoke(null);
                    if (setHiddenApiExemptions != null)
                        setHiddenApiExemptions.invoke(vmRuntime, new Object[]{new String[]{"L"}});
                }
            } catch (Exception e) {
                Log.e("PPApplication.onCreate", Log.getStackTraceString(e));
            }
        }
        //////////////////////////////////////////

        // Fix for FC: java.lang.IllegalArgumentException: register too many Broadcast Receivers
        //LoadedApkHuaWei.hookHuaWeiVerifier(this);

        if (logIntoFile || crashIntoFile)
            Permissions.grantLogToFilePermissions(getApplicationContext());

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
            PPApplication.setCustomKey("DEBUG", BuildConfig.DEBUG);
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

        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        if (pm != null)
            isScreenOn = pm.isInteractive();
        else
            isScreenOn = false;

        //	Debug.startMethodTracing("phoneprofiles");

        //long nanoTimeStart = startMeasuringRunTime();

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(PPApplication.refreshActivitiesBroadcastReceiver,
                new IntentFilter(PPApplication.PACKAGE_NAME + ".RefreshActivitiesBroadcastReceiver"));
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(PPApplication.dashClockBroadcastReceiver,
                new IntentFilter(PPApplication.PACKAGE_NAME + ".DashClockBroadcastReceiver"));

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
        //return pInfo.versionCode;
        return (int) PackageInfoCompat.getLongVersionCode(pInfo);
    }

    //--------------------------------------------------------------

    static void startPPService(Context context, Intent serviceIntent) {
        if (Build.VERSION.SDK_INT < 26)
            context.getApplicationContext().startService(serviceIntent);
        else
            context.getApplicationContext().startForegroundService(serviceIntent);
    }

    static void runCommand(Context context, Intent intent) {
        //PPApplication.logE("PPApplication.runCommand", "xxx");
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
        /*File sd = Environment.getExternalStorageDirectory();
        File exportDir = new File(sd, PPApplication.EXPORT_PATH);
        if (!(exportDir.exists() && exportDir.isDirectory()))
            //noinspection ResultOfMethodCallIgnored
            exportDir.mkdirs();

        File logFile = new File(sd, EXPORT_PATH + "/" + LOG_FILENAME);*/

        File path = instance.getApplicationContext().getExternalFilesDir(null);
        File logFile = new File(path, LOG_FILENAME);

        //noinspection ResultOfMethodCallIgnored
        logFile.delete();
    }

    static private void logIntoFile(String type, String tag, String text)
    {
        if (!logIntoFile)
            return;

        if (instance == null)
            return;

        try {
            File path = instance.getApplicationContext().getExternalFilesDir(null);
            //Log.e("PPApplication.logIntoFile", "----- path=" + path.getAbsolutePath());

            /*File sd = Environment.getExternalStorageDirectory();
            File exportDir = new File(sd, PPApplication.EXPORT_PATH);
            if (!(exportDir.exists() && exportDir.isDirectory()))
                //noinspection ResultOfMethodCallIgnored
                exportDir.mkdirs();

            File logFile = new File(sd, EXPORT_PATH + "/" + LOG_FILENAME);
            */

            File logFile = new File(path, LOG_FILENAME);

            if (logFile.length() > 1024 * 10000)
                resetLog();

            if (!logFile.exists()) {
                //noinspection ResultOfMethodCallIgnored
                logFile.createNewFile();
            }

            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            String log = "";
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat sdf = new SimpleDateFormat("d.MM.yy HH:mm:ss:S");
            String time = sdf.format(Calendar.getInstance().getTimeInMillis());
            log = log + time + "--" + type + "-----" + tag + "------" + text;
            buf.append(log);
            buf.newLine();
            buf.flush();
            buf.close();
        } catch (IOException e) {
            Log.e("PPApplication.logIntoFile", Log.getStackTraceString(e));
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

    static public boolean logEnabled() {
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

    static public int getDaysForNextDonationNotification(Context context)
    {
        ApplicationPreferences.getSharedPreferences(context);
        return ApplicationPreferences.preferences.getInt(PREF_DAYS_FOR_NEXT_DONATION_NOTIFICATION, 0);
    }

    static public void setDaysForNextDonationNotification(Context context, int days)
    {
        ApplicationPreferences.getSharedPreferences(context);
        Editor editor = ApplicationPreferences.preferences.edit();
        editor.putInt(PREF_DAYS_FOR_NEXT_DONATION_NOTIFICATION, days);
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
            //if (ApplicationPreferences.notificationShowInStatusBar(context)) {
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
            //        importance = NotificationManager.IMPORTANCE_DEFAULT;
            //} else
            //    importance = NotificationManager.IMPORTANCE_MIN;
            importance = NotificationManager.IMPORTANCE_LOW;

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

    static void createDonationNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= 26) {
            // The user-visible name of the channel.
            CharSequence name = context.getString(R.string.notification_channel_donation);
            // The user-visible description of the channel.
            String description = context.getString(R.string.empty_string);

            NotificationChannel channel = new NotificationChannel(DONATION_CHANNEL, name, NotificationManager.IMPORTANCE_LOW);

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

    static void createExportPPDataNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= 26) {
            // The user-visible name of the channel.
            CharSequence name = context.getString(R.string.notification_channel_exportPPData);
            // The user-visible description of the channel.
            String description = context.getString(R.string.notification_channel_export_pp_data_description);

            NotificationChannel channel = new NotificationChannel(EXPORT_PP_DATA_CHANNEL, name, NotificationManager.IMPORTANCE_LOW);

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
        PPApplication.createDonationNotificationChannel(appContext);
        PPApplication.createExportPPDataNotificationChannel(appContext);
    }

    static void showProfileNotification(/*Context context*/) {
        try {
            //PPApplication.logE("PPApplication.showProfileNotification", "xxx");
            /*Intent serviceIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_SHOW_PROFILE_NOTIFICATION, true);
            PPApplication.startPPService(context, serviceIntent);*/
            if (PhoneProfilesService.getInstance() != null)
                PhoneProfilesService.getInstance().showProfileNotification(false);
        } catch (Exception ignored) {}
    }

    // -----------------------------------------------

    // root -----------------------------------------------------

    static synchronized void initRoot() {
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
            //PPApplication.logE("PPApplication._isRooted", "start isRootAvailable");
            //if (RootTools.isRootAvailable()) {
            //noinspection RedundantIfStatement
            if (RootToolsSmall.isRooted()) {
                // device is rooted
                //PPApplication.logE("PPApplication._isRooted", "root available");
                rootMutex.rooted = true;
            } else {
                //PPApplication.logE("PPApplication._isRooted", "root NOT available");
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
                    //PPApplication.logE("PPApplication.isRootGranted", "start isAccessGiven");
                    //noinspection StatementWithEmptyBody
                    if (RootTools.isAccessGiven()) {
                        // root is granted
                        //PPApplication.logE("PPApplication.isRootGranted", "root granted");
                        //rootMutex.rootGranted = true;
                        //rootMutex.grantRootChecked = true;
                    } else {
                        // grant denied
                        //PPApplication.logE("PPApplication.isRootGranted", "root NOT granted");
                        //rootMutex.rootGranted = false;
                        //rootMutex.grantRootChecked = true;
                    }
                } catch (Exception e) {
                    Log.e("PPApplication.isRootGranted", Log.getStackTraceString(e));
                    //rootMutex.rootGranted = false;
                }
                //return rootMutex.rootGranted;
            }
        } /*else {
            PPApplication.logE("PPApplication.isRootGranted", "not rooted");
        }*/
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
                //PPApplication.logE("PPApplication.settingsBinaryExists", "start");
                rootMutex.settingsBinaryExists = RootToolsSmall.hasSettingBin();
                rootMutex.settingsBinaryChecked = true;
            }
            //PPApplication.logE("PPApplication.settingsBinaryExists", "settingsBinaryExists=" + rootMutex.settingsBinaryExists);
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
                //PPApplication.logE("PPApplication.serviceBinaryExists", "start");
                rootMutex.serviceBinaryExists = RootToolsSmall.hasServiceBin();
                rootMutex.serviceBinaryChecked = true;
            }
            //PPApplication.logE("PPApplication.serviceBinaryExists", "serviceBinaryExists=" + rootMutex.serviceBinaryExists);
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
        }

        try
        {
            //noinspection RegExpRedundantEscape
            Pattern compile = Pattern.compile("^[0-9]+\\s+([a-zA-Z0-9_\\-\\.]+): \\[(.*)\\]$");
            Process p=Runtime.getRuntime().exec("service list");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                Matcher matcher = compile.matcher(line);
                if (matcher.find()) {
                    synchronized (PPApplication.serviceListMutex) {
                        //serviceListMutex.serviceList.add(new Pair(matcher.group(1), matcher.group(2)));
                        serviceListMutex.serviceList.add(Pair.create(matcher.group(1), matcher.group(2)));
                        //PPApplication.logE("$$$ WifiAP", "PhoneProfilesService.getServicesList - matcher.group(1)="+matcher.group(1));
                        //PPApplication.logE("$$$ WifiAP", "PhoneProfilesService.getServicesList - matcher.group(2)="+matcher.group(2));
                    }
                }
            }
        }
        catch (Exception e) {
            Log.e("PPApplication.getServicesList", Log.getStackTraceString(e));
        }

        /*
        synchronized (PPApplication.rootMutex) {
            //noinspection RegExpRedundantEscape
            final Pattern compile = Pattern.compile("^[0-9]+\\s+([a-zA-Z0-9_\\-\\.]+): \\[(.*)\\]$");
            Command command = new Command(0, false, "service list") {
                @Override
                public void commandOutput(int id, String line) {
                    //PPApplication.logE("$$$ WifiAP", "PhoneProfilesService.getServicesList - line="+line);
                    Matcher matcher = compile.matcher(line);
                    if (matcher.find()) {
                        synchronized (PPApplication.serviceListMutex) {
                            //serviceListMutex.serviceList.add(new Pair(matcher.group(1), matcher.group(2)));
                            serviceListMutex.serviceList.add(Pair.create(matcher.group(1), matcher.group(2)));
                            //PPApplication.logE("$$$ WifiAP", "PhoneProfilesService.getServicesList - matcher.group(1)="+matcher.group(1));
                            //PPApplication.logE("$$$ WifiAP", "PhoneProfilesService.getServicesList - matcher.group(2)="+matcher.group(2));
                        }
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
        */
    }

    static Object getServiceManager(String serviceType) {
        synchronized (PPApplication.serviceListMutex) {
            if (serviceListMutex.serviceList != null) {
                //noinspection rawtypes
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
            //noinspection rawtypes
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

    /*
    static boolean isScreenOn(PowerManager powerManager) {
        //if (Build.VERSION.SDK_INT >= 20)
            return powerManager.isInteractive();
        //else
        //    return powerManager.isScreenOn();
    }
    */

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

    private static boolean isXiaomi() {
        return Build.BRAND.equalsIgnoreCase("xiaomi") ||
                Build.MANUFACTURER.equalsIgnoreCase("xiaomi") ||
                Build.FINGERPRINT.toLowerCase().contains("xiaomi");
    }

    private static boolean isMIUIROM() {
        boolean miuiRom1 = false;
        boolean miuiRom2 = false;
        boolean miuiRom3 = false;

        String line;
        BufferedReader input;
        try {
            java.lang.Process p = Runtime.getRuntime().exec("getprop ro.miui.ui.version.code");
            input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
            miuiRom1 = line.length() != 0;
            input.close();

            if (!miuiRom1) {
                p = Runtime.getRuntime().exec("getprop ro.miui.ui.version.name");
                input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
                line = input.readLine();
                miuiRom2 = line.length() != 0;
                input.close();
            }

            if (!miuiRom1 && !miuiRom2) {
                p = Runtime.getRuntime().exec("getprop ro.miui.internal.storage");
                input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
                line = input.readLine();
                miuiRom3 = line.length() != 0;
                input.close();
            }

        } catch (IOException ex) {
            //PPApplication.logE("PPApplication.isMIUIROM", Log.getStackTraceString(ex));
        }

        //PPApplication.logE("PPApplication.isMIUIROM", "miuiRom1="+miuiRom1);
        //PPApplication.logE("PPApplication.isMIUIROM", "miuiRom2="+miuiRom2);
        //PPApplication.logE("PPApplication.isMIUIROM", "miuiRom3="+miuiRom3);

        return miuiRom1 || miuiRom2 || miuiRom3;
    }

    private static String getEmuiRomName() {
        String line;
        BufferedReader input;
        try {
            java.lang.Process p = Runtime.getRuntime().exec("getprop ro.build.version.emui");
            input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
            input.close();
            return line;
        } catch (IOException ex) {
            //PPApplication.logE("PPApplication.getEmuiRomName", Log.getStackTraceString(ex));
            return "";
        }
    }

    private static boolean isHuawei() {
        return Build.BRAND.equalsIgnoreCase("huawei") ||
                Build.MANUFACTURER.equalsIgnoreCase("huawei") ||
                Build.FINGERPRINT.toLowerCase().contains("huawei");
    }

    private static boolean isEMUIROM() {
        String emuiRomName = getEmuiRomName();
        //PPApplication.logE("PPApplication.isEMUIROM", "emuiRomName="+emuiRomName);

        return (emuiRomName.length() != 0) ||
                Build.DISPLAY.toLowerCase().contains("emui2.3");// || "EMUI 2.3".equalsIgnoreCase(emuiRomName);
    }

    private static boolean isSamsung() {
        return Build.BRAND.equalsIgnoreCase("samsung") ||
                Build.MANUFACTURER.equalsIgnoreCase("samsung") ||
                Build.FINGERPRINT.toLowerCase().contains("samsung");
    }

    private static boolean isLG() {
        //PPApplication.logE("PPApplication.isLG", "brand="+Build.BRAND);
        //PPApplication.logE("PPApplication.isLG", "manufacturer="+Build.MANUFACTURER);
        //PPApplication.logE("PPApplication.isLG", "fingerprint="+Build.FINGERPRINT);
        return Build.BRAND.equalsIgnoreCase("lge") ||
                Build.MANUFACTURER.equalsIgnoreCase("lge") ||
                Build.FINGERPRINT.toLowerCase().contains("lge");
    }

    private static boolean isOnePlus() {
        //PPApplication.logE("PPApplication.isOnePlus", "brand="+Build.BRAND);
        //PPApplication.logE("PPApplication.isOnePlus", "manufacturer="+Build.MANUFACTURER);
        //PPApplication.logE("PPApplication.isOnePlus", "fingerprint="+Build.FINGERPRINT);
        return Build.BRAND.equalsIgnoreCase("oneplus") ||
                Build.MANUFACTURER.equalsIgnoreCase("oneplus") ||
                Build.FINGERPRINT.toLowerCase().contains("oneplus");
    }

    static boolean hasSystemFeature(Context context, String feature) {
        try {
            PackageManager packageManager = context.getPackageManager();
            return packageManager.hasSystemFeature(feature);
        } catch (Exception e) {
            //PPApplication.logE("PPApplication.hasSystemFeature", Log.getStackTraceString(e));
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
            //PPApplication.logE("PPApplication._exitApp", Log.getStackTraceString(e));
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

    static void setBlockProfileEventActions(boolean enable) {
        // if blockProfileEventActions = true, do not perform any actions, for example ActivateProfileHelper.lockDevice()
        PPApplication.blockProfileEventActions = enable;
        if (enable) {
            PPApplication.startHandlerThread();
            final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //PPApplication.logE("PPApplication.startHandlerThread", "START run - from=PPApplication.setBlockProfileEventActions");

                    //PPApplication.logE("PPApplication.setBlockProfileEventActions", "delayed boot up");
                    PPApplication.blockProfileEventActions = false;

                    //PPApplication.logE("PPApplication.startHandlerThread", "END run - from=PPApplication.setBlockProfileEventActions");
                }
            }, 30000);
        }
    }

    // Firebase Crashlytics -------------------------------------------------------------------------

    @SuppressWarnings("unused")
    static void recordException(Throwable ex) {
        try {
            FirebaseCrashlytics.getInstance().recordException(ex);
        } catch (Exception ignored) {}
    }

    @SuppressWarnings("unused")
    static void logToCrashlytics(String s) {
        try {
            FirebaseCrashlytics.getInstance().log(s);
        } catch (Exception ignored) {}
    }

    @SuppressWarnings({"SameParameterValue", "unused"})
    static void setCustomKey(String key, int value) {
        try {
            FirebaseCrashlytics.getInstance().setCustomKey(key, value);
        } catch (Exception ignored) {}
    }

    @SuppressWarnings({"SameParameterValue", "unused"})
    static void setCustomKey(String key, String value) {
        try {
            FirebaseCrashlytics.getInstance().setCustomKey(key, value);
        } catch (Exception ignored) {}
    }

    @SuppressWarnings("SameParameterValue")
    static void setCustomKey(String key, boolean value) {
        try {
            FirebaseCrashlytics.getInstance().setCustomKey(key, value);
        } catch (Exception ignored) {}
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
