package com.calmcampus.threegames;

import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Random;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;


public class ColorGameActivity extends AppCompatActivity {

    LinearLayout menuLayout, scoreLayout;
    ScrollView gameLayout;
    TextView tvColorWord, tvScore, tvTimer, tvRound, tvFinalScore;
    Button btnBlue, btnPurple, btnGreen, btnYellow, btnRed, btnBrown;
    Button btnStart, btnBack, btnBackToMenu;
    ArrayList<ColorItem> colorList = new ArrayList<>();
    ArrayList<Button> buttonList = new ArrayList<>();
    int score = 0;
    int textDuration = 2000;
    Handler handler = new Handler();
    Runnable textRunnable;
    int roundTime = 60000;
    int round = 1;
    int maxRounds = 5;

    // Timer
    CountDownTimer roundTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.colorgame_activity_main);

        // Layouts
        menuLayout = findViewById(R.id.menuLayout);
        gameLayout = findViewById(R.id.gameLayout);
        scoreLayout = findViewById(R.id.scoreLayout);

        // Menu
        btnStart = findViewById(R.id.btnStart);
        btnStart.setOnClickListener(v -> switchToGame());

        // Game
        tvColorWord = findViewById(R.id.tvColorWord);
        tvScore = findViewById(R.id.tvScore);
        tvTimer = findViewById(R.id.tvTimer);
        tvRound = findViewById(R.id.tvRound);

        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> switchToMenu());

        btnBlue = findViewById(R.id.btnBlue);
        btnPurple = findViewById(R.id.btnPurple);
        btnGreen = findViewById(R.id.btnGreen);
        btnYellow = findViewById(R.id.btnYellow);
        btnRed = findViewById(R.id.btnRed);
        btnBrown = findViewById(R.id.btnBrown);

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

        for (Button btn : buttonList) setupButton(btn);

        // Score screen
        tvFinalScore = findViewById(R.id.tvFinalScore);
        btnBackToMenu = findViewById(R.id.btnBackToMenu);
        btnBackToMenu.setOnClickListener(v -> switchToMenu());

        // Back press
        OnBackPressedCallback backCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (gameLayout.getVisibility() == VISIBLE) {
                    switchToMenu();
                } else {
                    finish();
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, backCallback);
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
            } else {
                score--;
            }
            tvScore.setText("Score: " + score);
            button.setEnabled(false);
        });
    }

    void showRandomText() {
        for (Button button : buttonList) button.setEnabled(true);

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
        tvTimer.setText("Time: " + roundTime / 1000 + "s");

        if (roundTimer != null) roundTimer.cancel(); // Cancel previous timer if any

        roundTimer = new CountDownTimer(roundTime, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tvTimer.setText("Time: " + millisUntilFinished / 1000 + "s");
            }

            @Override
            public void onFinish() {
                nextRound();
            }
        };
        roundTimer.start();
    }


    void nextRound() {
        round++;
        if (round > maxRounds) {
            endGame();
            return;
        }
        textDuration -= 300;
        if (textDuration < 500) textDuration = 500;

        handler.removeCallbacks(textRunnable);
        startRound();
    }

    void switchToGame() {
        menuLayout.setVisibility(GONE);
        scoreLayout.setVisibility(GONE);
        gameLayout.setVisibility(VISIBLE);

        // Reset game state
        score = 0;
        round = 1;
        textDuration = 2000;
        tvScore.setText("Score: " + score);

        startRound();
    }

    void switchToMenu() {
        handler.removeCallbacks(textRunnable);
        if (roundTimer != null) roundTimer.cancel();
        gameLayout.setVisibility(GONE);
        scoreLayout.setVisibility(GONE);
        menuLayout.setVisibility(VISIBLE);
    }

    void endGame() {
        handler.removeCallbacks(textRunnable);
        if (roundTimer != null) roundTimer.cancel();
        gameLayout.setVisibility(GONE);
        menuLayout.setVisibility(GONE);
        scoreLayout.setVisibility(VISIBLE);
        tvFinalScore.setText("Final Score: " + score);
    }

}
