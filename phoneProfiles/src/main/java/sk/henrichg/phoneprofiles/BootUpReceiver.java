package sk.henrichg.phoneprofiles;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

public class BootUpReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("BootUpReceiver.onReceive", "xxx");

        if (intent == null)
            return;

        String action = intent.getAction();
        if ((action != null) && (action.equals(Intent.ACTION_BOOT_COMPLETED) ||
                action.equals("android.intent.action.QUICKBOOT_POWERON") ||
                action.equals("com.htc.intent.action.QUICKBOOT_POWERON"))) {
            // start delayed boot up broadcast
            PPApplication.startedOnBoot = true;
            PhoneProfilesService.startHandlerThread();
            final Handler handler = new Handler(PhoneProfilesService.handlerThread.getLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    PPApplication.logE("BootUpReceiver.onReceive", "delayed boot up");
                    PPApplication.startedOnBoot = false;
                }
            }, 10000);

            PPApplication.setApplicationStarted(context, false);

            if (ApplicationPreferences.applicationStartOnBoot(context)) {
                // start ReceiverService
                //TODO Android O
                //if (Build.VERSION.SDK_INT < 26)
                    context.startService(new Intent(context.getApplicationContext(), PhoneProfilesService.class));
                //else
                //    context.startForegroundService(new Intent(context.getApplicationContext(), PhoneProfilesService.class));
            }
        }
    }

}
