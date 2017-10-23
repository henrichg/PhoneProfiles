package sk.henrichg.phoneprofiles;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.os.Handler;
import android.support.annotation.NonNull;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;

class PackageReplacedJob extends Job {

    static final String JOB_TAG  = "PackageReplacedJob";

    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        PPApplication.logE("PackageReplacedJob.onRunJob", "xxx");

        final Context appContext = getContext().getApplicationContext();

        // start delayed boot up broadcast
        PPApplication.startedOnBoot = true;
        final Handler handler = new Handler(appContext.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                PPApplication.logE("PackageReplacedJob.onRunJob", "delayed boot up");
                PPApplication.startedOnBoot = false;
            }
        }, 10000);

        Permissions.setShowRequestAccessNotificationPolicyPermission(appContext, true);
        Permissions.setShowRequestWriteSettingsPermission(appContext, true);
        //ActivateProfileHelper.setScreenUnlocked(appContext, true);

        int oldVersionCode = PPApplication.getSavedVersionCode(appContext);
        PPApplication.logE("@@@ PackageReplacedJob.onRunJob", "oldVersionCode="+oldVersionCode);
        int actualVersionCode;
        try {
            PackageInfo pInfo = appContext.getPackageManager().getPackageInfo(appContext.getPackageName(), 0);
            actualVersionCode = pInfo.versionCode;
            PPApplication.logE("@@@ PackageReplacedJob.onRunJob", "actualVersionCode=" + actualVersionCode);

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
        
        return Result.SUCCESS;
    }

    static void start() {
        JobRequest.Builder jobBuilder = new JobRequest.Builder(JOB_TAG);

        try {
            jobBuilder
                    .setUpdateCurrent(false) // don't update current, it would cancel this currently running job
                    .startNow()
                    .build()
                    .schedule();
        } catch (Exception ignored) { }
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
