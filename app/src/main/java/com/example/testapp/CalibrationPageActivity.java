package com.example.testapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class CalibrationPageActivity  extends AppCompatActivity {

    private TransactionAdapter adapter;
    private RecyclerView transactionsRecyclerView;

    private AppCalibrateDatabase calibrateDatabase;
    private TransactionDao transactionDao;
    GlobalState state = GlobalState.getInstance();


    private List<NotificationSource> notificationSources = new ArrayList<>();
    private NotificationSourceAdapter notificationSourceAdapter;
    private RecyclerView notificationSourceRecyclerView;

    TextView message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibration_page);

        transactionsRecyclerView = findViewById(R.id.transactionsRecyclerView);
        transactionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize and attach the adapter to the RecyclerView
        adapter = new TransactionAdapter(new ArrayList<>(), this);
        transactionsRecyclerView.setAdapter(adapter);

        calibrateDatabase = AppCalibrateDatabase.getDatabase(this);
        transactionDao = calibrateDatabase.transactionDao();

        SwitchCompat calibrateModeSwitch = findViewById(R.id.calibrateModeSwitch);
        message = findViewById(R.id.calibrationModeMessage);

        if (state.getCalibrateMode()) {
            message.setText("Captured notifications in calibration mode");
            transactionsRecyclerView.setVisibility(View.VISIBLE);
            calibrateModeSwitch.setChecked(true);
        } else {
            message.setText("Turn on to register new Transaction source");
            transactionsRecyclerView.setVisibility(View.INVISIBLE);
            calibrateModeSwitch.setChecked(false);
        }

        fetchCalibrateTransactions();


        notificationSourceRecyclerView = findViewById(R.id.notificationSourcesRecyclerView);
        notificationSourceRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        notificationSourceAdapter = new NotificationSourceAdapter(this, notificationSources);
//        notificationSourceAdapter.setClickListener(this);
        notificationSourceRecyclerView.setAdapter(notificationSourceAdapter);



        calibrateModeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                state.setCalibrateMode(isChecked);
                // Perform actions based on the state of the switch
                if (isChecked) {
                    message.setText("Captured notifications in calibration mode");
                    transactionsRecyclerView.setVisibility(View.VISIBLE);
                } else {
                    message.setText("Turn on to register new Transaction source");
                    transactionsRecyclerView.setVisibility(View.INVISIBLE);
                }


                String message1;
                if (isChecked) {
                    message1 = "Calibration mode on";
                } else {
                    message1 = "Calibration mode off";
                }

                // Create a custom toast layout
                LayoutInflater inflater = getLayoutInflater();
                View toastView = inflater.inflate(R.layout.toast_custom, null);

                // Set the message text
                TextView toastText = toastView.findViewById(R.id.toastText);
                toastText.setText(message1);

                // Create and show the custom toast
                Toast customToast = new Toast(CalibrationPageActivity.this);
                customToast.setDuration(Toast.LENGTH_SHORT);
                customToast.setView(toastView);
                customToast.show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchCalibrateTransactions();
    }

    private void fetchCalibrateTransactions() {
        new Thread(() -> {
            // Simulate network delay
            List<Transaction> transactions = new ArrayList<>();
            try {
                transactions = transactionDao.getAllTransactions();
            } catch (Exception e) {
                System.out.println("Error fetching data.");
            }

            List<Transaction> finalTransactions = transactions;
            runOnUiThread(() -> adapter.updateTransactions(finalTransactions));
        }).start();
    }
}