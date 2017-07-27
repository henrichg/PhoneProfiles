package sk.henrichg.phoneprofiles;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.commonsware.cwac.wakeful.WakefulIntentService;

public class PackageReplacedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if ((intent != null) && intent.getAction().equals(Intent.ACTION_MY_PACKAGE_REPLACED)) {
            Intent serviceIntent = new Intent(context.getApplicationContext(), PackageReplacedService.class);
            WakefulIntentService.sendWakefulWork(context.getApplicationContext(), serviceIntent);
        }
    }

}
