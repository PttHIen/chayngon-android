package com.vantinviet.foodies.android.RActivitiesAndFragments;

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
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessaging;
import com.vantinviet.foodies.android.Constants.Config;
import com.vantinviet.foodies.android.Constants.PreferenceClass;
import com.vantinviet.foodies.android.HActivitiesAndFragment.HotelMainActivity;
import com.vantinviet.foodies.android.R;
import com.vantinviet.foodies.android.RActivitiesAndFragments.Services.UpdateLocation;
import com.vantinviet.foodies.android.Utils.NotificationUtils;

/**
 * Created by Nabeel on 1/13/2018.
 */

public class RiderMainActivity extends AppCompatActivity {

    private Fragment mCurrentFrag;
    public static Context context;
    private static final int TIME_INTERVAL = 2000; // # milliseconds, desired time passed between two back presses.
    private long mBackPressed;
    SharedPreferences sPre;
    public static boolean RMAINACTIVITY;
    public static boolean CHAT_FLAG;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE|WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        setContentView(R.layout.activity_main);

        sPre = getSharedPreferences(PreferenceClass.user,MODE_PRIVATE);
        //getDealsList();
        //  getAllOrderParser();

        RMAINACTIVITY = true;

        mCurrentFrag = new RPagerMainFragment();
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
    }

    @Override
    protected void onResume() {
        // Other onResume() code here

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

    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();


        // Other onPause() code here

    }



    @Override
    public void onDestroy() {
        super.onDestroy();

        Intent intent = new Intent(RiderMainActivity.this, UpdateLocation.class);
        stopService(intent);

        // getContext().unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onBackPressed() {

        if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis()) {
            super.onBackPressed();
            RiderMainActivity.this.finish();
            return;
        } else {
            Toast.makeText(getBaseContext(), "Tap Again To Exit", Toast.LENGTH_SHORT).show();


            mBackPressed = System.currentTimeMillis();

        }


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
                    "Foodomia",
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.app_icon)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.app_icon))
                .setColor(getResources().getColor(R.color.colorRed))
                .setContentTitle("Foodomia")
                .setContentIntent(pendingIntent)
                .setContentText(String.format(message))
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setChannelId("channel-01");


        notificationManager.notify(0, builder.build());
    }
}



