package com.example.functiontesterfpv3;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MoodSelection extends AppCompatActivity {
    TextView questionText, moodText;
    SeekBar moodChoice;
    Button enterBtn;
    dbHelper db;
    String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.mood_selection);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        questionText = findViewById(R.id.questionText);
        moodText = findViewById(R.id.moodText);
        moodChoice = findViewById(R.id.moodChoice);
        enterBtn = findViewById(R.id.enterBtn);
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
        }
        cursor.close();

        moodChoice.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            String moodValue = "";
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                switch (progress) {
                    case 0:
                        moodValue = "Angry";
                        break;
                    case 1:
                        moodValue = "Anxious";
                        break;
                    case 2:
                        moodValue = "Sad";
                        break;
                    case 3:
                        moodValue = "Tired";
                        break;
                    case 4:
                        moodValue = "Calm";
                        break;
                    case 5:
                        moodValue = "Happy";
                        break;
                    default:
                        moodValue = "Calm";
                        break;
                }
                moodText.setText(moodValue);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        questionText.setText("How are you today, " + name + "?");

        enterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.updateUserMood(userId, moodChoice.getProgress() + 1);
                Intent i2 = new Intent(MoodSelection.this, HomePage.class);
                i2.putExtra("userId", userId);
                startActivity(i2);
            }
        });
    }
}
