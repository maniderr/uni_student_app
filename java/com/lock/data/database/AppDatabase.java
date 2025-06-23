package com.lock.data.database;

import android.content.Context;
import android.util.Log;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.lock.data.model.AiMessage;
import com.lock.data.dao.MessageDAO;
import com.lock.data.dao.CourseDao;
import com.lock.data.dao.TaskDao;
import com.lock.data.dao.UserDao;
import com.lock.data.model.Course;
import com.lock.data.model.User;
import com.lock.data.model.TaskItem;

@Database(entities = {User.class, Course.class, TaskItem.class, AiMessage.class}, version = 12, exportSchema = false)
@TypeConverters({CourseDao.Converters.class})
public abstract class AppDatabase extends RoomDatabase {

    public abstract UserDao userDao();
    public abstract CourseDao courseDao();
    public abstract TaskDao taskDao();
    public abstract MessageDAO messageDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    Log.d("DatabaseDebug", "Creating database instance");
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "app_database"
                            )
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        } else {
            Log.d("DatabaseDebug", "Database instance already exists");
        }
        return INSTANCE;
    }

}
