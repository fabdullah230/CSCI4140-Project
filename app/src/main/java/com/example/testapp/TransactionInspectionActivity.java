package com.example.testapp;

// TransactionInspectionActivity.java


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class TransactionInspectionActivity extends AppCompatActivity {

    private TransactionDao transactionDao;
    private AppDatabase database;
    private Transaction currentTransaction;
    private TextView commentsTextView;
    private ImageButton editCommentsButton;
    private EditText editCommentsEditText;

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
        Switch sharedSwitch = findViewById(R.id.sharedSwitch);
        EditText sharedAmountEditText = findViewById(R.id.sharedAmountEditText);
        commentsTextView = findViewById(R.id.commentsTextView);
        editCommentsButton = findViewById(R.id.editCommentsButton);
        editCommentsEditText = new EditText(this);
        Button deleteButton = findViewById(R.id.deleteButton);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.containsKey("transaction")) {
            currentTransaction = bundle.getParcelable("transaction");
            System.out.println("Received by inspection activity" + currentTransaction);
            if (currentTransaction != null) {
                titleTextView.setText(currentTransaction.getTitle());
                sourceTextView.setText("Source: " + currentTransaction.getSource());
                amountTextView.setText("Amount: HK$" + currentTransaction.getAmount());
                SimpleDateFormat sdf = new SimpleDateFormat("EEE dd MMM yyyy h:mm a", Locale.getDefault());
                timestampTextView.setText("Created at: " + sdf.format(currentTransaction.getTimestamp()));
                commentsTextView.setText(currentTransaction.getComments());
            }
        }

        sharedSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                sharedAmountEditText.setVisibility(View.VISIBLE);
            } else {
                sharedAmountEditText.setVisibility(View.GONE);
            }
        });

        editCommentsButton.setOnClickListener(v -> {
            showEditCommentsDialog();
        });

        sharedAmountEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No implementation needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // No implementation needed
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Update shared amount in the currentTransaction object
                if (currentTransaction != null) {
                    String sharedAmountStr = s.toString();
                    if (!TextUtils.isEmpty(sharedAmountStr)) {
                        currentTransaction.setSharedAmount(sharedAmountStr);
                    }
                }
            }
        });

        deleteButton.setOnClickListener(v -> {
            showDeleteConfirmationDialog();
        });
    }

    private void showEditCommentsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Comments")
                .setView(editCommentsEditText)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newComments = editCommentsEditText.getText().toString().trim();
                    commentsTextView.setText(newComments);

                    // Update comments in the currentTransaction object
                    if (currentTransaction != null) {
                        currentTransaction.setComments(newComments);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();

        // Set the original comment as the filled text in the edit text
        if (currentTransaction != null) {
            String originalComment = currentTransaction.getComments();
            if (originalComment != null) {
                editCommentsEditText.setText(originalComment);
                editCommentsEditText.setSelection(originalComment.length());
            } else {
                editCommentsEditText.setText("");  // Ensure the field is empty if there is no comment
            }
        }
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Transaction")
                .setMessage("Are you sure you want to delete this transaction?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (currentTransaction != null) {
                        new Thread(() -> {
                            transactionDao.deleteById(currentTransaction.getId());
                        }).start();
                    }
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}