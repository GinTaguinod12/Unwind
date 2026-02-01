package com.example.functiontesterfpv3;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AddJournalPage extends AppCompatActivity {
    EditText edtDate, edtPositiveEvent, edtChallenge, edtMoodInfluence, edtLesson;
    Button saveBtn, cancelBtn;
    dbHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_journal_page);

        edtDate = findViewById(R.id.edtDate);
        edtPositiveEvent = findViewById(R.id.edtPositiveEvent);
        edtChallenge = findViewById(R.id.edtChallenge);
        edtMoodInfluence = findViewById(R.id.edtMoodInfluence);
        edtLesson = findViewById(R.id.edtLesson);
        saveBtn = findViewById(R.id.saveBtn);
        cancelBtn = findViewById(R.id.cancelBtn);

        db = new dbHelper(this);

        long userId = getIntent().getLongExtra("userId", -1);

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.addJournal(new Journal(
                        edtDate.getText().toString(),
                        edtPositiveEvent.getText().toString(),
                        edtChallenge.getText().toString(),
                        edtMoodInfluence.getText().toString(),
                        edtLesson.getText().toString(),
                        userId
                ));
                finish();
            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
