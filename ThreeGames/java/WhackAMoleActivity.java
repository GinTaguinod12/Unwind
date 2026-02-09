package com.calmcampus.threegames;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.media.SoundPool;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class WhackAMoleActivity extends AppCompatActivity {

    private SoundPool soundPool;

    // UI Elements
    private TextView tvTimer, tvScore, tvHighScore;
    private Button btnStart, btnExit;
    private ImageView[] holes;
    private ImageView[][] explosions; // [hole][overlayIndex]
    private static final int EXPLOSION_OVERLAYS = 3; // 3 per hole

    private TextView[] scorePopups;

    // Game Variables
    private int score = 0;
    private int highScore = 0;
    private int timeLeft = 30;
    private boolean isGameRunning = false;
    private CountDownTimer gameTimer;
    private Handler moleHandler;
    private Random random;
    private int currentMoleIndex = -1;

    // SharedPreferences for high score
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "WhackAMolePrefs";
    private static final String HIGH_SCORE_KEY = "HighScore";

    // Game Settings
    private static final int GAME_DURATION = 30000; // 30 seconds
    private int MOLE_SHOW_TIME = 1000; // 1 second
    private int MOLE_SPAWN_DELAY = 800; // 0.8 seconds between spawns

    // HP System (using quarters for fractional damage)
    private int maxHp = 3;
    private int currentHp = 3;
    private int hpQuarters = 12; // 3 hearts * 4 quarters each
    private ImageView[] hearts;

    private long timeLeftMillis = GAME_DURATION;

    // Checker if the mole was hit
    private boolean moleWasHit = false;

    // For more moles when it progress
    private boolean[] activeMoles = new boolean[9];
    private int maxSimultaneousMoles = 1;

    private int bonkSound;

    // total real playtime in milliseconds
    private long playTimeMillis = 0;

    // Hide runnables for each mole to cancel delayed hides
    private Runnable[] hideRunnables = new Runnable[9];




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.whackamole_activity_main);

        // Initialize UI components
        initializeViews();

        // Initialize game components
        random = new Random();
        moleHandler = new Handler();
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Load high score
        loadHighScore();

        // Set up click listeners
        setupClickListeners();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            soundPool = new SoundPool.Builder()
                    .setMaxStreams(5)
                    .build();
        } else {
            soundPool = new SoundPool(5, android.media.AudioManager.STREAM_MUSIC, 0);
        }
        bonkSound = soundPool.load(this, R.raw.bonk, 1);

    }

    private void playBonkSound() {
        soundPool.play(bonkSound, 0.8f, 0.8f, 1, 0, 1f);
    }

    private void initializeViews() {
        tvTimer = findViewById(R.id.tvTimer);
        tvScore = findViewById(R.id.tvScore);
        tvHighScore = findViewById(R.id.tvHighScore);
        btnStart = findViewById(R.id.btnStart);
        btnExit = findViewById(R.id.btnExit);

        // Initialize holes array
        holes = new ImageView[9];
        holes[0] = findViewById(R.id.hole1);
        holes[1] = findViewById(R.id.hole2);
        holes[2] = findViewById(R.id.hole3);
        holes[3] = findViewById(R.id.hole4);
        holes[4] = findViewById(R.id.hole5);
        holes[5] = findViewById(R.id.hole6);
        holes[6] = findViewById(R.id.hole7);
        holes[7] = findViewById(R.id.hole8);
        holes[8] = findViewById(R.id.hole9);

        // Initialize explosions array
        explosions = new ImageView[9][EXPLOSION_OVERLAYS];
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < EXPLOSION_OVERLAYS; j++) {
                int resID = getResources().getIdentifier(
                        "explosion" + (i + 1) + "_" + (j + 1),
                        "id", getPackageName()
                );
                explosions[i][j] = findViewById(resID);
                explosions[i][j].setVisibility(View.GONE);
            }
        }


        // Initialize score popups array
        scorePopups = new TextView[9];
        scorePopups[0] = findViewById(R.id.scorePopup1);
        scorePopups[1] = findViewById(R.id.scorePopup2);
        scorePopups[2] = findViewById(R.id.scorePopup3);
        scorePopups[3] = findViewById(R.id.scorePopup4);
        scorePopups[4] = findViewById(R.id.scorePopup5);
        scorePopups[5] = findViewById(R.id.scorePopup6);
        scorePopups[6] = findViewById(R.id.scorePopup7);
        scorePopups[7] = findViewById(R.id.scorePopup8);
        scorePopups[8] = findViewById(R.id.scorePopup9);

        // Hearts
        hearts = new ImageView[3];
        hearts[0] = findViewById(R.id.heart1);
        hearts[1] = findViewById(R.id.heart2);
        hearts[2] = findViewById(R.id.heart3);



    }

    private void setupClickListeners() {
        // Start button
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startGame();
            }
        });

        // Exit button
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showExitDialog();
            }
        });

        // Set click listeners for all holes
        for (int i = 0; i < holes.length; i++) {
            final int index = i;
            holes[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onHoleClicked(index);
                }
            });
        }
    }

    private void startGame() {
        // Disable start button
        btnStart.setEnabled(false);
        btnStart.setAlpha(0.5f);

        // Reset game variables
        score = 0;
        timeLeft = 30;
        isGameRunning = true;
        currentMoleIndex = -1;

        // Reset active moles
        for (int i = 0; i < activeMoles.length; i++) {
            activeMoles[i] = false;
        }

        // Reset difficulty settings
        MOLE_SHOW_TIME = 1000;
        MOLE_SPAWN_DELAY = 800;
        maxSimultaneousMoles = 1;
        timeLeftMillis = GAME_DURATION;

        // Update UI
        updateScore();
        updateTimer();

        // Hide all moles
        hideAllMoles();

        // Start timer
        startTimer();

        // Start spawning moles
        spawnMole();

        // Reset HP on game start
        maxHp = 3;
        currentHp = maxHp;
        hpQuarters = 12; // 3 hearts * 4 quarters
        updateHearts();

        // Reset REAL absolute playtime counter
        playTimeMillis = 0;

    }

    private void startTimer() {
        gameTimer = new CountDownTimer(timeLeftMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftMillis = millisUntilFinished;
                updateTimer();

                playTimeMillis += 1000; // increment by 1 second each tick
                int elapsedSeconds = (int) (playTimeMillis / 1000);

                // Increase difficulty every 5 seconds
                if (elapsedSeconds % 5 == 0) {
                    MOLE_SHOW_TIME = Math.max(400, 1000 - elapsedSeconds * 20);
                    MOLE_SPAWN_DELAY = Math.max(120, 800 - elapsedSeconds * 35);

                    if (elapsedSeconds > 10) maxSimultaneousMoles = 2;
                    if (elapsedSeconds > 20) maxSimultaneousMoles = 3;
                }
            }

            @Override
            public void onFinish() {
                timeLeftMillis = 0;
                endGame();
            }
        }.start();
    }


    private void spawnMole() {
        if (!isGameRunning) return;

        int activeCount = 0;
        for (boolean active : activeMoles) {
            if (active) activeCount++;
        }

        if (activeCount < maxSimultaneousMoles) {

            int tempIndex;
            do {
                tempIndex = random.nextInt(9);
            } while (activeMoles[tempIndex]);

            final int moleIndex = tempIndex;

            activeMoles[moleIndex] = true;
            showMoleWithAnimation(moleIndex);

            hideRunnables[moleIndex] = () -> {
                if (isGameRunning && activeMoles[moleIndex]) {
                    loseHpQuarter();
                    hideMoleWithAnimation(moleIndex);
                    activeMoles[moleIndex] = false;
                }
            };
            moleHandler.postDelayed(hideRunnables[moleIndex], MOLE_SHOW_TIME);
        }
        moleHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                spawnMole();
            }
        }, MOLE_SPAWN_DELAY);
    }


    //change mole here
    private void showMoleWithAnimation(int index) {
        moleWasHit = false;
        final ImageView moleView = holes[index];
        moleView.setImageResource(R.drawable.molevector);

        // Pop-up animation: translate from below (hidden) to visible position
        moleView.setTranslationY(100f); // start below
        moleView.animate()
                .translationY(0f)   // move to original position
                .setDuration(300)
                .start();

    }

    private void hideMoleWithAnimation(int index) {
        final ImageView moleView = holes[index];

        // Animate mole moving down
        moleView.animate()
                .translationY(100f) // move down
                .setDuration(300)
                .withEndAction(() -> {
                    moleView.setImageResource(R.drawable.hole); // reset to hole
                    moleView.setTranslationY(0f); // reset position
                })
                .start();
    }


    private void updateHearts() {
        for (int i = 0; i < hearts.length; i++) {
            if (i < currentHp) {
                hearts[i].setImageResource(R.drawable.heart_full);
            } else {
                hearts[i].setImageResource(R.drawable.heart_empty);
            }
        }
    }



    private void loseHp() {
        currentHp--;
        updateHearts();

        if (currentHp <= 0) {
            endGame();
        }
    }

    // New method: Lose 1/4 of a heart
    private void loseHpQuarter() {
        hpQuarters--;

        // Update currentHp based on quarters (round up)
        currentHp = (int) Math.ceil(hpQuarters / 4.0);
        updateHearts();

        if (hpQuarters <= 0) {
            endGame();
        }
    }

    private void gainHeartContainer() {
        if (maxHp < 5) { // cap at 5 hearts
            maxHp++;
            currentHp = maxHp;
            hpQuarters = maxHp * 4;
            updateHearts();
        }
    }

    private void tryRestoreHp() {
        if (currentHp < maxHp) {
            int chance = random.nextInt(100); // 0–99
            if (chance < 20) { // 20% chance rng
                currentHp++;
                hpQuarters = currentHp * 4; // Restore full quarters for that heart
                updateHearts();
            }
        }
    }
    private void tryAddTime(int seconds, int percentChance) {
        int roll = random.nextInt(100); // 0–99
        if (roll < percentChance) {
            addTime(seconds);
        }
    }


    private void onHoleClicked(int index) {
        if (!isGameRunning) return;

        // Cancel scheduled hide
        if (hideRunnables[index] != null) {
            moleHandler.removeCallbacks(hideRunnables[index]);
        }

        // Only proceed if mole is currently active
        if (activeMoles[index]) {
            activeMoles[index] = false;  // mark mole as gone
            score += 10;
            updateScore();
            showHitAnimation(index);
            showScorePopup(index);

            // Small chance for bonus effects
            if (random.nextInt(100) < 10) { // 10% chance
                addTime(5);
            }
            tryRestoreHp();
        } else {
            // Clicking empty hole loses full heart
            loseHp();
        }
    }


    private void addTime(int seconds) {
        timeLeftMillis += seconds * 1000L;

        if (gameTimer != null) {
            gameTimer.cancel();
        }
        startTimer(); // restart with new time
    }





    private void showScorePopup(int index) {
        final TextView popupText = scorePopups[index];

        // Reset position and appearance
        popupText.setVisibility(View.VISIBLE);
        popupText.setAlpha(1f);
        popupText.setScaleX(1f);
        popupText.setScaleY(1f);
        popupText.setTranslationY(0f);

        // Animate: scale up, move up, and fade out
        popupText.animate()
                .scaleX(1.5f)
                .scaleY(1.5f)
                .translationY(-80f)  // Move up 80 pixels
                .alpha(0f)           // Fade out
                .setDuration(800)    // 800ms animation
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        popupText.setVisibility(View.GONE);
                        popupText.setTranslationY(0f);
                        popupText.setScaleX(1f);
                        popupText.setScaleY(1f);
                    }
                })
                .start();
    }

    private void showHitAnimation(int index) {
        final ImageView moleView = holes[index];

        // Find an available explosion overlay
        ImageView tempExplosionView = null;
        for (int i = 0; i < EXPLOSION_OVERLAYS; i++) {
            if (explosions[index][i].getVisibility() == View.GONE) {
                tempExplosionView = explosions[index][i];
                break;
            }
        }

        // fallback if all busy
        if (tempExplosionView == null) {
            tempExplosionView = explosions[index][0];
            tempExplosionView.animate().cancel();
        }

        // Make a final variable for lambda
        final ImageView explosionView = tempExplosionView;

        // Reset explosion state
        explosionView.setVisibility(View.VISIBLE);
        explosionView.setAlpha(0f);
        explosionView.setRotation(0f);

        // Explosion animation: fade in + rotate + fade out
        explosionView.animate()
                .alpha(1f)
                .rotationBy(360f)
                .setDuration(200)
                .withEndAction(() -> {
                    explosionView.animate()
                            .alpha(0f)
                            .rotationBy(360f)
                            .setDuration(200)
                            .withEndAction(() -> explosionView.setVisibility(View.GONE))
                            .start();
                })
                .start();

        // Change mole to "hit" image
        moleView.setImageResource(R.drawable.molevectorded);
        playBonkSound();

        // Scale animation: quick bounce
        ScaleAnimation scaleAnim = new ScaleAnimation(
                1.0f, 1.3f,
                1.0f, 1.3f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        scaleAnim.setDuration(150);
        scaleAnim.setRepeatCount(1);
        scaleAnim.setRepeatMode(Animation.REVERSE);

        moleView.startAnimation(scaleAnim);

        // Hide mole after short delay
        moleHandler.postDelayed(() -> hideMoleWithAnimation(index), 100);
    }



    private void endGame() {
        isGameRunning = false;

        // Stop timer
        if (gameTimer != null) {
            gameTimer.cancel();
        }

        // Stop spawning moles
        moleHandler.removeCallbacksAndMessages(null);

        // Hide all moles
        hideAllMoles();

        // Enable start button
        btnStart.setEnabled(true);
        btnStart.setAlpha(1.0f);

        // Check and update high score
        if (score > highScore) {
            highScore = score;
            saveHighScore();
            updateHighScore();
            showGameOverDialog("New High Score!", "Congratulations! You scored " + score + " points!");
        } else {
            showGameOverDialog("Game Over", "Your score: " + score + " points\nHigh Score: " + highScore);
        }
    }

    private void hideAllMoles() {
        for (ImageView hole : holes) {
            hole.setImageResource(R.drawable.hole);
        }
    }

    private void updateScore() {
        tvScore.setText("Score: " + score);
    }

    private void updateTimer() {
        int seconds = (int) (timeLeftMillis / 1000);
        tvTimer.setText("Time: " + seconds);
    }


    private void updateHighScore() {
        tvHighScore.setText("High Score: " + highScore);
    }

    private void loadHighScore() {
        highScore = sharedPreferences.getInt(HIGH_SCORE_KEY, 0);
        updateHighScore();
    }

    private void saveHighScore() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(HIGH_SCORE_KEY, highScore);
        editor.apply();
    }

    private void showExitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Exit Game");
        builder.setMessage("Are you sure you want to exit?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish(); // Close the app
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss(); // Close dialog
            }
        });
        builder.setCancelable(true);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showGameOverDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setCancelable(true);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up timers and handlers
        if (gameTimer != null) {
            gameTimer.cancel();
        }
        if (moleHandler != null) {
            moleHandler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    public void onBackPressed() {
        // Show exit dialog instead of directly closing
        showExitDialog();
    }
}