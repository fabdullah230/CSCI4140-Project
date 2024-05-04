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
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private static final String CHANNEL_ID = "sidebar_channel";
    private static final int NOTIFICATION_ID = 1;
    private static TransactionAdapter adapter;
    private static final String NOTIFICATION_LISTENER_PERMISSION = "android.permission.BIND_NOTIFICATION_LISTENER_SERVICE";
    private AppDatabase database;
    private TransactionDao transactionDao;

    private Button button;

    private AppCalibrateDatabase calibrateDatabase;
    private TransactionDao transactionCalibrateDao;
    private BroadcastReceiver transactionReceiver;

    private void requestNotificationListenerPermission() {
        if (!isNotificationListenerEnabled()) {
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
            startActivity(intent);
        }
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
            new LoadTransactionsTask().execute();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        View refresh = findViewById(R.id.refreshButton);
        refreshData(refresh);
        new LoadTransactionsTask().execute();
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

        RecyclerView transactionsRecyclerView = findViewById(R.id.transactionsRecyclerView);
        transactionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        database = AppDatabase.getDatabase(this);
        transactionDao = database.transactionDao();

        calibrateDatabase = AppCalibrateDatabase.getDatabase(this);
        transactionCalibrateDao = calibrateDatabase.transactionDao();

        new LoadTransactionsTask().execute();
    }

    public void refreshData(View view){
        System.out.println("Pressed refresh");
        new LoadTransactionsTask().execute();
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
            }
        }
    }

    public void openSidebar(View view) {
        drawerLayout.openDrawer(findViewById(R.id.navigation_view));
//        sendNotification();
    }

    private static final int PERMISSION_REQUEST_CODE = 123;

    private void sendNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Sidebar Opened")
                .setContentText("You have opened the sidebar." + System.currentTimeMillis())
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, PERMISSION_REQUEST_CODE);
        } else {
            // Permission is already granted, send the notification
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
        startActivity(new Intent(this, TestActivity.class));
    }

    public void openSettings(MenuItem item) {
        startActivity(new Intent(this, CalibrationPageActivity.class));
    }

    public void openAddTransactionDialog(View view) {
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
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    time.setText(sdf.format(calendar.getTime()));  // Show formatted date-time string in TextView

                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
                timePickerDialog.show();

            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });



        EditText source = dialogView.findViewById(R.id.editTextSource);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
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

        // Style the buttons
        Button positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        positiveButton.setTextColor(getResources().getColor(android.R.color.white));
        positiveButton.setBackgroundColor(getResources().getColor(android.R.color.black));
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