package sk.henrichg.phoneprofiles;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

import java.util.List;

public class WifiStateChangedBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### WifiStateChangedBroadcastReceiver.onReceive", "xxx");

        //WifiManager wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (!PPApplication.getApplicationStarted(context, true))
            // application is not started
            return;

        //loadPreferences(context);

        int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
        PPApplication.logE("$$$ WifiStateChangedBroadcastReceiver.onReceive","state="+wifiState);

        if ((wifiState == WifiManager.WIFI_STATE_ENABLED) || (wifiState == WifiManager.WIFI_STATE_DISABLED))
        {
            if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
                // refresh configured networks list
                WifiSSIDData.fillWifiConfigurationList(context);

                if (!PhoneProfilesService.connectToSSID.equals(Profile.CONNECTTOSSID_JUSTANY)) {
                    WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
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
                //else {
                //    WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                //    wifiManager.disconnect();
                //    wifiManager.reconnect();
                //}
            }
        }

    }
}
