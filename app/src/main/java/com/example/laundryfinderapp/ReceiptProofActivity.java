package com.example.laundryfinderapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public class ReceiptProofActivity extends AppCompatActivity {

    private ImageView imgPreview;
    private ProgressBar progressBar;
    private EditText edtNote;

    private RadioGroup rgPurpose;
    private RadioButton rbReceipt, rbLaundry, rbIssue;

    private Button btnPickImage, btnUpload, btnMyProofs;

    private Uri selectedImageUri;
    private Uri cameraTempUri;

    private FirebaseAuth mAuth;
    private StorageReference storageRef;
    private DatabaseReference dbRef;

    // MODES
    private boolean attachMode = false;
    private boolean editMode = false;

    // EDIT DATA
    private String editProofId = "";
    private String oldImageUrl = "";

    // Camera permission
    private final ActivityResultLauncher<String> requestCameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) openChooser();
                else Toast.makeText(this, "Camera permission denied.", Toast.LENGTH_LONG).show();
            });

    // Gallery
    private final ActivityResultLauncher<String> pickFromGalleryLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    imgPreview.setImageURI(uri);
                }
            });

    // Camera
    private final ActivityResultLauncher<Uri> takePictureLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (success && cameraTempUri != null) {
                    selectedImageUri = cameraTempUri;
                    imgPreview.setImageURI(selectedImageUri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt_proof);

        imgPreview = findViewById(R.id.imgPreview);
        progressBar = findViewById(R.id.progressBar);
        edtNote = findViewById(R.id.edtNote);

        rgPurpose = findViewById(R.id.rgPurpose);
        rbReceipt = findViewById(R.id.rbReceipt);
        rbLaundry = findViewById(R.id.rbLaundry);
        rbIssue = findViewById(R.id.rbIssue);

        btnPickImage = findViewById(R.id.btnPickImage);
        btnUpload = findViewById(R.id.btnUpload);
        btnMyProofs = findViewById(R.id.btnMyProofs);

        mAuth = FirebaseAuth.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();
        dbRef = FirebaseDatabase.getInstance().getReference();

        attachMode = getIntent().getBooleanExtra("attachMode", false);
        editMode = getIntent().getBooleanExtra("editMode", false);

        if (editMode) {
            loadEditData();
            btnUpload.setText("Update Proof");
        }

        btnPickImage.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
            } else {
                openChooser();
            }
        });

        btnMyProofs.setOnClickListener(v ->
                startActivity(new Intent(this, ProofListActivity.class))
        );

        btnUpload.setOnClickListener(v -> {
            if (editMode) updateProof();
            else uploadNewProof();
        });
    }

    private void loadEditData() {
        editProofId = getIntent().getStringExtra("proofId");
        oldImageUrl = getIntent().getStringExtra("imageUrl");

        String type = getIntent().getStringExtra("type");
        String note = getIntent().getStringExtra("note");

        edtNote.setText(note == null ? "" : note);
        Glide.with(this).load(oldImageUrl).into(imgPreview);

        if ("RECEIPT".equals(type)) rbReceipt.setChecked(true);
        if ("LAUNDRY_PROOF".equals(type)) rbLaundry.setChecked(true);
        if ("ISSUE_PROOF".equals(type)) rbIssue.setChecked(true);
    }

    private void openChooser() {
        String[] options = {"Camera", "Gallery"};
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Select Image")
                .setItems(options, (d, w) -> {
                    if (w == 0) openCamera();
                    else openGallery();
                })
                .show();
    }

    private void openGallery() {
        pickFromGalleryLauncher.launch("image/*");
    }

    private void openCamera() {
        try {
            File temp = File.createTempFile("proof_", ".jpg", getCacheDir());
            cameraTempUri = FileProvider.getUriForFile(
                    this, getPackageName() + ".fileprovider", temp
            );
            takePictureLauncher.launch(cameraTempUri);
        } catch (IOException e) {
            Toast.makeText(this, "Camera error", Toast.LENGTH_SHORT).show();
        }
    }

    private String getSelectedType() {
        if (rbLaundry.isChecked()) return "LAUNDRY_PROOF";
        if (rbIssue.isChecked()) return "ISSUE_PROOF";
        if (rbReceipt.isChecked()) return "RECEIPT";
        return "";
    }

    // ===== CREATE =====
    private void uploadNewProof() {
        if (mAuth.getCurrentUser() == null || selectedImageUri == null) {
            Toast.makeText(this, "Missing data", Toast.LENGTH_SHORT).show();
            return;
        }

        String type = getSelectedType();
        if (type.isEmpty()) {
            Toast.makeText(this, "Choose purpose", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = mAuth.getCurrentUser().getUid();
        String proofId = UUID.randomUUID().toString();

        saveToFirebase(uid, proofId, selectedImageUri, type);
    }

    // ===== UPDATE =====
    private void updateProof() {
        if (mAuth.getCurrentUser() == null) return;

        String uid = mAuth.getCurrentUser().getUid();
        String type = getSelectedType();
        String note = edtNote.getText().toString().trim();

        HashMap<String, Object> map = new HashMap<>();
        map.put("type", type);
        map.put("note", note);

        dbRef.child("proofs").child(uid).child(editProofId)
                .updateChildren(map)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Updated", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show()
                );
    }

    private void saveToFirebase(String uid, String proofId, Uri imageUri, String type) {
        progressBar.setVisibility(View.VISIBLE);
        btnUpload.setEnabled(false);

        StorageReference ref = storageRef.child("proofs/" + uid + "/" + proofId + ".jpg");

        ref.putFile(imageUri)
                .addOnSuccessListener(t ->
                        ref.getDownloadUrl().addOnSuccessListener(url -> {

                            HashMap<String, Object> map = new HashMap<>();
                            map.put("proofId", proofId);
                            map.put("imageUrl", url.toString());
                            map.put("type", type);
                            map.put("note", edtNote.getText().toString().trim());
                            map.put("createdAt", System.currentTimeMillis());

                            dbRef.child("proofs").child(uid).child(proofId)
                                    .setValue(map)
                                    .addOnSuccessListener(u -> {
                                        Toast.makeText(this, "Uploaded", Toast.LENGTH_SHORT).show();
                                        finish();
                                    });
                        })
                );
    }
}

