package com.vantinviet.foodies.android.ActivitiesAndFragments;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.vantinviet.foodies.android.Constants.Config;
import com.vantinviet.foodies.android.R;
import com.vantinviet.foodies.android.Utils.TabLayoutUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import vn.momo.momo_partner.AppMoMoLib;
import vn.momo.momo_partner.MoMoParameterNamePayment;

import static com.ahmadrosid.lib.drawroutemap.DrawRouteMaps.getContext;

public class Momo extends AppCompatActivity {
    private String amount = "10000";
    private String fee = "0";
    int environment = 2;//developer default
    private String merchantName = "Công ty TNHH Vạn Tín Việt";
    private String merchantCode = "MOMOLWUL20190330";
    private String merchantNameLabel = "NGON365.COM";
    SharedPreferences sPref;
    TextView txt_so_donhang;
    RelativeLayout div_thulai;
    RelativeLayout div_back_to_home;
    AlertDialog.Builder builder;
    RelativeLayout transparent_layer, progressDialog;
    RequestQueue queue;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_momo);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        progressDialog = findViewById(R.id.progressDialog);
        transparent_layer = findViewById(R.id.transparent_layer);
        transparent_layer.setVisibility(View.GONE);
        progressDialog.setVisibility(View.GONE);
        txt_so_donhang = findViewById(R.id.txt_so_donhang);
        div_thulai = (RelativeLayout) findViewById(R.id.div_thulai);

        div_thulai.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestMomoApp();
            }
        });
        div_back_to_home = (RelativeLayout) findViewById(R.id.div_back_to_home);

        builder = new AlertDialog.Builder(this);
        div_back_to_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Uncomment the below code to Set the message and title from the strings.xml file
                builder.setMessage(R.string.dialog_message).setTitle(R.string.dialog_title);

                //Setting message manually and performing action on button click
                builder.setMessage("Chúng tôi đang đợi ứng dụng momo của bạn chạy, bạn có muốn thoát thanh toán momo không ?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                transparent_layer.setVisibility(View.GONE);
                                progressDialog.setVisibility(View.GONE);
                                RestaurantsFragment.ALERT_MOMO_PAY_UNCOMPLETED=true;
                                Intent intent = new Intent(Momo.this, MainActivity.class);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //  Action for 'NO' Button
                                dialog.cancel();

                            }
                        });
                //Creating dialog box
                AlertDialog alert = builder.create();
                //Setting the title manually
                alert.setTitle("Quay về trang chủ");
                alert.show();

            }
        });
        queue = Volley.newRequestQueue(getBaseContext());

        requestMomoApp();

    }

    private void requestMomoApp() {

        Intent intent = getIntent();
        String data_order_detail = intent.getStringExtra("data_order_detail");

        AppMoMoLib.getInstance().setAction(AppMoMoLib.ACTION.PAYMENT);
        AppMoMoLib.getInstance().setActionType(AppMoMoLib.ACTION_TYPE.GET_TOKEN);
        amount = "1";

        Map<String, Object> eventValue = new HashMap<>();
        //client Required
        eventValue.put(MoMoParameterNamePayment.MERCHANT_NAME, merchantName);
        eventValue.put(MoMoParameterNamePayment.MERCHANT_CODE, merchantCode);

        if(environment == 0){
            AppMoMoLib.getInstance().setEnvironment(AppMoMoLib.ENVIRONMENT.DEBUG);
        }else if(environment == 1){
            AppMoMoLib.getInstance().setEnvironment(AppMoMoLib.ENVIRONMENT.DEVELOPMENT);
        }else if(environment == 2){
            AppMoMoLib.getInstance().setEnvironment(AppMoMoLib.ENVIRONMENT.PRODUCTION);
        }


        //client Optional
        //eventValue.put(MoMoParameterNamePayment.MERCHANT_BILL_ID, "merchant_billId_");
        eventValue.put(MoMoParameterNamePayment.FEE, fee);
        eventValue.put(MoMoParameterNamePayment.MERCHANT_NAME_LABEL, merchantNameLabel);

        //client call webview
        eventValue.put(MoMoParameterNamePayment.REQUEST_ID, merchantCode + "merchant_billId_" + System.currentTimeMillis());
        eventValue.put(MoMoParameterNamePayment.PARTNER_CODE, merchantCode);

        JSONObject objExtraData = new JSONObject();
        try {
            JSONArray array_data_order_detail = new JSONArray(data_order_detail);
            JSONObject object_data_order_detail = (JSONObject) array_data_order_detail.get(0);
            JSONObject order_detail = (JSONObject) object_data_order_detail.get("Order");
            long sub_total = 0;
            try {
                sub_total = Math.round(Double.parseDouble(order_detail.getString("sub_total")));
            } catch (NumberFormatException nfe) {
                System.out.println("Could not parse " + nfe);
            }
            //set số tiền để test
            //sub_total = 1;
            Log.d("sub_total %s", String.valueOf(sub_total));
            eventValue.put(MoMoParameterNamePayment.DESCRIPTION, "Thanh toán đơn hàng:" + order_detail.get("id"));
            eventValue.put(MoMoParameterNamePayment.AMOUNT, String.valueOf(sub_total));
            objExtraData.put("site_code", "ngon365.com");
            objExtraData.put("order_id", order_detail.get("id"));
            txt_so_donhang.setText(order_detail.get("id").toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        eventValue.put(MoMoParameterNamePayment.EXTRA_DATA, objExtraData.toString());
        eventValue.put(MoMoParameterNamePayment.REQUEST_TYPE, "payment");
        eventValue.put(MoMoParameterNamePayment.LANGUAGE, "vi");

        eventValue.put(MoMoParameterNamePayment.EXTRA, "");
        AppMoMoLib.getInstance().requestMoMoCallBack(Momo.this, eventValue);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AppMoMoLib.getInstance().REQUEST_CODE_MOMO && resultCode == -1) {
            Log.d("momodata", data.toString());
            if (data != null) {
                if (data.getIntExtra("status", -1) == 0) {
                    Bundle bundle = data.getExtras();
                    for (String key : bundle.keySet()) {
                        Object value = bundle.get(key);
                        Log.d("TAG!", String.format("%s %s (%s)", key,
                                value.toString(), value.getClass().getName()));
                    }
                    String momoExtraData = String.valueOf(data.getStringExtra("extraData")); //Token extraData
                    byte[] currentDataByte = Base64.decode(momoExtraData, Base64.DEFAULT);
                    JSONObject orderDetail = null;
                    JSONObject jsonObjectExtraData = null;
                    try {
                        String extraData = new String(currentDataByte, "UTF-8");
                        Log.d("extraData", extraData);
                        jsonObjectExtraData = new JSONObject(String.valueOf(extraData));
                        int order_id = jsonObjectExtraData.getInt("order_id");
                        updateOrderMomo(order_id);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Log.d("THANHTOAN", String.valueOf(data.getIntExtra("status", -1)));


                    int site_code = data.getIntExtra("site_code", -1);
                    Log.d("site_code", String.valueOf(site_code));
                    Log.d("THANHTOAN", String.valueOf(data.getIntExtra("status", -1)));
                    //TOKEN IS AVAILABLE
                    //TODO thông báo thành công khi thanh toán bằng momo


                    String token = data.getStringExtra("data"); //Token response
                    String phoneNumber = data.getStringExtra("phonenumber");
                    String env = data.getStringExtra("env");
                    if (env == null) {
                        env = "app";
                    }

                    if (token != null && !token.equals("")) {
                        Log.d("THANHTOAN", String.valueOf(data.getIntExtra("status", -1)));
                        // TODO: send phoneNumber & token to your server side to process payment with momo server
                        // IF Momo topup success, continue to process your order
                    } else {
                        Log.d("THANHTOAN", String.valueOf(data.getIntExtra("status", -1)));
                        //tvMessage.setText("message: " + this.getString(R.string.not_receive_info));
                    }
                } else if (data.getIntExtra("status", -1) == 1) {
                    Log.d("THANHTOAN", String.valueOf(data.getIntExtra("status", -1)));
                    //TOKEN FAIL
                    String message = data.getStringExtra("message") != null ? data.getStringExtra("message") : "Thất bại";
                    //tvMessage.setText("message: " + message);
                } else if (data.getIntExtra("status", -1) == 2) {
                    Log.d("THANHTOAN", String.valueOf(data.getIntExtra("status", -1)));
                    //TOKEN FAIL
                    //tvMessage.setText("message: " + this.getString(R.string.not_receive_info));
                } else {
                    Log.d("THANHTOAN", String.valueOf(data.getIntExtra("status", -1)));
                    //TOKEN FAIL
                    //tvMessage.setText("message: " + this.getString(R.string.not_receive_info));
                }
            } else {
                Log.d("THANHTOAN", String.valueOf(data.getIntExtra("status", -1)));
                //tvMessage.setText("message: " + this.getString(R.string.not_receive_info));
            }
        } else {
            RestaurantsFragment.ALERT_MOMO_PAY_UNCOMPLETED=true;
            Intent intent = new Intent(Momo.this, MainActivity.class);
            startActivity(intent);
            //tvMessage.setText("message: " + this.getString(R.string.not_receive_info_err));
        }

    }

    @SuppressLint("LongLogTag")
    private void updateOrderMomo(int order_id) {
        transparent_layer.setVisibility(View.VISIBLE);
        progressDialog.setVisibility(View.VISIBLE);
        Log.d("now update oder_id of momo", String.valueOf(order_id));
        Toast.makeText(getApplicationContext(), "now update oder_id of momo" + String.valueOf(order_id), Toast.LENGTH_SHORT).show();

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("order_id", order_id);
            Log.e("UPDATE_MOMO_ORDER_STATUS", Config.UPDATE_MOMO_ORDER_STATUS);
            Log.e("Obj", jsonObject.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }

// Request a string response from the provided URL.

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                Config.UPDATE_MOMO_ORDER_STATUS, jsonObject,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        Log.d("response save order status", response.toString());
                        String strJson = response.toString();
                        JSONObject jsonResponse = null;
                        try {
                            jsonResponse = new JSONObject(strJson);

                            Log.d("JSON response save order status", jsonResponse.toString());

                            int code_id = Integer.parseInt(jsonResponse.optString("code"));

                            if (code_id == 200) {
                                transparent_layer.setVisibility(View.GONE);
                                progressDialog.setVisibility(View.GONE);
                                RestaurantsFragment.ALERT_MOMO_PAY_COMPLETED=true;
                                Intent intent = new Intent(Momo.this, MainActivity.class);
                                startActivity(intent);
                            } else {
                            }

                            //JSONArray jsonMainNode = jsonResponse.optJSONArray("msg");                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                        //pDialog.hide();
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyLog.d("JSONPost Error %s",error.getMessage());
                        //  Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {
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
        queue.add(jsonObjReq);

    }


}
