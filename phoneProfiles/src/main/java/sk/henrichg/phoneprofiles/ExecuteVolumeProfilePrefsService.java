package sk.henrichg.phoneprofiles;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;

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
		
		long profile_id = intent.getLongExtra(GlobalData.EXTRA_PROFILE_ID, 0);
		Profile profile = dataWrapper.getProfileById(profile_id);
		profile = GlobalData.getMappedProfile(profile, context);
		if (profile != null)
		{
			AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);

            // set ringer mode to Ring for proper change ringer mode to Silent
            Settings.System.putInt(context.getContentResolver(), "notifications_use_ring_volume", 0);
            if (profile._volumeRingerMode == 4) {
                int ringerMode = profile._volumeRingerMode;
                profile._volumeRingerMode = 1;
                aph.setRingerMode(profile, audioManager);
                profile._volumeRingerMode = ringerMode;
            }

			/*
			TelephonyManager telephony = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephony.getCallState() != TelephonyManager.CALL_STATE_RINGING) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    //System.out.println(e);
                }
            }
            */

            Settings.System.putInt(context.getContentResolver(), "notifications_use_ring_volume", 0);
            aph.setVolumes(profile, audioManager);

            // set ringer mode after volume because volumes change silent/vibrate
            Settings.System.putInt(context.getContentResolver(), "notifications_use_ring_volume", 0);
            aph.setRingerMode(profile, audioManager);

		/*	if (intent.getBooleanExtra(GlobalData.EXTRA_SECOND_SET_VOLUMES, false))
			{
				// run service for execute volumes - second set
				Intent volumeServiceIntent = new Intent(context, ExecuteVolumeProfilePrefsService.class);
				volumeServiceIntent.putExtra(GlobalData.EXTRA_PROFILE_ID, profile._id);
				volumeServiceIntent.putExtra(GlobalData.EXTRA_SECOND_SET_VOLUMES, false);
				//WakefulIntentService.sendWakefulWork(context, radioServiceIntent);
				context.startService(volumeServiceIntent);
			} */
		} 
		dataWrapper.invalidateDataWrapper();
		aph = null;
		dataWrapper = null;
	}
	
	
}
