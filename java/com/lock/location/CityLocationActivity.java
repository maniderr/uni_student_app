package com.lock.location;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.api.model.Place;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.lock.R;
import com.lock.BuildConfig;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CityLocationActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mGoogleMap;
    private SupportMapFragment mapFrag;
    private FusedLocationProviderClient mFusedLocationClient;
    private Location mLastLocation;
    private Marker mCurrLocationMarker;
    private ArrayList<Location> mLastLocationArray = new ArrayList<>();

    private EditText searchEditText;
    private RecyclerView recyclerView;
    private PlacesClient placesClient;
    private PlacesAdapter placesAdapter;
    private String PLACES_API_KEY = BuildConfig.PLACES_API_KEY;

    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_location);

        searchEditText = findViewById(R.id.searchEditText);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), PLACES_API_KEY);
        }
        placesClient = Places.createClient(this);

        placesAdapter = new PlacesAdapter(new ArrayList<>(), placeId -> {
            fetchPlaceDetails(placeId);
            recyclerView.setVisibility(View.GONE);
        });
        recyclerView.setAdapter(placesAdapter);

        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            String query = searchEditText.getText().toString();
            if (!query.isEmpty()) {
                searchPlaces(query);
                recyclerView.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(this, "Please enter a search term", Toast.LENGTH_SHORT).show();
            }
            return true;
        });

        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        checkLocationPermissionAndEnableMyLocation();
                    } else {
                        Toast.makeText(this, "Permission denied", Toast.LENGTH_LONG).show();
                    }
                }
        );

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mLastLocationArray = getIntent().getParcelableArrayListExtra("LastLocation");
        if (mLastLocationArray != null && !mLastLocationArray.isEmpty()) {
            mLastLocation = mLastLocationArray.get(0);
        } else {
            Toast.makeText(this, "Location data not found", Toast.LENGTH_SHORT).show();
        }

        mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFrag != null) {
            mapFrag.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        LatLng timisoara = new LatLng(45.7489, 21.2087); // Timisoara coordinates
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(timisoara, 12));

        checkLocationPermissionAndEnableMyLocation();

        mGoogleMap.setOnMyLocationButtonClickListener(() -> {
            updateLocation();
            return true;
        });
    }

    private void checkLocationPermissionAndEnableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mGoogleMap.setMyLocationEnabled(true);
        } else {
            checkLocationPermission();
        }
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void updateLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            Log.i("MapsActivity", "Location: " + location.getLatitude() + " " + location.getLongitude());

                            if (mCurrLocationMarker != null) {
                                mCurrLocationMarker.remove();
                            }

                            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                            MarkerOptions markerOptions = new MarkerOptions();
                            markerOptions.position(latLng);
                            markerOptions.title("Current Position");
                            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                            mCurrLocationMarker = mGoogleMap.addMarker(markerOptions);

                            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 11));
                        } else {
                            Toast.makeText(this, "Unable to retrieve location", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void searchPlaces(String query) {
        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                .setQuery(query)
                .build();

        placesClient.findAutocompletePredictions(request).addOnSuccessListener(response -> {
            List<PlaceItem> placeItems = new ArrayList<>();
            for (AutocompletePrediction prediction : response.getAutocompletePredictions()) {
                placeItems.add(new PlaceItem(
                        prediction.getPlaceId(),
                        prediction.getFullText(null).toString()
                ));
            }
            placesAdapter.updatePlaces(placeItems);
        }).addOnFailureListener(exception -> {
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                Log.e("PlacesAPI", "Place not found: " + apiException.getStatusCode());
                Toast.makeText(this, "Error: " + apiException.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchPlaceDetails(String placeId) {
        List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);
        FetchPlaceRequest request = FetchPlaceRequest.builder(placeId, placeFields).build();

        placesClient.fetchPlace(request).addOnSuccessListener(response -> {
            Place place = response.getPlace();
            Log.d("PlaceDetails", "Name: " + place.getName());
            Log.d("PlaceDetails", "Address: " + place.getAddress());
            Log.d("PlaceDetails", "LatLng: " + place.getLatLng());
            Toast.makeText(this, "Fetched: " + place.getName(), Toast.LENGTH_SHORT).show();

            if (place.getLatLng() != null) {
                placeMarkerAndZoom(place.getName(), place.getLatLng());
            }
        }).addOnFailureListener(exception -> {
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                Log.e("PlacesAPI", "Place details not found: " + apiException.getStatusCode());
            }
        });
    }

    private void placeMarkerAndZoom(String placeName, LatLng latLng) {
        if (mGoogleMap != null) {
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(latLng)
                    .title(placeName)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

            mGoogleMap.addMarker(markerOptions);

            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f));
        }
    }
}