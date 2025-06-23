package com.lock.facultate_info;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface FacultateDAO {
    @Insert
    void insert(Facultate facultate);

    @Query("SELECT * FROM facultati ORDER BY id ASC")
    List<Facultate> getAllFacultati();

    @Query("DELETE FROM facultati")
    void deleteAllFacultati();

    @Update
    void update(Facultate facultate);

    @Query("DELETE FROM facultati WHERE id = :id")
    void deleteById(long id);
}