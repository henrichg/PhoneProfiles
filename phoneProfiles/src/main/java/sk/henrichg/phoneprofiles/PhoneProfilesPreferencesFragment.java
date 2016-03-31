package sk.henrichg.phoneprofiles;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.TwoStatePreference;
import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.CharacterStyle;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;

import com.fnp.materialpreferences.PreferenceFragment;

public class PhoneProfilesPreferencesFragment extends PhoneProfilesPreferencesNestedFragment
                                              implements SharedPreferences.OnSharedPreferenceChangeListener
{

    //private PreferenceManager prefMng;
    //private SharedPreferences preferences;
    public static Activity preferencesActivity = null;
    String extraScrollTo;

    //static final String PREF_APPLICATION_PERMISSIONS = "prf_pref_applicationPermissions";
    //static final int RESULT_APPLICATION_PERMISSIONS = 1990;
    //static final String PREF_WRITE_SYSTEM_SETTINGS_PERMISSIONS = "prf_pref_writeSystemSettingsPermissions";
    //static final int RESULT_WRITE_SYSTEM_SETTINGS_PERMISSIONS = 1991;


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // must by false to avoid FC when rotation changes and preference dialogs are shown
        setRetainInstance(false);

        preferencesActivity = getActivity();

        preferences = prefMng.getSharedPreferences();
        preferences.registerOnSharedPreferenceChangeListener(this);

        extraScrollTo = getArguments().getString(PhoneProfilesPreferencesActivity.EXTRA_SCROLL_TO, "");

        updateSharedPreference();

    }

    @Override
    public void addPreferencesFromResource(int preferenceResId) {
        prefMng = getPreferenceManager();
        prefMng.setSharedPreferencesName(GlobalData.APPLICATION_PREFS_NAME);
        prefMng.setSharedPreferencesMode(Activity.MODE_PRIVATE);
        super.addPreferencesFromResource(preferenceResId);
    }

    @Override
    public int addPreferencesFromResource() {
        return R.xml.phone_profiles_preferences;
    }

    private void updateSharedPreference()
    {
        setSummary(GlobalData.PREF_APPLICATION_START_ON_BOOT);
        setSummary(GlobalData.PREF_APPLICATION_ACTIVATE);
        setSummary(GlobalData.PREF_APPLICATION_ALERT);
        setSummary(GlobalData.PREF_APPLICATION_CLOSE);
        setSummary(GlobalData.PREF_APPLICATION_LONG_PRESS_ACTIVATION);
        setSummary(GlobalData.PREF_APPLICATION_LANGUAGE);
        setSummary(GlobalData.PREF_APPLICATION_THEME);
        setSummary(GlobalData.PREF_APPLICATION_ACTIVATOR_PREF_INDICATOR);
        setSummary(GlobalData.PREF_APPLICATION_EDITOR_PREF_INDICATOR);
        setSummary(GlobalData.PREF_APPLICATION_ACTIVATOR_HEADER);
        setSummary(GlobalData.PREF_APPLICATION_EDITOR_HEADER);
        setSummary(GlobalData.PREF_NOTIFICATION_TOAST);
        setSummary(GlobalData.PREF_NOTIFICATION_STATUS_BAR);

        if (android.os.Build.VERSION.SDK_INT >= 16) {
            setSummary(GlobalData.PREF_NOTIFICATION_SHOW_IN_STATUS_BAR);
            if (android.os.Build.VERSION.SDK_INT >= 21) {
                Preference preference = prefMng.findPreference(GlobalData.PREF_NOTIFICATION_SHOW_IN_STATUS_BAR);
                if (preference != null) {
                    preference.setTitle(R.string.phone_profiles_pref_notificationShowInStatusBarAndLockscreen);
                }
            }
        }
        else {
            Preference preference = prefMng.findPreference(GlobalData.PREF_NOTIFICATION_SHOW_IN_STATUS_BAR);
            if (preference != null) {
                preference.setEnabled(false);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean(GlobalData.PREF_NOTIFICATION_SHOW_IN_STATUS_BAR, true);
                editor.commit();
            }
        }

        setSummary(GlobalData.PREF_NOTIFICATION_STATUS_BAR_PERMANENT);
        //setSummary(GlobalData.PREF_NOTIFICATION_STATUS_BAR_CANCEL);

        if (android.os.Build.VERSION.SDK_INT >= 21)
        {
            // for Android 5.0, color notification icon is not supported
            Preference preference = prefMng.findPreference(GlobalData.PREF_NOTIFICATION_STATUS_BAR_STYLE);
            if (preference != null)
            {
                //PreferenceCategory preferenceCategory = (PreferenceCategory) findPreference("categoryNotifications");
                PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("categoryNotifications");
                preferenceCategory.removePreference(preference);
            }
        }
        else
            setSummary(GlobalData.PREF_NOTIFICATION_STATUS_BAR_STYLE);

        setSummary(GlobalData.PREF_APPLICATION_WIDGET_LIST_PREF_INDICATOR);
        setSummary(GlobalData.PREF_APPLICATION_WIDGET_LIST_HEADER);
        setSummary(GlobalData.PREF_APPLICATION_WIDGET_LIST_BACKGROUND);
        setSummary(GlobalData.PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_B);
        setSummary(GlobalData.PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_T);
        setSummary(GlobalData.PREF_APPLICATION_WIDGET_ICON_COLOR);
        setSummary(GlobalData.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS);
        setSummary(GlobalData.PREF_APPLICATION_WIDGET_LIST_ICON_COLOR);
        setSummary(GlobalData.PREF_APPLICATION_WIDGET_LIST_ICON_LIGHTNESS);
        setSummary(GlobalData.PREF_APPLICATION_BACKGROUND_PROFILE);
        setSummary(GlobalData.PREF_APPLICATION_ACTIVATOR_GRID_LAYOUT);
        setSummary(GlobalData.PREF_APPLICATION_WIDGET_LIST_GRID_LAYOUT);
        setSummary(GlobalData.PREF_APPLICATION_WIDGET_ICON_HIDE_PROFILE_NAME);
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

    static public Activity getPreferencesActivity()
    {
        return preferencesActivity;
    }

}
