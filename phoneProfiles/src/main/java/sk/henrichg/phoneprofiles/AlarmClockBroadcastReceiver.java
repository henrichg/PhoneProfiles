package sk.henrichg.phoneprofiles;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmClockBroadcastReceiver extends BroadcastReceiver {
    public AlarmClockBroadcastReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        //Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());
        //Log.e("AlarmClockBroadcastReceiver", "ALARM");

        if (!PPApplication.getApplicationStarted(context, true))
            return;

        if (android.os.Build.VERSION.SDK_INT >= 21) {

            if (ActivateProfileHelper.getSystemZenMode(context, ActivateProfileHelper.ZENMODE_ALL)
                    != ActivateProfileHelper.ZENMODE_ALL) {

                DataWrapper dataWrapper = new DataWrapper(context, false, false, 0);

                Profile profile = dataWrapper.getActivatedProfile();
                profile = Profile.getMappedProfile(profile, context);

                if (profile != null) {
                    /*PPNotificationListenerService.requestInterruptionFilter(context.getApplicationContext(),
                            NotificationListenerService.INTERRUPTION_FILTER_ALL);*/
                }
            }
        }
    }
}
