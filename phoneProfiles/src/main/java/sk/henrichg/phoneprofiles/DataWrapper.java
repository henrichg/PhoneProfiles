package sk.henrichg.phoneprofiles;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Icon;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static android.content.Context.POWER_SERVICE;

public class DataWrapper {

    public final Context context;
    //private boolean forGUI = false;
    private boolean monochrome = false;
    private int monochromeValue = 0xFF;
    private boolean useMonochromeValueForCustomIcon = false;

    boolean profileListFilled = false;
    final List<Profile> profileList = Collections.synchronizedList(new ArrayList<Profile>());

    DataWrapper(Context c, /*boolean fgui,*/ boolean mono, int monoVal,
                boolean useMonoValForCustomIcon)
    {
        //long nanoTimeStart = PPApplication.startMeasuringRunTime();

        context = c.getApplicationContext();

        setParameters(/*fgui,*/ mono, monoVal, useMonoValForCustomIcon);

        //activateProfileHelper = getActivateProfileHelper();

        //PPApplication.getMeasuredRunTime(nanoTimeStart, "ProfilesDataWrapper.constructor");
    }

    void setParameters(
            //boolean fgui,
            boolean mono,
            int monoVal,
            boolean useMonoValForCustomIcon)
    {
        //forGUI = fgui;
        monochrome = mono;
        monochromeValue = monoVal;
        useMonochromeValueForCustomIcon = useMonoValForCustomIcon;
    }

    private DataWrapper copyDataWrapper() {
        DataWrapper dataWrapper = new DataWrapper(context, monochrome, monochromeValue, useMonochromeValueForCustomIcon);
        synchronized (profileList) {
            dataWrapper.copyProfileList(this);
        }
        return dataWrapper;
    }

    void fillProfileList(boolean generateIcons, boolean generateIndicators)
    {
        //long nanoTimeStart = PPApplication.startMeasuringRunTime();

        synchronized (profileList) {
            if (!profileListFilled)
            {
                profileList.addAll(getNewProfileList(generateIcons, generateIndicators));
                profileListFilled = true;
            }
        }

        //PPApplication.getMeasuredRunTime(nanoTimeStart, "ProfilesDataWrapper.getProfileList");
    }

    List<Profile> getNewProfileList(boolean generateIcons, boolean generateIndicators) {
        List<Profile> newProfileList = DatabaseHandler.getInstance(context).getAllProfiles();

        //if (forGUI)
        //{
            //noinspection ForLoopReplaceableByForEach
            for (Iterator<Profile> it = newProfileList.iterator(); it.hasNext();) {
                Profile profile = it.next();
                if (generateIcons)
                    profile.generateIconBitmap(context, monochrome, monochromeValue, useMonochromeValueForCustomIcon);
                if (generateIndicators)
                    profile.generatePreferencesIndicator(context, monochrome, monochromeValue);
            }
        //}
        return newProfileList;
    }


    void setProfileList(List<Profile> sourceProfileList)
    {
        synchronized (profileList) {
            if (profileListFilled)
                profileList.clear();
            profileList.addAll(sourceProfileList);
            profileListFilled = true;
        }
    }

    void copyProfileList(DataWrapper fromDataWrapper)
    {
        synchronized (profileList) {
            if (profileListFilled) {
                profileList.clear();
                profileListFilled = false;
            }
            if (fromDataWrapper.profileListFilled) {
                profileList.addAll(fromDataWrapper.profileList);
                profileListFilled = true;
            }
        }
    }

