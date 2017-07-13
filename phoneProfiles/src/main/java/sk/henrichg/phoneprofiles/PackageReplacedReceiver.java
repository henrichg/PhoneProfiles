package sk.henrichg.phoneprofiles;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;

import com.commonsware.cwac.wakeful.WakefulIntentService;

import java.util.Calendar;

public class PackageReplacedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //int intentUid = intent.getExtras().getInt("android.intent.extra.UID");
        //int myUid = android.os.Process.myUid();
        //if (intentUid == myUid)
        //{
        Intent serviceIntent = new Intent(context.getApplicationContext(), PackageReplacedService.class);
        WakefulIntentService.sendWakefulWork(context.getApplicationContext(), serviceIntent);
        //}
    }

}
