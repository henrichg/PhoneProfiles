package sk.henrichg.phoneprofiles;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.commonsware.cwac.wakeful.WakefulIntentService;

public class PackageReplacedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("PackageReplacedReceiver.onReceive", "intent="+intent);
        if ((intent != null) && intent.getAction().equals(Intent.ACTION_MY_PACKAGE_REPLACED)) {
            PackageReplacedJob.start();
        }
    }

}
