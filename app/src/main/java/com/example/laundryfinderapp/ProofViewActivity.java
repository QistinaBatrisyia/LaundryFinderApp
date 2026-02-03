package com.example.laundryfinderapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class ProofViewActivity extends AppCompatActivity {

    private ImageView imgPreview;
    private EditText edtNote;
    private RadioGroup rgPurpose;
    private RadioButton rbReceipt, rbLaundry, rbIssue;
    private Button btnUpdate, btnBack;

    private String proofId;
    private String imageUrl;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_proof_view);

        imgPreview = findViewById(R.id.imgPreview);
        edtNote = findViewById(R.id.edtNote);
        rgPurpose = findViewById(R.id.rgPurpose);

        rbReceipt = findViewById(R.id.rbReceipt);
        rbLaundry = findViewById(R.id.rbLaundry);
        rbIssue = findViewById(R.id.rbIssue);

        btnUpdate = findViewById(R.id.btnUpdate);
        btnBack = findViewById(R.id.btnBack);

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // GET DATA
        proofId = getIntent().getStringExtra("proofId");
        imageUrl = getIntent().getStringExtra("imageUrl");
        String type = getIntent().getStringExtra("type");
        String note = getIntent().getStringExtra("note");

        Glide.with(this).load(imageUrl).into(imgPreview);
        edtNote.setText(note == null ? "" : note);

        if ("RECEIPT".equals(type)) rbReceipt.setChecked(true);
        else if ("LAUNDRY_PROOF".equals(type)) rbLaundry.setChecked(true);
        else if ("ISSUE_PROOF".equals(type)) rbIssue.setChecked(true);

        btnBack.setOnClickListener(v -> finish());

        btnUpdate.setOnClickListener(v -> updateProof());
    }

    private String getSelectedType() {
        if (rbReceipt.isChecked()) return "RECEIPT";
        if (rbLaundry.isChecked()) return "LAUNDRY_PROOF";
        if (rbIssue.isChecked()) return "ISSUE_PROOF";
        return null;
    }

    private void updateProof() {

        String newNote = edtNote.getText().toString().trim();
        String newType = getSelectedType();

        if (newType == null) {
            Toast.makeText(this, "Please select a purpose", Toast.LENGTH_SHORT).show();
            return;
        }

        HashMap<String, Object> map = new HashMap<>();
        map.put("note", newNote);
        map.put("type", newType);

        FirebaseDatabase.getInstance()
                .getReference("proofs")
                .child(uid)
                .child(proofId)
                .updateChildren(map)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Updated successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
}

