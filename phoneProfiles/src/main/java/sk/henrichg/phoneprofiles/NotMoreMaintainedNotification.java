package sk.henrichg.phoneprofiles;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

class NotMoreMaintainedNotification {

    private static final String PREF_SHOW_NOT_MORE_MAINTAINED_NOTIFICATION_ON_START = "show_not_more_maintained_notification_on_start";

    static void showNotification(Context context) {
        final Context appContext = context.getApplicationContext();

        //PPApplication.logE("IgnoreBatteryOptimizationNotification.showNotification", "pm="+pm);

        if (getMotMoreMaintainedNotificationOnStart(appContext))
            showNotification(appContext,
                    appContext.getString(R.string.not_more_maintained_notification_title),
                    appContext.getString(R.string.not_more_maintained_notification_text));
    }

    static private void showNotification(Context context, String title, String text) {
        String nTitle = title;
        String nText = text;
        if (Build.VERSION.SDK_INT < 24) {
            nTitle = context.getString(R.string.pp_app_name);
            nText = title+": "+text;
        }
        PPApplication.createExclamationNotificationChannel(context);
        NotificationCompat.Builder mBuilder =   new NotificationCompat.Builder(context, PPApplication.EXCLAMATION_NOTIFICATION_CHANNEL)
                .setColor(ContextCompat.getColor(context, R.color.notificationDecorationColor))
                .setSmallIcon(R.drawable.ic_exclamation_notify) // notification icon
                .setContentTitle(nTitle) // title for notification
                .setContentText(nText) // message for notification
                .setAutoCancel(true); // clear notification after click
        mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(nText));
        //Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
        //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //PendingIntent pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        //mBuilder.setContentIntent(pi);
        mBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
        //if (android.os.Build.VERSION.SDK_INT >= 21)
        //{
            mBuilder.setCategory(NotificationCompat.CATEGORY_RECOMMENDATION);
            mBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        //}
        mBuilder.setOnlyAlertOnce(true);

        Intent disableIntent = new Intent(context, NotMoreMaintainedDisableActivity.class);
        PendingIntent pDisableIntent = PendingIntent.getActivity(context, 0, disableIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Action.Builder actionBuilder = new NotificationCompat.Action.Builder(
                R.drawable.ic_action_exit_app_white,
                context.getString(R.string.not_more_maintained_notification_disable_button),
                pDisableIntent);
        mBuilder.addAction(actionBuilder.build());

        NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(context);
        try {
            mNotificationManager.notify(PPApplication.NOT_MORE_MAINTAINED_NOTIFICATION_ID, mBuilder.build());
        } catch (Exception e) {
            //Log.e("IgnoreBatteryOptimizationNotification.showNotification", Log.getStackTraceString(e));
            PPApplication.recordException(e);
        }
    }

    static void removeNotification(Context context)
    {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        try {
            notificationManager.cancel(PPApplication.NOT_MORE_MAINTAINED_NOTIFICATION_ID);
        } catch (Exception e) {
            PPApplication.recordException(e);
        }
    }

    static boolean getMotMoreMaintainedNotificationOnStart(Context context)
    {
        return ApplicationPreferences.
                getSharedPreferences(context).getBoolean(PREF_SHOW_NOT_MORE_MAINTAINED_NOTIFICATION_ON_START, true);
    }

    static void setNotMoreMaintainedNotificationOnStart(Context context, boolean show)
    {
        SharedPreferences.Editor editor = ApplicationPreferences.getSharedPreferences(context).edit();
        editor.putBoolean(PREF_SHOW_NOT_MORE_MAINTAINED_NOTIFICATION_ON_START, show);
        editor.apply();
    }
}
