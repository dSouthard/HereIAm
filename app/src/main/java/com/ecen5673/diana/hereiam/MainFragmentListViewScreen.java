package com.ecen5673.diana.hereiam;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.ui.storage.images.FirebaseImageLoader;


public class MainFragmentListViewScreen extends Fragment {

    // TextViews
    static TextView recordUserName;
    static TextView recordLocation;
    static TextView recordAddress;
    static TextView recordUpdateTime;
    static TextView recordActivity;
    static ImageView recordProfile;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MainFragmentListViewScreen() {
    }

    @Override
    public void onCreate(Bundle bundle){
        // Call super's method
        super.onCreate(bundle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle bundle) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_main_listview, viewGroup, false);

        // Set up text views
        // Populate with user info
        recordUserName = (TextView) rootView.findViewById(R.id.recordNameField);
        recordLocation = (TextView) rootView.findViewById(R.id.recordLocationField);
        recordAddress = (TextView) rootView.findViewById(R.id.recordAddress);
        recordUpdateTime = (TextView) rootView.findViewById(R.id.recordUpdateTimeField);
        recordActivity = (TextView) rootView.findViewById(R.id.recordActivity);
        recordProfile = (ImageView) rootView.findViewById(R.id.recordUserProfile);

        // Set up ListView
        ListView recordsListView = (ListView) rootView.findViewById(R.id.recordListView);
        recordsListView.setAdapter(MainActivity.friendsAdapter);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle bundle) {
        // Call super's method
        super.onActivityCreated(bundle);

    }

//    Populate the data into the template using the data object
    protected static void setText(Context activity){
        if (MainActivity.user == null) return;
        if (recordUserName != null)
            recordUserName.setText(R.string.you);
        if (recordLocation != null)
            recordLocation.setText(MainActivity.user.getLocation());
        if (recordAddress != null)
            recordAddress.setText(MainActivity.user.getAddress());
        if (recordUpdateTime != null)
            recordUpdateTime.setText(MainActivity.user.getUpdateTime());
        if (recordActivity != null)
            recordActivity.setText(MainActivity.user.getActivity());
        if (MainActivity.userBitmap != null)
            recordProfile.setImageBitmap(MainActivity.userBitmap);
        else
            Glide.with(activity).using(new FirebaseImageLoader())
                .load(MainActivity.userProfileRef)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(recordProfile);
    }

    protected static void setText() {
        if (MainActivity.user == null) return;
//        Populate the data into the template using the data object
        if (recordUserName != null)
            recordUserName.setText(R.string.you);
        if (recordLocation != null)
            recordLocation.setText(MainActivity.user.getLocation());
        if (recordAddress != null)
            recordAddress.setText(MainActivity.user.getAddress());
        if (recordUpdateTime != null)
            recordUpdateTime.setText(MainActivity.user.getUpdateTime());
        if (recordActivity != null)
            recordActivity.setText(MainActivity.user.getActivity());
        if (MainActivity.userBitmap != null)
            recordProfile.setImageBitmap(MainActivity.userBitmap);
    }
}
