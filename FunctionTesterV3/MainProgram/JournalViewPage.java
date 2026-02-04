package com.taguinod.unwind;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class JournalViewPage extends AppCompatActivity {
    // Labels for your 5 specific reflection questions
    TextView dateTxt, q1Txt, q2Txt, q3Txt, q4Txt, q5Txt;
    Button deleteBtn, backBtn;
    dbHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_journal_page);

        // 1. Initialize Views
        dateTxt = findViewById(R.id.dateTxt);
        q1Txt = findViewById(R.id.positiveEventTxt); // Why I felt this way
        q2Txt = findViewById(R.id.challengeTxt);     // Moment that affected me
        q3Txt = findViewById(R.id.moodInfluenceTxt); // What I'm avoiding
        q4Txt = findViewById(R.id.lessonTxt);        // What I need
        q5Txt = findViewById(R.id.lessonTxt2);       // Added: What I learned

        deleteBtn = findViewById(R.id.deleteBtn);
        backBtn = findViewById(R.id.backBtn);
        db = new dbHelper(this);

        // 2. Get Data from Intent (sent from JournalAdapter)
        int journalId = getIntent().getIntExtra("journalId", -1);
        dateTxt.setText(getIntent().getStringExtra("date"));
        q1Txt.setText(getIntent().getStringExtra("content1"));
        q2Txt.setText(getIntent().getStringExtra("content2"));
        q3Txt.setText(getIntent().getStringExtra("content3"));

        // 3. Handle the Combined Lesson (Questions 4 & 5)
        String combinedLesson = getIntent().getStringExtra("content4");
        if (combinedLesson != null && combinedLesson.contains(" | Learned: ")) {
            // Split the text back into the two separate questions
            String[] parts = combinedLesson.split(" \\| Learned: ");
            q4Txt.setText(parts[0].replace("Need: ", ""));
            if (q5Txt != null && parts.length > 1) {
                q5Txt.setText(parts[1]);
            }
        } else {
            q4Txt.setText(combinedLesson);
        }

        // 4. Delete Logic
        deleteBtn.setOnClickListener(v -> {
            db.deleteJournal(journalId);
            Toast.makeText(JournalViewPage.this, "Entry Deleted", Toast.LENGTH_SHORT).show();
            // finish() is better than a new Intent here because it returns
            // the user to the existing ListPage which will auto-refresh.
            finish();
        });

        // 5. Back Logic
        backBtn.setOnClickListener(v -> finish());
    }
}
