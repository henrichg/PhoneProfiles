package sk.henrichg.phoneprofiles;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;

public class PhoneProfilesPreferencesFragment extends PhoneProfilesPreferencesNestedFragment
                                              implements SharedPreferences.OnSharedPreferenceChangeListener
{

    //private PreferenceManager prefMng;
    //private SharedPreferences preferences;
    String extraScrollTo;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // must by false to avoid FC when rotation changes and preference dialogs are shown
        setRetainInstance(false);

        preferences = prefMng.getSharedPreferences();
        preferences.registerOnSharedPreferenceChangeListener(this);

        extraScrollTo = getArguments().getString(PhoneProfilesPreferencesActivity.EXTRA_SCROLL_TO, "");

        updateSharedPreference();

    }

    @Override
    public void addPreferencesFromResource(int preferenceResId) {
        prefMng = getPreferenceManager();
        prefMng.setSharedPreferencesName(PPApplication.APPLICATION_PREFS_NAME);
        prefMng.setSharedPreferencesMode(Activity.MODE_PRIVATE);
        super.addPreferencesFromResource(preferenceResId);
    }

    @Override
    public int addPreferencesFromResource() {
        return R.xml.phone_profiles_preferences;
    }

    private void updateSharedPreference()
    {
        setSummary(PPApplication.PREF_APPLICATION_START_ON_BOOT);
        setSummary(PPApplication.PREF_APPLICATION_ACTIVATE);
        setSummary(PPApplication.PREF_APPLICATION_ALERT);
        setSummary(PPApplication.PREF_APPLICATION_CLOSE);
        setSummary(PPApplication.PREF_APPLICATION_LONG_PRESS_ACTIVATION);
        setSummary(PPApplication.PREF_APPLICATION_LANGUAGE);
        setSummary(PPApplication.PREF_APPLICATION_THEME);
        setSummary(PPApplication.PREF_APPLICATION_ACTIVATOR_PREF_INDICATOR);
        setSummary(PPApplication.PREF_APPLICATION_EDITOR_PREF_INDICATOR);
        setSummary(PPApplication.PREF_APPLICATION_ACTIVATOR_HEADER);
        setSummary(PPApplication.PREF_APPLICATION_EDITOR_HEADER);
        setSummary(PPApplication.PREF_NOTIFICATION_TOAST);
        setSummary(PPApplication.PREF_NOTIFICATION_STATUS_BAR);
        setSummary(PPApplication.PREF_NOTIFICATION_TEXT_COLOR);
        setSummary(PPApplication.PREF_NOTIFICATION_THEME);

        if (android.os.Build.VERSION.SDK_INT >= 16) {
            setSummary(PPApplication.PREF_NOTIFICATION_SHOW_IN_STATUS_BAR);
            if (android.os.Build.VERSION.SDK_INT >= 21) {
                Preference preference = prefMng.findPreference(PPApplication.PREF_NOTIFICATION_SHOW_IN_STATUS_BAR);
                if (preference != null) {
                    preference.setTitle(R.string.phone_profiles_pref_notificationShowInStatusBarAndLockscreen);
                }
            }
        }
        else {
            Preference preference = prefMng.findPreference(PPApplication.PREF_NOTIFICATION_SHOW_IN_STATUS_BAR);
            if (preference != null) {
                preference.setEnabled(false);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean(PPApplication.PREF_NOTIFICATION_SHOW_IN_STATUS_BAR, true);
                editor.commit();
            }
        }

        setSummary(PPApplication.PREF_NOTIFICATION_STATUS_BAR_PERMANENT);
        //setSummary(PPApplication.PREF_NOTIFICATION_STATUS_BAR_CANCEL);

        // some devices supports color icons
        /*if (android.os.Build.VERSION.SDK_INT >= 21)
        {
            // for Android 5.0, color notification icon is not supported
            Preference preference = prefMng.findPreference(PPApplication.PREF_NOTIFICATION_STATUS_BAR_STYLE);
            if (preference != null)
            {
                //PreferenceCategory preferenceCategory = (PreferenceCategory) findPreference("categoryNotifications");
                PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("categoryNotifications");
                preferenceCategory.removePreference(preference);
            }
        }
        else*/
            setSummary(PPApplication.PREF_NOTIFICATION_STATUS_BAR_STYLE);

        setSummary(PPApplication.PREF_APPLICATION_WIDGET_LIST_PREF_INDICATOR);
        setSummary(PPApplication.PREF_APPLICATION_WIDGET_LIST_HEADER);
        setSummary(PPApplication.PREF_APPLICATION_WIDGET_LIST_BACKGROUND);
        setSummary(PPApplication.PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_B);
        setSummary(PPApplication.PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_T);
        setSummary(PPApplication.PREF_APPLICATION_WIDGET_ICON_COLOR);
        setSummary(PPApplication.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS);
        setSummary(PPApplication.PREF_APPLICATION_WIDGET_LIST_ICON_COLOR);
        setSummary(PPApplication.PREF_APPLICATION_WIDGET_LIST_ICON_LIGHTNESS);
        setSummary(PPApplication.PREF_APPLICATION_BACKGROUND_PROFILE);
        setSummary(PPApplication.PREF_APPLICATION_ACTIVATOR_GRID_LAYOUT);
        setSummary(PPApplication.PREF_APPLICATION_WIDGET_LIST_GRID_LAYOUT);
        setSummary(PPApplication.PREF_APPLICATION_WIDGET_ICON_HIDE_PROFILE_NAME);
        setSummary(PPApplication.PREF_APPLICATION_WIDGET_ICON_BACKGROUND);
        setSummary(PPApplication.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_B);
        setSummary(PPApplication.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_T);
    }

    @Override
    public void onStart()
    {
        super.onStart();

        /*
        PreferenceCategory scrollCategory = (PreferenceCategory) findPreference(extraScrollTo);
        if (scrollCategory != null) {
            // scroll to category
            for (int i = 0; i <  getPreferenceScreen().getRootAdapter().getCount(); i++){
                Object o = getPreferenceScreen().getRootAdapter().getItem(i);
                if (o instanceof PreferenceCategory ){
                    if (o.equals(scrollCategory)){
                        ListView listView = (ListView) getActivity().findViewById(android.R.id.list);
                        if (listView != null)
                            listView.setSelection(i);
                    }
                }
            }
        }
        */

        if (extraScrollTo != null) {
            PreferenceScreen preferenceScreen = (PreferenceScreen) findPreference("rootScreen");
            Preference preference = findPreference(extraScrollTo);
            if (preference != null) {
                int pos = preference.getOrder();
                preferenceScreen.onItemClick(null, null, pos, 0);
            }
            extraScrollTo = null;
        }

    }

}
