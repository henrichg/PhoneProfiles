package sk.henrichg.phoneprofiles;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.AudioManager;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import java.util.List;

public class DataWrapper {

    public Context context = null;
    private boolean forGUI = false;
    private boolean monochrome = false;
    private int monochromeValue = 0xFF;
    private Handler toastHandler;

    private DatabaseHandler databaseHandler = null;
    private ActivateProfileHelper activateProfileHelper = null;
    private List<Profile> profileList = null;

    DataWrapper(Context c, boolean fgui, boolean mono, int monoVal)
    {
        //long nanoTimeStart = GlobalData.startMeasuringRunTime();

        context = c;

        setParameters(fgui, mono, monoVal);

        databaseHandler = getDatabaseHandler();
        //activateProfileHelper = getActivateProfileHelper();

        //GlobalData.getMeasuredRunTime(nanoTimeStart, "ProfilesDataWrapper.constructor");
    }

    public void setParameters(
            boolean fgui,
            boolean mono,
            int monoVal)
    {
        forGUI = fgui;
        monochrome = mono;
        monochromeValue = monoVal;
    }

    public void setToastHandler(Handler handler)
    {
        toastHandler = handler;
    }

    public DatabaseHandler getDatabaseHandler()
    {
        if (databaseHandler == null)
            // parameter must by application context
            databaseHandler = DatabaseHandler.getInstance(context);

        return databaseHandler;
    }

    public ActivateProfileHelper getActivateProfileHelper()
    {
        if (activateProfileHelper == null)
            activateProfileHelper = new ActivateProfileHelper();

        return activateProfileHelper;
    }

    public List<Profile> getProfileList()
    {
        //long nanoTimeStart = GlobalData.startMeasuringRunTime();

        if (profileList == null)
        {
            profileList = getDatabaseHandler().getAllProfiles();

            if (forGUI)
            {
                for (Profile profile : profileList)
                {
                    profile.generateIconBitmap(context, monochrome, monochromeValue);
                    profile.generatePreferencesIndicator(context, monochrome, monochromeValue);
                }
            }
        }

        //GlobalData.getMeasuredRunTime(nanoTimeStart, "ProfilesDataWrapper.getProfileList");

        return profileList;
    }

    public void setProfileList(List<Profile> profileList, boolean recycleBitmaps)
    {
        if (recycleBitmaps)
            clearProfileList();
        else
            if (this.profileList != null)
                this.profileList.clear();
        this.profileList = profileList;
    }

    public Profile getNoinitializedProfile(String name, String icon, int order)
    {
        return new Profile(
                  name,
                  icon + "|1|0|0",
                  false,
                  order,
                  0,
                  "-1|1|0",
                  "-1|1|0",
                  "-1|1|0",
                  "-1|1|0",
                  "-1|1|0",
                  "-1|1|0",
                  0,
                  Settings.System.DEFAULT_RINGTONE_URI.toString(),
                  0,
                  Settings.System.DEFAULT_NOTIFICATION_URI.toString(),
                  0,
                  Settings.System.DEFAULT_ALARM_ALERT_URI.toString(),
                  0,
                  0,
                  0,
                  0,
                  Profile.BRIGHTNESS_ADAPTIVE_BRIGHTNESS_NOT_SET+"|1|1|0",
                  0,
                  "-|0",
                  0,
                  0,
                  0,
                  0,
                  "-",
                  0,
                  0,
                  0,
                  0,
                  0,
                  0,
                  Profile.AFTERDURATIONDO_NOTHING,
                  0,
                  0,
                  0,
                  0,
                  0,
                  false,
                  0,
                  0
            );
    }

    private String getVolumeLevelString(int percentage, int maxValue)
    {
        Double dValue = maxValue / 100.0 * percentage;
        return String.valueOf(dValue.intValue());
    }

    public Profile  getDefaultProfile(int index, boolean saveToDB)
    {
        AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        int	maximumValueRing = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
        int	maximumValueNotification = audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);
        int	maximumValueMusic = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int	maximumValueAlarm = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
        //int	maximumValueSystem = audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
        //int	maximumValueVoicecall = audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);

        Profile profile;

