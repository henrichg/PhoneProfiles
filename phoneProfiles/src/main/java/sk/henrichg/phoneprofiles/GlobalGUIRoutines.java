package sk.henrichg.phoneprofiles;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.BulletSpan;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.TypedValue;

import org.xml.sax.XMLReader;

import java.text.Collator;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import androidx.core.content.ContextCompat;
import androidx.preference.Preference;
import mobi.upod.timedurationpicker.TimeDurationPicker;

class GlobalGUIRoutines {

    static Collator collator = null;

    // import/export
    static final String DB_FILEPATH = "/data/" + PPApplication.PACKAGE_NAME + "/databases";
    static final String REMOTE_EXPORT_PATH = "/PhoneProfilesPlus";
    static final String EXPORT_APP_PREF_FILENAME = "ApplicationPreferences.backup";
    static final String EXPORT_DEF_PROFILE_PREF_FILENAME = "DefaultProfilePreferences.backup";

    // https://stackoverflow.com/questions/40221711/android-context-getresources-updateconfiguration-deprecated
    // but my solution working also in Android 8.1
    public static void setLanguage(Context context)//, boolean restart)
    {
        //if (android.os.Build.VERSION.SDK_INT < 24) {

            String lang = ApplicationPreferences.applicationLanguage(context);

            Locale appLocale;

            if (!lang.equals("system")) {
                String[] langSplit = lang.split("-");
                if (langSplit.length == 1)
                    appLocale = new Locale(lang);
                else
                    appLocale = new Locale(langSplit[0], langSplit[1]);
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                    appLocale = Resources.getSystem().getConfiguration().getLocales().get(0);
                else
                    appLocale = Resources.getSystem().getConfiguration().locale;
            }

            Locale.setDefault(appLocale);
            Configuration appConfig = new Configuration();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                appConfig.setLocale(appLocale);
            else
                appConfig.locale = appLocale;

            //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            //    Context context  = context.createConfigurationContext(appConfig);
            //else
            context.getResources().updateConfiguration(appConfig, context.getResources().getDisplayMetrics());
        //}

        // collator for application locale sorting
        collator = getCollator(context);

        PPApplication.createNotificationChannels(context);
    }

    private static Collator getCollator(Context context)
    {
        //if (android.os.Build.VERSION.SDK_INT < 24) {
            // get application Locale
            String lang = ApplicationPreferences.applicationLanguage(context);
            Locale appLocale;
            if (!lang.equals("system")) {
                String[] langSplit = lang.split("-");
                if (langSplit.length == 1)
                    appLocale = new Locale(lang);
                else
                    appLocale = new Locale(langSplit[0], langSplit[1]);
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                    appLocale = Resources.getSystem().getConfiguration().getLocales().get(0);
                else
                    appLocale = Resources.getSystem().getConfiguration().locale;
            }
            // get collator for application locale
            return Collator.getInstance(appLocale);
        /*}
        else {
            //Log.d("GlobalGUIRoutines.getCollator", java.util.Locale.getDefault().toString());
            return Collator.getInstance();
        }*/
    }

    public static void setTheme(Activity activity, boolean forPopup, boolean withToolbar)
    {
        int theme = getTheme(forPopup, withToolbar, activity);
        if (theme != 0)
            activity.setTheme(theme);
    }

