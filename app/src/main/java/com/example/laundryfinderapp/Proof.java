package com.example.laundryfinderapp;

public class Proof {
    public String proofId;
    public String imageUrl;
    public String type;
    public String note;
    public long createdAt;

    public Proof() {}

    public Proof(String proofId, String imageUrl, String type, String note, long createdAt) {
        this.proofId = proofId;
        this.imageUrl = imageUrl;
        this.type = type;
        this.note = note;
        this.createdAt = createdAt;
    }
}

