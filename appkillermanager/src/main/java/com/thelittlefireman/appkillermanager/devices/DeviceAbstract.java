package com.thelittlefireman.appkillermanager.devices;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import androidx.annotation.DrawableRes;

import com.thelittlefireman.appkillermanager.utils.ActionsUtils;
import com.thelittlefireman.appkillermanager.utils.LogUtils;
import com.thelittlefireman.appkillermanager.utils.Manufacturer;

public abstract class DeviceAbstract implements DeviceBase {

    @SuppressWarnings("unused")
    @Override
    public boolean needToUseAlongwithActionDoseMode(){
        return false;
    }

    @SuppressWarnings("unused")
    @Override
    @DrawableRes
    public int getHelpImageAutoStart(){
        return 0;
    }

    @SuppressWarnings("unused")
    @Override
    @DrawableRes
    public int getHelpImageNotification(){
        return 0;
    }

    @SuppressWarnings("unused")
    @DrawableRes
    @Override
    public int getHelpImagePowerSaving() {
        return 0;
    }

    @SuppressWarnings("unused")
    @Override
    public boolean isActionDozeModeNotNecessary(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            if (pm != null)
                return pm.isIgnoringBatteryOptimizations(context.getPackageName());
            else
                return false;
        }
        return false;
    }

    @Override
    public Intent getActionDozeMode(Context context) {
        //Android 7.0+ Doze
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            boolean ignoringBatteryOptimizations = (pm != null) && pm.isIgnoringBatteryOptimizations(context.getPackageName());
            if (!ignoringBatteryOptimizations) {
                Intent dozeIntent = ActionsUtils.createIntent();
                // Cannot fire Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                // due to Google play device policy restriction !
                dozeIntent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                dozeIntent.setData(Uri.parse("package:" + context.getPackageName()));
                return dozeIntent;
            } else {
                LogUtils.i(this.getClass().getName(), "getActionDozeMode" + "App is already enable to ignore doze " +
                        "battery optimization");
            }
        }
        return null;
    }

    @Override
    public Manufacturer getDeviceManufacturer() {
        return Manufacturer.ZTE;
    }

    @Override
    public boolean isActionPowerSavingAvailable(Context context) {
        Intent intent = getActionPowerSaving(context);
        return ActionsUtils.isIntentAvailable(context, intent);
    }

    @Override
    public boolean isActionAutoStartAvailable(Context context) {
        Intent intent = getActionAutoStart(context);
        return ActionsUtils.isIntentAvailable(context, intent);
    }

    @Override
    public boolean isActionNotificationAvailable(Context context) {
        Intent intent = getActionNotification(context);
        return ActionsUtils.isIntentAvailable(context, intent);
    }

}
