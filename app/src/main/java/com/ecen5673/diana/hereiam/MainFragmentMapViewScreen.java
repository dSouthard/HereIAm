package com.ecen5673.diana.hereiam;

import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by diana on 9/26/16.
 * Contains the map view for the app
 */
public class MainFragmentMapViewScreen extends Fragment
        implements OnMapReadyCallback {

    protected static final String TAG = "MFMapViewScreen";

    // Map Variables
    protected static GoogleMap map;
    private MapView mapView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        View view = inflater.inflate(R.layout.fragment_main_mapview, container, false);
        mapView = (MapView) view.findViewById(R.id.mapView);
        mapView.getMapAsync(this);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        mapView.onCreate(bundle);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume" );
        mapView.onResume();
    }

    // Initial configurations to the map, once it is ready to be viewed
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady" );
        map = googleMap;

        // Set view type (Satellite, hybrid, geographic
        map.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        // Enable my location
        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsRevoked();
        }
        map.setMyLocationEnabled(true);

        // Set up to show info window for the user
        map.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                // show info window with most up-to-date information
                UserInfoWindowAdapter userWindow = new UserInfoWindowAdapter(getActivity());
                showUserInfoWindow(userWindow.getUserInfoContents());
                return true;
            }
        });

        // Add friendsHashMap' markers
        addFriendsMarkers();

        // Set up custom info window
        map.setInfoWindowAdapter(new UserInfoWindowAdapter(getActivity()));
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                // show info window with most up-to-date information
                marker.showInfoWindow();
                return true;
            }
        });
    }

    protected void showUserInfoWindow(View view) {

        if (ActivityCompat.checkSelfPermission(this.getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this.getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsRevoked();
        }

//        Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
        if (MainActivity.user.getLocation() != null)
        {
            LatLng latLng = latLngFromString(MainActivity.user.getLocation());
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(latLng)      // Sets the center of the map to location user
                    .zoom(17)                   // Sets the zoom
//                    .bearing(90)                // Sets the orientation of the camera to east
//                    .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder
            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
        builder.setView(view)
                // Add action buttons
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onPause(){
        super.onPause();
        mapView.onPause();
    }


    protected static void addFriendsMarkers(){
        Log.d(TAG, "addFriendsMarkers" );
        // Clear previous markers as they may be inaccurate
        map.clear();

        // Set up markers for friendsHashMap and show locations
        if (!MainActivity.friendsArrayList.isEmpty())
            for (User friend : MainActivity.friendsArrayList){
                Marker marker = map.addMarker(new MarkerOptions()
                        .position(latLngFromString(friend.getLocation()))
                        .title(friend.getUserName()));
                marker.setTag(friend);
            }
    }

    // Location permissions should have been granted during welcome tutorial, but this may have been revoked by user
    public void permissionsRevoked(){
        Toast.makeText(this.getContext(), "Location permissions were revoked, cannot use this app", Toast.LENGTH_SHORT).show();
        System.exit(0);
        // TODO Implement method to ask for permissions again before exiting
    }

    // Recreate a LatLng from a formatted string
    private static LatLng latLngFromString(String string) {
        String parts[] = string.split(",");
        return new LatLng(Double.parseDouble(parts[0]), Double.parseDouble(parts[1]));
    }

}
