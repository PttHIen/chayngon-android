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
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
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
import com.vantinviet.foodies.android.Adapters.RestaurantsAdapter;
import com.vantinviet.foodies.android.Constants.Config;
import com.vantinviet.foodies.android.Constants.PreferenceClass;
import com.vantinviet.foodies.android.Models.RestaurantsModel;
import com.vantinviet.foodies.android.R;
import com.vantinviet.foodies.android.Utils.TabLayoutUtils;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Nabeel on 1/12/2018.
 */

public class ShowFavoriteRestFragment extends Fragment {

    ArrayList<RestaurantsModel> GetDataAdapter1;

    RecyclerView restaurant_recycler_view;

    SwipeRefreshLayout refresh_layout;

    RecyclerView.LayoutManager recyclerViewlayoutManager;
    RestaurantsAdapter recyclerViewadapter;

    CamomileSpinner progressBar;

    SharedPreferences sharedPreferences;

    ImageView back_icon;
    SearchView searchView;
    public static boolean FLAG_SHOW_FAV;
    public static boolean FROM_FAVORITE;

    RelativeLayout transparent_layer,progressDialog;
    String user_id;
    PercentRelativeLayout no_job_div;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.show_favorite_fragment, container, false);
        sharedPreferences = getContext().getSharedPreferences(PreferenceClass.user, Context.MODE_PRIVATE);
        user_id = sharedPreferences.getString(PreferenceClass.pre_user_id,"");
        restaurant_recycler_view = view.findViewById(R.id.restaurant_recycler_view);
        progressBar = view.findViewById(R.id.restaurantProgress);
        progressBar.start();
        restaurant_recycler_view.setHasFixedSize(true);
        recyclerViewlayoutManager = new LinearLayoutManager(getContext());
        restaurant_recycler_view.setLayoutManager(recyclerViewlayoutManager);
        init(view);
        getRestaurantList(user_id);
        return view;

    }

    public void init(View v){
        no_job_div = v.findViewById(R.id.no_job_div);
        progressDialog = v.findViewById(R.id.progressDialog);
        transparent_layer = v.findViewById(R.id.transparent_layer);

        searchView = v.findViewById(R.id.floating_search_view);
        search(searchView);

        back_icon = v.findViewById(R.id.back_icon);
        back_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment restaurantMenuItemsFragment = new UserAccountFragment();
                FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                transaction.add(R.id.restaurent_main_layout, restaurantMenuItemsFragment,"parent").commit();
              //  getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
            }
        });
        refresh_layout = v.findViewById(R.id.refresh_layout);
        refresh_layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                getRestaurantList(user_id);
                refresh_layout.setRefreshing(false);
            }
        });

    }

    public void getRestaurantList(String user_id){
        TabLayoutUtils.enableTabs(PagerMainActivity.tabLayout,false);
        progressBar.start();
        transparent_layer.setVisibility(View.VISIBLE);
        progressDialog.setVisibility(View.VISIBLE);
        GetDataAdapter1 = new ArrayList<>();
        RequestQueue queue = Volley.newRequestQueue(getContext());


        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("user_id", user_id);


            Log.e("Obj",jsonObject.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("jsonObjectfavo", jsonObject.toString());
// Request a string response from the provided URL.
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                Config.SHOW_FAV_RESTAURANT,jsonObject,
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

                                Log.d("jsonarrfa",jsonarray.toString());
                                for (int i = 0; i < jsonarray.length(); i++) {

                                    JSONObject   json1 = jsonarray.getJSONObject(i);

                                    JSONObject jsonObjRestaurant = json1.getJSONObject("Restaurant");
                                    Log.d("Restaurant", jsonObjRestaurant.toString());
                                    JSONObject jsonRestaurantFavorite = json1.getJSONObject("RestaurantFavourite");
                                    Log.d("RestaurantFavourite", jsonRestaurantFavorite.toString());
                                    JSONObject jsonObjCurrency = jsonObjRestaurant.getJSONObject("Currency");
                                    Log.d("Currency", jsonObjCurrency.toString());
                                    String symbol = jsonObjCurrency.optString("symbol");

                                    Log.d("symbol", symbol.toString());
                                  //  JSONObject jsonObjTax = jsonObjRestaurant.getJSONObject("Tax");
                                  //  Log.d("Tax", jsonObjTax.toString());
                                    JSONObject jsonObjRating = null;
                                    try {
                                        jsonObjRating = json1.getJSONObject("TotalRatings");
                                    }
                                    catch (JSONException ignored){
                                        ignored.getCause();
                                    }


                                    RestaurantsModel RestaurantObj = new RestaurantsModel();
                                    RestaurantObj.setRestaurant_name(jsonObjRestaurant.optString("name"));
                                    RestaurantObj.setRestaurant_slogen(jsonObjRestaurant.optString("slogan"));
                                    RestaurantObj.setRestaurant_about(jsonObjRestaurant.optString("about"));
                                    RestaurantObj.setRestaurant_fee(symbol+jsonObjRestaurant.optString("delivery_fee"));
                                    RestaurantObj.setRestaurant_image(jsonObjRestaurant.optString("image"));
                                    RestaurantObj.setRestaurant_id(jsonObjRestaurant.optString("id"));
                                    RestaurantObj.setRestaurant_phone(jsonObjRestaurant.optString("phone"));
                                    RestaurantObj.setRestaurant_cover(jsonObjRestaurant.optString("cover_image"));
                                    RestaurantObj.setRestaurant_isFav(jsonRestaurantFavorite.optString("favourite"));
                                    RestaurantObj.setPromoted(jsonObjRestaurant.optString("promoted"));
                                    RestaurantObj.setPreparation_time(jsonObjRestaurant.optString("preparation_time"));


                                    if(jsonObjRating==null) {

                                        RestaurantObj.setRestaurant_avgRating("0.00");
                                        RestaurantObj.setRestaurant_totalRating("0.00");
                                    }
                                    else {
                                        RestaurantObj.setRestaurant_avgRating(jsonObjRating.optString("avg"));
                                    }
                                    RestaurantObj.setRestaurant_currency(jsonObjCurrency.optString("symbol"));
                              //      RestaurantObj.setRestaurant_tax(jsonObjTax.optString("tax"));
                                    RestaurantObj.setRestaurant_restaurant_menu_style(jsonObjRestaurant.optString("menu_style"));

                                 /*  if (ratingObj!=null){
                                        RestaurantObj.setRestaurant_avgRating(ratingObj.getString("avg"));
                                    }
                                    else if(ratingObj==null){
                                        RestaurantObj.setRestaurant_avgRating("0.00");
                                    }*/


                                    GetDataAdapter1.add(RestaurantObj);
                                }
                                if(GetDataAdapter1!=null && GetDataAdapter1.size()>0) {
                                    recyclerViewadapter = new RestaurantsAdapter(GetDataAdapter1, getContext(), ShowFavoriteRestFragment.this, progressBar);
                                    restaurant_recycler_view.setAdapter(recyclerViewadapter);
                                    recyclerViewadapter.notifyDataSetChanged();
                                    TabLayoutUtils.enableTabs(PagerMainActivity.tabLayout, true);
                                    transparent_layer.setVisibility(View.GONE);
                                    progressDialog.setVisibility(View.GONE);

                                    recyclerViewadapter.setOnItemClickListner(new RestaurantsAdapter.OnItemClickListner() {
                                        @Override
                                        public void OnItemClicked(View view, final int position) {


                                            SharedPreferences.Editor editor = sharedPreferences.edit();
                                            editor.putString(PreferenceClass.RESTAURANT_NAME, GetDataAdapter1.get(position).getRestaurant_name());
                                            editor.putString(PreferenceClass.RESTAURANT_SALOGON, GetDataAdapter1.get(position).getRestaurant_salgon());
                                            editor.putString(PreferenceClass.RESTAURANT_IMAGE, GetDataAdapter1.get(position).getRestaurant_image());
                                            editor.putString(PreferenceClass.RESTAURANT_ID, GetDataAdapter1.get(position).getRestaurant_id());
                                            editor.putString(PreferenceClass.RESTAURANT_ABOUT, GetDataAdapter1.get(position).getRestaurant_about());
                                            editor.putString(PreferenceClass.RESTAURANT_RATING, GetDataAdapter1.get(position).getRestaurant_avgRating());
                                            editor.commit();

                                            FLAG_SHOW_FAV = true;

                                            Fragment restaurantMenuItemsFragment = new RestaurantMenuItemsFragment();
                                            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                                            transaction.add(R.id.restaurent_main_layout, restaurantMenuItemsFragment, "parent").commit();
                                        }
                                    });
                                }
                                else {
                                    recyclerViewadapter = new RestaurantsAdapter(GetDataAdapter1, getContext(), ShowFavoriteRestFragment.this, progressBar);
                                    restaurant_recycler_view.setAdapter(recyclerViewadapter);
                                    recyclerViewadapter.notifyDataSetChanged();
                                    TabLayoutUtils.enableTabs(PagerMainActivity.tabLayout,true);
                                    transparent_layer.setVisibility(View.GONE);
                                    progressDialog.setVisibility(View.GONE);
                                    no_job_div.setVisibility(View.VISIBLE);
                                }

                            }else{
                                JSONObject json = new JSONObject(jsonResponse.toString());
                              //  Toast.makeText(getContext(),json.optString("msg"), Toast.LENGTH_SHORT).show();
                                TabLayoutUtils.enableTabs(PagerMainActivity.tabLayout,true);
                                transparent_layer.setVisibility(View.GONE);
                                progressDialog.setVisibility(View.GONE);

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
            //    Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
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

        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(
                35000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(jsonObjReq);

    }


    private void search(android.support.v7.widget.SearchView searchView) {

        searchView.setOnQueryTextListener(new android.support.v7.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                if (recyclerViewadapter != null) recyclerViewadapter.getFilter().filter(newText);
                return true;
            }
        });
    }


}
