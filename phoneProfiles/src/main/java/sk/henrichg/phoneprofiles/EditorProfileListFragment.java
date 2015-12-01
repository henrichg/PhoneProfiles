package sk.henrichg.phoneprofiles;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.mobeta.android.dslv.DragSortListView;

import java.lang.ref.WeakReference;
import java.util.List;

public class EditorProfileListFragment extends Fragment {

    public DataWrapper dataWrapper;
    private ActivateProfileHelper activateProfileHelper;
    private List<Profile> profileList;
    private EditorProfileListAdapter profileListAdapter;
    private DragSortListView listView;
    private TextView activeProfileName;
    private ImageView activeProfileIcon;
    private DatabaseHandler databaseHandler;

    private WeakReference<LoadProfileListAsyncTask> asyncTaskContext;

    public static final int EDIT_MODE_UNDEFINED = 0;
    public static final int EDIT_MODE_INSERT = 1;
    public static final int EDIT_MODE_DUPLICATE = 2;
    public static final int EDIT_MODE_EDIT = 3;
    public static final int EDIT_MODE_DELETE = 4;

    /**
     * The fragment's current callback objects
     */
    private OnStartProfilePreferences onStartProfilePreferencesCallback = sDummyOnStartProfilePreferencesCallback;
    private OnFinishProfilePreferencesActionMode onFinishProfilePreferencesActionModeCallback = sDummyOnFinishProfilePreferencesActionModeCallback;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified.
     */
    // invoked when start profile preference fragment/activity needed
    public interface OnStartProfilePreferences {
        /**
         * Callback for when an item has been selected.
         */
        public void onStartProfilePreferences(Profile profile, int editMode);
    }

    /**
     * A dummy implementation of the Callbacks interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static OnStartProfilePreferences sDummyOnStartProfilePreferencesCallback = new OnStartProfilePreferences() {
        public void onStartProfilePreferences(Profile profile, int editMode) {
        }
    };

    // invoked when action mode finish needed (from profile list adapter)
    public interface OnFinishProfilePreferencesActionMode {
        public void onFinishProfilePreferencesActionMode();
    }

    private static OnFinishProfilePreferencesActionMode sDummyOnFinishProfilePreferencesActionModeCallback = new OnFinishProfilePreferencesActionMode() {
        public void onFinishProfilePreferencesActionMode() {
        }
    };

    public EditorProfileListFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof OnStartProfilePreferences)) {
            throw new IllegalStateException(
                    "Activity must implement fragment's callbacks.");
        }
        onStartProfilePreferencesCallback = (OnStartProfilePreferences) activity;

        if (activity instanceof OnFinishProfilePreferencesActionMode)
            onFinishProfilePreferencesActionModeCallback = (OnFinishProfilePreferencesActionMode) activity;

    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Reset the active callbacks interface to the dummy implementation.
        onStartProfilePreferencesCallback = sDummyOnStartProfilePreferencesCallback;
        onFinishProfilePreferencesActionModeCallback = sDummyOnFinishProfilePreferencesActionModeCallback;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // this is really important in order to save the state across screen
        // configuration changes for example
        setRetainInstance(true);

        dataWrapper = new DataWrapper(getActivity().getApplicationContext(), true, false, 0);

        databaseHandler = dataWrapper.getDatabaseHandler();

        activateProfileHelper = dataWrapper.getActivateProfileHelper();
        activateProfileHelper.initialize(getActivity(), getActivity().getApplicationContext());

        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView;

        if (GlobalData.applicationEditorPrefIndicator && GlobalData.applicationEditorHeader)
            rootView = inflater.inflate(R.layout.editor_profile_list, container, false);
        else
        if (GlobalData.applicationEditorHeader)
            rootView = inflater.inflate(R.layout.editor_profile_list_no_indicator, container, false);
        else
            rootView = inflater.inflate(R.layout.editor_profile_list_no_header, container, false);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        doOnViewCreated(view, savedInstanceState);
    }

    //@Override
    //public void onActivityCreated(Bundle savedInstanceState)
    @SuppressLint("InflateParams")
    public void doOnViewCreated(View view, Bundle savedInstanceState)
    {
        //super.onActivityCreated(savedInstanceState);

        // az tu mame layout, tak mozeme ziskat view-y
    /*	activeProfileName = (TextView)getActivity().findViewById(R.id.activated_profile_name);
        activeProfileIcon = (ImageView)getActivity().findViewById(R.id.activated_profile_icon);
        listView = (DragSortListView)getActivity().findViewById(R.id.main_profiles_list);
        listView.setEmptyView(getActivity().findViewById(R.id.editor_profiles_list_empty));
    */
        activeProfileName = (TextView)view.findViewById(R.id.activated_profile_name);
        activeProfileIcon = (ImageView)view.findViewById(R.id.activated_profile_icon);
        listView = (DragSortListView)view.findViewById(R.id.main_profiles_list);
        listView.setEmptyView(view.findViewById(R.id.editor_profiles_list_empty));

