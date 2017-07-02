package sk.henrichg.phoneprofiles;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

public class BootUpReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        PPApplication.logE("BootUpReceiver.onReceive", "xxx");

        //PPApplication.loadPreferences(context);

        // start delayed bootup broadcast
        PPApplication.startedOnBoot = true;
        AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent delayedBootUpIntent = new Intent(context, DelayedBootUpReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, delayedBootUpIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, 10);
        alarmMgr.set(AlarmManager.RTC, calendar.getTimeInMillis(), alarmIntent);

        PPApplication.setApplicationStarted(context, false);

        if (ApplicationPreferences.applicationStartOnBoot(context))
        {
            // start ReceiverService
            context.startService(new Intent(context.getApplicationContext(), PhoneProfilesService.class));
        }

    }

}
