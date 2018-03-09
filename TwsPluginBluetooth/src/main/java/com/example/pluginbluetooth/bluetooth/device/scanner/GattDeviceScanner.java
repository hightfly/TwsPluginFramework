package com.example.pluginbluetooth.bluetooth.device.scanner;


import android.content.Context;
import android.os.Handler;

import com.example.pluginbluetooth.bluetooth.gatt.DeviceScanner;
import com.example.pluginbluetooth.bluetooth.gatt.GattDevice;
import com.example.pluginbluetooth.bluetooth.gatt.ScanListener;

import qrom.component.log.QRomLog;

public class GattDeviceScanner implements ScanListener {

    private static final String TAG = "rick_Print:GattDeviceScanner";
    private final Context mContext;
    private final Handler mHandler = new Handler();
    private String mDeviceAddress;
    private GattDeviceScannerListener mListener;

    private DeviceScanner mScanner;

    private final Runnable mScanTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            onScanFinished(null);
        }
    };

    public GattDeviceScanner(final Context context) {
        mContext = context;
    }

    public void startScan(String deviceAddress, long maxScanTimeMS,
                          GattDeviceScannerListener listener) {
        mDeviceAddress = deviceAddress;
        mListener = listener;
        mHandler.removeCallbacks(mScanTimeoutRunnable);

        mHandler.postDelayed(mScanTimeoutRunnable, maxScanTimeMS);

        mScanner = new DeviceScanner(mContext, this);
        mScanner.start();
    }

    public void stopScan() {
        mScanner.stop();
        mHandler.removeCallbacks(mScanTimeoutRunnable);
        mListener = null;
    }

    @Override
    public void onScanResult(final GattDevice device) {
        QRomLog.i(TAG, "[onScanResult] form GattDeviceScanner = " + device.getAddress());
        if (device != null) {
            String address = device.getAddress();
            if (address != null && address.equals(mDeviceAddress)) {
                onScanFinished(device);
            }
        }
    }

    private void onScanFinished(final GattDevice device) {
        if (mListener != null) {
            mListener.onScanFinished(device);
        }
        stopScan();
    }

    public interface GattDeviceScannerListener {
        void onScanFinished(final GattDevice device);
    }
}
