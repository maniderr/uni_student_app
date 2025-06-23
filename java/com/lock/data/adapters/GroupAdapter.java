package com.lock.data.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lock.R;
import com.lock.utils.enums.UserGroup;

import java.util.ArrayList;
import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder> {
    private List<UserGroup> groups;
    private List<UserGroup> selectedGroups = new ArrayList<>();

    public GroupAdapter(List<UserGroup> groups) {
        this.groups = groups;
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_group_checkbox, parent, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        UserGroup group = groups.get(position);
        holder.checkBox.setText(group.name().replace("_", " "));
        holder.checkBox.setChecked(selectedGroups.contains(group));

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (!selectedGroups.contains(group)) {
                    selectedGroups.add(group);
                }
            } else {
                selectedGroups.remove(group);
            }
        });
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    public List<UserGroup> getSelectedGroups() {
        return selectedGroups;
    }

    public void setSelectedGroups(List<UserGroup> selectedGroups) {
        this.selectedGroups = selectedGroups;
        notifyDataSetChanged();
    }

    static class GroupViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.groupCheckBox);
        }
    }
}
