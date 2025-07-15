package com.example.quizapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuizActivity extends AppCompatActivity {

    private int maxQuestions = 5;
    private int score = 0;
    private int currentIndex = 0;

    FirebaseFirestore db;
    List<Question> questionList = new ArrayList<>();

    TextView tvQuestion, tvResult, tvQuestionCount, tvTimer;
    RadioGroup rgChoices, rgTrueFalse;
    RadioButton rbOption1, rbOption2, rbOption3, rbOption4, rbTrue, rbFalse;
    View cardResult;
    ImageView ivResultIcon;
    MaterialButton btnSubmit;

    CountDownTimer countDownTimer;
    CountDownTimer totalTimer;
    boolean isTimerEnabled = false;
    boolean isTotalTimerEnabled = false;

    int timePerQuestion = 10;
    int totalTimeInSeconds = 300;

    boolean answered = false;
    boolean isTransitioning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_quiz);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        db = FirebaseFirestore.getInstance();
        loadQuestions();

        btnSubmit.setOnClickListener(v -> {
            if (!answered) {
                if (isAnswerSelected()) {
                    checkAnswer(questionList.get(currentIndex), false);
                } else {
                    Toast.makeText(this, "Vui lòng chọn một đáp án!", Toast.LENGTH_SHORT).show();
                }
            } else {
                nextQuestionOrFinish();
            }
        });
    }

    private void initViews() {
        tvQuestion = findViewById(R.id.tv_question);
        tvQuestionCount = findViewById(R.id.tv_question_count);
        tvTimer = findViewById(R.id.tv_timer);
        rgChoices = findViewById(R.id.rg_choices);
        rgTrueFalse = findViewById(R.id.rg_true_false);
        rbOption1 = findViewById(R.id.rb_option1);
        rbOption2 = findViewById(R.id.rb_option2);
        rbOption3 = findViewById(R.id.rb_option3);
        rbOption4 = findViewById(R.id.rb_option4);
        rbTrue = findViewById(R.id.rb_true);
        rbFalse = findViewById(R.id.rb_false);
        cardResult = findViewById(R.id.card_result);
        tvResult = findViewById(R.id.tv_result);
        ivResultIcon = findViewById(R.id.iv_result_icon);
        btnSubmit = findViewById(R.id.btn_submit);
    }

    private void loadQuestions() {
        db.collection("questions")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot doc : querySnapshot) {
                        Question q = doc.toObject(Question.class);
                        questionList.add(q);
                    }

                    if (!questionList.isEmpty()) {
                        SharedPreferences prefs = getSharedPreferences("AppSettings", MODE_PRIVATE);
                        maxQuestions = prefs.getInt("question_count", 5);
                        isTimerEnabled = prefs.getBoolean("question_timer_on", false);
                        totalTimeInSeconds = prefs.getInt("quiz_total_time", 300);
                        isTotalTimerEnabled = totalTimeInSeconds > 0 && !isTimerEnabled;

                        Collections.shuffle(questionList);
                        if (questionList.size() > maxQuestions) {
                            questionList = questionList.subList(0, maxQuestions);
                        }

                        if (isTotalTimerEnabled) startTotalTimer();
                        showQuestion(currentIndex);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi khi tải câu hỏi!", Toast.LENGTH_SHORT).show());
    }

    private void showQuestion(int index) {
        answered = false;
        isTransitioning = false;

        Question q = questionList.get(index);

        tvQuestion.setText(q.getQuestion());
        tvQuestionCount.setText("Câu " + (index + 1) + "/" + questionList.size());

        rgChoices.clearCheck();
        rgTrueFalse.clearCheck();

        btnSubmit.setEnabled(true);
        btnSubmit.setText("Trả lời");

        tvTimer.setTextColor(getColor(R.color.white));
        cardResult.setVisibility(View.GONE);

        if (q.getType().equals("multiple")) {
            rgChoices.setVisibility(View.VISIBLE);
            rgTrueFalse.setVisibility(View.GONE);
            rbOption1.setText("A. " + q.getOptionA());
            rbOption2.setText("B. " + q.getOptionB());
            rbOption3.setText("C. " + q.getOptionC());
            rbOption4.setText("D. " + q.getOptionD());
        } else {
            rgChoices.setVisibility(View.GONE);
            rgTrueFalse.setVisibility(View.VISIBLE);
            rbTrue.setText("✓ " + q.getOptionA());
            rbFalse.setText("✗ " + q.getOptionB());
        }

        if (countDownTimer != null) countDownTimer.cancel();

        if (isTimerEnabled && !isTotalTimerEnabled) {
            tvTimer.setVisibility(View.VISIBLE);
            countDownTimer = new CountDownTimer(timePerQuestion * 1000L, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    int secondsLeft = (int) (millisUntilFinished / 1000);
                    tvTimer.setText("⏰ " + secondsLeft + "s");
                    tvTimer.setTextColor(secondsLeft <= 3 ? getColor(R.color.error) : getColor(R.color.white));
                }

                @Override
                public void onFinish() {
                    tvTimer.setText("⏰ 0s");
                    Toast.makeText(QuizActivity.this, "Hết giờ!", Toast.LENGTH_SHORT).show();
                    if (!answered) checkAnswer(q, true);
                }
            }.start();
        } else if (isTotalTimerEnabled) {
            tvTimer.setVisibility(View.VISIBLE);
            // Đã có timer chạy riêng -> không làm gì thêm
        } else {
            tvTimer.setVisibility(View.GONE);
        }
    }

    private void startTotalTimer() {
        totalTimer = new CountDownTimer(totalTimeInSeconds * 1000L, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int seconds = (int) (millisUntilFinished / 1000);
                int min = seconds / 60;
                int sec = seconds % 60;
                String formatted = String.format("⏰ %02d:%02d", min, sec);
                tvTimer.setText(formatted);
                tvTimer.setTextColor(seconds <= 10 ? getColor(R.color.error) : getColor(R.color.white));
            }

            @Override
            public void onFinish() {
                Toast.makeText(QuizActivity.this, "Hết thời gian làm bài!", Toast.LENGTH_SHORT).show();
                goToResultScreen();
            }
        }.start();
    }

    private boolean isAnswerSelected() {
        Question q = questionList.get(currentIndex);
        if (q.getType().equals("multiple")) {
            return rgChoices.getCheckedRadioButtonId() != -1;
        } else {
            return rgTrueFalse.getCheckedRadioButtonId() != -1;
        }
    }

    private void checkAnswer(Question q, boolean autoNext) {
        if (countDownTimer != null) countDownTimer.cancel();
        answered = true;

        String selected = "";

        if (q.getType().equals("multiple")) {
            int id = rgChoices.getCheckedRadioButtonId();
            if (id == rbOption1.getId()) selected = "A";
            else if (id == rbOption2.getId()) selected = "B";
            else if (id == rbOption3.getId()) selected = "C";
            else if (id == rbOption4.getId()) selected = "D";
        } else {
            int id = rgTrueFalse.getCheckedRadioButtonId();
            if (id == rbTrue.getId()) selected = "A";
            else if (id == rbFalse.getId()) selected = "B";
        }

        SharedPreferences prefs = getSharedPreferences("AppSettings", MODE_PRIVATE);
        boolean playCorrectSound = prefs.getBoolean("correct_sound_on", true);
        boolean playWrongSound = prefs.getBoolean("wrong_sound_on", true);

        boolean isCorrect = selected.equalsIgnoreCase(q.getCorrectAnswer());

        if (isCorrect) {
            score++;
            tvResult.setText("✅ Chính xác!");
            ivResultIcon.setImageResource(R.drawable.ic_check_circle);
            cardResult.setBackgroundColor(getColor(R.color.success_light));
            if (playCorrectSound) playSound(R.raw.correct_sound);
        } else {
            tvResult.setText("❌ Sai rồi! Đáp án đúng: " + q.getCorrectAnswer());
            ivResultIcon.setImageResource(R.drawable.ic_error);
            cardResult.setBackgroundColor(getColor(R.color.error_light));
            if (playWrongSound) playSound(R.raw.wrong_sound);
        }

        cardResult.setVisibility(View.VISIBLE);
        btnSubmit.setText(currentIndex < questionList.size() - 1 ? "Câu tiếp theo" : "Kết thúc");
        btnSubmit.setEnabled(true);

        if (autoNext) {
            new Handler().postDelayed(() -> {
                if (!isTransitioning) {
                    nextQuestionOrFinish();
                }
            }, 2000);
        }
    }

    private void nextQuestionOrFinish() {
        if (isTransitioning) return;
        isTransitioning = true;

        if (currentIndex < questionList.size() - 1) {
            currentIndex++;
            showQuestion(currentIndex);
        } else {
            goToResultScreen();
        }
    }

    private void goToResultScreen() {
        if (countDownTimer != null) countDownTimer.cancel();
        if (totalTimer != null) totalTimer.cancel();

        Intent intent = new Intent(QuizActivity.this, ResultActivity.class);
        intent.putExtra("score", score);
        intent.putExtra("total", questionList.size());
        startActivity(intent);
        finish();
    }

    private void playSound(int resId) {
        try {
            MediaPlayer mp = MediaPlayer.create(this, resId);
            if (mp != null) {
                mp.setOnCompletionListener(MediaPlayer::release);
                mp.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
        if (totalTimer != null) totalTimer.cancel();
    }
}
