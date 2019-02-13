package sk.henrichg.phoneprofiles;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;

public class PackageReplacedReceiver extends BroadcastReceiver {

    static private boolean restartService;

    @Override
    public void onReceive(Context context, Intent intent) {
        if ((intent != null) && (intent.getAction() != null) && intent.getAction().equals(Intent.ACTION_MY_PACKAGE_REPLACED)) {
            PPApplication.logE("PackageReplacedReceiver.onReceive", "xxx");

            restartService = false;

            // start delayed boot up broadcast
            PPApplication.startedOnBoot = true;
            PPApplication.startHandlerThread();
            final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    PPApplication.logE("PackageReplacedReceiver.onReceive", "delayed boot up");
                    PPApplication.startedOnBoot = false;
                }
            }, 30001);

            final Context appContext = context.getApplicationContext();

            final int oldVersionCode = PPApplication.getSavedVersionCode(appContext);
            // save version code
            try {
                PackageInfo pInfo = appContext.getPackageManager().getPackageInfo(appContext.getPackageName(), 0);
                int actualVersionCode = PPApplication.getVersionCode(pInfo);
                PPApplication.setSavedVersionCode(appContext, actualVersionCode);
            } catch (Exception ignored) {
            }

            PPApplication.startHandlerThread();
            final Handler handler2 = new Handler(PPApplication.handlerThread.getLooper());
            handler2.post(new Runnable() {
                @Override
                public void run() {
                    PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":PackageReplacedReceiver.onReceive.2");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        Permissions.setAllShowRequestPermissions(appContext, true);

                        //ActivateProfileHelper.setScreenUnlocked(appContext, true);

                        PPApplication.logE("PackageReplacedReceiver.onReceive", "oldVersionCode=" + oldVersionCode);
                        int actualVersionCode;
                        try {
                            PackageInfo pInfo = appContext.getPackageManager().getPackageInfo(appContext.getPackageName(), 0);
                            actualVersionCode = PPApplication.getVersionCode(pInfo);
                            PPApplication.logE("PackageReplacedReceiver.onReceive", "actualVersionCode=" + actualVersionCode);

                            if (oldVersionCode < actualVersionCode) {
                                if (actualVersionCode <= 2100) {
                                    ApplicationPreferences.getSharedPreferences(appContext);
                                    SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
                                    editor.putBoolean(ActivateProfileActivity.PREF_START_TARGET_HELPS, false);
                                    editor.putBoolean(ActivateProfileListFragment.PREF_START_TARGET_HELPS, false);
                                    editor.putBoolean(ActivateProfileListAdapter.PREF_START_TARGET_HELPS, false);
                                    editor.putBoolean(EditorProfilesActivity.PREF_START_TARGET_HELPS, false);
                                    editor.putBoolean(EditorProfileListFragment.PREF_START_TARGET_HELPS, false);
                                    editor.putBoolean(EditorProfileListAdapter.PREF_START_TARGET_HELPS, false);
                                    editor.putBoolean(ProfilePreferencesActivity.PREF_START_TARGET_HELPS, false);
                                    editor.putBoolean(ProfilePreferencesActivity.PREF_START_TARGET_HELPS_SAVE, false);
                                    editor.apply();

                                    restartService = true;
                                }
                                if (actualVersionCode <= 2500) {
                                    ApplicationPreferences.getSharedPreferences(appContext);
                                    SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
                                    editor.putBoolean(ProfilePreferencesActivity.PREF_START_TARGET_HELPS, true);
                                    editor.apply();

                                    restartService = true;
                                }
                                if (actualVersionCode <= 2700) {
                                    PPApplication.logE("PackageReplacedReceiver.onReceive", "donation alarm restart");
                                    PPApplication.setDaysAfterFirstStart(appContext, 0);
                                    PPApplication.setDonationNotificationCount(appContext, 0);
                                    DonationNotificationJob.scheduleJob(appContext, true);

                                    restartService = true;
                                }
                                if (actualVersionCode <= 2900) {
                                    SharedPreferences preferences = appContext.getSharedPreferences(PPApplication.SHARED_PROFILE_PREFS_NAME, Context.MODE_PRIVATE);
                                    if ((preferences.getInt(Profile.PREF_PROFILE_DEVICE_WIFI_AP, 0) == 3) &&
                                            (Build.VERSION.SDK_INT >= 26)) {
                                        // Toggle is not supported for wifi AP in Android 8+
                                        SharedPreferences.Editor editor = preferences.edit();
                                        editor.putInt(Profile.PREF_PROFILE_DEVICE_WIFI_AP, 0);
                                        editor.apply();

                                        restartService = true;
                                    }
                                }
                                if (actualVersionCode <= 3000) {
                                    SharedPreferences preferences = appContext.getSharedPreferences(PPApplication.SHARED_PROFILE_PREFS_NAME, Context.MODE_PRIVATE);
                                    if (preferences.getInt(Profile.PREF_PROFILE_LOCK_DEVICE, 0) == 3) {
                                        SharedPreferences.Editor editor = preferences.edit();
                                        editor.putInt(Profile.PREF_PROFILE_LOCK_DEVICE, 1);
                                        editor.apply();

                                        restartService = true;
                                    }
                                }
                                if (actualVersionCode <= 3100) {
                                    ApplicationPreferences.getSharedPreferences(appContext);
                                    if (!ApplicationPreferences.preferences.contains(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PREF_INDICATOR)) {
                                        SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
                                        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PREF_INDICATOR, ApplicationPreferences.applicationWidgetListPrefIndicator(appContext));
                                        editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND, ApplicationPreferences.applicationWidgetListBackground(appContext));
                                        editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_B, ApplicationPreferences.applicationWidgetListLightnessB(appContext));
                                        editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_T, ApplicationPreferences.applicationWidgetListLightnessT(appContext));
                                        editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_ICON_COLOR, ApplicationPreferences.applicationWidgetListIconColor(appContext));
                                        editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_ICON_LIGHTNESS, ApplicationPreferences.applicationWidgetListIconLightness(appContext));
                                        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_ROUNDED_CORNERS, ApplicationPreferences.applicationWidgetListRoundedCorners(appContext));
                                        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_TYPE, ApplicationPreferences.applicationWidgetListBackgroundType(appContext));
                                        editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_COLOR, ApplicationPreferences.applicationWidgetListBackgroundColor(appContext));
                                        editor.apply();

                                        restartService = true;
                                    }
                                }
                                if (actualVersionCode <= 3200) {
                                    ApplicationPreferences.getSharedPreferences(appContext);
                                    boolean darkBackground = ApplicationPreferences.preferences.getBoolean("notificationDarkBackground", false);
                                    if (darkBackground) {
                                        SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
                                        editor.putString(ApplicationPreferences.PREF_NOTIFICATION_BACKGROUND_COLOR, "1");
                                        editor.apply();

                                        restartService = true;
                                    }
                                }
                            }

                        } catch (Exception ignored) {
                        }
                    } finally {
                        if ((wakeLock != null) && wakeLock.isHeld()) {
                            try {
                                wakeLock.release();
                            } catch (Exception ignored) {}
                        }
                    }
                }
            });

            PPApplication.startHandlerThread();
            final Handler handler3 = new Handler(PPApplication.handlerThread.getLooper());
            handler3.postDelayed(new Runnable() {
                @Override
                public void run() {
                    PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":PackageReplacedReceiver.onReceive.2");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        if (restartService) {
                            //PPApplication.sleep(3000);
                            if (PPApplication.getApplicationStarted(appContext, false)) {
                                // application was started before upgrade
                                if (!PPApplication.getApplicationStarted(appContext, true)) {
                                    // service is not started, start it
                                    PPApplication.logE("PackageReplacedReceiver.onReceive", "restart PhoneProfilesService");
                                    startService(appContext);
                                }
                                else
                                    restartService = false;
                            }
                        }
                        if (!restartService) {
                            //PPApplication.sleep(3000);
                            if (PPApplication.getApplicationStarted(appContext, true)) {
                                // service is started by PPApplication
                                PPApplication.logE("PackageReplacedReceiver.onReceive", "activate profiles");
                                final DataWrapper dataWrapper = new DataWrapper(appContext, false, 0, false);
                                dataWrapper.activateProfileOnBoot();
                                PPApplication.showProfileNotification(/*context*/);
                                ActivateProfileHelper.updateGUI(appContext, true);
                            }
                        }
                    } finally {
                        if ((wakeLock != null) && wakeLock.isHeld()) {
                            try {
                                wakeLock.release();
                            } catch (Exception ignored) {}
                        }
                    }
                }
            }, 5000);
        }
    }

    private void startService(Context context) {
        boolean isStarted = PPApplication.getApplicationStarted(context, false);

        //PPApplication.sleep(3000);

        PPApplication.exitApp(context, null, false/*, false, true*/);

        if (isStarted)
        {
            PPApplication.sleep(2000);

            // start PhoneProfilesService
            PPApplication.logE("PackageReplacedReceiver.startService", "xxx");
            PPApplication.setApplicationStarted(context, true);
            Intent serviceIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_INITIALIZE_START, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ACTIVATE_PROFILES, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_PACKAGE_REPLACE, true);
            PPApplication.startPPService(context, serviceIntent);
        }
    }

}
