package com.example.gpslocationrecorder.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

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

    // ★ [추가] 데이터 싹 다 지우기 명령
    @Query("DELETE FROM parking_records")
    void deleteAll();
}
