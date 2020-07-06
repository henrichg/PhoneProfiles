package sk.henrichg.phoneprofiles;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

class ExportPPDataBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //PPApplication.logE("[***] PPPExtenderBroadcastReceiver.onReceive", "xxx");

        final Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(appContext, true))
            // application is not started
            return;

        if ((intent == null) || (intent.getAction() == null))
            return;

        //PPApplication.logE("[***] PPPExtenderBroadcastReceiver.onReceive", "action="+intent.getAction());

        switch (intent.getAction()) {
            case PPApplication.ACTION_EXPORT_PP_DATA_START:
                try {
                    // start registration service
                    Intent serviceIntent = new Intent(context.getApplicationContext(), ExportPPDataService.class);
                    PPApplication.startPPService(context, serviceIntent);
                } catch (Exception e) {
                    PPApplication.recordException(e);
                }

                break;
        }
    }

}
