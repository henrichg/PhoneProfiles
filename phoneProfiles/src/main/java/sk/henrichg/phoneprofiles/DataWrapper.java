package sk.henrichg.phoneprofiles;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.media.AudioManager;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import java.util.Iterator;
import java.util.List;

public class DataWrapper {

    public Context context = null;
    private boolean forGUI = false;
    private boolean monochrome = false;
    private int monochromeValue = 0xFF;

    private DatabaseHandler databaseHandler = null;
    private ActivateProfileHelper activateProfileHelper = null;
    private List<Profile> profileList = null;

    DataWrapper(Context c, boolean fgui, boolean mono, int monoVal)
    {
        //long nanoTimeStart = PPApplication.startMeasuringRunTime();

        context = c;

        setParameters(fgui, mono, monoVal);

        databaseHandler = getDatabaseHandler();
        //activateProfileHelper = getActivateProfileHelper();

        //PPApplication.getMeasuredRunTime(nanoTimeStart, "ProfilesDataWrapper.constructor");
    }

    void setParameters(
            boolean fgui,
            boolean mono,
            int monoVal)
    {
        forGUI = fgui;
        monochrome = mono;
        monochromeValue = monoVal;
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
        //long nanoTimeStart = PPApplication.startMeasuringRunTime();

        if (profileList == null)
        {
            profileList = getNewProfileList();
        }

        //PPApplication.getMeasuredRunTime(nanoTimeStart, "ProfilesDataWrapper.getProfileList");

        return profileList;
    }

    List<Profile> getNewProfileList() {
        List<Profile> newProfileList = getDatabaseHandler().getAllProfiles();

        if (forGUI)
        {
            for (Iterator<Profile> it = newProfileList.iterator(); it.hasNext();) {
                Profile profile = it.next();
                profile.generateIconBitmap(context, monochrome, monochromeValue);
                //if (generateIndicators)
                profile.generatePreferencesIndicator(context, monochrome, monochromeValue);
            }
        }
        return newProfileList;
    }


    void setProfileList(List<Profile> profileList/*, boolean recycleBitmaps*/)
    {
        /*if (recycleBitmaps)
            clearProfileList();
        else*/
            if (this.profileList != null)
                this.profileList.clear();
        this.profileList = profileList;
    }

