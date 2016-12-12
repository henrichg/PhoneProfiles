package sk.henrichg.phoneprofiles;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PackageReplacedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        //int intentUid = intent.getExtras().getInt("android.intent.extra.UID");
        //int myUid = android.os.Process.myUid();
        //if (intentUid == myUid)
        //{
            //GlobalData.loadPreferences(context);

            GlobalData.setShowRequestAccessNotificationPolicyPermission(context.getApplicationContext(), true);
            GlobalData.setShowRequestWriteSettingsPermission(context.getApplicationContext(), true);
            GlobalData.setScreenUnlocked(context.getApplicationContext(), true);

            if (GlobalData.getApplicationStarted(context, false))
            {
                // must by false for avoiding starts/pause events before restart events
                GlobalData.setApplicationStarted(context, false);

                // start ReceiverService
                context.startService(new Intent(context.getApplicationContext(), PhoneProfilesService.class));
            }

        //}
    }

}
