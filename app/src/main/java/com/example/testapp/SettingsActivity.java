package com.example.testapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

public class SettingsActivity extends AppCompatActivity {

    GlobalState state = GlobalState.getInstance(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        SwitchCompat calibrateModeSwitch = findViewById(R.id.calibrateModeSwitch);

        calibrateModeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                state.setCalibrateMode(isChecked);
                // Perform actions based on the state of the switch

                String message;
                if (isChecked) {
                    message = "Calibration mode on";
                } else {
                    message = "Calibration mode off";
                }

                // Create a custom toast layout
                LayoutInflater inflater = getLayoutInflater();
                View toastView = inflater.inflate(R.layout.toast_custom, null);

                // Set the message text
                TextView toastText = toastView.findViewById(R.id.toastText);
                toastText.setText(message);

                // Create and show the custom toast
                Toast customToast = new Toast(SettingsActivity.this);
                customToast.setDuration(Toast.LENGTH_SHORT);
                customToast.setView(toastView);
                customToast.show();
            }
        });
    }
}