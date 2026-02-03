package com.example.timertesting;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

public class FocusActivity extends AppCompatActivity {

    TextView tvTimer;
    Button btnPlayPause, btnStop;
    CountDownTimer timer;

    boolean isRunning = false;
    long focusTime, restTime, timeLeft;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.focustimertab);

        tvTimer = findViewById(R.id.tvTimer);
        btnPlayPause = findViewById(R.id.btnPlayPause);
        btnStop = findViewById(R.id.btnStop);

        focusTime = getIntent().getLongExtra("FOCUS_TIME", 0);
        restTime = getIntent().getLongExtra("REST_TIME", 0);

        timeLeft = focusTime;
        updateUI();

        startTimer();

        btnPlayPause.setOnClickListener(v -> togglePlayPause());
        btnStop.setOnClickListener(v -> stopAndReturn());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                stopAndReturn();
            }
        });
    }

    private void startTimer() {
        if (isRunning) return;

        timer = new CountDownTimer(timeLeft, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeft = millisUntilFinished;
                updateUI();
            }

            @Override
            public void onFinish() {
                // Automatically start Rest timer
                Intent intent = new Intent(FocusActivity.this, RestActivity.class);
                intent.putExtra("FOCUS_TIME", focusTime);
                intent.putExtra("REST_TIME", restTime);
                startActivity(intent);
                finish();
            }
        }.start();

        isRunning = true;
        btnPlayPause.setText("PAUSE");
    }

    private void togglePlayPause() {
        if (isRunning) {
            timer.cancel();
            isRunning = false;
            btnPlayPause.setText("PLAY");
        } else {
            startTimer();
        }
    }

    private void updateUI() {
        int h = (int) (timeLeft / 1000) / 3600;
        int m = (int) ((timeLeft / 1000) % 3600) / 60;
        int s = (int) (timeLeft / 1000) % 60;
        tvTimer.setText(String.format("%02d:%02d:%02d", h, m, s));
    }

    private void stopAndReturn() {
        if(timer != null) timer.cancel();

        Intent intent = new Intent(FocusActivity.this, TimerEditor.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
