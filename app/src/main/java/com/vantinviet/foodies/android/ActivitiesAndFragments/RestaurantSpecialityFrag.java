package com.vantinviet.foodies.android.ActivitiesAndFragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.vantinviet.foodies.android.Adapters.CountryListAdapter;
import com.vantinviet.foodies.android.Adapters.RestSpecialityAdapter;
import com.vantinviet.foodies.android.Constants.AllConstants;
import com.vantinviet.foodies.android.Constants.Config;
import com.vantinviet.foodies.android.Constants.PreferenceClass;
import com.vantinviet.foodies.android.Models.SpecialityModel;

import com.vantinviet.foodies.android.R;
import com.vantinviet.foodies.android.Utils.FontHelper;
import com.vantinviet.foodies.android.Utils.TabLayoutUtils;
import com.gmail.samehadar.iosdialog.CamomileSpinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by Nabeel on 1/26/2018.
 */

public class RestaurantSpecialityFrag extends Fragment {

    ImageView close_country;
    TextView title_city_tv;

    ArrayList<SpecialityModel> specialityArray;
    RecyclerView.LayoutManager recyclerViewlayoutManager;
    RestSpecialityAdapter recyclerViewadapter;
    RecyclerView card_recycler_view;

    CamomileSpinner pbHeaderProgress;
    SharedPreferences sharedPreferences;
    SearchView searchView;

    public static boolean RESTAURANT_SPECIALITY;
    RelativeLayout transparent_layer,progressDialog;
    @SuppressWarnings("deprecation")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.search_screen, container, false);
        FontHelper.applyFont(getContext(),getActivity().getWindow().getDecorView().getRootView(), AllConstants.verdana);

        searchView = v.findViewById(R.id.simpleSearchView);
        searchView.setQueryHint(Html.fromHtml("<font color = #dddddd>" + "Tìm kiếm đặc trưng của nhà hàng" + "</font>"));
        TextView searchText = (TextView)
                v.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        searchText.setTextSize(TypedValue.COMPLEX_UNIT_SP,14);
        searchText.setPadding(0,0,0,0);
        LinearLayout searchEditFrame = (LinearLayout) searchView.findViewById(R.id.search_edit_frame); // Get the Linear Layout
// Get the associated LayoutParams and set leftMargin
        ((LinearLayout.LayoutParams) searchEditFrame.getLayoutParams()).leftMargin = 5;
        search(searchView);
        card_recycler_view = v.findViewById(R.id.countries_list);
        recyclerViewlayoutManager = new LinearLayoutManager(getContext());
        card_recycler_view.setLayoutManager(recyclerViewlayoutManager);
        sharedPreferences = getContext().getSharedPreferences(PreferenceClass.user, Context.MODE_PRIVATE);
        pbHeaderProgress = v.findViewById(R.id.pbHeaderProgress);
        pbHeaderProgress.start();

        init(v);
        return v;
    }

    public void init(View v){


        progressDialog = v.findViewById(R.id.progressDialog);
        transparent_layer = v.findViewById(R.id.transparent_layer);

        specialityArray = new ArrayList<>();
        title_city_tv = v.findViewById(R.id.title_city_tv);

        if(RestaurantsFragment.FLAG_Restaurant_FRAGMENT){
            title_city_tv.setText("Chọn đặc trưng");
            RestaurantsFragment.FLAG_Restaurant_FRAGMENT = false;
        }

        close_country = v.findViewById(R.id.close_country);
        close_country.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

               /* if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.stopAutoManage((FragmentActivity) getContext());
                    mGoogleApiClient.disconnect();
                }*/
                RestaurantsFragment fragmentChild = new RestaurantsFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.search_main_container, fragmentChild);
                transaction.addToBackStack(null);
                transaction.commit();

            }
        });

        getRestSpecialityList();
    }


    @SuppressLint("LongLogTag")
    public void getRestSpecialityList(){

        TabLayoutUtils.enableTabs(PagerMainActivity.tabLayout,false);
        transparent_layer.setVisibility(View.VISIBLE);
        progressDialog.setVisibility(View.VISIBLE);
        RequestQueue queue = Volley.newRequestQueue(getContext());
        Log.d("SHOW_REST_SPECIALITY_LIST Link",Config.SHOW_REST_SPECIALITY_LIST);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Config.SHOW_REST_SPECIALITY_LIST, null, new Response.Listener<JSONObject>() {
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

                        for(int i = 0; i<jsonarray.length(); i++){

                            JSONObject jsonObject = jsonarray.getJSONObject(i);
                            SpecialityModel specialityModel = new SpecialityModel();

                            specialityModel.setName(jsonObject.optString("name"));
                            specialityModel.setId(jsonObject.optString("id"));


                            specialityArray.add(specialityModel);

                        }

                        if(specialityArray!=null) {
                            TabLayoutUtils.enableTabs(PagerMainActivity.tabLayout,true);
                            transparent_layer.setVisibility(View.GONE);
                            progressDialog.setVisibility(View.GONE);
                            recyclerViewadapter = new RestSpecialityAdapter(specialityArray, getActivity());
                            card_recycler_view.setAdapter(recyclerViewadapter);
                            recyclerViewadapter.notifyDataSetChanged();
                        }

                        recyclerViewadapter.setOnItemClickListner(new CountryListAdapter.OnItemClickListner() {
                            @Override
                            public void OnItemClicked(View view, int position) {
/*
                                if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                                    mGoogleApiClient.stopAutoManage((FragmentActivity) getContext());
                                    mGoogleApiClient.disconnect();
                                }*/
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString(PreferenceClass.RESTAURANT_SPECIALITY,specialityArray.get(position).getName());
                                editor.commit();

                                RESTAURANT_SPECIALITY = true;
                                RestaurantsFragment fragmentChild = new RestaurantsFragment();
                                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                                transaction.replace(R.id.search_main_container, fragmentChild);
                                transaction.addToBackStack(null);
                                transaction.commit();


                            }
                        });
                    }


                }
                catch (JSONException e){
                    e.getMessage();
                }



            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            //    Toast.makeText(getContext(),error.toString(),Toast.LENGTH_SHORT).show();
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

        queue.add(jsonObjectRequest);

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
