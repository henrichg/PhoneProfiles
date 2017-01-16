package sk.henrichg.phoneprofiles;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ScreenOnOffBroadcastReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        //Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        PPApplication.logE("ScreenOnOffBroadcastReceiver.onReceive","xxx");

        if (!PPApplication.getApplicationStarted(context, true))
            // application is not started
            return;

        PPApplication.logE("ScreenOnOffBroadcastReceiver.onReceive","application started");

        PPApplication.loadPreferences(context);

        if (intent.getAction().equals(Intent.ACTION_SCREEN_ON))
            PPApplication.logE("@@@ ScreenOnOffBroadcastReceiver.onReceive", "screen on");
        else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            PPApplication.logE("@@@ ScreenOnOffBroadcastReceiver.onReceive", "screen off");
            PPApplication.setScreenUnlocked(context, false);

            if (PPApplication.getApplicationStarted(context, true)) {
                if (PPApplication.notificationShowInStatusBar &&
                        PPApplication.notificationHideInLockscreen) {
                    DataWrapper dataWrapper = new DataWrapper(context, true, false, 0);
                    dataWrapper.getActivateProfileHelper().initialize(dataWrapper, context);
                    //dataWrapper.getActivateProfileHelper().removeNotification();
                    //dataWrapper.getActivateProfileHelper().setAlarmForRecreateNotification();
                    Profile activatedProfile = dataWrapper.getActivatedProfile();
                    dataWrapper.getActivateProfileHelper().showNotification(activatedProfile);
                    dataWrapper.invalidateDataWrapper();
                }
            }
        }
        if (intent.getAction().equals(Intent.ACTION_USER_PRESENT))
        {
            PPApplication.logE("@@@ ScreenOnOffBroadcastReceiver.onReceive", "screen unlock");
            PPApplication.setScreenUnlocked(context, true);

            if (PPApplication.getApplicationStarted(context, true)) {
                if (PPApplication.notificationShowInStatusBar &&
                    PPApplication.notificationHideInLockscreen) {
                    DataWrapper dataWrapper = new DataWrapper(context, true, false, 0);
                    dataWrapper.getActivateProfileHelper().initialize(dataWrapper, context);
                    //dataWrapper.getActivateProfileHelper().removeNotification();
                    //dataWrapper.getActivateProfileHelper().setAlarmForRecreateNotification();
                    Profile activatedProfile = dataWrapper.getActivatedProfile();
                    dataWrapper.getActivateProfileHelper().showNotification(activatedProfile);
                    dataWrapper.invalidateDataWrapper();
                }
            }

            // enable/disable keyguard
            Intent keyguardService = new Intent(context.getApplicationContext(), KeyguardService.class);
            context.startService(keyguardService);
        }

        if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            PPApplication.logE("@@@ ScreenOnOffBroadcastReceiver.onReceive", "screen on");
            if (PPApplication.getApplicationStarted(context, true)) {
                if (PPApplication.notificationShowInStatusBar &&
                        PPApplication.notificationHideInLockscreen) {
                    DataWrapper dataWrapper = new DataWrapper(context, true, false, 0);
                    dataWrapper.getActivateProfileHelper().initialize(dataWrapper, context);
                    //dataWrapper.getActivateProfileHelper().removeNotification();
                    //dataWrapper.getActivateProfileHelper().setAlarmForRecreateNotification();
                    Profile activatedProfile = dataWrapper.getActivatedProfile();
                    dataWrapper.getActivateProfileHelper().showNotification(activatedProfile);
                    dataWrapper.invalidateDataWrapper();
                }
            }
        }

    }

}
