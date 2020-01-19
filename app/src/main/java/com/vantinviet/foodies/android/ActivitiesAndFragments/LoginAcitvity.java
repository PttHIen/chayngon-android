package com.vantinviet.foodies.android.ActivitiesAndFragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
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
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.facebook.accountkit.AccountKitLoginResult;
import com.facebook.accountkit.PhoneNumber;
import com.facebook.accountkit.ui.AccountKitActivity;
import com.facebook.accountkit.ui.AccountKitConfiguration;
import com.facebook.accountkit.ui.LoginType;
import com.facebook.accountkit.ui.SkinManager;
import com.facebook.accountkit.ui.UIManager;
import com.gmail.samehadar.iosdialog.CamomileSpinner;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.iid.FirebaseInstanceId;
import com.vantinviet.foodies.android.Constants.AllConstants;
import com.vantinviet.foodies.android.Constants.Config;
import com.vantinviet.foodies.android.Constants.PreferenceClass;
import com.vantinviet.foodies.android.HActivitiesAndFragment.HotelMainActivity;
import com.vantinviet.foodies.android.R;
import com.vantinviet.foodies.android.RActivitiesAndFragments.RiderMainActivity;
import com.vantinviet.foodies.android.Utils.FontHelper;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.vantinviet.foodies.android.Utils.TabLayoutUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static com.facebook.FacebookSdk.getApplicationContext;
import static com.vantinviet.foodies.android.ActivitiesAndFragments.AddressListFragment.CART_NOT_LOAD;


/**
 * Created by RaoMudassar on 12/5/17.
 */

