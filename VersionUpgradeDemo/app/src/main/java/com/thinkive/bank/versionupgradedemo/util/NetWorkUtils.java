package com.thinkive.bank.versionupgradedemo.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.widget.Toast;

/**
 * @author: sq
 * @date: 2017/8/4
 * @corporation: 深圳市思迪信息技术股份有限公司
 * @description: 网络工具
 */
public class NetWorkUtils {

    private static Toast toast;

    /**
     * 检查网络是否可以连接互联网
     *
     * @param context
     * @return 是返回true，否则返回false
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            return false;
        } else {
            //获取所有网络连接信息
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {

                //逐一查找状态为已连接的网络
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 判断当前网络是什么网络 if(activeNetInfo.getType()==ConnectivityManager.TYPE_MOBILE)
     * { //判断3G网 activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI
     * //判断wifi
     *
     * @param context
     * @return boolean
     */
    public static String isWifiOrGprs(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetInfo != null
                && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {

            // Toast.makeText(context, "当前处于wifi网络", Toast.LENGTH_LONG).show();
            return "WIFI";
        }
       /* else if (activeNetInfo != null
                && activeNetInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
            // Toast.makeText(context, "当前处于gprs网络", Toast.LENGTH_LONG).show();
            if (activeNetInfo != null
    				&& (activeNetInfo.getSubtype() == TelephonyManager.NETWORK_TYPE_EDGE
    						|| activeNetInfo.getSubtype() == TelephonyManager.NETWORK_TYPE_GPRS || activeNetInfo
    						.getSubtype() == TelephonyManager.NETWORK_TYPE_CDMA)) {
            	return "2G";
    		}
        }*/
        else if (activeNetInfo != null
                && (activeNetInfo.getSubtype() == TelephonyManager.NETWORK_TYPE_EDGE
                || activeNetInfo.getSubtype() == TelephonyManager.NETWORK_TYPE_GPRS || activeNetInfo
                .getSubtype() == TelephonyManager.NETWORK_TYPE_CDMA)) {
            return "2G";
        } else if (activeNetInfo != null
                && (activeNetInfo.getSubtype() == TelephonyManager.NETWORK_TYPE_UMTS
                || activeNetInfo.getSubtype() == TelephonyManager.NETWORK_TYPE_EVDO_0 || activeNetInfo
                .getSubtype() == TelephonyManager.NETWORK_TYPE_EVDO_A || activeNetInfo.getSubtype() == TelephonyManager.NETWORK_TYPE_HSDPA ||
                activeNetInfo.getSubtype() == TelephonyManager.NETWORK_TYPE_HSUPA || activeNetInfo.getSubtype() == TelephonyManager.NETWORK_TYPE_HSPA
                || activeNetInfo.getSubtype() == TelephonyManager.NETWORK_TYPE_EVDO_B || activeNetInfo.getSubtype() == TelephonyManager.NETWORK_TYPE_EHRPD ||
                activeNetInfo.getSubtype() == TelephonyManager.NETWORK_TYPE_HSPAP)) {
            return "3G";
        } else if (activeNetInfo != null
                && (activeNetInfo.getSubtype() == TelephonyManager.NETWORK_TYPE_LTE)) {
            return "4G";
        } else {
            // Toast.makeText(context, "当前无网络", Toast.LENGTH_LONG).show();
            return "NONE";
        }
    }

    /**
     * 检查网络是否连接是否可以连接服务器，不可用会显示不能连接的提示
     *
     * @param context
     * @return 可以连接返回true，否则返回false
     */
    public static boolean checkNetworkAvailable(Context context) {
        if (isNetworkAvailable(context)) {
            return true;
        } else {
            toast = Toast.makeText(context, "网络连接不可用，请检查您的网络是否正常！", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
        return false;
    }

    /**
     * 对网络连接状态进行判断
     *
     * @return true, 可用； false， 不可用
     */
    public boolean isOpenNetwork(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connManager.getActiveNetworkInfo() != null) {
            return connManager.getActiveNetworkInfo().isAvailable();
        }
        return false;
    }
}
