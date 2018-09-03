package sk.henrichg.phoneprofiles;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class LockDeviceActivityFinishBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### LockDeviceActivityFinishBroadcastReceiver.onReceive", "xxx");

        if (PhoneProfilesService.getInstance() != null) {
            if (PhoneProfilesService.getInstance().lockDeviceActivity != null) {
                PhoneProfilesService.getInstance().lockDeviceActivity.finish();
            }
        }
    }

    static void removeAlarm(Context context)
    {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            Intent intent = new Intent(context, LockDeviceActivityFinishBroadcastReceiver.class);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, intent, PendingIntent.FLAG_NO_CREATE);
            if (pendingIntent != null) {
                PPApplication.logE("LockDeviceActivityFinishBroadcastReceiver.removeAlarm", "alarm found");

                alarmManager.cancel(pendingIntent);
                pendingIntent.cancel();
            }
        }
    }

    @SuppressLint({"SimpleDateFormat", "NewApi"})
    static void setAlarm(Context context)
    {
        removeAlarm(context);

        int delay = 20; // 20 seconds

        long alarmTime = SystemClock.elapsedRealtime() + delay * 1000;

        if (PPApplication.logEnabled()) {
            SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
            String result = sdf.format(alarmTime);
            PPApplication.logE("LockDeviceActivityFinishBroadcastReceiver.setAlarm", "alarmTime=" + result);
        }

        Intent intent = new Intent(context, LockDeviceActivityFinishBroadcastReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            if (android.os.Build.VERSION.SDK_INT >= 23)
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, alarmTime, pendingIntent);
            else //if (android.os.Build.VERSION.SDK_INT >= 19)
                alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, alarmTime, pendingIntent);
            //else
            //    alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, alarmTime, pendingIntent);
        }
    }

}
