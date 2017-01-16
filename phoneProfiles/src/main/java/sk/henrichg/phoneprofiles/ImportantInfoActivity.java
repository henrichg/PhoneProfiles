package sk.henrichg.phoneprofiles;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.readystatesoftware.systembartint.SystemBarTintManager;

public class ImportantInfoActivity extends AppCompatActivity {

    @SuppressLint("InlinedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        PPApplication.loadPreferences(getApplicationContext());

        // must by called before super.onCreate() for PreferenceActivity
        GlobalGUIRoutines.setTheme(this, false, false); // must by called before super.onCreate()
        GlobalGUIRoutines.setLanguage(getBaseContext());

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_important_info);

        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) && (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)) {
            Window w = getWindow(); // in Activity's onCreate() for instance
            //w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            // create our manager instance after the content view is set
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            // enable status bar tint
            tintManager.setStatusBarTintEnabled(true);
            // set a custom tint color for status bar
            if (PPApplication.applicationTheme.equals("material"))
                tintManager.setStatusBarTintColor(Color.parseColor("#ff237e9f"));
            else
                tintManager.setStatusBarTintColor(Color.parseColor("#ff202020"));
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.info_notification_title);
        }

        int versionCode = 0;
        Context context = getApplicationContext();
        try {
            PackageInfo pinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            versionCode = pinfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            //e.printStackTrace();
        }

        boolean news = false;
        boolean newsLatest = (versionCode >= ImportantInfoNotification.VERSION_CODE_FOR_NEWS);
        boolean news1634 = ((versionCode >= 1634) && (versionCode < ImportantInfoNotification.VERSION_CODE_FOR_NEWS));
        boolean news1622 = ((versionCode >= 1622) && (versionCode < ImportantInfoNotification.VERSION_CODE_FOR_NEWS));

        if (newsLatest) {
                // no news
        }

        if (news1634) {
            if (android.os.Build.VERSION.SDK_INT >= 23) {
                TextView infoText16 = (TextView) findViewById(R.id.activity_info_notification_dialog_info_text16);
                infoText16.setVisibility(View.GONE);
                TextView infoText18 = (TextView) findViewById(R.id.activity_info_notification_dialog_info_text18);
                infoText18.setVisibility(View.GONE);
                news = true;
            }
        }
        else {
            TextView infoText15 = (TextView) findViewById(R.id.activity_info_notification_dialog_info_text15);
            infoText15.setVisibility(View.GONE);
            TextView infoText17 = (TextView) findViewById(R.id.activity_info_notification_dialog_info_text17);
            infoText17.setVisibility(View.GONE);
            TextView infoText10a = (TextView) findViewById(R.id.activity_info_notification_dialog_info_text10a);
            infoText10a.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    startActivity(intent);
                }
            });
        }

        if (news1622) {
            if (android.os.Build.VERSION.SDK_INT >= 21) {
                TextView infoText14 = (TextView) findViewById(R.id.activity_info_notification_dialog_info_text14);
                infoText14.setVisibility(View.GONE);

                boolean a60 = (android.os.Build.VERSION.SDK_INT == 23) && Build.VERSION.RELEASE.equals("6.0");
                if ((android.os.Build.VERSION.SDK_INT >= 23) && (!a60)) {
                    TextView infoText13 = (TextView) findViewById(R.id.activity_info_notification_dialog_info_text13);
                    infoText13.setVisibility(View.GONE);
                }
                else {
                    TextView infoText13 = (TextView) findViewById(R.id.activity_info_notification_dialog_info_text13);
                    infoText13.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                            startActivity(intent);
                        }
                    });
                    news = true;
                }
            }
        }
        else {
            TextView infoText13 = (TextView) findViewById(R.id.activity_info_notification_dialog_info_text13);
            infoText13.setVisibility(View.GONE);

            boolean a60 = (android.os.Build.VERSION.SDK_INT == 23) && Build.VERSION.RELEASE.equals("6.0");
            if ((android.os.Build.VERSION.SDK_INT >= 23) && (!a60)) {
                TextView infoText14 = (TextView) findViewById(R.id.activity_info_notification_dialog_info_text14);
                infoText14.setVisibility(View.GONE);
            }
            else {
                TextView infoText14 = (TextView) findViewById(R.id.activity_info_notification_dialog_info_text14);
                infoText14.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                        startActivity(intent);
                    }
                });
            }
        }

        if (android.os.Build.VERSION.SDK_INT < 23) {
            TextView infoText15 = (TextView) findViewById(R.id.activity_info_notification_dialog_info_text15);
            infoText15.setVisibility(View.GONE);
            TextView infoText16 = (TextView) findViewById(R.id.activity_info_notification_dialog_info_text16);
            infoText16.setVisibility(View.GONE);
            TextView infoText17 = (TextView) findViewById(R.id.activity_info_notification_dialog_info_text17);
            infoText17.setVisibility(View.GONE);
            TextView infoText18 = (TextView) findViewById(R.id.activity_info_notification_dialog_info_text18);
            infoText18.setVisibility(View.GONE);
            TextView infoText10a = (TextView) findViewById(R.id.activity_info_notification_dialog_info_text10a);
            infoText10a.setVisibility(View.GONE);
        }

        if (android.os.Build.VERSION.SDK_INT < 21) {
            TextView infoText13 = (TextView) findViewById(R.id.activity_info_notification_dialog_info_text13);
            infoText13.setVisibility(View.GONE);
            TextView infoText14 = (TextView) findViewById(R.id.activity_info_notification_dialog_info_text14);
            infoText14.setVisibility(View.GONE);
        }

        if (PPApplication.getMergedRingNotificationVolumes(context)) {
            TextView infoText3 = (TextView) findViewById(R.id.activity_info_notification_dialog_info_text3);
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
            TextView infoText3 = (TextView) findViewById(R.id.activity_info_notification_dialog_info_text3);
            infoText3.setVisibility(View.GONE);
        }

        TextView infoText41 = (TextView)findViewById(R.id.activity_info_activate_profile_from_tasker_params);
        infoText41.setText("Send Intent [ \n" +
                " Extra:profile_name:profile name\n" +
                " Package:sk.henrichg.phoneprofiles\n" +
                " Class:sk.henrichg.phoneprofiles.ActivateProfileFromExternalApplicationActivity\n" +
                " Target:Activity\n" +
                "]");

        if (!news) {
            TextView infoTextNews = (TextView) findViewById(R.id.activity_info_notification_dialog_news);
            infoTextNews.setVisibility(View.GONE);
        }

    }

    @Override
    protected void onStart()
    {
        super.onStart();
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
