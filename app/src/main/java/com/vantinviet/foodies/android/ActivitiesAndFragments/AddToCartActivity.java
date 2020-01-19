package com.vantinviet.foodies.android.ActivitiesAndFragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ExpandableListView;
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
import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.Indicators.PagerIndicator;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.daimajia.slider.library.Tricks.ViewPagerEx;
import com.vantinviet.foodies.android.Adapters.AddToCartExpandable;
import com.vantinviet.foodies.android.Constants.AllConstants;
import com.vantinviet.foodies.android.Constants.Config;
import com.vantinviet.foodies.android.Constants.PreferenceClass;
import com.vantinviet.foodies.android.Models.CalculationModel;
import com.vantinviet.foodies.android.Models.CartChildModel;
import com.vantinviet.foodies.android.Models.CartParentModel;

import com.vantinviet.foodies.android.R;
import com.vantinviet.foodies.android.Utils.CustomExpandableListView;
import com.vantinviet.foodies.android.Utils.FontHelper;
import com.gmail.samehadar.iosdialog.CamomileSpinner;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static com.vantinviet.foodies.android.ActivitiesAndFragments.CartFragment.UPDATE_NODE;


/**
 * Created by Nabeel on 12/18/2017.
 */

public class AddToCartActivity extends AppCompatActivity implements  BaseSliderView.OnSliderClickListener, ViewPagerEx.OnPageChangeListener {

    ImageView clos_menu_items_detail;
    Button increament_btn, decrement_btn;
    TextView inc_dec_tv, grand_total_price_tv, total_price_tv, desc_tv, name_tv, cart_title_tv;
    int present_count = 1;
    SharedPreferences sPref;

    AddToCartExpandable listAdapter;
    CustomExpandableListView cartExpandableListView;
    ArrayList<CartParentModel> listDataHeader;
    ArrayList<CartChildModel> listChildData;
    private ArrayList<ArrayList<CartChildModel>> ListChild;
    //  HashMap<MenuItemModel,ArrayList<String>> listDataChild;
    String restaurant_menu_item_id, restaurant_id;
    DatabaseReference mDatabase;
    private static FirebaseDatabase firebaseDatabase;
    private static String userId, udid, key_, name_, desc, price_, symbol, res_id, res_tax, res_fee;
    Double menuExtraItemObj, itemPrice, previousMenuObjPrice;
    int required = 0;
    Double totalExtraItemPrice, grandTotal;

    int randomNum, count;

    ArrayList<HashMap<String, String>> extraItem;

    ArrayList<Integer> arrayList = new ArrayList<>();

    public static boolean FLAG_ONCE_LOOP_ADD, FLAG_CART_ADD, FIRST_TIME_LOADER, WAS_IN_BG, IS_APP_OPEN;
    String previousCheck;
    RelativeLayout add_to_cart;
    EditText inst_text;
    public static TextView tab_badge, cart_btn_text;
    static boolean calledAlready = false;
    String prev_rest_id;
    CamomileSpinner progress;
    RelativeLayout transparent_layer, progressDialog;
    int showcartCount = 0;
    String min_order_price, descFinal;
    private static boolean UPDATE_CONFIRM;
    private SliderLayout mDemoSlider;
    RequestQueue queue;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        firebaseDatabase = FirebaseDatabase.getInstance();
        setContentView(R.layout.activity_add_to_cart);

        View view = LayoutInflater.from(AddToCartActivity.this).inflate(R.layout.custom_tab, null);
        tab_badge = view.findViewById(R.id.tab_badge);

        progressDialog = findViewById(R.id.progressDialog);
        transparent_layer = findViewById(R.id.transparent_layer);

        progress = findViewById(R.id.addToCartProgress);
        progress.start();
        sPref = getSharedPreferences(PreferenceClass.user, Context.MODE_PRIVATE);
        udid = sPref.getString(PreferenceClass.UDID, "");
        restaurant_menu_item_id = getIntent().getStringExtra("extra_id");
        if (restaurant_menu_item_id == null) {
            restaurant_menu_item_id="0";
            transparent_layer.setVisibility(View.GONE);
            progressDialog.setVisibility(View.GONE);

        }

