package com.taguinod.database;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // ✅ Initialize database here

        SqliteOpenHelper dbHelper = new SqliteOpenHelper(this);

        // Insert sample Users
        dbHelper.insertUser("Juan Dela Cruz");
        dbHelper.insertUser("Maria Santos");

// Insert sample Quotes
        dbHelper.insertQuote("Believe in yourself.", "Unknown");
        dbHelper.insertQuote("Hard work beats talent.", "Tim Notke");

// Insert sample Moods
        dbHelper.insertMood("Happy");
        dbHelper.insertMood("Sad");
        dbHelper.insertMood("Motivated");

        SqliteOpenHelper dbHelper1 = new SqliteOpenHelper(this);

        if (dbHelper.isTableEmpty("Users")) {
            dbHelper.insertUser("Juan Dela Cruz");
            dbHelper.insertUser("Maria Santos");
        }

        if (dbHelper.isTableEmpty("Quotes")) {
            dbHelper.insertQuote("Believe in yourself.", "Unknown");
            dbHelper.insertQuote("Hard work beats talent.", "Tim Notke");
        }

        if (dbHelper.isTableEmpty("Moods")) {
            dbHelper.insertMood("Happy");
            dbHelper.insertMood("Sad");
            dbHelper.insertMood("Motivated");
        }



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
