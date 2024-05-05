package com.example.testapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class NotificationSourceRegistrationActivity extends AppCompatActivity {

    Transaction currentTransaction;

    TextView titleLabel;
    TextView sourceLabel;
    EditText nameEdit;
    EditText shouldContainEdit;
    Button addButton;

    private NotificationSourceDatabase database;
    private NotificationSourcesDao notificationSourcesDao;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_source_registration);

        titleLabel = findViewById(R.id.titleTextView);
        sourceLabel = findViewById(R.id.sourceTextView);
        nameEdit = findViewById(R.id.editName);
        shouldContainEdit = findViewById(R.id.editShouldContain);

        database = NotificationSourceDatabase.getDatabase(this);
        notificationSourcesDao = database.notificationSourcesDao();


        Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.containsKey("transaction")) {
            currentTransaction = bundle.getParcelable("transaction");
            titleLabel.setText(currentTransaction.getTitle());
            sourceLabel.setText("Package name: " + currentTransaction.getSource());
        }
    }

    public void addSource(View view) {
        new Thread(() -> {
            NotificationSource existingSource = notificationSourcesDao.getNotificationSourceByPackageName(currentTransaction.getSource());
            if (existingSource != null) {
                runOnUiThread(() -> {
                    // Handle the case when the source already exists (e.g., show an error message)
                    LayoutInflater inflater = getLayoutInflater();
                    View toastView = inflater.inflate(R.layout.toast_custom, null);

                    // Set the message text
                    TextView toastText = toastView.findViewById(R.id.toastText);
                    toastText.setText("Source already registered!");

                    // Create and show the custom toast
                    Toast customToast = new Toast(NotificationSourceRegistrationActivity.this);
                    customToast.setDuration(Toast.LENGTH_SHORT);
                    customToast.setView(toastView);
                    customToast.show();
                });
            } else {
                NotificationSource source = new NotificationSource(currentTransaction.getSource(), nameEdit.getText().toString(), shouldContainEdit.getText().toString());
                notificationSourcesDao.insert(source);
            }
        }).start();
        finish();
    }








}
