package com.example.pocketmanager;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class PagerAdapter extends FragmentStatePagerAdapter {

    int numOfTabs;

    public PagerAdapter(FragmentManager fm, int numTabs) {
        super(fm);
        this.numOfTabs=numTabs;
    }

    @Override
    public Fragment getItem(int i) {
        switch(i){
            case 0: return new TransactionFragment();
            case 1: return new OverviewFragment();
            default:return null;
        }
    }

    @Override
    public int getCount() {
        return numOfTabs;
    }
}
