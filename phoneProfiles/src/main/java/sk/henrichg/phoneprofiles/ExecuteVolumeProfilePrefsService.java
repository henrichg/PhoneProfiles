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

        Context context = getApplicationContext();

        GlobalData.loadPreferences(context);

        DataWrapper dataWrapper = new DataWrapper(context, false, false, 0);
        ActivateProfileHelper aph = dataWrapper.getActivateProfileHelper();
        aph.initialize(null, context);

        int linkUnlink = intent.getIntExtra(GlobalData.EXTRA_LINKUNLINK_VOLUMES, PhoneCallService.LINKMODE_NONE);
        long profile_id = intent.getLongExtra(GlobalData.EXTRA_PROFILE_ID, 0);
        Profile profile = dataWrapper.getProfileById(profile_id);
        profile = GlobalData.getMappedProfile(profile, context);
        if (profile != null)
        {
            if (Permissions.checkProfileVolumePreferences(context, profile)) {

                RingerModeChangeReceiver.removeAlarm(this);
                RingerModeChangeReceiver.internalChange = true;

                final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

                aph.setRingerMode(profile, audioManager, true, linkUnlink);
                aph.setVolumes(profile, audioManager, linkUnlink);
                aph.setRingerMode(profile, audioManager, false, linkUnlink);

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    //System.out.println(e);
                }

                RingerModeChangeReceiver.setAlarmForDisableInternalChange(context);

            }
        }
        dataWrapper.invalidateDataWrapper();
        aph = null;
        dataWrapper = null;
    }


}
