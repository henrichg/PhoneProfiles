package sk.henrichg.phoneprofiles;

import android.content.Context;
import android.content.SharedPreferences;

class ApplicationPreferences {

    static final String PREF_APPLICATION_START_ON_BOOT = "applicationStartOnBoot";
    static final String PREF_APPLICATION_ACTIVATE = "applicationActivate";
    static final String PREF_APPLICATION_ALERT = "applicationAlert";
    static final String PREF_APPLICATION_CLOSE = "applicationClose";
    static final String PREF_APPLICATION_LONG_PRESS_ACTIVATION = "applicationLongClickActivation";
    static final String PREF_APPLICATION_LANGUAGE = "applicationLanguage";
    static final String PREF_APPLICATION_THEME = "applicationTheme";
    static final String PREF_APPLICATION_ACTIVATOR_PREF_INDICATOR = "applicationActivatorPrefIndicator";
    static final String PREF_APPLICATION_EDITOR_PREF_INDICATOR = "applicationEditorPrefIndicator";
    static final String PREF_APPLICATION_ACTIVATOR_HEADER = "applicationActivatorHeader";
    static final String PREF_APPLICATION_EDITOR_HEADER = "applicationEditorHeader";
    static final String PREF_NOTIFICATION_TOAST = "notificationsToast";
    static final String PREF_NOTIFICATION_STATUS_BAR  = "notificationStatusBar";
    static final String PREF_NOTIFICATION_STATUS_BAR_STYLE  = "notificationStatusBarStyle";
    static final String PREF_NOTIFICATION_STATUS_BAR_PERMANENT  = "notificationStatusBarPermanent";
    static final String PREF_NOTIFICATION_STATUS_BAR_CANCEL  = "notificationStatusBarCancel";
    static final String PREF_NOTIFICATION_SHOW_IN_STATUS_BAR  = "notificationShowInStatusBar";
    static final String PREF_NOTIFICATION_TEXT_COLOR = "notificationTextColor";
    static final String PREF_APPLICATION_WIDGET_LIST_PREF_INDICATOR = "applicationWidgetListPrefIndicator";
    static final String PREF_APPLICATION_WIDGET_LIST_HEADER = "applicationWidgetListHeader";
    static final String PREF_APPLICATION_WIDGET_LIST_BACKGROUND = "applicationWidgetListBackground";
    static final String PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_B = "applicationWidgetListLightnessB";
    static final String PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_T = "applicationWidgetListLightnessT";
    static final String PREF_APPLICATION_WIDGET_ICON_COLOR = "applicationWidgetIconColor";
    static final String PREF_APPLICATION_WIDGET_ICON_LIGHTNESS = "applicationWidgetIconLightness";
    static final String PREF_APPLICATION_WIDGET_LIST_ICON_COLOR = "applicationWidgetListIconColor";
    static final String PREF_APPLICATION_WIDGET_LIST_ICON_LIGHTNESS = "applicationWidgetListIconLightness";
    static final String PREF_NOTIFICATION_PREF_INDICATOR = "notificationPrefIndicator";
    static final String PREF_APPLICATION_BACKGROUND_PROFILE = "applicationBackgroundProfile";
    static final String PREF_APPLICATION_ACTIVATOR_GRID_LAYOUT= "applicationActivatorGridLayout";
    static final String PREF_APPLICATION_WIDGET_LIST_GRID_LAYOUT= "applicationWidgetListGridLayout";
    static final String PREF_APPLICATION_WIDGET_ICON_HIDE_PROFILE_NAME = "applicationWidgetIconHideProfileName";
    static final String PREF_APPLICATION_UNLINK_RINGER_NOTIFICATION_VOLUMES = "applicationUnlinkRingerNotificationVolumes";
    static final String PREF_APPLICATION_SHORTCUT_EMBLEM = "applicationShortcutEmblem";
    static final String PREF_NOTIFICATION_HIDE_IN_LOCKSCREEN = "notificationHideInLockscreen";
    static final String PREF_APPLICATION_WIDGET_ICON_BACKGROUND = "applicationWidgetIconBackground";
    static final String PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_B = "applicationWidgetIconLightnessB";
    static final String PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_T = "applicationWidgetIconLightnessT";
    static final String PREF_NOTIFICATION_THEME = "notificationTheme";
    static final String PREF_APPLICATION_FORCE_SET_MERGE_RINGER_NOTIFICATION_VOLUMES = "applicationForceSetMergeRingNotificationVolumes";

