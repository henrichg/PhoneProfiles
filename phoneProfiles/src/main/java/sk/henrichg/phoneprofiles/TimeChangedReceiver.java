package sk.henrichg.phoneprofiles;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class TimeChangedReceiver extends BroadcastReceiver {
    public TimeChangedReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if ((intent != null) && (intent.getAction() != null)) {
            if (intent.getAction().equals(Intent.ACTION_TIMEZONE_CHANGED)) {
                PPApplication.logE("##### TimeChangedReceiver.onReceive", "xxx");

                Context appContext = context.getApplicationContext();

                if (!PPApplication.getApplicationStarted(appContext, true))
                    return;

                ProfileDurationAlarmBroadcastReceiver.removeAlarm(context);
                Profile.setActivatedProfileForDuration(context, 0);
            }
        }
    }
}
