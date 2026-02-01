package com.example.functiontesterfpv3;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ViewJournalPage extends AppCompatActivity {
    TextView dateTxt, positiveEventTxt, challengeTxt, moodInfluenceTxt, lessonTxt;
    Button deleteBtn, backBtn;
    dbHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_journal_page);

        dateTxt = findViewById(R.id.dateTxt);
        positiveEventTxt = findViewById(R.id.positiveEventTxt);
        challengeTxt = findViewById(R.id.challengeTxt);
        moodInfluenceTxt = findViewById(R.id.moodInfluenceTxt);
        lessonTxt = findViewById(R.id.lessonTxt);
        deleteBtn = findViewById(R.id.deleteBtn);
        backBtn = findViewById(R.id.backBtn);

        int journalId = getIntent().getIntExtra("journalId", -1);

        dateTxt.setText(getIntent().getStringExtra("date"));
        positiveEventTxt.setText(getIntent().getStringExtra("content1"));
        challengeTxt.setText(getIntent().getStringExtra("content2"));
        moodInfluenceTxt.setText(getIntent().getStringExtra("content3"));
        lessonTxt.setText(getIntent().getStringExtra("content4"));

        long userId = getIntent().getLongExtra("userId", -1);

        db = new dbHelper(this);

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.deleteJournal(journalId);
                Intent i = new Intent(ViewJournalPage.this, JournalListPage.class);
                i.putExtra("userId", userId);
                startActivity(i);
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }
}
