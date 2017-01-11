package sk.henrichg.phoneprofiles;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.WindowManager;

import java.util.Calendar;

public class RemoveBrightnessViewBroadcastReceiver extends BroadcastReceiver
{

    public void onReceive(Context context, Intent intent)
    {
        //Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        WindowManager windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        if (GUIData.brightneesView != null)
        {
            try {
                windowManager.removeView(GUIData.brightneesView);
            } catch (Exception ignore) {
            }
            GUIData.brightneesView = null;
        }
    }

    @SuppressLint({"SimpleDateFormat", "NewApi"})
    public static void setAlarm(Context context)
    {
        if (context != null)
        {
            removeAlarm(context);

            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.SECOND, 3);
            long alarmTime = calendar.getTimeInMillis();

            AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(context, RemoveBrightnessViewBroadcastReceiver.class);

            PendingIntent alarmIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            // not needed exact for removing notification
            /*if (GlobalData.exactAlarms && (android.os.Build.VERSION.SDK_INT >= 23))
                alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime, alarmIntent);
            else
            if (GlobalData.exactAlarms && (android.os.Build.VERSION.SDK_INT >= 19))
                alarmMgr.setExact(AlarmManager.RTC_WAKEUP, alarmTime, alarmIntent);
            else*/
                alarmMgr.set(AlarmManager.RTC_WAKEUP, alarmTime, alarmIntent);
        }
    }

    public static void removeAlarm(Context context)
    {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);
        Intent intent = new Intent(context, RemoveBrightnessViewBroadcastReceiver.class);
        PendingIntent pendingIntent;
        pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, intent, PendingIntent.FLAG_NO_CREATE);
        if (pendingIntent != null)
        {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }

}
