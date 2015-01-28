package sk.henrichg.phoneprofiles;

import java.util.Date;

import android.content.Context;
import android.media.AudioManager;

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
		int speakerPhone = dataWrapper.getDatabaseHandler().getActiveProfileSpeakerphone();
		dataWrapper.invalidateDataWrapper();

		if (speakerPhone != 0)
		{

			if (audioManager == null )
				audioManager = (AudioManager)savedContext.getSystemService(Context.AUDIO_SERVICE);
			
	        try {
	            Thread.sleep(500); // Delay 0,5 seconds to handle better turning on loudspeaker
	        } catch (InterruptedException e) {
	        }
		
	        //Activate loudspeaker
	        audioManager.setMode(AudioManager.MODE_IN_CALL);

	        savedSpeakerphone = audioManager.isSpeakerphoneOn();
	        audioManager.setSpeakerphoneOn(speakerPhone == 1);
	        
	        speakerphoneSelected = true;
	        
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
    	//Log.e("PhoneCallBroadcastReceiver.onIncomingCallStarted","xxx");
		if (audioManager == null )
			audioManager = (AudioManager)savedContext.getSystemService(Context.AUDIO_SERVICE);

		savedMode = audioManager.getMode();
    }
    
    protected void onOutgoingCallStarted(String number, Date start) {
    	//Log.e("PhoneCallBroadcastReceiver.onOutgoingCallStarted","xxx");
		if (audioManager == null )
			audioManager = (AudioManager)savedContext.getSystemService(Context.AUDIO_SERVICE);

		savedMode = audioManager.getMode();
    }

    protected void onIncomingCallAnswered(String number, Date start) {
    	//Log.e("PhoneCallBroadcastReceiver.onIncomingCallAnswered","xxx");
    	callAnswered(false);
    }

    protected void onOutgoingCallAnswered(String number, Date start) {
    	//Log.e("PhoneCallBroadcastReceiver.onOutgoingCallAnswered","xxx");
    	callAnswered(false);
    }

    protected void onIncomingCallEnded(String number, Date start, Date end) {
    	//Log.e("PhoneCallBroadcastReceiver.onIncomingCallEnded","xxx");
    	callEnded(true);
    }

    protected void onOutgoingCallEnded(String number, Date start, Date end) {
    	//Log.e("PhoneCallBroadcastReceiver.onOutgoingCallEnded","xxx");
    	callEnded(false);
    }

    protected void onMissedCall(String number, Date start) {
    }
    
}
