package sk.henrichg.phoneprofiles;

import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ActivateProfileListFragment extends Fragment {

    private DataWrapper dataWrapper;
    private ActivateProfileHelper activateProfileHelper;
    private List<Profile> profileList = null;
    private ActivateProfileListAdapter profileListAdapter = null;
    private ListView listView = null;
    private GridView gridView = null;
    private TextView activeProfileName;
    private ImageView activeProfileIcon;

    public int startupSource = 0;
    public Intent intent;

    private WeakReference<LoadProfileListAsyncTask> asyncTaskContext;

    public ActivateProfileListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // this is really important in order to save the state across screen
        // configuration changes for example
        setRetainInstance(true);

        dataWrapper = new DataWrapper(getActivity().getApplicationContext(), true, false, 0);
        activateProfileHelper = dataWrapper.getActivateProfileHelper();
        activateProfileHelper.initialize(dataWrapper, getActivity().getApplicationContext());

        intent = getActivity().getIntent();
        startupSource = intent.getIntExtra(GlobalData.EXTRA_STARTUP_SOURCE, 0);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView;

        if (!GlobalData.applicationActivatorGridLayout)
        {
            if (GlobalData.applicationActivatorPrefIndicator && GlobalData.applicationActivatorHeader)
                rootView = inflater.inflate(R.layout.activate_profile_list, container, false);
            else
            if (GlobalData.applicationActivatorHeader)
                rootView = inflater.inflate(R.layout.activate_profile_list_no_indicator, container, false);
            else
                rootView = inflater.inflate(R.layout.activate_profile_list_no_header, container, false);
        }
        else
        {
            if (GlobalData.applicationActivatorPrefIndicator && GlobalData.applicationActivatorHeader)
                rootView = inflater.inflate(R.layout.activate_profile_grid, container, false);
            else
            if (GlobalData.applicationActivatorHeader)
                rootView = inflater.inflate(R.layout.activate_profile_grid_no_indicator, container, false);
            else
                rootView = inflater.inflate(R.layout.activate_profile_grid_no_header, container, false);
        }

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        doOnViewCreated(view, savedInstanceState);
    }

    //@Override
    public void doOnViewCreated(View view, Bundle savedInstanceState)
    {
        activeProfileName = (TextView)view.findViewById(R.id.act_prof_activated_profile_name);
        activeProfileIcon = (ImageView)view.findViewById(R.id.act_prof_activated_profile_icon);
        if (!GlobalData.applicationActivatorGridLayout)
            listView = (ListView)view.findViewById(R.id.act_prof_profiles_list);
        else
            gridView = (GridView)view.findViewById(R.id.act_prof_profiles_grid);

        AbsListView absListView;
        if (!GlobalData.applicationActivatorGridLayout)
            absListView = listView;
        else
            absListView = gridView;

        //absListView.setLongClickable(false);

        absListView.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (!GlobalData.applicationLongClickActivation)
                    //activateProfileWithAlert(position);
                    activateProfile(position, GlobalData.STARTUP_SOURCE_ACTIVATOR);

            }

        });

        absListView.setOnItemLongClickListener(new OnItemLongClickListener() {

            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                if (GlobalData.applicationLongClickActivation)
                    //activateProfileWithAlert(position);
                    activateProfile(position, GlobalData.STARTUP_SOURCE_ACTIVATOR);

                return false;
            }

        });

        //absListView.setRemoveListener(onRemove);

        if (profileList == null)
        {
            LoadProfileListAsyncTask asyncTask = new LoadProfileListAsyncTask(this);
            this.asyncTaskContext = new WeakReference<>(asyncTask );
            asyncTask.execute();
        }
        else
        {
            absListView.setAdapter(profileListAdapter);

            doOnStart();
        }

    }

    private static class LoadProfileListAsyncTask extends AsyncTask<Void, Void, Void> {

        private WeakReference<ActivateProfileListFragment> fragmentWeakRef;
        private DataWrapper dataWrapper;

        private class ProfileComparator implements Comparator<Profile> {
            public int compare(Profile lhs, Profile rhs) {
                int res = 0;
                if ((lhs != null) && (rhs != null))
                    res = lhs._porder - rhs._porder;
                return res;
            }
        }

        private LoadProfileListAsyncTask (ActivateProfileListFragment fragment) {
            this.fragmentWeakRef = new WeakReference<>(fragment);
            this.dataWrapper = new DataWrapper(fragment.getActivity().getApplicationContext(), true, false, 0);
        }

        @Override
        protected Void doInBackground(Void... params) {
            List<Profile> profileList = dataWrapper.getProfileList();
            Collections.sort(profileList, new ProfileComparator());
            return null;
        }

        @Override
        protected void onPostExecute(Void response) {
            super.onPostExecute(response);
            
            ActivateProfileListFragment fragment = this.fragmentWeakRef.get(); 
            
            if ((fragment != null) && (fragment.isAdded())) {

                // get local profileList
                List<Profile> profileList = dataWrapper.getProfileList();
                // set copy local profile list into activity profilesDataWrapper
                fragment.dataWrapper.setProfileList(profileList, false);
                // set reference of profile list from profilesDataWrapper
                fragment.profileList = fragment.dataWrapper.getProfileList();

                if (fragment.profileList.size() == 0)
                {
                    // nie je ziaden profile, startneme Editor

                    fragment.doOnStart();

                    Intent intent = new Intent(fragment.getActivity().getBaseContext(), EditorProfilesActivity.class);
                    intent.putExtra(GlobalData.EXTRA_STARTUP_SOURCE, GlobalData.STARTUP_SOURCE_ACTIVATOR_START);
                    fragment.getActivity().startActivity(intent);

                    fragment.getActivity().finish();

                    return;
                }

                fragment.profileListAdapter = new ActivateProfileListAdapter(fragment, fragment.profileList, fragment.dataWrapper);

                AbsListView absListView;
                if (!GlobalData.applicationActivatorGridLayout)
                    absListView = fragment.listView;
                else
                    absListView = fragment.gridView;
                absListView.setAdapter(fragment.profileListAdapter);

                fragment.doOnStart();
            }
        }
    }

    private boolean isAsyncTaskPendingOrRunning() {
        return this.asyncTaskContext != null &&
              this.asyncTaskContext.get() != null &&
              !this.asyncTaskContext.get().getStatus().equals(AsyncTask.Status.FINISHED);
    }

    private void doOnStart()
    {
        if (!GlobalData.getApplicationStarted(getActivity().getApplicationContext()))
        {
            GlobalData.logE("ActivateProfileListFragment.doOnStart","application not started");

            // start service for first start
            //Intent firstStartServiceIntent = new Intent(getActivity().getApplicationContext(), FirstStartService.class);
            //getActivity().startService(firstStartServiceIntent);

            // start PhoneProfilesService
            getActivity().startService(new Intent(getActivity().getApplicationContext(), PhoneProfilesService.class));
        }
        else
        {
            GlobalData.logE("ActivateProfileListFragment.doOnStart", "xxx");

            if (PhoneProfilesService.instance == null) {
                // start PhoneProfilesService
                getActivity().startService(new Intent(getActivity().getApplicationContext(), PhoneProfilesService.class));
            }

            Profile profile = dataWrapper.getActivatedProfile();
            updateHeader(profile);
            if (startupSource == 0)
            {
                // aktivita nebola spustena z notifikacie, ani z widgetu
                // pre profil, ktory je prave aktivny, treba aktualizovat notifikaciu a widgety
                activateProfileHelper.showNotification(profile);
                activateProfileHelper.updateWidget();
            }
        }
        endOnStart();

        //GlobalData.getMeasuredRunTime(nanoTimeStart, "ActivateProfileActivity.onStart");

    }

    private void endOnStart()
    {
        // reset, aby sa to dalej chovalo ako normalne spustenie z lauchera
        startupSource = 0;

        //  aplikacia uz je 1. krat spustena - moved to FirstStartService
        //GlobalData.setApplicationStarted(getActivity().getApplicationContext(), true);
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
            AbsListView absListView;
            if (!GlobalData.applicationActivatorGridLayout)
                absListView = listView;
            else
                absListView = gridView;
            if (absListView != null)
                absListView.setAdapter(null);
            if (profileListAdapter != null)
                profileListAdapter.release();

            profileList = null;

            activateProfileHelper = null;
            if (dataWrapper != null)
                dataWrapper.invalidateDataWrapper();
            dataWrapper = null;
        }

        super.onDestroy();
    }

    private void updateHeader(Profile profile)
    {
        if (!GlobalData.applicationActivatorHeader)
            return;

        if (profile == null)
        {
            activeProfileName.setText(getResources().getString(R.string.profiles_header_profile_name_no_activated));
            activeProfileIcon.setImageResource(R.drawable.ic_profile_default);
        }
        else
        {
            activeProfileName.setText(profile.getProfileNameWithDuration(false, dataWrapper.context));
            if (profile.getIsIconResourceID())
            {
                if (profile.getUseCustomColorForIcon())
                    activeProfileIcon.setImageBitmap(profile._iconBitmap);
                else {
                    int res = getResources().getIdentifier(profile.getIconIdentifier(), "drawable", getActivity().getPackageName());
                    activeProfileIcon.setImageResource(res); // resource na ikonu
                }
            }
            else
            {
                activeProfileIcon.setImageBitmap(profile._iconBitmap);
            }
        }

        if (GlobalData.applicationActivatorPrefIndicator)
        {
            ImageView profilePrefIndicatorImageView = (ImageView)getActivity().findViewById(R.id.act_prof_activated_profile_pref_indicator);
            if (profilePrefIndicatorImageView != null)
            {
                if (profile == null)
                    profilePrefIndicatorImageView.setImageResource(R.drawable.ic_empty);
                else
                    profilePrefIndicatorImageView.setImageBitmap(profile._preferencesIndicator);
            }
        }
    }

    public void activateProfile(Profile profile, int startupSource)
    {
        dataWrapper.activateProfile(profile._id, startupSource, getActivity());
    }

    private void activateProfile(int position, int startupSource)
    {
        Profile profile = profileList.get(position);
        activateProfile(profile, startupSource);
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
        }
        else
            updateHeader(null);

        profileListAdapter.notifyDataSetChanged(refreshIcons);

    }

}
