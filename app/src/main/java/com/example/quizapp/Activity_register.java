package com.example.quizapp;

import android.content.Intent;
import android.os.Bundle;
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

public class Activity_register extends AppCompatActivity {

    EditText editTextEmailRegister, editTextPasswordRegister, editTextConfirmPassword;
    Button buttonRegister;
    TextView textBackToLogin;

    FirebaseAuth mAuth; // thêm FirebaseAuth

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.registerLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Khởi tạo FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // Ánh xạ view
        editTextEmailRegister = findViewById(R.id.editTextEmailRegister);
        editTextPasswordRegister = findViewById(R.id.editTextPasswordRegister);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        buttonRegister = findViewById(R.id.buttonRegister);
        textBackToLogin = findViewById(R.id.textBackToLogin);

        // Xử lý nút Đăng ký
        buttonRegister.setOnClickListener(v -> handleRegister());

        // Xử lý click "Đã có tài khoản? Đăng nhập"
        textBackToLogin.setOnClickListener(v -> {
            finish(); // Quay về Login
        });
    }

    private void handleRegister() {
        String email = editTextEmailRegister.getText().toString().trim();
        String password = editTextPasswordRegister.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Mật khẩu phải từ 6 ký tự trở lên", Toast.LENGTH_SHORT).show();
            return;
        }

        // Đăng ký với FirebaseAuth
        buttonRegister.setEnabled(false);
        buttonRegister.setText("Đang đăng ký...");

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    buttonRegister.setEnabled(true);
                    buttonRegister.setText("ĐĂNG KÝ");

                    if (task.isSuccessful()) {
                        Toast.makeText(Activity_register.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                        finish(); // Quay về Login
                    } else {
                        Toast.makeText(Activity_register.this, "Đăng ký thất bại: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
