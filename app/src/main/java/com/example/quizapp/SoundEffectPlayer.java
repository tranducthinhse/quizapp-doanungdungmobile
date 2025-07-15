package com.example.quizapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;

public class SoundEffectPlayer {

    public static void playCorrectSound(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
        boolean isOn = prefs.getBoolean("correct_sound_on", true);
        if (isOn) {
            MediaPlayer mp = MediaPlayer.create(context, R.raw.correct_sound);
            mp.setOnCompletionListener(MediaPlayer::release);
            mp.start();
        }
    }

    public static void playWrongSound(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
        boolean isOn = prefs.getBoolean("wrong_sound_on", true);
        if (isOn) {
            MediaPlayer mp = MediaPlayer.create(context, R.raw.wrong_sound);
            mp.setOnCompletionListener(MediaPlayer::release);
            mp.start();
        }
    }
}
