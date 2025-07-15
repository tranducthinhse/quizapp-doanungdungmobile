package com.example.quizapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quizapp.adapter.QuestionAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

public class AddQuestionActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private QuestionAdapter adapter;
    private List<Question> questionList = new ArrayList<>();
    private FirebaseFirestore db;
    private String currentUserId;

    private NestedScrollView nestedScrollView;

    private EditText etQuestion, etOptionA, etOptionB, etOptionC, etOptionD;
    private RadioGroup rgType, layoutTrueFalse, rgCorrectAnswer;
    private LinearLayout layoutMultipleChoice;
    private Button btnSaveQuestion;
    private TextView tvQuestionCount;

    private boolean isEditing = false;
    private int editingPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_question);

        nestedScrollView = findViewById(R.id.main); // 🛠️ FIXED: ánh xạ NestedScrollView để tránh NullPointerException

        ViewCompat.setOnApplyWindowInsetsListener(nestedScrollView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Ánh xạ view
        etQuestion = findViewById(R.id.etQuestion);
        rgType = findViewById(R.id.rgType);
        layoutTrueFalse = findViewById(R.id.layoutTrueFalse);
        layoutMultipleChoice = findViewById(R.id.layoutMultipleChoice);
        rgCorrectAnswer = findViewById(R.id.rgCorrectAnswer);

        etOptionA = findViewById(R.id.etOptionA);
        etOptionB = findViewById(R.id.etOptionB);
        etOptionC = findViewById(R.id.etOptionC);
        etOptionD = findViewById(R.id.etOptionD);

        btnSaveQuestion = findViewById(R.id.btnSaveQuestion);
        tvQuestionCount = findViewById(R.id.tvQuestionCount);

        recyclerView = findViewById(R.id.recyclerViewQuestions);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new QuestionAdapter(this, questionList, new QuestionAdapter.OnItemClickListener() {
            @Override
            public void onEdit(int position) {
                Question q = questionList.get(position);
                isEditing = true;
                editingPosition = position;

                etQuestion.setText(q.getQuestion());
                if (q.getType().equals("true_false")) {
                    rgType.check(R.id.rbTrueFalse);
                    layoutTrueFalse.check(q.getCorrectAnswer().equals("Đúng") ? R.id.rbTrue : R.id.rbFalse);
                } else {
                    rgType.check(R.id.rbMultipleChoice);
                    etOptionA.setText(q.getOptionA());
                    etOptionB.setText(q.getOptionB());
                    etOptionC.setText(q.getOptionC());
                    etOptionD.setText(q.getOptionD());

                    switch (q.getCorrectAnswer()) {
                        case "A": rgCorrectAnswer.check(R.id.rbA); break;
                        case "B": rgCorrectAnswer.check(R.id.rbB); break;
                        case "C": rgCorrectAnswer.check(R.id.rbC); break;
                        case "D": rgCorrectAnswer.check(R.id.rbD); break;
                    }
                }

                Toast.makeText(AddQuestionActivity.this, "Đang chỉnh sửa câu hỏi", Toast.LENGTH_SHORT).show();
                btnSaveQuestion.setText("Cập nhật");
                btnSaveQuestion.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_light));

                // Cuộn lên đầu form
                nestedScrollView.post(() -> nestedScrollView.smoothScrollTo(0, 0));
            }

            @Override
            public void onDelete(int position) {
                deleteQuestion(position);
            }
        });
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        rgType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbTrueFalse) {
                layoutTrueFalse.setVisibility(View.VISIBLE);
                layoutMultipleChoice.setVisibility(View.GONE);
            } else {
                layoutTrueFalse.setVisibility(View.GONE);
                layoutMultipleChoice.setVisibility(View.VISIBLE);
            }
        });

        loadQuestions();
        setupAddQuestion();
    }

    private void setupAddQuestion() {
        btnSaveQuestion.setOnClickListener(v -> {
            String questionText = etQuestion.getText().toString().trim();
            if (questionText.isEmpty()) {
                etQuestion.setError("Vui lòng nhập nội dung câu hỏi");
                return;
            }

            String type = rgType.getCheckedRadioButtonId() == R.id.rbTrueFalse ? "true_false" : "multiple_choice";

            Question question = isEditing ? questionList.get(editingPosition) : new Question();
            question.setQuestion(questionText);
            question.setOwnerId(currentUserId);
            question.setType(type);

            if (type.equals("true_false")) {
                int answerId = layoutTrueFalse.getCheckedRadioButtonId();
                question.setCorrectAnswer(answerId == R.id.rbTrue ? "Đúng" : "Sai");
            } else {
                String a = etOptionA.getText().toString().trim();
                String b = etOptionB.getText().toString().trim();
                String c = etOptionC.getText().toString().trim();
                String d = etOptionD.getText().toString().trim();

                if (a.isEmpty() || b.isEmpty() || c.isEmpty() || d.isEmpty()) {
                    Toast.makeText(this, "Vui lòng nhập đầy đủ 4 đáp án", Toast.LENGTH_SHORT).show();
                    return;
                }

                question.setOptionA(a);
                question.setOptionB(b);
                question.setOptionC(c);
                question.setOptionD(d);

                int answerId = rgCorrectAnswer.getCheckedRadioButtonId();
                if (answerId == R.id.rbA) question.setCorrectAnswer("A");
                else if (answerId == R.id.rbB) question.setCorrectAnswer("B");
                else if (answerId == R.id.rbC) question.setCorrectAnswer("C");
                else if (answerId == R.id.rbD) question.setCorrectAnswer("D");
            }

            if (isEditing) {
                db.collection("questions").document(question.getId())
                        .set(question)
                        .addOnSuccessListener(unused -> {
                            adapter.notifyItemChanged(editingPosition);
                            Toast.makeText(this, "Đã cập nhật câu hỏi", Toast.LENGTH_SHORT).show();
                            clearInput();
                            updateQuestionCount();
                        })
                        .addOnFailureListener(e -> {
                            Log.e("UpdateQuestion", "Lỗi khi cập nhật câu hỏi", e);
                            Toast.makeText(this, "Lỗi khi cập nhật", Toast.LENGTH_SHORT).show();
                        });
            } else {
                db.collection("questions")
                        .add(question)
                        .addOnSuccessListener(docRef -> {
                            question.setId(docRef.getId());
                            questionList.add(question);
                            adapter.notifyItemInserted(questionList.size() - 1);
                            updateQuestionCount();
                            Toast.makeText(this, "Đã thêm câu hỏi thành công", Toast.LENGTH_SHORT).show();
                            clearInput();
                        })
                        .addOnFailureListener(e -> {
                            Log.e("AddQuestion", "Lỗi khi thêm câu hỏi", e);
                            Toast.makeText(this, "Đã xảy ra lỗi. Vui lòng thử lại!", Toast.LENGTH_SHORT).show();
                        });
            }
        });
    }

    private void clearInput() {
        etQuestion.setText("");
        etOptionA.setText("");
        etOptionB.setText("");
        etOptionC.setText("");
        etOptionD.setText("");
        layoutTrueFalse.check(R.id.rbTrue);
        rgCorrectAnswer.check(R.id.rbA);
        rgType.check(R.id.rbTrueFalse);
        btnSaveQuestion.setText("Thêm câu hỏi");
        btnSaveQuestion.setBackgroundColor(getResources().getColor(com.google.android.material.R.color.design_default_color_primary));
        isEditing = false;
        editingPosition = -1;
    }

    private void loadQuestions() {
        db.collection("questions")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    questionList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Question q = doc.toObject(Question.class);
                        q.setId(doc.getId());
                        questionList.add(q);
                    }
                    adapter.notifyDataSetChanged();
                    updateQuestionCount();
                });
    }

    private void updateQuestionCount() {
        int count = questionList.size();
        String text = count + " câu hỏi";
        tvQuestionCount.setText(text);
    }

    private void deleteQuestion(int position) {
        Question q = questionList.get(position);

        new AlertDialog.Builder(this)
                .setTitle("Xóa câu hỏi")
                .setMessage("Bạn có chắc muốn xóa câu hỏi này không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    db.collection("questions").document(q.getId())
                            .delete()
                            .addOnSuccessListener(unused -> {
                                questionList.remove(position);
                                adapter.notifyItemRemoved(position);
                                updateQuestionCount();
                                Toast.makeText(this, "Đã xóa câu hỏi", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Lỗi khi xóa câu hỏi", Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
