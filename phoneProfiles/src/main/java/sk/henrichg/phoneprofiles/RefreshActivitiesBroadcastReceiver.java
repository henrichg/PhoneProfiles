package sk.henrichg.phoneprofiles;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

public class RefreshActivitiesBroadcastReceiver extends BroadcastReceiver {

    static final String EXTRA_REFRESH_ICONS = "refresh_icons";
    static final String EXTRA_REFRESH_ALSO_EDITOR = "refresh_also_editor";

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean refreshIcons = intent.getBooleanExtra(EXTRA_REFRESH_ICONS, false);
        boolean refreshAlsoEditor = intent.getBooleanExtra(EXTRA_REFRESH_ALSO_EDITOR, true);

        Intent refreshIntent = new Intent("RefreshActivatorGUIBroadcastReceiver");
        refreshIntent.putExtra(EXTRA_REFRESH_ICONS, refreshIcons);
        LocalBroadcastManager.getInstance(context).sendBroadcast(refreshIntent);
        /*ActivateProfileActivity activateProfileActivity = ActivateProfileActivity.getInstance();
        if (activateProfileActivity != null)
            activateProfileActivity.refreshGUI(refreshIcons);*/

        if (refreshAlsoEditor) {
            refreshIntent = new Intent("RefreshEditorGUIBroadcastReceiver");
            refreshIntent.putExtra(EXTRA_REFRESH_ICONS, refreshIcons);
            LocalBroadcastManager.getInstance(context).sendBroadcast(refreshIntent);
            /*EditorProfilesActivity editorProfilesActivity = EditorProfilesActivity.getInstance();
            if (editorProfilesActivity != null)
                // not change selection in editor if refresh is outside editor
                editorProfilesActivity.refreshGUI(refreshIcons, false);*/
        }
    }

}
