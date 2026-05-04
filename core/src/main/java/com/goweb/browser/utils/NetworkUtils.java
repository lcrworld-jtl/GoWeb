package com.goweb.browser.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.goweb.browser.App;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class NetworkUtils {

    private static final String TAG = "NetworkUtils";

    public static final int NETWORK_UNKNOWN = 0;
    public static final int NETWORK_WIFI = 1;
    public static final int NETWORK_MOBILE = 2;
    public static final int NETWORK_ETHERNET = 3;
    public static final int NETWORK_BLUETOOTH = 4;
    public static final int NETWORK_VPN = 5;
    public static final int NETWORK_NONE = -1;

    public static final int MOBILE_2G = 1;
    public static final int MOBILE_3G = 2;
    public static final int MOBILE_4G = 3;
    public static final int MOBILE_5G = 4;
    public static final int MOBILE_UNKNOWN = 0;

    public static boolean isNetworkAvailable() {
        Context context = App.getContext();
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        try {
            NetworkInfo info = cm.getActiveNetworkInfo();
            return info != null && info.isConnected();
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isWifiConnected() {
        Context context = App.getContext();
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        try {
            NetworkInfo info = cm.getActiveNetworkInfo();
            return info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_WIFI;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isMobileConnected() {
        Context context = App.getContext();
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        try {
            NetworkInfo info = cm.getActiveNetworkInfo();
            return info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_MOBILE;
        } catch (Exception e) {
            return false;
        }
    }

    public static int getNetworkType() {
        Context context = App.getContext();
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return NETWORK_NONE;
        try {
            NetworkInfo info = cm.getActiveNetworkInfo();
            if (info == null || !info.isConnected()) return NETWORK_NONE;
            int type = info.getType();
            switch (type) {
                case ConnectivityManager.TYPE_WIFI:
                    return NETWORK_WIFI;
                case ConnectivityManager.TYPE_MOBILE:
                    return NETWORK_MOBILE;
                case ConnectivityManager.TYPE_ETHERNET:
                    return NETWORK_ETHERNET;
                case ConnectivityManager.TYPE_BLUETOOTH:
                    return NETWORK_BLUETOOTH;
                default:
                    if (Build.VERSION.SDK_INT >= 21) {
                        if (type == 17) return NETWORK_VPN;
                    }
                    return NETWORK_UNKNOWN;
            }
        } catch (Exception e) {
            return NETWORK_UNKNOWN;
        }
    }

    public static String getNetworkTypeName() {
        int type = getNetworkType();
        switch (type) {
            case NETWORK_WIFI:
                return "WiFi";
            case NETWORK_MOBILE:
                int mobileType = getMobileNetworkClass();
                switch (mobileType) {
                    case MOBILE_2G: return "2G";
                    case MOBILE_3G: return "3G";
                    case MOBILE_4G: return "4G";
                    case MOBILE_5G: return "5G";
                    default: return "Mobile";
                }
            case NETWORK_ETHERNET:
                return "Ethernet";
            case NETWORK_BLUETOOTH:
                return "Bluetooth";
            case NETWORK_VPN:
                return "VPN";
            case NETWORK_NONE:
                return "No Connection";
            default:
                return "Unknown";
        }
    }

    public static int getMobileNetworkClass() {
        Context context = App.getContext();
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (tm == null) return MOBILE_UNKNOWN;
        try {
            int networkType = tm.getNetworkType();
            switch (networkType) {
                case TelephonyManager.NETWORK_TYPE_GPRS:
                case TelephonyManager.NETWORK_TYPE_EDGE:
                case TelephonyManager.NETWORK_TYPE_CDMA:
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                case TelephonyManager.NETWORK_TYPE_IDEN:
                case TelephonyManager.NETWORK_TYPE_GSM:
                    return MOBILE_2G;
                case TelephonyManager.NETWORK_TYPE_UMTS:
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                case TelephonyManager.NETWORK_TYPE_HSPA:
                case TelephonyManager.NETWORK_TYPE_EVDO_B:
                case TelephonyManager.NETWORK_TYPE_EHRPD:
                case TelephonyManager.NETWORK_TYPE_HSPAP:
                case TelephonyManager.NETWORK_TYPE_TD_SCDMA:
                    return MOBILE_3G;
                case TelephonyManager.NETWORK_TYPE_LTE:
                case TelephonyManager.NETWORK_TYPE_IWLAN:
                    return MOBILE_4G;
                default:
                    if (Build.VERSION.SDK_INT >= 29) {
                        if (networkType == TelephonyManager.NETWORK_TYPE_NR) {
                            return MOBILE_5G;
                        }
                    }
                    return MOBILE_UNKNOWN;
            }
        } catch (Exception e) {
            return MOBILE_UNKNOWN;
        }
    }

    public static String getWifiSSID() {
        Context context = App.getContext();
        try {
            WifiManager wm = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wm != null) {
                WifiInfo info = wm.getConnectionInfo();
                if (info != null) {
                    String ssid = info.getSSID();
                    if (ssid != null && !ssid.isEmpty() && !ssid.equals("<unknown ssid>")) {
                        return ssid.replace("\"", "");
                    }
                }
            }
        } catch (Exception e) {
        }
        return "";
    }

    public static int getWifiSignalStrength() {
        Context context = App.getContext();
        try {
            WifiManager wm = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wm != null) {
                WifiInfo info = wm.getConnectionInfo();
                if (info != null) {
                    int rssi = info.getRssi();
                    return WifiManager.calculateSignalLevel(rssi, 5);
                }
            }
        } catch (Exception e) {
        }
        return 0;
    }

    public static String getLocalIpAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (!addr.isLoopbackAddress() && addr instanceof Inet4Address) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
        }
        return "0.0.0.0";
    }

    public static String getNetworkInfoString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Type: ").append(getNetworkTypeName()).append("\n");
        if (isWifiConnected()) {
            String ssid = getWifiSSID();
            if (!ssid.isEmpty()) {
                sb.append("SSID: ").append(ssid).append("\n");
            }
            sb.append("Signal: ").append(getWifiSignalStrength()).append("/4\n");
        }
        sb.append("IP: ").append(getLocalIpAddress()).append("\n");
        sb.append("Available: ").append(isNetworkAvailable() ? "Yes" : "No");
        return sb.toString();
    }

    public static IntentFilter getNetworkChangeFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        return filter;
    }

    public static abstract class NetworkChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
                boolean noConnectivity = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
                if (noConnectivity) {
                    onNetworkLost();
                } else {
                    onNetworkAvailable();
                }
            }
        }

        public abstract void onNetworkAvailable();

        public abstract void onNetworkLost();
    }

    public static boolean isVpnActive() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces != null && interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                String name = iface.getName();
                if (name != null && (name.startsWith("tun") || name.startsWith("ppp") || name.startsWith("pptp"))) {
                    return iface.isUp();
                }
            }
        } catch (Exception e) {
        }
        return false;
    }

    public static String getConnectionQuality() {
        if (!isNetworkAvailable()) return "None";
        int type = getNetworkType();
        switch (type) {
            case NETWORK_WIFI:
                return "Excellent";
            case NETWORK_ETHERNET:
                return "Excellent";
            case NETWORK_MOBILE:
                int mobileClass = getMobileNetworkClass();
                switch (mobileClass) {
                    case MOBILE_5G: return "Excellent";
                    case MOBILE_4G: return "Good";
                    case MOBILE_3G: return "Fair";
                    case MOBILE_2G: return "Poor";
                    default: return "Unknown";
                }
            default:
                return "Unknown";
        }
    }
}
