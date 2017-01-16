package sk.henrichg.phoneprofiles;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

class EditorProfileListAdapter extends BaseAdapter
{

    private EditorProfileListFragment fragment;
    private DataWrapper dataWrapper;
    private List<Profile> profileList;

    EditorProfileListAdapter(EditorProfileListFragment f, DataWrapper pdw)
    {
        fragment = f;
        dataWrapper = pdw;
        profileList = dataWrapper.getProfileList();
    }

    public void release()
    {
        fragment = null;
        profileList = null;
    }

    public int getCount()
    {
        if (profileList == null)
            return 0;

        return profileList.size();
    }

    public Object getItem(int position)
    {
        if (profileList == null)
            return null;

        if (profileList.size() == 0)
            return null;

        return profileList.get(position);
    }

    public long getItemId(int position)
    {
        return position;
    }

    public int getItemId(Profile profile)
    {
        if (profileList == null)
            return -1;

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

    void addItem(Profile profile, boolean refresh)
    {
        if (profileList == null)
            return;

        profileList.add(profile);
        if (refresh)
            notifyDataSetChanged();
    }

/*	
    public void updateItem(Profile profile)
    {
        notifyDataSetChanged();
    }
*/	
    void deleteItemNoNotify(Profile profile)
    {
        dataWrapper.deleteProfile(profile);
    }

/*
    public void deleteItem(Profile profile)
    {
        deleteItemNoNotify(profile);
        notifyDataSetChanged();
    }
*/

    void clearNoNotify()
    {
        dataWrapper.deleteAllProfiles();
    }

    public void clear()
    {
        clearNoNotify();
        notifyDataSetChanged();
    }

    void changeItemOrder(int from, int to)
    {
        if (profileList == null)
            return;

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
        if (profileList == null)
            return null;

        for (Profile p : profileList)
        {
            if (p._checked)
            {
                return p;
            }
        }

        return null;
    }

    public void activateProfile(Profile profile)
    {
        if (profileList == null)
            return;

        for (Profile p : profileList)
        {
            p._checked = false;
        }

        // teraz musime najst profile v profileList
        int position = getItemId(profile);
        if (position != -1)
        {
            // najdenemu objektu nastavime _checked
            Profile _profile = profileList.get(position);
            if (_profile != null)
                _profile._checked = true;
        }
        notifyDataSetChanged();
    }

    void notifyDataSetChanged(boolean refreshIcons) {
        if (refreshIcons) {
            for (Profile profile : profileList) {
                dataWrapper.refreshProfileIcon(profile, false, 0);
            }
        }
        notifyDataSetChanged();
    }

    static class ViewHolder {
          RelativeLayout listItemRoot;
          ImageView profileIcon;
          TextView profileName;
          ImageView profileIndicator;
          ImageView profileItemEditMenu;
          int position;
        }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        ViewHolder holder;

        View vi = convertView;
        if (convertView == null)
        {
            LayoutInflater inflater = LayoutInflater.from(fragment.getActivity());
            if (PPApplication.applicationEditorPrefIndicator)
                vi = inflater.inflate(R.layout.editor_profile_list_item, parent, false);
            else
                vi = inflater.inflate(R.layout.editor_profile_list_item_no_indicator, parent, false);
            holder = new ViewHolder();
            holder.listItemRoot = (RelativeLayout)vi.findViewById(R.id.main_list_item_root);
            holder.profileName = (TextView)vi.findViewById(R.id.main_list_item_profile_name);
            holder.profileIcon = (ImageView)vi.findViewById(R.id.main_list_item_profile_icon);
            holder.profileItemEditMenu = (ImageView)vi.findViewById(R.id.main_list_item_edit_menu);
            if (PPApplication.applicationEditorPrefIndicator)
                holder.profileIndicator = (ImageView)vi.findViewById(R.id.main_list_profile_pref_indicator);
            vi.setTag(holder);        
        }
        else
        {
            holder = (ViewHolder)vi.getTag();
        }

        final Profile profile = profileList.get(position);

        if (profile._checked && (!PPApplication.applicationEditorHeader))
        {
            if (PPApplication.applicationTheme.equals("material"))
                holder.listItemRoot.setBackgroundResource(R.drawable.header_card_dlight);
            else
            if (PPApplication.applicationTheme.equals("dark"))
                holder.listItemRoot.setBackgroundResource(R.drawable.header_card_dark);
            else
            if (PPApplication.applicationTheme.equals("dlight"))
                holder.listItemRoot.setBackgroundResource(R.drawable.header_card_dlight);
            holder.profileName.setTypeface(null, Typeface.BOLD);
        }
        else
        {
            if (PPApplication.applicationTheme.equals("material"))
                holder.listItemRoot.setBackgroundResource(R.drawable.card);
            else
            if (PPApplication.applicationTheme.equals("dark"))
                holder.listItemRoot.setBackgroundResource(R.drawable.card_dark);
            else
            if (PPApplication.applicationTheme.equals("dlight"))
                holder.listItemRoot.setBackgroundResource(R.drawable.card);
            holder.profileName.setTypeface(null, Typeface.NORMAL);
        }

        String profileName = profile.getProfileNameWithDuration(false, dataWrapper.context);
        holder.profileName.setText(profileName);

        if (profile.getIsIconResourceID())
        {
            if (profile._iconBitmap != null)
                holder.profileIcon.setImageBitmap(profile._iconBitmap);
            else {
                //holder.profileIcon.setImageBitmap(null);
                int res = vi.getResources().getIdentifier(profile.getIconIdentifier(), "drawable",
                        vi.getContext().getPackageName());
                holder.profileIcon.setImageResource(res); // resource na ikonu
            }
        }
        else
        {
            holder.profileIcon.setImageBitmap(profile._iconBitmap);
        }
        
        if (PPApplication.applicationEditorPrefIndicator)
        {
            //profilePrefIndicatorImageView.setImageBitmap(null);
            //Bitmap bitmap = ProfilePreferencesIndicator.paint(profile, vi.getContext());
            //profilePrefIndicatorImageView.setImageBitmap(bitmap);
            if (holder.profileIndicator != null)
                holder.profileIndicator.setImageBitmap(profile._preferencesIndicator);
        }
        
        holder.profileItemEditMenu.setTag(profile);
        final ImageView profileItemEditMenu = holder.profileItemEditMenu;
        holder.profileItemEditMenu.setOnClickListener(new OnClickListener() {

                public void onClick(View v) {
                    fragment.showEditMenu(profileItemEditMenu);
                }
            });

        return vi;
    }

}
