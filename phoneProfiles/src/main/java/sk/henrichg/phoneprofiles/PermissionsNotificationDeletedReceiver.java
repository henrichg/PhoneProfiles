package sk.henrichg.phoneprofiles;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PermissionsNotificationDeletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        PPApplication.logE("### PermissionsNotificationDeletedReceiver","xxx");

        Permissions.clearMergedPermissions(context.getApplicationContext());

    }

}
