package sk.henrichg.phoneprofiles;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public class PackageReplacedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        //int intentUid = intent.getExtras().getInt("android.intent.extra.UID");
        //int myUid = android.os.Process.myUid();
        //if (intentUid == myUid)
        //{
            //PPApplication.loadPreferences(context);

            PPApplication.setShowRequestAccessNotificationPolicyPermission(context.getApplicationContext(), true);
            PPApplication.setShowRequestWriteSettingsPermission(context.getApplicationContext(), true);
            PPApplication.setScreenUnlocked(context.getApplicationContext(), true);

            int oldVersionCode = PPApplication.getSavedVersionCode(context.getApplicationContext());
            PPApplication.logE("@@@ PackageReplacedReceiver.onReceive", "oldVersionCode="+oldVersionCode);
            int actualVersionCode;
            try {
                PackageInfo pinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                actualVersionCode = pinfo.versionCode;
                PPApplication.logE("@@@ PackageReplacedReceiver.onReceive", "actualVersionCode=" + actualVersionCode);

                if (oldVersionCode < actualVersionCode) {
                    if (actualVersionCode <= 2100) {
                        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putBoolean(ActivateProfileActivity.PREF_START_TARGET_HELPS, false);
                        editor.putBoolean(ActivateProfileListFragment.PREF_START_TARGET_HELPS, false);
                        editor.putBoolean(ActivateProfileListAdapter.PREF_START_TARGET_HELPS, false);
                        editor.putBoolean(EditorProfilesActivity.PREF_START_TARGET_HELPS, false);
                        editor.putBoolean(EditorProfileListFragment.PREF_START_TARGET_HELPS, false);
                        editor.putBoolean(EditorProfileListAdapter.PREF_START_TARGET_HELPS, false);
                        editor.putBoolean(ProfilePreferencesActivity.PREF_START_TARGET_HELPS, true);
                        editor.commit();
                    }
                }
            } catch (PackageManager.NameNotFoundException e) {
                //e.printStackTrace();
            }

            if (PPApplication.getApplicationStarted(context, false))
            {
                if (PhoneProfilesService.instance != null) {
                    // stop PhoneProfilesService
                    context.stopService(new Intent(context.getApplicationContext(), PhoneProfilesService.class));
                    PPApplication.sleep(2000);
                }

                // must by false for avoiding starts/pause events before restart events
                PPApplication.setApplicationStarted(context, false);

                // start ReceiverService
                context.startService(new Intent(context.getApplicationContext(), PhoneProfilesService.class));
            }

        //}
    }

}
