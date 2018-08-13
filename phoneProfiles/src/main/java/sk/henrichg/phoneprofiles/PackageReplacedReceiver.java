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

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("PackageReplacedReceiver.onReceive", "xxx");
        if ((intent != null) && (intent.getAction() != null) && intent.getAction().equals(Intent.ACTION_MY_PACKAGE_REPLACED)) {

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
                int actualVersionCode = pInfo.versionCode;
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
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "PackageReplacedReceiver.onReceive.2");
                        wakeLock.acquire(10 * 60 * 1000);
                    }

                    Permissions.setShowRequestAccessNotificationPolicyPermission(appContext, true);
                    Permissions.setShowRequestWriteSettingsPermission(appContext, true);
                    //ActivateProfileHelper.setScreenUnlocked(appContext, true);

                    PPApplication.logE("PackageReplacedReceiver.onReceive", "oldVersionCode=" + oldVersionCode);
                    int actualVersionCode;
                    try {
                        PackageInfo pInfo = appContext.getPackageManager().getPackageInfo(appContext.getPackageName(), 0);
                        actualVersionCode = pInfo.versionCode;
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
                            }
                            if (actualVersionCode <= 2500) {
                                ApplicationPreferences.getSharedPreferences(appContext);
                                SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
                                editor.putBoolean(ProfilePreferencesActivity.PREF_START_TARGET_HELPS, true);
                                editor.apply();
                            }
                            if (actualVersionCode <= 2700) {
                                PPApplication.logE("PackageReplacedReceiver.onReceive", "donation alarm restart");
                                PPApplication.setDaysAfterFirstStart(appContext, 0);
                                PPApplication.setDonationNotificationCount(appContext, 0);
                                AboutApplicationJob.scheduleJob(appContext, true);
                            }
                            if (actualVersionCode <= 2900) {
                                SharedPreferences preferences = appContext.getSharedPreferences(PPApplication.SHARED_PROFILE_PREFS_NAME, Context.MODE_PRIVATE);
                                if ((preferences.getInt(Profile.PREF_PROFILE_DEVICE_WIFI_AP, 0) == 3) &&
                                        (Build.VERSION.SDK_INT >= 26)) {
                                    // Toggle is not supported for wifi AP in Android 8+
                                    SharedPreferences.Editor editor = preferences.edit();
                                    editor.putInt(Profile.PREF_PROFILE_DEVICE_WIFI_AP, 0);
                                    editor.apply();
                                }
                            }
                            if (actualVersionCode <= 3000) {
                                SharedPreferences preferences = appContext.getSharedPreferences(PPApplication.SHARED_PROFILE_PREFS_NAME, Context.MODE_PRIVATE);
                                if (preferences.getInt(Profile.PREF_PROFILE_LOCK_DEVICE, 0) == 3) {
                                    SharedPreferences.Editor editor = preferences.edit();
                                    editor.putInt(Profile.PREF_PROFILE_LOCK_DEVICE, 1);
                                    editor.apply();
                                }
                            }
                        }
                    } catch (Exception ignored) {
                    }

                    if ((wakeLock != null) && wakeLock.isHeld()) {
                        try {
                            wakeLock.release();
                        } catch (Exception ignored) {}
                    }
                }
            });

            if (PPApplication.getApplicationStarted(appContext, false))
            {
                startService(appContext);
                /*
                PPApplication.startHandlerThread();
                final Handler handler3 = new Handler(PPApplication.handlerThread.getLooper());
                handler3.post(new Runnable() {
                    @Override
                    public void run() {
                        PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                        PowerManager.WakeLock wakeLock = null;
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "PackageReplacedReceiver.onReceive.3");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        if (PhoneProfilesService.getInstance() != null) {
                            // stop PhoneProfilesService
                            PPApplication.sleep(2000);
                            appContext.stopService(new Intent(appContext, PhoneProfilesService.class));
                            PPApplication.sleep(2000);
                            startService(appContext);
                        } else
                            startService(appContext);

                        if ((wakeLock != null) && wakeLock.isHeld()) {
                            try {
                                wakeLock.release();
                            } catch (Exception ignored) {}
                        }
                    }
                });
                */
            }
        }
    }

    private void startService(Context context) {
        // must by false for avoiding starts/pause events before restart events
        //PPApplication.setApplicationStarted(context, false);

        // start PhoneProfilesService
        Intent serviceIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
        serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, true);
        serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_PACKAGE_REPLACE, true);
        PPApplication.startPPService(context, serviceIntent);
    }

}
