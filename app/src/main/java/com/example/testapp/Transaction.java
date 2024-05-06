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

    private String source;
    private String amount;
    private long timestamp;

    private boolean isShared;

    private String sharedAmount;
    private String personalAmount;
    private String comments;



    public String getPersonalAmount() {
        return personalAmount;
    }

    public void setPersonalAmount(String personalAmount) {
        this.personalAmount = personalAmount;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public boolean isShared() {
        return isShared;
    }

    public void setShared(boolean shared) {
        isShared = shared;
    }

    public String getSharedAmount() {
        return sharedAmount;
    }

    public void setSharedAmount(String sharedAmount) {
        this.sharedAmount = sharedAmount;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
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
                ", timestamp=" + timestamp +
                ", isShared=" + isShared +
                ", sharedAmount='" + sharedAmount + '\'' +
                ", personalAmount='" + personalAmount + '\'' +
                ", comments='" + comments + '\'' +
                '}';
    }

    public Transaction(String title, String source, String amount, long timestamp) {
        this.title = title;

        this.source = source;
        this.amount = amount;
        this.timestamp = timestamp;
        this.id = (int) (Math.random() * (10000000 + 1));
        this.isShared = false;
        this.comments = "No Comments";
        this.sharedAmount = "0.0";
        this.personalAmount = amount;
        System.out.println(this);
    }

    protected Transaction(Parcel in) {
        id = in.readInt();
        title = in.readString();

        source = in.readString();
        amount = in.readString();
        timestamp = in.readLong();
        isShared = in.readBoolean();
        comments = in.readString();
        sharedAmount = in.readString();
        personalAmount = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeString(source);
        dest.writeString(amount);
        dest.writeLong(timestamp);
        dest.writeBoolean(isShared);
        dest.writeString(comments);
        dest.writeString(sharedAmount);
        dest.writeString(personalAmount);
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