    private static int getTheme(boolean forPopup, boolean withToolbar, Context context) {
        switch (ApplicationPreferences.applicationTheme(context, true)) {
            /*case "color":
                if (forPopup)
                {
                    if (withToolbar)
                        return R.style.PopupTheme_withToolbar_color;
                    else
                        return R.style.PopupTheme_color;
                }
                else
                {
                    if (withToolbar)
                        return R.style.Theme_PhoneProfilesTheme_withToolbar_color;
                    else
                        return R.style.Theme_PhoneProfilesTheme_color;
                }*/
            case "dark":
                if (forPopup)
                {
                    if (withToolbar)
                        return R.style.PopupTheme_withToolbar_dark;
                    else
                        return R.style.PopupTheme_dark;
                }
                else
                {
                    if (withToolbar)
                        return R.style.Theme_PhoneProfilesTheme_withToolbar_dark;
                    else
                        return R.style.Theme_PhoneProfilesTheme_dark;
                }
            /*case "dlight":
                if (forPopup)
                {
                    if (withToolbar)
                        return R.style.PopupTheme_withToolbar_dlight;
                    else
                        return R.style.PopupTheme_dlight;
                }
                else
                {
                    if (withToolbar)
                        return R.style.Theme_PhoneProfilesTheme_withToolbar_dlight;
                    else
                        return R.style.Theme_PhoneProfilesTheme_dlight;
                }*/
            case "white":
            default:
                if (forPopup)
                {
                    if (withToolbar)
                        return R.style.PopupTheme_withToolbar_white;
                    else
                        return R.style.PopupTheme_white;
                }
                else
                {
                    if (withToolbar)
                        return R.style.Theme_PhoneProfilesTheme_withToolbar_white;
                    else
                        return R.style.Theme_PhoneProfilesTheme_white;
                }
                /*if (forPopup)
                {
                    if (withToolbar)
                        return R.style.PopupTheme_withToolbar_color;
                    else
                        return R.style.PopupTheme_color;
                }
                else
                {
                    if (withToolbar)
                        return R.style.Theme_PhoneProfilesTheme_withToolbar_color;
                    else
                        return R.style.Theme_PhoneProfilesTheme_color;
                }*/
        }
    }

    static void reloadActivity(Activity activity, @SuppressWarnings("SameParameterValue") boolean newIntent)
    {
        if (activity == null)
            return;

        if (newIntent)
        {

            final Activity _activity = activity;
            new Handler(activity.getMainLooper()).post(new Runnable() {

                @Override
                public void run()
                {
                    try {
                        Intent intent = _activity.getIntent();
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        _activity.finish();
                        _activity.overridePendingTransition(0, 0);

                        _activity.startActivity(intent);
                        _activity.overridePendingTransition(0, 0);
                    } catch (Exception ignore) {}
                }
            });
        }
        else
            activity.recreate();
    }

