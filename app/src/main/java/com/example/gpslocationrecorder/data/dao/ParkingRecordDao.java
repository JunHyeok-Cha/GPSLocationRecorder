package com.example.gpslocationrecorder.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.gpslocationrecorder.data.entity.ParkingRecord;

import java.util.List;

@Dao
public interface ParkingRecordDao {

    @Insert
    long insert(ParkingRecord record);

    @Query("SELECT * FROM parking_records ORDER BY created_at DESC")
    List<ParkingRecord> getAll();

    @Delete
    void delete(ParkingRecord record);

    @Query("DELETE FROM parking_records")
    void deleteAll();

    // ★ [추가] 데이터 수정(업데이트) 명령
    @Update
    void update(ParkingRecord record);
}