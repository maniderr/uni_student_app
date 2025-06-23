package com.lock.facultate_info;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.lock.BuildConfig;

import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FacultateViewModel extends AndroidViewModel {
    private FacultateDAO facultateDAO;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private MutableLiveData<List<Facultate>> facultateList = new MutableLiveData<>();
    private String URL = "https://zugevozpasdvufmjpsoc.supabase.co/rest/v1/facultati";

    public FacultateViewModel(@NonNull Application application) {
        super(application);
        FacultateAppDatabase database = FacultateAppDatabase.getInstance(application);
        facultateDAO = database.facultateDao();
        loadFacultati();
    }

    public LiveData<List<Facultate>> getAllFacultati() {
        return facultateList;
    }

    private void loadFacultati() {
        executorService.execute(() -> facultateList.postValue(facultateDAO.getAllFacultati()));
    }

    public void insert(Facultate facultate) {
        executorService.execute(() -> {
            facultateDAO.insert(facultate);
            insertToSupabase(facultate);
            loadFacultati();
        });
    }

    public void update(Facultate facultate) {
        executorService.execute(() -> {
            facultateDAO.update(facultate);
            loadFacultati();
        });
    }

    public void deleteAll() {
        executorService.execute(() -> {
            facultateDAO.deleteAllFacultati();
            loadFacultati();
        });
    }

    public void delete(Facultate facultate) {
        executorService.execute(() -> {
            facultateDAO.deleteById(facultate.getId());
            loadFacultati();
        });
    }

    private void insertToSupabase(Facultate facultate) {
        executorService.execute(() -> {
            try {
                JSONObject jsonBody = new JSONObject();
                jsonBody.put("name", facultate.getName());
                jsonBody.put("site", facultate.getSite());
                jsonBody.put("address", facultate.getAddress());
                jsonBody.put("phone", facultate.getPhone());
                jsonBody.put("fax", facultate.getFax());
                jsonBody.put("email", facultate.getEmail());

                URL url = new URL(URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("apikey", BuildConfig.SUPABASE_API_KEY);
                conn.setRequestProperty("Authorization", "Bearer " + BuildConfig.SUPABASE_API_KEY);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                os.write(jsonBody.toString().getBytes("UTF-8"));
                os.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_CREATED || responseCode == HttpURLConnection.HTTP_OK) {
                    System.out.println("Entry successfully inserted into Supabase!");
                } else {
                    System.out.println("Failed to insert: " + responseCode);
                }
                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}