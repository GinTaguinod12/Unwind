package com.taguinod.unwind;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;

public class JournalListPage extends AppCompatActivity {
    RecyclerView rvJournalEntries;
    ImageView btnBack;
    FloatingActionButton fabAddEntry;
    dbHelper db;
    long userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Ensure this matches your XML filename (e.g., activity_welcome or journal_list_page)
        setContentView(R.layout.journal_list_page);

        db = new dbHelper(this);
        userId = getIntent().getLongExtra("userId", -1);

        if (userId == -1) {
            finish();
            return;
        }

        // 1. Initialize Views (Matching your activity_welcome.xml IDs)
        rvJournalEntries = findViewById(R.id.rvJournalEntries);
        fabAddEntry = findViewById(R.id.fabAddEntry);
        btnBack = findViewById(R.id.btnBack);

        // 2. Setup RecyclerView
        rvJournalEntries.setLayoutManager(new LinearLayoutManager(this));
        loadData();

        // 3. Button Logic
        fabAddEntry.setOnClickListener(v -> {
            Intent intent = new Intent(JournalListPage.this, AddJournalPage.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });

        btnBack.setOnClickListener(v -> finish());
    }

    private void loadData() {
        ArrayList<Journal> list = db.getAllJournals(userId);
        JournalAdapter adapter = new JournalAdapter(this, list);
        rvJournalEntries.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData(); // Refresh the list when coming back from adding an entry
    }
}
