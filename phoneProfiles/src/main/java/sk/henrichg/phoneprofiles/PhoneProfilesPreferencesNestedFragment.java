package sk.henrichg.phoneprofiles;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
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
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;

import com.thelittlefireman.appkillermanager.managers.KillerManager;

public class PhoneProfilesPreferencesNestedFragment extends PreferenceFragment
                                              implements SharedPreferences.OnSharedPreferenceChangeListener
{

    PreferenceManager prefMng;
    private SharedPreferences preferences;

    static final String PREF_APPLICATION_PERMISSIONS = "permissionsApplicationPermissions";
    private static final int RESULT_APPLICATION_PERMISSIONS = 1990;
    static final String PREF_WRITE_SYSTEM_SETTINGS_PERMISSIONS = "permissionsWriteSystemSettingsPermissions";
    private static final int RESULT_WRITE_SYSTEM_SETTINGS_PERMISSIONS = 1991;
    private static final String PREF_APPLICATION_LANGUAGE_24 = "applicationLanguage24";
    //static final int RESULT_LOCALE_SETTINGS = 1992;
    static final String PREF_ACCESS_NOTIFICATION_POLICY_PERMISSIONS = "permissionsAccessNotificationPolicyPermissions";
    private static final int RESULT_ACCESS_NOTIFICATION_POLICY_PERMISSIONS = 1993;
    static final String PREF_DRAW_OVERLAYS_PERMISSIONS = "permissionsDrawOverlaysPermissions";
    private static final int RESULT_DRAW_OVERLAYS_POLICY_PERMISSIONS = 1998;
    static final String PREF_GRANT_ROOT_PERMISSION = "permissionsGrantRootPermission";

    static final String PREF_AUTOSTART_MANAGER = "applicationAutoStartManager";
    static final String PREF_NOTIFICATION_SYSTEM_SETTINGS = "notificationSystemSettings";
    static final String PREF_APPLICATION_POWER_MANAGER = "applicationPowerManager";
    static final String PREF_BATTERY_OPTIMIZATION_SYSTEM_SETTINGS = "applicationBatteryOptimization";

    @Override
    public int addPreferencesFromResource() {
        return -1;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // must by false to avoid FC when rotation changes and preference dialogs are shown
        setRetainInstance(false);

        prefMng = getPreferenceManager();
        preferences = prefMng.getSharedPreferences();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Toolbar toolbar = getActivity().findViewById(R.id.mp_toolbar);
        Bundle bundle = getArguments();
        if (bundle.getBoolean(PreferenceFragment.EXTRA_NESTED, false))
            toolbar.setSubtitle(getString(R.string.title_activity_phone_profiles_preferences));
        else
            toolbar.setSubtitle(null);

        prefMng = getPreferenceManager();
        prefMng.setSharedPreferencesName(PPApplication.APPLICATION_PREFS_NAME);
        prefMng.setSharedPreferencesMode(Activity.MODE_PRIVATE);

        preferences = prefMng.getSharedPreferences();
        preferences.registerOnSharedPreferenceChangeListener(this);

        PreferenceScreen preferenceCategoryScreen;
        preferenceCategoryScreen = (PreferenceScreen)prefMng.findPreference("applicationInterfaceCategory");
        if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen, "");
        preferenceCategoryScreen = (PreferenceScreen)prefMng.findPreference("categoryApplicationStart");
        if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen, "");
        preferenceCategoryScreen = (PreferenceScreen)prefMng.findPreference("categorySystem");
        if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen, "");
        preferenceCategoryScreen = (PreferenceScreen)prefMng.findPreference("categoryPermissions");
        if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen, "");
        preferenceCategoryScreen = (PreferenceScreen)prefMng.findPreference("categoryNotifications");
        if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen, "");
        preferenceCategoryScreen = (PreferenceScreen)prefMng.findPreference("profileActivationCategory");
        if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen, "");
        preferenceCategoryScreen = (PreferenceScreen)prefMng.findPreference("categoryActivator");
        if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen, "");
        preferenceCategoryScreen = (PreferenceScreen)prefMng.findPreference("categoryEditor");
        if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen, "");
        preferenceCategoryScreen = (PreferenceScreen)prefMng.findPreference("categoryWidgetList");
        if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen, "");
        preferenceCategoryScreen = (PreferenceScreen)prefMng.findPreference("categoryWidgetOneRow");
        if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen, "");
        preferenceCategoryScreen = (PreferenceScreen)prefMng.findPreference("categoryWidgetIcon");
        if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen, "");
        if ((PPApplication.sLook != null) && PPApplication.sLookCocktailPanelEnabled) {
            preferenceCategoryScreen = (PreferenceScreen) prefMng.findPreference("categorySamsungEdgePanel");
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen, "");
        }

        //PreferenceScreen systemCategory = (PreferenceScreen) findPreference("categorySystem");
        if (!ActivateProfileHelper.getMergedRingNotificationVolumes(getActivity().getApplicationContext())) {
            Preference preference = findPreference(ApplicationPreferences.PREF_APPLICATION_UNLINK_RINGER_NOTIFICATION_VOLUMES);
            if (preference != null) {
                preference.setEnabled(false);
                preference.setSummary(R.string.phone_profiles_pref_applicationUnlinkRingerNotificationVolumesUnlinked_summary);
                //systemCategory.removePreference(preference);
            }
        }
        else {
            Preference preference = findPreference(ApplicationPreferences.PREF_APPLICATION_UNLINK_RINGER_NOTIFICATION_VOLUMES);
            if (preference != null) {
                preference.setEnabled(true);
                preference.setSummary(R.string.phone_profiles_pref_applicationUnlinkRingerNotificationVolumes_summary);
            }
            /*Preference preference = findPreference(ApplicationPreferences.PREF_APPLICATION_RINGER_NOTIFICATION_VOLUMES_UNLINKED_INFO);
            if (preference != null)
                systemCategory.removePreference(preference);*/
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
                            AlertDialog dialog = dialogBuilder.create();

                            //Button positive = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                            //if (positive != null) positive.setAllCaps(false);
                            //Button negative = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                            //if (negative != null) negative.setAllCaps(false);

                            dialog.show();
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
                /*if (PPApplication.romIsMIUI) {
                    preference.setSummary(R.string.phone_profiles_pref_writeSystemSettingPermissions_summary_miui);
                }*/
                preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        //if (!PPApplication.romIsMIUI) {
                            if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_MANAGE_WRITE_SETTINGS, getActivity().getApplicationContext())) {
                                @SuppressLint("InlinedApi")
                                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                                //intent.addCategory(Intent.CATEGORY_DEFAULT);
                                startActivityForResult(intent, RESULT_WRITE_SYSTEM_SETTINGS_PERMISSIONS);
                            } else {
                                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                                dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                                //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                                dialogBuilder.setPositiveButton(android.R.string.ok, null);
                                AlertDialog dialog = dialogBuilder.create();
                                /*dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                                    @Override
                                    public void onShow(DialogInterface dialog) {
                                        Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                                        if (positive != null) positive.setAllCaps(false);
                                        Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                                        if (negative != null) negative.setAllCaps(false);
                                    }
                                });*/
                                dialog.show();
                            }
                        /*}
                        else {
                            try {
                                // MIUI 8
                                Intent localIntent = new Intent("miui.intent.action.APP_PERM_EDITOR");
                                localIntent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity");
                                localIntent.putExtra("extra_pkgname", getActivity().getPackageName());
                                startActivityForResult(localIntent, RESULT_WRITE_SYSTEM_SETTINGS_PERMISSIONS);
                            } catch (Exception e) {
                                try {
                                    // MIUI 5/6/7
                                    Intent localIntent = new Intent("miui.intent.action.APP_PERM_EDITOR");
                                    localIntent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
                                    localIntent.putExtra("extra_pkgname", getActivity().getPackageName());
                                    startActivityForResult(localIntent, RESULT_WRITE_SYSTEM_SETTINGS_PERMISSIONS);
                                } catch (Exception e1) {
                                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                                    dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                                    //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                                    dialogBuilder.setPositiveButton(android.R.string.ok, null);
                                    AlertDialog dialog = dialogBuilder.create();
                                    //dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                                    //    @Override
                                    //    public void onShow(DialogInterface dialog) {
                                    //        Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                                    //        if (positive != null) positive.setAllCaps(false);
                                    //        Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                                    //        if (negative != null) negative.setAllCaps(false);
                                    //    }
                                    //});
                                    dialog.show();
                                }
                            }
                        }*/
                        return false;
                    }
                });
            }
            preference = prefMng.findPreference(PREF_ACCESS_NOTIFICATION_POLICY_PERMISSIONS);
            if (preference != null) {
                boolean a60 = (android.os.Build.VERSION.SDK_INT == 23) && Build.VERSION.RELEASE.equals("6.0");
                if ((!a60) &&
                        GlobalGUIRoutines.activityActionExists(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS, getActivity().getApplicationContext())) {
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
                    /*if (PPApplication.romIsMIUI) {
                        preference.setTitle(R.string.phone_profiles_pref_drawOverlaysPermissions_miui);
                        preference.setSummary(R.string.phone_profiles_pref_drawOverlaysPermissions_summary_miui);
                    }*/
                    preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            //if (!PPApplication.romIsMIUI) {
                                if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, getActivity().getApplicationContext())) {
                                    @SuppressLint("InlinedApi")
                                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                                    //intent.addCategory(Intent.CATEGORY_DEFAULT);
                                    startActivityForResult(intent, RESULT_DRAW_OVERLAYS_POLICY_PERMISSIONS);
                                } else {
                                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                                    dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                                    //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                                    dialogBuilder.setPositiveButton(android.R.string.ok, null);
                                    AlertDialog dialog = dialogBuilder.create();
                                    /*dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                                        @Override
                                        public void onShow(DialogInterface dialog) {
                                            Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                                            if (positive != null) positive.setAllCaps(false);
                                            Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                                            if (negative != null) negative.setAllCaps(false);
                                        }
                                    });*/
                                    dialog.show();
                                }
                            /*}
                            else {
                                try {
                                    // MIUI 8
                                    Intent localIntent = new Intent("miui.intent.action.APP_PERM_EDITOR");
                                    localIntent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity");
                                    localIntent.putExtra("extra_pkgname", getActivity().getPackageName());
                                    startActivityForResult(localIntent, RESULT_DRAW_OVERLAYS_POLICY_PERMISSIONS);
                                } catch (Exception e) {
                                    try {
                                        // MIUI 5/6/7
                                        Intent localIntent = new Intent("miui.intent.action.APP_PERM_EDITOR");
                                        localIntent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
                                        localIntent.putExtra("extra_pkgname", getActivity().getPackageName());
                                        startActivityForResult(localIntent, RESULT_DRAW_OVERLAYS_POLICY_PERMISSIONS);
                                    } catch (Exception e1) {
                                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                                        dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                                        //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                                        dialogBuilder.setPositiveButton(android.R.string.ok, null);
                                        AlertDialog dialog = dialogBuilder.create();
                                        //dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                                        //    @Override
                                        //    public void onShow(DialogInterface dialog) {
                                        //        Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                                        //        if (positive != null) positive.setAllCaps(false);
                                        //        Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                                        //        if (negative != null) negative.setAllCaps(false);
                                        //    }
                                        //});
                                        dialog.show();
                                    }
                                }
                            }*/
                            return false;
                        }
                    });
                /*} else {
                    PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("categoryPermissions");
                    preferenceCategory.removePreference(preference);
                }*/
            }
            preference = prefMng.findPreference(PREF_BATTERY_OPTIMIZATION_SYSTEM_SETTINGS);
            if (preference != null) {
                //preference.setWidgetLayoutResource(R.layout.start_activity_preference);
                preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS, getActivity().getApplicationContext())) {
                            @SuppressLint("InlinedApi")
                            Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                            //intent.addCategory(Intent.CATEGORY_DEFAULT);
                            startActivity(intent);
                        }
                        else {
                            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                            dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                            //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                            dialogBuilder.setPositiveButton(android.R.string.ok, null);
                            AlertDialog dialog = dialogBuilder.create();
                            /*dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                                @Override
                                public void onShow(DialogInterface dialog) {
                                    Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                                    if (positive != null) positive.setAllCaps(false);
                                    Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                                    if (negative != null) negative.setAllCaps(false);
                                }
                            });*/
                            dialog.show();
                        }
                        return false;
                    }
                });
            }

            if (!PPApplication.isRooted()) {
                PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("categoryPermissions");
                preference = findPreference(PREF_GRANT_ROOT_PERMISSION);
                if ((preferenceCategory != null) && (preference != null))
                    preferenceCategory.removePreference(preference);
            }
        }
        else {
            if (PPApplication.isRooted()) {
                PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("categoryPermissions");
                if (preferenceCategory != null) {
                    Preference preference = findPreference(PREF_WRITE_SYSTEM_SETTINGS_PERMISSIONS);
                    if (preference != null)
                        preferenceCategory.removePreference(preference);
                    preference = findPreference(PREF_ACCESS_NOTIFICATION_POLICY_PERMISSIONS);
                    if (preference != null)
                        preferenceCategory.removePreference(preference);
                    preference = findPreference(PREF_DRAW_OVERLAYS_PERMISSIONS);
                    if (preference != null)
                        preferenceCategory.removePreference(preference);
                    preference = findPreference(PREF_APPLICATION_PERMISSIONS);
                    if (preference != null)
                        preferenceCategory.removePreference(preference);
                }
            }
            else {
                PreferenceScreen preferenceScreen = (PreferenceScreen) findPreference("rootScreen");
                PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("categoryPermissions");
                if (preferenceCategory != null)
                    preferenceScreen.removePreference(preferenceCategory);
            }

            PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("categorySystem");
            Preference preference = findPreference(PREF_BATTERY_OPTIMIZATION_SYSTEM_SETTINGS);
            if (preference != null)
                preferenceCategory.removePreference(preference);
        }

        if (PPApplication.isRooted()) {
            Preference preference = findPreference(PREF_GRANT_ROOT_PERMISSION);
            if (preference != null) {
                preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        Permissions.grantRoot(null, getActivity());
                        return false;
                    }
                });
            }
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
        Preference preference = prefMng.findPreference(PREF_AUTOSTART_MANAGER);
        if (preference != null) {
            if (KillerManager.isActionAvailable(getActivity(), KillerManager.Actions.ACTION_AUTOSTART)) {
                preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        try {
                            KillerManager.doActionAutoStart(getActivity());
                        }catch (Exception e) {
                            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                            dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                            //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                            dialogBuilder.setPositiveButton(android.R.string.ok, null);
                            AlertDialog dialog = dialogBuilder.create();
                            //dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                            //    @Override
                            //    public void onShow(DialogInterface dialog) {
                            //        Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                            //        if (positive != null) positive.setAllCaps(false);
                            //        Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                            //        if (negative != null) negative.setAllCaps(false);
                            //    }
                            //});
                            dialog.show();
                        }
                        return false;
                    }
                });
            } else {
                PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("categoryApplicationStart");
                preferenceCategory.removePreference(preference);
            }
        }
        preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_TYPE);
        if (preference != null) {
            if (preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_TYPE, false)) {
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_COLOR);
                if (_preference != null)
                    _preference.setEnabled(true);
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_B);
                if (_preference != null)
                    _preference.setEnabled(false);
            }
            else {
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_COLOR);
                if (_preference != null)
                    _preference.setEnabled(false);
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_B);
                if (_preference != null)
                    _preference.setEnabled(true);
            }
        }
        preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_TYPE);
        if (preference != null) {
            if (preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_TYPE, false)) {
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_COLOR);
                if (_preference != null)
                    _preference.setEnabled(true);
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_B);
                if (_preference != null)
                    _preference.setEnabled(false);
            }
            else {
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_COLOR);
                if (_preference != null)
                    _preference.setEnabled(false);
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_B);
                if (_preference != null)
                    _preference.setEnabled(true);
            }
        }
        preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_TYPE);
        if (preference != null) {
            if (preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_TYPE, false)) {
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_COLOR);
                if (_preference != null)
                    _preference.setEnabled(true);
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_B);
                if (_preference != null)
                    _preference.setEnabled(false);
            }
            else {
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_COLOR);
                if (_preference != null)
                    _preference.setEnabled(false);
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_B);
                if (_preference != null)
                    _preference.setEnabled(true);
            }
        }
        preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_TYPE);
        if (preference != null) {
            if (preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_TYPE, false)) {
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_COLOR);
                if (_preference != null)
                    _preference.setEnabled(true);
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_LIGHTNESS_B);
                if (_preference != null)
                    _preference.setEnabled(false);
            }
            else {
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_COLOR);
                if (_preference != null)
                    _preference.setEnabled(false);
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_LIGHTNESS_B);
                if (_preference != null)
                    _preference.setEnabled(true);
            }
        }
        if (Build.VERSION.SDK_INT >= 26) {
            preference = prefMng.findPreference(PREF_NOTIFICATION_SYSTEM_SETTINGS);
            if (preference != null) {
                preference.setSummary(getString(R.string.phone_profiles_pref_notificationSystemSettings_summary) +
                        " " + getString(R.string.notification_channel_activated_profile));
                //preference.setWidgetLayoutResource(R.layout.start_activity_preference);
                preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @TargetApi(Build.VERSION_CODES.O)
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
                        intent.putExtra(Settings.EXTRA_CHANNEL_ID, PPApplication.PROFILE_NOTIFICATION_CHANNEL);
                        intent.putExtra(Settings.EXTRA_APP_PACKAGE, getActivity().getPackageName());
                        if (GlobalGUIRoutines.activityIntentExists(intent, getActivity().getApplicationContext())) {
                            startActivity(intent);
                        } else {
                            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                            dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                            //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                            dialogBuilder.setPositiveButton(android.R.string.ok, null);
                            AlertDialog dialog = dialogBuilder.create();
                            /*dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                                @Override
                                public void onShow(DialogInterface dialog) {
                                    Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                                    if (positive != null) positive.setAllCaps(false);
                                    Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                                    if (negative != null) negative.setAllCaps(false);
                                }
                            });*/
                            dialog.show();
                        }
                        return false;
                    }
                });
            }
        }
        preference = prefMng.findPreference(PREF_APPLICATION_POWER_MANAGER);
        if (preference != null) {
            if (KillerManager.isActionAvailable(getActivity(), KillerManager.Actions.ACTION_POWERSAVING)) {
                preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        try {
                            KillerManager.doActionPowerSaving(getActivity());
                        }catch (Exception e) {
                            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                            dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                            //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                            dialogBuilder.setPositiveButton(android.R.string.ok, null);
                            AlertDialog dialog = dialogBuilder.create();
                            //dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                            //    @Override
                            //    public void onShow(DialogInterface dialog) {
                            //        Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                            //        if (positive != null) positive.setAllCaps(false);
                            //        Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                            //        if (negative != null) negative.setAllCaps(false);
                            //    }
                            //});
                            dialog.show();
                        }
                        return false;
                    }
                });
            } else {
                PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("categorySystem");
                preferenceCategory.removePreference(preference);
            }
        }
    }

    private static void setPreferenceTitleStyle(Preference preference, boolean bold, /*boolean underline,*/ boolean errorColor)
    {
        if (preference != null) {
            CharSequence title = preference.getTitle();
            Spannable sbt = new SpannableString(title);
            Object spansToRemove[] = sbt.getSpans(0, title.length(), Object.class);
            for (Object span : spansToRemove) {
                if (span instanceof CharacterStyle)
                    sbt.removeSpan(span);
            }
            if (bold/* || underline*/) {
                //if (bold)
                    sbt.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, sbt.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                /*if (underline)
                    sbt.setSpan(new UnderlineSpan(), 0, sbt.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);*/
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

        if (Build.VERSION.SDK_INT < 26) {
            boolean notificationStatusBar = preferences.getBoolean(ApplicationPreferences.PREF_NOTIFICATION_STATUS_BAR, true);
            boolean notificationStatusBarPermanent = preferences.getBoolean(ApplicationPreferences.PREF_NOTIFICATION_STATUS_BAR_PERMANENT, true);
            PreferenceScreen preferenceCategoryNotifications = (PreferenceScreen) findPreference("categoryNotifications");
            if (!(notificationStatusBar && notificationStatusBarPermanent)) {
                setPreferenceTitleStyle(preferenceCategoryNotifications, true, true);
                if (preferenceCategoryNotifications != null) {
                    String summary = getString(R.string.phone_profiles_pref_notificationStatusBarNotEnabled_summary) + " " +
                            getString(R.string.phone_profiles_pref_notificationStatusBarRequired) + "\n\n";
                    setCategorySummary(preferenceCategoryNotifications, summary);
                }
            } else {
                setPreferenceTitleStyle(preferenceCategoryNotifications, false, false);
                if (preferenceCategoryNotifications != null) {
                    String summary = "";
                    setCategorySummary(preferenceCategoryNotifications, summary);
                }
            }
            if (key.equals(ApplicationPreferences.PREF_NOTIFICATION_STATUS_BAR)) {
                setPreferenceTitleStyle(preference, !notificationStatusBar, !notificationStatusBar);
            }
            if (key.equals(ApplicationPreferences.PREF_NOTIFICATION_STATUS_BAR_PERMANENT)) {
                setPreferenceTitleStyle(preference, !notificationStatusBarPermanent, !notificationStatusBarPermanent);
            }
        }

        if ((android.os.Build.VERSION.SDK_INT >= 21) && (android.os.Build.VERSION.SDK_INT < 26)) {
            if (key.equals(ApplicationPreferences.PREF_NOTIFICATION_SHOW_IN_STATUS_BAR)) {
                boolean show = preferences.getBoolean(key, true);
                Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_NOTIFICATION_HIDE_IN_LOCKSCREEN);
                if (_preference != null)
                    _preference.setEnabled(show);
            }
        }

        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_TYPE)) {
            if (preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_TYPE, false)) {
                Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_COLOR);
                if (_preference != null)
                    _preference.setEnabled(true);
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_B);
                if (_preference != null)
                    _preference.setEnabled(false);
            }
            else {
                Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_COLOR);
                if (_preference != null)
                    _preference.setEnabled(false);
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_B);
                if (_preference != null)
                    _preference.setEnabled(true);
            }
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_TYPE)) {
            if (preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_TYPE, false)) {
                Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_COLOR);
                if (_preference != null)
                    _preference.setEnabled(true);
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_B);
                if (_preference != null)
                    _preference.setEnabled(false);
            }
            else {
                Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_COLOR);
                if (_preference != null)
                    _preference.setEnabled(false);
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_B);
                if (_preference != null)
                    _preference.setEnabled(true);
            }
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_TYPE)) {
            if (preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_TYPE, false)) {
                Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_COLOR);
                if (_preference != null)
                    _preference.setEnabled(true);
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_B);
                if (_preference != null)
                    _preference.setEnabled(false);
            }
            else {
                Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_COLOR);
                if (_preference != null)
                    _preference.setEnabled(false);
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_B);
                if (_preference != null)
                    _preference.setEnabled(true);
            }
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_TYPE)) {
            if (preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_TYPE, false)) {
                Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_COLOR);
                if (_preference != null)
                    _preference.setEnabled(true);
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_LIGHTNESS_B);
                if (_preference != null)
                    _preference.setEnabled(false);
            }
            else {
                Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_COLOR);
                if (_preference != null)
                    _preference.setEnabled(false);
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_LIGHTNESS_B);
                if (_preference != null)
                    _preference.setEnabled(true);
            }
        }

        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_SHOW_BORDER)) {
            Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_BORDER);
            if (_preference != null) {
                _preference.setEnabled(preferences.getBoolean(key, false));
            }
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_SHOW_BORDER)) {
            Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_BORDER);
            if (_preference != null) {
                _preference.setEnabled(preferences.getBoolean(key, false));
            }
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_SHOW_BORDER)) {
            Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_BORDER);
            if (_preference != null) {
                _preference.setEnabled(preferences.getBoolean(key, false));
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
        else
        //noinspection StatementWithEmptyBody
        if (preference instanceof RingtonePreference) {
            // keep summary from preference
        }
        else {
            if (!stringValue.isEmpty()) {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                //preference.setSummary(preference.toString());
                preference.setSummary(stringValue);
            }
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
            if (_preference != null) {
                boolean colorful = preferences.getString(key, "0").equals("1");
                _preference.setEnabled(colorful);
            }
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_ICON_COLOR)) {
            Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_ICON_LIGHTNESS);
            if (_preference != null) {
                boolean colorful = preferences.getString(key, "0").equals("1");
                _preference.setEnabled(colorful);
            }
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_ICON_COLOR)) {
            Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_ICON_LIGHTNESS);
            if (_preference != null) {
                boolean colorful = preferences.getString(key, "0").equals("1");
                _preference.setEnabled(colorful);
            }
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_ICON_COLOR)) {
            Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_ICON_LIGHTNESS);
            if (_preference != null) {
                boolean colorful = preferences.getString(key, "0").equals("1");
                _preference.setEnabled(colorful);
            }
        }
        /*if (key.equals(PREF_GRANT_ROOT_PERMISSION)) {
            if (PPApplication.isRooted()) {
                String summary;
                if (PPApplication.isRootGranted(true))
                    summary = getString(R.string.permission_granted);
                else
                    summary = getString(R.string.permission_not_granted);
                preference.setSummary(summary);
            }
        }*/
        if (Build.VERSION.SDK_INT >= 23) {
            /*if (key.equals(PREF_APPLICATION_PERMISSIONS)) {
                // not possible to get granted runtime permission groups :-(
            }*/
            if (key.equals(PREF_WRITE_SYSTEM_SETTINGS_PERMISSIONS)) {
                String summary;
                if (Settings.System.canWrite(getActivity().getApplicationContext()))
                    summary = getString(R.string.permission_granted);
                else {
                    summary = getString(R.string.permission_not_granted);
                    summary = summary + "\n\n" + getString(R.string.phone_profiles_pref_writeSystemSettingPermissions_summary);
                }
                preference.setSummary(summary);
            }
            if (key.equals(PREF_ACCESS_NOTIFICATION_POLICY_PERMISSIONS)) {
                String summary;
                if (Permissions.checkAccessNotificationPolicy(getActivity().getApplicationContext()))
                    summary = getString(R.string.permission_granted);
                else {
                    summary = getString(R.string.permission_not_granted);
                    summary = summary + "\n\n" + getString(R.string.phone_profiles_pref_accessNotificationPolicyPermissions_summary);
                }
                preference.setSummary(summary);
            }
            if (key.equals(PREF_DRAW_OVERLAYS_PERMISSIONS)) {
                String summary;
                if (Settings.canDrawOverlays(getActivity().getApplicationContext()))
                    summary = getString(R.string.permission_granted);
                else {
                    summary = getString(R.string.permission_not_granted);
                    summary = summary + "\n\n" + getString(R.string.phone_profiles_pref_drawOverlaysPermissions_summary);
                }
                preference.setSummary(summary);
            }
        }
    }

    private void setCategorySummary(PreferenceScreen preferenceCategory, String summary) {
        String key = preferenceCategory.getKey();

        if (key.equals("applicationInterfaceCategory")) {
            summary = summary + getString(R.string.phone_profiles_pref_applicationLanguage);
            if (!summary.isEmpty()) summary = summary +" • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationTheme);
        }
        if (key.equals("categoryApplicationStart")) {
            summary = summary + getString(R.string.phone_profiles_pref_applicationStartOnBoot);
            if (KillerManager.isActionAvailable(getActivity(), KillerManager.Actions.ACTION_AUTOSTART)) {
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + getString(R.string.phone_profiles_pref_systemAutoStartManager);
            }
            if (!summary.isEmpty()) summary = summary +" • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationActivate);
        }
        if (key.equals("categorySystem")) {
            summary = summary + getString(R.string.phone_profiles_pref_applicationUnlinkRingerNotificationVolumes);
            if (!summary.isEmpty()) summary = summary +" • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationForceSetMergeRingNotificationVolumes);
            if (Build.VERSION.SDK_INT >= 23) {
                if (!summary.isEmpty()) summary = summary + " • ";
                summary = summary + getString(R.string.phone_profiles_pref_applicationBatteryOptimization);
            }
            if (KillerManager.isActionAvailable(getActivity(), KillerManager.Actions.ACTION_POWERSAVING)) {
                if (!summary.isEmpty()) summary = summary + " • ";
                summary = summary + getString(R.string.phone_profiles_pref_applicationPowerManager);
            }
        }
        if (key.equals("categoryPermissions")) {
            if (PPApplication.isRooted()) {
                summary = summary + getString(R.string.phone_profiles_pref_grantRootPermission);
            }
            if (Build.VERSION.SDK_INT >= 23) {
                if (!summary.isEmpty()) summary = summary + " • ";
                summary = summary + getString(R.string.phone_profiles_pref_writeSystemSettingPermissions);
                if (!summary.isEmpty()) summary = summary + " • ";
                summary = summary + getString(R.string.phone_profiles_pref_accessNotificationPolicyPermissions);
                if (!summary.isEmpty()) summary = summary + " • ";
                summary = summary + getString(R.string.phone_profiles_pref_drawOverlaysPermissions);
                if (!summary.isEmpty()) summary = summary + " • ";
                summary = summary + getString(R.string.phone_profiles_pref_applicationPermissions);
            }
        }
        if (key.equals("categoryNotifications")) {
            summary = summary + getString(R.string.phone_profiles_pref_notificationsToast);
            if (Build.VERSION.SDK_INT >= 26) {
                if (!summary.isEmpty()) summary = summary + " • ";
                summary = summary + getString(R.string.phone_profiles_pref_notificationSystemSettings);
            }
            else {
                if (!summary.isEmpty()) summary = summary + " • ";
                summary = summary + getString(R.string.phone_profiles_pref_notificationStatusBar);
                if (!summary.isEmpty()) summary = summary + " • ";
                summary = summary + getString(R.string.phone_profiles_pref_notificationStatusBarPermanent);
            }
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_notificationStatusBarStyle);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_notificationPrefIndicator);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_notificationTextColor);
        }
        if (key.equals("profileActivationCategory")) {
            summary = summary + getString(R.string.phone_profiles_pref_applicationEventBackgroundProfile);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationAlert);
        }
        if (key.equals("categoryActivator")) {
            summary = summary + getString(R.string.phone_profiles_pref_applicationPrefIndicator);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationHeader);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationClose);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationGridLayout);
        }
        if (key.equals("categoryEditor")) {
            summary = summary + getString(R.string.phone_profiles_pref_applicationPrefIndicator);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationHeader);
        }
        if (key.equals("categoryWidgetList")) {
            summary = summary + getString(R.string.phone_profiles_pref_applicationPrefIndicator);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationHeader);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationGridLayout);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetBackgroundType);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetBackground);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetLightnessB);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetColorB);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetShowBorder);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetRoundedCorners);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetLightnessT);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetIconColor);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetLightnessI);
        }
        if (key.equals("categoryWidgetOneRow")) {
            summary = summary + getString(R.string.phone_profiles_pref_applicationPrefIndicator);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetBackgroundType);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetBackground);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetLightnessB);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetColorB);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetShowBorder);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetRoundedCorners);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetLightnessT);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetIconColor);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetLightnessI);
        }
        if (key.equals("categoryWidgetIcon")) {
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetIconBackgroundType);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetIconBackground);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetIconLightnessB);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetIconColorB);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetIconShowBorder);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetIconRoundedCorners);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetIconHideProfileName);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetIconLightnessT);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetIconColor);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetLightnessI);
        }
        if ((PPApplication.sLook != null) && PPApplication.sLookCocktailPanelEnabled) {
            if (key.equals("categorySamsungEdgePanel")) {
                summary = summary + getString(R.string.phone_profiles_pref_applicationHeader);
                if (!summary.isEmpty()) summary = summary + " • ";
                summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetBackgroundType);
                if (!summary.isEmpty()) summary = summary + " • ";
                summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetBackground);
                if (!summary.isEmpty()) summary = summary + " • ";
                summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetLightnessB);
                if (!summary.isEmpty()) summary = summary + " • ";
                summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetColorB);
                if (!summary.isEmpty()) summary = summary + " • ";
                summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetLightnessT);
                if (!summary.isEmpty()) summary = summary + " • ";
                summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetIconColor);
                if (!summary.isEmpty()) summary = summary + " • ";
                summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetLightnessI);
            }
        }

        if (!summary.isEmpty()) summary = summary +" • ";
        summary = summary + "…";

        preferenceCategory.setSummary(summary);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
            String key) {
        setSummary(key);
    }

    @Override
    public void onDestroy()
    {
        try {
            preferences.unregisterOnSharedPreferenceChangeListener(this);
        } catch (Exception ignored) {}
        super.onDestroy();
    }

    public void doOnActivityResult(int requestCode/*, int resultCode, Intent data*/)
    {
        if ((requestCode == RESULT_APPLICATION_PERMISSIONS) ||
            (requestCode == RESULT_WRITE_SYSTEM_SETTINGS_PERMISSIONS) ||
            (requestCode == RESULT_ACCESS_NOTIFICATION_POLICY_PERMISSIONS) ||
            (requestCode == RESULT_DRAW_OVERLAYS_POLICY_PERMISSIONS)) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Context context = getActivity().getApplicationContext();

                boolean finishActivity = false;
                boolean permissionsChanged = Permissions.getPermissionsChanged(context);

                if (requestCode == RESULT_WRITE_SYSTEM_SETTINGS_PERMISSIONS) {
                    boolean canWrite = Settings.System.canWrite(context);
                    permissionsChanged = Permissions.getWriteSystemSettingsPermission(context) != canWrite;
                    if (canWrite)
                        Permissions.setShowRequestWriteSettingsPermission(context, true);
                }
                if (requestCode == RESULT_ACCESS_NOTIFICATION_POLICY_PERMISSIONS) {
                    NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    boolean notificationPolicyGranted = (mNotificationManager != null) && (mNotificationManager.isNotificationPolicyAccessGranted());
                    permissionsChanged = Permissions.getNotificationPolicyPermission(context) != notificationPolicyGranted;
                    if (notificationPolicyGranted)
                        Permissions.setShowRequestAccessNotificationPolicyPermission(context, true);
                }
                if (requestCode == RESULT_DRAW_OVERLAYS_POLICY_PERMISSIONS) {
                    boolean canDrawOverlays = Settings.canDrawOverlays(context);
                    permissionsChanged = Permissions.getDrawOverlayPermission(context) != canDrawOverlays;
                    if (canDrawOverlays)
                        Permissions.setShowRequestDrawOverlaysPermission(context, true);
                }
                if (requestCode == RESULT_APPLICATION_PERMISSIONS) {
                    boolean calendarPermission = Permissions.checkCalendar(context);
                    permissionsChanged = Permissions.getCalendarPermission(context) != calendarPermission;
                    // finish Editor when permission is disabled
                    finishActivity = permissionsChanged && (!calendarPermission);
                    if (!permissionsChanged) {
                        boolean contactsPermission = Permissions.checkContacts(context);
                        permissionsChanged = Permissions.getContactsPermission(context) != contactsPermission;
                        // finish Editor when permission is disabled
                        finishActivity = permissionsChanged && (!contactsPermission);
                    }
                    if (!permissionsChanged) {
                        boolean locationPermission = Permissions.checkLocation(context);
                        permissionsChanged = Permissions.getLocationPermission(context) != locationPermission;
                        // finish Editor when permission is disabled
                        finishActivity = permissionsChanged && (!locationPermission);
                    }
                    if (!permissionsChanged) {
                        boolean smsPermission = Permissions.checkSMS(context);
                        permissionsChanged = Permissions.getSMSPermission(context) != smsPermission;
                        // finish Editor when permission is disabled
                        finishActivity = permissionsChanged && (!smsPermission);
                    }
                    if (!permissionsChanged) {
                        boolean phonePermission = Permissions.checkPhone(context);
                        permissionsChanged = Permissions.getPhonePermission(context) != phonePermission;
                        // finish Editor when permission is disabled
                        finishActivity = permissionsChanged && (!phonePermission);
                    }
                    if (!permissionsChanged) {
                        boolean storagePermission = Permissions.checkStorage(context);
                        permissionsChanged = Permissions.getStoragePermission(context) != storagePermission;
                        // finish Editor when permission is disabled
                        finishActivity = permissionsChanged && (!storagePermission);
                    }
                    if (!permissionsChanged) {
                        boolean cameraPermission = Permissions.checkCamera(context);
                        permissionsChanged = Permissions.getCameraPermission(context) != cameraPermission;
                        // finish Editor when permission is disabled
                        finishActivity = permissionsChanged && (!cameraPermission);
                    }
                    if (!permissionsChanged) {
                        boolean microphonePermission = Permissions.checkMicrophone(context);
                        permissionsChanged = Permissions.getMicrophonePermission(context) != microphonePermission;
                        // finish Editor when permission is disabled
                        finishActivity = permissionsChanged && (!microphonePermission);
                    }
                    if (!permissionsChanged) {
                        boolean sensorsPermission = Permissions.checkSensors(context);
                        permissionsChanged = Permissions.getSensorsPermission(context) != sensorsPermission;
                        // finish Editor when permission is disabled
                        finishActivity = permissionsChanged && (!sensorsPermission);
                    }
                }

                Permissions.saveAllPermissions(context, permissionsChanged);

                if (permissionsChanged) {
                    //DataWrapper dataWrapper = new DataWrapper(context, false, 0);

                    //Profile activatedProfile = dataWrapper.getActivatedProfile(true, true);
                    //dataWrapper.refreshProfileIcon(activatedProfile);
                    PPApplication.showProfileNotification(context);
                    ActivateProfileHelper.updateGUI(context, true);

                    if (finishActivity) {
                        getActivity().setResult(Activity.RESULT_CANCELED);
                        getActivity().finishAffinity();
                    } else {
                        getActivity().setResult(Activity.RESULT_OK);
                    }
                }
                else
                    getActivity().setResult(Activity.RESULT_CANCELED);
            }
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        doOnActivityResult(requestCode/*, resultCode, data*/);
    }

    @Override
    protected String getSavedInstanceStateKeyName() {
        return "PhoneProfilesPreferencesFragment_PreferenceScreenKey";
    }

}
