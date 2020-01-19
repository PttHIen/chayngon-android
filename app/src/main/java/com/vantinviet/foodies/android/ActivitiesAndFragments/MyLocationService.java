package com.vantinviet.foodies.android.ActivitiesAndFragments;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.LocationResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.vantinviet.foodies.android.Constants.AllConstants;
import com.vantinviet.foodies.android.Constants.PreferenceClass;
import com.vantinviet.foodies.android.RActivitiesAndFragments.RiderModels.TrackingModelClass;
import com.vantinviet.foodies.android.RActivitiesAndFragments.Services.MyLocation;

import static android.content.Context.MODE_PRIVATE;

public class MyLocationService extends BroadcastReceiver {
    DatabaseReference mDatabase;
    private static String previousLat = "0.0";
    private static String previousLong = "0.0";
    SharedPreferences sharedPreferences;
    FirebaseDatabase firebaseDatabase;
    SharedPreferences pending_job_pref;
    public static final String ACTION_PROCESS_UPDATE="com.vantinviet.foodies.android.UPDATE_LOCATION";
    @Override
    public void onReceive(Context context, Intent intent) {
        firebaseDatabase = FirebaseDatabase.getInstance();
        sharedPreferences =context.getSharedPreferences(PreferenceClass.user,MODE_PRIVATE);
        pending_job_pref = context.getSharedPreferences(PreferenceClass.user, Context.MODE_PRIVATE);
        String user_id = pending_job_pref.getString(PreferenceClass.pre_user_id, "");

        mDatabase = firebaseDatabase.getReference().child(AllConstants.TRACKING).child(user_id);
        mDatabase.keepSynced(true);

        if(intent != null && user_id!=""){
            final String action=intent.getAction();
            if(ACTION_PROCESS_UPDATE.equals(action)){
                LocationResult result= LocationResult.extractResult(intent);
                String first_name = sharedPreferences.getString(PreferenceClass.pre_first, "");
                String last_name = sharedPreferences.getString(PreferenceClass.pre_last,"");
                String pre_user_id = sharedPreferences.getString(PreferenceClass.pre_user_id,"");
                previousLat = String.valueOf(Double.parseDouble(previousLat)+Double.parseDouble("0.0"));
                previousLong = String.valueOf(Double.parseDouble(previousLong)+Double.parseDouble("0.0"));;
                if(result!=null){
                    Location location=result.getLastLocation();
                    mDatabase.setValue(new TrackingModelClass(String.valueOf(location.getLatitude()),String.valueOf(location.getLongitude()),previousLat,previousLong,first_name+" "+last_name,pre_user_id));
                    Log.d("Latitude", String.valueOf(location.getLatitude()));
                    Log.d("Long", String.valueOf(location.getLongitude()));
                    previousLat=String.valueOf(location.getLatitude());
                    previousLong=String.valueOf(location.getLongitude());
                }
            }
        }
    }
}
