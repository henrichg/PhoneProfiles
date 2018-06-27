package sk.henrichg.phoneprofiles;

import android.app.backup.BackupAgentHelper;

public class PhoneProfilesBackupAgent extends BackupAgentHelper {

    @Override
    public void onCreate() {
        PPApplication.logE("PhoneProfilesBackupAgent","onCreate");
    }

    @Override
    public void onRestoreFinished() {
        PPApplication.logE("PhoneProfilesBackupAgent","onRestoreFinished");

        // DO NOT CLOSE APPLICATION AFTER RESTORE

        //DataWrapper dataWrapper = new DataWrapper(getApplicationContext(), true, false, 0);

        PPApplication.exitApp(getApplicationContext(), /*dataWrapper,*/ null, false);

        ActivateProfileActivity activateProfileActivity = ActivateProfileActivity.getInstance();
        if (activateProfileActivity != null)
        {
            PPApplication.logE("PhoneProfilesBackupAgent","close ActivateProfileActivity");
            activateProfileActivity.finish();
        }

        EditorProfilesActivity editorProfilesActivity = EditorProfilesActivity.getInstance();
        if (editorProfilesActivity != null)
        {
            PPApplication.logE("PhoneProfilesBackupAgent","close EditorProfilesActivity");
            editorProfilesActivity.finish();
        }

        Permissions.setShowRequestAccessNotificationPolicyPermission(getApplicationContext(), true);
        Permissions.setShowRequestWriteSettingsPermission(getApplicationContext(), true);
        //ActivateProfileHelper.setScreenUnlocked(getApplicationContext(), true);
        ActivateProfileHelper.setMergedRingNotificationVolumes(getApplicationContext(), true);
    }


}
