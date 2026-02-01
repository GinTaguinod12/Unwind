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

public class RegisterPage extends AppCompatActivity {
    EditText regName, regPassword;
    Button saveBtn, cancelBtn;
    dbHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.register_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        regName = findViewById(R.id.regName);
        regPassword = findViewById(R.id.regPassword);
        saveBtn = findViewById(R.id.saveBtn);
        cancelBtn = findViewById(R.id.cancelBtn);
        db = new dbHelper(this);

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = regName.getText().toString();
                String pass = regPassword.getText().toString();

                if (!name.isBlank() && !pass.isBlank()) {
                    if (db.userExists(name, pass)) {
                        Toast.makeText(RegisterPage.this, "Please use other password.", Toast.LENGTH_SHORT).show();
                    } else {
                        User user = new User(name, pass);
                        db.registerUser(user);

                        Intent i = new Intent(RegisterPage.this, LoginPage.class);
                        Toast.makeText(RegisterPage.this, "Account Saved.", Toast.LENGTH_SHORT).show();
                        startActivity(i);
                    }
                } else {
                    Toast.makeText(RegisterPage.this, "Please fill all fields.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(RegisterPage.this, "Returning..", Toast.LENGTH_SHORT).show();
                Intent i2 = new Intent(RegisterPage.this, LoginPage.class);
                startActivity(i2);
            }
        });
    }
}
