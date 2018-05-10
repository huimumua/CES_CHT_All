package com.askey.mobile.zwave.control.interf;

/**
 * Created by skysoft on 2018/4/12.
 */

public class NetworkRole {
    private static NetworkRoleCallback networkRoleCallback;
    public static void setNetworkRoleCallback(NetworkRoleCallback callback) {
        networkRoleCallback = callback;
    }
   public static void showNetworkRole() {
       if (networkRoleCallback != null) {
           networkRoleCallback.showNetworkRole();
       }
    }
}
