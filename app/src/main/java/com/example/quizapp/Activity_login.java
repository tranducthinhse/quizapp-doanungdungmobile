package com.example.quizapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

public class Activity_login extends AppCompatActivity {

    EditText editTextEmail, editTextPassword;
    Button buttonLogin;
    TextView textSignUp, textForgotPassword;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // EdgeToEdge padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Khởi tạo FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // Ánh xạ view
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        textSignUp = findViewById(R.id.textSignUp);
        textForgotPassword = findViewById(R.id.textForgotPassword);

        // Xử lý nút Đăng nhập
        buttonLogin.setOnClickListener(v -> handleLogin());

        // Xử lý click Đăng ký
        textSignUp.setOnClickListener(v -> {
            // Hiệu ứng animation
            Animation anim = AnimationUtils.loadAnimation(Activity_login.this, R.anim.click_animation);
            v.startAnimation(anim);

            Intent intent = new Intent(Activity_login.this, Activity_register.class);
            startActivity(intent);
        });

        // Xử lý click Quên mật khẩu
        textForgotPassword.setOnClickListener(v -> {
            Animation anim = AnimationUtils.loadAnimation(Activity_login.this, R.anim.click_animation);
            v.startAnimation(anim);

            // Mở màn hình quên mật khẩu hoặc xử lý tại đây (chưa có thì toast tạm)
            Toast.makeText(Activity_login.this, "Tính năng quên mật khẩu chưa triển khai!", Toast.LENGTH_SHORT).show();
        });
    }

    private void handleLogin() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ email và mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }

        buttonLogin.setEnabled(false); // chặn spam click
        buttonLogin.setText("Đang đăng nhập...");

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    buttonLogin.setEnabled(true); // mở lại nút
                    buttonLogin.setText("ĐĂNG NHẬP");

                    if (task.isSuccessful()) {
                        Toast.makeText(Activity_login.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();

                        // Chuyển sang MainActivity
                        Intent intent = new Intent(Activity_login.this, MainActivity.class);
                        startActivity(intent);
                        finish(); // Đóng login
                    } else {
                        Toast.makeText(Activity_login.this, "Đăng nhập thất bại: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
