package com.vantinviet.foodies.android.RActivitiesAndFragments;


import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.applandeo.materialcalendarview.exceptions.OutOfDateRangeException;
import com.applandeo.materialcalendarview.listeners.OnDayClickListener;
import com.vantinviet.foodies.android.Constants.AllConstants;
import com.vantinviet.foodies.android.Constants.Config;
import com.vantinviet.foodies.android.Constants.PreferenceClass;
import com.vantinviet.foodies.android.R;
import com.vantinviet.foodies.android.Utils.FontHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Nabeel on 1/18/2018.
 */

public class RiderAddAvailabilityFragment extends Fragment {

    EditText start_date,end_date;

    Button back_icon,save_availability_btn;
    RelativeLayout all_day_div;
    LinearLayout available_div;
    SwitchCompat switich_availability;
    ArrayList<String> arrayList;
    CalendarView calendarView;
    private boolean STATE;
    SharedPreferences sPref;
    String time_start,time_end;
    String finalTime;
    ProgressBar scheduleProgress;
    long diff;
    FrameLayout frameLayout;
    SwipeRefreshLayout refresh_layout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.rider_add_availability, container, false);
        sPref = getContext().getSharedPreferences(PreferenceClass.user, Context.MODE_PRIVATE);
         frameLayout = v.findViewById(R.id.add_available_container);
        FontHelper.applyFont(getContext(),frameLayout, AllConstants.verdana);
        frameLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });
      //  init(v);
        arrayList = new ArrayList<>();

        init(v);
        return v;

    }

    public void init(View v){
        scheduleProgress =v.findViewById(R.id.scheduleProgress);
        start_date = v.findViewById(R.id.start_date);
        end_date = v.findViewById(R.id.end_date);
        save_availability_btn = v.findViewById(R.id.save_availability_btn);
        back_icon = v.findViewById(R.id.back_icon);
        all_day_div = v.findViewById(R.id.all_day_div);
        available_div = v.findViewById(R.id.available_div);
        switich_availability = v.findViewById(R.id.switich_availability);



        switich_availability.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){

                    time_start = "11:59:00";
                    time_end="23:59:00";
                    all_day_div.setVisibility(View.VISIBLE);
                    available_div.setVisibility(View.GONE);

                }
                else {
                    available_div.setVisibility(View.VISIBLE);
                    all_day_div.setVisibility(View.GONE);
                }
            }
        });


        back_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment restaurantMenuItemsFragment = new RAvailablityFragment();
                FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                transaction.add(R.id.add_available_container, restaurantMenuItemsFragment,"ParentFragment").commit();
            }
        });

        end_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Calendar mcurrentTime = Calendar.getInstance();
                final int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);


                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        time_end = selectedHour +":"+selectedMinute+":"+"00";
                        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss");
                        SimpleDateFormat sdfs = new SimpleDateFormat("hh:mm a");
                        Date dt;
                        try {
                            dt = sdf.parse(time_end);
                            System.out.println("Time Display: " + sdfs.format(dt));
                            finalTime = sdfs.format(dt);

                            // <-- I got result here
                        } catch (ParseException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }


                        try {
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
                            Date date1 = simpleDateFormat.parse(time_start);
                           Date date2 = simpleDateFormat.parse(time_end);

                            long difference = date2.getTime() - date1.getTime();
                           long days = (int) (difference / (1000*60*60*24));
                           long hours_ = (int) ((difference - (1000*60*60*24*days)) / (1000*60*60));
                            hours_ = (hours_ < 0 ? -hours_ : hours_);

                            if(hours_>=2){
                                end_date.setText(finalTime);
                            }
                            else {
                                AlertDialog.Builder builder1 = new AlertDialog.Builder(getContext());
                                builder1.setMessage(getResources().getString(R.string.minimum_housr_str));
                                builder1.setCancelable(true);

                                builder1.setPositiveButton(
                                        "OK",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                dialog.cancel();
                                            }
                                        });


                                AlertDialog alert11 = builder1.create();
                                alert11.show();
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }


                    }
                }, hour, minute, false);
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();

            }
        });


        start_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);

                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {

                        time_start = selectedHour +":"+selectedMinute+":"+"00";
                        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss");
                        SimpleDateFormat sdfs = new SimpleDateFormat("hh:mm a");
                        Date dt;
                        try {
                            dt = sdf.parse(time_start);
                            System.out.println("Time Display: " + sdfs.format(dt));
                            String finalTime = sdfs.format(dt);
                            start_date.setText(finalTime);
                            // <-- I got result here
                        } catch (ParseException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }, hour, minute, false);
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();

            }
        });


        calendarView = (CalendarView) v.findViewById(R.id.calendarView);
        Date date = new Date();
        try {
            calendarView.setDate(date);
        } catch (OutOfDateRangeException e) {
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        Calendar calendar12 = Calendar.getInstance();
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),calendar.get(Calendar.DATE)-1);
        calendar12.set(Calendar.DAY_OF_MONTH,calendar12.getActualMaximum(Calendar.DAY_OF_MONTH));
        calendarView.setMinimumDate(calendar);
     //   calendarView.setMaximumDate(calendar12);
        calendarView.setEnabled(false);
        calendarView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });



        calendarView.setOnDayClickListener(new OnDayClickListener() {
            @Override
            public void onDayClick(EventDay eventDay) {

                Calendar clickedDayCalendar = eventDay.getCalendar();
                String finalTime = null;
                String str = clickedDayCalendar.getTime().toString();

                str = str.replaceAll("[\\[\\]]", "");

                DateFormat formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.US);
                DateFormat newFormate = new SimpleDateFormat("yyyy-MM-dd");
                Date dt;
                try {
                    dt = formatter.parse(str);

                    finalTime = newFormate.format(dt);

                    if (arrayList.contains(finalTime)){

                     //   Toast.makeText(getContext(),"Removed",Toast.LENGTH_SHORT).show();
                        arrayList.remove(finalTime);
                    }
                    else {

                        arrayList.add(finalTime);
                      //  Toast.makeText(getContext(), finalTime, Toast.LENGTH_SHORT).show();

                    }

                    // <-- I got result here
                } catch (ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }


            }
        });


        save_availability_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scheduleProgress.setVisibility(View.VISIBLE);
              String str = joinList(arrayList);
             // Toast.makeText(getContext(),str,Toast.LENGTH_LONG).show();
                addRiderTiming(str);

            }
        });

       /* Calendar calendar = Calendar.getInstance();
        Calendar calendar12 = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH,calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
        calendar12.set(Calendar.DAY_OF_MONTH,calendar12.getActualMaximum(Calendar.DAY_OF_MONTH));
        calendarView.setMinimumDate(calendar);
        calendarView.setMaximumDate(calendar12);*/
    }

    public static String joinList(ArrayList<String> list){
        return list.toString().replaceAll("[\\[.\\].\\s+]", "");
    }

    public void addRiderTiming(String commaSplittedStr){

        String user_id = sPref.getString(PreferenceClass.pre_user_id,"");
        String id = sPref.getString("id_","");
        String date = sPref.getString("date_","");

        RequestQueue queue = Volley.newRequestQueue(getContext());
        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("user_id",user_id);
            jsonObject.put("starting_time",time_start);
            jsonObject.put("ending_time",time_end);
            jsonObject.put("date",commaSplittedStr);
            if(RAvailablityFragment.IS_TIMING_ID){
                jsonObject.put("id",id);
                jsonObject.put("date",date);
                RAvailablityFragment.IS_TIMING_ID = false;
            }
            else {
                jsonObject.put("date",commaSplittedStr);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Config.ADD_RIDER_TIMING, jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                String strResponse = response.toString();
                JSONObject jsonObject1 = null;
                try {
                    jsonObject1 = new JSONObject(strResponse);
                    int code_id  = Integer.parseInt(jsonObject1.optString("code"));

                    if(code_id == 200){
                        scheduleProgress.setVisibility(View.GONE);
                        Toast.makeText(getContext(),"SuccessFlly added",Toast.LENGTH_SHORT).show();
                        Fragment restaurantMenuItemsFragment = new RAvailablityFragment();
                        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                        transaction.add(R.id.add_available_container, restaurantMenuItemsFragment,"ParentFragment").commit();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("JSONPost", "Error: " + error.getMessage());
               // Toast.makeText(getContext(),error.toString(),Toast.LENGTH_LONG).show();
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
}
