package com.example.testapp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    public List<String> sources;
    private TransactionDao transactionDao;
    private AppDatabase database;
    private TransactionAdapter adapter = new TransactionAdapter(new ArrayList<>());
    private Spinner sourceSpinner;
    private Button toggleButton;


    private boolean timeBefore = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_history);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.history), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sourceSpinner = findViewById(R.id.sourceSpinner);
        toggleButton = findViewById(R.id.beforeAfterToggle);

        adapter = new TransactionAdapter(new ArrayList<>());


        RecyclerView transactionsRecyclerView = findViewById(R.id.transactionsRecyclerView1);
        transactionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));


        toggleButton.setOnClickListener(this::toggleTimeBeforeAfter);

        database = AppDatabase.getDatabase(this);
        transactionDao = database.transactionDao();
        new LoadSources().execute();
        new LoadTransactionsAll().execute();
    }

    public void toggleTimeBeforeAfter(View view) {
        timeBefore = !timeBefore;
        toggleButton.setSelected(timeBefore);
        toggleButton.setText(timeBefore ? "Before" : "After");
    }


    private class LoadSources extends AsyncTask<Void, Void, List<String>> {
        @Override
        protected List<String> doInBackground(Void... voids) {
            return transactionDao.getUniqueSources();
        }

        @Override
        protected void onPostExecute(List<String> result) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(HistoryActivity.this, android.R.layout.simple_spinner_dropdown_item, result);
            sourceSpinner.setAdapter(adapter);
            sourceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String selectedSource = (String) parent.getItemAtPosition(position);
                    new LoadTransactionsBySource().execute(selectedSource);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }
    }

    public class LoadTransactionsAll extends AsyncTask<Void, Void, List<Transaction>> {
        @Override
        protected List<Transaction> doInBackground(Void... void1) {
            return transactionDao.getAllTransactions();
        }

        @Override
        protected void onPostExecute(List<Transaction> transactions) {
            super.onPostExecute(transactions);
            for (Transaction t : transactions){
                System.out.println(t);
            }
            if (adapter == null) {
                adapter = new TransactionAdapter(transactions);
                RecyclerView transactionsRecyclerView = findViewById(R.id.transactionsRecyclerView1);
                transactionsRecyclerView.setAdapter(adapter);
            } else {
                runOnUiThread(() -> adapter.updateTransactions(transactions));
//                adapter.updateTransactions(transactions);
//                adapter.notifyDataSetChanged();
            }

        }
    }

    public class LoadTransactionsBySource extends AsyncTask<String, Void, List<Transaction>> {
        @Override
        protected List<Transaction> doInBackground(String... source) {
            return transactionDao.getTransactionsBySource(source[0]);
        }

        @Override
        protected void onPostExecute(List<Transaction> transactions) {
            if (adapter == null) {
                adapter = new TransactionAdapter(transactions);
                RecyclerView transactionsRecyclerView = findViewById(R.id.transactionsRecyclerView1);
                transactionsRecyclerView.setAdapter(adapter);
            } else {
                adapter.updateTransactions(transactions);
            }
        }
    }

    public class LoadTransactionsBefore extends AsyncTask<Long, Void, List<Transaction>> {
        @Override
        protected List<Transaction> doInBackground(Long... beforeTime) {
            return transactionDao.getTransactionsBefore(beforeTime[0]);
        }

        @Override
        protected void onPostExecute(List<Transaction> transactions) {
            if (adapter == null) {
                adapter = new TransactionAdapter(transactions);
                RecyclerView transactionsRecyclerView = findViewById(R.id.transactionsRecyclerView1);
                transactionsRecyclerView.setAdapter(adapter);
            } else {
                adapter.updateTransactions(transactions);
            }
        }
    }

    public class LoadTransactionsAfter extends AsyncTask<Long, Void, List<Transaction>> {
        @Override
        protected List<Transaction> doInBackground(Long... afterTime) {
            return transactionDao.getTransactionsAfter(afterTime[0]);
        }

        @Override
        protected void onPostExecute(List<Transaction> transactions) {
            if (adapter == null) {
                adapter = new TransactionAdapter(transactions);
                RecyclerView transactionsRecyclerView = findViewById(R.id.transactionsRecyclerView1);
                transactionsRecyclerView.setAdapter(adapter);
            } else {
                adapter.updateTransactions(transactions);
            }
        }
    }

    public class Params {
        long timestamp;
        String source;

        public Params(long timestamp, String source) {
            this.timestamp = timestamp;
            this.source = source;
        }
    }

    public class LoadTransactionsAfterBySource extends AsyncTask<Params, Void, List<Transaction>> {
        @Override
        protected List<Transaction> doInBackground(Params... params) {
            if (params == null || params.length == 0) {
                return new ArrayList<>();  // Return empty list if parameters are missing
            }
            Params param = params[0];
            return transactionDao.getTransactionsAfterBySource(param.timestamp, param.source);
        }

        @Override
        protected void onPostExecute(List<Transaction> transactions) {
            if (adapter == null) {
                adapter = new TransactionAdapter(transactions);
                RecyclerView transactionsRecyclerView = findViewById(R.id.transactionsRecyclerView1);
                transactionsRecyclerView.setAdapter(adapter);
            } else {
                adapter.updateTransactions(transactions);
            }
        }
    }





}