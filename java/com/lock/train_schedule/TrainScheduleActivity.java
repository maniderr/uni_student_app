package com.lock.train_schedule;

import android.annotation.SuppressLint;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;

import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import android.widget.Button;

import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.lock.BuildConfig;
import com.lock.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TrainScheduleActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mGoogleMap;
    private Polyline routePolyline;
    private SupportMapFragment mapFragment;
    private ProgressBar progressBar;
    private AutoCompleteTextView autoCompleteDeparture, autoCompleteDestination;
    private Calendar calendar = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
    private List<String> trainStopsDep = new ArrayList<>();
    private List<String> trainStopsDes = new ArrayList<>();

    private ArrayAdapter<String> departureAdapter;
    private ArrayAdapter<String> destinationAdapter;

    private TrainsApiService trainStopApi, routeApi, tripApi, stopTimesApi;

    private List<TrainStop> trainStopsList = new ArrayList<>();
    private List<Route> routeList = new ArrayList<>();
    private List<Trip> tripList = new ArrayList<>();
    private List<StopTime> stopTimesList = new ArrayList<>();
    String TRAINS_API_KEY = BuildConfig.SUPABASE_API_KEY;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_train_schedule);

        progressBar = findViewById(R.id.progressBar);
        autoCompleteDeparture = findViewById(R.id.etDeparture);
        autoCompleteDestination = findViewById(R.id.etDestination);
        Button btnSearch = findViewById(R.id.btnSearch);

        autoCompleteDeparture.setOnItemClickListener((parent, view, position, id) -> {
            autoCompleteDeparture.setText((String) parent.getItemAtPosition(position));
        });
        autoCompleteDestination.setOnItemClickListener((parent, view, position, id) -> {
            autoCompleteDestination.setText((String) parent.getItemAtPosition(position));
        });

        trainStopApi = SupabaseClient.getClient().create(TrainsApiService.class);
        routeApi = SupabaseClient.getClient().create(TrainsApiService.class);
        tripApi = SupabaseClient.getClient().create(TrainsApiService.class);
        stopTimesApi = SupabaseClient.getClient().create(TrainsApiService.class);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        btnSearch.setOnClickListener(v -> {
            String departure = autoCompleteDeparture.getText().toString().trim();
            String destination = autoCompleteDestination.getText().toString().trim();

            if (departure.isEmpty() || destination.isEmpty()) {
                Toast.makeText(this, "Please select valid stations", Toast.LENGTH_SHORT).show();
                return;
            }

            TrainStop depStop = findTrainStop(departure);
            TrainStop desStop = findTrainStop(destination);

            if (depStop == null || desStop == null) {
                Toast.makeText(this, "Stations not found", Toast.LENGTH_SHORT).show();
                return;
            }

            mGoogleMap.clear();

            LatLng depLatLng = new LatLng(depStop.getStopLat(), depStop.getStopLon());
            LatLng desLatLng = new LatLng(desStop.getStopLat(), desStop.getStopLon());

            mGoogleMap.addMarker(new MarkerOptions().position(depLatLng).title("Departure: " + departure));
            mGoogleMap.addMarker(new MarkerOptions().position(desLatLng).title("Destination: " + destination));

            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(depLatLng);
            builder.include(desLatLng);

            int padding = 150;
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), padding));

            findMatchingRoutes(departure, destination);

        });

        fetchAllTrainStops();
        fetchAllRoutes();
        fetchAllStopTimes();
    }

    private void drawRoutePolyline(List<StopTime> stopTimes) {
        if (mGoogleMap == null) return;

        if (routePolyline != null) {
            routePolyline.remove();
        }

        List<LatLng> points = new ArrayList<>();

        for (StopTime stopTime : stopTimes) {
            TrainStop trainStop = findTrainStopById(stopTime.getStopId());
            if (trainStop != null) {
                points.add(new LatLng(trainStop.getStopLat(), trainStop.getStopLon()));
            }
        }

        if (points.size() > 1) {
            routePolyline = mGoogleMap.addPolyline(new PolylineOptions()
                    .addAll(points)
                    .color(Color.BLUE)
                    .width(8)
                    .geodesic(true));
        }
    }

    private TrainStop findTrainStop(String name) {
        for (TrainStop stop : trainStopsList) {
            if (stop.getStopName().equalsIgnoreCase(name)) {
                return stop;
            }
        }
        return null;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        LatLng romania = new LatLng(45.9432, 24.9668);
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(romania, 6));

        mGoogleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                View view = View.inflate(TrainScheduleActivity.this, R.layout.custom_info_window, null);

                TextView title = view.findViewById(R.id.info_window_title);
                TextView snippet = view.findViewById(R.id.info_window_snippet);

                title.setText(marker.getTitle() != null ? marker.getTitle() : "");

                String snippetText = marker.getSnippet();
                snippet.setText(snippetText != null ? snippetText : "");

                return view;
            }
        });
    }

    private void fetchAllRoutes() {
        fetchRouteData(0);
    }

    private void fetchRouteData(int offset) {
        String token = "Bearer " + TRAINS_API_KEY;
        Call<List<Route>> call = routeApi.getRoute("*", 200, offset, token, TRAINS_API_KEY);
        //progressBar.setVisibility(View.VISIBLE);
        call.enqueue(new Callback<List<Route>>() {
            @Override
            public void onResponse(Call<List<Route>> call, Response<List<Route>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<Route> routes = response.body();
                    //Log.d("TrainSchedule", "Fetched " + routes.size() + " routes.");
                    routeList.addAll(routes);

                    if (routes.size() == 200) {
                        fetchRouteData(offset + 200);
                    } else {
                        //runOnUiThread(() -> showRoutes(routeList));
                    }
                }
                else {
                    Log.e("TrainSchedule", "Response was not successful. Code: " + response.code());
                    Toast.makeText(TrainScheduleActivity.this, "Failed to fetch routes. Status code: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Route>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e("TrainSchedule", "Failed to fetch routes", t);
            }
        });
    }

    private void findMatchingRoutes(String departure, String destination) {
        List<Route> matchingRoutes = new ArrayList<>();

        for (Route route : routeList) {
            List<String> routeStops = Arrays.asList(route.getFullRoute().split(" - "));
            boolean hasDeparture = routeStops.stream().anyMatch(s -> s.equalsIgnoreCase(departure));
            boolean hasDestination = routeStops.stream().anyMatch(s -> s.equalsIgnoreCase(destination));

            if (hasDeparture && hasDestination) {
                int depIndex = routeStops.indexOf(departure);
                int destIndex = routeStops.indexOf(destination);
                if (depIndex < destIndex) {
                    matchingRoutes.add(route);
                }
            }
        }

        if (!matchingRoutes.isEmpty()) {
            showRouteSelectionDialog(matchingRoutes, departure, destination);
        } else {
            Toast.makeText(this, "No valid route found between selected stations", Toast.LENGTH_SHORT).show();

            Log.d("RouteDebug", "Departure: " + departure + ", Destination: " + destination);
            Log.d("RouteDebug", "Total routes checked: " + routeList.size());
        }
    }

    private void showRouteSelectionDialog(List<Route> matchingRoutes, String departure, String destination) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_train_route_selection, null);
        builder.setView(dialogView);

        ListView lvRoutes = dialogView.findViewById(R.id.lvRoutes);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        RouteAdapter adapter = new RouteAdapter(this, matchingRoutes);
        lvRoutes.setAdapter(adapter);

        AlertDialog dialog = builder.create();

        lvRoutes.setOnItemClickListener((parent, view, position, id) -> {
            Route selectedRoute = matchingRoutes.get(position);
            processSelectedRoute(selectedRoute, departure, destination);
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void processSelectedRoute(Route selectedRoute, String departure, String destination) {
        List<String> validStops = getValidIntermediateStops(selectedRoute, departure, destination);
        fetchTripsForRoute(selectedRoute.getRouteId(), validStops, selectedRoute);
    }

    private List<StopTime> filterStopTimesForSegment(List<StopTime> allStopTimes, List<String> validStops) {
        List<StopTime> segmentStopTimes = new ArrayList<>();

        for (StopTime stopTime : allStopTimes) {
            TrainStop trainStop = findTrainStopById(stopTime.getStopId());
            if (trainStop != null && validStops.contains(trainStop.getStopName())) {
                segmentStopTimes.add(stopTime);
            }
        }

        return segmentStopTimes;
    }

    private void fetchTripsForRoute(int routeId, List<String> validStops, Route selectedRoute) {
        String token = "Bearer " + TRAINS_API_KEY;

        Call<List<Trip>> call = tripApi.getTrips(
                "trip_id,route_id,service_id",
                "eq." + routeId,
                token,
                TRAINS_API_KEY
        );

        call.enqueue(new Callback<List<Trip>>() {
            @Override
            public void onResponse(Call<List<Trip>> call, Response<List<Trip>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    tripList = response.body();
                    if (!tripList.isEmpty()) {
                        // For simplicity, take first trip (or implement trip selection)
                        Trip selectedTrip = tripList.get(0);
                        List<StopTime> allStopTimes = getStopTimesForTrip(selectedTrip.getTripId());
                        List<StopTime> segmentStopTimes = filterStopTimesForSegment(allStopTimes, validStops);

                        if (!segmentStopTimes.isEmpty()) {
                            plotTripStopsOnMap(segmentStopTimes, selectedRoute);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Trip>> call, Throwable t) {
                Log.e("TrainSchedule", "Failed to fetch trips", t);
            }
        });
    }

    private void fetchAllStopTimes() {
        fetchStopTimes(0);
    }

    private void fetchStopTimes(int offset) {
        String token = "Bearer " + TRAINS_API_KEY;
        Call<List<StopTime>> call = stopTimesApi.getStopTimes("*", 1000, offset, token, TRAINS_API_KEY);

        call.enqueue(new Callback<List<StopTime>>() {
            @Override
            public void onResponse(Call<List<StopTime>> call, Response<List<StopTime>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<StopTime> stopTimes = response.body();
                    Log.d("TrainSchedule", "Fetched " + stopTimes.size() + " routes.");
                    stopTimesList.addAll(stopTimes);

                    if (stopTimes.size() == 1000) {
                        fetchStopTimes(offset + 1000);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<StopTime>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                t.printStackTrace();
            }
        });
    }

    private List<StopTime> getStopTimesForTrip(int tripId) {
        List<StopTime> stopsForTrip = new ArrayList<>();
        for (StopTime stopTime : stopTimesList) {
            if (stopTime.getTripId() == tripId) {
                stopsForTrip.add(stopTime);
            }
        }
        Collections.sort(stopsForTrip, (a, b) -> Integer.compare(a.getStopSequence(), b.getStopSequence()));
        return stopsForTrip;
    }

    private TrainStop findTrainStopById(int stopId) {
        for (TrainStop stop : trainStopsList) {
            if (stop.getStopId() == stopId) {
                return stop;
            }
        }
        Log.e("TrainSchedule", "No stop found for ID: " + stopId);
        return null;
    }

    private List<String> getValidIntermediateStops(Route route, String selectedDeparture, String selectedDestination) {
        List<String> allStops = Arrays.asList(route.getFullRoute().split(" - "));
        List<String> validStops = new ArrayList<>();
        boolean startCollecting = false;

        for (String stop : allStops) {
            if (stop.equalsIgnoreCase(selectedDeparture)) {
                startCollecting = true;
            }
            if (startCollecting) {
                validStops.add(stop.trim());
            }
            if (stop.equalsIgnoreCase(selectedDestination)) {
                break;
            }
        }

        return validStops;
    }

    private void plotTripStopsOnMap(List<StopTime> stopTimes, Route route) {
        if (mGoogleMap == null) return;

        mGoogleMap.clear();
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        boolean hasValidStops = false;

        drawRoutePolyline(stopTimes);

        for (StopTime stopTime : stopTimes) {
            TrainStop trainStop = findTrainStopById(stopTime.getStopId());
            if (trainStop != null) {
                LatLng stopLatLng = new LatLng(trainStop.getStopLat(), trainStop.getStopLon());

                StringBuilder snippetBuilder = new StringBuilder();
                snippetBuilder.append("Sequence: ").append(stopTime.getStopSequence());

                if (stopTime.getArrivalTime() != null) {
                    snippetBuilder.append("\nArrival: ").append(stopTime.getArrivalTime());
                }

                if (stopTime.getDepartureTime() != null) {
                    snippetBuilder.append("\nDeparture: ").append(stopTime.getDepartureTime());
                }

                mGoogleMap.addMarker(new MarkerOptions()
                        .position(stopLatLng)
                        .title(trainStop.getStopName())
                        .snippet(snippetBuilder.toString()));

                boundsBuilder.include(stopLatLng);
                hasValidStops = true;
            }
        }

        if (hasValidStops) {
            int padding = 150;
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), padding));
        }
    }

    @Override
    protected void onDestroy() {
        if (routePolyline != null) {
            routePolyline.remove();
        }
        super.onDestroy();
    }

    private void fetchAllTrainStops() {
        trainStopsDes.clear();
        trainStopsDep.clear();
        fetchTrainStops(0);
    }

    private void fetchTrainStops(int offset) {
        Call<List<TrainStop>> call = trainStopApi.getTrainStops("*", 1000, offset);
        //progressBar.setVisibility(View.VISIBLE);
        call.enqueue(new Callback<List<TrainStop>>() {
            @Override
            public void onResponse(Call<List<TrainStop>> call, Response<List<TrainStop>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<TrainStop> stops = response.body();
                    for (TrainStop stop : stops) {
                        trainStopsDep.add(stop.getStopName());
                        trainStopsDes.add(stop.getStopName());
                        trainStopsList.add(stop);
                    }
                    if (stops.size() == 1000) {
                        fetchTrainStops(offset + 1000);
                    } else {
                        runOnUiThread(() -> populateAutoComplete());
                    }
                }
            }

            @Override
            public void onFailure(Call<List<TrainStop>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                t.printStackTrace();
            }
        });
    }

    private void populateAutoComplete() {
        departureAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, trainStopsDep);
        destinationAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, trainStopsDes);

        autoCompleteDeparture.setAdapter(departureAdapter);
        autoCompleteDestination.setAdapter(destinationAdapter);

        autoCompleteDeparture.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterAutoComplete(s.toString(), autoCompleteDeparture);
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void afterTextChanged(Editable s) {}
        });

        autoCompleteDestination.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterAutoComplete(s.toString(), autoCompleteDestination);
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Force dropdown to show when typing
        autoCompleteDeparture.setThreshold(1);
        autoCompleteDestination.setThreshold(1);
    }

    private void filterAutoComplete(String searchText, AutoCompleteTextView autoCompleteTextView) {
        String search = searchText.toLowerCase();
        List<String> filteredList = new ArrayList<>();

        List<String> sourceList = (autoCompleteTextView == autoCompleteDeparture) ? trainStopsDep : trainStopsDes;

        for (String stop : sourceList) {
            if (stop.toLowerCase().contains(search)) {
                filteredList.add(stop);
            }
        }

        ArrayAdapter<String> adapter = (autoCompleteTextView == autoCompleteDeparture) ? departureAdapter : destinationAdapter;
        adapter.clear();
        adapter.addAll(filteredList);
        adapter.notifyDataSetChanged();
    }
}