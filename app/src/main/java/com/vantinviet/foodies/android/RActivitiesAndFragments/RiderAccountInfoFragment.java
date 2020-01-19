package com.vantinviet.foodies.android.RActivitiesAndFragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.vantinviet.foodies.android.Constants.AllConstants;
import com.vantinviet.foodies.android.Constants.PreferenceClass;
import com.vantinviet.foodies.android.HActivitiesAndFragment.HProfileFragment;
import com.vantinviet.foodies.android.R;
import com.vantinviet.foodies.android.Utils.FontHelper;

/**
 * Created by Nabeel on 1/17/2018.
 */

public class RiderAccountInfoFragment extends Fragment {

    ImageView back_icon;
    TextView user_f_name,user_l_name,user_contact_number,rider_mail;

    SharedPreferences sPref;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.rider_edit_account, container, false);
        sPref = getContext().getSharedPreferences(PreferenceClass.user, Context.MODE_PRIVATE);
        FrameLayout frameLayout = v.findViewById(R.id.account_main_container);

        FontHelper.applyFont(getContext(),frameLayout, AllConstants.verdana);
        init(v);
        return v;

    }

    public void init(View v){

        String userF_name= sPref.getString(PreferenceClass.pre_first,"");
        String userL_name = sPref.getString(PreferenceClass.pre_last,"");
        String phone_number = sPref.getString(PreferenceClass.pre_contact,"");
        String rider_email = sPref.getString(PreferenceClass.pre_email,"");


        back_icon = v.findViewById(R.id.back_icon);
        user_f_name = v.findViewById(R.id.user_f_name);
        user_l_name = v.findViewById(R.id.user_l_name);
        user_contact_number = v.findViewById(R.id.user_contact_number);
        rider_mail = v.findViewById(R.id.rider_mail);

        user_f_name.setText(userF_name);
        user_l_name.setText(userL_name);
        user_contact_number.setText(phone_number);
        rider_mail.setText(rider_email);

        back_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(HProfileFragment.FLAG_ADMIN){
                    HProfileFragment rJobsFragment = new HProfileFragment();
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.replace(R.id.account_main_container, rJobsFragment);
                    transaction.addToBackStack(null);
                    transaction.commit();
                    HProfileFragment.FLAG_ADMIN = false;
                }
                else {
                    RProfileFragment rJobsFragment = new RProfileFragment();
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.replace(R.id.account_main_container, rJobsFragment);
                    transaction.addToBackStack(null);
                    transaction.commit();
                }
            }
        });

    }
}
