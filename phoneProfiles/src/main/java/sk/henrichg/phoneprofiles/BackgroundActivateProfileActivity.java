package sk.henrichg.phoneprofiles;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import me.drakeet.support.toast.ToastCompat;

public class BackgroundActivateProfileActivity extends AppCompatActivity {

    private DataWrapper dataWrapper;

    private int startupSource = 0;
    private long profile_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);

        Intent intent = getIntent();
        startupSource = intent.getIntExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_SHORTCUT);
        profile_id = intent.getLongExtra(PPApplication.EXTRA_PROFILE_ID, 0);

        if ((startupSource == PPApplication.STARTUP_SOURCE_WIDGET) ||
            (startupSource == PPApplication.STARTUP_SOURCE_SHORTCUT)) {

            dataWrapper = new DataWrapper(getApplicationContext(), false, 0, false);
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        if (!PPApplication.getApplicationStarted(getApplicationContext(), true)) {
            Toast msg = ToastCompat.makeText(getApplicationContext(),
                    getResources().getString(R.string.activate_profile_application_not_started),
                    Toast.LENGTH_LONG);
            msg.show();
            finish();
        }

        if ((startupSource == PPApplication.STARTUP_SOURCE_WIDGET) ||
            (startupSource == PPApplication.STARTUP_SOURCE_SHORTCUT)) {
            dataWrapper.activateProfile(profile_id, startupSource, this);
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        dataWrapper.invalidateDataWrapper();
        dataWrapper = null;
    }

    /*
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_PROFILE) {
            if (data != null) {
                long profileId = data.getLongExtra(PPApplication.EXTRA_PROFILE_ID, 0);
                int startupSource = data.getIntExtra(PPApplication.EXTRA_STARTUP_SOURCE, 0);
                boolean activateProfile = data.getBooleanExtra(Permissions.EXTRA_ACTIVATE_PROFILE, false);

                if (activateProfile) {
                    Profile profile = dataWrapper.getProfileById(profileId, false, false);
                    dataWrapper._activateProfile(profile, startupSource, this);
                }
            }
        }
    }
    */

    @Override
    public void finish()
    {
        super.finish();
        overridePendingTransition(0, 0);
    }

}
