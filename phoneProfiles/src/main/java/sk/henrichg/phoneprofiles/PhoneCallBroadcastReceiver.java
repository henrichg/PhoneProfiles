package sk.henrichg.phoneprofiles;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

import java.util.Date;

public class PhoneCallBroadcastReceiver extends PhoneCallReceiver {

	private static AudioManager audioManager = null;
	
	private static boolean savedSpeakerphone = false;
	private static boolean speakerphoneSelected = false;

	protected boolean onStartReceive()
	{
		if (!GlobalData.getApplicationStarted(super.savedContext))
			return false;

		GlobalData.loadPreferences(savedContext);

		return true;
	}

	protected void onEndReceive()
	{
	}
	
	private void callAnswered(boolean incoming)
	{
		DataWrapper dataWrapper = new DataWrapper(savedContext, false, false, 0);

        Profile profile = dataWrapper.getActivatedProfile();
        profile = GlobalData.getMappedProfile(profile, savedContext);

        if (profile != null) {

            if (profile._volumeSpeakerPhone != 0) {

                if (audioManager == null)
                    audioManager = (AudioManager) savedContext.getSystemService(Context.AUDIO_SERVICE);

                try {
                    Thread.sleep(500); // Delay 0,5 seconds to handle better turning on loudspeaker
                } catch (InterruptedException e) {
                }

                //Activate loudspeaker
                audioManager.setMode(AudioManager.MODE_IN_CALL);

                savedSpeakerphone = audioManager.isSpeakerphoneOn();
                boolean changeSpeakerphone = false;
                if (savedSpeakerphone && (profile._volumeSpeakerPhone == 2))
                    changeSpeakerphone = true;
                if ((!savedSpeakerphone) && (profile._volumeSpeakerPhone == 1))
                    changeSpeakerphone = true;
                if (changeSpeakerphone) {
                    audioManager.setSpeakerphoneOn(profile._volumeSpeakerPhone == 1);
                    speakerphoneSelected = true;
                }
            }
        }
	}
	
	private void callEnded(boolean incoming)
	{
    	//Deactivate loudspeaker
		
		if (audioManager == null )
			audioManager = (AudioManager)savedContext.getSystemService(Context.AUDIO_SERVICE);
		
    	//if (audioManager.isSpeakerphoneOn())
       	if (speakerphoneSelected)
    	{
    	    audioManager.setSpeakerphoneOn(savedSpeakerphone);
    		speakerphoneSelected = false;
            audioManager.setMode(AudioManager.MODE_NORMAL);
        }
    }

    protected void onIncomingCallStarted(String number, Date start) {
		if (audioManager == null )
			audioManager = (AudioManager)savedContext.getSystemService(Context.AUDIO_SERVICE);

        if (GlobalData.applicationUnlinkRingerNotificationVolumes) {
            /// for linked ringer and notification volume:
            //    notification volume in profile activation is set after ringer volume
            //    therefore reset ringer volume
            DataWrapper dataWrapper = new DataWrapper(savedContext, false, false, 0);
            Profile profile = dataWrapper.getActivatedProfile();
            if (profile != null) {
                Intent volumeServiceIntent = new Intent(savedContext, ExecuteVolumeProfilePrefsService.class);
                volumeServiceIntent.putExtra(GlobalData.EXTRA_PROFILE_ID, profile._id);
                savedContext.startService(volumeServiceIntent);
            }
            ///
        }
    }
    
    protected void onOutgoingCallStarted(String number, Date start) {
		if (audioManager == null )
			audioManager = (AudioManager)savedContext.getSystemService(Context.AUDIO_SERVICE);
    }

    private void setBackNotificationVolume() {
        if (GlobalData.applicationUnlinkRingerNotificationVolumes) {
            DataWrapper dataWrapper = new DataWrapper(savedContext, false, false, 0);
            Profile profile = dataWrapper.getActivatedProfile();
            if (profile != null) {
                Intent volumeServiceIntent = new Intent(savedContext, ExecuteVolumeProfilePrefsService.class);
                volumeServiceIntent.putExtra(GlobalData.EXTRA_PROFILE_ID, profile._id);
                savedContext.startService(volumeServiceIntent);
            }
        }
    }

    protected void onIncomingCallAnswered(String number, Date start) {
        callAnswered(false);
    }

    protected void onOutgoingCallAnswered(String number, Date start) {
    	callAnswered(false);
    }

    protected void onIncomingCallEnded(String number, Date start, Date end) {
        callEnded(true);
        setBackNotificationVolume();
    }

    protected void onOutgoingCallEnded(String number, Date start, Date end) {
        callEnded(false);
    }

    protected void onMissedCall(String number, Date start) {
        callEnded(true);
        setBackNotificationVolume();
    }
    
}
