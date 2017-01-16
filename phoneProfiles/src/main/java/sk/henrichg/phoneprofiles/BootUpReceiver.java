package sk.henrichg.phoneprofiles;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootUpReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        PPApplication.logE("BootUpReceiver.onReceive", "xxx");

        PPApplication.loadPreferences(context);

        PPApplication.setApplicationStarted(context, false);

        if (PPApplication.applicationStartOnBoot)
        {
            // start ReceiverService
            context.startService(new Intent(context.getApplicationContext(), PhoneProfilesService.class));
        }

    }

}
