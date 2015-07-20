package sk.henrichg.phoneprofiles;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;

import java.util.Date;

public class PhoneCallBroadcastReceiver extends PhoneCallReceiver {

    private static AudioManager audioManager = null;

    private static boolean savedSpeakerphone = false;
    private static boolean speakerphoneSelected = false;

    public static final int LINKMODE_NONE = 0;
    public static final int LINKMODE_LINK = 1;
    public static final int LINKMODE_UNLINK = 2;

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

                ///  change mode to MODE_IN_CALL
                audioManager.setMode(AudioManager.MODE_IN_CALL);

                savedSpeakerphone = audioManager.isSpeakerphoneOn();
                boolean changeSpeakerphone = false;
                if (savedSpeakerphone && (profile._volumeSpeakerPhone == 2)) // 2=speakerphone off
                    changeSpeakerphone = true;
                if ((!savedSpeakerphone) && (profile._volumeSpeakerPhone == 1)) // 1=speakerphone on
                    changeSpeakerphone = true;
                if (changeSpeakerphone) {
                    /// activate SpeakerPhone
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
            audioManager.setMode(AudioManager.MODE_NORMAL);
        }

        speakerphoneSelected = false;
    }

    protected void onIncomingCallStarted(String number, Date start) {
        if (audioManager == null )
            audioManager = (AudioManager)savedContext.getSystemService(Context.AUDIO_SERVICE);

        speakerphoneSelected = false;

        //Log.e("PhoneCallBroadcastReceiver", "onIncomingCallStarted - applicationUnlinkRingerNotificationVolumes="+GlobalData.applicationUnlinkRingerNotificationVolumes);

        if (GlobalData.applicationUnlinkRingerNotificationVolumes) {
            /// for linked ringer and notification volume:
            //    notification volume in profile activation is set after ringer volume
            //    therefore reset ringer volume
            DataWrapper dataWrapper = new DataWrapper(savedContext, false, false, 0);
            Profile profile = dataWrapper.getActivatedProfile();
            if (profile != null) {
                Intent volumeServiceIntent = new Intent(savedContext, ExecuteVolumeProfilePrefsService.class);
                volumeServiceIntent.putExtra(GlobalData.EXTRA_PROFILE_ID, profile._id);
                volumeServiceIntent.putExtra(GlobalData.EXTRA_LINKUNLINK_VOLUMES, PhoneCallBroadcastReceiver.LINKMODE_UNLINK);
                savedContext.startService(volumeServiceIntent);
            }
            ///
        }
    }
    
    protected void onOutgoingCallStarted(String number, Date start) {
        if (audioManager == null )
            audioManager = (AudioManager)savedContext.getSystemService(Context.AUDIO_SERVICE);

        speakerphoneSelected = false;
    }

    private void setBackNotificationVolume() {
        //Log.e("PhoneCallBroadcastReceiver", "setBackNotificationVolume - applicationUnlinkRingerNotificationVolumes="+GlobalData.applicationUnlinkRingerNotificationVolumes);

        if (GlobalData.applicationUnlinkRingerNotificationVolumes) {
            DataWrapper dataWrapper = new DataWrapper(savedContext, false, false, 0);
            Profile profile = dataWrapper.getActivatedProfile();
            if (profile != null) {
                try {
                    Thread.sleep(500); // Delay 0,5 seconds to wait for change audio mode
                } catch (InterruptedException e) {
                }
                audioManager.setMode(AudioManager.MODE_NORMAL);
                Intent volumeServiceIntent = new Intent(savedContext, ExecuteVolumeProfilePrefsService.class);
                volumeServiceIntent.putExtra(GlobalData.EXTRA_PROFILE_ID, profile._id);
                volumeServiceIntent.putExtra(GlobalData.EXTRA_LINKUNLINK_VOLUMES, PhoneCallBroadcastReceiver.LINKMODE_LINK);
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
