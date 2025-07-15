package com.example.quizapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

public class Activity_settings extends AppCompatActivity {

    Switch switchBackgroundMusic, switchCorrectSound, switchWrongSound, switchTimer;
    SharedPreferences sharedPreferences;
    boolean isMusicOn;

    SeekBar seekBarQuestionCount;
    TextView tvQuestionCount;
    MaterialButton btnSave, btnCancel;

    int selectedQuestionCount = 5;
    boolean isTimerEnabled = false;

    SeekBar seekbarTotalTimer;
    TextView tvTotalTimer;

    ImageView avatar1, avatar2, avatar3, avatar4;
    int selectedAvatarResId = R.drawable.avatar_1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            v.setPadding(
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).left,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).top,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).right,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            );
            return insets;
        });

        sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE);

        seekbarTotalTimer = findViewById(R.id.seekbar_total_timer);
        tvTotalTimer = findViewById(R.id.tv_total_timer);
        int savedTimeInSec = sharedPreferences.getInt("quiz_total_time", 300);
        int minutes = savedTimeInSec / 60;
        seekbarTotalTimer.setProgress(minutes);
        tvTotalTimer.setText(minutes + ":00");

        seekbarTotalTimer.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int displayMin = Math.max(progress, 1);
                tvTotalTimer.setText(displayMin + ":00");
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int finalMin = Math.max(seekBar.getProgress(), 1);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("quiz_total_time", finalMin * 60);
                editor.apply();
            }
        });

        switchBackgroundMusic = findViewById(R.id.switch_background_music);
        switchCorrectSound = findViewById(R.id.switch_correct_sound);
        switchWrongSound = findViewById(R.id.switch_wrong_sound);
        switchTimer = findViewById(R.id.switch_question_timer);
        seekBarQuestionCount = findViewById(R.id.seekbar_question_count);
        tvQuestionCount = findViewById(R.id.tv_question_count);
        btnSave = findViewById(R.id.btn_save);
        btnCancel = findViewById(R.id.btn_cancel);

        avatar1 = findViewById(R.id.avatar_1);
        avatar2 = findViewById(R.id.avatar_2);
        avatar3 = findViewById(R.id.avatar_3);
        avatar4 = findViewById(R.id.avatar_4);

        isMusicOn = sharedPreferences.getBoolean("music_on", false);
        switchBackgroundMusic.setChecked(isMusicOn);
        switchBackgroundMusic.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean("music_on", isChecked).apply();
            if (isChecked) {
                MusicPlayerManager.startMusic(Activity_settings.this);
            } else {
                MusicPlayerManager.stopMusic();
            }
        });

        boolean isCorrectOn = sharedPreferences.getBoolean("correct_sound_on", true);
        switchCorrectSound.setChecked(isCorrectOn);
        switchCorrectSound.setOnCheckedChangeListener((buttonView, isChecked) ->
                sharedPreferences.edit().putBoolean("correct_sound_on", isChecked).apply()
        );

        boolean isWrongOn = sharedPreferences.getBoolean("wrong_sound_on", true);
        switchWrongSound.setChecked(isWrongOn);
        switchWrongSound.setOnCheckedChangeListener((buttonView, isChecked) ->
                sharedPreferences.edit().putBoolean("wrong_sound_on", isChecked).apply()
        );

        isTimerEnabled = sharedPreferences.getBoolean("question_timer_on", false);
        switchTimer.setChecked(isTimerEnabled);
        switchTimer.setOnCheckedChangeListener((buttonView, isChecked) ->
                sharedPreferences.edit().putBoolean("question_timer_on", isChecked).apply()
        );

        int currentCount = sharedPreferences.getInt("question_count", 5);
        selectedQuestionCount = currentCount;
        seekBarQuestionCount.setProgress(currentCount);
        tvQuestionCount.setText(String.valueOf(currentCount));

        seekBarQuestionCount.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int value = Math.max(1, progress);
                selectedQuestionCount = value;
                tvQuestionCount.setText(String.valueOf(value));
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        int savedAvatarResId = sharedPreferences.getInt("avatar_res_id", R.drawable.avatar_1);
        if (savedAvatarResId == R.drawable.avatar_1) selectAvatar(avatar1, savedAvatarResId);
        else if (savedAvatarResId == R.drawable.avatar_2) selectAvatar(avatar2, savedAvatarResId);
        else if (savedAvatarResId == R.drawable.avatar_3) selectAvatar(avatar3, savedAvatarResId);
        else if (savedAvatarResId == R.drawable.avatar_4) selectAvatar(avatar4, savedAvatarResId);

        avatar1.setOnClickListener(v -> selectAvatar(avatar1, R.drawable.avatar_1));
        avatar2.setOnClickListener(v -> selectAvatar(avatar2, R.drawable.avatar_2));
        avatar3.setOnClickListener(v -> selectAvatar(avatar3, R.drawable.avatar_3));
        avatar4.setOnClickListener(v -> selectAvatar(avatar4, R.drawable.avatar_4));

        btnSave.setOnClickListener(v -> {
            sharedPreferences.edit()
                    .putInt("question_count", selectedQuestionCount)
                    .putInt("avatar_res_id", selectedAvatarResId)
                    .apply();
            Toast.makeText(this, "\u0110\u00e3 l\u01b0u c\u00e0i \u0111\u1eb7t", Toast.LENGTH_SHORT).show();
            finish();
        });

        btnCancel.setOnClickListener(v -> finish());
    }

    private void selectAvatar(ImageView selectedView, int avatarResId) {
        avatar1.setBackground(null);
        avatar2.setBackground(null);
        avatar3.setBackground(null);
        avatar4.setBackground(null);

        selectedView.setBackgroundResource(R.drawable.avatar_selected_background);
        selectedAvatarResId = avatarResId;
    }
}
