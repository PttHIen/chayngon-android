package com.vantinviet.foodies.android.ActivitiesAndFragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.percent.PercentRelativeLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.vantinviet.foodies.android.Adapters.DealsAdapter;
import com.vantinviet.foodies.android.Constants.Config;
import com.vantinviet.foodies.android.Constants.PreferenceClass;
import com.vantinviet.foodies.android.Models.DealsModel;

import com.vantinviet.foodies.android.R;
import com.vantinviet.foodies.android.Utils.TabLayoutUtils;
import com.gmail.samehadar.iosdialog.CamomileSpinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Nabeel on 1/25/2018.
 */

public class RestaurantDealsFragment extends Fragment {

    private RecyclerView deals_recyclerview;
    RecyclerView.LayoutManager recyclerViewlayoutManager;
    DealsAdapter recyclerViewadapter;
    CamomileSpinner dealsProgressBar;
    SwipeRefreshLayout mSwipeRefreshLayout;
    ArrayList<DealsModel> delsArrayList;
    SharedPreferences dealsSharedPreferences;
    ImageView back_icon;
    String lat,lon;
    @SuppressWarnings("deprecation")
    PercentRelativeLayout no_job_div;

    public static boolean RESTAUNT_DEALS_FRAG;

    RelativeLayout transparent_layer,progressDialog;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.deals_fragment, container, false);

        dealsSharedPreferences = getContext().getSharedPreferences(PreferenceClass.user, Context.MODE_PRIVATE);

        progressDialog = view.findViewById(R.id.progressDialog);
        transparent_layer = view.findViewById(R.id.transparent_layer);
        deals_recyclerview = view.findViewById(R.id.deals_recyclerview);
        dealsProgressBar = view.findViewById(R.id.dealsProgress);
        dealsProgressBar.start();

        deals_recyclerview.setHasFixedSize(true);

        recyclerViewlayoutManager = new LinearLayoutManager(getContext());
        deals_recyclerview.setLayoutManager(recyclerViewlayoutManager);
        RESTAUNT_DEALS_FRAG = true;

        initUI(view);
        getDealsList();

        return view;

    }

    private void initUI(View v){
        no_job_div = v.findViewById(R.id.no_job_div);
        back_icon = v.findViewById(R.id.back_icon);

        if(RESTAUNT_DEALS_FRAG){
            back_icon.setVisibility(View.VISIBLE);

        }

        back_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RESTAUNT_DEALS_FRAG = false;
                Fragment reviewListFragment = new RestaurantMenuItemsFragment();
                FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                transaction.replace(R.id.dearls_frag_main_container, reviewListFragment,"ParentFragment_MenuItems").commit();

            }
        });

        mSwipeRefreshLayout = v.findViewById(R.id.refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getDealsList();


                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

    }


    private void getDealsList(){

        TabLayoutUtils.enableTabs(PagerMainActivity.tabLayout,false);
        transparent_layer.setVisibility(View.VISIBLE);
        progressDialog.setVisibility(View.VISIBLE);

        String rest_id = dealsSharedPreferences.getString(PreferenceClass.RESTAURANT_ID,"");
        lat = dealsSharedPreferences.getString(PreferenceClass.LATITUDE,"");
        lon = dealsSharedPreferences.getString(PreferenceClass.LONGITUDE,"");
        delsArrayList = new ArrayList<>();
        RequestQueue queue = Volley.newRequestQueue(getContext());

        JSONObject jsonObject = new JSONObject();
        try {

            jsonObject.put("restaurant_id",rest_id);
            jsonObject.put("lat", lat);
            jsonObject.put("long", lon);
            //jsonObject.put("lat", latitude);
            //jsonObject.put("long", longitude);

            Log.e("Obj",jsonObject.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }

// Request a string response from the provided URL.
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                Config.SHOW_RESTAURANT_DEALS,jsonObject,
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

                                JSONObject json = new JSONObject(jsonResponse.toString());
                                JSONArray jsonarray = json.getJSONArray("msg");

                                for (int i = 0; i < jsonarray.length(); i++) {

                                    JSONObject json1 = jsonarray.getJSONObject(i);

                                    JSONObject jsonObjDeal = json1.getJSONObject("Deal");
                                    JSONObject jsonObjRestaurant = json1.getJSONObject("Restaurant");
                                    JSONObject jsonObjCurrency = jsonObjRestaurant.getJSONObject("Currency");
                                    JSONObject jsonObjTax = jsonObjRestaurant.getJSONObject("Tax");

                                    DealsModel dealsModel = new DealsModel();

                                    dealsModel.setPromoted(jsonObjDeal.optString("promoted"));
                                    dealsModel.setDeal_cover_image(jsonObjDeal.optString("cover_image"));
                                    dealsModel.setDeal_image(jsonObjDeal.optString("image"));
                                    dealsModel.setDeal_desc(jsonObjDeal.optString("description"));
                                    dealsModel.setDeal_restaurant_id(jsonObjDeal.optString("restaurant_id"));
                                    dealsModel.setDeal_id(jsonObjDeal.optString("id"));
                                    dealsModel.setDeal_name(jsonObjDeal.optString("name"));
                                    dealsModel.setDeal_price(jsonObjDeal.optString("price"));
                                    dealsModel.setDeal_expiry_date(jsonObjDeal.optString("ending_time"));

                                    dealsModel.setDeal_symbol(jsonObjCurrency.optString("symbol"));
                                    dealsModel.setRestaurant_name(jsonObjRestaurant.optString("name"));
                                    dealsModel.setDeal_tax(jsonObjTax.optString("tax"));
                                    dealsModel.setDeal_delivery_fee(jsonObjTax.optString("delivery_fee_per_mile"));
                                    dealsModel.setIsDeliveryFree(jsonObjRestaurant.optString("tax_free"));

                                    delsArrayList.add(dealsModel);

                                }
                                if (delsArrayList!=null&&delsArrayList.size()>0) {
                                    TabLayoutUtils.enableTabs(PagerMainActivity.tabLayout,true);
                                    transparent_layer.setVisibility(View.GONE);
                                    progressDialog.setVisibility(View.GONE);

                                    recyclerViewadapter = new DealsAdapter(delsArrayList, getActivity());
                                    deals_recyclerview.setAdapter(recyclerViewadapter);
                                    recyclerViewadapter.notifyDataSetChanged();
                                    recyclerViewadapter.setOnItemClickListner(new DealsAdapter.OnItemClickListner() {
                                        @Override
                                        public void OnItemClicked(View view, int position) {

                                            SharedPreferences.Editor editor = dealsSharedPreferences.edit();
                                            editor.putString(PreferenceClass.DEALS_DESC, delsArrayList.get(position).getDeal_desc());
                                            editor.putString(PreferenceClass.DEALS_HOTEL_NAME, delsArrayList.get(position).getRestaurant_name());
                                            editor.putString(PreferenceClass.DELAS_NAME, delsArrayList.get(position).getDeal_name());
                                            editor.putString(PreferenceClass.DEALS_PRICE, delsArrayList.get(position).getDeal_price());
                                            editor.putString(PreferenceClass.DEALS_IMAGE, delsArrayList.get(position).getDeal_image());
                                            editor.putString(PreferenceClass.DEALS_CURRENCY_SYMBOL, delsArrayList.get(position).getDeal_symbol());
                                            editor.putString(PreferenceClass.DEALS_TAX, delsArrayList.get(position).getDeal_tax());
                                            editor.putString(PreferenceClass.DEALS_DELIVERY_FEE, delsArrayList.get(position).getDeal_delivery_fee());
                                            editor.putString(PreferenceClass.RESTAURANT_ID,delsArrayList.get(position).getDeal_restaurant_id());
                                            editor.putString(PreferenceClass.DEAL_ID,delsArrayList.get(position).getDeal_id());
                                            editor.putString(PreferenceClass.IS_DELIVERY_FREE,delsArrayList.get(position).getIsDeliveryFree());
                                          //  editor.putString(PreferenceClass.DELIVERY,delsArrayList.get(position)
                                            editor.commit();

                                            Fragment restaurantMenuItemsFragment = new DealsDetailRestFragment();
                                            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                                            transaction.add(R.id.dearls_frag_main_container, restaurantMenuItemsFragment, "parent").commit();


                                        }
                                    });
                                }
                                else {
                                    TabLayoutUtils.enableTabs(PagerMainActivity.tabLayout,true);
                                    transparent_layer.setVisibility(View.GONE);
                                    progressDialog.setVisibility(View.GONE);

                                    no_job_div.setVisibility(View.VISIBLE);
                                }


                            }else{
                                TabLayoutUtils.enableTabs(PagerMainActivity.tabLayout,true);
                                transparent_layer.setVisibility(View.GONE);
                                progressDialog.setVisibility(View.GONE);

                                JSONObject json = new JSONObject(jsonResponse.toString());
                              //  Toast.makeText(getContext(),json.optString("msg"), Toast.LENGTH_SHORT).show();
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
                //  ed_progress.setVisibility(View.GONE);
                VolleyLog.d("JSONPost", "Error: " + error.getMessage());
              //  Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                TabLayoutUtils.enableTabs(PagerMainActivity.tabLayout,true);
                transparent_layer.setVisibility(View.GONE);
                progressDialog.setVisibility(View.GONE);

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
