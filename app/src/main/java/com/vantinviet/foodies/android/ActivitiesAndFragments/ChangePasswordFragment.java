package com.vantinviet.foodies.android.ActivitiesAndFragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.gmail.samehadar.iosdialog.CamomileSpinner;
import com.vantinviet.foodies.android.Constants.Config;
import com.vantinviet.foodies.android.Constants.PreferenceClass;
import com.vantinviet.foodies.android.HActivitiesAndFragment.HProfileFragment;
import com.vantinviet.foodies.android.R;
import com.vantinviet.foodies.android.RActivitiesAndFragments.RProfileFragment;
import com.vantinviet.foodies.android.Utils.TabLayoutUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static android.content.Context.INPUT_METHOD_SERVICE;

/**
 * Created by Nabeel on 12/30/2017.
 */

public class ChangePasswordFragment extends Fragment {

    ImageView back_icon;
    EditText old_password,new_password,confirm_password;
    Button btn_change_pass;
    SharedPreferences sharedPreferences;

    CamomileSpinner changePassProgress;
    RelativeLayout transparent_layer,progressDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.change_password_fragment, container, false);
        sharedPreferences = getContext().getSharedPreferences(PreferenceClass.user, Context.MODE_PRIVATE);

        init(v);

        return v;
    }

    public void init(View v){
        changePassProgress = v.findViewById(R.id.changePassProgress);
        changePassProgress.start();
        progressDialog = v.findViewById(R.id.progressDialog);
        transparent_layer = v.findViewById(R.id.transparent_layer);
        btn_change_pass = v.findViewById(R.id.btn_change_pass);

        old_password = v.findViewById(R.id.ed_old_pass);
        new_password = v.findViewById(R.id.ed_new_pass);
        confirm_password = v.findViewById(R.id.ed_confirm_pass);

        btn_change_pass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (old_password.getText().toString().trim().equals("") || old_password.getText().toString().length()<6) {

                    Toast.makeText(getContext(), "Độ dài mật khẩu không thể ngắn hơn 6!", Toast.LENGTH_SHORT).show();
                    old_password.setError("Độ dài mật khẩu không thể ngắn hơn 6!");

                } else if (new_password.getText().toString().trim().equals("") || new_password.getText().toString().length()<6) {

                    Toast.makeText(getContext(), "Check password length can not be shorter than 6!", Toast.LENGTH_SHORT).show();
                    new_password.setError("Check password length can not be shorter than 6!");
                }else if (confirm_password.getText().toString().trim().equals("") || confirm_password.getText().toString().length()<6) {

                    Toast.makeText(getContext(), "Check password length can not be shorter than 6!", Toast.LENGTH_SHORT).show();
                    confirm_password.setError("Check password length can not be shorter than 6!");
                }
        else {
                    if (new_password.getText().toString().equals(confirm_password.getText().toString())) {

                        changePasswordVollyRequest();
                    } else {
                        Toast.makeText(getContext(), "Password does not match", Toast.LENGTH_LONG).show();
                        confirm_password.setError("Password does not match");
                        new_password.setError("Password does not match");
                        //passwords not matching.please try again
                    }
                }
            }
        });
        back_icon = v.findViewById(R.id.back_icon);

        back_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                try  {
                    InputMethodManager imm = (InputMethodManager)getContext().getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
                } catch (Exception e) {

                }

                if(HProfileFragment.FLAG_ADMIN){
                    HProfileFragment rJobsFragment = new HProfileFragment();
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.replace(R.id.change_pass_main_container, rJobsFragment);
                    transaction.addToBackStack(null);
                    transaction.commit();
                    HProfileFragment.FLAG_ADMIN = false;
                }
                else if(RProfileFragment.FLAG_RIDER){
                    RProfileFragment rJobsFragment = new RProfileFragment();
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.replace(R.id.change_pass_main_container, rJobsFragment);
                    transaction.addToBackStack(null);
                    transaction.commit();
                    RProfileFragment.FLAG_RIDER = true;


                }

                else {
                    UserAccountFragment userAccountFragment = new UserAccountFragment();
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.replace(R.id.change_pass_main_container, userAccountFragment);
                    transaction.addToBackStack(null);
                    transaction.commit();
                }


            }
        });


    }

  public void changePasswordVollyRequest(){
      TabLayoutUtils.enableTabs(PagerMainActivity.tabLayout,false);
      transparent_layer.setVisibility(View.VISIBLE);
      progressDialog.setVisibility(View.VISIBLE);
        String getUser_id = sharedPreferences.getString(PreferenceClass.pre_user_id,"");
        RequestQueue queue = Volley.newRequestQueue(getContext());
       JSONObject jsonObject = new JSONObject();
      try {
          jsonObject.put("user_id",getUser_id);
          jsonObject.put("old_password",old_password.getText().toString());
          jsonObject.put("new_password",new_password.getText().toString());
      } catch (JSONException e) {
          e.printStackTrace();
      }

      Log.d("Config.CHANGE_PASSWORD", Config.CHANGE_PASSWORD);
      Log.d("jsonObject post change", jsonObject.toString());
      JsonObjectRequest requestForChangePass = new JsonObjectRequest(Request.Method.POST, Config.CHANGE_PASSWORD, jsonObject, new Response.Listener<JSONObject>() {
          @Override
          public void onResponse(JSONObject response) {


              Log.d("JSONPost", response.toString());
              String strJson =  response.toString();
              JSONObject jsonResponse = null;

              try {
                  jsonResponse = new JSONObject(strJson);

                  int code_id  = Integer.parseInt(jsonResponse.optString("code"));
                  Log.d("coed", jsonResponse.toString());
                  if (code_id==200){
                      TabLayoutUtils.enableTabs(PagerMainActivity.tabLayout,true);
                      transparent_layer.setVisibility(View.GONE);
                      progressDialog.setVisibility(View.GONE);
                      Toast.makeText(getContext(),"Password Changed Successfully",Toast.LENGTH_LONG).show();
                      if(RProfileFragment.FLAG_RIDER){
                          RProfileFragment rJobsFragment = new RProfileFragment();
                          FragmentTransaction transaction = getFragmentManager().beginTransaction();
                          transaction.replace(R.id.change_pass_main_container, rJobsFragment);
                          transaction.addToBackStack(null);
                          transaction.commit();
                          RProfileFragment.FLAG_RIDER = true;
                          Toast.makeText(getContext(),"Password Changed Successfully",Toast.LENGTH_LONG).show();

                      }
                      else {
                          UserAccountFragment userAccountFragment = new UserAccountFragment();
                          FragmentTransaction transaction = getFragmentManager().beginTransaction();
                          transaction.replace(R.id.change_pass_main_container, userAccountFragment);
                          transaction.addToBackStack(null);
                          transaction.commit();
                          Toast.makeText(getContext(),"Password Changed Successfully",Toast.LENGTH_LONG).show();

                      }
                  }
                  else {
                      TabLayoutUtils.enableTabs(PagerMainActivity.tabLayout,true);
                      transparent_layer.setVisibility(View.GONE);
                      progressDialog.setVisibility(View.GONE);
                      Toast.makeText(getContext(),"Password Not Changed",Toast.LENGTH_LONG).show();
                  }

              } catch (JSONException e) {
                  e.printStackTrace();
              }

          }
      }, new Response.ErrorListener() {
          @Override
          public void onErrorResponse(VolleyError error) {
              TabLayoutUtils.enableTabs(PagerMainActivity.tabLayout,true);
              transparent_layer.setVisibility(View.GONE);
              progressDialog.setVisibility(View.GONE);
              Log.d("Volly Error",error.toString());
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
