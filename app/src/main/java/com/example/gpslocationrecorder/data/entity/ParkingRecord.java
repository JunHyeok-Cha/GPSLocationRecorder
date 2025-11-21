package com.example.gpslocationrecorder.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "parking_records")
public class ParkingRecord {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "latitude")
    public double latitude;

    @ColumnInfo(name = "longitude")
    public double longitude;

    @ColumnInfo(name = "photo_path")
    public String photoPath;

    @ColumnInfo(name = "floor")
    public String floor;

    @ColumnInfo(name = "memo")
    public String memo;

    @ColumnInfo(name = "created_at")
    public long createdAt;   // System.currentTimeMillis()


    // Room 이 사용할 기본 생성자 (no-arg)
    public ParkingRecord() {
    }

    public ParkingRecord(
            double latitude,
            double longitude,
            String photoPath,
            String floor,
            String memo,
            long createdAt
    ) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.photoPath = photoPath;
        this.floor = floor;
        this.memo = memo;
        this.createdAt = createdAt;
    }
}