        /*
        View footerView =  ((LayoutInflater)getActivity().getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.editor_list_footer, null, false);
        listView.addFooterView(footerView, null, false);
        */

        Toolbar bottomToolbar = (Toolbar)getActivity().findViewById(R.id.editor_list_bottom_bar);
        Menu menu = bottomToolbar.getMenu();
        if (menu != null) menu.clear();
        bottomToolbar.inflateMenu(R.menu.editor_profiles_bottom_bar);
        bottomToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_add_profile:
                        startProfilePreferencesActivity(null);
                        return true;
                    case R.id.menu_delete_all_profiles:
                        deleteAllProfiles();
                        return true;
                    case R.id.menu_default_profile:
                        // start preferences activity for default profile
                        Intent intent = new Intent(getActivity().getBaseContext(), ProfilePreferencesFragmentActivity.class);
                        intent.putExtra(GlobalData.EXTRA_PROFILE_ID, GlobalData.DEFAULT_PROFILE_ID);
                        intent.putExtra(GlobalData.EXTRA_NEW_PROFILE_MODE, EDIT_MODE_EDIT);
                        getActivity().startActivityForResult(intent, GlobalData.REQUEST_CODE_PROFILE_PREFERENCES);
                        return true;
                }
                return false;
            }
        });

        listView.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                startProfilePreferencesActivity((Profile) profileListAdapter.getItem(position));

            }

        });

        listView.setOnItemLongClickListener(new OnItemLongClickListener() {

            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                activateProfile((Profile) profileListAdapter.getItem(position), true);

                return true;
            }

        });

        listView.setDropListener(new DragSortListView.DropListener() {
            public void drop(int from, int to) {
                profileListAdapter.changeItemOrder(from, to); // swap profiles
                databaseHandler.setPOrder(profileList);  // set profiles _porder and write it into db
                activateProfileHelper.updateWidget();
            }
        });
        
        if (profileList == null)
        {
            LoadProfileListAsyncTask asyncTask = new LoadProfileListAsyncTask(this);
            this.asyncTaskContext = new WeakReference<LoadProfileListAsyncTask >(asyncTask );
            asyncTask.execute();
        }
        else
        {
            listView.setAdapter(profileListAdapter);
        
            // pre profil, ktory je prave aktivny, treba aktualizovat aktivitu
            Profile profile;
            profile = dataWrapper.getActivatedProfile();
            updateHeader(profile);
        }

    }

    private static class LoadProfileListAsyncTask extends AsyncTask<Void, Void, Void> {

        private WeakReference<EditorProfileListFragment> fragmentWeakRef;
        private DataWrapper dataWrapper;
        boolean defaultProfilesGenerated = false;

        private LoadProfileListAsyncTask (EditorProfileListFragment fragment) {
            this.fragmentWeakRef = new WeakReference<EditorProfileListFragment>(fragment);
            this.dataWrapper = new DataWrapper(fragment.getActivity().getApplicationContext(), true, false, 0);
        }

        @Override
        protected Void doInBackground(Void... params) {
            List<Profile> profileList = dataWrapper.getProfileList();
            if (profileList.size() == 0)
            {
                // no profiles in DB, generate default profiles
                profileList = dataWrapper.getDefaultProfileList();
                defaultProfilesGenerated = true;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void response) {
            super.onPostExecute(response);
            
            EditorProfileListFragment fragment = this.fragmentWeakRef.get(); 
            
            if ((fragment != null) && (fragment.isAdded())) {

                // get local profileList
                List<Profile> profileList = dataWrapper.getProfileList();
                // set copy local profile list into activity profilesDataWrapper
                fragment.dataWrapper.setProfileList(profileList, false);
                // set reference of profile list from profilesDataWrapper
                fragment.profileList = fragment.dataWrapper.getProfileList();

                fragment.profileListAdapter = new EditorProfileListAdapter(fragment, fragment.dataWrapper);
                fragment.listView.setAdapter(fragment.profileListAdapter);

                // pre profil, ktory je prave aktivny, treba aktualizovat aktivitu
                Profile profile;
                profile = fragment.dataWrapper.getActivatedProfile();
                fragment.updateHeader(profile);

                if (defaultProfilesGenerated)
                {
                    fragment.activateProfileHelper.updateWidget();
                    Toast msg = Toast.makeText(fragment.getActivity(),
                            fragment.getResources().getString(R.string.toast_default_profiles_generated),
                            Toast.LENGTH_SHORT);
                    msg.show();
                }
            
            }
        }
    }

    private boolean isAsyncTaskPendingOrRunning() {
        return this.asyncTaskContext != null &&
              this.asyncTaskContext.get() != null &&
              !this.asyncTaskContext.get().getStatus().equals(AsyncTask.Status.FINISHED);
    }

    @Override
    public void onStart()
    {
        super.onStart();
    }

    @Override
    public void onDestroy()
    {
        if (!isAsyncTaskPendingOrRunning())
        {
            if (listView != null)
                listView.setAdapter(null);
            if (profileListAdapter != null)
                profileListAdapter.release();

            activateProfileHelper = null;
            profileList = null;
            databaseHandler = null;

            if (dataWrapper != null)
                dataWrapper.invalidateDataWrapper();
            dataWrapper = null;
        }

        super.onDestroy();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.fragment_editor_profile_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
        /*case R.id.menu_new_profile:
            startProfilePreferencesActivity(null);

            return true;*/
        /*case R.id.menu_delete_all_profiles:
            deleteAllProfiles();

            return true;*/
        /*case R.id.menu_default_profile:
            // start preferences activity for default profile
            Intent intent = new Intent(getActivity().getBaseContext(), ProfilePreferencesFragmentActivity.class);
            intent.putExtra(GlobalData.EXTRA_PROFILE_ID, GlobalData.DEFAULT_PROFILE_ID);
            intent.putExtra(GlobalData.EXTRA_NEW_PROFILE_MODE, EDIT_MODE_EDIT);
            getActivity().startActivityForResult(intent, GlobalData.REQUEST_CODE_PROFILE_PREFERENCES);

            return true;*/
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private void startProfilePreferencesActivity(Profile profile)
    {

        Profile _profile = profile;
        int editMode;

        if (_profile != null)
        {
            // editacia profilu
            editMode = EDIT_MODE_EDIT;
        }
        else
        {
            // pridanie noveho profilu
            editMode = EDIT_MODE_INSERT;
        }

        // Notify the active callbacks interface (the activity, if the
        // fragment is attached to one) one must start profile preferences
        onStartProfilePreferencesCallback.onStartProfilePreferences(_profile, editMode);
    }

    public void duplicateProfile(Profile origProfile)
    {
        int editMode;

        // zduplikovanie profilu
        editMode = EDIT_MODE_DUPLICATE;

        // Notify the active callbacks interface (the activity, if the
        // fragment is attached to one) one must start profile preferences
        onStartProfilePreferencesCallback.onStartProfilePreferences(origProfile, editMode);

    }

    public void deleteProfile(Profile profile)
    {
        //final Profile _profile = profile;
        //final Activity activity = getActivity();

        if (dataWrapper.getProfileById(profile._id) == null)
            // profile not exists
            return;

        Profile activatedProfile = dataWrapper.getActivatedProfile();
        if ((activatedProfile != null) && (activatedProfile._id == profile._id)) {
            // remove alarm for profile duration
            ProfileDurationAlarmBroadcastReceiver.removeAlarm(getActivity().getApplicationContext());
            GlobalData.setActivatedProfileForDuration(getActivity().getApplicationContext(), 0);
        }

        profileListAdapter.deleteItemNoNotify(profile);
        databaseHandler.deleteProfile(profile);

        profileListAdapter.notifyDataSetChanged();
        // v pripade, ze sa odmaze aktivovany profil, nastavime, ze nic nie je aktivovane
        //Profile profile = databaseHandler.getActivatedProfile();
        Profile _profile = profileListAdapter.getActivatedProfile();
        updateHeader(_profile);
        activateProfileHelper.showNotification(_profile);
        activateProfileHelper.updateWidget();

        onStartProfilePreferencesCallback.onStartProfilePreferences(null, EDIT_MODE_DELETE);

    }

    public void deleteProfileWithAlert(Profile profile)
    {
        final Profile _profile = profile;

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setTitle(getResources().getString(R.string.profile_string_0) + ": " + profile._name);
        dialogBuilder.setMessage(R.string.delete_profile_alert_message);
        //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);

        dialogBuilder.setPositiveButton(R.string.alert_button_yes, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                deleteProfile(_profile);
            }
        });
        dialogBuilder.setNegativeButton(R.string.alert_button_no, null);
        dialogBuilder.show();
    }

    public void showEditMenu(View view)
    {
        //Context context = ((AppCompatActivity)getActivity()).getSupportActionBar().getThemedContext();
        Context context = view.getContext();
        PopupMenu popup = new PopupMenu(context, view);
        getActivity().getMenuInflater().inflate(R.menu.profile_list_item_edit, popup.getMenu());

        final Profile profile = (Profile)view.getTag();

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            public boolean onMenuItemClick(android.view.MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.profile_list_item_menu_activate:
                        activateProfile(profile, true);
                        return true;
                    case R.id.profile_list_item_menu_duplicate:
                        duplicateProfile(profile);
                        return true;
                    case R.id.profile_list_item_menu_delete:
                        deleteProfileWithAlert(profile);
                        return true;
                    default:
                        return false;
                }
            }
        });


        popup.show();
    }

    private void deleteAllProfiles()
    {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setTitle(R.string.alert_title_delete_all_profiles);
        dialogBuilder.setMessage(R.string.alert_message_delete_all_profiles);
        //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);

        //final Activity activity = getActivity();

        dialogBuilder.setPositiveButton(R.string.alert_button_yes, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {

                // remove alarm for profile duration
                ProfileDurationAlarmBroadcastReceiver.removeAlarm(getActivity().getApplicationContext());
                GlobalData.setActivatedProfileForDuration(getActivity().getApplicationContext(), 0);

                profileListAdapter.clearNoNotify();
                databaseHandler.deleteAllProfiles();

                profileListAdapter.notifyDataSetChanged();
                // v pripade, ze sa odmaze aktivovany profil, nastavime, ze nic nie je aktivovane
                //Profile profile = databaseHandler.getActivatedProfile();
                //Profile profile = profileListAdapter.getActivatedProfile();
                updateHeader(null);
                activateProfileHelper.removeNotification();
                activateProfileHelper.updateWidget();

                onStartProfilePreferencesCallback.onStartProfilePreferences(null, EDIT_MODE_DELETE);

            }
        });
        dialogBuilder.setNegativeButton(R.string.alert_button_no, null);
        dialogBuilder.show();
    }

    public void updateHeader(Profile profile)
    {
        if (!GlobalData.applicationEditorHeader)
            return;

        if ((activeProfileName == null) || (activeProfileIcon == null))
            return;

        if (profile == null)
        {
            activeProfileName.setText(getResources().getString(R.string.profiles_header_profile_name_no_activated));
            activeProfileIcon.setImageResource(R.drawable.ic_profile_default);
        }
        else
        {
            activeProfileName.setText(profile._name);
            if (profile.getIsIconResourceID())
            {
                if (profile._iconBitmap != null)
                    activeProfileIcon.setImageBitmap(profile._iconBitmap);
                else {
                    int res = getResources().getIdentifier(profile.getIconIdentifier(), "drawable", getActivity().getPackageName());
                    activeProfileIcon.setImageResource(res); // resource na ikonu
                }
            }
            else
            {
                //Resources resources = getResources();
                //int height = (int) resources.getDimension(android.R.dimen.app_icon_size);
                //int width = (int) resources.getDimension(android.R.dimen.app_icon_size);
                //Bitmap bitmap = BitmapResampler.resample(profile.getIconIdentifier(), width, height);
                //activeProfileIcon.setImageBitmap(bitmap);
                activeProfileIcon.setImageBitmap(profile._iconBitmap);
            }
        }

        if (GlobalData.applicationEditorPrefIndicator)
        {
            ImageView profilePrefIndicatorImageView = (ImageView)getActivity().findViewById(R.id.activated_profile_pref_indicator);
            if (profilePrefIndicatorImageView != null)
            {
                //profilePrefIndicatorImageView.setImageBitmap(ProfilePreferencesIndicator.paint(profile, getActivity().getBaseContext()));
                if (profile == null)
                    profilePrefIndicatorImageView.setImageResource(R.drawable.ic_empty);
                else
                    profilePrefIndicatorImageView.setImageBitmap(profile._preferencesIndicator);
            }
        }
    }

    public void doOnActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == GlobalData.REQUEST_CODE_ACTIVATE_PROFILE)
        {
            if(resultCode == Activity.RESULT_OK)
            {
                long profile_id = data.getLongExtra(GlobalData.EXTRA_PROFILE_ID, -1);
                Profile profile = dataWrapper.getProfileById(profile_id);

                if (profileListAdapter != null)
                    profileListAdapter.activateProfile(profile);
                updateHeader(profile);
             }
             if (resultCode == Activity.RESULT_CANCELED)
             {
                 //Write your code if there's no result
             }
        }
    }

    public void activateProfile(Profile profile, boolean interactive)
    {
        /*
        Intent intent = new Intent(getActivity().getBaseContext(), BackgroundActivateProfileActivity.class);
        intent.putExtra(GlobalData.EXTRA_STARTUP_SOURCE, GlobalData.STARTUP_SOURCE_EDITOR);
        intent.putExtra(GlobalData.EXTRA_PROFILE_ID, profile._id);
        getActivity().startActivityForResult(intent, GlobalData.REQUEST_CODE_ACTIVATE_PROFILE);
        */
        dataWrapper.activateProfile(profile._id, GlobalData.STARTUP_SOURCE_EDITOR, getActivity());
    }

    // called from profile list adapter
    public void finishProfilePreferencesActionMode()
    {
        onFinishProfilePreferencesActionModeCallback.onFinishProfilePreferencesActionMode();
    }

    public void updateListView(Profile profile, boolean newProfile, boolean refreshIcons)
    {
        if (profileListAdapter != null)
        {
            if (newProfile)
            {
                // add profile into listview
                profileListAdapter.addItem(profile, false);

            }
            profileListAdapter.notifyDataSetChanged(refreshIcons);
        }
    }

    public void refreshGUI(boolean refreshIcons)
    {
        if ((dataWrapper == null) || (profileListAdapter == null))
            return;

        Profile profileFromAdapter = profileListAdapter.getActivatedProfile();
        if (profileFromAdapter != null) {
            profileFromAdapter._checked = false;
            if (refreshIcons) {
                dataWrapper.refreshProfileIcon(profileFromAdapter, false, 0);
            }
        }

        Profile profileFromDB = dataWrapper.getDatabaseHandler().getActivatedProfile();
        if (profileFromDB != null)
        {
            Profile profileFromDataWrapper = dataWrapper.getProfileById(profileFromDB._id);
            if (profileFromDataWrapper != null) {
                profileFromDataWrapper._checked = true;
                if (refreshIcons) {
                    dataWrapper.refreshProfileIcon(profileFromDataWrapper, false, 0);
                }
            }
            updateHeader(profileFromDataWrapper);
            updateListView(profileFromDataWrapper, false, refreshIcons);
        }
        else
        {
            updateHeader(null);
            updateListView(null, false, refreshIcons);
        }
    }

    public void removeAdapter() {
        if (listView != null)
            listView.setAdapter(null);
    }

}
