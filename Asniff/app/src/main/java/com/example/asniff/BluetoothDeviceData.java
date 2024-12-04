package com.example.asniff;

public class BluetoothDeviceData {
    private String deviceName;
    private String macAddress;

    public BluetoothDeviceData(String deviceName, String macAddress) {
        this.deviceName = deviceName;
        this.macAddress = macAddress;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getMacAddress() {
        return macAddress;
    }
}