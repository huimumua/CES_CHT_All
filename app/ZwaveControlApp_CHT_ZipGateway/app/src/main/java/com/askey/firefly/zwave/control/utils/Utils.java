package com.askey.firefly.zwave.control.utils;

import android.os.Build;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;


/**
 * Created by chiapin on 2017/7/25.
 */

public class Utils {
    private static final String LOG_TAG = "Utils";

    public static String getPublicTopicName(){

        String tPublicTopicName = Build.SERIAL;
        //String tPublicTopicName = UUID.randomUUID().toString().replaceAll("-","");
        if (tPublicTopicName.contains("unknown")){
            tPublicTopicName = getAndroidID();
            if (tPublicTopicName.contains("noAndroidId")){
                tPublicTopicName = UUID.randomUUID().toString().replaceAll("-","");
            }
        }
        return tPublicTopicName;
    }
    public static String getAndroidID(){

        String sAndroidId = getSystemProp("net.hostname");
        if (sAndroidId == null){
            sAndroidId = "noAndroidId";
        }else{
            String[] tokens = sAndroidId.split("android-");
            sAndroidId = tokens[tokens.length-1];
        }
        return sAndroidId;
    }

    public static String getIpAddress() {
        String hotspotIp = "192.168.43.1";
        try {
            for (Enumeration<NetworkInterface> enNetI = NetworkInterface
                    .getNetworkInterfaces(); enNetI.hasMoreElements(); ) {
                NetworkInterface netI = enNetI.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = netI
                        .getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (inetAddress instanceof Inet4Address && !inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return hotspotIp;
    }

    public static ArrayList searchString(String sSource, String sTarget){

        ArrayList<String> tmpResut = new ArrayList<>();
        ArrayList<String> resultList = new ArrayList<String>(Arrays.asList(sSource.replaceAll("\n","").split(",")));

        for (int idx=0;idx<resultList.size();idx++) {
            if(resultList.get(idx).contains(sTarget)) {
                String[] tokens = resultList.get(idx).split("\""+sTarget+"\":");
                tokens[1] = tokens[1].trim().replaceAll("\"","");
                tmpResut.add(tokens[1]);
            }
        }
        return tmpResut;
    }

    public static String getDeviceInfoNode(String sSource, String sTarget){

        ArrayList<String> resultList = new ArrayList<String>(Arrays.asList(sSource.split(",")));

        for (int idx=0;idx<resultList.size();idx++) {
            if(resultList.get(idx).contains(sTarget)) {
                String[] tokens = resultList.get(idx).split("\""+sTarget+"\":");
                tokens[1] = tokens[1].trim().replaceAll("\"","");
                return tokens[1];
            }
        }
        return "";
    }

    public static String getSystemProp(String propName){
        String GETPROP_EXECUTABLE_PATH = "/system/bin/getprop";

        Process process = null;
        BufferedReader bufferedReader = null;
        try {
            process = new ProcessBuilder().command(GETPROP_EXECUTABLE_PATH, propName).redirectErrorStream(true).start();
            bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = bufferedReader.readLine();
            if (line == null){
                line = ""; //prop not set
            }
            //Log.i(LOG_TAG,"read System Property: " + propName + "=" + line);
            return line;
        } catch (Exception e) {
            //Log.e(LOG_TAG,"Failed to read System Property " + propName,e);
            return "";
        } finally{
            if (bufferedReader != null){
                try {
                    bufferedReader.close();
                } catch (IOException e) {}
            }
            if (process != null){
                process.destroy();
            }
        }
    }

    public static boolean isGoodJson(String jsonStr) {
        try {
            JSONObject o = new JSONObject(jsonStr);
        } catch (JSONException e) {
            return false;
        }
        return true;
    }

    public static final byte[] intToByteArray(int value) {
        return new byte[] {
                (byte)(value >>> 24),
                (byte)(value >>> 16),
                (byte)(value >>> 8),
                (byte)value};
    }

    public static final int byteArrayToInt(byte[] bytes){
        int value = ((bytes[0] & 0xFF) << 24) | ((bytes[1] & 0xFF) << 16)
                | ((bytes[2] & 0xFF) << 8) | (bytes[3] & 0xFF);

        return value;
    }

    public static int[] convertIntegers(List<Integer> integers)
    {
        int[] ret = new int[integers.size()+1];
        for (int i=0; i < ret.length-1; i++)
        {
            ret[i] = integers.get(i).intValue();
        }
        ret[ret.length-1] = 0;
        return ret;
    }
}
