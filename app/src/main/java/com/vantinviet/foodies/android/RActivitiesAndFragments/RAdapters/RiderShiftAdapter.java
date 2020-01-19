package com.vantinviet.foodies.android.RActivitiesAndFragments.RAdapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.vantinviet.foodies.android.Constants.Config;
import com.vantinviet.foodies.android.Constants.PreferenceClass;
import com.vantinviet.foodies.android.R;
import com.vantinviet.foodies.android.RActivitiesAndFragments.RAvailablityFragment;
import com.vantinviet.foodies.android.RActivitiesAndFragments.RiderModels.RShiftModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Nabeel on 3/22/2018.
 */

public class RiderShiftAdapter extends RecyclerView.Adapter<RiderShiftAdapter.ViewHolder>  {

    ArrayList<RShiftModel> getDataAdapter;
    Context context;
    OnItemClickListner onItemClickListner;
    RAvailablityFragment fragment;
    String user_id;
    SharedPreferences sPref;
    RelativeLayout progressDialog;
   // String final_startDate,final_endDate;
    public RiderShiftAdapter(ArrayList<RShiftModel> getDataAdapter, Context context, RAvailablityFragment fragment){
        super();
        this.getDataAdapter = getDataAdapter;
        this.context = context;
        this.fragment = fragment;
    }

