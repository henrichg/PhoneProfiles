package sk.henrichg.phoneprofiles;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ScreenOnOffBroadcastReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("ScreenOnOffBroadcastReceiver.onReceive","xxx");

        Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(appContext, true))
            // application is not started
            return;

        Intent serviceIntent = new Intent(appContext, ScreenOnOffService.class);
        serviceIntent.setAction(intent.getAction());
        context.startService(serviceIntent);
    }

}
