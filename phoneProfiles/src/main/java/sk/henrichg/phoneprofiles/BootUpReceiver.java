package sk.henrichg.phoneprofiles;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

public class BootUpReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("BootUpReceiver.onReceive", "xxx");

        // start delayed boot up broadcast
        PPApplication.startedOnBoot = true;
        final Handler handler = new Handler(context.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                PPApplication.logE("BootUpReceiver.onReceive", "delayed boot up");
                PPApplication.startedOnBoot = false;
            }
        }, 10000);

        PPApplication.setApplicationStarted(context, false);

        if (ApplicationPreferences.applicationStartOnBoot(context))
        {
            // start ReceiverService
            context.startService(new Intent(context.getApplicationContext(), PhoneProfilesService.class));
        }

    }

}
