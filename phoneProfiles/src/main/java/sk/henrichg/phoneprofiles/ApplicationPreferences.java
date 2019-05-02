package sk.henrichg.phoneprofiles;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

class ApplicationPreferences {

    static SharedPreferences preferences = null;

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
    //static final String PREF_APPLICATION_RINGER_NOTIFICATION_VOLUMES_UNLINKED_INFO = "applicationRingerNotificationVolumesUnlinkedInfo";
    static final String PREF_APPLICATION_SHORTCUT_EMBLEM = "applicationShortcutEmblem";
    static final String PREF_NOTIFICATION_HIDE_IN_LOCKSCREEN = "notificationHideInLockscreen";
    static final String PREF_APPLICATION_WIDGET_ICON_BACKGROUND = "applicationWidgetIconBackground";
    static final String PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_B = "applicationWidgetIconLightnessB";
    static final String PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_T = "applicationWidgetIconLightnessT";
    //static final String PREF_NOTIFICATION_THEME = "notificationTheme";
    static final String PREF_APPLICATION_FORCE_SET_MERGE_RINGER_NOTIFICATION_VOLUMES = "applicationForceSetMergeRingNotificationVolumes";
    //private static final String PREF_APPLICATION_SAMSUNG_EDGE_PREF_INDICATOR = "applicationSamsungEdgePrefIndicator";
    static final String PREF_APPLICATION_SAMSUNG_EDGE_HEADER = "applicationSamsungEdgeHeader";
    static final String PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND = "applicationSamsungEdgeBackground";
    static final String PREF_APPLICATION_SAMSUNG_EDGE_LIGHTNESS_B = "applicationSamsungEdgeLightnessB";
    static final String PREF_APPLICATION_SAMSUNG_EDGE_LIGHTNESS_T = "applicationSamsungEdgeLightnessT";
    static final String PREF_APPLICATION_SAMSUNG_EDGE_ICON_COLOR = "applicationSamsungEdgeIconColor";
    static final String PREF_APPLICATION_SAMSUNG_EDGE_ICON_LIGHTNESS = "applicationSamsungEdgeIconLightness";
    //private static final String PREF_APPLICATION_SAMSUNG_EDGE_GRID_LAYOUT= "applicationSamsungEdgeGridLayout";
    static final String PREF_APPLICATION_WIDGET_LIST_ROUNDED_CORNERS = "applicationWidgetListRoundedCorners";
    static final String PREF_APPLICATION_WIDGET_ICON_ROUNDED_CORNERS = "applicationWidgetIconRoundedCorners";
    static final String PREF_APPLICATION_WIDGET_LIST_BACKGROUND_TYPE = "applicationWidgetListBackgroundType";
    static final String PREF_APPLICATION_WIDGET_LIST_BACKGROUND_COLOR = "applicationWidgetListBackgroundColor";
    static final String PREF_APPLICATION_WIDGET_ICON_BACKGROUND_TYPE = "applicationWidgetIconBackgroundType";
    static final String PREF_APPLICATION_WIDGET_ICON_BACKGROUND_COLOR = "applicationWidgetIconBackgroundColor";
    static final String PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_TYPE = "applicationSamsungEdgeBackgroundType";
    static final String PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_COLOR = "applicationSamsungEdgeBackgroundColor";
    static final String PREF_APPLICATION_NEVER_ASK_FOR_GRANT_ROOT = "applicationNeverAskForGrantRoot";
    static final String PREF_NOTIFICATION_SHOW_BUTTON_EXIT = "notificationShowButtonExit";
    static final String PREF_APPLICATION_WIDGET_ONE_ROW_PREF_INDICATOR = "applicationWidgetOneRowPrefIndicator";
    static final String PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND = "applicationWidgetOneRowBackground";
    static final String PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_B = "applicationWidgetOneRowLightnessB";
    static final String PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_T = "applicationWidgetOneRowLightnessT";
    static final String PREF_APPLICATION_WIDGET_ONE_ROW_ICON_COLOR = "applicationWidgetOneRowIconColor";
    static final String PREF_APPLICATION_WIDGET_ONE_ROW_ICON_LIGHTNESS = "applicationWidgetOneRowIconLightness";
    static final String PREF_APPLICATION_WIDGET_ONE_ROW_ROUNDED_CORNERS = "applicationWidgetOneRowRoundedCorners";
    static final String PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_TYPE = "applicationWidgetOneRowBackgroundType";
    static final String PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_COLOR = "applicationWidgetOneRowBackgroundColor";
    static final String PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_BORDER = "applicationWidgetListLightnessBorder";
    static final String PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_BORDER = "applicationWidgetOneRowLightnessBorder";
    static final String PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_BORDER = "applicationWidgetIconLightnessBorder";
    static final String PREF_APPLICATION_WIDGET_LIST_SHOW_BORDER = "applicationWidgetListShowBorder";
    static final String PREF_APPLICATION_WIDGET_ONE_ROW_SHOW_BORDER = "applicationWidgetOneRowShowBorder";
    static final String PREF_APPLICATION_WIDGET_ICON_SHOW_BORDER = "applicationWidgetIconShowBorder";
    //static final String PREF_APPLICATION_NOT_DARK_THEME = "applicationNotDarkTheme";
    static final String PREF_APPLICATION_WIDGET_LIST_CUSTOM_ICON_LIGHTNESS = "applicationWidgetListCustomIconLightness";
    static final String PREF_APPLICATION_WIDGET_ONE_ROW_CUSTOM_ICON_LIGHTNESS = "applicationWidgetOneRowCustomIconLightness";
    static final String PREF_APPLICATION_WIDGET_ICON_CUSTOM_ICON_LIGHTNESS = "applicationWidgetIconCustomIconLightness";
    static final String PREF_APPLICATION_SAMSUNG_EDGE_CUSTOM_ICON_LIGHTNESS = "applicationSamsungEdgeCustomIconLightness";
    //static final String PREF_NOTIFICATION_DARK_BACKGROUND = "notificationDarkBackground";
    static final String PREF_NOTIFICATION_USE_DECORATION = "notificationUseDecoration";
    static final String PREF_NOTIFICATION_LAYOUT_TYPE = "notificationLayoutType";
    static final String PREF_NOTIFICATION_BACKGROUND_COLOR = "notificationBackgroundColor";
    static final String PREF_APPLICATION_NIGHT_MODE_OFF_THEME = "applicationNightModeOffTheme";

