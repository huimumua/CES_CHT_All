package com.askey.firefly.zwave.control.thirdparty.usbserial;

import android.hardware.usb.UsbDevice;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.app.PendingIntent;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import com.askey.firefly.zwave.control.thirdparty.usbserial.usbserial.UsbSerialDevice;
import com.askey.firefly.zwave.control.thirdparty.usbserial.usbserial.UsbSerialInterface;

public class UsbSerial {

    static {
        System.loadLibrary("usbserial-jni");
    }

    private UsbDevice device;
    private final String TAG = "UsbSerial";
    private UsbManager usbManager;
    private Context mContext = null;
    private UsbSerialDevice serialPort;
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    public UsbSerial(Context context) {
        mContext = context;
        UsbSerial_Set_Object();
        Log.d(TAG, "UsbSerial Construct");
    }

    /*
     * Request user permission. The response will be received in the BroadcastReceiver
     */
    private void requestUserPermission() {
        PendingIntent mPendingIntent = PendingIntent.getBroadcast(mContext, 0, new Intent(ACTION_USB_PERMISSION), 0);
        usbManager.requestPermission(device, mPendingIntent);
    }

    private boolean isCdcDevice(int vid,int pid) {
        final int[][] vid_pid = {{0x0658,0x0200}};
        for(int[] a:vid_pid){
            if(vid == a[0] && pid == a[1])
                return true;
        }
        return false;
    }

    private UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() {
        @Override
        public void onReceivedData(byte[] arg0) {
            buffer.write(arg0);
        }
    };

    public int Open()
    {
        boolean hasDevice = false;
        usbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();

        if (!usbDevices.isEmpty()) {
            for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
                device = entry.getValue();
                int deviceVID = device.getVendorId();
                int devicePID = device.getProductId();
                if (isCdcDevice(deviceVID,devicePID)) {
                    if (!usbManager.hasPermission(device)) {
                        requestUserPermission();
                        return -1;
                    } else {
                        hasDevice = true;
                        break;
                    }
                }
            }
            if (!hasDevice){
                return -1;
            }
        }else{
            return -1;
        }

        UsbDeviceConnection connection = usbManager.openDevice(device);

        if(connection == null)
        {
            return -1;
        }

        serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection);
        if (serialPort != null) {
            if (serialPort.open()) {
                serialPort.setBaudRate(115200);
                serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
                serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
                serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
                serialPort.read(mCallback);
                return 0;
            }
        }
        return -1;
    }

    public void Close()
    {
        serialPort.close();
    }

    public void Write(byte[] data)
    {
        serialPort.write(data);
    }

    public int Read(byte[] data, int len)
    {
        return buffer.read(data, 0, len);
    }

    public int Check()
    {
        return buffer.getUsed();
    }

    private ByteRingBuffer buffer = new ByteRingBuffer(1024);

    private native void UsbSerial_Set_Object();
}
