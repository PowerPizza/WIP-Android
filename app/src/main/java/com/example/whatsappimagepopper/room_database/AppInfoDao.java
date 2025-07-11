package com.example.whatsappimagepopper.room_database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.sql.Struct;
import java.util.List;

@Dao
public interface AppInfoDao {
    @Query("Select * FROM app_info")
    List<AppInfoTable> getAll();

    @Insert
    long insertOne(AppInfoTable app_infos);

    @Query("Select * FROM app_info WHERE user_name=:username AND password=:pass")
    AppInfoTable findByUsernamePassword(String username, String pass);

    @Query("DELETE FROM app_info WHERE user_name=:username AND password=:pass")
    int deleteByUsernamePassword(String username, String pass);

    @Query("UPDATE app_info SET user_name=:username AND password=:pass WHERE user_name=:old_username")
    int updateByUsername(String old_username, String pass, String username);

    @Query("SELECT * FROM app_info LIMIT 1")
    List<AppInfoTable> selectFirstRow();

    @Query("DELETE FROM app_info")
    int clearTable();
}