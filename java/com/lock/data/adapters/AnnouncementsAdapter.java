package com.lock.data.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lock.R;
import com.lock.data.model.Announcement;

import java.util.List;

public class AnnouncementsAdapter extends RecyclerView.Adapter<AnnouncementsAdapter.AnnouncementViewHolder> {
    private List<Announcement> announcements;
    private OnDeleteClickListener deleteClickListener;
    private static boolean showDeleteButton;

    public interface OnDeleteClickListener {
        void onDeleteClick(long announcementId);
    }

    public AnnouncementsAdapter(List<Announcement> announcements, OnDeleteClickListener listener, boolean showDeleteButton) {
        this.announcements = announcements;
        this.deleteClickListener = listener;
        this.showDeleteButton = showDeleteButton;
    }

    @NonNull
    @Override
    public AnnouncementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_announcement, parent, false);
        return new AnnouncementViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AnnouncementViewHolder holder, int position) {
        Announcement announcement = announcements.get(position);
        holder.textView.setText(announcement.getAncmnt_text());
        holder.dateView.setText(announcement.getAncmnt_date());

        if (showDeleteButton && deleteClickListener != null) {
            holder.deleteButton.setOnClickListener(v -> {
                deleteClickListener.onDeleteClick(announcement.getId());
            });
        }
    }

    @Override
    public int getItemCount() {
        return announcements != null ? announcements.size() : 0;
    }

    public void setAnnouncements(List<Announcement> announcements) {
        this.announcements = announcements;
        notifyDataSetChanged();
    }

    static class AnnouncementViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        TextView dateView;
        ImageButton deleteButton;

        public AnnouncementViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.announcement_text);
            dateView = itemView.findViewById(R.id.announcement_date);
            deleteButton = itemView.findViewById(R.id.delete_button);

            deleteButton.setVisibility(showDeleteButton ? View.VISIBLE : View.GONE);
        }
    }
}