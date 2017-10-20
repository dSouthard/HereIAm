package com.ecen5673.diana.hereiam;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

/**
 * Created by diana on 9/30/16.
 *
 * New window when clicking on markers whn viewing google maps
 */
public class UserInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
    private View view;
    private Context context;

    @SuppressLint("InflateParams")
    public UserInfoWindowAdapter(Activity activity) {
        this.context = activity;
        this.view = activity.getLayoutInflater().inflate(R.layout.listview_item_record, null);

        // Set content to fit into window
        LinearLayout thisLayout = (LinearLayout) view.findViewById(R.id.recordLinearLayout);
        thisLayout.getLayoutParams().width = LinearLayout.LayoutParams.WRAP_CONTENT;
    }

    // Auto-generated method stub
    @Override
    public View getInfoWindow(Marker marker) {
        view.setBackgroundColor(context.getColor(R.color.gray));

        // Fill in views with marker's items
        User user = (User) marker.getTag();
        Log.d("Info Window", "Clicked on User: " + user.toString());

        // Lookup view for data population
        TextView recordUserName = (TextView) view.findViewById(R.id.recordNameField);
        TextView recordLocation = (TextView) view.findViewById(R.id.recordLocationField);
        TextView recordAddress = (TextView) view.findViewById(R.id.recordAddress);
        TextView recordUpdateTime = (TextView) view.findViewById(R.id.recordUpdateTimeField);
        TextView recordActivity = (TextView) view.findViewById(R.id.recordActivity);
        ImageView recordProfile = (ImageView) view.findViewById(R.id.recordUserProfile);

        // Populate the data into the template using the data object
        recordUserName.setText(user.getUserName());
        recordLocation.setText(user.getLocation());
        recordAddress.setText(user.getAddress());
        recordUpdateTime.setText(user.getUpdateTime());
        recordActivity.setText(user.getActivity());
        Glide.with(context).using(new FirebaseImageLoader())
                .load(MainActivity.storageRef.child(user.getUserID()).child(Constants.PROFILE_STRING))
                .into(recordProfile);

        // wrap content to fit into window size
        recordUserName.getLayoutParams().width = LinearLayout.LayoutParams.WRAP_CONTENT;
        recordLocation.getLayoutParams().width = LinearLayout.LayoutParams.WRAP_CONTENT;
        recordAddress.getLayoutParams().width = LinearLayout.LayoutParams.WRAP_CONTENT;
        recordUpdateTime.getLayoutParams().width = LinearLayout.LayoutParams.WRAP_CONTENT;
        recordActivity.getLayoutParams().width = LinearLayout.LayoutParams.WRAP_CONTENT;

        return view;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

    public View getUserInfoContents() {
        // Fill in views with marker's items
        User user = MainActivity.user;

        // Lookup view for data population
        TextView recordUserName = (TextView) view.findViewById(R.id.recordNameField);
        TextView recordLocation = (TextView) view.findViewById(R.id.recordLocationField);
        TextView recordAddress = (TextView) view.findViewById(R.id.recordAddress);
        TextView recordUpdateTime = (TextView) view.findViewById(R.id.recordUpdateTimeField);
        TextView recordActivity = (TextView) view.findViewById(R.id.recordActivity);
        ImageView recordProfile = (ImageView) view.findViewById(R.id.recordUserProfile);

        // Populate the data into the template using the data object
//        recordUserName.setText(user.getUserName());
        recordUserName.setText(R.string.you);
        recordLocation.setText(user.getLocation());
        recordAddress.setText(user.getAddress());
        recordUpdateTime.setText(user.getUpdateTime());
        recordActivity.setText(user.getActivity());
//        if (user.getPhotoUri() != null) recordProfile.setImageURI(Uri.parse(user.getPhotoUri()));
        if (MainActivity.userBitmap != null)
            recordProfile.setImageBitmap(MainActivity.userBitmap);
        else
            Glide.with(context).using(new FirebaseImageLoader())
                .load(MainActivity.userProfileRef)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(recordProfile);

        // wrap content to fit into window size
        recordUserName.getLayoutParams().width = LinearLayout.LayoutParams.WRAP_CONTENT;
        recordLocation.getLayoutParams().width = LinearLayout.LayoutParams.WRAP_CONTENT;
        recordAddress.getLayoutParams().width = LinearLayout.LayoutParams.WRAP_CONTENT;
        recordUpdateTime.getLayoutParams().width = LinearLayout.LayoutParams.WRAP_CONTENT;
        recordActivity.getLayoutParams().width = LinearLayout.LayoutParams.WRAP_CONTENT;

        return view;
    }
}
