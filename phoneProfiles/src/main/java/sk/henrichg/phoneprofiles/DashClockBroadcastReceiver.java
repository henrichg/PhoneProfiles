package sk.henrichg.phoneprofiles;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;

public class DashClockBroadcastReceiver extends BroadcastReceiver {

    //public static final String INTENT_REFRESH_DASHCLOCK = "sk.henrichg.phoneprofiles.REFRESH_DASHCLOCK";

    @Override
    public void onReceive(Context context, Intent intent) {
        LocalBroadcastManager.getInstance(context.getApplicationContext()).unregisterReceiver(PPApplication.dashClockBroadcastReceiver);

        //DashClockJob.start(context.getApplicationContext());
        //final Context appContext = context.getApplicationContext();
        PPApplication.startHandlerThread();
        final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                PhoneProfilesDashClockExtension dashClockExtension = PhoneProfilesDashClockExtension.getInstance();
                if (dashClockExtension != null)
                {
                    dashClockExtension.updateExtension();
                }
            }
        });
    }

}
