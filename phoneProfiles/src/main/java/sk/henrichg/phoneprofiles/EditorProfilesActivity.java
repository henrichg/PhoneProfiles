package sk.henrichg.phoneprofiles;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import sk.henrichg.phoneprofiles.EditorProfileListFragment.OnFinishProfilePreferencesActionMode;
import sk.henrichg.phoneprofiles.EditorProfileListFragment.OnStartProfilePreferences;
import sk.henrichg.phoneprofiles.ProfilePreferencesFragment.OnHideActionMode;
import sk.henrichg.phoneprofiles.ProfilePreferencesFragment.OnRedrawProfileListFragment;
import sk.henrichg.phoneprofiles.ProfilePreferencesFragment.OnRestartProfilePreferences;
import sk.henrichg.phoneprofiles.ProfilePreferencesFragment.OnShowActionMode;

import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceScreen;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;

public class EditorProfilesActivity extends ActionBarActivity
                                    implements OnStartProfilePreferences,
                                               OnRestartProfilePreferences,
                                               OnRedrawProfileListFragment,
                                               OnFinishProfilePreferencesActionMode,
                                               OnShowActionMode,
                                               OnHideActionMode
{

	private static EditorProfilesActivity instance;

	private static boolean savedInstanceStateChanged; 

	private static ApplicationsCache applicationsCache;
	
	private int editMode;

	private static final String SP_RESET_PREFERENCES_FRAGMENT = "editor_restet_preferences_fragment";
	private static final String SP_RESET_PREFERENCES_FRAGMENT_PROFILE_ID = "editor_restet_preferences_fragment_profile_id";
	private static final String SP_RESET_PREFERENCES_FRAGMENT_EDIT_MODE = "editor_restet_preferences_fragment_edit_mode";
	private static final int RESET_PREFERENCE_FRAGMENT_RESET = 1;
	private static final int RESET_PREFERENCE_FRAGMENT_REMOVE = 2;
	
	
	/**
	 * Whether or not the activity is in two-pane mode, i.e. running on a tablet
	 * device.
	 */
	public static boolean mTwoPane;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		GUIData.setTheme(this, false);
		GUIData.setLanguage(getBaseContext());

		super.onCreate(savedInstanceState);

		instance = this;
		
		savedInstanceStateChanged = (savedInstanceState != null);
		
		createApplicationsCache();
		
		setContentView(R.layout.activity_editor_profile_list);
		
		if (findViewById(R.id.editor_profile_detail_container) != null) {
			// The detail container view will be present only in the
			// large-screen layouts (res/values-large and
			// res/values-sw600dp). If this view is present, then the
			// activity should be in two-pane mode.
			mTwoPane = true;

			if (savedInstanceState == null)
				onStartProfilePreferences(null, EditorProfileListFragment.EDIT_MODE_EDIT);
			else
			{
				// for 7 inch tablets lauout changed:
				//   - portrait - one pane
				//   - landscape - two pane
				// onRestartProfilePreferences is called, when user save/not save profile
				// preference changes (Back button, or Cancel in ActionMode)
				// In this method, editmode and profile_id is saved into shared preferences
				// And when orientaion changed into lanscape mode, profile preferences fragment
				// must by recreated due profile preference changes
		    	SharedPreferences preferences = getSharedPreferences(GlobalData.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
		    	int resetMode = preferences.getInt(SP_RESET_PREFERENCES_FRAGMENT, 0);
		    	if (resetMode == RESET_PREFERENCE_FRAGMENT_RESET)
		    	{
					// restart profile preferences fragmentu
		    		long profile_id = preferences.getLong(SP_RESET_PREFERENCES_FRAGMENT_PROFILE_ID, 0);
		    		int editMode =  preferences.getInt(SP_RESET_PREFERENCES_FRAGMENT_EDIT_MODE, EditorProfileListFragment.EDIT_MODE_UNDEFINED);
					Bundle arguments = new Bundle();
					arguments.putLong(GlobalData.EXTRA_PROFILE_ID, profile_id);
					arguments.putInt(GlobalData.EXTRA_NEW_PROFILE_MODE, editMode);
					arguments.putInt(GlobalData.EXTRA_PREFERENCES_STARTUP_SOURCE, GlobalData.PREFERENCES_STARTUP_SOURCE_FRAGMENT);
					ProfilePreferencesFragment fragment = new ProfilePreferencesFragment();
					fragment.setArguments(arguments);
					getFragmentManager().beginTransaction()
							.replace(R.id.editor_profile_detail_container, fragment, "ProfilePreferencesFragment").commit();
		    	}
		    	else
		    	if (resetMode == RESET_PREFERENCE_FRAGMENT_REMOVE)
		    	{
					ProfilePreferencesFragment fragment = (ProfilePreferencesFragment)getFragmentManager().findFragmentById(R.id.editor_profile_detail_container);
					if (fragment != null)
					{
						getFragmentManager().beginTransaction()
							.remove(fragment).commit();
					}
		    	}
		    	// remove preferences
		    	Editor editor = preferences.edit();
		    	editor.remove(SP_RESET_PREFERENCES_FRAGMENT);
		    	editor.remove(SP_RESET_PREFERENCES_FRAGMENT_PROFILE_ID);
		    	editor.remove(SP_RESET_PREFERENCES_FRAGMENT_EDIT_MODE);
				editor.commit();
			}
		}
		else
		{
			mTwoPane = false;
			FragmentManager fragmentManager = getFragmentManager();
			Fragment fragment = fragmentManager.findFragmentByTag("ProfilePreferencesFragment");
			if (fragment != null)
				fragmentManager.beginTransaction()
				.remove(fragment).commit();
			fragmentManager.executePendingTransactions();
		}
		
		//getSupportActionBar().setHomeButtonEnabled(true);
		//getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle(R.string.title_activity_editor);

	/*	getSupportActionBar().setDisplayShowTitleEnabled(false);
		
		// Create an array adapter to populate dropdownlist 
	    ArrayAdapter<CharSequence> navigationAdapter =
	            ArrayAdapter.createFromResource(getBaseContext(), R.array.phoneProfilesNavigator, R.layout.sherlock_spinner_item);

	    // Enabling dropdown list navigation for the action bar 
	    getSupportActionBar().setNavigationMode(com.actionbarsherlock.app.ActionBar.NAVIGATION_MODE_LIST);
	*/

	/*    // Defining Navigation listener 
	    ActionBar.OnNavigationListener navigationListener = new ActionBar.OnNavigationListener() {

	        public boolean onNavigationItemSelected(int itemPosition, long itemId) {
	            switch(itemPosition) {
	            case 0:
	            	//
	                break;
	            case 1:
	                //...
	                break;
	            }
	            return false;
	        }
	    };

	    // Setting dropdown items and item navigation listener for the actionbar 
	    getSupportActionBar().setListNavigationCallbacks(navigationAdapter, navigationListener);
	    navigationAdapter.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);
	*/	

		
		//Log.e("EditorProfilesActivity.onCreate", "xxxx");
		
	}

	public static EditorProfilesActivity getInstance()
	{
		return instance;
	}
	
	@Override
	protected void onStart()
	{
		super.onStart();
		
		//Log.d("EditorProfilesActivity.onStart", "xxxx");
		
	}
	
	@Override
	protected void onStop()
	{
		super.onStop();
		instance = null;

		//Log.e("EditorProfilesActivity.onStop","xxx");
	}
	
	@Override 
	protected void onResume()
	{
		super.onResume();
		if (instance == null)
		{
			instance = this;
			refreshGUI();
		}
	}
	
	@Override
	protected void onDestroy()
	{
		//Log.e("EditorProfilesActivity.onDestroy", "xxxx");

		if (!savedInstanceStateChanged)
		{
			// no destroy applicationsCache on orientation change
			if (applicationsCache != null)
				applicationsCache.clearCache(true);
			applicationsCache = null;
		}
		
		super.onDestroy();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_editor_profiles, menu);
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

		boolean isPPHInstalled = PhoneProfilesHelper.isPPHelperInstalled(getBaseContext(), PhoneProfilesHelper.PPHELPER_CURRENT_VERSION);
		
		MenuItem menuItem = menu.findItem(R.id.menu_pphelper_install);
		if (menuItem != null)
		{
			//menuItem.setVisible(GlobalData.isRooted(true) && (!isPPHInstalled));
			menuItem.setVisible(!isPPHInstalled);
			
			if (PhoneProfilesHelper.PPHelperVersion != -1)
			{
				menuItem.setTitle(R.string.menu_phoneprofilehepler_upgrade);
			}
			else
			{
				menuItem.setTitle(R.string.menu_phoneprofilehepler_install);
			}
		}
		menuItem = menu.findItem(R.id.menu_pphelper_uninstall);
		if (menuItem != null)
		{
			//menuItem.setVisible(GlobalData.isRooted(true) && (PhoneProfilesHelper.PPHelperVersion != -1));
			menuItem.setVisible(PhoneProfilesHelper.PPHelperVersion != -1);
		}
		
		return super.onPrepareOptionsMenu(menu);
	}	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		Intent intent;
			
		switch (item.getItemId()) {
		case R.id.menu_settings:
			//Log.d("EditorProfilesActivity.onOptionsItemSelected", "menu_settings");
			
			intent = new Intent(getBaseContext(), PhoneProfilesPreferencesActivity.class);

			startActivityForResult(intent, GlobalData.REQUEST_CODE_APPLICATION_PREFERENCES);

			return true;
		case R.id.menu_pphelper_install:
			PhoneProfilesHelper.installPPHelper(this, false);
			return true;
		case R.id.menu_pphelper_uninstall:
			PhoneProfilesHelper.uninstallPPHelper(this);
			return true;
		case R.id.menu_export:
			//Log.d("EditorProfileListFragment.onOptionsItemSelected", "menu_export");

			exportData();
			
			return true;
		case R.id.menu_import:
			//Log.d("EditorProfileListFragment.onOptionsItemSelected", "menu_import");

			importData();
			
			return true;
		/*case R.id.menu_help:
			try {
			    Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/henrichg/PhoneProfiles/wiki"));
			    startActivity(myIntent);
			} catch (ActivityNotFoundException e) {
			    //Toast.makeText(this, "No application can handle this request."
			    //    + " Please install a webbrowser",  Toast.LENGTH_LONG).show();
			    e.printStackTrace();
			}			
			return true;*/
		case R.id.menu_exit:
			//Log.d("EditorProfilesActivity.onOptionsItemSelected", "menu_exit");
			
			GlobalData.setApplicationStarted(getBaseContext(), false);
			
			// zrusenie notifikacie
			getDataWrapper().getActivateProfileHelper().removeNotification();
			
			stopService(new Intent(getApplicationContext(), ReceiversService.class));
			if (Keyguard.keyguardService != null)
				stopService(Keyguard.keyguardService);
			
			Keyguard.reenable();
			
			finish();

			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	// fix for bug in LG stock ROM Android <= 4.1
	// https://code.google.com/p/android/issues/detail?id=78154
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	     if ((keyCode == KeyEvent.KEYCODE_MENU) &&
		      (Build.VERSION.SDK_INT <= 16) &&
		      (Build.MANUFACTURER.compareTo("LGE") == 0)) {
		   return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
	    if ((keyCode == KeyEvent.KEYCODE_MENU) &&
		         (Build.VERSION.SDK_INT <= 16) &&
		         (Build.MANUFACTURER.compareTo("LGE") == 0)) {
		   openOptionsMenu();
	     return true;
	    }
	    return super.onKeyUp(keyCode, event);
	}
	/////
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		savedInstanceStateChanged = true; 
		
		if (mTwoPane) {
	    	SharedPreferences preferences = getSharedPreferences(GlobalData.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
			
			if ((editMode != EditorProfileListFragment.EDIT_MODE_INSERT) &&
			    (editMode != EditorProfileListFragment.EDIT_MODE_DUPLICATE))
			{
				FragmentManager fragmentManager = getFragmentManager();
				Fragment fragment = fragmentManager.findFragmentByTag("ProfilePreferencesFragment");
				if (fragment != null)
				{
					Editor editor = preferences.edit();
					editor.putInt(SP_RESET_PREFERENCES_FRAGMENT, RESET_PREFERENCE_FRAGMENT_RESET);
					editor.putLong(SP_RESET_PREFERENCES_FRAGMENT_PROFILE_ID, ((ProfilePreferencesFragment)fragment).profile_id);
					editor.putInt(SP_RESET_PREFERENCES_FRAGMENT_EDIT_MODE, editMode);
					editor.commit();
				}
			}
			else
			{
		    	Editor editor = preferences.edit();
		    	editor.putInt(SP_RESET_PREFERENCES_FRAGMENT, RESET_PREFERENCE_FRAGMENT_REMOVE);
		    	editor.putLong(SP_RESET_PREFERENCES_FRAGMENT_PROFILE_ID, 0);
		    	editor.putInt(SP_RESET_PREFERENCES_FRAGMENT_EDIT_MODE, editMode);
				editor.commit();
			}
		}
    	
	}	
	
	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
		
		getBaseContext().getResources().updateConfiguration(newConfig, getBaseContext().getResources().getDisplayMetrics());
		GUIData.reloadActivity(this, false);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		//Log.e("EditorProfileListActivity.onActivityResult","requestCode="+requestCode);
		
		if (requestCode == GlobalData.REQUEST_CODE_ACTIVATE_PROFILE)
		{
			EditorProfileListFragment fragment = (EditorProfileListFragment)getFragmentManager().findFragmentById(R.id.editor_profile_list);
			if (fragment != null)
				fragment.doOnActivityResult(requestCode, resultCode, data);
		}
		else
		if (requestCode == GlobalData.REQUEST_CODE_PROFILE_PREFERENCES)
		{
			if ((resultCode == RESULT_OK) && (data != null))
			{
				long profile_id = data.getLongExtra(GlobalData.EXTRA_PROFILE_ID, 0);
				int newProfileMode = data.getIntExtra(GlobalData.EXTRA_NEW_PROFILE_MODE, EditorProfileListFragment.EDIT_MODE_UNDEFINED);
	
				//Log.e("EditorProfilesActivity.onActivityResult","profile_id="+profile_id);
				
				if (profile_id > 0)
				{
					Profile profile = getDataWrapper().getDatabaseHandler().getProfile(profile_id);
					//Log.e("EditorProfilesActivity.onActivityResult","profile="+profile);
			    	// generate bitmaps
					profile.generateIconBitmap(getBaseContext(), false, 0);
					profile.generatePreferencesIndicator(getBaseContext(), false, 0);
	
					// redraw list fragment , notifications, widgets after finish ProfilePreferencesFragmentActivity
					onRedrawProfileListFragment(profile, newProfileMode);
				}
				else
				if (profile_id == GlobalData.DEFAULT_PROFILE_ID)
				{
					// refresh activity for changes of default profile
					GUIData.reloadActivity(this, false);
				}
			}
		}
		else
		if (requestCode == GlobalData.REQUEST_CODE_APPLICATION_PREFERENCES)
		{
			if ((resultCode == RESULT_OK) && (data != null))
			{
				boolean restart = data.getBooleanExtra(GlobalData.EXTRA_RESET_EDITOR, false); 
	
				if (restart)
				{
					// refresh activity for special changes
					GUIData.reloadActivity(this, true);
				}
			}
		}
		else
		if (requestCode == GlobalData.REQUEST_CODE_REMOTE_EXPORT)
		{
			//Log.e("EditorProfilesActivity.onActivityResult","resultCode="+resultCode);

			if (resultCode == RESULT_OK)
			{
				doImportData(GUIData.REMOTE_EXPORT_PATH);
			}	
		}
		else
		{
			// send other activity results into preference fragment
			ProfilePreferencesFragment fragment = (ProfilePreferencesFragment)getFragmentManager().findFragmentById(R.id.editor_profile_detail_container);
			if (fragment != null)
				fragment.doOnActivityResult(requestCode, resultCode, data);
		}
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            // handle your back button code here
    		if (mTwoPane) {
	        	ProfilePreferencesFragment fragment = (ProfilePreferencesFragment)getFragmentManager().findFragmentById(R.id.editor_profile_detail_container);
	    		if ((fragment != null) && (fragment.isActionModeActive()))
	    		{
	    			fragment.finishActionMode(ProfilePreferencesFragment.BUTTON_CANCEL);
		            return true; // consumes the back key event - ActionMode is not finished
	    		}
	    		else
	    		    return super.dispatchKeyEvent(event);
    		}
    		else
    		    return super.dispatchKeyEvent(event);
        }
	    return super.dispatchKeyEvent(event);
	}

	private void importExportErrorDialog(int importExport)
	{
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
		String resString;
		if (importExport == 1)
			resString = getResources().getString(R.string.import_profiles_alert_title);
		else
			resString = getResources().getString(R.string.export_profiles_alert_title);
		dialogBuilder.setTitle(resString);
		if (importExport == 1)
			resString = getResources().getString(R.string.import_profiles_alert_error);
		else
			resString = getResources().getString(R.string.export_profiles_alert_error);
		dialogBuilder.setMessage(resString + "!");
		//dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
		dialogBuilder.setPositiveButton(android.R.string.ok, null);
		dialogBuilder.show();
	}
	
	@SuppressWarnings({ "unchecked" })
	private boolean importApplicationPreferences(File src, int what) {
	    boolean res = false;
	    ObjectInputStream input = null;
	    try {
	        	input = new ObjectInputStream(new FileInputStream(src));
	            Editor prefEdit;
		        if (what == 1)
		        	prefEdit = getSharedPreferences(GlobalData.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE).edit();
		        else
		        	prefEdit = getSharedPreferences(GlobalData.DEFAULT_PROFILE_PREFS_NAME, Activity.MODE_PRIVATE).edit();
	            prefEdit.clear();
	            Map<String, ?> entries = (Map<String, ?>) input.readObject();
	            for (Entry<String, ?> entry : entries.entrySet()) {
	                Object v = entry.getValue();
	                String key = entry.getKey();

	                if (v instanceof Boolean)
	                    prefEdit.putBoolean(key, ((Boolean) v).booleanValue());
	                else if (v instanceof Float)
	                    prefEdit.putFloat(key, ((Float) v).floatValue());
	                else if (v instanceof Integer)
	                    prefEdit.putInt(key, ((Integer) v).intValue());
	                else if (v instanceof Long)
	                    prefEdit.putLong(key, ((Long) v).longValue());
	                else if (v instanceof String)
	                    prefEdit.putString(key, ((String) v));
	                
	                if (what == 1)
	                {
	                	if (key.equals(GlobalData.PREF_APPLICATION_THEME))
	                	{
	                		if (((String)v).equals("light"))
	    	                    prefEdit.putString(key, "material");
	                	}
	                }
	            }
	            prefEdit.commit();
	        res = true;         
	    } catch (FileNotFoundException e) {
	    	// no error, this is OK
	        //e.printStackTrace();
	    	res = true;
	    } catch (IOException e) {
	        e.printStackTrace();
	    } catch (ClassNotFoundException e) {
	        e.printStackTrace();
	    }finally {
	        try {
	            if (input != null) {
	                input.close();
	            }
	        } catch (IOException ex) {
	            ex.printStackTrace();
	        }
	    }
	    return res;
	}	
	
	private void doImportData(String applicationDataPath)
	{
		final Activity activity = this;
		final String _applicationDataPath = applicationDataPath;
		
		class ImportAsyncTask extends AsyncTask<Void, Integer, Integer> 
		{
			private ProgressDialog dialog;
			private DataWrapper dataWrapper;
			
			ImportAsyncTask()
			{
		         this.dialog = new ProgressDialog(activity);
		         this.dataWrapper = getDataWrapper();
			}
			
			@Override
			protected void onPreExecute()
			{
				super.onPreExecute();

                lockScreenOrientation();
			    this.dialog.setMessage(getResources().getString(R.string.import_profiles_alert_title));
                this.dialog.setCancelable(false);
                this.dialog.setIndeterminate(false);
			    this.dialog.show();
				
				// check root, this set GlobalData.rooted for doInBackgroud()
				GlobalData.isRooted(false);
			}
			
			@Override
			protected Integer doInBackground(Void... params) {
				
				int ret = dataWrapper.getDatabaseHandler().importDB(_applicationDataPath);
				
				if (ret == 1)
				{
					// check for hardware capability and update data
					ret = dataWrapper.getDatabaseHandler().updateForHardware(activity.getBaseContext());
				}
				if (ret == 1)
				{
					File sd = Environment.getExternalStorageDirectory();
					File exportFile = new File(sd, _applicationDataPath + "/" + GUIData.EXPORT_APP_PREF_FILENAME);
					if (!importApplicationPreferences(exportFile, 1))
						ret = 0;
					else
					{
						exportFile = new File(sd, _applicationDataPath + "/" + GUIData.EXPORT_DEF_PROFILE_PREF_FILENAME);
						if (!importApplicationPreferences(exportFile, 2))
							ret = 0;
					}
				}
				
				return ret;
			}
			
			@Override
			protected void onPostExecute(Integer result)
			{
				super.onPostExecute(result);
				
			    if (dialog.isShowing())
		            dialog.dismiss();
                unlockScreenOrientation();
				
				if (result == 1)
				{
					GlobalData.loadPreferences(getBaseContext());

					dataWrapper.clearProfileList();
					dataWrapper.getDatabaseHandler().deactivateProfile();
					dataWrapper.getActivateProfileHelper().showNotification(null);
					dataWrapper.getActivateProfileHelper().updateWidget();

					// toast notification
					Toast msg = Toast.makeText(getBaseContext(), 
							getResources().getString(R.string.toast_import_ok), 
							Toast.LENGTH_SHORT);
					msg.show();

					// refresh activity
					GUIData.reloadActivity(activity, true);
				
				}
				else
				{
					importExportErrorDialog(1);
				}
			}

            private void lockScreenOrientation() {
                int currentOrientation = activity.getResources().getConfiguration().orientation;
                if (currentOrientation == Configuration.ORIENTATION_PORTRAIT) {
                    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                } else {
                    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                }
            }

            private void unlockScreenOrientation() {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
            }


        }
		
		new ImportAsyncTask().execute();
	}
	
	private void importDataAlert(boolean remoteExport)
	{
		final boolean _remoteExport = remoteExport;
		AlertDialog.Builder dialogBuilder2 = new AlertDialog.Builder(this);
		if (remoteExport)
		{
			dialogBuilder2.setTitle(getResources().getString(R.string.import_profiles_from_phoneprofilesplus_alert_title2));
			dialogBuilder2.setMessage(getResources().getString(R.string.import_profiles_alert_message));
			//dialogBuilder2.setIcon(android.R.drawable.ic_dialog_alert);
		}
		else
		{
			dialogBuilder2.setTitle(getResources().getString(R.string.import_profiles_alert_title));
			dialogBuilder2.setMessage(getResources().getString(R.string.import_profiles_alert_message));
			//dialogBuilder2.setIcon(android.R.drawable.ic_dialog_alert);
		}

		dialogBuilder2.setPositiveButton(R.string.alert_button_yes, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if (_remoteExport)
				{
					// start RemoteExportDataActivity
					Intent intent = new Intent("phoneprofilesplus.intent.action.EXPORTDATA");
					
					final PackageManager packageManager = getPackageManager();
				    List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
				    if (list.size() > 0)					
				    	startActivityForResult(intent, GlobalData.REQUEST_CODE_REMOTE_EXPORT);
				    else
				    	importExportErrorDialog(1);				    	
				}
				else
					doImportData(GlobalData.EXPORT_PATH);
			}
		});
		dialogBuilder2.setNegativeButton(R.string.alert_button_no, null);
		dialogBuilder2.show();
	}
	
	private void importData()
	{
		// test whether the PhoneProfilePlus is installed
		PackageManager packageManager = getBaseContext().getPackageManager();
		Intent phoneProfiles = packageManager.getLaunchIntentForPackage("sk.henrichg.phoneprofilesplus");
		if (phoneProfiles != null)
		{
			// PhoneProfilesPlus is istalled

			AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
			dialogBuilder.setTitle(getResources().getString(R.string.import_profiles_from_phoneprofilesplus_alert_title));
			dialogBuilder.setMessage(getResources().getString(R.string.import_profiles_from_phoneprofilesplus_alert_message));
			//dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
			
			dialogBuilder.setPositiveButton(R.string.alert_button_yes, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					importDataAlert(true);
				}
			});
			dialogBuilder.setNegativeButton(R.string.alert_button_no, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					importDataAlert(false);
				}
			});
			dialogBuilder.show();
		}
		else
			importDataAlert(false);
	}

	private boolean exportApplicationPreferences(File dst, int what) {
	    boolean res = false;
	    ObjectOutputStream output = null;
	    try {
	        output = new ObjectOutputStream(new FileOutputStream(dst));
	        SharedPreferences pref;
	        if (what == 1)
	        	pref = getSharedPreferences(GlobalData.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
	        else
	        	pref = getSharedPreferences(GlobalData.DEFAULT_PROFILE_PREFS_NAME, Activity.MODE_PRIVATE);
	        output.writeObject(pref.getAll());

	        res = true;
	    } catch (FileNotFoundException e) {
	    	// this is OK
	        //e.printStackTrace();
	    	res = true;
	    } catch (IOException e) {
	        e.printStackTrace();
	    }finally {
	        try {
	            if (output != null) {
	                output.flush();
	                output.close();
	            }
	        } catch (IOException ex) {
	            ex.printStackTrace();
	        }
	    }
	    return res;
	}
	
	private void exportData()
	{
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
		dialogBuilder.setTitle(getResources().getString(R.string.export_profiles_alert_title));
		dialogBuilder.setMessage(getResources().getString(R.string.export_profiles_alert_message) + "?");
		//dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);

		final Activity activity = this;
		
		dialogBuilder.setPositiveButton(R.string.alert_button_yes, new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				
				class ExportAsyncTask extends AsyncTask<Void, Integer, Integer> 
				{
					private ProgressDialog dialog;
					private DataWrapper dataWrapper;
					
					ExportAsyncTask()
					{
				         this.dialog = new ProgressDialog(activity);
				         this.dataWrapper = getDataWrapper();
					}
					
					@Override
					protected void onPreExecute()
					{
						super.onPreExecute();
						
					    this.dialog.setMessage(getResources().getString(R.string.export_profiles_alert_title));
                        this.dialog.setCancelable(false);
                        this.dialog.setIndeterminate(false);
					    this.dialog.show();
					}
					
					@Override
					protected Integer doInBackground(Void... params) {
						
						int ret = dataWrapper.getDatabaseHandler().exportDB();
						if (ret == 1)
						{
							File sd = Environment.getExternalStorageDirectory();
							File exportFile = new File(sd, GlobalData.EXPORT_PATH + "/" + GUIData.EXPORT_APP_PREF_FILENAME);
							if (!exportApplicationPreferences(exportFile, 1))
								ret = 0;
							else
							{
								exportFile = new File(sd, GlobalData.EXPORT_PATH + "/" + GUIData.EXPORT_DEF_PROFILE_PREF_FILENAME);
								if (!exportApplicationPreferences(exportFile, 2))
									ret = 0;
							}
						}

						return ret;
					}
					
					@Override
					protected void onPostExecute(Integer result)
					{
						super.onPostExecute(result);
						
					    if (dialog.isShowing())
				            dialog.dismiss();
						
						if (result == 1)
						{

							// toast notification
							Toast msg = Toast.makeText(getBaseContext(), 
									getResources().getString(R.string.toast_export_ok), 
									Toast.LENGTH_SHORT);
							msg.show();
						
						}
						else
						{
							importExportErrorDialog(2);
						}
					}
					
				}
				
				new ExportAsyncTask().execute();
				
			}
		});
		dialogBuilder.setNegativeButton(R.string.alert_button_no, null);
		dialogBuilder.show();
	}
	
	public void onStartProfilePreferences(Profile profile, int editMode) {
		
		this.editMode = editMode;

		onFinishProfilePreferencesActionMode();
		
		if (mTwoPane) {
			// In two-pane mode, show the detail view in this activity by
			// adding or replacing the detail fragment using a
			// fragment transaction.

			if ((profile != null) || 
				(editMode == EditorProfileListFragment.EDIT_MODE_INSERT) ||
				(editMode == EditorProfileListFragment.EDIT_MODE_DUPLICATE))
			{
				Bundle arguments = new Bundle();
				if (editMode == EditorProfileListFragment.EDIT_MODE_INSERT)
					arguments.putLong(GlobalData.EXTRA_PROFILE_ID, 0);
				else
					arguments.putLong(GlobalData.EXTRA_PROFILE_ID, profile._id);
				arguments.putInt(GlobalData.EXTRA_NEW_PROFILE_MODE, editMode);
				arguments.putInt(GlobalData.EXTRA_PREFERENCES_STARTUP_SOURCE, GlobalData.PREFERENCES_STARTUP_SOURCE_FRAGMENT);
				ProfilePreferencesFragment fragment = new ProfilePreferencesFragment();
				fragment.setArguments(arguments);
				getFragmentManager().beginTransaction()
					.replace(R.id.editor_profile_detail_container, fragment, "ProfilePreferencesFragment").commit();
			}
			else
			{
				ProfilePreferencesFragment fragment = (ProfilePreferencesFragment)getFragmentManager().findFragmentById(R.id.editor_profile_detail_container);
				if (fragment != null)
				{
					getFragmentManager().beginTransaction()
						.remove(fragment).commit();
				}
			}

		} else {
			// In single-pane mode, simply start the profile preferences activity
			// for the profile id.
			if (((profile != null) || 
				 (editMode == EditorProfileListFragment.EDIT_MODE_INSERT) ||
				 (editMode == EditorProfileListFragment.EDIT_MODE_DUPLICATE))
				&& (editMode != EditorProfileListFragment.EDIT_MODE_DELETE))
			{
				Intent intent = new Intent(getBaseContext(), ProfilePreferencesFragmentActivity.class);
				if (editMode == EditorProfileListFragment.EDIT_MODE_INSERT)
					intent.putExtra(GlobalData.EXTRA_PROFILE_ID, 0);
				else
					intent.putExtra(GlobalData.EXTRA_PROFILE_ID, profile._id);
				intent.putExtra(GlobalData.EXTRA_NEW_PROFILE_MODE, editMode);
				startActivityForResult(intent, GlobalData.REQUEST_CODE_PROFILE_PREFERENCES);
			}
		}
	}

	public void onRestartProfilePreferences(Profile profile, int newProfileMode) {
		if (mTwoPane) {
			if ((newProfileMode != EditorProfileListFragment.EDIT_MODE_INSERT) &&
			    (newProfileMode != EditorProfileListFragment.EDIT_MODE_DUPLICATE))
			{
				// restart profile preferences fragmentu
				Bundle arguments = new Bundle();
				arguments.putLong(GlobalData.EXTRA_PROFILE_ID, profile._id);
				arguments.putInt(GlobalData.EXTRA_NEW_PROFILE_MODE, editMode);
				arguments.putInt(GlobalData.EXTRA_PREFERENCES_STARTUP_SOURCE, GlobalData.PREFERENCES_STARTUP_SOURCE_FRAGMENT);
				ProfilePreferencesFragment fragment = new ProfilePreferencesFragment();
				fragment.setArguments(arguments);
				getFragmentManager().beginTransaction()
						.replace(R.id.editor_profile_detail_container, fragment, "ProfilePreferencesFragment").commit();
			}
			else
			{
				ProfilePreferencesFragment fragment = (ProfilePreferencesFragment)getFragmentManager().findFragmentById(R.id.editor_profile_detail_container);
				if (fragment != null)
				{
					getFragmentManager().beginTransaction()
						.remove(fragment).commit();
				}
			}
		}
		else
		{
	    	SharedPreferences preferences = getSharedPreferences(GlobalData.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
			
			if ((newProfileMode != EditorProfileListFragment.EDIT_MODE_INSERT) &&
			    (newProfileMode != EditorProfileListFragment.EDIT_MODE_DUPLICATE))
			{
		    	Editor editor = preferences.edit();
		    	editor.putInt(SP_RESET_PREFERENCES_FRAGMENT, RESET_PREFERENCE_FRAGMENT_RESET);
		    	editor.putLong(SP_RESET_PREFERENCES_FRAGMENT_PROFILE_ID, profile._id);
		    	editor.putInt(SP_RESET_PREFERENCES_FRAGMENT_EDIT_MODE, editMode);
				editor.commit();
			}
			else
			{
		    	Editor editor = preferences.edit();
		    	editor.putInt(SP_RESET_PREFERENCES_FRAGMENT, RESET_PREFERENCE_FRAGMENT_REMOVE);
		    	editor.putLong(SP_RESET_PREFERENCES_FRAGMENT_PROFILE_ID, profile._id);
		    	editor.putInt(SP_RESET_PREFERENCES_FRAGMENT_EDIT_MODE, editMode);
				editor.commit();
			}
		}
	}

	public void onRedrawProfileListFragment(Profile profile, int newProfileMode) 
	{
		//Log.e("EditorProfileActivity.onRedrawProfileListFragment","xxx");

		// redraw headeru list fragmentu, notifikacie a widgetov
		EditorProfileListFragment fragment = (EditorProfileListFragment)getFragmentManager().findFragmentById(R.id.editor_profile_list);
		//Log.e("EditorProfilesActivity.onRedrawProfileListFragment","fragment="+fragment);

		if (fragment != null)
		{
			// update profile, this rewrite profile in profileList
			fragment.dataWrapper.updateProfile(profile);
			
			boolean newProfile = ((newProfileMode == EditorProfileListFragment.EDIT_MODE_INSERT) ||
					              (newProfileMode == EditorProfileListFragment.EDIT_MODE_DUPLICATE));
			fragment.updateListView(profile, newProfile);

			Profile activeProfile = fragment.dataWrapper.getActivatedProfile();
			fragment.updateHeader(activeProfile);
			fragment.dataWrapper.getActivateProfileHelper().showNotification(activeProfile);
			fragment.dataWrapper.getActivateProfileHelper().updateWidget();
		}
		onRestartProfilePreferences(profile, newProfileMode);
	}

	public void onFinishProfilePreferencesActionMode() {
		//if (mTwoPane) {
			Fragment fragment = getFragmentManager().findFragmentById(R.id.editor_profile_detail_container);
			if (fragment != null)
				((ProfilePreferencesFragment)fragment).finishActionMode(ProfilePreferencesFragment.BUTTON_CANCEL);
		//}
	}
	
	@Override
	public void onShowActionMode() {
		Fragment fragment = getFragmentManager().findFragmentById(R.id.editor_profile_list);
		if (fragment != null)
			((EditorProfileListFragment)fragment).fabButton.hide();
	}
	
	@Override
	public void onHideActionMode() {
		Fragment fragment = getFragmentManager().findFragmentById(R.id.editor_profile_list);
		if (fragment != null)
			((EditorProfileListFragment)fragment).fabButton.show();
	}

	
	public void onPreferenceAttached(PreferenceScreen root, int xmlId) {
		return;
	}
	
	public static ApplicationsCache getApplicationsCache()
	{
		return applicationsCache;
	}
	
	public static void createApplicationsCache()
	{
		if ((!savedInstanceStateChanged) || (applicationsCache == null))
		{
			if (applicationsCache != null)
				applicationsCache.clearCache(true);
			applicationsCache =  new ApplicationsCache();
		}
	}

	private DataWrapper getDataWrapper()
	{
		EditorProfileListFragment fragment = (EditorProfileListFragment)getFragmentManager().findFragmentById(R.id.editor_profile_list);
		if (fragment != null)
			return ((EditorProfileListFragment)fragment).dataWrapper;
		else
			return null;
	}

	public void refreshGUI()
	{
		EditorProfileListFragment fragment = (EditorProfileListFragment)getFragmentManager().findFragmentById(R.id.editor_profile_list);
		if (fragment != null)
			fragment.refreshGUI();
	}

}