    @Override
    public RiderShiftAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_item_shifts, parent, false);
        sPref = context.getSharedPreferences(PreferenceClass.user,Context.MODE_PRIVATE);
        user_id = sPref.getString(PreferenceClass.pre_user_id,"");

        RiderShiftAdapter.ViewHolder viewHolder = new RiderShiftAdapter.ViewHolder(v);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RiderShiftAdapter.ViewHolder holder, final int position) {
        RShiftModel getDataAdapter1 =  getDataAdapter.get(position);

        holder.btn.setTag(getDataAdapter1);
        RShiftModel checkWetherToShow=(RShiftModel)holder.btn.getTag();

        holder.in_process_tv.setTag(getDataAdapter1);
        RShiftModel checkWetherToShowProcess=(RShiftModel)holder.in_process_tv.getTag();

        final String dtStart = getDataAdapter1.getDate();
        final String id = getDataAdapter1.getId();
      //  String confirm = getDataAdapter1.getConfirm();



        if(!RAvailablityFragment.FLAG_RIDER_TIMING){
            holder.btn.setText("Add");
            holder.btn.setVisibility(View.VISIBLE);

        }else {

            if (checkWetherToShowProcess.getAdmin_confirm().equalsIgnoreCase("1")) {

                if (checkWetherToShowProcess.getConfirm().equalsIgnoreCase("1")) {
                    holder.in_process_tv.setText("Confirmed");
                    holder.btn.setVisibility(View.GONE);
                } else if (checkWetherToShow.getConfirm().equalsIgnoreCase("0")) {
                    holder.btn.setVisibility(View.VISIBLE);
                } else {
                    holder.btn.setVisibility(View.GONE);
                }

            } else {
                holder.in_process_tv.setText("In Process");
                holder.btn.setVisibility(View.GONE);
            }
        }

        // holder.parent_tv.setText(dtStart);

            //   System.out.println("Time Display: " + sdfs.format(dt)); // <-- I got result here

        SimpleDateFormat form = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat postFormater = new SimpleDateFormat("EEEE, MMMM d, yyyy");

        String inputdate = dtStart;

        Date date = null;

        try {
            date = form.parse(inputdate);

        } catch (ParseException e) {
            e.printStackTrace();

        }

        String resultdate = postFormater.format(date);

        holder.date.setText(resultdate);


        final String endTime = getDataAdapter1.getEnd_time();
        final String   startTime = getDataAdapter1.getStart_time();

        String time = startTime;
        String time2 = endTime;

        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm:ss");
        final SimpleDateFormat dateFormat1 = new SimpleDateFormat("hh:mm a");
        Date date1,date2;
        try {
            date1 = dateFormat.parse(time);
            date2 = dateFormat.parse(time2);

            //   System.out.println("Time Display: " + sdfs.format(dt)); // <-- I got result here
            holder.start_end_date.setText(dateFormat1.format(date1)+"-"+dateFormat1.format(date2));
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        holder.order_item_main_div.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    onItemClickListner.OnItemClicked(v,position);
                }catch (NullPointerException e){
                    e.printStackTrace();
                }

            }
        });

        holder.btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addOpenShift(startTime,endTime,id,dtStart);
            }
        });

        if(RAvailablityFragment.FLAG_RIDER_TIMING){
            holder.btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    updateRiderShift(id);
                }
            });

        }

    }

    @Override
    public int getItemCount() {
        return getDataAdapter.size() ;
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        public TextView start_end_date,date,in_process_tv;

        RelativeLayout order_item_main_div;
        Button btn;


        public ViewHolder(View itemView) {

            super(itemView);

            start_end_date = itemView.findViewById(R.id.start_end_date);
            date = itemView.findViewById(R.id.date);
            order_item_main_div = (RelativeLayout) itemView.findViewById(R.id.order_item_main_div);
            in_process_tv = itemView.findViewById(R.id.in_process_tv);
            btn = itemView.findViewById(R.id.btn);

        }
    }

    public interface OnItemClickListner {
        void OnItemClicked(View view, int position);
    }

    public void setOnItemClickListner(OnItemClickListner onItemClickListner) {
        this.onItemClickListner = onItemClickListner;
    }


    public void addOpenShift(String startTime, String endTime, String shiftID, String date){

        fragment.progressDialog.setVisibility(View.VISIBLE);

        RequestQueue queue = Volley.newRequestQueue(context);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("user_id",user_id);
            jsonObject.put("starting_time",startTime);
            jsonObject.put("ending_time",endTime);
            jsonObject.put("open_shift_id",shiftID);
            jsonObject.put("date",date);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Config.ADD_RIDER_TIMING, jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                String strResponse = response.toString();
                try {
                    JSONObject jsonObject1 = new JSONObject(strResponse);

                    int code_id  = Integer.parseInt(jsonObject1.optString("code"));
                    if (code_id==200){

                        Toast.makeText(context,"SuccessFully Added",Toast.LENGTH_SHORT).show();
                        notifyDataSetChanged();
                        //fragment.progressDialog.setVisibility(View.GONE);
                        fragment.getAvailableTimeListRiderTiming("OpenShift");

                    }
                    else {
                        fragment.progressDialog.setVisibility(View.GONE);
                        Toast.makeText(context,strResponse.toString(),Toast.LENGTH_SHORT).show();
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                fragment.progressDialog.setVisibility(View.GONE);
                Toast.makeText(context,error.toString(),Toast.LENGTH_SHORT).show();

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

    private void updateRiderShift(String id){
        fragment.progressDialog.setVisibility(View.VISIBLE);
        RequestQueue queue = Volley.newRequestQueue(context);
        JSONObject jsonObject = new JSONObject();
        try {
           jsonObject.put("id",id);
           jsonObject.put("confirm","1");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Config.UPDATE_RIDER_SHIFT_STATUS, jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                String strResponse = response.toString();
                try {
                    JSONObject jsonObject1 = new JSONObject(strResponse);
                    int code_id  = Integer.parseInt(jsonObject1.optString("code"));
             //       JSONObject msgObj = jsonObject1.getJSONObject("msg");

                    if (code_id==200){

                        Toast.makeText(context,"SuccessFully Updated",Toast.LENGTH_SHORT).show();
                        notifyDataSetChanged();
                        fragment.getAvailableTimeListRiderTiming("RiderTiming");

                    }
                    else {
                        fragment.progressDialog.setVisibility(View.GONE);
                        Toast.makeText(context,strResponse,Toast.LENGTH_SHORT).show();
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                fragment.progressDialog.setVisibility(View.GONE);
                Toast.makeText(context,error.toString(),Toast.LENGTH_SHORT).show();

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


    public static String getDayFromDateString(String stringDate,String dateTimeFormat)
    {
        String[] daysArray = new String[] {"Saturday","Sunday","Monday","Tuesday","Wednesday","Thursday","Friday"};
        String[] monthArray = new String[]{"January","February","March","April","May","June","July","August","September","October",
                "November","December"};
        String day = "";
        String month ="";
        String year = "";
        int dayOfWeek =0;
        int monthOfWeek = 0;
        //dateTimeFormat = yyyy-MM-dd HH:mm:ss
        SimpleDateFormat formatter = new SimpleDateFormat(dateTimeFormat);
        Date date;
        try {
            date = formatter.parse(stringDate);
            Calendar c = Calendar.getInstance();
            c.setTime(date);
            dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
            monthOfWeek = c.get(Calendar.MONTH);
            year = String.valueOf(c.get(Calendar.YEAR));
            if (dayOfWeek < 0) {
                dayOfWeek += 7;
            }
            if(monthOfWeek<0){
                monthOfWeek+=12;
            }
            day = daysArray[dayOfWeek];
            month = monthArray[monthOfWeek];
        } catch (Exception e) {
            e.printStackTrace();
        }

        return month+" "+day+" "+year;
    }



}