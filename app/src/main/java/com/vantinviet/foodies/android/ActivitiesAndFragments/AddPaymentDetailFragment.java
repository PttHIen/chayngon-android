package com.vantinviet.foodies.android.ActivitiesAndFragments;

import android.app.DatePickerDialog;
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
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.gmail.samehadar.iosdialog.CamomileSpinner;
import com.vantinviet.foodies.android.Constants.Config;
import com.vantinviet.foodies.android.Constants.PreferenceClass;
import com.vantinviet.foodies.android.R;
import com.vantinviet.foodies.android.Utils.TabLayoutUtils;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Nabeel on 1/1/2018.
 */

public class AddPaymentDetailFragment extends Fragment {
    private SharedPreferences sharedPreferences;
    private String userPublishableKey = "";
    String month,year;
    Card card;
    Token tok;
    Stripe stripe;
    ImageView back_icon;
    Button cancle_credit_card_btn,save_payment_method_btn;
    private Calendar myCalendar;
    private EditText card_number_editText,card_validity,name_on_card,cvv,billing_address_card,city_card,card_state,card_zip;
    CamomileSpinner pbHeaderProgress;
    RelativeLayout transparent_layer,progressDialog;

 //  public static ArrayList<CardDetailModel> cardetailArraylist;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE|WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        |WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        View v = inflater.inflate(R.layout.add_credit_card_detail, container, false);
        init(v);
        sharedPreferences = getContext().getSharedPreferences(PreferenceClass.user, Context.MODE_PRIVATE);

