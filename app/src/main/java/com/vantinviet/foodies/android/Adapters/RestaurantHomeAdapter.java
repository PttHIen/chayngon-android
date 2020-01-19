package com.vantinviet.foodies.android.Adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;
import com.vantinviet.foodies.android.Constants.Config;
import com.vantinviet.foodies.android.R;
import com.vantinviet.foodies.android.Models.RestaurantsModel;

import java.io.InputStream;
import java.util.ArrayList;

import static com.facebook.FacebookSdk.getApplicationContext;

/*
 * Created by Sambhaji Karad on 04-Jan-18
 * Mobile 9423476192
 * Email sambhaji2134@gmail.com/
*/

public class RestaurantHomeAdapter extends RecyclerView.Adapter<RestaurantHomeAdapter.ViewHolder> {
    ArrayList<RestaurantsModel> getDataAdapter;
    private ArrayList<RestaurantsModel> mFilteredList;
    private ArrayList<RestaurantsModel> mValues;
    private Context mContext;
    RequestQueue queue;
    private ArrayList<RestaurantsModel> RestaurantDataAdapter;
    protected ItemListener mListener;
    private FragmentTransaction curentTransaction;
    String lat, lon, user_id;
    public RestaurantHomeAdapter(Context context, ArrayList<RestaurantsModel> values,ItemListener mListener) {
        mValues = values;
        mContext = context;
        mListener = mListener;
        queue = Volley.newRequestQueue(getApplicationContext());
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public SharedPreferences sharedPreferences;

        private TextView textView, txt_fee_icon, txt_baking;
        private ImageView imageView;
        private RelativeLayout relativeLayout;
        private RestaurantsModel item;
        public ViewHolder(View v) {
            super(v);
            v.setOnClickListener(this);
            textView = (TextView) v.findViewById(R.id.title_restaurants);
            txt_baking = (TextView) v.findViewById(R.id.tv_baking);
            txt_fee_icon = (TextView) v.findViewById(R.id.tv_fee_icon);
            imageView = (ImageView) v.findViewById(R.id.profile_image_restaurant);
            relativeLayout = (RelativeLayout) v.findViewById(R.id.relativeLayout);
        }

        public void setData(RestaurantsModel item) {
            this.item = item;
            textView.setText(item.getRestaurant_name());
            txt_baking.setText(item.getPreparation_time() + " min");
            txt_fee_icon.setText(item.getRestaurant_distance());
            String url = Config.imgBaseURL + item.getRestaurant_image();

            Picasso.with(getApplicationContext()).load(url).into(imageView);
          // Picasso.with(getContext()).load(Config.imgBaseURL + item.getRestaurant_image()).into(imageView);
          //  new DownloadImageFromInternet(imageView).execute(Config.imgBaseURL + item.getRestaurant_image());



            //imageView.setImageResource(item.drawable);
        }

        public void onClick(View view) {
            if (mListener != null) {
                mListener.onItemClick(item);
            }
        }

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_home_page_restaurant, parent, false);
        return new ViewHolder(view);
    }
    public void setmFilteredList(ArrayList<RestaurantsModel> mFilteredList) {
        this.mFilteredList = mFilteredList;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        viewHolder.setData(mValues.get(position));
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public interface ItemListener {
        void onItemClick(RestaurantsModel item);
    }
    private class DownloadImageFromInternet extends AsyncTask<String, Void, Bitmap> {
        ImageView imageView;

        public DownloadImageFromInternet(ImageView imageView) {
            this.imageView = imageView;
        }

        protected Bitmap doInBackground(String... urls) {
            String imageURL = urls[0];
            Bitmap bimage = null;
            try {
                InputStream in = new java.net.URL(imageURL).openStream();
                bimage = BitmapFactory.decodeStream(in);

            } catch (Exception e) {
                Log.e("Error Message", e.getMessage());
                e.printStackTrace();
            }
            return bimage;
        }

        protected void onPostExecute(Bitmap result) {
            imageView.setImageBitmap(result);
        }
    }

}