    static boolean applicationStartOnBoot(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_APPLICATION_START_ON_BOOT, true);
    }

    static boolean applicationActivate(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_APPLICATION_ACTIVATE, true);
    }

    static boolean applicationActivateWithAlert(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_APPLICATION_ALERT, true);
    }

    static boolean applicationClose(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_APPLICATION_CLOSE, true);
    }

    static boolean applicationLongClickActivation(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_APPLICATION_LONG_PRESS_ACTIVATION, false);
    }

    static String applicationLanguage(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(PREF_APPLICATION_LANGUAGE, "system");
    }

    static public String applicationTheme(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        String applicationTheme = preferences.getString(PREF_APPLICATION_THEME, "material");
        if (applicationTheme.equals("light")){
            applicationTheme = "material";
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(PREF_APPLICATION_THEME, applicationTheme);
            editor.commit();
        }
        return applicationTheme;
    }

    static boolean applicationActivatorPrefIndicator(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_APPLICATION_ACTIVATOR_PREF_INDICATOR, true);
    }

    static public boolean applicationEditorPrefIndicator(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_APPLICATION_EDITOR_PREF_INDICATOR, true);
    }

    static boolean applicationActivatorHeader(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_APPLICATION_ACTIVATOR_HEADER, true);
    }

    static boolean applicationEditorHeader(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_APPLICATION_EDITOR_HEADER, true);
    }

    static boolean notificationsToast(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_NOTIFICATION_TOAST, true);
    }

    static boolean notificationStatusBar(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_NOTIFICATION_STATUS_BAR, true);
    }

    static boolean notificationStatusBarPermanent(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_NOTIFICATION_STATUS_BAR_PERMANENT, true);
    }

    static String notificationStatusBarCancel(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(PREF_NOTIFICATION_STATUS_BAR_CANCEL, "10");
    }

    static String notificationStatusBarStyle(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(PREF_NOTIFICATION_STATUS_BAR_STYLE, "1");
    }

    static boolean notificationShowInStatusBar(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_NOTIFICATION_SHOW_IN_STATUS_BAR, true);
    }

    static String notificationTextColor(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(PREF_NOTIFICATION_TEXT_COLOR, "0");
    }

    static boolean notificationHideInLockscreen(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_NOTIFICATION_HIDE_IN_LOCKSCREEN, false);
    }

    static String notificationTheme(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(PREF_NOTIFICATION_THEME, "0");
    }

    static boolean applicationWidgetListPrefIndicator(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_APPLICATION_WIDGET_LIST_PREF_INDICATOR, true);
    }

    static boolean applicationWidgetListHeader(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_APPLICATION_WIDGET_LIST_HEADER, true);
    }

    static String applicationWidgetListBackground(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(PREF_APPLICATION_WIDGET_LIST_BACKGROUND, "25");
    }

    static String applicationWidgetListLightnessB(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_B, "0");
    }

    static String applicationWidgetListLightnessT(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_T, "100");
    }

    static String applicationWidgetIconColor(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(PREF_APPLICATION_WIDGET_ICON_COLOR, "0");
    }

    static String applicationWidgetIconLightness(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(PREF_APPLICATION_WIDGET_ICON_LIGHTNESS, "100");
    }

    static String applicationWidgetListIconColor(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(PREF_APPLICATION_WIDGET_LIST_ICON_COLOR, "0");
    }

    static String applicationWidgetListIconLightness(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(PREF_APPLICATION_WIDGET_LIST_ICON_LIGHTNESS, "100");
    }

    static boolean notificationPrefIndicator(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_NOTIFICATION_PREF_INDICATOR, true);
    }

    static String applicationBackgroundProfile(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(PREF_APPLICATION_BACKGROUND_PROFILE, "-999");
    }

    static boolean applicationActivatorGridLayout(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_APPLICATION_ACTIVATOR_GRID_LAYOUT, true);
    }

    static boolean applicationWidgetListGridLayout(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_APPLICATION_WIDGET_LIST_GRID_LAYOUT, true);
    }

    static boolean applicationWidgetIconHideProfileName(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_APPLICATION_WIDGET_ICON_HIDE_PROFILE_NAME, false);
    }

    static boolean applicationUnlinkRingerNotificationVolumes(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_APPLICATION_UNLINK_RINGER_NOTIFICATION_VOLUMES, false);
    }

    static boolean applicationShortcutEmblem(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_APPLICATION_SHORTCUT_EMBLEM, true);
    }

    static String applicationWidgetIconBackground(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(PREF_APPLICATION_WIDGET_ICON_BACKGROUND, "0");
    }

    static String applicationWidgetIconLightnessB(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_B, "0");
    }

    static String applicationWidgetIconLightnessT(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_T, "100");
    }

    static int applicationForceSetMergeRingNotificationVolumes(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return Integer.valueOf(preferences.getString(PREF_APPLICATION_FORCE_SET_MERGE_RINGER_NOTIFICATION_VOLUMES, "0"));
    }

}
