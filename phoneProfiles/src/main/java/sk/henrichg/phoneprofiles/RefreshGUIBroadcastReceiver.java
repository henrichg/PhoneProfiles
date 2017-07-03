package sk.henrichg.phoneprofiles;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

public class RefreshGUIBroadcastReceiver extends BroadcastReceiver {

    //public static final String INTENT_REFRESH_GUI = "sk.henrichg.phoneprofiles.REFRESH_GUI";
    public static final String EXTRA_REFRESH_ICONS = "refresh_icons";

    @Override
    public void onReceive(Context context, Intent intent) {
        LocalBroadcastManager.getInstance(context.getApplicationContext()).unregisterReceiver(PPApplication.refreshGUIBroadcastReceiver);

        boolean refreshIcons = intent.getBooleanExtra(EXTRA_REFRESH_ICONS, false);

        ActivateProfileActivity activateProfileActivity = ActivateProfileActivity.getInstance();
        if (activateProfileActivity != null)
            activateProfileActivity.refreshGUI(refreshIcons);

        EditorProfilesActivity editorProfilesActivity = EditorProfilesActivity.getInstance();
        if (editorProfilesActivity != null)
            editorProfilesActivity.refreshGUI(refreshIcons, true);
    }

}
