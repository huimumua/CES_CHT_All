package com.askey.mobile.zwave.control.util;

import android.os.Build;
import android.os.Environment;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.regex.Pattern;

/**
 * Utility class to get device parameters
 * 
 */
public class DeviceUtils {
    private static final String CPU_DIR = "/sys/devices/system/cpu/";
    private static final String LOG_TAG = DeviceUtils.class.getSimpleName();

    /**
     * This method is used to get the Device's OS version
     * 
     * @return the Device's OS version
     */
    public static int getDeviceOsVersion() {
        return Build.VERSION.SDK_INT;
    }

    /**
     * Gets the number of cores available on this device, across all processors. Uses/parses the
     * directory "/sys/devices/system/cpu" and no of files in this directory is the actual no of
     * cores. Uses file filters for accurate result.
     * 
     * @return the number of available cores, or 1 if unable to get the result
     */
    public static int getNumberOfCores() {
        /**
         * Private FileFilter Class to display only CPU devices in the directory listing
         * 
         */
        class CpuFilter implements FileFilter {
            @Override
            public boolean accept( File pathname ) {
                /**
                 * Check if filename is "cpu", followed by a single digit number
                 */
                return Pattern.matches("cpu[0-9]+", pathname.getName());
            }
        }

        try {
            /**
             * Get directory containing CPU info
             */
            File dir = new File( CPU_DIR );

            /**
             * Add a filer to only list the files starting with "cpu"
             */
            File[] files = dir.listFiles( new CpuFilter( ) );

            /**
             * Return the number of cores (virtual CPU devices)
             */
            return files.length;
        } catch ( Exception e ) {
            Logg.e(LOG_TAG, "DeviceUtils: getnumberOfCores: Exception->" + e.getMessage( ) );
            return 1;
        }
    }

    /**
     * 获取设备的MAC地址
     * */
   public static String getMac() {
        String macSerial = null;
        String str = "";
        try {
            Process pp = Runtime.getRuntime().exec(
                    "cat /sys/class/net/wlan0/address ");
            InputStreamReader ir = new InputStreamReader(pp.getInputStream(), "UTF-8");
            LineNumberReader input = new LineNumberReader(ir);

            for (; null != str;) {
                str = input.readLine();
                if (str != null) {
                    macSerial = str.trim();// 去空格
                    break;
                }
            }
        } catch (IOException ex) {
            // 赋予默认值
            ex.printStackTrace();
        }
        return macSerial;
    }


    /**
     * This function checks whether SD card is present or not and whether space is available or not.
     * 
     * @return true if SD card is available, false otherwise.
     */
    public static boolean isSdCardPresent() {
        String state = Environment.getExternalStorageState( );
        return state.equals(Environment.MEDIA_MOUNTED) && !state.equals(Environment.MEDIA_MOUNTED_READ_ONLY);
    }

    /**
     * Returns the canonical path of the external media directory. A canonical path is an absolute
     * path with all symbolic links and references to "." and ".." resolved. If the canonical path
     * cannot be resolved, returns the absolute path.<br>
     * Note that the media may not be available. Use  to check
     * for media availability,
     * 
     * @return A String containing the path.
     */
    private static final String _getExternalMediaPath() {
        try {
            return Environment.getExternalStorageDirectory( ).getCanonicalPath( );
        } catch ( IOException e ) {
            e.printStackTrace( );
            return Environment.getExternalStorageDirectory( ).getAbsolutePath( );
        }
    }

    /**
     * Method to get the device model. e.g. Droid 3
     * 
     * @return The model name of the device.
     */
    public String getDeviceModel() {
        return Build.MODEL;
    }

    /**
     * This method is used to get the device manufacturer. e.g. Samsung
     * 
     * @return the manufacturer.
     */
    public String getDeviceManufacturer() {
        return Build.MANUFACTURER;
    }

    /**
     * This method is used to get the device sdk version.
     * 
     * @return the the device sdk version.
     */
    public String getDeviceSdkVersion() {
        return Build.VERSION.RELEASE;
    }

    /**
     * 获取android动系统版本
     * */
    public static int getAndroidSDKVersion() {
        int version = 0;
        try {
            version = Integer.valueOf(Build.VERSION.SDK_INT);
        } catch (NumberFormatException e) {
            Logg.e(LOG_TAG,"getAndroidSDKVersion:"+e.toString());
        }
        return version;
    }


}
