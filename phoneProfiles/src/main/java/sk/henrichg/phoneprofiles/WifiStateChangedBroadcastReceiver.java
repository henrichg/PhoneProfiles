package sk.henrichg.phoneprofiles;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.PowerManager;

import java.util.List;

import static android.content.Context.POWER_SERVICE;

public class WifiStateChangedBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### WifiStateChangedBroadcastReceiver.onReceive", "xxx");

        final Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(appContext, true))
            // application is not started
            return;

        final int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
        PPApplication.logE("$$$ WifiStateChangedBroadcastReceiver.onReceive","state="+wifiState);

        if ((wifiState == WifiManager.WIFI_STATE_ENABLED) || (wifiState == WifiManager.WIFI_STATE_DISABLED))
        {
            PPApplication.startHandlerThread();
            final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "WifiStateChangedBroadcastReceiver.onReceive");
                        wakeLock.acquire(10 * 60 * 1000);
                    }

                    if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
                        // refresh configured networks list
                        WifiSSIDData.fillWifiConfigurationList(appContext);

                        if (!PhoneProfilesService.connectToSSID.equals(Profile.CONNECTTOSSID_JUSTANY)) {
                            WifiManager wifiManager = (WifiManager) appContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                            if (wifiManager != null) {
                                List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
                                if (list != null) {
                                    for (WifiConfiguration i : list) {
                                        if (i.SSID != null && i.SSID.equals(PhoneProfilesService.connectToSSID)) {
                                            //wifiManager.disconnect();
                                            wifiManager.enableNetwork(i.networkId, true);
                                            //wifiManager.reconnect();
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if ((wakeLock != null) && wakeLock.isHeld()) {
                        try {
                            wakeLock.release();
                        } catch (Exception ignored) {}
                    }
                }
            });
        }

    }
}
