package com.example.functiontesterfpv3;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class JournalListPage extends AppCompatActivity {
    RecyclerView recyclerView;
    Button returnBtn;
    FloatingActionButton addBtn;
    dbHelper db;
    long userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.journal_list_page);

        recyclerView = findViewById(R.id.recyclerView);
        addBtn = findViewById(R.id.addBtn);
        returnBtn = findViewById(R.id.returnBtn);
        db = new dbHelper(this);

        Intent i = getIntent();

        if (userId == -1) {
            finish();
            return;
        }

        userId = i.getLongExtra("userId", -1);

        recyclerView.setLayoutManager(
                new LinearLayoutManager(this));
        loadData();

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(JournalListPage.this, AddJournalPage.class);
                i.putExtra("userId", userId);
                startActivity(i);
            }
        });
        returnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i2 = new Intent(JournalListPage.this, HomePage.class);

                i2.putExtra("userId", userId);
                startActivity(i2);
            }
        });

    }
    private void loadData() {
        ArrayList<Journal> list = db.getAllJournals(userId);
        recyclerView.setAdapter(new JournalAdapter(this, list));
    }
    protected void onResume() {
        super.onResume();
        loadData();
    }
}
