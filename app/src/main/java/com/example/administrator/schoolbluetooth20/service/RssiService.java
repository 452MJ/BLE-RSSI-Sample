package com.example.administrator.schoolbluetooth20.service;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

import com.example.administrator.schoolbluetooth20.activity.MainActivity;
import com.example.administrator.schoolbluetooth20.bean.BLEObj;

import java.util.HashMap;
import java.util.Map;

public class RssiService extends Service {
    private Map<String, BluetoothGatt> mGattMap = new HashMap<>();
    private Map<String, BLEObj> mConnectedMap = new HashMap<>();
    private MyBinder mBinder = new MyBinder();
    private Handler mHandler;

    public RssiService() {
    }



    public class MyBinder extends Binder{
        public void setHandler(Handler handler){
            mHandler = handler;
        }
        public Map<String, BLEObj> getConnectedMap(){
            return mConnectedMap;
        }
        public RssiService getService(){
            return RssiService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    if (mHandler != null){
                        mHandler.sendEmptyMessage(MainActivity.SERVICE_RSSI);
                        for (String key : mGattMap.keySet()) {
                            mGattMap.get(key).connect();
                            mGattMap.get(key).readRemoteRssi();
                        }
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disconnectBLE();
    }

    public boolean connectBLE(BLEObj obj){
        String address = obj.getAddress();
        BluetoothDevice device = obj.getDevice();
        BluetoothGatt gatt = null;
        //如果当前设备没有添加GATT回调监听
        if (!mGattMap.containsKey(address)){
            //创建新的GATT监听回调
            gatt = device.connectGatt(this, false, mCallback);
            mGattMap.put(address, gatt);

            if (mConnectedMap.containsKey(address) == false && mGattMap.size() <= 5) {
                mConnectedMap.put(address, obj);
                return true;
            }
        }else {
            gatt = mGattMap.get(address);
        }
        return false;
    }

    public void disconnectBLE(){
        for (String key : mGattMap.keySet()){
            mGattMap.get(key).disconnect();
            mGattMap.get(key).close();
        }
        mGattMap.clear();
        mConnectedMap.clear();
    }

    public void disconnectBLE(BLEObj obj) {
        mGattMap.get(obj.getAddress()).disconnect();
        mGattMap.get(obj.getAddress()).close();
        mGattMap.remove(obj.getAddress());
        mConnectedMap.remove(obj.getAddress());
    }

    BluetoothGattCallback mCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == BluetoothProfile.STATE_CONNECTED){
                gatt.readRemoteRssi();
            }
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
            String address = gatt.getDevice().getAddress();
            BLEObj obj = mConnectedMap.get(address);
            obj.setRssi(rssi);
            obj.setTimestamp(System.currentTimeMillis());
        }
    };
}
