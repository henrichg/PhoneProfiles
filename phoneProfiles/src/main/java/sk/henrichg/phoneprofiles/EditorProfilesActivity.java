package sk.henrichg.phoneprofiles;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import me.drakeet.support.toast.ToastCompat;
import sk.henrichg.phoneprofiles.EditorProfileListFragment.OnStartProfilePreferences;
import sk.henrichg.phoneprofiles.ProfileDetailsFragment.OnStartProfilePreferencesFromDetail;

public class EditorProfilesActivity extends AppCompatActivity
                                    implements OnStartProfilePreferences,
                                               OnStartProfilePreferencesFromDetail
{

    //private static volatile EditorProfilesActivity instance;

    private static boolean savedInstanceStateChanged;

    private static ApplicationsCache applicationsCache;

    private AsyncTask importAsyncTask = null;
    private AsyncTask exportAsyncTask = null;
    static boolean doImport = false;
    private AlertDialog importProgressDialog = null;
    private AlertDialog exportProgressDialog = null;

    private static final String SP_PROFILE_DETAILS_PROFILE_ID = "profile_detail_profile_id";
    private static final String SP_PROFILE_DETAILS_EDIT_MODE = "profile_detail_edit_mode";
    private static final String SP_PROFILE_DETAILS_PREDEFINED_PROFILE_INDEX = "profile_detail_predefined_profile_index";

    public boolean targetHelpsSequenceStarted;
    public static final String PREF_START_TARGET_HELPS = "editor_profiles_activity_start_target_helps";

    static final String EXTRA_NEW_PROFILE_MODE = "new_profile_mode";
    static final String EXTRA_PREDEFINED_PROFILE_INDEX = "predefined_profile_index";

    // request code for startActivityForResult with intent BackgroundActivateProfileActivity
    static final int REQUEST_CODE_ACTIVATE_PROFILE = 6220;
    // request code for startActivityForResult with intent ProfilePreferencesActivity
    static final int REQUEST_CODE_PROFILE_PREFERENCES = 6221;
    // request code for startActivityForResult with intent PhoneProfilesActivity
    private static final int REQUEST_CODE_APPLICATION_PREFERENCES = 6229;
    // request code for startActivityForResult with intent "phoneprofiles.intent.action.EXPORTDATA"
    private static final int REQUEST_CODE_REMOTE_EXPORT = 6250;

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private static boolean mTwoPane;

    private Toolbar editorToolbar;

    AddProfileDialog addProfileDialog;

    private final BroadcastReceiver refreshGUIBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent ) {
            boolean refreshIcons = intent.getBooleanExtra(RefreshActivitiesBroadcastReceiver.EXTRA_REFRESH_ICONS, false);
            // not change selection in editor if refresh is outside editor
            EditorProfilesActivity.this.refreshGUI(refreshIcons, false);
        }
    };

    private final BroadcastReceiver showTargetHelpsBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive( Context context, Intent intent ) {
            Fragment fragment = EditorProfilesActivity.this.getSupportFragmentManager().findFragmentById(R.id.editor_profile_list);
            if (fragment != null) {
                ((EditorProfileListFragment) fragment).showTargetHelps();
            }
        }
    };

    private final BroadcastReceiver finishBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive( Context context, Intent intent ) {
            EditorProfilesActivity.this.finish();
        }
    };

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        GlobalGUIRoutines.setTheme(this, false, true);
        GlobalGUIRoutines.setLanguage(getBaseContext());

        super.onCreate(savedInstanceState);

        /*synchronized (EditorProfilesActivity.class) {
            instance = this;
        }*/

        savedInstanceStateChanged = (savedInstanceState != null);

        createApplicationsCache();

        setContentView(R.layout.activity_editor_profile_list);

        if (/*(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) &&*/ (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)) {
            Window w = getWindow(); // in Activity's onCreate() for instance
            //w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            // create our manager instance after the content view is set
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            // enable status bar tint
            tintManager.setStatusBarTintEnabled(true);
            // set a custom tint color for status bar
            switch (ApplicationPreferences.applicationTheme(getApplicationContext(), true)) {
                case "color":
                    tintManager.setStatusBarTintColor(ContextCompat.getColor(getBaseContext(), R.color.primary));
                    break;
                case "white":
                    tintManager.setStatusBarTintColor(ContextCompat.getColor(getBaseContext(), R.color.primaryDark19_white));
                    break;
                default:
                    tintManager.setStatusBarTintColor(ContextCompat.getColor(getBaseContext(), R.color.primary_dark));
                    break;
            }
        }

        // add profile list into list container
        EditorProfileListFragment listFragment = new EditorProfileListFragment();
        Bundle arguments = new Bundle();
        arguments.putBoolean(EditorProfileListFragment.START_TARGET_HELPS_ARGUMENT, false);
        listFragment.setArguments(arguments);
        getSupportFragmentManager().beginTransaction()
            .replace(R.id.editor_profile_list, listFragment, "EditorProfileListFragment").commit();

        if (findViewById(R.id.editor_profile_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            if (savedInstanceState == null) {
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.editor_profile_detail_container);
                if (fragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .remove(fragment).commit();
                }
            }
            else {
                ApplicationPreferences.getSharedPreferences(this);
                long profile_id = ApplicationPreferences.preferences.getLong(SP_PROFILE_DETAILS_PROFILE_ID, 0);
                int editMode = ApplicationPreferences.preferences.getInt(SP_PROFILE_DETAILS_EDIT_MODE, EditorProfileListFragment.EDIT_MODE_UNDEFINED);
                int predefinedProfileIndex = ApplicationPreferences.preferences.getInt(SP_PROFILE_DETAILS_PREDEFINED_PROFILE_INDEX, 0);
                arguments = new Bundle();
                arguments.putLong(PPApplication.EXTRA_PROFILE_ID, profile_id);
                arguments.putInt(EditorProfilesActivity.EXTRA_NEW_PROFILE_MODE, editMode);
                arguments.putInt(EditorProfilesActivity.EXTRA_PREDEFINED_PROFILE_INDEX, predefinedProfileIndex);
                arguments.putBoolean(EditorProfileListFragment.START_TARGET_HELPS_ARGUMENT, false);
                ProfileDetailsFragment fragment = new ProfileDetailsFragment();
                fragment.setArguments(arguments);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.editor_profile_detail_container, fragment, "ProfileDetailsFragment").commit();
            }
        }
        else
        {
            mTwoPane = false;
            FragmentManager fragmentManager = getSupportFragmentManager();
            Fragment fragment = fragmentManager.findFragmentByTag("ProfileDetailsFragment");
            if (fragment != null)
                fragmentManager.beginTransaction()
                .remove(fragment).commit();
            fragmentManager.executePendingTransactions();
        }

        editorToolbar = findViewById(R.id.editor_toolbar);
        //editorToolbar.inflateMenu(R.menu.editor_top_bar);
        setSupportActionBar(editorToolbar);

        if (getSupportActionBar() != null) {
            //getSupportActionBar().setHomeButtonEnabled(true);
            //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.title_activity_editor);
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(refreshGUIBroadcastReceiver,
                new IntentFilter("RefreshEditorGUIBroadcastReceiver"));
        LocalBroadcastManager.getInstance(this).registerReceiver(showTargetHelpsBroadcastReceiver,
                new IntentFilter("ShowEditorTargetHelpsBroadcastReceiver"));

        refreshGUI(false, true);

        LocalBroadcastManager.getInstance(this).registerReceiver(finishBroadcastReceiver,
                new IntentFilter("FinishEditorBroadcastReceiver"));
    }

    /*public static EditorProfilesActivity getInstance()
    {
        return instance;
    }*/

    @Override
    protected void onStart()
    {
        super.onStart();

        // this is for list widget header
        if (!PPApplication.getApplicationStarted(getApplicationContext(), true))
        {
            // start PhoneProfilesService
            //PPApplication.firstStartServiceStarted = false;
            PPApplication.setApplicationStarted(getApplicationContext(), true);
            Intent serviceIntent = new Intent(getApplicationContext(), PhoneProfilesService.class);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_INITIALIZE_START, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ACTIVATE_PROFILES, true);
            PPApplication.startPPService(this, serviceIntent);
        }
        else
        {
            if ((PhoneProfilesService.getInstance() == null) || (!PhoneProfilesService.getInstance().getServiceHasFirstStart())) {
                // start PhoneProfilesService
                //PPApplication.firstStartServiceStarted = false;
                Intent serviceIntent = new Intent(getApplicationContext(), PhoneProfilesService.class);
                serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, true);
                serviceIntent.putExtra(PhoneProfilesService.EXTRA_INITIALIZE_START, true);
                serviceIntent.putExtra(PhoneProfilesService.EXTRA_ACTIVATE_PROFILES, false);
                PPApplication.startPPService(this, serviceIntent);
            }
        }
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        if ((addProfileDialog != null) && (addProfileDialog.mDialog != null) && addProfileDialog.mDialog.isShowing())
            addProfileDialog.mDialog.dismiss();

        /*synchronized (EditorProfilesActivity.class) {
            instance = null;
        }*/
    }

    /*
    @Override
    protected void onResume()
    {
        super.onResume();

        if (EditorProfilesActivity.getInstance() == null)
        {
            synchronized (EditorProfilesActivity.class) {
                instance = this;
            }
            refreshGUI(false, false);
        }
    }
    */

    @Override
    protected void onDestroy()
    {
        if ((importProgressDialog != null) && importProgressDialog.isShowing()) {
            importProgressDialog.dismiss();
            importProgressDialog = null;
        }
        if ((exportProgressDialog != null) && exportProgressDialog.isShowing()) {
            exportProgressDialog.dismiss();
            exportProgressDialog = null;
        }
        if ((importAsyncTask != null) && !importAsyncTask.getStatus().equals(AsyncTask.Status.FINISHED)){
            importAsyncTask.cancel(true);
            doImport = false;
        }
        if ((exportAsyncTask != null) && !exportAsyncTask.getStatus().equals(AsyncTask.Status.FINISHED)){
            exportAsyncTask.cancel(true);
        }

        if (!savedInstanceStateChanged)
        {
            // no destroy applicationsCache on orientation change
            if (applicationsCache != null)
                applicationsCache.clearCache(true);
            applicationsCache = null;
        }

        LocalBroadcastManager.getInstance(this).unregisterReceiver(refreshGUIBroadcastReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(showTargetHelpsBroadcastReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(finishBroadcastReceiver);

        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        editorToolbar.inflateMenu(R.menu.editor_top_bar);
        return true;
    }

    private static void onNextLayout(final View view, final Runnable runnable) {
        final ViewTreeObserver observer = view.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                final ViewTreeObserver trueObserver;

                if (observer.isAlive()) {
                    trueObserver = observer;
                } else {
                    trueObserver = view.getViewTreeObserver();
                }

                trueObserver.removeOnGlobalLayoutListener(this);

                runnable.run();
            }
        });
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean ret = super.onPrepareOptionsMenu(menu);

        MenuItem menuItem = menu.findItem(R.id.menu_dark_theme);
        if (menuItem != null)
        {
            String appTheme = ApplicationPreferences.applicationTheme(getApplicationContext(), false);
            if (!appTheme.equals("night_mode")) {
                menuItem.setVisible(true);
                if (appTheme.equals("dark"))
                    menuItem.setTitle(R.string.menu_dark_theme_off);
                else
                    menuItem.setTitle(R.string.menu_dark_theme_on);
            }
            else
                menuItem.setVisible(false);
        }

        onNextLayout(editorToolbar, new Runnable() {
            @Override
            public void run() {
                showTargetHelps();
            }
        });

        return ret;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Intent intent;

        switch (item.getItemId()) {
            case R.id.menu_settings:
                intent = new Intent(getBaseContext(), PhoneProfilesPreferencesActivity.class);

                startActivityForResult(intent, REQUEST_CODE_APPLICATION_PREFERENCES);

                return true;
            case R.id.menu_dark_theme:
                String theme = ApplicationPreferences.applicationTheme(getApplicationContext(), false);
                if (!theme.equals("night_mode")) {
                    if (theme.equals("dark")) {
                        SharedPreferences preferences = ApplicationPreferences.getSharedPreferences(getApplicationContext());
                        theme = preferences.getString(ApplicationPreferences.PREF_APPLICATION_NOT_DARK_THEME, "color");
                        Editor editor = preferences.edit();
                        editor.putString(ApplicationPreferences.PREF_APPLICATION_THEME, theme);
                        editor.apply();
                    } else {
                        SharedPreferences preferences = ApplicationPreferences.getSharedPreferences(getApplicationContext());
                        Editor editor = preferences.edit();
                        editor.putString(ApplicationPreferences.PREF_APPLICATION_NOT_DARK_THEME, theme);
                        editor.putString(ApplicationPreferences.PREF_APPLICATION_THEME, "dark");
                        editor.apply();
                    }
                    GlobalGUIRoutines.reloadActivity(this, true);
                }
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
                    ToastCompat.makeText(getApplicationContext(), "No application can handle this request."
                        + " Please install a web browser",  Toast.LENGTH_LONG).show();
                }
                return true;*/
            case R.id.menu_about:
                intent = new Intent(getBaseContext(), AboutApplicationActivity.class);
                startActivity(intent);
                return true;
            case R.id.menu_exit:
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
                dialogBuilder.setTitle(R.string.exit_application_alert_title);
                dialogBuilder.setMessage(R.string.exit_application_alert_message);
                //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                dialogBuilder.setPositiveButton(R.string.alert_button_yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        PPApplication.exitApp(getApplicationContext(), /*getDataWrapper(),*/ EditorProfilesActivity.this,
                                false/*, true*/);
                    }
                });
                dialogBuilder.setNegativeButton(R.string.alert_button_no, null);
                AlertDialog dialog = dialogBuilder.create();
                /*dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                        if (positive != null) positive.setAllCaps(false);
                        Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                        if (negative != null) negative.setAllCaps(false);
                    }
                });*/
                if (!isFinishing())
                    dialog.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /*
    // fix for bug in LG stock ROM Android <= 4.1
    // https://code.google.com/p/android/issues/detail?id=78154
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        String manufacturer = PPApplication.getROMManufacturer();
        if ((keyCode == KeyEvent.KEYCODE_MENU) &&
            (Build.VERSION.SDK_INT <= 16) &&
            (manufacturer != null) && (manufacturer.compareTo("lge") == 0)) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        String manufacturer = PPApplication.getROMManufacturer();
        if ((keyCode == KeyEvent.KEYCODE_MENU) &&
            (Build.VERSION.SDK_INT <= 16) &&
            (manufacturer != null) && (manufacturer.compareTo("lge") == 0)) {
            openOptionsMenu();
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }
    /////
    */

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        savedInstanceStateChanged = true;

        if (mTwoPane) {
            ApplicationPreferences.getSharedPreferences(this);

            FragmentManager fragmentManager = getSupportFragmentManager();
            Fragment fragment = fragmentManager.findFragmentByTag("ProfileDetailsFragment");
            if (fragment != null)
            {
                Editor editor = ApplicationPreferences.preferences.edit();
                editor.putLong(SP_PROFILE_DETAILS_PROFILE_ID, ((ProfileDetailsFragment) fragment).profile_id);
                //editor.putInt(SP_PROFILE_DETAILS_EDIT_MODE, ((ProfileDetailsFragment) fragment).editMode);
                //editor.putInt(SP_PROFILE_DETAILS_PREDEFINED_PROFILE_INDEX, ((ProfileDetailsFragment) fragment).predefinedProfileIndex);
                editor.apply();
            }
        }
    }

    /*
    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);

        getBaseContext().getResources().updateConfiguration(newConfig, getBaseContext().getResources().getDisplayMetrics());
        GlobalGUIRoutines.reloadActivity(this, false);
    }
    */

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == REQUEST_CODE_ACTIVATE_PROFILE)
        {
            EditorProfileListFragment fragment = (EditorProfileListFragment)getSupportFragmentManager().findFragmentById(R.id.editor_profile_list);
            if (fragment != null)
                fragment.doOnActivityResult(requestCode, resultCode, data);
        }
        else
        if (requestCode == REQUEST_CODE_PROFILE_PREFERENCES)
        {
            if ((resultCode == RESULT_OK) && (data != null))
            {
                long profile_id = data.getLongExtra(PPApplication.EXTRA_PROFILE_ID, 0);
                int newProfileMode = data.getIntExtra(EditorProfilesActivity.EXTRA_NEW_PROFILE_MODE, EditorProfileListFragment.EDIT_MODE_UNDEFINED);
                int predefinedProfileIndex = data.getIntExtra(EditorProfilesActivity.EXTRA_PREDEFINED_PROFILE_INDEX, 0);

                if (profile_id > 0)
                {
                    //noinspection ConstantConditions
                    Profile profile = DatabaseHandler.getInstance(getApplicationContext()).getProfile(profile_id);
                    if (profile != null) {
                        // generate bitmaps
                        profile.generateIconBitmap(getBaseContext(), false, 0, false);
                        profile.generatePreferencesIndicator(getBaseContext(), false, 0);

                        // redraw list fragment , notifications, widgets after finish ProfilePreferencesActivity
                        redrawProfileListFragment(profile, newProfileMode, predefinedProfileIndex/*, true*/);

                        Profile mappedProfile = Profile.getMappedProfile(profile, getApplicationContext());
                        Permissions.grantProfilePermissions(getApplicationContext(), mappedProfile, false,
                                /*true, false, 0,*/ PPApplication.STARTUP_SOURCE_EDITOR, false, false, false);
                    }
                }
                else
                if (profile_id == Profile.SHARED_PROFILE_ID)
                {
                    // refresh activity for changes of default profile
                    GlobalGUIRoutines.reloadActivity(this, false);

                    Profile sharedProfile = Profile.getSharedProfile(getApplicationContext());
                    Permissions.grantProfilePermissions(getApplicationContext(), sharedProfile, false,
                            /*true, false, 0,*/ PPApplication.STARTUP_SOURCE_EDITOR, false, false, false);

                }
            }
            else
            if (data != null) {
                boolean restart = data.getBooleanExtra(PhoneProfilesPreferencesActivity.EXTRA_RESET_EDITOR, false);
                if (restart) {
                    // refresh activity for special changes
                    GlobalGUIRoutines.reloadActivity(this, true);
                }
            }
        }
        else
        if (requestCode == REQUEST_CODE_APPLICATION_PREFERENCES)
        {
            if ((resultCode == RESULT_OK) && (data != null))
            {
                boolean restart = data.getBooleanExtra(PhoneProfilesPreferencesActivity.EXTRA_RESET_EDITOR, false);

                if (restart)
                {
                    // refresh activity for special changes
                    GlobalGUIRoutines.reloadActivity(this, true);
                }
            }
        }
        else
        if (requestCode == REQUEST_CODE_REMOTE_EXPORT)
        {
            if (resultCode == RESULT_OK)
            {
                doImportData(GlobalGUIRoutines.REMOTE_EXPORT_PATH);
            }
        }
        /*else
        if (requestCode == Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_PROFILE) {
            if (data != null) {
                long profileId = data.getLongExtra(PPApplication.EXTRA_PROFILE_ID, 0);
                int startupSource = data.getIntExtra(PPApplication.EXTRA_STARTUP_SOURCE, 0);
                boolean activateProfile = data.getBooleanExtra(Permissions.EXTRA_ACTIVATE_PROFILE, false);

                if (activateProfile && (getDataWrapper() != null)) {
                    Profile profile = getDataWrapper().getProfileById(profileId, false, false);
                    getDataWrapper()._activateProfile(profile, startupSource, this);
                }
            }
        }*/
        else
        if (requestCode == Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_EXPORT) {
            if (resultCode == RESULT_OK) {
                doExportData();
            }
        }
        else
        if (requestCode == Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_IMPORT) {
            if ((resultCode == RESULT_OK) && (data != null)) {
                doImportData(data.getStringExtra(Permissions.EXTRA_APPLICATION_DATA_PATH));
            }
        }
    }

    /*
    private void importExportErrorDialog(int importExport, int result)
    {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        String title;
        if (importExport == 1)
            title = getString(R.string.import_profiles_alert_title);
        else
            title = getString(R.string.export_profiles_alert_title);
        dialogBuilder.setTitle(title);
        String message;
        if (importExport == 1) {
            message = getString(R.string.import_profiles_alert_error);
            if (result == DatabaseHandler.IMPORT_ERROR_NEVER_VERSION)
                message = message + ". " + getString(R.string.import_profiles_alert_error_database_newer_version);
        }
        else
            message = getString(R.string.export_profiles_alert_error);
        dialogBuilder.setMessage(message);
        //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
        dialogBuilder.setPositiveButton(android.R.string.ok, null);
        AlertDialog dialog = dialogBuilder.create();

        //Button positive = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        //if (positive != null) positive.setAllCaps(false);
        //Button negative = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        //if (negative != null) negative.setAllCaps(false);

        dialog.show();
    }
    */
    private void importExportErrorDialog(int importExport, int dbResult, int appSettingsResult, int sharedProfileResult)
    {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        String title;
        if (importExport == 1)
            title = getString(R.string.import_profiles_alert_title);
        else
            title = getString(R.string.export_profiles_alert_title);
        dialogBuilder.setTitle(title);
        String message;
        if (importExport == 1) {
            message = getString(R.string.import_profiles_alert_error) + ":";
            if (dbResult != DatabaseHandler.IMPORT_OK) {
                if (dbResult == DatabaseHandler.IMPORT_ERROR_NEVER_VERSION)
                    message = message + "\n• " + getString(R.string.import_profiles_alert_error_database_newer_version);
                else
                    message = message + "\n• " + getString(R.string.import_profiles_alert_error_database_bug);
            }
            if (appSettingsResult == 0)
                message = message + "\n• " + getString(R.string.import_profiles_alert_error_appSettings_bug);
            if (sharedProfileResult == 0)
                message = message + "\n• " + getString(R.string.import_profiles_alert_error_sharedProfile_bug);
        }
        else
            message = getString(R.string.export_profiles_alert_error);
        dialogBuilder.setMessage(message);
        //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
        dialogBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // refresh activity
                GlobalGUIRoutines.reloadActivity(EditorProfilesActivity.this, true);
            }
        });
        dialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                // refresh activity
                GlobalGUIRoutines.reloadActivity(EditorProfilesActivity.this, true);
            }
        });
        AlertDialog dialog = dialogBuilder.create();
        /*dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                if (positive != null) positive.setAllCaps(false);
                Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                if (negative != null) negative.setAllCaps(false);
            }
        });*/
        if (!isFinishing())
            dialog.show();
    }


    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean importApplicationPreferences(File src, int what) {
        boolean res = true;
        ObjectInputStream input = null;
        try {
            try {
                input = new ObjectInputStream(new FileInputStream(src));
                Editor prefEdit;
                if (what == 1)
                    prefEdit = getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE).edit();
                else
                    prefEdit = getSharedPreferences(PPApplication.SHARED_PROFILE_PREFS_NAME, Activity.MODE_PRIVATE).edit();
                prefEdit.clear();
                //noinspection unchecked
                Map<String, ?> entries = (Map<String, ?>) input.readObject();
                for (Entry<String, ?> entry : entries.entrySet()) {
                    Object v = entry.getValue();
                    String key = entry.getKey();

                    if (v instanceof Boolean)
                        prefEdit.putBoolean(key, (Boolean) v);
                    else if (v instanceof Float)
                        prefEdit.putFloat(key, (Float) v);
                    else if (v instanceof Integer)
                        prefEdit.putInt(key, (Integer) v);
                    else if (v instanceof Long)
                        prefEdit.putLong(key, (Long) v);
                    else if (v instanceof String)
                        prefEdit.putString(key, ((String) v));

                    if (what == 1) {
                        if (key.equals(ApplicationPreferences.PREF_APPLICATION_THEME)) {
                            if (v.equals("light"))
                                prefEdit.putString(key, "white");
                            if (v.equals("material"))
                                prefEdit.putString(key, "color");
                        }
                        if (key.equals(ActivateProfileHelper.PREF_MERGED_RING_NOTIFICATION_VOLUMES))
                            ActivateProfileHelper.setMergedRingNotificationVolumes(getApplicationContext(), true, prefEdit);
                    }

                    if (what == 2) {
                        if (key.equals(Profile.PREF_PROFILE_LOCK_DEVICE)) {
                            if (v.equals("3"))
                                prefEdit.putString(Profile.PREF_PROFILE_LOCK_DEVICE, "1");
                        }
                    }
                }
                prefEdit.apply();
                if (what == 1) {
                    PPApplication.setSavedVersionCode(getApplicationContext(), 0);
                }
            }/* catch (FileNotFoundException ignored) {
                // no error, this is OK
            }*/ catch (Exception e) {
                res = false;
            }
        } finally {
            try {
                if (input != null)
                    input.close();
            } catch (IOException ignored) {
            }
        }
        return res;
    }

    private void doImportData(String applicationDataPath)
    {
        final EditorProfilesActivity activity = this;
        final String _applicationDataPath = applicationDataPath;

        if (Permissions.grantImportPermissions(activity.getApplicationContext(), activity, applicationDataPath)) {

            @SuppressLint("StaticFieldLeak")
            class ImportAsyncTask extends AsyncTask<Void, Integer, Integer> {
                private DataWrapper dataWrapper;
                private int dbError = DatabaseHandler.IMPORT_OK;
                private boolean appSettingsError = false;
                private boolean sharedProfileError = false;

                private ImportAsyncTask() {
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
                    dialogBuilder.setMessage(R.string.import_profiles_alert_title);

                    LayoutInflater inflater = (activity.getLayoutInflater());
                    @SuppressLint("InflateParams")
                    View layout = inflater.inflate(R.layout.activity_progress_bar_dialog, null);
                    dialogBuilder.setView(layout);

                    importProgressDialog = dialogBuilder.create();

                    this.dataWrapper = getDataWrapper();
                }

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();

                    doImport = true;

                    GlobalGUIRoutines.lockScreenOrientation(activity);
                    importProgressDialog.setCancelable(false);
                    importProgressDialog.setCanceledOnTouchOutside(false);
                    if (!activity.isFinishing())
                        importProgressDialog.show();

                    if (PhoneProfilesService.getInstance() != null) {
                        PhoneProfilesService.stop(getApplicationContext());
                    }

                    EditorProfileListFragment fragment = (EditorProfileListFragment) getSupportFragmentManager().findFragmentById(R.id.editor_profile_list);
                    if (fragment != null)
                        fragment.removeAdapter();
                }

                @Override
                protected Integer doInBackground(Void... params) {
                    dbError = DatabaseHandler.getInstance(this.dataWrapper.context).importDB(_applicationDataPath);
                    if (dbError == DatabaseHandler.IMPORT_OK) {
                        // check for hardware capability and update data
                        DatabaseHandler.getInstance(this.dataWrapper.context).disableNotAllowedPreferences();
                        this.dataWrapper.clearProfileList();
                        DatabaseHandler.getInstance(this.dataWrapper.context).deactivateProfile();
                    }

                    File sd = Environment.getExternalStorageDirectory();
                    File exportFile = new File(sd, _applicationDataPath + "/" + GlobalGUIRoutines.EXPORT_APP_PREF_FILENAME);
                    appSettingsError = !importApplicationPreferences(exportFile, 1);
                    exportFile = new File(sd, _applicationDataPath + "/" + GlobalGUIRoutines.EXPORT_DEF_PROFILE_PREF_FILENAME);
                    sharedProfileError = !importApplicationPreferences(exportFile, 2);

                    PPApplication.logE("EditorProfilesActivity.doImportData", "dbError="+dbError);
                    PPApplication.logE("EditorProfilesActivity.doImportData", "appSettingsError="+appSettingsError);
                    PPApplication.logE("EditorProfilesActivity.doImportData", "sharedProfileError="+sharedProfileError);

                    if (!appSettingsError) {
                        Permissions.setAllShowRequestPermissions(getApplicationContext(), true);
                    }

                    if ((dbError == DatabaseHandler.IMPORT_OK) && (!(appSettingsError || sharedProfileError)))
                        return 1;
                    else
                        return 0;
                }

                @Override
                protected void onPostExecute(Integer result) {
                    super.onPostExecute(result);

                    doImport = false;

                    if ((importProgressDialog != null) && importProgressDialog.isShowing()) {
                        importProgressDialog.dismiss();
                        importProgressDialog = null;
                    }
                    if (!isFinishing())
                        GlobalGUIRoutines.unlockScreenOrientation(activity);

                    ActivateProfileHelper.updateGUI(dataWrapper.context, true);

                    PPApplication.setApplicationStarted(this.dataWrapper.context, true);
                    Intent serviceIntent = new Intent(this.dataWrapper.context, PhoneProfilesService.class);
                    serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, true);
                    serviceIntent.putExtra(PhoneProfilesService.EXTRA_INITIALIZE_START, true);
                    serviceIntent.putExtra(PhoneProfilesService.EXTRA_ACTIVATE_PROFILES, true);
                    PPApplication.startPPService(activity, serviceIntent);

                    if ((dbError == DatabaseHandler.IMPORT_OK) && (!(appSettingsError || sharedProfileError))) {
                        // toast notification
                        Toast msg = ToastCompat.makeText(this.dataWrapper.context.getApplicationContext(),
                                getResources().getString(R.string.toast_import_ok),
                                Toast.LENGTH_SHORT);
                        msg.show();

                        // refresh activity
                        if (!isFinishing())
                            GlobalGUIRoutines.reloadActivity(activity, true);
                    } else {
                        int appSettingsResult = 1;
                        if (appSettingsError) appSettingsResult = 0;
                        int sharedProfileResult = 1;
                        if (sharedProfileError) sharedProfileResult = 0;
                        if (!isFinishing())
                            importExportErrorDialog(1, dbError, appSettingsResult, sharedProfileResult);
                    }
                }

            }

            importAsyncTask = new ImportAsyncTask().execute();

        }
    }

    private void importDataAlert(/*boolean remoteExport*/)
    {
        //final boolean _remoteExport = remoteExport;

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
                /*if (_remoteExport) {
                    // start RemoteExportDataActivity
                    Intent intent = new Intent("phoneprofilesplus.intent.action.EXPORTDATA");

                    final PackageManager packageManager = getPackageManager();
                    List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                    if (list.size() > 0)
                        startActivityForResult(intent, REQUEST_CODE_REMOTE_EXPORT);
                    else
                        importExportErrorDialog(1, 0);
                } else*/
                    doImportData(PPApplication.EXPORT_PATH);
            }
        });
        dialogBuilder2.setNegativeButton(R.string.alert_button_no, null);
        AlertDialog dialog = dialogBuilder2.create();
        /*dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                if (positive != null) positive.setAllCaps(false);
                Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                if (negative != null) negative.setAllCaps(false);
            }
        });*/
        if (!isFinishing())
            dialog.show();
    }

    private void importData()
    {
        /*
        // test whether the PhoneProfilesPlus is installed
        PackageManager packageManager = getApplicationContext().getPackageManager();
        Intent phoneProfiles = packageManager.getLaunchIntentForPackage("sk.henrichg.phoneprofilesplus");
        if (phoneProfiles != null)
        {
            // PhoneProfilesPlus is installed

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
            AlertDialog dialog = dialogBuilder.create();

            //Button positive = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
            //if (positive != null) positive.setAllCaps(false);
            //Button negative = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
            //if (negative != null) negative.setAllCaps(false);

            dialog.show();
        }
        else */
            importDataAlert();
    }

    @SuppressLint("ApplySharedPref")
    private boolean exportApplicationPreferences(File dst, int what) {
        boolean res = true;
        ObjectOutputStream output = null;
        try {
            try {
                output = new ObjectOutputStream(new FileOutputStream(dst));
                SharedPreferences pref;
                if (what == 1)
                    pref = getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
                else
                    pref = getSharedPreferences(PPApplication.SHARED_PROFILE_PREFS_NAME, Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.commit();
                output.writeObject(pref.getAll());
            } catch (FileNotFoundException ignored) {
                // this is OK
            } catch (IOException e) {
                res = false;
            }
        } finally {
            try {
                if (output != null) {
                    output.flush();
                    output.close();
                }
            } catch (IOException ignored) {
            }
        }
        return res;
    }

    private void exportData()
    {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle(R.string.export_profiles_alert_title);
        dialogBuilder.setMessage(getString(R.string.export_profiles_alert_message) + " \"" + PPApplication.EXPORT_PATH + "\".\n\n" +
                                 getString(R.string.export_profiles_alert_message_note));
        //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);

        dialogBuilder.setPositiveButton(R.string.alert_button_backup, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                doExportData();
            }
        });
        dialogBuilder.setNegativeButton(android.R.string.cancel, null);
        AlertDialog dialog = dialogBuilder.create();
        /*dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                if (positive != null) positive.setAllCaps(false);
                Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                if (negative != null) negative.setAllCaps(false);
            }
        });*/
        if (!isFinishing())
            dialog.show();
    }

    private void doExportData()
    {
        final EditorProfilesActivity activity = this;

        if (Permissions.grantExportPermissions(activity.getApplicationContext(), activity)) {

            @SuppressLint("StaticFieldLeak")
            class ExportAsyncTask extends AsyncTask<Void, Integer, Integer> {
                private DataWrapper dataWrapper;

                private ExportAsyncTask() {
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
                    dialogBuilder.setMessage(R.string.export_profiles_alert_title);

                    LayoutInflater inflater = (activity.getLayoutInflater());
                    @SuppressLint("InflateParams")
                    View layout = inflater.inflate(R.layout.activity_progress_bar_dialog, null);
                    dialogBuilder.setView(layout);

                    exportProgressDialog = dialogBuilder.create();

                    this.dataWrapper = getDataWrapper();
                }

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();

                    GlobalGUIRoutines.lockScreenOrientation(activity);
                    exportProgressDialog.setCancelable(false);
                    exportProgressDialog.setCanceledOnTouchOutside(false);
                    if (!activity.isFinishing())
                        exportProgressDialog.show();
                }

                @Override
                protected Integer doInBackground(Void... params) {
                    int ret = DatabaseHandler.getInstance(this.dataWrapper.context).exportDB();
                    if (ret == 1) {
                        File sd = Environment.getExternalStorageDirectory();
                        File exportFile = new File(sd, PPApplication.EXPORT_PATH + "/" + GlobalGUIRoutines.EXPORT_APP_PREF_FILENAME);
                        if (exportApplicationPreferences(exportFile, 1)) {
                            exportFile = new File(sd, PPApplication.EXPORT_PATH + "/" + GlobalGUIRoutines.EXPORT_DEF_PROFILE_PREF_FILENAME);
                            if (!exportApplicationPreferences(exportFile, 2))
                                ret = 0;
                        }
                        else
                            ret = 0;
                    }

                    return ret;
                }

                @Override
                protected void onPostExecute(Integer result) {
                    super.onPostExecute(result);

                    if ((exportProgressDialog != null) && exportProgressDialog.isShowing()) {
                        exportProgressDialog.dismiss();
                        exportProgressDialog = null;
                    }
                    if (!isFinishing())
                        GlobalGUIRoutines.unlockScreenOrientation(activity);

                    if (result == 1) {

                        // toast notification
                        Toast msg = ToastCompat.makeText(this.dataWrapper.context.getApplicationContext(),
                                getResources().getString(R.string.toast_export_ok),
                                Toast.LENGTH_SHORT);
                        msg.show();

                    } else {
                        if (!isFinishing())
                            importExportErrorDialog(2, 0, 0,0);
                    }
                }

            }

            exportAsyncTask = new ExportAsyncTask().execute();

        }
    }

    private void startProfilePreferenceActivity(Profile profile, int editMode, int predefinedProfileIndex) {
        Intent intent = new Intent(getBaseContext(), ProfilePreferencesActivity.class);
        if (editMode == EditorProfileListFragment.EDIT_MODE_INSERT)
            intent.putExtra(PPApplication.EXTRA_PROFILE_ID, 0L);
        else
            intent.putExtra(PPApplication.EXTRA_PROFILE_ID, profile._id);
        intent.putExtra(EditorProfilesActivity.EXTRA_NEW_PROFILE_MODE, editMode);
        intent.putExtra(EditorProfilesActivity.EXTRA_PREDEFINED_PROFILE_INDEX, predefinedProfileIndex);
        startActivityForResult(intent, REQUEST_CODE_PROFILE_PREFERENCES);
    }

    public void onStartProfilePreferences(Profile profile, int editMode, int predefinedProfileIndex/*, boolean startTargetHelps*/) {

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
                arguments.putLong(PPApplication.EXTRA_PROFILE_ID, profile._id);
                arguments.putInt(EditorProfilesActivity.EXTRA_NEW_PROFILE_MODE, editMode);
                arguments.putInt(EditorProfilesActivity.EXTRA_PREDEFINED_PROFILE_INDEX, predefinedProfileIndex);
                arguments.putBoolean(EditorProfileListFragment.START_TARGET_HELPS_ARGUMENT, true/*startTargetHelps*/);
                ProfileDetailsFragment fragment = new ProfileDetailsFragment();
                fragment.setArguments(arguments);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.editor_profile_detail_container, fragment, "ProfileDetailsFragment").commit();

            }
            else
            {
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.editor_profile_detail_container);
                if (fragment != null)
                {
                    getSupportFragmentManager().beginTransaction()
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

    private void redrawProfilePreferences(Profile profile, int newProfileMode, int predefinedProfileIndex/*, boolean startTargetHelps*/) {
        if (mTwoPane) {
            if (profile != null)
            {
                // restart profile preferences fragment
                Bundle arguments = new Bundle();
                arguments.putLong(PPApplication.EXTRA_PROFILE_ID, profile._id);
                arguments.putInt(EditorProfilesActivity.EXTRA_NEW_PROFILE_MODE, newProfileMode);
                arguments.putInt(EditorProfilesActivity.EXTRA_PREDEFINED_PROFILE_INDEX, predefinedProfileIndex);
                arguments.putBoolean(EditorProfileListFragment.START_TARGET_HELPS_ARGUMENT, true/*startTargetHelps*/);
                ProfileDetailsFragment fragment = new ProfileDetailsFragment();
                fragment.setArguments(arguments);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.editor_profile_detail_container, fragment, "ProfileDetailsFragment").commit();
            }
            else
            {
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.editor_profile_detail_container);
                if (fragment != null)
                {
                    getSupportFragmentManager().beginTransaction()
                            .remove(fragment).commit();
                }
            }
        }
    }

    private void redrawProfileListFragment(Profile profile, int newProfileMode, int predefinedProfileIndex/*, boolean startTargetHelps*/)
    {
        // redraw list fragment header, notification and widgets
        final EditorProfileListFragment fragment = (EditorProfileListFragment) getSupportFragmentManager().findFragmentById(R.id.editor_profile_list);

        if (fragment != null) {
            // update profile, this rewrite profile in profileList
            fragment.activityDataWrapper.updateProfile(profile);

            boolean newProfile = ((newProfileMode == EditorProfileListFragment.EDIT_MODE_INSERT) ||
                    (newProfileMode == EditorProfileListFragment.EDIT_MODE_DUPLICATE));
            fragment.updateListView(profile, newProfile, false, false);

            Profile activeProfile = fragment.activityDataWrapper.getActivatedProfile(true,
                    ApplicationPreferences.applicationEditorPrefIndicator(fragment.activityDataWrapper.context));
            fragment.updateHeader(activeProfile);
            PPApplication.showProfileNotification(/*fragment.activityDataWrapper.context*/);
            ActivateProfileHelper.updateGUI(fragment.activityDataWrapper.context, true);

            fragment.activityDataWrapper.setDynamicLauncherShortcutsFromMainThread();
        }
        redrawProfilePreferences(profile, newProfileMode, predefinedProfileIndex/*, startTargetHelps*/);
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
        EditorProfileListFragment fragment = (EditorProfileListFragment)getSupportFragmentManager().findFragmentById(R.id.editor_profile_list);
        if (fragment != null)
            return fragment.activityDataWrapper;
        else
            return null;
    }

    private void refreshGUI(boolean refreshIcons, boolean setPosition)
    {
        final boolean _refreshIcons = refreshIcons;
        final boolean _setPosition = setPosition;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (doImport)
                    return;

                EditorProfileListFragment fragment = (EditorProfileListFragment)getSupportFragmentManager().findFragmentById(R.id.editor_profile_list);
                if (fragment != null)
                    fragment.refreshGUI(_refreshIcons, _setPosition);
            }
        });
    }

    private void showTargetHelps() {
        /*if (Build.VERSION.SDK_INT <= 19)
            // TapTarget.forToolbarMenuItem FC :-(
            // Toolbar.findViewById() returns null
            return;*/

        ApplicationPreferences.getSharedPreferences(this);

        boolean startTargetHelps = ApplicationPreferences.preferences.getBoolean(PREF_START_TARGET_HELPS, true);

        if (startTargetHelps ||
                ApplicationPreferences.preferences.getBoolean(EditorProfileListFragment.PREF_START_TARGET_HELPS, true) ||
                ApplicationPreferences.preferences.getBoolean(EditorProfileListAdapter.PREF_START_TARGET_HELPS, true)) {

            //Log.d("EditorProfilesActivity.showTargetHelps", "PREF_START_TARGET_HELPS_ORDER=true");

            if (startTargetHelps) {
                //Log.d("EditorProfilesActivity.showTargetHelps", "PREF_START_TARGET_HELPS=true");

                Editor editor = ApplicationPreferences.preferences.edit();
                editor.putBoolean(PREF_START_TARGET_HELPS, false);
                editor.apply();

                //TypedValue tv = new TypedValue();

                //final Display display = getWindowManager().getDefaultDisplay();

                String appTheme = ApplicationPreferences.applicationTheme(getApplicationContext(), true);
                int circleColor = R.color.tabTargetHelpCircleColor;
                if (appTheme.equals("dark"))
                    circleColor = R.color.tabTargetHelpCircleColor_dark;
                int textColor = R.color.tabTargetHelpTextColor;
                if (appTheme.equals("white"))
                    textColor = R.color.tabTargetHelpTextColor_white;
                boolean tintTarget = !appTheme.equals("white");

                final TapTargetSequence sequence = new TapTargetSequence(this);
                List<TapTarget> targets = new ArrayList<>();
                targets.add(
                        TapTarget.forToolbarOverflow(editorToolbar, getString(R.string.editor_activity_targetHelps_applicationMenu_title), getString(R.string.editor_activity_targetHelps_applicationMenu_description))
                                .targetCircleColor(circleColor)
                                .textColor(textColor)
                                .tintTarget(tintTarget)
                                .drawShadow(true)
                                .id(1)
                );

                int id = 2;
                try {
                    targets.add(
                            TapTarget.forToolbarMenuItem(editorToolbar, R.id.important_info, getString(R.string.editor_activity_targetHelps_importantInfoButton_title), getString(R.string.editor_activity_targetHelps_importantInfoButton_description))
                                    .targetCircleColor(circleColor)
                                    .textColor(textColor)
                                    .tintTarget(tintTarget)
                                    .drawShadow(true)
                                    .id(id)
                    );
                    ++id;
                } catch (Exception ignored) {} // not in action bar?

                sequence.targets(targets);
                sequence.listener(new TapTargetSequence.Listener() {
                    // This listener will tell us when interesting(tm) events happen in regards
                    // to the sequence
                    @Override
                    public void onSequenceFinish() {
                        targetHelpsSequenceStarted = false;
                        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.editor_profile_list);
                        if (fragment != null) {
                            ((EditorProfileListFragment) fragment).showTargetHelps();
                        }
                    }

                    @Override
                    public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {
                        //Log.d("TapTargetView", "Clicked on " + lastTarget.id());
                    }

                    @Override
                    public void onSequenceCanceled(TapTarget lastTarget) {
                        targetHelpsSequenceStarted = false;
                        Editor editor = ApplicationPreferences.preferences.edit();
                        editor.putBoolean(PREF_START_TARGET_HELPS, false);
                        editor.putBoolean(EditorProfileListFragment.PREF_START_TARGET_HELPS, false);
                        editor.putBoolean(EditorProfileListAdapter.PREF_START_TARGET_HELPS, false);
                        editor.apply();
                    }
                });
                sequence.continueOnCancel(true)
                        .considerOuterCircleCanceled(true);
                targetHelpsSequenceStarted = true;
                sequence.start();
            }
            else {
                //Log.d("EditorProfilesActivity.showTargetHelps", "PREF_START_TARGET_HELPS=false");
                final Handler handler = new Handler(getMainLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent("ShowEditorTargetHelpsBroadcastReceiver");
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                        /*if (EditorProfilesActivity.getInstance() != null) {
                            Fragment fragment = EditorProfilesActivity.getInstance().getFragmentManager().findFragmentById(R.id.editor_profile_list);
                            if (fragment != null) {
                                ((EditorProfileListFragment) fragment).showTargetHelps();
                            }
                        }*/
                    }
                }, 500);
            }
        }
    }

}
