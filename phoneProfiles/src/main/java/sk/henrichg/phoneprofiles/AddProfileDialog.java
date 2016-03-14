package sk.henrichg.phoneprofiles;

import android.content.Context;
import android.support.v7.app.AppCompatDialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class AddProfileDialog
{
    private AddProfileAdapter addProfileAdapter;

    public List<Profile> profileList;

    private Context _context;
    private EditorProfileListFragment profileListFragment;

    //private MaterialDialog mDialog;
    private AppCompatDialog mDialog;
    private ListView listView;

    public AddProfileDialog(Context context, EditorProfileListFragment profileListFragment)
    {
        _context = context;
        this.profileListFragment = profileListFragment;

        profileList = new ArrayList<Profile>();

        boolean monochrome = false;
        int monochromeValue = 0xFF;

        Profile profile;
        profile = profileListFragment.dataWrapper.getNoinitializedProfile(
                                        context.getResources().getString(R.string.profile_name_default),
                                        GlobalData.PROFILE_ICON_DEFAULT, 0);
        profile.generateIconBitmap(context, monochrome, monochromeValue);
        profile.generatePreferencesIndicator(context, monochrome, monochromeValue);
        profileList.add(profile);
        for (int index = 0; index < 6; index++) {
            profile = profileListFragment.dataWrapper.getDefaultProfile(index, false);
            profile.generateIconBitmap(context, monochrome, monochromeValue);
            profile.generatePreferencesIndicator(context, monochrome, monochromeValue);
            profileList.add(profile);
        }

        mDialog = new AppCompatDialog(context);
        mDialog.setTitle(R.string.new_profile_predefined_profiles_dialog);
        mDialog.setContentView(R.layout.activity_profile_pref_dialog);

        listView = (ListView)mDialog.findViewById(R.id.profile_pref_dlg_listview);

        addProfileAdapter = new AddProfileAdapter(this, _context, profileList);
        listView.setAdapter(addProfileAdapter);

        listView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                doOnItemSelected(position);
            }

        });

    }

    public void doOnItemSelected(int position)
    {
        profileListFragment.startProfilePreferencesActivity(null, position);
        mDialog.dismiss();
    }

    public void show() {
        mDialog.show();
    }

}
