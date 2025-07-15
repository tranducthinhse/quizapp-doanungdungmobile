package com.example.quizapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import com.example.quizapp.MusicPlayerManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Phát nhạc nếu được bật
        SharedPreferences prefs = getSharedPreferences("AppSettings", MODE_PRIVATE);
        boolean isMusicOn = prefs.getBoolean("music_on", false);
        if (isMusicOn) {
            MusicPlayerManager.startMusic(this);
        }

        // Gắn sự kiện cho cardSettings
        CardView cardSettings = findViewById(R.id.cardSettings);
        cardSettings.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Activity_settings.class);
            startActivity(intent);
        });

        CardView cardQuizAnswer = findViewById(R.id.cardQuizAnswer);
        cardQuizAnswer.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, QuizActivity.class);
            startActivity(intent);
        });

        CardView cardCreateQuiz = findViewById(R.id.cardCreateQuiz);
        cardCreateQuiz.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddQuestionActivity.class);
            startActivity(intent);
        });
    }



    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = getSharedPreferences("AppSettings", MODE_PRIVATE);
        boolean isMusicOn = prefs.getBoolean("music_on", false);

        if (isMusicOn && !MusicPlayerManager.isPlaying()) {
            MusicPlayerManager.startMusic(this);
        } else if (!isMusicOn && MusicPlayerManager.isPlaying()) {
            MusicPlayerManager.stopMusic();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Tùy chọn: bạn có thể dừng nhạc khi thoát app hoàn toàn
        // MusicPlayerManager.release();
    }
}
