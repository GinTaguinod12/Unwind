package com.example.colorgame;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Random;

public class ColorGame extends AppCompatActivity {

    TextView tvColorWord, tvScore, tvTimer, tvRound;
    Button btnBlue, btnPurple, btnGreen, btnYellow, btnRed, btnBrown;
    Button btnBack;
    ArrayList<ColorItem> colorList = new ArrayList<>();
    ArrayList<Button> buttonList = new ArrayList<>();
    int score = 0;
    int textDuration = 2000;
    Handler handler = new Handler();
    Runnable textRunnable;
    int roundTime = 60000;
    int round = 1;
    int maxRounds = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.colorgame);

        tvColorWord = findViewById(R.id.tvColorWord);
        tvScore = findViewById(R.id.tvScore);
        tvTimer = findViewById(R.id.tvTimer);
        tvRound = findViewById(R.id.tvRound);

        btnBlue = findViewById(R.id.btnBlue);
        btnPurple = findViewById(R.id.btnPurple);
        btnGreen = findViewById(R.id.btnGreen);
        btnYellow = findViewById(R.id.btnYellow);
        btnRed = findViewById(R.id.btnRed);
        btnBrown = findViewById(R.id.btnBrown);

        btnBack = findViewById(R.id.btnBack);

        colorList.add(new ColorItem("BLUE", Color.BLUE));
        colorList.add(new ColorItem("PURPLE", Color.parseColor("#9C27B0")));
        colorList.add(new ColorItem("GREEN", Color.GREEN));
        colorList.add(new ColorItem("YELLOW", Color.YELLOW));
        colorList.add(new ColorItem("RED", Color.RED));
        colorList.add(new ColorItem("BROWN", Color.parseColor("#795548")));

        buttonList.add(btnBlue);
        buttonList.add(btnPurple);
        buttonList.add(btnGreen);
        buttonList.add(btnYellow);
        buttonList.add(btnRed);
        buttonList.add(btnBrown);

        setupButton(btnBlue);
        setupButton(btnPurple);
        setupButton(btnGreen);
        setupButton(btnYellow);
        setupButton(btnRed);
        setupButton(btnBrown);

        OnBackPressedCallback backCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, backCallback);

        btnBack.setOnClickListener(v -> {
            backCallback.handleOnBackPressed();
        });

        startRound();
    }

    private class ColorItem {
        String name;
        int colorValue;

        ColorItem(String name, int colorValue) {
            this.name = name;
            this.colorValue = colorValue;
        }
    }

    void setupButton(Button button) {
        button.setOnClickListener(v -> {
            if (!button.isEnabled()) return; 

            String selected = button.getText().toString();
            String correct = tvColorWord.getText().toString();

            if (selected.equals(correct)) {
                score++;
                tvScore.setText("Score: " + score);
                button.setEnabled(false);
            }
        });
    }

    void showRandomText() {
        for (Button button : buttonList) {
            button.setEnabled(true);
        }

        Random random = new Random();
        ColorItem wordItem = colorList.get(random.nextInt(colorList.size()));
        ColorItem colorItem = colorList.get(random.nextInt(colorList.size()));

        tvColorWord.setText(wordItem.name);
        tvColorWord.setTextColor(colorItem.colorValue);
    }

    void startTextLoop() {
        textRunnable = new Runnable() {
            @Override
            public void run() {
                showRandomText();
                handler.postDelayed(this, textDuration);
            }
        };
        handler.post(textRunnable);
    }

    void startRound() {
        tvRound.setText("Round: " + round);
        startTextLoop();
        startRoundTimer();
    }

    void startRoundTimer() {
        new CountDownTimer(roundTime, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                tvTimer.setText("Time: " + millisUntilFinished / 1000 + "s");
            }

            @Override
            public void onFinish() {
                nextRound();
            }
        }.start();
    }

    void nextRound() {
        round++;
        if (round > maxRounds) {
            Intent intent = new Intent(this, ScoreScreen.class);
            intent.putExtra("score", score);
            startActivity(intent);
            finish();
            return;
        }
        textDuration -= 300;
        if (textDuration < 500) textDuration = 500;

        handler.removeCallbacks(textRunnable);
        startRound();
    }
}
