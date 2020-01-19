package com.vantinviet.foodies.android.Models;

public class UserDevice {
    public String id;
    public String name;
    public String email;
    public String device_tocken;
    public String os;

    // Default constructor required for calls to
    // DataSnapshot.getValue(UserDevice.class)
    public UserDevice() {
    }

    public UserDevice(String id, String name, String email,String device_tocken,String os) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.device_tocken = device_tocken;
        this.os = os;
    }
}
