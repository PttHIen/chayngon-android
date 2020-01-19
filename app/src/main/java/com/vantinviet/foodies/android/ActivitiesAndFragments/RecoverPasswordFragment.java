package com.vantinviet.foodies.android.ActivitiesAndFragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.gmail.samehadar.iosdialog.CamomileSpinner;
import com.vantinviet.foodies.android.Constants.AllConstants;
import com.vantinviet.foodies.android.Constants.Config;
import com.vantinviet.foodies.android.Constants.PreferenceClass;
import com.vantinviet.foodies.android.R;
import com.vantinviet.foodies.android.Utils.FontHelper;
import com.vantinviet.foodies.android.Utils.TabLayoutUtils;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RecoverPasswordFragment extends Fragment {

    ImageView back_icon;
    Button btn_recover;
    EditText recover_email;
    SharedPreferences sharedPreferences;
    CamomileSpinner progressBar;
    RelativeLayout transparent_layer, progressDialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE|WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                |WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        View v = inflater.inflate(R.layout.activity_recover_password, container, false);
        sharedPreferences = getContext().getSharedPreferences(PreferenceClass.user,Context.MODE_PRIVATE);

        FrameLayout frameLayout = v.findViewById(R.id.profile_main_container);
        FontHelper.applyFont(getContext(),frameLayout, AllConstants.verdana);
        initUI(v);

        return v;

    }

    public void initUI(View v){

        progressBar = v.findViewById(R.id.signUpProgress);
        progressBar.start();
        progressDialog = v.findViewById(R.id.progressDialog);
        transparent_layer = v.findViewById(R.id.transparent_layer);

        recover_email = v.findViewById(R.id.recover_email);

        btn_recover = v.findViewById(R.id.btn_recover);
        btn_recover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
        recoverPasswordVollyRequest();
            }
        });

        back_icon = v.findViewById(R.id.back_icon);
        back_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment restaurantMenuItemsFragment = new LoginAcitvity();
                FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                transaction.add(R.id.main_recover_pass, restaurantMenuItemsFragment, "ParentFragment").commit();
            }
        });
    }

    public void recoverPasswordVollyRequest(){

        TabLayoutUtils.enableTabs(PagerMainActivity.tabLayout, false);
        transparent_layer.setVisibility(View.VISIBLE);
        progressDialog.setVisibility(View.VISIBLE);
       // String user_email = sharedPreferences.getString(PreferenceClass.pre_email,"");
        RequestQueue queue = Volley.newRequestQueue(getContext());
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("email",recover_email.getText().toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }


        JsonObjectRequest requestForChangePass = new JsonObjectRequest(Request.Method.POST, Config.FORGOT_PASSWORD, jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {


                Log.d("JSONPost", response.toString());
                String strJson =  response.toString();
                JSONObject jsonResponse = null;

                try {
                    jsonResponse = new JSONObject(strJson);

                    int code_id  = Integer.parseInt(jsonResponse.optString("code"));
                    if (code_id==200){

                        TabLayoutUtils.enableTabs(PagerMainActivity.tabLayout, true);
                        transparent_layer.setVisibility(View.GONE);
                        progressDialog.setVisibility(View.GONE);
                        Toast.makeText(getContext(),"Password sent to your given email",Toast.LENGTH_LONG).show();
                        Fragment restaurantMenuItemsFragment = new LoginAcitvity();
                        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                        transaction.add(R.id.main_recover_pass, restaurantMenuItemsFragment, "ParentFragment").commit();




                        /*startActivity(new Intent(getContext(),LoginAcitvity.class));
                        finish();*/
                    }
                    else {
                        Toast.makeText(getContext(),"Email của bạn không đúng",Toast.LENGTH_LONG).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                VolleyLog.d("JSONPost", "Error: " + error.getMessage());
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

        queue.add(requestForChangePass);

    }

}
