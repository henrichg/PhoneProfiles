package sk.henrichg.phoneprofiles;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.PowerManager;

import static android.content.Context.POWER_SERVICE;

public class ScreenOnOffBroadcastReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("ScreenOnOffBroadcastReceiver.onReceive","xxx");

        final Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(appContext, true))
            // application is not started
            return;

        //ScreenOnOffJob.start(appContext, intent.getAction());

        final String action = intent.getAction();

        PPApplication.logE("@@@ ScreenOnOffBroadcastReceiver.onReceive", "before start handler");
        PPApplication.startHandlerThread();
        final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                if (powerManager != null) {
                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "ScreenOnOffBroadcastReceiver.onReceive");
                    wakeLock.acquire(10 * 60 * 1000);
                }

                if ((action != null) && action.equals(Intent.ACTION_SCREEN_ON))
                    PPApplication.logE("@@@ ScreenOnOffBroadcastReceiver.onReceive", "screen on");
                else if ((action != null) && action.equals(Intent.ACTION_SCREEN_OFF)) {
                    PPApplication.logE("@@@ ScreenOnOffBroadcastReceiver.onReceive", "screen off");

                    //boolean lockDeviceEnabled = false;
                    if (PPApplication.lockDeviceActivity != null) {
                        //lockDeviceEnabled = true;
                        PPApplication.lockDeviceActivity.finish();
                    }

                    //ActivateProfileHelper.setScreenUnlocked(appContext, false);

                    if (ApplicationPreferences.notificationShowInStatusBar(appContext) &&
                            ApplicationPreferences.notificationHideInLockScreen(appContext)) {
                        DataWrapper dataWrapper = new DataWrapper(appContext, true, false, 0);
                        //dataWrapper.getActivateProfileHelper().removeNotification();
                        //dataWrapper.getActivateProfileHelper().setAlarmForRecreateNotification();
                        Profile activatedProfile = dataWrapper.getActivatedProfile();
                        if (PhoneProfilesService.instance != null)
                            PhoneProfilesService.instance.showProfileNotification(activatedProfile, dataWrapper);
                        dataWrapper.invalidateDataWrapper();
                    }
                }
                if ((action != null) && action.equals(Intent.ACTION_USER_PRESENT)) {
                    PPApplication.logE("@@@ ScreenOnOffBroadcastReceiver.onReceive", "screen unlock");
                    //ActivateProfileHelper.setScreenUnlocked(appContext, true);

                    final DataWrapper dataWrapper = new DataWrapper(appContext, true, false, 0);

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
                                    ActivateProfileHelper.setScreenTimeout(screenTimeout, appContext);
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

                if ((action != null) && action.equals(Intent.ACTION_SCREEN_ON)) {
                    PPApplication.logE("@@@ ScreenOnOffBroadcastReceiver.onReceive", "screen on");
                    if (ApplicationPreferences.notificationShowInStatusBar(appContext) &&
                            ApplicationPreferences.notificationHideInLockScreen(appContext)) {
                        DataWrapper dataWrapper = new DataWrapper(appContext, true, false, 0);
                        //dataWrapper.getActivateProfileHelper().removeNotification();
                        //dataWrapper.getActivateProfileHelper().setAlarmForRecreateNotification();
                        Profile activatedProfile = dataWrapper.getActivatedProfile();
                        if (PhoneProfilesService.instance != null)
                            PhoneProfilesService.instance.showProfileNotification(activatedProfile, dataWrapper);
                        dataWrapper.invalidateDataWrapper();
                    }
                }

                if ((wakeLock != null) && wakeLock.isHeld())
                    wakeLock.release();
            }
        });
        PPApplication.logE("@@@ ScreenOnOffBroadcastReceiver.onReceive", "after start handler");

    }

}
