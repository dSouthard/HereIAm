package com.ecen5673.diana.hereiam;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class WelcomeTutorialFragmentAlertScreen extends Fragment {

    public WelcomeTutorialFragmentAlertScreen() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_welcome_tutorial_alert, viewGroup, false);
    }
}
