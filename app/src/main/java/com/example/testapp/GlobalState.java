package com.example.testapp;

import android.content.Context;
import android.content.SharedPreferences;

public class GlobalState {
    private static GlobalState instance;
    private boolean calibrateMode;
//    private SharedPreferences sharedPreferences;
//    private SharedPreferences.Editor editor;

    private NotificationSourceDatabase database;

    private GlobalState(Context context) {
//        sharedPreferences = context.getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
//        editor = sharedPreferences.edit();
        database = NotificationSourceDatabase.getDatabase(context);
    }

    public static synchronized GlobalState getInstance(Context context) {
        if (instance == null) {
            instance = new GlobalState(context);
        }
        return instance;
    }

    public boolean getCalibrateMode() {
        return calibrateMode;
    }

    public void setCalibrateMode(boolean calibrateMode) {
        this.calibrateMode = calibrateMode;
    }

    public NotificationSourceDatabase getDatabase() {
        return database;
    }
}


