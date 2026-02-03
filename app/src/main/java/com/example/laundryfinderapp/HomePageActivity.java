package com.example.laundryfinderapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class HomePageActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        mAuth = FirebaseAuth.getInstance();
        // ❌ NO findViewById
        // ❌ NO CardView references
    }

    // ---------------- NAVIGATION METHODS ----------------

    public void openNearby(View view) {
        startActivity(new Intent(this, NearbyLaundryMapActivity.class));
    }

    public void openReminder(View view) {
        startActivity(new Intent(this, ReminderActivity.class));
    }

    public void openUploadReceipt(View view) {
        startActivity(new Intent(this, ReceiptProofActivity.class));
    }

    public void openMyProof(View view) {
        startActivity(new Intent(this, ProofListActivity.class));
    }

    public void openAbout(View view) {
        startActivity(new Intent(this, AboutActivity.class));
    }

    public void logout(View view) {
        mAuth.signOut();
        Intent i = new Intent(this, LoginActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
}


