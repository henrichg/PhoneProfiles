package sk.henrichg.phoneprofiles;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class GrantPermissionActivity extends AppCompatActivity {

    private int grantType;
    private List<Permissions.PermissionType> permissions;
    private long profile_id;
    private boolean onlyNotification;
    private boolean mergedNotification;
    private boolean forGUI;
    private boolean monochrome;
    private int monochromeValue;
    private int startupSource;
    private boolean interactive;
    private String applicationDataPath;
    private boolean activateProfile;

    private Profile profile;
    private DataWrapper dataWrapper;

    private boolean started = false;

    private static final int WRITE_SETTINGS_REQUEST_CODE = 909090;
    private static final int PERMISSIONS_REQUEST_CODE = 909091;
    private static final int ACCESS_NOTIFICATION_POLICY_REQUEST_CODE = 909092;
    private static final int DRAW_OVERLAYS_REQUEST_CODE = 909093;

    private static final String NOTIFICATION_DELETED_ACTION = "sk.henrichg.phoneprofiles.PERMISSIONS_NOTIFICATION_DELETED";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);

        Intent intent = getIntent();
        grantType = intent.getIntExtra(Permissions.EXTRA_GRANT_TYPE, 0);
        onlyNotification = intent.getBooleanExtra(Permissions.EXTRA_ONLY_NOTIFICATION, false);
        permissions = intent.getParcelableArrayListExtra(Permissions.EXTRA_PERMISSION_TYPES);
        mergedNotification = false;
        if (permissions == null) {
            permissions = Permissions.getMergedPermissions(getApplicationContext());
            mergedNotification = true;
        }

        profile_id = intent.getLongExtra(PPApplication.EXTRA_PROFILE_ID, 0);
        forGUI = intent.getBooleanExtra(Permissions.EXTRA_FOR_GUI, false);
        monochrome = intent.getBooleanExtra(Permissions.EXTRA_MONOCHROME, false);
        monochromeValue = intent.getIntExtra(Permissions.EXTRA_MONOCHROME_VALUE, 0xFF);
        startupSource = intent.getIntExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_ACTIVATOR);
        interactive = intent.getBooleanExtra(Permissions.EXTRA_INTERACTIVE, true);
        applicationDataPath = intent.getStringExtra(Permissions.EXTRA_APPLICATION_DATA_PATH);
        activateProfile = intent.getBooleanExtra(Permissions.EXTRA_ACTIVATE_PROFILE, true) && (profile_id != Profile.DEFAULT_PROFILE_ID);

        dataWrapper = new DataWrapper(getApplicationContext(), forGUI, monochrome, monochromeValue);
        if (profile_id != Profile.DEFAULT_PROFILE_ID)
            profile = dataWrapper.getProfileById(profile_id);
        else
            profile = Profile.getDefaultProfile(getApplicationContext());
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        if (started) return;
        started = true;

        if ((grantType == Permissions.GRANT_TYPE_PROFILE) && (profile == null)) {
            finish();
            return;
        }

        final Context context = getApplicationContext();

        if (permissions.size() == 0) {
            // called from notification - recheck permissions
            if (grantType == Permissions.GRANT_TYPE_INSTALL_TONE) {
                boolean granted = Permissions.checkInstallTone(context);
                if (!granted) {
                    permissions.add(new Permissions.PermissionType(Permissions.PERMISSION_INSTALL_TONE, Manifest.permission.WRITE_EXTERNAL_STORAGE));
                }
                else {
                    Toast msg = Toast.makeText(context,
                            context.getResources().getString(R.string.toast_permissions_granted),
                            Toast.LENGTH_SHORT);
                    msg.show();
                    finish();
                    return;
                }
            }
            else {
                // get permissions from shared preferences and recheck it
                permissions = Permissions.recheckPermissions(context, Permissions.getMergedPermissions(context));
                mergedNotification = true;
                if (permissions.size() == 0) {
                    Toast msg = Toast.makeText(context,
                            context.getResources().getString(R.string.toast_permissions_granted),
                            Toast.LENGTH_SHORT);
                    msg.show();
                    finish();
                    return;
                }
            }
        }

        boolean showRequestWriteSettings = false;
        boolean showRequestAccessNotificationPolicy = false;
        boolean showRequestDrawOverlays = false;
        boolean showRequestReadExternalStorage = false;
        boolean showRequestReadPhoneState = false;
        boolean showRequestProcessOutgoingCalls = false;
        boolean showRequestWriteExternalStorage = false;

        //Log.e("GrantPermissionActivity", "onStart - permissions.size="+permissions.size());

        for (Permissions.PermissionType permissionType : permissions) {
            if (permissionType.permission.equals(Manifest.permission.WRITE_SETTINGS))
                showRequestWriteSettings = Permissions.getShowRequestWriteSettingsPermission(context);
            if (permissionType.permission.equals(Manifest.permission.ACCESS_NOTIFICATION_POLICY))
                showRequestAccessNotificationPolicy = Permissions.getShowRequestAccessNotificationPolicyPermission(context);
            if (permissionType.permission.equals(Manifest.permission.SYSTEM_ALERT_WINDOW))
                showRequestDrawOverlays = Permissions.getShowRequestDrawOverlaysPermission(context);
            if (permissionType.permission.equals(Manifest.permission.READ_EXTERNAL_STORAGE))
                showRequestReadExternalStorage = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            if (permissionType.permission.equals(Manifest.permission.READ_PHONE_STATE))
                showRequestReadPhoneState = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE);
            if (permissionType.permission.equals(Manifest.permission.PROCESS_OUTGOING_CALLS))
                showRequestProcessOutgoingCalls = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.PROCESS_OUTGOING_CALLS);
            if (permissionType.permission.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE))
                showRequestWriteExternalStorage = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (showRequestWriteSettings ||
                showRequestReadExternalStorage ||
                showRequestReadPhoneState ||
                showRequestProcessOutgoingCalls ||
                showRequestWriteExternalStorage ||
                showRequestAccessNotificationPolicy ||
                showRequestDrawOverlays) {

            if (onlyNotification) {
                int notificationID;
                NotificationCompat.Builder mBuilder;
                Intent intent = new Intent(context, GrantPermissionActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);  // this close all activities with same taskAffinity
                if (grantType == Permissions.GRANT_TYPE_INSTALL_TONE) {
                    String nTitle = context.getString(R.string.permissions_for_install_tone_text_notification);
                    String nText = context.getString(R.string.permissions_for_install_tone_big_text_notification);
                    if (android.os.Build.VERSION.SDK_INT < 24) {
                        nTitle = context.getString(R.string.app_name);
                        nText = context.getString(R.string.permissions_for_install_tone_text_notification)+": "+
                                context.getString(R.string.permissions_for_install_tone_big_text_notification);
                    }
                    mBuilder =   new NotificationCompat.Builder(context)
                            .setSmallIcon(R.drawable.ic_exclamation_notify) // notification icon
                            .setContentTitle(nTitle) // title for notification
                            .setContentText(nText)
                            .setStyle(new NotificationCompat.BigTextStyle().bigText(nText))
                            .setAutoCancel(true); // clear notification after click
                    notificationID = PPApplication.GRANT_INSTALL_TONE_PERMISSIONS_NOTIFICATION_ID;
                }
                else {
                    String ntitle = context.getString(R.string.permissions_for_install_tone_text_notification);
                    String ntext = "";
                    if (android.os.Build.VERSION.SDK_INT < 24) {
                        ntitle = context.getString(R.string.app_name);
                        ntext = context.getString(R.string.permissions_for_profile_text_notification)+": ";
                    }
                    if (mergedNotification) {
                        ntext = ntext + context.getString(R.string.permissions_for_profile_text1m) + " " +
                                context.getString(R.string.permissions_for_profile_big_text_notification);
                    }
                    else {
                        ntext = ntext + context.getString(R.string.permissions_for_profile_text1) + " ";
                        if (profile != null)
                            ntext = ntext + "\"" + profile._name + "\" ";
                        ntext = ntext + context.getString(R.string.permissions_for_profile_big_text_notification);
                    }
                    mBuilder =   new NotificationCompat.Builder(context)
                            .setSmallIcon(R.drawable.ic_exclamation_notify) // notification icon
                            .setContentTitle(ntitle) // title for notification
                            .setContentText(ntext) // message for notification
                            .setAutoCancel(true); // clear notification after click
                    mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(ntext));

                    Intent deleteIntent = new Intent(NOTIFICATION_DELETED_ACTION);
                    PendingIntent deletePendingIntent = PendingIntent.getBroadcast(context, grantType, deleteIntent, 0);
                    mBuilder.setDeleteIntent(deletePendingIntent);

                    intent.putExtra(PPApplication.EXTRA_PROFILE_ID, profile._id);
                    intent.putExtra(Permissions.EXTRA_FOR_GUI, forGUI);
                    intent.putExtra(Permissions.EXTRA_MONOCHROME, monochrome);
                    intent.putExtra(Permissions.EXTRA_MONOCHROME_VALUE, monochromeValue);
                    notificationID = PPApplication.GRANT_PROFILE_PERMISSIONS_NOTIFICATION_ID;
                }
                permissions.clear();
                intent.putExtra(Permissions.EXTRA_GRANT_TYPE, grantType);
                intent.putParcelableArrayListExtra(Permissions.EXTRA_PERMISSION_TYPES, (ArrayList<Permissions.PermissionType>) permissions);
                intent.putExtra(Permissions.EXTRA_ONLY_NOTIFICATION, false);
                intent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, startupSource);
                intent.putExtra(Permissions.EXTRA_INTERACTIVE, interactive);

                PendingIntent pi = PendingIntent.getActivity(context, grantType, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                mBuilder.setContentIntent(pi);
                mBuilder.setPriority(Notification.PRIORITY_MAX);
                if (android.os.Build.VERSION.SDK_INT >= 21)
                {
                    mBuilder.setCategory(Notification.CATEGORY_RECOMMENDATION);
                    mBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
                }
                NotificationManager mNotificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
                if (mNotificationManager != null)
                    mNotificationManager.notify(notificationID, mBuilder.build());

                finish();
                return;
            }
            else {
                String showRequestString;

                if (grantType == Permissions.GRANT_TYPE_INSTALL_TONE)
                    showRequestString = context.getString(R.string.permissions_for_install_tone_text1) + "<br><br>";
                else if (grantType == Permissions.GRANT_TYPE_WALLPAPER)
                    showRequestString = context.getString(R.string.permissions_for_wallpaper_text1) + "<br><br>";
                else if (grantType == Permissions.GRANT_TYPE_CUSTOM_PROFILE_ICON)
                    showRequestString = context.getString(R.string.permissions_for_custom_profile_icon_text1) + "<br><br>";
                else if (grantType == Permissions.GRANT_TYPE_EXPORT)
                    showRequestString = context.getString(R.string.permissions_for_export_app_data_text1) + "<br><br>";
                else if (grantType == Permissions.GRANT_TYPE_IMPORT)
                    showRequestString = context.getString(R.string.permissions_for_import_app_data_text1) + "<br><br>";
                else if (grantType == Permissions.GRANT_TYPE_BRIGHTNESS_DIALOG)
                    showRequestString = context.getString(R.string.permissions_for_brightness_dialog_text1) + "<br><br>";
                else {
                    if (mergedNotification) {
                        showRequestString = context.getString(R.string.permissions_for_profile_text1m) + " ";
                        showRequestString = showRequestString + context.getString(R.string.permissions_for_profile_text2) + "<br><br>";
                    }
                    else {
                        showRequestString = context.getString(R.string.permissions_for_profile_text1) + " ";
                        if (profile != null)
                            showRequestString = showRequestString + "\"" + profile._name + "\" ";
                        showRequestString = showRequestString + context.getString(R.string.permissions_for_profile_text2) + "<br><br>";
                    }
                }

                if (showRequestWriteSettings) {
                    //Log.e("GrantPermissionActivity", "onStart - showRequestWriteSettings");
                    showRequestString = showRequestString + "<b>" + "\u2022 " + context.getString(R.string.permission_group_name_write_settings) + "</b>";
                    showRequestString = showRequestString + "<br>";
                }
                if (showRequestReadExternalStorage || showRequestWriteExternalStorage) {
                    //Log.e("GrantPermissionActivity", "onStart - showRequestReadExternalStorage");
                    showRequestString = showRequestString + "<b>" + "\u2022 " + context.getString(R.string.permission_group_name_storage) + "</b>";
                    showRequestString = showRequestString + "<br>";
                }
                if (showRequestReadPhoneState || showRequestProcessOutgoingCalls) {
                    //Log.e("GrantPermissionActivity", "onStart - showRequestReadPhoneState");
                    showRequestString = showRequestString + "<b>" + "\u2022 " + context.getString(R.string.permission_group_name_phone) + "</b>";
                    showRequestString = showRequestString + "<br>";
                }
                if (showRequestAccessNotificationPolicy) {
                    //Log.e("GrantPermissionActivity", "onStart - showRequestAccessNotificationPolicy");
                    showRequestString = showRequestString + "<b>" + "\u2022 " + context.getString(R.string.permission_group_name_access_notification_policy) + "</b>";
                    showRequestString = showRequestString + "<br>";
                }
                if (showRequestDrawOverlays) {
                    //Log.e("GrantPermissionActivity", "onStart - showRequestDrawOverlays");
                    showRequestString = showRequestString + "<b>" + "\u2022 " + context.getString(R.string.permission_group_name_draw_overalys) + "</b>";
                    showRequestString = showRequestString + "<br>";
                }

                showRequestString = showRequestString + "<br>";

                if (grantType == Permissions.GRANT_TYPE_INSTALL_TONE)
                    showRequestString = showRequestString + context.getString(R.string.permissions_for_install_tone_text2);
                else if (grantType == Permissions.GRANT_TYPE_WALLPAPER)
                    showRequestString = showRequestString + context.getString(R.string.permissions_for_wallpaper_text2);
                else if (grantType == Permissions.GRANT_TYPE_CUSTOM_PROFILE_ICON)
                    showRequestString = showRequestString + context.getString(R.string.permissions_for_custom_profile_icon_text2);
                else if (grantType == Permissions.GRANT_TYPE_EXPORT)
                    showRequestString = showRequestString + context.getString(R.string.permissions_for_export_app_data_text2);
                else if (grantType == Permissions.GRANT_TYPE_IMPORT)
                    showRequestString = showRequestString + context.getString(R.string.permissions_for_import_app_data_text2);
                else if (grantType == Permissions.GRANT_TYPE_BRIGHTNESS_DIALOG)
                    showRequestString = showRequestString + context.getString(R.string.permissions_for_brightness_dialog_text2);
                else
                    showRequestString = showRequestString + context.getString(R.string.permissions_for_profile_text3);

                // set theme and language for dialog alert ;-)
                // not working on Android 2.3.x
                GlobalGUIRoutines.setTheme(this, true, false);
                GlobalGUIRoutines.setLanguage(this.getBaseContext());

                final boolean _showRequestWriteSettings = showRequestWriteSettings;
                final boolean _showRequestAccessNotificationPolicy = showRequestAccessNotificationPolicy;
                final boolean _showRequestDrawOverlays = showRequestDrawOverlays;

                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
                dialogBuilder.setTitle(R.string.permissions_alert_title);
                dialogBuilder.setMessage(GlobalGUIRoutines.fromHtml(showRequestString));
                dialogBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int iteration = 4;
                        if (_showRequestWriteSettings)
                            iteration = 1;
                        else if (_showRequestAccessNotificationPolicy)
                            iteration = 2;
                        else if (_showRequestDrawOverlays)
                            iteration = 3;
                        requestPermissions(iteration);
                    }
                });
                dialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        finish();
                        Permissions.releaseReferences();
                        if (mergedNotification)
                            Permissions.clearMergedPermissions(context);
                    }
                });
                dialogBuilder.show();
            }
        }
        else {
            requestPermissions(3);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.

                boolean allGranted = true;
                for (int grantResult : grantResults) {
                    if (grantResult == PackageManager.PERMISSION_DENIED) {
                        allGranted = false;
                        break;
                    }
                }

                if (allGranted) {
                    finishGrant();
                } else {
                    if (!onlyNotification) {
                        Context context = getApplicationContext();
                        Toast msg = Toast.makeText(context,
                                context.getResources().getString(R.string.app_name) + ": " +
                                        context.getResources().getString(R.string.toast_permissions_not_granted),
                                Toast.LENGTH_SHORT);
                        msg.show();
                    }
                    finish();
                    Permissions.releaseReferences();
                    if (mergedNotification)
                        Permissions.clearMergedPermissions(getApplicationContext());
                }
                //return;
                break;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        final Context context = getApplicationContext();
        if (requestCode == WRITE_SETTINGS_REQUEST_CODE) {
            if (!Settings.System.canWrite(context)) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
                dialogBuilder.setTitle(R.string.permissions_alert_title);
                dialogBuilder.setMessage(R.string.permissions_write_settings_not_allowed_confirm);
                dialogBuilder.setPositiveButton(R.string.alert_button_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Permissions.setShowRequestWriteSettingsPermission(context, false);
                        requestPermissions(2);
                    }
                });
                dialogBuilder.setNegativeButton(R.string.alert_button_no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Permissions.setShowRequestWriteSettingsPermission(context, true);
                        requestPermissions(2);
                    }
                });
                dialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        requestPermissions(2);
                    }
                });
                dialogBuilder.show();
            }
            else {
                Permissions.setShowRequestWriteSettingsPermission(context, true);
                requestPermissions(2);
            }
        }
        if (requestCode == ACCESS_NOTIFICATION_POLICY_REQUEST_CODE) {
            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (mNotificationManager != null) {
                if (!mNotificationManager.isNotificationPolicyAccessGranted()) {
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
                    dialogBuilder.setTitle(R.string.permissions_alert_title);
                    dialogBuilder.setMessage(R.string.permissions_access_notification_policy_not_allowed_confirm);
                    dialogBuilder.setPositiveButton(R.string.alert_button_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Permissions.setShowRequestAccessNotificationPolicyPermission(context, false);
                            requestPermissions(3);
                        }
                    });
                    dialogBuilder.setNegativeButton(R.string.alert_button_no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Permissions.setShowRequestAccessNotificationPolicyPermission(context, true);
                            requestPermissions(3);
                        }
                    });
                    dialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            requestPermissions(3);
                        }
                    });
                    dialogBuilder.show();
                } else {
                    Permissions.setShowRequestAccessNotificationPolicyPermission(context, true);
                    requestPermissions(3);
                }
            }
            else
                requestPermissions(3);
        }
        if (requestCode == DRAW_OVERLAYS_REQUEST_CODE) {
            if (!Settings.canDrawOverlays(context)) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
                dialogBuilder.setTitle(R.string.permissions_alert_title);
                dialogBuilder.setMessage(R.string.permissions_draw_overlays_not_allowed_confirm);
                dialogBuilder.setPositiveButton(R.string.alert_button_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Permissions.setShowRequestDrawOverlaysPermission(context, false);
                        requestPermissions(4);
                    }
                });
                dialogBuilder.setNegativeButton(R.string.alert_button_no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Permissions.setShowRequestDrawOverlaysPermission(context, true);
                        requestPermissions(4);
                    }
                });
                dialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        requestPermissions(4);
                    }
                });
                dialogBuilder.show();
            }
            else {
                Permissions.setShowRequestDrawOverlaysPermission(context, true);
                requestPermissions(4);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void requestPermissions(int iteration) {

        if (iteration == 1) {
            boolean writeSettingsFound = false;
            for (Permissions.PermissionType permissionType : permissions) {
                if (permissionType.permission.equals(Manifest.permission.WRITE_SETTINGS) &&
                        GlobalGUIRoutines.activityActionExists(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS, getApplicationContext())) {
                    writeSettingsFound = true;
                    final Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                    startActivityForResult(intent, WRITE_SETTINGS_REQUEST_CODE);
                    break;
                }
            }
            if (!writeSettingsFound)
                requestPermissions(2);
        }
        else
        if (iteration == 2) {
            boolean accessNotificationPolicyFound = false;
            boolean no60 = !Build.VERSION.RELEASE.equals("6.0");
            for (Permissions.PermissionType permissionType : permissions) {
                if (no60 && permissionType.permission.equals(Manifest.permission.ACCESS_NOTIFICATION_POLICY) &&
                        GlobalGUIRoutines.activityActionExists(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS, getApplicationContext())) {
                    accessNotificationPolicyFound = true;
                    final Intent intent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                    startActivityForResult(intent, ACCESS_NOTIFICATION_POLICY_REQUEST_CODE);
                    break;
                }
            }
            if (!accessNotificationPolicyFound)
                requestPermissions(3);
        }
        else
        if (iteration == 3) {
            boolean drawOverlaysFound = false;
            //boolean api25 = android.os.Build.VERSION.SDK_INT >= 25;
            for (Permissions.PermissionType permissionType : permissions) {
                if (/*api25 &&*/ permissionType.permission.equals(Manifest.permission.SYSTEM_ALERT_WINDOW) &&
                        GlobalGUIRoutines.activityActionExists(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION, getApplicationContext())) {
                    drawOverlaysFound = true;
                    final Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                    startActivityForResult(intent, DRAW_OVERLAYS_REQUEST_CODE);
                    break;
                }
            }
            if (!drawOverlaysFound)
                requestPermissions(4);
        }
        else {
            List<String> permList = new ArrayList<>();
            for (Permissions.PermissionType permissionType : permissions) {
                if ((!permissionType.permission.equals(Manifest.permission.WRITE_SETTINGS)) &&
                    (!permissionType.permission.equals(Manifest.permission.ACCESS_NOTIFICATION_POLICY)) &&
                    (!permissionType.permission.equals(Manifest.permission.SYSTEM_ALERT_WINDOW)) &&
                    (!permList.contains(permissionType.permission))) {
                    //Log.e("GrantPermissionActivity", "requestPermissions - permission=" + permissionType.permission);
                    permList.add(permissionType.permission);
                }
            }

            //Log.e("GrantPermissionActivity", "requestPermissions - permList.size=" + permList.size());
            if (permList.size() > 0) {

                String[] permArray = new String[permList.size()];
                for (int i = 0; i < permList.size(); i++) permArray[i] = permList.get(i);

                ActivityCompat.requestPermissions(this, permArray, PERMISSIONS_REQUEST_CODE);
            }
            else
                finishGrant();
        }
    }

    private void finishGrant() {
        Context context = getApplicationContext();

        ActivateProfileHelper activateProfileHelper = dataWrapper.getActivateProfileHelper();
        activateProfileHelper.initialize(dataWrapper, context);

        if (forGUI && (profile != null))
        {
            // regenerate profile icon
            dataWrapper.refreshProfileIcon(profile, monochrome, monochromeValue);
        }

        if (grantType == Permissions.GRANT_TYPE_INSTALL_TONE) {
            //finishAffinity();
            finish();
            Permissions.removeInstallToneNotification(context);
            TonesHandler.installTone(TonesHandler.TONE_ID, TonesHandler.TONE_NAME, context, true);
        }
        else
        if (grantType == Permissions.GRANT_TYPE_WALLPAPER) {
            finish();
            if (Permissions.imageViewPreference != null)
                Permissions.imageViewPreference.startGallery();
        }
        else
        if (grantType == Permissions.GRANT_TYPE_CUSTOM_PROFILE_ICON) {
            finish();
            if (Permissions.profileIconPreference != null)
                Permissions.profileIconPreference.startGallery();
        }
        else
        if (grantType == Permissions.GRANT_TYPE_EXPORT) {
            finish();
            if (Permissions.editorActivity != null)
                Permissions.editorActivity.doExportData();
        }
        else
        if (grantType == Permissions.GRANT_TYPE_IMPORT) {
            finish();
            if (Permissions.editorActivity != null)
                Permissions.editorActivity.doImportData(applicationDataPath);
        }
        else
        if (grantType == Permissions.GRANT_TYPE_BRIGHTNESS_DIALOG) {
            finish();
            if (Permissions.brightnessDialogPreference != null)
                Permissions.brightnessDialogPreference.enableViews();
        }
        else
        if (grantType == Permissions.GRANT_TYPE_RINGTONE_PREFERENCE) {
            if (Permissions.ringtonePreference != null)
                Permissions.ringtonePreference.refreshListView();
            finish();
        }
        else {
            //finishAffinity();
            finish();
            Permissions.removeProfileNotification(context);
            if (activateProfile)
                dataWrapper._activateProfile(profile, startupSource, interactive, Permissions.profileActivationActivity);
        }
        Permissions.releaseReferences();
        if (mergedNotification)
            Permissions.clearMergedPermissions(context);

        //if (grantType != Permissions.GRANT_TYPE_PROFILE) {
            Profile activatedProfile = dataWrapper.getActivatedProfile();
            if ((activatedProfile == null) || (activatedProfile._id == profile_id)) {
                if (PhoneProfilesService.instance != null)
                    PhoneProfilesService.instance.showProfileNotification(profile, dataWrapper);
            }
            activateProfileHelper.updateWidget(true);

            /*Intent intent5 = new Intent();
            intent5.setAction(RefreshGUIBroadcastReceiver.INTENT_REFRESH_GUI);
            intent5.putExtra(RefreshGUIBroadcastReceiver.EXTRA_REFRESH_ICONS, true);
            context.sendBroadcast(intent5);*/
            LocalBroadcastManager.getInstance(context).registerReceiver(PPApplication.refreshGUIBroadcastReceiver, new IntentFilter("RefreshGUIBroadcastReceiver"));
            Intent intent5 = new Intent("RefreshGUIBroadcastReceiver");
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent5);

        //}


    }

    @Override
    public void finish()
    {
        super.finish();
        overridePendingTransition(0, 0);
    }

}
