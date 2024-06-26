package com.example.testapp;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private static final String CHANNEL_ID = "sidebar_channel";
    private static final int NOTIFICATION_ID = 1;
    private TransactionAdapter adapter;
    private RecyclerView transactionsRecyclerView;
    private static final String NOTIFICATION_LISTENER_PERMISSION = "android.permission.BIND_NOTIFICATION_LISTENER_SERVICE";
    private AppDatabase database;
    private TransactionDao transactionDao;

    private TextView sumAmount;

    private AppCalibrateDatabase calibrateDatabase;
    private TransactionDao transactionCalibrateDao;
    private BroadcastReceiver transactionReceiver;

    private void requestNotificationListenerPermission() {
        if (!isNotificationListenerEnabled()) {
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
            startActivity(intent);
        }
    }

    public void setSumAmount(List<Transaction> transactions){
        double sum = 0;
        for (Transaction t : transactions){
            sum += Double.parseDouble(t.getAmount());
        }
        sumAmount.setText("HK$ " + String.format("%.2f", sum));
    }



    //this is to prevent opening notification page every time
    private boolean isNotificationListenerEnabled() {
        String packageName = getPackageName();
        String className = NotificationListener.class.getName();
        String flat = Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");
        if (flat != null) {
            String[] names = flat.split(":");
            for (String name : names) {
                ComponentName cn = ComponentName.unflattenFromString(name);
                if (cn != null && packageName.equals(cn.getPackageName()) && className.equals(cn.getClassName())) {
                    return true;
                }
            }
        }
        return false;
    }

    private void createNotificationChannel() {
        CharSequence name = "Sidebar Channel";
        String description = "Channel for sidebar notifications";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        channel.setDescription(description);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    public void addTransaction(Transaction transaction) {
        System.out.println("adding transaction: " + transaction);
        new InsertTransactionTask().execute(transaction);
    }


    @Override
    protected void onStart() {
        transactionReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("transaction_received")) {
                    Transaction transaction = intent.getParcelableExtra("transaction");
                    if (transaction != null) {
                        System.out.println("Broadcast received: " + transaction);
                        addTransaction(transaction);
                    }
                }
            }
        };
        super.onStart();
        IntentFilter intentFilter = new IntentFilter("transaction_received");
        LocalBroadcastManager.getInstance(this).registerReceiver(transactionReceiver, intentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(transactionReceiver);
    }

    private class InsertTransactionTask extends AsyncTask<Transaction, Void, Void> {
        @Override
        protected Void doInBackground(Transaction... transactions) {
            GlobalState state = GlobalState.getInstance();
            System.out.println(transactions[0]);
            if (state.getCalibrateMode()){
                transactionCalibrateDao.insert(transactions[0]);
            }
            else {
                transactionDao.insert(transactions[0]);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            // Update the transaction list
            fetchTransactions();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
//        View refresh = findViewById(R.id.refreshButton);
        View refresh = null; // @hk-company-work: R.id.refreshButton is removed from the UI...
        refreshData(refresh);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        createNotificationChannel();
        requestNotificationListenerPermission();
        drawerLayout = findViewById(R.id.drawer_layout);
        sumAmount = findViewById(R.id.sumAmount);


        transactionsRecyclerView = findViewById(R.id.transactionsRecyclerView);
        transactionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TransactionAdapter(new ArrayList<>(), this);
        transactionsRecyclerView.setAdapter(adapter);


        database = AppDatabase.getDatabase(this);
        transactionDao = database.transactionDao();

        calibrateDatabase = AppCalibrateDatabase.getDatabase(this);
        transactionCalibrateDao = calibrateDatabase.transactionDao();

        fetchTransactions();
    }

    public void refreshData(View view){
        System.out.println("Pressed refresh");
        fetchTransactions();
    }

    public class LoadTransactionsTask extends AsyncTask<Void, Void, List<Transaction>> {
        @Override
        protected List<Transaction> doInBackground(Void... voids) {
            return transactionDao.getAllTransactions();
        }

        @Override
        protected void onPostExecute(List<Transaction> transactions) {
            super.onPostExecute(transactions);
            if (adapter == null) {
                adapter = new TransactionAdapter(transactions, MainActivity.this);
                RecyclerView transactionsRecyclerView = findViewById(R.id.transactionsRecyclerView);
                transactionsRecyclerView.setAdapter(adapter);
            } else {
                adapter.updateTransactions(transactions);
                setSumAmount(transactions);
            }
        }
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
            runOnUiThread(() -> setSumAmount(finalTransactions));
        }).start();
    }








    public void openSidebar(View view) {
//        drawerLayout.openDrawer(findViewById(R.id.navigation_view));
        // @hk-company-work: Changed to Pop-up menu for latest UI
//        sendNotification();

        PopupMenu menu = new PopupMenu(this, view);
        menu.getMenuInflater().inflate(R.menu.nav_menu, menu.getMenu());
        menu.show();
    }



    private static final int PERMISSION_REQUEST_CODE = 123;

    private void sendNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Sidebar Opened")
                .setContentText("You have opened the sidebar. Random amount: " + ((int) (Math.random() * (1000 - 50 + 1)) + 50))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, PERMISSION_REQUEST_CODE);
        } else {
            // Permission is already granted, send the notification
            System.out.println("sending notification");
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        }
    }

    public void sendNotification(View view){
        sendNotification();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted, send the notification
                sendNotification();
            } else {
                // Permission is denied, show a message or take appropriate action
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void openHistory(MenuItem item) {
        startActivity(new Intent(this, HistoryActivity.class));
    }

    public void openSettings(MenuItem item) {
        startActivity(new Intent(this, CalibrationPageActivity.class));
    }

    public void openAddTransactionDialog(View view) {
        sendNotification();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_transaction, null);
        builder.setView(dialogView);
        builder.setTitle("Add New Transaction");

        EditText title = dialogView.findViewById(R.id.editTextTitle);
        EditText amount = dialogView.findViewById(R.id.editTextAmount);
        TextView time = dialogView.findViewById(R.id.editTextTime);


        Calendar calendar = Calendar.getInstance();
        time.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(this,  (view2, year, month, dayOfMonth) -> {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                // After date is selected, show time picker
                TimePickerDialog timePickerDialog = new TimePickerDialog(this,  (view1, hourOfDay, minute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);
                    calendar.set(Calendar.SECOND, 0);
                    calendar.set(Calendar.MILLISECOND, 0);

                    // Formatting the calendar time to the TextView
                    SimpleDateFormat sdf = new SimpleDateFormat("EEE dd MMM yyyy h:mm a", Locale.getDefault());
                    time.setText(sdf.format(calendar.getTime()));  // Show formatted date-time string in TextView

                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
                timePickerDialog.show();

            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });



        EditText source = dialogView.findViewById(R.id.editTextSource);

        SimpleDateFormat sdf = new SimpleDateFormat("EEE dd MMM yyyy h:mm a", Locale.getDefault());
        String currentTime = sdf.format(new Date());
        time.setText(currentTime);

        builder.setPositiveButton("Add", (dialog, which) -> {
            if (title.getText().toString().isEmpty() || source.getText().toString().isEmpty() || amount.getText().toString().isEmpty()){

            }
            else{
                String transactionTitle = title.getText().toString() ;
                String transactionSource = source.getText().toString();
                String transactionAmount = amount.getText().toString();
                long transactionTimestamp = calendar.getTimeInMillis(); // Unix timestamp in milliseconds

                Transaction transaction = new Transaction(transactionTitle, transactionSource, transactionAmount, transactionTimestamp);
                addTransaction(transaction);
            }

        });

        builder.setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();


        // @hk-company-work: Change Yes/Cancel color for latest UI
        TypedValue typedValue = new TypedValue();
        this.getTheme().resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true);
        int backgroundColor = typedValue.data;
        this.getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnPrimary, typedValue, true);
        int foregroundColor = typedValue.data;


        // Style the buttons
        Button positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
//        positiveButton.setTextColor(getResources().getColor(android.R.color.white));
//        positiveButton.setBackgroundColor(getResources().getColor(android.R.color.black));
        positiveButton.setTextColor(foregroundColor);
        positiveButton.setBackgroundColor(backgroundColor);
        positiveButton.setTextSize(16);
        positiveButton.setAllCaps(false);
        positiveButton.setTypeface(null, Typeface.BOLD);

        Button negativeButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        negativeButton.setTextColor(getResources().getColor(android.R.color.black));
        negativeButton.setBackgroundColor(getResources().getColor(android.R.color.white));
        negativeButton.setTextSize(16);
        negativeButton.setAllCaps(false);
    }





}