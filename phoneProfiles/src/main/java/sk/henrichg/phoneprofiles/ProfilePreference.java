package sk.henrichg.phoneprofiles;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

public class ProfilePreference extends Preference {

    private String profileId;
    //private CharSequence preferenceTitle;
    int addNoActivateItem;

    private Context prefContext;

    public static DataWrapper dataWrapper;


    public ProfilePreference(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ProfilePreference);

        addNoActivateItem = typedArray.getInt(R.styleable.ProfilePreference_addNoActivateItem, 0);

        profileId = "0";
        prefContext = context;
        //preferenceTitle = getTitle();

        dataWrapper = new DataWrapper(context, true, false, 0);

        setWidgetLayoutResource(R.layout.profile_preference); // resource na layout custom preference - TextView-ImageView

        typedArray.recycle();

    }

    //@Override
    protected void onBindView(View view)
    {
        super.onBindView(view);

        //preferenceTitleView = (TextView)view.findViewById(R.id.applications_pref_label);  // resource na title
        //preferenceTitleView.setText(preferenceTitle);

        ImageView profileIcon = (ImageView)view.findViewById(R.id.profile_pref_icon); // resource na ImageView v custom preference layoute

        if (profileIcon != null)
        {
            Profile profile = dataWrapper.getProfileById(Long.parseLong(profileId));
            if (profile != null)
            {
                if (profile.getIsIconResourceID())
                {
                    if (profile._iconBitmap != null)
                        profileIcon.setImageBitmap(profile._iconBitmap);
                    else {
                        //profileIcon.setImageBitmap(null);
                        int res = prefContext.getResources().getIdentifier(profile.getIconIdentifier(), "drawable",
                                prefContext.getPackageName());
                        profileIcon.setImageResource(res); // resource na ikonu
                    }
                }
                else
                {
                    profileIcon.setImageBitmap(profile._iconBitmap);
                }
            }
            else
            {
                //if ((addNoActivateItem == 1) && (Long.parseLong(profileId) == PPApplication.PROFILE_NO_ACTIVATE))
                //    profileIcon.setImageResource(R.drawable.ic_profile_default); // resource na ikonu
                //else
                    profileIcon.setImageResource(R.drawable.ic_empty); // resource na ikonu
            }
            setSummary(Long.parseLong(profileId));
        }
    }

    @Override
    protected void onClick()
    {
        // klik na preference

        final ProfilePreferenceDialog dialog = new ProfilePreferenceDialog(prefContext, this, profileId);
        dialog.show();
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index)
    {
        super.onGetDefaultValue(a, index);

        return a.getString(index);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue)
    {
        if (restoreValue) {
            // restore state
            String value;
            try {
                value = getPersistedString(profileId);
            } catch  (Exception e) {
                value = profileId;
            }
            profileId = value;
        }
        else {
            // set state
            String value = (String) defaultValue;
            profileId = value;
            persistString(value);
        }
    }

    @Override
    protected Parcelable onSaveInstanceState()
    {
        // ulozime instance state - napriklad kvoli zmene orientacie

        final Parcelable superState = super.onSaveInstanceState();
        /*if (isPersistent()) {
            // netreba ukladat, je ulozene persistentne
            return superState;
        }*/

        // ulozenie istance state
        final SavedState myState = new SavedState(superState);
        myState.profileId = profileId;
        myState.addNoActivateItem = addNoActivateItem;
        return myState;

    }

    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        if (dataWrapper == null)
            dataWrapper = new DataWrapper(prefContext, true, false, 0);

        if (!state.getClass().equals(SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            setSummary(Long.parseLong(profileId));
            return;
        }

        // restore instance state
        SavedState myState = (SavedState)state;
        super.onRestoreInstanceState(myState.getSuperState());
        profileId = myState.profileId;
        addNoActivateItem = myState.addNoActivateItem;

        setSummary(Long.parseLong(profileId));
        notifyChanged();
    }

    @Override
    protected void onPrepareForRemoval()
    {
        super.onPrepareForRemoval();
        dataWrapper.invalidateDataWrapper();
        dataWrapper = null;
    }

    /*
    public String getProfileId()
    {
        return profileId;
    }
    */

    void setProfileId(long newProfileId)
    {
        String newValue = String.valueOf(newProfileId);

        if (!callChangeListener(newValue)) {
            // nema sa nova hodnota zapisat
            return;
        }

        profileId = newValue;

        // set summary
        setSummary(Long.parseLong(profileId));

        // zapis do preferences
        persistString(newValue);

        // Data sa zmenili,notifikujeme
        notifyChanged();

    }

    public void setSummary(long profileId)
    {
        Profile profile = dataWrapper.getProfileById(profileId);
        if (profile != null)
        {
            setSummary(profile._name);
        }
        else
        {
            if ((addNoActivateItem == 1) && (profileId == PPApplication.PROFILE_NO_ACTIVATE))
                setSummary(prefContext.getResources().getString(R.string.profile_preference_profile_end_no_activate));
            else
                setSummary(prefContext.getResources().getString(R.string.profile_preference_profile_not_set));
        }
    }

    // SavedState class
    private static class SavedState extends BaseSavedState
    {
        String profileId;
        int addNoActivateItem;

        SavedState(Parcel source)
        {
            super(source);

            // restore data
            profileId = source.readString();
            addNoActivateItem = source.readInt();

        }

        @Override
        public void writeToParcel(Parcel dest, int flags)
        {
            super.writeToParcel(dest, flags);

            // save data
            dest.writeString(profileId);
            dest.writeInt(addNoActivateItem);
        }

        SavedState(Parcelable superState)
        {
            super(superState);
        }

        @SuppressWarnings("unused")
        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in)
            {
                return new SavedState(in);
            }
            public SavedState[] newArray(int size)
            {
                return new SavedState[size];
            }

        };

    }
}
