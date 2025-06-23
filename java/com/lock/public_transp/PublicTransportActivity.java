package com.lock.public_transp;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import android.Manifest;
import com.lock.R;
import com.lock.BuildConfig;
import com.lock.data.model.PublicTransportRoute;
import com.lock.data.model.PublicTransportShapePoint;
import com.lock.data.model.PublicTransportStop;
import com.lock.data.model.PublicTransportTrip;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class PublicTransportActivity extends AppCompatActivity implements OnMapReadyCallback{

    private static final int SMS_PERMISSION_REQUEST_CODE = 101;
    private final String TRANZY_API_KEY = BuildConfig.TRANZY_API_KEY;
    private final String AGENCY_ID = "8";
    private Spinner spinnerRoutes, spinnerTrips;
    private GoogleMap mMap;
    private TranzyApiService apiService;
    private List<PublicTransportRoute> routeList = new ArrayList<>();
    private List<PublicTransportTrip> tripList = new ArrayList<>();
    private List<PublicTransportStop> allStops = new ArrayList<>();
    private Polyline currentPolyline;
    private List<Marker> stopMarkers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_public_transport);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS},
                    SMS_PERMISSION_REQUEST_CODE);
        }

        Button btnBuyTicket = findViewById(R.id.btn_buy_ticket);
        btnBuyTicket.setOnClickListener(v -> showBuyTicketDialog());

        spinnerRoutes = findViewById(R.id.spinner_routes);
        spinnerTrips = findViewById(R.id.spinner_trips);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.tranzy.ai/v1/opendata/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(TranzyApiService.class);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        fetchRoutes();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        LatLng timisoara = new LatLng(45.7489, 21.2087); // Timisoara coordinates
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(timisoara, 12));

        fetchStops();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                    mMap.getUiSettings().setMyLocationButtonEnabled(true);
                }
            } else {
                Log.e("Location", "Location permission denied");
            }
        }

        if (requestCode == SMS_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "SMS permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "SMS permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void fetchStops() {
        Call<List<PublicTransportStop>> call = apiService.getStops(TRANZY_API_KEY, AGENCY_ID);
        call.enqueue(new Callback<List<PublicTransportStop>>() {
            @Override
            public void onResponse(@NonNull Call<List<PublicTransportStop>> call, @NonNull Response<List<PublicTransportStop>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allStops = response.body();
                } else {
                    Log.e("API_ERROR", "Failed to fetch stops: " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<PublicTransportStop>> call, @NonNull Throwable t) {
                Log.e("API_ERROR", "Failed to fetch stops", t);
            }
        });
    }

    private void fetchRoutes() {
        Call<List<PublicTransportRoute>> call = apiService.getRoutes(TRANZY_API_KEY, AGENCY_ID);
        call.enqueue(new Callback<List<PublicTransportRoute>>() {
            @Override
            public void onResponse(@NonNull Call<List<PublicTransportRoute>> call, @NonNull Response<List<PublicTransportRoute>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    routeList = response.body();

                    Collections.sort(routeList, new Comparator<PublicTransportRoute>() {
                        @Override
                        public int compare(PublicTransportRoute route1, PublicTransportRoute route2) {
                            return route1.getFormattedName().compareToIgnoreCase(route2.getFormattedName());
                        }
                    });

                    populateRouteDropdown();
                } else {
                    Log.e("API_ERROR", "Failed to fetch routes: " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<PublicTransportRoute>> call, @NonNull Throwable t) {
                Log.e("API_ERROR", "Failed to fetch routes", t);
            }
        });
    }

    private void populateRouteDropdown() {
        List<String> routeNames = new ArrayList<>();
        routeNames.add("Select Route");

        for (PublicTransportRoute route : routeList) {
            routeNames.add(route.getFormattedName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                routeNames
        );
        spinnerRoutes.setAdapter(adapter);

        spinnerRoutes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    return;
                }
                fetchTrips(routeList.get(position - 1).getRouteId());
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void fetchTrips(int routeId) {
        Call<List<PublicTransportTrip>> call = apiService.getTrips(TRANZY_API_KEY, AGENCY_ID);
        call.enqueue(new Callback<List<PublicTransportTrip>>() {
            @Override
            public void onResponse(@NonNull Call<List<PublicTransportTrip>> call, @NonNull Response<List<PublicTransportTrip>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    tripList.clear();

                    List<String> tripNames = new ArrayList<>();
                    tripNames.add("Select Direction");

                    for (PublicTransportTrip trip : response.body()) {
                        if (trip.getRouteId() == routeId) {
                            tripList.add(trip);
                            tripNames.add(trip.getFormattedName());
                        }
                    }
                    spinnerTrips.setAdapter(new ArrayAdapter<>(PublicTransportActivity.this, android.R.layout.simple_spinner_dropdown_item, tripNames));

                    spinnerTrips.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            if (position == 0) {
                                return;
                            }
                            fetchShapes(tripList.get(position - 1).getShapeId());
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {}
                    });

                } else {
                    Log.e("API_ERROR", "Failed to fetch trips: " + response.message());
                }
            }
            @Override
            public void onFailure(@NonNull Call<List<PublicTransportTrip>> call, @NonNull Throwable t) {
                Log.e("API_ERROR", "Failed to fetch trips", t);
            }
        });
    }

    private void fetchShapes(String shapeId) {
        Call<List<PublicTransportShapePoint>> call = apiService.getShapes(TRANZY_API_KEY, AGENCY_ID, shapeId);
        call.enqueue(new Callback<List<PublicTransportShapePoint>>() {
            @Override
            public void onResponse(@NonNull Call<List<PublicTransportShapePoint>> call, @NonNull Response<List<PublicTransportShapePoint>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    drawRoute(response.body());
                } else {
                    Log.e("API_ERROR", "Failed to fetch shapes: " + response.message());
                }
            }
            @Override
            public void onFailure(@NonNull Call<List<PublicTransportShapePoint>> call, @NonNull Throwable t) {
                Log.e("API_ERROR", "Failed to fetch shapes", t);
            }
        });
    }

    private void drawRoute(List<PublicTransportShapePoint> shapePoints) {
        if (currentPolyline != null) {
            currentPolyline.remove();
        }
        PolylineOptions polylineOptions = new PolylineOptions().color(Color.BLUE).width(5f);
        for (PublicTransportShapePoint sp : shapePoints) {
            polylineOptions.add(new LatLng(sp.getShapeLat(), sp.getShapeLon()));
        }
        currentPolyline = mMap.addPolyline(polylineOptions);
        displayNearbyStops(shapePoints);
    }

    private void displayNearbyStops(List<PublicTransportShapePoint> shapePoints) {
        for (Marker marker : stopMarkers) {
            marker.remove();
        }
        stopMarkers.clear();

        double thresholdDistance = 0.001;

        for (PublicTransportStop stop : allStops) {
            LatLng stopLatLng = new LatLng(stop.getStopLat(), stop.getStopLon());

            boolean isNearby = false;
            for (PublicTransportShapePoint shapePoint : shapePoints) {
                LatLng shapeLatLng = new LatLng(shapePoint.getShapeLat(), shapePoint.getShapeLon());
                double distance = calculateDistance(stopLatLng, shapeLatLng);

                if (distance < thresholdDistance) {
                    isNearby = true;
                    break;
                }
            }

            if (isNearby) {
                Marker marker = mMap.addMarker(new MarkerOptions().position(stopLatLng).title(stop.getStopName()));
                stopMarkers.add(marker);
            }
        }
    }

    private double calculateDistance(LatLng point1, LatLng point2) {
        double latDiff = point1.latitude - point2.latitude;
        double lonDiff = point1.longitude - point2.longitude;
        return Math.sqrt(latDiff * latDiff + lonDiff * lonDiff);
    }

    private void showBuyTicketDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Route Number");

        final EditText input = new EditText(this);
        input.setHint("Route Number");
        builder.setView(input);

        builder.setPositiveButton("Buy", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String routeNumber = input.getText().toString().trim();
                if (!TextUtils.isEmpty(routeNumber)) {
                    sendSms(routeNumber);
                } else {
                    Toast.makeText(PublicTransportActivity.this, "Please enter a route number", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();
    }

    private void sendSms(String routeNumber) {
        String phoneNumber = "7442";
        String message = "B" + routeNumber;

        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            Toast.makeText(this, "Ticket purchased successfully!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "SMS failed to send", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

}

