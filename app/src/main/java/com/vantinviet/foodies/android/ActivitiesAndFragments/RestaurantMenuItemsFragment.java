package com.vantinviet.foodies.android.ActivitiesAndFragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.SearchView;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.vantinviet.foodies.android.Adapters.RestaurantMenuAdapter;
import com.vantinviet.foodies.android.Constants.AllConstants;
import com.vantinviet.foodies.android.Constants.Config;
import com.vantinviet.foodies.android.Constants.PreferenceClass;
import com.vantinviet.foodies.android.Models.RestaurantChildModel;
import com.vantinviet.foodies.android.Models.RestaurantParentModel;
import com.vantinviet.foodies.android.R;
import com.vantinviet.foodies.android.Utils.CustomExpandableListView;
import com.vantinviet.foodies.android.Utils.FontHelper;
import com.vantinviet.foodies.android.Utils.TabLayoutUtils;
import com.gmail.samehadar.iosdialog.CamomileSpinner;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * Created by Nabeel on 12/14/2017.
 */

public class RestaurantMenuItemsFragment extends Fragment {

    ImageView backIcon, cover_image, about_icon, close_suggestion;
    RelativeLayout about_restaurant_div, review_restaurant_div, suggestion_div, suggestion_txt, rest_open_div;
    CustomExpandableListView expandableListView, restaurant_menu_item_list_suggestion;
    TextView rastaurant_menu_item_title_tv, restaurant_name_tv, salogon_tv, miles_desc_tv;
    CircleImageView restaurant_img;
    SharedPreferences res_menuItemPref;
    RestaurantMenuAdapter restaurantMenuAdapter;
    ArrayList<RestaurantParentModel> listDataHeader;
    ArrayList<RestaurantChildModel> listChildData;
    private ArrayList<ArrayList<RestaurantChildModel>> ListChild;
    CamomileSpinner res_menu_item_progress;
    RatingBar rating;
    SearchView searchView;
    RelativeLayout upper_header;
    LinearLayout about_div;
    String udid, key, userId;
    DatabaseReference mDatabase;
    FirebaseDatabase firebaseDatabase;

    public static boolean FLAG_RES_MENU_FRAG, FLAG_SUGGESTION;
    String delivery_fee_per_km, min_order_price, delivery_free_range, symbol;

    RelativeLayout transparent_layer, progressDialog;
    public static final int PERMISSION_DATA_CART_ADED = 5;
    private int SUGGESTION_ID;
    String restarant_open;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.restaurant_menu_items_fragment, container, false);
        FrameLayout frameLayout = view.findViewById(R.id.resaurant_menu_items_main_layout);
        FontHelper.applyFont(getContext(), frameLayout.getRootView(), AllConstants.verdana);

        frameLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        res_menuItemPref = getContext().getSharedPreferences(PreferenceClass.user, Context.MODE_PRIVATE);
        udid = res_menuItemPref.getString(PreferenceClass.UDID, "");
        initUI(view);

        expandableListView = (CustomExpandableListView) view.findViewById(R.id.restaurant_menu_item_list);
        expandableListView.setExpanded(true);
        expandableListView.setGroupIndicator(null);

        restaurant_menu_item_list_suggestion = (CustomExpandableListView) view.findViewById(R.id.restaurant_menu_item_list_suggestion);
        restaurant_menu_item_list_suggestion.setExpanded(true);
        restaurant_menu_item_list_suggestion.setGroupIndicator(null);

        restaurantMenuDetail();

        // Check If Rest Open


        // end
        return view;
    }

    @SuppressWarnings("deprecation")
    public void initUI(View v) {

        progressDialog = v.findViewById(R.id.progressDialog);
        transparent_layer = v.findViewById(R.id.transparent_layer);

        /// All Suggestion Work
        suggestion_txt = v.findViewById(R.id.suggestion_txt);
        close_suggestion = v.findViewById(R.id.close_suggestion);
        suggestion_div = v.findViewById(R.id.suggestion_div);
        suggestion_div.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FLAG_SUGGESTION = true;
                restaurantMenuDetail();
                close_suggestion.setVisibility(View.VISIBLE);
                suggestion_txt.setVisibility(View.VISIBLE);
                suggestion_div.setVisibility(View.GONE);

                upper_header.setVisibility(View.GONE);
                review_restaurant_div.setVisibility(View.GONE);
                about_restaurant_div.setVisibility(View.GONE);
                about_div.setVisibility(View.GONE);
            }
        });

        close_suggestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FLAG_SUGGESTION = false;
                upper_header.setVisibility(View.VISIBLE);
                review_restaurant_div.setVisibility(View.VISIBLE);
                about_restaurant_div.setVisibility(View.VISIBLE);
                about_div.setVisibility(View.VISIBLE);
                close_suggestion.setVisibility(View.GONE);
                suggestion_txt.setVisibility(View.GONE);
                suggestion_div.setVisibility(View.VISIBLE);
                restaurantMenuDetail();


            }
        });


        // End Suggestion
        miles_desc_tv = v.findViewById(R.id.miles_desc_tv);
        searchView = v.findViewById(R.id.floating_search_view);
        searchView.setQueryHint(Html.fromHtml("<font color = #dddddd>" + "Tìm kiếm Thực đơn" + "</font>"));
        TextView searchText = (TextView)
                v.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        searchText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        searchText.setPadding(0, 0, 0, 0);
        LinearLayout searchEditFrame = (LinearLayout) searchView.findViewById(R.id.search_edit_frame); // Get the Linear Layout
