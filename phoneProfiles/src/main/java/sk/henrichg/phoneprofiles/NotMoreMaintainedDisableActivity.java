package sk.henrichg.phoneprofiles;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
//import me.drakeet.support.toast.ToastCompat;

public class NotMoreMaintainedDisableActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);

        PPApplication.logE("NotMoreMaintainedDisableActivity.onCreate", "xxx");

        // close notification drawer - broadcast pending intent not close it :-/
        Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        sendBroadcast(it);
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        PPApplication.logE("NotMoreMaintainedDisableActivity.onStart", "xxx");

        // set theme and language for dialog alert ;-)
        GlobalGUIRoutines.setTheme(this, true, false);
        //GlobalGUIRoutines.setLanguage(this);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle(getString(R.string.not_more_maintained_notification_title));
        dialogBuilder.setMessage(getString(R.string.not_more_maintained_confirm_notification_disable));
        //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
        dialogBuilder.setPositiveButton(R.string.alert_button_yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                NotMoreMaintainedNotification.setNotMoreMaintainedNotificationOnStart(getApplicationContext(), false);
                NotMoreMaintainedNotification.removeNotification(getApplicationContext());
            }
        });
        dialogBuilder.setNegativeButton(R.string.alert_button_no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                NotMoreMaintainedNotification.setNotMoreMaintainedNotificationOnStart(getApplicationContext(), true);
                NotMoreMaintainedNotification.removeNotification(getApplicationContext());
            }
        });
        dialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                NotMoreMaintainedNotification.removeNotification(getApplicationContext());
            }
        });
        AlertDialog dialog = dialogBuilder.create();

//        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//            @Override
//            public void onShow(DialogInterface dialog) {
//                Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                if (positive != null) positive.setAllCaps(false);
//                Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                if (negative != null) negative.setAllCaps(false);
//            }
//        });

        if (!isFinishing())
            dialog.show();
    }

    @Override
    public void finish()
    {
        super.finish();
        overridePendingTransition(0, 0);
    }

}
