package com.lock.train_schedule;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class RouteAdapter extends ArrayAdapter<Route> {
    public RouteAdapter(Context context, List<Route> routes) {
        super(context, 0, routes);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Route route = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(android.R.layout.simple_list_item_1, parent, false);
        }

        TextView textView = convertView.findViewById(android.R.id.text1);

        // Format: "First Departure → Final Destination"
        String[] allStops = route.getFullRoute().split(" - ");
        String displayText = allStops[0] + " → " + allStops[allStops.length-1];

        textView.setText(displayText);
        return convertView;
    }
}
