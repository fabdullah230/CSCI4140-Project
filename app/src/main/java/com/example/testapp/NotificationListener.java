package com.example.testapp;

import android.app.Notification;
import android.content.Intent;
import android.os.AsyncTask;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class NotificationListener extends NotificationListenerService {
    private static final String TAG = "NotificationListener";

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);
        // Parse the notification and create a Transaction object
        Transaction transaction = parseNotification(sbn);
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

    private Transaction parseNotification(StatusBarNotification sbn) {
        // Extract relevant information from the notification
        String title = sbn.getNotification().extras.getString(Notification.EXTRA_TITLE);
        String text = sbn.getNotification().extras.getString(Notification.EXTRA_TEXT);
        String packageName = sbn.getPackageName();
        long timestamp = sbn.getPostTime();

        // Parse the notification data and create a Transaction object
        // You can customize this based on the structure of the notifications you want to capture
        String transactionTitle = title;
        String transactionSource = packageName;
        String transactionAmount = "0"; // Default amount is zero


        // Extract amount from the notification text if available
        if (text != null) {
            // Use regular expressions or string manipulation to extract the amount
            // For example, if the amount is prefixed with "Amount: "
            if (text.contains("Amount: ")) {
                transactionAmount = text.split("Amount: ")[1];
            }
        }

        return new Transaction(transactionTitle, transactionSource, transactionAmount, timestamp);
    }
}