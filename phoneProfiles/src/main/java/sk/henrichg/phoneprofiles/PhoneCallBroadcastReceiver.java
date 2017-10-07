package sk.henrichg.phoneprofiles;

import android.content.Intent;

import java.util.Date;

public class PhoneCallBroadcastReceiver extends PhoneCallReceiver {

    public static final String EXTRA_SERVICE_PHONE_EVENT = "service_phone_event";
    public static final String EXTRA_SERVICE_PHONE_INCOMING = "service_phone_incoming";
    public static final String EXTRA_SERVICE_PHONE_NUMBER = "service_phone_number";

    public static final int SERVICE_PHONE_EVENT_START = 1;
    public static final int SERVICE_PHONE_EVENT_ANSWER = 2;
    public static final int SERVICE_PHONE_EVENT_END = 3;

    protected boolean onStartReceive()
    {
        if (!PPApplication.getApplicationStarted(super.savedContext, true))
            return false;

        return true;
    }

    protected void onEndReceive()
    {
    }

    private void startService(int phoneEvent, boolean incoming, String number) {
        PhoneCallJob.start(phoneEvent, incoming/*, number*/);
    }

    protected void onIncomingCallStarted(String number, Date start) {
        startService(SERVICE_PHONE_EVENT_START, true, number);
    }
    
    protected void onOutgoingCallStarted(String number, Date start) {
        startService(SERVICE_PHONE_EVENT_START, false, number);
    }

    protected void onIncomingCallAnswered(String number, Date start) {
        startService(SERVICE_PHONE_EVENT_ANSWER, true, number);
    }

    protected void onOutgoingCallAnswered(String number, Date start) {
        startService(SERVICE_PHONE_EVENT_ANSWER, false, number);
    }

    protected void onIncomingCallEnded(String number, Date start, Date end) {
        startService(SERVICE_PHONE_EVENT_END, true, number);
    }

    protected void onOutgoingCallEnded(String number, Date start, Date end) {
        startService(SERVICE_PHONE_EVENT_END, false, number);
    }

    protected void onMissedCall(String number, Date start) {
        startService(SERVICE_PHONE_EVENT_END, true, number);
    }
    
}
