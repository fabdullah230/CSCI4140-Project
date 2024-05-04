package com.example.testapp;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Random;

@Entity(tableName = "transactions")
public class Transaction implements Parcelable{

    @PrimaryKey(autoGenerate = true)
    @NonNull
    private int id;
    private String title;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    private String source;
    private String amount;
    private long timestamp;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", source='" + source + '\'' +
                ", amount='" + amount + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }


    public Transaction(String title, String source, String amount, long timestamp) {
        this.title = title;
        this.source = source;
        this.amount = amount;
        this.timestamp = timestamp;
        this.id = (int) (Math.random() * (10000000 + 1));
    }

    protected Transaction(Parcel in) {
        id = in.readInt();
        title = in.readString();
        source = in.readString();
        amount = in.readString();
        timestamp = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeString(source);
        dest.writeString(amount);
        dest.writeLong(timestamp);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<Transaction> CREATOR = new Parcelable.Creator<Transaction>() {
        @Override
        public Transaction createFromParcel(Parcel in) {
            return new Transaction(in);
        }

        @Override
        public Transaction[] newArray(int size) {
            return new Transaction[size];
        }
    };

}