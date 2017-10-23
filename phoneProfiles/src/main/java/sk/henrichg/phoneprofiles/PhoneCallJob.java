package sk.henrichg.phoneprofiles;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;

class PhoneCallJob extends Job {

    static final String JOB_TAG  = "PhoneCallJob";

    private static AudioManager audioManager = null;

    private static boolean savedSpeakerphone = false;
    private static boolean speakerphoneSelected = false;

    static final int LINKMODE_NONE = 0;
    static final int LINKMODE_LINK = 1;
    private static final int LINKMODE_UNLINK = 2;
    
    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        PPApplication.logE("ScreenOnOffJob.onRunJob", "xxx");

        final Context appContext = getContext().getApplicationContext();

        Bundle bundle = params.getTransientExtras();

        int phoneEvent = bundle.getInt(PhoneCallBroadcastReceiver.EXTRA_SERVICE_PHONE_EVENT, 0);
        boolean incoming = bundle.getBoolean(PhoneCallBroadcastReceiver.EXTRA_SERVICE_PHONE_INCOMING, true);
        //String number = bundle.getString(PhoneCallBroadcastReceiver.EXTRA_SERVICE_PHONE_NUMBER);

        switch (phoneEvent) {
            case PhoneCallBroadcastReceiver.SERVICE_PHONE_EVENT_START:
                callStarted(incoming, appContext);
                break;
            case PhoneCallBroadcastReceiver.SERVICE_PHONE_EVENT_ANSWER:
                callAnswered(appContext);
                break;
            case PhoneCallBroadcastReceiver.SERVICE_PHONE_EVENT_END:
                callEnded(incoming, appContext);
                break;
        }

        return Result.SUCCESS;
    }

    static void start(int phoneEvent, boolean incoming/*, String number*/) {
        JobRequest.Builder jobBuilder = new JobRequest.Builder(JOB_TAG);

        Bundle bundle = new Bundle();
        bundle.putInt(PhoneCallBroadcastReceiver.EXTRA_SERVICE_PHONE_EVENT, phoneEvent);
        bundle.putBoolean(PhoneCallBroadcastReceiver.EXTRA_SERVICE_PHONE_INCOMING, incoming);
        //bundle.putString(PhoneCallBroadcastReceiver.EXTRA_SERVICE_PHONE_NUMBER, number);

        try {
            jobBuilder
                    .setUpdateCurrent(false) // don't update current, it would cancel this currently running job
                    .setTransientExtras(bundle)
                    .startNow()
                    .build()
                    .schedule();
        } catch (Exception ignored) { }
    }

    private void setLinkUnlinkNotificationVolume(int linkMode, Context context) {
        if (ActivateProfileHelper.getMergedRingNotificationVolumes(context) && ApplicationPreferences.applicationUnlinkRingerNotificationVolumes(context)) {
            DataWrapper dataWrapper = new DataWrapper(context, false, false, 0);
            Profile profile = dataWrapper.getActivatedProfile();
            if (profile != null) {
                //Log.e("PhoneCallJob", "doCallEvent - unlink");
                ExecuteVolumeProfilePrefsJob.start(profile._id, linkMode, false);
            }
            dataWrapper.invalidateDataWrapper();
        }
    }

    private void callStarted(boolean incoming, Context context)
    {
        if (audioManager == null )
            audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);

        speakerphoneSelected = false;

        if (incoming)
            setLinkUnlinkNotificationVolume(LINKMODE_UNLINK, context);
    }

    private void callAnswered(Context context)
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
        //Log.e("PhoneCallJob", "callAnswered audioMode=" + audioManager.getMode());

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

    private void callEnded(boolean incoming, Context context)
    {
        //Deactivate loudspeaker

        if (audioManager == null )
            audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);

        // audiomode is set to MODE_IN_CALL by system
        //Log.e("PhoneCallJob", "callEnded (before back speaker phone) audioMode="+audioManager.getMode());

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
        //Log.e("PhoneCallJob", "callEnded (before unlink) audioMode="+audioManager.getMode());

        if (incoming)
            setLinkUnlinkNotificationVolume(LINKMODE_LINK, context);
    }
    
}
