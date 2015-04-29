package sk.henrichg.phoneprofiles;

import android.content.Context;
import android.media.AudioManager;

import java.util.Date;

public class PhoneCallBroadcastReceiver extends PhoneCallReceiver {

	private static AudioManager audioManager = null;
	
	private static int savedMode = AudioManager.MODE_NORMAL;
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
                audioManager.setSpeakerphoneOn(profile._volumeSpeakerPhone == 1);

                speakerphoneSelected = true;

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
    		audioManager.setMode(savedMode); 
    		
    		speakerphoneSelected = false;
        }
	}

    protected void onIncomingCallStarted(String number, Date start) {
		if (audioManager == null )
			audioManager = (AudioManager)savedContext.getSystemService(Context.AUDIO_SERVICE);

		savedMode = audioManager.getMode();

        /// for linked ringer and notification volume:
        // notification volume in profile activatin is set after ringer volume
        // therefore reset ringer volume
        DataWrapper dataWrapper = new DataWrapper(savedContext, false, false, 0);

        Profile profile = dataWrapper.getActivatedProfile();
        profile = GlobalData.getMappedProfile(profile, savedContext);

        if (profile != null) {
            if (profile.getVolumeRingtoneChange())
            {
                int volume = profile.getVolumeRingtoneValue();
                audioManager.setStreamVolume(AudioManager.STREAM_RING, volume, 0);
                //Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_RING, profile.getVolumeRingtoneValue());
            }
        }
    }
    
    protected void onOutgoingCallStarted(String number, Date start) {
		if (audioManager == null )
			audioManager = (AudioManager)savedContext.getSystemService(Context.AUDIO_SERVICE);

		savedMode = audioManager.getMode();
    }

    private void setBackNotificationVolume() {
        if (audioManager == null )
            audioManager = (AudioManager)savedContext.getSystemService(Context.AUDIO_SERVICE);

        DataWrapper dataWrapper = new DataWrapper(savedContext, false, false, 0);

        Profile profile = dataWrapper.getActivatedProfile();
        profile = GlobalData.getMappedProfile(profile, savedContext);

        if (profile != null) {
            if (profile.getVolumeNotificationChange())
            {
                int volume = profile.getVolumeNotificationValue();
                audioManager.setMode(savedMode);
                audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, volume, 0);
                //Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_NOTIFICATION, profile.getVolumeNotificationValue());
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
        setBackNotificationVolume();
    }
    
}
