package com.example.testapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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

    public void addSource(View view){
        //check here whether it alr exists
        NotificationSource source = new NotificationSource(currentTransaction.getSource(), nameEdit.getText().toString(), shouldContainEdit.getText().toString());
        new Thread(() -> {
            notificationSourcesDao.insert(source);
        }).start();
        finish();
    }








}
