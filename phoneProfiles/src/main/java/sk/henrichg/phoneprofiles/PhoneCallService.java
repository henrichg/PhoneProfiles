package sk.henrichg.phoneprofiles;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.SystemClock;

public class PhoneCallService extends IntentService {

    private Context context;
    private static AudioManager audioManager = null;

    private static boolean savedSpeakerphone = false;
    private static boolean speakerphoneSelected = false;

    public static final int LINKMODE_NONE = 0;
    public static final int LINKMODE_LINK = 1;
    public static final int LINKMODE_UNLINK = 2;

    public PhoneCallService() {
        super("PhoneCallService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {

            context = getApplicationContext();

            int phoneEvent = intent.getIntExtra(PhoneCallBroadcastReceiver.EXTRA_SERVICE_PHONE_EVENT, 0);
            boolean incoming = intent.getBooleanExtra(PhoneCallBroadcastReceiver.EXTRA_SERVICE_PHONE_INCOMING, true);
            //String number = intent.getStringExtra(PhoneCallBroadcastReceiver.EXTRA_SERVICE_PHONE_NUMBER);

            switch (phoneEvent) {
                case PhoneCallBroadcastReceiver.SERVICE_PHONE_EVENT_START:
                    callStarted(incoming);
                    break;
                case PhoneCallBroadcastReceiver.SERVICE_PHONE_EVENT_ANSWER:
                    callAnswered();
                    break;
                case PhoneCallBroadcastReceiver.SERVICE_PHONE_EVENT_END:
                    callEnded(incoming);
                    break;
            }
        }
    }

    private void setLinkUnlinkNotificationVolume(int linkMode) {
        if (ActivateProfileHelper.getMergedRingNotificationVolumes(context) && ApplicationPreferences.applicationUnlinkRingerNotificationVolumes(context)) {
            DataWrapper dataWrapper = new DataWrapper(context, false, false, 0);
            Profile profile = dataWrapper.getActivatedProfile();
            if (profile != null) {
                //Log.e("PhoneCallService", "doCallEvent - unlink");
                try {
                    Intent volumeServiceIntent = new Intent(context, ExecuteVolumeProfilePrefsService.class);
                    volumeServiceIntent.putExtra(PPApplication.EXTRA_PROFILE_ID, profile._id);
                    volumeServiceIntent.putExtra(ActivateProfileHelper.EXTRA_LINKUNLINK_VOLUMES, linkMode);
                    volumeServiceIntent.putExtra(ActivateProfileHelper.EXTRA_FOR_PROFILE_ACTIVATION, false);
                    context.startService(volumeServiceIntent);
                } catch (Exception ignored) {}
            }
            dataWrapper.invalidateDataWrapper();
        }
    }

    private void callStarted(boolean incoming)
    {
        if (audioManager == null )
            audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);

        speakerphoneSelected = false;

        if (incoming)
            setLinkUnlinkNotificationVolume(LINKMODE_UNLINK);
    }

    private void callAnswered()
    {
        if (audioManager == null )
            audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);

        long start = SystemClock.uptimeMillis();
        do {
            if (audioManager.getMode() != AudioManager.MODE_IN_CALL)
                //try { Thread.sleep(100); } catch (InterruptedException e) {};
                SystemClock.sleep(100);
            else
                break;
        } while (SystemClock.uptimeMillis() - start < 2000);

        // audiomode is set to MODE_IN_CALL by system
        //Log.e("PhoneCallService", "callAnswered audioMode=" + audioManager.getMode());

        DataWrapper dataWrapper = new DataWrapper(context, false, false, 0);

        Profile profile = dataWrapper.getActivatedProfile();
        profile = Profile.getMappedProfile(profile, context);

        if (profile != null) {

            if (profile._volumeSpeakerPhone != 0) {
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

        dataWrapper.invalidateDataWrapper();
    }

    private void callEnded(boolean incoming)
    {
        //Deactivate loudspeaker

        if (audioManager == null )
            audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);

        // audiomode is set to MODE_IN_CALL by system
        //Log.e("PhoneCallService", "callEnded (before back speaker phone) audioMode="+audioManager.getMode());

        if (speakerphoneSelected)
        {
            audioManager.setSpeakerphoneOn(savedSpeakerphone);
        }

        speakerphoneSelected = false;

        // Delay 2 seconds mode changed to MODE_NORMAL
        long start = SystemClock.uptimeMillis();
        do {
            if (audioManager.getMode() != AudioManager.MODE_NORMAL)
                //try { Thread.sleep(100); } catch (InterruptedException e) {};
                SystemClock.sleep(100);
            else
                break;
        } while (SystemClock.uptimeMillis() - start < 2000);

        // audiomode is set to MODE_NORMAL by system
        //Log.e("PhoneCallService", "callEnded (before unlink) audioMode="+audioManager.getMode());

        if (incoming)
            setLinkUnlinkNotificationVolume(LINKMODE_LINK);
    }

}
