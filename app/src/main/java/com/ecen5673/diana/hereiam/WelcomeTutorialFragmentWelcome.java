package com.ecen5673.diana.hereiam;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class WelcomeTutorialFragmentWelcome extends Fragment {

    public WelcomeTutorialFragmentWelcome() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_welcome_tutorial_welcome, viewGroup, false);
    }
}
