package com.lock.data.adapters;

import android.app.usage.UsageStats;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.lock.R;

import java.text.DateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class UsageStatsAdapter extends BaseAdapter {
    private static final String TAG = "UsageStatsAdapter";
    private static final int _DISPLAY_ORDER_USAGE_TIME = 0;
    private static final int _DISPLAY_ORDER_LAST_TIME_USED = 1;
    private static final int _DISPLAY_ORDER_APP_NAME = 2;
    private static final int _DISPLAY_ORDER_BATTERY_DRAIN = 3;
    private long totalScreenOnTime = 1;

    private int mDisplayOrder = _DISPLAY_ORDER_USAGE_TIME;
    private final LayoutInflater mInflater;
    private final PackageManager mPm;
    private final List<UsageStats> mPackageStats;
    private final Map<String, String> mAppLabelMap = new HashMap<>();

    public UsageStatsAdapter(LayoutInflater inflater, PackageManager pm, List<UsageStats> stats) {
        this.mInflater = inflater;
        this.mPm = pm;
        this.mPackageStats = stats;
        this.totalScreenOnTime = calculateTotalScreenTime(stats);
        loadAppLabels();
    }

    private long calculateTotalScreenTime(List<UsageStats> stats) {
        long total = 0;
        for (UsageStats stat : stats) {
            total += stat.getTotalTimeInForeground();
        }
        return total > 0 ? total : 1;
    }

    private double calculateBatteryDrain(long appUsageTime) {
        double estimatedTotalBatteryUsed = 15.0;
        return (appUsageTime / (double) totalScreenOnTime) * estimatedTotalBatteryUsed;
    }

    private void loadAppLabels() {
        for (UsageStats pkgStats : mPackageStats) {
            try {
                String label = mPm.getApplicationLabel(mPm.getApplicationInfo(pkgStats.getPackageName(), 0)).toString();
                mAppLabelMap.put(pkgStats.getPackageName(), label);
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, "Package not found: " + pkgStats.getPackageName(), e);
            }
        }
    }

    @Override
    public int getCount() {
        return mPackageStats.size();
    }

    @Override
    public Object getItem(int position) {
        return mPackageStats.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void updateData(List<UsageStats> stats, double totalMinutes) {
        this.mPackageStats.clear();
        this.mPackageStats.addAll(stats);
        this.mAppLabelMap.clear();
        loadAppLabels();
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AppViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_usage_stats, parent, false);
            holder = new AppViewHolder();
            holder.appIcon = convertView.findViewById(R.id.app_icon);
            holder.pkgName = convertView.findViewById(R.id.package_name);
            holder.lastTimeUsed = convertView.findViewById(R.id.last_time_used);
            holder.usageTime = convertView.findViewById(R.id.usage_time);
            holder.batteryDrain = convertView.findViewById(R.id.battery_drain);
            convertView.setTag(holder);
        } else {
            holder = (AppViewHolder) convertView.getTag();
        }

        UsageStats pkgStats = mPackageStats.get(position);
        if (pkgStats != null) {
            String label = mAppLabelMap.get(pkgStats.getPackageName());
            holder.pkgName.setText(label);

            Date lastUsed = new Date(pkgStats.getLastTimeUsed());
            DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);
            holder.lastTimeUsed.setText(dateFormat.format(lastUsed));

            long totalSeconds = pkgStats.getTotalTimeInForeground() / 1000;
            holder.usageTime.setText(formatElapsedTime(totalSeconds));

            try {
                holder.appIcon.setImageDrawable(mPm.getApplicationIcon(pkgStats.getPackageName()));
            } catch (PackageManager.NameNotFoundException e) {
                holder.appIcon.setImageDrawable(null);
            }
            double batteryDrain = calculateBatteryDrain(pkgStats.getTotalTimeInForeground());
            holder.batteryDrain.setText(String.format("%.1f%%", batteryDrain));
        }

        return convertView;
    }

    private String formatElapsedTime(long totalSeconds) {
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        if (hours > 0) {
            return String.format(Locale.getDefault(), "%dh %02dm", hours, minutes);
        } else {
            return String.format(Locale.getDefault(), "%dm %02ds", minutes, seconds);
        }
    }

    public void sortList(int sortOrder) {
        if (mDisplayOrder == sortOrder) return;
        mDisplayOrder = sortOrder;
        sortList();
    }

    private void sortList() {
        switch (mDisplayOrder) {
            case _DISPLAY_ORDER_USAGE_TIME:
                Collections.sort(mPackageStats, new UsageTimeComparator());
                break;
            case _DISPLAY_ORDER_LAST_TIME_USED:
                Collections.sort(mPackageStats, new LastTimeUsedComparator());
                break;
            case _DISPLAY_ORDER_APP_NAME:
                Collections.sort(mPackageStats, new AppNameComparator(mAppLabelMap));
                break;
            case _DISPLAY_ORDER_BATTERY_DRAIN:
                Collections.sort(mPackageStats, new BatteryDrainComparator(mAppLabelMap, totalScreenOnTime));
                break;
        }
        notifyDataSetChanged();
    }

    static class AppViewHolder {
        ImageView appIcon;
        TextView pkgName;
        TextView lastTimeUsed;
        TextView usageTime;
        TextView batteryDrain;
    }

    public static class LastTimeUsedComparator implements Comparator<UsageStats> {
        @Override
        public int compare(UsageStats a, UsageStats b) {
            return Long.compare(b.getLastTimeUsed(), a.getLastTimeUsed());
        }
    }

    public static class UsageTimeComparator implements Comparator<UsageStats> {
        @Override
        public int compare(UsageStats a, UsageStats b) {
            return Long.compare(b.getTotalTimeInForeground(), a.getTotalTimeInForeground());
        }
    }

    public static class BatteryDrainComparator implements Comparator<UsageStats> {
        private final Map<String, String> appLabelMap;
        private final long totalScreenOnTime;

        public BatteryDrainComparator(Map<String, String> appLabelMap, long totalScreenOnTime) {
            this.appLabelMap = appLabelMap;
            this.totalScreenOnTime = totalScreenOnTime;
        }

        @Override
        public int compare(UsageStats a, UsageStats b) {
            double drainA = (a.getTotalTimeInForeground() / (double) totalScreenOnTime) * 100;
            double drainB = (b.getTotalTimeInForeground() / (double) totalScreenOnTime) * 100;
            return Double.compare(drainB, drainA);
        }
    }

    public static class AppNameComparator implements Comparator<UsageStats> {
        private final Map<String, String> appLabelMap;

        public AppNameComparator(Map<String, String> appLabelMap) {
            this.appLabelMap = appLabelMap;
        }

        @Override
        public int compare(UsageStats a, UsageStats b) {
            return appLabelMap.get(a.getPackageName()).compareTo(appLabelMap.get(b.getPackageName()));
        }
    }
}

