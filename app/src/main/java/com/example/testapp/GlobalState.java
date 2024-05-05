package com.example.testapp;

import android.content.Context;
import android.content.SharedPreferences;

public class GlobalState {
    private static GlobalState instance;
    private boolean calibrateMode;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private GlobalState(Context context) {
        sharedPreferences = context.getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public static synchronized GlobalState getInstance(Context context) {
        if (instance == null) {
            instance = new GlobalState(context);
        }
        return instance;
    }

    public boolean getCalibrateMode() {
        return sharedPreferences.getBoolean("calibrate_mode", false);
    }

    public void setCalibrateMode(boolean calibrateMode) {
        editor.putBoolean("calibrate_mode", calibrateMode);
        editor.apply();
    }


}


