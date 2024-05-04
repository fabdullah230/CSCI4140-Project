package com.example.testapp;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface TransactionDao {
    @Insert
    void insert(Transaction transaction);

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    List<Transaction> getAllTransactions();

    @Query("SELECT DISTINCT source FROM transactions")
    List<String> getUniqueSources();

    @Query("SELECT * FROM transactions WHERE timestamp < :timestamp ORDER BY timestamp DESC")
    List<Transaction> getTransactionsBefore(long timestamp);

    @Query("SELECT * FROM transactions WHERE timestamp > :timestamp ORDER BY timestamp DESC")
    List<Transaction> getTransactionsAfter(long timestamp);

    @Query("SELECT * FROM transactions WHERE timestamp >= :startOfMonth ORDER BY timestamp DESC")
    List<Transaction> getThisMonthsTransactions(long startOfMonth);

    @Query("SELECT * FROM transactions WHERE source = :source ORDER BY timestamp DESC")
    List<Transaction> getTransactionsBySource(String source);

    @Query("SELECT * FROM transactions WHERE title = :title ORDER BY timestamp DESC")
    List<Transaction> getTransactionByTitle(String title);

    @Query("DELETE FROM transactions WHERE id = :transactionId")
    void deleteById(int transactionId);

    @Query("SELECT * FROM transactions WHERE timestamp < :timestamp AND source = :source ORDER BY timestamp DESC")
    List<Transaction> getTransactionsBeforeBySource(long timestamp, String source);

    @Query("SELECT * FROM transactions WHERE timestamp > :timestamp AND source = :source ORDER BY timestamp DESC")
    List<Transaction> getTransactionsAfterBySource(long timestamp, String source);


}