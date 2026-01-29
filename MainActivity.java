package com.taguinod.database;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import com.taguinod.database.R;


import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    EditText inputName;
    SeekBar seekMood;
    TextView moodText;
    Button enter;
    dbhelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inputName = (EditText) findViewById(R.id.ipName);
        seekMood = (SeekBar) findViewById(R.id.choice);
        moodText = findViewById(R.id.moodText);
        enter = findViewById(R.id.enterBtn);
        db = new dbhelper (this);

        seekMood.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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

        enter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = inputName.getText().toString();
                int mood = seekMood.getProgress();

                if (name.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please Enter Name.", Toast.LENGTH_SHORT).show();

                } else {
                    Users user = new Users(name, mood);
                    dbhelper db = new dbhelper(MainActivity.this); // make sure you have this
                    long id = db.insertOrUpdateUser(user);


                    Intent i = new Intent(MainActivity.this, MainWindow.class);

                    i.putExtra("curUserId", id);
                    i.putExtra("mood", mood);
                    startActivity(i);
                }
            }
        });
    }
}
