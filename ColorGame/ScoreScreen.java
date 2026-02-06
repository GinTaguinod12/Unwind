package com.example.colorgame;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ScoreScreen extends AppCompatActivity {

    TextView tvFinalScore;
    Button btnBackToMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scorescreen);

        tvFinalScore = findViewById(R.id.tvFinalScore);
        btnBackToMenu = findViewById(R.id.btnBackToMenu);

        int score = getIntent().getIntExtra("score", 0);
        tvFinalScore.setText("Final Score: " + score);

        btnBackToMenu.setOnClickListener(v -> {
            finish();
        });
    }
}
