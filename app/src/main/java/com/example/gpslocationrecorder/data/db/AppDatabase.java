package com.example.gpslocationrecorder.data.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.gpslocationrecorder.data.dao.ParkingRecordDao;
import com.example.gpslocationrecorder.data.entity.ParkingRecord;

@Database(
        entities = {ParkingRecord.class},
        version = 1,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract ParkingRecordDao parkingRecordDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "parking_record_db"
                            )
                            // .fallbackToDestructiveMigration() // 스키마 바꿀 때 편하게 싹 밀고 싶으면 추가
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
