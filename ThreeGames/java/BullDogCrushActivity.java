package com.calmcampus.threegames;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class BullDogCrushActivity extends AppCompatActivity {
    // ============================================================
    // GRID DIMENSIONS - Change these values to adjust grid size
    // GRID_ROWS = number of rows (height)
    // GRID_COLS = number of columns (width)
    // ============================================================


    private SoundPool soundPool;
    private int clickSound;
    private int matchSound;
    private static final int GRID_ROWS = 8;
    private static final int GRID_COLS = 8;

    private static final int GRID_SIZE = 8;

    private ImageView[][] candies = new ImageView[GRID_ROWS][GRID_COLS];
    private int[][] candyTypes = new int[GRID_ROWS][GRID_COLS];
    private int[][] powerUpTypes = new int[GRID_ROWS][GRID_COLS]; // Track power-up types
    private int score = 0;
    private int comboCount = 0;
    private int currentComboMultiplier = 1;
    private int movesLeft = 30; // Starting number of moves
    private TextView scoreText;
    private TextView comboText;
    private TextView movesText;
    private GridLayout gridLayout;
    private FrameLayout rootLayout;
    private FrameLayout boardContainer;
    private Button retryButton;
    private Button exitButton;

    // Touch/swipe variables
    private float touchStartX = 0;
    private float touchStartY = 0;
    private int touchStartRow = -1;
    private int touchStartCol = -1;
    private static final int SWIPE_THRESHOLD = 50;

    // Animation durations
    private static final int SWAP_DURATION = 250;
    private static final int MATCH_DURATION = 300;
    private static final int DROP_DURATION = 300;
    private static final int SPAWN_DURATION = 300;

    // Flag to prevent multiple simultaneous operations
    private boolean isProcessing = false;

    // Double-tap detection for power-ups
    private long lastTapTime = 0;
    private int lastTapRow = -1;
    private int lastTapCol = -1;
    private static final long DOUBLE_TAP_DELAY = 300; // 300ms for double tap

    // Tutorial mode
    private boolean isTutorialMode = true;
    private int tutorialStep = 0; // 0=firecracker, 1=bomb, 2=dynamite, 3=TNT, 4=done
    private TextView tutorialText;
    private Button skipButton;
    private ImageView tutorialArrow;
    private boolean tutorialPowerUpCreated = false; // Track if power-up was created

    // Swipe power-up activation
    private int swipePowerUpRow = -1;
    private int swipePowerUpCol = -1;
    private boolean isDraggingPowerUp = false;

    // Power-up type constants
    private static final int POWERUP_NONE = 0;
    private static final int POWERUP_FIRECRACKER_HORIZONTAL = 1;
    private static final int POWERUP_FIRECRACKER_VERTICAL = 2;
    private static final int POWERUP_BOMB = 3;
    private static final int POWERUP_DYNAMITE = 4;
    private static final int POWERUP_TNT = 5;

    private enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    private int[] candyColors = {
            R.drawable.candy_red,
            R.drawable.candy_blue,
            R.drawable.candy_green,
            R.drawable.candy_yellow,
            R.drawable.candy_purple,
            R.drawable.candy_orange
    };

    private int startSound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bulldogcrush_activity_main);


        scoreText = findViewById(R.id.scoreText);
        comboText = findViewById(R.id.comboText);
        movesText = findViewById(R.id.movesText);
        gridLayout = findViewById(R.id.gridLayout);
        retryButton = findViewById(R.id.retryButton);
        exitButton = findViewById(R.id.exitButton);

        boardContainer = findViewById(R.id.boardContainer);
        gridLayout     = findViewById(R.id.gridLayout);
        tutorialArrow = findViewById(R.id.tutorialArrow);


        // Verify critical UI elements exist
        if (gridLayout == null) {
            throw new RuntimeException("GridLayout not found! Check your XML layout file.");
        }

        // Get root layout for combo popup
        rootLayout = (FrameLayout) findViewById(android.R.id.content);

        initializeGrid();
        fillTutorialGrid(); // Start with tutorial grid
        updateMovesText();

        // Hide retry button during tutorial
        if (retryButton != null) {
            retryButton.setVisibility(android.view.View.INVISIBLE);
        }

        // Show tutorial message
        showTutorialMessage();

        if (retryButton != null) {
            retryButton.setOnClickListener(v -> resetGame());
        }
        if (exitButton != null) {
            exitButton.setOnClickListener(v -> finish());
        }

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setFlags(AudioAttributes.FLAG_LOW_LATENCY)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(12)
                .setAudioAttributes(audioAttributes)
                .build();

        clickSound = soundPool.load(this, R.raw.bubblereverse, 1);
        matchSound = soundPool.load(this, R.raw.bubblepopintro, 1);
        startSound = soundPool.load(this, R.raw.bubblepopintro, 1);


        soundPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> {
            if (status == 0 && sampleId == startSound) {
                soundPool.play(startSound, 1f, 1f, 1, 0, 1f);
            }
        });


    }

    // Bubble start sound pop
    private void playStartSound() {
        soundPool.play(startSound, 0.8f, 0.8f, 1, 0, 1f);
    }
    private void resetGame() {

        playStartSound();

        score = 0;
        comboCount = 0;
        currentComboMultiplier = 1;
        movesLeft = 30;
        isProcessing = false;

        if (scoreText != null) {
            scoreText.setText("Score: 0");
        }
        if (comboText != null) {
            comboText.setText("");
        }
        updateMovesText();

        touchStartRow = -1;
        touchStartCol = -1;
        gridLayout.removeAllViews();
        candies = new ImageView[GRID_ROWS][GRID_COLS];
        candyTypes = new int[GRID_ROWS][GRID_COLS];
        powerUpTypes = new int[GRID_ROWS][GRID_COLS];
        initializeGrid();
        fillGrid();
    }

    // Tutorial methods
    private void fillTutorialGrid() {
        // Clear grid first
        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                candyTypes[row][col] = -1;
                powerUpTypes[row][col] = POWERUP_NONE;
                candies[row][col].setImageResource(0);
            }
        }

        if (tutorialStep == 0) {
            // Tutorial Step 1: Teach Firecracker (Match 4)
            // Fixed map for firecracker tutorial
            int[][] fixedMap = {
                    {0, 1, 2, 3, 4, 5, 0, 1}, // Row 0
                    {2, 3, 4, 5, 0, 1, 2, 3}, // Row 1
                    {4, 5, 2, 1, 0, 3, 4, 5}, // Row 2
                    {1, 0, 5, 0, 5, 0, 0, 2}, // Row 3 - RED candies to match
                    {0, 5, 0, 1, 0, 3, 4, 5}, // Row 4
                    {5, 0, 1, 2, 3, 4, 5, 0}, // Row 5
                    {1, 0, 3, 4, 5, 0, 1, 2}, // Row 6
                    {3, 4, 5, 0, 1, 2, 3, 4}  // Row 7
            };
//            int[][] fixedMap = {
//                    {0, 1, 2, 3, 4, 5, 0, 1}, // Row 0
//                    {2, 3, 4, 5, 2, 1, 2, 3}, // Row 1
//                    {4, 5, 2, 1, 0, 3, 4, 5}, // Row 2
//                    {1, 2, 0, 0, 5, 0, 5, 2}, // Row 3 - RED candies to match
//                    {3, 4, 5, 1, 2, 3, 4, 5}, // Row 4
//                    {5, 0, 1, 2, 3, 4, 5, 0}, // Row 5
//                    {1, 2, 3, 4, 5, 0, 1, 2}, // Row 6
//                    {3, 4, 5, 0, 1, 2, 3, 4}  // Row 7
//            };

            for (int row = 0; row < GRID_ROWS; row++) {
                for (int col = 0; col < GRID_COLS; col++) {
                    candyTypes[row][col] = fixedMap[row][col];
                    candies[row][col].setImageResource(candyColors[fixedMap[row][col]]);
                }
            }

        } else if (tutorialStep == 1) {
            // Tutorial Step 2: Teach Bomb (Match 5)
            // Fixed map for bomb tutorial
            int[][] fixedMap = {
                    {2, 3, 4, 5, 0, 2, 3, 4}, // Row 0
                    {4, 5, 0, 2, 3, 4, 5, 0}, // Row 1
                    {0, 2, 3, 4, 5, 0, 2, 3}, // Row 2
                    {3, 4, 1, 0, 2, 3, 4, 5}, // Row 3
                    {1, 1, 5, 1, 1, 0, 3, 4}, // Row 4 - BLUE candies to match (4 + 1 nearby)
                    {5, 0, 2, 3, 4, 5, 0, 2}, // Row 5
                    {2, 3, 4, 5, 0, 2, 3, 4}, // Row 6
                    {4, 5, 0, 2, 3, 4, 5, 0}  // Row 7
            };

            for (int row = 0; row < GRID_ROWS; row++) {
                for (int col = 0; col < GRID_COLS; col++) {
                    candyTypes[row][col] = fixedMap[row][col];
                    candies[row][col].setImageResource(candyColors[fixedMap[row][col]]);
                }
            }

        } else if (tutorialStep == 2) {
            // Tutorial Step 3.1: Teach Dynamite (Match 6)

            //        D
            //        E
            //    A B c C
            //        F

            // Fixed map for dynamite tutorial
            int[][] fixedMap = {
                    {0, 1, 3, 4, 5, 0, 1, 3}, // Row 0
                    {3, 4, 5, 2, 1, 3, 4, 5}, // Row 1
                    {5, 0, 1, 2, 4, 5, 0, 1}, // Row 2
                    {1, 2, 2, 3, 2, 0, 3, 5}, // Row 3 - GREEN candies to match (5 + 1 nearby)
                    {4, 5, 0, 2, 3, 4, 5, 1}, // Row 4
                    {0, 1, 3, 4, 5, 0, 1, 3}, // Row 5
                    {3, 4, 5, 0, 1, 3, 4, 5}, // Row 6
                    {5, 0, 1, 3, 4, 5, 0, 1}  // Row 7
            };

            for (int row = 0; row < GRID_ROWS; row++) {
                for (int col = 0; col < GRID_COLS; col++) {
                    candyTypes[row][col] = fixedMap[row][col];
                    candies[row][col].setImageResource(candyColors[fixedMap[row][col]]);
                }
            }

        } else if (tutorialStep == 3) {
            // Tutorial Step 3.2: Teach Dynamite (Match 6)

            //        D
            //        E
            //      A x B C
            //        F

            // Fixed map for dynamite tutorial
            int[][] fixedMap = {
                    {0, 1, 3, 4, 5, 0, 1, 3}, // Row 0
                    {3, 4, 5, 2, 1, 3, 4, 5}, // Row 1
                    {2, 0, 1, 2, 4, 5, 0, 1}, // Row 2
                    {1, 5, 2, 3, 2, 2, 3, 5}, // Row 3 - GREEN candies to match (5 + 1 nearby)
                    {4, 5, 0, 2, 3, 4, 5, 1}, // Row 4
                    {0, 1, 3, 4, 5, 0, 1, 3}, // Row 5
                    {3, 4, 5, 0, 1, 3, 4, 5}, // Row 6
                    {5, 0, 1, 3, 4, 5, 0, 1}  // Row 7
            };

            for (int row = 0; row < GRID_ROWS; row++) {
                for (int col = 0; col < GRID_COLS; col++) {
                    candyTypes[row][col] = fixedMap[row][col];
                    candies[row][col].setImageResource(candyColors[fixedMap[row][col]]);
                }
            }

        } else if (tutorialStep == 4) {
            // Tutorial Step 3.3: Teach Dynamite (Match 6)

            //        D
            //      A x B C
            //        E
            //        F

            // Fixed map for dynamite tutorial
            int[][] fixedMap = {
                    {0, 1, 3, 4, 5, 0, 1, 3}, // Row 0
                    {3, 4, 5, 0, 1, 3, 4, 5}, // Row 1
                    {2, 0, 1, 2, 4, 5, 0, 1}, // Row 2
                    {1, 5, 2, 3, 2, 2, 3, 5}, // Row 3 - GREEN candies to match (5 + 1 nearby)
                    {4, 5, 0, 2, 3, 4, 5, 1}, // Row 4
                    {0, 1, 3, 2, 5, 0, 1, 3}, // Row 5
                    {3, 4, 5, 0, 1, 3, 4, 5}, // Row 6
                    {5, 0, 1, 3, 4, 5, 0, 1}  // Row 7
            };

            for (int row = 0; row < GRID_ROWS; row++) {
                for (int col = 0; col < GRID_COLS; col++) {
                    candyTypes[row][col] = fixedMap[row][col];
                    candies[row][col].setImageResource(candyColors[fixedMap[row][col]]);
                }
            }

        } else if (tutorialStep == 5) {
            // Tutorial Step 3.4: Teach Dynamite (Match 6)

            //          D
            //      A B x C
            //          E
            //          F

            // Fixed map for dynamite tutorial
            int[][] fixedMap = {
                    {0, 1, 3, 4, 5, 0, 1, 3}, // Row 0
                    {3, 4, 5, 0, 1, 3, 4, 5}, // Row 1
                    {5, 0, 1, 2, 4, 5, 0, 1}, // Row 2
                    {1, 2, 2, 3, 2, 1, 3, 5}, // Row 3 - GREEN candies to match (5 + 1 nearby)
                    {4, 5, 0, 2, 3, 4, 5, 1}, // Row 4
                    {0, 1, 3, 2, 5, 0, 1, 3}, // Row 5
                    {3, 4, 5, 0, 1, 3, 4, 5}, // Row 6
                    {5, 0, 1, 3, 4, 5, 0, 1}  // Row 7
            };

            for (int row = 0; row < GRID_ROWS; row++) {
                for (int col = 0; col < GRID_COLS; col++) {
                    candyTypes[row][col] = fixedMap[row][col];
                    candies[row][col].setImageResource(candyColors[fixedMap[row][col]]);
                }
            }

        } else if (tutorialStep == 6) {
            // Tutorial Step 4.1: Teach TNT (Match 7+)

            //        F
            //    A B c D E
            //        C
            //        G

            int[][] fixedMap = {
                    {0, 1, 2, 4, 5, 0, 1, 2}, // Row 0
                    {4, 5, 0, 5, 2, 4, 5, 0}, // Row 1
                    {2, 4, 5, 3, 1, 2, 4, 5}, // Row 2
                    {0, 3, 3, 4, 3, 3, 2, 4}, // Row 3 - T-top: 3 yellows horizontal (cols 2,3,4)
                    {2, 0, 1, 3, 5, 4, 0, 1}, // Row 4 - T-stem: yellow at col 3
                    {1, 2, 4, 3, 0, 1, 2, 4}, // Row 5 - T-stem: yellow at col 3
                    {5, 0, 1, 2, 4, 5, 0, 1}, // Row 6 - T-stem: yellow at col 3
                    {2, 4, 5, 1, 3, 2, 4, 5}  // Row 7 - SWAP col 3 (blue) with col 4 (yellow) to complete T!
            };

            for (int row = 0; row < GRID_ROWS; row++) {
                for (int col = 0; col < GRID_COLS; col++) {
                    candyTypes[row][col] = fixedMap[row][col];
                    candies[row][col].setImageResource(candyColors[fixedMap[row][col]]);
                }
            }
        } else if (tutorialStep == 7) {

            //        G
            //        F
            //    A B c D E
            //        C

            int[][] fixedMap = {
                    {0, 1, 2, 4, 5, 0, 1, 2}, // Row 0
                    {4, 5, 0, 3, 2, 4, 5, 0}, // Row 1
                    {2, 4, 5, 3, 1, 2, 4, 5}, // Row 2
                    {0, 3, 3, 4, 3, 3, 2, 4}, // Row 3 -
                    {2, 0, 1, 3, 5, 4, 0, 1}, // Row 4 -
                    {1, 2, 4, 5, 0, 1, 2, 4}, // Row 5 -
                    {5, 0, 1, 2, 4, 5, 0, 1}, // Row 6 -
                    {2, 4, 5, 1, 3, 2, 4, 5}  // Row 7 -
            };

            for (int row = 0; row < GRID_ROWS; row++) {
                for (int col = 0; col < GRID_COLS; col++) {
                    candyTypes[row][col] = fixedMap[row][col];
                    candies[row][col].setImageResource(candyColors[fixedMap[row][col]]);
                }
            }
        }
        else if (tutorialStep == 8) {

            //        A
            //        B
            //      C c F G
            //        D
            //        E

            int[][] fixedMap = {
                    {0, 1, 2, 4, 5, 0, 1, 2}, // Row 0
                    {4, 5, 0, 3, 2, 4, 5, 0}, // Row 1
                    {2, 4, 5, 3, 1, 2, 4, 5}, // Row 2
                    {0, 1, 3, 4, 3, 3, 2, 4}, // Row 3
                    {2, 0, 1, 3, 5, 4, 0, 1}, // Row 4
                    {1, 2, 4, 3, 0, 1, 2, 4}, // Row 5
                    {5, 0, 1, 2, 4, 5, 0, 1}, // Row 6
                    {2, 4, 5, 1, 3, 2, 4, 5}  // Row 7
            };

            for (int row = 0; row < GRID_ROWS; row++) {
                for (int col = 0; col < GRID_COLS; col++) {
                    candyTypes[row][col] = fixedMap[row][col];
                    candies[row][col].setImageResource(candyColors[fixedMap[row][col]]);
                }
            }
        }
        else if (tutorialStep == 9) {

            //        A
            //        B
            //    G F c C
            //        D
            //        E

            int[][] fixedMap = {
                    {0, 1, 2, 4, 5, 0, 1, 2}, // Row 0
                    {4, 5, 0, 3, 2, 4, 5, 0}, // Row 1
                    {2, 4, 5, 3, 1, 2, 4, 5}, // Row 2
                    {0, 3, 3, 4, 3, 0, 2, 4}, // Row 3
                    {2, 0, 1, 3, 5, 4, 0, 1}, // Row 4
                    {1, 2, 4, 3, 0, 1, 2, 4}, // Row 5
                    {5, 0, 1, 2, 4, 5, 0, 1}, // Row 6
                    {2, 4, 5, 1, 3, 2, 4, 5}  // Row 7
            };

            for (int row = 0; row < GRID_ROWS; row++) {
                for (int col = 0; col < GRID_COLS; col++) {
                    candyTypes[row][col] = fixedMap[row][col];
                    candies[row][col].setImageResource(candyColors[fixedMap[row][col]]);
                }
            }
        }
    }

    private void showTutorialMessage() {
        // Create container layout
        android.widget.LinearLayout tutorialContainer = new android.widget.LinearLayout(this);
        tutorialContainer.setOrientation(android.widget.LinearLayout.VERTICAL);
        tutorialContainer.setBackgroundColor(0xDD000000);
        tutorialContainer.setPadding(40, 40, 40, 40);
        tutorialContainer.setGravity(Gravity.CENTER);

        FrameLayout.LayoutParams containerParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        containerParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        containerParams.topMargin = 20;
        tutorialContainer.setLayoutParams(containerParams);

        // Tutorial text
        tutorialText = new TextView(this);
        updateTutorialText();
        tutorialText.setTextSize(18);
        tutorialText.setTextColor(getResources().getColor(android.R.color.white));
        tutorialText.setGravity(Gravity.CENTER);

        tutorialContainer.addView(tutorialText);

        // Skip button
        skipButton = new Button(this);
        skipButton.setText("Skip Tutorial");
        skipButton.setTextSize(16);
        skipButton.setTextColor(getResources().getColor(android.R.color.white));
        skipButton.setBackgroundColor(0xFFFF5722);
        skipButton.setPadding(40, 15, 40, 15);

        android.widget.LinearLayout.LayoutParams buttonParams = new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
        );
        buttonParams.topMargin = 30;
        skipButton.setLayoutParams(buttonParams);

        skipButton.setOnClickListener(v -> {
            rootLayout.removeView(tutorialContainer);
            isTutorialMode = false;

            // Show retry button when skipping tutorial
            if (retryButton != null) {
                retryButton.setVisibility(android.view.View.VISIBLE);
            }

            resetGame();
        });

        tutorialContainer.addView(skipButton);
        rootLayout.addView(tutorialContainer);

        // Animate in
        tutorialContainer.setScaleX(0f);
        tutorialContainer.setScaleY(0f);
        tutorialContainer.setAlpha(0f);

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(tutorialContainer, "scaleX", 0f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(tutorialContainer, "scaleY", 0f, 1.0f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(tutorialContainer, "alpha", 0f, 1.0f);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleX, scaleY, alpha);
        set.setDuration(400);
        set.setInterpolator(new OvershootInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // Show arrow after tutorial message appears
                new Handler().postDelayed(() -> {

                }, 300);
            }
        });
        set.start();
    }

    private void updateTutorialText() {
        String text = "";
        switch (tutorialStep) {
            case 0:
                text = "STEP 1: FIRECRACKER 🎆\n\n" +
                        "Match 4 candies in a row\n" +
                        "to create a Firecracker!\n\n" +
                        "It clears a whole line!\n\n" +
                        "Swap the candies!";
                break;
            case 1:
                text = "STEP 2: BOMB 💣\n\n" +
                        "Match 5 candies in a row\n" +
                        "to create a Bomb!\n\n" +
                        "It clears a 3x3 area!\n\n" +
                        "Make a bomb!";
                break;
            case 2:
                text = "STEP 3: DYNAMITE 🧨\n\n" +
                        "Match 6 candies in a row\n" +
                        "to create Dynamite!\n\n" +
                        "It clears a 5x5 area!\n\n" +
                        "Create dynamite!";
                break;
            case 3:
                text = "STEP 3.1: DYNAMITE 🧨\n\n" +
                        "Match 6 candies in a row\n" +
                        "to create Dynamite!\n\n" +
                        "It clears a 5x5 area!\n\n" +
                        "Create dynamite!";
                break;
            case 4:
                text = "STEP 3.2: DYNAMITE 🧨\n\n" +
                        "Match 6 candies in a row\n" +
                        "to create Dynamite!\n\n" +
                        "It clears a 5x5 area!\n\n" +
                        "Create dynamite!";
                break;
            case 5:
                text = "STEP 3.3: DYNAMITE 🧨\n\n" +
                        "Match 6 candies in a row\n" +
                        "to create Dynamite!\n\n" +
                        "It clears a 5x5 area!\n\n" +
                        "Create dynamite!";
                break;
            case 6:
                text = "STEP 4.3: TNT 💥\n\n" +
                        "Match 7+ candies\n" +
                        "to create TNT!\n\n" +
                        "Form a T-SHAPE combo!\n" +
                        "It clears a MASSIVE 7x7 area!\n\n" +
                        "Make the T!";
                break;
            case 7:
                text = "STEP 4: TNT 💥\n\n" +
                        "Match 7+ candies\n" +
                        "to create TNT!\n\n" +
                        "Form a T-SHAPE combo!\n" +
                        "It clears a MASSIVE 7x7 area!\n\n" +
                        "Make the T!";
                break;
            case 8:
                text = "STEP 4.2: TNT 💥\n\n" +
                        "Match 7+ candies\n" +
                        "to create TNT!\n\n" +
                        "Form a T-SHAPE combo!\n" +
                        "It clears a MASSIVE 7x7 area!\n\n" +
                        "Make the T!";
                break;
            case 9:
                text = "STEP 4.4: TNT 💥\n\n" +
                        "Match 7+ candies\n" +
                        "to create TNT!\n\n" +
                        "Form a T-SHAPE combo!\n" +
                        "It clears a MASSIVE 7x7 area!\n\n" +
                        "Make the T!";
                break;
        }
        if (tutorialText != null) {
            tutorialText.setText(text);
        }
    }



    private void advanceTutorial() {
        tutorialStep++;
        tutorialPowerUpCreated = false; // Reset for next step

        // Remove arrow
        if (tutorialArrow != null) {
            rootLayout.removeView(tutorialArrow);
            tutorialArrow = null;
        }

        if (tutorialStep >= 10) {
            // Tutorial complete
            if (tutorialText != null && tutorialText.getParent() != null) {
                rootLayout.removeView((android.view.View) tutorialText.getParent());
            }
            isTutorialMode = false;

            // Show retry button now that tutorial is done
            if (retryButton != null) {
                retryButton.setVisibility(android.view.View.VISIBLE);
            }

            resetGame();
        } else {
            // Next tutorial step
            updateTutorialText();
            fillTutorialGrid();
            // Show arrow after a delay to let grid settle
            new Handler().postDelayed(() -> {
            }, 500);
        }
    }


    private void updateMovesText() {
        if (movesText != null) {
            movesText.setText("Moves: " + movesLeft);
        }

        // Check for game over
        if (movesLeft <= 0) {
            showGameOver();
        }
    }

    private void showGameOver() {
        isProcessing = true;

        // Create game over popup
        TextView gameOverText = new TextView(this);
        gameOverText.setText("Game Over!\nFinal Score: " + score);
        gameOverText.setTextSize(36);
        gameOverText.setTextColor(getResources().getColor(android.R.color.white));
        gameOverText.setBackgroundColor(getResources().getColor(android.R.color.black));
        gameOverText.setGravity(Gravity.CENTER);
        gameOverText.setPadding(60, 60, 60, 60);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.CENTER;
        gameOverText.setLayoutParams(params);

        rootLayout.addView(gameOverText);

        gameOverText.setScaleX(0f);
        gameOverText.setScaleY(0f);
        gameOverText.setAlpha(0f);

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(gameOverText, "scaleX", 0f, 1.2f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(gameOverText, "scaleY", 0f, 1.2f, 1.0f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(gameOverText, "alpha", 0f, 1.0f);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleX, scaleY, alpha);
        set.setDuration(600);
        set.setInterpolator(new OvershootInterpolator());
        set.start();

        // Auto-remove after 3 seconds
        new Handler().postDelayed(() -> {
            rootLayout.removeView(gameOverText);
        }, 3000);
    }

    private void initializeGrid() {
        // ⚠️ IMPORTANT: Make sure your GridLayout in XML has these properties:
        // android:layout_width="match_parent"  ← Must be match_parent, not wrap_content!
        // android:layout_gravity="center"
        // If rightmost column is cut off, check your activity_main.xml

        // Set the grid layout dimensions (columns x rows)
        gridLayout.setColumnCount(GRID_COLS);  // Number of columns (width)
        gridLayout.setRowCount(GRID_ROWS);      // Number of rows (height)

        // Calculate candy size based on BOTH screen dimensions to ensure it fits
        // Get available screen space
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int screenHeight = getResources().getDisplayMetrics().heightPixels;

        // Reserve space for UI elements (score, buttons, status bar, etc.)
        // ⚠️ ADJUST THIS VALUE if the grid is still cut off or too small:
        // - If bottom row is cut off → INCREASE this number (try 450, 500, 550)
        // - If grid is too small with empty space → DECREASE this number (try 350, 300)
        int reservedHeight = 400; // Start with 400 and adjust as needed
        int availableHeight = screenHeight - reservedHeight;

        // Account for margins between candies (1 pixel on each side = 2 pixels per candy)
        int marginPerCandy = 2; // 1px left + 1px right (or top + bottom)
        int totalWidthMargins = GRID_COLS * marginPerCandy;
        int totalHeightMargins = GRID_ROWS * marginPerCandy;

        // Calculate maximum candy size based on width (subtract total margins)
        int candySizeByWidth = (screenWidth - totalWidthMargins) / GRID_COLS;

        // Calculate maximum candy size based on height (subtract total margins)
        int candySizeByHeight = (availableHeight - totalHeightMargins) / GRID_ROWS;

        // Use the SMALLER of the two to ensure grid fits on screen
        int candySize = Math.min(candySizeByWidth, candySizeByHeight);

        // Add debug logging to see what's happening
        System.out.println("========== GRID CALCULATIONS ==========");
        System.out.println("Screen: " + screenWidth + "x" + screenHeight);
        System.out.println("Grid: " + GRID_COLS + " cols x " + GRID_ROWS + " rows");
        System.out.println("Available height: " + availableHeight);
        System.out.println("Total margins (width): " + totalWidthMargins);
        System.out.println("Total margins (height): " + totalHeightMargins);
        System.out.println("Candy size by width: " + candySizeByWidth);
        System.out.println("Candy size by height: " + candySizeByHeight);
        System.out.println("Final candy size: " + candySize);
        System.out.println("Total grid width: " + (candySize * GRID_COLS + totalWidthMargins));
        System.out.println("Total grid height: " + (candySize * GRID_ROWS + totalHeightMargins));
        System.out.println("======================================");

        // Create candy ImageViews and add to grid
        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                ImageView candy = new ImageView(this);


                // CRITICAL: Use exact calculated size for both width AND height
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = candySize;  // Use calculated size
                params.height = candySize; // Use calculated size

                // Set 1 pixel margin on all sides (total 2px between candies)
                params.setMargins(1, 1, 1, 1);

                // Explicitly set row and column (prevents layout issues)
                params.rowSpec = GridLayout.spec(row);
                params.columnSpec = GridLayout.spec(col);

                candy.setLayoutParams(params);
                candy.setScaleType(ImageView.ScaleType.FIT_XY);
                candy.setBackgroundResource(R.drawable.candy_cell_background);
                candy.setPadding(3, 3, 3, 3);

                final int currentRow = row;
                final int currentCol = col;

                candy.setOnTouchListener((v, event) -> {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            onTouchDown(currentRow, currentCol, event);
                            return true;
                        case MotionEvent.ACTION_UP:
                            onTouchUp(currentRow, currentCol, event);
                            return true;
                    }
                    return false;
                });

                candies[row][col] = candy;
                powerUpTypes[row][col] = POWERUP_NONE;
                gridLayout.addView(candy);
            }
        }
    }

    private void onTouchDown(int row, int col, MotionEvent event) {
        if (isProcessing || movesLeft <= 0) return;

        // Check if this is a power-up
        if (powerUpTypes[row][col] != POWERUP_NONE) {
            // Mark that we're starting a power-up drag
            swipePowerUpRow = row;
            swipePowerUpCol = col;
            isDraggingPowerUp = true;

            // Also check for double-tap for backwards compatibility
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastTapTime < DOUBLE_TAP_DELAY &&
                    lastTapRow == row && lastTapCol == col) {
                // Double tap detected - explode the power-up
                activatePowerUp(row, col);
                lastTapTime = 0; // Reset
                lastTapRow = -1;
                lastTapCol = -1;
                swipePowerUpRow = -1;
                swipePowerUpCol = -1;
                isDraggingPowerUp = false;
                return;
            } else {
                lastTapTime = currentTime;
                lastTapRow = row;
                lastTapCol = col;
            }
        }

        touchStartRow = row;
        touchStartCol = col;
        touchStartX = event.getRawX();
        touchStartY = event.getRawY();

        playClickSound();
        animatePress(candies[row][col]);
    }

    private void onTouchUp(int row, int col, MotionEvent event) {
        if (isProcessing || movesLeft <= 0) return;
        if (touchStartRow == -1 || touchStartCol == -1) return;

        float dx = event.getRawX() - touchStartX;
        float dy = event.getRawY() - touchStartY;

        // Check if we were dragging a power-up and swiped
        if (isDraggingPowerUp && swipePowerUpRow != -1 && swipePowerUpCol != -1) {
            if (Math.abs(dx) > SWIPE_THRESHOLD || Math.abs(dy) > SWIPE_THRESHOLD) {
                // Swipe detected on power-up - activate it!
                activatePowerUp(swipePowerUpRow, swipePowerUpCol);
                swipePowerUpRow = -1;
                swipePowerUpCol = -1;
                isDraggingPowerUp = false;
                touchStartRow = -1;
                touchStartCol = -1;
                return;
            }
        }

        // Reset power-up drag state
        isDraggingPowerUp = false;
        swipePowerUpRow = -1;
        swipePowerUpCol = -1;

        if (Math.abs(dx) > SWIPE_THRESHOLD || Math.abs(dy) > SWIPE_THRESHOLD) {
            int targetRow = touchStartRow;
            int targetCol = touchStartCol;

            if (Math.abs(dx) > Math.abs(dy)) {
                targetCol += (dx > 0) ? 1 : -1;
            } else {
                targetRow += (dy > 0) ? 1 : -1;
            }

            if (targetRow >= 0 && targetRow < GRID_ROWS && targetCol >= 0 && targetCol < GRID_COLS) {
                attemptSwap(touchStartRow, touchStartCol, targetRow, targetCol);
            }
        }

        touchStartRow = -1;
        touchStartCol = -1;
    }

    private void attemptSwap(int r1, int c1, int r2, int c2) {
        isProcessing = true;
        animateSwap(r1, c1, r2, c2, true);
    }

    private void animateSwap(int r1, int c1, int r2, int c2, boolean checkForMatches) {
        ImageView candy1 = candies[r1][c1];
        ImageView candy2 = candies[r2][c2];

        float dx = (c2 - c1) * candy1.getWidth();
        float dy = (r2 - r1) * candy1.getHeight();

        ObjectAnimator anim1X = ObjectAnimator.ofFloat(candy1, "translationX", 0f, dx);
        ObjectAnimator anim1Y = ObjectAnimator.ofFloat(candy1, "translationY", 0f, dy);
        ObjectAnimator anim2X = ObjectAnimator.ofFloat(candy2, "translationX", 0f, -dx);
        ObjectAnimator anim2Y = ObjectAnimator.ofFloat(candy2, "translationY", 0f, -dy);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(anim1X, anim1Y, anim2X, anim2Y);
        set.setDuration(SWAP_DURATION);
        set.setInterpolator(new DecelerateInterpolator());

        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                candy1.setTranslationX(0);
                candy1.setTranslationY(0);
                candy2.setTranslationX(0);
                candy2.setTranslationY(0);

                swapCandies(r1, c1, r2, c2);

                if (checkForMatches) {
                    new Handler().postDelayed(() -> {
                        if (checkMatches()) {
                            // Successful move - decrement moves
                            movesLeft--;
                            updateMovesText();

                            comboCount++;
                            updateComboMultiplier();
                            new Handler().postDelayed(() -> processMatches(), MATCH_DURATION);
                        } else {
                            animateSwap(r1, c1, r2, c2, false);
                            new Handler().postDelayed(() -> {
                                isProcessing = false;
                                resetCombo();
                            }, SWAP_DURATION);
                        }
                    }, 100);
                } else {
                    isProcessing = false;
                }
            }
        });
        set.start();
    }

    private void updateComboMultiplier() {
        currentComboMultiplier = 1 + (comboCount - 1);
        if (comboCount > 1) {
            if (comboText != null) {
                comboText.setText("Combo: " + comboCount + "!");
            }
            showComboPopup(comboCount);
        }
    }

    private void resetCombo() {
        comboCount = 0;
        currentComboMultiplier = 1;
        if (comboText != null) {
            comboText.setText("");
        }
    }

    private void processMatches() {
        removeMatches();
        new Handler().postDelayed(() -> {
            dropCandies();
            new Handler().postDelayed(() -> {
                spawnNewCandies();
                new Handler().postDelayed(() -> {
                    if (checkMatches()) {
                        comboCount++;
                        updateComboMultiplier();
                        new Handler().postDelayed(() -> processMatches(), MATCH_DURATION);
                    } else {
                        // Check if in tutorial mode and a power-up was created
                        if (isTutorialMode && !tutorialPowerUpCreated) {
                            boolean foundPowerUp = false;
                            for (int row = 0; row < GRID_ROWS; row++) {
                                for (int col = 0; col < GRID_COLS; col++) {
                                    if (powerUpTypes[row][col] != POWERUP_NONE) {
                                        foundPowerUp = true;
                                        break;
                                    }
                                }
                                if (foundPowerUp) break;
                            }
                            if (foundPowerUp) {
                                // Power-up created! Hide arrow and update text
                                tutorialPowerUpCreated = true;
                                if (tutorialArrow != null) {
                                    ObjectAnimator fadeOut = ObjectAnimator.ofFloat(tutorialArrow, "alpha", 1.0f, 0f);
                                    fadeOut.setDuration(300);
                                    fadeOut.addListener(new AnimatorListenerAdapter() {
                                        @Override
                                        public void onAnimationEnd(Animator animation) {
                                            if (tutorialArrow != null) {
                                                rootLayout.removeView(tutorialArrow);
                                                tutorialArrow = null;
                                            }
                                        }
                                    });
                                    fadeOut.start();
                                }

                                // Update tutorial text to instruct double-tap
                                if (tutorialText != null) {
                                    String bombName = "";
                                    switch (tutorialStep) {
                                        case 0: bombName = "Firecracker"; break;
                                        case 1: bombName = "Bomb"; break;
                                        case 2: bombName = "Dynamite"; break;
                                        case 3: bombName = "TNT"; break;
                                    }
                                    tutorialText.setText("Great! " + bombName + " created! 🎉\n\n" +
                                            "Now DOUBLE-TAP it\n" +
                                            "to explode and see\n" +
                                            "what it does!");
                                }
                            }
                        }

                        isProcessing = false;
                        resetCombo();
                    }
                }, SPAWN_DURATION);
            }, DROP_DURATION);
        }, MATCH_DURATION);
    }

    private void removeMatches() {
        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                if (candyTypes[row][col] == -1 && powerUpTypes[row][col] == POWERUP_NONE) {
                    animateMatch(candies[row][col]);
                    int points = 10 * currentComboMultiplier;
                    score += points;
                    if (scoreText != null) {
                        scoreText.setText("Score: " + score);
                    }
                    playMatchSound();
                }
            }
        }
    }

    private void dropCandies() {
        for (int col = 0; col < GRID_COLS; col++) {
            int emptyRow = GRID_ROWS - 1;
            for (int row = GRID_ROWS - 1; row >= 0; row--) {
                if (candyTypes[row][col] != -1) {
                    if (row != emptyRow) {
                        final int fromRow = row;
                        final int toRow = emptyRow;
                        final int column = col;

                        candyTypes[toRow][column] = candyTypes[fromRow][column];
                        powerUpTypes[toRow][column] = powerUpTypes[fromRow][column];
                        candyTypes[fromRow][column] = -1;
                        powerUpTypes[fromRow][column] = POWERUP_NONE;

                        animateDrop(fromRow, toRow, column);
                    }
                    emptyRow--;
                }
            }
        }
    }

    private void spawnNewCandies() {
        Random random = new Random();
        for (int col = 0; col < GRID_COLS; col++) {
            for (int row = 0; row < GRID_ROWS; row++) {
                if (candyTypes[row][col] == -1) {
                    int type;
                    do {
                        type = random.nextInt(candyColors.length);
                    } while (wouldCreateMatch(row, col, type));

                    candyTypes[row][col] = type;
                    powerUpTypes[row][col] = POWERUP_NONE;
                    candies[row][col].setImageResource(candyColors[type]);
                    animateSpawn(candies[row][col]);
                }
            }
        }
    }

    private void animateDrop(int fromRow, int toRow, int col) {
        ImageView fromCandy = candies[fromRow][col];
        ImageView toCandy = candies[toRow][col];

        int distance = (toRow - fromRow);
        float dropDistance = distance * fromCandy.getHeight();

        // Set the destination candy with the correct image
        if (powerUpTypes[toRow][col] != POWERUP_NONE) {
            toCandy.setImageResource(getPowerUpDrawable(powerUpTypes[toRow][col]));
        } else {
            toCandy.setImageResource(candyColors[candyTypes[toRow][col]]);
        }
        toCandy.setTranslationY(-dropDistance);
        toCandy.setAlpha(1.0f);

        ObjectAnimator drop = ObjectAnimator.ofFloat(toCandy, "translationY", -dropDistance, 0);
        drop.setDuration(DROP_DURATION);
        drop.setInterpolator(new BounceInterpolator());
        drop.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                toCandy.setTranslationY(0);
            }
        });
        drop.start();

        fromCandy.setImageResource(0);
    }

    private void animateSpawn(ImageView candy) {
        candy.setScaleX(0f);
        candy.setScaleY(0f);
        candy.setAlpha(0f);

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(candy, "scaleX", 0f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(candy, "scaleY", 0f, 1.0f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(candy, "alpha", 0f, 1.0f);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleX, scaleY, alpha);
        set.setDuration(SPAWN_DURATION);
        set.setInterpolator(new OvershootInterpolator());
        set.start();
    }

    private void playClickSound() {
        soundPool.play(clickSound, 0.3f, 0.3f, 1, 0, 1f);
    }

    private void playMatchSound() {
        soundPool.play(matchSound, 0.5f, 0.5f, 1, 0, 1f);
    }

    private void showComboPopup(int combo) {
        TextView comboPopup = new TextView(this);

        String comboText;
        int textColor;

        comboPopup.setText("COMBO x" + combo + "!");
        comboPopup.setTextSize(32);
        comboPopup.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
        comboPopup.setGravity(Gravity.CENTER);

        if (combo == 2) {
            comboText = "NICE! x2";
            textColor = 0xFF4CAF50;
        } else if (combo == 3) {
            comboText = "GREAT! x3";
            textColor = 0xFF2196F3;
        } else if (combo == 4) {
            comboText = "AMAZING! x4";
            textColor = 0xFF9C27B0;
        } else if (combo >= 5) {
            comboText = "INCREDIBLE! x" + combo;
            textColor = 0xFFFF5722;
        } else {
            comboText = "COMBO x" + combo;
            textColor = 0xFFFF9800;
        }
        comboPopup.setText(comboText);
        comboPopup.setTextSize(32);
        comboPopup.setTextColor(textColor);
        comboPopup.setShadowLayer(8, 0, 0, 0xFF000000);
        comboPopup.setTypeface(null, android.graphics.Typeface.BOLD);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.CENTER;
        comboPopup.setLayoutParams(params);

        rootLayout.addView(comboPopup);

        comboPopup.setScaleX(0f);
        comboPopup.setScaleY(0f);
        comboPopup.setAlpha(0f);

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(comboPopup, "scaleX", 0f, 1.5f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(comboPopup, "scaleY", 0f, 1.5f, 1.0f);
        ObjectAnimator alphaIn = ObjectAnimator.ofFloat(comboPopup, "alpha", 0f, 1.0f);

        AnimatorSet setIn = new AnimatorSet();
        setIn.playTogether(scaleX, scaleY, alphaIn);
        setIn.setDuration(500);

        // Add slower fade out after display
        setIn.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                new Handler().postDelayed(() -> {
                    ObjectAnimator fadeOut = ObjectAnimator.ofFloat(comboPopup, "alpha", 1.0f, 0f);
                    fadeOut.setDuration(1500); // Slower fade: 1.5 seconds
                    fadeOut.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            rootLayout.removeView(comboPopup);
                        }
                    });
                    fadeOut.start();
                }, 1000); // Stay visible for 1 second before fading
            }
        });
        setIn.start();
    }

    private void animateMatch(ImageView candy) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(candy, "scaleX", 1.0f, 1.3f, 0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(candy, "scaleY", 1.0f, 1.3f, 0f);
        ObjectAnimator rotation = ObjectAnimator.ofFloat(candy, "rotation", 0f, 360f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(candy, "alpha", 1.0f, 0f);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleX, scaleY, rotation, alpha);
        set.setDuration(MATCH_DURATION);
        set.setInterpolator(new AccelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                candy.setScaleX(1.0f);
                candy.setScaleY(1.0f);
                candy.setRotation(0f);
                candy.setAlpha(1.0f);
            }
        });
        set.start();
    }

    private void animatePress(ImageView candy) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(candy, "scaleX", 1.0f, 0.9f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(candy, "scaleY", 1.0f, 0.9f, 1.0f);
        ObjectAnimator rotation = ObjectAnimator.ofFloat(candy, "rotation", 0f, -5f, 5f, 0f);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleX, scaleY, rotation);
        set.setDuration(200);
        set.setInterpolator(new OvershootInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                candy.setRotation(0f);
            }
        });
        set.start();
    }

    private void fillGrid() {
        Random random = new Random();
        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                int type;
                do {
                    type = random.nextInt(candyColors.length);
                } while (wouldCreateMatch(row, col, type));

                candyTypes[row][col] = type;
                powerUpTypes[row][col] = POWERUP_NONE;
                candies[row][col].setImageResource(candyColors[type]);

                candies[row][col].setScaleX(0f);
                candies[row][col].setScaleY(0f);
                candies[row][col].setAlpha(0f);

                final ImageView candy = candies[row][col];
                final int delay = (row + col) * 30;

                new Handler().postDelayed(() -> {
                    ObjectAnimator scaleX = ObjectAnimator.ofFloat(candy, "scaleX", 0f, 1.0f);
                    ObjectAnimator scaleY = ObjectAnimator.ofFloat(candy, "scaleY", 0f, 1.0f);
                    ObjectAnimator alpha = ObjectAnimator.ofFloat(candy, "alpha", 0f, 1.0f);

                    AnimatorSet set = new AnimatorSet();
                    set.playTogether(scaleX, scaleY, alpha);
                    set.setDuration(300);
                    set.setInterpolator(new OvershootInterpolator());
                    set.start();
                }, delay);
            }
        }
    }

    private boolean wouldCreateMatch(int row, int col, int type) {
        if (col >= 2 && candyTypes[row][col-1] == type && candyTypes[row][col-2] == type) {
            return true;
        }
        if (row >= 2 && candyTypes[row-1][col] == type && candyTypes[row-2][col] == type) {
            return true;
        }
        return false;
    }

    private void swapCandies(int r1, int c1, int r2, int c2) {
        int temp = candyTypes[r1][c1];
        candyTypes[r1][c1] = candyTypes[r2][c2];
        candyTypes[r2][c2] = temp;

        int tempPowerUp = powerUpTypes[r1][c1];
        powerUpTypes[r1][c1] = powerUpTypes[r2][c2];
        powerUpTypes[r2][c2] = tempPowerUp;

        if (powerUpTypes[r1][c1] != POWERUP_NONE) {
            candies[r1][c1].setImageResource(getPowerUpDrawable(powerUpTypes[r1][c1]));
        } else {
            candies[r1][c1].setImageResource(candyColors[candyTypes[r1][c1]]);
        }

        if (powerUpTypes[r2][c2] != POWERUP_NONE) {
            candies[r2][c2].setImageResource(getPowerUpDrawable(powerUpTypes[r2][c2]));
        } else {
            candies[r2][c2].setImageResource(candyColors[candyTypes[r2][c2]]);
        }
    }

    private boolean checkSpecialShapes(boolean[][] matched) {
        boolean hasMatch = false;

        // Check for +-shapes (plus shapes) - 5 candies in a + pattern
        for (int row = 1; row < GRID_ROWS - 1; row++) {
            for (int col = 1; col < GRID_COLS - 1; col++) {
                int centerType = candyTypes[row][col];
                if (centerType == -1 || powerUpTypes[row][col] != POWERUP_NONE) continue;
                if (matched[row][col]) continue;

                // Check if it forms a + shape (center + up + down + left + right)
                if (candyTypes[row - 1][col] == centerType && powerUpTypes[row - 1][col] == POWERUP_NONE &&
                        candyTypes[row + 1][col] == centerType && powerUpTypes[row + 1][col] == POWERUP_NONE &&
                        candyTypes[row][col - 1] == centerType && powerUpTypes[row][col - 1] == POWERUP_NONE &&
                        candyTypes[row][col + 1] == centerType && powerUpTypes[row][col + 1] == POWERUP_NONE &&
                        !matched[row - 1][col] && !matched[row + 1][col] &&
                        !matched[row][col - 1] && !matched[row][col + 1]) {

                    hasMatch = true;

                    // Mark all 5 cells as matched
                    matched[row][col] = true;
                    matched[row - 1][col] = true;
                    matched[row + 1][col] = true;
                    matched[row][col - 1] = true;
                    matched[row][col + 1] = true;

                    // Create a special power-up (TNT for +-shape)
                    powerUpTypes[row][col] = POWERUP_TNT;
                    candies[row][col].setImageResource(getPowerUpDrawable(POWERUP_TNT));
                    animatePowerUpCreation(candies[row][col]);

                    // Mark other cells for removal
                    candyTypes[row - 1][col] = -1;
                    candyTypes[row + 1][col] = -1;
                    candyTypes[row][col - 1] = -1;
                    candyTypes[row][col + 1] = -1;
                }
            }
        }

        // THEE TNT Check for LARGE T-shapes (7 candies total) - Creates TNT!
        // Format: 3 horizontal + 4 vertical = 7 total
        for (int row = 1; row < GRID_ROWS - 1; row++) {
            for (int col = 1; col < GRID_COLS - 1; col++) {
                int centerType = candyTypes[row][col];
                if (centerType == -1 || powerUpTypes[row][col] != POWERUP_NONE) continue;
                if (matched[row][col]) continue;

                // Large T-shape pointing DOWN: ⊥ (3 horizontal at top, 4 vertical down)
                //    Y Y Y
                //      Y
                //      Y
                //      Y
                if (row < GRID_ROWS - 3 &&
                        candyTypes[row][col - 1] == centerType && powerUpTypes[row][col - 1] == POWERUP_NONE &&
                        candyTypes[row][col + 1] == centerType && powerUpTypes[row][col + 1] == POWERUP_NONE &&
                        candyTypes[row + 1][col] == centerType && powerUpTypes[row + 1][col] == POWERUP_NONE &&
                        candyTypes[row + 2][col] == centerType && powerUpTypes[row + 2][col] == POWERUP_NONE &&
                        candyTypes[row + 3][col] == centerType && powerUpTypes[row + 3][col] == POWERUP_NONE &&
                        !matched[row][col - 1] && !matched[row][col + 1] &&
                        !matched[row + 1][col] && !matched[row + 2][col] && !matched[row + 3][col]) {

                    hasMatch = true;
                    matched[row][col] = true;
                    matched[row][col - 1] = true;
                    matched[row][col + 1] = true;
                    matched[row + 1][col] = true;
                    matched[row + 2][col] = true;
                    matched[row + 3][col] = true;

                    powerUpTypes[row][col] = POWERUP_TNT;
                    candies[row][col].setImageResource(getPowerUpDrawable(POWERUP_TNT));
                    animatePowerUpCreation(candies[row][col]);
                    tutorialPowerUpCreated = true;

                    candyTypes[row][col - 1] = -1;
                    candyTypes[row][col + 1] = -1;
                    candyTypes[row + 1][col] = -1;
                    candyTypes[row + 2][col] = -1;
                    candyTypes[row + 3][col] = -1;
                }
                // Large T-shape pointing UP: T (3 horizontal at bottom, 4 vertical up)
                else if (row >= 3 &&
                        candyTypes[row][col - 1] == centerType && powerUpTypes[row][col - 1] == POWERUP_NONE &&
                        candyTypes[row][col + 1] == centerType && powerUpTypes[row][col + 1] == POWERUP_NONE &&
                        candyTypes[row - 1][col] == centerType && powerUpTypes[row - 1][col] == POWERUP_NONE &&
                        candyTypes[row - 2][col] == centerType && powerUpTypes[row - 2][col] == POWERUP_NONE &&
                        candyTypes[row - 3][col] == centerType && powerUpTypes[row - 3][col] == POWERUP_NONE &&
                        !matched[row][col - 1] && !matched[row][col + 1] &&
                        !matched[row - 1][col] && !matched[row - 2][col] && !matched[row - 3][col]) {

                    hasMatch = true;
                    matched[row][col] = true;
                    matched[row][col - 1] = true;
                    matched[row][col + 1] = true;
                    matched[row - 1][col] = true;
                    matched[row - 2][col] = true;
                    matched[row - 3][col] = true;

                    powerUpTypes[row][col] = POWERUP_TNT;
                    candies[row][col].setImageResource(getPowerUpDrawable(POWERUP_TNT));
                    animatePowerUpCreation(candies[row][col]);
                    tutorialPowerUpCreated = true;

                    candyTypes[row][col - 1] = -1;
                    candyTypes[row][col + 1] = -1;
                    candyTypes[row - 1][col] = -1;
                    candyTypes[row - 2][col] = -1;
                    candyTypes[row - 3][col] = -1;
                }
                // Large T-shape pointing RIGHT: ⊢ (3 vertical on left, 4 horizontal right)
                else if (col < GRID_COLS - 3 &&
                        candyTypes[row - 1][col] == centerType && powerUpTypes[row - 1][col] == POWERUP_NONE &&
                        candyTypes[row + 1][col] == centerType && powerUpTypes[row + 1][col] == POWERUP_NONE &&
                        candyTypes[row][col + 1] == centerType && powerUpTypes[row][col + 1] == POWERUP_NONE &&
                        candyTypes[row][col + 2] == centerType && powerUpTypes[row][col + 2] == POWERUP_NONE &&
                        candyTypes[row][col + 3] == centerType && powerUpTypes[row][col + 3] == POWERUP_NONE &&
                        !matched[row - 1][col] && !matched[row + 1][col] &&
                        !matched[row][col + 1] && !matched[row][col + 2] && !matched[row][col + 3]) {

                    hasMatch = true;
                    matched[row][col] = true;
                    matched[row - 1][col] = true;
                    matched[row + 1][col] = true;
                    matched[row][col + 1] = true;
                    matched[row][col + 2] = true;
                    matched[row][col + 3] = true;

                    powerUpTypes[row][col] = POWERUP_TNT;
                    candies[row][col].setImageResource(getPowerUpDrawable(POWERUP_TNT));
                    animatePowerUpCreation(candies[row][col]);
                    tutorialPowerUpCreated = true;

                    candyTypes[row - 1][col] = -1;
                    candyTypes[row + 1][col] = -1;
                    candyTypes[row][col + 1] = -1;
                    candyTypes[row][col + 2] = -1;
                    candyTypes[row][col + 3] = -1;
                }
                // Large T-shape pointing LEFT: ⊣ (3 vertical on right, 4 horizontal left)
                else if (col >= 3 &&
                        candyTypes[row - 1][col] == centerType && powerUpTypes[row - 1][col] == POWERUP_NONE &&
                        candyTypes[row + 1][col] == centerType && powerUpTypes[row + 1][col] == POWERUP_NONE &&
                        candyTypes[row][col - 1] == centerType && powerUpTypes[row][col - 1] == POWERUP_NONE &&
                        candyTypes[row][col - 2] == centerType && powerUpTypes[row][col - 2] == POWERUP_NONE &&
                        candyTypes[row][col - 3] == centerType && powerUpTypes[row][col - 3] == POWERUP_NONE &&
                        !matched[row - 1][col] && !matched[row + 1][col] &&
                        !matched[row][col - 1] && !matched[row][col - 2] && !matched[row][col - 3]) {

                    hasMatch = true;
                    matched[row][col] = true;
                    matched[row - 1][col] = true;
                    matched[row + 1][col] = true;
                    matched[row][col - 1] = true;
                    matched[row][col - 2] = true;
                    matched[row][col - 3] = true;

                    powerUpTypes[row][col] = POWERUP_TNT;
                    candies[row][col].setImageResource(getPowerUpDrawable(POWERUP_TNT));
                    animatePowerUpCreation(candies[row][col]);
                    tutorialPowerUpCreated = true;

                    candyTypes[row - 1][col] = -1;
                    candyTypes[row + 1][col] = -1;
                    candyTypes[row][col - 1] = -1;
                    candyTypes[row][col - 2] = -1;
                    candyTypes[row][col - 3] = -1;
                }
                // Check for T-shapes (4 variations: T, ⊥, ⊢, ⊣)

                // Large variation 2: T-shape pointing DOWN: ⊥ (5 horizontal at bottom, 3 vertical down)
                // If row is negative = go up, positive = go down
                // If colum is negative = left, positive =right

                //        C
                //    A B c D E
                //        F
                //        G

                if (row + 2 < GRID_ROWS &&
                        col >= 2 && col < GRID_COLS - 2 &&
                        candyTypes[row][col - 1] == centerType && powerUpTypes[row][col - 1] == POWERUP_NONE &&
                        candyTypes[row][col + 1] == centerType && powerUpTypes[row][col + 1] == POWERUP_NONE &&
                        candyTypes[row][col - 2] == centerType && powerUpTypes[row][col - 2] == POWERUP_NONE &&
                        candyTypes[row][col + 2] == centerType && powerUpTypes[row][col + 2] == POWERUP_NONE &&
                        candyTypes[row + 1][col] == centerType && powerUpTypes[row + 1][col] == POWERUP_NONE &&
                        candyTypes[row + 2][col] == centerType && powerUpTypes[row + 2][col] == POWERUP_NONE &&

                        !matched[row][col - 1] &&
                        !matched[row][col + 1] &&
                        !matched[row][col - 2] &&
                        !matched[row][col + 2] &&
                        !matched[row + 1][col] &&
                        !matched[row + 2][col] ){

                    hasMatch = true;
                    matched[row][col] = true;
                    matched[row][col - 1] = true;
                    matched[row][col + 1] = true;
                    matched[row][col - 2] = true;
                    matched[row][col + 2] = true;
                    matched[row + 1][col] = true;
                    matched[row + 2][col] = true;

                    powerUpTypes[row][col] = POWERUP_TNT;
                    candies[row][col].setImageResource(getPowerUpDrawable(POWERUP_TNT));
                    animatePowerUpCreation(candies[row][col]);
                    tutorialPowerUpCreated = true;

                    candyTypes[row][col - 1] = -1;
                    candyTypes[row][col + 1] = -1;
                    candyTypes[row][col - 2] = -1;
                    candyTypes[row][col + 2] = -1;
                    candyTypes[row + 1][col] = -1;
                    candyTypes[row + 2][col] = -1;
                }

                //        G
                //        F
                //    A B c D E
                //        C

                if (row >= 2 && row < GRID_ROWS - 1 &&
                        col >= 2 && col < GRID_COLS - 2 &&
                        candyTypes[row][col - 2] == centerType && powerUpTypes[row][col - 2] == POWERUP_NONE &&
                        candyTypes[row][col - 1] == centerType && powerUpTypes[row][col - 1] == POWERUP_NONE &&
                        candyTypes[row][col + 1] == centerType && powerUpTypes[row][col + 1] == POWERUP_NONE &&
                        candyTypes[row][col + 2] == centerType && powerUpTypes[row][col + 2] == POWERUP_NONE &&
                        candyTypes[row - 1][col] == centerType && powerUpTypes[row - 1][col] == POWERUP_NONE &&
                        candyTypes[row - 2][col] == centerType && powerUpTypes[row - 2][col] == POWERUP_NONE &&

                        // Check not already matched
                        !matched[row][col - 2] &&
                        !matched[row][col - 1] &&
                        !matched[row][col + 1] &&
                        !matched[row][col + 2] &&
                        !matched[row - 1][col] &&
                        !matched[row - 2][col] ){

                    hasMatch = true;
                    matched[row][col] = true;
                    matched[row][col - 2] = true;
                    matched[row][col - 1] = true;
                    matched[row][col + 1] = true;
                    matched[row][col + 2] = true;
                    matched[row - 1][col] = true;
                    matched[row - 2][col] = true;

                    powerUpTypes[row][col] = POWERUP_TNT;
                    candies[row][col].setImageResource(getPowerUpDrawable(POWERUP_TNT));
                    animatePowerUpCreation(candies[row][col]);
                    tutorialPowerUpCreated = true;

                    candyTypes[row][col - 2] = -1;
                    candyTypes[row][col - 1] = -1;
                    candyTypes[row][col + 1] = -1;
                    candyTypes[row][col + 2] = -1;
                    candyTypes[row - 1][col] = -1;
                    candyTypes[row - 2][col] = -1;
                }

                //        A
                //        B
                //      C c F G
                //        D
                //        E

                else if (row >= 2 && row < GRID_ROWS - 2 && col >= 2 && col < GRID_COLS - 2 &&
                        candyTypes[row - 2][col] == centerType && powerUpTypes[row - 2][col] == POWERUP_NONE && // A
                        candyTypes[row - 1][col] == centerType && powerUpTypes[row - 1][col] == POWERUP_NONE && // B
                        candyTypes[row + 1][col] == centerType && powerUpTypes[row + 1][col] == POWERUP_NONE && // A
                        candyTypes[row + 2][col] == centerType && powerUpTypes[row + 2][col] == POWERUP_NONE && // E
                        candyTypes[row][col + 1] == centerType && powerUpTypes[row][col + 1] == POWERUP_NONE && // F
                        candyTypes[row][col + 2] == centerType && powerUpTypes[row][col + 2] == POWERUP_NONE && // G

                        !matched[row - 2][col] &&
                        !matched[row - 1][col] &&
                        !matched[row + 1][col] &&
                        !matched[row + 2][col] &&
                        !matched[row][col + 1] &&
                        !matched[row][col + 2] ){

                    hasMatch = true;
                    matched[row - 2][col] = true;
                    matched[row - 1][col] = true;
                    matched[row][col] = true;
                    matched[row + 1][col] = true;
                    matched[row + 2][col] = true;
                    matched[row][col + 1] = true;
                    matched[row][col + 2] = true;

                    powerUpTypes[row][col] = POWERUP_TNT;
                    candies[row][col].setImageResource(getPowerUpDrawable(POWERUP_TNT));
                    animatePowerUpCreation(candies[row][col]);
                    tutorialPowerUpCreated = true;

                    candyTypes[row - 2][col] = -1;
                    candyTypes[row - 1][col] = -1;
                    candyTypes[row + 1][col] = -1;
                    candyTypes[row + 2][col] = -1;
                    candyTypes[row][col + 1] = -1;
                    candyTypes[row][col + 2] = -1;
                }

                //        A
                //        B
                //    G F c C
                //        D
                //        E

                else if (row >= 2 && row < GRID_ROWS - 2 && col >= 2 && col < GRID_COLS - 2 &&
                        candyTypes[row - 2][col] == centerType && powerUpTypes[row - 2][col] == POWERUP_NONE && // A
                        candyTypes[row - 1][col] == centerType && powerUpTypes[row - 1][col] == POWERUP_NONE && // B
                        candyTypes[row + 1][col] == centerType && powerUpTypes[row + 1][col] == POWERUP_NONE && // A
                        candyTypes[row + 2][col] == centerType && powerUpTypes[row + 2][col] == POWERUP_NONE && // E
                        candyTypes[row][col - 1] == centerType && powerUpTypes[row][col - 1] == POWERUP_NONE && // F
                        candyTypes[row][col - 2] == centerType && powerUpTypes[row][col - 2] == POWERUP_NONE && // G

                        !matched[row - 2][col] &&
                        !matched[row - 1][col] &&
                        !matched[row + 1][col] &&
                        !matched[row + 2][col] &&
                        !matched[row][col - 1] &&
                        !matched[row][col - 2] ){

                    hasMatch = true;
                    matched[row - 2][col] = true;
                    matched[row - 1][col] = true;
                    matched[row][col] = true;
                    matched[row + 1][col] = true;
                    matched[row + 2][col] = true;
                    matched[row][col - 1] = true;
                    matched[row][col - 2] = true;

                    powerUpTypes[row][col] = POWERUP_TNT;
                    candies[row][col].setImageResource(getPowerUpDrawable(POWERUP_TNT));
                    animatePowerUpCreation(candies[row][col]);
                    tutorialPowerUpCreated = true;

                    candyTypes[row - 2][col] = -1;
                    candyTypes[row - 1][col] = -1;
                    candyTypes[row + 1][col] = -1;
                    candyTypes[row + 2][col] = -1;
                    candyTypes[row][col - 1] = -1;
                    candyTypes[row][col - 2] = -1;
                }

                //THEE BOMB : t-shapes
                //        C
                //      B c D
                //        F
                //        G

                if (row + 2 < GRID_ROWS &&
                        col >= 2 && col < GRID_COLS - 2 &&
//                        candyTypes[row][col + 2] == centerType && powerUpTypes[row][col + 2] == POWERUP_NONE &&
//                        candyTypes[row][col - 2] == centerType && powerUpTypes[row][col - 2] == POWERUP_NONE &&
                        candyTypes[row][col - 1] == centerType && powerUpTypes[row][col - 1] == POWERUP_NONE &&
                        candyTypes[row][col + 1] == centerType && powerUpTypes[row][col + 1] == POWERUP_NONE &&
                        candyTypes[row + 1][col] == centerType && powerUpTypes[row + 1][col] == POWERUP_NONE &&
                        candyTypes[row + 2][col] == centerType && powerUpTypes[row + 2][col] == POWERUP_NONE &&

                        !matched[row][col - 1] &&
                        !matched[row][col + 1] &&
//                        !matched[row][col - 2] &&
//                        !matched[row][col + 2] &&
                        !matched[row + 1][col] &&
                        !matched[row + 2][col] ){

                    hasMatch = true;
                    matched[row][col] = true;
                    matched[row][col - 1] = true;
                    matched[row][col + 1] = true;
//                    matched[row][col - 2] = true;
//                    matched[row][col + 2] = true;
                    matched[row + 1][col] = true;
                    matched[row + 2][col] = true;

                    powerUpTypes[row][col] = POWERUP_BOMB;
                    candies[row][col].setImageResource(getPowerUpDrawable(POWERUP_BOMB));
                    animatePowerUpCreation(candies[row][col]);
                    tutorialPowerUpCreated = true;

                    candyTypes[row][col - 1] = -1;
                    candyTypes[row][col + 1] = -1;
//                    candyTypes[row][col - 2] = -1;
//                    candyTypes[row][col + 2] = -1;
                    candyTypes[row + 1][col] = -1;
                    candyTypes[row + 2][col] = -1;
                }

                //        G
                //        F
                //      B c D
                //        C

                if (row >= 2 && row < GRID_ROWS - 1 &&
                        col >= 2 && col < GRID_COLS - 2 &&
//                        candyTypes[row][col - 2] == centerType && powerUpTypes[row][col - 2] == POWERUP_NONE &&
//                        candyTypes[row][col + 2] == centerType && powerUpTypes[row][col + 2] == POWERUP_NONE &&
                        candyTypes[row][col - 1] == centerType && powerUpTypes[row][col - 1] == POWERUP_NONE &&
                        candyTypes[row][col + 1] == centerType && powerUpTypes[row][col + 1] == POWERUP_NONE &&
                        candyTypes[row - 1][col] == centerType && powerUpTypes[row - 1][col] == POWERUP_NONE &&
                        candyTypes[row - 2][col] == centerType && powerUpTypes[row - 2][col] == POWERUP_NONE &&

                        // Check not already matched
//                        !matched[row][col - 2] &&
//                        !matched[row][col + 2] &&
                        !matched[row][col - 1] &&
                        !matched[row][col + 1] &&
                        !matched[row - 1][col] &&
                        !matched[row - 2][col] ){

                    hasMatch = true;
                    matched[row][col] = true;
//                    matched[row][col + 2] = true;
//                    matched[row][col - 2] = true;
                    matched[row][col - 1] = true;
                    matched[row][col + 1] = true;
                    matched[row - 1][col] = true;
                    matched[row - 2][col] = true;

                    powerUpTypes[row][col] = POWERUP_BOMB;
                    candies[row][col].setImageResource(getPowerUpDrawable(POWERUP_BOMB));
                    animatePowerUpCreation(candies[row][col]);
                    tutorialPowerUpCreated = true;

//                    candyTypes[row][col - 2] = -1;
//                    candyTypes[row][col + 2] = -1;
                    candyTypes[row][col - 1] = -1;
                    candyTypes[row][col + 1] = -1;
                    candyTypes[row - 1][col] = -1;
                    candyTypes[row - 2][col] = -1;
                }

                //
                //        B
                //      C c F G
                //        D
                //

                else if (row >= 2 && row < GRID_ROWS - 2 && col >= 2 && col < GRID_COLS - 2 &&
//                        candyTypes[row - 2][col] == centerType && powerUpTypes[row - 2][col] == POWERUP_NONE && // A
//                        candyTypes[row + 2][col] == centerType && powerUpTypes[row + 2][col] == POWERUP_NONE && // E
                        candyTypes[row - 1][col] == centerType && powerUpTypes[row - 1][col] == POWERUP_NONE && // B
                        candyTypes[row + 1][col] == centerType && powerUpTypes[row + 1][col] == POWERUP_NONE && // D
                        candyTypes[row][col + 1] == centerType && powerUpTypes[row][col + 1] == POWERUP_NONE && // F
                        candyTypes[row][col + 2] == centerType && powerUpTypes[row][col + 2] == POWERUP_NONE && // G

//                        !matched[row - 2][col] &&
//                        !matched[row + 2][col] &&
                        !matched[row - 1][col] &&
                        !matched[row + 1][col] &&
                        !matched[row][col + 1] &&
                        !matched[row][col + 2] ){

                    hasMatch = true;
//                    matched[row - 2][col] = true;
//                    matched[row + 2][col] = true;
                    matched[row - 1][col] = true;
                    matched[row][col] = true;
                    matched[row + 1][col] = true;
                    matched[row][col + 1] = true;
                    matched[row][col + 2] = true;

                    powerUpTypes[row][col] = POWERUP_BOMB;
                    candies[row][col].setImageResource(getPowerUpDrawable(POWERUP_BOMB));
                    animatePowerUpCreation(candies[row][col]);
                    tutorialPowerUpCreated = true;

//                    candyTypes[row - 2][col] = -1;
//                    candyTypes[row + 2][col] = -1;
                    candyTypes[row - 1][col] = -1;
                    candyTypes[row + 1][col] = -1;
                    candyTypes[row][col + 1] = -1;
                    candyTypes[row][col + 2] = -1;
                }

                //
                //        B
                //    G F c C
                //        D
                //

                else if (row >= 2 && row < GRID_ROWS - 2 && col >= 2 && col < GRID_COLS - 2 &&
//                        candyTypes[row + 2][col] == centerType && powerUpTypes[row + 2][col] == POWERUP_NONE && // E
//                        candyTypes[row - 2][col] == centerType && powerUpTypes[row - 2][col] == POWERUP_NONE && // A
                        candyTypes[row - 1][col] == centerType && powerUpTypes[row - 1][col] == POWERUP_NONE && // B
                        candyTypes[row + 1][col] == centerType && powerUpTypes[row + 1][col] == POWERUP_NONE && // D
                        candyTypes[row][col - 1] == centerType && powerUpTypes[row][col - 1] == POWERUP_NONE && // F
                        candyTypes[row][col - 2] == centerType && powerUpTypes[row][col - 2] == POWERUP_NONE && // G

//                        !matched[row - 2][col] &&
//                        !matched[row + 2][col] &&
                        !matched[row - 1][col] &&
                        !matched[row + 1][col] &&
                        !matched[row][col - 1] &&
                        !matched[row][col - 2] ){

                    hasMatch = true;
//                    matched[row - 2][col] = true;
//                    matched[row + 2][col] = true;
                    matched[row - 1][col] = true;
                    matched[row][col] = true;
                    matched[row + 1][col] = true;
                    matched[row][col - 1] = true;
                    matched[row][col - 2] = true;

                    powerUpTypes[row][col] = POWERUP_BOMB;
                    candies[row][col].setImageResource(getPowerUpDrawable(POWERUP_BOMB));
                    animatePowerUpCreation(candies[row][col]);
                    tutorialPowerUpCreated = true;

//                    candyTypes[row - 2][col] = -1;
//                    candyTypes[row + 2][col] = -1;
                    candyTypes[row - 1][col] = -1;
                    candyTypes[row + 1][col] = -1;
                    candyTypes[row][col - 1] = -1;
                    candyTypes[row][col - 2] = -1;
                }
            }
        }

        // THEE DYNAMITE Check for T-shapes (4 variations: T, ⊥, ⊢, ⊣)
        for (int row = 1; row < GRID_ROWS - 1; row++) {
            for (int col = 1; col < GRID_COLS - 1; col++) {
                int centerType = candyTypes[row][col];
                if (centerType == -1 || powerUpTypes[row][col] != POWERUP_NONE) continue;
                if (matched[row][col]) continue;

                //        D
                //        E
                //    A B c C
                //        F

                // T-shape pointing up: ⊥ (horizontal line at top, vertical line down)
                if (row >= 2 && row < GRID_ROWS - 2 && col >= 2 && col < GRID_COLS - 2 &&

                        candyTypes[row][col - 2] == centerType && powerUpTypes[row][col - 2] == POWERUP_NONE &&
                        candyTypes[row][col - 1] == centerType && powerUpTypes[row][col - 1] == POWERUP_NONE &&
                        candyTypes[row - 2][col] == centerType && powerUpTypes[row - 1][col] == POWERUP_NONE &&
                        candyTypes[row - 1][col] == centerType && powerUpTypes[row - 2][col] == POWERUP_NONE &&
                        candyTypes[row + 1][col] == centerType && powerUpTypes[row + 2][col] == POWERUP_NONE &&

                        !matched[row][col - 2] &&
                        !matched[row][col - 1] &&
                        !matched[row - 2][col] &&
                        !matched[row - 1][col] &&
                        !matched[row + 1][col] ){

                    hasMatch = true;
                    matched[row][col - 2] = true;
                    matched[row][col - 1] = true;
                    matched[row - 2][col] = true;
                    matched[row - 1][col] = true;
                    matched[row + 1][col] = true;

                    powerUpTypes[row][col] = POWERUP_DYNAMITE;
                    candies[row][col].setImageResource(getPowerUpDrawable(POWERUP_DYNAMITE));
                    animatePowerUpCreation(candies[row][col]);

                    candyTypes[row][col - 2] = -1;
                    candyTypes[row][col - 1] = -1;
                    candyTypes[row - 2][col] = -1;
                    candyTypes[row - 1][col] = -1;
                    candyTypes[row + 1][col] = -1;

                }

                //        D
                //        E
                //    A B c F
                //        C

                // T-shape pointing up: ⊥ (horizontal line at top, vertical line down)
                if (row >= 2 && row < GRID_ROWS - 2 && col >= 2 && col < GRID_COLS - 2 &&

                        candyTypes[row][col - 2] == centerType && powerUpTypes[row][col - 2] == POWERUP_NONE &&
                        candyTypes[row][col - 1] == centerType && powerUpTypes[row][col - 1] == POWERUP_NONE &&
                        candyTypes[row][col + 1] == centerType && powerUpTypes[row][col + 1] == POWERUP_NONE &&
                        candyTypes[row - 2][col] == centerType && powerUpTypes[row - 1][col] == POWERUP_NONE &&
                        candyTypes[row - 1][col] == centerType && powerUpTypes[row - 2][col] == POWERUP_NONE &&

                        !matched[row][col - 2] &&
                        !matched[row][col - 1] &&
                        !matched[row][col + 1] &&
                        !matched[row - 2][col] &&
                        !matched[row - 1][col] ){

                    hasMatch = true;
                    matched[row][col - 2] = true;
                    matched[row][col - 1] = true;
                    matched[row][col + 1] = true;
                    matched[row - 2][col] = true;
                    matched[row - 1][col] = true;

                    powerUpTypes[row][col] = POWERUP_DYNAMITE;
                    candies[row][col].setImageResource(getPowerUpDrawable(POWERUP_DYNAMITE));
                    animatePowerUpCreation(candies[row][col]);

                    candyTypes[row][col - 2] = -1;
                    candyTypes[row][col - 1] = -1;
                    candyTypes[row][col + 1] = -1;
                    candyTypes[row - 2][col] = -1;
                    candyTypes[row - 1][col] = -1;

                }

                //        D
                //        E
                //      C c B A
                //        F

                // T-shape pointing left: ⊢ (vertical line on left, horizontal line right)
                else if (row >= 2 && row < GRID_ROWS - 2 && col >= 2 && col < GRID_COLS - 2 &&

                        candyTypes[row][col + 2] == centerType && powerUpTypes[row][col + 2] == POWERUP_NONE &&
                        candyTypes[row][col + 1] == centerType && powerUpTypes[row][col + 1] == POWERUP_NONE &&
                        candyTypes[row - 2][col] == centerType && powerUpTypes[row - 2][col] == POWERUP_NONE &&
                        candyTypes[row - 1][col] == centerType && powerUpTypes[row - 1][col] == POWERUP_NONE &&
                        candyTypes[row + 1][col] == centerType && powerUpTypes[row - 1][col] == POWERUP_NONE &&
                        !matched[row][col + 2] &&
                        !matched[row][col + 1] &&
                        !matched[row - 2][col] &&
                        !matched[row - 1][col] &&
                        !matched[row + 1][col] ){

                    hasMatch = true;
                    matched[row][col + 2] = true;
                    matched[row][col + 1] = true;
                    matched[row - 2][col] = true;
                    matched[row - 1][col] = true;
                    matched[row + 1][col] = true;

                    powerUpTypes[row][col] = POWERUP_DYNAMITE;
                    candies[row][col].setImageResource(getPowerUpDrawable(POWERUP_DYNAMITE));
                    animatePowerUpCreation(candies[row][col]);

                    candyTypes[row][col + 2] = -1;
                    candyTypes[row][col + 1] = -1;
                    candyTypes[row - 2][col] = -1;
                    candyTypes[row - 1][col] = -1;
                    candyTypes[row + 1][col] = -1;
                }
                //        D
                //        E
                //      F c B A
                //        C

                // T-shape pointing left: ⊢ (vertical line on left, horizontal line right)
                else if (row >= 2 && row < GRID_ROWS - 2 && col >= 2 && col < GRID_COLS - 2 &&

                        candyTypes[row][col + 2] == centerType && powerUpTypes[row][col + 2] == POWERUP_NONE &&
                        candyTypes[row][col + 1] == centerType && powerUpTypes[row][col + 1] == POWERUP_NONE &&
                        candyTypes[row][col - 1] == centerType && powerUpTypes[row][col - 1] == POWERUP_NONE &&
                        candyTypes[row - 2][col] == centerType && powerUpTypes[row - 2][col] == POWERUP_NONE &&
                        candyTypes[row - 1][col] == centerType && powerUpTypes[row - 1][col] == POWERUP_NONE &&
                        !matched[row][col + 2] &&
                        !matched[row][col + 1] &&
                        !matched[row][col - 1] &&
                        !matched[row - 2][col] &&
                        !matched[row - 1][col] ){

                    hasMatch = true;
                    matched[row][col + 2] = true;
                    matched[row][col + 1] = true;
                    matched[row][col - 1] = true;
                    matched[row - 2][col] = true;
                    matched[row - 1][col] = true;

                    powerUpTypes[row][col] = POWERUP_DYNAMITE;
                    candies[row][col].setImageResource(getPowerUpDrawable(POWERUP_DYNAMITE));
                    animatePowerUpCreation(candies[row][col]);

                    candyTypes[row][col + 2] = -1;
                    candyTypes[row][col + 1] = -1;
                    candyTypes[row][col - 1] = -1;
                    candyTypes[row - 2][col] = -1;
                    candyTypes[row - 1][col] = -1;
                }

                //        D
                //      C c B A
                //        E
                //        F

                // T-shape pointing down: T (horizontal line at bottom, vertical line up)
                else if (row >= 2 && row < GRID_ROWS - 2 && col >= 2 && col < GRID_COLS - 2 &&

                        candyTypes[row][col + 2] == centerType && powerUpTypes[row][col + 2] == POWERUP_NONE &&
                        candyTypes[row][col + 1] == centerType && powerUpTypes[row][col + 1] == POWERUP_NONE &&
                        candyTypes[row - 1][col] == centerType && powerUpTypes[row - 1][col] == POWERUP_NONE &&
                        candyTypes[row + 1][col] == centerType && powerUpTypes[row + 1][col] == POWERUP_NONE &&
                        candyTypes[row + 2][col] == centerType && powerUpTypes[row + 2][col] == POWERUP_NONE &&
                        !matched[row][col + 2] &&
                        !matched[row][col + 1] &&
                        !matched[row - 1][col] &&
                        !matched[row + 1][col] &&
                        !matched[row + 2][col] ){

                    hasMatch = true;
                    matched[row][col] = true;
                    matched[row][col + 2] = true;
                    matched[row][col + 1] = true;
                    matched[row - 1][col] = true;
                    matched[row + 1][col] = true;
                    matched[row + 2][col] = true;

                    powerUpTypes[row][col] = POWERUP_DYNAMITE;
                    candies[row][col].setImageResource(getPowerUpDrawable(POWERUP_DYNAMITE));
                    animatePowerUpCreation(candies[row][col]);

                    candyTypes[row][col + 2] = -1;
                    candyTypes[row][col + 1] = -1;
                    candyTypes[row - 1][col] = -1;
                    candyTypes[row + 1][col] = -1;
                    candyTypes[row + 2][col] = -1;
                }

                //        C
                //      D c B A
                //        E
                //        F

                // T-shape pointing down: T (horizontal line at bottom, vertical line up)
                else if (row >= 2 && row < GRID_ROWS - 2 && col >= 2 && col < GRID_COLS - 2 &&

                        candyTypes[row][col + 2] == centerType && powerUpTypes[row][col + 2] == POWERUP_NONE &&
                        candyTypes[row][col + 1] == centerType && powerUpTypes[row][col + 1] == POWERUP_NONE &&
                        candyTypes[row][col - 1] == centerType && powerUpTypes[row][col - 1] == POWERUP_NONE &&
                        candyTypes[row + 1][col] == centerType && powerUpTypes[row + 1][col] == POWERUP_NONE &&
                        candyTypes[row + 2][col] == centerType && powerUpTypes[row + 2][col] == POWERUP_NONE &&
                        !matched[row][col + 2] &&
                        !matched[row][col + 1] &&
                        !matched[row][col - 1] &&
                        !matched[row + 1][col] &&
                        !matched[row + 2][col] ){

                    hasMatch = true;
                    matched[row][col] = true;
                    matched[row][col + 2] = true;
                    matched[row][col + 1] = true;
                    matched[row][col - 1] = true;
                    matched[row + 1][col] = true;
                    matched[row + 2][col] = true;

                    powerUpTypes[row][col] = POWERUP_DYNAMITE;
                    candies[row][col].setImageResource(getPowerUpDrawable(POWERUP_DYNAMITE));
                    animatePowerUpCreation(candies[row][col]);

                    candyTypes[row][col + 2] = -1;
                    candyTypes[row][col + 1] = -1;
                    candyTypes[row][col - 1] = -1;
                    candyTypes[row + 1][col] = -1;
                    candyTypes[row + 2][col] = -1;
                }

                //          D
                //      A B c C
                //          E
                //          F

                // T-shape pointing right: ⊣ (vertical line on right, horizontal line left)
                else if (row >= 2 && row < GRID_ROWS - 2 && col >= 2 && col < GRID_COLS - 2 &&

                        candyTypes[row][col - 2] == centerType && powerUpTypes[row][col - 2] == POWERUP_NONE &&
                        candyTypes[row][col - 1] == centerType && powerUpTypes[row][col - 1] == POWERUP_NONE &&
                        candyTypes[row - 1][col] == centerType && powerUpTypes[row - 1][col] == POWERUP_NONE &&
                        candyTypes[row + 1][col] == centerType && powerUpTypes[row + 1][col] == POWERUP_NONE &&
                        candyTypes[row + 2][col] == centerType && powerUpTypes[row + 2][col] == POWERUP_NONE &&
                        !matched[row][col - 2] &&
                        !matched[row][col - 1] &&
                        !matched[row - 1][col] &&
                        !matched[row + 1][col] &&
                        !matched[row + 2][col] ){

                    hasMatch = true;
                    matched[row][col] = true;
                    matched[row][col - 2] = true;
                    matched[row][col - 1] = true;
                    matched[row - 1][col] = true;
                    matched[row + 1][col] = true;
                    matched[row + 2][col] = true;

                    powerUpTypes[row][col] = POWERUP_DYNAMITE;
                    candies[row][col].setImageResource(getPowerUpDrawable(POWERUP_DYNAMITE));
                    animatePowerUpCreation(candies[row][col]);

                    candyTypes[row][col - 2] = -1;
                    candyTypes[row][col - 1] = -1;
                    candyTypes[row - 1][col] = -1;
                    candyTypes[row + 1][col] = -1;
                    candyTypes[row + 2][col] = -1;
                }

                //          C
                //      A B c D
                //          E
                //          F

                // T-shape pointing right: ⊣ (vertical line on right, horizontal line left)
                else if (row >= 2 && row < GRID_ROWS - 2 && col >= 2 && col < GRID_COLS - 2 &&

                        candyTypes[row][col - 2] == centerType && powerUpTypes[row][col - 2] == POWERUP_NONE &&
                        candyTypes[row][col - 1] == centerType && powerUpTypes[row][col - 1] == POWERUP_NONE &&
                        candyTypes[row][col + 1] == centerType && powerUpTypes[row][col + 1] == POWERUP_NONE &&
                        candyTypes[row + 1][col] == centerType && powerUpTypes[row + 1][col] == POWERUP_NONE &&
                        candyTypes[row + 2][col] == centerType && powerUpTypes[row + 2][col] == POWERUP_NONE &&
                        !matched[row][col - 2] &&
                        !matched[row][col - 1] &&
                        !matched[row][col + 1] &&
                        !matched[row + 1][col] &&
                        !matched[row + 2][col] ){

                    hasMatch = true;
                    matched[row][col] = true;
                    matched[row][col - 2] = true;
                    matched[row][col - 1] = true;
                    matched[row][col + 1] = true;
                    matched[row + 1][col] = true;
                    matched[row + 2][col] = true;

                    powerUpTypes[row][col] = POWERUP_DYNAMITE;
                    candies[row][col].setImageResource(getPowerUpDrawable(POWERUP_DYNAMITE));
                    animatePowerUpCreation(candies[row][col]);

                    candyTypes[row][col - 2] = -1;
                    candyTypes[row][col - 1] = -1;
                    candyTypes[row][col + 1] = -1;
                    candyTypes[row + 1][col] = -1;
                    candyTypes[row + 1][col] = -1;
                }
            }
        }

        // THEE BOMB Check for L-shapes (4 orientations) - 5 candies forming an L
        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                int cornerType = candyTypes[row][col];
                if (cornerType == -1 || powerUpTypes[row][col] != POWERUP_NONE) continue;
                if (matched[row][col]) continue;

                // L-shape: └ (corner bottom-left, extends right and up)
                if (row >= 2 && col < GRID_COLS - 2 &&
                        candyTypes[row - 1][col] == cornerType && powerUpTypes[row - 1][col] == POWERUP_NONE &&
                        candyTypes[row - 2][col] == cornerType && powerUpTypes[row - 2][col] == POWERUP_NONE &&
                        candyTypes[row][col + 1] == cornerType && powerUpTypes[row][col + 1] == POWERUP_NONE &&
                        candyTypes[row][col + 2] == cornerType && powerUpTypes[row][col + 2] == POWERUP_NONE &&
                        !matched[row - 1][col] && !matched[row - 2][col] &&
                        !matched[row][col + 1] && !matched[row][col + 2]) {

                    hasMatch = true;
                    matched[row][col] = true;
                    matched[row - 1][col] = true;
                    matched[row - 2][col] = true;
                    matched[row][col + 1] = true;
                    matched[row][col + 2] = true;

                    powerUpTypes[row][col] = POWERUP_BOMB;
                    candies[row][col].setImageResource(getPowerUpDrawable(POWERUP_BOMB));
                    animatePowerUpCreation(candies[row][col]);

                    candyTypes[row - 1][col] = -1;
                    candyTypes[row - 2][col] = -1;
                    candyTypes[row][col + 1] = -1;
                    candyTypes[row][col + 2] = -1;
                }
                // L-shape: ┘ (corner bottom-right, extends left and up)
                else if (row >= 2 && col >= 2 &&
                        candyTypes[row - 1][col] == cornerType && powerUpTypes[row - 1][col] == POWERUP_NONE &&
                        candyTypes[row - 2][col] == cornerType && powerUpTypes[row - 2][col] == POWERUP_NONE &&
                        candyTypes[row][col - 1] == cornerType && powerUpTypes[row][col - 1] == POWERUP_NONE &&
                        candyTypes[row][col - 2] == cornerType && powerUpTypes[row][col - 2] == POWERUP_NONE &&
                        !matched[row - 1][col] && !matched[row - 2][col] &&
                        !matched[row][col - 1] && !matched[row][col - 2]) {

                    hasMatch = true;
                    matched[row][col] = true;
                    matched[row - 1][col] = true;
                    matched[row - 2][col] = true;
                    matched[row][col - 1] = true;
                    matched[row][col - 2] = true;

                    powerUpTypes[row][col] = POWERUP_BOMB;
                    candies[row][col].setImageResource(getPowerUpDrawable(POWERUP_BOMB));
                    animatePowerUpCreation(candies[row][col]);

                    candyTypes[row - 1][col] = -1;
                    candyTypes[row - 2][col] = -1;
                    candyTypes[row][col - 1] = -1;
                    candyTypes[row][col - 2] = -1;
                }
                // L-shape: ┌ (corner top-left, extends right and down)
                else if (row < GRID_ROWS - 2 && col < GRID_COLS - 2 &&
                        candyTypes[row + 1][col] == cornerType && powerUpTypes[row + 1][col] == POWERUP_NONE &&
                        candyTypes[row + 2][col] == cornerType && powerUpTypes[row + 2][col] == POWERUP_NONE &&
                        candyTypes[row][col + 1] == cornerType && powerUpTypes[row][col + 1] == POWERUP_NONE &&
                        candyTypes[row][col + 2] == cornerType && powerUpTypes[row][col + 2] == POWERUP_NONE &&
                        !matched[row + 1][col] && !matched[row + 2][col] &&
                        !matched[row][col + 1] && !matched[row][col + 2]) {

                    hasMatch = true;
                    matched[row][col] = true;
                    matched[row + 1][col] = true;
                    matched[row + 2][col] = true;
                    matched[row][col + 1] = true;
                    matched[row][col + 2] = true;

                    powerUpTypes[row][col] = POWERUP_BOMB;
                    candies[row][col].setImageResource(getPowerUpDrawable(POWERUP_BOMB));
                    animatePowerUpCreation(candies[row][col]);

                    candyTypes[row + 1][col] = -1;
                    candyTypes[row + 2][col] = -1;
                    candyTypes[row][col + 1] = -1;
                    candyTypes[row][col + 2] = -1;
                }
                // L-shape: ┐ (corner top-right, extends left and down)
                else if (row < GRID_ROWS - 2 && col >= 2 &&
                        candyTypes[row + 1][col] == cornerType && powerUpTypes[row + 1][col] == POWERUP_NONE &&
                        candyTypes[row + 2][col] == cornerType && powerUpTypes[row + 2][col] == POWERUP_NONE &&
                        candyTypes[row][col - 1] == cornerType && powerUpTypes[row][col - 1] == POWERUP_NONE &&
                        candyTypes[row][col - 2] == cornerType && powerUpTypes[row][col - 2] == POWERUP_NONE &&
                        !matched[row + 1][col] && !matched[row + 2][col] &&
                        !matched[row][col - 1] && !matched[row][col - 2]) {

                    hasMatch = true;
                    matched[row][col] = true;
                    matched[row + 1][col] = true;
                    matched[row + 2][col] = true;
                    matched[row][col - 1] = true;
                    matched[row][col - 2] = true;

                    powerUpTypes[row][col] = POWERUP_BOMB;
                    candies[row][col].setImageResource(getPowerUpDrawable(POWERUP_BOMB));
                    animatePowerUpCreation(candies[row][col]);

                    candyTypes[row + 1][col] = -1;
                    candyTypes[row + 2][col] = -1;
                    candyTypes[row][col - 1] = -1;
                    candyTypes[row][col - 2] = -1;
                }
            }
        }

        return hasMatch;
    }

    private void animatePowerUpCreation(ImageView powerUpView) {
        powerUpView.setScaleX(0f);
        powerUpView.setScaleY(0f);

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(powerUpView, "scaleX", 0f, 1.3f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(powerUpView, "scaleY", 0f, 1.3f, 1.0f);
        ObjectAnimator rotation = ObjectAnimator.ofFloat(powerUpView, "rotation", 0f, 360f);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleX, scaleY, rotation);
        set.setDuration(500);
        set.setInterpolator(new OvershootInterpolator(2.0f));
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                powerUpView.setRotation(0f);
            }
        });
        set.start();
    }

    private boolean checkMatches() {
        boolean hasMatch = false;
        boolean[][] matched = new boolean[GRID_ROWS][GRID_COLS];

        // First, check for special shapes (T-shapes and +-shapes) - these get priority
        hasMatch = checkSpecialShapes(matched) || hasMatch;

        // Check for matches of length 7+, 6, 5, 4, and 3 (in descending order for priority)
        int[] matchLengths = {7, 6, 5, 4, 3};

        for (int matchLength : matchLengths) {
            // Horizontal Check
            for (int row = 0; row < GRID_ROWS; row++) {
                for (int col = 0; col <= GRID_COLS - matchLength; col++) {
                    int type = candyTypes[row][col];
                    if (type == -1 || powerUpTypes[row][col] != POWERUP_NONE) continue;

                    // Check if already matched
                    boolean alreadyMatched = false;
                    for (int i = 0; i < matchLength; i++) {
                        if (matched[row][col + i]) {
                            alreadyMatched = true;
                            break;
                        }
                    }
                    if (alreadyMatched) continue;

                    boolean matchFound = true;
                    for (int i = 1; i < matchLength; i++) {
                        if (candyTypes[row][col + i] != type || powerUpTypes[row][col + i] != POWERUP_NONE) {
                            matchFound = false;
                            break;
                        }
                    }

                    if (matchFound) {
                        hasMatch = true;

                        // Mark all cells as matched
                        for (int i = 0; i < matchLength; i++) {
                            matched[row][col + i] = true;
                        }

                        // Create power-up only for 4+ matches
                        if (matchLength >= 4) {
                            createPowerUp(row, col, row, col + matchLength - 1, matchLength, true);
                        } else {
                            // For 3-matches, just remove them
                            removeMatchedCandies(row, col, row, col + matchLength - 1);
                        }
                    }
                }
            }

            // Vertical Check
            for (int col = 0; col < GRID_COLS; col++) {
                for (int row = 0; row <= GRID_ROWS - matchLength; row++) {
                    int type = candyTypes[row][col];
                    if (type == -1 || powerUpTypes[row][col] != POWERUP_NONE) continue;

                    // Check if already matched
                    boolean alreadyMatched = false;
                    for (int i = 0; i < matchLength; i++) {
                        if (matched[row + i][col]) {
                            alreadyMatched = true;
                            break;
                        }
                    }
                    if (alreadyMatched) continue;

                    boolean matchFound = true;
                    for (int i = 1; i < matchLength; i++) {
                        if (candyTypes[row + i][col] != type || powerUpTypes[row + i][col] != POWERUP_NONE) {
                            matchFound = false;
                            break;
                        }
                    }

                    if (matchFound) {
                        hasMatch = true;

                        // Mark all cells as matched
                        for (int i = 0; i < matchLength; i++) {
                            matched[row + i][col] = true;
                        }

                        // Create power-up only for 4+ matches
                        if (matchLength >= 4) {
                            createPowerUp(row, col, row + matchLength - 1, col, matchLength, false);
                        } else {
                            // For 3-matches, just remove them
                            removeMatchedCandies(row, col, row + matchLength - 1, col);
                        }
                    }
                }
            }
        }

        // Mark all matched candies as removed (except power-ups)
        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                if (matched[row][col] && powerUpTypes[row][col] == POWERUP_NONE) {
                    candyTypes[row][col] = -1;
                }
            }
        }

        return hasMatch;
    }

    private void removeMatchedCandies(int row1, int col1, int row2, int col2) {
        int startRow = Math.min(row1, row2);
        int endRow = Math.max(row1, row2);
        int startCol = Math.min(col1, col2);
        int endCol = Math.max(col1, col2);

        for (int row = startRow; row <= endRow; row++) {
            for (int col = startCol; col <= endCol; col++) {
                if (powerUpTypes[row][col] == POWERUP_NONE) {
                    candyTypes[row][col] = -1;
                }
            }
        }
    }

    private void createPowerUp(int startRow, int startCol, int endRow, int endCol, int matchLength, boolean isHorizontal) {
        // Determine the center position for the power-up (where user completed the match)
        int powerUpRow = (startRow + endRow) / 2;
        int powerUpCol = (startCol + endCol) / 2;

        int powerUpType = POWERUP_NONE;

        // Determine power-up type based on match length
        if (matchLength == 4) {
            // Firecracker - horizontal or vertical based on match direction
            powerUpType = isHorizontal ? POWERUP_FIRECRACKER_HORIZONTAL : POWERUP_FIRECRACKER_VERTICAL;
        } else if (matchLength == 5) {
            // Bomb - 2x2 radius
            powerUpType = POWERUP_BOMB;
        } else if (matchLength == 6) {
            // Dynamite - 3x3 radius
            powerUpType = POWERUP_DYNAMITE;
        } else if (matchLength >= 7) {
            // TNT - 4x4 radius
            powerUpType = POWERUP_TNT;
        }

        // Set the power-up at the center position
        powerUpTypes[powerUpRow][powerUpCol] = powerUpType;

        // Important: Don't clear the candy type, just update the visual
        candies[powerUpRow][powerUpCol].setImageResource(getPowerUpDrawable(powerUpType));

        // Animate the power-up creation
        animatePowerUpCreation(candies[powerUpRow][powerUpCol]);
    }

    private int getPowerUpDrawable(int powerUpType) {
        switch (powerUpType) {
            case POWERUP_FIRECRACKER_HORIZONTAL:
            case POWERUP_FIRECRACKER_VERTICAL:
                return R.drawable.ic_firecracker;
            case POWERUP_BOMB:
                return R.drawable.ic_bomb;
            case POWERUP_DYNAMITE:
                return R.drawable.ic_dynamite;
            case POWERUP_TNT:
                return R.drawable.ic_tnt;
            default:
                return 0;
        }
    }

    private void activatePowerUp(int row, int col) {
        int powerUpType = powerUpTypes[row][col];

        isProcessing = true;

        // Decrement moves for power-up activation
        movesLeft--;
        updateMovesText();

        switch (powerUpType) {
            case POWERUP_FIRECRACKER_HORIZONTAL:
                explodeFirecrackerHorizontal(row, col);
                break;
            case POWERUP_FIRECRACKER_VERTICAL:
                explodeFirecrackerVertical(row, col);
                break;
            case POWERUP_BOMB:
                explodeBomb(row, col, 2);
                break;
            case POWERUP_DYNAMITE:
                explodeDynamite(row, col, 3);
                break;
            case POWERUP_TNT:
                explodeTNT(row, col, 4);
                break;
        }

        // Clear the power-up itself
        powerUpTypes[row][col] = POWERUP_NONE;
        candyTypes[row][col] = -1;
        removeCandyWithAnimation(candies[row][col]);

        // Process the board after explosion
        new Handler().postDelayed(() -> {
            // Check if this was a tutorial power-up explosion
            if (isTutorialMode && tutorialPowerUpCreated) {
                // Advance tutorial after explosion completes
                new Handler().postDelayed(() -> {
                    advanceTutorial();
                }, 1500);
            }
            processMatches();
        }, MATCH_DURATION);
    }

    private void explodeFirecrackerHorizontal(int row, int col) {
        // Destroy 5 tiles horizontally (2 left, center, 2 right)
        for (int c = Math.max(0, col - 2); c <= Math.min(GRID_COLS - 1, col + 2); c++) {
            if (candyTypes[row][c] != -1 || powerUpTypes[row][c] != POWERUP_NONE) {
                powerUpTypes[row][c] = POWERUP_NONE;
                candyTypes[row][c] = -1;
                removeCandyWithAnimation(candies[row][c]);
                score += 10 * currentComboMultiplier;
            }
        }
        if (scoreText != null) {
            scoreText.setText("Score: " + score);
        }
    }

    private void explodeFirecrackerVertical(int row, int col) {
        // Destroy 5 tiles vertically (2 up, center, 2 down)
        for (int r = Math.max(0, row - 2); r <= Math.min(GRID_ROWS - 1, row + 2); r++) {
            if (candyTypes[r][col] != -1 || powerUpTypes[r][col] != POWERUP_NONE) {
                powerUpTypes[r][col] = POWERUP_NONE;
                candyTypes[r][col] = -1;
                removeCandyWithAnimation(candies[r][col]);
                score += 10 * currentComboMultiplier;
            }
        }
        if (scoreText != null) {
            scoreText.setText("Score: " + score);
        }
    }

    private void explodeBomb(int row, int col, int radius) {
        // Explode in a 2x2 area (1 tile in each direction from center = 3x3 grid)
        int range = 1;
        for (int r = Math.max(0, row - range); r <= Math.min(GRID_ROWS - 1, row + range); r++) {
            for (int c = Math.max(0, col - range); c <= Math.min(GRID_COLS - 1, col + range); c++) {
                if (candyTypes[r][c] != -1 || powerUpTypes[r][c] != POWERUP_NONE) {
                    powerUpTypes[r][c] = POWERUP_NONE;
                    candyTypes[r][c] = -1;
                    removeCandyWithAnimation(candies[r][c]);
                    score += 10 * currentComboMultiplier;
                }
            }
        }
        if (scoreText != null) {
            scoreText.setText("Score: " + score);
        }
    }

    private void explodeDynamite(int row, int col, int radius) {
        // Explode in a 3x3 area (2 tiles in each direction from center = 5x5 grid)
        int range = 2;
        for (int r = Math.max(0, row - range); r <= Math.min(GRID_ROWS - 1, row + range); r++) {
            for (int c = Math.max(0, col - range); c <= Math.min(GRID_COLS - 1, col + range); c++) {
                if (candyTypes[r][c] != -1 || powerUpTypes[r][c] != POWERUP_NONE) {
                    powerUpTypes[r][c] = POWERUP_NONE;
                    candyTypes[r][c] = -1;
                    removeCandyWithAnimation(candies[r][c]);
                    score += 10 * currentComboMultiplier;
                }
            }
        }
        if (scoreText != null) {
            scoreText.setText("Score: " + score);
        }
    }

    private void explodeTNT(int row, int col, int radius) {
        // Explode in a large area (3 tiles in each direction from center = 7x7 grid)
        int range = 3;
        for (int r = Math.max(0, row - range); r <= Math.min(GRID_ROWS - 1, row + range); r++) {
            for (int c = Math.max(0, col - range); c <= Math.min(GRID_COLS - 1, col + range); c++) {
                if (candyTypes[r][c] != -1 || powerUpTypes[r][c] != POWERUP_NONE) {
                    powerUpTypes[r][c] = POWERUP_NONE;
                    candyTypes[r][c] = -1;
                    removeCandyWithAnimation(candies[r][c]);
                    score += 10 * currentComboMultiplier;
                }
            }
        }
        if (scoreText != null) {
            scoreText.setText("Score: " + score);
        }
    }

    private void removeCandyWithAnimation(ImageView candy) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(candy, "scaleX", 1.0f, 0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(candy, "scaleY", 1.0f, 0f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(candy, "alpha", 1.0f, 0f);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleX, scaleY, alpha);
        set.setDuration(300);
        set.setInterpolator(new AccelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                candy.setImageResource(0);
                candy.setScaleX(1.0f);
                candy.setScaleY(1.0f);
                candy.setAlpha(1.0f);
            }
        });
        set.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        soundPool.release();
        soundPool = null;
    }
}