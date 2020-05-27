package sk.henrichg.phoneprofiles;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.PowerManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;

import java.util.List;

import static android.content.Context.POWER_SERVICE;

class PPPExtenderBroadcastReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        //PPApplication.logE("[***] PPPExtenderBroadcastReceiver.onReceive", "xxx");

        final Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(appContext, true))
            // application is not started
            return;

        if ((intent == null) || (intent.getAction() == null))
            return;

        //PPApplication.logE("[***] PPPExtenderBroadcastReceiver.onReceive", "action="+intent.getAction());

        switch (intent.getAction()) {
            case PPApplication.ACTION_ACCESSIBILITY_SERVICE_CONNECTED:
                Intent _intent = new Intent(PPApplication.ACTION_REGISTER_PPPE_FUNCTION);
                _intent.putExtra(PPApplication.EXTRA_REGISTRATION_APP, "PhoneProfiles");
                _intent.putExtra(PPApplication.EXTRA_REGISTRATION_TYPE, PPApplication.REGISTRATION_TYPE_FORCE_STOP_APPLICATIONS_REGISTER);
                context.sendBroadcast(_intent, PPApplication.ACCESSIBILITY_SERVICE_PERMISSION);
                break;
            case PPApplication.ACTION_FORCE_STOP_APPLICATIONS_END:
                final long profileId = intent.getLongExtra(PPApplication.EXTRA_PROFILE_ID, 0);
                //PPApplication.logE("[***] PPPExtenderBroadcastReceiver,onReceive", "profileId="+profileId);
                if (profileId != 0) {
                    PPApplication.startHandlerThread();
                    final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                            PowerManager.WakeLock wakeLock = null;
                            try {
                                if (powerManager != null) {
                                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":PPPExtenderBroadcastReceiver.onReceive");
                                    wakeLock.acquire(10 * 60 * 1000);
                                }

                                Profile profile = DatabaseHandler.getInstance(appContext).getProfile(profileId);
                                if (profile != null)
                                    ActivateProfileHelper.executeForInteractivePreferences(profile, appContext);
                            } finally {
                                if ((wakeLock != null) && wakeLock.isHeld()) {
                                    try {
                                        wakeLock.release();
                                    } catch (Exception ignored) {}
                                }
                            }
                        }
                    });
                }
                break;
        }
    }

    static boolean isAccessibilityServiceEnabled(Context context) {
        AccessibilityManager manager = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (manager != null) {
            List<AccessibilityServiceInfo> runningServices =
                    manager.getEnabledAccessibilityServiceList(AccessibilityEvent.TYPES_ALL_MASK);

            for (AccessibilityServiceInfo service : runningServices) {
                if (service != null) {
                    //PPApplication.logE("PPPExtenderBroadcastReceiver.isAccessibilityServiceEnabled", "serviceId=" + service.getId());
                    if (PPApplication.EXTENDER_ACCESSIBILITY_SERVICE_ID.equals(service.getId())) {
                        //PPApplication.logE("PPPExtenderBroadcastReceiver.isAccessibilityServiceEnabled", "true");
                        return true;
                    }
                }
            }
            //PPApplication.logE("PPPExtenderBroadcastReceiver.isAccessibilityServiceEnabled", "false");
            return false;
        }
        //PPApplication.logE("PPPExtenderBroadcastReceiver.isAccessibilityServiceEnabled", "false");
        return false;
    }

    static int isExtenderInstalled(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo appInfo = packageManager.getApplicationInfo(PPApplication.PACKAGE_NAME_EXTENDER, 0);
            boolean installed = appInfo.enabled;
            if (installed) {
                //PPApplication.logE("PPPExtenderBroadcastReceiver.isExtenderInstalled", "installed=true");
                PackageInfo pInfo = packageManager.getPackageInfo(appInfo.packageName, 0);
                //noinspection UnnecessaryLocalVariable
                int version = PPApplication.getVersionCode(pInfo);
                //PPApplication.logE("PPPExtenderBroadcastReceiver.isExtenderInstalled", "version="+version);
                return version;
            }
            else {
                //PPApplication.logE("PPPExtenderBroadcastReceiver.isExtenderInstalled", "installed=false");
                return 0;
            }
        }
        catch (Exception e) {
            //PPApplication.logE("PPPExtenderBroadcastReceiver.isExtenderInstalled", "exception");
            return 0;
        }
    }

    static boolean isEnabled(Context context,
                             @SuppressWarnings("SameParameterValue") int version) {
        int extenderVersion = isExtenderInstalled(context);
        boolean enabled = false;
        if (extenderVersion >= version)
            enabled = isAccessibilityServiceEnabled(context);
        return  (extenderVersion >= version) && enabled;
    }

}
