package com.lock.facultate_info;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lock.R;

import java.util.List;


public class FacultateAdapter extends RecyclerView.Adapter<FacultateAdapter.FacultateViewHolder> {

    private List<Facultate> facultateList;
    private OnFacultateClickListener listener;
    private boolean hideButtons;

    public FacultateAdapter(List<Facultate> facultateList, boolean hideButtons) {
        this.facultateList = facultateList;
        this.hideButtons = hideButtons;
    }

    @NonNull
    @Override
    public FacultateAdapter.FacultateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_facultate, parent, false);
        return new FacultateAdapter.FacultateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FacultateAdapter.FacultateViewHolder holder, int position) {
        Facultate facultate = facultateList.get(position);
        holder.nameTextView.setText(facultate.getName());
        holder.siteTextView.setText(facultate.getSite());
        holder.addressTextView.setText(facultate.getAddress());
        holder.phoneTextView.setText(facultate.getPhone());
        holder.faxTextView.setText(facultate.getFax());
        holder.emailTextView.setText(facultate.getEmail());

        if (hideButtons) {
            holder.btnUpdate.setVisibility(View.GONE);
            holder.btnDelete.setVisibility(View.GONE);
        } else {
            holder.btnUpdate.setVisibility(View.VISIBLE);
            holder.btnDelete.setVisibility(View.VISIBLE);
        }

        holder.btnUpdate.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUpdateClick(facultate);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(facultate);
            }
        });
    }

    public void setOnFacultateClickListener(OnFacultateClickListener listener) {
        this.listener = listener;
    }

    @Override
    public int getItemCount() {
        return facultateList.size();
    }

    public void setFacultateList(List<Facultate> facultati) {
        this.facultateList = facultati;
        notifyDataSetChanged();
    }

    public static class FacultateViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView siteTextView;
        TextView addressTextView;
        TextView phoneTextView;
        TextView faxTextView;
        TextView emailTextView;
        ImageButton btnUpdate;
        ImageButton btnDelete;

        public FacultateViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            siteTextView = itemView.findViewById(R.id.siteTextView);
            addressTextView = itemView.findViewById(R.id.addressTextView);
            phoneTextView = itemView.findViewById(R.id.phoneTextView);
            faxTextView = itemView.findViewById(R.id.faxTextView);
            emailTextView = itemView.findViewById(R.id.emailTextView);
            btnUpdate = itemView.findViewById(R.id.btnUpdate);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }

    public interface OnFacultateClickListener {
        void onUpdateClick(Facultate facultate);
        void onDeleteClick(Facultate facultate);
    }
}