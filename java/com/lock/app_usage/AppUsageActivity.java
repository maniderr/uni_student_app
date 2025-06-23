package com.lock.app_usage;

import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.lock.R;
import com.lock.data.adapters.UsageStatsAdapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppUsageActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, View.OnClickListener {

    private static final String TAG = "UsageStatsActivity";
    private UsageStatsManager mUsageStatsManager;
    private LayoutInflater mInflater;
    private UsageStatsAdapter mAdapter;
    private PackageManager mPm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_usage_stats);

        mUsageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        requestPermissions();

        mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mPm = getPackageManager();

        ImageButton refreshButton = findViewById(R.id.refreshButton);
        refreshButton.setOnClickListener(this);

        Spinner typeSpinner = findViewById(R.id.typeSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.sort_options,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(adapter);
        typeSpinner.setOnItemSelectedListener(this);

        ListView listView = findViewById(R.id.pkg_list);

        List<UsageStats> usageStatsList = getUsageStats();
        mAdapter = new UsageStatsAdapter(mInflater, mPm, usageStatsList);
        listView.setAdapter(mAdapter);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.refreshButton) {
            refreshUsageStats();
        }
    }

    private void refreshUsageStats() {
        List<UsageStats> stats = getUsageStats();
        mAdapter = new UsageStatsAdapter(mInflater, mPm, stats);
        ListView listView = findViewById(R.id.pkg_list);
        listView.setAdapter(mAdapter);
    }

    private void requestPermissions() {
        boolean hasPermission = checkUsageStatsPermission();
        if (!hasPermission) {
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
            Toast.makeText(this, "Please grant usage access permission", Toast.LENGTH_LONG).show();
        }
    }

    private boolean checkUsageStatsPermission() {
        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        mAdapter.sortList(position);
    }

    private List<UsageStats> getUsageStats() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -30);

        List<UsageStats> allStats = mUsageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_BEST, cal.getTimeInMillis(), System.currentTimeMillis()
        );

        List<UsageStats> filteredStats = new ArrayList<>();
        Map<String, UsageStats> mostRecentStats = new HashMap<>();

        for (UsageStats stats : allStats) {
            try {
                ApplicationInfo appInfo = mPm.getApplicationInfo(stats.getPackageName(), 0);
                String appName = mPm.getApplicationLabel(appInfo).toString();

                if (appName.isEmpty()) continue;

                Intent launchIntent = getPackageManager().getLaunchIntentForPackage(stats.getPackageName());
                if (launchIntent != null && stats.getTotalTimeInForeground() > 0) {
                    UsageStats existing = mostRecentStats.get(stats.getPackageName());
                    if (existing == null || stats.getLastTimeUsed() > existing.getLastTimeUsed()) {
                        mostRecentStats.put(stats.getPackageName(), stats);
                    }
                }
            } catch (PackageManager.NameNotFoundException e) {
                // Ignore
            }
        }
        filteredStats.addAll(mostRecentStats.values());
        return filteredStats;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // do nothing
    }
}