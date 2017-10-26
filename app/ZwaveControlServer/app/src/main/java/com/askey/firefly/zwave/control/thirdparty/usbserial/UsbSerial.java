package com.askey.firefly.zwave.control.thirdparty.usbserial;

import android.util.Log;
import java.util.List;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.content.Context;
import android.content.Intent;
import android.app.PendingIntent;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import com.askey.firefly.zwave.control.thirdparty.usbserial.driver.UsbSerialPort;
import com.askey.firefly.zwave.control.thirdparty.usbserial.driver.UsbSerialDriver;
import com.askey.firefly.zwave.control.thirdparty.usbserial.driver.UsbSerialProber;
import com.askey.firefly.zwave.control.thirdparty.usbserial.util.SerialInputOutputManager;

public class UsbSerial {

    static
    {
        System.loadLibrary("usbserial-jni");
    }

    private final String TAG = "UsbSerial";
    private SerialInputOutputManager mSerialIoManager;
    private static UsbSerialPort sPort = null;
    private Context mContext = null;
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    public UsbSerial(Context context)
    {
        mContext = context;
        UsbSerial_Set_Object();
        Log.d(TAG, "UsbSerial Construct");
    }

    private final SerialInputOutputManager.Listener mListener = new SerialInputOutputManager.Listener()
    {
        @Override
        public void onRunError(Exception e) {
            Log.d(TAG, "Runner stopped.");
        }

        @Override
        public void onNewData(final byte[] data) {

            buffer.write(data);
        }
    };

    public int Open()
    {
        UsbManager mUsbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);

        final List<UsbSerialDriver> drivers = UsbSerialProber.getDefaultProber().findAllDrivers(mUsbManager);

        boolean found = false;

        for (final UsbSerialDriver driver : drivers)
        {
            if (found)
                break;

            final List<UsbSerialPort> ports = driver.getPorts();

            for (final UsbSerialPort port : ports)
            {
                //if(!mUsbManager.hasPermission(port.getDriver().getDevice())) {
                //    PendingIntent mPendingIntent = PendingIntent.getBroadcast(mContext, 0, new Intent(ACTION_USB_PERMISSION), 0);
                //    mUsbManager.requestPermission(port.getDriver().getDevice(), mPendingIntent);
                //}

                sPort = port;
                found = true;
                break;
            }
        }

        if(!found)
        {
            Log.d(TAG, "not found device");
            return -1;
        }

        UsbDeviceConnection connection = mUsbManager.openDevice(sPort.getDriver().getDevice());

        if(connection == null)
        {
            return -1;
        }

        try
        {
            sPort.open(connection);
            sPort.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
        }
        catch (IOException e)
        {
            Log.e(TAG, "Error setting up device: " + e.getMessage(), e);

            try
            {
                sPort.close();
            }
            catch (IOException e2)
            {
                // Ignore.
            }

            sPort = null;

            return -1;
        }

        onDeviceStateChange();

        return 0;
    }

    public void Close()
    {
        stopIoManager();

        if(sPort != null)
        {
            try
            {
                sPort.close();
            }
            catch (IOException e2)
            {
                // Ignore.
            }

            sPort = null;
        }
    }

    public void Write(byte[] data)
    {
        if (mSerialIoManager != null)
        {
            mSerialIoManager.writeAsync(data);
        }
    }

    public int Read(byte[] data, int len)
    {
        return buffer.read(data, 0, len);
    }

    public int Check()
    {
        return buffer.getUsed();
    }

    private void stopIoManager()
    {
        if (mSerialIoManager != null)
        {
            Log.i(TAG, "Stopping io manager ..");
            mSerialIoManager.stop();
            mSerialIoManager = null;
        }
    }

    private void startIoManager()
    {
        if (sPort != null)
        {
            Log.i(TAG, "Starting io manager ..");
            mSerialIoManager = new SerialInputOutputManager(sPort, mListener);
            mExecutor.submit(mSerialIoManager);
        }
    }

    private void onDeviceStateChange()
    {
        stopIoManager();
        startIoManager();
    }

    private ByteRingBuffer buffer = new ByteRingBuffer(1024);

    private native void UsbSerial_Set_Object();
}
