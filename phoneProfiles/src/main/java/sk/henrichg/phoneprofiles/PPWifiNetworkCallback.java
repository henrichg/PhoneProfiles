package sk.henrichg.phoneprofiles;

import android.net.ConnectivityManager;
import android.net.Network;

@SuppressWarnings("WeakerAccess")
public class PPWifiNetworkCallback extends ConnectivityManager.NetworkCallback {

    static boolean connected = false;

    @Override
    public void onLost(Network network) {
        //record wi-fi disconnect event
        //PPApplication.logE("PPWifiNetworkCallback.onLost", "xxx");
        connected = false;
    }

    @Override
    public void onUnavailable() {
        //PPApplication.logE("PPWifiNetworkCallback.onUnavailable", "xxx");
        connected = false;
    }

    @Override
    public void onLosing(Network network, int maxMsToLive) {
        //PPApplication.logE("PPWifiNetworkCallback.onLosing", "xxx");
    }

    @Override
    public void onAvailable(Network network) {
        //record wi-fi connect event
        //PPApplication.logE("PPWifiNetworkCallback.onAvailable", "xxx");
        connected = true;
    }

}
