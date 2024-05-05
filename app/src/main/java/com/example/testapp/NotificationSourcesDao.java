package com.example.testapp;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface NotificationSourcesDao {

    @Insert
    void insert(NotificationSource notificationSource);

    @Query("SELECT * FROM sources")
    List<NotificationSource> getAllNotificationSources();

    @Query("UPDATE sources SET name = :name WHERE packageName = :packageName")
    void editNameByPackageName(String packageName, String name);

    @Query("DELETE FROM sources WHERE packageName = :packageName")
    void deleteByPackageName(String packageName);

    @Query("UPDATE sources SET shouldContain = :shouldContain WHERE packageName = :packageName")
    void editShouldContainByPackageName(String packageName, String shouldContain);

    @Query("SELECT * FROM sources WHERE packageName = :packageName")
    NotificationSource getNotificationSourceByPackageName(String packageName);


}
