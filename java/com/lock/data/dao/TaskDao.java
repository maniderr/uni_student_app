package com.lock.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.lock.data.model.TaskItem;

import java.util.List;

@Dao
public interface TaskDao {
    @Insert
    long insert(TaskItem taskItem);

    @Insert
    void insertAll(List<TaskItem> taskItems);

    @Query("SELECT *FROM taskItems")
    List<TaskItem> getAllTasks();

    @Query("SELECt *from taskItems WHERE id = :id")
    TaskItem getTaskById(long id);

    @Query("SELECT * FROM taskItems WHERE date = :date")
    List<TaskItem> getTasksByDate(String date);

    @Query("SELECT * FROM taskItems WHERE username = :username")
    List<TaskItem> getTasksByUsername(String username);

    @Query("SELECT * FROM taskItems WHERE username = :username AND date = :date")
    List<TaskItem> getTasksByUsernameAndDate(String username, String date);

    @Update
    void update(TaskItem taskItem);

    @Delete
    void delete(TaskItem taskItem);
}
