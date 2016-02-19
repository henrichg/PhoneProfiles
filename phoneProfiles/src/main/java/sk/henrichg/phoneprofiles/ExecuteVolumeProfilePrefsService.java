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

                RingerModeChangeReceiver.internalChange = true;

                AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

                // set ringer mode to Ring for proper change ringer mode to Silent
                //aph.setRingerMode(profile, audioManager, true, linkUnlink);

                aph.setVolumes(profile, audioManager, linkUnlink);

                /*
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    //System.out.println(e);
                }
                */

                // set ringer mode to Ring for proper change ringer mode to Silent
                aph.setRingerMode(profile, audioManager, true, linkUnlink);

                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    //System.out.println(e);
                }

                // set ringer mode after volume because volumes change silent/vibrate
                aph.setRingerMode(profile, audioManager, false, linkUnlink);

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    //System.out.println(e);
                }

                RingerModeChangeReceiver.internalChange = false;

            }
        }
        dataWrapper.invalidateDataWrapper();
        aph = null;
        dataWrapper = null;
    }


}
