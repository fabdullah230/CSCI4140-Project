package com.example.testapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;


public class HistoryActivity extends AppCompatActivity {
    private TransactionAdapter adapter;
    private RecyclerView transactionsRecyclerView;

    private TransactionDao transactionDao;
    private AppDatabase database;
    private Spinner sourceSpinner;

    private Button datePickerButton;
    private Button beforeAfterToggle;
    private boolean timeBefore = true;
    private String source = "All";

    private long selectedTime = 0;

    @Override
    protected void onResume() {
        super.onResume();
        fetchTransactionsWithSourceAndTimeBeforeAfter();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        sourceSpinner = findViewById(R.id.sourceSpinner);

        transactionsRecyclerView = findViewById(R.id.transactionsRecyclerView);
        transactionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new TransactionAdapter(new ArrayList<>(), this);
        transactionsRecyclerView.setAdapter(adapter);

        database = AppDatabase.getDatabase(this);
        transactionDao = database.transactionDao();



//        Button refreshButton = findViewById(R.id.refreshButton);
        datePickerButton = findViewById(R.id.datePickerButton);
        beforeAfterToggle = findViewById(R.id.beforeAfterToggle);

//        refreshButton.setOnClickListener(v -> fetchTransactionsWithSourceAndTimeBeforeAfter());

        fetchSources();
//        addOnClickToDatePickerUtil();

        fetchTransactions();
    }

    private void fetchTransactions() {
        //```java
        // Placeholder for fetching data from an API
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

    private void fetchSources() {
        new Thread(() -> {
            List<String> sources = transactionDao.getUniqueSources();
            sources.add("All");

            // Run on UI thread to update UI components
            runOnUiThread(() -> {
                // Ensure you use the custom spinner item layout created earlier
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(HistoryActivity.this, R.layout.spinner_item, sources);
                arrayAdapter.setDropDownViewResource(R.layout.spinner_item); // Use custom layout for dropdown view as well
                sourceSpinner.setAdapter(arrayAdapter);

                sourceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String selectedSource = (String) parent.getItemAtPosition(position);

                        // Start another thread to perform database operations or other long running tasks
                        new Thread(() -> {
                            source = selectedSource;
                            runOnUiThread(() -> fetchTransactionsWithSourceAndTimeBeforeAfter());
                        }).start();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
            });
        }).start();
    }



    public void datePickerUtil(View view) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view2, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            // After date is selected, show time picker
            TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view1, hourOfDay, minute) -> {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);

                // Formatting the calendar time to the TextView
                SimpleDateFormat sdf = new SimpleDateFormat("EEE dd MMM yyyy h:mm a", Locale.getDefault());                Date selectedDateTime = calendar.getTime();
                selectedTime = selectedDateTime.getTime(); // Update selectedTime with the selected date and time
                datePickerButton.setText(sdf.format(selectedDateTime)); // Show formatted date-time string in TextView

                fetchTransactionsWithSourceAndTimeBeforeAfter();
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
            timePickerDialog.show();

        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
        fetchTransactionsWithSourceAndTimeBeforeAfter();
    }

    public void toggleBeforeAfter(View view){
        timeBefore = !timeBefore;
        beforeAfterToggle.setSelected(timeBefore);
        beforeAfterToggle.setText(timeBefore ? "Before" : "After");
        fetchTransactionsWithSourceAndTimeBeforeAfter();
    }

    private void fetchTransactionsWithSourceAndTimeBeforeAfter() {

        System.out.println("Selected time: " + selectedTime + ", selected source: " + source);
        //```java
        // Placeholder for fetching data from an API
        new Thread(() -> {
            // Simulate network delay
            List<Transaction> transactions = new ArrayList<>();

            if (source.equals("All") && selectedTime==0){
                transactions = transactionDao.getAllTransactions();
            }
            else if (source.equals("All") && selectedTime!=0){
                if (timeBefore) {
                    transactions = transactionDao.getTransactionsBefore(selectedTime);
                }
                else {
                    transactions = transactionDao.getTransactionsAfter(selectedTime);
                }
            }
            else if (!source.equals("All") && selectedTime==0){
                transactions = transactionDao.getTransactionsBySource(source);
            }
            else if (!source.equals("All") && selectedTime!=0) {
                if (timeBefore) {
                    transactions = transactionDao.getTransactionsBeforeBySource(selectedTime, source);
                }
                else {
                    transactions = transactionDao.getTransactionsAfterBySource(selectedTime, source);
                }
            }


            List<Transaction> finalTransactions = transactions;
            for (Transaction t: transactions){
                System.out.println(transactions);
            }
            runOnUiThread(() -> adapter.updateTransactions(finalTransactions));
        }).start();
    }








}