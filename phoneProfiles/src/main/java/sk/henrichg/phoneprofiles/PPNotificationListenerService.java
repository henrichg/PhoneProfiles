package sk.henrichg.phoneprofiles;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Build;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import androidx.core.app.NotificationManagerCompat;
import android.text.TextUtils;

import java.util.Set;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class PPNotificationListenerService extends NotificationListenerService {

    private static final String ACTION_REQUEST_INTERRUPTION_FILTER = "sk.henrichg.phoneprofilesplus.ACTION_REQUEST_INTERRUPTION_FILTER";
            //PPNotificationListenerService.class.getPackage().getName() + '.' + "ACTION_REQUEST_INTERRUPTION_FILTER";
    private static final String EXTRA_FILTER = "filter";

    private static final String TAG = PPNotificationListenerService.class.getSimpleName();

    private NLServiceReceiver nlservicereceiver;

    @Override
    public void onCreate() {
        super.onCreate();

        nlservicereceiver = new NLServiceReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_REQUEST_INTERRUPTION_FILTER);
        registerReceiver(nlservicereceiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceiver(nlservicereceiver);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
    }

    // Android 5.0 Lollipop

    @Override public void onListenerConnected() {
    }
    @Override public void onListenerHintsChanged(int hints) {
    }

    @Override
    public void onInterruptionFilterChanged(int interruptionFilter) {
        boolean a60 = (android.os.Build.VERSION.SDK_INT == 23) && Build.VERSION.RELEASE.equals("6.0");
        if (((android.os.Build.VERSION.SDK_INT >= 21) && (android.os.Build.VERSION.SDK_INT < 23)) || a60) {
            if (!RingerModeChangeReceiver.internalChange) {
                final AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);

                // convert to profile zenMode
                int zenMode = 0;
                switch (interruptionFilter) {
                    case NotificationListenerService.INTERRUPTION_FILTER_ALL:
                        if (ActivateProfileHelper.vibrationIsOn(audioManager, true))
                            zenMode = 4;
                        else
                            zenMode = 1;
                        break;
                    case NotificationListenerService.INTERRUPTION_FILTER_PRIORITY:
                        if (ActivateProfileHelper.vibrationIsOn(audioManager, true))
                            zenMode = 5;
                        else
                            zenMode = 2;
                        break;
                    case NotificationListenerService.INTERRUPTION_FILTER_NONE:
                        zenMode = 3;
                        break;
                    case NotificationListenerService.INTERRUPTION_FILTER_ALARMS: // new filter - Alarm only - Android M
                        zenMode = 6;
                        break;
                }
                if (zenMode != 0) {
                    RingerModeChangeReceiver.notUnlinkVolumes = true;
                    ActivateProfileHelper.setRingerMode(getApplicationContext(), 5);
                    ActivateProfileHelper.setZenMode(getApplicationContext(), zenMode);
                }
            }

            //RingerModeChangeReceiver.setAlarmForDisableInternalChange(getApplicationContext());
        }

    }

    private static int getZenMode(Context context, AudioManager audioManager) {
        // convert to profile zenMode
        int zenMode = 0;
        int systemZenMode = ActivateProfileHelper.getSystemZenMode(context/*, -1*/);
        PPApplication.logE(TAG, "getZenMode(" + systemZenMode + ')');
        switch (systemZenMode) {
            case ActivateProfileHelper.ZENMODE_ALL:
                if (ActivateProfileHelper.vibrationIsOn(audioManager, true))
                    zenMode = 4;
                else
                    zenMode = 1;
                break;
            case ActivateProfileHelper.ZENMODE_PRIORITY:
                if (ActivateProfileHelper.vibrationIsOn(audioManager, true))
                    zenMode = 5;
                else
                    zenMode = 2;
                break;
            case ActivateProfileHelper.ZENMODE_NONE:
                zenMode = 3;
                break;
            case ActivateProfileHelper.ZENMODE_ALARMS: // new filter - Alarm only - Android M
                zenMode = 6;
                break;
        }
        return zenMode;
    }

    public static void setZenMode(Context context, AudioManager audioManager) {
        boolean a60 = (android.os.Build.VERSION.SDK_INT == 23) && Build.VERSION.RELEASE.equals("6.0");
        if (((android.os.Build.VERSION.SDK_INT >= 21) && (android.os.Build.VERSION.SDK_INT < 23)) || a60) {
            int zenMode = getZenMode(context, audioManager);
            if (zenMode != 0) {
                ActivateProfileHelper.setRingerMode(context, 5);
                ActivateProfileHelper.setZenMode(context, zenMode);
            }
        }
    }

    public static boolean isNotificationListenerServiceEnabled(Context context) {
        /*
        ContentResolver contentResolver = context.getContentResolver();
        String enabledNotificationListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners");
        String className = PPNotificationListenerService.class.getName();
        // check to see if the enabledNotificationListeners String contains our package name
        if ((enabledNotificationListeners == null) || (!enabledNotificationListeners.contains(className)))
        {
            // in this situation we know that the user has not granted the app the Notification access permission
            return false;
        }
        else
        {
            return true;
        }
        */

        Set<String> packageNames = NotificationManagerCompat.getEnabledListenerPackages (context);
        //String className = PPNotificationListenerService.class.getName();
        String packageName = context.getPackageName();

        //if (packageNames != null) {
            for (String pkgName : packageNames) {
                //if (className.contains(pkgName)) {
                if (packageName.equals(pkgName)) {
                    return true;
                }
            }
            return false;
        //}
        //else
        //    return false;
    }

    private static Intent getInterruptionFilterRequestIntent(final int filter, final Context context) {
        Intent request = new Intent(ACTION_REQUEST_INTERRUPTION_FILTER);
        //request.setComponent(new ComponentName(context, PPNotificationListenerService.class));
        request.setPackage(context.getPackageName());
        request.putExtra(EXTRA_FILTER, filter);
        return request;
    }

    /** Convenience method for sending an {@link android.content.Intent} with {@link #ACTION_REQUEST_INTERRUPTION_FILTER}. */
    @SuppressLint("InlinedApi")
    public static void requestInterruptionFilter(final Context context, final int zenMode) {
        boolean a60 = (android.os.Build.VERSION.SDK_INT == 23) && Build.VERSION.RELEASE.equals("6.0");
        if (((android.os.Build.VERSION.SDK_INT >= 21) && (android.os.Build.VERSION.SDK_INT < 23)) || a60) {
            if (isNotificationListenerServiceEnabled(context)) {
                int interruptionFilter = NotificationListenerService.INTERRUPTION_FILTER_ALL;
                switch (zenMode) {
                    case ActivateProfileHelper.ZENMODE_ALL:
                        interruptionFilter = NotificationListenerService.INTERRUPTION_FILTER_ALL;
                        break;
                    case ActivateProfileHelper.ZENMODE_PRIORITY:
                        interruptionFilter = NotificationListenerService.INTERRUPTION_FILTER_PRIORITY;
                        break;
                    case ActivateProfileHelper.ZENMODE_NONE:
                        interruptionFilter = NotificationListenerService.INTERRUPTION_FILTER_NONE;
                        break;
                    case ActivateProfileHelper.ZENMODE_ALARMS:
                        interruptionFilter = NotificationListenerService.INTERRUPTION_FILTER_ALARMS;
                        break;
                }
                Intent request = getInterruptionFilterRequestIntent(interruptionFilter, context);
                context.sendBroadcast(request);
            }
        }
    }

    /*
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            // Handle being told to change the interruption filter (zen mode).
            if (!TextUtils.isEmpty(intent.getAction())) {
                if (ACTION_REQUEST_INTERRUPTION_FILTER.equals(intent.getAction())) {
                    if (intent.hasExtra(EXTRA_FILTER)) {
                        final int zenMode = intent.getIntExtra(EXTRA_FILTER, ActivateProfileHelper.ZENMODE_ALL);
                        switch (zenMode) {
                            case ActivateProfileHelper.ZENMODE_ALL:
                                requestInterruptionFilter(INTERRUPTION_FILTER_ALL);
                                break;
                            case ActivateProfileHelper.ZENMODE_PRIORITY:
                                requestInterruptionFilter(INTERRUPTION_FILTER_PRIORITY);
                                break;
                            case ActivateProfileHelper.ZENMODE_NONE:
                                requestInterruptionFilter(INTERRUPTION_FILTER_NONE);
                                break;
                            case ActivateProfileHelper.ZENMODE_ALARMS:
                                requestInterruptionFilter(INTERRUPTION_FILTER_ALARMS);
                                break;
                        }
                    }
                }
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }
    */

    class NLServiceReceiver extends BroadcastReceiver {

        @SuppressLint("InlinedApi")
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean a60 = (android.os.Build.VERSION.SDK_INT == 23) && Build.VERSION.RELEASE.equals("6.0");
            if (((android.os.Build.VERSION.SDK_INT >= 21) && (android.os.Build.VERSION.SDK_INT < 23)) || a60) {
                // Handle being told to change the interruption filter (zen mode).
                if (!TextUtils.isEmpty(intent.getAction())) {
                    if (ACTION_REQUEST_INTERRUPTION_FILTER.equals(intent.getAction())) {
                        if (intent.hasExtra(EXTRA_FILTER)) {
                            final int filter = intent.getIntExtra(EXTRA_FILTER, INTERRUPTION_FILTER_ALL);
                            switch (filter) {
                                case INTERRUPTION_FILTER_ALL:
                                case INTERRUPTION_FILTER_PRIORITY:
                                case INTERRUPTION_FILTER_NONE:
                                case INTERRUPTION_FILTER_ALARMS:
                                    try {
                                        if (isNotificationListenerServiceEnabled(context))
                                            requestInterruptionFilter(filter);
                                    } catch (SecurityException ignored) {
                                        // Fix disallowed call from unknown listener exception.
                                        // java.lang.SecurityException: Disallowed call from unknown listener
                                    }
                                    break;
                            }
                        }
                    }
                }
            }

        }
    }
}
