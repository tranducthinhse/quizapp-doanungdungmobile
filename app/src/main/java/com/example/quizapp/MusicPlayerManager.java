package com.example.quizapp;

import android.content.Context;
import android.media.MediaPlayer;

public class MusicPlayerManager {
    private static MediaPlayer mediaPlayer;
    private static boolean isMusicPlaying = false;

    public static void startMusic(Context context) {
        if (!isMusicPlaying) {
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer.create(context.getApplicationContext(), R.raw.background_music);
                mediaPlayer.setLooping(true);
            }
            mediaPlayer.start();
            isMusicPlaying = true;
        }
    }

    public static void stopMusic() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isMusicPlaying = false;
        }
    }

    public static void release() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
            isMusicPlaying = false;
        }
    }

    public static boolean isPlaying() {
        return isMusicPlaying;
    }
}

