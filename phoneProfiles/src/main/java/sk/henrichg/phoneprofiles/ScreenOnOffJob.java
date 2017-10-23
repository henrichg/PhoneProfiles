package sk.henrichg.phoneprofiles;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;

class ScreenOnOffJob extends Job {

    static final String JOB_TAG  = "ScreenOnOffJob";

    private static final String EXTRA_ACTION = "action";
    
    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        PPApplication.logE("ScreenOnOffJob.onRunJob", "xxx");

        final Context appContext = getContext().getApplicationContext();

        Bundle bundle = params.getTransientExtras();
        String action = bundle.getString(EXTRA_ACTION, "");
        
        if (action != null) {
            if (action.equals(Intent.ACTION_SCREEN_ON))
                PPApplication.logE("@@@ ScreenOnOffJob.onRunJob", "screen on");
            else if (action.equals(Intent.ACTION_SCREEN_OFF)) {
                PPApplication.logE("@@@ ScreenOnOffJob.onRunJob", "screen off");

                //boolean lockDeviceEnabled = false;
                if (PPApplication.lockDeviceActivity != null) {
                    //lockDeviceEnabled = true;
                    PPApplication.lockDeviceActivity.finish();
                    PPApplication.lockDeviceActivity.overridePendingTransition(0, 0);
                }

                //ActivateProfileHelper.setScreenUnlocked(appContext, false);

                if (ApplicationPreferences.notificationShowInStatusBar(appContext) &&
                        ApplicationPreferences.notificationHideInLockScreen(appContext)) {
                    DataWrapper dataWrapper = new DataWrapper(appContext, true, false, 0);
                    dataWrapper.getActivateProfileHelper().initialize(dataWrapper, appContext);
                    //dataWrapper.getActivateProfileHelper().removeNotification();
                    //dataWrapper.getActivateProfileHelper().setAlarmForRecreateNotification();
                    Profile activatedProfile = dataWrapper.getActivatedProfile();
                    if (PhoneProfilesService.instance != null)
                        PhoneProfilesService.instance.showProfileNotification(activatedProfile, dataWrapper);
                    dataWrapper.invalidateDataWrapper();
                }
            }
            if (action.equals(Intent.ACTION_USER_PRESENT)) {
                PPApplication.logE("@@@ ScreenOnOffJob.onRunJob", "screen unlock");
                //ActivateProfileHelper.setScreenUnlocked(appContext, true);

                final DataWrapper dataWrapper = new DataWrapper(appContext, true, false, 0);
                dataWrapper.getActivateProfileHelper().initialize(dataWrapper, appContext);

                if (ApplicationPreferences.notificationShowInStatusBar(appContext) &&
                        ApplicationPreferences.notificationHideInLockScreen(appContext)) {
                    //dataWrapper.getActivateProfileHelper().removeNotification();
                    //dataWrapper.getActivateProfileHelper().setAlarmForRecreateNotification();
                    Profile activatedProfile = dataWrapper.getActivatedProfile();
                    if (PhoneProfilesService.instance != null)
                        PhoneProfilesService.instance.showProfileNotification(activatedProfile, dataWrapper);
                }

                // change screen timeout
                    /*if (lockDeviceEnabled && Permissions.checkLockDevice(appContext))
                        Settings.System.putInt(appContext.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, PPApplication.screenTimeoutBeforeDeviceLock);*/
                final int screenTimeout = ActivateProfileHelper.getActivatedProfileScreenTimeout(appContext);
                if ((screenTimeout > 0) && (Permissions.checkScreenTimeout(appContext))) {
                    if (PPApplication.screenTimeoutHandler != null) {
                        PPApplication.screenTimeoutHandler.post(new Runnable() {
                            public void run() {
                                dataWrapper.getActivateProfileHelper().setScreenTimeout(screenTimeout, appContext);
                                dataWrapper.invalidateDataWrapper();
                            }
                        });
                    }/* else {
                        dataWrapper.getActivateProfileHelper().setScreenTimeout(screenTimeout);
                        dataWrapper.invalidateDataWrapper();
                    }*/
                }

                // enable/disable keyguard
                try {
                    // start PhoneProfilesService
                    //PPApplication.firstStartServiceStarted = false;
                    Intent serviceIntent = new Intent(appContext, PhoneProfilesService.class);
                    serviceIntent.putExtra(PhoneProfilesService.EXTRA_SWITCH_KEYGUARD, true);
                    //TODO Android O
                    //if (Build.VERSION.SDK_INT < 26)
                    appContext.startService(serviceIntent);
                    //else
                    //    startForegroundService(serviceIntent);
                } catch (Exception ignored) {}
            }

            if (action.equals(Intent.ACTION_SCREEN_ON)) {
                PPApplication.logE("@@@ ScreenOnOffJob.onRunJob", "screen on");
                if (ApplicationPreferences.notificationShowInStatusBar(appContext) &&
                        ApplicationPreferences.notificationHideInLockScreen(appContext)) {
                    DataWrapper dataWrapper = new DataWrapper(appContext, true, false, 0);
                    dataWrapper.getActivateProfileHelper().initialize(dataWrapper, appContext);
                    //dataWrapper.getActivateProfileHelper().removeNotification();
                    //dataWrapper.getActivateProfileHelper().setAlarmForRecreateNotification();
                    Profile activatedProfile = dataWrapper.getActivatedProfile();
                    if (PhoneProfilesService.instance != null)
                        PhoneProfilesService.instance.showProfileNotification(activatedProfile, dataWrapper);
                    dataWrapper.invalidateDataWrapper();
                }
            }
        }

        return Result.SUCCESS;
    }

    static void start(Context context, String action) {
        final JobRequest.Builder jobBuilder = new JobRequest.Builder(JOB_TAG);

        final Bundle bundle = new Bundle();
        bundle.putString(EXTRA_ACTION, action);

        final Handler handler = new Handler(context.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    jobBuilder
                            .setUpdateCurrent(false) // don't update current, it would cancel this currently running job
                            .setTransientExtras(bundle)
                            .startNow()
                            .build()
                            .schedule();
                } catch (Exception ignored) { }
            }
        });
    }

}
