package sk.henrichg.phoneprofiles;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.thelittlefireman.appkillermanager.managers.KillerManager;

public class ImportantInfoActivity extends AppCompatActivity {

    @SuppressLint("InlinedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // must by called before super.onCreate() for PreferenceActivity
        GlobalGUIRoutines.setTheme(this, false, false); // must by called before super.onCreate()
        GlobalGUIRoutines.setLanguage(getBaseContext());

        super.onCreate(savedInstanceState);

        final Activity activity = this;

        setContentView(R.layout.activity_important_info);

        /*
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Window w = getWindow(); // in Activity's onCreate() for instance
            //w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            // create our manager instance after the content view is set
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            // enable status bar tint
            tintManager.setStatusBarTintEnabled(true);
            // set a custom tint color for status bar
            switch (ApplicationPreferences.applicationTheme(getApplicationContext(), true)) {
                case "color":
                    tintManager.setStatusBarTintColor(ContextCompat.getColor(getBaseContext(), R.color.primary));
                    break;
                case "white":
                    tintManager.setStatusBarTintColor(ContextCompat.getColor(getBaseContext(), R.color.primaryDark19_white));
                    break;
                default:
                    tintManager.setStatusBarTintColor(ContextCompat.getColor(getBaseContext(), R.color.primary_dark));
                    break;
            }
        }
        */

        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.info_notification_title);
            getSupportActionBar().setElevation(GlobalGUIRoutines.dpToPx(1));
        }

        int versionCode = 0;
        Context context = getApplicationContext();
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            versionCode = PPApplication.getVersionCode(pInfo);
            PPApplication.logE("ImportantInfoHelpFragment.onViewCreated", "versionCode="+versionCode);
        } catch (Exception ignored) {
        }

        boolean news = false;
        boolean newsLatest = (versionCode >= ImportantInfoNotification.VERSION_CODE_FOR_NEWS);
        PPApplication.logE("ImportantInfoHelpFragment.onViewCreated", "newsLatest="+newsLatest);
        boolean news2755 = ((versionCode >= 2755) && (versionCode < ImportantInfoNotification.VERSION_CODE_FOR_NEWS));
        PPApplication.logE("ImportantInfoHelpFragment.onViewCreated", "news2690="+news2755);
        boolean news2690 = ((versionCode >= 2690) && (versionCode < ImportantInfoNotification.VERSION_CODE_FOR_NEWS));
        PPApplication.logE("ImportantInfoHelpFragment.onViewCreated", "news2690="+news2690);
        boolean news1634 = ((versionCode >= 1634) && (versionCode < ImportantInfoNotification.VERSION_CODE_FOR_NEWS));
        PPApplication.logE("ImportantInfoHelpFragment.onViewCreated", "news1634="+news1634);
        boolean news1622 = ((versionCode >= 1622) && (versionCode < ImportantInfoNotification.VERSION_CODE_FOR_NEWS));
        PPApplication.logE("ImportantInfoHelpFragment.onViewCreated", "news1622="+news1622);

        //noinspection StatementWithEmptyBody
        if (newsLatest) {
            // move this to newXXX, for switch off news
        }
        else {
            // move this to newXXX, for switch off news
        }

        if (news2755) {
            news = true;
            TextView infoText1 = findViewById(R.id.activity_info_notification_privacy_policy_backup_files_2_news);
            infoText1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url = "https://sites.google.com/site/phoneprofiles/home/privacy-policy";
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    try {
                        startActivity(Intent.createChooser(i, getString(R.string.web_browser_chooser)));
                    } catch (Exception ignored) {}
                }
            });
        }
        else {
            TextView infoText1 = findViewById(R.id.activity_info_notification_privacy_policy_backup_files_news);
            infoText1.setVisibility(View.GONE);
            infoText1 = findViewById(R.id.activity_info_notification_privacy_policy_backup_files_2_news);
            infoText1.setVisibility(View.GONE);
        }

        if (news2690) {
            news = true;
        }
        else {
            // empty this, for switch off news
            TextView infoText1 = findViewById(R.id.activity_info_notification_profile_grant_news);
            infoText1.setVisibility(View.GONE);
            TextView infoText2 = findViewById(R.id.activity_info_notification_profile_grant_lookSectionProfiles_news);
            infoText2.setVisibility(View.GONE);
        }

        if (news1634) {
            if (android.os.Build.VERSION.SDK_INT >= 23) {
                TextView infoText16 = findViewById(R.id.activity_info_notification_profile_ringerMode_root);
                infoText16.setVisibility(View.GONE);
                TextView infoText18 = findViewById(R.id.activity_info_notification_profile_adaptiveBrightness_root);
                infoText18.setVisibility(View.GONE);
                news = true;
            }
        }
        else {
            TextView infoText15 = findViewById(R.id.activity_info_notification_profile_ringerMode_root_news);
            infoText15.setVisibility(View.GONE);
            TextView infoText17 = findViewById(R.id.activity_info_notification_profile_adaptiveBrightness_root_news);
            infoText17.setVisibility(View.GONE);
            TextView infoText10a = findViewById(R.id.activity_info_notification_app_standby);
            infoText10a.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS, getApplicationContext())) {
                        Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                        //intent.addCategory(Intent.CATEGORY_DEFAULT);
                        startActivity(intent);
                    }
                    else {
                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
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
                        if (!activity.isFinishing())
                            dialog.show();
                    }
                }
            });
        }

        if (news1622) {
            //if (android.os.Build.VERSION.SDK_INT >= 21) {
                TextView infoText14 = findViewById(R.id.activity_info_notification_profile_zenMode);
                infoText14.setVisibility(View.GONE);

                boolean a60 = (android.os.Build.VERSION.SDK_INT == 23) && Build.VERSION.RELEASE.equals("6.0");
                if ((android.os.Build.VERSION.SDK_INT >= 23) && (!a60) &&
                        GlobalGUIRoutines.activityActionExists(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS, context)) {
                    TextView infoText13 = findViewById(R.id.activity_info_notification_profile_zenMode_news);
                    infoText13.setVisibility(View.GONE);
                }
                else {
                    TextView infoText13 = findViewById(R.id.activity_info_notification_profile_zenMode_news);
                    if (android.os.Build.VERSION.SDK_INT >= 23)
                        infoText13.setText(R.string.important_info_profile_zenModeM);
                    infoText13.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (GlobalGUIRoutines.activityActionExists("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS", getApplicationContext())) {
                                Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                                startActivity(intent);
                            }
                            else {
                                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
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
                                if (!activity.isFinishing())
                                    dialog.show();
                            }
                        }
                    });
                    news = true;
                }
            //}
        }
        else {
            TextView infoText13 = findViewById(R.id.activity_info_notification_profile_zenMode_news);
            infoText13.setVisibility(View.GONE);

            boolean a60 = (android.os.Build.VERSION.SDK_INT == 23) && Build.VERSION.RELEASE.equals("6.0");
            if ((android.os.Build.VERSION.SDK_INT >= 23) && (!a60) &&
                    GlobalGUIRoutines.activityActionExists(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS, context)) {
                TextView infoText14 = findViewById(R.id.activity_info_notification_profile_zenMode);
                infoText14.setVisibility(View.GONE);
            }
            else {
                TextView infoText14 = findViewById(R.id.activity_info_notification_profile_zenMode);
                if (android.os.Build.VERSION.SDK_INT >= 23)
                    infoText14.setText(R.string.important_info_profile_zenModeM);
                infoText14.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (GlobalGUIRoutines.activityActionExists("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS", getApplicationContext())) {
                            Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                            startActivity(intent);
                        }
                        else {
                            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
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
                            if (!activity.isFinishing())
                                dialog.show();
                        }
                    }
                });
            }
        }

        if (android.os.Build.VERSION.SDK_INT < 23) {
            TextView infoText15 = findViewById(R.id.activity_info_notification_profile_ringerMode_root_news);
            infoText15.setVisibility(View.GONE);
            TextView infoText16 = findViewById(R.id.activity_info_notification_profile_ringerMode_root);
            infoText16.setVisibility(View.GONE);
            TextView infoText17 = findViewById(R.id.activity_info_notification_profile_adaptiveBrightness_root_news);
            infoText17.setVisibility(View.GONE);
            TextView infoText18 = findViewById(R.id.activity_info_notification_profile_adaptiveBrightness_root);
            infoText18.setVisibility(View.GONE);
            TextView infoText10a = findViewById(R.id.activity_info_notification_app_standby);
            infoText10a.setVisibility(View.GONE);
        }

        /*
        if (android.os.Build.VERSION.SDK_INT < 21) {
            TextView infoText13 = findViewById(R.id.activity_info_notification_profile_zenMode_news);
            infoText13.setVisibility(View.GONE);
            TextView infoText14 = findViewById(R.id.activity_info_notification_profile_zenMode);
            infoText14.setVisibility(View.GONE);
        }
        */

        if (ActivateProfileHelper.getMergedRingNotificationVolumes(context)) {
            TextView infoText3 = findViewById(R.id.activity_info_notification_unlink_ringer_notification_volumes);
            infoText3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getBaseContext(), PhoneProfilesPreferencesActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(PhoneProfilesPreferencesActivity.EXTRA_SCROLL_TO, "categorySystem");
                    startActivity(intent);
                }
            });
        }
        else {
            TextView infoText3 = findViewById(R.id.activity_info_notification_unlink_ringer_notification_volumes);
            infoText3.setVisibility(View.GONE);
        }

        if (KillerManager.isActionAvailable(getBaseContext(), KillerManager.Actions.ACTION_POWERSAVING)) {
            TextView infoText = findViewById(R.id.activity_info_notification_power_manager);
            infoText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        KillerManager.doActionPowerSaving(getBaseContext());
                    }catch (Exception e) {
                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
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
                        if (!activity.isFinishing())
                            dialog.show();
                    }
                }
            });
            /* currently not implemented in KillerManager
            if (!(device instanceof Sony)) {
                infoText = findViewById(R.id.activity_info_notification_sony_stamina_mode);
                infoText.setVisibility(View.GONE);
            }*/
            if (!Build.MANUFACTURER.equalsIgnoreCase("sony")) {
                infoText = findViewById(R.id.activity_info_notification_sony_stamina_mode);
                infoText.setVisibility(View.GONE);
            }
        }
        else {
            TextView infoText = findViewById(R.id.activity_info_notification_power_manager);
            infoText.setVisibility(View.GONE);
            if (!Build.MANUFACTURER.equalsIgnoreCase("sony")) {
                infoText = findViewById(R.id.activity_info_notification_sony_stamina_mode);
                infoText.setVisibility(View.GONE);
            }
        }

        TextView infoText41 = findViewById(R.id.activity_info_activate_profile_from_tasker_params);
        String str = "Send Intent [\n" +
                " Extra:profile_name:profile name\n" +
                " Package:sk.henrichg.phoneprofiles\n" +
                " Class:sk.henrichg.phoneprofiles.ActivateProfileFromExternalApplicationActivity\n" +
                " Target:Activity\n" +
                "]";
        Spannable spannable = new SpannableString(str);
        spannable.setSpan(new BackgroundColorSpan(GlobalGUIRoutines.getThemeCommandBackgroundColor(this)), 0, str.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        infoText41.setText(spannable);

        TextView infoTextADBDownload = findViewById(R.id.activity_info_notification_profile_grant_1_howTo_11);
        str = getString(R.string.important_info_profile_grant_1_howTo_11);
        spannable = new SpannableString(str);
        spannable.setSpan(new BackgroundColorSpan(GlobalGUIRoutines.getThemeCommandBackgroundColor(this)), 0, str.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        infoTextADBDownload.setText(spannable);

        TextView infoTextGrant1Command = findViewById(R.id.activity_info_notification_dialog_info_grant_1_command);
        str = "adb\u00A0shell\u00A0pm\u00A0grant\u00A0"+context.getPackageName()+"\u00A0" +
                "android.permission.WRITE_SECURE_SETTINGS";
        spannable = new SpannableString(str);
        spannable.setSpan(new BackgroundColorSpan(GlobalGUIRoutines.getThemeCommandBackgroundColor(this)), 0, str.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        infoTextGrant1Command.setText(spannable);

        AboutApplicationActivity.emailMe((TextView) findViewById(R.id.activity_info_notification_contact),
                getString(R.string.important_info_contact),
                "", getString(R.string.about_application_support_subject),
                AboutApplicationActivity.getEmailBodyText(AboutApplicationActivity.EMAIL_BODY_SUPPORT, activity),
                true, activity);
        AboutApplicationActivity.emailMe((TextView) findViewById(R.id.activity_info_translations),
                getString(R.string.important_info_translations),
                getString(R.string.about_application_translations2),
                getString(R.string.about_application_translations_subject),
                AboutApplicationActivity.getEmailBodyText(AboutApplicationActivity.EMAIL_BODY_TRANSLATIONS, activity),
                true,activity);

        int extenderVersion = PPPExtenderBroadcastReceiver.isExtenderInstalled(context);

        if ((extenderVersion != 0) && (extenderVersion < PPApplication.VERSION_CODE_EXTENDER_LATEST)) {
            news = true;
            TextView infoText1 = findViewById(R.id.activity_info_notification_accessibility_service_new_version);
            infoText1.setVisibility(View.VISIBLE);
            infoText1 = findViewById(R.id.activity_info_notification_accessibility_service_new_version_2);
            infoText1.setVisibility(View.VISIBLE);
            infoText1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url = "https://github.com/henrichg/PhoneProfilesPlusExtender/releases";
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    try {
                        startActivity(Intent.createChooser(i, getString(R.string.web_browser_chooser)));
                    } catch (Exception ignored) {}
                }
            });
        }
        else {
            TextView infoText1 = findViewById(R.id.activity_info_notification_accessibility_service_new_version);
            infoText1.setVisibility(View.GONE);
            infoText1 = findViewById(R.id.activity_info_notification_accessibility_service_new_version_2);
            infoText1.setVisibility(View.GONE);
        }

        if (!news) {
            TextView infoTextNews = findViewById(R.id.activity_info_notification_news);
            infoTextNews.setVisibility(View.GONE);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

}
