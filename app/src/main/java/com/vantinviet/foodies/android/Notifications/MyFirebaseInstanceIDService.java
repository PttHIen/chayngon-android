package com.vantinviet.foodies.android.Notifications;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.vantinviet.foodies.android.Constants.Config;

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {
    private static final String TAG = "MyFirebaseIIDService";

    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        // Saving reg id to shared preferences
        storeRegIdInPref(refreshedToken);

        //  eRWir4ppw-Y:APA91bGVX0vY0PboEgQCq9BdNexaxSNs9-bK0VyDiAjJ0WmW2gpwLuJpj2MYRbRRxytegtU6Nu_G-ekeB3L_ad9mVCfeKB7vpqDtaPX3aTbc8RES_aU2Pm17AHpHQ3v1Hl7q8Kx0SCV2
        // sending reg id to your server
        sendRegistrationToServer(refreshedToken);

        // Notify UI that registration has completed, so the progress indicator can be hidden.
        Intent registrationComplete = new Intent(Config.REGISTRATION_COMPLETE);
        registrationComplete.putExtra("token", refreshedToken);
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }

    private void sendRegistrationToServer(final String token) {
        // sending gcm token to server
        Log.e(TAG, "sendRegistrationToServer: " + token);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        DatabaseReference fcmDatabaseRef = ref.child("FCM_Device_Tokens").push();
        FCM_Device_Tokens obj = new FCM_Device_Tokens();
        obj.setToken(token);
        fcmDatabaseRef.setValue(obj);
    }


    private void storeRegIdInPref(String token) {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("regId", token);
        editor.commit();
    }
}
