package com.example.functiontesterfpv3;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LoginPage extends AppCompatActivity {
    EditText uName, uPassword;
    Button loginBtn, registerBtn;
    dbHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.login_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        uName = findViewById(R.id.uName);
        uPassword = findViewById(R.id.uPassword);
        loginBtn = findViewById(R.id.loginBtn);
        registerBtn = findViewById(R.id.registerBtn);
        db = new dbHelper(this);

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginPage.this, RegisterPage.class);
                startActivity(i);
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = uName.getText().toString();
                String pass = uPassword.getText().toString();

                if (db.userExists(name, pass)) {
                    Toast.makeText(LoginPage.this, "Logging in..", Toast.LENGTH_SHORT).show();
                    Intent i2 = new Intent(LoginPage.this, MoodSelection.class);

                    long userId = db.getUserId(name, pass);
                    i2.putExtra("userId", userId);
                    startActivity(i2);
                } else {
                    Toast.makeText(LoginPage.this, "Invalid Name/Password", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
