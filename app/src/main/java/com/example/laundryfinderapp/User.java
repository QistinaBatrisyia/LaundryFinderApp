package com.example.laundryfinderapp;

public class User {
    public String email;      // User's email
    public String preference; // User's preferences (e.g., laundry service)

    // Default constructor required for Firebase
    public User() {}

    // Constructor with parameters
    public User(String email, String preference) {
        this.email = email;
        this.preference = preference;
    }
}

