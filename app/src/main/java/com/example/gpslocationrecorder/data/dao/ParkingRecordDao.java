package com.example.gpslocationrecorder.data.dao;

import androidx.room.Dao;
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
}
