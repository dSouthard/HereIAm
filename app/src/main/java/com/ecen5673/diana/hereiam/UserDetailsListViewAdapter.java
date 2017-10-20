package com.ecen5673.diana.hereiam;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;

import java.util.ArrayList;

/**
 * Created by diana on 9/30/16.
 *
 */
public class UserDetailsListViewAdapter extends ArrayAdapter<User> {

    //    private ArrayList arrayData;
    private Context context;

    public UserDetailsListViewAdapter(Context context, ArrayList <User> objects) {
        super(context, 0, objects);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        // Get the data time for this position
        User user = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.listview_item_record, parent, false);
        }

        // Lookup view for data population
        TextView recordUserName = (TextView) convertView.findViewById(R.id.recordNameField);
        TextView recordLocation = (TextView) convertView.findViewById(R.id.recordLocationField);
        TextView recordAddress = (TextView) convertView.findViewById(R.id.recordAddress);
        TextView recordUpdateTime = (TextView) convertView.findViewById(R.id.recordUpdateTimeField);
        TextView recordActivity = (TextView) convertView.findViewById(R.id.recordActivity);
        ImageView recordProfile = (ImageView) convertView.findViewById(R.id.recordUserProfile);

        // Populate the data into the template using the data object
        recordUserName.setText(user.getUserName());
        recordLocation.setText(user.getLocation());
        recordAddress.setText(user.getAddress());
        recordUpdateTime.setText(user.getUpdateTime());
        recordActivity.setText(user.getActivity());
        Glide.with(context)
                .using(new FirebaseImageLoader())
                .load(MainActivity.storageRef.child(user.getUserID()).child(Constants.PROFILE_STRING))
//                .diskCacheStrategy(DiskCacheStrategy.NONE)
//                .skipMemoryCache(true)
                .into(recordProfile);

        return convertView;
    }
}
