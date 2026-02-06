package com.example.meditate_maintab;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MaterialCardView cardCalm  = findViewById(R.id.cardCalm);
        MaterialCardView cardBreak = findViewById(R.id.cardBreak);
        MaterialCardView cardLove  = findViewById(R.id.cardLove);
        MaterialCardView cardFocus = findViewById(R.id.cardFocus);

        cardCalm.setOnClickListener(v ->
                openMeditation("Guided Calm", 5, R.raw.calm)
        );

        cardBreak.setOnClickListener(v ->
                openMeditation("Mind Break", 5, R.raw.mind)
        );

        cardLove.setOnClickListener(v ->
                openMeditation("Self Love", 5, R.raw.self)
        );

        cardFocus.setOnClickListener(v ->
                openMeditation("Focus & Peace", 5, R.raw.focus)
        );
    }

    private void openMeditation(String title, int minutes, int audioRes) {
        Intent i = new Intent(this, MeditationPlayerActivity.class);
        i.putExtra("title", title);
        i.putExtra("time", minutes * 60 * 1000L);
        i.putExtra("audio", audioRes);
        startActivity(i);
    }
}
