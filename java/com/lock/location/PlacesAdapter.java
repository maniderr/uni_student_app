package com.lock.location;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PlacesAdapter extends RecyclerView.Adapter<PlacesAdapter.PlaceViewHolder> {

    private List<PlaceItem> placeItems;
    private final OnPlaceClickListener onPlaceClickListener;

    public PlacesAdapter(List<PlaceItem> placeItems, OnPlaceClickListener onPlaceClickListener) {
        this.placeItems = placeItems;
        this.onPlaceClickListener = onPlaceClickListener;
    }

    @NonNull
    @Override
    public PlaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new PlaceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceViewHolder holder, int position) {
        PlaceItem placeItem = placeItems.get(position);
        holder.textView.setText(placeItem.getName());
        holder.itemView.setOnClickListener(v -> onPlaceClickListener.onPlaceClick(placeItem.getPlaceId()));
    }

    @Override
    public int getItemCount() {
        return placeItems.size();
    }

    public void updatePlaces(List<PlaceItem> placeItems) {
        this.placeItems = placeItems;
        notifyDataSetChanged();
    }

    static class PlaceViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public PlaceViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);
        }
    }

    interface OnPlaceClickListener {
        public void onPlaceClick(String placeId);
    }
}