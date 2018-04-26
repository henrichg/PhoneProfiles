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

class AccessibilityServiceBroadcastReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        final Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(appContext, true))
            // application is not started
            return;

        if ((intent == null) || (intent.getAction() == null))
            return;

        PPApplication.logE("AccessibilityServiceBroadcastReceiver.onReceive", "action="+intent.getAction());

        if (intent.getAction().equals(PPApplication.ACTION_FORCE_STOP_APPLICATIONS_END)) {
            final long profileId = intent.getLongExtra(PPApplication.EXTRA_PROFILE_ID, 0);
            if (profileId != 0) {
                PPApplication.startHandlerThread();
                final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                        PowerManager.WakeLock wakeLock = null;
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "AccessibilityServiceBroadcastReceiver.onReceive");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        Profile profile = DatabaseHandler.getInstance(appContext).getProfile(profileId);
                        ActivateProfileHelper.executeForInteractivePreferences(profile, appContext);

                        if ((wakeLock != null) && wakeLock.isHeld())
                            wakeLock.release();
                    }
                });
            }
        }
    }

    static boolean isAccessibilityServiceEnabled(Context context) {
        AccessibilityManager manager = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (manager != null) {
            List<AccessibilityServiceInfo> runningServices =
                    manager.getEnabledAccessibilityServiceList(AccessibilityEvent.TYPES_ALL_MASK);

            for (AccessibilityServiceInfo service : runningServices) {
                if (service != null) {
                    PPApplication.logE("AccessibilityServiceBroadcastReceiver.isAccessibilityServiceEnabled", "serviceId=" + service.getId());
                    if (PPApplication.EXTENDER_ACCESSIBILITY_SERVICE_ID.equals(service.getId())) {
                        PPApplication.logE("AccessibilityServiceBroadcastReceiver.isAccessibilityServiceEnabled", "true");
                        return true;
                    }
                }
            }
            PPApplication.logE("AccessibilityServiceBroadcastReceiver.isAccessibilityServiceEnabled", "false");
            return false;
        }
        PPApplication.logE("AccessibilityServiceBroadcastReceiver.isAccessibilityServiceEnabled", "false");
        return false;
    }

    static int isExtenderInstalled(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo appInfo = packageManager.getApplicationInfo("sk.henrichg.phoneprofilesplusextender", 0);
            boolean installed = appInfo.enabled;
            if (installed) {
                PPApplication.logE("AccessibilityServiceBroadcastReceiver.isExtenderInstalled", "installed=true");
                PackageInfo pInfo = packageManager.getPackageInfo(appInfo.packageName, 0);
                int version = pInfo.versionCode;
                PPApplication.logE("AccessibilityServiceBroadcastReceiver.isExtenderInstalled", "version="+version);
                return version;
            }
            else {
                PPApplication.logE("AccessibilityServiceBroadcastReceiver.isExtenderInstalled", "installed=false");
                return 0;
            }
        }
        catch (Exception e) {
            PPApplication.logE("AccessibilityServiceBroadcastReceiver.isExtenderInstalled", "exception");
            return 0;
        }
    }

    static boolean isEnabled(Context context, int version) {
        int extenderVersion = isExtenderInstalled(context);
        boolean enabled = false;
        if (extenderVersion >= version)
            enabled = isAccessibilityServiceEnabled(context);
        return  (extenderVersion >= version) && enabled;
    }

}
