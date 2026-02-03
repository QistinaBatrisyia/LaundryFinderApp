package com.example.laundryfinderapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.UUID;

public class IssueReportActivity extends AppCompatActivity {

    private EditText edtTitle, edtDesc;
    private Button btnSubmit;

    private String proofId, imageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issue_report);

        edtTitle = findViewById(R.id.edtIssueTitle);
        edtDesc = findViewById(R.id.edtIssueDesc);
        btnSubmit = findViewById(R.id.btnSubmitIssue);

        proofId = getIntent().getStringExtra("proofId");
        imageUrl = getIntent().getStringExtra("imageUrl");

        btnSubmit.setOnClickListener(v -> submitIssue());
    }

    private void submitIssue() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;

        String title = edtTitle.getText().toString().trim();
        String desc = edtDesc.getText().toString().trim();

        if (title.isEmpty() || desc.isEmpty()) {
            Toast.makeText(this, "Please fill title and description.", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String issueId = UUID.randomUUID().toString();

        HashMap<String, Object> map = new HashMap<>();
        map.put("issueId", issueId);
        map.put("title", title);
        map.put("description", desc);
        map.put("imageUrl", imageUrl);
        map.put("proofId", proofId);
        map.put("createdAt", System.currentTimeMillis());
        map.put("status", "OPEN");

        FirebaseDatabase.getInstance().getReference()
                .child("issues")
                .child(uid)
                .child(issueId)
                .setValue(map)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Issue submitted!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Submit failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
}
