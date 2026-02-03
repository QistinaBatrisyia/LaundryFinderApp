package com.example.laundryfinderapp;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UserProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText editTextEmail;
    private EditText editTextPreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile); // Profile Editing Layout

        mAuth = FirebaseAuth.getInstance();  // Initialize Firebase Authentication
        editTextEmail = findViewById(R.id.editTextEmail);  // Find email EditText
        editTextPreference = findViewById(R.id.editTextPreference);  // Find preference EditText

        // Example: Load user data (email and preferences) from Firebase and display it
        loadUserData();
    }

    // Method to load user data from Firebase
    private void loadUserData() {
        String userId = mAuth.getCurrentUser().getUid(); // Get current user's unique ID
        DatabaseReference database = FirebaseDatabase.getInstance().getReference("users");

        database.child(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                User user = task.getResult().getValue(User.class);  // Get user data from database
                if (user != null) {
                    // Set the user data to the EditText fields
                    editTextEmail.setText(user.email);
                    editTextPreference.setText(user.preference);
                }
            } else {
                Toast.makeText(UserProfileActivity.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Save the updated profile data (email and preference) to Firebase
    public void saveChanges(View view) {
        String updatedEmail = editTextEmail.getText().toString();
        String updatedPreference = editTextPreference.getText().toString();

        // Check if fields are empty
        if (updatedEmail.isEmpty() || updatedPreference.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get current user's ID and reference the "users" node in Firebase
        String userId = mAuth.getCurrentUser().getUid();
        DatabaseReference database = FirebaseDatabase.getInstance().getReference("users");

        // Create a User object to hold the updated data
        User updatedUser = new User(updatedEmail, updatedPreference);

        // Update the user data in Firebase
        database.child(userId).setValue(updatedUser)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(UserProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    // Optionally navigate back to the Home page or another screen
                    finish();  // Finish this activity and go back to the previous one
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(UserProfileActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                });
    }
}

