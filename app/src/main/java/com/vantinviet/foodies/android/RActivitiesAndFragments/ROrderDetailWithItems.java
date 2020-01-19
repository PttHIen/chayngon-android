package com.vantinviet.foodies.android.RActivitiesAndFragments;

import android.content.Context;
import android.content.SharedPreferences;
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
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.gmail.samehadar.iosdialog.CamomileSpinner;
import com.vantinviet.foodies.android.Adapters.ExpandableListAdapter;
import com.vantinviet.foodies.android.Constants.AllConstants;
import com.vantinviet.foodies.android.Constants.Config;
import com.vantinviet.foodies.android.Constants.PreferenceClass;
import com.vantinviet.foodies.android.Models.MenuItemExtraModel;
import com.vantinviet.foodies.android.Models.MenuItemModel;
import com.vantinviet.foodies.android.R;
import com.vantinviet.foodies.android.Utils.CustomExpandableListView;
import com.vantinviet.foodies.android.Utils.FontHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ROrderDetailWithItems extends Fragment{

    TextView order_title_tv;
    SharedPreferences sPref_Items;
    String order_number;
    ExpandableListAdapter listAdapter;
    CustomExpandableListView customExpandableListView;
    ArrayList<MenuItemModel> listDataHeader;
    ArrayList<MenuItemExtraModel> listChildData;
    private ArrayList<ArrayList<MenuItemExtraModel>> ListChild;
    CamomileSpinner orderProgress;
    RelativeLayout transparent_layer,progressDialog;
    ImageView back_icon;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        View v = inflater.inflate(R.layout.order_detail_items, container, false);
        FrameLayout frameLayout = v.findViewById(R.id.main_order_item_detail);
        FontHelper.applyFont(getContext(),frameLayout, AllConstants.verdana);

        frameLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });

        sPref_Items = getContext().getSharedPreferences(PreferenceClass.user, Context.MODE_PRIVATE);

        initUI(v);


        customExpandableListView = (CustomExpandableListView ) v.findViewById(R.id.custon_list_order_items);
        customExpandableListView .setExpanded(true);
        customExpandableListView.setGroupIndicator(null);

        customExpandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v,
                                        int groupPosition, long id) {
                return true; // This way the expander cannot be collapsed
            }
        });
        return v;
    }

    public void initUI(View v){

        order_number = sPref_Items.getString(PreferenceClass.RIDER_ORDER_NUMBER,"");
        order_title_tv = v.findViewById(R.id.order_title_tv);
        order_title_tv.setText("Order #"+order_number);
        orderProgress = v.findViewById(R.id.orderProgress);
        orderProgress.start();
        progressDialog = v.findViewById(R.id.progressDialog);
        transparent_layer = v.findViewById(R.id.transparent_layer);
        back_icon = v.findViewById(R.id.back_icon);
        back_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Fragment restaurantMenuItemsFragment = new ROrderDetailFragment();
                FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                transaction.add(R.id.main_order_item_detail, restaurantMenuItemsFragment,"parent").commit();
            }
        });


        getOrderDetailItems();


    }


    public void getOrderDetailItems(){
    //    TabLayoutUtils.enableTabs(PagerMainActivity.tabLayout,false);
        transparent_layer.setVisibility(View.VISIBLE);
        progressDialog.setVisibility(View.VISIBLE);
        listDataHeader = new ArrayList<MenuItemModel>();
        ListChild = new ArrayList<>();

        //   listDataChild = new HashMap<MenuItemModel, ArrayList<String>>();
        RequestQueue queue = Volley.newRequestQueue(getContext());
        JSONObject orderJsonObject = new JSONObject();
        try {
            orderJsonObject.put("order_id",order_number);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest orderJsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Config.SHOW_ORDER_DETAIL, orderJsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                String strJson =  response.toString();
                JSONObject jsonResponse = null;
                try {
                    jsonResponse = new JSONObject(strJson);

                    Log.d("JSONPost", jsonResponse.toString());

                    int code_id  = Integer.parseInt(jsonResponse.optString("code"));

                    if(code_id == 200) {

                        JSONObject json = new JSONObject(jsonResponse.toString());
                        JSONArray jsonArray = json.getJSONArray("msg");

                        for (int i=0;i<jsonArray.length();i++){

                            JSONObject allJsonObject = jsonArray.getJSONObject(i);
                            JSONObject orderJsonObject = allJsonObject.getJSONObject("Order");
                            JSONObject userInfoObj = allJsonObject.getJSONObject("UserInfo");
                            JSONObject userAddressObj = allJsonObject.getJSONObject("Address");
                            JSONObject restaurantJsonObject = allJsonObject.getJSONObject("Restaurant");
                            JSONObject taxObj = restaurantJsonObject.getJSONObject("Tax");
                            JSONObject restaurantCurrencuObj = restaurantJsonObject.getJSONObject("Currency");
                            String currency_symbol= restaurantCurrencuObj.optString("symbol");

                            String first_name = userInfoObj.optString("first_name");
                            String last_name = userInfoObj.optString("last_name");
                         //   order_user_name_tv.setText(first_name+" "+last_name);
                           // order_user_number_tv.setText(userInfoObj.optString("phone"));
                            String street_user = userAddressObj.optString("street");
                            String zip_user = userAddressObj.optString("zip");
                            String city_user = userAddressObj.optString("city");
                            String state_user = userAddressObj.optString("state");
                            String country_user = userAddressObj.optString("country");

                         /*   if(delivery.equalsIgnoreCase("0")){
                                order_user_address_tv.setText("Pick Up");
                            }
                            else {
                                order_user_address_tv.setText(street_user + ", " + city_user);
                            }

                            if(order_user_address_tv.getText().toString().equalsIgnoreCase("Pick Up")){
                                track_order_div.setBackgroundColor(getContext().getResources().getColor(R.color.trackColor));
                                pick_up = 1;
                            }
*/

                          /*  if(HJobsFragment.FLAG_HJOBS) {
                                order_user_address_tv.setText(street_user + ", " + city_user);
                            }
*/
                         //   inst_tv.setText(orderJsonObject.optString("instructions"));
                      //      total_amount_tv.setText(currency_symbol+orderJsonObject.optString("price"));

                            String getPaymentMethodTV = orderJsonObject.optString("cod");
                            if(getPaymentMethodTV.equalsIgnoreCase("0")) {
                              //  payment_method_tv.setText("Credit Card");
                            }
                            else {
                              //  payment_method_tv.setText("Cash On Delivery");
                            }

                         //   hotel_name_tv.setText(restaurantJsonObject.optString("name"));
                          //  hotel_phone_number_tv.setText(restaurantJsonObject.optString("phone"));
                            JSONObject restaurantAddress = restaurantJsonObject.getJSONObject("RestaurantLocation");
                            String street = restaurantAddress.optString("street");
                            String zip = restaurantAddress.optString("zip");
                            String city = restaurantAddress.optString("city");
                            String state = restaurantAddress.optString("state");
                            String country = restaurantAddress.optString("country");

                          /*  hotel_add_tv.setText(street+", "+city);
                            if(HJobsFragment.FLAG_HJOBS) {
                                hotel_add_tv.setText(street + ", " + city);
                            }*/

                            //// Total Payment
                            String tax = orderJsonObject.optString("tax");

                            String delivery_fee = orderJsonObject.optString("delivery_fee");
                          //  total_delivery_fee_tv.setText(currency_symbol+delivery_fee);
                            String tax_free = restaurantJsonObject.optString("tax_free");
                            if(tax_free.equalsIgnoreCase("1")) {
                              //  tax_tv.setText("(" + "0" + "%)");
                            }
                            // Double getTotalTax = Double.parseDouble(tax)*Double.parseDouble(sub_total)/100;
                          //  total_tex_tv.setText(tax);

                            String subTotal = orderJsonObject.optString("sub_total");
                           // sub_total_amount_tv.setText(subTotal);


                            //// End

                            JSONArray menuItemArray = allJsonObject.getJSONArray("OrderMenuItem");

                            for (int j=0;j<menuItemArray.length();j++) {

                                JSONObject alljsonJsonObject2 = menuItemArray.getJSONObject(j);
                                MenuItemModel menuItemModel = new MenuItemModel();
                                menuItemModel.setItem_name(alljsonJsonObject2.optString("name"));
                                menuItemModel.setItem_price(currency_symbol + alljsonJsonObject2.optString("price"));
                                menuItemModel.setId(alljsonJsonObject2.optString("id"));
                                menuItemModel.setOrder_id(alljsonJsonObject2.optString("order_id"));
                                menuItemModel.setOrder_quantity(alljsonJsonObject2.optString("quantity"));

                                listDataHeader.add(menuItemModel);

                                listChildData = new ArrayList<>();

                                JSONArray extramenuItemArray = alljsonJsonObject2.getJSONArray("OrderMenuExtraItem");
                                if(extramenuItemArray!=null&& extramenuItemArray.length()>0){
                                    for (int k = 0; k < extramenuItemArray.length(); k++) {
                                        if (extramenuItemArray.length() != 0) {
                                            JSONObject allJsonObject3 = extramenuItemArray.getJSONObject(k);
                                            MenuItemExtraModel menuItemExtraModel = new MenuItemExtraModel();

                                            menuItemExtraModel.setExtra_item_name(allJsonObject3.optString("name"));
                                            menuItemExtraModel.setPrice(allJsonObject3.optString("price"));
                                            menuItemExtraModel.setQuantity(allJsonObject3.optString("quantity"));
                                            menuItemExtraModel.setCurrency(currency_symbol);

                                            listChildData.add(menuItemExtraModel);

                                        }

                                    }

                                }
                                ListChild.add(listChildData);
                            }
                        }

                        listAdapter = new ExpandableListAdapter(getContext(), listDataHeader, ListChild);
                      //  TabLayoutUtils.enableTabs(PagerMainActivity.tabLayout,true);
                        transparent_layer.setVisibility(View.GONE);
                        progressDialog.setVisibility(View.GONE);
                        // setting list adapter
                        customExpandableListView.setAdapter(listAdapter);
                        for(int l=0; l < listAdapter.getGroupCount(); l++)
                            if(ListChild.size()!=0) {
                                customExpandableListView.expandGroup(l);
                            }

                    }



                }catch (Exception e){
                    e.getMessage();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
               // TabLayoutUtils.enableTabs(PagerMainActivity.tabLayout,true);
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

        queue.add(orderJsonObjectRequest);

    }

}
