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

import java.util.List;

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
        stopSelf();
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
        List<Profile> profileList = DatabaseHandler.getInstance(context).getAllProfiles();
        Intent intent = new Intent(PPApplication.ACTION_EXPORT_PP_DATA_PROFILES_COUNT);
        intent.putExtra(PPApplication.EXTRA_PP_PROFILES_COUNT, profileList.size());
        context.sendBroadcast(intent, PPApplication.EXPORT_PP_DATA_PERMISSION);

        for (Profile profile : profileList) {
            PPProfileForExport profileForExport = new PPProfileForExport();
            profileForExport.KEY_ID = profile._id;
            profileForExport.KEY_NAME = profile._name;
            profileForExport.KEY_ICON = profile._icon;
            profileForExport.KEY_CHECKED = profile._checked;
            profileForExport.KEY_PORDER = profile._porder;
            profileForExport.KEY_VOLUME_RINGER_MODE = profile._volumeRingerMode;
            profileForExport.KEY_VOLUME_RINGTONE = profile._volumeRingtone;
            profileForExport.KEY_VOLUME_NOTIFICATION = profile._volumeNotification;
            profileForExport.KEY_VOLUME_MEDIA = profile._volumeMedia;
            profileForExport.KEY_VOLUME_ALARM = profile._volumeAlarm;
            profileForExport.KEY_VOLUME_SYSTEM = profile._volumeSystem;
            profileForExport.KEY_VOLUME_VOICE = profile._volumeVoice;
            profileForExport.KEY_SOUND_RINGTONE_CHANGE = profile._soundRingtoneChange;
            profileForExport.KEY_SOUND_RINGTONE = profile._soundRingtone;
            profileForExport.KEY_SOUND_NOTIFICATION_CHANGE = profile._soundNotificationChange;
            profileForExport.KEY_SOUND_NOTIFICATION = profile._soundNotification;
            profileForExport.KEY_SOUND_ALARM_CHANGE = profile._soundAlarmChange;
            profileForExport.KEY_SOUND_ALARM = profile._soundAlarm;
            profileForExport.KEY_DEVICE_AIRPLANE_MODE = profile._deviceAirplaneMode;
            profileForExport.KEY_DEVICE_WIFI = profile._deviceWiFi;
            profileForExport.KEY_DEVICE_BLUETOOTH = profile._deviceBluetooth;
            profileForExport.KEY_DEVICE_SCREEN_TIMEOUT = profile._deviceScreenTimeout;
            profileForExport.KEY_DEVICE_BRIGHTNESS = profile._deviceBrightness;
            profileForExport.KEY_DEVICE_WALLPAPER_CHANGE = profile._deviceWallpaperChange;
            profileForExport.KEY_DEVICE_WALLPAPER = profile._deviceWallpaper;
            profileForExport.KEY_DEVICE_MOBILE_DATA = profile._deviceMobileData;
            profileForExport.KEY_DEVICE_MOBILE_DATA_PREFS = profile._deviceMobileDataPrefs;
            profileForExport.KEY_DEVICE_GPS = profile._deviceGPS;
            profileForExport.KEY_DEVICE_RUN_APPLICATION_CHANGE = profile._deviceRunApplicationChange;
            profileForExport.KEY_DEVICE_RUN_APPLICATION_PACKAGE_NAME = profile._deviceRunApplicationPackageName;
            profileForExport.KEY_DEVICE_AUTOSYNC = profile._deviceAutoSync;
            profileForExport.KEY_DEVICE_AUTOROTATE = profile._deviceAutoRotate;
            profileForExport.KEY_DEVICE_LOCATION_SERVICE_PREFS = profile._deviceLocationServicePrefs;
            profileForExport.KEY_VOLUME_SPEAKER_PHONE = profile._volumeSpeakerPhone;
            profileForExport.KEY_DEVICE_NFC = profile._deviceNFC;
            profileForExport.KEY_DURATION = profile._duration;
            profileForExport.KEY_AFTER_DURATION_DO = profile._afterDurationDo;
            profileForExport.KEY_VOLUME_ZEN_MODE = profile._volumeZenMode;
            profileForExport.KEY_DEVICE_KEYGUARD = profile._deviceKeyguard;
            profileForExport.KEY_VIBRATE_ON_TOUCH = profile._vibrationOnTouch;
            profileForExport.KEY_DEVICE_WIFI_AP = profile._deviceWiFiAP;
            profileForExport.KEY_DEVICE_POWER_SAVE_MODE = profile._devicePowerSaveMode;
            profileForExport.KEY_ASK_FOR_DURATION = profile._askForDuration;
            profileForExport.KEY_DEVICE_NETWORK_TYPE = profile._deviceNetworkType;
            profileForExport.KEY_NOTIFICATION_LED = profile._notificationLed;
            profileForExport.KEY_VIBRATE_WHEN_RINGING = profile._vibrateWhenRinging;
            profileForExport.KEY_DEVICE_WALLPAPER_FOR = profile._deviceWallpaperFor;
            profileForExport.KEY_HIDE_STATUS_BAR_ICON = profile._hideStatusBarIcon;
            profileForExport.KEY_LOCK_DEVICE = profile._lockDevice;
            profileForExport.KEY_DEVICE_CONNECT_TO_SSID = profile._deviceConnectToSSID;
            profileForExport.KEY_DURATION_NOTIFICATION_SOUND = profile._durationNotificationSound;
            profileForExport.KEY_DURATION_NOTIFICATION_VIBRATE = profile._durationNotificationVibrate;
            profileForExport.KEY_DEVICE_WIFI_AP_PREFS = profile._deviceWiFiAPPrefs;
            profileForExport.KEY_HEADS_UP_NOTIFICATIONS = profile._headsUpNotifications;
            profileForExport.KEY_DEVICE_FORCE_STOP_APPLICATION_CHANGE = profile._deviceForceStopApplicationChange;
            profileForExport.KEY_DEVICE_FORCE_STOP_APPLICATION_PACKAGE_NAME = profile._deviceForceStopApplicationPackageName;
            profileForExport.KEY_ACTIVATION_BY_USER_COUNT = profile._activationByUserCount;
            profileForExport.KEY_DEVICE_NETWORK_TYPE_PREFS = profile._deviceNetworkTypePrefs;
            profileForExport.KEY_DEVICE_CLOSE_ALL_APPLICATIONS = profile._deviceCloseAllApplications;
            profileForExport.KEY_SCREEN_NIGHT_MODE = profile._screenNightMode;
            profileForExport.KEY_DTMF_TONE_WHEN_DIALING = profile._dtmfToneWhenDialing;
            profileForExport.KEY_SOUND_ON_TOUCH = profile._soundOnTouch;
            profileForExport.KEY_VOLUME_DTMF = profile._volumeDTMF;
            profileForExport.KEY_VOLUME_ACCESSIBILITY = profile._volumeAccessibility;
            profileForExport.KEY_VOLUME_BLUETOOTH_SCO = profile._volumeBluetoothSCO;

            intent = new Intent(PPApplication.ACTION_EXPORT_PP_DATA_PROFILES);
            intent.putExtra(PPApplication.EXTRA_PP_PROFILE_DATA, profileForExport);
            context.sendBroadcast(intent, PPApplication.EXPORT_PP_DATA_PERMISSION);
        }

    }

    private void exportShortcuts() {
        List<Shortcut> shortcutList = DatabaseHandler.getInstance(context).getAllShortcuts();
        Intent intent = new Intent(PPApplication.ACTION_EXPORT_PP_DATA_SHORTCUTS_COUNT);
        intent.putExtra(PPApplication.EXTRA_PP_SHORTCUTS_COUNT, shortcutList.size());
        context.sendBroadcast(intent, PPApplication.EXPORT_PP_DATA_PERMISSION);

        for (Shortcut shortcut : shortcutList) {
            PPShortcutForExport shortcutForExport = new PPShortcutForExport();
            shortcutForExport.KEY_S_ID = shortcut._id;
            shortcutForExport.KEY_S_INTENT = shortcut._intent;
            shortcutForExport.KEY_S_NAME = shortcut._name;

            intent = new Intent(PPApplication.ACTION_EXPORT_PP_DATA_SHORTCUTS);
            intent.putExtra(PPApplication.EXTRA_PP_SHORTCUT_DATA, shortcutForExport);
            context.sendBroadcast(intent, PPApplication.EXPORT_PP_DATA_PERMISSION);
        }
    }

    private void exportIntents() {
        List<PPIntent> intentList = DatabaseHandler.getInstance(context).getAllIntents();
        Intent intent = new Intent(PPApplication.ACTION_EXPORT_PP_DATA_INTENTS_COUNT);
        intent.putExtra(PPApplication.EXTRA_PP_INTENTS_COUNT, intentList.size());
        context.sendBroadcast(intent, PPApplication.EXPORT_PP_DATA_PERMISSION);

        for (PPIntent ppIntent : intentList) {
            PPIntentForExport intentForExport = new PPIntentForExport();
            intentForExport.KEY_IN_ID = ppIntent._id;
            intentForExport.KEY_IN_PACKAGE_NAME = ppIntent._packageName;
            intentForExport.KEY_IN_CLASS_NAME = ppIntent._className;
            intentForExport.KEY_IN_ACTION = ppIntent._action;
            intentForExport.KEY_IN_DATA = ppIntent._data;
            intentForExport.KEY_IN_MIME_TYPE = ppIntent._mimeType;
            intentForExport.KEY_IN_EXTRA_KEY_1 = ppIntent._extraKey1;
            intentForExport.KEY_IN_EXTRA_VALUE_1 = ppIntent._extraValue1;
            intentForExport.KEY_IN_EXTRA_TYPE_1 = ppIntent._extraType1;
            intentForExport.KEY_IN_EXTRA_KEY_2 = ppIntent._extraKey2;
            intentForExport.KEY_IN_EXTRA_VALUE_2 = ppIntent._extraValue2;
            intentForExport.KEY_IN_EXTRA_TYPE_2 = ppIntent._extraType2;
            intentForExport.KEY_IN_EXTRA_KEY_3 = ppIntent._extraKey3;
            intentForExport.KEY_IN_EXTRA_VALUE_3 = ppIntent._extraValue3;
            intentForExport.KEY_IN_EXTRA_TYPE_3 = ppIntent._extraType3;
            intentForExport.KEY_IN_EXTRA_KEY_4 = ppIntent._extraKey4;
            intentForExport.KEY_IN_EXTRA_VALUE_4 = ppIntent._extraValue4;
            intentForExport.KEY_IN_EXTRA_TYPE_4 = ppIntent._extraType4;
            intentForExport.KEY_IN_EXTRA_KEY_5 = ppIntent._extraKey5;
            intentForExport.KEY_IN_EXTRA_VALUE_5 = ppIntent._extraValue5;
            intentForExport.KEY_IN_EXTRA_TYPE_5 = ppIntent._extraType5;
            intentForExport.KEY_IN_EXTRA_KEY_6 = ppIntent._extraKey6;
            intentForExport.KEY_IN_EXTRA_VALUE_6 = ppIntent._extraValue6;
            intentForExport.KEY_IN_EXTRA_TYPE_6 = ppIntent._extraType6;
            intentForExport.KEY_IN_EXTRA_KEY_7 = ppIntent._extraKey7;
            intentForExport.KEY_IN_EXTRA_VALUE_7 = ppIntent._extraValue7;
            intentForExport.KEY_IN_EXTRA_TYPE_7 = ppIntent._extraType7;
            intentForExport.KEY_IN_EXTRA_KEY_8 = ppIntent._extraKey8;
            intentForExport.KEY_IN_EXTRA_VALUE_8 = ppIntent._extraValue8;
            intentForExport.KEY_IN_EXTRA_TYPE_8 = ppIntent._extraType8;
            intentForExport.KEY_IN_EXTRA_KEY_9 = ppIntent._extraKey9;
            intentForExport.KEY_IN_EXTRA_VALUE_9 = ppIntent._extraValue9;
            intentForExport.KEY_IN_EXTRA_TYPE_9 = ppIntent._extraType9;
            intentForExport.KEY_IN_EXTRA_KEY_10 = ppIntent._extraKey10;
            intentForExport.KEY_IN_EXTRA_VALUE_10 = ppIntent._extraValue10;
            intentForExport.KEY_IN_EXTRA_TYPE_10 = ppIntent._extraType10;
            intentForExport.KEY_IN_CATEGORIES = ppIntent._categories;
            intentForExport.KEY_IN_FLAGS = ppIntent._flags;
            intentForExport.KEY_IN_NAME = ppIntent._name;
            intentForExport.KEY_IN_USED_COUNT = ppIntent._usedCount;
            intentForExport.KEY_IN_INTENT_TYPE = ppIntent._intentType;

            intent = new Intent(PPApplication.ACTION_EXPORT_PP_DATA_INTENTS);
            intent.putExtra(PPApplication.EXTRA_PP_INTENT_DATA, intentForExport);
            context.sendBroadcast(intent, PPApplication.EXPORT_PP_DATA_PERMISSION);
        }
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
