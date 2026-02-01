package com.example.functiontesterfpv3;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class HomePage extends AppCompatActivity {
    TextView txtName, txtMood, txtQuote, txtAuthor;
    Button homeBtn, journalBtn;
    dbHelper db;
    String name;
    int mood;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.home_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        txtName = findViewById(R.id.txtName);
        txtMood = findViewById(R.id.txtMood);
        txtQuote = findViewById(R.id.txtQuote);
        txtAuthor = findViewById(R.id.txtAuthor);
        homeBtn = findViewById(R.id.homeBtn);
        journalBtn = findViewById(R.id.journalBtn);
        db = new dbHelper(this);
        Intent i = getIntent();
        long userId = i.getLongExtra("userId", -1);

        if (userId == -1) {
            finish();
            return;
        }

        Cursor cursor = db.getUserById(userId);
        if(cursor.moveToFirst()) {
            name = cursor.getString(cursor.getColumnIndexOrThrow("Name"));
            mood = cursor.getInt(cursor.getColumnIndexOrThrow("MoodId"));
        }
        cursor.close();

        Quote quote = db.getRandomQuoteByMood(mood);
        String moodTxt;
        switch (mood) {
            case 1:
                moodTxt = "Uh oh, you're feeling angry today.";
                break;
            case 2:
                moodTxt = "Oh no, whats making you anxious?";
                break;
            case 3:
                moodTxt = "Awh, don't be sad. Cheer up!";
                break;
            case 4:
                moodTxt = "It is okay to be tired, take a break.";
                break;
            case 6:
                moodTxt = "That's amazing! You're feeling happy today.";
                break;
            default:
                moodTxt = "Nice! Keep up that calmness.";
                break;
        }
        txtMood.setText(moodTxt);
        txtName.setText(name);

        if (quote != null) {
            txtQuote.setText("“" + quote.getQuoteText() + "”");
            txtAuthor.setText("- " + quote.getAuthor());
        }

        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i2 = new Intent(HomePage.this, LoginPage.class);
                startActivity(i2);
            }
        });

        journalBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i3 = new Intent(HomePage.this, JournalListPage.class);

                i3.putExtra("userId", userId);
                startActivity(i3);
            }
        });

    }
}
