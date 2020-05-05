package sk.henrichg.phoneprofiles;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class NotificationCancelAlarmBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //PPApplication.logE("### NotificationCancelAlarmBroadcastReceiver","xxx");

        if (Build.VERSION.SDK_INT >= 26)
            return;

        if (PhoneProfilesService.getInstance() != null)
            PhoneProfilesService.getInstance().stopForeground(true);
        else {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null)
                notificationManager.cancel(PPApplication.PROFILE_NOTIFICATION_ID);
        }

    }

}
