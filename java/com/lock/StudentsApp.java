package com.lock;

import android.app.Application;

import com.lock.data.dao.CourseDao;
import com.lock.data.dao.UserDao;
import com.lock.data.database.AppDatabase;

import java.util.concurrent.Executors;

public class StudentsApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AppDatabase db = AppDatabase.getInstance(this);
        UserDao userDao = db.userDao();
        Executors.newSingleThreadExecutor().execute(userDao::getAllUsers);
    }

}
