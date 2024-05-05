package com.example.testapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NotificationSourceAdapter extends RecyclerView.Adapter<NotificationSourceAdapter.ViewHolder> {

    private List<NotificationSource> notificationSources;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    Context context;

    NotificationSourceDatabase notificationSourceDatabase;
    NotificationSourcesDao notificationSourcesDao;

    // data is passed into the constructor
    NotificationSourceAdapter(Context context, List<NotificationSource> data) {
        this.mInflater = LayoutInflater.from(context);
        this.notificationSources = data;
        this.context = context;
        notificationSourceDatabase = NotificationSourceDatabase.getDatabase(context);
        notificationSourcesDao = notificationSourceDatabase.notificationSourcesDao();
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_notification_source, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        NotificationSource source = notificationSources.get(position);
        holder.nameTextView.setText(source.getName());
        holder.packageNameTextView.setText(source.getPackageName());
        holder.shouldContainTextView.setText(source.getShouldContain());
        holder.deleteButton.setOnClickListener(v -> {
            System.out.println("Delete pressed");
            deleteNotificationSource(source);
        });
    }

    private void deleteNotificationSource(NotificationSource source){
        new Thread(() -> {
            notificationSourcesDao.deleteByPackageName(source.getPackageName());
            notificationSources.clear();
            notificationSources.addAll(notificationSourcesDao.getAllNotificationSources());
        }).start();
        notifyDataSetChanged();
    }

    public void updateNotificationSources(List<NotificationSource> notificationSources1){
        notificationSources.clear();
        notificationSources.addAll(notificationSources1);
        notifyDataSetChanged();
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return notificationSources.size();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView packageNameTextView;
        TextView shouldContainTextView;
        ImageButton deleteButton;

        ViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.name);
            packageNameTextView = itemView.findViewById(R.id.packageName);
            shouldContainTextView = itemView.findViewById(R.id.shouldContain);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }

    // convenience method for getting data at click position
    NotificationSource getItem(int id) {
        return notificationSources.get(id);
    }

    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onDeleteClick(int position);
    }
}