        transparent_layer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });

        if (UPDATE_NODE) {
            key_ = getIntent().getStringExtra("key");
        }
        name_ = getIntent().getStringExtra("name");
        desc = getIntent().getStringExtra("desc");
        price_ = getIntent().getStringExtra("price");
        symbol = getIntent().getStringExtra("symbol");
        res_id = sPref.getString(PreferenceClass.RESTAURANT_ID, "");
        res_tax = sPref.getString(PreferenceClass.RESTAURANT_ITEM_TAX, "");
        min_order_price = sPref.getString(PreferenceClass.MINIMUM_ORDER_PRICE, "");
        res_fee = "200";
        inst_text = findViewById(R.id.inst_text);
        Random r = new Random();
        randomNum = r.nextInt(1000 - 65) + 65;

        extraItem = new ArrayList<>();


        RelativeLayout main_add_to_cart = findViewById(R.id.main_add_to_cart);
        FontHelper.applyFont(getApplicationContext(), main_add_to_cart, AllConstants.verdana);
        mDatabase = firebaseDatabase.getReference().child(AllConstants.CALCULATION).child(udid);
        mDatabase.keepSynced(true);
        FLAG_CART_ADD = false;

        Query query = mDatabase;
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // mDatabase.setValue(null);

                    if (UPDATE_NODE) {
                        mDatabase.child(key_).setValue(new CalculationModel(userId, restaurant_menu_item_id,
                                name_, price_, "", "1", "0", min_order_price,
                                extraItem, inst_text.getText().toString(), res_id, symbol, desc, res_fee, res_tax));
                    } else {
                        userId = mDatabase.push().getKey();
                        mDatabase.child(userId).setValue(new CalculationModel(userId, restaurant_menu_item_id,
                                name_, price_, "", "1", "0", min_order_price,
                                extraItem, inst_text.getText().toString(), res_id, symbol, desc, res_fee, res_tax));
                    }
                } else {

                    if (UPDATE_NODE) {
                        mDatabase.child(key_).setValue(new CalculationModel(userId, restaurant_menu_item_id,
                                name_, price_, "", "1", "0", min_order_price,
                                extraItem, inst_text.getText().toString(), res_id, symbol, desc, res_fee, res_tax));
                    } else {
                        userId = mDatabase.push().getKey();
                        VolleyLog.d("restaurant_menu_item_id %s",restaurant_menu_item_id);
                        CalculationModel calculationModel=new CalculationModel(userId, restaurant_menu_item_id,
                                name_, price_, "", "1", "0", min_order_price, extraItem, inst_text.getText().toString(), res_id, symbol, desc, res_fee, res_tax);

                        VolleyLog.d("CALCULATIONMODEL %s",calculationModel.toString());
                        VolleyLog.d("userId %s",userId);
                        VolleyLog.d("extraItem %s",extraItem);
                        mDatabase.child(userId).setValue(calculationModel);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        cartExpandableListView = (CustomExpandableListView) findViewById(R.id.item_detail_list);
        cartExpandableListView.setExpanded(true);
        cartExpandableListView.setGroupIndicator(null);

        cartExpandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v,
                                        int groupPosition, long id) {
                return true; // This way the expander cannot be collapsed
            }
        });

        init();
        increamentDecFunc();


    }


    public void init() {
        cart_btn_text = findViewById(R.id.cart_btn_text);
        if (UPDATE_NODE) {
            cart_btn_text.setText("Cập nhật giỏ hàng");

        }
        add_to_cart = findViewById(R.id.add_to_cart);
        increament_btn = findViewById(R.id.plus_btn);
        decrement_btn = findViewById(R.id.minus_btn);
        inc_dec_tv = findViewById(R.id.inc_dec_tv);
        clos_menu_items_detail = findViewById(R.id.clos_menu_items_detail);
        grand_total_price_tv = findViewById(R.id.grand_total_price_tv);
        total_price_tv = findViewById(R.id.total_price_tv);
        desc_tv = findViewById(R.id.desc_tv);
        name_tv = findViewById(R.id.name_tv);

        cart_title_tv = findViewById(R.id.cart_title_tv);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadCalculationDetail();
                //  progress.setVisibility(View.GONE);
            }
        }, 500);

        clos_menu_items_detail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //   startActivity(new Intent(AddToCartActivity.this,MainActivity.class));

                IS_APP_OPEN = true;

                if (UPDATE_CONFIRM) {
                    UPDATE_CONFIRM = false;
                } else {
                    if (UPDATE_NODE) {
                        UPDATE_NODE = false;
                    } else {
                        deleteCurrentNode();
                    }
                }
                RestaurantMenuItemsFragment.FLAG_SUGGESTION = false;
                finish();

            }
        });


        descFinal = desc.replaceAll("&amp;", "&");


        name_tv.setText(name_);

        desc_tv.setText(descFinal);
        int tien = Integer.parseInt(price_);
        String pattern = "###,###,###.###";
        DecimalFormat decimalFormat = new DecimalFormat(pattern);

        String format = decimalFormat.format(tien);
        total_price_tv.setText(format + " " + symbol);
        cart_title_tv.setText(name_);
        getOrderDetailItems();

        add_to_cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (count == required) {

                    Query lastQuery = mDatabase.orderByKey().limitToFirst(1);
                    lastQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            @SuppressWarnings("unchecked")
                            Map<String, Object> td = (HashMap<String, Object>) dataSnapshot.getValue();

                            Collection<Object> values = td.values();

                            JSONArray jsonArray = new JSONArray(values);

                            int size = jsonArray.length();

                            for (int a = 0; a < 1; a++) {

                                JSONObject allJsonObject = null;
                                try {
                                    allJsonObject = jsonArray.getJSONObject(0);
                                    prev_rest_id = String.valueOf(allJsonObject.optString("restID"));

                                    if (!prev_rest_id.equalsIgnoreCase(res_id)) {
                                        showDialogIfChangeRest();

                                    } else {
                                        Toast.makeText(AddToCartActivity.this, "Thêm vào giỏ hàng thành công", Toast.LENGTH_SHORT).show();
                                        //   sendBroadcast(new Intent("AddToCart"));
                                        if (UPDATE_NODE) {
                                            mDatabase.child(key_).setValue(new CalculationModel(key_, restaurant_menu_item_id,
                                                    name_, price_, String.valueOf(grandTotal), inc_dec_tv.getText().toString(), "0",
                                                    min_order_price, extraItem, inst_text.getText().toString(), res_id, symbol, desc, res_fee, res_tax));
                                            AddPaymentFragment.FLAG_ADD_PAYMENT = false;
                                            AddressListFragment.FLAG_ADDRESS_LIST = false;
                                            UPDATE_NODE = false;
                                            UPDATE_CONFIRM = true;
                                        } else {

                                            mDatabase.child(userId).setValue(new CalculationModel(userId, restaurant_menu_item_id,
                                                    name_, price_, String.valueOf(grandTotal), inc_dec_tv.getText().toString(), "0",
                                                    min_order_price, extraItem, inst_text.getText().toString(), res_id, symbol, desc, res_fee, res_tax));
                                            AddPaymentFragment.FLAG_ADD_PAYMENT = false;
                                            AddressListFragment.FLAG_ADDRESS_LIST = false;
                                            SharedPreferences.Editor editor = sPref.edit();
                                            count = sPref.getInt("count", 0);
                                            showcartCount = count + 1;
                                            editor.putInt("count", showcartCount);
                                            editor.putInt(PreferenceClass.CART_COUNT, 1).commit();
                                            FLAG_CART_ADD = true;
                                            Intent data = new Intent();
                                            setResult(RESULT_OK, data);
                                        }
                                        finish();
                                    }


                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                           /* for(DataSnapshot dataSnapshot1:dataSnapshot.getChildren()) {

                                if(dataSnapshot1.getValue()!=null) {
                                    */
                            //  prev_rest_id = String.valueOf(dataSnapshot1.child("restID").getValue());


                               /* }
                                else {
                                    mDatabase.child(userId).setValue(new CalculationModel(userId, restaurant_menu_item_id,
                                            name_, price_, String.valueOf(grandTotal), inc_dec_tv.getText().toString(), "0", extraItem, inst_text.getText().toString(), res_id, symbol, desc, res_fee, res_tax));
                                    AddPaymentFragment.FLAG_ADD_PAYMENT = false;
                                    AddressListFragment.FLAG_ADDRESS_LIST = false;
                                    FLAG_CART_ADD = true;
                                    finish();
                                }*/
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    //  tab_badge.setVisibility(View.VISIBLE);

                } else {
                    Toast.makeText(AddToCartActivity.this, "Chọn tất cả các mục cần thiết", Toast.LENGTH_SHORT).show();
                }
                // Toast.makeText(AddToCartActivity.this, "Thêm vào giỏ hàng thành công", Toast.LENGTH_SHORT).show();
            }
        });
        //sideshow
        mDemoSlider = (SliderLayout)findViewById(R.id.slider);
        JSONObject slideshowImages = new JSONObject();
        try {
            if (restaurant_menu_item_id != null) {
                slideshowImages.put("restaurant_menu_item_id", restaurant_menu_item_id);
            } else {
                transparent_layer.setVisibility(View.GONE);
                progressDialog.setVisibility(View.GONE);
            }
            slideshowImages.put("restaurant_id", restaurant_id);
        } catch (JSONException e) {
            e.printStackTrace();
            progressDialog.setVisibility(View.GONE);
            Toast.makeText(AddToCartActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
        }

        Log.d("slideshowImages", slideshowImages.toString());
        Log.d("LINK_SHOW_MENU_ITEM", Config.SHOW_IMAGE_MENU_EXTRA_ITEM);
        queue = Volley.newRequestQueue(AddToCartActivity.this);
        JsonObjectRequest orderJsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Config.SHOW_IMAGE_MENU_EXTRA_ITEM, slideshowImages, new Response.Listener<JSONObject>() {
            @SuppressLint("LongLogTag")
            @Override
            public void onResponse(JSONObject response) {
                Log.d("response 1","1");
                String strJson = response.toString();
                JSONObject jsonResponse = null;
                try {
                    jsonResponse = new JSONObject(strJson);

                    Log.d("JSONPOST_IMAGE_EXTRA_MENU", jsonResponse.toString());

                    int code_id = Integer.parseInt(jsonResponse.optString("code"));
                    Log.d("CODE_ID",String.valueOf(code_id));
                    if (code_id == 200) {

                        JSONObject json = new JSONObject(jsonResponse.toString());
                        JSONArray jsonArray = json.getJSONArray("msg");
                        HashMap<String,String> url_maps = new HashMap<String, String>();
                        for (int i = 0; i < jsonArray.length(); i++) {

                            JSONObject item = jsonArray.getJSONObject(i);
                            url_maps.put("", Config.imgBaseURL+"/"+item.get("image").toString());


                        }
                        Log.d("url_maps",url_maps.toString());
                        transparent_layer.setVisibility(View.GONE);
                        progressDialog.setVisibility(View.GONE);
                        for(String name : url_maps.keySet()){
                            TextSliderView textSliderView = new TextSliderView(getBaseContext());
                            // initialize a SliderLayout
                            Log.d("slide_image",url_maps.get(name));
                            textSliderView
                                    .description(name)
                                    .image(url_maps.get(name))
                                    .setScaleType(BaseSliderView.ScaleType.Fit)
                                    ;

                            //add your extra information
                            textSliderView.bundle(new Bundle());
                            textSliderView.getBundle()
                                    .putString("extra",name);

                            mDemoSlider.addSlider(textSliderView);
                            mDemoSlider.setPresetTransformer(SliderLayout.Transformer.Accordion);
                            mDemoSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
                            mDemoSlider.setCustomAnimation(new DescriptionAnimation());
                            mDemoSlider.setDuration(4000);

                        }
                    } else {
                        //  progress.setVisibility(View.GONE);
                    }

                } catch (Exception e) {
                    e.getMessage();
                    transparent_layer.setVisibility(View.GONE);
                    progressDialog.setVisibility(View.GONE);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                transparent_layer.setVisibility(View.GONE);
                progressDialog.setVisibility(View.GONE);
                VolleyLog.d("JSONPost", "Error: " + error.getMessage());
                // Toast.makeText(AddToCartActivity.this,error.toString(),Toast.LENGTH_SHORT).show();
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

        queue.add(orderJsonObjectRequest);





    }

    @Override
    public void onBackPressed() {

        IS_APP_OPEN = true;

        if (UPDATE_CONFIRM) {
            UPDATE_CONFIRM = false;
        } else {
            deleteCurrentNode();
        }
        UPDATE_NODE = false;
        RestaurantMenuItemsFragment.FLAG_SUGGESTION = false;
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
      /*  mDatabase = firebaseDatabase.getReference().child("Calculation").child(udid);
        mDatabase.keepSynced(true);
        FLAG_CART_ADD = false;*/

        //  Toast.makeText(getApplicationContext(),"onResume",Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
       /* mDatabase = firebaseDatabase.getReference().child("Calculation").child(udid);
        mDatabase.keepSynced(true);
        FLAG_CART_ADD = false;*/
        //  Toast.makeText(getApplicationContext(),"onStart",Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onRestart() {
        super.onRestart();

        if (WAS_IN_BG) {
            mDatabase = firebaseDatabase.getReference().child(AllConstants.CALCULATION).child(udid);
            mDatabase.keepSynced(true);
            FLAG_CART_ADD = false;
            WAS_IN_BG = false;
            Query query = mDatabase;
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // mDatabase.setValue(null);

                        if (UPDATE_NODE) {
                            mDatabase.child(key_).setValue(new CalculationModel(key_, restaurant_menu_item_id,
                                    name_, price_, "", "1", "0", min_order_price,
                                    extraItem, inst_text.getText().toString(), res_id, symbol, desc, res_fee, res_tax));
                        } else {

                            mDatabase.child(userId).setValue(new CalculationModel(userId, restaurant_menu_item_id,
                                    name_, price_, "", "1", "0", min_order_price,
                                    extraItem, inst_text.getText().toString(), res_id, symbol, desc, res_fee, res_tax));
                        }
                    } else {
                        if (UPDATE_NODE) {
                            mDatabase.child(key_).setValue(new CalculationModel(key_, restaurant_menu_item_id,
                                    name_, price_, "", "1", "0", min_order_price,
                                    extraItem, inst_text.getText().toString(), res_id, symbol, desc, res_fee, res_tax));
                        } else {

                            mDatabase.child(userId).setValue(new CalculationModel(userId, restaurant_menu_item_id,
                                    name_, price_, "", "1", "0",
                                    min_order_price, extraItem, inst_text.getText().toString(), res_id, symbol, desc, res_fee, res_tax));
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }


        //   Toast.makeText(getApplicationContext(),"onRestart",Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onPause() {
        super.onPause();

        //  Toast.makeText(getApplicationContext(),"OnPause",Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onStop() {
        super.onStop();

        if (!FLAG_CART_ADD) {
            if (IS_APP_OPEN) {
                IS_APP_OPEN = false;
            } else {
                if (UPDATE_CONFIRM) {
                    UPDATE_CONFIRM = false;
                } else {
                    deleteCurrentNode();
                }
                WAS_IN_BG = true;
            }
            //   Toast.makeText(getApplicationContext(),"OnPause",Toast.LENGTH_SHORT).show();
        } else {

        }

        // Toast.makeText(getApplicationContext(),"OnStop",Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Toast.makeText(getApplicationContext(),"OnDestroy",Toast.LENGTH_SHORT).show();
    }

    public void increamentDecFunc() {

        increament_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    String presentValStr = inc_dec_tv.getText().toString();
                    present_count = Integer.parseInt(presentValStr);
                    present_count++;
                    inc_dec_tv.setText(String.valueOf(present_count));
                    SharedPreferences.Editor editor = sPref.edit();
                    editor.putInt(PreferenceClass.DEALS_QUANTITY, present_count).commit();

                    if (UPDATE_NODE) {
                        mDatabase.child(key_).setValue(new CalculationModel(key_, restaurant_menu_item_id,
                                name_, price_, "", inc_dec_tv.getText().toString(), "0", min_order_price,
                                extraItem, inst_text.getText().toString(), res_id, symbol, desc, res_fee, res_tax));
                    } else {
                        mDatabase.child(userId).setValue(new CalculationModel(userId, restaurant_menu_item_id,
                                name_, price_, "", inc_dec_tv.getText().toString(), "0", min_order_price, extraItem, inst_text.getText().toString(), res_id, symbol, desc, res_fee, res_tax));
                    }
                    loadCalculationDetail();
                } catch (Exception e) {
                    e.printStackTrace();
                    // Toast.makeText(getApplicationContext(),"Some error :(",Toast.LENGTH_LONG).show();
                }
            }
        });

        decrement_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    String presentValStr = inc_dec_tv.getText().toString();
                    present_count = Integer.parseInt(presentValStr);
                    if (presentValStr.equalsIgnoreCase(String.valueOf(Integer.parseInt("1")))) {
                        //  Toast.makeText(getContext(),"Can not Less than 1",Toast.LENGTH_LONG).show();
                    } else {
                        present_count--;
                    }
                    inc_dec_tv.setText(String.valueOf(present_count));
                    SharedPreferences.Editor editor = sPref.edit();
                    editor.putInt(PreferenceClass.DEALS_QUANTITY, present_count).commit();
                    if (UPDATE_NODE) {
                        mDatabase.child(key_).setValue(new CalculationModel(key_, restaurant_menu_item_id,
                                name_, price_, "", inc_dec_tv.getText().toString(), "0", min_order_price,
                                extraItem, inst_text.getText().toString(), res_id, symbol, desc, res_fee, res_tax));
                    } else {
                        mDatabase.child(userId).setValue(new CalculationModel(userId, restaurant_menu_item_id,
                                name_, price_, "", inc_dec_tv.getText().toString(), "0", min_order_price, extraItem, inst_text.getText().toString(), res_id, symbol, desc, res_fee, res_tax));
                    }
                    loadCalculationDetail();

                } catch (Exception e) {
                    e.printStackTrace();
                    //  Toast.makeText(getApplicationContext(),"Some error :(",Toast.LENGTH_LONG).show();
                }
            }
        });

    }
    @Override
    public void onSliderClick(BaseSliderView slider) {
        Toast.makeText(this,slider.getBundle().get("extra") + "",Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

    @Override
    public void onPageSelected(int position) {
        Log.d("Slider Demo", "Page Changed: " + position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {}
    public void getOrderDetailItems() {
        //  restaurant_menu_item_id = sPref.getString(PreferenceClass.RESTAURANT_MENU_ITEM_ID,"");
        restaurant_id = sPref.getString(PreferenceClass.RESTAURANT_ID, "");
        listDataHeader = new ArrayList<CartParentModel>();
        ListChild = new ArrayList<>();
        transparent_layer.setVisibility(View.VISIBLE);
        progressDialog.setVisibility(View.VISIBLE);
        //   listDataChild = new HashMap<MenuItemModel, ArrayList<String>>();
        queue = Volley.newRequestQueue(AddToCartActivity.this);

        JSONObject orderJsonObject = new JSONObject();
        try {
            if (restaurant_menu_item_id != null) {
                orderJsonObject.put("restaurant_menu_item_id", restaurant_menu_item_id);
            } else {
                transparent_layer.setVisibility(View.GONE);
                progressDialog.setVisibility(View.GONE);
            }
            orderJsonObject.put("restaurant_id", restaurant_id);
        } catch (JSONException e) {
            e.printStackTrace();
            progressDialog.setVisibility(View.GONE);
            Toast.makeText(AddToCartActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
        }

        Log.d("orderJsonObjectItem", orderJsonObject.toString());
        Log.d("LINK_SHOW_MENU_ITEM", Config.SHOW_MENU_EXTRA_ITEM);
        JsonObjectRequest orderJsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Config.SHOW_MENU_EXTRA_ITEM, orderJsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                String strJson = response.toString();
                JSONObject jsonResponse = null;
                try {
                    jsonResponse = new JSONObject(strJson);

                    Log.d("jsonPost Extra menu", jsonResponse.toString());

                    int code_id = Integer.parseInt(jsonResponse.optString("code"));
                    count = Integer.parseInt(jsonResponse.optString("count"));

                    if (code_id == 200) {

                        JSONObject json = new JSONObject(jsonResponse.toString());
                        JSONArray jsonArray = json.getJSONArray("msg");

                        for (int i = 0; i < jsonArray.length(); i++) {

                            JSONObject allJsonObject = jsonArray.getJSONObject(i);
                            JSONObject restaurantMenuExtraSection = allJsonObject.getJSONObject("RestaurantMenuExtraSection");
                            Log.d("RestaurantMenuExtra", restaurantMenuExtraSection.toString());
                            JSONArray restaurantMenuExtraItem = restaurantMenuExtraSection.getJSONArray("RestaurantMenuExtraItem");
                            Log.d("RestaurantExtraItem", restaurantMenuExtraItem.toString());
                            JSONObject restaurant = allJsonObject.getJSONObject("Restaurant");
                            Log.d("Restaurant", restaurant.toString());
                            JSONObject currency = restaurant.getJSONObject("Currency");
                            Log.d("Currency", currency.toString());
                           // JSONObject tax = restaurant.getJSONObject("Tax");
                            String symbol = currency.optString("symbol");
                            Log.d("symbol", symbol);
                            CartParentModel menuItemModel = new CartParentModel();

                            menuItemModel.setParentName(restaurantMenuExtraSection.optString("name"));
                            menuItemModel.setRequired(restaurantMenuExtraSection.optString("required"));
                            menuItemModel.setSymbol(symbol);

                            listDataHeader.add(menuItemModel);
                            listChildData = new ArrayList<>();
                            //// End

                            for (int j = 0; j < restaurantMenuExtraItem.length(); j++) {

                                JSONObject alljsonJsonObject2 = restaurantMenuExtraItem.getJSONObject(j);

                                CartChildModel menuItemExtraModel = new CartChildModel();

                                menuItemExtraModel.setChild_item_name(alljsonJsonObject2.optString("name"));
                                menuItemExtraModel.setChild_item_price(alljsonJsonObject2.optString("price"));
                                menuItemExtraModel.setExtra_item_id(alljsonJsonObject2.optString("id"));
                                menuItemExtraModel.setPos(j);
                                menuItemExtraModel.setSymbol(symbol);

                                listChildData.add(menuItemExtraModel);

                            }

                            ListChild.add(listChildData);
                            Log.d("ListChild", ListChild.toString());
                            transparent_layer.setVisibility(View.GONE);
                            progressDialog.setVisibility(View.GONE);
                            listAdapter = new AddToCartExpandable(getApplicationContext(), listDataHeader, ListChild);

                            cartExpandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
                                @Override
                                public boolean onChildClick(ExpandableListView expandableListView, View view, int groupPosition, int childPosition, long l) {

                                    final CartChildModel item = (CartChildModel) listAdapter.getChild(groupPosition, childPosition);

                                    boolean iteIsRequired = item.isCheckRequired();

                                    if (!iteIsRequired) {

                                        CheckBox checkBox = view.findViewById(R.id.check_btn);

                                        if (checkBox != null) {
                                            //  checkBox.toggle();

                                            if (!checkBox.isChecked()) {
                                                //  item.setCheckBoxIsChecked(true);
                                                checkBox.setChecked(true);

                                                FLAG_ONCE_LOOP_ADD = true;
                                                addNewNode(item.getExtra_item_id(), item.getChild_item_name(), item.getChild_item_price());
                                                loadCalculationDetail();
                                            } else if (checkBox.isChecked()) {
                                                //   item.setCheckBoxIsChecked(false);
                                                checkBox.setChecked(false);
                                                FLAG_ONCE_LOOP_ADD = false;
                                                deleteNewNode(item.getExtra_item_id());
                                                //   loadCalculationDetail();

                                            }
                                        }
                                    } else {


                                        if (!item.isCheckedddd()) {

                                            String string = arrayList.toString();
                                            ArrayList<CartChildModel> childsList = listAdapter.getChilderns(groupPosition);
                                            for (CartChildModel model : childsList) {
                                                if (model.isCheckedddd()) {
                                                    previousCheck = model.getExtra_item_id();
                                                    break;
                                                }
                                            }

                                            if (arrayList.contains(groupPosition)) {
                                                //FLAG_CHECKBOX_TOGGLER = true;
                                                deleteNewNode(previousCheck);
                                                addNewNode(item.getExtra_item_id(), item.getChild_item_name(), item.getChild_item_price());
                                                previousCheck = item.getExtra_item_id();


                                            } else {
                                                //FLAG_CHECKBOX_TOGGLER = false;
                                                addNewNode(item.getExtra_item_id(), item.getChild_item_name(), item.getChild_item_price());
                                                previousCheck = item.getExtra_item_id();
                                                loadCalculationDetail();
                                                required = required + 1;
                                                // previousCheck = String.valueOf(previousValArray.get(groupPosition));
                                            }

                                            if (!arrayList.contains(groupPosition)) {
                                                arrayList.add(groupPosition);
                                                //  arrayPos.add(groupPosition,previousCheck);
                                            }

                                            upDateNotify(listAdapter.getChilderns(groupPosition));
                                            item.setCheckeddd(true);
                                            listAdapter.notifyDataSetChanged();
                                        }


                                    }
                                    return false;
                                }
                            });

                        }

                        // setting list adapter
                        cartExpandableListView.setAdapter(listAdapter);
                        if (listAdapter.getGroupCount() == 0) {
                            transparent_layer.setVisibility(View.GONE);
                            progressDialog.setVisibility(View.GONE);
                        }
                        for (int l = 0; l < listAdapter.getGroupCount(); l++)
                            cartExpandableListView.expandGroup(l);
                    } else {
                        //  progress.setVisibility(View.GONE);
                    }

                } catch (Exception e) {
                    e.getMessage();
                    transparent_layer.setVisibility(View.GONE);
                    progressDialog.setVisibility(View.GONE);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                transparent_layer.setVisibility(View.GONE);
                progressDialog.setVisibility(View.GONE);
                VolleyLog.d("JSONPost", "Error: " + error.getMessage());
                // Toast.makeText(AddToCartActivity.this,error.toString(),Toast.LENGTH_SHORT).show();
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

        queue.add(orderJsonObjectRequest);

    }

    public void upDateNotify(ArrayList<CartChildModel> child) {
        for (int i = 0; i < child.size(); i++) {
            child.get(i).setCheckeddd(false);
        }
    }

    public void addNewNode(String id, String name, String price) {
        Log.d("hello", "123");
        HashMap<String, String> names = new HashMap<>();
        names.put("menu_extra_item_id", id);
        names.put("menu_extra_item_name", name);
        names.put("menu_extra_item_price", price);
        names.put("menu_extra_item_quantity", inc_dec_tv.getText().toString());
        extraItem.add(names);
        if (UPDATE_NODE) {
            mDatabase.child(key_).setValue(new CalculationModel(key_, restaurant_menu_item_id,
                    name_, price_, "", "1", "0", min_order_price,
                    extraItem, inst_text.getText().toString(), res_id, symbol, desc, res_fee, res_tax));
        } else {
            mDatabase.child(userId).setValue(new CalculationModel(userId, restaurant_menu_item_id,
                    name_, price_, "", inc_dec_tv.getText().toString(), "0",
                    min_order_price, extraItem, inst_text.getText().toString(), res_id, symbol, desc, res_fee, res_tax));
        }
         loadCalculationDetail();
    }

    public void deleteNewNode(final String id) {

        DatabaseReference query = mDatabase;//mDatabase;//FirebaseDatabase.getInstance().getReference();
        Query lastQuery = query.orderByKey().limitToLast(1);
        lastQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //child is each element in the finished list
                @SuppressWarnings("unchecked")
                Map<String, Object> td = (HashMap<String, Object>) dataSnapshot.getValue();

                Collection<Object> values = td.values();
                String string = values.toString();

                JSONArray jsonArray = null;
                try {
                    jsonArray = new JSONArray(values);

                    for (int a = 0; a < jsonArray.length(); a++) {

                        JSONObject allJsonObject = jsonArray.getJSONObject(a);
                        JSONArray extraItemArray = allJsonObject.getJSONArray("extraItem");
                        Log.d("extraItem", "hello124");
                        for (int b = 0; b < extraItemArray.length(); b++) {

                            JSONObject jsonObject = extraItemArray.getJSONObject(b);
                            String menuExtraItemObj = jsonObject.optString("menu_extra_item_id");

                            if (menuExtraItemObj.equalsIgnoreCase(id)) {

                                //  int some = i;
                                try {
                                    extraItem.remove(b);
                                    if (UPDATE_NODE) {
                                        mDatabase.child(key_).setValue(new CalculationModel(key_, restaurant_menu_item_id,
                                                name_, price_, "", "1", "0", min_order_price,
                                                extraItem, inst_text.getText().toString(), res_id, symbol, desc, res_fee, res_tax));
                                    } else {
                                        mDatabase.child(userId).setValue(new CalculationModel(userId, restaurant_menu_item_id,
                                                name_, price_, "", inc_dec_tv.getText().toString(), "0",
                                                min_order_price, extraItem, inst_text.getText().toString(), res_id, symbol, desc, res_fee, res_tax));
                                    } // loadCalculationDetail();
                                } catch (IndexOutOfBoundsException e) {
                                    e.getMessage();
                                }

                            }

                        }
                        loadCalculationDetail();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void loadCalculationDetail() {
        if (FIRST_TIME_LOADER) {
            transparent_layer.setVisibility(View.VISIBLE);
            progressDialog.setVisibility(View.VISIBLE);
            FIRST_TIME_LOADER = false;
        }

        DatabaseReference query = mDatabase;//mDatabase;//FirebaseDatabase.getInstance().getReference();
        Query lastQuery = query.orderByKey().limitToLast(1);
        lastQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //child is each element in the finished list
                @SuppressWarnings("unchecked")
                Map<String, Object> td = (HashMap<String, Object>) dataSnapshot.getValue();

                if (td != null) {
                    Collection<Object> value = td.values();
                    String string = value.toString();
                    Log.d("AAAAstring", string);
                    JSONArray jsonArray = null;
                    try {
                        jsonArray = new JSONArray(value);
                        grandTotal = 0.0;
                        totalExtraItemPrice = 0.0;
                        menuExtraItemObj = 0.0;
                        itemPrice = 0.0;
                        for (int a = 0; a < jsonArray.length(); a++) {

                            JSONObject allJsonObject = jsonArray.getJSONObject(a);
                            itemPrice = Double.parseDouble(allJsonObject.optString("mPrice"));
                            //  itemPr = itemPrice;

                            grandTotal = totalExtraItemPrice + itemPrice * Double.valueOf(inc_dec_tv.getText().toString());
                            // String str = grandTotal;


                            int gia = Integer.valueOf(grandTotal.intValue());
                            String pattern = "###,###,###.###";
                            DecimalFormat decimalFormat = new DecimalFormat(pattern);

                            String format1 = decimalFormat.format(gia);
                            grand_total_price_tv.setText(format1 + " " + symbol);

                            if (allJsonObject.has("extraItem") && allJsonObject.getJSONArray("extraItem") != null) {

                                JSONArray extraItemArray = allJsonObject.getJSONArray("extraItem");

                                for (int b = 0; b < extraItemArray.length(); b++) {

                                    JSONObject jsonObject = extraItemArray.getJSONObject(b);
                                    menuExtraItemObj = Double.parseDouble(jsonObject.optString("menu_extra_item_price"));

                                    totalExtraItemPrice = totalExtraItemPrice + menuExtraItemObj;
                                    Double total2 = totalExtraItemPrice;

                                }
                                grandTotal = (totalExtraItemPrice + itemPrice) * Double.valueOf(inc_dec_tv.getText().toString());
                                int tongGia = Integer.valueOf(grandTotal.intValue());
                                String pattern1 = "###,###,###.###";
                                DecimalFormat decimalFormat1 = new DecimalFormat(pattern1);

                                String format2 = decimalFormat1.format(tongGia);

                                grand_total_price_tv.setText(format2 + " " + symbol);

                            }
                            transparent_layer.setVisibility(View.GONE);
                            progress.setVisibility(View.GONE);
                        }

                    } catch (JSONException e) {
                        VolleyLog.d("ERRRRRRR %s", e.toString());
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    public void deleteCurrentNode() {

        DatabaseReference query = mDatabase;//mDatabase;//FirebaseDatabase.getInstance().getReference();
        Query lastQuery = query.orderByKey().limitToLast(1);

        lastQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {

                    dataSnapshot1.getRef().setValue(null);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void deleteNodeExpectLast() {
        final DatabaseReference query = mDatabase;//mDatabase;//FirebaseDatabase.getInstance().getReference();
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                @SuppressWarnings("unchecked")
                Map<String, Object> td = (HashMap<String, Object>) dataSnapshot.getValue();

                Collection<Object> values = td.values();

                JSONArray jsonArray = new JSONArray(values);

                int size = jsonArray.length();

                Query lastQuery = query.orderByKey().limitToFirst(size - 1);

                lastQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {

                            dataSnapshot1.getRef().setValue(null);
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    public void showDialogIfChangeRest() {

        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(AddToCartActivity.this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(AddToCartActivity.this);
        }
        builder.setTitle("Thay đổi nhà hàng")
                .setMessage("Bạn có muốn thay đổi đơn hàng của mình và bắt đầu đơn hàng mới?")
                .setPositiveButton("Đồng ý", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                        deleteNodeExpectLast();
                        SharedPreferences.Editor editor = sPref.edit();
                        editor.putInt("count", 0).commit();
                        Toast.makeText(AddToCartActivity.this, "Đã thay đổi nhà hàng, mời bạn đặt món", Toast.LENGTH_SHORT).show();
                    }


                })
                .setNegativeButton("Hủy bỏ", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .show();

    }

}

