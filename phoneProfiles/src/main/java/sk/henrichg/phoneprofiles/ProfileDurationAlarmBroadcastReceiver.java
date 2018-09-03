package sk.henrichg.phoneprofiles;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;

import java.util.Calendar;

import static android.content.Context.POWER_SERVICE;

public class ProfileDurationAlarmBroadcastReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        if (PPApplication.getApplicationStarted(context, false)) {
            //ProfileDurationJob.start(context.getApplicationContext(), intent.getLongExtra(PPApplication.EXTRA_PROFILE_ID, 0));
            final Context appContext = context.getApplicationContext();
            final long profileId = intent.getLongExtra(PPApplication.EXTRA_PROFILE_ID, 0);
            PPApplication.startHandlerThread();
            final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (profileId != 0) {

                        PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                        PowerManager.WakeLock wakeLock = null;
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "ProfileDurationAlarmBroadcastReceiver.onReceive");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        DataWrapper dataWrapper = new DataWrapper(appContext, false, 0);

                        Profile profile = dataWrapper.getProfileById(profileId, false, false);
                        Profile activatedProfile = dataWrapper.getActivatedProfile(false, false);

                        if ((profile != null) && (activatedProfile != null) &&
                                (activatedProfile._id == profile._id) &&
                                (profile._afterDurationDo != Profile.AFTERDURATIONDO_NOTHING))
                        {
                            // alarm is from activated profile

                            if (!profile._durationNotificationSound.isEmpty() || profile._durationNotificationVibrate) {
                                if (PhoneProfilesService.getInstance() != null) {
                                    PPApplication.logE("##### ProfileDurationAlarmBroadcastReceiver.onReceive", "play notification");
                                    PhoneProfilesService.getInstance().playNotificationSound(profile._durationNotificationSound, profile._durationNotificationVibrate);
                                    //PPApplication.sleep(500);
                                }
                            }

                            long activateProfileId = 0;
                            if (profile._afterDurationDo == Profile.AFTERDURATIONDO_BACKGROUNPROFILE)
                            {
                                activateProfileId = Long.valueOf(ApplicationPreferences.applicationBackgroundProfile(appContext));
                                if (activateProfileId == Profile.PROFILE_NO_ACTIVATE)
                                    activateProfileId = 0;
                            }
                            if (profile._afterDurationDo == Profile.AFTERDURATIONDO_UNDOPROFILE)
                            {
                                activateProfileId = Profile.getActivatedProfileForDuration(appContext);
                            }

                            dataWrapper.activateProfileAfterDuration(activateProfileId);
                        }

                        dataWrapper.invalidateDataWrapper();

                        if ((wakeLock != null) && wakeLock.isHeld()) {
                            try {
                                wakeLock.release();
                            } catch (Exception ignored) {}
                        }
                    }
                }
            });
        }
    }

    @SuppressLint({"SimpleDateFormat", "NewApi"})
    static public void setAlarm(Profile profile, Context context)
    {
        removeAlarm(context);

        if (profile == null)
            return;

        if ((profile._afterDurationDo != Profile.AFTERDURATIONDO_NOTHING) &&
            (profile._duration > 0))
        {
            // duration for start is > 0
            // set alarm

            Calendar now = Calendar.getInstance();
            now.add(Calendar.SECOND, profile._duration);
            long alarmTime = now.getTimeInMillis();// + 1000 * 60 * profile._duration;

            Profile.setActivatedProfileEndDurationTime(context, alarmTime);

            //SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
            //String result = sdf.format(alarmTime);

            Intent intent = new Intent(context, ProfileDurationAlarmBroadcastReceiver.class);
            intent.putExtra(PPApplication.EXTRA_PROFILE_ID, profile._id);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), (int) profile._id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            if (alarmManager != null) {
                alarmTime = SystemClock.elapsedRealtime() + profile._duration * 1000;
                if (android.os.Build.VERSION.SDK_INT >= 23)
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, alarmTime, pendingIntent);
                else //if (PPApplication.exactAlarms && (android.os.Build.VERSION.SDK_INT >= 19))
                    alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, alarmTime, pendingIntent);
                //else
                //    alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, alarmTime, pendingIntent);

                //this._isInDelay = true;
            }
        }
        //else
        //	this._isInDelay = false;

        //dataWrapper.getDatabaseHandler().updateEventInDelay(this);
    }

    static public void removeAlarm(Context context)
    {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            Intent intent = new Intent(context, ProfileDurationAlarmBroadcastReceiver.class);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, intent, PendingIntent.FLAG_NO_CREATE);
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent);
                pendingIntent.cancel();
            }

            //this._isInDelay = false;
            //dataWrapper.getDatabaseHandler().updateEventInDelay(this);
        }
        Profile.setActivatedProfileEndDurationTime(context, 0);
    }

}