// Get the associated LayoutParams and set leftMargin
        ((LinearLayout.LayoutParams) searchEditFrame.getLayoutParams()).leftMargin = 5;
        search(searchView);

        upper_header = v.findViewById(R.id.upper_header);
        about_div = v.findViewById(R.id.about_div);

        about_icon = v.findViewById(R.id.about_icon);

        res_menu_item_progress = v.findViewById(R.id.res_menu_item_progress);
        res_menu_item_progress.start();
        // All Preference Data
        rating = v.findViewById(R.id.rating);
        rastaurant_menu_item_title_tv = v.findViewById(R.id.rastaurant_menu_item_title_tv);
        restaurant_name_tv = v.findViewById(R.id.restaurant_name_tv);
        salogon_tv = v.findViewById(R.id.salogon_tv);
        restaurant_img = v.findViewById(R.id.restaurant_image);
        cover_image = v.findViewById(R.id.cover_image);

        final String restaurant_name = res_menuItemPref.getString(PreferenceClass.RESTAURANT_NAME, "");
        String restaurant_img_pref = res_menuItemPref.getString(PreferenceClass.RESTAURANT_IMAGE, "");
        String restaurant_slogon = res_menuItemPref.getString(PreferenceClass.RESTAURANT_SALOGON, "");
        delivery_fee_per_km = res_menuItemPref.getString(PreferenceClass.DELIVERY_FEE_PER_KM, "");
        min_order_price = res_menuItemPref.getString(PreferenceClass.MINIMUM_ORDER_PRICE, "");
        delivery_free_range = res_menuItemPref.getString(PreferenceClass.DELIVERY_FEE_RANGE, "");
        symbol = res_menuItemPref.getString(PreferenceClass.RESTAURANT_SYMBOL, "");

        String restaurant_id = res_menuItemPref.getString(PreferenceClass.RESTAURANT_ID, "");
        final String restaurant_about = res_menuItemPref.getString(PreferenceClass.RESTAURANT_ABOUT, "");
        String rating_str = res_menuItemPref.getString(PreferenceClass.RESTAURANT_RATING, "");

        rastaurant_menu_item_title_tv.setText(restaurant_name);
        restaurant_name_tv.setText(restaurant_name);
        salogon_tv.setText(restaurant_slogon);

        if (delivery_free_range.equalsIgnoreCase("0")) {
            miles_desc_tv.setText(delivery_fee_per_km +  "k" + "/km");
        } else {
            miles_desc_tv.setText(delivery_fee_per_km +  "k" + "/km - free ship đơn trên " + min_order_price + "k" + " trong bán kính " + delivery_free_range + " km");
        }
        Picasso.with(getContext()).load(Config.imgBaseURL + restaurant_img_pref).
                fit().centerCrop()
                .placeholder(R.drawable.unknown_img)
                .error(R.drawable.unknown_img).into(restaurant_img);

        /// End//

        about_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialDialog.Builder(getContext())
                        .title(restaurant_name)
                        .content(restaurant_about)
                        .positiveText("OK")
                        .show();
            }
        });

        backIcon = v.findViewById(R.id.back_icon_menu_option);
        backIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (ShowFavoriteRestFragment.FLAG_SHOW_FAV) {
                    ShowFavoriteRestFragment fragmentChild = new ShowFavoriteRestFragment();
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.replace(R.id.resaurant_menu_items_main_layout, fragmentChild);
                    transaction.addToBackStack(null);
                    transaction.commit();
                } else {
                    RestaurantsFragment fragmentChild = new RestaurantsFragment();
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.replace(R.id.resaurant_menu_items_main_layout, fragmentChild);
                    transaction.addToBackStack(null);
                    transaction.commit();
                    FLAG_SUGGESTION = false;
                }

            }
        });

        about_restaurant_div = (RelativeLayout) v.findViewById(R.id.about_restaurant_div);
        about_restaurant_div.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                FLAG_RES_MENU_FRAG = true;
                Fragment reviewListFragment = new RestaurantDealsFragment();
                FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                transaction.replace(R.id.resaurant_menu_items_main_layout, reviewListFragment, "ParentFragment_MenuItems").commit();

            }
        });

        review_restaurant_div = (RelativeLayout) v.findViewById(R.id.review_restaurant_div);
        review_restaurant_div.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment reviewListFragment = new ReviewListFragment();
                FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                transaction.replace(R.id.resaurant_menu_items_main_layout, reviewListFragment, "ParentFragment_MenuItems").commit();
            }
        });


        upper_header.setVisibility(View.VISIBLE);
        review_restaurant_div.setVisibility(View.VISIBLE);
        about_restaurant_div.setVisibility(View.VISIBLE);
        about_div.setVisibility(View.VISIBLE);

        rest_open_div = v.findViewById(R.id.rest_open_div);


    }

    @Override
    public void onResume() {
        super.onResume();
        upper_header.setVisibility(View.VISIBLE);
        review_restaurant_div.setVisibility(View.VISIBLE);
        about_restaurant_div.setVisibility(View.VISIBLE);
        about_div.setVisibility(View.VISIBLE);
    }


    private void search(final android.support.v7.widget.SearchView searchView) {

    /*    searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // FLAG_SUGGESTION = true;
              //  restaurantMenuDetail();
            }
        });
*/

        searchView.setOnQueryTextListener(new android.support.v7.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {


                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                if (newText.equalsIgnoreCase("")) {
                    FLAG_SUGGESTION = false;
                    upper_header.setVisibility(View.VISIBLE);
                    review_restaurant_div.setVisibility(View.VISIBLE);
                    about_restaurant_div.setVisibility(View.VISIBLE);
                    about_div.setVisibility(View.VISIBLE);
                    restaurantMenuDetail();
                } else {
                    if (restaurantMenuAdapter != null)
                        restaurantMenuAdapter.getFilter().filter(newText);
                    upper_header.setVisibility(View.GONE);
                    review_restaurant_div.setVisibility(View.GONE);
                    about_restaurant_div.setVisibility(View.GONE);
                    about_div.setVisibility(View.GONE);
                    close_suggestion.setVisibility(View.GONE);
                    suggestion_txt.setVisibility(View.GONE);

                }
                return true;
            }
        });


        searchView.setOnCloseListener(new SearchView.OnCloseListener() {

            @Override
            public boolean onClose() {

                Log.i("SearchView:", "onClose");
                searchView.onActionViewCollapsed();
                upper_header.setVisibility(View.VISIBLE);
                review_restaurant_div.setVisibility(View.VISIBLE);
                about_restaurant_div.setVisibility(View.VISIBLE);
                about_div.setVisibility(View.VISIBLE);
                for (int m = 0; m < restaurantMenuAdapter.getGroupCount(); m++)
                    expandableListView.expandGroup(m);

                return false;
            }
        });

        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchView.onActionViewExpanded();

            }
        });

        searchView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {

            @Override
            public void onViewDetachedFromWindow(View arg0) {
                // search was detached/closed

                upper_header.setVisibility(View.VISIBLE);
                //   about_div.setVisibility(View.VISIBLE);
            }

            @Override
            public void onViewAttachedToWindow(View arg0) {
                // search was opened

            }
        });
    }


    public void restaurantMenuDetail() {

        if (FLAG_SUGGESTION) {
            expandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
                @Override
                public boolean onGroupClick(ExpandableListView parent, View v,
                                            int groupPosition, long id) {
                    return false; // This way the expander cannot be collapsed
                }
            });
        } else {
            expandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
                @Override
                public boolean onGroupClick(ExpandableListView parent, View v,
                                            int groupPosition, long id) {
                    return true; // This way the expander cannot be collapsed
                }
            });
        }

        TabLayoutUtils.enableTabs(PagerMainActivity.tabLayout, false);
        transparent_layer.setVisibility(View.VISIBLE);
        progressDialog.setVisibility(View.VISIBLE);
        expandableListView.setVisibility(View.VISIBLE);
        listDataHeader = new ArrayList<>();
        ListChild = new ArrayList<>();
        String id = res_menuItemPref.getString(PreferenceClass.RESTAURANT_ID, "");
        RequestQueue queue = Volley.newRequestQueue(getContext());


        Calendar c = Calendar.getInstance();
        System.out.println("Current time => " + c.getTime());

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = df.format(c.getTime());

        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("id", id);
            jsonObject.put("current_time", formattedDate);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("jsonObject", jsonObject.toString());
        Log.d("SHOW_RESTAURANT_MENU", Config.SHOW_RESTAURANT_MENU);
        JsonObjectRequest resMenuItemRequest = new JsonObjectRequest(Request.Method.POST, Config.SHOW_RESTAURANT_MENU, jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {


                String strJson = response.toString();
                JSONObject jsonResponse = null;
                try {
                    jsonResponse = new JSONObject(strJson);

                    Log.d("JSONPostlistmanu", jsonResponse.toString());

                    int code_id = Integer.parseInt(jsonResponse.optString("code"));

                    if (code_id == 200) {

                        JSONObject json = new JSONObject(jsonResponse.toString());
                        JSONArray jsonArray = json.getJSONArray("msg");

                        for (int i = 0; i < jsonArray.length(); i++) {

                            JSONObject allJsonObject1 = jsonArray.getJSONObject(i);
                            JSONObject currency = allJsonObject1.getJSONObject("Currency");
                            String currency_symbol = currency.optString("symbol");
                            JSONObject coverImage = allJsonObject1.getJSONObject("Restaurant");
                            String coverImgURL = coverImage.optString("cover_image");
                            restarant_open = coverImage.optString("open");
                            Picasso.with(getContext()).load(Config.imgBaseURL + coverImgURL).
                                    fit().centerCrop().error(R.drawable.unknown_img)
                                    .into(cover_image);

                            final JSONArray resMenuArray = allJsonObject1.getJSONArray("RestaurantMenu");
                            Log.d("JSONPostlistmenu", "resMenuArray.length=" + resMenuArray.length());
                            for (int j = 0; j < resMenuArray.length(); j++) {

                                JSONObject resMenuAllObj = resMenuArray.getJSONObject(j);

                                RestaurantParentModel restaurantParentModel = new RestaurantParentModel();

                                restaurantParentModel.setTitle(resMenuAllObj.optString("name"));
                                restaurantParentModel.setSub_title(resMenuAllObj.optString("description"));

                                listDataHeader.add(restaurantParentModel);

                                listChildData = new ArrayList<>();

                                JSONArray menuItemArray = resMenuAllObj.getJSONArray("RestaurantMenuItem");
                                Log.d("menuItemArray", menuItemArray.toString());
                                Log.d("Count", String.valueOf(menuItemArray.length()));
                                for (int k = 0; k < menuItemArray.length(); k++) {
                                    JSONObject menuItemArrayObj = menuItemArray.getJSONObject(k);

                                    RestaurantChildModel restaurantChildModel = new RestaurantChildModel();

                                    restaurantChildModel.setChild_title(menuItemArrayObj.optString("name"));
                                    restaurantChildModel.setChild_sub_title(menuItemArrayObj.optString("description"));
                                    restaurantChildModel.setOrder_detail(menuItemArrayObj.optString("out_of_order"));
                                    restaurantChildModel.setPrice(menuItemArrayObj.optString("price"));
                                    restaurantChildModel.setImagePath(menuItemArrayObj.optString("image"));
                                    restaurantChildModel.setRestaurant_menu_item_id(menuItemArrayObj.optString("id"));
                                    restaurantChildModel.setCurrency_symbol(currency_symbol);
                                    listChildData.add(restaurantChildModel);
                                }
                                ListChild.add(listChildData);

                            }
                            TabLayoutUtils.enableTabs(PagerMainActivity.tabLayout, true);
                            transparent_layer.setVisibility(View.GONE);
                            progressDialog.setVisibility(View.GONE);
                            restaurant_menu_item_list_suggestion.setVisibility(View.GONE);
                            restaurantMenuAdapter = new RestaurantMenuAdapter(getContext(), listDataHeader, ListChild);
                            expandableListView.setAdapter(restaurantMenuAdapter);

                            /// Check If Out Of Order


                            expandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
                                @Override
                                public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {

                                    SUGGESTION_ID = groupPosition;
                                    FLAG_SUGGESTION = false;
                                    //getRestaurantSuggestion();

                                    return false;
                                }
                            });


                            if (restarant_open.equalsIgnoreCase("1")) {

                                expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
                                    @Override
                                    public boolean onChildClick(ExpandableListView expandableListView, View view,
                                                                int groupPosition, int childPosition, long l) {
                                        RestaurantChildModel item = (RestaurantChildModel) restaurantMenuAdapter.getChild(groupPosition, childPosition);

                                        //  Toast.makeText(getContext(), item.getChild_title(), Toast.LENGTH_SHORT).show();

                                        if (item.getOrder_detail().equalsIgnoreCase("1")) {
                                        Log.d("item click",item.toString());
                                        } else {
                                            Intent intent = new Intent(getActivity(), AddToCartActivity.class);
                                            intent.putExtra("name", item.getChild_title());
                                            intent.putExtra("desc", item.getChild_sub_title());
                                            intent.putExtra("image", item.getImagePath());
                                            intent.putExtra("price", item.getPrice());
                                            intent.putExtra("extra_id", item.getRestaurant_menu_item_id());
                                            //  intent.putExtra("key",userId);
                                            intent.putExtra("symbol", item.getCurrency_symbol());
                                            //  getContext().startActivity(intent);

                                            getActivity().startActivityForResult(intent, PERMISSION_DATA_CART_ADED);

                                        }

                                        //  handleClick(item);
                                        return false;
                                    }
                                });
                            } else {
                                rest_open_div.setVisibility(View.VISIBLE);
                                about_restaurant_div.setClickable(false);
                                expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
                                    @Override
                                    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                                        return true;
                                    }
                                });

                            }
                        }
                        // End

                        if (FLAG_SUGGESTION) {

                        } else {
                            for (int m = 0; m < restaurantMenuAdapter.getGroupCount(); m++)
                                expandableListView.expandGroup(m);
                        }

                    }

                } catch (Exception e) {
                    Log.d("ERROR:", e.getMessage());
                    e.getMessage();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //  Toast.makeText(getContext(),error.toString(),Toast.LENGTH_LONG).show();

                VolleyLog.d("JSONPost", "Error: " + error.getMessage());
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

        queue.add(resMenuItemRequest);

    }


    public void getRestaurantSuggestion() {
        TabLayoutUtils.enableTabs(PagerMainActivity.tabLayout, false);
        transparent_layer.setVisibility(View.VISIBLE);
        progressDialog.setVisibility(View.VISIBLE);
        restaurant_menu_item_list_suggestion.setVisibility(View.VISIBLE);
        listDataHeader = new ArrayList<>();
        ListChild = new ArrayList<>();
        String id = res_menuItemPref.getString(PreferenceClass.RESTAURANT_ID, "");
        RequestQueue queue = Volley.newRequestQueue(getContext());


        Calendar c = Calendar.getInstance();
        System.out.println("Current time => " + c.getTime());

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = df.format(c.getTime());

        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("id", id);
            jsonObject.put("current_time", formattedDate);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest resMenuItemRequest = new JsonObjectRequest(Request.Method.POST, Config.SHOW_RESTAURANT_MENU, jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {


                String strJson = response.toString();
                JSONObject jsonResponse = null;
                try {
                    jsonResponse = new JSONObject(strJson);
                    int code_id = Integer.parseInt(jsonResponse.optString("code"));

                    if (code_id == 200) {

                        JSONObject json = new JSONObject(jsonResponse.toString());
                        JSONArray jsonArray = json.getJSONArray("msg");

                        for (int i = 0; i < jsonArray.length(); i++) {

                            if (i == SUGGESTION_ID) {

                                JSONObject allJsonObject1 = jsonArray.getJSONObject(SUGGESTION_ID);
                                JSONObject currency = allJsonObject1.getJSONObject("Currency");
                                String currency_symbol = currency.optString("symbol");
                                JSONObject coverImage = allJsonObject1.getJSONObject("Restaurant");
                                String coverImgURL = coverImage.optString("cover_image");
                                Picasso.with(getContext()).load(Config.imgBaseURL + coverImgURL).
                                        fit().centerCrop().error(R.drawable.unknown_img)
                                        .into(cover_image);

                                final JSONArray resMenuArray = allJsonObject1.getJSONArray("RestaurantMenu");

                                for (int j = 0; j < resMenuArray.length(); j++) {

                                    JSONObject resMenuAllObj = resMenuArray.getJSONObject(j);

                                    RestaurantParentModel restaurantParentModel = new RestaurantParentModel();

                                    restaurantParentModel.setTitle(resMenuAllObj.optString("name"));
                                    restaurantParentModel.setSub_title(resMenuAllObj.optString("description"));

                                    listDataHeader.add(restaurantParentModel);

                                    listChildData = new ArrayList<>();

                                    JSONArray menuItemArray = resMenuAllObj.getJSONArray("RestaurantMenuItem");
                                    Log.d("Count", String.valueOf(menuItemArray.length()));
                                    for (int k = 0; k < menuItemArray.length(); k++) {
                                        JSONObject menuItemArrayObj = menuItemArray.getJSONObject(k);

                                        RestaurantChildModel restaurantChildModel = new RestaurantChildModel();

                                        restaurantChildModel.setChild_title(menuItemArrayObj.optString("name"));
                                        restaurantChildModel.setChild_sub_title(menuItemArrayObj.optString("description"));
                                        restaurantChildModel.setOrder_detail(menuItemArrayObj.optString("out_of_order"));
                                        restaurantChildModel.setPrice(menuItemArrayObj.optString("price"));
                                        restaurantChildModel.setImagePath("https://images.foody.vn/res/g73/723818/prof/s640x400/foody-upload-api-foody-mobile-11-190221090633.jpg");
                                        restaurantChildModel.setCurrency_symbol(currency_symbol);

                                        listChildData.add(restaurantChildModel);


                                        JSONArray extra_section = menuItemArrayObj.getJSONArray("RestaurantMenuExtraSection");

                                        for (int n = 0; n < extra_section.length(); n++) {

                                            JSONObject extraItemJsonObject1 = extra_section.getJSONObject(n);

                                            restaurantChildModel.setRestaurant_menu_item_id(extraItemJsonObject1.optString("restaurant_menu_item_id"));

                                        }

                                    }
                                    ListChild.add(listChildData);

                                }
                                TabLayoutUtils.enableTabs(PagerMainActivity.tabLayout, true);
                                transparent_layer.setVisibility(View.GONE);
                                progressDialog.setVisibility(View.GONE);
                                expandableListView.setVisibility(View.GONE);
                                restaurantMenuAdapter = new RestaurantMenuAdapter(getContext(), listDataHeader, ListChild);
                                restaurant_menu_item_list_suggestion.setAdapter(restaurantMenuAdapter);

                                /// Check If Out Of Order

                                restaurant_menu_item_list_suggestion.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
                                    @Override
                                    public boolean onChildClick(ExpandableListView expandableListView, View view,
                                                                int groupPosition, int childPosition, long l) {
                                        RestaurantChildModel item = (RestaurantChildModel) restaurantMenuAdapter.getChild(groupPosition, childPosition);

                                        Toast.makeText(getContext(), item.getChild_title(), Toast.LENGTH_SHORT).show();

                                        if (item.getOrder_detail().equalsIgnoreCase("1")) {

                                        } else {
                                            Intent intent = new Intent(getActivity(), AddToCartActivity.class);
                                            intent.putExtra("name", item.getChild_title());
                                            intent.putExtra("desc", item.getChild_sub_title());
                                            intent.putExtra("image", item.getImagePath());
                                            intent.putExtra("price", item.getPrice());
                                            intent.putExtra("extra_id", item.getRestaurant_menu_item_id());
                                            //  intent.putExtra("key",userId);
                                            intent.putExtra("symbol", item.getCurrency_symbol());
                                            //  getContext().startActivity(intent);

                                            getActivity().startActivityForResult(intent, PERMISSION_DATA_CART_ADED);


                                        }

                                        //  handleClick(item);
                                        return false;
                                    }
                                });
                            }
                            // End

                            if (FLAG_SUGGESTION) {

                            } else {
                                for (int m = 0; m < restaurantMenuAdapter.getGroupCount(); m++)
                                    restaurant_menu_item_list_suggestion.expandGroup(m);
                            }

                        }

                    }
                } catch (Exception e) {
                    e.getMessage();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //  Toast.makeText(getContext(),error.toString(),Toast.LENGTH_LONG).show();

                VolleyLog.d("JSONPost", "Error: " + error.getMessage());
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

        queue.add(resMenuItemRequest);

    }


}
