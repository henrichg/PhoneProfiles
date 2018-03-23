package sk.henrichg.phoneprofiles;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.os.Handler;
import android.os.PowerManager;

import static android.content.Context.POWER_SERVICE;

public class PackageReplacedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("PackageReplacedReceiver.onReceive", "intent="+intent);
        if ((intent != null) && (intent.getAction() != null) && intent.getAction().equals(Intent.ACTION_MY_PACKAGE_REPLACED)) {

            // start delayed boot up broadcast
            PPApplication.startedOnBoot = true;
            PPApplication.startHandlerThread();
            final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    PPApplication.logE("PackageReplacedReceiver.onReceive", "delayed boot up");
                    PPApplication.startedOnBoot = false;
                }
            }, 30000);

            final Context appContext = context.getApplicationContext();
            if (PPApplication.getApplicationStarted(appContext, false))
            {
                if (PhoneProfilesService.instance != null) {
                    // stop PhoneProfilesService
                    appContext.stopService(new Intent(appContext, PhoneProfilesService.class));
                    PPApplication.sleep(2000);
                    startService(appContext);
                }
                else
                    startService(appContext);
            }
        }
    }

    private void startService(Context context) {
        // must by false for avoiding starts/pause events before restart events
        PPApplication.setApplicationStarted(context, false);

        // start PhoneProfilesService
        Intent serviceIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
        serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, true);
        serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_PACKAGE_REPLACE, true);
        PPApplication.startPPService(context, serviceIntent);
    }

}
