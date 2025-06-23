package com.lock.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.lock.data.model.AiMessage;

import java.util.List;

@Dao
public interface MessageDAO {
    @Insert
    void insert(AiMessage message);

    @Query("SELECT * FROM AiMessages WHERE username = :username ORDER BY timestamp ASC")
    List<AiMessage> getMessagesByUser(String username);

    @Query("DELETE FROM AiMessages WHERE username = :username")
    void deleteMessagesByUser(String username);
}