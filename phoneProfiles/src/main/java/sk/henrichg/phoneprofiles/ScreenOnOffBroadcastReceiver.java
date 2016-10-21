package sk.henrichg.phoneprofiles;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ScreenOnOffBroadcastReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {

        GlobalData.logE("ScreenOnOffBroadcastReceiver.onReceive","xxx");

        if (!GlobalData.getApplicationStarted(context))
            // application is not started
            return;

        GlobalData.logE("ScreenOnOffBroadcastReceiver.onReceive","application started");

        GlobalData.loadPreferences(context);

        if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            GlobalData.logE("@@@ ScreenOnOffBroadcastReceiver.onReceive", "screen on");
            if (GlobalData.getApplicationStarted(context)) {
                if (GlobalData.notificationShowInStatusBar &&
                        GlobalData.notificationHideInLockscreen) {
                    DataWrapper dataWrapper = new DataWrapper(context, true, false, 0);
                    dataWrapper.getActivateProfileHelper().initialize(dataWrapper, null, context);
                    dataWrapper.getActivateProfileHelper().removeNotification();
                    dataWrapper.getActivateProfileHelper().setAlarmForRecreateNotification();
                    dataWrapper.invalidateDataWrapper();
                }
            }
        }
        else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            GlobalData.logE("@@@ ScreenOnOffBroadcastReceiver.onReceive", "screen off");
            GlobalData.setScreenUnlocked(context, false);
        }
        if (intent.getAction().equals(Intent.ACTION_USER_PRESENT))
        {
            GlobalData.logE("@@@ ScreenOnOffBroadcastReceiver.onReceive", "screen unlock");
            GlobalData.setScreenUnlocked(context, true);

            if (GlobalData.getApplicationStarted(context)) {
                if (GlobalData.notificationShowInStatusBar &&
                    GlobalData.notificationHideInLockscreen) {
                    DataWrapper dataWrapper = new DataWrapper(context, true, false, 0);
                    dataWrapper.getActivateProfileHelper().initialize(dataWrapper, null, context);
                    dataWrapper.getActivateProfileHelper().removeNotification();
                    dataWrapper.getActivateProfileHelper().setAlarmForRecreateNotification();
                    dataWrapper.invalidateDataWrapper();
                }
            }

            // enable/disable keyguard
            Intent keyguardService = new Intent(context.getApplicationContext(), KeyguardService.class);
            context.startService(keyguardService);
        }
    }

}
