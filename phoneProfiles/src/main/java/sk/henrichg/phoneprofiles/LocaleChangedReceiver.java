package sk.henrichg.phoneprofiles;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class LocaleChangedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ApplicationPreferences.applicationLanguage(context).equals("system"))
        {
            NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(PPApplication.PROFILE_NOTIFICATION_ID);
        }

    }

}
