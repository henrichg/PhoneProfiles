package sk.henrichg.phoneprofiles;

import java.util.Calendar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.WindowManager;

public class RemoveBrightnessViewBroadcastReceiver extends BroadcastReceiver
{

	public void onReceive(Context context, Intent intent)
	{
		WindowManager windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
		if (GUIData.brightneesView != null)
		{
			windowManager.removeView(GUIData.brightneesView);
			GUIData.brightneesView = null;
		}
	}

	@SuppressLint("SimpleDateFormat")
	public static void setAlarm(Context context)
	{
		if (context != null)
		{
			removeAlarm(context);
	
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.SECOND, 1);
	        long alarmTime = calendar.getTimeInMillis(); 
			        		
	        AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
	 		Intent intent = new Intent(context, RemoveBrightnessViewBroadcastReceiver.class);
			
			PendingIntent alarmIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
	        alarmMgr.set(AlarmManager.RTC, alarmTime, alarmIntent);
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