public class LoginAcitvity extends Fragment implements View.OnClickListener,GoogleApiClient.OnConnectionFailedListener,
            GoogleApiClient.ConnectionCallbacks{

    SharedPreferences sPref;
    RelativeLayout btn_sign_up_now,fb_div;
    public static boolean LOGIN_FLAG;

    FrameLayout fb_login_layout,login_main_div;

    CamomileSpinner logInProgress;
    RelativeLayout transparent_layer,progressDialog,google_sign_in_div,phone_sign_in_div;

    Button log_in_now,btn_google;
    TextView fb_btn;

    TextView loginText,tv_email,tv_pass,sign_up_txt,tv_forget_password,tv_signed_up_now,tv_sign_up;

    EditText ed_email,ed_password;
    LoginButton login_button_fb;
    public static int APP_REQUEST_CODE_FACEBOOK_VERIFY_PHONE = 99;
    public static int APP_REQUEST_CODE_GOOGLE = 123;
    ImageView back_icon;


    public static final Pattern EMAIL_ADDRESS_PATTERN = Pattern.compile(
            "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                    "\\@" +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                    "(" +
                    "\\." +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                    ")+"
    );

    CallbackManager callbackManager;
    public static GoogleSignInClient  mGoogleSignInClient;


    @SuppressWarnings("deprecation")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.login_activity, container, false);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE|WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                |WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        String languageToLoad = "vi";
        Locale locale = new Locale(languageToLoad);
        Configuration config = new Configuration();
        config.locale = locale;
        getContext().getResources().updateConfiguration(config, getContext().getResources().getDisplayMetrics());
        AccountKit.initialize(getApplicationContext());
        sPref = getContext().getSharedPreferences(PreferenceClass.user,Context.MODE_PRIVATE);


        ed_email = (EditText)v.findViewById(R.id.ed_email);
        ed_password =(EditText)v.findViewById(R.id.ed_password);
        log_in_now = (Button)v.findViewById(R.id.btn_login);


        // Google SignIn Initialize

        String serverClientId = getResources().getString(R.string.google_api_client_id);

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(getContext(), gso);
    //    signInButton = v.findViewById(R.id.sign_in_button);

        // End//

        /// FB Login
        FacebookSdk.sdkInitialize(getContext());
        AppEventsLogger.activateApp(getContext());

        callbackManager = CallbackManager.Factory.create();

        /// End

        tv_sign_up = v.findViewById(R.id.tv_sign_up);
        tv_signed_up_now = v.findViewById(R.id.tv_signed_up_now);
        FontHelper.applyFont(getContext(),tv_sign_up, AllConstants.verdana);

        fb_btn = v.findViewById(R.id.fb_btn);
        fb_div = v.findViewById(R.id.fb_div);
        fb_div.setOnClickListener(this);
        fb_btn.setOnClickListener(this);
        login_button_fb = (LoginButton) v.findViewById(R.id.login_button_fb);
        login_button_fb.setReadPermissions(Arrays.asList("email"));


        // If using in a fragment
        login_button_fb.setFragment(this);
        // Callback registration

        back_icon = v.findViewById(R.id.back_icon);
        back_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try  {
                    InputMethodManager imm = (InputMethodManager)getContext().getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
                } catch (Exception e) {

                }

                UserAccountFragment userAccountFragment = new UserAccountFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.login_main_div, userAccountFragment);
                transaction.addToBackStack(null);
                transaction.commit();
                LoginManager.getInstance().logOut();
               /* CartFragment.CART_ADDRESS = true;
               */
                //LOGIN_FLAG = true;
            }
        });


        // Callback registration
        login_button_fb.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                final AccessToken accessToken = loginResult.getAccessToken();
               // final String id = Profile.getCurrentProfile().getId().toString();
                GraphRequest request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject user, GraphResponse graphResponse) {
                        String useremail = user.optString("email");

                        String FName = user.optString("first_name");
                        String LName = user.optString("last_name");
                        String ID = user.optString("id");

                        SignInFacebookAccount(ID,useremail, FName, LName);

                        //loginFacebook(useremail,ID);

                    }
                });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "last_name,first_name,email");
                request.setParameters(parameters);
                request.executeAsync();


            }

            @Override
            public void onCancel() {
                // App code
                Toast.makeText(getContext(),"Cancle",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
                Toast.makeText(getContext(),"Error",Toast.LENGTH_SHORT).show();
            }
        });

        login_main_div = v.findViewById(R.id.login_main_div);
        login_main_div.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

                return false;
            }
        });

        google_sign_in_div = v.findViewById(R.id.google_sign_up_div);
        google_sign_in_div.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getContext());
                if (acct != null) {
                    String Fname = acct.getGivenName();
                    String Lname = acct.getFamilyName();
                    String Email = acct.getEmail();
                    String Password = acct.getId();
                    SignInGoogleAcount(Email,Fname,Lname);
                }
                else {
                    Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                    startActivityForResult(signInIntent, 123);
                }
            }
        });
        phone_sign_in_div = v.findViewById(R.id.phone_sign_in_div);
        phone_sign_in_div.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(getContext(), AccountKitActivity.class);
                AccountKitConfiguration.AccountKitConfigurationBuilder configurationBuilder = new AccountKitConfiguration.AccountKitConfigurationBuilder(
                        LoginType.PHONE,AccountKitActivity.ResponseType.TOKEN); // or .ResponseType.CODE
                UIManager uiManager = new SkinManager(
                        LoginType.PHONE,
                        SkinManager.Skin.TRANSLUCENT,
                        (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? getResources().getColor(R.color.colorPrimary,null):getResources().getColor(R.color.colorPrimary)),
                        R.drawable.welcome_bg,
                        SkinManager.Tint.WHITE,
                        0.55
                );
                /*If you want default country code*/
                // configurationBuilder.setDefaultCountryCode("IN");
                configurationBuilder.setUIManager(uiManager);
                final String[] whiteList
                        = getResources().getStringArray(R.array.whitelistedSmsCountryCodes);
                configurationBuilder.setSMSWhitelist(whiteList);
                intent.putExtra(AccountKitActivity.ACCOUNT_KIT_ACTIVITY_CONFIGURATION,configurationBuilder.build());
                startActivityForResult(intent, APP_REQUEST_CODE_FACEBOOK_VERIFY_PHONE);
            }
        });



        btn_sign_up_now = (RelativeLayout) v.findViewById(R.id.btn_sign_up_now);
        tv_email = (TextView)v.findViewById(R.id.tv_email);
        tv_pass = (TextView)v.findViewById(R.id.tv_password);
        sign_up_txt = (TextView)v.findViewById(R.id.tv_sign_up);

        logInProgress = v.findViewById(R.id.logInProgress);
        logInProgress.start();
        progressDialog = v.findViewById(R.id.progressDialog);
        transparent_layer = v.findViewById(R.id.transparent_layer);

        loginText = (TextView)v.findViewById(R.id.login_title);
        tv_forget_password = v.findViewById(R.id.tv_forget_password);
        tv_forget_password.setOnClickListener(this);
        FontHelper.applyFont(getContext(),tv_forget_password, AllConstants.arial);

        //

        log_in_now.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                boolean valid = checkEmail(ed_email.getText().toString());

                if (ed_email.getText().toString().trim().equals("")) {

                    Toast.makeText(getContext(), "Vui lòng nhập email!", Toast.LENGTH_SHORT).show();

                } else if (ed_password.getText().toString().trim().equals("")) {

                    Toast.makeText(getContext(), "Vui lòng nhập mật khẩu!", Toast.LENGTH_SHORT).show();
                }else if (ed_password.getText().toString().length()<6) {

                    Toast.makeText(getContext(), "Vui lòng nhập mật khẩu ít nhất 6 ký tự!", Toast.LENGTH_SHORT).show();
                }
                else if (!valid) {

                    Toast.makeText(getContext(), "Vui lòng nhập email hợp lệ!", Toast.LENGTH_SHORT).show();
                }else {

                   String this_email = ed_email.getText().toString();
                   String this_password = ed_password.getText().toString();
                    CART_NOT_LOAD = true;
                    login(this_email,this_password);

                }
            }
        });

