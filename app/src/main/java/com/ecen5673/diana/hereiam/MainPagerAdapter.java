package com.ecen5673.diana.hereiam;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

/**
 * Created by diana on 9/28/16.
 *
 * A simple pager adapter that represents 5 ScreenSlidePageFragment objects
 * in sequence
 */
public class MainPagerAdapter extends FragmentStatePagerAdapter {

    private static final String TAG = "MainPagerAdapter";

    // The number of pages to show in this demo
    private static final int NUM_PAGES = 4;
    private MainFragmentListViewScreen listViewScreen;
    private MainFragmentMapViewScreen mapViewScreen;
    private MainFragmentMenuScreen menuScreen;
    private MainFragmentAlertScreen alertScreen;

    public MainPagerAdapter(FragmentManager fm){
        super(fm);
        Log.d(TAG, "Creating MainPagerAdapter");

        // Create listview and mapview fragments
        listViewScreen = new MainFragmentListViewScreen();
        mapViewScreen = new MainFragmentMapViewScreen();
    }

    @Override
    public Fragment getItem(int position) {
        switch(position){
            default:
                // Listview Screen
                if (listViewScreen == null) {
                    listViewScreen = new MainFragmentListViewScreen();
                }
                return listViewScreen;
            case 1:
                // Mapview Screen
                if (mapViewScreen == null)
                    mapViewScreen = new MainFragmentMapViewScreen();
                return mapViewScreen;
            case 2:
                // Menu Screen
                if (menuScreen == null)
                    menuScreen = new MainFragmentMenuScreen();
                return menuScreen;
            case 3:
                // Alert Screen
                if (alertScreen == null)
                    alertScreen = new MainFragmentAlertScreen();
                return alertScreen;
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
                return "Map";
            case 2:
                return "Menu";
            case 3:
                return "Alerts";
            default:
                return "Home";
        }
    }
}
