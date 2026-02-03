package com.example.laundryfinderapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

public class NearbyLaundryMapActivity extends FragmentActivity
        implements OnMapReadyCallback {

    private static final int REQ_LOCATION = 2001;

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_laundry_map);

        fusedClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);

        if (hasLocationPermission()) {
            startLocationFlow();
        } else {
            requestLocationPermission();
        }
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQ_LOCATION
        );
    }

    private void startLocationFlow() {
        try {
            mMap.setMyLocationEnabled(true);
        } catch (SecurityException e) {
            return;
        }

        fusedClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location == null) {
                        Toast.makeText(
                                this,
                                "Unable to get location.",
                                Toast.LENGTH_LONG
                        ).show();
                        return;
                    }

                    LatLng me = new LatLng(
                            location.getLatitude(),
                            location.getLongitude()
                    );

                    mMap.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(me, 14f)
                    );

                    searchNearbyLaundry(location);
                });
    }

    private void searchNearbyLaundry(Location location) {

        double lat = location.getLatitude();
        double lng = location.getLongitude();

        int radius = 10000; // 10 km
        String apiKey = getString(R.string.google_maps_key);

        // 🔎 Search laundromats (English + Malay)
        String url =
                "https://maps.googleapis.com/maps/api/place/nearbysearch/json"
                        + "?location=" + lat + "," + lng
                        + "&radius=" + radius
                        + "&keyword=dobi|laundry|kedai dobi"
                        + "&key=" + apiKey;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        // ✅ CHECK API STATUS FIRST
                        String status = response.getString("status");

                        if (!status.equals("OK")) {
                            Toast.makeText(
                                    this,
                                    "Places API status: " + status,
                                    Toast.LENGTH_LONG
                            ).show();
                            return; // STOP HERE
                        }

                        JSONArray results = response.getJSONArray("results");

                        if (results.length() == 0) {
                            Toast.makeText(this,
                                    "No laundry found nearby",
                                    Toast.LENGTH_LONG).show();
                            return;
                        }

                        // Clear old markers
                        mMap.clear();

                        // Add markers
                        for (int i = 0; i < results.length(); i++) {
                            JSONObject place = results.getJSONObject(i);

                            String name = place.getString("name");
                            JSONObject loc = place
                                    .getJSONObject("geometry")
                                    .getJSONObject("location");

                            double pLat = loc.getDouble("lat");
                            double pLng = loc.getDouble("lng");

                            mMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(pLat, pLng))
                                    .title(name));
                        }

                        Toast.makeText(
                                this,
                                "Found " + results.length() + " laundries",
                                Toast.LENGTH_SHORT
                        ).show();

                    } catch (Exception e) {
                        Toast.makeText(
                                this,
                                "Parse error: " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                },
                error -> Toast.makeText(
                        this,
                        "Places request error: " + error.getMessage(),
                        Toast.LENGTH_LONG
                ).show()
        );

        Volley.newRequestQueue(this).add(request);
    }



    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(
                requestCode, permissions, grantResults
        );

        if (requestCode == REQ_LOCATION
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            startLocationFlow();

        } else {
            Toast.makeText(
                    this,
                    "Location permission is required.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }
}

