package com.example.testapp;

public class GlobalState {
    private static GlobalState instance;
    private boolean calibrateMode;


    public boolean getCalibrateMode() {
        return calibrateMode;
    }

    public void setCalibrateMode(boolean calibrateMode) {
        this.calibrateMode = calibrateMode;
    }

    private GlobalState() {
        // Private constructor to prevent instantiation
    }

    public static synchronized GlobalState getInstance() {
        if (instance == null) {
            instance = new GlobalState();
        }
        return instance;
    }


}