package sk.henrichg.phoneprofiles;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootUpReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        GlobalData.logE("BootUpReceiver.onReceive", "xxx");

        GlobalData.loadPreferences(context);

        GlobalData.setApplicationStarted(context, false);

        if (GlobalData.applicationStartOnBoot)
        {
            // start ReceiverService
            context.startService(new Intent(context.getApplicationContext(), PhoneProfilesService.class));
        }

    }

}
