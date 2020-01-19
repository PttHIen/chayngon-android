package com.vantinviet.foodies.android.RActivitiesAndFragments.RAdapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vantinviet.foodies.android.R;
import com.vantinviet.foodies.android.RActivitiesAndFragments.RiderModels.RiderJobModel;

import java.util.ArrayList;

/**
 * Created by Nabeel on 1/16/2018.
 */

public class RCurrentJobAdapter extends RecyclerView.Adapter<RCurrentJobAdapter.ViewHolder> {

    ArrayList<RiderJobModel> getDataAdapter;
    Context context;
    RPendingJobAdapter.OnItemClickListner onItemClickListner;

    public RCurrentJobAdapter(ArrayList<RiderJobModel> getDataAdapter, Context context){
        super();
        this.getDataAdapter = getDataAdapter;
        this.context = context;

    }

    @Override
    public RCurrentJobAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_item_rider_jobs, parent, false);

        RCurrentJobAdapter.ViewHolder viewHolder = new RCurrentJobAdapter.ViewHolder(v);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        RiderJobModel jobListModel = getDataAdapter.get(position);
        String symbol = jobListModel.getRider_symbol();
        holder.r_hotel_name.setText(jobListModel.getHotel_name());
        holder.r_order_number.setText("Order #"+jobListModel.getOrder_number());
        holder.r_order_address.setText(jobListModel.getHotel_address());
        holder.r_total_bil_tv.setText(jobListModel.getOrder_price() + symbol);
        holder.card_detail_tv.setText(jobListModel.getOrder_cash_status());
        holder.time_tv.setText(jobListModel.getOrder_time());


        holder.r_job_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onItemClickListner.OnItemClicked(v, position);
            }
        });
    }


    @Override
    public int getItemCount() {
        return getDataAdapter.size() ;
    }



    class ViewHolder extends RecyclerView.ViewHolder{

        public TextView r_hotel_name,r_order_number,r_order_address,r_total_bil_tv,card_detail_tv,time_tv;
        RelativeLayout r_job_main;


        public ViewHolder(View itemView) {

            super(itemView);

            r_hotel_name = itemView.findViewById(R.id.r_hotel_name);
            r_order_number = itemView.findViewById(R.id.r_order_number);
            r_order_address = itemView.findViewById(R.id.r_order_address);
            r_total_bil_tv = itemView.findViewById(R.id.r_total_bil_tv);
            card_detail_tv = itemView.findViewById(R.id.card_detail_tv);
            time_tv = itemView.findViewById(R.id.time_tv);

            r_job_main = itemView.findViewById(R.id.r_job_main);

        }
    }

    public interface OnItemClickListner {
        void OnItemClicked(View view, int position);
    }

    public void setOnItemClickListner(RPendingJobAdapter.OnItemClickListner onCardClickListner) {
        this.onItemClickListner = onCardClickListner;
    }
}