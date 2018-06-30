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
                    if (PhoneProfilesService.instance != null) {
                        if (PhoneProfilesService.instance.lockDeviceActivity != null) {
                            //lockDeviceEnabled = true;
                            PhoneProfilesService.instance.lockDeviceActivity.finish();
                        }
                    }

                    //ActivateProfileHelper.setScreenUnlocked(appContext, false);

                    if (ApplicationPreferences.notificationShowInStatusBar(appContext) &&
                            ApplicationPreferences.notificationHideInLockScreen(appContext)) {
                        DataWrapper dataWrapper = new DataWrapper(appContext, false, 0);
                        //dataWrapper.getActivateProfileHelper().removeNotification();
                        //dataWrapper.getActivateProfileHelper().setAlarmForRecreateNotification();
                        PPApplication.showProfileNotification(appContext);
                        dataWrapper.invalidateDataWrapper();
                    }
                }
                if ((action != null) && action.equals(Intent.ACTION_USER_PRESENT)) {
                    PPApplication.logE("@@@ ScreenOnOffBroadcastReceiver.onReceive", "screen unlock");
                    //ActivateProfileHelper.setScreenUnlocked(appContext, true);

                    if (ApplicationPreferences.notificationShowInStatusBar(appContext) &&
                            ApplicationPreferences.notificationHideInLockScreen(appContext)) {
                        //dataWrapper.getActivateProfileHelper().removeNotification();
                        //dataWrapper.getActivateProfileHelper().setAlarmForRecreateNotification();
                        PPApplication.showProfileNotification(appContext);
                    }

                    // change screen timeout
                    final DataWrapper dataWrapper = new DataWrapper(appContext, false, 0);
                    /*if (lockDeviceEnabled && Permissions.checkLockDevice(appContext))
                        Settings.System.putInt(appContext.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, PPApplication.screenTimeoutBeforeDeviceLock);*/
                    final int screenTimeout = ActivateProfileHelper.getActivatedProfileScreenTimeout(appContext);
                    PPApplication.logE("@@@ ScreenOnOffBroadcastReceiver.onReceive", "screenTimeout="+screenTimeout);
                    if ((screenTimeout > 0) && (Permissions.checkScreenTimeout(appContext))) {
                        PPApplication.logE("@@@ ScreenOnOffBroadcastReceiver.onReceive", "permission ok");
                        if (PPApplication.screenTimeoutHandler != null) {
                            PPApplication.screenTimeoutHandler.post(new Runnable() {
                                public void run() {
                                    PPApplication.logE("@@@ ScreenOnOffBroadcastReceiver.onReceive", "call ActivateProfileHelper.setScreenTimeout");
                                    ActivateProfileHelper.setScreenTimeout(screenTimeout, appContext);
                                }
                            });
                        }/* else {
                            dataWrapper.getActivateProfileHelper().setScreenTimeout(screenTimeout);
                        }*/
                    }
                    dataWrapper.invalidateDataWrapper();

                    // enable/disable keyguard
                    try {
                        // start PhoneProfilesService
                        //PPApplication.firstStartServiceStarted = false;
                        Intent serviceIntent = new Intent(appContext, PhoneProfilesService.class);
                        serviceIntent.putExtra(PhoneProfilesService.EXTRA_SWITCH_KEYGUARD, true);
                        PPApplication.startPPService(appContext, serviceIntent);
                    } catch (Exception ignored) {}
                }

                if ((action != null) && action.equals(Intent.ACTION_SCREEN_ON)) {
                    PPApplication.logE("@@@ ScreenOnOffBroadcastReceiver.onReceive", "screen on");
                    if (ApplicationPreferences.notificationShowInStatusBar(appContext) &&
                            ApplicationPreferences.notificationHideInLockScreen(appContext)) {
                        DataWrapper dataWrapper = new DataWrapper(appContext, false, 0);
                        //dataWrapper.getActivateProfileHelper().removeNotification();
                        //dataWrapper.getActivateProfileHelper().setAlarmForRecreateNotification();
                        PPApplication.showProfileNotification(appContext);
                        dataWrapper.invalidateDataWrapper();
                    }
                }

                if ((wakeLock != null) && wakeLock.isHeld()) {
                    try {
                        wakeLock.release();
                    } catch (Exception ignored) {}
                }
            }
        });
        PPApplication.logE("@@@ ScreenOnOffBroadcastReceiver.onReceive", "after start handler");

    }

}
