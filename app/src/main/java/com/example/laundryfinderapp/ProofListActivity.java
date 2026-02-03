package com.example.laundryfinderapp;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class ProofListActivity extends AppCompatActivity {

    private RecyclerView recyclerProofs;
    private TextView txtEmpty;

    private final ArrayList<ProofModel> list = new ArrayList<>();
    private ProofAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_proof_list);

        // Bind views
        recyclerProofs = findViewById(R.id.recyclerProofs);
        txtEmpty = findViewById(R.id.txtEmpty);
        TextView txtBack = findViewById(R.id.txtBack);

        // Back button
        txtBack.setOnClickListener(v -> finish());

        // RecyclerView setup
        adapter = new ProofAdapter(this, list);
        recyclerProofs.setLayoutManager(new LinearLayoutManager(this));
        recyclerProofs.setAdapter(adapter);

        loadProofs();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload list after edit / delete
        loadProofs();
    }

    private void loadProofs() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseDatabase.getInstance()
                .getReference("proofs")
                .child(uid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    list.clear();

                    for (DataSnapshot s : snapshot.getChildren()) {
                        ProofModel p = s.getValue(ProofModel.class);
                        if (p != null) {
                            list.add(p);
                        }
                    }

                    adapter.notifyDataSetChanged();

                    // Empty state
                    if (list.isEmpty()) {
                        txtEmpty.setVisibility(View.VISIBLE);
                        recyclerProofs.setVisibility(View.GONE);
                    } else {
                        txtEmpty.setVisibility(View.GONE);
                        recyclerProofs.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Load failed: " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show()
                );
    }
}

