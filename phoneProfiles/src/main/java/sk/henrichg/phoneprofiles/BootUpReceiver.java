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
            PPApplication.startHandlerThread();
            final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    PPApplication.logE("BootUpReceiver.onReceive", "delayed boot up");
                    PPApplication.startedOnBoot = false;
                }
            }, 30000);

            PPApplication.logE("BootUpReceiver.onReceive", "applicationStartOnBoot=" + ApplicationPreferences.applicationStartOnBoot(context));

            //PPApplication.setApplicationStarted(context, false);

            if (ApplicationPreferences.applicationStartOnBoot(context)) {
                // start ReceiverService
                PPApplication.setApplicationStarted(context.getApplicationContext(), true);
                Intent serviceIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
                serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, true);
                serviceIntent.putExtra(PhoneProfilesService.EXTRA_STARTED_FROM_APP, true);
                serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_BOOT, true);
                PPApplication.startPPService(context, serviceIntent);
            }
        }
    }

}
