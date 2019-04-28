package sk.henrichg.phoneprofiles;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceDialogFragmentCompat;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreferenceCompat;

public class ProfilesPrefsFragment extends PreferenceFragmentCompat
                                    implements SharedPreferences.OnSharedPreferenceChangeListener {

    private PreferenceManager prefMng;
    private SharedPreferences preferences;

    private boolean nestedFragment = false;

    private static final String PREF_NOTIFICATION_ACCESS = "prf_pref_volumeNotificationsAccessSettings";
    private static final int RESULT_NOTIFICATION_ACCESS_SETTINGS = 1980;

    private static final int RESULT_UNLINK_VOLUMES_APP_PREFERENCES = 1981;

    private static final String PREF_VOLUME_NOTIFICATION_VOLUME0 = "prf_pref_volumeNotificationVolume0";

    private static final String PRF_GRANT_PERMISSIONS = "prf_pref_grantPermissions";
    private static final String PREF_FORCE_STOP_APPLICATIONS_CATEGORY = "prf_pref_forceStopApplicationsCategory";
    static final String PREF_FORCE_STOP_APPLICATIONS_INSTALL_EXTENDER = "prf_pref_deviceForceStopApplicationInstallExtender";
    private static final String PREF_FORCE_STOP_APPLICATIONS_ACCESSIBILITY_SETTINGS = "prf_pref_deviceForceStopApplicationAccessibilitySettings";
    private static final int RESULT_ACCESSIBILITY_SETTINGS = 1983;
    private static final String PRF_GRANT_ROOT = "prf_pref_grantRoot";
    private static final String PREF_INSTALL_SILENT_TONE = "prf_pref_soundInstallSilentTone";
    private static final String PREF_LOCK_DEVICE_CATEGORY = "prf_pref_lockDeviceCategory";
    static final String PREF_LOCK_DEVICE_INSTALL_EXTENDER = "prf_pref_lockDeviceInstallExtender";
    private static final String PREF_LOCK_DEVICE_ACCESSIBILITY_SETTINGS = "prf_pref_lockDeviceAccessibilitySettings";

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        PPApplication.logE("ProfilesPrefsFragment.onCreate", "xxx");

        // is requred for to not call onCreate and onDestroy on orientation change
        setRetainInstance(true);

        nestedFragment = !(this instanceof ProfilesPrefsActivity.ProfilesPrefsRoot);
        PPApplication.logE("ProfilesPrefsFragment.onCreate", "nestedFragment="+nestedFragment);

        initPreferenceFragment(savedInstanceState);

        updateAllSummary();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        //initPreferenceFragment();
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference)
    {
        PPApplication.logE("ProfilesPrefsFragment.onDisplayPreferenceDialog", "xxx");

        PreferenceDialogFragmentCompat dialogFragment = null;

        if (preference instanceof DurationDialogPreferenceX)
        {
            ((DurationDialogPreferenceX)preference).fragment = new DurationDialogPreferenceFragmentX();
            dialogFragment = ((DurationDialogPreferenceX)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        if (preference instanceof RingtonePreferenceX)
        {
            ((RingtonePreferenceX)preference).fragment = new RingtonePreferenceFragmentX();
            dialogFragment = ((RingtonePreferenceX)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }

        if (preference instanceof InfoDialogPreferenceX)
        {
            ((InfoDialogPreferenceX)preference).fragment = new InfoDialogPreferenceFragmentX();
            dialogFragment = ((InfoDialogPreferenceX)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        if (preference instanceof ProfileIconPreferenceX)
        {
            ((ProfileIconPreferenceX)preference).fragment = new ProfileIconPreferenceFragmentX();
            dialogFragment = ((ProfileIconPreferenceX)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        if (preference instanceof VolumeDialogPreferenceX)
        {
            ((VolumeDialogPreferenceX)preference).fragment = new VolumeDialogPreferenceFragmentX();
            dialogFragment = ((VolumeDialogPreferenceX)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        if (preference instanceof NotificationVolume0DialogPreferenceX)
        {
            ((NotificationVolume0DialogPreferenceX)preference).fragment = new NotificationVolume0DialogPreferenceFragmentX();
            dialogFragment = ((NotificationVolume0DialogPreferenceX)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        if (preference instanceof ConnectToSSIDDialogPreferenceX)
        {
            ((ConnectToSSIDDialogPreferenceX)preference).fragment = new ConnectToSSIDDialogPreferenceFragmentX();
            dialogFragment = ((ConnectToSSIDDialogPreferenceX)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        if (preference instanceof BrightnessDialogPreferenceX)
        {
            ((BrightnessDialogPreferenceX)preference).fragment = new BrightnessDialogPreferenceFragmentX();
            dialogFragment = ((BrightnessDialogPreferenceX)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        if (preference instanceof ApplicationsDialogPreferenceX)
        {
            ((ApplicationsDialogPreferenceX)preference).fragment = new ApplicationsDialogPreferenceFragmentX();
            dialogFragment = ((ApplicationsDialogPreferenceX)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        if (preference instanceof ApplicationsMultiSelectDialogPreferenceX)
        {
            ((ApplicationsMultiSelectDialogPreferenceX)preference).fragment = new ApplicationsMultiSelectDialogPreferenceFragmentX();
            dialogFragment = ((ApplicationsMultiSelectDialogPreferenceX)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }

        if (dialogFragment != null)
        {
            FragmentManager fragmentManager = getFragmentManager();
            if (fragmentManager != null) {
                dialogFragment.setTargetFragment(this, 0);
                dialogFragment.show(fragmentManager, "sk.henrichg.phoneprofilesplus.ProfilesPrefsActivity.DIALOG");
            }
        }
        else
        {
            super.onDisplayPreferenceDialog(preference);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        PPApplication.logE("ProfilesPrefsFragment.onActivityCreated", "xxx");

        if (getActivity() == null)
            return;

        // must be used handler for rewrite toolbar title/subtitle
        final ProfilesPrefsFragment fragment = this;
        Handler handler = new Handler(getActivity().getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (getActivity() == null)
                    return;

                Toolbar toolbar = getActivity().findViewById(R.id.activity_preferences_toolbar);
                if (nestedFragment) {
                    toolbar.setTitle(fragment.getPreferenceScreen().getTitle());
                }
                else {
                    toolbar.setTitle(getString(R.string.title_activity_profile_preferences));
                }

            }
        }, 200);

        if (savedInstanceState != null) {
            //startupSource = savedInstanceState.getInt("startupSource", PPApplication.PREFERENCES_STARTUP_SOURCE_ACTIVITY);
        }
    }

    @Override
    public void onDestroy()
    {
        try {
            preferences.unregisterOnSharedPreferenceChangeListener(this);

            /*
            SharedPreferences.Editor editor = profilesPreferences.edit();
            updateSharedPreferences(editor, preferences);
            editor.apply();
            */

            PPApplication.logE("ProfilesPrefsFragment.onDestroy", "xxx");

        } catch (Exception ignored) {}

        super.onDestroy();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        PPApplication.logE("ProfilesPrefsFragment.onSharedPreferenceChanged", "xxx");

        String value;
        if (key.equals(Profile.PREF_PROFILE_NAME)) {
            value = sharedPreferences.getString(key, "");
            if (getActivity() != null) {
                // must be used handler for rewrite toolbar title/subtitle
                final ProfilesPrefsFragment fragment = this;
                final String _value = value;
                Handler handler = new Handler(getActivity().getMainLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (getActivity() == null)
                            return;

                        Toolbar toolbar = getActivity().findViewById(R.id.activity_preferences_toolbar);
                        toolbar.setSubtitle(getString(R.string.profile_string_0) + ": " + _value);
                    }
                }, 200);
            }
        }
        if (key.equals(Profile.PREF_PROFILE_ASK_FOR_DURATION) ||
                key.equals(Profile.PREF_PROFILE_DURATION_NOTIFICATION_VIBRATE) ||
                key.equals(Profile.PREF_PROFILE_HIDE_STATUS_BAR_ICON)) {
            boolean bValue = sharedPreferences.getBoolean(key, false);
            value = Boolean.toString(bValue);
        }
        else
            value = sharedPreferences.getString(key, "");
        setSummary(key, value);
        // disable depended preferences
        disableDependedPref(key, value);

        setPermissionsPreference();

        ProfilesPrefsActivity activity = (ProfilesPrefsActivity)getActivity();
        if (activity != null) {
            activity.showSaveMenu = true;
            activity.invalidateOptionsMenu();
        }
    }

    void doOnActivityResult(int requestCode, int resultCode/*, Intent data*/) {
        PPApplication.logE("ProfilesPrefsFragment.doOnActivityResult", "xxx");
        PPApplication.logE("ProfilesPrefsFragment.doOnActivityResult", "requestCode=" + requestCode);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        doOnActivityResult(requestCode, resultCode);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        //outState.putInt("startupSource", startupSource);
    }

    private void initPreferenceFragment(Bundle savedInstanceState) {
        prefMng = getPreferenceManager();

        preferences = prefMng.getSharedPreferences();

        /*
        PPApplication.logE("ProfilesPrefsFragment.initPreferenceFragment", "getContext()="+getContext());

        if (savedInstanceState == null) {
            if (getContext() != null) {
                profilesPreferences = getContext().getSharedPreferences(PREFS_NAME_ACTIVITY, Activity.MODE_PRIVATE);

                SharedPreferences.Editor editor = preferences.edit();
                updateSharedPreferences(editor, profilesPreferences);
                editor.apply();
            }
        }
        */

        preferences.registerOnSharedPreferenceChangeListener(this);
    }

    /*
    void updateSharedPreferences(SharedPreferences.Editor editor, SharedPreferences fromPreference) {
    }
    */

    private String getTitleWhenPreferenceChanged(String key, boolean systemSettings, Context context) {
        Preference preference = prefMng.findPreference(key);
        String title = "";
        if ((preference != null) && (preference.isEnabled())) {
            if (key.equals(Profile.PREF_PROFILE_ASK_FOR_DURATION) ||
                            key.equals(Profile.PREF_PROFILE_DURATION_NOTIFICATION_VIBRATE)) {
                /*boolean defaultValue =
                        getResources().getBoolean(
                                GlobalGUIRoutines.getResourceId(preference.getKey(), "bool", context));*/
                //noinspection ConstantConditions
                boolean defaultValue = Profile.defaultValuesBoolean.get(preference.getKey());
                if (preferences.getBoolean(key, defaultValue) != defaultValue)
                    title = preference.getTitle().toString();
            }
            else {
                /*String defaultValue =
                        getResources().getString(
                                GlobalGUIRoutines.getResourceId(preference.getKey(), "string", context));*/
                String defaultValue = Profile.defaultValuesString.get(preference.getKey());
                String value = preferences.getString(preference.getKey(), defaultValue);
                if (value != null) {
                    if (preference instanceof VolumeDialogPreferenceX) {
                        if (VolumeDialogPreference.changeEnabled(value))
                            title = preference.getTitle().toString();
                    } else if (preference instanceof BrightnessDialogPreferenceX) {
                        if (BrightnessDialogPreference.changeEnabled(value))
                            title = preference.getTitle().toString();
                    } else {
                        if (!value.equals(defaultValue)) {
                            if (key.equals(Profile.PREF_PROFILE_VOLUME_ZEN_MODE) &&
                                    (android.os.Build.VERSION.SDK_INT >= 23))
                                title = context.getString(R.string.profile_preferences_volumeZenModeM);
                            else
                                title = preference.getTitle().toString();
                        }
                    }
                }
            }
            if (systemSettings) {
                if (!title.isEmpty() && !title.contains("(S)"))
                    title = "(S) " + title;
            }
            return title;
        }
        else
            return title;
    }

    private void setCategorySummary(Preference preference, boolean bold, Context context) {
        String key = preference.getKey();
        boolean _bold = bold;
        Preference preferenceScreen = null;
        String summary = "";

        if (key.equals(Profile.PREF_PROFILE_DURATION) ||
                key.equals(Profile.PREF_PROFILE_AFTER_DURATION_DO) ||
                key.equals(Profile.PREF_PROFILE_ASK_FOR_DURATION) ||
                key.equals(Profile.PREF_PROFILE_DURATION_NOTIFICATION_SOUND) ||
                key.equals(Profile.PREF_PROFILE_DURATION_NOTIFICATION_VIBRATE)) {
            String title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DURATION, false, context);
            String afterDurationDoTitle = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_AFTER_DURATION_DO, false, context);
            if (!title.isEmpty()) {
                _bold = true;
                summary = summary + title + " • ";
                summary = summary + afterDurationDoTitle;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_ASK_FOR_DURATION, false, context);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            if (bold) {
                // any of duration preferences are set
                title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DURATION_NOTIFICATION_SOUND, false, context);
                if (!title.isEmpty()) {
                    if (!summary.isEmpty()) summary = summary + " • ";
                    summary = summary + title;
                }
                title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DURATION_NOTIFICATION_VIBRATE, false, context);
                if (!title.isEmpty()) {
                    if (!summary.isEmpty()) summary = summary + " • ";
                    summary = summary + title;
                }
            }
            preferenceScreen = prefMng.findPreference("prf_pref_activationDurationCategory");
        }

        if (key.equals(Profile.PREF_PROFILE_VOLUME_RINGER_MODE) ||
                key.equals(Profile.PREF_PROFILE_VOLUME_ZEN_MODE) ||
                key.equals(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING)) {
            String title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_VOLUME_RINGER_MODE, false, context);
            if (!title.isEmpty()) {
                _bold = true;
                summary = summary + title;
            }
            boolean a60 = (android.os.Build.VERSION.SDK_INT == 23) && Build.VERSION.RELEASE.equals("6.0");
            @SuppressLint("InlinedApi")
            boolean addS = !((android.os.Build.VERSION.SDK_INT >= 23) && (!a60) &&
                    GlobalGUIRoutines.activityActionExists(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS, context));
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_VOLUME_ZEN_MODE, addS, context);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING, false, context);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            preferenceScreen = prefMng.findPreference("prf_pref_soundProfileCategory");
        }

        if (key.equals(Profile.PREF_PROFILE_VOLUME_RINGTONE) ||
                key.equals(Profile.PREF_PROFILE_VOLUME_NOTIFICATION) ||
                key.equals(Profile.PREF_PROFILE_VOLUME_MEDIA) ||
                key.equals(Profile.PREF_PROFILE_VOLUME_ALARM) ||
                key.equals(Profile.PREF_PROFILE_VOLUME_SYSTEM) ||
                key.equals(Profile.PREF_PROFILE_VOLUME_VOICE) ||
                key.equals(Profile.PREF_PROFILE_VOLUME_SPEAKER_PHONE)) {
            String title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_VOLUME_RINGTONE, false, context);
            if (!title.isEmpty()) {
                _bold = true;
                summary = summary + title;
            }
            String ringtoneValue = preferences.getString(Profile.PREF_PROFILE_VOLUME_RINGTONE, "");
            if ((!ActivateProfileHelper.getMergedRingNotificationVolumes(context) || ApplicationPreferences.applicationUnlinkRingerNotificationVolumes(context)) &&
                    getEnableVolumeNotificationByRingtone(ringtoneValue)) {
                title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_VOLUME_NOTIFICATION, false, context);
                if (!title.isEmpty()) {
                    _bold = true;
                    if (!summary.isEmpty()) summary = summary + " • ";
                    summary = summary + title;
                }
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_VOLUME_MEDIA, false, context);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_VOLUME_ALARM, false, context);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_VOLUME_SYSTEM, false, context);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_VOLUME_VOICE, false, context);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_VOLUME_SPEAKER_PHONE, false, context);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            preferenceScreen = prefMng.findPreference("prf_pref_volumeCategory");
        }

        if (key.equals(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE) ||
                //key.equals(Profile.PREF_PROFILE_SOUND_RINGTONE) ||
                key.equals(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE) ||
                //key.equals(Profile.PREF_PROFILE_SOUND_NOTIFICATION) ||
                key.equals(Profile.PREF_PROFILE_SOUND_ALARM_CHANGE)) {
            //key.equals(Profile.PREF_PROFILE_SOUND_ALARM)) {
            String title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE, false, context);
            if (!title.isEmpty()) {
                _bold = true;
                summary = summary + title;
            }
            //_bold = _bold || isBold(Profile.PREF_PROFILE_SOUND_RINGTONE);
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE, false, context);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            //_bold = _bold || isBold(Profile.PREF_PROFILE_SOUND_NOTIFICATION);
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_SOUND_ALARM_CHANGE, false, context);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            //_bold = _bold || isBold(Profile.PREF_PROFILE_SOUND_ALARM);
            preferenceScreen = prefMng.findPreference("prf_pref_soundsCategory");
        }

        if (key.equals(Profile.PREF_PROFILE_VIBRATION_ON_TOUCH) ||
                key.equals(Profile.PREF_PROFILE_DTMF_TONE_WHEN_DIALING) ||
                key.equals(Profile.PREF_PROFILE_SOUND_ON_TOUCH)) {
            String title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_SOUND_ON_TOUCH, false, context);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_VIBRATION_ON_TOUCH, false, context);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DTMF_TONE_WHEN_DIALING, false, context);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            preferenceScreen = prefMng.findPreference("prf_pref_touchEffectsCategory");
        }

        if (key.equals(Profile.PREF_PROFILE_DEVICE_AIRPLANE_MODE) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_AUTOSYNC) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_WIFI) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_WIFI_AP) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_WIFI_AP_PREFS) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_BLUETOOTH) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_GPS) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_LOCATION_SERVICE_PREFS) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_NFC) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_PREFS) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_CONNECT_TO_SSID)) {
            String title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_AIRPLANE_MODE, false, context);
            if (!title.isEmpty()) {
                _bold = true;
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_AUTOSYNC, false, context);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE, false, context);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_PREFS, false, context);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA, false, context);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS, false, context);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_WIFI, false, context);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_CONNECT_TO_SSID, false, context);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_WIFI_AP, false, context);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_WIFI_AP_PREFS, false, context);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_BLUETOOTH, false, context);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_GPS, false, context);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_LOCATION_SERVICE_PREFS, false, context);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_NFC, false, context);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            preferenceScreen = prefMng.findPreference("prf_pref_radiosCategory");
        }

        if (key.equals(Profile.PREF_PROFILE_DEVICE_SCREEN_TIMEOUT) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_KEYGUARD) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_BRIGHTNESS) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_AUTOROTATE) ||
                key.equals(Profile.PREF_PROFILE_NOTIFICATION_LED) ||
                key.equals(Profile.PREF_PROFILE_HEADS_UP_NOTIFICATIONS) ||
                key.equals(Profile.PREF_PROFILE_SCREEN_NIGHT_MODE)) {
            String title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_SCREEN_TIMEOUT, false, context);
            if (!title.isEmpty()) {
                _bold = true;
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_KEYGUARD, false, context);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_BRIGHTNESS, false, context);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_AUTOROTATE, false, context);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_NOTIFICATION_LED, false, context);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_HEADS_UP_NOTIFICATIONS, false, context);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_SCREEN_NIGHT_MODE, false, context);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            preferenceScreen = prefMng.findPreference("prf_pref_screenCategory");
        }

        if (key.equals(Profile.PREF_PROFILE_DEVICE_POWER_SAVE_MODE) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_CLOSE_ALL_APPLICATIONS) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE) ||
                key.equals(Profile.PREF_PROFILE_LOCK_DEVICE)) {
            String title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_POWER_SAVE_MODE, false, context);
            if (!title.isEmpty()) {
                _bold = true;
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE, false, context);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_CLOSE_ALL_APPLICATIONS, false, context);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE, false, context);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE, false, context);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_LOCK_DEVICE, false, context);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            preferenceScreen = prefMng.findPreference("prf_pref_othersCategory");
        }

        if (preferenceScreen != null) {
            GlobalGUIRoutines.setPreferenceTitleStyleX(preferenceScreen, true, _bold, false, false, false);
            if (_bold)
                preferenceScreen.setSummary(summary);
            else
                preferenceScreen.setSummary("");
        }
    }

    private void setSummaryForNotificationVolume0(Context context) {
        Preference preference = prefMng.findPreference(PREF_VOLUME_NOTIFICATION_VOLUME0);
        if (preference != null) {
            String notificationToneChange = preferences.getString(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE, "0");
            String notificationTone = preferences.getString(Profile.PREF_PROFILE_SOUND_NOTIFICATION, "");
            String uriId = TonesHandler.getPhoneProfilesSilentUri(context, RingtoneManager.TYPE_NOTIFICATION);
            if (notificationToneChange.equals("1") && notificationTone.equals(uriId))
                preference.setSummary(R.string.profile_preferences_volumeNotificationVolume0_summaryConfigured);
            else
                preference.setSummary(R.string.profile_preferences_volumeNotificationVolume0_summaryConfigureForVolume0);
        }
    }

    private void setSummary(String key, Object value)
    {
        if (getActivity() == null)
            return;

        Context context = getActivity().getApplicationContext();

        if (key.equals(Profile.PREF_PROFILE_NAME))
        {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                preference.setSummary(value.toString());
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, !value.toString().isEmpty(), false, false, false);
                setCategorySummary(preference, !value.toString().isEmpty(), context);
            }
        }
        if (key.equals(Profile.PREF_PROFILE_VOLUME_RINGER_MODE))
        {
            String sValue = value.toString();
            ListPreference listPreference = prefMng.findPreference(key);
            if (listPreference != null) {
                int index = listPreference.findIndexOfValue(sValue);
                CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                listPreference.setSummary(summary);
                GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, index > 0, false, false, false);
                setCategorySummary(listPreference, index > 0, context);
            }
        }
        if (key.equals(Profile.PREF_PROFILE_VOLUME_ZEN_MODE))
        {
            //if (android.os.Build.VERSION.SDK_INT >= 21)
            //{
                /*final boolean canEnableZenMode =
                        (PPNotificationListenerService.isNotificationListenerServiceEnabled(context.getApplicationContext()) ||
                         (PPApplication.isRooted(false) && PPApplication.settingsBinaryExists())
                        );*/
            final boolean canEnableZenMode = ActivateProfileHelper.canChangeZenMode(context, false);

            if (!canEnableZenMode)
            {
                ListPreference listPreference = prefMng.findPreference(key);
                if (listPreference != null) {
                    listPreference.setEnabled(false);
                    listPreference.setSummary(getResources().getString(R.string.profile_preferences_device_not_allowed)+
                            ": "+getResources().getString(R.string.preference_not_allowed_reason_not_configured_in_system_settings));
                    boolean a60 = (android.os.Build.VERSION.SDK_INT == 23) && Build.VERSION.RELEASE.equals("6.0");
                    @SuppressLint("InlinedApi")
                    boolean addS = !((android.os.Build.VERSION.SDK_INT >= 23) && (!a60) &&
                            GlobalGUIRoutines.activityActionExists(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS, context));
                    GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, false, false, false, addS);
                    setCategorySummary(listPreference, false, context);
                }
            }
            else
            {
                String sValue = value.toString();
                ListPreference listPreference = prefMng.findPreference(key);
                if (listPreference != null) {
                    int iValue = Integer.parseInt(sValue);
                    int index = listPreference.findIndexOfValue(sValue);
                    CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                    if ((iValue != Profile.NO_CHANGE_VALUE) /*&& (iValue != Profile.SHARED_PROFILE_VALUE)*/) {
                        if (!((iValue == 6) && (android.os.Build.VERSION.SDK_INT < 23))) {
                            String[] summaryArray = getResources().getStringArray(R.array.zenModeSummaryArray);
                            summary = summary + " - " + summaryArray[iValue - 1];
                        }
                    }
                    listPreference.setSummary(summary);

                    final String sRingerMode = preferences.getString(Profile.PREF_PROFILE_VOLUME_RINGER_MODE, "");
                    int iRingerMode;
                    if (sRingerMode.isEmpty())
                        iRingerMode = 0;
                    else
                        iRingerMode = Integer.parseInt(sRingerMode);

                    if (iRingerMode == 5) {
                        boolean a60 = (android.os.Build.VERSION.SDK_INT == 23) && Build.VERSION.RELEASE.equals("6.0");
                        @SuppressLint("InlinedApi")
                        boolean addS = !((android.os.Build.VERSION.SDK_INT >= 23) && (!a60) &&
                                GlobalGUIRoutines.activityActionExists(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS, context));
                        GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, index > 0, false, false, addS);
                        setCategorySummary(listPreference, index > 0, context);
                    }
                    listPreference.setEnabled(iRingerMode == 5);
                }
            }
            //}
        }
        if (key.equals(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE) ||
                key.equals(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE) ||
                key.equals(Profile.PREF_PROFILE_SOUND_ALARM_CHANGE))
        {
            String sValue = value.toString();
            ListPreference listPreference = prefMng.findPreference(key);
            if (listPreference != null) {
                int index = listPreference.findIndexOfValue(sValue);
                CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                listPreference.setSummary(summary);
                GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, index > 0, false, false, false);
                setCategorySummary(listPreference, index > 0, context);
            }
            setSummaryForNotificationVolume0(context);
        }
        if (key.equals(Profile.PREF_PROFILE_SOUND_RINGTONE) ||
                key.equals(Profile.PREF_PROFILE_SOUND_NOTIFICATION) ||
                key.equals(Profile.PREF_PROFILE_SOUND_ALARM))
        {
            setSummaryForNotificationVolume0(context);
        }
        if (key.equals(Profile.PREF_PROFILE_DEVICE_AIRPLANE_MODE) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_AUTOSYNC) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_WIFI) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_BLUETOOTH) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_GPS) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_NFC) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_WIFI_AP) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_WIFI_AP_PREFS) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_POWER_SAVE_MODE) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_PREFS) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_CONNECT_TO_SSID))
        {
            if (key.equals(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA)) {
                // set mobile data preference title
                ListPreference mobileDataPreference = prefMng.findPreference(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA);
                if (mobileDataPreference != null) {
                    //if (android.os.Build.VERSION.SDK_INT >= 21) {
                    mobileDataPreference.setTitle(R.string.profile_preferences_deviceMobileData_21);
                    mobileDataPreference.setDialogTitle(R.string.profile_preferences_deviceMobileData_21);
                    /*}
                    else {
                        mobileDataPreference.setTitle(R.string.profile_preferences_deviceMobileData);
                        mobileDataPreference.setDialogTitle(R.string.profile_preferences_deviceMobileData);
                    }*/
                }
            }
            PreferenceAllowed preferenceAllowed = Profile.isProfilePreferenceAllowed(key, null, true, context);
            if (preferenceAllowed.allowed != PreferenceAllowed.PREFERENCE_ALLOWED)
            {
                Preference preference = prefMng.findPreference(key);
                if (preference != null) {
                    preference.setEnabled(false);
                    if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)
                        preference.setSummary(getResources().getString(R.string.profile_preferences_device_not_allowed)+
                                ": "+ preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                    GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, false, false, false, false);
                    setCategorySummary(preference, false, context);
                }
            }
            else
            if (key.equals(Profile.PREF_PROFILE_DEVICE_CONNECT_TO_SSID)) {
                Preference preference = prefMng.findPreference(key);
                if (preference != null) {
                    String sValue = value.toString();
                    boolean bold = !sValue.equals(Profile.CONNECTTOSSID_JUSTANY);
                    GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, bold, false, false, false);
                    setCategorySummary(preference, bold, context);
                }
            }
            else
            {
                String sValue = value.toString();
                ListPreference listPreference = prefMng.findPreference(key);
                if (listPreference != null) {
                    int index = listPreference.findIndexOfValue(sValue);
                    CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                    listPreference.setSummary(summary);
                    GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, index > 0, false, false, false);
                    setCategorySummary(listPreference, index > 0, context);
                }
            }

        }
        if (key.equals(Profile.PREF_PROFILE_DEVICE_KEYGUARD))
        {
            ListPreference listPreference = prefMng.findPreference(key);
            if (listPreference != null) {
                PreferenceAllowed preferenceAllowed = Profile.isProfilePreferenceAllowed(key, null, true, context);
                if (preferenceAllowed.allowed != PreferenceAllowed.PREFERENCE_ALLOWED) {
                    listPreference.setEnabled(false);
                    if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)
                        listPreference.setSummary(getResources().getString(R.string.profile_preferences_device_not_allowed)+
                                ": "+ preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                    GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, false, false, false, false);
                    setCategorySummary(listPreference, false, context);
                }
                else {
                    String sValue = value.toString();
                    int index = listPreference.findIndexOfValue(sValue);
                    CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                    listPreference.setSummary(summary);
                    GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, index > 0, false, false, false);
                    setCategorySummary(listPreference, index > 0, context);
                }
            }
        }
        if (key.equals(Profile.PREF_PROFILE_DEVICE_SCREEN_TIMEOUT))
        {
            String sValue = value.toString();
            ListPreference listPreference = prefMng.findPreference(key);
            if (listPreference != null) {
                int index = listPreference.findIndexOfValue(sValue);
                CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                listPreference.setSummary(summary);
                GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, index > 0, false, false, false);
                setCategorySummary(listPreference, index > 0, context);
            }
        }
        if (key.equals(Profile.PREF_PROFILE_DEVICE_AUTOROTATE))
        {
            String sValue = value.toString();
            ListPreference listPreference = prefMng.findPreference(key);
            if (listPreference != null) {
                int index = listPreference.findIndexOfValue(sValue);
                CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                listPreference.setSummary(summary);
                GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, index > 0, false, false, false);
                setCategorySummary(listPreference, index > 0, context);
            }
        }
        if (key.equals(Profile.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_CLOSE_ALL_APPLICATIONS) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_LOCATION_SERVICE_PREFS) ||
                key.equals(Profile.PREF_PROFILE_VOLUME_SPEAKER_PHONE) ||
                key.equals(Profile.PREF_PROFILE_VIBRATION_ON_TOUCH) ||
                key.equals(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_WALLPAPER_FOR) ||
                key.equals(Profile.PREF_PROFILE_LOCK_DEVICE) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_WIFI_AP_PREFS) ||
                key.equals(Profile.PREF_PROFILE_DTMF_TONE_WHEN_DIALING) ||
                key.equals(Profile.PREF_PROFILE_SOUND_ON_TOUCH))
        {
            PreferenceAllowed preferenceAllowed;
            if (key.equals(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING))
                preferenceAllowed = Profile.isProfilePreferenceAllowed(key, null, true, context);
            else {
                preferenceAllowed = new PreferenceAllowed();
                preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_ALLOWED;
            }
            if (preferenceAllowed.allowed != PreferenceAllowed.PREFERENCE_ALLOWED)
            {
                ListPreference listPreference = prefMng.findPreference(key);
                if (listPreference != null) {
                    listPreference.setEnabled(false);
                    if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)
                        listPreference.setSummary(getResources().getString(R.string.profile_preferences_device_not_allowed)+
                                ": "+ preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                    GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, false, false, false, false);
                    setCategorySummary(listPreference, false, context);
                }
            }
            else {
                String sValue = value.toString();
                ListPreference listPreference = prefMng.findPreference(key);
                if (listPreference != null) {
                    int index = listPreference.findIndexOfValue(sValue);
                    CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                    listPreference.setSummary(summary);
                    GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, index > 0, false, false, false);
                    setCategorySummary(listPreference, index > 0, context);
                }
            }
        }

        if (key.equals(Profile.PREF_PROFILE_NOTIFICATION_LED))
        {
            ListPreference listPreference = prefMng.findPreference(key);
            if (listPreference != null) {
                if (android.os.Build.VERSION.SDK_INT >= 23) {
                    listPreference.setTitle(R.string.profile_preferences_notificationLed_23);
                    listPreference.setDialogTitle(R.string.profile_preferences_notificationLed_23);
                } else {
                    listPreference.setTitle(R.string.profile_preferences_notificationLed);
                    listPreference.setDialogTitle(R.string.profile_preferences_notificationLed);
                }
                PreferenceAllowed preferenceAllowed = Profile.isProfilePreferenceAllowed(key, null, true, context);
                if (preferenceAllowed.allowed != PreferenceAllowed.PREFERENCE_ALLOWED) {
                    listPreference.setEnabled(false);
                    if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)
                        listPreference.setSummary(getResources().getString(R.string.profile_preferences_device_not_allowed)+
                                ": "+ preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                    GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, false, false, false, false);
                    setCategorySummary(listPreference, false, context);
                } else {
                    String sValue = value.toString();
                    int index = listPreference.findIndexOfValue(sValue);
                    CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                    listPreference.setSummary(summary);
                    GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, index > 0, false, false, false);
                    setCategorySummary(listPreference, index > 0, context);
                }
            }
        }
        if (key.equals(Profile.PREF_PROFILE_HEADS_UP_NOTIFICATIONS) ||
                key.equals(Profile.PREF_PROFILE_SCREEN_NIGHT_MODE))
        {
            ListPreference listPreference = prefMng.findPreference(key);
            if (listPreference != null) {
                PreferenceAllowed preferenceAllowed = Profile.isProfilePreferenceAllowed(key, null, true, context);
                if (preferenceAllowed.allowed != PreferenceAllowed.PREFERENCE_ALLOWED) {
                    listPreference.setEnabled(false);
                    if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)
                        listPreference.setSummary(getResources().getString(R.string.profile_preferences_device_not_allowed)+
                                ": "+ preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                    GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, false, false, false, false);
                    setCategorySummary(listPreference, false, context);
                } else {
                    String sValue = value.toString();
                    int index = listPreference.findIndexOfValue(sValue);
                    CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                    listPreference.setSummary(summary);
                    GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, index > 0, false, false, false);
                    setCategorySummary(listPreference, index > 0, context);
                }
            }
        }
        if (key.equals(Profile.PREF_PROFILE_DURATION))
        {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                String sValue = value.toString();
                int iValue = 0;
                if (!sValue.isEmpty())
                    iValue = Integer.valueOf(sValue);
                //preference.setSummary(sValue);
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, iValue > 0, false, false, false);
                setCategorySummary(preference, iValue > 0, context);
            }
        }
        if (key.equals(Profile.PREF_PROFILE_AFTER_DURATION_DO))
        {
            String sValue = value.toString();
            ListPreference listPreference = prefMng.findPreference(key);
            if (listPreference != null) {
                int index = listPreference.findIndexOfValue(sValue);
                CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                listPreference.setSummary(summary);
                /*String defaultValue =
                        getResources().getString(
                                GlobalGUIRoutines.getResourceId(key, "string", context));*/
                String defaultValue = Profile.defaultValuesString.get(key);
                GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, value != defaultValue, false, false, false);
                setCategorySummary(listPreference, /*index > 0*/false, context);
            }
        }
        if (key.equals(Profile.PREF_PROFILE_ASK_FOR_DURATION))
        {
            String sValue = value.toString();
            SwitchPreferenceCompat checkBoxPreference = prefMng.findPreference(key);
            if (checkBoxPreference != null) {
                boolean show = sValue.equals("true");
                GlobalGUIRoutines.setPreferenceTitleStyleX(checkBoxPreference, true, show, false, false, false);
                setCategorySummary(checkBoxPreference, show, context);
            }
        }
        if (key.equals(Profile.PREF_PROFILE_DURATION_NOTIFICATION_SOUND))
        {
            String sValue = value.toString();
            RingtonePreferenceX ringtonePreference = prefMng.findPreference(key);
            if (ringtonePreference != null) {
                boolean show = !sValue.isEmpty();
                GlobalGUIRoutines.setPreferenceTitleStyleX(ringtonePreference, true, show, false, false, false);
                setCategorySummary(ringtonePreference, show, context);
            }
        }
        if (key.equals(Profile.PREF_PROFILE_DURATION_NOTIFICATION_VIBRATE))
        {
            String sValue = value.toString();
            SwitchPreferenceCompat checkBoxPreference = prefMng.findPreference(key);
            if (checkBoxPreference != null) {
                boolean show = sValue.equals("true");
                GlobalGUIRoutines.setPreferenceTitleStyleX(checkBoxPreference, true, show, false, false, false);
                setCategorySummary(checkBoxPreference, show, context);
            }
        }
        if (key.equals(Profile.PREF_PROFILE_HIDE_STATUS_BAR_ICON) && (Build.VERSION.SDK_INT < 26))
        {
            String sValue = value.toString();
            SwitchPreferenceCompat checkBoxPreference = prefMng.findPreference(key);
            if (checkBoxPreference != null) {
                boolean show = sValue.equals("true");
                GlobalGUIRoutines.setPreferenceTitleStyleX(checkBoxPreference, true, show, false, false, false);
                setCategorySummary(checkBoxPreference, show, context);
            }
        }
        if (key.equals(Profile.PREF_PROFILE_VOLUME_RINGTONE) ||
                key.equals(Profile.PREF_PROFILE_VOLUME_NOTIFICATION) ||
                key.equals(Profile.PREF_PROFILE_VOLUME_MEDIA) ||
                key.equals(Profile.PREF_PROFILE_VOLUME_ALARM) ||
                key.equals(Profile.PREF_PROFILE_VOLUME_SYSTEM) ||
                key.equals(Profile.PREF_PROFILE_VOLUME_VOICE))
        {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                String sValue = value.toString();
                boolean change = VolumeDialogPreference.changeEnabled(sValue);
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, change, false, false, false);
                setCategorySummary(preference, change, context);
            }
        }
        if (key.equals(PREF_VOLUME_NOTIFICATION_VOLUME0)) {
            setSummaryForNotificationVolume0(context);
        }
        if (key.equals(Profile.PREF_PROFILE_DEVICE_BRIGHTNESS))
        {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                String sValue = value.toString();
                boolean change = BrightnessDialogPreference.changeEnabled(sValue);
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, change, false, false, false);
                setCategorySummary(preference, change, context);
            }
        }

        if (key.equals(PREF_FORCE_STOP_APPLICATIONS_INSTALL_EXTENDER)) {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                int extenderVersion = PPPExtenderBroadcastReceiver.isExtenderInstalled(context);
                if (extenderVersion == 0)
                    preference.setSummary(R.string.profile_preferences_deviceForceStopApplications_PPPExtender_install_summary);
                else
                if (extenderVersion < PPApplication.VERSION_CODE_EXTENDER_3_0)
                    preference.setSummary(R.string.event_preferences_applications_PPPExtender_new_version_summary);
                else
                    preference.setSummary(R.string.event_preferences_applications_PPPExtender_upgrade_summary);
            }
        }
        if (key.equals(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_PACKAGE_NAME)) {
            Preference preferenceScreen = prefMng.findPreference(PREF_FORCE_STOP_APPLICATIONS_CATEGORY);
            if (preferenceScreen != null) {
                int index = 0;
                String sValue = "0";
                CharSequence categorySummary = "";


                ListPreference listPreference =
                        prefMng.findPreference(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE);
                if (listPreference != null) {

                    boolean ok = true;
                    CharSequence changeSummary = "";
                    int extenderVersion = PPPExtenderBroadcastReceiver.isExtenderInstalled(context);
                    if (extenderVersion == 0) {
                        ok = false;
                        changeSummary = getResources().getString(R.string.profile_preferences_device_not_allowed) +
                                ": " + getString(R.string.preference_not_allowed_reason_not_extender_installed);
                        categorySummary = changeSummary;
                    }
                    else
                    if (extenderVersion < PPApplication.VERSION_CODE_EXTENDER_3_0) {
                        ok = false;
                        changeSummary = getResources().getString(R.string.profile_preferences_device_not_allowed) +
                                ": " + getString(R.string.preference_not_allowed_reason_extender_not_upgraded);
                        categorySummary = changeSummary;
                    }
                    else
                    if (!PPPExtenderBroadcastReceiver.isAccessibilityServiceEnabled(context)) {
                        ok = false;
                        changeSummary = getResources().getString(R.string.profile_preferences_device_not_allowed)+
                                ": "+getString(R.string.preference_not_allowed_reason_not_enabled_accessibility_settings_for_extender);
                        categorySummary = changeSummary;
                    }
                    if (!ok) {
                        listPreference.setSummary(changeSummary);
                        GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, false, false, false, false);
                        setCategorySummary(listPreference, false, context);
                    }
                    else {
                        sValue = listPreference.getValue();
                        index = listPreference.findIndexOfValue(sValue);
                        changeSummary = (index >= 0) ? listPreference.getEntries()[index] : null;
                        categorySummary = changeSummary;
                        listPreference.setSummary(changeSummary);
                        GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, index > 0, false, false, false);
                        setCategorySummary(listPreference, index > 0, context);
                    }
                }

                if (sValue.equals("1")) {
                    ApplicationsMultiSelectDialogPreferenceX appMultiSelectPreference =
                            prefMng.findPreference(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_PACKAGE_NAME);
                    if (appMultiSelectPreference != null)
                        categorySummary = categorySummary + " • " + appMultiSelectPreference.getSummaryAMSDP();
                }
                preferenceScreen.setSummary(categorySummary);
                GlobalGUIRoutines.setPreferenceTitleStyleX(preferenceScreen, true, (index > 0), false, false, false);
            }
        }

        if (key.equals(PREF_LOCK_DEVICE_INSTALL_EXTENDER)) {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                int extenderVersion = PPPExtenderBroadcastReceiver.isExtenderInstalled(context);
                if (extenderVersion == 0)
                    preference.setSummary(R.string.profile_preferences_lockDevice_PPPExtender_install_summary);
                else
                if (extenderVersion < PPApplication.VERSION_CODE_EXTENDER_4_0)
                    preference.setSummary(R.string.event_preferences_applications_PPPExtender_new_version_summary);
                else
                    preference.setSummary(R.string.event_preferences_applications_PPPExtender_upgrade_summary);
            }
        }
        if (key.equals(Profile.PREF_PROFILE_LOCK_DEVICE)) {
            Preference preferenceScreen = prefMng.findPreference(PREF_LOCK_DEVICE_CATEGORY);
            if (preferenceScreen != null) {
                int index = 0;
                String sValue;// = "0";
                CharSequence categorySummary = "";

                ListPreference listPreference =
                        prefMng.findPreference(Profile.PREF_PROFILE_LOCK_DEVICE);
                if (listPreference != null) {
                    sValue = listPreference.getValue();
                    //boolean ok = true;
                    CharSequence changeSummary;// = "";

                    index = listPreference.findIndexOfValue(sValue);
                    changeSummary = (index >= 0) ? listPreference.getEntries()[index] : null;

                    if (sValue.equals("3")) {
                        int extenderVersion = PPPExtenderBroadcastReceiver.isExtenderInstalled(context);
                        if (extenderVersion == 0) {
                            //ok = false;
                            changeSummary = changeSummary + "\n\n" +
                                    getResources().getString(R.string.profile_preferences_device_not_allowed) +
                                    ": " + getString(R.string.preference_not_allowed_reason_not_extender_installed);
                            categorySummary = changeSummary;
                        } else if (extenderVersion < PPApplication.VERSION_CODE_EXTENDER_4_0) {
                            //ok = false;
                            changeSummary = changeSummary + "\n\n" +
                                    getResources().getString(R.string.profile_preferences_device_not_allowed) +
                                    ": " + getString(R.string.preference_not_allowed_reason_extender_not_upgraded);
                            categorySummary = changeSummary;
                        } else if (!PPPExtenderBroadcastReceiver.isAccessibilityServiceEnabled(context)) {
                            //ok = false;
                            changeSummary = changeSummary + "\n\n" +
                                    getResources().getString(R.string.profile_preferences_device_not_allowed) +
                                    ": " + getString(R.string.preference_not_allowed_reason_not_enabled_accessibility_settings_for_extender);
                            categorySummary = changeSummary;
                        }
                        else
                            categorySummary = changeSummary;
                    }
                    else
                        categorySummary = changeSummary;

                    listPreference.setSummary(changeSummary);
                    GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, index > 0, false, false, false);
                    setCategorySummary(listPreference, index > 0, context);

                }
                preferenceScreen.setSummary(categorySummary);
                GlobalGUIRoutines.setPreferenceTitleStyleX(preferenceScreen, true, (index > 0), false, false, false);
            }
        }

    }

    void setSummary(String key) {
        String value;
        if (key.equals(Profile.PREF_PROFILE_ASK_FOR_DURATION) ||
                key.equals(Profile.PREF_PROFILE_DURATION_NOTIFICATION_VIBRATE) ||
                key.equals(Profile.PREF_PROFILE_HIDE_STATUS_BAR_ICON)) {
            boolean b = preferences.getBoolean(key, false);
            value = Boolean.toString(b);
        }
        else
            value = preferences.getString(key, "");
        setSummary(key, value);
    }

    private void updateAllSummary() {
        if (getActivity() == null)
            return;

        // disable depended preferences
        disableDependedPref(Profile.PREF_PROFILE_VOLUME_RINGTONE);
        disableDependedPref(Profile.PREF_PROFILE_VOLUME_NOTIFICATION);
        disableDependedPref(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE);
        disableDependedPref(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE);
        disableDependedPref(Profile.PREF_PROFILE_SOUND_ALARM_CHANGE);
        disableDependedPref(Profile.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE);
        disableDependedPref(Profile.PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE);
        disableDependedPref(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE);
        disableDependedPref(Profile.PREF_PROFILE_DEVICE_WIFI_AP);
        disableDependedPref(Profile.PREF_PROFILE_VOLUME_RINGER_MODE);
        disableDependedPref(Profile.PREF_PROFILE_VOLUME_ZEN_MODE);

        setSummary(Profile.PREF_PROFILE_VOLUME_UNLINK_VOLUMES_APP_SETTINGS);

        //if (startupSource != PPApplication.PREFERENCES_STARTUP_SOURCE_SHARED_PROFILE)
        //{
        setSummary(Profile.PREF_PROFILE_NAME);
        setSummary(Profile.PREF_PROFILE_DURATION);
        setSummary(Profile.PREF_PROFILE_AFTER_DURATION_DO);
        setSummary(Profile.PREF_PROFILE_ASK_FOR_DURATION);
        setSummary(Profile.PREF_PROFILE_DURATION_NOTIFICATION_SOUND);
        setSummary(Profile.PREF_PROFILE_DURATION_NOTIFICATION_VIBRATE);
        setSummary(Profile.PREF_PROFILE_HIDE_STATUS_BAR_ICON);
        //}
        setSummary(Profile.PREF_PROFILE_VOLUME_RINGER_MODE);
        setSummary(Profile.PREF_PROFILE_VOLUME_ZEN_MODE);
        setSummary(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE);
        setSummary(Profile.PREF_PROFILE_SOUND_RINGTONE);
        setSummary(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE);
        setSummary(Profile.PREF_PROFILE_SOUND_NOTIFICATION);
        setSummary(Profile.PREF_PROFILE_SOUND_ALARM_CHANGE);
        setSummary(Profile.PREF_PROFILE_SOUND_ALARM);
        setSummary(Profile.PREF_PROFILE_DEVICE_AIRPLANE_MODE);
        setSummary(Profile.PREF_PROFILE_DEVICE_WIFI);
        setSummary(Profile.PREF_PROFILE_DEVICE_BLUETOOTH);
        setSummary(Profile.PREF_PROFILE_DEVICE_SCREEN_TIMEOUT);
        setSummary(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA);
        setSummary(Profile.PREF_PROFILE_DEVICE_GPS);
        setSummary(Profile.PREF_PROFILE_DEVICE_AUTOSYNC);
        setSummary(Profile.PREF_PROFILE_DEVICE_AUTOROTATE);
        setSummary(Profile.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE);
        setSummary(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS);
        setSummary(Profile.PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE);
        setSummary(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE);
        setSummary(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_PACKAGE_NAME);
        setSummary(ProfilePreferencesNestedFragment.PREF_FORCE_STOP_APPLICATIONS_INSTALL_EXTENDER);
        setSummary(Profile.PREF_PROFILE_DEVICE_LOCATION_SERVICE_PREFS);
        setSummary(Profile.PREF_PROFILE_VOLUME_SPEAKER_PHONE);
        setSummary(Profile.PREF_PROFILE_DEVICE_NFC);
        setSummary(Profile.PREF_PROFILE_DEVICE_KEYGUARD);
        setSummary(Profile.PREF_PROFILE_VOLUME_RINGTONE);
        setSummary(Profile.PREF_PROFILE_VOLUME_NOTIFICATION);
        setSummary(Profile.PREF_PROFILE_VOLUME_MEDIA);
        setSummary(Profile.PREF_PROFILE_VOLUME_ALARM);
        setSummary(Profile.PREF_PROFILE_VOLUME_SYSTEM);
        setSummary(Profile.PREF_PROFILE_VOLUME_VOICE);
        setSummary(Profile.PREF_PROFILE_DEVICE_BRIGHTNESS);
        setSummary(Profile.PREF_PROFILE_VIBRATION_ON_TOUCH);
        setSummary(Profile.PREF_PROFILE_DEVICE_WIFI_AP);
        setSummary(Profile.PREF_PROFILE_DEVICE_POWER_SAVE_MODE);
        setSummary(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE);
        setSummary(Profile.PREF_PROFILE_NOTIFICATION_LED);
        setSummary(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING);
        setSummary(Profile.PREF_PROFILE_DEVICE_WALLPAPER_FOR);
        setSummary(Profile.PREF_PROFILE_LOCK_DEVICE);
        setSummary(Profile.PREF_PROFILE_DEVICE_CONNECT_TO_SSID);
        setSummary(Profile.PREF_PROFILE_DEVICE_WIFI_AP_PREFS);
        setSummary(Profile.PREF_PROFILE_HEADS_UP_NOTIFICATIONS);
        setSummary(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_PREFS);
        setSummary(Profile.PREF_PROFILE_DEVICE_CLOSE_ALL_APPLICATIONS);
        setSummary(Profile.PREF_PROFILE_SCREEN_NIGHT_MODE);
        setSummary(Profile.PREF_PROFILE_DTMF_TONE_WHEN_DIALING);
        setSummary(Profile.PREF_PROFILE_SOUND_ON_TOUCH);
        setSummary(ProfilePreferencesNestedFragment.PREF_LOCK_DEVICE_INSTALL_EXTENDER);
    }

    private boolean getEnableVolumeNotificationByRingtone(String ringtoneValue) {
        boolean enabled = Profile.getVolumeRingtoneChange(ringtoneValue);
        if (enabled) {
            int volume = Profile.getVolumeRingtoneValue(ringtoneValue);
            return volume > 0;
        }
        else
            return true;
    }

    private boolean getEnableVolumeNotificationVolume0(boolean notificationEnabled, String notificationValue, Context context) {
        return  notificationEnabled && ActivateProfileHelper.getMergedRingNotificationVolumes(context) &&
                ApplicationPreferences.applicationUnlinkRingerNotificationVolumes(context) &&
                Profile.getVolumeRingtoneChange(notificationValue) && (Profile.getVolumeRingtoneValue(notificationValue) == 0);
    }

    private void disableDependedPref(String key, Object value)
    {
        if (getActivity() == null)
            return;

        Context context = getActivity().getApplicationContext();

        String sValue = value.toString();

        final String ON = "1";

        if (key.equals(Profile.PREF_PROFILE_VOLUME_RINGTONE)) {
            boolean enabled = getEnableVolumeNotificationByRingtone(sValue);
            Preference preference = prefMng.findPreference(Profile.PREF_PROFILE_VOLUME_NOTIFICATION);
            if (preference != null)
                preference.setEnabled(enabled);
            String notificationValue = preferences.getString(Profile.PREF_PROFILE_VOLUME_NOTIFICATION, "");
            enabled = getEnableVolumeNotificationVolume0(enabled, notificationValue, context);
            preference = prefMng.findPreference(PREF_VOLUME_NOTIFICATION_VOLUME0);
            if (preference != null)
                preference.setEnabled(enabled);
        }
        if (key.equals(Profile.PREF_PROFILE_VOLUME_NOTIFICATION)) {
            String ringtoneValue = preferences.getString(Profile.PREF_PROFILE_VOLUME_RINGTONE, "");
            boolean enabled = (!ActivateProfileHelper.getMergedRingNotificationVolumes(context) || ApplicationPreferences.applicationUnlinkRingerNotificationVolumes(context)) &&
                    getEnableVolumeNotificationByRingtone(ringtoneValue);
            Preference preference = prefMng.findPreference(Profile.PREF_PROFILE_VOLUME_NOTIFICATION);
            if (preference != null)
                preference.setEnabled(enabled);
            enabled = getEnableVolumeNotificationVolume0(enabled, sValue, context);
            preference = prefMng.findPreference(PREF_VOLUME_NOTIFICATION_VOLUME0);
            if (preference != null)
                preference.setEnabled(enabled);
        }
        if (key.equals(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE))
        {
            boolean enabled = !(/*sValue.equals(Profile.SHARED_PROFILE_VALUE_STR) ||*/ sValue.equals(Profile.NO_CHANGE_VALUE_STR));
            Preference preference = prefMng.findPreference(Profile.PREF_PROFILE_SOUND_RINGTONE);
            if (preference != null)
                preference.setEnabled(enabled);
        }
        if (key.equals(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE))
        {
            boolean enabled = !(/*sValue.equals(Profile.SHARED_PROFILE_VALUE_STR) ||*/ sValue.equals(Profile.NO_CHANGE_VALUE_STR));
            Preference preference = prefMng.findPreference(Profile.PREF_PROFILE_SOUND_NOTIFICATION);
            if (preference != null)
                preference.setEnabled(enabled);
        }
        if (key.equals(Profile.PREF_PROFILE_SOUND_ALARM_CHANGE))
        {
            boolean enabled = !(/*sValue.equals(Profile.SHARED_PROFILE_VALUE_STR) ||*/ sValue.equals(Profile.NO_CHANGE_VALUE_STR));
            Preference preference = prefMng.findPreference(Profile.PREF_PROFILE_SOUND_ALARM);
            if (preference != null)
                preference.setEnabled(enabled);
        }
        if (key.equals(Profile.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE))
        {
            boolean enabled = !(/*sValue.equals(Profile.SHARED_PROFILE_VALUE_STR) ||*/ sValue.equals(Profile.NO_CHANGE_VALUE_STR));
            Preference preference = prefMng.findPreference(Profile.PREF_PROFILE_DEVICE_WALLPAPER);
            if (preference != null)
                preference.setEnabled(enabled);
            preference = prefMng.findPreference(Profile.PREF_PROFILE_DEVICE_WALLPAPER_FOR);
            if (preference != null)
                preference.setEnabled(enabled);
        }
        if (key.equals(Profile.PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE))
        {
            boolean enabled = !(/*sValue.equals(Profile.SHARED_PROFILE_VALUE_STR) ||*/ sValue.equals(Profile.NO_CHANGE_VALUE_STR));
            Preference preference = prefMng.findPreference(Profile.PREF_PROFILE_DEVICE_RUN_APPLICATION_PACKAGE_NAME);
            if (preference != null)
                preference.setEnabled(enabled);
        }
        if (key.equals(Profile.PREF_PROFILE_DEVICE_WIFI_AP))
        {
            if (Profile.isProfilePreferenceAllowed(key, null, true, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                boolean enabled = !sValue.equals(ON);
                ListPreference preference = prefMng.findPreference(Profile.PREF_PROFILE_DEVICE_WIFI);
                if (preference != null) {
                    if (!enabled)
                        preference.setValue(Profile.NO_CHANGE_VALUE_STR);
                    preference.setEnabled(enabled);
                }
            }
        }
        if (key.equals(Profile.PREF_PROFILE_VOLUME_RINGER_MODE) ||
                key.equals(Profile.PREF_PROFILE_VOLUME_ZEN_MODE)) {
            //if (android.os.Build.VERSION.SDK_INT >= 21) {
            String ringerMode = preferences.getString(Profile.PREF_PROFILE_VOLUME_RINGER_MODE, "0");
            String zenMode = preferences.getString(Profile.PREF_PROFILE_VOLUME_ZEN_MODE, "0");
            boolean enabled = false;
            // also look at Profile.mergeProfiles()
            if ((Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING, null, true, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) &&
                    ringerMode.equals("5")) {
                if (zenMode.equals("1") || zenMode.equals("2"))
                    enabled = true;
            }
            ListPreference preference = prefMng.findPreference(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING);
            if (preference != null) {
                if (!enabled)
                    preference.setValue(Profile.NO_CHANGE_VALUE_STR);
                preference.setEnabled(enabled);
            }
            //}
        }

        if (key.equals(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE)) {
            setSummary(PREF_FORCE_STOP_APPLICATIONS_INSTALL_EXTENDER);
            boolean ok = PPPExtenderBroadcastReceiver.isEnabled(context, PPApplication.VERSION_CODE_EXTENDER_3_0);
            Preference preference = prefMng.findPreference(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE);
            if (preference != null) {
                preference.setEnabled(ok);
                setSummary(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE);
            }
            ApplicationsMultiSelectDialogPreferenceX appPreference =
                    prefMng.findPreference(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_PACKAGE_NAME);
            if (appPreference != null) {
                appPreference.setEnabled(ok && (!(/*sValue.equals(Profile.SHARED_PROFILE_VALUE_STR) ||*/ sValue.equals(Profile.NO_CHANGE_VALUE_STR))));
                appPreference.setSummaryAMSDP();
            }
        }

        if (key.equals(Profile.PREF_PROFILE_LOCK_DEVICE)) {
            setSummary(PREF_LOCK_DEVICE_INSTALL_EXTENDER);
            Preference preference = prefMng.findPreference(Profile.PREF_PROFILE_LOCK_DEVICE);
            if (preference != null) {
                setSummary(Profile.PREF_PROFILE_LOCK_DEVICE);
            }
        }
    }

    void disableDependedPref(String key) {
        String value;
        /*if (key.equals(Profile.PREF_PROFILE_SHOW_IN_ACTIVATOR)) {
            boolean b = preferences.getBoolean(key, false);
            value = Boolean.toString(b);
        }
        else*/
            value = preferences.getString(key, "");
        disableDependedPref(key, value);
    }

    void setPermissionsPreference() {
        Bundle bundle = this.getArguments();

        if (bundle == null)
            return;

        if (nestedFragment)
            return;

        if (getActivity() == null)
            return;

        Context context = getActivity().getApplicationContext();

        //Log.e("***** ProfilePreferencesNestedFragment.setPermissionPreference","xxx");

        long profile_id = bundle.getLong(PPApplication.EXTRA_PROFILE_ID, 0);
        if (profile_id != 0) {
            int newProfileMode = bundle.getInt(EditorProfilesActivity.EXTRA_NEW_PROFILE_MODE, EditorProfileListFragment.EDIT_MODE_UNDEFINED);
            int predefinedProfileIndex = bundle.getInt(EditorProfilesActivity.EXTRA_PREDEFINED_PROFILE_INDEX, 0);
            final Profile profile = ((ProfilesPrefsActivity) getActivity())
                    .getProfileFromPreferences(profile_id, newProfileMode, predefinedProfileIndex);

            // not some permissions
            if (Permissions.checkProfilePermissions(context, profile).size() == 0) {
                Preference preference = prefMng.findPreference(PRF_GRANT_PERMISSIONS);
                if (preference != null) {
                    PreferenceScreen preferenceCategory = prefMng.findPreference("rootScreen");
                    if (preferenceCategory != null)
                        preferenceCategory.removePreference(preference);
                }
            }
            else {
                Preference preference = prefMng.findPreference(PRF_GRANT_PERMISSIONS);
                if (preference == null) {
                    PreferenceScreen preferenceCategory = findPreference("rootScreen");
                    if (preferenceCategory != null) {
                        preference = new Preference(context);
                        preference.setKey(PRF_GRANT_PERMISSIONS);
                        preference.setWidgetLayoutResource(R.layout.start_activity_preference);
                        preference.setLayoutResource(R.layout.mp_preference_material_widget);
                        preference.setOrder(-100);
                        preferenceCategory.addPreference(preference);
                    }
                }
                if (preference != null) {
                    Spannable title = new SpannableString(getString(R.string.preferences_grantPermissions_title));
                    title.setSpan(new ForegroundColorSpan(Color.RED), 0, title.length(), 0);
                    preference.setTitle(title);
                    Spannable summary = new SpannableString(getString(R.string.preferences_grantPermissions_summary));
                    summary.setSpan(new ForegroundColorSpan(Color.RED), 0, summary.length(), 0);
                    preference.setSummary(summary);

                    final Activity activity = getActivity();
                    preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            //Profile mappedProfile = Profile.getMappedProfile(profile, appContext);
                            Permissions.grantProfilePermissions(activity, profile, false,
                                    PPApplication.STARTUP_SOURCE_EDITOR, false, false, true);
                            return false;
                        }
                    });
                }
            }

            // not enabled grant root
            if (Profile.isProfilePreferenceAllowed("-", profile, true, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                Preference preference = prefMng.findPreference(PRF_GRANT_ROOT);
                if (preference != null) {
                    PreferenceScreen preferenceCategory = findPreference("rootScreen");
                    if (preferenceCategory != null)
                        preferenceCategory.removePreference(preference);
                }
            }
            else {
                Preference preference = prefMng.findPreference(PRF_GRANT_ROOT);
                if (preference == null) {
                    PreferenceScreen preferenceCategory = findPreference("rootScreen");
                    if (preferenceCategory != null) {
                        preference = new Preference(context);
                        preference.setKey(PRF_GRANT_ROOT);
                        preference.setWidgetLayoutResource(R.layout.start_activity_preference);
                        preference.setLayoutResource(R.layout.mp_preference_material_widget);
                        preference.setOrder(-100);
                        preferenceCategory.addPreference(preference);
                    }
                }
                if (preference != null) {
                    Spannable title = new SpannableString(getString(R.string.preferences_grantRoot_title));
                    title.setSpan(new ForegroundColorSpan(Color.RED), 0, title.length(), 0);
                    preference.setTitle(title);
                    Spannable summary = new SpannableString(getString(R.string.preferences_grantRoot_summary));
                    summary.setSpan(new ForegroundColorSpan(Color.RED), 0, summary.length(), 0);
                    preference.setSummary(summary);

                    final Activity activity = getActivity();
                    final ProfilesPrefsFragment fragment = this;
                    preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            Permissions.grantRootX(fragment, activity);
                            return false;
                        }
                    });
                }
            }
        }
        else {
            Preference preference = prefMng.findPreference(PRF_GRANT_PERMISSIONS);
            if (preference != null) {
                PreferenceScreen preferenceCategory = findPreference("rootScreen");
                if (preferenceCategory != null)
                    preferenceCategory.removePreference(preference);
            }
            preference = prefMng.findPreference(PRF_GRANT_ROOT);
            if (preference != null) {
                PreferenceScreen preferenceCategory = findPreference("rootScreen");
                if (preferenceCategory != null)
                    preferenceCategory.removePreference(preference);
            }
        }
    }

}
