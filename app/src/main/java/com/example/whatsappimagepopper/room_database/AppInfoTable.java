package com.example.whatsappimagepopper.room_database;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "app_info")
public class AppInfoTable {
    @PrimaryKey
    @NonNull
    public String user_name;
    public String password;
    public String endpoint_name;

    public AppInfoTable() {
        user_name = "";
    }
}

