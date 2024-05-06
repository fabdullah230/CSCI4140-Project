package com.example.testapp;

// TransactionAdapter.java

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private List<Transaction> transactions;



    private Context context;
    GlobalState state;

    public TransactionAdapter(List<Transaction> transactions, Context context) {
        this.transactions = transactions;
        this.context = context;
        state = GlobalState.getInstance();
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactions.get(position);
        holder.titleTextView.setText(transaction.getTitle());
        holder.sourceTextView.setText(transaction.getSource());
//        holder.amountTextView.setText(transaction.getAmount());
        holder.amountTextView.setText("HK$ " + transaction.getAmount()); // @hk-company-work: Updated for latest UI

        SimpleDateFormat sdf = new SimpleDateFormat("EEE dd MMM yyyy h:mm a", Locale.getDefault());
        holder.timestampTextView.setText(sdf.format(transaction.getTimestamp()));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (state.getCalibrateMode()) {
                    openNotificationSourceRegistration(transaction);
                }
                else {
                    openTransactionInspection(transaction);
                }
            }
        });
    }

    private void openNotificationSourceRegistration(Transaction transaction) {
        Intent intent = new Intent(context, NotificationSourceRegistrationActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable("transaction", transaction);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    private void openTransactionInspection(Transaction transaction) {
        Intent intent = new Intent(context, TransactionInspectionActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable("transaction", transaction);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public void updateTransactions(List<Transaction> newTransactions) {
        transactions.clear();
        transactions.addAll(newTransactions);
        notifyDataSetChanged();
    }

    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView sourceTextView;
        TextView amountTextView;
        TextView timestampTextView;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            sourceTextView = itemView.findViewById(R.id.sourceTextView);
            amountTextView = itemView.findViewById(R.id.amountTextView);
            timestampTextView = itemView.findViewById(R.id.timestampTextView);
        }
    }
}