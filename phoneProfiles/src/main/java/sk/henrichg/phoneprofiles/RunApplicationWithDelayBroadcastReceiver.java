package sk.henrichg.phoneprofiles;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.SystemClock;

public class RunApplicationWithDelayBroadcastReceiver extends BroadcastReceiver {

    private static final String EXTRA_RUN_APPLICATION_DATA = "run_application_data";

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### RunApplicationWithDelayBroadcastReceiver.onReceive", "xxx");

        Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(appContext, true))
            // application is not started
            return;

        if (intent != null) {
            String runApplicationData = intent.getStringExtra(EXTRA_RUN_APPLICATION_DATA);

            Intent appIntent;
            PackageManager packageManager = context.getPackageManager();

            if (Application.isShortcut(runApplicationData)) {
                long shortcutId = Application.getShortcutId(runApplicationData);
                if (shortcutId > 0) {
                    Shortcut shortcut = DatabaseHandler.getInstance(context).getShortcut(shortcutId);
                    if (shortcut != null) {
                        try {
                            appIntent = Intent.parseUri(shortcut._intent, 0);
                            if (appIntent != null) {
                                appIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                try {
                                    context.startActivity(appIntent);
                                } catch (Exception ignored) {
                                }
                            }
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
            else
            if (Application.isIntent(runApplicationData)) {
                //TODO intent
                long intentId = Application.getIntentId(runApplicationData);
                if (intentId > 0) {
                    PPIntent ppIntent = DatabaseHandler.getInstance(context).getIntent(intentId);
                    if (ppIntent != null) {
                        appIntent = ApplicationEditorIntentActivity.createIntent(ppIntent);
                        if (appIntent != null) {
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            try {
                                context.startActivity(appIntent);
                            } catch (Exception ignored) {
                            }
                        }
                    }
                }
            } else {
                String packageName = Application.getPackageName(runApplicationData);
                appIntent = packageManager.getLaunchIntentForPackage(packageName);
                if (appIntent != null) {
                    appIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                    appIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    try {
                        context.startActivity(appIntent);
                    } catch (Exception ignored) {
                    }
                }
            }
        }
    }

    static void setDelayAlarm(Context context, int startApplicationDelay, String runApplicationData)
    {
        removeDelayAlarm(context);

        if (startApplicationDelay > 0)
        {
            long alarmTime = SystemClock.elapsedRealtime() + startApplicationDelay * 1000;

            Intent intent = new Intent(context, RunApplicationWithDelayBroadcastReceiver.class);
            intent.putExtra(EXTRA_RUN_APPLICATION_DATA, runApplicationData);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, intent, 0);

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                if (android.os.Build.VERSION.SDK_INT >= 23)
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, alarmTime, pendingIntent);
                else //if (android.os.Build.VERSION.SDK_INT >= 19)
                    alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, alarmTime, pendingIntent);
                //else
                //    alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, alarmTime, pendingIntent);
            }
        }
    }

    private static void removeDelayAlarm(Context context)
    {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            Intent intent = new Intent(context, RunApplicationWithDelayBroadcastReceiver.class);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, intent, PendingIntent.FLAG_NO_CREATE);
            if (pendingIntent != null) {
                PPApplication.logE("RunApplicationWithDelayBroadcastReceiver.removeDelayAlarm", "alarm found");

                alarmManager.cancel(pendingIntent);
                pendingIntent.cancel();
            }
        }
    }

}
