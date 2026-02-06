package com.example.colorgame;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class ColorGameMenu extends AppCompatActivity {

    Button btnStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.colorgamemenu);

        btnStart = findViewById(R.id.btnStart);

        btnStart.setOnClickListener(v -> {
            Intent intent = new Intent(ColorGameMenu.this, ColorGame.class);
            startActivity(intent);
        });
    }
}
