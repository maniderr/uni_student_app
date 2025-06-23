package com.lock.facultate_info;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;

@Database(entities = {Facultate.class}, version = 2, exportSchema = false)
public abstract class FacultateAppDatabase extends RoomDatabase {
    private static com.lock.facultate_info.FacultateAppDatabase instance;

    public abstract FacultateDAO facultateDao();

    public static synchronized com.lock.facultate_info.FacultateAppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            com.lock.facultate_info.FacultateAppDatabase.class, "facultate_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}