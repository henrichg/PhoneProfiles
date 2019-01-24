package sk.henrichg.phoneprofiles;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

public class LocaleChangedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if ((intent != null) && (intent.getAction() != null) && intent.getAction().equals(Intent.ACTION_LOCALE_CHANGED)) {

            final Context appContext = context.getApplicationContext();

            if (PPApplication.getApplicationStarted(appContext, false)) {
                PPApplication.startHandlerThread();
                final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (ApplicationPreferences.applicationLanguage(appContext).equals("system")) {
                            PPApplication.showProfileNotification(/*appContext*/);
                        }
                    }
                });
            }
        }
    }

}
