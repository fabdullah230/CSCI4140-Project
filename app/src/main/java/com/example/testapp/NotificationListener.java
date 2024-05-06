package com.example.testapp;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NotificationListener extends NotificationListenerService {
    private static final String TAG = "NotificationListener";

    private NotificationSourceDatabase database;
    private NotificationSourcesDao notificationSourcesDao;

    private List<NotificationSource> registeredSources;

    private GlobalState state = GlobalState.getInstance();

    Context context;



    @Override
    public void onCreate(){
        context = getApplicationContext();
        database = NotificationSourceDatabase.getDatabase(context);
        notificationSourcesDao = database.notificationSourcesDao();

        new Thread(() -> {
            registeredSources = notificationSourcesDao.getAllNotificationSources();
        }).start();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);
        // Parse the notification and create a Transaction object
        Transaction transaction = parseNotification(sbn);
        System.out.println(transaction);
        if (transaction != null) {
//            new InsertTransactionTask().execute(transaction);
            Intent intent = new Intent("transaction_received");
            intent.putExtra("transaction", transaction);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }

    private class InsertTransactionTask extends AsyncTask<Transaction, Void, Void> {

        @Override
        protected Void doInBackground(Transaction... transactions) {
            System.out.println("we putting stuff in db");
            System.out.println(transactions[0]);
            AppDatabase.getDatabase(NotificationListener.this).transactionDao().insert(transactions[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Intent intent = new Intent("com.example.REFRESH_TRANSACTIONS");
            sendBroadcast(intent);
        }



    }

    public static double parseFirstNumber(String input) {
        String numberRegex = "[-+]?\\d+(\\.\\d+)?"; // Regular expression for matching numbers (including decimals)

        // Find the first number in the input string
        Pattern pattern = Pattern.compile(numberRegex);
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            String numberString = matcher.group();
            return Double.parseDouble(numberString);
        } else {
            // No number found in the string
            return 0;
        }
    }

    private Transaction parseNotification(StatusBarNotification sbn) {
        // Extract relevant information from the notification
        String title = sbn.getNotification().extras.getString(Notification.EXTRA_TITLE);
        String text = sbn.getNotification().extras.getString(Notification.EXTRA_TEXT);
        String packageName = sbn.getPackageName();

        long timestamp = sbn.getPostTime();

        System.out.println(sbn);

        if(state.getCalibrateMode()){
            return new Transaction(title,  packageName, "0.0", timestamp);

        } else {
            for (NotificationSource n : registeredSources){
                System.out.println("current: " + n.getPackageName() + ", matching: " + packageName);
                if (packageName.equals(n.getPackageName()) && text.contains(n.getShouldContain())){

                    String transactionSource = n.getName();
                    String transactionAmount = String.valueOf(parseFirstNumber(text));
                    return new Transaction(title,  transactionSource, transactionAmount, timestamp);

                }
            }
        }


        return null;
    }
}