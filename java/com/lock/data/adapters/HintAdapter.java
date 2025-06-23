package com.lock.data.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.lock.R;
import com.lock.utils.enums.Role;
import com.lock.utils.enums.Faculty;
import com.lock.utils.enums.UserGroup;
import com.lock.utils.enums.Year;

public class HintAdapter<T> extends ArrayAdapter<T> {
    private String hint;
    private LayoutInflater inflater;
    private boolean isHintDisabled = true;

    public HintAdapter(Context context, int resource, T[] objects, String hint) {
        super(context, resource, objects);
        this.hint = hint;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (position == 0) {
            TextView view = (TextView) inflater.inflate(R.layout.item_register_spinner_dropdown, parent, false);
            view.setText(hint);
            view.setTextColor(ContextCompat.getColor(getContext(), android.R.color.darker_gray));
            return view;
        }

        View view = super.getView(position, convertView, parent);
        if (view instanceof TextView) {
            T item = getItem(position);
            ((TextView) view).setText(getDisplayValue(item));
            ((TextView) view).setTextColor(ContextCompat.getColor(getContext(), android.R.color.black));
        }
        return view;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if (position == 0) {
            TextView view = (TextView) inflater.inflate(R.layout.item_register_spinner_dropdown, parent, false);
            view.setText(hint);
            view.setTextColor(ContextCompat.getColor(getContext(), android.R.color.darker_gray));
            view.setBackgroundResource(R.drawable.register_spinner_hint_background);
            return view;
        }

        View view = super.getDropDownView(position, convertView, parent);
        if (view instanceof TextView) {
            T item = getItem(position);
            ((TextView) view).setText(getDisplayValue(item));
            ((TextView) view).setTextColor(ContextCompat.getColor(getContext(), android.R.color.black));
            view.setBackgroundResource(R.drawable.register_spinner_item_background);
        }
        return view;
    }

    private String getDisplayValue(T item) {
        if (item == null) {
            return "";
        }
        if (item instanceof Faculty) {
            return ((Faculty) item).getDisplayName();
        } else if (item instanceof Year) {
            return String.valueOf(((Year) item).getValue());
        } else if (item instanceof UserGroup) {
            return String.valueOf(((UserGroup) item).getValue());
        } else if (item instanceof Role) {
            return String.valueOf(((Role) item).getValue());
        }
        return item.toString();
    }

    @Override
    public int getCount() {
        return super.getCount() + (isHintDisabled ? 1 : 0);
    }

    @Override
    public T getItem(int position) {
        return position == 0 && isHintDisabled ? null : super.getItem(position - (isHintDisabled ? 1 : 0));
    }

    @Override
    public long getItemId(int position) {
        return position == 0 && isHintDisabled ? -1 : super.getItemId(position - (isHintDisabled ? 1 : 0));
    }

    @Override
    public boolean isEnabled(int position) {
        return !(position == 0 && isHintDisabled);
    }
}