    static Profile getNonInitializedProfile(String name, String icon, int order)
    {
        return new Profile(
                name,
                icon + Profile.defaultValuesString.get("prf_pref_profileIcon_withoutIcon"),
                false,
                order,
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_volumeRingerMode")),
                Profile.defaultValuesString.get("prf_pref_volumeRingtone"),
                Profile.defaultValuesString.get("prf_pref_volumeNotification"),
                Profile.defaultValuesString.get("prf_pref_volumeMedia"),
                Profile.defaultValuesString.get("prf_pref_volumeAlarm"),
                Profile.defaultValuesString.get("prf_pref_volumeSystem"),
                Profile.defaultValuesString.get("prf_pref_volumeVoice"),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_soundRingtoneChange")),
                Settings.System.DEFAULT_RINGTONE_URI.toString(),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_soundNotificationChange")),
                Settings.System.DEFAULT_NOTIFICATION_URI.toString(),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_soundAlarmChange")),
                Settings.System.DEFAULT_ALARM_ALERT_URI.toString(),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_deviceAirplaneMode")),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_deviceWiFi")),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_deviceBluetooth")),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_deviceScreenTimeout")),
                Profile.BRIGHTNESS_ADAPTIVE_BRIGHTNESS_NOT_SET + Profile.defaultValuesString.get("prf_pref_deviceBrightness_withoutLevel"),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_deviceWallpaperChange")),
                Profile.defaultValuesString.get("prf_pref_deviceWallpaper"),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_deviceMobileData")),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_deviceMobileDataPrefs")),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_deviceGPS")),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_deviceRunApplicationChange")),
                Profile.defaultValuesString.get("prf_pref_deviceRunApplicationPackageName"),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_deviceAutosync")),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_deviceAutoRotation")),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_deviceLocationServicePrefs")),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_volumeSpeakerPhone")),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_deviceNFC")),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_duration")),
                Profile.AFTERDURATIONDO_UNDOPROFILE,
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_volumeZenMode")),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_deviceKeyguard")),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_vibrationOnTouch")),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_deviceWiFiAP")),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_devicePowerSaveMode")),
                Profile.defaultValuesBoolean.get("prf_pref_askForDuration"),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_deviceNetworkType")),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_notificationLed")),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_vibrateWhenRinging")),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_deviceWallpaperFor")),
                Profile.defaultValuesBoolean.get("prf_pref_hideStatusBarIcon"),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_lockDevice")),
                Profile.defaultValuesString.get("prf_pref_deviceConnectToSSID"),
                Profile.defaultValuesString.get("prf_pref_durationNotificationSound"),
                Profile.defaultValuesBoolean.get("prf_pref_durationNotificationVibrate"),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_deviceWiFiAPPrefs")),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_headsUpNotifications")),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_deviceForceStopApplicationChange")),
                Profile.defaultValuesString.get("prf_pref_deviceForceStopApplicationPackageName"),
                0,
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_deviceNetworkTypePrefs")),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_deviceCloseAllApplications")),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_screenNightMode")),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_dtmfToneWhenDialing")),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_soundOnTouch"))
            );
    }

    private String getVolumeLevelString(int percentage, int maxValue)
    {
        Double dValue = maxValue / 100.0 * percentage;
        return String.valueOf(dValue.intValue());
    }

    Profile  getPredefinedProfile(int index, boolean saveToDB, Context baseContext)
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
                profile = getNonInitializedProfile(baseContext.getString(R.string.default_profile_name_home), "ic_profile_home_2", 1);
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
                profile = getNonInitializedProfile(baseContext.getString(R.string.default_profile_name_outdoor), "ic_profile_outdoors_1", 2);
                //if (android.os.Build.VERSION.SDK_INT >= 18) {
                    if (ActivateProfileHelper.canChangeZenMode(context, true)) {
                        if (android.os.Build.VERSION.SDK_INT >= 23) {
                            profile._volumeRingerMode = 5;
                            profile._volumeZenMode = 1; // ALL
                        } else if (android.os.Build.VERSION.SDK_INT >= 21) {
                            profile._volumeRingerMode = 5;
                            profile._volumeZenMode = 1; // ALL
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
                profile = getNonInitializedProfile(baseContext.getString(R.string.default_profile_name_work), "ic_profile_work_5", 3);
                //if (android.os.Build.VERSION.SDK_INT >= 18) {
                    if (ActivateProfileHelper.canChangeZenMode(context, true)) {
                        if (android.os.Build.VERSION.SDK_INT >= 23) {
                            profile._volumeRingerMode = 5;
                            profile._volumeZenMode = 4; // ALL with vibration
                        } else if (android.os.Build.VERSION.SDK_INT >= 21) {
                            profile._volumeRingerMode = 5;
                            profile._volumeZenMode = 4; // ALL with vibration
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
                profile = getNonInitializedProfile(baseContext.getString(R.string.default_profile_name_meeting), "ic_profile_meeting_2", 4);
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
                profile = getNonInitializedProfile(baseContext.getString(R.string.default_profile_name_sleep), "ic_profile_sleep", 5);
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
                profile = getNonInitializedProfile(baseContext.getString(R.string.default_profile_name_battery_low), "ic_profile_battery_1", 6);
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
                DatabaseHandler.getInstance(context).addProfile(profile);
        }

        return profile;
    }

    void fillPredefinedProfileList(@SuppressWarnings("SameParameterValue") boolean generateIcons,
                                   boolean generateIndicators,
                                   Context baseContext)
    {
        synchronized (profileList) {
            clearProfileList();
            DatabaseHandler.getInstance(context).deleteAllProfiles();

            for (int index = 0; index < 6; index++)
                getPredefinedProfile(index, true, baseContext);

            fillProfileList(generateIcons, generateIndicators);
        }
    }

    void clearProfileList()
    {
        synchronized (profileList) {
            if (profileListFilled)
            {
                for (Iterator<Profile> it = profileList.iterator(); it.hasNext(); ) {
                    Profile profile = it.next();
                    profile.releaseIconBitmap();
                    profile.releasePreferencesIndicator();
                    it.remove();
                }
            }
            profileListFilled = false;
        }
    }

    Profile getActivatedProfileFromDB(boolean generateIcon, boolean generateIndicators)
    {
        Profile profile = DatabaseHandler.getInstance(context).getActivatedProfile();
        if (/*forGUI &&*/ (profile != null))
        {
            if (generateIcon)
                profile.generateIconBitmap(context, monochrome, monochromeValue, useMonochromeValueForCustomIcon);
            if (generateIndicators)
                profile.generatePreferencesIndicator(context, monochrome, monochromeValue);
        }
        return profile;
    }

    public Profile getActivatedProfile(boolean generateIcon, boolean generateIndicators)
    {
        synchronized (profileList) {
            if (!profileListFilled) {
                return getActivatedProfileFromDB(generateIcon, generateIndicators);
            } else {
                //noinspection ForLoopReplaceableByForEach
                for (Iterator<Profile> it = profileList.iterator(); it.hasNext(); ) {
                    Profile profile = it.next();
                    if (profile._checked) {
                        return profile;
                    }
                }
                // when profile not found, get profile from db
                return getActivatedProfileFromDB(generateIcon, generateIndicators);
            }
        }
    }

    /*
    public Profile getFirstProfile()
    {
        if (profileList == null)
        {
            Profile profile = DatabaseHandler.getInstance(context).getFirstProfile();
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

    /*
    private int getItemPosition(Profile profile)
    {
        if (profile == null)
            return -1;

        if (profileList == null)
            return DatabaseHandler.getInstance(context).getProfilePosition(profile);
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
    */

    private void setProfileActive(Profile profile)
    {
        synchronized (profileList) {
            if (!profileListFilled)
                return;

            //noinspection ForLoopReplaceableByForEach
            for (Iterator<Profile> it = profileList.iterator(); it.hasNext();) {
                Profile _profile = it.next();
                _profile._checked = false;
            }

            if (profile != null)
                profile._checked = true;
        }
    }

    Profile getProfileById(long id, boolean generateIcon, boolean generateIndicators)
    {
        synchronized (profileList) {
            if (!profileListFilled) {
                Profile profile = DatabaseHandler.getInstance(context).getProfile(id);
                if (/*forGUI &&*/ (profile != null)) {
                    if (generateIcon)
                        profile.generateIconBitmap(context, monochrome, monochromeValue, useMonochromeValueForCustomIcon);
                    if (generateIndicators)
                        profile.generatePreferencesIndicator(context, monochrome, monochromeValue);
                }
                return profile;
            } else {
                //noinspection ForLoopReplaceableByForEach
                for (Iterator<Profile> it = profileList.iterator(); it.hasNext(); ) {
                    Profile profile = it.next();
                    if (profile._id == id)
                        return profile;
                }
                return null;
            }
        }
    }

    void updateProfile(Profile profile)
    {
        if (profile != null)
        {
            Profile origProfile = getProfileById(profile._id, false, false);
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

        synchronized (profileList) {
            profileList.remove(profile);
        }

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
        synchronized (profileList) {
            profileList.clear();
        }

        // unlink profiles from Background profile
        ApplicationPreferences.getSharedPreferences(context);
        Editor editor = ApplicationPreferences.preferences.edit();
        editor.putString(ApplicationPreferences.PREF_APPLICATION_BACKGROUND_PROFILE, String.valueOf(Profile.PROFILE_NO_ACTIVATE));
        editor.apply();
    }

    public void invalidateDataWrapper()
    {
        clearProfileList();
    }

    void refreshProfileIcon(Profile profile,
                            @SuppressWarnings("SameParameterValue") boolean generateIcon,
                            boolean generateIndicators) {
        if (profile != null) {
            boolean isIconResourceID = profile.getIsIconResourceID();
            String iconIdentifier = profile.getIconIdentifier();
            DatabaseHandler.getInstance(context).getProfileIcon(profile);
            if (isIconResourceID && iconIdentifier.equals("ic_profile_default") && (!profile.getIsIconResourceID())) {
                if (generateIcon)
                    profile.generateIconBitmap(context, monochrome, monochromeValue, useMonochromeValueForCustomIcon);
                if (generateIndicators)
                    profile.generatePreferencesIndicator(context, monochrome, monochromeValue);
            }
        }
    }

//----- Activate profile ---------------------------------------------------------------------------------------------

    void _activateProfile(final Profile _profile, int startupSource, final boolean interactive, Activity _activity)
    {
        // remove last configured profile duration alarm
        ProfileDurationAlarmBroadcastReceiver.removeAlarm(context);
        Profile.setActivatedProfileForDuration(context, 0);

        final Profile profile = Profile.getMappedProfile(_profile, context);

        // get currently activated profile
        Profile activatedProfile = getActivatedProfile(false, false);

        DatabaseHandler.getInstance(context).activateProfile(_profile);
        setProfileActive(_profile);

        if (activatedProfile != null) {
            long profileId = activatedProfile._id;
            Profile.setActivatedProfileForDuration(context, profileId);
        }
        else
            Profile.setActivatedProfileForDuration(context, 0);
        ProfileDurationAlarmBroadcastReceiver.setAlarm(profile, context);

        PPApplication.showProfileNotification(context);
        ActivateProfileHelper.updateGUI(context, true);

        final Context _context = context;

        PPApplication.startHandlerThread();
        Handler handler = new Handler(PPApplication.handlerThread.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {

                PowerManager powerManager = (PowerManager) _context.getSystemService(POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                if (powerManager != null) {
                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME+":DataWrapper._activateProfile.1");
                    wakeLock.acquire(10 * 60 * 1000);
                }

                ActivateProfileHelper.execute(_context, profile/*, _interactive*/);

                if ((wakeLock != null) && wakeLock.isHeld()) {
                    try {
                        wakeLock.release();
                    } catch (Exception ignored) {}
                }
            }
        });

        if (ApplicationPreferences.notificationsToast(context) && (!ActivateProfileHelper.lockRefresh))
        {
            // toast notification
            if (PPApplication.toastHandler != null)
            {
                PPApplication.toastHandler.post(new Runnable() {
                    public void run() {
                        showToastAfterActivation(profile);
                    }
                });
            }
            //else
            //    showToastAfterActivation(_profile);
        }

        if (interactive) {
            final DataWrapper dataWrapper = copyDataWrapper();

            PPApplication.startHandlerThread();
            handler = new Handler(PPApplication.handlerThread.getLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    PowerManager powerManager = (PowerManager) _context.getSystemService(POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME+":DataWrapper._activateProfile.2");
                        wakeLock.acquire(10 * 60 * 1000);
                    }

                    DatabaseHandler.getInstance(_context).increaseActivationByUserCount(_profile);
                    dataWrapper.setDynamicLauncherShortcuts();

                    if ((wakeLock != null) && wakeLock.isHeld()) {
                        try {
                            wakeLock.release();
                        } catch (Exception ignored) {
                        }
                    }
                }
            });
        }

        // for startActivityForResult
        if (_activity != null)
        {
            Intent returnIntent = new Intent();
            returnIntent.putExtra(PPApplication.EXTRA_PROFILE_ID, _profile._id);
            returnIntent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, startupSource);
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
            GlobalGUIRoutines.setTheme(activity, true, true);
            GlobalGUIRoutines.setLanguage(activity.getBaseContext());

            final Profile _profile = profile;
            final int _startupSource = startupSource;
            final Activity _activity = activity;
            final DataWrapper _dataWrapper = this;

            if (_profile._askForDuration) {
                FastAccessDurationDialog dlg = new FastAccessDurationDialog(_activity, _profile, _dataWrapper,
                        /*monochrome, monochromeValue,*/ _startupSource);
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
                                /*true, monochrome, monochromeValue,*/
                                _startupSource, true, true, false))
                            _dataWrapper._activateProfile(_profile, _startupSource, true, _activity);
                        else {
                            Intent returnIntent = new Intent();
                            _activity.setResult(Activity.RESULT_CANCELED, returnIntent);

                            finishActivity(_startupSource, true, _activity);
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
        }
        else
        {
            if (profile._askForDuration/* && interactive*/) {
                FastAccessDurationDialog dlg = new FastAccessDurationDialog(activity, profile, this,
                        /*monochrome, monochromeValue,*/ startupSource);
                dlg.show();
            }
            else {
                boolean granted;
                //if (interactive) {
                    // set theme and language for dialog alert ;-)
                    // not working on Android 2.3.x
                    GlobalGUIRoutines.setTheme(activity, true, true);
                    GlobalGUIRoutines.setLanguage(activity.getBaseContext());

                    granted = Permissions.grantProfilePermissions(context, profile, false,
                            /*true, monochrome, monochromeValue,*/
                            startupSource, true, true, false);
                /*}
                else
                    granted = Permissions.grantProfilePermissions(context, profile, true,
                            forGUI, monochrome, monochromeValue,
                            startupSource, false, null, true);*/
                if (granted)
                    _activateProfile(profile, startupSource, true, activity);
                else {
                    Intent returnIntent = new Intent();
                    activity.setResult(Activity.RESULT_CANCELED, returnIntent);

                    finishActivity(startupSource, true, activity);
                }
            }
        }
    }

    void finishActivity(int startupSource, boolean closeActivator, final Activity activity)
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
                    finish = closeActivator;
            }
        }
        else
        if (startupSource == PPApplication.STARTUP_SOURCE_EDITOR)
        {
            finish = false;
        }

        if (finish) {
            final Handler handler = new Handler(context.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    activity.finish();
                }
            });
        }
    }

    public void activateProfile(long profile_id, int startupSource, Activity activity)
    {
        Profile profile;

        // for activated profile, update of activity is required
        profile = getActivatedProfile(false, false);

        long backgroundProfileId = Long.valueOf(ApplicationPreferences.applicationBackgroundProfile(context));
        if ((profile == null) &&
            (backgroundProfileId != Profile.PROFILE_NO_ACTIVATE))
        {
            profile = getProfileById(backgroundProfileId, false, false);
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
                    DatabaseHandler.getInstance(context).deactivateProfile();
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
                    DatabaseHandler.getInstance(context).deactivateProfile();
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
                profile = getProfileById(profile_id, false, false);
        }


        if (actProfile && (profile != null))
        {
            if (startupSource == PPApplication.STARTUP_SOURCE_BOOT)
                _activateProfile(profile, PPApplication.STARTUP_SOURCE_BOOT, false, null);
            else
                activateProfileWithAlert(profile, startupSource, /*interactive,*/ activity);
        }
        else
        {
            PPApplication.showProfileNotification(context);
            ActivateProfileHelper.updateGUI(context, true);

            // for startActivityForResult
            if (activity != null)
            {
                Intent returnIntent = new Intent();
                returnIntent.putExtra(PPApplication.EXTRA_PROFILE_ID, profile_id);
                returnIntent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, startupSource);
                activity.setResult(Activity.RESULT_OK,returnIntent);
            }

            finishActivity(startupSource, true, activity);
        }

    }

    void activateProfileAfterDuration(long profile_id)
    {
        int startupSource = PPApplication.STARTUP_SOURCE_SERVICE_MANUAL;
        Profile profile = getProfileById(profile_id, false, false);
        if (profile == null) {
            // remove last configured profile duration alarm
            ProfileDurationAlarmBroadcastReceiver.removeAlarm(context);
            Profile.setActivatedProfileForDuration(context, 0);
            PPApplication.showProfileNotification(context);
            ActivateProfileHelper.updateGUI(context, true);
            return;
        }
        if (Permissions.grantProfilePermissions(context, profile, true,
                /*false, monochrome, monochromeValue,*/
                startupSource, true, true, false)) {
            _activateProfile(profile, startupSource, true, null);
        }
    }

    @TargetApi(Build.VERSION_CODES.N_MR1)
    private ShortcutInfo createShortcutInfo(Profile profile) {
        boolean isIconResourceID;
        String iconIdentifier;
        Bitmap profileBitmap;
        boolean useCustomColor;

        Intent shortcutIntent;

        isIconResourceID = profile.getIsIconResourceID();
        iconIdentifier = profile.getIconIdentifier();
        useCustomColor = profile.getUseCustomColorForIcon();

        if (isIconResourceID) {
            //noinspection ConstantConditions
            if (profile._iconBitmap != null)
                profileBitmap = profile._iconBitmap;
            else {
                //int iconResource = context.getResources().getIdentifier(iconIdentifier, "drawable", context.getPackageName());
                int iconResource = Profile.getIconResource(iconIdentifier);
                profileBitmap = BitmapFactory.decodeResource(context.getResources(), iconResource);
            }
        } else {
            Resources resources = context.getResources();
            int height = (int) resources.getDimension(android.R.dimen.app_icon_size);
            int width = (int) resources.getDimension(android.R.dimen.app_icon_size);
            //Log.d("---- ShortcutCreatorListFragment.generateIconBitmap","resampleBitmapUri");
            profileBitmap = BitmapManipulator.resampleBitmapUri(iconIdentifier, width, height, true, false, context.getApplicationContext());
            if (profileBitmap == null) {
                int iconResource = R.drawable.ic_profile_default;
                profileBitmap = BitmapFactory.decodeResource(context.getResources(), iconResource);
            }
        }

        if (ApplicationPreferences.applicationWidgetIconColor(context).equals("1")) {
            if (isIconResourceID || useCustomColor) {
                // icon is from resource or colored by custom color
                int monochromeValue = 0xFF;
                String applicationWidgetIconLightness = ApplicationPreferences.applicationWidgetIconLightness(context);
                if (applicationWidgetIconLightness.equals("0")) monochromeValue = 0x00;
                if (applicationWidgetIconLightness.equals("25")) monochromeValue = 0x40;
                if (applicationWidgetIconLightness.equals("50")) monochromeValue = 0x80;
                if (applicationWidgetIconLightness.equals("75")) monochromeValue = 0xC0;
                //if (applicationWidgetIconLightness.equals("100")) monochromeValue = 0xFF;
                profileBitmap = BitmapManipulator.monochromeBitmap(profileBitmap, monochromeValue/*, getActivity().getBaseContext()*/);
            } else {
                float monochromeValue = 255f;
                String applicationWidgetIconLightness = ApplicationPreferences.applicationWidgetIconLightness(context);
                if (applicationWidgetIconLightness.equals("0")) monochromeValue = -255f;
                if (applicationWidgetIconLightness.equals("25")) monochromeValue = -128f;
                if (applicationWidgetIconLightness.equals("50")) monochromeValue = 0f;
                if (applicationWidgetIconLightness.equals("75")) monochromeValue = 128f;
                //if (applicationWidgetIconLightness.equals("100")) monochromeValue = 255f;
                profileBitmap = BitmapManipulator.grayScaleBitmap(profileBitmap);
                profileBitmap = BitmapManipulator.setBitmapBrightness(profileBitmap, monochromeValue);
            }
        }

        shortcutIntent = new Intent(context.getApplicationContext(), BackgroundActivateProfileActivity.class);
        shortcutIntent.setAction(Intent.ACTION_MAIN);
        shortcutIntent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_SHORTCUT);
        //noinspection ConstantConditions
        shortcutIntent.putExtra(PPApplication.EXTRA_PROFILE_ID, profile._id);

        String profileName = profile._name;
        String longLabel = profileName;
        if (profileName.isEmpty()) {
            profileName = " ";
            longLabel = " ";
        }

        return new ShortcutInfo.Builder(context, "profile_" + profile._id)
                .setShortLabel(profileName)
                .setLongLabel(/*context.getString(R.string.shortcut_activate_profile) + */longLabel)
                .setIcon(Icon.createWithBitmap(profileBitmap))
                .setIntent(shortcutIntent)
                .build();
    }

    void setDynamicLauncherShortcuts() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {
            ShortcutManager shortcutManager = context.getSystemService(ShortcutManager.class);

            if (shortcutManager != null) {
                final int limit = 5;
                List<Profile> countedProfiles = DatabaseHandler.getInstance(context).getProfilesForDynamicShortcuts(true/*, limit*/);
                List<Profile> notCountedProfiles = DatabaseHandler.getInstance(context).getProfilesForDynamicShortcuts(false/*, limit*/);

                ArrayList<ShortcutInfo> shortcuts = new ArrayList<>();

                for (Profile profile : countedProfiles) {
                    PPApplication.logE("DataWrapper.setDynamicLauncherShortcuts", "countedProfile=" + profile._name);
                    profile.generateIconBitmap(context, monochrome, monochromeValue, useMonochromeValueForCustomIcon);
                    shortcuts.add(createShortcutInfo(profile));
                }

                int shortcutsCount = countedProfiles.size();
                if (shortcutsCount < limit) {
                    for (Profile profile : notCountedProfiles) {
                        PPApplication.logE("DataWrapper.setDynamicLauncherShortcuts", "notCountedProfile=" + profile._name);
                        profile.generateIconBitmap(context, monochrome, monochromeValue, useMonochromeValueForCustomIcon);
                        shortcuts.add(createShortcutInfo(profile));

                        ++shortcutsCount;
                        if (shortcutsCount == limit)
                            break;
                    }
                }

                shortcutManager.setDynamicShortcuts(shortcuts);
            }
        }
    }

    void setDynamicLauncherShortcutsFromMainThread()
    {
        PPApplication.logE("DataWrapper.setDynamicLauncherShortcutsFromMainThread", "start");
        final DataWrapper dataWrapper = copyDataWrapper();

        PPApplication.startHandlerThread();
        final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                PowerManager powerManager = (PowerManager) dataWrapper.context.getSystemService(POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                if (powerManager != null) {
                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME+":DataWrapper.setDynamicLauncherShortcutsFromMainThread");
                    wakeLock.acquire(10 * 60 * 1000);
                }

                dataWrapper.setDynamicLauncherShortcuts();

                if ((wakeLock != null) && wakeLock.isHeld()) {
                    try {
                        wakeLock.release();
                    } catch (Exception ignored) {}
                }
            }
        });
    }

}
