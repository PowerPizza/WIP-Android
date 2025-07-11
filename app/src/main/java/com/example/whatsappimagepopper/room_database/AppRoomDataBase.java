package com.example.whatsappimagepopper.room_database;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.InvalidationTracker;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {AppInfoTable.class}, version = 1)
public abstract class AppRoomDataBase extends RoomDatabase {
    public static volatile AppRoomDataBase Instance = null;
    public static synchronized AppRoomDataBase getInstance(Context context) {
        AppRoomDataBase instance_cpy = Instance;
        if (instance_cpy == null){
            synchronized (AppRoomDataBase.class) {
                instance_cpy = Instance;
                if (instance_cpy == null){
                    instance_cpy = Instance = Room.databaseBuilder(context, AppRoomDataBase.class, "app_info_db").build();
                }
            }
        }
        return instance_cpy;
    }

    public abstract AppInfoDao appInfoDao();
}