        switch (index) {
            case 0:
                profile = getNoinitializedProfile(context.getString(R.string.default_profile_name_home), "ic_profile_home_2", 1);
                if (PPNotificationListenerService.isNotificationListenerServiceEnabled(context)) {
                    if (android.os.Build.VERSION.SDK_INT >= 23) {
                        profile._volumeRingerMode = 5;
                        profile._volumeZenMode = 1; // ALL
                    } else if (android.os.Build.VERSION.SDK_INT >= 21) {
                        profile._volumeRingerMode = 5;
                        profile._volumeZenMode = 1; // ALL
                    } else
                        profile._volumeRingerMode = 1;
                } else
                    profile._volumeRingerMode = 1;
                profile._volumeRingtone = getVolumeLevelString(71, maximumValueRing) + "|0|0";
                profile._volumeNotification = getVolumeLevelString(86, maximumValueNotification) + "|0|0";
                profile._volumeAlarm = getVolumeLevelString(100, maximumValueAlarm) + "|0|0";
                profile._volumeMedia = getVolumeLevelString(80, maximumValueMusic) + "|0|0";
                profile._deviceWiFi = 1;
                //profile._deviceBrightness = "60|0|0|0";
                break;
            case 1:
                profile = getNoinitializedProfile(context.getString(R.string.default_profile_name_outdoor), "ic_profile_outdoors_1", 2);
                if (PPNotificationListenerService.isNotificationListenerServiceEnabled(context)) {
                    if (android.os.Build.VERSION.SDK_INT >= 23) {
                        profile._volumeRingerMode = 5;
                        profile._volumeZenMode = 4; // ALL with vibration
                    } else if (android.os.Build.VERSION.SDK_INT >= 21) {
                        profile._volumeRingerMode = 5;
                        profile._volumeZenMode = 4; // ALL with vibration
                    } else
                        profile._volumeRingerMode = 2;
                } else
                    profile._volumeRingerMode = 2;
                profile._volumeRingtone = getVolumeLevelString(100, maximumValueRing) + "|0|0";
                profile._volumeNotification = getVolumeLevelString(100, maximumValueNotification) + "|0|0";
                profile._volumeAlarm = getVolumeLevelString(100, maximumValueAlarm) + "|0|0";
                profile._volumeMedia = getVolumeLevelString(93, maximumValueMusic) + "|0|0";
                profile._deviceWiFi = 2;
                //profile._deviceBrightness = "255|0|0|0";
                break;
            case 2:
                profile = getNoinitializedProfile(context.getString(R.string.default_profile_name_work), "ic_profile_work_5", 3);
                if (PPNotificationListenerService.isNotificationListenerServiceEnabled(context)) {
                    if (android.os.Build.VERSION.SDK_INT >= 23) {
                        profile._volumeRingerMode = 5;
                        profile._volumeZenMode = 1; // ALL
                    } else if (android.os.Build.VERSION.SDK_INT >= 21) {
                        profile._volumeRingerMode = 5;
                        profile._volumeZenMode = 1; // ALL
                    } else
                        profile._volumeRingerMode = 1;
                } else
                    profile._volumeRingerMode = 1;
                profile._volumeRingtone = getVolumeLevelString(57, maximumValueRing) + "|0|0";
                profile._volumeNotification = getVolumeLevelString(71, maximumValueNotification) + "|0|0";
                profile._volumeAlarm = getVolumeLevelString(57, maximumValueAlarm) + "|0|0";
                profile._volumeMedia = getVolumeLevelString(80, maximumValueMusic) + "|0|0";
                profile._deviceWiFi = 2;
                //profile._deviceBrightness = "60|0|0|0";
                break;
            case 3:
                profile = getNoinitializedProfile(context.getString(R.string.default_profile_name_meeting), "ic_profile_meeting_2", 4);
                if (PPNotificationListenerService.isNotificationListenerServiceEnabled(context)) {
                    if (android.os.Build.VERSION.SDK_INT >= 23) {
                        profile._volumeRingerMode = 5;
                        profile._volumeZenMode = 3; // NONE
                    } else if (android.os.Build.VERSION.SDK_INT >= 21) {
                        profile._volumeRingerMode = 5;
                        profile._volumeZenMode = 3; // NONE
                    } else
                        profile._volumeRingerMode = 4;
                } else
                    profile._volumeRingerMode = 4;
                profile._volumeRingtone = getVolumeLevelString(57, maximumValueRing) + "|0|0";
                profile._volumeNotification = getVolumeLevelString(71, maximumValueNotification) + "|0|0";
                profile._volumeAlarm = getVolumeLevelString(57, maximumValueAlarm) + "|0|0";
                profile._volumeMedia = getVolumeLevelString(80, maximumValueMusic) + "|0|0";
                profile._deviceWiFi = 0;
                //profile._deviceBrightness = Profile.BRIGHTNESS_ADAPTIVE_BRIGHTNESS_NOT_SET+"|1|1|0";
                break;
            case 4:
                profile = getNoinitializedProfile(context.getString(R.string.default_profile_name_sleep), "ic_profile_sleep", 5);
                if (PPNotificationListenerService.isNotificationListenerServiceEnabled(context)) {
                    if (android.os.Build.VERSION.SDK_INT >= 23) {
                        profile._volumeRingerMode = 5;
                        profile._volumeZenMode = 6; // ALARMS
                    } else if (android.os.Build.VERSION.SDK_INT >= 21) {
                        profile._volumeRingerMode = 5;
                        profile._volumeZenMode = 3; // NONE
                    } else
                        profile._volumeRingerMode = 4;
                } else
                    profile._volumeRingerMode = 4;
                profile._volumeRingtone = getVolumeLevelString(71, maximumValueRing) + "|0|0";
                profile._volumeNotification = getVolumeLevelString(86, maximumValueNotification) + "|0|0";
                profile._volumeAlarm = getVolumeLevelString(100, maximumValueAlarm) + "|0|0";
                profile._volumeMedia = getVolumeLevelString(80, maximumValueMusic) + "|0|0";
                profile._deviceWiFi = 0;
                //profile._deviceBrightness = "10|0|0|0";
                break;
            case 5:
                profile = getNoinitializedProfile(context.getString(R.string.default_profile_name_battery_low), "ic_profile_battery_1", 6);
                profile._deviceAutosync = 2;
                profile._deviceMobileData = 2;
                profile._deviceWiFi = 2;
                profile._deviceBluetooth = 2;
                profile._deviceGPS = 2;
                break;
            default:
                profile = null;
        }

