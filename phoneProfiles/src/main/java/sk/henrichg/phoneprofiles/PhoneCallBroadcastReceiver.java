package sk.henrichg.phoneprofiles;

import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import android.os.SystemClock;

import java.util.Date;

public class PhoneCallBroadcastReceiver extends PhoneCallReceiver {

    private static AudioManager audioManager = null;

    private static boolean savedSpeakerphone = false;
    private static boolean speakerphoneSelected = false;

    //public static final String EXTRA_SERVICE_PHONE_EVENT = "service_phone_event";
    //public static final String EXTRA_SERVICE_PHONE_INCOMING = "service_phone_incoming";
    //public static final String EXTRA_SERVICE_PHONE_NUMBER = "service_phone_number";

    private static final int SERVICE_PHONE_EVENT_START = 1;
    private static final int SERVICE_PHONE_EVENT_ANSWER = 2;
    private static final int SERVICE_PHONE_EVENT_END = 3;

    static final int LINKMODE_NONE = 0;
    static final int LINKMODE_LINK = 1;
    private static final int LINKMODE_UNLINK = 2;

    protected boolean onStartReceive()
    {
        return PPApplication.getApplicationStarted(super.savedContext, true);
    }

    protected void onEndReceive()
    {
    }

    protected void onIncomingCallStarted(String number, Date start) {
        doCall(savedContext, SERVICE_PHONE_EVENT_START, true/*, number*/);
    }
    
    protected void onOutgoingCallStarted(String number, Date start) {
        doCall(savedContext, SERVICE_PHONE_EVENT_START, false/*, number*/);
    }

    protected void onIncomingCallAnswered(String number, Date start) {
        doCall(savedContext, SERVICE_PHONE_EVENT_ANSWER, true/*, number*/);
    }

    protected void onOutgoingCallAnswered(String number, Date start) {
        doCall(savedContext, SERVICE_PHONE_EVENT_ANSWER, false/*, number*/);
    }

    protected void onIncomingCallEnded(String number, Date start, Date end) {
        doCall(savedContext, SERVICE_PHONE_EVENT_END, true/*, number*/);
    }

    protected void onOutgoingCallEnded(String number, Date start, Date end) {
        doCall(savedContext, SERVICE_PHONE_EVENT_END, false/*, number*/);
    }

    protected void onMissedCall(String number, Date start) {
        doCall(savedContext, SERVICE_PHONE_EVENT_END, true/*, number*/);
    }

    private void doCall(final Context context, final int phoneEvent, final boolean incoming/*, final String number*/) {
        final Context appContext = context.getApplicationContext();
        PPApplication.startHandlerThread();
        final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
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
            }
        });
    }

    private static void setLinkUnlinkNotificationVolume(final int linkMode, final Context context) {
        if (ActivateProfileHelper.getMergedRingNotificationVolumes(context) && ApplicationPreferences.applicationUnlinkRingerNotificationVolumes(context)) {
            DataWrapper dataWrapper = new DataWrapper(context, false, 0);
            final Profile profile = dataWrapper.getActivatedProfile(false, false);
            if (profile != null) {
                ActivateProfileHelper.executeForVolumes(context, profile, linkMode, false);
            }
            dataWrapper.invalidateDataWrapper();
        }
    }

    private static void callStarted(boolean incoming, Context context)
    {
        if (audioManager == null )
            audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);

        speakerphoneSelected = false;

        if (incoming)
            setLinkUnlinkNotificationVolume(LINKMODE_UNLINK, context);
    }

    private static void callAnswered(Context context)
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

        // audio mode is set to MODE_IN_CALL by system

        DataWrapper dataWrapper = new DataWrapper(context, false, 0);

        Profile profile = dataWrapper.getActivatedProfile(false, false);
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

    private static void callEnded(boolean incoming, Context context)
    {
        //Deactivate loudspeaker

        if (audioManager == null )
            audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);

        // audio mode is set to MODE_IN_CALL by system

        if (speakerphoneSelected)
        {
            if (audioManager != null)
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

        // audio mode is set to MODE_NORMAL by system

        if (incoming)
            setLinkUnlinkNotificationVolume(LINKMODE_LINK, context);
    }

}
