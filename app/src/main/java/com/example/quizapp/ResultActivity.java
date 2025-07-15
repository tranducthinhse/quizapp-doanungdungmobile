package com.example.quizapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class ResultActivity extends AppCompatActivity {

    TextView tvScore, tvPercentage;
    MaterialButton btnRetry, btnExit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        // Ánh xạ view
        tvScore = findViewById(R.id.tv_score);
        tvPercentage = findViewById(R.id.tv_percentage);
        btnRetry = findViewById(R.id.btn_retry);
        btnExit = findViewById(R.id.btn_exit);

        // Nhận dữ liệu từ QuizActivity
        Intent intent = getIntent();
        int score = intent.getIntExtra("score", 0);
        int total = intent.getIntExtra("total", 1); // Tránh chia cho 0

        // Gán dữ liệu ra giao diện
        tvScore.setText(score + "/" + total);

        int percent = (int) ((score * 100.0f) / total);
        tvPercentage.setText(percent + "% chính xác");

        // Nút Chơi lại
        btnRetry.setOnClickListener(v -> {
            Intent retryIntent = new Intent(ResultActivity.this, QuizActivity.class);
            startActivity(retryIntent);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            finish();
        });

        // Nút Thoát
        btnExit.setOnClickListener(v -> {
            Intent exitIntent = new Intent(ResultActivity.this, MainActivity.class);
            exitIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(exitIntent);
            finish();
        });


    }
}
