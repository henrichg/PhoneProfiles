package sk.henrichg.phoneprofiles;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class RefreshGUIBroadcastReceiver extends BroadcastReceiver {

	public static final String INTENT_REFRESH_GUI = "sk.henrichg.phoneprofiles.REFRESH_GUI";

	@Override
	public void onReceive(Context context, Intent intent) {
		ActivateProfileActivity activateProfileActivity = ActivateProfileActivity.getInstance();
		if (activateProfileActivity != null)
			activateProfileActivity.refreshGUI();

		EditorProfilesActivity editorProfilesActivity = EditorProfilesActivity.getInstance();
		if (editorProfilesActivity != null)
			editorProfilesActivity.refreshGUI();
	}

}
