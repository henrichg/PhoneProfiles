package sk.henrichg.phoneprofiles;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.os.Handler;
import android.os.PowerManager;

import static android.content.Context.POWER_SERVICE;

public class PackageReplacedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("PackageReplacedReceiver.onReceive", "intent="+intent);
        if ((intent != null) && intent.getAction().equals(Intent.ACTION_MY_PACKAGE_REPLACED)) {
            // PackageReplacedJob.start(context.getApplicationContext());
            final Context appContext = context.getApplicationContext();

            final Handler handler = new Handler(appContext.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "PackageReplacedReceiver.onReceive");
                    wakeLock.acquire(10 * 60 * 1000);

                    // start delayed boot up broadcast
                    PPApplication.startedOnBoot = true;
                    final Handler handler = new Handler(appContext.getMainLooper());
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            PPApplication.logE("PackageReplacedReceiver.onReceive", "delayed boot up");
                            PPApplication.startedOnBoot = false;
                        }
                    }, 10000);

                    Permissions.setShowRequestAccessNotificationPolicyPermission(appContext, true);
                    Permissions.setShowRequestWriteSettingsPermission(appContext, true);
                    //ActivateProfileHelper.setScreenUnlocked(appContext, true);

                    int oldVersionCode = PPApplication.getSavedVersionCode(appContext);
                    PPApplication.logE("@@@ PackageReplacedReceiver.onReceive", "oldVersionCode="+oldVersionCode);
                    int actualVersionCode;
                    try {
                        PackageInfo pInfo = appContext.getPackageManager().getPackageInfo(appContext.getPackageName(), 0);
                        actualVersionCode = pInfo.versionCode;
                        PPApplication.logE("@@@ PackageReplacedReceiver.onReceive", "actualVersionCode=" + actualVersionCode);

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
                        }
                    } catch (Exception e) {
                        //e.printStackTrace();
                    }

                    if (PPApplication.getApplicationStarted(appContext, false))
                    {
                        if (PhoneProfilesService.instance != null) {
                            // stop PhoneProfilesService
                            appContext.stopService(new Intent(appContext, PhoneProfilesService.class));
                            Handler _handler = new Handler(appContext.getMainLooper());
                            Runnable r = new Runnable() {
                                public void run() {
                                    startService(appContext);
                                }
                            };
                            _handler.postDelayed(r, 2000);
                        }
                        else
                            startService(appContext);
                    }

                    wakeLock.release();
                }
            });

        }
    }

    private void startService(Context context) {
        // must by false for avoiding starts/pause events before restart events
        PPApplication.setApplicationStarted(context, false);

        // start PhoneProfilesService
        //TODO Android O
        //if (Build.VERSION.SDK_INT < 26)
        context.startService(new Intent(context.getApplicationContext(), PhoneProfilesService.class));
        //else
        //    context.startForegroundService(new Intent(context.getApplicationContext(), PhoneProfilesService.class));
    }

}
