package com.vantinviet.foodies.android.Notifications;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class FCM_Device_Tokens {
    private String token;

    public FCM_Device_Tokens() {
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