    static SharedPreferences getSharedPreferences(Context context) {
        if (preferences == null)
            preferences = context.getApplicationContext().getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences;
    }

    static boolean applicationStartOnBoot(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_START_ON_BOOT, true);
    }

    static boolean applicationActivate(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_ACTIVATE, true);
    }

    static boolean applicationActivateWithAlert(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_ALERT, true);
    }

    static boolean applicationClose(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_CLOSE, true);
    }

    static boolean applicationLongClickActivation(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_LONG_PRESS_ACTIVATION, false);
    }

    static String applicationLanguage(Context context) {
        return getSharedPreferences(context).getString(PREF_APPLICATION_LANGUAGE, "system");
    }

    static public String applicationTheme(Context context, boolean useNightMode) {
        String applicationTheme = getSharedPreferences(context).getString(PREF_APPLICATION_THEME, "color");
        if (applicationTheme.equals("light")){
            applicationTheme = "color";
            SharedPreferences.Editor editor = getSharedPreferences(context).edit();
            editor.putString(PREF_APPLICATION_THEME, applicationTheme);
            editor.apply();
        }
        if (applicationTheme.equals("material")){
            applicationTheme = "color";
            SharedPreferences.Editor editor = getSharedPreferences(context).edit();
            editor.putString(PREF_APPLICATION_THEME, applicationTheme);
            editor.apply();
        }
        if (applicationTheme.equals("night_mode") && useNightMode) {
            int nightModeFlags =
                    context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            switch (nightModeFlags) {
                case Configuration.UI_MODE_NIGHT_YES:
                    applicationTheme = "dark";
                    break;
                case Configuration.UI_MODE_NIGHT_NO:
                    applicationTheme = getSharedPreferences(context).getString(PREF_APPLICATION_NIGHT_MODE_OFF_THEME, "color");
                    break;
                case Configuration.UI_MODE_NIGHT_UNDEFINED:
                    applicationTheme = getSharedPreferences(context).getString(PREF_APPLICATION_NIGHT_MODE_OFF_THEME, "color");
                    break;
            }
        }
        return applicationTheme;
    }

    static boolean applicationActivatorPrefIndicator(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_ACTIVATOR_PREF_INDICATOR, true);
    }

    static boolean applicationEditorPrefIndicator(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_EDITOR_PREF_INDICATOR, true);
    }

    static boolean applicationActivatorHeader(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_ACTIVATOR_HEADER, true);
    }

    static boolean applicationEditorHeader(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_EDITOR_HEADER, true);
    }

    static boolean notificationsToast(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_NOTIFICATION_TOAST, true);
    }

    static boolean notificationStatusBar(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_NOTIFICATION_STATUS_BAR, true);
    }

    static boolean notificationStatusBarPermanent(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_NOTIFICATION_STATUS_BAR_PERMANENT, true);
    }

    static String notificationStatusBarCancel(Context context) {
        return getSharedPreferences(context).getString(PREF_NOTIFICATION_STATUS_BAR_CANCEL, "10");
    }

    static String notificationStatusBarStyle(Context context) {
        return getSharedPreferences(context).getString(PREF_NOTIFICATION_STATUS_BAR_STYLE, "1");
    }

    static boolean notificationShowInStatusBar(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_NOTIFICATION_SHOW_IN_STATUS_BAR, true);
    }

    static String notificationTextColor(Context context) {
        return getSharedPreferences(context).getString(PREF_NOTIFICATION_TEXT_COLOR, "0");
    }

    static boolean notificationHideInLockScreen(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_NOTIFICATION_HIDE_IN_LOCKSCREEN, false);
    }

    /*
    static String notificationTheme(Context context) {
        return getSharedPreferences(context).getString(PREF_NOTIFICATION_THEME, "0");
    }
    */

    static boolean applicationWidgetListPrefIndicator(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_LIST_PREF_INDICATOR, true);
    }

    static boolean applicationWidgetListHeader(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_LIST_HEADER, true);
    }

    static String applicationWidgetListBackground(Context context) {
        return getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_LIST_BACKGROUND, "25");
    }

    static String applicationWidgetListLightnessB(Context context) {
        return getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_B, "0");
    }

    static String applicationWidgetListLightnessT(Context context) {
        return getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_T, "100");
    }

    static String applicationWidgetIconColor(Context context) {
        return getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ICON_COLOR, "0");
    }

    static String applicationWidgetIconLightness(Context context) {
        return getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ICON_LIGHTNESS, "100");
    }

    static String applicationWidgetListIconColor(Context context) {
        return getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_LIST_ICON_COLOR, "0");
    }

    static String applicationWidgetListIconLightness(Context context) {
        return getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_LIST_ICON_LIGHTNESS, "100");
    }

    static boolean notificationPrefIndicator(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_NOTIFICATION_PREF_INDICATOR, true);
    }

    static String applicationBackgroundProfile(Context context) {
        return getSharedPreferences(context).getString(PREF_APPLICATION_BACKGROUND_PROFILE, "-999");
    }

    static boolean applicationActivatorGridLayout(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_ACTIVATOR_GRID_LAYOUT, true);
    }

    static boolean applicationWidgetListGridLayout(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_LIST_GRID_LAYOUT, true);
    }

    static boolean applicationWidgetIconHideProfileName(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_ICON_HIDE_PROFILE_NAME, false);
    }

    static boolean applicationShortcutEmblem(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_SHORTCUT_EMBLEM, true);
    }

    static String applicationWidgetIconBackground(Context context) {
        return getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ICON_BACKGROUND, "25");
    }

    static String applicationWidgetIconLightnessB(Context context) {
        return getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_B, "0");
    }

    static String applicationWidgetIconLightnessT(Context context) {
        return getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_T, "100");
    }

    static boolean applicationUnlinkRingerNotificationVolumes(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_UNLINK_RINGER_NOTIFICATION_VOLUMES, false);
    }

    static int applicationForceSetMergeRingNotificationVolumes(Context context) {
        return Integer.valueOf(getSharedPreferences(context).getString(PREF_APPLICATION_FORCE_SET_MERGE_RINGER_NOTIFICATION_VOLUMES, "0"));
    }

    /*
    static boolean applicationSamsungEdgePrefIndicator(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_SAMSUNG_EDGE_PREF_INDICATOR, false);
    }
    */

    static boolean applicationSamsungEdgeHeader(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_SAMSUNG_EDGE_HEADER, true);
    }

    static String applicationSamsungEdgeBackground(Context context) {
        return getSharedPreferences(context).getString(PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND, "25");
    }

    static String applicationSamsungEdgeLightnessB(Context context) {
        return getSharedPreferences(context).getString(PREF_APPLICATION_SAMSUNG_EDGE_LIGHTNESS_B, "0");
    }

    static String applicationSamsungEdgeLightnessT(Context context) {
        return getSharedPreferences(context).getString(PREF_APPLICATION_SAMSUNG_EDGE_LIGHTNESS_T, "100");
    }

    static String applicationSamsungEdgeIconColor(Context context) {
        return getSharedPreferences(context).getString(PREF_APPLICATION_SAMSUNG_EDGE_ICON_COLOR, "0");
    }

    static String applicationSamsungEdgeIconLightness(Context context) {
        return getSharedPreferences(context).getString(PREF_APPLICATION_SAMSUNG_EDGE_ICON_LIGHTNESS, "100");
    }

    /*
    static boolean applicationSamsungEdgeGridLayout(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_SAMSUNG_EDGE_GRID_LAYOUT, true);
    }
    */

    static boolean applicationWidgetListRoundedCorners(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_LIST_ROUNDED_CORNERS, true);
    }

    static boolean applicationWidgetIconRoundedCorners(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_ICON_ROUNDED_CORNERS, true);
    }

    static boolean applicationWidgetListBackgroundType(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_LIST_BACKGROUND_TYPE, false);
    }

    static String applicationWidgetListBackgroundColor(Context context) {
        return getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_LIST_BACKGROUND_COLOR, "-1"); // white color
    }

    static boolean applicationWidgetIconBackgroundType(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_ICON_BACKGROUND_TYPE, false);
    }

    static String applicationWidgetIconBackgroundColor(Context context) {
        return getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ICON_BACKGROUND_COLOR, "-1"); // white color
    }

    static boolean applicationSamsungEdgeBackgroundType(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_TYPE, false);
    }

    static String applicationSamsungEdgeBackgroundColor(Context context) {
        return getSharedPreferences(context).getString(PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_COLOR, "-1"); // white color
    }

    static boolean applicationNeverAskForGrantRoot(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_NEVER_ASK_FOR_GRANT_ROOT, false);
    }

    static boolean notificationShowButtonExit(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_NOTIFICATION_SHOW_BUTTON_EXIT, false);
    }

    static boolean applicationWidgetOneRowPrefIndicator(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_ONE_ROW_PREF_INDICATOR, true);
    }

    static String applicationWidgetOneRowBackground(Context context) {
        return getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND, "25");
    }

    static String applicationWidgetOneRowLightnessB(Context context) {
        return getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_B, "0");
    }

    static String applicationWidgetOneRowLightnessT(Context context) {
        return getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_T, "100");
    }

    static String applicationWidgetOneRowIconColor(Context context) {
        return getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ONE_ROW_ICON_COLOR, "0");
    }

    static String applicationWidgetOneRowIconLightness(Context context) {
        return getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ONE_ROW_ICON_LIGHTNESS, "100");
    }

    static boolean applicationWidgetOneRowRoundedCorners(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_ONE_ROW_ROUNDED_CORNERS, true);
    }

    static boolean applicationWidgetOneRowBackgroundType(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_TYPE, false);
    }

    static String applicationWidgetOneRowBackgroundColor(Context context) {
        return getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_COLOR, "-1"); // white color
    }

    static String applicationWidgetListLightnessBorder(Context context) {
        return getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_BORDER, "100");
    }

    static String applicationWidgetOneRowLightnessBorder(Context context) {
        return getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_BORDER, "100");
    }

    static String applicationWidgetIconLightnessBorder(Context context) {
        return getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_BORDER, "100");
    }

    static boolean applicationWidgetListShowBorder(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_LIST_SHOW_BORDER, false);
    }

    static boolean applicationWidgetOneRowShowBorder(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_ONE_ROW_SHOW_BORDER, false);
    }

    static boolean applicationWidgetIconShowBorder(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_ICON_SHOW_BORDER, false);
    }

    static boolean applicationWidgetListCustomIconLightness(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_LIST_CUSTOM_ICON_LIGHTNESS, false);
    }

    static boolean applicationWidgetOneRowCustomIconLightness(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_ONE_ROW_CUSTOM_ICON_LIGHTNESS, false);
    }

    static boolean applicationWidgetIconCustomIconLightness(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_ICON_CUSTOM_ICON_LIGHTNESS, false);
    }

    static boolean applicationSamsungEdgeCustomIconLightness(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_SAMSUNG_EDGE_CUSTOM_ICON_LIGHTNESS, false);
    }

    /*
    static boolean notificationDarkBackground(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_NOTIFICATION_DARK_BACKGROUND, false);
    }
    */

    static boolean notificationUseDecoration(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_NOTIFICATION_USE_DECORATION, true);
    }

    static String notificationLayoutType(Context context) {
        return getSharedPreferences(context).getString(PREF_NOTIFICATION_LAYOUT_TYPE, "0");
    }

    static String notificationBackgroundColor(Context context) {
        return getSharedPreferences(context).getString(PREF_NOTIFICATION_BACKGROUND_COLOR, "0");
    }

    static String applicationNightModeOffTheme(Context context) {
        return getSharedPreferences(context).getString(PREF_APPLICATION_NIGHT_MODE_OFF_THEME, "color");
    }

}
