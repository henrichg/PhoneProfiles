package sk.henrichg.phoneprofiles;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

public class ExecuteVolumeProfilePrefsService extends IntentService //WakefulIntentService 
{

    public ExecuteVolumeProfilePrefsService() {
        super("ExecuteRadioProfilePrefsService");
    }

    //@Override
    //protected void doWakefulWork(Intent intent) {
    protected void onHandleIntent(Intent intent) {
        //Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        Context context = getApplicationContext();

        PPApplication.loadPreferences(context);

        PPApplication.setMergedRingNotificationVolumes(getApplicationContext(), false);

        DataWrapper dataWrapper = new DataWrapper(context, false, false, 0);
        ActivateProfileHelper aph = dataWrapper.getActivateProfileHelper();
        aph.initialize(dataWrapper, context);

        int linkUnlink;
        if (PPApplication.getMergedRingNotificationVolumes(context) && PPApplication.applicationUnlinkRingerNotificationVolumes)
            linkUnlink = intent.getIntExtra(PPApplication.EXTRA_LINKUNLINK_VOLUMES, PhoneCallService.LINKMODE_NONE);
        else
            linkUnlink = PhoneCallService.LINKMODE_NONE;
        long profile_id = intent.getLongExtra(PPApplication.EXTRA_PROFILE_ID, 0);
        Profile profile = dataWrapper.getProfileById(profile_id);
        profile = PPApplication.getMappedProfile(profile, context);

        boolean forProfileActivation = intent.getBooleanExtra(PPApplication.EXTRA_FOR_PROFILE_ACTIVATION, false);

        if (profile != null)
        {
            aph.setTones(profile);

            if (/*Permissions.checkProfileVolumePreferences(context, profile) &&*/
                Permissions.checkProfileAccessNotificationPolicy(context, profile)) {

                aph.changeRingerModeForVolumeEqual0(profile);
                aph.changeNotificationVolumeForVolumeEqual0(profile);

                RingerModeChangeReceiver.removeAlarm(this);
                RingerModeChangeReceiver.internalChange = true;

                final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

                aph.setRingerMode(profile, audioManager, true, forProfileActivation);
                aph.setVolumes(profile, audioManager, linkUnlink, forProfileActivation);
                aph.setRingerMode(profile, audioManager, false, forProfileActivation);

                //try { Thread.sleep(500); } catch (InterruptedException e) { }
                //SystemClock.sleep(500);
                PPApplication.sleep(500);

                RingerModeChangeReceiver.setAlarmForDisableInternalChange(context);

            }

            aph.setTones(profile);
        }
        dataWrapper.invalidateDataWrapper();
    }


}
