package sk.henrichg.phoneprofiles;

import android.content.Context;
import android.content.res.TypedArray;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;

import androidx.preference.DialogPreference;

public class VolumeDialogPreferenceX extends DialogPreference {

    VolumeDialogPreferenceFragmentX fragment;

    private final Context _context;

    final AudioManager audioManager;
    MediaPlayer mediaPlayer = null;

    // Custom xml attributes.
    String volumeType;
    int noChange;
    int sharedProfile;
    int disableSharedProfile;

    int maximumValue = 7;
    final int minimumValue = 0;
    int maximumMediaValue = 15;
    private int defaultValueRing = 0;
    private int defaultValueNotification = 0;
    int defaultValueMusic = 0;
    private int defaultValueAlarm = 0;
    private int defaultValueSystem = 0;
    private int defaultValueVoice = 0;
    final int stepSize = 1;

    private String sValue = "0|1";
    int value = 0;

    public VolumeDialogPreferenceX(Context context, AttributeSet attrs) {
        super(context, attrs);

        _context = context;

        TypedArray typedArray = context.obtainStyledAttributes(attrs,
            R.styleable.VolumeDialogPreference);

        volumeType = typedArray.getString(
            R.styleable.VolumeDialogPreference_volumeType);
        noChange = typedArray.getInteger(
            R.styleable.VolumeDialogPreference_vNoChange, 1);
        sharedProfile = typedArray.getInteger(
                R.styleable.VolumeDialogPreference_vSharedProfile, 0);
        disableSharedProfile = typedArray.getInteger(
                R.styleable.VolumeDialogPreference_vDisableSharedProfile, 0);

        audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);

        if (audioManager != null) {
            // get max. values from audio manager
            if (volumeType.equalsIgnoreCase("RINGTONE"))
                maximumValue = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
            else if (volumeType.equalsIgnoreCase("NOTIFICATION"))
                maximumValue = audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);
            else if (volumeType.equalsIgnoreCase("MEDIA"))
                maximumValue = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            else if (volumeType.equalsIgnoreCase("ALARM"))
                maximumValue = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
            else if (volumeType.equalsIgnoreCase("SYSTEM"))
                maximumValue = audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
            else if (volumeType.equalsIgnoreCase("VOICE"))
                maximumValue = audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
            maximumMediaValue = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

