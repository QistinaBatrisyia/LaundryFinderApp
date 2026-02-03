package com.example.laundryfinderapp;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private TextInputEditText editTextEmail, editTextPassword;
    private CheckBox checkRemember;

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        editTextEmail = findViewById(R.id.edtEmail);
        editTextPassword = findViewById(R.id.edtPassword);
        checkRemember = findViewById(R.id.checkRemember);

        prefs = getSharedPreferences("login_prefs", MODE_PRIVATE);

        boolean remember = prefs.getBoolean("remember", false);
        String savedEmail = prefs.getString("email", "");

        if (remember) {
            editTextEmail.setText(savedEmail);
            checkRemember.setChecked(true);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        1001
                );
            }
        }
    }

    // LOGIN
    public void login(View view) {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this,
                    getString(R.string.error_fill_all),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        SharedPreferences.Editor editor = prefs.edit();
                        if (checkRemember.isChecked()) {
                            editor.putString("email", email);
                            editor.putBoolean("remember", true);
                        } else {
                            editor.clear();
                        }
                        editor.apply();

                        Toast.makeText(this,
                                getString(R.string.login_success),
                                Toast.LENGTH_SHORT).show();

                        startActivity(new Intent(
                                LoginActivity.this,
                                HomePageActivity.class));
                        finish();

                    } else {
                        try {
                            throw task.getException();
                        } catch (FirebaseAuthInvalidCredentialsException e) {
                            Toast.makeText(this,
                                    getString(R.string.invalid_login),
                                    Toast.LENGTH_SHORT).show();
                        } catch (FirebaseAuthInvalidUserException e) {
                            Toast.makeText(this,
                                    getString(R.string.account_not_found),
                                    Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Toast.makeText(this,
                                    e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    public void goToSignUp(View view) {
        startActivity(new Intent(this, SignUpActivity.class));
    }

    public void goToForgotPassword(View view) {
        startActivity(new Intent(this, ForgotPasswordActivity.class));
    }

    public void openGmail(View view) {
        startActivity(new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://mail.google.com/")));
    }

    public void openInstagram(View view) {
        startActivity(new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://www.instagram.com/")));
    }

    public void openTiktok(View view) {
        startActivity(new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://www.tiktok.com/")));
    }
}






