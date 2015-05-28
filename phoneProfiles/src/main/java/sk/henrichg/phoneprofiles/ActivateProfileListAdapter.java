package sk.henrichg.phoneprofiles;

import android.app.Fragment;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

public class ActivateProfileListAdapter extends BaseAdapter
{

	private List<Profile> profileList;
	private Fragment fragment;
	
	public ActivateProfileListAdapter(Fragment f, List<Profile> pl)
	{
		fragment = f;
		profileList = pl;
	}   
	
	public void release()
	{
		fragment = null;
		profileList = null;
	}
	
	public int getCount()
	{
		return profileList.size();
	}

	public Object getItem(int position)
	{
		return profileList.get(position);
	}

	public long getItemId(int position)
	{
		return position;
	}

	public int getItemId(Profile profile)
	{
		for (int i = 0; i < profileList.size(); i++)
		{
			if (profileList.get(i)._id == profile._id)
				return i;
		}
		return -1;
	}
	
	public void setList(List<Profile> pl)
	{
		profileList = pl;
		notifyDataSetChanged();
	}
	
	public void addItem(Profile profile)
	{
		int maxPOrder = 0;
		int pOrder;
		for (Profile p : profileList)
		{
			pOrder = p._porder;
			if (pOrder > maxPOrder) maxPOrder = pOrder;
		}
		profile._porder = maxPOrder+1;
		profileList.add(profile);
		notifyDataSetChanged();
	}

	public void updateItem(Profile profile)
	{
		notifyDataSetChanged();
	}
	
	public void deleteItem(Profile profile)
	{
		profileList.remove(profile);
		notifyDataSetChanged();
	}
	
	public void changeItemOrder(int from, int to)
	{
		Profile profile = profileList.get(from);
		profileList.remove(from);
		profileList.add(to, profile);
		for (int i = 0; i < profileList.size(); i++)
		{
			profileList.get(i)._porder = i+1;
		}
		notifyDataSetChanged();
	}
	
	public Profile getActivatedProfile()
	{
		for (Profile p : profileList)
		{
			if (p._checked)
			{
				return p;
			}
		}
		
		return null;
	}

	static class ViewHolder {
		  RelativeLayout listItemRoot;
		  ImageView profileIcon;
		  TextView profileName;
		  ImageView profileIndicator;
		  int position;
		}
	
	public View getView(int position, View convertView, ViewGroup parent)
	{
		ViewHolder holder;
		
		View vi = convertView;
        if (convertView == null)
        {
            holder = new ViewHolder();
    		LayoutInflater inflater = LayoutInflater.from(fragment.getActivity());
			if (!GlobalData.applicationActivatorGridLayout)
			{
	        	if (GlobalData.applicationActivatorPrefIndicator)
	        		vi = inflater.inflate(R.layout.activate_profile_list_item, parent, false);
	        	else
	        		vi = inflater.inflate(R.layout.activate_profile_list_item_no_indicator, parent, false);
	            holder.listItemRoot = (RelativeLayout)vi.findViewById(R.id.act_prof_list_item_root);
	            holder.profileName = (TextView)vi.findViewById(R.id.act_prof_list_item_profile_name);
	            holder.profileIcon = (ImageView)vi.findViewById(R.id.act_prof_list_item_profile_icon);
	    		if (GlobalData.applicationActivatorPrefIndicator)
	    			holder.profileIndicator = (ImageView)vi.findViewById(R.id.act_prof_list_profile_pref_indicator);
			}
			else
			{
	      		vi = inflater.inflate(R.layout.activate_profile_grid_item, parent, false);
		        holder.listItemRoot = (RelativeLayout)vi.findViewById(R.id.act_prof_list_item_root);
		        holder.profileName = (TextView)vi.findViewById(R.id.act_prof_list_item_profile_name);
		        holder.profileIcon = (ImageView)vi.findViewById(R.id.act_prof_list_item_profile_icon);
			}
            vi.setTag(holder);        
        }
        else
        {
        	holder = (ViewHolder)vi.getTag();
        }

        Profile profile = profileList.get(position);

        if (profile._checked && (!GlobalData.applicationActivatorHeader))
        {
        	if (GlobalData.applicationTheme.equals("material"))
        		holder.listItemRoot.setBackgroundResource(R.drawable.header_card);
        	else
           	if (GlobalData.applicationTheme.equals("dark"))
           		holder.listItemRoot.setBackgroundResource(R.drawable.header_card_dark);
        	else
           	if (GlobalData.applicationTheme.equals("dlight"))
           		holder.listItemRoot.setBackgroundResource(R.drawable.header_card);
        	holder.profileName.setTypeface(null, Typeface.BOLD);
        }
        else
        {
        	if (GlobalData.applicationTheme.equals("material"))
        		holder.listItemRoot.setBackgroundResource(R.drawable.card);
        	else
           	if (GlobalData.applicationTheme.equals("dark"))
           		holder.listItemRoot.setBackgroundResource(R.drawable.card_dark);
        	else
           	if (GlobalData.applicationTheme.equals("dlight"))
           		holder.listItemRoot.setBackgroundResource(R.drawable.card);
        	holder.profileName.setTypeface(null, Typeface.NORMAL);
        }

		String profileName = profile._name;
		if ((profile._duration > 0) && (profile._afterDurationDo != Profile.AFTERDURATIONDO_NOTHING))
			profileName = "[" + profile._duration + "] " + profileName;
        holder.profileName.setText(profileName);

        if (profile.getIsIconResourceID())
        {
        	holder.profileIcon.setImageResource(0);
        	int res = vi.getResources().getIdentifier(profile.getIconIdentifier(), "drawable", 
        				vi.getContext().getPackageName());
        	holder.profileIcon.setImageResource(res); // resource na ikonu
        }
        else
        {
        	//profileIcon.setImageBitmap(null);
        /*	Resources resources = vi.getResources();
    		int height = (int) resources.getDimension(android.R.dimen.app_icon_size);
    		int width = (int) resources.getDimension(android.R.dimen.app_icon_size);
    		Bitmap bitmap = BitmapResampler.resample(profile.getIconIdentifier(), width, height);
        	profileIcon.setImageBitmap(bitmap); */
        	holder.profileIcon.setImageBitmap(profile._iconBitmap);
        }

		if ((GlobalData.applicationActivatorPrefIndicator) && (!GlobalData.applicationActivatorGridLayout))
		{
			//profilePrefIndicatorImageView.setImageBitmap(null);
			//Bitmap bitmap = ProfilePreferencesIndicator.paint(profile, vi.getContext());
			//profilePrefIndicatorImageView.setImageBitmap(bitmap);
			holder.profileIndicator.setImageBitmap(profile._preferencesIndicator);
		}

        /*		ImageView profileItemEditMenu = (ImageView)vi.findViewById(R.id.act_prof_list_item_edit_menu);
		profileItemEditMenu.setTag(position);
		profileItemEditMenu.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					activity.openContextMenu(v);
				}
			});
*/		
			
		return vi;
	}

}
