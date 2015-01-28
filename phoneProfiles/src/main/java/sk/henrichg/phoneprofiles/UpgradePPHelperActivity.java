package sk.henrichg.phoneprofiles;

import android.os.Bundle;
import android.app.Activity;

public class UpgradePPHelperActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//Log.d("BackgroundActivateProfileActivity.onCreate","xxx");
		
		GlobalData.loadPreferences(getBaseContext());
	}

	@Override
	protected void onStart()
	{
		super.onStart();

		//Log.e("BackgroundActivateProfileActivity.onStart", "startupSource="+startupSource);
		
		PhoneProfilesHelper.installPPHelper(this, true);
	}
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
	}
	
}
