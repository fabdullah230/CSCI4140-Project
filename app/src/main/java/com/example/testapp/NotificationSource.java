package com.example.testapp;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "sources")
public class NotificationSource {
    @PrimaryKey
    @NonNull
    public String packageName;
    public String name;
    public String shouldContain;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getShouldContain() {
        return shouldContain;
    }

    public void setShouldContain(String shouldContain) {
        this.shouldContain = shouldContain;
    }

    public NotificationSource(String packageName, String name, String shouldContain) {
        this.packageName = packageName;
        this.name = name;
        this.shouldContain = shouldContain;
    }

    @Override
    public String toString() {
        return "NotificationSource{" +
                "packageName='" + packageName + '\'' +
                ", name='" + name + '\'' +
                ", shouldContain='" + shouldContain + '\'' +
                '}';
    }
}
