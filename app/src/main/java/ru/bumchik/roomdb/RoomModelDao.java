package ru.bumchik.roomdb;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface RoomModelDao {
    @Query("SELECT * FROM roommodel")
    abstract List<RoomModel> getAll();

    @Query("SELECT * FROM roommodel WHERE userId LIKE :name LIMIT 1")
    RoomModel findByUserId(String name);

    @Insert
    void insertAll(List<RoomModel> item);

    @Update
    void update(RoomModel item);

    @Delete
    void delete(RoomModel item);

    @Query("DELETE FROM roommodel")
    void deleteAll();
}
