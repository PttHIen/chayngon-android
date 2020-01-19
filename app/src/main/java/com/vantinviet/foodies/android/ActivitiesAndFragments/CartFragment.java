package com.vantinviet.foodies.android.ActivitiesAndFragments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import android.support.percent.PercentRelativeLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.vantinviet.foodies.android.Adapters.CartFragExpandable;
import com.vantinviet.foodies.android.Constants.AllConstants;
import com.vantinviet.foodies.android.Constants.Config;
import com.vantinviet.foodies.android.Constants.PreferenceClass;
import com.vantinviet.foodies.android.Models.CartFragChildModel;
import com.vantinviet.foodies.android.Models.CartFragParentModel;

import com.vantinviet.foodies.android.R;
import com.vantinviet.foodies.android.Utils.CustomExpandableListView;
import com.vantinviet.foodies.android.Utils.TabLayoutUtils;
import com.gmail.samehadar.iosdialog.CamomileSpinner;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import vn.momo.momo_partner.AppMoMoLib;
import vn.momo.momo_partner.MoMoParameterNamePayment;

import static com.vantinviet.foodies.android.ActivitiesAndFragments.AddressListFragment.CART_NOT_LOAD;
import static com.vantinviet.foodies.android.Constants.Config.GET_DISTANCE;

/**
 * Created by Nabeel on 2/12/2018.
 */

public class CartFragment extends Fragment {

    RelativeLayout accept_div,decline_div,cart_payment_method_div,cart_address_div,tip_div,promo_code_div,cart_check_out_div;
    TextView decline_tv,accept_tv,tax_tv,credit_card_number_tv,delivery_address_tv,rider_tip_price_tv,total_delivery_fee_tv,
            promo_tv,total_promo_tv,total_sum_tv,rider_tip,discount_tv,rest_name_tv,free_delivery_tv;
    CustomExpandableListView selected_item_list;
    SharedPreferences sPref;
    DatabaseReference mDatabase;
    FirebaseDatabase firebaseDatabase;
   private static String udid,tax_dues,payment_id,instructions,card_number,riderTip,tax_preference,fee_prefernce,total_sum,res_id,user_id,rest_name,mQuantity,rest_id,
            coupan_code_;
    String grandTotal_ = "0";

    CartFragExpandable cartFragExpandable;
    ArrayList<CartFragParentModel> listDataHeader;
    ArrayList<CartFragChildModel> listChildData;
    private ArrayList<ArrayList<CartFragChildModel>> ListChild;
    TextView sub_total_price_tv,total_tex_tv;
    String grandTotal,symbol,street,apartment,city,state,address_id, lattitude, longtitude;
    public static boolean CART_PAYMENT_METHOD,CART_ADDRESS,CART_LOGIN;
    CamomileSpinner cartProgress;
    Button clear_btn;
    @SuppressWarnings("deprecation")
    PercentRelativeLayout no_cart_div;
    Collection<Object> values;
    Map<String, Object> td;
    HashMap<String,Object> values_final;
    ArrayList<HashMap<String,Object>> extraItemArray;
    private boolean FLAG_COUPON;
    boolean getLoINSession,PICK_UP;
    Double previousRiderTip = 0.0;
    SwipeRefreshLayout refresh;
    private boolean isViewShown = false;
    LinearLayout mainCartDiv;
    JSONArray jsonArrayMenuExtraItem;
    SwipeRefreshLayout swipeRefresh;
    FrameLayout cart_main_container;
    public static boolean ORDER_PLACED,UPDATE_NODE;

    RelativeLayout transparent_layer,progressDialog;

    public static boolean FLAG_CLEAR_ORDER;
    String minimumOrderPrice;
    private static  String key,extraID,mDesc,mGrandTotal,mInstruction,mCurrency,mDesc_,mFee,mName,mPrice,mQuantity_,mTax,
            minimumOrderPrice_,required,restID;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        View view = inflater.inflate(R.layout.fragment_cart, container, false);
        if (!isViewShown) {
            initUI(view);
        }

