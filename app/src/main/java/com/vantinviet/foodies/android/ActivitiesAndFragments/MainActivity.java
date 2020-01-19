package com.vantinviet.foodies.android.ActivitiesAndFragments;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.vantinviet.foodies.android.Adapters.RestaurantsAdapter;

import com.vantinviet.foodies.android.Constants.Config;
import com.vantinviet.foodies.android.Constants.PreferenceClass;

import com.vantinviet.foodies.android.HActivitiesAndFragment.HotelMainActivity;
import com.vantinviet.foodies.android.Models.UserDevice;
import com.vantinviet.foodies.android.R;
import com.vantinviet.foodies.android.Utils.FontHelper;
import com.vantinviet.foodies.android.Utils.NotificationUtils;

import vn.momo.momo_partner.AppMoMoLib;


public class MainActivity extends FragmentActivity {
    private Fragment mCurrentFrag;
    public static Context context;
    private static final int TIME_INTERVAL = 2000; // # milliseconds, desired time passed between two back presses.
    private long mBackPressed;
    public static SharedPreferences sPre;
    public static boolean FLAG_MAIN;
    public static RecyclerView.LayoutManager recyclerViewlayoutManager;
    public static RestaurantsAdapter recyclerViewadapter;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference mDatabaseUser;
    private SharedPreferences currentUser;
    String pre_user_id="0";
    String user_token="0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE|WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        sPre = getSharedPreferences(PreferenceClass.user,MODE_PRIVATE);

        FLAG_MAIN = true;
        context = MainActivity.this;

        mCurrentFrag = new PagerMainActivity();
        if(mCurrentFrag!=null) {
            getSupportFragmentManager().beginTransaction().add(R.id.main_container, mCurrentFrag).commit();

        }

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                // checking for type intent filter
                if (intent.getAction().equals(Config.REGISTRATION_COMPLETE)) {
                    // gcm successfully registered
                    // now subscribe to `global` topic to receive app wide notifications
                    FirebaseMessaging.getInstance().subscribeToTopic(Config.TOPIC_GLOBAL);

                    //   displayFirebaseRegId();

                } else if (intent.getAction().equals(Config.PUSH_NOTIFICATION)) {
                    // new push notification is received

                    String message = intent.getStringExtra("message");

                    sendMyNotification(message);

                    sendBroadcast(new Intent("newOrder"));

                    //   Toast.makeText(getApplicationContext(), "Push notification: " + message, Toast.LENGTH_LONG).show();

                    //  txtMessage.setText(message);
                }
            }
        };
        firebaseDatabase = FirebaseDatabase.getInstance();
        saveUserOnline();



    }



    @Override
    public void onBackPressed() {

        if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis()) {
            super.onBackPressed();
            MainActivity.this.finish();
            return;
        } else {
            Toast.makeText(getBaseContext(), "Tap Again To Exit", Toast.LENGTH_SHORT).show();

            mBackPressed = System.currentTimeMillis();

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("hell34343","");
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RestaurantMenuItemsFragment.PERMISSION_DATA_CART_ADED) {

            if(resultCode == RESULT_OK) {
                Intent intent = new Intent();
                intent.setAction("AddToCart");
                sendBroadcast(intent);
                SearchFragment.FLAG_COUNTRY_NAME = true;
            }
        }else if(requestCode == AppMoMoLib.getInstance().REQUEST_CODE_MOMO && resultCode == -1) {
            if(data != null) {
                if(data.getIntExtra("status", -1) == 0) {
                    //TOKEN IS AVAILABLE
                    //TODO thông báo thành công khi thanh toán bằng momo
                    String token = data.getStringExtra("data"); //Token response
                    String phoneNumber = data.getStringExtra("phonenumber");
                    String env = data.getStringExtra("env");
                    if(env == null){
                        env = "app";
                    }

                    if(token != null && !token.equals("")) {
                        // TODO: send phoneNumber & token to your server side to process payment with momo server
                        // IF Momo topup success, continue to process your order
                    } else {
                        //tvMessage.setText("message: " + this.getString(R.string.not_receive_info));
                    }
                } else if(data.getIntExtra("status", -1) == 1) {
                    //TOKEN FAIL
                    String message = data.getStringExtra("message") != null?data.getStringExtra("message"):"Thất bại";
                    //tvMessage.setText("message: " + message);
                } else if(data.getIntExtra("status", -1) == 2) {
                    //TOKEN FAIL
                    //tvMessage.setText("message: " + this.getString(R.string.not_receive_info));
                } else {
                    //TOKEN FAIL
                    //tvMessage.setText("message: " + this.getString(R.string.not_receive_info));
                }
            } else {
                //tvMessage.setText("message: " + this.getString(R.string.not_receive_info));
            }
        } else {
            //tvMessage.setText("message: " + this.getString(R.string.not_receive_info_err));
        }
    }

    @Override
    protected void onResume() {
        // Other onResume() code here
        Log.d("cuong1","23232");
        super.onResume();

        // register GCM registration complete receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.REGISTRATION_COMPLETE));

        // register new push message receiver
        // by doing this, the activity will be notified each time a new message arrives
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.PUSH_NOTIFICATION));

        // clear the notification area when the app is opened
        NotificationUtils.clearNotifications(getApplicationContext());
        saveUserOnline();


    }
    public void saveUserOnline(){
        currentUser = getSharedPreferences(PreferenceClass.user, Context.MODE_PRIVATE);
        String first_name = currentUser.getString(PreferenceClass.pre_first, "");
        String last_name = currentUser.getString(PreferenceClass.pre_last,"");
        String email = currentUser.getString(PreferenceClass.pre_email, "");
        pre_user_id=currentUser.getString(PreferenceClass.pre_user_id, "0");

        mDatabaseUser = firebaseDatabase.getReference().child("usersDevice").child(user_token);
        mDatabaseUser.removeValue();
        user_token=FontHelper.getTokenFirebase(20);
        mDatabaseUser = firebaseDatabase.getReference().child("usersDevice").child(user_token);

        String device_tocken = FirebaseInstanceId.getInstance().getToken();
        Log.d("device_tocken",device_tocken);
        mDatabaseUser.push().setValue(new UserDevice(pre_user_id,first_name+" "+last_name,email,device_tocken,"android"));

    }
    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
        // Other onPause() code here


    }


    private void sendMyNotification(String message) {
        //On click of notification it redirect to this Activity
        Intent intent = new Intent(this, HotelMainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            /* Create or update. */
            NotificationChannel channel = new NotificationChannel("channel-01",
                    "ngon365",
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.app_icon)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.app_icon))
                .setColor(getResources().getColor(R.color.colorRed))
                .setContentTitle("ngon365")
                .setContentIntent(pendingIntent)
                .setContentText(String.format(message))
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setChannelId("channel-01");


        notificationManager.notify(0, builder.build());
    }

}

