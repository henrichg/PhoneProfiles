package sk.henrichg.phoneprofiles;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.content.Intent;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

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

		//Log.e("ActivateProfileListFragment.onCreate","xxx");
		
		// this is really important in order to save the state across screen
		// configuration changes for example
		setRetainInstance(true);
	
		dataWrapper = new DataWrapper(getActivity().getBaseContext(), true, false, 0);
		activateProfileHelper = dataWrapper.getActivateProfileHelper();
		activateProfileHelper.initialize(getActivity(), getActivity().getBaseContext());
		
		intent = getActivity().getIntent();
		startupSource = intent.getIntExtra(GlobalData.EXTRA_START_APP_SOURCE, 0);

		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		//Log.e("ActivateProfileListFragment.onCreateView","xxx");
		
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

		//Log.e("ActivateProfileListFragment.onViewCreated","xxx");
		
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

				//Log.d("ActivateProfilesActivity.onItemClick", "xxxx");

				if (!GlobalData.applicationLongClickActivation)
					//activateProfileWithAlert(position);
					activateProfile(position, GlobalData.STARTUP_SOURCE_ACTIVATOR);

			}
			
		}); 
		
		absListView.setOnItemLongClickListener(new OnItemLongClickListener() {

			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

				//Log.d("ActivateProfilesActivity.onItemLongClick", "xxxx");
				
				if (GlobalData.applicationLongClickActivation)
					//activateProfileWithAlert(position);
					activateProfile(position, GlobalData.STARTUP_SOURCE_ACTIVATOR);

				return false;
			}
			
		});
		
        //absListView.setRemoveListener(onRemove);

		if (profileList == null)
		{
			//Log.e("ActivateProfileListFragment.onViewCreated","profileList==null");
			
			LoadProfileListAsyncTask asyncTask = new LoadProfileListAsyncTask(this);
		    this.asyncTaskContext = new WeakReference<LoadProfileListAsyncTask >(asyncTask );
		    asyncTask.execute();			
		}
		else
		{
			//Log.e("ActivateProfileListFragment.onViewCreated","profileList!=null");
			absListView.setAdapter(profileListAdapter);
			
			doOnStart();
		}
		
		//Log.d("EditorProfileListFragment.onActivityCreated", "xxx");
        
	}
	
	private static class LoadProfileListAsyncTask extends AsyncTask<Void, Void, Void> {

        private WeakReference<ActivateProfileListFragment> fragmentWeakRef;
		private DataWrapper dataWrapper; 

		private class ProfileComparator implements Comparator<Profile> {
			public int compare(Profile lhs, Profile rhs) {
			    int res = lhs._porder - rhs._porder;
		        return res;
		    }
		}
		
        private LoadProfileListAsyncTask (ActivateProfileListFragment fragment) {
            this.fragmentWeakRef = new WeakReference<ActivateProfileListFragment>(fragment);
	        this.dataWrapper = new DataWrapper(fragment.getActivity().getBaseContext(), true, false, 0);
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
					intent.putExtra(GlobalData.EXTRA_START_APP_SOURCE, GlobalData.STARTUP_SOURCE_ACTIVATOR_START);
					fragment.getActivity().startActivity(intent);
					
					fragment.getActivity().finish();
	
					return;
				}
    	        
    	        fragment.profileListAdapter = new ActivateProfileListAdapter(fragment, fragment.profileList);
    	        
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
		//long nanoTimeStart = GlobalData.startMeasuringRunTime();

		//Log.e("ActivateProfileListFragment.doOnStart", "startupSource="+startupSource);
		
		/*
		//if (!GlobalData.getApplicationStarted(getActivity().getBaseContext()))
		//{
			// grant root
			Intent eventsServiceIntent = new Intent(getActivity().getBaseContext(), GrantRootService.class);
			getActivity().getBaseContext().startService(eventsServiceIntent);
		//}

		Profile profile = dataWrapper.getActivatedProfile();

		boolean actProfile = false;
		if (startupSource == 0)
		{
			
			// aktivita nebola spustena z notifikacie, ani z widgetu
			// lebo v tychto pripadoch sa nesmie spravit aktivacia profilu
			// pri starte aktivity
			
			if (!GlobalData.getApplicationStarted(getActivity().getBaseContext()))
			{
				// aplikacia este nie je nastartovana, takze mozeme
				// aktivovat profil, ak je nastavene, ze sa tak ma stat
	
				if (GlobalData.applicationActivate)
				{
					// je nastavene, ze pri starte sa ma aktivita aktivovat
					long backgroundProfileId = Long.valueOf(GlobalData.applicationBackgroundProfile);
					if ((profile == null) && 
						(backgroundProfileId != GlobalData.PROFILE_NO_ACTIVATE))
					{
						profile = dataWrapper.getProfileById(backgroundProfileId);
					}
					actProfile = true;
				}
				else
				{
					// profile sa nema aktivovat, tak ho deaktivujeme
					dataWrapper.getDatabaseHandler().deactivateProfile();
					profile = null;
				}
				
				// start PPHelper
				//PhoneProfilesHelper.startPPHelper(getActivity().getBaseContext());
			}
			else
			{
				if (GlobalData.applicationActivate)
				{
					long backgroundProfileId = Long.valueOf(GlobalData.applicationBackgroundProfile);
					if ((profile == null) && 
						(backgroundProfileId != GlobalData.PROFILE_NO_ACTIVATE))
					{
						profile = dataWrapper.getProfileById(backgroundProfileId);
						actProfile = true;
					}
				}
			}
		}
		//Log.d("ActivateProfilesActivity.onStart", "actProfile="+String.valueOf(actProfile));

		if (actProfile && (profile != null))
		{
			// aktivacia profilu
			activateProfile(profile, GlobalData.STARTUP_SOURCE_ACTIVATOR_START);
			endOnStart();
		}
		else
		{
			updateHeader(profile);
			if (startupSource == 0)
			{
				// aktivita nebola spustena z notifikacie, ani z widgetu
				// pre profil, ktory je prave aktivny, treba aktualizovat notifikaciu a widgety 
				activateProfileHelper.showNotification(profile);
				activateProfileHelper.updateWidget();
			}
			endOnStart();
		}
		
		*/
		
		if (!GlobalData.getApplicationStarted(getActivity().getBaseContext()))
		{
			// start service for first start
			Intent firstStartServiceIntent = new Intent(getActivity().getBaseContext(), FirstStartService.class);
			getActivity().getBaseContext().startService(firstStartServiceIntent);
		}
		else
		{
			GlobalData.logE("ActivateProfileListFragment.doOnStart", "xxx");
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
		
		//Log.d("PhoneProfileActivity.onStart", "xxxx");
		
	}
	
	private void endOnStart()
	{
		// reset, aby sa to dalej chovalo ako normalne spustenie z lauchera
		startupSource = 0;

		//  aplikacia uz je 1. krat spustena - moved to FirstStartService
		//GlobalData.setApplicationStarted(getActivity().getBaseContext(), true);
	}
	
	
	@Override
	public void onStart()
	{
		super.onStart();

		//Log.d("EditorProfileListFragment.onStart", "xxxx");
		
	}
	
	@Override
	public void onDestroy()
	{
		//Log.e("ActivateProfileListFragment.onDestroy","xxx");

		if (!isAsyncTaskPendingOrRunning())
		{
			//Log.e("ActivateProfileListFragment.onDestroy","asyncTask not running");
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
		//else
			//Log.e("ActivateProfileListFragment.onDestroy","asyncTask running");
		
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
			activeProfileName.setText(profile._name);
	        if (profile.getIsIconResourceID())
	        {
				int res = getResources().getIdentifier(profile.getIconIdentifier(), "drawable", getActivity().getPackageName());
				activeProfileIcon.setImageResource(res); // resource na ikonu
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

	private void activateProfile(Profile profile, int startupSource)
	{
		dataWrapper.activateProfile(profile._id, startupSource, getActivity());
	}
	
	private void activateProfile(int position, int startupSource)
	{
		//Log.d("ActivateProfileActivity.activateProfile","size="+profileList.size());
		//Log.d("ActivateProfileActivity.activateProfile","position="+position);
		Profile profile = profileList.get(position);
		//Log.d("ActivateProfileActivity.activateProfile","profile_id="+profile._id);
		activateProfile(profile, startupSource);
	} 

	public void refreshGUI()
	{
		if ((dataWrapper == null) || (profileListAdapter == null))
			return;
		
		Profile profileFromAdapter = profileListAdapter.getActivatedProfile();
		if (profileFromAdapter != null)
			profileFromAdapter._checked = false;

		Profile profileFromDB = dataWrapper.getDatabaseHandler().getActivatedProfile();
		if (profileFromDB != null)
		{
			Profile profileFromDataWrapper = dataWrapper.getProfileById(profileFromDB._id);
			if (profileFromDataWrapper != null)
				profileFromDataWrapper._checked = true;
			updateHeader(profileFromDataWrapper);
		}
		else
			updateHeader(null);

		profileListAdapter.notifyDataSetChanged();
		
	}
	
}