            // get actual values from audio manager
            defaultValueRing = audioManager.getStreamVolume(AudioManager.STREAM_RING);
            defaultValueNotification = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
            defaultValueMusic = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            defaultValueAlarm = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
            defaultValueSystem = audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
            defaultValueVoice = audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
        }

        typedArray.recycle();
    }

    @Override
    protected void onSetInitialValue(Object defaultValue)
    {
        // Get the persistent value and correct it for the minimum value.
        sValue = getPersistedString((String) defaultValue);

        PPApplication.logE("VolumeDialogPreferenceX.getValueVDP", "form onSetInitialValue");
        getValueVDP();
        setSummaryVDP();
    }

    private void getValueVDP()
    {
        PPApplication.logE("VolumeDialogPreferenceX.getValueVDP", "sValue="+sValue);
        String[] splits = sValue.split("\\|");
        try {
            value = Integer.parseInt(splits[0]);
            if (value == -1)
            {
                if (volumeType.equalsIgnoreCase("SYSTEM"))
                    value = defaultValueSystem;
                else
                if (volumeType.equalsIgnoreCase("RINGTONE"))
                    value = defaultValueRing;
                else
                if (volumeType.equalsIgnoreCase("NOTIFICATION"))
                    value =  defaultValueNotification;
                else
                if (volumeType.equalsIgnoreCase("MEDIA"))
                    value =  defaultValueMusic;
                else
                if (volumeType.equalsIgnoreCase("ALARM"))
                    value =  defaultValueAlarm;
                else
                if (volumeType.equalsIgnoreCase("VOICE"))
                    value =  defaultValueVoice;
            }
        } catch (Exception e) {
            PPApplication.logE("VolumeDialogPreferenceX.getValueVDP", Log.getStackTraceString(e));
            value = 0;
        }
        value = value - minimumValue;

        try {
            noChange = Integer.parseInt(splits[1]);
        } catch (Exception e) {
            noChange = 1;
        }
        try {
            sharedProfile = Integer.parseInt(splits[2]);
        } catch (Exception e) {
            sharedProfile = 0;
        }

        // You're never know...
        if (value < 0) {
            value = 0;
        }

        PPApplication.logE("VolumeDialogPreferenceX.getValueVDP", "value="+value);
        PPApplication.logE("VolumeDialogPreferenceX.getValueVDP", "noChange="+noChange);
        PPApplication.logE("VolumeDialogPreferenceX.getValueVDP", "sharedProfile="+sharedProfile);
    }

    private void setSummaryVDP()
    {
        String prefVolumeDataSummary;
        if (noChange == 1)
            prefVolumeDataSummary = _context.getResources().getString(R.string.preference_profile_no_change);
        else
        if (sharedProfile == 1)
            prefVolumeDataSummary = _context.getResources().getString(R.string.preference_profile_default_profile);
        else
            prefVolumeDataSummary = String.valueOf(value) + " / " + String.valueOf(maximumValue);
        setSummary(prefVolumeDataSummary);
    }

    String getSValue() {
        return Integer.toString(value + minimumValue)
                + "|" + Integer.toString(noChange)
                + "|" + Integer.toString(sharedProfile);
    }

    void persistValue() {
        if (shouldPersist()) {
            persistString(getSValue());
            setSummaryVDP();
        }
    }

    static boolean changeEnabled(String value) {
        String[] splits = value.split("\\|");
        if (splits.length > 1) {
            try {
                return Integer.parseInt(splits[1]) == 0;
            } catch (Exception e) {
                return false;
            }
        }
        else
            return false;
    }

    @Override
    protected Parcelable onSaveInstanceState()
    {
        final Parcelable superState = super.onSaveInstanceState();
        /*if (isPersistent()) {
            return superState;
        }*/

        final VolumeDialogPreferenceX.SavedState myState = new VolumeDialogPreferenceX.SavedState(superState);
        myState.sValue = sValue;
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        if (!state.getClass().equals(VolumeDialogPreferenceX.SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            PPApplication.logE("VolumeDialogPreferenceX.getValueVDP", "form onRestoreInstanceState");
            getValueVDP();
            setSummaryVDP();
            return;
        }

        // restore instance state
        VolumeDialogPreferenceX.SavedState myState = (VolumeDialogPreferenceX.SavedState)state;
        super.onRestoreInstanceState(myState.getSuperState());
        sValue = myState.sValue;

        PPApplication.logE("VolumeDialogPreferenceX.getValueVDP", "form onRestoreInstanceState");
        getValueVDP();
        setSummaryVDP();
    }

    // SavedState class
    private static class SavedState extends BaseSavedState
    {
        String sValue;

        SavedState(Parcel source)
        {
            super(source);

            // restore profileId
            sValue = source.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags)
        {
            super.writeToParcel(dest, flags);

            // save profileId
            dest.writeString(sValue);
        }

        SavedState(Parcelable superState)
        {
            super(superState);
        }

        @SuppressWarnings("unused")
        public static final Creator<SavedState> CREATOR =
                new Creator<VolumeDialogPreferenceX.SavedState>() {
                    public VolumeDialogPreferenceX.SavedState createFromParcel(Parcel in)
                    {
                        return new VolumeDialogPreferenceX.SavedState(in);
                    }
                    public VolumeDialogPreferenceX.SavedState[] newArray(int size)
                    {
                        return new VolumeDialogPreferenceX.SavedState[size];
                    }

                };

    }

}
