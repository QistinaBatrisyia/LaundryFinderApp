package com.example.laundryfinderapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.io.OutputStream;

public class ReceiptCameraActivity extends AppCompatActivity {

    private ImageView imgReceipt;
    private Bitmap capturedBitmap = null;

    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    if (extras != null && extras.get("data") instanceof Bitmap) {
                        capturedBitmap = (Bitmap) extras.get("data"); // thumbnail bitmap
                        imgReceipt.setImageBitmap(capturedBitmap);
                    }
                } else {
                    Toast.makeText(this, "Camera cancelled", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt_camera);

        imgReceipt = findViewById(R.id.imgReceipt);
        Button btnTakePhoto = findViewById(R.id.btnTakePhoto);
        Button btnSave = findViewById(R.id.btnSave);
        TextView txtBack = findViewById(R.id.txtBack);

        btnTakePhoto.setOnClickListener(v -> openCamera());
        btnSave.setOnClickListener(v -> savePhotoToGallery());
        txtBack.setOnClickListener(v -> finish());
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraLauncher.launch(intent);
    }

    private void savePhotoToGallery() {
        if (capturedBitmap == null) {
            Toast.makeText(this, "Please take a photo first.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            String filename = "LaundryReceipt_" + System.currentTimeMillis() + ".jpg";

            Uri imageUri = getContentResolver().insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    new android.content.ContentValues()
            );

            if (imageUri == null) {
                Toast.makeText(this, "Failed to create file.", Toast.LENGTH_SHORT).show();
                return;
            }

            OutputStream outputStream = getContentResolver().openOutputStream(imageUri);
            if (outputStream != null) {
                capturedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                outputStream.close();
                Toast.makeText(this, "Saved to Gallery!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to save image.", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Toast.makeText(this, "Save failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
