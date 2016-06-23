package com.example.administrator.schoolbluetooth20.bean;

import android.bluetooth.BluetoothDevice;

import java.io.Serializable;
import java.text.SimpleDateFormat;

/**
 * Created by Administrator on 2016/3/13.
 */
public class BLEObj implements Serializable {

    private String address;
    private BluetoothDevice device;
    private int rssi;
    private String timestamp;
    private boolean status;
    private boolean isConnected = false;

    public boolean isConnected() {
        return isConnected;
    }

    public void setIsConnected(boolean isConnected) {
        this.isConnected = isConnected;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long currenttime) {
        SimpleDateFormat sdf =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        this.timestamp = sdf.format(currenttime);
    }
    public void setTimestamp(String currenttime) {
        this.timestamp = currenttime;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public void setDevice(BluetoothDevice device) {
        this.device = device;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

}
