package com.taguinod.unwind;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddJournalPage extends AppCompatActivity {

    // Using your specific IDs from your XML
    private TextInputEditText etDate, etQuestion1, etQuestion2, etQuestion3, etQuestion4, etQuestion5;
    private MaterialButton btnSave;
    private ImageView btnBack;
    private dbHelper db;
    private final Calendar calendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Ensure this matches your XML filename (activity_main or add_journal_page)
        setContentView(R.layout.add_journal_page);

        // 1. Initialize Database and Views
        db = new dbHelper(this);
        long userId = getIntent().getLongExtra("userId", -1);

        etDate = findViewById(R.id.etDate);
        etQuestion1 = findViewById(R.id.etQuestion1);
        etQuestion2 = findViewById(R.id.etQuestion2);
        etQuestion3 = findViewById(R.id.etQuestion3);
        etQuestion4 = findViewById(R.id.etQuestion4);
        etQuestion5 = findViewById(R.id.etQuestion5);
        btnSave = findViewById(R.id.btnSave);
        btnBack = findViewById(R.id.btnBack);

        // 2. Setup Date Picker (as per your original functionality)
        updateLabel();
        etDate.setOnClickListener(v -> showDatePicker());

        // 3. Save Logic (Mapping your 5 questions into their 4 database slots)
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String date = etDate.getText().toString();
                String q1 = etQuestion1.getText().toString();
                String q2 = etQuestion2.getText().toString();
                String q3 = etQuestion3.getText().toString();
                String q4 = etQuestion4.getText().toString();
                String q5 = etQuestion5.getText().toString();

                // Merge Q4 and Q5 into the 'Lesson' column so no data is lost
                String combinedLesson = "Need: " + q4 + " | Learned: " + q5;

                // Call the groupmate's database method
                db.addJournal(new Journal(
                        date,
                        q1,             // Saved as PositiveEvent
                        q2,             // Saved as Challenge
                        q3,             // Saved as MoodInfluence
                        combinedLesson, // Saved as Lesson
                        userId
                ));

                Toast.makeText(AddJournalPage.this, "Reflection Saved!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        // 4. Back/Cancel Logic
        btnBack.setOnClickListener(v -> finish());
    }

    private void showDatePicker() {
        new DatePickerDialog(this, (view, year, month, day) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, day);
            updateLabel();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateLabel() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy", Locale.US);
        etDate.setText(sdf.format(calendar.getTime()));
    }
}
