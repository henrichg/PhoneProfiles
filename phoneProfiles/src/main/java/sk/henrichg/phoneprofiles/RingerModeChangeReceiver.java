package sk.henrichg.phoneprofiles;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

public class RingerModeChangeReceiver extends BroadcastReceiver {

    public static boolean internalChange = false;
    public static boolean notUnlinkVolumes = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!internalChange) {
            notUnlinkVolumes = true;
            final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            setRingerMode(context, audioManager);
        }

        //setAlarmForDisableInternalChange(context);
    }

    private static int getRingerMode(Context context, AudioManager audioManager) {
        int ringerMode = audioManager.getRingerMode();

        PPApplication.logE("RingerModeChangeReceiver.getRingerMode", "ringerMode="+ringerMode);

        // convert to profile ringerMode
        int pRingerMode = 0;
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            int systemZenMode = ActivateProfileHelper.getSystemZenMode(context/*, -1*/);
            PPApplication.logE("RingerModeChangeReceiver.getRingerMode", "systemZenMode=" + systemZenMode);
            if (systemZenMode == ActivateProfileHelper.ZENMODE_ALL) {
                switch (ringerMode) {
                    case AudioManager.RINGER_MODE_NORMAL:
                        if (ActivateProfileHelper.vibrationIsOn(audioManager, false))
                            pRingerMode = 2;
                        else
                            pRingerMode = 1;
                        break;
                    case AudioManager.RINGER_MODE_VIBRATE:
                        pRingerMode = 3;
                        break;
                    case AudioManager.RINGER_MODE_SILENT:
                        pRingerMode = 4;
                        break;
                }
            }
            else
                pRingerMode = 5;
        }
        else {
            switch (ringerMode) {
                case AudioManager.RINGER_MODE_NORMAL:
                    if (ActivateProfileHelper.vibrationIsOn(audioManager, false))
                        pRingerMode = 2;
                    else
                        pRingerMode = 1;
                    break;
                case AudioManager.RINGER_MODE_VIBRATE:
                    pRingerMode = 3;
                    break;
                case AudioManager.RINGER_MODE_SILENT:
                    pRingerMode = 4;
                    break;
            }
        }

        PPApplication.logE("RingerModeChangeReceiver.getRingerMode", "pRingerMode=" + pRingerMode);

        return pRingerMode;
    }

    public static void setRingerMode(Context context, AudioManager audioManager) {
        int pRingerMode = getRingerMode(context, audioManager);
        if (pRingerMode != 0) {
            ActivateProfileHelper.setRingerMode(context, pRingerMode);
        }
    }

}
