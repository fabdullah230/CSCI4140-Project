package com.example.testapp;

// TransactionInspectionActivity.java

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class TransactionInspectionActivity extends AppCompatActivity {

    private TransactionDao transactionDao;
    private AppDatabase database;
    private Transaction currentTransaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_inspection);
        database = AppDatabase.getDatabase(this);
        transactionDao = database.transactionDao(); // Replace with your actual TransactionDao implementation

        TextView titleTextView = findViewById(R.id.titleTextView);
        TextView sourceTextView = findViewById(R.id.sourceTextView);
        TextView amountTextView = findViewById(R.id.amountTextView);
        TextView timestampTextView = findViewById(R.id.timestampTextView);
        Button deleteButton = findViewById(R.id.deleteButton);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.containsKey("transaction")) {
            currentTransaction = bundle.getParcelable("transaction");
            if (currentTransaction != null) {
                titleTextView.setText( currentTransaction.getTitle());
                sourceTextView.setText("Source: " +currentTransaction.getSource());
                amountTextView.setText("Amount: HK$" + currentTransaction.getAmount());
                SimpleDateFormat sdf = new SimpleDateFormat("EEE dd MMM yyyy h:mm a", Locale.getDefault());
                timestampTextView.setText("Created at: " + sdf.format(currentTransaction.getTimestamp()));
            }
        }

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteConfirmationDialog();
            }
        });
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Transaction")
                .setMessage("Are you sure you want to delete this transaction?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (currentTransaction != null) {
                            new Thread(() -> {
                                transactionDao.deleteById(currentTransaction.getId());
                            }).start();

                        }
                        finish();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}