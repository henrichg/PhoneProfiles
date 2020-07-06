package sk.henrichg.phoneprofiles;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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

        if ((intent != null) && (intent.getAction() != null)) {
            switch (intent.getAction()) {
                case PPApplication.ACTION_EXPORT_PP_DATA_START:
                    startOfExport();

                    exportApplicationData();
                    exportProfiles();
                    exportShortcuts();
                    exportIntents();

                    endOfExport();
                    break;
            }
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

    private void startOfExport() {
        Intent intent = new Intent(PPApplication.ACTION_EXPORT_PP_DATA_STARTED);
        context.sendBroadcast(intent, PPApplication.EXPORT_PP_DATA_PERMISSION);
    }

    private void endOfExport() {
        Intent intent = new Intent(PPApplication.ACTION_EXPORT_PP_DATA_ENDED);
        context.sendBroadcast(intent, PPApplication.EXPORT_PP_DATA_PERMISSION);
    }

    private void exportApplicationData() {
        PPApplicationDataForExport applicationData = new PPApplicationDataForExport();
        applicationData.applicationStartOnBoot = ApplicationPreferences.applicationStartOnBoot(context);
        applicationData.applicationActivate = ApplicationPreferences.applicationActivate(context);
        applicationData.applicationActivateWithAlert = ApplicationPreferences.applicationActivateWithAlert(context);
        applicationData.applicationClose = ApplicationPreferences.applicationClose(context);
        applicationData.applicationLongClickActivation = ApplicationPreferences.applicationLongClickActivation(context);
        applicationData.applicationLanguage = ApplicationPreferences.applicationLanguage(context);
        applicationData.applicationTheme = ApplicationPreferences.getSharedPreferences(context).getString(ApplicationPreferences.PREF_APPLICATION_THEME, "white");
        applicationData.applicationActivatorPrefIndicator = ApplicationPreferences.applicationActivatorPrefIndicator(context);
        applicationData.applicationEditorPrefIndicator = ApplicationPreferences.applicationEditorPrefIndicator(context);
        applicationData.applicationActivatorHeader = ApplicationPreferences.applicationActivatorHeader(context);
        applicationData.applicationEditorHeader = ApplicationPreferences.applicationEditorHeader(context);
        applicationData.notificationsToast = ApplicationPreferences.notificationsToast(context);
        applicationData.notificationStatusBar = ApplicationPreferences.notificationStatusBar(context);
        applicationData.notificationStatusBarPermanent = ApplicationPreferences.notificationStatusBarPermanent(context);
        applicationData.notificationStatusBarCancel = ApplicationPreferences.notificationStatusBarCancel(context);
        applicationData.notificationStatusBarStyle = ApplicationPreferences.notificationStatusBarStyle(context);
        applicationData.notificationShowInStatusBar = ApplicationPreferences.notificationShowInStatusBar(context);
        applicationData.notificationTextColor = ApplicationPreferences.notificationTextColor(context);
        applicationData.notificationHideInLockscreen = ApplicationPreferences.notificationHideInLockScreen(context);
        applicationData.applicationWidgetListPrefIndicator = ApplicationPreferences.applicationWidgetListPrefIndicator(context);
        applicationData.applicationWidgetListHeader = ApplicationPreferences.applicationWidgetListHeader(context);
        applicationData.applicationWidgetListBackground = ApplicationPreferences.applicationWidgetListBackground(context);
        applicationData.applicationWidgetListLightnessB = ApplicationPreferences.applicationWidgetListLightnessB(context);
        applicationData.applicationWidgetListLightnessT = ApplicationPreferences.applicationWidgetListLightnessT(context);
        applicationData.applicationWidgetIconColor = ApplicationPreferences.applicationWidgetIconColor(context);
        applicationData.applicationWidgetIconLightness = ApplicationPreferences.applicationWidgetIconLightness(context);
        applicationData.applicationWidgetListIconColor = ApplicationPreferences.applicationWidgetListIconColor(context);
        applicationData.applicationWidgetListIconLightness = ApplicationPreferences.applicationWidgetListIconLightness(context);
        applicationData.notificationPrefIndicator = ApplicationPreferences.notificationPrefIndicator(context);
        applicationData.applicationBackgroundProfile = ApplicationPreferences.applicationBackgroundProfile(context);
        applicationData.applicationActivatorGridLayout = ApplicationPreferences.applicationActivatorGridLayout(context);
        applicationData.applicationWidgetListGridLayout = ApplicationPreferences.applicationWidgetListGridLayout(context);
        applicationData.applicationWidgetIconHideProfileName = ApplicationPreferences.applicationWidgetIconHideProfileName(context);
        applicationData.applicationShortcutEmblem = ApplicationPreferences.applicationShortcutEmblem(context);
        applicationData.applicationWidgetIconBackground = ApplicationPreferences.applicationWidgetIconBackground(context);
        applicationData.applicationWidgetIconLightnessB = ApplicationPreferences.applicationWidgetIconLightnessB(context);
        applicationData.applicationWidgetIconLightnessT = ApplicationPreferences.applicationWidgetIconLightnessT(context);
        applicationData.applicationUnlinkRingerNotificationVolumes = ApplicationPreferences.applicationUnlinkRingerNotificationVolumes(context);
        applicationData.applicationForceSetMergeRingNotificationVolumes = ApplicationPreferences.applicationForceSetMergeRingNotificationVolumes(context);
        applicationData.applicationSamsungEdgeHeader = ApplicationPreferences.applicationSamsungEdgeHeader(context);
        applicationData.applicationSamsungEdgeBackground = ApplicationPreferences.applicationSamsungEdgeBackground(context);
        applicationData.applicationSamsungEdgeLightnessB = ApplicationPreferences.applicationSamsungEdgeLightnessB(context);
        applicationData.applicationSamsungEdgeLightnessT = ApplicationPreferences.applicationSamsungEdgeLightnessT(context);
        applicationData.applicationSamsungEdgeIconColor = ApplicationPreferences.applicationSamsungEdgeIconColor(context);
        applicationData.applicationSamsungEdgeIconLightness = ApplicationPreferences.applicationSamsungEdgeIconLightness(context);
        applicationData.applicationWidgetListRoundedCorners = ApplicationPreferences.applicationWidgetListRoundedCorners(context);
        applicationData.applicationWidgetIconRoundedCorners = ApplicationPreferences.applicationWidgetIconRoundedCorners(context);
        applicationData.applicationWidgetListBackgroundType = ApplicationPreferences.applicationWidgetListBackgroundType(context);
        applicationData.applicationWidgetListBackgroundColor = ApplicationPreferences.applicationWidgetListBackgroundColor(context);
        applicationData.applicationWidgetIconBackgroundType = ApplicationPreferences.applicationWidgetIconBackgroundType(context);
        applicationData.applicationWidgetIconBackgroundColor = ApplicationPreferences.applicationWidgetIconBackgroundColor(context);
        applicationData.applicationSamsungEdgeBackgroundType = ApplicationPreferences.applicationSamsungEdgeBackgroundType(context);
        applicationData.applicationSamsungEdgeBackgroundColor = ApplicationPreferences.applicationSamsungEdgeBackgroundColor(context);
        applicationData.applicationNeverAskForGrantRoot = ApplicationPreferences.applicationNeverAskForGrantRoot(context);
        applicationData.notificationShowButtonExit = ApplicationPreferences.notificationShowButtonExit(context);
        applicationData.applicationWidgetOneRowPrefIndicator = ApplicationPreferences.applicationWidgetOneRowPrefIndicator(context);
        applicationData.applicationWidgetOneRowBackground = ApplicationPreferences.applicationWidgetOneRowBackground(context);
        applicationData.applicationWidgetOneRowLightnessB = ApplicationPreferences.applicationWidgetOneRowLightnessB(context);
        applicationData.applicationWidgetOneRowLightnessT = ApplicationPreferences.applicationWidgetOneRowLightnessT(context);
        applicationData.applicationWidgetOneRowIconColor = ApplicationPreferences.applicationWidgetOneRowIconColor(context);
        applicationData.applicationWidgetOneRowIconLightness = ApplicationPreferences.applicationWidgetOneRowIconLightness(context);
        applicationData.applicationWidgetOneRowRoundedCorners = ApplicationPreferences.applicationWidgetOneRowRoundedCorners(context);
        applicationData.applicationWidgetOneRowBackgroundType = ApplicationPreferences.applicationWidgetOneRowBackgroundType(context);
        applicationData.applicationWidgetOneRowBackgroundColor = ApplicationPreferences.applicationWidgetOneRowBackgroundColor(context);
        applicationData.applicationWidgetListLightnessBorder = ApplicationPreferences.applicationWidgetListLightnessBorder(context);
        applicationData.applicationWidgetOneRowLightnessBorder = ApplicationPreferences.applicationWidgetOneRowLightnessBorder(context);
        applicationData.applicationWidgetIconLightnessBorder = ApplicationPreferences.applicationWidgetIconLightnessBorder(context);
        applicationData.applicationWidgetListShowBorder = ApplicationPreferences.applicationWidgetListShowBorder(context);
        applicationData.applicationWidgetOneRowShowBorder = ApplicationPreferences.applicationWidgetOneRowShowBorder(context);
        applicationData.applicationWidgetIconShowBorder = ApplicationPreferences.applicationWidgetIconShowBorder(context);
        applicationData.applicationWidgetListCustomIconLightness = ApplicationPreferences.applicationWidgetListCustomIconLightness(context);
        applicationData.applicationWidgetOneRowCustomIconLightness = ApplicationPreferences.applicationWidgetOneRowCustomIconLightness(context);
        applicationData.applicationWidgetIconCustomIconLightness = ApplicationPreferences.applicationWidgetIconCustomIconLightness(context);
        applicationData.applicationSamsungEdgeCustomIconLightness = ApplicationPreferences.applicationSamsungEdgeCustomIconLightness(context);
        applicationData.notificationUseDecoration = ApplicationPreferences.notificationUseDecoration(context);
        applicationData.notificationLayoutType = ApplicationPreferences.notificationLayoutType(context);
        applicationData.notificationBackgroundColor = ApplicationPreferences.notificationBackgroundColor(context);

        Intent intent = new Intent(PPApplication.ACTION_EXPORT_PP_DATA_APPLICATION_PREFERENCES);
        intent.putExtra(PPApplication.EXTRA_PP_APPLICATION_DATA, applicationData);
        context.sendBroadcast(intent, PPApplication.EXPORT_PP_DATA_PERMISSION);
    }

    private void exportProfiles() {

    }

    private void exportShortcuts() {

    }

    private void exportIntents() {

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
