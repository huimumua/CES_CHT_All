package com.askey.firefly.zwave.control.thirdparty.usbserial;

import android.hardware.usb.UsbDevice;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.app.PendingIntent;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import com.askey.firefly.zwave.control.thirdparty.usbserial.usbserial.APMSerialDevice;
import com.askey.firefly.zwave.control.thirdparty.usbserial.usbserial.CDCSerialDevice;
import com.askey.firefly.zwave.control.thirdparty.usbserial.usbserial.UsbSerialDevice;
import com.askey.firefly.zwave.control.thirdparty.usbserial.usbserial.UsbSerialInterface;

public class UsbSerial {

    static {
        System.loadLibrary("usbserial-jni");
    }

    private UsbDevice device;
    private final String TAG = "UsbSerial";
    private UsbDeviceConnection connection;
    private UsbManager usbManager;
    private Context mContext = null;
    private UsbSerialDevice serialPort;
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
	
    public UsbSerial(Context context) {
        mContext = context;
        UsbSerial_Set_Object();
    }
	
    /*
     * Request user permission. The response will be received in the BroadcastReceiver
     */
    private void requestUserPermission() {
        PendingIntent mPendingIntent = PendingIntent.getBroadcast(mContext, 0, new Intent(ACTION_USB_PERMISSION), 0);
        usbManager.requestPermission(device, mPendingIntent);
    }

    private ISPort isCdc = new ISPort() {
        @Override
        public boolean isPort(int vid, int pid) {
            final int[][] vid_pid = {{0x0658,0x0200}};
            for(int[] a:vid_pid){
                if(vid == a[0] && pid == a[1])
                    return true;
            }
            return false;
        }
    };

    private ISPort isApm = new ISPort() {
        @Override
        public boolean isPort(int vid, int pid) {
            final int[][] vid_pid = {{0x0658,0x0280}};
            for(int[] a:vid_pid){
                if(vid == a[0] && pid == a[1])
                    return true;
            }
            return false;
        }
    };

    private UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() {
        @Override
        public void onReceivedData(byte[] arg0) {
            buffer.write(arg0);
        }
    };


    private boolean FindDevice(ISPort sel){
        usbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();
        if (!usbDevices.isEmpty()) {
            for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
                device = entry.getValue();
                int deviceVID = device.getVendorId();
                int devicePID = device.getProductId();
                if (sel.isPort(deviceVID, devicePID)) {
                    if (usbManager.hasPermission(device)) {
                        return true;
                    } else {
                        requestUserPermission();
                        return false;
                    }
                }
            }
        }
        return false;
    }

    public int Open()
    {
        if(!FindDevice(isCdc)){
            return -1;
        }
		
        connection = usbManager.openDevice(device);
        
		if(connection == null)
		{
			return -1;
		}
		
		serialPort = new CDCSerialDevice(device, connection,-1);

        if (serialPort.open()) {
            serialPort.setBaudRate(115200);
            serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
            serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
            serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
            serialPort.read(mCallback);
            return 0;
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

    public int OpenApm()
    {
        if(!FindDevice(isApm)){
            return -1;
        }

        connection = usbManager.openDevice(device);

        if(connection == null)
        {
            return -1;
        }

        serialPort = new APMSerialDevice(device, connection,-1);

        if (serialPort.syncOpen()) {
            return 0;
        }

        return -1;
    }
	
    public void WriteApm(byte[] data){
        serialPort.syncWrite(data,1000);
    }

    public int ReadApm(byte[] buf,int len){
        return serialPort.syncRead(buf,2000);
    }

    public void CloseApm(){
        serialPort.syncClose();
    }

    public int GetApmBcdDevice(){
        byte[] des;
        int bcdDevice;
        des = connection.getRawDescriptors();
        bcdDevice = 0xFF & des[12];
        bcdDevice += (0xFF & des[13]) * 0x100;
        Log.d(TAG,"BcdDevice = " + Integer.toHexString(bcdDevice));
        return bcdDevice;
    }

    interface ISPort{
        boolean isPort(int vid,int pid);
    }

    private ByteRingBuffer buffer = new ByteRingBuffer(1024);

    private native void UsbSerial_Set_Object();
}
