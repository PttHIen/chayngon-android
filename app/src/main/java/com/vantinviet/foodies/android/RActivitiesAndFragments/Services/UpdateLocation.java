package com.vantinviet.foodies.android.RActivitiesAndFragments.Services;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.vantinviet.foodies.android.ActivitiesAndFragments.MainActivity;
import com.vantinviet.foodies.android.Constants.AllConstants;
import com.vantinviet.foodies.android.Constants.Config;
import com.vantinviet.foodies.android.Constants.PreferenceClass;
import com.vantinviet.foodies.android.RActivitiesAndFragments.RiderModels.ChatMessage;
import com.vantinviet.foodies.android.RActivitiesAndFragments.RiderModels.TrackingModelClass;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Nabeel on 1/17/2018.
 */

public class UpdateLocation extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    //BGTask bgtask = new BGTask(this);

    public static String data;
    Intent intent;
    private static final String TAG = MainActivity.class.getSimpleName();

    private static Location mLastLocation;

    private GoogleApiClient mGoogleApiClient;

    private LocationRequest mLocationRequest;

    // Location updates intervals in sec
    private static int UPDATE_INTERVAL = 2000;
    private static int FATEST_INTERVAL = 2000;
    private static int DISPLACEMENT = 0;
 //   public static String str_receiver = "com.microsolstechnologies.android.android";

    DatabaseReference mDatabase;
    FirebaseDatabase firebaseDatabase;
    SharedPreferences pending_job_pref;
    String user_id;
    private static String previousLat = "0.0";
    private static String previousLong = "0.0";
    SharedPreferences sharedPreferences;
    final class Mythreadclass implements Runnable {


        @Override
        public void run() {

            mLocationRequest.setInterval(UPDATE_INTERVAL);
            mLocationRequest.setFastestInterval(FATEST_INTERVAL);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mLocationRequest.setSmallestDisplacement(DISPLACEMENT);

        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
        sharedPreferences =getBaseContext().getSharedPreferences(PreferenceClass.user,MODE_PRIVATE);
        buildGoogleApiClient();
        createLocationRequest();
        mGoogleApiClient.connect();
        intent = new Intent();

        pending_job_pref = getSharedPreferences(PreferenceClass.user, Context.MODE_PRIVATE);
        user_id = pending_job_pref.getString(PreferenceClass.pre_user_id, "");

        firebaseDatabase = FirebaseDatabase.getInstance();
        mDatabase = firebaseDatabase.getReference().child(AllConstants.TRACKING).child(user_id);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
        Thread thread = new Thread(new Mythreadclass());
        thread.start();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
      //  Toast.makeText(this, "destroyed", Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }


    private void displayLocation() {


        // mLastLocation = LocationServices.FusedLocationApi
        //       .getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {
            double latitude = mLastLocation.getLatitude();
            double longitude = mLastLocation.getLongitude();
           // data = Double.toString(latitude) + "\n" + Double.toString(longitude);
           // Toast.makeText(this, data, Toast.LENGTH_SHORT).show();
            DecimalFormat decimalFormat = new DecimalFormat("#.#######");

            previousLat = String.valueOf(Double.parseDouble(previousLat)+Double.parseDouble("0.0"));
            previousLong = String.valueOf(Double.parseDouble(previousLong)+Double.parseDouble("0.0"));;
            mDatabase.keepSynced(true);
            String first_name = sharedPreferences.getString(PreferenceClass.pre_first, "");
            String last_name = sharedPreferences.getString(PreferenceClass.pre_last,"");
            String pre_user_id = sharedPreferences.getString(PreferenceClass.pre_user_id,"");

            Log.d("Rider Location lat",decimalFormat.format(latitude).toString());
            Log.d("Rider Location long",decimalFormat.format(longitude).toString());
            Log.d("Rider Location prev lat",previousLat);
            Log.d("Rider Location pre Long",previousLong);
            Log.d("now update location","");
            // Bug bị out khi đăng nhập account Shipper
            mDatabase.setValue(new TrackingModelClass(""+decimalFormat.format(latitude),""+decimalFormat.format(longitude),previousLat,previousLong,first_name+" "+last_name,pre_user_id));

            previousLat = ""+latitude;
            previousLong = ""+longitude;

           // fn_update(latitude,longitude);

           /* BGTask bgtask = new BGTask(this);
            bgtask.execute(Double.toString(latitude), Double.toString(longitude));*/

        } else {


            Toast.makeText(this, "Không thể có được vị trí. Đảm bảo vị trí được bật trên thiết bị", Toast.LENGTH_SHORT).show();
        }
    }


    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    /**
     * Creating location request object
     * */
    protected void createLocationRequest() {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }


    protected void startLocationUpdates() {
        mGoogleApiClient.connect();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);

    }


    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }


    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode());
    }

    @Override
    public void onConnected(Bundle arg0) {

        // Once connected with google api, get the location
     //   Toast.makeText(this,"connected",Toast.LENGTH_SHORT).show();
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        // Assign the new location
        mLastLocation = location;


       /* Toast.makeText(getApplicationContext(), "Locatn changed!",
                Toast.LENGTH_SHORT).show();*/

        // Displaying the new location on UI
        displayLocation();
        saveRiderLocation(location);


    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void fn_update(double lat,double lng){

        intent.putExtra("latutide",lat+"");
        intent.putExtra("longitude",lng+"");
        sendBroadcast(intent);
    }


    public void saveRiderLocation(Location location){

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("user_id",user_id);
            jsonObject.put("lat",location.getLatitude());
            jsonObject.put("long",location.getLongitude());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("Save location rider",Config.ADD_LOCATIONS);
        Log.d("Location rider",jsonObject.toString());
        JsonObjectRequest updateLocationRequest = new JsonObjectRequest(Request.Method.POST, Config.ADD_LOCATIONS, jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                Log.d("JSONPost", response.toString());
                String strJson =  response.toString();
                JSONObject jsonResponse = null;
                try {
                    jsonResponse = new JSONObject(strJson);

                    Log.d("JSONPost", jsonResponse.toString());

                    int code_id  = Integer.parseInt(jsonResponse.optString("code"));

                    if(code_id == 200) {

                        Log.e("MSG",jsonResponse.toString());

                    }

                }catch (Exception e){

                    e.getMessage();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("api-key", "2a5588cf-4cf3-4f1c-9548-cc1db4b54ae3");
                return headers;
            }
        };

        queue.add(updateLocationRequest);



    }


  /*  boolean isGPSEnable = false;
    boolean isNetworkEnable = false;
    double latitude, longitude;
    LocationManager locationManager;
    Location location;
    private Handler mHandler = new Handler();
    private Timer mTimer = null;
    long notify_interval = 5000;
    public static String str_receiver = "com.microsolstechnologies.android.android";
    Intent intent;
    DatabaseReference mDatabase;
    FirebaseDatabase firebaseDatabase;

    SharedPreferences sPref;

    private static String previousLat="0.0";
    private static String previousLong="0.0";
    String user_id;

    public UpdateLocation() {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sPref = getApplicationContext().getSharedPreferences(PreferenceClass.user,MODE_PRIVATE);
        user_id = sPref.getString(PreferenceClass.pre_user_id,"");
        mTimer = new Timer();
        mTimer.schedule(new TimerTaskToGetLocation(), 5, notify_interval);
        intent = new Intent(str_receiver);

        firebaseDatabase = FirebaseDatabase.getInstance();
        mDatabase = firebaseDatabase.getReference().child(AllConstants.TRACKING).child(user_id);


      //  fn_getlocation();
    }

    @Override
    public void onLocationChanged(Location location_) {

       // fn_update(location);
        previousLat = String.valueOf(Double.parseDouble(previousLat)+Double.parseDouble("0.0"));
        previousLong = String.valueOf(Double.parseDouble(previousLong)+Double.parseDouble("0.0"));;

        mDatabase.setValue(new TrackingModelClass(""+location_.getLatitude(),""+location_.getLongitude(),previousLat,previousLong));

        previousLat = ""+location_.getLatitude();
        previousLong = ""+location_.getLongitude();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private void fn_getlocation() {
        locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        isGPSEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnable = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!isGPSEnable && !isNetworkEnable) {

        } else {

            if (isNetworkEnable) {
                location = null;
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
                if (locationManager!=null){
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if (location!=null){

                        Log.e("latitude",location.getLatitude()+"");
                        Log.e("longitude",location.getLongitude()+"");

                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        //fn_update(location);

                        saveRiderLocation(location);
                    }
                }

            }

        else
            if (isGPSEnable){
                location = null;
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,this);
                if (locationManager!=null){
                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (location!=null){
                        Log.e("latitude",location.getLatitude()+"");
                        Log.e("longitude",location.getLongitude()+"");
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        //fn_update(location);
                        saveRiderLocation(location);
                    }
                }
            }

        }

    }

    private class TimerTaskToGetLocation extends TimerTask {
        @Override
        public void run() {

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    fn_getlocation();

                }
            });

        }
    }

    private void fn_update(Location location){

        intent.putExtra("latutide",location.getLatitude()+"");
        intent.putExtra("longitude",location.getLongitude()+"");
        sendBroadcast(intent);
    }

    public void saveRiderLocation(Location location){

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("user_id",user_id);
            jsonObject.put("lat",location.getLatitude());
            jsonObject.put("long",location.getLongitude());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest updateLocationRequest = new JsonObjectRequest(Request.Method.POST, Config.ADD_LOCATIONS, jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                Log.d("JSONPost", response.toString());
                String strJson =  response.toString();
                JSONObject jsonResponse = null;
                try {
                    jsonResponse = new JSONObject(strJson);

                    Log.d("JSONPost", jsonResponse.toString());

                    int code_id  = Integer.parseInt(jsonResponse.optString("code"));

                    if(code_id == 200) {

                        Log.e("MSG",jsonResponse.toString());

                    }

                }catch (Exception e){

                    e.getMessage();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        queue.add(updateLocationRequest);

    }*/

}
