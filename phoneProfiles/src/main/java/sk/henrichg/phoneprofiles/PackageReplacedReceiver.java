package sk.henrichg.phoneprofiles;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;

import java.util.Calendar;

public class PackageReplacedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        final Context appContext = context.getApplicationContext();

        //int intentUid = intent.getExtras().getInt("android.intent.extra.UID");
        //int myUid = android.os.Process.myUid();
        //if (intentUid == myUid)
        //{
            //PPApplication.loadPreferences(appContext);

            // start delayed bootup broadcast
            PPApplication.startedOnBoot = true;
            AlarmManager alarmMgr = (AlarmManager)appContext.getSystemService(Context.ALARM_SERVICE);
            Intent delayedBootUpIntent = new Intent(appContext, DelayedBootUpReceiver.class);
            PendingIntent alarmIntent = PendingIntent.getBroadcast(appContext, 0, delayedBootUpIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.SECOND, 10);
            alarmMgr.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent);

            Permissions.setShowRequestAccessNotificationPolicyPermission(appContext, true);
            Permissions.setShowRequestWriteSettingsPermission(appContext, true);
            //ActivateProfileHelper.setScreenUnlocked(appContext, true);

            int oldVersionCode = PPApplication.getSavedVersionCode(appContext);
            PPApplication.logE("@@@ PackageReplacedReceiver.onReceive", "oldVersionCode="+oldVersionCode);
            int actualVersionCode;
            try {
                PackageInfo pinfo = appContext.getPackageManager().getPackageInfo(appContext.getPackageName(), 0);
                actualVersionCode = pinfo.versionCode;
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
                    Handler handler = new Handler();
                    Runnable r = new Runnable() {
                        public void run() {
                            startService(appContext);
                        }
                    };
                    handler.postDelayed(r, 2000);
                }
                else
                    startService(appContext);
            }

        //}
    }

    private void startService(Context context) {
        // must by false for avoiding starts/pause events before restart events
        PPApplication.setApplicationStarted(context, false);

        // start PhoneProfilesService
        context.startService(new Intent(context.getApplicationContext(), PhoneProfilesService.class));
    }

}
