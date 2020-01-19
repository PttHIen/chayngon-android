package com.vantinviet.foodies.android.RActivitiesAndFragments.RAdapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import com.vantinviet.foodies.android.RActivitiesAndFragments.RChatFragment;
import com.vantinviet.foodies.android.RActivitiesAndFragments.RJobsFragment;
import com.vantinviet.foodies.android.RActivitiesAndFragments.RProfileFragment;

/**
 * Created by Nabeel on 1/15/2018.
 */

public class RAdapterPager extends FragmentStatePagerAdapter {
    SparseArray<Fragment> registeredFragments = new SparseArray<Fragment>();
    int mNumOfTabs;


    public RAdapterPager(FragmentManager fragmentManager, int tabCount) {
        super(fragmentManager);
        this.mNumOfTabs=tabCount;
    }
    @Override
    public Fragment getItem(int position) {
        Fragment fm=null;
        switch (position) {

            case 0:
                fm = new RJobsFragment();
                break;
            case 1:
                fm = new RChatFragment();
                break;
            case 2:
                fm = new RProfileFragment();
                break;

        }
        return fm;
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }

    /* @Override
     public Parcelable saveState() {
     // Do Nothing
     return saveState();
     }*/
    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }


    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        registeredFragments.put(position, fragment);
        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        registeredFragments.remove(position);
        super.destroyItem(container, position, object);
    }

    public Fragment getRegisteredFragment(int position) {
        return registeredFragments.get(position);
    }

}