//        btn_sign_up_now.setTypeface(TypeFace.getTypeface(getApplicationContext(),TypeFace.arial))


        btn_sign_up_now.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Fragment restaurantMenuItemsFragment = new SingUpActivity();
                FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                transaction.add(R.id.login_main_div, restaurantMenuItemsFragment,"parent").commit();

                LoginManager.getInstance().logOut();
            }
        });



        return v;
    }

    private void login(String email,String pass){
        //Getting values from edit texts
        TabLayoutUtils.enableTabs(PagerMainActivity.tabLayout,false);
        transparent_layer.setVisibility(View.VISIBLE);
        progressDialog.setVisibility(View.VISIBLE);


        String _lat = sPref.getString(PreferenceClass.LATITUDE,"");
        String _long = sPref.getString(PreferenceClass.LONGITUDE,"");
        String device_tocken = FirebaseInstanceId.getInstance().getToken();

        //Creating a string request
        RequestQueue queue = Volley.newRequestQueue(getContext());
        String url = Config.LOGIN_URL;
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("email", email);
            jsonObject.put("password", pass);
            jsonObject.put("device_token", device_tocken);

            if(_lat.isEmpty()){
                jsonObject.put("lat", "31.5042483");
            }else {
                jsonObject.put("lat", _lat);
            }
            if(_long.isEmpty()){
                jsonObject.put("long", "74.3307944");
            }else {
                jsonObject.put("long", _long);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
// Request a string response from the provided URL.
        Log.d("jsonObject login",jsonObject.toString());
        Log.d("url login",url);
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                url,jsonObject,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        Log.d("JSONPost8", response.toString());
                        String strJson =  response.toString();
                        JSONObject jsonResponse = null;
                        try {
                            jsonResponse = new JSONObject(strJson);

                            Log.d("JSONPost9", jsonResponse.toString());

                            int code_id  = Integer.parseInt(jsonResponse.optString("code"));

                            if(code_id == 200) {
                                TabLayoutUtils.enableTabs(PagerMainActivity.tabLayout,true);
                                transparent_layer.setVisibility(View.GONE);
                                progressDialog.setVisibility(View.GONE);
                                JSONObject json = new JSONObject(jsonResponse.toString());
                                JSONObject resultObj = json.getJSONObject("msg");
                             //Đã chạy qua đây
                                Log.d("resultObj",resultObj.toString());

                                JSONObject resultObj1 = resultObj.getJSONObject("UserInfo");
                                Log.d("UserInfo",resultObj1.toString());
                                JSONObject resultObj2 = resultObj.getJSONObject("User");
                                Log.d("User",resultObj2.toString());
                                JSONObject resultObj3 = resultObj.getJSONObject("Admin");
                                Log.d("Admin",resultObj3.toString());

                                SharedPreferences.Editor editor = sPref.edit();
                                editor.putString(PreferenceClass.pre_email, ed_email.getText().toString());
                                editor.putString(PreferenceClass.pre_pass, ed_password.getText().toString());
                                editor.putString(PreferenceClass.pre_first, resultObj1.optString("first_name"));
                                editor.putString(PreferenceClass.pre_last, resultObj1.optString("last_name"));
                                editor.putString(PreferenceClass.pre_contact, resultObj1.optString("phone"));
                                editor.putString(PreferenceClass.pre_user_id, resultObj1.optString("user_id"));
                                String admin_user_id=resultObj3.optString("user_id");
                                editor.putString(PreferenceClass.ADMIN_USER_ID,admin_user_id);
                                editor.putString(PreferenceClass.ADMIN_PHONE_NUMBER,resultObj3.optString("phone"));

                                editor.putBoolean(PreferenceClass.IS_LOGIN, true);
                                editor.commit();
                                OrderDetailFragment.CALLBACK_ORDERFRAG = true;

                                if(resultObj2.optString("role").equalsIgnoreCase("rider")){

                                    editor.putString(PreferenceClass.USER_TYPE,resultObj2.optString("role"));
                                    editor.commit();
                                    startActivity(new Intent(getContext(),RiderMainActivity.class));
                                    getActivity().finish();

                                }

                                else if(resultObj2.optString("role").equalsIgnoreCase("user")) {
                                    editor.putString(PreferenceClass.USER_TYPE,resultObj2.optString("role"));
                                    editor.commit();
                                   if(CartFragment.CART_LOGIN){
                                       startActivity(new Intent(getContext(), MainActivity.class));
                                       getActivity().finish();
                                   }
                                   else {
                                       startActivity(new Intent(getContext(), MainActivity.class));
                                       getActivity().finish();
                                   }


                                }

                                else  if(resultObj2.optString("role").equalsIgnoreCase("hotel")){

                                    editor.putString(PreferenceClass.USER_TYPE,resultObj2.optString("role"));
                                    editor.commit();
                                    startActivity(new Intent(getContext(),HotelMainActivity.class));
                                    getActivity().finish();

                                }
                                Toast.makeText(getContext(), "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(getContext(), "Sai tài khoản hoặc mật khẩu", Toast.LENGTH_SHORT).show();
                                TabLayoutUtils.enableTabs(PagerMainActivity.tabLayout,true);
                                transparent_layer.setVisibility(View.GONE);
                                progressDialog.setVisibility(View.GONE);
                                JSONObject json = new JSONObject(jsonResponse.toString());

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
                TabLayoutUtils.enableTabs(PagerMainActivity.tabLayout,true);
                transparent_layer.setVisibility(View.GONE);
                progressDialog.setVisibility(View.GONE);
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

// Add the request to the RequestQueue.
        queue.add(jsonObjReq);
    }
    private void loginPhoneNUmber(String phone_number){
        //Getting values from edit texts
        TabLayoutUtils.enableTabs(PagerMainActivity.tabLayout,false);
        transparent_layer.setVisibility(View.VISIBLE);
        progressDialog.setVisibility(View.VISIBLE);


        String _lat = sPref.getString(PreferenceClass.LATITUDE,"");
        String _long = sPref.getString(PreferenceClass.LONGITUDE,"");
        String device_tocken = FirebaseInstanceId.getInstance().getToken();

        //Creating a string request
        RequestQueue queue = Volley.newRequestQueue(getContext());
        String url = Config.LOGIN_PHONE_URL;
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("phone_number", phone_number);
            jsonObject.put("device_token", device_tocken);

            if(_lat.isEmpty()){
                jsonObject.put("lat", "31.5042483");
            }else {
                jsonObject.put("lat", _lat);
            }
            if(_long.isEmpty()){
                jsonObject.put("long", "74.3307944");
            }else {
                jsonObject.put("long", _long);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
// Request a string response from the provided URL.
        Log.d("jsonObject login",jsonObject.toString());
        Log.d("url login phone number",url);
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                url,jsonObject,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        Log.d("JSONPost8", response.toString());
                        String strJson =  response.toString();
                        JSONObject jsonResponse = null;
                        try {
                            jsonResponse = new JSONObject(strJson);

                            Log.d("JSONPost9", jsonResponse.toString());

                            int code_id  = Integer.parseInt(jsonResponse.optString("code"));

                            if(code_id == 200) {
                                TabLayoutUtils.enableTabs(PagerMainActivity.tabLayout,true);
                                transparent_layer.setVisibility(View.GONE);
                                progressDialog.setVisibility(View.GONE);
                                JSONObject json = new JSONObject(jsonResponse.toString());
                                JSONObject resultObj = json.getJSONObject("msg");
                             //Đã chạy qua đây
                                Log.d("resultObj",resultObj.toString());

                                JSONObject resultObjUserInfo = resultObj.getJSONObject("UserInfo");
                                Log.d("UserInfo",resultObjUserInfo.toString());
                                JSONObject resultObjUser = resultObj.getJSONObject("User");
                                Log.d("User",resultObjUser.toString());
                                JSONObject resultObjUserAdmin = resultObj.getJSONObject("Admin");
                                Log.d("Admin",resultObjUserAdmin.toString());

                                SharedPreferences.Editor editor = sPref.edit();
                                editor.putString(PreferenceClass.pre_email, resultObjUser.optString("email"));
                                editor.putString(PreferenceClass.pre_pass, resultObjUser.optString("password"));
                                editor.putString(PreferenceClass.pre_first, resultObjUserInfo.optString("first_name"));
                                editor.putString(PreferenceClass.pre_last, resultObjUserInfo.optString("last_name"));
                                editor.putString(PreferenceClass.pre_contact, resultObjUserInfo.optString("phone"));
                                editor.putString(PreferenceClass.pre_user_id, resultObjUserInfo.optString("user_id"));
                                String admin_user_id=resultObjUserAdmin.optString("user_id");
                                editor.putString(PreferenceClass.ADMIN_USER_ID,admin_user_id);
                                editor.putString(PreferenceClass.ADMIN_PHONE_NUMBER,resultObjUserAdmin.optString("phone"));

                                editor.putBoolean(PreferenceClass.IS_LOGIN, true);
                                editor.commit();
                                OrderDetailFragment.CALLBACK_ORDERFRAG = true;

                                if(resultObjUser.optString("role").equalsIgnoreCase("rider")){

                                    editor.putString(PreferenceClass.USER_TYPE,resultObjUser.optString("role"));
                                    editor.commit();
                                    startActivity(new Intent(getContext(),RiderMainActivity.class));
                                    getActivity().finish();

                                }

                                else if(resultObjUser.optString("role").equalsIgnoreCase("user")) {
                                    editor.putString(PreferenceClass.USER_TYPE,resultObjUser.optString("role"));
                                    editor.commit();
                                   if(CartFragment.CART_LOGIN){
                                       startActivity(new Intent(getContext(), MainActivity.class));
                                       getActivity().finish();
                                   }
                                   else {
                                       startActivity(new Intent(getContext(), MainActivity.class));
                                       getActivity().finish();
                                   }


                                }

                                else  if(resultObjUser.optString("role").equalsIgnoreCase("hotel")){

                                    editor.putString(PreferenceClass.USER_TYPE,resultObjUser.optString("role"));
                                    editor.commit();
                                    startActivity(new Intent(getContext(),HotelMainActivity.class));
                                    getActivity().finish();

                                }
                                Toast.makeText(getContext(), "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(getContext(), "Sai tài khoản hoặc mật khẩu", Toast.LENGTH_SHORT).show();
                                TabLayoutUtils.enableTabs(PagerMainActivity.tabLayout,true);
                                transparent_layer.setVisibility(View.GONE);
                                progressDialog.setVisibility(View.GONE);
                                JSONObject json = new JSONObject(jsonResponse.toString());

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
                TabLayoutUtils.enableTabs(PagerMainActivity.tabLayout,true);
                transparent_layer.setVisibility(View.GONE);
                progressDialog.setVisibility(View.GONE);
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

// Add the request to the RequestQueue.
        queue.add(jsonObjReq);
    }
    private void loginFacebook(String email,String pass){
        //Getting values from edit texts
        TabLayoutUtils.enableTabs(PagerMainActivity.tabLayout,false);
        transparent_layer.setVisibility(View.VISIBLE);
        progressDialog.setVisibility(View.VISIBLE);


        String _lat = sPref.getString(PreferenceClass.LATITUDE,"");
        String _long = sPref.getString(PreferenceClass.LONGITUDE,"");
        String device_tocken = FirebaseInstanceId.getInstance().getToken();

        //Creating a string request
        RequestQueue queue = Volley.newRequestQueue(getContext());
        String url = Config.LOGIN_URL;
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("email", email);
            jsonObject.put("password", pass);
            jsonObject.put("device_token", device_tocken);
            jsonObject.put("login_by", "facebook");

            if(_lat.isEmpty()){
                jsonObject.put("lat", "31.5042483");
            }else {
                jsonObject.put("lat", _lat);
            }
            if(_long.isEmpty()){
                jsonObject.put("long", "74.3307944");
            }else {
                jsonObject.put("long", _long);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
// Request a string response from the provided URL.
        Log.d("jsonObject login",jsonObject.toString());
        Log.d("url login",url);
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                url,jsonObject,
                new Response.Listener<JSONObject>() {

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
                                TabLayoutUtils.enableTabs(PagerMainActivity.tabLayout,true);
                                transparent_layer.setVisibility(View.GONE);
                                progressDialog.setVisibility(View.GONE);
                                JSONObject json = new JSONObject(jsonResponse.toString());
                                JSONObject resultObj = json.getJSONObject("msg");
                                JSONObject json1 = new JSONObject(resultObj.toString());
                                JSONObject resultObj1 = json1.getJSONObject("UserInfo");
                                JSONObject resultObj2 = json1.getJSONObject("User");
                                JSONObject resultObj3 = json1.getJSONObject("Admin");

                                SharedPreferences.Editor editor = sPref.edit();
                                editor.putString(PreferenceClass.pre_email, ed_email.getText().toString());
                                editor.putString(PreferenceClass.pre_pass, ed_password.getText().toString());
                                editor.putString(PreferenceClass.pre_first, resultObj1.optString("first_name"));
                                editor.putString(PreferenceClass.pre_last, resultObj1.optString("last_name"));
                                editor.putString(PreferenceClass.pre_contact, resultObj1.optString("phone"));
                                editor.putString(PreferenceClass.pre_user_id, resultObj1.optString("user_id"));
                                String admin_user_id=resultObj3.optString("user_id");
                                editor.putString(PreferenceClass.ADMIN_USER_ID,admin_user_id);
                                editor.putString(PreferenceClass.ADMIN_PHONE_NUMBER,resultObj3.optString("phone"));

                                editor.putBoolean(PreferenceClass.IS_LOGIN, true);
                                editor.commit();

                                OrderDetailFragment.CALLBACK_ORDERFRAG = true;

                                if(resultObj2.optString("role").equalsIgnoreCase("rider")){

                                    editor.putString(PreferenceClass.USER_TYPE,resultObj2.optString("role"));
                                    editor.commit();
                                    startActivity(new Intent(getContext(),RiderMainActivity.class));
                                    getActivity().finish();

                                }

                                else if(resultObj2.optString("role").equalsIgnoreCase("user")) {

                                    editor.putString(PreferenceClass.USER_TYPE,resultObj2.optString("role"));
                                    editor.commit();
                                   if(CartFragment.CART_LOGIN){

                                       startActivity(new Intent(getContext(), MainActivity.class));
                                       getActivity().finish();
                                   }
                                   else {
                                       startActivity(new Intent(getContext(), MainActivity.class));
                                       getActivity().finish();
                                   }


                                }

                                else  if(resultObj2.optString("role").equalsIgnoreCase("hotel")){

                                    editor.putString(PreferenceClass.USER_TYPE,resultObj2.optString("role"));
                                    editor.commit();
                                    startActivity(new Intent(getContext(),HotelMainActivity.class));
                                    getActivity().finish();

                                }
                                Toast.makeText(getContext(), "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(getContext(), "Sai tài khoản hoặc mật khẩu", Toast.LENGTH_SHORT).show();
                                TabLayoutUtils.enableTabs(PagerMainActivity.tabLayout,true);
                                transparent_layer.setVisibility(View.GONE);
                                progressDialog.setVisibility(View.GONE);
                                JSONObject json = new JSONObject(jsonResponse.toString());

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
                TabLayoutUtils.enableTabs(PagerMainActivity.tabLayout,true);
                transparent_layer.setVisibility(View.GONE);
                progressDialog.setVisibility(View.GONE);
                VolleyLog.d("JSONPost", "Error: " + error.getMessage());
                Toast.makeText(getContext(), "Sai tài khoản hoặc mật khẩu", Toast.LENGTH_SHORT).show();
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

// Add the request to the RequestQueue.
        queue.add(jsonObjReq);
    }


    public void googleSignIn(){

    }

    private boolean checkEmail(String email) {
        return EMAIL_ADDRESS_PATTERN.matcher(email).matches();
    }


    @Override
    public void onClick(View view) {
        if(view == fb_div){
            login_button_fb.performClick();
        }
        if(view==fb_btn){
            login_button_fb.performClick();
        }
        else if(view==tv_forget_password){

            Fragment restaurantMenuItemsFragment = new RecoverPasswordFragment();
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            transaction.add(R.id.login_main_div, restaurantMenuItemsFragment,"parent").commit();
         //   LOGIN_FLAG = true;

        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @SuppressLint("LongLogTag")
    private void handleSignInResultGoogle(GoogleSignInResult result) {
        Log.d("handleSignInResultGoogle", "handleSignInResultGoogle:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();

            //   Log.e(TAG, "display name: " + acct.getDisplayName());
            String Fname = acct.getGivenName();
            String Lname = acct.getFamilyName();
            String Email = acct.getEmail();
            SignInGoogleAcount(Email,Fname,Lname);
        }
    }
    @SuppressLint("LongLogTag")
    private void SignInGoogleAcount(String email, String f_name, String l_name) {
        //Getting values from edit texts
        TabLayoutUtils.enableTabs(PagerMainActivity.tabLayout, false);
        transparent_layer.setVisibility(View.VISIBLE);
        progressDialog.setVisibility(View.VISIBLE);
        //Creating a string request
        RequestQueue queue = Volley.newRequestQueue(getContext());
        String url = Config.GOOGLE_SIGNUP_URL;
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("email", email);
            jsonObject.put("device_token", "123");
            jsonObject.put("first_name", f_name);
            jsonObject.put("last_name", l_name);
            jsonObject.put("role", "user");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("jsonObjectSignUpGoogle", url);
        Log.d("jsonObjectSignUpGoogleLink", jsonObject.toString());
// Request a string response from the provided URL.
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                url, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        TabLayoutUtils.enableTabs(PagerMainActivity.tabLayout, true);
                        transparent_layer.setVisibility(View.GONE);
                        progressDialog.setVisibility(View.GONE);
                        Log.d("JSONPost", response.toString());
                        String strJson = response.toString();
                        JSONObject jsonResponse = null;
                        try {
                            jsonResponse = new JSONObject(strJson);
                            Log.d("JSONPost", jsonResponse.toString());
                            int code_id = Integer.parseInt(jsonResponse.optString("code"));
                            if (code_id == 200) {
                                TabLayoutUtils.enableTabs(PagerMainActivity.tabLayout,true);
                                transparent_layer.setVisibility(View.GONE);
                                progressDialog.setVisibility(View.GONE);
                                JSONObject json = new JSONObject(jsonResponse.toString());
                                JSONObject resultObj = json.getJSONObject("msg");
                                //Đã chạy qua đây
                                Log.d("resultObj",resultObj.toString());

                                JSONObject resultObjUserInfo = resultObj.getJSONObject("UserInfo");
                                Log.d("UserInfo",resultObjUserInfo.toString());
                                JSONObject resultObjUser = resultObj.getJSONObject("User");
                                Log.d("User",resultObjUser.toString());
                                JSONObject resultObjUserAdmin = resultObj.getJSONObject("Admin");
                                Log.d("Admin",resultObjUserAdmin.toString());

                                SharedPreferences.Editor editor = sPref.edit();
                                editor.putString(PreferenceClass.pre_email, resultObjUser.optString("email"));
                                editor.putString(PreferenceClass.pre_pass, resultObjUser.optString("password"));
                                editor.putString(PreferenceClass.pre_first, resultObjUserInfo.optString("first_name"));
                                editor.putString(PreferenceClass.pre_last, resultObjUserInfo.optString("last_name"));
                                editor.putString(PreferenceClass.pre_contact, resultObjUserInfo.optString("phone"));
                                editor.putString(PreferenceClass.pre_user_id, resultObjUserInfo.optString("user_id"));
                                String admin_user_id=resultObjUserAdmin.optString("user_id");
                                editor.putString(PreferenceClass.ADMIN_USER_ID,admin_user_id);
                                editor.putString(PreferenceClass.ADMIN_PHONE_NUMBER,resultObjUserAdmin.optString("phone"));

                                editor.putBoolean(PreferenceClass.IS_LOGIN, true);
                                editor.commit();
                                OrderDetailFragment.CALLBACK_ORDERFRAG = true;

                                if(resultObjUser.optString("role").equalsIgnoreCase("rider")){

                                    editor.putString(PreferenceClass.USER_TYPE,resultObjUser.optString("role"));
                                    editor.commit();
                                    startActivity(new Intent(getContext(),RiderMainActivity.class));
                                    getActivity().finish();

                                }

                                else if(resultObjUser.optString("role").equalsIgnoreCase("user")) {
                                    editor.putString(PreferenceClass.USER_TYPE,resultObjUser.optString("role"));
                                    editor.commit();
                                    if(CartFragment.CART_LOGIN){
                                        startActivity(new Intent(getContext(), MainActivity.class));
                                        getActivity().finish();
                                    }
                                    else {
                                        startActivity(new Intent(getContext(), MainActivity.class));
                                        getActivity().finish();
                                    }


                                }

                                else  if(resultObjUser.optString("role").equalsIgnoreCase("hotel")){

                                    editor.putString(PreferenceClass.USER_TYPE,resultObjUser.optString("role"));
                                    editor.commit();
                                    startActivity(new Intent(getContext(),HotelMainActivity.class));
                                    getActivity().finish();

                                }
                                Toast.makeText(getContext(), "Đăng nhập thành công", Toast.LENGTH_SHORT).show();

                            } else {
                                TabLayoutUtils.enableTabs(PagerMainActivity.tabLayout, true);
                                transparent_layer.setVisibility(View.GONE);
                                progressDialog.setVisibility(View.GONE);
                                JSONObject json = new JSONObject(jsonResponse.toString());
                                Toast.makeText(getContext(), json.optString("msg"), Toast.LENGTH_SHORT).show();
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
                TabLayoutUtils.enableTabs(PagerMainActivity.tabLayout, true);
                transparent_layer.setVisibility(View.GONE);
                progressDialog.setVisibility(View.GONE);
                VolleyLog.d("JSONPost", "Error: " + error.getMessage());
                // Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
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
// Add the request to the RequestQueue.
        queue.add(jsonObjReq);
    }

    public void Storedata(String fname,String lname,String email,String password){

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
        //login with Gmail
        if(requestCode==APP_REQUEST_CODE_GOOGLE){
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResultGoogle(result);

          //  handleSignInResultGoogle(task);
        }else if (requestCode == APP_REQUEST_CODE_FACEBOOK_VERIFY_PHONE) { // confirm that this response matches your request
            AccountKitLoginResult loginResult = data.getParcelableExtra(AccountKitLoginResult.RESULT_KEY);
            String toastMessage;
            if (loginResult.getError() != null) {
                toastMessage = loginResult.getError().getErrorType().getMessage();
                //showErrorActivity(loginResult.getError());
            } else if (loginResult.wasCancelled()) {
                toastMessage = "Login Cancelled";
            } else {
                if (loginResult.getAccessToken() != null) {
                    toastMessage = "Success:" + loginResult.getAccessToken().getAccountId();
                    // Get email
                    //String email = loginResult.getAccessToken();
                    //Log.d("Email kit",email);
                } else {
                    toastMessage = String.format(
                            "Success:%s...",
                            loginResult.getAuthorizationCode().substring(0,10));
                }

                // If you have an authorization code, retrieve it from
                loginResult.getAuthorizationCode();
                // and pass it to your server and exchange it for an access token.

                // Success! Start your next activity...
                goToFaceookLoggedInActivityAffterVerifyPhone();
            }

            // Surface the result to your user in an appropriate way.
            Toast.makeText(
                    getContext(),
                    toastMessage,
                    Toast.LENGTH_LONG)
                    .show();
        }
        else {

        }
      /*  else if(mCallbackManager.onActivityResult(requestCode, resultCode, data)){
            return;
        }*/
    }
    @SuppressLint("LongLogTag")
    private void SignInFacebookAccount(String facebookId,String email, String f_name, String l_name) {
        //Getting values from edit texts
        TabLayoutUtils.enableTabs(PagerMainActivity.tabLayout, false);
        transparent_layer.setVisibility(View.VISIBLE);
        progressDialog.setVisibility(View.VISIBLE);
        //Creating a string request
        RequestQueue queue = Volley.newRequestQueue(getContext());
        String url = Config.FACEBOOK_SIGNUP_URL;
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("email", email);
            jsonObject.put("device_token", "123");
            jsonObject.put("facebookId", facebookId);
            jsonObject.put("first_name", f_name);
            jsonObject.put("last_name", l_name);
            jsonObject.put("role", "user");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("jsonObjectSignUpFacebook", url);
        Log.d("jsonObjectSignUpFacebookLink", jsonObject.toString());
// Request a string response from the provided URL.
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                url, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        TabLayoutUtils.enableTabs(PagerMainActivity.tabLayout, true);
                        transparent_layer.setVisibility(View.GONE);
                        progressDialog.setVisibility(View.GONE);
                        Log.d("JSONPost", response.toString());
                        String strJson = response.toString();
                        JSONObject jsonResponse = null;
                        try {
                            jsonResponse = new JSONObject(strJson);
                            Log.d("JSONPost", jsonResponse.toString());
                            int code_id = Integer.parseInt(jsonResponse.optString("code"));
                            if (code_id == 200) {
                                TabLayoutUtils.enableTabs(PagerMainActivity.tabLayout,true);
                                transparent_layer.setVisibility(View.GONE);
                                progressDialog.setVisibility(View.GONE);
                                JSONObject json = new JSONObject(jsonResponse.toString());
                                JSONObject resultObj = json.getJSONObject("msg");
                                //Đã chạy qua đây
                                Log.d("resultObj",resultObj.toString());

                                JSONObject resultObjUserInfo = resultObj.getJSONObject("UserInfo");
                                Log.d("UserInfo",resultObjUserInfo.toString());
                                JSONObject resultObjUser = resultObj.getJSONObject("User");
                                Log.d("User",resultObjUser.toString());
                                JSONObject resultObjUserAdmin = resultObj.getJSONObject("Admin");
                                Log.d("Admin",resultObjUserAdmin.toString());

                                SharedPreferences.Editor editor = sPref.edit();
                                editor.putString(PreferenceClass.pre_email, resultObjUser.optString("email"));
                                editor.putString(PreferenceClass.pre_pass, resultObjUser.optString("password"));
                                editor.putString(PreferenceClass.pre_first, resultObjUserInfo.optString("first_name"));
                                editor.putString(PreferenceClass.pre_last, resultObjUserInfo.optString("last_name"));
                                editor.putString(PreferenceClass.pre_contact, resultObjUserInfo.optString("phone"));
                                editor.putString(PreferenceClass.pre_user_id, resultObjUserInfo.optString("user_id"));
                                String admin_user_id=resultObjUserAdmin.optString("user_id");
                                editor.putString(PreferenceClass.ADMIN_USER_ID,admin_user_id);
                                editor.putString(PreferenceClass.ADMIN_PHONE_NUMBER,resultObjUserAdmin.optString("phone"));

                                editor.putBoolean(PreferenceClass.IS_LOGIN, true);
                                editor.commit();
                                OrderDetailFragment.CALLBACK_ORDERFRAG = true;

                                if(resultObjUser.optString("role").equalsIgnoreCase("rider")){

                                    editor.putString(PreferenceClass.USER_TYPE,resultObjUser.optString("role"));
                                    editor.commit();
                                    startActivity(new Intent(getContext(),RiderMainActivity.class));
                                    getActivity().finish();

                                }

                                else if(resultObjUser.optString("role").equalsIgnoreCase("user")) {
                                    editor.putString(PreferenceClass.USER_TYPE,resultObjUser.optString("role"));
                                    editor.commit();
                                    if(CartFragment.CART_LOGIN){
                                        startActivity(new Intent(getContext(), MainActivity.class));
                                        getActivity().finish();
                                    }
                                    else {
                                        startActivity(new Intent(getContext(), MainActivity.class));
                                        getActivity().finish();
                                    }


                                }

                                else  if(resultObjUser.optString("role").equalsIgnoreCase("hotel")){

                                    editor.putString(PreferenceClass.USER_TYPE,resultObjUser.optString("role"));
                                    editor.commit();
                                    startActivity(new Intent(getContext(),HotelMainActivity.class));
                                    getActivity().finish();

                                }
                                Toast.makeText(getContext(), "Đăng nhập thành công", Toast.LENGTH_SHORT).show();

                            } else {
                                TabLayoutUtils.enableTabs(PagerMainActivity.tabLayout, true);
                                transparent_layer.setVisibility(View.GONE);
                                progressDialog.setVisibility(View.GONE);
                                JSONObject json = new JSONObject(jsonResponse.toString());
                                Toast.makeText(getContext(), json.optString("msg"), Toast.LENGTH_SHORT).show();
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
                TabLayoutUtils.enableTabs(PagerMainActivity.tabLayout, true);
                transparent_layer.setVisibility(View.GONE);
                progressDialog.setVisibility(View.GONE);
                VolleyLog.d("JSONPost", "Error: " + error.getMessage());
                // Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
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
// Add the request to the RequestQueue.
        queue.add(jsonObjReq);
    }

    private void goToFaceookLoggedInActivityAffterVerifyPhone() {
        com.facebook.accountkit.AccessToken accessToken = AccountKit.getCurrentAccessToken();
        if (accessToken != null) {
            //Handle Returning User
            AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                @Override
                public void onSuccess(final Account account) {
                    // Get Account Kit ID
                    String accountKitId = account.getId();
                    Log.e("Account Kit Id", accountKitId);

                    if(account.getPhoneNumber()!=null) {
                        Log.e("CountryCode1", "" + account.getPhoneNumber().getCountryCode());
                        Log.e("PhoneNumber1", "" + account.getPhoneNumber().getPhoneNumber());

                        // Get phone number
                        PhoneNumber phoneNumber = account.getPhoneNumber();
                        String phoneNumberString = phoneNumber.toString();
                        Log.e("NumberString1", phoneNumberString);
                        loginPhoneNUmber(phoneNumberString);
                    }

                    if(account.getEmail()!=null)
                        Log.e("Email1",account.getEmail());
                }

                @Override
                public void onError(final AccountKitError error) {
                    // Handle Error
                    Log.e("ERROR1",error.toString());
                }
            });
        } else {
            //Handle new or logged out user
            Log.e("ERROR2","Logged Out");
        }


    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

   /* @Override
    public void onBackPressed() {
        super.onBackPressed();
        LoginManager.getInstance().logOut();
    }*/
}