    static void setPreferenceTitleStyleX(Preference preference,
                                         boolean enabled, boolean bold, boolean addBullet,
                                         @SuppressWarnings("SameParameterValue") boolean underline,
                                         boolean errorColor, boolean systemSettings)
    {
        if (preference != null) {
            CharSequence title = preference.getTitle();
            if (systemSettings) {
                String s = title.toString();
                if (!s.contains("(S)")) {
                    if (bold && addBullet)
                        title = TextUtils.concat("• (S) ", title);
                    else
                        title = TextUtils.concat("(S) ", title);
                }
            }
            if (addBullet) {
                if (bold) {
                    String s = title.toString();
                    if (!s.startsWith("• "))
                        title = TextUtils.concat("• ", title);
                } else {
                    String s = title.toString();
                    if (s.startsWith("• "))
                        title = TextUtils.replace(title, new String[]{"• "}, new CharSequence[]{""});
                }
            }
            Spannable sbt = new SpannableString(title);
            Object[] spansToRemove = sbt.getSpans(0, title.length(), Object.class);
            for (Object span : spansToRemove) {
                if (span instanceof CharacterStyle)
                    sbt.removeSpan(span);
            }
            if (bold || underline) {
                if (bold) {
                    sbt.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, sbt.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    //sbt.setSpan(new RelativeSizeSpan(1.05f), 0, sbt.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                if (underline) {
                    if (bold && addBullet)
                        sbt.setSpan(new UnderlineSpan(), 2, sbt.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    else
                        sbt.setSpan(new UnderlineSpan(), 0, sbt.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                if (errorColor && enabled)
                    sbt.setSpan(new ForegroundColorSpan(Color.RED), 0, sbt.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                preference.setTitle(sbt);
            } else {
                preference.setTitle(sbt);
            }
        }
    }

    /*
    private static float pixelsToSp(Context context, float px) {
        return px / context.getResources().getDisplayMetrics().scaledDensity;
    }
    */

    /*
    private static float spToPixels(Context context, float sp) {
        return sp * context.getResources().getDisplayMetrics().scaledDensity;
    }
    */

    @SuppressWarnings("SameParameterValue")
    static int dpToPx(int dp)
    {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    /*static int pxToDp(int px)
    {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    } */

    /**
     * Uses reflection to access divider private attribute and override its color
     * Use Color.Transparent if you wish to hide them
     */
    /*static void setSeparatorColorForNumberPicker(NumberPicker picker, int separatorColor) {
        Field[] pickerFields = NumberPicker.class.getDeclaredFields();
        for (Field pf : pickerFields) {
            if (pf.getName().equals("mSelectionDivider")) {
                pf.setAccessible(true);
                try {
                    pf.set(picker, new ColorDrawable(separatorColor));
                } catch (IllegalAccessException | IllegalArgumentException ignored) {
                }
                break;
            }
        }
    }*/

    /*static void updateTextAttributesForNumberPicker(NumberPicker picker, int textSizeSP) {
        for (int i = 0; i < picker.getChildCount(); i++){
            View child = picker.getChildAt(i);
            if (child instanceof EditText) {
                try {
                    Field selectorWheelPaintField = NumberPicker.class.getDeclaredField("mSelectorWheelPaint");
                    selectorWheelPaintField.setAccessible(true);

                    Paint wheelPaint = ((Paint)selectorWheelPaintField.get(picker));
                    //wheelPaint.setColor(textColor);
                    wheelPaint.setTextSize(spToPixels(picker.getContext(), textSizeSP));

                    EditText editText = ((EditText) child);
                    //editText.setTextColor(textColor);
                    editText.setTextSize(textSizeSP);

                    picker.invalidate();
                    break;
                }
                catch (NoSuchFieldException | IllegalAccessException | IllegalArgumentException ignored) {
                }
            }
        }
    }*/

    static Spanned fromHtml(String source, @SuppressWarnings("SameParameterValue") boolean forBullets) {
        Spanned htmlSpanned;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            htmlSpanned =  Html.fromHtml(source, Html.FROM_HTML_MODE_COMPACT);
        } else {
            if (forBullets)
                htmlSpanned = Html.fromHtml(source, null, new LiTagHandler());
            else
                htmlSpanned = Html.fromHtml(source);
        }

        if (forBullets)
            return addBullets(htmlSpanned);
        else
            return  htmlSpanned;

    }

    private static int dip(int dp) {
        return (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, Resources.getSystem().getDisplayMetrics()));
    }

    private static SpannableStringBuilder addBullets(Spanned htmlSpanned) {
        SpannableStringBuilder spannableBuilder = new SpannableStringBuilder(htmlSpanned);
        BulletSpan[] spans = spannableBuilder.getSpans(0, spannableBuilder.length(), BulletSpan.class);
        if (spans != null) {
            for (BulletSpan span : spans) {
                int start = spannableBuilder.getSpanStart(span);
                int end  = spannableBuilder.getSpanEnd(span);
                spannableBuilder.removeSpan(span);
                spannableBuilder.setSpan(new ImprovedBulletSpan(dip(2), dip(8), 0), start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            }
        }
        return spannableBuilder;
    }

    @SuppressLint("DefaultLocale")
    static String getDurationString(int duration) {
        int hours = duration / 3600;
        int minutes = (duration % 3600) / 60;
        int seconds = duration % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    static String getListPreferenceString(String value, int arrayValuesRes,
                                          int arrayStringsRes, Context context) {
        String[] arrayValues = context.getResources().getStringArray(arrayValuesRes);
        String[] arrayStrings = context.getResources().getStringArray(arrayStringsRes);
        int index = 0;
        for (String arrayValue : arrayValues) {
            if (arrayValue.equals(value))
                break;
            ++index;
        }
        try {
            return arrayStrings[index];
        } catch (Exception e) {
            return context.getString(R.string.array_pref_no_change);
        }
    }

    static String getZenModePreferenceString(String value, Context context) {
        String[] arrayValues = context.getResources().getStringArray(R.array.zenModeValues);
        String[] arrayStrings = context.getResources().getStringArray(R.array.zenModeArray);
        String[] arraySummaryStrings = context.getResources().getStringArray(R.array.zenModeSummaryArray);
        int index = 0;
        for (String arrayValue : arrayValues) {
            if (arrayValue.equals(value))
                break;
            ++index;
        }
        try {
            return arrayStrings[index] + " - " + arraySummaryStrings[Integer.valueOf(value) - 1];
        } catch (Exception e) {
            return context.getString(R.string.array_pref_no_change);
        }
    }

    static void setRingtonePreferenceSummary(final String initSummary, final String ringtoneUri,
                                             final androidx.preference.Preference preference, final Context context) {
        new AsyncTask<Void, Integer, Void>() {

            private String ringtoneName;

            @Override
            protected Void doInBackground(Void... params) {
                if ((ringtoneUri == null) || ringtoneUri.isEmpty())
                    ringtoneName = context.getString(R.string.ringtone_preference_none);
                else {
                    Uri uri = Uri.parse(ringtoneUri);
                    Ringtone ringtone = RingtoneManager.getRingtone(context, uri);
                    try {
                        ringtoneName = ringtone.getTitle(context);
                    } catch (Exception e) {
                        ringtoneName = context.getString(R.string.ringtone_preference_not_set);
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                preference.setSummary(TextUtils.replace(initSummary, new String[]{"<ringtone_name>"}, new String[]{ringtoneName}));
            }

        }.execute();
    }

    static void setProfileSoundsPreferenceSummary(final String initSummary,
                                                  final String ringtoneUri, final String notificationUri, final String alarmUri,
                                                  final androidx.preference.Preference preference, final Context context) {
        new AsyncTask<Void, Integer, Void>() {

            private String ringtoneName;
            private String notificationName;
            private String alarmName;

            @Override
            protected Void doInBackground(Void... params) {
                if ((ringtoneUri == null) || ringtoneUri.isEmpty())
                    ringtoneName = context.getString(R.string.ringtone_preference_none);
                else {
                    String[] splits = ringtoneUri.split("\\|");
                    Uri uri = Uri.parse(splits[0]);
                    Ringtone ringtone = RingtoneManager.getRingtone(context, uri);
                    try {
                        ringtoneName = ringtone.getTitle(context);
                    } catch (Exception e) {
                        ringtoneName = context.getString(R.string.ringtone_preference_not_set);
                    }
                }

                if ((notificationUri == null) || notificationUri.isEmpty())
                    notificationName = context.getString(R.string.ringtone_preference_none);
                else {
                    String[] splits = notificationUri.split("\\|");
                    Uri uri = Uri.parse(splits[0]);
                    Ringtone ringtone = RingtoneManager.getRingtone(context, uri);
                    try {
                        notificationName = ringtone.getTitle(context);
                    } catch (Exception e) {
                        notificationName = context.getString(R.string.ringtone_preference_not_set);
                    }
                }

                if ((alarmUri == null) || alarmUri.isEmpty())
                    alarmName = context.getString(R.string.ringtone_preference_none);
                else {
                    String[] splits = alarmUri.split("\\|");
                    Uri uri = Uri.parse(splits[0]);
                    Ringtone ringtone = RingtoneManager.getRingtone(context, uri);
                    try {
                        alarmName = ringtone.getTitle(context);
                    } catch (Exception e) {
                        alarmName = context.getString(R.string.ringtone_preference_not_set);
                    }
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                preference.setSummary(TextUtils.replace(initSummary,
                        new String[]{"<ringtone_name>", "<notification_name>", "<alarm_name>"},
                        new String[]{ringtoneName, notificationName, alarmName}));
            }

        }.execute();
    }

    @SuppressLint("DefaultLocale")
    static String getEndsAtString(int duration) {
        if(duration == 0) {
            return "--";
        }

        Calendar ends = Calendar.getInstance();
        ends.add(Calendar.SECOND, duration);
        int hours = ends.get(Calendar.HOUR_OF_DAY);
        int minutes = ends.get(Calendar.MINUTE);
        int seconds = ends.get(Calendar.SECOND);
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    static int getThemeAccentColor (final Context context) {
        final TypedValue value = new TypedValue ();
        context.getTheme ().resolveAttribute (R.attr.colorAccent, value, true);
        return value.data;
    }

    static int getThemeTextColor (final Context context) {
        final TypedValue value = new TypedValue ();
        context.getTheme ().resolveAttribute (R.attr.activityTextColor, value, true);
        return value.data;
    }

    static int getThemeCommandBackgroundColor (final Context context) {
        final TypedValue value = new TypedValue ();
        context.getTheme ().resolveAttribute (R.attr.activityCommandBackgroundColor, value, true);
        return value.data;
    }

    static int getThemeColorControlHighlight (final Context context) {
        final TypedValue value = new TypedValue ();
        context.getTheme ().resolveAttribute (R.attr.colorControlHighlight, value, true);
        return value.data;
    }

    /*
    static int getThemeActivatorGridDividerColor(final boolean show, final Context context) {
        final TypedValue value = new TypedValue();
        if (show)
            context.getTheme().resolveAttribute(android.R.attr.listDivider, value, false);
        else
            context.getTheme().resolveAttribute(R.attr.activityBackgroundColor, value, false);
        return value.data;
    }
    */

    static void setThemeTimeDurationPickerDisplay(TimeDurationPicker timeDurationPicker, final Activity activity) {
        if (ApplicationPreferences.applicationTheme(activity, true).equals("white")) {
            timeDurationPicker.setDisplayTextAppearance(R.style.TextAppearance_TimeDurationPicker_Display);
            timeDurationPicker.setUnitTextAppearance(R.style.TextAppearance_TimeDurationPicker_Unit);
            timeDurationPicker.setBackspaceIcon(ContextCompat.getDrawable(activity, R.drawable.ic_backspace_light_tdp));
            timeDurationPicker.setClearIcon(ContextCompat.getDrawable(activity, R.drawable.ic_clear_light_tdp));
        }
        else {
            timeDurationPicker.setDisplayTextAppearance(R.style.TextAppearance_TimeDurationPicker_Display_Dark);
            timeDurationPicker.setUnitTextAppearance(R.style.TextAppearance_TimeDurationPicker_Unit_Dark);
            timeDurationPicker.setBackspaceIcon(ContextCompat.getDrawable(activity, R.drawable.ic_backspace_tdp));
            timeDurationPicker.setClearIcon(ContextCompat.getDrawable(activity, R.drawable.ic_clear_tdp));
        }
        timeDurationPicker.setDurationDisplayBackgroundColor(Color.TRANSPARENT);
        //timeDurationPicker.setSeparatorColor(GlobalGUIRoutines.getThemeDialogDividerColor(activity));
    }

    /*
    static int getResourceId(String pVariableName, String pResourceName, Context context)
    {
        try {
            return context.getResources().getIdentifier(pVariableName, pResourceName, context.getPackageName());
        } catch (Exception e) {
            return -1;
        }
    }
    */

    static boolean activityActionExists(String action, Context context) {
        try {
            final Intent intent = new Intent(action);
            List<ResolveInfo> activities = context.getApplicationContext().getPackageManager().queryIntentActivities(intent, 0);
            return activities.size() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    static boolean activityIntentExists(Intent intent, Context context) {
        try {
            List<ResolveInfo> activities = context.getApplicationContext().getPackageManager().queryIntentActivities(intent, 0);
            return activities.size() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    static void lockScreenOrientation(Activity activity) {
        try {
            int currentOrientation = activity.getResources().getConfiguration().orientation;
            if (currentOrientation == Configuration.ORIENTATION_PORTRAIT) {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
            } else {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            }
            // FC in tablets: java.lang.IllegalStateException: Only fullscreen activities can request orientation
        } catch (Exception ignored) {}
    }

    static void unlockScreenOrientation(Activity activity) {
        try {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
            // FC in tablets: java.lang.IllegalStateException: Only fullscreen activities can request orientation
        } catch (Exception ignored) {}
    }

    /*
    private static Point getRealScreenSize(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager != null) {
            Display display = windowManager.getDefaultDisplay();
            Point size = new Point();

            //if (Build.VERSION.SDK_INT >= 17) {
            display.getRealSize(size);
            //} else {
            //    try {
            //        size.x = (Integer) Display.class.getMethod("getRawWidth").invoke(display);
            //        size.y = (Integer) Display.class.getMethod("getRawHeight").invoke(display);
            //    } catch (Exception ignored) {
            //    }
            //}

            return size;
        }
        else
            return null;
    }
    */

    /*
    private static Point getAppUsableScreenSize(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager != null) {
            Display display = windowManager.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            return size;
        }
        else
            return null;
    }
    */

    static class LiTagHandler implements Html.TagHandler {

        @Override
        public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {

            class Bullet {}

            if (tag.equals("li") && opening) {
                output.setSpan(new Bullet(), output.length(), output.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            }
            if (tag.equals("li") && !opening) {
                //output.append("\n\n");
                output.append("\n");
                Bullet[] spans = output.getSpans(0, output.length(), Bullet.class);
                if (spans != null) {
                    Bullet lastMark = spans[spans.length-1];
                    int start = output.getSpanStart(lastMark);
                    output.removeSpan(lastMark);
                    if (start != output.length()) {
                        output.setSpan(new BulletSpan(), start, output.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    }
                }
            }
        }

    }

}
