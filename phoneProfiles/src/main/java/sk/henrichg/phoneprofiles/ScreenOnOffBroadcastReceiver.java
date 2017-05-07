package sk.henrichg.phoneprofiles;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;

public class ScreenOnOffBroadcastReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        //Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        PPApplication.logE("ScreenOnOffBroadcastReceiver.onReceive","xxx");

        Context appContext = context.getApplicationContext();
        if (!PPApplication.getApplicationStarted(appContext, true))
            // application is not started
            return;

        PPApplication.logE("ScreenOnOffBroadcastReceiver.onReceive","application started");

        boolean lockDeviceEnabled = false;
        if (PPApplication.lockDeviceActivity != null) {
            lockDeviceEnabled = true;
            PPApplication.lockDeviceActivity.finish();
            PPApplication.lockDeviceActivity.overridePendingTransition(0, 0);
        }

        //PPApplication.loadPreferences(context);

        if (intent.getAction().equals(Intent.ACTION_SCREEN_ON))
            PPApplication.logE("@@@ ScreenOnOffBroadcastReceiver.onReceive", "screen on");
        else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            PPApplication.logE("@@@ ScreenOnOffBroadcastReceiver.onReceive", "screen off");
            ActivateProfileHelper.setScreenUnlocked(appContext, false);

            if (ApplicationPreferences.notificationShowInStatusBar(appContext) &&
                    ApplicationPreferences.notificationHideInLockscreen(appContext)) {
                DataWrapper dataWrapper = new DataWrapper(appContext, true, false, 0);
                dataWrapper.getActivateProfileHelper().initialize(dataWrapper, appContext);
                //dataWrapper.getActivateProfileHelper().removeNotification();
                //dataWrapper.getActivateProfileHelper().setAlarmForRecreateNotification();
                Profile activatedProfile = dataWrapper.getActivatedProfile();
                dataWrapper.getActivateProfileHelper().showNotification(activatedProfile);
                dataWrapper.invalidateDataWrapper();
            }
        }
        if (intent.getAction().equals(Intent.ACTION_USER_PRESENT))
        {
            PPApplication.logE("@@@ ScreenOnOffBroadcastReceiver.onReceive", "screen unlock");
            ActivateProfileHelper.setScreenUnlocked(appContext, true);

            DataWrapper dataWrapper = new DataWrapper(appContext, true, false, 0);
            dataWrapper.getActivateProfileHelper().initialize(dataWrapper, appContext);

            if (ApplicationPreferences.notificationShowInStatusBar(appContext) &&
                    ApplicationPreferences.notificationHideInLockscreen(appContext)) {
                //dataWrapper.getActivateProfileHelper().removeNotification();
                //dataWrapper.getActivateProfileHelper().setAlarmForRecreateNotification();
                Profile activatedProfile = dataWrapper.getActivatedProfile();
                dataWrapper.getActivateProfileHelper().showNotification(activatedProfile);
                dataWrapper.invalidateDataWrapper();
            }

            // change screen timeout
            if (lockDeviceEnabled && Permissions.checkLockDevice(appContext))
                Settings.System.putInt(appContext.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, PPApplication.screenTimeoutBeforeDeviceLock);
            int screenTimeout = ActivateProfileHelper.getActivatedProfileScreenTimeout(appContext);
            if (screenTimeout > 0)
                dataWrapper.getActivateProfileHelper().setScreenTimeout(screenTimeout);

            dataWrapper.invalidateDataWrapper();

            // enable/disable keyguard
            Intent keyguardService = new Intent(appContext, KeyguardService.class);
            appContext.startService(keyguardService);
        }

        if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            PPApplication.logE("@@@ ScreenOnOffBroadcastReceiver.onReceive", "screen on");
            if (ApplicationPreferences.notificationShowInStatusBar(appContext) &&
                    ApplicationPreferences.notificationHideInLockscreen(appContext)) {
                DataWrapper dataWrapper = new DataWrapper(appContext, true, false, 0);
                dataWrapper.getActivateProfileHelper().initialize(dataWrapper, appContext);
                //dataWrapper.getActivateProfileHelper().removeNotification();
                //dataWrapper.getActivateProfileHelper().setAlarmForRecreateNotification();
                Profile activatedProfile = dataWrapper.getActivatedProfile();
                dataWrapper.getActivateProfileHelper().showNotification(activatedProfile);
                dataWrapper.invalidateDataWrapper();
            }
        }

    }

}
