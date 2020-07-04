package sk.henrichg.phoneprofiles;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import static android.app.Notification.DEFAULT_SOUND;
import static android.app.Notification.DEFAULT_VIBRATE;

public class ExportPPDataService extends Service {

    private Context context;

    private ExportPPDataStopButtonBroadcastReceiver exportPPDataStopButtonBroadcastReceiver = null;

    // this is for stop button in notification
    static final String ACTION_EXPORT_PP_DATA_STOP_BUTTON = PPApplication.PACKAGE_NAME + ".ExportPPDataService.ACTION_STOP_BUTTON";

    public ExportPPDataService() {
    }

    @Override
    public void onCreate()
    {
        super.onCreate();

        context = this;

        showNotification();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        if (exportPPDataStopButtonBroadcastReceiver == null) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ExportPPDataService.ACTION_EXPORT_PP_DATA_STOP_BUTTON);
            exportPPDataStopButtonBroadcastReceiver =
                    new ExportPPDataService.ExportPPDataStopButtonBroadcastReceiver();
            context.registerReceiver(exportPPDataStopButtonBroadcastReceiver, intentFilter);
        }

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        stopForeground(true);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        try {
            notificationManager.cancel(PPApplication.EXPORT_PP_DATA_NOTIFICATION_ID);
        } catch (Exception e) {
            PPApplication.recordException(e);
        }


        if (exportPPDataStopButtonBroadcastReceiver != null) {
            try {
                context.unregisterReceiver(exportPPDataStopButtonBroadcastReceiver);
            } catch (IllegalArgumentException e) {
                //PPApplication.recordException(e);
            }
            exportPPDataStopButtonBroadcastReceiver = null;
        }
    }

    private void showNotification() {
        String text;
        text = getString(R.string.export_pp_data_text);

        PPApplication.createExportPPDataNotificationChannel(this);
        NotificationCompat.Builder mBuilder =   new NotificationCompat.Builder(this, PPApplication.EXPORT_PP_DATA_CHANNEL)
                .setColor(ContextCompat.getColor(this, R.color.notificationDecorationColor))
                .setSmallIcon(R.drawable.ic_exclamation_notify) // notification icon
                .setContentTitle(getString(R.string.phone_profiles_export_pp_data_notification)) // title for notification
                .setContentText(text) // message for notification
                .setAutoCancel(true); // clear notification after click

        Intent stopPPExportIntent = new Intent(ACTION_EXPORT_PP_DATA_STOP_BUTTON);
        PendingIntent stopPPExportPendingIntent = PendingIntent.getBroadcast(context, 0, stopPPExportIntent, 0);
        mBuilder.addAction(R.drawable.ic_action_stop_white,
                context.getString(R.string.phone_profiles_export_pp_data_stop),
                stopPPExportPendingIntent);

        mBuilder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        //if (android.os.Build.VERSION.SDK_INT >= 21)
        //{
        mBuilder.setCategory(NotificationCompat.CATEGORY_RECOMMENDATION);
        mBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        //}

        Notification notification = mBuilder.build();
        notification.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
        notification.flags &= ~Notification.FLAG_SHOW_LIGHTS;
        notification.ledOnMS = 0;
        notification.ledOffMS = 0;
        notification.sound = null;
        notification.vibrate = null;
        notification.defaults &= ~DEFAULT_SOUND;
        notification.defaults &= ~DEFAULT_VIBRATE;
        startForeground(PPApplication.EXPORT_PP_DATA_NOTIFICATION_ID, notification);
    }


    public class ExportPPDataStopButtonBroadcastReceiver extends BroadcastReceiver {

        //final MobileCellsRegistrationDialogPreference preference;

        ExportPPDataStopButtonBroadcastReceiver() {
            //this.preference = preference;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            stopSelf();
        }
    }

}
