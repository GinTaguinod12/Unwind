package com.example.meditate_maintab;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.animation.ScaleAnimation;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class MeditationPlayerActivity extends AppCompatActivity {

    ImageButton btnPlayPause, btnBack;
    TextView tvTimer, tvTitle;

    MediaPlayer mediaPlayer;
    CountDownTimer countDownTimer;
    boolean isPlaying = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Toast.makeText(this, "PLAYER OPENED", Toast.LENGTH_LONG).show();

        setContentView(R.layout.activity_meditation_player);


        btnPlayPause = findViewById(R.id.btnPlayPause);
        btnBack = findViewById(R.id.btnBack);
        tvTimer = findViewById(R.id.tvTimer);
        tvTitle = findViewById(R.id.tvTitle);

        String title = getIntent().getStringExtra("title");
        if (title == null) title = "Meditation";
        tvTitle.setText(title);
        int audioRes = getIntent().getIntExtra("audio", 0);
        long time = getIntent().getLongExtra("time", 300000);

        tvTitle.setText(title);

        mediaPlayer = MediaPlayer.create(this, audioRes);

        if (mediaPlayer == null) {
            Toast.makeText(this, "Audio file not found", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        startTimer(time);

        btnPlayPause.setOnClickListener(v -> {
            animateButton(btnPlayPause);
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                btnPlayPause.setImageResource(R.drawable.baseline_play_circle_outline_24);
            } else {
                mediaPlayer.start();
                btnPlayPause.setImageResource(R.drawable.baseline_pause_circle_outline_24);
            }
        });

        btnBack.setOnClickListener(v -> finish());
    }

    private void startTimer(long millis) {
        countDownTimer = new CountDownTimer(millis, 1000) {
            @Override
            public void onTick(long ms) {
                int min = (int) (ms / 1000) / 60;
                int sec = (int) (ms / 1000) % 60;
                tvTimer.setText(String.format(Locale.getDefault(), "%02d:%02d", min, sec));
            }

            @Override
            public void onFinish() {
                tvTimer.setText("00:00");
                mediaPlayer.pause();
            }
        }.start();
    }

    private void animateButton(View view) {
        ScaleAnimation scale = new ScaleAnimation(
                1f, 0.9f, 1f, 0.9f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f
        );
        scale.setDuration(150);
        scale.setRepeatCount(1);
        scale.setRepeatMode(ScaleAnimation.REVERSE);
        view.startAnimation(scale);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
