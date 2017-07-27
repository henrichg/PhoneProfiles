package sk.henrichg.phoneprofiles;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

// Delete button (X) or "clear all" in notification
public class PermissionsNotificationDeletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("### PermissionsNotificationDeletedReceiver","xxx");

        Permissions.clearMergedPermissions(context.getApplicationContext());

    }

}
