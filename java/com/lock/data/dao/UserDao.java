package com.lock.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.lock.data.model.User;

import java.util.List;

@Dao
public interface UserDao {

    @Insert
    void insert(User user);

    @Insert
    void insertAll(List<User> users);

    @Query("SELECT * FROM users")
    List<User> getAllUsers();

    @Query("SELECT * FROM users WHERE id = :userId")
    User getUserById(long userId);

    @Query("SELECT * FROM users WHERE username = :username AND password = :password LIMIT 1")
    User getUserByUsernameAndPassword(String username, String password);

    @Update
    void update(User user);

    @Delete
    void delete(User user);

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    User getUserByUsername(String username);
}
