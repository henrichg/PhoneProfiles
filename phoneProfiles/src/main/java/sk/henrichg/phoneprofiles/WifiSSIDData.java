package sk.henrichg.phoneprofiles;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

class WifiSSIDData {

    String ssid;
    String bssid;
    //private boolean custom;

    // constructor is required for GSon !!!
    WifiSSIDData() {}

    WifiSSIDData(String ssid, String bssid/*, boolean custom*/)
    {
        this.ssid = ssid;
        this.bssid = bssid;
        //this.custom = custom;
    }

    private static final String SCAN_RESULT_COUNT_PREF = "count";
    private static final String SCAN_RESULT_DEVICE_PREF = "device";

    static void fillWifiConfigurationList(Context context)
    {
        //if (wifiConfigurationList == null)
        //    wifiConfigurationList = new ArrayList<WifiSSIDData>();

        List<WifiSSIDData> wifiConfigurationList = new ArrayList<>();

        WifiManager wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifi == null)
            return;

        if (wifi.getWifiState() != WifiManager.WIFI_STATE_ENABLED)
            // wifi must be enabled for wifi.getConfiguredNetworks()
            return;

        List<WifiConfiguration> _wifiConfigurationList = wifi.getConfiguredNetworks();
        if (_wifiConfigurationList != null)
        {
            wifiConfigurationList.clear();
            for (WifiConfiguration device : _wifiConfigurationList)
            {
                boolean found = false;
                for (WifiSSIDData _device : wifiConfigurationList)
                {
                    //if (_device.bssid.equals(device.BSSID))
                    if ((_device.ssid != null) && (_device.ssid.equals(device.SSID)))
                    {
                        found = true;
                        break;
                    }
                }
                if (!found)
                {
                    wifiConfigurationList.add(new WifiSSIDData(device.SSID, device.BSSID/*, false*/));
                }
            }
        }

        SharedPreferences preferences = context.getSharedPreferences(PPApplication.WIFI_CONFIGURATION_LIST_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.clear();

        editor.putInt(SCAN_RESULT_COUNT_PREF, wifiConfigurationList.size());

        Gson gson = new Gson();

        for (int i = 0; i < wifiConfigurationList.size(); i++) {
            String json = gson.toJson(wifiConfigurationList.get(i));
            editor.putString(SCAN_RESULT_DEVICE_PREF + i, json);
        }

        editor.apply();
    }

    static List<WifiSSIDData> getWifiConfigurationList(Context context)
    {
        synchronized (PPApplication.scanResultsMutex) {
            //if (wifiConfigurationList == null)
            //    wifiConfigurationList = new ArrayList<WifiSSIDData>();

            //wifiConfigurationList.clear();

            List<WifiSSIDData> wifiConfigurationList = new ArrayList<>();

            SharedPreferences preferences = context.getSharedPreferences(PPApplication.WIFI_CONFIGURATION_LIST_PREFS_NAME, Context.MODE_PRIVATE);

            int count = preferences.getInt(SCAN_RESULT_COUNT_PREF, 0);

            Gson gson = new Gson();

            for (int i = 0; i < count; i++) {
                String json = preferences.getString(SCAN_RESULT_DEVICE_PREF + i, "");
                if (!json.isEmpty()) {
                    WifiSSIDData device = gson.fromJson(json, WifiSSIDData.class);
                    wifiConfigurationList.add(device);
                }
            }

            return wifiConfigurationList;
        }
    }

}
