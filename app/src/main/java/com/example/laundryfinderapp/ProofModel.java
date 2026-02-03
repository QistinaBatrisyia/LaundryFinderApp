package com.example.laundryfinderapp;

public class ProofModel {
    public String proofId;
    public String imageUrl;
    public String type;
    public String note;
    public long createdAt;

    public ProofModel() {
        // Required empty constructor for Firebase
    }

    public ProofModel(String proofId, String imageUrl, String type, String note, long createdAt) {
        this.proofId = proofId;
        this.imageUrl = imageUrl;
        this.type = type;
        this.note = note;
        this.createdAt = createdAt;
    }
}