        return v;
    }

    public void init(View v){
        myCalendar = Calendar.getInstance();
        back_icon = v.findViewById(R.id.back_icon);
        pbHeaderProgress = v.findViewById(R.id.pbHeaderProgress);
        pbHeaderProgress.start();
        progressDialog = v.findViewById(R.id.progressDialog);
        transparent_layer = v.findViewById(R.id.transparent_layer);

        cancle_credit_card_btn = v.findViewById(R.id.cancle_credit_card_btn);
        card_number_editText = v.findViewById(R.id.card_number_editText);
        card_validity = v.findViewById(R.id.card_validity);
        name_on_card = v.findViewById(R.id.name_on_card);
        cvv = v.findViewById(R.id.cvv);
        billing_address_card = v.findViewById(R.id.billing_address_card);
        city_card = v.findViewById(R.id.city_card);
        card_state = v.findViewById(R.id.card_state);
        card_zip = v.findViewById(R.id.card_zip);
        save_payment_method_btn = v.findViewById(R.id.save_payment_method_btn);

        save_payment_method_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitCard(view);
                InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                pbHeaderProgress.setVisibility(View.VISIBLE);
            }
        });

        if (AddPaymentFragment.FLAG_FRAGMENT){
            back_icon.setVisibility(View.VISIBLE);
            cancle_credit_card_btn.setVisibility(View.GONE);
            AddPaymentFragment.FLAG_FRAGMENT = false;
        }

        back_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Fragment restaurantMenuItemsFragment = new AddPaymentFragment();
                FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                transaction.add(R.id.add_payment_detail_container, restaurantMenuItemsFragment,"parent").commit();
                InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

            }
        });

        card_number_editText.addTextChangedListener(new PaymentMethodActivity.FourDigitCardFormatWatcher());
        datePickerDialog();

    }

    private void datePickerDialog(){


        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
               myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }

        };

        card_validity.setInputType(0);
        card_validity.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(getContext(), date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

    }

    private void updateLabel() {
        String myFormat = "MM/yy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        card_validity.setText(sdf.format(myCalendar.getTime()));
    }
    @SuppressWarnings("deprecation")
    public void submitCard(View view) {
        // TODO: replace with your own test key

        TabLayoutUtils.enableTabs(PagerMainActivity.tabLayout,false);
        transparent_layer.setVisibility(View.VISIBLE);
        progressDialog.setVisibility(View.VISIBLE);

        String cardNumberField = card_number_editText.getText().toString();
        String monthField = card_validity.getText().toString();
        if(!monthField.isEmpty()) {
            month = monthField.substring(0, 2);
        }
        String yearField = card_validity.getText().toString();
        if(!yearField.isEmpty()) {
            year = yearField.substring(3, 5);
        }
       String cvcField = cvv.getText().toString();

        card = new Card(
                cardNumberField.replace(" ",""),
                Integer.valueOf(month),
                Integer.valueOf(year),
                cvcField
        );

      /*  card.setCurrency("usd");
        card.setName("[NAME_SURNAME]");
        card.setAddressZip("[ZIP]");*/

        card.setNumber(cardNumberField.replace(" ",""));
        card.setExpMonth( Integer.valueOf(month));
        card.setExpYear(Integer.valueOf(year));
        card.setCVC(cvcField);


        stripe = new Stripe(getContext());
        stripe.createToken(card, "pk_test_cMgFVNBSNkkppTJinMtdQAvi", new TokenCallback() {
            public void onSuccess(Token token) {
                // TODO: Send Token information to your backend to initiate a charge
              //  Toast.makeText(getContext(), "Token created: " + token.getId(), Toast.LENGTH_LONG).show();
                tok = token;

                if(tok!=null){
                    addPaymentMethodVollyCal();
                }
               // new StripeCharge(token.getId()).execute();
            }

            public void onError(Exception error) {
                Log.d("Stripe", error.getLocalizedMessage());
            }
        });
    }


    public void addPaymentMethodVollyCal(){
        TabLayoutUtils.enableTabs(PagerMainActivity.tabLayout,false);
        transparent_layer.setVisibility(View.VISIBLE);
        progressDialog.setVisibility(View.VISIBLE);

      //  cardetailArraylist = new ArrayList<>();
        String user_id = sharedPreferences.getString(PreferenceClass.pre_user_id,"");
        String card_number = card_number_editText.getText().toString();

        //Creating a string request
        RequestQueue queue = Volley.newRequestQueue(getContext());

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("user_id", user_id);
            jsonObject.put("name",name_on_card.getText().toString());
            jsonObject.put("card",card_number.replace(" ",""));
            jsonObject.put("cvc",cvv.getText().toString());
            jsonObject.put("exp_month",month);
            jsonObject.put("exp_year",year);
            jsonObject.put("default","1");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST, Config.ADD_PAYMENT_METHOD, jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

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
                   //     Toast.makeText(getContext(),"Data Added Successfully",Toast.LENGTH_LONG).show();
                        Fragment restaurantMenuItemsFragment = new AddPaymentFragment();
                        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                        transaction.add(R.id.add_payment_detail_container, restaurantMenuItemsFragment,"parent").commit();

                    }

                    }
                    catch (JSONException e){
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                TabLayoutUtils.enableTabs(PagerMainActivity.tabLayout,true);
                transparent_layer.setVisibility(View.GONE);
                progressDialog.setVisibility(View.GONE);
                VolleyLog.d("JSONPost", "Error: " + error.getMessage());
               // Toast.makeText(getContext(), "Error: " +error.getMessage(), Toast.LENGTH_SHORT).show();
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

        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(
                35000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(jsonObjReq);




    }


    public static class ObjectSerializer {

        public static String serialize(Serializable obj) throws IOException {
            if (obj == null) return "";
            ByteArrayOutputStream serialObj = new ByteArrayOutputStream();
            ObjectOutputStream objStream = new ObjectOutputStream(serialObj);
            objStream.writeObject(obj);
            objStream.close();
            return encodeBytes(serialObj.toByteArray());
        }

        public static Object deserialize(String str) throws IOException, ClassNotFoundException {
            if (str == null || str.length() == 0) return null;
            ByteArrayInputStream serialObj = new ByteArrayInputStream(decodeBytes(str));
            ObjectInputStream objStream = new ObjectInputStream(serialObj);
            return objStream.readObject();
        }

        public static String encodeBytes(byte[] bytes) {
            StringBuffer strBuf = new StringBuffer();

            for (int i = 0; i < bytes.length; i++) {
                strBuf.append((char) (((bytes[i] >> 4) & 0xF) + ((int) 'a')));
                strBuf.append((char) (((bytes[i]) & 0xF) + ((int) 'a')));
            }

            return strBuf.toString();
        }

        public static byte[] decodeBytes(String str) {
            byte[] bytes = new byte[str.length() / 2];
            for (int i = 0; i < str.length(); i+=2) {
                char c = str.charAt(i);
                bytes[i/2] = (byte) ((c - 'a') << 4);
                c = str.charAt(i+1);
                bytes[i/2] += (c - 'a');
            }
            return bytes;
        }
    }

}