        AppMoMoLib.getInstance().setEnvironment(AppMoMoLib.ENVIRONMENT.DEVELOPMENT); // AppMoMoLib.ENVIRONMENT.PRODUCTION
        return view;

    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (getView() != null) {
            isViewShown = true;
            // fetchdata() contains logic to show data when page is selected mostly asynctask to fill the data
            cart_main_container.invalidate();
            initUI(getView());
        } else {
            isViewShown = false;
        }

    }
    @SuppressWarnings("deprecation")
    public void initUI(View view){
        free_delivery_tv = view.findViewById(R.id.free_delivery_tv);
        progressDialog = view.findViewById(R.id.progressDialog);
        transparent_layer = view.findViewById(R.id.transparent_layer);
        progressDialog.setVisibility(View.GONE);
        delivery_address_tv = view.findViewById(R.id.delivery_address_tv);
        cartProgress = view.findViewById(R.id.cartProgress);
        cartProgress.start();
        cart_main_container = view.findViewById(R.id.cart_main_container);
        cart_main_container.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });


        cart_main_container.invalidate();
        delivery_address_tv.setText("Chọn địa chỉ giao hàng");
        credit_card_number_tv = view.findViewById(R.id.credit_card_number_tv);
        credit_card_number_tv.setText("Chọn phương thức thanh toán");
        sPref = getContext().getSharedPreferences(PreferenceClass.user, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sPref.edit();
        udid = sPref.getString(PreferenceClass.UDID,"");
        //grandTotal = sPref.getString(PreferenceClass.GRAND_TOTAL,"");
        //  symbol = sPref.getString("symbol","");
        getLoINSession = sPref.getBoolean(PreferenceClass.IS_LOGIN,false);
        extraItemArray = new ArrayList<>();

        // res_id = sPref.getString(PreferenceClass.RESTAURANT_ID,"");
        user_id = sPref.getString(PreferenceClass.pre_user_id,"");

        address_id = sPref.getString(PreferenceClass.ADDRESS_ID,"");
        payment_id = sPref.getString(PreferenceClass.PAYMENT_ID,"");
        rest_name = sPref.getString(PreferenceClass.RESTAURANT_NAME,"");
        rest_id = sPref.getString(PreferenceClass.RESTAURANT_ID,"");


        firebaseDatabase = FirebaseDatabase.getInstance();
        mDatabase = firebaseDatabase.getReference().child(AllConstants.CALCULATION).child(udid);
        //  mDatabase.keepSynced(true);
        riderTip = "0";
        // tax_preference = sPref.getString(PreferenceClass.RESTAURANT_ITEM_TAX,"");


        no_cart_div = view.findViewById(R.id.no_cart_div);
        mainCartDiv = view.findViewById(R.id.mainCartDiv);
        promo_tv = view.findViewById(R.id.promo_tv);
        total_promo_tv = view.findViewById(R.id.total_promo_tv);

           // mDatabase.keepSynced(true);


        no_cart_div.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });

        cart_check_out_div = view.findViewById(R.id.cart_check_out_div);

        clear_btn = view.findViewById(R.id.clear_btn);
        rest_name_tv = view.findViewById(R.id.rest_name_tv);

        discount_tv = view.findViewById(R.id.discount_tv);
        promo_code_div = view.findViewById(R.id.promo_code_div);
        rider_tip = view.findViewById(R.id.rider_tip);
        total_sum_tv = view.findViewById(R.id.total_sum_tv);
       // total_deal_order_tv = view.findViewById(R.id.total_deal_order_tv);
        total_delivery_fee_tv= view.findViewById(R.id.total_delivery_fee_tv);
        rider_tip_price_tv = view.findViewById(R.id.rider_tip_price_tv);

        if(rider_tip.getText().toString().equalsIgnoreCase("0 "+symbol)){
            rider_tip.setText("Thêm tiền thưởng cho shipper");
        }
        tip_div = view.findViewById(R.id.tip_div);

        total_tex_tv = view.findViewById(R.id.total_tex_tv);
        tax_tv = view.findViewById(R.id.tax_tv);

        rest_name_tv.setText(rest_name);

        sub_total_price_tv = view.findViewById(R.id.sub_total_price_tv);
        decline_div = view.findViewById(R.id.decline_div);
        accept_div = view.findViewById(R.id.accept_div);
        decline_tv = view.findViewById(R.id.decline_tv);
        accept_tv = view.findViewById(R.id.accept_tv);
        selected_item_list = view.findViewById(R.id.selected_item_list);




        cart_payment_method_div = view.findViewById(R.id.cart_payment_method_div);
        cart_address_div = view.findViewById(R.id.cart_address_div);

        cart_check_out_div.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(delivery_address_tv.getText().toString().equalsIgnoreCase("Chọn địa chỉ giao hàng")
                        || credit_card_number_tv.getText().toString().equalsIgnoreCase("Chọn phương thức thanh toán"))
                {
                    Toast.makeText(getContext(),"Địa chỉ giao hàng HOẶC Phương thức thanh toán bị bỏ lỡ",Toast.LENGTH_LONG).show();
                }else {
                    placeOrder();
                }
            }
        });

        clear_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showDialogCartDelete();



            }
        });

            if (AddressListFragment.FLAG_ADDRESS_LIST) {
                if(AddressListFragment.FLAG_NO_ADRESS_CHOSE){
                    AddressListFragment.FLAG_NO_ADRESS_CHOSE = false;
                    delivery_address_tv.setText("Chọn địa chỉ giao hàng");

                }
                else {
                    street = sPref.getString(PreferenceClass.STREET, "");
                    city = sPref.getString(PreferenceClass.CITY, "");
                    lattitude = sPref.getString(PreferenceClass.LAT_SHIP, "");
                    longtitude = sPref.getString(PreferenceClass.LONG_SHIP, "");
                    state = sPref.getString(PreferenceClass.STATE, "");
                    apartment = sPref.getString(PreferenceClass.APARTMENT, "");
                    AddressListFragment.FLAG_ADDRESS_LIST = false;
                    AddPaymentFragment.FLAG_ADD_PAYMENT = true;
                    delivery_address_tv.setText(street + " " + city + " " + state);
                    RequestQueue queue = Volley.newRequestQueue(getContext());
                    JSONObject addressJsonObject = new JSONObject();
                    try {
                            addressJsonObject.put("long", longtitude);
                            addressJsonObject.put("lat", lattitude);
                            addressJsonObject.put("res_id", rest_id);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Log.d("jsonObject login",addressJsonObject.toString());

                    JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                            GET_DISTANCE,addressJsonObject,
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
                                            JSONObject json = new JSONObject(jsonResponse.toString());
                                            String resultObj = json.getString("msg");
                                            fee_prefernce = resultObj;
                                            String prices = String.valueOf( Math.round(Double.parseDouble(fee_prefernce) * 5000));
                                            Log.d("khoang cach", prices);
                                        editor.putString(PreferenceClass.RESTAURANT_DISTANCE, prices);
                                        editor.commit();
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    getCartData();
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


            }

        if (AddPaymentFragment.FLAG_ADD_PAYMENT) {
            card_number = sPref.getString(PreferenceClass.CREDIT_CARD_ARRAY, "");
            if (AddPaymentFragment.FLAG_CASH_ON_DELIVERY) {
                credit_card_number_tv.setText("Thanh toán khi giao hàng");
                credit_card_number_tv.setTextColor(getResources().getColor(R.color.black));
               // AddPaymentFragment.FLAG_CASH_ON_DELIVERY = false;
              // AddPaymentFragment.FLAG_PAYMENT_METHOD = false;
            }else if(AddPaymentFragment.FLAG_PAYMENT_BY_MOMO = true) {
                credit_card_number_tv.setText(getResources().getString(R.string.payment_by_momo));
                credit_card_number_tv.setTextColor(getResources().getColor(R.color.black));

            } else if(AddPaymentFragment.FLAG_PAYMENT_METHOD = true) {

                if(card_number.isEmpty()){
                    credit_card_number_tv.setText("Thanh toán khi giao hàng");
                 //   AddPaymentFragment.FLAG_PAYMENT_METHOD = false;
                }
                else {
                    credit_card_number_tv.setText("**** **** **** " + card_number);
                 //   AddPaymentFragment.FLAG_PAYMENT_METHOD = false;
                }
                credit_card_number_tv.setTextColor(getResources().getColor(R.color.black));

            }
            AddressListFragment.FLAG_ADDRESS_LIST = true;
            AddPaymentFragment.FLAG_ADD_PAYMENT = false;
        }


        tip_div.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addRiderTip();
            }
        });

        promo_code_div.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                varifyCoupan();
            }
        });

        cart_address_div.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!getLoINSession){
                    Fragment restaurantMenuItemsFragment = new UserAccountFragment();
                    FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                    transaction.add(R.id.cart_main_container, restaurantMenuItemsFragment,"parent").commit();
                    CART_ADDRESS = true;
                    CART_LOGIN = true;
                }
                else {
                    CART_ADDRESS = true;
                    Fragment restaurantMenuItemsFragment = new AddressListFragment();
                    FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                    transaction.add(R.id.cart_main_container, restaurantMenuItemsFragment, "parent").commit();
                    SharedPreferences.Editor editor = sPref.edit();
                    editor.putString("grandTotal",grandTotal);
                    editor.putString(PreferenceClass.RESTAURANT_ID,res_id).apply();
                }

            }
        });

        cart_payment_method_div.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!getLoINSession){
                    Fragment restaurantMenuItemsFragment = new UserAccountFragment();
                    FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                    transaction.add(R.id.cart_main_container, restaurantMenuItemsFragment,"parent").commit();
                    CART_PAYMENT_METHOD = true;
                    CART_LOGIN = true;
                }
                else {
                    CART_PAYMENT_METHOD = true;
                    Fragment restaurantMenuItemsFragment = new AddPaymentFragment();
                    FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                    transaction.add(R.id.cart_main_container, restaurantMenuItemsFragment, "parent").commit();
                }
            }
        });





        selected_item_list .setExpanded(true);
        selected_item_list.setGroupIndicator(null);

        //  cartExpandableListView.setChoiceMode(ExpandableListView.CHOICE_MODE_SINGLE);

        selected_item_list.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v,
                                        int groupPosition, long id) {
                return true; // This way the expander cannot be collapsed
            }
        });


        decline_div.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                decline_div.setBackground(getResources().getDrawable(R.drawable.round_shape_btn_login));
                accept_div.setBackground(getResources().getDrawable(R.drawable.round_shape_btn_grey));
                decline_tv.setTextColor(getResources().getColor(R.color.colorWhite));
                accept_tv.setTextColor(getResources().getColor(R.color.or_color_name));
                rider_tip_price_tv.setText("0 " + symbol);
                total_delivery_fee_tv.setText("0 " + symbol);
                rider_tip.setText("0 " + symbol);
                delivery_address_tv.setText("Mua tại quầy");
                PICK_UP = true;
                getTotalSumDeliveryFee(fee_prefernce,PICK_UP);
                getTotalSumTip(riderTip,PICK_UP);

                cart_address_div.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        return true;
                    }
                });

                tip_div.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        return true;
                    }
                });

            }
        });

        accept_div.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                decline_div.setBackground(getResources().getDrawable(R.drawable.round_shape_btn_grey));
                accept_div.setBackground(getResources().getDrawable(R.drawable.round_shape_btn_login));
                decline_tv.setTextColor(getResources().getColor(R.color.or_color_name));
                accept_tv.setTextColor(getResources().getColor(R.color.colorWhite));
                rider_tip_price_tv.setText(riderTip + symbol);
                total_delivery_fee_tv.setText( fee_prefernce +" "+ symbol);
                rider_tip.setText( riderTip + symbol);
                if(street == null && apartment == null && city == null && state == null){
                    delivery_address_tv.setText("Chọn địa chỉ giao hàng");
                }
                else {
                    delivery_address_tv.setText(street + " " + apartment + " " + city + " " + state);
                }
                PICK_UP = false;

                previousRiderTip = 0.0;
                getTotalSumDeliveryFee(fee_prefernce,PICK_UP);
                getTotalSumTip(riderTip,PICK_UP);

                cart_address_div.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        return false;
                    }
                });

                tip_div.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        return false;
                    }
                });
            }
        });

    }

    public void showDialogCartDelete(){

        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(getContext(), android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(getContext());
        }
        builder.setTitle("Xóa giỏ hàng?")
                .setMessage("Bạn có chắc chắn muốn xóa giỏ hàng?")
                .setPositiveButton("Đồng ý", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                        mDatabase.setValue(null);
                        no_cart_div.setVisibility(View.VISIBLE);
                        mainCartDiv.setVisibility(View.GONE);
                        SharedPreferences.Editor editor = sPref.edit();
                        editor.putString(PreferenceClass.ADDRESS_DELIVERY_FEE,"0");
                        editor.putInt(PreferenceClass.CART_COUNT,0);
                        editor.putInt("count",0).commit();
                        Intent intent = new Intent();
                        intent.setAction("AddToCart");
                        getContext().sendBroadcast(intent);

                        rider_tip.setText("Thêm tiền thưởng");
                        discount_tv.setText("Thêm mã khuyến mãi");
                        riderTip = "0";
                        previousRiderTip=Double.parseDouble("0.0");

                        FLAG_CLEAR_ORDER = true;

                        dialog.dismiss();
                    }


                })
                .setNegativeButton("Hủy bỏ", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing

                        dialog.dismiss();
                    }
                })
                .show();

    }

    @SuppressWarnings("unchecked")
    public void getCartData(){
        mDatabase.keepSynced(true);
        TabLayoutUtils.enableTabs(PagerMainActivity.tabLayout,false);
        transparent_layer.setVisibility(View.VISIBLE);
        progressDialog.setVisibility(View.VISIBLE);
        listDataHeader = new ArrayList<>();
        ListChild = new ArrayList<>();
        DatabaseReference query = mDatabase;
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //child is each element in the finished list
                 td = (HashMap<String, Object>) dataSnapshot.getValue();
              if(td!=null) {

                    values = td.values();
                    String string = values.toString();

                    JSONArray jsonArray = null;
                    try {
                        jsonArray = new JSONArray(values);
                        grandTotal_ = "0";
                        for (int a = 0; a < jsonArray.length(); a++) {

                            JSONObject allJsonObject = jsonArray.getJSONObject(a);

                            Log.d("allJsonObject", allJsonObject.toString());
                            CartFragParentModel cartFragParentModel = new CartFragParentModel();

                            cartFragParentModel.setItem_name(allJsonObject.optString("mName"));
                            cartFragParentModel.setItem_price(allJsonObject.optString("mPrice"));
                            mQuantity = allJsonObject.optString("mQuantity");
                            cartFragParentModel.setItem_quantity(allJsonObject.optString("mQuantity"));
                            cartFragParentModel.setItem_symbol(allJsonObject.optString("mCurrency"));
                            cartFragParentModel.setItem_key(allJsonObject.optString("key"));
                        //    cartFragParentModel.setItem_fee(allJsonObject.optString("mFee"));



                            String total = allJsonObject.optString("grandTotal");
                            int minshipfree =  Integer.parseInt(allJsonObject.optString("minimumOrderPrice").trim()==""?allJsonObject.optString("minimumOrderPrice"):"0")*1000;
                            minimumOrderPrice = String.valueOf(minshipfree) ;
                            symbol = allJsonObject.optString("mCurrency");

                            res_id = allJsonObject.optString("restID");

                            if (total.isEmpty() || total.equalsIgnoreCase("null")) {

                                total = "0";
                            }

                            getDescText(minimumOrderPrice,total);

                            grandTotal = String.valueOf(Double.parseDouble(total) + Double.parseDouble(grandTotal_));

                            grandTotal_ = grandTotal;

                            tax_preference = allJsonObject.optString("mTax");
                            instructions = allJsonObject.optString("instruction");


                            listDataHeader.add(cartFragParentModel);
                            listChildData = new ArrayList<>();

                            if (!allJsonObject.has("extraItem")) {
                                TabLayoutUtils.enableTabs(PagerMainActivity.tabLayout,true);
                                transparent_layer.setVisibility(View.GONE);
                                progressDialog.setVisibility(View.GONE);
                                ListChild.add(listChildData);
                            } else {
                                JSONArray extraItemArray = allJsonObject.getJSONArray("extraItem");

                                for (int b = 0; b < extraItemArray.length(); b++) {

                                    JSONObject jsonObject = extraItemArray.getJSONObject(b);

                                    CartFragChildModel cartFragChildModel = new CartFragChildModel();

                                    cartFragChildModel.setQuantity(allJsonObject.optString("mQuantity"));
                                    cartFragChildModel.setSymbol(allJsonObject.optString("mCurrency"));
                                    cartFragChildModel.setName(jsonObject.optString("menu_extra_item_name"));
                                    cartFragChildModel.setPrice(jsonObject.optString("menu_extra_item_price"));
                                    cartFragChildModel.setPrice(jsonObject.optString("menu_extra_item_price"));

                                    listChildData.add(cartFragChildModel);
                                }
                                ListChild.add(listChildData);

                            }
                        }
                            if(listDataHeader!=null&&listDataHeader.size()>0){

                                (getView().findViewById(R.id.no_cart_div)).setVisibility(View.GONE);
                             //  (getView().findViewById(R.id.no_cart_div)).invalidate();
                               (getView().findViewById(R.id.mainCartDiv)).setVisibility(View.VISIBLE);

                                sub_total_price_tv.setText(grandTotal+ symbol);

                                if(!tax_preference.isEmpty()) {
                                    tax_tv.setText("("+tax_preference+"%)");
                                }
                                else {
                                    tax_preference = String.valueOf(0);
                                    tax_tv.setText("(0%)");
                                }


                               String mfee = fee_prefernce = sPref.getString(PreferenceClass.RESTAURANT_DISTANCE,"");
                                if(fee_prefernce!=null) {
                                    if (fee_prefernce.isEmpty()) {
                                        Log.d("", fee_prefernce);
                                        fee_prefernce = mfee;
                                        Log.d("fee_prefernce2", fee_prefernce);
                                    }
                                }
                                if (fee_prefernce==null){
                                    fee_prefernce="0";
                                }



                                if (grandTotal.isEmpty()){
                                    grandTotal="0.0";
                                }
                                if(tax_dues!=null) {
                                    if (tax_dues.isEmpty()) {
                                        tax_dues = String.valueOf(Double.parseDouble(grandTotal)*Double.parseDouble(tax_preference)/100);
                                    }
                                }
                                else {
                                    tax_dues = String.valueOf(0);
                                    tax_tv.setText("(0%)");
                                }
                                if(delivery_address_tv.getText().toString().equalsIgnoreCase("Chọn địa chỉ giao hàng")){
                                    fee_prefernce = ""+0.0;
                                }
                                total_delivery_fee_tv.setText(fee_prefernce +" "+ symbol);
                                // Getting Total Sum
                                total_sum = String.valueOf(Double.valueOf(Double.parseDouble(grandTotal)+Double.parseDouble(tax_dues)+Double.parseDouble(fee_prefernce.replace(",", ""))));

                                //getTotalSumTip("0",PICK_UP);

                                rider_tip_price_tv.setText("0.0 " + symbol);

                                total_promo_tv.setText("0.0 " + symbol);
                                total_sum_tv.setText(total_sum);


                                cartFragExpandable = new CartFragExpandable(getContext(), listDataHeader, ListChild);
                                selected_item_list.setAdapter(cartFragExpandable);
                                TabLayoutUtils.enableTabs(PagerMainActivity.tabLayout,true);
                                transparent_layer.setVisibility(View.GONE);
                                progressDialog.setVisibility(View.GONE);
                                int itemCount = cartFragExpandable.getGroupCount();


                                for(int i=0; i < cartFragExpandable.getGroupCount(); i++)
                                    try {

                                        selected_item_list.expandGroup(i);
                                    }
                                    catch (IndexOutOfBoundsException e){
                                        e.getCause();
                                    }

                                selected_item_list.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
                                    @Override
                                    public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {

                                        CartFragParentModel item = (CartFragParentModel) listDataHeader.get(groupPosition);

                                        key = item.getItem_key();

                                        customDialogbox();

                                        return true;
                                    }
                                });

                            }
                            else {
                                (getView().findViewById(R.id.no_cart_div)).setVisibility(View.VISIBLE);
                               // (getView().findViewById(R.id.no_cart_div)).invalidate();
                                (getView().findViewById(R.id.mainCartDiv)).setVisibility(View.GONE);
                            }




                    } catch (JSONException e) {
                        e.printStackTrace();
                        TabLayoutUtils.enableTabs(PagerMainActivity.tabLayout,true);
                        transparent_layer.setVisibility(View.GONE);
                        progressDialog.setVisibility(View.GONE);
                        (getView().findViewById(R.id.no_cart_div)).setVisibility(View.VISIBLE);
                        (getView().findViewById(R.id.mainCartDiv)).setVisibility(View.GONE);
                    }
                }
                else {
                  TabLayoutUtils.enableTabs(PagerMainActivity.tabLayout,true);
                  transparent_layer.setVisibility(View.GONE);
                  progressDialog.setVisibility(View.GONE);
                  (getView().findViewById(R.id.no_cart_div)).setVisibility(View.VISIBLE);
                  (getView().findViewById(R.id.mainCartDiv)).setVisibility(View.GONE);
                 // (getView().findViewById(R.id.no_cart_div)).invalidate();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                TabLayoutUtils.enableTabs(PagerMainActivity.tabLayout,true);
                transparent_layer.setVisibility(View.GONE);
                progressDialog.setVisibility(View.GONE);
                (getView().findViewById(R.id.no_cart_div)).setVisibility(View.VISIBLE);
                (getView().findViewById(R.id.mainCartDiv)).setVisibility(View.GONE);

            }
        });

    }



    private void getDescText(String minimumOrderPrice,String grandTotal){

        Double var3 = Double.parseDouble(minimumOrderPrice)-Double.parseDouble(grandTotal);
        Log.d("var3", var3.toString());
        if(var3 >= Double.parseDouble(minimumOrderPrice)){

            free_delivery_tv.setText("Bạn đã đạt được thứ tự giao hàng miễn phí.");


        }
        else {
            if(String.valueOf(var3).contains("-")){
                free_delivery_tv.setText("Bạn đã đạt được giao hàng miễn phí.");
            }
            else {
                free_delivery_tv.setText("Bạn phải cần thêm "+ var3 + " " + symbol +" để được giao hàng miễn phí.");
            }
        }


    }


    public void addRiderTip(){

        // custom dialog
        final Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.custom_dialog_cart);

        final EditText ed_text = dialog.findViewById(R.id.ed_text);
        ed_text.setInputType(InputType.TYPE_CLASS_NUMBER);
        TextView title = dialog.findViewById(R.id.title);
        title.setText("Thêm tiền thưởng");
        ed_text.setHint("Nhập số tiền thưởng");
        // set the custom dialog components - text, image and button

        Button cancelDiv = (Button) dialog.findViewById(R.id.cancel_btn);
        Button done_btn =  (Button) dialog.findViewById(R.id.done_btn);

        done_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                riderTip = ed_text.getText().toString();
                PICK_UP = false;
                getTotalSumTip(riderTip,PICK_UP);
                rider_tip_price_tv.setText(riderTip+symbol);
                rider_tip.setText(riderTip+symbol);
                dialog.dismiss();
            }
        });


        // if button is clicked, close the custom dialog
        cancelDiv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();

    }

    public void varifyCoupan(){

        // custom dialog
        final Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.custom_dialog_cart);


       final EditText ed_text = dialog.findViewById(R.id.ed_text);

        // set the custom dialog components - text, image and button

        Button cancelDiv = (Button) dialog.findViewById(R.id.cancel_btn);
        Button done_btn = (Button) dialog.findViewById(R.id.done_btn);

        done_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                coupan_code_ = ed_text.getText().toString();
                getCoupanRequest(coupan_code_);

                TabLayoutUtils.enableTabs(PagerMainActivity.tabLayout,false);
                transparent_layer.setVisibility(View.VISIBLE);
                progressDialog.setVisibility(View.VISIBLE);
                dialog.dismiss();
            }
        });


        // if button is clicked, close the custom dialog
        cancelDiv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();

    }

    public void getCoupanRequest(String coupan_code){

        RequestQueue queue = Volley.newRequestQueue(getContext());

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("coupon_code",coupan_code);
            jsonObject.put("restaurant_id",res_id);
            jsonObject.put("user_id",user_id);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Config.VERIFY_COUPAN, jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

            String string = response.toString();

                try {
                    JSONObject jsonObject1 = new JSONObject(string);

                    int code = Integer.parseInt(jsonObject1.optString("code"));
                    if(FLAG_COUPON){
                        Toast.makeText(getContext(),"Phiếu giảm giá đã được thêm",Toast.LENGTH_SHORT).show();
                        TabLayoutUtils.enableTabs(PagerMainActivity.tabLayout,true);
                        transparent_layer.setVisibility(View.GONE);
                        progressDialog.setVisibility(View.GONE);
                    }
                    else {
                        if (code == 200) {
                            FLAG_COUPON = true;
                            JSONArray jsonArray = jsonObject1.getJSONArray("msg");
                            for (int i = 0; i < jsonArray.length(); i++) {

                                JSONObject jsonObject2 = jsonArray.getJSONObject(i);

                                JSONObject jsonObject3 = jsonObject2.getJSONObject("RestaurantCoupon");
                                String discount = jsonObject3.optString("discount");

                                // riderTip = edittext.getText().toString();


                                promo_tv.setText("("+discount+"%)");
                                getTotalSumCoupon(discount,symbol);
                              //  discount_tv.setText(symbol + discount);
                                TabLayoutUtils.enableTabs(PagerMainActivity.tabLayout,true);
                                transparent_layer.setVisibility(View.GONE);
                                progressDialog.setVisibility(View.GONE);
                              //  no_cart_div.setVisibility(View.VISIBLE);
                               // mainCartDiv.setVisibility(View.GONE);
                               // mDatabase.keepSynced(true);
                            }

                            //rider_tip.setText(symbol+riderTip);
                        } else {
                            Toast.makeText(getContext(), response.toString(), Toast.LENGTH_SHORT).show();
                            TabLayoutUtils.enableTabs(PagerMainActivity.tabLayout,true);
                            transparent_layer.setVisibility(View.GONE);
                            progressDialog.setVisibility(View.GONE);
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    TabLayoutUtils.enableTabs(PagerMainActivity.tabLayout,true);
                    transparent_layer.setVisibility(View.GONE);
                    progressDialog.setVisibility(View.GONE);

                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                TabLayoutUtils.enableTabs(PagerMainActivity.tabLayout,true);
                transparent_layer.setVisibility(View.GONE);
                progressDialog.setVisibility(View.GONE);

                Log.d("Volly Error", error.toString());
               // Toast.makeText(getContext(),error.toString(),Toast.LENGTH_SHORT).show();

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


    public void getTotalSumDeliveryFee(String deliveryFee,boolean pick_up){

        if(pick_up){
            total_sum = String.valueOf(Double.parseDouble(total_sum)-Double.parseDouble(deliveryFee));
        }
        else {
            total_sum = String.valueOf(Double.parseDouble(total_sum) + Double.parseDouble(deliveryFee));
        }
        total_sum_tv.setText(new DecimalFormat("##.##").format(Double.parseDouble(total_sum)) + " "+ symbol);
    }
    public void getTotalSumTip(String riderTip,boolean rider_tip_pick_up){
        if(rider_tip_pick_up){
            total_sum = String.valueOf(Double.parseDouble(total_sum)-Double.parseDouble(riderTip));
        }
        else {

            total_sum = String.valueOf(Double.parseDouble(total_sum) + Double.parseDouble(riderTip));
            total_sum = String.valueOf(Double.parseDouble(total_sum)-previousRiderTip);
            previousRiderTip = Double.parseDouble(riderTip);

        }
        total_sum_tv.setText(new DecimalFormat("##.##").format(Double.parseDouble(total_sum))+symbol);

    }

    public void getTotalSumCoupon(String discount,String symbol){

        Double total_discount = Double.valueOf(new DecimalFormat("##.##").format(Double.parseDouble(discount)/100*Double.parseDouble(grandTotal_)));

        discount_tv.setText(+total_discount+" ("+discount+"%)" + " " + symbol);
        total_promo_tv.setText(total_discount +" "+ symbol);

        total_sum = String.valueOf(Double.parseDouble(total_sum)-total_discount);

        total_sum_tv.setText(new DecimalFormat("##.##").format(Double.parseDouble(total_sum)) + symbol);

    }

    public void placeOrder(){
        TabLayoutUtils.enableTabs(PagerMainActivity.tabLayout,false);
        transparent_layer.setVisibility(View.VISIBLE);
        progressDialog.setVisibility(View.VISIBLE);
        JSONArray menu_item=null;
        JSONArray valueArray = new JSONArray(values);
        for (int i=0;i<valueArray.length();i++){

            JSONObject jsonObject1 = null;
            try {
                jsonObject1 = valueArray.getJSONObject(i);
                values_final= new HashMap<>();

                if(jsonObject1.optString("extraItem")!=null&& !jsonObject1.optString("extraItem").isEmpty()) {
                    jsonArrayMenuExtraItem = new JSONArray(jsonObject1.optString("extraItem"));
                    values_final.put("menu_extra_item",jsonArrayMenuExtraItem);
                    String size = String.valueOf(jsonArrayMenuExtraItem.length());
                }
                else {
                    values_final.put("menu_extra_item",new JSONArray("["+"]"));
                }


                    values_final.put("menu_item_price", jsonObject1.optString("mPrice"));
                    values_final.put("menu_item_quantity", jsonObject1.optString("mQuantity"));
                    values_final.put("menu_item_name", jsonObject1.optString("mName"));


                     extraItemArray.add(values_final);

            } catch (JSONException e) {
                e.printStackTrace();
                TabLayoutUtils.enableTabs(PagerMainActivity.tabLayout,true);
                transparent_layer.setVisibility(View.GONE);
                progressDialog.setVisibility(View.GONE);

            }

        }

        //JSONObject obj=new JSONObject(values_final);
        menu_item =new JSONArray(extraItemArray);

        Calendar c = Calendar.getInstance();
        System.out.println("Current time => "+c.getTime());

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = df.format(c.getTime());




        final JSONObject jsonObject = new JSONObject();
        try {

            jsonObject.put("user_id",user_id);
            jsonObject.put("price",total_sum);
            jsonObject.put("sub_total",grandTotal);
            jsonObject.put("tax",tax_dues);
            jsonObject.put("quantity",mQuantity);
            if(delivery_address_tv.getText().toString().equalsIgnoreCase("Mua tại quầy"))
            {
                jsonObject.put("address_id", "");
            }else {
                jsonObject.put("address_id", address_id);
            }
            jsonObject.put("restaurant_id",res_id);
            jsonObject.put("instructions",instructions);
            jsonObject.put("coupon_id","0");
            jsonObject.put("order_time",formattedDate);
            jsonObject.put("delivery_fee",fee_prefernce);
            jsonObject.put("version",SplashScreen.VERSION_CODE);

            if(delivery_address_tv.getText().toString().equalsIgnoreCase("Mua tại quầy"))
            {
                jsonObject.put("delivery","0");
            }
            else {
                jsonObject.put("delivery","1");
            }

            if(rider_tip.getText().toString().equalsIgnoreCase(getResources().getString(R.string.add_rider_tip))){
                jsonObject.put("rider_tip","0");
            }
            else {
                String riderTip_ = riderTip;
                jsonObject.put("rider_tip",riderTip_ );
            }

            jsonObject.put("device","android");


            if(credit_card_number_tv.getText().toString().equalsIgnoreCase(getResources().getString(R.string.cash_on_delivery))){
                jsonObject.put("cod","1");
                jsonObject.put("payment_id","2");
                jsonObject.put("payment_method_id","0");
            }else if(credit_card_number_tv.getText().toString().equalsIgnoreCase(getResources().getString(R.string.payment_by_momo))){
                jsonObject.put("cod","0");
                jsonObject.put("payment_id","1");
                jsonObject.put("payment_method_id","");
            }
            else {
                jsonObject.put("cod","0");
                jsonObject.put("payment_id",3);
                jsonObject.put("payment_method_id","");
            }

            jsonObject.put("menu_item",menu_item);
            String str = menu_item.toString();



        } catch (JSONException e) {
            e.printStackTrace();
            TabLayoutUtils.enableTabs(PagerMainActivity.tabLayout,true);
            transparent_layer.setVisibility(View.GONE);
            progressDialog.setVisibility(View.GONE);

        }

        Log.d("ORDER_JSON_POST",jsonObject.toString());
        RequestQueue queue = Volley.newRequestQueue(getContext());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Config.PLACE_ORDER, jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                String str = response.toString();
                try {
                    JSONObject jsonObject1 = new JSONObject(str);
                    int code = Integer.parseInt(jsonObject1.optString("code"));
                    String data_oder_detail = jsonObject1.optString("msg");
                    if(code==401){
                        Toast.makeText(getContext(),str,Toast.LENGTH_SHORT).show();
                    }

                    else if (code==200) {
                        if(credit_card_number_tv.getText().toString().equalsIgnoreCase(getResources().getString(R.string.payment_by_momo))){
                            Intent intent = new Intent(getContext(),Momo.class);
                            TabLayoutUtils.enableTabs(PagerMainActivity.tabLayout, true);
                            intent.putExtra("data_order_detail",data_oder_detail );
                            transparent_layer.setVisibility(View.GONE);
                            progressDialog.setVisibility(View.GONE);
                            Toast.makeText(getContext(), "Lưu đơn hàng thành công chúng tôi đang tiếp tục sử lý thanh toán với tài khoản momo của bạn", Toast.LENGTH_LONG).show();
                            mDatabase.setValue(null);
                            // PagerMainActivity.viewPager.setCurrentItem(1, true);
                            SharedPreferences.Editor editor = sPref.edit();
                            editor.putInt(PreferenceClass.CART_COUNT, 0);
                            editor.putInt("count", 0).commit();
                            FLAG_CLEAR_ORDER = true;
                            startActivity(intent);
                        }else {
                            TabLayoutUtils.enableTabs(PagerMainActivity.tabLayout, true);
                            transparent_layer.setVisibility(View.GONE);
                            progressDialog.setVisibility(View.GONE);
                            Toast.makeText(getContext(), "Đặt hàng thành công", Toast.LENGTH_LONG).show();
                            mDatabase.setValue(null);
                            Intent intent = new Intent();
                            intent.setAction("AddToCart");
                            getContext().sendBroadcast(intent);

                            // PagerMainActivity.viewPager.setCurrentItem(1, true);
                            SharedPreferences.Editor editor = sPref.edit();
                            editor.putString(PreferenceClass.ADDRESS_DELIVERY_FEE, "0");
                            editor.putInt(PreferenceClass.CART_COUNT, 0);
                            editor.putInt("count", 0).commit();
                            ORDER_PLACED = true;

                            FLAG_CLEAR_ORDER = true;
                            OrderDetailFragment.CALLBACK_ORDERFRAG = true;

                            getCartData();

                            startActivity(new Intent(getContext(), MainActivity.class));
                            getActivity().finish();
                        }

                    }

                    else {
                        TabLayoutUtils.enableTabs(PagerMainActivity.tabLayout,true);
                        transparent_layer.setVisibility(View.GONE);
                        progressDialog.setVisibility(View.GONE);
                        Toast.makeText(getContext(),"Địa chỉ bạn đã chọn không khớp với thành phố của bạn.",Toast.LENGTH_SHORT).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    TabLayoutUtils.enableTabs(PagerMainActivity.tabLayout,true);
                    transparent_layer.setVisibility(View.GONE);
                    progressDialog.setVisibility(View.GONE);


                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("ERROR_PLACE_ORDER_API %s",error.toString());
                TabLayoutUtils.enableTabs(PagerMainActivity.tabLayout,true);
                transparent_layer.setVisibility(View.GONE);
                progressDialog.setVisibility(View.GONE);
                Toast.makeText(getContext(),error.toString(),Toast.LENGTH_SHORT).show();

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

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(jsonObjectRequest);
    }

    private String amount = "10000";
    private String fee = "0";
    int environment = 0;//developer default
    private String merchantName = "Demo SDK";
    private String merchantCode = "SCB01";
    private String merchantNameLabel = "Nhà cung cấp";
    private String description = "Thanh toán dịch vụ ABC";

    private void requestPaymentMomo() {

        AppMoMoLib.getInstance().setAction(AppMoMoLib.ACTION.PAYMENT);
        AppMoMoLib.getInstance().setActionType(AppMoMoLib.ACTION_TYPE.GET_TOKEN);
        amount="1";

        Map<String, Object> eventValue = new HashMap<>();
        //client Required
        eventValue.put(MoMoParameterNamePayment.MERCHANT_NAME, merchantName);
        eventValue.put(MoMoParameterNamePayment.MERCHANT_CODE, merchantCode);
        eventValue.put(MoMoParameterNamePayment.AMOUNT, amount);
        eventValue.put(MoMoParameterNamePayment.DESCRIPTION, description);
        //client Optional
        //eventValue.put(MoMoParameterNamePayment.MERCHANT_BILL_ID, "merchant_billId_");
        eventValue.put(MoMoParameterNamePayment.FEE, fee);
        eventValue.put(MoMoParameterNamePayment.MERCHANT_NAME_LABEL, merchantNameLabel);

        //client call webview
        eventValue.put(MoMoParameterNamePayment.REQUEST_ID,  merchantCode+"merchant_billId_"+System.currentTimeMillis());
        eventValue.put(MoMoParameterNamePayment.PARTNER_CODE, merchantCode);

        JSONObject objExtraData = new JSONObject();
        try {
            objExtraData.put("site_code", "008");
            objExtraData.put("site_name", "CGV Cresent Mall");
            objExtraData.put("screen_code", 0);
            objExtraData.put("screen_name", "Special");
            objExtraData.put("movie_name", "Kẻ Trộm Mặt Trăng 3");
            objExtraData.put("movie_format", "2D");
            objExtraData.put("ticket", "{\"ticket\":{\"01\":{\"type\":\"std\",\"price\":110000,\"qty\":3}}}");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        eventValue.put(MoMoParameterNamePayment.EXTRA_DATA, objExtraData.toString());
        eventValue.put(MoMoParameterNamePayment.REQUEST_TYPE, "payment");
        eventValue.put(MoMoParameterNamePayment.LANGUAGE, "vi");

        eventValue.put(MoMoParameterNamePayment.EXTRA, "");
        Log.d("getActivity",getActivity().toString());

        AppMoMoLib.getInstance().requestMoMoCallBack(getActivity(), eventValue);

    }



    public void customDialogbox(){

        // custom dialog
        final Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.custom_dialoge_box);

        // set the custom dialog components - text, image and button

        RelativeLayout cancelDiv = (RelativeLayout) dialog.findViewById(R.id.forth);
        RelativeLayout currentOrderDiv = (RelativeLayout) dialog.findViewById(R.id.second);
        RelativeLayout pastOrderDiv = (RelativeLayout) dialog.findViewById(R.id.third);
        TextView first_tv = (TextView)dialog.findViewById(R.id.first_tv);
        TextView second_tv = (TextView)dialog.findViewById(R.id.second_tv);
        TextView third_tv = (TextView)dialog.findViewById(R.id.third_tv);
        first_tv.setText("Sửa");
        first_tv.setTextColor(getResources().getColor(R.color.colorFB));
        second_tv.setText("Xóa");
        second_tv.setTextColor(getResources().getColor(R.color.colorRed));
        third_tv.setTextColor(getResources().getColor(R.color.colorFB));

        currentOrderDiv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editNode();

                UPDATE_NODE = true;

                dialog.dismiss();

            }
        });

        pastOrderDiv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                deleteSelectedNode(key);
                dialog.dismiss();

            }
        });

        // if button is clicked, close the custom dialog
        cancelDiv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }


    @Override
    public void onResume() {
        super.onResume();
        if(CART_NOT_LOAD ){
            CART_NOT_LOAD = false;
        }
        else {
            getCartData();
        }
    }

    public void deleteSelectedNode(final String key){

       final DatabaseReference deleteNode = mDatabase.child(key);

       deleteNode.addListenerForSingleValueEvent(new ValueEventListener() {
           @Override
           public void onDataChange(DataSnapshot dataSnapshot) {

               String name = dataSnapshot.child("key").getValue(String.class);


                   if(name.equalsIgnoreCase(key)){
                       deleteNode.setValue(null);
                       getCartData();

                       int getCartCount = sPref.getInt("count",0);

                       SharedPreferences.Editor editor = sPref.edit();
                       editor.putInt("count",getCartCount-1).commit();
                       getActivity().sendBroadcast(new Intent("AddToCart"));

                   }

           }

           @Override
           public void onCancelled(DatabaseError databaseError) {

           }
       });

    }

    public void editNode(){

        final DatabaseReference deleteNode = mDatabase.child(key);

        deleteNode.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child("key").getValue(String.class);

                if(name.equalsIgnoreCase(key)){

                    extraID = dataSnapshot.child("mID").getValue(String.class);
                    mDesc = dataSnapshot.child("mDesc").getValue(String.class);
                    mGrandTotal = dataSnapshot.child("grandTotal").getValue(String.class);
                    mInstruction =dataSnapshot.child("instruction").getValue(String.class);
                    mCurrency = dataSnapshot.child("mCurrency").getValue(String.class);
                    mDesc_ = dataSnapshot.child("mDesc").getValue(String.class);
                    mFee = dataSnapshot.child("mFee").getValue(String.class);
                    mName = dataSnapshot.child("mName").getValue(String.class);
                    mPrice = dataSnapshot.child("mPrice").getValue(String.class);
                    mQuantity_ = dataSnapshot.child("mQuantity").getValue(String.class);
                    mTax = dataSnapshot.child("mTax").getValue(String.class);
                    minimumOrderPrice_ = dataSnapshot.child("minimumOrderPrice").getValue(String.class);
                    required = dataSnapshot.child("required").getValue(String.class);
                    restID = dataSnapshot.child("restID").getValue(String.class);

                    Intent intent = new Intent(getContext(),AddToCartActivity.class);
                    intent.putExtra("extra_id",extraID );
                    intent.putExtra("desc",mDesc);
                    intent.putExtra("name",mName);
                    intent.putExtra("price",mPrice);
                    intent.putExtra("symbol",mCurrency);
                    intent.putExtra("key",key);
                    startActivity(intent);

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



    }

}