        if (profile != null) {
            if (saveToDB)
                getDatabaseHandler().addProfile(profile);
        }

        return profile;
    }

    public List<Profile>  getDefaultProfileList()
    {
        clearProfileList();
        getDatabaseHandler().deleteAllProfiles();

        for (int index = 0; index < 6; index++)
            getDefaultProfile(index, true);

        return getProfileList();

    }

    public void clearProfileList()
    {
        if (profileList != null)
        {
            for (Profile profile : profileList)
            {
                profile.releaseIconBitmap();
                profile.releasePreferencesIndicator();
            }
            profileList.clear();
        }
        profileList = null;
    }

    public Profile getActivatedProfileFromDB()
    {
        Profile profile = getDatabaseHandler().getActivatedProfile();
        if (forGUI && (profile != null))
        {
            profile.generateIconBitmap(context, monochrome, monochromeValue);
            profile.generatePreferencesIndicator(context, monochrome, monochromeValue);
        }
        return profile;
    }

    public Profile getActivatedProfile()
    {
        if (profileList == null)
        {
            return getActivatedProfileFromDB();
        }
        else
        {
            Profile profile;
            for (int i = 0; i < profileList.size(); i++)
            {
                profile = profileList.get(i);
                if (profile._checked)
                    return profile;
            }
        }

        return null;
    }

    public Profile getFirstProfile()
    {
        if (profileList == null)
        {
            Profile profile = getDatabaseHandler().getFirstProfile();
            if (forGUI && (profile != null))
            {
                profile.generateIconBitmap(context, monochrome, monochromeValue);
                profile.generatePreferencesIndicator(context, monochrome, monochromeValue);
            }
            return profile;
        }
        else
        {
            Profile profile;
            if (profileList.size() > 0)
                profile = profileList.get(0);
            else
                profile = null;

            return profile;
        }
    }

    public int getItemPosition(Profile profile)
    {
        if (profile == null)
            return -1;

        if (profileList == null)
            return getDatabaseHandler().getProfilePosition(profile);
        else
        {
            if (profile != null)
            {
                for (int i = 0; i < profileList.size(); i++)
                {
                    if (profileList.get(i)._id == profile._id)
                        return i;
                }
            }
            return -1;
        }
    }

    public void setProfileActive(Profile profile)
    {
        if ((profileList == null) || (profile == null))
            return;

        for (Profile p : profileList)
        {
            p._checked = false;
        }

        // teraz musime najst profile v profileList
        int position = getItemPosition(profile);
        if (position != -1)
        {
            // najdenemu objektu nastavime _checked
            Profile _profile = profileList.get(position);
            if (_profile != null)
                _profile._checked = true;
        }
    }

    public Profile getProfileById(long id)
    {
        if (profileList == null)
        {
            Profile profile = getDatabaseHandler().getProfile(id);
            if (forGUI && (profile != null))
            {
                profile.generateIconBitmap(context, monochrome, monochromeValue);
                profile.generatePreferencesIndicator(context, monochrome, monochromeValue);
            }
            return profile;
        }
        else
        {
            Profile profile;
            for (int i = 0; i < profileList.size(); i++)
            {
                profile = profileList.get(i);
                if (profile._id == id)
                    return profile;
            }

            return null;
        }
    }

    public void updateProfile(Profile profile)
    {
        if (profile != null)
        {
            Profile origProfile = getProfileById(profile._id);
            if (origProfile != null)
                origProfile.copyProfile(profile);
        }
    }

    public void reloadProfilesData()
    {
        clearProfileList();
        getProfileList();
    }

    public void deleteProfile(Profile profile)
    {
        if (profile == null)
            return;

        profileList.remove(profile);

        // unlink profile from Background profile
        if (Long.valueOf(GlobalData.applicationBackgroundProfile) == profile._id)
        {
            SharedPreferences preferences = context.getSharedPreferences(GlobalData.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
            Editor editor = preferences.edit();
            editor.putString(GlobalData.PREF_APPLICATION_BACKGROUND_PROFILE, String.valueOf(GlobalData.PROFILE_NO_ACTIVATE));
            editor.commit();
        }
    }

    public void deleteAllProfiles()
    {
        profileList.clear();

        // unlink profiles from Background profile
        SharedPreferences preferences = context.getSharedPreferences(GlobalData.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putString(GlobalData.PREF_APPLICATION_BACKGROUND_PROFILE, String.valueOf(GlobalData.PROFILE_NO_ACTIVATE));
        editor.commit();
    }

    public void invalidateDataWrapper()
    {
        clearProfileList();
        databaseHandler = null;
        if (activateProfileHelper != null)
            activateProfileHelper.deinitialize();
        activateProfileHelper = null;
    }

    public void refreshProfileIcon(Profile profile, boolean monochrome, int monochromeValue) {
        if (profile != null) {
            boolean isIconResourceID = profile.getIsIconResourceID();
            String iconIdentifier = profile.getIconIdentifier();
            getDatabaseHandler().getProfileIcon(profile);
            if (isIconResourceID && iconIdentifier.equals("ic_profile_default") && (!profile.getIsIconResourceID())) {
                profile.generateIconBitmap(context, monochrome, monochromeValue);
                profile.generatePreferencesIndicator(context, monochrome, monochromeValue);
            }
        }
    }

//----- Activate profile ---------------------------------------------------------------------------------------------

    public void _activateProfile(Profile _profile, int startupSource, boolean _interactive, Activity _activity)
    {
        // remove last configured profile duration alarm
        ProfileDurationAlarmBroadcastReceiver.removeAlarm(context);
        GlobalData.setActivatedProfileForDuration(context, 0);

        final Profile profile = _profile;
        final boolean interactive = _interactive;
        final Activity activity = _activity;

        Profile activatedProfile = getActivatedProfile();

        databaseHandler.activateProfile(profile);
        setProfileActive(profile);

        activateProfileHelper.execute(profile, interactive);

        if (interactive)
        {
            long profileId = 0;
            if (activatedProfile != null)
                profileId = activatedProfile._id;
            GlobalData.setActivatedProfileForDuration(context, profileId);
            ProfileDurationAlarmBroadcastReceiver.setAlarm(profile, context);
        }

        activateProfileHelper.showNotification(profile);
        activateProfileHelper.updateWidget();

        if (GlobalData.notificationsToast)
        {
            // toast notification
            if (toastHandler != null)
            {
                final Profile __profile = profile;
                toastHandler.post(new Runnable() {
                    public void run() {
                        showToastAfterActivation(__profile);
                    }
                });
            }
            else
                showToastAfterActivation(profile);
        }

        // for startActivityForResult
        if (activity != null)
        {
            Intent returnIntent = new Intent();
            returnIntent.putExtra(GlobalData.EXTRA_PROFILE_ID, profile._id);
            returnIntent.getIntExtra(GlobalData.EXTRA_STARTUP_SOURCE, startupSource);
            activity.setResult(Activity.RESULT_OK,returnIntent);
        }

        finishActivity(startupSource, true, activity);
    }

    private void showToastAfterActivation(Profile profile)
    {
        Toast msg = Toast.makeText(context,
                context.getResources().getString(R.string.toast_profile_activated_0) + ": " + profile._name + " " +
                        context.getResources().getString(R.string.toast_profile_activated_1),
                Toast.LENGTH_SHORT);
        msg.show();
    }

    private void activateProfileWithAlert(Profile profile, int startupSource, boolean interactive, Activity activity)
    {
        if ((GlobalData.applicationActivateWithAlert && interactive) ||
            (startupSource == GlobalData.STARTUP_SOURCE_EDITOR))
        {
            // set theme and language for dialog alert ;-)
            // not working on Android 2.3.x
            GUIData.setTheme(activity, true, false);
            GUIData.setLanguage(activity.getBaseContext());

            final Profile _profile = profile;
            final boolean _interactive = interactive;
            final int _startupSource = startupSource;
            final Activity _activity = activity;
            final DataWrapper _dataWrapper = this;

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
            dialogBuilder.setTitle(activity.getResources().getString(R.string.profile_string_0) + ": " + profile._name);
            dialogBuilder.setMessage(R.string.activate_profile_alert_message);
            //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
            dialogBuilder.setPositiveButton(R.string.alert_button_yes, new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    if (Permissions.grantProfilePermissions(context, _profile, false,
                            forGUI, monochrome, monochromeValue,
                            _startupSource, _interactive, _activity)) {
                        if (_profile._askForDuration) {
                            FastAccessDurationDialog dlg = new FastAccessDurationDialog(_activity, _profile, _dataWrapper, _startupSource, _interactive);
                            dlg.show();
                        }
                        else
                            _activateProfile(_profile, _startupSource, _interactive, _activity);
                    }
                    else {
                        Intent returnIntent = new Intent();
                        _activity.setResult(Activity.RESULT_CANCELED,returnIntent);

                        finishActivity(_startupSource, false, _activity);
                    }
                }
            });
            dialogBuilder.setNegativeButton(R.string.alert_button_no, new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {

                    // for startActivityForResult
                    Intent returnIntent = new Intent();
                    _activity.setResult(Activity.RESULT_CANCELED,returnIntent);

                    finishActivity(_startupSource, false, _activity);
                }
            });
            dialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {

                public void onCancel(DialogInterface dialog) {
                    // for startActivityForResult
                    Intent returnIntent = new Intent();
                    _activity.setResult(Activity.RESULT_CANCELED,returnIntent);

                    finishActivity(_startupSource, false, _activity);
                }
            });
            dialogBuilder.show();
        }
        else
        {
            boolean granted;
            if (interactive)
                granted = Permissions.grantProfilePermissions(context, profile, false,
                        forGUI, monochrome, monochromeValue,
                        startupSource, interactive, activity);
            else
                granted = Permissions.grantProfilePermissions(context, profile, true,
                        forGUI, monochrome, monochromeValue,
                        startupSource, interactive, null);
            if (granted) {
                if (profile._askForDuration && interactive) {
                    FastAccessDurationDialog dlg = new FastAccessDurationDialog(activity, profile, this, startupSource, interactive);
                    dlg.show();
                }
                else
                    _activateProfile(profile, startupSource, interactive, activity);
            }
        }
    }

    public void finishActivity(int startupSource, boolean afterActivation, Activity _activity)
    {
        if (_activity == null)
            return;

        final Activity activity = _activity;

        boolean finish = true;

        if (startupSource == GlobalData.STARTUP_SOURCE_ACTIVATOR_START)
        {
            finish = false;
        }
        else
        if (startupSource == GlobalData.STARTUP_SOURCE_ACTIVATOR)
        {
            finish = false;
            if (GlobalData.applicationClose)
            {
                // ma sa zatvarat aktivita po aktivacii
                if (GlobalData.getApplicationStarted(activity.getApplicationContext()))
                    // aplikacia je uz spustena, mozeme aktivitu zavriet
                    // tymto je vyriesene, ze pri spusteni aplikacie z launchera
                    // sa hned nezavrie
                    finish = afterActivation;
            }
        }
        else
        if (startupSource == GlobalData.STARTUP_SOURCE_EDITOR)
        {
            finish = false;
        }

        if (finish)
        {
            if (activity != null)
                activity.finish();
        }
    }

    public void activateProfile(long profile_id, int startupSource, Activity activity)
    {
        Profile profile;

        // pre profil, ktory je prave aktivny, treba aktualizovat aktivitu
        profile = getActivatedProfile();

        long backgroundProfileId = Long.valueOf(GlobalData.applicationBackgroundProfile);
        if ((profile == null) &&
            (backgroundProfileId != GlobalData.PROFILE_NO_ACTIVATE))
        {
            profile = getProfileById(backgroundProfileId);
        }

        boolean actProfile = false;
        boolean interactive = false;
        if ((startupSource == GlobalData.STARTUP_SOURCE_SHORTCUT) ||
            (startupSource == GlobalData.STARTUP_SOURCE_WIDGET) ||
            (startupSource == GlobalData.STARTUP_SOURCE_ACTIVATOR) ||
            (startupSource == GlobalData.STARTUP_SOURCE_EDITOR) ||
            (startupSource == GlobalData.STARTUP_SOURCE_SERVICE))
        {
            // aktivita spustena z shortcutu alebo zo service, profil aktivujeme
            actProfile = true;
            interactive = ((startupSource != GlobalData.STARTUP_SOURCE_SERVICE));
        }
        else
        if (startupSource == GlobalData.STARTUP_SOURCE_BOOT)
        {
            // boot telefonu

            ProfileDurationAlarmBroadcastReceiver.removeAlarm(context);
            GlobalData.setActivatedProfileForDuration(context, 0);

            if (GlobalData.applicationActivate)
            {
                // je nastavene, ze pri starte sa ma aktivita aktivovat
                actProfile = true;
            }
            else
            {
                if (profile != null)
                {
                    getDatabaseHandler().deactivateProfile();
                    //profile._checked = false;
                    profile = null;
                }
            }
        }
        else
        if (startupSource == GlobalData.STARTUP_SOURCE_ACTIVATOR_START)
        {
            // aktivita bola spustena po boote telefonu

            ProfileDurationAlarmBroadcastReceiver.removeAlarm(context);
            GlobalData.setActivatedProfileForDuration(context, 0);

            if (GlobalData.applicationActivate)
            {
                // je nastavene, ze pri starte sa ma aktivita aktivovat
                actProfile = true;
            }
            else
            {
                if (profile != null)
                {
                    getDatabaseHandler().deactivateProfile();
                    //profile._checked = false;
                    profile = null;
                }
            }
        }

        if ((startupSource == GlobalData.STARTUP_SOURCE_SHORTCUT) ||
            (startupSource == GlobalData.STARTUP_SOURCE_WIDGET) ||
            (startupSource == GlobalData.STARTUP_SOURCE_ACTIVATOR) ||
            (startupSource == GlobalData.STARTUP_SOURCE_EDITOR) ||
            (startupSource == GlobalData.STARTUP_SOURCE_SERVICE))
        {
            if (profile_id == 0)
                profile = null;
            else
                profile = getProfileById(profile_id);
        }


        if (actProfile && (profile != null))
        {
            // aktivacia profilu
            activateProfileWithAlert(profile, startupSource, interactive, activity);
        }
        else
        {
            activateProfileHelper.showNotification(profile);
            activateProfileHelper.updateWidget();

            // for startActivityForResult
            if (activity != null)
            {
                Intent returnIntent = new Intent();
                returnIntent.putExtra(GlobalData.EXTRA_PROFILE_ID, profile_id);
                returnIntent.getIntExtra(GlobalData.EXTRA_STARTUP_SOURCE, startupSource);
                activity.setResult(Activity.RESULT_OK,returnIntent);
            }

            finishActivity(startupSource, true, activity);
        }

    }
}
