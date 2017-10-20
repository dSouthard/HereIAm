package com.ecen5673.diana.hereiam;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by diana on 9/28/16.
 *
 * A simple pager adapter that represents 5 ScreenSlidePageFragment objects
 * in sequence
 */
public class WelcomeTutorialPagerAdapter extends FragmentStatePagerAdapter {

    // The number of pages to show in this demo
    private static final int NUM_PAGES = 6;

    public WelcomeTutorialPagerAdapter(FragmentManager fm){
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch(position){
            default:
                // Initial welcome fragment
                return new WelcomeTutorialFragmentWelcome();
            case 1:
                // Tutorial Page: Show what normal screen is, List View
                return new WelcomeTutorialFragmentListViewScreen();
            case 2:
                // Tutorial Page: Show what normal screen is, Map View
                return new WelcomeTutorialFragmentMapViewScreen();
            case 3:
                // Tutorial Page: Show what menu screen is
                return new WelcomeTutorialFragmentMenuScreen();
            case 4:
                // Tutorial Page: Show what Alert screen is
                return new WelcomeTutorialFragmentAlertScreen();
            case 5:
                // Done with tutorial, prepare to enter in user information
                return new WelcomeTutorialFragmentFinish();
        }
    }

    @Override
    public int getCount() {
        return NUM_PAGES;
    }

    @Override
    public CharSequence getPageTitle(int position){
        switch (position){
            case 1:
                return "Home ";
            case 2:
                return "Map";
            case 3:
                return "Menu";
            case 4:
                return "Alert";
            case 5:
                return "Done";
            default:
                return "Begin";
        }
    }
}
