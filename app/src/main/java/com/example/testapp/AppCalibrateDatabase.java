package com.example.testapp;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Transaction.class}, version = 1)
public abstract class AppCalibrateDatabase extends RoomDatabase {
    public abstract TransactionDao transactionDao();

    private static volatile AppCalibrateDatabase INSTANCE;

    static AppCalibrateDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppCalibrateDatabase.class, "app_calibrate_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
