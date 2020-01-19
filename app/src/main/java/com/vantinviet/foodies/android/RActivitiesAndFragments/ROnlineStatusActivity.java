package com.vantinviet.foodies.android.RActivitiesAndFragments;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.vantinviet.foodies.android.Constants.Config;
import com.vantinviet.foodies.android.Constants.PreferenceClass;
import com.vantinviet.foodies.android.R;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Nabeel on 2/9/2018.
 */

public class ROnlineStatusActivity extends AppCompatActivity {

    ImageView back_icon;
    SwitchCompat on_line_switch;
    Context context;
    SharedPreferences sPref;
    String user_id, online_status, hotel_phone_number,phone_number_final;

    ProgressBar pb_online_status;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rider_online_status);
        sPref = getSharedPreferences(PreferenceClass.user, MODE_PRIVATE);
        online_status = sPref.getString(PreferenceClass.RIDER_ONLINE_STATUS, "");
        hotel_phone_number = sPref.getString(PreferenceClass.ADMIN_PHONE_NUMBER, "");

        context = ROnlineStatusActivity.this;
        initView();
    }

    public void initView() {

        pb_online_status = findViewById(R.id.pb_online_status);
        back_icon = findViewById(R.id.back_icon);
        on_line_switch = findViewById(R.id.on_line_switch);

        back_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(context,RiderMainActivity.class));
                finish();
            }
        });

        if (online_status.equalsIgnoreCase("1")) {
            on_line_switch.setChecked(true);

        } else {
            on_line_switch.setChecked(false);
        }

        on_line_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                if (!b) {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
                    builder1.setMessage("Trước tiên bạn phải gọi điện hoặc trò chuyện với chúng tôi.");
                    builder1.setTitle("Chọn gọi điện hoặc nhắn tin!");
                    builder1.setCancelable(true);
                    on_line_switch.setChecked(true);

                    builder1.setPositiveButton(
                            "Gọi",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    phoneCall();
                                    dialog.cancel();
                                }
                            });

                    builder1.setNegativeButton(
                            "Nhắn tin",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    RiderMainActivity.CHAT_FLAG = true;
                                    startActivity(new Intent(context,RiderMainActivity.class));
                                    finish();
                                    dialog.cancel();
                                }
                            });

                    AlertDialog alert11 = builder1.create();
                    alert11.show();
                }

                else {
                    getOnlineStatus();
                    pb_online_status.setVisibility(View.VISIBLE);
                }
            }
        });

    }

    public void getOnlineStatus() {

        user_id = sPref.getString(PreferenceClass.pre_user_id, "");

        RequestQueue queue = Volley.newRequestQueue(context);
        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("user_id", user_id);
            jsonObject.put("online", "1");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Config.UPDATE_RIDER_STATUS, jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                String str = response.toString();

                try {
                    JSONObject jsonObject1 = new JSONObject(str);

                    int code = Integer.parseInt(jsonObject1.optString("code"));

                    if (code==200){

                        Toast.makeText(context,"Successfully Updated",Toast.LENGTH_SHORT).show();
                        pb_online_status.setVisibility(View.VISIBLE);
                        startActivity(new Intent(context,RiderMainActivity.class));
                        finish();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

             //   Toast.makeText(context, error.toString(), Toast.LENGTH_SHORT).show();

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

        queue.add(jsonObjectRequest);

    }

    public void phoneCall() {

        AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
        builder1.setMessage("Trước tiên bạn phải gọi điện hoặc trò chuyện với chúng tôi.");
        builder1.setTitle("Gọi ngay !");
        builder1.setCancelable(true);

        builder1.setPositiveButton(
                "Gọi",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                      onCall();

                        dialog.cancel();
                    }
                });

        builder1.setNegativeButton(
                "Hủy bỏ",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();

    }


    public void onCall() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CALL_PHONE},
                    123);
        } else {
            startActivity(new Intent(Intent.ACTION_CALL).setData(Uri.parse("tel:"+getResources().getString(R.string.admin_contact_number))));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {

            case 123:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    onCall();
                } else {
                    Log.d("TAG", "Call Permission Not Granted");
                }
                break;

            default:
                break;
        }
    }

}
