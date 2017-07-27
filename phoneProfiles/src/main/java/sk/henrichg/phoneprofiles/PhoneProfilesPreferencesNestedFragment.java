package sk.henrichg.phoneprofiles;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import android.support.v7.app.AlertDialog;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;

import static android.app.Activity.RESULT_CANCELED;

public class PhoneProfilesPreferencesNestedFragment extends PreferenceFragment
                                              implements SharedPreferences.OnSharedPreferenceChangeListener
{

    protected PreferenceManager prefMng;
    protected SharedPreferences preferences;

    private static final String PREF_APPLICATION_PERMISSIONS = "permissionsApplicationPermissions";
    private static final int RESULT_APPLICATION_PERMISSIONS = 1990;
    private static final String PREF_WRITE_SYSTEM_SETTINGS_PERMISSIONS = "permissionsWriteSystemSettingsPermissions";
    private static final int RESULT_WRITE_SYSTEM_SETTINGS_PERMISSIONS = 1991;
    private static final String PREF_APPLICATION_LANGUAGE_24 = "applicationLanguage24";
    //static final int RESULT_LOCALE_SETTINGS = 1992;
    private static final String PREF_ACCESS_NOTIFICATION_POLICY_PERMISSIONS = "permissionsAccessNotificationPolicyPermissions";
    private static final int RESULT_ACCESS_NOTIFICATION_POLICY_PERMISSIONS = 1993;
    private static final String PREF_DRAW_OVERLAYS_PERMISSIONS = "permissionsDrawOverlaysPermissions";
    private static final int RESULT_DRAW_OVERLAYS_POLICY_PERMISSIONS = 1998;

    @Override
    public int addPreferencesFromResource() {
        return -1;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // must by false to avoid FC when rotation changes and preference dialogs are shown
        setRetainInstance(false);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        prefMng = getPreferenceManager();
        prefMng.setSharedPreferencesName(PPApplication.APPLICATION_PREFS_NAME);
        prefMng.setSharedPreferencesMode(Activity.MODE_PRIVATE);

        preferences = prefMng.getSharedPreferences();
        preferences.registerOnSharedPreferenceChangeListener(this);

        PreferenceScreen systemCategory = (PreferenceScreen) findPreference("categorySystem");
        if (!ActivateProfileHelper.getMergedRingNotificationVolumes(getActivity().getApplicationContext())) {
            Preference preference = findPreference(ApplicationPreferences.PREF_APPLICATION_UNLINK_RINGER_NOTIFICATION_VOLUMES);
            if (preference != null)
                systemCategory.removePreference(preference);
        }
        else {
            Preference preference = findPreference(ApplicationPreferences.PREF_APPLICATION_RINGER_NOTIFICATION_VOLUMES_UNLINKED_INFO);
            if (preference != null)
                systemCategory.removePreference(preference);
        }

        /*if (Build.VERSION.SDK_INT >= 24) {
            PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("applicationInterfaceCategory");
            Preference preference = findPreference(ApplicationPreferences.PREF_APPLICATION_LANGUAGE);
            if (preference != null)
                preferenceCategory.removePreference(preference);
            preference = findPreference(PREF_APPLICATION_LANGUAGE_24);
            if (preference != null) {
                preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_LOCALE_SETTINGS, getActivity().getApplicationContext())) {
                            Intent intent = new Intent(Settings.ACTION_LOCALE_SETTINGS);
                            startActivityForResult(intent, RESULT_LOCALE_SETTINGS);
                        }
                        else {
                            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                            dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                            //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                            dialogBuilder.setPositiveButton(android.R.string.ok, null);
                            dialogBuilder.show();
                        }
                        return false;
                    }
                });
            }
        }
        else {*/
            PreferenceScreen _preferenceCategory = (PreferenceScreen) findPreference("applicationInterfaceCategory");
            Preference _preference = findPreference(PREF_APPLICATION_LANGUAGE_24);
            if (_preference != null)
                _preferenceCategory.removePreference(_preference);
        //}
        if (Build.VERSION.SDK_INT >= 23) {
            Preference preference = prefMng.findPreference(PREF_APPLICATION_PERMISSIONS);
            if (preference != null) {
                //preference.setWidgetLayoutResource(R.layout.start_activity_preference);
                preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        //intent.addCategory(Intent.CATEGORY_DEFAULT);
                        intent.setData(Uri.parse("package:sk.henrichg.phoneprofiles"));
                        if (GlobalGUIRoutines.activityIntentExists(intent, getActivity().getApplicationContext())) {
                            startActivityForResult(intent, RESULT_APPLICATION_PERMISSIONS);
                        }
                        return false;
                    }
                });
            }
            preference = prefMng.findPreference(PREF_WRITE_SYSTEM_SETTINGS_PERMISSIONS);
            if (preference != null) {
                //preference.setWidgetLayoutResource(R.layout.start_activity_preference);
                preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_MANAGE_WRITE_SETTINGS, getActivity().getApplicationContext())) {
                            @SuppressLint("InlinedApi")
                            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                            //intent.addCategory(Intent.CATEGORY_DEFAULT);
                            startActivityForResult(intent, RESULT_WRITE_SYSTEM_SETTINGS_PERMISSIONS);
                        }
                        else {
                            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                            dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                            //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                            dialogBuilder.setPositiveButton(android.R.string.ok, null);
                            dialogBuilder.show();
                        }
                        return false;
                    }
                });
            }
            preference = prefMng.findPreference(PREF_ACCESS_NOTIFICATION_POLICY_PERMISSIONS);
            if (preference != null) {
                boolean a60 = (android.os.Build.VERSION.SDK_INT == 23) && Build.VERSION.RELEASE.equals("6.0");
                if ((android.os.Build.VERSION.SDK_INT >= 23) && (!a60) &&
                        GlobalGUIRoutines.activityActionExists(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS, getActivity())) {
                    //preference.setWidgetLayoutResource(R.layout.start_activity_preference);
                    preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            @SuppressLint("InlinedApi")
                            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                            //intent.addCategory(Intent.CATEGORY_DEFAULT);
                            startActivityForResult(intent, RESULT_ACCESS_NOTIFICATION_POLICY_PERMISSIONS);
                            return false;
                        }
                    });
                } else {
                    PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("categoryPermissions");
                    preferenceCategory.removePreference(preference);
                }
            }
            preference = prefMng.findPreference(PREF_DRAW_OVERLAYS_PERMISSIONS);
            if (preference != null) {
                //if (android.os.Build.VERSION.SDK_INT >= 25) {
                    //preference.setWidgetLayoutResource(R.layout.start_activity_preference);
                    preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, getActivity().getApplicationContext())) {
                                @SuppressLint("InlinedApi")
                                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                                //intent.addCategory(Intent.CATEGORY_DEFAULT);
                                startActivityForResult(intent, RESULT_DRAW_OVERLAYS_POLICY_PERMISSIONS);
                            }
                            else {
                                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                                dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                                //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                                dialogBuilder.setPositiveButton(android.R.string.ok, null);
                                dialogBuilder.show();
                            }
                            return false;
                        }
                    });
                /*} else {
                    PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("categoryPermissions");
                    preferenceCategory.removePreference(preference);
                }*/
            }
        }
        else {
            PreferenceScreen preferenceScreen = (PreferenceScreen) findPreference("rootScreen");
            PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("categoryPermissions");
            if (preferenceCategory != null)
                preferenceScreen.removePreference(preferenceCategory);
        }
        if (android.os.Build.VERSION.SDK_INT < 21) {
            PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("categoryNotifications");
            Preference preference = prefMng.findPreference(ApplicationPreferences.PREF_NOTIFICATION_HIDE_IN_LOCKSCREEN);
            if (preference != null)
                preferenceCategory.removePreference(preference);
        }
        if ((PPApplication.sLook == null) || (!PPApplication.sLookCocktailPanelEnabled)) {
            PreferenceScreen preferenceScreen = (PreferenceScreen) findPreference("rootScreen");
            PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("categorySamsungEdgePanel");
            if (preferenceCategory != null)
                preferenceScreen.removePreference(preferenceCategory);
        }
    }

    private static void setPreferenceTitleStyle(Preference preference, boolean bold, boolean underline, boolean errorColor)
    {
        if (preference != null) {
            CharSequence title = preference.getTitle();
            Spannable sbt = new SpannableString(title);
            Object spansToRemove[] = sbt.getSpans(0, title.length(), Object.class);
            for (Object span : spansToRemove) {
                if (span instanceof CharacterStyle)
                    sbt.removeSpan(span);
            }
            if (bold || underline) {
                if (bold)
                    sbt.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, sbt.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                if (underline)
                    sbt.setSpan(new UnderlineSpan(), 0, sbt.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                if (errorColor)
                    sbt.setSpan(new ForegroundColorSpan(Color.RED), 0, sbt.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                preference.setTitle(sbt);
            } else {
                preference.setTitle(sbt);
            }
        }
    }

    void setSummary(String key)
    {
        Preference preference = prefMng.findPreference(key);

        if (preference == null)
            return;

        PreferenceScreen preferenceCategoryNotifications = (PreferenceScreen) findPreference("categoryNotifications");
        boolean notificationStatusBar = preferences.getBoolean(ApplicationPreferences.PREF_NOTIFICATION_STATUS_BAR, true);
        boolean notificationStatusBarPermanent = preferences.getBoolean(ApplicationPreferences.PREF_NOTIFICATION_STATUS_BAR_PERMANENT, true);
        if (!(notificationStatusBar && notificationStatusBarPermanent)) {
            setPreferenceTitleStyle(preferenceCategoryNotifications, true, false, true);
            if (preferenceCategoryNotifications != null)
                preferenceCategoryNotifications.setSummary(getString(R.string.phone_profiles_pref_notificationStatusBarNotEnabled_summary) + " " +
                        getString(R.string.phone_profiles_pref_notificationStatusBarRequired));
        }
        else {
            setPreferenceTitleStyle(preferenceCategoryNotifications, false, false, false);
            if (preferenceCategoryNotifications != null)
                preferenceCategoryNotifications.setSummary(R.string.empty_string);
        }
        if (key.equals(ApplicationPreferences.PREF_NOTIFICATION_STATUS_BAR)) {
            setPreferenceTitleStyle(preference, !notificationStatusBar, false, !notificationStatusBar);
        }
        if (key.equals(ApplicationPreferences.PREF_NOTIFICATION_STATUS_BAR_PERMANENT)) {
            setPreferenceTitleStyle(preference, !notificationStatusBarPermanent, false, !notificationStatusBarPermanent);
        }

        if (android.os.Build.VERSION.SDK_INT >= 21) {
            if (key.equals(ApplicationPreferences.PREF_NOTIFICATION_SHOW_IN_STATUS_BAR)) {
                boolean show = preferences.getBoolean(key, true);
                Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_NOTIFICATION_HIDE_IN_LOCKSCREEN);
                _preference.setEnabled(show);
            }
        }

        // Do not bind toggles.
        if (preference instanceof CheckBoxPreference || preference instanceof TwoStatePreference) {
            return;
        }

        String stringValue = preferences.getString(key, "");

        if (key.equals(ApplicationPreferences.PREF_APPLICATION_BACKGROUND_PROFILE))
        {
            long lProfileId;
            try {
                lProfileId = Long.parseLong(stringValue);
            } catch (Exception e) {
                lProfileId = 0;
            }
            ProfilePreference profilePreference = (ProfilePreference)preference;
            profilePreference.setSummary(lProfileId);
        }
        else
        if (preference instanceof ListPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list.
            ListPreference listPreference = (ListPreference) preference;
            int index = listPreference.findIndexOfValue(stringValue);

            // Set the summary to reflect the new value.
            CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
            if (summary != null)
            {
                String sSummary = summary.toString();
                sSummary = sSummary.replace("%", "%%");
                preference.setSummary(sSummary);
            }
            else
                preference.setSummary(null);
        }
        /*else if (preference instanceof RingtonePreference) {
            // For ringtone preferences, look up the correct display value
            // using RingtoneManager.
            if (TextUtils.isEmpty(stringValue)) {
                // Empty values correspond to 'silent' (no ringtone).
                preference.setSummary(R.string.ringtone_silent);
            } else {
                Ringtone ringtone = RingtoneManager.getRingtone(
                        preference.getContext(), Uri.parse(stringValue));

                if (ringtone == null) {
                    // Clear the summary if there was a lookup error.
                    preference.setSummary(null);
                } else {
                    // Set the summary to reflect the new ringtone display
                    // name.
                    String name = ringtone
                            .getTitle(preference.getContext());
                    preference.setSummary(name);
                }
            }

        }*/
        else {
            // For all other preferences, set the summary to the value's
            // simple string representation.
            //preference.setSummary(preference.toString());
            preference.setSummary(stringValue);
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_FORCE_SET_MERGE_RINGER_NOTIFICATION_VOLUMES)) {
            Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_UNLINK_RINGER_NOTIFICATION_VOLUMES);
            if (_preference != null) {
                boolean enabled;
                String value = preferences.getString(key, "0");
                if (!value.equals("0"))
                    enabled = value.equals("1");
                else
                    enabled = ActivateProfileHelper.getMergedRingNotificationVolumes(getActivity().getApplicationContext());
                //Log.d("PhoneProfilesPreferencesNestedFragment.setSummary","enabled="+enabled);
                _preference.setEnabled(enabled);
            }
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_COLOR)) {
            Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS);
            boolean colorful = preferences.getString(key, "0").equals("1");
            _preference.setEnabled(colorful);
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_ICON_COLOR)) {
            Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_ICON_LIGHTNESS);
            boolean colorful = preferences.getString(key, "0").equals("1");
            _preference.setEnabled(colorful);
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_ICON_COLOR)) {
            Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_ICON_LIGHTNESS);
            if (_preference != null) {
                boolean colorful = preferences.getString(key, "0").equals("1");
                _preference.setEnabled(colorful);
            }
        }
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
            String key) {
        setSummary(key);
    }

    @Override
    public void onDestroy()
    {
        preferences.unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }

    public void doOnActivityResult(int requestCode, int resultCode, Intent data)
    {
        if ((requestCode == RESULT_APPLICATION_PERMISSIONS) ||
            (requestCode == RESULT_WRITE_SYSTEM_SETTINGS_PERMISSIONS) ||
            (requestCode == RESULT_ACCESS_NOTIFICATION_POLICY_PERMISSIONS) ||
            (requestCode == RESULT_DRAW_OVERLAYS_POLICY_PERMISSIONS)) {

            Context context = getActivity().getApplicationContext();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (requestCode == RESULT_WRITE_SYSTEM_SETTINGS_PERMISSIONS) {
                    if (Settings.System.canWrite(context))
                        Permissions.setShowRequestWriteSettingsPermission(context, true);
                }
                if (requestCode == RESULT_ACCESS_NOTIFICATION_POLICY_PERMISSIONS) {
                    NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    if (mNotificationManager.isNotificationPolicyAccessGranted())
                        Permissions.setShowRequestAccessNotificationPolicyPermission(context, true);
                }
                if (requestCode == RESULT_DRAW_OVERLAYS_POLICY_PERMISSIONS) {
                    if (Settings.canDrawOverlays(context))
                        Permissions.setShowRequestDrawOverlaysPermission(context, true);
                }
            }

            DataWrapper dataWrapper = new DataWrapper(context, true, false, 0);

            ActivateProfileHelper activateProfileHelper = dataWrapper.getActivateProfileHelper();
            activateProfileHelper.initialize(dataWrapper, context);

            Profile activatedProfile = dataWrapper.getActivatedProfile();
            dataWrapper.refreshProfileIcon(activatedProfile, false, 0);
            activateProfileHelper.showNotification(activatedProfile);
            activateProfileHelper.updateWidget(true);

            /*Intent intent5 = new Intent();
            intent5.setAction(RefreshGUIBroadcastReceiver.INTENT_REFRESH_GUI);
            intent5.putExtra(RefreshGUIBroadcastReceiver.EXTRA_REFRESH_ICONS, true);
            context.sendBroadcast(intent5);*/

            getActivity().setResult(RESULT_CANCELED);
            getActivity().finishAffinity();
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        doOnActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected String getSavedInstanceStateKeyName() {
        return "PhoneProfilesPreferencesFragment_PreferenceScreenKey";
    }

}