    static Profile getNonInitializedProfile(String name, String icon, int order)
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
                  "-",
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
                  Profile.AFTERDURATIONDO_UNDOPROFILE,
                  0,
                  0,
                  0,
                  0,
                  0,
                  false,
                  0,
                  0,
                  0,
                  0,
                  false,
                  0,
                  Profile.CONNECTTOSSID_JUSTANY,
                  "",
                  false,
                  0
            );
    }

    private String getVolumeLevelString(int percentage, int maxValue)
    {
        Double dValue = maxValue / 100.0 * percentage;
        return String.valueOf(dValue.intValue());
    }

    Profile  getPredefinedProfile(int index, boolean saveToDB)
    {
        int	maximumValueRing = 7;
        int	maximumValueNotification = 7;
        int	maximumValueMusic = 15;
        int	maximumValueAlarm = 7;
        //int	maximumValueSystem = 7;
        //int	maximumValueVoiceCall = 7;
        AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            maximumValueRing = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
            maximumValueNotification = audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);
            maximumValueMusic = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            maximumValueAlarm = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
            //maximumValueSystem = audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
            //maximumValueVoiceCall = audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
        }

        Profile profile;

        switch (index) {
            case 0:
                profile = getNonInitializedProfile(context.getString(R.string.default_profile_name_home), "ic_profile_home_2", 1);
                //if (android.os.Build.VERSION.SDK_INT >= 18) {
                    if (ActivateProfileHelper.canChangeZenMode(context, true)) {
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
                //} else
                //    profile._volumeRingerMode = 1;
                profile._volumeRingtone = getVolumeLevelString(71, maximumValueRing) + "|0|0";
                profile._volumeNotification = getVolumeLevelString(86, maximumValueNotification) + "|0|0";
                profile._volumeAlarm = getVolumeLevelString(100, maximumValueAlarm) + "|0|0";
                profile._volumeMedia = getVolumeLevelString(80, maximumValueMusic) + "|0|0";
                profile._deviceWiFi = 1;
                //profile._deviceBrightness = "60|0|0|0";
                break;
            case 1:
                profile = getNonInitializedProfile(context.getString(R.string.default_profile_name_outdoor), "ic_profile_outdoors_1", 2);
                //if (android.os.Build.VERSION.SDK_INT >= 18) {
                    if (ActivateProfileHelper.canChangeZenMode(context, true)) {
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
                //} else
                //    profile._volumeRingerMode = 2;
                profile._volumeRingtone = getVolumeLevelString(100, maximumValueRing) + "|0|0";
                profile._volumeNotification = getVolumeLevelString(100, maximumValueNotification) + "|0|0";
                profile._volumeAlarm = getVolumeLevelString(100, maximumValueAlarm) + "|0|0";
                profile._volumeMedia = getVolumeLevelString(93, maximumValueMusic) + "|0|0";
                profile._deviceWiFi = 2;
                //profile._deviceBrightness = "255|0|0|0";
                break;
            case 2:
                profile = getNonInitializedProfile(context.getString(R.string.default_profile_name_work), "ic_profile_work_5", 3);
                //if (android.os.Build.VERSION.SDK_INT >= 18) {
                    if (ActivateProfileHelper.canChangeZenMode(context, true)) {
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
                //} else
                //    profile._volumeRingerMode = 1;
                profile._volumeRingtone = getVolumeLevelString(57, maximumValueRing) + "|0|0";
                profile._volumeNotification = getVolumeLevelString(71, maximumValueNotification) + "|0|0";
                profile._volumeAlarm = getVolumeLevelString(57, maximumValueAlarm) + "|0|0";
                profile._volumeMedia = getVolumeLevelString(80, maximumValueMusic) + "|0|0";
                profile._deviceWiFi = 2;
                //profile._deviceBrightness = "60|0|0|0";
                break;
            case 3:
                profile = getNonInitializedProfile(context.getString(R.string.default_profile_name_meeting), "ic_profile_meeting_2", 4);
                //if (android.os.Build.VERSION.SDK_INT >= 18) {
                    if (ActivateProfileHelper.canChangeZenMode(context, true)) {
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
                //} else
                //    profile._volumeRingerMode = 4;
                profile._volumeRingtone = getVolumeLevelString(57, maximumValueRing) + "|0|0";
                profile._volumeNotification = getVolumeLevelString(71, maximumValueNotification) + "|0|0";
                profile._volumeAlarm = getVolumeLevelString(57, maximumValueAlarm) + "|0|0";
                profile._volumeMedia = getVolumeLevelString(80, maximumValueMusic) + "|0|0";
                profile._deviceWiFi = 0;
                //profile._deviceBrightness = Profile.BRIGHTNESS_ADAPTIVE_BRIGHTNESS_NOT_SET+"|1|1|0";
                break;
            case 4:
                profile = getNonInitializedProfile(context.getString(R.string.default_profile_name_sleep), "ic_profile_sleep", 5);
                //if (android.os.Build.VERSION.SDK_INT >= 18) {
                    if (ActivateProfileHelper.canChangeZenMode(context, true)) {
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
                //} else
                //    profile._volumeRingerMode = 4;
                profile._volumeRingtone = getVolumeLevelString(71, maximumValueRing) + "|0|0";
                profile._volumeNotification = getVolumeLevelString(86, maximumValueNotification) + "|0|0";
                profile._volumeAlarm = getVolumeLevelString(100, maximumValueAlarm) + "|0|0";
                profile._volumeMedia = getVolumeLevelString(80, maximumValueMusic) + "|0|0";
                profile._deviceWiFi = 0;
                //profile._deviceBrightness = "10|0|0|0";
                break;
            case 5:
                profile = getNonInitializedProfile(context.getString(R.string.default_profile_name_battery_low), "ic_profile_battery_1", 6);
                profile._deviceAutoSync = 2;
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

    void getPredefinedProfileList()
    {
        clearProfileList();
        getDatabaseHandler().deleteAllProfiles();

        for (int index = 0; index < 6; index++)
            getPredefinedProfile(index, true);

        getProfileList();
    }

    void clearProfileList()
    {
        if (profileList != null)
        {
            for(Iterator<Profile> it = profileList.iterator(); it.hasNext();) {
                Profile profile = it.next();
                profile.releaseIconBitmap();
                profile.releasePreferencesIndicator();
                it.remove();
            }
        }
        profileList = null;
    }

    private Profile getActivatedProfileFromDB()
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

    /*
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
    */

    private int getItemPosition(Profile profile)
    {
        if (profile == null)
            return -1;

        if (profileList == null)
            return getDatabaseHandler().getProfilePosition(profile);
        else
        {
            for (int i = 0; i < profileList.size(); i++)
            {
                if (profileList.get(i)._id == profile._id)
                    return i;
            }
            return -1;
        }
    }

    private void setProfileActive(Profile profile)
    {
        if ((profileList == null) || (profile == null))
            return;

        for (Profile p : profileList)
        {
            p._checked = false;
        }

        int position = getItemPosition(profile);
        if (position != -1)
        {
            Profile _profile = profileList.get(position);
            if (_profile != null)
                _profile._checked = true;
        }
    }

    Profile getProfileById(long id)
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

    void updateProfile(Profile profile)
    {
        if (profile != null)
        {
            Profile origProfile = getProfileById(profile._id);
            if (origProfile != null)
                origProfile.copyProfile(profile);
        }
    }

    /*
    public void reloadProfilesData()
    {
        clearProfileList();
        getProfileList();
    }
    */

    void deleteProfile(Profile profile)
    {
        if (profile == null)
            return;

        profileList.remove(profile);

        // unlink profile from Background profile
        if (Long.valueOf(ApplicationPreferences.applicationBackgroundProfile(context)) == profile._id)
        {
            ApplicationPreferences.getSharedPreferences(context);
            Editor editor = ApplicationPreferences.preferences.edit();
            editor.putString(ApplicationPreferences.PREF_APPLICATION_BACKGROUND_PROFILE, String.valueOf(Profile.PROFILE_NO_ACTIVATE));
            editor.apply();
        }
    }

    void deleteAllProfiles()
    {
        profileList.clear();

        // unlink profiles from Background profile
        ApplicationPreferences.getSharedPreferences(context);
        Editor editor = ApplicationPreferences.preferences.edit();
        editor.putString(ApplicationPreferences.PREF_APPLICATION_BACKGROUND_PROFILE, String.valueOf(Profile.PROFILE_NO_ACTIVATE));
        editor.apply();
    }

    public void invalidateDataWrapper()
    {
        clearProfileList();
        databaseHandler = null;
        if (activateProfileHelper != null)
            activateProfileHelper.deinitialize();
        activateProfileHelper = null;
    }

    void refreshProfileIcon(Profile profile, boolean monochrome, int monochromeValue) {
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

    void _activateProfile(Profile _profile, int startupSource, /*boolean _interactive,*/ Activity _activity)
    {
        // remove last configured profile duration alarm
        ProfileDurationAlarmBroadcastReceiver.removeAlarm(context);
        Profile.setActivatedProfileForDuration(context, 0);

        Profile profile = Profile.getMappedProfile(_profile, context);

        Profile activatedProfile = getActivatedProfile();

        databaseHandler.activateProfile(profile);
        setProfileActive(profile);

        activateProfileHelper.execute(profile/*, _interactive*/);

        /*if (_interactive)
        {*/
            long profileId = 0;
            if (activatedProfile != null)
                profileId = activatedProfile._id;
            Profile.setActivatedProfileForDuration(context, profileId);
            ProfileDurationAlarmBroadcastReceiver.setAlarm(profile, context);
        //}

        if (PhoneProfilesService.instance != null)
            PhoneProfilesService.instance.showProfileNotification(profile, this);
        activateProfileHelper.updateWidget(true);

        if (ApplicationPreferences.notificationsToast(context) && (!ActivateProfileHelper.lockRefresh))
        {
            // toast notification
            if (PPApplication.toastHandler != null)
            {
                final Profile __profile = _profile;
                PPApplication.toastHandler.post(new Runnable() {
                    public void run() {
                        showToastAfterActivation(__profile);
                    }
                });
            }
            //else
            //    showToastAfterActivation(_profile);
        }

        // for startActivityForResult
        if (_activity != null)
        {
            Intent returnIntent = new Intent();
            returnIntent.putExtra(PPApplication.EXTRA_PROFILE_ID, _profile._id);
            returnIntent.getIntExtra(PPApplication.EXTRA_STARTUP_SOURCE, startupSource);
            _activity.setResult(Activity.RESULT_OK,returnIntent);
        }

        finishActivity(startupSource, true, _activity);
    }

    private void showToastAfterActivation(Profile profile)
    {
        Toast msg = Toast.makeText(context,
                context.getResources().getString(R.string.toast_profile_activated_0) + ": " + profile._name + " " +
                        context.getResources().getString(R.string.toast_profile_activated_1),
                Toast.LENGTH_SHORT);
        msg.show();
    }

    private void activateProfileWithAlert(Profile profile, int startupSource, /*boolean interactive,*/ Activity activity)
    {
        if (/*interactive &&*/ (ApplicationPreferences.applicationActivateWithAlert(context) ||
                            (startupSource == PPApplication.STARTUP_SOURCE_EDITOR)))
        {
            // set theme and language for dialog alert ;-)
            // not working on Android 2.3.x
            GlobalGUIRoutines.setTheme(activity, true, false);
            GlobalGUIRoutines.setLanguage(activity.getBaseContext());

            final Profile _profile = profile;
            final int _startupSource = startupSource;
            final Activity _activity = activity;
            final DataWrapper _dataWrapper = this;

            if (_profile._askForDuration) {
                FastAccessDurationDialog dlg = new FastAccessDurationDialog(_activity, _profile, _dataWrapper,
                        monochrome, monochromeValue, _startupSource);
                dlg.show();
            }
            else {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
                dialogBuilder.setTitle(activity.getResources().getString(R.string.profile_string_0) + ": " + profile._name);
                dialogBuilder.setMessage(R.string.activate_profile_alert_message);
                //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                dialogBuilder.setPositiveButton(R.string.alert_button_yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        if (Permissions.grantProfilePermissions(context, _profile, false,
                                forGUI, monochrome, monochromeValue,
                                _startupSource, /*true,*/ _activity, true))
                            _dataWrapper._activateProfile(_profile, _startupSource, /*true,*/ _activity);
                        else {
                            Intent returnIntent = new Intent();
                            _activity.setResult(Activity.RESULT_CANCELED, returnIntent);

                            finishActivity(_startupSource, false, _activity);
                        }
                    }
                });
                dialogBuilder.setNegativeButton(R.string.alert_button_no, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {

                        // for startActivityForResult
                        Intent returnIntent = new Intent();
                        _activity.setResult(Activity.RESULT_CANCELED, returnIntent);

                        finishActivity(_startupSource, false, _activity);
                    }
                });
                dialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {

                    public void onCancel(DialogInterface dialog) {
                        // for startActivityForResult
                        Intent returnIntent = new Intent();
                        _activity.setResult(Activity.RESULT_CANCELED, returnIntent);

                        finishActivity(_startupSource, false, _activity);
                    }
                });
                dialogBuilder.show();
            }
        }
        else
        {
            if (profile._askForDuration/* && interactive*/) {
                FastAccessDurationDialog dlg = new FastAccessDurationDialog(activity, profile, this,
                        monochrome, monochromeValue, startupSource);
                dlg.show();
            }
            else {
                boolean granted;
                //if (interactive) {
                    // set theme and language for dialog alert ;-)
                    // not working on Android 2.3.x
                    GlobalGUIRoutines.setTheme(activity, true, false);
                    GlobalGUIRoutines.setLanguage(activity.getBaseContext());

                    granted = Permissions.grantProfilePermissions(context, profile, false,
                            forGUI, monochrome, monochromeValue,
                            startupSource, /*true,*/ activity, true);
                /*}
                else
                    granted = Permissions.grantProfilePermissions(context, profile, true,
                            forGUI, monochrome, monochromeValue,
                            startupSource, false, null, true);*/
                if (granted)
                    _activateProfile(profile, startupSource, /*interactive,*/ activity);
            }
        }
    }

    void finishActivity(int startupSource, boolean afterActivation, Activity activity)
    {
        if (activity == null)
            return;

        boolean finish = true;

        if (startupSource == PPApplication.STARTUP_SOURCE_ACTIVATOR_START)
        {
            finish = false;
        }
        else
        if (startupSource == PPApplication.STARTUP_SOURCE_ACTIVATOR)
        {
            finish = false;
            if (ApplicationPreferences.applicationClose(context))
            {
                if (PPApplication.getApplicationStarted(activity.getApplicationContext(), false))
                    finish = afterActivation;
            }
        }
        else
        if (startupSource == PPApplication.STARTUP_SOURCE_EDITOR)
        {
            finish = false;
        }

        if (finish)
            activity.finish();
    }

    public void activateProfile(long profile_id, int startupSource, Activity activity)
    {
        Profile profile;

        // for activated profile, update of activity is required
        profile = getActivatedProfile();

        long backgroundProfileId = Long.valueOf(ApplicationPreferences.applicationBackgroundProfile(context));
        if ((profile == null) &&
            (backgroundProfileId != Profile.PROFILE_NO_ACTIVATE))
        {
            profile = getProfileById(backgroundProfileId);
        }

        boolean actProfile = false;
        //boolean interactive = false;
        if ((startupSource == PPApplication.STARTUP_SOURCE_SHORTCUT) ||
            (startupSource == PPApplication.STARTUP_SOURCE_WIDGET) ||
            (startupSource == PPApplication.STARTUP_SOURCE_ACTIVATOR) ||
            (startupSource == PPApplication.STARTUP_SOURCE_EDITOR) ||
            (startupSource == PPApplication.STARTUP_SOURCE_SERVICE) ||
            (startupSource == PPApplication.STARTUP_SOURCE_SERVICE_MANUAL))
        {
            // activity started from shortcut or service, activate profile
            actProfile = true;
            //interactive = ((startupSource != PPApplication.STARTUP_SOURCE_SERVICE));
        }
        else
        if (startupSource == PPApplication.STARTUP_SOURCE_BOOT)
        {
            // device boot

            ProfileDurationAlarmBroadcastReceiver.removeAlarm(context);
            Profile.setActivatedProfileForDuration(context, 0);

            if (ApplicationPreferences.applicationActivate(context))
            {
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
        if (startupSource == PPApplication.STARTUP_SOURCE_ACTIVATOR_START)
        {
            // activity started after device boot

            ProfileDurationAlarmBroadcastReceiver.removeAlarm(context);
            Profile.setActivatedProfileForDuration(context, 0);

            if (ApplicationPreferences.applicationActivate(context))
            {
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

        if ((startupSource == PPApplication.STARTUP_SOURCE_SHORTCUT) ||
            (startupSource == PPApplication.STARTUP_SOURCE_WIDGET) ||
            (startupSource == PPApplication.STARTUP_SOURCE_ACTIVATOR) ||
            (startupSource == PPApplication.STARTUP_SOURCE_EDITOR) ||
            (startupSource == PPApplication.STARTUP_SOURCE_SERVICE))
        {
            if (profile_id == 0)
                profile = null;
            else
                profile = getProfileById(profile_id);
        }


        if (actProfile && (profile != null))
        {
            if (startupSource == PPApplication.STARTUP_SOURCE_BOOT)
                _activateProfile(profile, PPApplication.STARTUP_SOURCE_BOOT, /*boolean _interactive,*/ null);
            else
                activateProfileWithAlert(profile, startupSource, /*interactive,*/ activity);
        }
        else
        {
            if (PhoneProfilesService.instance != null)
                PhoneProfilesService.instance.showProfileNotification(profile, this);
            activateProfileHelper.updateWidget(true);

            // for startActivityForResult
            if (activity != null)
            {
                Intent returnIntent = new Intent();
                returnIntent.putExtra(PPApplication.EXTRA_PROFILE_ID, profile_id);
                returnIntent.getIntExtra(PPApplication.EXTRA_STARTUP_SOURCE, startupSource);
                activity.setResult(Activity.RESULT_OK,returnIntent);
            }

            finishActivity(startupSource, true, activity);
        }

    }

    void activateProfileAfterDuration(long profile_id)
    {
        int startupSource = PPApplication.STARTUP_SOURCE_SERVICE_MANUAL;
        Profile profile = getProfileById(profile_id);
        if (profile == null) return;
        if (Permissions.grantProfilePermissions(context, profile, true,
                forGUI, monochrome, monochromeValue,
                startupSource, /*true,*/ null, true)) {
            getActivateProfileHelper().initialize(context);
            _activateProfile(profile, startupSource, /*true,*/ null);
        }
    }

}
