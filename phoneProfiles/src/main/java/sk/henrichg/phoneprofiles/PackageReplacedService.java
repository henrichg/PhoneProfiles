package sk.henrichg.phoneprofiles;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;

import com.commonsware.cwac.wakeful.WakefulIntentService;

public class PackageReplacedService extends WakefulIntentService {

    public PackageReplacedService() {
        super("PackageReplacedService");
    }

    @Override
    protected void doWakefulWork(Intent intent) {
        if (intent != null) {
            final Context appContext = getApplicationContext();

            // start delayed boot up broadcast
            PPApplication.startedOnBoot = true;
            final Handler handler = new Handler(getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    PPApplication.logE("PackageReplacedService.onHandleIntent", "delayed boot up");
                    PPApplication.startedOnBoot = false;
                }
            }, 10000);

            Permissions.setShowRequestAccessNotificationPolicyPermission(appContext, true);
            Permissions.setShowRequestWriteSettingsPermission(appContext, true);
            //ActivateProfileHelper.setScreenUnlocked(appContext, true);

            int oldVersionCode = PPApplication.getSavedVersionCode(appContext);
            PPApplication.logE("@@@ PackageReplacedService.onHandleIntent", "oldVersionCode="+oldVersionCode);
            int actualVersionCode;
            try {
                PackageInfo pInfo = appContext.getPackageManager().getPackageInfo(appContext.getPackageName(), 0);
                actualVersionCode = pInfo.versionCode;
                PPApplication.logE("@@@ PackageReplacedService.onHandleIntent", "actualVersionCode=" + actualVersionCode);

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
            } catch (PackageManager.NameNotFoundException e) {
                //e.printStackTrace();
            }

            if (PPApplication.getApplicationStarted(appContext, false))
            {
                if (PhoneProfilesService.instance != null) {
                    // stop PhoneProfilesService
                    appContext.stopService(new Intent(appContext, PhoneProfilesService.class));
                    Handler _handler = new Handler(getMainLooper());
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
