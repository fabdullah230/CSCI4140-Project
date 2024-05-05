package com.example.testapp;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {NotificationSource.class}, version = 1)
public abstract class NotificationSourceDatabase extends RoomDatabase {
    public abstract NotificationSourcesDao notificationSourcesDao();

    private static volatile NotificationSourceDatabase INSTANCE;

    static NotificationSourceDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (NotificationSourceDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    NotificationSourceDatabase.class, "notification_source_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
