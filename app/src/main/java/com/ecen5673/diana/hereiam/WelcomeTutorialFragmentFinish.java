package com.ecen5673.diana.hereiam;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class WelcomeTutorialFragmentFinish extends Fragment {

    static public Button doneButton;

    public WelcomeTutorialFragmentFinish() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_welcome_tutorial_finish, viewGroup, false);
        doneButton = (Button)rootView.findViewById(R.id.finishButton);
        doneButton.setOnClickListener(new OnClickUserListener(getActivity()));
        return rootView;
    }
}
