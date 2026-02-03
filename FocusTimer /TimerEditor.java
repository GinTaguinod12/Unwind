package com.example.timertesting;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class TimerEditor extends AppCompatActivity {

    EditText etHours, etMinutes, etSeconds;
    EditText rtHours, rtMinutes, rtSeconds;
    Button btnStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timereditortab);

        etHours = findViewById(R.id.etHours);
        etMinutes = findViewById(R.id.etMinutes);
        etSeconds = findViewById(R.id.etSeconds);

        rtHours = findViewById(R.id.rtHours);
        rtMinutes = findViewById(R.id.rtMinutes);
        rtSeconds = findViewById(R.id.rtSeconds);

        btnStart = findViewById(R.id.btnStart);

        btnStart.setOnClickListener(v -> startFocusTimer());
    }

    private void startFocusTimer() {
        long focusMillis = getTimeInMillis(etHours, etMinutes, etSeconds);
        long restMillis = getTimeInMillis(rtHours, rtMinutes, rtSeconds);

        if (focusMillis == 0 || restMillis == 0) {
            Toast.makeText(this, "Please enter focus and rest time", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, FocusActivity.class);
        intent.putExtra("FOCUS_TIME", focusMillis);
        intent.putExtra("REST_TIME", restMillis);
        startActivity(intent);
    }

    private long getTimeInMillis(EditText h, EditText m, EditText s) {
        int hours = h.getText().toString().isEmpty() ? 0 : Integer.parseInt(h.getText().toString());
        int minutes = m.getText().toString().isEmpty() ? 0 : Integer.parseInt(m.getText().toString());
        int seconds = s.getText().toString().isEmpty() ? 0 : Integer.parseInt(s.getText().toString());

        return (hours * 3600L + minutes * 60L + seconds) * 1000L;
    }
}
