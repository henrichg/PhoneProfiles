package sk.henrichg.phoneprofiles;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.readystatesoftware.systembartint.SystemBarTintManager;

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

import sk.henrichg.phoneprofiles.EditorProfileListFragment.OnStartProfilePreferences;
import sk.henrichg.phoneprofiles.ProfileDetailsFragment.OnStartProfilePreferencesFromDetail;

public class EditorProfilesActivity extends AppCompatActivity
                                    implements OnStartProfilePreferences,
                                               OnStartProfilePreferencesFromDetail
{

    private static EditorProfilesActivity instance;

    private static boolean savedInstanceStateChanged;

    private static ApplicationsCache applicationsCache;

    private static final String SP_PROFILE_DETAILS_PROFILE_ID = "profile_detail_profile_id";
    private static final String SP_PROFILE_DETAILS_EDIT_MODE = "profile_detail_edit_mode";
    private static final String SP_PROFILE_DETAILS_PREDEFINED_PROFILE_INDEX = "profile_detali_predefined_profile_index";

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    public static boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        GUIData.setTheme(this, false, false);
        GUIData.setLanguage(getBaseContext());

        super.onCreate(savedInstanceState);

        instance = this;

        savedInstanceStateChanged = (savedInstanceState != null);

        createApplicationsCache();

        setContentView(R.layout.activity_editor_profile_list);

        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) && (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)) {
            Window w = getWindow(); // in Activity's onCreate() for instance
            //w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            // create our manager instance after the content view is set
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            // enable status bar tint
            tintManager.setStatusBarTintEnabled(true);
            // set a custom tint color for status bar
            if (GlobalData.applicationTheme.equals("material"))
                tintManager.setStatusBarTintColor(Color.parseColor("#ff237e9f"));
            else
                tintManager.setStatusBarTintColor(Color.parseColor("#ff202020"));
        }

        // add profile list into list container
        EditorProfileListFragment listFragment = new EditorProfileListFragment();
        getFragmentManager().beginTransaction()
            .replace(R.id.editor_profile_list, listFragment, "EditorProfileListFragment").commit();

        if (findViewById(R.id.editor_profile_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            if (savedInstanceState == null) {
                Fragment fragment = getFragmentManager().findFragmentById(R.id.editor_profile_detail_container);
                if (fragment != null) {
                    getFragmentManager().beginTransaction()
                            .remove(fragment).commit();
                }
            }
            else {
                SharedPreferences preferences = getSharedPreferences(GlobalData.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
                long profile_id = preferences.getLong(SP_PROFILE_DETAILS_PROFILE_ID, 0);
                int editMode = preferences.getInt(SP_PROFILE_DETAILS_EDIT_MODE, EditorProfileListFragment.EDIT_MODE_UNDEFINED);
                int predefinedProfileIndex = preferences.getInt(SP_PROFILE_DETAILS_PREDEFINED_PROFILE_INDEX, 0);
                Bundle arguments = new Bundle();
                arguments.putLong(GlobalData.EXTRA_PROFILE_ID, profile_id);
                arguments.putInt(GlobalData.EXTRA_NEW_PROFILE_MODE, editMode);
                arguments.putInt(GlobalData.EXTRA_PREDEFINED_PROFILE_INDEX, predefinedProfileIndex);
                ProfileDetailsFragment fragment = new ProfileDetailsFragment();
                fragment.setArguments(arguments);
                getFragmentManager().beginTransaction()
                        .replace(R.id.editor_profile_detail_container, fragment, "ProfileDetailsFragment").commit();
            }
        }
        else
        {
            mTwoPane = false;
            FragmentManager fragmentManager = getFragmentManager();
            Fragment fragment = fragmentManager.findFragmentByTag("ProfileDetailsFragment");
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


    }

    public static EditorProfilesActivity getInstance()
    {
        return instance;
    }

    @Override
    protected void onStart()
    {
        super.onStart();
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        if (instance == this)
            instance = null;
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if (instance == null)
        {
            instance = this;
            refreshGUI(false);
        }
    }

    @Override
    protected void onDestroy()
    {
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

        boolean toneInstalled = FirstStartService.isToneInstalled(FirstStartService.TONE_ID, getApplicationContext());

        MenuItem menuItem = menu.findItem(R.id.menu_install_tone);
        if ((menuItem != null) && toneInstalled)
        {
            menuItem.setVisible(false);
        }

        boolean isPPHInstalled = PhoneProfilesHelper.isPPHelperInstalled(getApplicationContext(), PhoneProfilesHelper.PPHELPER_CURRENT_VERSION);

        menuItem = menu.findItem(R.id.menu_pphelper_uninstall);
        if (menuItem != null)
        {
            menuItem.setVisible(PhoneProfilesHelper.PPHelperVersion != -1);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    public static void exitApp(Context context, DataWrapper dataWrapper) {
        GlobalData.setApplicationStarted(context, false);

        // remove alarm for profile duration
        ProfileDurationAlarmBroadcastReceiver.removeAlarm(context);
        GlobalData.setActivatedProfileForDuration(context, 0);

        // zrusenie notifikacie
        dataWrapper.getActivateProfileHelper().removeNotification();
        ImportantInfoNotification.removeNotification(context);
        Permissions.removeNotifications(context);

        context.stopService(new Intent(context, PhoneProfilesService.class));
        context.stopService(new Intent(context, KeyguardService.class));

        ActivateProfileHelper.screenTimeoutUnlock(context);
        ActivateProfileHelper.removeBrightnessView(context);

        GlobalData.initRoot();

        GlobalData.setShowRequestAccessNotificationPolicyPermission(context.getApplicationContext(), true);
        GlobalData.setShowRequestWriteSettingsPermission(context.getApplicationContext(), true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Intent intent;

        switch (item.getItemId()) {
        case R.id.menu_settings:
            intent = new Intent(getBaseContext(), PhoneProfilesPreferencesActivity.class);

            startActivityForResult(intent, GlobalData.REQUEST_CODE_APPLICATION_PREFERENCES);

            return true;
        case R.id.menu_install_tone:
            FirstStartService.installTone(FirstStartService.TONE_ID, FirstStartService.TONE_NAME, getApplicationContext(), true);
            return true;
        case R.id.menu_pphelper_uninstall:
            PhoneProfilesHelper.uninstallPPHelper(this);
            return true;
        case R.id.menu_export:
            exportData();
            return true;
        case R.id.menu_import:
            importData();
            return true;
        case R.id.important_info:
            intent = new Intent(getBaseContext(), ImportantInfoActivity.class);
            startActivity(intent);
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
        case R.id.menu_about:
            intent = new Intent(getBaseContext(), AboutApplicationActivity.class);
            startActivity(intent);
            return true;
        case R.id.menu_exit:
            exitApp(getApplicationContext(), getDataWrapper());

            Handler handler=new Handler();
            Runnable r=new Runnable() {
                public void run() {
                    finish();
                }
            };
            handler.postDelayed(r, 500);

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

            FragmentManager fragmentManager = getFragmentManager();
            Fragment fragment = fragmentManager.findFragmentByTag("ProfileDetailsFragment");
            if (fragment != null)
            {
                Editor editor = preferences.edit();
                editor.putLong(SP_PROFILE_DETAILS_PROFILE_ID, ((ProfileDetailsFragment) fragment).profile_id);
                editor.putInt(SP_PROFILE_DETAILS_EDIT_MODE, ((ProfileDetailsFragment) fragment).editMode);
                editor.putInt(SP_PROFILE_DETAILS_PREDEFINED_PROFILE_INDEX, ((ProfileDetailsFragment) fragment).predefinedProfileIndex);
                editor.commit();
            }
        }
    }

    /*
    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);

        getBaseContext().getResources().updateConfiguration(newConfig, getBaseContext().getResources().getDisplayMetrics());
        GUIData.reloadActivity(this, false);
    }
    */

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
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
                int predefinedProfileIndex = data.getIntExtra(GlobalData.EXTRA_PREDEFINED_PROFILE_INDEX, 0);

                if (profile_id > 0)
                {
                    Profile profile = getDataWrapper().getDatabaseHandler().getProfile(profile_id);
                    // generate bitmaps
                    profile.generateIconBitmap(getBaseContext(), false, 0);
                    profile.generatePreferencesIndicator(getBaseContext(), false, 0);

                    // redraw list fragment , notifications, widgets after finish ProfilePreferencesFragmentActivity
                    redrawProfileListFragment(profile, newProfileMode, predefinedProfileIndex);

                    Profile mappedProfile = GlobalData.getMappedProfile(profile, getApplicationContext());
                    Permissions.grantProfilePermissions(getApplicationContext(), mappedProfile, false,
                            true, false, 0, GlobalData.STARTUP_SOURCE_EDITOR, true, this, false);
                }
                else
                if (profile_id == GlobalData.DEFAULT_PROFILE_ID)
                {
                    // refresh activity for changes of default profile
                    GUIData.reloadActivity(this, false);

                    Profile defaultProfile = GlobalData.getDefaultProfile(getApplicationContext());
                    Permissions.grantProfilePermissions(getApplicationContext(), defaultProfile, false,
                            true, false, 0, GlobalData.STARTUP_SOURCE_EDITOR, true, this, false);

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
            if (resultCode == RESULT_OK)
            {
                doImportData(GUIData.REMOTE_EXPORT_PATH);
            }
        }
    }

    private void importExportErrorDialog(int importExport)
    {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        int resString;
        if (importExport == 1)
            resString = R.string.import_profiles_alert_title;
        else
            resString = R.string.export_profiles_alert_title;
        dialogBuilder.setTitle(resString);
        if (importExport == 1)
            resString = R.string.import_profiles_alert_error;
        else
            resString = R.string.export_profiles_alert_error;
        dialogBuilder.setMessage(resString);
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

    public void doImportData(String applicationDataPath)
    {
        final EditorProfilesActivity activity = this;
        final String _applicationDataPath = applicationDataPath;

        if (Permissions.grantImportPermissions(activity.getApplicationContext(), activity, applicationDataPath)) {

            class ImportAsyncTask extends AsyncTask<Void, Integer, Integer> {
                private MaterialDialog dialog;
                private DataWrapper dataWrapper;

                ImportAsyncTask() {
                    this.dialog = new MaterialDialog.Builder(activity)
                            .content(R.string.import_profiles_alert_title)
                                    //.disableDefaultFonts()
                            .progress(true, 0)
                            .build();

                    this.dataWrapper = getDataWrapper();
                }

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();

                    lockScreenOrientation();
                    this.dialog.setCancelable(false);
                    this.dialog.setCanceledOnTouchOutside(false);
                    this.dialog.show();

                    EditorProfileListFragment fragment = (EditorProfileListFragment) getFragmentManager().findFragmentById(R.id.editor_profile_list);
                    if (fragment != null)
                        ((EditorProfileListFragment) fragment).removeAdapter();
                }

                @Override
                protected Integer doInBackground(Void... params) {
                    int ret = dataWrapper.getDatabaseHandler().importDB(_applicationDataPath);

                    if (ret == 1) {
                        // check for hardware capability and update data
                        ret = dataWrapper.getDatabaseHandler().disableNotAllowedPreferences(activity.getApplicationContext());
                    }
                    if (ret == 1) {
                        File sd = Environment.getExternalStorageDirectory();
                        File exportFile = new File(sd, _applicationDataPath + "/" + GUIData.EXPORT_APP_PREF_FILENAME);
                        if (!importApplicationPreferences(exportFile, 1))
                            ret = 0;
                        else {
                            exportFile = new File(sd, _applicationDataPath + "/" + GUIData.EXPORT_DEF_PROFILE_PREF_FILENAME);
                            if (!importApplicationPreferences(exportFile, 2))
                                ret = 0;
                        }
                    }

                    return ret;
                }

                @Override
                protected void onPostExecute(Integer result) {
                    super.onPostExecute(result);

                    if (dialog.isShowing())
                        dialog.dismiss();
                    unlockScreenOrientation();

                    if (result == 1) {
                        GlobalData.loadPreferences(getApplicationContext());

                        dataWrapper.clearProfileList();
                        dataWrapper.getDatabaseHandler().deactivateProfile();
                        dataWrapper.getActivateProfileHelper().showNotification(null);
                        dataWrapper.getActivateProfileHelper().updateWidget();

                        GlobalData.setShowRequestAccessNotificationPolicyPermission(getApplicationContext(), true);
                        GlobalData.setShowRequestWriteSettingsPermission(getApplicationContext(), true);

                        // toast notification
                        Toast msg = Toast.makeText(getApplicationContext(),
                                getResources().getString(R.string.toast_import_ok),
                                Toast.LENGTH_SHORT);
                        msg.show();

                        // refresh activity
                        GUIData.reloadActivity(activity, true);

                    } else {
                        importExportErrorDialog(1);
                    }
                }

                private void lockScreenOrientation() {
                    int currentOrientation = activity.getResources().getConfiguration().orientation;
                    if (currentOrientation == Configuration.ORIENTATION_PORTRAIT) {
                        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
                    } else {
                        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                    }
                }

                private void unlockScreenOrientation() {
                    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
                }


            }

            new ImportAsyncTask().execute();

        }
    }

    private void importDataAlert(boolean remoteExport)
    {
        final boolean _remoteExport = remoteExport;

        AlertDialog.Builder dialogBuilder2 = new AlertDialog.Builder(this);
        /*if (remoteExport)
        {
            dialogBuilder2.setTitle(getResources().getString(R.string.import_profiles_from_phoneprofilesplus_alert_title2));
            dialogBuilder2.setMessage(getResources().getString(R.string.import_profiles_alert_message));
            //dialogBuilder2.setIcon(android.R.drawable.ic_dialog_alert);
        }
        else
        {*/
            dialogBuilder2.setTitle(getResources().getString(R.string.import_profiles_alert_title));
            dialogBuilder2.setMessage(getResources().getString(R.string.import_profiles_alert_message));
            //dialogBuilder2.setIcon(android.R.drawable.ic_dialog_alert);
        //}

        dialogBuilder2.setPositiveButton(R.string.alert_button_yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (_remoteExport) {
                    // start RemoteExportDataActivity
                    Intent intent = new Intent("phoneprofilesplus.intent.action.EXPORTDATA");

                    final PackageManager packageManager = getPackageManager();
                    List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                    if (list.size() > 0)
                        startActivityForResult(intent, GlobalData.REQUEST_CODE_REMOTE_EXPORT);
                    else
                        importExportErrorDialog(1);
                } else
                    doImportData(GlobalData.EXPORT_PATH);
            }
        });
        dialogBuilder2.setNegativeButton(R.string.alert_button_no, null);
        dialogBuilder2.show();
    }

    private void importData()
    {
        /*
        // test whether the PhoneProfilePlus is installed
        PackageManager packageManager = getApplicationContext().getPackageManager();
        Intent phoneProfiles = packageManager.getLaunchIntentForPackage("sk.henrichg.phoneprofilesplus");
        if (phoneProfiles != null)
        {
            // PhoneProfilesPlus is istalled

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            dialogBuilder.setTitle(R.string.import_profiles_from_phoneprofilesplus_alert_title);
            dialogBuilder.setMessage(R.string.import_profiles_from_phoneprofilesplus_alert_message);
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
        else */
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
        final Activity activity = this;

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle(R.string.export_profiles_alert_title);
        dialogBuilder.setMessage(R.string.export_profiles_alert_message);
        //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);

        dialogBuilder.setPositiveButton(R.string.alert_button_yes, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                doExportData();
            }
        });
        dialogBuilder.setNegativeButton(R.string.alert_button_no, null);
        dialogBuilder.show();
    }

    public void doExportData()
    {
        final EditorProfilesActivity activity = this;

        if (Permissions.grantExportPermissions(activity.getApplicationContext(), activity)) {

            class ExportAsyncTask extends AsyncTask<Void, Integer, Integer> {
                private MaterialDialog dialog;
                private DataWrapper dataWrapper;

                ExportAsyncTask() {
                    this.dialog = new MaterialDialog.Builder(activity)
                            .content(R.string.export_profiles_alert_title)
                                    //.disableDefaultFonts()
                            .progress(true, 0)
                            .build();
                    this.dataWrapper = getDataWrapper();
                }

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();

                    this.dialog.setCancelable(false);
                    this.dialog.setCanceledOnTouchOutside(false);
                    this.dialog.show();
                }

                @Override
                protected Integer doInBackground(Void... params) {

                    int ret = dataWrapper.getDatabaseHandler().exportDB();
                    if (ret == 1) {
                        File sd = Environment.getExternalStorageDirectory();
                        File exportFile = new File(sd, GlobalData.EXPORT_PATH + "/" + GUIData.EXPORT_APP_PREF_FILENAME);
                        if (!exportApplicationPreferences(exportFile, 1))
                            ret = 0;
                        else {
                            exportFile = new File(sd, GlobalData.EXPORT_PATH + "/" + GUIData.EXPORT_DEF_PROFILE_PREF_FILENAME);
                            if (!exportApplicationPreferences(exportFile, 2))
                                ret = 0;
                        }
                    }

                    return ret;
                }

                @Override
                protected void onPostExecute(Integer result) {
                    super.onPostExecute(result);

                    if (dialog.isShowing())
                        dialog.dismiss();

                    if (result == 1) {

                        // toast notification
                        Toast msg = Toast.makeText(getApplicationContext(),
                                getResources().getString(R.string.toast_export_ok),
                                Toast.LENGTH_SHORT);
                        msg.show();

                    } else {
                        importExportErrorDialog(2);
                    }
                }

            }

            new ExportAsyncTask().execute();

        }
    }

    private void startProfilePreferenceActivity(Profile profile, int editMode, int predefinedProfileIndex) {
        Intent intent = new Intent(getBaseContext(), ProfilePreferencesFragmentActivity.class);
        if (editMode == EditorProfileListFragment.EDIT_MODE_INSERT)
            intent.putExtra(GlobalData.EXTRA_PROFILE_ID, 0L);
        else
            intent.putExtra(GlobalData.EXTRA_PROFILE_ID, profile._id);
        intent.putExtra(GlobalData.EXTRA_NEW_PROFILE_MODE, editMode);
        intent.putExtra(GlobalData.EXTRA_PREDEFINED_PROFILE_INDEX, predefinedProfileIndex);
        startActivityForResult(intent, GlobalData.REQUEST_CODE_PROFILE_PREFERENCES);
    }

    public void onStartProfilePreferences(Profile profile, int editMode, int predefinedProfileIndex) {

        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.

            if ((editMode == EditorProfileListFragment.EDIT_MODE_INSERT) ||
                (editMode == EditorProfileListFragment.EDIT_MODE_DUPLICATE)) {
                startProfilePreferenceActivity(profile, editMode, predefinedProfileIndex);
            }
            else
            if (profile != null)
            {
                Bundle arguments = new Bundle();
                arguments.putLong(GlobalData.EXTRA_PROFILE_ID, profile._id);
                arguments.putInt(GlobalData.EXTRA_NEW_PROFILE_MODE, editMode);
                arguments.putInt(GlobalData.EXTRA_PREDEFINED_PROFILE_INDEX, predefinedProfileIndex);
                ProfileDetailsFragment fragment = new ProfileDetailsFragment();
                fragment.setArguments(arguments);
                getFragmentManager().beginTransaction()
                        .replace(R.id.editor_profile_detail_container, fragment, "ProfileDetailsFragment").commit();

            }
            else
            {
                Fragment fragment = getFragmentManager().findFragmentById(R.id.editor_profile_detail_container);
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
                startProfilePreferenceActivity(profile, editMode, predefinedProfileIndex);
        }
    }

    @Override
    public void onStartProfilePreferencesFromDetail(Profile profile) {
        startProfilePreferenceActivity(profile, EditorProfileListFragment.EDIT_MODE_EDIT, 0);
    }

    public void redrawProfilePreferences(Profile profile, int newProfileMode, int predefinedProfileIndex) {
        if (mTwoPane) {
            if (profile != null)
            {
                // restart profile preferences fragmentu
                Bundle arguments = new Bundle();
                arguments.putLong(GlobalData.EXTRA_PROFILE_ID, profile._id);
                arguments.putInt(GlobalData.EXTRA_NEW_PROFILE_MODE, newProfileMode);
                arguments.putInt(GlobalData.EXTRA_PREDEFINED_PROFILE_INDEX, predefinedProfileIndex);
                ProfileDetailsFragment fragment = new ProfileDetailsFragment();
                fragment.setArguments(arguments);
                getFragmentManager().beginTransaction()
                        .replace(R.id.editor_profile_detail_container, fragment, "ProfileDetailsFragment").commit();
            }
            else
            {
                Fragment fragment = getFragmentManager().findFragmentById(R.id.editor_profile_detail_container);
                if (fragment != null)
                {
                    getFragmentManager().beginTransaction()
                            .remove(fragment).commit();
                }
            }
        }
    }

    public void redrawProfileListFragment(Profile profile, int newProfileMode, int predefinedProfileIndex)
    {
        // redraw headeru list fragmentu, notifikacie a widgetov
        EditorProfileListFragment fragment = (EditorProfileListFragment)getFragmentManager().findFragmentById(R.id.editor_profile_list);

        if (fragment != null)
        {
            // update profile, this rewrite profile in profileList
            fragment.dataWrapper.updateProfile(profile);

            boolean newProfile = ((newProfileMode == EditorProfileListFragment.EDIT_MODE_INSERT) ||
                                  (newProfileMode == EditorProfileListFragment.EDIT_MODE_DUPLICATE));
            fragment.updateListView(profile, newProfile, false);

            Profile activeProfile = fragment.dataWrapper.getActivatedProfile();
            fragment.updateHeader(activeProfile);
            fragment.dataWrapper.getActivateProfileHelper().showNotification(activeProfile);
            fragment.dataWrapper.getActivateProfileHelper().updateWidget();
        }
        redrawProfilePreferences(profile, newProfileMode, predefinedProfileIndex);
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

    public void refreshGUI(boolean refreshIcons)
    {
        EditorProfileListFragment fragment = (EditorProfileListFragment)getFragmentManager().findFragmentById(R.id.editor_profile_list);
        if (fragment != null)
            fragment.refreshGUI(refreshIcons);
    }

}
