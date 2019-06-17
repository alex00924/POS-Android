package com.bulletcart.videorewards.Adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.bulletcart.videorewards.Fragments.HomeFragment;
import com.bulletcart.videorewards.Fragments.StoresFragment;
import com.bulletcart.videorewards.Fragments.TransactionsFragment;
import com.bulletcart.videorewards.Fragments.VideosFragment;


/**
 * Created by DroidOXY
 */

public class ViewPagerAdapter extends FragmentStatePagerAdapter {

    private CharSequence Titles[];
    private int NumbOfTabs;

    // Constructor
    public ViewPagerAdapter(FragmentManager fm, CharSequence mTitles[], int mNumbOfTabsumb) {
        super(fm);
        this.Titles = mTitles;
        this.NumbOfTabs = mNumbOfTabsumb;
    }

    @Override
    public Fragment getItem(int position) {

        if(position == 0) {

//            return new HomeFragment();
            return new StoresFragment();

        }else{
            return new VideosFragment();
//            return new TransactionsFragment();

        }
    }

    @Override
    public CharSequence getPageTitle(int position){ return Titles[position]; }

    @Override
    public int getCount(){ return NumbOfTabs; }

}